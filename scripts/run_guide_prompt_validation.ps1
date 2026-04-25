[CmdletBinding()]
param(
    [ValidateSet('w', 'x', 'xx', 'y', 'z', 'af', 'ag', 'ah', 'ai', 'aj', 'ak', 'al', 'am', 'an', 'ao', 'ap', 'aq', 'ar', 'as', 'at', 'au', 'av', 'aw', 'ax', 'ay', 'az', 'ba', 'bb', 'bc', 'bd', 'be', 'bf', 'bg', 'bh', 'bi', 'bj', 'bk', 'bl', 'bm', 'bn', 'bo', 'bp', 'bq', 'br', 'bs', 'bt', 'bu', 'bv', 'bw', 'bx', 'by', 'bz', 'ca', 'cb', 'cc', 'cd', 'ce', 'cf', 'cg', 'ch', 'ci', 'cj', 'ck', 'cl', 'cm', 'cn', 'co', 'cp', 'cq', 'cr', 'cs', 'ct', 'cu', 'cv', 'cw', 'cx', 'cy', 'cz', 'da', 'db', 'dc', 'dd', 'de', 'df', 'dg', 'dh', 'di', 'dj', 'dk', 'dl', 'dm', 'dn', 'do', 'dp', 'dq', 'dr', 'ds', 'dt', 'du', 'dv', 'dw', 'dx', 'dy', 'dz', 'ea', 'eb', 'ec', 'ed', 'ee', 'ef', 'eg', 'eh', 'ei', 'ej', 'ek', 'el', 'em', 'en', 'eo', 'ep', 'eq', 'er', 'es', 'et', 'eu', 'ev', 'ew', 'ex', 'ey', 'ez', 'fa', 'fb', 'fc', 'fd', 'fe', 'all')]
    [string]$Wave = 'w',

    [string]$PythonPath = "$HOME\AppData\Local\Python\bin\python.exe",

    [string]$GenerationUrl = "http://127.0.0.1:1235/v1",

    [string]$GenerationModel = "gemma-4-e2b-it-litert",

    [string]$EmbedUrl = "http://127.0.0.1:1234/v1",

    [int]$TopK = 8,

    [string]$OutputDir = "artifacts/bench",

    [string[]]$ExtraBenchArgs = @(),

    [switch]$EnableCardBackedRuntimeAnswers
)

$ErrorActionPreference = 'Stop'

function Test-IsLiteRtValidationTarget {
    [CmdletBinding()]
    param(
        [string]$GenerationModel,
        [string]$GenerationUrl
    )

    $model = ([string]$GenerationModel).Trim().ToLowerInvariant()
    $url = ([string]$GenerationUrl).Trim().ToLowerInvariant()

    return $model.Contains('litert') -or $url.Contains('litert')
}

function Get-BenchJsonPath {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [string]$OutputPath
    )

    $extension = [System.IO.Path]::GetExtension($OutputPath)
    if ([string]::IsNullOrWhiteSpace($extension)) {
        return ($OutputPath + '.json')
    }

    return ($OutputPath.Substring(0, $OutputPath.Length - $extension.Length) + '.json')
}

function Write-Utf8NoBomFile {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [string]$Path,

        [Parameter(Mandatory)]
        [string]$Value
    )

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Value, $utf8NoBom)
}

function Get-LiteRtRetryDelayMilliseconds {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [ValidateRange(1, 2)]
        [int]$RetryNumber,

        [ValidateRange(0, 1000)]
        [int]$JitterMaximumMs = 125,

        [Nullable[int]]$FixedJitterMs
    )

    $baseDelayMs = switch ($RetryNumber) {
        1 { 200 }
        2 { 800 }
        default { throw "Unsupported LiteRT retry number: $RetryNumber" }
    }

    $jitterMs = if ($PSBoundParameters.ContainsKey('FixedJitterMs') -and $null -ne $FixedJitterMs) {
        [Math]::Max(0, [int]$FixedJitterMs)
    } else {
        Get-Random -Minimum 0 -Maximum ($JitterMaximumMs + 1)
    }

    return ($baseDelayMs + $jitterMs)
}

function Get-EffectiveCardBackedRuntimeAnswerFlag {
    [CmdletBinding()]
    param(
        [switch]$EnableCardBackedRuntimeAnswers
    )

    if ($EnableCardBackedRuntimeAnswers) {
        return '1'
    }

    $current = ([string]$env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS).Trim()
    if ([string]::IsNullOrWhiteSpace($current)) {
        return '0'
    }
    return $current
}

function Invoke-WithCardBackedRuntimeEnv {
    [CmdletBinding()]
    param(
        [switch]$EnableCardBackedRuntimeAnswers,

        [Parameter(Mandatory)]
        [scriptblock]$ScriptBlock
    )

    $hadPrevious = Test-Path Env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS
    $previous = $env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS
    if ($EnableCardBackedRuntimeAnswers) {
        $env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS = '1'
    }
    try {
        & $ScriptBlock
    }
    finally {
        if ($EnableCardBackedRuntimeAnswers) {
            if ($hadPrevious) {
                $env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS = $previous
            } else {
                Remove-Item Env:SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS -ErrorAction SilentlyContinue
            }
        }
    }
}

function Get-LiteRtHostFlakeInfo {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [string]$JsonPath,

        [string]$GenerationModel,

        [string]$GenerationUrl
    )

    $isLiteRtTarget = Test-IsLiteRtValidationTarget -GenerationModel $GenerationModel -GenerationUrl $GenerationUrl
    $info = [ordered]@{
        json_path = $JsonPath
        generation_model = $GenerationModel
        generation_url = $GenerationUrl
        is_litert_target = $isLiteRtTarget
        artifact_found = $false
        artifact_parse_error = $null
        result_count = 0
        error_count = 0
        server_error_500_count = 0
        prompt_indexes = @()
        servers = @()
        should_retry = $false
    }

    if (-not (Test-Path -LiteralPath $JsonPath)) {
        return [pscustomobject]$info
    }

    $info.artifact_found = $true

    try {
        $artifact = Get-Content -LiteralPath $JsonPath -Raw | ConvertFrom-Json
    }
    catch {
        $info.artifact_parse_error = $_.Exception.Message
        return [pscustomobject]$info
    }

    $results = @($artifact.results)
    $promptIndexes = New-Object System.Collections.Generic.List[int]
    $servers = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)

    foreach ($result in $results) {
        if ($null -eq $result) {
            continue
        }

        $info.result_count += 1

        $hasError = -not [string]::IsNullOrWhiteSpace([string]$result.error)
        if ($hasError) {
            $info.error_count += 1
        }

        $statusCode = $null
        if ($null -ne $result.error_status_code) {
            try {
                $statusCode = [int]$result.error_status_code
            }
            catch {
                $statusCode = $null
            }
        }

        if ($statusCode -ne 500) {
            continue
        }

        $category = ([string]$result.error_category).Trim().ToLowerInvariant()
        if ($category -and $category -ne 'server_error' -and $category -ne 'litert_host_flake') {
            continue
        }

        $info.server_error_500_count += 1

        if ($null -ne $result.index) {
            try {
                [void]$promptIndexes.Add([int]$result.index)
            }
            catch {
            }
        }

        $server = ([string]$result.server).Trim()
        if (-not [string]::IsNullOrWhiteSpace($server)) {
            [void]$servers.Add($server)
        }
    }

    $info.prompt_indexes = @($promptIndexes)
    $info.servers = @($servers)
    $info.should_retry = ($isLiteRtTarget -and $info.server_error_500_count -gt 0)

    return [pscustomobject]$info
}

function Set-LiteRtHostFlakeMarker {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [string]$JsonPath,

        [Parameter(Mandatory)]
        [string]$MarkdownPath,

        [Parameter(Mandatory)]
        [psobject]$FlakeInfo,

        [Parameter(Mandatory)]
        [string]$Wave,

        [Parameter(Mandatory)]
        [int]$AttemptCount,

        [int[]]$RetryDelaysMs = @()
    )

    $tag = 'litert_host_flake'
    $retryCount = [Math]::Max(0, $AttemptCount - 1)
    $delayLabel = if ($RetryDelaysMs.Count -gt 0) { ($RetryDelaysMs -join ', ') } else { 'none' }
    $generationUrl = ([string]$FlakeInfo.generation_url).Trim()
    $serverLabel = if ($FlakeInfo.servers.Count -gt 0) { ($FlakeInfo.servers -join ', ') } elseif (-not [string]::IsNullOrWhiteSpace($generationUrl)) { $generationUrl } else { 'unknown' }
    $promptIndexLabel = if ($FlakeInfo.prompt_indexes.Count -gt 0) { ($FlakeInfo.prompt_indexes -join ', ') } else { 'unknown' }

    if (Test-Path -LiteralPath $JsonPath) {
        $artifact = Get-Content -LiteralPath $JsonPath -Raw | ConvertFrom-Json
        if ($null -eq $artifact.summary) {
            $artifact | Add-Member -NotePropertyName summary -NotePropertyValue ([pscustomobject]@{}) -Force
        }

        $artifact.summary | Add-Member -NotePropertyName harness_failure_tag -NotePropertyValue $tag -Force
        $artifact.summary | Add-Member -NotePropertyName litert_harness_attempts -NotePropertyValue $AttemptCount -Force
        $artifact.summary | Add-Member -NotePropertyName litert_harness_retry_count -NotePropertyValue $retryCount -Force
        $artifact.summary | Add-Member -NotePropertyName litert_harness_retry_delays_ms -NotePropertyValue @($RetryDelaysMs) -Force
        $artifact.summary | Add-Member -NotePropertyName litert_server_error_500_count -NotePropertyValue $FlakeInfo.server_error_500_count -Force

        $artifact | Add-Member -NotePropertyName harness -NotePropertyValue ([pscustomobject]@{
            guide_prompt_validation = [pscustomobject]@{
                tag = $tag
                status = 'failed'
                wave = $Wave
                attempts = $AttemptCount
                retry_count = $retryCount
                retry_delays_ms = @($RetryDelaysMs)
                server_error_500_count = $FlakeInfo.server_error_500_count
                error_count = $FlakeInfo.error_count
                prompt_indexes = @($FlakeInfo.prompt_indexes)
                servers = @($FlakeInfo.servers)
                generation_model = $FlakeInfo.generation_model
                generation_url = $FlakeInfo.generation_url
            }
        }) -Force

        Write-Utf8NoBomFile -Path $JsonPath -Value ($artifact | ConvertTo-Json -Depth 100)
    }

    $markerLines = @(
        '**Harness failure:** `litert_host_flake`',
        "",
        ('*Wave: {0} | Attempts: {1} | Retry count: {2} | Retry delays ms: {3}*' -f $Wave, $AttemptCount, $retryCount, $delayLabel),
        ('*LiteRT HTTP 500 prompts: {0} | Prompt indexes: {1} | Servers: {2}*' -f $FlakeInfo.server_error_500_count, $promptIndexLabel, $serverLabel),
        ""
    )
    $markerBlock = ($markerLines -join [Environment]::NewLine)

    if (Test-Path -LiteralPath $MarkdownPath) {
        $currentMarkdown = Get-Content -LiteralPath $MarkdownPath -Raw
        if ($currentMarkdown -notmatch 'litert_host_flake') {
            Write-Utf8NoBomFile -Path $MarkdownPath -Value ($markerBlock + [Environment]::NewLine + $currentMarkdown)
        }
    } else {
        Write-Utf8NoBomFile -Path $MarkdownPath -Value $markerBlock
    }
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$benchPath = Join-Path $repoRoot 'bench.py'
$outputRoot = Join-Path $repoRoot $OutputDir

$waveMap = @{
    w = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_w_20260413.txt'
    x = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_x_20260413.txt'
    xx = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_xx_abstain_20260417.txt'
    y = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_y_20260413.txt'
    z = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_z_20260415.txt'
    af = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_af_20260415.txt'
    ag = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ag_20260415.txt'
    ah = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ah_20260415.txt'
    ai = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ai_20260415.txt'
    aj = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_aj_20260416.txt'
    ak = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ak_20260416.txt'
    al = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_al_20260416.txt'
    am = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_am_20260416.txt'
    an = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_an_20260416.txt'
    ao = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ao_20260416.txt'
    ap = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ap_20260416.txt'
    aq = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_aq_20260416.txt'
    ar = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ar_20260416.txt'
    as = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_as_20260416.txt'
    at = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_at_20260416.txt'
    au = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_au_20260416.txt'
    av = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_av_20260416.txt'
    aw = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_aw_20260416.txt'
    ax = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ax_20260416.txt'
    ay = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ay_20260416.txt'
    az = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_az_20260416.txt'
    ba = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ba_20260417.txt'
    bb = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bb_20260417.txt'
    bc = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bc_20260417.txt'
    bd = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bd_20260417.txt'
    be = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_be_20260417.txt'
    bf = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bf_20260417.txt'
    bg = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bg_20260417.txt'
    bh = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bh_20260417.txt'
    bi = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bi_20260417.txt'
    bj = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bj_20260417.txt'
    bk = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bk_20260417.txt'
    bl = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bl_20260417.txt'
    bm = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bm_20260417.txt'
    bn = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bn_20260417.txt'
    bo = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bo_20260417.txt'
    bp = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bp_20260417.txt'
    bq = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bq_20260417.txt'
    br = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_br_20260417.txt'
    bs = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bs_20260417.txt'
    bt = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bt_20260417.txt'
    bu = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bu_20260417.txt'
    bv = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bv_20260417.txt'
    bw = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bw_20260417.txt'
    bx = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bx_20260417.txt'
    by = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_by_20260417.txt'
    bz = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_bz_20260417.txt'
    ca = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ca_20260417.txt'
    cb = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cb_20260417.txt'
    cc = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cc_20260417.txt'
    cd = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cd_20260417.txt'
    ce = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ce_20260417.txt'
    cf = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cf_20260417.txt'
    cg = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cg_20260417.txt'
    ch = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ch_20260417.txt'
    ci = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ci_20260417.txt'
    cj = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cj_20260417.txt'
    ck = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ck_20260417.txt'
    cl = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cl_20260417.txt'
    cm = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cm_20260417.txt'
    cn = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cn_20260417.txt'
    co = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_co_20260417.txt'
    cp = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cp_20260417.txt'
    cq = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cq_20260417.txt'
    cr = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cr_20260417.txt'
    cs = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cs_20260417.txt'
    ct = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ct_20260417.txt'
    cu = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cu_20260417.txt'
    cv = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cv_20260417.txt'
    cw = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cw_20260417.txt'
    cx = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cx_20260417.txt'
    cy = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cy_20260417.txt'
    cz = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_cz_20260417.txt'
    da = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_da_20260417.txt'
    db = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_db_20260417.txt'
    dc = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dc_20260417.txt'
    dd = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dd_20260417.txt'
    de = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_de_20260417.txt'
    df = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_df_20260417.txt'
    dg = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dg_20260417.txt'
    dh = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dh_20260417.txt'
    di = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_di_20260417.txt'
    dj = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dj_20260417.txt'
    dk = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dk_20260417.txt'
    dl = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dl_20260417.txt'
    dm = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dm_20260417.txt'
    dn = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dn_20260417.txt'
    do = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_do_20260417.txt'
    dp = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dp_20260417.txt'
    dq = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dq_20260417.txt'
    dr = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dr_20260417.txt'
    ds = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ds_20260417.txt'
    dt = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dt_20260417.txt'
    du = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_du_20260417.txt'
    dv = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dv_20260417.txt'
    dw = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dw_20260417.txt'
    dx = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dx_20260417.txt'
    dy = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dy_20260417.txt'
    dz = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_dz_20260417.txt'
    ea = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ea_20260417.txt'
    eb = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_eb_20260417.txt'
    ec = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ec_20260417.txt'
    ed = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ed_20260417.txt'
    ee = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ee_20260417.txt'
    ef = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ef_20260417.txt'
    eg = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_eg_20260417.txt'
    eh = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_eh_20260417.txt'
    ei = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ei_20260417.txt'
    ej = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ej_20260417.txt'
    ek = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ek_20260417.txt'
    el = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_el_20260417.txt'
    em = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_em_20260417.txt'
    en = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_en_20260417.txt'
    eo = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_eo_20260417.txt'
    ep = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ep_20260417.txt'
    eq = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_eq_20260417.txt'
    er = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_er_20260417.txt'
    es = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_es_20260417.txt'
    et = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_et_20260417.txt'
    eu = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_eu_20260417.txt'
    ev = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ev_20260417.txt'
    ew = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ew_20260417.txt'
    ex = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ex_20260417.txt'
    ey = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ey_20260417.txt'
    ez = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_ez_20260417.txt'
    fa = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_fa_20260417.txt'
    fb = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_fb_20260417.txt'
    fc = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_fc_20260417.txt'
    fd = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_fd_20260417.txt'
    fe = Join-Path $repoRoot 'artifacts\prompts\adhoc\test_targeted_guide_direction_wave_fe_20260417.txt'
}

$waveTopKOverrides = @{
    be = 5
    bf = 6
    bh = 6
    bm = 6
    bn = 6
    bq = 6
    cg = 6
    de = 6
    fd = 5
    bi = 6
    ci = 6
    cj = 6
    ck = 6
    cl = 6
    cm = 6
    cn = 6
    co = 6
    cp = 6
    cq = 6
    cr = 6
    cs = 6
    ct = 6
    cu = 6
    cv = 6
    cw = 6
    cx = 6
    cy = 6
    cz = 6
    da = 6
    db = 6
    dc = 6
    df = 6
    dn = 6
    ee = 6
    ez = 6
}

$waveExtraBenchArgsMap = @{
    de = @('--max-completion-tokens', '768')
    fd = @('--max-completion-tokens', '768')
}

if (-not (Test-Path -LiteralPath $PythonPath)) {
    throw "Python executable not found at: $PythonPath"
}

if (-not (Test-Path -LiteralPath $benchPath)) {
    throw "bench.py not found at: $benchPath"
}

function Invoke-GuidePromptValidation {
    [CmdletBinding()]
    param(
        [string]$Wave,
        [string]$PythonPath,
        [string]$GenerationUrl,
        [string]$GenerationModel,
        [string]$EmbedUrl,
        [int]$TopK,
        [string]$OutputDir,
        [string[]]$ExtraBenchArgs,
        [switch]$EnableCardBackedRuntimeAnswers
    )

    New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null

    $wavesToRun = if ($Wave -eq 'all') { @('w', 'x', 'xx', 'y', 'z', 'af', 'ag', 'ah', 'ai', 'aj', 'ak', 'al', 'am', 'an', 'ao', 'ap', 'aq', 'ar', 'as', 'at', 'au', 'av', 'aw', 'ax', 'ay', 'az', 'ba', 'bb', 'bc', 'bd', 'be', 'bf', 'bg', 'bh', 'bi', 'bj', 'bk', 'bl', 'bm', 'bn', 'bo', 'bp', 'bq', 'br', 'bs', 'bt', 'bu', 'bv', 'bw', 'bx', 'by', 'bz', 'ca', 'cb', 'cc', 'cd', 'ce', 'cf', 'cg', 'ch', 'ci', 'cj', 'ck', 'cl', 'cm', 'cn', 'co', 'cp', 'cq', 'cr', 'cs', 'ct', 'cu', 'cv', 'cw', 'cx', 'cy', 'cz', 'da', 'db', 'dc', 'dd', 'de', 'df', 'dg', 'dh', 'di', 'dj', 'dk', 'dl', 'dm', 'dn', 'do', 'dp', 'dq', 'dr', 'ds', 'dt', 'du', 'dv', 'dw', 'dx', 'dy', 'dz', 'ea', 'eb', 'ec', 'ed', 'ee', 'ef', 'eg', 'eh', 'ei', 'ej', 'ek', 'el', 'em', 'en', 'eo', 'ep', 'eq', 'er', 'es', 'et', 'eu', 'ev', 'ew', 'ex', 'ey', 'ez', 'fa', 'fb', 'fc', 'fd', 'fe') } else { @($Wave) }
    $timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'

    Push-Location $repoRoot
    try {
        foreach ($currentWave in $wavesToRun) {
            $promptPath = $waveMap[$currentWave]
            if (-not (Test-Path -LiteralPath $promptPath)) {
                throw "Prompt pack not found for wave '$currentWave': $promptPath"
            }

            $effectiveTopK = if ($waveTopKOverrides.ContainsKey($currentWave)) {
                [Math]::Min($TopK, [int]$waveTopKOverrides[$currentWave])
            } else {
                $TopK
            }
            $waveExtraBenchArgs = if ($waveExtraBenchArgsMap.ContainsKey($currentWave)) {
                $waveExtraBenchArgsMap[$currentWave]
            } else {
                @()
            }

            $effectiveGenerationUrl = $GenerationUrl
            $effectiveGenerationModel = $GenerationModel
            $isLiteRtTarget = Test-IsLiteRtValidationTarget -GenerationModel $effectiveGenerationModel -GenerationUrl $effectiveGenerationUrl
            $maxAttempts = if ($isLiteRtTarget) { 3 } else { 1 }
            $retryDelaysMs = New-Object System.Collections.Generic.List[int]

            $outputPath = Join-Path $outputRoot ("guide_wave_{0}_{1}.md" -f $currentWave, $timestamp)
            $jsonPath = Get-BenchJsonPath -OutputPath $outputPath
            $benchArgs = @(
                $benchPath,
                '--prompts', $promptPath,
                '--output', $outputPath,
                '--urls', $effectiveGenerationUrl,
                '--worker-models', $effectiveGenerationModel,
                '--top-k', $effectiveTopK
            ) + $waveExtraBenchArgs + $ExtraBenchArgs

            if (-not [string]::IsNullOrWhiteSpace($EmbedUrl)) {
                $benchArgs += @('--embed-url', $EmbedUrl)
            }

            for ($attempt = 1; $attempt -le $maxAttempts; $attempt++) {
                Write-Host ("Running guide prompt validation wave '{0}' (attempt {1}/{2})..." -f $currentWave, $attempt, $maxAttempts)
                Write-Host ("  prompts: {0}" -f $promptPath)
                Write-Host ("  output : {0}" -f $outputPath)
                Write-Host ("  gen url : {0}" -f $effectiveGenerationUrl)
                Write-Host ("  model   : {0}" -f $effectiveGenerationModel)
                Write-Host ("  top_k   : {0}" -f $effectiveTopK)
                Write-Host ("  embed   : {0}" -f $EmbedUrl)
                Write-Host ("  reviewed-card runtime answers: {0}" -f (Get-EffectiveCardBackedRuntimeAnswerFlag -EnableCardBackedRuntimeAnswers:$EnableCardBackedRuntimeAnswers))

                Invoke-WithCardBackedRuntimeEnv -EnableCardBackedRuntimeAnswers:$EnableCardBackedRuntimeAnswers -ScriptBlock {
                    & $PythonPath @benchArgs
                }
                if ($LASTEXITCODE -ne 0) {
                    throw "bench.py failed for wave '$currentWave' with exit code $LASTEXITCODE"
                }

                $flakeInfo = Get-LiteRtHostFlakeInfo -JsonPath $jsonPath -GenerationModel $effectiveGenerationModel -GenerationUrl $effectiveGenerationUrl
                if (-not $flakeInfo.should_retry) {
                    break
                }

                if ($attempt -ge $maxAttempts) {
                    Set-LiteRtHostFlakeMarker -JsonPath $jsonPath -MarkdownPath $outputPath -FlakeInfo $flakeInfo -Wave $currentWave -AttemptCount $attempt -RetryDelaysMs @($retryDelaysMs)
                    $promptIndexes = @($flakeInfo.prompt_indexes)
                    $servers = @($flakeInfo.servers)
                    throw ('bench.py recorded persistent LiteRT HTTP 500 failures for wave ''{0}'' after {1} attempts (`litert_host_flake`); prompts={2}; servers={3}' -f $currentWave, $attempt, ($promptIndexes -join ', '), ($servers -join ', '))
                }

                $delayMs = Get-LiteRtRetryDelayMilliseconds -RetryNumber $attempt
                [void]$retryDelaysMs.Add($delayMs)
                Write-Warning ("LiteRT host flake detected for wave '{0}' on attempt {1}/{2}; retrying after {3} ms." -f $currentWave, $attempt, $maxAttempts, $delayMs)
                Start-Sleep -Milliseconds $delayMs
            }
        }
    }
    finally {
        Pop-Location
    }
}

if ($MyInvocation.InvocationName -ne '.') {
    Invoke-GuidePromptValidation `
        -Wave $Wave `
        -PythonPath $PythonPath `
        -GenerationUrl $GenerationUrl `
        -GenerationModel $GenerationModel `
        -EmbedUrl $EmbedUrl `
        -TopK $TopK `
        -OutputDir $OutputDir `
        -ExtraBenchArgs $ExtraBenchArgs `
        -EnableCardBackedRuntimeAnswers:$EnableCardBackedRuntimeAnswers
}

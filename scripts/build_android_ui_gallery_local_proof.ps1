param(
    [string]$OutputRoot = "artifacts/android_ui_gallery_local_proof",
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [switch]$SkipHostStates,
    [string]$HostInferenceUrl = "http://10.0.2.2:1235/v1",
    [string]$HostInferenceModel = "gemma-4-e2b-it-litert",
    [int]$MaxParallelDevices = 4,
    [string[]]$RoleFilter = @(),
    [switch]$PlanOnly,
    [switch]$SkipPlanValidation
)

$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$statePackScript = Join-Path $PSScriptRoot "build_android_ui_state_pack_parallel.ps1"
$planValidator = Join-Path $PSScriptRoot "validate_android_ui_state_pack_plan.py"

if (-not (Test-Path -LiteralPath $statePackScript)) {
    throw "build_android_ui_state_pack_parallel.ps1 not found at $statePackScript"
}

function ConvertTo-RepoRelativePath {
    param([string]$Path)

    $fullPath = [System.IO.Path]::GetFullPath($Path)
    $rootPath = [System.IO.Path]::GetFullPath($repoRoot)
    if (-not $rootPath.EndsWith([System.IO.Path]::DirectorySeparatorChar)) {
        $rootPath = $rootPath + [System.IO.Path]::DirectorySeparatorChar
    }

    if ($fullPath.StartsWith($rootPath, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $fullPath.Substring($rootPath.Length)
    }

    return $fullPath
}

function Get-LatestRunDirectory {
    param([string]$Root)

    $resolvedRoot = Join-Path $repoRoot $Root
    if (-not (Test-Path -LiteralPath $resolvedRoot)) {
        return $null
    }

    return Get-ChildItem -LiteralPath $resolvedRoot -Directory |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1
}

function Write-GalleryRunSheet {
    param(
        [string]$RunDirectory,
        [bool]$IsPlanOnly
    )

    $relativeRunDirectory = ConvertTo-RepoRelativePath -Path $RunDirectory
    $runSheetPath = Join-Path $RunDirectory "GALLERY_LOCAL_PROOF.md"
    $summaryPath = Join-Path $RunDirectory "summary.json"
    $manifestPath = Join-Path $RunDirectory "manifest.json"
    $planPath = Join-Path $RunDirectory "plan.json"

    $lines = @(
        "# Android UI Gallery Local Proof",
        "",
        "This folder is a local design-review aid for the task-based Android UI gallery.",
        "It does not create acceptance evidence by itself; the fixed four-posture state pack remains the UI acceptance proof.",
        "",
        "## Run Mode",
        "",
        ('- Plan only: `{0}`' -f ([string]$IsPlanOnly).ToLowerInvariant()),
        ('- Output directory: `{0}`' -f $relativeRunDirectory),
        "",
        "## Acceptance Boundary",
        "",
        '- Passing `summary.json` from the wrapped state-pack command is required before using screenshots as acceptance proof.',
        '- `plan.json`, this run sheet, and any hand-selected gallery ordering are non-acceptance review context.',
        "- Reviewed emergency shots must be labeled developer/test-gated unless product runtime exposure has been separately approved.",
        "- Gallery review can approve visual direction only; it does not replace regression, source, handoff, or state assertions.",
        "",
        "## Primary Artifact Paths",
        "",
        ('- Summary: `{0}`' -f $(if (Test-Path -LiteralPath $summaryPath) { ConvertTo-RepoRelativePath -Path $summaryPath } else { "not generated in this mode" })),
        ('- Manifest: `{0}`' -f $(if (Test-Path -LiteralPath $manifestPath) { ConvertTo-RepoRelativePath -Path $manifestPath } else { "not generated in this mode" })),
        ('- Plan: `{0}`' -f $(if (Test-Path -LiteralPath $planPath) { ConvertTo-RepoRelativePath -Path $planPath } else { "not generated in this mode" })),
        ('- Screenshots: `{0}`' -f (ConvertTo-RepoRelativePath -Path (Join-Path $RunDirectory "screenshots"))),
        ('- UI dumps: `{0}`' -f (ConvertTo-RepoRelativePath -Path (Join-Path $RunDirectory "dumps"))),
        ('- Per-state summaries: `{0}`' -f (ConvertTo-RepoRelativePath -Path (Join-Path $RunDirectory "summaries"))),
        ('- Parallel logs: `{0}`' -f (ConvertTo-RepoRelativePath -Path (Join-Path $RunDirectory "parallel_logs"))),
        "",
        "## Task Gallery Assembly Order",
        "",
        '1. G-01 first run / pack ready: start with `homeEntryShowsPrimaryBrowseAndAskLanes`.',
        '2. G-02 Library browse / search: use `searchQueryShowsResultsWithoutShellPolling` and linked-guide handoff screenshots.',
        "3. G-03 guide open / reader: use guide connection, related-guide, and tablet source-context states.",
        "4. G-04 standard answer: use deterministic detail, host generative detail, and neutral provenance states.",
        "5. G-05 reviewed emergency answer: add only separately captured developer/test-gated reviewed-card shots.",
        '6. G-06 follow-up thread: use `scriptedSeededFollowUpThreadShowsInlineHistory`.',
        "7. G-07 cross-reference / source navigation: use anchored cross-reference and off-rail cross-reference states.",
        "8. G-08 through G-11 edge states: add no-pack, slow-model, abstain, and large-font captures when separately available.",
        "",
        "## Posture Matrix",
        "",
        '- `phone_portrait`: `emulator-5556`',
        '- `phone_landscape`: `emulator-5560`',
        '- `tablet_portrait`: `emulator-5554`',
        '- `tablet_landscape`: `emulator-5558`'
    )

    $lines | Set-Content -LiteralPath $runSheetPath -Encoding UTF8
    return $runSheetPath
}

$statePackArgs = @(
    "-NoProfile",
    "-NonInteractive",
    "-ExecutionPolicy", "Bypass",
    "-File", $statePackScript,
    "-OutputRoot", $OutputRoot,
    "-HostInferenceUrl", $HostInferenceUrl,
    "-HostInferenceModel", $HostInferenceModel,
    "-MaxParallelDevices", ([string]$MaxParallelDevices)
)

foreach ($role in @($RoleFilter)) {
    if (-not [string]::IsNullOrWhiteSpace([string]$role)) {
        $statePackArgs += @("-RoleFilter", [string]$role)
    }
}
if ($SkipBuild) {
    $statePackArgs += "-SkipBuild"
}
if ($SkipInstall) {
    $statePackArgs += "-SkipInstall"
}
if ($SkipHostStates) {
    $statePackArgs += "-SkipHostStates"
}
if ($PlanOnly) {
    $statePackArgs += "-PlanOnly"
}

& powershell @statePackArgs
$statePackExitCode = $LASTEXITCODE

$latestRun = Get-LatestRunDirectory -Root $OutputRoot
if ($null -eq $latestRun) {
    throw "Wrapped state-pack command did not create a run directory under $OutputRoot"
}

$runSheetPath = Write-GalleryRunSheet -RunDirectory $latestRun.FullName -IsPlanOnly ([bool]$PlanOnly)
Write-Host ("Gallery run sheet: {0}" -f $runSheetPath)

if ($PlanOnly -and -not $SkipPlanValidation) {
    $planPath = Join-Path $latestRun.FullName "plan.json"
    if (-not (Test-Path -LiteralPath $planPath)) {
        throw "PlanOnly run did not produce plan.json at $planPath"
    }
    if (-not (Test-Path -LiteralPath $planValidator)) {
        throw "Plan validator not found at $planValidator"
    }

    $python = Join-Path $repoRoot ".venvs\senku-validate\Scripts\python.exe"
    if (-not (Test-Path -LiteralPath $python)) {
        $python = "python"
    }
    & $python -B $planValidator $planPath
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

if ($statePackExitCode -ne 0) {
    exit $statePackExitCode
}

exit 0

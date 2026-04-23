#!/usr/bin/env pwsh
<#
.SYNOPSIS
Start a non-blocking Qwen27 scout task and return job metadata.

.DESCRIPTION
Creates metadata artifacts under `artifacts/qwen_jobs` and launches a detached
PowerShell worker process that runs `invoke_qwen27_scout.ps1` and writes final
status/output back to metadata.

.EXAMPLE
.\scripts\start_qwen27_scout_job.ps1 -Message "Reply with exactly READY" -MaxTokens 64

.EXAMPLE
.\scripts\start_qwen27_scout_job.ps1 -PromptFile .\prompt.txt -OutputDir artifacts\qwen_jobs
#>

[CmdletBinding(DefaultParameterSetName = 'Text')]
param(
    [Parameter(Mandatory = $true, Position = 0, ParameterSetName = 'Text')]
    [string]$Message,

    [Parameter(Mandatory = $true, Position = 0, ParameterSetName = 'File')]
    [string]$PromptFile,

    [int]$MaxTokens = 256,
    [string]$Label = '',
    [string]$RouteHint = '',
    [string]$OutputDir = 'artifacts/qwen_jobs',
    [string]$Endpoint = 'http://127.0.0.1:1234/v1/chat/completions',
    [string]$Model = 'qwen27_58k'
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$repoRoot = Split-Path -Parent $PSScriptRoot
$resolvedOutputDir = if ([System.IO.Path]::IsPathRooted($OutputDir)) {
    $OutputDir
} else {
    Join-Path $repoRoot $OutputDir
}
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

function Get-QwenPromptPreview {
    param(
        [string]$Text,
        [int]$MaxChars = 160
    )

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return ''
    }

    $singleLine = ($Text -replace '\s+', ' ').Trim()
    if ($singleLine.Length -le $MaxChars) {
        return $singleLine
    }

    return ($singleLine.Substring(0, $MaxChars) + '...')
}

$invokeScript = Join-Path $PSScriptRoot 'invoke_qwen27_scout.ps1'
$workerScript = Join-Path $PSScriptRoot 'run_qwen27_scout_job_worker.ps1'
if (-not (Test-Path -LiteralPath $invokeScript)) {
    throw "Required script not found: $invokeScript"
}
if (-not (Test-Path -LiteralPath $workerScript)) {
    throw "Required script not found: $workerScript"
}
if ($PSCmdlet.ParameterSetName -eq 'File' -and -not (Test-Path -LiteralPath $PromptFile)) {
    throw "Prompt file not found: $PromptFile"
}

$jobId = [guid]::NewGuid().ToString()
$metadataPath = Join-Path $resolvedOutputDir ("qwen27_scout_{0}.metadata.json" -f $jobId)
$resultPath = Join-Path $resolvedOutputDir ("qwen27_scout_{0}.result.json" -f $jobId)
$stdoutPath = Join-Path $resolvedOutputDir ("qwen27_scout_{0}.stdout.log" -f $jobId)
$stderrPath = Join-Path $resolvedOutputDir ("qwen27_scout_{0}.stderr.log" -f $jobId)

$createdAt = (Get-Date).ToString('o')
$metadata = [ordered]@{
    job_id         = $jobId
    label          = $Label
    route_hint     = $RouteHint
    status         = 'queued'
    prompt_mode    = $PSCmdlet.ParameterSetName
    prompt_text    = if ($PSCmdlet.ParameterSetName -eq 'Text') { $Message } else { '' }
    prompt_file    = if ($PSCmdlet.ParameterSetName -eq 'File') { (Resolve-Path -LiteralPath $PromptFile).Path } else { '' }
    prompt_preview = if ($PSCmdlet.ParameterSetName -eq 'Text') { Get-QwenPromptPreview -Text $Message } else { Get-QwenPromptPreview -Text (Get-Content -LiteralPath $PromptFile -Raw) }
    endpoint       = $Endpoint
    model          = $Model
    max_tokens     = $MaxTokens
    output_dir     = $resolvedOutputDir
    metadata_path  = $metadataPath
    result_path    = $resultPath
    stdout_path    = $stdoutPath
    stderr_path    = $stderrPath
    created_at     = $createdAt
    started_at     = $null
    completed_at   = $null
    worker_pid     = $null
    succeeded      = $null
    output_text    = $null
    model_id       = $null
    error          = $null
}
$metadata | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $metadataPath -Encoding UTF8

$workerArgs = @(
    '-ExecutionPolicy', 'Bypass',
    '-NoProfile',
    '-File', $workerScript,
    '-InvokeScript', $invokeScript,
    '-MetadataPath', $metadataPath,
    '-ResultPath', $resultPath
)

$proc = Start-Process -FilePath powershell -ArgumentList $workerArgs -WindowStyle Hidden -PassThru `
    -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath

$metadata.worker_pid = $proc.Id
$metadata.status = 'running'
$metadata.started_at = (Get-Date).ToString('o')
$metadata | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $metadataPath -Encoding UTF8

[pscustomobject]@{
    ok            = $true
    job_id        = $jobId
    label         = $Label
    route_hint    = $RouteHint
    status        = 'running'
    worker_pid    = $proc.Id
    metadata_path = $metadataPath
    result_path   = $resultPath
    model         = $Model
    endpoint      = $Endpoint
} | ConvertTo-Json -Depth 10

#!/usr/bin/env pwsh
<#
.SYNOPSIS
Poll an async Qwen27 scout job started by start_qwen27_scout_job.ps1.

.EXAMPLE
.\scripts\get_qwen27_scout_job.ps1 -JobId "<GUID>"

.EXAMPLE
.\scripts\get_qwen27_scout_job.ps1 -MetadataFile .\artifacts\qwen_jobs\qwen27_scout_<GUID>.metadata.json
#>

[CmdletBinding(DefaultParameterSetName = 'ByJobId')]
param(
    [Parameter(Mandatory = $true, ParameterSetName = 'ByJobId')]
    [string]$JobId,

    [Parameter(Mandatory = $true, ParameterSetName = 'ByMetadataFile')]
    [string]$MetadataFile,

    [string]$OutputDir = 'artifacts/qwen_jobs'
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$repoRoot = Split-Path -Parent $PSScriptRoot
$resolvedOutputDir = if ([System.IO.Path]::IsPathRooted($OutputDir)) {
    $OutputDir
} else {
    Join-Path $repoRoot $OutputDir
}

function Resolve-MetadataPath {
    if ($PSCmdlet.ParameterSetName -eq 'ByMetadataFile') {
        if ([System.IO.Path]::IsPathRooted($MetadataFile)) {
            return $MetadataFile
        }
        return Join-Path $repoRoot $MetadataFile
    }
    return Join-Path $resolvedOutputDir ("qwen27_scout_{0}.metadata.json" -f $JobId)
}

$metadataPath = Resolve-MetadataPath
if (-not (Test-Path -LiteralPath $metadataPath)) {
    throw "Metadata file not found: $metadataPath"
}

$metadata = Get-Content -LiteralPath $metadataPath -Raw | ConvertFrom-Json
$status = [string]$metadata.status
$workerPid = if ($null -ne $metadata.worker_pid -and "$($metadata.worker_pid)".Trim() -ne '') { [int]$metadata.worker_pid } else { $null }

if (($status -eq 'running' -or $status -eq 'queued') -and $workerPid) {
    $worker = Get-Process -Id $workerPid -ErrorAction SilentlyContinue
    if ($null -eq $worker) {
        $status = 'failed'
        $metadata.status = 'failed'
        if ([string]::IsNullOrWhiteSpace([string]$metadata.error)) {
            $stderrText = ''
            if ($metadata.PSObject.Properties.Name -contains 'stderr_path' -and -not [string]::IsNullOrWhiteSpace([string]$metadata.stderr_path)) {
                $stderrPath = [string]$metadata.stderr_path
                if (Test-Path -LiteralPath $stderrPath) {
                    $stderrText = (Get-Content -LiteralPath $stderrPath -Raw -ErrorAction SilentlyContinue).Trim()
                }
            }
            $metadata.error = if ([string]::IsNullOrWhiteSpace($stderrText)) {
                "Worker process is no longer running."
            } else {
                $stderrText
            }
        }
        if ([string]::IsNullOrWhiteSpace([string]$metadata.completed_at)) {
            $metadata.completed_at = (Get-Date).ToString('o')
        }
        $metadata | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $metadataPath -Encoding UTF8
    }
}

$complete = $status -in @('completed', 'failed')

[pscustomobject]@{
    job_id        = [string]$metadata.job_id
    label         = if ($metadata.PSObject.Properties.Name -contains 'label') { [string]$metadata.label } else { '' }
    route_hint    = if ($metadata.PSObject.Properties.Name -contains 'route_hint') { [string]$metadata.route_hint } else { '' }
    metadata_file = $metadataPath
    status        = $status
    complete      = $complete
    worker_pid    = if ($workerPid) { $workerPid } else { $null }
    created_at    = [string]$metadata.created_at
    started_at    = [string]$metadata.started_at
    completed_at  = [string]$metadata.completed_at
    succeeded     = if ($null -ne $metadata.succeeded) { [bool]$metadata.succeeded } else { $null }
    prompt_preview = if ($metadata.PSObject.Properties.Name -contains 'prompt_preview') { [string]$metadata.prompt_preview } else { $null }
    output_text   = if ($complete -and -not [string]::IsNullOrWhiteSpace([string]$metadata.output_text)) { [string]$metadata.output_text } else { $null }
    model_id      = if ($complete -and -not [string]::IsNullOrWhiteSpace([string]$metadata.model_id)) { [string]$metadata.model_id } else { $null }
    error         = if (-not [string]::IsNullOrWhiteSpace([string]$metadata.error)) { [string]$metadata.error } else { $null }
} | ConvertTo-Json -Depth 10

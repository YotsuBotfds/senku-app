#!/usr/bin/env pwsh
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$InvokeScript,

    [Parameter(Mandatory = $true)]
    [string]$MetadataPath,

    [Parameter(Mandatory = $true)]
    [string]$ResultPath
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

function Save-Metadata {
    param(
        [string]$Status,
        [bool]$Succeeded,
        [string]$OutputText,
        [string]$ModelId,
        [string]$ErrorText
    )

    $payload = @{}
    if (Test-Path -LiteralPath $MetadataPath) {
        try {
            $existing = Get-Content -LiteralPath $MetadataPath -Raw | ConvertFrom-Json -ErrorAction Stop
            foreach ($p in $existing.PSObject.Properties) {
                $payload[$p.Name] = $p.Value
            }
        } catch {
        }
    }

    $payload.status = $Status
    $payload.succeeded = $Succeeded
    $payload.output_text = $OutputText
    $payload.model_id = $ModelId
    $payload.error = $ErrorText
    $payload.completed_at = (Get-Date).ToString('o')
    if (-not $payload.ContainsKey('worker_pid')) {
        $payload.worker_pid = $PID
    } else {
        $payload.worker_pid = $PID
    }

    $payload | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $MetadataPath -Encoding UTF8
}

function Get-AssistantTextFromResponse {
    param($Response)

    if ($null -eq $Response -or $null -eq $Response.choices -or $Response.choices.Count -le 0) {
        return ''
    }
    $message = $Response.choices[0].message
    if ($null -eq $message) {
        return ''
    }

    $content = [string]$message.content
    if (-not [string]::IsNullOrWhiteSpace($content)) {
        return $content
    }

    $reasoningContent = [string]$message.reasoning_content
    if (-not [string]::IsNullOrWhiteSpace($reasoningContent)) {
        return $reasoningContent
    }

    return ''
}

try {
    if (-not (Test-Path -LiteralPath $InvokeScript)) {
        throw "invoke script missing: $InvokeScript"
    }
    if (-not (Test-Path -LiteralPath $MetadataPath)) {
        throw "metadata missing: $MetadataPath"
    }

    $metadata = Get-Content -LiteralPath $MetadataPath -Raw | ConvertFrom-Json -ErrorAction Stop
    $endpoint = [string]$metadata.endpoint
    $model = [string]$metadata.model
    $promptMode = [string]$metadata.prompt_mode
    $promptFile = [string]$metadata.prompt_file
    $promptText = [string]$metadata.prompt_text
    $maxTokens = if ($null -ne $metadata.max_tokens) { [int]$metadata.max_tokens } else { 256 }

    if ([string]::IsNullOrWhiteSpace($endpoint)) {
        throw "metadata.endpoint is empty"
    }
    if ([string]::IsNullOrWhiteSpace($model)) {
        throw "metadata.model is empty"
    }
    if ($promptMode -eq 'File' -and -not (Test-Path -LiteralPath $promptFile)) {
        throw "prompt file missing: $promptFile"
    }
    if ($promptMode -eq 'Text' -and [string]::IsNullOrWhiteSpace($promptText)) {
        throw "metadata.prompt_text is empty"
    }

    $invokeParams = @{
        Endpoint = $endpoint
        Model = $model
        MaxTokens = $maxTokens
        AsJson = $true
    }
    if ($promptMode -eq 'File') {
        $invokeParams.PromptFile = $promptFile
    } else {
        $invokeParams.Message = $promptText
    }

    $raw = & $InvokeScript @invokeParams
    $rawText = if ($null -eq $raw) { '' } else { ($raw -join "`n") }
    if ([string]::IsNullOrWhiteSpace($rawText)) {
        throw "No response text returned by invoke_qwen27_scout.ps1"
    }

    $response = $rawText | ConvertFrom-Json -ErrorAction Stop
    $outputText = ''
    $modelId = $model
    if ($null -ne $response.model -and -not [string]::IsNullOrWhiteSpace([string]$response.model)) {
        $modelId = [string]$response.model
    }
    $outputText = Get-AssistantTextFromResponse -Response $response
    if ([string]::IsNullOrWhiteSpace($outputText)) {
        throw "Model returned empty content."
    }

    [pscustomobject]@{
        ok          = $true
        model       = $modelId
        output_text = $outputText
        completed_at = (Get-Date).ToString('o')
    } | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $ResultPath -Encoding UTF8

    Save-Metadata -Status 'completed' -Succeeded $true -OutputText $outputText -ModelId $modelId -ErrorText ''
}
catch {
    $msg = $_.Exception.Message
    [pscustomobject]@{
        ok = $false
        error = $msg
        completed_at = (Get-Date).ToString('o')
    } | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $ResultPath -Encoding UTF8

    $fallbackModel = ''
    try {
        if (Test-Path -LiteralPath $MetadataPath) {
            $m = Get-Content -LiteralPath $MetadataPath -Raw | ConvertFrom-Json -ErrorAction SilentlyContinue
            if ($null -ne $m -and $m.PSObject.Properties.Name -contains 'model') {
                $fallbackModel = [string]$m.model
            }
        }
    } catch {
    }
    Save-Metadata -Status 'failed' -Succeeded $false -OutputText '' -ModelId $fallbackModel -ErrorText $msg
}

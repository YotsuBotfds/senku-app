#!/usr/bin/env pwsh

[CmdletBinding(DefaultParameterSetName = 'Text')]
param(
    [Parameter(Mandatory = $true, Position = 0, ParameterSetName = 'Text')]
    [string]$Message,

    [Parameter(Mandatory = $true, Position = 0, ParameterSetName = 'File')]
    [string]$PromptFile,

    [string]$Endpoint = 'http://127.0.0.1:1234/v1/chat/completions',
    [string]$Model = 'qwen3.5-27b',
    [int]$MaxTokens,
    [double]$Temperature,
    [switch]$AllowThink,
    [switch]$AsJson,
    [switch]$NoResolveLoadedIdentifier,
    [switch]$CompactScout,
    [switch]$NoCompactDefault,
    [switch]$NoRetryOnEmpty
)

$scriptPath = Join-Path $PSScriptRoot 'invoke_qwen_scout.ps1'
if (-not (Test-Path -LiteralPath $scriptPath)) {
    throw "Required script not found: $scriptPath"
}

$resolvedModel = $Model
if (-not $NoResolveLoadedIdentifier) {
    try {
        $models = Invoke-RestMethod -Method Get -Uri ($Endpoint -replace '/chat/completions$', '/models')
        $ids = @()
        if ($null -ne $models.data) {
            $ids = @($models.data | ForEach-Object { $_.id })
        }
        if ($ids -contains 'qwen27_58k') {
            $resolvedModel = 'qwen27_58k'
        }
        $exactLoaded = @($ids | Where-Object { $_ -like "${Model}:*" })
        if ($exactLoaded.Count -gt 0) {
            $resolvedModel = ($exactLoaded | Sort-Object {
                    $m = [regex]::Match($_, ':(\d+)$')
                    if ($m.Success) { [int]$m.Groups[1].Value } else { -1 }
                } -Descending | Select-Object -First 1)
        }
    }
    catch {
        # keep fallback to base model id
    }
}

if (-not $PSBoundParameters.ContainsKey('MaxTokens')) {
    $MaxTokens = 256
}

if ((-not $AllowThink) -and (-not $CompactScout) -and (-not $NoCompactDefault)) {
    $CompactScout = $true
}

$promptText =
    if ($PSCmdlet.ParameterSetName -eq 'File') {
        if (-not (Test-Path -LiteralPath $PromptFile)) {
            throw "Prompt file not found: $PromptFile"
        }
        Get-Content -LiteralPath $PromptFile -Raw
    } else {
        $Message
    }

if ($CompactScout) {
    $compactPrefix = @'
Return only final answer content.
No analysis text.
No reasoning trace.
Keep output concise and directly usable.
'@
    $promptText = "$compactPrefix`n$promptText"
}

function Invoke-Qwen27Attempt {
    param(
        [string]$AttemptPrompt,
        [int]$AttemptMaxTokens
    )

    $invokeParams = @{
        Endpoint = $Endpoint
        Model = $resolvedModel
        Message = $AttemptPrompt
        MaxTokens = $AttemptMaxTokens
        AsJson = $true
    }
    if ($PSBoundParameters.ContainsKey('Temperature')) { $invokeParams.Temperature = $Temperature }
    if ($AllowThink) { $invokeParams.AllowThink = $true }

    $raw = & $scriptPath @invokeParams
    return ($raw | ConvertFrom-Json)
}

function Get-QwenAssistantText {
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

$response = Invoke-Qwen27Attempt -AttemptPrompt $promptText -AttemptMaxTokens $MaxTokens
$content = Get-QwenAssistantText -Response $response
$isEmptyContent = [string]::IsNullOrWhiteSpace($content)

if ($isEmptyContent -and (-not $NoRetryOnEmpty)) {
    $retryPrompt = "Return final answer only. Do not output analysis or reasoning text.`n$promptText"
    $retryMaxCeiling = if ($CompactScout) { 768 } else { [int]::MaxValue }
    $retryMax = [Math]::Min($retryMaxCeiling, [Math]::Max(256, ($MaxTokens * 2)))
    $response = Invoke-Qwen27Attempt -AttemptPrompt $retryPrompt -AttemptMaxTokens $retryMax
    $content = Get-QwenAssistantText -Response $response
    if ([string]::IsNullOrWhiteSpace($content)) {
        $retryPrompt2 = "Output final answer only. No analysis. No preface. If list requested, output just list lines.`n$promptText"
        $retryMax2Ceiling = if ($CompactScout) { 1024 } else { [int]::MaxValue }
        $retryMax2 = [Math]::Min($retryMax2Ceiling, [Math]::Max(512, ($retryMax * 2)))
        $response = Invoke-Qwen27Attempt -AttemptPrompt $retryPrompt2 -AttemptMaxTokens $retryMax2
        $content = Get-QwenAssistantText -Response $response
    }
}

if ($AsJson) {
    $response | ConvertTo-Json -Depth 16
    return
}

if ([string]::IsNullOrWhiteSpace($content)) {
    Write-Output ""
    return
}

Write-Output ($content.Trim())

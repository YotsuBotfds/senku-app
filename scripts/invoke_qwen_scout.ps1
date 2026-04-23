#!/usr/bin/env pwsh

<#
.SYNOPSIS
Send a direct OpenAI-compatible chat completion request to a Qwen scout endpoint.
#>
[CmdletBinding(DefaultParameterSetName = 'Text')]
param(
    [Parameter(Mandatory = $true, Position = 0, ParameterSetName = 'Text')]
    [string]$Message,

    [Parameter(Mandatory = $true, Position = 0, ParameterSetName = 'File')]
    [string]$PromptFile,

    [string]$Endpoint = 'http://192.168.0.88:1234/v1/chat/completions',
    [string]$Model = 'qwen/qwen3.5-9b',
    [int]$MaxTokens,
    [double]$Temperature,
    [switch]$AllowThink,
    [switch]$AsJson,
    [switch]$CompactScout,
    [switch]$NoRetryOnEmpty
)

if (-not $PSBoundParameters.ContainsKey('MaxTokens')) {
    $MaxTokens = 256
}

$prompt =
    if ($PSCmdlet.ParameterSetName -eq 'File') {
        if (-not (Test-Path -LiteralPath $PromptFile)) {
            throw "Prompt file not found: $PromptFile"
        }
        Get-Content -LiteralPath $PromptFile -Raw
    } else {
        $Message
    }

$preparedPrompt = $prompt
if (-not $AllowThink) {
    $hasThinkControl =
        $preparedPrompt -match '(?m)^\s*/no_think\b' -or
        $preparedPrompt -match '(?m)^\s*/think\b'
    if (-not $hasThinkControl) {
        $preparedPrompt = "/no_think`n$preparedPrompt"
    }
}

if ($CompactScout) {
    $compactPrefix = @'
Return only final answer content.
No analysis text.
No reasoning trace.
Keep output concise and directly usable.
'@
    $preparedPrompt = "$compactPrefix`n$preparedPrompt"
}

function Invoke-QwenScoutAttempt {
    param(
        [string]$AttemptPrompt,
        [int]$AttemptMaxTokens
    )

    $body = @{
        model = $Model
        messages = @(@{ role = 'user'; content = $AttemptPrompt })
        max_tokens = $AttemptMaxTokens
    }

    if ($PSBoundParameters.ContainsKey('Temperature')) { $body.temperature = $Temperature }

    try {
        return (Invoke-RestMethod -Method Post -Uri $Endpoint -ContentType 'application/json' -Body ($body | ConvertTo-Json -Depth 8))
    }
    catch {
        throw "Request failed: $($_.Exception.Message)"
    }
}

$response = Invoke-QwenScoutAttempt -AttemptPrompt $preparedPrompt -AttemptMaxTokens $MaxTokens
$content = $response.choices[0].message.content
$isEmptyContent = [string]::IsNullOrWhiteSpace($content)

if ($isEmptyContent -and (-not $NoRetryOnEmpty)) {
    $retryPrompt = "Return final answer only. Do not output analysis or reasoning text.`n$preparedPrompt"
    $retryMax = [Math]::Max(256, ($MaxTokens * 2))
    $response = Invoke-QwenScoutAttempt -AttemptPrompt $retryPrompt -AttemptMaxTokens $retryMax
    $content = $response.choices[0].message.content
    if ([string]::IsNullOrWhiteSpace($content)) {
        $retryPrompt2 = "Output final answer only. No analysis. No preface. If list requested, output just list lines.`n$preparedPrompt"
        $retryMax2 = [Math]::Max(512, ($retryMax * 2))
        $response = Invoke-QwenScoutAttempt -AttemptPrompt $retryPrompt2 -AttemptMaxTokens $retryMax2
        $content = $response.choices[0].message.content
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

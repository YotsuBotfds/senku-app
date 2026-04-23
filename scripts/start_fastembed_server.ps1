[CmdletBinding()]
param(
    [string]$BindHost = "127.0.0.1",
    [int]$Port = 8801,
    [string]$ApiModelId = "nomic-ai/text-embedding-nomic-embed-text-v1.5",
    [string]$BackendModelName = "nomic-ai/nomic-embed-text-v1.5",
    [string]$CacheDir = "",
    [int]$Threads = 0,
    [string]$PythonPath = "$HOME\AppData\Local\Python\bin\python.exe"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$serverScript = Join-Path $repoRoot "scripts\fastembed_openai_server.py"

if (-not (Test-Path -LiteralPath $PythonPath)) {
    throw "Python executable not found at: $PythonPath"
}

if (-not (Test-Path -LiteralPath $serverScript)) {
    throw "FastEmbed host server script not found at $serverScript"
}

$args = @(
    $serverScript,
    "--host", $BindHost,
    "--port", $Port,
    "--api-model-id", $ApiModelId,
    "--backend-model-name", $BackendModelName
)

if (-not [string]::IsNullOrWhiteSpace($CacheDir)) {
    $args += @("--cache-dir", $CacheDir)
}

if ($Threads -gt 0) {
    $args += @("--threads", $Threads)
}

& $PythonPath @args

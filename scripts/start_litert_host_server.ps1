param(
    [string]$ModelPath = "",
    [string]$ModelId = "gemma-4-e2b-it-litert",
    [string]$BindHost = "127.0.0.1",
    [int]$Port = 1235,
    [string]$BackendOrder = "gpu,cpu",
    [string]$BinaryPath = "",
    [int]$TimeoutSeconds = 1200
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$serverScript = Join-Path $repoRoot "scripts\litert_native_openai_server.py"
$defaultBinDir = Join-Path $repoRoot "artifacts\litert_release_bin"
$defaultBinary = Join-Path $defaultBinDir "lit_windows_x86_64.exe"
$liteRtVersion = "v0.10.1"
$binaryUrl = "https://github.com/google-ai-edge/LiteRT-LM/releases/download/$liteRtVersion/lit_windows_x86_64.exe"

function Resolve-DefaultLiteRtModelPath {
    if (-not [string]::IsNullOrWhiteSpace($env:SENKU_LITERT_MODEL_PATH)) {
        return $env:SENKU_LITERT_MODEL_PATH
    }

    $userHome = [Environment]::GetFolderPath("UserProfile")
    $modelNames = @(
        "gemma-4-E4B-it.litertlm",
        "gemma-4-E4B-it.task",
        "gemma-4-E2B-it.litertlm",
        "gemma-4-E2B-it.task"
    )
    $candidates = foreach ($modelName in $modelNames) {
        @(
            (Join-Path $repoRoot $modelName),
            (Join-Path $repoRoot ("models\" + $modelName)),
            (Join-Path $userHome ("Downloads\" + $modelName))
        )
    }
    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    return ""
}

function Resolve-LiteRtModelId {
    param([string]$ResolvedModelPath)

    $normalized = [System.IO.Path]::GetFileName($ResolvedModelPath).ToLowerInvariant()
    if ($normalized.Contains("e4b")) {
        return "gemma-4-e4b-it-litert"
    }
    if ($normalized.Contains("e2b")) {
        return "gemma-4-e2b-it-litert"
    }
    return "gemma-4-e2b-it-litert"
}

function Copy-IfExists {
    param(
        [string]$SourcePath,
        [string]$DestinationDir
    )

    if (Test-Path -LiteralPath $SourcePath) {
        try {
            Copy-Item -LiteralPath $SourcePath -Destination $DestinationDir -Force
        } catch {
            Write-Warning "Skipping copy for ${SourcePath}: $($_.Exception.Message)"
        }
    }
}

function Ensure-LiteRtBinary {
    param(
        [string]$EffectiveBinaryPath
    )

    $binDir = Split-Path -Parent $EffectiveBinaryPath
    New-Item -ItemType Directory -Force -Path $binDir | Out-Null

    if (-not (Test-Path -LiteralPath $EffectiveBinaryPath)) {
        Write-Host "Downloading LiteRT binary to $EffectiveBinaryPath"
        Invoke-WebRequest -Headers @{ "User-Agent" = "Codex" } -Uri $binaryUrl -OutFile $EffectiveBinaryPath
    }

    $upstreamPrebuilt = "C:\Users\tateb\Documents\litert-lm-upstream\prebuilt\windows_x86_64"
    if (Test-Path -LiteralPath $upstreamPrebuilt) {
        Get-ChildItem -LiteralPath $upstreamPrebuilt -Filter *.dll | ForEach-Object {
            Copy-IfExists $_.FullName $binDir
        }
    }

    Copy-IfExists "C:\Program Files (x86)\Windows Kits\10\Redist\D3D\x64\dxcompiler.dll" $binDir
    Copy-IfExists "C:\Program Files (x86)\Windows Kits\10\Redist\D3D\x64\dxil.dll" $binDir
}

function Resolve-PythonCommand {
    if (-not [string]::IsNullOrWhiteSpace($env:SENKU_PYTHON_PATH)) {
        return @{
            Command = $env:SENKU_PYTHON_PATH
            Args    = @()
        }
    }

    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($python) {
        return @{
            Command = $python.Source
            Args    = @()
        }
    }

    $pyLauncher = Get-Command py -ErrorAction SilentlyContinue
    if ($pyLauncher) {
        return @{
            Command = $pyLauncher.Source
            Args    = @("-3")
        }
    }

    $userHome = [Environment]::GetFolderPath("UserProfile")
    $candidates = @(
        (Join-Path $repoRoot ".venv\Scripts\python.exe"),
        (Join-Path $repoRoot "venv\Scripts\python.exe"),
        (Join-Path $userHome "AppData\Local\Python\pythoncore-3.14-64\python.exe"),
        (Join-Path $userHome "AppData\Local\Python\bin\python.exe")
    )

    foreach ($candidate in $candidates) {
        try {
            if (Test-Path -LiteralPath $candidate) {
                return @{
                    Command = $candidate
                    Args    = @()
                }
            }
        } catch {
            continue
        }
    }

    throw "Python launcher not found. Set SENKU_PYTHON_PATH or install python/py on PATH."
}

if (-not (Test-Path -LiteralPath $serverScript)) {
    throw "Native LiteRT host server script not found at $serverScript"
}

if ([string]::IsNullOrWhiteSpace($ModelPath)) {
    $ModelPath = Resolve-DefaultLiteRtModelPath
}

$effectiveBinaryPath = if ([string]::IsNullOrWhiteSpace($BinaryPath)) { $defaultBinary } else { $BinaryPath }
Ensure-LiteRtBinary -EffectiveBinaryPath $effectiveBinaryPath

if (-not (Test-Path -LiteralPath $ModelPath)) {
    throw "LiteRT model not found at $ModelPath"
}

$resolvedModelId = if ([string]::IsNullOrWhiteSpace($ModelId) -or $ModelId -eq "gemma-4-e2b-it-litert") {
    Resolve-LiteRtModelId -ResolvedModelPath $ModelPath
} else {
    $ModelId
}

$pythonResolution = Resolve-PythonCommand
$pythonCommand = $pythonResolution.Command
$pythonArgs = @($pythonResolution.Args)

$pythonArgs += @(
    $serverScript,
    "--host", $BindHost,
    "--port", $Port,
    "--binary-path", $effectiveBinaryPath,
    "--model-path", $ModelPath,
    "--model-id", $resolvedModelId,
    "--backend-order", $BackendOrder,
    "--timeout-seconds", $TimeoutSeconds
)

Write-Host "LiteRT model: $ModelPath"
Write-Host "LiteRT model id: $resolvedModelId"

& $pythonCommand @pythonArgs

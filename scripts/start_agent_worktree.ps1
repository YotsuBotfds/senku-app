param(
    [Parameter(Mandatory = $true)]
    [string]$Lane,

    [string]$BaseRef = "HEAD",
    [string]$Branch,
    [string]$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [string]$WorkerRoot,
    [string]$LeaseDir,
    [string]$Owner = "",
    [string]$Task = "",
    [switch]$Force
)

$ErrorActionPreference = "Stop"

function Invoke-Git {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments,
        [Parameter(Mandatory = $true)]
        [string]$WorkingDirectory
    )
    $output = & git -C $WorkingDirectory @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed in $WorkingDirectory`: $output"
    }
    return $output
}

function Convert-ToSafeName {
    param([Parameter(Mandatory = $true)][string]$Value)
    $safe = $Value.Trim().ToLowerInvariant() -replace "[^a-z0-9._-]+", "-"
    $safe = $safe.Trim(".-")
    if (-not $safe) {
        throw "Lane must contain at least one letter or number."
    }
    return $safe
}

$repo = (Resolve-Path $RepoRoot).Path
$safeLane = Convert-ToSafeName $Lane
if (-not $Branch) {
    $Branch = "worker/$safeLane"
}
Invoke-Git -Arguments @("check-ref-format", "--branch", $Branch) -WorkingDirectory $repo | Out-Null
if (-not $WorkerRoot) {
    $repoParent = Split-Path -Parent $repo
    $repoName = Split-Path -Leaf $repo
    $WorkerRoot = Join-Path $repoParent "$repoName`_worktrees"
}
if (-not $LeaseDir) {
    $LeaseDir = Join-Path $repo "artifacts\runs\worker_lanes"
}

$workerRootFull = [System.IO.Path]::GetFullPath($WorkerRoot)
$worktreePath = [System.IO.Path]::GetFullPath((Join-Path $workerRootFull $safeLane))
$leaseDirFull = [System.IO.Path]::GetFullPath($LeaseDir)
$expectedPrefix = $workerRootFull.TrimEnd("\", "/") + [System.IO.Path]::DirectorySeparatorChar
if (-not $worktreePath.StartsWith($expectedPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Resolved worktree path escaped worker root: $worktreePath"
}

$baseSha = (Invoke-Git -Arguments @("rev-parse", $BaseRef) -WorkingDirectory $repo | Select-Object -First 1).Trim()
New-Item -ItemType Directory -Force -Path $workerRootFull | Out-Null
New-Item -ItemType Directory -Force -Path $leaseDirFull | Out-Null

if ((Test-Path -LiteralPath $worktreePath) -and -not $Force) {
    throw "Worktree path already exists: $worktreePath. Use -Force only after confirming it is the intended lane."
}

$branchExists = $false
& git -C $repo show-ref --verify --quiet "refs/heads/$Branch"
if ($LASTEXITCODE -eq 0) {
    $branchExists = $true
}
elseif ($LASTEXITCODE -ne 1) {
    throw "Unable to check branch refs/heads/$Branch"
}

if ($branchExists) {
    Invoke-Git -Arguments @("worktree", "add", $worktreePath, $Branch) -WorkingDirectory $repo | Out-Null
}
else {
    Invoke-Git -Arguments @("worktree", "add", "-b", $Branch, $worktreePath, $BaseRef) -WorkingDirectory $repo | Out-Null
}

$lease = [ordered]@{
    lane = $safeLane
    requested_lane = $Lane
    branch = $Branch
    base_ref = $BaseRef
    base_sha = $baseSha
    repo_root = $repo
    worktree_path = $worktreePath
    owner = $Owner
    task = $Task
    created_at = (Get-Date).ToUniversalTime().ToString("o")
}
$leasePath = Join-Path $leaseDirFull "$safeLane.json"
$lease | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $leasePath -Encoding UTF8

[ordered]@{
    lane = $safeLane
    branch = $Branch
    worktree_path = $worktreePath
    lease_path = $leasePath
    base_sha = $baseSha
} | ConvertTo-Json -Depth 4

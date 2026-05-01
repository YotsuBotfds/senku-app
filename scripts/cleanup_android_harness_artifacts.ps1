param(
    [string[]]$Targets = @(
        "artifacts/instrumented_ui_smoke",
        "artifacts/ui_validation",
        "artifacts/live_debug"
    ),
    [int]$KeepDays = 7,
    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$defaultTargetRoots = @(
    "artifacts/instrumented_ui_smoke",
    "artifacts/ui_validation",
    "artifacts/live_debug"
)

function Resolve-CleanupArtifactPath {
    param(
        [Parameter(Mandatory)]
        [string]$Path
    )

    $candidate = if ([System.IO.Path]::IsPathRooted($Path)) { $Path } else { Join-Path $repoRoot $Path }
    return [System.IO.Path]::GetFullPath($candidate)
}

function Test-IsUnderCleanupRoot {
    param(
        [Parameter(Mandatory)]
        [string]$Path,

        [Parameter(Mandatory)]
        [string[]]$Roots
    )

    $fullPath = [System.IO.Path]::GetFullPath($Path).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar
    )
    foreach ($root in $Roots) {
        $fullRoot = [System.IO.Path]::GetFullPath($root).TrimEnd(
            [System.IO.Path]::DirectorySeparatorChar,
            [System.IO.Path]::AltDirectorySeparatorChar
        )
        if ($fullPath.Equals($fullRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
        $rootWithSeparator = $fullRoot + [System.IO.Path]::DirectorySeparatorChar
        if ($fullPath.StartsWith($rootWithSeparator, [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }
    return $false
}

$allowedCleanupRoots = @($defaultTargetRoots | ForEach-Object { Resolve-CleanupArtifactPath -Path $_ })
$cutoffUtc = (Get-Date).ToUniversalTime().AddDays(-1 * [Math]::Abs($KeepDays))
$removed = New-Object System.Collections.Generic.List[string]

foreach ($target in $Targets) {
    $resolvedTarget = Resolve-CleanupArtifactPath -Path $target
    if (-not (Test-IsUnderCleanupRoot -Path $resolvedTarget -Roots $allowedCleanupRoots)) {
        throw "Cleanup target is outside allowed Android harness artifact roots: $resolvedTarget"
    }
    if (-not (Test-Path -LiteralPath $resolvedTarget)) {
        continue
    }

    foreach ($item in Get-ChildItem -LiteralPath $resolvedTarget -Force -ErrorAction SilentlyContinue) {
        $lastWriteUtc = $item.LastWriteTimeUtc
        if ($lastWriteUtc -ge $cutoffUtc) {
            continue
        }

        if ($WhatIf) {
            Write-Host "[whatif] Would remove $($item.FullName)"
            continue
        }

        $resolvedItem = Resolve-CleanupArtifactPath -Path $item.FullName
        if (-not (Test-IsUnderCleanupRoot -Path $resolvedItem -Roots @($resolvedTarget))) {
            throw "Cleanup item is outside selected Android harness artifact target: $resolvedItem"
        }
        if (-not (Test-IsUnderCleanupRoot -Path $resolvedItem -Roots $allowedCleanupRoots)) {
            throw "Cleanup item is outside allowed Android harness artifact roots: $resolvedItem"
        }
        Remove-Item -LiteralPath $resolvedItem -Recurse -Force -ErrorAction Stop
        $removed.Add($resolvedItem)
    }
}

[pscustomobject]@{
    keep_days = $KeepDays
    cutoff_utc = $cutoffUtc.ToString("o")
    removed_count = $removed.Count
    removed = $removed
} | ConvertTo-Json -Depth 5

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
$cutoffUtc = (Get-Date).ToUniversalTime().AddDays(-1 * [Math]::Abs($KeepDays))
$removed = New-Object System.Collections.Generic.List[string]

foreach ($target in $Targets) {
    $resolvedTarget = if ([System.IO.Path]::IsPathRooted($target)) { $target } else { Join-Path $repoRoot $target }
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

        Remove-Item -LiteralPath $item.FullName -Recurse -Force -ErrorAction Stop
        $removed.Add($item.FullName)
    }
}

[pscustomobject]@{
    keep_days = $KeepDays
    cutoff_utc = $cutoffUtc.ToString("o")
    removed_count = $removed.Count
    removed = $removed
} | ConvertTo-Json -Depth 5

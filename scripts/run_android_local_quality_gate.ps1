[CmdletBinding()]
param(
    [string[]]$Tests = @(),

    [switch]$SkipDiffCheck,

    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

function Quote-GateArgument {
    param(
        [Parameter(Mandatory)]
        [string]$Value
    )

    if ($Value -match '^[A-Za-z0-9_./:\\-]+$') {
        return $Value
    }

    return "'" + ($Value -replace "'", "''") + "'"
}

function Format-GateCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Executable,

        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    $parts = @('&', (Quote-GateArgument $Executable))
    $parts += $Arguments | ForEach-Object { Quote-GateArgument $_ }
    return ($parts -join ' ')
}

function Invoke-GateCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Name,

        [Parameter(Mandatory)]
        [string]$Executable,

        [Parameter(Mandatory)]
        [string[]]$Arguments,

        [Parameter(Mandatory)]
        [string]$WorkingDirectory
    )

    $display = Format-GateCommand -Executable $Executable -Arguments $Arguments
    Write-Host ""
    Write-Host "==> $Name"
    Write-Host "    $display"

    if ($WhatIf) {
        return
    }

    Push-Location $WorkingDirectory
    try {
        & $Executable @Arguments
        if ($LASTEXITCODE -ne 0) {
            throw "$Name failed with exit code $LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$androidRoot = Join-Path $repoRoot 'android-app'
$gradleWrapper = Join-Path $androidRoot 'gradlew.bat'

if (-not (Test-Path $gradleWrapper)) {
    throw "Missing Android Gradle wrapper: $gradleWrapper"
}

$defaultTests = @(
    'com.senku.ui.answer.AnswerContentFactoryTest',
    'com.senku.mobile.DetailActionBlockPresentationFormatterTest',
    'com.senku.mobile.DetailSurfaceContractTest',
    'com.senku.mobile.DetailWarningCopySanitizerTest',
    'com.senku.mobile.EmergencySurfacePolicyTest',
    'com.senku.mobile.MainActivityPhoneNavigationTest',
    'com.senku.mobile.SearchResultAdapterTest',
    'com.senku.ui.evidence.PureEvidenceModelsTest',
    'com.senku.ui.search.SearchResultCardHeuristicsTest',
    'com.senku.ui.sources.SourceRowModelTest',
    'com.senku.ui.tablet.StressReadingPolicyTest',
    'com.senku.ui.tablet.TabletEvidenceVisibilityPolicyTest',
    'com.senku.ui.theme.ContrastAuditTest'
)

$extraTests = @($Tests) |
    ForEach-Object { ([string]$_).Split(',') } |
    ForEach-Object { $_.Trim() } |
    Where-Object { $_ }
$allTests = @($defaultTests + $extraTests) | Where-Object { $_ } | Select-Object -Unique

Write-Host "Android local quality gate"
Write-Host "Repo: $repoRoot"
Write-Host "Focused JVM filters:"
$allTests | ForEach-Object { Write-Host "  --tests $_" }

if (-not $SkipDiffCheck) {
    Invoke-GateCommand `
        -Name 'git diff whitespace check' `
        -Executable 'git' `
        -Arguments @('diff', '--check') `
        -WorkingDirectory $repoRoot
}

$gradleArgs = @('--no-daemon', ':app:testDebugUnitTest')
foreach ($test in $allTests) {
    $gradleArgs += @('--tests', $test)
}

Invoke-GateCommand `
    -Name 'focused Android JVM tests' `
    -Executable $gradleWrapper `
    -Arguments $gradleArgs `
    -WorkingDirectory $androidRoot

Write-Host ""
Write-Host "Android local quality gate passed."

[CmdletBinding()]
param(
    [string[]]$Path = @('scripts', 'tests\powershell'),

    [string[]]$Exclude = @(),

    [switch]$SkipParser,

    [switch]$SkipAnalyzer,

    [switch]$SkipPester,

    [switch]$RequireAnalyzer,

    [switch]$RequirePester,

    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

if ($RequireAnalyzer -and $SkipAnalyzer) {
    throw "-RequireAnalyzer cannot be combined with -SkipAnalyzer."
}
if ($RequirePester -and $SkipPester) {
    throw "-RequirePester cannot be combined with -SkipPester."
}

function Get-RepoRoot {
    $root = (& git rev-parse --show-toplevel 2>$null)
    if ($LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace($root)) {
        return $root.Trim()
    }
    return (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
}

function ConvertTo-RelativePath {
    param(
        [Parameter(Mandatory)]
        [string]$Root,

        [Parameter(Mandatory)]
        [string]$FullName
    )

    $rootPath = [System.IO.Path]::GetFullPath($Root).TrimEnd([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar)
    $fullPath = [System.IO.Path]::GetFullPath($FullName)
    if ($fullPath.StartsWith($rootPath, [System.StringComparison]::OrdinalIgnoreCase)) {
        return $fullPath.Substring($rootPath.Length).TrimStart('\', '/')
    }
    return $fullPath
}

function Test-IsExcluded {
    param(
        [Parameter(Mandatory)]
        [string]$RelativePath,

        [string[]]$Patterns
    )

    $normalized = $RelativePath.Replace('\', '/')
    foreach ($pattern in $Patterns) {
        if ([string]::IsNullOrWhiteSpace($pattern)) {
            continue
        }
        $normalizedPattern = $pattern.Replace('\', '/')
        if ($normalized -like $normalizedPattern -or $normalized.StartsWith($normalizedPattern.TrimEnd('/'), [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }
    return $false
}

function Get-PowerShellFiles {
    param(
        [Parameter(Mandatory)]
        [string]$RepoRoot,

        [Parameter(Mandatory)]
        [string[]]$InputPaths,

        [string[]]$ExcludePatterns
    )

    $files = New-Object System.Collections.Generic.List[object]
    $seen = @{}
    $expandedInputPaths = @(
        foreach ($rawItem in $InputPaths) {
            foreach ($item in ([string]$rawItem).Split(',', [System.StringSplitOptions]::RemoveEmptyEntries)) {
                $item.Trim()
            }
        }
    )
    foreach ($item in $expandedInputPaths) {
        if ([string]::IsNullOrWhiteSpace($item)) {
            throw "-Path entries must not be empty."
        }
        $candidate = if ([System.IO.Path]::IsPathRooted($item)) { $item } else { Join-Path $RepoRoot $item }
        if (-not (Test-Path -LiteralPath $candidate)) {
            throw "PowerShell quality path not found: $item"
        }
        $resolved = Resolve-Path -LiteralPath $candidate
        $pathItem = Get-Item -LiteralPath $resolved.Path
        if ($pathItem.PSIsContainer) {
            $matches = Get-ChildItem -LiteralPath $pathItem.FullName -Recurse -File -Include *.ps1, *.psm1, *.psd1
        } else {
            $matches = @($pathItem)
        }
        foreach ($match in $matches) {
            if ($match.Extension -notin @('.ps1', '.psm1', '.psd1')) {
                continue
            }
            $relative = ConvertTo-RelativePath -Root $RepoRoot -FullName $match.FullName
            if (Test-IsExcluded -RelativePath $relative -Patterns $ExcludePatterns) {
                continue
            }
            if (-not $seen.ContainsKey($match.FullName)) {
                $seen[$match.FullName] = $true
                $files.Add([pscustomobject]@{
                    FullName = $match.FullName
                    RelativePath = $relative
                })
            }
        }
    }
    return @($files | Sort-Object RelativePath)
}

function Invoke-ParserGate {
    param(
        [Parameter(Mandatory)]
        [object[]]$Files
    )

    $failures = New-Object System.Collections.Generic.List[string]
    foreach ($file in $Files) {
        $tokens = $null
        $errors = $null
        [System.Management.Automation.Language.Parser]::ParseFile($file.FullName, [ref]$tokens, [ref]$errors) | Out-Null
        if ($errors -and $errors.Count -gt 0) {
            foreach ($errorRecord in $errors) {
                $failures.Add(("{0}: {1}" -f $file.RelativePath, $errorRecord.Message))
            }
        }
    }
    if ($failures.Count -gt 0) {
        $failures | ForEach-Object { Write-Error $_ }
        throw "PowerShell parser gate failed for $($failures.Count) error(s)."
    }
    Write-Host ("Parser gate passed for {0} PowerShell file(s)." -f $Files.Count)
}

function Invoke-AnalyzerGate {
    param(
        [Parameter(Mandatory)]
        [string]$RepoRoot,

        [Parameter(Mandatory)]
        [object[]]$Files,

        [switch]$Require
    )

    $module = Get-Module -ListAvailable -Name PSScriptAnalyzer | Sort-Object Version -Descending | Select-Object -First 1
    if ($null -eq $module) {
        $message = "PSScriptAnalyzer is not installed; skipping analyzer gate."
        if ($Require) {
            throw $message
        }
        Write-Warning $message
        return
    }

    Import-Module PSScriptAnalyzer -Force
    $settingsPath = Join-Path $RepoRoot 'PSScriptAnalyzerSettings.psd1'
    $findings = @()
    foreach ($file in $Files) {
        $args = @{
            Path = $file.FullName
            Recurse = $false
        }
        if (Test-Path -LiteralPath $settingsPath) {
            $args.Settings = $settingsPath
        }
        $findings += Invoke-ScriptAnalyzer @args
    }
    $blockingFindings = @($findings | Where-Object { $_.Severity -eq 'Error' })
    if ($blockingFindings.Count -gt 0) {
        $blockingFindings | Format-Table -AutoSize | Out-String | Write-Error
        throw "PSScriptAnalyzer gate failed for $($blockingFindings.Count) error finding(s)."
    }
    if ($findings.Count -gt 0) {
        $findings | Format-Table -AutoSize | Out-String | Write-Warning
        Write-Host ("PSScriptAnalyzer reported {0} non-blocking warning/information finding(s)." -f $findings.Count)
        return
    }
    Write-Host ("PSScriptAnalyzer gate passed for {0} PowerShell file(s)." -f $Files.Count)
}

function Invoke-PesterGate {
    param(
        [Parameter(Mandatory)]
        [string]$RepoRoot,

        [switch]$Require
    )

    $module = Get-Module -ListAvailable -Name Pester | Sort-Object Version -Descending | Select-Object -First 1
    if ($null -eq $module) {
        $message = "Pester is not installed; skipping Pester gate."
        if ($Require) {
            throw $message
        }
        Write-Warning $message
        return
    }

    $testFiles = @(Get-ChildItem -LiteralPath (Join-Path $RepoRoot 'tests\powershell') -File -Filter '*.Tests.ps1' -ErrorAction SilentlyContinue)
    if ($testFiles.Count -eq 0) {
        Write-Warning "No PowerShell Pester tests found under tests\powershell."
        return
    }

    Import-Module Pester -Force
    $failedCount = 0
    foreach ($testFile in $testFiles) {
        $result = Invoke-Pester -Script $testFile.FullName -PassThru
        if ($null -ne $result -and $result.PSObject.Properties.Name -contains 'FailedCount') {
            $failedCount += [int]$result.FailedCount
        } elseif ($LASTEXITCODE -ne 0) {
            $failedCount += 1
        }
    }
    if ($failedCount -gt 0) {
        throw "Pester gate failed with $failedCount failed test(s)."
    }
    Write-Host ("Pester gate passed for {0} test file(s)." -f $testFiles.Count)
}

$repoRoot = Get-RepoRoot
$files = Get-PowerShellFiles -RepoRoot $repoRoot -InputPaths $Path -ExcludePatterns $Exclude
if ($files.Count -eq 0) {
    throw "No PowerShell files selected."
}

Push-Location $repoRoot
try {
    if ($WhatIf) {
        Write-Host "PowerShell quality gate dry run."
        Write-Host ("Selected files: {0}" -f $files.Count)
        $files | ForEach-Object { Write-Host ("- {0}" -f $_.RelativePath) }
        return
    }

    if (-not $SkipParser) {
        Invoke-ParserGate -Files $files
    }
    if (-not $SkipAnalyzer) {
        Invoke-AnalyzerGate -RepoRoot $repoRoot -Files $files -Require:$RequireAnalyzer
    }
    if (-not $SkipPester) {
        Invoke-PesterGate -RepoRoot $repoRoot -Require:$RequirePester
    }
} finally {
    Pop-Location
}

param(
    [string[]]$Devices,
    [string]$PackageName = "com.senku.mobile",
    [string]$LaunchActivity = "com.senku.mobile/.MainActivity",
    [string]$LaunchQuery = "water filter charcoal sand",
    [string]$HostSqlitePath,
    [string]$OutputRoot,
    [int]$LaunchWaitSeconds = 20
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-RepoRoot {
    return [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot ".."))
}

function Invoke-CapturedCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath,
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $output = & $FilePath @Arguments 2>&1
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousPreference
    }
    $text = ($output | Out-String)
    if ($null -eq $text) {
        $text = ""
    }
    $text = $text.TrimEnd()

    return [pscustomobject]@{
        command = (($Arguments | ForEach-Object {
                if ($_ -match "\s") {
                    '"' + $_ + '"'
                } else {
                    $_
                }
            }) -join " ")
        exitCode = $exitCode
        output = $text
    }
}

function New-SqliteProbeResult {
    param(
        [Parameter(Mandatory = $true)]
        [pscustomobject]$CommandResult,
        [Parameter(Mandatory = $true)]
        [string]$Sql,
        [Parameter(Mandatory = $true)]
        [string]$Database,
        [Parameter(Mandatory = $true)]
        [string]$Mode
    )

    $text = $CommandResult.output
    $failed = $CommandResult.exitCode -ne 0 -or
        $text -match "(?im)^error:" -or
        $text -match "no such module" -or
        $text -match "unable to open database" -or
        $text -match "run-as:"

    return [pscustomobject]@{
        mode = $Mode
        database = $Database
        sql = $Sql
        success = -not $failed
        exitCode = $CommandResult.exitCode
        output = $text
        command = $CommandResult.command
    }
}

function Get-AdbPath {
    $adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
    if (-not (Test-Path $adb)) {
        throw "adb not found at $adb"
    }
    return $adb
}

function Get-PythonPath {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($python) {
        return $python.Source
    }
    $py = Get-Command py -ErrorAction SilentlyContinue
    if ($py) {
        return $py.Source
    }
    throw "python or py launcher not found on PATH"
}

function Get-ConnectedDevices {
    param([string]$AdbPath)

    $result = Invoke-CapturedCommand -FilePath $AdbPath -Arguments @("devices")
    $devices = @()
    foreach ($line in ($result.output -split "\r?\n")) {
        if ($line -match "^(?<serial>\S+)\s+device$") {
            $devices += $Matches.serial
        }
    }
    return $devices
}

function Normalize-DeviceArguments {
    param([string[]]$Serials)

    $normalized = New-Object System.Collections.Generic.List[string]
    foreach ($serial in @($Serials)) {
        foreach ($piece in ([string]$serial -split ",")) {
            $trimmed = $piece.Trim()
            if (-not [string]::IsNullOrWhiteSpace($trimmed)) {
                $normalized.Add($trimmed)
            }
        }
    }
    return $normalized.ToArray()
}

function Order-Devices {
    param([string[]]$Serials)

    $preferred = @(
        "emulator-5556",
        "emulator-5560",
        "emulator-5554",
        "emulator-5558"
    )

    $ordered = New-Object System.Collections.Generic.List[string]
    foreach ($serial in $preferred) {
        if ($Serials -contains $serial) {
            $ordered.Add($serial)
        }
    }
    foreach ($serial in ($Serials | Sort-Object)) {
        if (-not $ordered.Contains($serial)) {
            $ordered.Add($serial)
        }
    }
    return $ordered.ToArray()
}

function Get-DeviceProperty {
    param(
        [string]$AdbPath,
        [string]$Device,
        [string]$PropertyName
    )

    $result = Invoke-CapturedCommand -FilePath $AdbPath -Arguments @("-s", $Device, "shell", "getprop", $PropertyName)
    return $result.output.Trim()
}

function Invoke-DeviceSqlite {
    param(
        [string]$AdbPath,
        [string]$Device,
        [string]$Database,
        [string]$Sql,
        [switch]$UseRunAs,
        [string]$PackageName
    )

    $arguments = @("-s", $Device, "exec-out")
    if ($UseRunAs) {
        $arguments += @("run-as", $PackageName, "sqlite3", $Database, $Sql)
        $mode = "run-as"
    } else {
        $arguments += @("sqlite3", $Database, $Sql)
        $mode = "shell"
    }

    $result = Invoke-CapturedCommand -FilePath $AdbPath -Arguments $arguments
    return New-SqliteProbeResult -CommandResult $result -Sql $Sql -Database $Database -Mode $mode
}

function Invoke-DeviceCommand {
    param(
        [string]$AdbPath,
        [string]$Device,
        [string[]]$Arguments
    )

    return Invoke-CapturedCommand -FilePath $AdbPath -Arguments (@("-s", $Device) + $Arguments)
}

function Get-HostPackProbe {
    param(
        [string]$PythonPath,
        [string]$SqlitePath
    )

    $script = @'
import json
import pathlib
import sqlite3
import sys

path = pathlib.Path(sys.argv[1]).resolve()
summary = {
    "path": str(path),
    "exists": path.exists(),
}

if path.exists():
    try:
        conn = sqlite3.connect(str(path))
        try:
            cur = conn.cursor()
            summary["sqlite_version"] = cur.execute("select sqlite_version()").fetchone()[0]
            summary["tables"] = [
                {"name": row[0], "sql": row[1]}
                for row in cur.execute(
                    "select name, sql from sqlite_master where type='table' and name in (?, ?) order by name",
                    ("lexical_chunks_fts", "lexical_chunks_fts4"),
                ).fetchall()
            ]
            summary["row_counts"] = {}
            for table_name in ("lexical_chunks_fts", "lexical_chunks_fts4"):
                try:
                    summary["row_counts"][table_name] = cur.execute(
                        f"select count(*) from {table_name}"
                    ).fetchone()[0]
                except Exception as exc:
                    summary["row_counts"][table_name] = {"error": str(exc)}
        finally:
            conn.close()
    except Exception as exc:
        summary["error"] = str(exc)

print(json.dumps(summary))
'@

    $raw = $script | & $PythonPath - $SqlitePath
    $text = ($raw | Out-String)
    if ($null -eq $text) {
        $text = ""
    }
    $text = $text.Trim()
    if ([string]::IsNullOrWhiteSpace($text)) {
        throw "Host pack probe returned no JSON for $SqlitePath"
    }
    return $text | ConvertFrom-Json
}

function Get-OutputDirectory {
    param(
        [string]$RepoRoot,
        [string]$RequestedOutputRoot
    )

    if ([string]::IsNullOrWhiteSpace($RequestedOutputRoot)) {
        $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
        return Join-Path $RepoRoot "artifacts\android_fts5_probe\$timestamp"
    }
    return [System.IO.Path]::GetFullPath($RequestedOutputRoot)
}

function Save-Utf8Text {
    param(
        [string]$Path,
        [string]$Content
    )

    $parent = Split-Path -Parent $Path
    if ($parent) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    $text = $Content
    if ($null -eq $text) {
        $text = ""
    }
    $encoding = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $text, $encoding)
}

function Get-AppRuntimeProbe {
    param(
        [string]$AdbPath,
        [string]$Device,
        [string]$PackageName,
        [string]$LaunchActivity,
        [string]$LaunchQuery,
        [int]$LaunchWaitSeconds,
        [string]$LogcatPath
    )

    $queryNeedle = 'query="' + $LaunchQuery + '"'
    $ftsPattern = "fts\.available|fts\.unavailable"
    $searchPattern = [regex]::Escape('search.start query="' + $LaunchQuery + '"')
    $topPattern = [regex]::Escape('search.topResults query="' + $LaunchQuery + '"')

    Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @("logcat", "-c") | Out-Null
    Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @("shell", "am", "force-stop", $PackageName) | Out-Null

    $encodedQuery = [System.Uri]::EscapeDataString($LaunchQuery)
    $launch = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @(
        "shell", "am", "start", "-n", $LaunchActivity, "--es", "auto_query", $encodedQuery
    )

    $deadline = (Get-Date).AddSeconds($LaunchWaitSeconds)
    $latestLog = ""
    do {
        Start-Sleep -Seconds 2
        $logResult = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @(
            "logcat", "-d", "SenkuPackRepo:D", "SenkuMobile:D", "*:S"
        )
        $latestLog = $logResult.output

        $sawFts = $latestLog -match $ftsPattern
        $sawSearch = $latestLog -match $searchPattern
        $sawTop = $latestLog -match $topPattern
    } while ((Get-Date) -lt $deadline -and -not ($sawFts -and ($sawSearch -or $sawTop)))

    Save-Utf8Text -Path $LogcatPath -Content $latestLog

    $ftsLines = @()
    $searchLines = @()
    $topLines = @()
    foreach ($line in ($latestLog -split "\r?\n")) {
        if ($line -match $ftsPattern) {
            $ftsLines += $line.Trim()
        }
        if ($line -match $searchPattern) {
            $searchLines += $line.Trim()
        }
        if ($line -match $topPattern) {
            $topLines += $line.Trim()
        }
    }

    return [pscustomobject]@{
        launch = $launch
        query = $LaunchQuery
        logcatPath = $LogcatPath
        ftsLines = $ftsLines
        searchStartLines = $searchLines
        searchTopResultLines = $topLines
    }
}

function Get-DeviceProbe {
    param(
        [string]$AdbPath,
        [string]$Device,
        [string]$PackageName,
        [string]$LaunchActivity,
        [string]$LaunchQuery,
        [int]$LaunchWaitSeconds,
        [string]$OutputDirectory
    )

    Write-Host ("Probing {0}" -f $Device)

    $deviceDir = Join-Path $OutputDirectory $Device
    New-Item -ItemType Directory -Force -Path $deviceDir | Out-Null

    $appPathResult = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @("shell", "pm", "path", $PackageName)
    $appInstalled = $appPathResult.output -match "^package:"

    $runAsPwd = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @("shell", "run-as", $PackageName, "pwd")
    $runAsOk = $runAsPwd.exitCode -eq 0 -and -not [string]::IsNullOrWhiteSpace($runAsPwd.output) -and $runAsPwd.output -notmatch "run-as:"

    $sqliteVersion = Invoke-DeviceSqlite -AdbPath $AdbPath -Device $Device -Database ":memory:" -Sql "SELECT sqlite_version();" -PackageName $PackageName
    $moduleList = Invoke-DeviceSqlite -AdbPath $AdbPath -Device $Device -Database ":memory:" -Sql "PRAGMA module_list;" -PackageName $PackageName
    $fts5Create = Invoke-DeviceSqlite -AdbPath $AdbPath -Device $Device -Database ":memory:" -Sql "CREATE VIRTUAL TABLE fts5_probe USING fts5(body);" -PackageName $PackageName
    $fts4Create = Invoke-DeviceSqlite -AdbPath $AdbPath -Device $Device -Database ":memory:" -Sql "CREATE VIRTUAL TABLE fts4_probe USING fts4(body);" -PackageName $PackageName

    $packFiles = $null
    $packManifestText = $null
    $packManifest = $null
    $fts5Schema = $null
    $fts4Schema = $null
    $fts5Match = $null
    $fts4Match = $null
    $appRuntime = $null

    if ($appInstalled -and $runAsOk) {
        $packFiles = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @(
            "exec-out", "run-as", $PackageName, "ls", "files/mobile_pack"
        )
        $packManifestText = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @(
            "exec-out", "run-as", $PackageName, "cat", "files/mobile_pack/senku_manifest.json"
        )
        if ($packManifestText.exitCode -eq 0 -and $packManifestText.output.Trim().StartsWith("{")) {
            try {
                $packManifest = $packManifestText.output | ConvertFrom-Json
            } catch {
                $packManifest = $null
            }
        }
        $fts5Schema = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @(
            "exec-out", "run-as", $PackageName, "sqlite3", "files/mobile_pack/senku_mobile.sqlite3", ".schema lexical_chunks_fts"
        )
        $fts4Schema = Invoke-DeviceCommand -AdbPath $AdbPath -Device $Device -Arguments @(
            "exec-out", "run-as", $PackageName, "sqlite3", "files/mobile_pack/senku_mobile.sqlite3", ".schema lexical_chunks_fts4"
        )
        $fts5Match = Invoke-DeviceSqlite -AdbPath $AdbPath -Device $Device -Database "files/mobile_pack/senku_mobile.sqlite3" -Sql "SELECT count(*) FROM lexical_chunks_fts WHERE lexical_chunks_fts MATCH 'water';" -UseRunAs -PackageName $PackageName
        $fts4Match = Invoke-DeviceSqlite -AdbPath $AdbPath -Device $Device -Database "files/mobile_pack/senku_mobile.sqlite3" -Sql "SELECT count(*) FROM lexical_chunks_fts4 WHERE lexical_chunks_fts4 MATCH 'water';" -UseRunAs -PackageName $PackageName
        $appRuntime = Get-AppRuntimeProbe -AdbPath $AdbPath -Device $Device -PackageName $PackageName -LaunchActivity $LaunchActivity -LaunchQuery $LaunchQuery -LaunchWaitSeconds $LaunchWaitSeconds -LogcatPath (Join-Path $deviceDir "logcat.txt")
    }

    $moduleNames = @()
    if ($moduleList -and $moduleList.output) {
        $moduleNames = @(
            $moduleList.output -split "\r?\n" |
                ForEach-Object { $_.Trim() } |
                Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        )
    }

    $runtimeLogState = "missing"
    if ($appRuntime -and $appRuntime.ftsLines.Count -gt 0) {
        if ($appRuntime.ftsLines -match "fts\.available table=lexical_chunks_fts ") {
            $runtimeLogState = "fts5"
        } elseif ($appRuntime.ftsLines -match "fts\.available table=lexical_chunks_fts4 ") {
            $runtimeLogState = "fts4"
        } elseif ($appRuntime.ftsLines -match "fts\.unavailable") {
            $runtimeLogState = "unavailable"
        }
    }

    $fts5Available = $fts5Create.success -or ($moduleNames -contains "fts5") -or $runtimeLogState -eq "fts5" -or ($fts5Match -and $fts5Match.success)
    $authoritative = $false
    if (($fts5Create -and -not $fts5Create.success -and $fts5Create.output -match "no such module: fts5") -and
        ($fts5Match -and -not $fts5Match.success -and $fts5Match.output -match "no such module: fts5") -and
        $runtimeLogState -eq "unavailable") {
        $authoritative = $true
    }

    return [pscustomobject]@{
        device = $Device
        androidRelease = Get-DeviceProperty -AdbPath $AdbPath -Device $Device -PropertyName "ro.build.version.release"
        androidSdk = Get-DeviceProperty -AdbPath $AdbPath -Device $Device -PropertyName "ro.build.version.sdk"
        model = Get-DeviceProperty -AdbPath $AdbPath -Device $Device -PropertyName "ro.product.model"
        abi = Get-DeviceProperty -AdbPath $AdbPath -Device $Device -PropertyName "ro.product.cpu.abi"
        appInstalled = $appInstalled
        appPath = $appPathResult.output
        runAsOk = $runAsOk
        runAsWorkingDirectory = $runAsPwd.output
        sqliteVersion = $sqliteVersion
        moduleList = $moduleList
        moduleNames = $moduleNames
        memoryCreateFts5 = $fts5Create
        memoryCreateFts4 = $fts4Create
        installedPackFiles = $packFiles
        installedPackManifestText = if ($packManifestText) { $packManifestText.output } else { $null }
        installedPackManifest = $packManifest
        installedPackFts5Schema = if ($fts5Schema) { $fts5Schema.output } else { $null }
        installedPackFts4Schema = if ($fts4Schema) { $fts4Schema.output } else { $null }
        installedPackFts5Match = $fts5Match
        installedPackFts4Match = $fts4Match
        appRuntime = $appRuntime
        conclusion = [pscustomobject]@{
            fts5Available = $fts5Available
            appRuntimeState = $runtimeLogState
            shellFts4Available = $fts4Create.success -or ($moduleNames -contains "fts4") -or ($fts4Match -and $fts4Match.success)
            authoritativeForTestedRuntime = $authoritative
        }
    }
}

$repoRoot = Get-RepoRoot
$adbPath = Get-AdbPath
$pythonPath = Get-PythonPath
Invoke-CapturedCommand -FilePath $adbPath -Arguments @("start-server") | Out-Null

if ([string]::IsNullOrWhiteSpace($HostSqlitePath)) {
    $HostSqlitePath = Join-Path $repoRoot "android-app\app\src\main\assets\mobile_pack\senku_mobile.sqlite3"
}

$connectedDevices = Get-ConnectedDevices -AdbPath $adbPath
if (-not $Devices -or $Devices.Count -eq 0) {
    if (-not $connectedDevices -or $connectedDevices.Count -eq 0) {
        throw "No connected Android devices or emulators were detected."
    }
    $Devices = Order-Devices -Serials $connectedDevices
} else {
    $Devices = Normalize-DeviceArguments -Serials $Devices
    $Devices = Order-Devices -Serials $Devices
}

$outputDirectory = Get-OutputDirectory -RepoRoot $repoRoot -RequestedOutputRoot $OutputRoot
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$hostProbe = Get-HostPackProbe -PythonPath $pythonPath -SqlitePath $HostSqlitePath
$hostTables = @()
if ($hostProbe.PSObject.Properties.Name -contains "tables") {
    $hostTables = @($hostProbe.tables)
}
$deviceProbes = @()
foreach ($device in $Devices) {
    $deviceProbes += Get-DeviceProbe -AdbPath $adbPath -Device $device -PackageName $PackageName -LaunchActivity $LaunchActivity -LaunchQuery $LaunchQuery -LaunchWaitSeconds $LaunchWaitSeconds -OutputDirectory $outputDirectory
}

$authoritativeDevices = @($deviceProbes | Where-Object { $_.conclusion.authoritativeForTestedRuntime })
$fts5PositiveDevices = @($deviceProbes | Where-Object { $_.conclusion.fts5Available })
$appUnavailableDevices = @($deviceProbes | Where-Object { $_.conclusion.appRuntimeState -eq "unavailable" })

$summary = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    repoRoot = $repoRoot
    outputDirectory = $outputDirectory
    packageName = $PackageName
    launchActivity = $LaunchActivity
    launchQuery = $LaunchQuery
    hostPack = $hostProbe
    devices = $deviceProbes
    conclusion = [pscustomobject]@{
        testedDeviceCount = $deviceProbes.Count
        hostPackContainsFts5Schema = @($hostTables | Where-Object { $_.name -eq "lexical_chunks_fts" }).Count -gt 0
        hostPackContainsFts4Schema = @($hostTables | Where-Object { $_.name -eq "lexical_chunks_fts4" }).Count -gt 0
        anyDeviceReportsFts5 = $fts5PositiveDevices.Count -gt 0
        allTestedDevicesMarkedAppUnavailable = $deviceProbes.Count -gt 0 -and $appUnavailableDevices.Count -eq $deviceProbes.Count
        authoritativeForTestedRuntime = $authoritativeDevices.Count -eq $deviceProbes.Count
    }
}

$summaryJson = $summary | ConvertTo-Json -Depth 8
Save-Utf8Text -Path (Join-Path $outputDirectory "summary.json") -Content $summaryJson

Write-Host ("Output directory: {0}" -f $outputDirectory)
Write-Host ("Host pack contains lexical_chunks_fts: {0}" -f $summary.conclusion.hostPackContainsFts5Schema)
Write-Host ("Host pack contains lexical_chunks_fts4: {0}" -f $summary.conclusion.hostPackContainsFts4Schema)

foreach ($probe in $deviceProbes) {
    $deviceJson = $probe | ConvertTo-Json -Depth 8
    Save-Utf8Text -Path (Join-Path $outputDirectory ($probe.device + ".json")) -Content $deviceJson

    $fts5Status = if ($probe.conclusion.fts5Available) { "yes" } else { "no" }
    $authority = if ($probe.conclusion.authoritativeForTestedRuntime) { "authoritative" } else { "best-effort" }
    Write-Host ("[{0}] FTS5 available: {1} | app runtime: {2} | shell FTS4: {3} | confidence: {4}" -f `
            $probe.device,
            $fts5Status,
            $probe.conclusion.appRuntimeState,
            $probe.conclusion.shellFts4Available,
            $authority)
}

if ($summary.conclusion.authoritativeForTestedRuntime) {
    exit 0
}

if ($deviceProbes.Count -gt 0 -or $hostProbe.exists) {
    Write-Warning "Probe completed with partial evidence. See summary.json for missing layers."
    exit 0
}

throw "Probe did not collect any usable host or device evidence."

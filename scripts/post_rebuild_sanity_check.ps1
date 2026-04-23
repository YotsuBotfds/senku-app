[CmdletBinding()]
param(
    [string]$PythonExe = 'python'
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

function Invoke-Step {
    param(
        [string]$Title,
        [scriptblock]$Script
    )

    Write-Host ""
    Write-Host "=== $Title ==="
    & $Script
}

Invoke-Step -Title 'Collection stats' -Script {
    & $PythonExe ingest.py --stats
}

Invoke-Step -Title 'Manifest summary' -Script {
    if (-not (Test-Path 'db\ingest_manifest.json')) {
        Write-Host 'Manifest not found: db\ingest_manifest.json'
        return
    }

    $manifest = Get-Content 'db\ingest_manifest.json' -Raw | ConvertFrom-Json
    $entryCount = @($manifest.PSObject.Properties).Count
    Write-Host ("Manifest entries: {0}" -f $entryCount)
    @($manifest.PSObject.Properties | Select-Object -First 5 Name, Value) | Format-Table -AutoSize
}

Invoke-Step -Title 'Prompt packs ready to run' -Script {
    @(
        'artifacts/prompts/adhoc/test_targeted_guide_direction_wave_af_20260415.txt',
        'artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ag_20260415.txt',
        'artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ah_20260415.txt'
    ) | ForEach-Object {
        $exists = Test-Path $_
        [pscustomobject]@{
            prompt_pack = $_
            exists      = $exists
        }
    } | Format-Table -AutoSize
}

Invoke-Step -Title 'Suggested validation commands' -Script {
    @'
.\scripts\run_guide_prompt_validation.ps1 -Wave af
.\scripts\run_guide_prompt_validation.ps1 -Wave ag
.\scripts\run_guide_prompt_validation.ps1 -Wave ah
'@
}

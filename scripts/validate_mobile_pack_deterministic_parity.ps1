$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$venvPython = Join-Path $repoRoot "venv\Scripts\python.exe"
$pythonExe = if (Test-Path $venvPython) { $venvPython } else { "python" }

Push-Location $repoRoot
try {
    @'
from mobile_pack import validate_mobile_pack_deterministic_parity

result = validate_mobile_pack_deterministic_parity()
print(
    "Validated mobile-pack deterministic parity: "
    f"{result['exported_rule_count']} exported rules, "
    f"{result['java_predicate_count']} Android predicates."
)
'@ | & $pythonExe -
}
finally {
    Pop-Location
}

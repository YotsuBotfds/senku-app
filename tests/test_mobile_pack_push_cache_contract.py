import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "push_mobile_pack_to_android.ps1"


class MobilePackPushCacheContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def assertBlockContainsFields(self, start_marker, end_marker, fields):
        start = self.script.index(start_marker)
        end = self.script.index(end_marker, start)
        block = self.script[start:end]
        for field in fields:
            self.assertIn(field, block)

    def test_exposes_opt_in_cache_controls(self):
        self.assertIn("[switch]$SkipIfCurrent", self.script)
        self.assertIn("[switch]$ForcePush", self.script)
        self.assertIn('[string]$SummaryPath = ""', self.script)

    def test_uses_device_scoped_harness_state_file(self):
        self.assertIn('Join-Path $repoRoot "artifacts\\harness_state"', self.script)
        self.assertIn('"mobile_pack_push_" + (Get-SafeStateName -Value $Device) + ".json"', self.script)

    def test_fingerprint_covers_manifest_sqlite_and_vector_files(self):
        self.assertIn("Get-PackFileFingerprint", self.script)
        self.assertIn("Get-FileHash -Algorithm SHA256 -LiteralPath $Path", self.script)
        self.assertIn("senku_manifest.json", self.script)
        self.assertIn("$manifestSummary.sqliteName", self.script)
        self.assertIn("$manifestSummary.vectorName", self.script)

    def test_skip_if_current_verifies_installed_files_before_returning(self):
        skip_marker = "Local pack fingerprint and installed file sizes match prior verified push; skipping adb upload."
        wait_marker = 'Invoke-AdbChecked -Arguments @("-s", $Device, "wait-for-device")'
        self.assertGreater(self.script.index(skip_marker), self.script.index(wait_marker))
        self.assertIn("Test-MobilePackPushStateCurrent -StatePath $statePath -ExpectedState $pushState", self.script)
        self.assertIn("function Test-InstalledPackFilesCurrent", self.script)
        self.assertIn('"-s", $Device, "shell", "run-as", $PackageName, "wc", "-c"', self.script)
        self.assertIn('Test-InstalledPackFilesCurrent -PackFiles $packFiles -ManifestName "senku_manifest.json"', self.script)

    def test_console_and_summary_include_cache_hit(self):
        self.assertIn('Write-Host "Mobile pack push cache hit: true"', self.script)
        self.assertIn('Write-Host "Mobile pack push cache hit: false"', self.script)
        self.assertIn("cache_hit = [bool]$cacheHit", self.script)
        self.assertIn("Write-MobilePackPushSummary -Summary $summaryObject -Path $SummaryPath", self.script)

    def test_cache_hit_summary_preserves_cache_schema_fields(self):
        self.assertBlockContainsFields(
            "Local pack fingerprint and installed file sizes match prior verified push; skipping adb upload.",
            "Write-MobilePackPushSummary -Summary $summaryObject -Path $SummaryPath",
            [
                "state_path = $statePath",
                "cache_hit = [bool]$cacheHit",
                "skip_if_current = [bool]$SkipIfCurrent",
                "force_push = [bool]$ForcePush",
                "pushed = $false",
                "pack_version = $manifestSummary.manifest.pack_version",
                "pack_fingerprint = $packFingerprint",
            ],
        )

    def test_pushed_summary_preserves_cache_schema_fields(self):
        self.assertBlockContainsFields(
            "if ($ShowInstalledManifest) {",
            "Write-MobilePackPushSummary -Summary $summaryObject -Path $SummaryPath",
            [
                "state_path = $statePath",
                "cache_hit = [bool]$cacheHit",
                "skip_if_current = [bool]$SkipIfCurrent",
                "force_push = [bool]$ForcePush",
                "pushed = $true",
                "pack_version = $manifestSummary.manifest.pack_version",
                "pack_fingerprint = $packFingerprint",
            ],
        )

    def test_verified_state_write_preserves_cache_schema_fields(self):
        state_dir = 'Join-Path $repoRoot "artifacts\\harness_state"'
        state_file = '"mobile_pack_push_" + (Get-SafeStateName -Value $Device) + ".json"'
        mkdir = "New-Item -ItemType Directory -Force -Path $harnessStateDir | Out-Null"
        write_state = '($verifiedPushState | ConvertTo-Json -Depth 6) | Set-Content -LiteralPath $statePath -Encoding UTF8'

        self.assertLess(self.script.index(state_dir), self.script.index(state_file))
        self.assertLess(self.script.index(state_file), self.script.index(mkdir))
        self.assertLess(self.script.index(mkdir), self.script.index(write_state))
        self.assertBlockContainsFields(
            "$verifiedPushState = [pscustomobject]@{",
            write_state,
            [
                "schema_version = $pushState.schema_version",
                'updated_utc = (Get-Date).ToUniversalTime().ToString("o")',
                "pack_fingerprint = $pushState.pack_fingerprint",
                "files = $pushState.files",
            ],
        )


if __name__ == "__main__":
    unittest.main()

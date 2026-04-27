import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_instrumented_ui_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidInstrumentedUiSmokeCaptureSummaryTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_capture_summary_is_opt_in_and_separate_from_run_summary(self):
        self.assertIn('[string]$SummaryPath = ""', self.script)
        self.assertIn('[string]$CaptureSummaryPath = ""', self.script)
        self.assertIn('function Write-AndroidInstrumentedCaptureSummary', self.script)
        self.assertIn('Write-AndroidInstrumentedCaptureSummary `', self.script)
        self.assertIn('-Path $CaptureSummaryPath `', self.script)
        self.assertIn('$summaryJson | Set-Content -LiteralPath $SummaryPath -Encoding UTF8', self.script)

    def test_capture_summary_emits_canonical_fields_from_smoke_metadata(self):
        for expected in (
            "schema_version = 1",
            "serial = $Device",
            "role = $captureRole",
            "orientation = $Orientation",
            'apk_sha256 = $(if ([string]::IsNullOrWhiteSpace($ResolvedApkSha)) { "not_provided" } else { $ResolvedApkSha })',
            'platform_tools_version = $(if ([string]::IsNullOrWhiteSpace($hostAdbPlatformToolsVersion)) { "not_provided" } else { $hostAdbPlatformToolsVersion })',
            "artifacts = [pscustomobject]@{",
            'screenshot = New-CaptureSummaryArtifact -Name "screenshot" -Path $firstScreenshotPath',
            'ui_dump = New-CaptureSummaryArtifact -Name "ui_dump" -Path $firstDumpPath',
            'logcat = New-CaptureSummaryArtifact -Name "logcat" -Path $logcatArtifactPath',
            "model_identity = [pscustomobject]@{",
            "installed_pack_metadata = Convert-InstalledPackMetadataForCaptureSummary -InstalledPack $InstalledPack",
        ):
            self.assertIn(expected, self.script)

    def test_capture_summary_stays_non_acceptance_shape_evidence(self):
        self.assertNotIn("CaptureSummaryEvidenceRole", self.script)
        self.assertIn("non_acceptance_evidence = $true", self.script)
        self.assertIn("acceptance_evidence = $false", self.script)

    def test_capture_summary_hashes_local_artifacts_and_fails_when_required_artifact_missing(self):
        self.assertIn("function Get-LocalFileSha256", self.script)
        self.assertIn("Get-FileHash -Algorithm SHA256 -LiteralPath $Path", self.script)
        self.assertIn('throw "Cannot write canonical capture summary: required $Name artifact is missing at $Path"', self.script)
        self.assertIn("-ScreenshotFiles $localScreenshotFiles `", self.script)
        self.assertIn("-DumpFiles $localDumpFiles `", self.script)
        self.assertIn("-LogcatFilePath $(if ($logcatPath -and (Test-Path -LiteralPath $logcatPath -PathType Leaf)) { $logcatPath } else { $null }) `", self.script)

    def test_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_instrumented_ui_smoke.ps1",
                "-SkipAnalyzer",
                "-SkipPester",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)


if __name__ == "__main__":
    unittest.main()

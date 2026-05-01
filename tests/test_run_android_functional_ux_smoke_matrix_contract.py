import re
import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_functional_ux_smoke_matrix.ps1"
SMOKE_SCRIPT = REPO_ROOT / "scripts" / "run_android_instrumented_ui_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidFunctionalUxSmokeMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")
        cls.smoke_script = SMOKE_SCRIPT.read_text(encoding="utf-8-sig")

    def test_functional_follow_up_preset_is_in_matrix(self):
        self.assertIn("- phone-functional-follow-up", self.script)
        self.assertIn('"phone-functional-follow-up"', self.script)

        match = re.search(r"\$presets = @\((.*?)\)", self.script, re.S)
        self.assertIsNotNone(match)
        presets = re.findall(r'"([^"]+)"', match.group(1))
        self.assertEqual(
            presets,
            [
                "phone-functional",
                "phone-functional-follow-up",
                "phone-functional-saved",
                "phone-functional-back-provenance",
            ],
        )

    def test_matrix_presets_are_registered_by_child_smoke_runner(self):
        matrix_match = re.search(r"\$presets = @\((.*?)\)", self.script, re.S)
        self.assertIsNotNone(matrix_match)
        matrix_presets = re.findall(r'"([^"]+)"', matrix_match.group(1))

        smoke_lines = self.smoke_script.splitlines()
        smoke_preset_line_index = next(
            index for index, line in enumerate(smoke_lines) if "[string]$SmokePreset" in line
        )
        validate_set_line = smoke_lines[smoke_preset_line_index - 1]
        self.assertIn("[ValidateSet(", validate_set_line)
        registered_presets = {preset for preset in re.findall(r'"([^"]*)"', validate_set_line) if preset}

        switch_match = re.search(r"switch \(\$SmokePreset\) \{(.*?)\n\}", self.smoke_script, re.S)
        self.assertIsNotNone(switch_match)
        switch_presets = set(re.findall(r'^\s*"([^"]+)"\s*\{', switch_match.group(1), re.M))

        for preset in matrix_presets:
            with self.subTest(preset=preset):
                self.assertIn(preset, registered_presets)
                self.assertIn(preset, switch_presets)

    def test_runner_invokes_each_preset_with_capture_summary(self):
        self.assertIn('"-SmokePreset"', self.script)
        self.assertIn("$preset,", self.script)
        self.assertIn('"-SummaryPath"', self.script)
        self.assertIn('"-CaptureSummaryPath"', self.script)
        self.assertIn('"-CaptureLogcat"', self.script)
        self.assertIn('$canReuseInstalledApks = $true', self.script)

    def test_matrix_summary_explains_skip_and_reuse_build_state(self):
        self.assertIn("$reuseInstalledApksForPreset = [bool]$canReuseInstalledApks", self.script)
        self.assertIn("$effectiveSkipBuild = [bool]($SkipBuild -or $reuseInstalledApksForPreset)", self.script)
        self.assertIn("$effectiveSkipInstall = [bool]($SkipInstall -or $reuseInstalledApksForPreset)", self.script)
        self.assertIn("skip_build_requested = [bool]$SkipBuild", self.script)
        self.assertIn("skip_install_requested = [bool]$SkipInstall", self.script)
        self.assertIn("reused_installed_apks = [bool]$reuseInstalledApksForPreset", self.script)
        self.assertIn("effective_skip_build = [bool]$effectiveSkipBuild", self.script)
        self.assertIn("effective_skip_install = [bool]$effectiveSkipInstall", self.script)

    def test_matrix_holds_single_outer_device_lock_for_child_runs(self):
        self.assertIn('$lockRoot = Join-Path $repoRoot "artifacts\\harness_locks"', self.script)
        self.assertIn("function Acquire-MatrixDeviceLock", self.script)
        self.assertIn('ProgressLabel ("[functional-ux-matrix:{0}]" -f $DeviceName)', self.script)
        self.assertIn("acquiring matrix device lock", self.script)
        self.assertIn("child smoke runs will skip nested lock acquisition", self.script)
        self.assertIn("if ($SkipDeviceLock -or $matrixDeviceLockUsed)", self.script)
        self.assertIn("device_lock_posture = $(if ($matrixDeviceLockUsed) { \"matrix_lock_children_skip_nested\" } else { \"skipped\" })", self.script)

    def test_matrix_preflights_phone_device_role(self):
        self.assertIn("function Assert-FunctionalUxPhoneDevice", self.script)
        self.assertIn("Resolve-AndroidDeviceFacts -AdbPath $adb -DeviceName $Device", self.script)
        self.assertIn("Functional UX smoke matrix runs phone-* presets", self.script)
        self.assertIn("Assert-FunctionalUxPhoneDevice", self.script)

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
                "scripts\\run_android_functional_ux_smoke_matrix.ps1",
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

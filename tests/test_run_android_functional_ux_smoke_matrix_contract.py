import json
import os
import re
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_functional_ux_smoke_matrix.ps1"
SMOKE_SCRIPT = REPO_ROOT / "scripts" / "run_android_instrumented_ui_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"

EXPECTED_FUNCTIONAL_PRESETS = [
    "phone-functional",
    "phone-functional-follow-up",
    "phone-functional-saved",
    "phone-functional-back-provenance",
]
EXPECTED_TABLET_FUNCTIONAL_PRESETS = [
    "tablet-functional-rail",
    "tablet-functional-header",
]


def _run_powershell(args, *, env=None, timeout=30):
    return subprocess.run(
        ["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", *args],
        cwd=REPO_ROOT,
        env=env,
        capture_output=True,
        text=True,
        check=False,
        timeout=timeout,
    )


def _matrix_presets_for_package(script_text, package):
    match = re.search(
        rf'"{re.escape(package)}"\s*\{{\s*return\s*@\((.*?)\)\s*\}}',
        script_text,
        re.S,
    )
    if match is None:
        raise AssertionError(f"Could not find matrix preset declaration for {package}.")
    return re.findall(r'"([^"]+)"', match.group(1))


class RunAndroidFunctionalUxSmokeMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")
        cls.smoke_script = SMOKE_SCRIPT.read_text(encoding="utf-8-sig")

    def test_functional_matrix_presets_are_declared_and_registered_by_child_runner(self):
        phone_presets = _matrix_presets_for_package(self.script, "phone-functional")
        tablet_presets = _matrix_presets_for_package(self.script, "tablet-functional")
        self.assertEqual(phone_presets, EXPECTED_FUNCTIONAL_PRESETS)
        self.assertEqual(tablet_presets, EXPECTED_TABLET_FUNCTIONAL_PRESETS)

        smoke_lines = self.smoke_script.splitlines()
        smoke_preset_line_index = next(
            index for index, line in enumerate(smoke_lines) if "[string]$SmokePreset" in line
        )
        validate_set_line = smoke_lines[smoke_preset_line_index - 1]
        registered_presets = {preset for preset in re.findall(r'"([^"]*)"', validate_set_line) if preset}

        missing_presets = sorted(set(phone_presets + tablet_presets) - registered_presets)
        self.assertEqual(missing_presets, [])

    def test_default_preset_package_preserves_phone_matrix_behavior(self):
        self.assertIn('[string]$PresetPackage = "phone-functional"', self.script)
        self.assertIn('"phone-functional" {', self.script)
        self.assertIn('"tablet-functional" {', self.script)
        self.assertIn("preset_package = $PresetPackage", self.script)

    def test_matrix_device_role_preflight_uses_bounded_adb_wait(self):
        self.assertIn("[int]$DeviceWaitTimeoutSeconds = 120", self.script)
        self.assertIn('throw "-DeviceWaitTimeoutSeconds must be 1 or greater."', self.script)
        self.assertIn('$waitTimeoutMilliseconds = [Math]::Max(1000, $DeviceWaitTimeoutSeconds * 1000)', self.script)
        self.assertIn('Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments @("-s", $Device, "wait-for-device") -TimeoutMilliseconds $waitTimeoutMilliseconds', self.script)
        self.assertIn("adb wait-for-device timed out after $waitTimeoutMilliseconds ms for $Device during matrix device-role preflight", self.script)
        self.assertNotIn("& $adb -s $Device wait-for-device", self.script)

    def test_child_runner_receives_quoted_paths_and_matrix_arguments(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_root = Path(temp_dir) / "contract path with spaces"
            temp_root.mkdir()
            capture_path = temp_root / "received args.jsonl"
            child_runner = temp_root / "fake child runner.ps1"
            artifact_root = temp_root / "artifact root with spaces"
            output_label = "matrix label"
            child_runner.write_text(
                r'''
$record = [ordered]@{
    args = @($args)
    smoke_preset = $args[([Array]::IndexOf($args, "-SmokePreset") + 1)]
    artifact_root = $args[([Array]::IndexOf($args, "-ArtifactRoot") + 1)]
    summary_path = $args[([Array]::IndexOf($args, "-SummaryPath") + 1)]
    capture_summary_path = $args[([Array]::IndexOf($args, "-CaptureSummaryPath") + 1)]
}
($record | ConvertTo-Json -Compress) | Add-Content -LiteralPath $env:MATRIX_ARG_CAPTURE
$summaryPath = $record.summary_path
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $summaryPath) | Out-Null
[ordered]@{
    status = "passed"
    failure_reason = $null
    selected_test_methods = @("fake")
    screenshot_count = 0
    dump_count = 0
    artifact_expectations_met = $true
} | ConvertTo-Json | Set-Content -LiteralPath $summaryPath -Encoding UTF8
exit 0
''',
                encoding="utf-8",
            )

            env = {**os.environ, "MATRIX_ARG_CAPTURE": str(capture_path)}
            result = _run_powershell(
                [
                    "-File",
                    str(SCRIPT),
                    "-Device",
                    "emulator-5554",
                    "-ArtifactRoot",
                    str(artifact_root),
                    "-OutputLabel",
                    output_label,
                    "-ChildRunnerOverride",
                    str(child_runner),
                    "-SkipDeviceLock",
                    "-SkipDevicePreflight",
                    "-PresetTimeoutSeconds",
                    "0",
                ],
                env=env,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            invocations = [
                json.loads(line)
                for line in capture_path.read_text(encoding="utf-8").splitlines()
                if line.strip()
            ]

        self.assertEqual([entry["smoke_preset"] for entry in invocations], EXPECTED_FUNCTIONAL_PRESETS)
        for entry in invocations:
            args = entry["args"]
            self.assertIn("-CaptureLogcat", args)
            self.assertIn("-SummaryPath", args)
            self.assertIn("-CaptureSummaryPath", args)
            self.assertIn("artifact root with spaces", entry["artifact_root"])
            self.assertIn(output_label, entry["summary_path"])
            self.assertIn(output_label, entry["capture_summary_path"])

    def test_tablet_package_child_runner_receives_tablet_presets_only(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_root = Path(temp_dir)
            capture_path = temp_root / "received_tablet_args.jsonl"
            child_runner = temp_root / "fake_child.ps1"
            artifact_root = temp_root / "artifacts"
            output_label = "tablet_contract"
            child_runner.write_text(
                r'''
$record = [ordered]@{
    args = @($args)
    smoke_preset = $args[([Array]::IndexOf($args, "-SmokePreset") + 1)]
}
($record | ConvertTo-Json -Compress) | Add-Content -LiteralPath $env:MATRIX_ARG_CAPTURE
$summaryPath = $args[([Array]::IndexOf($args, "-SummaryPath") + 1)]
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $summaryPath) | Out-Null
[ordered]@{
    status = "passed"
    failure_reason = $null
    selected_test_methods = @("fake")
    screenshot_count = 0
    dump_count = 0
    artifact_expectations_met = $true
} | ConvertTo-Json | Set-Content -LiteralPath $summaryPath -Encoding UTF8
exit 0
''',
                encoding="utf-8",
            )

            env = {**os.environ, "MATRIX_ARG_CAPTURE": str(capture_path)}
            result = _run_powershell(
                [
                    "-File",
                    str(SCRIPT),
                    "-Device",
                    "emulator-5554",
                    "-PresetPackage",
                    "tablet-functional",
                    "-ArtifactRoot",
                    str(artifact_root),
                    "-OutputLabel",
                    output_label,
                    "-ChildRunnerOverride",
                    str(child_runner),
                    "-SkipDeviceLock",
                    "-SkipDevicePreflight",
                    "-PresetTimeoutSeconds",
                    "0",
                ],
                env=env,
            )

            invocations = [
                json.loads(line)
                for line in capture_path.read_text(encoding="utf-8").splitlines()
                if line.strip()
            ]
            summary = json.loads((artifact_root / output_label / "matrix_summary.json").read_text(encoding="utf-8-sig"))

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertEqual([entry["smoke_preset"] for entry in invocations], EXPECTED_TABLET_FUNCTIONAL_PRESETS)
        self.assertEqual(summary["preset_package"], "tablet-functional")
        self.assertEqual([preset["preset"] for preset in summary["presets"]], EXPECTED_TABLET_FUNCTIONAL_PRESETS)

    def test_matrix_summary_records_reuse_build_state_behaviorally(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_root = Path(temp_dir)
            child_runner = temp_root / "fake_child.ps1"
            artifact_root = temp_root / "artifacts"
            output_label = "reuse_contract"
            child_runner.write_text(
                r'''
$summaryIndex = [Array]::IndexOf($args, "-SummaryPath")
$summaryPath = $args[$summaryIndex + 1]
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $summaryPath) | Out-Null
[ordered]@{
    status = "passed"
    failure_reason = $null
    selected_test_methods = @("fake")
    screenshot_count = 0
    dump_count = 0
    artifact_expectations_met = $true
} | ConvertTo-Json | Set-Content -LiteralPath $summaryPath -Encoding UTF8
exit 0
''',
                encoding="utf-8",
            )

            result = _run_powershell(
                [
                    "-File",
                    str(SCRIPT),
                    "-Device",
                    "emulator-5554",
                    "-ArtifactRoot",
                    str(artifact_root),
                    "-OutputLabel",
                    output_label,
                    "-ChildRunnerOverride",
                    str(child_runner),
                    "-SkipDeviceLock",
                    "-SkipDevicePreflight",
                    "-PresetTimeoutSeconds",
                    "0",
                ]
            )

            summary_path = artifact_root / output_label / "matrix_summary.json"
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertTrue(summary["passed"])
        self.assertEqual(summary["completed_preset_count"], len(EXPECTED_FUNCTIONAL_PRESETS))
        self.assertEqual([preset["preset"] for preset in summary["presets"]], EXPECTED_FUNCTIONAL_PRESETS)
        self.assertFalse(summary["presets"][0]["reused_installed_apks"])
        for preset in summary["presets"][1:]:
            self.assertTrue(preset["reused_installed_apks"])
            self.assertTrue(preset["effective_skip_build"])
            self.assertTrue(preset["effective_skip_install"])

    def test_watchdog_times_out_child_process_and_writes_matrix_timeout_summary(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_root = Path(temp_dir)
            child_runner = temp_root / "slow_child.ps1"
            artifact_root = temp_root / "artifacts"
            output_label = "timeout_contract"
            child_runner.write_text("Start-Sleep -Seconds 30\nexit 0\n", encoding="utf-8")

            result = _run_powershell(
                [
                    "-File",
                    str(SCRIPT),
                    "-Device",
                    "emulator-5554",
                    "-ArtifactRoot",
                    str(artifact_root),
                    "-OutputLabel",
                    output_label,
                    "-ChildRunnerOverride",
                    str(child_runner),
                    "-SkipDeviceLock",
                    "-SkipDevicePreflight",
                    "-PresetTimeoutSeconds",
                    "1",
                    "-PresetProgressIntervalSeconds",
                    "1",
                    "-FailFast",
                ],
                timeout=15,
            )

            summary_path = artifact_root / output_label / "matrix_summary.json"
            summary = json.loads(summary_path.read_text(encoding="utf-8-sig"))

        self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertFalse(summary["passed"])
        self.assertTrue(summary["stopped_early"])
        self.assertEqual(summary["completed_preset_count"], 1)
        first_preset = summary["presets"][0]
        self.assertEqual(first_preset["exit_code"], 124)
        self.assertTrue(first_preset["timed_out"])
        self.assertEqual(first_preset["status"], "matrix_timeout")
        self.assertIn("watchdog timed out", first_preset["failure_reason"])

    def test_parser_gate_passes(self):
        result = _run_powershell(
            [
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_functional_ux_smoke_matrix.ps1",
                "-SkipAnalyzer",
                "-SkipPester",
            ]
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)


if __name__ == "__main__":
    unittest.main()

import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_managed_device_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class AndroidManagedDeviceSmokeContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_wrapper_is_dry_run_only_and_documents_non_acceptance_boundary(self):
        self.assertIn("[switch]$DryRun", self.script)
        self.assertIn("[string]$TaskInventoryPath", self.script)
        self.assertIn("[switch]$ProbeTaskInventory", self.script)
        self.assertIn("if (-not $DryRun)", self.script)
        self.assertIn("dry-run-only", self.script)
        self.assertIn("must not launch Gradle Managed Devices yet", self.script)
        self.assertIn('"-Psenku.enableManagedDevices=true"', self.script)
        self.assertIn('"senkuManagedSmoke"', self.script)
        self.assertIn("non_acceptance_evidence = $true", self.script)
        self.assertIn("would_launch_emulators = $false", self.script)
        self.assertIn("managed_devices_launched = $false", self.script)
        self.assertIn("acceptance_evidence = $false", self.script)
        self.assertIn("function Parse-ManagedDeviceScaffold", self.script)
        self.assertIn("function Expand-ArtifactRoots", self.script)
        self.assertIn("function Build-ExpectedGradleTaskNames", self.script)
        self.assertIn("function Build-PlannedResultEvidenceSchema", self.script)
        self.assertIn("function Parse-GradleTaskInventory", self.script)
        self.assertIn("function Read-TaskInventory", self.script)
        self.assertIn("function Invoke-TaskInventoryProbe", self.script)
        self.assertIn("agp_plugin_version", self.script)
        self.assertIn("configured_device_names", self.script)
        self.assertIn("configured_device_api_levels", self.script)
        self.assertIn("configured_device_image_sources", self.script)
        self.assertIn("managed_device_scaffold", self.script)
        self.assertIn("smoke_group", self.script)
        self.assertIn("expected_artifact_roots", self.script)
        self.assertIn("observed_gradle_task_names", self.script)
        self.assertIn("observed_expected_gradle_task_names", self.script)
        self.assertIn("planned_result_evidence_schema", self.script)
        self.assertIn("planned_not_collected", self.script)
        self.assertIn("total_test_count", self.script)
        self.assertIn("failure_count", self.script)
        self.assertIn("artifact_paths", self.script)
        self.assertIn("task_inventory_source", self.script)
        self.assertIn("task_inventory_probe_ran", self.script)
        self.assertIn('$plannedTaskInventoryCommand = ".\\gradlew.bat :app:tasks --all $managedDeviceProperty --console=plain"', self.script)
        self.assertIn('$comparisonBaseline = "fixed_four_emulator_matrix"', self.script)
        self.assertIn("fixed four-emulator evidence remains primary", self.script)
        self.assertIn('$summaryJsonPath = Join-Path $resolvedOutputDir "summary.json"', self.script)
        self.assertIn('$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"', self.script)

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
                "scripts\\run_android_managed_device_smoke.ps1",
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

    def test_dry_run_writes_repo_shaped_summary(self):
        output_dir = Path(tempfile.mkdtemp(prefix="managed_device_smoke_"))
        try:
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir),
                    "-DryRun",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("fixed four-emulator evidence remains primary", result.stdout)
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "dry_run_only")
            self.assertTrue(summary["dry_run"])
            self.assertTrue(summary["non_acceptance_evidence"])
            self.assertEqual(summary["gradle_property"], "-Psenku.enableManagedDevices=true")
            self.assertEqual(summary["task_group"], "senkuManagedSmoke")
            self.assertEqual(summary["task_name"], ":app:senkuManagedSmoke")
            self.assertEqual(summary["expected_devices"], ["senkuPhoneApi30", "senkuTabletApi30"])
            self.assertEqual(
                summary["expected_artifact_roots"],
                [
                    "android-app/app/build/outputs/androidTest-results/managedDevice/senkuPhoneApi30",
                    "android-app/app/build/outputs/androidTest-results/managedDevice/senkuTabletApi30",
                    "android-app/app/build/reports/androidTests/managedDevice/senkuPhoneApi30",
                    "android-app/app/build/reports/androidTests/managedDevice/senkuTabletApi30",
                ],
            )
            self.assertEqual(summary["expected_test_target"], ":app:senkuManagedSmoke")
            self.assertEqual(summary["comparison_baseline"], "fixed_four_emulator_matrix")
            self.assertEqual(
                summary["planned_task_inventory_command"],
                ".\\gradlew.bat :app:tasks --all -Psenku.enableManagedDevices=true --console=plain",
            )
            self.assertEqual(
                summary["expected_gradle_task_names"],
                [
                    ":app:senkuPhoneApi30DebugAndroidTest",
                    ":app:senkuTabletApi30DebugAndroidTest",
                    ":app:senkuManagedSmokeGroupDebugAndroidTest",
                ],
            )
            self.assertEqual(summary["observed_gradle_task_names"], [])
            self.assertEqual(summary["observed_expected_gradle_task_names"], [])
            planned_schema = summary["planned_result_evidence_schema"]
            self.assertEqual(planned_schema["schema_version"], 1)
            self.assertEqual(planned_schema["status"], "planned_not_collected")
            self.assertTrue(planned_schema["non_acceptance_evidence"])
            self.assertFalse(planned_schema["acceptance_evidence"])
            self.assertIsNone(planned_schema["result_fields"]["total_test_count"])
            self.assertIsNone(planned_schema["result_fields"]["passed_test_count"])
            self.assertIsNone(planned_schema["result_fields"]["failed_test_count"])
            self.assertIsNone(planned_schema["result_fields"]["skipped_test_count"])
            self.assertIsNone(planned_schema["result_fields"]["failure_count"])
            self.assertEqual(planned_schema["result_fields"]["failures"], [])
            self.assertEqual(
                planned_schema["result_fields"]["per_device_results"],
                [
                    {
                        "device_name": "senkuPhoneApi30",
                        "total_test_count": None,
                        "failed_test_count": None,
                        "artifact_paths": [],
                    },
                    {
                        "device_name": "senkuTabletApi30",
                        "total_test_count": None,
                        "failed_test_count": None,
                        "artifact_paths": [],
                    },
                ],
            )
            self.assertEqual(planned_schema["evidence_fields"]["artifact_roots"], summary["expected_artifact_roots"])
            self.assertEqual(planned_schema["evidence_fields"]["junit_xml_paths"], [])
            self.assertEqual(planned_schema["evidence_fields"]["html_report_paths"], [])
            self.assertEqual(planned_schema["evidence_fields"]["logcat_paths"], [])
            self.assertEqual(planned_schema["evidence_fields"]["screenshot_paths"], [])
            self.assertIsNone(planned_schema["evidence_fields"]["stdout_path"])
            self.assertIsNone(planned_schema["evidence_fields"]["stderr_path"])
            self.assertEqual(summary["task_inventory_source"], "not_collected")
            self.assertFalse(summary["task_inventory_probe_ran"])
            self.assertIsNone(summary["task_inventory"])
            self.assertIn("managed_device_scaffold", summary)
            scaffold = summary["managed_device_scaffold"]
            self.assertEqual(scaffold.get("agp_plugin_version"), "8.2.1")
            self.assertEqual(scaffold.get("smoke_group"), "senkuManagedSmoke")
            self.assertEqual(scaffold.get("configured_device_names"), ["senkuPhoneApi30", "senkuTabletApi30"])
            self.assertEqual(scaffold.get("configured_device_api_levels"), [30, 30])
            self.assertEqual(scaffold.get("configured_device_image_sources"), ["aosp", "aosp"])
            self.assertEqual(scaffold.get("smoke_group_devices"), ["senkuPhoneApi30", "senkuTabletApi30"])
            self.assertEqual(
                scaffold.get("expected_gradle_task_names"),
                summary["expected_gradle_task_names"],
            )
            self.assertEqual(
                scaffold.get("expected_artifact_roots"),
                summary["expected_artifact_roots"],
            )
            configured_devices = scaffold.get("configured_devices", [])
            self.assertEqual(len(configured_devices), 2)
            self.assertEqual(
                configured_devices[0],
                {"name": "senkuPhoneApi30", "api_level": 30, "image_source": "aosp"},
            )
            self.assertEqual(
                configured_devices[1],
                {"name": "senkuTabletApi30", "api_level": 30, "image_source": "aosp"},
            )
            self.assertIn("-Psenku.enableManagedDevices=true", summary["planned_command"])
            self.assertIn(":app:senkuManagedSmoke", summary["planned_command"])
            self.assertFalse(summary["would_launch_emulators"])
            self.assertFalse(summary["managed_devices_launched"])
            self.assertFalse(summary["acceptance_evidence"])
            self.assertEqual(summary["primary_evidence"], "fixed_four_emulator_matrix")
            self.assertIn("fixed four-emulator evidence remains primary", summary["stop_line"])
            self.assertTrue((output_dir / "summary.md").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_dry_run_can_ingest_saved_task_inventory_without_probe(self):
        output_dir = Path(tempfile.mkdtemp(prefix="managed_device_smoke_inventory_"))
        try:
            inventory_path = output_dir / "tasks.txt"
            inventory_path.write_text(
                "\n".join(
                    [
                        "> Task :app:tasks",
                        "",
                        "Android tasks",
                        "-------------",
                        "senkuPhoneApi30DebugAndroidTest - Installs and runs instrumentation tests on senkuPhoneApi30.",
                        "senkuTabletApi30DebugAndroidTest - Installs and runs instrumentation tests on senkuTabletApi30.",
                        "senkuManagedSmokeGroupDebugAndroidTest - Installs and runs instrumentation tests on a group.",
                        "unrelatedDebugAndroidTest - Existing unrelated task should remain observed but not expected.",
                    ]
                ),
                encoding="utf-8",
            )

            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir / "out"),
                    "-DryRun",
                    "-TaskInventoryPath",
                    str(inventory_path),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            summary = json.loads((output_dir / "out" / "summary.json").read_text(encoding="utf-8-sig"))
            self.assertEqual(summary["status"], "dry_run_only")
            self.assertTrue(summary["non_acceptance_evidence"])
            self.assertFalse(summary["acceptance_evidence"])
            self.assertEqual(summary["task_inventory_source"], "path")
            self.assertFalse(summary["task_inventory_probe_ran"])
            self.assertEqual(
                summary["expected_gradle_task_names"],
                [
                    ":app:senkuPhoneApi30DebugAndroidTest",
                    ":app:senkuTabletApi30DebugAndroidTest",
                    ":app:senkuManagedSmokeGroupDebugAndroidTest",
                ],
            )
            self.assertEqual(
                summary["observed_gradle_task_names"],
                [
                    ":app:senkuPhoneApi30DebugAndroidTest",
                    ":app:senkuTabletApi30DebugAndroidTest",
                    ":app:senkuManagedSmokeGroupDebugAndroidTest",
                    ":app:unrelatedDebugAndroidTest",
                ],
            )
            self.assertEqual(
                summary["observed_expected_gradle_task_names"],
                summary["expected_gradle_task_names"],
            )
            self.assertEqual(summary["task_inventory"]["source"], "path")
            self.assertFalse(summary["task_inventory"]["gradle_invoked"])
            self.assertEqual(
                summary["task_inventory"]["observed_gradle_task_names"],
                summary["observed_gradle_task_names"],
            )
            self.assertIn("tasks.txt", summary["task_inventory"]["source_path"])
            summary_md = (output_dir / "out" / "summary.md").read_text(encoding="utf-8-sig")
            self.assertIn("- planned_result_evidence_schema: planned_not_collected v1", summary_md)
            self.assertIn(
                "- observed_gradle_task_names: :app:senkuPhoneApi30DebugAndroidTest, "
                ":app:senkuTabletApi30DebugAndroidTest, "
                ":app:senkuManagedSmokeGroupDebugAndroidTest, "
                ":app:unrelatedDebugAndroidTest",
                summary_md,
            )
            self.assertIn(
                "- observed_expected_gradle_task_names: :app:senkuPhoneApi30DebugAndroidTest, "
                ":app:senkuTabletApi30DebugAndroidTest, "
                ":app:senkuManagedSmokeGroupDebugAndroidTest",
                summary_md,
            )
            self.assertIn("- task_inventory_source: path", summary_md)
            self.assertIn("- task_inventory_probe_ran: False", summary_md)
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)

    def test_without_dry_run_fails_before_writing_artifacts(self):
        output_dir = Path(tempfile.mkdtemp(prefix="managed_device_smoke_no_dry_run_"))
        try:
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-OutputDir",
                    str(output_dir),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertNotEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("dry-run-only", result.stderr + result.stdout)
            self.assertFalse((output_dir / "summary.json").exists())
        finally:
            shutil.rmtree(output_dir, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()

import hashlib
import json
import subprocess
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_litert_readiness_matrix.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"
SUMMARY_VALIDATOR_SCRIPT = REPO_ROOT / "scripts" / "validate_android_litert_readiness_summary.py"
VALIDATION_PYTHON = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"


class AndroidLiteRtReadinessMatrixContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

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
                "scripts\\run_android_litert_readiness_matrix.ps1",
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

    def test_script_declares_summary_markdown_output(self):
        self.assertIn('$summaryMarkdownPath = Join-Path $resolvedOutputDir "summary.md"', self.script)
        self.assertIn("function New-LiteRtReadinessMarkdown", self.script)

    def test_dry_run_writes_readiness_summary_without_adb_requirement(self):
        payload = b"tiny litert placeholder\n"
        expected_hash = hashlib.sha256(payload).hexdigest()

        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            model_path = temp_path / "tiny.task"
            output_dir = temp_path / "out"
            model_path.write_bytes(payload)

            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-DryRun",
                    "-OutputDir",
                    str(output_dir),
                    "-ModelPath",
                    str(model_path),
                    "-PackageName",
                    "com.example.senku",
                    "-Backend",
                    "litert",
                    "-RequestMode",
                    "single_prompt_smoke",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            self.assertIn("no adb", result.stdout.lower())
            self.assertTrue((output_dir / "summary.json").exists())
            self.assertTrue((output_dir / "summary.md").exists())

            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8-sig"))
            summary_markdown = (output_dir / "summary.md").read_text(encoding="utf-8-sig")

            validator = subprocess.run(
                [
                    str(VALIDATION_PYTHON),
                    str(SUMMARY_VALIDATOR_SCRIPT),
                    str(output_dir / "summary.json"),
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

        self.assertTrue(summary["non_acceptance_evidence"])
        self.assertFalse(summary["acceptance_evidence"])
        self.assertTrue(summary["dry_run"])
        self.assertEqual(summary["status"], "dry_run_only")
        self.assertEqual(summary["real_run_status"], "not_implemented")
        self.assertEqual(
            summary["stop_line"],
            "STOP: LiteRT readiness dry run is non-acceptance evidence only; fixed four-emulator evidence remains primary.",
        )
        self.assertEqual(summary["primary_evidence"], "fixed_four_emulator_matrix")
        self.assertEqual(summary["comparison_baseline"], "fixed_four_emulator_matrix")
        self.assertEqual(summary["runtime_evidence"], "none_dry_run_only")
        self.assertEqual(summary["package_name"], "com.example.senku")
        self.assertEqual(summary["model"]["path"], str(model_path))
        self.assertTrue(summary["model"]["exists"])
        self.assertEqual(summary["model"]["name"], "tiny.task")
        self.assertEqual(summary["model"]["bytes"], len(payload))
        self.assertEqual(summary["model"]["sha256"], expected_hash)
        self.assertEqual(summary["model_identity"]["source"], "dry_run_model_path")
        self.assertEqual(summary["model_identity"]["path"], str(model_path))
        self.assertTrue(summary["model_identity"]["exists"])
        self.assertEqual(summary["model_identity"]["name"], "tiny.task")
        self.assertEqual(summary["model_identity"]["bytes"], len(payload))
        self.assertEqual(summary["model_identity"]["sha256"], expected_hash)
        self.assertEqual(
            summary["app_private_target"]["path"],
            "/data/user/0/com.example.senku/files/models/tiny.task",
        )
        self.assertFalse(summary["data_free_space_posture"]["adb_required_in_dry_run"])
        self.assertFalse(summary["data_free_space_posture"]["adb_queried"])
        self.assertEqual(
            summary["data_free_space_posture"]["required_bytes"],
            len(payload) * 2 + 67108864,
        )
        self.assertEqual(summary["backend"]["name"], "litert")
        self.assertEqual(summary["backend"]["package"], "com.example.senku")
        self.assertEqual(summary["backend"]["readiness_matrix"], "fixed_four_emulator")
        self.assertEqual(summary["backend"]["real_run_status"], "not_implemented")
        self.assertFalse(summary["backend"]["adb_required_in_dry_run"])
        self.assertEqual(summary["request"]["mode"], "single_prompt_smoke")
        self.assertEqual(summary["request"]["backend"], "litert")
        self.assertEqual(summary["request"]["package"], "com.example.senku")
        self.assertEqual(summary["request"]["real_run_status"], "not_implemented")
        self.assertFalse(summary["request"]["device_required_in_dry_run"])
        self.assertEqual(summary["request"]["prompt"], "LiteRT readiness placeholder prompt.")
        self.assertIn("backend request/response timing", summary["request"]["expected_artifacts"])
        self.assertEqual(summary["runtime_readiness"]["status"], "not_captured_dry_run")
        self.assertEqual(summary["runtime_readiness"]["real_run_status"], "not_implemented")
        self.assertFalse(summary["runtime_readiness"]["acceptance_evidence"])
        self.assertFalse(summary["runtime_readiness"]["device_required_in_dry_run"])
        self.assertEqual(summary["runtime_readiness"]["model_bytes"], len(payload))
        self.assertEqual(summary["runtime_readiness"]["model_sha256"], expected_hash)
        self.assertEqual(
            summary["runtime_readiness"]["app_private_path"],
            "/data/user/0/com.example.senku/files/models/tiny.task",
        )
        self.assertEqual(summary["runtime_readiness"]["backend_requested"], "litert")
        self.assertEqual(summary["runtime_readiness"]["backend_actual"], "")
        self.assertIsNone(summary["runtime_readiness"]["init_timing_ms"])
        self.assertIsNone(summary["runtime_readiness"]["first_response_timing_ms"])
        self.assertEqual(summary["runtime_readiness"]["native_log_excerpt"], "")
        self.assertEqual(summary["runtime_readiness"]["native_log_sha256"], "")
        self.assertEqual(summary["runtime_readiness"]["cpu_fallback"], "not_observed_dry_run")
        self.assertEqual(summary["runtime_readiness"]["gpu_fallback"], "not_observed_dry_run")
        self.assertEqual(summary["runtime_readiness"]["npu_fallback"], "not_observed_dry_run")
        self.assertEqual(summary["logcat_extraction_plan"]["status"], "planned_for_real_run")
        self.assertEqual(summary["logcat_extraction_plan"]["real_run_status"], "not_implemented")
        self.assertFalse(summary["logcat_extraction_plan"]["adb_required_in_dry_run"])
        self.assertTrue(summary["logcat_extraction_plan"]["clear_before_run"])
        self.assertTrue(summary["logcat_extraction_plan"]["capture_after_run"])
        self.assertIn("logcat", summary["logcat_extraction_plan"]["command"])
        self.assertEqual(
            summary["logcat_extraction_plan"]["artifact"],
            "logcat_litert_readiness_<serial>.txt",
        )
        self.assertIn("LiteRT:D", summary["logcat_extraction_plan"]["extraction_filters"])
        self.assertIn("fixed four-emulator posture matrix", summary["fixed_four_emulator_stop_line"])

        self.assertIn("# LiteRT Readiness Dry Run", summary_markdown)
        self.assertIn("Real run status: not_implemented", summary_markdown)
        self.assertIn("Non-acceptance evidence: true", summary_markdown)
        self.assertIn("Acceptance evidence: false", summary_markdown)
        self.assertIn("Primary evidence: fixed_four_emulator_matrix", summary_markdown)
        self.assertIn("STOP: LiteRT readiness dry run is non-acceptance evidence only", summary_markdown)
        self.assertIn("- Name: tiny.task", summary_markdown)
        self.assertIn(f"- Bytes: {len(payload)}", summary_markdown)
        self.assertIn(f"- SHA-256: {expected_hash}", summary_markdown)
        self.assertIn("- Identity source: dry_run_model_path", summary_markdown)
        self.assertIn("- Backend: litert", summary_markdown)
        self.assertIn("- Package: com.example.senku", summary_markdown)
        self.assertIn("- Backend real run status: not_implemented", summary_markdown)
        self.assertIn("- Backend ADB required in dry run: false", summary_markdown)
        self.assertIn("- Request mode: single_prompt_smoke", summary_markdown)
        self.assertIn("- Request backend: litert", summary_markdown)
        self.assertIn("- Request package: com.example.senku", summary_markdown)
        self.assertIn("- Request real run status: not_implemented", summary_markdown)
        self.assertIn("- Request device required in dry run: false", summary_markdown)
        self.assertIn("## Runtime readiness", summary_markdown)
        self.assertIn("- Status: not_captured_dry_run", summary_markdown)
        self.assertIn("- Model bytes: " + str(len(payload)), summary_markdown)
        self.assertIn(f"- Model SHA-256: {expected_hash}", summary_markdown)
        self.assertIn("- App-private path: /data/user/0/com.example.senku/files/models/tiny.task", summary_markdown)
        self.assertIn("- Backend requested: litert", summary_markdown)
        self.assertIn("- Backend actual: n/a", summary_markdown)
        self.assertIn("- Init timing ms: n/a", summary_markdown)
        self.assertIn("- First response timing ms: n/a", summary_markdown)
        self.assertIn("- Native log excerpt: n/a", summary_markdown)
        self.assertIn("- Native log SHA-256: n/a", summary_markdown)
        self.assertIn("- CPU fallback: not_observed_dry_run", summary_markdown)
        self.assertIn("- Required bytes: " + str(len(payload) * 2 + 67108864), summary_markdown)
        self.assertIn("- Status: planned_for_real_run", summary_markdown)
        self.assertIn("- ADB required in dry run: false", summary_markdown)
        self.assertIn("adb -s <serial> logcat -d -v time", summary_markdown)
        self.assertTrue(summary_markdown.rstrip().endswith(summary["fixed_four_emulator_stop_line"]))

        self.assertEqual(validator.returncode, 0, validator.stderr + validator.stdout)
        self.assertIn("android_litert_readiness_summary: ok", validator.stdout)


if __name__ == "__main__":
    unittest.main()

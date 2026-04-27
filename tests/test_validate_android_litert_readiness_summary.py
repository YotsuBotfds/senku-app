import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_litert_readiness_summary import validate_litert_readiness_summary


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_litert_readiness_summary.py"


def make_summary() -> dict:
    return {
        "status": "dry_run_only",
        "real_run_status": "not_implemented",
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "dry_run": True,
        "stop_line": (
            "STOP: LiteRT readiness dry run is non-acceptance evidence only; "
            "fixed four-emulator evidence remains primary."
        ),
        "primary_evidence": "fixed_four_emulator_matrix",
        "comparison_baseline": "fixed_four_emulator_matrix",
        "runtime_evidence": "none_dry_run_only",
        "generated_utc": "2026-04-27T12:00:00.0000000Z",
        "package_name": "com.example.senku",
        "model": {
            "path": "C:\\tmp\\tiny.task",
            "exists": True,
            "name": "tiny.task",
            "bytes": 22,
            "sha256": "a" * 64,
        },
        "model_identity": {
            "source": "dry_run_model_path",
            "path": "C:\\tmp\\tiny.task",
            "name": "tiny.task",
            "bytes": 22,
            "sha256": "a" * 64,
            "exists": True,
        },
        "app_private_target": {
            "directory": "/data/user/0/com.example.senku/files/models",
            "path": "/data/user/0/com.example.senku/files/models/tiny.task",
        },
        "data_free_space_posture": {
            "adb_required_in_dry_run": False,
            "adb_queried": False,
            "check": "real run must verify /data free bytes before staging",
            "required_bytes": 67108908,
            "required_rule": "model_bytes * 2 + 67108864 when model exists",
            "skip_allowed_for_acceptance": False,
        },
        "backend": {
            "name": "litert",
            "package": "com.example.senku",
            "readiness_matrix": "fixed_four_emulator",
            "real_run_status": "not_implemented",
            "adb_required_in_dry_run": False,
        },
        "request": {
            "mode": "single_prompt_smoke",
            "prompt": "LiteRT readiness placeholder prompt.",
            "backend": "litert",
            "package": "com.example.senku",
            "real_run_status": "not_implemented",
            "device_required_in_dry_run": False,
            "expected_artifacts": [
                "summary.json",
                "logcat excerpt",
                "backend request/response timing",
            ],
        },
        "runtime_readiness": {
            "status": "not_captured_dry_run",
            "real_run_status": "not_implemented",
            "acceptance_evidence": False,
            "device_required_in_dry_run": False,
            "model_bytes": 22,
            "model_sha256": "a" * 64,
            "app_private_path": "/data/user/0/com.example.senku/files/models/tiny.task",
            "backend_requested": "litert",
            "backend_actual": "",
            "init_timing_ms": None,
            "first_response_timing_ms": None,
            "native_log_excerpt": "",
            "native_log_sha256": "",
            "cpu_fallback": "not_observed_dry_run",
            "gpu_fallback": "not_observed_dry_run",
            "npu_fallback": "not_observed_dry_run",
        },
        "logcat_extraction_plan": {
            "status": "planned_for_real_run",
            "real_run_status": "not_implemented",
            "adb_required_in_dry_run": False,
            "clear_before_run": True,
            "capture_after_run": True,
            "command": "adb -s <serial> logcat -d -v time Senku:D LiteRT:D AndroidRuntime:E *:S",
            "artifact": "logcat_litert_readiness_<serial>.txt",
            "extraction_filters": ["Senku:D", "LiteRT:D", "AndroidRuntime:E", "*:S"],
        },
        "fixed_four_emulator_stop_line": (
            "fixed four-emulator posture matrix: 5556 phone portrait; "
            "5560 phone landscape; 5554 tablet portrait; 5558 tablet landscape"
        ),
        "output_dir": "C:\\tmp\\out",
    }


class ValidateAndroidLiteRtReadinessSummaryTests(unittest.TestCase):
    def write_summary(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "summary.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_litert_readiness_summary_passes(self):
        data, errors = validate_litert_readiness_summary(self.write_summary(make_summary()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["runtime_evidence"], "none_dry_run_only")

    def test_absent_model_summary_passes_with_null_required_bytes(self):
        summary = make_summary()
        summary["model"].update({"exists": False, "name": "", "bytes": None, "sha256": ""})
        summary["model_identity"].update({"exists": False, "name": "", "bytes": None, "sha256": ""})
        summary["app_private_target"]["path"] = "/data/user/0/com.example.senku/files/models/<model-file>"
        summary["runtime_readiness"].update(
            {
                "model_bytes": None,
                "model_sha256": "",
                "app_private_path": "/data/user/0/com.example.senku/files/models/<model-file>",
            }
        )
        summary["data_free_space_posture"]["required_bytes"] = None

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertEqual(errors, [])

    def test_rejects_acceptance_or_real_run_posture(self):
        summary = make_summary()
        summary["status"] = "pass"
        summary["dry_run"] = False
        summary["real_run_status"] = "pass"
        summary["runtime_evidence"] = "litert_runtime"
        summary["non_acceptance_evidence"] = False
        summary["acceptance_evidence"] = True

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertIn("expected root.status to be 'dry_run_only'", errors)
        self.assertIn("expected root.dry_run to be True", errors)
        self.assertIn("expected root.real_run_status to be 'not_implemented'", errors)
        self.assertIn("expected root.runtime_evidence to be 'none_dry_run_only'", errors)
        self.assertIn("expected root.non_acceptance_evidence to be True", errors)
        self.assertIn("expected root.acceptance_evidence to be False", errors)

    def test_rejects_wrong_primary_and_comparison_evidence(self):
        summary = make_summary()
        summary["primary_evidence"] = "managed_device"
        summary["comparison_baseline"] = "managed_device"

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertIn("expected root.primary_evidence to be 'fixed_four_emulator_matrix'", errors)
        self.assertIn("expected root.comparison_baseline to be 'fixed_four_emulator_matrix'", errors)

    def test_rejects_model_identity_drift(self):
        summary = make_summary()
        summary["model_identity"]["sha256"] = "b" * 64
        summary["model_identity"]["source"] = "runtime_probe"

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertIn("expected model_identity.source to be 'dry_run_model_path'", errors)
        self.assertIn("expected model_identity.sha256 to match model.sha256", errors)

    def test_rejects_backend_request_and_logcat_drift(self):
        summary = make_summary()
        summary["backend"]["real_run_status"] = "ready"
        summary["backend"]["adb_required_in_dry_run"] = True
        summary["request"]["backend"] = "llamacpp"
        summary["request"]["package"] = "com.other"
        summary["request"]["device_required_in_dry_run"] = True
        summary["logcat_extraction_plan"]["status"] = "captured"
        summary["logcat_extraction_plan"]["extraction_filters"] = ["Senku:D"]

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertIn("expected backend.real_run_status to be 'not_implemented'", errors)
        self.assertIn("expected backend.adb_required_in_dry_run to be False", errors)
        self.assertIn("expected request.package to match root.package_name", errors)
        self.assertIn("expected request.backend to match backend.name", errors)
        self.assertIn("expected request.device_required_in_dry_run to be False", errors)
        self.assertIn("expected logcat_extraction_plan.status to be 'planned_for_real_run'", errors)
        self.assertIn("missing logcat_extraction_plan.extraction_filters LiteRT:D", errors)

    def test_rejects_runtime_readiness_acceptance_or_observed_fields(self):
        summary = make_summary()
        summary["runtime_readiness"].update(
            {
                "status": "captured",
                "real_run_status": "pass",
                "acceptance_evidence": True,
                "device_required_in_dry_run": True,
                "backend_actual": "gpu",
                "init_timing_ms": 123,
                "first_response_timing_ms": 456,
                "native_log_excerpt": "LiteRT initialized",
                "native_log_sha256": "b" * 64,
                "cpu_fallback": "cpu",
            }
        )

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertIn("expected runtime_readiness.status to be 'not_captured_dry_run'", errors)
        self.assertIn("expected runtime_readiness.real_run_status to be 'not_implemented'", errors)
        self.assertIn("expected runtime_readiness.acceptance_evidence to be False", errors)
        self.assertIn("expected runtime_readiness.device_required_in_dry_run to be False", errors)
        self.assertIn("expected runtime_readiness.backend_actual to be ''", errors)
        self.assertIn("expected runtime_readiness.init_timing_ms to be None", errors)
        self.assertIn("expected runtime_readiness.first_response_timing_ms to be None", errors)
        self.assertIn("expected runtime_readiness.native_log_excerpt to be ''", errors)
        self.assertIn("expected runtime_readiness.native_log_sha256 to be ''", errors)
        self.assertIn(
            "expected runtime_readiness.cpu_fallback to be 'not_observed_dry_run' or 'planned_for_real_run'",
            errors,
        )

    def test_rejects_runtime_readiness_metadata_drift(self):
        summary = make_summary()
        summary["runtime_readiness"]["model_bytes"] = 1
        summary["runtime_readiness"]["model_sha256"] = "b" * 64
        summary["runtime_readiness"]["app_private_path"] = "/data/user/0/com.example.senku/files/models/other.task"
        summary["runtime_readiness"]["backend_requested"] = "cpu"

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertIn("expected runtime_readiness.model_bytes to match model.bytes", errors)
        self.assertIn("expected runtime_readiness.model_sha256 to match model.sha256", errors)
        self.assertIn("expected runtime_readiness.app_private_path to match app_private_target.path", errors)
        self.assertIn("expected runtime_readiness.backend_requested to match backend.name", errors)

    def test_rejects_bad_data_free_space_posture(self):
        summary = make_summary()
        summary["data_free_space_posture"]["adb_queried"] = True
        summary["data_free_space_posture"]["required_bytes"] = 1
        summary["data_free_space_posture"]["skip_allowed_for_acceptance"] = True

        _, errors = validate_litert_readiness_summary(self.write_summary(summary))

        self.assertIn("expected data_free_space_posture.adb_queried to be False", errors)
        self.assertIn("expected data_free_space_posture.skip_allowed_for_acceptance to be False", errors)
        self.assertIn(
            "expected data_free_space_posture.required_bytes to equal "
            "model.bytes * 2 + 67108864 (67108908)",
            errors,
        )

    def test_cli_reports_failure_without_device_work(self):
        summary = make_summary()
        del summary["logcat_extraction_plan"]["command"]
        path = self.write_summary(summary)
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: missing logcat_extraction_plan.command", result.stdout)


if __name__ == "__main__":
    unittest.main()

import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_tooling_version_manifest import validate_manifest


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_tooling_version_manifest.py"


def make_manifest() -> dict:
    return {
        "manifest_kind": "android_tooling_version_manifest",
        "schema_version": 1,
        "generated_at_utc": "2026-04-27T12:00:00+00:00",
        "repo_root": "C:\\repo",
        "metadata_only": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "inputs": {
            "wrapper_properties": "android-app/gradle/wrapper/gradle-wrapper.properties",
            "build_files": [
                "android-app/build.gradle",
                "android-app/app/build.gradle",
                "android-app/settings.gradle",
            ],
        },
        "summary": {
            "metadata_only": True,
            "acceptance_evidence": False,
            "gradle_wrapper": {
                "count": 1,
                "version": "8.7",
                "has_sha256": False,
            },
            "android_gradle_plugin": {
                "count": 1,
                "versions": ["8.6.1"],
            },
            "kotlin_plugins": {
                "count": 1,
                "versions": ["2.0.21"],
            },
            "androidx_test": {
                "count": 1,
                "coordinates": ["androidx.test:runner:1.6.2"],
            },
            "orchestrator": {
                "count": 1,
                "coordinates": ["androidx.test:orchestrator:1.5.1"],
            },
            "litert_lm": {
                "count": 1,
                "coordinates": ["com.google.ai.edge.litertlm:litert-lm:1.0.0"],
            },
            "sdk_path_hints": {
                "set_count": 1,
                "existing_count": 0,
            },
            "host_tools": {
                "probed": False,
                "adb_version": None,
                "emulator_version": None,
            },
        },
        "gradle_wrapper": {
            "available": True,
            "distribution_url": "https://services.gradle.org/distributions/gradle-8.7-bin.zip",
            "distribution_sha256_sum": None,
            "distribution_version": "8.7",
        },
        "plugins": [
            {
                "id": "com.android.application",
                "version": "8.6.1",
                "source": "android-app/build.gradle",
            },
            {
                "id": "org.jetbrains.kotlin.android",
                "version": "2.0.21",
                "source": "android-app/build.gradle",
            },
        ],
        "android_gradle_plugin": [
            {
                "id": "com.android.application",
                "version": "8.6.1",
                "source": "android-app/build.gradle",
            }
        ],
        "kotlin_plugins": [
            {
                "id": "org.jetbrains.kotlin.android",
                "version": "2.0.21",
                "source": "android-app/build.gradle",
            }
        ],
        "dependencies": {
            "androidx_test": [
                {
                    "group": "androidx.test",
                    "name": "runner",
                    "version": "1.6.2",
                    "coordinate": "androidx.test:runner:1.6.2",
                    "source": "android-app/app/build.gradle",
                }
            ],
            "orchestrator": [
                {
                    "group": "androidx.test",
                    "name": "orchestrator",
                    "version": "1.5.1",
                    "coordinate": "androidx.test:orchestrator:1.5.1",
                    "source": "android-app/app/build.gradle",
                }
            ],
            "litert_lm": [
                {
                    "group": "com.google.ai.edge.litertlm",
                    "name": "litert-lm",
                    "version": "1.0.0",
                    "coordinate": "com.google.ai.edge.litertlm:litert-lm:1.0.0",
                    "source": "android-app/app/build.gradle",
                }
            ],
        },
        "sdk_path_hints": {
            "ANDROID_HOME": None,
            "ANDROID_SDK_ROOT": "C:\\Android\\Sdk",
            "ANDROID_AVD_HOME": None,
            "existing": [],
        },
        "host_tools": {
            "probed": False,
            "adb": {
                "available": False,
                "reason": "probe_disabled",
            },
            "emulator": {
                "available": False,
                "reason": "probe_disabled",
            },
        },
        "extra_future_metadata_field": {"kept": True},
    }


class ValidateAndroidToolingVersionManifestTests(unittest.TestCase):
    def write_manifest(self, payload: dict) -> Path:
        temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(temp_dir.cleanup)
        path = Path(temp_dir.name) / "android_tooling_version_manifest.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_valid_manifest_passes_with_disabled_tool_probes(self):
        data, errors = validate_manifest(self.write_manifest(make_manifest()))

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertTrue(data["metadata_only"])

    def test_valid_manifest_passes_with_tool_probe_output_shape(self):
        manifest = make_manifest()
        manifest["host_tools"] = {
            "probed": True,
            "adb": {
                "available": True,
                "command": ["adb", "version"],
                "path": "C:\\Android\\Sdk\\platform-tools\\adb.exe",
                "returncode": 0,
                "version": "1.0.41",
                "output": "Android Debug Bridge version 1.0.41",
            },
            "emulator": {
                "available": False,
                "command": ["emulator", "-version"],
                "reason": "not_found",
            },
        }

        _, errors = validate_manifest(self.write_manifest(manifest))

        self.assertEqual(errors, [])

    def test_missing_gradle_and_dependency_shape_fails(self):
        manifest = make_manifest()
        del manifest["gradle_wrapper"]["distribution_version"]
        del manifest["android_gradle_plugin"][0]["version"]
        del manifest["dependencies"]["orchestrator"]
        manifest["sdk_path_hints"]["existing"] = [""]

        _, errors = validate_manifest(self.write_manifest(manifest))

        self.assertIn("missing gradle_wrapper.distribution_version", errors)
        self.assertIn("missing android_gradle_plugin[0].version", errors)
        self.assertIn("missing dependencies.orchestrator", errors)
        self.assertIn("expected sdk_path_hints.existing[0] to be non-empty str", errors)

    def test_metadata_only_posture_is_required(self):
        manifest = make_manifest()
        manifest["metadata_only"] = False
        manifest["non_acceptance_evidence"] = False
        manifest["summary"]["metadata_only"] = False

        _, errors = validate_manifest(self.write_manifest(manifest))

        self.assertIn("expected root.metadata_only to be true", errors)
        self.assertIn("expected root.non_acceptance_evidence to be true", errors)
        self.assertIn("expected summary.metadata_only to be true", errors)

    def test_acceptance_evidence_is_rejected_recursively(self):
        manifest = make_manifest()
        manifest["acceptance_evidence"] = True
        manifest["summary"]["acceptance_evidence"] = True
        manifest["host_tools"]["adb"]["ui_acceptance_evidence"] = True

        _, errors = validate_manifest(self.write_manifest(manifest))

        self.assertIn("expected root.acceptance_evidence to be false", errors)
        self.assertIn("expected summary.acceptance_evidence to be false", errors)
        self.assertIn("expected root.host_tools.adb.ui_acceptance_evidence to be false", errors)

    def test_reviewer_summary_shape_is_required(self):
        manifest = make_manifest()
        del manifest["summary"]["androidx_test"]["count"]
        manifest["summary"]["kotlin_plugins"]["versions"] = [""]
        manifest["summary"]["host_tools"]["adb_version"] = 123

        _, errors = validate_manifest(self.write_manifest(manifest))

        self.assertIn("missing summary.androidx_test.count", errors)
        self.assertIn("expected summary.kotlin_plugins.versions[0] to be non-empty str", errors)
        self.assertIn("expected summary.host_tools.adb_version to be str|NoneType, got int", errors)

    def test_cli_reports_failure_without_device_or_sdk_tools(self):
        manifest = make_manifest()
        manifest["acceptance_evidence"] = True
        path = self.write_manifest(manifest)
        python_path = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"

        result = subprocess.run(
            [str(python_path), str(SCRIPT_PATH), str(path)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 1)
        self.assertIn("ERROR: expected root.acceptance_evidence to be false", result.stdout)


if __name__ == "__main__":
    unittest.main()

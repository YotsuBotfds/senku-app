import json
import subprocess
import sys
import tempfile
import textwrap
import unittest
from datetime import datetime, timezone
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "write_android_tooling_version_manifest.py"

sys.path.insert(0, str(REPO_ROOT / "scripts"))
import write_android_tooling_version_manifest as manifest_script  # noqa: E402


def write_sample_android_repo(root: Path) -> None:
    wrapper = root / "android-app" / "gradle" / "wrapper" / "gradle-wrapper.properties"
    wrapper.parent.mkdir(parents=True)
    wrapper.write_text(
        "\n".join(
            [
                "distributionBase=GRADLE_USER_HOME",
                "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.2.1-bin.zip",
                "distributionSha256Sum=abc123",
            ]
        ),
        encoding="utf-8",
    )
    (root / "android-app").mkdir(exist_ok=True)
    (root / "android-app" / "build.gradle").write_text(
        textwrap.dedent(
            """
            plugins {
                id 'com.android.application' version '8.2.1' apply false
                id 'org.jetbrains.kotlin.android' version '2.2.21' apply false
                id 'org.jetbrains.kotlin.plugin.compose' version '2.2.21' apply false
            }
            """
        ),
        encoding="utf-8",
    )
    app = root / "android-app" / "app"
    app.mkdir()
    (app / "build.gradle").write_text(
        textwrap.dedent(
            """
            plugins {
                id 'com.android.application'
                id 'org.jetbrains.kotlin.android'
            }

            dependencies {
                implementation 'com.google.ai.edge.litertlm:litertlm-android:0.10.0'
                androidTestImplementation 'androidx.test:runner:1.6.2'
                androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
                androidTestUtil 'androidx.test:orchestrator:1.5.1'
            }
            """
        ),
        encoding="utf-8",
    )
    (root / "android-app" / "settings.gradle").write_text("pluginManagement { repositories { google() } }\n", encoding="utf-8")


class WriteAndroidToolingVersionManifestTests(unittest.TestCase):
    def test_build_manifest_records_metadata_only_android_tooling_versions(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            write_sample_android_repo(repo)

            manifest = manifest_script.build_manifest(
                repo,
                probe_tools=False,
                now=datetime(2026, 4, 27, 12, 0, tzinfo=timezone.utc),
            )

        self.assertEqual(manifest["manifest_kind"], "android_tooling_version_manifest")
        self.assertTrue(manifest["metadata_only"])
        self.assertTrue(manifest["non_acceptance_evidence"])
        self.assertFalse(manifest["acceptance_evidence"])
        self.assertEqual(manifest["gradle_wrapper"]["distribution_version"], "8.2.1")
        self.assertEqual(manifest["gradle_wrapper"]["distribution_sha256_sum"], "abc123")
        self.assertIn(
            {"id": "com.android.application", "version": "8.2.1", "source": "android-app/build.gradle"},
            manifest["android_gradle_plugin"],
        )
        self.assertIn(
            {"id": "org.jetbrains.kotlin.android", "version": "2.2.21", "source": "android-app/build.gradle"},
            manifest["kotlin_plugins"],
        )
        self.assertEqual(
            [dep["coordinate"] for dep in manifest["dependencies"]["litert_lm"]],
            ["com.google.ai.edge.litertlm:litertlm-android:0.10.0"],
        )
        self.assertIn(
            "androidx.test:orchestrator:1.5.1",
            [dep["coordinate"] for dep in manifest["dependencies"]["orchestrator"]],
        )
        self.assertEqual(manifest["host_tools"]["adb"]["reason"], "probe_disabled")

    def test_cli_writes_json_and_markdown_outputs(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir) / "repo"
            repo.mkdir()
            write_sample_android_repo(repo)
            json_out = Path(temp_dir) / "out" / "tooling.json"
            md_out = Path(temp_dir) / "out" / "tooling.md"

            result = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT),
                    "--repo-root",
                    str(repo),
                    "--json-out",
                    str(json_out),
                    "--markdown-out",
                    str(md_out),
                    "--no-probe-tools",
                ],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

            self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
            payload = json.loads(json_out.read_text(encoding="utf-8"))
            markdown = md_out.read_text(encoding="utf-8")

        self.assertFalse(payload["acceptance_evidence"])
        self.assertIn("non_acceptance_evidence: `true`", markdown)
        self.assertIn("`com.google.ai.edge.litertlm:litertlm-android:0.10.0`", markdown)
        self.assertIn("- adb: `probe_disabled`", markdown)

    def test_parse_tool_version_prefers_platform_tools_version_line(self):
        output = "\n".join(
            [
                "Android Debug Bridge version 1.0.41",
                "Version 36.0.0-13206524",
                "Installed as C:\\Android\\platform-tools\\adb.exe",
            ]
        )
        self.assertEqual(manifest_script.parse_tool_version(output), "36.0.0-13206524")

    def test_parse_tool_version_handles_emulator_output(self):
        output = "Android emulator version 35.4.9.0 (build_id 13025442)\nCopyright ..."
        self.assertEqual(manifest_script.parse_tool_version(output), "35.4.9.0 (build_id 13025442)")

    def test_missing_repo_files_are_recorded_as_unavailable_not_errors(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            manifest = manifest_script.build_manifest(Path(temp_dir), probe_tools=False)

        self.assertFalse(manifest["gradle_wrapper"]["available"])
        self.assertEqual(manifest["plugins"], [])
        self.assertEqual(manifest["dependencies"]["androidx_test"], [])


if __name__ == "__main__":
    unittest.main()

import hashlib
import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.validate_android_capture_summary import validate_capture_summary
from scripts.write_android_capture_summary import (
    build_capture_summary,
    markdown_for_capture_summary,
    write_capture_summary,
)


REPO_ROOT = Path(__file__).resolve().parents[1]
WRITE_SCRIPT = REPO_ROOT / "scripts" / "write_android_capture_summary.py"
VALIDATE_SCRIPT = REPO_ROOT / "scripts" / "validate_android_capture_summary.py"
PYTHON_PATH = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"


def digest(content: bytes) -> str:
    return hashlib.sha256(content).hexdigest()


class WriteAndroidCaptureSummaryTests(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp_dir.cleanup)
        self.root = Path(self.temp_dir.name)

    def write_file(self, name: str, content: bytes) -> Path:
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(content)
        return path

    def test_builds_summary_with_hashes_and_validates(self):
        screenshot = self.write_file("screen.png", b"png bytes")
        ui_dump = self.write_file("window.xml", b"<hierarchy />")
        logcat = self.write_file("logcat.txt", b"log line\n")
        screenrecord = self.write_file("session.mp4", b"mp4 bytes")
        apk = self.write_file("app.apk", b"apk bytes")
        metadata = self.write_file(
            "installed-pack.json",
            json.dumps(
                {
                    "status": "available",
                    "pack_format": "senku-mobile-pack-v2",
                    "pack_version": 2,
                    "answer_cards": 271,
                }
            ).encode("utf-8"),
        )
        output = self.root / "capture_summary.json"

        summary = build_capture_summary(
            screenshot=str(screenshot),
            ui_dump=str(ui_dump),
            logcat=str(logcat),
            screenrecord=str(screenrecord),
            apk=str(apk),
            installed_pack_metadata=str(metadata),
            serial="emulator-5556",
            role="phone_portrait",
            orientation="portrait",
            platform_tools_version="36.0.0-13206524",
            model_name="gemma-4-e2b-it-litert",
            model_sha256="f" * 64,
        )
        write_capture_summary(output, summary)

        data, errors = validate_capture_summary(output)

        self.assertEqual(errors, [])
        self.assertIsNotNone(data)
        self.assertEqual(data["artifacts"]["screenshot"]["sha256"], digest(b"png bytes"))
        self.assertEqual(data["artifacts"]["ui_dump"]["sha256"], digest(b"<hierarchy />"))
        self.assertEqual(data["artifacts"]["logcat"]["sha256"], digest(b"log line\n"))
        self.assertEqual(data["artifacts"]["screenrecord"]["sha256"], digest(b"mp4 bytes"))
        self.assertEqual(data["apk_sha256"], digest(b"apk bytes"))
        self.assertEqual(
            data["installed_pack_metadata"]["source_sha256"],
            digest(metadata.read_bytes()),
        )
        self.assertEqual(data["installed_pack_metadata"]["answer_cards"], 271)
        self.assertEqual(data["model_identity"]["name"], "gemma-4-e2b-it-litert")
        self.assertEqual(data["migration_metadata"]["status"], "capture_only")
        self.assertEqual(data["migration_metadata"]["capture_summary_schema_version"], 1)
        self.assertFalse(data["migration_metadata"]["android_layout_change"])
        self.assertFalse(data["migration_metadata"]["large_data_lane_change"])
        self.assertFalse(data["migration_metadata"]["reindex_required"])

    def test_markdown_summary_includes_reviewer_fields_without_changing_json(self):
        screenshot = self.write_file("screen.png", b"png bytes")
        ui_dump = self.write_file("window.xml", b"<hierarchy />")
        logcat = self.write_file("logcat.txt", b"log line\n")
        screenrecord = self.write_file("session.mp4", b"mp4 bytes")
        apk = self.write_file("app.apk", b"apk bytes")
        metadata = self.write_file(
            "installed-pack.json",
            json.dumps(
                {
                    "status": "available",
                    "pack_format": "senku-mobile-pack-v2",
                    "pack_version": 2,
                    "answer_cards": 6,
                }
            ).encode("utf-8"),
        )

        summary = build_capture_summary(
            screenshot=str(screenshot),
            ui_dump=str(ui_dump),
            logcat=str(logcat),
            screenrecord=str(screenrecord),
            apk=str(apk),
            installed_pack_metadata=str(metadata),
            serial="emulator-5556",
            role="phone_portrait",
            orientation="portrait",
            platform_tools_version="36.0.0-13206524",
            model_name="gemma-4-e2b-it-litert",
            model_sha256="f" * 64,
            package_data_cleared_before_capture=False,
            package_data_restored_after_capture=True,
            package_data_description="restored from harness snapshot",
        )
        before_json = json.dumps(summary, indent=2, sort_keys=True)

        markdown = markdown_for_capture_summary(summary)
        after_json = json.dumps(summary, indent=2, sort_keys=True)

        self.assertEqual(after_json, before_json)
        self.assertIn("- serial: `emulator-5556`", markdown)
        self.assertIn("- role: `phone_portrait`", markdown)
        self.assertIn("- orientation: `portrait`", markdown)
        self.assertIn("- non_acceptance_evidence: `true`", markdown)
        self.assertIn("- acceptance_evidence: `false`", markdown)
        self.assertIn(f"- screenshot: `{digest(b'png bytes')}`", markdown)
        self.assertIn(f"- ui_dump: `{digest(b'<hierarchy />')}`", markdown)
        self.assertIn(f"- logcat: `{digest(b'log line\n')}`", markdown)
        self.assertIn(f"- screenrecord: `{digest(b'mp4 bytes')}`", markdown)
        self.assertIn(f"- sha256: `{digest(b'apk bytes')}`", markdown)
        self.assertIn("- name: `gemma-4-e2b-it-litert`", markdown)
        self.assertIn("- status: `available`", markdown)
        self.assertIn("- pack_format: `senku-mobile-pack-v2`", markdown)
        self.assertIn("- pack_version: `2`", markdown)
        self.assertIn("- status: `capture_only`", markdown)
        self.assertIn("- capture_summary_schema_version: `1`", markdown)
        self.assertIn("- android_layout_change: `false`", markdown)
        self.assertIn("- large_data_lane_change: `false`", markdown)
        self.assertIn("- reindex_required: `false`", markdown)
        self.assertIn("- cleared_before_capture: `false`", markdown)
        self.assertIn("- restored_after_capture: `true`", markdown)
        self.assertIn("- description: `restored from harness snapshot`", markdown)

    def test_cli_writes_summary_that_validator_accepts_without_optional_files(self):
        screenshot = self.write_file("screen.png", b"png bytes")
        ui_dump = self.write_file("window.xml", b"<hierarchy />")
        logcat = self.write_file("logcat.txt", b"log line\n")
        output = self.root / "nested" / "capture_summary.json"
        markdown_output = self.root / "nested" / "capture_summary.md"

        write_result = subprocess.run(
            [
                str(PYTHON_PATH),
                str(WRITE_SCRIPT),
                "--output",
                str(output),
                "--markdown-out",
                str(markdown_output),
                "--screenshot",
                str(screenshot),
                "--ui-dump",
                str(ui_dump),
                "--logcat",
                str(logcat),
                "--serial",
                "device-1234",
                "--role",
                "tablet_landscape",
                "--orientation",
                "landscape",
                "--platform-tools-version",
                "36.0.0-13206524",
                "--model-name",
                "test-model",
                "--model-sha256",
                "a" * 64,
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(write_result.returncode, 0, write_result.stderr)
        validate_result = subprocess.run(
            [str(PYTHON_PATH), str(VALIDATE_SCRIPT), str(output)],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        data = json.loads(output.read_text(encoding="utf-8"))
        markdown = markdown_output.read_text(encoding="utf-8")

        self.assertEqual(validate_result.returncode, 0, validate_result.stdout)
        self.assertIn("android_capture_summary: ok", validate_result.stdout)
        self.assertNotIn("screenrecord", data["artifacts"])
        self.assertEqual(data["apk_sha256"], "not_provided")
        self.assertEqual(data["installed_pack_metadata"]["status"], "not_provided")
        self.assertEqual(data["migration_metadata"]["status"], "capture_only")
        self.assertIn("- serial: `device-1234`", markdown)
        self.assertIn("- role: `tablet_landscape`", markdown)
        self.assertIn("- orientation: `landscape`", markdown)
        self.assertIn("- screenshot: `", markdown)
        self.assertNotIn("- screenrecord:", markdown)
        self.assertIn("- sha256: `not_provided`", markdown)
        self.assertIn("- status: `not_provided`", markdown)
        self.assertIn("- status: `capture_only`", markdown)


if __name__ == "__main__":
    unittest.main()

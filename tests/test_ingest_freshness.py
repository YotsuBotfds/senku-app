import json
import tempfile
import unittest
from pathlib import Path

from ingest_freshness import (
    ABSENT_OR_INVALID,
    FRESH,
    INCOMPLETE_UNTRUSTED,
    STALE,
    collect_guide_file_info,
    evaluate_ingest_freshness,
)


def write_guide(path: Path, guide_id: str, title: str, body: str = "Body.") -> None:
    path.write_text(
        f"""---
id: {guide_id}
slug: {path.stem}
title: {title}
category: utility
description: Test guide.
---

## Body

{body}
""",
        encoding="utf-8",
    )


def write_manifest(path: Path, guides_dir: Path, guide_ids: list[str] | None = None) -> None:
    info = collect_guide_file_info(str(guides_dir))
    selected = guide_ids or sorted(info)
    manifest = {
        guide_id: {
            "guide_id": guide_id,
            "source_file": info[guide_id]["basename"],
            "sha256": info[guide_id]["sha256"],
        }
        for guide_id in selected
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(manifest, indent=2, sort_keys=True), encoding="utf-8")


class IngestFreshnessTests(unittest.TestCase):
    def test_complete_manifest_matching_shas_is_fresh(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = root / "guides"
            guides.mkdir()
            write_guide(guides / "one.md", "GD-001", "One")
            manifest = root / "db" / "ingest_manifest.json"
            write_manifest(manifest, guides)

            report = evaluate_ingest_freshness(
                compendium_dir=str(guides),
                manifest_path=str(manifest),
            )

        self.assertEqual(report.status, FRESH)

    def test_complete_manifest_changed_sha_is_stale(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = root / "guides"
            guides.mkdir()
            guide = guides / "one.md"
            write_guide(guide, "GD-001", "One")
            manifest = root / "db" / "ingest_manifest.json"
            write_manifest(manifest, guides)
            guide.write_text(guide.read_text(encoding="utf-8") + "\nChanged.\n", encoding="utf-8")

            report = evaluate_ingest_freshness(
                compendium_dir=str(guides),
                manifest_path=str(manifest),
            )

        self.assertEqual(report.status, STALE)
        self.assertEqual(report.changed_guide_ids, ("GD-001",))

    def test_changed_guide_is_blocking_until_manifest_sha_refreshes(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = root / "guides"
            guides.mkdir()
            guide = guides / "one.md"
            write_guide(guide, "GD-001", "One")
            manifest = root / "db" / "ingest_manifest.json"
            write_manifest(manifest, guides)
            guide.write_text(guide.read_text(encoding="utf-8") + "\nChanged.\n", encoding="utf-8")

            stale_report = evaluate_ingest_freshness(
                compendium_dir=str(guides),
                manifest_path=str(manifest),
            )
            write_manifest(manifest, guides)
            fresh_report = evaluate_ingest_freshness(
                compendium_dir=str(guides),
                manifest_path=str(manifest),
            )

        self.assertEqual(stale_report.status, STALE)
        self.assertEqual(stale_report.changed_guide_ids, ("GD-001",))
        self.assertTrue(stale_report.is_blocking)
        self.assertEqual(fresh_report.status, FRESH)
        self.assertFalse(fresh_report.is_blocking)

    def test_nearly_complete_manifest_missing_new_guide_is_stale(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = root / "guides"
            guides.mkdir()
            write_guide(guides / "one.md", "GD-001", "One")
            write_guide(guides / "two.md", "GD-002", "Two")
            manifest = root / "db" / "ingest_manifest.json"
            write_manifest(manifest, guides, ["GD-001"])

            report = evaluate_ingest_freshness(
                compendium_dir=str(guides),
                manifest_path=str(manifest),
            )

        self.assertEqual(report.status, STALE)
        self.assertEqual(report.missing_guide_ids, ("GD-002",))

    def test_truncated_manifest_is_incomplete_untrusted(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = root / "guides"
            guides.mkdir()
            for index in range(30):
                write_guide(guides / f"guide-{index}.md", f"GD-{index:03d}", f"Guide {index}")
            manifest = root / "db" / "ingest_manifest.json"
            write_manifest(manifest, guides, ["GD-000", "GD-001"])

            report = evaluate_ingest_freshness(
                compendium_dir=str(guides),
                manifest_path=str(manifest),
            )

        self.assertEqual(report.status, INCOMPLETE_UNTRUSTED)
        self.assertEqual(report.manifest_count, 2)
        self.assertEqual(report.guide_count, 30)

    def test_invalid_manifest_warns_without_blocking(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides = root / "guides"
            guides.mkdir()
            write_guide(guides / "one.md", "GD-001", "One")
            manifest = root / "db" / "ingest_manifest.json"
            manifest.parent.mkdir(parents=True)
            manifest.write_text("{not json", encoding="utf-8")

            report = evaluate_ingest_freshness(
                compendium_dir=str(guides),
                manifest_path=str(manifest),
            )

        self.assertEqual(report.status, ABSENT_OR_INVALID)


if __name__ == "__main__":
    unittest.main()

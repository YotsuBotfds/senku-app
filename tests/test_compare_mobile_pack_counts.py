import contextlib
import io
import json
import sqlite3
import tempfile
import unittest
from contextlib import closing
from pathlib import Path

from scripts.compare_mobile_pack_counts import (
    compare_summaries,
    main,
    report_has_fail_on_mismatch_condition,
    summarize_pack,
)


class CompareMobilePackCountsTests(unittest.TestCase):
    def test_summarize_materialized_pack_counts_optional_tables(self):
        with tempfile.TemporaryDirectory() as tmp:
            pack_dir = Path(tmp)
            db_path = pack_dir / "senku_mobile.sqlite3"
            with closing(sqlite3.connect(db_path)) as conn:
                conn.execute("CREATE TABLE guides (id TEXT)")
                conn.execute("CREATE TABLE answer_cards (id TEXT)")
                conn.executemany("INSERT INTO guides (id) VALUES (?)", [("GD-1",), ("GD-2",)])
                conn.execute("INSERT INTO answer_cards (id) VALUES ('card')")
                conn.commit()
            (pack_dir / "senku_manifest.json").write_text(
                json.dumps({"counts": {"guides": 2, "answer_cards": 1}}),
                encoding="utf-8",
            )

            summary = summarize_pack(pack_dir)

            self.assertEqual(summary["sqlite_status"], "materialized")
            self.assertEqual(summary["sqlite_counts"]["guides"], 2)
            self.assertEqual(summary["sqlite_counts"]["answer_cards"], 1)
            self.assertIn("answer_card_sources", summary["table_errors"])

    def test_lfs_pointer_is_not_opened_as_sqlite(self):
        with tempfile.TemporaryDirectory() as tmp:
            pack_dir = Path(tmp)
            db_path = pack_dir / "senku_mobile.sqlite3"
            sha = "a" * 64
            db_path.write_text(
                "\n".join(
                    [
                        "version https://git-lfs.github.com/spec/v1",
                        f"oid sha256:{sha}",
                        "size 12345",
                    ]
                )
                + "\n",
                encoding="utf-8",
            )
            (pack_dir / "senku_manifest.json").write_text(
                json.dumps(
                    {
                        "counts": {"answer_cards": 6},
                        "files": {"sqlite": {"path": "senku_mobile.sqlite3", "sha256": sha, "bytes": 12345}},
                    }
                ),
                encoding="utf-8",
            )

            summary = summarize_pack(pack_dir)

            self.assertEqual(summary["sqlite_status"], "git_lfs_pointer")
            self.assertEqual(summary["manifest_counts"]["answer_cards"], 6)
            self.assertTrue(summary["sqlite_file"]["pointer_matches_manifest"])
            self.assertEqual(summary["sqlite_counts"], {})

    def test_compare_prefers_sqlite_counts_when_materialized(self):
        baseline = {
            "manifest_counts": {"answer_cards": 6},
            "sqlite_counts": {},
        }
        candidate = {
            "manifest_counts": {"answer_cards": 999, "chunks": 10},
            "sqlite_counts": {"answer_cards": 271},
        }

        report = compare_summaries(baseline, candidate)

        self.assertEqual(
            report["count_deltas"],
            [
                {"name": "answer_cards", "baseline": 6, "candidate": 271, "delta": 265},
                {"name": "chunks", "baseline": None, "candidate": 10, "delta": None},
            ],
        )

    def test_records_manifest_sqlite_internal_drift(self):
        with tempfile.TemporaryDirectory() as tmp:
            pack_dir = Path(tmp)
            db_path = pack_dir / "senku_mobile.sqlite3"
            with closing(sqlite3.connect(db_path)) as conn:
                conn.execute("CREATE TABLE answer_cards (id TEXT)")
                conn.executemany("INSERT INTO answer_cards (id) VALUES (?)", [("one",), ("two",)])
                conn.commit()
            (pack_dir / "senku_manifest.json").write_text(
                json.dumps({"counts": {"answer_cards": 3}}),
                encoding="utf-8",
            )

            summary = summarize_pack(pack_dir)

            self.assertEqual(
                summary["manifest_sqlite_mismatches"],
                [{"name": "answer_cards", "manifest": 3, "sqlite": 2, "delta": -1}],
            )

    def test_fail_on_mismatch_condition_detects_manifest_sqlite_drift(self):
        report = {
            "baseline": {"manifest_sqlite_mismatches": []},
            "candidate": {
                "manifest_sqlite_mismatches": [
                    {"name": "answer_cards", "manifest": 3, "sqlite": 2, "delta": -1}
                ]
            },
            "count_deltas": [{"name": "answer_cards", "baseline": 2, "candidate": 2, "delta": 0}],
        }

        self.assertTrue(report_has_fail_on_mismatch_condition(report))

    def test_fail_on_mismatch_condition_detects_candidate_regression(self):
        report = {
            "baseline": {"manifest_sqlite_mismatches": []},
            "candidate": {"manifest_sqlite_mismatches": []},
            "count_deltas": [{"name": "guides", "baseline": 10, "candidate": 9, "delta": -1}],
        }

        self.assertTrue(report_has_fail_on_mismatch_condition(report))

    def test_fail_on_mismatch_condition_allows_growth_and_unknown_delta(self):
        report = {
            "baseline": {"manifest_sqlite_mismatches": []},
            "candidate": {"manifest_sqlite_mismatches": []},
            "count_deltas": [
                {"name": "answer_cards", "baseline": 1, "candidate": 2, "delta": 1},
                {"name": "chunks", "baseline": None, "candidate": 4, "delta": None},
            ],
        }

        self.assertFalse(report_has_fail_on_mismatch_condition(report))

    def test_cli_default_reports_mismatch_without_failing(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            baseline_dir = tmp_path / "baseline"
            candidate_dir = tmp_path / "candidate"
            baseline_dir.mkdir()
            candidate_dir.mkdir()
            (baseline_dir / "senku_manifest.json").write_text(
                json.dumps({"counts": {"answer_cards": 3}}),
                encoding="utf-8",
            )
            (candidate_dir / "senku_manifest.json").write_text(
                json.dumps({"counts": {"answer_cards": 2}}),
                encoding="utf-8",
            )

            with contextlib.redirect_stdout(io.StringIO()):
                exit_code = main([str(baseline_dir), str(candidate_dir)])

            self.assertEqual(exit_code, 0)

    def test_cli_fail_on_mismatch_fails_on_candidate_regression(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            baseline_dir = tmp_path / "baseline"
            candidate_dir = tmp_path / "candidate"
            baseline_dir.mkdir()
            candidate_dir.mkdir()
            (baseline_dir / "senku_manifest.json").write_text(
                json.dumps({"counts": {"answer_cards": 3}}),
                encoding="utf-8",
            )
            (candidate_dir / "senku_manifest.json").write_text(
                json.dumps({"counts": {"answer_cards": 2}}),
                encoding="utf-8",
            )

            with contextlib.redirect_stdout(io.StringIO()):
                exit_code = main([str(baseline_dir), str(candidate_dir), "--fail-on-mismatch"])

            self.assertEqual(exit_code, 1)


if __name__ == "__main__":
    unittest.main()

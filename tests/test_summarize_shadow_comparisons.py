import importlib.util
import io
import json
import tempfile
import unittest
from pathlib import Path
from contextlib import redirect_stdout


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1]
        / "scripts"
        / "summarize_shadow_comparisons.py"
    )
    spec = importlib.util.spec_from_file_location(
        "summarize_shadow_comparisons", module_path
    )
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_summary(path: Path, payload: dict) -> None:
    path.write_text(json.dumps(payload), encoding="utf-8")


class SummarizeShadowComparisonsTests(unittest.TestCase):
    def test_collect_summaries_infers_labels_and_reads_fields(self):
        module = load_module()

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "contextual_shadow_compare_a"
            dir_a.mkdir()
            dir_b = root / "contextual_shadow_compare_b"
            dir_b.mkdir()

            summary = {
                "row_count": 12,
                "deltas": {
                    "hit_at_1": {
                        "comparable_rows": 9,
                        "net_improved": 1,
                    },
                    "hit_at_3": {"net_improved": 2},
                    "hit_at_k": {"net_improved": -1},
                },
                "baseline": {"hit_at_1": {"rate": 0.75}},
                "shadow": {"hit_at_1": {"rate": 0.83}},
                "mean_top_k_overlap_jaccard": 0.44,
                "baseline_primary": {"hit_at_1": {"rate": 0.8}},
                "shadow_primary": {"hit_at_1": {"rate": 0.6}},
                "primary_family_deltas": {"hit_at_1": {"net_improved": -1}},
                "mean_baseline_owner_family_concentration": 0.7,
                "mean_shadow_owner_family_concentration": 0.4,
                "mean_owner_family_concentration_delta": -0.3,
            }
            write_summary(dir_a / module.SUMMARY_FILENAME, summary)
            summary_b = dict(summary)
            summary_b["row_count"] = 8
            summary_b["baseline"]["hit_at_1"]["rate"] = 0.5
            summary_b["shadow"]["hit_at_1"]["rate"] = 0.8
            write_summary(dir_b / module.SUMMARY_FILENAME, summary_b)

            rows = module.collect_summaries([dir_a, dir_b])

        self.assertEqual(rows[0]["label"], dir_a.name)
        self.assertEqual(rows[1]["label"], dir_b.name)
        self.assertEqual(rows[0]["row_count"], 12)
        self.assertEqual(rows[0]["baseline_hit_at_1_rate"], 0.75)
        self.assertEqual(rows[0]["shadow_hit_at_1_rate"], 0.83)
        self.assertEqual(rows[0]["hit_at_1_net"], 1)
        self.assertEqual(rows[0]["baseline_primary_hit_at_1_rate"], 0.8)
        self.assertEqual(rows[0]["shadow_primary_hit_at_1_rate"], 0.6)
        self.assertEqual(rows[0]["primary_hit_at_1_net"], -1)
        self.assertEqual(rows[0]["mean_owner_family_concentration_delta"], -0.3)

    def test_collect_summaries_respects_explicit_labels_and_renders_markdown(self):
        module = load_module()

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir) / "first"
            root.mkdir()
            write_summary(
                root / module.SUMMARY_FILENAME,
                {
                    "row_count": 6,
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 3, "net_improved": 0},
                        "hit_at_3": {"net_improved": 1},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"hit_at_1": {"rate": 0.4}},
                    "shadow": {"hit_at_1": {"rate": 0.7}},
                    "mean_top_k_overlap_jaccard": 0.5,
                },
            )

            rows = module.collect_summaries([root], labels=["Custom A"])
            rendered = module.render_markdown_table(rows)

        self.assertIn(
            "| label | row_count | comparable_rows_hit_at_1 | baseline_scored_rows |",
            rendered,
        )
        self.assertIn("Custom A", rendered)

    def test_render_markdown_table_sanitizes_newlines_inside_cells(self):
        module = load_module()

        rendered = module.render_markdown_table(
            [
                {
                    "label": "Line one\nLine two",
                    "row_count": 1,
                    "hit_at_1_net": "gain\r\nwith | pipe",
                }
            ],
            columns=["label", "row_count", "hit_at_1_net"],
        )

        self.assertEqual(len(rendered.splitlines()), 3)
        self.assertIn("Line one<br>Line two", rendered)
        self.assertIn("gain<br>with \\| pipe", rendered)

    def test_main_json_output_is_machine_readable(self):
        module = load_module()

        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "contextual_shadow_compare_one"
            dir_a.mkdir()
            summary_path = dir_a / module.SUMMARY_FILENAME
            write_summary(
                summary_path,
                {
                    "row_count": 2,
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 2, "net_improved": 0},
                        "hit_at_3": {"net_improved": 0},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"hit_at_1": {"rate": 1.0}},
                    "shadow": {"hit_at_1": {"rate": 1.0}},
                    "mean_top_k_overlap_jaccard": 0.2,
                },
            )

            output = io.StringIO()
            with redirect_stdout(output):
                module.main([str(dir_a), "--json"])
            payload = json.loads(output.getvalue())

        self.assertEqual(len(payload), 1)
        self.assertEqual(payload[0]["label"], dir_a.name)
        self.assertEqual(payload[0]["comparable_rows_hit_at_1"], 2)
        self.assertAlmostEqual(payload[0]["mean_top_k_overlap_jaccard"], 0.2)

    def test_fallback_row_count_from_rows_jsonl(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            dir_a = root / "contextual_shadow_compare_rows"
            dir_a.mkdir()
            write_summary(
                dir_a / module.SUMMARY_FILENAME,
                {
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 1, "net_improved": 0},
                        "hit_at_3": {"net_improved": 0},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"hit_at_1": {"rate": 0.0}},
                    "shadow": {"hit_at_1": {"rate": 0.0}},
                    "mean_top_k_overlap_jaccard": 0.0,
                },
            )
            (dir_a / "compare_contextual_shadow_retrieval_rows.jsonl").write_text(
                '{"a": 1}\n{"a": 2}\n{"a": 3}\n',
                encoding="utf-8",
            )

            rows = module.collect_summaries([dir_a])

        self.assertEqual(rows[0]["row_count"], 3)

    def test_count_csv_rows_ignores_blank_rows(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            rows_csv = root / "rows.csv"
            rows_csv.write_text(
                "query,answer\n"
                "q1,a1\n"
                "\n"
                ",\n"
                "q2,a2\n",
                encoding="utf-8",
            )

            count = module._count_csv_rows(rows_csv)

        self.assertEqual(count, 2)

    def test_infer_row_count_accepts_numeric_string_and_falls_back_on_invalid(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            good_dir = root / "good"
            bad_dir = root / "bad"
            no_positive_dir = root / "no_positive"
            for directory in (good_dir, bad_dir, no_positive_dir):
                directory.mkdir()

            write_summary(
                good_dir / module.SUMMARY_FILENAME,
                {
                    "row_count": "4",
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 0, "net_improved": 0},
                        "hit_at_3": {"net_improved": 0},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"hit_at_1": {"rate": 0.0}},
                    "shadow": {"hit_at_1": {"rate": 0.0}},
                    "mean_top_k_overlap_jaccard": 0.0,
                },
            )
            rows = module.collect_summaries([good_dir])

            write_summary(
                bad_dir / module.SUMMARY_FILENAME,
                {
                    "row_count": "not-a-number",
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 0, "net_improved": 0},
                        "hit_at_3": {"net_improved": 0},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"hit_at_1": {"rate": 0.0}},
                    "shadow": {"hit_at_1": {"rate": 0.0}},
                    "mean_top_k_overlap_jaccard": 0.0,
                },
            )
            (bad_dir / "compare_contextual_shadow_retrieval_rows.csv").write_text(
                "query,answer\n"
                "q1,a1\n"
                "\n"
                ",,\n"
                "q2,a2\n",
                encoding="utf-8",
            )
            fallback_rows = module.collect_summaries([bad_dir])

            write_summary(
                no_positive_dir / module.SUMMARY_FILENAME,
                {
                    "row_count": 0,
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 0, "net_improved": 0},
                        "hit_at_3": {"net_improved": 0},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"hit_at_1": {"rate": 0.0}},
                    "shadow": {"hit_at_1": {"rate": 0.0}},
                    "mean_top_k_overlap_jaccard": 0.0,
                },
            )
            (no_positive_dir / "compare_contextual_shadow_retrieval_rows.jsonl").write_text(
                '{"a": 1}\n{"a": 2}\n',
                encoding="utf-8",
            )
            fallback_zero_rows = module.collect_summaries([no_positive_dir])

        self.assertEqual(rows[0]["row_count"], 4)
        self.assertEqual(fallback_rows[0]["row_count"], 2)
        self.assertEqual(fallback_zero_rows[0]["row_count"], 2)

    def test_primary_fields_are_optional_and_stay_stable_when_missing(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir) / "contextual_shadow_compare_optional_primary"
            root.mkdir()
            write_summary(
                root / module.SUMMARY_FILENAME,
                {
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 2, "net_improved": 0},
                        "hit_at_3": {"net_improved": 0},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"scored_rows": 2, "hit_at_1": {"count": 1, "rate": 0.5}},
                    "shadow": {"scored_rows": 2, "hit_at_1": {"count": 1, "rate": 0.5}},
                    "mean_top_k_overlap_jaccard": 0.1,
                },
            )

            rows = module.collect_summaries([root])

        self.assertIsNone(rows[0]["baseline_primary_hit_at_1_rate"])
        self.assertIsNone(rows[0]["shadow_primary_hit_at_1_rate"])
        self.assertIsNone(rows[0]["primary_hit_at_1_net"])

    def test_primary_metrics_remain_stable_when_zero_primary_denominator(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir) / "contextual_shadow_compare_zero_primary"
            root.mkdir()
            write_summary(
                root / module.SUMMARY_FILENAME,
                {
                    "row_count": 4,
                    "deltas": {
                        "hit_at_1": {"comparable_rows": 0, "net_improved": 0},
                        "hit_at_3": {"net_improved": 0},
                        "hit_at_k": {"net_improved": 0},
                    },
                    "baseline": {"scored_rows": 0, "hit_at_1": {"count": 0, "rate": None}},
                    "shadow": {"scored_rows": 0, "hit_at_1": {"count": 0, "rate": None}},
                    "baseline_primary": {"scored_rows": 0, "hit_at_1": {"count": 0, "rate": None}},
                    "shadow_primary": {"scored_rows": 0, "hit_at_1": {"count": 0, "rate": None}},
                    "primary_family_deltas": {"hit_at_1": {"net_improved": 0}},
                    "mean_top_k_overlap_jaccard": 0.0,
                },
            )

            output = io.StringIO()
            with redirect_stdout(output):
                module.main([str(root), "--json"])
            payload = json.loads(output.getvalue())

        self.assertEqual(payload[0]["baseline_primary_hit_at_1_rate"], None)
        self.assertEqual(payload[0]["shadow_primary_hit_at_1_rate"], None)
        self.assertEqual(payload[0]["primary_hit_at_1_net"], 0)


if __name__ == "__main__":
    unittest.main()

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


if __name__ == "__main__":
    unittest.main()

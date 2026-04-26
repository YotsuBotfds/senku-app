import importlib.util
import json
import os
import sys
import tempfile
import unittest
from pathlib import Path


def load_module():
    module_path = Path(__file__).resolve().parents[1] / "scripts" / "rag_experiment_lineage.py"
    spec = importlib.util.spec_from_file_location("rag_experiment_lineage", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def write_diag(
    path: Path,
    *,
    supported: int,
    miss: int,
    row_bucket: str,
    cited: str,
    total_rows: int = 1,
) -> None:
    path.mkdir(parents=True)
    data = {
        "generated_at": "2026-04-25T12:00:00",
        "summary": {
            "total_rows": total_rows,
            "by_bucket": {
                "expected_supported": supported,
                "generation_miss": miss,
            },
            "expected_cited": f"{supported}/1 (100.0%)" if supported else "0/1 (0.0%)",
        },
        "rows": [
            {
                "prompt_id": "RE2-BR-005",
                "suspected_failure_bucket": row_bucket,
                "expected_guide_ids": "GD-646",
                "top_retrieved_guide_ids": "GD-646|GD-250",
                "cited_guide_ids": cited,
                "expected_cited": "yes" if "GD-646" in cited else "no",
                "app_acceptance_status": "moderate_supported" if supported else "needs_evidence_owner",
                "short_reason": "fixture",
            }
        ],
    }
    (path / "diagnostics.json").write_text(json.dumps(data), encoding="utf-8")


class RAGExperimentLineageTests(unittest.TestCase):
    def test_discover_load_summarize_and_transition(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "artifacts" / "bench"
            first = bench / "rag_eval_fixture_baseline_diag"
            second = bench / "rag_eval_fixture_candidate_diag"
            write_diag(first, supported=0, miss=1, row_bucket="generation_miss", cited="GD-250")
            write_diag(second, supported=1, miss=0, row_bucket="expected_supported", cited="GD-646")
            os.utime(first / "diagnostics.json", (1000, 1000))
            os.utime(second / "diagnostics.json", (2000, 2000))

            dirs = module.discover_diagnostic_dirs(bench, "rag_eval_fixture")
            runs = [module.load_diagnostic_run(path, stem="rag_eval_fixture") for path in dirs]
            lineage = module.build_lineage(runs)

        self.assertEqual([run.label for run in runs], ["baseline", "candidate"])
        self.assertEqual(lineage["run_count"], 2)
        self.assertEqual(lineage["runs"][1]["expected_supported"], 1)
        self.assertEqual(lineage["transitions"][0]["kind"], "improved")
        self.assertEqual(lineage["transitions"][0]["prompt_id"], "RE2-BR-005")

    def test_discovery_skips_partial_runs_by_default(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "artifacts" / "bench"
            full = bench / "rag_eval_fixture_full_diag"
            partial = bench / "rag_eval_fixture_one_row_diag"
            write_diag(full, supported=1, miss=0, row_bucket="expected_supported", cited="GD-646", total_rows=21)
            write_diag(partial, supported=1, miss=0, row_bucket="expected_supported", cited="GD-646", total_rows=1)

            default_dirs = module.discover_diagnostic_dirs(bench, "rag_eval_fixture")
            all_dirs = module.discover_diagnostic_dirs(bench, "rag_eval_fixture", include_partial=True)

        self.assertEqual([path.name for path in default_dirs], ["rag_eval_fixture_full_diag"])
        self.assertEqual({path.name for path in all_dirs}, {"rag_eval_fixture_full_diag", "rag_eval_fixture_one_row_diag"})

    def test_render_markdown_includes_run_and_transition_tables(self):
        module = load_module()
        lineage = {
            "generated_at": "2026-04-25T12:00:00",
            "run_count": 1,
            "runs": [
                {
                    "label": "candidate",
                    "expected_supported": 1,
                    "generation_miss": 0,
                    "ranking_miss": 0,
                    "retrieval_miss": 0,
                    "artifact_error": 0,
                    "expected_cited": "1/1",
                    "path": "artifacts/bench/candidate_diag",
                }
            ],
            "transitions": [],
        }

        rendered = module.render_markdown(lineage)

        self.assertIn("# RAG Experiment Lineage", rendered)
        self.assertIn("`candidate`", rendered)
        self.assertIn("No prompt-level transitions found.", rendered)


if __name__ == "__main__":
    unittest.main()

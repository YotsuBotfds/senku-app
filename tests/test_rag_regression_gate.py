import importlib.util
import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path


def load_module():
    module_path = Path(__file__).resolve().parents[1] / "scripts" / "rag_regression_gate.py"
    spec = importlib.util.spec_from_file_location("rag_regression_gate", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_diagnostics(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload), encoding="utf-8")


def payload(
    *,
    supported: int = 3,
    retrieval: int = 0,
    ranking: int = 0,
    generation: int = 0,
    owner_top3: str = "3/3 (100.0%)",
    owner_topk: str = "3/3 (100.0%)",
    expected_cited: str = "3/3 (100.0%)",
    marker_fail: int = 0,
    marker_warn: int = 0,
    total_rows: int = 3,
) -> dict:
    return {
        "generated_at": "2026-04-25T12:00:00",
        "summary": {
            "total_rows": total_rows,
            "by_bucket": {
                "expected_supported": supported,
                "retrieval_miss": retrieval,
                "ranking_miss": ranking,
                "generation_miss": generation,
            },
            "expected_owner_rows": total_rows,
            "expected_owner_top3_count": owner_top3,
            "expected_owner_topk_count": owner_topk,
            "expected_owner_top3_share": "1.0000" if supported == total_rows else "0.6667",
            "expected_owner_topk_share": "1.0000" if supported == total_rows else "0.6667",
            "expected_cited": expected_cited,
            "top1_marker_risk_counts": {
                "fail": marker_fail,
                "warn": marker_warn,
                "none": max(0, total_rows - marker_fail - marker_warn),
            },
            "top1_bridge_rows": 0,
            "top1_unresolved_partial_rows": 0,
        },
        "rows": [],
    }


class RAGRegressionGateTests(unittest.TestCase):
    def setUp(self) -> None:
        self.module = load_module()

    def test_build_report_flags_default_count_regressions(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            baseline = root / "baseline_diag"
            current = root / "current_diag"
            write_diagnostics(baseline / "diagnostics.json", payload())
            write_diagnostics(
                current / "diagnostics.json",
                payload(
                    supported=2,
                    generation=1,
                    owner_top3="2/3 (66.7%)",
                    owner_topk="2/3 (66.7%)",
                    expected_cited="2/3 (66.7%)",
                    marker_fail=1,
                ),
            )

            report = self.module.build_report(
                baseline,
                current,
                fail_on_regression=True,
            )

        regressed = {
            item["metric"]
            for item in report["comparisons"]
            if item["regressed"]
        }
        self.assertEqual(report["status"], "regression")
        self.assertEqual(report["exit_code"], 1)
        self.assertIn("expected_supported", regressed)
        self.assertIn("generation_miss", regressed)
        self.assertIn("expected_owner_top3_hits", regressed)
        self.assertIn("expected_cited_count", regressed)
        self.assertIn("top1_marker_fail", regressed)

    def test_report_only_main_returns_zero_for_regression(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            baseline = root / "baseline_diag"
            current = root / "current_diag"
            write_diagnostics(baseline / "diagnostics.json", payload())
            write_diagnostics(current / "diagnostics.json", payload(supported=2, generation=1))

            output = io.StringIO()
            with redirect_stdout(output):
                exit_code = self.module.main(
                    [str(baseline), str(current), "--format", "text"]
                )

        self.assertEqual(exit_code, 0)
        self.assertIn("RAG regression gate: REGRESSION", output.getvalue())

    def test_fail_on_regression_main_returns_one(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            baseline = root / "baseline_diag"
            current = root / "current_diag"
            write_diagnostics(baseline / "diagnostics.json", payload())
            write_diagnostics(current / "diagnostics.json", payload(supported=2))

            with redirect_stdout(io.StringIO()):
                exit_code = self.module.main(
                    [str(baseline), str(current), "--fail-on-regression"]
                )

        self.assertEqual(exit_code, 1)

    def test_allow_regression_suppresses_configured_count_drop(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            baseline = root / "baseline_diag"
            current = root / "current_diag"
            write_diagnostics(baseline / "diagnostics.json", payload())
            write_diagnostics(current / "diagnostics.json", payload(supported=2))

            report = self.module.build_report(
                baseline,
                current,
                rules={"expected_supported": "down"},
                allowances={"expected_supported": 1},
                fail_on_regression=True,
            )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["exit_code"], 0)

    def test_allow_regression_rejects_non_finite_number(self):
        with self.assertRaisesRegex(ValueError, "non-negative number"):
            self.module.parse_allowances(["expected_supported=nan"])

    def test_non_finite_metric_values_are_reported_unavailable(self):
        comparison = self.module.compare_metrics(
            {"expected_supported": "nan"},
            {"expected_supported": 1},
            {"expected_supported": "down"},
        )[0]

        self.assertFalse(comparison["available"])
        self.assertFalse(comparison["regressed"])
        self.assertIsNone(comparison["delta"])

    def test_accepts_diagnostics_json_file_and_emits_json(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            baseline = root / "baseline_diag" / "diagnostics.json"
            current = root / "current_diag" / "diagnostics.json"
            write_diagnostics(baseline, payload())
            write_diagnostics(current, payload())

            output = io.StringIO()
            with redirect_stdout(output):
                exit_code = self.module.main([str(baseline), str(current), "--json"])
            report = json.loads(output.getvalue())

        self.assertEqual(exit_code, 0)
        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["baseline"]["label"], "baseline_diag")
        self.assertEqual(report["current"]["label"], "current_diag")

    def test_row_fallback_counts_owner_and_marker_metrics(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            baseline = root / "baseline_diag"
            current = root / "current_diag"
            write_diagnostics(
                baseline / "diagnostics.json",
                {
                    "summary": {"total_rows": 2},
                    "rows": [
                        {
                            "suspected_failure_bucket": "expected_supported",
                            "expected_guide_ids": "GD-001",
                            "expected_owner_top3_count": 1,
                            "expected_owner_topk_count": 1,
                            "expected_owner_top3_share": 0.3333,
                            "expected_owner_topk_share": 1.0,
                            "expected_cited": "yes",
                            "top1_marker_risk": "none",
                        },
                        {
                            "suspected_failure_bucket": "expected_supported",
                            "expected_guide_ids": "GD-002",
                            "expected_owner_top3_count": 1,
                            "expected_owner_topk_count": 1,
                            "expected_owner_top3_share": 0.3333,
                            "expected_owner_topk_share": 1.0,
                            "expected_cited": "yes",
                            "top1_marker_risk": "none",
                        },
                    ],
                },
            )
            write_diagnostics(
                current / "diagnostics.json",
                {
                    "summary": {"total_rows": 2},
                    "rows": [
                        {
                            "suspected_failure_bucket": "expected_supported",
                            "expected_guide_ids": "GD-001",
                            "expected_owner_top3_count": 1,
                            "expected_owner_topk_count": 1,
                            "expected_owner_top3_share": 0.3333,
                            "expected_owner_topk_share": 1.0,
                            "expected_cited": "yes",
                            "top1_marker_risk": "none",
                        },
                        {
                            "suspected_failure_bucket": "generation_miss",
                            "expected_guide_ids": "GD-002",
                            "expected_owner_top3_count": 0,
                            "expected_owner_topk_count": 0,
                            "expected_owner_top3_share": 0.0,
                            "expected_owner_topk_share": 0.0,
                            "expected_cited": "no",
                            "top1_marker_risk": "warn",
                        },
                    ],
                },
            )

            report = self.module.build_report(baseline, current)

        regressed = {
            item["metric"]
            for item in report["comparisons"]
            if item["regressed"]
        }
        self.assertIn("expected_owner_top3_hits", regressed)
        self.assertIn("expected_cited_count", regressed)
        self.assertIn("top1_marker_warn", regressed)
        self.assertEqual(report["baseline"]["expected_supported"], 2)
        self.assertEqual(report["current"]["generation_miss"], 1)

    def test_custom_checks_can_disable_defaults(self):
        rules = self.module.parse_rules(
            no_default_checks=True,
            checks=["total_rows:down"],
            ignored=[],
        )

        self.assertEqual(rules, {"total_rows": "down"})


if __name__ == "__main__":
    unittest.main()

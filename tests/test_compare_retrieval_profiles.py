import importlib.util
import json
import tempfile
import unittest
from pathlib import Path


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1] / "scripts" / "compare_retrieval_profiles.py"
    )
    spec = importlib.util.spec_from_file_location("compare_retrieval_profiles", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class CompareRetrievalProfilesTests(unittest.TestCase):
    def setUp(self):
        self.module = load_module()

    def test_delta_rows_compare_against_first_profile(self):
        rows = [
            {
                "profile": "baseline",
                "hit_at_1_rate": 0.5,
                "hit_at_3_rate": 0.75,
                "hit_at_k_rate": 1.0,
                "mean_owner_share": 0.25,
                "mean_latency_ms": 10.0,
                "p95_latency_ms": 20.0,
            },
            {
                "profile": "candidate",
                "hit_at_1_rate": 0.75,
                "hit_at_3_rate": 0.5,
                "hit_at_k_rate": 1.0,
                "mean_owner_share": 0.5,
                "mean_latency_ms": 12.0,
                "p95_latency_ms": 18.0,
            },
        ]

        deltas = self.module.delta_rows(rows)

        self.assertEqual(deltas[0]["profile"], "candidate")
        self.assertEqual(deltas[0]["baseline_profile"], "baseline")
        self.assertEqual(deltas[0]["hit_at_1_rate_delta"], 0.25)
        self.assertEqual(deltas[0]["hit_at_3_rate_delta"], -0.25)
        self.assertEqual(deltas[0]["hit_at_k_rate_delta"], 0.0)
        self.assertEqual(deltas[0]["mean_owner_share_delta"], 0.25)
        self.assertEqual(deltas[0]["mean_latency_ms_delta"], 2.0)
        self.assertEqual(deltas[0]["p95_latency_ms_delta"], -2.0)

    def test_compare_profiles_runs_requested_profiles(self):
        calls = []
        original_load_prompt_pack = self.module.eval_pack.load_prompt_pack
        original_evaluate_pack = self.module.eval_pack.evaluate_pack

        def fake_load_prompt_pack(path):
            return [{"prompt": "q", "expected_guide_ids": ["GD-1"]}]

        def fake_evaluate_pack(prompt_rows, *, retrieval_profile_override, **kwargs):
            calls.append(retrieval_profile_override)
            hit = retrieval_profile_override == "candidate"
            return [
                {
                    "expected_guide_ids": ["GD-1"],
                    "expected_hit_at_1": hit,
                    "expected_hit_at_3": hit,
                    "expected_hit_at_k": True,
                    "expected_owner_best_rank": 1 if hit else 2,
                    "expected_owner_count": 1,
                    "retrieved_count": 2,
                    "owner_share": 0.5,
                    "retrieval_elapsed_ms": 5.0 if hit else 10.0,
                    "top1_marker_risk": "none",
                    "top1_is_bridge": "no",
                    "top1_has_unresolved_partial": "no",
                    "top_distractor_guide_ids": [],
                }
            ]

        self.module.eval_pack.load_prompt_pack = fake_load_prompt_pack
        self.module.eval_pack.evaluate_pack = fake_evaluate_pack
        try:
            payload = self.module.compare_profiles(
                Path("pack.jsonl"),
                ["baseline", "candidate"],
                collection=object(),
                top_k=8,
            )
        finally:
            self.module.eval_pack.load_prompt_pack = original_load_prompt_pack
            self.module.eval_pack.evaluate_pack = original_evaluate_pack

        self.assertEqual(calls, ["baseline", "candidate"])
        self.assertEqual(payload["profile_rows"][0]["profile"], "baseline")
        self.assertEqual(payload["profile_rows"][1]["profile"], "candidate")
        self.assertEqual(payload["deltas_vs_baseline"][0]["hit_at_1_rate_delta"], 1.0)
        self.assertEqual(payload["deltas_vs_baseline"][0]["mean_latency_ms_delta"], -5.0)

    def test_compare_profiles_normalizes_benign_row_shape_variants(self):
        original_load_prompt_pack = self.module.eval_pack.load_prompt_pack
        original_evaluate_pack = self.module.eval_pack.evaluate_pack

        def fake_load_prompt_pack(path):
            return [{"prompt": "q", "expected_guide_ids": ["GD-1"]}]

        def fake_evaluate_pack(prompt_rows, *, retrieval_profile_override, **kwargs):
            return [
                {
                    "expected_guide_ids": " GD-1 | gd-2 ",
                    "primary_expected_guide_ids": ["gd-1, GD-2"],
                    "expected_hit_at_1": True,
                    "expected_hit_at_3": True,
                    "expected_hit_at_k": True,
                    "expected_owner_best_rank": 1,
                    "primary_hit_at_1": True,
                    "primary_hit_at_3": True,
                    "primary_hit_at_k": True,
                    "primary_owner_best_rank": 1,
                    "expected_owner_count": 1,
                    "retrieved_count": 2,
                    "owner_share": 0.5,
                    "retrieval_elapsed_ms": 5.0,
                    "top1_marker_risk": "none",
                    "top1_is_bridge": [" no ", "yes"],
                    "top1_has_unresolved_partial": "no | true",
                    "top_distractor_guide_ids": [],
                }
            ]

        self.module.eval_pack.load_prompt_pack = fake_load_prompt_pack
        self.module.eval_pack.evaluate_pack = fake_evaluate_pack
        try:
            payload = self.module.compare_profiles(
                Path("pack.jsonl"),
                ["baseline"],
                collection=object(),
                top_k=8,
            )
        finally:
            self.module.eval_pack.load_prompt_pack = original_load_prompt_pack
            self.module.eval_pack.evaluate_pack = original_evaluate_pack

        summary = payload["runs"][0]["summary"]
        normalized_row = payload["runs"][0]["rows"][0]
        self.assertEqual(normalized_row["expected_guide_ids"], ["GD-1", "GD-2"])
        self.assertEqual(normalized_row["primary_expected_guide_ids"], ["GD-1", "GD-2"])
        self.assertEqual(summary["expected_owner_rows"], 1)
        self.assertEqual(summary["primary_expected_rows"], 1)
        self.assertEqual(summary["top1_bridge_rows"], 1)
        self.assertEqual(summary["top1_unresolved_partial_rows"], 1)

    def test_render_markdown_includes_profile_latency_and_marker_columns(self):
        payload = {
            "prompt_pack": "pack.jsonl",
            "generated_at": "2026-04-26T00:00:00",
            "config": {"top_k": 8},
            "profile_rows": [
                {
                    "profile": "baseline",
                    "hit_at_1_rate": 0.5,
                    "hit_at_3_rate": 1.0,
                    "hit_at_k_rate": 1.0,
                    "mean_owner_share": 0.25,
                    "mean_latency_ms": 12.3,
                    "p95_latency_ms": 20.1,
                    "top1_marker_risk_counts": {"warn": 1},
                }
            ],
            "deltas_vs_baseline": [],
        }

        markdown = self.module.render_markdown(payload)

        self.assertIn("| profile | hit@1 | hit@3 | hit@k | owner share | mean ms | p95 ms | marker risks |", markdown)
        self.assertIn("| baseline | 0.5 | 1 | 1 | 0.25 | 12.3 | 20.1 | warn:1 |", markdown)

    def test_render_markdown_handles_mixed_marker_label_shapes(self):
        payload = {
            "prompt_pack": "pack.jsonl",
            "generated_at": "2026-04-26T00:00:00",
            "config": {"top_k": 8},
            "profile_rows": [
                {
                    "profile": "candidate|profile",
                    "hit_at_1_rate": None,
                    "hit_at_3_rate": None,
                    "hit_at_k_rate": None,
                    "mean_owner_share": None,
                    "mean_latency_ms": None,
                    "p95_latency_ms": None,
                    "top1_marker_risk_counts": {
                        3: 1,
                        "pipe|label": 2,
                        "line\nlabel": 4,
                    },
                }
            ],
            "deltas_vs_baseline": [],
        }

        markdown = self.module.render_markdown(payload)

        self.assertIn("candidate\\|profile", markdown)
        self.assertIn("3:1", markdown)
        self.assertIn("line label:4", markdown)
        self.assertIn("pipe\\|label:2", markdown)

    def test_cli_parse_accepts_profiles_and_outputs(self):
        args = self.module.parse_args(
            [
                "pack.jsonl",
                "--profiles",
                "a",
                "b",
                "--top-k",
                "4",
                "--output-json",
                "out.json",
            ]
        )

        self.assertEqual(args.profiles, ["a", "b"])
        self.assertEqual(args.top_k, 4)
        self.assertEqual(args.output_json, Path("out.json"))


if __name__ == "__main__":
    unittest.main()

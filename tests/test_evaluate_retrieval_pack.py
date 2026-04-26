import importlib.util
import json
import tempfile
import unittest
from pathlib import Path


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1] / "scripts" / "evaluate_retrieval_pack.py"
    )
    spec = importlib.util.spec_from_file_location("evaluate_retrieval_pack", module_path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class EvaluateRetrievalPackTests(unittest.TestCase):
    def setUp(self):
        self.module = load_module()

    def test_extract_guide_ids_accepts_common_shapes(self):
        self.assertEqual(
            self.module.extract_guide_ids("GD-898|gd-301, and GD-898 again"),
            ["GD-898", "GD-301"],
        )
        self.assertEqual(
            self.module.extract_guide_ids(
                [{"guide_id": "GD-111"}, {"id": "GD-222"}, "GD-333"]
            ),
            ["GD-111", "GD-222", "GD-333"],
        )
        self.assertEqual(
            self.module.extract_guide_ids(
                {
                    "guide_ids": ["GD-444|GD-555"],
                    "expected_source_guide_ids": [{"target_guide_id": "gd-666"}],
                }
            ),
            ["GD-444", "GD-555", "GD-666"],
        )

    def test_load_prompt_pack_preserves_metadata_and_expected_owner(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "pack.jsonl"
            path.write_text(
                json.dumps(
                    {
                        "id": "CASE-001",
                        "lane": "rag-eval",
                        "section": "Owner checks",
                        "guide_id": "GD-024",
                        "primary_expected_guides": ["GD-024"],
                        "prompt": "How should we stay warm tonight?",
                    }
                )
                + "\n",
                encoding="utf-8",
            )

            rows = self.module.load_prompt_pack(path)

        self.assertEqual(rows[0]["prompt_id"], "CASE-001")
        self.assertEqual(rows[0]["lane"], "rag-eval")
        self.assertEqual(rows[0]["section"], "Owner checks")
        self.assertEqual(rows[0]["expected_guide_ids"], ["GD-024"])
        self.assertEqual(rows[0]["primary_expected_guide_ids"], ["GD-024"])
        self.assertEqual(rows[0]["prompt"], "How should we stay warm tonight?")

    def test_load_prompt_pack_normalizes_primary_expected_guide_ids_with_duplicates_and_blank_noise(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "pack.jsonl"
            path.write_text(
                json.dumps(
                    {
                        "id": "CASE-003",
                        "prompt": "Noise should be ignored in primary expectations.",
                        "primary_expected_guides": [
                            "GD-024",
                            "gd-024",
                            "",
                            "   ",
                            "GD-12x",
                            {"guide_id": "gd-120"},
                        ],
                        "primary_expected_guide_id": "gd-120",
                    }
                )
                + "\n",
                encoding="utf-8",
            )

            rows = self.module.load_prompt_pack(path)

        self.assertEqual(rows[0]["primary_expected_guide_ids"], ["GD-120", "GD-024"])

    def test_primary_expected_guides_do_not_replace_expected_guides(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "pack.jsonl"
            path.write_text(
                json.dumps(
                    {
                        "id": "CASE-002",
                        "expected_guides": ["GD-380", "GD-232"],
                        "primary_expected_guides": [{"guide_id": "GD-380"}],
                        "prompt": "My kid was hit in the belly and looks faint.",
                    }
                )
                + "\n",
                encoding="utf-8",
            )

            rows = self.module.load_prompt_pack(path)

        self.assertEqual(rows[0]["expected_guide_ids"], ["GD-380", "GD-232"])
        self.assertEqual(rows[0]["primary_expected_guide_ids"], ["GD-380"])

    def test_row_metrics_reports_owner_hits_share_rank_and_distractors(self):
        metrics = self.module.row_metrics(
            ["GD-898", "GD-301"],
            ["GD-054", "GD-898", "GD-301", "GD-898", "GD-239"],
            top_k=5,
        )

        self.assertFalse(metrics["expected_hit_at_1"])
        self.assertTrue(metrics["expected_hit_at_3"])
        self.assertTrue(metrics["expected_hit_at_k"])
        self.assertEqual(metrics["expected_owner_best_rank"], 2)
        self.assertEqual(metrics["expected_owner_count"], 3)
        self.assertEqual(metrics["retrieved_count"], 5)
        self.assertAlmostEqual(metrics["owner_share"], 0.6)
        self.assertEqual(
            metrics["top_distractor_guide_ids"],
            [
                {"guide_id": "GD-054", "count": 1},
                {"guide_id": "GD-239", "count": 1},
            ],
        )

    def test_row_metrics_sorts_equal_count_distractors_by_guide_id(self):
        metrics = self.module.row_metrics(
            ["GD-999"],
            ["GD-300", "GD-100", "GD-200"],
            top_k=3,
        )

        self.assertEqual(
            metrics["top_distractor_guide_ids"],
            [
                {"guide_id": "GD-100", "count": 1},
                {"guide_id": "GD-200", "count": 1},
                {"guide_id": "GD-300", "count": 1},
            ],
        )

    def test_primary_row_metrics_reports_primary_hits_and_rank_only(self):
        metrics = self.module.primary_row_metrics(
            ["GD-380"],
            ["GD-232", "GD-380", "GD-584"],
            top_k=3,
        )

        self.assertEqual(
            metrics,
            {
                "primary_hit_at_1": False,
                "primary_hit_at_3": True,
                "primary_hit_at_k": True,
                "primary_best_rank": 2,
                "primary_owner_best_rank": 2,
            },
        )

    def test_summarize_rows_aggregates_expected_owner_metrics(self):
        rows = [
            {
                "expected_guide_ids": ["GD-111"],
                "primary_expected_guide_ids": ["GD-111"],
                "expected_hit_at_1": True,
                "expected_hit_at_3": True,
                "expected_hit_at_k": True,
                "expected_owner_best_rank": 1,
                "primary_hit_at_1": True,
                "primary_hit_at_3": True,
                "primary_hit_at_k": True,
                "primary_owner_best_rank": 1,
                "expected_owner_count": 1,
                "retrieved_count": 3,
                "retrieval_elapsed_ms": 12.0,
                "top1_marker_risk": "warn",
                "top1_is_bridge": "yes",
                "top1_has_unresolved_partial": "yes",
                "top_distractor_guide_ids": [
                    {"guide_id": "GD-222", "count": 2},
                ],
            },
            {
                "expected_guide_ids": ["GD-333"],
                "primary_expected_guide_ids": ["GD-444"],
                "expected_hit_at_1": False,
                "expected_hit_at_3": False,
                "expected_hit_at_k": False,
                "expected_owner_best_rank": None,
                "primary_hit_at_1": False,
                "primary_hit_at_3": True,
                "primary_hit_at_k": True,
                "primary_owner_best_rank": 2,
                "expected_owner_count": 0,
                "retrieved_count": 2,
                "retrieval_elapsed_ms": 4.0,
                "top1_marker_risk": "none",
                "top1_is_bridge": "no",
                "top1_has_unresolved_partial": "no",
                "top_distractor_guide_ids": [
                    {"guide_id": "GD-222", "count": 1},
                    {"guide_id": "GD-444", "count": 1},
                ],
            },
            {
                "expected_guide_ids": ["GD-555"],
                "primary_expected_guide_ids": ["GD-555"],
                "error": "RuntimeError: embed unavailable",
            },
            {
                "expected_guide_ids": [],
                "retrieved_count": 1,
            },
        ]

        summary = self.module.summarize_rows(rows)

        self.assertEqual(summary["total_prompts"], 4)
        self.assertEqual(summary["expected_owner_rows"], 2)
        self.assertEqual(summary["retrieval_error_rows"], 1)
        self.assertEqual(summary["expected_hit_at_1"]["text"], "1/2 (50.0%)")
        self.assertEqual(summary["expected_hit_at_3"]["text"], "1/2 (50.0%)")
        self.assertEqual(summary["expected_hit_at_k"]["text"], "1/2 (50.0%)")
        self.assertEqual(summary["expected_owner_best_rank"], "1.00")
        self.assertEqual(summary["primary_expected_rows"], 2)
        self.assertEqual(summary["primary_hit_at_1"]["text"], "1/2 (50.0%)")
        self.assertEqual(summary["primary_hit_at_3"]["text"], "2/2 (100.0%)")
        self.assertEqual(summary["primary_hit_at_k"]["text"], "2/2 (100.0%)")
        self.assertEqual(summary["primary_best_rank"], "1.50")
        self.assertEqual(summary["primary_owner_best_rank"], "1.50")
        self.assertEqual(summary["primary_owner_ranked_rows"], 2)
        self.assertEqual(summary["simple_owner_share"]["text"], "1/5 (20.0%)")
        self.assertEqual(summary["retrieval_latency_ms"], {"count": 2, "mean": 8.0, "p95": 12.0})
        self.assertEqual(summary["top1_marker_risk_counts"], {"warn": 1, "none": 1})
        self.assertEqual(summary["top1_bridge_rows"], 1)
        self.assertEqual(summary["top1_unresolved_partial_rows"], 1)
        self.assertEqual(
            summary["top_distractor_guide_ids"][:2],
            [
                {"guide_id": "GD-222", "count": 3},
                {"guide_id": "GD-444", "count": 1},
            ],
        )

    def test_summarize_rows_sorts_equal_count_distractors_by_guide_id(self):
        rows = [
            {
                "expected_guide_ids": ["GD-999"],
                "expected_owner_count": 0,
                "retrieved_count": 3,
                "top_distractor_guide_ids": [
                    {"guide_id": "GD-300", "count": 1},
                    {"guide_id": "GD-100", "count": 1},
                    {"guide_id": "GD-200", "count": 1},
                ],
            }
        ]

        summary = self.module.summarize_rows(rows)

        self.assertEqual(
            summary["top_distractor_guide_ids"],
            [
                {"guide_id": "GD-100", "count": 1},
                {"guide_id": "GD-200", "count": 1},
                {"guide_id": "GD-300", "count": 1},
            ],
        )

    def test_guide_ids_from_results_reads_chroma_like_metadata(self):
        results = {
            "metadatas": [
                [
                    {"guide_id": "GD-001", "guide_title": "One"},
                    {"guide_id": "GD-002", "guide_title": "Two"},
                ]
            ]
        }

        self.assertEqual(
            self.module.guide_ids_from_results(results),
            ["GD-001", "GD-002"],
        )

    def test_evaluate_pack_uses_retrieval_hook_without_generation_and_adds_marker_overlay(self):
        calls = []
        original = self.module.retrieve_for_prompt

        def fake_retrieve(
            prompt,
            collection,
            *,
            top_k,
            category,
            embed_url,
            retrieval_profile_override=None,
        ):
            calls.append(
                {
                    "prompt": prompt,
                    "collection": collection,
                    "top_k": top_k,
                    "category": category,
                    "embed_url": embed_url,
                    "retrieval_profile_override": retrieval_profile_override,
                }
            )
            return ["GD-999", "GD-123"], {"retrieval_meta": {"fake": True}}

        self.module.retrieve_for_prompt = fake_retrieve
        try:
            rows = self.module.evaluate_pack(
                [
                    {
                        "line_number": 1,
                        "prompt_id": "P-1",
                        "section": "Section",
                        "prompt": "question?",
                        "expected_guide_ids": ["GD-123"],
                        "primary_expected_guide_ids": ["GD-123"],
                    }
                ],
                collection="fake-collection",
                top_k=3,
                category="medical",
                embed_url="http://embed",
                retrieval_profile_override="safety_triage",
                corpus_marker_lookup={
                    "GD-999": {
                        "severity_counts": {"warn": 1},
                        "marker_counts": {"unresolved_partial": 1},
                        "bridge": True,
                    }
                },
            )
        finally:
            self.module.retrieve_for_prompt = original

        self.assertEqual(calls[0]["prompt"], "question?")
        self.assertEqual(calls[0]["collection"], "fake-collection")
        self.assertEqual(calls[0]["top_k"], 3)
        self.assertEqual(calls[0]["category"], "medical")
        self.assertEqual(calls[0]["embed_url"], "http://embed")
        self.assertEqual(calls[0]["retrieval_profile_override"], "safety_triage")
        self.assertEqual(rows[0]["top_retrieved_guide_ids"], ["GD-999", "GD-123"])
        self.assertEqual(rows[0]["expected_owner_best_rank"], 2)
        self.assertEqual(rows[0]["primary_expected_guide_ids"], ["GD-123"])
        self.assertFalse(rows[0]["primary_hit_at_1"])
        self.assertTrue(rows[0]["primary_hit_at_3"])
        self.assertTrue(rows[0]["primary_hit_at_k"])
        self.assertEqual(rows[0]["primary_best_rank"], 2)
        self.assertEqual(rows[0]["primary_owner_best_rank"], 2)
        self.assertEqual(rows[0]["top1_marker_risk"], "warn")
        self.assertEqual(rows[0]["top1_is_bridge"], "yes")
        self.assertEqual(rows[0]["top1_has_unresolved_partial"], "yes")
        self.assertIsInstance(rows[0]["retrieval_elapsed_ms"], float)
        self.assertTrue(rows[0]["retrieval_meta"]["fake"])

    def test_render_markdown_includes_primary_metrics_and_row_columns(self):
        payload = {
            "prompt_pack": "pack.jsonl",
            "generated_at": "2026-04-26T10:00:00",
            "config": {
                "top_k": 3,
                "category": None,
                "embed_url": None,
                "retrieval_profile_override": None,
            },
            "summary": {
                "total_prompts": 1,
                "expected_owner_rows": 1,
                "primary_expected_rows": 1,
                "retrieval_error_rows": 0,
                "expected_hit_at_1": {"text": "0/1 (0.0%)"},
                "expected_hit_at_3": {"text": "1/1 (100.0%)"},
                "expected_hit_at_k": {"text": "1/1 (100.0%)"},
                "expected_owner_best_rank": "2.00",
                "primary_hit_at_1": {"text": "1/1 (100.0%)"},
                "primary_hit_at_3": {"text": "1/1 (100.0%)"},
                "primary_hit_at_k": {"text": "1/1 (100.0%)"},
                "primary_best_rank": "1.00",
                "primary_owner_best_rank": "1.00",
                "simple_owner_share": {"text": "1/3 (33.3%)"},
                "retrieval_latency_ms": {"mean": 1.0, "p95": 1.0},
                "top_distractor_guide_ids": [],
            },
            "rows": [
                {
                    "prompt_index": 1,
                    "prompt_id": "P-1",
                    "expected_guide_ids": ["GD-380", "GD-232"],
                    "primary_expected_guide_ids": ["GD-380"],
                    "top_retrieved_guide_ids": ["GD-380", "GD-232", "GD-584"],
                    "expected_hit_at_1": True,
                    "expected_hit_at_3": True,
                    "expected_hit_at_k": True,
                    "expected_owner_best_rank": 1,
                    "primary_hit_at_1": True,
                    "primary_hit_at_3": True,
                    "primary_hit_at_k": True,
                    "primary_owner_best_rank": 1,
                    "owner_share": 0.6667,
                }
            ],
        }

        markdown = self.module.render_markdown(payload)

        self.assertIn("- primary_expected_rows: 1", markdown)
        self.assertIn("| primary hit@1 | 1/1 (100.0%) |", markdown)
        self.assertIn("primary best rank", markdown)
        self.assertIn("GD-380", markdown)

    def test_render_markdown_does_not_export_primary_alias_for_blank_primary_rows(self):
        payload = {
            "prompt_pack": "pack.jsonl",
            "generated_at": "2026-04-26T10:00:00",
            "config": {
                "top_k": 3,
                "category": None,
                "embed_url": None,
                "retrieval_profile_override": None,
            },
            "summary": {
                "total_prompts": 1,
                "expected_owner_rows": 1,
                "primary_expected_rows": 0,
                "retrieval_error_rows": 0,
                "expected_hit_at_1": {"text": "1/1 (100.0%)"},
                "expected_hit_at_3": {"text": "1/1 (100.0%)"},
                "expected_hit_at_k": {"text": "1/1 (100.0%)"},
                "expected_owner_best_rank": "1.00",
                "primary_hit_at_1": {"text": "unknown"},
                "primary_hit_at_3": {"text": "unknown"},
                "primary_hit_at_k": {"text": "unknown"},
                "primary_best_rank": "unknown",
                "simple_owner_share": {"text": "1/1 (100.0%)"},
                "retrieval_latency_ms": {"mean": 1.0, "p95": 1.0},
                "top_distractor_guide_ids": [],
            },
            "rows": [
                {
                    "prompt_index": 1,
                    "prompt_id": "P-1",
                    "expected_guide_ids": ["GD-120"],
                    "primary_expected_guide_ids": [],
                    "top_retrieved_guide_ids": ["GD-120"],
                    "expected_hit_at_1": True,
                    "expected_hit_at_3": True,
                    "expected_hit_at_k": True,
                    "expected_owner_best_rank": 1,
                    "owner_share": 1.0,
                }
            ],
        }

        markdown = self.module.render_markdown(payload)
        import scripts.validate_prompt_expectations as validator

        with tempfile.TemporaryDirectory() as tmpdir:
            md_path = Path(tmpdir) / "retrieval.md"
            md_path.write_text(markdown, encoding="utf-8")
            rows, _ = validator.load_retrieval_eval_markdown(
                md_path, root=Path(tmpdir)
            )
        self.assertEqual(rows[0].get("primary_expected_guide_ids"), None)
        self.assertEqual(rows[0].get("primary"), None)

        # Keep markdown readable but avoid emitting the primary alias that
        # validator parses as an explicit expectation field.
        self.assertIn("| # | id | expected | primary_guide_ids | top retrieved | hit@1", markdown)

    def test_render_markdown_sanitizes_row_breaking_cell_text(self):
        payload = {
            "prompt_pack": "pack.jsonl",
            "generated_at": "2026-04-26T10:00:00",
            "config": {
                "top_k": 1,
                "category": None,
                "embed_url": None,
                "retrieval_profile_override": None,
            },
            "summary": {
                "total_prompts": 1,
                "expected_owner_rows": 1,
                "primary_expected_rows": 0,
                "retrieval_error_rows": 1,
                "expected_hit_at_1": {"text": "0/1 (0.0%)"},
                "expected_hit_at_3": {"text": "0/1 (0.0%)"},
                "expected_hit_at_k": {"text": "0/1 (0.0%)"},
                "expected_owner_best_rank": "unknown",
                "primary_hit_at_1": {"text": "unknown"},
                "primary_hit_at_3": {"text": "unknown"},
                "primary_hit_at_k": {"text": "unknown"},
                "primary_best_rank": "unknown",
                "simple_owner_share": {"text": "0/0 (0.0%)"},
                "retrieval_latency_ms": {"mean": None, "p95": None},
                "top_distractor_guide_ids": [],
            },
            "rows": [
                {
                    "prompt_index": 1,
                    "prompt_id": "P-1\r\n| injected |",
                    "expected_guide_ids": ["GD-120"],
                    "primary_expected_guide_ids": [],
                    "top_retrieved_guide_ids": [],
                    "expected_hit_at_1": False,
                    "expected_hit_at_3": False,
                    "expected_hit_at_k": False,
                    "owner_share": 0.0,
                    "error": "RuntimeError: bad\r\n| row |",
                }
            ],
        }

        markdown = self.module.render_markdown(payload)
        row_lines = [
            line
            for line in markdown.splitlines()
            if line.startswith("| 1 |") or "injected" in line or "RuntimeError" in line
        ]

        self.assertEqual(len(row_lines), 1)
        self.assertIn("P-1  \\| injected \\|", row_lines[0])
        self.assertIn("RuntimeError: bad  \\| row \\|", row_lines[0])

    def test_evaluate_pack_deduplicates_expected_and_primary_ids(self):
        calls = []

        original = self.module.retrieve_for_prompt

        def fake_retrieve(
            prompt,
            collection,
            *,
            top_k,
            category,
            embed_url,
            retrieval_profile_override=None,
        ):
            calls.append(True)
            return ["GD-380"], {"retrieval_meta": {"fake": True}}

        self.module.retrieve_for_prompt = fake_retrieve
        try:
            rows = self.module.evaluate_pack(
                [
                    {
                        "line_number": 1,
                        "prompt_id": "P-1",
                        "section": "Section",
                        "prompt": "question?",
                        "expected_guide_ids": ["gd-024", "GD-024", "GD-120"],
                        "primary_expected_guide_ids": ["GD-380", "gd-380", "gd-381", "GD-381", "gd-381"],
                    }
                ],
                collection="fake-collection",
                top_k=3,
                category=None,
                embed_url=None,
            )
        finally:
            self.module.retrieve_for_prompt = original

        self.assertEqual(calls, [True])
        self.assertEqual(rows[0]["expected_guide_ids"], ["GD-024", "GD-120"])
        self.assertEqual(rows[0]["primary_expected_guide_ids"], ["GD-380", "GD-381"])

    def test_retrieve_for_prompt_profile_override_is_scoped(self):
        import bench
        import query

        original_retrieve = bench.retrieve_chunks
        original_profile = query._retrieval_profile_for_question
        seen_profiles = []

        def fake_profile(_question):
            return "original-profile"

        def fake_retrieve(prompt, collection, top_k, *, category, embed_url):
            seen_profiles.append(query._retrieval_profile_for_question(prompt))
            return {"metadatas": [[{"guide_id": "GD-123"}]]}, [], {"profile": seen_profiles[-1]}

        query._retrieval_profile_for_question = fake_profile
        bench.retrieve_chunks = fake_retrieve
        try:
            retrieved, meta = self.module.retrieve_for_prompt(
                "prompt",
                collection=object(),
                top_k=8,
                category=None,
                embed_url=None,
                retrieval_profile_override="forced-profile",
            )
        finally:
            bench.retrieve_chunks = original_retrieve
            restored = query._retrieval_profile_for_question
            query._retrieval_profile_for_question = original_profile

        self.assertEqual(retrieved, ["GD-123"])
        self.assertEqual(meta["retrieval_meta"]["profile"], "forced-profile")
        self.assertEqual(seen_profiles, ["forced-profile"])
        self.assertIs(restored, fake_profile)


if __name__ == "__main__":
    unittest.main()

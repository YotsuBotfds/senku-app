import json
import shutil
import unittest
import uuid
from pathlib import Path

from bench_artifact_tools import (
    build_eval_rows,
    compare_artifacts,
    parse_bench_markdown_responses,
)


class BenchArtifactToolsTests(unittest.TestCase):
    def make_tmpdir(self) -> Path:
        root = Path("artifacts") / "bench" / "bench_artifact_tools_unit_tests"
        root.mkdir(parents=True, exist_ok=True)
        path = root / uuid.uuid4().hex
        path.mkdir()
        self.addCleanup(lambda: shutil.rmtree(path, ignore_errors=True))
        return path

    def test_parse_bench_markdown_responses_extracts_answer_body(self):
        markdown = """# Report

## 1. What now?
*Section: Core Regression*
*Decision path: rag*
*Generation: 1.0s | Tokens: 100->50 | Chunks: 24 | Server: local*
*Runtime: finish=stop | retries=0 | cap_hit=no | cap=768*

First line of answer.
Second line of answer.

**Sources:**
  - guide
"""
        responses = parse_bench_markdown_responses(markdown)
        self.assertEqual(
            responses[1],
            "First line of answer.\nSecond line of answer.",
        )

    def test_parse_bench_markdown_responses_stops_at_padded_source_headers(self):
        markdown = """# Report

## 1. What now?
*Section: Core Regression*

Answer before cited sources.

  **Sources:**
  - cited guide that should not become answer text

## 2. What else?
*Section: Core Regression*

Answer before retrieved context.

  **Retrieved Context (not explicitly cited):**
  - retrieved guide that should not become answer text
"""

        responses = parse_bench_markdown_responses(markdown)

        self.assertEqual(responses[1], "Answer before cited sources.")
        self.assertEqual(responses[2], "Answer before retrieved context.")

    def test_build_eval_rows_falls_back_to_markdown_response_text(self):
        root = self.make_tmpdir()
        json_path = root / "bench_sample.json"
        md_path = root / "bench_sample.md"
        json_path.write_text(
            json.dumps(
                {
                    "timestamp": "2026-04-10T00:00:00",
                    "config": {"model": "test-model", "mode": "default"},
                    "summary": {"total_prompts": 1},
                    "results": [
                        {
                            "index": 1,
                            "section": "Core Regression",
                            "question": "What now?",
                            "decision_path": "rag",
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-001"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-001", "GD-002"]
                            },
                            "response_text": "",
                        }
                    ],
                }
            ),
            encoding="utf-8",
        )
        md_path.write_text(
            """# Report

## 1. What now?
*Section: Core Regression*

Recovered from markdown.

**Sources:**
  - guide
""",
            encoding="utf-8",
        )

        rows = build_eval_rows([json_path])

        self.assertEqual(rows[0]["response_text"], "Recovered from markdown.")
        self.assertEqual(rows[0]["cited_guide_ids"], "GD-001")
        self.assertEqual(rows[0]["top_retrieved_guide_ids"], "GD-001,GD-002")

    def test_build_eval_rows_sanitizes_guide_id_lists(self):
        root = self.make_tmpdir()
        json_path = root / "bench_sample.json"
        json_path.write_text(
            json.dumps(
                {
                    "config": {},
                    "summary": {"total_prompts": 1},
                    "results": [
                        {
                            "index": 1,
                            "section": "Core Regression",
                            "question": "What now?",
                            "cited_guide_ids": [" GD-001 ", None, 42, ""],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": [" GD-002 ", None, 43, ""]
                            },
                            "response_text": "Answer.",
                        }
                    ],
                }
            ),
            encoding="utf-8",
        )

        rows = build_eval_rows([json_path])

        self.assertEqual(rows[0]["cited_guide_ids"], "GD-001,42")
        self.assertEqual(rows[0]["top_retrieved_guide_ids"], "GD-002,43")

    def test_build_eval_rows_supports_standalone_markdown_artifact(self):
        root = self.make_tmpdir()
        md_path = root / "bench_sample.md"
        md_path.write_text(
            """# Report

## 1. What now?
*Section: Core Regression*

Recovered from markdown only.
""",
            encoding="utf-8",
        )

        rows = build_eval_rows([md_path])

        self.assertEqual(len(rows), 1)
        self.assertEqual(rows[0]["section"], "Core Regression")
        self.assertEqual(rows[0]["question"], "What now?")
        self.assertEqual(rows[0]["response_text"], "Recovered from markdown only.")

    def test_compare_artifacts_reports_prompt_level_deltas(self):
        root = self.make_tmpdir()
        baseline = root / "baseline.json"
        candidate = root / "candidate.json"

        baseline.write_text(
            json.dumps(
                {
                    "config": {"model": "baseline-model"},
                    "summary": {
                        "errors": 0,
                        "duplicate_citation_total": 1,
                        "cap_hit_prompts": 0,
                        "wall_duration": 10,
                    },
                    "results": [
                        {
                            "index": 1,
                            "section": "Core Regression",
                            "question": "Prompt A",
                            "decision_path": "rag",
                            "source_mode": "cited",
                            "generation_time": 4.0,
                            "duplicate_citation_count": 1,
                            "completion_cap_hit": False,
                            "finish_reason": "stop",
                            "error": None,
                        }
                    ],
                }
            ),
            encoding="utf-8",
        )
        candidate.write_text(
            json.dumps(
                {
                    "config": {"model": "candidate-model"},
                    "summary": {
                        "errors": 1,
                        "duplicate_citation_total": 4,
                        "cap_hit_prompts": 1,
                        "wall_duration": 12,
                    },
                    "results": [
                        {
                            "index": 1,
                            "section": "Core Regression",
                            "question": "Prompt A",
                            "decision_path": "no-rag",
                            "source_mode": "retrieved",
                            "generation_time": 6.5,
                            "duplicate_citation_count": 4,
                            "completion_cap_hit": True,
                            "finish_reason": "length",
                            "error": "boom",
                            "error_category": "runtime_400",
                        }
                    ],
                }
            ),
            encoding="utf-8",
        )

        comparison = compare_artifacts(baseline, candidate)

        self.assertEqual(comparison["prompt_overlap"], 1)
        self.assertEqual(len(comparison["error_regressions"]), 1)
        self.assertEqual(len(comparison["decision_path_changes"]), 1)
        self.assertEqual(len(comparison["source_mode_changes"]), 1)
        self.assertEqual(comparison["replay_packs"]["errors"], ["Prompt A"])

    def test_compare_artifacts_keeps_duplicate_questions_by_index(self):
        root = self.make_tmpdir()
        baseline = root / "baseline.json"
        candidate = root / "candidate.json"

        payload = {
            "config": {},
            "summary": {},
            "results": [
                {
                    "index": 1,
                    "section": "Core Regression",
                    "question": "Prompt A",
                    "decision_path": "rag",
                    "source_mode": "cited",
                    "generation_time": 4.0,
                    "duplicate_citation_count": 0,
                    "completion_cap_hit": False,
                    "finish_reason": "stop",
                    "error": None,
                },
                {
                    "index": 2,
                    "section": "Core Regression",
                    "question": "Prompt A",
                    "decision_path": "rag",
                    "source_mode": "cited",
                    "generation_time": 5.0,
                    "duplicate_citation_count": 0,
                    "completion_cap_hit": False,
                    "finish_reason": "stop",
                    "error": None,
                },
            ],
        }
        baseline.write_text(json.dumps(payload), encoding="utf-8")
        candidate.write_text(json.dumps(payload), encoding="utf-8")

        comparison = compare_artifacts(baseline, candidate)

        self.assertEqual(comparison["prompt_overlap"], 2)
        self.assertEqual(len(comparison["row_deltas"]), 2)


if __name__ == "__main__":
    unittest.main()

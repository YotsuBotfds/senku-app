import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts import export_rag_eval_dataset as exporter


REPO_ROOT = Path(__file__).resolve().parents[1]
PYTHON = REPO_ROOT / ".venvs" / "senku-validate" / "Scripts" / "python.exe"


def read_jsonl(path: Path) -> list[dict]:
    return [
        json.loads(line)
        for line in path.read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]


class ExportRagEvalDatasetTests(unittest.TestCase):
    def test_bench_only_exports_stable_rag_eval_fields(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "sample.json"
            bench.write_text(
                json.dumps(
                    {
                        "results": [
                            {
                                "index": 1,
                                "question": "How do I clean a small wound?",
                                "response_text": "Rinse it and watch for infection. [GD-111]",
                                "section": "Wound",
                                "prompt_id": "W-1",
                                "decision_path": "rag",
                                "source_mode": "cited",
                                "cited_guide_ids": ["GD-111"],
                                "retrieval_metadata": {
                                    "top_retrieved_guide_ids": ["GD-111", "GD-222"],
                                    "source_candidates": [
                                        {
                                            "guide_id": "GD-111",
                                            "title": "Wound Care",
                                            "section": "Cleaning",
                                            "category": "medical",
                                        }
                                    ],
                                },
                                "prompt_metadata": {
                                    "ground_truth": "Use clean water and monitor infection signs.",
                                },
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            records = exporter.build_records(bench_paths=[bench], diagnostics_paths=[])

        self.assertEqual(len(records), 1)
        record = records[0]
        self.assertEqual(
            list(record.keys()),
            ["question", "answer", "contexts", "ground_truth", "metadata"],
        )
        self.assertEqual(record["question"], "How do I clean a small wound?")
        self.assertEqual(record["answer"], "Rinse it and watch for infection. [GD-111]")
        self.assertEqual(record["contexts"], ["GD-111 Wound Care -> Cleaning (medical)"])
        self.assertEqual(
            record["ground_truth"], "Use clean water and monitor infection signs."
        )
        self.assertEqual(record["metadata"]["cited_guide_ids"], ["GD-111"])
        self.assertEqual(
            record["metadata"]["top_retrieved_guide_ids"], ["GD-111", "GD-222"]
        )

    def test_bench_only_contexts_fall_back_to_top_retrieved_ids(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "ids_only.json"
            bench.write_text(
                json.dumps(
                    {
                        "results": [
                            {
                                "index": 1,
                                "question": "ids?",
                                "response_text": "answer",
                                "retrieval_metadata": {
                                    "top_retrieved_guide_ids": ["GD-010", "GD-011"],
                                },
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            records = exporter.build_records(bench_paths=[bench], diagnostics_paths=[])

        self.assertEqual(records[0]["contexts"], ["GD-010", "GD-011"])

    def test_split_guide_ids_deduplicates_nested_and_comma_values(self):
        self.assertEqual(
            exporter.split_guide_ids(
                {
                    "primary": ["GD-010, GD-011", {"fallback": "GD-010|unknown"}],
                    "secondary": "GD-012",
                }
            ),
            ["GD-010", "GD-011", "GD-012"],
        )

    def test_split_guide_ids_normalizes_mixed_whitespace_shapes(self):
        self.assertEqual(
            exporter.split_guide_ids(
                {
                    "primary": "GD-020\tGD-021\n none",
                    "secondary": ["GD-022 GD-020", {"extra": "unknown\r\nGD-023"}],
                }
            ),
            ["GD-020", "GD-021", "GD-022", "GD-023"],
        )

    def test_diagnostics_metadata_normalizes_nested_guide_id_values(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            diagnostics = Path(tmpdir) / "diagnostics.json"
            diagnostics.write_text(
                json.dumps(
                    {
                        "rows": [
                            {
                                "artifact_name": "diag_only.json",
                                "prompt_index": 4,
                                "prompt_text": "Normalize nested ids?",
                                "cited_guide_ids": [
                                    " GD-001 ",
                                    "unknown",
                                    {"extra": "GD-002, GD-001"},
                                ],
                                "top_retrieved_guide_ids": "GD-003| none |GD-004,GD-003",
                                "expected_guide_ids": {
                                    "primary": ["GD-005", "null", "GD-006|GD-005"]
                                },
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            records = exporter.build_records(
                bench_paths=[],
                diagnostics_paths=[diagnostics],
            )

        metadata = records[0]["metadata"]
        self.assertEqual(metadata["cited_guide_ids"], ["GD-001", "GD-002"])
        self.assertEqual(metadata["top_retrieved_guide_ids"], ["GD-003", "GD-004"])
        self.assertEqual(metadata["expected_guide_ids"], ["GD-005", "GD-006"])

    def test_diagnostics_enriches_bench_row_with_bucket_and_status_metadata(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "run.json"
            diagnostics_dir = root / "diag"
            diagnostics_dir.mkdir()
            bench.write_text(
                json.dumps(
                    {
                        "results": [
                            {
                                "index": 2,
                                "question": "Can I take unknown pills?",
                                "response_text": "Do not take unknown pills. [GD-301]",
                                "retrieval_metadata": {
                                    "top_retrieved_guide_ids": ["GD-301"],
                                },
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )
            (diagnostics_dir / "diagnostics.json").write_text(
                json.dumps(
                    {
                        "rows": [
                            {
                                "artifact_name": "run.json",
                                "artifact_path": str(bench),
                                "prompt_index": 2,
                                "prompt_text": "Can I take unknown pills?",
                                "suspected_failure_bucket": "expected_supported",
                                "answer_card_status": "pass",
                                "app_gate_status": "pass",
                                "app_acceptance_status": "pass",
                                "app_acceptance_root_cause": "supported",
                                "safety_surface_status": "not_safety_critical",
                                "ui_surface_bucket": "standard",
                                "evidence_owner_status": "expected_owner_cited",
                                "claim_support_status": "pass",
                                "cited_guide_ids": "GD-301",
                                "top_retrieved_guide_ids": "GD-301|GD-999",
                                "expected_guide_ids": "GD-301",
                                "expected_guide_family": "poisoning",
                                "short_reason": "owner cited",
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            records = exporter.build_records(
                bench_paths=[bench],
                diagnostics_paths=[diagnostics_dir],
            )

        metadata = records[0]["metadata"]
        self.assertEqual(records[0]["ground_truth"], "expected guides: GD-301; family: poisoning")
        self.assertEqual(metadata["suspected_failure_bucket"], "expected_supported")
        self.assertEqual(metadata["answer_card_status"], "pass")
        self.assertEqual(metadata["app_acceptance_status"], "pass")
        self.assertEqual(metadata["app_acceptance_root_cause"], "supported")
        self.assertEqual(metadata["safety_surface_status"], "not_safety_critical")
        self.assertEqual(metadata["ui_surface_bucket"], "standard")
        self.assertEqual(metadata["expected_guide_ids"], ["GD-301"])
        self.assertEqual(metadata["top_retrieved_guide_ids"], ["GD-301", "GD-999"])

    def test_diagnostics_only_row_exports_empty_answer_with_context_ids(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            diagnostics = Path(tmpdir) / "diagnostics.json"
            diagnostics.write_text(
                json.dumps(
                    {
                        "rows": [
                            {
                                "artifact_name": "diag_only.json",
                                "prompt_index": 4,
                                "prompt_text": "Where is the owner guide?",
                                "top_retrieved_guide_ids": "GD-010|GD-011",
                                "cited_guide_ids": "",
                                "suspected_failure_bucket": "retrieval_miss",
                                "app_acceptance_root_cause": "evidence_owner",
                                "safety_surface_status": "emergency_first_supported",
                                "ui_surface_bucket": "emergency_first",
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            records = exporter.build_records(
                bench_paths=[],
                diagnostics_paths=[diagnostics],
            )

        self.assertEqual(records[0]["question"], "Where is the owner guide?")
        self.assertEqual(records[0]["answer"], "")
        self.assertEqual(records[0]["contexts"], ["GD-010", "GD-011"])
        self.assertEqual(records[0]["metadata"]["suspected_failure_bucket"], "retrieval_miss")
        self.assertEqual(records[0]["metadata"]["app_acceptance_root_cause"], "evidence_owner")
        self.assertEqual(
            records[0]["metadata"]["safety_surface_status"],
            "emergency_first_supported",
        )
        self.assertEqual(records[0]["metadata"]["ui_surface_bucket"], "emergency_first")

    def test_repeatable_trace_jsonl_adds_span_summary(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "trace_run.json"
            trace_a = root / "trace_a.jsonl"
            trace_b = root / "trace_b.jsonl"
            bench.write_text(
                json.dumps(
                    {
                        "results": [
                            {
                                "index": 1,
                                "question": "trace?",
                                "response_text": "answer",
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )
            trace_a.write_text(
                json.dumps(
                    {
                        "name": "bench.retrieve",
                        "trace_id": "t1",
                        "span_id": "s1",
                        "start_time_unix_nano": 1_000_000,
                        "end_time_unix_nano": 3_000_000,
                        "status": {"code": "OK"},
                        "attributes": {
                            "artifact_name": "trace_run.json",
                            "prompt_index": 1,
                            "phase": "retrieve",
                        },
                    }
                )
                + "\n",
                encoding="utf-8",
            )
            trace_b.write_text(
                json.dumps(
                    {
                        "name": "bench.generate",
                        "trace_id": "t1",
                        "span_id": "s2",
                        "start_time_unix_nano": 3_000_000,
                        "end_time_unix_nano": 8_000_000,
                        "status": {"code": "ERROR"},
                        "attributes": {
                            "artifact_name": "trace_run.json",
                            "prompt_index": 1,
                            "phase": "generate",
                        },
                    }
                )
                + "\n",
                encoding="utf-8",
            )

            records = exporter.build_records(
                bench_paths=[bench],
                diagnostics_paths=[],
                trace_paths=[trace_a, trace_b],
            )

        trace = records[0]["metadata"]["trace"]
        self.assertEqual(trace["span_count"], 2)
        self.assertEqual(trace["error_phases"], ["generate"])
        self.assertEqual(trace["duration_ms_by_phase"], {"generate": 5.0, "retrieve": 2.0})
        self.assertEqual([span["phase"] for span in trace["spans"]], ["retrieve", "generate"])

    def test_trace_summary_uses_name_phase_and_top_level_identity(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "trace_run.json"
            trace_path = root / "trace.jsonl"
            bench.write_text(
                json.dumps(
                    {
                        "results": [
                            {
                                "index": 1,
                                "question": "trace?",
                                "response_text": "answer",
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )
            trace_path.write_text(
                json.dumps(
                    {
                        "name": "rag.retrieve",
                        "artifact_name": "trace_run.json",
                        "prompt_index": 1,
                        "duration_ms": "2.3456",
                        "status": "FAIL",
                        "trace_id": "t1",
                        "span_id": "s1",
                    }
                )
                + "\n",
                encoding="utf-8",
            )

            records = exporter.build_records(
                bench_paths=[bench],
                diagnostics_paths=[],
                trace_paths=[trace_path],
            )

        trace = records[0]["metadata"]["trace"]
        self.assertEqual(trace["error_phases"], ["retrieve"])
        self.assertEqual(trace["duration_ms_by_phase"], {"retrieve": 2.346})
        self.assertEqual(trace["spans"][0]["phase"], "retrieve")
        self.assertEqual(trace["spans"][0]["duration_ms"], 2.346)

    def test_cli_writes_jsonl(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            bench = root / "cli.json"
            output = root / "dataset.jsonl"
            bench.write_text(
                json.dumps(
                    {
                        "results": [
                            {
                                "index": 1,
                                "question": "CLI?",
                                "response_text": "yes",
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            subprocess.run(
                [
                    str(PYTHON if PYTHON.exists() else sys.executable),
                    "-B",
                    str(REPO_ROOT / "scripts" / "export_rag_eval_dataset.py"),
                    "--bench-json",
                    str(bench),
                    "--output-jsonl",
                    str(output),
                ],
                cwd=REPO_ROOT,
                check=True,
            )

            rows = read_jsonl(output)

        self.assertEqual(rows[0]["question"], "CLI?")
        self.assertEqual(rows[0]["answer"], "yes")


if __name__ == "__main__":
    unittest.main()

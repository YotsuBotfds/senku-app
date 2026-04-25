import json
import shutil
import unittest
import uuid
import csv
from pathlib import Path

from scripts.analyze_rag_bench_failures import (
    analyze,
    build_rows,
    load_expectations,
    summarize,
    write_markdown,
)


class AnalyzeRagBenchFailuresTests(unittest.TestCase):
    def make_tmpdir(self) -> Path:
        root = Path("artifacts") / "bench" / "rag_diag_unit_tests"
        root.mkdir(parents=True, exist_ok=True)
        path = root / uuid.uuid4().hex
        path.mkdir()
        self.addCleanup(lambda: shutil.rmtree(path, ignore_errors=True))
        return path

    def test_expected_guide_metadata_drives_hit_and_ranking_bucket(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "How should I treat this?",
                            "prompt_metadata": {"expected_guide_ids": "GD-002"},
                            "decision_path": "rag",
                            "generation_time": 1.2,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-002"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-001", "GD-002"]
                            },
                            "response_text": "Use the expected guide. [GD-002]",
                            "completion_cap_hit": False,
                            "finish_reason": "stop",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        rows = build_rows([artifact])

        self.assertEqual(rows[0]["expected_hit_at_1"], "no")
        self.assertEqual(rows[0]["expected_hit_at_3"], "yes")
        self.assertEqual(rows[0]["expected_cited"], "yes")
        self.assertEqual(rows[0]["suspected_failure_bucket"], "ranking_miss")

    def test_answer_card_diagnostics_prefer_expected_guides(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-222"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-222", "GD-111"]
                            },
                            "response_text": "Call 911. [GD-222]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "card-expected",
                "guide_id": "GD-111",
                "risk_tier": "high",
                "required_first_actions": ["Call 911"],
                "forbidden_advice": [],
            },
            {
                "card_id": "card-cited",
                "guide_id": "GD-222",
                "risk_tier": "high",
                "required_first_actions": ["Use epinephrine"],
                "forbidden_advice": ["Call 911"],
            },
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["answer_card_status"], "pass")
        self.assertEqual(rows[0]["answer_card_ids"], "card-expected")
        self.assertEqual(rows[0]["answer_card_required_hits"], "Call 911")
        self.assertEqual(rows[0]["answer_card_forbidden_hits"], "")
        self.assertEqual(rows[0]["suspected_failure_bucket"], "ranking_miss")
        self.assertEqual(rows[0]["claim_support_status"], "pass")
        self.assertEqual(rows[0]["claim_action_count"], 1)
        self.assertEqual(rows[0]["claim_supported_count"], 1)
        self.assertEqual(rows[0]["claim_unknown_count"], 0)
        self.assertEqual(rows[0]["claim_forbidden_count"], 0)
        self.assertEqual(rows[0]["claim_support_basis"], "card_required_action:1")

    def test_shadow_card_answer_diagnostics_do_not_change_actual_bucket(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "They cannot breathe and look blue.",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"]
                            },
                            "response_text": "Call 911. [GD-111]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "airway_shadow",
                "guide_id": "GD-111",
                "risk_tier": "critical",
                "required_first_actions": ["Call 911", "Keep the airway open"],
                "first_actions": ["Monitor breathing"],
                "urgent_red_flags": ["Blue lips"],
                "forbidden_advice": ["Do not wait overnight"],
                "do_not": [],
                "source_sections": [{"guide": "GD-111"}],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["suspected_failure_bucket"], "expected_supported")
        self.assertEqual(rows[0]["answer_card_status"], "partial")
        self.assertEqual(rows[0]["shadow_card_answer_status"], "pass")
        self.assertEqual(rows[0]["shadow_claim_support_status"], "pass")
        self.assertEqual(rows[0]["shadow_card_answer_cited_guide_ids"], "GD-111")
        self.assertIn("Keep the airway open", rows[0]["shadow_card_answer_text"])

    def test_bare_meningitis_comparison_does_not_require_strict_emergency_card(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "is this meningitis or a viral illness",
                            "prompt_metadata": {"expected_guide_ids": "GD-589"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-589"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-589", "GD-284"],
                                "retrieval_profile": "compare_or_boundary",
                            },
                            "response_text": (
                                "I cannot diagnose from that label alone.\n"
                                "1. Check for stiff neck, severe headache, confusion, "
                                "hard-to-wake behavior, or a non-blanching rash. [GD-589]\n"
                                "2. If any are present, seek emergency clinical evaluation. [GD-589]\n"
                                "3. If those are absent and symptoms are mild, viral illness "
                                "is possible; monitor for red flags. [GD-589]"
                            ),
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "meningitis_sepsis_child",
                "guide_id": "GD-589",
                "slug": "sepsis-recognition-antibiotic-protocols",
                "title": "Sepsis Recognition, Escalation & Empiric Antibiotic Protocols",
                "risk_tier": "critical",
                "required_first_actions": [
                    "Treat fever plus meningitis or brain-warning signs as suspected meningitis or meningococcemia.",
                    "Escalate urgently for emergency medical evaluation and antibiotic-capable care.",
                ],
                "first_actions": ["Check mental status."],
                "urgent_red_flags": ["Fever with stiff neck."],
                "forbidden_advice": [],
                "do_not": [],
                "source_sections": [{"guide": "GD-589"}],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)
        summary = summarize(rows)

        self.assertEqual(rows[0]["answer_card_status"], "not_applicable_compare")
        self.assertEqual(rows[0]["answer_card_missing_required"], "")
        self.assertEqual(rows[0]["app_acceptance_status"], "strong_supported")
        self.assertEqual(summary["generated_shadow_card_gap_rows"], 0)

    def test_red_flag_meningitis_prompt_still_uses_strict_emergency_card(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "fever stiff neck and purple rash that does not fade",
                            "prompt_metadata": {"expected_guide_ids": "GD-589"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-589"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-589"],
                            },
                            "response_text": "Watch symptoms closely. [GD-589]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "meningitis_sepsis_child",
                "guide_id": "GD-589",
                "risk_tier": "critical",
                "required_first_actions": [
                    "Treat fever plus meningitis or brain-warning signs as suspected meningitis or meningococcemia.",
                    "Escalate urgently for emergency medical evaluation and antibiotic-capable care.",
                ],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": [],
                "do_not": [],
                "source_sections": [{"guide": "GD-589"}],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertIn(rows[0]["answer_card_status"], {"partial", "fail"})
        self.assertIn(
            "Escalate urgently for emergency medical evaluation",
            rows[0]["answer_card_missing_required"],
        )

    def test_stiff_neck_meningitis_compare_still_counts_strict_emergency_gaps(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "fever with stiff neck: meningitis vs viral illness?",
                            "prompt_metadata": {"expected_guide_ids": "GD-589"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-589"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-589", "GD-284"],
                                "retrieval_profile": "compare_or_boundary",
                            },
                            "response_text": "It may be viral, but keep watching symptoms. [GD-589]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "meningitis_sepsis_child",
                "guide_id": "GD-589",
                "risk_tier": "critical",
                "required_first_actions": [
                    "Treat fever plus meningitis or brain-warning signs as suspected meningitis or meningococcemia.",
                    "Escalate urgently for emergency medical evaluation and antibiotic-capable care.",
                ],
                "first_actions": [],
                "urgent_red_flags": ["Fever with stiff neck."],
                "forbidden_advice": [],
                "do_not": [],
                "source_sections": [{"guide": "GD-589"}],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)
        summary = summarize(rows)

        self.assertNotEqual(rows[0]["answer_card_status"], "not_applicable_compare")
        self.assertIn(rows[0]["answer_card_status"], {"partial", "fail"})
        self.assertIn(
            "Escalate urgently for emergency medical evaluation",
            rows[0]["answer_card_missing_required"],
        )
        self.assertEqual(summary["generated_shadow_card_gap_rows"], 1)
        self.assertIn(
            (
                "Escalate urgently for emergency medical evaluation and "
                "antibiotic-capable care.",
                1,
            ),
            summary["top_generated_shadow_missing_required"],
        )

    def test_card_backed_runtime_rows_are_evaluable_without_generation_time(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "They cannot breathe and look blue.",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "card_backed_runtime",
                            "answer_provenance": "reviewed_card_runtime",
                            "reviewed_card_backed": True,
                            "reviewed_card_ids": ["airway_shadow"],
                            "reviewed_card_review_status": "pilot_reviewed",
                            "reviewed_card_guide_ids": ["GD-111"],
                            "generation_time": 0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"],
                                "safety_critical": True,
                            },
                            "response_text": "Call 911. Keep the airway open. [GD-111]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "airway_shadow",
                "guide_id": "GD-111",
                "risk_tier": "critical",
                "required_first_actions": ["Call 911", "Keep the airway open"],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": [],
                "do_not": [],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)
        summary = summarize(rows)

        self.assertEqual(rows[0]["generated"], "no")
        self.assertEqual(rows[0]["answer_provenance"], "reviewed_card_runtime")
        self.assertEqual(rows[0]["reviewed_card_backed"], "yes")
        self.assertEqual(rows[0]["answer_surface_label"], "reviewed_card_evidence")
        self.assertEqual(rows[0]["reviewed_card_ids"], "airway_shadow")
        self.assertEqual(rows[0]["reviewed_card_review_status"], "pilot_reviewed")
        self.assertEqual(rows[0]["reviewed_card_guide_ids"], "GD-111")
        self.assertEqual(rows[0]["answer_card_status"], "pass")
        self.assertEqual(rows[0]["claim_support_status"], "pass")
        self.assertEqual(summary["generation_workload"]["generated"], 0)
        self.assertEqual(summary["reviewed_card_backed_rows"], 1)
        self.assertEqual(
            summary["answer_provenance_counts"],
            {"reviewed_card_runtime": 1},
        )
        self.assertEqual(
            summary["answer_surface_label_counts"],
            {"reviewed_card_evidence": 1},
        )
        self.assertEqual(summary["answer_card_counts"], {"pass": 1})
        self.assertEqual(summary["claim_support_counts"], {"pass": 1})

    def test_provenance_distinguishes_reviewed_card_from_generated_strong_answer(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "They cannot breathe and look blue.",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "card_backed_runtime",
                            "answer_provenance": "reviewed_card_runtime",
                            "reviewed_card_backed": True,
                            "reviewed_card_ids": ["airway_shadow"],
                            "reviewed_card_review_status": "approved",
                            "reviewed_card_guide_ids": ["GD-111"],
                            "generation_time": 0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"],
                                "support_strength": "strong",
                            },
                            "response_text": "Call 911. Keep the airway open. [GD-111]",
                        },
                        {
                            "index": 2,
                            "question": "They cannot breathe and look blue.",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "support_strength": "strong",
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"],
                            },
                            "response_text": "Call 911. Keep the airway open. [GD-111]",
                        },
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "airway_shadow",
                "guide_id": "GD-111",
                "risk_tier": "critical",
                "required_first_actions": ["Call 911", "Keep the airway open"],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": [],
                "do_not": [],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)
        summary = summarize(rows)

        self.assertEqual(rows[0]["answer_provenance"], "reviewed_card_runtime")
        self.assertEqual(rows[0]["answer_surface_label"], "reviewed_card_evidence")
        self.assertEqual(rows[1]["answer_provenance"], "generated_model")
        self.assertEqual(rows[1]["answer_surface_label"], "generated_evidence")
        self.assertEqual(
            summary["answer_provenance_counts"],
            {"reviewed_card_runtime": 1, "generated_model": 1},
        )
        self.assertEqual(
            summary["answer_surface_label_counts"],
            {"reviewed_card_evidence": 1, "generated_evidence": 1},
        )

    def test_summary_counts_generated_shadow_card_gaps(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        output_dir = root / "diagnostics"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "They cannot breathe and look blue.",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"]
                            },
                            "response_text": "Call 911. [GD-111]",
                        },
                        {
                            "index": 2,
                            "question": "They are already breathing normally.",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"]
                            },
                            "response_text": "Call 911. Keep the airway open. [GD-111]",
                        },
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "airway_shadow",
                "guide_id": "GD-111",
                "risk_tier": "critical",
                "required_first_actions": ["Call 911", "Keep the airway open"],
                "first_actions": ["Monitor breathing"],
                "urgent_red_flags": ["Blue lips"],
                "forbidden_advice": [],
                "do_not": [],
                "source_sections": [{"guide": "GD-111"}],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)
        summary = summarize(rows)

        self.assertEqual(summary["generated_shadow_card_gap_rows"], 1)
        self.assertEqual(
            summary["top_generated_shadow_missing_required"],
            [("Keep the airway open", 1)],
        )
        self.assertEqual(
            summary["generated_shadow_card_gap_examples"][0]["missing_required"],
            "Keep the airway open",
        )

        output_dir.mkdir()
        write_markdown(rows, summary, output_dir / "report.md")
        report = (output_dir / "report.md").read_text(encoding="utf-8")
        self.assertIn("## Generated vs Shadow Card Gaps", report)
        self.assertIn("Keep the airway open", report)

    def test_claim_support_diagnostics_report_unknown_and_forbidden_actions(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"]
                            },
                            "response_text": "- Call 911.\n- Wait overnight.",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "card-expected",
                "guide_id": "GD-111",
                "risk_tier": "high",
                "required_first_actions": ["Call 911"],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": ["Wait overnight"],
            },
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["claim_support_status"], "fail")
        self.assertEqual(rows[0]["claim_action_count"], 2)
        self.assertEqual(rows[0]["claim_supported_count"], 1)
        self.assertEqual(rows[0]["claim_unknown_count"], 0)
        self.assertEqual(rows[0]["claim_forbidden_count"], 1)
        self.assertEqual(
            rows[0]["claim_support_basis"],
            "card_forbidden_advice:1|card_required_action:1",
        )

    def test_answer_card_diagnostics_do_not_fail_non_generated_uncertainty(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {"expected_guide_ids": "GD-333"},
                            "decision_path": "uncertain_fit",
                            "generation_time": 0,
                            "source_mode": "retrieved",
                            "cited_guide_ids": [],
                            "retrieval_metadata": {
                                "answer_mode": "uncertain_fit",
                                "top_retrieved_guide_ids": ["GD-333"],
                            },
                            "response_text": "",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "card-uncertain",
                "guide_id": "GD-333",
                "risk_tier": "critical",
                "required_first_actions": ["Call 911"],
                "forbidden_advice": ["Wait overnight"],
            }
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["answer_card_status"], "no_generated_answer")
        self.assertEqual(rows[0]["answer_card_ids"], "card-uncertain")
        self.assertEqual(rows[0]["answer_card_missing_required"], "")
        self.assertEqual(rows[0]["suspected_failure_bucket"], "abstain_or_clarify_needed")

    def test_answer_card_diagnostics_use_family_to_filter_shared_backup_guides(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {
                                "expected_guide_ids": "GD-111|GD-222",
                                "expected_guide_family": "target_family",
                            },
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111", "GD-222"]
                            },
                            "response_text": "Call 911. [GD-111]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "target_family_card",
                "guide_id": "GD-111",
                "risk_tier": "high",
                "required_first_actions": ["Call 911"],
                "forbidden_advice": [],
            },
            {
                "card_id": "unrelated_backup_card",
                "guide_id": "GD-222",
                "risk_tier": "high",
                "required_first_actions": ["Use epinephrine"],
                "forbidden_advice": ["Call 911"],
            },
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["answer_card_status"], "pass")
        self.assertEqual(rows[0]["answer_card_ids"], "target_family_card")
        self.assertEqual(rows[0]["answer_card_forbidden_hits"], "")

    def test_answer_card_diagnostics_choose_strongest_family_match(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {
                                "expected_guide_ids": "GD-111|GD-222",
                                "expected_guide_family": "newborn_sepsis",
                            },
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111", "GD-222"]
                            },
                            "response_text": "Keep the newborn warm. [GD-111]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "newborn_danger_sepsis",
                "guide_id": "GD-111",
                "risk_tier": "high",
                "required_first_actions": ["Keep the newborn warm"],
                "forbidden_advice": [],
            },
            {
                "card_id": "meningitis_sepsis_child",
                "guide_id": "GD-222",
                "risk_tier": "high",
                "required_first_actions": ["Escalate for meningitis"],
                "forbidden_advice": ["Keep the newborn warm"],
            },
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["answer_card_status"], "pass")
        self.assertEqual(rows[0]["answer_card_ids"], "newborn_danger_sepsis")

    def test_answer_card_diagnostics_pass_prompt_text_for_conditional_branches(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "The newborn cord stump has pus and spreading redness.",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-111"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111"],
                                "safety_critical": True,
                            },
                            "response_text": "Get emergency help now. [GD-111]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "newborn_danger_sepsis",
                "guide_id": "GD-111",
                "risk_tier": "critical",
                "required_first_actions": ["Get emergency help now"],
                "conditional_required_actions": [
                    {
                        "trigger_terms": ["cord", "pus", "spreading redness"],
                        "required_actions": ["Seek antibiotic-capable care"],
                    }
                ],
                "forbidden_advice": [],
            },
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["answer_card_status"], "partial")
        self.assertEqual(rows[0]["app_acceptance_status"], "moderate_supported")
        self.assertIn(
            "Seek antibiotic-capable care",
            rows[0]["answer_card_missing_required"],
        )

    def test_markdown_source_ids_are_used_when_json_lacks_ranked_candidates(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        markdown = root / "sample.md"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {"expected_guide_id": "GD-777"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": [],
                            "retrieval_metadata": {},
                            "response_text": "Answer.",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        markdown.write_text(
            """# Report

## 1. Prompt

Answer.

**Sources:**
  - GD-777 (deterministic citation; no retrieved chunk)
""",
            encoding="utf-8",
        )

        rows = build_rows([artifact])

        self.assertEqual(rows[0]["top_retrieved_guide_ids"], "GD-777")
        self.assertEqual(rows[0]["expected_hit_at_1"], "yes")
        self.assertEqual(rows[0]["suspected_failure_bucket"], "generation_miss")

    def test_expected_supported_bucket_separates_success_from_unknown(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {"expected_guide_id": "GD-777"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-777"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-777", "GD-888"]
                            },
                            "response_text": "Answer. [GD-777]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        rows = build_rows([artifact])

        self.assertEqual(rows[0]["suspected_failure_bucket"], "expected_supported")

    def test_manifest_expectations_apply_by_wave_artifact_name(self):
        root = self.make_tmpdir()
        artifact = root / "guide_wave_zz_20260424_120000.json"
        manifest = root / "expectations.yaml"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Prompt",
                            "prompt_metadata": {},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-002"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-001", "GD-002"]
                            },
                            "response_text": "Answer. [GD-002]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )
        manifest.write_text(
            """
waves:
  zz:
    topic: test_family
    expected_guides:
      - id: GD-002
""".lstrip(),
            encoding="utf-8",
        )

        rows = build_rows([artifact], expectations=load_expectations(manifest))

        self.assertEqual(rows[0]["expected_guide_ids"], "GD-002")
        self.assertEqual(rows[0]["expected_guide_family"], "test_family")
        self.assertEqual(rows[0]["expected_hit_at_1"], "no")
        self.assertEqual(rows[0]["expected_hit_at_3"], "yes")
        self.assertEqual(rows[0]["expected_cited"], "yes")

    def test_uncertain_fit_gate_classifies_as_abstain_or_clarify_needed(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "is this normal newborn behavior or sepsis",
                            "prompt_metadata": {"expected_guide_ids": "GD-002"},
                            "decision_path": "uncertain_fit",
                            "generation_time": 0,
                            "source_mode": "retrieved",
                            "cited_guide_ids": [],
                            "retrieval_metadata": {
                                "answer_mode": "uncertain_fit",
                                "top_retrieved_guide_ids": ["GD-999"],
                            },
                            "response_text": "Senku found guides that may be relevant, but this is not a confident fit.",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        rows = build_rows([artifact])

        self.assertEqual(
            rows[0]["suspected_failure_bucket"],
            "abstain_or_clarify_needed",
        )
        self.assertIn("uncertain_fit", rows[0]["short_reason"])
        self.assertEqual(rows[0]["expected_hit_at_k"], "no")
        self.assertEqual(rows[0]["answer_mode"], "uncertain_fit")
        self.assertEqual(rows[0]["app_gate_status"], "uncertain_fit")
        self.assertEqual(rows[0]["app_acceptance_status"], "uncertain_fit_accepted")
        self.assertEqual(rows[0]["ui_surface_bucket"], "limited_fit")

    def test_app_gate_fields_allow_retrieval_quality_to_remain_visible(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Can I use this for a rash?",
                            "prompt_metadata": {"expected_guide_ids": "GD-123"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "retrieved",
                            "cited_guide_ids": [],
                            "answer_mode": "uncertain_fit",
                            "support_strength": "weak",
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-123"],
                                "safety_critical": True,
                                "retrieval_profile": "guide_hit_low_confidence",
                            },
                            "response_text": "This may need a clinician to check.",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        rows = build_rows([artifact])

        self.assertEqual(rows[0]["answer_mode"], "uncertain_fit")
        self.assertEqual(rows[0]["support_strength"], "weak")
        self.assertEqual(rows[0]["safety_critical"], "True")
        self.assertEqual(rows[0]["retrieval_profile"], "guide_hit_low_confidence")
        self.assertEqual(rows[0]["app_gate_status"], "uncertain_fit")
        self.assertEqual(rows[0]["suspected_failure_bucket"], "abstain_or_clarify_needed")
        self.assertEqual(rows[0]["expected_hit_at_1"], "yes")
        self.assertEqual(rows[0]["expected_hit_at_k"], "yes")

    def test_analyze_reports_app_gate_counts_and_writes_csv_columns(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        output_dir = root / "diagnostics"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Should I answer this?",
                            "prompt_metadata": {"expected_guide_ids": "GD-456"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "retrieved",
                            "cited_guide_ids": [],
                            "retrieval_metadata": {
                                "answer_mode": "abstain",
                                "support_strength": "none",
                                "safety_critical": False,
                                "retrieval_profile": "no_clear_match",
                                "top_retrieved_guide_ids": ["GD-456"],
                            },
                            "response_text": "I cannot confidently answer this.",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        rows, summary = analyze([artifact], output_dir)

        self.assertEqual(rows[0]["app_gate_status"], "abstain")
        self.assertEqual(rows[0]["app_acceptance_status"], "abstain_accepted")
        self.assertEqual(rows[0]["ui_surface_bucket"], "abstain")
        self.assertEqual(summary["app_gate_counts"], {"abstain": 1})
        self.assertEqual(summary["app_acceptance_counts"], {"abstain_accepted": 1})
        self.assertEqual(summary["ui_surface_counts"], {"abstain": 1})
        self.assertEqual(summary["claim_support_counts"], {"no_cards": 1})
        self.assertEqual(summary["gated_expected_guide_rows"], 1)
        self.assertEqual(summary["gated_expected_hit_at_1"], "1/1 (100.0%)")
        report = (output_dir / "report.md").read_text(encoding="utf-8")
        self.assertIn("## App Gates", report)
        self.assertIn("## App Acceptance", report)
        self.assertIn("`abstain`: 1", report)
        with (output_dir / "diagnostics.csv").open(encoding="utf-8", newline="") as handle:
            csv_row = next(csv.DictReader(handle))
        self.assertEqual(csv_row["answer_mode"], "abstain")
        self.assertEqual(csv_row["answer_provenance"], "generated_model")
        self.assertEqual(csv_row["answer_surface_label"], "abstain")
        self.assertEqual(csv_row["app_gate_status"], "abstain")
        self.assertEqual(csv_row["app_acceptance_status"], "abstain_accepted")
        self.assertEqual(csv_row["ui_surface_bucket"], "abstain")
        self.assertIn("answer_card_status", csv_row)
        self.assertIn("claim_support_status", csv_row)
        self.assertIn("claim_action_count", csv_row)
        self.assertIn("shadow_card_answer_status", csv_row)
        self.assertIn("shadow_claim_support_status", csv_row)
        self.assertIn("## Answer Cards", report)
        self.assertIn("## Claim Support", report)
        self.assertIn("## Shadow Card Composer", report)

    def test_analyze_surfaces_completion_safety_trimmed(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        output_dir = root / "diagnostics"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Vomiting after a hit to the stomach",
                            "prompt_metadata": {"expected_guide_ids": "GD-999"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-999"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-999"],
                                "safety_critical": True,
                            },
                            "response_text": "Call emergency services if symptoms are severe. [GD-999]",
                            "completion_cap_hit": False,
                            "completion_retry_count": 1,
                            "completion_safety_trimmed": True,
                            "finish_reason": "stop",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        rows, summary = analyze([artifact], output_dir)

        self.assertTrue(rows[0]["completion_safety_trimmed"])
        self.assertEqual(summary["generation_workload"]["completion_safety_trimmed"], 1)
        report = (output_dir / "report.md").read_text(encoding="utf-8")
        self.assertIn("Safety-trimmed completions: `1`", report)
        with (output_dir / "diagnostics.csv").open(encoding="utf-8", newline="") as handle:
            csv_row = next(csv.DictReader(handle))
        self.assertEqual(csv_row["completion_safety_trimmed"], "True")

    def test_app_acceptance_flags_missing_evidence_owner_and_card_gap(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "Should we watch at home?",
                            "prompt_metadata": {"expected_guide_ids": "GD-111"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-222"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-111", "GD-222"],
                                "safety_critical": True,
                            },
                            "response_text": "Call 911 now. [GD-222]",
                        },
                        {
                            "index": 2,
                            "question": "Should we watch at home?",
                            "prompt_metadata": {"expected_guide_ids": "GD-333"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-333"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-333"],
                                "safety_critical": True,
                            },
                            "response_text": "- Call 911 now.\n- Wait overnight.",
                        },
                        {
                            "index": 3,
                            "question": "Pale and dizzy after abdominal trauma",
                            "prompt_metadata": {"expected_guide_ids": "GD-444"},
                            "decision_path": "rag",
                            "generation_time": 1.0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-444"],
                            "retrieval_metadata": {
                                "top_retrieved_guide_ids": ["GD-999", "GD-444"],
                                "safety_critical": True,
                            },
                            "response_text": "- Call 911 now.\n- Check for shock.",
                        },
                    ]
                }
            ),
            encoding="utf-8",
        )
        cards = [
            {
                "card_id": "owner",
                "guide_id": "GD-111",
                "risk_tier": "high",
                "required_first_actions": ["Call 911"],
                "forbidden_advice": [],
            },
            {
                "card_id": "gap",
                "guide_id": "GD-333",
                "risk_tier": "high",
                "required_first_actions": ["Call 911"],
                "first_actions": [],
                "urgent_red_flags": [],
                "forbidden_advice": ["Wait overnight"],
            },
            {
                "card_id": "grounded_rank_miss",
                "guide_id": "GD-444",
                "risk_tier": "high",
                "required_first_actions": ["Call 911"],
                "first_actions": ["Check for shock"],
                "urgent_red_flags": [],
                "forbidden_advice": [],
            },
        ]

        rows = build_rows([artifact], answer_cards=cards)

        self.assertEqual(rows[0]["evidence_owner_status"], "expected_owner_retrieved_not_cited")
        self.assertEqual(rows[0]["app_acceptance_status"], "needs_evidence_owner")
        self.assertEqual(rows[0]["safety_surface_status"], "emergency_first_supported")
        self.assertEqual(rows[1]["evidence_owner_status"], "expected_owner_cited")
        self.assertEqual(rows[1]["app_acceptance_status"], "card_contract_gap")
        self.assertEqual(rows[1]["ui_surface_bucket"], "emergency_first")
        self.assertEqual(rows[2]["suspected_failure_bucket"], "ranking_miss")
        self.assertEqual(rows[2]["app_acceptance_status"], "moderate_supported")
        self.assertEqual(rows[2]["evidence_owner_status"], "expected_owner_cited")

    def test_analyze_writes_markdown_csv_and_json(self):
        root = self.make_tmpdir()
        artifact = root / "sample.json"
        output_dir = root / "diagnostics"
        artifact.write_text(
            json.dumps(
                {
                    "results": [
                        {
                            "index": 1,
                            "question": "food stuck and they cannot speak",
                            "prompt_metadata": {},
                            "decision_path": "deterministic",
                            "generation_time": 0,
                            "source_mode": "cited",
                            "cited_guide_ids": ["GD-579"],
                            "retrieval_metadata": {},
                            "response_text": "Back blows and abdominal thrusts. [GD-579]",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        rows, summary = analyze([artifact], output_dir)

        self.assertEqual(rows[0]["suspected_failure_bucket"], "deterministic_pass")
        self.assertEqual(summary["by_bucket"]["deterministic_pass"], 1)
        self.assertTrue((output_dir / "report.md").exists())
        self.assertTrue((output_dir / "diagnostics.csv").exists())
        self.assertTrue((output_dir / "diagnostics.json").exists())


if __name__ == "__main__":
    unittest.main()

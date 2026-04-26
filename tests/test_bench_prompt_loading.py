import json
import tempfile
import unittest
from pathlib import Path

import bench


class BenchPromptLoadingTests(unittest.TestCase):
    def test_load_prompts_txt_preserves_section_headers(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            prompt_path = Path(tmpdir) / "prompts.txt"
            prompt_path.write_text(
                "# Section A\n"
                "first prompt\n"
                "\n"
                "# Section B\n"
                "second prompt\n",
                encoding="utf-8",
            )

            prompt_entries, source_format = bench.load_prompts(str(prompt_path))

        self.assertEqual(source_format, "txt")
        self.assertEqual(
            prompt_entries,
            [
                {
                    "question": "first prompt",
                    "section": "Section A",
                    "prompt_id": None,
                    "lane": None,
                    "style": None,
                    "target_behavior": None,
                    "what_it_tests": None,
                    "metadata": {},
                },
                {
                    "question": "second prompt",
                    "section": "Section B",
                    "prompt_id": None,
                    "lane": None,
                    "style": None,
                    "target_behavior": None,
                    "what_it_tests": None,
                    "metadata": {},
                },
            ],
        )

    def test_load_prompts_csv_preserves_structured_metadata(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            prompt_path = Path(tmpdir) / "prompts.csv"
            prompt_path.write_text(
                "id,lane,section,style,target_behavior,what_it_tests,prompt\n"
                "SP-001,core,Section A,direct,grounded,coverage check,First prompt?\n",
                encoding="utf-8",
            )

            prompt_entries, source_format = bench.load_prompts(str(prompt_path))

        self.assertEqual(source_format, "csv")
        self.assertEqual(prompt_entries[0]["question"], "First prompt?")
        self.assertEqual(prompt_entries[0]["section"], "Section A")
        self.assertEqual(prompt_entries[0]["prompt_id"], "SP-001")
        self.assertEqual(prompt_entries[0]["lane"], "core")
        self.assertEqual(prompt_entries[0]["style"], "direct")
        self.assertEqual(prompt_entries[0]["target_behavior"], "grounded")
        self.assertEqual(prompt_entries[0]["what_it_tests"], "coverage check")
        self.assertEqual(
            prompt_entries[0]["metadata"],
            {
                "lane": "core",
                "style": "direct",
                "target_behavior": "grounded",
                "what_it_tests": "coverage check",
            },
        )

    def test_load_prompts_jsonl_preserves_structured_metadata(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            prompt_path = Path(tmpdir) / "prompts.jsonl"
            prompt_path.write_text(
                json.dumps(
                    {
                        "id": "SP-002",
                        "lane": "stress",
                        "section": "Section B",
                        "style": "control",
                        "target_behavior": "clarify-or-bound",
                        "what_it_tests": "meta-control",
                        "prompt": "water",
                    }
                )
                + "\n",
                encoding="utf-8",
            )

            prompt_entries, source_format = bench.load_prompts(str(prompt_path))

        self.assertEqual(source_format, "jsonl")
        self.assertEqual(prompt_entries[0]["prompt_id"], "SP-002")
        self.assertEqual(prompt_entries[0]["lane"], "stress")
        self.assertEqual(prompt_entries[0]["style"], "control")
        self.assertEqual(prompt_entries[0]["target_behavior"], "clarify-or-bound")
        self.assertEqual(prompt_entries[0]["what_it_tests"], "meta-control")

    def test_load_prompts_jsonl_rejects_duplicate_prompt_ids(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            prompt_path = Path(tmpdir) / "prompts.jsonl"
            prompt_path.write_text(
                json.dumps({"id": "SP-003", "prompt": "first"}) + "\n"
                + json.dumps({"prompt_id": "SP-003", "prompt": "second"}) + "\n",
                encoding="utf-8",
            )

            with self.assertRaisesRegex(
                ValueError,
                r"Duplicate structured prompt ID `SP-003` at .* row 2; first seen at row 1",
            ):
                bench.load_prompts(str(prompt_path))

    def test_apply_prompt_filters_matches_structured_fields(self):
        prompt_entries = [
            bench._build_prompt_entry(
                "first",
                "Alpha",
                prompt_id="SP-001",
                metadata={
                    "lane": "core",
                    "style": "direct",
                    "target_behavior": "grounded",
                },
            ),
            bench._build_prompt_entry(
                "second",
                "Beta",
                prompt_id="SP-002",
                metadata={
                    "lane": "stress",
                    "style": "control",
                    "target_behavior": "clarify-or-bound",
                },
            ),
        ]

        filtered = bench._apply_prompt_filters(
            prompt_entries,
            sections={"beta"},
            lanes={"stress"},
            styles={"control"},
            target_behaviors={"clarify-or-bound"},
            prompt_ids={"sp-002"},
        )

        self.assertEqual([entry["question"] for entry in filtered], ["second"])


if __name__ == "__main__":
    unittest.main()

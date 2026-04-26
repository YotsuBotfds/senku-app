import json
import tempfile
import unittest
from pathlib import Path

from scripts import package_dual_model_answers_markdown as packager


def write_json(path: Path, payload: dict) -> Path:
    path.write_text(json.dumps(payload), encoding="utf-8")
    return path


class PackageDualModelAnswersMarkdownTests(unittest.TestCase):
    def test_load_structured_prompt_pack_accepts_csv_and_cleans_rows(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            path = Path(temp_dir) / "prompts.csv"
            path.write_text(
                "\n".join(
                    [
                        "id,prompt,lane,section,style,target_behavior,what_it_tests",
                        " P-001 , What now? , eval9 , , terse , first action , routing ",
                        "P-blank,   ,eval9,Ignored,,,",
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            rows = packager.load_structured_prompt_pack(path)

        self.assertEqual(1, len(rows))
        self.assertEqual(
            {
                "prompt_id": "P-001",
                "lane": "eval9",
                "section": "Unsectioned",
                "style": "terse",
                "target_behavior": "first action",
                "what_it_tests": "routing",
                "question": "What now?",
            },
            rows[0],
        )

    def test_load_structured_prompt_pack_accepts_jsonl_question_and_prompt_id(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            path = Path(temp_dir) / "prompts.jsonl"
            path.write_text(
                "\n"
                + json.dumps(
                    {
                        "prompt_id": "P-002",
                        "question": " Where next? ",
                        "lane": "eval8",
                        "section": "Navigation",
                    }
                )
                + "\n"
                + json.dumps({"prompt_id": "P-empty", "question": "  "})
                + "\n",
                encoding="utf-8",
            )

            rows = packager.load_structured_prompt_pack(path)

        self.assertEqual(1, len(rows))
        self.assertEqual("P-002", rows[0]["prompt_id"])
        self.assertEqual("Where next?", rows[0]["question"])
        self.assertEqual("Navigation", rows[0]["section"])

    def test_load_structured_prompt_pack_ignores_malformed_jsonl_rows_and_fields(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            path = Path(temp_dir) / "prompts.jsonl"
            path.write_text(
                "\n".join(
                    [
                        json.dumps(["not", "a", "row"]),
                        json.dumps(
                            {
                                "prompt_id": ["structured"],
                                "question": {"text": "structured"},
                                "section": ["bad"],
                            }
                        ),
                        json.dumps(
                            {
                                "id": {"bad": "id"},
                                "prompt": " Keep this? ",
                                "lane": {"bad": "lane"},
                                "style": ["bad"],
                            }
                        ),
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            rows = packager.load_structured_prompt_pack(path)

        self.assertEqual(1, len(rows))
        self.assertEqual("", rows[0]["prompt_id"])
        self.assertEqual("", rows[0]["lane"])
        self.assertEqual("", rows[0]["style"])
        self.assertEqual("Unsectioned", rows[0]["section"])
        self.assertEqual("Keep this?", rows[0]["question"])

    def test_prompt_key_prefers_prompt_id_over_question(self):
        self.assertEqual(
            ("prompt_id", "P-003"),
            packager.prompt_key({"prompt_id": " P-003 ", "question": "Duplicate?"}),
        )
        self.assertEqual(
            ("question", "Fallback?"),
            packager.prompt_key({"question": " Fallback? "}),
        )

    def test_load_bundle_detects_duplicates_and_preserves_first_record(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            first = write_json(
                root / "first.json",
                {
                    "config": {"model": "artifact-model"},
                    "results": [
                        {
                            "prompt_id": "P-004",
                            "question": "Question A",
                            "response_text": " First answer ",
                            "model": "result-model",
                            "decision_path": "rag",
                            "source_mode": "cited",
                            "duplicate_citation_count": 0,
                        }
                    ],
                },
            )
            second = write_json(
                root / "second.json",
                {
                    "config": {"model": "other-model"},
                    "results": [
                        {
                            "prompt_id": "P-004",
                            "question": "Question A",
                            "response_text": "Second answer",
                        }
                    ],
                },
            )

            rows, artifact_names, duplicates = packager.load_bundle([first, second])

        self.assertEqual(["first.json", "second.json"], artifact_names)
        self.assertEqual([(("prompt_id", "P-004"), "second.json")], duplicates)
        record = rows[("prompt_id", "P-004")]
        self.assertEqual("First answer", record["response_text"])
        self.assertEqual("artifact-model", record["artifact_model"])
        self.assertEqual("result-model", record["result_model"])
        self.assertEqual(0, record["duplicate_citation_count"])

    def test_render_model_block_handles_missing_error_blank_and_zero_dup_count(self):
        self.assertIn(
            "_Missing from selected artifacts._",
            "\n".join(packager.render_model_block("Left", None)),
        )
        self.assertEqual(
            ["**Left**", "**ERROR:** timeout"],
            packager.render_model_block("Left", {"error": "timeout"}),
        )
        blank = "\n".join(
            packager.render_model_block(
                "Left",
                {
                    "response_text": " ",
                    "result_model": "model-a",
                    "duplicate_citation_count": 0,
                },
            )
        )
        self.assertIn("dup_cites=0", blank)
        self.assertIn("_No response text captured._", blank)

    def test_render_model_block_coerces_malformed_artifact_values(self):
        rendered = packager.render_model_block(
            "Left",
            {
                "response_text": {"answer": ["structured", 7]},
                "result_model": 42,
                "decision_path": ["rag", "fallback"],
                "source_mode": {"mode": "cited"},
            },
        )

        self.assertEqual("**Left**", rendered[0])
        self.assertIn("model=`42`", rendered[1])
        self.assertIn('path=["rag", "fallback"]', rendered[1])
        self.assertIn('source={"mode": "cited"}', rendered[1])
        self.assertEqual('{"answer": ["structured", 7]}', rendered[2])

        error = packager.render_model_block("Right", {"error": {"kind": "timeout"}})
        self.assertEqual(["**Right**", '**ERROR:** {"kind": "timeout"}'], error)

    def test_render_markdown_matches_prompt_id_and_question_keys(self):
        prompts = [
            {
                "prompt_id": "P-005",
                "lane": "eval9",
                "section": "Safety",
                "style": "brief",
                "target_behavior": "first action",
                "what_it_tests": "prompt id matching",
                "question": "Prompt keyed?",
            },
            {
                "prompt_id": "",
                "lane": "eval9",
                "section": "Routing",
                "style": "",
                "target_behavior": "",
                "what_it_tests": "",
                "question": "Question keyed?",
            },
        ]
        left_bundle = {
            ("prompt_id", "P-005"): {"response_text": "Left by id"},
            ("question", "Question keyed?"): {"response_text": "Left by question"},
        }
        right_bundle = {
            ("prompt_id", "P-005"): {"response_text": "Right by id"},
        }

        markdown = packager.render_markdown(
            prompts,
            "Left",
            left_bundle,
            ["left.json"],
            "Right",
            right_bundle,
            ["right.json"],
        )

        self.assertTrue(markdown.startswith("# Senku Full Answer Package - Left vs Right\n"))
        self.assertTrue(markdown.endswith("\n"))
        self.assertIn("- Left: left.json", markdown)
        self.assertIn("- Right: right.json", markdown)
        self.assertIn("## Lane: eval9", markdown)
        self.assertIn("### Safety", markdown)
        self.assertIn("### Routing", markdown)
        self.assertIn("Left by id", markdown)
        self.assertIn("Right by id", markdown)
        self.assertIn("Left by question", markdown)
        self.assertIn("_Missing from selected artifacts._", markdown)


if __name__ == "__main__":
    unittest.main()

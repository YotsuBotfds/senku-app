import json
import tempfile
import unittest
from pathlib import Path

from scripts.index_prompt_packs import (
    index_prompt_pack,
    index_prompt_packs,
    main,
    parse_txt_pack,
)


class IndexPromptPacksTests(unittest.TestCase):
    def make_tmpdir(self):
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def test_parse_txt_pack_tracks_sections_and_line_ids(self):
        root = self.make_tmpdir()
        pack = root / "prompts.txt"
        pack.write_text("# Core\nfirst prompt\n\n# Medical\nsecond prompt\n", encoding="utf-8")

        records = parse_txt_pack(pack)

        self.assertEqual([record.text for record in records], ["first prompt", "second prompt"])
        self.assertEqual([record.id for record in records], ["2", "5"])
        self.assertEqual([record.section for record in records], ["Core", "Medical"])

    def test_index_csv_counts_metadata_and_duplicates(self):
        root = self.make_tmpdir()
        pack = root / "pack.csv"
        pack.write_text(
            "id,lane,section,style,target_behavior,prompt\n"
            "A,core,Water,direct,target one,How do I store water?\n"
            "A,core,Water,urgent,target one,How do I store water?\n"
            "B,craft,Soap,direct,target two,How do I make soap?\n",
            encoding="utf-8",
        )

        record = index_prompt_pack(pack, base_dir=root)

        self.assertEqual(record.path, "pack.csv")
        self.assertEqual(record.prompt_count, 3)
        self.assertEqual(record.ids_count, 3)
        self.assertEqual(record.duplicate_ids, ["A"])
        self.assertEqual(len(record.duplicate_texts), 1)
        self.assertEqual(record.sections, ["Water", "Soap"])
        self.assertEqual(record.lanes, ["core", "craft"])
        self.assertEqual(record.styles, ["direct", "urgent"])
        self.assertEqual(record.target_behaviors, ["target one", "target two"])

    def test_index_json_and_jsonl_pack_variants(self):
        root = self.make_tmpdir()
        json_pack = root / "pack.json"
        json_pack.write_text(
            json.dumps(
                {
                    "prompts": [
                        {"label": "one", "prompt": "First prompt", "section": "S1"},
                        {"case_id": "two", "initial_query": "Second prompt", "expected_behavior": "Stay focused"},
                    ]
                }
            ),
            encoding="utf-8",
        )
        jsonl_pack = root / "pack.jsonl"
        jsonl_pack.write_text(
            '{"case_id":"J1","family":"water","priority":"high","initial_query":"Rain barrels?","expected_behavior":"Use water lane"}\n'
            '{"id":"J2","query":"Soap?"}\n',
            encoding="utf-8",
        )

        json_record = index_prompt_pack(json_pack, base_dir=root)
        jsonl_record = index_prompt_pack(jsonl_pack, base_dir=root)

        self.assertEqual(json_record.prompt_count, 2)
        self.assertEqual(json_record.ids_count, 2)
        self.assertEqual(json_record.sections, ["S1"])
        self.assertEqual(json_record.target_behaviors, ["Stay focused"])
        self.assertEqual(jsonl_record.prompt_count, 2)
        self.assertEqual(jsonl_record.lanes, ["water"])
        self.assertEqual(jsonl_record.styles, ["high"])

    def test_index_prompt_packs_includes_test_prompts(self):
        root = self.make_tmpdir()
        prompts_dir = root / "artifacts" / "prompts"
        prompts_dir.mkdir(parents=True)
        (prompts_dir / "pack.txt").write_text("artifact prompt\n", encoding="utf-8")
        test_prompts = root / "test_prompts.txt"
        test_prompts.write_text("# Smoke\nsmoke prompt\n", encoding="utf-8")

        records = index_prompt_packs(
            prompts_dir,
            test_prompts_path=test_prompts,
            base_dir=root,
        )

        self.assertEqual([record.path for record in records], ["artifacts/prompts/pack.txt", "test_prompts.txt"])
        self.assertEqual([record.prompt_count for record in records], [1, 1])

    def test_main_writes_markdown_and_json(self):
        root = self.make_tmpdir()
        prompts_dir = root / "prompts"
        prompts_dir.mkdir()
        output_md = root / "out" / "index.md"
        output_json = root / "out" / "index.json"
        (prompts_dir / "pack.json").write_text(
            json.dumps([{"id": "P1", "prompt": "Hello"}]),
            encoding="utf-8",
        )

        rc = main(
            [
                "--prompts-dir",
                str(prompts_dir),
                "--no-test-prompts",
                "--output-md",
                str(output_md),
                "--output-json",
                str(output_json),
            ]
        )

        self.assertEqual(rc, 0)
        self.assertIn("# Prompt Pack Index", output_md.read_text(encoding="utf-8"))
        rows = json.loads(output_json.read_text(encoding="utf-8"))
        self.assertEqual(rows[0]["prompt_count"], 1)
        self.assertEqual(rows[0]["ids_count"], 1)


if __name__ == "__main__":
    unittest.main()

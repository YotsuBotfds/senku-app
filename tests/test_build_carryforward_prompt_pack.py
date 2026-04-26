import csv
import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path

from scripts.build_carryforward_prompt_pack import (
    append_carryforward_rows,
    load_structured_rows,
    main,
)


class BuildCarryforwardPromptPackTests(unittest.TestCase):
    def test_load_structured_rows_rejects_csv_extra_columns(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            pack = Path(tmpdir) / "pack.csv"
            pack.write_text(
                "id,prompt\n"
                "SP-001,valid prompt,unexpected trailing cell\n",
                encoding="utf-8",
            )

            with self.assertRaisesRegex(
                ValueError,
                r"Unexpected extra columns in .*pack\.csv at line 2",
            ):
                load_structured_rows(pack)

    def test_append_carryforward_rows_preserves_source_index_as_number(self):
        combined_rows, appended_rows = append_carryforward_rows(
            [],
            [{"section": "Core Regression", "prompt": "How do I store water?"}],
        )

        self.assertEqual(combined_rows, appended_rows)
        self.assertEqual(appended_rows[0]["source_index"], 1)

    def test_main_sanitizes_control_text_in_outputs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmp = Path(tmpdir)
            structured = tmp / "structured.jsonl"
            canonical = tmp / "canonical.txt"
            output_prefix = tmp / "carryforward"

            structured.write_text(
                json.dumps(
                    {
                        "id": "SP-\x00001",
                        "section": "Base\x07Section",
                        "prompt": "Structured\x1f prompt?",
                    }
                )
                + "\n",
                encoding="utf-8",
            )
            canonical.write_text(
                "\ufeff# Core\x00 Regression\n"
                "Canonical\x07 prompt?\n",
                encoding="utf-8",
            )

            with redirect_stdout(io.StringIO()):
                rc = main(
                    [
                        "--structured-pack",
                        str(structured),
                        "--canonical-prompts",
                        str(canonical),
                        "--output-prefix",
                        str(output_prefix),
                    ]
                )

            self.assertEqual(rc, 0)
            txt_output = output_prefix.with_suffix(".txt").read_text(encoding="utf-8")
            csv_output = output_prefix.with_suffix(".csv").read_text(encoding="utf-8")
            jsonl_output = output_prefix.with_suffix(".jsonl").read_text(encoding="utf-8")

            for output in (txt_output, csv_output, jsonl_output):
                self.assertNotIn("\x00", output)
                self.assertNotIn("\x07", output)
                self.assertNotIn("\x1f", output)

            rows = [
                json.loads(line)
                for line in jsonl_output.splitlines()
                if line.strip()
            ]
            self.assertEqual(rows[0]["id"], "SP- 001")
            self.assertEqual(rows[1]["source_index"], 1)
            self.assertEqual(rows[1]["lane"], "core-carryforward")
            with output_prefix.with_suffix(".csv").open("r", encoding="utf-8", newline="") as handle:
                csv_rows = list(csv.DictReader(handle))
            self.assertEqual(csv_rows[1]["prompt"], "Canonical prompt?")


if __name__ == "__main__":
    unittest.main()

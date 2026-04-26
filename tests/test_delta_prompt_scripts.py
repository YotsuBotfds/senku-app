import csv
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
BUILD_DELTA = REPO_ROOT / "scripts" / "build_delta_prompt_pack.py"
MERGE_PACKS = REPO_ROOT / "scripts" / "merge_structured_prompt_packs.py"


class DeltaPromptScriptsTests(unittest.TestCase):
    def test_build_delta_prompt_pack_creates_structured_outputs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmp = Path(tmpdir)
            delta_txt = tmp / "delta.txt"
            delta_txt.write_text(
                "# Senku Delta Regression Pack — 96 prompts\n\n"
                "# 01. triage_paraphrase_regressions\n\n"
                "One person is unconscious but breathing after a fall, and another is yelling with an obviously broken leg. Who do I deal with first, and why?\n\n"
                "# 04. tradeoff_decision_frameworks\n\n"
                "We have one clean tarp left. Is it better used for shelter, rain catchment, or keeping a patient dry? Give me the decision rules, not one universal answer.\n",
                encoding="utf-8",
            )
            output_prefix = tmp / "delta_structured"

            subprocess.run(
                [
                    sys.executable,
                    str(BUILD_DELTA),
                    "--delta-prompts",
                    str(delta_txt),
                    "--output-prefix",
                    str(output_prefix),
                ],
                check=True,
            )

            with output_prefix.with_suffix(".csv").open("r", encoding="utf-8", newline="") as handle:
                csv_rows = list(csv.DictReader(handle))
            jsonl_rows = [
                json.loads(line)
                for line in output_prefix.with_suffix(".jsonl").read_text(encoding="utf-8").splitlines()
                if line.strip()
            ]

            self.assertEqual(len(csv_rows), 2)
            self.assertEqual(len(jsonl_rows), 2)
            self.assertEqual(csv_rows[0]["lane"], "delta")
            self.assertEqual(csv_rows[0]["section"], "Delta / Triage Paraphrase Regressions")
            self.assertEqual(csv_rows[1]["style"], "tradeoff")
            self.assertEqual(csv_rows[1]["target_behavior"], "conditional-decision-framework")

    def test_build_delta_prompt_pack_sanitizes_and_dedupes_prompts(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmp = Path(tmpdir)
            delta_txt = tmp / "delta.txt"
            delta_txt.write_text(
                "\ufeff# Senku Delta Regression Pack - 96 prompts\n\n"
                "# 01. triage_paraphrase_regressions\n\n"
                "  Check\x00 the unconscious patient first.  \n"
                "\n"
                "Check   the unconscious patient first.\n"
                "Keep a distinct prompt after the duplicate.\x07\n",
                encoding="utf-8",
            )
            output_prefix = tmp / "delta_structured"

            subprocess.run(
                [
                    sys.executable,
                    str(BUILD_DELTA),
                    "--delta-prompts",
                    str(delta_txt),
                    "--output-prefix",
                    str(output_prefix),
                ],
                check=True,
            )

            with output_prefix.with_suffix(".csv").open("r", encoding="utf-8", newline="") as handle:
                rows = list(csv.DictReader(handle))

            self.assertEqual([row["id"] for row in rows], ["DX-001", "DX-002"])
            self.assertEqual(rows[0]["prompt"], "Check the unconscious patient first.")
            self.assertEqual(rows[1]["prompt"], "Keep a distinct prompt after the duplicate.")

    def test_merge_structured_prompt_packs_preserves_order_and_dedupes(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmp = Path(tmpdir)
            base_csv = tmp / "base.csv"
            base_csv.write_text(
                "id,lane,section,style,target_behavior,what_it_tests,prompt\n"
                "SP-001,core,Base,direct,grounded,base one,Prompt A\n"
                "SP-002,core,Base,direct,grounded,base two,Prompt B\n",
                encoding="utf-8",
            )
            addon_csv = tmp / "addon.csv"
            addon_csv.write_text(
                "id,lane,section,style,target_behavior,what_it_tests,prompt\n"
                "DX-001,delta,Delta,scenario,delta,delta one,Prompt B\n"
                "DX-002,delta,Delta,scenario,delta,delta two,Prompt C\n",
                encoding="utf-8",
            )
            output_prefix = tmp / "merged"

            subprocess.run(
                [
                    sys.executable,
                    str(MERGE_PACKS),
                    "--base-pack",
                    str(base_csv),
                    "--addon-pack",
                    str(addon_csv),
                    "--dedupe-prompt-text",
                    "--output-prefix",
                    str(output_prefix),
                ],
                check=True,
            )

            with output_prefix.with_suffix(".csv").open("r", encoding="utf-8", newline="") as handle:
                rows = list(csv.DictReader(handle))
            self.assertEqual([row["prompt"] for row in rows], ["Prompt A", "Prompt B", "Prompt C"])
            self.assertEqual([row["id"] for row in rows], ["SP-001", "SP-002", "DX-002"])

    def test_merge_structured_prompt_packs_rejects_extra_csv_fields(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            tmp = Path(tmpdir)
            base_csv = tmp / "base.csv"
            base_csv.write_text(
                "id,prompt\n"
                "SP-001,Prompt A,unexpected\n",
                encoding="utf-8",
            )
            addon_csv = tmp / "addon.csv"
            addon_csv.write_text(
                "id,prompt\n"
                "DX-001,Prompt B\n",
                encoding="utf-8",
            )

            result = subprocess.run(
                [
                    sys.executable,
                    str(MERGE_PACKS),
                    "--base-pack",
                    str(base_csv),
                    "--addon-pack",
                    str(addon_csv),
                    "--output-prefix",
                    str(tmp / "merged"),
                ],
                capture_output=True,
                text=True,
            )

            self.assertNotEqual(result.returncode, 0)
            self.assertIn("Malformed CSV row", result.stderr)
            self.assertIn("unexpected extra fields ['unexpected']", result.stderr)


if __name__ == "__main__":
    unittest.main()

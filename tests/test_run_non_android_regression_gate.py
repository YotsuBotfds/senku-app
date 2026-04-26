import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_non_android_regression_gate.ps1"
GENERATED_FIXTURE_DIR = REPO_ROOT / "tests" / "fixtures" / "non_android_generated"


class RunNonAndroidRegressionGateTests(unittest.TestCase):
    def run_script(self, *args, check=True):
        return subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(SCRIPT),
                *args,
            ],
            capture_output=True,
            text=True,
            check=check,
            cwd=REPO_ROOT,
        )

    def test_fast_whatif_prints_partial_router_gate_commands(self):
        result = self.run_script("-Label", "unit_label", "-WhatIf")

        self.assertIn("Non-Android regression gate dry run. Label: unit_label", result.stdout)
        self.assertIn("PartialRouter.prompt_expectations", result.stdout)
        self.assertIn("PartialRouter.retrieval_eval", result.stdout)
        self.assertIn("PartialRouter.retrieval_expectations", result.stdout)
        self.assertIn("rag_eval_partial_router_holdouts_20260425_unit_label_retrieval_only.json", result.stdout)
        self.assertIn("Eval9Primary.prompt_expectations", result.stdout)
        self.assertIn("Eval9Primary.retrieval_eval", result.stdout)
        self.assertIn("Eval9Primary.retrieval_expectations", result.stdout)
        self.assertIn("rag_eval9_high_liability_compound_holdouts_20260426_unit_label_retrieval_only.json", result.stdout)
        self.assertIn("--allowed-drift-manifest", result.stdout)
        self.assertIn("notes\\specs\\partial_router_allowed_drift_20260426.json", result.stdout)
        self.assertIn("--fail-on-warnings", result.stdout)

    def test_allow_retrieval_warnings_omits_strict_warning_flag(self):
        result = self.run_script(
            "-Label",
            "unit_label",
            "-AllowRetrievalWarnings",
            "-WhatIf",
        )

        self.assertNotIn("--fail-on-warnings", result.stdout)
        self.assertIn("--fail-on-errors", result.stdout)

    def test_include_safety_critical_adds_eval6_commands(self):
        result = self.run_script(
            "-Label",
            "unit_label",
            "-IncludeSafetyCritical",
            "-WhatIf",
        )

        self.assertIn("SafetyCritical.prompt_expectations", result.stdout)
        self.assertIn("rag_eval_high_liability_compound_holdouts_20260425.jsonl", result.stdout)
        self.assertIn("rag_eval_high_liability_compound_holdouts_20260425_unit_label_retrieval_only.json", result.stdout)

    def test_generated_mode_requires_bench_json(self):
        result = self.run_script(
            "-Mode",
            "Generated",
            "-Label",
            "unit_label",
            "-WhatIf",
            check=False,
        )

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("GeneratedBenchJson", result.stderr)

    def test_generated_mode_requires_baseline_diag_when_blank(self):
        result = self.run_script(
            "-Mode",
            "Generated",
            "-Label",
            "unit_label",
            "-GeneratedBenchJson",
            "tests\\fixtures\\non_android_generated\\candidate.json",
            "-GeneratedBaselineDiag",
            "",
            "-WhatIf",
            check=False,
        )

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("GeneratedBaselineDiag", result.stderr)

    def test_generated_mode_prints_analysis_and_gate_commands(self):
        result = self.run_script(
            "-Mode",
            "Generated",
            "-Label",
            "unit_label",
            "-GeneratedBenchJson",
            "tests\\fixtures\\non_android_generated\\candidate.json",
            "-GeneratedBaselineDiag",
            "tests\\fixtures\\non_android_generated\\baseline_diag",
            "-FailOnGeneratedRegression",
            "-WhatIf",
        )

        self.assertIn("Generated.failure_analysis", result.stdout)
        self.assertIn("scripts\\analyze_rag_bench_failures.py", result.stdout)
        self.assertIn("Generated.regression_gate", result.stdout)
        self.assertIn("tests\\fixtures\\non_android_generated\\baseline_diag", result.stdout)
        self.assertIn("--fail-on-regression", result.stdout)

    def test_all_mode_whatif_prints_fast_and_generated_commands_without_safety_by_default(self):
        result = self.run_script(
            "-Mode",
            "All",
            "-Label",
            "unit_label",
            "-GeneratedBenchJson",
            "tests\\fixtures\\non_android_generated\\candidate.json",
            "-GeneratedBaselineDiag",
            "tests\\fixtures\\non_android_generated\\baseline_diag",
            "-WhatIf",
        )

        self.assertIn("PartialRouter.prompt_expectations", result.stdout)
        self.assertIn("Eval9Primary.prompt_expectations", result.stdout)
        self.assertIn("Generated.failure_analysis", result.stdout)
        self.assertIn("Generated.regression_gate", result.stdout)
        self.assertNotIn("SafetyCritical.prompt_expectations", result.stdout)

    def test_all_mode_whatif_with_safety_critical_includes_all_command_families(self):
        result = self.run_script(
            "-Mode",
            "All",
            "-IncludeSafetyCritical",
            "-Label",
            "unit_label",
            "-GeneratedBenchJson",
            "tests\\fixtures\\non_android_generated\\candidate.json",
            "-GeneratedBaselineDiag",
            "tests\\fixtures\\non_android_generated\\baseline_diag",
            "-WhatIf",
        )

        self.assertIn("PartialRouter.prompt_expectations", result.stdout)
        self.assertIn("PartialRouter.retrieval_eval", result.stdout)
        self.assertIn("PartialRouter.retrieval_expectations", result.stdout)
        self.assertIn("Eval9Primary.prompt_expectations", result.stdout)
        self.assertIn("Eval9Primary.retrieval_eval", result.stdout)
        self.assertIn("Eval9Primary.retrieval_expectations", result.stdout)
        self.assertIn("SafetyCritical.prompt_expectations", result.stdout)
        self.assertIn("SafetyCritical.retrieval_eval", result.stdout)
        self.assertIn("SafetyCritical.retrieval_expectations", result.stdout)
        self.assertIn("Generated.failure_analysis", result.stdout)
        self.assertIn("Generated.regression_gate", result.stdout)

    def test_generated_mode_whatif_omits_fast_retrieval_commands(self):
        result = self.run_script(
            "-Mode",
            "Generated",
            "-Label",
            "unit_label",
            "-GeneratedBenchJson",
            "tests\\fixtures\\non_android_generated\\candidate.json",
            "-GeneratedBaselineDiag",
            "tests\\fixtures\\non_android_generated\\baseline_diag",
            "-FailOnGeneratedRegression",
            "-WhatIf",
        )

        self.assertIn("Generated.failure_analysis", result.stdout)
        self.assertIn("scripts\\analyze_rag_bench_failures.py", result.stdout)
        self.assertIn("Generated.regression_gate", result.stdout)
        self.assertIn("scripts\\rag_regression_gate.py", result.stdout)

        self.assertNotIn("PartialRouter.retrieval_eval", result.stdout)
        self.assertNotIn("Eval9Primary.retrieval_eval", result.stdout)
        self.assertNotIn("scripts\\evaluate_retrieval_pack.py", result.stdout)
        self.assertNotIn("validate_prompt_expectations.py", result.stdout)
        self.assertNotIn(
            "rag_eval9_high_liability_compound_holdouts_20260426_retrieval_only",
            result.stdout,
        )
        self.assertNotIn("FastEmbed", result.stdout)

    def test_structural_mode_whatif_omits_retrieval_and_generated_commands(self):
        result = self.run_script(
            "-Mode",
            "Structural",
            "-Label",
            "unit_label",
            "-WhatIf",
        )

        self.assertIn("Non-Android regression gate dry run. Label: unit_label", result.stdout)
        self.assertIn("PartialRouter.prompt_expectations", result.stdout)
        self.assertIn("Eval9Primary.prompt_expectations", result.stdout)
        self.assertIn("scripts\\validate_prompt_expectations.py", result.stdout)
        self.assertIn("rag_eval_partial_router_holdouts_20260425.jsonl", result.stdout)
        self.assertIn("rag_eval9_high_liability_compound_holdouts_20260426.jsonl", result.stdout)

        self.assertNotIn("PartialRouter.retrieval_eval", result.stdout)
        self.assertNotIn("Eval9Primary.retrieval_eval", result.stdout)
        self.assertNotIn("scripts\\evaluate_retrieval_pack.py", result.stdout)
        self.assertNotIn("Generated.failure_analysis", result.stdout)
        self.assertNotIn("scripts\\analyze_rag_bench_failures.py", result.stdout)
        self.assertNotIn("Generated.regression_gate", result.stdout)
        self.assertNotIn("scripts\\rag_regression_gate.py", result.stdout)
        self.assertNotIn("FastEmbed", result.stdout)

    def test_generated_mode_whatif_summary_omits_fast_retrieval_commands(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            summary_path = Path(tmpdir) / "summary.md"
            env = {"GITHUB_STEP_SUMMARY": str(summary_path)}
            result = subprocess.run(
                [
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    str(SCRIPT),
                    "-Mode",
                    "Generated",
                    "-Label",
                    "unit_label",
                    "-GeneratedBenchJson",
                    "tests\\fixtures\\non_android_generated\\candidate.json",
                    "-GeneratedBaselineDiag",
                    "tests\\fixtures\\non_android_generated\\baseline_diag",
                    "-FailOnGeneratedRegression",
                    "-WhatIf",
                ],
                capture_output=True,
                text=True,
                check=True,
                cwd=REPO_ROOT,
                env={**os.environ, **env},
            )

            summary = summary_path.read_text(encoding="utf-8")

        self.assertIn("Non-Android regression gate dry run. Label: unit_label", result.stdout)
        self.assertIn("- Status: what-if", summary)
        self.assertIn("- Mode: `Generated`", summary)
        self.assertIn("- `Generated.failure_analysis`", summary)
        self.assertIn("- `Generated.regression_gate`", summary)
        self.assertNotIn("PartialRouter", summary)
        self.assertNotIn("Eval9Primary", summary)
        self.assertNotIn("evaluate_retrieval_pack.py", summary)
        self.assertNotIn("validate_prompt_expectations.py", summary)
        self.assertNotIn("FastEmbed", summary)

    def test_generated_fixture_chain_passes_analysis_and_regression_gate(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            output_dir = Path(tmpdir) / "diag"
            analyze = subprocess.run(
                [
                    sys.executable,
                    "-B",
                    "scripts\\analyze_rag_bench_failures.py",
                    str(GENERATED_FIXTURE_DIR / "candidate.json"),
                    "--output-dir",
                    str(output_dir),
                ],
                capture_output=True,
                text=True,
                cwd=REPO_ROOT,
                check=True,
            )
            self.assertIn("expected_supported", analyze.stdout)

            gate = subprocess.run(
                [
                    sys.executable,
                    "-B",
                    "scripts\\rag_regression_gate.py",
                    str(GENERATED_FIXTURE_DIR / "baseline_diag"),
                    str(output_dir),
                    "--format",
                    "text",
                    "--fail-on-regression",
                ],
                capture_output=True,
                text=True,
                cwd=REPO_ROOT,
                check=True,
            )
            self.assertIn("RAG regression gate: PASS", gate.stdout)

    def test_invalid_label_fails_before_running(self):
        result = self.run_script("-Label", "bad label", "-WhatIf", check=False)

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Label", result.stderr)

    def test_zero_top_k_fails_before_dry_run_command_output(self):
        result = self.run_script(
            "-Label",
            "unit_label",
            "-TopK",
            "0",
            "-WhatIf",
            check=False,
        )

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("TopK", result.stderr)
        self.assertNotIn("Non-Android regression gate dry run", result.stdout)
        self.assertNotIn("PartialRouter.prompt_expectations", result.stdout)

    def test_negative_top_k_fails_before_dry_run_command_output(self):
        result = self.run_script(
            "-Label",
            "unit_label",
            "-TopK",
            "-3",
            "-WhatIf",
            check=False,
        )

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("TopK", result.stderr)
        self.assertNotIn("Non-Android regression gate dry run", result.stdout)
        self.assertNotIn("PartialRouter.prompt_expectations", result.stdout)


if __name__ == "__main__":
    unittest.main()

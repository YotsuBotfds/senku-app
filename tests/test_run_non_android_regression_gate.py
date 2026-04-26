import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_non_android_regression_gate.ps1"


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

    def test_generated_mode_prints_analysis_and_gate_commands(self):
        result = self.run_script(
            "-Mode",
            "Generated",
            "-Label",
            "unit_label",
            "-GeneratedBenchJson",
            "artifacts\\bench\\candidate.json",
            "-FailOnGeneratedRegression",
            "-WhatIf",
        )

        self.assertIn("Generated.failure_analysis", result.stdout)
        self.assertIn("scripts\\analyze_rag_bench_failures.py", result.stdout)
        self.assertIn("Generated.regression_gate", result.stdout)
        self.assertIn("--fail-on-regression", result.stdout)

    def test_invalid_label_fails_before_running(self):
        result = self.run_script("-Label", "bad label", "-WhatIf", check=False)

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Label", result.stderr)


if __name__ == "__main__":
    unittest.main()

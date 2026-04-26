import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_validation_slice.ps1"


class RunValidationSliceTests(unittest.TestCase):
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

    def test_tooling_context_indexes_whatif_prints_unittest_command(self):
        result = self.run_script("-Slice", "ToolingContextIndexes", "-WhatIf")

        self.assertIn("Validation slice dry run", result.stdout)
        self.assertIn("ToolingContextIndexes.unit", result.stdout)
        self.assertIn("tests.test_index_bench_artifacts", result.stdout)
        self.assertIn("tests.test_guide_edit_impact.GuideEditImpactTests", result.stdout)
        self.assertIn("-m unittest", result.stdout)

    def test_rag_eval_post_fixes_whatif_prints_smoke_commands(self):
        result = self.run_script("-Slice", "RAGEvalPostFixes", "-WhatIf")

        self.assertIn("RAGEvalPostFixes.query_retrieval_miss", result.stdout)
        self.assertIn("scripts\\query_rag_diagnostics.py", result.stdout)
        self.assertIn("rag_eval_partial_router_holdouts_20260425_post_fixes_diag", result.stdout)
        self.assertIn("RAGEvalPostFixes.compact_context", result.stdout)

    def test_ad_hoc_command_whatif_prints_without_running(self):
        result = self.run_script("-Command", "Write-Host SHOULD_PRINT_ONLY", "-WhatIf")

        self.assertIn("[AdHoc.1] Write-Host SHOULD_PRINT_ONLY", result.stdout)

    def test_unknown_slice_fails_before_running(self):
        result = self.run_script("-Slice", "NoSuchSlice", "-WhatIf", check=False)

        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Unknown validation slice", result.stderr)

    def test_comma_separated_slices_are_supported(self):
        result = self.run_script(
            "-Slice",
            "ToolingContextIndexes,RAGEvalPostFixes",
            "-WhatIf",
        )

        self.assertIn("ToolingContextIndexes.unit", result.stdout)
        self.assertIn("RAGEvalPostFixes.query_retrieval_miss", result.stdout)

    def test_slice_names_allow_outer_whitespace_and_mixed_case(self):
        result = self.run_script("-Slice", "  toolingcontextindexes  ", "-WhatIf")

        self.assertIn("ToolingContextIndexes.unit", result.stdout)
        self.assertIn("tests.test_index_bench_artifacts", result.stdout)


if __name__ == "__main__":
    unittest.main()

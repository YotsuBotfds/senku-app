import re
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "start_fastembed_server.ps1"


class FastEmbedHostLauncherContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_default_parameters_match_fastembed_host_contract(self):
        self.assertIn('[string]$BindHost = "127.0.0.1"', self.script)
        self.assertIn("[int]$Port = 8801", self.script)
        self.assertIn(
            '[string]$ApiModelId = "nomic-ai/text-embedding-nomic-embed-text-v1.5"',
            self.script,
        )
        self.assertIn(
            '[string]$BackendModelName = "nomic-ai/nomic-embed-text-v1.5"',
            self.script,
        )

    def test_server_script_is_resolved_from_repo_scripts_directory(self):
        self.assertIn("$repoRoot = Split-Path -Parent $PSScriptRoot", self.script)
        self.assertIn(
            '$serverScript = Join-Path $repoRoot "scripts\\fastembed_openai_server.py"',
            self.script,
        )

    def test_missing_python_path_and_server_script_throw(self):
        self.assertRegex(
            self.script,
            r"if \(-not \(Test-Path -LiteralPath \$PythonPath\)\) \{\s*"
            r'throw "Python executable not found at: \$PythonPath"',
        )
        self.assertRegex(
            self.script,
            r"if \(-not \(Test-Path -LiteralPath \$serverScript\)\) \{\s*"
            r'throw "FastEmbed host server script not found at \$serverScript"',
        )

    def test_required_cli_args_are_forwarded(self):
        args_match = re.search(r"\$args = @\((.*?)\)", self.script, re.DOTALL)

        self.assertIsNotNone(args_match)
        args_block = args_match.group(1)
        for expected in (
            "$serverScript",
            '"--host", $BindHost',
            '"--port", $Port',
            '"--api-model-id", $ApiModelId',
            '"--backend-model-name", $BackendModelName',
        ):
            self.assertIn(expected, args_block)

    def test_optional_cache_dir_arg_is_only_added_when_non_empty(self):
        self.assertRegex(
            self.script,
            r"if \(-not \[string\]::IsNullOrWhiteSpace\(\$CacheDir\)\) \{\s*"
            r'\$args \+= @\("--cache-dir", \$CacheDir\)',
        )

    def test_optional_threads_arg_is_only_added_when_positive(self):
        self.assertRegex(
            self.script,
            r"if \(\$Threads -gt 0\) \{\s*"
            r'\$args \+= @\("--threads", \$Threads\)',
        )

    def test_final_invocation_uses_python_path_with_args_array(self):
        self.assertRegex(self.script.rstrip(), r"& \$PythonPath @args$")


if __name__ == "__main__":
    unittest.main()

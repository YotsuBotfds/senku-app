import re
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "start_litert_host_server.ps1"


class LiteRtHostLauncherContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8")

    def test_env_model_path_wins_before_default_discovery(self):
        env_override = "$env:SENKU_LITERT_MODEL_PATH"
        model_names = "$modelNames = @("

        self.assertLess(self.script.index(env_override), self.script.index(model_names))
        self.assertIn(f"return {env_override}", self.script)

    def test_default_model_discovery_prefers_e2b_before_e4b(self):
        match = re.search(r"\$modelNames = @\((.*?)\)", self.script, re.DOTALL)

        self.assertIsNotNone(match)
        model_names_block = match.group(1)
        self.assertLess(
            model_names_block.index('"gemma-4-E2B-it.litertlm"'),
            model_names_block.index('"gemma-4-E4B-it.litertlm"'),
        )
        self.assertLess(
            model_names_block.index('"gemma-4-E2B-it.task"'),
            model_names_block.index('"gemma-4-E4B-it.task"'),
        )


if __name__ == "__main__":
    unittest.main()

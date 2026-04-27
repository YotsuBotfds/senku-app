import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
BUILD_GRADLE = REPO_ROOT / "android-app" / "app" / "build.gradle"
README = REPO_ROOT / "android-app" / "README.md"


class AndroidTestOrchestratorContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.gradle = BUILD_GRADLE.read_text(encoding="utf-8")
        cls.readme = README.read_text(encoding="utf-8")

    def test_orchestrator_is_property_gated_and_disabled_by_default(self):
        self.assertIn("providers.gradleProperty('senku.enableTestOrchestrator')", self.gradle)
        self.assertIn(".orElse(false)", self.gradle)
        self.assertIn("if (senkuTestOrchestratorEnabled) {", self.gradle)

        execution_index = self.gradle.index("execution 'ANDROIDX_TEST_ORCHESTRATOR'")
        dependency_index = self.gradle.index("androidTestUtil 'androidx.test:orchestrator:1.5.1'")
        for index in (execution_index, dependency_index):
            prior_gate = self.gradle.rfind("if (senkuTestOrchestratorEnabled) {", 0, index)
            self.assertNotEqual(prior_gate, -1)
        self.assertEqual(self.gradle.count("execution 'ANDROIDX_TEST_ORCHESTRATOR'"), 1)
        self.assertEqual(self.gradle.count("androidTestUtil 'androidx.test:orchestrator:1.5.1'"), 1)

    def test_clear_package_data_is_gated_with_orchestrator(self):
        clear_package_index = self.gradle.index("testInstrumentationRunnerArguments clearPackageData: 'true'")
        execution_index = self.gradle.index("execution 'ANDROIDX_TEST_ORCHESTRATOR'")
        clear_package_gate = self.gradle.rfind("if (senkuTestOrchestratorEnabled) {", 0, clear_package_index)
        execution_gate = self.gradle.rfind("if (senkuTestOrchestratorEnabled) {", 0, execution_index)

        self.assertNotEqual(clear_package_gate, -1)
        self.assertNotEqual(execution_gate, -1)
        self.assertLess(clear_package_index, execution_index)

    def test_readme_marks_orchestrator_as_opt_in_non_acceptance_and_warns_on_warm_state(self):
        self.assertIn("'-Psenku.enableTestOrchestrator=true'", self.readme)
        self.assertIn("non-acceptance flake-isolation scaffold only", self.readme)
        self.assertIn("do not opt into Orchestrator by\ndefault", self.readme)
        self.assertIn("clearPackageData=true", self.readme)
        self.assertIn("do not use this lane for warm-state", self.readme)
        self.assertIn("pack-cache", self.readme)
        self.assertIn("imported-model persistence evidence", self.readme)
        self.assertIn("Fixed four-emulator screenshot/state-pack\nevidence remains primary", self.readme)


if __name__ == "__main__":
    unittest.main()

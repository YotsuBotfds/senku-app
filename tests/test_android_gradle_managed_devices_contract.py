import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
BUILD_GRADLE = REPO_ROOT / "android-app" / "app" / "build.gradle"
README = REPO_ROOT / "android-app" / "README.md"


class AndroidGradleManagedDevicesContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.gradle = BUILD_GRADLE.read_text(encoding="utf-8")
        cls.readme = README.read_text(encoding="utf-8")

    def test_managed_devices_are_property_gated(self):
        self.assertIn("providers.gradleProperty('senku.enableManagedDevices')", self.gradle)
        self.assertIn(".orElse(false)", self.gradle)
        self.assertIn("if (senkuManagedDevicesEnabled) {", self.gradle)
        self.assertLess(
            self.gradle.index("if (senkuManagedDevicesEnabled) {"),
            self.gradle.index("managedDevices {"),
        )

    def test_managed_device_scaffold_is_limited_to_opt_in_smoke_group(self):
        for expected in (
            "senkuPhoneApi30",
            "senkuTabletApi30",
            "senkuManagedSmoke",
            "targetDevices.add(devices.senkuPhoneApi30)",
            "targetDevices.add(devices.senkuTabletApi30)",
        ):
            self.assertIn(expected, self.gradle)

    def test_readme_marks_managed_devices_as_non_acceptance_evidence(self):
        self.assertIn("'-Psenku.enableManagedDevices=true'", self.readme)
        self.assertIn("future parallel smoke lane", self.readme)
        self.assertIn("does not replace the", self.readme)
        self.assertIn("fixed four-emulator screenshot/state-pack evidence", self.readme)


if __name__ == "__main__":
    unittest.main()

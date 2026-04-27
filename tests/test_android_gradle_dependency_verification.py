import re
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
ANDROID_ROOT = REPO_ROOT / "android-app"
VERIFICATION_METADATA = ANDROID_ROOT / "gradle" / "verification-metadata.xml"
SETTINGS_GRADLE = ANDROID_ROOT / "settings.gradle"
WRAPPER_PROPERTIES = ANDROID_ROOT / "gradle" / "wrapper" / "gradle-wrapper.properties"
GRADLE_NAMESPACE = {"gradle": "https://schema.gradle.org/dependency-verification"}


class AndroidGradleDependencyVerificationTests(unittest.TestCase):
    def test_dependency_verification_metadata_is_checked_in_with_sha256s(self):
        tree = ET.parse(VERIFICATION_METADATA)
        root = tree.getroot()

        self.assertEqual(
            root.tag,
            "{https://schema.gradle.org/dependency-verification}verification-metadata",
        )
        self.assertEqual(
            root.findtext("gradle:configuration/gradle:verify-metadata", namespaces=GRADLE_NAMESPACE),
            "true",
        )
        self.assertEqual(
            root.findtext("gradle:configuration/gradle:verify-signatures", namespaces=GRADLE_NAMESPACE),
            "false",
        )

        components = root.findall("gradle:components/gradle:component", namespaces=GRADLE_NAMESPACE)
        sha256_nodes = root.findall(".//gradle:sha256", namespaces=GRADLE_NAMESPACE)

        self.assertGreater(len(components), 100)
        self.assertGreater(len(sha256_nodes), 250)
        for node in sha256_nodes:
            value = node.attrib.get("value", "")
            self.assertRegex(value, r"^[0-9a-f]{64}$")

    def test_android_gradle_repositories_and_wrapper_are_pinned(self):
        settings = SETTINGS_GRADLE.read_text(encoding="utf-8")
        wrapper = WRAPPER_PROPERTIES.read_text(encoding="utf-8")

        self.assertIn("repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)", settings)
        self.assertIn("google()", settings)
        self.assertIn("mavenCentral()", settings)
        self.assertIn("gradlePluginPortal()", settings)
        self.assertNotIn("mavenLocal()", settings)
        self.assertNotRegex(settings, re.compile(r"\bmaven\s*\{", re.IGNORECASE))
        self.assertIn(
            "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.2.1-bin.zip",
            wrapper,
        )


if __name__ == "__main__":
    unittest.main()

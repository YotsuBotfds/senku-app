import re
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
HOST_CONFIG = REPO_ROOT / "android-app" / "app" / "src" / "main" / "java" / "com" / "senku" / "mobile" / "HostInferenceConfig.java"
NETWORK_SECURITY_CONFIG = REPO_ROOT / "android-app" / "app" / "src" / "main" / "res" / "xml" / "network_security_config.xml"
HARNESS_COMMON = REPO_ROOT / "scripts" / "android_harness_common.psm1"
ANDROID_README = REPO_ROOT / "android-app" / "README.md"
ANDROID_MAIN_JAVA = REPO_ROOT / "android-app" / "app" / "src" / "main" / "java"
APPROVED_NETWORK_CALL_SITES = {"com/senku/mobile/HostInferenceClient.java"}
NETWORK_PRIMITIVES = (
    "HttpURLConnection",
    ".openConnection(",
    ".toURL()",
    "OkHttpClient",
    ".newCall(",
    "Socket(",
    "InetAddress",
)


class AndroidHostInferenceGuardrailTest(unittest.TestCase):
    def test_app_default_host_inference_url_is_emulator_friendly(self):
        source = HOST_CONFIG.read_text(encoding="utf-8")

        self.assertIn('DEFAULT_BASE_URL = "http://10.0.2.2:1235/v1"', source)

    def test_cleartext_hosts_are_limited_to_android_host_inference_loopbacks(self):
        root = ET.parse(NETWORK_SECURITY_CONFIG).getroot()

        base_config = root.find("base-config")
        self.assertIsNotNone(base_config)
        self.assertEqual(base_config.attrib["cleartextTrafficPermitted"], "false")

        allowed_hosts = [
            domain.text.strip()
            for domain in root.findall("./domain-config[@cleartextTrafficPermitted='true']/domain")
        ]
        self.assertEqual(allowed_hosts, ["10.0.2.2", "127.0.0.1", "localhost"])
        self.assertNotIn("10.0.3.2", allowed_hosts)

    def test_physical_device_wrapper_rewrites_emulator_host_to_adb_reverse_loopback(self):
        script = HARNESS_COMMON.read_text(encoding="utf-8")

        self.assertIn("function Resolve-AndroidHostInferenceUrlForDevice", script)
        self.assertRegex(script, r'if \(\$DeviceName -like "emulator-\*"\) \{\s*return \$Url\s*\}')
        self.assertIn('if ($uri.Host -ne "10.0.2.2")', script)
        self.assertIn('$reverseTimeoutMilliseconds = 10000', script)
        self.assertIn('Invoke-AndroidAdbCommandCapture -AdbPath $AdbPath -Arguments @("-s", $DeviceName, "reverse", ("tcp:{0}" -f $port), ("tcp:{0}" -f $port)) -TimeoutMilliseconds $reverseTimeoutMilliseconds', script)
        self.assertIn("adb reverse timed out after {0} ms", script)
        self.assertNotIn('& $AdbPath -s $DeviceName reverse ("tcp:{0}" -f $port) ("tcp:{0}" -f $port)', script)
        self.assertIn('$builder.Host = "127.0.0.1"', script)

    def test_readme_documents_emulator_default_and_physical_device_rewrite(self):
        readme = ANDROID_README.read_text(encoding="utf-8")

        self.assertIn("app defaults stay emulator-friendly at `http://10.0.2.2:1235/v1`", readme)
        self.assertRegex(readme, re.compile(r"physical devices.*adb reverse.*127\.0\.0\.1", re.DOTALL))

    def test_production_network_call_sites_stay_limited_to_host_inference_client(self):
        hits = []
        for path in ANDROID_MAIN_JAVA.rglob("*.java"):
            relative_path = path.relative_to(ANDROID_MAIN_JAVA).as_posix()
            source = path.read_text(encoding="utf-8")
            for primitive in NETWORK_PRIMITIVES:
                if primitive in source:
                    hits.append((relative_path, primitive))

        offenders = [
            f"{relative_path}: {primitive}"
            for relative_path, primitive in hits
            if relative_path not in APPROVED_NETWORK_CALL_SITES
        ]
        self.assertEqual([], offenders)
        self.assertTrue(
            any(relative_path == "com/senku/mobile/HostInferenceClient.java" for relative_path, _ in hits),
            "expected HostInferenceClient to remain the single approved network call site",
        )


if __name__ == "__main__":
    unittest.main()

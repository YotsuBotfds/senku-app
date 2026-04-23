import importlib.util
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[1] / "scripts" / "regenerate_deterministic_registry.py"
SPEC = importlib.util.spec_from_file_location("regenerate_deterministic_registry", MODULE_PATH)
regenerate_deterministic_registry = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(regenerate_deterministic_registry)


class RegenerateDeterministicRegistryTests(unittest.TestCase):
    def test_rendered_registry_matches_checked_in_file(self):
        rules = regenerate_deterministic_registry.load_sidecar()
        regenerate_deterministic_registry.validate_rule_names(rules)

        rendered = regenerate_deterministic_registry.render_registry(rules)
        current = regenerate_deterministic_registry.REGISTRY_PATH.read_text(encoding="utf-8")

        self.assertEqual(current, rendered)


if __name__ == "__main__":
    unittest.main()

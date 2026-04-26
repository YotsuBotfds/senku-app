import importlib.util
import shutil
import unittest
import uuid
from pathlib import Path


def load_module():
    module_path = (
        Path(__file__).resolve().parents[1]
        / "scripts"
        / "validate_guide_answer_cards.py"
    )
    spec = importlib.util.spec_from_file_location("validate_guide_answer_cards", module_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class GuideAnswerCardValidationTests(unittest.TestCase):
    def make_root(self) -> Path:
        root = Path("artifacts") / "bench" / "guide_answer_card_unit_tests"
        root.mkdir(parents=True, exist_ok=True)
        path = root / uuid.uuid4().hex
        path.mkdir()
        self.addCleanup(lambda: shutil.rmtree(path, ignore_errors=True))
        return path

    def write_fixture(self, root: Path) -> tuple[Path, Path, Path]:
        schema_path = root / "schema.yaml"
        cards_dir = root / "cards"
        guides_dir = root / "guides"
        cards_dir.mkdir()
        guides_dir.mkdir()
        schema_path.write_text(
            """
card:
  required_fields:
    - card_id
    - guide_id
    - slug
    - title
    - risk_tier
    - required_first_actions
    - forbidden_advice
    - citation_ids
    - source_sections
""".lstrip(),
            encoding="utf-8",
        )
        (guides_dir / "alpha.md").write_text(
            """---
id: GD-001
slug: alpha
title: Alpha Guide
---

Body.
""",
            encoding="utf-8",
        )
        (cards_dir / "alpha.yaml").write_text(
            """
card_id: alpha-card
guide_id: GD-001
slug: alpha
title: Alpha Guide
risk_tier: critical
required_first_actions:
  - Act first.
forbidden_advice:
  - Do not delay.
citation_ids:
  - alpha-citation
source_sections:
  - guide: GD-001
    sections:
      - "#overview"
""".lstrip(),
            encoding="utf-8",
        )
        return schema_path, cards_dir, guides_dir

    def test_validate_accepts_complete_card_matching_guide_frontmatter(self):
        module = load_module()
        schema_path, cards_dir, guides_dir = self.write_fixture(self.make_root())

        failures, card_count = module.validate(schema_path, cards_dir, guides_dir)

        self.assertEqual(failures, [])
        self.assertEqual(card_count, 1)

    def test_validate_reports_missing_safety_fields_for_high_risk_card(self):
        module = load_module()
        root = self.make_root()
        schema_path, cards_dir, guides_dir = self.write_fixture(root)
        (cards_dir / "alpha.yaml").write_text(
            """
card_id: alpha-card
guide_id: GD-001
slug: alpha
title: Alpha Guide
risk_tier: high
required_first_actions: []
forbidden_advice: []
citation_ids:
  - alpha-citation
source_sections:
  - "#overview"
""".lstrip(),
            encoding="utf-8",
        )

        failures, card_count = module.validate(schema_path, cards_dir, guides_dir)

        self.assertEqual(card_count, 1)
        self.assertIn(
            "alpha.yaml: required_first_actions must be nonempty for high risk_tier",
            failures,
        )
        self.assertIn(
            "alpha.yaml: forbidden_advice must be nonempty for high risk_tier",
            failures,
        )

    def test_validate_reports_frontmatter_mismatch_and_empty_citations(self):
        module = load_module()
        root = self.make_root()
        schema_path, cards_dir, guides_dir = self.write_fixture(root)
        (cards_dir / "alpha.yaml").write_text(
            """
card_id: alpha-card
guide_id: GD-001
slug: wrong-slug
title: Wrong Title
risk_tier: medium
required_first_actions: []
forbidden_advice: []
citation_ids: []
source_sections: []
""".lstrip(),
            encoding="utf-8",
        )

        failures, _ = module.validate(schema_path, cards_dir, guides_dir)

        self.assertIn(
            "alpha.yaml: slug 'wrong-slug' does not match GD-001 frontmatter 'alpha'",
            failures,
        )
        self.assertIn(
            "alpha.yaml: title 'Wrong Title' does not match GD-001 frontmatter 'Alpha Guide'",
            failures,
        )
        self.assertIn("alpha.yaml: citation_ids must be nonempty", failures)
        self.assertIn("alpha.yaml: source_sections must be nonempty", failures)

    def test_validate_reports_non_mapping_card_yaml(self):
        module = load_module()
        root = self.make_root()
        schema_path, cards_dir, guides_dir = self.write_fixture(root)
        (cards_dir / "alpha.yaml").write_text(
            """
- not
- a
- mapping
""".lstrip(),
            encoding="utf-8",
        )

        failures, card_count = module.validate(schema_path, cards_dir, guides_dir)

        self.assertEqual(card_count, 1)
        self.assertEqual(failures, ["alpha.yaml: card YAML must be a mapping"])

    def test_validate_reports_malformed_conditional_required_actions(self):
        module = load_module()
        root = self.make_root()
        schema_path, cards_dir, guides_dir = self.write_fixture(root)
        (cards_dir / "alpha.yaml").write_text(
            """
card_id: alpha-card
guide_id: GD-001
slug: alpha
title: Alpha Guide
risk_tier: critical
required_first_actions:
  - Act first.
forbidden_advice:
  - Do not delay.
conditional_required_actions:
  - trigger_terms: []
    required_actions: []
citation_ids:
  - alpha-citation
source_sections:
  - "#overview"
""".lstrip(),
            encoding="utf-8",
        )

        failures, card_count = module.validate(schema_path, cards_dir, guides_dir)

        self.assertEqual(card_count, 1)
        self.assertIn(
            "alpha.yaml: conditional_required_actions[1].trigger_terms must be nonempty",
            failures,
        )
        self.assertIn(
            "alpha.yaml: conditional_required_actions[1].required_actions must be nonempty",
            failures,
        )

    def test_validate_reports_source_invariant_conflicts(self):
        module = load_module()
        root = self.make_root()
        schema_path, cards_dir, guides_dir = self.write_fixture(root)
        (guides_dir / "beta.md").write_text(
            """---
id: GD-002
slug: beta
title: Beta Guide
---

If suspected but not visible: finger sweep the mouth.
""",
            encoding="utf-8",
        )
        (cards_dir / "beta.yaml").write_text(
            """
card_id: beta-card
guide_id: GD-002
slug: beta
title: Beta Guide
risk_tier: critical
required_first_actions:
  - Act first.
forbidden_advice:
  - Do not delay.
citation_ids:
  - beta-citation
source_sections:
  - guide: GD-002
    sections:
      - "#overview"
source_invariants:
  - name: no blind sweeps
    guide: GD-002
    must_include:
      - do not perform a blind finger sweep
    must_not_match:
      - 'not visible:\\s*finger sweep'
""".lstrip(),
            encoding="utf-8",
        )

        failures, card_count = module.validate(schema_path, cards_dir, guides_dir)

        self.assertEqual(card_count, 2)
        self.assertIn(
            "beta.yaml: source_invariants[1].must_include missing from GD-002: "
            "'do not perform a blind finger sweep'",
            failures,
        )
        self.assertIn(
            "beta.yaml: source_invariants[1].must_not_match matched in GD-002: "
            "'not visible:\\\\s*finger sweep'",
            failures,
        )

    def test_validate_accepts_satisfied_source_invariants(self):
        module = load_module()
        root = self.make_root()
        schema_path, cards_dir, guides_dir = self.write_fixture(root)
        (cards_dir / "alpha.yaml").write_text(
            """
card_id: alpha-card
guide_id: GD-001
slug: alpha
title: Alpha Guide
risk_tier: critical
required_first_actions:
  - Act first.
forbidden_advice:
  - Do not delay.
citation_ids:
  - alpha-citation
source_sections:
  - guide: GD-001
    sections:
      - "#overview"
source_invariants:
  - name: body remains present
    guide: GD-001
    must_include:
      - Body.
    must_not_match:
      - 'not visible:\\s*finger sweep'
""".lstrip(),
            encoding="utf-8",
        )

        failures, card_count = module.validate(schema_path, cards_dir, guides_dir)

        self.assertEqual(failures, [])
        self.assertEqual(card_count, 1)

    def test_validate_reports_malformed_source_invariants(self):
        module = load_module()
        root = self.make_root()
        schema_path, cards_dir, guides_dir = self.write_fixture(root)
        (cards_dir / "alpha.yaml").write_text(
            """
card_id: alpha-card
guide_id: GD-001
slug: alpha
title: Alpha Guide
risk_tier: critical
required_first_actions:
  - Act first.
forbidden_advice:
  - Do not delay.
citation_ids:
  - alpha-citation
source_sections:
  - guide: GD-001
    sections:
      - "#overview"
source_invariants:
  unexpected: mapping
""".lstrip(),
            encoding="utf-8",
        )
        (cards_dir / "beta.yaml").write_text(
            """
card_id: beta-card
guide_id: GD-001
slug: alpha
title: Alpha Guide
risk_tier: critical
required_first_actions:
  - Act first.
forbidden_advice:
  - Do not delay.
citation_ids:
  - beta-citation
source_sections:
  - guide: GD-001
    sections:
      - "#overview"
source_invariants:
  - not a mapping
  - name: ""
    guide: GD-001
    must_include:
      - Body.
  - name: missing guide
    guide: GD-404
    must_include:
      - Body.
  - name: no checks
    guide: GD-001
  - name: bad regex
    guide: GD-001
    must_not_match:
      - "["
""".lstrip(),
            encoding="utf-8",
        )

        failures, card_count = module.validate(schema_path, cards_dir, guides_dir)

        self.assertEqual(card_count, 2)
        self.assertIn(
            "alpha.yaml: source_invariants must be a list when present",
            failures,
        )
        self.assertIn(
            "beta.yaml: source_invariants[1] must be a mapping",
            failures,
        )
        self.assertIn(
            "beta.yaml: source_invariants[2].name must be nonempty",
            failures,
        )
        self.assertIn(
            "beta.yaml: source_invariants[3].guide 'GD-404' was not found under guides/",
            failures,
        )
        self.assertIn(
            "beta.yaml: source_invariants[4] must define at least one of "
            "must_include, must_not_include, or must_not_match",
            failures,
        )
        self.assertTrue(
            any(
                failure.startswith(
                    "beta.yaml: source_invariants[5].must_not_match invalid regex '[':"
                )
                for failure in failures
            ),
            failures,
        )

    def test_missing_yaml_support_fails_clearly(self):
        module = load_module()
        original_yaml = module.yaml
        module.yaml = None
        self.addCleanup(lambda: setattr(module, "yaml", original_yaml))

        with self.assertRaisesRegex(RuntimeError, "PyYAML is required"):
            module.load_yaml(Path("does-not-matter.yaml"))

    def test_choking_source_guides_do_not_advise_blind_sweeps(self):
        repo_root = Path(__file__).resolve().parents[1]
        guide_text = (
            repo_root / "guides" / "pediatric-emergencies-field.md"
        ).read_text(encoding="utf-8").lower()

        self.assertNotIn("if suspected but not visible: finger sweep", guide_text)
        self.assertNotRegex(guide_text, r"not visible:\s*finger sweep")
        self.assertIn("do not perform a blind finger sweep", guide_text)
        self.assertIn("remove only a clearly visible object", guide_text)


if __name__ == "__main__":
    unittest.main()

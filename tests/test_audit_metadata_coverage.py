import json
import tempfile
import unittest
from pathlib import Path

from scripts.audit_metadata_coverage import audit_guide, audit_guides, render_markdown


class MetadataCoverageAuditTests(unittest.TestCase):
    def test_direct_malformed_frontmatter_audit_allows_missing_answer_card_lookup(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guide_path = Path(tmpdir) / "broken-direct-guide.md"
            guide_path.write_text(
                """---
id: GD-201
slug: broken-direct-guide
title: Broken Direct Guide
category: medical
liability_level: high
related: [GD-999
---

Body.
""",
                encoding="utf-8",
            )

            record = audit_guide(guide_path, frontmatter_issue="frontmatter parse failed")

        self.assertIsNotNone(record)
        assert record is not None
        self.assertEqual(record["guide_id"], "GD-201")
        self.assertEqual(record["answer_card_count"], 0)
        self.assertEqual(record["reviewed_answer_card_count"], 0)
        self.assertIn("malformed_frontmatter", record["gaps"])
        self.assertEqual(record["severity"], "gap")

    def test_malformed_high_liability_frontmatter_is_audited_as_gap(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "broken-guide.md").write_text(
                """---
id: GD-200
slug: broken-guide
title: Broken Guide
category: medical
liability_level: critical
related: [GD-999
---

Body.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)
            markdown = render_markdown(audit)

        record = audit["guides"][0]
        self.assertEqual(audit["summary"]["guides_scanned"], 1)
        self.assertEqual(audit["summary"]["high_liability_guides"], 1)
        self.assertEqual(audit["summary"]["high_liability_guides_with_gaps"], 1)
        self.assertEqual(audit["summary"]["malformed_frontmatter_count"], 1)
        self.assertIn("malformed_frontmatter", audit["summary"]["gap_counts"])
        self.assertIn("malformed_frontmatter", record["gaps"])
        self.assertTrue(record["high_liability"])
        self.assertEqual(record["severity"], "gap")
        self.assertIn("frontmatter", record["frontmatter_error"].lower())
        self.assertIn("broken-guide.md", markdown)
        self.assertIn("frontmatter_error", markdown)

    def test_high_liability_missing_routing_and_policy_is_reported(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "danger-guide.md").write_text(
                """---
id: GD-100
slug: danger-guide
title: Danger Guide
category: medical
description: A high risk guide.
liability_level: critical
tags: [medical]
---

Body.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)
            markdown = render_markdown(audit)
            payload = json.loads(json.dumps(audit))

        record = payload["guides"][0]
        self.assertEqual(payload["summary"]["high_liability_guides"], 1)
        self.assertEqual(payload["summary"]["high_liability_guides_with_gaps"], 1)
        self.assertEqual(record["severity"], "gap")
        self.assertIn("missing_routing_support", record["gaps"])
        self.assertIn("missing_citation_policy", record["gaps"])
        self.assertIn("missing_applicability", record["gaps"])
        self.assertIn("GD-100", markdown)

    def test_high_liability_with_aliases_routing_policy_and_applicability_has_no_gap(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides_dir = root / "guides"
            cards_dir = root / "cards"
            guides_dir.mkdir()
            cards_dir.mkdir()
            (guides_dir / "ready-guide.md").write_text(
                """---
id: GD-101
slug: ready-guide
title: Ready Guide
category: medical
description: A high risk guide.
liability_level: high
tags: [medical, triage]
aliases: [danger help]
routing_cues: [danger triage]
related: [GD-102]
citations_required: true
applicability: Emergency field care.
---

Body.
""",
                encoding="utf-8",
            )
            (cards_dir / "ready-guide.yaml").write_text(
                """card_id: ready_guide
guide_id: GD-101
review_status: pilot_reviewed
runtime_citation_policy: reviewed_source_family
routine_boundary: Avoid routine framing.
acceptable_uncertain_fit: Ask for clarification.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir, cards_dir=cards_dir)

        record = audit["guides"][0]
        self.assertEqual(audit["summary"]["high_liability_guides_with_gaps"], 0)
        self.assertEqual(record["severity"], "none")
        self.assertEqual(record["gaps"], [])

    def test_body_routing_markers_do_not_satisfy_structured_routing_support(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "body-routing-guide.md").write_text(
                """---
id: GD-103
slug: body-routing-guide
title: Body Routing Guide
category: medical
description: A high risk guide.
liability_level: high
tags: [medical]
citations_required: true
applicability: Emergency field care.
related: [GD-101]
---

Use this guide when urgent symptoms are present.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)
            markdown = render_markdown(audit)

        record = audit["guides"][0]
        self.assertFalse(record["has_routing_support"])
        self.assertFalse(record["has_structured_routing_support"])
        self.assertTrue(record["has_body_routing_markers"])
        self.assertEqual(record["body_routing_marker_count"], 1)
        self.assertIn("missing_routing_support", record["gaps"])
        self.assertEqual(record["severity"], "gap")
        self.assertIn("body routing hints", markdown)
        self.assertIn("GD-103", markdown)

    def test_high_liability_false_citations_required_does_not_satisfy_policy(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "false-citation-guide.md").write_text(
                """---
id: GD-104
slug: false-citation-guide
title: False Citation Guide
category: medical
description: A high risk guide.
liability_level: critical
tags: [medical]
aliases: [danger help]
routing_cues: [danger triage]
related: [GD-101]
citations_required: false
applicability: Emergency field care.
---

Body.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)

        record = audit["guides"][0]
        self.assertFalse(record["has_citation_policy"])
        self.assertIn("missing_citation_policy", record["gaps"])
        self.assertEqual(record["severity"], "gap")

    def test_high_liability_string_false_citations_required_does_not_satisfy_policy(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            guides_dir = root / "guides"
            cards_dir = root / "cards"
            guides_dir.mkdir()
            cards_dir.mkdir()
            (guides_dir / "quoted-false-citation-guide.md").write_text(
                """---
id: GD-105
slug: quoted-false-citation-guide
title: Quoted False Citation Guide
category: medical
description: A high risk guide.
liability_level: high
tags: [medical]
aliases: [danger help]
routing_cues: [danger triage]
related: [GD-101]
citations_required: "false"
applicability: Emergency field care.
---

Body.
""",
                encoding="utf-8",
            )
            (cards_dir / "quoted-false-citation-guide.yaml").write_text(
                """card_id: quoted_false_citation_guide
guide_id: GD-105
review_status: pilot_reviewed
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir, cards_dir=cards_dir)

        record = audit["guides"][0]
        self.assertFalse(record["has_citation_policy"])
        self.assertIn("missing_citation_policy", record["gaps"])
        self.assertNotIn("missing_reviewed_answer_card", record["gaps"])

    def test_high_liability_numeric_zero_citations_required_does_not_satisfy_policy(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "zero-citation-guide.md").write_text(
                """---
id: GD-106
slug: zero-citation-guide
title: Zero Citation Guide
category: medical
description: A high risk guide.
liability_level: high
tags: [medical]
aliases: [danger help]
routing_cues: [danger triage]
related: [GD-101]
citations_required: 0
applicability: Emergency field care.
---

Body.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)

        record = audit["guides"][0]
        self.assertFalse(record["has_citation_policy"])
        self.assertIn("missing_citation_policy", record["gaps"])
        self.assertEqual(record["severity"], "gap")

    def test_high_liability_list_of_false_like_citations_does_not_satisfy_policy(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "false-list-citation-guide.md").write_text(
                """---
id: GD-107
slug: false-list-citation-guide
title: False List Citation Guide
category: medical
description: A high risk guide.
liability_level: high
tags: [medical]
aliases: [danger help]
routing_cues: [danger triage]
related: [GD-101]
citations_required: ["false", "0", "none"]
applicability: Emergency field care.
---

Body.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)

        record = audit["guides"][0]
        self.assertFalse(record["has_citation_policy"])
        self.assertIn("missing_citation_policy", record["gaps"])
        self.assertEqual(record["severity"], "gap")

    def test_high_liability_false_like_metadata_shapes_do_not_satisfy_gaps(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "false-shaped-metadata-guide.md").write_text(
                """---
id: GD-108
slug: false-shaped-metadata-guide
title: False Shaped Metadata Guide
category: medical
description: A high risk guide.
liability_level: high
tags: [medical]
aliases: false
routing_cues:
  primary: false
related: ["none", "0", false]
citations_required:
  required: false
applicability:
  use_when: none
  not_for: false
---

Body.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)

        record = audit["guides"][0]
        self.assertEqual(record["aliases_count"], 0)
        self.assertEqual(record["routing_cues_count"], 0)
        self.assertEqual(record["related_count"], 0)
        self.assertFalse(record["has_structured_routing_support"])
        self.assertFalse(record["has_citation_policy"])
        self.assertFalse(record["has_applicability"])
        self.assertIn("missing_routing_support", record["gaps"])
        self.assertIn("missing_related", record["gaps"])
        self.assertIn("missing_citation_policy", record["gaps"])
        self.assertIn("missing_applicability", record["gaps"])
        self.assertEqual(record["severity"], "gap")

    def test_low_liability_metadata_gaps_are_not_counted_as_high_liability_gaps(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides_dir = Path(tmpdir) / "guides"
            guides_dir.mkdir()
            (guides_dir / "low-guide.md").write_text(
                """---
id: GD-102
slug: low-guide
title: Low Guide
category: craft
description: A low risk guide.
liability_level: low
---

Body.
""",
                encoding="utf-8",
            )

            audit = audit_guides(guides_dir)

        self.assertEqual(audit["summary"]["high_liability_guides"], 0)
        self.assertEqual(audit["summary"]["high_liability_guides_with_gaps"], 0)


if __name__ == "__main__":
    unittest.main()

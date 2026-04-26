import tempfile
import unittest
from pathlib import Path

from guide_catalog import (
    find_malformed_frontmatter,
    get_anchor_related_link_weights,
    get_reciprocal_links,
    load_guide_catalog,
)


class GuideCatalogTests(unittest.TestCase):
    def test_non_mapping_frontmatter_is_ignored(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "bad.md").write_text(
                """---
- not
- a mapping
---

Body text
""",
                encoding="utf-8",
            )
            (root / "good.md").write_text(
                """---
id: GD-001
slug: good-guide
title: Good Guide
---

Body text
""",
                encoding="utf-8",
            )

            by_id, by_slug = load_guide_catalog(str(root))
            issues = find_malformed_frontmatter(str(root))

        self.assertEqual(set(by_id), {"GD-001"})
        self.assertEqual(set(by_slug), {"good-guide"})
        self.assertEqual(len(issues), 1)
        self.assertEqual(issues[0]["source_file"], "bad.md")
        self.assertIn("not a mapping", issues[0]["reason"])

    def test_malformed_frontmatter_is_ignored(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "bad.md").write_text(
                """---
id: [unterminated
slug: bad-guide
title: Bad Guide
---

Body text
""",
                encoding="utf-8",
            )
            (root / "good.md").write_text(
                """---
id: GD-001
slug: good-guide
title: Good Guide
---

Body text
""",
                encoding="utf-8",
            )

            by_id, by_slug = load_guide_catalog(str(root))
            issues = find_malformed_frontmatter(str(root))

        self.assertEqual(set(by_id), {"GD-001"})
        self.assertEqual(set(by_slug), {"good-guide"})
        self.assertEqual(len(issues), 1)
        self.assertEqual(issues[0]["source_file"], "bad.md")
        self.assertIn("invalid frontmatter YAML", issues[0]["reason"])

    def test_duplicate_guide_id_raises_clear_error(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "one.md").write_text(
                """---
id: GD-001
slug: guide-one
title: One
---
""",
                encoding="utf-8",
            )
            (root / "two.md").write_text(
                """---
id: GD-001
slug: guide-two
title: Two
---
""",
                encoding="utf-8",
            )

            with self.assertRaisesRegex(ValueError, "Duplicate guide id"):
                load_guide_catalog(str(root))

    def test_duplicate_guide_slug_raises_clear_error(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "one.md").write_text(
                """---
id: GD-001
slug: same-slug
title: One
---
""",
                encoding="utf-8",
            )
            (root / "two.md").write_text(
                """---
id: GD-002
slug: same-slug
title: Two
---
""",
                encoding="utf-8",
            )

            with self.assertRaisesRegex(ValueError, "Duplicate guide slug"):
                load_guide_catalog(str(root))

    def test_reciprocal_and_unidirectional_related_links_are_resolved(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "anchor.md").write_text(
                """---
id: GD-001
slug: anchor-guide
title: Anchor
related:
  - reciprocal-guide
  - one-way-guide
---
""",
                encoding="utf-8",
            )
            (root / "reciprocal.md").write_text(
                """---
id: GD-002
slug: reciprocal-guide
title: Reciprocal
related: anchor-guide
---
""",
                encoding="utf-8",
            )
            (root / "one-way.md").write_text(
                """---
id: GD-003
slug: one-way-guide
title: One Way
---
""",
                encoding="utf-8",
            )

            weights = get_anchor_related_link_weights("GD-001", str(root))

        self.assertEqual({"GD-002": 0.5, "GD-003": 0.3}, weights)

    def test_get_reciprocal_links_returns_only_mutual_frontmatter_links(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "one.md").write_text(
                """---
id: GD-001
slug: guide-one
title: One
related: [guide-two, guide-three]
---
""",
                encoding="utf-8",
            )
            (root / "two.md").write_text(
                """---
id: GD-002
slug: guide-two
title: Two
related: guide-one
---
""",
                encoding="utf-8",
            )
            (root / "three.md").write_text(
                """---
id: GD-003
slug: guide-three
title: Three
---
""",
                encoding="utf-8",
            )

            reciprocal = get_reciprocal_links("GD-001", str(root))

        self.assertEqual({"GD-002"}, reciprocal)


if __name__ == "__main__":
    unittest.main()

import importlib.util
import tempfile
import unittest
from pathlib import Path


def load_module():
    module_path = Path(__file__).resolve().parents[1] / "scripts" / "build_guide_graph.py"
    spec = importlib.util.spec_from_file_location("build_guide_graph", module_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class BuildGuideGraphTests(unittest.TestCase):
    def test_graph_extracts_frontmatter_and_body_links(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "alpha.md").write_text(
                """---
id: GD-001
slug: alpha
title: Alpha
related:
  - beta
---

Alpha points to `gamma.md`, <a href="../delta.html">Delta</a>, and [Beta](../beta.md).
""",
                encoding="utf-8",
            )
            (root / "beta.md").write_text(
                """---
id: GD-002
slug: beta
title: Beta
---
""",
                encoding="utf-8",
            )
            (root / "gamma.md").write_text(
                """---
id: GD-003
slug: gamma
title: Gamma
---
""",
                encoding="utf-8",
            )
            (root / "delta.md").write_text(
                """---
id: GD-004
slug: delta
title: Delta
---
""",
                encoding="utf-8",
            )

            graph = module.build_graph(root)

        edges = {(edge["source"], edge["target"], edge["type"]) for edge in graph["edges"]}
        self.assertIn(("alpha", "beta", "frontmatter_related"), edges)
        self.assertIn(("alpha", "beta", "markdown_link"), edges)
        self.assertIn(("alpha", "gamma", "inline_filename"), edges)
        self.assertIn(("alpha", "delta", "html_link"), edges)
        self.assertEqual(graph["summary"]["orphan_count"], 0)

    def test_self_links_are_not_emitted(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "solo.md").write_text(
                """---
id: GD-001
slug: solo
title: Solo
related:
  - solo
---

Use `solo.md` and <a href="../solo.html">Solo</a>.
""",
                encoding="utf-8",
            )

            graph = module.build_graph(root)

        self.assertEqual(graph["summary"]["edge_count"], 0)
        self.assertEqual(graph["orphans"], ["solo"])

    def test_frontmatter_related_normalizes_file_references_and_dedupes(self):
        module = load_module()
        with tempfile.TemporaryDirectory() as tmpdir:
            root = Path(tmpdir)
            (root / "alpha.md").write_text(
                """---
id: GD-001
slug: alpha
title: Alpha
related:
  - Beta.md
  - ../beta.html
  - ./ALPHA.md
  - beta
  - "bad slug"
  - ""
---
""",
                encoding="utf-8",
            )
            (root / "beta.md").write_text(
                """---
id: GD-002
slug: beta
title: Beta
---
""",
                encoding="utf-8",
            )

            graph = module.build_graph(root)

        self.assertEqual(
            graph["edges"],
            [{"source": "alpha", "target": "beta", "type": "frontmatter_related"}],
        )
        self.assertEqual(graph["summary"]["edge_count"], 1)


if __name__ == "__main__":
    unittest.main()

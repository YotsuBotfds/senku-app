import json
import tempfile
import unittest
from pathlib import Path

from scripts.select_prompt_pack_guides import (
    expand_related,
    load_guide_catalog,
    load_prompt_pack_guide_ids,
)


def write_guide(path: Path, guide_id: str, related: str = "") -> None:
    related_line = f"related: [{related}]\n" if related else ""
    path.write_text(
        f"""---
id: {guide_id}
slug: {path.stem}
title: {path.stem}
category: utility
description: Test guide.
{related_line}---

## Body

Content.
""",
        encoding="utf-8",
    )


class SelectPromptPackGuidesTests(unittest.TestCase):
    def test_load_prompt_pack_extracts_expected_and_primary_guides(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            pack = Path(tmpdir) / "pack.jsonl"
            pack.write_text(
                json.dumps(
                    {
                        "id": "row",
                        "prompt": "question",
                        "expected_guide_ids": ["GD-001", "GD-002"],
                        "primary_expected_guides": ["GD-002"],
                    }
                )
                + "\n",
                encoding="utf-8",
            )

            guide_ids = load_prompt_pack_guide_ids(pack)

        self.assertEqual(guide_ids, ["GD-001", "GD-002"])

    def test_load_prompt_pack_skips_malformed_jsonl_rows(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            pack = Path(tmpdir) / "pack.jsonl"
            pack.write_text(
                "\n".join(
                    [
                        '{"id": "broken", "expected_guide_ids": ["GD-999"]',
                        json.dumps({"id": "valid", "expected_guide_ids": ["GD-001"]}),
                        "not json",
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            guide_ids = load_prompt_pack_guide_ids(pack)

        self.assertEqual(guide_ids, ["GD-001"])

    def test_expand_related_includes_one_hop_related_guides(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides = Path(tmpdir) / "guides"
            guides.mkdir()
            write_guide(guides / "one.md", "GD-001", "GD-002")
            write_guide(guides / "two.md", "GD-002")
            catalog = load_guide_catalog(guides)

            selected = expand_related(["GD-001"], catalog, depth=1)

        self.assertEqual(selected, ["GD-001", "GD-002"])

    def test_expand_related_resolves_inline_slug_lists(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides = Path(tmpdir) / "guides"
            guides.mkdir()
            write_guide(guides / "one.md", "GD-001", "two")
            write_guide(guides / "two.md", "GD-002")
            catalog = load_guide_catalog(guides)

            selected = expand_related(["GD-001"], catalog, depth=1)

        self.assertEqual(selected, ["GD-001", "GD-002"])

    def test_expand_related_resolves_multiline_slug_lists(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            guides = Path(tmpdir) / "guides"
            guides.mkdir()
            (guides / "one.md").write_text(
                """---
id: GD-001
slug: one
title: one
category: utility
description: Test guide.
related:
  - two
---

## Body

Content.
""",
                encoding="utf-8",
            )
            write_guide(guides / "two.md", "GD-002")
            catalog = load_guide_catalog(guides)

            selected = expand_related(["GD-001"], catalog, depth=1)

        self.assertEqual(selected, ["GD-001", "GD-002"])


if __name__ == "__main__":
    unittest.main()

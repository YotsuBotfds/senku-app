#!/usr/bin/env python3
"""Build a machine-readable guide relationship graph from Senku markdown guides."""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
GUIDES_DIR = ROOT / "guides"
DEFAULT_OUTPUT = ROOT / "artifacts" / "guide_graph"


FRONTMATTER_BOUNDARY = re.compile(r"^---\s*$", re.MULTILINE)
HREF_HTML_RE = re.compile(r'href="([^"]+\.html(?:[?#][^"]*)?)"', re.IGNORECASE)
HREF_MD_RE = re.compile(r"\(([^)\s]+\.md(?:[?#][^)]*)?)\)", re.IGNORECASE)
INLINE_MD_FILENAME_RE = re.compile(r"`([a-z0-9-]+)\.md`", re.IGNORECASE)
SLUG_FILE_RE = re.compile(r"^([a-z0-9-]+)(?:\.(?:md|html))?$", re.IGNORECASE)


def parse_frontmatter(text: str) -> tuple[dict, str]:
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return {}, text

    closing_index = None
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            closing_index = index
            break
    if closing_index is None:
        return {}, text

    try:
        meta = yaml.safe_load("\n".join(lines[1:closing_index])) or {}
    except yaml.YAMLError:
        meta = {}
    if not isinstance(meta, dict):
        meta = {}
    body = "\n".join(lines[closing_index + 1 :])
    return meta, body


def normalize_related(value) -> list[str]:
    if value is None:
        return []
    if isinstance(value, str):
        cleaned = value.strip()
        return [cleaned] if cleaned else []
    if isinstance(value, list):
        items = []
        for item in value:
            if item is None:
                continue
            cleaned = str(item).strip()
            if cleaned:
                items.append(cleaned)
        return items
    return []


def normalize_slug_reference(value) -> str:
    cleaned = str(value).strip()
    if not cleaned:
        return ""
    cleaned = cleaned.replace("\\", "/").rstrip("/")
    cleaned = re.split(r"[?#]", cleaned, maxsplit=1)[0]
    filename = cleaned.rsplit("/", 1)[-1].strip()
    match = SLUG_FILE_RE.match(filename)
    if not match:
        return ""
    return match.group(1).lower()


def markdown_code(value) -> str:
    text = str(value).replace("\r", " ").replace("\n", " ")
    if "`" not in text:
        return f"`{text}`"
    fence = "`" * (max(len(match.group(0)) for match in re.finditer(r"`+", text)) + 1)
    return f"{fence} {text} {fence}"


def load_guides(guides_dir: Path) -> tuple[list[dict], dict[str, dict]]:
    guides = []
    by_slug = {}
    for path in sorted(guides_dir.glob("*.md")):
        text = path.read_text(encoding="utf-8")
        meta, body = parse_frontmatter(text)
        slug = normalize_slug_reference(meta.get("slug", ""))
        guide_id = str(meta.get("id", "")).strip()
        title = str(meta.get("title", "")).strip()
        category = str(meta.get("category", "")).strip()
        related = normalize_related(meta.get("related"))
        if not slug:
            continue
        try:
            display_path = str(path.relative_to(ROOT)).replace("\\", "/")
        except ValueError:
            display_path = path.name
        node = {
            "slug": slug,
            "guide_id": guide_id,
            "title": title,
            "category": category,
            "path": display_path,
            "related": related,
            "body": body,
        }
        guides.append(node)
        by_slug[slug] = node
    return guides, by_slug


def extract_body_links(body: str, valid_slugs: set[str]) -> dict[str, set[str]]:
    edges = defaultdict(set)

    for match in HREF_HTML_RE.finditer(body):
        slug = normalize_slug_reference(match.group(1))
        if slug in valid_slugs:
            edges["html_link"].add(slug)

    for match in HREF_MD_RE.finditer(body):
        slug = normalize_slug_reference(match.group(1))
        if slug in valid_slugs:
            edges["markdown_link"].add(slug)

    for match in INLINE_MD_FILENAME_RE.finditer(body):
        slug = match.group(1).strip().lower()
        if slug in valid_slugs:
            edges["inline_filename"].add(slug)

    return edges


def build_graph(guides_dir: Path) -> dict:
    guides, by_slug = load_guides(guides_dir)
    valid_slugs = set(by_slug)

    nodes = []
    edges = []
    incoming = Counter()
    outgoing = Counter()

    for guide in guides:
        slug = guide["slug"]
        nodes.append(
            {
                "slug": slug,
                "guide_id": guide["guide_id"],
                "title": guide["title"],
                "category": guide["category"],
                "path": guide["path"],
            }
        )

        seen = set()
        for raw_target_slug in guide["related"]:
            target_slug = normalize_slug_reference(raw_target_slug)
            if target_slug not in valid_slugs or target_slug == slug:
                continue
            edge_key = (slug, target_slug, "frontmatter_related")
            if edge_key in seen:
                continue
            seen.add(edge_key)
            edges.append({"source": slug, "target": target_slug, "type": "frontmatter_related"})
            incoming[target_slug] += 1
            outgoing[slug] += 1

        body_edges = extract_body_links(guide["body"], valid_slugs)
        for edge_type, targets in body_edges.items():
            for target_slug in sorted(targets):
                if target_slug == slug:
                    continue
                edge_key = (slug, target_slug, edge_type)
                if edge_key in seen:
                    continue
                seen.add(edge_key)
                edges.append({"source": slug, "target": target_slug, "type": edge_type})
                incoming[target_slug] += 1
                outgoing[slug] += 1

    orphan_slugs = sorted(
        node["slug"] for node in nodes if incoming[node["slug"]] == 0 and outgoing[node["slug"]] == 0
    )
    top_incoming = sorted(
        (
            {
                "slug": slug,
                "guide_id": by_slug[slug]["guide_id"],
                "title": by_slug[slug]["title"],
                "count": count,
            }
            for slug, count in incoming.items()
        ),
        key=lambda item: (-item["count"], item["slug"]),
    )[:25]
    top_outgoing = sorted(
        (
            {
                "slug": slug,
                "guide_id": by_slug[slug]["guide_id"],
                "title": by_slug[slug]["title"],
                "count": count,
            }
            for slug, count in outgoing.items()
        ),
        key=lambda item: (-item["count"], item["slug"]),
    )[:25]

    return {
        "summary": {
            "node_count": len(nodes),
            "edge_count": len(edges),
            "orphan_count": len(orphan_slugs),
            "edge_type_counts": Counter(edge["type"] for edge in edges),
        },
        "nodes": nodes,
        "edges": edges,
        "orphans": orphan_slugs,
        "top_incoming": top_incoming,
        "top_outgoing": top_outgoing,
    }


def write_outputs(graph: dict, output_dir: Path) -> tuple[Path, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    json_path = output_dir / "guide_graph.json"
    md_path = output_dir / "guide_graph_summary.md"

    serializable = dict(graph)
    serializable["summary"] = dict(serializable["summary"])
    serializable["summary"]["edge_type_counts"] = dict(serializable["summary"]["edge_type_counts"])
    json_path.write_text(json.dumps(serializable, indent=2), encoding="utf-8")

    summary = serializable["summary"]
    lines = [
        "# Guide Graph Summary",
        "",
        f"- Nodes: {summary['node_count']}",
        f"- Edges: {summary['edge_count']}",
        f"- Orphans: {summary['orphan_count']}",
        "",
        "## Edge Types",
        "",
    ]
    for edge_type, count in sorted(summary["edge_type_counts"].items()):
        lines.append(f"- `{edge_type}`: {count}")

    lines.extend(["", "## Top Incoming", ""])
    for item in graph["top_incoming"]:
        lines.append(f"- {markdown_code(item['guide_id'])} {markdown_code(item['slug'])}: {item['count']}")

    lines.extend(["", "## Top Outgoing", ""])
    for item in graph["top_outgoing"]:
        lines.append(f"- {markdown_code(item['guide_id'])} {markdown_code(item['slug'])}: {item['count']}")

    lines.extend(["", "## Orphans", ""])
    if graph["orphans"]:
        for slug in graph["orphans"]:
            lines.append(f"- {markdown_code(slug)}")
    else:
        lines.append("- none")

    md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return json_path, md_path


def main() -> int:
    parser = argparse.ArgumentParser(description="Build a guide relationship graph from Senku markdown guides.")
    parser.add_argument("--guides-dir", default=str(GUIDES_DIR))
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT))
    args = parser.parse_args()

    graph = build_graph(Path(args.guides_dir))
    json_path, md_path = write_outputs(graph, Path(args.output_dir))

    print(f"Wrote graph JSON: {json_path}")
    print(f"Wrote graph summary: {md_path}")
    print(
        f"Nodes={graph['summary']['node_count']} "
        f"Edges={graph['summary']['edge_count']} "
        f"Orphans={graph['summary']['orphan_count']}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

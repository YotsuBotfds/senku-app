#!/usr/bin/env python3
"""Rank guide audit hotspots by combining relationship edges with invariant density."""

from __future__ import annotations

import argparse
import json
from collections import Counter, defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
GRAPH_PATH = ROOT / "artifacts" / "guide_graph" / "guide_graph.json"
INVARIANT_PATH = ROOT / "artifacts" / "guide_invariants" / "guide_invariants.jsonl"
DEFAULT_OUTPUT = ROOT / "artifacts" / "guide_audit_hotspots"


def load_graph(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def load_invariant_counts(path: Path) -> Counter:
    counts = Counter()
    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            if not line.strip():
                continue
            try:
                record = json.loads(line)
            except json.JSONDecodeError:
                continue
            if not isinstance(record, dict):
                continue
            slug = record.get("slug")
            if not isinstance(slug, str) or not slug.strip():
                continue
            counts[slug] += 1
    return counts


def build_hotspots(graph: dict, invariant_counts: Counter) -> dict:
    node_meta = {node["slug"]: node for node in graph["nodes"]}
    pair_map = {}
    by_source = defaultdict(set)
    by_target = defaultdict(set)

    for edge in graph["edges"]:
        by_source[edge["source"]].add(edge["target"])
        by_target[edge["target"]].add(edge["source"])

    for edge in graph["edges"]:
        source = edge["source"]
        target = edge["target"]
        source_count = invariant_counts[source]
        target_count = invariant_counts[target]
        score = source_count + target_count
        pair_key = tuple(sorted((source, target)))
        record = pair_map.setdefault(
            pair_key,
            {
                "guide_a": pair_key[0],
                "guide_b": pair_key[1],
                "guide_a_invariants": invariant_counts[pair_key[0]],
                "guide_b_invariants": invariant_counts[pair_key[1]],
                "score": invariant_counts[pair_key[0]] + invariant_counts[pair_key[1]],
                "edge_types": set(),
                "direction_count": 0,
            },
        )
        record["edge_types"].add(edge["type"])
        record["direction_count"] += 1

    pair_scores = []
    for record in pair_map.values():
        serializable = dict(record)
        serializable["edge_types"] = sorted(serializable["edge_types"])
        pair_scores.append(serializable)

    pair_scores.sort(key=lambda item: (-item["score"], item["guide_a"], item["guide_b"]))

    guide_scores = []
    for slug, meta in node_meta.items():
        guide_scores.append(
            {
                "slug": slug,
                "guide_id": meta["guide_id"],
                "title": meta["title"],
                "category": meta["category"],
                "invariant_count": invariant_counts[slug],
                "out_neighbors": len(by_source[slug]),
                "in_neighbors": len(by_target[slug]),
                "combined_surface": invariant_counts[slug] + len(by_source[slug]) + len(by_target[slug]),
            }
        )
    guide_scores.sort(key=lambda item: (-item["combined_surface"], item["slug"]))

    return {
        "top_pairs": pair_scores[:100],
        "top_guides": guide_scores[:100],
    }


def markdown_code(value: object) -> str:
    text = str(value)
    tick_count = 1
    current_ticks = 0
    for char in text:
        if char == "`":
            current_ticks += 1
            tick_count = max(tick_count, current_ticks + 1)
        else:
            current_ticks = 0
    fence = "`" * tick_count
    if "`" in text:
        return f"{fence} {text} {fence}"
    return f"{fence}{text}{fence}"


def write_outputs(hotspots: dict, output_dir: Path) -> tuple[Path, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    json_path = output_dir / "guide_audit_hotspots.json"
    md_path = output_dir / "guide_audit_hotspots.md"

    json_path.write_text(json.dumps(hotspots, indent=2), encoding="utf-8")

    lines = [
        "# Guide Audit Hotspots",
        "",
        "## Top Guide Hotspots",
        "",
    ]
    for item in hotspots["top_guides"][:25]:
        lines.append(
            f"- {markdown_code(item['guide_id'])} {markdown_code(item['slug'])}: "
            f"invariants={item['invariant_count']}, in_neighbors={item['in_neighbors']}, "
            f"out_neighbors={item['out_neighbors']}, combined_surface={item['combined_surface']}"
        )

    lines.extend(["", "## Top Connected Pair Hotspots", ""])
    for item in hotspots["top_pairs"][:40]:
        lines.append(
            f"- {markdown_code(item['guide_a'])} <-> {markdown_code(item['guide_b'])}: "
            f"score={item['score']} "
            f"[{item['guide_a_invariants']} + {item['guide_b_invariants']}] "
            f"types={','.join(markdown_code(edge_type) for edge_type in item['edge_types'])} "
            f"directions={item['direction_count']}"
        )

    md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return json_path, md_path


def main() -> int:
    parser = argparse.ArgumentParser(description="Rank guide audit hotspots from graph + invariant surfaces.")
    parser.add_argument("--graph", default=str(GRAPH_PATH))
    parser.add_argument("--invariants", default=str(INVARIANT_PATH))
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT))
    args = parser.parse_args()

    graph = load_graph(Path(args.graph))
    invariant_counts = load_invariant_counts(Path(args.invariants))
    hotspots = build_hotspots(graph, invariant_counts)
    json_path, md_path = write_outputs(hotspots, Path(args.output_dir))

    print(f"Wrote hotspot JSON: {json_path}")
    print(f"Wrote hotspot summary: {md_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

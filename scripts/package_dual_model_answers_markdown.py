#!/usr/bin/env python3
"""Package two model answer bundles into one markdown review file."""

from __future__ import annotations

import argparse
import csv
import json
import sys
from collections import Counter
from datetime import datetime
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from bench_artifact_tools import load_bench_artifact  # noqa: E402


def markdown_value(value):
    if value is None:
        return ""
    if isinstance(value, str):
        return value.strip()
    try:
        return json.dumps(value, ensure_ascii=True, sort_keys=True).strip()
    except TypeError:
        return str(value).strip()


def load_structured_prompt_pack(path):
    prompt_path = Path(path)
    suffix = prompt_path.suffix.lower()
    if suffix == ".csv":
        with prompt_path.open("r", encoding="utf-8", newline="") as handle:
            reader = csv.DictReader(handle)
            return [_clean_prompt_row(row) for row in reader if _prompt_text(row)]
    if suffix == ".jsonl":
        rows = []
        with prompt_path.open("r", encoding="utf-8") as handle:
            for line in handle:
                line = line.strip()
                if not line:
                    continue
                rows.append(_clean_prompt_row(json.loads(line)))
        return [row for row in rows if row.get("question")]
    raise SystemExit(f"Unsupported prompt pack format for {prompt_path}")


def _prompt_text(row):
    return (row.get("prompt") or row.get("question") or "").strip()


def _clean_prompt_row(row):
    question = _prompt_text(row)
    section = (row.get("section") or "").strip() or "Unsectioned"
    return {
        "prompt_id": (row.get("id") or row.get("prompt_id") or "").strip(),
        "lane": (row.get("lane") or "").strip(),
        "section": section,
        "style": (row.get("style") or "").strip(),
        "target_behavior": (row.get("target_behavior") or "").strip(),
        "what_it_tests": (row.get("what_it_tests") or "").strip(),
        "question": question,
    }


def prompt_key(entry):
    prompt_id = (entry.get("prompt_id") or "").strip()
    if prompt_id:
        return ("prompt_id", prompt_id)
    return ("question", (entry.get("question") or "").strip())


def load_bundle(paths):
    rows = {}
    duplicates = []
    artifact_names = []
    for raw_path in paths:
        artifact = load_bench_artifact(raw_path)
        artifact_data = artifact["data"]
        artifact_name = artifact["path"].name
        artifact_names.append(artifact_name)
        artifact_model = artifact_data.get("config", {}).get("model")
        for result in artifact_data.get("results", []):
            key = prompt_key(result)
            if key in rows:
                duplicates.append((key, artifact_name))
                continue
            rows[key] = {
                "response_text": (result.get("response_text") or "").strip(),
                "error": result.get("error"),
                "artifact_name": artifact_name,
                "artifact_model": artifact_model,
                "result_model": result.get("model"),
                "decision_path": result.get("decision_path"),
                "source_mode": result.get("source_mode"),
                "duplicate_citation_count": result.get("duplicate_citation_count"),
            }
    return rows, artifact_names, duplicates


def render_model_block(label, record):
    lines = [f"**{label}**"]
    if record is None:
        lines.append("_Missing from selected artifacts._")
        return lines

    details = []
    model_name = markdown_value(record.get("result_model") or record.get("artifact_model"))
    if model_name:
        details.append(f"model=`{model_name}`")
    decision_path = markdown_value(record.get("decision_path"))
    if decision_path:
        details.append(f"path={decision_path}")
    source_mode = markdown_value(record.get("source_mode"))
    if source_mode:
        details.append(f"source={source_mode}")
    if record.get("duplicate_citation_count") is not None:
        details.append(f"dup_cites={record['duplicate_citation_count']}")
    if details:
        lines.append(f"*{' | '.join(details)}*")

    error = markdown_value(record.get("error"))
    if error:
        lines.append(f"**ERROR:** {error}")
        return lines

    response_text = markdown_value(record.get("response_text"))
    lines.append(response_text or "_No response text captured._")
    return lines


def render_markdown(prompt_entries, left_label, left_bundle, left_artifacts, right_label, right_bundle, right_artifacts):
    lane_counts = Counter(entry.get("lane") or "unlabeled" for entry in prompt_entries)
    lines = [
        f"# Senku Full Answer Package - {left_label} vs {right_label}",
        "",
        f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
        f"Total prompts: {len(prompt_entries)}",
        f"Prompt lanes: {dict(lane_counts)}",
        "",
        "## Source Artifacts",
        "",
        f"- {left_label}: {', '.join(left_artifacts)}",
        f"- {right_label}: {', '.join(right_artifacts)}",
        "",
    ]

    current_lane = None
    current_section = None
    for index, entry in enumerate(prompt_entries, start=1):
        lane = entry.get("lane") or "unlabeled"
        section = entry.get("section") or "Unsectioned"
        if lane != current_lane:
            current_lane = lane
            current_section = None
            lines.extend([f"## Lane: {lane}", ""])
        if section != current_section:
            current_section = section
            lines.extend([f"### {section}", ""])

        lines.append(f"#### {index}. {entry.get('prompt_id') or 'Prompt'}")
        meta_bits = []
        if entry.get("style"):
            meta_bits.append(f"style={entry['style']}")
        if entry.get("target_behavior"):
            meta_bits.append(f"target={entry['target_behavior']}")
        if meta_bits:
            lines.append(f"*{' | '.join(meta_bits)}*")
        if entry.get("what_it_tests"):
            lines.append(f"*What it tests:* {entry['what_it_tests']}")
        lines.extend([
            "",
            "**Prompt**",
            entry["question"],
            "",
        ])

        key = prompt_key(entry)
        lines.extend(render_model_block(left_label, left_bundle.get(key)))
        lines.append("")
        lines.extend(render_model_block(right_label, right_bundle.get(key)))
        lines.extend(["", "---", ""])

    return "\n".join(lines).rstrip() + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--prompts", required=True, help="Structured CSV or JSONL prompt pack")
    parser.add_argument("--left-label", required=True, help="Label for the first model bundle")
    parser.add_argument("--left-artifacts", nargs="+", required=True, help="Artifacts for the first model bundle")
    parser.add_argument("--right-label", required=True, help="Label for the second model bundle")
    parser.add_argument("--right-artifacts", nargs="+", required=True, help="Artifacts for the second model bundle")
    parser.add_argument("--output", required=True, help="Markdown output path")
    args = parser.parse_args()

    prompt_entries = load_structured_prompt_pack(args.prompts)
    left_bundle, left_artifacts, left_duplicates = load_bundle(args.left_artifacts)
    right_bundle, right_artifacts, right_duplicates = load_bundle(args.right_artifacts)

    missing_left = [entry for entry in prompt_entries if prompt_key(entry) not in left_bundle]
    missing_right = [entry for entry in prompt_entries if prompt_key(entry) not in right_bundle]

    if left_duplicates:
        raise SystemExit(f"Duplicate prompt keys found in {args.left_label} bundle: {left_duplicates[:5]}")
    if right_duplicates:
        raise SystemExit(f"Duplicate prompt keys found in {args.right_label} bundle: {right_duplicates[:5]}")

    rendered = render_markdown(
        prompt_entries,
        args.left_label,
        left_bundle,
        left_artifacts,
        args.right_label,
        right_bundle,
        right_artifacts,
    )

    output_path = Path(args.output)
    output_path.write_text(rendered, encoding="utf-8")

    print(f"Wrote {len(prompt_entries)} prompts to {output_path}")
    print(f"Missing {args.left_label}: {len(missing_left)}")
    print(f"Missing {args.right_label}: {len(missing_right)}")


if __name__ == "__main__":
    main()

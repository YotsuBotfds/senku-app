#!/usr/bin/env python3
"""Convert the plain-text Senku delta regression pack into a structured prompt pack."""

from __future__ import annotations

import argparse
import csv
import json
import re
from pathlib import Path


SECTION_PREFIX_RE = re.compile(r"^\d+\.\s*")

SECTION_METADATA = {
    "triage_paraphrase_regressions": {
        "section": "Delta / Triage Paraphrase Regressions",
        "style": "scenario",
        "target_behavior": "triage-priority-robustness",
        "what_it_tests": "preserve triage ordering across paraphrases instead of matching a narrow template",
    },
    "mixed_objective_answer_separation": {
        "section": "Delta / Mixed Objective Answer Separation",
        "style": "scenario",
        "target_behavior": "answer-lane-separation",
        "what_it_tests": "keep injury, shelter, water, logistics, and deadlines separated with explicit priority order",
    },
    "communication_trust_and_authentication": {
        "section": "Delta / Communication Trust And Authentication",
        "style": "direct",
        "target_behavior": "in-domain-comms-trust",
        "what_it_tests": "treat message trust, verification, and rumor resistance as an in-domain survival systems problem",
    },
    "tradeoff_decision_frameworks": {
        "section": "Delta / Tradeoff Decision Frameworks",
        "style": "tradeoff",
        "target_behavior": "conditional-decision-framework",
        "what_it_tests": "reward conditional decision logic instead of one-size-fits-all recommendations",
    },
    "logistics_and_physical_handling": {
        "section": "Delta / Logistics And Physical Handling",
        "style": "direct",
        "target_behavior": "practical-logistics-handling",
        "what_it_tests": "stay on practical hauling, transport, and physical handling instead of drifting into adjacent planning lanes",
    },
    "support_thin_and_adjacent_drift_guards": {
        "section": "Delta / Support Thin And Adjacent Drift Guards",
        "style": "direct",
        "target_behavior": "bounded-advice-honesty",
        "what_it_tests": "give bounded advice, admit thin support, and avoid borrowing from adjacent guides without justification",
    },
    "education_accessibility_and_skill_transfer": {
        "section": "Delta / Education Accessibility And Skill Transfer",
        "style": "teaching",
        "target_behavior": "education-accessibility-skill-transfer",
        "what_it_tests": "cover skill transfer, low-literacy operations, onboarding, and accessibility as core camp functions",
    },
    "domain_boundary_and_in_domain_misclass_tests": {
        "section": "Delta / Domain Boundary And In Domain Misclass Tests",
        "style": "direct",
        "target_behavior": "in-domain-boundary-routing",
        "what_it_tests": "keep valid communications, governance, and rumor-control prompts inside Senku's survival boundary",
    },
}


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--delta-prompts",
        required=True,
        help="Plain-text delta pack (.txt) with section headers",
    )
    parser.add_argument(
        "--output-prefix",
        required=True,
        help="Output prefix without extension; writes .txt, .csv, and .jsonl",
    )
    parser.add_argument(
        "--lane",
        default="delta",
        help="Lane label to assign to every delta prompt (default: delta)",
    )
    parser.add_argument(
        "--id-prefix",
        default="DX",
        help="Prompt ID prefix for generated rows (default: DX)",
    )
    return parser.parse_args()


def clean_text(value):
    if value is None:
        return ""
    return str(value).strip()


def canonical_section_key(raw_header):
    header = clean_text(raw_header)
    header = header.lstrip("#").strip()
    header = SECTION_PREFIX_RE.sub("", header)
    return header.strip().lower()


def parse_delta_prompt_entries(path, *, lane="delta", id_prefix="DX"):
    rows = []
    current_section_key = None
    current_metadata = None
    source_path = Path(path)

    for raw_line in source_path.read_text(encoding="utf-8").splitlines():
        line = clean_text(raw_line)
        if not line:
            continue
        if line.startswith("#"):
            section_key = canonical_section_key(line)
            if section_key == "senku delta regression pack — 96 prompts":
                continue
            if section_key == "senku delta regression pack - 96 prompts":
                continue
            current_section_key = section_key
            current_metadata = SECTION_METADATA.get(section_key)
            if current_metadata is None:
                raise ValueError(f"Unrecognized delta section header: {line}")
            continue

        # Allow freeform intro notes ahead of the first numbered section.
        if current_metadata is None:
            continue

        row_index = len(rows) + 1
        rows.append(
            {
                "id": f"{id_prefix}-{row_index:03d}",
                "lane": lane,
                "section": current_metadata["section"],
                "style": current_metadata["style"],
                "target_behavior": current_metadata["target_behavior"],
                "what_it_tests": current_metadata["what_it_tests"],
                "prompt": line,
                "source_pack": source_path.name,
                "source_index": row_index,
                "source_section_key": current_section_key,
            }
        )
    return rows


def write_txt(rows, output_path):
    lines = []
    current_section = None
    for row in rows:
        section = clean_text(row.get("section")) or "Delta"
        prompt = clean_text(row.get("prompt") or row.get("question"))
        if not prompt:
            continue
        if section != current_section:
            if lines:
                lines.append("")
            lines.append(f"# {section}")
            current_section = section
        lines.append(prompt)
    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def write_csv(rows, output_path):
    fieldnames = [
        "id",
        "lane",
        "section",
        "style",
        "target_behavior",
        "what_it_tests",
        "prompt",
        "source_pack",
        "source_index",
        "source_section_key",
    ]
    with output_path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        for row in rows:
            writer.writerow(row)


def write_jsonl(rows, output_path):
    with output_path.open("w", encoding="utf-8") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False) + "\n")


def main():
    args = parse_args()
    rows = parse_delta_prompt_entries(
        args.delta_prompts,
        lane=args.lane,
        id_prefix=args.id_prefix,
    )
    output_prefix = Path(args.output_prefix)
    output_prefix.parent.mkdir(parents=True, exist_ok=True)

    txt_path = output_prefix.with_suffix(".txt")
    csv_path = output_prefix.with_suffix(".csv")
    jsonl_path = output_prefix.with_suffix(".jsonl")

    write_txt(rows, txt_path)
    write_csv(rows, csv_path)
    write_jsonl(rows, jsonl_path)

    print(f"Delta prompts parsed: {len(rows)}")
    print(f"Wrote {txt_path}")
    print(f"Wrote {csv_path}")
    print(f"Wrote {jsonl_path}")


if __name__ == "__main__":
    main()

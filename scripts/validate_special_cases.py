#!/usr/bin/env python3
"""Lightweight validation for deterministic special-case routing."""

import re
import sys
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from guide_catalog import all_guide_ids
from deterministic_special_case_registry import VALID_PROMOTION_STATUSES
import query


OVERLAP_REQUIRED_FIELDS = {
    "source_rule_id",
    "sample_prompt",
    "matches",
    "winner_rule_ids",
    "winner_reason",
}

OVERLAP_MATCH_REQUIRED_FIELDS = {
    "rule_id",
    "priority",
    "lexical_signature_size",
    "promotion_status",
}


def _missing_fields(record, required_fields):
    return sorted(field for field in required_fields if field not in record)


def build_overlap_matrix_rows(overlaps):
    rows = []
    for overlap_index, overlap in enumerate(overlaps):
        missing_overlap_fields = _missing_fields(overlap, OVERLAP_REQUIRED_FIELDS)
        if missing_overlap_fields:
            raise ValueError(
                "malformed overlap record "
                f"{overlap_index}: missing {', '.join(missing_overlap_fields)}"
            )

        winner_rule_ids = list(overlap["winner_rule_ids"])
        for match_index, match in enumerate(overlap["matches"]):
            missing_match_fields = _missing_fields(match, OVERLAP_MATCH_REQUIRED_FIELDS)
            if missing_match_fields:
                source_rule_id = overlap["source_rule_id"]
                raise ValueError(
                    "malformed overlap match "
                    f"{overlap_index}.{match_index} for {source_rule_id}: "
                    f"missing {', '.join(missing_match_fields)}"
                )

            rows.append(
                {
                    "source_rule_id": overlap["source_rule_id"],
                    "sample_prompt": overlap["sample_prompt"],
                    "matched_rule_id": match["rule_id"],
                    "matched_priority": match["priority"],
                    "matched_lexical_signature_size": match["lexical_signature_size"],
                    "matched_promotion_status": match["promotion_status"],
                    "winner_rule_ids": winner_rule_ids,
                    "winner_reason": overlap["winner_reason"],
                    "is_winner": match["rule_id"] in winner_rule_ids,
                }
            )
    return rows


def write_overlap_matrix_json(path, rows):
    try:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(
            json.dumps(rows, indent=2, sort_keys=True),
            encoding="utf-8",
        )
    except (OSError, TypeError, ValueError) as exc:
        return f"failed to write --overlap-matrix-json {path}: {exc}"
    return None


def parse_args(argv=None):
    parser = argparse.ArgumentParser(
        description="Validate deterministic special-case routing.",
    )
    parser.add_argument(
        "--overlap-matrix-json",
        type=Path,
        help="Optional path to write flattened deterministic overlap rows as JSON.",
    )
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(argv)
    failures = []
    rules = query.get_deterministic_special_case_rules()
    seen_rule_ids = set()
    valid_guide_ids = all_guide_ids()
    overlaps = query.get_deterministic_special_case_overlaps()
    try:
        overlap_matrix_rows = build_overlap_matrix_rows(overlaps)
    except ValueError as exc:
        print(f"failed to build overlap matrix rows: {exc}", file=sys.stderr)
        return 2
    promotion_counts = {}

    if args.overlap_matrix_json:
        write_error = write_overlap_matrix_json(
            args.overlap_matrix_json,
            overlap_matrix_rows,
        )
        if write_error:
            print(write_error, file=sys.stderr)
            return 2

    for overlap in overlaps:
        if len(overlap["winner_rule_ids"]) != 1:
            matches = ", ".join(
                f"{match['rule_id']}@{match['priority']}"
                for match in overlap["matches"]
            )
            failures.append(
                "overlap unresolved for "
                f"{overlap['source_rule_id']}: {matches}"
            )

    for rule in rules:
        if rule.rule_id in seen_rule_ids:
            failures.append(f"duplicate rule_id: {rule.rule_id}")
        seen_rule_ids.add(rule.rule_id)
        promotion_counts[rule.promotion_status] = (
            promotion_counts.get(rule.promotion_status, 0) + 1
        )
        if not rule.promotion_status:
            failures.append(f"{rule.rule_id}: missing promotion_status")
        elif rule.promotion_status not in VALID_PROMOTION_STATUSES:
            failures.append(
                f"{rule.rule_id}: invalid promotion_status {rule.promotion_status!r}"
            )

        decision_path, decision_detail = query.classify_special_case(rule.sample_prompt)
        if decision_path != "deterministic" or decision_detail != rule.rule_id:
            failures.append(
                f"{rule.rule_id}: sample prompt misrouted to {decision_path}/{decision_detail}"
            )
            continue

        response = query.build_special_case_response(rule.sample_prompt)
        if not response:
            failures.append(f"{rule.rule_id}: empty response")
            continue

        normalized = query.normalize_response_text(response)
        citations = sorted(set(re.findall(r"GD-\d{3}", normalized)))
        if not citations:
            failures.append(f"{rule.rule_id}: no citations in response")
        else:
            missing_ids = [guide_id for guide_id in citations if guide_id not in valid_guide_ids]
            if missing_ids:
                failures.append(
                    f"{rule.rule_id}: unknown guide ids in response: {', '.join(missing_ids)}"
                )

        if "GD/" in normalized or "GD-" not in normalized:
            failures.append(f"{rule.rule_id}: malformed citation normalization")

    if failures:
        print("Deterministic special-case validation failed:")
        for failure in failures:
            print(f" - {failure}")
        return 1

    print(
        f"Validated {len(rules)} deterministic special-case rules with unique ids, "
        f"working sample prompts, normalized citations, {len(overlaps)} overlap "
        f"checks, and promotion statuses {promotion_counts}."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())

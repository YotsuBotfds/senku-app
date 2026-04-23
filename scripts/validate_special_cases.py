#!/usr/bin/env python3
"""Lightweight validation for deterministic special-case routing."""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from guide_catalog import all_guide_ids
from deterministic_special_case_registry import VALID_PROMOTION_STATUSES
import query


def main():
    failures = []
    rules = query.get_deterministic_special_case_rules()
    seen_rule_ids = set()
    valid_guide_ids = all_guide_ids()
    overlaps = query.get_deterministic_special_case_overlaps()
    promotion_counts = {}

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

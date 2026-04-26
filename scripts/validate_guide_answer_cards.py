#!/usr/bin/env python3
"""Lightweight validation for guide answer-card pilot YAML."""

from __future__ import annotations

import sys
import re
from pathlib import Path

try:
    import yaml
except ImportError:  # pragma: no cover - exercised in minimal envs.
    yaml = None


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SCHEMA_PATH = ROOT / "notes" / "specs" / "guide_answer_card_schema.yaml"
DEFAULT_CARDS_DIR = ROOT / "notes" / "specs" / "guide_answer_cards"
DEFAULT_GUIDES_DIR = ROOT / "guides"
SAFETY_CRITICAL_TIERS = {"critical", "high"}


def load_yaml(path: Path) -> object:
    if yaml is None:
        raise RuntimeError(
            "PyYAML is required to validate guide answer cards. "
            "Install PyYAML or run in the validation environment."
        )
    with path.open("r", encoding="utf-8") as handle:
        return yaml.safe_load(handle) or {}


def read_frontmatter(path: Path) -> dict:
    text = path.read_text(encoding="utf-8", errors="ignore")
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return {}
    try:
        closing_index = next(
            index for index, line in enumerate(lines[1:], start=1) if line.strip() == "---"
        )
    except StopIteration:
        return {}
    frontmatter = yaml.safe_load("\n".join(lines[1:closing_index])) or {}
    return frontmatter if isinstance(frontmatter, dict) else {}


def build_guide_lookup(guides_dir: Path) -> dict[str, dict]:
    lookup = {}
    for path in sorted(guides_dir.glob("*.md")):
        frontmatter = read_frontmatter(path)
        guide_id = frontmatter.get("id")
        if guide_id:
            lookup[str(guide_id)] = {
                "path": path,
                "slug": frontmatter.get("slug"),
                "title": frontmatter.get("title"),
            }
    return lookup


def is_nonempty(value: object) -> bool:
    if value is None:
        return False
    if isinstance(value, str):
        return bool(value.strip())
    if isinstance(value, (list, tuple, set, dict)):
        return bool(value)
    return True


def validate_card(path: Path, card: object, required_fields: list[str], guides: dict) -> list[str]:
    failures = []
    label = path.name
    if not isinstance(card, dict):
        return [f"{label}: card YAML must be a mapping"]

    for field in required_fields:
        if field not in card:
            failures.append(f"{label}: missing required field {field}")
        elif not is_nonempty(card.get(field)):
            failures.append(f"{label}: required field {field} must be nonempty")

    guide_id = card.get("guide_id")
    guide = guides.get(str(guide_id))
    if guide is None:
        failures.append(f"{label}: guide_id {guide_id!r} was not found under guides/")
    else:
        if card.get("slug") != guide.get("slug"):
            failures.append(
                f"{label}: slug {card.get('slug')!r} does not match "
                f"{guide_id} frontmatter {guide.get('slug')!r}"
            )
        if card.get("title") != guide.get("title"):
            failures.append(
                f"{label}: title {card.get('title')!r} does not match "
                f"{guide_id} frontmatter {guide.get('title')!r}"
            )

    if not is_nonempty(card.get("citation_ids")):
        failures.append(f"{label}: citation_ids must be nonempty")
    if not is_nonempty(card.get("source_sections")):
        failures.append(f"{label}: source_sections must be nonempty")

    if card.get("risk_tier") in SAFETY_CRITICAL_TIERS:
        if not is_nonempty(card.get("required_first_actions")):
            failures.append(
                f"{label}: required_first_actions must be nonempty for "
                f"{card.get('risk_tier')} risk_tier"
            )
        if not is_nonempty(card.get("forbidden_advice")):
            failures.append(
                f"{label}: forbidden_advice must be nonempty for "
                f"{card.get('risk_tier')} risk_tier"
            )

    failures.extend(validate_conditional_required_actions(label, card))
    failures.extend(validate_source_invariants(label, card, guides))
    return failures


def validate_conditional_required_actions(label: str, card: dict) -> list[str]:
    branches = card.get("conditional_required_actions")
    if branches is None:
        return []
    if not isinstance(branches, list):
        return [f"{label}: conditional_required_actions must be a list when present"]

    failures = []
    for index, branch in enumerate(branches, start=1):
        prefix = f"{label}: conditional_required_actions[{index}]"
        if not isinstance(branch, dict):
            failures.append(f"{prefix} must be a mapping")
            continue
        if not is_nonempty(branch.get("trigger_terms")):
            failures.append(f"{prefix}.trigger_terms must be nonempty")
        if not is_nonempty(branch.get("required_actions")):
            failures.append(f"{prefix}.required_actions must be nonempty")
    return failures


def validate_source_invariants(label: str, card: dict, guides: dict) -> list[str]:
    invariants = card.get("source_invariants")
    if invariants is None:
        return []
    if not isinstance(invariants, list):
        return [f"{label}: source_invariants must be a list when present"]

    failures = []
    text_cache: dict[str, str] = {}
    for index, invariant in enumerate(invariants, start=1):
        prefix = f"{label}: source_invariants[{index}]"
        if not isinstance(invariant, dict):
            failures.append(f"{prefix} must be a mapping")
            continue
        name = str(invariant.get("name") or "").strip()
        if not name:
            failures.append(f"{prefix}.name must be nonempty")
        guide_id = str(invariant.get("guide") or card.get("guide_id") or "").strip()
        guide = guides.get(guide_id)
        if guide is None:
            failures.append(f"{prefix}.guide {guide_id!r} was not found under guides/")
            continue
        checks = (
            invariant.get("must_include"),
            invariant.get("must_not_include"),
            invariant.get("must_not_match"),
        )
        if not any(is_nonempty(value) for value in checks):
            failures.append(
                f"{prefix} must define at least one of must_include, "
                "must_not_include, or must_not_match"
            )
            continue
        if guide_id not in text_cache:
            text_cache[guide_id] = guide["path"].read_text(
                encoding="utf-8", errors="ignore"
            ).lower()
        guide_text = text_cache[guide_id]
        for phrase in as_string_list(invariant.get("must_include")):
            if phrase.lower() not in guide_text:
                failures.append(
                    f"{prefix}.must_include missing from {guide_id}: {phrase!r}"
                )
        for phrase in as_string_list(invariant.get("must_not_include")):
            if phrase.lower() in guide_text:
                failures.append(
                    f"{prefix}.must_not_include found in {guide_id}: {phrase!r}"
                )
        for pattern in as_string_list(invariant.get("must_not_match")):
            try:
                matched = re.search(pattern, guide_text, flags=re.IGNORECASE)
            except re.error as exc:
                failures.append(f"{prefix}.must_not_match invalid regex {pattern!r}: {exc}")
                continue
            if matched:
                failures.append(
                    f"{prefix}.must_not_match matched in {guide_id}: {pattern!r}"
                )
    return failures


def as_string_list(value: object) -> list[str]:
    if value is None:
        return []
    if isinstance(value, str):
        return [value]
    if isinstance(value, (list, tuple, set)):
        return [str(item) for item in value if str(item).strip()]
    return [str(value)]


def validate(
    schema_path: Path = DEFAULT_SCHEMA_PATH,
    cards_dir: Path = DEFAULT_CARDS_DIR,
    guides_dir: Path = DEFAULT_GUIDES_DIR,
) -> tuple[list[str], int]:
    schema = load_yaml(schema_path)
    if not isinstance(schema, dict):
        return [f"{schema_path}: schema YAML must be a mapping"], 0

    required_fields = schema.get("card", {}).get("required_fields", [])
    if not isinstance(required_fields, list) or not required_fields:
        return [f"{schema_path}: schema.card.required_fields must be a nonempty list"], 0

    card_paths = sorted(cards_dir.glob("*.yaml"))
    if not card_paths:
        return [f"{cards_dir}: no guide answer-card YAML files found"], 0

    guides = build_guide_lookup(guides_dir)
    failures = []
    for card_path in card_paths:
        failures.extend(validate_card(card_path, load_yaml(card_path), required_fields, guides))
    return failures, len(card_paths)


def main() -> int:
    try:
        failures, card_count = validate()
    except RuntimeError as exc:
        print(f"Guide answer-card validation failed: {exc}", file=sys.stderr)
        return 1

    if failures:
        print("Guide answer-card validation failed:")
        for failure in failures:
            print(f" - {failure}")
        return 1

    print(f"Validated {card_count} guide answer cards against schema and guide frontmatter.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

#!/usr/bin/env python3
"""Regenerate the deterministic registry from a YAML sidecar plus introspection."""

from __future__ import annotations

import argparse
import importlib
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

import deterministic_special_case_registry as registry

REGISTRY_PATH = ROOT / "deterministic_special_case_registry.py"
SIDECAR_PATH = ROOT / "notes" / "specs" / "deterministic_registry_sidecar.yaml"

HEADER = '''"""Declarative registry for Senku deterministic special-case rules."""

from dataclasses import dataclass

PACK_ONLY_PROMOTION_NOTE = (
    "Pack-exported metadata only; not yet promoted to the live Android "
    "deterministic router."
)
ANDROID_ACTIVE_PROMOTION_NOTE = (
    "Live in android-app/app/src/main/java/com/senku/mobile/"
    "DeterministicAnswerRouter.java."
)
VALID_PROMOTION_STATUSES = (
    "inactive",
    "candidate",
    "under-review",
    "active",
)


@dataclass(frozen=True)
class DeterministicSpecialCaseSpec:
    """Static metadata for one deterministic control-path rule."""

    rule_id: str
    predicate_name: str
    builder_name: str
    sample_prompt: str
    priority: int = 100
    promotion_status: str = "inactive"
    promotion_notes: str | None = PACK_ONLY_PROMOTION_NOTE
    lexical_signature_terms: tuple[str, ...] = ()


DETERMINISTIC_SPECIAL_CASE_SPECS = (
'''

FOOTER = ")\n"

PROMOTION_CONSTANTS = {
    registry.PACK_ONLY_PROMOTION_NOTE: "PACK_ONLY_PROMOTION_NOTE",
    registry.ANDROID_ACTIVE_PROMOTION_NOTE: "ANDROID_ACTIVE_PROMOTION_NOTE",
}


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Regenerate deterministic_special_case_registry.py from a YAML sidecar."
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Exit non-zero instead of writing when the regenerated registry differs.",
    )
    parser.add_argument(
        "--bootstrap-sidecar",
        action="store_true",
        help="Create or refresh the YAML sidecar from the current checked-in registry.",
    )
    return parser.parse_args(argv)


def current_rules_as_yaml() -> dict:
    rules = []
    for spec in registry.DETERMINISTIC_SPECIAL_CASE_SPECS:
        rules.append(
            {
                "rule_id": spec.rule_id,
                "predicate_name": spec.predicate_name,
                "builder_name": spec.builder_name,
                "sample_prompt": spec.sample_prompt,
                "priority": spec.priority,
                "promotion_status": spec.promotion_status,
                "promotion_notes": spec.promotion_notes,
                "lexical_signature_terms": list(spec.lexical_signature_terms),
            }
        )
    return {"rules": rules}


def bootstrap_sidecar() -> None:
    SIDECAR_PATH.parent.mkdir(parents=True, exist_ok=True)
    SIDECAR_PATH.write_text(
        yaml.safe_dump(current_rules_as_yaml(), sort_keys=False, allow_unicode=False),
        encoding="utf-8",
    )


def load_sidecar() -> list[dict]:
    if not SIDECAR_PATH.exists():
        raise FileNotFoundError(
            f"missing sidecar: {SIDECAR_PATH}. Run with --bootstrap-sidecar once first."
        )
    payload = yaml.safe_load(SIDECAR_PATH.read_text(encoding="utf-8")) or {}
    rules = payload.get("rules")
    if not isinstance(rules, list) or not rules:
        raise ValueError(f"{SIDECAR_PATH} must define a non-empty 'rules' list")
    return rules


def validate_rule_names(rules: list[dict]) -> None:
    query_module = importlib.import_module("query")
    builders_module = importlib.import_module("special_case_builders")
    valid_statuses = set(registry.VALID_PROMOTION_STATUSES)
    for row in rules:
        rule_id = str(row.get("rule_id") or "").strip()
        predicate_name = str(row.get("predicate_name") or "").strip()
        builder_name = str(row.get("builder_name") or "").strip()
        promotion_status = str(row.get("promotion_status") or "").strip()
        if not rule_id:
            raise ValueError("rule_id cannot be blank")
        predicate_exists = (
            bool(predicate_name)
            and (hasattr(query_module, predicate_name) or hasattr(builders_module, predicate_name))
        )
        if not predicate_exists:
            raise ValueError(
                f"{rule_id}: missing predicate '{predicate_name}' in query.py or special_case_builders.py"
            )
        if not builder_name or not hasattr(builders_module, builder_name):
            raise ValueError(f"{rule_id}: missing builder '{builder_name}' in special_case_builders.py")
        if promotion_status not in valid_statuses:
            raise ValueError(f"{rule_id}: invalid promotion_status '{promotion_status}'")


def _render_promotion_notes(value: str | None) -> str:
    if value is None:
        return "None"
    constant_name = PROMOTION_CONSTANTS.get(value)
    if constant_name:
        return constant_name
    return repr(value)


def _render_lexical_signature_terms(values: list[str]) -> str:
    if not values:
        return "()"
    rendered = ", ".join(repr(str(value)) for value in values)
    if len(values) == 1:
        rendered += ","
    return f"({rendered})"


def render_registry(rules: list[dict]) -> str:
    chunks = [HEADER]
    for row in rules:
        priority = int(row.get("priority", 100))
        promotion_status = str(row.get("promotion_status") or "inactive")
        promotion_notes = row.get("promotion_notes", registry.PACK_ONLY_PROMOTION_NOTE)
        lexical_signature_terms = list(row.get("lexical_signature_terms") or [])
        lines = [
            "    DeterministicSpecialCaseSpec(",
            f'        {row["rule_id"]!r},',
            f'        {row["predicate_name"]!r},',
            f'        {row["builder_name"]!r},',
            f'        {row["sample_prompt"]!r},',
        ]
        if priority != 100:
            lines.append(f"        priority={priority},")
        if promotion_status != "inactive":
            lines.append(f'        promotion_status={promotion_status!r},')
        if promotion_notes != registry.PACK_ONLY_PROMOTION_NOTE:
            lines.append(f"        promotion_notes={_render_promotion_notes(promotion_notes)},")
        if lexical_signature_terms:
            lines.append(
                f"        lexical_signature_terms={_render_lexical_signature_terms(lexical_signature_terms)},"
            )
        lines.append("    ),")
        chunks.append("\n".join(lines) + "\n")
    chunks.append(FOOTER)
    return "".join(chunks)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    if args.bootstrap_sidecar or not SIDECAR_PATH.exists():
        bootstrap_sidecar()
    rules = load_sidecar()
    validate_rule_names(rules)
    rendered = render_registry(rules)
    current = REGISTRY_PATH.read_text(encoding="utf-8")
    if args.check:
        if rendered != current:
            print("deterministic registry is out of date", file=sys.stderr)
            return 1
        print("deterministic registry is up to date")
        return 0
    if rendered != current:
        REGISTRY_PATH.write_text(rendered, encoding="utf-8")
        print(f"updated {REGISTRY_PATH}")
    else:
        print(f"no changes for {REGISTRY_PATH}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

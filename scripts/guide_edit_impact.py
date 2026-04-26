#!/usr/bin/env python3
"""Plan focused validation for guide/card edits."""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

try:
    from ingest import parse_frontmatter
except Exception:  # pragma: no cover - keeps --help usable in thin envs.
    parse_frontmatter = None


PYTHON = r".\.venvs\senku-validate\Scripts\python.exe"
HIGH_LIABILITY_LEVELS = {"high", "critical"}
CARD_DIR = Path("notes/specs/guide_answer_cards")
RUNTIME_PREFIXES = (
    "query.py",
    "config.py",
    "bench.py",
    "guide_catalog.py",
    "guide_answer_card_contracts.py",
    "metadata_helpers.py",
    "query_answer_card_runtime.py",
    "mobile_pack_answer_cards.py",
    "ingest.py",
    "deterministic_special_case_registry.py",
    "special_case_builders.py",
)
PROTECTED_BENIGN_UNTRACKED = {
    "notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md",
    "notes/PLANNER_HANDOFF_2026-04-25_POST_CLI_TERMINATION.md",
    "notes/PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md",
    "notes/PLANNER_HANDOFF_2026-04-26_POST_CARD5_PAUSE.md",
}
PROTECTED_HANDOFF_PATTERN = re.compile(
    r"^notes/PLANNER_HANDOFF_\d{4}-\d{2}-\d{2}_[A-Z0-9_]+\.md$"
)


@dataclass(frozen=True)
class Change:
    path: str
    categories: tuple[str, ...]
    exists: bool
    high_liability: bool = False


def normalize_path(path: str | Path) -> str:
    value = str(path).strip().strip('"')
    value = value.replace("\\", "/")
    value = value.replace("\x00", "<NUL>")
    while value.startswith("./"):
        value = value[2:]
    while "//" in value:
        value = value.replace("//", "/")
    return value


def path_exists(path: str) -> bool:
    if not path:
        return False
    try:
        return (REPO_ROOT / path).exists()
    except (OSError, ValueError):
        return False


def is_protected_benign_untracked(path: str) -> bool:
    return path in PROTECTED_BENIGN_UNTRACKED or bool(
        PROTECTED_HANDOFF_PATTERN.fullmatch(path)
    )


def read_git_status_paths() -> list[str]:
    result = subprocess.run(
        ["git", "status", "--short"],
        cwd=REPO_ROOT,
        check=True,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    paths: list[str] = []
    for line in result.stdout.splitlines():
        if not line.strip():
            continue
        payload = line[3:] if len(line) > 3 else line.strip()
        if " -> " in payload:
            payload = payload.split(" -> ", 1)[1]
        normalized = normalize_path(payload)
        if line[:2] == "??" and is_protected_benign_untracked(normalized):
            continue
        paths.append(normalized)
    return paths


def classify_path(path: str | Path) -> Change:
    normalized = normalize_path(path)
    lower = normalized.lower()
    categories: list[str] = []

    if lower.startswith("guides/") and lower.endswith(".md"):
        categories.extend(["guide", "frontmatter"])
    if lower.startswith(str(CARD_DIR).replace("\\", "/") + "/") and lower.endswith(
        (".yaml", ".yml")
    ):
        categories.append("card")
    if lower.startswith("tests/") or lower.startswith("android-app/app/src/test/"):
        categories.append("test")
    if lower.startswith("notes/"):
        categories.append("note")
    if lower.startswith("scripts/"):
        categories.append("runtime")
    if lower in RUNTIME_PREFIXES or lower.startswith("android-app/"):
        categories.append("runtime")
    if not categories:
        categories.append("other")

    return Change(
        path=normalized,
        categories=tuple(dict.fromkeys(categories)),
        exists=path_exists(normalized),
        high_liability=is_high_liability_guide(normalized),
    )


def is_high_liability_guide(path: str) -> bool:
    if not (path.startswith("guides/") and path.endswith(".md")):
        return False
    full_path = REPO_ROOT / path
    if not full_path.exists() or parse_frontmatter is None:
        return False
    try:
        metadata, _body = parse_frontmatter(full_path.read_text(encoding="utf-8"))
    except Exception:
        return False
    level = str(metadata.get("liability_level") or "").strip().lower()
    return level in HIGH_LIABILITY_LEVELS


def focused_test_for_script(path: str) -> str | None:
    normalized = normalize_path(path)
    if not normalized.endswith(".py"):
        return None
    if not (normalized.startswith("scripts/") or normalized in RUNTIME_PREFIXES):
        return None

    test_path = Path("tests") / f"test_{Path(normalized).stem}.py"
    if (REPO_ROOT / test_path).exists():
        return f"& {PYTHON} -B -m unittest {test_path.as_posix().replace('/', '.')[:-3]} -v"
    return None


def powershell_quote(value: str) -> str:
    value = value.replace("/", "\\")
    if value and all(char.isalnum() or char in "_.:\\-" for char in value):
        return value
    return "'" + value.replace("'", "''") + "'"


def incremental_ingest_plan_command(paths: list[str]) -> str:
    args = " ".join(powershell_quote(path) for path in sorted(dict.fromkeys(paths)))
    return f"& {PYTHON} -B scripts\\plan_incremental_ingest.py {args}".rstrip()


def build_plan(paths: list[str]) -> dict[str, Any]:
    changes = [classify_path(path) for path in paths]
    category_map: dict[str, list[str]] = {}
    for change in changes:
        for category in change.categories:
            category_map.setdefault(category, []).append(change.path)

    commands: list[dict[str, str]] = []
    if "guide" in category_map:
        commands.append(
            {
                "reason": "Plan focused incremental ingest for touched guide markdown before trusting retrieval behavior.",
                "command": incremental_ingest_plan_command(category_map["guide"]),
            }
        )
        commands.append(
            {
                "reason": "Marker scan for guide markdown and routing markers.",
                "command": f"& {PYTHON} -B scripts\\scan_corpus_markers.py --fail-on-unresolved",
            }
        )
    if "guide" in category_map and any(change.high_liability for change in changes):
        commands.append(
            {
                "reason": "Metadata audit for touched high-liability guides.",
                "command": f"& {PYTHON} -B scripts\\audit_metadata_coverage.py",
            }
        )
    if "card" in category_map:
        commands.append(
            {
                "reason": "Validate guide answer-card YAML contracts.",
                "command": f"& {PYTHON} -B scripts\\validate_guide_answer_cards.py",
            }
        )

    seen_script_tests: set[str] = set()
    for change in changes:
        command = focused_test_for_script(change.path)
        if command and command not in seen_script_tests:
            seen_script_tests.add(command)
            commands.append(
                {
                    "reason": f"Focused test for touched script {change.path}.",
                    "command": command,
                }
            )

    return {
        "changed_files": [change.__dict__ for change in changes],
        "categories": {key: sorted(value) for key, value in sorted(category_map.items())},
        "commands": commands,
    }


def render_markdown(plan: dict[str, Any]) -> str:
    lines = ["# Guide Edit Impact", ""]
    lines.append("## Changes")
    for change in plan["changed_files"]:
        flags = ", ".join(change["categories"])
        suffix = " high-liability" if change["high_liability"] else ""
        missing = " missing" if not change["exists"] else ""
        lines.append(f"- `{change['path']}`: {flags}{suffix}{missing}")
    if not plan["changed_files"]:
        lines.append("- none")

    lines.extend(["", "## Validation"])
    if plan["commands"]:
        for item in plan["commands"]:
            lines.append(f"- {item['reason']}")
            lines.append(f"  `{item['command']}`")
    else:
        lines.append("- No focused guide/card validation needed from these paths.")
    return "\n".join(lines)


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Classify guide/card edits and suggest focused validation."
    )
    parser.add_argument("paths", nargs="*", help="Changed file paths to classify.")
    parser.add_argument(
        "--from-git-status",
        action="store_true",
        help="Use paths from git status --short instead of positional paths.",
    )
    parser.add_argument("--json", action="store_true", help="Emit compact JSON.")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    paths = read_git_status_paths() if args.from_git_status else args.paths
    plan = build_plan(paths)
    if args.json:
        print(json.dumps(plan, indent=2, sort_keys=True))
    else:
        print(render_markdown(plan))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

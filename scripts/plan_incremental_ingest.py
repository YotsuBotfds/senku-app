#!/usr/bin/env python3
"""Plan safe incremental guide ingest commands without mutating stores."""

import argparse
import json
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path


DEFAULT_PYTHON = r".\.venvs\senku-validate\Scripts\python.exe"
GUIDE_DIR = "guides"
SPEC_PREFIXES = ("notes/specs/",)
SPEC_EXTENSIONS = {".md", ".yaml", ".yml", ".json", ".jsonl"}


@dataclass(frozen=True)
class ChangedPath:
    path: str
    status: str = "M"


def repo_root_from_script() -> Path:
    return Path(__file__).resolve().parents[1]


def _clean_rel_path(path: str, root: Path) -> str:
    raw = str(path).strip().strip('"')
    if not raw:
        return ""
    candidate = Path(raw)
    if candidate.is_absolute():
        try:
            raw = str(candidate.resolve().relative_to(root.resolve()))
        except ValueError:
            raw = str(candidate)
    raw = raw.replace("\\", "/")
    while raw.startswith("./"):
        raw = raw[2:]
    return raw.strip("/")


def _existing_guide_basename_path(rel_path: str, root: Path) -> str:
    if "/" in rel_path:
        return rel_path
    candidate = root / GUIDE_DIR / rel_path
    if candidate.is_file():
        return f"{GUIDE_DIR}/{rel_path}"
    return rel_path


def normalize_requested_path(path: str, root: Path) -> str:
    rel_path = _clean_rel_path(path, root)
    return _existing_guide_basename_path(rel_path, root)


def is_guide_markdown(rel_path: str) -> bool:
    return rel_path.startswith(f"{GUIDE_DIR}/") and Path(rel_path).suffix.lower() == ".md"


def is_spec_file(rel_path: str) -> bool:
    return (
        rel_path.startswith(SPEC_PREFIXES)
        and Path(rel_path).suffix.lower() in SPEC_EXTENSIONS
    )


def _dedupe_sorted(paths):
    return sorted(dict.fromkeys(path for path in paths if path))


def _git(args, cwd: Path) -> str:
    completed = subprocess.run(
        ["git", *args],
        cwd=str(cwd),
        check=True,
        capture_output=True,
        text=True,
    )
    return completed.stdout


def collect_git_changes(root: Path, git_runner=_git):
    """Return changed tracked and untracked paths from git status-like data."""
    changes = []

    name_status = git_runner(["diff", "--name-status", "-M", "HEAD", "--"], root)
    for line in name_status.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t")
        status = parts[0]
        if status.startswith(("R", "C")) and len(parts) >= 3:
            changes.append(ChangedPath(parts[1], status="D"))
            changes.append(ChangedPath(parts[2], status=status[0]))
        elif len(parts) >= 2:
            changes.append(ChangedPath(parts[1], status=status[:1] or "M"))

    untracked = git_runner(["ls-files", "--others", "--exclude-standard"], root)
    for line in untracked.splitlines():
        if line.strip():
            changes.append(ChangedPath(line.strip(), status="??"))

    return changes


def build_plan(
    paths=None,
    *,
    root=None,
    force_files=False,
    python_path=DEFAULT_PYTHON,
    git_runner=_git,
):
    root = Path(root) if root is not None else repo_root_from_script()
    explicit_paths = paths is not None and len(paths) > 0
    raw_changes = (
        [ChangedPath(path, status="M") for path in paths]
        if explicit_paths
        else collect_git_changes(root, git_runner=git_runner)
    )

    guide_files = []
    deleted_guide_files = []
    spec_files = []
    deleted_spec_files = []
    other_files = []

    for change in raw_changes:
        rel_path = normalize_requested_path(change.path, root)
        if not rel_path:
            continue
        file_exists = (root / rel_path).is_file()
        deleted = change.status == "D" or (explicit_paths and not file_exists)
        if is_guide_markdown(rel_path):
            if deleted:
                deleted_guide_files.append(rel_path)
            else:
                guide_files.append(rel_path)
        elif is_spec_file(rel_path):
            if deleted:
                deleted_spec_files.append(rel_path)
            else:
                spec_files.append(rel_path)
        else:
            other_files.append(rel_path)

    guide_files = _dedupe_sorted(guide_files)
    deleted_guide_files = _dedupe_sorted(deleted_guide_files)
    spec_files = _dedupe_sorted(spec_files)
    deleted_spec_files = _dedupe_sorted(deleted_spec_files)
    other_files = _dedupe_sorted(other_files)

    warnings = []
    if deleted_guide_files:
        warnings.append(
            "Deleted or missing guide markdown cannot be removed by "
            "ingest.py --files; run a full rebuild or restore the path."
        )
    if spec_files or deleted_spec_files:
        warnings.append(
            "Spec files are not ingested by ingest.py --files; run the relevant "
            "spec/card/prompt validators for those changes."
        )
    if other_files:
        warnings.append(
            "Non-guide files were detected; no ingest command is emitted for them."
        )

    commands = []
    if guide_files:
        commands.append(format_ingest_command(guide_files, force_files, python_path))
        commands.append(format_stats_command(python_path))
    elif deleted_guide_files:
        commands.append(format_rebuild_command(python_path))
        commands.append(format_stats_command(python_path))

    return {
        "source": "explicit" if explicit_paths else "git",
        "dry_run": True,
        "force_files": bool(force_files),
        "guide_files": guide_files,
        "deleted_guide_files": deleted_guide_files,
        "spec_files": spec_files,
        "deleted_spec_files": deleted_spec_files,
        "other_files": other_files,
        "warnings": warnings,
        "commands": commands,
    }


_PS_SIMPLE = re.compile(r"^[A-Za-z0-9_.:/\\-]+$")


def ps_quote(value: str) -> str:
    value = value.replace("/", "\\")
    if _PS_SIMPLE.match(value):
        return value
    return "'" + value.replace("'", "''") + "'"


def format_ingest_command(guide_files, force_files=False, python_path=DEFAULT_PYTHON):
    args = [ps_quote(path) for path in guide_files]
    command = f"& {ps_quote(python_path)} -B .\\ingest.py --files {' '.join(args)}"
    if force_files:
        command += " --force-files"
    return command


def format_stats_command(python_path=DEFAULT_PYTHON):
    return f"& {ps_quote(python_path)} -B .\\ingest.py --stats"


def format_rebuild_command(python_path=DEFAULT_PYTHON):
    return f"& {ps_quote(python_path)} -B .\\ingest.py --rebuild"


def _render_list(lines, title, values):
    lines.append(title)
    if values:
        for value in values:
            lines.append(f"  - {value}")
    else:
        lines.append("  - none")


def render_plan(plan):
    lines = [
        "# Incremental Ingest Plan",
        "",
        f"- Source: {plan['source']}",
        "- Mode: dry run only; no ingest data is mutated.",
        f"- Force unchanged guide files: {'yes' if plan['force_files'] else 'no'}",
        "",
    ]
    _render_list(lines, "Guide markdown selected for incremental ingest:", plan["guide_files"])
    _render_list(lines, "Deleted or missing guide markdown:", plan["deleted_guide_files"])
    _render_list(lines, "Spec files detected:", plan["spec_files"])
    _render_list(lines, "Deleted or missing spec files:", plan["deleted_spec_files"])
    _render_list(lines, "Other files detected:", plan["other_files"])

    lines.append("")
    _render_list(lines, "Warnings:", plan["warnings"])
    lines.append("")
    lines.append("Commands:")
    if plan["commands"]:
        for command in plan["commands"]:
            lines.append(f"  {command}")
    else:
        lines.append("  - none")

    lines.extend(
        [
            "",
            "Guidance:",
            "  - Run incremental ingest only when an existing collection is present.",
            "  - Make sure the embedding LM Studio endpoint is healthy before ingest.",
            "  - Keep the stats command paired with each incremental batch before trusting retrieval.",
            "  - Use --force-files after ingest plumbing changes that require re-embedding unchanged guide files.",
        ]
    )
    return "\n".join(lines) + "\n"


def main(argv=None):
    parser = argparse.ArgumentParser(
        description="Plan safe ingest.py --files commands for changed guide files."
    )
    parser.add_argument(
        "paths",
        nargs="*",
        help="Explicit guide/spec paths. If omitted, changed paths are read from git.",
    )
    parser.add_argument("--root", default=str(repo_root_from_script()), help="Repo root.")
    parser.add_argument(
        "--force-files",
        action="store_true",
        help="Include --force-files in the planned incremental ingest command.",
    )
    parser.add_argument(
        "--python-path",
        default=DEFAULT_PYTHON,
        help="Python executable to show in planned commands.",
    )
    parser.add_argument("--json", action="store_true", help="Print the plan as JSON.")
    args = parser.parse_args(argv)

    try:
        plan = build_plan(
            args.paths,
            root=Path(args.root),
            force_files=args.force_files,
            python_path=args.python_path,
        )
    except subprocess.CalledProcessError as exc:
        sys.stderr.write(f"git change detection failed: {exc}\n")
        return 2

    if args.json:
        print(json.dumps(plan, indent=2, sort_keys=True))
    else:
        print(render_plan(plan), end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

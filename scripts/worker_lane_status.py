#!/usr/bin/env python3
"""Report git worktree worker lanes and lane lease files."""

from __future__ import annotations

import argparse
import json
import subprocess
from pathlib import Path
from typing import Any, Callable, Sequence


DEFAULT_LEASE_DIR = Path("artifacts") / "runs" / "worker_lanes"

CommandRunner = Callable[[Sequence[str], Path], subprocess.CompletedProcess[str]]


def run_command(command: Sequence[str], cwd: Path) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        list(command),
        cwd=cwd,
        check=False,
        capture_output=True,
        text=True,
        timeout=10,
    )


def _norm_path(value: str | Path) -> str:
    try:
        return str(Path(value).resolve()).casefold()
    except OSError:
        return str(value).replace("\\", "/").casefold()


def parse_worktree_porcelain(text: str) -> list[dict[str, Any]]:
    """Parse `git worktree list --porcelain` output."""

    records: list[dict[str, Any]] = []
    current: dict[str, Any] = {}
    last_key: str | None = None

    def finish() -> None:
        nonlocal current, last_key
        if current:
            records.append(current)
        current = {}
        last_key = None

    for raw_line in text.splitlines():
        line = raw_line.rstrip("\n")
        if not line:
            finish()
            continue
        if " " in line:
            key, value = line.split(" ", 1)
        else:
            key, value = line, True
        if key in {"locked", "prunable"} and last_key == key and isinstance(current.get(key), bool):
            current[f"{key}_reason"] = value if isinstance(value, str) else ""
        elif key in current:
            existing = current[key]
            if isinstance(existing, list):
                existing.append(value)
            else:
                current[key] = [existing, value]
        else:
            current[key] = value
        last_key = key
    finish()

    for record in records:
        branch = record.get("branch")
        if isinstance(branch, str) and branch.startswith("refs/heads/"):
            record["branch_short"] = branch.removeprefix("refs/heads/")
        elif isinstance(branch, str):
            record["branch_short"] = branch
        else:
            record["branch_short"] = ""
        record["detached"] = "detached" in record or not bool(record["branch_short"])
    return records


def load_lane_leases(lease_dir: Path) -> dict[str, Any]:
    leases: list[dict[str, Any]] = []
    malformed: list[dict[str, str]] = []
    if not lease_dir.is_dir():
        return {"leases": leases, "malformed": malformed, "lease_dir": str(lease_dir)}

    for path in sorted(lease_dir.glob("*.json")):
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            malformed.append({"path": str(path), "error": exc.__class__.__name__})
            continue
        if not isinstance(payload, dict):
            malformed.append({"path": str(path), "error": "not_object"})
            continue
        payload = dict(payload)
        payload.setdefault("lease_file", str(path))
        leases.append(payload)
    return {"leases": leases, "malformed": malformed, "lease_dir": str(lease_dir)}


def _dirty_status_label(index_status: str, worktree_status: str) -> str:
    if index_status == "R" or worktree_status == "R":
        return "renamed"
    if index_status == "?" and worktree_status == "?":
        return "untracked"
    if index_status == "M" or worktree_status == "M":
        return "modified"
    if index_status == "A" or worktree_status == "A":
        return "added"
    if index_status == "D" or worktree_status == "D":
        return "deleted"
    if index_status == "C" or worktree_status == "C":
        return "copied"
    if index_status == "U" or worktree_status == "U":
        return "unmerged"
    return "changed"


def parse_git_status_short_line(line: str) -> dict[str, str]:
    """Parse one `git status --short` line into a stable summary."""

    if len(line) < 3:
        return {
            "raw": line,
            "index_status": "",
            "worktree_status": "",
            "status": "changed",
            "path": line.strip(),
        }

    index_status = line[0]
    worktree_status = line[1]
    path_text = line[3:]
    detail = {
        "raw": line,
        "index_status": index_status,
        "worktree_status": worktree_status,
        "status": _dirty_status_label(index_status, worktree_status),
        "path": path_text,
    }
    if " -> " in path_text and detail["status"] == "renamed":
        old_path, new_path = path_text.split(" -> ", 1)
        detail["original_path"] = old_path
        detail["path"] = new_path
    return detail


def parse_git_status_short(text: str) -> tuple[list[str], list[dict[str, str]], dict[str, int]]:
    entries = [line for line in text.splitlines() if line.strip()]
    entry_details = [parse_git_status_short_line(line) for line in entries]
    status_counts: dict[str, int] = {}
    for detail in entry_details:
        status = detail["status"]
        status_counts[status] = status_counts.get(status, 0) + 1
    return entries, entry_details, status_counts


def _markdown_table_cell(value: Any) -> str:
    return str(value or "").replace("\r", " ").replace("\n", " ").replace("|", "\\|")


def _dirty_summary(path: Path, runner: CommandRunner) -> dict[str, Any]:
    result = runner(["git", "-C", str(path), "status", "--short"], path)
    if result.returncode:
        return {
            "clean": False,
            "changed": 0,
            "entries": [],
            "entry_details": [],
            "status_counts": {},
            "error": (result.stderr or result.stdout or f"git exited {result.returncode}").strip(),
        }
    entries, entry_details, status_counts = parse_git_status_short(result.stdout)
    return {
        "clean": not entries,
        "changed": len(entries),
        "entries": entries[:20],
        "entry_details": entry_details[:20],
        "status_counts": status_counts,
        "truncated": len(entries) > 20,
    }


def collect_status(
    repo_root: Path,
    *,
    lease_dir: Path | None = None,
    runner: CommandRunner = run_command,
    include_dirty: bool = True,
) -> dict[str, Any]:
    root = repo_root.resolve()
    lease_path = (lease_dir or root / DEFAULT_LEASE_DIR).resolve()
    worktree_result = runner(["git", "worktree", "list", "--porcelain"], root)
    lease_data = load_lane_leases(lease_path)

    if worktree_result.returncode:
        return {
            "repo_root": str(root),
            "lease_dir": str(lease_path),
            "worktrees": [],
            "leases": lease_data["leases"],
            "malformed_leases": lease_data["malformed"],
            "error": (worktree_result.stderr or worktree_result.stdout or "git worktree list failed").strip(),
        }

    worktrees = parse_worktree_porcelain(worktree_result.stdout)
    leases = lease_data["leases"]
    leases_by_path = {
        _norm_path(str(lease.get("worktree_path", ""))): lease
        for lease in leases
        if lease.get("worktree_path")
    }
    leases_by_branch = {
        str(lease.get("branch", "")): lease for lease in leases if lease.get("branch")
    }

    for worktree in worktrees:
        path = str(worktree.get("worktree", ""))
        branch = str(worktree.get("branch_short", ""))
        lease = leases_by_path.get(_norm_path(path)) or leases_by_branch.get(branch)
        if lease:
            worktree["lease"] = lease
            worktree["lane"] = lease.get("lane") or lease.get("name") or ""
        else:
            worktree["lane"] = ""
        if include_dirty and path:
            worktree["dirty"] = _dirty_summary(Path(path), runner)

    leased_paths = {_norm_path(str(item.get("worktree", ""))) for item in worktrees}
    orphan_leases = [
        lease
        for lease in leases
        if lease.get("worktree_path") and _norm_path(str(lease["worktree_path"])) not in leased_paths
    ]
    return {
        "repo_root": str(root),
        "lease_dir": str(lease_path),
        "worktrees": worktrees,
        "leases": leases,
        "orphan_leases": orphan_leases,
        "malformed_leases": lease_data["malformed"],
    }


def render_markdown(status: dict[str, Any], *, limit: int = 12) -> str:
    lines = [
        "# Worker Lane Status",
        "",
        f"- Repo: `{status.get('repo_root', '')}`",
        f"- Lease dir: `{status.get('lease_dir', '')}`",
    ]
    if status.get("error"):
        lines.append(f"- Error: `{status['error']}`")
        return "\n".join(lines) + "\n"

    worktrees = list(status.get("worktrees", []))
    lines.extend(
        [
            f"- Worktrees: {len(worktrees)}",
            f"- Lane leases: {len(status.get('leases', []))}",
            "",
            "| Lane | Branch | Dirty | Path |",
            "| --- | --- | ---: | --- |",
        ]
    )
    for worktree in worktrees[:limit]:
        dirty = worktree.get("dirty", {})
        if dirty.get("error"):
            dirty_label = "error"
        elif dirty:
            dirty_label = "clean" if dirty.get("clean") else str(dirty.get("changed", 0))
        else:
            dirty_label = "unknown"
        lines.append(
            "| {lane} | {branch} | {dirty} | {path} |".format(
                lane=_markdown_table_cell(worktree.get("lane")),
                branch=_markdown_table_cell(worktree.get("branch_short")),
                dirty=dirty_label,
                path=_markdown_table_cell(worktree.get("worktree")),
            )
        )
    if len(worktrees) > limit:
        lines.append(f"| ... | {len(worktrees) - limit} more worktrees |  |  |")
    malformed = status.get("malformed_leases", [])
    if malformed:
        lines.extend(["", f"- Malformed leases: {len(malformed)}"])
    orphans = status.get("orphan_leases", [])
    if orphans:
        lines.extend(["", f"- Orphan leases: {len(orphans)}"])
    return "\n".join(lines) + "\n"


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=Path.cwd())
    parser.add_argument("--lease-dir", type=Path)
    parser.add_argument("--format", choices=["json", "markdown"], default="json")
    parser.add_argument("--no-dirty", action="store_true", help="Skip per-worktree git status checks.")
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    status = collect_status(
        args.repo_root,
        lease_dir=args.lease_dir,
        include_dirty=not args.no_dirty,
    )
    if args.format == "markdown":
        print(render_markdown(status), end="")
    else:
        print(json.dumps(status, indent=2, sort_keys=True))
    return 1 if status.get("error") else 0


if __name__ == "__main__":
    raise SystemExit(main())

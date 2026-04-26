#!/usr/bin/env python3
"""Serve a tiny local live queue monitor for the Senku repo."""

from __future__ import annotations

import argparse
import html
import json
import re
import subprocess
import sys
from dataclasses import dataclass
from datetime import datetime, timezone
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any, Callable, Sequence
from urllib.parse import urlparse

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts import worker_lane_status


DEFAULT_REFRESH_SECONDS = 20
DEFAULT_DISPATCH_LIMIT = 10
DEFAULT_BENCH_LIMIT = 16
DEFAULT_COMMIT_LIMIT = 6
DEFAULT_CP9_SNIPPET_LIMIT = 6
DEFAULT_CP9_LINE_CHARS = 280
SKIP_DISPATCH_FILENAMES = {"README.md", "dispatch_index.generated.md"}
PROTECTED_BENIGN_UNTRACKED = {
    "notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md",
    "notes/PLANNER_HANDOFF_2026-04-25_POST_CLI_TERMINATION.md",
    "notes/PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md",
    "notes/PLANNER_HANDOFF_2026-04-26_POST_CARD5_PAUSE.md",
}


@dataclass(frozen=True)
class CommandResult:
    stdout: str
    stderr: str = ""
    returncode: int = 0
    error: str | None = None


GitRunner = Callable[[Path, Sequence[str]], CommandResult]


def local_timestamp() -> str:
    return datetime.now(timezone.utc).astimezone().isoformat(timespec="seconds")


def repo_relative(path: Path, repo_root: Path) -> str:
    try:
        return path.relative_to(repo_root).as_posix()
    except ValueError:
        return path.as_posix()


def run_git(repo_root: Path, args: Sequence[str]) -> CommandResult:
    try:
        completed = subprocess.run(
            ["git", *args],
            cwd=repo_root,
            check=False,
            capture_output=True,
            text=True,
            timeout=8,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        return CommandResult(stdout="", stderr="", returncode=1, error=exc.__class__.__name__)
    return CommandResult(
        stdout=completed.stdout,
        stderr=completed.stderr,
        returncode=completed.returncode,
    )


def _git_error(result: CommandResult) -> str | None:
    if result.error:
        return result.error
    if result.returncode:
        return (result.stderr or result.stdout or f"git exited {result.returncode}").strip()
    return None


def parse_git_status_short(
    status_text: str,
    *,
    limit: int = 40,
    benign_untracked_paths: set[str] | None = None,
) -> dict[str, Any]:
    counts: dict[str, int] = {}
    actionable_counts: dict[str, int] = {}
    entries: list[dict[str, str]] = []
    benign_untracked: list[dict[str, str]] = []
    total = 0
    actionable_total = 0
    benign_paths = {path.replace("\\", "/") for path in benign_untracked_paths or set()}
    for raw_line in status_text.splitlines():
        if not raw_line.strip():
            continue
        if raw_line.startswith("## "):
            continue
        total += 1
        status = raw_line[:2]
        path = (raw_line[3:].strip() if len(raw_line) > 3 else "").replace("\\", "/")
        counts[status] = counts.get(status, 0) + 1
        entry = {"status": status, "path": path}
        if status == "??" and path in benign_paths:
            benign_untracked.append(entry)
            continue
        actionable_total += 1
        actionable_counts[status] = actionable_counts.get(status, 0) + 1
        if len(entries) < limit:
            entries.append(entry)
    return {
        "clean": actionable_total == 0,
        "raw_clean": total == 0,
        "total_changed": actionable_total,
        "raw_total_changed": total,
        "status_counts": actionable_counts,
        "raw_status_counts": counts,
        "entries": entries,
        "benign_untracked": benign_untracked,
        "benign_untracked_count": len(benign_untracked),
        "truncated": actionable_total > len(entries),
    }


def parse_git_commits(log_text: str) -> list[dict[str, str]]:
    commits: list[dict[str, str]] = []
    for line in log_text.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t", 2)
        if len(parts) == 3:
            short_hash, commit_time, subject = parts
        elif len(parts) == 2:
            short_hash, subject = parts
            commit_time = ""
        else:
            short_hash = parts[0]
            commit_time = ""
            subject = ""
        commits.append(
            {
                "hash": short_hash.strip(),
                "time": commit_time.strip(),
                "subject": subject.strip(),
            }
        )
    return commits


def collect_git_summary(
    repo_root: Path,
    *,
    commit_limit: int = DEFAULT_COMMIT_LIMIT,
    git_runner: GitRunner = run_git,
) -> dict[str, Any]:
    status_result = git_runner(repo_root, ["status", "--short"])
    branch_result = git_runner(repo_root, ["branch", "--show-current"])
    head_result = git_runner(repo_root, ["rev-parse", "--short", "HEAD"])
    head_full_result = git_runner(repo_root, ["rev-parse", "HEAD"])
    log_result = git_runner(
        repo_root,
        ["log", f"-n{commit_limit}", "--date=iso-strict", "--pretty=format:%h%x09%cI%x09%s"],
    )

    status_error = _git_error(status_result)
    status = (
        parse_git_status_short(
            status_result.stdout,
            benign_untracked_paths=PROTECTED_BENIGN_UNTRACKED,
        )
        if not status_error
        else {
            "clean": False,
            "raw_clean": False,
            "total_changed": 0,
            "raw_total_changed": 0,
            "status_counts": {},
            "raw_status_counts": {},
            "entries": [],
            "benign_untracked": [],
            "benign_untracked_count": 0,
            "truncated": False,
            "error": status_error,
        }
    )
    return {
        "branch": branch_result.stdout.strip() if not _git_error(branch_result) else "",
        "head": head_result.stdout.strip() if not _git_error(head_result) else "",
        "head_full": head_full_result.stdout.strip() if not _git_error(head_full_result) else "",
        "status": status,
        "latest_commits": parse_git_commits(log_result.stdout) if not _git_error(log_result) else [],
        "latest_commits_error": _git_error(log_result),
    }


def _first_heading(text: str) -> str:
    for line in text.splitlines():
        stripped = line.strip()
        if stripped.startswith("#"):
            return stripped.lstrip("#").strip()
    return ""


def _read_heading(path: Path) -> str:
    try:
        return _first_heading(path.read_text(encoding="utf-8", errors="replace")[:65536])
    except OSError:
        return ""


def collect_dispatch_pointers(
    repo_root: Path,
    *,
    limit: int = DEFAULT_DISPATCH_LIMIT,
) -> dict[str, Any]:
    dispatch_dir = repo_root / "notes" / "dispatch"
    if not dispatch_dir.is_dir():
        return {"active": [], "tooling": [], "error": f"missing {repo_relative(dispatch_dir, repo_root)}"}

    notes: list[dict[str, Any]] = []
    for path in dispatch_dir.glob("*.md"):
        if path.name in SKIP_DISPATCH_FILENAMES:
            continue
        stat = path.stat()
        title = _read_heading(path) or path.stem.replace("_", " ")
        pointer = {
            "path": repo_relative(path, repo_root),
            "name": path.name,
            "title": title,
            "mtime": datetime.fromtimestamp(stat.st_mtime, timezone.utc).astimezone().isoformat(
                timespec="seconds"
            ),
            "size": stat.st_size,
        }
        notes.append(pointer)

    notes.sort(key=lambda item: (item["mtime"], item["path"]), reverse=True)
    tooling = [
        item
        for item in notes
        if item["name"].upper().startswith("RAG-TOOL") or "tool" in item["title"].lower()
    ]
    return {
        "active": notes[:limit],
        "tooling": tooling[:limit],
        "active_count": len(notes),
        "tooling_count": len(tooling),
    }


def classify_bench_path(path: Path) -> str:
    name = path.name.lower()
    suffix = path.suffix.lower()
    if path.is_dir():
        if name.endswith("_diag") or "diagnostic" in name or "diagnostics" in name:
            return "diagnostic_dir"
        return "directory"
    if suffix == ".json":
        if "diagnostic" in name or "diagnostics" in name:
            return "diagnostic_json"
        return "json"
    if suffix == ".md":
        if "diagnostic" in name or "diagnostics" in name or name == "report.md":
            return "diagnostic_report"
        return "markdown"
    if suffix in {".log", ".out", ".err"}:
        return "log"
    if suffix == ".jsonl":
        return "jsonl"
    return suffix.lstrip(".") or "artifact"


def collect_bench_artifacts(
    repo_root: Path,
    *,
    limit: int = DEFAULT_BENCH_LIMIT,
) -> dict[str, Any]:
    bench_dir = repo_root / "artifacts" / "bench"
    if not bench_dir.is_dir():
        return {"newest": [], "diagnostics": [], "error": f"missing {repo_relative(bench_dir, repo_root)}"}

    pointers: list[dict[str, Any]] = []
    for path in bench_dir.rglob("*"):
        try:
            stat = path.stat()
        except OSError:
            continue
        kind = classify_bench_path(path)
        pointer: dict[str, Any] = {
            "path": repo_relative(path, repo_root),
            "name": path.name,
            "kind": kind,
            "mtime": datetime.fromtimestamp(stat.st_mtime, timezone.utc).astimezone().isoformat(
                timespec="seconds"
            ),
            "is_dir": path.is_dir(),
        }
        if path.is_file():
            pointer["size"] = stat.st_size
        pointers.append(pointer)

    pointers.sort(key=lambda item: (item["mtime"], item["path"]), reverse=True)
    diagnostics = [
        item
        for item in pointers
        if "diagnostic" in item["kind"] or item["name"].lower().endswith("_diag")
    ]
    return {
        "newest": pointers[:limit],
        "diagnostics": diagnostics[:limit],
        "scanned_count": len(pointers),
    }


def _markdown_section(text: str, heading: str) -> list[str]:
    lines = text.splitlines()
    start_index: int | None = None
    heading_marker = f"## {heading}".casefold()
    for index, line in enumerate(lines):
        if line.strip().casefold() == heading_marker:
            start_index = index + 1
            break
    if start_index is None:
        return []

    section: list[str] = []
    for line in lines[start_index:]:
        if line.startswith("## "):
            break
        section.append(line)
    return section


def _compact_markdown_line(line: str) -> str:
    compact = line.strip()
    compact = re.sub(r"^\s*[-*]\s+", "", compact)
    compact = re.sub(r"\*\*([^*]+)\*\*", r"\1", compact)
    compact = compact.replace("`", "")
    return compact.strip()


def _truncate_text(text: str, *, max_chars: int = DEFAULT_CP9_LINE_CHARS) -> str:
    if len(text) <= max_chars:
        return text
    return text[: max(0, max_chars - 3)].rstrip() + "..."


def _compact_snippet(lines: Sequence[str], *, limit: int) -> list[str]:
    snippet: list[str] = []
    for line in lines:
        compact = _compact_markdown_line(line)
        if not compact:
            continue
        snippet.append(_truncate_text(compact))
        if len(snippet) >= limit:
            break
    return snippet


def collect_cp9_summary(
    repo_root: Path,
    *,
    limit: int = DEFAULT_CP9_SNIPPET_LIMIT,
) -> dict[str, Any]:
    cp9_path = repo_root / "notes" / "CP9_ACTIVE_QUEUE.md"
    if not cp9_path.is_file():
        return {
            "path": repo_relative(cp9_path, repo_root),
            "active_snippet": [],
            "rag_landed": [],
            "error": f"missing {repo_relative(cp9_path, repo_root)}",
        }

    try:
        text = cp9_path.read_text(encoding="utf-8", errors="replace")
        stat = cp9_path.stat()
    except OSError as exc:
        return {
            "path": repo_relative(cp9_path, repo_root),
            "active_snippet": [],
            "rag_landed": [],
            "error": exc.__class__.__name__,
        }

    active_lines = _markdown_section(text, "Active")
    landed = [
        _truncate_text(_compact_markdown_line(line))
        for line in active_lines
        if "landed" in line.casefold() and re.search(r"`?RAG-[A-Z0-9-]+`?", line)
    ]
    landed = [line for line in landed if line]
    return {
        "path": repo_relative(cp9_path, repo_root),
        "title": _first_heading(text),
        "mtime": datetime.fromtimestamp(stat.st_mtime, timezone.utc).astimezone().isoformat(
            timespec="seconds"
        ),
        "active_snippet": _compact_snippet(active_lines, limit=limit),
        "rag_landed": landed[:limit],
    }


def collect_worker_lanes(
    repo_root: Path,
    *,
    git_runner: GitRunner = run_git,
) -> dict[str, Any]:
    def runner(command: Sequence[str], cwd: Path) -> CommandResult:
        if command and command[0] == "git":
            return git_runner(cwd, command[1:])
        return CommandResult(stdout="", stderr=f"unsupported command: {' '.join(command)}", returncode=1)

    return worker_lane_status.collect_status(repo_root, runner=runner)


def collect_monitor_data(
    repo_root: Path,
    *,
    commit_limit: int = DEFAULT_COMMIT_LIMIT,
    dispatch_limit: int = DEFAULT_DISPATCH_LIMIT,
    bench_limit: int = DEFAULT_BENCH_LIMIT,
    git_runner: GitRunner = run_git,
    now: Callable[[], str] = local_timestamp,
) -> dict[str, Any]:
    root = repo_root.resolve()
    return {
        "timestamp": now(),
        "repo_root": str(root),
        "git": collect_git_summary(root, commit_limit=commit_limit, git_runner=git_runner),
        "queues": collect_dispatch_pointers(root, limit=dispatch_limit),
        "worker_lanes": collect_worker_lanes(root, git_runner=git_runner),
        "cp9": collect_cp9_summary(root),
        "bench": collect_bench_artifacts(root, limit=bench_limit),
    }


def json_response(data: dict[str, Any]) -> bytes:
    return json.dumps(data, indent=2, sort_keys=True).encode("utf-8")


def render_html_page(*, refresh_seconds: int = DEFAULT_REFRESH_SECONDS) -> str:
    refresh_ms = max(1, refresh_seconds) * 1000
    refresh_label = html.escape(str(max(1, refresh_seconds)))
    return f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Senku Live Queue Monitor</title>
  <style>
    :root {{
      color-scheme: light dark;
      --bg: #f8fafc;
      --fg: #172033;
      --muted: #687084;
      --panel: #ffffff;
      --line: #d8dee8;
      --accent: #0f766e;
      --warn: #9a3412;
      --shadow: 0 1px 2px rgba(15, 23, 42, 0.08);
    }}
    @media (prefers-color-scheme: dark) {{
      :root {{
        --bg: #10141b;
        --fg: #f4f7fb;
        --muted: #aeb7c7;
        --panel: #171d27;
        --line: #2b3545;
        --accent: #5eead4;
        --warn: #fdba74;
        --shadow: none;
      }}
    }}
    * {{ box-sizing: border-box; }}
    body {{
      margin: 0;
      font: 14px/1.45 system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      background: var(--bg);
      color: var(--fg);
    }}
    header {{
      padding: 18px 24px 12px;
      border-bottom: 1px solid var(--line);
      background: var(--panel);
      box-shadow: var(--shadow);
    }}
    h1 {{ margin: 0 0 6px; font-size: 22px; letter-spacing: 0; }}
    h2 {{ margin: 0 0 10px; font-size: 15px; letter-spacing: 0; }}
    main {{
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 14px;
      padding: 16px;
    }}
    section {{
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 8px;
      padding: 14px;
      min-width: 0;
      box-shadow: var(--shadow);
    }}
    .meta {{ color: var(--muted); display: flex; flex-wrap: wrap; gap: 10px 16px; }}
    .status-line {{ font-weight: 650; margin-bottom: 8px; }}
    .clean {{ color: var(--accent); }}
    .dirty {{ color: var(--warn); }}
    ul {{ margin: 0; padding-left: 18px; }}
    li {{ margin: 4px 0; overflow-wrap: anywhere; }}
    code {{
      font-family: ui-monospace, SFMono-Regular, Consolas, "Liberation Mono", monospace;
      font-size: 12px;
      background: color-mix(in srgb, var(--line) 38%, transparent);
      padding: 1px 4px;
      border-radius: 4px;
    }}
    .empty {{ color: var(--muted); }}
    .wide {{ grid-column: 1 / -1; }}
  </style>
</head>
<body>
  <header>
    <h1>Senku Live Queue Monitor</h1>
    <div class="meta">
      <span>Repo: <code id="repo-root">loading</code></span>
      <span>Snapshot: <code id="snapshot-time">loading</code></span>
      <span>Browser refresh: <code id="browser-time">loading</code></span>
      <span>Polling every <code>{refresh_label}s</code></span>
    </div>
  </header>
  <main>
    <section>
      <h2>Git Status</h2>
      <div id="git-status"></div>
      <ul id="git-files"></ul>
    </section>
    <section>
      <h2>Latest Commits</h2>
      <ul id="commits"></ul>
    </section>
    <section>
      <h2>Worker Lanes</h2>
      <ul id="worker-lanes"></ul>
    </section>
    <section>
      <h2>Active Dispatch</h2>
      <ul id="dispatch"></ul>
    </section>
    <section>
      <h2>Tooling Queue</h2>
      <ul id="tooling"></ul>
    </section>
    <section class="wide">
      <h2>CP9 / RAG Queue</h2>
      <ul id="cp9-active"></ul>
      <h2>Recent RAG Landings</h2>
      <ul id="cp9-landed"></ul>
    </section>
    <section class="wide">
      <h2>Newest Bench Diagnostics</h2>
      <ul id="diagnostics"></ul>
    </section>
    <section class="wide">
      <h2>Newest Bench Artifacts</h2>
      <ul id="bench"></ul>
    </section>
  </main>
  <script>
    const REFRESH_MS = {refresh_ms};
    const el = (id) => document.getElementById(id);
    const code = (text) => {{
      const node = document.createElement('code');
      node.textContent = text || '';
      return node;
    }};
    const isRecord = (value) => !!value && typeof value === 'object' && !Array.isArray(value);
    const asArray = (value) => Array.isArray(value) ? value : [];
    const displayText = (value) => value == null ? '' : String(value);
    const clear = (node) => {{ while (node.firstChild) node.removeChild(node.firstChild); }};
    const empty = (node, text) => {{
      clear(node);
      const item = document.createElement('li');
      item.className = 'empty';
      item.textContent = text;
      node.appendChild(item);
    }};
    const appendPointer = (list, item) => {{
      const li = document.createElement('li');
      if (!isRecord(item)) {{
        li.textContent = displayText(item);
        list.appendChild(li);
        return;
      }}
      li.appendChild(code(displayText(item.path || item.hash || '')));
      const text = displayText(item.title || item.subject || item.kind || '');
      li.appendChild(document.createTextNode(text ? ` ${{text}}` : ''));
      const meta = displayText(item.mtime || item.time || '');
      if (meta) li.appendChild(document.createTextNode(` (${{meta}})`));
      list.appendChild(li);
    }};
    const fillList = (id, values, emptyText) => {{
      const list = el(id);
      clear(list);
      const items = asArray(values);
      if (!items.length) {{
        empty(list, emptyText);
        return;
      }}
      items.forEach((item) => appendPointer(list, item));
    }};
    const fillTextList = (id, values, emptyText) => {{
      const list = el(id);
      clear(list);
      const items = asArray(values);
      if (!items.length) {{
        empty(list, emptyText);
        return;
      }}
      items.forEach((text) => {{
        const item = document.createElement('li');
        item.textContent = displayText(text);
        list.appendChild(item);
      }});
    }};
    const formatDirtySummary = (dirty) => {{
      if (!isRecord(dirty) || dirty.error) {{
        return "dirty unknown";
      }}
      if (dirty.clean) {{
        return "clean";
      }}
      const changed = Number(dirty.changed || 0);
      const counts = isRecord(dirty.status_counts) ? dirty.status_counts : {{}};
      const ordered = [
        "modified",
        "added",
        "deleted",
        "renamed",
        "copied",
        "unmerged",
        "untracked",
        "changed",
      ];
      const summaryParts = [];
      for (const status of ordered) {{
        if (counts[status]) {{
          summaryParts.push(`${{counts[status]}} ${{status}}`);
        }}
      }}
      const changedLabel = `${{changed}} changed`;
      if (!summaryParts.length) {{
        return changedLabel;
      }}
      return `${{changedLabel}} (${{summaryParts.join(", ")}})`;
    }};
    const fillWorkerLanes = (values) => {{
      const list = el('worker-lanes');
      clear(list);
      const items = asArray(values);
      if (!items.length) {{
        empty(list, 'No git worktrees found.');
        return;
      }}
      items.forEach((item) => {{
        const li = document.createElement('li');
        if (!isRecord(item)) {{
          li.textContent = displayText(item);
          list.appendChild(li);
          return;
        }}
        const lane = displayText(item.lane || '(unleased)');
        const branch = displayText(item.branch_short || '(detached)');
        const dirty = formatDirtySummary(item.dirty);
        li.appendChild(code(lane));
        li.appendChild(document.createTextNode(` ${{branch}} - ${{dirty}} - ${{displayText(item.worktree)}}`));
        list.appendChild(li);
      }});
    }};
    function render(data) {{
      data = isRecord(data) ? data : {{}};
      el('repo-root').textContent = data.repo_root || '';
      el('snapshot-time').textContent = data.timestamp || '';
      el('browser-time').textContent = new Date().toLocaleTimeString();

      const statusBox = el('git-status');
      clear(statusBox);
      const status = data.git && data.git.status ? data.git.status : {{}};
      const line = document.createElement('div');
      line.className = `status-line ${{status.clean ? 'clean' : 'dirty'}}`;
      const branch = data.git && data.git.branch ? data.git.branch : '(detached or unknown)';
      const head = data.git && data.git.head ? data.git.head : '';
      line.textContent = status.error
        ? `Git status unavailable: ${{status.error}}`
        : `${{status.clean ? 'Clean' : `${{status.total_changed || 0}} changed`}} on ${{branch}} ${{head}}`;
      statusBox.appendChild(line);
      const benign = asArray(status.benign_untracked);
      if (benign.length) {{
        const detail = document.createElement('div');
        detail.className = 'meta';
        detail.textContent = `Benign untracked: ${{benign.map((item) => item.path).join(', ')}}`;
        statusBox.appendChild(detail);
      }}
      fillList('git-files', status.entries || [], 'No worktree changes.');
      fillList('commits', data.git ? data.git.latest_commits : [], 'No commits found.');
      fillWorkerLanes(data.worker_lanes ? data.worker_lanes.worktrees : []);
      fillList('dispatch', data.queues ? data.queues.active : [], 'No active dispatch notes found.');
      fillList('tooling', data.queues ? data.queues.tooling : [], 'No tooling notes found.');
      fillTextList('cp9-active', data.cp9 ? data.cp9.active_snippet : [], 'No CP9 active queue snippet found.');
      fillTextList('cp9-landed', data.cp9 ? data.cp9.rag_landed : [], 'No recent RAG landings found.');
      fillList('diagnostics', data.bench ? data.bench.diagnostics : [], 'No bench diagnostics found.');
      fillList('bench', data.bench ? data.bench.newest : [], 'No bench artifacts found.');
    }}
    async function refresh() {{
      try {{
        const response = await fetch('/status.json?ts=' + Date.now(), {{ cache: 'no-store' }});
        if (!response.ok) throw new Error(response.status + ' ' + response.statusText);
        render(await response.json());
      }} catch (error) {{
        el('snapshot-time').textContent = 'refresh failed: ' + error.message;
      }}
    }}
    refresh();
    setInterval(refresh, REFRESH_MS);
  </script>
</body>
</html>
"""


def make_handler(repo_root: Path, refresh_seconds: int) -> type[BaseHTTPRequestHandler]:
    class LiveQueueMonitorHandler(BaseHTTPRequestHandler):
        def do_GET(self) -> None:
            route = urlparse(self.path).path
            if route in {"/", "/index.html"}:
                self._send(
                    render_html_page(refresh_seconds=refresh_seconds).encode("utf-8"),
                    "text/html; charset=utf-8",
                )
                return
            if route in {"/status", "/status.json"}:
                self._send(
                    json_response(collect_monitor_data(repo_root)),
                    "application/json; charset=utf-8",
                    cache=False,
                )
                return
            self.send_error(HTTPStatus.NOT_FOUND, "Not found")

        def log_message(self, format: str, *args: Any) -> None:
            return

        def _send(self, body: bytes, content_type: str, *, cache: bool = True) -> None:
            self.send_response(HTTPStatus.OK)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(body)))
            if not cache:
                self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(body)

    return LiveQueueMonitorHandler


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="127.0.0.1", help="Host interface to bind.")
    parser.add_argument("--port", type=int, default=8765, help="Port to bind.")
    parser.add_argument(
        "--repo-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Repository root to monitor.",
    )
    parser.add_argument(
        "--refresh-seconds",
        type=int,
        default=DEFAULT_REFRESH_SECONDS,
        help="Browser JSON polling interval.",
    )
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    repo_root = args.repo_root.resolve()
    refresh_seconds = max(1, args.refresh_seconds)
    server = ThreadingHTTPServer(
        (args.host, args.port),
        make_handler(repo_root, refresh_seconds),
    )
    print(
        f"Serving Senku live queue monitor for {repo_root} at "
        f"http://{args.host}:{args.port}/"
    )
    print("Press Ctrl+C to stop.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print()
    finally:
        server.server_close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

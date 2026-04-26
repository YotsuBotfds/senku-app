"""Build a compact index of files under artifacts/bench."""

from __future__ import annotations

import argparse
import json
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path


DEFAULT_MAX_JSON_BYTES = 256 * 1024
LOG_SUFFIXES = {".log", ".out", ".err"}
JSONL_TYPE_VALUE_MAX_LEN = 80
BINARY_SUFFIXES = {
    ".apk",
    ".bin",
    ".db",
    ".gz",
    ".jar",
    ".jpg",
    ".jpeg",
    ".png",
    ".sqlite",
    ".webp",
    ".zip",
}


def iter_bench_artifacts(bench_dir, max_json_bytes=DEFAULT_MAX_JSON_BYTES):
    """Yield compact manifest records for files below bench_dir."""
    root = Path(bench_dir)
    for path in sorted(p for p in root.rglob("*") if p.is_file()):
        rel_path = path.relative_to(root).as_posix()
        try:
            stat = _stat_path(path)
        except OSError as exc:
            yield {
                "path": rel_path,
                "size": 0,
                "mtime": "",
                "kind": infer_artifact_kind(path),
                "summary": {
                    "skipped": "stat_unreadable",
                    "error": exc.__class__.__name__,
                },
            }
            continue
        record = {
            "path": rel_path,
            "size": stat.st_size,
            "mtime": datetime.fromtimestamp(stat.st_mtime, timezone.utc).isoformat(),
            "kind": infer_artifact_kind(path),
        }
        hints = infer_run_hints(path, root)
        if hints:
            record["hints"] = hints
        summary = summarize_artifact(path, stat.st_size, max_json_bytes)
        if summary:
            record["summary"] = summary
        yield record


def _stat_path(path):
    return path.stat()


def infer_artifact_kind(path):
    """Infer a coarse artifact kind from suffixes and common bench naming."""
    name = path.name.lower()
    suffix = path.suffix.lower()
    if suffix in LOG_SUFFIXES or name.endswith("_stdout.log") or name.endswith("_stderr.log"):
        return "log"
    if suffix in BINARY_SUFFIXES:
        return "binary"
    if suffix == ".json":
        if "diagnostic" in name or "diagnostics" in name:
            return "diagnostic_json"
        if "summary" in name:
            return "summary_json"
        return "json"
    if suffix == ".jsonl":
        return "jsonl"
    if suffix == ".md":
        return "markdown_report"
    if suffix == ".csv":
        return "csv"
    if suffix == ".xml":
        return "xml"
    if suffix == ".txt":
        return "text"
    return suffix.lstrip(".") or "file"


def infer_run_hints(path, bench_dir):
    rel = path.relative_to(bench_dir)
    stem = path.stem
    hints = {}
    if len(rel.parts) > 1:
        hints["group"] = rel.parts[0]

    for suffix in (
        "_prompt_replay",
        "_prompts",
        "_stdout",
        "_stderr",
        "_diagnostics",
        "_diagnostic",
        "_summary",
    ):
        if stem.endswith(suffix):
            stem = stem[: -len(suffix)]
            break
    if stem:
        hints["run"] = stem
    return hints


def summarize_artifact(path, size, max_json_bytes):
    suffix = path.suffix.lower()
    if suffix == ".json":
        if size > max_json_bytes:
            return {"skipped": "json_too_large"}
        return summarize_json_file(path)
    if suffix == ".md":
        if size > max_json_bytes:
            return {"skipped": "markdown_too_large"}
        return summarize_markdown_file(path)
    if suffix == ".jsonl":
        if size > max_json_bytes:
            return {"skipped": "jsonl_too_large"}
        return summarize_jsonl_file(path)
    if suffix in LOG_SUFFIXES:
        return {"skipped": "log_not_read"}
    if suffix in BINARY_SUFFIXES:
        return {"skipped": "binary_not_read"}
    return {}


def summarize_json_file(path):
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
        return {"skipped": "json_unreadable", "error": exc.__class__.__name__}

    if isinstance(data, dict):
        summary = {}
        for key in ("timestamp", "section_filter"):
            if key in data and _is_scalar(data[key]):
                summary[key] = data[key]
        if isinstance(data.get("config"), dict):
            config = data["config"]
            summary["config"] = _select_scalar_keys(
                config,
                ("model", "runtime_profile", "mode", "rag", "top_k", "temperature"),
            )
        if isinstance(data.get("summary"), dict):
            summary["counts"] = _select_numeric_keys(data["summary"])
        for list_key in ("results", "diagnostics", "failures", "rows"):
            if isinstance(data.get(list_key), list):
                summary[f"{list_key}_count"] = len(data[list_key])
        return summary or {"json_type": "object", "top_level_keys": sorted(data)[:12]}

    if isinstance(data, list):
        return {"json_type": "array", "items_count": len(data)}
    return {"json_type": type(data).__name__}


def summarize_jsonl_file(path):
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except (OSError, UnicodeDecodeError) as exc:
        return {"skipped": "jsonl_unreadable", "error": exc.__class__.__name__}

    summary = {
        "line_count": len(lines),
        "object_count": 0,
        "non_object_lines": 0,
        "malformed_lines": 0,
    }
    record_type_counts = Counter()
    report_type_counts = Counter()

    for line in lines:
        if not line.strip():
            summary["malformed_lines"] += 1
            continue
        try:
            data = json.loads(line)
        except json.JSONDecodeError:
            summary["malformed_lines"] += 1
            continue
        if not isinstance(data, dict):
            summary["non_object_lines"] += 1
            continue

        summary["object_count"] += 1
        _count_jsonl_type_value(record_type_counts, data.get("record_type"))
        _count_jsonl_type_value(report_type_counts, data.get("report_type"))

    if record_type_counts:
        summary["record_type_counts"] = dict(record_type_counts.most_common(8))
    if report_type_counts:
        summary["report_type_counts"] = dict(report_type_counts.most_common(8))
    if summary["non_object_lines"] == 0:
        del summary["non_object_lines"]
    return summary


def summarize_markdown_file(path):
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except (OSError, UnicodeDecodeError) as exc:
        return {"skipped": "markdown_unreadable", "error": exc.__class__.__name__}

    summary = {
        "line_count": len(lines),
        "heading_count": 0,
    }
    for line in lines:
        stripped = line.lstrip()
        if not stripped.startswith("#"):
            continue
        marker, _, title = stripped.partition(" ")
        if 1 <= len(marker) <= 6 and set(marker) == {"#"}:
            summary["heading_count"] += 1
            if "title" not in summary and title.strip():
                summary["title"] = title.strip()
    return summary


def _is_scalar(value):
    return isinstance(value, (str, int, float, bool)) or value is None


def _select_scalar_keys(data, keys):
    selected = {}
    for key in keys:
        value = data.get(key)
        if _is_scalar(value):
            selected[key] = value
    return selected


def _select_numeric_keys(data):
    counts = {}
    for key, value in data.items():
        if isinstance(value, bool):
            continue
        if isinstance(value, (int, float)):
            counts[key] = value
    return counts


def _count_jsonl_type_value(counter, value):
    normalized = _normalize_jsonl_type_value(value)
    if normalized is None:
        return
    counter[normalized] += 1


def _normalize_jsonl_type_value(value):
    if not isinstance(value, str) or not value:
        return None
    value = " ".join(value.split())
    if not value:
        return None
    if len(value) <= JSONL_TYPE_VALUE_MAX_LEN:
        return value
    if JSONL_TYPE_VALUE_MAX_LEN <= 3:
        return value[:JSONL_TYPE_VALUE_MAX_LEN]
    return f"{value[:JSONL_TYPE_VALUE_MAX_LEN - 3]}..."


def write_jsonl(records, output_path):
    path = Path(output_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        "\n".join(json.dumps(record, sort_keys=True) for record in records) + "\n",
        encoding="utf-8",
    )


def write_markdown(records, output_path):
    path = Path(output_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        "# Bench Artifact Index",
        "",
        "| Path | Kind | Size | Mtime | Run | Summary |",
        "| --- | --- | ---: | --- | --- | --- |",
    ]
    for record in records:
        summary = _format_summary(record.get("summary", {}))
        hints = record.get("hints", {})
        lines.append(
            "| {path} | {kind} | {size} | {mtime} | {run} | {summary} |".format(
                path=_escape_md(record["path"]),
                kind=_escape_md(record["kind"]),
                size=record["size"],
                mtime=_escape_md(record["mtime"]),
                run=_escape_md(hints.get("run", "")),
                summary=_escape_md(summary),
            )
        )
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def _format_summary(summary):
    if not summary:
        return ""
    parts = []
    for key in ("results_count", "diagnostics_count", "failures_count", "rows_count"):
        if key in summary:
            parts.append(f"{key}={summary[key]}")
    counts = summary.get("counts")
    if isinstance(counts, dict):
        parts.extend(f"{key}={value}" for key, value in sorted(counts.items())[:8])
    if "title" in summary:
        parts.append(f"title={summary['title']}")
    for key in (
        "line_count",
        "object_count",
        "non_object_lines",
        "malformed_lines",
        "heading_count",
    ):
        if key in summary:
            parts.append(f"{key}={summary[key]}")
    for count_key in ("record_type_counts", "report_type_counts"):
        counts = summary.get(count_key)
        if isinstance(counts, dict):
            formatted_counts = ", ".join(
                f"{key}:{value}" for key, value in counts.items()
            )
            if formatted_counts:
                parts.append(f"{count_key}={formatted_counts}")
    if "skipped" in summary:
        parts.append(f"skipped={summary['skipped']}")
    if "error" in summary:
        parts.append(f"error={summary['error']}")
    if not parts and "json_type" in summary:
        parts.append(f"json_type={summary['json_type']}")
    return "; ".join(parts)


def _escape_md(value):
    return str(value).replace("\r\n", "\n").replace("\r", " ").replace("|", "\\|").replace("\n", " ")


def parse_args(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bench-dir", default="artifacts/bench")
    parser.add_argument("--output-jsonl", required=True)
    parser.add_argument("--output-md", required=True)
    parser.add_argument("--max-json-bytes", type=int, default=DEFAULT_MAX_JSON_BYTES)
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(argv)
    records = list(iter_bench_artifacts(args.bench_dir, args.max_json_bytes))
    write_jsonl(records, args.output_jsonl)
    write_markdown(records, args.output_md)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

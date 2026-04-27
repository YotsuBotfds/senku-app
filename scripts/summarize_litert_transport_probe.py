#!/usr/bin/env python3
"""Summarize LiteRT model transport probe results without touching a device."""

from __future__ import annotations

import argparse
import json
from collections import Counter
from pathlib import Path
from typing import Any


DIRECT_STREAM_METHODS = {
    "cmd_redirect_cat",
    "cmd_type_pipe_cat",
    "process_stdin_copy_cat",
}

BYTE_SENSITIVE_PAYLOAD_CLASSES = {
    "binary_random_64mb",
    "real_model_litert",
}

BLOCKER_5554_DATA_PARTITION = "5554_data_partition_blocker"
BLOCKER_DIRECT_STREAM_NOT_BYTE_SAFE = "direct_stream_not_byte_safe"


def summarize_probe(results_path: str | Path) -> dict[str, Any]:
    """Return a compact blocker-oriented summary for a probe results.json."""
    path = Path(results_path)
    artifact = _read_json(path)
    rows = _result_rows(artifact)
    counts = Counter(_text(row.get("verdict")).lower() or "unresolved" for row in rows)
    methods = sorted({_text(row.get("method")) for row in rows if _text(row.get("method"))})
    payload_classes = sorted(
        {_text(row.get("payload_class")) for row in rows if _text(row.get("payload_class"))}
    )
    labels = _detect_blocker_labels(rows)
    summary = artifact.get("summary", {}) if isinstance(artifact, dict) else {}

    return {
        "source": path.as_posix(),
        "evidence_kind": "preflight_non_acceptance",
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "safe_count": int(counts.get("safe", 0)),
        "unsafe_count": int(counts.get("unsafe", 0)),
        "unresolved_count": int(counts.get("unresolved", 0)),
        "methods": methods,
        "payload_classes": payload_classes,
        "adb_platform_tools_version": _text(summary.get("adb_platform_tools_version")),
        "adb_version_line": _text(summary.get("adb_version_line")),
        "blocker_labels": labels,
        "blockers": _blocker_details(rows, labels),
    }


def render_markdown(summary: dict[str, Any]) -> str:
    """Render a compact Markdown summary for handoff notes."""
    lines = [
        "# LiteRT Transport Probe Blocker Summary",
        "",
        "**Evidence posture:** non-acceptance preflight evidence from existing `results.json` artifact.",
        "",
        f"- Safe: {_int(summary.get('safe_count'))}",
        f"- Unsafe: {_int(summary.get('unsafe_count'))}",
        f"- Unresolved: {_int(summary.get('unresolved_count'))}",
        f"- Methods: {_join(summary.get('methods'))}",
        f"- Payload classes: {_join(summary.get('payload_classes'))}",
        f"- ADB platform-tools version: {_inline(summary.get('adb_platform_tools_version')) or '(not present)'}",
        f"- ADB version line: {_inline(summary.get('adb_version_line')) or '(not present)'}",
        f"- Blocker labels: {_join(summary.get('blocker_labels'))}",
    ]

    blockers = summary.get("blockers")
    if isinstance(blockers, list) and blockers:
        lines.extend(["", "## Blockers", ""])
        for blocker in blockers:
            if not isinstance(blocker, dict):
                continue
            lines.append(f"### {_inline(blocker.get('label'))}")
            lines.append("")
            for example in blocker.get("examples", []):
                if not isinstance(example, dict):
                    continue
                lines.append(
                    "- "
                    f"method={_inline(example.get('method'))} "
                    f"serial={_inline(example.get('serial'))} "
                    f"payload_class={_inline(example.get('payload_class'))} "
                    f"verdict={_inline(example.get('verdict'))} "
                    f"notes={_inline(example.get('notes'))}"
                )
            lines.append("")

    return "\n".join(lines).rstrip() + "\n"


def _read_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8-sig") as handle:
        return json.load(handle)


def _result_rows(artifact: Any) -> list[dict[str, Any]]:
    if isinstance(artifact, dict):
        rows = artifact.get("results", [])
    elif isinstance(artifact, list):
        rows = artifact
    else:
        rows = []
    return [row for row in rows if isinstance(row, dict)]


def _detect_blocker_labels(rows: list[dict[str, Any]]) -> list[str]:
    labels = []
    if any(_is_5554_data_partition_blocker(row) for row in rows):
        labels.append(BLOCKER_5554_DATA_PARTITION)
    if any(_is_direct_stream_not_byte_safe(row) for row in rows):
        labels.append(BLOCKER_DIRECT_STREAM_NOT_BYTE_SAFE)
    return labels


def _is_5554_data_partition_blocker(row: dict[str, Any]) -> bool:
    serial = _text(row.get("serial")).lower()
    verdict = _text(row.get("verdict")).lower()
    notes = _combined_row_text(row)
    return "5554" in serial and verdict in {"unsafe", "unresolved"} and (
        "no space left on device" in notes
        or "storage-confounded" in notes
        or "data partition" in notes
    )


def _is_direct_stream_not_byte_safe(row: dict[str, Any]) -> bool:
    method = _text(row.get("method"))
    verdict = _text(row.get("verdict")).lower()
    payload_class = _text(row.get("payload_class"))
    if method not in DIRECT_STREAM_METHODS or verdict != "unsafe":
        return False
    if payload_class not in BYTE_SENSITIVE_PAYLOAD_CLASSES:
        return False
    expected = row.get("expected_bytes")
    remote = row.get("remote_bytes")
    if remote is None:
        return False
    if row.get("hash_match") is False:
        return True
    return expected is not None and remote is not None and expected != remote


def _blocker_details(rows: list[dict[str, Any]], labels: list[str]) -> list[dict[str, Any]]:
    detectors = {
        BLOCKER_5554_DATA_PARTITION: _is_5554_data_partition_blocker,
        BLOCKER_DIRECT_STREAM_NOT_BYTE_SAFE: _is_direct_stream_not_byte_safe,
    }
    details = []
    for label in labels:
        examples = [_example(row) for row in rows if detectors[label](row)]
        details.append({"label": label, "examples": examples[:5]})
    return details


def _example(row: dict[str, Any]) -> dict[str, Any]:
    return {
        "method": _text(row.get("method")),
        "serial": _text(row.get("serial")),
        "payload_class": _text(row.get("payload_class")),
        "verdict": _text(row.get("verdict")),
        "remote_bytes": row.get("remote_bytes"),
        "expected_bytes": row.get("expected_bytes"),
        "hash_match": row.get("hash_match"),
        "notes": _text(row.get("notes")),
    }


def _combined_row_text(row: dict[str, Any]) -> str:
    parts = [_text(row.get("notes")), _text(row.get("remote_relative_path")), _text(row.get("remote_tmp_path"))]
    for step in row.get("transport_steps", []):
        if isinstance(step, dict):
            parts.extend([_text(step.get("stdout")), _text(step.get("stderr")), _text(step.get("command_text"))])
    return "\n".join(parts).lower()


def _join(value: Any) -> str:
    if not isinstance(value, list) or not value:
        return "(none)"
    return ", ".join(_inline(item) for item in value)


def _inline(value: Any) -> str:
    return _text(value).replace("\r", " ").replace("\n", " ").replace("|", "/").strip()


def _text(value: Any) -> str:
    return "" if value is None else str(value)


def _int(value: Any) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return 0


def _positive_int(value: str) -> int:
    parsed = int(value)
    if parsed <= 0:
        raise argparse.ArgumentTypeError("--limit must be greater than 0")
    return parsed


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("results_json", help="Path to probe_litert_model_transport.ps1 results.json")
    parser.add_argument("--json", action="store_true", help="Emit compact JSON to stdout")
    parser.add_argument("--markdown", action="store_true", help="Emit Markdown to stdout")
    parser.add_argument("--json-out", "--output-json", dest="json_out", help="Write compact JSON summary to this path")
    parser.add_argument(
        "--markdown-out",
        "--output-md",
        dest="markdown_out",
        help="Write Markdown summary to this path",
    )
    parser.add_argument(
        "--fail-on-blockers",
        action="store_true",
        help="Return exit code 1 when blocker labels are supported by the artifact",
    )
    args = parser.parse_args(argv)

    summary = summarize_probe(args.results_json)
    json_text = json.dumps(summary, indent=2, sort_keys=True) + "\n"
    markdown_text = render_markdown(summary)

    if args.json_out:
        Path(args.json_out).write_text(json_text, encoding="utf-8")
    if args.markdown_out:
        Path(args.markdown_out).write_text(markdown_text, encoding="utf-8")

    if args.json or not args.markdown:
        print(json_text, end="")
    if args.markdown:
        print(markdown_text, end="")

    if args.fail_on_blockers and summary["blocker_labels"]:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

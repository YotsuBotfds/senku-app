#!/usr/bin/env python3
"""Build a compact lineage report for related RAG diagnostic runs."""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable, Mapping, Sequence


DEFAULT_LIMIT = 40
DIAGNOSTICS_JSON = "diagnostics.json"


@dataclass(frozen=True)
class DiagnosticRun:
    label: str
    path: Path
    generated_at: str
    mtime: float
    summary: Mapping[str, Any]
    rows: list[Mapping[str, Any]]


def load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def _as_int(value: Any) -> int:
    if isinstance(value, bool):
        return int(value)
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        return int(value)
    if isinstance(value, str):
        match = re.match(r"\s*(\d+)", value)
        if match:
            return int(match.group(1))
    return 0


def _mtime_iso(path: Path) -> str:
    return datetime.fromtimestamp(path.stat().st_mtime, timezone.utc).astimezone().isoformat(
        timespec="seconds"
    )


def _label_from_dir(path: Path, stem: str | None) -> str:
    name = path.name
    if name.endswith("_diag"):
        name = name[:-5]
    if stem and name.startswith(stem):
        name = name[len(stem) :].lstrip("_-")
    return name or path.name


def _diagnostic_total_rows(path: Path) -> int:
    try:
        data = load_json(path / DIAGNOSTICS_JSON)
    except (OSError, json.JSONDecodeError):
        return 0
    summary = data.get("summary") if isinstance(data, Mapping) else {}
    if isinstance(summary, Mapping):
        total = _as_int(summary.get("total_rows"))
        if total:
            return total
    rows = data.get("rows") if isinstance(data, Mapping) else []
    return len(rows) if isinstance(rows, list) else 0


def discover_diagnostic_dirs(
    bench_dir: Path,
    stem: str,
    *,
    limit: int = DEFAULT_LIMIT,
    include_partial: bool = False,
) -> list[Path]:
    candidates = [
        path
        for path in bench_dir.glob(f"{stem}*_diag")
        if path.is_dir() and (path / DIAGNOSTICS_JSON).is_file()
    ]
    if candidates and not include_partial:
        row_counts = {path: _diagnostic_total_rows(path) for path in candidates}
        max_rows = max(row_counts.values(), default=0)
        if max_rows:
            candidates = [path for path in candidates if row_counts.get(path) == max_rows]
    candidates.sort(key=lambda path: path.stat().st_mtime)
    if limit > 0:
        return candidates[-limit:]
    return candidates


def load_diagnostic_run(path: Path, *, stem: str | None = None) -> DiagnosticRun:
    data = load_json(path / DIAGNOSTICS_JSON)
    rows = data.get("rows")
    if not isinstance(rows, list):
        rows = []
    summary = data.get("summary")
    if not isinstance(summary, Mapping):
        summary = {}
    return DiagnosticRun(
        label=_label_from_dir(path, stem),
        path=path,
        generated_at=str(data.get("generated_at") or _mtime_iso(path / DIAGNOSTICS_JSON)),
        mtime=(path / DIAGNOSTICS_JSON).stat().st_mtime,
        summary=summary,
        rows=[row for row in rows if isinstance(row, Mapping)],
    )


def _count_from_summary(run: DiagnosticRun, bucket: str) -> int:
    by_bucket = run.summary.get("by_bucket")
    if isinstance(by_bucket, Mapping):
        return _as_int(by_bucket.get(bucket))
    return 0


def _ratio_text(value: Any) -> str:
    return str(value or "")


def summarize_run(run: DiagnosticRun) -> dict[str, Any]:
    return {
        "label": run.label,
        "path": run.path.as_posix(),
        "generated_at": run.generated_at,
        "total_rows": _as_int(run.summary.get("total_rows")) or len(run.rows),
        "expected_supported": _count_from_summary(run, "expected_supported"),
        "generation_miss": _count_from_summary(run, "generation_miss"),
        "ranking_miss": _count_from_summary(run, "ranking_miss"),
        "retrieval_miss": _count_from_summary(run, "retrieval_miss"),
        "artifact_error": _count_from_summary(run, "artifact_error"),
        "abstain_or_clarify_needed": _count_from_summary(run, "abstain_or_clarify_needed"),
        "expected_owner_best_rank": str(run.summary.get("expected_owner_best_rank") or ""),
        "expected_hit_at_1": _ratio_text(run.summary.get("expected_hit_at_1")),
        "expected_hit_at_3": _ratio_text(run.summary.get("expected_hit_at_3")),
        "expected_hit_at_k": _ratio_text(run.summary.get("expected_hit_at_k")),
        "expected_cited": _ratio_text(run.summary.get("expected_cited")),
        "top1_bridge_rows": _as_int(run.summary.get("top1_bridge_rows")),
        "top1_unresolved_partial_rows": _as_int(run.summary.get("top1_unresolved_partial_rows")),
    }


def _row_key(row: Mapping[str, Any]) -> str:
    return str(row.get("prompt_id") or row.get("prompt_index") or "").strip()


def _row_state(row: Mapping[str, Any]) -> dict[str, Any]:
    return {
        "bucket": str(row.get("suspected_failure_bucket") or "").strip(),
        "expected_guide_ids": str(row.get("expected_guide_ids") or "").strip(),
        "top_retrieved_guide_ids": str(row.get("top_retrieved_guide_ids") or "").strip(),
        "cited_guide_ids": str(row.get("cited_guide_ids") or "").strip(),
        "expected_owner_best_rank": row.get("expected_owner_best_rank"),
        "expected_cited": str(row.get("expected_cited") or "").strip(),
        "app_acceptance_status": str(row.get("app_acceptance_status") or "").strip(),
        "short_reason": str(row.get("short_reason") or "").strip(),
    }


def build_row_index(run: DiagnosticRun) -> dict[str, dict[str, Any]]:
    indexed: dict[str, dict[str, Any]] = {}
    for row in run.rows:
        key = _row_key(row)
        if key:
            indexed[key] = _row_state(row)
    return indexed


def transition_kind(before: Mapping[str, Any], after: Mapping[str, Any]) -> str:
    before_bucket = str(before.get("bucket") or "")
    after_bucket = str(after.get("bucket") or "")
    if before_bucket == after_bucket:
        if before.get("cited_guide_ids") != after.get("cited_guide_ids"):
            return "citation_change"
        if before.get("top_retrieved_guide_ids") != after.get("top_retrieved_guide_ids"):
            return "retrieval_change"
        return "unchanged"
    if after_bucket == "expected_supported":
        return "improved"
    if before_bucket == "expected_supported":
        return "regressed"
    return "changed"


def build_transitions(runs: Sequence[DiagnosticRun]) -> list[dict[str, Any]]:
    transitions: list[dict[str, Any]] = []
    indexes = [build_row_index(run) for run in runs]
    for previous, current, prev_index, curr_index in zip(runs, runs[1:], indexes, indexes[1:]):
        for key in sorted(set(prev_index) | set(curr_index)):
            before = prev_index.get(key)
            after = curr_index.get(key)
            if before is None or after is None:
                transitions.append(
                    {
                        "from": previous.label,
                        "to": current.label,
                        "prompt_id": key,
                        "kind": "added" if before is None else "removed",
                        "before": before,
                        "after": after,
                    }
                )
                continue
            kind = transition_kind(before, after)
            if kind != "unchanged":
                transitions.append(
                    {
                        "from": previous.label,
                        "to": current.label,
                        "prompt_id": key,
                        "kind": kind,
                        "before": before,
                        "after": after,
                    }
                )
    return transitions


def build_lineage(runs: Sequence[DiagnosticRun]) -> dict[str, Any]:
    return {
        "generated_at": datetime.now(timezone.utc).astimezone().isoformat(timespec="seconds"),
        "run_count": len(runs),
        "runs": [summarize_run(run) for run in runs],
        "transitions": build_transitions(runs),
    }


def _markdown_cell(value: Any) -> str:
    return str(value).replace("\\", "\\\\").replace("|", "\\|").replace("\n", "<br>")


def _table_row(values: Iterable[Any]) -> str:
    return "| " + " | ".join(_markdown_cell(value) for value in values) + " |"


def render_markdown(lineage: Mapping[str, Any]) -> str:
    lines = [
        "# RAG Experiment Lineage",
        "",
        f"Generated: `{lineage.get('generated_at', '')}`",
        f"Runs: `{lineage.get('run_count', 0)}`",
        "",
        "## Runs",
        "",
        _table_row(
            [
                "label",
                "expected_supported",
                "generation_miss",
                "ranking_miss",
                "retrieval_miss",
                "artifact_error",
                "expected_cited",
                "path",
            ]
        ),
        _table_row(["---", "---:", "---:", "---:", "---:", "---:", "---", "---"]),
    ]
    for run in lineage.get("runs", []):
        if not isinstance(run, Mapping):
            continue
        lines.append(
            _table_row(
                [
                    f"`{run.get('label', '')}`",
                    run.get("expected_supported", 0),
                    run.get("generation_miss", 0),
                    run.get("ranking_miss", 0),
                    run.get("retrieval_miss", 0),
                    run.get("artifact_error", 0),
                    run.get("expected_cited", ""),
                    f"`{run.get('path', '')}`",
                ]
            )
        )

    lines.extend(["", "## Row Transitions", ""])
    transitions = [item for item in lineage.get("transitions", []) if isinstance(item, Mapping)]
    if not transitions:
        lines.append("No prompt-level transitions found.")
    else:
        lines.extend(
            [
                _table_row(["from", "to", "prompt", "kind", "bucket", "cited"]),
                _table_row(["---", "---", "---", "---", "---", "---"]),
            ]
        )
        for item in transitions:
            before = item.get("before") if isinstance(item.get("before"), Mapping) else {}
            after = item.get("after") if isinstance(item.get("after"), Mapping) else {}
            before_bucket = before.get("bucket", "") if isinstance(before, Mapping) else ""
            after_bucket = after.get("bucket", "") if isinstance(after, Mapping) else ""
            before_cited = before.get("cited_guide_ids", "") if isinstance(before, Mapping) else ""
            after_cited = after.get("cited_guide_ids", "") if isinstance(after, Mapping) else ""
            lines.append(
                _table_row(
                    [
                        f"`{item.get('from', '')}`",
                        f"`{item.get('to', '')}`",
                        f"`{item.get('prompt_id', '')}`",
                        item.get("kind", ""),
                        f"{before_bucket} -> {after_bucket}",
                        f"{before_cited} -> {after_cited}",
                    ]
                )
            )
    lines.append("")
    return "\n".join(lines)


def write_outputs(lineage: Mapping[str, Any], output_json: Path, output_md: Path) -> None:
    output_json.parent.mkdir(parents=True, exist_ok=True)
    output_json.write_text(json.dumps(lineage, indent=2, sort_keys=True), encoding="utf-8")
    output_md.parent.mkdir(parents=True, exist_ok=True)
    output_md.write_text(render_markdown(lineage), encoding="utf-8")


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bench-dir", type=Path, default=Path("artifacts/bench"))
    parser.add_argument("--stem", help="Diagnostic directory stem to discover.")
    parser.add_argument("--diag-dir", type=Path, action="append", default=[], help="Explicit diagnostic dir.")
    parser.add_argument("--limit", type=int, default=DEFAULT_LIMIT)
    parser.add_argument(
        "--include-partial",
        action="store_true",
        help="Include focused diagnostic dirs with fewer rows than the largest discovered run.",
    )
    parser.add_argument("--output-json", type=Path)
    parser.add_argument("--output-md", type=Path)
    parser.add_argument("--json", action="store_true", help="Print JSON to stdout instead of markdown.")
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    dirs: list[Path] = []
    if args.diag_dir:
        dirs.extend(args.diag_dir)
    elif args.stem:
        dirs.extend(
            discover_diagnostic_dirs(
                args.bench_dir,
                args.stem,
                limit=args.limit,
                include_partial=args.include_partial,
            )
        )
    else:
        raise SystemExit("Provide --stem or at least one --diag-dir")

    runs = [load_diagnostic_run(path, stem=args.stem) for path in dirs]
    runs.sort(key=lambda run: (run.mtime, run.path.as_posix()))
    lineage = build_lineage(runs)

    if args.output_json or args.output_md:
        default_base = args.bench_dir / f"{args.stem or 'rag_experiment'}_lineage"
        write_outputs(
            lineage,
            args.output_json or default_base.with_suffix(".json"),
            args.output_md or default_base.with_suffix(".md"),
        )

    if args.json:
        print(json.dumps(lineage, indent=2, sort_keys=True))
    else:
        print(render_markdown(lineage))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

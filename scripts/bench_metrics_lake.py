#!/usr/bin/env python3
"""Build a local metrics lake from bench JSON, JSONL, and Markdown artifacts."""

from __future__ import annotations

import argparse
import json
import sqlite3
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable, Mapping, Sequence

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from index_bench_artifacts import infer_artifact_kind


SUPPORTED_SUFFIXES = {".json", ".jsonl", ".md"}
DETAIL_LIST_KEYS = ("results", "rows", "guides", "diagnostics", "failures")


class OptionalDependencyError(RuntimeError):
    """Raised when a requested optional database backend is unavailable."""


def resolve_backend(requested: str) -> str:
    if requested not in {"auto", "duckdb", "sqlite"}:
        raise ValueError(f"unsupported backend: {requested}")
    if requested == "sqlite":
        return "sqlite"
    try:
        import duckdb  # noqa: F401
    except ImportError as exc:
        if requested == "duckdb":
            raise OptionalDependencyError(
                "DuckDB backend requested but the optional 'duckdb' package is not "
                "installed. Install duckdb or rerun with --backend sqlite/auto."
            ) from exc
        return "sqlite"
    return "duckdb"


def connect_database(output_path: Path, backend: str):
    output_path.parent.mkdir(parents=True, exist_ok=True)
    if backend == "duckdb":
        import duckdb

        return duckdb.connect(str(output_path))
    return sqlite3.connect(str(output_path))


def iter_artifact_paths(inputs: Sequence[Path]) -> list[Path]:
    seen: set[Path] = set()
    paths: list[Path] = []
    for input_path in inputs:
        if input_path.is_dir():
            candidates = (
                path
                for path in sorted(input_path.rglob("*"))
                if path.is_file() and path.suffix.lower() in SUPPORTED_SUFFIXES
            )
        elif input_path.is_file() and input_path.suffix.lower() in SUPPORTED_SUFFIXES:
            candidates = (input_path,)
        else:
            candidates = ()

        for candidate in candidates:
            resolved = candidate.resolve()
            if resolved not in seen:
                seen.add(resolved)
                paths.append(candidate)
    return paths


def initialize_schema(conn) -> None:
    statements = [
        """
        CREATE TABLE IF NOT EXISTS ingest_runs (
            run_id TEXT PRIMARY KEY,
            source_label TEXT,
            ingested_at TEXT NOT NULL,
            backend TEXT NOT NULL
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS artifacts (
            artifact_id INTEGER PRIMARY KEY,
            run_id TEXT NOT NULL,
            path TEXT NOT NULL,
            absolute_path TEXT NOT NULL,
            kind TEXT NOT NULL,
            suffix TEXT NOT NULL,
            size_bytes INTEGER NOT NULL,
            mtime_utc TEXT NOT NULL,
            paired_markdown_path TEXT,
            json_type TEXT,
            top_level_keys_json TEXT,
            error TEXT
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS metrics (
            artifact_id INTEGER NOT NULL,
            metric_path TEXT NOT NULL,
            metric_value TEXT,
            metric_number DOUBLE,
            metric_type TEXT NOT NULL
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS detail_rows (
            artifact_id INTEGER NOT NULL,
            row_kind TEXT NOT NULL,
            row_index INTEGER NOT NULL,
            entity_id TEXT,
            section TEXT,
            lane TEXT,
            style TEXT,
            status TEXT,
            question TEXT,
            error TEXT,
            generation_time DOUBLE,
            prompt_tokens DOUBLE,
            completion_tokens DOUBLE,
            chunks_retrieved DOUBLE,
            server TEXT,
            model TEXT,
            raw_json TEXT NOT NULL
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS markdown_reports (
            artifact_id INTEGER NOT NULL,
            title TEXT,
            line_count INTEGER NOT NULL,
            heading_count INTEGER NOT NULL
        )
        """,
        "CREATE INDEX IF NOT EXISTS idx_metrics_path ON metrics(metric_path)",
        "CREATE INDEX IF NOT EXISTS idx_detail_rows_kind ON detail_rows(row_kind)",
        "DROP VIEW IF EXISTS v_artifact_summary",
        """
        CREATE VIEW v_artifact_summary AS
        SELECT
            a.path,
            a.kind,
            a.size_bytes,
            a.mtime_utc,
            a.json_type,
            a.paired_markdown_path,
            COUNT(DISTINCT m.metric_path) AS metric_count,
            COUNT(DISTINCT d.row_kind || ':' || CAST(d.row_index AS TEXT))
                AS detail_row_count,
            a.error
        FROM artifacts a
        LEFT JOIN metrics m ON m.artifact_id = a.artifact_id
        LEFT JOIN detail_rows d ON d.artifact_id = a.artifact_id
        GROUP BY
            a.artifact_id, a.path, a.kind, a.size_bytes, a.mtime_utc,
            a.json_type, a.paired_markdown_path, a.error
        """,
        "DROP VIEW IF EXISTS v_run_metrics",
        """
        CREATE VIEW v_run_metrics AS
        SELECT
            a.path,
            a.kind,
            a.mtime_utc,
            m.metric_path,
            m.metric_value,
            m.metric_number,
            m.metric_type
        FROM artifacts a
        JOIN metrics m ON m.artifact_id = a.artifact_id
        """,
        "DROP VIEW IF EXISTS v_trend_summary",
        """
        CREATE VIEW v_trend_summary AS
        SELECT
            a.path,
            a.mtime_utc,
            MAX(CASE WHEN m.metric_path = 'timestamp' THEN m.metric_value END)
                AS artifact_timestamp,
            MAX(CASE WHEN m.metric_path = 'config.model' THEN m.metric_value END)
                AS model,
            MAX(CASE WHEN m.metric_path = 'config.runtime_profile' THEN m.metric_value END)
                AS runtime_profile,
            MAX(CASE WHEN m.metric_path = 'config.mode' THEN m.metric_value END)
                AS mode,
            MAX(CASE WHEN m.metric_path = 'summary.total_prompts' THEN m.metric_number END)
                AS total_prompts,
            MAX(CASE WHEN m.metric_path = 'summary.successful_prompts' THEN m.metric_number END)
                AS successful_prompts,
            MAX(CASE WHEN m.metric_path = 'summary.errors' THEN m.metric_number END)
                AS errors,
            MAX(CASE WHEN m.metric_path = 'summary.wall_duration' THEN m.metric_number END)
                AS wall_duration,
            MAX(CASE WHEN m.metric_path = 'summary.avg_generation_time' THEN m.metric_number END)
                AS avg_generation_time,
            MAX(CASE WHEN m.metric_path = 'summary.guides_ranked' THEN m.metric_number END)
                AS guides_ranked,
            MAX(CASE WHEN m.metric_path = 'summary.guides_scanned' THEN m.metric_number END)
                AS guides_scanned,
            COUNT(DISTINCT d.row_kind || ':' || CAST(d.row_index AS TEXT))
                AS detail_row_count
        FROM artifacts a
        LEFT JOIN metrics m ON m.artifact_id = a.artifact_id
        LEFT JOIN detail_rows d ON d.artifact_id = a.artifact_id
        GROUP BY a.artifact_id, a.path, a.mtime_utc
        HAVING COUNT(m.metric_path) > 0 OR COUNT(d.row_index) > 0
        """,
    ]
    for statement in statements:
        conn.execute(statement)
    conn.commit()


def ingest_artifacts(
    conn,
    artifact_paths: Sequence[Path],
    *,
    base_dir: Path | None,
    backend: str,
    source_label: str,
) -> dict[str, Any]:
    run_id = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    conn.execute(
        "INSERT INTO ingest_runs(run_id, source_label, ingested_at, backend) VALUES (?, ?, ?, ?)",
        (run_id, source_label, datetime.now(timezone.utc).isoformat(), backend),
    )

    counts = {"artifacts": 0, "metrics": 0, "detail_rows": 0, "markdown_reports": 0}
    for path in artifact_paths:
        record_counts = ingest_artifact(conn, path, run_id=run_id, base_dir=base_dir)
        for key, value in record_counts.items():
            counts[key] += value

    conn.commit()
    return {"run_id": run_id, "backend": backend, **counts}


def ingest_artifact(conn, path: Path, *, run_id: str, base_dir: Path | None) -> dict[str, int]:
    stat = path.stat()
    rel_path = _relative_display_path(path, base_dir)
    paired_markdown = _paired_markdown_path(path, base_dir)
    artifact_id = _insert_artifact(
        conn,
        run_id=run_id,
        path=rel_path,
        absolute_path=str(path.resolve()),
        kind=infer_artifact_kind(path),
        suffix=path.suffix.lower(),
        size_bytes=stat.st_size,
        mtime_utc=datetime.fromtimestamp(stat.st_mtime, timezone.utc).isoformat(),
        paired_markdown_path=paired_markdown,
    )

    counts = {"artifacts": 1, "metrics": 0, "detail_rows": 0, "markdown_reports": 0}
    suffix = path.suffix.lower()
    if suffix == ".json":
        counts.update(_add_counts(counts, ingest_json_artifact(conn, artifact_id, path)))
    elif suffix == ".jsonl":
        counts.update(_add_counts(counts, ingest_jsonl_artifact(conn, artifact_id, path)))
    elif suffix == ".md":
        counts.update(_add_counts(counts, ingest_markdown_artifact(conn, artifact_id, path)))
    return counts


def ingest_json_artifact(conn, artifact_id: int, path: Path) -> dict[str, int]:
    counts = {"metrics": 0, "detail_rows": 0, "markdown_reports": 0}
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
        conn.execute(
            "UPDATE artifacts SET error = ? WHERE artifact_id = ?",
            (f"{exc.__class__.__name__}: {exc}", artifact_id),
        )
        return counts

    _update_json_shape(conn, artifact_id, data)
    counts["metrics"] += insert_metrics(conn, artifact_id, extract_metrics(data))
    counts["detail_rows"] += insert_detail_rows(conn, artifact_id, extract_detail_rows(data))
    return counts


def ingest_jsonl_artifact(conn, artifact_id: int, path: Path) -> dict[str, int]:
    rows: list[tuple[str, int, Mapping[str, Any]]] = []
    metrics: list[tuple[str, Any]] = []
    errors: list[str] = []
    evidence_row_index = 0
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except (OSError, UnicodeDecodeError) as exc:
        conn.execute(
            "UPDATE artifacts SET error = ? WHERE artifact_id = ?",
            (f"{exc.__class__.__name__}: {exc}", artifact_id),
        )
        return {"metrics": 0, "detail_rows": 0, "markdown_reports": 0}

    for index, line in enumerate(lines):
        if not line.strip():
            continue
        try:
            value = json.loads(line)
        except json.JSONDecodeError as exc:
            errors.append(f"line {index + 1}: {exc.__class__.__name__}")
            continue
        if isinstance(value, Mapping):
            rows.append(("jsonl", index, value))
            evidence_rows = _artifact_path_evidence_rows(value, evidence_row_index)
            evidence_row_index += len(evidence_rows)
            rows.extend(evidence_rows)
        else:
            metrics.append((f"jsonl.scalar_line.{index}", value))

    conn.execute(
        "UPDATE artifacts SET json_type = ?, error = ? WHERE artifact_id = ?",
        ("jsonl", "; ".join(errors) if errors else None, artifact_id),
    )
    return {
        "metrics": insert_metrics(conn, artifact_id, metrics),
        "detail_rows": insert_detail_rows(conn, artifact_id, rows),
        "markdown_reports": 0,
    }


def ingest_markdown_artifact(conn, artifact_id: int, path: Path) -> dict[str, int]:
    try:
        text = path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError) as exc:
        conn.execute(
            "UPDATE artifacts SET error = ? WHERE artifact_id = ?",
            (f"{exc.__class__.__name__}: {exc}", artifact_id),
        )
        return {"metrics": 0, "detail_rows": 0, "markdown_reports": 0}

    lines = text.splitlines()
    headings = [line.strip("# ").strip() for line in lines if line.startswith("#")]
    conn.execute(
        """
        INSERT INTO markdown_reports(artifact_id, title, line_count, heading_count)
        VALUES (?, ?, ?, ?)
        """,
        (artifact_id, headings[0] if headings else None, len(lines), len(headings)),
    )
    return {"metrics": 0, "detail_rows": 0, "markdown_reports": 1}


def extract_metrics(data: Any) -> list[tuple[str, Any]]:
    metrics: list[tuple[str, Any]] = []
    if not isinstance(data, Mapping):
        metrics.append(("json.item_count", len(data) if isinstance(data, list) else None))
        return metrics

    for key, value in data.items():
        if _is_scalar(value):
            metrics.append((key, value))
    for parent_key in ("config", "summary", "prompt_filters"):
        value = data.get(parent_key)
        if isinstance(value, Mapping):
            metrics.extend(_flatten_scalar_metrics(parent_key, value))
    return metrics


def extract_detail_rows(data: Any) -> list[tuple[str, int, Mapping[str, Any]]]:
    if isinstance(data, list):
        return [
            ("items", index, row)
            for index, row in enumerate(data)
            if isinstance(row, Mapping)
        ]
    if not isinstance(data, Mapping):
        return []

    rows: list[tuple[str, int, Mapping[str, Any]]] = []
    for key in DETAIL_LIST_KEYS:
        value = data.get(key)
        if isinstance(value, list):
            rows.extend(
                (key, index, row)
                for index, row in enumerate(value)
                if isinstance(row, Mapping)
            )
    return rows


def _artifact_path_evidence_rows(
    record: Mapping[str, Any], start_index: int
) -> list[tuple[str, int, Mapping[str, Any]]]:
    evidence = record.get("artifact_path_evidence")
    if not isinstance(evidence, list):
        return []

    rows: list[tuple[str, int, Mapping[str, Any]]] = []
    inherited_fields = _artifact_path_evidence_parent_fields(record)
    for evidence_index, item in enumerate(evidence):
        if isinstance(item, Mapping):
            rows.append(
                (
                    "artifact_path_evidence",
                    start_index + evidence_index,
                    _normalize_artifact_path_evidence(item, inherited_fields),
                )
            )
    return rows


def _artifact_path_evidence_parent_fields(
    record: Mapping[str, Any],
) -> dict[str, Any]:
    inherited: dict[str, Any] = {}
    for key in ("task", "lane", "label", "commit", "generated_at", "record_type"):
        value = record.get(key)
        if value is not None and _is_scalar(value):
            inherited[key] = value
    return inherited


def _normalize_artifact_path_evidence(
    item: Mapping[str, Any], inherited_fields: Mapping[str, Any]
) -> dict[str, Any]:
    normalized = dict(item)
    if normalized.get("status") is None and isinstance(
        normalized.get("exists"), bool
    ):
        normalized["status"] = (
            "present" if normalized.get("exists") else "missing"
        )
    if (
        normalized.get("path") is None
        and isinstance(normalized.get("artifact_path"), str)
        and normalized["artifact_path"].strip()
    ):
        normalized["path"] = normalized["artifact_path"]
    for key, value in inherited_fields.items():
        normalized.setdefault(key, value)
    return normalized


def insert_metrics(conn, artifact_id: int, metrics: Iterable[tuple[str, Any]]) -> int:
    rows = []
    for metric_path, value in metrics:
        if not _is_scalar(value):
            continue
        rows.append(
            (
                artifact_id,
                metric_path,
                None if value is None else str(value),
                _number_value(value),
                _metric_type(value),
            )
        )
    if rows:
        conn.executemany(
            """
            INSERT INTO metrics(
                artifact_id, metric_path, metric_value, metric_number, metric_type
            )
            VALUES (?, ?, ?, ?, ?)
            """,
            rows,
        )
    return len(rows)


def insert_detail_rows(
    conn,
    artifact_id: int,
    rows: Iterable[tuple[str, int, Mapping[str, Any]]],
) -> int:
    values = []
    for row_kind, row_index, row in rows:
        values.append(
            (
                artifact_id,
                row_kind,
                row_index,
                _first_text(row, "prompt_id", "guide_id", "id", "slug", "file"),
                _first_text(row, "section", "category"),
                _first_text(row, "lane"),
                _first_text(row, "style"),
                _first_text(
                    row,
                    "app_acceptance_status",
                    "answer_card_status",
                    "claim_support_status",
                    "status",
                    "candidate_action",
                ),
                _first_text(row, "question", "prompt"),
                _first_text(row, "error"),
                _number_value(row.get("generation_time")),
                _number_value(row.get("prompt_tokens")),
                _number_value(row.get("completion_tokens")),
                _number_value(row.get("chunks_retrieved")),
                _first_text(row, "server", "worker"),
                _first_text(row, "model"),
                json.dumps(row, sort_keys=True),
            )
        )
    if values:
        conn.executemany(
            """
            INSERT INTO detail_rows(
                artifact_id, row_kind, row_index, entity_id, section, lane, style,
                status, question, error, generation_time, prompt_tokens,
                completion_tokens, chunks_retrieved, server, model, raw_json
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            values,
        )
    return len(values)


def fetch_summary_rows(conn, limit: int) -> list[tuple[Any, ...]]:
    cursor = conn.execute(
        """
        SELECT
            path, model, runtime_profile, mode, total_prompts, successful_prompts,
            errors, wall_duration, avg_generation_time, guides_ranked,
            guides_scanned, detail_row_count
        FROM v_trend_summary
        ORDER BY mtime_utc DESC, path DESC
        LIMIT ?
        """,
        (limit,),
    )
    return list(cursor.fetchall())


def render_summary(rows: Sequence[Sequence[Any]]) -> str:
    headers = (
        "path",
        "model",
        "profile",
        "mode",
        "total",
        "ok",
        "errors",
        "wall_s",
        "avg_gen_s",
        "guides_ranked",
        "guides_scanned",
        "detail_rows",
    )
    lines = [
        "| " + " | ".join(headers) + " |",
        "| " + " | ".join("---" for _ in headers) + " |",
    ]
    for row in rows:
        lines.append("| " + " | ".join(_md_cell(value) for value in row) + " |")
    return "\n".join(lines)


def _insert_artifact(
    conn,
    *,
    run_id: str,
    path: str,
    absolute_path: str,
    kind: str,
    suffix: str,
    size_bytes: int,
    mtime_utc: str,
    paired_markdown_path: str | None,
) -> int:
    cursor = conn.execute("SELECT COALESCE(MAX(artifact_id), 0) + 1 FROM artifacts")
    artifact_id = int(cursor.fetchone()[0])
    conn.execute(
        """
        INSERT INTO artifacts(
            artifact_id, run_id, path, absolute_path, kind, suffix, size_bytes,
            mtime_utc, paired_markdown_path
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            artifact_id,
            run_id,
            path,
            absolute_path,
            kind,
            suffix,
            size_bytes,
            mtime_utc,
            paired_markdown_path,
        ),
    )
    return artifact_id


def _update_json_shape(conn, artifact_id: int, data: Any) -> None:
    if isinstance(data, Mapping):
        json_type = "object"
        keys = json.dumps(sorted(str(key) for key in data.keys()))
    elif isinstance(data, list):
        json_type = "array"
        keys = None
    else:
        json_type = type(data).__name__
        keys = None
    conn.execute(
        "UPDATE artifacts SET json_type = ?, top_level_keys_json = ? WHERE artifact_id = ?",
        (json_type, keys, artifact_id),
    )


def _flatten_scalar_metrics(prefix: str, data: Mapping[str, Any]) -> list[tuple[str, Any]]:
    metrics: list[tuple[str, Any]] = []
    for key, value in data.items():
        metric_path = f"{prefix}.{key}"
        if _is_scalar(value):
            metrics.append((metric_path, value))
        elif isinstance(value, Mapping):
            metrics.extend(_flatten_scalar_metrics(metric_path, value))
    return metrics


def _relative_display_path(path: Path, base_dir: Path | None) -> str:
    if base_dir is not None:
        try:
            return path.resolve().relative_to(base_dir.resolve()).as_posix()
        except ValueError:
            pass
    return path.as_posix()


def _paired_markdown_path(path: Path, base_dir: Path | None) -> str | None:
    if path.suffix.lower() not in {".json", ".jsonl"}:
        return None
    markdown_path = path.with_suffix(".md")
    if not markdown_path.exists():
        return None
    return _relative_display_path(markdown_path, base_dir)


def _add_counts(
    current: Mapping[str, int], increment: Mapping[str, int]
) -> dict[str, int]:
    merged = dict(current)
    for key, value in increment.items():
        merged[key] = merged.get(key, 0) + value
    return merged


def _is_scalar(value: Any) -> bool:
    return isinstance(value, (str, int, float, bool)) or value is None


def _number_value(value: Any) -> float | None:
    if isinstance(value, bool) or value is None:
        return None
    if isinstance(value, (int, float)):
        return float(value)
    try:
        return float(str(value).strip())
    except (TypeError, ValueError):
        return None


def _metric_type(value: Any) -> str:
    if value is None:
        return "null"
    if isinstance(value, bool):
        return "bool"
    if isinstance(value, (int, float)):
        return "number"
    return "text"


def _first_text(row: Mapping[str, Any], *keys: str) -> str | None:
    for key in keys:
        value = row.get(key)
        if _is_scalar(value) and value not in (None, ""):
            return str(value)
    return None


def _md_cell(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, float):
        text = f"{value:.4f}".rstrip("0").rstrip(".")
    else:
        text = str(value)
    return text.replace("|", "\\|").replace("\n", " ")


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "artifacts",
        nargs="+",
        type=Path,
        help="Bench artifact files or directories to ingest.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("artifacts/bench/bench_metrics_lake.db"),
        help="Database output path. Auto backend writes DuckDB when available, otherwise SQLite.",
    )
    parser.add_argument(
        "--backend",
        choices=("auto", "duckdb", "sqlite"),
        default="auto",
        help="Database backend to use (default: auto).",
    )
    parser.add_argument(
        "--base-dir",
        type=Path,
        default=None,
        help="Optional base directory for relative artifact paths.",
    )
    parser.add_argument(
        "--source-label",
        default="bench_metrics_lake",
        help="Label recorded on the ingest run.",
    )
    parser.add_argument(
        "--summary",
        action="store_true",
        help="Print the v_trend_summary rows after ingestion.",
    )
    parser.add_argument(
        "--summary-limit",
        type=int,
        default=20,
        help="Maximum summary rows to print.",
    )
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    try:
        backend = resolve_backend(args.backend)
    except OptionalDependencyError as exc:
        print(str(exc), file=sys.stderr)
        return 2

    artifact_paths = iter_artifact_paths(args.artifacts)
    if not artifact_paths:
        print("No supported .json, .jsonl, or .md artifacts found.", file=sys.stderr)
        return 1

    conn = connect_database(args.output, backend)
    try:
        initialize_schema(conn)
        summary = ingest_artifacts(
            conn,
            artifact_paths,
            base_dir=args.base_dir,
            backend=backend,
            source_label=args.source_label,
        )
        print(
            "Ingested {artifacts} artifacts, {metrics} metrics, "
            "{detail_rows} detail rows, {markdown_reports} markdown reports "
            "into {output} using {backend}.".format(output=args.output, **summary)
        )
        if backend == "sqlite" and args.backend == "auto":
            print("DuckDB unavailable; used SQLite fallback.")
        if args.summary:
            print(render_summary(fetch_summary_rows(conn, args.summary_limit)))
    finally:
        conn.close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

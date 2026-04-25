"""Compare contextual-shadow retrieval against existing bench baselines.

This is analyzer tooling only. It builds temporary shadow indexes from the
S15 JSONL export and writes paired retrieval-hit metrics without changing
production Chroma, lexical, query, guide, or Android behavior.
"""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import sys
import tempfile
from collections import Counter
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

try:
    import yaml
except ImportError:  # pragma: no cover - only hit in minimal environments.
    yaml = None


DEFAULT_EXPECTATIONS_PATH = Path("notes/specs/rag_prompt_expectations_seed_20260424.yaml")
GUIDE_WAVE_RE = re.compile(r"guide_wave_([a-z]+)_", re.IGNORECASE)
GUIDE_ID_RE = re.compile(r"\bGD-\d+\b")
HIT_METRICS = ("hit_at_1", "hit_at_3", "hit_at_k")

ROWS_FILENAME = "compare_contextual_shadow_retrieval_rows.jsonl"
SUMMARY_FILENAME = "compare_contextual_shadow_summary.json"
CSV_FILENAME = "compare_contextual_shadow_retrieval_rows.csv"

EXPECTED_GUIDE_KEYS = (
    "expected_guide_id",
    "expected_guide_ids",
    "expected_guides",
    "expected_source_guide_ids",
    "target_guide_id",
    "target_guide_ids",
    "target_guides",
    "guide_id",
    "guide_ids",
)

CSV_FIELDS = (
    "artifact_path",
    "artifact_name",
    "prompt_index",
    "prompt_id",
    "section",
    "question",
    "expected_topic",
    "expected_guide_ids",
    "baseline_top_guide_ids",
    "shadow_top_guide_ids",
    "baseline_expected_rank",
    "shadow_expected_rank",
    "baseline_hit_at_1",
    "baseline_hit_at_3",
    "baseline_hit_at_k",
    "shadow_hit_at_1",
    "shadow_hit_at_3",
    "shadow_hit_at_k",
    "rank_delta",
    "top_k_overlap_count",
    "top_k_overlap_jaccard",
)


def load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def load_expectations(path: Path | None) -> dict[str, Any]:
    if path is None:
        return {}
    if not path.exists():
        raise FileNotFoundError(path)
    if path.suffix.lower() == ".json":
        return load_json(path)
    if yaml is None:
        raise RuntimeError(f"Cannot read YAML expectations without PyYAML: {path}")
    with path.open("r", encoding="utf-8") as handle:
        return yaml.safe_load(handle) or {}


def read_frontmatter_fields(path: Path) -> dict[str, str]:
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except UnicodeDecodeError:
        lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    if not lines or lines[0].strip() != "---":
        return {}

    fields: dict[str, str] = {}
    for line in lines[1:]:
        if line.strip() == "---":
            break
        if ":" not in line or line[:1].isspace():
            continue
        key, value = line.split(":", 1)
        value = value.strip().strip('"').strip("'")
        if value:
            fields[key.strip()] = value
    return fields


def load_guide_lookup(guides_dir: Path = Path("guides")) -> dict[str, dict[str, str]]:
    lookup = {"slug_to_id": {}, "title_to_id": {}}
    if not guides_dir.exists():
        return lookup

    for path in guides_dir.glob("*.md"):
        fields = read_frontmatter_fields(path)
        guide_id = fields.get("id")
        if not guide_id:
            continue
        slug = fields.get("slug")
        title = fields.get("title")
        if slug:
            lookup["slug_to_id"][slug] = guide_id
        if title:
            lookup["title_to_id"][normalize_title(title)] = guide_id
    return lookup


def normalize_title(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip().lower()


def _dedupe(items: list[str]) -> list[str]:
    seen = set()
    deduped = []
    for item in items:
        if item and item not in seen:
            seen.add(item)
            deduped.append(item)
    return deduped


def _coerce_sequence(value: Any) -> list[Any]:
    if value is None:
        return []
    if isinstance(value, (list, tuple, set)):
        return list(value)
    if isinstance(value, str):
        if "|" in value:
            return [part.strip() for part in value.split("|")]
        if "," in value:
            return [part.strip() for part in value.split(",")]
    return [value]


def normalize_expected_guide_ids(
    value: Any, guide_lookup: dict[str, dict[str, str]] | None = None
) -> list[str]:
    """Resolve guide ids from ids, slugs, dict records, or compact strings."""
    guide_lookup = guide_lookup or {"slug_to_id": {}, "title_to_id": {}}
    resolved: list[str] = []
    for item in _coerce_sequence(value):
        if isinstance(item, dict):
            if item.get("id"):
                resolved.extend(normalize_expected_guide_ids(item["id"], guide_lookup))
            if item.get("slug"):
                slug = str(item["slug"]).strip()
                resolved.append(guide_lookup.get("slug_to_id", {}).get(slug, slug))
            if item.get("title"):
                title = normalize_title(str(item["title"]))
                resolved.append(guide_lookup.get("title_to_id", {}).get(title, title))
            continue

        text = str(item or "").strip()
        if not text:
            continue
        guide_ids = GUIDE_ID_RE.findall(text)
        if guide_ids:
            resolved.extend(guide_ids)
            continue
        resolved.append(guide_lookup.get("slug_to_id", {}).get(text, text))
    return _dedupe(resolved)


def wave_key_from_artifact_path(path: os.PathLike[str] | str) -> str | None:
    match = GUIDE_WAVE_RE.search(Path(path).name)
    return match.group(1).lower() if match else None


def extract_expected_guides_from_wave_manifest(
    artifact_path: os.PathLike[str] | str,
    expectations: dict[str, Any],
    guide_lookup: dict[str, dict[str, str]] | None = None,
) -> dict[str, Any]:
    wave_key = wave_key_from_artifact_path(artifact_path)
    wave_block = (expectations.get("waves") or {}).get(wave_key or "", {}) or {}
    return {
        "wave_key": wave_key,
        "expected_topic": wave_block.get("topic", ""),
        "expected_guide_ids": normalize_expected_guide_ids(
            wave_block.get("expected_guides"), guide_lookup
        ),
    }


def extract_expected_guides(
    row: dict[str, Any],
    artifact_path: os.PathLike[str] | str,
    expectations: dict[str, Any],
    guide_lookup: dict[str, dict[str, str]] | None = None,
) -> dict[str, Any]:
    """Resolve row-specific expectations, falling back to the wave manifest."""
    guide_lookup = guide_lookup or {"slug_to_id": {}, "title_to_id": {}}
    row_expected: list[str] = []
    for container in (
        row,
        row.get("prompt_metadata") if isinstance(row.get("prompt_metadata"), dict) else {},
        row.get("metadata") if isinstance(row.get("metadata"), dict) else {},
    ):
        for key in EXPECTED_GUIDE_KEYS:
            if key in container:
                row_expected.extend(
                    normalize_expected_guide_ids(container.get(key), guide_lookup)
                )

    manifest_info = extract_expected_guides_from_wave_manifest(
        artifact_path, expectations, guide_lookup
    )
    if row_expected:
        return {
            **manifest_info,
            "expected_guide_ids": _dedupe(row_expected),
        }
    return manifest_info


def load_contextual_shadow_jsonl(path: os.PathLike[str] | str) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    with Path(path).open("r", encoding="utf-8") as handle:
        for line_number, line in enumerate(handle, start=1):
            line = line.strip()
            if not line:
                continue
            try:
                record = json.loads(line)
            except json.JSONDecodeError as exc:
                raise ValueError(f"Invalid JSONL at {path}:{line_number}: {exc}") from exc
            if not isinstance(record, dict):
                raise ValueError(f"Shadow record at {path}:{line_number} is not an object")
            if not record.get("chunk_id"):
                raise ValueError(f"Shadow record at {path}:{line_number} lacks chunk_id")
            retrieval_text = record.get("contextual_retrieval_text") or record.get(
                "raptor_lite_text"
            )
            if not retrieval_text:
                raise ValueError(
                    f"Shadow record at {path}:{line_number} lacks contextual_retrieval_text/raptor_lite_text"
                )
            metadata = record.get("metadata")
            if not isinstance(metadata, dict):
                metadata = {}
            records.append(
                {
                    "chunk_id": str(record["chunk_id"]),
                    "document": str(record.get("document", "")),
                    "contextual_retrieval_text": str(retrieval_text),
                    "raptor_lite_text": str(record.get("raptor_lite_text", retrieval_text)),
                    "metadata": metadata,
                }
            )
    return records


def baseline_candidate_guide_ids(row: dict[str, Any]) -> list[str]:
    retrieval_meta = row.get("retrieval_metadata") or {}
    if not isinstance(retrieval_meta, dict):
        retrieval_meta = {}

    top_ids = normalize_expected_guide_ids(retrieval_meta.get("top_retrieved_guide_ids"))
    if top_ids:
        return top_ids

    source_candidates = retrieval_meta.get("source_candidates") or row.get(
        "source_candidates"
    )
    ids = []
    if isinstance(source_candidates, list):
        for candidate in source_candidates:
            if isinstance(candidate, dict):
                ids.extend(
                    normalize_expected_guide_ids(
                        candidate.get("guide_id") or candidate.get("guide_ids")
                    )
                )
    return _dedupe(ids)


def result_candidate_guide_ids(results: dict[str, Any], limit: int | None = None) -> list[str]:
    ids = []
    for metadata in (results.get("metadatas") or [[]])[0]:
        if isinstance(metadata, dict):
            ids.extend(normalize_expected_guide_ids(metadata.get("guide_id")))
            if limit is not None and len(ids) >= limit:
                break
    return _dedupe(ids[:limit] if limit is not None else ids)


def sanitize_chroma_metadata(metadata: dict[str, Any]) -> dict[str, str | int | float | bool]:
    """Keep temporary Chroma metadata within scalar value constraints."""
    sanitized: dict[str, str | int | float | bool] = {}
    for key, value in metadata.items():
        if value is None:
            sanitized[str(key)] = ""
        elif isinstance(value, (str, int, float, bool)):
            sanitized[str(key)] = value
        else:
            sanitized[str(key)] = json.dumps(value, ensure_ascii=False, sort_keys=True)
    return sanitized


def expected_rank(candidates: list[str], expected_ids: list[str]) -> int | None:
    expected = set(expected_ids)
    if not candidates or not expected:
        return None
    for index, guide_id in enumerate(candidates, start=1):
        if guide_id in expected:
            return index
    return None


def hit_flags(rank: int | None, candidates: list[str], expected_ids: list[str]) -> dict[str, bool | None]:
    if not candidates or not expected_ids:
        return {metric: None for metric in HIT_METRICS}
    if rank is None:
        return {metric: False for metric in HIT_METRICS}
    return {
        "hit_at_1": rank <= 1,
        "hit_at_3": rank <= 3,
        "hit_at_k": True,
    }


def compare_retrieval_row(
    *,
    artifact_path: os.PathLike[str] | str,
    prompt_index: int | str | None,
    question: str,
    baseline_top_guide_ids: list[str],
    shadow_top_guide_ids: list[str],
    expected_guide_ids: list[str],
    expected_topic: str = "",
    prompt_id: str | None = None,
    section: str | None = None,
) -> dict[str, Any]:
    baseline_rank = expected_rank(baseline_top_guide_ids, expected_guide_ids)
    shadow_rank = expected_rank(shadow_top_guide_ids, expected_guide_ids)
    baseline_hits = hit_flags(
        baseline_rank, baseline_top_guide_ids, expected_guide_ids
    )
    shadow_hits = hit_flags(shadow_rank, shadow_top_guide_ids, expected_guide_ids)
    baseline_set = set(baseline_top_guide_ids)
    shadow_set = set(shadow_top_guide_ids)
    overlap = baseline_set & shadow_set
    union = baseline_set | shadow_set
    rank_delta = None
    if baseline_rank is not None and shadow_rank is not None:
        rank_delta = baseline_rank - shadow_rank

    row = {
        "artifact_path": str(artifact_path),
        "artifact_name": Path(artifact_path).name,
        "prompt_index": prompt_index,
        "prompt_id": prompt_id,
        "section": section,
        "question": question,
        "expected_topic": expected_topic,
        "expected_guide_ids": expected_guide_ids,
        "baseline_top_guide_ids": baseline_top_guide_ids,
        "shadow_top_guide_ids": shadow_top_guide_ids,
        "baseline_expected_rank": baseline_rank,
        "shadow_expected_rank": shadow_rank,
        "rank_delta": rank_delta,
        "top_k_overlap_count": len(overlap),
        "top_k_overlap_jaccard": round(len(overlap) / len(union), 4) if union else None,
    }
    for metric, value in baseline_hits.items():
        row[f"baseline_{metric}"] = value
    for metric, value in shadow_hits.items():
        row[f"shadow_{metric}"] = value
    return row


def _score_side(rows: list[dict[str, Any]], side: str) -> dict[str, Any]:
    scored_rows = [
        row
        for row in rows
        if row.get("expected_guide_ids") and row.get(f"{side}_hit_at_k") is not None
    ]
    metrics: dict[str, Any] = {"scored_rows": len(scored_rows)}
    for metric in HIT_METRICS:
        count = sum(1 for row in scored_rows if row.get(f"{side}_{metric}") is True)
        metrics[metric] = {
            "count": count,
            "rate": round(count / len(scored_rows), 4) if scored_rows else None,
        }
    return metrics


def _comparable_hit_rows(rows: list[dict[str, Any]], metric: str) -> list[dict[str, Any]]:
    return [
        row
        for row in rows
        if row.get(f"baseline_{metric}") is not None
        and row.get(f"shadow_{metric}") is not None
    ]


def _metric_deltas(rows: list[dict[str, Any]], metric: str) -> dict[str, Any]:
    improved = regressed = unchanged = comparable = 0
    for row in _comparable_hit_rows(rows, metric):
        baseline = row.get(f"baseline_{metric}")
        shadow = row.get(f"shadow_{metric}")
        comparable += 1
        if baseline == shadow:
            unchanged += 1
        elif shadow and not baseline:
            improved += 1
        elif baseline and not shadow:
            regressed += 1

    return {
        "comparable_rows": comparable,
        "improved": improved,
        "regressed": regressed,
        "unchanged": unchanged,
        "net_improved": improved - regressed,
    }


def aggregate_comparison_rows(rows: list[dict[str, Any]]) -> dict[str, Any]:
    overlap_values = [
        row["top_k_overlap_jaccard"]
        for row in rows
        if row.get("top_k_overlap_jaccard") is not None
        and row.get("baseline_hit_at_k") is not None
        and row.get("shadow_hit_at_k") is not None
    ]
    baseline = _score_side(rows, "baseline")
    shadow = _score_side(rows, "shadow")
    deltas = {metric: _metric_deltas(rows, metric) for metric in HIT_METRICS}
    return {
        "generated_at": datetime.now().isoformat(timespec="seconds"),
        "row_count": len(rows),
        "expected_row_count": sum(1 for row in rows if row.get("expected_guide_ids")),
        "baseline": baseline,
        "shadow": shadow,
        "deltas": deltas,
        "mean_top_k_overlap_jaccard": (
            round(sum(overlap_values) / len(overlap_values), 4)
            if overlap_values
            else None
        ),
        "expected_topics": dict(
            Counter(row.get("expected_topic") or "unknown" for row in rows)
        ),
    }


def _csv_value(value: Any) -> str:
    if isinstance(value, list):
        return "|".join(str(item) for item in value)
    if value is None:
        return ""
    return str(value)


def write_outputs(
    rows: list[dict[str, Any]], summary: dict[str, Any], out_dir: os.PathLike[str] | str
) -> dict[str, str]:
    out_path = Path(out_dir)
    out_path.mkdir(parents=True, exist_ok=True)
    rows_path = out_path / ROWS_FILENAME
    summary_path = out_path / SUMMARY_FILENAME
    csv_path = out_path / CSV_FILENAME

    with rows_path.open("w", encoding="utf-8") as handle:
        for row in rows:
            json.dump(row, handle, ensure_ascii=False, sort_keys=True)
            handle.write("\n")

    with summary_path.open("w", encoding="utf-8") as handle:
        json.dump(summary, handle, indent=2, ensure_ascii=False, sort_keys=True)
        handle.write("\n")

    with csv_path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=CSV_FIELDS)
        writer.writeheader()
        for row in rows:
            writer.writerow({field: _csv_value(row.get(field)) for field in CSV_FIELDS})

    return {
        "rows_jsonl": str(rows_path),
        "summary_json": str(summary_path),
        "rows_csv": str(csv_path),
    }


@dataclass
class ShadowIndex:
    client: Any
    collection: Any
    chroma_tempdir: tempfile.TemporaryDirectory[str] | None = None
    lexical_tempdir: tempfile.TemporaryDirectory[str] | None = None
    lexical_path: Path | None = None

    def cleanup(self) -> None:
        self.chroma_tempdir = _cleanup_tempdir(self.chroma_tempdir)
        self.lexical_tempdir = _cleanup_tempdir(self.lexical_tempdir)


def _cleanup_tempdir(
    tempdir: tempfile.TemporaryDirectory[str] | None,
) -> tempfile.TemporaryDirectory[str] | None:
    if tempdir is not None:
        try:
            tempdir.cleanup()
        except PermissionError:
            # Windows can keep Chroma/SQLite temp files locked for a moment after
            # the script finishes. The indexes are temporary and ignored by git.
            pass
    return None


def _batched(records: list[dict[str, Any]], batch_size: int):
    for index in range(0, len(records), batch_size):
        yield records[index : index + batch_size]


def build_shadow_index(
    records: list[dict[str, Any]],
    *,
    collection_name: str,
    embed_url: str | None,
    batch_size: int,
    include_lexical: bool = True,
) -> ShadowIndex:
    import chromadb
    import query

    chroma_tempdir = None
    if hasattr(chromadb, "EphemeralClient"):
        client = chromadb.EphemeralClient()
    else:  # pragma: no cover - compatibility branch for older Chroma.
        chroma_tempdir = tempfile.TemporaryDirectory(
            prefix="senku-shadow-chroma-",
            ignore_cleanup_errors=True,
        )
        client = chromadb.PersistentClient(path=chroma_tempdir.name)

    collection = client.create_collection(
        name=collection_name,
        metadata={"hnsw:space": "cosine"},
    )
    for batch in _batched(records, batch_size):
        documents = [record["contextual_retrieval_text"] for record in batch]
        embeddings = query.embed_batch(documents, base_url=embed_url)
        if len(embeddings) != len(batch):
            raise RuntimeError(
                f"Shadow embedding count mismatch: expected {len(batch)}, got {len(embeddings)}"
            )
        collection.add(
            ids=[record["chunk_id"] for record in batch],
            documents=documents,
            embeddings=embeddings,
            metadatas=[sanitize_chroma_metadata(record["metadata"]) for record in batch],
        )

    lexical_tempdir = None
    lexical_path = None
    if include_lexical:
        from ingest import add_lexical_records, create_lexical_index

        lexical_tempdir = tempfile.TemporaryDirectory(
            prefix="senku-shadow-lexical-",
            ignore_cleanup_errors=True,
        )
        lexical_path = Path(lexical_tempdir.name) / "senku_shadow_lexical.sqlite3"
        conn = create_lexical_index(str(lexical_path))
        try:
            for batch in _batched(records, batch_size):
                add_lexical_records(
                    conn,
                    [record["chunk_id"] for record in batch],
                    [record["contextual_retrieval_text"] for record in batch],
                    [record["metadata"] for record in batch],
                )
        finally:
            conn.close()

    return ShadowIndex(
        client=client,
        collection=collection,
        chroma_tempdir=chroma_tempdir,
        lexical_tempdir=lexical_tempdir,
        lexical_path=lexical_path,
    )


def retrieve_shadow_guide_ids(
    question: str,
    shadow_index: ShadowIndex,
    *,
    top_k: int,
    embed_url: str | None,
) -> list[str]:
    import query

    old_lexical_path = query.config.LEXICAL_DB_PATH
    old_query_lexical_index = query.query_lexical_index
    if shadow_index.lexical_path is not None:
        query.config.LEXICAL_DB_PATH = str(shadow_index.lexical_path)
    else:
        query.query_lexical_index = (
            lambda _text, _limit, _category=None: query._empty_retrieval_results(
                kind="lexical"
            )
        )
    try:
        results, _sub_queries, _metadata = query.retrieve_results(
            question,
            shadow_index.collection,
            top_k,
            lm_studio_url=embed_url,
        )
        return result_candidate_guide_ids(results, limit=top_k)
    finally:
        query.query_lexical_index = old_query_lexical_index
        query.config.LEXICAL_DB_PATH = old_lexical_path


def compare_bench_artifacts(
    bench_paths: list[Path],
    *,
    expectations: dict[str, Any],
    guide_lookup: dict[str, dict[str, str]],
    shadow_index: ShadowIndex,
    top_k: int,
    embed_url: str | None,
    max_prompts: int | None = None,
) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for artifact_path in bench_paths:
        artifact = load_json(artifact_path)
        for result in artifact.get("results", []):
            if max_prompts is not None and len(rows) >= max_prompts:
                return rows
            question = str(result.get("question") or "").strip()
            if not question:
                continue
            expected = extract_expected_guides(
                result, artifact_path, expectations, guide_lookup
            )
            shadow_top = retrieve_shadow_guide_ids(
                question, shadow_index, top_k=top_k, embed_url=embed_url
            )
            rows.append(
                compare_retrieval_row(
                    artifact_path=artifact_path,
                    prompt_index=result.get("index"),
                    prompt_id=result.get("prompt_id"),
                    section=result.get("section"),
                    question=question,
                    baseline_top_guide_ids=baseline_candidate_guide_ids(result),
                    shadow_top_guide_ids=shadow_top,
                    expected_guide_ids=expected["expected_guide_ids"],
                    expected_topic=expected.get("expected_topic", ""),
                )
            )
    return rows


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Compare S15 contextual shadow retrieval with bench baselines."
    )
    parser.add_argument("--shadow-jsonl", required=True, help="S15 shadow JSONL path.")
    parser.add_argument(
        "--bench",
        nargs="+",
        required=True,
        help="One or more bench JSON artifacts to compare.",
    )
    parser.add_argument(
        "--out-dir",
        required=True,
        help="Directory for summary JSON, row JSONL, and CSV outputs.",
    )
    parser.add_argument(
        "--expectations",
        default=str(DEFAULT_EXPECTATIONS_PATH),
        help="Expectation manifest YAML/JSON. Use empty string to disable.",
    )
    parser.add_argument("--guides-dir", default="guides", help="Guide markdown root.")
    parser.add_argument("--top-k", type=int, default=8, help="Retrieval top-k.")
    parser.add_argument("--embed-url", default=None, help="Embedding endpoint base URL.")
    parser.add_argument(
        "--collection-name",
        default="senku_contextual_shadow_compare",
        help="Temporary Chroma collection name.",
    )
    parser.add_argument("--batch-size", type=int, default=64, help="Embedding batch size.")
    parser.add_argument(
        "--max-shadow-records",
        type=int,
        default=None,
        help="Optional smoke-test cap for loaded shadow records.",
    )
    parser.add_argument(
        "--max-prompts",
        type=int,
        default=None,
        help="Optional smoke-test cap for compared prompts.",
    )
    parser.add_argument(
        "--disable-shadow-lexical",
        action="store_true",
        help="Use shadow vector retrieval only instead of temporary contextual FTS.",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    expectations_path = Path(args.expectations) if args.expectations else None
    expectations = load_expectations(expectations_path)
    guide_lookup = load_guide_lookup(Path(args.guides_dir))
    records = load_contextual_shadow_jsonl(args.shadow_jsonl)
    if args.max_shadow_records is not None:
        records = records[: args.max_shadow_records]
    if not records:
        raise SystemExit("No contextual shadow records loaded.")

    shadow_index = build_shadow_index(
        records,
        collection_name=args.collection_name,
        embed_url=args.embed_url,
        batch_size=args.batch_size,
        include_lexical=not args.disable_shadow_lexical,
    )
    try:
        rows = compare_bench_artifacts(
            [Path(path) for path in args.bench],
            expectations=expectations,
            guide_lookup=guide_lookup,
            shadow_index=shadow_index,
            top_k=args.top_k,
            embed_url=args.embed_url,
            max_prompts=args.max_prompts,
        )
        summary = aggregate_comparison_rows(rows)
        outputs = write_outputs(rows, summary, args.out_dir)
    finally:
        shadow_index.cleanup()

    print(f"Wrote {len(rows)} contextual shadow comparison rows.")
    print(f"Summary: {outputs['summary_json']}")
    print(f"Rows: {outputs['rows_jsonl']}")
    print(f"CSV: {outputs['rows_csv']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

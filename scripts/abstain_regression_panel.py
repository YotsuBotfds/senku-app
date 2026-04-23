#!/usr/bin/env python3
"""Run a small abstain-threshold regression panel against live retrieval."""

from __future__ import annotations

import argparse
import csv
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

import yaml


def _parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Evaluate abstain-threshold sensitivity on a curated panel."
    )
    parser.add_argument(
        "--panel-path", required=True, help="Path to panel JSON or YAML."
    )
    parser.add_argument("--output-dir", required=True, help="Directory for artifacts.")
    parser.add_argument(
        "--top-k", type=int, default=8, help="Retrieval top-k to use for each query."
    )
    parser.add_argument(
        "--lm-studio-url",
        default=None,
        help="Embedding endpoint override. Defaults to config.LM_STUDIO_URL.",
    )
    parser.add_argument(
        "--chroma-db-dir",
        default=None,
        help="Chroma DB path override. Defaults to config.CHROMA_DB_DIR.",
    )
    return parser.parse_args()


def _round3(value: float) -> float:
    return round(float(value), 3)


def _decision(
    max_overlap, max_similarity, unique_hit_count, sim_threshold, hit_threshold
):
    return (
        max_overlap <= query._ABSTAIN_MAX_OVERLAP_TOKENS
        and max_similarity < sim_threshold
        and unique_hit_count < hit_threshold
    )


def _normalize_panel_payload(panel_path: Path, data) -> tuple[dict, list[dict]]:
    if isinstance(data, list):
        if not data:
            raise ValueError("Panel file must contain at least one query.")
        return {}, data

    if not isinstance(data, dict):
        raise ValueError(
            "Panel file must be either a query list or a mapping with queries."
        )

    panel = data.get("queries")
    if not isinstance(panel, list) or not panel:
        raise ValueError("Panel mapping must include a non-empty queries list.")

    panel_metadata = {
        "panel_id": data.get("panel_id") or panel_path.stem,
        "title": data.get("title") or "Abstain Regression Panel",
        "description": data.get("description") or "",
    }
    return panel_metadata, panel


def _load_panel(panel_path: Path) -> tuple[dict, list[dict]]:
    with panel_path.open("r", encoding="utf-8") as handle:
        data = yaml.safe_load(handle)

    panel_metadata, panel = _normalize_panel_payload(panel_path, data)
    return panel_metadata, panel


def _compute_row_metrics(question: str, results: dict, *, row_limit: int) -> dict:
    query_tokens = query._content_tokens(question)
    docs = results.get("documents", [[]])[0]
    metas = results.get("metadatas", [[]])[0]
    dists = results.get("distances", [[]])[0]
    max_overlap = 0
    max_similarity = 0.0
    unique_hits = set()

    for index, (doc, meta) in enumerate(zip(docs, metas)):
        if index >= row_limit:
            break
        dist = dists[index] if index < len(dists) else 1.0
        overlap_tokens = query._abstain_row_overlap_tokens(query_tokens, doc, meta)
        max_overlap = max(max_overlap, len(overlap_tokens))
        max_similarity = max(
            max_similarity, query._abstain_row_vector_similarity(meta, dist)
        )
        unique_hits.update(overlap_tokens)

    return {
        "max_overlap": max_overlap,
        "max_similarity": _round3(max_similarity),
        "unique_hit_count": len(unique_hits),
        "unique_hits": sorted(unique_hits),
        "predicted_abstain": _decision(
            max_overlap,
            max_similarity,
            len(unique_hits),
            query._ABSTAIN_MIN_VECTOR_SIMILARITY,
            query._ABSTAIN_MIN_UNIQUE_LEXICAL_HITS,
        ),
    }


def _top_row_details(question: str, results: dict) -> list[dict]:
    query_tokens = query._content_tokens(question)
    rows = []
    for doc, meta, dist in query._abstain_top_rows(results):
        overlap_tokens = query._abstain_row_overlap_tokens(query_tokens, doc, meta)
        rows.append(
            {
                "guide_id": meta.get("guide_id"),
                "guide_title": meta.get("guide_title"),
                "category": meta.get("category"),
                "section_heading": meta.get("section_heading"),
                "vector_similarity": _round3(
                    query._abstain_row_vector_similarity(meta, dist)
                ),
                "overlap_count": len(overlap_tokens),
                "overlap_tokens": sorted(overlap_tokens),
                "lexical_hits": int(meta.get("_lexical_hits", 0) or 0),
            }
        )
    return rows


def _evaluate_panel(
    panel, collection, *, top_k: int, lm_studio_url: str | None
) -> list[dict]:
    evaluated = []
    for item in panel:
        question = item["question"]
        results, _sub_queries, retrieval_meta = query.retrieve_results(
            question,
            collection,
            top_k,
            lm_studio_url=lm_studio_url,
        )
        base_metrics = _compute_row_metrics(
            question, results, row_limit=query._ABSTAIN_ROW_LIMIT
        )
        row_limit_metrics = {
            str(limit): _compute_row_metrics(question, results, row_limit=limit)
            for limit in (1, 2, 3, 5)
        }
        base_prediction, match_labels = query._should_abstain(results, question)

        evaluated.append(
            {
                **item,
                "off_topic_gate": bool(query._is_clearly_off_topic(question)),
                "base_prediction": bool(base_prediction),
                "base_match_labels": match_labels,
                "metrics": base_metrics,
                "row_limit_metrics": row_limit_metrics,
                "retrieval_meta": {
                    "objective_coverage": (
                        (retrieval_meta.get("current_frame") or {}).get(
                            "objective_coverage"
                        )
                    ),
                    "support_summary": retrieval_meta.get("support_summary"),
                    "retrieval_mix": retrieval_meta.get("retrieval_mix"),
                    "categories": retrieval_meta.get("categories"),
                },
                "top_rows": _top_row_details(question, results),
            }
        )
    return evaluated


def _build_sweep_rows(panel_rows: list[dict]) -> list[dict]:
    similarities = [
        _round3(query._ABSTAIN_MIN_VECTOR_SIMILARITY - 0.05),
        _round3(query._ABSTAIN_MIN_VECTOR_SIMILARITY),
        _round3(query._ABSTAIN_MIN_VECTOR_SIMILARITY + 0.05),
    ]
    hit_thresholds = [
        query._ABSTAIN_MIN_UNIQUE_LEXICAL_HITS - 1,
        query._ABSTAIN_MIN_UNIQUE_LEXICAL_HITS,
        query._ABSTAIN_MIN_UNIQUE_LEXICAL_HITS + 1,
    ]

    expected_abstain_total = sum(1 for row in panel_rows if row["expected_abstain"])
    expected_non_abstain_total = sum(
        1 for row in panel_rows if not row["expected_abstain"]
    )
    sweep_rows = []

    for sim_threshold in similarities:
        for hit_threshold in hit_thresholds:
            true_positive = 0
            false_positive = 0
            false_negative = 0
            predictions = {}

            for row in panel_rows:
                predicted = _decision(
                    row["metrics"]["max_overlap"],
                    row["metrics"]["max_similarity"],
                    row["metrics"]["unique_hit_count"],
                    sim_threshold,
                    hit_threshold,
                )
                predictions[row["id"]] = predicted
                if row["expected_abstain"] and predicted:
                    true_positive += 1
                elif row["expected_abstain"] and not predicted:
                    false_negative += 1
                elif (not row["expected_abstain"]) and predicted:
                    false_positive += 1

            abstain_count = sum(1 for value in predictions.values() if value)
            sweep_rows.append(
                {
                    "similarity_threshold": sim_threshold,
                    "unique_hit_threshold": hit_threshold,
                    "abstain_count": abstain_count,
                    "abstain_rate": _round3(abstain_count / len(panel_rows)),
                    "false_positive_count": false_positive,
                    "false_positive_rate": _round3(
                        false_positive / expected_non_abstain_total
                    ),
                    "true_positive_count": true_positive,
                    "false_negative_count": false_negative,
                    "true_positive_rate": _round3(
                        true_positive / expected_abstain_total
                    ),
                    "predictions": predictions,
                }
            )

    return sweep_rows


def _write_summary_csv(path: Path, sweep_rows: list[dict]) -> None:
    fieldnames = [
        "similarity_threshold",
        "unique_hit_threshold",
        "abstain_count",
        "abstain_rate",
        "false_positive_count",
        "false_positive_rate",
        "true_positive_count",
        "true_positive_rate",
        "false_negative_count",
    ]
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        for row in sweep_rows:
            writer.writerow({name: row[name] for name in fieldnames})


def _write_query_details_csv(path: Path, panel_rows: list[dict]) -> None:
    fieldnames = [
        "id",
        "bucket",
        "expected_abstain",
        "base_prediction",
        "max_overlap",
        "max_similarity",
        "unique_hit_count",
        "unique_hits",
        "top_guides",
        "row_limit_1_predicted",
        "row_limit_2_predicted",
        "row_limit_3_predicted",
        "row_limit_5_predicted",
    ]
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        for row in panel_rows:
            writer.writerow(
                {
                    "id": row["id"],
                    "bucket": row["bucket"],
                    "expected_abstain": row["expected_abstain"],
                    "base_prediction": row["base_prediction"],
                    "max_overlap": row["metrics"]["max_overlap"],
                    "max_similarity": row["metrics"]["max_similarity"],
                    "unique_hit_count": row["metrics"]["unique_hit_count"],
                    "unique_hits": ", ".join(row["metrics"]["unique_hits"]),
                    "top_guides": ", ".join(
                        filter(
                            None,
                            [
                                details.get("guide_id")
                                for details in row["top_rows"][: query._ABSTAIN_ROW_LIMIT]
                            ],
                        )
                    ),
                    "row_limit_1_predicted": row["row_limit_metrics"]["1"][
                        "predicted_abstain"
                    ],
                    "row_limit_2_predicted": row["row_limit_metrics"]["2"][
                        "predicted_abstain"
                    ],
                    "row_limit_3_predicted": row["row_limit_metrics"]["3"][
                        "predicted_abstain"
                    ],
                    "row_limit_5_predicted": row["row_limit_metrics"]["5"][
                        "predicted_abstain"
                    ],
                }
            )


def _build_summary_markdown(
    metadata: dict, panel_rows: list[dict], sweep_rows: list[dict]
) -> str:
    production_row = next(
        row
        for row in sweep_rows
        if row["similarity_threshold"] == _round3(query._ABSTAIN_MIN_VECTOR_SIMILARITY)
        and row["unique_hit_threshold"] == query._ABSTAIN_MIN_UNIQUE_LEXICAL_HITS
    )
    lines = [
        f"# {metadata['panel_title']}",
        "",
        f"- Generated at: `{metadata['generated_at_utc']}`",
        f"- Panel ID: `{metadata['panel_id']}`",
        f"- Panel path: `{metadata['panel_path']}`",
        f"- Top-k: `{metadata['top_k']}`",
        f"- Collection size: `{metadata['collection_count']}`",
        f"- LM Studio URL: `{metadata['lm_studio_url']}`",
        f"- Production constants: row_limit=`{metadata['row_limit']}`, max_overlap_tokens=`{metadata['max_overlap_tokens']}`, min_vector_similarity=`{metadata['min_vector_similarity']}`, min_unique_hits=`{metadata['min_unique_hits']}`",
        f"- Panel size: `{metadata['panel_size']}` ({metadata['expected_abstain_count']} should abstain / {metadata['expected_non_abstain_count']} should not)",
    ]
    if metadata["panel_description"]:
        lines.append(f"- Description: {metadata['panel_description']}")

    lines.extend(
        [
            "",
            "## Sweep Summary",
            "",
            "| similarity | hits | abstain rate | false-positive rate | TP | FP | FN |",
            "| --- | --- | --- | --- | --- | --- | --- |",
        ]
    )
    for row in sweep_rows:
        lines.append(
            f"| {row['similarity_threshold']:.2f} | {row['unique_hit_threshold']} | "
            f"{row['abstain_rate']:.3f} | {row['false_positive_rate']:.3f} | "
            f"{row['true_positive_count']} | {row['false_positive_count']} | {row['false_negative_count']} |"
        )

    lines.extend(
        [
            "",
            "## Production-Point Per Query",
            "",
            "| id | expected | predicted | max overlap | max similarity | unique hits | top-3 guides |",
            "| --- | --- | --- | --- | --- | --- | --- |",
        ]
    )
    for row in panel_rows:
        lines.append(
            f"| {row['id']} | "
            f"{'abstain' if row['expected_abstain'] else 'answer'} | "
            f"{'abstain' if row['base_prediction'] else 'answer'} | "
            f"{row['metrics']['max_overlap']} | "
            f"{row['metrics']['max_similarity']:.3f} | "
            f"{row['metrics']['unique_hit_count']} | "
            f"{', '.join(filter(None, [entry.get('guide_id') for entry in row['top_rows'][:3]]))} |"
        )

    row_limit_changes = [
        row
        for row in panel_rows
        if row["row_limit_metrics"]["3"]["predicted_abstain"]
        != row["row_limit_metrics"]["5"]["predicted_abstain"]
    ]
    lines.extend(
        [
            "",
            "## Row-Limit Notes",
            "",
            f"- Production-point (`{metadata['min_vector_similarity']:.2f} / {metadata['min_unique_hits']}`) abstain rate: `{production_row['abstain_rate']:.3f}`",
            f"- Production-point false-positive rate: `{production_row['false_positive_rate']:.3f}`",
            f"- Production-point true-positive count: `{production_row['true_positive_count']}` / `{metadata['expected_abstain_count']}`",
        ]
    )
    if row_limit_changes:
        changed_ids = ", ".join(row["id"] for row in row_limit_changes)
        lines.append(
            f"- Queries that change between row_limit=3 and row_limit=5 at the production point: `{changed_ids}`"
        )
    else:
        lines.append(
            "- No query changed between row_limit=3 and row_limit=5 at the production point."
        )

    return "\n".join(lines) + "\n"


if __name__ == "__main__":
    args = _parse_args()
    script_path = Path(__file__).resolve()
    repo_root = script_path.parents[1]
    if str(repo_root) not in sys.path:
        sys.path.insert(0, str(repo_root))

    import chromadb
    import config
    import query

    panel_path = Path(args.panel_path).resolve()
    output_dir = Path(args.output_dir).resolve()
    output_dir.mkdir(parents=True, exist_ok=True)

    panel_metadata, panel = _load_panel(panel_path)
    chroma_db_dir = args.chroma_db_dir or config.CHROMA_DB_DIR
    lm_studio_url = args.lm_studio_url or config.LM_STUDIO_URL

    client = chromadb.PersistentClient(path=chroma_db_dir)
    collection = client.get_collection("senku_guides")

    evaluated_panel = _evaluate_panel(
        panel, collection, top_k=args.top_k, lm_studio_url=lm_studio_url
    )
    sweep_rows = _build_sweep_rows(evaluated_panel)

    metadata = {
        "generated_at_utc": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "panel_id": panel_metadata.get("panel_id") or panel_path.stem,
        "panel_title": panel_metadata.get("title") or "Abstain Regression Panel",
        "panel_description": panel_metadata.get("description") or "",
        "panel_path": str(panel_path),
        "output_dir": str(output_dir),
        "top_k": args.top_k,
        "collection_count": collection.count(),
        "lm_studio_url": lm_studio_url,
        "chroma_db_dir": chroma_db_dir,
        "row_limit": query._ABSTAIN_ROW_LIMIT,
        "max_overlap_tokens": query._ABSTAIN_MAX_OVERLAP_TOKENS,
        "min_vector_similarity": _round3(query._ABSTAIN_MIN_VECTOR_SIMILARITY),
        "min_unique_hits": query._ABSTAIN_MIN_UNIQUE_LEXICAL_HITS,
        "panel_size": len(evaluated_panel),
        "expected_abstain_count": sum(
            1 for row in evaluated_panel if row["expected_abstain"]
        ),
        "expected_non_abstain_count": sum(
            1 for row in evaluated_panel if not row["expected_abstain"]
        ),
    }

    summary = {
        "metadata": metadata,
        "sweep": sweep_rows,
        "queries": evaluated_panel,
    }

    (output_dir / "summary.json").write_text(
        json.dumps(summary, indent=2) + "\n", encoding="utf-8"
    )
    _write_summary_csv(output_dir / "sweep_summary.csv", sweep_rows)
    _write_query_details_csv(output_dir / "query_details.csv", evaluated_panel)
    (output_dir / "summary.md").write_text(
        _build_summary_markdown(metadata, evaluated_panel, sweep_rows),
        encoding="utf-8",
    )

    print(f"Wrote abstain regression artifacts to {output_dir}")

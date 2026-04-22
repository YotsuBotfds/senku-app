#!/usr/bin/env python3
"""Refresh retrieval metadata inside an existing Senku mobile pack."""

from __future__ import annotations

import argparse
import json
import shutil
import sqlite3
import sys
from contextlib import closing
from datetime import datetime, timezone
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

import config  # noqa: E402
from metadata_validation import (  # noqa: E402
    format_validation_errors,
    report_has_errors,
    validate_guide_records,
)
from mobile_pack import (  # noqa: E402
    DEFAULT_MANIFEST_FILENAME,
    DEFAULT_SQLITE_FILENAME,
    ChunkRecord,
    GuideRecord,
    STRUCTURE_TYPE_GENERAL,
    VECTOR_DTYPE_SUFFIXES,
    _derive_chunk_metadata,
    _derive_guide_metadata,
    _file_info,
    load_guide_records,
)


def _safe_text(value) -> str:
    return "" if value is None else str(value)


def _metadata_guide_count(guide_metadata_by_id) -> int:
    total = 0
    for metadata in guide_metadata_by_id.values():
        if metadata.structure_type != STRUCTURE_TYPE_GENERAL or metadata.topic_tags:
            total += 1
    return total


def _copy_pack_tree(input_dir: Path, output_dir: Path) -> None:
    if output_dir.exists():
        shutil.rmtree(output_dir)
    shutil.copytree(input_dir, output_dir)


def _refresh_sqlite(sqlite_path: Path, guide_records: list[GuideRecord]) -> int:
    guide_metadata_by_id = {
        guide.guide_id: _derive_guide_metadata(guide)
        for guide in guide_records
    }
    metadata_guide_count = _metadata_guide_count(guide_metadata_by_id)
    generated_at = datetime.now(timezone.utc).isoformat()

    with closing(sqlite3.connect(sqlite_path)) as conn:
        conn.execute("PRAGMA journal_mode=WAL")
        conn.execute("PRAGMA synchronous=NORMAL")

        guide_updates = []
        for guide in guide_records:
            metadata = guide_metadata_by_id[guide.guide_id]
            guide_updates.append(
                (
                    metadata.content_role,
                    metadata.time_horizon,
                    metadata.structure_type,
                    metadata.topic_tags,
                    guide.guide_id,
                )
            )
        conn.executemany(
            """
            UPDATE guides
            SET content_role = ?, time_horizon = ?, structure_type = ?, topic_tags = ?
            WHERE guide_id = ?
            """,
            guide_updates,
        )

        chunk_updates = []
        rows = conn.execute(
            """
            SELECT chunk_id, source_file, guide_id, guide_title, slug, description, category,
                   difficulty, last_updated, version, liability_level, tags, related,
                   section_id, section_heading, document
            FROM chunks
            """
        ).fetchall()
        for row in rows:
            chunk = ChunkRecord(
                chunk_id=_safe_text(row[0]),
                source_file=_safe_text(row[1]),
                guide_id=_safe_text(row[2]),
                guide_title=_safe_text(row[3]),
                slug=_safe_text(row[4]),
                description=_safe_text(row[5]),
                category=_safe_text(row[6]),
                difficulty=_safe_text(row[7]),
                last_updated=_safe_text(row[8]),
                version=_safe_text(row[9]),
                liability_level=_safe_text(row[10]),
                tags=_safe_text(row[11]),
                related=_safe_text(row[12]),
                section_id=_safe_text(row[13]),
                section_heading=_safe_text(row[14]),
                document=_safe_text(row[15]),
                embedding=(),
            )
            metadata = _derive_chunk_metadata(chunk, guide_metadata_by_id.get(chunk.guide_id))
            chunk_updates.append(
                (
                    metadata.content_role,
                    metadata.time_horizon,
                    metadata.structure_type,
                    metadata.topic_tags,
                    chunk.chunk_id,
                )
            )

        conn.executemany(
            """
            UPDATE chunks
            SET content_role = ?, time_horizon = ?, structure_type = ?, topic_tags = ?
            WHERE chunk_id = ?
            """,
            chunk_updates,
        )

        conn.execute("DELETE FROM lexical_chunks_fts")
        conn.execute(
            """
            INSERT INTO lexical_chunks_fts (
                chunk_id, search_text, guide_title, guide_id, section_heading,
                slug, tags, description, category, liability_level,
                content_role, time_horizon, structure_type, topic_tags
            )
            SELECT
                chunk_id, document, guide_title, guide_id, section_heading,
                slug, tags, description, category, liability_level,
                content_role, time_horizon, structure_type, topic_tags
            FROM chunks
            """
        )
        conn.execute(
            """
            INSERT INTO pack_meta (key, value)
            VALUES ('generated_at', ?)
            ON CONFLICT(key) DO UPDATE SET value=excluded.value
            """,
            (generated_at,),
        )
        conn.execute(
            """
            INSERT INTO pack_meta (key, value)
            VALUES ('retrieval_metadata_guide_count', ?)
            ON CONFLICT(key) DO UPDATE SET value=excluded.value
            """,
            (str(metadata_guide_count),),
        )
        conn.execute("INSERT INTO lexical_chunks_fts(lexical_chunks_fts) VALUES('optimize')")
        conn.commit()

    return metadata_guide_count


def _detect_vector_path(output_dir: Path, manifest: dict) -> Path:
    vector_name = manifest.get("files", {}).get("vectors", {}).get("path")
    if vector_name:
        return output_dir / vector_name
    vector_dtype = manifest.get("embedding", {}).get("vector_dtype", "float16")
    suffix = VECTOR_DTYPE_SUFFIXES.get(vector_dtype, "f16")
    return output_dir / f"senku_vectors.{suffix}"


def _refresh_manifest(output_dir: Path, metadata_guide_count: int) -> dict:
    manifest_path = output_dir / DEFAULT_MANIFEST_FILENAME
    sqlite_path = output_dir / DEFAULT_SQLITE_FILENAME
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    vector_path = _detect_vector_path(output_dir, manifest)

    sqlite_info = _file_info(sqlite_path)
    vector_info = _file_info(vector_path)
    generated_at = datetime.now(timezone.utc).isoformat()

    manifest["generated_at"] = generated_at
    manifest.setdefault("counts", {})
    manifest["counts"]["retrieval_metadata_guides"] = metadata_guide_count
    manifest.setdefault("files", {})
    manifest["files"]["sqlite"] = sqlite_info
    manifest["files"]["vectors"] = vector_info

    manifest_path.write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return {
        "manifest_path": str(manifest_path),
        "sqlite_path": str(sqlite_path),
        "vector_path": str(vector_path),
        "generated_at": generated_at,
        "counts": manifest["counts"],
        "files": manifest["files"],
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input_dir", help="Existing mobile pack directory to refresh")
    parser.add_argument("output_dir", help="Output directory for the refreshed pack")
    parser.add_argument(
        "--guides-dir",
        default=config.COMPENDIUM_DIR,
        help=f"Guides directory (default: {config.COMPENDIUM_DIR})",
    )
    args = parser.parse_args()

    input_dir = Path(args.input_dir).resolve()
    output_dir = Path(args.output_dir).resolve()
    guides_dir = Path(args.guides_dir).resolve()

    if not input_dir.is_dir():
        raise SystemExit(f"Input pack directory not found: {input_dir}")
    if not (input_dir / DEFAULT_SQLITE_FILENAME).is_file():
        raise SystemExit(f"Missing {DEFAULT_SQLITE_FILENAME} in {input_dir}")
    if not (input_dir / DEFAULT_MANIFEST_FILENAME).is_file():
        raise SystemExit(f"Missing {DEFAULT_MANIFEST_FILENAME} in {input_dir}")

    guide_records = load_guide_records(guides_dir)
    metadata_report = validate_guide_records(
        guide_records,
        scope="refresh_mobile_pack_metadata",
    )
    _copy_pack_tree(input_dir, output_dir)
    if report_has_errors(metadata_report):
        raise SystemExit(format_validation_errors(metadata_report))
    metadata_guide_count = _refresh_sqlite(output_dir / DEFAULT_SQLITE_FILENAME, guide_records)
    summary = _refresh_manifest(output_dir, metadata_guide_count)
    print(json.dumps(summary, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()

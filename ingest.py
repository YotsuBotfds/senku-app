#!/usr/bin/env python3
"""Ingest markdown guides into ChromaDB for RAG retrieval."""

import argparse
import hashlib
import json
import os
import re
import shutil
import sqlite3
import sys
import time
from collections import defaultdict

import chromadb
import requests
import yaml
from rich.console import Console
from rich.table import Table

import config
from lmstudio_utils import (
    embedding_models_to_try,
    is_retryable_lm_request,
    normalize_lm_studio_url,
    should_try_embedding_fallback,
)
from metadata_helpers import derive_bridge_metadata, normalize_tags
from metadata_validation import (
    REPORT_FILENAME as METADATA_VALIDATION_REPORT_FILENAME,
    format_validation_errors,
    report_has_errors,
    validate_guide_records,
    write_validation_report,
)
from token_estimation import estimate_tokens

console = Console()
COLLECTION_NAME = "senku_guides"
MANIFEST_PATH = os.path.join(config.CHROMA_DB_DIR, "ingest_manifest.json")
METADATA_VALIDATION_REPORT_PATH = os.path.join(
    config.CHROMA_DB_DIR,
    METADATA_VALIDATION_REPORT_FILENAME,
)
DEFAULT_EMBEDDING_BATCH_SIZE = 64
EMBEDDING_BATCH_SIZE_ENV = "SENKU_INGEST_EMBED_BATCH_SIZE"
GUIDE_SUMMARY_FIELD_CHARS = 600
GUIDE_SUMMARY_HEADINGS_CHARS = 800
GUIDE_SUMMARY_BODY_EXCERPT_CHARS = 800


def create_lexical_index(path):
    """Create or open the lexical FTS shadow index used for hybrid retrieval."""
    conn = sqlite3.connect(path)
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA synchronous=NORMAL")
    conn.executescript(
        """
        CREATE TABLE IF NOT EXISTS lexical_chunk_meta (
            chunk_id TEXT PRIMARY KEY,
            document TEXT NOT NULL,
            source_file TEXT,
            guide_id TEXT,
            guide_title TEXT,
            slug TEXT,
            description TEXT,
            category TEXT,
            difficulty TEXT,
            last_updated TEXT,
            version TEXT,
            liability_level TEXT,
            tags TEXT,
            related TEXT,
            section_id TEXT,
            section_heading TEXT
        );

        CREATE INDEX IF NOT EXISTS lexical_chunk_meta_category_idx
            ON lexical_chunk_meta(category);
        CREATE INDEX IF NOT EXISTS lexical_chunk_meta_guide_id_idx
            ON lexical_chunk_meta(guide_id);
        CREATE INDEX IF NOT EXISTS lexical_chunk_meta_slug_idx
            ON lexical_chunk_meta(slug);

        CREATE VIRTUAL TABLE IF NOT EXISTS lexical_chunks_fts USING fts5(
            chunk_id UNINDEXED,
            search_text,
            guide_title,
            guide_id,
            section_heading,
            slug,
            tags,
            description,
            category,
            liability_level,
            tokenize='porter unicode61'
        );
        """
    )
    return conn


def clear_lexical_records(conn):
    """Clear all rows from the lexical index tables."""
    conn.execute("DELETE FROM lexical_chunk_meta")
    conn.execute("DELETE FROM lexical_chunks_fts")
    conn.commit()


def file_sha256(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        for block in iter(lambda: f.read(65536), b""):
            h.update(block)
    return h.hexdigest()


def load_ingest_manifest():
    if not os.path.isfile(MANIFEST_PATH):
        return {}
    try:
        with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
            return json.load(f)
    except (json.JSONDecodeError, OSError):
        return {}


def save_ingest_manifest(manifest):
    os.makedirs(os.path.dirname(MANIFEST_PATH), exist_ok=True)
    tmp = MANIFEST_PATH + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2, sort_keys=True)
    os.replace(tmp, MANIFEST_PATH)


def resolve_embedding_batch_size(value=None):
    """Return the embedding batch size requested by CLI/env."""
    raw_value = value
    if raw_value is None:
        raw_value = os.environ.get(EMBEDDING_BATCH_SIZE_ENV, DEFAULT_EMBEDDING_BATCH_SIZE)
    try:
        batch_size = int(raw_value)
    except (TypeError, ValueError):
        raise ValueError(
            f"Embedding batch size must be a positive integer: {raw_value!r}"
        )
    if batch_size < 1:
        raise ValueError(
            f"Embedding batch size must be a positive integer: {raw_value!r}"
        )
    return batch_size


def normalize_ingest_manifest(manifest, file_info_by_guide_id):
    """Return a manifest keyed by guide_id, migrating legacy basename keys when possible."""
    if not isinstance(manifest, dict):
        return {}

    normalized = {}
    legacy_by_sha = defaultdict(list)

    for key, entry in manifest.items():
        if not isinstance(entry, dict):
            entry = {"sha256": str(entry)}

        cleaned = dict(entry)
        cleaned["sha256"] = str(cleaned.get("sha256", ""))

        if key in file_info_by_guide_id:
            guide_id = key
            cleaned["guide_id"] = guide_id
            cleaned["source_file"] = file_info_by_guide_id[guide_id]["basename"]
            normalized[guide_id] = cleaned
            continue

        for guide_id, info in file_info_by_guide_id.items():
            if info["basename"] == key:
                cleaned["guide_id"] = guide_id
                cleaned["source_file"] = info["basename"]
                normalized[guide_id] = cleaned
                break
        else:
            sha = cleaned.get("sha256", "")
            if sha:
                legacy_by_sha[sha].append(cleaned)

    for guide_id, info in file_info_by_guide_id.items():
        if guide_id in normalized:
            continue
        sha = info.get("sha256", "")
        if not sha:
            continue
        matches = legacy_by_sha.get(sha)
        if not matches:
            continue
        cleaned = dict(matches.pop(0))
        cleaned["guide_id"] = guide_id
        cleaned["source_file"] = info["basename"]
        cleaned["sha256"] = sha
        normalized[guide_id] = cleaned

    return normalized


def collect_manifest_file_info(md_files):
    """Collect guide file metadata needed to preserve the ingest manifest."""
    file_info_by_guide_id = {}
    for filepath in md_files:
        try:
            with open(filepath, "r", encoding="utf-8") as f:
                meta, _body = parse_frontmatter(f.read())
        except OSError:
            continue
        if not isinstance(meta, dict):
            continue
        guide_id = str(meta.get("id", "")).strip()
        if not guide_id:
            continue
        file_info_by_guide_id[guide_id] = {
            "basename": os.path.basename(filepath),
            "path": filepath,
            "sha256": file_sha256(filepath),
        }
    return file_info_by_guide_id


def delete_lexical_records_by_guide_ids(conn, guide_ids):
    """Delete lexical rows for one or more guide IDs."""
    if not guide_ids:
        return

    placeholders = ",".join("?" * len(guide_ids))
    rows = conn.execute(
        f"SELECT chunk_id FROM lexical_chunk_meta WHERE guide_id IN ({placeholders})",
        guide_ids,
    ).fetchall()
    chunk_ids = [r[0] for r in rows]

    conn.execute(
        f"DELETE FROM lexical_chunk_meta WHERE guide_id IN ({placeholders})",
        guide_ids,
    )
    if chunk_ids:
        id_placeholders = ",".join("?" * len(chunk_ids))
        conn.execute(
            f"DELETE FROM lexical_chunks_fts WHERE chunk_id IN ({id_placeholders})",
            chunk_ids,
        )
    conn.commit()


def delete_collection_records_for_guide_ids(collection, guide_ids):
    """Delete Chroma records for one or more guide IDs."""
    for guide_id in guide_ids:
        result = collection.get(
            where={"guide_id": guide_id},
            limit=200000,
        )
        ids = result.get("ids") or []
        if ids:
            collection.delete(ids=ids)


def collect_markdown_files():
    """Collect markdown guides and return sorted absolute paths."""
    md_files = []
    for root, _, files in os.walk(config.COMPENDIUM_DIR):
        for name in files:
            if name.endswith(".md"):
                md_files.append(os.path.join(root, name))
    return sorted(md_files)


def normalize_selected_files(selected):
    """Resolve user-specified guide paths or basenames to absolute paths."""
    all_files = collect_markdown_files()
    basename_lookup = {os.path.basename(path): path for path in all_files}

    resolved = []
    for request in selected:
        candidate = os.path.normpath(request)
        if os.path.isabs(candidate) and os.path.isfile(candidate):
            resolved.append(os.path.abspath(candidate))
            continue
        if os.path.isfile(candidate):
            resolved.append(os.path.abspath(candidate))
            continue

        direct_compendium = os.path.join(config.COMPENDIUM_DIR, candidate)
        if os.path.isfile(direct_compendium):
            resolved.append(os.path.abspath(direct_compendium))
            continue

        by_basename = basename_lookup.get(os.path.basename(candidate))
        if by_basename is not None:
            resolved.append(by_basename)

    # Preserve order while removing duplicates
    unique = []
    seen = set()
    for path in resolved:
        if path not in seen:
            seen.add(path)
            unique.append(path)
    return unique


def make_chunk_id(chunk, index):
    """Create a stable, collision-resistant chunk id."""
    if isinstance(chunk, tuple):
        text = chunk[0] if len(chunk) > 0 else ""
        metadata = chunk[1] if len(chunk) > 1 and isinstance(chunk[1], dict) else {}
    else:
        text = chunk.get("text", "")
        metadata = chunk.get("metadata", {})
    source_file = metadata.get("source_file", "")
    section_id = metadata.get("section_id", "")
    section_heading = metadata.get("section_heading", "")
    raw = "|".join(
        [
            source_file,
            section_id,
            section_heading,
            str(index),
            text,
        ]
    )
    digest = hashlib.sha1(raw.encode("utf-8")).hexdigest()
    return f"chunk_{digest}"


def build_contextual_retrieval_text(chunk):
    """Build non-production retrieval text that preserves guide and section context."""
    text = str(chunk.get("text", "") if isinstance(chunk, dict) else "")
    metadata = chunk.get("metadata", {}) if isinstance(chunk, dict) else {}
    if not isinstance(metadata, dict):
        metadata = {}

    guide_bits = [
        metadata.get("guide_title", ""),
        metadata.get("guide_id", ""),
        metadata.get("slug", ""),
    ]
    guide_label = " | ".join(str(bit).strip() for bit in guide_bits if str(bit).strip())
    lines = []
    if guide_label:
        lines.append(f"Guide: {guide_label}")
    if metadata.get("description"):
        lines.append(f"Purpose: {metadata.get('description')}")
    if metadata.get("section_heading"):
        lines.append(f"Section: {metadata.get('section_heading')}")
    if metadata.get("category") or metadata.get("tags"):
        category_tags = ", ".join(
            str(bit).strip()
            for bit in (metadata.get("category", ""), metadata.get("tags", ""))
            if str(bit).strip()
        )
        lines.append(f"Category/tags: {category_tags}")
    if metadata.get("liability_level"):
        lines.append(f"Liability: {metadata.get('liability_level')}")
    if metadata.get("related"):
        lines.append(f"Related: {metadata.get('related')}")
    if metadata.get("aliases"):
        lines.append(f"Aliases: {metadata.get('aliases')}")
    if metadata.get("routing_cues"):
        lines.append(f"Routing cues: {metadata.get('routing_cues')}")
    if metadata.get("applicability"):
        lines.append(f"Applicability: {metadata.get('applicability')}")

    if lines:
        lines.append("")
    lines.append(text.strip())
    return "\n".join(lines).strip()


def _frontmatter_text(value):
    """Return a compact metadata string from scalar/list frontmatter values."""
    if value is None:
        return ""
    if isinstance(value, (list, tuple, set)):
        parts = []
        for item in value:
            if isinstance(item, dict):
                parts.extend(
                    str(part).strip()
                    for part in item.values()
                    if str(part).strip()
                )
            else:
                text = str(item).strip()
                if text:
                    parts.append(text)
        return ",".join(parts)
    if isinstance(value, dict):
        return ",".join(
            str(part).strip()
            for part in value.values()
            if str(part).strip()
        )
    return str(value).strip()


def _truncate_summary_text(value, limit):
    text = str(value or "").strip()
    if len(text) <= limit:
        return text
    return text[:limit].rstrip() + " ..."


def guide_summary_index_text(meta, body):
    """Build a compact one-vector guide summary for fast CI retrieval smoke checks."""
    lines = []
    for label, key in (
        ("Guide", "title"),
        ("ID", "id"),
        ("Slug", "slug"),
        ("Description", "description"),
        ("Category", "category"),
        ("Difficulty", "difficulty"),
        ("Liability", "liability_level"),
    ):
        value = _truncate_summary_text(
            _frontmatter_text(meta.get(key)),
            GUIDE_SUMMARY_FIELD_CHARS,
        )
        if value:
            lines.append(f"{label}: {value}")

    for label, key in (
        ("Tags", "tags"),
        ("Aliases", "aliases"),
        ("Routing cues", "routing_cues"),
        ("Applicability", "applicability"),
        ("Related", "related"),
    ):
        value = _truncate_summary_text(
            _frontmatter_text(meta.get(key)),
            GUIDE_SUMMARY_FIELD_CHARS,
        )
        if value:
            lines.append(f"{label}: {value}")

    headings = []
    for match in re.finditer(r"^#{2,3}\s+(.+)$", body, flags=re.MULTILINE):
        heading = match.group(1).strip()
        if heading:
            headings.append(heading)
    if headings:
        lines.append(
            "Headings: "
            + _truncate_summary_text(
                "; ".join(headings),
                GUIDE_SUMMARY_HEADINGS_CHARS,
            )
        )

    body_excerpt = clean_chunk_text(body)[:GUIDE_SUMMARY_BODY_EXCERPT_CHARS].strip()
    if body_excerpt:
        lines.extend(["", body_excerpt])
    return "\n".join(lines).strip()


def contextual_shadow_record(chunk, index):
    """Return one JSONL-safe contextual shadow export record."""
    metadata = dict(chunk.get("metadata", {}) if isinstance(chunk, dict) else {})
    document = str(chunk.get("text", "") if isinstance(chunk, dict) else "")
    return {
        "chunk_id": make_chunk_id(chunk, index),
        "document": document,
        "contextual_retrieval_text": build_contextual_retrieval_text(chunk),
        "metadata": metadata,
    }


def remove_contextual_shadow_jsonl(output_path):
    """Remove stale shadow output before a new shadow-only export starts."""
    try:
        os.remove(os.fspath(output_path))
    except FileNotFoundError:
        return


def write_contextual_shadow_jsonl(chunks, output_path):
    """Write contextual retrieval text without changing production ingest stores."""
    output_path = os.fspath(output_path)
    parent = os.path.dirname(output_path)
    if parent:
        os.makedirs(parent, exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as handle:
        for index, chunk in enumerate(chunks):
            json.dump(
                contextual_shadow_record(chunk, index),
                handle,
                ensure_ascii=False,
                sort_keys=True,
            )
            handle.write("\n")
    return len(chunks)


def export_contextual_shadow_jsonl(md_files, output_path):
    """Parse guides and write contextual shadow records without DB mutation."""
    remove_contextual_shadow_jsonl(output_path)
    all_chunks = []
    errors = []
    for filepath in md_files:
        try:
            meta, chunks = process_file(filepath)
            basename = os.path.basename(filepath)
            if meta is None:
                errors.append((basename, "Failed to parse frontmatter"))
                continue
            if not chunks:
                errors.append((basename, "Produced zero chunks"))
                continue
            all_chunks.extend(chunks)
        except Exception as exc:
            errors.append((os.path.basename(filepath), str(exc)))

    if errors:
        console.print("\n[bold red]Contextual shadow export failed.[/bold red]")
        for filename, reason in errors:
            console.print(f"  [red]{filename}:[/red] {reason}")
        raise SystemExit(1)

    count = write_contextual_shadow_jsonl(all_chunks, output_path)
    console.print(
        f"[green]Wrote {count} contextual shadow chunks to {output_path}[/green]"
    )
    return count


def add_lexical_records(conn, ids, texts, metadatas, search_texts=None):
    """Insert one batch into the lexical FTS shadow index."""
    if search_texts is None:
        search_texts = texts
    meta_rows = []
    fts_rows = []
    for chunk_id, text, meta, search_text in zip(ids, texts, metadatas, search_texts):
        meta_rows.append(
            (
                chunk_id,
                text,
                meta.get("source_file", ""),
                meta.get("guide_id", ""),
                meta.get("guide_title", ""),
                meta.get("slug", ""),
                meta.get("description", ""),
                meta.get("category", ""),
                meta.get("difficulty", ""),
                meta.get("last_updated", ""),
                meta.get("version", ""),
                meta.get("liability_level", ""),
                meta.get("tags", ""),
                meta.get("related", ""),
                meta.get("section_id", ""),
                meta.get("section_heading", ""),
            )
        )
        fts_rows.append(
            (
                chunk_id,
                search_text,
                meta.get("guide_title", ""),
                meta.get("guide_id", ""),
                meta.get("section_heading", ""),
                meta.get("slug", ""),
                meta.get("tags", ""),
                meta.get("description", ""),
                meta.get("category", ""),
                meta.get("liability_level", ""),
            )
        )

    conn.executemany(
        """
        INSERT INTO lexical_chunk_meta (
            chunk_id, document, source_file, guide_id, guide_title, slug, description,
            category, difficulty, last_updated, version, liability_level, tags, related,
            section_id, section_heading
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        meta_rows,
    )
    conn.executemany(
        """
        INSERT INTO lexical_chunks_fts (
            chunk_id, search_text, guide_title, guide_id, section_heading, slug,
            tags, description, category, liability_level
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        fts_rows,
    )
    conn.commit()


def parse_frontmatter(text):
    """Extract YAML frontmatter and body from markdown text."""
    text = text.lstrip("\ufeff")
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return None, text
    closing_index = None
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            closing_index = index
            break
    if closing_index is None:
        return None, text
    try:
        meta = yaml.safe_load("\n".join(lines[1:closing_index]))
    except yaml.YAMLError:
        return None, text
    if not isinstance(meta, dict):
        return None, text
    body = "\n".join(lines[closing_index + 1 :])
    return meta, body


def clean_chunk_text(text):
    """Strip HTML tags, SVG blocks, and admonition markers from chunk text."""
    # Strip <svg>...</svg> blocks (multiline)
    text = re.sub(r"<svg[\s\S]*?</svg>", "", text)
    # Strip common HTML tags while leaving arbitrary angle-bracketed examples intact.
    text = re.sub(
        r"</?(?:a|article|aside|blockquote|br|code|dd|div|dl|dt|em|figcaption|figure|h[1-6]|hr|i|img|li|nav|ol|p|pre|section|small|span|strong|sub|sup|table|tbody|td|tfoot|th|thead|tr|u|ul)\b[^>]*>",
        "",
        text,
        flags=re.IGNORECASE,
    )
    # Strip admonition markers (:::warning, :::note, :::info-box, etc.) but keep content
    text = re.sub(r"^:::\w[\w-]*\s*$", "", text, flags=re.MULTILINE)
    text = re.sub(r"^:::\s*$", "", text, flags=re.MULTILINE)
    # Collapse excessive blank lines
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def split_sections(body):
    """Split body on <section id="..."> / </section> boundaries.
    Returns list of (section_id, section_text) tuples.
    """
    pattern = r'<section\s+id="([^"]*)"[^>]*>'
    parts = re.split(pattern, body)
    # parts: [before_first_section, id1, content1, id2, content2, ...]
    sections = []
    i = 1
    while i < len(parts) - 1:
        section_id = parts[i]
        raw = parts[i + 1]
        # Remove the closing </section> tag
        raw = re.sub(r"</section>\s*$", "", raw.strip())
        sections.append((section_id, raw))
        i += 2
    if sections:
        return sections
    return split_markdown_sections(body)


def slugify_section_id(text):
    """Create a stable fallback section id from a heading."""
    lowered = re.sub(r"<[^>]+>", " ", text or "").lower()
    lowered = re.sub(r"[^a-z0-9]+", "-", lowered).strip("-")
    return lowered or "section"


def split_markdown_sections(body):
    """Fallback splitter for guides that use ordinary markdown headings.

    Many newer guides are plain markdown with `##` headings instead of explicit
    `<section id=\"...\">` wrappers. When that happens, chunk them by heading so
    they do not silently disappear from ingest.
    """
    normalized = (body or "").strip()
    if not normalized:
        return []

    heading_matches = list(re.finditer(r"^##\s+(.+)$", normalized, re.MULTILINE))
    if not heading_matches:
        return [("body", normalized)]

    sections = []
    intro = normalized[: heading_matches[0].start()].strip()
    if intro:
        sections.append(("intro", intro))

    seen_ids = set(section_id for section_id, _ in sections)
    for index, match in enumerate(heading_matches):
        start = match.start()
        end = (
            heading_matches[index + 1].start()
            if index + 1 < len(heading_matches)
            else len(normalized)
        )
        section_text = normalized[start:end].strip()
        if not section_text:
            continue
        base_id = slugify_section_id(match.group(1))
        section_id = base_id
        suffix = 2
        while section_id in seen_ids:
            section_id = f"{base_id}-{suffix}"
            suffix += 1
        seen_ids.add(section_id)
        sections.append((section_id, section_text))

    return sections


def extract_heading(text):
    """Extract the first ## or ### heading from text."""
    m = re.search(r"^(#{2,3})\s+(.+)$", text, re.MULTILINE)
    return m.group(2).strip() if m else ""


CHUNK_MAX_TOKENS = 200
OVERLAP_TOKENS = 50


def split_paragraphs_with_overlap(section_id, text, heading):
    """Split text at paragraph breaks with token overlap between chunks."""
    paragraphs = re.split(r"\n\n+", text)
    chunks = []
    current = []
    current_tokens = 0
    prefix = f"## {heading}\n\n" if heading else ""
    prefix_tokens = estimate_tokens(prefix)

    for para in paragraphs:
        para = para.strip()
        if not para:
            continue
        para_tokens = estimate_tokens(para)

        if current and current_tokens + para_tokens + prefix_tokens > CHUNK_MAX_TOKENS:
            chunk_text = prefix + "\n\n".join(current)
            chunks.append((section_id, chunk_text))

            # Build overlap from tail of current chunk
            overlap = []
            overlap_tok = 0
            for p in reversed(current):
                pt = estimate_tokens(p)
                if overlap_tok + pt > OVERLAP_TOKENS:
                    break
                overlap.insert(0, p)
                overlap_tok += pt
            current = overlap
            current_tokens = overlap_tok

        current.append(para)
        current_tokens += para_tokens

    if current:
        chunk_text = prefix + "\n\n".join(current)
        chunks.append((section_id, chunk_text))

    return chunks


def sub_split_on_h3(section_id, text, parent_heading):
    """Split a large section on ### headings, prepending the parent ## heading.
    If any sub-chunk still exceeds CHUNK_MAX_TOKENS, split at paragraph breaks."""
    parts = re.split(r"(?=^###\s)", text, flags=re.MULTILINE)
    chunks = []
    for part in parts:
        part = part.strip()
        if not part:
            continue
        # Prepend parent heading if chunk doesn't start with ##
        if parent_heading and not part.startswith("## "):
            part = f"## {parent_heading}\n\n{part}"

        if estimate_tokens(part) > CHUNK_MAX_TOKENS:
            heading = extract_heading(part)
            chunks.extend(split_paragraphs_with_overlap(section_id, part, heading))
        else:
            chunks.append((section_id, part))
    return chunks


def embed_batch(texts, model=None, base_url=None):
    """Call LM Studio embeddings endpoint for a batch of texts."""
    model = model or config.EMBED_MODEL
    models = embedding_models_to_try(model)
    lm_studio_url = normalize_lm_studio_url(
        base_url or getattr(config, "EMBED_URL", config.LM_STUDIO_URL)
    )
    url = f"{lm_studio_url}/embeddings"
    last_exc = None

    for index, candidate in enumerate(models):
        try:
            resp = requests.post(
                url, json={"input": texts, "model": candidate}, timeout=120
            )
            resp.raise_for_status()
            data = resp.json()["data"]
            return [d["embedding"] for d in sorted(data, key=lambda x: x["index"])]
        except requests.RequestException as exc:
            last_exc = exc
            if index == len(models) - 1 or not should_try_embedding_fallback(exc):
                raise

            console.print(
                f"[yellow]Embedding model {candidate} failed: {exc}. "
                f"Falling back to {models[index + 1]}...[/yellow]"
            )

    raise last_exc


def is_retryable_embedding_error(exc):
    """Return True for transient LM Studio embedding failures."""
    return is_retryable_lm_request(exc)


def embed_batch_with_retry(texts, batch_label, max_attempts=5):
    """Retry transient embedding failures before giving up on a batch."""
    delay = 2
    last_error = None

    for attempt in range(1, max_attempts + 1):
        try:
            return embed_batch(texts)
        except Exception as exc:
            last_error = exc
            if not is_retryable_embedding_error(exc) or attempt == max_attempts:
                raise

            console.print(
                f"[yellow]Embedding batch {batch_label} failed on attempt "
                f"{attempt}/{max_attempts}: {exc}. Retrying in {delay}s...[/yellow]"
            )
            time.sleep(delay)
            delay = min(delay * 2, 30)

    raise last_error


def get_collection(client):
    """Get the current collection handle by name.

    Chroma collection handles can become stale during long rebuilds, so reacquire
    by name instead of trusting a single object for the full ingest.
    """
    return client.get_collection(COLLECTION_NAME)


def add_records_with_retry(
    collection, client, ids, texts, embeddings, metadatas, batch_no, max_attempts=3
):
    """Add one batch to Chroma, refreshing the collection handle only after failure."""
    current_collection = collection
    delay = 1
    last_error = None

    for attempt in range(1, max_attempts + 1):
        try:
            current_collection.add(
                ids=ids,
                documents=texts,
                embeddings=embeddings,
                metadatas=metadatas,
            )
            return current_collection
        except chromadb.errors.NotFoundError as exc:
            last_error = exc
            if attempt == max_attempts:
                raise
            console.print(
                f"[yellow]Collection handle went stale during batch {batch_no}; "
                f"refreshing and retrying add ({attempt}/{max_attempts})...[/yellow]"
            )
            current_collection = get_collection(client)
            time.sleep(delay)
            delay = min(delay * 2, 5)

    raise last_error


def _bridge_tag_consistency_warnings(guide_records):
    """Return exact bridge-tag inconsistency warnings for ingest validation."""
    warnings = []
    for record in guide_records:
        guide_id = str(record.get("guide_id", "")).strip() or str(
            record.get("source_file", "")
        ).strip() or "<unknown>"
        bridge_enabled = bool(record.get("bridge"))
        tag_has_bridge_guide = "bridge-guide" in normalize_tags(record.get("tags", []))
        if bridge_enabled and tag_has_bridge_guide:
            warnings.append(
                f"bridge-tag inconsistency: {guide_id} has "
                f"bridge={bridge_enabled} and "
                f"tag_has_bridge_guide={tag_has_bridge_guide}"
            )
    return warnings


def process_file(filepath, *, guide_summary_index=False):
    """Parse a single markdown guide into chunks with metadata."""
    with open(filepath, "r", encoding="utf-8") as f:
        text = f.read()

    meta, body = parse_frontmatter(text)
    if not meta:
        return None, []

    guide_id = meta.get("id", "")
    guide_title = meta.get("title", "")
    slug = meta.get("slug", "")
    description = meta.get("description", "")
    category = meta.get("category", "")
    difficulty = meta.get("difficulty", "")
    last_updated = meta.get("last_updated", "")
    version = meta.get("version", "")
    liability_level = meta.get("liability_level", "")
    tags = meta.get("tags", [])
    bridge_metadata = derive_bridge_metadata(
        tags, explicit_bridge=meta.get("bridge", False)
    )
    normalized_tags = bridge_metadata["normalized_tags"]
    related = meta.get("related", [])
    related_str = ",".join(related) if isinstance(related, list) else str(related)
    aliases = _frontmatter_text(meta.get("aliases"))
    routing_cues = _frontmatter_text(meta.get("routing_cues"))
    applicability = _frontmatter_text(meta.get("applicability"))
    common_metadata = {
        "source_file": os.path.basename(filepath),
        "guide_id": str(guide_id),
        "guide_title": guide_title,
        "slug": str(slug or ""),
        "description": str(description or ""),
        "category": category,
        "difficulty": difficulty,
        "last_updated": str(last_updated or ""),
        "version": str(version or ""),
        "liability_level": str(liability_level or ""),
        "tags": ",".join(normalized_tags),
        "related": related_str,
        "aliases": aliases,
        "routing_cues": routing_cues,
        "applicability": applicability,
        "bridge": bridge_metadata["bridge"],
    }

    sections = split_sections(body)
    chunks = []

    if guide_summary_index:
        metadata = dict(common_metadata)
        metadata.update(
            {
                "section_id": "guide-summary",
                "section_heading": "Guide Summary",
            }
        )
        return meta, [
            {
                "text": guide_summary_index_text(meta, body),
                "metadata": metadata,
            }
        ]

    for section_id, section_text in sections:
        heading = extract_heading(section_text)
        cleaned = clean_chunk_text(section_text)

        if estimate_tokens(cleaned) > CHUNK_MAX_TOKENS:
            sub_chunks = sub_split_on_h3(section_id, cleaned, heading)
            for sub_section_id, sub_text in sub_chunks:
                sub_heading = extract_heading(sub_text)
                metadata = dict(common_metadata)
                metadata.update(
                    {
                        "section_id": sub_section_id,
                        "section_heading": sub_heading or heading,
                    }
                )
                chunks.append(
                    {
                        "text": sub_text,
                        "metadata": metadata,
                    }
                )
        else:
            metadata = dict(common_metadata)
            metadata.update(
                {
                    "section_id": section_id,
                    "section_heading": heading,
                }
            )
            chunks.append(
                {
                    "text": cleaned,
                    "metadata": metadata,
                }
            )

    return meta, chunks


def print_stats(collection):
    """Print collection statistics."""
    count = collection.count()
    console.print(f"\n[bold]Collection:[/bold] {collection.name}")
    console.print(f"[bold]Total chunks:[/bold] {count}")

    if count == 0:
        return

    # Sample metadata to get category distribution in bounded pages to avoid
    # oversized SQL variable lists from full-collection scans.
    page_size = 500
    cats = defaultdict(int)
    offset = 0
    while True:
        page = collection.get(include=["metadatas"], limit=page_size, offset=offset)
        metadatas = page.get("metadatas") or []
        for m in metadatas:
            cats[m.get("category", "unknown")] += 1

        if len(metadatas) < page_size:
            break
        offset += page_size

    table = Table(title="Chunks per Category")
    table.add_column("Category", style="cyan")
    table.add_column("Chunks", justify="right", style="green")
    for cat, n in sorted(cats.items(), key=lambda x: -x[1]):
        table.add_row(cat, str(n))
    console.print(table)


def main():
    parser = argparse.ArgumentParser(description="Ingest guides into ChromaDB")
    parser.add_argument("--rebuild", action="store_true", help="Force full re-index")
    parser.add_argument("--stats", action="store_true", help="Print stats and exit")
    parser.add_argument(
        "--files",
        nargs="+",
        help=(
            "Re-ingest one or more guide files by path or basename. "
            "Existing collection must exist unless using --rebuild."
        ),
    )
    parser.add_argument(
        "--force-files",
        action="store_true",
        help="With --files, re-embed selected guides even if their manifest SHA is unchanged.",
    )
    parser.add_argument(
        "--contextual-shadow-jsonl",
        help=(
            "Write non-production contextual retrieval text records to this JSONL "
            "path while preserving raw chunk documents."
        ),
    )
    parser.add_argument(
        "--contextual-shadow-only",
        action="store_true",
        help=(
            "Only write --contextual-shadow-jsonl and exit before Chroma, "
            "embedding, lexical, or manifest work."
        ),
    )
    parser.add_argument(
        "--embedding-batch-size",
        type=int,
        default=None,
        help=(
            "Embedding request batch size. Defaults to "
            f"${EMBEDDING_BATCH_SIZE_ENV} or {DEFAULT_EMBEDDING_BATCH_SIZE}."
        ),
    )
    parser.add_argument(
        "--guide-summary-index",
        action="store_true",
        help=(
            "Index one compact guide-summary chunk per guide. Intended for "
            "fast CI retrieval smoke indexes, not production-quality retrieval."
        ),
    )
    args = parser.parse_args()

    if args.force_files and not args.files:
        parser.error("--force-files requires --files")

    if args.contextual_shadow_only and not args.contextual_shadow_jsonl:
        parser.error("--contextual-shadow-only requires --contextual-shadow-jsonl")

    if args.guide_summary_index and not args.rebuild:
        parser.error("--guide-summary-index requires --rebuild")

    all_md_files = collect_markdown_files()
    selected_md_files = (
        normalize_selected_files(args.files) if args.files else all_md_files
    )

    if args.files:
        requested_basenames = {os.path.basename(req) for req in args.files}
        resolved_basenames = {os.path.basename(path) for path in selected_md_files}
        missing = sorted(requested_basenames - resolved_basenames)
        if any(missing):
            for item in missing:
                console.print(f"[red]Could not resolve guide file: {item}[/red]")
            sys.exit(1)

    if args.contextual_shadow_only:
        export_contextual_shadow_jsonl(selected_md_files, args.contextual_shadow_jsonl)
        return

    if args.rebuild and not args.stats and os.path.isdir(config.CHROMA_DB_DIR):
        shutil.rmtree(config.CHROMA_DB_DIR)
    os.makedirs(config.CHROMA_DB_DIR, exist_ok=True)

    client = chromadb.PersistentClient(path=config.CHROMA_DB_DIR)

    if args.stats:
        try:
            collection = get_collection(client)
        except Exception:
            console.print("[red]No collection found. Run ingest first.[/red]")
            sys.exit(1)
        print_stats(collection)
        return

    # Check if DB already exists
    existing = [c.name for c in client.list_collections()]
    if COLLECTION_NAME in existing and not args.rebuild and not args.files:
        console.print(
            "[yellow]Database already exists. Use --rebuild to re-index, "
            "or --stats to view collection info.[/yellow]"
        )
        return

    if args.files and not args.rebuild and COLLECTION_NAME not in existing:
        console.print(
            "[red]No collection found for incremental mode. "
            "Use --rebuild for a full index first.[/red]"
        )
        sys.exit(1)

    # Test embedding endpoint connectivity
    embed_url = getattr(config, "EMBED_URL", config.LM_STUDIO_URL)
    try:
        requests.get(f"{embed_url}/models", timeout=5)
    except requests.ConnectionError:
        console.print(
            f"[red]Cannot connect to embedding endpoint at {embed_url}. "
            "Make sure the embedding-capable LM Studio server is running.[/red]"
        )
        sys.exit(1)

    # Delete existing collection only for full rebuilds
    if args.rebuild and COLLECTION_NAME in existing:
        client.delete_collection(COLLECTION_NAME)

    if args.rebuild or COLLECTION_NAME not in existing:
        client.create_collection(
            name=COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"},
        )

    collection = get_collection(client)
    lexical_conn = create_lexical_index(config.LEXICAL_DB_PATH)
    if args.rebuild:
        clear_lexical_records(lexical_conn)

    incremental_mode = args.files and not args.rebuild
    manifest = load_ingest_manifest() if incremental_mode else {}

    if args.files:
        md_files = selected_md_files
    else:
        md_files = all_md_files
    total_chunks = 0
    errors = []
    validation_errors = []
    all_chunks = []
    valid_files = set()
    selected_guide_ids = defaultdict(list)
    file_info_by_guide_id = {}
    guide_validation_records = []

    console.print(f"\n[bold]Processing {len(md_files)} guide files...[/bold]\n")

    for filepath in md_files:
        try:
            meta, chunks = process_file(
                filepath,
                guide_summary_index=args.guide_summary_index,
            )
            basename = os.path.basename(filepath)
            if meta is None:
                errors.append(
                    (os.path.basename(filepath), "Failed to parse frontmatter")
                )
                continue
            guide_validation_records.append(
                {
                    "guide_id": meta.get("id", ""),
                    "slug": meta.get("slug", ""),
                    "title": meta.get("title", ""),
                    "category": meta.get("category", ""),
                    "description": meta.get("description", ""),
                    "source_file": basename,
                    "bridge": derive_bridge_metadata(
                        meta.get("tags", []),
                        explicit_bridge=meta.get("bridge", False),
                    )["bridge"],
                    "tags": meta.get("tags", []),
                }
            )
            if not chunks:
                validation_errors.append((basename, "Produced zero chunks"))
                continue
            guide_id = str(meta.get("id", "")).strip()
            selected_guide_ids[guide_id].append(basename)
            file_info_by_guide_id[guide_id] = {
                "basename": basename,
                "path": filepath,
                "sha256": file_sha256(filepath),
            }
            all_chunks.extend(chunks)
            valid_files.add(guide_id)
        except Exception as e:
            errors.append((os.path.basename(filepath), str(e)))

    basename_counts = defaultdict(int)
    for path in all_md_files:
        basename_counts[os.path.basename(path)] += 1
    duplicate_basenames = sorted(
        basename for basename, count in basename_counts.items() if count > 1
    )
    if duplicate_basenames:
        if incremental_mode:
            console.print(
                "[red]Duplicate basenames block incremental ingest: "
                f"{', '.join(duplicate_basenames)}[/red]"
            )
            console.print(
                "[red]Use --rebuild until guide filenames are unique.[/red]"
            )
            raise SystemExit(1)
        console.print(
            "[yellow]Duplicate basenames detected during rebuild: "
            f"{', '.join(duplicate_basenames)}[/yellow]"
        )

    manifest_file_info = (
        collect_manifest_file_info(all_md_files)
        if incremental_mode
        else file_info_by_guide_id
    )
    manifest = normalize_ingest_manifest(manifest, manifest_file_info)

    metadata_report = validate_guide_records(
        guide_validation_records,
        scope="ingest",
    )
    for warning in _bridge_tag_consistency_warnings(guide_validation_records):
        console.print(f"[yellow]{warning}[/yellow]")
    write_validation_report(metadata_report, METADATA_VALIDATION_REPORT_PATH)
    if report_has_errors(metadata_report):
        console.print("\n[bold red]Ingest metadata validation failed.[/bold red]")
        console.print(f"[red]{format_validation_errors(metadata_report)}[/red]")
        raise SystemExit(1)

    if incremental_mode:
        for guide_id, basenames in selected_guide_ids.items():
            if guide_id and len(basenames) > 1:
                validation_errors.append(
                    (
                        ", ".join(sorted(basenames)),
                        f"Duplicate guide id in --files set: {guide_id}",
                    )
                )
        try:
            from guide_catalog import all_guide_ids

            all_guide_ids(config.COMPENDIUM_DIR)
        except Exception as e:
            validation_errors.append(
                ("guide_catalog", f"Duplicate-id catalog validation failed: {e}")
            )
        if errors or validation_errors:
            combined_errors = errors + validation_errors
            console.print(
                "\n[bold red]Incremental ingest aborted before DB mutation.[/bold red]"
            )
            console.print("[red]Preflight validation errors:[/red]")
            for filename, reason in combined_errors:
                console.print(f"  {filename}: {reason}")
            raise SystemExit(1)
        changed = set()
        skipped = set()
        for guide_id in sorted(valid_files):
            info = file_info_by_guide_id.get(guide_id)
            if info is None:
                changed.add(guide_id)
                continue
            if args.force_files:
                changed.add(guide_id)
                continue
            if manifest.get(guide_id, {}).get("sha256") == info["sha256"]:
                skipped.add(guide_id)
            else:
                changed.add(guide_id)
        if skipped:
            console.print(
                f"  [dim]Skipping {len(skipped)} unchanged: "
                f"{', '.join(sorted(skipped))}[/dim]"
            )
        all_chunks = [c for c in all_chunks if c["metadata"]["guide_id"] in changed]
        valid_files = changed
        delete_collection_records_for_guide_ids(
            collection,
            list(dict.fromkeys(sorted(valid_files))),
        )
        delete_lexical_records_by_guide_ids(lexical_conn, list(valid_files))

    console.print(f"Parsed {len(md_files)} files -> {len(all_chunks)} chunks")

    if args.contextual_shadow_jsonl:
        write_contextual_shadow_jsonl(all_chunks, args.contextual_shadow_jsonl)
        console.print(
            f"[green]Wrote {len(all_chunks)} contextual shadow chunks to "
            f"{args.contextual_shadow_jsonl}[/green]"
        )

    embedded_guide_ids = set()
    expected_chunks_by_guide_id = defaultdict(int)
    embedded_chunks_by_guide_id = defaultdict(int)
    failed_guide_ids = set()
    for chunk in all_chunks:
        guide_id = chunk["metadata"].get("guide_id", "")
        if guide_id:
            expected_chunks_by_guide_id[guide_id] += 1

    # Embed and store in batches
    try:
        batch_size = resolve_embedding_batch_size(args.embedding_batch_size)
    except ValueError as exc:
        console.print(f"[red]{exc}[/red]")
        raise SystemExit(1)
    total_batches = (len(all_chunks) + batch_size - 1) // batch_size
    for i in range(0, len(all_chunks), batch_size):
        batch = all_chunks[i : i + batch_size]
        texts = [c["text"] for c in batch]
        retrieval_texts = [build_contextual_retrieval_text(c) for c in batch]
        metadatas = [c["metadata"] for c in batch]
        batch_no = i // batch_size + 1
        console.print(f"  Embedding batch {batch_no}/{total_batches} ({len(batch)} chunks)")

        try:
            embeddings = embed_batch_with_retry(retrieval_texts, batch_no)
            batch_records = [
                (text, metadata, embedding, i + j, retrieval_text)
                for j, (text, metadata, embedding, retrieval_text) in enumerate(
                    zip(texts, metadatas, embeddings, retrieval_texts)
                )
            ]
        except Exception as e:
            console.print(
                f"\n[yellow]Batch {batch_no} failed after retries; "
                "falling back to per-chunk embedding.[/yellow]"
            )
            batch_records = []
            for j, chunk in enumerate(batch):
                try:
                    retrieval_text = build_contextual_retrieval_text(chunk)
                    single_embedding = embed_batch_with_retry(
                        [retrieval_text],
                        f"{batch_no}.{j + 1}",
                    )[0]
                    batch_records.append(
                        (
                            chunk["text"],
                            chunk["metadata"],
                            single_embedding,
                            i + j,
                            retrieval_text,
                        )
                    )
                except Exception as item_error:
                    guide_id = chunk["metadata"].get("guide_id", "unknown")
                    source_file = chunk["metadata"].get("source_file", "unknown")
                    errors.append((f"{source_file}:{guide_id}", str(item_error)))
                    if guide_id and guide_id != "unknown":
                        failed_guide_ids.add(guide_id)
                    console.print(
                        f"[red]Skipping chunk {guide_id} in batch {batch_no} after retries: "
                        f"{item_error}[/red]"
                    )

            if not batch_records:
                console.print(
                    f"[red]Batch {batch_no} produced no embeddable chunks after per-chunk retries.[/red]"
                )
                continue

        texts = [record[0] for record in batch_records]
        metadatas = [record[1] for record in batch_records]
        embeddings = [record[2] for record in batch_records]
        retrieval_texts = [record[4] for record in batch_records]

        ids = [make_chunk_id(record, record[3]) for record in batch_records]
        collection = add_records_with_retry(
            collection,
            client,
            ids,
            texts,
            embeddings,
            metadatas,
            batch_no,
        )
        add_lexical_records(lexical_conn, ids, texts, metadatas, retrieval_texts)
        total_chunks += len(batch_records)
        for _rec in batch_records:
            guide_id = _rec[1].get("guide_id", "")
            if guide_id:
                embedded_guide_ids.add(guide_id)
                embedded_chunks_by_guide_id[guide_id] += 1
        console.print(
            f"  Embedded batch {batch_no}"
            f"/{total_batches}"
            f" ({total_chunks}/{len(all_chunks)} chunks)"
        )

    if embedded_chunks_by_guide_id:
        for guide_id in sorted(embedded_guide_ids):
            info = file_info_by_guide_id.get(guide_id)
            if not info:
                continue
            if failed_guide_ids and guide_id in failed_guide_ids:
                continue
            if embedded_chunks_by_guide_id.get(guide_id, 0) != expected_chunks_by_guide_id.get(
                guide_id, 0
            ):
                continue
            manifest[guide_id] = {
                "sha256": info["sha256"],
                "source_file": info["basename"],
                "guide_id": guide_id,
            }
        save_ingest_manifest(manifest)

    # Summary
    console.print(f"\n[bold green]Done![/bold green]")
    console.print(f"  Files processed: {len(md_files)}")
    console.print(f"  Total chunks: {total_chunks}")

    if errors:
        console.print(f"\n[bold red]Parse errors ({len(errors)}):[/bold red]")
        for fname, err in errors:
            console.print(f"  [red]{fname}:[/red] {err}")

    lexical_conn.close()
    print_stats(get_collection(client))


if __name__ == "__main__":
    main()

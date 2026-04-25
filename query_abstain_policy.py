"""Weak-retrieval abstain scoring helpers for query/bench paths."""

from __future__ import annotations

import re


ABSTAIN_ROW_LIMIT = 3
ABSTAIN_MAX_OVERLAP_TOKENS = 1
ABSTAIN_MIN_VECTOR_SIMILARITY = 0.67
ABSTAIN_MIN_UNIQUE_LEXICAL_HITS = 2


def _abstain_top_rows(results, *, limit=ABSTAIN_ROW_LIMIT):
    """Return the top reranked rows used for the weak-retrieval abstain check."""
    documents = (results or {}).get("documents", [[]])[0]
    metadatas = (results or {}).get("metadatas", [[]])[0]
    distances = (results or {}).get("distances", [[]])[0]
    rows = []
    for index, (doc, meta) in enumerate(zip(documents, metadatas)):
        dist = distances[index] if index < len(distances) else 1.0
        rows.append((doc or "", meta or {}, dist))
        if len(rows) >= limit:
            break
    return rows


def _abstain_row_overlap_tokens(query_tokens, doc, meta, *, content_tokens):
    """Return query-bearing tokens supported by one retrieved row."""
    haystack = " ".join(
        [
            meta.get("guide_title", ""),
            meta.get("section_heading", ""),
            meta.get("description", ""),
            meta.get("tags", ""),
            meta.get("category", ""),
            doc or "",
        ]
    )
    return query_tokens & content_tokens(haystack)


def _abstain_row_vector_similarity(meta, dist):
    """Approximate vector similarity from the preserved reranked distance."""
    if not meta.get("_vector_hits"):
        return 0.0
    try:
        similarity = 1.0 - float(dist)
    except (TypeError, ValueError):
        return 0.0
    return max(0.0, min(1.0, similarity))


def _abstain_match_label(
    overlap_count,
    vector_similarity,
    lexical_hits,
    *,
    min_vector_similarity=ABSTAIN_MIN_VECTOR_SIMILARITY,
):
    """Return the display label for an adjacent retrieved guide."""
    if overlap_count >= 2 or vector_similarity >= min_vector_similarity:
        return "moderate match"
    if overlap_count >= 1 or vector_similarity >= 0.45 or lexical_hits:
        return "low match"
    return "off-topic candidate"


def _should_abstain(
    results,
    query,
    *,
    content_tokens,
    row_limit=ABSTAIN_ROW_LIMIT,
    max_overlap_tokens=ABSTAIN_MAX_OVERLAP_TOKENS,
    min_vector_similarity=ABSTAIN_MIN_VECTOR_SIMILARITY,
    min_unique_lexical_hits=ABSTAIN_MIN_UNIQUE_LEXICAL_HITS,
):
    """Return whether weak retrieval should bypass generation plus row labels."""
    rows = _abstain_top_rows(results, limit=row_limit)
    query_tokens = content_tokens(query or "")
    if not rows or not query_tokens:
        return False, []

    max_overlap = 0
    max_vector_similarity = 0.0
    unique_lexical_hits = set()
    match_labels = []

    for doc, meta, dist in rows:
        overlap_tokens = _abstain_row_overlap_tokens(
            query_tokens,
            doc,
            meta,
            content_tokens=content_tokens,
        )
        overlap_count = len(overlap_tokens)
        vector_similarity = _abstain_row_vector_similarity(meta, dist)
        lexical_hits = int(meta.get("_lexical_hits", 0) or 0)

        max_overlap = max(max_overlap, overlap_count)
        max_vector_similarity = max(max_vector_similarity, vector_similarity)
        unique_lexical_hits.update(overlap_tokens)
        match_labels.append(
            _abstain_match_label(
                overlap_count,
                vector_similarity,
                lexical_hits,
                min_vector_similarity=min_vector_similarity,
            )
        )

    should_abstain = (
        max_overlap <= max_overlap_tokens
        and max_vector_similarity < min_vector_similarity
        and len(unique_lexical_hits) < min_unique_lexical_hits
    )
    return should_abstain, match_labels


def _truncate_abstain_query(query, *, limit=60):
    """Return a stable, UI-safe copy of the user query for abstain text."""
    normalized = re.sub(r"\s+", " ", (query or "").strip())
    if len(normalized) <= limit:
        return normalized
    clipped = normalized[: max(limit - 3, 0)].rstrip()
    return f"{clipped}..."

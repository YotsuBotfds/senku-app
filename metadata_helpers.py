"""Shared metadata normalization helpers used by ingest and retrieval.

The canonical helper is ``normalize_metadata_tag()``. It is a strict
superset of the earlier query and ingest normalizers: it preserves the same
slug-style output while also folding accented unicode text to ASCII for more
stable tag comparisons.
"""

from __future__ import annotations

import re
import unicodedata
from collections.abc import Mapping

BRIDGE_GUIDE_TAG = "bridge-guide"


def normalize_metadata_tag(value: str) -> str:
    """Normalize a frontmatter tag or metadata flag for routing checks."""
    normalized = unicodedata.normalize("NFKD", str(value or "").strip().lower())
    ascii_only = normalized.encode("ascii", "ignore").decode("ascii")
    return re.sub(r"[^a-z0-9]+", "-", ascii_only).strip("-")


def normalize_tag_value(tag):
    """Backward-compatible wrapper for older imports."""
    return normalize_metadata_tag(tag)


def normalize_tags(tags):
    """Return de-duplicated normalized tags from list or CSV-ish metadata."""
    if isinstance(tags, str):
        raw_tags = re.split(r"[\r\n,]+", tags)
    elif isinstance(tags, (list, tuple, set)):
        raw_tags = tags
    else:
        raw_tags = []

    normalized = []
    seen = set()
    for tag in raw_tags:
        if isinstance(tag, (Mapping, list, tuple, set)):
            continue
        normalized_tag = normalize_metadata_tag(tag)
        if not normalized_tag or normalized_tag in seen:
            continue
        seen.add(normalized_tag)
        normalized.append(normalized_tag)
    return normalized


def derive_bridge_metadata(tags, explicit_bridge=False):
    """Return canonical bridge metadata plus a stale-tag consistency signal."""
    normalized_tags = normalize_tags(tags)
    has_bridge_tag = BRIDGE_GUIDE_TAG in normalized_tags
    bridge_enabled = bool(explicit_bridge) or has_bridge_tag
    filtered_tags = [tag for tag in normalized_tags if tag != BRIDGE_GUIDE_TAG]
    inconsistent = bridge_enabled and BRIDGE_GUIDE_TAG in filtered_tags
    return {
        "bridge": bridge_enabled,
        "normalized_tags": filtered_tags,
        "had_bridge_tag": has_bridge_tag,
        "inconsistent": inconsistent,
    }

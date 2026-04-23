"""Guide catalog helpers for validating citations and resolving guide metadata."""

from dataclasses import dataclass
from functools import lru_cache
from pathlib import Path

import yaml

import config


@dataclass(frozen=True)
class GuideReference:
    """Minimal metadata needed for citation validation and future slug-based lookup."""

    guide_id: str
    slug: str
    title: str
    source_file: str
    related_refs: tuple[str, ...] = ()


def _normalize_related_refs(value):
    """Return normalized related-guide references from frontmatter."""
    if isinstance(value, str):
        raw_values = value.split(",")
    elif isinstance(value, (list, tuple, set)):
        raw_values = value
    else:
        return ()

    refs = []
    for raw_value in raw_values:
        normalized = str(raw_value or "").strip()
        if normalized:
            refs.append(normalized)
    return tuple(refs)


def _parse_frontmatter(path):
    """Parse YAML frontmatter from a guide file."""
    text = path.read_text(encoding="utf-8-sig")
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return {}
    closing_index = None
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            closing_index = index
            break
    if closing_index is None:
        return {}
    parsed = yaml.safe_load("\n".join(lines[1:closing_index])) or {}
    return parsed if isinstance(parsed, dict) else {}


@lru_cache(maxsize=4)
def load_guide_catalog(guides_dir=None):
    """Load the current guide catalog keyed by both id and slug."""
    base_dir = Path(guides_dir or config.COMPENDIUM_DIR)
    by_id = {}
    by_slug = {}

    for path in sorted(base_dir.glob("*.md")):
        meta = _parse_frontmatter(path)
        guide_id = str(meta.get("id", "")).strip()
        slug = str(meta.get("slug", "")).strip()
        title = str(meta.get("title", "")).strip()
        if not guide_id or not slug:
            continue

        reference = GuideReference(
            guide_id=guide_id,
            slug=slug,
            title=title,
            source_file=path.name,
            related_refs=_normalize_related_refs(meta.get("related")),
        )
        if guide_id in by_id:
            previous = by_id[guide_id]
            raise ValueError(
                f"Duplicate guide id {guide_id!r} in {path.name}; already defined by {previous.source_file}"
            )
        if slug in by_slug:
            previous = by_slug[slug]
            raise ValueError(
                f"Duplicate guide slug {slug!r} in {path.name}; already defined by {previous.source_file}"
            )
        by_id[guide_id] = reference
        by_slug[slug] = reference

    return by_id, by_slug


@lru_cache(maxsize=4)
def _load_related_link_weights(guides_dir=None):
    """Load directed related-guide weights keyed by anchor guide id."""
    by_id, by_slug = load_guide_catalog(guides_dir)
    related_by_guide = {guide_id: set() for guide_id in by_id}

    for guide_id, reference in by_id.items():
        for raw_ref in reference.related_refs:
            target = (
                by_slug.get(raw_ref)
                or by_slug.get(raw_ref.lower())
                or by_id.get(raw_ref)
                or by_id.get(raw_ref.upper())
            )
            if target is not None and target.guide_id != guide_id:
                related_by_guide[guide_id].add(target.guide_id)

    reciprocal_by_guide = {guide_id: set() for guide_id in by_id}
    weighted_by_guide = {guide_id: {} for guide_id in by_id}
    for guide_id, related_ids in related_by_guide.items():
        for related_id in related_ids:
            if guide_id in related_by_guide.get(related_id, set()):
                reciprocal_by_guide[guide_id].add(related_id)
                weighted_by_guide[guide_id][related_id] = 0.5
            else:
                weighted_by_guide[guide_id][related_id] = 0.3

    return reciprocal_by_guide, weighted_by_guide


def get_guide_reference_by_id(guide_id, guides_dir=None):
    """Return one guide reference by canonical guide id."""
    by_id, _ = load_guide_catalog(guides_dir)
    return by_id.get(guide_id)


def get_guide_reference_by_slug(slug, guides_dir=None):
    """Return one guide reference by slug."""
    _, by_slug = load_guide_catalog(guides_dir)
    return by_slug.get(slug)


def all_guide_ids(guides_dir=None):
    """Return the set of currently known guide ids."""
    by_id, _ = load_guide_catalog(guides_dir)
    return set(by_id)


def get_reciprocal_links(guide_id, guides_dir=None):
    """Return reciprocal related-guide ids for the given guide."""
    reciprocal_by_guide, _ = _load_related_link_weights(guides_dir)
    return set(reciprocal_by_guide.get(guide_id, set()))


def get_anchor_related_link_weights(guide_id, guides_dir=None):
    """Return related-guide weights for thread-anchor priors."""
    _, weighted_by_guide = _load_related_link_weights(guides_dir)
    return dict(weighted_by_guide.get(guide_id, {}))

"""Post-generation response and citation normalization helpers.

This module intentionally does not import query.py. Query-specific callers can
inject catalog and logging hooks while keeping the legacy query.* helper names
patchable.
"""

from collections import Counter
import re


MAX_INLINE_CITATIONS_PER_LINE = 2
MAX_INLINE_CITATIONS_PER_STEP_LINE = 1
_WARNING_RESIDUAL_BRACKET_PATTERN = re.compile(r"\[([^\[\]\n]{1,80})\]")
_WARNING_RESIDUAL_CITATION_PATTERN = re.compile(
    r"^(?:GD[-/]\d{1,3})(?:\s*,\s*GD[-/]\d{1,3})*$",
    re.IGNORECASE,
)
_WARNING_RESIDUAL_PREFIXES = (
    "instructional mandate",
    "instructional constraint",
    "instructional warning",
    "instructional advisory",
    "instructional note",
    "system instruction",
    "system warning",
    "system advisory",
    "control instruction",
    "control warning",
    "safety instruction",
    "safety mandate",
    "safety advisory",
    "safety note",
    "safety warning",
    "safety constraint",
)
_WARNING_RESIDUAL_EXACT_LABELS = {
    "warning",
    "caution",
    "advisory",
    "instruction",
}
_WARNING_RESIDUAL_TRAIL_MARKERS = (
    "implied",
    "label",
    "labels",
    "residue",
    "hazard",
    "hazards",
    "risk",
    "risks",
    "process",
    "processes",
)

_FORBIDDEN_RESPONSE_PHRASE_REPLACEMENTS = (
    ("the provided notes", "the guides"),
    ("the retrieved notes", "the guides"),
    ("the supplied context", "the guide support"),
    ("based on the provided information", "based on the guides"),
)


def _fix_mojibake(text):
    """Fix double-encoded UTF-8 produced by the LLM."""
    try:
        return text.encode("latin-1").decode("utf-8")
    except (UnicodeDecodeError, UnicodeEncodeError):
        return re.sub(
            r"\u00c2([\u00a0-\u00bf])",
            lambda m: m.group(1),
            text,
        )


def _extract_gd_ids(text):
    """Extract recoverable GD citation IDs from noisy model output."""
    citations = []
    seen = set()
    patterns = re.findall(r"GD[-/]\d{1,3}(?:\s*[/,]\s*(?:GD[-/])?\d{1,3})*", text or "")
    for raw_group in patterns:
        for digits in re.findall(r"\d{1,3}", raw_group):
            normalized = f"GD-{int(digits):03d}"
            if normalized in seen:
                continue
            seen.add(normalized)
            citations.append(normalized)
    return citations


def _normalize_citation_group(match):
    """Deduplicate and normalize a single bracketed GD citation group."""
    citations = _extract_gd_ids(match.group(0))
    if not citations:
        return ""
    return "[" + ", ".join(citations) + "]"


def _compress_citations_on_line(line):
    """Collapse repeated citation groups on one output line into one small cluster."""
    citation_groups = re.findall(r"\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\]", line)
    if not citation_groups:
        return line

    citations = []
    seen = set()
    for group in citation_groups:
        for citation in re.findall(r"GD-\d{3}", group):
            if citation in seen:
                continue
            seen.add(citation)
            citations.append(citation)

    max_citations = MAX_INLINE_CITATIONS_PER_LINE
    if re.match(r"^\s*(?:\d+\.\s+|[-*]\s+)", line):
        max_citations = MAX_INLINE_CITATIONS_PER_STEP_LINE

    if len(citation_groups) == 1 and len(citations) <= max_citations:
        return line

    body = re.sub(r"\s*\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\]", "", line).rstrip()
    body = re.sub(r"\s+([,.;:!?])", r"\1", body)
    if not body:
        return "[" + ", ".join(citations[:max_citations]) + "]"

    clipped = citations[:max_citations]
    return f"{body} [" + ", ".join(clipped) + "]"


def _rewrite_line_citations(line, citations):
    """Rewrite one line with a normalized set of inline citations."""
    body = re.sub(r"\s*\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\]", "", line).rstrip()
    body = re.sub(r"\s+([,.;:!?])", r"\1", body)
    if not citations:
        return body
    if not body:
        return "[" + ", ".join(citations) + "]"
    return f"{body} [" + ", ".join(citations) + "]"


def _compress_citations_across_numbered_steps(text):
    """Remove repeated citations inside one numbered-step block while keeping new sources."""
    lines = text.splitlines()
    if not lines:
        return text

    output = []
    in_step = False
    step_seen = set()

    for line in lines:
        if re.match(r"^\s*\d+\.\s+", line) or re.match(r"^\s*#{1,6}\s+\d+\.\s+", line):
            in_step = True
            step_seen = set(_extract_gd_ids(line))
            output.append(line)
            continue

        if in_step and not line.strip():
            in_step = False
            step_seen = set()
            output.append(line)
            continue

        if in_step and re.match(r"^\s*#{1,6}\s+", line):
            in_step = False
            step_seen = set()

        if in_step:
            citations = _extract_gd_ids(line)
            if citations:
                keep = [citation for citation in citations if citation not in step_seen]
                step_seen.update(citations)
                if keep != citations:
                    output.append(_rewrite_line_citations(line, keep))
                    continue

        output.append(line)

    return "\n".join(output)


def _match_replacement_case(source, replacement):
    if source.isupper():
        return replacement.upper()
    if source[:1].isupper():
        return replacement[:1].upper() + replacement[1:]
    return replacement


def _scrub_retrieval_mechanism_language(text):
    """Rewrite retrieval-mechanism phrasing into guide-facing language."""
    cleaned = text
    for phrase, replacement in _FORBIDDEN_RESPONSE_PHRASE_REPLACEMENTS:
        cleaned = re.sub(
            re.escape(phrase),
            lambda match: _match_replacement_case(match.group(0), replacement),
            cleaned,
            flags=re.IGNORECASE,
        )
    return cleaned


def _is_warning_residual_bracket(label):
    """Return True for bracketed control/warning residue that is not a citation."""
    normalized = re.sub(r"\s+", " ", (label or "").strip()).lower()
    if not normalized:
        return False
    if _WARNING_RESIDUAL_CITATION_PATTERN.fullmatch(normalized):
        return False
    if "gd-" in normalized or "gd/" in normalized:
        return False
    if normalized in _WARNING_RESIDUAL_EXACT_LABELS:
        return True
    if any(normalized.startswith(prefix) for prefix in _WARNING_RESIDUAL_PREFIXES):
        return True
    return normalized.startswith(("warning ", "advisory ", "caution ")) and any(
        marker in normalized for marker in _WARNING_RESIDUAL_TRAIL_MARKERS
    )


def _strip_warning_residual_brackets(text):
    """Remove stale bracketed warning/instruction labels while keeping real citations."""
    if not text:
        return text

    def _rewrite(match):
        label = match.group(1)
        if _is_warning_residual_bracket(label):
            return ""
        return match.group(0)

    cleaned = _WARNING_RESIDUAL_BRACKET_PATTERN.sub(_rewrite, text)
    cleaned = re.sub(r"\[\s*\]", "", cleaned)
    cleaned = re.sub(r"\(\s*\)", "", cleaned)
    cleaned = re.sub(r"[ \t]+([,.;:!?])", r"\1", cleaned)
    cleaned = re.sub(r"[ \t]+\n", "\n", cleaned)
    cleaned = re.sub(r"[ ]{2,}", " ", cleaned)
    return cleaned


def _default_all_guide_ids():
    from guide_catalog import all_guide_ids

    return all_guide_ids()


def _drop_unknown_guide_citations(text, *, valid_guide_ids_provider=None, warn_event=None):
    """Remove citations that do not exist in the live guide catalog."""
    if valid_guide_ids_provider is None:
        valid_guide_ids_provider = _default_all_guide_ids
    valid_guide_ids = set(valid_guide_ids_provider())
    invalid_ids = set()

    def _rewrite_group(match):
        citations = re.findall(r"GD-\d{3}", match.group(0))
        keep = []
        for citation in citations:
            if citation in valid_guide_ids:
                if citation not in keep:
                    keep.append(citation)
            else:
                invalid_ids.add(citation)
        if not keep:
            return ""
        return "[" + ", ".join(keep) + "]"

    cleaned = re.sub(r"\[(?:[^\]]*GD[^\]]*)\]", _rewrite_group, text)
    remaining_invalid = set(re.findall(r"GD-\d{3}", cleaned)) - valid_guide_ids
    invalid_ids.update(remaining_invalid)
    for invalid_id in sorted(invalid_ids):
        cleaned = re.sub(rf"\b{re.escape(invalid_id)}\b", "", cleaned)
        if warn_event is not None:
            warn_event("citation_hallucination", guide_id=invalid_id)

    cleaned = re.sub(r"\[\s*\]", "", cleaned)
    cleaned = re.sub(r"\(\s*\)", "", cleaned)
    cleaned = re.sub(r"\s+([,.;:!?])", r"\1", cleaned)
    return cleaned


def normalize_response_text(text, *, valid_guide_ids_provider=None, warn_event=None):
    """Normalize common model-output citation/pathology issues."""
    if not text:
        return text

    normalized = _fix_mojibake(text)
    normalized = _strip_warning_residual_brackets(normalized)
    normalized = re.sub(
        r"\bGD[-/](\d{1,3})\b",
        lambda match: f"GD-{int(match.group(1)):03d}",
        normalized,
    )
    normalized = re.sub(
        r"\[(?:[^\]]*GD[^\]]*)\]", _normalize_citation_group, normalized
    )
    normalized = "\n".join(
        _compress_citations_on_line(line) for line in normalized.splitlines()
    )
    normalized = _compress_citations_across_numbered_steps(normalized)
    normalized = _drop_unknown_guide_citations(
        normalized,
        valid_guide_ids_provider=valid_guide_ids_provider,
        warn_event=warn_event,
    )
    normalized = re.sub(
        r"(\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\])(?:\s*\1)+", r"\1", normalized
    )
    normalized = _scrub_retrieval_mechanism_language(normalized)
    normalized = re.sub(r"[ \t]+\n", "\n", normalized)
    normalized = re.sub(r"\n{3,}", "\n\n", normalized)
    normalized = re.sub(r"[ ]{2,}", " ", normalized)
    return normalized.strip()


def _duplicate_citation_count(text):
    """Return the number of repeated guide citations in a response."""
    counts = Counter(re.findall(r"GD-\d+", text or ""))
    return sum(count - 1 for count in counts.values() if count > 1)

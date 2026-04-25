"""Completion-shape hardening helpers shared by query and bench paths."""

from __future__ import annotations

import re


def _normalize_completion_text(text, normalize_response_text_fn=None):
    """Normalize completion text when the caller provides a runtime normalizer."""
    text = text or ""
    if normalize_response_text_fn is None:
        return text
    return normalize_response_text_fn(text)


def _numbered_step_numbers(text):
    """Extract numbered-list step numbers from the response."""
    return [int(match.group(1)) for match in re.finditer(r"(?m)^\s*(\d+)\.\s", text or "")]


def _has_malformed_trailing_citation(text):
    """Return True for obvious cut-off citation endings like `[GD-085`."""
    stripped = (text or "").rstrip()
    if not stripped:
        return True
    if stripped.count("[") > stripped.count("]"):
        return True

    tail = stripped[-24:]
    citation_start = tail.rfind("[GD-")
    return citation_start != -1 and "]" not in tail[citation_start:]


def _is_obviously_incomplete_crisis_response(text):
    """Catch crisis outputs that are clearly truncated or missing the scaffold."""
    stripped = (text or "").strip()
    if not stripped:
        return True
    if _has_malformed_trailing_citation(stripped):
        return True

    step_numbers = _numbered_step_numbers(stripped)
    if step_numbers:
        if step_numbers != list(range(1, len(step_numbers) + 1)):
            return True
        if len(step_numbers) < 3:
            return True

    word_count = len(re.findall(r"\b\w+\b", stripped))
    if word_count < 60 and len(step_numbers) < 4:
        return True

    return stripped[-1] not in ".?!]"


def _has_substantive_response(text):
    """Return True when the model returned non-empty content."""
    return bool(text and text.strip())


def _is_obviously_incomplete_safety_response(text, *, normalize_response_text_fn=None):
    """Detect truncated final-line scaffolds that are too risky to accept as final."""
    cleaned_text = _normalize_completion_text(text, normalize_response_text_fn)
    if not _has_substantive_response(cleaned_text):
        return False

    lines = [line.strip() for line in cleaned_text.splitlines() if line.strip()]
    if not lines:
        return False

    final_line = lines[-1]
    compact = re.sub(r"\s+", " ", final_line).strip()
    markdown_light = re.sub(r"[*_`]+", "", compact).strip()
    without_marker = re.sub(
        r"^\s*(?:[-*+]\s*)?(?:\d+[\.)]\s*)?",
        "",
        markdown_light,
    ).strip()
    without_marker = without_marker.strip("-: ")

    if re.search(r"(?i)(?:^|\s)if\s*$", markdown_light):
        return True
    if re.match(r"(?i)^if\b[^.!?]*:\s*$", without_marker):
        return True
    if re.match(r"(?i)^if\b", without_marker) and not re.search(r"[.!?]\s*$", without_marker):
        tail = without_marker.lower().split()
        if len(tail) <= 3 or tail[-1] in {
            "a",
            "an",
            "and",
            "by",
            "for",
            "of",
            "or",
            "the",
            "to",
            "with",
        }:
            return True
    if re.match(r"^\s*(?:[-*+]\s*)?\d+[\.)]\s*$", compact):
        return True
    if re.match(r"^\s*(?:[-*+]\s*)?\d+[\.)]\s+[^.!?]{1,100}:\s*$", markdown_light):
        return True
    return False


def _trim_incomplete_final_safety_line(text, *, normalize_response_text_fn=None):
    """Drop a final dangling scaffold line when the remaining safety answer is usable."""
    cleaned_text = _normalize_completion_text(text, normalize_response_text_fn)
    if not _is_obviously_incomplete_safety_response(cleaned_text):
        return cleaned_text, False

    lines = cleaned_text.splitlines()
    last_content_index = None
    for index in range(len(lines) - 1, -1, -1):
        if lines[index].strip():
            last_content_index = index
            break
    if last_content_index is None:
        return cleaned_text, False

    trimmed = "\n".join(lines[:last_content_index]).rstrip()
    if not _has_substantive_response(trimmed):
        return cleaned_text, False
    if _is_obviously_incomplete_safety_response(trimmed):
        return cleaned_text, False
    return trimmed, True


def _is_valid_crisis_retry_response(text, *, normalize_response_text_fn=None):
    """Return True when a crisis retry produced a complete 4-step scaffold."""
    cleaned_text = _normalize_completion_text(text, normalize_response_text_fn)
    if not _has_substantive_response(cleaned_text):
        return False
    if _is_obviously_incomplete_crisis_response(cleaned_text):
        return False
    step_matches = re.findall(
        r"(?m)^\s*(?:Step\s*)?([1-4])[\)\.\:-]",
        cleaned_text,
    )
    if step_matches != ["1", "2", "3", "4"]:
        return False
    if re.search(r"(?m)^\s*(?:Step\s*)?[5-9][\)\.\:-]", cleaned_text):
        return False
    return True


def _build_crisis_retry_messages(system_prompt, prompt):
    """Tighten completion shape for a single crisis-response retry."""
    retry_system_prompt = (
        f"{system_prompt}\n\n"
        "Completion hardening for emergency mental-health responses: "
        "finish the full answer before stopping. Do not stop mid-sentence, "
        "mid-list, or mid-citation."
    )
    retry_prompt = (
        f"{prompt}\n\n"
        "Completion contract for this retry: write one short urgency summary, "
        "then exactly 4 numbered steps. Each step must be a complete sentence "
        "ending with a closed guide citation like [GD-123]. Include close "
        "supervision, means restriction, urgent escalation, and emergency "
        "medical red flags when relevant."
    )
    return [
        {"role": "system", "content": retry_system_prompt},
        {"role": "user", "content": retry_prompt},
    ]

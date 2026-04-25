"""Completion-shape hardening helpers shared by query and bench paths."""

from __future__ import annotations

import re


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

"""Desktop confidence-label contract for the blocked Wave B answer metadata lane."""

from __future__ import annotations

from dataclasses import dataclass

SYSTEM_INSTRUCTION_DEFAULT = "default_answer"
SYSTEM_INSTRUCTION_LIKELY_MATCH = "likely_match_answer"
SYSTEM_INSTRUCTION_LOW_CONFIDENCE = "low_confidence_answer"
SYSTEM_INSTRUCTION_UNCERTAIN_FIT = "uncertain_fit_answer"
SYSTEM_INSTRUCTION_ABSTAIN = "abstain_answer"

META_TONE_NONE = ""
META_TONE_WARN = "warn"


@dataclass(frozen=True)
class ConfidencePresentation:
    label: str
    system_instruction: str
    metastrip_token: str
    metastrip_tone: str


def _normalize_label(label: str | None) -> str:
    normalized = (label or "").strip().lower().replace("_", "-")
    if normalized in {"", "high", "default"}:
        return "high"
    if normalized in {"medium", "likely", "likely-match"}:
        return "medium"
    if normalized in {"low", "low-confidence"}:
        return "low"
    if normalized in {"uncertain", "uncertain-fit"}:
        return "uncertain-fit"
    if normalized == "abstain":
        return "abstain"
    return "high"


def resolve_confidence_presentation(label: str | None, *, abstain: bool = False) -> ConfidencePresentation:
    if abstain:
        return ConfidencePresentation(
            label="abstain",
            system_instruction=SYSTEM_INSTRUCTION_ABSTAIN,
            metastrip_token="",
            metastrip_tone=META_TONE_NONE,
        )

    normalized = _normalize_label(label)
    if normalized == "medium":
        return ConfidencePresentation(
            label="medium",
            system_instruction=SYSTEM_INSTRUCTION_LIKELY_MATCH,
            metastrip_token="likely match",
            metastrip_tone=META_TONE_NONE,
        )
    if normalized == "low":
        return ConfidencePresentation(
            label="low",
            system_instruction=SYSTEM_INSTRUCTION_LOW_CONFIDENCE,
            metastrip_token="low confidence",
            metastrip_tone=META_TONE_WARN,
        )
    if normalized == "uncertain-fit":
        return ConfidencePresentation(
            label="uncertain-fit",
            system_instruction=SYSTEM_INSTRUCTION_UNCERTAIN_FIT,
            metastrip_token="uncertain fit",
            metastrip_tone=META_TONE_WARN,
        )
    return ConfidencePresentation(
        label="high",
        system_instruction=SYSTEM_INSTRUCTION_DEFAULT,
        metastrip_token="",
        metastrip_tone=META_TONE_NONE,
    )

"""Small text-matching helpers shared by query routing rules."""

import re


def _normalize_text(value):
    if not isinstance(value, str):
        return ""
    return value.casefold()


def text_has_marker(text, markers):
    """Return True if any marker appears in text."""
    lower = _normalize_text(text)
    for marker in markers:
        marker = _normalize_text(marker)
        if not marker:
            continue
        if " " in marker:
            if marker in lower:
                return True
        elif re.search(r"\b" + re.escape(marker) + r"\b", lower):
            return True
    return False


_text_has_marker = text_has_marker

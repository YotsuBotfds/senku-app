"""Small text-matching helpers shared by query routing rules."""

import re


def text_has_marker(text, markers):
    """Return True if any marker appears in text."""
    lower = (text or "").lower()
    for marker in markers:
        if " " in marker:
            if marker in lower:
                return True
        elif re.search(r"\b" + re.escape(marker) + r"\b", lower):
            return True
    return False


_text_has_marker = text_has_marker

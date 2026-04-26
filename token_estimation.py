"""Token estimation helpers with optional tokenizer-backed counting."""

import re

try:
    import tiktoken
except ImportError:  # pragma: no cover - optional dependency
    tiktoken = None


_ENCODER = None
if tiktoken is not None:  # pragma: no branch - tiny setup path
    try:
        _ENCODER = tiktoken.get_encoding("cl100k_base")
    except Exception:  # pragma: no cover - defensive fallback
        _ENCODER = None


def estimate_tokens(text):
    """Estimate token count, preferring a real tokenizer when available."""
    if not text:
        return 0
    if not isinstance(text, str):
        raise TypeError("estimate_tokens expects a string or falsy empty input")

    if _ENCODER is not None:
        try:
            return len(_ENCODER.encode(text))
        except Exception:
            pass

    # Fallback: count word/punctuation pieces plus a small character floor so
    # dense technical strings and abbreviations do not undercount badly.
    pieces = re.findall(r"\w+|[^\w\s]", text, flags=re.UNICODE)
    return max(len(pieces), len(text) // 5)

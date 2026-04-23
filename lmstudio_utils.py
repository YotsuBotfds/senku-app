"""Shared LM Studio helper functions."""

import requests

LM_STUDIO_RETRY_STATUS_CODES = {408, 425, 429, 500, 502, 503, 504}

LM_STUDIO_MODEL_LOAD_400_MARKERS = (
    "failed to load model",
    "model is loading",
    "model loading",
    "insufficient system resources",
)

LM_STUDIO_CONTEXT_PRESSURE_400_MARKERS = (
    "context length",
    "maximum context length",
    "context window",
    "too many tokens",
    "prompt is too long",
    "requested tokens",
    "exceeds context",
    "exceeds the context",
)

LM_STUDIO_CONTEXT_OVERFLOW_500_MARKERS = (
    "input token ids are too long",
    "exceeding the maximum number of tokens allowed",
    "maximum number of tokens allowed",
)

LM_STUDIO_BAD_REQUEST_400_MARKERS = (
    "invalid request",
    "invalid json",
    "invalid message",
    "malformed",
    "schema",
    "unknown model",
    "model not found",
    "does not exist",
)

LM_STUDIO_MODEL_ALIAS_400_MARKERS = (
    "unknown model",
    "model not found",
    "does not exist",
)

EMBED_MODEL_FALLBACKS = {
    "nomic-ai/text-embedding-nomic-embed-text-v1.5": [
        "stratalab-org/text-embedding-nomic-embed-text-v1.5",
    ],
    "stratalab-org/text-embedding-nomic-embed-text-v1.5": [
        "nomic-ai/text-embedding-nomic-embed-text-v1.5",
    ],
}


def normalize_lm_studio_url(base_url):
    """Normalize a configured LM Studio base URL."""
    return (base_url or "").rstrip("/")


def embedding_models_to_try(model):
    """Return the configured embedding model plus any compatible aliases."""
    models = [model]
    for fallback in EMBED_MODEL_FALLBACKS.get(model, []):
        if fallback not in models:
            models.append(fallback)
    return models


def should_try_embedding_fallback(exc):
    """Return True when LM Studio rejected a model ID but a compatible alias may work."""
    if not isinstance(exc, requests.HTTPError):
        return False

    response = getattr(exc, "response", None)
    if getattr(response, "status_code", None) != 400:
        return False

    message = (getattr(response, "text", "") or "").lower()
    return any(marker in message for marker in LM_STUDIO_MODEL_LOAD_400_MARKERS) or any(
        marker in message for marker in LM_STUDIO_MODEL_ALIAS_400_MARKERS
    )


def classify_lm_request_error(exc):
    """Return a normalized description for LM Studio request failures."""
    if isinstance(exc, requests.Timeout):
        return {
            "category": "timeout",
            "status_code": None,
            "retryable": True,
            "message": str(exc),
        }

    if isinstance(exc, requests.ConnectionError):
        return {
            "category": "connection_error",
            "status_code": None,
            "retryable": True,
            "message": str(exc),
        }

    if isinstance(exc, requests.HTTPError):
        response = getattr(exc, "response", None)
        status_code = getattr(response, "status_code", None)
        message = (getattr(response, "text", "") or "").strip()
        message_lower = message.lower()

        if status_code == 400:
            if any(marker in message_lower for marker in LM_STUDIO_CONTEXT_PRESSURE_400_MARKERS):
                category = "context_pressure"
                retryable = False
            elif any(marker in message_lower for marker in LM_STUDIO_BAD_REQUEST_400_MARKERS):
                category = "bad_request"
                retryable = False
            elif any(marker in message_lower for marker in LM_STUDIO_MODEL_LOAD_400_MARKERS):
                category = "model_load"
                retryable = False
            else:
                category = "runtime_400"
                retryable = False
        elif status_code == 408:
            category = "timeout_http"
            retryable = True
        elif status_code == 425:
            category = "too_early"
            retryable = True
        elif status_code == 429:
            category = "rate_limited"
            retryable = True
        elif status_code in (500, 502, 503, 504):
            if any(marker in message_lower for marker in LM_STUDIO_CONTEXT_OVERFLOW_500_MARKERS):
                category = "context_overflow"
                retryable = False
            else:
                category = "server_error"
                retryable = True
        elif status_code is not None and status_code >= 500:
            if any(marker in message_lower for marker in LM_STUDIO_CONTEXT_OVERFLOW_500_MARKERS):
                category = "context_overflow"
                retryable = False
            else:
                category = "server_error"
                retryable = True
        else:
            category = f"http_{status_code}" if status_code is not None else "http_error"
            retryable = status_code in LM_STUDIO_RETRY_STATUS_CODES

        return {
            "category": category,
            "status_code": status_code,
            "retryable": retryable,
            "message": message or str(exc),
        }

    if isinstance(exc, requests.RequestException):
        return {
            "category": "request_error",
            "status_code": None,
            "retryable": False,
            "message": str(exc),
        }

    return {
        "category": "error",
        "status_code": None,
        "retryable": False,
        "message": str(exc),
    }


def is_retryable_lm_request(exc):
    """Return True for transient LM Studio request failures."""
    return classify_lm_request_error(exc)["retryable"]

"""Small JSONL trace writer and OTel-shaped adapter for RAG runs."""

from __future__ import annotations

import contextvars
import json
import math
import secrets
import threading
import time
from pathlib import Path
from types import TracebackType
from typing import Any, Callable, Iterator, Mapping, MutableMapping, Sequence


REDACTED_VALUE = "[redacted]"
TRACE_SCHEMA_VERSION = 1

_CURRENT_SPAN: contextvars.ContextVar[tuple[str, str] | None] = (
    contextvars.ContextVar("rag_trace_current_span", default=None)
)


def sanitize_attributes(attributes: Mapping[str, Any] | None) -> dict[str, Any]:
    """Return JSON-safe attributes with raw prompt/question fields redacted."""
    if not attributes:
        return {}
    return {
        str(key): _sanitize_value(value, key=str(key))
        for key, value in sorted(attributes.items(), key=lambda item: str(item[0]))
    }


def to_otel_span(record: Mapping[str, Any]) -> dict[str, Any]:
    """Map one internal JSONL record into a deterministic OTel-shaped span."""
    trace_id = str(record.get("trace_id") or "")
    span_id = str(record.get("span_id") or "")
    raw_status = record.get("status")
    status = dict(raw_status) if isinstance(raw_status, Mapping) else {}
    status_code = raw_status if raw_status and not isinstance(raw_status, Mapping) else status.get("code")
    parent_span_id = record.get("parent_span_id")
    attributes = dict(record.get("attributes") or {})
    for key in ("artifact_name", "prompt_index"):
        if key in record and key not in attributes:
            attributes[key] = record.get(key)

    span = {
        "name": str(record.get("name") or ""),
        "context": {
            "trace_id": trace_id,
            "span_id": span_id,
            "trace_state": "",
        },
        "parent_id": str(parent_span_id) if parent_span_id else None,
        "kind": str(record.get("kind") or "INTERNAL"),
        "start_time_unix_nano": int(record.get("start_time_unix_nano") or 0),
        "end_time_unix_nano": int(record.get("end_time_unix_nano") or 0),
        "status": {
            "status_code": str(status_code or "UNSET"),
            "description": str(status.get("message") or ""),
        },
        "attributes": sanitize_attributes(attributes),
        "events": sanitize_events(record.get("events") or []),
    }
    if "duration_ms" in record:
        span["duration_ms"] = record.get("duration_ms")
    return span


def iter_otel_spans(path: str | Path) -> Iterator[dict[str, Any]]:
    """Yield OTel-shaped spans from a JSONL trace file.

    Malformed JSONL rows are ignored so one partial/corrupt trace event does
    not prevent later valid spans from being exported.
    """
    trace_path = Path(path)
    with trace_path.open("r", encoding="utf-8") as handle:
        for line in handle:
            stripped = line.strip()
            if not stripped:
                continue
            try:
                payload = json.loads(stripped)
            except json.JSONDecodeError:
                continue
            if isinstance(payload, Mapping):
                yield to_otel_span(payload)


class RAGTraceWriter:
    """Thread-safe append-only JSONL writer for lightweight RAG span traces."""

    def __init__(
        self,
        path: str | Path,
        *,
        enabled: bool = True,
        clock_ns: Callable[[], int] | None = None,
        id_factory: Callable[[int], str] | None = None,
    ) -> None:
        self.path = Path(path)
        self.enabled = enabled
        self._clock_ns = clock_ns or time.time_ns
        self._id_factory = id_factory or (lambda nbytes: secrets.token_hex(nbytes))
        self._lock = threading.RLock()
        self._handle = None
        if enabled:
            self.path.parent.mkdir(parents=True, exist_ok=True)
            self._handle = self.path.open("a", encoding="utf-8")

    def __enter__(self) -> "RAGTraceWriter":
        return self

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc: BaseException | None,
        tb: TracebackType | None,
    ) -> None:
        self.close()

    def close(self) -> None:
        with self._lock:
            if self._handle is not None:
                self._handle.close()
                self._handle = None

    def write(self, record: Mapping[str, Any]) -> None:
        if not self.enabled:
            return
        with self._lock:
            if self._handle is None:
                raise ValueError("trace writer is closed")
            self._handle.write(
                json.dumps(
                    _sanitize_record(record),
                    allow_nan=False,
                    sort_keys=True,
                    separators=(",", ":"),
                )
                + "\n"
            )
            self._handle.flush()

    def span(
        self,
        name: str,
        *,
        attributes: Mapping[str, Any] | None = None,
        trace_id: str | None = None,
        span_id: str | None = None,
        kind: str = "INTERNAL",
    ) -> "_RAGSpan":
        return _RAGSpan(
            writer=self,
            name=name,
            attributes=attributes,
            trace_id=trace_id,
            span_id=span_id,
            kind=kind,
        )

    def _new_trace_id(self) -> str:
        return self._id_factory(16)

    def _new_span_id(self) -> str:
        return self._id_factory(8)

    def _now_ns(self) -> int:
        return int(self._clock_ns())


class _RAGSpan:
    def __init__(
        self,
        *,
        writer: RAGTraceWriter,
        name: str,
        attributes: Mapping[str, Any] | None,
        trace_id: str | None,
        span_id: str | None,
        kind: str,
    ) -> None:
        self._writer = writer
        self._name = name
        self._attributes = sanitize_attributes(attributes)
        self._explicit_trace_id = trace_id
        self._explicit_span_id = span_id
        self._kind = kind
        self._token: contextvars.Token[tuple[str, str] | None] | None = None
        self.trace_id = ""
        self.span_id = ""
        self.parent_span_id: str | None = None
        self.start_time_unix_nano = 0

    def __enter__(self) -> "_RAGSpan":
        parent = _CURRENT_SPAN.get()
        if parent is None:
            self.trace_id = self._explicit_trace_id or self._writer._new_trace_id()
            self.parent_span_id = None
        else:
            self.trace_id = self._explicit_trace_id or parent[0]
            self.parent_span_id = parent[1]
        self.span_id = self._explicit_span_id or self._writer._new_span_id()
        self.start_time_unix_nano = self._writer._now_ns()
        self._token = _CURRENT_SPAN.set((self.trace_id, self.span_id))
        return self

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc: BaseException | None,
        tb: TracebackType | None,
    ) -> bool:
        end_time_unix_nano = self._writer._now_ns()
        status: MutableMapping[str, Any] = {"code": "OK"}
        if exc is not None:
            status = {
                "code": "ERROR",
                "message": type(exc).__name__,
            }

        record: dict[str, Any] = {
            "schema_version": TRACE_SCHEMA_VERSION,
            "name": self._name,
            "kind": self._kind,
            "trace_id": self.trace_id,
            "span_id": self.span_id,
            "parent_span_id": self.parent_span_id,
            "start_time_unix_nano": self.start_time_unix_nano,
            "end_time_unix_nano": end_time_unix_nano,
            "duration_ms": round(
                (end_time_unix_nano - self.start_time_unix_nano) / 1_000_000,
                6,
            ),
            "status": dict(status),
            "attributes": self._attributes,
            "events": [],
        }
        if exc is not None:
            record["events"].append(
                {
                    "name": "exception",
                    "time_unix_nano": end_time_unix_nano,
                    "attributes": {
                        "exception.type": type(exc).__name__,
                        "exception.message": REDACTED_VALUE,
                        "exception.stacktrace": REDACTED_VALUE,
                    },
                }
            )

        try:
            self._writer.write(record)
        finally:
            if self._token is not None:
                _CURRENT_SPAN.reset(self._token)
        return False


def _sanitize_value(value: Any, *, key: str | None = None) -> Any:
    if key is not None and _is_sensitive_key(key):
        return REDACTED_VALUE
    if isinstance(value, Mapping):
        return {
            str(inner_key): _sanitize_value(inner_value, key=str(inner_key))
            for inner_key, inner_value in sorted(
                value.items(), key=lambda item: str(item[0])
            )
        }
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        return [_sanitize_value(item) for item in value]
    return _json_safe(value)


def sanitize_events(events: Any) -> list[Any]:
    if isinstance(events, Mapping):
        events = [events]
    if isinstance(events, (str, bytes, bytearray)) or not isinstance(events, Sequence):
        events = [events]
    sanitized = []
    for event in events:
        if not isinstance(event, Mapping):
            sanitized.append(_json_safe(event))
            continue
        event_copy = dict(event)
        if isinstance(event_copy.get("attributes"), Mapping):
            event_copy["attributes"] = sanitize_attributes(event_copy["attributes"])
        else:
            event_copy["attributes"] = _json_safe(event_copy.get("attributes", {}))
        sanitized.append(_json_safe(event_copy))
    return sanitized


def _sanitize_record(record: Mapping[str, Any]) -> dict[str, Any]:
    sanitized = dict(record)
    if isinstance(sanitized.get("attributes"), Mapping):
        sanitized["attributes"] = sanitize_attributes(sanitized["attributes"])
    if "events" in sanitized:
        sanitized["events"] = sanitize_events(sanitized["events"])
    return _json_safe(sanitized)


def _is_sensitive_key(key: str) -> bool:
    normalized = "".join(
        char.lower() if char.isalnum() else "_" for char in str(key)
    )
    parts = [part for part in normalized.split("_") if part]
    safe_suffixes = {"id", "ids", "index", "hash", "count"}
    if any(part in {"question", "questions"} for part in parts):
        return parts[-1] not in safe_suffixes
    if any(part in {"prompt", "prompts"} for part in parts):
        return parts[-1] not in safe_suffixes
    return False


def _json_safe(value: Any) -> Any:
    if value is None or isinstance(value, (str, bool, int)):
        return value
    if isinstance(value, float):
        if math.isfinite(value):
            return value
        return str(value)
    if isinstance(value, Mapping):
        return {str(key): _json_safe(inner) for key, inner in value.items()}
    if isinstance(value, Sequence) and not isinstance(value, (str, bytes, bytearray)):
        return [_json_safe(item) for item in value]
    return str(value)

import json
import tempfile
import threading
import unittest
from pathlib import Path

from rag_trace import RAGTraceWriter, iter_otel_spans, sanitize_attributes, to_otel_span


def read_jsonl(path: Path) -> list[dict]:
    return [
        json.loads(line)
        for line in path.read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]


class CounterClock:
    def __init__(self, start: int = 1_000_000_000) -> None:
        self.value = start
        self.lock = threading.Lock()

    def __call__(self) -> int:
        with self.lock:
            self.value += 1_000_000
            return self.value


class CounterIds:
    def __init__(self) -> None:
        self.value = 0
        self.lock = threading.Lock()

    def __call__(self, nbytes: int) -> str:
        with self.lock:
            self.value += 1
            return f"{self.value:0{nbytes * 2}x}"


class RAGTraceTests(unittest.TestCase):
    def test_span_writes_jsonl_and_redacts_prompt_and_question_attrs(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            with RAGTraceWriter(path, clock_ns=CounterClock(), id_factory=CounterIds()) as writer:
                with writer.span(
                    "retrieve",
                    attributes={
                        "prompt": "raw prompt text",
                        "user_question": "raw question text",
                        "prompt_id": "RE2-BR-005",
                        "prompt_index": 7,
                        "question_hash": "abc123",
                        "retrieved_count": 3,
                        "nested": {"system_prompt": "raw system prompt"},
                    },
                ):
                    pass

            rows = read_jsonl(path)

        self.assertEqual(len(rows), 1)
        self.assertEqual(rows[0]["name"], "retrieve")
        self.assertEqual(rows[0]["status"], {"code": "OK"})
        self.assertEqual(rows[0]["attributes"]["prompt"], "[redacted]")
        self.assertEqual(rows[0]["attributes"]["user_question"], "[redacted]")
        self.assertEqual(rows[0]["attributes"]["nested"]["system_prompt"], "[redacted]")
        self.assertEqual(rows[0]["attributes"]["prompt_id"], "RE2-BR-005")
        self.assertEqual(rows[0]["attributes"]["prompt_index"], 7)
        self.assertEqual(rows[0]["attributes"]["question_hash"], "abc123")
        self.assertNotIn("raw prompt text", json.dumps(rows[0]))
        self.assertNotIn("raw question text", json.dumps(rows[0]))

    def test_nested_spans_share_trace_and_set_parent(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            with RAGTraceWriter(path, clock_ns=CounterClock(), id_factory=CounterIds()) as writer:
                with writer.span("query"):
                    with writer.span("rerank"):
                        pass

            rows = read_jsonl(path)

        child, parent = rows
        self.assertEqual(parent["name"], "query")
        self.assertEqual(child["name"], "rerank")
        self.assertEqual(child["trace_id"], parent["trace_id"])
        self.assertEqual(child["parent_span_id"], parent["span_id"])
        self.assertIsNone(parent["parent_span_id"])

    def test_exception_status_is_recorded_and_exception_propagates(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            with RAGTraceWriter(path, clock_ns=CounterClock(), id_factory=CounterIds()) as writer:
                with self.assertRaises(RuntimeError):
                    with writer.span("generate"):
                        raise RuntimeError("model failed")

            rows = read_jsonl(path)

        self.assertEqual(rows[0]["status"]["code"], "ERROR")
        self.assertEqual(rows[0]["status"]["message"], "RuntimeError")
        self.assertEqual(rows[0]["events"][0]["name"], "exception")
        self.assertEqual(
            rows[0]["events"][0]["attributes"]["exception.type"],
            "RuntimeError",
        )
        self.assertEqual(
            rows[0]["events"][0]["attributes"]["exception.message"],
            "[redacted]",
        )

    def test_thread_safe_writer_emits_complete_json_lines(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            with RAGTraceWriter(path, clock_ns=CounterClock(), id_factory=CounterIds()) as writer:
                threads = [
                    threading.Thread(
                        target=lambda index=index: writer.write(
                            {
                                "name": "worker",
                                "worker": index,
                                "attributes": {"retrieved_count": index},
                            }
                        )
                    )
                    for index in range(20)
                ]
                for thread in threads:
                    thread.start()
                for thread in threads:
                    thread.join()

            rows = read_jsonl(path)

        self.assertEqual(len(rows), 20)
        self.assertEqual({row["worker"] for row in rows}, set(range(20)))

    def test_otel_mapping_and_iterator_are_deterministic(self):
        record = {
            "name": "retrieve",
            "trace_id": "trace-1",
            "span_id": "span-1",
            "parent_span_id": "parent-1",
            "start_time_unix_nano": 100,
            "end_time_unix_nano": 200,
            "status": {"code": "OK"},
            "attributes": {"question": "raw?", "k": 5},
        }

        mapped = to_otel_span(record)

        self.assertEqual(
            mapped,
            {
                "name": "retrieve",
                "context": {
                    "trace_id": "trace-1",
                    "span_id": "span-1",
                    "trace_state": "",
                },
                "parent_id": "parent-1",
                "kind": "INTERNAL",
                "start_time_unix_nano": 100,
                "end_time_unix_nano": 200,
                "status": {"status_code": "OK", "description": ""},
                "attributes": {"k": 5, "question": "[redacted]"},
                "events": [],
            },
        )

        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            path.write_text(json.dumps(record) + "\n", encoding="utf-8")
            spans = list(iter_otel_spans(path))

        self.assertEqual(spans, [mapped])

    def test_sanitize_attributes_handles_non_json_values(self):
        attrs = sanitize_attributes({"tags": {"a", "b"}, "safe": True})

        self.assertEqual(attrs["safe"], True)
        self.assertIn("'", attrs["tags"])


if __name__ == "__main__":
    unittest.main()

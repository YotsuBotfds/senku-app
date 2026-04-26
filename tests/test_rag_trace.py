import json
import math
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

    def test_direct_write_redacts_sensitive_attributes_and_events(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            with RAGTraceWriter(path) as writer:
                writer.write(
                    {
                        "name": "manual",
                        "attributes": {
                            "question": "raw question text",
                            "prompt_hash": "safe-hash",
                        },
                        "events": [
                            {
                                "name": "input",
                                "attributes": {
                                    "prompt_text": "raw prompt text",
                                    "prompt_id": "P-1",
                                },
                            }
                        ],
                    }
                )

            rows = read_jsonl(path)

        payload = json.dumps(rows[0])
        self.assertEqual(rows[0]["attributes"]["question"], "[redacted]")
        self.assertEqual(rows[0]["attributes"]["prompt_hash"], "safe-hash")
        self.assertEqual(rows[0]["events"][0]["attributes"]["prompt_text"], "[redacted]")
        self.assertEqual(rows[0]["events"][0]["attributes"]["prompt_id"], "P-1")
        self.assertNotIn("raw question text", payload)
        self.assertNotIn("raw prompt text", payload)

    def test_direct_write_normalizes_single_event_mapping_before_redaction(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            with RAGTraceWriter(path) as writer:
                writer.write(
                    {
                        "name": "manual",
                        "events": {
                            "name": "input",
                            "attributes": {
                                "question_text": "raw question text",
                                "prompt_id": "P-1",
                            },
                        },
                    }
                )

            rows = read_jsonl(path)

        payload = json.dumps(rows[0])
        self.assertIsInstance(rows[0]["events"], list)
        self.assertEqual(rows[0]["events"][0]["name"], "input")
        self.assertEqual(
            rows[0]["events"][0]["attributes"]["question_text"],
            "[redacted]",
        )
        self.assertEqual(rows[0]["events"][0]["attributes"]["prompt_id"], "P-1")
        self.assertNotIn("raw question text", payload)

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
            "events": [{"name": "input", "attributes": {"prompt": "raw prompt"}}],
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
                "events": [{"name": "input", "attributes": {"prompt": "[redacted]"}}],
            },
        )

        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            path.write_text(json.dumps(record) + "\n", encoding="utf-8")
            spans = list(iter_otel_spans(path))

        self.assertEqual(spans, [mapped])

    def test_otel_mapping_accepts_string_status(self):
        record = {
            "name": "retrieve",
            "trace_id": "trace-1",
            "span_id": "span-1",
            "status": "FAIL",
            "artifact_name": "run.json",
            "prompt_index": 3,
            "duration_ms": "2.3456",
        }

        mapped = to_otel_span(record)

        self.assertEqual(mapped["status"]["status_code"], "FAIL")
        self.assertEqual(mapped["status"]["description"], "")
        self.assertEqual(mapped["attributes"]["artifact_name"], "run.json")
        self.assertEqual(mapped["attributes"]["prompt_index"], 3)
        self.assertEqual(mapped["duration_ms"], "2.3456")

    def test_otel_iterator_skips_malformed_jsonl_rows(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            path.write_text(
                "\n".join(
                    [
                        json.dumps(
                            {
                                "name": "first",
                                "trace_id": "trace-1",
                                "span_id": "span-1",
                            }
                        ),
                        "{not valid json",
                        json.dumps(["not", "a", "span"]),
                        json.dumps(
                            {
                                "name": "second",
                                "trace_id": "trace-2",
                                "span_id": "span-2",
                            }
                        ),
                    ]
                )
                + "\n",
                encoding="utf-8",
            )

            spans = list(iter_otel_spans(path))

        self.assertEqual([span["name"] for span in spans], ["first", "second"])
        self.assertEqual(spans[0]["context"]["trace_id"], "trace-1")
        self.assertEqual(spans[1]["context"]["trace_id"], "trace-2")

    def test_sanitize_attributes_handles_non_json_values(self):
        attrs = sanitize_attributes({"tags": {"a", "b"}, "safe": True})

        self.assertEqual(attrs["safe"], True)
        self.assertIn("'", attrs["tags"])

    def test_direct_write_converts_non_finite_values_to_json_strings(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            path = Path(tmpdir) / "trace.jsonl"
            with RAGTraceWriter(path) as writer:
                writer.write(
                    {
                        "name": "manual",
                        "duration_ms": math.inf,
                        "attributes": {
                            "score": math.nan,
                            "finite_score": 0.25,
                        },
                        "events": [
                            {
                                "name": "rank",
                                "attributes": {"delta": -math.inf},
                            }
                        ],
                    }
                )

            raw = path.read_text(encoding="utf-8")
            rows = read_jsonl(path)

        self.assertNotIn("NaN", raw)
        self.assertNotIn("Infinity", raw)
        self.assertEqual(rows[0]["duration_ms"], "inf")
        self.assertEqual(rows[0]["attributes"]["score"], "nan")
        self.assertEqual(rows[0]["attributes"]["finite_score"], 0.25)
        self.assertEqual(rows[0]["events"][0]["attributes"]["delta"], "-inf")

    def test_sanitize_attributes_redacts_plural_prompt_question_fields(self):
        attrs = sanitize_attributes(
            {
                "prompts": ["raw prompt one", "raw prompt two"],
                "questions": ["raw question"],
                "prompt_ids": ["P-1"],
                "question_hash": "safe-hash",
                "prompt_index": 3,
                "prompt_count": 2,
            }
        )

        self.assertEqual(attrs["prompts"], "[redacted]")
        self.assertEqual(attrs["questions"], "[redacted]")
        self.assertEqual(attrs["prompt_ids"], ["P-1"])
        self.assertEqual(attrs["question_hash"], "safe-hash")
        self.assertEqual(attrs["prompt_index"], 3)
        self.assertEqual(attrs["prompt_count"], 2)


if __name__ == "__main__":
    unittest.main()

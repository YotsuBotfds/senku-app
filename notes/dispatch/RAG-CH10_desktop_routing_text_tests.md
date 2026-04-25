# RAG-CH10 Desktop Routing Text Helper Tests

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH10` - add focused tests for `query_routing_text.py`.

## Role

Main agent on `gpt-5.5 medium`. This is tiny test-only code health.

## Preconditions

- Work from clean checkpoint `3caec56`.
- Treat routing text matching behavior as frozen.
- Avoid predicate/routing behavior changes.

## Outcome

Add direct coverage for the shared marker-matching helper used by query routing
and deterministic routing surfaces.

Landed shape:

- importing `query_routing_text` does not load `query` or `bench`;
- phrase markers match substrings case-insensitively;
- single-word markers require word boundaries and do not stem;
- empty text and empty marker sets return false;
- legacy private alias `_text_has_marker` remains the public helper.

## Boundaries

- Do not edit `query.py`, `query_routing_text.py`,
  `query_routing_predicates.py`, deterministic routing, guide content, Android,
  runtime defaults, or product behavior.
- Do not change marker matching semantics in this slice.

## Acceptance

- Python compile passes for `query_routing_text.py`, the focused test,
  `query.py`, and `tests/test_query_routing.py`.
- Focused routing-text tests and wrapper/routing tests pass.
- Diff is limited to the direct test, validation-lane docs, and this dispatch
  note.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query_routing_text.py tests\test_query_routing_text.py query.py tests\test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing_text tests.test_query_routing -v
```

Final result: both commands passed; the focused routing lane ran `67` tests.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- stop lines confirmed.

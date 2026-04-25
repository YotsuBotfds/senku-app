# RAG-CH4 Desktop Completion-Shape Hardening Extraction

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH4` - move remaining pure bench completion-shape helpers into
`query_completion_hardening.py` while preserving `bench.py` compatibility
wrappers.

## Role

Main agent / worker on `gpt-5.5 medium`. This is code health only.

## Preconditions

- Work from clean checkpoint `c558f91`.
- Read the `RAG-S13` warning in `notes/dispatch/README.md`: do not extract
  `build_prompt(...)` or medical predicate clusters in this slice.
- Treat desktop behavior as frozen unless tests prove the wrapper behavior is
  unchanged.

## Outcome

Move these pure helpers out of `bench.py`:

- `_is_obviously_incomplete_safety_response`;
- `_trim_incomplete_final_safety_line`;
- `_has_substantive_response`;
- `_is_valid_crisis_retry_response`.

`bench.py` keeps same-name wrappers so existing tests and import surfaces keep
working. `query_completion_hardening.py` should not import `bench.py` or
`query.py`; inject normalization helpers where needed.

Landed shape:

- the four pure helpers now live in `query_completion_hardening.py`;
- `bench.py` keeps same-name wrappers that call the extracted helpers;
- `normalize_response_text` is passed into extracted helpers by dependency
  injection;
- direct helper coverage was added to `tests/test_query_completion_hardening.py`.

## Boundaries

- Do not touch `query.py`.
- Do not change retry policy/control flow in `bench.py`.
- Do not move or rewrite crisis continuation prompt copy.
- Do not touch retrieval, ranking, prompt assembly, medical predicates, guides,
  Android, or generated artifacts.
- Do not broaden completion hardening behavior.

## Acceptance

- Python compile passes for `query.py`, `bench.py`,
  `query_completion_hardening.py`, and focused tests.
- Focused unit tests pass:
  `tests.test_query_completion_hardening`, `tests.test_bench_runtime`, and
  `tests.test_runtime_profiles`.
- Diff is limited to the extracted helper module, compatibility wrappers,
  focused tests if needed, and this dispatch note.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py bench.py query_completion_hardening.py tests\test_query_completion_hardening.py tests\test_bench_runtime.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_completion_hardening tests.test_bench_runtime tests.test_runtime_profiles -v
```

Final result: both commands passed; unit lane ran `39` tests successfully.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- stop lines confirmed.

# RAG-CH8 Desktop Bench Answer Diagnostics Tests

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH8` - add focused tests for `rag_bench_answer_diagnostics.py`.

## Role

Main agent / worker on `gpt-5.5 medium`. This is test-only code health.

## Preconditions

- Work from clean checkpoint `e339212`.
- Treat analyzer behavior and app/card acceptance classifications as frozen.
- Keep tests synthetic and dependency-injected; avoid generating new bench
  artifacts.

## Outcome

Add direct coverage for the extracted bench answer-diagnostics helper so future
analyzer changes can test pure card/app-acceptance behavior without going
through larger artifact-building tests first.

Landed shape:

- importing `rag_bench_answer_diagnostics` does not load `query`, `bench`, or
  `scripts.analyze_rag_bench_failures`;
- `compact_match_phrases(...)` skips blanks and deduplicates phrases;
- `compact_claim_basis(...)` counts and sorts support-basis values;
- `select_family_cards(...)` narrows by expected-family tokens and falls back
  when no card family matches;
- bare meningitis-vs-viral compare prompts are distinguished from red-flag
  comparisons;
- `app_acceptance_diagnostics(...)` covers uncertain-fit acceptance,
  expected-owner cited strong support, generated missing evidence owner, and
  missing emergency-first safety surface using injected detectors.

## Boundaries

- Do not edit `rag_bench_answer_diagnostics.py` unless a direct test reveals a
  real helper bug.
- Do not edit guide content, prompt packs, Android, runtime defaults,
  `query.py`, `bench.py`, or product behavior.
- Do not add analyzer output columns or change bucket/app-acceptance
  classifications.

## Acceptance

- Python compile passes for the helper, focused test, analyzer script, and
  analyzer regression test.
- Focused unit tests pass for the direct helper and existing analyzer coverage.
- Diff is limited to the direct test, validation-lane docs, and this dispatch
  note.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile rag_bench_answer_diagnostics.py tests\test_rag_bench_answer_diagnostics.py scripts\analyze_rag_bench_failures.py tests\test_analyze_rag_bench_failures.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_rag_bench_answer_diagnostics tests.test_analyze_rag_bench_failures -v
```

Final result: both commands passed; the focused unit lane ran `34` tests.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- stop lines confirmed.

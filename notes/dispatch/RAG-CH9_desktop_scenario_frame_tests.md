# RAG-CH9 Desktop Scenario-Frame Helper Tests

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH9` - add focused tests for `query_scenario_frame.py`.

## Role

Main agent / worker on `gpt-5.5 medium`. This is test-only code health.

## Preconditions

- Work from clean checkpoint `bdee735`.
- Treat session-frame behavior as frozen.
- Keep this slice limited to direct helper coverage unless tests reveal a real
  helper bug.

## Outcome

Add direct coverage for the extracted scenario-frame helper so session-aware
follow-up behavior has a small test surface outside the broad `query.py`
routing tests.

Landed shape:

- importing `query_scenario_frame` does not load `query` or `bench`;
- injected domain detection and question-restart splitting are exercised;
- inventory fragments become assets instead of objectives;
- placeholder inventory phrases such as "what we have" are ignored;
- deadline, constraint, hazard, people, and environment extraction are covered;
- `update_session_state(...)` and `merge_frame_with_session(...)` preserve
  first-seen order while deduplicating;
- session rendering and `_should_use_session_context(...)` cover vague
  follow-up, explicit reset, and domain-bearing non-follow-up cases.

## Boundaries

- Do not edit `query.py`, `query_scenario_frame.py`, `build_prompt(...)`,
  routing predicates, guide content, Android, generated artifacts, runtime
  defaults, or product behavior.
- Do not change session-frame semantics in this slice.
- Keep tests direct and dependency-injected.

## Acceptance

- Python compile passes for `query_scenario_frame.py`, the focused test,
  `query.py`, and `tests/test_query_routing.py`.
- Focused scenario-frame tests and wrapper/routing tests pass.
- Broad deterministic safety lane remains green.
- Diff is limited to the direct test, validation-lane docs, and this dispatch
  note.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query_scenario_frame.py tests\test_query_scenario_frame.py query.py tests\test_query_routing.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_scenario_frame tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Final result: all commands passed. The focused scenario/routing lane ran `69`
tests; the broader deterministic lane ran `148` tests and validated `173`
deterministic special-case rules.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- stop lines confirmed.

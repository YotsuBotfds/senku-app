# RAG-CH6 Desktop Prompt-Runtime Helper Tests

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH6` - add focused tests for `query_prompt_runtime.py`.

## Role

Main agent / worker on `gpt-5.5 medium`. This is test-only code health.

## Preconditions

- Work from clean checkpoint `5cae0e2`.
- Treat query/bench runtime behavior as frozen.
- Keep this slice limited to direct helper coverage unless a test reveals a
  real helper bug.

## Outcome

Add direct unit coverage for the prompt-runtime helper module shared by
`query.py` and `bench.py`.

Landed shape:

- importing `query_prompt_runtime` does not import `query` or `bench`;
- `system_prompt_text(...)` uses `build_system_prompt(mode)` when the config
  module provides it;
- `system_prompt_text(...)` falls back to `SYSTEM_PROMPT`;
- `prompt_token_limit_from_runtime_profile(...)` uses an injected profile
  without calling the getter;
- missing `prompt_token_limit` raises `ValueError`;
- `estimate_chat_prompt_tokens(...)` accounts for prompt/system message
  overhead with and without a system prompt;
- injected `system_prompt_text` bypasses the resolver.

## Boundaries

- Do not edit `query.py`, `bench.py`, `query_prompt_runtime.py`, prompts,
  routing, retrieval, guides, Android, or generated artifacts.
- Do not change prompt text, runtime profile policy, token-budget constants, or
  generation behavior.
- Keep the tests dependency-injected; do not import the broad runtime modules
  for helper-only assertions.

## Acceptance

- Python compile passes for `query_prompt_runtime.py` and the focused test.
- Focused unit tests pass:
  `tests.test_query_prompt_runtime` and `tests.test_runtime_profiles`.
- Diff is limited to the direct test, validation-lane docs, and this dispatch
  note.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query_prompt_runtime.py tests\test_query_prompt_runtime.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_prompt_runtime tests.test_runtime_profiles -v
```

Final result: both commands passed; the focused unit lane ran `17` tests.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- stop lines confirmed.

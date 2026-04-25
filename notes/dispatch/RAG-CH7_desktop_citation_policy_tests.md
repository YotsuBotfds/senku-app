# RAG-CH7 Desktop Citation Policy Helper Tests

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH7` - add focused tests for `query_citation_policy.py`.

## Role

Main agent / worker on `gpt-5.5 medium`. This is test-only code health.

## Preconditions

- Work from clean checkpoint `d97ff62`.
- Treat citation-policy behavior as frozen unless direct tests expose a real
  helper bug.
- Use injected predicates for helper assertions.

## Outcome

Add direct coverage for the extracted citation owner-priority helper so future
work can verify this safety-owner seam without loading the full `query.py`
routing context.

Landed shape:

- importing `query_citation_policy` does not import `query`;
- airway owner IDs are prioritized ahead of unrelated retrieved guide IDs;
- newborn reviewed owner-family ordering is preserved;
- non-owner prompts keep the original allowlist object unchanged;
- non-allergy airway prompts narrow citations to retrieved airway-owner IDs;
- airway narrowing falls back to owner IDs already present in the allowlist;
- allergy/anaphylaxis airway prompts do not narrow away allergy sources;
- meningitis-vs-viral comparison prompts filter to clinical owner IDs;
- meningitis rash emergency prompts do not apply the comparison-only filter.

## Boundaries

- Do not edit `query.py`, `query_citation_policy.py`, guides, Android, pack
  assets, prompt text, runtime defaults, or product exposure settings.
- Do not import `query` for helper-only assertions.
- Do not change medical predicate behavior in this slice.

## Acceptance

- Python compile passes for `query_citation_policy.py`, the focused test, and
  wrapper/routing touchpoints.
- Focused unit tests pass for the direct helper and existing wrapper/routing
  coverage.
- Diff is limited to the direct test, validation-lane docs, and this dispatch
  note.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query_citation_policy.py tests\test_query_citation_policy.py query.py tests\test_query_routing.py tests\test_query_answer_card_runtime.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_citation_policy tests.test_query_answer_card_runtime.QueryCitationAllowlistWrapperTests tests.test_query_routing.QueryRoutingTests.test_safety_owner_citation_allowlist_prioritizes_retrieved_target_owners tests.test_query_routing.QueryRoutingTests.test_newborn_citation_allowlist_prioritizes_reviewed_owner_family -v
```

Final result: both commands passed; the focused unit lane ran `12` tests.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- stop lines confirmed.

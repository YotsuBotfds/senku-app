# RAG-S13 Code-Health Extractions

## Slice

Behavior-preserving RAG code-health extraction after the S12 proof set.

## Role

Main agent with scout/worker support on `gpt-5.5 medium`. Use high reasoning
only if touching safety predicates or guide-owner policy behavior.

## Preconditions

- Current behavioral baseline:
  `artifacts/bench/rag_diagnostics_20260424_1715_rags12_litert_defaults_smoke/report.md`
- Runtime defaults are split:
  - generation: `http://127.0.0.1:1235/v1`, `gemma-4-e2b-it-litert`
  - embeddings: `http://127.0.0.1:1234/v1`
- Do not reopen Android or guide-content lanes.

## Outcome

Landed seven bounded extractions:

- `query_abstain_policy.py`
  - weak-retrieval abstain scoring constants;
  - top-row extraction and overlap/vector label helpers;
  - stable query truncation for abstain/uncertain-fit cards.
- `query_answer_card_runtime.py`
  - reviewed answer-card loading/selection helpers;
  - evidence-packet prompt-contract rendering;
  - opt-in reviewed-card runtime answer plan;
  - retrieved citation allowlist extraction.
- `rag_bench_answer_diagnostics.py`
  - analyzer row diagnostic enrichers for answer-card status;
  - claim-support diagnostics;
  - shadow card-answer diagnostics;
  - app acceptance / evidence-owner / UI-surface classification.
- `query_citation_policy.py`
  - source-owner citation priority ordering;
  - narrowed retrieved guide-ID allowlist for airway and meningitis compare
    prompts.
- `query_completion_hardening.py`
  - numbered-step extraction for safety completion checks;
  - malformed trailing citation detection;
  - incomplete crisis-response detection and retry-message construction.
- `query_response_normalization.py`
  - post-generation citation normalization/compression;
  - warning/control bracket residue scrubbing;
  - retrieval-mechanism wording scrub;
  - unknown-guide citation dropping with injected catalog/log hooks.
- `query_prompt_runtime.py`
  - shared query/bench system-prompt lookup;
  - shared prompt-token limit helpers while preserving query and bench call
    shapes;
  - shared chat prompt-token estimation with injected token counters.

`query.py` and `scripts/analyze_rag_bench_failures.py` keep compatibility
wrappers / direct imported symbols so existing tests and patch points continue
to work.

## Boundaries

- Did not extract medical predicate detectors.
- Did not move `build_prompt(...)`.
- Did not move airway context-row filtering because `build_prompt(...)` still
  uses it directly.
- Did not move prompt-token budgeting or prompt contract assembly out of
  `query.py`.
- Did not move safety-critical abstain escalation copy because it depends on
  broad medical/safety predicates.
- Did not move emergency mental-health query detection or marker constants; the
  completion-hardening extraction is intentionally pure `re`/string logic.
- Did not port the desktop reviewed-card/provenance contract to Android in this
  slice. The Python work defines the contract and proof; Android still needs a
  dedicated Kotlin/mobile-pack slice.

## Acceptance

- Direct extraction-boundary tests:
  `tests/test_query_abstain_policy.py`,
  `tests/test_query_answer_card_runtime.py`,
  `tests/test_query_completion_hardening.py`,
  `tests/test_query_response_normalization.py`
- Focused gate:
  `tests.test_query_abstain_policy`, `tests.test_query_answer_card_runtime`,
  `tests.test_abstain`, `tests.test_uncertain_fit`, `tests.test_query_routing`,
  `tests.test_bench_runtime`, `tests.test_guide_answer_card_contracts`,
  `tests.test_analyze_rag_bench_failures`
- S12 preservation artifact:
  `artifacts/bench/rag_diagnostics_20260424_1750_rags13_code_health_final_smoke/report.md`

Final counts stayed clean over `24` EX/EY/EZ/FC rows:

- deterministic pass `13`
- expected supported `9`
- abstain/clarify needed `2`
- retrieval/ranking/generation/safety-contract misses `0`
- hit@1 `21/24`
- hit@3/hit@k/cited `24/24`
- reviewed card-backed answers `8`
- generated prompts `1`
- generated-vs-shadow card gaps `0`

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_answer_card_runtime.py query_citation_policy.py rag_bench_answer_diagnostics.py scripts\analyze_rag_bench_failures.py tests\test_query_answer_card_runtime.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_abstain_policy.py tests\test_query_abstain_policy.py tests\test_abstain.py bench.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_completion_hardening.py bench.py tests\test_query_completion_hardening.py tests\test_bench_runtime.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_response_normalization.py tests\test_query_response_normalization.py bench.py scripts\validate_special_cases.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py bench.py query_prompt_runtime.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_completion_hardening tests.test_bench_runtime tests.test_runtime_profiles tests.test_query_routing.QueryRoutingTests.test_stream_response_card_backed_answer_short_circuits_build_prompt -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_response_normalization tests.test_citation_validation tests.test_special_cases -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_runtime_profiles tests.test_bench_runtime tests.test_bench_config -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_abstain_policy tests.test_query_answer_card_runtime tests.test_abstain tests.test_uncertain_fit tests.test_query_routing tests.test_bench_runtime tests.test_guide_answer_card_contracts tests.test_analyze_rag_bench_failures -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_response_normalization tests.test_citation_validation tests.test_query_completion_hardening tests.test_query_abstain_policy tests.test_query_answer_card_runtime tests.test_abstain tests.test_uncertain_fit tests.test_query_routing tests.test_bench_runtime tests.test_bench_config tests.test_runtime_profiles tests.test_analyze_rag_bench_failures tests.test_guide_answer_card_contracts tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_guide_answer_cards.py
```

Latest broad suite result: `266` tests passed. Validators confirmed `173`
deterministic rules and `6` guide answer cards.

## Follow-Up

Next reasonable code-health slice is not another broad `query.py` pull. Prefer
one of:

- direct tests for `query_citation_policy.py` if owner policy grows further;
- documentation-only planning for the medical predicate extraction until those
  predicates can be grouped by domain with dedicated tests.
- uncertain-fit / confidence helpers are not the next automatic move: they are
  well tested but still entangled with abstain, metadata rerank, and
  safety-critical scenario logic.
- Android translation should be explicit, not assumed: port provenance/card
  labels and/or mobile-pack card metadata behind a dedicated app slice.

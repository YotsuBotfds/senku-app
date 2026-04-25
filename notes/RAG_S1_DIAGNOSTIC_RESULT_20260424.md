# RAG-S1 Diagnostic Result - 2026-04-24

This note records the first RAG-next-level diagnostic run after the 2026-04-24 method pivot.

## Inputs

Analyzer:

- `scripts/analyze_rag_bench_failures.py`

Expectation seed:

- `notes/specs/rag_prompt_expectations_seed_20260424.yaml`

Diagnostic artifact:

- `artifacts/bench/rag_diagnostics_20260424_1000/report.md`
- `artifacts/bench/rag_diagnostics_20260424_1000/diagnostics.csv`
- `artifacts/bench/rag_diagnostics_20260424_1000/diagnostics.json`

Bench inputs:

- `artifacts/bench/guide_wave_ex_20260424_092327.json`
- `artifacts/bench/guide_wave_ey_20260424_092407.json`
- `artifacts/bench/guide_wave_ez_20260424_092437.json`
- `artifacts/bench/guide_wave_fa_20260424_092935.json`
- `artifacts/bench/guide_wave_fb_20260424_092957.json`
- `artifacts/bench/guide_wave_fc_20260424_093011.json`
- `artifacts/bench/guide_wave_fd_20260424_093037.json`
- `artifacts/bench/guide_wave_fe_20260424_093104.json`

## Headline

The current problem is mostly retrieval/ranking, not generation and not a need to keep adding deterministic rules indefinitely.

Across `48` prompt rows:

- `deterministic_pass`: `16`
- `retrieval_miss`: `17`
- `ranking_miss`: `4`
- `safety_contract_miss`: `2`
- `rag_unknown_no_expectation`: `9`
- `generation_miss`: `0`
- `unsupported_or_truncated_answer`: `0`
- `artifact_error`: `0`

Expected-guide rates from the seed manifest:

- hit@1: `18/48 (37.5%)`
- hit@3: `27/48 (56.2%)`
- hit@k: `29/48 (60.4%)`
- cited: `29/48 (60.4%)`

## Per-Wave Read

- `EX` choking / food obstruction:
  - `2` deterministic passes, `2` retrieval misses, `1` ranking miss, `1` safety-contract miss.
  - The RAG path often reaches pediatric/respiratory/anaphylaxis/reflux neighbors before the airway owner.
- `EY` meningitis:
  - `1` deterministic pass, `4` retrieval misses, `1` ranking miss.
  - The sepsis/meningitis owner is sometimes present but too low, and often absent from cited/source rows.
- `EZ` newborn sepsis:
  - `4` retrieval misses, `1` ranking miss, `1` safety-contract miss.
  - The retrieval surface lands on infant care / pediatric broad guides but often misses sepsis-first escalation.
- `FA` poisoning / unknown ingestion:
  - `2` deterministic passes, `2` supported RAG rows, `1` ranking miss, `1` retrieval miss.
  - Better than EX/EY/EZ, but still needs retrieval/rerank support for vague ingestion phrasing.
- `FB` infected wound:
  - `4` deterministic passes, `2` retrieval misses.
  - The red-streak/fever pattern is protected by deterministic paths, but boundary prompts still drift.
- `FC` abdominal trauma:
  - `1` deterministic pass, `4` retrieval misses, `1` supported RAG row.
  - Trauma mechanism wording is not reliably pulling acute abdomen / trauma owners.
- `FD` dangerous activation / mania-like crisis:
  - `6` supported RAG rows.
  - This wave does not justify a deterministic extension on current artifact evidence.
- `FE` stroke/TIA + cardiac overlap:
  - `6` deterministic passes.
  - No immediate RAG work needed unless answer wording review finds a defect.

## Backlog Decision

Do not promote `D52`+ deterministic continuation from this evidence. The next high-leverage work is:

1. Start `RAG-S4` safety-triage retrieval profiles / rerank policy using EX/EY/EZ/FC as the proof set.
2. Start `RAG-S2` answer cards for choking, sepsis/meningitis/newborn danger, poisoning, infected wound, abdominal trauma, and stroke/cardiac overlap.
3. Use `RAG-S3` contextual chunk ingest after a small proof set is ready, because retrieval misses dominate.

## Observability Follow-Up Landed

`RAG-S1b` landed in the same work block:

- `bench.py` now preserves ordered `source_candidates` and `top_retrieved_guide_ids` in per-result retrieval metadata.
- `bench_artifact_tools.py` now flattens `top_retrieved_guide_ids` for evaluator rows.
- Tests moved bench-artifact scratch work into `artifacts/bench/bench_artifact_tools_unit_tests/` to avoid the known Windows temp-permission failure mode.

Focused validation:

- `python -B -m py_compile bench.py bench_artifact_tools.py scripts\analyze_rag_bench_failures.py tests\test_bench_runtime.py tests\test_bench_artifact_tools.py tests\test_analyze_rag_bench_failures.py`
- `python -B -m unittest tests.test_bench_runtime tests.test_bench_artifact_tools tests.test_analyze_rag_bench_failures -v`
- Result: `22` tests passed.

## RAG-S4 Foundation Started

The first behavior-layer follow-up also landed:

- `query.py` now assigns an explicit retrieval profile before fetching:
  - `safety_triage`
  - `normal_vs_urgent`
  - `compare_or_boundary`
  - `low_support`
  - `how_to_task`
- Safety / normal-vs-urgent profiles add owner-focused supplemental retrieval text for:
  - choking / foreign-body airway obstruction;
  - sick newborn / possible sepsis;
  - abdominal trauma;
  - infected wound boundary prompts;
  - general emergency red-flag triage.
- Retrieval metadata now carries `retrieval_profile`.

Focused validation after this step:

- `python -B -m py_compile query.py bench.py bench_artifact_tools.py scripts\analyze_rag_bench_failures.py tests\test_query_routing.py tests\test_bench_runtime.py tests\test_bench_artifact_tools.py tests\test_analyze_rag_bench_failures.py`
- `python -B -m unittest tests.test_query_routing tests.test_bench_runtime tests.test_bench_artifact_tools tests.test_analyze_rag_bench_failures -v`
- Result: `59` tests passed.

## RAG-S4 Fresh Target-Wave Proof

Fresh EX/EY/EZ/FC reruns after the retrieval-profile foundation produced:

- Artifact: `artifacts/bench/rag_diagnostics_20260424_1005_fresh_rags4/report.md`
- Rows: `24`
- Buckets: `4` deterministic pass, `1` `rag_unknown_no_expectation`, `12` retrieval misses, `7` ranking misses, `0` safety-contract misses.
- Expected-guide rates: hit@1 `3/24`, hit@3 `6/24`, hit@k `12/24`, cited `11/24`.

Owner-aware rerank then produced:

- Artifact: `artifacts/bench/rag_diagnostics_20260424_1152_rags4_owner_rerank/report.md`
- Rows: `24`
- Buckets: `4` deterministic pass, `2` `rag_unknown_no_expectation`, `7` retrieval misses, `10` ranking misses, `1` safety-contract miss.
- Expected-guide rates: hit@1 `4/24`, hit@3 `10/24`, hit@k `17/24`, cited `13/24`.

Interpretation: this is progress toward a scalable RAG lane, not just infinite prompt patching. Candidate inclusion improved materially (`hit@k` from `12/24` to `17/24`), but the miss shape moved toward ranking and answer-contract control. The diagnostic loop is now: expected guide manifest plus source candidates plus app answer contract plus safety-profile gates. Next slices should emphasize owner promotion/gating, expectation-driven evidence acceptance, retrieval-profile contract hardening, and guide metadata/citation-owner quality rather than automatic deterministic `D52`+ expansion.

## App Contract Quick Win

The bench/app contract now emits answer-state fields in fresh artifacts:

- Fresh artifact: `artifacts/bench/guide_wave_ex_20260424_114434.json`
- Diagnostic artifact: `artifacts/bench/rag_diagnostics_20260424_1147_ex_contract/report.md`
- New/confirmed fields: `confidence_label`, `answer_mode`, `support_strength`, `safety_critical`, `retrieval_profile`, `app_gate_status`, and `primary_source_titles`.
- The analyzer report now includes an App Gates section so answer-mode/support/safety gate status is visible beside retrieval/ranking buckets.

## Cleaner Expectation Taxonomy

The analyzer now uses `expected_supported` when expected guides are retrieved/cited correctly, instead of lumping supported expectation rows into `rag_unknown_no_expectation`.

EZ expectation seed correction:

- Primary newborn danger-sign family owners: `GD-492`, `GD-298`, `GD-617`, `GD-284`.
- Backup/general support remains `GD-589` and `GD-232`.

Latest EZ weak-support proof:

- Fresh artifact: `artifacts/bench/guide_wave_ez_20260424_115755.json`
- Corrected diagnostic: `artifacts/bench/rag_diagnostics_20260424_ez_expected_supported`
- Bucket counts: `expected_supported` `5`, `abstain_or_clarify_needed` `1`.
- Newborn sepsis weak-support prompt now produces an immediate uncertainty card with an emergency safety line, `generation_time` `0`, and `completion_tokens` `0`.

Full EX/EY/EZ/FC existing-artifact rerun with cleaner owner-family taxonomy:

- Diagnostic artifact: `artifacts/bench/rag_diagnostics_20260424_1210_owner_family_taxonomy`
- Rows: `24`
- Bucket counts: `deterministic_pass` `4`, `expected_supported` `7`, `abstain_or_clarify_needed` `1`, `ranking_miss` `5`, `retrieval_miss` `7`.

RAG-S2 answer-card pilot also exists:

- Schema: `notes/specs/guide_answer_card_schema.yaml`
- Pilot cards: `6` cards under `notes/specs/guide_answer_cards/`
- Local YAML / required-field validation passed.

Android answer-card plumbing also started: `AnswerContent.kt` now has `evidenceForAnswerState`. The Android JVM test is still blocked in this sandbox by Android Gradle Plugin home setup / AGP home access, so treat the app-side result as code-path progress with validation pending outside this shell rather than a product failure.

Python validation after the owner-rerank patch:

- `python -B -m py_compile bench.py scripts\analyze_rag_bench_failures.py tests\test_bench_runtime.py tests\test_analyze_rag_bench_failures.py` passed.
- `python -B -m unittest tests.test_bench_runtime tests.test_analyze_rag_bench_failures -v` passed: `20` tests.
- `python -B -m unittest tests.test_uncertain_fit tests.test_query_routing tests.test_bench_artifact_tools -v` passed: `45` tests.

Next RAG-S4/RAG-S5 proof needed:

- app acceptance metrics for answer modes, decision paths, and immediate uncertainty cards;
- use answer-card/evidence-owner contracts before touching runtime rerank;
- inspect remaining ranking/retrieval misses for expectation mismatch first;
- reranker/evidence owner ranking only after expected owners/contracts are trusted;
- retrieval-profile contract hardening, especially safety-profile metadata;
- guide metadata and citation-owner quality so evidence acceptance can trust the right owners;
- answer-card/claim-support work for any remaining safety-contract miss.

## Caveats

- Current analyzer uses paired Markdown source rows and guide-title mapping to infer candidate guide IDs when JSON lacks raw ranked retrieval rows.
- The expectation manifest is a seed, not a clinical truth source. It should be reviewed as answer cards land.
- Diagnostic buckets are heuristic; they are good enough for backlog routing, not final pass/fail grading.

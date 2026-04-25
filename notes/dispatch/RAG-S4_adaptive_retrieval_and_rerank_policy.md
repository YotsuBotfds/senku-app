# Slice RAG-S4 - adaptive retrieval and rerank policy

- **Status:** foundation and S4b/S4c target-wave cleanup completed for the current proof set. Retrieval profile labels and safety/normal-vs-urgent supplemental retrieval lanes are implemented; analyzer/app-gate fields are visible; the current best proof is `artifacts/bench/rag_diagnostics_20260424_1410_child_choking_gate/report.md`.
- **Role:** main agent (`gpt-5.5 medium`; use high for safety-owner priority changes).
- **Paste to:** after `RAG-S1` and preferably after at least one retrieval diagnostic report.
- **Why this slice now:** one retrieval profile cannot serve safety triage, ordinary how-to, guide-family comparison, and weak-support questions equally well.

## Outcome

Introduce explicit retrieval profiles and an instruction-aware rerank policy layer using existing metadata before considering a model reranker.

## Candidate Profiles

- `safety_triage`
- `how_to_task`
- `compare_or_boundary`
- `normal_vs_urgent`
- `low_support`

## Likely Touch Set

- `query.py`
- `tests/test_query_routing.py`
- maybe new config constants in `config.py`

## Acceptance

- Profile selection is visible in bench/review metadata.
- Analyzer/app-gate fields are visible: `answer_mode`, `support_strength`, `safety_critical`, `retrieval_profile`, and `app_gate_status`; reports include an App Gates section.
- Safety-triage profile prioritizes emergency/focused guides above routine broad guides when red flags are present.
- Low-support profile can force abstain/clarify instead of confident generation.
- Focused tests prove at least one prompt per profile.

## Next Validation Step

Historical proof points:

- `artifacts/bench/rag_diagnostics_20260424_1210_owner_family_taxonomy`
- `24` rows: `4` deterministic pass, `7` `expected_supported`, `1` `abstain_or_clarify_needed`, `5` ranking misses, `7` retrieval misses.
- `artifacts/bench/rag_diagnostics_20260424_1240_fresh_answer_card_family_contract`
- `24` rows: `6` deterministic pass, `10` `expected_supported`, `2` `abstain_or_clarify_needed`, `5` ranking misses, `1` retrieval miss; expected-guide rates hit@1 `17/24`, hit@3 `22/24`, hit@k `23/24`, cited `21/24`.
- `artifacts/bench/rag_diagnostics_20260424_1232_post_s4b_runtime_card`
- `24` rows: `7` deterministic pass, `11` `expected_supported`, `2` `abstain_or_clarify_needed`, `4` ranking misses, `0` retrieval misses; expected-guide rates hit@1 `19/24`, hit@3 `23/24`, hit@k `24/24`, cited `22/24`.
- Prior proof: `artifacts/bench/rag_diagnostics_20260424_1408_card_planned_guarded_airway/report.md`
- `24` rows: `12` deterministic pass, `10` `expected_supported`, `2` `abstain_or_clarify_needed`; retrieval/ranking/generation/safety-contract misses are all `0`; expected-guide rates hit@1 `21/24` (`87.5%`), hit@3 `24/24`, hit@k `24/24`, cited `24/24`; expected owner cited `24/24`.
- Current superseding proof: `artifacts/bench/rag_diagnostics_20260424_1410_child_choking_gate/report.md`
- `24` rows: `13` deterministic pass, `9` `expected_supported`, `2` `abstain_or_clarify_needed`; retrieval/ranking/generation/safety-contract misses are all `0`; expected-guide rates hit@1 `21/24` (`87.5%`), hit@3 `24/24`, hit@k `24/24`, cited `24/24`; expected owner cited `24/24`.
- App acceptance is now `15` strong, `7` moderate, `2` uncertain; answer cards are `15` no-generated-answer, `7` partial, `2` pass; claim support is `15` no-generated-answer, `9` pass. The former residual claim partial is resolved by source-content hygiene and prompt/card guardrails, and `EX` #2 now uses the existing choking safety gate instead of generated infant-only phrasing.

The analyzer now classifies correctly retrieved/cited expectation rows as `expected_supported` instead of `rag_unknown_no_expectation`. EZ newborn danger-sign primary family owners are `GD-492`, `GD-298`, `GD-617`, and `GD-284`; `GD-589` / `GD-232` stay backup/general support.

The remaining EX/EY/EZ/FC backlog is no longer retrieval/ranking. The next slices should expand or normalize answer-card clauses, tune generated answer structure, and keep guide-source contradictions visible as content hygiene defects. Runtime answer-card injection is proven for reviewed owner cards and supporting `source_sections` with token guard; the EZ top-k `6` run is the current lesson for weak-support newborn prompts. Runtime changes that moved the earlier 12:51 proof were narrow detector aliases and abdominal-owner rerank cleanup; do not add broad deterministic rules from this result. If a fresh EX/EY/EZ/FC bench is still needed after future card/content changes, rerun:

```powershell
.\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py <fresh EX/EY/EZ/FC json paths>
```

Compare expected-guide hit@1 / hit@3 against `artifacts/bench/rag_diagnostics_20260424_1000/`.

Future S4 regression triage should stay evidence-owner based, not score tuning:

- Produce a short table for each remaining miss: prompt id, expected owner(s), retrieved owner(s), cited owner(s), card owner if present, and disposition (`expectation mismatch`, `retrieval defect`, `ranking defect`, `citation-owner defect`, or `card gap`).
- Only promote runtime rerank changes for rows classified as true ranking defects.
- Route card gaps back to `RAG-S2` and unsupported generated-action rows to `RAG-S5`.
- Treat the `FC` prompt `do we watch at home or get urgent help first` as a prompt/expectation-contract issue unless the prompt file adds abdominal-trauma context; it is not a valid reason to add a broad `watch at home` abdominal predicate.

## Anti-Recommendations

- Do not introduce external reranker dependencies in the first slice.
- Do not tune global thresholds as a substitute for profile-specific behavior.

# Slice RAG-S10 - answer provenance labels

- **Status:** desktop artifact/analyzer provenance labels landed 2026-04-24.
- **Role:** main agent (`gpt-5.5 medium`; use high only for safety-critical UI wording or ambiguous reviewed-card ownership decisions).
- **Paste to:** after `RAG-S9` opt-in reviewed-card runtime proof.
- **Why this slice now:** `RAG-S9` proved reviewed-card-backed runtime answers can pass strict card/claim diagnostics, but they still looked like ordinary strong evidence unless a reviewer inferred meaning from `decision_path` and zero generation time. This slice makes answer provenance explicit before any Android UI changes.

## Outcome

Bench artifacts and analyzer diagnostics now distinguish:

- `reviewed_card_runtime`
- `generated_model`
- `deterministic_rule`
- `uncertain_fit_card`
- `abstain_card`
- `no_rag`
- `no_answer`

The analyzer also emits a future-facing surface label:

- `reviewed_card_evidence`
- `generated_evidence`
- `deterministic_rule`
- `limited_fit`
- `abstain`
- `no_rag`
- `no_answer`

## 2026-04-24 Landing

Implemented in `query.py`:

- `_card_backed_runtime_answer_plan(...)` returns deterministic reviewed-card answer text plus `card_ids`, `guide_ids`, `cited_guide_ids`, `review_status`, and `risk_tier`.
- `_card_backed_runtime_answer(...)` remains the compatibility wrapper that returns answer text only.
- Runtime behavior is still gated by `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`.

Implemented in `bench.py`:

- Prompt metadata and bench JSON now include:
  - `answer_provenance`
  - `reviewed_card_backed`
  - `reviewed_card_ids`
  - `reviewed_card_review_status`
  - `reviewed_card_guide_ids`
  - `reviewed_card_cited_guide_ids`
- Markdown/JSON summaries now count `answer_provenance`.

Implemented in `scripts/analyze_rag_bench_failures.py`:

- CSV/JSON/report fields now include:
  - `answer_provenance`
  - `reviewed_card_backed`
  - `answer_surface_label`
  - `reviewed_card_ids`
  - `reviewed_card_review_status`
  - `reviewed_card_guide_ids`
- `reviewed_card_runtime` rows are evaluable even with `generation_time=0`, using provenance rather than only `decision_path`.

Fresh native proof:

- Wave artifacts:
  - `artifacts/bench/guide_wave_ex_20260424_155936.json`
  - `artifacts/bench/guide_wave_ey_20260424_155941.json`
  - `artifacts/bench/guide_wave_ez_20260424_155950.json`
  - `artifacts/bench/guide_wave_fc_20260424_160016.json`
- Combined analyzer output:
  `artifacts/bench/rag_diagnostics_20260424_1601_rags10_provenance_native/report.md`

Counts over `24` EX/EY/EZ/FC rows:

- deterministic pass `13`
- expected supported `9`
- abstain/clarify needed `2`
- retrieval/ranking/generation/safety-contract misses `0`
- hit@1 `21/24`
- hit@3 `24/24`
- hit@k `24/24`
- cited `24/24`
- generated prompts `6`
- reviewed card-backed answers `3`
- safety trims `0`

Provenance counts:

- `deterministic_rule`: `13`
- `generated_model`: `6`
- `reviewed_card_runtime`: `3`
- `uncertain_fit_card`: `2`

Surface label counts:

- `deterministic_rule`: `13`
- `generated_evidence`: `6`
- `reviewed_card_evidence`: `3`
- `limited_fit`: `2`

The native artifacts identify reviewed-card runtime answers from:

- `choking_airway_obstruction` (`pilot_reviewed`)
- `abdominal_internal_bleeding` (`pilot_reviewed`)

Residual Generated vs Shadow gaps are unchanged:

- `EY` #6 meningitis generated partial.
- `EZ` #1-#5 newborn generated partials due to the missing warmth line.

## Planner Read

This slice keeps Android UI untouched while stabilizing the contract Android should eventually consume. The product distinction is now explicit: reviewed card-backed answers are not merely generated strong-evidence answers with faster runtime; they are deterministic compositions from reviewed card contracts with card IDs and review status attached.

Next best work:

- fix or route the residual generated-vs-shadow gaps (`EY` #6 and `EZ` #1-#5);
- expand reviewed cards only with source-invariant checks and analyzer proof;
- design the Android label mapping from `answer_surface_label` after the desktop/analyzer contract remains stable.

## Acceptance

- Bench JSON carries provenance and reviewed-card metadata.
- Analyzer CSV/report distinguishes reviewed-card evidence from generated evidence.
- Fresh EX/EY/EZ/FC proof preserves the clean bucket/citation rates.
- Android UI remains unchanged.

## Boundaries

- Do not enable reviewed-card runtime answers by default.
- Do not surface new Android labels until the artifact/analyzer contract is stable.
- Do not treat `support_strength=strong` as equivalent to reviewed-card provenance.
- Do not populate reviewed-card metadata for generated answers.

## Report Format

- Files changed.
- Fresh analyzer artifact path and provenance counts.
- Focused and broad validation commands.
- Remaining generated-vs-shadow gaps.

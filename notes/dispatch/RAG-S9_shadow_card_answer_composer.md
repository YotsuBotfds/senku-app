# Slice RAG-S9 - shadow card answer composer

- **Status:** shadow composer and disabled-by-default runtime hook landed 2026-04-24.
- **Role:** main agent (`gpt-5.5 medium`; use high only for safety-critical production-answer wording or card ownership disputes).
- **Paste to:** after `RAG-S8` evidence packets are available and the analyzer has current EX/EY/EZ/FC artifacts.
- **Why this slice now:** S8 proved that reviewed cards can feed generation, but generated answers still often miss strict required-action phrasing. This slice tests whether direct card-backed composition closes that structure gap before changing production behavior.

## Outcome

Build a deterministic shadow answer from reviewed answer cards:

- required first actions first;
- active conditional requirements from prompt/context;
- limited supporting first actions;
- conservative negative safety lines only when they do not trigger forbidden-advice diagnostics;
- limited red flags;
- inline owner citations only for allowed primary card guide IDs.

The analyzer compares that shadow answer against the same answer-card and claim-support diagnostics used for generated answers.

Runtime use is still off by default. The first hook is gated by
`SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`, runs only after abstain /
uncertain-fit handling, and only short-circuits model generation for exactly one
reviewed high-risk card with an allowlisted primary owner citation.
The latest opt-in runtime patch changed `_card_backed_runtime_answer(...)` so
runtime card conditionals activate from the question text only, not from
retrieved document context.

## 2026-04-24 Landing

Implemented in `guide_answer_card_contracts.py`:

- `compose_card_backed_answer(...)` composes a deterministic answer plan and answer text from reviewed cards.
- The helper uses evidence units internally, preserves card order, caps cards, and returns `status`, `card_ids`, `guide_ids`, `cited_guide_ids`, `lines`, and `answer_text`.
- Citations are constrained to the primary card guide ID and omitted if the allowed guide list does not include that primary owner.
- Negative safety lines are filtered through the forbidden-advice checker before inclusion, so clausal "do not" phrases that look like positive forbidden advice are skipped.

Implemented in `scripts/analyze_rag_bench_failures.py`:

- Row/CSV fields:
  - `shadow_card_answer_status`
  - `shadow_claim_support_status`
  - `shadow_claim_action_count`
  - `shadow_card_answer_cited_guide_ids`
- JSON rows also include `shadow_card_answer_text`.
- The report includes a `Shadow Card Composer` section and prompt-table shadow status.
- The same analyzer change also surfaces `completion_safety_trimmed` in CSV/JSON/report workload counts.

Implemented in `query.py`:

- `_card_backed_runtime_answer(...)` returns a deterministic reviewed-card
  answer only when `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`.
- The hook requires exactly one selected `pilot_reviewed` / `approved` card,
  `critical` / `high` risk tier, an allowlisted primary guide owner, ready
  composed text, and at least one emitted citation.
- Runtime card conditionals now activate from the user's question text only, so
  retrieved context cannot accidentally turn on a branch that the prompt did
  not ask for.
- `stream_response(...)` calls the hook after abstain / uncertain-fit handling
  and before prompt construction / model generation.
- Production default remains unchanged.

Fresh shadow proof:

- Diagnostic artifact: `artifacts/bench/rag_diagnostics_20260424_1530_rags9_shadow_card_composer/report.md`
- Same 24-row EX/EY/EZ/FC set as the final S8 proof.
- Bucket counts remain clean: deterministic pass `13`, expected supported `9`, abstain/clarify needed `2`, with `0` retrieval/ranking/generation/safety-contract misses.
- Workload now reports `1` safety-trimmed completion.
- Generated-answer card diagnostics remain `15` no-generated-answer, `7` partial, `2` pass.
- Shadow composer diagnostics are `24` card-answer pass and `24` claim-support pass.
- Gap-summary artifact:
  `artifacts/bench/rag_diagnostics_20260424_1535_rags9_generated_shadow_gaps/report.md`
  preserves the same bucket counts and adds `Generated vs Shadow Card Gaps`.
  It finds `7` generated rows where the generated answer is card partial but
  the shadow answer passes. Top missing clauses: newborn warm/monitor while
  arranging urgent evaluation (`5`), choking branch discriminator actions
  (`1` each), and meningitis/antibiotic-capable escalation pair (`1` each).

Fresh opt-in runtime proof:

- Wave artifacts:
  - `artifacts/bench/guide_wave_ex_20260424_154606.json`
  - `artifacts/bench/guide_wave_ey_20260424_154611.json`
  - `artifacts/bench/guide_wave_ez_20260424_154621.json`
  - `artifacts/bench/guide_wave_fc_20260424_154647.json`
- Combined analyzer output:
  `artifacts/bench/rag_diagnostics_20260424_1547_rags9_card_backed_runtime_optin_fixed/report.md`
- Bucket counts remain clean: deterministic pass `13`, expected supported `9`,
  abstain/clarify needed `2`, with `0` retrieval/ranking/generation/safety-contract
  misses.
- Expected-guide rates: hit@1 `21/24`, hit@3 `24/24`, hit@k `24/24`, cited
  `24/24`.
- Workload: `13` deterministic, `2` uncertain_fit, `3` card_backed_runtime,
  `6` rag, `6` generated prompts, and no safety trims.
- Generated-answer card diagnostics are `15` no-generated-answer, `3` pass,
  and `6` partial.
- Generated claim support remains `15` no-generated-answer and `9` pass.
- Shadow card composer remains `24` card-answer pass and `24` claim-support
  pass.
- Residual Generated vs Shadow gaps are `EY` #6 meningitis generated partial
  and `EZ` #1-#5 newborn generated partials due to the missing warmth line.

Validation:

- Focused card/analyzer tests passed: `45` tests.
- Full query-routing suite passed: `52` tests.
- Broad desktop suite passed: `228` tests.
- `scripts/validate_special_cases.py` validated `173` deterministic rules.
- `scripts/validate_guide_answer_cards.py` validated `6` cards.

## Planner Read

This is a strong signal that the reviewed-card layer can produce complete guide-grounded answers when the answer shape is controlled. The current bottleneck is not retrieval/ranking for the proof set, and not evidence availability for the six pilot families; it is the production path's reliance on the model to rediscover every required clause from the evidence packet. The opt-in runtime proof now shows that the guarded runtime path can short-circuit three reviewed high-risk prompts without changing the default behavior.

Next best work:

- keep the shadow diagnostics in place as a regression meter;
- expand reviewed cards where real guide families are missing;
- keep the narrow opt-in runtime path reviewed-card-only and question-text-only
  for conditional activation;
- decide how the UI/app should display deterministic card-backed answers versus generated answers;
- keep app acceptance and claim/card support diagnostics as gates.

## Acceptance

- Shadow composition is deterministic and tested without a model call.
- Analyzer reports shadow card and claim status without changing bucket classification.
- A fresh EX/EY/EZ/FC analyzer run shows whether direct card composition closes answer-card structure gaps.
- Production `query.py` answer behavior remains unchanged unless
  `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1` is explicitly set.

## Boundaries

- Do not enable card-backed answers by default yet.
- Do not use source-section backup guide IDs as invented inline answer owners.
- Do not make this an LLM judge or evaluator.
- Do not expand deterministic safety predicates to chase shadow-composer metrics.
- Do not apply this to unreviewed guide families until the card schema has a reviewed owner card.

## Delegation Hints

- Scout: inspect analyzer rows where generated answer-card status is partial but shadow is pass; summarize which clauses production misses most often.
- Worker: add a guarded runtime experiment only after planner approval, with a hard allowlist of reviewed pilot card IDs.
- Scout: review UI/app surfaces for where a deterministic card-backed answer could be labeled and cited distinctly from a generated answer.

## Report Format

- Files changed.
- Focused and broad validation commands.
- Fresh analyzer artifact path and counts.
- Whether production answer behavior changed.

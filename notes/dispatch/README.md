# `notes/dispatch/`

Editable slice prompts for main-agent dispatch. One file per slice. Each
file is meant to be opened, tweaked, and copy-pasted into a fresh main
agent session. Treat them as living - if a slice evolves mid-session,
edit the file, do not let the drift live only in your head.

## Convention

- Filename: `<slice_id>_<short_name>.md` (e.g. `P1_planner_cleanup.md`).
- Slice IDs map 1:1 to rows in `notes/CP9_ACTIVE_QUEUE.md`.
- Each file has: `Slice`, `Role`, `Preconditions`, `Outcome`, `Boundaries`,
  `Acceptance`, `Delegation hints`, `Report format`.
- Role defaults to main agent / general scout-worker routing on
  `gpt-5.5 medium`; use high reasoning only for delicate safety predicates,
  safety-critical answer wording, or ambiguous ownership decisions.
- Delegation hints are **suggestions**, not orders. Main agent owns the
  final routing choice per `notes/SUBAGENT_WORKFLOW.md`.
- For slices that touch external framework/library behavior (Android
  SDK, Compose, Gradle, Python libs), include a one-line hint in
  `Anti-recommendations` (or wherever natural) pointing Codex at the
  relevant MCP - e.g. context7 for Android/Compose/library API docs,
  git MCP for repo-history questions, sequential-thinking for
  genuinely ambiguous decomposition. Codex has context7, git, and
  sequential-thinking registered in `~/.codex/config.toml` but
  doesn't always reach for them unprompted. Especially valuable
  when the symptom involves framework behavior (IME, focus,
  lifecycle, recomposition) - without the hint, Codex tends to
  layer defensive code on top of assumptions rather than checking
  authoritative docs (R-ui2 v1->v2->v3 is the cautionary tale).

## Current Status / Historical Dispatch Ledger

No active non-Android RAG/tooling slices are currently in flight.

CP9 is closed. RC v5 cut landed 2026-04-20, Wave C is closed through
`W-C-4`, and the desktop deterministic guide wave is paused after the
2026-04-24 method review.

GitHub/tooling status, 2026-04-26: the repository has been created and pushed
to `https://github.com/YotsuBotfds/senku-app.git`; local `master` tracks
`origin/master`; the latest tooling-infrastructure proof recorded here is
`7f8e774`. The 2026-04-26 tooling run is landed through retrieval-profile
comparison: non-Android GitHub regression gate, Actions security lint, workflow
attestation bundle, bench metrics lake, RAG trace diagnostics and bench spans,
PowerShell quality gate, optional RAG eval dataset exporter, retrieval-profile
comparison tooling, the accepted partial-router drift allowlist for strict gate
runs, and single-step CI-side retrieval index rebuilds are closed unless a new
regression artifact reopens them.

Current RAG status:

- The RAG-S/RAG-T/RAG-EVAL/RAG-TOOL wave recorded below is landed history,
  not an active queue. `RAG-S1` through `RAG-S22`, `RAG-T12`, the
  RAG-EVAL partial/router cleanup, and the RAG-TOOL utilities are closed unless
  a fresh regression artifact reopens one.
- Current non-Android baseline is
  `artifacts/bench/rag_diagnostics_20260425_1708_runtime_card_id_filter/`
  for FA/FB/FD/FE and
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag/report.md`
  for partial/router held-outs. The latter supersedes the earlier
  `post_fixes`, `metadata_visible`, and `contextual_index` partial/router
  waypoints.
- `RAG-EVAL3`, `RAG-EVAL5`, GD-024/GD-029/GD-035/GD-052/GD-397/GD-646/
  GD-648/GD-649 source packaging, bridge metadata routing for
  GD-634/GD-635/GD-636, and `RAG-TOOL6` incremental ingest planning are landed.
- `notes/AGENT_TOOLING_UPGRADES_20260426.md` is now a completed tooling
  status note through runtime endpoint preflight, worker worktree lanes,
  non-Android GitHub regression workflow, Actions security lint, attested
  non-Android regression bundle, bench metrics lake, RAG trace core/bench
  spans, PowerShell quality gate, RAG eval dataset export, and
  retrieval-profile comparison tooling plus the accepted partial-router drift
  allowlist for strict gate runs.
- Android reviewed-card/runtime work remains a separate lane; do not infer
  Android implementation work from this non-Android status reconciliation.

Historical landed ledger:

- `RAG-S1` produced the first diagnostic result at
  `notes/RAG_S1_DIAGNOSTIC_RESULT_20260424.md`.
- `RAG-S1b` observability has landed in the same work block.
- `RAG-S4` foundation, `S4b`, and `RAG-S2` pilot cards now exist; the latest
  steering artifact is
  `artifacts/bench/rag_diagnostics_20260424_1455_card_clause_invariants/report.md`.
- `RAG-S5a` has analyzer claim-support diagnostics from `rag_claim_support.py`;
  `RAG-S6a` has analyzer-only app acceptance fields. Use those gates before
  adding more deterministic patches.
- `RAG-S7` source-content invariants have initial validator support and tiny
  invariants on the critical pilot cards.
- `RAG-S8` has an initial runtime evidence-unit / citation-first composer:
  reviewed cards now become ordered prompt evidence with citation anchors,
  active conditionals, source metadata, and labeled support phrases. The fresh
  generated-answer proof is
  `artifacts/bench/rag_diagnostics_20260424_1516_rags8_evidence_packet_trimmed/report.md`;
  clean retrieval/ranking/generation/safety buckets were preserved, and the
  `FC` #3 malformed LiteRT stop is now handled by safety-tail retry/trim.
- `RAG-S9` has a shadow card-answer composer plus a disabled-by-default runtime hook:
  `compose_card_backed_answer(...)` can deterministically compose reviewed
  card answers, and the analyzer reports shadow card/claim status plus
  `completion_safety_trimmed`. The latest opt-in runtime proof is
  `artifacts/bench/rag_diagnostics_20260424_1547_rags9_card_backed_runtime_optin_fixed/report.md`;
  it preserves deterministic pass `13`, expected supported `9`,
  abstain/clarify `2`, hit@1 `21/24`, hit@3/hit@k/cited `24/24`,
  with workload `13` deterministic, `2` uncertain_fit, `3`
  card_backed_runtime, `6` rag, `6` generated prompts, and no safety trims.
  Runtime card conditionals now activate from question text only, not retrieved
  document context. Generated-vs-shadow residuals are `EY` #6 meningitis
  generated partial and `EZ` #1-#5 newborn generated partials from the missing
  warmth line; shadow card-backed answers remain `24/24` card pass and `24/24`
  claim pass. Runtime card-backed answers remain off unless
  `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1` is set.
- `RAG-S10` has desktop artifact/analyzer provenance labels for the runtime
  card-backed path. Bench JSON now carries `answer_provenance`,
  `reviewed_card_backed`, reviewed card IDs/status/guide IDs, and the analyzer
  reports `answer_surface_label` so `reviewed_card_evidence` is distinct from
  `generated_evidence`. Native proof:
  `artifacts/bench/rag_diagnostics_20260424_1601_rags10_provenance_native/report.md`;
  counts remain deterministic pass `13`, expected supported `9`,
  abstain/clarify `2`, hit@1 `21/24`, hit@3/hit@k/cited `24/24`, with
  provenance `13` deterministic_rule, `6` generated_model, `3`
  reviewed_card_runtime, and `2` uncertain_fit_card. Android UI is intentionally
  unchanged until this contract is consumed in a dedicated app slice.
- `RAG-S11` has reviewed source-family runtime coverage for newborn danger
  signs without making backup citations global. The newborn card now reviews
  `GD-492`, `GD-298`, and `GD-617` as source-family owners and opts into
  `runtime_citation_policy: reviewed_source_family`; other cards keep the
  primary-owner-only default. Proof:
  `artifacts/bench/rag_diagnostics_20260424_1625_rags11_newborn_source_family/report.md`;
  counts remain deterministic pass `13`, expected supported `9`,
  abstain/clarify `2`, retrieval/ranking/generation/safety-contract misses `0`,
  hit@3/hit@k/cited `24/24`, with `8` reviewed-card runtime answers and only
  `1` generated prompt. The remaining generated-vs-shadow gap is the ambiguous
  `EY` #6 meningitis vs viral comparison, intentionally not a deterministic
  emergency fallback.
- `RAG-S12` has resolved the ambiguous `EY` #6 meningitis vs viral comparison
  without broad emergency fallback. Bare comparison prompts stay generated RAG,
  use if/then clinical comparison shape, cite retrieved clinical owner-family
  evidence, and surface as `not_applicable_compare` rather than strict
  emergency-card failures; fever-plus-stiff-neck comparison prompts still use
  the strict meningitis emergency-card lane. Proof:
  `artifacts/bench/rag_diagnostics_20260424_1659_rags12_meningitis_compare_final/report.md`;
  counts remain deterministic pass `13`, expected supported `9`,
  abstain/clarify `2`, retrieval/ranking/generation/safety-contract misses `0`,
  hit@3/hit@k/cited `24/24`, with `8` reviewed-card runtime answers, `1`
  generated prompt, and `0` generated-vs-shadow card gaps.
- `RAG-S13` has landed the first code-health extraction pass after the RAG
  behavior proof. Weak-retrieval abstain scoring moved to
  `query_abstain_policy.py`; reviewed-card runtime helpers moved to
  `query_answer_card_runtime.py`; analyzer row diagnostic enrichers moved to
  `rag_bench_answer_diagnostics.py`; source-owner citation policy moved to
  `query_citation_policy.py`; pure completion-shape guards moved to
  `query_completion_hardening.py`; response/citation normalization moved to
  `query_response_normalization.py`; shared query/bench prompt-budget helpers
  moved to `query_prompt_runtime.py`. `query.py` and
  `scripts/analyze_rag_bench_failures.py` keep compatibility wrappers / imported
  symbols for existing tests and patch points. Proof:
  `artifacts/bench/rag_diagnostics_20260424_1750_rags13_code_health_final_smoke/report.md`;
  counts remain deterministic pass `13`, expected supported `9`,
  abstain/clarify `2`, retrieval/ranking/generation/safety-contract misses `0`,
  hit@3/hit@k/cited `24/24`, with `8` reviewed-card runtime answers, `1`
  generated prompt, and `0` generated-vs-shadow card gaps. Do not treat this as
  permission to extract `build_prompt(...)` or the medical predicate jungle
  without a new scoped slice. Android does not consume these Python modules
  automatically; carry the reviewed-card/provenance contract into Android via a
  dedicated Kotlin/mobile-pack slice.
- `RAG-S14` has landed analyzer-only evidence nugget diagnostics. The analyzer
  now reports evidence nugget status plus total/present/cited/supported/missing/
  contradicted counts and top missing/contradicted phrases through CSV and
  Markdown summaries. This is measurement only: no runtime query, retrieval,
  prompt, guide, or Android behavior changed. Dispatch note:
  `notes/dispatch/RAG-S14_nugget_evidence_diagnostics.md`.
- `RAG-S15` has landed a non-default contextual-chunk shadow export lane.
  Output is ingest-only JSONL with raw `document`,
  `contextual_retrieval_text`, `metadata`, and `chunk_id` fields for analysis,
  with no production Chroma, embedding, lexical, query, guide, or Android
  behavior changes. Dispatch note:
  `notes/dispatch/RAG-S15_contextual_chunk_shadow_export.md`.
- `RAG-S15b` has landed a measurement-only contextual-shadow retrieval
  comparator. It builds temporary shadow vector/lexical indexes from the S15
  JSONL export, compares existing bench baselines against shadow retrieval
  hit@1/hit@3/hit@k, and writes summary JSON, row JSONL, and CSV outputs.
  Rows with no baseline retrieval candidates are unscored for baseline deltas,
  so shadow-only coverage is visible without being counted as fake improvement.
  Runtime query, production indexes, guides, and Android behavior remain
  unchanged. Dispatch note:
  `notes/dispatch/RAG-S15b_contextual_shadow_retrieval_comparator.md`.
- `RAG-S16` has landed a shadow-only section-family / RAPTOR-lite export for
  later retrieval-comparison experiments. It writes non-production JSONL records
  with `section_family`, `contextual_retrieval_text`, `raptor_lite_text`, and
  metadata, supports optional overlapping windows through `--family-stride`,
  and the S15b comparator now accepts those records. Production query/index/guide
  behavior and Android behavior remain unchanged. Dispatch note:
  `notes/dispatch/RAG-S16_section_family_shadow_export.md`.
- `RAG-S16c` adds primary-owner concentration diagnostics to the shadow
  comparator so broad expected-guide families cannot hide drift toward backup
  support guides. Dispatch note:
  `notes/dispatch/RAG-S16c_owner_family_concentration_metric.md`.
- `RAG-S17` lets the reviewed-card runtime answer before uncertain-fit copy
  when the existing strict planner can produce a ready cited answer. Abstain
  still wins first, the runtime remains opt-in, and no app-gate/product surface
  relabeling is included. Fresh EX/EY/EZ/FC proof moves reviewed-card runtime
  answers from `8` to `10`, answer-card pass from `8` to `10`, and
  claim-support pass from `9` to `11` with retrieval/ranking/generation/safety
  misses still `0`. Dispatch note:
  `notes/dispatch/RAG-S17_uncertain_fit_reviewed_card_fallback.md`.
- `RAG-S18` aligns unknown-ingestion / poisoning retrieval with the reviewed
  poisoning card. Possible medicine ingestion, mouth-burn-after-tasting, and
  unknown-ate-acting-off prompts now route through safety-triage retrieval,
  targeted toxicology supplemental search, owner-positive metadata rerank, and
  reviewed source-family card citation. Fresh FA proof:
  `artifacts/bench/rag_diagnostics_20260425_1431_fa_poison_card_alignment/report.md`.
  Dispatch note:
  `notes/dispatch/RAG-S18_poisoning_card_retrieval_alignment.md`.
- `RAG-S19` lets the reviewed infected-wound card answer when `GD-585` is
  retrieved outside the top-two card-selection window. FB #6 now uses
  `card_backed_runtime`, cites `GD-585`, and passes answer-card, claim, and
  evidence-nugget checks while preserving the visible rank-4 owner drift for a
  later ranking slice. Fresh FB proof:
  `artifacts/bench/rag_diagnostics_20260425_1445_fb_wound_card_runtime/report.md`.
  Dispatch note:
  `notes/dispatch/RAG-S19_infected_wound_reviewed_card_runtime.md`.
- `RAG-S20` updates analyzer taxonomy so strict reviewed-card runtime rows that
  cite an expected owner and pass answer-card, claim-support, and evidence
  nugget checks count as `expected_supported` even when the expected owner was
  not rank 1. Generated rank drift remains `ranking_miss`, and owner drift
  remains visible through the `RAG-T4` owner concentration metrics. Fresh
  FA/FB/FD/FE taxonomy proof:
  `artifacts/bench/rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy/report.md`;
  diagnostic bad buckets are now `0`, quality score is `8.83`, and owner
  concentration remains best-rank `1.36`, top-3 `21/24`, top-k `22/24`.
  Dispatch note:
  `notes/dispatch/RAG-S20_reviewed_card_rank_drift_taxonomy.md`.
- `RAG-S21` extends the existing `mania_no_sleep_immediate_safety`
  deterministic predicate to cover the FD dangerous-activation phrasings while
  preserving a stress/anxiety insomnia near-miss guard. Fresh FD proof:
  `artifacts/bench/guide_wave_fd_20260425_150350.json`; combined FA/FB/FD/FE
  proof:
  `artifacts/bench/rag_diagnostics_20260425_1503_fa_fb_fd_fe_fd_deterministic/report.md`.
  Quality score is now `9.37`, generated rows are `0/24`, deterministic rows
  are `18/24`, and bad diagnostic buckets remain `0`. Dispatch note:
  `notes/dispatch/RAG-S21_dangerous_activation_deterministic_extension.md`.
- `RAG-S22` routes "toddler swallowed an unknown pill" through
  `unknown_child_ingestion` instead of generic `unknown_medication`, while
  preserving found-medication guidance for unmarked pills that were not
  swallowed. With reviewed-card runtime enabled, combined FA/FB/FD/FE proof is
  `artifacts/bench/rag_diagnostics_20260425_1513_fa_child_pill_runtime_fd_deterministic/report.md`.
  Quality score is now `9.56`, generated rows remain `0/24`, and app
  acceptance no longer has a `needs_evidence_owner` row. Dispatch note:
  `notes/dispatch/RAG-S22_child_unknown_pill_poisoning_boundary.md`.
- `RAG-T1` and the follow-on tooling utilities are landed; retained dispatch
  links below are historical. The first targets were shadow-comparison
  summaries, agent run manifests, and RAG trend tables so later
  retrieval/card/evidence slices spent less time manually opening artifacts.
  Strategy note:
  `notes/AGENT_TOOLING_UPGRADES_20260425.md`. First utility dispatch:
  `notes/dispatch/RAG-T1_shadow_comparison_summarizer.md`. Diagnostics trend
  utility dispatch:
  `notes/dispatch/RAG-T2_rag_diagnostics_trend_summarizer.md`. Run-manifest
  writer dispatch:
  `notes/dispatch/RAG-T3_run_manifest_writer.md`. Owner-family concentration
  metric dispatch:
  `notes/dispatch/RAG-T4_owner_family_concentration_metric.md`.
- `RAG-T5` surfaces reviewed-card runtime-answer enablement in guide validation
  artifacts and diagnostics so comparable 9.x panels cannot accidentally mix
  opt-in reviewed-card runtime rows with plain RAG rows. Dispatch note:
  `notes/dispatch/RAG-T5_reviewed_card_runtime_validation_harness.md`.
- `RAG-DOCS1` adds the first static corpus marker scanner from the deep
  research backlog. It found `55` unresolved partial markers in `55` guides
  and writes JSON/Markdown reports for packaging/eval follow-up. Dispatch note:
  `notes/dispatch/RAG-DOCS1_corpus_marker_scanner.md`.
- `RAG-EVAL1` lets RAG diagnostics consume that marker scan with top-1 marker
  risk, bridge, unresolved-partial, and marker-type columns. Fresh FA/FB/FD/FE
  overlay stayed at `9.56/10` and showed no top-1 bridge or unresolved
  partial rows. Dispatch note:
  `notes/dispatch/RAG-EVAL1_corpus_marker_diagnostic_overlay.md`.
- `RAG-EVAL2` adds a held-out prompt pack (`RE2-UP-*`, `RE2-BR-*`) to directly
  test primary-answer regressions where top-1 retrieval is partial-bearing or
  thin bridge. Latest diagnostics found 2 top-1 unresolved-partial rows, 1
  top-1 bridge row, 8 retrieval misses, and 4 ranking misses. See:
  `notes/specs/rag_eval_partial_router_holdouts_20260425.md` and
  `notes/dispatch/RAG-EVAL2_partial_router_holdout_pack.md`.
- `RAG-RUNTIME1` resolved model/runtime naming. `gemma-4-e2b-it-litert` is the
  canonical local LiteRT generation target ID for the current desktop and guide
  validation lanes, distinct from public model IDs and broad-quality overrides.
  Decision note: `notes/RUNTIME_TARGET_NAMING_20260425.md`. Dispatch note:
  `notes/dispatch/RAG-RUNTIME1_gemma_litert_target_naming.md`.
- `RAG-TRUST1` drafted Senku's trust model and high-risk answer policy,
  including can-answer/cannot-verify/escalate modes, `uncertain_fit` vs
  `abstain`, citation/provenance expectations, offline limits, and
  high-liability examples. Policy note:
  `notes/SENKU_TRUST_MODEL_HIGH_RISK_POLICY_20260425.md`. Dispatch note:
  `notes/dispatch/RAG-TRUST1_senku_trust_model_high_risk_policy.md`.
- `RAG-T6` adds the same top-1 marker overlay to `rag_trend.py` so session
  startup tables can compare marker risk, bridge rows, and unresolved-partial
  rows across diagnostic panels. Dispatch note:
  `notes/dispatch/RAG-T6_rag_trend_marker_overlay_columns.md`.
- `RAG-META1` adds a non-blocking high-liability metadata audit. First scan
  found `249` high-liability guides, `248` with at least one coverage gap, and
  only `6` reviewed answer-card guide surfaces. Dispatch note:
  `notes/dispatch/RAG-META1_high_liability_metadata_audit.md`.
- `RAG-T7` joins observed diagnostics, metadata audit, and corpus marker scan
  into a guide-quality priority table with candidate actions and reasons.
  Fresh panel ranked `18` observed guides, `16` high-liability. Dispatch note:
  `notes/dispatch/RAG-T7_guide_quality_priority_selector.md`.
- `RAG-T8` collapses the same evidence to expected-guide-family grain. Fresh
  panel ranked four families: wound partial repair first, then mental-health,
  stroke/cardiac overlap, and child-ingestion card expansion lanes. Dispatch
  note:
  `notes/dispatch/RAG-T8_high_liability_family_priority_selector.md`.
- `RAG-CH4` landed a narrow desktop code-health extraction after the Android
  A14 policy loop. Remaining pure bench completion-shape helpers moved into
  `query_completion_hardening.py`, with same-name wrappers kept in `bench.py`
  and direct helper coverage in `tests/test_query_completion_hardening.py`.
  No retry policy, prompt text, retrieval/ranking, `query.py`, or medical
  predicate behavior changed. Focused compile and unit validation passed
  `39` tests. Dispatch note:
  `notes/dispatch/RAG-CH4_desktop_completion_shape_hardening_extraction.md`.
- `RAG-CH6` added direct desktop helper coverage for `query_prompt_runtime.py`.
  `tests/test_query_prompt_runtime.py` now proves import purity, system-prompt
  builder/fallback behavior, runtime-profile prompt-limit errors, and
  chat-prompt token overhead with injected counting/resolver functions. No
  query/bench runtime behavior changed. Focused compile and unit validation
  passed `17` tests. Dispatch note:
  `notes/dispatch/RAG-CH6_desktop_prompt_runtime_tests.md`.
- `RAG-CH7` added direct desktop helper coverage for `query_citation_policy.py`.
  `tests/test_query_citation_policy.py` now proves import purity,
  safety-owner priority ordering, non-allergy airway narrowing, allergy
  non-narrowing, newborn owner-family ordering, and meningitis comparison
  owner filtering with injected predicates. No citation-policy behavior
  changed. Focused compile and unit validation passed `12` tests. Dispatch
  note:
  `notes/dispatch/RAG-CH7_desktop_citation_policy_tests.md`.
- `RAG-CH8` added direct desktop helper coverage for
  `rag_bench_answer_diagnostics.py`. `tests/test_rag_bench_answer_diagnostics.py`
  now proves import purity, compact phrase/basis helpers, family-card
  selection, bare-vs-red-flag meningitis compare behavior, and key
  app-acceptance classifications with injected detectors. No analyzer columns,
  bucket classifications, guide content, runtime behavior, or product behavior
  changed. Focused compile and unit validation passed `34` tests. Dispatch
  note:
  `notes/dispatch/RAG-CH8_desktop_bench_answer_diagnostics_tests.md`.
- `RAG-CH9` added direct desktop helper coverage for `query_scenario_frame.py`.
  `tests/test_query_scenario_frame.py` now proves import purity, injected
  domain/restart behavior, inventory-to-asset handling, placeholder inventory
  filtering, scenario extraction, session merge/update ordering, rendering, and
  session-context gating. No `query.py`, prompt, routing predicate, guide,
  Android, runtime, or product behavior changed. Focused scenario/routing
  validation passed `69` tests, and the broader deterministic lane passed
  `148` tests plus `173` rule validation. Dispatch note:
  `notes/dispatch/RAG-CH9_desktop_scenario_frame_tests.md`.
- `RAG-CH10` added direct desktop helper coverage for `query_routing_text.py`.
  `tests/test_query_routing_text.py` now proves import purity, phrase-marker
  matching, single-word word-boundary behavior, empty-input behavior, and the
  legacy `_text_has_marker` alias. No routing predicate, deterministic rule,
  guide, Android, runtime, or product behavior changed. Focused compile and
  routing validation passed `67` tests. Dispatch note:
  `notes/dispatch/RAG-CH10_desktop_routing_text_tests.md`.
- `RAG-A1` has landed the first Android receiving-shape slice. `AnswerContent`
  now carries display-only answer surface/provenance fields with safe defaults,
  and `PaperAnswerCard` can distinguish deterministic, limited-fit, and abstain
  labels without claiming reviewed-card evidence.
- `RAG-A2` has landed optional mobile-pack answer-card metadata tables:
  `answer_cards`, `answer_card_clauses`, and `answer_card_sources`, plus
  `counts.answer_cards` and `pack_meta.answer_card_count`. Android runtime
  behavior is unchanged; the next Android slice is a backward-compatible reader
  that returns empty card data for old packs.
- `RAG-A3` has landed that backward-compatible Android reader/model layer.
  `PackManifest` treats `counts.answer_cards` as optional, `PackRepository`
  exposes a narrow answer-card loader, and `AnswerCardDao` returns empty results
  for old packs without the optional tables. DAO instrumentation coverage was
  added and passed across the documented Senku matrix: `5556` phone portrait,
  `5560` phone landscape, `5554` tablet portrait, and `5558` tablet landscape.
- `RAG-A4` extracted the new mobile-pack answer-card export helpers into
  `mobile_pack_answer_cards.py`. `mobile_pack.py` keeps the private helper
  names as imported aliases, and the extracted module does not import
  `chromadb`.
- `RAG-A5` has landed the first disabled-by-default Android reviewed-card
  runtime pilot. `OfflineAnswerEngine` can compose the
  `poisoning_unknown_ingestion` card from `answer_card_clauses` after the
  model/host availability gate, only when the hidden
  `reviewed_card_runtime_enabled` preference is enabled, the query matches an
  unknown-ingestion/poisoning shape, exactly one `GD-898` card is selected, and
  the card status is `reviewed`, `pilot_reviewed`, or `approved`. DAO
  instrumentation now confirms `pilot_reviewed` compatibility on all four
  documented Senku AVD lanes, and full runtime instrumentation confirms
  `prepare(...)` returns `answer_card:poisoning_unknown_ingestion` from a temp
  answer-card pack before generation. This landed before the UI-surface bridge
  in `RAG-A6`.
- `RAG-A6` has landed that first UI-surface bridge without changing layout:
  `AnswerContent.fromAnswerRun(...)` now passes `ruleId` into surface
  inference, and deterministic `answer_card:` rule IDs surface as
  `ReviewedCardEvidence` / `reviewed_card_runtime` with
  `reviewedCardBacked=true`.
- `RAG-A7` extracted the Android reviewed-card runtime planner/composer into
  `AnswerCardRuntime` and added `ReviewedCardRuntimeConfig` over the existing
  hidden preference. No exported-activity intent override or visible toggle
  landed; connected runtime/config instrumentation passed across the documented
  Senku matrix.
- `RAG-A8` added a developer-panel toggle for that hidden preference. The
  button is not a normal product control, refreshes from
  `ReviewedCardRuntimeConfig` on resume, and phone-landscape developer content
  is now scrollable. Connected toggle instrumentation passed on the documented
  Senku matrix and screenshots live at
  `artifacts/android_reviewed_card_toggle_20260424_193049/`.
- `RAG-A9` added a reviewed-card prompt harness proof over the normal
  `MainActivity` scripted ask path. The trusted final run used the real
  answer-card probe pack after a fresh APK install and repush, and passed
  phone/tablet portrait/landscape with assertions for `REVIEWED EVIDENCE`,
  `answer_card:poisoning_unknown_ingestion`, `GD-898`, and poisoning/vomiting
  body fragments. Artifacts:
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_475/`
  and
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_566/`.
  That manual repush requirement is superseded by `RAG-A13` for the six
  pilot-reviewed cards now bundled in the checked-in asset pack.
- `RAG-A10` added explicit Android reviewed-card metadata through runtime,
  prepared answer, answer run, detail state, Paper-card content, and proof
  formatter state. The prompt harness now asserts card ID, card guide ID,
  review status, cited reviewed source guide IDs, and proof-summary visibility
  in addition to the A9 answer-surface checks. Trusted artifacts:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_371/`
  and
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_366/`.
  A same-block persistence follow-up records ready instant reviewed-card
  answers into recent threads and round-trips `ReviewedCardMetadata` through
  `SessionMemory` JSON.
- `RAG-A11a` expanded the disabled-by-default Android reviewed-card runtime to
  the `newborn_danger_sepsis` / `GD-284` card without changing product
  exposure. The bundled-pack prompt proof passed across tablet portrait, phone
  portrait, tablet landscape, and phone landscape on APK SHA
  `60bb6751d18f04f13dff4a6441c65f2cd59f98195ca243f26e5e591830582b5c`, asserting
  `REVIEWED EVIDENCE`, `answer_card:newborn_danger_sepsis`, card/source
  metadata, recent-thread metadata, and newborn danger body fragments. Dispatch
  note:
  `notes/dispatch/RAG-A11a_android_newborn_danger_reviewed_card_runtime.md`.
- `RAG-A11b` expanded the disabled-by-default Android reviewed-card runtime to
  the `choking_airway_obstruction` / `GD-232` card without changing product
  exposure. The bundled-pack prompt proof passed across tablet portrait, phone
  portrait, tablet landscape, and phone landscape on APK SHA
  `63126e427e21385f1e55e0451b81a32d768eb9ab63cc734b77efca723dd740af`,
  asserting `REVIEWED EVIDENCE`, `answer_card:choking_airway_obstruction`,
  card/source metadata, recent-thread metadata, and infant choking body
  fragments including the 5-back-blows / 5-chest-thrusts branch. Dispatch note:
  `notes/dispatch/RAG-A11b_android_choking_airway_reviewed_card_runtime.md`.
- `RAG-A11c` expanded the disabled-by-default Android reviewed-card runtime to
  the `meningitis_sepsis_child` / `GD-589` card without changing product
  exposure. The bundled-pack prompt proof passed across tablet portrait, phone
  portrait, tablet landscape, and phone landscape on APK SHA
  `8bee066f6ce3a988388cd60d767e70bb93c46bf8cf43a4329ddd7ff21171ddd4`,
  asserting `REVIEWED EVIDENCE`, `answer_card:meningitis_sepsis_child`,
  card/source metadata, recent-thread metadata, and red-flag meningitis/sepsis
  body fragments. Bare meningitis-vs-viral comparison prompts and
  public-health reporting/contact-tracing prompts remain generated or
  compare/clarify, not strict emergency-card fallback. Dispatch note:
  `notes/dispatch/RAG-A11c_android_meningitis_sepsis_child_reviewed_card_runtime.md`.
- `RAG-A11d` expanded the disabled-by-default Android reviewed-card runtime to
  the `infected_wound_spreading_infection` / `GD-585` card without changing
  product exposure. The bundled-pack prompt proof passed across tablet
  portrait, phone portrait, tablet landscape, and phone landscape on APK SHA
  `f1ae9a28e619069cbd8bc057070440e49948d30395e56d9fa727a2855c1de6d6`,
  asserting `REVIEWED EVIDENCE`,
  `answer_card:infected_wound_spreading_infection`, card/source metadata,
  recent-thread metadata, and spreading wound infection body fragments.
  Routine wound care, generic cellulitis education, newborn cord infection, and
  meningitis/sepsis rash prompts remain out of strict emergency-card fallback.
  Dispatch note:
  `notes/dispatch/RAG-A11d_android_infected_wound_reviewed_card_runtime.md`.
- `RAG-A11e` expanded the disabled-by-default Android reviewed-card runtime to
  the `abdominal_internal_bleeding` / `GD-380` card without changing product
  exposure, completing the checked-in six-card pilot runtime pack. The
  bundled-pack prompt proof passed across tablet portrait, phone portrait,
  tablet landscape, and phone landscape on APK SHA
  `3bd27a2aad4504328428aa78ec2e5e91414eafa05b5dfd1c918505264e249f2c`,
  asserting `REVIEWED EVIDENCE`, `answer_card:abdominal_internal_bleeding`,
  card/source metadata, recent-thread metadata, and abdominal trauma/internal
  bleeding body fragments. Pregnancy/ectopic prompts, routine GI prompts, and
  other emergency-card families remain out of strict emergency-card fallback.
  Dispatch note:
  `notes/dispatch/RAG-A11e_android_abdominal_internal_bleeding_reviewed_card_runtime.md`.
- `RAG-CH2` landed the post-six-card runtime code-health extraction:
  `AnswerCardRuntime` now uses an in-file `CardSpec` registry and shared
  planner helper for reviewed-card planning. Clinical predicates, marker sets,
  source construction, metadata, product gating, UI, and pack schema remain
  unchanged. Focused JVM/build checks, connected
  `OfflineAnswerEngineAnswerCardRuntimeTest`, and a phone prompt canary passed
  on APK SHA
  `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`.
  Dispatch note:
  `notes/dispatch/RAG-CH2_android_reviewed_card_runtime_planner_registry.md`.
- `RAG-A14a` landed a proof-only layout / pack readiness gate without changing
  product exposure. `AnswerCardAssetPackParityTest` now asserts shipped
  asset-pack answer-card tables, counts, expected card IDs, `pilot_reviewed`
  status, and clause/source coverage. Developer-panel collapsed/off/on
  screenshots and a four-posture poisoning reviewed-card prompt matrix live
  under `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/` on
  APK SHA
  `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`.
  Runtime remains off by default. Dispatch note:
  `notes/dispatch/RAG-A14a_android_reviewed_card_layout_pack_readiness.md`.
- `RAG-A14b` landed the proof-surface copy harmonization found by A14a
  screenshot review: reviewed-card meta strip, body/card label, and proof
  summary now use reviewed-card-specific trust copy instead of source-strength
  copy. Runtime remains off by default. Focused build/JVM checks and a
  four-posture poisoning reviewed-card prompt matrix passed under
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/` on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
  Dispatch note:
  `notes/dispatch/RAG-A14b_android_reviewed_card_proof_surface_copy.md`.
- `RAG-A14c` landed the forbidden-label prompt harness guard after A14b.
  `PromptHarnessSmokeTest` can now assert that reviewed-card prompts do not
  leak `STRONG EVIDENCE` and non-reviewed prompts do not leak
  `REVIEWED EVIDENCE`, checking UIAutomator visible text, accessibility
  descriptions, and settled detail signals. Runtime-on reviewed/non-reviewed
  phone canaries plus a runtime-off non-reviewed four-posture matrix passed
  under `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/` on APK
  SHA `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
  Runtime remains off by default. Dispatch note:
  `notes/dispatch/RAG-A14c_android_forbidden_reviewed_label_harness.md`.
- `RAG-A14d` closed exposure policy with Option A: keep reviewed-card runtime
  developer/test-only and default `off`. The current evidence does not approve
  product-default behavior, card expansion, top-level product UI,
  local-preview toggle, or runtime default change. Decision note:
  `notes/ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`;
  dispatch prompt:
  `notes/dispatch/RAG-A14d_android_reviewed_card_exposure_policy_closeout.md`.
- `RAG-CH3` landed the androidTest-only scripted harness contract extraction:
  `ScriptedPromptHarnessContract` now owns reviewed-card scripted Bundle
  parsing and pure fail-closed reviewed-evidence validation, while
  `PromptHarnessSmokeTest` keeps UI/activity assertions and artifact capture.
  Android test compile / test APK assembly plus phone reviewed and non-reviewed
  forbidden-label canaries passed under
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/`. Dispatch
  note:
  `notes/dispatch/RAG-CH3_android_scripted_harness_contract.md`.
- `RAG-CH5` added direct androidTest coverage for that extracted harness
  contract. `ScriptedPromptHarnessContractTest` now checks URL decoding,
  pipe-delimited argument parsing, legacy forbidden-label alias support,
  boolean parsing, and fail-closed `REVIEWED EVIDENCE` requirements without
  running the full prompt UI path. Android test compilation and the focused
  connected instrumentation class passed `7` tests on each attached Senku
  emulator lane. Dispatch note:
  `notes/dispatch/RAG-CH5_android_scripted_harness_contract_tests.md`.
- `RAG-A12` has a real-pack answer-card export probe at
  `artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`:
  `6` pilot cards, `116` clauses, `19` sources, no sourceless or clauseless
  cards. It did not overwrite app assets or install the pack.
- `RAG-A13` replaced the checked-in Android asset pack with that proven probe
  pack. Bundled manifest generated at `2026-04-25T00:08:46.459832+00:00`
  reports `answer_cards=6`; SQLite has `6` answer cards, `116` clauses, and
  `19` sources, all `pilot_reviewed` with no missing clause/source links.
  Clean-install no-push matrix proof passed with APK SHA
  `be32ff34f66e7d478082ae2bd292f0e8315f231ec9f55c4852debfdd0e3cc553` under
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/`.
  Runtime remains off by default and product enablement remains gated.
- `RAG-CH1` has landed a narrow Android code-health extraction:
  `DetailReviewedCardMetadataBridge` now owns reviewed-card metadata intent
  transport, current detail state, and rule-id guarded lookup, reducing future
  edits in `DetailActivity`. Behavior is unchanged; compile/unit checks and a
  clean-install no-push four-emulator reviewed-card matrix passed with APK SHA
  `7dae8277227c8f5ddc2997c412203da1309addff6e76f7f278e1e0734b80464c` under
  `artifacts/android_reviewed_card_bridge_matrix_20260424/`. Dispatch note:
  `notes/dispatch/RAG-CH1_android_detail_metadata_bridge.md`.
- `RAG-A14` dispatch exists at
  `notes/dispatch/RAG-A14_android_reviewed_card_product_enablement_gate.md`.
  It defines policy, proof-surface, screenshot/layout, pack/version, and
  validation gates before reviewed-card runtime can move beyond developer/test
  exposure. The first enforceable guard has landed: reviewed-card evidence
  labels now require deterministic `answer_card:` provenance, present
  reviewed-card metadata, non-empty cited reviewed source guide IDs, and
  visible source rows in the harness; old packs with missing answer-card tables
  fall back to null card planning. Runtime remains off by default, full product
  enablement remains open, and A14 does not expand cards. Prompt proof
  artifacts are under
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/20260424_213205_634/emulator-5556`
  and
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/clean_5554/20260424_213252_605/emulator-5554`
  on APK SHA
  `ffe6f23038d20f488c73f5166649b4319b3413e37d7dfd759037f8ef7541eaca`.
  These are reinstall/no-pack-push proofs, not clean-install proofs, because
  uninstall returned `DELETE_FAILED_INTERNAL_ERROR` before reinstall.
- Android reviewed-card continuation is organized in
  `notes/ANDROID_REVIEWED_CARD_RUNTIME_BACKLOG_20260424.md`; do not rediscover
  the next steps from scratch.
- `RAG-DOCS2` repaired the observed GD-585 `wound-assessment-tool` unresolved
  partial by replacing it with source-local wound assessment content. Fresh
  marker proof at
  `artifacts/bench/corpus_marker_scan_20260425_1622_gd585_repair.md` drops
  unresolved partials from `55` to `54`.
- `RAG-T9` refined the family-priority card columns so deterministic
  no-generation rows count as skipped/not evaluable instead of missing cards.
  Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1645_card_semantics.md`
  shows wound at `1/0/0/5` pass/gap/missing/skipped and changes the next wound
  action to `inspect_retrieval_ranking`.
- `RAG-T10` added FB support-owner expectations for `GD-235` and `GD-622` and
  separated uncertain-fit non-owner citations from true retrieval-ranking
  drift. Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1658_support_uncertainfit_semantics.md`
  moves the observed families to targeted metadata work.
- `RAG-META2` added frontmatter metadata for the dangerous-activation family
  (`GD-858`, `GD-859`, `GD-918`). Fresh proof at
  `artifacts/bench/metadata_coverage_audit_20260425_1724_dangerous_activation_metadata.md`
  shows aliases/routing/citation/applicability covered; the remaining family
  gap is reviewed answer-card coverage.
- `RAG-META3` added wound-family support metadata for `GD-232`, `GD-235`,
  `GD-589`, and `GD-622`. Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1732_wound_family_metadata.md`
  drops the wound score from `14` to `4`; remaining wound gaps are reviewed
  answer cards for `GD-235` and `GD-622`.
- `RAG-META4` added poisoning-family metadata for `GD-301` and `GD-898`.
  Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1738_poisoning_metadata.md`
  drops the poisoning score from `6` to `2`; remaining poisoning gap is a
  reviewed answer card for `GD-301`.
- `RAG-META5` added stroke/cardiac metadata for `GD-601`. Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1743_stroke_cardiac_metadata.md`
  drops the stroke/cardiac score from `4` to `2`; remaining gap is a reviewed
  answer card for `GD-601`.
- `RAG-T11` refined the family-priority selector so frontmatter metadata gaps
  and `missing_reviewed_answer_card` are reported separately. Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1648_reviewed_card_semantics.md`
  shows all four observed high-liability families at `0/N` frontmatter/card
  gaps with `consider_reviewed_answer_card`, preventing further false metadata
  churn.
- `RAG-CARD1` added a reviewed `GD-859` answer-card contract for dangerous
  activation / acute mania crisis. Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1650_dangerous_activation_card.md`
  moves dangerous activation from `0/3` to `0/2` frontmatter/card gaps and
  score `6` to `4`, while leaving routine anxiety and PFA as boundary-support
  guides rather than primary card owners. A follow-up now includes `GD-858` and
  `GD-918` as boundary-support source sections with invariants proving they
  hand manic/dangerous activation to the crisis lane; audit semantics still
  count only direct reviewed-card ownership.
- `RAG-CARD2` added a reviewed `GD-601` answer-card contract for acute
  coronary / stroke overlap. Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1656_acute_coronary_card.md`
  moves stroke/cardiac overlap from `0/1` to `0/0` frontmatter/card gaps and
  score `2` to `0`; the family action is now `regression_monitor`.
- `RAG-CARD3` added a reviewed `GD-301` answer-card contract for poisoning /
  unknown toxic exposure support. Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1700_poisoning_gd301_card.md`
  moves unknown-child-ingestion / poisoning from `0/1` to `0/0`
  frontmatter/card gaps and score `2` to `0`; the family action is now
  `regression_monitor`.
- `RAG-CARD4` added reviewed support-card contracts for `GD-235` infection
  control wound escalation and `GD-622` animal-bite wound infection support.
  Fresh proof at
  `artifacts/bench/high_liability_family_priorities_20260425_1720_wound_support_cards.md`
  moves infected-wound / spreading-redness from `0/2` to `0/0`
  frontmatter/card gaps and score `4` to `0`; the family action is now
  `regression_monitor`. Post-wound runtime proof at
  `artifacts/bench/rag_diagnostics_20260425_1714_post_wound_cards_runtime_panel/`
  keeps retrieval/ranking/generation/safety misses at `0`; the remaining
  dangerous activation `0/2` card gaps are support-surface counters only on
  current evidence.
- `RAG-OPS1` added
  `notes/RAG_SCHEMA_VECTOR_RELEASE_OPS_ADR_20260425.md`, documenting canonical
  guide/card sources, rebuildable Chroma/mobile-pack snapshots, release gates,
  migration rules, and rollback posture for future RAG changes.
- `RAG-TRUST2` added
  `notes/SENKU_HIGH_LIABILITY_RED_TEAM_HANDBOOK_20260425.md`, a release/red-team
  checklist tying deterministic rules, reviewed-card runtime, evidence nuggets,
  claim support, app acceptance, and Android enablement gates together for
  high-liability changes.
- `RAG-PRIV1` added
  `notes/SENKU_PRIVACY_EXPORT_DELETE_INCIDENT_POLICY_20260425.md`, documenting
  local/offline posture, data classes, deletion/export gates, artifact hygiene,
  incident response, and release blockers before any notes/sync/personal-record
  feature ships.
- `RAG-REL1` added
  `notes/SENKU_RELEASE_UPDATE_MIGRATION_RUNBOOK_20260425.md`, documenting
  release classes, guide `version`/`last_updated` expectations, explicit
  `reindex_required` values, manifest capture, migration rules, and rollback
  proof.
- `RAG-PLAY1` added
  `notes/SENKU_COMPOUND_SCENARIO_PLAYBOOKS_20260425.md`, documenting multi-lane
  evaluation playbooks and prompt-shape expectations for outage, contaminated
  water/food, evacuation/livestock, storm shelter recovery, and
  injury/cold/water scenarios.
- `RAG-T12` fixed analyzer diagnostics for actual reviewed-card runtime rows
  so answer-card / evidence-nugget / claim-support checks evaluate the
  artifact's `reviewed_card_ids`, not every reviewed card in the expected guide
  family. Fresh proof at
  `artifacts/bench/rag_diagnostics_20260425_1708_runtime_card_id_filter/`
  restores the post-card FA/FB/FD/FE panel to `0` ranking misses, `5`
  answer-card passes, `19/19` evidence nuggets, and
  `strong_supported:23|uncertain_fit_accepted:1`.
- `D48` through `D51` remain parked safety-gate dispatches for EW/EX/EY/EZ.
- The implied `D52`+ continuation into FA/FB/FC/FD is paused; do not infer
  those slices from the morning handoff alone.

Use `notes/CP9_ACTIVE_QUEUE.md` as the live source for next-step
ordering, post-RC backlog truth, and current status. Do not infer the
next slice from this README alone.

## Landed (not yet rotated)

Retained live root records:
`A1_retry_5560_landscape.md` (superseded-but-kept),
`P5_scope_note_landscape_phone.md` (cancelled-but-kept),
`probe_rain_shelter_mode_flip.md` (stale probe kept per D5).

Unrotated prompt drafts still present at the dispatch root:
`D9_tracker_doc_reconciliation_and_historical_labeling.md`,
`D10_wave_c_direction_note_lock.md`,
`D11_repo_root_retention_triage.md`,
`D12_dispatch_readme_resync.md`,
`D13_relocate_root_docs_into_notes.md`,
`W-C-0_panel_expansion_and_runner_preflight.md`,
`W-C-1a_final_mode_runtime_emission_fix.md`,
`W-C-1_final_mode_telemetry_aggregation_helper.md`,
`W-C-2_desktop_abstain_threshold_tuning.md`,
`W-C-3_android_abstain_vector_mirror.md`,
`W-C-4_uncertain_fit_upper_band_calibration.md`,
`W-C-5a_low_coverage_canary_probe_and_closeout.md`.

Those files reflect landed or historical dispatch work that has not yet
been rotated out of the root. This slice does not move them.

## Dispatch-root trust

`notes/dispatch/` is not a fully normalized live root. It currently
mixes:

- `README.md`
- the retained live records above
- unrotated prompt drafts that still document landed work
- `completed/` for rotated historical slice files

For cleanup, redispatch, or historical reasoning, take a fresh inventory
of the root and cross-check `notes/CP9_ACTIVE_QUEUE.md` instead of
trusting old D8-era assumptions about what has already been rotated.

## Superseded - do not redispatch

- `A1_retry_5560_landscape.md` - prescribed ESC-dismiss approach was
  wrong (ESC keyevent 111 does not close Gboard on modern AVDs).
  A1b's device-level `UiDevice.pressBack()` variant landed GREEN.
  Retained at the dispatch root as a live historical record.

## Cancelled - not dispatched

- `P5_scope_note_landscape_phone.md` - partial-GREEN fallback not
  needed; A1b closed Stage 0 GREEN on 5560 landscape. File retained
  at the dispatch root for record.

# RAG-A14 Android Reviewed-Card Product Enablement Gate

## Slice

`RAG-A14` - define the gates required before reviewed-card runtime moves
beyond developer/test exposure.

## Goal

This is not a card-expansion slice and not a UI redesign slice. It is the
product-enablement checklist that prevents `REVIEWED EVIDENCE` from becoming
broadly visible without matching policy, proof, pack, layout, and validation
evidence.

## Current Baseline

- Runtime remains off by default through `ReviewedCardRuntimeConfig`.
- The only visible control is developer-panel scoped.
- The checked-in Android asset pack now includes the proven six-card probe
  pack, and runtime selection is developer/test-gated for those six pilot
  cards.
- Detail proof metadata carries card ID, guide ID, review status, citation
  policy, provenance, and cited reviewed source guide IDs.
- The prompt harness can assert reviewed-card answer surface, proof summary,
  and recent-thread metadata.

## First Enforceable Guard Landed

The first A14 guard is now enforced inside the Android runtime/proof path. This
does not product-enable reviewed cards, does not turn the runtime on by default,
and does not expand card coverage beyond the existing pilot selection.

Guard behavior now landed:

- `AnswerContentFactory.inferAnswerSurface(...)` only labels
  `ReviewedCardEvidence` when the answer is deterministic, `ruleId` starts
  with `answer_card:`, `sourceCount > 0`,
  `ReviewedCardMetadata.isPresent()` is true, provenance is
  `reviewed_card_runtime`, and cited reviewed source guide IDs are non-empty.
- `PromptHarnessSmokeTest` fails closed for
  `ScriptedExpectedAnswerSurfaceLabel "REVIEWED EVIDENCE"`: the expected
  answer-card rule ID, primary source guide, card ID, card guide ID, review
  status, and cited reviewed source guide IDs must all be supplied, and detail
  must expose at least one source row.
- `OfflineAnswerEngineAnswerCardRuntimeTest` now includes old-pack fallback
  instrumentation: with runtime enabled and optional answer-card tables
  missing, `AnswerCardRuntime.tryPlan(...)` returns null instead of crashing or
  fabricating a card answer.

Validation passed:

- focused JVM/build command over `AnswerContentFactoryTest`,
  `AnswerCardRuntimeTest`, `SessionMemoryTest`,
  `DetailReviewedCardMetadataBridgeTest`, compiled Android tests, and
  assembled debug/test APKs;
- connected instrumentation over `AnswerCardDaoTest`,
  `OfflineAnswerEngineAnswerCardRuntimeTest`, and
  `DeveloperPanelRuntimeToggleTest`;
- prompt harness reinstall/no-pack-push proofs on APK SHA
  `ffe6f23038d20f488c73f5166649b4319b3413e37d7dfd759037f8ef7541eaca`.

Artifacts:

- `artifacts/android_reviewed_card_a14_guard_harness_20260424/20260424_213205_634/emulator-5556`
- `artifacts/android_reviewed_card_a14_guard_harness_20260424/clean_5554/20260424_213252_605/emulator-5554`

Honesty note: uninstall attempts before those two prompt proofs returned
`DELETE_FAILED_INTERNAL_ERROR`. The APK was reinstalled with `install -r`, and
no mobile pack push was performed. Treat these as reinstall/no-pack-push
proofs, not clean-install proofs.

## A14a Readiness Gate Landed

`RAG-A14a` adds proof-only readiness evidence after the six-card runtime pack
and CH2 planner registry. It does not product-enable reviewed cards and does
not turn the runtime on by default.

Dispatch:
`notes/dispatch/RAG-A14a_android_reviewed_card_layout_pack_readiness.md`.

Pack/version evidence:

- `AnswerCardAssetPackParityTest` reads the shipped manifest and SQLite assets;
- manifest `counts.answer_cards=6`;
- SQLite counts are `answer_cards=6`, `answer_card_clauses=116`,
  `answer_card_sources=19`;
- `pack_meta.answer_card_count=6`;
- all six expected card IDs are present and `pilot_reviewed`;
- no card is missing clauses or sources;
- connected parity instrumentation passed across the fixed four-emulator
  matrix.

Layout/proof evidence:

- developer-panel collapsed, expanded runtime-off, and runtime-on screenshots
  were captured for all four documented emulators under
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/developer_panel/`;
- poisoning reviewed-card prompt proof passed across phone/tablet portrait and
  landscape under
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/`;
- prompt proof asserted `REVIEWED EVIDENCE`,
  `answer_card:poisoning_unknown_ingestion`, card/source metadata, cited source
  ID `GD-898`, recent-thread metadata, and poisoning body fragments on APK SHA
  `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`.

A14 is still not fully closed. The next step is manual review of those
screenshots, then either a targeted layout/proof fix or a separate
exposure-policy/support-language slice.

## A14b Proof-Surface Copy Gate Landed

`RAG-A14b` fixes the A14a screenshot finding where reviewed-card detail showed
`Reviewed evidence` in the body/card while the top meta strip still showed
`STRONG EVIDENCE`. Runtime remains off by default and developer/test scoped.

Dispatch:
`notes/dispatch/RAG-A14b_android_reviewed_card_proof_surface_copy.md`.

The fix adds a reviewed-card display-label helper gated through
`AnswerContentFactory.inferAnswerSurface(...)`, so reviewed-card trust surfaces
show `Reviewed evidence` while raw source-strength logic remains unchanged for
non-reviewed flows.

Validation passed:

- focused build / Android test compilation;
- focused JVM tests for `AnswerContentFactoryTest` and
  `DetailReviewedCardMetadataBridgeTest`;
- single phone reviewed-card prompt proof;
- four-posture reviewed-card prompt matrix under
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/` on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.

A14 is still not fully closed. After A14c's negative-control harness proof, the
next A14 slice should decide exposure policy/support language rather than
expanding card coverage.

## A14c Forbidden-Label Harness Gate Landed

`RAG-A14c` adds explicit negative controls for answer-surface trust labels.
Runtime remains off by default and developer/test scoped.

Dispatch:
`notes/dispatch/RAG-A14c_android_forbidden_reviewed_label_harness.md`.

The prompt harness now accepts forbidden answer-surface labels and checks them
against UIAutomator visible text, accessibility descriptions, and settled
detail signals. This proves reviewed-card detail surfaces do not leak
`STRONG EVIDENCE`, and non-reviewed detail surfaces do not leak
`REVIEWED EVIDENCE`.

Validation passed:

- PowerShell parse checks for `run_android_instrumented_ui_smoke.ps1` and
  `run_android_prompt.ps1`;
- Android test compilation and debug Android-test APK assembly;
- runtime-on non-reviewed phone canary for `generic_puncture`;
- runtime-on reviewed inverse phone canary for `poisoning_unknown_ingestion`;
- runtime-off non-reviewed four-posture matrix under
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/` on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.

A14c does not approve support language, choose non-developer exposure, or make
reviewed cards product-default.

## A14d Exposure Policy Decision Landed

`RAG-A14d` closes the current exposure-policy question with Option A: keep
reviewed-card runtime developer/test-only and default `off` for now.

Decision note:
`notes/ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`.

The current evidence set does not approve product-default behavior, card
expansion, top-level product UI, a local-preview toggle, or a runtime default
change. A future non-developer preview would first need the exact support
language tested across phone portrait, phone landscape, tablet portrait, and
tablet landscape while preserving the A14 guard and A14c forbidden-label
controls.

## Acceptance Gates

### Exposure Policy Gate

Before any broader exposure:

- runtime default remains `off` unless this gate is explicitly closed;
- no top-level product toggle, onboarding banner, or normal settings control
  lands as part of A14;
- developer-panel control remains the only manual switch until product language
  and support expectations are approved;
- reviewed-card answers may surface as `REVIEWED EVIDENCE` only when
  the answer is deterministic, provenance is `reviewed_card_runtime`, `ruleId`
  starts with `answer_card:`, source count is nonzero,
  `ReviewedCardMetadata.isPresent()` is true, and cited reviewed source guide
  IDs are non-empty;
- generated/model answers must never use reviewed-card evidence labels;
- old packs or packs with missing answer-card tables must silently fall back to
  the existing path; the first A14 fallback instrumentation proves
  `AnswerCardRuntime.tryPlan(...)` returns null with runtime enabled and
  missing optional answer-card tables.

### Proof Surface Gate

Every enabled-card proof must show, and the harness must assert:

- `REVIEWED EVIDENCE`;
- `answer_card:<card_id>`;
- card ID;
- card guide ID;
- review status;
- runtime citation policy where available;
- cited reviewed source guide IDs;
- at least one visible source row;
- recent-thread metadata round trip after reopening the conversation.

Stop if proof text can show `REVIEWED EVIDENCE` without card metadata, cited
reviewed source IDs, or at least one visible source row.

For mixed-copy regressions, use the A14c forbidden-label guard: reviewed-card
proofs should forbid `STRONG EVIDENCE`, and non-reviewed controls should forbid
`REVIEWED EVIDENCE`.

### Layout / Screenshot Gate

Before broader exposure, capture and review screenshots for:

- home collapsed developer panel;
- home expanded developer panel with runtime toggle;
- reviewed-card detail answer;
- expanded/collapsed proof surface;
- recent-thread reopen for the reviewed-card answer.

Required matrix:

- phone portrait `emulator-5556`;
- phone landscape `emulator-5560`;
- tablet portrait `emulator-5554`;
- tablet landscape `emulator-5558`.

Layout acceptance:

- no new top-level product control;
- developer panel does not dominate tablet home layouts;
- phone landscape remains scrollable and no controls are clipped;
- proof surface text does not overlap, truncate important IDs, or crowd out the
  answer body;
- existing evidence/status language is reused.

### Pack / Version Gate

Before exposure, prove the bundled asset pack and runtime-loaded pack agree:

- manifest reports expected `counts.answer_cards`;
- SQLite tables exist: `answer_cards`, `answer_card_clauses`, and
  `answer_card_sources`;
- DB counts match expected card/clause/source totals for the shipped pack;
- all runtime-eligible cards have review status in `reviewed`,
  `pilot_reviewed`, or `approved`;
- every runtime-eligible card has at least one clause and at least one source;
- `pack_meta.answer_card_count` matches manifest count;
- clean APK install works without manual pack push;
- old-pack fallback still returns empty card data and does not crash.

### Card Expansion Stop Lines

A14 does not expand runtime cards. If expansion is attempted in a later slice,
stop unless each new card has:

- exact card ID allowlist;
- exact guide/source-owner policy;
- one-card-only selection;
- query-text-only conditional activation;
- visible source rows;
- proof metadata assertions;
- negative near-miss prompts proving the card does not over-trigger.

Do not activate conditionals from retrieved context. Do not reuse
`SearchResult` as the card model. Do not hardcode reviewed-card answers into
deterministic routing.

## Validation Matrix

Minimum A14 validation:

- JVM/unit: `PackManifestTest`, `AnswerCardRuntimeTest`, `SessionMemoryTest`,
  and `AnswerContentFactoryTest`.
- Android instrumentation: `AnswerCardDaoTest`,
  `OfflineAnswerEngineAnswerCardRuntimeTest`, `DeveloperPanelRuntimeToggleTest`,
  and `PromptHarnessSmokeTest` with reviewed-card runtime enabled.
- Build: `:app:assembleDebug` and `:app:assembleDebugAndroidTest`.
- Connected matrix: clean install with runtime off; clean install with runtime
  enabled by harness; recent-thread reopen; and old-pack/missing-card-table
  fallback.

## Close Criteria

A14 is not fully closed by the first enforceable guard. Full A14 closure still
requires the report to name the exact artifact directories, APK SHA, pack
manifest timestamp, answer-card DB counts, four-emulator screenshot set, and
prompt-harness assertions, including forbidden-label controls where relevant.
If any gate fails, leave runtime developer/test scoped and file the failing
gate as the next slice.

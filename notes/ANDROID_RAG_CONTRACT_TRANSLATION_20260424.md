# Android RAG Contract Translation - 2026-04-24

## Question

Does the desktop RAG/answer-card work translate to Android?

Short answer: partly, but not automatically. The desktop work is now the
contract and proof oracle. Android still needs explicit Kotlin/mobile-pack
plumbing before reviewed-card answers, provenance labels, and card metadata
change the APK behavior.

## Desktop Contract Now Proven

The current desktop proof over EX/EY/EZ/FC lives at:

`artifacts/bench/rag_diagnostics_20260424_1750_rags13_code_health_final_smoke/report.md`

Stable counts:

- deterministic pass: `13`
- expected supported: `9`
- abstain/clarify needed: `2`
- retrieval/ranking/generation/safety-contract misses: `0`
- hit@1: `21/24`
- hit@3/hit@k/cited: `24/24`
- reviewed card-backed answers: `8`
- generated prompts: `1`
- generated-vs-shadow card gaps: `0`

Desktop artifacts/analyzer now distinguish:

- `answer_provenance`: deterministic rule, generated model, reviewed card
  runtime, uncertain-fit card;
- `answer_surface_label`: deterministic rule, generated evidence, reviewed
  card evidence, limited fit;
- reviewed card IDs / guide IDs / cited guide IDs;
- expected evidence owner and app acceptance.

## Android Current Shape

Android already has some receiving primitives:

- `OfflineAnswerEngine.AnswerMode`: `CONFIDENT`, `UNCERTAIN_FIT`, `ABSTAIN`;
- `OfflineAnswerEngine.ConfidenceLabel`: `HIGH`, `MEDIUM`, `LOW`;
- `AnswerRun.deterministic`, `AnswerRun.abstain`, `AnswerRun.ruleId`;
- `AnswerContent.evidence`, `sourceCount`, `abstain`, `uncertainFit`;
- UI copy for `STRONG EVIDENCE`, `MODERATE EVIDENCE`, `UNSURE FIT`, and
  `NO MATCH`.

Android does not yet have:

- cited-vs-retrieved distinction in `SearchResult`;
- a dedicated reviewed-card `AnswerRun` output type;
- public runtime enablement or user-facing reviewed-card toggle policy.

Android now has the first narrow runtime bridge:

- optional answer-card metadata in the mobile pack;
- a backward-compatible Android reader/model layer;
- a disabled-by-default poisoning / unknown-ingestion reviewed-card runtime
  pilot that composes from `answer_card_clauses` only when exactly one eligible
  `poisoning_unknown_ingestion` card with at least one source row is selected;
- connected full `prepare(...)` instrumentation proving the dark runtime path
  against a temp answer-card pack on the documented Senku matrix;
- display-model inference that maps deterministic `answer_card:` rule IDs to
  `ReviewedCardEvidence` / `reviewed_card_runtime` only when source count is
  nonzero, reviewed-card metadata is present, provenance is
  `reviewed_card_runtime`, and cited reviewed source guide IDs are non-empty;
- `AnswerCardRuntime` / `ReviewedCardRuntimeConfig` as the Android-side planner
  and hidden preference wrapper for instrumentation and future developer UI.
- the normal scripted prompt automation path can now prove a reviewed-card
  answer against a real pushed pack, with `REVIEWED EVIDENCE`, the
  `answer_card:poisoning_unknown_ingestion` rule ID, primary source `GD-898`,
  and poisoning/vomiting body fragments asserted across the fixed
  phone/tablet portrait/landscape emulator matrix.
- the same gated Android runtime now covers one additional reviewed card,
  `newborn_danger_sepsis` / `GD-284`, from the bundled six-card asset pack.
  Its prompt proof asserts `answer_card:newborn_danger_sepsis`, card/source
  metadata, cited source IDs `GD-284|GD-298|GD-492|GD-617`, recent-thread
  metadata, and newborn danger body fragments across phone/tablet portrait and
  landscape. This remains developer/test scoped.
- the same gated Android runtime now also covers
  `choking_airway_obstruction` / `GD-232`, including infant choking branch
  proof, cited source IDs `GD-232|GD-284|GD-298|GD-617`, and near-miss guards
  for allergy, panic, poisoning, and post-choking retained-object drift. This
  remains developer/test scoped.
- the same gated Android runtime now also covers
  `meningitis_sepsis_child` / `GD-589`, including red-flag child
  meningitis/sepsis proof, cited source IDs
  `GD-589|GD-235|GD-268|GD-284|GD-298` in Android DAO order, and boundaries
  that keep bare meningitis-vs-viral comparison prompts plus public-health
  reporting/contact-tracing prompts generated or compare/clarify. This remains
  developer/test scoped.
- the same gated Android runtime now also covers
  `infected_wound_spreading_infection` / `GD-585`, including spreading wound
  infection proof, cited source ID `GD-585`, and boundaries that keep routine
  wound care, generic cellulitis education, newborn cord infection, and
  meningitis/sepsis rash prompts out of strict reviewed-card fallback. This
  remains developer/test scoped.
- the same gated Android runtime now also covers
  `abdominal_internal_bleeding` / `GD-380`, including abdominal
  trauma/internal bleeding proof, cited source IDs `GD-380|GD-232|GD-584`, and
  boundaries that keep pregnancy/ectopic prompts, routine GI prompts, and other
  emergency-card families out of strict reviewed-card fallback. This remains
  developer/test scoped and completes the checked-in six-card pilot pack.
- explicit reviewed-card metadata now travels through Android runtime and
  detail proof state: card ID, card guide ID, review status, runtime citation
  policy, answer provenance, and cited reviewed source guide IDs. The prompt
  harness asserts the metadata and its proof-summary visibility across the
  same fixed matrix.
- recent-thread persistence records ready instant reviewed-card answers and
  round-trips `ReviewedCardMetadata` through `SessionMemory` JSON.
- the checked-in Android asset pack now includes the proven six-card
  answer-card probe pack, so clean APK installs can exercise the pilot runtime
  without a manual pack push when the developer/test flag is enabled.
- `DetailReviewedCardMetadataBridge` now owns reviewed-card metadata intent
  transport, current detail state, and rule-id guarded lookup as a narrow
  code-health extraction; behavior is unchanged after compile/unit checks and a
  clean-install no-push four-emulator reviewed-card matrix under
  `artifacts/android_reviewed_card_bridge_matrix_20260424/`.
- `RAG-CH2` now owns the next narrow Android code-health extraction:
  `AnswerCardRuntime` uses an in-file `CardSpec` registry and shared planner
  helper for the six reviewed-card runtime paths. Clinical predicates remain
  explicit and behavior is unchanged after focused JVM/build checks, connected
  `OfflineAnswerEngineAnswerCardRuntimeTest`, and a phone prompt canary under
  `artifacts/android_reviewed_card_ch2_runtime_registry_harness_20260424/`.
- `RAG-A14a` added proof-only layout / pack readiness evidence without changing
  product exposure. `AnswerCardAssetPackParityTest` now asserts the shipped
  asset pack's answer-card counts, tables, expected card IDs, review statuses,
  and clause/source coverage. Developer-panel home-state screenshots plus a
  poisoning reviewed-card prompt matrix live under
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/`.
- `RAG-A14` product enablement gate dispatch exists at
  `notes/dispatch/RAG-A14_android_reviewed_card_product_enablement_gate.md`.
  Its first enforceable guard has landed for reviewed-evidence label
  inference, proof-harness fail-closed requirements, and old-pack missing-table
  fallback. Runtime remains off by default, full product enablement remains
  open, and A14 does not expand cards. Prompt proof artifacts are under
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/20260424_213205_634/emulator-5556`
  and
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/clean_5554/20260424_213252_605/emulator-5554`
  on APK SHA
  `ffe6f23038d20f488c73f5166649b4319b3413e37d7dfd759037f8ef7541eaca`.
  These are reinstall/no-pack-push proofs, not clean-install proofs, because
  uninstall returned `DELETE_FAILED_INTERNAL_ERROR` before reinstall.
- `RAG-A14b` then harmonized reviewed-card proof-surface copy. The reviewed-card
  meta strip, body/card label, and proof summary now use reviewed-card-specific
  trust wording instead of source-strength copy, while runtime remains off by
  default. Four-posture prompt proof lives under
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/` on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
- `RAG-A14c` then added a forbidden-label prompt harness guard. Reviewed-card
  prompts can assert that `STRONG EVIDENCE` is absent, non-reviewed prompts can
  assert that `REVIEWED EVIDENCE` is absent, and the harness checks visible
  UIAutomator text, accessibility descriptions, and settled detail signals.
  Runtime-on reviewed and non-reviewed canaries plus a runtime-off
  non-reviewed four-posture matrix live under
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/` on the same
  APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
- `RAG-A14d` then closed the exposure-policy decision with Option A:
  reviewed-card runtime stays developer/test-only and default `off`. No
  product-default behavior, card expansion, top-level product UI,
  local-preview toggle, or runtime default change is approved from the current
  evidence set. Decision note:
  `notes/ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`.

## Mobile-Pack Status

Before `RAG-A2`, the mobile pack exported only:

- `guides`
- `guide_related`
- `deterministic_rules`
- `chunks`
- `lexical_chunks_fts`
- `lexical_chunks_fts4`
- `pack_meta`

`RAG-A2` has now added optional SQLite answer-card metadata tables, not
runtime behavior:

- `answer_cards`
- `answer_card_clauses`
- `answer_card_sources`

The manifest now reports `counts.answer_cards`, `pack_meta.answer_card_count`,
and the three metadata-only schema entries. Android consumes this through the
dedicated answer-card reader/model layer. Do not add card fields to
`SearchResult`; card data should keep its own model.

Fresh real-pack proof now exists at
`artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`.
The exported pack contains `6` pilot cards, `116` card clauses, and `19` card
sources; all cards are `pilot_reviewed`, and no card is missing clauses or
sources.

`RAG-A13` replaced the checked-in
`android-app/app/src/main/assets/mobile_pack` with that proven pack. The
bundled manifest was generated at `2026-04-25T00:08:46.459832+00:00` and
reports `answer_cards=6`; SQLite parity checks found `6` answer cards, `116`
clauses, and `19` sources. Clean-install no-push proof passed across the fixed
matrix with APK SHA
`be32ff34f66e7d478082ae2bd292f0e8315f231ec9f55c4852debfdd0e3cc553` and
recent-thread metadata assertion enabled. Runtime remains off by default.

## Recommended Android Order

1. Display-only receiving shape.
   - Add an `AnswerSurfaceLabel` model in `AnswerContent`.
   - Infer `Abstain`, `LimitedFit`, `DeterministicRule`, and
     `GeneratedEvidence` from existing Android state.
   - Do not claim `ReviewedCardEvidence` until card metadata/runtime exists.
   - Status: landed in `RAG-A1`.

2. Mobile-pack export path.
   - Export optional answer-card tables from `mobile_pack.py`.
   - Add manifest/count parity and pack schema tests.
   - Add Android reader tests that old packs without card tables still work.
   - Status: exporter and Python tests landed in `RAG-A2`; Android reader
     layer and instrumentation test landed in `RAG-A3`. Connected DAO
     instrumentation passed across the documented Senku phone/tablet
     portrait/landscape matrix.

3. One gated Android card-backed runtime pilot.
   - Start with `poisoning_unknown_ingestion`.
   - Select only reviewed/approved high-risk cards.
   - Require exactly one selected card.
   - Activate conditional clauses from question text only.
   - Keep the normal engine path unchanged when the flag is off.
   - Status: initial disabled-by-default pilot landed in `RAG-A5`; no
     user-facing toggle yet. `RAG-A11a` later expanded the same gated runtime
     to `newborn_danger_sepsis` / `GD-284`, `RAG-A11b` expanded it to
     `choking_airway_obstruction` / `GD-232`, `RAG-A11c` expanded it to
     `meningitis_sepsis_child` / `GD-589`, and `RAG-A11d` expanded it to
     `infected_wound_spreading_infection` / `GD-585`; `RAG-A11e` completed the
     checked-in six-card pilot with `abdominal_internal_bleeding` / `GD-380`.
     Runtime remains off by default.

4. UI label integration.
   - Runtime/card data now exists behind a dark flag.
   - Status: initial display-model inference landed in `RAG-A6`; no layout
     redesign or screenshot lane yet.
   - Keep source/evidence panels grounded in cited guide owners, not just
     retrieved candidates.

5. Config / enablement policy.
   - Status: `RAG-A7` added a config wrapper, but no visible control and no
     exported-activity intent override.
   - `RAG-A8` added the developer-panel button; it remains developer/test
     scoped.
   - `RAG-A14` now defines the policy, proof-surface, screenshot/layout,
     pack/version, and validation gates required before broader product
     exposure.
   - Its first enforceable guard is landed: `REVIEWED EVIDENCE` requires an
     expected answer-card rule ID, primary source guide, card ID, card guide
     ID, review status, cited reviewed source guide IDs, and at least one
     visible source row in harness proof. Old packs with missing answer-card
     tables return null planning rather than crash/fabrication when runtime is
     enabled.
   - Normal product enablement remains deferred, and A14 does not expand cards.

6. Real-pack E2E.
   - Status: `RAG-A12` probe exported a fresh real mobile pack with answer-card
     tables intact, and `RAG-A9` now uses that pack for the Android scripted
     prompt E2E after hot-swapping it onto each emulator.
   - `RAG-A10` then promoted card ID, card guide ID, review status, citation
     policy, provenance, and cited reviewed source IDs into explicit
     `ReviewedCardMetadata` carried by runtime/detail/proof state.
   - `RAG-A13` closed asset-pack parity by bundling the proven six-card pack in
     the checked-in Android assets. Next step is product enablement policy, not
     proving the poisoning pilot path again.
   - `RAG-A11a` added the first post-poisoning Android runtime expansion:
     `newborn_danger_sepsis` / `GD-284`, with four-emulator bundled-pack prompt
     proof on APK SHA
     `60bb6751d18f04f13dff4a6441c65f2cd59f98195ca243f26e5e591830582b5c`.
   - `RAG-A11b` added the second post-poisoning Android runtime expansion:
     `choking_airway_obstruction` / `GD-232`, with four-emulator bundled-pack
     prompt proof on APK SHA
     `63126e427e21385f1e55e0451b81a32d768eb9ab63cc734b77efca723dd740af`.
   - `RAG-A11c` added the third post-poisoning Android runtime expansion:
     `meningitis_sepsis_child` / `GD-589`, with four-emulator bundled-pack
     prompt proof on APK SHA
     `8bee066f6ce3a988388cd60d767e70bb93c46bf8cf43a4329ddd7ff21171ddd4`.
   - `RAG-A11d` added the fourth post-poisoning Android runtime expansion:
     `infected_wound_spreading_infection` / `GD-585`, with four-emulator
     bundled-pack prompt proof on APK SHA
     `f1ae9a28e619069cbd8bc057070440e49948d30395e56d9fa727a2855c1de6d6`.
   - `RAG-A11e` added the fifth post-poisoning Android runtime expansion and
     completed the checked-in six-card pilot:
     `abdominal_internal_bleeding` / `GD-380`, with four-emulator bundled-pack
     prompt proof on APK SHA
     `3bd27a2aad4504328428aa78ec2e5e91414eafa05b5dfd1c918505264e249f2c`.
   - `RAG-CH2` then reduced runtime planner duplication with an in-file
     `CardSpec` registry and shared planner helper; clinical predicates,
     product gating, source construction, and metadata behavior are unchanged.
   - `RAG-A14a` then added proof-only pack/layout readiness: shipped asset-pack
     parity instrumentation, developer-panel collapsed/off/on screenshots, and
     a four-posture poisoning reviewed-card prompt matrix. Runtime remains off
     by default and product enablement remains open.
   - `RAG-A14b` then fixed the mixed reviewed-card proof-surface copy found in
     A14a screenshot review.
   - `RAG-A14c` then added forbidden-label negative controls so reviewed and
     non-reviewed answer surfaces cannot leak each other's trust labels in
     prompt proof.
   - `RAG-A14d` then chose Option A for exposure policy: keep reviewed-card
     runtime developer/test-only and default `off` until exact support language
     and product layout are proven across the fixed postures.

## Stop Lines

- Do not hardcode reviewed-card answers into `DeterministicAnswerRouter`.
- Do not reuse `SearchResult` as the card model.
- Do not activate card conditionals from retrieved context.
- Do not make Android runtime depend on card tables without preserving the
  tested old-pack fallback: missing answer-card tables should return null card
  planning, not crash or fabricate a card answer.
- Do not present generated answers as reviewed evidence.

# Android Reviewed-Card Runtime Backlog - 2026-04-24

## Current Position

The desktop reviewed-card/provenance work now translates to Android as a real
dark runtime path, not merely as a document contract.

Landed Android sequence:

- `RAG-A1`: display-only answer surface/provenance receiving shape.
- `RAG-A2`: optional mobile-pack `answer_cards`, `answer_card_clauses`, and
  `answer_card_sources` tables.
- `RAG-A3`: backward-compatible Android reader/model layer.
- `RAG-A4`: mobile-pack answer-card export helper extraction.
- `RAG-A5`: disabled-by-default `poisoning_unknown_ingestion` runtime pilot.
- `RAG-A6`: `answer_card:` rule IDs surface as reviewed-card evidence.
- `RAG-A7`: Android runtime planner/config extraction.
- `RAG-A8`: developer-panel reviewed-card runtime toggle with screenshot proof.
- `RAG-A9`: scripted prompt harness proof against a real pushed pack.
- `RAG-A10`: explicit reviewed-card detail/proof metadata carrier.
- `RAG-CH1`: narrow detail metadata bridge extraction; behavior unchanged.
- `RAG-A12` probe: real mobile-pack export now includes answer-card tables.
- `RAG-A13`: checked-in Android asset pack replaced with the proven six-card
  probe pack.
- `RAG-A14` dispatch: product enablement gate before reviewed-card runtime can
  move beyond developer/test exposure.
- `RAG-A14` first guard: enforceable reviewed-evidence label, proof-harness,
  and old-pack fallback checks have landed inside the still-gated runtime.
- `RAG-A11a`: `newborn_danger_sepsis` / `GD-284` added as the second
  developer/test-gated Android reviewed-card runtime path, with phone/tablet
  portrait/landscape prompt proof.
- `RAG-A11b`: `choking_airway_obstruction` / `GD-232` added as the third
  developer/test-gated Android reviewed-card runtime path, with infant choking
  prompt proof across phone/tablet portrait and landscape.
- `RAG-A11c`: `meningitis_sepsis_child` / `GD-589` added as the fourth
  developer/test-gated Android reviewed-card runtime path, with red-flag child
  meningitis/sepsis prompt proof across phone/tablet portrait and landscape.
- `RAG-A11d`: `infected_wound_spreading_infection` / `GD-585` added as the
  fifth developer/test-gated Android reviewed-card runtime path, with spreading
  wound infection prompt proof across phone/tablet portrait and landscape.
- `RAG-A11e`: `abdominal_internal_bleeding` / `GD-380` added as the sixth and
  final checked-in pilot Android reviewed-card runtime path, with abdominal
  trauma/internal bleeding prompt proof across phone/tablet portrait and
  landscape.
- `RAG-CH2`: reviewed-card runtime planner registry extracted in
  `AnswerCardRuntime`; behavior unchanged after focused JVM/build, connected
  runtime instrumentation, and a prompt canary.
- `RAG-A14a`: proof-only layout / pack readiness gate landed; runtime remains
  developer/test scoped and off by default.
- `RAG-A14b`: reviewed-card proof-surface copy harmonized so the meta strip,
  body/card label, and proof summary use reviewed-card trust wording.
- `RAG-A14c`: forbidden answer-surface label harness landed, with reviewed and
  non-reviewed inverse controls proving `REVIEWED EVIDENCE` and
  `STRONG EVIDENCE` do not leak onto the wrong answer surface.
- `RAG-A14d`: exposure policy decision recorded as Option A: keep reviewed-card
  runtime developer/test-only and default `off` for now.
- `RAG-CH3`: Android scripted prompt harness contract extraction landed;
  behavior unchanged, androidTest-only.

This is real progress toward guide-backed answers. We are no longer only
patching after bench failures; the app now has a narrow, tested path from
reviewed guide-card metadata to an answer surface.

## What Still Is Not Product Behavior

- The reviewed-card runtime is still off by default.
- The only visible toggle is developer-panel scoped; it is not a normal
  product control.
- Six cards are wired into Android runtime selection:
  `poisoning_unknown_ingestion`, `newborn_danger_sepsis`, and
  `choking_airway_obstruction`, `meningitis_sepsis_child`, and
  `infected_wound_spreading_infection`, and
  `abdominal_internal_bleeding`.
- Recent-thread metadata now has harness assertion coverage for the
  `poisoning_unknown_ingestion`, `newborn_danger_sepsis`, and
  `choking_airway_obstruction`, `meningitis_sepsis_child`, and
  `infected_wound_spreading_infection`, and `abdominal_internal_bleeding`
  reviewed-card paths.
- The toggle has screenshot proof across the documented emulator matrix, but
  the final product layout remains unsettled.

## Recommended Next Slices

Current A14 decision after A14d: do not expand cards or product-enable reviewed
runtime yet. Reviewed-card runtime stays developer/test-only and default `off`.
Decision note:
[`ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`](./ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md).
Draft/background note:
[`ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DRAFT_20260424.md`](./ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DRAFT_20260424.md).

### RAG-A8 Developer-Panel Toggle

Status: landed.

Added a reversible developer-panel button for the hidden runtime flag.

Targets:

- `MainActivity.java`
- all five `activity_main.xml` variants
- `strings.xml`
- focused home/developer-panel UI test

Acceptance:

- default collapsed developer panel remains the entry point;
- expanded developer panel shows one compact runtime toggle;
- label flips between off/on;
- screenshots/harness artifacts exist for phone/tablet portrait/landscape at
  `artifacts/android_reviewed_card_toggle_20260424_193049/`;
- no top-level product control.

Do not add explanation copy, banners, or cards. Keep it inside the existing
developer panel.

### RAG-A9 Reviewed-Card Automation Harness

Status: landed.

The normal `MainActivity` scripted ask path can now enable the hidden runtime
inside instrumentation and assert the reviewed-card answer surface end to end.
It uses the real exported probe pack, not a synthetic temp DB.

- `PromptHarnessSmokeTest`
- `DetailActivity`
- phone-landscape `activity_detail.xml`
- `AnswerCardDao`
- `run_android_instrumented_ui_smoke.ps1`
- `run_android_prompt.ps1`

Final proof:

- real pack:
  `artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`
- final trusted matrix artifacts:
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_475/`
  and
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_566/`

Assertions covered:

- `REVIEWED EVIDENCE`;
- `answer_card:poisoning_unknown_ingestion`;
- primary source `GD-898`;
- body fragments `Call poison control` and
  `Avoid: Do not induce vomiting`;
- no host/model generation call is required for this branch.

Operational note: `RAG-A13` replaced the checked-in Android asset pack with the
same six-card probe pack, so fresh APK installs no longer need a manual pack
push for these pilot-reviewed cards. Runtime still requires the developer/test
flag.

### RAG-A10 Detail-Proof Metadata

Status: landed.

Reviewed-card metadata now travels beside `ruleId` as `ReviewedCardMetadata`
instead of being inferred from `SearchResult` display rows.

Fields carried:

- card ID;
- card guide ID;
- review status;
- runtime citation policy;
- answer provenance;
- cited reviewed source guide IDs.

Touched:

- `ReviewedCardMetadata`
- `AnswerCardRuntime`
- `OfflineAnswerEngine`
- `DetailActivity`
- `SessionMemory`
- `MainActivity`
- `DetailSessionPresentationFormatter`
- `DetailProofPresentationFormatter`
- `AnswerContent`
- `PromptHarnessSmokeTest`
- Android prompt/smoke wrapper scripts

Final proof:

- real pack:
  `artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`
- trusted metadata/proof matrix artifacts:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_371/`
  and
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_366/`
- post-persistence phone regression artifacts:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203421_850/`
  and
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203421_874/`
- recent-thread metadata assertion artifacts:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203803_648/`
- query-keyed recent-thread assertion spot check:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203948_946/`
- cold-restore recent-thread assertion spot check:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_204236_127/`

Assertions covered:

- `REVIEWED EVIDENCE`;
- `answer_card:poisoning_unknown_ingestion`;
- primary source `GD-898`;
- card ID `poisoning_unknown_ingestion`;
- card guide ID `GD-898`;
- review status `pilot_reviewed`;
- cited reviewed source guide ID `GD-898`;
- proof-summary visibility for the metadata;
- body fragments `Call poison control`, `Avoid: Do not induce vomiting`, and
  `GD-898`.

Operational note: this still does not make the runtime product-default. It only
makes the proof contract explicit and testable.

Follow-up landed in the same planner block:

- ready instant reviewed-card answers are recorded into recent threads;
- `ReviewedCardMetadata` round-trips through `SessionMemory` JSON;
- recent-thread reopen passes the metadata back into `DetailActivity`;
- focused JVM proof is `SessionMemoryTest`;
- the prompt harness can now assert that
  `ChatSessionStore.recentConversationPreviews(...)` contains the scripted
  reviewed-card metadata.

### RAG-CH1 Detail Metadata Bridge Extraction

Status: landed and validated.

`DetailReviewedCardMetadataBridge` now owns reviewed-card metadata intent
transport, current detail state, and rule-id guarded lookup. This is a narrow
code-health extraction to reduce future edits in `DetailActivity`; reviewed-card
behavior is unchanged.

Proof:

- dispatch note: `notes/dispatch/RAG-CH1_android_detail_metadata_bridge.md`;
- compile/unit checks passed for `DetailReviewedCardMetadataBridgeTest`,
  `SessionMemoryTest`, and `AnswerContentFactoryTest`;
- `:app:compileDebugAndroidTestJavaWithJavac`,
  `:app:compileDebugAndroidTestKotlin`, `:app:assembleDebug`, and
  `:app:assembleDebugAndroidTest` passed;
- clean-install no-push matrix passed with APK SHA
  `7dae8277227c8f5ddc2997c412203da1309addff6e76f7f278e1e0734b80464c`;
- artifacts are under
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5554/20260424_212147_831/emulator-5554`,
  `.../clean_matrix_5556/20260424_212147_832/emulator-5556`,
  `.../clean_matrix_5558/20260424_212147_831/emulator-5558`, and
  `.../clean_matrix_5560/20260424_212147_828/emulator-5560`.

Harness note: an initial phone proof failed because
`PromptHarnessSmokeTest` still reflected the removed
`currentReviewedCardMetadata` field. The app launch path was preserved; the
harness now reads `reviewedCardMetadataBridge.current()` and the full matrix
passes.

### RAG-A11 Expand Android Runtime Cards

After the poisoning pilot is stable, add the next reviewed cards one at a time:

- choking / airway obstruction;
- newborn danger signs;
- meningitis / sepsis child;
- infected wound spreading infection;
- abdominal / internal bleeding.

Acceptance for each:

- exact card ID allowlist;
- exactly one eligible card selected;
- conditionals triggered from question text only;
- old packs remain silent fallback;
- connected matrix proof.

#### RAG-A11a Newborn Danger Signs

Status: landed and validated; runtime remains developer/test gated.

Android now selects the `newborn_danger_sepsis` / `GD-284` reviewed card when
the hidden runtime flag is enabled, exactly one eligible card is loaded from
the bundled pack, and the query contains newborn-age language plus a newborn
danger marker. Conditionals still activate from question text only.

Guardrails:

- exact card ID `newborn_danger_sepsis`;
- exact card guide ID `GD-284`;
- allowed review status `reviewed`, `pilot_reviewed`, or `approved`;
- risk tier `critical` or `high`;
- visible source rows required;
- cited reviewed source guide IDs are carried as `GD-284`, `GD-298`, `GD-492`,
  and `GD-617`;
- A14 reviewed-evidence guard still applies.

Validation:

- focused JVM/build checks passed for `AnswerCardRuntimeTest`,
  `AnswerContentFactoryTest`, `DetailReviewedCardMetadataBridgeTest`, Android
  test compilation, and debug/test APK assembly;
- connected instrumentation passed for `AnswerCardDaoTest`,
  `OfflineAnswerEngineAnswerCardRuntimeTest`, and
  `DeveloperPanelRuntimeToggleTest`;
- prompt query `newborn is limp, will not feed, and is hard to wake` asserted
  `REVIEWED EVIDENCE`, `answer_card:newborn_danger_sepsis`, card ID
  `newborn_danger_sepsis`, card guide `GD-284`, review status
  `pilot_reviewed`, cited source IDs `GD-284|GD-298|GD-492|GD-617`,
  recent-thread metadata, and body fragments `Treat breathing difficulty`,
  `Keep the newborn warm`, `Avoid: Do not treat fever`, and `GD-284`.

Four-emulator reinstall/no-pack-push prompt matrix passed on APK SHA
`60bb6751d18f04f13dff4a6441c65f2cd59f98195ca243f26e5e591830582b5c`:

- tablet portrait:
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5554/20260424_214842_111/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5556/20260424_214842_109/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5558_landscape/20260424_214914_616/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5560_landscape/20260424_214914_616/emulator-5560`

Dispatch note:
`notes/dispatch/RAG-A11a_android_newborn_danger_reviewed_card_runtime.md`.

Honesty note: no mobile pack push was used, but do not describe the prompt
matrix as strict clean-install proof; setup used reinstall/package verification
before the no-pack-push harness run.

#### RAG-A11b Choking / Airway Obstruction

Status: landed and validated; runtime remains developer/test gated.

Android now selects the `choking_airway_obstruction` / `GD-232` reviewed card
when the hidden runtime flag is enabled, exactly one eligible card is loaded
from the bundled pack, and the query contains choking/foreign-body-airway
context plus an action or complete-obstruction signal. Conditionals still
activate from question text only.

Guardrails:

- exact card ID `choking_airway_obstruction`;
- exact card guide ID `GD-232`;
- allowed review status `reviewed`, `pilot_reviewed`, or `approved`;
- risk tier `critical` or `high`;
- visible source rows required;
- cited reviewed source guide IDs are carried as `GD-232`, `GD-284`, `GD-298`,
  and `GD-617`;
- allergy/anaphylaxis, poisoning, panic-only, and post-choking retained-object
  near-misses are guarded away from this card;
- A14 reviewed-evidence guard still applies.

Validation:

- focused JVM/build checks passed for `AnswerCardRuntimeTest`,
  `AnswerContentFactoryTest`, `DetailReviewedCardMetadataBridgeTest`, Android
  test compilation, and debug/test APK assembly;
- connected instrumentation passed across the four AVD lanes for
  `AnswerCardDaoTest`, `OfflineAnswerEngineAnswerCardRuntimeTest`, and
  `DeveloperPanelRuntimeToggleTest`;
- prompt query `baby is choking and cannot cry or cough` asserted
  `REVIEWED EVIDENCE`, `answer_card:choking_airway_obstruction`, card ID
  `choking_airway_obstruction`, card guide `GD-232`, review status
  `pilot_reviewed`, cited source IDs `GD-232|GD-284|GD-298|GD-617`,
  recent-thread metadata, and body fragments `start age-appropriate choking
  rescue`, `5 back blows followed by 5 chest thrusts`,
  `Avoid: Do not perform abdominal thrusts on infants`, and `GD-232`.

Four-emulator reinstall/no-pack-push prompt matrix passed on APK SHA
`63126e427e21385f1e55e0451b81a32d768eb9ab63cc734b77efca723dd740af`:

- tablet portrait:
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5554/20260424_220443_026/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5556/20260424_220443_026/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5558_landscape/20260424_220443_026/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5560_landscape/20260424_220443_026/emulator-5560`

Dispatch note:
`notes/dispatch/RAG-A11b_android_choking_airway_reviewed_card_runtime.md`.

Honesty note: no mobile pack push was used, but do not describe the prompt
matrix as strict clean-install proof; setup used reinstall/package verification
before the no-pack-push harness run.

#### RAG-A11c Meningitis / Sepsis Child

Status: landed and validated; runtime remains developer/test gated.

Android now selects the `meningitis_sepsis_child` / `GD-589` reviewed card when
the hidden runtime flag is enabled, exactly one eligible card is loaded from
the bundled pack, and the query contains a red-flag meningitis/sepsis clinical
predicate. Conditionals still activate from query text only.

Guardrails:

- exact card ID `meningitis_sepsis_child`;
- exact card guide ID `GD-589`;
- card title `Sepsis Recognition, Escalation & Empiric Antibiotic Protocols`;
- allowed review status `reviewed`, `pilot_reviewed`, or `approved`;
- review status in the bundled proof is `pilot_reviewed`;
- risk tier `critical` or `high`; bundled proof risk is `critical`;
- runtime citation policy is the empty string;
- exactly one eligible card is selected from the pack;
- visible source rows required;
- conditionals activate from query text only;
- red-flag clinical predicate only;
- bare `meningitis vs viral illness` / comparison prompts and public-health
  reporting/contact-tracing prompts stay generated or compare/clarify, not
  strict emergency-card fallback;
- A14 reviewed-evidence guard still applies.

Android DAO source ordering in proof is `GD-589|GD-235|GD-268|GD-284|GD-298`.
The YAML/card spec order differs
(`GD-589|GD-284|GD-298|GD-268|GD-235`), but Android prompt proof records DAO
order.

Validation:

- focused JVM/build checks passed for `AnswerCardRuntimeTest`,
  `OfflineAnswerEngineTest`, `AnswerContentFactoryTest`,
  `DetailReviewedCardMetadataBridgeTest`, Android test compilation, and
  debug/test APK assembly;
- connected instrumentation passed `10` tests per device for
  `AnswerCardDaoTest`, `OfflineAnswerEngineAnswerCardRuntimeTest`, and
  `DeveloperPanelRuntimeToggleTest`;
- prompt query
  `child has fever, stiff neck, and a purple rash that does not fade when pressed`
  asserted `REVIEWED EVIDENCE`, `answer_card:meningitis_sepsis_child`, card ID
  `meningitis_sepsis_child`, card guide `GD-589`, review status
  `pilot_reviewed`, cited source IDs `GD-589|GD-235|GD-268|GD-284|GD-298`,
  recent-thread metadata, and body fragments
  `suspected meningitis or meningococcemia`, `Escalate urgently`,
  `Fever with stiff neck`, and
  `Avoid: Do not route fever plus stiff neck`.

Commands recorded for the A11c proof:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.mobile.OfflineAnswerEngineTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --rerun-tasks --console=plain
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest,com.senku.mobile.DeveloperPanelRuntimeToggleTest" --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Four-emulator reinstall/no-pack-push prompt matrix passed on APK SHA
`8bee066f6ce3a988388cd60d767e70bb93c46bf8cf43a4329ddd7ff21171ddd4`:

- tablet portrait:
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5554/20260424_222904_794/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5556/20260424_222904_793/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5558_landscape/20260424_222904_794/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5560_landscape/20260424_222904_794/emulator-5560`

Spot proof passed under
`artifacts/instrumented_ui_smoke/20260424_222750_539/emulator-5556`; copied
dump/logcat also live under
`artifacts/android_reviewed_card_a11c_meningitis_harness_20260424/`.

Dispatch note:
`notes/dispatch/RAG-A11c_android_meningitis_sepsis_child_reviewed_card_runtime.md`.

Honesty notes:

- no mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`;
- matrix settings were `skip_build=true`; `skip_install=false` for
  `5554`/`5558`/`5560`, and `skip_install=true` for `5556` because the spot
  proof had just installed the same APK;
- an initial spot proof before the gate fix failed at
  `artifacts/instrumented_ui_smoke/20260424_221947_692/emulator-5556` because
  `MainActivity` stayed on results when no model was available;
- reviewed-card runtime now runs before the host/local model availability gate,
  so reviewed-card prompt proof works with `model_name: null`.

Same-block continuation added the remaining checked-in pilot cards below:
`RAG-A11d` and `RAG-A11e`, followed by the `RAG-CH2` runtime planner registry
cleanup.

#### RAG-A11d Infected Wound / Spreading Infection

Status: landed and validated; runtime remains developer/test gated.

Android now selects the `infected_wound_spreading_infection` / `GD-585`
reviewed card when the hidden runtime flag is enabled, exactly one eligible
card is loaded from the bundled pack, and the query contains wound context plus
spreading/local infection or systemic danger language. Conditionals still
activate from query text only.

Guardrails:

- exact card ID `infected_wound_spreading_infection`;
- exact card guide ID `GD-585`;
- card title `Wound Hygiene, Infection Prevention & Field Sanitation`;
- allowed review status `reviewed`, `pilot_reviewed`, or `approved`;
- review status in the bundled proof is `pilot_reviewed`;
- risk tier `critical` or `high`; bundled proof risk is `high`;
- runtime citation policy is the empty string;
- exactly one eligible card is selected from the pack;
- visible source rows required;
- conditionals activate from query text only;
- wound context plus spreading/local infection or systemic danger language is
  required;
- routine wound cleaning, generic infected-wound education, generic cellulitis
  education, antibiotic stockpiling, clean puncture wounds, newborn cord
  infections, and meningitis/sepsis rash prompts stay out of this strict
  reviewed-card fallback;
- A14 reviewed-evidence guard still applies.

Validation:

- focused JVM/build checks passed for `AnswerCardRuntimeTest`, Android test
  compilation, and debug/test APK assembly;
- connected instrumentation passed `11` tests per device for
  `AnswerCardDaoTest`, `OfflineAnswerEngineAnswerCardRuntimeTest`, and
  `DeveloperPanelRuntimeToggleTest`;
- prompt query
  `cut on my hand yesterday and now a red streak is moving up my arm` asserted
  `REVIEWED EVIDENCE`, `answer_card:infected_wound_spreading_infection`, card
  ID `infected_wound_spreading_infection`, card guide `GD-585`, review status
  `pilot_reviewed`, cited source ID `GD-585`, recent-thread metadata, and body
  fragments `Assess whether redness is spreading`,
  `Escalate urgently if red streaking`,
  `For spreading infection, clean 2 to 3 times daily`, and
  `Avoid: Do not reassure rapidly spreading redness`.

Four-emulator no-pack-push prompt matrix passed on APK SHA
`f1ae9a28e619069cbd8bc057070440e49948d30395e56d9fa727a2855c1de6d6`:

- tablet portrait:
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5554/20260424_225122_947/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5556/20260424_225122_947/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5558_landscape/20260424_225122_947/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5560_landscape/20260424_225122_947/emulator-5560`

Spot proof passed under
`artifacts/instrumented_ui_smoke/20260424_224638_937/emulator-5556`; copied
dump/logcat also live under
`artifacts/android_reviewed_card_a11d_infected_wound_harness_20260424/`.

Dispatch note:
`notes/dispatch/RAG-A11d_android_infected_wound_reviewed_card_runtime.md`.

Honesty notes:

- no mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`;
- the canonical matrix reused the already installed same APK after the spot and
  earlier matrix proof;
- a broad parallel Gradle attempt during development failed with unresolved
  Kotlin imports while another Gradle compile lane was running, but the focused
  serial JVM test, Android test compilation, connected instrumentation, and APK
  assembly passed afterward. Treat the parallel run as invalid evidence.

#### RAG-A11e Abdominal / Internal Bleeding

Status: landed and validated; runtime remains developer/test gated.

Android now selects the `abdominal_internal_bleeding` / `GD-380` reviewed card
when the hidden runtime flag is enabled, exactly one eligible card is loaded
from the bundled pack, and the query contains either abdominal trauma/impact
plus danger signs or GI bleeding plus danger signs. Conditionals still activate
from query text only.

Guardrails:

- exact card ID `abdominal_internal_bleeding`;
- exact card guide ID `GD-380`;
- card title `Acute Abdominal Emergencies & Surgical Decision-Making`;
- allowed review status `reviewed`, `pilot_reviewed`, or `approved`;
- review status in the bundled proof is `pilot_reviewed`;
- risk tier `critical` or `high`; bundled proof risk is `critical`;
- runtime citation policy is the empty string;
- exactly one eligible card is selected from the pack;
- visible source rows required;
- conditionals activate from query text only;
- activation is intentionally narrow: abdominal trauma/impact plus danger
  signs, or GI bleeding plus danger signs;
- pregnancy/ectopic prompts are deferred out of this branch;
- routine GI illness, reflux, constipation, hemorrhoids, mild cramps, black
  stool without danger signs, generic surgical abdomen, poisoning, choking,
  newborn, wound infection, and meningitis/sepsis prompts stay out of this
  strict reviewed-card fallback;
- A14 reviewed-evidence guard still applies.

Android DAO source ordering in proof is `GD-380|GD-232|GD-584`.

Validation:

- focused JVM/build checks passed for `AnswerCardRuntimeTest`,
  `OfflineAnswerEngineTest`, `AnswerContentFactoryTest`,
  `DetailReviewedCardMetadataBridgeTest`, Android test compilation, and
  debug/test APK assembly;
- connected instrumentation passed `12` tests per device for
  `AnswerCardDaoTest`, `OfflineAnswerEngineAnswerCardRuntimeTest`, and
  `DeveloperPanelRuntimeToggleTest`;
- prompt query `bike handlebar hit his belly and now he is pale and dizzy`
  asserted `REVIEWED EVIDENCE`, `answer_card:abdominal_internal_bleeding`,
  card ID `abdominal_internal_bleeding`, card guide `GD-380`, review status
  `pilot_reviewed`, cited source IDs `GD-380|GD-232|GD-584`, recent-thread
  metadata, and body fragments `Check for signs of serious internal injury`,
  `Urgent medical evaluation`,
  `Abdominal trauma or impact followed by belly pain`, and
  `Do not treat GI or rectal bleeding`.

Four-emulator no-pack-push prompt matrix passed on APK SHA
`3bd27a2aad4504328428aa78ec2e5e91414eafa05b5dfd1c918505264e249f2c`:

- tablet portrait:
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5554/20260424_230341_476/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5556/20260424_230341_476/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5558_landscape/20260424_230341_476/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5560_landscape/20260424_230341_503/emulator-5560`

Spot proof passed under
`artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_harness_20260424/20260424_230239_297/emulator-5556`.

Dispatch note:
`notes/dispatch/RAG-A11e_android_abdominal_internal_bleeding_reviewed_card_runtime.md`.

Honesty notes:

- no mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`;
- matrix settings were `skip_build=true`; `skip_install=false` for
  `5554`/`5558`/`5560`, and `skip_install=true` for `5556` because the spot
  proof had just installed the same APK;
- during review, the abdominal context predicate was tightened so bare
  `side`/`flank` wording does not trigger unless it is side/flank pain, and
  negated danger phrases such as `no dizziness or weakness` do not activate the
  GI-bleeding branch.

Same-block follow-up: after the six checked-in pilot cards reached
developer/test-gated runtime coverage, `RAG-CH2` extracted the repeated runtime
planner descriptor/helper shape in `AnswerCardRuntime` while keeping clinical
predicates explicit.

### RAG-CH2 Runtime Planner Registry

Status: landed and validated; behavior unchanged.

`AnswerCardRuntime` now uses an in-file `CardSpec` registry and shared planner
helper instead of six repeated `tryPlan(...)` branches and six near-identical
private planner methods. The registry carries only card ID, guide ID, structure
type, and predicate function.

Guardrails:

- clinical predicate methods and marker sets remain explicit;
- tests keep card-specific selector/plan/reject cases instead of hiding safety
  behavior in one broad table;
- `OfflineAnswerEngine`, `AnswerCardDao`, pack schema/export, UI labels, and
  product-enable gating were not changed;
- pregnancy-specific abdominal routing remains deferred;
- runtime remains off by default and A14 reviewed-evidence gates still apply.

Validation:

- focused JVM/build checks passed for `AnswerCardRuntimeTest`,
  `OfflineAnswerEngineTest`, `AnswerContentFactoryTest`,
  `DetailReviewedCardMetadataBridgeTest`, Android test compilation, and
  debug/test APK assembly;
- connected instrumentation passed `8` tests per device for
  `OfflineAnswerEngineAnswerCardRuntimeTest`;
- post-refactor phone prompt canary passed on APK SHA
  `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`, using
  the A11e abdominal/internal bleeding prompt and asserting reviewed-card
  metadata, cited source IDs `GD-380|GD-232|GD-584`, recent-thread metadata,
  and expected body fragments.

Artifact:
`artifacts/android_reviewed_card_ch2_runtime_registry_harness_20260424/20260424_231010_747/emulator-5556`.

Dispatch note:
`notes/dispatch/RAG-CH2_android_reviewed_card_runtime_planner_registry.md`.

Next recommendation: do not product-enable solely because the six-card runtime
pack is wired. Return to `RAG-A14` with exposure policy, layout proof,
proof-surface copy, and pack/version expectations reviewed together.

### RAG-A12 Real Pack End-to-End

Build a real mobile pack with answer-card tables, install it in the app, and run
the reviewed-card automation path without synthetic temp DB fixtures.

This is the point where screenshot proof becomes more product-representative.

### RAG-A13 Asset-Pack Answer-Card Parity

Status: landed.

The checked-in `android-app/app/src/main/assets/mobile_pack` was replaced with
the proven probe pack at
`artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`.

Bundled manifest generated at `2026-04-25T00:08:46.459832+00:00` reports
`answer_cards=6`. SQLite counts are `answer_cards=6`,
`answer_card_clauses=116`, and `answer_card_sources=19`; all six cards are
`pilot_reviewed`, with no missing clause or source links.

Proof:

- build/unit checks passed for `PackManifestTest`, `AnswerCardRuntimeTest`,
  `SessionMemoryTest`, `:app:assembleDebug`, and
  `:app:assembleDebugAndroidTest`;
- clean-install no-push matrix passed with APK SHA
  `be32ff34f66e7d478082ae2bd292f0e8315f231ec9f55c4852debfdd0e3cc553`;
- query `my child swallowed an unknown cleaner` selected
  `poisoning_unknown_ingestion`, guide `GD-898`, status `pilot_reviewed`, with
  recent-thread metadata assertion enabled;
- artifacts are under
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5554/20260424_210714_546/emulator-5554`,
  `.../clean_matrix_5556/20260424_210714_546/emulator-5556`,
  `.../clean_matrix_5558/20260424_210714_555/emulator-5558`, and
  `.../clean_matrix_5560/20260424_210714_546/emulator-5560`.

Harness cleanup: the smoke script now uses `mkdir -p` for the `run-as`
artifact pre-clean path; the warning-fix proof passed under
`artifacts/android_reviewed_card_asset_pack_harness_20260424/warning_fix_5556/20260424_210857_505/emulator-5556`.

Runtime remains off by default. This closes the manual probe-pack push gap for
fresh APK installs, but card expansion and product enablement are still gated.

### RAG-A14 Product Enablement Gate

Status: first enforceable guard landed; product enablement still open.

Dispatch:
`notes/dispatch/RAG-A14_android_reviewed_card_product_enablement_gate.md`.

This defines the gates required before reviewed-card runtime moves beyond
developer/test exposure: exposure policy, proof surface, screenshot/layout,
pack/version parity, and validation evidence. Runtime remains off by default,
the developer-panel control remains the only visible switch, and `RAG-A14` does
not expand runtime cards.

First guard now enforced:

- `AnswerContentFactory.inferAnswerSurface(...)` only labels
  `ReviewedCardEvidence` when the answer is deterministic, the rule ID starts
  `answer_card:`, `sourceCount > 0`, reviewed-card metadata is present,
  provenance is `reviewed_card_runtime`, and cited reviewed source guide IDs
  are non-empty.
- `PromptHarnessSmokeTest` fails closed for expected `REVIEWED EVIDENCE` unless
  the expected answer-card rule ID, primary source guide, card ID, card guide
  ID, review status, and cited reviewed source guide IDs are supplied, and the
  detail screen exposes at least one source row.
- `OfflineAnswerEngineAnswerCardRuntimeTest` covers the old-pack fallback:
  with runtime enabled and optional answer-card tables missing,
  `AnswerCardRuntime.tryPlan(...)` returns null rather than crashing or
  fabricating a card answer.

Validation:

- focused JVM/build command passed over `AnswerContentFactoryTest`,
  `AnswerCardRuntimeTest`, `SessionMemoryTest`,
  `DetailReviewedCardMetadataBridgeTest`, Android test compilation, and
  debug/test APK assembly;
- connected instrumentation passed over `AnswerCardDaoTest`,
  `OfflineAnswerEngineAnswerCardRuntimeTest`, and
  `DeveloperPanelRuntimeToggleTest`;
- prompt harness reinstall/no-pack-push proofs passed on APK SHA
  `ffe6f23038d20f488c73f5166649b4319b3413e37d7dfd759037f8ef7541eaca`.

Artifacts:

- `artifacts/android_reviewed_card_a14_guard_harness_20260424/20260424_213205_634/emulator-5556`
- `artifacts/android_reviewed_card_a14_guard_harness_20260424/clean_5554/20260424_213252_605/emulator-5554`

Honesty note: uninstall attempts before those two prompt proofs returned
`DELETE_FAILED_INTERNAL_ERROR`; the APK was reinstalled with `install -r`, and
no mobile pack push was performed. Do not describe these two prompt proofs as
clean-install proofs.

### RAG-A14a Layout / Pack Readiness

Status: landed and validated; product enablement still open.

Dispatch:
`notes/dispatch/RAG-A14a_android_reviewed_card_layout_pack_readiness.md`.

This is a proof-only follow-up to A14 after A11e and CH2. Runtime remains off by
default, the developer-panel control remains the only visible manual switch,
and no product-facing exposure policy changed.

Pack/version gate added:

- `AnswerCardAssetPackParityTest` reads the shipped
  `mobile_pack/senku_manifest.json` and `mobile_pack/senku_mobile.sqlite3`;
- asserts manifest `answer_cards=6`;
- asserts SQLite tables `answer_cards`, `answer_card_clauses`, and
  `answer_card_sources` exist;
- asserts counts `6`, `116`, and `19`;
- asserts `pack_meta.answer_card_count=6`;
- asserts all six expected card IDs are present and `pilot_reviewed`;
- asserts no card is missing clauses or sources.

Layout/proof packet:

- developer-panel screenshots for collapsed, expanded runtime-off, and
  runtime-on states were captured on all four documented emulators under
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/developer_panel/`;
- poisoning reviewed-card detail proof passed across phone/tablet portrait and
  landscape under
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/prompt_matrix_5554/20260424_232015_186/emulator-5554`,
  `.../prompt_matrix_5556/20260424_232015_189/emulator-5556`,
  `.../prompt_matrix_5558_landscape/20260424_232015_187/emulator-5558`, and
  `.../prompt_matrix_5560_landscape/20260424_232015_186/emulator-5560`.

Prompt assertions covered `REVIEWED EVIDENCE`,
`answer_card:poisoning_unknown_ingestion`, card/source metadata, cited source
ID `GD-898`, recent-thread metadata, and poisoning body fragments on APK SHA
`69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`.

Validation:

- focused Android test compilation passed;
- `AnswerCardAssetPackParityTest` passed `1` test per device across the fixed
  matrix;
- `DeveloperPanelRuntimeToggleTest` passed `1` test per device across the
  fixed matrix;
- prompt matrix passed with `model_name=null` and no mobile-pack push.

Honesty notes:

- A14 is still not fully closed;
- developer-panel screenshots are PNG-only from the test's `/sdcard/Download`
  path, while prompt proof artifacts include screenshot, UI dump, logcat, and
  summary;
- manual screenshot review found a proof-surface copy issue: the reviewed-card
  detail card showed `Reviewed evidence`, while the upper status strip still
  showed `STRONG EVIDENCE`; `RAG-A14b` fixes this;
- the next A14 slice should likely decide non-developer exposure
  policy/support language after screenshot review.

### RAG-A14b Proof-Surface Copy Harmonization

Status: landed and validated; product enablement still open.

Dispatch:
`notes/dispatch/RAG-A14b_android_reviewed_card_proof_surface_copy.md`.

This is the targeted follow-up from A14a screenshot review. Runtime remains off
by default, and no product exposure, pack schema, runtime predicate, card
coverage, or developer toggle behavior changed.

What changed:

- reviewed-card meta strip now displays `Reviewed evidence`;
- reviewed-card compact header/proof summary state now uses reviewed-card
  trust wording;
- the Paper-card/body label still displays `Reviewed evidence` /
  `REVIEWED EVIDENCE`;
- raw source-strength logic remains separate for non-reviewed flows.

Validation:

- focused build / Android test compilation passed;
- focused JVM tests passed for `AnswerContentFactoryTest` and
  `DetailReviewedCardMetadataBridgeTest`;
- single phone poisoning reviewed-card prompt proof passed under
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/single_5556/20260424_233307_217/emulator-5556`;
- four-posture prompt matrix passed under
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/matrix_5554/20260424_233418_753/emulator-5554`,
  `.../matrix_5556/20260424_233418_753/emulator-5556`,
  `.../matrix_5558_landscape/20260424_233418_753/emulator-5558`, and
  `.../matrix_5560_landscape/20260424_233418_753/emulator-5560`;
- APK SHA:
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`;
- artifact XML grep over the A14b proof packet found reviewed-card trust copy
  and no remaining `STRONG` hits for the reviewed-card prompt dumps.
- non-reviewed control prompt `generic_puncture` passed with `STRONG EVIDENCE`
  under
  `artifacts/android_reviewed_card_a14b_non_reviewed_control_20260424/strong_5556_packaged/20260424_234836_248/emulator-5556`;
- `PromptHarnessSmokeTest` expected-label matching now checks visible text and
  content descriptions, with case variants for reviewed/strong/moderate/
  deterministic labels.
- after that matcher refinement, the reviewed-card poisoning prompt was rerun
  successfully under
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/reviewed_5556_after_control/20260424_234949_096/emulator-5556`.

### RAG-A14c Forbidden Reviewed Label Harness

Status: landed and validated; product enablement still open.

Dispatch:
`notes/dispatch/RAG-A14c_android_forbidden_reviewed_label_harness.md`.

This is the negative-control follow-up after A14b. Runtime remains off by
default, and no product exposure, pack schema, runtime predicate, or card
coverage changed.

What changed:

- `PromptHarnessSmokeTest` now accepts
  `scriptedForbiddenAnswerSurfaceLabels`, with legacy singular alias support;
- forbidden labels are checked against UIAutomator visible text, accessibility
  content descriptions, and settled prompt signals;
- `run_android_instrumented_ui_smoke.ps1` exposes
  `-ScriptedForbiddenAnswerSurfaceLabels` and records
  `scripted_forbidden_answer_surface_labels` in `summary.json`;
- `run_android_prompt.ps1` exposes `-ForbiddenAnswerSurfaceLabels`.

Validation:

- PowerShell parse checks passed for both Android prompt wrapper scripts;
- Android test compilation and debug Android-test APK assembly passed;
- runtime-on non-reviewed phone canary for `generic_puncture` asserted
  `STRONG EVIDENCE` and forbade `REVIEWED EVIDENCE` under
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/canary_5556_runtime_on/20260425_084723_076/emulator-5556`;
- runtime-on reviewed inverse phone canary for
  `poisoning_unknown_ingestion` asserted `REVIEWED EVIDENCE` and forbade
  `STRONG EVIDENCE` under
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/reviewed_5556_runtime_on/20260425_084832_600/emulator-5556`;
- runtime-off four-posture non-reviewed control matrix passed under
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/matrix_5554/20260425_084910_317/emulator-5554`,
  `.../matrix_5556/20260425_084910_339/emulator-5556`,
  `.../matrix_5558_landscape/20260425_084910_317/emulator-5558`, and
  `.../matrix_5560_landscape/20260425_084910_317/emulator-5560`;
- APK SHA:
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.

Honesty note: A14c proves label exclusion in the harness. It does not approve
support language, choose non-developer exposure, or make reviewed cards
product-default.

### RAG-A14d Exposure Policy Decision

Status: closed with Option A; product enablement remains deferred.

Decision note:
`notes/ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`.

Decision:

- keep reviewed-card runtime developer/test-only;
- keep runtime default `off`;
- do not add a local-preview toggle, top-level product UI, card expansion, or
  product-default behavior from the current evidence set.

Evidence basis:

- A14a proved layout/pack readiness and exposed the mixed-copy issue later
  fixed by A14b;
- A14b proved reviewed-card trust-copy harmonization across the four postures;
- A14c proved forbidden-label controls for reviewed and non-reviewed flows;
- support-language strings for local preview have not been tested across the
  fixed four postures;
- phone landscape still deserves product polish because the composer can cover
  much of the reviewed-card detail content;
- A14c's strongest runtime-on inverse controls are phone canaries, not a full
  local-preview reviewed matrix.

Follow-up if non-developer exposure is still desired: run a narrow
support-language proof across phone/tablet portrait and landscape while keeping
runtime default `off`, without adding a top-level product UI or expanding
cards.

### RAG-CH3 Scripted Harness Contract Extraction

Status: landed and validated; behavior unchanged.

Dispatch:
`notes/dispatch/RAG-CH3_android_scripted_harness_contract.md`.

`ScriptedPromptHarnessContract` now owns androidTest-only scripted reviewed-card
Bundle parsing and pure fail-closed `REVIEWED EVIDENCE` expectation validation.
`PromptHarnessSmokeTest` keeps UIAutomator waits, activity assertions,
reflection helpers, settled detail signals, and artifact capture.

Guardrails:

- production app code unchanged;
- runtime defaults and product exposure unchanged;
- instrumentation argument names and pipe-delimited semantics preserved;
- no changes to `DetailActivity`, proof formatter copy, pack assets, card
  predicates, or UI resources.

Validation:

- Android test compile / test APK assembly passed;
- reviewed poisoning canary passed under
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/reviewed_5556/20260425_091908_871/emulator-5556`;
- runtime-on non-reviewed puncture canary passed under
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/non_reviewed_5556_runtime_on/20260425_092020_829/emulator-5556`;
- APK SHA:
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.

## UI Notes

Layout is not final, so reviewed-card UI work should be conservative:

- Use the existing evidence pill/status language before inventing new panels.
- Keep developer controls inside the developer panel.
- Avoid a landing-page or explanatory product surface.
- For phone portrait/landscape, watch vertical crowding inside the home scroll.
- For tablets, verify the developer panel does not visually dominate the home
  controls when expanded.

## Method Check

This path is getting closer:

- desktop proof defines answer/card/provenance contracts;
- mobile pack now carries the data;
- Android can read old and new packs;
- Android can compose reviewed-card answers without generation for six
  cards: `poisoning_unknown_ingestion`, `newborn_danger_sepsis`, and
  `choking_airway_obstruction`, `meningitis_sepsis_child`, and
  `infected_wound_spreading_infection`, and `abdominal_internal_bleeding`;
- Android can label those answers as reviewed-card evidence;
- instrumentation now has a hidden runtime switch.
- the developer panel can toggle that switch on-device without exported
  activity intents.
- the scripted prompt harness now proves the same runtime path through
  `MainActivity` and `DetailActivity` with a real pushed pack across the fixed
  four-emulator matrix.
- reviewed-card metadata now travels explicitly through runtime, prepared
  answer, answer run, detail state, Paper-card content, and proof formatter
  state. Harness assertions cover the metadata and its proof-summary visibility.
- the prompt harness can now assert forbidden answer-surface labels, so
  reviewed-card prompts fail if they leak `STRONG EVIDENCE` and non-reviewed
  prompts fail if they leak `REVIEWED EVIDENCE`.
- A14d chose Option A for exposure policy: keep reviewed-card runtime
  developer/test-only and default `off` until exact support language and
  product layout are proven across the fixed postures.
- `RAG-CH3` reduced the A14-era prompt-harness monolith by moving pure scripted
  reviewed-card contract parsing/validation into `ScriptedPromptHarnessContract`
  while leaving behavior and product exposure unchanged.
- `DetailReviewedCardMetadataBridge` now owns detail metadata transport/state
  and rule-id guarded lookup; this is code-health only, with behavior unchanged.
- reviewed-card answers now require at least one visible source row, and
  generic clean-bottle wording is guarded away from the poisoning pilot.
- a fresh real-pack export at
  `artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`
  contains `6` pilot cards, `116` card clauses, and `19` card sources, with no
  sourceless or clauseless cards.
- the checked-in Android asset pack now carries that same six-card answer-card
  data, so clean APK installs can prove the pilot path without a manual pack
  push. Runtime remains developer/test-flag gated.
- `RAG-A14` has its first enforceable guard landed: reviewed-card evidence
  labeling now requires deterministic `answer_card:` provenance plus present
  metadata, cited reviewed source guide IDs, and visible source rows in the
  harness. Runtime remains developer/test scoped until the remaining policy,
  layout, pack, and validation gates are closed, and it does not expand cards.
- `RAG-A11a` added the first runtime expansion beyond poisoning:
  `newborn_danger_sepsis` / `GD-284`, backed by bundled-pack prompt proof across
  phone/tablet portrait and landscape. This is progress through reviewed-card
  contracts, not broad retrieval patching.
- `RAG-A11b` added the second runtime expansion beyond poisoning:
  `choking_airway_obstruction` / `GD-232`, including infant branch proof and
  near-miss guards for allergy, panic, poisoning, and post-choking retained
  object drift.
- `RAG-A11c` added the third runtime expansion beyond poisoning:
  `meningitis_sepsis_child` / `GD-589`, including red-flag child
  meningitis/sepsis proof, visible-source/metadata assertions, and boundaries
  that keep bare meningitis-vs-viral comparisons plus public-health reporting
  prompts out of strict emergency-card fallback.
- `RAG-A11d` added the fourth runtime expansion beyond poisoning:
  `infected_wound_spreading_infection` / `GD-585`, including spreading wound
  infection proof, visible-source/metadata assertions, and boundaries that keep
  routine wound care, generic cellulitis education, newborn cord infection, and
  meningitis/sepsis rash prompts out of strict emergency-card fallback.
- `RAG-A11e` added the fifth runtime expansion beyond poisoning and completes
  the checked-in six-card pilot:
  `abdominal_internal_bleeding` / `GD-380`, including abdominal trauma/internal
  bleeding proof, visible-source/metadata assertions, and boundaries that keep
  pregnancy/ectopic prompts, routine GI prompts, and other emergency-card
  families out of strict fallback.

The next risk is not retrieval. The next risk is product enablement discipline:
only expose reviewed-card runtime where the card policy, proof surface, support
language, and layout have been verified together.

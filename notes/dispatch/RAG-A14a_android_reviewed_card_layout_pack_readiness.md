# RAG-A14a Android Reviewed-Card Layout / Pack Readiness

## Slice

`RAG-A14a` - add proof-only product-enablement readiness evidence after all
six checked-in pilot reviewed-card runtime paths and the CH2 planner registry.

## Outcome

Runtime remains developer/test scoped and off by default. No product default,
top-level control, onboarding, pack schema, runtime predicate, or UI copy was
product-enabled.

This slice adds two readiness gates:

- an instrumented shipped-asset-pack parity test;
- a four-emulator layout/proof packet covering developer-panel home states and
  a reviewed-card detail answer.

## Changed Surface

- `android-app/app/src/androidTest/java/com/senku/mobile/AnswerCardAssetPackParityTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/DeveloperPanelRuntimeToggleTest.java`

`DeveloperPanelRuntimeToggleTest` now captures three home screenshots:

- developer panel collapsed;
- developer panel expanded with reviewed-card runtime off;
- developer panel expanded with reviewed-card runtime on.

## Pack / Version Gate

`AnswerCardAssetPackParityTest` reads the shipped app assets:

- `mobile_pack/senku_manifest.json`;
- `mobile_pack/senku_mobile.sqlite3`.

It asserts:

- manifest `counts.answer_cards == 6`;
- tables exist: `answer_cards`, `answer_card_clauses`,
  `answer_card_sources`;
- DB counts are `6`, `116`, and `19`;
- `pack_meta.answer_card_count == 6`;
- expected card IDs are present:
  `poisoning_unknown_ingestion`, `newborn_danger_sepsis`,
  `choking_airway_obstruction`, `meningitis_sepsis_child`,
  `infected_wound_spreading_infection`, and
  `abdominal_internal_bleeding`;
- all cards are `pilot_reviewed`;
- every card has at least one clause and at least one source.

Independent pack facts:

- manifest/pack timestamp: `2026-04-25T00:08:46.459832+00:00`;
- `answer_cards=6`;
- `answer_card_clauses=116`;
- `answer_card_sources=19`;
- `pilot_reviewed` cards: `6`;
- sourceless/clauseless cards: `0`.

## Layout / Proof Evidence

Developer-panel screenshot packet:

`artifacts/android_reviewed_card_a14a_layout_readiness_20260424/developer_panel/`

Each documented emulator has three PNGs:

- `developer_panel_runtime_toggle__developer_panel_collapsed.png`;
- `developer_panel_runtime_toggle__developer_panel_expanded_runtime_off.png`;
- `developer_panel_runtime_toggle__reviewed_card_runtime_on.png`.

Reviewed-card detail proof used query:

```text
my child swallowed an unknown cleaner
```

Expected assertions:

- `REVIEWED EVIDENCE`;
- `answer_card:poisoning_unknown_ingestion`;
- primary source guide `GD-898`;
- card ID `poisoning_unknown_ingestion`;
- card guide ID `GD-898`;
- review status `pilot_reviewed`;
- cited reviewed source guide ID `GD-898`;
- recent-thread reviewed-card metadata;
- body fragments `Call poison control`, `Avoid: Do not induce vomiting`, and
  `GD-898`.

Four-emulator no-pack-push prompt matrix passed on APK SHA
`69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`:

- tablet portrait:
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/prompt_matrix_5554/20260424_232015_186/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/prompt_matrix_5556/20260424_232015_189/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/prompt_matrix_5558_landscape/20260424_232015_187/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/prompt_matrix_5560_landscape/20260424_232015_186/emulator-5560`

## Validation

Focused JVM/build validation passed before the A14a artifact packet:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.mobile.OfflineAnswerEngineTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --rerun-tasks --console=plain
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

A14a-specific validation passed:

```powershell
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin --console=plain
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardAssetPackParityTest" --console=plain
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.DeveloperPanelRuntimeToggleTest" --console=plain
```

`AnswerCardAssetPackParityTest` passed `1` test per device across the fixed
matrix. `DeveloperPanelRuntimeToggleTest` passed `1` test per device across the
fixed matrix.

## Honesty Notes

- A14 is still not fully closed.
- Runtime remains off by default.
- The developer-panel control remains the only visible manual switch.
- Manual screenshot review found a proof-surface copy issue: the reviewed-card
  detail card showed `Reviewed evidence`, while the upper status strip still
  showed `STRONG EVIDENCE`. Follow-up slice `RAG-A14b` fixes this.
- Developer-panel screenshots are PNG-only from the test's `/sdcard/Download`
  artifact path; prompt proof artifacts include screenshots, UI dumps, logcat,
  and summaries.
- This slice proves layout/readiness evidence and pack parity; it does not
  approve broader user-facing exposure.

## Next Slice

`RAG-A14b` now harmonizes the reviewed-card proof-surface copy. If
layout/proof surface is otherwise acceptable after manual screenshot review,
define the exact exposure policy and support language for any non-developer
use.

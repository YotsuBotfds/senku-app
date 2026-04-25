# RAG-A11e Android Abdominal / Internal Bleeding Reviewed-Card Runtime

## Slice

`RAG-A11e` - expand the disabled-by-default Android reviewed-card runtime to
the abdominal / internal bleeding pilot card:
`abdominal_internal_bleeding` / `GD-380`.

## Outcome

Android can now compose the `abdominal_internal_bleeding` reviewed card from
bundled answer-card tables when the developer/test runtime flag is enabled and
the query has either abdominal trauma/impact plus danger signs or GI bleeding
plus danger signs.

This is the sixth and final checked-in six-card pilot reviewed-card runtime
path. Runtime is still off by default, and product enablement remains governed
by `RAG-A14`.

## Guardrails

- Exact card allowlist: `abdominal_internal_bleeding`.
- Exact card guide: `GD-380`.
- Card title: `Acute Abdominal Emergencies & Surgical Decision-Making`.
- Eligible review status: `reviewed`, `pilot_reviewed`, or `approved`.
- Bundled proof review status: `pilot_reviewed`.
- Eligible risk tier: `critical` or `high`.
- Bundled proof risk: `critical`.
- Runtime citation policy: empty string.
- Exactly one eligible card is selected from the pack.
- The card must expose visible source rows.
- Conditionals activate from query text only.
- Activation is intentionally narrow:
  abdominal trauma/impact plus danger signs, or GI bleeding plus danger signs.
- Pregnancy/ectopic prompts are deferred out of this runtime branch.
- Routine GI illness, reflux, constipation, hemorrhoids, mild cramps, black
  stool without danger signs, generic surgical abdomen, poisoning, choking,
  newborn, wound infection, and meningitis/sepsis prompts must stay out of this
  strict reviewed-card fallback.
- Reviewed-card metadata is carried through `ReviewedCardMetadata`.
- Evidence labeling still requires the A14 guard:
  deterministic `answer_card:` rule, nonzero source count, present
  `reviewed_card_runtime` metadata, and non-empty cited reviewed source guide
  IDs.

Android DAO source ordering exposes `GD-380`, `GD-232`, and `GD-584` for the
runtime metadata/proof.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/AnswerCardRuntime.java`
- `android-app/app/src/test/java/com/senku/mobile/AnswerCardRuntimeTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/OfflineAnswerEngineAnswerCardRuntimeTest.java`

## Validation

Focused JVM/build validation passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.mobile.OfflineAnswerEngineTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --rerun-tasks --console=plain
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Connected instrumentation passed `12` tests per device:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest,com.senku.mobile.DeveloperPanelRuntimeToggleTest" --console=plain
```

Prompt proof used query:

```text
bike handlebar hit his belly and now he is pale and dizzy
```

Expected assertions:

- `REVIEWED EVIDENCE`;
- `answer_card:abdominal_internal_bleeding`;
- primary source guide `GD-380`;
- card ID `abdominal_internal_bleeding`;
- card guide ID `GD-380`;
- review status `pilot_reviewed`;
- cited reviewed source guide IDs `GD-380`, `GD-232`, and `GD-584`;
- recent-thread reviewed-card metadata;
- body fragments `Check for signs of serious internal injury`,
  `Urgent medical evaluation`,
  `Abdominal trauma or impact followed by belly pain`, and
  `Do not treat GI or rectal bleeding`.

Passing phone spot proof:
`artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_harness_20260424/20260424_230239_297/emulator-5556`.

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

## Honesty Notes

- No mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`.
- Matrix settings were `skip_build=true`; `skip_install=false` for
  `5554`/`5558`/`5560`, and `skip_install=true` for `5556` because the spot
  proof had just installed the same APK.
- During review, the abdominal context predicate was tightened so bare
  `side`/`flank` wording does not trigger unless it is side/flank pain, and
  negated danger phrases such as `no dizziness or weakness` do not activate the
  GI-bleeding branch.

## Next Slice

All six checked-in pilot cards now have developer/test-gated Android reviewed
runtime coverage. The next slice should be code health: extract the repeated
card descriptor/planner shape in `AnswerCardRuntime` before any product
enablement work expands the surface area.

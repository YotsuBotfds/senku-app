# RAG-A11a Android Newborn Danger Reviewed-Card Runtime

## Slice

`RAG-A11a` - expand the disabled-by-default Android reviewed-card runtime from
the poisoning pilot to one additional reviewed card:
`newborn_danger_sepsis` / `GD-284`.

## Outcome

Android can now compose the `newborn_danger_sepsis` reviewed card from bundled
answer-card tables when the developer/test runtime flag is enabled and the
query has both newborn-age language and a newborn danger-sign marker.

This remains developer/test gated. Runtime is still off by default, and product
enablement remains governed by `RAG-A14`.

## Guardrails

- Exact card allowlist: `newborn_danger_sepsis`.
- Exact card guide: `GD-284`.
- Eligible review status: `reviewed`, `pilot_reviewed`, or `approved`.
- Eligible risk tier: `critical` or `high`.
- Exactly one eligible card is selected from the pack.
- The card must expose at least one source row.
- Conditionals activate from query text only.
- Reviewed-card metadata is carried through `ReviewedCardMetadata`.
- Evidence labeling still requires the A14 guard:
  deterministic `answer_card:` rule, nonzero source count, present
  `reviewed_card_runtime` metadata, and non-empty cited reviewed source guide
  IDs.

The newborn predicate intentionally rejects arbitrary week counts. It accepts
explicit newborn/neonate/neonatal wording, 1-28 day old baby/infant wording, or
1-4 week old baby/infant wording, plus danger markers such as limp/floppy,
will-not-feed, hard-to-wake, fever/low temperature, breathing trouble, seizure,
green/bilious vomiting, no urine/no wet diaper, or cord infection signs.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/AnswerCardRuntime.java`
- `android-app/app/src/test/java/com/senku/mobile/AnswerCardRuntimeTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/OfflineAnswerEngineAnswerCardRuntimeTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

`PromptHarnessSmokeTest` also now accepts title-case/lowercase UI variants of
the reviewed-evidence label when the expected contract is
`REVIEWED EVIDENCE`, matching the current detail UI text while still asserting
the reviewed-card metadata contract.

## Validation

Focused JVM/build validation passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

Connected instrumentation passed:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest,com.senku.mobile.DeveloperPanelRuntimeToggleTest"
```

Prompt proof used query:

```text
newborn is limp, will not feed, and is hard to wake
```

Expected assertions:

- `REVIEWED EVIDENCE`;
- `answer_card:newborn_danger_sepsis`;
- primary source guide `GD-284`;
- card ID `newborn_danger_sepsis`;
- card guide ID `GD-284`;
- review status `pilot_reviewed`;
- cited reviewed source guide IDs `GD-284`, `GD-298`, `GD-492`, and `GD-617`;
- recent-thread reviewed-card metadata;
- body fragments `Treat breathing difficulty`, `Keep the newborn warm`,
  `Avoid: Do not treat fever`, and `GD-284`.

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

Honesty notes:

- No mobile-pack push was used for the prompt matrix; it used the checked-in
  six-card asset pack from `RAG-A13`.
- Treat the prompt matrix as reinstall/no-pack-push proof, not a strict clean
  uninstall proof.
- An initial phone proof failed because the harness waited for uppercase label
  text while the UI displayed `Reviewed evidence`; the harness now checks the
  same contract with UI-label casing variants.
- An initial landscape-phone matrix command omitted `-Orientation landscape`;
  the final tablet/phone landscape artifacts above are the trusted landscape
  proof.

## Next Slice

Recommended next runtime expansion is `RAG-A11b` choking / airway obstruction:
one exact card, one-card-only selection, query-text-only conditionals, and
negative near-misses for allergy/panic/poisoning drift.

# RAG-A11b Android Choking Airway Reviewed-Card Runtime

## Slice

`RAG-A11b` - expand the disabled-by-default Android reviewed-card runtime to
the choking / airway obstruction pilot card:
`choking_airway_obstruction` / `GD-232`.

## Outcome

Android can now compose the `choking_airway_obstruction` reviewed card from
bundled answer-card tables when the developer/test runtime flag is enabled and
the query has choking/foreign-body-airway context plus an action or complete
obstruction signal.

This remains developer/test gated. Runtime is still off by default, and product
enablement remains governed by `RAG-A14`.

## Guardrails

- Exact card allowlist: `choking_airway_obstruction`.
- Exact card guide: `GD-232`.
- Eligible review status: `reviewed`, `pilot_reviewed`, or `approved`.
- Eligible risk tier: `critical` or `high`.
- Exactly one eligible card is selected from the pack.
- The card must expose visible source rows.
- Conditionals activate from query text only.
- Allergy/anaphylaxis, poisoning/unknown-ingestion, panic-only, and
  post-choking retained-object near-misses are guarded away from this card.
- Reviewed-card metadata is carried through `ReviewedCardMetadata`.
- Evidence labeling still requires the A14 guard:
  deterministic `answer_card:` rule, nonzero source count, present
  `reviewed_card_runtime` metadata, and non-empty cited reviewed source guide
  IDs.

The choking card has no `runtime_citation_policy` value in the bundled pack; it
uses the default primary-owner behavior. Android DAO source ordering exposes
`GD-232`, `GD-284`, `GD-298`, and `GD-617` for the runtime metadata/proof.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/AnswerCardRuntime.java`
- `android-app/app/src/test/java/com/senku/mobile/AnswerCardRuntimeTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/OfflineAnswerEngineAnswerCardRuntimeTest.java`

`AnswerCardRuntime` also now includes up to four `forbidden_advice` clauses in
reviewed-card answers so the choking card can surface both the no-blind-finger
guard and the no-abdominal-thrusts-on-infants guard.

## Validation

Focused JVM/build validation passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

Connected instrumentation passed across the four AVD lanes:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest,com.senku.mobile.DeveloperPanelRuntimeToggleTest"
```

Prompt proof used query:

```text
baby is choking and cannot cry or cough
```

Expected assertions:

- `REVIEWED EVIDENCE`;
- `answer_card:choking_airway_obstruction`;
- primary source guide `GD-232`;
- card ID `choking_airway_obstruction`;
- card guide ID `GD-232`;
- review status `pilot_reviewed`;
- cited reviewed source guide IDs `GD-232`, `GD-284`, `GD-298`, and `GD-617`;
- recent-thread reviewed-card metadata;
- body fragments `start age-appropriate choking rescue`,
  `5 back blows followed by 5 chest thrusts`,
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

Initial phone spot proof:
`artifacts/android_reviewed_card_a11b_choking_harness_20260424/20260424_220345_636/emulator-5556`.

Honesty notes:

- No mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`.
- Treat the prompt matrix as reinstall/no-pack-push proof, not strict clean
  uninstall proof.
- `emulator-5556` was already installed from the spot proof, so its matrix lane
  reported `skip_install=true`; the other lanes reinstalled from the built APK.

## Next Slice

Recommended next runtime expansion is `RAG-A11c` meningitis / sepsis child,
but keep the `RAG-S12` boundary: fever/stiff-neck/red-flag prompts may activate
the strict card; bare meningitis-vs-viral comparison must stay generated or
compare/clarify, not deterministic emergency-card fallback.

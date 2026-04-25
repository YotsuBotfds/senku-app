# RAG-A11c Android Meningitis / Sepsis Child Reviewed-Card Runtime

## Slice

`RAG-A11c` - expand the disabled-by-default Android reviewed-card runtime to
the child meningitis / sepsis pilot card:
`meningitis_sepsis_child` / `GD-589`.

## Outcome

Android can now compose the `meningitis_sepsis_child` reviewed card from
bundled answer-card tables when the developer/test runtime flag is enabled and
the query has a red-flag child meningitis/sepsis clinical predicate.

This is the fourth developer/test-gated Android reviewed-card runtime path
after poisoning, newborn danger signs, and choking / airway obstruction.
Runtime is still off by default, and product enablement remains governed by
`RAG-A14`.

## Guardrails

- Exact card allowlist: `meningitis_sepsis_child`.
- Exact card guide: `GD-589`.
- Card title: `Sepsis Recognition, Escalation & Empiric Antibiotic Protocols`.
- Eligible review status: `reviewed`, `pilot_reviewed`, or `approved`.
- Bundled proof review status: `pilot_reviewed`.
- Eligible risk tier: `critical` or `high`.
- Bundled proof risk: `critical`.
- Runtime citation policy: empty string.
- Exactly one eligible card is selected from the pack.
- The card must expose visible source rows.
- Conditionals activate from query text only.
- Activation requires a red-flag clinical predicate.
- Bare `meningitis vs viral illness` / comparison prompts and public-health
  reporting/contact-tracing prompts must stay generated or compare/clarify, not
  strict emergency-card fallback.
- Reviewed-card metadata is carried through `ReviewedCardMetadata`.
- Evidence labeling still requires the A14 guard:
  deterministic `answer_card:` rule, nonzero source count, present
  `reviewed_card_runtime` metadata, and non-empty cited reviewed source guide
  IDs.

Android DAO source ordering exposes `GD-589`, `GD-235`, `GD-268`, `GD-284`,
and `GD-298` for the runtime metadata/proof. The YAML/card spec order differs
(`GD-589`, `GD-284`, `GD-298`, `GD-268`, `GD-235`), but Android proof records
DAO order.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/AnswerCardRuntime.java`
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- `android-app/app/src/test/java/com/senku/mobile/AnswerCardRuntimeTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/OfflineAnswerEngineAnswerCardRuntimeTest.java`

The extra runtime bug fixed during prompt proof: reviewed-card runtime now runs
before the host/local model availability gate, so reviewed-card prompt proof
works with `model_name: null`.

## Validation

Focused JVM/build validation passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.mobile.OfflineAnswerEngineTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --rerun-tasks --console=plain
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Connected instrumentation passed `10` tests per device:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest,com.senku.mobile.DeveloperPanelRuntimeToggleTest" --console=plain
```

Prompt proof used query:

```text
child has fever, stiff neck, and a purple rash that does not fade when pressed
```

Expected assertions:

- `REVIEWED EVIDENCE`;
- `answer_card:meningitis_sepsis_child`;
- primary source guide `GD-589`;
- card ID `meningitis_sepsis_child`;
- card guide ID `GD-589`;
- review status `pilot_reviewed`;
- cited reviewed source guide IDs `GD-589`, `GD-235`, `GD-268`, `GD-284`, and
  `GD-298`;
- recent-thread reviewed-card metadata;
- body fragments `suspected meningitis or meningococcemia`,
  `Escalate urgently`, `Fever with stiff neck`, and
  `Avoid: Do not route fever plus stiff neck`.

Passing phone spot proof:
`artifacts/instrumented_ui_smoke/20260424_222750_539/emulator-5556`.

Copied dump/logcat are also under:
`artifacts/android_reviewed_card_a11c_meningitis_harness_20260424/`.

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

## Honesty Notes

- No mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`.
- Matrix settings were `skip_build=true`; `skip_install=false` for
  `5554`/`5558`/`5560`, and `skip_install=true` for `5556` because the spot
  proof had just installed the same APK.
- Initial phone spot proof
  `artifacts/instrumented_ui_smoke/20260424_221947_692/emulator-5556` failed
  before the availability-gate fix because `MainActivity` stayed on results
  with no model.

## Next Slice

Choose the next one-card runtime expansion from the remaining high-risk
reviewed cards. Keep exact-card allowlisting, exactly-one-card selection,
visible-source requirements, query-text-only conditionals, and the `RAG-A14`
product-enable gate intact.

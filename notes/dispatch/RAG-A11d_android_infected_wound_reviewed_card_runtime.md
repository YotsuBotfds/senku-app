# RAG-A11d Android Infected Wound Reviewed-Card Runtime

## Slice

`RAG-A11d` - expand the disabled-by-default Android reviewed-card runtime to
the infected wound / spreading infection pilot card:
`infected_wound_spreading_infection` / `GD-585`.

## Outcome

Android can now compose the `infected_wound_spreading_infection` reviewed card
from bundled answer-card tables when the developer/test runtime flag is enabled
and the query has wound context plus spreading/local infection or systemic
danger language.

This is the fifth developer/test-gated Android reviewed-card runtime path after
poisoning, newborn danger signs, choking / airway obstruction, and child
meningitis / sepsis. Runtime is still off by default, and product enablement
remains governed by `RAG-A14`.

## Guardrails

- Exact card allowlist: `infected_wound_spreading_infection`.
- Exact card guide: `GD-585`.
- Card title: `Wound Hygiene, Infection Prevention & Field Sanitation`.
- Eligible review status: `reviewed`, `pilot_reviewed`, or `approved`.
- Bundled proof review status: `pilot_reviewed`.
- Eligible risk tier: `critical` or `high`.
- Bundled proof risk: `high`.
- Runtime citation policy: empty string.
- Exactly one eligible card is selected from the pack.
- The card must expose visible source rows.
- Conditionals activate from query text only.
- Activation requires wound context plus spreading/local infection or systemic
  danger language.
- Routine wound cleaning, generic infected-wound education, generic cellulitis
  education, antibiotic stockpiling, clean puncture wounds, newborn cord
  infections, and meningitis/sepsis rash prompts must stay out of this strict
  reviewed-card fallback.
- Reviewed-card metadata is carried through `ReviewedCardMetadata`.
- Evidence labeling still requires the A14 guard:
  deterministic `answer_card:` rule, nonzero source count, present
  `reviewed_card_runtime` metadata, and non-empty cited reviewed source guide
  IDs.

Android DAO source ordering exposes only `GD-585` for this runtime proof.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/AnswerCardRuntime.java`
- `android-app/app/src/test/java/com/senku/mobile/AnswerCardRuntimeTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/OfflineAnswerEngineAnswerCardRuntimeTest.java`

The earlier A11c availability-gate fix is what lets this prompt proof run with
`model_name: null`; no new model dependency was introduced.

## Validation

Focused JVM/build validation passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --console=plain
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Connected instrumentation passed `11` tests per device:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest,com.senku.mobile.DeveloperPanelRuntimeToggleTest" --console=plain
```

Prompt proof used query:

```text
cut on my hand yesterday and now a red streak is moving up my arm
```

Expected assertions:

- `REVIEWED EVIDENCE`;
- `answer_card:infected_wound_spreading_infection`;
- primary source guide `GD-585`;
- card ID `infected_wound_spreading_infection`;
- card guide ID `GD-585`;
- review status `pilot_reviewed`;
- cited reviewed source guide ID `GD-585`;
- recent-thread reviewed-card metadata;
- body fragments `Assess whether redness is spreading`,
  `Escalate urgently if red streaking`,
  `For spreading infection, clean 2 to 3 times daily`, and
  `Avoid: Do not reassure rapidly spreading redness`.

Passing phone spot proof:
`artifacts/instrumented_ui_smoke/20260424_224638_937/emulator-5556`.

Copied dump/logcat are also under:
`artifacts/android_reviewed_card_a11d_infected_wound_harness_20260424/`.

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

An earlier same-APK matrix also passed under timestamp
`20260424_224747_155`; the later `225122_947` run is the canonical record for
this dispatch.

## Honesty Notes

- No mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`.
- Matrix settings were `skip_build=true`; the canonical matrix reused the
  installed APK (`skip_install=true`) after the same APK had already been
  installed for the spot and earlier matrix proof.
- A broad parallel Gradle attempt during development failed with unresolved
  Kotlin imports while another Gradle compile lane was running. The focused
  serial JVM test, Android test compilation, connected instrumentation, and APK
  assembly all passed afterward; treat the parallel run as invalid evidence.

## Next Slice

Only one checked-in six-card pilot remains unwired:
`abdominal_internal_bleeding` / `GD-380`. Prefer a narrow A11e predicate around
abdominal trauma/shock and gastrointestinal bleeding. Defer pregnancy-specific
abdominal routing until the composer/card-clause shape can surface more than
the current first few urgent flags.

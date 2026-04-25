# RAG-CH2 Android Reviewed-Card Runtime Planner Registry

## Slice

`RAG-CH2` - code-health extraction after all six checked-in pilot reviewed
cards were wired into the disabled-by-default Android runtime.

## Outcome

`AnswerCardRuntime` now uses a small in-file `CardSpec` registry plus one
shared planner helper for reviewed-card runtime planning.

This collapses the six repeated `tryPlan(...)` branches and six near-identical
private `plan*FromCards(...)` implementations while preserving the exact
clinical predicates, marker sets, card guardrails, source construction,
metadata, product gating, and UI behavior proven through `RAG-A11e`.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/AnswerCardRuntime.java`

No docs, UI, pack schema/export, `OfflineAnswerEngine`, `AnswerCardDao`, or
product-enable code was changed by the runtime refactor itself.

## Guardrails

- Registry carries only card ID, guide ID, structure type, and predicate.
- Clinical predicates and marker sets remain explicit in
  `AnswerCardRuntime.java`; they were not externalized or data-driven.
- Card-specific selector tests remain readable and safety-case oriented.
- Existing test wrappers still call the same card-specific entry points.
- Runtime remains off by default.
- A14 reviewed-evidence guard remains unchanged.
- Pregnancy-specific abdominal routing remains deferred.

## Validation

Focused JVM/build validation passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.mobile.OfflineAnswerEngineTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --rerun-tasks --console=plain
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Connected reviewed-card runtime instrumentation passed across the fixed
four-emulator matrix:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest" --console=plain
```

That connected run executed `8` tests per device on:

- `5554` tablet portrait
- `5556` phone portrait
- `5558` tablet landscape
- `5560` phone landscape

Post-refactor prompt canary passed on APK SHA
`69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`:

- artifact:
  `artifacts/android_reviewed_card_ch2_runtime_registry_harness_20260424/20260424_231010_747/emulator-5556`
- query:
  `bike handlebar hit his belly and now he is pale and dizzy`
- expected:
  `REVIEWED EVIDENCE`, `answer_card:abdominal_internal_bleeding`, card/source
  metadata, cited source IDs `GD-380|GD-232|GD-584`, recent-thread metadata,
  and abdominal trauma/internal bleeding body fragments.

## Honesty Notes

- The prompt canary was a one-device phone portrait smoke after the refactor,
  not a second full four-posture prompt matrix. The full A11e prompt matrix on
  the pre-refactor equivalent behavior passed immediately before CH2.
- No mobile-pack push was used; the proof used the checked-in six-card asset
  pack from `RAG-A13`.

## Next Slice

Product enablement is still gated. Before enabling the runtime for normal users,
prefer an A14 follow-up that reviews exposure policy, layout screenshots,
proof-surface copy, and pack/version expectations together.

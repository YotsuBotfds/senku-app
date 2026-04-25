# RAG-A1 Android Answer Surface Receiving Shape

## Slice

Add a display-only Android answer provenance/surface-label receiving shape for
the desktop RAG-S10/S13 contract. This does not enable reviewed-card runtime.

## Role

Main agent or Android worker on `gpt-5.5 medium`. Use high only if touching
safety-critical routing or runtime answer composition.

## Preconditions

- Desktop contract proof:
  `artifacts/bench/rag_diagnostics_20260424_1750_rags13_code_health_final_smoke/report.md`
- Translation note:
  `notes/ANDROID_RAG_CONTRACT_TRANSLATION_20260424.md`
- Do not edit mobile-pack schema or `OfflineAnswerEngine` generation behavior
  in this slice.

## Target Files

- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/test/java/com/senku/ui/answer/AnswerContentFactoryTest.kt`

## Acceptance

- Android can infer display labels from existing state:
  - abstain -> `Abstain`
  - uncertain fit -> `LimitedFit`
  - deterministic -> `DeterministicRule`
  - confident non-deterministic with sources -> `GeneratedEvidence`
  - otherwise `Unknown`
- Existing constructor call sites keep compiling through safe defaults.
- No new runtime/card-backed behavior is enabled.
- Focused unit test for `AnswerContentFactoryTest` passes.

## Outcome

- Landed display-only receiving shape in:
  - `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
  - `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
  - `android-app/app/src/test/java/com/senku/ui/answer/AnswerContentFactoryTest.kt`
- Added `AnswerSurfaceLabel`, `answerProvenance`, and
  `reviewedCardBacked` fields with safe defaults.
- UI compact labels now distinguish deterministic, uncertain-fit, and abstain
  surfaces without claiming reviewed-card evidence.
- Validation passed:
  `.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.ui.answer.*" --tests "com.senku.mobile.OfflineAnswerEngineTest"`.

## Follow-Up

Next translation slice is now the backward-compatible Android reader for the
optional answer-card tables. Runtime card-backed answer composition comes after
that, starting with a single reviewed poisoning card.

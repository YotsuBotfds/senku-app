# RAG-A7 Android Reviewed-Card Runtime Config Extraction

## Slice

Keep the new reviewed-card runtime path from becoming more `OfflineAnswerEngine`
bulk, and add a stable config surface for instrumentation/future developer UI.

## Outcome

Landed.

- Added `AnswerCardRuntime` as the focused runtime planner/composer for the
  dark poisoning answer-card pilot.
- `OfflineAnswerEngine` now only asks the planner for an `AnswerPlan` and wraps
  it as a deterministic `PreparedAnswer`.
- Added `ReviewedCardRuntimeConfig` over the existing
  `senku_answer_card_runtime.reviewed_card_runtime_enabled` preference.
- Connected runtime instrumentation now uses the real config path for opt-in,
  and verifies default-off behavior at the planner layer.
- JVM card-runtime predicate/composition coverage now lives in
  `AnswerCardRuntimeTest` instead of bloating `OfflineAnswerEngineTest`.
- Review-scout follow-up tightened the query gate and source/evidence guard:
  generic clean-bottle wording no longer matches the poisoning pilot, and
  zero-source card plans cannot surface as reviewed evidence.

## Boundaries

- No visible button yet.
- No layout XML changes.
- No screenshot requirement for this slice.
- No runtime default change.
- No exported-activity intent override.

## Acceptance

Passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.mobile.OfflineAnswerEngineTest --tests com.senku.ui.answer.AnswerContentFactoryTest
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest"
```

Connected runtime/config instrumentation ran on the documented Senku matrix:

- `5556` phone portrait
- `5560` phone landscape
- `5554` tablet portrait
- `5558` tablet landscape

## Next

- A developer-panel button is feasible, but it touches all five
  `activity_main.xml` variants and should include screenshot/layout review.
- Keep normal product enablement deferred until reviewed-card policy is
  finalized.

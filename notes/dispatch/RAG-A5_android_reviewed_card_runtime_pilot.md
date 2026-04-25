# RAG-A5 Android Reviewed-Card Runtime Pilot

## Slice

Land the smallest Android runtime bridge from optional answer-card tables to an
actual answer, without changing default product behavior.

## Outcome

Landed.

- `OfflineAnswerEngine` now has a disabled-by-default reviewed-card runtime
  branch for the single `poisoning_unknown_ingestion` / `GD-898` pilot card.
- The branch is hidden behind preference
  `senku_answer_card_runtime.reviewed_card_runtime_enabled`.
- It runs after the existing host/model availability check and before broad
  retrieval.
- It requires an unknown-ingestion / poisoning query shape, exactly one selected
  card, risk tier `high` or `critical`, and review status `reviewed`,
  `pilot_reviewed`, or `approved`.
- It requires at least one visible answer-card source row before returning a
  reviewed-card answer.
- The answer body is composed from card clauses: required first actions,
  active question-triggered conditionals, first actions, red flags, and
  conservative negative advice.
- `AnswerCardDao` now accepts `pilot_reviewed`, matching the current pilot YAML
  cards.
- `OfflineAnswerEngineAnswerCardRuntimeTest` proves the full
  `prepare(...)` path on-device with a temp pack, hidden flag enabled, and host
  inference mode enabled to satisfy the existing model/host gate without
  calling generation.

## Boundaries

- No UI redesign.
- No public settings/toggle.
- No new `SearchResult` card fields.
- No broad reviewed-card selection across multiple guides.
- No activation of conditionals from retrieved context.

## Acceptance

Passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.OfflineAnswerEngineTest --tests com.senku.mobile.PackManifestTest --tests com.senku.mobile.PackInstallerTest
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:testDebugUnitTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.OfflineAnswerEngineTest
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest"
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest"
```

The connected DAO proof ran on the documented Senku matrix:

- `5556` phone portrait
- `5560` phone landscape
- `5554` tablet portrait
- `5558` tablet landscape

## Next

- Add reviewed-card UI surfacing by mapping deterministic `ruleId` values with
  prefix `answer_card:` to `ReviewedCardEvidence`.
- Add a product-level flag/toggle policy before enabling the runtime outside
  test fixtures.

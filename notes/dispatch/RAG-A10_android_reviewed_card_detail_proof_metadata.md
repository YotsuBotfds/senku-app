# RAG-A10 Android Reviewed-Card Detail-Proof Metadata

## Slice

Android reviewed-card metadata carrier and proof-surface wiring.

## Status

Landed 2026-04-24.

## Outcome

Reviewed-card metadata now travels as first-class Android state beside
`ruleId`, instead of being inferred from `SearchResult` display rows or
`answer_card:` string parsing.

The carried fields are:

- reviewed card ID: `poisoning_unknown_ingestion`;
- card guide ID: `GD-898`;
- review status: `pilot_reviewed`;
- runtime citation policy: `reviewed_source_family`;
- answer provenance: `reviewed_card_runtime`;
- cited reviewed source guide IDs, currently `GD-898` for the pilot card.

The normal scripted prompt harness can assert these fields on `DetailActivity`
state and in the generated proof summary.

## Code Changes

- Added `ReviewedCardMetadata`, a serializable null-object carrier with ordered,
  de-duplicated cited reviewed source guide IDs.
- `AnswerCardRuntime.AnswerPlan` now carries metadata populated while the full
  `AnswerCard` is still available.
- `OfflineAnswerEngine.PreparedAnswer` and `AnswerRun` carry the metadata
  through deterministic reviewed-card prepare/generate paths; other answer
  routes default to empty metadata.
- `DetailActivity` stores `currentReviewedCardMetadata`, preserves it in
  pending-answer intents, applies it on prepared preview and answer success,
  and passes it into Paper-card answer content and proof formatter state.
- `DetailProofPresentationFormatter` now includes reviewed-card proof lines for
  card ID, review status, card guide, and reviewed source guide IDs.
- `AnswerContent` can carry the metadata for Compose/Paper-card consumers.
- `PromptHarnessSmokeTest`, `run_android_instrumented_ui_smoke.ps1`, and
  `run_android_prompt.ps1` now expose explicit reviewed-card metadata
  expectations.
- Follow-up persistence is also wired: `SessionMemory` records
  `ReviewedCardMetadata` beside `ruleId`, round-trips it through JSON, exposes
  it on `TurnSnapshot`, and `MainActivity.openRecentThread(...)` passes it back
  into `DetailActivity.newAnswerIntent(...)`.
- Instant prepared answers opened from `MainActivity.openPendingAnswerDetail(...)`
  now record ready deterministic/abstain/uncertain-fit answers before opening
  detail, so reviewed-card answers can appear in recent threads with their
  metadata.

## Proof

Local build and focused JVM tests:

```powershell
.\gradlew.bat :app:compileDebugJavaWithJavac :app:compileDebugKotlin :app:compileDebugAndroidTestJavaWithJavac :app:testDebugUnitTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.ui.answer.AnswerContentFactoryTest
```

Persistence follow-up JVM proof:

```powershell
.\gradlew.bat :app:compileDebugJavaWithJavac :app:compileDebugKotlin :app:testDebugUnitTest --tests com.senku.mobile.SessionMemoryTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.ui.answer.AnswerContentFactoryTest
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin
```

Post-persistence APK regression prompt proof after fresh install and real-pack
repush:

- phone portrait `emulator-5556`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203421_850/emulator-5556/`
- phone landscape `emulator-5560`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203421_874/emulator-5560/`

Both passed with APK SHA
`1426d44a994f9c27ac268ba1eadb0bbf2614c14eee6f59c25de5decc69e1a3be`.

Recent-thread metadata assertion follow-up:

- `PromptHarnessSmokeTest#scriptedPromptFlowCompletes` now accepts
  `scriptedAssertRecentThreadReviewedCardMetadata`.
- The PowerShell wrappers expose
  `-ScriptedAssertRecentThreadReviewedCardMetadata` and
  `-AssertRecentThreadReviewedCardMetadata`.
- The assertion checks `ChatSessionStore.recentConversationPreviews(...)` for
  the current scripted query and verifies the reviewed-card metadata persisted
  into the latest turn.

Proof:

- phone portrait `emulator-5556`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203803_648/emulator-5556/`
- phone landscape `emulator-5560`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203803_648/emulator-5560/`

After scout review, the helper now keys the recent-thread lookup on the
scripted query itself. Follow-up proof:

- phone portrait `emulator-5556`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_203948_946/emulator-5556/`

The same helper now resets the in-process `ChatSessionStore` and re-reads
recent previews from preferences before asserting metadata again. Cold-restore
spot proof:

- phone portrait `emulator-5556`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_204236_127/emulator-5556/`

Connected runtime proof:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest"
```

Passed on all four documented emulators.

Trusted final prompt matrix after reinstalling the fresh debug app/test APKs
and repushing the real probe pack:

- tablet portrait `emulator-5554`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_371/emulator-5554/`
- phone portrait `emulator-5556`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_366/emulator-5556/`
- tablet landscape `emulator-5558`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_371/emulator-5558/`
- phone landscape `emulator-5560`:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_371/emulator-5560/`

All four passed `PromptHarnessSmokeTest#scriptedPromptFlowCompletes` with
assertions for:

- `REVIEWED EVIDENCE`;
- `answer_card:poisoning_unknown_ingestion`;
- primary source `GD-898`;
- reviewed card ID `poisoning_unknown_ingestion`;
- card guide ID `GD-898`;
- review status `pilot_reviewed`;
- cited reviewed source guide ID `GD-898`;
- body fragments `Call poison control`, `Avoid: Do not induce vomiting`, and
  `GD-898`;
- proof-summary visibility for the reviewed-card metadata.

## Repro Command Shape

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device emulator-5556 `
  -SkipBuild `
  -SkipInstall `
  -SmokeProfile custom `
  -Orientation portrait `
  -ScriptedQuery "my child swallowed an unknown cleaner" `
  -ScriptedAsk `
  -ScriptedExpectedSurface detail `
  -ScriptedExpectedTitle "my child swallowed an unknown cleaner" `
  -ScriptedTimeoutMs 30000 `
  -EnableHostInferenceSmoke `
  -AllowHostFallback `
  -ScriptedEnableReviewedCardRuntime `
  -ScriptedExpectedAnswerSurfaceLabel "REVIEWED EVIDENCE" `
  -ScriptedExpectedRuleId "answer_card:poisoning_unknown_ingestion" `
  -ScriptedExpectedSourceGuideId "GD-898" `
  -ScriptedExpectedReviewedCardId "poisoning_unknown_ingestion" `
  -ScriptedExpectedReviewedCardGuideId "GD-898" `
  -ScriptedExpectedReviewedCardReviewStatus "pilot_reviewed" `
  -ScriptedExpectedReviewedCardSourceGuideIds "GD-898" `
  -ScriptedExpectedBodyContains "Call poison control|Avoid: Do not induce vomiting|GD-898" `
  -ArtifactRoot "artifacts/android_reviewed_card_metadata_harness_20260424" `
  -SkipDeviceLock
```

Important: if a fresh APK is installed, repush
`artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`
before running the trusted prompt harness. The checked-in asset pack still does
not contain answer-card tables.

## Follow-Ups

- Product enablement remains deferred; the runtime is still off by default.
- Phone-landscape still has tight vertical space around the docked composer.
  The visible label is correct, but the full Paper card and proof affordance may
  need layout work before product enablement.
- No dedicated recent-thread UI instrumentation was added in this slice; the
  persistence path is covered by `SessionMemoryTest` and compile coverage.

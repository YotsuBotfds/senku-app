# RAG-CH1 Android Detail Metadata Bridge

## Slice

`RAG-CH1` - narrow reviewed-card detail metadata bridge extraction.

## Outcome

`DetailReviewedCardMetadataBridge` now owns reviewed-card metadata intent
transport, current detail state, reset/set behavior, and rule-id guarded lookup
for tablet turn binding. This reduces future reviewed-card edits in
`DetailActivity` without changing product behavior.

The prompt harness was updated to read reviewed-card metadata through the
bridge while retaining fallback support for the old field name.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/DetailReviewedCardMetadataBridge.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailReviewedCardMetadataBridgeTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

## Validation

Focused compile/unit:

```powershell
.\gradlew.bat :app:compileDebugJavaWithJavac :app:compileDebugKotlin :app:testDebugUnitTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --tests com.senku.mobile.SessionMemoryTest --tests com.senku.ui.answer.AnswerContentFactoryTest
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --tests com.senku.mobile.SessionMemoryTest --tests com.senku.ui.answer.AnswerContentFactoryTest
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

Clean-install no-push bridge matrix passed with APK SHA
`7dae8277227c8f5ddc2997c412203da1309addff6e76f7f278e1e0734b80464c`:

- tablet portrait:
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5554/20260424_212147_831/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5556/20260424_212147_832/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5558/20260424_212147_831/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5560/20260424_212147_828/emulator-5560`

The matrix used query `my child swallowed an unknown cleaner` with runtime
enabled by harness and asserted `REVIEWED EVIDENCE`,
`answer_card:poisoning_unknown_ingestion`, guide `GD-898`, review status
`pilot_reviewed`, cited reviewed source `GD-898`, body fragments, and
recent-thread metadata.

## Note

An initial phone proof failed because the harness still reflected the removed
`currentReviewedCardMetadata` field. The app launch path was preserved; the
harness now reads `reviewedCardMetadataBridge.current()` and the full matrix
passes.

# RAG-A3 Android Answer-Card Reader

## Slice

Add a backward-compatible Android reader/model layer for optional mobile-pack
answer-card metadata tables without changing answer behavior.

## Role

Android worker on `gpt-5.5 medium`. Use high only if extending into runtime
answer composition or safety-critical answer wording.

## Preconditions

- `RAG-A1` display-only answer surface receiving shape landed.
- `RAG-A2` mobile-pack export writes optional `answer_cards`,
  `answer_card_clauses`, and `answer_card_sources` tables.
- Old packs without those tables must continue to work.

## Target Files

- `android-app/app/src/main/java/com/senku/mobile/AnswerCard.java`
- `android-app/app/src/main/java/com/senku/mobile/AnswerCardClause.java`
- `android-app/app/src/main/java/com/senku/mobile/AnswerCardSource.java`
- `android-app/app/src/main/java/com/senku/mobile/AnswerCardDao.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/main/java/com/senku/mobile/PackManifest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackManifestTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/AnswerCardDaoTest.java`

## Outcome

- Added plain Java answer-card models for card metadata, clauses, and reviewed
  source rows.
- Added package-private `AnswerCardDao` with:
  - `loadCardsForGuideIds(Set<String> guideIds, int limit)`;
  - reviewed-only filtering;
  - stable `guide_id`, `card_id` ordering;
  - parsed `trigger_terms_json` and `sections_json`;
  - old-pack fallback to empty results when any optional table is absent.
- `PackRepository` exposes a narrow
  `loadAnswerCardsForGuideIds(Set<String> guideIds, int limit)` delegate.
- `PackManifest` now parses `counts.answer_cards` with `optInt(..., 0)`.
- Added instrumentation coverage for DAO old-pack fallback, reviewed-only
  filtering, JSON parsing, source rows, and limit/order behavior.

## Validation

- Passed:
  `.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:testDebugUnitTest --tests com.senku.mobile.PackManifestTest --tests com.senku.mobile.PackInstallerTest`
- An accidental generic `Medium_Phone_API_36.1` run was not accepted as matrix
  proof and was superseded.
- Started the documented Senku matrix headless from the local Android SDK:
  - phone portrait: `emulator-5556` / `Senku_Large_4`
  - phone landscape: `emulator-5560` / `Senku_Large_3`
  - tablet portrait: `emulator-5554` / `Senku_Tablet_2`
  - tablet landscape: `emulator-5558` / `Senku_Tablet`
- Passed connected DAO instrumentation across all four Senku lanes:
  `.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest"`.
  Gradle started `3` tests on each AVD and finished `BUILD SUCCESSFUL`.

## Boundaries

- No changes to `SearchResult`, `OfflineAnswerEngine`, prompt building, ranking,
  or UI behavior.
- Do not make answer-card tables required in `PackInstaller`.
- Do not compose runtime answers from cards until a single-card pilot slice has
  explicit safety gating and connected-device validation.

## Next Slice

Pilot a disabled-by-default Android card-backed answer path for exactly one
reviewed high-risk card, likely `poisoning_unknown_ingestion`, after connected
DAO instrumentation passes on an emulator/device.

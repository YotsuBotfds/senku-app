# RAG-A15c - Android Pack Schema Census Hardening

Date: 2026-04-26

## Scope

Add test-only Android migration guards for current-head pack inventory and old
answer-card schema compatibility.

No production code, runtime predicates, UI, checked-in assets, or product
exposure changed.

## Changes

`AnswerCardCurrentHeadPackCensusTest` now checks more of the already-pushed
current-head 271-card pack without invoking runtime planning:

- `answer_cards=271`
- `answer_card_clauses=6945`
- `answer_card_sources=311`
- review status is limited to `approved` or `pilot_reviewed`
- required card fields are non-empty:
  `card_id`, `guide_id`, `slug`, `title`, `risk_tier`, `evidence_owner`,
  `review_status`, `routine_boundary`, and `acceptable_uncertain_fit`

`AnswerCardDaoTest` now covers additional old-schema fail-closed cases:

- `answer_card_clauses` exists but is missing `trigger_terms_json`
- `answer_card_sources` exists but is missing `sections_json` / `is_primary`

Both cases must return no cards instead of crashing or fabricating partial
reviewed-card data.

## Validation

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --console=plain
# BUILD SUCCESSFUL

.\gradlew.bat :app:connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.AnswerCardCurrentHeadPackCensusTest" `
  --console=plain
# BUILD SUCCESSFUL
```

Connected result summary:

- `Senku_Large_4`: `9` tests, `0` skipped; current-head census executed.
- `Senku_Large_3`: `9` tests, `0` skipped; current-head census executed.
- `Senku_Tablet_2`: `9` tests, `1` skipped; current-head pushed pack absent.
- `Senku_Tablet`: `9` tests, `1` skipped; current-head pushed pack absent.

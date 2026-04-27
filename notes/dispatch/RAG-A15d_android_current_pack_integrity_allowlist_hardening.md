# RAG-A15d - Android Current-Pack Integrity and Allowlist Hardening

Date: 2026-04-27

## Scope

Further harden Android test-only migration guards for the pushed current-head
271-card pack.

No production runtime predicates, UI, checked-in assets, or product exposure
changed.

## Changes

`AnswerCardCurrentHeadPackCensusTest` now also asserts:

- no orphan `answer_card_clauses.card_id` rows;
- no orphan `answer_card_sources.card_id` rows;
- `answer_card_sources.source_guide_id` is non-empty;
- `answer_card_clauses.text` is non-empty;
- every answer card has at least one primary source.

`AnswerCardRuntimeAllowlistCurrentHeadTest` now samples non-pilot cards from
deterministic buckets instead of only the first 24 card IDs:

- 8 first non-pilot cards by `card_id ASC`;
- 8 last non-pilot cards by `card_id DESC`;
- 8 high/critical non-pilot cards, with critical cards first;
- deterministic top-up if buckets overlap.

Assertion messages now include `card_id/guide_id/risk_tier` for each sampled
non-pilot card.

## Validation

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --console=plain
# BUILD SUCCESSFUL

.\gradlew.bat :app:connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardCurrentHeadPackCensusTest,com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest" `
  --console=plain
# BUILD SUCCESSFUL; skipped on lanes without the pushed current-head pack.
```

Direct pushed-pack proof on `emulator-5556` after restoring the debug app/test
package and re-pushing `artifacts/mobile_pack/senku_current_head_20260426_232032`:

```powershell
adb -s emulator-5556 shell am instrument -w `
  -e class com.senku.mobile.AnswerCardCurrentHeadPackCensusTest,com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest `
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
# OK (2 tests)
```

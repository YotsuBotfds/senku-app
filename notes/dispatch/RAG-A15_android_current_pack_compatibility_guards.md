# RAG-A15 - Android Current-Pack Compatibility Guards

Date: 2026-04-26

## Scope

Add test-only Android guards for mobile-pack migration confidence:

- old answer-card tables that are present but column-incompatible fail closed;
- an already-pushed current-head pack can be censused without invoking reviewed
  card runtime selection.

No production code, asset files, runtime predicates, defaults, or UI exposure
changed.

## Changes

`AnswerCardDaoTest` now covers an old `answer_cards` schema that has the table
and review-status data but is missing newer columns required by DAO reads. The
DAO is expected to return no cards instead of throwing into runtime behavior.

`AnswerCardCurrentHeadPackCensusTest` is an assumption-gated instrumentation
test. It reads `files/mobile_pack` directly from the app sandbox and skips unless
that installed pack is already the pushed current-head 271-card pack. When the
pack is present, it checks:

- `answer_cards`, `answer_card_clauses`, and `answer_card_sources` tables exist;
- `answer_cards` count is `271`;
- `pack_meta.answer_card_count` is `271`;
- every answer card has at least one clause;
- every answer card has at least one source.

The census intentionally does not call `PackInstaller.ensureInstalled(...)` so a
clean emulator does not replace the pushed pack with checked-in assets before the
skip check.

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

- `Senku_Large_4`: `7` tests, `0` skipped; pushed-pack census ran.
- `Senku_Large_3`: `7` tests, `0` skipped; pushed-pack census ran.
- `Senku_Tablet_2`: `7` tests, `1` skipped; current-head pushed pack absent.
- `Senku_Tablet`: `7` tests, `1` skipped; current-head pushed pack absent.

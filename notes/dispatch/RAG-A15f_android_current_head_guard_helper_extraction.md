# RAG-A15f - Android Current-Head Guard Helper Extraction

Date: 2026-04-27

## Scope

Extract shared androidTest-only support for pushed current-head pack guards.

No production code, runtime predicates, UI, checked-in assets, or product
exposure changed.

## Change

Added `CurrentHeadAnswerCardPackTestSupport` for:

- current-head answer-card count constant;
- app-sandbox `mobile_pack` manifest and SQLite file lookup;
- manifest file reading;
- shared assumption gating for tests that require the pushed 271-card pack.

Updated:

- `AnswerCardCurrentHeadPackCensusTest`
- `AnswerCardRuntimeAllowlistCurrentHeadTest`

The extraction keeps existing assertions and behavior unchanged while reducing
drift across future current-head guard additions.

## Validation

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --console=plain
# BUILD SUCCESSFUL

.\gradlew.bat :app:connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardCurrentHeadPackCensusTest,com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest" `
  --console=plain
# BUILD SUCCESSFUL; skipped where the pushed current-head pack was absent.
```

Direct pushed-pack proof on `emulator-5556` after restoring the debug app/test
package and re-pushing `artifacts/mobile_pack/senku_current_head_20260426_232032`:

```powershell
adb -s emulator-5556 shell am instrument -w `
  -e class com.senku.mobile.AnswerCardCurrentHeadPackCensusTest,com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest `
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
# OK (2 tests)
```

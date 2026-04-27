# RAG-A15b - Android Current-Head Runtime Allowlist Guard

Date: 2026-04-26

## Scope

Add an assumption-gated Android instrumentation guard proving that a pushed
current-head 271-card pack does not broaden reviewed-card runtime selection
beyond the six explicitly wired pilot cards.

No production runtime predicates, UI, checked-in assets, or defaults changed.

## Change

`AnswerCardRuntimeAllowlistCurrentHeadTest` reads the app-sandbox
`files/mobile_pack` directly and skips unless the installed manifest reports
`answer_cards=271`.

When the pushed current-head pack is present, the test:

- asserts the installed `answer_cards` table count is `271`;
- proves all six pilot runtime queries still plan to their expected
  `answer_card:*` rule IDs and reviewed-card metadata;
- samples 24 non-pilot current-head cards and verifies none satisfy any of the
  six runtime planner hooks.

The test uses the existing runtime test seams and does not call or modify product
exposure controls.

## Validation

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --console=plain
# BUILD SUCCESSFUL

.\gradlew.bat :app:connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest" `
  --console=plain
# BUILD SUCCESSFUL; skipped on lanes without the pushed current-head pack.
```

Direct proof after reinstalling the debug app/test packages and re-pushing
`artifacts/mobile_pack/senku_current_head_20260426_232032` to `emulator-5556`:

```powershell
adb -s emulator-5556 shell am instrument -w `
  -e class com.senku.mobile.AnswerCardRuntimeAllowlistCurrentHeadTest `
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
# OK (1 test)
```

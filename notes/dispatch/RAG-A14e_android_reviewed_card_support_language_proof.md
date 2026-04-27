# RAG-A14e Android Reviewed-Card Support-Language Proof

Date: 2026-04-26

## Scope

Add one developer-panel support line for the reviewed-card runtime toggle:

> Developer-only proof mode. Default app behavior stays off.

This is a proof/support-language slice only. It does not product-enable
reviewed-card runtime, change the default preference, add top-level UI, or
expand Android runtime cards beyond the six checked-in pilot cards.

## Changed Surfaces

- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/DeveloperPanelRuntimeToggleTest.java`

The instrumentation test now asserts the exact support copy and captures
screenshots/dumps through the shared `files/test-artifacts` harness directory.

## Validation

Compile/build:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
# BUILD SUCCESSFUL
```

Four-lane developer-panel proof:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device <fixed-matrix-device> `
  -TestClass com.senku.mobile.DeveloperPanelRuntimeToggleTest `
  -SmokeProfile custom `
  -Orientation <portrait-or-landscape> `
  -ArtifactRoot artifacts/android_reviewed_card_developer_panel_runtime_toggle/<lane> `
  -CaptureLogcat `
  -ClearLogcatBeforeRun
# OK (1 test) on each lane; wrapper status pass on each lane.
```

Passing artifact roots:

- `artifacts/android_reviewed_card_developer_panel_runtime_toggle/phone_portrait/20260426_231751_289/emulator-5556/`
- `artifacts/android_reviewed_card_developer_panel_runtime_toggle/phone_landscape/20260426_231809_710/emulator-5560/`
- `artifacts/android_reviewed_card_developer_panel_runtime_toggle/tablet_portrait/20260426_231835_664/emulator-5554/`
- `artifacts/android_reviewed_card_developer_panel_runtime_toggle/tablet_landscape/20260426_231859_538/emulator-5558/`

Each passing lane copied three screenshots and three UI dumps:

- `developer_panel_runtime_toggle__developer_panel_collapsed`
- `developer_panel_runtime_toggle__developer_panel_expanded_runtime_off`
- `developer_panel_runtime_toggle__reviewed_card_runtime_on`

## Boundary

Runtime remains developer/test scoped and default `off`. The Android asset pack
still contains the six pilot reviewed-card runtime cards; broader desktop card
inventory remains pack/migration backlog, not product runtime coverage.

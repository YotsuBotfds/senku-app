# RAG-A8 Android Reviewed-Card Developer Toggle

## Slice

Expose the hidden Android reviewed-card runtime flag through the existing
developer panel only, with matrix screenshot proof.

## Outcome

Landed.

- Added `reviewed_card_runtime_button` to all five `activity_main.xml`
  variants.
- Wired `MainActivity` to `ReviewedCardRuntimeConfig` for on/off label updates
  and preference writes.
- Refreshed the button label immediately after binding and during `onResume()`
  so external preference changes do not leave stale UI.
- Kept the control inside `developer_content`; no top-level product toggle or
  exported activity intent override was added.
- Made the phone-landscape left rail scrollable after screenshot review showed
  expanded developer content could clip on short screens.
- Added `DeveloperPanelRuntimeToggleTest`, which opens the developer panel,
  toggles the reviewed-card runtime on/off, asserts the config state and label,
  and captures matrix screenshots.

## Artifacts

Final screenshot bundle:

`artifacts/android_reviewed_card_toggle_20260424_193049/`

Includes:

- `phone_portrait_5556/developer_panel_runtime_toggle_on.png`
- `phone_landscape_5560/developer_panel_runtime_toggle_on.png`
- `tablet_portrait_5554/developer_panel_runtime_toggle_on.png`
- `tablet_landscape_5558/developer_panel_runtime_toggle_on.png`

## Validation

Passed:

```powershell
.\gradlew.bat :app:compileDebugJavaWithJavac :app:compileDebugAndroidTestJavaWithJavac
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.DeveloperPanelRuntimeToggleTest"
```

The connected test ran on:

- `5556` phone portrait
- `5560` phone landscape
- `5554` tablet portrait
- `5558` tablet landscape

## UI Notes

- The collapsed developer panel is still visible on home/browse. That was
  pre-existing, but it remains a product-surface question while layout is not
  finalized.
- Tablet portrait and phone portrait can still be vertically dense when
  developer tools are expanded. The new toggle is visible in the screenshot
  artifact, but final UI should decide whether developer controls remain on the
  primary home surface.

## Next

- `RAG-A9`: use this config path in the prompt automation harness to prove a
  reviewed-card answer surface end-to-end without exported intent overrides.

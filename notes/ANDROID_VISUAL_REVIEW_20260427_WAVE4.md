# Android Visual Review 20260427 Wave 4

Worker AF. Scope: post-`0f86cbc` local visual smoke validation only. No
production code edits.

## Commands Run

- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -BuildOnly -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedSeededFollowUpThreadShowsInlineHistory' -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc/tablet_thread_portrait -CaptureLogcat -ClearLogcatBeforeRun`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5558 -SkipBuild -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#guideDetailDestinationKeepsSourceContextOnTabletLandscape' -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc/tablet_answer_landscape -CaptureLogcat -ClearLogcatBeforeRun`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -SkipBuild -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#homeEntryShowsPrimaryBrowseAndAskLanes' -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc/phone_home_landscape -CaptureLogcat -ClearLogcatBeforeRun`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#deterministicAskNavigatesToDetailScreen' -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc/phone_answer_portrait -CaptureLogcat -ClearLogcatBeforeRun`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest' -ScriptedQuery 'choking' -ScriptedAsk -ScriptedExpectedSurface detail -ScriptedCaptureLabel 'tablet_emergency_portrait' -ScriptedTimeoutMs 20000 -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc/tablet_emergency_portrait -CaptureLogcat`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling' -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc/tablet_search_portrait -CaptureLogcat`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5558 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling' -ArtifactRoot artifacts/android_visual_wave4_post_0f86cbc/tablet_search_landscape -CaptureLogcat`

## Harness Result

All focused smokes passed at the instrumentation/artifact level: `OK (1 test)`,
`status: pass`, one screenshot and one UI dump copied per state, and requested
orientation matched actual orientation. No `OK (0 tests)` invocation failures
were observed.

## Artifacts

- Tablet thread portrait:
  `artifacts/android_visual_wave4_post_0f86cbc/tablet_thread_portrait/20260427_221412_181/emulator-5554/screenshots/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- Tablet emergency portrait:
  `artifacts/android_visual_wave4_post_0f86cbc/tablet_emergency_portrait/20260427_221503_279/emulator-5554/screenshots/scriptedPromptFlowCompletes__tablet_emergency_portrait.png`
- Tablet answer/detail landscape:
  `artifacts/android_visual_wave4_post_0f86cbc/tablet_answer_landscape/20260427_221412_180/emulator-5558/screenshots/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
- Tablet search portrait:
  `artifacts/android_visual_wave4_post_0f86cbc/tablet_search_portrait/20260427_221526_872/emulator-5554/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- Tablet search landscape:
  `artifacts/android_visual_wave4_post_0f86cbc/tablet_search_landscape/20260427_221550_016/emulator-5558/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- Phone home landscape:
  `artifacts/android_visual_wave4_post_0f86cbc/phone_home_landscape/20260427_221412_181/emulator-5560/screenshots/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- Phone answer portrait:
  `artifacts/android_visual_wave4_post_0f86cbc/phone_answer_portrait/20260427_221412_180/emulator-5556/screenshots/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`

## Remaining Mismatches Against Mocks

- Tablet thread portrait vs `artifacts/mocks/thread-tablet-portrait.png`:
  structurally improved and not collapsed, but still denser/heavier than the
  target. Capture keeps a wide left thread rail and a source/cross-reference
  right rail with many cards; target is flatter, has lighter row treatment, and
  shows only compact source evidence.
- Tablet emergency portrait vs `artifacts/mocks/emergency-tablet-portrait.png`:
  visual fail against the emergency target. Capture routes to a generic
  no-match/detail treatment for `choking`, with source chrome and "try"
  suggestions. Target expects a distinct danger banner, immediate action rows,
  and proof card treatment.
- Tablet answer/detail landscape vs `artifacts/mocks/answer-tablet-landscape.png`:
  capture is a guide/source-context state, not the answer target. It preserves
  source context, but uses oversized long-form body copy and exposes reviewed
  boundary text; target expects a compact answer surface with thread/source
  columns, confidence state, related guides, and paper answer hierarchy.
- Tablet search portrait vs `artifacts/mocks/search-tablet-portrait.png`:
  capture remains card-heavy and content-dense. It shows `fire - 75 results`,
  badges/chips, bordered cards, and many visible rows; target is `rain shelter -
  4 results`, flatter rows, score markers, and less visual chrome.
- Tablet search landscape vs `artifacts/mocks/search-tablet-landscape.png`:
  capture has the expected three-column shape with preview, but still uses the
  same `fire - 75 results` card/badge treatment and wider result cards. Target
  is flatter, less chip-heavy, and uses compact scored rows for `rain shelter`.
- Phone home landscape vs `artifacts/mocks/home-phone-landscape.png`:
  capture is closer after phone-home polish but still too zoomed/spacious. It
  shows one recent thread, oversized search/category surfaces, and a large empty
  recent-thread panel; target shows denser pack status, tighter categories, and
  three recent thread rows.
- Phone answer portrait vs `artifacts/mocks/answer-phone-portrait.png`:
  visual fail against target hierarchy. Capture has oversized header controls,
  clipped title/header text, large source-preview chips, and a partially clipped
  answer body. Target expects a clean top answer header, full paper-style answer
  body, inline uncertain-fit warning, source cards, related guides, and an
  uncluttered composer.

## Notes

These captures are valid smoke evidence for post-`0f86cbc` because app content,
orientation, screenshots, UI dumps, and summaries agree. They are not visual
acceptance proof against `artifacts/mocks`; the target deltas above remain.

# Android Visual Review 20260427 Wave 3 - Worker S

Scope: screenshot smoke validation only after commit `5648d16`. No app code edited.

## Commands Run

- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -BuildOnly -ArtifactRoot artifacts/android_visual_wave3_worker_s`
- Initial method-scoped attempts on `emulator-5554`, `emulator-5558`, and `emulator-5556` with default `-SmokeProfile basic` failed with `OK (0 tests)` because the script appended the basic profile methods to the requested method name.
- Rerun commands used `-SmokeProfile custom`:
  - tablet search portrait: `-Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling'`
  - tablet thread portrait: `-Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedSeededFollowUpThreadShowsInlineHistory'`
  - tablet answer/detail landscape: `-Device emulator-5558 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#guideDetailDestinationKeepsSourceContextOnTabletLandscape'`
  - phone answer/detail portrait: `-Device emulator-5556 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#deterministicAskNavigatesToDetailScreen'`
  - phone home landscape: `-Device emulator-5560 -SkipBuild -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#homeEntryShowsPrimaryBrowseAndAskLanes'`
  - tablet emergency portrait: `-Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest' -ScriptedQuery 'choking' -ScriptedAsk -ScriptedExpectedSurface detail -ScriptedCaptureLabel 'tablet_emergency_portrait' -ScriptedTimeoutMs 20000`

## Pass / Fail

- Build: pass.
- Corrected smoke captures: pass, 6/6.
- Initial incorrectly parameterized method captures: fail/no screenshots, 4 runs. These are harness invocation failures, not app failures.

## Artifact Paths

- Tablet search portrait: `artifacts/android_visual_wave3_worker_s/tablet_search_portrait/20260427_215250_851/emulator-5554/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- Tablet thread portrait: `artifacts/android_visual_wave3_worker_s/tablet_thread_portrait/20260427_215307_758/emulator-5554/screenshots/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- Tablet answer/detail landscape: `artifacts/android_visual_wave3_worker_s/tablet_answer_landscape/20260427_215250_881/emulator-5558/screenshots/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
- Phone answer/detail portrait: `artifacts/android_visual_wave3_worker_s/phone_answer_emergency_portrait/20260427_215250_914/emulator-5556/screenshots/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`
- Phone home landscape: `artifacts/android_visual_wave3_worker_s/phone_home_landscape/20260427_215338_686/emulator-5560/screenshots/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- Tablet emergency portrait: `artifacts/android_visual_wave3_worker_s/tablet_emergency_portrait/20260427_215428_047/emulator-5554/screenshots/scriptedPromptFlowCompletes__tablet_emergency_portrait.png`

## Biggest Target Mismatches vs `artifacts/mocks`

- Tablet portrait thread/emergency are the largest mismatch: the answer body is constrained to a very narrow center column, producing word-per-line wrapping and extreme vertical overflow. Target mocks show a broad readable main column with sources in a right rail.
- Tablet search portrait diverges from the mock density and structure: captured results are large card blocks with badges/snippets and a prominent vertical split, while the mock uses flatter list rows, compact scoring, and more whitespace below the four results.
- Tablet emergency portrait does not match the emergency-card target: captured screen is a generic thread/detail layout with orange low-match text and source graph; mock expects a dedicated danger banner, immediate action rows, and "why this answer" evidence.
- Tablet answer/detail landscape shows oversized body typography and visible reviewed-card boundary text in the main content, unlike the mock answer surfaces that prioritize compact action/evidence structure.
- Phone home landscape is closer, but captured layout is more zoomed/spacious than the mock: category cards are much larger, fewer recent threads are visible, and the status/search treatment differs from the compact target.

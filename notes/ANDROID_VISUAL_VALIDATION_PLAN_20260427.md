# Android Visual Validation Plan 20260427

Role: Worker AD. Scope is post-`4800104` Android visual validation planning only.
No production code edits.

## Baseline And Inputs

- Current baseline: `4800104` (`polish android search guide and phone home`).
- Planning inputs:
  - `notes/ANDROID_UI_REDESIGN_PHASE_TRACKER_20260427.md`
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE3.md`
- Fixed devices:
  - Phone portrait: `emulator-5556`
  - Phone landscape: `emulator-5560`
  - Tablet portrait: `emulator-5554`
  - Tablet landscape: `emulator-5558`
- Target gallery: `artifacts/mocks`.

## Next Checkpoint

Run a focused post-wave smoke first, then the fixed four-posture state pack only
after the focused captures prove the largest Wave 3 risks are no worse. The
focused checkpoint should prioritize tablet detail-family states because Wave 3
showed the largest target gaps there: tablet portrait thread/emergency column
width, tablet emergency treatment, and tablet landscape answer/detail density.

Method-scoped smoke commands must use `-SmokeProfile custom`. Wave 3 proved that
method-scoped runs with default `basic` can produce `OK (0 tests)` because the
basic profile appends its own method list to the requested method.

## Exact Smoke Commands

Build once and install from the same APK pair:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -BuildOnly -ArtifactRoot artifacts/android_visual_post_4800104_worker_ad
```

Tablet portrait thread, highest priority regression from Wave 3:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedSeededFollowUpThreadShowsInlineHistory' -ArtifactRoot artifacts/android_visual_post_4800104_worker_ad/tablet_thread_portrait -CaptureLogcat -ClearLogcatBeforeRun
```

Tablet portrait emergency, dedicated emergency treatment check:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest' -ScriptedQuery 'choking' -ScriptedAsk -ScriptedExpectedSurface detail -ScriptedCaptureLabel 'tablet_emergency_portrait' -ScriptedTimeoutMs 20000 -ArtifactRoot artifacts/android_visual_post_4800104_worker_ad/tablet_emergency_portrait -CaptureLogcat
```

Tablet landscape answer/detail density and source-context check:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5558 -SkipBuild -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#guideDetailDestinationKeepsSourceContextOnTabletLandscape' -ArtifactRoot artifacts/android_visual_post_4800104_worker_ad/tablet_answer_landscape -CaptureLogcat -ClearLogcatBeforeRun
```

Tablet portrait search density regression:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling' -ArtifactRoot artifacts/android_visual_post_4800104_worker_ad/tablet_search_portrait -CaptureLogcat
```

Phone landscape home regression from the `4800104` phone-home polish:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -SkipBuild -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#homeEntryShowsPrimaryBrowseAndAskLanes' -ArtifactRoot artifacts/android_visual_post_4800104_worker_ad/phone_home_landscape -CaptureLogcat -ClearLogcatBeforeRun
```

Phone portrait answer/detail smoke, to keep deterministic detail intact:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#deterministicAskNavigatesToDetailScreen' -ArtifactRoot artifacts/android_visual_post_4800104_worker_ad/phone_answer_portrait -CaptureLogcat -ClearLogcatBeforeRun
```

If all focused smokes capture app content, run the fixed four-posture state pack:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts/ui_state_pack_post_4800104_worker_ad -SkipBuild -SkipInstall -MaxParallelDevices 4
```

If device pressure is high, run the same pack in tablet-first order:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts/ui_state_pack_post_4800104_worker_ad_tablet_first -SkipBuild -SkipInstall -MaxParallelDevices 2 -RoleFilter tablet_portrait,tablet_landscape
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts/ui_state_pack_post_4800104_worker_ad_phone_second -SkipBuild -SkipInstall -MaxParallelDevices 2 -RoleFilter phone_portrait,phone_landscape
```

## Screenshots To Compare First

Compare against `artifacts/mocks` in this order:

1. Tablet portrait thread:
   - capture: `scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
   - target: `thread-tablet-portrait.png`
2. Tablet portrait emergency:
   - capture: `scriptedPromptFlowCompletes__tablet_emergency_portrait.png`
   - target: `emergency-tablet-portrait.png`
3. Tablet landscape answer/detail:
   - capture: `guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
   - target: `answer-tablet-landscape.png`; also inspect against
     `guide-tablet-landscape.png` if the captured state is guide/source-context
     rather than answer mode.
4. Tablet portrait search:
   - capture: `searchQueryShowsResultsWithoutShellPolling__search_results.png`
   - target: `search-tablet-portrait.png`
5. Phone landscape home:
   - capture: `homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
   - target: `home-phone-landscape.png`
6. Phone portrait answer/detail:
   - capture: `deterministicAskNavigatesToDetailScreen__deterministic_detail.png`
   - target: `answer-phone-portrait.png`

After these focused checks, compare the state-pack `screenshots/` role folders
against every target-backed state in the fixed matrix. Emergency remains
portrait-only unless a tracked landscape target is added.

## Known Failure Modes To Watch

- `OK (0 tests)` on method-scoped commands means the smoke was invoked with a
  profile other than `custom`; discard the artifact as invocation failure.
- Tablet portrait thread/emergency can collapse the main answer body into a very
  narrow center column, producing word-per-line wrapping and vertical overflow.
- Tablet emergency can fall back to generic detail/thread treatment with
  low-match/source-graph chrome instead of the target danger banner and immediate
  action rows.
- Tablet landscape detail can show oversized body type or reviewed-card boundary
  text that competes with compact action/evidence structure.
- Tablet search portrait can drift back toward large cards, prominent split
  chrome, and badge-heavy density instead of flatter compact rows.
- Phone landscape home can be too zoomed/spacious, with oversized category cards
  and too few recent threads visible.
- Any screenshot showing launcher, stale frame, wrong orientation, keyboard/IME
  occlusion, or missing app content is not visual acceptance proof even if the
  wrapper exits successfully.
- Do not treat full state-pack output as closure until `summary.json`, copied
  screenshots, and UI dumps agree on role, device, orientation, and captured app
  content.

# Android UI Redesign Phase Tracker - 2026-04-27

Purpose: overnight delegation map for the remaining Android mock-parity work
after commit `f2f6395`. This tracker is planning-only. Do not edit production
code from this lane; production changes belong to the implementation slice
owners below.

## Current Committed Baseline

- Current pushed baseline: `f2f6395` (`HEAD -> master`, `origin/master`).
- Commit title: `advance android mock parity polish`.
- Relevant stack: `5648d16` -> `4800104` -> `0f86cbc` -> `f2f6395`.
- `f2f6395` touched detail emergency/action rendering, tablet detail/thread
  composition, tablet evidence policy, search row presentation, and phone
  landscape home/search layout.
- Current target gallery: `artifacts/mocks`.
- Target note: `notes/ANDROID_TARGET_MOCKS_20260427.md`.
- Latest visual review note: `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE4.md`.
- Mock filenames currently present:
  - `answer-phone-landscape.png`, `answer-phone-portrait.png`,
    `answer-tablet-landscape.png`, `answer-tablet-portrait.png`
  - `emergency-phone-portrait.png`, `emergency-tablet-portrait.png`
  - `guide-phone-landscape.png`, `guide-phone-portrait.png`,
    `guide-tablet-landscape.png`, `guide-tablet-portrait.png`
  - `home-phone-landscape.png`, `home-phone-portrait.png`,
    `home-tablet-landscape.png`, `home-tablet-portrait.png`
  - `search-phone-landscape.png`, `search-phone-portrait.png`,
    `search-tablet-landscape.png`, `search-tablet-portrait.png`
  - `thread-phone-landscape.png`, `thread-phone-portrait.png`,
    `thread-tablet-landscape.png`, `thread-tablet-portrait.png`

## Fixed Validation Matrix

- Phone portrait: `emulator-5556`
- Phone landscape: `emulator-5560`
- Tablet portrait: `emulator-5554`
- Tablet landscape: `emulator-5558`

Emergency target coverage is portrait-only:

- `emergency-phone-portrait.png`
- `emergency-tablet-portrait.png`

Do not add emergency landscape acceptance requirements unless a new tracked
target mock is added.

## Current Worktree Guard

During this tracker refresh, Android production WIP appeared in the worktree.
Treat it as another worker's work. Do not delete, stash, or revert it from this
planner lane.

Observed WIP assignment:

- W5-S1 shell/search/home:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
  - `android-app/app/src/main/res/drawable/bg_manual_home_recent_row.xml`
  - `android-app/app/src/main/res/drawable/ic_search_magnifier.xml`
  - `android-app/app/src/main/res/layout-sw600dp/list_item_result.xml`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- W5-S2 detail-host/emergency:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- W5-S4 tablet detail/thread:
  - `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
  - `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
  - `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
  - `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
  - `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
  - `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`

There were also unrelated untracked planner handoff notes. Ignore them unless
their owner asks for help.

Before any worker starts:

```powershell
git status --short
git log --oneline -n 5 --decorate
```

If tracked production files are dirty, assign them to the matching slice owner
or park them before coding.

## Shared Build And Smoke Commands

Baseline JVM/build commands:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
```

Focused local quality gate:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
```

Full fixed-matrix state pack when all four emulators are already running:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts/android_visual_wave5_state_pack_post_f2f6395 -MaxParallelDevices 4
```

Headless fixed-matrix state pack when the lane should launch the emulators:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_headless_state_pack_lane.ps1 -OutputRoot artifacts/android_visual_wave5_headless_post_f2f6395 -LaunchProfile clean-headless -RealRun
```

Non-acceptance preflight for a planned state pack:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts/android_visual_wave5_state_pack_post_f2f6395 -PlanOnly
```

## Remaining Target Deltas After `f2f6395`

Treat these as the Wave 5 backlog until a newer visual review supersedes them:

- Search tablet portrait/landscape: still too card-heavy and should target
  the `rain shelter - 4 results` compact row treatment.
- Phone home landscape: still too spacious; target shows denser pack status,
  tighter categories, and three recent-thread rows.
- Phone answer portrait: header controls/title can clip and source chips/body
  still crowd the answer target.
- Tablet answer/detail landscape: must prove the actual answer target, not a
  guide/source-context substitute.
- Tablet thread portrait: structurally present but still too dense/heavy; target
  wants flatter thread rows and compact evidence.
- Tablet emergency portrait: must render the distinct danger surface for a true
  emergency prompt instead of generic no-match/detail treatment.
- Guide reader/cross-reference states: need a final pass for readable paper body
  and compact source/link rail across phone and tablet postures.

## Implementation Slices

Each slice below has a disjoint reserved write set. If a worker needs a file
outside its set, pause and ask the planner to resequence.

### W5-S1 Shell, Home, And Search

- Can run concurrently with: W5-S2, W5-S3, W5-S4, W5-S5.
- Reserved owner: shell/search/home worker.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
  - `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
  - `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
  - `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
  - `android-app/app/src/main/res/layout/activity_main.xml`
  - `android-app/app/src/main/res/layout-land/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp/list_item_result.xml`
  - `android-app/app/src/main/res/values/ids.xml`
  - `android-app/app/src/main/res/drawable/bg_manual_phone_home_root.xml`
  - `android-app/app/src/main/res/drawable/bg_manual_phone_home_search.xml`
  - `android-app/app/src/main/res/drawable/bg_manual_phone_home_search_icon.xml`
  - `android-app/app/src/main/res/drawable/bg_manual_home_recent_row.xml`
  - `android-app/app/src/main/res/drawable/bg_search_result_row_dark.xml`
  - `android-app/app/src/main/res/drawable/bg_search_score_tick.xml`
  - `android-app/app/src/main/res/drawable/ic_search_magnifier.xml`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityIdentityTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/MainPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
  - `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`
- Target screenshots:
  - `home-phone-portrait.png`, `home-phone-landscape.png`
  - `home-tablet-portrait.png`, `home-tablet-landscape.png`
  - `search-phone-portrait.png`, `search-phone-landscape.png`
  - `search-tablet-portrait.png`, `search-tablet-landscape.png`
- Focus:
  - Match compact `rain shelter` search rows, score ticks, selected state, and
    tablet landscape preview behavior.
  - Tighten phone landscape home density without changing product IA or tab
    semantics.
  - If filters remain display-only, state that in the handoff.
- Unit validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.MainActivityHomeChromeTest" --tests "com.senku.mobile.MainActivityIdentityTest" --tests "com.senku.mobile.MainActivityPhoneNavigationTest" --tests "com.senku.mobile.MainPresentationFormatterTest" --tests "com.senku.mobile.SearchResultAdapterTest" --tests "com.senku.ui.search.SearchResultCardHeuristicsTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
```

- Focused smoke commands:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'rain shelter' -ScriptedExpectedSurface results -ScriptedCaptureLabel 'search_rain_shelter_phone_portrait' -ArtifactRoot artifacts/android_visual_wave5_shell -CaptureLogcat -ClearLogcatBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'rain shelter' -ScriptedExpectedSurface results -ScriptedCaptureLabel 'search_rain_shelter_phone_landscape' -ArtifactRoot artifacts/android_visual_wave5_shell -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'rain shelter' -ScriptedExpectedSurface results -ScriptedCaptureLabel 'search_rain_shelter_tablet_portrait' -ArtifactRoot artifacts/android_visual_wave5_shell -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5558 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'rain shelter' -ScriptedExpectedSurface results -ScriptedCaptureLabel 'search_rain_shelter_tablet_landscape' -ArtifactRoot artifacts/android_visual_wave5_shell -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#homeEntryShowsPrimaryBrowseAndAskLanes' -ArtifactRoot artifacts/android_visual_wave5_shell -CaptureLogcat
```

- Commit criteria:
  - One shell/search/home commit.
  - No duplicate `results_list`, `results_header`, or `search_input` IDs in an
    inflated layout.

### W5-S2 Detail Host And Emergency Surface

- Can run concurrently with: W5-S1, W5-S3, W5-S4, W5-S5.
- Reserved owner: detail-host/emergency worker.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
  - `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
  - `android-app/app/src/main/res/layout/activity_detail.xml`
  - `android-app/app/src/main/res/layout-land/activity_detail.xml`
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - `android-app/app/src/main/res/layout-large/activity_detail.xml`
  - `android-app/app/src/main/res/layout-large-land/activity_detail.xml`
  - `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
  - `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
  - `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailSurfaceContractTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`
- Target screenshots:
  - `emergency-phone-portrait.png`, `emergency-tablet-portrait.png`
  - Regression: `answer-phone-portrait.png`, `answer-phone-landscape.png`
  - Regression: `guide-phone-portrait.png`, `thread-phone-portrait.png`
- Focus:
  - True emergency prompts must show one clear danger lane, ordered immediate
    action rows, and one proof/evidence card.
  - Negative controls must not receive emergency treatment.
  - Fix host-level clipping and overly large phone answer header controls
    without changing answer-card body internals.
- Unit validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailActionBlockPresentationFormatterTest" --tests "com.senku.mobile.DetailSurfaceContractTest" --tests "com.senku.mobile.DetailWarningCopySanitizerTest" --tests "com.senku.mobile.EmergencySurfacePolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
```

- Focused smoke commands:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'baby is choking and cannot cry or cough' -ScriptedAsk -ScriptedExpectedSurface detail -ScriptedCaptureLabel 'emergency_choking_phone_portrait' -ScriptedTimeoutMs 20000 -ArtifactRoot artifacts/android_visual_wave5_emergency -CaptureLogcat -ClearLogcatBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'baby is choking and cannot cry or cough' -ScriptedAsk -ScriptedExpectedSurface detail -ScriptedCaptureLabel 'emergency_choking_tablet_portrait' -ScriptedTimeoutMs 20000 -ArtifactRoot artifacts/android_visual_wave5_emergency -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'How do I reduce choking hazards in a toddler play area?' -ScriptedAsk -ScriptedExpectedSurface detail -ScriptedCaptureLabel 'negative_choking_prevention_phone_portrait' -ScriptedTimeoutMs 20000 -ArtifactRoot artifacts/android_visual_wave5_emergency -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedPromptFlowCompletes' -ScriptedQuery 'My throat feels scratchy after dust, but I can breathe and swallow. What should I monitor?' -ScriptedAsk -ScriptedExpectedSurface detail -ScriptedCaptureLabel 'negative_breathing_ok_tablet_portrait' -ScriptedTimeoutMs 20000 -ArtifactRoot artifacts/android_visual_wave5_emergency -CaptureLogcat
```

- Commit criteria:
  - One detail-host/emergency commit.
  - Handoff names the true emergency prompt and both negative controls.
  - No emergency landscape target is invented.

### W5-S3 Paper Answer Body

- Can run concurrently with: W5-S1, W5-S2, W5-S4, W5-S5.
- Reserved owner: paper-answer worker.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
  - `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
  - `android-app/app/src/main/res/drawable/bg_detail_answer_shell.xml`
  - `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
  - `android-app/app/src/test/java/com/senku/mobile/DetailAnswerBodyFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailMetaPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailProofPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailProvenancePresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/ui/answer/AnswerContentFactoryTest.kt`
  - `android-app/app/src/test/java/com/senku/ui/answer/PaperAnswerCardLabelTest.kt`
- Target screenshots:
  - `answer-phone-portrait.png`, `answer-phone-landscape.png`
  - `answer-tablet-portrait.png`, `answer-tablet-landscape.png`
- Focus:
  - Reduce parchment/card dominance while preserving hierarchy.
  - Keep uncertain-fit, abstain, deterministic, and reviewed-evidence labels
    distinct.
  - Make source preview/footer copy compact enough to stop crowding answer body.
- Unit validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailAnswerBodyFormatterTest" --tests "com.senku.mobile.DetailMetaPresentationFormatterTest" --tests "com.senku.mobile.DetailProofPresentationFormatterTest" --tests "com.senku.mobile.DetailProvenancePresentationFormatterTest" --tests "com.senku.ui.answer.AnswerContentFactoryTest" --tests "com.senku.ui.answer.PaperAnswerCardLabelTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
```

- Focused smoke commands:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#deterministicAskNavigatesToDetailScreen' -ArtifactRoot artifacts/android_visual_wave5_answer -CaptureLogcat -ClearLogcatBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#answerModeLandscapePhoneShowsCompactCrossReferenceCue' -ArtifactRoot artifacts/android_visual_wave5_answer -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane' -ArtifactRoot artifacts/android_visual_wave5_answer -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5558 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane' -ArtifactRoot artifacts/android_visual_wave5_answer -CaptureLogcat
```

- Commit criteria:
  - One answer-body commit.
  - Handoff includes before/after answer screenshots for all four postures.

### W5-S4 Tablet Detail, Evidence, Thread, And Composer

- Can run concurrently with: W5-S1, W5-S2, W5-S3, W5-S5.
- Reserved owner: tablet-detail/thread worker.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/DetailSessionPresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailTranscriptFormatter.java`
  - `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
  - `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
  - `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
  - `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
  - `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailSessionPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailTranscriptFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
  - `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`
- Target screenshots:
  - `answer-tablet-portrait.png`, `answer-tablet-landscape.png`
  - `thread-phone-portrait.png`, `thread-phone-landscape.png`
  - `thread-tablet-portrait.png`, `thread-tablet-landscape.png`
  - `guide-tablet-portrait.png`, `guide-tablet-landscape.png`
- Focus:
  - Tablet answer landscape must be a true answer-state proof, not a guide
    substitution.
  - Flatten and lighten thread rows and evidence rail density.
  - Keep composer placement stable across phone landscape, tablet portrait, and
    tablet landscape.
- Unit validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --tests "com.senku.mobile.DetailSessionPresentationFormatterTest" --tests "com.senku.mobile.DetailTranscriptFormatterTest" --tests "com.senku.ui.tablet.StressReadingPolicyTest" --tests "com.senku.ui.tablet.TabletEvidenceVisibilityPolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
```

- Focused smoke commands:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedSeededFollowUpThreadShowsInlineHistory' -ArtifactRoot artifacts/android_visual_wave5_thread -CaptureLogcat -ClearLogcatBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedSeededFollowUpThreadShowsInlineHistory' -ArtifactRoot artifacts/android_visual_wave5_thread -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedSeededFollowUpThreadShowsInlineHistory' -ArtifactRoot artifacts/android_visual_wave5_thread -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5558 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#scriptedSeededFollowUpThreadShowsInlineHistory' -ArtifactRoot artifacts/android_visual_wave5_thread -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_detail_followup_matrix.ps1
```

- Commit criteria:
  - One tablet-detail/thread commit.
  - Handoff calls out composer focus/IME behavior.

### W5-S5 Guide Reader Copy And Source Styling

- Can run concurrently with: W5-S1, W5-S2, W5-S3, W5-S4.
- Reserved owner: guide/source worker.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/DetailCitationPresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailSourcePresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
  - `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
  - `android-app/app/src/main/res/drawable/bg_detail_source_card_flat.xml`
  - `android-app/app/src/main/res/drawable/bg_source_link.xml`
  - `android-app/app/src/main/res/drawable/bg_source_link_active.xml`
  - `android-app/app/src/main/res/drawable/bg_sources_section_pill.xml`
  - `android-app/app/src/main/res/drawable/bg_sources_stack_shell.xml`
  - `android-app/app/src/test/java/com/senku/mobile/CopySanitizerTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailCitationPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailSourcePresentationFormatterTest.java`
- Target screenshots:
  - `guide-phone-portrait.png`, `guide-phone-landscape.png`
  - `guide-tablet-portrait.png`, `guide-tablet-landscape.png`
  - Regression: `answer-tablet-landscape.png`, `thread-tablet-portrait.png`
- Focus:
  - Reader body should look deliberate and scannable, not generic card chrome.
  - Source/cross-reference links should be compact and stateful.
  - Keep guide semantics separate from answer semantics.
- Unit validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.CopySanitizerTest" --tests "com.senku.mobile.DetailCitationPresentationFormatterTest" --tests "com.senku.mobile.DetailGuideContextPresentationFormatterTest" --tests "com.senku.mobile.DetailGuidePresentationFormatterTest" --tests "com.senku.mobile.DetailRelatedGuidePresentationFormatterTest" --tests "com.senku.mobile.DetailSourcePresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
```

- Focused smoke commands:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5556 -SkipBuild -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#guideDetailUsesCrossReferenceCopyOffRail' -ArtifactRoot artifacts/android_visual_wave5_guide -CaptureLogcat -ClearLogcatBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#guideDetailUsesCrossReferenceCopyOffRail' -ArtifactRoot artifacts/android_visual_wave5_guide -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5554 -SkipBuild -SkipInstall -Orientation portrait -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#guideDetailUsesCrossReferenceCopyOffRail' -ArtifactRoot artifacts/android_visual_wave5_guide -CaptureLogcat
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Device emulator-5558 -SkipBuild -SkipInstall -Orientation landscape -SmokeProfile custom -TestClass 'com.senku.mobile.PromptHarnessSmokeTest#guideDetailDestinationKeepsSourceContextOnTabletLandscape' -ArtifactRoot artifacts/android_visual_wave5_guide -CaptureLogcat
```

- Commit criteria:
  - One guide/source commit.
  - Handoff names any intentional source-rail deviation from mocks.

## Integration Order

1. Run the worktree guard commands and confirm no tracked production WIP is
   unassigned.
2. If emulator capacity allows, run a fresh post-`f2f6395` state pack before
   coding and record the artifact root in the active handoff.
3. Start W5-S1 through W5-S5 in parallel only if their reserved write sets stay
   disjoint.
4. Merge slice commits in this order when green: W5-S3, W5-S5, W5-S1, W5-S4,
   W5-S2. Keep W5-S2 late because it owns `DetailActivity.java`.
5. After all slice commits merge, run the focused local quality gate.
6. Run the fixed four-posture state pack and compare screenshots against
   `artifacts/mocks`, not older captures.
7. Produce a morning review note only after the final state pack exists.

## Final Closure Commands

Run these with no concurrent emulator-heavy work:

```powershell
git status --short
git diff --check
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts/android_visual_wave5_state_pack_post_f2f6395 -MaxParallelDevices 4
```

If using the headless launcher instead:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_headless_state_pack_lane.ps1 -OutputRoot artifacts/android_visual_wave5_headless_post_f2f6395 -LaunchProfile clean-headless -RealRun
```

## Commit And Checkpoint Rules

- One slice per commit. Do not combine shell/search, guide, answer, thread, and
  emergency in the same commit.
- Each handoff must include exact files changed, exact commands run, screenshot
  artifact root, target screenshots reviewed, and residual mismatch.
- Unit tests alone do not close a visual slice.
- Do not update files under `artifacts/mocks` as part of implementation.
- Do not edit target notes to match implementation output unless the design
  target actually changes and a planner approves it.
- Do not broaden emergency eligibility as a visual fix.
- Do not expose reviewed-card runtime outside developer/test controls as part
  of mock parity.
- If a worker touches a chokepoint outside its reserved write set, pause and
  ask for orchestration before committing.

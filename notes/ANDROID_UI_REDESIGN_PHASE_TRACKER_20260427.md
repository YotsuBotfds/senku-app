# Android UI Redesign Phase Tracker - 2026-04-27

Purpose: overnight delegation map from the current committed Android redesign
baseline through final parity with the tracked target mocks in `artifacts/mocks`.
This tracker is planning-only; production code should be changed only by the
slice owners below.

## Current Committed Baseline

- Pushed baseline: `5648d16a9358d9303c33a75b9b948ce96f8c48d6`
  (`5648d16`, `HEAD -> master`, `origin/master`).
- Commit title: `advance android tablet and emergency redesign`.
- Baseline landed after `edae11a` and `de0c071`; do not reopen already-landed
  broad home/search/detail redesign unless a target screenshot mismatch requires
  it.
- `5648d16` added tablet search shell structure, tablet detail/evidence/thread
  baselines, docked composer adjustments, and emergency portrait/action-block
  hooks.
- Target gallery: `artifacts/mocks`.
- Target note: `notes/ANDROID_TARGET_MOCKS_20260427.md`.
- Current planning notes:
  - `notes/ANDROID_UI_REDESIGN_PROGRESS_20260427.md`
  - `notes/ANDROID_NEXT_UI_SLICES_20260427.md`
  - `notes/ANDROID_SEARCH_TABLET_SHELL_PLAN_20260427.md`

Authoring warning: the worktree had uncommitted Android production changes when
this tracker was written. Treat the committed baseline as `5648d16`; before
assigning any worker that touches the files below, either commit/stash/reconcile
the existing WIP or assign that WIP to the same owner:

- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_source_card_flat.xml`
- `android-app/app/src/main/res/drawable/bg_manual_phone_home_root.xml`
- `android-app/app/src/main/res/drawable/bg_manual_phone_home_search.xml`
- `android-app/app/src/main/res/drawable/bg_manual_phone_home_search_icon.xml`
- `android-app/app/src/main/res/drawable/bg_search_result_row_dark.xml`
- `android-app/app/src/main/res/drawable/bg_search_score_tick.xml`
- `android-app/app/src/main/res/drawable/bg_source_link.xml`
- `android-app/app/src/main/res/drawable/bg_source_link_active.xml`
- `android-app/app/src/main/res/drawable/bg_sources_section_pill.xml`
- `android-app/app/src/main/res/drawable/bg_sources_stack_shell.xml`
- `android-app/app/src/main/res/drawable/ic_home_back_chevron.xml`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

## Fixed Validation Matrix

- Phone portrait: `emulator-5556`
- Phone landscape: `emulator-5560`
- Tablet portrait: `emulator-5554`
- Tablet landscape: `emulator-5558`

Target screenshots by state:

- Home: `home-phone-portrait.png`, `home-phone-landscape.png`,
  `home-tablet-portrait.png`, `home-tablet-landscape.png`
- Search: `search-phone-portrait.png`, `search-phone-landscape.png`,
  `search-tablet-portrait.png`, `search-tablet-landscape.png`
- Answer: `answer-phone-portrait.png`, `answer-phone-landscape.png`,
  `answer-tablet-portrait.png`, `answer-tablet-landscape.png`
- Guide reader: `guide-phone-portrait.png`, `guide-phone-landscape.png`,
  `guide-tablet-portrait.png`, `guide-tablet-landscape.png`
- Follow-up thread: `thread-phone-portrait.png`, `thread-phone-landscape.png`,
  `thread-tablet-portrait.png`, `thread-tablet-landscape.png`
- Emergency: `emergency-phone-portrait.png`,
  `emergency-tablet-portrait.png`; no emergency landscape target exists.

Baseline commands every implementation worker should know:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Final visual pack command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -RealRun
```

## Integration Chokepoints

Only one active worker should own each chokepoint at a time:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Owns detail-mode routing, emergency header/action rendering, guide/detail
    binding, follow-up composer hooks, and many screenshot states.
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - Owns home/search shell switching, tablet search shell, result selection,
    nav rail/bottom tab visibility, and prompt entry.
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
  - Owns tablet answer/thread/guide station composition.
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml` and
  `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
  - Shared by tablet home and tablet search.
- `android-app/app/src/main/res/layout/activity_detail.xml`,
  `android-app/app/src/main/res/layout-land/activity_detail.xml`,
  `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`,
  `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`,
  `android-app/app/src/main/res/layout-large/activity_detail.xml`, and
  `android-app/app/src/main/res/layout-large-land/activity_detail.xml`
  - Shared by phone detail, guide, emergency, and legacy host views.
- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
  - Shared by answer, tablet detail, and any paper-like answer surface.
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
  - Shared by answer, thread, and emergency follow-up.
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - Shared screenshot/capture truth. Do not mix harness changes with visual
    production changes unless the slice explicitly owns both.

If a slice needs a chokepoint outside its reserved write set, stop and ask the
orchestrator to resequence the work.

## Phase 0 - Baseline Proof And Worktree Freeze

Goal: make the overnight start state reviewable and prevent accidental merges
over existing uncommitted work.

### P0-A Baseline Delta Capture

- Can run concurrently: no; run before implementation.
- Reserved write set:
  - `notes/ANDROID_UI_REDESIGN_PHASE_TRACKER_20260427.md`
  - new artifact directories under `artifacts/`
- Production write set: none.
- Validate screenshots:
  - Run the final visual pack command above if emulator capacity allows.
  - At minimum capture answer, guide, search, thread, and emergency portrait
    states for the fixed matrix where targets exist.
- Commands:

```powershell
git status --short
git log --oneline -n 5 --decorate
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -RealRun
```

- Checkpoint criteria:
  - Artifact root recorded in morning handoff.
  - Any dirty production files are assigned to an owner or parked before coding.
  - Emergency validation is portrait-only unless a new target is supplied.

## Phase 1 - Remaining Core Surface Parity

Goal: close the biggest visible mismatches from `5648d16` without mixing
screen families. These slices are designed for parallel implementation if the
dirty worktree warning is resolved first.

### P1-A Answer Card De-Parchment

- Can run concurrently: yes, with P1-B/P1-C/P1-D/P1-E.
- Do not touch: `DetailActivity.java`, `TabletDetailScreen.kt`,
  `DockedComposer.kt`.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
  - `android-app/app/src/main/res/drawable/bg_detail_answer_shell.xml`
  - `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
  - `android-app/app/src/test/java/com/senku/ui/answer/PaperAnswerCardLabelTest.kt`
  - `android-app/app/src/test/java/com/senku/mobile/DetailAnswerBodyFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailSurfaceContractTest.java`
- Target screenshots:
  - `answer-phone-portrait.png`
  - `answer-phone-landscape.png`
  - `answer-tablet-portrait.png`
  - `answer-tablet-landscape.png`
- Focus:
  - Flatten card borders/dividers and reduce parchment dominance.
  - Preserve answer hierarchy, warnings, source/proof labels, abstain and
    uncertain-fit distinctions.
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.ui.answer.PaperAnswerCardLabelTest" --tests "com.senku.mobile.DetailAnswerBodyFormatterTest" --tests "com.senku.mobile.DetailSurfaceContractTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

- Screenshot proof:
  - Capture answer detail on all four fixed devices.
- Reasoning level: high.
- Commit criteria:
  - One answer-only commit.
  - Before/after answer screenshots linked in commit or handoff note.

### P1-B Guide Reader And Cross-Reference Station

- Can run concurrently: yes, unless another worker owns `EvidencePane.kt`.
- Do not touch: `DetailActivity.java`, `TabletDetailScreen.kt`,
  `DockedComposer.kt`.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
  - `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
  - `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
  - `android-app/app/src/main/res/drawable/bg_detail_source_card_flat.xml`
  - `android-app/app/src/main/res/drawable/bg_source_link.xml`
  - `android-app/app/src/main/res/drawable/bg_source_link_active.xml`
  - `android-app/app/src/main/res/drawable/bg_sources_section_pill.xml`
  - `android-app/app/src/main/res/drawable/bg_sources_stack_shell.xml`
  - `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`
- Target screenshots:
  - `guide-phone-portrait.png`
  - `guide-phone-landscape.png`
  - `guide-tablet-portrait.png`
  - `guide-tablet-landscape.png`
- Focus:
  - Paper reader body should look intentional and readable, not generic cardy.
  - Cross-reference rail should read as a compact rail with source/link states.
  - Do not merge guide copy semantics with answer/detail answer semantics.
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.DetailGuidePresentationFormatterTest" --tests "com.senku.mobile.DetailGuideContextPresentationFormatterTest" --tests "com.senku.mobile.DetailRelatedGuidePresentationFormatterTest" --tests "com.senku.ui.tablet.TabletEvidenceVisibilityPolicyTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

- Screenshot proof:
  - Capture guide reader on all four fixed devices.
- Reasoning level: high.
- Commit criteria:
  - One guide/cross-ref commit.
  - Handoff notes any remaining need for a first-class Compose guide station.

### P1-C Tablet Search Interactive Polish

- Can run concurrently: yes, if no other worker touches `MainActivity.java` or
  tablet `activity_main.xml`.
- Do not touch: detail files.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
  - `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
  - `android-app/app/src/main/res/values/ids.xml`
  - `android-app/app/src/main/res/drawable/bg_search_result_row_dark.xml`
  - `android-app/app/src/main/res/drawable/bg_search_score_tick.xml`
  - `android-app/app/src/test/java/com/senku/mobile/MainPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
  - `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`
- Target screenshots:
  - `search-tablet-portrait.png`
  - `search-tablet-landscape.png`
  - Regression: `search-phone-portrait.png`,
    `search-phone-landscape.png`
- Focus:
  - Keep the landed tablet search shell.
  - Add/finish coherent filter rail, selected row state, and landscape preview
    rail behavior.
  - Decide explicitly whether filters are display-only for this phase. Default:
    display-only counts; no retrieval re-query.
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.MainPresentationFormatterTest" --tests "com.senku.mobile.SearchResultAdapterTest" --tests "com.senku.ui.search.SearchResultCardHeuristicsTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

- Screenshot proof:
  - Tablet portrait and landscape search for `rain shelter`.
  - Phone portrait and landscape regression search for `rain shelter`.
- Reasoning level: high.
- Commit criteria:
  - One tablet-search commit.
  - No duplicate `results_list`, `results_header`, or `search_input` IDs in an
    inflated layout.

### P1-D Follow-Up Thread Parity

- Can run concurrently: yes, if no other worker touches
  `TabletDetailScreen.kt` or `DockedComposer.kt`.
- Do not touch: `DetailActivity.java`, `MainActivity.java`.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
  - `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
  - `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
  - `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailTranscriptFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailSessionPresentationFormatter.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailTranscriptFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailSessionPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
  - `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
- Target screenshots:
  - `thread-phone-portrait.png`
  - `thread-phone-landscape.png`
  - `thread-tablet-portrait.png`
  - `thread-tablet-landscape.png`
- Focus:
  - Transcript density, turn/source rails, selected turn state, and composer
    placement.
  - Keep landscape-phone composer from occluding captured detail content.
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.DetailTranscriptFormatterTest" --tests "com.senku.mobile.DetailSessionPresentationFormatterTest" --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --tests "com.senku.ui.tablet.StressReadingPolicyTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File ..\scripts\run_android_detail_followup_matrix.ps1
```

- Screenshot proof:
  - Follow-up flow on all four fixed devices after at least one sent follow-up.
- Reasoning level: high.
- Commit criteria:
  - One thread-only commit.
  - Composer focus/IME behavior called out in handoff.

### P1-E Emergency Portrait Parity And Negative Controls

- Can run concurrently: yes, but this slice exclusively owns
  `DetailActivity.java`.
- Do not touch: search/home files, tablet search XML, `PaperAnswerCard.kt`.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
  - `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
  - `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
  - `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
  - `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`
- Target screenshots:
  - `emergency-phone-portrait.png`
  - `emergency-tablet-portrait.png`
- Focus:
  - One danger lane, ordered immediate action rows, one clear source/evidence
    card, quiet composer.
  - Do not broaden emergency eligibility.
  - Do not create emergency landscape expectations.
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.EmergencySurfacePolicyTest" --tests "com.senku.mobile.DetailActionBlockPresentationFormatterTest" --tests "com.senku.mobile.DetailWarningCopySanitizerTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

- Screenshot proof:
  - True emergency on `emulator-5556` and `emulator-5554`.
  - Two negative controls that must not show emergency treatment.
- Reasoning level: xhigh.
- Commit criteria:
  - One emergency-only commit.
  - Handoff states the exact emergency prompt and two negative controls used.

## Phase 2 - Cross-Posture Polish

Goal: after Phase 1 merges, resolve posture-specific drift and home/nav polish
without reopening the surface contracts.

### P2-A Phone Landscape Density Pass

- Can run concurrently: no, if any active worker still owns `DetailActivity.java`
  or phone landscape layouts.
- Reserved write set:
  - `android-app/app/src/main/res/layout-land/activity_main.xml`
  - `android-app/app/src/main/res/layout-land/list_item_result.xml`
  - `android-app/app/src/main/res/layout-land/activity_detail.xml`
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- Target screenshots:
  - `home-phone-landscape.png`
  - `search-phone-landscape.png`
  - `answer-phone-landscape.png`
  - `guide-phone-landscape.png`
  - `thread-phone-landscape.png`
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.MainActivityPhoneNavigationTest" --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

- Screenshot proof:
  - Phone landscape captures for every non-emergency target state.
- Reasoning level: high.
- Commit criteria:
  - One landscape-density commit.
  - No product/routing behavior changes.

### P2-B Tablet Home And Nav Rail Alignment

- Can run concurrently: yes, only after P1-C merges or parks tablet search XML.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
  - `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityIdentityTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`
- Target screenshots:
  - `home-tablet-portrait.png`
  - `home-tablet-landscape.png`
  - Regression: `home-phone-portrait.png`, `home-phone-landscape.png`
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.MainActivityHomeChromeTest" --tests "com.senku.mobile.MainActivityIdentityTest" --tests "com.senku.mobile.MainActivityPhoneNavigationTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

- Screenshot proof:
  - Home on all four fixed devices.
- Reasoning level: medium-high.
- Commit criteria:
  - One home/nav commit.
  - Does not reopen tab count, lane naming, or product IA.

### P2-C Tablet Detail Integration Pass

- Can run concurrently: no; this is a merge-cleanup slice across detail
  chokepoints after P1-A/P1-B/P1-D/P1-E.
- Reserved write set:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
  - `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`
- Target screenshots:
  - `answer-tablet-portrait.png`
  - `answer-tablet-landscape.png`
  - `guide-tablet-portrait.png`
  - `guide-tablet-landscape.png`
  - `thread-tablet-portrait.png`
  - `thread-tablet-landscape.png`
  - `emergency-tablet-portrait.png`
- Validation:

```powershell
cd android-app
.\gradlew.bat :app:testDebugUnitTest --tests "com.senku.ui.tablet.StressReadingPolicyTest" --tests "com.senku.ui.tablet.TabletEvidenceVisibilityPolicyTest" --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

- Screenshot proof:
  - Tablet portrait and landscape detail-family captures.
- Reasoning level: xhigh.
- Commit criteria:
  - One integration commit only after individual Phase 1 commits are green.
  - Handoff lists any intentional deviations from target mocks.

## Phase 3 - Full Matrix Closure

Goal: turn individual green slices into a reviewable final redesign proof.

### P3-A Full Local Quality Gate

- Can run concurrently: no.
- Reserved write set: none unless tests fail; if tests fail, route back to the
  owning slice.
- Commands:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
```

- Checkpoint criteria:
  - JVM tests pass.
  - Debug APK and androidTest APK assemble.
  - Any known local-quality exception is documented with owner and expiry.

### P3-B Fixed Four-Posture State Pack

- Can run concurrently: no; do not run competing ADB-heavy tasks.
- Reserved write set:
  - new artifact directories under `artifacts/`
  - optional summary note under `notes/`
- Command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -RealRun
```

- Screenshot proof:
  - All target-backed states across the fixed matrix.
  - Emergency portrait only.
- Checkpoint criteria:
  - Summary artifact linked in morning review.
  - Screenshots are compared against `artifacts/mocks`, not older captures.
  - Dumps/screenshots show app content, not launcher or stale frames.

### P3-C Morning Review Packet

- Can run concurrently: yes, after P3-B artifacts exist.
- Reserved write set:
  - `notes/ANDROID_UI_REDESIGN_PROGRESS_20260427.md`
  - optional new morning review note under `notes/`
- Contents:
  - Baseline commit and final commit range.
  - Per-state visual verdict: pass, acceptable delta, or follow-up.
  - Artifact roots for four-posture pack and any focused smokes.
  - Remaining risks and exact owners.
- Checkpoint criteria:
  - No unassigned production dirty files.
  - Every implementation slice has a commit, validation result, and screenshot
    artifact reference.

## Overnight Delegation Order

Recommended order if multiple workers are available:

1. Run P0-A, freeze or assign existing WIP.
2. Start P1-A, P1-B, P1-C, P1-D, and P1-E in parallel only after write-set
   ownership is clear.
3. Merge Phase 1 in this order when green: P1-A, P1-B, P1-C, P1-D, P1-E.
   Keep P1-E late because it owns `DetailActivity.java`.
4. Run P2-A only after Phase 1 detail/composer work is merged.
5. Run P2-B after P1-C; tablet home/search share tablet `activity_main.xml`.
6. Run P2-C last as a detail-family integration pass.
7. Run P3-A and P3-B with no concurrent emulator-heavy work.
8. Produce P3-C before morning review.

## Commit And Checkpoint Rules

- One slice per commit. Do not combine search, guide, thread, and emergency in
  the same commit.
- Each commit message should name the surface, for example:
  `polish android guide reader rails`.
- Each slice handoff must include:
  - exact files changed
  - exact commands run
  - screenshot artifact root
  - target screenshots reviewed
  - known residual mismatch, if any
- A slice is not visually closed from unit tests alone.
- Do not update target mocks as part of implementation.
- Do not add emergency landscape targets or acceptance requirements unless a
  new design target is explicitly tracked.
- If a worker touches a chokepoint outside its reserved write set, pause and
  ask for orchestration before committing.

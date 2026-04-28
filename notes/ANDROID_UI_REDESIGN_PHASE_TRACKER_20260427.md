# Android UI Redesign Phase Tracker - 2026-04-27

Purpose: overnight delegation map for the remaining Android target-screenshot
work. This is a planning-only lane. The planner owns this note only; do not
edit, revert, stash, or clean Android source from this lane.

## Inputs Read

- Target mocks under `artifacts/mocks`: 22 PNGs covering home, search, answer,
  thread, guide, and emergency. Emergency has portrait-only targets.
- Latest state pack:
  `artifacts/ui_state_pack/20260427_233208`.
- Latest progress note:
  `notes/ANDROID_UI_REDESIGN_PROGRESS_20260427.md`.
- Wave8 visual review:
  `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE8_STATE_PACK_231419.md`.
- Current `git status --short`, `git diff --stat`, and recent git log.

## Current Baseline

- Current HEAD: `7338983` (`record android wave8 state pack proof`).
- Latest local state pack:
  `artifacts/ui_state_pack/20260427_233208`.
- Pack status: `pass`, 47 states, 47 pass, 0 fail, 0 platform ANRs.
- Matrix health: homogeneous APK/model, 0 rotation mismatches.
- APK SHA: `c5e8b4cc57b59bfef2b7cc481d6ba461681135d0e556b64342a18f685821697a`.
- Model: `gemma-4-e2b-it-litert`.
- Model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.

Wave8 moved this from harness-readiness to visual-parity work:

- Search capture now uses `rain shelter`.
- Home manual-shell category counts match the target contract:
  Shelter 84, Water 67, Fire 52, Food 91, Medicine 73, Tools 119.
- Tablet search score markers now use compact check labels.
- Phone-landscape host answer now has a true
  `rain_shelter_gd345_split_answer` proof capture.

Wave9 local integration proof before commit:

- Full state pack:
  `artifacts/ui_state_pack/20260428_000801`.
- Pack status: `pass`, 47 states, 47 pass, 0 fail, 0 platform ANRs.
- Matrix health: homogeneous APK/model, 0 rotation mismatches.
- APK SHA:
  `d57faf541306f67922a5566ade41cc6e25a6455ee0d267f9f5f413d02a291d27`.
- Model: `gemma-4-e2b-it-litert`.
- Model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
- Focused pre-pack checks passed for guide phone portrait, guide phone
  landscape, thread phone portrait, thread phone landscape, and the host-backed
  phone-landscape `rain_shelter_gd345_split_answer` proof.
- Visual movement: home/search density, guide paper shell, compact thread
  transcript rows, and phone-landscape source-rail viewport stability.

## Fixed Device Matrix

| Role | Device | Screenshot dimensions | Density |
| --- | --- | --- | --- |
| Phone portrait | `emulator-5556` | `1080x2400` | 420 |
| Phone landscape | `emulator-5560` | `2400x1080` | 420 |
| Tablet portrait | `emulator-5554` | `1600x2560` | 320 |
| Tablet landscape | `emulator-5558` | `2560x1600` | 320 |

## Worktree Guard

Current dirty Android files appear to be active implementation work owned by
other agents. Do not revert or reformat them from this planner lane.

- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailSessionPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`

Untracked guide paper drawables are also present:

- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_link_row.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_panel.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_section_label.xml`

Every implementation worker starts with:

```powershell
git status --short
git log --oneline -n 8 --decorate
git diff --stat
```

If a planned slice needs a dirty file listed above, assign it to the current
owner of that dirty change or resequence the slice. Do not open a second
parallel implementation on the same chokepoint file.

## Current Visual Gap Ranking

Use `artifacts/mocks` as the visual truth. Latest pack `20260427_233208` is
trusted for screenshot availability, but not for final visual parity.

1. Guide reader document surface: biggest gap. Target is a cream field-manual
   document with sections and cross-reference rails; current capture is still
   closer to dark detail chrome.
2. Thread transcript view: target is a compact two-turn transcript; current
   capture still reads as large answer/detail blocks.
3. Emergency burn-hazard state: target is burn hazard with four immediate
   actions; current emergency capture is trusted but scenario/hierarchy still
   needs target alignment.
4. Global chrome and density: headers, nav, card padding, and typography remain
   heavier than the mocks across families.
5. Search visual language: inputs now match better, but result cards remain too
   heavy versus compact rows/ticks.
6. Answer density: phone-landscape true split is proven, but answer/tablet
   surfaces still need flatter document/source composition.

## Target Mock Contract

| Family | Contract |
| --- | --- |
| Home | Dark compact field-manual library. Phone portrait has status pill, search block, 2x3 category grid, and three recent-thread rows. Phone landscape uses left rail plus dense library/recent split. Tablet home keeps rail, content column, and recent-thread balance. Counts: Shelter 84, Water 67, Fire 52, Food 91, Medicine 73, Tools 119. |
| Search | Query is `rain shelter`, count is 4, timing is `12ms`. Phone is compact list rows. Phone landscape shows three dense rows. Tablet landscape has filter rail, result list, and preview pane; tablet portrait has filter/results without a preview rail. Score marks are compact ticks, not heavy chips or text artifacts. |
| Answer | `Rain shelter` answer, GD-345, 3 sources, unsure fit. Phone portrait is text-first with compact sources and related guides. Phone landscape is a true split answer: answer body left, sources/related guides right, composer stable. Tablet targets require actual answer-state proof, not guide/source destination substitutes. |
| Thread | `Rain shelter` thread with 2 turns. Landscape target keeps thread/answer content left and sources right with a stable follow-up composer. Rows should be flatter and denser than the current large-card treatment. |
| Guide | `Foundry & Metal Casting`, GD-132, cream paper reader. Phone target is paper body plus bottom nav. Tablet landscape target has sections rail left, centered paper page, cross-reference rail right. Guide semantics must stay separate from answer semantics. |
| Emergency | Burn hazard answer, danger badge, one strong danger lane, four ordered immediate actions, one evidence/provenance card, quiet composer. Tablet portrait keeps the same emergency hierarchy and a large quiet lower field. No emergency landscape target exists. |

## Target Screenshot Checklist

| Target mock | Phase | Required state-pack evidence |
| --- | --- | --- |
| `home-phone-portrait.png` | P1 | `phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` |
| `home-phone-landscape.png` | P1 | `phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` |
| `home-tablet-portrait.png` | P1 | `tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` |
| `home-tablet-landscape.png` | P1 | `tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` |
| `search-phone-portrait.png` | P1 | `phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` |
| `search-phone-landscape.png` | P1 | `phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` |
| `search-tablet-portrait.png` | P1 | `tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` |
| `search-tablet-landscape.png` | P1 | `tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` |
| `guide-phone-portrait.png` | P2 | `phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png` and `phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` |
| `guide-phone-landscape.png` | P2 | `phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png` and `phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` |
| `guide-tablet-portrait.png` | P2 | `tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png` and `tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` |
| `guide-tablet-landscape.png` | P2 | `tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png` and `tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` |
| `thread-phone-portrait.png` | P3 | `phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` |
| `thread-phone-landscape.png` | P3 | `phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` |
| `thread-tablet-portrait.png` | P3 | `tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` |
| `thread-tablet-landscape.png` | P3 | `tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` |
| `answer-phone-portrait.png` | P4 | `phone_portrait/deterministicAskNavigatesToDetailScreen__deterministic_detail.png` and `phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` |
| `answer-phone-landscape.png` | P4 | `phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png` plus deterministic regression screenshot |
| `answer-tablet-portrait.png` | P4 | `tablet_portrait/deterministicAskNavigatesToDetailScreen__deterministic_detail.png` and `tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` |
| `answer-tablet-landscape.png` | P4 | `tablet_landscape/deterministicAskNavigatesToDetailScreen__deterministic_detail.png` and `tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` |
| `emergency-phone-portrait.png` | P5 | `phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png` |
| `emergency-tablet-portrait.png` | P5 | `tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png` |

## Screenshot Review Gates

No phase is complete from unit tests alone.

Each implementation slice must leave a handoff with:

- Changed files and known file locks.
- Unit/assemble commands run.
- Focused or full state-pack root.
- Exact target mocks reviewed.
- Exact screenshot paths reviewed from that state pack.
- Residual mismatches split into content/data, layout/density, and behavior.
- A short note confirming whether XML dumps still expose required labels for
  harness assertions.

Use a focused pack during slice work and a full four-device pack before closure.
If a focused run changes only one family, still include regression screenshots
for any shared chrome or detail chokepoint it touches.

## Phase 0 - Dispatch Locks And Evidence Sync

Owner: orchestrator / notes lane.

Goal: prevent workers from stepping on each other while visual parity work is
still split across shared Android files.

Checklist:

- [ ] Confirm current dirty Android files have named owners before assigning
  new work.
- [ ] Start new work from `7338983` plus the current dirty owner context, or
  from a newer explicit commit/pack named in the handoff.
- [ ] Treat `artifacts/ui_state_pack/20260427_233208` as the baseline unless a
  newer passing pack is named.
- [ ] Preserve Wave8 progress evidence; do not resurrect cleared harness
  blockers as active blockers.
- [ ] Keep emergency acceptance portrait-only.

Likely owner files:

- Notes only for this phase.

Parallel safety:

- This phase can run beside implementation work as long as it edits notes only.

## Phase 1 - Home And Search Final Density Pass

Owner slice: home/search shell lane.

Priority: high, but respect the current dirty work in `MainActivity.java` and
`SearchResultAdapter.java`.

Screens/form factors:

- Home phone portrait, phone landscape, tablet portrait, tablet landscape.
- Search phone portrait, phone landscape, tablet portrait, tablet landscape.

Acceptance checklist:

- [ ] Home category counts stay exact: Shelter 84, Water 67, Fire 52, Food 91,
  Medicine 73, Tools 119.
- [ ] Home recent-thread rows match the mock rhythm: dense, flat, unclipped, and
  visible in all postures.
- [ ] Phone portrait home keeps status/search/category/recent order without
  bottom-nav overlap.
- [ ] Phone landscape home keeps the left rail and compact library/recent split.
- [ ] Tablet home keeps rail/content/recent balance without oversized top chrome.
- [ ] Search query remains `rain shelter`, result count remains 4, timing remains
  `12ms` or a documented harness-content exception.
- [ ] Search rows are compact rows, not bulky cards; phone landscape shows three
  dense rows.
- [ ] Tablet portrait has filters plus results without a preview rail.
- [ ] Tablet landscape has filter rail, selected result list, and preview pane.
- [ ] Score markers remain compact check/tick labels with numeric scores.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/list_item_result.xml`
- `android-app/app/src/main/res/drawable/bg_manual_phone_home_root.xml`
- `android-app/app/src/main/res/drawable/bg_manual_home_recent_row.xml`
- `android-app/app/src/main/res/drawable/bg_search_result_row_dark.xml`
- `android-app/app/src/main/res/drawable/bg_search_score_tick.xml`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityIdentityTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

Suggested validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.MainActivityHomeChromeTest" --tests "com.senku.mobile.MainActivityIdentityTest" --tests "com.senku.mobile.MainActivityPhoneNavigationTest" --tests "com.senku.mobile.SearchResultAdapterTest" --tests "com.senku.ui.search.SearchResultCardHeuristicsTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_home_search_density -MaxParallelDevices 4
```

Screenshot review gate:

- Compare the eight P1 target mocks against the eight home/search screenshots
  in the focused pack.
- Check dumps for query/count/category labels so visual changes did not hide
  harness anchors.

Parallel safety:

- Can run beside P3 thread work only if P3 stays out of `MainActivity.java`,
  search adapter files, and shared app chrome.
- Do not run beside another home/search slice.

## Phase 2 - Guide Document Reader Station

Owner slice: guide/detail lane.

Priority: highest visual gap. Current dirty files already include guide paper
shell drawables and `activity_detail` layouts, so treat this as a detail
chokepoint.

Screens/form factors:

- Guide phone portrait, phone landscape, tablet portrait, tablet landscape.

Acceptance checklist:

- [ ] Guide target content is `Foundry & Metal Casting`, GD-132.
- [ ] Phone guide uses cream paper reader body with compact section typography
  and bottom nav.
- [ ] Phone landscape reduces dominant dark chrome and keeps paper content
  readable above the fold.
- [ ] Tablet portrait has a paper-reader treatment, not answer/detail chrome.
- [ ] Tablet landscape has sections rail left, centered paper page, and
  cross-reference rail right.
- [ ] Related-guide preview and off-rail cross-reference behavior remain green.
- [ ] Guide semantics stay separate from answer semantics in dumps and UI copy.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_panel.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_link_row.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_section_label.xml`
- `android-app/app/src/main/java/com/senku/ui/guide/GuideReaderScreen.kt` if
  a first-class guide Compose surface is created.
- `android-app/app/src/main/java/com/senku/ui/guide/GuideRail.kt` if created.
- `android-app/app/src/main/java/com/senku/ui/guide/CrossReferenceRail.kt` if
  created.
- `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/ui/guide/*Test.kt` if guide Compose
  files are added.

Suggested validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailGuidePresentationFormatterTest" --tests "com.senku.mobile.DetailGuideContextPresentationFormatterTest" --tests "com.senku.mobile.DetailRelatedGuidePresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_guide_reader_station -MaxParallelDevices 4
```

Screenshot review gate:

- Compare all four guide mocks.
- Include the tablet landscape source-context/cross-reference screenshot.
- Handoff states whether the implementation is XML polish or a first-class
  guide Compose surface.

Parallel safety:

- Do not run beside P4 or P5 unless the same worker owns `DetailActivity.java`
  and `activity_detail` layouts.
- Can run beside P1 only if shared chrome/drawables are not touched.

## Phase 3 - Thread Transcript Density And Composer Stability

Owner slice: thread/detail lane.

Priority: high. Current dirty file
`DetailThreadHistoryRenderer.java` likely belongs here.

Screens/form factors:

- Thread phone portrait, phone landscape, tablet portrait, tablet landscape.

Acceptance checklist:

- [ ] Thread target content remains a `Rain shelter` two-turn thread.
- [ ] Rows look like compact transcript turns with Q/A labels, timestamps,
  anchors, confidence, and small guide chips.
- [ ] Phone portrait avoids large answer-card slabs and keeps the composer
  stable.
- [ ] Phone landscape keeps split behavior: thread/answer left, sources right.
- [ ] Tablet modes keep evidence/source rail visible without widening the
  transcript into oversized cards.
- [ ] Follow-up composer does not resize, overlap, or cover critical content.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailSessionPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailTranscriptFormatter.java`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailSessionPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailTranscriptFormatterTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
- `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`

Suggested validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --tests "com.senku.mobile.DetailSessionPresentationFormatterTest" --tests "com.senku.mobile.DetailTranscriptFormatterTest" --tests "com.senku.ui.tablet.StressReadingPolicyTest" --tests "com.senku.ui.tablet.TabletEvidenceVisibilityPolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_thread_transcript_density -MaxParallelDevices 4
```

Screenshot review gate:

- Compare all four thread mocks.
- Handoff explicitly mentions composer focus/IME behavior and whether the rail
  is visible in landscape/tablet screenshots.

Parallel safety:

- Can run beside P1 if it stays out of app shell/search files.
- Do not run beside P2/P4/P5 if it needs `DetailActivity.java` or detail
  layout changes.

## Phase 4 - Answer Document And Source Stack Density

Owner slice: answer/detail lane.

Priority: medium-high. Phone-landscape true split answer proof exists; this
phase is the density/source-stack convergence pass.

Screens/form factors:

- Answer phone portrait, phone landscape, tablet portrait, tablet landscape.

Acceptance checklist:

- [ ] Answer target content is `Rain shelter`, GD-345, 3 sources, unsure fit.
- [ ] Phone portrait is text-first with compact sources and related guides.
- [ ] Phone landscape uses the true
  `rain_shelter_gd345_split_answer` proof state, not a guide/source substitute.
- [ ] Landscape composer remains docked and does not obscure answer body.
- [ ] Tablet answer captures show actual answer mode, not guide reader or
  source-context destination.
- [ ] Source preview/footer copy is compact enough to leave the answer body
  above the fold.
- [ ] Uncertain-fit, abstain, deterministic, reviewed-evidence, and
  strong-evidence labels remain distinct.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- `android-app/app/src/main/res/drawable/bg_detail_answer_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailAnswerBodyFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailMetaPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProofPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProvenancePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailSurfaceContractTest.java`
- `android-app/app/src/test/java/com/senku/ui/answer/AnswerContentFactoryTest.kt`
- `android-app/app/src/test/java/com/senku/ui/answer/PaperAnswerCardLabelTest.kt`

Suggested validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailAnswerBodyFormatterTest" --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --tests "com.senku.mobile.DetailMetaPresentationFormatterTest" --tests "com.senku.mobile.DetailProofPresentationFormatterTest" --tests "com.senku.mobile.DetailProvenancePresentationFormatterTest" --tests "com.senku.mobile.DetailSurfaceContractTest" --tests "com.senku.ui.answer.AnswerContentFactoryTest" --tests "com.senku.ui.answer.PaperAnswerCardLabelTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_answer_document_density -MaxParallelDevices 4
```

Screenshot review gate:

- Compare all four answer mocks.
- For phone landscape, cite the `rain_shelter_gd345_split_answer` screenshot.
- Review note names content mismatch separately from layout/density mismatch.

Parallel safety:

- Sequence with P2 and P5 because they share `DetailActivity.java` and detail
  layouts.

## Phase 5 - Emergency Burn-Hazard Visual Parity

Owner slice: emergency/detail lane.

Priority: high after the detail chokepoint is free. Emergency screenshots are
trusted in `20260427_233208`, but target scenario and hierarchy are not done.

Screens/form factors:

- Emergency phone portrait.
- Emergency tablet portrait.

Acceptance checklist:

- [ ] Target scenario is burn hazard, not a different emergency answer.
- [ ] Danger badge and one strong danger lane are visible.
- [ ] Four ordered immediate-action rows are visible and compact.
- [ ] Exactly one evidence/provenance card carries the source context.
- [ ] Composer is quiet and does not compete with the urgent hierarchy.
- [ ] Tablet portrait keeps the wide emergency arrangement and quiet lower field.
- [ ] Emergency eligibility remains narrow.
- [ ] Negative controls stay non-emergency.
- [ ] No emergency landscape acceptance is added.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`

Suggested validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailActionBlockPresentationFormatterTest" --tests "com.senku.mobile.DetailWarningCopySanitizerTest" --tests "com.senku.mobile.EmergencySurfacePolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_emergency_burn_hazard -MaxParallelDevices 4
```

Screenshot review gate:

- Compare only:
  - `artifacts/mocks/emergency-phone-portrait.png`
  - `artifacts/mocks/emergency-tablet-portrait.png`
- Handoff names the true emergency prompt and at least two negative controls.

Parallel safety:

- Sequence with P2 and P4 unless the same worker owns the detail chokepoint.

## Phase 6 - Global Chrome And Density Harmonization

Owner slice: integration polish lane.

Goal: remove remaining heavy chrome after family-specific slices land. This is
not a replacement for P1-P5; it is the final shared pass.

Screens/form factors:

- At least one representative screenshot from every family and every posture
  touched by shared chrome.

Acceptance checklist:

- [ ] Header/status/nav areas are closer to mock proportions.
- [ ] Cards become flatter rows or document panels where the mocks require it.
- [ ] Typography scale is compact in tool surfaces and does not hide target
  content below the fold.
- [ ] Bottom navigation/composer areas do not occlude list rows or answer body.
- [ ] Shared drawables do not accidentally make answer, guide, thread, and
  emergency surfaces visually indistinguishable.

Likely owner files:

- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- Shared detail/home/search drawables and dimens, if present.
- Family-specific files already listed in P1-P5 if the polish changes them.

Suggested validation:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_global_chrome_density -MaxParallelDevices 4
```

Screenshot review gate:

- Review note must state which shared files changed and which families were
  visually rechecked.
- Any family whose shared chrome changed gets fresh screenshots, not borrowed
  screenshots from an older pack.

Parallel safety:

- Run after P1-P5 or give this to the same integrator who is landing the final
  family slice.

## Phase 7 - Full Mock-Parity Closure

Owner: final integration/review lane.

Acceptance checklist:

- [ ] `git diff --check` is clean.
- [ ] Unit tests and APK assembly pass.
- [ ] Full fixed-matrix state pack is `pass` or every accepted delta is named
  in a fresh review note.
- [ ] Every target mock has a current screenshot comparison.
- [ ] Review separates true content/data gaps from layout/density gaps.
- [ ] Review confirms no emergency landscape acceptance was added.

Final commands:

```powershell
git status --short
git diff --check
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\final_android_ui_redesign_20260427 -MaxParallelDevices 4
```

If launching a clean fixed matrix:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_headless_state_pack_lane.ps1 -OutputRoot artifacts\ui_state_pack\final_android_ui_redesign_headless_20260427 -LaunchProfile clean-headless -RealRun
```

## Dispatch Order

| Order | Slice | Start when | Do not run beside | First proof expected |
| --- | --- | --- | --- | --- |
| 0 | P0 locks/evidence | Immediately. | None, notes-only. | Named owners for dirty source files. |
| 1 | P2 guide reader | Detail/layout owner is available. | P4/P5 detail slices. | Four guide screenshots show cream document treatment. |
| 2 | P3 thread transcript | Thread renderer owner is available. | Detail layout changes unless same owner. | Four thread screenshots show compact transcript and stable composer. |
| 3 | P5 emergency burn hazard | Detail chokepoint is free. | P2/P4 detail slices. | Phone/tablet portrait emergency screenshots match burn-hazard hierarchy. |
| 4 | P1 home/search density | Current MainActivity/SearchResultAdapter WIP is owned or landed. | Other home/search slices. | Eight home/search screenshots match target content and compact rows. |
| 5 | P4 answer density | Guide/emergency detail work is stable. | P2/P5 detail slices. | Four answer screenshots show actual answer mode and compact sources. |
| 6 | P6 global chrome | Family slices have landed. | Any unowned source WIP. | Fresh screenshots for every family touched by shared chrome. |
| 7 | P7 closure | All target-family slices have handoffs. | Implementation work. | Full passing state pack plus mock checklist review. |

Notes on ordering:

- P2/P4/P5 are ordered mainly because they share `DetailActivity.java` and
  `activity_detail` layouts.
- P1 can be pulled earlier if the current home/search owner is ready.
- P3 can run in parallel with P1 if it stays in thread/composer files and avoids
  detail layout churn.

## Non-Negotiables

- Do not modify `artifacts/mocks`.
- Do not edit production Android files from the planner lane.
- Do not revert dirty work owned by another agent.
- Do not broaden emergency eligibility to satisfy a visual target.
- Do not expose reviewed-card runtime behavior outside developer/test controls.
- Do not call a slice complete without screenshot or dump proof from the fixed
  matrix or a clearly scoped focused smoke run.

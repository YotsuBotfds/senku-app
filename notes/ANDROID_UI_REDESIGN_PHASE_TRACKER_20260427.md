# Android UI Redesign Phase Tracker - 2026-04-27

Purpose: overnight delegation map for the remaining Android mock-parity work.
This tracker is planning-only. The planner lane's only write set is this note.
Do not edit, revert, stash, or clean Android source from this lane.

## Inputs Read

- Target mocks under `artifacts/mocks`; visually checked all home, search,
  answer, thread, guide, and emergency target files in that directory.
- Latest progress note: `notes/ANDROID_UI_REDESIGN_PROGRESS_20260427.md`.
- Wave 6 plan: `notes/ANDROID_UI_REDESIGN_WAVE6_PLAN_20260427.md`.
- Wave 7 reviews:
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE7_HOME_SEARCH.md`
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE7_DETAIL_SURFACES.md`
  - `notes/ANDROID_VISUAL_REVIEW_20260427_WAVE7_STATE_PACK_TRIAGE.md`
- Freshest reviewed state pack:
  `artifacts/ui_state_pack/20260427_225149`.

## Current Baseline

- Current local/pushed HEAD before Wave7 commit: `094ca52`
  (`advance android W6 visual and harness fixes`).
- Latest full state pack:
  `artifacts/ui_state_pack/20260427_231419`.
- Pack status: `pass`, 47 states, 47 pass, 0 fail, 0 platform ANRs.
- Matrix health: homogeneous APK/model, no rotation mismatches.
- APK SHA: `f2a2c0f3295a4a84a9c8f895273fcb7f95d2fc5b307d43f7ab3cad44525bc6a7`.
- Model: `gemma-4-e2b-it-litert`.
- Model SHA: `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.

Some older notes still name `3b8baf6` or `094ca52` as the baseline because they
were written before the Wave7 closure pack. Use `artifacts/ui_state_pack/20260427_231419`
as the starting point for new visual-parity work.

## Worktree Guard

Current dirty Android files appear to be another worker's active work. Do not
revert them from this planner lane.

- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`

Untracked Wave 7 review notes are also present. Treat them as live review
evidence, not cleanup targets.

Every implementation worker starts with:

```powershell
git status --short
git log --oneline -n 8 --decorate
```

If a planned slice needs a dirty file listed above, assign that slice to the
worker who owns the dirty change or explicitly resequence the work.

## Fixed Device Matrix

| Role | Device | Target dimensions in latest pack |
| --- | --- | --- |
| Phone portrait | `emulator-5556` | `1080x2400`, density 420 |
| Phone landscape | `emulator-5560` | `2400x1080`, density 420 |
| Tablet portrait | `emulator-5554` | `1600x2560`, density 320 |
| Tablet landscape | `emulator-5558` | `2560x1600`, density 320 |

Emergency mock coverage is portrait-only:

- `artifacts/mocks/emergency-phone-portrait.png`
- `artifacts/mocks/emergency-tablet-portrait.png`

Do not add emergency landscape acceptance unless a new target mock is added.

## Target Mock Contract

Use `artifacts/mocks` as the visual truth, not older captures.

| Family | Mock contract to preserve |
| --- | --- |
| Home | Dark compact field-manual library. Phone portrait has status pill, search block, 2x3 category grid, and three recent-thread rows. Phone landscape uses left rail plus dense library/recent split. Tablet home keeps rail, content column, and recent-thread balance. Category counts in mocks are Shelter 84, Water 67, Fire 52, Food 91, Medicine 73, Tools 119. |
| Search | Query is `rain shelter`, count is 4, timing is `12ms`. Phone is a compact list, not card blocks. Phone landscape shows three dense rows. Tablet landscape has filter rail, result list, and preview pane; tablet portrait has filter/results without a preview rail. Score marks are compact ticks, not heavy chips or hyphen artifacts. |
| Answer | `Rain shelter` answer, GD-345, 3 sources, unsure fit. Phone portrait is text-first with compact sources and related guides. Phone landscape is a true split answer: answer body left, sources/related guides right, composer stable. Tablet answer targets need actual answer-state proof, not guide/source destination substitutes. |
| Thread | `Rain shelter` thread with 2 turns. Landscape target keeps thread/answer content left and sources right with a stable follow-up composer. Rows should be flatter and denser than the current large-card treatment. |
| Guide | `Foundry & Metal Casting`, GD-132, cream paper reader. Phone target is paper body plus bottom nav. Tablet landscape target has sections rail left, centered paper page, cross-reference rail right. Guide semantics must stay separate from answer semantics. |
| Emergency | Burn hazard answer, danger badge, one strong danger lane, four ordered immediate actions, one evidence/provenance card, quiet composer. Tablet portrait keeps the same emergency hierarchy and a large quiet lower field. |

## Wave 7 Status Snapshot

| Surface | Current verdict | Fresh evidence |
| --- | --- | --- |
| Home, all postures | Partial | Structure exists, but density, target content, counts, and recent-thread rows still diverge. |
| Search, phone landscape | Partial | Former blank-state blocker is fixed. Still uses `fire` content and card-like result rhythm instead of compact `rain shelter` rows. |
| Search, tablet portrait/landscape | Partial, with linked-guide failures | Results render, but tablet linked-guide handoff readiness fails in both tablet postures. Filters are text-like and preview/cue discoverability is incomplete. |
| Phone portrait deterministic answer | Pass for capture | Anchor/source chips are visible, but content differs from target mock and density still needs polish. |
| Phone landscape answer | Partial, with one failure | Fresh captures prove source/cross-reference paths, but not the mock's true split answer. Generative landscape has a provenance wording/visibility assertion failure. |
| Phone landscape thread | Pass for split behavior | Expected split behavior is present. Density remains larger than mock. |
| Guide cross-reference | Partial to pass by posture | Tablet related/cross-reference captures exist, but styling is still dark detail surface rather than cream guide reader. Phone related-guide preview title still fails. |
| Emergency phone portrait | Pass for surface behavior | True emergency surface captured, but content differs from burn-hazard target. |
| Emergency tablet portrait | Fail | Summary exists but no trusted screenshot; assertion says source or handoff context is not visible in tablet portrait three-pane mode. |

## Top 5 Next Slices

1. P2-A/P2-B Home and search mock-content/density alignment: all four search
   postures on `rain shelter` rows, then all four home postures on target
   counts and recent-thread rows.
2. P3-A Answer phone landscape true split answer capture and density parity,
   guarded by answer phone portrait.
3. P3-C Thread density: compact multi-turn layout and stable composer across
   all four postures.
4. P4-A/P4-B Guide reader: cream paper reader styling, sections rail, and
   cross-reference rail while keeping the now-passing behavior.
5. Phase 5 Emergency visual polish: burn-hazard hierarchy and immediate-action
   density against phone/tablet portrait target mocks.

## Overnight Dispatch Queue

| Order | Slice | Assign when | Do not run beside | First proof expected |
| --- | --- | --- | --- | --- |
| 1 | P2-A search content/density | Wave7 full pack is passing. | Search adapter/card slices with overlapping files. | Four search screenshots match `rain shelter`, 4 results, compact score ticks. |
| 2 | P2-B home density/content | A home worker can stay out of detail chokepoints. | None if it avoids `DetailActivity.java` and shared dirty files. | Four home screenshots show target category counts and unclipped recent/category rows. |
| 3 | P3-A/P3-B answer parity | Detail owner is available. | P3-C, P4-A, P4-B, Phase 5 if `DetailActivity.java` overlaps. | Phone landscape answer screenshot is the actual split answer, not a guide/source destination. |
| 4 | P3-C thread density | Answer parity is stable or a separate worker owns disjoint Compose files. | Detail chokepoint slices if composer/detail files overlap. | Four thread screenshots keep composer stable and rails visible. |
| 5 | P4-A/P4-B guide reader | Related-guide behavior remains green. | Other `DetailActivity.java` slices. | Cream paper guide reader screenshots for phone/tablet, with cross-reference behavior preserved. |
| 6 | Phase 5 emergency polish | Tablet and phone emergency captures remain green. | Other `DetailActivity.java` slices. | Phone and tablet portrait emergency screenshots match burn-hazard hierarchy only. |

## Phase 0 - Orchestration And Evidence Guard

Owner: orchestrator / notes lane.

Screens/form factors: all; this phase does not change UI.

Acceptance criteria:

- Dirty Android files are assigned before implementation starts.
- Wave 7 review notes are preserved and referenced in handoffs.
- Workers use `094ca52` plus `artifacts/ui_state_pack/20260427_225149` as the
  baseline unless a newer state pack is explicitly named.
- No implementation slice claims visual closure from unit tests alone.
- Emergency landscape is not added to acceptance.

Likely owner files:

- Notes only for this lane.
- Implementation workers should own their production files in the later phases.

Suggested checks:

```powershell
git status --short
git log --oneline -n 8 --decorate
git diff --stat
```

## Phase 1 - P0 Harness Blockers Before Visual Polish

These six failures block trusted screenshots or indicate live app/copy
contracts. They should be cleared before broad density work.

### P1-A Tablet Linked-Guide Handoff Readiness

Screens/form factors:

- Search tablet portrait.
- Search tablet landscape.

Current failure:

- `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` fails on both tablet
  postures.
- Results render with 4 items, but `visiblePreview` and `visibleCue` are empty
  and the handoff never becomes ready.

Acceptance criteria:

- Tablet search results expose a discoverable linked-guide cue, selected preview,
  or tested tablet-specific handoff path.
- Portrait and landscape both reach trusted screenshots for the linked-guide
  handoff state.
- Phone search behavior remains stable.
- If tablet rich cards intentionally suppress phone-style cues, update the
  harness contract only with explicit evidence and reviewer signoff.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/res/layout/list_item_result.xml`
- `android-app/app/src/main/res/layout-sw600dp/list_item_result.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainPresentationFormatterTest.java`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.SearchResultAdapterTest" --tests "com.senku.mobile.MainPresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_tablet_linked_guide -MaxParallelDevices 4
```

State-pack acceptance:

- Tablet portrait and tablet landscape have copied screenshots for
  `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`.
- Raw dumps include a visible linked-guide cue, selected preview, or linked
  destination text.

### P1-B Tablet Portrait Emergency Source Context

Screens/form factors:

- Emergency tablet portrait.
- Regression: emergency phone portrait.

Current failure:

- `emergencyPortraitAnswerShowsImmediateActionState` fails on tablet portrait.
- Emergency answer and source exist, but tablet portrait three-pane mode does
  not satisfy the visible source or handoff context contract.

Acceptance criteria:

- Tablet portrait produces a trusted emergency screenshot.
- Danger banner, four immediate actions, evidence/provenance context, and
  composer are visible or reachable without contradicting the target hierarchy.
- Phone portrait emergency remains captured.
- Negative controls do not receive emergency styling.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.EmergencySurfacePolicyTest" --tests "com.senku.mobile.DetailActionBlockPresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_emergency_portrait -MaxParallelDevices 4
```

State-pack acceptance:

- `phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
  exists.
- `tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
  exists.
- Emergency review compares only to the two portrait emergency mocks.

### P1-C Detail Copy And Provenance Contracts

Screens/form factors:

- Phone portrait guide related paths.
- Phone landscape guide related paths.
- Phone landscape generative answer provenance.

Current failures:

- `guideDetailShowsRelatedGuideNavigation` fails on phone portrait and phone
  landscape because preview title no longer identifies the selected linked
  guide.
- `generativeAskWithHostInferenceNavigatesToDetailScreen` fails on phone
  landscape because visible provenance wording is missing from the panel the
  harness can see.

Acceptance criteria:

- Phone related-guide preview title clearly identifies the selected linked
  guide in both postures.
- Phone landscape generated answer keeps provenance/source-preview wording
  visible in the inspected panel.
- The fix does not flood the UI with verbose copy or regress guide reader
  density.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailSourcePresentationFormatter.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailGuideContextPresentationFormatterTest" --tests "com.senku.mobile.DetailRelatedGuidePresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_detail_copy_contracts -MaxParallelDevices 4
```

State-pack acceptance:

- The three failing tests above move to pass.
- Trusted screenshots or dumps exist for phone portrait and phone landscape
  guide related paths, plus phone landscape generative answer provenance.

## Phase 2 - Search And Home Mock Parity

Start this after Phase 1-A is stable, since tablet search readiness and visual
search polish share owner files.

### P2-A Search Content And Density

Screens/form factors:

- Search phone portrait.
- Search phone landscape.
- Search tablet portrait.
- Search tablet landscape.

Acceptance criteria:

- Search visual state uses target content: `rain shelter`, 4 results, and
  compact results matching the mocks.
- Phone portrait shows four dense list rows without bottom nav occluding the
  final row.
- Phone landscape shows three dense rows and no blank or oversized content
  area.
- Tablet portrait has filter rail plus dense results with no preview rail.
- Tablet landscape has filter rail, selected result list, and preview pane.
- Filter controls look like controls, not literal `[]` text.
- Score marks render as compact ticks with numeric scores, not bulky chips.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/list_item_result.xml`
- `android-app/app/src/main/res/drawable/bg_search_result_row_dark.xml`
- `android-app/app/src/main/res/drawable/bg_search_score_tick.xml`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.SearchResultAdapterTest" --tests "com.senku.ui.search.SearchResultCardHeuristicsTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_search_density -MaxParallelDevices 4
```

State-pack acceptance:

- Compare all four search screenshots against:
  - `artifacts/mocks/search-phone-portrait.png`
  - `artifacts/mocks/search-phone-landscape.png`
  - `artifacts/mocks/search-tablet-portrait.png`
  - `artifacts/mocks/search-tablet-landscape.png`

### P2-B Home Density And Target Content

Screens/form factors:

- Home phone portrait.
- Home phone landscape.
- Home tablet portrait.
- Home tablet landscape.

Acceptance criteria:

- Home mocks' category counts and recent-thread content are visible in the
  target test state, or the review note explicitly calls out intentional
  harness-content divergence.
- Phone landscape keeps the left rail and two-pane home split without clipping
  the lower category row.
- Phone portrait keeps status/search/category/recent rhythm close to target.
- Tablet home preserves rail/content/recent structure with tighter category and
  recent-thread rows.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/main/res/drawable/bg_manual_phone_home_root.xml`
- `android-app/app/src/main/res/drawable/bg_manual_home_recent_row.xml`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityIdentityTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.MainActivityHomeChromeTest" --tests "com.senku.mobile.MainActivityIdentityTest" --tests "com.senku.mobile.MainActivityPhoneNavigationTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_home_density -MaxParallelDevices 4
```

State-pack acceptance:

- Compare all four home screenshots against the home mocks.
- Verify no duplicate IDs in home/search XML after layout edits.

## Phase 3 - Answer And Thread Detail Parity

### P3-A Phone Landscape True Split Answer

Screens/form factors:

- Answer phone landscape.
- Regression: answer phone portrait.

Acceptance criteria:

- Fresh phone landscape capture matches the target split-answer surface:
  answer body on the left, sources and related guides on the right.
- The reviewed capture is not a source/guide destination substitute.
- Composer remains docked and does not obscure the answer body.
- Phone portrait deterministic answer keeps source chips and related guides
  visible.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailSurfaceContractTest.java`
- `android-app/app/src/test/java/com/senku/ui/answer/PaperAnswerCardLabelTest.kt`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --tests "com.senku.mobile.DetailSurfaceContractTest" --tests "com.senku.ui.answer.PaperAnswerCardLabelTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_answer_split -MaxParallelDevices 4
```

State-pack acceptance:

- Compare phone answer screenshots against:
  - `artifacts/mocks/answer-phone-portrait.png`
  - `artifacts/mocks/answer-phone-landscape.png`

### P3-B Answer Body And Tablet Answer Density

Screens/form factors:

- Answer tablet portrait.
- Answer tablet landscape.
- Regression: answer phone portrait and phone landscape.

Acceptance criteria:

- Tablet answer captures show actual answer mode, not guide reader or
  source-context substitution.
- Answer body is text-first and flatter, with source/proof labels preserved.
- Uncertain-fit, abstain, deterministic, reviewed-evidence, and strong-evidence
  labels remain distinct.
- Source preview/footer copy is compact enough to leave the answer body above
  the fold.

Likely owner files:

- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/drawable/bg_detail_answer_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailAnswerBodyFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailMetaPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProofPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProvenancePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/ui/answer/AnswerContentFactoryTest.kt`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailAnswerBodyFormatterTest" --tests "com.senku.mobile.DetailMetaPresentationFormatterTest" --tests "com.senku.mobile.DetailProofPresentationFormatterTest" --tests "com.senku.mobile.DetailProvenancePresentationFormatterTest" --tests "com.senku.ui.answer.AnswerContentFactoryTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_answer_tablet_density -MaxParallelDevices 4
```

State-pack acceptance:

- Compare all four answer screenshots against answer mocks.
- Review note names any remaining content mismatch separately from layout
  mismatch.

### P3-C Thread Density And Composer Stability

Screens/form factors:

- Thread phone portrait.
- Thread phone landscape.
- Thread tablet portrait.
- Thread tablet landscape.

Acceptance criteria:

- Phone landscape split behavior remains pass.
- Thread rows are flatter and closer to mock density across all postures.
- Source/evidence rail remains visible in landscape and tablet modes.
- Follow-up composer is stable and does not resize or cover critical content.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailSessionPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
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

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --tests "com.senku.mobile.DetailSessionPresentationFormatterTest" --tests "com.senku.mobile.DetailTranscriptFormatterTest" --tests "com.senku.ui.tablet.StressReadingPolicyTest" --tests "com.senku.ui.tablet.TabletEvidenceVisibilityPolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_thread_density -MaxParallelDevices 4
```

State-pack acceptance:

- Compare all four thread screenshots against thread mocks.
- Handoff explicitly mentions composer focus and IME behavior.

## Phase 4 - Guide Reader And Cross-Reference Parity

Start after Phase 1-C unless the same owner is already holding the dirty guide
context files.

### P4-A Phone Guide Related And Cross-Reference Capture

Screens/form factors:

- Guide phone portrait.
- Guide phone landscape.

Acceptance criteria:

- Related-guide preview title identifies the selected linked guide.
- Off-rail cross-reference copy remains visible.
- Phone guide screenshots are trusted captures, not failed pre-capture states.
- Styling moves toward guide-reader paper body without breaking current
  cross-reference behavior.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailGuidePresentationFormatterTest" --tests "com.senku.mobile.DetailGuideContextPresentationFormatterTest" --tests "com.senku.mobile.DetailRelatedGuidePresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_phone_guide -MaxParallelDevices 4
```

State-pack acceptance:

- Compare phone guide screenshots against:
  - `artifacts/mocks/guide-phone-portrait.png`
  - `artifacts/mocks/guide-phone-landscape.png`

### P4-B Tablet Guide Reader Station

Screens/form factors:

- Guide tablet portrait.
- Guide tablet landscape.

Acceptance criteria:

- Tablet landscape has sections rail, centered cream paper page, and
  cross-reference rail.
- Tablet portrait keeps paper-reader treatment and related/cross-reference
  navigation without relying on dark answer/detail chrome.
- Existing tablet related/cross-reference behavior remains captured.
- Guide semantics stay separate from answer semantics.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/ui/guide/GuideReaderScreen.kt` if created
- `android-app/app/src/main/java/com/senku/ui/guide/GuideRail.kt` if created
- `android-app/app/src/main/java/com/senku/ui/guide/CrossReferenceRail.kt` if created
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/test/java/com/senku/ui/guide/*Test.kt` if guide Compose files are created

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailGuidePresentationFormatterTest" --tests "com.senku.mobile.DetailGuideContextPresentationFormatterTest" --tests "com.senku.mobile.DetailRelatedGuidePresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_tablet_guide_reader -MaxParallelDevices 4
```

State-pack acceptance:

- Compare tablet guide screenshots against:
  - `artifacts/mocks/guide-tablet-portrait.png`
  - `artifacts/mocks/guide-tablet-landscape.png`
- Review note explicitly states whether any guide-reader station work is a
  first-class Compose surface or XML polish.

## Phase 5 - Emergency Visual Polish

Start after Phase 1-B produces trusted tablet portrait emergency proof.

Screens/form factors:

- Emergency phone portrait.
- Emergency tablet portrait.

Acceptance criteria:

- Target burn-hazard emergency hierarchy is matched: danger badge, one danger
  lane, four ordered action rows, one evidence card, quiet composer.
- Tablet portrait has the target wide emergency arrangement and lower empty
  field without losing source context.
- Emergency eligibility remains narrow.
- Negative controls stay non-emergency.
- No emergency landscape acceptance is created.

Likely owner files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`

Suggested validation/state-pack checks:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailActionBlockPresentationFormatterTest" --tests "com.senku.mobile.DetailWarningCopySanitizerTest" --tests "com.senku.mobile.EmergencySurfacePolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_emergency_polish -MaxParallelDevices 4
```

State-pack acceptance:

- Compare only:
  - `artifacts/mocks/emergency-phone-portrait.png`
  - `artifacts/mocks/emergency-tablet-portrait.png`
- Handoff names the true emergency prompt and at least two negative controls.

## Phase 6 - Full Mock-Parity Review And Closure

Owner: final integration/review lane after slice commits land.

Acceptance criteria:

- `git diff --check` is clean.
- Unit tests and APK assembly pass.
- Full four-device state pack is `pass` or has only accepted visual deltas
  documented in a fresh review note.
- Every target mock family has a current screenshot comparison:
  home, search, answer, thread, guide, emergency.
- Review separates true knowledge/content gaps from layout/density gaps.

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

## Delegation Notes

- P1-A and P2-A both touch search files. Do not run them in parallel unless the
  same owner owns both.
- P1-B, P1-C, P3-A, P4-A, P4-B, and Phase 5 all touch `DetailActivity.java`.
  Sequence these tightly.
- Home density can run while detail work is active if it stays out of
  `DetailActivity.java` and shared formatter files.
- Thread density can run after detail copy contracts are stable if it stays in
  tablet/thread/composer files.
- One slice per commit. Each handoff should name files changed, commands run,
  state-pack root, target mocks reviewed, and residual mismatches.

## Non-Negotiables

- Do not modify `artifacts/mocks`.
- Do not edit production Android files from the planner lane.
- Do not broaden emergency eligibility to satisfy a visual target.
- Do not expose reviewed-card runtime behavior outside developer/test controls.
- Do not call a slice complete without screenshot or dump proof from the fixed
  matrix or a clearly scoped focused smoke run.

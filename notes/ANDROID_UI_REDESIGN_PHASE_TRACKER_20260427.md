# Android UI Redesign Phase Tracker - 2026-04-27

Purpose: current planning map for the remaining Android target-screenshot
redesign work. This is a planner lane only. The Tracker-Planner owns exactly
one write target: `notes/ANDROID_UI_REDESIGN_PHASE_TRACKER_20260427.md`.

## Current Anchor And Proof

- Current pushed anchor: `f9b6e11`
  (`advance android mock parity wave11`), observed at `HEAD`, `origin/master`,
  and `origin/HEAD` during this refresh.
- Current technical proof:
  `artifacts/ui_state_pack/20260428_013056`.
  - Status: `pass`, 47 states, 47 pass, 0 fail, 0 platform ANRs.
  - Matrix APK homogeneity: `true`.
  - APK SHA:
    `25363347fe8556cb4595daed1a556f2b86720f1c2616abdbfdf8b91327dbd358`.
  - Model: `gemma-4-e2b-it-litert`.
  - Model SHA:
    `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
  - Rotation mismatch count: 0.
- Local quality gate passed after the linked-guide search preview binding fix:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1`.
- Current target mocks: `artifacts/mocks`, 22 PNGs total:
  - home, search, answer, guide, and thread each cover phone portrait, phone
    landscape, tablet portrait, and tablet landscape.
  - emergency covers phone portrait and tablet portrait only.

Interpretation:

- `20260428_013056` is the current acceptance-grade technical proof pack for
  state-pack health, APK homogeneity, model identity, and device rotation.
- Passing technical proof is not final visual parity. Remaining work is judged
  against `artifacts/mocks` and must cite current screenshots and XML dumps.
- Earlier diagnostic packs and focused runs are superseded for baseline
  planning. Keep them as regression context only when a worker explicitly needs
  failure history.

## Inputs Incorporated

- `notes/ANDROID_VISUAL_QA_20260428_0115.md`: analysis-only comparison against
  `artifacts/mocks`. It identifies the largest remaining visual gaps as answer
  mode substitution, guide/source-like composition, emergency tablet hierarchy,
  thread tablet structure, and home/search density.
- `notes/ANDROID_GUIDE_READER_VISUAL_AUDIT_20260428.md`: analysis-only guide
  audit. It calls out phone guide header density, raw section marker leakage in
  Compose, tablet guide rail semantics, tablet paper content, and
  cross-reference rail contract drift.
- `artifacts/mocks`: visual truth for phase closure. Do not modify these files.
- Existing tracker sections were reconciled into the current phase map below;
  stale latest-proof and older-anchor claims were removed.

## Worktree And Write Rules

- Tracker-Planner may edit only this tracker.
- Do not edit Android source, generated artifacts, `artifacts/mocks`, or
  protected `notes/PLANNER_HANDOFF_*.md` files from this lane.
- Current `git status --short` during final sanity pass showed active Android
  source/resource/test changes outside this planner lane. Treat them as
  worker-owned and do not revert, reformat, or overwrite them from this lane.
- Dirty Android files observed:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
  - `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
  - `android-app/app/src/main/res/drawable/bg_detail_guide_paper_link_row.xml`
  - `android-app/app/src/main/res/drawable/bg_detail_guide_paper_panel.xml`
  - `android-app/app/src/main/res/drawable/bg_detail_guide_paper_section_label.xml`
  - `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
  - `android-app/app/src/main/res/layout-land/activity_detail.xml`
  - `android-app/app/src/main/res/layout/activity_detail.xml`
  - `android-app/app/src/main/res/layout/activity_main.xml`
  - `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
- Untracked analysis notes may also be active worker outputs. Protected
  `notes/PLANNER_HANDOFF_*.md` files remain untouched.
- Every implementation worker starts with:

```powershell
git status --short
git log --oneline -n 8 --decorate
git diff --stat
```

- If a phase needs a file currently dirty under another worker, resequence the
  phase or transfer ownership before writing. Do not open two active
  implementation slices on the same source file.

## Device Matrix And ADB Discipline

Use the fixed matrix from the local scripts:

| Role | Device | Expected screenshot | Density |
| --- | --- | --- | --- |
| `phone_portrait` | `emulator-5556` | `1080x2400` | 420 |
| `phone_landscape` | `emulator-5560` | `2400x1080` | 420 |
| `tablet_portrait` | `emulator-5554` | `1600x2560` | 320 |
| `tablet_landscape` | `emulator-5558` | `2560x1600` | 320 |

Rules:

- Use device-scoped adb commands only, for example
  `adb -s emulator-5558 ...`.
- Full closure must build/install or otherwise prove one homogeneous installed
  APK across all four roles.
- Focused runs may prove a single posture, but their handoff must name device,
  APK SHA, screenshot path, dump path, and whether the proof is diagnostic or
  acceptance evidence.

## Current Visual Defect Map

Use `artifacts/ui_state_pack/20260428_013056/screenshots` and matching
`dumps` for fresh comparisons unless a newer phase-specific proof replaces it.
The visual QA notes used earlier screenshots, so each worker must first confirm
which gaps still reproduce on the current pack.

| Family | Current priority | Remaining gap to check against mocks |
| --- | --- | --- |
| Answer | Blocking | Screens may still read as provenance/source or old detail-card states instead of compact answer article plus source cards. |
| Guide | Blocking | Phone guide header/body density, tablet guide sections rail, centered paper page, cross-reference rail count/labels, and raw marker cleanup remain priority checks. |
| Search | Blocking | Search behavior passes, but result order/content, count/filter copy, compact score markers, and tablet preview composition need mock parity. |
| Thread | Partial | Transcript is closer, but phone crowding and tablet rail/center proportions need tighter density. |
| Emergency | Partial | Phone is closer; tablet portrait still needs inline hierarchy rather than a dominant overlay-like danger treatment. No emergency landscape target exists. |
| Home | Partial | Closest family; remaining work is compact header/status/search/category/recent proportions and bottom chrome clearance. |

No screenshot item is closed by unit tests alone. Every phase handoff must cite
target mock(s), current screenshot(s), current XML dump(s), and residual
mismatches split into content/data, layout/density, and behavior.

## Phase Map

The slices below are ordered by current visual risk. Each phase owns only the
write targets listed for that phase while active. Files outside a phase's write
targets require resequencing through the owning phase.

### P0 - Planner Evidence Lock

Owner: Tracker-Planner.

Write ownership:

- `notes/ANDROID_UI_REDESIGN_PHASE_TRACKER_20260427.md`

Status:

- Current anchor and proof reconciled to `f9b6e11` and
  `artifacts/ui_state_pack/20260428_013056`.
- Android source, generated artifacts, target mocks, and protected handoffs are
  out of scope for this lane.

### P1 - Shared Detail Mode And Shell

Owner slice: detail-shell integrator.

Priority:

- First implementation slice if answer/guide screenshots still show source,
  provenance, thread, or old detail modes in place of the target surfaces.

Primary targets:

- Answer mode selection across all four answer mocks.
- Guide mode selection across all four guide mocks.
- Shared detail shell/chrome that affects answer, guide, thread, or emergency.

Write ownership:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-large/activity_detail.xml`
- `android-app/app/src/main/res/layout-large-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailSurfaceContractTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
- `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`

Proof gate:

- Fresh screenshots and dumps for representative answer, guide, thread, and
  emergency states in every posture touched by shell changes.
- Answer and guide screenshots must show the intended family surface, not a
  source/provenance substitute.

### P2 - Guide Reader Structure

Owner slice: guide reader lane.

Primary targets:

- `guide-phone-portrait.png`
- `guide-phone-landscape.png`
- `guide-tablet-portrait.png`
- `guide-tablet-landscape.png`

Remaining slices:

- Phone reader density and compact header/body.
- Shared guide section/admonition model so Compose never exposes raw
  `[[SECTION]]` markers or mojibake section text.
- Tablet guide sections rail, centered paper content, and cross-reference rail
  contract.

Write ownership:

- `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_panel.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_link_row.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_section_label.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`

Shared-lock note:

- If tablet guide rail or paper layout requires `TabletDetailScreen.kt`,
  `ThreadRail.kt`, `EvidencePane.kt`, `DetailActivity.java`, or detail layout
  XML, this phase must be assigned to the P1 owner or resequenced through P1.

Proof gate:

- All four guide mocks reviewed against current screenshots.
- Tablet landscape shows sections rail, centered paper page, and
  cross-reference rail.
- XML dumps keep guide semantics separate from answer/source semantics.

### P3 - Answer Article And Source Stack Density

Owner slice: answer/detail content lane.

Primary targets:

- `answer-phone-portrait.png`
- `answer-phone-landscape.png`
- `answer-tablet-portrait.png`
- `answer-tablet-landscape.png`

Write ownership:

- `android-app/app/src/main/java/com/senku/mobile/DetailAnswerBodyFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailAnswerPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailMetaPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailProofPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailProvenancePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailReviewedCardMetadataBridge.java`
- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailAnswerBodyFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailMetaPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProofPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProvenancePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailReviewedCardMetadataBridgeTest.java`
- `android-app/app/src/test/java/com/senku/ui/answer/AnswerContentFactoryTest.kt`
- `android-app/app/src/test/java/com/senku/ui/answer/PaperAnswerCardLabelTest.kt`

Shared-lock note:

- Escalate to P1 for `DetailActivity.java`, tablet detail shell files, composer,
  or detail layout XML.

Proof gate:

- All four answer mocks reviewed.
- Phone landscape cites the true split answer screenshot
  `rain_shelter_gd345_split_answer` or its current equivalent.
- Dumps/tests keep uncertain-fit, abstain, deterministic, reviewed-evidence,
  and strong-evidence labels distinct.

### P4 - Search Rows And Tablet Preview Parity

Owner slice: search lane.

Primary targets:

- `search-phone-portrait.png`
- `search-phone-landscape.png`
- `search-tablet-portrait.png`
- `search-tablet-landscape.png`

Write ownership:

- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

Shared-lock note:

- Escalate to P7 for `MainActivity.java`, `activity_main*`, shared home/search
  chrome, or `BottomTabBar.kt`.

Proof gate:

- All four search mocks reviewed.
- XML dumps retain query `rain shelter`, result count `4`, timing `12ms`,
  compact score/result labels, and stable linked-guide handoff behavior.

### P5 - Thread Transcript Density

Owner slice: thread/detail content lane.

Primary targets:

- `thread-phone-portrait.png`
- `thread-phone-landscape.png`
- `thread-tablet-portrait.png`
- `thread-tablet-landscape.png`

Write ownership:

- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`

Shared-lock note:

- Escalate to P1 for shared composer, `TabletDetailScreen.kt`,
  `EvidencePane.kt`, `DetailActivity.java`, or detail layout XML.

Proof gate:

- All four thread mocks reviewed.
- Handoff names composer focus/IME behavior and whether evidence/source rails
  remain visible in landscape and tablet screenshots.

### P6 - Emergency Portrait Hierarchy

Owner slice: emergency/detail content lane.

Primary targets:

- `emergency-phone-portrait.png`
- `emergency-tablet-portrait.png`

Write ownership:

- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`

Shared-lock note:

- Escalate to P1 for shared composer, `DetailActivity.java`, tablet shell files,
  or detail layout XML.

Proof gate:

- Only the two emergency mocks are reviewed.
- Confirm four ordered immediate actions, one evidence/provenance card, quiet
  composer, and no bottom collision.
- Confirm at least two negative controls stay non-emergency.
- Do not add emergency landscape acceptance.

### P7 - Home And Shared App Chrome Polish

Owner slice: shell/home integrator.

Primary targets:

- `home-phone-portrait.png`
- `home-phone-landscape.png`
- `home-tablet-portrait.png`
- `home-tablet-landscape.png`

Write ownership:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- home/search chrome drawables under `android-app/app/src/main/res/drawable/`
  with prefixes `bg_home`, `bg_manual_home`, `bg_manual_phone_home`,
  `bg_tablet_home`, and `bg_search_shell`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`

Shared-lock note:

- Search result row/card changes stay in P4. Detail/composer changes stay in
  P1.

Proof gate:

- All four home mocks reviewed.
- Verify status/search/category/recent order, bottom-nav clearance, and
  recent-row density.

### P8 - Global Mock-Parity Closure

Owner: final integration/review lane.

Write ownership:

- No standing implementation ownership. Any final fix must first claim the
  owning P1-P7 file set.

Final local validation gate:

```powershell
git status --short
git diff --check
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\final_android_ui_redesign_20260427 -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Closure criteria:

- Full pack status is `pass`.
- APK SHA is homogeneous across all four roles.
- Model name/SHA are homogeneous across all host-backed roles.
- Rotation mismatch count is 0.
- Every one of the 22 target mocks has a current screenshot review entry.
- Any accepted delta is named as content/data, layout/density, or behavior.

## Dispatch Priority

1. P0: keep tracker/evidence lock current.
2. P1: fix family mode selection and shared detail shell if current screenshots
   still show source/provenance/thread substitution.
3. P2/P3: close guide reader and answer article/source density; run in parallel
   only if neither needs the P1 shared lock.
4. P4: close search rows, data/order, and tablet preview parity.
5. P5/P6: tighten thread density and emergency portrait hierarchy.
6. P7: final home/shared chrome polish.
7. P8: full homogeneous four-role closure and 22-mock review.

## Non-Negotiables

- Do not modify `artifacts/mocks`.
- Do not edit Android code from the planner lane.
- Do not touch protected `notes/PLANNER_HANDOFF_*.md` files.
- Do not revert or clean changes owned by another worker.
- Do not broaden emergency eligibility to satisfy a screenshot.
- Do not use non-homogeneous APK evidence as closure.
- Do not call a phase complete without screenshot and XML dump proof from a
  focused local smoke or full local state pack.

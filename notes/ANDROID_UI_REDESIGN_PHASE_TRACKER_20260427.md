# Android UI Redesign Phase Tracker - 2026-04-27

Purpose: current planning map for the remaining Android target-screenshot
redesign work. This is a planner lane only. Edit this file only from the
planner role; do not edit Android source, generated artifacts, or
`notes/PLANNER_HANDOFF_*.md` files from this lane.

## Current Anchor And Evidence

- Implementation anchor for dispatch: `086f73a`
  (`advance android mock parity wave10`), already pushed.
- Current target mocks: `artifacts/mocks`, 22 PNGs total:
  - home, search, answer, guide, and thread each cover phone portrait, phone
    landscape, tablet portrait, and tablet landscape.
  - emergency covers phone portrait and tablet portrait only.
- Latest broad evidence root:
  `artifacts/ui_state_pack/20260428_0055_failed_postures_recheck`.
  - Status: `partial`, 35 states, 34 pass, 1 fail, 0 platform ANRs.
  - Covered roles: `phone_portrait`, `tablet_portrait`, `tablet_landscape`.
  - Missing from this root: `phone_landscape`.
  - Matrix APK homogeneity: `false`.
  - APKs observed: `b679c1d0e1f815f2eb1f80eb051b6ef901c6fb3793214e1c186cd17f326546aa`
    on phone/tablet portrait and
    `c4c28a03f61b9f3eb6ea902e04fd1c5b06724cde94f432444739d705286f01a2`
    on tablet landscape.
  - Model: `gemma-4-e2b-it-litert`.
  - Model SHA:
    `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
  - Only failure:
    `tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes`, smoke wrapper
    failed before trusted summary.
- Latest focused evidence root:
  `artifacts/focused_smoke_tablet_landscape_home_recheck`.
  - Run:
    `20260428_010738_778/emulator-5558`.
  - Status: `pass`, 1 screenshot, 1 dump, 0 platform ANRs.
  - Screenshot:
    `screenshots/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`.
  - Dump:
    `dumps/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`.
  - Device facts: `emulator-5558`, landscape, `2560x1600`, density `320`,
    rotation matched.
  - APK:
    `c4c28a03f61b9f3eb6ea902e04fd1c5b06724cde94f432444739d705286f01a2`.

Interpretation:

- The focused tablet-landscape home recheck repairs the failed smoke evidence
  for that state only.
- Neither evidence root is a final acceptance pack because the broad root is
  mixed-APK and missing phone landscape, while the focused root covers only one
  tablet-landscape home state.
- The next closure proof must be a four-role, homogeneous-APK state pack from
  the current implementation anchor or an explicit newer implementation commit.

## Integrated Checkpoint - 2026-04-28 01:37

Candidate implementation tree after the wave11 integration pass has a clean
local technical gate but is not final mock parity.

Validation evidence:

- Local quality gate:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1`
  passed after restoring the linked-guide search preview binding.
- Clean full state pack:
  `artifacts/ui_state_pack/20260428_013033`.
  - Status: `pass`, 47 states, 47 pass, 0 fail, 0 platform ANRs.
  - Matrix APK homogeneity: `true`.
  - APK SHA:
    `25363347fe8556cb4595daed1a556f2b86720f1c2616abdbfdf8b91327dbd358`.
  - Model: `gemma-4-e2b-it-litert`.
  - Model SHA:
    `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
  - Rotation mismatch count: 0.
- Diagnostic failed pack:
  `artifacts/ui_state_pack/20260428_011942`, 44/47.
  - Failure family:
    `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` on phone portrait,
    tablet portrait, and tablet landscape.
  - Cause found in integration review:
    `SearchResultAdapter` hid the linked-guide preview chrome instead of
    binding it after search density changes.
  - Focused rechecks after the fix passed on:
    `artifacts/focused_smoke_linked_handoff_phone_portrait_20260428_0130`,
    `artifacts/focused_smoke_linked_handoff_tablet_portrait_20260428_0130`,
    and
    `artifacts/focused_smoke_linked_handoff_tablet_landscape_20260428_0130`.

Screenshot reviewer verdicts against `artifacts/mocks` using
`artifacts/ui_state_pack/20260428_013033`:

| Family | Verdict | Blocking/remaining reason |
| --- | --- | --- |
| Home | Partial | Home chrome and density are closer but still larger than mocks; phone portrait header/status/recent-thread proportions remain off. |
| Search | Blocking | Layout passes behavior, but search structure, result order/content, tablet filter counts, and tablet preview target differ from mocks. |
| Answer | Blocking | Answer screens still read as old/detail-card or rail/provenance states rather than the compact answer article plus source cards. |
| Thread/follow-up | Partial | Compact transcript work landed, but phone portrait overflows/crowds and tablet rails are still too spacious versus the thread mocks. |
| Guide | Blocking | Guide screens still show cross-reference/source-like states with oversized prose instead of compact manual pages with section rails and related rows. |
| Emergency | Partial | Core danger content is present; tablet portrait still uses an overlay-like composition rather than the mock's full-page inline hierarchy. |

Next wave priority from screenshot review:

1. Answer/guide mode composition and state selection, especially phone answer
   and all guide readers.
2. Search mock data/order and tablet filter/preview parity.
3. Thread density/composer clearance after answer/detail shell decisions.
4. Emergency tablet portrait hierarchy after shared detail shell stabilizes.

## Worktree Guard

Other workers have active Android source/test changes. Do not revert,
reformat, or overwrite them from this planner lane.

Dirty Android files observed during this planning refresh:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
- `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`

Protected untracked planner handoffs are present under
`notes/PLANNER_HANDOFF_*.md`. Leave them untouched and untracked.
Other untracked notes may be active worker outputs; leave unrelated notes
untouched from this lane.

Each implementation worker starts with:

```powershell
git status --short
git log --oneline -n 8 --decorate
git diff --stat
```

If a slice needs a dirty file owned by another worker, resequence the slice or
ask the owner to take it. Do not open a second parallel implementation on the
same source file.

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
- Do not use bare `adb` when more than one emulator may be running.
- Do not call a pack acceptance-ready unless every role reports the same APK
  SHA and the expected model identity.
- Focused runs may prove a single posture, but their handoff must name the
  device, APK SHA, screenshot path, dump path, and whether the proof is
  acceptance evidence or only diagnostic evidence.
- `-SkipInstall` is acceptable for fast diagnosis only when the handoff records
  the installed APK SHA. Full closure must build/install or otherwise prove a
  homogeneous installed APK across all four roles.

## Screenshot Review Checklist

| Review item | Current status | Evidence read | Next screenshot gate |
| --- | --- | --- | --- |
| Tablet landscape shell/chrome | Smoke-fixed, visual open | Broad root failed `tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes`; focused root passed the same state on `emulator-5558` with APK `c4c28...`. | Compare `artifacts/mocks/home-tablet-landscape.png` to the focused home screenshot, then re-run tablet landscape in a homogeneous pack after any shell/chrome edits. |
| Answer/detail density | Open | Phone portrait, tablet portrait, and tablet landscape answer states pass in the broad root; phone landscape is absent from that root. | Compare all four answer mocks to fresh screenshots, including phone landscape true split answer; verify density/source labels without guide/source substitution. |
| Guide detail structure | Open | Guide related/off-rail/source-context states pass in covered roles, but the evidence is mixed-APK and not final visual parity. | Compare all four guide mocks; tablet landscape must show sections rail, centered paper page, and cross-reference rail while preserving guide semantics in XML. |
| Search drift | Watch | Search states pass in phone portrait, tablet portrait, and tablet landscape; phone landscape is absent. | Compare all four search mocks; dumps must keep query `rain shelter`, count `4`, timing `12ms`, compact score markers, and stable tablet preview behavior. |
| Phone home polish | Open | Phone portrait home passes in the broad root. | Compare `artifacts/mocks/home-phone-portrait.png`; verify status/search/category/recent order, bottom-nav clearance, and recent-row density. |
| Emergency bottom collision | Watch | Phone and tablet portrait emergency states pass in the broad root. | Compare the two emergency mocks under a homogeneous APK; verify the composer/evidence area does not collide with immediate actions. No emergency landscape target should be added. |

No screenshot item is closed by unit tests alone. Every slice handoff must cite
the target mock(s), current screenshot(s), current XML dump(s), and residual
mismatches split into content/data, layout/density, and behavior.

## Parallel Phase Map

The slices below are designed to be disjoint by file ownership. If a worker
needs a file outside the listed lock set, the slice must be resequenced through
the shared-shell or shared-detail owner.

### P0 - Coordination And Evidence Lock

Owner: planner/orchestrator.

Scope:

- This note, dispatch handoffs, and screenshot-review status only.
- No Android source edits.

Gate:

- `git status --short` reviewed.
- Dirty file owners named before dispatch.
- Anchor and evidence roots named exactly:
  `086f73a`,
  `artifacts/ui_state_pack/20260428_0055_failed_postures_recheck`,
  `artifacts/focused_smoke_tablet_landscape_home_recheck`.

Parallel status:

- Can run beside implementation work because it is notes-only.

### P1 - Shared App Shell And Home Chrome

Owner slice: shell/home integrator.

Primary targets:

- `home-phone-portrait.png`
- `home-phone-landscape.png`
- `home-tablet-portrait.png`
- `home-tablet-landscape.png`
- Tablet-landscape shell/chrome checklist item.
- Phone home polish checklist item.

File lock:

- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`, if needed.
- Home Compose/UI primitives touched for shell layout.
- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`, if
  shared bottom chrome changes are required.

Do not run beside:

- Another home/shell slice.
- Search work that also needs `MainActivity.java` or `activity_main*`.

Local validation gate:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.MainActivityHomeChromeTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_shell_home -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Screenshot gate:

- Review all four home mocks.
- Include the focused tablet-landscape home recheck path if no newer focused
  screenshot exists.
- Closure requires a homogeneous-APK four-role pack, not the mixed-APK broad
  root.

### P2 - Search Rows And Tablet Preview Drift

Owner slice: search lane.

Primary targets:

- `search-phone-portrait.png`
- `search-phone-landscape.png`
- `search-tablet-portrait.png`
- `search-tablet-landscape.png`
- Search drift checklist item.

File lock:

- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- Search-specific tests.

Escalate to P1 if the change needs `MainActivity.java`, `activity_main*`, or
shared bottom chrome.

Local validation gate:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.SearchResultAdapterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_search_drift -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Screenshot gate:

- Review all four search mocks.
- XML dumps must retain query `rain shelter`, result count `4`, timing `12ms`,
  and compact score/result labels.

Parallel status:

- Can run beside P3/P4/P5 only if it stays inside the search file lock.

### P3 - Shared Detail Shell Foundation

Owner slice: detail-shell integrator.

Primary targets:

- Shared answer/guide/thread/emergency detail chrome.
- Tablet detail density and rail allocation.
- Emergency bottom-collision guard, when the fix requires shell/composer layout.

File lock:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`, if
  shared composer placement changes are required.

Do not run beside:

- P4, P5, P6, or P7 if those slices need any file in this lock set.

Local validation gate:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailSurfaceContractTest" --tests "com.senku.mobile.DetailFollowupLandscapeComposerTest" --tests "com.senku.ui.tablet.StressReadingPolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_detail_shell -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Screenshot gate:

- Review one representative answer, guide, thread, and emergency screenshot for
  every posture touched by the shared shell change.
- Any shared shell change resets the screenshot-review status for the affected
  family until a fresh screenshot is cited.

### P4 - Guide Reader Structure

Owner slice: guide/detail content lane.

Primary targets:

- `guide-phone-portrait.png`
- `guide-phone-landscape.png`
- `guide-tablet-portrait.png`
- `guide-tablet-landscape.png`
- Guide detail structure checklist item.

File lock:

- `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
- Guide-specific drawables or Compose files, if created.
- Guide-specific tests.

Escalate to P3 if the change needs `DetailActivity.java`, tablet detail shell,
composer, or shared detail layouts.

Local validation gate:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailGuidePresentationFormatterTest" --tests "com.senku.mobile.DetailGuideContextPresentationFormatterTest" --tests "com.senku.mobile.DetailRelatedGuidePresentationFormatterTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_guide_reader -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Screenshot gate:

- Review all four guide mocks.
- Tablet landscape must show sections rail, centered paper page, and
  cross-reference rail.
- XML dumps must keep guide semantics separate from answer/source semantics.

Parallel status:

- Can run beside P2 and P5/P6/P7 only if it stays inside the guide file lock.

### P5 - Answer Detail Density And Sources

Owner slice: answer/detail content lane.

Primary targets:

- `answer-phone-portrait.png`
- `answer-phone-landscape.png`
- `answer-tablet-portrait.png`
- `answer-tablet-landscape.png`
- Answer/detail density checklist item.

File lock:

- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- Answer/source/provenance formatters and answer-specific tests.

Coordinate with P3 if the change needs `TabletDetailScreen.kt`,
`EvidencePane.kt`, shared composer, `DetailActivity.java`, or detail layouts.

Local validation gate:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailAnswerBodyFormatterTest" --tests "com.senku.mobile.DetailMetaPresentationFormatterTest" --tests "com.senku.mobile.DetailProofPresentationFormatterTest" --tests "com.senku.mobile.DetailProvenancePresentationFormatterTest" --tests "com.senku.ui.answer.AnswerContentFactoryTest" --tests "com.senku.ui.answer.PaperAnswerCardLabelTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_answer_density -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Screenshot gate:

- Review all four answer mocks.
- Phone landscape must cite a true split answer screenshot for
  `rain_shelter_gd345_split_answer` or its current equivalent.
- Dumps/tests must keep uncertain-fit, abstain, deterministic,
  reviewed-evidence, and strong-evidence labels distinct.

Parallel status:

- Can run beside P2 and P4/P6/P7 only if it stays inside the answer file lock.

### P6 - Thread Transcript Density

Owner slice: thread/detail content lane.

Primary targets:

- `thread-phone-portrait.png`
- `thread-phone-landscape.png`
- `thread-tablet-portrait.png`
- `thread-tablet-landscape.png`

File lock:

- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- Thread/transcript-specific tests.

Coordinate with P3 if the change needs shared composer, `TabletDetailScreen.kt`,
`EvidencePane.kt`, `DetailActivity.java`, or detail layouts.

Local validation gate:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailThreadHistoryRendererTest" --tests "com.senku.ui.tablet.StressReadingPolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_thread_density -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Screenshot gate:

- Review all four thread mocks.
- Handoff must mention composer focus/IME behavior and whether evidence/source
  rails remain visible in landscape/tablet screenshots.

Parallel status:

- Can run beside P2, P4, P5, and P7 only if it stays inside the thread file
  lock.

### P7 - Emergency Portrait Collision And Hierarchy

Owner slice: emergency/detail content lane.

Primary targets:

- `emergency-phone-portrait.png`
- `emergency-tablet-portrait.png`
- Emergency bottom-collision checklist item.

File lock:

- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- Emergency-specific drawables and tests.

Escalate to P3 if the collision fix needs shared composer, `DetailActivity.java`,
tablet shell files, or detail layouts.

Local validation gate:

```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.senku.mobile.DetailActionBlockPresentationFormatterTest" --tests "com.senku.mobile.DetailWarningCopySanitizerTest" --tests "com.senku.mobile.EmergencySurfacePolicyTest" --console=plain
.\gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\wip_emergency_collision -RoleFilter phone_portrait,tablet_portrait -MaxParallelDevices 2
```

Screenshot gate:

- Review only the two emergency mocks.
- Confirm four ordered immediate actions, one evidence/provenance card, quiet
  composer, and no bottom collision.
- Confirm at least two negative controls stay non-emergency.
- Do not add emergency landscape acceptance.

Parallel status:

- Can run beside P2, P4, P5, and P6 only if it stays inside the emergency file
  lock.

### P8 - Global Mock-Parity Closure

Owner: final integration/review lane.

Scope:

- No broad redesign starts here.
- Only integration fixes required by screenshot review.

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

Optional clean-headless confirmation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_headless_state_pack_lane.ps1 -OutputRoot artifacts\ui_state_pack\final_android_ui_redesign_headless_20260427 -LaunchProfile clean-headless -RealRun
```

Closure criteria:

- Full pack status is `pass`.
- APK SHA is homogeneous across all four roles.
- Model name/SHA are homogeneous across all host-backed roles.
- Rotation mismatch count is 0.
- Every one of the 22 target mocks has a current screenshot review entry.
- Any accepted delta is named as content/data, layout/density, or behavior.

## Parallel Dispatch Summary

Safe parallel cohort after dirty-file ownership is clear:

- P1 shell/home can run alone for `activity_main*` and shared home chrome.
- P2 search can run in parallel if it stays in search adapter/card files.
- P4 guide, P5 answer, P6 thread, and P7 emergency can run in parallel only
  when each stays inside its content lock set.
- P3 shared detail shell is the serialization point for `DetailActivity`,
  `activity_detail*`, `TabletDetailScreen`, `EvidencePane`, and shared composer
  placement.

Recommended order:

1. P0: confirm owners and evidence lock.
2. P1: close tablet-landscape shell/home and phone home polish.
3. P2: close search drift.
4. P3: run only if any detail family needs shared shell/layout movement.
5. P4/P5/P6/P7: run content lanes in parallel when their file locks are clean.
6. P8: full homogeneous four-role mock-parity closure.

## Non-Negotiables

- Do not modify `artifacts/mocks`.
- Do not edit Android code from the planner lane.
- Do not touch protected `notes/PLANNER_HANDOFF_*.md` files.
- Do not revert or clean changes owned by another worker.
- Do not broaden emergency eligibility to satisfy a screenshot.
- Do not use mixed-APK evidence as closure.
- Do not call a phase complete without screenshot and XML dump proof from a
  focused local smoke or full local state pack.

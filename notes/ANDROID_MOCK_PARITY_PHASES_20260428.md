# Android Mock Parity Phase Map - 2026-04-28

Planner lane only. This note tracks the remaining target-mock parity work from
`artifacts/mocks` and the latest reviewer findings. Do not edit Android source
from this lane.

## Evidence Anchor

- Target mocks: `artifacts/mocks`, 22 PNGs.
  - Home, Search, Answer, Guide, Thread: phone portrait, phone landscape,
    tablet portrait, tablet landscape.
  - Emergency: phone portrait and tablet portrait only.
- Prior visual QA: `notes/ANDROID_VISUAL_QA_20260428_0315.md`.
- Latest implementation anchor: `fbf69e0`
  (`advance android mock parity wave17`).
- Latest visual proof: `artifacts/ui_state_pack/20260428_035816`.
  - Status `pass`, 47/47 states, homogeneous APK
    `b12344900d12a08a6a37a3a12b4e530ead6359e807e95683dadbafed014a8c81`,
    model `gemma-4-e2b-it-litert`, rotation mismatch count 0.
  - Use this pack for latest screenshots/dumps before assigning the next
    visual slice.
- Latest reviewer note: `notes/ANDROID_VISUAL_QA_20260428_0408.md`.

Current reviewer blockers:

- Answer detail still falls into guide/manual or answer-card presentation rather
  than target answer hierarchy.
- Thread transcript is not target-shaped and still inherits answer-detail card
  behavior.
- Guide reader still does not match compact guide mocks.
- Search and Home are improved but remain density/chrome polish.

Emergency tablet core content is close after wave17, but phone/tablet chrome
polish remains open.

## Phase Map

### P0 - Evidence And Dispatch Lock

Status: open for tracker updates only.

Acceptance:

- This file names the active proof pack, fallback pack, target mocks, and
  current blockers.
- No code, generated artifacts, target mocks, or protected handoff notes are
  edited from this lane.

Write set:

- `notes/ANDROID_MOCK_PARITY_PHASES_20260428.md`

### P1 - Tablet Detail Ownership

Goal: put tablet-only source and emergency ownership in the shared detail shell
instead of answer/emergency content formatters.

Blockers covered:

- Tablet answer source rail escapes answer ownership.
- Tablet emergency needs a full-height owned hierarchy, not an overlay over
  stale thread/source rails.

Primary acceptance:

- `answer-tablet-portrait.png` and `answer-tablet-landscape.png`: answer
  surface keeps the target tablet structure: left thread/source support, center
  article, bounded right source cards. Source selection must not turn the
  answer into a guide/cross-reference station unless the user navigated to a
  guide route.
- `emergency-tablet-portrait.png`: emergency is the tablet surface owner from
  the header through the bottom context/composer. No stale thread rail, source
  graph, or normal answer shell remains visible behind it.
- Phone answer/thread/emergency screenshots remain unaffected except for shared
  composer clearance.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-large/activity_detail.xml`
- `android-app/app/src/main/res/layout-large-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`

Do not also assign these files to P2-P4 while P1 is active.

### P2 - Thread Transcript Shape

Goal: make thread screens read like transcript surfaces, not answer-detail
cards with thread history attached.

Primary acceptance:

- `thread-phone-portrait.png`: Q1/A1 and Q2/A2 appear as transcript entries.
  The current `Field entry - Moderate evidence` answer card treatment is gone.
  Source chips and composer do not overlap the transcript.
- `thread-phone-landscape.png`: Q2/A2 is the primary landscape content with the
  compact right source rail; no provenance card replaces the transcript.
- `thread-tablet-portrait.png` and `thread-tablet-landscape.png`: transcript is
  primary, with Q1/A1 and Q2/A2 rhythm matching the mocks. The tablet title and
  rails support the thread instead of presenting `Abrasives Manufacturing - 2
  turns` as an answer detail.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/ThreadRailPolicyTest.kt`

Escalate to P1, do not self-claim, if the fix needs `DetailActivity.java`,
`TabletDetailScreen.kt`, `EvidencePane.kt`, detail XML, or composer geometry.

### P3 - Emergency Content Polish

Goal: after P1 owns the tablet emergency surface, align the emergency copy,
proof card, and warning treatments with the two emergency mocks.

Primary acceptance:

- `emergency-phone-portrait.png`: danger header, summary sentence, four
  immediate actions, `WHY THIS ANSWER`, single GD-132 anchor card, and quiet
  emergency composer match the mock. No `Route, backend & proof` label remains.
- `emergency-tablet-portrait.png`: same content contract in full tablet width,
  with large empty lower field and no normal answer source graph.
- No emergency landscape target is introduced.
- Negative controls remain non-emergency.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`

P3 may run only after P1 if tablet mounting or composer behavior is still
changing.

### P4 - Search Polish

Goal: close the remaining search visual deltas without reopening search shell
architecture.

Current latest evidence:

- `20260428_033240` shows the target result IDs/order in dumps:
  GD-023, GD-027, GD-345, GD-294.
- Remaining polish: literal `[ ]` filter text instead of checkbox controls,
  incomplete filter counts/window options, row/score density, and preview prose
  matching.

Primary acceptance:

- `search-phone-portrait.png` and `search-phone-landscape.png`: compact search
  header, four-result list, scores, metadata, snippets, linked-guide rows, and
  bottom/nav clearance match the mocks.
- `search-tablet-portrait.png`: left filter rail uses real checkbox affordances
  with counts; results column keeps the target row density; no landscape
  preview rail appears.
- `search-tablet-landscape.png`: filter rail, centered result list, and GD-023
  preview rail match target proportions and copy, including window options.
- Browse linked-guide handoff still opens the expected linked guide.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

Do not mix P4 with P1-P3 in one worker because `MainActivity` and detail-shell
work need separate review and proof.

## Acceptance Matrix

| Screen | Phone portrait | Phone landscape | Tablet portrait | Tablet landscape |
| --- | --- | --- | --- | --- |
| Answer | Article-first answer, no bottom collision. | Split answer article visible, no source/provenance clipping. | Source rail bounded by tablet answer surface; not guide station. | Source cards match answer mock; selection does not escape answer ownership. |
| Thread | Q1/A1 + Q2/A2 transcript, no answer card mode. | Q2/A2 primary with compact sources; no provenance replacement. | Two-turn transcript primary; no answer-detail title/card hierarchy. | Same transcript hierarchy with right sources only as support. |
| Emergency | Danger surface, four actions, `WHY THIS ANSWER`, quiet composer. | No target; do not accept/reject landscape. | Full-height tablet emergency owner; no stale rails behind it. | No target; do not accept/reject landscape. |
| Search | GD-023/GD-027/GD-345/GD-294 list, compact rows and nav clearance. | Compact landscape list/header and scores. | Checkbox filter rail with counts, result density, no preview rail. | Checkbox filter rail, result list, GD-023 preview, target copy/proportions. |

## Delegation Rules

- Start every worker with `git status --short`; the worktree was dirty in all
  active Android lanes when this tracker was created.
- One active worker per phase write set. If a live dirty file appears in another
  phase's write set, resequence instead of sharing it.
- P1 must lead when a fix changes tablet ownership, detail layouts, shared
  composer, or evidence/source rails.
- P2 may run in parallel with P4 only if neither worker touches files outside
  their disjoint write set.
- P3 should follow P1 unless the work is purely formatter/drawable copy.
- P4 can run independently after claiming `MainActivity.java` and search layout
  ownership.

## Validation Gates

Local preflight for any implementation worker:

```powershell
git status --short
git diff --check
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --console=plain
cd ..
```

Focused visual proof:

- P1: tablet portrait/landscape answer-source states, tablet portrait emergency,
  plus one phone detail sanity screenshot.
- P2: all four thread screenshots/dumps.
- P3: emergency phone portrait and tablet portrait screenshots/dumps, plus at
  least two non-emergency negative controls.
- P4: all four search result screenshots/dumps and linked-guide handoff proof.

Closure pack:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\<closure_pack> -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

Final closure requires:

- Full pack status `pass`, 47/47 states.
- Homogeneous APK SHA across all four roles.
- Homogeneous model name/SHA for host-backed roles.
- Rotation mismatch count 0.
- Screenshot plus XML dump review against every relevant mock in the acceptance
  matrix.
- `20260428_035816` is the current clean technical proof; final visual closure
  still requires screenshot review against the target mocks.

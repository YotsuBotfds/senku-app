# Android Mock Parity 10/10 Plan - 2026-04-29

Planner lane only. This note slices the remaining Senku Android mock-parity
work for up to five implementation workers while the parent integrates. Do not
edit Android source from this lane.

## 2026-04-29 Deep-Review Rebaseline

Current pushed head after the deep-review hardening wave:

- `6cd0f2d fix tablet ask button submit target`
- Prior hardening commits in this run:
  - `69422d9 harden shared ask submit routing`
  - `1e0f0e3 improve shared search input accessibility labels`
  - `452434c hide legacy body mirror in answer mode`
  - `23a4721 cover restored ask tab ownership`
  - `260eb3f default tablet answers to answer-first layout`

Acceptance proof:

- State pack:
  `artifacts/ui_state_pack/deep_review_hardening/20260429_144709/summary.json`
  reports `status=pass`, `22/22`, `fail_count=0`, `platform_anr_count=0`,
  `matrix_homogeneous=true`, `rotation_mismatch_count=0`.
- APK SHA:
  `c0975f08ffb7de6bcc98b0756aed5a608c50bf79403bef70984f56d4385542e7`.
- Host model:
  `gemma-4-e2b-it-litert`,
  SHA `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
- Visual diff:
  `artifacts/ui_state_pack/deep_review_hardening/20260429_144709/visual_diff/mock_parity_visual_diff.md`
  validated with `validate_android_mock_visual_diff_report.py`.

Fresh worst visual-drift order:

- `guide-tablet-portrait.png` MAE `51.74`.
- `guide-phone-portrait.png` MAE `33.48`.
- `guide-phone-landscape.png` MAE `24.89`.
- `answer-phone-portrait.png` MAE `23.71`.
- `answer-phone-landscape.png` MAE `21.14`.
- `guide-tablet-landscape.png` MAE `20.15`.
- `emergency-phone-portrait.png` MAE `16.87`.
- `thread-phone-landscape.png` MAE `15.67`.

Deep-review items already closed:

- Shared Ask/Search submit routing centralized for IME and hardware Enter.
- Explicit Search and Ask button targets added so tablet Ask button no longer
  falls through to search ownership.
- Shared search input labels moved to string resources with `labelFor` on the
  existing icon labels where available, without visible mock drift.
- Hidden answer-body mirror is now `GONE` in answer mode instead of being kept
  alive at near-zero size.
- Restored phone tab ownership is covered by unit tests.
- Tablet answer mode now defaults to answer-first evidence-pane collapse unless
  evidence is explicitly expanded.

Fresh worker batch from this baseline:

- Guide Reader Structure owns guide presentation formatters, guide paper
  drawables, and guide formatter tests.
- Phone Detail Shell / Composer owns `DetailActivity`, phone detail XML, phone
  composer/danger shell resources, and phone detail tests.
- Tablet Detail Compose owns `TabletDetailScreen.kt`, evidence models, and
  tablet/evidence tests.
- Search Results Polish owns result adapters/cards/layouts and search tests.
- Home Shell / Nav Chrome owns main XML variants, home/nav/search drawables,
  category/bottom-tab Compose primitives, and home chrome tests.

Parent integration rule for this batch: land one worker slice at a time, run
its focused tests, then run a role-focused state-pack proof before the next
commit when the changed surface is high-risk. The final batch proof is the full
four-role state pack plus mock visual diff.

## Evidence Anchors

- Target mocks: `artifacts/mocks`, 22 canonical PNGs.
- Current best proof:
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538`.
  Summary is `pass`, `22/22`, homogeneous APK
  `aa30ddeecae6b62696ad4896f51ecc6daf7cef73e9e67cb228fd60362967d793`,
  model `gemma-4-e2b-it-litert`, rotation mismatch `0`.
- Previous comparison proof:
  `artifacts/ui_state_pack/wave68_integrated_final/20260429_075219`.
- External direction:
  `codex_screenshot_alignment_notes.md`.
- Existing tracker:
  `notes/ANDROID_MOCK_PARITY_PHASES_20260428.md`.

Observed Wave69 deltas against targets:

- `answer-phone-landscape.png` starts at the answer body/callout rather than
  the question/meta scaffold.
- `thread-phone-landscape.png` starts at Q2 and misses the full Q1/Q2 first
  viewport flow requested for final parity.
- `emergency-phone-portrait.png` and `emergency-tablet-portrait.png` keep the
  right data, but the banner/proof hierarchy is crowded and the why card is too
  visually dominant.
- `guide-tablet-portrait.png` and `guide-tablet-landscape.png` now have the app
  rail and denser rows, but paper/rail allocation and type scale still drift
  from the mock.
- Home/search are close enough to treat as final polish after detail screens
  stop moving.

## Coordination Rules

- Every worker starts with `git status --short`, `git log --oneline -n 8
  --decorate`, and `git diff --stat`.
- Do not revert or normalize unrelated dirty files. If a file in a worker's
  write set is already dirty, the worker must report the conflict and either
  rebase their scope or let the parent sequence it.
- One owner per write set. `DetailActivity.java`, `TabletDetailScreen.kt`, and
  `MainActivity.java` are integration-hot files and should not be shared inside
  the same batch.
- Each worker returns: changed files, focused tests, proof pack path, exact
  target/current/new screenshot paths reviewed, and dump assertions.
- The parent owns the final full pack and tracker update.

## Phase 0 - Parent Snapshot

Parent only, no Android edits.

Acceptance:

- Confirm the current comparison set has exactly the 22 target files in
  `artifacts/mocks` and the 22 Wave69 files in
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks`.
- Record any live dirty-file conflicts before assigning workers.
- Use Wave69 dumps under
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/dumps` as
  the text contract baseline.

## Phase 1 - Parallel Worker Batch

### Worker 1 - Phone Landscape Answer/Thread Viewport

Purpose: fix the two final phone-landscape detail blockers without changing
tablet layout policy or emergency formatting.

Likely files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailTranscriptFormatter.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailTranscriptFormatterTest.java`

Do not touch:

- `TabletDetailScreen.kt`
- `MainActivity.java`
- Emergency policy/formatter files unless the parent sequences this worker
  after Worker 3.

Acceptance:

- Target:
  `artifacts/mocks/answer-phone-landscape.png`.
  Baseline:
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/answer-phone-landscape.png`.
  New proof:
  `<proof>/mocks/answer-phone-landscape.png`.
  The first viewport shows the answer meta row, question, `GD-345` submeta, and
  the start of the body before the callout; it must not start at mid-body.
- Dump:
  `<proof>/dumps/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`
  has visible nodes for `ANSWER`, `THIS DEVICE`, `1 TURN`, the rain-shelter
  question, and `GD-345`.
- Target:
  `artifacts/mocks/thread-phone-landscape.png` plus conversation target intent
  for the full Q1/Q2 flow.
  Baseline:
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/thread-phone-landscape.png`.
  New proof:
  `<proof>/mocks/thread-phone-landscape.png`.
  The thread first viewport starts with the requested chronological context,
  not only Q2; Q1/A1 and Q2/A2 remain ordered, with the composer still visible.
- Dump:
  `<proof>/dumps/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`
  exposes Q1, A1, Q2, and A2 in order and keeps `THREAD GD-220` ownership.

### Worker 2 - Tablet Guide Proportions

Purpose: bring guide tablet portrait/landscape to target paper, rail, and type
scale without touching phone detail or home/search.

Likely files:

- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_panel.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_link_row.xml`
- `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
- `android-app/app/src/test/java/com/senku/ui/tablet/ThreadRailPolicyTest.kt`

Do not touch:

- `DetailActivity.java`
- `MainActivity.java`

Acceptance:

- Target/baseline/new:
  `artifacts/mocks/guide-tablet-portrait.png`,
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/guide-tablet-portrait.png`,
  `<proof>/mocks/guide-tablet-portrait.png`.
  The paper is centered with target-like width, the title is not clipped or
  oversized, section rail rows match the 7 visible mock labels, and required
  reading rows fit without crowding.
- Target/baseline/new:
  `artifacts/mocks/guide-tablet-landscape.png`,
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/guide-tablet-landscape.png`,
  `<proof>/mocks/guide-tablet-landscape.png`.
  The app rail, section rail, paper, and cross-reference rail match the mock
  balance; paper type is materially smaller than Wave69 and reference cards no
  longer dominate.
- Dumps:
  `<proof>/dumps/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
  and
  `<proof>/dumps/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
  preserve `SECTIONS`, `17`, `FIELD MANUAL`, `GD-132`, and `OPENED FROM GD-220`.

### Worker 3 - Emergency Hierarchy

Purpose: make emergency phone/tablet match the target warning hierarchy and why
card density without changing the general answer/thread path.

Likely files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailProofPresentationFormatter.java`
- `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProofPresentationFormatterTest.java`

Do not touch:

- `DetailActivity.java` unless Worker 1 has landed or the parent grants the
  file.
- `TabletDetailScreen.kt`
- Home/search files.

Acceptance:

- Target/baseline/new:
  `artifacts/mocks/emergency-phone-portrait.png`,
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/emergency-phone-portrait.png`,
  `<proof>/mocks/emergency-phone-portrait.png`.
  Banner, four immediate actions, why card, and composer all fit the first
  viewport with target hierarchy; the why card is compact, not a large crowded
  block.
- Target/baseline/new:
  `artifacts/mocks/emergency-tablet-portrait.png`,
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/emergency-tablet-portrait.png`,
  `<proof>/mocks/emergency-tablet-portrait.png`.
  Tablet emergency uses the compact mock header/action/why-card rhythm, not the
  large `Back`/`Home` header treatment seen in Wave69.
- Dumps:
  `<proof>/dumps/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.xml`
  and
  `<proof>/dumps/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.xml`
  preserve the four action titles, `minimum 5 m from active work zone`,
  `GD-132`, `ANCHOR`, and `93%`.

### Worker 4 - Detail Source/Related Card Polish

Purpose: finish answer source cards and related-guide rows as shared detail
primitives while avoiding layout ownership.

Likely files:

- `android-app/app/src/main/java/com/senku/mobile/DetailSourcePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/ui/sources/SourceRow.kt`
- `android-app/app/src/main/res/drawable/bg_detail_source_card_flat.xml`
- `android-app/app/src/main/res/drawable/bg_detail_sources_shell_flat.xml`
- `android-app/app/src/main/res/drawable/ic_detail_action_chevron.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailSourcePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`
- `android-app/app/src/androidTest/java/com/senku/ui/sources/SourceRowTest.kt`

Do not touch:

- `DetailActivity.java`
- `TabletDetailScreen.kt`
- Emergency action formatter/policy.

Acceptance:

- Target/baseline/new:
  `artifacts/mocks/answer-phone-portrait.png`,
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/answer-phone-portrait.png`,
  `<proof>/mocks/answer-phone-portrait.png`.
  Source cards look like target cards with distinct meta/title/quote hierarchy,
  `Tap to expand` where appropriate, and related guide rows are divider-like
  with the target four IDs.
- Target/baseline/new:
  `artifacts/mocks/answer-tablet-portrait.png`,
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks/answer-tablet-portrait.png`,
  `<proof>/mocks/answer-tablet-portrait.png`.
  Source rail/card density matches the mock without changing tablet lane widths.
- Dumps preserve `GD-220`, `GD-132`, `GD-345`, `SOURCES`, `RELATED GUIDES`,
  and no collapsed generic `Show`/`Hide` control appears in the first viewport.

### Worker 5 - Home/Search Final Polish

Purpose: close the low-risk home/search deltas after detail ownership is stable.

Likely files:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/mobile/HomeGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
- `android-app/app/src/main/java/com/senku/ui/home/IdentityStrip.kt`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

Do not touch:

- Detail activity/layout files.
- Tablet detail files.

Acceptance:

- New proof screenshots for all eight home/search targets match:
  `home-phone-portrait.png`, `home-phone-landscape.png`,
  `home-tablet-portrait.png`, `home-tablet-landscape.png`,
  `search-phone-portrait.png`, `search-phone-landscape.png`,
  `search-tablet-portrait.png`, and `search-tablet-landscape.png`.
- Baseline paths are the same filenames under
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538/mocks`.
- Search keeps `rain shelter`, `4 RESULTS`, the target result order
  `GD-023`, `GD-027`, `GD-345`, `GD-294`, and tablet filter/preview panes.
- Home keeps the target pack row, six category cards, and three recent threads
  without adding duplicate nav treatments.

## Phase 2 - Parent Integration

Parent integrates workers in this order unless dirty-file reality says
otherwise:

1. Worker 1, because it owns `DetailActivity.java`.
2. Worker 4, because source/related polish depends on the detail shell staying
   stable.
3. Worker 3, unless it needed `DetailActivity.java`, in which case merge it
   immediately after Worker 1.
4. Worker 2, because it owns `TabletDetailScreen.kt`.
5. Worker 5, last, to avoid chrome/layout churn while detail screenshots still
   move.

Integration acceptance:

- Run focused JVM tests named by each worker plus `git diff --check`.
- Build a full four-role pack:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\<integration_pack> -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4`
- The pack must be `pass`, `22/22`, homogeneous APK/model, `0` ANRs, and `0`
  rotation mismatches.
- Visual review every new `<integration_pack>/mocks/*.png` against
  `artifacts/mocks/*.png`.

## Phase 3 - Final Micro-Slice

Only start this after Phase 2 proves green. The parent assigns one narrow
worker or does it directly.

Likely residuals:

- Compact topbar/header type that still looks too tall in detail screens.
- Remaining tablet guide title/body scale if Worker 2 lands close but not exact.
- Emergency card/bottom composer spacing if the first viewport still feels
  crowded.
- Home/search typography if the detailed surfaces are already 10/10.

Acceptance:

- No new behavior or fixture changes unless visual QA names a specific target
  mismatch.
- Each micro-fix cites the exact target, baseline integration screenshot, new
  screenshot, and dump path.

## Phase 4 - Closure

Final closure criteria:

- Full local quality gate passes:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1`
- Final full proof pack is `pass`, `22/22`, homogeneous APK/model, `0` ANRs,
  and `0` rotation mismatches.
- `goal_mock_pack.zip_path` contains exactly the 22 canonical PNGs.
- No live timestamp/chrome leakage; deterministic frame remains `4:21` /
  `OFFLINE`.
- No mode leakage:
  answer owns answer/source cards, thread owns chronological turns, guide owns
  section/cross-reference rails, emergency owns immediate actions and compact
  why card, search owns filter/preview panes.
- Update `notes/ANDROID_MOCK_PARITY_PHASES_20260428.md` only after closure, or
  append a short pointer there to this plan if the parent wants to preserve the
  older phase map.

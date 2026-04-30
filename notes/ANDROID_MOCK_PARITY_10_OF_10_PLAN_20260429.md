# Android Mock Parity 10/10 Plan - 2026-04-29

Planner lane only. This note slices the remaining Senku Android mock-parity
work for up to five implementation workers while the parent integrates. Do not
edit Android source from this lane.

## 2026-04-30 03:32 Current-Head Rebaseline

Current pushed head for this tracker:

- `6aaf2e9` - Add Saved navigation harness proof.
- Commits integrated since the 2026-04-29 source/emergency/Ask wave include:
  `7e61fd6`, `96eee1a`, `12aa170`, `9f3a7f8`, `bd2b8db`,
  `38cc3c5`, `7ca3717`, `6df609b`, `c653297`, `b582b22`,
  `99b3c89`, `d2b8403`, `e18665e`, `986350f`, `1c3fcc9`,
  `d8ae733`, `ae00092`, `e2d3092`, `7e49e6c`, `204d907`,
  `3838421`, `7fb42cc`, `07fa0d8`, and `6aaf2e9`.

Latest proof artifact:

- `artifacts/ui_state_pack/post_push_0323_visual_proof/20260430_033244`.
- Summary reports `total_states=12`, `pass_count=12`, `fail_count=0`,
  `platform_anr_count=0`, `matrix_homogeneous=true`, and
  `rotation_mismatch_count=0`.
- Matrix APK SHA:
  `6e1380d0510e03fda7083b3b36d3a488786f31e520f9ef52783c8d505ebcdee2`.
- Host model remains `gemma-4-e2b-it-litert`, SHA
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
- Important status interpretation: the pack status is `fail` only because this
  is a filtered portrait proof with 12/22 canonical PNGs. The captured states
  passed; full normalized review and mock zip were skipped because the 10
  landscape targets were not present.
- Human review aid:
  `artifacts/ui_state_pack/post_push_0323_visual_proof/20260430_033244/filtered_normalized_review/`.

Captured canonical targets in the latest proof:

- Phone portrait: home, search, thread, guide, answer, emergency.
- Tablet portrait: home, search, thread, guide, answer, emergency.

Missing from the latest filtered proof and still required for closure:

- `answer-phone-landscape.png`, `answer-tablet-landscape.png`,
  `guide-phone-landscape.png`, `guide-tablet-landscape.png`,
  `home-phone-landscape.png`, `home-tablet-landscape.png`,
  `search-phone-landscape.png`, `search-tablet-landscape.png`,
  `thread-phone-landscape.png`, and `thread-tablet-landscape.png`.

Latest full 22-target visual-diff baseline remains:

- `artifacts/ui_state_pack/source_emergency_ask_wave_clean/20260429_173223`.
- Use that pack's validated `visual_diff/mock_parity_visual_diff.md` as the
  last complete MAE ordering until a new full four-role pack is captured.

High-signal deltas after the current-head commits:

- Tablet answer/thread source panes are now intentionally visible in portrait;
  verify they match the target hierarchy without crowding the answer body.
- XML and Compose typography/color/navigation tokens have been normalized; the
  next full proof should check rail label scale, selected state, and chrome
  consistency across XML and Compose surfaces.
- Phone guide chrome density was tightened; confirm phone portrait improved
  without making phone landscape guide/detail too compressed.
- Search row density was tightened after a reverted tablet home/search shell
  geometry attempt; recheck all home/search postures before any more shell
  geometry work.
- State-pack review artifacts now include filtered normalized review aids; keep
  the distinction between raw screenshots, filtered review PNGs, and full
  canonical mock parity artifacts.
- Saved navigation now has harness proof through `6aaf2e9`; include Saved in
  functional/back-stack proof, but do not treat it as one of the 22 visual mock
  closure targets unless the canonical mock set expands.

Post-4am delegation order:

1. Full proof worker: run a four-role, 22-target state pack from `6aaf2e9`
   before assigning more visual source edits. Acceptance is `22/22`, homogeneous
   APK/model, `0` ANRs, `0` rotation mismatches, and complete mock zip or
   normalized review.
2. Visual triage worker: compare the new full pack against `artifacts/mocks`
   and the last full baseline above. Produce a short ranked list by target,
   separating pure visual drift from functional UX/back-stack behavior.
3. Detail/source worker: if the full proof shows the source panes are still a
   high drift item, tune tablet answer/thread source hierarchy first, then phone
   guide/detail density. Do not reopen home/search shell geometry in this slice.
4. Home/search worker: only after detail/source is stable, retune home/search
   density and rail/chrome tokens across all four postures.
5. Functional UX worker: run Saved, Ask/Search submit ownership, Emergency exit
   labeling, and Back-stack checks as a companion proof. Keep these findings out
   of the 22-target MAE list unless they visibly alter canonical screenshots.
6. Closure worker: run the local quality gate and final full mock parity pack,
   then update the older phase tracker with a pointer to the winning proof.

### 2026-04-30 03:46 Integrator Addendum

Additional pushed commits after the 03:32 rebaseline:

- `8e01be2` - test: lock tablet rail navigation parity.
- `702f590` - Update mock parity tracking for latest proof.
- `a84861c` - Tighten tablet answer rail balance.

Latest post-push tablet-only proof:

- `artifacts/ui_state_pack/post_push_0346_visual_proof/20260430_034645`.
- Summary reports `total_states=6`, `pass_count=6`, `fail_count=0`,
  `platform_anr_count=0`, `matrix_homogeneous=true`, and
  `rotation_mismatch_count=0`.
- Pack status is still expected `fail` because this is a tablet-portrait-only
  filtered proof with 6/22 canonical PNGs.
- `answer-tablet-portrait.png` confirms the source pane is visible and narrowed
  from the earlier 420dp portrait rail to 300dp, restoring answer-first width.

Next high-signal detail slice:

- Tablet answer target still shows related guide rows under the warning block;
  the current Compose answer surface does not yet render those rows in-flow.
  This likely needs a deliberate state/model slice from `DetailActivity`'s
  `currentRelatedGuides` into `TabletDetailState`, then a Compose list below
  `PrimaryAnswerBlock`.

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

## 2026-04-29 Worker Wave And Guide-Frame Proof

Integrated commits after the deep-review baseline:

- `07fba96 polish search result density`
- `40fe6c3 restore guide section heading styling`
- `31f1e02 polish home chrome proportions`
- `3a0ea33 tighten tablet thread hierarchy`
- `bb1073d surface phone detail composer context`
- `c8fe8fd extend tablet guide paper frame`

Validation:

- Worker-wave clean pass before guide-frame patch:
  `artifacts/ui_state_pack/deep_review_worker_wave/20260429_151911/summary.json`
  reports `22/22`, `platform_anr_count=0`, homogeneous APK
  `1eba7c982c93498f9467c5d42c6988d3ea35ef0c9131520f3f8460366ecde29e`.
- Guide-frame pass after `c8fe8fd`:
  `artifacts/ui_state_pack/guide_frame_wave/20260429_153637/summary.json`
  reports `22/22`, `platform_anr_count=0`, homogeneous APK
  `ace978bf151964f4338122be5e5038af471c2bb3a3d70173dcb1963e419c2a9b`.
- Visual diff validated:
  `artifacts/ui_state_pack/guide_frame_wave/20260429_153637/visual_diff/mock_parity_visual_diff.md`.

Important metric movement from the guide-frame patch:

- `guide-tablet-portrait.png` MAE `51.74 -> 18.05`.
- `guide-tablet-landscape.png` MAE `20.15 -> 16.75`.

Remaining top drift after `c8fe8fd`:

- `guide-phone-portrait.png` MAE `33.60`.
- `guide-phone-landscape.png` MAE `25.10`.
- `answer-phone-portrait.png` MAE `24.60`.
- `answer-phone-landscape.png` MAE `20.75`.
- `guide-tablet-portrait.png` MAE `18.05`.
- `emergency-phone-portrait.png` MAE `16.97`.
- `guide-tablet-landscape.png` MAE `16.75`.
- `thread-phone-landscape.png` MAE `15.80`.

Wave notes:

- Search density improved phone landscape but regressed phone portrait/tablet
  slightly; retune should be conditional or partially rollback-oriented.
- Tablet thread hierarchy metadata/app-rail changes are functionally sensible,
  but visual MAE worsened in tablet landscape; follow-up should decide whether
  to keep, tune, or partially revert.
- Phone composer context matches the newer UX direction, but phone portrait
  answer MAE worsened; inspect whether context row placement or composer
  height needs tighter target-specific spacing.

## 2026-04-29 Phone/Tablet Geometry Wave And Deep-Review Input

Integrated commits after the guide-frame proof:

- `abf9722 retune search row density by form factor`
- `34752b8 extend phone guide paper frame`
- `1177a3e tighten tablet guide landscape rails`

Validation:

- Full state pack:
  `artifacts/ui_state_pack/phone_tablet_geometry_wave/20260429_155255/summary.json`
  reports `22/22`, `platform_anr_count=0`, homogeneous APK
  `acc1455b30c189b0800b4b79816c006856ef3061b9127be432a356a88187c99c`.
- Visual diff validated:
  `artifacts/ui_state_pack/phone_tablet_geometry_wave/20260429_155255/visual_diff/mock_parity_visual_diff.md`.
- Local time at this checkpoint: `2026-04-29 16:02:57 -05:00`.

Metric movement from `guide_frame_wave/20260429_153637` to
`phone_tablet_geometry_wave/20260429_155255`:

- `guide-phone-portrait.png` MAE `33.60 -> 25.01`.
- `guide-tablet-landscape.png` MAE `16.75 -> 15.08`.
- `guide-phone-landscape.png` MAE `25.10 -> 24.38`.
- `answer-phone-portrait.png` MAE `24.60 -> 23.97`.
- `search-tablet-landscape.png` MAE `10.10 -> 9.98`.
- Small regressions to watch:
  `search-phone-portrait.png` MAE `13.65 -> 14.00` and
  `search-tablet-portrait.png` MAE `6.69 -> 6.94`.

Remaining top visual drift after this wave:

- `guide-phone-portrait.png` MAE `25.01`.
- `guide-phone-landscape.png` MAE `24.38`.
- `answer-phone-portrait.png` MAE `23.97`.
- `answer-phone-landscape.png` MAE `20.75`.
- `guide-tablet-portrait.png` MAE `18.05`.
- `emergency-phone-portrait.png` MAE `16.97`.
- `thread-phone-landscape.png` MAE `15.80`.
- `thread-phone-portrait.png` MAE `15.33`.
- `guide-tablet-landscape.png` MAE `15.08`.

Deep-review file now active:
`C:\Users\tateb\Downloads\uiuxreviewdeepresearch.md`.

Deep-review recommendations already addressed by recent commits:

- Shared Ask/Search submit routing now goes through the centralized submit
  path for IME and hardware Enter.
- Tablet Ask button no longer falls through to search ownership.
- Shared search input labels were moved toward explicit string/label
  semantics.
- Hidden phone answer body mirror is now removed from answer mode instead of
  kept as a near-invisible view.
- Restored phone tab ownership has focused unit coverage.
- Tablet answer mode now defaults to answer-first collapsed evidence.

Deep-review items still active:

- Continue adding local functional regression coverage around rotation and
  restored-state submit ownership.
- Keep improving answer/source evidence hierarchy without reintroducing side
  rails as the default answer surface.
- Reduce reliance on posture-specific constants only where a small shared
  policy can replace them without destabilizing mock parity.
- Treat local state packs and focused tests as the required validation lane
  instead of GitHub-hosted CI.

## Evidence Anchors

- Target mocks: `artifacts/mocks`, 22 canonical PNGs.
- Current best proof:
  `artifacts/ui_state_pack/post_push_0323_visual_proof/20260430_033244`.
  Captured states are clean (`12/12`, `fail_count=0`, homogeneous APK
  `6e1380d0510e03fda7083b3b36d3a488786f31e520f9ef52783c8d505ebcdee2`,
  model `gemma-4-e2b-it-litert`, rotation mismatch `0`), but the pack-level
  status is `fail` because this filtered portrait proof is missing the 10
  landscape canonical PNGs.
- Latest complete 22-target comparison proof:
  `artifacts/ui_state_pack/source_emergency_ask_wave_clean/20260429_173223`.
  Summary is `pass`, `22/22`, homogeneous APK
  `bc02d70835a10520de6602af914388c27b65f7f1fa60264d52e6365dc8ce7cb0`,
  model `gemma-4-e2b-it-litert`, rotation mismatch `0`, with validated
  `visual_diff/mock_parity_visual_diff.md`.
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

## 2026-04-29 Source/Emergency/Ask Wave Proof

Integrated commits after the geometry/guide-frame baseline:

- `373c800` - emergency action hierarchy and density polish.
- `0bb79cf` - answer source card and related guide primitive polish.
- `0d8e799` - landscape Ask-tab submit ownership fix.
- `c32e200` - tablet portrait search state-pack wait stabilization after
  measured offline hybrid search exceeded the old 15s capture budget.

Validation:

- Focused JVM/API checks passed:
  `:app:testDebugUnitTest --tests com.senku.mobile.MainActivityPhoneNavigationTest --tests com.senku.mobile.DetailActionBlockPresentationFormatterTest --tests com.senku.mobile.DetailProofPresentationFormatterTest --tests com.senku.mobile.EmergencySurfacePolicyTest --tests com.senku.mobile.DetailSourcePresentationFormatterTest --tests com.senku.mobile.DetailRelatedGuidePresentationFormatterTest --tests com.senku.ui.sources.SourceRowModelTest :app:assembleDebug :app:assembleDebugAndroidTest`.
- Direct tablet portrait search instrumentation passed after `c32e200`:
  `PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling`,
  `OK (1 test)`, time `26.709s`.
- Tablet portrait role rerun:
  `artifacts/ui_state_pack/search_wait_patch_tablet_portrait/20260429_172559/summary.json`
  reports role `6/6`, `platform_anr_count=0`.
- Final full proof:
  `artifacts/ui_state_pack/source_emergency_ask_wave_clean/20260429_173223/summary.json`
  reports `status=pass`, `22/22`, `fail_count=0`, `platform_anr_count=0`,
  homogeneous APK `bc02d70835a10520de6602af914388c27b65f7f1fa60264d52e6365dc8ce7cb0`,
  host model `gemma-4-e2b-it-litert`,
  SHA `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
- Goal mock zip:
  `artifacts/ui_state_pack/source_emergency_ask_wave_clean/20260429_173223_mocks.zip`
  contains all 22 canonical PNGs.
- Visual diff validated:
  `artifacts/ui_state_pack/source_emergency_ask_wave_clean/20260429_173223/visual_diff/mock_parity_visual_diff.md`.

Current worst visual drift after this wave:

- `answer-phone-portrait.png` MAE `25.48`.
- `guide-phone-portrait.png` MAE `25.01`.
- `guide-phone-landscape.png` MAE `24.38`.
- `answer-phone-landscape.png` MAE `21.48`.
- `guide-tablet-portrait.png` MAE `18.05`.
- `emergency-phone-portrait.png` MAE `16.61`.
- `thread-phone-landscape.png` MAE `16.29`.
- `thread-phone-portrait.png` MAE `15.33`.

Known remaining design mismatch:

- Closed by the unified detail nav rail slice: phone landscape detail/thread now
  uses the labeled `Library` / `Ask` / `Saved` app rail with the same icon
  language and separator as home/search instead of the compact `S / = / Q / []`
  glyph rail. Tablet detail already used a labeled Compose rail; a later
  behavior-only follow-up can make its `Ask` rail item active if needed.

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

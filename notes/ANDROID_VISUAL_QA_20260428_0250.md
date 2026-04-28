# Android Visual QA Punch List - 2026-04-28 02:50

Reviewer lane: analysis only. Android source was not edited.

Baseline proof pack: `artifacts/ui_state_pack/20260428_024021`

Target mocks: `artifacts/mocks`

Technical status of baseline pack: pass, 47/47 states, homogeneous APK
`dea572e14f73fa8ee8115205b4beae06bd37be720d33f1c5bd7345be9af197bf`,
model `gemma-4-e2b-it-litert`, rotation mismatch count 0.

Important coordination note: `git status --short` showed active concurrent
Android source edits across P1-P7 ownership sets while this review was written.
Treat every file listed below as already potentially owned by another worker
until live status and ownership transfer are checked.

## Ranked Punch List

### 1. P1/P3 - Answer surface is still the largest visual blocker

Verdict: blocking across all four answer mocks.

Target mocks:

- `artifacts/mocks/answer-phone-portrait.png`
- `artifacts/mocks/answer-phone-landscape.png`
- `artifacts/mocks/answer-tablet-portrait.png`
- `artifacts/mocks/answer-tablet-landscape.png`

Current evidence:

- Phone portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- Phone portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`
- Phone landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`
- Phone landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.xml`
- Tablet portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- Tablet portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`
- Tablet landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- Tablet landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`

Concrete gaps:

- Phone portrait target is a clean answer article with sources and related
  guide rows. Current proof renders a `Field entry - Unsure fit` answer card,
  then expands `Route, proof & provenance | Uncertain | Unsure fit | 3 src`
  down into the composer region.
- Phone landscape target is the split answer article. Current proof still shows
  `Field entry - Unsure fit` and the uncertainty card; the hidden
  `detail_body` is effectively collapsed to `[26,1014][29,1017]` in the dump.
- Tablet portrait and landscape target the answer-first article layout. Current
  proof shows a centered uncertainty card and thread-context composer, not the
  answer article/source stack.
- Source/provenance/source-graph states remain visually distinct, but they are
  not acceptable substitutes for the answer mocks.

File ownership:

- P1 shared shell/mode owner if the fix needs surface selection, detail layout,
  tablet shell, evidence rail, or composer clearance:
  `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`,
  `android-app/app/src/main/res/layout/activity_detail.xml`,
  `android-app/app/src/main/res/layout-land/activity_detail.xml`,
  `android-app/app/src/main/res/layout-large/activity_detail.xml`,
  `android-app/app/src/main/res/layout-large-land/activity_detail.xml`,
  `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`,
  `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`,
  `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`,
  `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`,
  `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`.
- P3 answer content owner for article/source/provenance formatting:
  `android-app/app/src/main/java/com/senku/mobile/DetailAnswerBodyFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailAnswerPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailMetaPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailProofPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailProvenancePresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailReviewedCardMetadataBridge.java`,
  `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`,
  `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`.

### 2. P1/P5 - Thread still reads as answer detail, not transcript hierarchy

Verdict: blocking across all four thread mocks.

Target mocks:

- `artifacts/mocks/thread-phone-portrait.png`
- `artifacts/mocks/thread-phone-landscape.png`
- `artifacts/mocks/thread-tablet-portrait.png`
- `artifacts/mocks/thread-tablet-landscape.png`

Current evidence:

- Phone portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- Phone portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`
- Phone landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- Phone landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`
- Tablet portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- Tablet portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`
- Tablet landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- Tablet landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`

Concrete gaps:

- Phone portrait mock is a clean two-turn transcript. Current proof inserts a
  large selected `Field entry - Moderate evidence` answer card after Q1/A1 and
  pushes content into the bottom composer area.
- Phone landscape target keeps Q2/A2 and sources readable in a compact split.
  Current proof has header/action chrome consuming too much height and the
  dump shows the focused `detail_body` collapsed to `[26,818][29,821]`.
- Tablet portrait/landscape mocks make the transcript the primary surface with
  support rails. Current proof titles the main surface as
  `Abrasives Manufacturing - 2 turns`, centers an answer card, and demotes the
  transcript into side/earlier-turn structures.

File ownership:

- P5 content owner:
  `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`,
  `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`,
  `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`,
  `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`.
- Escalate to P1 for `TabletDetailScreen.kt`, `EvidencePane.kt`,
  `DetailActivity.java`, detail layout XML, or composer changes.

### 3. P1/P6 - Emergency tablet remains a narrow overlay; phone needs composer polish

Verdict: tablet portrait blocking; phone portrait close but not clean.

Target mocks:

- `artifacts/mocks/emergency-phone-portrait.png`
- `artifacts/mocks/emergency-tablet-portrait.png`

Current evidence:

- Phone portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- Phone portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.xml`
- Tablet portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- Tablet portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.xml`

Concrete gaps:

- Tablet target is a full tablet emergency hierarchy. Current proof places
  `detail_emergency_header` in bounds `[296,160][1176,1161]`, a narrow red
  overlay on top of stale answer/detail content.
- Tablet current also leaves the old answer card visible behind the emergency
  panel, so hierarchy and background ownership are still wrong.
- Phone current has the correct four immediate actions, but keeps an expanded
  `Route, backend & proof` block and `Guide connection | Show` near the
  composer. Target phone mock ends with one compact evidence card and a quiet
  composer.

File ownership:

- P6 emergency content owner:
  `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`,
  `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`,
  `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`,
  `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`,
  `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`,
  `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`,
  `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`,
  `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`.
- Escalate to P1 for tablet shell, `DetailActivity.java`, detail layouts, or
  composer/background ownership.

### 4. P1/P2 - Guide reader is improved on tablet, but phone and xref modes still miss

Verdict: blocking until all four guide mocks use one coherent guide-reader
contract.

Target mocks:

- `artifacts/mocks/guide-phone-portrait.png`
- `artifacts/mocks/guide-phone-landscape.png`
- `artifacts/mocks/guide-tablet-portrait.png`
- `artifacts/mocks/guide-tablet-landscape.png`

Current evidence:

- Phone portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- Phone portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- Phone landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- Phone landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- Tablet portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- Tablet portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- Tablet landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- Tablet landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- Tablet xref screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
- Tablet xref dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.xml`

Concrete gaps:

- Phone portrait and landscape still render the guide body as an oversized
  raw paper document. The dump exposes one huge text node starting with
  `DANGER | EXTREME BURN HAZARD...`, rather than the mock's compact header,
  warning module, sections, and link rows.
- Tablet related-guide state is close structurally, but the dump reports
  `SECTIONS - 1` and `CROSS-REFERENCE - 1`; the target mocks expect the full
  sections rail and cross-reference rail contract.
- Tablet landscape xref/field-link variants show `FIELD LINKS` and a
  `CROSS-REFERENCE - 7` rail, not the target `CROSS-REFERENCE - 6` guide rail
  and centered paper contract. The paper body also exposes raw `DANGER` and
  `WARNING` text inside a single long text block.

File ownership:

- P2 guide owner:
  `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`,
  guide paper drawables under
  `android-app/app/src/main/res/drawable/bg_detail_guide_paper_*.xml`,
  `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`,
  `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`,
  `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`.
- Escalate to P1 for `TabletDetailScreen.kt`, `ThreadRail.kt`,
  `EvidencePane.kt`, `DetailActivity.java`, or detail XML.

### 5. P4/P7 - Search result content, filters, and preview are not mock-parity

Verdict: blocking for all four search mocks.

Target mocks:

- `artifacts/mocks/search-phone-portrait.png`
- `artifacts/mocks/search-phone-landscape.png`
- `artifacts/mocks/search-tablet-portrait.png`
- `artifacts/mocks/search-tablet-landscape.png`

Current evidence:

- Phone portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- Phone portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.xml`
- Phone landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- Phone landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.xml`
- Tablet portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- Tablet portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.xml`
- Tablet landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- Tablet landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.xml`

Concrete gaps:

- Target result order/content is GD-023, GD-027, GD-345, GD-294. Current dump
  shows GD-345, GD-446, GD-023, GD-873. This is content/data, not just layout.
- Phone target has a dedicated search input row. Current phone screenshots
  collapse query/count/timing into one large title line under the global home
  chrome.
- Tablet filters are literal text labels such as `[ ] Shelter`, `[ ] Water`,
  `[ ] Immediate`; target uses proper checkbox controls and tighter filter
  rhythm.
- Tablet landscape target previews GD-023. Current preview is `PREVIEW - GD-345`
  and includes raw markdown `## Wood Quality Evaluation...`.

File ownership:

- P4 search owner:
  `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`,
  `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`,
  `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`,
  `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`.
- Escalate to P7 for `MainActivity.java`, `activity_main*`, shared
  home/search chrome, or `BottomTabBar.kt`.

### 6. P7 - Home is no longer the main blocker, but chrome/density still differs

Verdict: open polish, not current visual blocker.

Target mocks:

- `artifacts/mocks/home-phone-portrait.png`
- `artifacts/mocks/home-phone-landscape.png`
- `artifacts/mocks/home-tablet-portrait.png`
- `artifacts/mocks/home-tablet-landscape.png`

Current evidence:

- Phone portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- Phone portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`
- Phone landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- Phone landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`
- Tablet portrait screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- Tablet portrait dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`
- Tablet landscape screenshot:
  `artifacts/ui_state_pack/20260428_024021/screenshots/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- Tablet landscape dump:
  `artifacts/ui_state_pack/20260428_024021/dumps/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`

Concrete gaps:

- Current screenshots still include Android system status/nav bars and heavier
  top chrome; target mocks read as a tighter framed app shell.
- Phone recent-thread rows use live/generated wording and `T-00:01` style
  metadata instead of the target row copy and confidence/status rhythm.
- Tablet landscape proportions are usable but not mock-tight: current recent
  threads sit inside a heavier panel and category/search spacing differs.

File ownership:

- P7 home/chrome owner:
  `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`,
  `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`,
  `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`,
  `android-app/app/src/main/res/layout/activity_main.xml`,
  `android-app/app/src/main/res/layout-land/activity_main.xml`,
  `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`,
  `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`,
  `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`,
  home/search chrome drawables under `android-app/app/src/main/res/drawable/`
  with prefixes `bg_home`, `bg_manual_home`, `bg_manual_phone_home`,
  `bg_tablet_home`, and `bg_search_shell`,
  `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`,
  `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`.

## Recommended Dispatch Order

1. Claim/settle ownership first. The live worktree already has modified files
   in P1-P7 areas.
2. P1/P3 answer: restore true answer article mode and composer clearance.
3. P1/P5 thread: make transcript hierarchy primary on phone and tablet.
4. P1/P6 emergency: replace tablet overlay with full tablet emergency
   hierarchy, then clean phone bottom proof/composer behavior.
5. P1/P2 guide: normalize phone reader and tablet section/xref contracts.
6. P4 search: fix result data/order, filter controls, and tablet preview.
7. P7 home/chrome: final proportional polish after shared search/detail shell
   changes settle.

## Closure Notes

- Do not accept any family from unit tests alone; every closure needs current
  screenshot plus XML dump proof.
- Emergency landscape still has no target mock and should not become an
  acceptance surface.
- The next full closure pack must remain homogeneous across APK, model, and
  rotation facts, matching the standard used by `20260428_024021`.

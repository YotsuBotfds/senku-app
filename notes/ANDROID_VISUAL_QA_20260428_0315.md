# Android Visual QA Punch List - 2026-04-28 03:15

Reviewer lane: analysis only. Android source and generated artifacts were not
edited.

Baseline proof pack: `artifacts/ui_state_pack/20260428_030203`

Previous review: `notes/ANDROID_VISUAL_QA_20260428_0250.md`

Target mocks: `artifacts/mocks`

Technical status of baseline pack: pass, 47/47 states, homogeneous APK
`ad0d41b6b7d9f169a1e89ea3c63be854f9aafbef1a9a1c374c42d7832c4cf732`,
model `gemma-4-e2b-it-litert`, rotation mismatch count 0.

Scope note: ranked items below are only remaining actionable visual blockers.
Home/chrome remains polish and is not ranked here.

## What Changed Since 02:50

- Emergency moved the most: tablet portrait is no longer a narrow centered
  strip, and it now renders the correct four immediate actions in a broad
  emergency panel. It still sits over stale tablet rails/content and uses the
  wrong bottom proof/source treatment, so it remains open.
- Thread phone portrait now exposes Q1/A1 and the Q2 question before the
  selected answer card. It still switches the active answer into a card surface
  instead of the mock's clean Q2/A2 transcript.
- Answer, guide reader, and search are materially unchanged against the mocks:
  answer still renders uncertainty cards/proof rails, guide phone still renders
  raw oversized paper text, and search still has the wrong result set/order and
  tablet preview.

## Ranked Blockers

### 1. P1/P3 - Answer Article Mode Still Does Not Match Any Answer Mock

Verdict: blocking across all four answer mocks.

Changed since 02:50: no substantive closure. The phone/tablet answer states
still route to `Field entry - Unsure fit` cards and proof/source rails instead
of the mock's answer article with sources and related-guide rows.

Target mocks:

- `artifacts/mocks/answer-phone-portrait.png`
- `artifacts/mocks/answer-phone-landscape.png`
- `artifacts/mocks/answer-tablet-portrait.png`
- `artifacts/mocks/answer-tablet-landscape.png`

Current evidence:

- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`

Concrete gaps:

- Phone portrait still starts the body with `Field entry - Unsure fit`; the dump
  shows `Route, proof & provenance | Uncertain | Unsure fit | 3 src | Show proof`
  expanded into the lower viewport.
- Phone landscape still uses `detail_answer_bubble` and
  `detail_answer_card` for the split answer state, with source buttons clipped
  at the bottom of the main column.
- Tablet portrait/landscape still present a card/source-graph answer layout
  with `Unsure fit`, not the mock's article-first answer surface.

Owned file set for next slice:

- P1 shared shell/mode: `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`,
  `android-app/app/src/main/res/layout/activity_detail.xml`,
  `android-app/app/src/main/res/layout-land/activity_detail.xml`,
  `android-app/app/src/main/res/layout-large/activity_detail.xml`,
  `android-app/app/src/main/res/layout-large-land/activity_detail.xml`,
  `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`,
  `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`,
  `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`,
  `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`,
  `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`.
- P3 answer content: `android-app/app/src/main/java/com/senku/mobile/DetailAnswerBodyFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailAnswerPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailMetaPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailProofPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailProvenancePresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailReviewedCardMetadataBridge.java`,
  `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`,
  `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`.

### 2. P1/P2 - Guide Reader Still Falls Back To Raw Paper Text

Verdict: blocking for guide phone and tablet guide/xref contracts.

Changed since 02:50: no visible guide-reader closure. Phone remains a large raw
paper document; tablet related/xref states still expose reduced rail counts or
raw guide body content rather than the mock section/cross-reference contract.

Target mocks:

- `artifacts/mocks/guide-phone-portrait.png`
- `artifacts/mocks/guide-phone-landscape.png`
- `artifacts/mocks/guide-tablet-portrait.png`
- `artifacts/mocks/guide-tablet-landscape.png`

Current evidence:

- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.xml`

Concrete gaps:

- Phone portrait still renders `DANGER - EXTREME BURN HAZARD` as oversized
  raw body text on beige paper instead of the mock's compact field-manual
  header, warning module, section cards, and related links.
- Tablet xref dump still reports `SECTIONS - 1` and `CROSS-REFERENCE - 1`,
  while the mock expects the full guide navigation/rail contract.
- Tablet xref body still exposes one huge raw `DANGER`/`WARNING` text node,
  including unrelated foundry guide sections, instead of the formatted guide
  reader surface.

Owned file set for next slice:

- P2 guide owner: `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`,
  `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`,
  guide paper drawables under `android-app/app/src/main/res/drawable/bg_detail_guide_paper_*.xml`,
  `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`,
  `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`,
  `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`.
- Escalate to P1 for `TabletDetailScreen.kt`, `ThreadRail.kt`,
  `EvidencePane.kt`, `DetailActivity.java`, or detail XML.

### 3. P4/P7 - Search Result Set, Filters, And Preview Remain Off-Mock

Verdict: blocking across all four search mocks.

Changed since 02:50: no closure. Result order/content still starts with
GD-345/GD-446 instead of GD-023/GD-027, tablet filters remain literal `[ ]`
text, and tablet preview remains GD-345 with raw markdown.

Target mocks:

- `artifacts/mocks/search-phone-portrait.png`
- `artifacts/mocks/search-phone-landscape.png`
- `artifacts/mocks/search-tablet-portrait.png`
- `artifacts/mocks/search-tablet-landscape.png`

Current evidence:

- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.xml`

Concrete gaps:

- Current result order is GD-345, GD-446, GD-023, GD-873; target order/content
  is GD-023, GD-027, GD-345, GD-294.
- Tablet filters are text nodes such as `[ ] Shelter`, `[ ] Water`, and
  `[ ] Immediate`; target uses real checkbox controls with counts.
- Tablet landscape preview remains `PREVIEW - GD-345` with body text beginning
  `## Wood Quality Evaluation...`; target preview is GD-023 with clean prose.

Owned file set for next slice:

- P4 search owner: `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`,
  `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`,
  `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`,
  `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`.
- Escalate to P7 for `MainActivity.java`, `activity_main*`, shared
  home/search chrome, or `BottomTabBar.kt`.

### 4. P1/P5 - Thread Is Partly Better On Phone But Still Not A Transcript

Verdict: blocking across all four thread mocks.

Changed since 02:50: phone portrait now shows the prior Q1/A1 plus the active
Q2 prompt before the answer card, which is progress. It still promotes the
active response into `Field entry - Moderate evidence`, and tablet remains a
selected answer/detail layout rather than the mock transcript hierarchy.

Target mocks:

- `artifacts/mocks/thread-phone-portrait.png`
- `artifacts/mocks/thread-phone-landscape.png`
- `artifacts/mocks/thread-tablet-portrait.png`
- `artifacts/mocks/thread-tablet-landscape.png`

Current evidence:

- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.xml`

Concrete gaps:

- Phone portrait mock expects Q2/A2 as transcript prose; current proof shows
  `Field entry - Moderate evidence` and a large selected answer card that runs
  into the composer/source-chip area.
- Phone current also anchors the active response around GD-220/source-match
  card treatment, while the target A2 row uses the clean thread answer rhythm
  and GD-345 tag.
- Tablet portrait/landscape still title the detail as
  `Abrasives Manufacturing - 2 turns` with source graph/detail rails, not a
  primary two-turn transcript surface.

Owned file set for next slice:

- P5 content owner: `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`,
  `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`,
  `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`,
  `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`.
- Escalate to P1 for `TabletDetailScreen.kt`, `EvidencePane.kt`,
  `DetailActivity.java`, detail layout XML, or composer changes.

### 5. P1/P6 - Emergency Is Improved But Still Mounted As Detail Overlay

Verdict: still blocking for tablet portrait and still open on phone bottom
contract.

Changed since 02:50: tablet portrait now has a broad emergency panel with the
correct four actions, so the previous narrow-overlay failure is partially
addressed. It is not closure: stale tablet thread/source rails are visible
behind it, and the target uses a full emergency hierarchy with a `WHY THIS
ANSWER` card rather than a modal-like proof overlay.

Target mocks:

- `artifacts/mocks/emergency-phone-portrait.png`
- `artifacts/mocks/emergency-tablet-portrait.png`

Current evidence:

- `artifacts/ui_state_pack/20260428_030203/screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.xml`
- `artifacts/ui_state_pack/20260428_030203/screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- `artifacts/ui_state_pack/20260428_030203/dumps/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.xml`

Concrete gaps:

- Tablet current shows the emergency panel over the normal tablet detail shell;
  stale thread/source cards remain visible behind the panel.
- Tablet dump shows `Route, backend & proof` inside the emergency surface; mock
  expects `WHY THIS ANSWER` with a single GD-132 anchor evidence card.
- Phone current top/action content is close, but the lower region still shows
  `Route, backend & proof`, `Guide connection | Show`, horizontal source chips,
  and generic `Ask follow-up` composer instead of the mock's quiet emergency
  context/footer.

Owned file set for next slice:

- P6 emergency owner: `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`,
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

## Dispatch Order

1. P1/P3 answer: restore true answer article mode and source/related-guide
   stack before proof/composer polish.
2. P1/P2 guide: replace raw phone paper and tablet xref raw-body fallback with
   the formatted guide-reader contract.
3. P4/P7 search: fix deterministic result set/order, checkbox controls, and
   GD-023 tablet preview.
4. P1/P5 thread: finish the phone transcript conversion and move tablet thread
   away from selected answer/detail mode.
5. P1/P6 emergency: convert the improved panel into the full emergency surface,
   then clean phone footer/proof/composer behavior.

## Closure Notes

- Keep using screenshot plus XML dump proof for closure; none of the blockers
  above can be accepted from tests alone.
- Emergency landscape still has no target mock and should not be introduced as
  an acceptance surface.
- The next closure pack should preserve the same hygiene as `20260428_030203`:
  homogeneous APK/model matrix and zero rotation mismatches.

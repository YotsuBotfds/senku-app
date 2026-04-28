# Android Visual Review - Wave 8 State Pack 231419

State pack: `artifacts/ui_state_pack/20260427_231419`
Target mocks: `artifacts/mocks`

## Scope

Reviewed the fresh passing UI state pack screenshots against the target screenshots under `artifacts/mocks`. No code was edited.

Pack health from `artifacts/ui_state_pack/20260427_231419/summary.json`:

- Overall status: `pass`
- Total states: 47
- Passed: 47
- Failed: 0
- Platform ANRs: 0
- Matrix homogeneous: true
- Rotation mismatches: 0

## Mock Checklist

| Mock item | Representative state-pack screenshots | Status |
|---|---|---|
| `artifacts/mocks/home-phone-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Partial: structure is present, but copy, density, header treatment, and category counts drift. |
| `artifacts/mocks/home-phone-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Partial: core lanes exist, but current landscape app shell is larger/heavier than mock. |
| `artifacts/mocks/home-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Partial: tablet layout renders, but uses the new full app-shell vocabulary rather than the mock's quieter split. |
| `artifacts/mocks/home-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Partial: side rail and recent-thread pane are present, but spacing, copy, counts, and top chrome differ. |
| `artifacts/mocks/search-phone-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Partial: four results render, but query, row/card design, header, metadata order, and bottom clipping differ. |
| `artifacts/mocks/search-phone-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Partial: result list exists, but card scale and navigation chrome remain off-target. |
| `artifacts/mocks/search-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Partial: populated state passes; still visually heavier and less mock-like. |
| `artifacts/mocks/search-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Partial: tablet search state exists, but the result/detail balance and mock density are not yet matched. |
| `artifacts/mocks/answer-phone-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Partial: answer content and source context exist, but the screen has shifted into a large card/detail template rather than the mock's compact answer document. |
| `artifacts/mocks/answer-phone-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_landscape/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Partial: landscape answer is stable, but top controls and large typography/card treatment remain off-target. |
| `artifacts/mocks/answer-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Partial: tablet answer path is covered; mock-level source stack and compact detail density are not yet matched. |
| `artifacts/mocks/answer-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Partial: state is present and passing; mock's document/source composition is still different. |
| `artifacts/mocks/guide-phone-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` | Major gap: content appears, but not in the mock's cream document surface; current header/card treatment consumes too much space. |
| `artifacts/mocks/guide-phone-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` | Major gap: same guide-surface mismatch, with landscape chrome still dominant. |
| `artifacts/mocks/guide-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` | Major gap: passing state, but target tablet guide document styling is not there yet. |
| `artifacts/mocks/guide-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`; `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` | Major gap: source context exists, but the document panel, typography, and rail balance differ strongly from target. |
| `artifacts/mocks/thread-phone-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Major gap: thread history is represented as large question/answer blocks inside detail mode, not the compact two-turn transcript from the mock. |
| `artifacts/mocks/thread-phone-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Major gap: same transcript-vs-detail mismatch. |
| `artifacts/mocks/thread-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Major gap: tablet thread path passes, but not visually aligned with target transcript density. |
| `artifacts/mocks/thread-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Major gap: passing state, but still uses answer/detail composition instead of the mock thread view. |
| `artifacts/mocks/emergency-phone-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png` | Major gap: emergency semantics render, but the target burn-hazard layout is not matched; current poisoning answer has oversized provenance card and different action/copy set. |
| `artifacts/mocks/emergency-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png` | Major gap: tablet emergency passes, but remains visually/copy-wise far from the target emergency mock. |

## Ranked Visual Gaps

1. Guide detail document surface is the biggest mismatch. Target mocks such as `artifacts/mocks/guide-phone-portrait.png` use a cream field-manual document panel with dense sectional typography; current screenshots such as `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png` show a dark header-plus-card shell and oversized text, with the cream content starting too low and too wide.

2. Thread screens are not yet the target transcript view. `artifacts/mocks/thread-phone-portrait.png` is a compact two-turn history with Q/A labels, timestamps, anchors, confidence, and small guide chips; `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` renders the prior turn as large cards and then drops into a detail answer surface.

3. Emergency answer state is passing but visually/copy-wise off target. `artifacts/mocks/emergency-phone-portrait.png` is a burn-hazard emergency with tight top alert, four numbered actions, and a compact source card; `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png` is a poisoning scenario with a huge title/header, three actions, and an oversized provenance block that dominates the viewport.

4. App chrome and density are still heavier than the mocks across phone and tablet. Examples: `artifacts/mocks/home-phone-portrait.png` versus `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`, and `artifacts/mocks/home-tablet-landscape.png` versus `artifacts/ui_state_pack/20260427_231419/screenshots/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`. The status/header/nav areas consume more height, labels are larger, and the mock's compact separators and metadata rhythm are not fully restored.

5. Search result visual language remains card-heavy and mismatched. `artifacts/mocks/search-phone-portrait.png` shows a search-specific header row, compact list rows, score marks, and thin dividers; `artifacts/ui_state_pack/20260427_231419/screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` uses large result cards, a generic app header, different query/content, and bottom navigation clipping on the fourth card.

## Notes

- The pack is a real passing baseline and covers all mock families with at least one representative state.
- The remaining work is mostly visual convergence, not basic state availability.
- The highest-leverage next pass is to decide whether current state-pack copy/data should be updated to the exact mock scenarios, or whether the mocks should be treated as layout/style targets only. Several comparisons are blurred by scenario drift, especially emergency and search.

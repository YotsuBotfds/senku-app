# Android Mock Parity Phase Map - 2026-04-28

Planner lane only. This is the concise overnight dispatch sheet for closing
visual parity against `artifacts/mocks`. Do not edit Android source,
generated artifacts, target mocks, or protected `notes/PLANNER_HANDOFF_*.md`
files from this lane.

## Evidence Anchor

- Target mocks: `artifacts/mocks`, 22 PNGs.
  - Home, Search, Answer, Guide, Thread: phone portrait, phone landscape,
    tablet portrait, tablet landscape.
  - Emergency: phone portrait and tablet portrait only.
- Current proof pack: `artifacts/ui_state_pack/20260428_054213`.
  - Status `pass`, 47/47 states, homogeneous APK
    `41f9af85783049a6f1c6a9554b0a21e2bf3c026983c20be2ff7c37133fcd1426`,
    model `gemma-4-e2b-it-litert`, rotation mismatch count 0.
- Reviewed anchors: `00a3adb` (`advance android mock parity wave20`),
  `7efcac0` (`advance android mock parity wave18`), and `343bfde`
  (`polish android mock parity wave18`).
- Reviewer note: `notes/ANDROID_VISUAL_QA_20260428_0445.md`.
- Current verdict: technical proof is green; visual parity is not closed.
  Wave18 resolved the tablet thread `GD-?` header leak and removed the
  answer-source `GD-?` fallback labels. Remaining high-risk blockers are
  Answer, Guide, and Emergency mock drift, plus Home density/chrome polish.
- Claude source mocks reviewed for target intent:
  `claudedesign4-27/surface-home-search.jsx`,
  `claudedesign4-27/surface-answer-thread.jsx`,
  `claudedesign4-27/surface-guide.jsx`,
  `claudedesign4-27/primitives.jsx`, and
  `claudedesign4-27/tokens.jsx`.
- Live worktree caution at this planner refresh: wave20 was committed and
  pushed as `00a3adb`; only protected untracked handoff notes remained before
  wave21 workers started. Reconcile live `git status --short` before staging
  any worker output.

## Proof Path Rules

- For every screenshot path below, the matching dump path is the same relative
  path under `artifacts/ui_state_pack/20260428_043744/20260428_043744/dumps/` with `.xml`
  replacing `.png`.
- Every worker must cite target mock(s), current screenshot(s), current dump(s),
  and remaining deltas split as content/data, layout/density, or behavior.
- Focused proof is acceptable for a phase commit only when device, APK SHA,
  screenshot path, dump path, and diagnostic-vs-acceptance status are named.
  Final closure still requires a full homogeneous four-role state pack.

## Per-Screen Acceptance Matrix

All rows require screenshot plus XML dump review against the target mock. Unit
tests, harness pass counts, and data-pack health can support a phase but do not
close visual parity by themselves.

| Screen | Target mocks | Acceptance criteria | Current proof paths | Owning slice |
| --- | --- | --- | --- | --- |
| Home | `home-phone-portrait.png`, `home-phone-landscape.png`, `home-tablet-portrait.png`, `home-tablet-landscape.png` | Shows the flat `HOME SENKU` identity strip, offline/pack-ready state, search affordance, six category tiles with target counts, recent-thread rows, and Library/Ask/Saved nav without developer chrome. Phone portrait keeps bottom nav clear; phone landscape and tablets use side rail plus balanced content/recent-thread columns. | `screenshots/*/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`; use `homeGuideIntentShowsGuideConnectionContext__home_guide_connection_context.png` only as secondary shell context. | P6 |
| Search | `search-phone-portrait.png`, `search-phone-landscape.png`, `search-tablet-portrait.png`, `search-tablet-landscape.png` | Query is `rain shelter`; count is `4 results`; rows remain `GD-023`, `GD-027`, `GD-345`, `GD-294` with compact score bars/labels, category/role/window metadata, and snippets. Tablet filter rail uses checkbox affordances and counts; tablet landscape includes the `GD-023` preview rail. Phone landscape must not regress to a blank or over-chromed state. | `screenshots/*/searchQueryShowsResultsWithoutShellPolling__search_results.png`; `screenshots/*/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`. | P5 for rows/cards; P6 for shell/filter chrome |
| Answer | `answer-phone-portrait.png`, `answer-phone-landscape.png`, `answer-tablet-portrait.png`, `answer-tablet-landscape.png` | Renders as answer canvas, not guide paper or proof-only card mode: `ANSWER GD-345 - Rain shelter`, question title, text-first answer body, `UNSURE FIT` notice, `Sources - 3`, related guides, and follow-up composer. Phone portrait has no bottom-input collision. Phone landscape keeps split answer article plus source/related rail visible. Tablets keep bounded answer/source rails; tablet landscape may use thread/answer/evidence panes. | `screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`; `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`; `screenshots/tablet_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`; `screenshots/tablet_landscape/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`. | P1 for mode/shell; P2 for answer article/source stack |
| Guide | `guide-phone-portrait.png`, `guide-phone-landscape.png`, `guide-tablet-portrait.png`, `guide-tablet-landscape.png` | Renders as guide reader: `GUIDE GD-132 - Foundry & Metal Casting`, parchment paper panel, field-manual header, section content, danger/admonition block, required-reading rows, and no raw section markers or mojibake. Tablet portrait has sections rail plus centered paper. Tablet landscape has sections rail, centered paper, and `Cross-reference - 6` rail. Guide semantics must stay separate from answer/source/provenance semantics in dumps. | `screenshots/*/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`; `screenshots/phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `screenshots/phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`. | P1 for mode/shell; P4 for guide formatter/paper/xref |
| Thread | `thread-phone-portrait.png`, `thread-phone-landscape.png`, `thread-tablet-portrait.png`, `thread-tablet-landscape.png` | Renders as chronological transcript: `THREAD GD-220 - Rain shelter - 2 turns`, Q1/A1 plus Q2/A2 rhythm, source chips, confidence markers, and composer. Phone landscape shows recent turn plus support rail without clipping. Tablet portrait keeps transcript primary with source support rail. Tablet landscape uses left turn index, center transcript, and right `Sources in thread - 2`; no answer-detail takeover or stale `GD-?` owner labels. | `screenshots/*/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`. | P1 for shell/tablet ownership; P3 for transcript renderer/rail |
| Emergency | `emergency-phone-portrait.png`, `emergency-tablet-portrait.png` | Portrait-only acceptance: `ANSWER GD-132 - Burn hazard response`, `DANGER` badge, danger banner, underlined `minimum 5 m from active work zone`, four ordered immediate actions, one `WHY THIS ANSWER` / `GD-132` evidence card, and quiet emergency composer. Preserve negative controls as non-emergency. Do not create or require emergency landscape targets. Tablet portrait must remain a full-height emergency owner with no stale background rails. | `screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`; `screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`. | P1 for shared mounting; P7 for emergency content/chrome |

## Phase Map

### P0 - Evidence And Dispatch Lock

Owner: planner only.

Acceptance:

- Active proof pack remains `artifacts/ui_state_pack/20260428_054213`.
- This file stays aligned with reviewer findings and live `git status`.
- No code, generated artifacts, target mocks, or protected handoff notes are
  edited from this lane.

Disjoint write set:

- `notes/ANDROID_MOCK_PARITY_PHASES_20260428.md`

### P1 - Shared Detail Mode And Shell

Goal: stop answer/thread/guide states from falling into the wrong detail mode
while preserving the green emergency tablet ownership.

Target mocks:

- `artifacts/mocks/answer-*.png`
- `artifacts/mocks/thread-*.png`
- `artifacts/mocks/guide-*.png`
- `artifacts/mocks/emergency-tablet-portrait.png`

Current proof paths:

- `screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`
- `screenshots/tablet_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`
- `screenshots/tablet_landscape/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`
- `screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- `screenshots/*/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/*/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`

Acceptance:

- Answer routes render answer hierarchy, not guide/manual fallback or proof-only
  cards.
- Thread routes render transcript hierarchy, not answer-detail card mode.
- Guide routes remain guide reader routes and keep source/cross-reference rails
  only where mocks show them.
- Phone portrait has no bottom-input collision; phone landscape has no
  header/body clipping.
- Tablet emergency remains a full-height emergency owner with no stale rails
  visible behind it.

Disjoint write set:

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
- `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`

### P2 - Answer Article And Source Stack

Goal: make answer screens match the article-first answer mocks after P1 fixes
mode ownership.

Target mocks:

- `artifacts/mocks/answer-phone-portrait.png`
- `artifacts/mocks/answer-phone-landscape.png`
- `artifacts/mocks/answer-tablet-portrait.png`
- `artifacts/mocks/answer-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/phone_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_preview.png`
- `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`
- `screenshots/tablet_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`
- `screenshots/tablet_landscape/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`

Acceptance:

- Phone portrait shows compact answer header, article body, unsure-fit notice,
  source cards, related-guide rows, and composer clearance like the mock.
- Phone landscape keeps the split answer article visible without source or
  provenance clipping.
- Tablet answer keeps bounded source support rails and source cards without
  becoming a guide/cross-reference station.
- Dumps keep uncertain-fit, abstain, deterministic, reviewed-evidence, strong
  evidence, proof, and provenance labels distinct.

Disjoint write set:

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

Escalate to P1 for detail shell, tablet shell, composer, or detail XML.

### P3 - Thread Transcript Shape

Goal: make thread screens read as chronological Q/A transcript surfaces.

Target mocks:

- `artifacts/mocks/thread-phone-portrait.png`
- `artifacts/mocks/thread-phone-landscape.png`
- `artifacts/mocks/thread-tablet-portrait.png`
- `artifacts/mocks/thread-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`

Acceptance:

- Phone portrait and landscape show Q1/A1 plus Q2/A2 transcript rhythm with no
  overlapping chips, composer, or proof card takeover.
- Tablet portrait and landscape make the two-turn transcript primary; side
  rails support the thread instead of presenting an answer-detail hierarchy.
- Handoff names composer focus/IME behavior and whether source rails remain
  visible in landscape/tablet.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/ThreadRailPolicyTest.kt`

Escalate to P1 for detail shell, tablet shell, evidence panes, composer, or
detail XML.

### P4 - Guide Reader Structure

Goal: close compact guide reader parity and tablet rail/paper/xref structure.

Target mocks:

- `artifacts/mocks/guide-phone-portrait.png`
- `artifacts/mocks/guide-phone-landscape.png`
- `artifacts/mocks/guide-tablet-portrait.png`
- `artifacts/mocks/guide-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
- `screenshots/phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
- `screenshots/phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
- `screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`

Acceptance:

- Phone guide uses compact top chrome, contained paper card, sections, related
  rows, and bottom nav without oversized manual-body treatment.
- Compose output never exposes raw section markers or mojibake section text.
- Tablet guide shows sections rail, centered paper, and cross-reference rail
  matching the guide mocks.
- Dumps keep guide semantics separate from answer/source/provenance semantics.

Disjoint write set:

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

Escalate to P1 for tablet shell, evidence panes, composer, or detail XML.

### P5 - Search Results And Preview Components

Goal: close search row/card data, score, snippet, and preview component parity
without taking shared app chrome.

Target mocks:

- `artifacts/mocks/search-phone-portrait.png`
- `artifacts/mocks/search-phone-landscape.png`
- `artifacts/mocks/search-tablet-portrait.png`
- `artifacts/mocks/search-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/*/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`

Acceptance:

- Result IDs/order remain `GD-023`, `GD-027`, `GD-345`, `GD-294`.
- Rows keep compact scores, metadata, snippets, linked-guide affordance, and
  stable browse linked-guide handoff.
- Tablet landscape preview copy/proportions match `GD-023` target content.
- If filter rail, app shell, or layout XML changes are needed, transfer that
  work to P6 before editing.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

### P6 - Home/Search Shell And App Chrome

Goal: finish shared home/search layout density, filter rail affordances, and
bottom/status chrome after higher-risk detail surfaces settle.

Target mocks:

- `artifacts/mocks/home-*.png`
- `artifacts/mocks/search-*.png`

Current proof paths:

- `screenshots/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/*/searchQueryShowsResultsWithoutShellPolling__search_results.png`

Acceptance:

- Home status/search/category/recent order, recent-row density, tablet/landscape
  spacing, and bottom-nav clearance match the home mocks.
- Search tablet filter rail uses real checkbox affordances and counts; phone
  and landscape search header/input density match the mocks.
- Recheck Home after Search changes because this phase owns shared chrome.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- Home/search chrome drawables under `android-app/app/src/main/res/drawable/`
  with prefixes `bg_home`, `bg_manual_home`, `bg_manual_phone_home`,
  `bg_tablet_home`, and `bg_search_shell`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`

### P7 - Emergency Content And Chrome Polish

Goal: preserve the close tablet emergency hierarchy and finish phone/tablet
content weight, warning card, and composer chrome.

Target mocks:

- `artifacts/mocks/emergency-phone-portrait.png`
- `artifacts/mocks/emergency-tablet-portrait.png`

Current proof paths:

- `screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- `screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`

Acceptance:

- Phone and tablet show danger header, summary sentence, four immediate
  actions, `WHY THIS ANSWER`, one GD-132 proof card, and quiet emergency
  composer without normal answer/source graph takeover.
- At least two negative controls remain non-emergency.
- No emergency landscape target is introduced as acceptance evidence.
- If shell mounting, top frame, or composer behavior changes are needed,
  transfer to P1 before editing.

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

### P8 - Closure Review

Owner: final integration/review lane. No standing implementation write set.
Any final fix must first claim the owning P1-P7 file set.

Closure acceptance:

- Full pack status `pass`, 47/47 states.
- Homogeneous APK SHA across all four roles.
- Homogeneous model name/SHA for host-backed roles.
- Rotation mismatch count 0.
- Screenshot plus XML dump review against all 22 target mocks.
- Any accepted delta is named as content/data, layout/density, or behavior.

## Parallelization Rules

- Start every worker with `git status --short`, `git log --oneline -n 8
  --decorate`, and `git diff --stat`.
- One active worker per phase write set. If a live dirty file appears in
  another phase's write set, resequence instead of sharing it.
- P1 must lead when a fix changes detail mode routing, tablet ownership, detail
  layouts, shared composer, or evidence/source rails.
- P2, P3, P4, P5, and P7 may run in parallel only if they stay inside their
  disjoint write sets and do not need P1/P6 files.
- P6 should run after P5 unless the only search work left is shared shell or
  filter rail.
- P8 starts only after each target mock has a current screenshot/dump verdict.

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

Closure pack:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\<closure_pack> -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```

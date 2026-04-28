# Android Visual Review - Wave 7 State Pack Triage

State pack: `artifacts/ui_state_pack/20260427_225149`

## Scope

Read:

- `summary.json`
- `manifest.json`
- failing raw `summary.json`
- failing `instrumentation.txt`
- failing `logcat.txt`
- captured failing dumps/screenshots where present

No production code was edited.

## Pack Health

- Overall status: `partial`
- Total states: 47
- Passed: 41
- Failed: 6
- Platform ANRs: 0
- Matrix homogeneous: true
- APK SHA: `a320d03d5449e2d2eecf50d94d1a966ce942e9352618410fd647cc9780b9efde`
- Model: `gemma-4-e2b-it-litert`
- Model SHA: `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`
- Installed pack metadata is present and homogeneous across devices.
- Viewport facts show no rotation mismatches.

The manifest-level failure reason for all six is `smoke wrapper failed before trusted summary`, but the raw folders show ordinary JUnit assertion failures, not crashes, ANRs, install/cache mismatches, or device identity problems.

## Failure Triage

| # | Role / device | Test | Exact assertion / failure | Classification | Next owner files |
|---|---|---|---|---|---|
| 1 | `tablet_landscape` / `emulator-5558` | `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` | `linked-guide handoff never became ready; waitReady=false, posture=2, adapterCount=4, childCount=4, firstPreviewPosition=0, visiblePreview='', visibleCue='', visibleSurface='... SEARCH Abrasives Manufacturing - 4 results ...'; harness signals=idle` | App visual/copy contract. Search results render, but the linked-guide cue/preview surface is hidden or never becomes discoverable on tablet landscape. | `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`, `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`, `android-app/app/src/main/res/layout/list_item_result.xml`, then test contract in `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java` if tablet cue suppression is intentional. |
| 2 | `tablet_portrait` / `emulator-5554` | `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` | Same assertion, with `posture=1`, `adapterCount=4`, `childCount=4`, empty `visiblePreview` and `visibleCue`; captured state remains on the search-results list. | App visual/copy contract. Same linked-guide handoff readiness gap as tablet landscape, reproduced in tablet portrait. | Same as #1. Also inspect tablet/compact result-card rules in `SearchResultAdapter.bindLinkedGuideCue(...)`. |
| 3 | `phone_landscape` / `emulator-5560` | `generativeAskWithHostInferenceNavigatesToDetailScreen` | `visible provenance panel should keep provenance wording visible` from `assertGeneratedTrustSpineSettled(...)`. Raw summary has 0 screenshots and 0 dumps because the assertion fired before capture. | Visual/copy. The detail surface reaches answer mode, but a visible provenance panel has meta text that does not include expected provenance/source-preview wording. | `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`, `android-app/app/src/main/res/layout-land/activity_detail.xml`, `android-app/app/src/main/res/values/strings.xml`, and provenance assertion in `PromptHarnessSmokeTest.java` if the accepted wording changed. |
| 4 | `phone_portrait` / `emulator-5556` | `guideDetailShowsRelatedGuideNavigation` | `preview title should identify the selected linked guide` at `PromptHarnessSmokeTest.java:1599`. Raw summary has 0 screenshots and 0 dumps because assertion fired inside `scenario.onActivity(...)` before capture. | Visual/copy. Preview-first related-guide panel exists enough for the test path, but the preview title no longer contains `selected linked guide`. | `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`, `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`, `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`, `android-app/app/src/main/res/layout/activity_detail.xml`, `strings.xml`. |
| 5 | `phone_landscape` / `emulator-5560` | `guideDetailShowsRelatedGuideNavigation` | Same assertion as #4: `preview title should identify the selected linked guide`. | Visual/copy. Same related-guide preview-title regression/contract mismatch, reproduced on phone landscape. | Same as #4, plus `android-app/app/src/main/res/layout-land/activity_detail.xml`. |
| 6 | `tablet_portrait` / `emulator-5554` | `emergencyPortraitAnswerShowsImmediateActionState` | `emergency portrait should keep source or handoff context visible` at `PromptHarnessSmokeTest.java:1111`. Logcat shows `detail.intent title="Unknown poison swallowed" answerMode=true sources=1 ...` and `tablet_three_pane mounted orientation=port turns=1 sources=1 xrefs=0`, then the assertion fails. | App layout/visual. The emergency answer and source exist, but the legacy `detail_sources_panel` visibility contract is not met in tablet portrait three-pane mode. | `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`, tablet Compose/three-pane detail rendering in the same file, `android-app/app/src/main/res/layout/activity_detail.xml`, `strings.xml`, and the test assertion if tablet portrait should validate Compose source context instead of `detail_sources_panel`. |

## Raw Artifact Pointers

- Tablet landscape linked handoff: `raw/20260427_225241_755/emulator-5558/`
  - Captured `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff_not_ready.png`
  - Captured dump confirms search results are visible and populated, not a blank/state-load failure.
- Tablet portrait linked handoff: `raw/20260427_225244_079/emulator-5554/`
  - Captured `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff_not_ready.png`
  - Dump shows populated results including `GD-220` / `Abrasives Manufacturing`, but no discoverable linked-guide cue/preview.
- Phone landscape generative provenance: `raw/20260427_225330_890/emulator-5560/`
- Phone portrait related-guide preview title: `raw/20260427_225356_701/emulator-5556/`
- Phone landscape related-guide preview title: `raw/20260427_225416_390/emulator-5560/`
- Tablet portrait emergency source context: `raw/20260427_225517_404/emulator-5554/`

## Recommended Ownership

1. Assign #1 and #2 to the search-result card / browse handoff owner. The key question is whether tablet result cards should expose a linked-guide action, or whether the harness should use a tablet-specific handoff path. Current code paths suggest rich tablet cards suppress the linked cue before it can become ready.
2. Assign #3, #4, and #5 to detail visual/copy ownership. These are wording/visibility contract failures around provenance and related-guide preview identity.
3. Assign #6 to tablet detail layout ownership. The emergency source exists, but tablet portrait three-pane mode does not satisfy the legacy `detail_sources_panel` visibility assertion.

No harness infrastructure failure is indicated beyond the state-pack wrapper converting failed instrumentation runs into `smoke wrapper failed before trusted summary`.

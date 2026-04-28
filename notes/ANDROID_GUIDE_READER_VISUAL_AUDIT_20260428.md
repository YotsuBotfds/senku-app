# Android Guide Reader Visual Audit - 2026-04-28

Worker: Guide-Audit. Scope: analysis only. Android source was not edited.

## Inputs

- Target mocks:
  - `artifacts/mocks/guide-phone-portrait.png`
  - `artifacts/mocks/guide-phone-landscape.png`
  - `artifacts/mocks/guide-tablet-portrait.png`
  - `artifacts/mocks/guide-tablet-landscape.png`
- Current proof pack:
  - `artifacts/ui_state_pack/20260428_010423`
  - Status `pass`, 47 states, 47 pass, 0 fail, 0 ANR.
- Guide screenshots reviewed:
  - `screenshots/phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
  - `screenshots/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
  - `screenshots/phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
  - `screenshots/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
  - `screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
  - `screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
  - `screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
  - `screenshots/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`

## Current Implementation Shape

- Phone/XML guide mode is still rendered through `DetailActivity.java` and
  `activity_detail.xml`. `renderDetailState()` feeds guide text into
  `buildStyledGuideBody()` and then calls `applyGuideReaderPresentation()`
  (`DetailActivity.java:1046-1155`, `6546-6589`).
- Guide body parsing/styling is centralized in
  `DetailGuidePresentationFormatter.java`, with section markers introduced by
  `buildGuideBody()` and restyled by `buildStyledGuideBody()`
  (`DetailGuidePresentationFormatter.java:62-66`, `81-116`, `194-225`).
- Related/cross-reference copy is centralized in
  `DetailGuideContextPresentationFormatter.java` and rendered through
  `DetailActivity` panels (`DetailActivity.java:5865-6141`).
- Tablet guide mode is Compose-backed. `TabletDetailScreen` always lays out a
  `ThreadRail`, center workspace, and evidence pane; guide mode swaps the
  center content to `GuidePaperSurface` but keeps the surrounding thread/source
  model (`TabletDetailScreen.kt:303-379`, `396-427`, `476-523`).
- Tablet paper content is currently built from `ThreadTurnList` /
  `ThreadTurnBlock`, not a first-class section model
  (`TabletDetailScreen.kt:500-510`, `619-655`, `791-1045`).
- Tablet left rail is still labeled `THREAD` / `SOURCES`
  (`ThreadRail.kt:68-98`). Tablet right rail is the evidence cross-reference
  section (`EvidencePane.kt:323-384`).

## Top Gaps

1. Phone portrait still has an oversized dark detail header and large field
   header card above the paper. The target has compact top chrome and the cream
   manual page starts immediately below it. Evidence:
   `phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`.

2. Phone paper typography is too large and too much raw body text appears above
   the first section. The target shows a compact danger callout followed by
   `Section 1 Area readiness` and required-reading rows within the first
   viewport.
   Current phone portrait only reaches the start of the section label, and the
   section body/related rows are pushed below the fold.

3. Section/admonition normalization diverges between XML phone and Compose
   tablet. XML phone converts section markers to styled labels, but tablet
   screenshots expose raw `[[SECTION]]` text and mojibake section-symbol text
   in Compose. Evidence:
   `tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
   and `tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`.

4. Tablet guide surfaces still carry answer/thread semantics. The target tablet
   landscape left rail is `SECTIONS - 17`; current left rail says `THREAD - 1`
   and `SOURCES - 1`. The current center page is paper-like, but its content is
   a converted answer/thread block rather than the target manual section layout.

5. Tablet portrait should be a wide guide reader with a sections rail and paper
   document focus. Current tablet portrait adds a right cross-reference rail,
   making the reader feel like answer evidence mode and narrowing the paper
   page relative to the target.

6. Right cross-reference rail is close in spirit but not contract-accurate.
   The target tablet landscape shows `CROSS-REFERENCE - 6`, with GD-220 as
   anchor and selected related/required rows. Current evidence shows
   `CROSS-REFERENCE - 7`, with GD-132 as anchor in the source-context
   screenshot.

## Next Implementation Slices

1. Phone reader density and header slice.
   Own:
   - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
   - `android-app/app/src/main/res/layout/activity_detail.xml`
   - `android-app/app/src/main/res/layout-land/activity_detail.xml`
   - guide paper drawables under `android-app/app/src/main/res/drawable/bg_detail_guide_paper*.xml`
   Verify against:
   - `guide-phone-portrait.png`
   - `guide-phone-landscape.png`
   - `phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
   - `phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
   Target outcome: compact top chrome, no dominant field-header card, smaller
   paper body type, danger block plus `Section 1` visible above the fold.

2. Shared guide body section model slice.
   Own:
   - `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
   - `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
   - `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`
   - `android-app/app/src/test/java/com/senku/mobile/CopySanitizerTest.java`
   - new `com.senku.ui.guide` tests only if a Compose model is introduced
   Verify against all four guide screenshots and XML dumps.
   Target outcome: one reusable parsed guide-section/callout representation so
   Compose does not display `[[SECTION]]` or mojibake section-symbol text.

3. Tablet sections rail slice.
   Own:
   - `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
   - `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`, or a
     new `android-app/app/src/main/java/com/senku/ui/guide/GuideRail.kt`
   - `android-app/app/src/test/java/com/senku/ui/tablet/StressReadingPolicyTest.kt`
   Verify against:
   - `guide-tablet-portrait.png`
   - `guide-tablet-landscape.png`
   - `tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
   - `tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
   Target outcome: guide mode left rail says `SECTIONS - 17` and lists section
   anchors instead of `THREAD` / `SOURCES`.

4. Tablet paper content slice.
   Own:
   - `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
   - optional new `android-app/app/src/main/java/com/senku/ui/guide/GuideReaderScreen.kt`
   - optional new `android-app/app/src/test/java/com/senku/ui/guide/*Test.kt`
   Verify against all tablet guide screenshots.
   Target outcome: center paper renders manual header, compact danger callout,
   section title/body paragraphs, and required-reading rows rather than
   answer-style `AnswerContent` blocks.

5. Cross-reference rail contract slice.
   Own:
   - `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
   - `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
   - `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`
   - `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`
   Verify against:
   - `guide-tablet-landscape.png`
   - `tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
   Target outcome: guide mode right rail matches the mock's count and relation
   labels, especially GD-220 anchor vs GD-132 current-page context.

## File-Lock Notes

Current dirty files include `DetailActivity.java`,
`TabletDetailScreen.kt`, `ThreadRail.kt`, `EvidencePane.kt`, and related tests.
Do not start the slices above in parallel unless the active owner of those
dirty files also owns the slice.

## Validation Gate

Run focused guide unit tests plus assemble, then produce a focused guide state
pack or full fixed-matrix pack. The handoff should cite the exact four guide
mock names and the exact screenshot names listed above, and should confirm that
guide dumps still keep guide semantics separate from answer semantics.

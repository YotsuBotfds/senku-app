# Android Visual Review - 2026-04-27 Wave 5 Guide/Emergency

Reviewer: R3 guide/emergency visual audit only  
State pack: `artifacts/ui_state_pack/20260427_223445`  
Mocks: `artifacts/mocks`  
Scope: guide reader and emergency portrait surfaces only. No production code edited.

## Pack Status

- `summary.json` reports overall status `partial`: 45 states, 38 pass, 7 fail, 0 platform ANRs.
- Device matrix is homogeneous for APK/model identity.
- Fresh screenshots exist for several guide-adjacent surfaces, but some target guide states failed before trusted summary and produced no screenshot.
- No fresh emergency-specific portrait screenshot was found in this state pack.

## Target Results

| Target mock | Result | Fresh artifact screenshots reviewed |
| --- | --- | --- |
| `artifacts/mocks/guide-phone-portrait.png` | Partial | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/homeGuideIntentShowsGuideConnectionContext__home_guide_connection_context.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/answerModeProvenanceOpenRemainsNeutral__answer_provenance_neutral.png` |
| `artifacts/mocks/guide-phone-landscape.png` | Partial | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/homeGuideIntentShowsGuideConnectionContext__home_guide_connection_context.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/deterministicAskNavigatesToDetailScreen__landscape_focus_ready.png` |
| `artifacts/mocks/guide-tablet-portrait.png` | Partial | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/homeGuideIntentShowsGuideConnectionContext__home_guide_connection_context.png` |
| `artifacts/mocks/guide-tablet-landscape.png` | Partial | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_landscape/homeGuideIntentShowsGuideConnectionContext__home_guide_connection_context.png` |
| `artifacts/mocks/emergency-phone-portrait.png` | Fail | No emergency-specific fresh portrait screenshot found. Closest inspected portrait answer surface: `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/answerModeProvenanceOpenRemainsNeutral__answer_provenance_neutral.png` |
| `artifacts/mocks/emergency-tablet-portrait.png` | Fail | No emergency-specific fresh tablet portrait screenshot found. Closest inspected guide/answer surfaces: `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`; `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png` |

## Top Remaining UI Deltas

- Guide reader chrome is still not matching the mocks. The mocks show a compact app bar, paper-like centered guide document, section navigation, and cross-reference rail. Fresh phone captures instead show oversized answer/detail chrome, large rounded icon clusters, and a large gradient field header card.
- Phone portrait guide content is legible and anchored to the correct GD-132/GD-220 context, but the document width, typography scale, header treatment, and paper framing differ sharply from `guide-phone-portrait.png`.
- Phone landscape guide capture is especially far from target: the field header spans nearly the full viewport and the paper body is clipped low in the frame, while the mock expects a structured landscape reader with clear nav/rail balance.
- Tablet landscape captures preserve cross-reference/source context, but the fresh state appears as an answer/detail split with dark reading canvas instead of the mock's centered cream guide page with left sections and right cross-reference rail.
- Tablet portrait guide captures are closer in content availability than phone failures, but still use current detail rendering rather than the mock's guide-document hierarchy and compact top navigation.
- Emergency portrait surfaces cannot be visually accepted from this pack. The emergency mocks require a distinct danger answer layout with immediate action steps and emergency context input, but no fresh emergency target capture exists for either phone portrait or tablet portrait.

## Harness Failures Relevant To Scope

All relevant failures in `manifest.json` report `smoke wrapper failed before trusted summary` and produced no trusted screenshots:

- `phone_landscape / guideDetailShowsRelatedGuideNavigation`
- `phone_landscape / guideDetailUsesCrossReferenceCopyOffRail`
- `phone_portrait / deterministicAskNavigatesToDetailScreen`
- `phone_portrait / guideDetailShowsRelatedGuideNavigation`
- `phone_portrait / guideDetailUsesCrossReferenceCopyOffRail`
- `tablet_landscape / searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`
- `tablet_portrait / searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`

These failures directly reduce confidence for guide reader parity, especially phone direct guide detail states and cross-reference/off-rail guide states. No platform ANR was reported.

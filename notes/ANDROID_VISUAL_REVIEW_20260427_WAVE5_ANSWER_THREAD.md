# Android Visual Review - 20260427 Wave 5 - Answer/Thread

Reviewer: R2
Scope: visual audit only, answer detail and follow-up thread surfaces across phone/tablet portrait/landscape.
Fresh state pack: `artifacts/ui_state_pack/20260427_223445`
Target mocks: `artifacts/mocks`

## Pack Status

- `summary.json` status: partial.
- Total states: 45; pass: 38; fail: 7.
- No platform ANRs reported.
- Matrix homogeneous: APK SHA `ad8f7401fd7f8aa000d2115a1a46174b2ae1472734ad51abadccd7ef8d4112b0`, model `gemma-4-e2b-it-litert`, model SHA `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
- Relevant harness gap: `phone_portrait/deterministicAskNavigatesToDetailScreen` failed before trusted summary, so phone portrait answer-detail coverage depends on the generative detail screenshot rather than the deterministic answer target.

## Per-Target Results

| Target mock | Fresh screenshot reviewed | Result | Notes |
| --- | --- | --- | --- |
| `artifacts/mocks/answer-phone-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Partial | Core answer state is present, but the fresh UI uses a large field-question hero/card layout, oversized top controls, and horizontal suggestion chips. It does not match the mock's compact top bar, inline answer body, visible source cards, related guides, and bottom context rail. |
| `artifacts/mocks/thread-phone-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Partial | Two-turn thread and composer are present. Fresh surface is much more card-heavy, with Q1/A1 rendered as large rounded blocks and Q2 answer detail pushed below the fold; the mock expects a flatter inline transcript with both turns visible and source chips integrated. |
| `artifacts/mocks/answer-phone-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Fail | Fresh screenshot is a full-width debug/field-entry answer card with very large controls and no right source rail in view. The mock expects a split answer/source layout with compact chrome and the answer body visible beside sources. |
| `artifacts/mocks/thread-phone-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Fail | Thread state exists, but fresh layout shows only the first Q/A cards plus composer at landscape height. The mock expects the active turn, sources rail, and denser thread transcript in a compact split layout. |
| `artifacts/mocks/answer-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Partial | Tablet portrait has persistent left rail and right cross-reference rail, but it is the alternate answer-thread shell rather than the mock answer detail. Content is oversized, duplicated guide names dominate, and the concise answer with source cards/related guides from the mock is not visible. |
| `artifacts/mocks/thread-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Partial | Two turns, left thread rail, right cross-reference rail, and composer are present. Compared with the mock, the fresh screen is looser and more verbose, with extra answer-detail sections and much less of the expected compact thread/source arrangement visible above the fold. |
| `artifacts/mocks/answer-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` | Partial | Three-column structure is recognizable, but middle content is oversized and shifted into a verbose generative answer format. The mock's tighter answer detail, source list density, and related-guide balance are not achieved. |
| `artifacts/mocks/thread-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png` | Partial | Layout broadly matches the tablet landscape thread concept: left turn rail, center thread, right sources, bottom composer. Remaining deltas are density and hierarchy: large typography, excessive vertical spacing, and partially clipped second answer at the bottom. |

## Top Remaining UI Deltas

1. Answer detail is not consistently using the target mock shell. Fresh phone/tablet screenshots often show a debug-like field-entry/generative answer presentation with large hero text, large rounded controls, and verbose duplicated guide text instead of the compact answer body plus source/related-guide structure.
2. Phone landscape is the furthest from target. Both answer and thread miss the mock's dense split-pane treatment and instead show oversized content that leaves key source/detail regions absent or below the fold.
3. Thread surfaces preserve the semantic state but not the mock density. Q/A history appears, yet cards, typography, and spacing are much larger than target, causing active answers, source chips, and proof controls to fall off-screen.
4. Tablet layouts keep rails, but the center column over-expands typography and leaves large unused dark areas, especially on thread screens.
5. Mock top chrome is not matched. Fresh screenshots use prominent grouped navigation/share/home controls and native status bars that visually dominate compared with the target's slim app bar treatment.
6. Source rail/cross-reference content differs from mocks in labels and density. Fresh rails often show `CROSS-REFERENCE - 7` and unrelated-looking related guide lists, while mocks expect concise `SOURCES` cards and related guides tied tightly to the answer.

## Harness Failures Relevant To Answer/Thread

- `phone_portrait/deterministicAskNavigatesToDetailScreen`: fail, `smoke wrapper failed before trusted summary`; no screenshot or dump. This leaves a direct phone portrait deterministic answer-detail hole.
- Other answer/thread-focused states reviewed here passed and produced screenshots:
  - `generativeAskWithHostInferenceNavigatesToDetailScreen` passed for phone portrait, phone landscape, tablet portrait, tablet landscape.
  - `scriptedSeededFollowUpThreadShowsInlineHistory` passed for phone portrait, phone landscape, tablet portrait, tablet landscape.
  - `answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane` and `answerModeProvenanceOpenRemainsNeutral` passed for all four roles, but those are source/provenance variants rather than the primary answer/thread mocks.


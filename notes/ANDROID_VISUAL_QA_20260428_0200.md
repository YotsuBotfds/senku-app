# Android Visual QA - 2026-04-28 02:00

Role: Visual-Delta-Reviewer. Scope: analysis only; no Android source edits.

Reviewed latest proof pack `artifacts/ui_state_pack/20260428_015154` against
`artifacts/mocks`, with prior notes `ANDROID_VISUAL_QA_20260428_0140.md`,
`ANDROID_GUIDE_READER_VISUAL_AUDIT_20260428.md`, and
`ANDROID_ANSWER_VISUAL_MODE_AUDIT_20260428.md` as trend context.

## Wave12 Delta

1. Guide tablet improved: the left rail now says `SECTIONS` and guide chrome
   says `GUIDE`, replacing the most obvious answer/thread rail mismatch. This
   is the clearest forward movement from the prior guide audit.
2. Guide tablet still is not mock-close: it shows `SECTIONS - 1`, not `17`;
   the center paper still exposes raw `[[SECTION]]`; the danger block is plain
   long-form source text, not the compact callout plus section hierarchy shown
   in `guide-tablet-landscape.png`.
3. Guide phone did not materially improve. It still opens as oversized source
   text with a dominant dark header and no first-viewport required-reading rows.
   The mock starts with compact guide chrome, field-manual metadata, a compact
   danger callout, Section 1, and required-reading rows.
4. Search tablet improved structurally: filter/result/preview columns are
   present and closer to `search-tablet-landscape.png` than the old generic
   list shell.
5. Search content and density regressed or remain off-contract: actual ranks
   `GD-345` first for rain shelter, while the mock ranks `GD-023` first; result
   cards are wider/looser; the preview uses markdown/source-style text and
   linked-guide copy rather than the tighter mock preview.

## Remaining Blockers By Next Implementation Value

1. **Guide body model/parsing.** Normalize guide content before both XML and
   Compose render: no raw `[[SECTION]]`, no all-caps source dumps, no one-section
   rail when the target guide has 17 sections.
2. **Phone guide density/header.** Reduce the dark header and serif body scale
   so the first viewport matches the mock hierarchy: field-manual header,
   compact danger callout, Section 1, and required-reading rows.
3. **Tablet answer-first layout.** Answer states are still a three-pane
   thread/source/cross-reference workbench. The answer should be the dominant
   card; proof/source rails should be collapsed or explicitly proof-scoped.
4. **Tablet emergency sizing.** Emergency tablet still uses a broad overlay over
   the answer shell with background rails visible. It should become an inline,
   proportionate emergency treatment in the tablet page model.
5. **Search contract polish.** Keep the improved three-column tablet shape, but
   align result ordering/content, tighter typography, checkbox/filter styling,
   and preview copy with the mocks.

## Notes For Next Slice

- Treat `20260428_015154` as a valid mechanical proof pack: `47/47` pass,
  homogeneous APK/model, no ANR, installed pack `senku-mobile-pack-v2` with
  `271` answer cards.
- Do not let green state assertions stand in for visual parity. The largest
  gaps are now visual contract gaps, not harness stability gaps.
- The guide tablet rail improvement is worth preserving; the next work should
  deepen that into a real guide section model rather than returning to shared
  answer/thread surfaces.

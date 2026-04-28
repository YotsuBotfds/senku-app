# Android Visual QA - 2026-04-28 01:15

Compared `artifacts/ui_state_pack/20260428_010423/screenshots` against `artifacts/mocks` for home, search, thread, answer, guide, and emergency targets. Analysis only; no Android source touched.

## Top blockers

1. **Answer screen is frequently the wrong visual mode.** Representative `answerModeProvenanceOpenRemainsNeutral__answer_provenance_neutral.png` captures show a huge source/document card with the header "Field manual" / "You're looking at the source text directly" instead of the mocked answer layout with prose answer, source rail, related guides, and follow-up composer. This is severe on phone portrait/landscape and changes the entire screen identity.

2. **Guide detail landscape is clipped and source-like instead of mock-like.** Phone landscape actual shows a large dark "FIELD HEADER" block and a beige document pane cut off at the bottom, while the mock is a readable guide article page with stable cream body and related navigation. The actual loses much of the guide composition and looks like a provenance/source viewer.

3. **Emergency tablet portrait has an oversized danger overlay.** Actual tablet portrait places a large red/brown immediate-action panel over the answer/thread layout and leaves broad empty columns below; the mock keeps the emergency flow compact, inline, and mostly dark green. Phone portrait is closer, but still visually heavier and more modal than the target.

4. **Thread tablet layouts are structurally different from mocks.** Actual tablet portrait/landscape uses denser multi-pane columns with side lists and a shifted conversation area; mocks emphasize a cleaner central thread with a simpler right source rail. This is less catastrophic than answer/guide, but still a clear target mismatch.

5. **Home/search are closest but still off in density and responsive framing.** Search generally lands in the right family, especially landscape/tablet, though spacing and rails differ from the mocks. Home has the expected browse/ask lanes, but portrait and tablet portrait feel too compressed/sparse compared with the mock proportions.

## Best next visual targets

- Fix answer/guide mode selection or state capture first; many severe mismatches appear to be source/provenance surfaces replacing the intended answer/guide targets.
- Then normalize emergency tablet portrait so the danger treatment stays within the target inline composition rather than becoming a dominant overlay.

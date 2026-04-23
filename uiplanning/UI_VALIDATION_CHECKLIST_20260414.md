# UI Validation Checklist - 2026-04-14

Use this checklist after each UI slice that changes answer-thread or results-screen density.

## Device/Posture Matrix

| Lane | Device | Orientation | Deterministic Prompt | Streaming Prompt |
|------|--------|-------------|----------------------|------------------|
| Phone portrait | `RFCX607ZM8L` | Portrait | `How do I start a fire in rain?` | `How do I build a simple rain shelter from a tarp and cord?` |
| Phone landscape | `emulator-5558` | Landscape | `How do I start a fire in rain?` | `How do I organize a 2-day emergency food bag?` |
| Tablet clipboard | `R92X51AG48D` | Portrait | `How do I start a fire in rain?` | `How do I boil water safely without a pot?` |
| Tablet station | `emulator-5560` | Landscape | `How do I start a fire in rain?` | `How should I pack a field medical kit for a two-day hike?` |

## Required Checks

- [ ] Header trust spine visible (`route`, `evidence`, `src`, `turn`, anchor chip)
- [ ] Answer body appears above fold quickly in phone portrait
- [ ] Hero behavior correct:
  - [ ] visible during pending/staged generation
  - [ ] collapsed after stable answer render on phone portrait
- [ ] Follow-up composer behavior:
  - [ ] compact on phone portrait/landscape
  - [ ] expanded where intended (tablet station only)
- [ ] Source and provenance behavior:
  - [ ] source chips/buttons are tappable
  - [ ] provenance preview updates in place
  - [ ] no background spill past rounded corners in landscape
- [ ] `Why this answer`:
  - [ ] collapsed affordance is visible
  - [ ] expands/collapses correctly

## Streaming-Specific Checks (local on-device inference)

- [ ] Incremental answer text updates are visible before final completion
- [ ] No `<pad>` token leakage in body text
- [ ] Word spacing is preserved during stream
- [ ] Busy status transitions:
  - [ ] `Using N guides while Senku builds the answer...`
  - [ ] `Streaming answer...` / `Streaming answer from N guides...`
  - [ ] `Offline answer ready in X s`

## Artifact Capture

For each lane, capture:

- [ ] top viewport screenshot
- [ ] mid-scroll screenshot
- [ ] deep-scroll screenshot
- [ ] ui dump (`.xml`) for top state

Suggested folder pattern:

- `artifacts/<lane>_<yyyymmdd>/top.png`
- `artifacts/<lane>_<yyyymmdd>/mid.png`
- `artifacts/<lane>_<yyyymmdd>/deep.png`
- `artifacts/<lane>_<yyyymmdd>/top_dump.xml`

## Exit Criteria

- [ ] `assembleDebug` passes
- [ ] deterministic and streaming checks both pass on physical tablet (`R92X51AG48D`)
- [ ] no regression in post-search escape controls (`Home` + `Ask From Guides`) on phone portrait

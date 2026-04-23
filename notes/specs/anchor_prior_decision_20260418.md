# Anchor-Prior Decision

Date: 2026-04-18

Decision:
- enable and harden anchor-prior

## Recommendation

Anchor-prior should ship as an intentional feature, not remain dead code and not be deleted.

## Why

- `query.py` still carries the full anchor-prior machinery, but the path is blocked by `ENABLE_ANCHOR_PRIOR = False`.
- The live machinery currently sits at:
  - `query.py:59` for the feature flag
  - `query.py:6795-7006` for anchor state recording and application
  - `query.py:7168` for the active merge path
- `OPUS-B-03` already reads adjacent anchor/session state, so the product is effectively half-committed already.
- Follow-up quality is a known weak spot, and this path is the existing leverage point rather than a speculative new system.

## Risks

- short deictic follow-ups may over-bias toward the previous anchor when the user actually intended a topic switch
- stale anchor carryover can survive session reuse after long idle gaps until the Android idle-reset lane is fully validated

## Required follow-on

1. `BACK-R-03`
   finish validation for the 1-hour idle reset so reused conversations clear stale anchor state
2. keep monitoring follow-up quality after enabling the feature on desktop

## Status

The decision is recorded and desktop code now matches it: `query.py` ships with `ENABLE_ANCHOR_PRIOR = True`, and Android now uses a typed `AnchorPriorDirective` parameter instead of routing through the old string sentinel in normal call paths.

`BACK-R-01` is closed. `BACK-R-03` remains the validation follow-on for stale-anchor reset.

# MetaStrip Confidence Token Addendum

Date: 2026-04-18

Purpose:
- isolate the confidence-token contract used by `BACK-U-03`
- keep the transport and UI rules separate from the broader uncertain-fit backlog

Source:
- derived from `notes/specs/wave_b_uncertain_fit_addenda_20260418.md`

## Contract

Intent:
- expose a compact confidence token for non-abstain answers
- keep abstain as its own route state instead of treating it like a confidence tier

State mapping:
- `high` -> no MetaStrip token
- `medium` -> `likely match` with default tone
- `low` -> `low confidence` with warn tone
- `abstain` -> no confidence token

Placement:
- render the token after the backend token
- render it before sources / turns / evidence counters

Transport:
- add `confidenceLabel` to the prepared/final answer payload
- preserve the label when a pending answer is reopened for resumed generation
- degrade gracefully when the label is missing

Prompt contract:
- append `The answer confidence is <label>.`
- for `low`, append `If confidence is low, note the gap in the first sentence.`

Android notes:
- do not show the token on the abstain route
- keep the rest of `MetaStrip` ordering unchanged
- allow high / missing labels to behave exactly like the legacy strip

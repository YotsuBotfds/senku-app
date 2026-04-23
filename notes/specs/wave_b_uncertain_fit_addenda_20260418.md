# Wave B Uncertain-Fit Addenda

Date: 2026-04-18

Purpose:
- capture the Wave B scouting outputs that are blocked behind `OPUS-E-05`
- keep the uncertain-fit UI and copy contract ready before `BACK-U-01` through `BACK-U-03` open

## MetaStrip Confidence Token

Intent:
- add a compact confidence token to `MetaStrip` for non-abstain low-confidence states

Suggested token labels:
- `likely match`
- `low confidence`
- `uncertain fit`

State mapping:
- `high` -> no token
- `medium` -> optional `likely match`
- `low` -> `low confidence` with warn tone
- `uncertain-fit` -> `uncertain fit` with warn tone
- `abstain` -> no confidence token; abstain route state remains authoritative

Android contract notes:
- add a `confidenceLabel` field to the answer payload
- render the token in `buildRev03MetaStripItems()` after the backend token and before evidence/source counters
- use `Tone.Warn` for `low` and `uncertain-fit`
- preserve legacy strip behavior if the label is missing
- include the token in MetaStrip accessibility summaries

Desktop contract notes:
- expose a stable confidence tag in answer metadata
- keep abstain distinct from confidence tokens
- degrade gracefully on unknown labels

## PaperAnswerCard Uncertain-Fit Variant

New visible state:
- `uncertain_fit` when confidence metadata is low but `abstain == false`

Visual distinctions:
- keep the standard `SENKU ANSWERED` base title for non-abstain cards
- switch the evidence pill to `LOW CONFIDENCE` or `UNSURE FIT`
- use a muted warning wash and a left warning border
- avoid the abstain danger wash or danger border

Body posture:
- first sentence should say the guidance is related but not a confident fit
- avoid final or decisive framing
- allow steps only when present, but bias the wording toward verifying applicability before acting
- keep `Show proof` and source/provenance behavior unchanged

Evidence handling:
- do not infer uncertain-fit from source count alone
- drive it from explicit confidence metadata
- treat `low` plus non-abstain as uncertain-fit
- keep the abstain path unchanged

Implementation notes:
- add an explicit uncertainty field to the answer content model instead of overloading `abstain`
- update `AnswerContentFactory.fromRenderedAnswer` to honor the new metadata
- keep MetaStrip warning-capable and avoid string-heuristic detection in `DetailActivity`

## Safety-Critical Escalation Copy

Preferred line:
- `If this is urgent or could be a safety risk, stop and call local emergency services now (911 where applicable); if this may be poisoning, call Poison Control now, and keep the person with a trusted adult while waiting.`

Alternate lines:
- `If someone may be in immediate danger, treat this as a crisis: keep them with a trusted adult, remove obvious risks, and call emergency services right now; for ingestion/exposure, call Poison Control.`
- `If this feels urgent or unclear, do not proceed on this guidance alone; call emergency services now (local number or 911). For suspected poisoning, add Poison Control before continuing.`

Placement and constraints:
- gate this line to safety-critical abstain or uncertain-fit only
- append it above the `Closest matches` block
- keep abstain copy free of retrieval-mechanism wording
- apply the same line in both `query.py` and `OfflineAnswerEngine.java`

## Ready-When-Unblocked

When `OPUS-E-05` closes:

1. `BACK-U-03` should add the confidence field and MetaStrip token path first.
2. `BACK-U-01` should add the uncertain-fit PaperAnswerCard state driven by that metadata.
3. `BACK-U-02` should append the safety-critical escalation line only on the relevant low-applicability states.
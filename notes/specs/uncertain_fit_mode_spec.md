# Spec: Uncertain-Fit Answer Mode

Date: 2026-04-19
Task: `BACK-U-01`

## Purpose

- add a third answer mode between confident generation and abstain
- preserve the abstain floor while giving related-but-not-trustworthy retrieval a deterministic response path

## Mode Contract

- `confident`: normal generated answer path
- `uncertain_fit`: deterministic card that frames the retrieved guides as related context, not a confident answer
- `abstain`: existing no-match path; unchanged below the abstain floor

## Trigger Rules

Evaluate in this order:

1. If the existing abstain gate fires, return `abstain`.
2. Otherwise return `uncertain_fit` if any of the following are true:
   - normalized average top-row RRF strength is `< 0.65`
   - top-row vector similarity is in `[0.45, 0.62]`
   - the scenario frame is safety-critical, there is no primary-owner support match in the retrieval review, and the confidence label is `medium` or `low`
3. Otherwise return `confident`.

## Signal Notes

- Desktop normalized RRF strength is computed from the existing `_rrf_score` metadata and clamped to a `0.0..1.0` range before averaging.
- Android mirrors the same branch names and threshold meanings using existing retrieval-order, overlap, metadata-profile, and confidence-label signals already available in `OfflineAnswerEngine`.
- Primary-owner support match means the retrieval review has direct support or covered-objective evidence for the safety-critical query family; do not add a second classifier.

## Uncertain-Fit Body Template

Use this exact structure:

1. `Senku found guides that may be relevant to "{query}", but this is not a confident fit.`
2. blank line
3. safety-critical escalation line, only when the existing U-02 helper says to show it
4. blank line
5. `Possibly relevant guides in the library:`
6. bullet list of top related guides using the existing guide id / title / category / match-label format
7. blank line
8. `Try:`
9. `- checking whether the guide matches the exact person, symptom, tool, or setting`
10. `- asking a narrower follow-up with the exact detail that is missing`
11. `- treating the guides above as related context, not a final answer`

## Copy Constraints

- first sentence must explicitly say the fit is not confident
- do not call the listed guides "the answer"
- keep escalation above the related-guides block
- keep abstain copy and uncertain-fit copy distinct

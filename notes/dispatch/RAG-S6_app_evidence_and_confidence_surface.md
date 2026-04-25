# Slice RAG-S6 - app evidence and confidence surface

- **Role:** main agent (`gpt-5.5 medium`; use high only for Android answer-routing changes).
- **Paste to:** after diagnostics/profile work identifies stable confidence fields to surface.
- **Why this slice now:** the app should help users understand whether an answer is strongly guide-supported, weak, urgent, or needs clarification.

## Outcome

Design and implement a compact evidence/confidence surface for guide answers.

## Current S6a Status

Analyzer-only app acceptance fields now exist:

- `app_acceptance_status`
- `app_acceptance_reason`
- `evidence_owner_status`
- `safety_surface_status`
- `ui_surface_bucket`

Current best artifact: `artifacts/bench/rag_diagnostics_20260424_1410_child_choking_gate/report.md`.

Current app acceptance counts:

- `strong_supported`: `15`
- `moderate_supported`: `7`
- `uncertain_fit_accepted`: `2`

The 14:10 proof has `0` retrieval, ranking, generation, or safety-contract misses and expected-owner citation `24/24`. Answer-card diagnostics are `15` no-generated-answer, `7` partial, `2` pass; claim support is `15` no-generated-answer, `9` pass. Remaining app work is card-clause normalization, generated-answer structure, and source-content hygiene, not more retrieval/ranking changes for EX/EY/EZ/FC.

## Candidate UI Signals

- "Based on" guide titles.
- Support strength.
- Urgent red-flag banner when safety profile triggers.
- "I need one detail" state for weak support.
- Citation/why drawer for guide IDs and first-action basis.

## Likely Touch Set

- Android answer model / rendering files
- desktop answer metadata if needed
- tests around rendered confidence/evidence state

## Acceptance

- Users can distinguish confident guide support from weak support.
- Urgent answers show the emergency-first basis.
- Weak-support answers avoid looking like confident instructions.
- Desktop/app artifacts record acceptance counts by `answer_mode`, `decision_path`, `support_strength`, and `app_gate_status`.
- Analyzer records app acceptance counts by evidence owner, safety surface, and UI surface bucket.
- Immediate uncertainty cards are accepted when weak support still includes an emergency safety line and no generation call.
- Strong/confident safety answers require expected/card evidence-owner support.

## Anti-Recommendations

- Do not add visual clutter to every answer.
- Do not expose raw debug jargon.
- Do not let confidence labels reduce safety escalation when a red flag is present.

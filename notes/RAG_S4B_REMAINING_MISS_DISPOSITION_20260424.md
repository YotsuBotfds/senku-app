# RAG S4b Remaining-Miss Disposition - 2026-04-24

Input artifact:

- `artifacts/bench/rag_diagnostics_20260424_1240_fresh_answer_card_family_contract`

This table separates actual runtime defects from card or prompt-contract issues before any more rerank tuning.

| Row | Prompt | Expected owners | Top retrieved owners | Cited owners | Card | Disposition | Next action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| EX #5 | drooling and cannot swallow after a bite of food | `GD-232`, `GD-579`, `GD-298`, `GD-284`, `GD-617` | `GD-898`, `GD-400`, `GD-911`, `GD-284`, `GD-916`, `GD-939` | `GD-898`, `GD-911` | `choking_airway_obstruction` / fail | Mixed: ranking defect, citation-owner defect, card gap | Add or adjust food-bolus/drooling ownership so airway/foreign-body guides beat poisoning/allergy, and ensure citation selection uses the retrieved choking owner when present. |
| EY #6 | is this meningitis or a viral illness | `GD-589`, `GD-232`, `GD-235`, `GD-284`, `GD-268` | `GD-298`, `GD-268`, `GD-053`, `GD-949`, `GD-284` | `GD-268`, `GD-949`, `GD-298` | `meningitis_sepsis_child` / fail | Mixed: ranking defect, citation-owner defect | Tune meningitis-vs-viral rerank/citation preference toward `GD-589` / `GD-268`; keep this as compare/clarify unless red flags are present. |
| FC #1 | child fell and now has belly pain | `GD-380`, `GD-297`, `GD-232` | `GD-910`, `GD-380`, `GD-947`, `GD-733`, `GD-298`, `GD-284` | `GD-380`, `GD-733` | `abdominal_internal_bleeding` / fail | Mixed: ranking defect, card gap | Boost fall plus belly pain toward `GD-380`; revise the card language so abdominal trauma concern is not framed only as GI bleed or ectopic collapse. |
| FC #4 | pale and dizzy after abdominal trauma | `GD-380`, `GD-297`, `GD-232` | `GD-558`, `GD-380`, `GD-039`, `GD-401`, `GD-947` | `GD-380`, `GD-947` | `abdominal_internal_bleeding` / fail | Mixed: ranking defect, card gap | Rerank `GD-380` above EMS/compartment-syndrome adjacency for trauma plus shock signs; add explicit trauma-shock required action to the card. |
| FC #5 | left side pain after handlebar injury | `GD-380`, `GD-297`, `GD-232` | `GD-039`, `GD-049`, `GD-380`, `GD-355`, `GD-922`, `GD-915`, `GD-949` | `GD-915` | `abdominal_internal_bleeding` / fail | Mixed: ranking defect, citation-owner defect | Add a narrow handlebar/abdominal-impact rerank boost for `GD-380`; fix citation selection so retrieved expected owners outrank musculoskeletal red-flag support. |
| FC #6 | do we watch at home or get urgent help first | `GD-380`, `GD-297`, `GD-232` | `GD-733`, `GD-918`, `GD-922`, `GD-936`, `GD-930` | `GD-918`, `GD-733`, `GD-922` | `abdominal_internal_bleeding` / fail | Expectation mismatch | Rewritten in `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_fc_20260417.txt` as `after a hard belly hit do we watch at home or get urgent help first`; do not add a broad urgent-vs-home deterministic predicate from the old row. |

## Planner Read

- Main fix lane is targeted rerank/citation-owner preference, not broad deterministic routing. FC #5 is the closest high-risk lexical case, but even there a narrow handlebar or abdominal-impact boost is cleaner than a new deterministic builder.
- The abdominal card needs review: it is named for internal bleeding, but required hits overfit GI bleed/ectopic language, so trauma prompts fail the card even when the answer reaches `GD-380`.
- FC #6 was a contract/prompt seed issue. As written, it contained no abdominal-trauma anchor, so the retrieval miss was expected behavior rather than a retrieval defect. The prompt seed has been rewritten with a belly-hit anchor for the next bench run.

## Recommended Next Slices

- `S4b-runtime`: patch only the rows classified as true ranking/citation-owner defects, with focused tests around food bolus, meningitis-vs-viral, and abdominal trauma/handlebar ownership.
- `S2b-card-review`: split or broaden the abdominal card so trauma shock/internal-injury required actions are distinct from GI bleed/ectopic required actions.
- `S1c-prompt-contract`: update the FC prompt expectation for #6 so the question is self-contained or remove it from the abdominal-trauma expectation family.

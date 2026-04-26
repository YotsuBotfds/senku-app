# RAG-EVAL8b Initial Miss Classification

## Slice

Classified the three EVAL8 retrieval-only top-8 misses from
`RAG-EVAL8a_initial_retrieval_validation.md`. This slice is dispatch-only: no
prompt-pack, guide, runtime, test, script, or bench-artifact changes.

## Sources Read

- `notes/dispatch/RAG-EVAL8a_initial_retrieval_validation.md`
- `notes/specs/rag_eval8_compound_boundary_holdouts_20260425.md`
- `notes/dispatch/RAG-EVAL8_compound_boundary_pack.md`
- `artifacts/prompts/adhoc/rag_eval8_compound_boundary_holdouts_20260425.jsonl`
- `artifacts/bench/rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.md`
- Focused snippets/frontmatter from expected and retrieved guide families for
  heat/toxicology, spine/first-aid/cold-water, and child sepsis/meningitis.

## Classification Table

| id | miss shape | classification | next smallest safe action | reasoning level |
| --- | --- | --- | --- | --- |
| `RE8-HT-001` | Expected `GD-377|GD-602|GD-301|GD-035|GD-232`; retrieved `GD-054|GD-054|GD-898|GD-960|GD-054|GD-512|GD-353|GD-526`. | Likely unfair expectation / pack design issue, with duplicated source-family ownership. | Reclassify the prompt expectation before any guide/runtime work: either include `GD-054` and `GD-526` as fair heat/toxicology owners or narrow the prompt/spec if the intent is specifically to prove `GD-377` plus `GD-602`/`GD-301`. | Medium-high. The retrieved set includes a direct toxicology owner (`GD-054`) and a direct heat-stroke/thermal-injury owner (`GD-526`). `GD-377`, `GD-301`, and `GD-602` also fit, but the current expected list excludes older/parallel owner surfaces that are not obviously wrong for the prompt. |
| `RE8-SP-001` | Expected `GD-049|GD-232|GD-024|GD-035`; retrieved `GD-731|GD-935|GD-298|GD-935|GD-369|GD-941|GD-030|GD-733`. Retrieval profile was `how_to_task`, `safety_critical=false`, with focus drift to `cabin`. | Metadata/routing/ranking drift, not a guide-language gap. | Add a future focused routing/ranking diagnostic for possible spinal injury phrasing: `neck hurts` + `tingling/numbness` + `carry/move` should trigger safety triage and surface `GD-049`/`GD-232` before water, child-safety, search, or common-ailment support. Do not edit guides until a diagnostic confirms where the phrase is lost. | High. Existing `GD-049` and `GD-232` source text already contains spinal precautions, tingling/numbness, do-not-move, and cold-exposure support. The failure is that the prompt was not treated as safety-critical and top retrieval selected anatomy/water/child-safety/common-ailment distractors. |
| `RE8-CM-001` | Expected `GD-589|GD-284|GD-298|GD-617|GD-219|GD-232`; retrieved `GD-949|GD-936|GD-733|GD-948|GD-733|GD-918|GD-930|GD-915`. | Metadata/routing/ranking drift, bordering on true retrieval defect if reproduced outside this pack. | Run a focused retrieval diagnostic for `stiff neck` + `purple rash` with and without explicit `fever` and connectivity distractors. If reproduced, treat as high-priority routing/ranking work for `GD-589` reviewed-card owner selection; do not change the pack expectation first. | High. `GD-589` has exact frontmatter aliases/routing cues for fever + stiff neck + purple/dark rash and body text that names meningitis/meningococcemia/sepsis. `GD-284`, `GD-298`, and `GD-617` also contain child meningitis/sepsis support. Retrieval instead ranked generic headache/asthma/common-ailment/allergy/anxiety/back-pain surfaces, so the owner evidence exists but was not surfaced. |

## Cautions

- Do not implement deterministic rules just to improve EVAL8.
- For `RE8-HT-001`, changing runtime before resolving expected-owner fairness
  would risk tuning toward one of several legitimate overlapping heat/toxicology
  source families.
- For `RE8-CM-001`, the prompt omits explicit fever, but the stiff-neck +
  purple-rash child presentation is still a fair meningitis/sepsis retrieval
  expectation because the guide metadata already covers that emergency family.
- For `RE8-SP-001`, avoid guide edits until a retrieval diagnostic proves the
  miss is caused by missing source language rather than safety-frame/ranking
  selection.

## Recommendation

Handle `RE8-HT-001` as expectation cleanup first. Handle `RE8-SP-001` and
`RE8-CM-001` as focused retrieval diagnostics, with `RE8-CM-001` highest
liability because it should exercise an existing reviewed-card owner family.

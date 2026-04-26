# RAG-EVAL6d Electrical / Trauma Retrieval Triage

Date: 2026-04-25

Scope: retrieval/source-packaging triage only for EVAL6 shelter/electrical rows and the injury/cold row. No guide edits, runtime edits, rerank changes, generation checks, or expected-owner changes were made.

Inputs:

- `notes/dispatch/RAG-EVAL6a_high_liability_compound_retrieval_smoke.md`
- `artifacts/prompts/adhoc/rag_eval_high_liability_compound_holdouts_20260425.jsonl`
- `artifacts/bench/rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.{json,md}`
- Guide frontmatter/content for expected owners `GD-513`, `GD-695`, `GD-345`, `GD-446`, `GD-232`, `GD-297`, `GD-024`, `GD-035`, `GD-931` and distractors `GD-921`, `GD-940`, `GD-049`, `GD-378`.

## Rows Checked

| row | expected primary owner(s) | observed top retrieved guides | triage status |
| --- | --- | --- | --- |
| RE6-SH-001 | `GD-513` | `GD-921`, `GD-921`, `GD-921`, `GD-921`, `GD-695`, `GD-695`, `GD-695`, `GD-964` | source-packaging gap for electrical hazard ownership |
| RE6-SH-003 | `GD-513` | `GD-940`, `GD-921`, `GD-921`, `GD-921`, `GD-940`, `GD-921`, `GD-940`, `GD-695` | source-packaging gap for electrical hazard ownership |
| RE6-IC-001 | `GD-232`, `GD-297` | `GD-049`, `GD-049`, `GD-931`, `GD-931`, `GD-695`, `GD-393`, `GD-290`, `GD-378` | mixed: metadata gap for `GD-232`; expected-owner adjustment likely for `GD-297` |

## Why `GD-513` Is Absent For Wet Electrical Rows

`GD-513` has the right substantive content. Its top triage block explicitly covers wet breaker boxes, sparking outlets, exposed live wires, downed lines, and "hazard before repair, flood cleanup, or generic troubleshooting." It also tells users not to touch wet panels, standing water, metal equipment, or conductors and to shut off power only from a dry safe location.

The miss is in retrieval/source packaging, not in the safety concept:

- The scenario frame for both shelter/electrical rows is classified as construction/shelter/water, not electrical hazard. For RE6-SH-001 the domains are `construction,shelter,water`, hazards are only `storm damage,wet exposure`, and retrieval specs are just the raw prompt plus `storm damaged roof`, `water is coming in`, and `some outlets got wet`. No electrical-safety expansion is injected.
- RE6-SH-003 is even more roof-shaped: the only retrieval spec is the raw prompt plus a `(focus: roof)` query. That strongly favors repair guides over hazard-isolation guides.
- `GD-921` and `GD-940` contain exact roof/home-repair wording plus electrical warnings. `GD-921` has "water near lights, outlets, or metal fixtures" and "shut off power" in the active-leak sequence, so it covers all SH-001 objectives directly. `GD-940` has aliases like "water near electrical outlet" and first-check repair triage. These are legitimate support guides, but they are not enough to own the red-flag wet-electrical abstain boundary.
- `GD-513` frontmatter has aliases for `wet breaker box`, `breaker box got wet`, `sparking outlet`, and `outlet sparked`, but not the exact compound retrieval language used here: wet outlets, electrical damage inside, standing water near the panel, storm/roof leak plus energized/wet panel, and repair/patch request blocked by electrical hazard.
- Existing metadata audits flag `GD-513` as missing `routing_cues`, `applicability`, `citation_policy`, and reviewed card packaging. That matches this miss: the guide content is strong, but it is not packaged to win compound construction/water prompts.

Interpretation: keep `GD-513` as the expected primary owner for these rows. The current retrieval stack is over-indexing on roof repair/source wording and under-indexing on electrical hazard isolation when water plus outlets/panel appears inside a shelter prompt.

## Why Trauma Owners Are Absent For RE6-IC-001

RE6-IC-001 asks: "One team member may have a spinal injury, the creek may be contaminated, and the temperature is dropping fast. Give me a clean, separated answer."

Observed retrieval is not random: `GD-049` appears at ranks 1-2, and that guide is explicitly "Orthopedics & Fracture Management" with "spinal precautions" in its description. `GD-931` appears at ranks 3-4 for contaminated creek/source assessment. Cold/survival support appears later through `GD-695`, `GD-290`, and `GD-378`.

The primary-owner absence has two different causes:

- `GD-232` is substantively relevant but under-packaged for this exact wording. It contains spinal immobilization guidance and hypothermia content, but its frontmatter/routing cues are mostly broad emergency, CPR, shock, bleeding, stroke, chest pain, poisoning, burns, and electrical shock. It does not expose "possible spinal injury", "do not move", "support head and neck", or "spine precautions plus cold exposure" as first-class retrieval cues. In a long first-aid guide, the spine section is too deep to beat the more specific orthopedics owner.
- `GD-297` is probably not the right primary expected owner for this row as written. Its guide is trauma hemorrhage control: tourniquets, bleeding, wound packing, hemorrhagic shock, pelvic/truncal bleeding. RE6-IC-001 has possible spine injury but no bleeding, shock, wound, crush, or hemorrhage terms. Its absence is therefore more likely an expected-owner issue than a retrieval failure.

`GD-024` also being absent is understandable: the prompt says "temperature is dropping fast", but the retrieval frame puts every objective in `water`, with hazard `contamination`; it does not classify cold exposure or hypothermia. That is a secondary source-packaging/query-framing issue, not a reason to make cold the primary owner ahead of spine precautions.

Interpretation: keep RE6-IC-001 retrieval-smoke-first and abstain-boundary protected. Treat `GD-049` as a plausible primary or co-primary spine owner, keep `GD-232` as broad emergency support after metadata/source packaging improves, and remove or demote `GD-297` unless the prompt is rewritten to include hemorrhage/shock.

## Smallest Next Implementation Slice

Recommended next slice: guide metadata/source-packaging only, followed by re-ingest and the same retrieval-only eval.

1. Add narrow electrical hazard routing/source packaging for `GD-513`:
   - aliases/routing cues for wet outlets, water through outlet/light fixture, electrical damage inside after storm, standing water near panel/breaker box, roof patch blocked by electrical hazard, and wet electrical before roof repair.
   - applicability that says storm, flood, roof leak, or cleanup prompts with wet outlets/panels should route to electrical hazard isolation before repair instructions.
   - reciprocal support links from `GD-921`/`GD-940` to `electrical-safety-hazard-prevention` if guide-link packaging is part of the chosen metadata path.

2. Add narrow spine/cold emergency retrieval cues for `GD-232`, without changing its medical advice:
   - possible spinal injury, do not move casually, support head/neck, spinal precautions, cold exposure while immobilized, keep warm without unsafe movement.
   - Consider whether `GD-049` should be the primary expected owner for spine-specific rows and `GD-232` the emergency support owner.

3. Adjust RE6-IC-001 expectations after packaging review:
   - likely primary: `GD-049` for spine precautions, with `GD-232` and `GD-024` as support.
   - likely demote/remove: `GD-297` unless the row is changed to include severe bleeding, shock, wound packing, or hemorrhage control.

Do not open generation for these rows until owner evidence improves. RE6-SH-003 and RE6-IC-001 should preserve abstain behavior: no roof-patching instructions across possible live/wet electrical hazards, and no casual movement of a possible spinal injury.

## Validation To Run After That Slice

After metadata/source packaging edits:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B ingest.py --stats
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.json --output-md artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```

Success criteria for this slice:

- `GD-513` appears in top-8 for RE6-SH-001 and RE6-SH-003, ideally top-3 for RE6-SH-003 because it is an abstain row.
- RE6-IC-001 either retrieves `GD-232` after source packaging or has its primary expected owners adjusted to reflect `GD-049` as the spine owner and `GD-297` as out-of-scope for the current prompt.
- No generation claims are made from these rows until retrieval/source ownership is fair.

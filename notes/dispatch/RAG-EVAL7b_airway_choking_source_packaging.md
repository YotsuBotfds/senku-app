# RAG-EVAL7b Airway Choking Source Packaging

## Scope

Packaging-only retrieval fix for EVAL7 severe adult choking / food bolus:

- `guides/first-aid.md`
- `guides/emergency-airway-management.md`
- `notes/dispatch/RAG-EVAL7b_airway_choking_source_packaging.md`

No procedure or body advice was changed. Edits were limited to frontmatter
aliases, tags, and routing cues so the existing choking source content is
easier for retrieval to select.

While touching first-aid frontmatter, the pre-existing mojibake icon was also
restored. Broader first-aid body mojibake remains deferred to a separate
safety-reviewed text-quality tranche.

## Source Packaging

Added narrow adult choking / food-bolus language:

- food stuck
- cannot speak
- turning blue
- choking rescue
- abdominal thrusts
- do not give water
- severe airway obstruction

`GD-232` remains the practical first-aid owner. `GD-579` is packaged as the
airway companion source for severe obstruction and escalation context.

## Validation

Rerun label:
`rag_eval7_red_team_boundary_holdouts_20260425_airway_packaging_retrieval_only`.

Commands run:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B ingest.py --files guides/first-aid.md guides/emergency-airway-management.md --force-files
& .\.venvs\senku-validate\Scripts\python.exe -B ingest.py --stats
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_airway_packaging_retrieval_only.json --output-md artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_airway_packaging_retrieval_only.md --progress
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval7_red_team_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_airway_packaging_retrieval_only.json --retrieval-top-k 8 --output-json artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_airway_packaging_retrieval_only_expectations.json --output-md artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_airway_packaging_retrieval_only_expectations.md --output-text artifacts\bench\rag_eval7_red_team_boundary_holdouts_20260425_airway_packaging_retrieval_only_expectations.txt --fail-on-errors
```

Results:

- Re-ingest processed 2 files / 208 chunks.
- `ingest.py --stats`: collection total `49737` chunks; medical `9631`.
- Retrieval pack: expected hit@1 `5/10`, hit@3 `9/10`, hit@k `9/10`, retrieval errors `0`.
- `RE7-AW-002`: moved from missing expected owner in top-k to `GD-579` at rank `3`; hit@3 and hit@k are yes; owner share `0.7500`.
- Prompt expectation validation: `errors=0`, `warnings=1`; remaining warning is unrelated `RE7-ST-001`.

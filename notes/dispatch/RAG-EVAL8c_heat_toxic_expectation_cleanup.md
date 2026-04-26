# RAG-EVAL8c Heat/Toxic Expectation Cleanup

## Slice

Applied the EVAL8b classification for `RE8-HT-001`: the initial retrieval miss
was an unfair expectation caused by duplicated heat/toxicology source-family
ownership, not a guide/runtime defect. This slice edits only the EVAL8 prompt
pack, EVAL8 spec, and this dispatch note.

## Files Changed

- `artifacts/prompts/adhoc/rag_eval8_compound_boundary_holdouts_20260425.jsonl`
- `notes/specs/rag_eval8_compound_boundary_holdouts_20260425.md`
- `notes/dispatch/RAG-EVAL8c_heat_toxic_expectation_cleanup.md`

## Local Inspection

- `GD-054` is a legitimate toxic exposure owner for this prompt: it covers
  poison identification and emergency treatment of toxic exposures, unknown
  substance exposure, altered consciousness, agricultural/chemical exposures,
  organophosphates, decontamination, and toxic hyperthermia.
- `GD-526` is a legitimate heat owner for this prompt: it covers heat stroke,
  altered mental state/confusion, field cooling, sweating nuance for exertional
  heat stroke, and the warning not to delay cooling.
- The original owners remain legitimate: `GD-377` for heat illness, `GD-602`
  for toxidrome/field poisoning, and `GD-301` as toxicology support.

## Expectation Change

Only `RE8-HT-001` changed.

- `expected_guides` changed from
  `GD-377, GD-602, GD-301, GD-035, GD-232`
  to
  `GD-377, GD-526, GD-602, GD-054, GD-301, GD-035, GD-232`.
- `primary_expected_guides` changed from
  `GD-377, GD-602`
  to
  `GD-377, GD-526, GD-602, GD-054`.

The prompt text, expected behavior, required concepts, forbidden/suspicious
actions, and all other EVAL8 rows were left unchanged.

## Validation

Commands run after this cleanup:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -c "import json, pathlib; p=pathlib.Path('artifacts/prompts/adhoc/rag_eval8_compound_boundary_holdouts_20260425.jsonl'); rows=[json.loads(line) for line in p.read_text(encoding='utf-8').splitlines() if line.strip()]; print(f'ok rows={len(rows)}')"
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --fail-on-errors
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```

Results:

- JSONL parse/count: `ok rows=10`.
- Prompt expectation validation: `pass`; `prompts=10 expected_rows=10 errors=0
  warnings=0 suppressed=0`.
- Retrieval expectation validation against the saved initial artifact:
  `warn`; `prompts=10 expected_rows=10 errors=0 warnings=3 suppressed=0`.

The third result still reports `RE8-HT-001`, `RE8-SP-001`, and `RE8-CM-001`
because
`artifacts/bench/rag_eval8_compound_boundary_holdouts_20260425_initial_retrieval_only.json`
embeds the pre-cleanup `expected_guide_ids` for each retrieval row. The
validator uses those embedded expectations before consulting the updated prompt
pack.

A read-only current-pack comparison against the same saved top-8 retrieval IDs
shows the intended warning drop:

```text
current-pack top8 missing: RE8-SP-001, RE8-CM-001
warnings= 2
```

Remaining EVAL8 retrieval warnings under current prompt expectations:
`RE8-SP-001` and `RE8-CM-001`.

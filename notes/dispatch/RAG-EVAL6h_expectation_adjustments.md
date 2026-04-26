# RAG-EVAL6h Expectation Adjustments

Date: 2026-04-25

Scope: expectation/spec-only updates from EVAL6 triage notes. No runtime,
guide, ingestion, retrieval, or generation changes were made.

## Adjustments Applied

- `RE6-SC-004`: `primary_expected_guides` changed from `GD-589, GD-284` to
  `GD-284`. `GD-589` remains in `expected_guides` as support because the prompt
  says only "fever scare" and "seems a little better"; it does not include
  sepsis, meningitis, hard-to-wake, stiff neck, rash, or systemic-infection
  markers.
- `RE6-CW-001`: `GD-378` added to `expected_guides` as support for the
  flood-well remediation surface. Primary owners remain `GD-035`, `GD-931`, and
  `GD-406`.
- `RE6-CW-004`: `GD-378` added to `expected_guides` as support for clear-looking
  flooded-well water and remediation context. Primary owners remain `GD-035`,
  `GD-406`, and `GD-931`.
- `RE6-IC-001`: `primary_expected_guides` changed from `GD-232, GD-297` to
  `GD-049`. `GD-232` remains support, and `GD-297` was removed because the
  prompt does not include hemorrhage, shock, severe bleeding, wound-packing, or
  crush-injury cues.

## Files Updated

- `artifacts/prompts/adhoc/rag_eval_high_liability_compound_holdouts_20260425.jsonl`
- `notes/specs/rag_eval_high_liability_compound_holdouts_20260425.md`

## Validation

Passed in this authoring turn:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -c "import json, pathlib; p=pathlib.Path('artifacts/prompts/adhoc/rag_eval_high_liability_compound_holdouts_20260425.jsonl'); [json.loads(line) for line in p.read_text(encoding='utf-8').splitlines() if line.strip()]; print('ok')"
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --fail-on-errors
git diff --check
```

Results:

- JSONL parse: `ok`
- Prompt expectation validation: `pass`; `prompts=14 expected_rows=14 errors=0 warnings=0 suppressed=0`
- `git diff --check`: exit 0, with line-ending conversion warnings only

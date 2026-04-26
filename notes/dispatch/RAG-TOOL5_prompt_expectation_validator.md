# RAG-TOOL5 Prompt Expectation Validator

## Why This Slice

Prompt-pack expectation drift can turn a good retrieval result into a false
miss. The recent `RE2-UP-009` cleanup is the example: stale expectation
metadata pointed at `GD-120` while the sharpen-a-blade prompt correctly
retrieved `GD-397`.

## Scope

Added `scripts/validate_prompt_expectations.py`, a stdlib-only linter for
JSONL/CSV prompt packs.

It checks:

- stable prompt IDs are present and unique across the selected packs
- expected guide fields contain well-formed `GD-###` IDs
- expected guide IDs exist in `guides/*.md` frontmatter
- `guide_id`/`guide_ids` and explicit expectation fields overlap when both are
  present
- optional allowed-drift JSON suppresses known exceptions
- optional retrieval-only eval JSON/Markdown flags expected owners absent from
  retrieved top-k

## Usage

Focused prompt-pack check:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --output-json artifacts\bench\prompt_expectation_validation.json --output-md artifacts\bench\prompt_expectation_validation.md --fail-on-errors
```

With retrieval-only eval:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\retrieval_pack_eval.json --retrieval-top-k 24 --fail-on-errors --fail-on-warnings
```

Allowed-drift manifest shape:

```json
{
  "allowed_drift": [
    {
      "prompt_id": "RE2-UP-009",
      "issue_codes": ["expectation_field_disagreement"],
      "guide_ids": ["GD-120", "GD-397"],
      "reason": "Known historical expectation cleanup."
    }
  ]
}
```

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_validate_prompt_expectations -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\validate_prompt_expectations.py tests\test_validate_prompt_expectations.py
```

The tool is report-only unless `--fail-on-errors` or `--fail-on-warnings` is
set, so it can be introduced as a soft preflight before becoming a gate.

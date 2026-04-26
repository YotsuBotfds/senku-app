# RAG-EVAL8 Compound Boundary Pack

## Slice

Drafted the next held-out compound/boundary prompt pack. This slice is
evaluation-artifact only: no guide edits, no runtime or rerank changes, no
ingestion, no retrieval eval, no generation, and no Android work.

## Files Changed

- `artifacts/prompts/adhoc/rag_eval8_compound_boundary_holdouts_20260425.jsonl`
- `notes/specs/rag_eval8_compound_boundary_holdouts_20260425.md`
- `notes/dispatch/RAG-EVAL8_compound_boundary_pack.md`

## Sources Read

- `notes/AGENT_OPERATING_CONTEXT.md`
- `notes/dispatch/RAG-NEXT_current_backlog_triage_20260425.md`
- `notes/SENKU_COMPOUND_SCENARIO_PLAYBOOKS_20260425.md`
- `notes/specs/rag_eval_high_liability_compound_holdouts_20260425.md`
- `notes/specs/rag_eval7_red_team_boundary_holdouts_20260425.md`
- `notes/specs/rag_eval_partial_router_holdouts_20260425.md`
- `notes/dispatch/RAG-EVAL4_partial_router_followup_priorities.md`

## Rows Included

The pack contains 10 compound/boundary prompts:

- `RE8-EL-001`: storm cleanup electrical shock plus roof leak pressure.
- `RE8-HT-001`: heat illness versus toxic exposure with questionable water.
- `RE8-MD-001`: conflicting medication labels during an outage.
- `RE8-RW-001`: roof runoff / flood-affected well for infant formula.
- `RE8-KT-001`: crowded volunteer kitchen with illness cluster and limited
  handwashing water.
- `RE8-TR-001`: human disaster triage competing with loose livestock and route
  risk.
- `RE8-SP-001`: possible spinal injury with cold exposure and contaminated
  creek movement pressure.
- `RE8-CM-001`: meningitis/sepsis red flags competing with internet/router
  troubleshooting.
- `RE8-WS-001`: small water-system restart blocked by wet electrical pump-room
  hazards.
- `RE8-FS-001`: neighborhood food-system continuity under refrigerator outage
  and stomach-illness pressure.

## Owner Intent

Primary owners are intentionally mixed across high-liability medical, hazard,
public-health, and bridge/system families:

- electrical hazard / shock: `GD-513`, support `GD-232`;
- heat and toxic exposure: `GD-377`, `GD-602`, support `GD-301`;
- medication uncertainty: `GD-239`;
- runoff and questionable water: `GD-721`, `GD-035`, `GD-931`;
- community kitchen and illness control: `GD-961`, `GD-732`;
- disaster triage: `GD-029`, `GD-232`;
- spine movement boundary: `GD-049`;
- meningitis/sepsis child red flags: `GD-589`;
- water-system operations with electrical hazard: `GD-648`, `GD-513`;
- food-system continuity with food safety: `GD-634`, `GD-591`, `GD-732`.

## Safety Cautions

- Do not add deterministic rules just to improve this pack.
- Do not treat support lanes as failure when the answer clearly preserves the
  primary hazard order and cites a primary owner.
- Do not run generated-answer diagnostics before retrieval-only owner checks on
  rows marked `retrieval-smoke-first`.
- Do not reuse this as Android reviewed-card exposure proof.
- If a bridge/system row misses, classify expectation fairness before adding
  metadata churn; recent backlog says bridge metadata/routing work is closed
  unless fresh evidence reopens it.

## Validation

Passed in this authoring turn:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -c "import json, pathlib; p=pathlib.Path('artifacts/prompts/adhoc/rag_eval8_compound_boundary_holdouts_20260425.jsonl'); rows=[json.loads(line) for line in p.read_text(encoding='utf-8').splitlines() if line.strip()]; print(f'ok rows={len(rows)}')"
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8_compound_boundary_holdouts_20260425.jsonl --fail-on-errors
```

Results:

- JSONL parse: `ok rows=10`
- Prompt expectation validation: `pass`; `prompts=10 expected_rows=10 errors=0 warnings=0 suppressed=0`

## Next Step

Run a retrieval-only pass for the new JSONL pack, then review misses for unfair
expectation, source packaging, marker/partial drift, or true retrieval/ranking
work before any guide/runtime changes.

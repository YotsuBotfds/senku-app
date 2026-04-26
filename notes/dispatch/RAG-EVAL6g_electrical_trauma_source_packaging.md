# RAG-EVAL6g Electrical / Trauma Source Packaging

Date: 2026-04-25

Scope: narrow guide metadata/source-packaging only. No JSONL expected-owner edits,
ingest, retrieval eval, generation checks, or medical/electrical procedure changes
were made.

## Changes Made

- `GD-513` (`guides/electrical-safety-hazard-prevention.md`): added targeted
  aliases and routing cues for wet outlets, water through fixtures, storm/roof
  leak electrical damage, standing water near panels or breaker boxes, and roof
  repair blocked by wet electrical hazards.
- `GD-232` (`guides/first-aid.md`): added targeted aliases and routing cues for
  possible spinal injury, do-not-move-casually wording, support head/neck, spine
  precautions, and cold exposure while the person must be kept still.

## Reciprocal Support Review

- Verified `guides/roof-leak-emergency-repair.md` is `GD-921`.
- Verified `guides/simple-home-repairs.md` is `GD-940`.
- No reciprocal edits were made in this slice. Both guides already contain
  support warnings for water near lights, outlets, wiring, metal fixtures, and
  electrical hazard before repair; the retrieval gap was `GD-513` ownership
  packaging rather than missing roof/simple-repair warnings.

## Expected-Owner Note

For RE6-IC-001, `GD-049` (`orthopedics-fractures`) remains the likely primary or
co-primary spine owner because it explicitly owns spinal precautions. `GD-232`
should remain broad emergency stabilization support. `GD-297` should likely be
demoted or removed from that row unless the prompt is rewritten to include
severe bleeding, shock, wound packing, or hemorrhage control. This slice does
not edit JSONL expectations.

## Ingest Run

Run after orchestrator applied the batched guide edits:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\electrical-safety-hazard-prevention.md guides\first-aid.md
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --stats
```

Result:

- 7-guide batched ingest with the contaminated-well/food packaging slice
  processed 643 chunks total.
- Collection `senku_guides` remained at 49,746 chunks after replacement.
- Integrated retrieval eval moved `RE6-IC-001` to hit@1 via `GD-049`.
- Storm-shelter electrical rows still need a follow-up: `GD-513` is in top 8,
  but roof/shelter repair owners dominate ranks 1-4.

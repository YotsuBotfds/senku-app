# RAG-EVAL6f Contaminated Well + Food Source Packaging

Date: 2026-04-26

## Scope

Narrow source-packaging follow-up for the contaminated-well plus food-scarcity
rows triaged in `RAG-EVAL6c_contaminated_well_food_retrieval_triage.md`.

Edited only the expected guide source packaging:

- `guides/water-purification.md` (GD-035)
- `guides/questionable-water-assessment-clarification.md` (GD-931)
- `guides/water-testing-quality-assessment.md` (GD-406)
- `guides/food-safety-contamination-prevention.md` (GD-666)
- `guides/food-rationing.md` (GD-089)

No prompt JSONL, runtime retrieval, rerank logic, or ingest data was changed.

## Packaging Added

Water guides now expose targeted contaminated-well retrieval language for:

- flooded or contaminated drinking wells
- clear-looking well or flood-affected water
- boiling-once and boiling-limit questions
- children drinking questionable well water
- testing, remediation, safer-source selection, and treatment-limit handoffs

The water-safety boundary remains explicit: clear-looking water is not proof of
safety after flooding, and boiling helps with many biological hazards but does
not resolve chemical, fuel, sewage, heavy-metal, pesticide, or unknown flood
contamination.

Food guides now expose targeted scarcity-safety language for:

- stored grain with questionable water
- stretching supplies without making people sick
- little cooking fuel under food scarcity
- refusing spoilage, unsafe-water, and undercooking shortcuts while rationing

## Expected-Owner Adjustment

GD-378 `flood-response-remediation` should be expected support for RE6-CW-001
and RE6-CW-004. It owns exact flooded-well remediation language and is a valid
support source, not a junk distractor, when the prompt foregrounds flooding plus
well contamination.

This slice intentionally did not edit the JSONL expectations. The orchestrator
should make that expectation adjustment in the eval-pack lane after ingest.

## Ingest Run

Run after orchestrator applied the batched guide edits:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\food-rationing.md guides\food-safety-contamination-prevention.md guides\questionable-water-assessment-clarification.md guides\water-purification.md guides\water-testing-quality-assessment.md
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --stats
```

Result:

- 7-guide batched ingest with the electrical/trauma packaging slice processed
  643 chunks total.
- Collection `senku_guides` remained at 49,746 chunks after replacement.
- Integrated retrieval eval moved `RE6-CW-001` and `RE6-CW-004` to hit@1 via
  `GD-378` support while keeping the primary water-safety owner set unchanged.

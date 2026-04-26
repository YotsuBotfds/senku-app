# RAG-EVAL6a High-Liability Compound Retrieval Smoke

Worker: Lovelace
Date: 2026-04-25

## Scope

First retrieval-only smoke for the EVAL6 high-liability compound holdout pack.
No guide edits, runtime changes, rerank changes, or generation were performed.

Pack:
`artifacts/prompts/adhoc/rag_eval_high_liability_compound_holdouts_20260425.jsonl`

Spec:
`notes/specs/rag_eval_high_liability_compound_holdouts_20260425.md`

Outputs:

- `artifacts/bench/rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.json`
- `artifacts/bench/rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.md`

## Commands

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.json --output-md artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.md --progress
```

Result: passed; 14/14 rows retrieved; retrieval_error_rows=0.

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```

Result: passed with warnings; errors=0, warnings=4.

Warnings:

- RE6-SC-002: expected guide owner never appears in retrieved top-k.
- RE6-SC-003: expected guide owner never appears in retrieved top-k.
- RE6-CW-001: expected guide owner never appears in retrieved top-k.
- RE6-CW-004: expected guide owner never appears in retrieved top-k.

## Retrieval Metrics

Top-k: 8

| metric | value |
| --- | --- |
| expected hit@1 | 1/14 (7.1%) |
| expected hit@3 | 5/14 (35.7%) |
| expected hit@8 | 10/14 (71.4%) |
| expected owner best rank | 3.90 |
| simple owner share | 29/112 (25.9%) |

Top distractors by count:

- GD-378: 9
- GD-921: 8
- GD-695: 5
- GD-924: 5
- GD-288: 4

## Primary Owner Absences

Rows with at least one primary expected guide absent from top-8:

| row | missing primary owner(s) | observed top retrieved guides |
| --- | --- | --- |
| RE6-SC-002 | GD-284 | GD-288, GD-211, GD-375, GD-288, GD-288, GD-211, GD-372, GD-288 |
| RE6-SC-003 | GD-284, GD-492 | GD-596, GD-023, GD-079, GD-373, GD-916, GD-023, GD-359, GD-698 |
| RE6-SC-004 | GD-589 | GD-492, GD-284, GD-617, GD-284, GD-284, GD-911, GD-284, GD-916 |
| RE6-CW-001 | GD-035, GD-931, GD-406 | GD-378, GD-695, GD-378, GD-695, GD-378, GD-398, GD-694, GD-378 |
| RE6-CW-002 | GD-666, GD-906, GD-591, GD-089 | GD-924, GD-924, GD-373, GD-931, GD-961, GD-924, GD-931, GD-953 |
| RE6-CW-004 | GD-035, GD-406, GD-931 | GD-378, GD-378, GD-695, GD-378, GD-553, GD-695, GD-378, GD-694 |
| RE6-SH-001 | GD-513 | GD-921, GD-921, GD-921, GD-921, GD-695, GD-695, GD-695, GD-964 |
| RE6-SH-003 | GD-513 | GD-940, GD-921, GD-921, GD-921, GD-940, GD-921, GD-940, GD-695 |
| RE6-IC-001 | GD-232, GD-297 | GD-049, GD-049, GD-931, GD-931, GD-695, GD-393, GD-290, GD-378 |

Rows with all primary expected guides present in top-8:

- RE6-SC-001
- RE6-EV-002
- RE6-EV-004
- RE6-IC-002
- RE6-IC-004

## Before-Generation Triage

Keep retrieval-smoke-first:

- RE6-SC-002 and RE6-SC-003: newborn/child danger owners are absent from top-8; generation would be testing distractor behavior rather than expected emergency ownership.
- RE6-CW-001 and RE6-CW-004: no expected contaminated-water owner appears in top-8; these need retrieval/source-packaging investigation first.
- RE6-CW-002: a supporting water owner appears, but none of the four primary food/water scarcity owners appear; keep retrieval-first before treating it as a fair generation row.
- RE6-SH-001 and RE6-SH-003: supporting owner GD-695 appears, but primary wet-electrical owner GD-513 is absent; retrieval evidence is not yet strong enough for generation claims.
- RE6-IC-001: intentionally marked retrieval-smoke-first in the pack; primary trauma/cold owners are absent even though water support appears.

Guide/card-work-first before generation:

- RE6-SC-001: strong owner share and primary GD-589 present, but high-liability reviewed-card emergency behavior should stay card/runtime-focused.
- RE6-SC-004: GD-284 appears, but primary GD-589 is missing; card behavior can be checked after retrieval owner balance improves.
- RE6-EV-002, RE6-EV-004, RE6-IC-002, RE6-IC-004: primary owners appear, but these are high-liability reviewed-card runtime rows and should not be promoted to generation-only evidence.

## Retrieval Risks

1. Child/newborn emergency prompts are brittle: two of four sick-child/outage rows miss the expected emergency owners entirely.
2. Contaminated-well plus food-scarcity prompts are dominated by unrelated distractors, especially GD-378 and GD-695, with two complete expected-owner misses.
3. Shelter/electrical and injury/cold rows retrieve support lanes but often miss primary hazard owners, which could let generation answer from secondary context.


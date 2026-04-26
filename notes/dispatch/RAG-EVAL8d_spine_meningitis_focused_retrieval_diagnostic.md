# RAG-EVAL8d Spine / Meningitis Focused Retrieval Diagnostic

## Slice

Medium Worker M ran a focused retrieval-only diagnostic for the two EVAL8
misses previously classified as routing/ranking drift:

- `RE8-SP-001`: possible spinal injury with movement pressure, cold exposure,
  contaminated creek/cabin distractors; expected `GD-049` / `GD-232`.
- `RE8-CM-001`: child stiff neck plus purple/dark rash with connectivity
  distractors; expected `GD-589` plus child emergency support owners
  `GD-284` / `GD-298` / `GD-617` / `GD-232`.

No EVAL8 pack/spec, guides, runtime code, tests, or scripts were edited.

## Files

- Prompt pack:
  `artifacts/prompts/adhoc/rag_eval8d_spine_meningitis_focused_retrieval_20260426.jsonl`
- Expectation validation:
  `artifacts/bench/rag_eval8d_spine_meningitis_focused_retrieval_20260426_expectations.json`
  and `.md`
- Retrieval-only top-8 evaluation:
  `artifacts/bench/rag_eval8d_spine_meningitis_focused_retrieval_20260426_retrieval_top8.json`
  and `.md`
- Retrieval expectation cross-check:
  `artifacts/bench/rag_eval8d_spine_meningitis_focused_retrieval_20260426_retrieval_expectations.json`
  and `.md`

## Commands

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -c "import json, pathlib; p=pathlib.Path('artifacts/prompts/adhoc/rag_eval8d_spine_meningitis_focused_retrieval_20260426.jsonl'); rows=[json.loads(line) for line in p.read_text(encoding='utf-8').splitlines() if line.strip()]; print(f'valid_jsonl rows={len(rows)} ids=' + ','.join(r['id'] for r in rows))"

& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8d_spine_meningitis_focused_retrieval_20260426.jsonl --fail-on-errors --output-json artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_expectations.json --output-md artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_expectations.md

& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval8d_spine_meningitis_focused_retrieval_20260426.jsonl --top-k 8 --output-json artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_retrieval_top8.json --output-md artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_retrieval_top8.md --progress

& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval8d_spine_meningitis_focused_retrieval_20260426.jsonl --retrieval-eval artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_retrieval_top8.json --retrieval-top-k 8 --fail-on-errors --output-json artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_retrieval_expectations.json --output-md artifacts\bench\rag_eval8d_spine_meningitis_focused_retrieval_20260426_retrieval_expectations.md
```

## Results

Pack-level retrieval metrics at `top_k=8`:

- `expected_hit_at_1`: `3/6 (50.0%)`
- `expected_hit_at_3`: `3/6 (50.0%)`
- `expected_hit_at_k`: `3/6 (50.0%)`
- `expected_owner_best_rank`: `1.00` across the three rows with owner hits
- `simple_owner_share`: `16/48 (33.3%)`
- `retrieval_error_rows`: `0`

Row metrics:

| id | expected | top retrieved | hit@k | best rank | note |
| --- | --- | --- | --- | --- | --- |
| `RE8d-SP-001` | `GD-049,GD-232,GD-024,GD-035` | `GD-731,GD-935,GD-298,GD-935,GD-369,GD-941,GD-030,GD-733` | no |  | Reproduces original spine/cold/creek miss. |
| `RE8d-SP-002` | `GD-049,GD-232,GD-024` | `GD-049,GD-584,GD-378,GD-375,GD-049,GD-526,GD-298,GD-396` | yes | 1 | Numb-fingers/drag-to-truck phrasing reaches `GD-049`. |
| `RE8d-SP-003` | `GD-049,GD-232,GD-035` | `GD-731,GD-393,GD-732,GD-922,GD-298,GD-915,GD-359,GD-578` | no |  | Cabin/slick-ground/dirty-creek phrasing misses spine owners. |
| `RE8d-CM-001` | `GD-589,GD-284,GD-298,GD-617,GD-232` | `GD-949,GD-936,GD-733,GD-948,GD-733,GD-918,GD-930,GD-915` | no |  | Reproduces original no-fever connectivity miss. |
| `RE8d-CM-002` | `GD-589,GD-284,GD-298,GD-617,GD-232` | `GD-589,GD-284,GD-589,GD-589,GD-589,GD-298,GD-284,GD-915` | yes | 1 | Explicit fever/dark rash strongly retrieves `GD-589`. |
| `RE8d-CM-003` | `GD-589,GD-284,GD-298,GD-617,GD-232` | `GD-284,GD-589,GD-284,GD-589,GD-617,GD-589,GD-589,GD-911` | yes | 1 | No explicit fever still succeeds when abnormal sleepiness is present. |

The retrieval expectation cross-check completed with `Errors: 0` and
`Warnings: 3`, all `retrieval_missing_expected_owner` warnings for
`RE8d-SP-001`, `RE8d-SP-003`, and `RE8d-CM-001`.

## Interpretation

- The spine miss is reproducible and phrase-sensitive. `neck pain + numb
  fingers + drag to truck` reaches `GD-049`, while creek/cabin/slick-ground
  variants drift to water, search, child-safety, and general preparedness
  surfaces. This still looks like routing/ranking drift, not a content gap.
- The meningitis/sepsis miss is also phrase-sensitive. Explicit fever succeeds
  strongly, and a no-fever variant with abnormal sleepiness succeeds through
  `GD-284`/`GD-589`, but the original stiff-neck + purple-rash + connectivity
  phrasing still misses all expected owners.
- Connectivity, cabin, creek, and general preparedness distractors can still
  dominate when the prompt is compact and lacks one extra medical cue.

## Recommended Next Action

Do not edit guide content from this diagnostic alone. Assign a focused
retrieval/routing worker to inspect the safety-critical query framing and
metadata/rerank cues for:

- `neck/stiff neck/neck hurts` plus `tingling/numbness` plus `carry/move/drag`;
- child `stiff neck` plus `purple/dark rash` without explicit fever, especially
  when connectivity/outage terms are present.

The target fix should improve owner surfacing for `GD-049`/`GD-232` and
`GD-589` without broadening deterministic emergency fallbacks or changing
EVAL8 expectations.

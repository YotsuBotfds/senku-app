# RAG-T4 Owner-Family Concentration Metric

## Slice

Add durable owner-family concentration metrics to RAG diagnostics and trend
tables so guide-family drift is visible without manual CSV inspection.

## Outcome

`scripts/analyze_rag_bench_failures.py` now writes per-row owner-family fields:

- `expected_owner_best_rank`
- `expected_owner_top3_count`
- `expected_owner_topk_count`
- `expected_owner_top3_share`
- `expected_owner_topk_share`

The diagnostic summary now aggregates the same metrics, and
`scripts/rag_trend.py` prints them as first-class trend columns. Trend has two
paths:

- prefer the summary fields when a directory was analyzed after this slice;
- recompute from older row data when the row has `expected_guide_ids` and
  `top_retrieved_guide_ids` but no owner metric fields yet.

This keeps older diagnostic directories useful and avoids false `0/24`
readings for pre-metric artifacts.

## Proof

The metric separates answer/card progress from owner-rank drift. Example trend:

| label | expected_owner_best_rank | top3 | topk | top3_share | topk_share |
| --- | ---: | --- | --- | ---: | ---: |
| FA/FB/FD/FE before | 1.62 | 19/24 | 21/24 | 0.3333 | 0.3418 |
| FA after poisoning alignment | 1.20 | 5/6 | 5/6 | 0.4444 | 0.4444 |

That confirms the poisoning slice improved FA owner concentration while also
showing FA #3 remains a rank-1 drift row rather than an answer-card failure.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\analyze_rag_bench_failures.py scripts\rag_trend.py tests\test_analyze_rag_bench_failures.py tests\test_rag_trend.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_analyze_rag_bench_failures tests.test_rag_trend -v
```

Focused validation passed `36` tests.

## Notes

A parallel wound-owner ranking experiment for FB #6 was tested live and backed
out before commit because it made the exact row worse: the expected wound owner
family disappeared from top-k. The next wound slice should use this metric first
and avoid committing rerank changes until FB #6 owner concentration improves in
live smoke.

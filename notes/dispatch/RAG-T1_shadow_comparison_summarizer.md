# RAG-T1 Shadow Comparison Summarizer

## Slice

Repo-local tooling utility for summarizing shadow retrieval comparison outputs.

## Role

Tooling lane. This supports `RAG-S15b` / `RAG-S16` measurement work and should
make later retrieval experiments faster to interpret.

## Outcome

Adds `scripts/summarize_shadow_comparisons.py`, a stdlib-only CLI that reads
one or more comparison output directories containing
`compare_contextual_shadow_summary.json` and prints a compact Markdown table by
default.

It also supports `--json` for machine-readable output and repeated `--label`
arguments for stable lane names.

Example:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_shadow_comparisons.py artifacts\bench\contextual_shadow_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_stride1_20260425 --label contextual --label section-family --label section-family-stride1
```

Current EZ summary:

| label | row_count | comparable_rows_hit_at_1 | baseline_scored_rows | shadow_scored_rows | baseline_hit_at_1_rate | shadow_hit_at_1_rate | hit_at_1_net | hit_at_3_net | hit_at_k_net | mean_top_k_overlap_jaccard |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| contextual | 6 | 6 | 6 | 6 | 1 | 1 | 0 | 0 | 0 | 0.5492 |
| section-family | 6 | 6 | 6 | 6 | 1 | 1 | 0 | 0 | 0 | 0.2865 |
| section-family-stride1 | 6 | 6 | 6 | 6 | 1 | 1 | 0 | 0 | 0 | 0.4214 |

## Boundaries

- No production retrieval, query, ingest, guide, Android, or benchmark behavior
  changes.
- No new dependencies.
- This is a reader over existing artifacts only.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\summarize_shadow_comparisons.py tests\test_summarize_shadow_comparisons.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_summarize_shadow_comparisons -v
```

## Next

Use this as the first `RAG-T1` utility, then add:

- an agent run manifest writer under ignored `artifacts/runs/`;
- a broader RAG trend table over analyzer artifacts and shadow summaries;
- optional dashboard/eval integrations only after the local summary path is
  stable.

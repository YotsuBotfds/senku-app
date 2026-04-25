# RAG-T2 RAG Diagnostics Trend Summarizer

## Slice

Repo-local trend reader for analyzer `diagnostics.json` artifacts.

## Role

Tooling lane. This is meant to answer "where are we on answer quality?" without
manually opening `report.md`, `diagnostics.csv`, and `diagnostics.json` for
every recent run.

## Outcome

Adds `scripts/summarize_rag_diagnostics.py`, a stdlib-only CLI that reads one or
more `rag_diagnostics_*` directories and prints a compact Markdown table by
default. `--json` emits machine-readable output. Repeated `--label` arguments
can pin stable labels to input directories.

Example:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_rag_diagnostics.py artifacts\bench\rag_diagnostics_20260424_1654_rags12_meningitis_compare_final artifacts\bench\rag_diagnostics_20260424_1750_rags13_code_health_final_smoke --label rags12-gap --label rags13-final
```

Current comparison:

| label | generated_at | total_rows | expected_rows | bad_buckets | hit_at_1_rate | hit_at_3_rate | hit_at_k_rate | cited_rate | strong_supported | moderate_supported | uncertain_fit_accepted | card_pass | card_partial | card_fail | claim_pass | generated_shadow_card_gap_rows | generated_model | reviewed_card_runtime | deterministic_rule | quality_score_10 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| rags12-gap | 2026-04-24T16:53:52 | 24 | 24 | 1 | 0.875 | 1 | 1 | 0.9583 | 21 | 0 | 2 | 8 | 0 | 0 | 8 | 0 | 1 | 8 | 13 | 9.48 |
| rags13-final | 2026-04-24T17:49:11 | 24 | 24 | 0 | 0.875 | 1 | 1 | 1 | 22 | 0 | 2 | 8 | 0 | 0 | 9 | 0 | 1 | 8 | 13 | 9.84 |

## Quality Score

`quality_score_10` is a transparent selected-panel heuristic, not a global app
rating. It combines:

- no bad failure bucket rows;
- expected-guide hit and citation rates;
- app acceptance support;
- answer-card pass/partial/fail status;
- claim-support pass rate for generated/reviewed-card answers.

Use it as a quick trend dial for a known panel. Do not use it to claim the whole
guide-answering system is `9/10`.

## Boundaries

- No production retrieval, query, guide, Android, or analyzer behavior changes.
- No new dependencies.
- Reads existing analyzer artifacts only.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\summarize_rag_diagnostics.py tests\test_summarize_rag_diagnostics.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_summarize_rag_diagnostics -v
```

## Next

- Add owner-family concentration to shadow retrieval comparison so expected
  owner hit rates cannot hide drift toward broad backup guides.
- Add a durable run manifest for shadow/eval smokes.
- Use the trend table at session start before deciding whether to work on
  retrieval, card applicability, generation shape, or UI surfacing.

# RAG-S20 Reviewed-Card Rank-Drift Taxonomy

## Slice

Treat strict reviewed-card runtime answers as supported when the answer cites an
expected owner and passes answer-card, claim-support, and evidence-nugget
contracts, even if the expected owner was not rank 1.

## Role

Main agent / analyzer tooling slice.

## Preconditions

- `RAG-T4` owner-family concentration metrics are present in analyzer and trend
  outputs.
- `RAG-S18` and `RAG-S19` have already moved FA poisoning and FB wound rows onto
  reviewed-card runtime answers.

## Outcome

- `scripts/analyze_rag_bench_failures.py` keeps ordinary generated rank drift as
  `ranking_miss`.
- Reviewed-card runtime rank drift is promoted to `expected_supported` only when:
  - the row would otherwise be `ranking_miss`;
  - provenance is `reviewed_card_runtime`;
  - an expected guide is cited;
  - answer-card, claim-support, and evidence-nugget diagnostics are all `pass`.
- Rank drift remains visible through `expected_owner_best_rank`,
  top-3/top-k counts, and owner-share metrics.

## Acceptance

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\analyze_rag_bench_failures.py tests\test_analyze_rag_bench_failures.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_analyze_rag_bench_failures -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\guide_wave_fa_20260425_143154.json artifacts\bench\guide_wave_fb_20260425_144548.json artifacts\bench\guide_wave_fd_20260425_144802.json artifacts\bench\guide_wave_fe_20260425_144829.json --output-dir artifacts\bench\rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_rag_diagnostics.py artifacts\bench\rag_diagnostics_20260425_1448_fa_fb_fd_fe_current artifacts\bench\rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy --label current-before-taxonomy --label current-taxonomy
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_trend.py artifacts\bench\rag_diagnostics_20260425_1448_fa_fb_fd_fe_current artifacts\bench\rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy --label current-before-taxonomy --label current-taxonomy
```

## Proof

Fresh FA/FB/FD/FE taxonomy proof:

- `artifacts/bench/rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy/`
- Bucket counts: `12` deterministic pass, `11` expected supported, `1`
  abstain/clarify.
- Quality score moved from `8.54` to `8.83`.
- Bad diagnostic buckets moved from `2` to `0`.
- Owner concentration did not change: best rank `1.36`, top-3 `21/24`, top-k
  `22/24`, top-3 share `0.3750`, top-k share `0.4118`.

## Boundaries

- This is analyzer taxonomy only. It does not change retrieval, reranking,
  answer generation, reviewed-card runtime behavior, guide text, or Android.
- Do not use this as permission to hide owner drift. Any follow-up ranking work
  should target the owner concentration metrics directly.

## Next Step

The remaining score gap is no longer a diagnostic bucket problem. It is mostly
retrieval concentration plus one weak app-acceptance row:

- FA #3 still has expected owner best rank `2`.
- FB #6 still has expected owner best rank `4`.
- FB #5 is an ambiguous/no-context prompt that surfaces as
  `uncertain_fit_accepted` and `needs_evidence_owner`.

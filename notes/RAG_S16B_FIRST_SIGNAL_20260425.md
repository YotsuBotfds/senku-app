# RAG-S16b First Shadow Retrieval Signal - 2026-04-25

## Purpose

Record the first measured comparison between:

- production bench retrieval baselines,
- `RAG-S15` contextual chunk shadow export, and
- `RAG-S16` section-family / RAPTOR-lite shadow export.

This note is measurement-only. It does not promote either shadow lane to
production retrieval.

## Comparator Hygiene

Commit `12fa3e5` changed `scripts/compare_contextual_shadow_retrieval.py` so
rows with no retrieval candidates on one side are unscored for that side. This
prevents deterministic/no-retrieval rows in older bench artifacts from counting
as fake baseline misses or fake shadow improvements.

Interpretation rules:

- Use `deltas.*.comparable_rows` for direct A/B claims.
- `baseline.scored_rows` and `shadow.scored_rows` can differ; those are
  side-specific denominators.
- `top_k_overlap_jaccard` is rank-blind set overlap, useful for drift checks
  but not for rank-quality claims.

## EX Airway Smoke

Inputs:

- Baseline: `artifacts/bench/guide_wave_ex_20260424_1410_child_choking_gate.json`
- Contextual shadow: `artifacts/tmp/contextual_shadow_ex_airway.jsonl`
- Section-family shadow: `artifacts/tmp/section_family_ex_airway.jsonl`

Result:

- Contextual shadow and section-family shadow each scored `6/6` expected-guide
  hit@1/hit@3/hit@k.
- Only `2` rows are baseline-vs-shadow comparable because the older baseline
  artifact carries ranked retrieval candidates for only those generated rows.
- Comparable deltas are unchanged: baseline and shadow are both `2/2` on
  hit@1/hit@3/hit@k.

Read: this smoke proves comparator compatibility and shadow coverage, not a
true retrieval improvement.

## EZ Newborn Comparable Panel

Inputs:

- Baseline: `artifacts/bench/guide_wave_ez_20260424_162406.json`
- Contextual shadow export:
  `artifacts/tmp/contextual_shadow_ez_newborn.jsonl` (`541` chunks)
- Section-family export:
  `artifacts/tmp/section_family_ez_newborn.jsonl` (`42` records)
- Overlapping section-family export:
  `artifacts/tmp/section_family_ez_newborn_stride1.jsonl` (`79` records)

Outputs:

- `artifacts/bench/contextual_shadow_compare_ez_newborn_20260425/`
- `artifacts/bench/section_family_compare_ez_newborn_20260425/`
- `artifacts/bench/section_family_compare_ez_newborn_stride1_20260425/`

One-command summary:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_shadow_comparisons.py artifacts\bench\contextual_shadow_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_stride1_20260425 --label contextual --label section-family --label section-family-stride1
```

Summary:

| Retrieval lane | Comparable rows | hit@1 | hit@3 | hit@k | Mean top-k overlap |
| --- | ---: | ---: | ---: | ---: | ---: |
| Contextual shadow | 6 | 6/6 | 6/6 | 6/6 | 0.5492 |
| Section-family shadow, non-overlap | 6 | 6/6 | 6/6 | 6/6 | 0.2865 |
| Section-family shadow, stride 1 | 6 | 6/6 | 6/6 | 6/6 | 0.4214 |

Read:

- EZ is a ceiling panel for this metric. Production baseline is already `6/6`
  hit@1, so neither shadow lane can show an expected-owner hit-rate
  improvement.
- Contextual shadow preserves the newborn owner family cleanly and stays closer
  to the baseline candidate set.
- Section-family shadow still hits expected owners, but drifts more strongly
  toward broader support guides (`GD-232`, `GD-589`) and away from the exact
  baseline owner mix on several rows.
- Overlapping section-family windows reduce that drift versus non-overlap, but
  still do not match contextual chunks for candidate-set stability on this
  panel.

## Product Implication

We are closer, but this is not yet a promotion signal.

The immediate value of `RAG-S15`/`S16` is instrumentation:

- generate alternate retrieval indexes without touching production,
- compare them against existing bench artifacts,
- separate coverage from comparable-row improvement,
- identify when a candidate preserves owner-family safety.

Next proof needs panels where baseline is not already perfect at hit@1 and where
baseline artifacts carry `top_retrieved_guide_ids` on most rows.

## Next Slices

1. Add an owner-family concentration metric to the comparator, because broad
   expected families can hide drift from primary owner guides to backup support
   guides.
2. Run contextual and section-family shadows on a weaker-but-comparable panel
   after identifying one with enough baseline retrieval candidates.
3. Promote nothing until a candidate shows positive comparable-row deltas or
   better owner-family concentration without safety regressions.

# RAG-S16c Owner-Family Concentration Metric

## Slice

Add primary-owner concentration diagnostics to shadow retrieval comparison.

## Role

Measurement lane. This closes the gap where broad expected-guide families could
make a shadow index look perfect on hit@1 while it drifted from primary owner
guides toward backup support guides.

## Outcome

`scripts/compare_contextual_shadow_retrieval.py` now keeps raw expected-guide
hit@1/hit@3/hit@k unchanged, and adds a second primary-owner layer:

- `primary_expected_guide_ids`
- `backup_expected_guide_ids`
- `baseline_primary_hit_at_1` / `hit_at_3` / `hit_at_k`
- `shadow_primary_hit_at_1` / `hit_at_3` / `hit_at_k`
- `baseline_owner_family_concentration`
- `shadow_owner_family_concentration`
- `owner_family_concentration_delta`

The concentration formula is intentionally simple:

```text
unique primary expected guide IDs in returned candidates / returned candidate count
```

Rows with no primary-owner expectation remain unscored for the primary layer.

## Manifest

`notes/specs/rag_prompt_expectations_seed_20260424.yaml` now declares
`primary_expected_guides` for the currently measured safety waves:

- `EX`: primary airway/choking owners `GD-232`, `GD-579`
- `EY`: primary sepsis/meningitis owner `GD-589`
- `EZ`: primary newborn/pediatric owners `GD-492`, `GD-298`, `GD-617`, `GD-284`
- `FC`: primary abdominal/trauma owners `GD-380`, `GD-297`

Backup guides are derived as expected guides not in the primary set.

## EZ Signal

Using the regenerated EZ newborn shadow comparisons:

| label | raw hit@1 | primary hit@1 | owner concentration delta | top-k overlap |
| --- | ---: | ---: | ---: | ---: |
| contextual | 6/6 | 4/6 | +0.0500 | 0.5492 |
| section-family | 6/6 | 4/6 | -0.1722 | 0.2865 |
| section-family-stride1 | 6/6 | 4/6 | -0.0555 | 0.4214 |

Read:

- Raw hit@1 is a ceiling metric on EZ.
- Primary-owner metrics expose the backup-guide drift that raw hit@1 hides.
- Contextual chunks preserve primary-owner concentration best on this panel.
- Overlapping section-family windows reduce drift compared with non-overlap but
  still trail contextual chunks.

One-command summary:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_shadow_comparisons.py artifacts\bench\contextual_shadow_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_stride1_20260425 --label contextual --label section-family --label section-family-stride1
```

## Boundaries

- No production retrieval, query, ingest, guide, Android, or prompt behavior
  changes.
- Existing raw expected-guide hit metrics are unchanged.
- This is analyzer/comparator output only.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\compare_contextual_shadow_retrieval.py scripts\summarize_shadow_comparisons.py tests\test_compare_contextual_shadow_retrieval.py tests\test_summarize_shadow_comparisons.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_compare_contextual_shadow_retrieval tests.test_summarize_shadow_comparisons -v
```

Live EZ comparisons were regenerated for contextual, section-family, and
section-family stride-1 shadow artifacts.

## Next

Use this metric before promoting any retrieval candidate. The next answer-quality
slice should inspect why reviewed-card answers trigger for only part of the
selected panel: retrieval absence, routing/profile mismatch, or answer-card
applicability conditions.

# RAG-CH6 Mobile Pack Count Probe Helper

Date: 2026-04-26

## Scope

Add a safe helper for comparing Android bundled mobile-pack counts against a
fresh current-head mobile-pack export without requiring local Git LFS assets to
be hydrated.

This is Android migration tooling only. It does not modify the Android asset
pack, push a pack to devices, product-enable reviewed-card runtime, or expand
runtime card selection.

## Changed Surfaces

- `scripts/compare_mobile_pack_counts.py`
- `tests/test_compare_mobile_pack_counts.py`

The helper accepts a pack directory, `senku_manifest.json`, or
`senku_mobile.sqlite3`. It reports:

- manifest counts;
- materialized SQLite table counts where available;
- missing optional table errors;
- Git LFS pointer metadata and whether the pointer matches manifest file
  metadata;
- manifest-vs-SQLite internal count drift;
- baseline-to-candidate count deltas.

## Current Probe

Fresh current-head export:

```powershell
$Python = ".\.venvs\senku-validate\Scripts\python.exe"
$Stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$PackDir = "artifacts\mobile_pack\senku_current_head_$Stamp"
& $Python -B scripts\export_mobile_pack.py $PackDir `
  --chroma-dir db `
  --guides-dir guides `
  --collection-name senku_guides `
  --vector-dtype float16 `
  --mobile-top-k 10
```

Export produced:

- `artifacts/mobile_pack/senku_current_head_20260426_232032/`
- `answer_cards`: 271
- SQLite bytes: 290,738,176
- SQLite sha256: `bca1dc3d6de3e8ecd4d2ac585b97e4914974cb6d6889443a313646f295d686c5`
- vector bytes: 76,555,808
- vector sha256: `5c4decacbf506b31acf8ae1d2568771be24004c46c96944456c8d33b7948eeb1`

Comparison:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\compare_mobile_pack_counts.py `
  android-app\app\src\main\assets\mobile_pack `
  artifacts\mobile_pack\senku_current_head_20260426_232032 `
  --output artifacts\mobile_pack\compare_android_asset_vs_current_head_20260426_232032.json
```

Observed baseline/candidate shape:

- Android bundled SQLite is an unresolved Git LFS pointer locally.
- The pointer sha256/bytes match the Android manifest.
- Android manifest baseline: `answer_cards=6`, `chunks=49726`,
  `retrieval_metadata_guides=233`.
- Current-head export: `answer_cards=271`, `answer_card_clauses=6945`,
  `answer_card_sources=311`, `chunks=49841`,
  `retrieval_metadata_guides=237`.
- No manifest/SQLite count drift was observed in the current-head export.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\compare_mobile_pack_counts.py tests\test_compare_mobile_pack_counts.py
# OK

& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_compare_mobile_pack_counts tests.test_mobile_pack_manifest_parity tests.test_run_mobile_headless_preflight -v
# Ran 8 tests - OK
```

## Boundary

This helper makes pack growth and local asset hydration status explicit. It is
not itself evidence that the broader 271-card desktop inventory is Android
runtime-ready.

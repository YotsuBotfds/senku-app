# Post-Rebuild Sanity Check

Run this immediately after the full rebuild completes.

## 1. Collection stats

```powershell
.\scripts\post_rebuild_sanity_check.ps1
```

What we want to see:
- collection stats return cleanly
- manifest exists again under `db/ingest_manifest.json`
- manifest entry count looks corpus-scale, not partial

## 2. Prompt packs already staged

- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_af_20260415.txt`
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ag_20260415.txt`
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ah_20260415.txt`

## 3. Validation order

```powershell
.\scripts\run_guide_prompt_validation.ps1 -Wave af
.\scripts\run_guide_prompt_validation.ps1 -Wave ag
.\scripts\run_guide_prompt_validation.ps1 -Wave ah
```

## 4. Families these packs target

- `wave_af`
  - baby discomfort vs infant/child routing
  - household chemicals vs child unknown-ingestion routing
  - water storage/rationing vs tank-maintenance surfacing
  - symptom-first mild-medical complaint routing
- `wave_ag`
  - dental prevention vs emergency routing
  - household chemicals vs child unknown-ingestion realistic phrasing
- `wave_ah`
  - household chemicals complaint-first alias coverage
  - rat poison / mouse bait / rodenticide
  - toddler licked cleaner
  - cleaner or bleach splashed in eye
  - heater/stove indoor headache
  - paint thinner / paint fumes / varnish exposure
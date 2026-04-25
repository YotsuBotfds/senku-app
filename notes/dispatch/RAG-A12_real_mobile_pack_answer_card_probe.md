# RAG-A12 Real Mobile-Pack Answer-Card Probe

## Slice

Prove the reviewed-card mobile-pack path against a real exported pack, not only
unit fixtures and synthetic Android temp databases.

## Outcome

Landed as a probe artifact.

- Export command:

```powershell
.\.venvs\senku-validate\Scripts\python.exe -B scripts\export_mobile_pack.py artifacts\mobile_pack\senku_20260424_answer_cards_probe_20260424_190810
```

- Export artifact:
  `artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`
- Manifest counts include `754` guides, `49,726` chunks, and `6` answer cards.
- SQLite proof:
  - `answer_cards`: `6`
  - `answer_card_clauses`: `116`
  - `answer_card_sources`: `19`
  - `SOURCELESS_CARDS`: none
  - `CLAUSELESS_CARDS`: none
  - `review_status`: `pilot_reviewed:6`
- Card IDs:
  - `abdominal_internal_bleeding`
  - `choking_airway_obstruction`
  - `infected_wound_spreading_infection`
  - `meningitis_sepsis_child`
  - `newborn_danger_sepsis`
  - `poisoning_unknown_ingestion`
- Exported SQLite SHA-256:
  `bf2d8e616c2855c63ab52d72c537290ca2e022b967e975368059cce1a3366540`
- Exported vector SHA-256:
  `893a7d5704603dd2dfb9645dd90c845e9ede61f4d1f27b5e7fe86bee5df81802`

## Boundaries

- Did not overwrite `android-app/app/src/main/assets/mobile_pack`.
- Did not install the real exported pack into Android.
- Did not run screenshot/UI harness against this pack.

## Next

- Use this artifact as the reference for a later real-pack Android E2E.
- Keep the current app runtime dark until the developer-panel toggle and
  screenshot/layout proof are paired with the reviewed-card evidence surface.

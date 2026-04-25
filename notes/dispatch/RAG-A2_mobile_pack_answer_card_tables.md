# RAG-A2 Mobile-Pack Answer-Card Tables

## Slice

Add optional reviewed answer-card metadata tables to the mobile-pack SQLite
export and manifest without changing Android runtime behavior.

## Role

Main agent or Python pack worker on `gpt-5.5 medium`. Use high only if the
slice expands into runtime answer composition or safety-critical card wording.

## Preconditions

- Desktop contract proof:
  `artifacts/bench/rag_diagnostics_20260424_1750_rags13_code_health_final_smoke/report.md`
- Translation note:
  `notes/ANDROID_RAG_CONTRACT_TRANSLATION_20260424.md`
- Android receiving-shape slice:
  `notes/dispatch/RAG-A1_android_answer_surface_receiving_shape.md`

## Target Files

- `mobile_pack.py`
- `guide_answer_card_contracts.py`
- `tests/test_mobile_pack.py`
- `notes/MOBILE_OFFLINE_PACK_SPEC_20260410.md`

## Outcome

- `mobile_pack.py` now exports optional metadata-only SQLite tables:
  - `answer_cards`
  - `answer_card_clauses`
  - `answer_card_sources`
- Manifest `counts.answer_cards`, `pack_meta.answer_card_count`, and
  `schema.sqlite_tables` now report those tables.
- Synthetic unit tests cover:
  - empty optional table export;
  - one card with required, conditional, first-action, red-flag, forbidden,
    `do_not`, uncertain-fit, and source-section rows;
  - rejection when an included card references an unknown source guide.
- `guide_answer_card_contracts.py` now preserves card metadata used by mobile
  export: `slug`, `evidence_owner`, `routine_boundary`, and `notes`.
- Follow-up `RAG-A4` extracted the answer-card preparation and SQLite insertion
  helpers into `mobile_pack_answer_cards.py` while keeping `mobile_pack.py`
  compatibility aliases.

## Validation

- `python -B -m py_compile mobile_pack.py guide_answer_card_contracts.py tests\test_mobile_pack.py`
- `python -B -m unittest tests.test_mobile_pack -v` passed `51` tests.
- `python -B -m unittest tests.test_mobile_pack_manifest_parity tests.test_guide_answer_card_contracts -v` passed `29` tests.
- `python -B scripts\validate_guide_answer_cards.py` validated `6` cards.
- Real guide set smoke found `6` exportable answer cards:
  `abdominal_internal_bleeding`, `choking_airway_obstruction`,
  `infected_wound_spreading_infection`, `meningitis_sepsis_child`,
  `newborn_danger_sepsis`, and `poisoning_unknown_ingestion`.
- Android compatibility check passed:
  `.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.PackManifestTest --tests com.senku.mobile.PackInstallerTest`.

## Boundaries

- Tables are optional metadata. Android runtime behavior is unchanged.
- Do not make `answer_cards` required in `PackManifest`.
- Do not route runtime answers from cards until Android has old-pack fallback
  reader tests.
- Do not reuse `SearchResult` as the card model.

## Next Slice

The backward-compatible Android reader/model layer landed in
`notes/dispatch/RAG-A3_android_answer_card_reader.md`. Runtime composition still
comes later, after connected DAO instrumentation passes on a device/emulator.

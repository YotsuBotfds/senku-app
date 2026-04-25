# RAG-A13 Android Asset-Pack Answer-Card Parity Closeout

## Slice

`RAG-A13` - Android asset-pack answer-card parity.

## Outcome

The checked-in Android asset pack now matches the proven answer-card probe pack
for the six pilot-reviewed cards.

The previous checked-in
`android-app/app/src/main/assets/mobile_pack` was replaced with the proven probe
pack at
`artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`.

Bundled manifest:

- `generated_at`: `2026-04-25T00:08:46.459832+00:00`
- `answer_cards`: `6`

SQLite counts:

- `answer_cards`: `6`
- `answer_card_clauses`: `116`
- `answer_card_sources`: `19`

All six cards have `review_status=pilot_reviewed`, and validation found no
missing clause or source links.

## Runtime Policy

Runtime behavior remains off by default. The proof path uses the existing
developer/test flag; this closeout does not product-enable reviewed-card
answers.

Fresh APK installs no longer require a manual probe-pack push for the six
pilot-reviewed cards. Card expansion and product enablement remain gated.

## Validation

Build/unit checks passed:

```powershell
:app:testDebugUnitTest --tests com.senku.mobile.PackManifestTest --tests com.senku.mobile.AnswerCardRuntimeTest --tests com.senku.mobile.SessionMemoryTest
:app:assembleDebug
:app:assembleDebugAndroidTest
```

Clean-install no-push matrix passed with APK SHA
`be32ff34f66e7d478082ae2bd292f0e8315f231ec9f55c4852debfdd0e3cc553`.

Prompt assertion:

- query: `my child swallowed an unknown cleaner`
- reviewed-card ID: `poisoning_unknown_ingestion`
- guide: `GD-898`
- review status: `pilot_reviewed`
- recent-thread metadata assertion: enabled

Artifacts:

- tablet portrait:
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5554/20260424_210714_546/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5556/20260424_210714_546/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5558/20260424_210714_555/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5560/20260424_210714_546/emulator-5560`

## Harness Cleanup

The smoke-script artifact pre-clean step now uses `mkdir -p` under `run-as`, so
fresh installs no longer emit the trailing `files/test-artifacts` warning after
successful artifact copy.

Follow-up proof:

- `artifacts/android_reviewed_card_asset_pack_harness_20260424/warning_fix_5556/20260424_210857_505/emulator-5556`

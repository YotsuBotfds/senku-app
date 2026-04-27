# RAG-A14k - Android Current-Head Reviewed Prompt Canary

Date: 2026-04-26

## Scope

Retry the reviewed-card prompt canary against a pushed current-head 271-card
mobile pack after adding A14j platform-ANR summary classification.

This is proof-only. It does not replace checked-in Android assets, broaden
runtime selection, change product exposure, or turn reviewed-card runtime on by
default.

## Setup

`emulator-5556` no longer had the app installed, so the lane was restored with a
debug app/test install, then the current-head pack was pushed again:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\push_mobile_pack_to_android.ps1 `
  -Device emulator-5556 `
  -PackDir artifacts\mobile_pack\senku_current_head_20260426_232032 `
  -ForceStop `
  -ShowInstalledManifest
```

Installed pack verification:

- SQLite: `290738176` bytes,
  `bca1dc3d6de3e8ecd4d2ac585b97e4914974cb6d6889443a313646f295d686c5`
- Vectors: `76555808` bytes,
  `5c4decacbf506b31acf8ae1d2568771be24004c46c96944456c8d33b7948eeb1`
- Manifest counts: `answer_cards=271`, `guides=754`, `chunks=49841`,
  `deterministic_rules=9`, `guide_related_links=5750`,
  `retrieval_metadata_guides=237`

## Canary

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device emulator-5556 `
  -TestClass com.senku.mobile.PromptHarnessSmokeTest `
  -SkipBuild `
  -SkipInstall `
  -Orientation portrait `
  -ScriptedQuery "my child swallowed an unknown cleaner" `
  -ScriptedAsk `
  -ScriptedEnableReviewedCardRuntime `
  -ScriptedExpectedSurface detail `
  -ScriptedExpectedTitle "my child swallowed an unknown cleaner" `
  -ScriptedCaptureLabel prompt_detail `
  -ScriptedExpectedAnswerSurfaceLabel "REVIEWED EVIDENCE" `
  -ScriptedForbiddenAnswerSurfaceLabels "STRONG EVIDENCE" `
  -ScriptedExpectedRuleId "answer_card:poisoning_unknown_ingestion" `
  -ScriptedExpectedSourceGuideId "GD-898" `
  -ScriptedExpectedReviewedCardId "poisoning_unknown_ingestion" `
  -ScriptedExpectedReviewedCardGuideId "GD-898" `
  -ScriptedExpectedReviewedCardReviewStatus "pilot_reviewed" `
  -ScriptedExpectedReviewedCardSourceGuideIds "GD-898" `
  -ScriptedAssertRecentThreadReviewedCardMetadata `
  -ArtifactRoot artifacts/android_current_head_pack_runtime_canary_retry_5556 `
  -SummaryPath artifacts\android_current_head_pack_runtime_canary_retry_5556\summary.json `
  -CaptureLogcat `
  -ClearLogcatBeforeRun
```

Result:

- `OK (1 test)`
- wrapper `status=pass`
- `installed_pack.counts.answer_cards=271`
- `scripted_query="my child swallowed an unknown cleaner"`
- `scripted_expected_surface=detail`
- `scripted_forbidden_answer_surface_labels="STRONG EVIDENCE"`
- `platform_anr=null`

Artifact root:

`artifacts/android_current_head_pack_runtime_canary_retry_5556/20260426_235215_006/emulator-5556`

## Boundary

This proves the existing six-card reviewed runtime can compose the poisoning
pilot from a pushed current-head pack. It does not make the remaining 265 cards
runtime-ready.

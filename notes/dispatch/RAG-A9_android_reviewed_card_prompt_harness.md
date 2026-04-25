# RAG-A9 Android Reviewed-Card Prompt Harness

## Slice

Android reviewed-card runtime automation proof.

## Status

Landed 2026-04-24.

## Outcome

`PromptHarnessSmokeTest#scriptedPromptFlowCompletes` now has opt-in reviewed-card
runtime arguments for scripted prompt runs. The harness can enable
`ReviewedCardRuntimeConfig` inside instrumentation, launch the normal
`MainActivity` automation path, and assert that the resulting `DetailActivity`
surface keeps:

- answer surface label: `REVIEWED EVIDENCE`;
- rule ID: `answer_card:poisoning_unknown_ingestion`;
- primary source guide: `GD-898`;
- answer-body fragments: `Call poison control`, `Avoid: Do not induce vomiting`,
  and `GD-898`.

This keeps runtime enablement in instrumentation preferences instead of exported
activity intent extras.

## Code Changes

- `DetailActivity.newPendingAnswerIntent(...)` preserves deterministic prepared
  answer bodies and `prepared.ruleId`.
- `DetailActivity` passes `answer_card:` rule IDs into `AnswerContentFactory`
  for Paper card rendering and tablet/current-turn content.
- Phone-landscape `activity_detail.xml` now includes `detail_answer_card` and
  `detail_body_mirror_shell`, matching the Paper-card path available in portrait.
- Reviewed-card detail body labels use `Reviewed evidence` so first-viewport
  phone-landscape state does not read as generic deterministic evidence.
- `AnswerCardDao` orders sources by `is_primary DESC` before guide ID so the
  reviewed card primary source remains `GD-898`.
- `run_android_instrumented_ui_smoke.ps1` and `run_android_prompt.ps1` expose
  scripted reviewed-card runtime and assertion switches.

## Proof

Real pack used:

`artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`

Important: after installing a fresh APK, repush the real probe pack. A normal
app install can leave the app using the asset pack, which currently has no
answer-card tables.

Trusted final matrix run after fresh APK install and real-pack repush:

- tablet portrait `emulator-5554`:
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_475/emulator-5554/`
- phone portrait `emulator-5556`:
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_475/emulator-5556/`
- tablet landscape `emulator-5558`:
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_566/emulator-5558/`
- phone landscape `emulator-5560`:
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_566/emulator-5560/`

All four runs passed `PromptHarnessSmokeTest#scriptedPromptFlowCompletes`.

Additional focused proof:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest"
```

Passed on the documented four-emulator matrix.

## Repro Command Shape

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\push_mobile_pack_to_android.ps1 `
  -Device emulator-5556 `
  -PackDir artifacts\mobile_pack\senku_20260424_answer_cards_probe_20260424_190810 `
  -ForceStop

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device emulator-5556 `
  -SkipBuild `
  -SkipInstall `
  -SmokeProfile custom `
  -Orientation portrait `
  -ScriptedQuery "my child swallowed an unknown cleaner" `
  -ScriptedAsk `
  -ScriptedExpectedSurface detail `
  -ScriptedExpectedTitle "my child swallowed an unknown cleaner" `
  -ScriptedTimeoutMs 30000 `
  -EnableHostInferenceSmoke `
  -AllowHostFallback `
  -ScriptedEnableReviewedCardRuntime `
  -ScriptedExpectedAnswerSurfaceLabel "REVIEWED EVIDENCE" `
  -ScriptedExpectedRuleId "answer_card:poisoning_unknown_ingestion" `
  -ScriptedExpectedSourceGuideId "GD-898" `
  -ScriptedExpectedBodyContains "Call poison control|Avoid: Do not induce vomiting|GD-898"
```

Host inference is enabled only to satisfy the current ask gate; the reviewed-card
runtime returns before generation is needed.

## Follow-Ups

- Product enablement remains deferred; runtime is still off by default.
- The current asset pack still lacks answer-card tables. Either update assets in
  a dedicated pack-refresh slice or keep repushing the real probe pack for A9
  reproductions.
- Phone-landscape still has tight vertical space with the docked composer. The
  reviewed body label is visible immediately, but the full Paper card content
  may require scrolling.

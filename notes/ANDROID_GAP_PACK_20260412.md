# Android Gap Pack - 2026-04-12

This note documents the compact Android validation pack at [`../artifacts/prompts/adhoc/android_gap_pack_20260412.jsonl`](../artifacts/prompts/adhoc/android_gap_pack_20260412.jsonl).

## Purpose

This pack is a compact replay set for the current Android coverage mismatch called out in [`ANDROID_SCOUT_FINDINGS_20260412.md`](./ANDROID_SCOUT_FINDINGS_20260412.md).

It is intentionally not a general smoke pack. It is a gap pack:

- `20` first-turn prompts total
- `8` follow-up-enabled cases total
- `5` families with `4` first-turn prompts each
- session-style JSONL so a future harness can run single-turn and in-thread follow-up passes from the same file

## Families Covered

- `comms_governance_security`
- `water_infra_sanitation`
- `construction_watercraft`
- `multi_objective_separation`
- `craft_refinement`

## Where The Prompts Came From

Most prompts were pulled directly from the structured prompt packs:

- [`../artifacts/prompts/adhoc/senku_762_prompt_pack_with_carryforward_v2_20260410.jsonl`](../artifacts/prompts/adhoc/senku_762_prompt_pack_with_carryforward_v2_20260410.jsonl)

The chat-follow-up construction prompts came from the Android follow-up files already used in live emulator work:

- [`../artifacts/prompts/adhoc/android_session_followups_20260411.jsonl`](../artifacts/prompts/adhoc/android_session_followups_20260411.jsonl)
- [`../artifacts/prompts/android_followup_regression_20260411.jsonl`](../artifacts/prompts/android_followup_regression_20260411.jsonl)
- [`../artifacts/prompts/adhoc/android_lane_construction_20260411.txt`](../artifacts/prompts/adhoc/android_lane_construction_20260411.txt)

Two active water-distribution follow-up lines were taken from the current Android notes instead of the main prompt packs because they are already part of the validated emulator lane:

- [`NEXT_AGENT_HANDOFF_20260411.md`](./NEXT_AGENT_HANDOFF_20260411.md)
- [`ANDROID_EMULATOR_SWEEP_20260411.md`](./ANDROID_EMULATOR_SWEEP_20260411.md)

One generic follow-up prompt, `what gets priority first`, came from the recommended follow-up patterns in [`ANDROID_SCOUT_FINDINGS_20260412.md`](./ANDROID_SCOUT_FINDINGS_20260412.md).

## Pack Shape

Each JSONL line is one runnable case with:

- `case_id`
- `family`
- `focus`
- `priority`
- `suggested_emulator`
- `initial_query`
- `follow_up_queries`
- `expected_behavior`
- `source_refs`
- `baseline_artifacts`

The file is meant to support two passes:

- single-turn pass over all `20` records
- follow-up pass only on the `8` records where `follow_up_queries` is non-empty

## Recommended Emulator Lanes

- `emulator-5554`: water infrastructure/sanitation plus multi-objective separation
- `emulator-5556`: comms/governance/security plus gravity-fed water-distribution
- `emulator-5558`: construction and watercraft
- `emulator-5560`: craft/refinement

This follows the current role split in [`ANDROID_EMULATOR_SWEEP_20260411.md`](./ANDROID_EMULATOR_SWEEP_20260411.md).

## Run Guidance

Single-turn replay:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_prompt.ps1 `
  -Emulator emulator-5556 `
  -Query "We run couriers between camps. What is the simplest way to prove a note is real without making the system too fragile?" `
  -Ask `
  -WaitForCompletion `
  -MaxWaitSeconds 180 `
  -PollSeconds 10 `
  -DumpPath artifacts\bench\gap_pack_example.xml
```

Follow-up replay:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_detail_followup.ps1 `
  -Emulator emulator-5558 `
  -InitialQuery "how do i build a house" `
  -FollowUpQuery "what about drainage" `
  -InferenceMode host `
  -MaxWaitSeconds 180
```

Gap-pack replay:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_gap_pack.ps1 `
  -GapPackPath artifacts\prompts\adhoc\android_gap_pack_20260412.jsonl `
  -Mode both `
  -InferenceMode host `
  -MaxCases 4
```

Practical sequence:

1. Run all first turns once on their suggested emulator lane.
2. Re-run only the `8` follow-up-enabled cases through the detail follow-up harness.
3. Grade anchor-guide family, support drift, section choice, and latency band.
4. Compare any ambiguous Android result against desktop before calling it a true content gap.

## Useful Current Baselines

Strong current Android references that line up with this pack:

- water-distribution follow-up baseline:
  - [`../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.json`](../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.json)
- broad house follow-up baseline:
  - [`../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.json`](../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.json)
- candle/tallow follow-up baseline:
  - [`../artifacts/bench/host_mode_detail_20260412/candles_followup_5560_gpu_v7_stagepenaltyguard.json`](../artifacts/bench/host_mode_detail_20260412/candles_followup_5560_gpu_v7_stagepenaltyguard.json)

Known useful miss examples that justify this pack:

- courier-authentication miss:
  - [`../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml)
- soapmaking miss:
  - [`../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml)

## Early Spotcheck Results

The first targeted spotchecks confirmed that this pack is finding real Android breadth gaps instead of synthetic edge cases.

- courier authentication on `emulator-5556`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml)
  - result: generic answer about markers/tracking
  - read: usable wording but not a real note-authentication system
- soap from animal fat on `emulator-5560`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml)
  - result: wrong-family grounding
  - read: cited rabies/animal-bite content instead of soapmaking or fat-processing guidance
- canoe on `emulator-5558`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml)
  - result: broadly plausible dugout-canoe answer
  - read: not an obvious failure, though source visibility still needs better capture
- multi-objective separation on `emulator-5554`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml)
  - result: answer blended priorities instead of separating medical/shelter/water lanes cleanly
  - read: likely more of an answer-shaping/session issue than a pure missing-knowledge issue

These early replays make the current gap-pack priorities clearer:

1. comms/governance/security is still under-shaped
2. craft/refinement still has severe retrieval misses
3. multi-objective separation needs structured-answer evaluation, not just source-family checks

## Spotcheck Results So Far

These were replayed after the pack was assembled to confirm it is finding real Android weaknesses rather than hypothetical ones.

- courier-authentication on `emulator-5556`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml)
  - grade: weak but grounded
  - read: answer is generic and does not really solve note-authentication robustness
- soapmaking from animal fat on `emulator-5560`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml)
  - grade: wrong-guide grounding
  - read: answer claims the notes do not cover soapmaking and surfaces `GD-622` rabies/bite-care content
- canoe prompt on `emulator-5558`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml)
  - grade: usable
  - read: answer is broadly plausible, but source visibility was weak in the captured screen
- multi-objective separation on `emulator-5554`
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml)
  - grade: weak but grounded
  - read: answer does not cleanly keep medical, water, and shelter lanes separated

Current takeaway:

- this pack is already surfacing real Android misses across breadth categories
- the next value is repeated replay plus targeted fixes, not rebuilding the pack again

## Why This Split

The current scout read is that Android risk is no longer just a few isolated prompts. It is broader coverage mismatch:

- communications and governance are under-replayed
- water infrastructure is weaker than water-container safety
- broad construction is much better but still needs replay breadth
- multi-objective separation is under-tested
- craft/refinement still has obvious watchlist misses like soapmaking

This pack turns that read into something compact enough to replay repeatedly without falling back to random prompt picking.
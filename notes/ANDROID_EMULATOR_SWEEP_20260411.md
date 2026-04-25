# Android Emulator Sweep - 2026-04-11

## Focused Regression Update - 2026-04-12

This file started as the `2026-04-11` broad sweep note. The sections below capture the current focused regression baselines after the follow-up/chat and retrieval work on `2026-04-12`.

### Current Device Roles

- `emulator-5554`: water storage / follow-up lane
- `emulator-5556`: explicit water-distribution / system-design lane
- `emulator-5558`: construction / house lane
- `emulator-5560`: candle / tallow lane

### Current Best Follow-Up Baselines

- `emulator-5554`
  - Prompt flow: `what's the safest way to store treated water long term -> what next`
  - Best baseline artifact: [`../artifacts/bench/host_mode_detail_20260412/water_followup_5554_gpu_v14_keywordcapguard.json`](../artifacts/bench/host_mode_detail_20260412/water_followup_5554_gpu_v14_keywordcapguard.json)
  - Read: stable FIFO continuation on `GD-252`
  - Speed: `31.9 s`
- `emulator-5556`
  - Prompt flow: `how do i design a gravity-fed water distribution system -> what about storage tanks`
  - Best baseline artifact: [`../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.json`](../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.json)
  - Paired logcat: [`../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.logcat.txt`](../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.logcat.txt)
  - Read: initial turn anchors on `GD-105`; follow-up stays on `GD-105` / `GD-270`
- `emulator-5558`
  - Prompt flow: `how do i build a house -> what next`
  - Best current baseline artifact: [`../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.json`](../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.json)
  - Paired logcat: [`../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.logcat.txt`](../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.logcat.txt)
  - Read: follow-up stays on footing/foundation sequencing instead of drifting into air sealing
  - Speed: `36.4 s`
- `emulator-5560`
  - Prompt flow: `how do i make candles for light -> what about using tallow`
  - Baseline artifact: [`../artifacts/bench/host_mode_detail_20260412/candles_followup_5560_gpu_v7_stagepenaltyguard.json`](../artifacts/bench/host_mode_detail_20260412/candles_followup_5560_gpu_v7_stagepenaltyguard.json)
  - Read: clean single-guide grounding on `GD-486`
  - Speed: `7.6 s`

### Important Fixes Since The Original Sweep

- Deterministic narrow prompts are no longer the main story; the bigger gains are now in retrieval and follow-up continuity.
- Explicit water-distribution prompts were upgraded from wrong-family retrieval to the correct guide family.
- The in-detail follow-up harness now correctly recognizes thread-style completion and produces reliable JSON outputs again.
- Broad house `what next` no longer defaults to air-sealing/thermal-efficiency advice.

### Regressions Avoided / Reverted

- A focused house route-spec reduction experiment was tried and then reverted.
  - It reduced route specs for `cabin house foundation drainage`
  - It did not improve the lane enough and made the support mix worse
  - Keep using the `v10_keywordcap` house baseline as the better checkpoint
- A focused `water_distribution` route-budget trim was also tried and reverted on `2026-04-12`.
  - It targeted the `gravity-fed water distribution system -> storage tanks` lane
  - It regressed the initial turn back into `GD-444` accessible-shelter noise
  - Do not reuse that trim as-is

### Current Interpretation

- The Android app is now genuinely usable for single-turn and in-thread follow-up validation.
- The remaining major gap is speed on broad/follow-up construction queries, not gross correctness.
- Water storage and explicit water-distribution are now much more trustworthy than they were in the original sweep.
- Candle/tallow remains a good fast guardrail lane after retrieval changes.
- Breadth spotchecks from the new Android gap pack confirmed real misses outside the four main regression lanes:
  - courier authentication is still generic
  - soapmaking from animal fat is still a wrong-guide miss
  - multi-objective separation still needs better answer structure
- A reproducible snapshot bundle is being staged at [`../artifacts/snapshots/senku_android_snapshot_20260412_120727`](../artifacts/snapshots/senku_android_snapshot_20260412_120727) so these baselines are not only preserved in chat.
- The matching zip is now available at [`../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip`](../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip).
- Postsnapshot house trim checkpoint:
  - prompt logcat: [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_trimmed/house_5558_trimmed_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_trimmed/house_5558_trimmed_prompt.logcat.txt)
  - follow-up artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/house_followup_5558_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/house_followup_5558_trimmed.json)
  - read: broad house speed improved materially after trimming starter-build route chunk collection, but answer quality still needs another pass
- Matching postsnapshot guardrails:
  - `emulator-5554`: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/water_followup_5554_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/water_followup_5554_trimmed.json)
  - `emulator-5556`: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/distribution_followup_5556_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/distribution_followup_5556_trimmed.json)
  - `emulator-5560`: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/candles_followup_5560_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/candles_followup_5560_trimmed.json)
  - read: no obvious cross-lane regression from the house-only route-budget trim
- House quality/scoring checkpoint after the trim:
  - prompt logcat: [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt)
  - follow-up artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json)
  - read: broad-house follow-up is now cleaner and faster than the earlier `39.8 s` trimmed-only checkpoint
- Structural runtime note:
  - the tested emulator path is logging `fts.unavailable` / `no such module: fts5` while probing `lexical_chunks_fts`
  - this should be treated as a major mobile-runtime constraint when interpreting Android retrieval latency
- Real-device backend note:
  - the current on-device LiteRT path tries `GPU` first on ARM Android builds and falls back to `CPU` only if GPU initialization fails
  - emulator/x86 stays CPU-first by design

## Setup

- APK: `android-app/app/build/outputs/apk/debug/app-debug.apk`
- Mobile pack manifest after refresh: `96` deterministic rules
- Emulators:
  - `emulator-5554` water lane
  - `emulator-5556` medicine lane
  - `emulator-5558` construction lane
  - `emulator-5560` fire/tools lane

## Strong Results

- `can i reuse bleach bottles for water storage`
  - Result: deterministic instant answer grounded in reused-container storage guidance
  - Artifacts:
    - `artifacts/sweep_water_5554_p1.xml`
- `what do i do for a deep puncture wound`
  - Result: deterministic instant answer with correct wound-care framing
  - Artifacts:
    - `artifacts/sweep_medicine_5556_p1.xml`
- `can i store drinking water in old soda bottles`
  - Result: deterministic instant answer after matcher broadening
  - Artifacts:
    - `artifacts/variant_water_bottles_5554_r7.xml`
- `how do i sanitize old jugs for drinking water`
  - Result: deterministic instant answer
  - Artifacts:
    - `artifacts/variant_water_jugs_5556_r7.xml`
- `how do i purify water without fuel`
  - Result: deterministic instant answer using SODIS / filtration / sequencing guidance
  - Artifacts:
    - `artifacts/variant_water_nofuel_5558_r7.xml`
- `how do i start a fire in rain`
  - Result: deterministic instant answer grounded in wet-fire guidance
  - Artifacts:
    - `artifacts/sweep_fire_5560_p1.xml`
    - `artifacts/variant_fire_rain_5560_r7.xml`
- `how do i join metal without a welder`
  - Result: deterministic instant answer with good source grounding
  - Artifacts:
    - `artifacts/sweep_fire_5560_p2.xml`

## Slow But Acceptable

- `how do i build a clay oven`
  - Result: completed with local Gemma answer in about `27.4 s`
  - Read: answer shape was usable and on-topic
  - Artifacts:
    - `artifacts/sweep_construction_5558_long_p3.xml`

## Bad Grounding

- `i got a metal splinter in my hand`
  - Result: completed in about `59.4 s`, but answer degraded into generic refusal-style medical text
  - Retrieval grounding was wrong: cited `GD-657 Anti-Counterfeit Currency & Minting`
  - Artifacts:
    - `artifacts/deepcheck_metal_splinter_5556.xml`
- `how do i make candles for light`
  - Result: completed in about `44.4 s`, but answer said the notes did not contain coverage
  - Retrieval grounding was wrong: cited `GD-311 Agroforestry and Silvopasture`
  - Artifacts:
    - `artifacts/deepcheck_candles_5560.xml`

## Likely Retrieval / Routing Stalls

- `what's the safest way to store treated water long term`
  - Result: still on `Retrieving offline context...` after `90 s`
  - Artifacts:
    - `artifacts/deepcheck_water_store_5554.xml`
- `how do i build a house`
  - Result: still on `Retrieving offline context...` after `90 s`
  - Artifacts:
    - `artifacts/deepcheck_house_5558.xml`
- `how do i build a canoe`
  - Result: still on retrieval after the `40 s` sweep capture
  - Artifacts:
    - `artifacts/sweep_construction_5558_long_p2.xml`
- `how do i make muddy pond water drinkable with very little fuel`
  - Result: still on generation after the `40 s` sweep capture
  - Artifacts:
    - `artifacts/sweep_water_5554_long_p3.xml`

## Interpretation

- Deterministic Android routing is now clearly helping on narrow starter prompts.
- The next weak spot is broad route-focused retrieval on construction and long-term water storage prompts.
- The next medical/fire follow-up should target bad retrieval anchors for:
  - `metal splinter`
  - `candles for light`

## Suggested Next Fix Order

1. Add deterministic or stronger route-focused handling for `candles for light`.
2. Add deterministic or stronger route-focused handling for `metal splinter`.
3. Profile `searchRouteFocusedResults(...)` on broad construction / long-term water storage prompts.
4. Tighten broad construction routing for `house` and `canoe` in the app before further latency work.

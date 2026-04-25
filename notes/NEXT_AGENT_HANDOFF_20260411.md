# Next Agent Handoff - 2026-04-11

This note is the current handoff snapshot for the next agent picking up Senku work.

## Update - 2026-04-12

This note was refreshed on `2026-04-12` after extended Android parity work across all 4 emulators.

### What Changed Most

- A compact roadmap spine now exists for the current Android push:
  - [`ANDROID_ROADMAP_20260412.md`](./ANDROID_ROADMAP_20260412.md)
  - use this for the active fix order and validation queue, not just chat memory
- A specialized-lane retrieval patch landed for `message_auth` and `soapmaking`:
  - `message_auth` now searches the `communications` family instead of only `society`
  - `soapmaking` now uses a stronger crafts/chemistry route lane and direct topical anchor requirements
  - concrete section headings now score better for message-auth and soapmaking, while generic governance/overview rows score worse
- Exact host inference with the phone artifact is now a normal validation path:
  - Android is using the host endpoint `http://10.0.2.2:1235/v1`
  - host model id is `gemma-4-e2b-it-litert`
  - this is the desktop-hosted `.litertlm` path, not the older GGUF proxy path
- The in-detail follow-up harness in [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) now correctly recognizes the newer thread-style detail UI instead of timing out after successful follow-ups.
- Explicit water-distribution routing was materially fixed:
  - first-turn prompts like `how do i design a gravity-fed water distribution system` now classify as route-focused water-storage/system queries
  - the answer anchor no longer falls back to generic `GD-252` storage rows on that lane
- House follow-ups are meaningfully better:
  - generic `what next` no longer jumps straight into air-sealing/thermal-efficiency advice
  - current best baseline stays on the footing/foundation sequence
- One experiment was explicitly rejected and reverted:
  - shrinking the house route-spec set for focused follow-ups made the lane slower/noisier
  - do not reuse that idea without new evidence
- Another experiment was explicitly rejected and reverted after the snapshot:
  - trimming explicit `water_distribution` route chunk/guide budgets looked promising for speed
  - live `5556` validation regressed back into `GD-444` accessibility junk on the initial turn
  - the change was reverted immediately; keep that lane broad until a safer narrowing signal exists
- The live Android assets now include the refreshed full-pack metadata pass:
  - source pack: [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh)
  - refresh script: [`../scripts/refresh_mobile_pack_metadata.py`](../scripts/refresh_mobile_pack_metadata.py)
  - this is the safe full-pack refresh, not the tiny `1344`-chunk experimental export
- Early results from the live metadata refresh are promising but incomplete:
  - courier-authentication now reaches a route-focused lane instead of failing for lack of metadata, but the answer is still too generic
  - soapmaking moved into a much more plausible craft/chemistry retrieval family, but still needs better anchor/support selection
- Broad house route-budget trimming now has a validated win:
  - trimming starter-build route chunk collection reduced broad-house route search from about `47.4 s` to `15.6 s`
  - the same change brought the broad initial house answer from about `67.6 s` total down to about `31.1 s`
  - broad house follow-up `what next` is now at `39.8 s` on the trimmed build
- A follow-up broad-house quality pass also landed:
  - generic-house thermal-efficiency sections are now penalized more aggressively in metadata scoring
  - this did not beat the fastest trimmed-only initial timing, but it improved generic-house support ordering and follow-up quality
  - current better broad-house follow-up checkpoint is [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json) at `29.2 s`
- A more important structural issue emerged during live validation:
  - Android is logging `fts.unavailable` and `no such module: fts5`
  - this means the runtime is not actually using the shipped `lexical_chunks_fts` table on the tested emulator path
  - treat this as a major mobile retrieval constraint and not just a debug warning
- A scout pass now makes the FTS root cause much clearer:
  - the app is currently opening the pack through framework SQLite, not a bundled SQLite runtime with known FTS5 support
  - the pack ships the FTS table, but runtime probing still fails immediately with `no such module: fts5`
  - treat current Android lexical timings as fallback-path timings until the SQLite runtime path is fixed
  - concrete evidence points to framework SQLite mismatch, not bad pack export:
    - runtime DB open path is `android.database.sqlite.SQLiteDatabase` in [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
    - the app build does not currently bundle an alternate SQLite runtime in [`../android-app/app/build.gradle`](../android-app/app/build.gradle)
    - the pack manifest still lists `lexical_chunks_fts` in [`../android-app/app/src/main/assets/mobile_pack/senku_manifest.json`](../android-app/app/src/main/assets/mobile_pack/senku_manifest.json)
    - the direct live artifact is [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt)
  - smallest safe next move is to capture SQLite version / compile-option diagnostics before retuning lexical search as though FTS5 were active
- A snapshot bundle is being maintained for this state:
  - snapshot root: [`../artifacts/snapshots/senku_android_snapshot_20260412_120727`](../artifacts/snapshots/senku_android_snapshot_20260412_120727)
  - intended use: preserve the current debug APK, exact mobile pack assets, exact `E2B` `.litertlm`, host server scripts, and install notes as a reproducible checkpoint

### Current Best Validated Android Baselines

- Courier authentication after the specialized-lane patch:
  - artifacts:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.xml`](../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.xml)
    - [`../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.logcat.txt`](../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.logcat.txt)
  - read:
    - live logcat now shows `anchorGuide="GD-389"`
    - support candidates moved into the message-auth family (`GD-436`)
    - answer is still somewhat generic, but retrieval-family grounding is now materially better
- Soapmaking after the specialized-lane patch:
  - artifact:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5556_v1.xml`](../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5556_v1.xml)
  - read:
    - answer now stays on a safe lye/fat/cure path instead of the older wrong-family miss
    - `5560` remained a dirty harness/runtime lane on this pass, so `5556` is the reliable soap checkpoint for now
- Water long-term storage follow-up:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412/water_followup_5554_gpu_v14_keywordcapguard.json`](../artifacts/bench/host_mode_detail_20260412/water_followup_5554_gpu_v14_keywordcapguard.json)
  - result: `what's the safest way to store treated water long term -> what next`
  - quality: stays on `GD-252`
  - speed: `31.9 s`
- Explicit water-distribution follow-up:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.json`](../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.json)
  - paired logcat: [`../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.logcat.txt`](../artifacts/bench/host_mode_detail_20260412/distribution_followup_5556_gpu_v12_harnessfix.logcat.txt)
  - result: `how do i design a gravity-fed water distribution system -> what about storage tanks`
  - quality: initial turn anchors on `GD-105`; follow-up answer stays on `GD-105` / `GD-270`
- Broad house follow-up:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.json`](../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.json)
  - paired logcat: [`../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.logcat.txt`](../artifacts/bench/host_mode_detail_20260412/house_followup_5558_gpu_v10_keywordcap.logcat.txt)
  - result: `how do i build a house -> what next`
  - quality: follow-up stays on footing/foundation sequencing
  - speed: `36.4 s`
- Current postsnapshot trimmed-build house timing checkpoint:
  - prompt logcat: [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_trimmed/house_5558_trimmed_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_trimmed/house_5558_trimmed_prompt.logcat.txt)
  - follow-up artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/house_followup_5558_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/house_followup_5558_trimmed.json)
  - read: route search improved dramatically on the initial house turn, but answer shaping is still weaker than desktop and still leans too much on `GD-106`
  - speed: initial house `31.1 s`; follow-up `39.8 s`
- Current broad-house quality checkpoint after the scoring penalty pass:
  - prompt logcat: [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt)
  - follow-up artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json)
  - read: slower than the fastest trimmed-only initial checkpoint, but follow-up quality and source ordering are better
  - speed: initial house `39.2 s`; follow-up `29.2 s`
- Current postsnapshot trimmed-build guardrails:
  - water follow-up: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/water_followup_5554_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/water_followup_5554_trimmed.json)
  - distribution follow-up: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/distribution_followup_5556_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/distribution_followup_5556_trimmed.json)
  - candle follow-up: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/candles_followup_5560_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/candles_followup_5560_trimmed.json)
  - read: the house-only trim did not obviously break the other 3 emulator lanes, but water/distribution are still materially slower than candle
- New specialized-lane patch guardrails:
  - water ask-offline:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/water_5554_v15.xml`](../artifacts/bench/metadata_refresh_20260412_patch/water_5554_v15.xml)
    - read: still grounded on `GD-252`, subtitle `37.0 s`
  - house ask-offline:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/house_5558_v11.xml`](../artifacts/bench/metadata_refresh_20260412_patch/house_5558_v11.xml)
    - read: no catastrophic retrieval failure, but answer quality softened and drifted back toward a shed/frame path
- Candle/tallow follow-up:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412/candles_followup_5560_gpu_v7_stagepenaltyguard.json`](../artifacts/bench/host_mode_detail_20260412/candles_followup_5560_gpu_v7_stagepenaltyguard.json)
  - result: `how do i make candles for light -> what about using tallow`
  - quality: clean single-guide grounding on `GD-486`
  - speed: `7.6 s`

### Files Changed Most Recently

- Retrieval/routing:
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`](../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java)
- Follow-up/UI path:
  - [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`](../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java)
  - [`../android-app/app/src/main/res/layout/activity_detail.xml`](../android-app/app/src/main/res/layout/activity_detail.xml)
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - [`../scripts/stop_android_harness_orphans.ps1`](../scripts/stop_android_harness_orphans.ps1)
- Regression coverage:
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`](../android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java)

### Current Recommended Next Steps

1. Keep house speed work focused on retrieval cost, not more answer-shaping tweaks.
   - Current log evidence still shows `routeSearch` and `keywordMs` dominating broad/follow-up house latency even after the latest wins.
   - But after the newest pass, `GD-383` is finally outranking `GD-106`, so generic-house scoring is moving in the right direction.
2. Preserve the specialized-lane gains while reducing residual answer softness.
   - Courier retrieval is now on the right guide family, but the answer still needs a stronger concrete authentication workflow.
   - Soapmaking is much better, but source visibility and stable craft anchoring still need a cleaner checkpoint.
3. Preserve the explicit water-distribution gains while reducing follow-up cost there.
   - The lane is now correct, but it is still slower than candle/water FIFO.
4. Continue turning the detail screen into a true chat surface.
   - The harness now supports the new thread-style UI, so UI work is easier to validate repeatedly.
5. Add lightweight backend observability before real-device inference work expands.
   - The current app only surfaces the final local backend winner and not the attempted path or fallback reason.
   - A small `BackendDiagnostics` layer in `LiteRtModelRunner` plus UI/log surfacing would make A35 testing much more trustworthy.
6. Keep records current in this handoff and the emulator sweep note whenever baselines materially move.
7. Clean Android harness orphans before trusting reruns after interrupted sessions.
   - Use [`../scripts/stop_android_harness_orphans.ps1`](../scripts/stop_android_harness_orphans.ps1) instead of hand-killing PowerShell trees.
8. Investigate the Android FTS5 runtime mismatch before assuming current lexical timings are representative.
   - The pack contains `lexical_chunks_fts`, but the tested Android runtime is logging `no such module: fts5`.
9. Keep replaying the Android gap pack instead of relying only on the four main regression lanes.
   - Real breadth misses are already confirmed for courier-authentication, soapmaking, and multi-objective separation.

### New Harness / Breadth Findings

- [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1) was fixed for more trustworthy ask-only replays.
  - it now creates the local dump directory before `adb pull`
  - when `-Ask` is used without `-ExpectedDetailTitle`, it now defaults the expected detail title to the query text
  - this avoids stale-detail captures and missing dump-path failures during breadth replay
- [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) was hardened for long answers.
  - It now scrolls to reveal the in-detail follow-up composer and uses stronger fallback capture for final JSON fields.
  - This fixed false follow-up timeouts on the newer thread-style detail UI.
- Gap-pack spotchecks confirmed real breadth issues beyond the core lane set:
  - courier authentication:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml)
    - generic/shallow answer, not a robust authentication workflow
  - soapmaking from animal fat:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml)
    - wrong-guide miss into `GD-622` rabies/bite-care content
  - multi-objective separation:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml)
    - answer does not separate water/medical/shelter priorities cleanly
  - canoe:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml)
    - looks usable, but still needs more replay depth
- Two `water_distribution` latency experiments were tried and rejected after live validation:
  - lowering the guide-search threshold was faster but regressed the anchor back toward generic `GD-252`
  - changing the guide candidate budget preserved `GD-105` but made latency even worse
  - both experiments were reverted immediately

### Structural Scout Findings Worth Acting On Next

- Deterministic routing still has no true shared source of truth across desktop, pack export, and Android runtime.
  - Treat this as a high-priority parity project once the current retrieval pass settles.
- Manifest/runtime drift is still real.
  - The app displays pack defaults, but multiple runtime knobs still live as Android constants instead of pack-driven config.
- Desktop and Android session models still diverge.
  - Desktop remains the quality reference overall, but deterministic turns on desktop still need explicit session-state coverage before calling that path fully comparable.
- Metadata is helping, but coverage is still partial.
  - Do not over-trust auto-tagging without export-time audits and spot checks on weak prompt families.
- There are still too many parallel rule systems.
  - The likely medium-term fix is test-first shared contracts generated from desktop and consumed by Android validation before runtime migration.
- Coverage mismatch is now a bigger Android risk than a handful of isolated prompt misses.
  - See [`ANDROID_SCOUT_FINDINGS_20260412.md`](./ANDROID_SCOUT_FINDINGS_20260412.md) for the compact gap-analysis plan and the recommended 20-prompt + 8-follow-up Android gap pack.
- The next UI slice has a clear low-risk direction now.
  - A scout pass on the detail screen recommends a transcript-first chat surface that keeps the existing harness-facing view IDs stable instead of attempting a risky full rewrite.
  - Keep `detail_title`, `detail_subtitle`, `detail_body`, `detail_sources_container`, `detail_followup_input`, `detail_followup_send`, and `detail_scroll` stable while promoting `detail_thread_container` into the real conversation surface.
- Real-device backend observability is still too thin.
  - Today the app can attempt local LiteRT `GPU` first on ARM devices and silently fall back to `CPU`, but the user mostly sees only the winner.
  - The recommended next slice is a tiny backend-diagnostics object in `LiteRtModelRunner`, surfaced in `MainActivity`, `OfflineAnswerEngine`, and the harness artifacts so phones like the A35 can prove what happened.

### Latest Follow-Up And Breadth Testing Notes

- The in-detail follow-up harness was hardened after long-answer failures.
  - file: [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - current behavior:
    - scrolls long answers to reveal the follow-up composer instead of assuming it is missing
    - recovers more stable final title/body metadata after follow-up completion
    - produces more usable JSON artifacts even when the header capture is imperfect
- Sequential host-mode runs remain the trustworthy path.
  - parallel host-mode runs can contaminate timings because the current desktop host serializes requests
- Gap-pack breadth spotchecks are now producing real misses worth prioritizing:
  - courier-authentication answer is too generic:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml)
  - soapmaking from animal fat is a clear wrong-guide miss:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml)
  - multi-objective triage/separation is still too blended:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml)
  - canoe prompt looked broadly usable:
    - [`../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml)
- Rejected experiments to remember:
  - explicit `water_distribution` route-guide threshold trim improved speed but collapsed quality back to `GD-252`
  - explicit `water_distribution` guide-budget change preserved the anchor but made latency much worse
  - both were reverted; do not reuse those edits as-is

### Snapshot Notes

- The current snapshot bundle should include:
  - debug APK
  - `senku_manifest.json`
  - `senku_mobile.sqlite3`
  - `senku_vectors.f16`
  - exact `gemma-4-E2B-it.litertlm`
  - host-mode helper scripts
  - install instructions and checksum manifest
- Snapshot bundle is now staged and zipped:
  - folder: [`../artifacts/snapshots/senku_android_snapshot_20260412_120727`](../artifacts/snapshots/senku_android_snapshot_20260412_120727)
  - repo-local zip: [`../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip`](../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip)
  - current user-kept zip copy: [`C:/Users/tateb/Desktop/senku_android_snapshot_20260412_120727.zip`](C:/Users/tateb/Desktop/senku_android_snapshot_20260412_120727.zip)
- Active work-floor record for the current continuation window lives in [`ACTIVE_WORK_LOG_20260412.md`](./ACTIVE_WORK_LOG_20260412.md).
- On real Android hardware, the current build tries LiteRT `GPU` first and falls back to `CPU` only if GPU init fails.
  - Emulator/x86 remains CPU-first by design.

### New Late-Stage Notes

- Manifest/runtime drift is still affecting tuning decisions.
  - The pack manifest still advertises `mobile_top_k = 10`, but Android answer generation still uses hardcoded `ANSWER_CANDIDATE_LIMIT = 16` and `ANSWER_CONTEXT_LIMIT = 4` in [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java).
  - Practical meaning: changing manifest `mobile_top_k` alone does not currently change live answer behavior.
- The soapmaking lane improved in latency but is still not fixed at the guide-family level.
  - latest reliable artifact:
    - [`../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.xml`](../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.xml)
    - [`../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.logcat.txt`](../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.logcat.txt)
  - current read:
    - answer body is broadly usable and faster than the earlier `62 s` lane
    - route-guide breadth dropped from `40` to `28`
    - anchor is still `GD-571`, so the main problem is still anchor-family selection rather than pure latency
- Current best diagnosis for soapmaking:
  - the wrong-family guide still seems to be entering through the broader ranked-result/anchor pool, not just the route sweep
  - metadata verification is now the highest-ROI parallel lane:
    - verify structure-type and topic-tag coverage on the real soapmaking/craft guides
    - then tighten anchor-pool admission using verified metadata instead of more ad hoc keyword pressure

## What Exists Now

Desktop:
- `query.py` is still the quality reference
- desktop has the richer retrieval stack: deterministic pre-RAG routing, supplemental retrieval lanes, metadata-aware reranking, and structured session state
- if mobile looks weak, compare the same prompt on desktop before assuming the model is the main problem

Mobile:
- the Android debug app exists under [`android-app`](../android-app)
- the app installs the exported mobile pack, searches locally, imports a local LiteRT-LM model, and runs offline Gemma inference
- the active APK output path is `android-app/app/build/outputs/apk/debug/app-debug.apk`
- emulator inference currently works on the CPU path with Gemma 4 `E2B`

Pack/export:
- mobile pack export is built around [`mobile_pack.py`](../mobile_pack.py) and [`scripts/export_mobile_pack.py`](../scripts/export_mobile_pack.py)
- current pack metadata reports `96` deterministic rules

## Most Relevant Docs

- [`../AGENTS.md`](../AGENTS.md): fast repo landing page
- [`../TESTING_METHODOLOGY.md`](../TESTING_METHODOLOGY.md): living validation workflow
- [`../android-app/README.md`](../android-app/README.md): Android build/install notes
- [`ANDROID_EMULATOR_SWEEP_20260411.md`](./ANDROID_EMULATOR_SWEEP_20260411.md): latest broad emulator sweep
- [`ANDROID_SCOUT_FINDINGS_20260412.md`](./ANDROID_SCOUT_FINDINGS_20260412.md): structural risks and next validation pack
- [`ACTIVE_WORK_LOG_20260412.md`](./ACTIVE_WORK_LOG_20260412.md): current continuation floor and snapshot record

## Current Workstreams

1. Android parity with desktop retrieval quality
2. Android chat/session continuity so follow-ups stop feeling stateless
3. Continued `E2B` floor testing plus `E4B` quality-tier evaluation
4. Broader mobile prompt-family coverage instead of narrow one-off fixes

## Current Mobile State

Strengths:
- deterministic narrow prompts are much better than they were
- exact `E2B` host inference now works with the `.litertlm` artifact on the desktop-hosted GPU path
- browse/search/detail flows exist and are testable
- in-thread follow-ups are now a normal validated path, not an experimental side lane

Weaknesses:
- broad route-focused prompts are still weaker/slower than desktop
- house retrieval still spends too much time in route search and keyword retrieval
- some house support lists still carry lower-value subsystem noise even when the final answer is better
- mobile chat/session behavior is much better than before, but the UI still reads like a strong detail screen rather than a finished chat app

Known weak prompt families from recent emulator testing:
- `how do i build a house`
- broader house continuations like `what next`
- explicit water-distribution/system-design prompts are now much better, but still slower than they should be
- long-term water storage is now usable, but still worth watching for latency/regression

## Files To Look At First

Desktop retrieval/routing:
- [`../query.py`](../query.py)
- [`../deterministic_special_case_registry.py`](../deterministic_special_case_registry.py)
- [`../special_case_builders.py`](../special_case_builders.py)

Android retrieval/prompting:
- [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
- [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
- [`../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`](../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java)
- [`../android-app/app/src/main/java/com/senku/mobile/MainActivity.java`](../android-app/app/src/main/java/com/senku/mobile/MainActivity.java)
- [`../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`](../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java)

Android validation harness:
- [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1)

## Recommended Next Sequence

1. Verify the current Android build on all 4 emulators after each substantial retrieval/UI change.
2. Use the in-detail follow-up harness as a primary regression lane:
   - water FIFO continuation
   - house `what next`
   - explicit water-distribution `storage tanks`
   - candle `tallow`
3. Re-run broad construction and long-term water-storage prompts after retrieval changes.
4. Compare remaining weak mobile prompts against desktop handling in `query.py`.
5. Prefer retrieval/metadata/session fixes over prompt-only shaping.

## Validation Commands

Desktop:
```bash
python3 -m unittest discover -s tests -v
python3 scripts/validate_special_cases.py
```

Android build:
```powershell
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

Android prompt automation:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_prompt.ps1 `
  -Emulator emulator-5554 `
  -Query "how do i build a canoe" `
  -Ask `
  -WaitForCompletion `
  -MaxWaitSeconds 120 `
  -PollSeconds 10 `
  -DumpPath artifacts\example.xml
```

## Model Guidance

- `E2B` is the practical baseline/default right now
- `E4B` is worth targeting as the quality tier, especially for future hardware
- if both models fail on the same prompt family, fix retrieval/routing first

## Notes For The Next Agent

- Keep `AGENTS.md` light; put evolving process in focused docs like the testing methodology and handoff notes
- Update [`../TESTING_METHODOLOGY.md`](../TESTING_METHODOLOGY.md) when the validation loop changes
- If you materially change current status or next steps, refresh this handoff note instead of letting it drift
- If you run a speed experiment that hurts quality or latency, note that outcome here instead of silently overwriting the baseline
- Prefer metadata/category/alias signals over hardcoded guide IDs in new runtime retrieval work.
  - Keep guide IDs in notes, artifacts, and regression assertions, but avoid making runtime routing depend on a specific `GD-xxx` unless the path is explicitly deterministic and narrow by design.

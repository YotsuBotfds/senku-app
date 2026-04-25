# Active Work Log - 2026-04-12

This note records the active continuation floor for the current Android iteration session.

## Time Snapshot

- Snapshot verified at `2026-04-12T12:11:42.5584523-05:00`
- Continue active iteration/testing until at least `2026-04-12T16:11:42.5584523-05:00`
- Refreshed continuation floor at `2026-04-12T14:20:15.2843349-05:00`
- Continue active iteration/testing until at least `2026-04-12T18:20:15.2843349-05:00`
- Refreshed continuation floor at `2026-04-12T16:36:45.8552703-05:00`
- Continue active iteration/testing until at least `2026-04-12T20:36:45.8552703-05:00`
- Refreshed continuation floor at `2026-04-12T20:29:24.9361348-05:00`
- Continue active iteration/testing until at least `2026-04-13T00:29:24.9361348-05:00`
- Refreshed continuation floor at `2026-04-12T21:00:53.1715240-05:00`
- Continue active iteration/testing until at least `2026-04-13T01:00:53.1715240-05:00`
- Time check after house/distribution anchor patch validation: `2026-04-12T22:28:38.2523220-05:00`
  - still actively within the required continuation window
- Time check after snapshot/doc pass: `2026-04-12T13:11:11.1466818-05:00`
  - still actively within the required continuation window
- Time check after headless parity + harness cleanup pass: `2026-04-12T20:46:20.2657070-05:00`
  - still actively within the required continuation window
- Refreshed continuation floor at `2026-04-12T23:07:23.8735887-05:00`
- Continue active iteration/testing until at least `2026-04-13T03:07:23.8735887-05:00`
- Time check after site-selection + route-FTS4 probe pass: `2026-04-12T23:38:54.4692543-05:00`
  - still actively within the required continuation window
- Time check after compact house-site route-lane validation: `2026-04-12T23:47:51.4239956-05:00`
  - still actively within the required continuation window
- Time check after refreshed snapshot creation: `2026-04-12T23:52:42.9130413-05:00`
  - still actively within the required continuation window
- Time check after exact-artifact compact-lane validation: `2026-04-12T23:49:19.9015480-05:00`
  - still actively within the required continuation window
- Refreshed continuation floor at `2026-04-12T23:45:47.5403355-05:00`
- Continue active iteration/testing until at least `2026-04-13T03:45:47.5403355-05:00`
- Time check after runtime-threshold validation: `2026-04-13T00:05:16.0752424-05:00`
  - still actively within the required continuation window
- Refreshed continuation floor at `2026-04-13T02:16:14.7535680-05:00`
- Continue active iteration/testing until at least `2026-04-13T06:16:14.7535680-05:00`
- Time check after desktop support hardening validation: `2026-04-13T02:19:29.1230083-05:00`
  - still actively within the required continuation window
- Refreshed continuation floor at `2026-04-13T02:16:14.7535680-05:00`
- Continue active iteration/testing until at least `2026-04-13T06:16:14.7535680-05:00`
- Time check after desktop hardening validation: `2026-04-13T02:18:21.3834390-05:00`
  - still actively within the required continuation window

## Verified Snapshot Paths

- Current snapshot folder: [`../artifacts/snapshots/senku_android_snapshot_20260412_234900`](../artifacts/snapshots/senku_android_snapshot_20260412_234900)
- Current snapshot zip: [`../artifacts/snapshots/senku_android_snapshot_20260412_234900.zip`](../artifacts/snapshots/senku_android_snapshot_20260412_234900.zip)
- Desktop copy: [`C:/Users/tateb/Desktop/senku_android_snapshot_20260412_234900.zip`](C:/Users/tateb/Desktop/senku_android_snapshot_20260412_234900.zip)
- Snapshot folder: [`../artifacts/snapshots/senku_android_snapshot_20260412_120727`](../artifacts/snapshots/senku_android_snapshot_20260412_120727)
- Snapshot zip: [`../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip`](../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip)

## Roadmap Spine

- Active roadmap note: [`ANDROID_ROADMAP_20260412.md`](./ANDROID_ROADMAP_20260412.md)
- Detailed handoff/state note: [`NEXT_AGENT_HANDOFF_20260411.md`](./NEXT_AGENT_HANDOFF_20260411.md)
- Breadth replay pack: [`ANDROID_GAP_PACK_20260412.md`](./ANDROID_GAP_PACK_20260412.md)
- Structural scout note: [`ANDROID_SCOUT_FINDINGS_20260412.md`](./ANDROID_SCOUT_FINDINGS_20260412.md)
- Metadata audit note: [`METADATA_AUDIT_20260412.md`](./METADATA_AUDIT_20260412.md)
- Future update architecture note: [`MOBILE_UPDATE_ARCHITECTURE_20260412.md`](./MOBILE_UPDATE_ARCHITECTURE_20260412.md)

## Snapshot Verification

- Fresh compact-lane snapshot created and zip-verified at `2026-04-12T23:52:13-05:00`.
- `docs/INSTALLATION_INSTRUCTIONS.md` inside the new snapshot now includes the bundled `push_mobile_pack_to_android.ps1` workflow.
- ZIP listing was verified after creation.
- `docs/checksums.txt` exists inside the snapshot and covers the staged files.
- The snapshot includes:
  - debug APK
  - pack DB/vector/manifest
  - exact `gemma-4-E2B-it.litertlm`
  - host server scripts
  - install instructions
  - current handoff/sweep/testing/scout notes

## Current Immediate Priorities

1. Resume Android retrieval/session iteration after snapshot completion.
2. Validate the latest house route-budget change on-device.
3. Keep water/distribution/candle guardrails clean while broadening validation coverage.
4. Turn scout findings into a compact Android gap pack and replay lane.
5. Treat metadata verification and pack refresh as a first-class workstream before more one-off Android prompt tuning.

## Latest Checkpoint

- New runtime-threshold checkpoint is now recorded:
  - water distribution on `5556`:
    - [`../artifacts/bench/live_refresh_v6_20260412/distribution_5556_v9_runtimeguidecap.logcat.txt`](../artifacts/bench/live_refresh_v6_20260412/distribution_5556_v9_runtimeguidecap.logcat.txt)
    - read:
      - `routeGuideSearch.pre` now lands with `sections=5 threshold=5`
      - the runtime threshold cut causes `routeGuideSearch.skip`
      - `routeSearch` completes in about `68.1 s`
      - whole search completes in about `81.1 s`
      - `search.topResults` stays in-family on `GD-553` and `GD-270`
  - broad site-selection on `5554`:
    - kept runtime FTS4 progress:
      - [`../artifacts/bench/live_refresh_v6_20260412/site_5554_v15_120s.logcat.txt`](../artifacts/bench/live_refresh_v6_20260412/site_5554_v15_120s.logcat.txt)
      - read: compact route chunk and guide FTS both execute, but guide FTS still adds zero sections on the current broad-site lane
    - reverted shortcut:
      - [`../artifacts/bench/live_refresh_v6_20260412/site_5554_v16_runtimeguidecap.logcat.txt`](../artifacts/bench/live_refresh_v6_20260412/site_5554_v16_runtimeguidecap.logcat.txt)
      - read: a temporary broad-site runtime threshold shortcut made search finish in about `58.8 s`, but top results collapsed into junk (`GD-292`, `GD-110`, `GD-444`), so that shortcut was reverted in the repo
  - practical conclusion:
    - the no-`bm25` runtime threshold is a real keeper for explicit `water_distribution`
    - the same idea is not safe for broad `site_selection` without better route quality first
    - the broad-site bottleneck has narrowed to guide-stage usefulness, not the old route-topic explosion

- New exact-artifact compact house-site checkpoint is now recorded:
  - broad building-site prompt on `5554`:
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.logcat.txt`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.logcat.txt)
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.xml`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.xml)
    - read:
      - fresh clear-logcat run now shows `routeSpecs=3`
      - explicit topics stay narrowed to `site_selection, drainage`
      - compact route FTS is live on-device via `choose* OR building* OR site* OR drainage*`
  - small-cabin site/foundation prompt on `5560`:
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/cabin_site_5560_v1_short.logcat.txt`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/cabin_site_5560_v1_short.logcat.txt)
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/cabin_site_5560_v1_short.xml`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/cabin_site_5560_v1_short.xml)
    - read:
      - fresh clear-logcat run now shows `routeSpecs=4`
      - explicit topics stay narrowed to `site_selection, foundation, drainage`
      - compact route FTS is live on-device via `choose* OR safe* OR site* OR foundation*`
  - water-storage guardrail on `5556`:
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/water_5556_v9.logcat.txt`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/water_5556_v9.logcat.txt)
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/water_5556_v9.xml`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/water_5556_v9.xml)
    - read:
      - water-storage still starts on `water_storage, container_sanitation, water_rotation`
      - the house-site compact-lane patch did not disturb the water guardrail
  - practical conclusion:
    - the compact site-selection route-profile fix is confirmed on-device
    - the remaining bottleneck is still route-search latency after the new compact FTS4 start, not the old broad house starter-build bundle
    - `5558` remains the flaky emulator lane, so clean retrieval validation is currently leaning on `5554/5556/5560`

- New compact house-site checkpoint is now recorded:
  - broad building-site prompt on `5554`:
    - [`../artifacts/bench/quick_startlogs_compactlane_20260412/site_5554.logcat.txt`](../artifacts/bench/quick_startlogs_compactlane_20260412/site_5554.logcat.txt)
    - [`../artifacts/bench/compact_budget_runtime_20260412/site_5554.logcat.txt`](../artifacts/bench/compact_budget_runtime_20260412/site_5554.logcat.txt)
    - read:
      - broad site-selection now stays on `routeSpecs=3`
      - explicit topics stay narrowed to `site_selection, drainage`
      - runtime `routeChunkFts` is now using `candidateLimit=192` instead of the older starter-build `600` budget
  - small-cabin site/foundation prompt on `5560` after clean reinstall:
    - [`../artifacts/bench/quick_startlogs_compactlane_20260412/cabin_site_5560_reinstall.logcat.txt`](../artifacts/bench/quick_startlogs_compactlane_20260412/cabin_site_5560_reinstall.logcat.txt)
    - [`../artifacts/bench/compact_budget_runtime_20260412/cabin_site_5560_reinstall.logcat.txt`](../artifacts/bench/compact_budget_runtime_20260412/cabin_site_5560_reinstall.logcat.txt)
    - read:
      - the lane now starts at `routeSpecs=4`
      - explicit topics stay narrowed to `site_selection, foundation, drainage`
      - runtime `routeChunkFts` is also down to `candidateLimit=192`
  - water-storage guardrail on `5556`:
    - [`../artifacts/bench/quick_startlogs_compactlane_20260412/water_5556.logcat.txt`](../artifacts/bench/quick_startlogs_compactlane_20260412/water_5556.logcat.txt)
    - read:
      - water-storage stayed on `water_storage, container_sanitation, water_rotation`
      - the house-site compact-lane patch did not disturb the water guardrail
  - practical conclusion:
    - the route-profile fix is live on Android, not just in tests
    - the remaining bottleneck is now per-query FTS4 route latency and later guide sweep, not the old broad house starter-build budget
    - `5558` is currently the flaky install lane, so clean validation is temporarily leaning on `5554/5556/5560`

- New site-selection / route-FTS4 checkpoint is now recorded:
  - broad site-selection probe on `5554`:
    - [`../artifacts/bench/live_refresh_v6_20260412/site_5554_v10_fts4simpler.logcat.txt`](../artifacts/bench/live_refresh_v6_20260412/site_5554_v10_fts4simpler.logcat.txt)
    - read:
      - explicit topics are now correctly narrowed to `site_selection, drainage` on fresh clear-logcat runs
      - route-focused FTS4 is now executing instead of immediately logging `unsupported_ranking`
      - the current compact chunk queries are:
        - `choose* OR building* OR site* OR drainage*`
        - `shelter* OR site* OR selection* OR hazard*`
        - `shelter* OR site* OR selection* OR terrain*`
      - the run now reaches `routeGuideSearch.pre` with `sections=2 threshold=12 compact=true`
      - broad site-selection is still too slow to finish inside the current short log-only budget, so guide-sweep completion is the next bottleneck rather than chunk-route seeding
  - current implementation notes behind that checkpoint:
    - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
      - added stronger site-selection markers like `safe site`, `choose a safe site`, and `site and foundation`
      - site-breadth intent now honors the siting-query signal instead of requiring pre-existing `site_selection` topic detection
    - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
      - route-focused chunk/guide search no longer skips FTS when the Android runtime only exposes `lexical_chunks_fts4`
      - unranked FTS4 route queries now use a smaller primary-token budget, and hyphenated terms are split into separate prefixes for safer FTS parsing
    - regression coverage expanded in:
      - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
      - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)

- Fresh Android anchor-validation checkpoint is now recorded:
  - broad house on `5558`:
    - [`../artifacts/bench/live_prompt_validation_20260412/house_5558_v5_houseanchor_rerun.logcat.txt`](../artifacts/bench/live_prompt_validation_20260412/house_5558_v5_houseanchor_rerun.logcat.txt)
    - read: `search.topResults` is now fully `GD-094`, `context.selected` anchors on `GD-094`, and the answer body stays on site/foundation/walls/roof sequencing instead of the older `GD-618` or `GD-383` anchor path
  - direct gravity-fed distribution on `5556`:
    - [`../artifacts/bench/live_prompt_validation_20260412/distribution_5556_v3_prompt.logcat.txt`](../artifacts/bench/live_prompt_validation_20260412/distribution_5556_v3_prompt.logcat.txt)
    - read: `context.selected` is now `GD-270` design/tower/layout/community-water sections instead of the older `GD-648` lifecycle/checklist path
  - direct `storage tanks water distribution` on `5560`:
    - [`../artifacts/bench/live_prompt_validation_20260412/storage_tanks_5560_v2_clean.logcat.txt`](../artifacts/bench/live_prompt_validation_20260412/storage_tanks_5560_v2_clean.logcat.txt)
    - read: `context.selected` also anchors on `GD-270` and the answer body stays on tower/layout/pressure logic rather than the earlier abrasives/junk lane
  - water-storage one-turn guard on `5554`:
    - [`../artifacts/bench/live_prompt_validation_20260412/water_5554_v3_guard.logcat.txt`](../artifacts/bench/live_prompt_validation_20260412/water_5554_v3_guard.logcat.txt)
    - read: the answer path still anchors on `GD-252`, so the new house/distribution fixes did not regress the core storage lane
- New implementation notes behind that checkpoint:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
    - added stronger section penalties for shared-shelter/camp-evolution house drift and for lifecycle/checklist/meta water-distribution drift
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
    - added water-distribution lifecycle penalties in anchor focus and a new broad-house anchor chooser so broad house prompts prefer a `cabin_house` starter guide over narrow general foundation references
  - regression coverage expanded in:
    - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
    - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
- New efficiency note:
  - targeted headless no-FTS batch now agrees with Android on broad house and direct distribution:
    - [`../artifacts/bench/headless_targeted_house_distribution_20260412_afterpatch_nofTs/mobile_headless_answers_headless_targeted_house_distribution_20260412_20260412_213047_060985.json`](../artifacts/bench/headless_targeted_house_distribution_20260412_afterpatch_nofTs/mobile_headless_answers_headless_targeted_house_distribution_20260412_20260412_213047_060985.json)
  - remaining harness bug:
    - the headless FTS lane can still choke on `gravity-fed` queries, so direct water-distribution batch runs currently need `--force-no-fts`

- Fresh harness fix is now live:
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) now freezes the first valid second-turn answer dump immediately instead of letting later detail-screen transitions overwrite `*_followup_answer.xml`
  - `Reveal-AnswerHeader` now prefers the frozen local artifact and uses a separate temporary refresh dump when it needs live re-checks
  - invalid late `Guide note` / non-answer captures now fail the run instead of being serialized as a successful follow-up answer
- Fresh Windows shell fix is also live:
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) and [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1) now disable `PSNativeCommandUseErrorActionPreference` in the child scripts themselves so `adb pull` progress lines do not abort runs
- Live replay launched on `5560` after that patch:
  - run label: `water_followup_5560_v18_freeze`
  - target: `what is the safest way to store treated water long term -> what about rotation`
  - output dir: [`../artifacts/bench/android_packrefresh_v7_20260412`](../artifacts/bench/android_packrefresh_v7_20260412)
- Current replay label after the shell-level retry:
  - `water_followup_5560_v18_freezee`
- Cheap headless parity guard now works more cleanly too:
  - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py) now tolerates UTF-8 BOM input and reconfigures stdout to UTF-8 on Windows
  - fresh batch artifact:
    - [`../artifacts/bench/headless_live_batch_20260412/mobile_headless_answers_headless_live_batch_20260412_20260412_210442_235161.json`](../artifacts/bench/headless_live_batch_20260412/mobile_headless_answers_headless_live_batch_20260412_20260412_210442_235161.json)
  - current top-context read:
    - `safe site and foundation for a small cabin` still overweights `GD-094`
    - `waterproof a roof with no tar or shingles` still overweights `GD-094`
    - `build a clay oven` is clean on `GD-505`
    - `merge with another group if we do not trust them yet` stays on `GD-626`
    - `make soap from animal fat and ash` still misanchors on `GD-262`
- Follow-up harness recovery is now fully validated on the water lane:
  - artifact set:
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.json`](../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.json)
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.logcat.txt)
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee_followup_answer.xml`](../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee_followup_answer.xml)
  - read:
    - in-detail submission succeeded without fallback
    - second-turn capture froze correctly on `what about rotation`
    - final answer stayed on `GD-252` and the source-link probe verified successfully
    - the remaining issue is now retrieval quality/speed, not follow-up harness survival
- Follow-up harness matrix validation is now real across three more families:
  - candle:
    - [`../artifacts/bench/followup_matrix_20260412/candles_followup_5554_v1_matrix.json`](../artifacts/bench/followup_matrix_20260412/candles_followup_5554_v1_matrix.json)
    - read: in-detail follow-up succeeded, frozen second-turn artifacts landed, source-link probe verified, final answer `10.8 s`
  - house:
    - [`../artifacts/bench/followup_matrix_20260412/house_followup_5558_v1_matrix.json`](../artifacts/bench/followup_matrix_20260412/house_followup_5558_v1_matrix.json)
    - read: in-detail follow-up succeeded, frozen second-turn artifacts landed, source-link probe verified, final answer `63.3 s`
  - distribution:
    - [`../artifacts/bench/followup_matrix_20260412/distribution_followup_5556_v1_matrix.json`](../artifacts/bench/followup_matrix_20260412/distribution_followup_5556_v1_matrix.json)
    - read: in-detail follow-up succeeded, frozen second-turn artifacts landed, source-link probe verified, final answer `104.9 s`
- Practical conclusion:
  - the follow-up harness should now be treated as usable infrastructure rather than the main blocker
  - the next blockers are content/retrieval quality again, especially:
    - house first-turn drifting into `GD-618`
    - water-distribution/storage-tanks drifting into `GD-648`

- Added [`../scripts/stop_android_harness_runs.ps1`](../scripts/stop_android_harness_runs.ps1) so dirty emulator lanes can be cleared without manual PID hunting.
- Patched the headless parity scorer in [`../scripts/mobile_headless_parity.py`](../scripts/mobile_headless_parity.py) so merge/trust governance prompts stop over-rewarding `mutual aid` / `pooling` section headers.
- Added a regression in [`../tests/test_mobile_headless_parity.py`](../tests/test_mobile_headless_parity.py) covering `Monitoring & Graduated Sanctions` versus `Historical Mutual Aid Models` / `Insurance Pooling and Accounting`.
- Validated the new headless governance checkpoint:
  - artifact: [`../artifacts/bench/headless_scout_20260412/mobile_headless_answers_inline_20260412_204541_154012.json`](../artifacts/bench/headless_scout_20260412/mobile_headless_answers_inline_20260412_204541_154012.json)
  - result: `how do we merge with another group if we don't trust them yet` now top-contexts on `GD-626 / Commons Management & Sustainable Resource Governance` instead of `GD-865`
- Follow-up harness status is mixed:
  - retrieval/content for the water `what about rotation` lane is still trusted from earlier Android logcat
  - the current blocker is initial detail acquisition noise on `5560`, not the underlying second-turn retrieval path
- Follow-up harness capture improved materially after the latest script patch:
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) now keeps a best terminal-state candidate and actively tries to reveal the expected answer state instead of accepting arbitrary body changes
  - fresh guardrail proof:
    - [`../artifacts/bench/android_packrefresh_v7_20260412/candles_followup_5554_v1_harnessguard.json`](../artifacts/bench/android_packrefresh_v7_20260412/candles_followup_5554_v1_harnessguard.json)
    - result: `how do i make candles for light -> what about using tallow`
    - read: second-turn answer captured cleanly, source buttons stayed visible, and source-link probe verified `GD-075`
- New headless spotcheck batch is now recorded:
  - [`../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_728424.json`](../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_728424.json)
  - [`../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_780767.json`](../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_780767.json)
  - [`../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_651143.json`](../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_651143.json)
  - [`../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_700781.json`](../artifacts/bench/headless_checks_20260412/mobile_headless_answers_inline_20260412_204821_700781.json)
  - read:
    - `safe site and foundation for a small cabin` stays in-family but still overfocuses on `GD-383` foundation sections and needs stronger site-selection mix
    - `waterproof a roof with no tar or shingles` stays on `GD-094`, but the context window still admits junk like the `Concrete Mixing Ratio Calculator`
    - `build a clay oven` is now cleanly on `GD-505`
    - `merge with another group if we don't trust each other yet` stays on `GD-626`, but it still leans too hard toward monitoring/quota sections instead of clearer trust-repair/mediation material

## Delegation Lanes

- The best subagent use is now sidecar architecture work, not live emulator steering.
- Active/high-value scout lanes for the new direction:
  - metadata schema evolution plus pack-audit workflow
  - Android transcript-first chat UI roadmap
  - prompt-family eval and gap-analysis system design
  - one systems sidecar at a time such as pack/model delivery, deterministic parity, or runtime observability
- Keep the main agent on:
  - live Android code changes
  - emulator validation
  - artifact review
  - documentation consolidation
- Treat subagents as read-only scouts unless a narrowly scoped implementation task is clearly separable.

## New Documentation / Scout Consolidation Pass

- Closed the finished brainstorm scouts after folding their output into the repo notes.
- Current live scout themes:
  - FTS5 runtime mismatch on Android
  - soap/craft refinement miss analysis
  - next chat-UI implementation slice with harness stability in mind
  - on-device backend observability for real phones like the Samsung A35
- Current real-device backend read from code:
  - ARM Android builds try LiteRT `GPU` first and fall back to `CPU` only if GPU init fails
  - emulator/x86 builds stay CPU-first by design
  - source: [`../android-app/app/src/main/java/com/senku/mobile/LiteRtModelRunner.java`](../android-app/app/src/main/java/com/senku/mobile/LiteRtModelRunner.java)

## New Gap-Pack Spotcheck Findings

- courier-authentication spotcheck remains a real gap
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/courier_auth_5556_v1.xml)
  - read: generic answer, weak verification/authentication guidance
- soapmaking spotcheck remains a real wrong-guide miss
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/soap_animal_fat_5560_v2.xml)
  - read: cites rabies/bite-care content instead of craft/refinement guidance
- canoe spotcheck looks usable but still lightly validated
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/canoe_5558_v1.xml)
- multi-objective separation spotcheck is still weak
  - artifact: [`../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml`](../artifacts/bench/gap_pack_spotcheck_20260412/multi_lane_5554_v1.xml)
  - read: does not keep the requested lanes separated cleanly

## Metadata Refresh Pass Now Live

- The Android app assets were updated from the refreshed full-pack copy at:
  - [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh)
- This is the real full pack, not the tiny experimental export.
- The refresh script now lives at:
  - [`../scripts/refresh_mobile_pack_metadata.py`](../scripts/refresh_mobile_pack_metadata.py)
- The live refresh changed the current Android baseline in a meaningful way:
  - courier-authentication is now entering a route-focused lane instead of missing metadata entirely
  - soapmaking moved out of the worst wrong-family behavior and into a more plausible craft/chemistry lane
- Current validation artifacts from this pass:
  - courier auth replay:
    - [`../artifacts/bench/metadata_refresh_20260412/courier_auth_5556_v3.xml`](../artifacts/bench/metadata_refresh_20260412/courier_auth_5556_v3.xml)
    - [`../artifacts/bench/metadata_refresh_20260412/courier_auth_5556_v3.logcat.txt`](../artifacts/bench/metadata_refresh_20260412/courier_auth_5556_v3.logcat.txt)
  - soapmaking replay:
    - [`../artifacts/bench/metadata_refresh_20260412/soapmaking_5560_v4.xml`](../artifacts/bench/metadata_refresh_20260412/soapmaking_5560_v4.xml)
    - [`../artifacts/bench/metadata_refresh_20260412/soapmaking_5560_v4.logcat.txt`](../artifacts/bench/metadata_refresh_20260412/soapmaking_5560_v4.logcat.txt)

## Harness / Experiment Notes

- [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) now handles long-answer follow-up runs more reliably by scrolling to reveal the composer and using stronger artifact-field fallbacks.
- Two explicit `water_distribution` speed experiments were tried after the snapshot and both were rejected:
  - guide-search threshold trim: faster but regressed quality back to generic `GD-252`
  - guide candidate-budget change: preserved `GD-105` but made latency worse
- Both failed experiments were reverted; current app code is back on the prior retrieval baseline plus the harness fix.

## New Validated Checkpoint After Snapshot

- Broad house route-budget trim is now live in [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java).
  - starter-build route chunk budget now trims broad house prompts from `42` candidate sections down to `30`
  - explicit house-topic follow-ups now trim from `42` down to `32`
- Validation:
  - `.\gradlew.bat testDebugUnitTest assembleDebug` passed
  - rebuilt APK reinstalled successfully to `5554`, `5556`, `5558`, and `5560`
- New broad house prompt timing:
  - artifact: [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_trimmed/house_5558_trimmed_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_trimmed/house_5558_trimmed_prompt.logcat.txt)
  - result: `how do i build a house`
  - route search dropped from about `47.4 s` to `15.6 s`
  - total initial answer time dropped from about `67.6 s` to `31.1 s`
- New broad house follow-up timing:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/house_followup_5558_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/house_followup_5558_trimmed.json)
  - result: `how do i build a house -> what next`
  - speed: `39.8 s`
  - visible source: `GD-094 / Foundations`
- Water guardrail after the house trim:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/water_followup_5554_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/water_followup_5554_trimmed.json)
  - result: still grounded in the water-storage lane
  - speed: `49.8 s`
  - read: not obviously broken by the house-only trim, but still slower than the earlier best `31.9 s` checkpoint
- Distribution guardrail after the house trim:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/distribution_followup_5556_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/distribution_followup_5556_trimmed.json)
  - result: `how do i design a gravity-fed water distribution system -> what about storage tanks`
  - speed: `65.8 s`
  - visible sources: `GD-105`, `GD-270`, `GD-105`
  - read: still in the correct gravity-fed/storage-tank lane after the house-only trim
- Candle guardrail after the house trim:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/candles_followup_5560_trimmed.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_trimmed/candles_followup_5560_trimmed.json)
  - result: `how do i make candles for light -> what about using tallow`
  - speed: `10.7 s`
  - visible source: `GD-486`
  - read: still clean after the house-only trim

## New House-Quality Follow-Up Pass

- Broad-house scoring penalty pass:
  - change: increased the generic-house penalty on thermal-efficiency/air-sealing sections in [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - intent: stop `GD-106` from outranking better generic foundation/site material on `how do i build a house`
- New broad-house initial checkpoint:
  - artifact: [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt)
  - UI dump: [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.xml`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.xml)
  - result: initial house answer is still not fully desktop-quality, but it is now foundation/frame focused instead of drifting into air sealing
  - timing: route search `23.7 s`, total initial answer `39.2 s`
  - ranking read: `GD-383` now outranks `GD-106` in support candidates
- New broad-house follow-up checkpoint:
  - artifact: [`../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json`](../artifacts/bench/host_mode_detail_20260412_postsnapshot_housepenalty/house_followup_5558_housepenalty.json)
  - result: `how do i build a house -> what next`
  - timing: `29.2 s`
  - visible sources: `GD-094`, `GD-383`, `GD-383`
  - read: continuation is cleaner and more sequential than the earlier `39.8 s` checkpoint

## Structural Finding: FTS5 Is Not Active On Android Runtime

- Live Android logcat is now explicitly showing:
  - `fts.unavailable`
  - `no such module: fts5`
- Evidence:
  - [`../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt`](../artifacts/bench/host_mode_smoke_20260412_postsnapshot_housepenalty/house_5558_housepenalty_prompt.logcat.txt)
- Practical meaning:
  - the pack still ships `lexical_chunks_fts`
  - the app still probes and attempts to use it
  - but the emulator runtime is falling back to the non-FTS lexical path
- This likely explains why Android lexical retrieval remains slower and noisier than intended.
- Treat this as a major roadmap item, not just a minor logging curiosity.

## Rejected Live Experiment

- `2026-04-12` explicit `water_distribution` route-budget trim in `PackRepository.java`
  - goal: reduce route-search cost for `gravity-fed water distribution system` / `storage tanks`
  - result: live rerun on `emulator-5556` collapsed back into `[GD-444] Accessible Shelter & Universal Design`
  - action taken: reverted immediately and rebuilt the debug APK
  - takeaway: that lane still needs breadth in route search, even though it is expensive

## Current Continuation Checkpoint

- Time check:
  - current check captured at `2026-04-12T13:11:11.1466818-05:00`
  - the active continuation floor remains `2026-04-12T16:11:42.5584523-05:00`
- New roadmap/doc checkpoint:
  - current check captured at `2026-04-12T13:35:00.8540640-05:00`
  - roadmap state is now anchored in [`ANDROID_ROADMAP_20260412.md`](./ANDROID_ROADMAP_20260412.md)
  - still actively within the required continuation window
- New live validation/doc checkpoint:
  - current check captured at `2026-04-12T13:53:50.9840092-05:00`
  - still actively within the required continuation window
- New doc-sync checkpoint:
  - current check captured at `2026-04-12T15:46:08.3916537-05:00`
  - `AGENTS.md` was refreshed to point at the newer roadmap/work-log and follow-up harness entry points
  - the active continuation floor remains `2026-04-12T18:20:15.2843349-05:00`
- New continuation check:
  - current check captured at `2026-04-12T16:05:25.2090617-05:00`
  - still actively within the required continuation window
- New checkpoint:
  - current check captured at `2026-04-12T19:22:39.1775489-05:00`
  - the active continuation floor remains `2026-04-12T22:58:07.3424219-05:00`
  - still actively within the required continuation window
- Scout housekeeping:
  - the first post-snapshot scout batch has been closed after their outputs were folded into the notes
  - their main conclusions remain:
    - Android deterministic runtime still executes only a small handwritten subset despite the pack advertising `96` rules
    - Android runtime knobs still drift from pack metadata
    - the next UI slice should be a transcript-first chat surface that preserves stable harness hooks
- New scout housekeeping:
  - stale/completed agents were cleaned up again at `2026-04-12T15:46:08.3916537-05:00`
  - `Gibbs` was completed and closed cleanly after landing the matrix harness/tooling direction

## New Retrieval / Harness Checkpoint

- New low-friction harness landed:
  - [`../scripts/run_android_search_log_only.ps1`](../scripts/run_android_search_log_only.ps1)
  - purpose: validate long Android search/retrieval lanes from `SenkuPackRepo` logcat without repeated `uiautomator dump` polling
  - practical win: avoids the `UiAutomationService ... already registered` collisions that were muddying long water runs on `5554`
- New broad water-storage Android checkpoint:
  - artifact: [`../artifacts/bench/android_searchcheck_v3j_20260412/water_5554_search.logcat.txt`](../artifacts/bench/android_searchcheck_v3j_20260412/water_5554_search.logcat.txt)
  - result: `what's the safest way to store treated water long term`
  - route search: `44.8 s`
  - total search: `54.5 s`
  - read:
    - route results cut down to `6`
    - the old `GD-373` junk siblings (`Medical Supplies`, `Cash`, `Light`, `Cold-Climate`) are gone
    - the remaining water gap is narrower now: `GD-373 :: Water Storage & Purification` still outranks more specialized storage guides
- New community security Android checkpoint:
  - artifact: [`../artifacts/bench/android_searchcheck_v3i_20260412/community_security_5556_search.logcat.txt`](../artifacts/bench/android_searchcheck_v3i_20260412/community_security_5556_search.logcat.txt)
  - result: `How do we protect a vulnerable work site without spreading people too thin?`
  - route guide search now skips at `sections=44 threshold=12`
  - route search dropped to `54.7 s`
  - total search dropped to `77.5 s`
  - top family stayed correct: `GD-651` then `GD-388`
- New community governance theft checkpoint:
  - artifact: [`../artifacts/bench/android_searchcheck_v3i_20260412/governance_theft_5558_search.logcat.txt`](../artifacts/bench/android_searchcheck_v3i_20260412/governance_theft_5558_search.logcat.txt)
  - result: `What do we do if someone keeps stealing from the group food stores?`
  - route guide search now skips at `sections=46 threshold=12`
  - route search dropped to `67.4 s`
  - top family stayed correct: `GD-626`
- New community governance merge checkpoint:
  - artifact: [`../artifacts/bench/android_searchcheck_v3i_20260412/governance_merge_5560_search.logcat.txt`](../artifacts/bench/android_searchcheck_v3i_20260412/governance_merge_5560_search.logcat.txt)
  - result: `How do we merge with another group if we don't trust each other yet?`
  - route guide search now skips at `sections=15 threshold=12`
  - route search dropped from the older ~`194.7 s` band to `26.7 s`
  - total search dropped from the older ~`212.2 s` band to `54.7 s`
  - top family stayed correct: `GD-626`

## New Android Water-Route Checkpoint

- Refreshed continuation floor:
  - floor started at `2026-04-12T18:58:07.3424219-05:00`
  - continue active iteration/testing until at least `2026-04-12T22:58:07.3424219-05:00`
- Current stable checkpoint time:
  - verified at `2026-04-12T19:16:22.1600763-05:00`
- Code changes in this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
    - broad `water_storage` route filtering now rejects generic zero-signal rows unless they have stronger storage-specific topic tags
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
    - long-term water-storage planning prompts now use the compact route sweep instead of the wider one
  - tests:
    - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
    - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
- Validation:
  - alt-build targeted JVM suite passed:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryRouteProfileTest --tests com.senku.mobile.PackRepositoryTest`
  - alt-build APK assembled and reinstalled to all four emulators from:
    - [`../artifacts/tmp/android-app-altbuild/app/build/outputs/apk/debug/app-debug.apk`](../artifacts/tmp/android-app-altbuild/app/build/outputs/apk/debug/app-debug.apk)
- Latest live Android evidence:
  - water manual replay:
    - [`../artifacts/bench/android_searchcheck_v3f_20260412/water_5554_manual_v2.logcat.txt`](../artifacts/bench/android_searchcheck_v3f_20260412/water_5554_manual_v2.logcat.txt)
    - result: route search dropped to about `50.9 s`, total search to about `62.7 s`
    - top results no longer include `GD-373` cash/light/medical/cold-climate sections
    - remaining top result is still `GD-373 :: Home Inventory :: Water Storage & Purification`
    - specialized support now sits immediately behind it: `GD-417`, `GD-035`, `GD-252`
  - community security guard:
    - [`../artifacts/bench/android_searchcheck_v3f_20260412/community_security_5556_search.logcat.txt`](../artifacts/bench/android_searchcheck_v3f_20260412/community_security_5556_search.logcat.txt)
    - still tops out on `GD-651`, with `GD-388` as secondary support
  - governance theft guard:
    - [`../artifacts/bench/android_searchcheck_v3f_20260412/governance_theft_5558_search.logcat.txt`](../artifacts/bench/android_searchcheck_v3f_20260412/governance_theft_5558_search.logcat.txt)
    - still tops out on `GD-626`
- Practical read:
  - this was a real retrieval improvement, not just a timing fluke
  - the broad water lane is no longer dominated by obvious inventory junk
  - the next water pass should demote broad inventory starter guidance beneath specialized storage guides, then move that fix back into pack metadata so runtime heuristics can stay lighter
  - `Maxwell` did not answer a direct status ping and was closed as stale so new scout work can start from a clean pool
- Real-device backend behavior:

## New Structured State Tracker

- Machine-readable tracker landed at:
  - [`AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Validator landed at:
  - [`../scripts/validate_agent_state.py`](../scripts/validate_agent_state.py)
- Purpose:
  - keep active emulator lanes, latest artifacts, continuation timing, and next actions in a deterministic format that subagents and harness scripts can read without parsing prose

## New Water-Distribution Topic-Seed Fix

- Android retrieval patch:
  - explicit `water_distribution` first-turn queries no longer seed `container_sanitation` and `water_rotation` into the query metadata profile
  - support filtering for explicit distribution prompts was tightened further in [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
- Regression coverage:
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
- Validation:
  - `.\gradlew.bat testDebugUnitTest` passed in `android-app`
  - `python -m unittest tests.test_mobile_pack -v` passed
  - alt-build `testDebugUnitTest assembleDebug` passed in [`../artifacts/tmp/android-app-altbuild`](../artifacts/tmp/android-app-altbuild)
- Live checkpoint:
  - XML: [`../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_topicseedfix.xml`](../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_topicseedfix.xml)
  - logcat: [`../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_topicseedfix.logcat.txt`](../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_topicseedfix.logcat.txt)
  - result:
    - `preferredTopicTags` in the trace are now `water_distribution, water_storage`
    - the old polluted `container_sanitation` / `water_rotation` seed is gone
    - bad `GD-252` support leakage disappeared from the trace
    - remaining miss: anchor is still `GD-105` and the answer is still too generic

## New Continuation Check

- current check captured at `2026-04-12T16:25:33.4130786-05:00`
- the active continuation floor remains `2026-04-12T18:20:15.2843349-05:00`
- still actively within the required continuation window
  - on actual ARM Android hardware, the current build attempts LiteRT `GPU` first and falls back to `CPU` only if GPU initialization fails
  - evidence is in [`../android-app/app/src/main/java/com/senku/mobile/LiteRtModelRunner.java`](../android-app/app/src/main/java/com/senku/mobile/LiteRtModelRunner.java), where x86/generic/ranchu/goldfish paths force CPU but non-emulator builds try `new Backend.GPU(), new Backend.CPU()`
  - the current UI also surfaces the last successfully loaded local backend in [`../android-app/app/src/main/java/com/senku/mobile/MainActivity.java`](../android-app/app/src/main/java/com/senku/mobile/MainActivity.java)
- Harness state:

## New Fast Pack Refresh Lane

- Pack-only refresh script landed at:
  - [`../scripts/push_mobile_pack_to_android.ps1`](../scripts/push_mobile_pack_to_android.ps1)
- Purpose:
  - push a refreshed `mobile_pack` into `files/mobile_pack` on a debuggable emulator/device via `adb` + `run-as`
  - avoid Gradle rebuild + APK reinstall when only the pack changes
- Verified smoke:
  - pack push target: `emulator-5556`
  - source pack: [`../artifacts/tmp/android-app-altbuild/app/src/main/assets/mobile_pack`](../artifacts/tmp/android-app-altbuild/app/src/main/assets/mobile_pack)
  - the push + restart path completed successfully with installed size verification
  - follow-on replay artifacts:
    - [`../artifacts/bench/pack_push_smoke_20260412/distribution_5556_pushsmoke.xml`](../artifacts/bench/pack_push_smoke_20260412/distribution_5556_pushsmoke.xml)
    - [`../artifacts/bench/pack_push_smoke_20260412/distribution_5556_pushsmoke.logcat.txt`](../artifacts/bench/pack_push_smoke_20260412/distribution_5556_pushsmoke.logcat.txt)
  - replay read:
    - `fts.unavailable` is still present
    - `structure=water_distribution`
    - `routeSpecs=5`
    - `context anchorGuide="GD-270"`
    - host backend remained `gpu`
- Current meaning:
  - pack edits no longer need to pay the full Android rebuild/install tax
  - this should become the default lane for metadata-only or pack-only iteration

## New Water-Distribution Layout Pass

- Narrow Android tuning pass landed in:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
- Change intent:
  - explicit gravity-fed design prompts now prefer layout-focused sections like `Community Water Point Design` and `Distribution Network Layout`
  - `Common Mistakes in Water System Design` is no longer promoted into the first-turn answer window for the base design query
  - explicit water-distribution prompt guidance now stresses `head pressure`, `main line layout`, `storage/pressure`, and `outlets/limits`
  - explicit water-distribution prompts now use `4` context notes and a larger excerpt budget
- Validation:
  - `android-app` `testDebugUnitTest` passed
  - alt-build `testDebugUnitTest assembleDebug` passed
  - rebuilt alt-build APK installed successfully across `5554`, `5556`, `5558`, and `5560`
- New primary checkpoint:
  - [`../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.xml`](../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.xml)
  - [`../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.logcat.txt`](../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.logcat.txt)
  - read:
    - `context.selected` is now `GD-270` sections `1`, `2`, `5`, and `4`
    - `Common Mistakes` dropped out
    - visible answer now says `head pressure`, `main line`, `elevated storage/water tower`, and `community water points`
- Guardrail replays after this patch:
  - water storage:
    - [`../artifacts/bench/distribution_guardrails_20260412/water_5554_v1.xml`](../artifacts/bench/distribution_guardrails_20260412/water_5554_v1.xml)
    - [`../artifacts/bench/distribution_guardrails_20260412/water_5554_v1.logcat.txt`](../artifacts/bench/distribution_guardrails_20260412/water_5554_v1.logcat.txt)
    - read: still anchored on `GD-252`
  - broad house:
    - [`../artifacts/bench/distribution_guardrails_20260412/house_5558_v1.xml`](../artifacts/bench/distribution_guardrails_20260412/house_5558_v1.xml)
    - [`../artifacts/bench/distribution_guardrails_20260412/house_5558_v1.logcat.txt`](../artifacts/bench/distribution_guardrails_20260412/house_5558_v1.logcat.txt)
    - read: visible answer still sane, though support mix still includes `Outbuildings for Off-Grid Living`
  - candles:
    - [`../artifacts/bench/distribution_guardrails_20260412/candles_5560_v1.xml`](../artifacts/bench/distribution_guardrails_20260412/candles_5560_v1.xml)
    - [`../artifacts/bench/distribution_guardrails_20260412/candles_5560_v1.logcat.txt`](../artifacts/bench/distribution_guardrails_20260412/candles_5560_v1.logcat.txt)
    - read: deterministic candle lane remains instant and intact

## New Continuation Check

- current check captured at `2026-04-12T16:47:20.2023857-05:00`
- the active continuation floor remains `2026-04-12T20:36:45.8552703-05:00`
- still actively within the required continuation window
  - the in-detail follow-up harness was recently hardened to scroll long answers until the follow-up composer is visible again and to recover header/title metadata more reliably after follow-up completion
  - file: [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
- Live gap-pack spotchecks now worth preserving:
  - courier-authentication prompt is still shallow/generic instead of building a robust verification lane
  - soapmaking prompt is a clear miss and surfaced the wrong guide family
  - canoe prompt looked broadly usable
  - multi-objective separation prompt stayed too blended instead of keeping priorities separated cleanly

## New Specialized-Lane Retrieval Patch

- Android retrieval was patched to improve specialized route-focused lanes without hardcoding guide IDs:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
- Main behavior changes:
  - `message_auth` route specs now search the `communications` family instead of only `society`
  - `soapmaking` route specs now stay on `crafts` + `chemistry` and add a stronger cold-process/cure lane
  - route-focused specialized prompts now require stronger direct topical anchor signal for `message_auth`, `soapmaking`, `glassmaking`, and `fair_trial`
  - message-auth and soapmaking section headings now get better metadata bonuses for concrete sections and stronger penalties for generic overview/governance drift
- Regression coverage added in:
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
- Validation:
  - `.\gradlew.bat testDebugUnitTest` passed
  - `.\gradlew.bat assembleDebug` passed
  - rebuilt APK reinstalled successfully to `5554`, `5556`, `5558`, and `5560`

## New Targeted Replay Results After Specialized-Lane Patch

- courier authentication on `5556`
  - artifacts:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.xml`](../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.xml)
    - [`../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.logcat.txt`](../artifacts/bench/metadata_refresh_20260412_patch/courier_auth_5556_v4.logcat.txt)
  - result:
    - live logcat now shows `anchorGuide="GD-389"`
    - support candidates moved into the message-auth family (`GD-436` sections)
    - subtitle time `22.7 s`
  - read:
    - retrieval-family fix is real
    - answer is still somewhat generic, but it is now grounded in the right guide family instead of generic governance
- soapmaking on `5556`
  - artifact:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5556_v1.xml`](../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5556_v1.xml)
  - result:
    - subtitle time `62.4 s`
    - answer now stays on a safe lye/fat starter path with curing + safety instead of the old wrong-family miss

## New Harness Repair Checkpoint

- Docs/landing sync:
  - [`../AGENTS.md`](../AGENTS.md) was refreshed to point at the newer roadmap/work-log notes and the dedicated follow-up harness scripts.
- Subagent cleanup:
  - the lingering completed scout was closed
  - the one stale non-responsive scout was also closed after a direct status ping failed
  - the pool is clean again before the next scout batch
- Follow-up harness repair:
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) now verifies that the in-detail follow-up actually advances after submit instead of assuming the first send tap registered
  - the runner now:
    - looks for immediate post-submit advance signals
    - retries `Enter` once locally if no advance is visible
    - records `followup_submission_advanced_after_submit` in the manifest
  - the source-link reveal budget was also increased for long answers
  - the harness wrappers now also preflight-clean conflicting same-emulator follow-up runners:
    - [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1)
    - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
  - this is now part of the tooling contract, not just a manual cleanup habit
- Verified replay after the harness repair:
  - artifact:
    - [`../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v2.json`](../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v2.json)
    - [`../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v2.logcat.txt`](../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v2.logcat.txt)
  - result:
    - `how do i design a gravity-fed water distribution system -> what about storage tanks`
    - stayed on `in_detail_ui`
    - `followup_submission_used_fallback = false`
    - `followup_submission_primary_signal = visible_send_button_advanced`
    - `followup_submission_advanced_after_submit = true`
    - total answer time `63.3 s`
    - answer stayed grounded on `GD-270`
- Remaining harness edge case:
  - the next replay on the same lane hit a separate initial-screen failure where the captured `initial.xml` was the Android launcher home screen rather than the Senku detail screen
  - artifact:
    - [`../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v3_initial.xml`](../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v3_initial.xml)
  - read:
    - this is not the old post-submit fallback bug
    - it is a different harness reliability issue around initial detail-screen acquisition or app focus
- Time check after the harness repair pass:
  - current check captured at `2026-04-12T16:00:42.3210475-05:00`
  - still actively within the required continuation window

## New Harness Checkpoint: Distribution Follow-Up Submission

- Follow-up harness fix landed in:
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
- What changed:
  - terminal-state detection now makes a short controlled follow-up-control reveal attempt before assuming the detail view never finished
  - in-detail follow-up submission now retries the send path more deliberately
  - if no real advance signal appears after repeated send attempts, the harness falls back immediately instead of waiting through the long completion timeout
- New logged validation:
  - [`../artifacts/bench/harness_diag_20260412/distribution_followup_5556_logged_fix2.json`](../artifacts/bench/harness_diag_20260412/distribution_followup_5556_logged_fix2.json)
  - [`../artifacts/bench/harness_diag_20260412/distribution_followup_5556_logged_fix2.logcat.txt`](../artifacts/bench/harness_diag_20260412/distribution_followup_5556_logged_fix2.logcat.txt)
- Result:
  - `how do i design a gravity-fed water distribution system -> what about storage tanks`
  - follow-up stayed on `in_detail_ui`
  - `followup_submission_primary_signal = visible_send_button_advanced`
  - fallback is now `false`
  - answer stayed correct on `GD-270`
  - follow-up subtitle time dropped to `63.3 s` from the older `104.7 s` fallback-flavored checkpoint
- Remaining harness quirk:
  - that logged rerun missed the guide-detail source probe even though the visible source buttons were correct
  - follow-up harness was then patched again so source probing reveals the guide header before validation instead of judging only from a body-first dump
- Fast-lane confirmation of the source-probe fix:
  - [`../artifacts/bench/harness_diag_20260412/candles_followup_5560_direct_fix3.json`](../artifacts/bench/harness_diag_20260412/candles_followup_5560_direct_fix3.json)
  - result:
    - `how do i make candles for light -> what about using tallow`
    - follow-up stayed on `in_detail_ui`
    - source-link probe is back to `verified = true`
    - observed guide title correctly resolved to `Animal Fat Rendering and Tallow Uses`

## Documentation Sync Checkpoint

- Fresh time check captured at `2026-04-12T15:46:08.3916537-05:00`
  - still actively within the required continuation floor of `2026-04-12T18:20:15.2843349-05:00`
- [`../AGENTS.md`](../AGENTS.md) was refreshed to point at the current Android workstream docs and harness entry points:
  - [`./ANDROID_ROADMAP_20260412.md`](./ANDROID_ROADMAP_20260412.md)
  - [`./ACTIVE_WORK_LOG_20260412.md`](./ACTIVE_WORK_LOG_20260412.md)
  - [`./METADATA_AUDIT_20260412.md`](./METADATA_AUDIT_20260412.md)
  - [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1)
  - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
- Agent housekeeping:
  - stale completed scout `Gibbs` was closed after its roadmap/tooling output was folded in
  - stale non-responsive scout `Maxwell` was closed after it failed a direct status ping
  - a fresh four-scout batch was then launched for:
    - metadata audit design
    - transcript-first chat UI planning
    - prompt-family validation/gap-system design
    - medium-term systems architecture

## New Headless Preflight Lane

- Added a new emulator-free family-pack preflight harness:
  - [`../scripts/run_mobile_headless_preflight.py`](../scripts/run_mobile_headless_preflight.py)
- Purpose:
  - summarize the current Android family prompt packs
  - count prompts per family
  - attach the current metadata-audit read for the overlapping audited families
  - give a fast breadth triage lane before spending emulator time
- Current validated run:
  - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_155719.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_155719.json)
- Updated run with cross-family guide review queue:
  - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_155937.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_155937.json)
- Current read:
  - construction pack: `10` prompts
  - water pack: `11` prompts, linked audit still shows `water_distribution` at `134` mismatches
  - crafts pack: `10` prompts, linked audits show `soapmaking=30` mismatches and `glassmaking=71`
  - comms/governance pack: `9` prompts, linked audit shows `message_auth=17`
  - medicine pack: `10` prompts
  - multi-objective pack: `8` prompts
  - follow-up matrix: `10` cases
- Practical meaning:
  - we do not need to burn emulator time on every prompt to keep progress coherent
  - emulator runs should now be treated as guardrail/end-to-end truth lanes after headless preflight narrows the likely problem families
  - manual metadata review can now start from a ranked guide queue instead of raw prompt misses
- Current top cross-family review queue from the updated preflight:
  - `GD-105` Plumbing & Water Systems
  - `GD-253` Thermal Energy Storage
  - `GD-036` Sanitation & Public Health
  - `GD-224` Blacksmithing
  - `GD-270` Community Water Distribution Systems

## Water-Distribution Schema Split Checkpoint

- A reduced schema split is now landed in code:
  - pack builder can emit `water_distribution` as a real `structure_type`
  - Android metadata scoring now treats explicit water-distribution rows as first-class matches instead of generic `water_storage`
- Validation:
  - `python -m unittest tests.test_mobile_pack -v` passed
  - `.\gradlew.bat testDebugUnitTest` passed
- Safe refresh target:
  - [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v4`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v4)
- New headless proof point:
  - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_160223.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_160223.json)
- Result:
  - `water_distribution` mismatches dropped from `134` to `104`
  - `GD-105` fell out of the top cross-family queue
  - the remaining big water-distribution queue is now led by `GD-253`, `GD-036`, and `GD-553`
- Practical read:
  - the schema split is already reducing audit noise off-emulator
  - next decision is whether to promote the refreshed `v4` pack into the live Android assets for targeted emulator confirmation

## Water-Distribution Precedence Follow-Up

- The first `water_distribution` split still left `GD-553` mislabeled because guide-level `water storage` wording was winning before the stronger distribution signal.
- A follow-up precedence fix is now landed in [`../mobile_pack.py`](../mobile_pack.py):
  - core water-distribution markers now win earlier in structure detection for mixed overview guides
- Validation:
  - `python -m unittest tests.test_mobile_pack -v` passed again
- New refreshed pack:
  - [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v5`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v5)
- New best headless checkpoint:
  - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_161225.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_161225.json)
- Result:
  - `water_distribution` mismatches dropped again from `104` to `91`
  - `GD-553` now carries `structure_type=water_distribution`
  - `GD-553` fell out of the top cross-family review queue
  - the leading remaining water-distribution noise is now mostly `GD-253` and `GD-036`
- Practical read:
  - the off-emulator metadata loop is working exactly the way we want
  - the next clean step is deciding when to promote `v5` into the live Android pack and replay the `5556` distribution lane with the cleaner harness artifacts

## Current Continuation Checkpoint

- Time check:
  - current check captured at `2026-04-12T15:57:25.7686025-05:00`
  - still actively within the required continuation floor of `2026-04-12T18:20:15.2843349-05:00`
  - read:
    - materially improved output
    - source capture still needs better visibility on this run
- soapmaking on `5560`
  - artifacts:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5560_v5.xml`](../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5560_v5.xml)
    - [`../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5560_v5.logcat.txt`](../artifacts/bench/metadata_refresh_20260412_patch/soapmaking_5560_v5.logcat.txt)
  - read:
    - this lane was partially improved but the harness/runtime state on `5560` was dirty and not trustworthy for a final score
    - keep `5556` as the current reliable soapmaking checkpoint
- water long-term storage on `5554`
  - artifact:
  - [`../artifacts/bench/metadata_refresh_20260412_patch/water_5554_v15.xml`](../artifacts/bench/metadata_refresh_20260412_patch/water_5554_v15.xml)
  - result:
    - subtitle time `37.0 s`
    - still grounded on `[GD-252] Storage & Material Management`
  - read:
    - water guardrail held after the specialized-lane patch
- broad house on `5558`

## Metadata-First Pivot

- A pack-side metadata issue is now confirmed as a real cross-topic root cause, not just a soapmaking one-off.
- Concrete example:
  - in the previous refreshed pack, `GD-571` chunks that clearly discussed soapmaking still exported `structure_type=glassmaking`
  - several of those rows also lacked `soapmaking` topic tags entirely
- Root cause identified in [`../mobile_pack.py`](../mobile_pack.py):
  - chemistry structure detection only recognized `soapmaking` / `glassmaking` from `core_text`
  - chunk metadata then fell back to guide-level structure when local chunk detection stayed `general`
- New pack-builder work landed:
  - `soapmaking` can now be detected from strong chunk-body markers, not only headings / slugs
  - conservative regression tests were added so incidental “soap is slippery” chemistry text does not flip whole chunks into `soapmaking`
- Validation:
  - `python -m unittest tests.test_mobile_pack -v` passed after the new metadata tests
- New tooling:
  - [`../scripts/audit_mobile_pack_metadata.py`](../scripts/audit_mobile_pack_metadata.py) now scans a mobile pack for likely metadata mismatches across specialized families
- New pack refresh:
  - refreshed pack copies:
    - [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v2`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v2)
    - [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v3`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v3)
  - the latest embedded Android pack is now `v3`
- New live direction:
  - metadata verification and pack refresh now take priority over more ad hoc Android route nudges
  - the next question is how much the refreshed pack improves real emulator replays once the harness settles after the asset swap
  - artifact:
    - [`../artifacts/bench/metadata_refresh_20260412_patch/house_5558_v11.xml`](../artifacts/bench/metadata_refresh_20260412_patch/house_5558_v11.xml)
  - result:
    - subtitle time `69.9 s`
    - answer drifted back toward a shed/frame path
  - read:
    - no catastrophic retrieval collapse, but quality is softer again
    - house still needs a separate quality/speed pass and should not be treated as solved by the specialized-lane patch

## Structural Scout Confirmation: Android FTS5 Is Still Off

- The FTS scout confirmed the likely root cause for the Android lexical mismatch:
  - the app is opening the pack through framework SQLite and is not currently bundling an alternate SQLite runtime with known FTS5 support
  - the pack ships `lexical_chunks_fts`, but runtime probing still fails with `no such module: fts5`
- Practical takeaway:
  - current Android lexical timings should still be treated as fallback-path timings, not real FTS timings
  - fix the SQLite runtime path before over-tuning lexical budgets again

## Runtime Design Principle To Keep

- Avoid baking guide IDs or guide titles directly into new runtime retrieval logic when a metadata/alias/category signal can do the job.
- Guide IDs are still useful in:
  - benchmark artifacts
  - notes
  - regression assertions about observed outputs
- But the retrieval/rerank direction should keep moving toward structure types, topic tags, aliases, and pack-exported metadata instead of `GD-xxx` dependencies.

## New Mid-Afternoon Checkpoint

- Time check:
  - current check captured at `2026-04-12T14:18:25.9281557-05:00`
  - still actively within the required continuation window ending at `2026-04-12T16:11:42.5584523-05:00`
- Snapshot state:
  - the snapshot folder is still present at [`../artifacts/snapshots/senku_android_snapshot_20260412_120727`](../artifacts/snapshots/senku_android_snapshot_20260412_120727)
  - the user-kept desktop zip copy exists at:
    - [`C:/Users/tateb/Desktop/senku_android_snapshot_20260412_120727.zip`](C:/Users/tateb/Desktop/senku_android_snapshot_20260412_120727.zip)
  - a repo-local zip copy has now been restaged at:
    - [`../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip`](../artifacts/snapshots/senku_android_snapshot_20260412_120727.zip)
- Runtime knob drift confirmed again:
  - pack manifest still advertises `mobile_top_k = 10` in [`../android-app/app/src/main/assets/mobile_pack/senku_manifest.json`](../android-app/app/src/main/assets/mobile_pack/senku_manifest.json)
  - live Android answer generation is still using hardcoded limits in [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java):
    - `ANSWER_CANDIDATE_LIMIT = 16`
    - `ANSWER_CONTEXT_LIMIT = 4`
  - practical takeaway:
    - changing manifest `mobile_top_k` alone does not currently change Android answer behavior
- New soapmaking replay lane after more metadata/anchor tightening:
  - artifacts:
    - [`../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.xml`](../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.xml)
    - [`../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.logcat.txt`](../artifacts/bench/metadata_refresh_20260412_soapfocus/soapmaking_5556_v6_retry.logcat.txt)
  - result:
    - subtitle improved to `40.4 s`
    - answer body is still broadly usable
    - anchor is still wrong-family `GD-571`
  - read:
    - the recent fixes reduced route-guide breadth and improved latency
    - they did not solve the deeper anchor-family problem
    - the likely next lever is metadata verification plus broader anchor-pool gating, not another small keyword tweak

## Late-Afternoon Follow-Up Rerank Checkpoint

- Time check:
  - checkpoint recorded after `2026-04-12T15:08:09-05:00`
  - still inside the active continuation floor ending at `2026-04-12T18:20:15.2843349-05:00`
- New Android retrieval/rerank work landed:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
    - generic `water_storage` headings now get positive section lift
    - `chemical storage / hazard management` sections now take a much stronger penalty for generic water-storage lanes
  - [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java)
    - session reranking now boosts explicit-follow-up raw topic matches harder
    - recent-guide recency is reduced when it is only repeating last-turn guide context without matching the new explicit follow-up topic
  - tests expanded in:
    - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
    - [`../android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`](../android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java)
- Validation:
  - alt-build Android validation passed again with `testDebugUnitTest` + `assembleDebug`
  - APK reinstalled across all 4 emulators from the clean temp build tree
- Live replay results:
  - house drainage follow-up restored on `5558`:
    - artifact: [`../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4d.json`](../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4d.json)
    - result:
      - subtitle `38.0 s`
      - answer now grounds on `GD-333`
      - visible sources: `GD-333 Swale Design for Water Harvesting and Flow` + `GD-094 Foundations`
    - important diagnosis:
      - fresh paired logcat proved route search never lost drainage; the failure was later in session-aware reranking, where recent `GD-094` context pushed drainage below the prompt window
  - water `what next` improved on `5554`:
    - artifact: [`../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4c.json`](../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4c.json)
    - result:
      - subtitle `39.2 s`
      - answer now stays on `GD-252` with food-safe containers + FIFO rotation
      - visible sources are now `Water Storage: Hydration Assurance` + `Rotation Schedules: FIFO Implementation & Discipline`
    - remaining issue:
      - the detail follow-up harness fell back to `auto_followup_fallback`
      - source-link probe did not reopen the expected detail view on that run, so harness/UI robustness is still a live follow-up item
  - candle/tallow guard stayed clean on `5560`:
    - artifact: [`../artifacts/bench/metadata_refresh_20260412_v4/candles_followup_5560_v4b.json`](../artifacts/bench/metadata_refresh_20260412_v4/candles_followup_5560_v4b.json)
    - result:
      - subtitle `7.8 s`
      - single visible source remains `GD-486`
- Future-direction scout synthesis worth keeping:
  - the strongest systems recommendation was to stop maintaining separate desktop/mobile deterministic brains
  - preferred direction:
    - compile deterministic rules from desktop into a portable payload exported through the pack
    - load that payload on Android instead of growing the hardcoded Java router forever
  - this should stay behind the current retrieval/harness cleanup, but it is now a real roadmap item rather than a vague later idea

## Late-Afternoon Concurrency + Follow-Up Checkpoint

- Time check:
  - current checkpoint recorded after `2026-04-12T15:08:14-05:00`
  - still inside the active continuation floor ending at `2026-04-12T18:20:15.2843349-05:00`
- New Android follow-up fixes landed:
  - [`../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`](../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java)
    - focused `cabin_house` follow-ups such as `what about drainage` now keep the retrieval/context query tight instead of re-importing broad roof/wall/weatherproofing carryover from the last turn
    - the practical effect is a narrower house follow-up query closer to the older good `drainage + foundation + site selection` shape
  - [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java)
    - session rerank now treats short explicit `cabin_house` follow-ups as higher-signal than generic recent-guide continuity
    - this keeps direct drainage sections in the prompt window instead of letting prior `GD-094` house sections crowd them back out
  - tests extended in:
    - [`../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java)
    - existing regression guard in [`../android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`](../android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java) is now passing again
- Validation:
  - clean alt-build Android pass succeeded again with `testDebugUnitTest` + `assembleDebug`
  - APK reinstalled to all 4 emulators from the temp build tree
- Live replay results on the fresh build:
  - house drainage follow-up on `5558` recovered in:
    - [`../artifacts/bench/metadata_refresh_20260412_v5/house_followup_5558_v5.json`](../artifacts/bench/metadata_refresh_20260412_v5/house_followup_5558_v5.json)
    - result:
      - subtitle `38.0 s`
      - answer is now grounded on `GD-333` swale/drainage content again
      - important nuance:
        - this run finished through harness fallback rather than the in-detail input path, so the retrieval/content result is trusted
        - the missing in-detail source buttons are now considered a harness/UI robustness problem, not evidence that retrieval failed again
  - water `what next` on `5554` improved further in:
    - [`../artifacts/bench/metadata_refresh_20260412_v5/water_followup_5554_v5.json`](../artifacts/bench/metadata_refresh_20260412_v5/water_followup_5554_v5.json)
    - result:
      - subtitle `39.2 s`
      - answer now stays on food-grade containers + FIFO rotation under `GD-252`
      - chemical-storage drift is gone in the live answer body
      - same tooling note:
        - this run also finished via harness fallback, so source-button probing was not available
  - candle/tallow guard stayed clean on `5560` in:
    - [`../artifacts/bench/metadata_refresh_20260412_v5/candles_followup_5560_v5.json`](../artifacts/bench/metadata_refresh_20260412_v5/candles_followup_5560_v5.json)
    - result:
      - subtitle `8.2 s`
      - single visible source remains `GD-486`
      - in-detail follow-up UI path still worked normally here
  - gravity-fed distribution `storage tanks` guard on `5556` stayed content-correct in:
    - [`../artifacts/bench/metadata_refresh_20260412_v5/distribution_followup_5556_v5.json`](../artifacts/bench/metadata_refresh_20260412_v5/distribution_followup_5556_v5.json)
    - result:
      - subtitle `85.5 s`
      - answer body stays on `GD-270` storage-tank / water-tower planning
      - post-answer source surface is still wrong/noisy, showing `GD-252 Grain Storage` instead of the real answer guide
    - read:
      - this is more evidence that the current weak point is follow-up harness/source-probe reliability rather than retrieval quality
- Tooling/concurrency sidecars now active:
  - `Maxwell` is building a faster Android testing harness sidecar
  - `Gibbs` is mapping near-term validation-efficiency improvements and safe parallelization boundaries
  - `Socrates` is building prompt-family batching aids under `artifacts/prompts`
- Prompt-family batching aid landed:
  - new family packs now live under [`../artifacts/prompts/families_20260412`](../artifacts/prompts/families_20260412)
  - entry point:
    - [`../artifacts/prompts/families_20260412/README.md`](../artifacts/prompts/families_20260412/README.md)
  - the pack set includes:
    - core single-turn family packs
    - a follow-up matrix shortlist
    - suggested emulator lane assignments for construction, water, crafts, and comms/governance
- First family-pack spot checks:
  - soapmaking on `5560`:
    - artifact: [`../artifacts/bench/family_spotchecks_20260412/soapmaking_5560_v1.xml`](../artifacts/bench/family_spotchecks_20260412/soapmaking_5560_v1.xml)
    - read:
      - completed in `89.1 s`
      - answer is broadly usable and safety-aware
      - still too generic / starter-path shaped, which keeps soapmaking on the metadata + anchor-family cleanup queue
  - courier authentication on `5556`:
    - artifact: [`../artifacts/bench/family_spotchecks_20260412/courier_auth_5556_v1.xml`](../artifacts/bench/family_spotchecks_20260412/courier_auth_5556_v1.xml)
    - read:
      - completed in `40.9 s`
      - answer is grounded on chain-of-custody behavior and cites `GD-389`
      - this looks like a solid green comms/governance spot check
- Current read:
  - retrieval quality is improving faster than harness robustness right now
  - the next speed/cleanliness win is likely in test tooling:
    - matrix wrappers
    - automatic logcat capture
    - better artifact naming
    - more reliable in-detail follow-up submission detection
    - specifically:
      - relax the current strict typed-text echo check in [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) before triggering `auto_followup_fallback`

## Harness Recovery Checkpoint

- Time check:
  - checkpoint recorded after the `v6` replay pass on `2026-04-12T15:23:54-05:00`
  - still inside the active continuation floor ending at `2026-04-12T18:20:15.2843349-05:00`
- Harness state:
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1) is now behaving correctly for the previously flaky house and water follow-up lanes
  - key read:
    - the harness now stays on the in-detail composer path instead of dropping to `auto_followup_fallback` just because the input-text echo is unreliable in the accessibility dump
- Fresh `v6` validation:
  - house drainage follow-up on `5558`:
    - [`../artifacts/bench/metadata_refresh_20260412_v6/house_followup_5558_v6.json`](../artifacts/bench/metadata_refresh_20260412_v6/house_followup_5558_v6.json)
    - result:
      - subtitle `55.9 s`
      - `followup_submission_mode = in_detail_ui`
      - `followup_submission_input_match = true`
      - source probing succeeded again
      - visible sources now include `GD-333 Swale Design for Water Harvesting and Flow`, plus `GD-094` support
  - water `what next` on `5554`:
    - [`../artifacts/bench/metadata_refresh_20260412_v6/water_followup_5554_v6.json`](../artifacts/bench/metadata_refresh_20260412_v6/water_followup_5554_v6.json)
    - result:
      - subtitle `54.8 s`
      - `followup_submission_mode = in_detail_ui`
      - `followup_submission_input_match = true`
      - source probing succeeded again
      - visible sources now correctly show `GD-252` hydration + FIFO rotation + food-storage support
- Practical takeaway:
  - the retrieval fixes were already real in `v5`
  - `v6` confirms the harness/source-probe surface has now caught back up for the two most important flaky follow-up lanes
  - next follow-up harness confirmation target is the gravity-fed distribution `storage tanks` lane on `5556`

## Prompt Family Batching + Latest Live Read

- Prompt batching sidecar landed under:
  - [`../artifacts/prompts/families_20260412/README.md`](../artifacts/prompts/families_20260412/README.md)
  - [`../artifacts/prompts/families_20260412/android_family_manifest_20260412.json`](../artifacts/prompts/families_20260412/android_family_manifest_20260412.json)
- New reusable family packs now exist for:
  - `construction_watercraft`
  - `water_infra_sanitation`
  - `craft_refinement`
  - `medical_first_aid`
  - `comms_governance_security`
  - `multi_objective_separation`
  - plus a shared follow-up shortlist in [`../artifacts/prompts/families_20260412/followup_matrix_20260412.jsonl`](../artifacts/prompts/families_20260412/followup_matrix_20260412.jsonl)
- Practical use:
  - `.txt` packs are ready for single-turn Android batch runs
  - the follow-up matrix is the new quick-pick shortlist for session validation across the 4-emulator grid
- Latest live checkpoints on the fresh post-rerank build:
  - house drainage on `5558`:
    - [`../artifacts/bench/metadata_refresh_20260412_v5/house_followup_5558_v5.json`](../artifacts/bench/metadata_refresh_20260412_v5/house_followup_5558_v5.json)
    - answer is back on `GD-333` drainage/swale content
    - subtitle `38.0 s`
    - follow-up landed through harness fallback, so source-button verification was not available on that run
  - water `what next` on `5554`:
    - [`../artifacts/bench/metadata_refresh_20260412_v5/water_followup_5554_v5.json`](../artifacts/bench/metadata_refresh_20260412_v5/water_followup_5554_v5.json)
    - answer stays on food-grade containers + FIFO rotation under `GD-252`
    - visible sources are `Hydration Assurance`, `Rotation Schedules`, and `Food Storage Fundamentals`
    - subtitle `49.9 s`
    - the huge wall-clock harness duration was a polling artifact; the in-app answer time remained about `50 s`
  - candle/tallow on `5560`:
    - [`../artifacts/bench/metadata_refresh_20260412_v5/candles_followup_5560_v5.json`](../artifacts/bench/metadata_refresh_20260412_v5/candles_followup_5560_v5.json)
    - still clean on `GD-486`
    - subtitle `8.2 s`

## Harness + Session Pass

- New Android follow-up harness tooling landed under:
  - [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1)
  - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
- Practical effect:
  - single-run wrapper now clears and captures per-run logcat automatically
  - matrix wrapper can run JSON/JSONL follow-up batches with a configurable parallel limit while preserving the existing script contract
- `run_android_detail_followup.ps1` was also hardened:
  - it no longer falls back immediately just because the accessibility dump failed to echo the typed follow-up text
  - it now trusts visible composer/send bounds as the primary in-detail signal
  - it only retries with `auto_followup` after the in-detail path fails to advance
  - output manifests now record `followup_submission_input_match` so echo failures are observable instead of inferred
- Retrieval/session fix landed in:
  - [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java)
  - read:
    - fresh context now stays ahead of recent-session carryover in `boostRecentContextMatches`
    - this fixed the house-drainage regression where fresh `GD-333` drainage context was being buried under older `GD-094` sections
- Current validated checkpoints:
  - house drainage on `5558`:
    - [`../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4e_harnessfix.json`](../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4e_harnessfix.json)
    - result:
      - subtitle `55.9 s`
      - `followup_submission_mode="in_detail_ui"`
      - `followup_submission_used_fallback=false`
      - answer is back on `GD-333` swale/drainage content
  - logcat-backed drainage confirmation on `5558`:
    - [`../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4d.logcat.txt`](../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4d.logcat.txt)
    - read:
      - fresh support candidate is still `GD-333`
      - anchor remains `GD-094`
      - final answer body uses `GD-333`, so the session merge is no longer burying the fresh drainage guide
  - candle/tallow on `5560`:
    - [`../artifacts/bench/metadata_refresh_20260412_v4/candles_followup_5560_v4b.json`](../artifacts/bench/metadata_refresh_20260412_v4/candles_followup_5560_v4b.json)
    - result:
      - subtitle `7.3 s`
      - single visible source remains `GD-486`
  - water `what next` on `5554`:
    - [`../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4d_noprobe.json`](../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4d_noprobe.json)
    - result:
      - subtitle `34.8 s`
      - chemical-storage drift is still gone
      - remaining weakness is section mix, not family routing:
        - answer stays on FIFO / generic storage language
        - visible sources still include `Food Storage Fundamentals`
- Strong future-direction note from the latest scout pass:
  - deterministic desktop/mobile parity should converge via a compiled shared payload exported from desktop and consumed by Android, rather than keeping separate deterministic brains alive indefinitely
- Additional harness + water follow-up notes:
  - the script-side follow-up fix was validated on a recent touchy house lane in:
    - [`../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4e_harnessfix.json`](../artifacts/bench/metadata_refresh_20260412_v4/house_followup_5558_v4e_harnessfix.json)
    - read:
      - `followup_submission_mode="in_detail_ui"`
      - `followup_submission_input_match=true`
      - `followup_submission_used_fallback=false`
      - drainage answer remains on `GD-333`
  - the new logged wrapper was validated in:
    - [`../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4e_logged.json`](../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4e_logged.json)
    - paired logcat:
      - [`../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4e_logged.logcat.txt`](../artifacts/bench/metadata_refresh_20260412_v4/water_followup_5554_v4e_logged.logcat.txt)
    - read:
      - per-run logcat capture is working end to end
      - visible post-answer source surface is cleaner again, collapsing back to a single `GD-252 / Water Storage: Hydration Assurance` button
      - remaining weakness is answer phrasing still drifting toward generic FIFO language (`all stored materials`) instead of staying tightly on water containers/rotation
- New tool now available for faster follow-up validation:
  - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
  - purpose:
    - run a JSONL follow-up suite through the detail-follow-up harness
    - capture per-case logcat automatically
    - emit JSONL + CSV summary rows for quick emulator-matrix review
  - smoke validation:
    - output folder: [`../artifacts/bench/followup_matrix_smoke_20260412`](../artifacts/bench/followup_matrix_smoke_20260412)
    - summary files:
      - [`../artifacts/bench/followup_matrix_smoke_20260412/followup_matrix_20260412_151653.jsonl`](../artifacts/bench/followup_matrix_smoke_20260412/followup_matrix_20260412_151653.jsonl)
      - [`../artifacts/bench/followup_matrix_smoke_20260412/followup_matrix_20260412_151653.csv`](../artifacts/bench/followup_matrix_smoke_20260412/followup_matrix_20260412_151653.csv)
    - paired case logcat:
      - [`../artifacts/bench/followup_matrix_smoke_20260412/water_soda_bottles_emulator_5554.logcat.txt`](../artifacts/bench/followup_matrix_smoke_20260412/water_soda_bottles_emulator_5554.logcat.txt)
    - read:
      - the deterministic bottles case passed
      - the new harness already removes one manual step from every guarded follow-up replay

## Structured State Checkpoint

- New machine-readable agent ledger is live at:
  - [`AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Validator is live at:
  - [`../scripts/validate_agent_state.py`](../scripts/validate_agent_state.py)
- Purpose:
  - keep emulator lanes, open scouts, active artifacts, and immediate next actions in strict YAML
  - let future agents and harness scripts read exact state without parsing the markdown notes heuristically
- Current use:
  - active continuation floor
  - live `5556` water-distribution replay status
  - immediate next actions and harness status without relying on prose parsing

## Water Distribution Specialization Checkpoint

- Timestamp:
  - `2026-04-12T16:27:19.5549817-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`](../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java)
  - tests:
    - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
    - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
    - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
- Validation:
  - `.\gradlew.bat testDebugUnitTest` passed in the main Android project after the specialization + SessionMemory alignment
  - the main-tree `assembleDebug` path is still file-locked on Windows, so the live replay used the alt-build shadow tree again
  - alt-build assemble passed from:
    - [`../artifacts/tmp/android-app-altbuild`](../artifacts/tmp/android-app-altbuild)
- Latest live replay:
  - query: `how do i design a gravity-fed water distribution system`
  - emulator: `emulator-5556`
  - artifacts:
    - [`../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_supportclean.xml`](../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_supportclean.xml)
    - [`../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_supportclean.logcat.txt`](../artifacts/bench/schema_split_live_20260412/distribution_prompt_5556_v7_supportclean.logcat.txt)
  - strongest reads:
    - `search.start` now logs `structure=water_distribution`
    - `routeSpecs=5`
    - `context anchorGuide="GD-270"`
    - host generation stayed on `gpu`
  - remaining gap:
    - the noisy `context.supportCandidates` preview dropped out on the latest replay, but the answer wording still leans a bit toward maintenance/failure language rather than a pure layout/build sequence

## Harness Integration Update

- The new pack hot-swap script is now callable inline from:
  - [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1)
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
- New flag:
  - `-PushPackDir <path-to-mobile_pack>`
- Verified smoke:
  - [`../artifacts/bench/harness_packpush_20260412/candles_5560_v1.xml`](../artifacts/bench/harness_packpush_20260412/candles_5560_v1.xml)
  - [`../artifacts/bench/harness_packpush_20260412/candles_5560_v1.logcat.txt`](../artifacts/bench/harness_packpush_20260412/candles_5560_v1.logcat.txt)
  - read:
    - the harness successfully pushed the pack, verified installed sizes, then launched the deterministic candle replay in one command

## New Water-Distribution Layout Pass

- Narrow Android tuning pass landed in:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
- Change intent:
  - explicit gravity-fed design prompts now prefer layout-focused sections like `Community Water Point Design` and `Distribution Network Layout`
  - `Common Mistakes in Water System Design` is no longer promoted into the first-turn answer window for the base design query
  - explicit water-distribution prompt guidance now stresses `head pressure`, `main line layout`, `storage/pressure`, and `outlets/limits`
  - explicit water-distribution prompts now use `4` context notes and a larger excerpt budget
- Validation:
  - `android-app` `testDebugUnitTest` passed
  - alt-build `testDebugUnitTest assembleDebug` passed
  - rebuilt alt-build APK installed successfully across `5554`, `5556`, `5558`, and `5560`
- New primary checkpoint:
  - [`../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.xml`](../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.xml)
  - [`../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.logcat.txt`](../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v1.logcat.txt)
  - read:
    - `context.selected` is now `GD-270` sections `1`, `2`, `5`, and `4`
    - `Common Mistakes` dropped out
    - visible answer now says `head pressure`, `main line`, `elevated storage/water tower`, and `community water points`

## New Distribution Follow-Up Confirmation

- Follow-up checkpoint:
  - [`../artifacts/bench/distribution_followup_20260412/designpass.json`](../artifacts/bench/distribution_followup_20260412/designpass.json)
- Read:
  - `how do i design a gravity-fed water distribution system -> what about storage tanks`
  - stayed on `GD-270`
  - source buttons verified `2. Water Tower Construction & Sizing`, `1. Gravity-Fed Water Distribution Design`, and `5. Community Water Point Design`
  - source-link probe succeeded

## New Broad-House Route Cleanup

- Narrow route-filter pass landed in:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
- Change intent:
  - broad-house first turns now hard-reject the `Outbuildings for Off-Grid Living` family at the route-filter stage
- Validation:
  - `android-app` `testDebugUnitTest` passed
  - alt-build `testDebugUnitTest assembleDebug` passed
  - rebuilt alt-build APK installed on `5558`
- New checkpoint:
  - [`../artifacts/bench/house_routecleanup_20260412/house_5558_v1.xml`](../artifacts/bench/house_routecleanup_20260412/house_5558_v1.xml)
  - [`../artifacts/bench/house_routecleanup_20260412/house_5558_v1.logcat.txt`](../artifacts/bench/house_routecleanup_20260412/house_5558_v1.logcat.txt)
  - read:
    - `Outbuildings for Off-Grid Living` dropped out of `search.topResults`
    - `context.selected` now starts with `GD-094 :: Structural Engineering Basics for Off-Grid Builders`
    - visible answer still stays on site, drainage/foundation, walls/roof, and weatherproofing

## New Continuation Check

- current check captured at `2026-04-12T16:56:37.6994015-05:00`
- the active continuation floor remains `2026-04-12T20:36:45.8552703-05:00`
- still actively within the required continuation window

## New House Foundation Anchor Checkpoint

- Timestamp:
  - `2026-04-12T17:23:21.3516190-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - tests:
    - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
    - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
- Change intent:
  - generic house prompts now penalize broad structural-overview sections harder
  - frost-line / footing sections get recognized as foundation-adjacent for generic house routing
  - broad-house route-anchor scoring now adds an extra preference for positive section-heading bonuses, so foundation/site-prep sections can beat generic overviews
- Validation:
  - `android-app` `testDebugUnitTest` passed after the new anchor regression test was added
  - alt-build `testDebugUnitTest assembleDebug` passed
  - rebuilt alt-build APK installed on `5558`, `5556`, and `5554`
- New primary checkpoint:
  - [`../artifacts/bench/house_routecleanup_20260412/house_5558_v3.xml`](../artifacts/bench/house_routecleanup_20260412/house_5558_v3.xml)
  - [`../artifacts/bench/house_routecleanup_20260412/house_5558_v3.logcat.txt`](../artifacts/bench/house_routecleanup_20260412/house_5558_v3.logcat.txt)
  - read:
    - `search.topResults` now starts with `GD-383 :: Frost Line and Frost Heave`
    - `context.selected` now stays entirely on `GD-383` foundation sections
    - `anchorGuide="GD-383"`
    - visible answer is now foundation-first instead of generic carpentry-first
    - total detail subtitle time is `95.4 s`
- Guardrails from the same replay wave:
  - [`../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v3.xml`](../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v3.xml)
  - [`../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v3.logcat.txt`](../artifacts/bench/distribution_tuning_20260412/distribution_5556_designprompt_v3.logcat.txt)
  - read:
    - explicit gravity-fed distribution stayed anchored on `GD-270`
    - `context.selected` still uses the good `GD-270` design / tower / water-point / layout mix
  - [`../artifacts/bench/distribution_tuning_20260412/water_5554_guard_v1.xml`](../artifacts/bench/distribution_tuning_20260412/water_5554_guard_v1.xml)
  - [`../artifacts/bench/distribution_tuning_20260412/water_5554_guard_v1.logcat.txt`](../artifacts/bench/distribution_tuning_20260412/water_5554_guard_v1.logcat.txt)
  - read:
    - water storage stayed anchored on `GD-252`
    - the lane still leaks bad support/context sections like `Root Cellars` and `Canning`, so water remains the next cleanup target

## New Headless Answer Harness

- New script:
  - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py)
- Scope of the first landed slice:
  - off-emulator mobile-pack retrieval
  - context selection
  - prompt building
  - optional host generation
  - `--force-no-fts` to mimic the current Android runtime fallback path
- Verified smoke:
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_20260412_172309.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_20260412_172309.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_20260412_172308.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_20260412_172308.json)
- Read:
  - `how do i build a house` headless smoke completed and emitted ranked results, selected context, and the built prompt
  - `how do i design a gravity-fed water distribution system` headless smoke completed and top-contexted on `GD-270`
  - this is not yet full vector-perfect Android parity, but it is now a usable off-emulator lane for faster retrieval/prompt iteration

## New Headless Scorer Sync

- Timestamp:
  - `2026-04-12T17:33:07.5977384-05:00`
- Files updated for this slice:
  - [`../scripts/mobile_headless_parity.py`](../scripts/mobile_headless_parity.py)
  - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py)
- Change intent:
  - sync more of the Android-style section scoring into the off-emulator parity lane so water-storage and water-distribution stop selecting obviously wrong support sections
  - add a narrow broad-house priority bonus in the headless harness so first-turn `build a house` answers prefer foundation/site/drainage sections over insulation and roof-first drift
  - keep the headless lane deliberately strict enough to catch bad overcorrections before spending emulator time on them
- Read from the intermediate failure:
  - the first pass cleaned water but briefly overcorrected broad-house into `GD-106 Floor & Foundation Insulation`
  - that regression was caught immediately in the headless artifact and then fixed in the next scorer pass
- New primary checkpoints:
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_878475.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_878475.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_544618.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_544618.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_581590.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_581590.json)
- Read:
  - `how do i build a house` now top-contexts on `GD-383 / Stone Foundation Construction`, `Drainage and Waterproofing`, and `Foundation Repair Techniques`, with `GD-094 Foundations` as supporting route context
  - `what's the safest way to store treated water long term` now top-contexts on `GD-252 / Water Storage: Hydration Assurance`, then rotation-schedule sections, with `Chemical Storage` removed from the top window
  - `how do i design a gravity-fed water distribution system` now top-contexts on `GD-270 / Gravity-Fed Water Distribution Design`, `Distribution Network Layout`, and `Community Water Point Design`, with `Common Mistakes` removed from the top window
  - the new headless lane is now strong enough to catch ranking regressions quickly before guarded emulator replay

## New Live Metadata Patch Replay

- Timestamp:
  - `2026-04-12T17:45:58.6610000-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - tests:
    - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
    - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
- Change intent:
  - strengthen explicit water-distribution section preference for layout/network-oriented notes over troubleshooting-style sections
  - penalize water-storage distractors like `Pest Prevention`, `Ammunition Storage`, and similar non-potable storage sections more directly
  - stop negative-scored same-guide sections from filling the `GD-252` context window just because they share the anchor guide
- Validation:
  - `android-app` `testDebugUnitTest` passed after each narrow scoring/context patch
  - alt-build `testDebugUnitTest assembleDebug` passed
  - rebuilt alt-build APK installed successfully across the emulator grid, then `5554` was replayed again after the guide-section context gate landed
- Live read:
  - [`../artifacts/bench/live_metadata_patch_20260412/distribution_5556_v4.logcat.txt`](../artifacts/bench/live_metadata_patch_20260412/distribution_5556_v4.logcat.txt)
  - `GD-270` stayed anchored on the explicit gravity-fed distribution query and selected `1`, `2`, `5`, and `4`, with `Common Mistakes` still out of the answer window
  - [`../artifacts/bench/live_metadata_patch_20260412/water_5554_v4.logcat.txt`](../artifacts/bench/live_metadata_patch_20260412/water_5554_v4.logcat.txt)
  - [`../artifacts/bench/live_metadata_patch_20260412/water_5554_v4.xml`](../artifacts/bench/live_metadata_patch_20260412/water_5554_v4.xml)
  - `GD-252` stayed anchored for the treated-water prompt, and the selected context improved from `Hydration + Rotation + Chemical Storage` to `Hydration + Rotation + Seasonal Rotation + Failure Modes & Recovery`
  - that means `Chemical Storage` and `Ammunition Storage` are now out of the selected live context, but the remaining last-slot leak is `Failure Modes & Recovery`

## New Continuation Reset And Clay-Oven Fix

- New continuation floor:
  - `2026-04-12T17:48:45.5458549-05:00`
- New continue-until target:
  - `2026-04-12T21:48:45.5458549-05:00`
- Timestamp for this checkpoint:
  - `2026-04-12T17:52:05.0065612-05:00`
- Files updated for this slice:
  - [`../scripts/mobile_headless_parity.py`](../scripts/mobile_headless_parity.py)
  - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - add a dedicated headless `clay_oven` route so clay/cob/bread oven prompts stop falling through to default lexical noise
  - keep the fix metadata/routing-based instead of hardcoding guide IDs
  - preserve the improved site/foundation and roof-waterproof headless behavior while adding the new lane
- New primary checkpoints:
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_216672.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_216672.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_329196.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_329196.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_401006.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_401006.json)
- Read:
  - `how do i build a clay oven` now routes as `clay_oven` and top-contexts on `GD-505 / Brick Bread Oven`, `Oven Types Comparison`, and `Cob Oven Construction`
  - `how do i choose a safe site and foundation for a small cabin` still lands on `GD-383`
  - `how do i waterproof a roof with no tar or shingles` still lands on `GD-094`
  - the next exposed family gap after this pass is no longer construction breadth; it is the governance/security default lane from the wider headless batch

## New Governance / Security Headless Route Families

- Timestamp:
  - `2026-04-12T17:57:42.4734391-05:00`
- Files updated for this slice:
  - [`../scripts/mobile_headless_parity.py`](../scripts/mobile_headless_parity.py)
  - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py)
  - [`../tests/test_mobile_headless_parity.py`](../tests/test_mobile_headless_parity.py)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - add family-level headless routing for `community_security` and `community_governance` instead of letting those prompts fall through to `default`
  - keep the fix metadata/category/section-based, not guide-id hardcoded
  - trust the already-ranked route `support_score` during anchor selection so governance prompts stop flipping back to worse anchors from clipped previews
- Validation:
  - `python -m unittest tests.test_mobile_headless_parity -v` passed
  - direct headless replays:
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_540004.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_540004.json)
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_660817.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_660817.json)
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_717564.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_717564.json)
  - wider family sweep:
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_comms_governance_core_20260412_20260412_175727_595793.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_comms_governance_core_20260412_20260412_175727_595793.json)
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_construction_core_20260412_20260412_175728_484329.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_construction_core_20260412_20260412_175728_484329.json)
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_water_core_20260412_20260412_175727_135507.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_water_core_20260412_20260412_175727_135507.json)
- Read:
  - `protect a vulnerable work site, field, or water point` now top-contexts on `GD-651 / Critical Infrastructure Physical Security`
  - `someone is stealing food from the group what do we do` now top-contexts on `GD-626 / Commons Management & Sustainable Resource Governance`
  - `two groups of survivors want to merge but dont trust each other` is at least in-family now, but still leans on `GD-865 / Insurance, Risk Pooling & Mutual Aid Funds`
  - clay-oven stays fixed on `GD-505`
  - courier auth stays on `GD-389`, fair trial stays on `GD-197`, and the water family stayed clean in the same sweep

## Update After Water Tie-Breaker Validation Prep

- New continuation floor:
  - `2026-04-12T19:42:30.7848808-05:00`
- New continue-until target:
  - `2026-04-12T23:42:30.7848808-05:00`
- Timestamp:
  - `2026-04-12T19:42:30.7848808-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - keep the new broad-water route refinement bonus in place while validating it on a quieter emulator lane
  - demote broad `inventory` water rows when topic overlap is weak and boost planning/subsystem water guides when topic overlap is strong
  - keep the testing loop concurrent by running the exact Android replay in the background while docs and scout management continue
- Validation status:
  - targeted `PackRepository` regression tests for the new water refinement bonus already pass
  - exact-query Android replay started on `emulator-5560` via [`../scripts/run_android_search_log_only.ps1`](../scripts/run_android_search_log_only.ps1)
  - pending artifact target:
    - [`../artifacts/bench/android_packrefresh_v5_20260412/water_5560_logonly_v2.logcat.txt`](../artifacts/bench/android_packrefresh_v5_20260412/water_5560_logonly_v2.logcat.txt)
- Read:
  - the highest-leverage remaining water knob is still route ordering, not route classification
  - the best current plan is to validate the runtime tie-breaker before doing another metadata or anchor-selection pass

## Update After Pack V7 Water Metadata Cleanup And Live Replay

- Timestamp:
  - `2026-04-12T19:57:47.1990000-05:00`
- Files updated for this slice:
  - [`../mobile_pack.py`](../mobile_pack.py)
  - [`../tests/test_mobile_pack.py`](../tests/test_mobile_pack.py)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - fix the real pack-builder root cause behind stray `GD-373` `water_storage` rows instead of only piling on Android rerank heuristics
  - make `water_storage` guide inheritance require either explicit core/heading focus or stronger body evidence, so `tank` / `barrel` / `food-grade` mentions in general inventory sections stop leaking into water metadata
  - keep the Android-side broad-water ordering bonus in place, then validate the cleaned pack and rerank together on a real emulator
- Validation:
  - `python -m unittest tests.test_mobile_pack -v` passed after the new real `Home Inventory` regressions were added
  - `GD-373` `water_storage` chunk count dropped:
    - `v5`: `14`
    - `v6`: `11`
    - `v7`: `8`
  - clean v7 replay:
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_5560_logonly_v4.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/water_5560_logonly_v4.logcat.txt)
- Read:
  - the cleaned pack plus the latest rerank finally moved the exact Android water-storage search off the broad inventory row: `GD-417` now ranks first, ahead of `GD-373`
  - the same replay cut route search to about `35.5 s` and total search to about `45.3 s`
  - the next required check is answer-path quality, not just ranked search order: verify the actual offline answer/source chips use the cleaner specialized lane

## Update After Water Answer-Anchor Fix Landed Live

- Timestamp:
  - `2026-04-12T20:21:31.2810000-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - stop broad water-storage answers from anchoring on `GD-373` just because the route-focused inventory section has strong lexical overlap
  - prefer a non-inventory water-storage anchor when a viable specialized guide exists, and reject support guides that only overlap via `container_sanitation`
  - keep the fix metadata/role/title-based rather than hardcoding guide ids into the app logic
- Validation:
  - targeted Android unit suite passed after each anchor/support pass:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.PackRepositoryTest --tests com.senku.mobile.QueryRouteProfileTest --tests com.senku.mobile.QueryMetadataProfileTest`
  - final live answer-path replay:
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v6_anchorfix3.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v6_anchorfix3.logcat.txt)
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v6_anchorfix3.xml`](../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v6_anchorfix3.xml)
- Read:
  - the live Android answer now anchors on `GD-252` instead of `GD-373`
  - the detail screen source chip now shows `[GD-252] Storage & Material Management / Water Storage: Hydration Assurance`
  - the earlier bogus waterproofing/sealants warning is gone; the answer now stays on food-safe containers, sanitation, and rotation
  - raw search order is still not fully aligned with answer order because `search.topResults` still opens with `GD-417`, so the next water pass should focus on first-turn follow-up behavior and, later, raw search alignment

## Update After Governance Anchor Fix And Follow-Up Harness Guard

- Timestamp:
  - `2026-04-12T20:43:14.0941100-05:00`
- Files updated for this slice:
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - stop the detail follow-up harness from accepting arbitrary guide-note body changes as a completed second turn
  - bias specialized community-governance anchor selection away from finance-oriented `GD-865` sections for merge-and-trust prompts and back toward `GD-626`
  - keep shipping testable Android builds through the shadow alt-build tree instead of fighting the locked main-tree APK output
- Validation:
  - harness/script syntax validated:
    - `python scripts/validate_agent_state.py`
    - PowerShell parser check for [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - Android unit slice passed after the governance-anchor patch:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.PackRepositoryTest --tests com.senku.mobile.QueryMetadataProfileTest`
    - shadow-tree repeat:
      - [`../artifacts/tmp/android-app-altbuild`](../artifacts/tmp/android-app-altbuild)
  - live Android governance replay on `emulator-5558`:
    - [`../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2.logcat.txt)
    - [`../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2_manual.xml`](../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2_manual.xml)
- Read:
  - the merge-and-trust Android lane now anchors on `GD-626` live, with `context.selected` showing `GD-626` first instead of the old `GD-865` mutual-aid-fund lead
  - the answer finishes in about `39.7 s` on host GPU and the detail view remains on the expected question title with the in-thread composer visible
  - quality is improved but still not ideal: the current support mix includes `GD-385` and `GD-657`, so the answer is more governance-aligned than before but still not fully trust-repair-specific
  - the detail follow-up harness is partially improved: it no longer false-positives on guide-note body changes, but the newest water replay still exposes a separate initial-detail acquisition bug that can leave the app on the main-screen host-generation view instead of a stable detail-screen capture

## Update After Headless Governance Parity Fix And Prompt-Harness Hardening

- Timestamp:
  - `2026-04-12T20:51:53.5806806-05:00`
- Files updated for this slice:
  - [`../scripts/mobile_headless_parity.py`](../scripts/mobile_headless_parity.py)
  - [`../tests/test_mobile_headless_parity.py`](../tests/test_mobile_headless_parity.py)
  - [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1)
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - bring the off-emulator governance parity lane back in line with Android by penalizing finance-oriented `GD-865` sections for trust-repair / merge prompts
  - harden the initial Android ask capture by reusing the quieter `uiautomator dump` path already proven in the follow-up harness
  - stop the follow-up harness from relaunching the same initial ask when MainActivity is simply still busy generating the first answer
- Validation:
  - headless parity regression:
    - `python -m unittest tests.test_mobile_headless_parity -v`
  - exact headless governance replay after the fix:
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_204620_734776.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_204620_734776.json)
  - fresh Android search-only scouts:
    - [`../artifacts/bench/android_packrefresh_v7_20260412/cabin_site_5554_logonly_v1.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/cabin_site_5554_logonly_v1.logcat.txt)
    - [`../artifacts/bench/android_packrefresh_v7_20260412/roof_waterproof_5556_logonly_v1.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/roof_waterproof_5556_logonly_v1.logcat.txt)
  - pack hot-swap refresh on `emulator-5560`:
    - `powershell -ExecutionPolicy Bypass -File scripts/push_mobile_pack_to_android.ps1 -Device emulator-5560 -PackDir artifacts/pack_refresh_community_20260412_v7 -ForceStop`
- Read:
  - the exact merge-and-trust headless prompt now top-contexts on `GD-626` instead of `GD-865`, which makes the fast lane trustworthy again for that family
  - cabin site/foundation search on Android is still construction-family correct, but raw ranking stays too `GD-094`-heavy and does not surface `GD-446` early enough
  - roof waterproofing search on Android is still construction-family correct, but the answer-context lane is likely to admit `GD-094` calculator noise unless section scoring tightens further
  - the water follow-up harness bug is now more clearly understood: the worst waste was duplicate first-turn launches from a busy main-screen state, not a broken second-turn retrieval path

## Update After Water Prompt Harness Recovery On 5560

- Timestamp:
  - `2026-04-12T20:56:26.9053896-05:00`
- Files updated for this slice:
  - [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1)
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - stop `run_android_prompt.ps1` from accepting the MainActivity status panel as a completed answer when `-Ask` expects a detail screen
  - remove temp-file collisions between overlapping harness runs by switching prompt/follow-up temp dumps to unique per-run names and treating local dump cleanup as best-effort
  - re-establish one clean 5560 first-turn water checkpoint before trusting the next follow-up replay
- Validation:
  - PowerShell parser check:
    - [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1)
    - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - Android unit slice still green after the surrounding retrieval changes:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.PackRepositoryTest --tests com.senku.mobile.QueryRouteProfileTest --tests com.senku.mobile.QueryMetadataProfileTest`
  - live background 5560 prompt replay:
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v16_bg.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v16_bg.logcat.txt)
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v16_bg.xml`](../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v16_bg.xml)
    - [`../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v16_bg.stdout.log`](../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v16_bg.stdout.log)
- Read:
  - the recovered 5560 prompt lane now lands on the expected detail screen again with `You asked`, `Senku answered`, the `Host answer | ... | gpu | 98.2 s` subtitle, and the inline follow-up composer visible
  - logcat still confirms the improved water path end to end:
    - raw ranking still starts `GD-417 / GD-373 / GD-457`
    - `context.selected` still picks `GD-252` first
    - `anchorGuide="GD-252"`
  - the visible source chip is still `[GD-252] Storage & Material Management / Water Storage: Hydration Assurance`
  - the second-turn water replay is still the remaining proof point; first-turn capture is back under control

## Update After Android Site-Selection FTS Fallback Repair

- Timestamp:
  - `2026-04-13T00:31:58.3882526-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`](../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - stop broad Android site-selection from wasting no-`bm25` route time on a useless raw-query spec and row-order FTS junk
  - prioritize `GD-446`-style siting sections in the Android no-`bm25` FTS fallback using a site-selection-specific joined-column `ORDER BY`
  - keep short siting follow-ups from collapsing into `drainage runoff cabin house` by adding preferred siting/foundation topic hints before generic `cabin house` structure hints
- Validation:
  - Android JVM guards passed after the route/SessionMemory patch:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryRouteProfileTest --tests com.senku.mobile.PackRepositoryTest --tests com.senku.mobile.SessionMemoryTest`
  - broad site search-only replay on `emulator-5554`:
    - [`../artifacts/bench/live_refresh_v7_20260413/site_5554_v1.logcat.txt`](../artifacts/bench/live_refresh_v7_20260413/site_5554_v1.logcat.txt)
  - broad site full answer replay on `emulator-5554`:
    - [`../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.xml`](../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.xml)
    - [`../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.logcat.txt`](../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.logcat.txt)
  - small-cabin siting follow-up replay on `emulator-5560`:
    - [`../artifacts/bench/detail_refresh_v7_20260413/site_foundation_5560_v3.json`](../artifacts/bench/detail_refresh_v7_20260413/site_foundation_5560_v3.json)
    - [`../artifacts/bench/detail_refresh_v7_20260413/site_foundation_5560_v3_followup.xml`](../artifacts/bench/detail_refresh_v7_20260413/site_foundation_5560_v3_followup.xml)
- Read:
  - broad site on `5554` is the real retrieval win:
    - `routeSpecs=2`
    - `routeChunkFts` now runs with `order=site_selection_priority`
    - `search.topResults` is led by `GD-446` guide-focus plus `Terrain Analysis`, `Wind Exposure and Microclimate`, `Identifying Natural Hazards`, and `Water Proximity and Quality`
  - broad site full-answer path now benefits from that retrieval fix too:
    - `context.selected` is entirely `GD-446`
    - `anchorGuide="GD-446"`
    - host generation completed on `backend=gpu` in about `4.0 s`
    - total ask time was about `79.4 s`
  - the repaired small-cabin follow-up now stays in the siting/foundation lane instead of collapsing to a generic cabin-house query:
    - search query became `drainage runoff foundation site selection cabin house`
    - `routeSpecs=3`
    - `order=site_selection_priority`
    - `anchorGuide="GD-446"`
    - total follow-up answer time was about `55.7 s`
  - small-cabin drainage/runoff quality is improved structurally, but the answer body still leans on `GD-593` drainage assessment more than I want; the next quality pass should tighten the support mix once the siting anchor is already correct

## Update After Broad-Site Winter-Sun Follow-Up Repair

- Timestamp:
  - `2026-04-13T00:47:35.6241188-05:00`
- Validation:
  - `.\gradlew.bat assembleDebug`
  - reinstall to `emulator-5554`
  - broad-site follow-up replay:
    - [`../artifacts/bench/detail_refresh_v7_20260413/site_choice_5554_v2.json`](../artifacts/bench/detail_refresh_v7_20260413/site_choice_5554_v2.json)
  - live logcat confirmation from `emulator-5554`
- Read:
  - the old broad-site second-turn collapse is gone:
    - old bad query: `What about winter sun and summer shade?`
    - repaired query: `winter sun summer shade site selection foundation cabin house`
  - the repaired follow-up now stays on the intended route:
    - `routeFocused=true`
    - `routeSpecs=3`
    - `order=site_selection_priority`
    - `anchorGuide="GD-446"`
  - the repaired `search.topResults` are now in-family:
    - `GD-446 :: Terrain Analysis`
    - `GD-094 :: Foundations`
    - `GD-383 :: Stone Foundation Construction`
    - `GD-593 :: Foundation Selection by Soil Type`
  - the user-visible answer and sources are now aligned with the repaired retrieval path:
    - answer body is a siting-focused response sourced from `GD-446`
    - source buttons show `GD-446`, `GD-094`, and `GD-383`
    - the old `GD-064` agriculture leak is gone from the visible source list
  - remaining issue:
    - the source-link probe still does not open the expected guide detail view after tapping the correct source button, so source navigation is now the next concrete Android UI bug

## Update After Manual Auto-Followup Contract Validation

- Timestamp:
  - `2026-04-13T01:16:06.6637263-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/MainActivity.java`](../android-app/app/src/main/java/com/senku/mobile/MainActivity.java)
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Validation:
  - manual adb auto-followup replay on `emulator-5554`:
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554.xml`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554.xml)
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_b.xml`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_b.xml)
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_d.xml`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_d.xml)
  - live logcat confirmation from `emulator-5554`
- Read:
  - the app-side automation contract is now clean again:
    - `MainActivity` no longer steals focus into the main search box during automation-driven second turns
    - the initial broad-site query stays isolated in `search_input`
    - the second-turn text lives only in `detail_followup_input`
  - the repaired broad-site seasonal follow-up now launches with the stronger query:
    - `winter sun summer shade site selection wind exposure microclimate cabin`
  - retrieval is materially better than the visible answer body:
    - `search.topResults` stays on `GD-446`
    - `context.selected` is led by `GD-446 :: Wind Exposure and Microclimate`
    - the final selected context is `Wind Exposure and Microclimate`, `Terrain Analysis`, `Site Assessment Checklist`, and `Seasonal Considerations`
  - the completed manual second turn finished on host `gpu` in about `111.0 s` and kept the user in the in-detail thread instead of bouncing back to the search screen
  - remaining quality gap:
    - the final answer body is still too generic and terrain-heavy for a prompt explicitly asking about winter sun and summer shade, so answer shaping is still lagging behind the repaired retrieval/context path

## Update After Headless Scout Reality Check

- Timestamp:
  - `2026-04-13T01:16:06.6637263-05:00`
- Validation:
  - targeted headless batch:
    - [`../artifacts/bench/headless_live_20260413/mobile_headless_answers_headless_gap_scout_20260412_20260413_010941_281153.json`](../artifacts/bench/headless_live_20260413/mobile_headless_answers_headless_gap_scout_20260412_20260413_010941_281153.json)
- Read:
  - the headless answer runner is useful as a scout lane, not a full Android oracle
  - it correctly exposed:
    - soapmaking still anchoring on `GD-262` instead of dedicated soapmaking guides
    - a broad-site parity gap where headless still drifts more than live Android
  - it also confirmed some lanes are back in-family:
    - message-auth / courier trust now stays on `GD-389`
    - roof waterproofing is at least landing in the weatherproofing family instead of total junk
  - practical takeaway:
    - keep using headless first for broad scouting and metadata/ranking work
    - continue treating guarded emulator replays as the truth lane for final Android-parity claims

## Update After Manual Auto-Followup Contract Cleanup

- Timestamp:
  - `2026-04-13T01:14:41.7102940-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/MainActivity.java`](../android-app/app/src/main/java/com/senku/mobile/MainActivity.java)
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - stop automation-driven follow-up text from landing in the wrong `EditText`
  - make the detail follow-up harness re-dump focused UI state before clearing or matching inline text
  - prove the repaired app/harness contract with a direct `adb am start ... --es auto_query ... --es auto_followup_query ...` run instead of relying only on the heavier wrapper
- Validation:
  - manual `adb` auto-followup artifacts on `emulator-5554`:
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554.xml`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554.xml)
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_b.xml`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_b.xml)
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_d.xml`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_d.xml)
  - live logcat confirmation from `emulator-5554`
- Read:
  - the app-side contract is clean again:
    - `MainActivity` search input kept only the initial broad-site query
    - `DetailActivity` held the second-turn text in `detail_followup_input`
    - the old concatenated-input corruption is gone
  - the repaired second turn now uses the intended retrieval query:
    - `winter sun summer shade site selection wind exposure microclimate cabin`
    - `routeFocused=true`
    - `order=site_selection_microclimate_priority`
    - `anchorGuide="GD-446"`
  - the repaired follow-up context is materially better than the older foundation-heavy version:
    - `GD-446 :: Wind Exposure and Microclimate`
    - `GD-446 :: Terrain Analysis`
    - `GD-446 :: Site Assessment Checklist`
    - `GD-446 :: Seasonal Considerations`
  - the completed second-turn UI confirms host GPU generation is still the active path:
    - subtitle: `Host answer | gemma-4-e2b-it-litert @ 10.0.2.2:1235 | gpu | 111.0 s`
  - remaining gap:
    - the answer body is still too generic and terrain/drainage-heavy for a `winter sun / summer shade` question even though retrieval/context is now clearly microclimate-aware

## Update After Seasonal Site Prompt-Shaping Replay

- Timestamp:
  - `2026-04-13T01:26:19.1727309-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`](../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java`](../android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - let follow-up prompt shaping inherit the richer `contextSelectionQuery` instead of routing only on the vague raw follow-up text
  - keep the fourth `Seasonal Considerations` note in the prompt for seasonal siting follow-ups
  - explicitly tell the model to answer winter solar gain / summer shade / wind exposure tradeoffs first when that is what the follow-up is asking
- Validation:
  - focused Android JVM guard:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryRouteProfileTest --tests com.senku.mobile.PromptBuilderTest --tests com.senku.mobile.OfflineAnswerEngineTest`
  - `.\gradlew.bat assembleDebug`
  - rebuilt APK replayed manually on `emulator-5554` with encoded automation extras
  - final artifacts:
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_f.xml`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_f.xml)
    - [`../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_f.log.txt`](../artifacts/bench/manual_auto_followup_20260413/manual_auto_followup_5554_f.log.txt)
- Read:
  - the repaired second-turn retrieval stayed correct:
    - `context.selected` remained `GD-446 :: Wind Exposure and Microclimate`, `Terrain Analysis`, `Site Assessment Checklist`, `Seasonal Considerations`
    - `anchorGuide="GD-446"`
  - the prompt path is now visibly different:
    - `ask.prompt ... promptContext=4`
    - the prior run was still at `promptContext=3`
  - the user-visible answer improved materially:
    - it now explicitly says site selection must evaluate year-round conditions
    - it now tells the user to assess orientation for winter solar gain and summer shade
    - it still keeps wind exposure and seasonal hazards in-frame
  - current result:
    - host GPU subtitle: `Host answer | gemma-4-e2b-it-litert @ 10.0.2.2:1235 | gpu | 115.2 s`
    - this is slower than the earlier `111.0 s` replay but much better in answer quality, and the delta is retrieval-dominated rather than generation-dominated

## Update After External Audit Verification Pass

- Timestamp:
  - `2026-04-13T01:49:21.4153745-05:00`
- Change intent:
  - verify an external Android audit claim-by-claim before taking it as roadmap truth
  - separate current real bugs from stale claims that the repo already moved past
  - record the next patch targets so the follow-on work stays cohesive
- Confirmed:
  - `PackRepository.java` still drops `routeResults` on the `centroid == null` path instead of preserving them through `mergePreferredResults`
  - `QueryMetadataProfile.java` still treats `storage tank` as a water-distribution structure marker, which can bias storage queries toward the wrong lane
  - `PromptBuilder.java` still forces the same `Short answer / Steps / Limits or safety` format for every query family
  - `PromptBuilder.java` still emits thin chunk headers and clips excerpts with a raw substring rather than a sentence-aware boundary
  - mobile inference still uses a single prompt payload with no actual system-role message:
    - `HostInferenceClient.java` sends only a `user` message
    - `LiteRtModelRunner.java` creates the conversation without a system prompt
  - `SessionMemory.java` still has the broad singularize/prefix matching issue (`glass -> glas`, prefix-style false positives)
- Stale or overstated:
  - the older `FTS hit prevents LIKE fallback` claim is stale because the current route collectors now use `shouldBackfillLikeAfterFts(...)`
  - the older `noBm25RouteFtsOrder only handles house/site_selection` claim is stale because soapmaking now has its own no-bm25 ordering branch too
  - the `0-token query becomes focused house follow-up` claim is false in current code; `isFocusedHouseFollowUp(...)` exits early for `directTokenCount <= 0`
- Practical read:
  - the highest-value next fixes from that audit are still real:
    - preserve route results when centroid build fails
    - tighten water-storage vs water-distribution structure markers
    - improve the mobile prompt contract instead of treating every question as the same 3-section answer
  - the external audit is useful, but it should be treated as a lagging review snapshot rather than the live source of truth

## Update After Verified Audit Fixes And Host-System Prompt Smoke

- Timestamp:
  - `2026-04-13T01:58:40.6640000-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/HostInferenceClient.java`](../android-app/app/src/main/java/com/senku/mobile/HostInferenceClient.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`](../android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`](../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`](../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java`](../android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - land the small, verified fixes from the audit instead of acting on stale findings
  - restore the verification lane after compile/test drift surfaced during the audit pass
  - give host inference a real system-role payload while keeping the Android prompt contract stable
- What landed:
  - fixed a small compile break in `PackRepository` by making `buildGuideSectionKey(...)` static where the new centroid-missing merge helper uses it
  - fixed missing `assertFalse` / `assertEquals` imports in the Android JVM test suite so the audit verification lane runs cleanly again
  - tightened `SessionMemory` token matching:
    - `glass` / `stress` no longer singularize incorrectly
    - broad prefix collisions like `winter` vs `win` are rejected
    - useful bounded matches like `seal` / `sealing` and `drain` / `drainage` are still allowed
  - added `PromptBuilder.buildOfflineAnswerSystemPrompt(...)` and switched `HostInferenceClient` to send a real `system` message before the user prompt
- Validation:
  - targeted JVM regression pass:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.PromptBuilderTest --tests com.senku.mobile.SessionMemoryTest --tests com.senku.mobile.QueryMetadataProfileTest --tests com.senku.mobile.QueryRouteProfileTest --tests com.senku.mobile.PackRepositoryTest`
  - `.\gradlew.bat assembleDebug`
  - live host smoke on `emulator-5556`:
    - query: `what's the safest way to store treated water long term`
    - log artifact: [`../artifacts/bench/live_refresh_v8_20260413/water_5556_systemprompt_smoke.logcat.txt`](../artifacts/bench/live_refresh_v8_20260413/water_5556_systemprompt_smoke.logcat.txt)
- Read:
  - the new host request now carries a real system payload:
    - `host.request start ... systemChars=866 promptChars=2600`
  - the path still uses host GPU successfully:
    - `host.response parsed ... backend=gpu elapsedSeconds=4.441`
  - the water-storage smoke still completed end to end after retrieval/context work:
    - `ask.generate ... totalElapsedMs=95976`
- Practical read:
  - the audit’s prompt-gap claim now has a real first fix on the board
  - the next prompt-side win is route-conditional answer formatting rather than one rigid `Short answer / Steps / Limits or safety` template
  - the next retrieval-side cleanup is still shared marker drift, especially water-storage vs water-distribution wording

## Update After Desktop Tooling Hardening Pass

- Timestamp:
  - `2026-04-13T02:17:00.0000000-05:00`
- Files updated for this slice:
  - [`../guide_catalog.py`](../guide_catalog.py)
  - [`../bench_artifact_tools.py`](../bench_artifact_tools.py)
  - [`../tests/test_guide_catalog.py`](../tests/test_guide_catalog.py)
  - [`../tests/test_bench_artifact_tools.py`](../tests/test_bench_artifact_tools.py)
- Change intent:
  - harden desktop validation/tooling paths surfaced by the second external audit
  - stop silent corruption in guide lookup and bench-artifact comparison flows
- What landed:
  - `guide_catalog.py` now:
    - ignores non-mapping YAML frontmatter instead of assuming every parsed YAML object supports `.get(...)`
    - raises clear errors on duplicate guide `id` / `slug` instead of silently letting the last file win
  - `bench_artifact_tools.py` now:
    - supports standalone markdown bench artifacts instead of falling through to `json.loads()` on raw `.md`
    - preserves duplicate prompts during artifact comparison by including `index` in the row key
- Validation:
  - `python -m unittest tests.test_bench_artifact_tools tests.test_guide_catalog -v`
- Practical read:
  - citation-validation tooling is less fragile now
  - bench comparisons are less likely to silently lie when prompt packs intentionally repeat a question inside one section

## Update After Query Guardrails And Script Hygiene Batch

- Timestamp:
  - `2026-04-13T02:29:00.0000000-05:00`
- Files updated for this slice:
  - [`../query.py`](../query.py)
  - [`../tests/test_query_routing.py`](../tests/test_query_routing.py)
  - [`../scripts/start_litert_host_server.ps1`](../scripts/start_litert_host_server.ps1)
  - [`../scripts/litert_native_openai_server.py`](../scripts/litert_native_openai_server.py)
  - [`../scripts/start_android_detail_followup_lane.ps1`](../scripts/start_android_detail_followup_lane.ps1)
  - [`../scripts/run_android_prompt_logged.ps1`](../scripts/run_android_prompt_logged.ps1)
  - [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1)
  - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
  - [`../scripts/run_android_session_flow.ps1`](../scripts/run_android_session_flow.ps1)
  - [`../scripts/run_mobile_headless_preflight.py`](../scripts/run_mobile_headless_preflight.py)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - harden the second-audit `query.py` failure paths so missing data fails loudly or degrades safely instead of silently corrupting retrieval/prompt behavior
  - remove the most obvious script fragilities without changing harness semantics
- What landed:
  - `query.py` now:
    - raises clearly if embedding count does not match retrieval spec count instead of silently dropping lanes through `zip(...)`
    - uses safe metadata defaults when building prompt headers so sparse metadata rows no longer crash prompt generation
  - script hygiene:
    - LiteRT host launchers now resolve the default model path from `SENKU_LITERT_MODEL_PATH` or common local candidate paths instead of one hardcoded username path
    - `start_android_detail_followup_lane.ps1` no longer uses PowerShell 7 `??` syntax before launching `powershell.exe`
    - caller-supplied run labels are sanitized before being used in output filenames on the logged/followup launcher paths
    - `run_android_session_flow.ps1` now percent-encodes `auto_query` like the other Android launchers
    - `run_mobile_headless_preflight.py` now respects requested small `--limit` values instead of forcing a minimum of 12 in the review queue
- Validation:
  - desktop/unit:
    - `python -m unittest tests.test_query_routing tests.test_bench_artifact_tools tests.test_guide_catalog -v`
  - Python syntax:
    - `python -m py_compile scripts/litert_native_openai_server.py scripts/run_mobile_headless_preflight.py`
  - Windows PowerShell parse sanity:
    - touched `.ps1` files successfully parsed via `powershell.exe -NoProfile -Command "[void][scriptblock]::Create((Get-Content -Raw ...))"`
- Practical read:
  - desktop retrieval/prompt failures are less likely to go silent now
  - launcher robustness is better without changing the main Android validation flow
  - the next likely high-ROI desktop fix from the second audit is `lmstudio_utils.py` retry/fallback classification, followed by ingest/frontmatter cleanup

## Update After Desktop Support Hardening Batch

- Timestamp:
  - `2026-04-13T02:19:29.1230083-05:00`
- Files updated for this slice:
  - [`../lmstudio_utils.py`](../lmstudio_utils.py)
  - [`../ingest.py`](../ingest.py)
  - [`../bench.py`](../bench.py)
  - [`../tests/test_lmstudio_utils.py`](../tests/test_lmstudio_utils.py)
  - [`../tests/test_ingest.py`](../tests/test_ingest.py)
  - [`../tests/test_bench.py`](../tests/test_bench.py)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - close the remaining verified desktop/tooling audit items so the faster headless lane is less brittle
  - make the structured checkpoint point at a real validation artifact instead of a stale or missing path
- What landed:
  - `lmstudio_utils.py` now keeps embedding fallback limited to known model-load/model-alias failures and treats opaque runtime `400` responses plus generic `RequestException`s as non-retryable
  - `ingest.py` now only parses leading fenced YAML frontmatter and preserves non-HTML angle-bracket text during chunk cleaning
  - `bench.py` now reports `Avg per successful prompt` from successful generation time only, so failed generations no longer inflate the success-only latency line
  - the structured state checkpoint now references a real focused-suite artifact:
    - [`../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt`](../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt)
- Validation:
  - `python -m unittest tests.test_bench tests.test_lmstudio_utils tests.test_ingest -v`
  - validation artifact:
    - [`../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt`](../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt)
- Practical read:
  - the emulator-light testing pyramid is stronger now because the desktop/headless support lane is less likely to silently misclassify failures or corrupt ingest inputs
  - the next mainline product work should move back to Android quality targets rather than keep extending this audit branch

## Update After LM Studio Retry And Ingest Hardening

- Timestamp:
  - `2026-04-13T02:18:21.3834390-05:00`
- Files updated for this slice:
  - [`../lmstudio_utils.py`](../lmstudio_utils.py)
  - [`../ingest.py`](../ingest.py)
  - [`../tests/test_lmstudio_utils.py`](../tests/test_lmstudio_utils.py)
  - [`../tests/test_ingest.py`](../tests/test_ingest.py)
- Change intent:
  - finish the remaining high-confidence second-audit hardening items on the desktop/tooling side
  - make request retry/fallback behavior less misleading and make ingest parsing less destructive
- What landed:
  - `lmstudio_utils.py` now:
    - treats model-alias 400s like known model-load errors for embedding fallback
    - classifies opaque runtime 400s as non-retryable instead of pretending they are safe transient failures
    - treats generic `RequestException` failures as non-retryable until a narrower category is known
  - `ingest.py` now:
    - only parses frontmatter from a leading fenced YAML block
    - ignores non-mapping YAML frontmatter cleanly
    - preserves non-HTML angle-bracket text such as `<bucket>` or `< 5` while still stripping real HTML tags and SVG blocks
- Validation:
  - focused suite artifact:
    - [`../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt`](../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt)
- Practical read:
  - desktop support code is less likely to hide real configuration/runtime failures behind retries
  - ingest is less likely to silently damage guide content during chunk cleaning

## Update After Bench Summary Metric Fix

- Timestamp:
  - `2026-04-13T02:18:21.3834390-05:00`
- Files updated for this slice:
  - [`../bench.py`](../bench.py)
  - [`../tests/test_bench_runtime.py`](../tests/test_bench_runtime.py)
- Change intent:
  - close the remaining verified bench-report bug from the second audit
  - make benchmark summaries stop overstating successful-lane latency when failures are present
- What landed:
  - the report summary is now wired through the existing `_build_generation_time_summary(...)` timing path instead of mixing failed-generation time into the success-only average line
  - `Avg per successful prompt` now divides successful generation time by successful prompt count instead of using total generation time from failed + successful prompts
  - added a regression test that proves errored prompts are excluded from the success-only timing metrics
- Validation:
  - focused suite artifact:
    - [`../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt`](../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt)
- Practical read:
  - bench summaries now tell the truth more cleanly when a run has partial failures
  - the second-audit desktop batch is now mostly closed out, which makes the next iterations easier to trust

## Update After Baseline-Aware Metadata Gate

- Timestamp:
  - `2026-04-13T02:31:12.0862842-05:00`
- Files updated for this slice:
  - [`../scripts/audit_mobile_pack_metadata.py`](../scripts/audit_mobile_pack_metadata.py)
  - [`../scripts/run_mobile_headless_preflight.py`](../scripts/run_mobile_headless_preflight.py)
  - [`../tests/test_run_mobile_headless_preflight.py`](../tests/test_run_mobile_headless_preflight.py)
  - [`./METADATA_AUDIT_20260412.md`](./METADATA_AUDIT_20260412.md)
- Change intent:
  - turn the metadata no-regression rule against `v5` into an actual tool instead of a manual note
  - widen the audit system so it can watch more than the original four specialist families
- What landed:
  - `audit_mobile_pack_metadata.py` now exposes full `guide_counts`, not just truncated top-guide samples
  - `run_mobile_headless_preflight.py` now supports:
    - `--baseline-sqlite-path`
    - family-level mismatch deltas
    - guide-level regression queues
  - preflight now also audits:
    - `water_storage`
    - `community_security`
    - `community_governance`
  - regression coverage was added for the new delta/review-queue helpers
- Validation:
  - focused suite:
    - [`../artifacts/bench/headless_preflight_20260413/preflight_tests_v2.txt`](../artifacts/bench/headless_preflight_20260413/preflight_tests_v2.txt)
  - active-pack vs `v5` baseline:
    - [`../artifacts/bench/headless_preflight_20260413/preflight_compare_v2.txt`](../artifacts/bench/headless_preflight_20260413/preflight_compare_v2.txt)
- Read:
  - the active asset pack still regresses hard against `v5` on `water_distribution` (`148` vs `91`, delta `+57`)
  - the newly added audit families are useful signal, but they are not the main new regression driver
  - the new regression queue points straight at the `water_distribution` guides that got worse: `GD-105`, `GD-553`, `GD-270`, `GD-074`, and `GD-352`

## Update After Current-Builder Probe Pack

- Timestamp:
  - `2026-04-13T02:31:12.0862842-05:00`
- Files updated for this slice:
  - exported probe pack:
    - [`../artifacts/mobile_pack/senku_20260413_current_builder_probe`](../artifacts/mobile_pack/senku_20260413_current_builder_probe)
- Change intent:
  - determine whether the active Android asset-pack regression is stale deployment drift or a still-live builder heuristic problem
- Validation:
  - probe export completed from current code:
    - [`../artifacts/mobile_pack/senku_20260413_current_builder_probe/senku_manifest.json`](../artifacts/mobile_pack/senku_20260413_current_builder_probe/senku_manifest.json)
  - probe vs `v5` baseline preflight:
    - [`../artifacts/bench/headless_preflight_20260413/preflight_compare_builder_probe_v1.txt`](../artifacts/bench/headless_preflight_20260413/preflight_compare_builder_probe_v1.txt)
  - guarded Android hot-swap check on `5556`:
    - search log:
      - [`../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.logcat.txt`](../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.logcat.txt)
    - ask/search log:
      - [`../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe_answer.logcat.txt`](../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe_answer.logcat.txt)
    - UI dump:
      - [`../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.xml`](../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.xml)
- Read:
  - the current builder does recover the worst lost guide structures from the active asset pack:
    - `GD-105`, `GD-270`, and `GD-553` are back to `structure_type=water_distribution`
  - that proves deployment drift is part of the problem
  - but the probe pack still regresses against `v5` on several families, so it is not promotion-ready:
    - `water_distribution 134`
    - `water_storage 234`
    - `message_auth 30`
    - `glassmaking 80`
  - live `5556` search with the probe pack still stays in-family and finishes quickly enough to be a useful guardrail:
    - `search.topResults` led by `GD-553` / `GD-270`
    - about `20.3 s` on the search-log replay

## Update After Second Source-Level Audit Verification

- Timestamp:
  - `2026-04-13T02:00:00-05:00`
- Change intent:
  - verify a second external source-level audit before promoting its findings into the queue
  - separate truly current Python/tooling issues from anything stale or overstated
- Confirmed:
  - `guide_catalog.py` still assumes frontmatter YAML is a mapping and silently overwrites duplicate `id` / `slug` entries
  - `bench_artifact_tools.py` still crashes on standalone markdown artifacts when sibling JSON is missing
  - `bench_artifact_tools.py` still collapses duplicate prompts in the same section because it keys comparison rows by `(section, question)`
  - `ingest.py` still parses frontmatter from the first `---` anywhere and still strips every `<...>` sequence during chunk cleaning
  - `query.py` still risks silent retrieval-lane loss on embedding/spec length mismatch and still builds prompt headers with direct metadata indexing
  - several script/tooling portability issues are real today:
    - Tate-specific default LiteRT model path
    - PowerShell 7 `??` under `powershell.exe`
    - raw `auto_query` replay in `run_android_session_flow.ps1`
    - unsanitized caller-supplied run labels in multiple harness wrappers
- Nuance:
  - the `run_mobile_headless_preflight.py --limit` complaint is only partially true:
    - it does not globally clamp the report to `12`
    - but `global_review_queue` is still forced to at least `12` entries, so that part of the report can exceed the requested limit
- Practical read:
  - this second audit is much more current than the earlier Android audit
  - the highest-ROI fixes from it are in the bench/catalog/script safety layer, not in core Android retrieval

## Update After Prompt Metadata Hints And Water-Storage Context Cleanup

- Timestamp:
  - `2026-04-13T02:52:09.3900315-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`](../android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java`](../android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - give the mobile prompt a compact metadata-aware context contract instead of relying only on title/section text
  - keep broad water-storage from pulling blank container-fabrication guides into answer context when the user is asking about safe storage, not how to build a vessel
  - verify whether the old off-emulator gravity-fed FTS failure is still real before leaving it in the queue
- What landed:
  - `PromptBuilder` now:
    - adds compact `Metadata:` lines for each retrieved note with anchor/support, category, role, horizon, structure, and trimmed topic tags
    - tells the host-system prompt to treat anchor notes and matching structure/topic tags as stronger evidence
  - `QueryMetadataProfile` now exposes a `waterStorageContainerMakingIntent()` flag so water-storage filtering can distinguish build-a-container asks from safe-storage asks
  - `PackRepository` now:
    - demotes container/vessel-making guides in broad water-storage ordering when the query is not a container-build ask
    - rejects blank `guide-focus` container-fabrication supports from the final water-storage context on non-build asks
- Validation:
  - Android JVM targeted:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.PromptBuilderTest`
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryMetadataProfileTest --tests com.senku.mobile.PackRepositoryTest`
  - Android build:
    - `.\gradlew.bat assembleDebug`
  - Desktop support suite:
    - `python -m unittest tests.test_bench tests.test_lmstudio_utils tests.test_ingest tests.test_query_routing tests.test_bench_artifact_tools tests.test_guide_catalog -v`
  - Headless FTS probe:
    - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_water_core_20260412_20260413_022113_272226.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_water_core_20260412_20260413_022113_272226.json)
  - Live Android prompt artifacts:
    - water prompt-metadata smoke:
      - [`../artifacts/bench/live_refresh_v9_20260413/water_5554_promptmeta.logcat.txt`](../artifacts/bench/live_refresh_v9_20260413/water_5554_promptmeta.logcat.txt)
      - [`../artifacts/bench/live_refresh_v9_20260413/water_5554_promptmeta.xml`](../artifacts/bench/live_refresh_v9_20260413/water_5554_promptmeta.xml)
    - cabin-site prompt-metadata smoke:
      - [`../artifacts/bench/live_refresh_v9_20260413/site_foundation_5560_promptmeta.logcat.txt`](../artifacts/bench/live_refresh_v9_20260413/site_foundation_5560_promptmeta.logcat.txt)
      - [`../artifacts/bench/live_refresh_v9_20260413/site_foundation_5560_promptmeta.xml`](../artifacts/bench/live_refresh_v9_20260413/site_foundation_5560_promptmeta.xml)
    - final broad-water storage checkpoint:
      - [`../artifacts/bench/live_refresh_v9_20260413/water_5554_waterrefine_v3b.logcat.txt`](../artifacts/bench/live_refresh_v9_20260413/water_5554_waterrefine_v3b.logcat.txt)
      - [`../artifacts/bench/live_refresh_v9_20260413/water_5554_waterrefine_v3b.xml`](../artifacts/bench/live_refresh_v9_20260413/water_5554_waterrefine_v3b.xml)
- Read:
  - the current headless runner no longer reproduces the old gravity-fed FTS failure on the water-core pack, so that stale blocker can come off the queue
  - the prompt-metadata change is live on Android:
    - `5554` water host request grew to `systemChars=1013` / `promptChars=2942`
    - `5560` cabin-site host request grew to `systemChars=1611` / `promptChars=3507`
  - the first broad-water refinement pass was only half-right:
    - it pushed `GD-417` lower in raw ranking
    - but it also let `GD-417` leak into `context.selected`, which made the answer mushier
  - the follow-up support-pool fix is the keeper:
    - final `5554` water `context.selected` is now all `GD-252` sections again
    - raw search still starts with `GD-373`, so broad water-storage ranking is not done yet
    - but the answer path is cleaner again and no longer uses the wrong blank container-making guide as support
  - `5558` is still the flaky lane:
    - it remains adb-visible, but install and package queries keep hanging, so it should stay demoted until restarted cleanly

## Update After Warm-Start Harness Support And Water-Distribution FTS4 Ordering

- Timestamp:
  - `2026-04-13T03:10:19.4725975-05:00`
- Files updated for this slice:
  - [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1)
  - [`../scripts/run_android_prompt_logged.ps1`](../scripts/run_android_prompt_logged.ps1)
  - [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1)
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - reduce emulator truth-run overhead without lowering fidelity by letting repeated prompt/follow-up checks skip the app force-stop path
  - record enough timing metadata in prompt manifests that warm vs cold comparisons survive outside terminal wall-clock output
  - close one still-real Android retrieval gap by giving `water_distribution` a dedicated no-`bm25` route-order path on FTS4/no-`bm25` runtimes
- What landed:
  - prompt and detail-followup harnesses now accept `-WarmStart`
  - prompt and detail-followup logged wrappers now record:
    - `warm_start`
    - `started_at`
    - `completed_at`
  - `PackRepository.noBm25RouteFtsOrder(...)` now includes a `water_distribution_priority` branch instead of falling straight to `rowid`
  - focused regression coverage was added for the new `water_distribution_priority` label
- Validation:
  - Android JVM targeted:
    - `.\gradlew.bat testDebugUnitTest --tests com.senku.mobile.PackRepositoryTest`
  - Android build:
    - `.\gradlew.bat assembleDebug`
  - APK reinstall:
    - `emulator-5554`
    - `emulator-5556`
    - `emulator-5560`
  - paired warm-start artifacts:
    - cold:
      - [`../artifacts/bench/harness_warmstart_20260413/candles_5560_cold_v2.json`](../artifacts/bench/harness_warmstart_20260413/candles_5560_cold_v2.json)
    - warm:
      - [`../artifacts/bench/harness_warmstart_20260413/candles_5560_warm_v2.json`](../artifacts/bench/harness_warmstart_20260413/candles_5560_warm_v2.json)
- Read:
  - the warm-start harness change is materially real, not just plumbing:
    - paired deterministic candle replays on `5560` measured:
      - cold `15.4 s`
      - warm `10.0 s`
      - delta `5.4 s`
      - about `35.2%` faster
  - the new manifest timing fields make these comparisons durable inside the artifact set itself
  - the water-distribution no-`bm25` ordering patch is green at the JVM level and is now in the freshly reinstalled APK set
  - a fresh post-install live `5556` gravity-fed distribution replay was started next; that lane still needs a clean artifact harvest before treating the live Android verification as complete

## Update After Live 5556 Gravity-Fed Distribution Replay

- Timestamp:
  - `2026-04-13T03:15:40.0542081-05:00`
- Additional files updated for this slice:
  - [`../scripts/run_android_search_log_only.ps1`](../scripts/run_android_search_log_only.ps1)
  - [`../scripts/run_android_prompt_logged.ps1`](../scripts/run_android_prompt_logged.ps1)
  - [`../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`](../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java)
  - [`../notes/ANDROID_ROADMAP_20260412.md`](../notes/ANDROID_ROADMAP_20260412.md)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- What landed on top of the previous slice:
  - `run_android_prompt_logged.ps1` now kills conflicting same-emulator prompt harnesses before launch, mirroring the existing detail-followup protection
  - `run_android_search_log_only.ps1` now supports `-WarmStart`
  - `DetailActivity` now:
    - triggers auto-follow-up on the next UI post instead of a hardcoded `700 ms` delay
    - uses `executor.shutdown()` instead of `shutdownNow()` in `onDestroy()`
- Validation:
  - live Android distribution artifacts:
    - [`../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.json`](../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.json)
    - [`../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.logcat.txt`](../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.logcat.txt)
    - [`../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.xml`](../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.xml)
  - contaminated-run proof that the mismatch guard is working:
    - [`../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v1.json`](../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v1.json)
- Read:
  - the live Android `5556` gravity-fed replay is now the best current truth point for this lane:
    - `routeChunkFts` and `routeGuideFts` both logged `order=water_distribution_priority`
    - `search.topResults` led with `GD-553 :: Gravity-Fed Distribution Systems`, `GD-553 :: Distribution System Components`, and `GD-270`
    - `context.selected` stayed on `GD-553` guide sections plus `GD-270 :: Community Water Point Design`
    - wrapper manifest:
      - elapsed wall time `95.0 s`
      - detail subtitle `Host answer | gemma-4-e2b-it-litert @ 10.0.2.2:1235 | gpu | 82.9 s`
  - the first `v1` replay on the same lane did not fail silently:
    - it captured `how do i build a clay oven`
    - the wrapper marked it as `detail_title_matches_expected=false`
    - that is a real harness integrity win and justifies keeping the mismatch guard

## Update After Verified Source-Link Probe On 5560

- Timestamp:
  - `2026-04-13T03:24:20.7333633-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`](../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- Change intent:
  - make tapped source chips behave like real guide links instead of reopening the thin retrieved snippet object
  - keep the tapped section context visible while resolving back to the full guide body by `guide_id`
- What landed:
  - `PackRepository` now exposes `loadGuideById(...)`
  - `DetailActivity` source buttons now:
    - resolve the tapped `guide_id` back to the full guide record when possible
    - preserve the tapped source section heading in the opened guide body
    - fall back to the original snippet object only if guide lookup fails
- Validation:
  - Android build:
    - `.\gradlew.bat assembleDebug`
  - live source-probe artifact:
    - [`../artifacts/bench/live_refresh_v11_20260413/candles_followup_5560_sourceprobe_v2.json`](../artifacts/bench/live_refresh_v11_20260413/candles_followup_5560_sourceprobe_v2.json)
    - [`../artifacts/bench/live_refresh_v11_20260413/candles_followup_5560_sourceprobe_v2_source_probe.xml`](../artifacts/bench/live_refresh_v11_20260413/candles_followup_5560_sourceprobe_v2_source_probe.xml)
- Read:
  - the fast candle lane now verifies the source-chip path again:
    - `source_link_probe_attempted=true`
    - `source_link_verified=true`
    - tapped label:
      - `[GD-075] Butchering & Meat Processing / Rendering Fats: Tallow & Lard`
    - observed guide title:
      - `Butchering & Meat Processing`
    - observed subtitle:
      - `GD-075 | agriculture | beginner`
    - opened guide screen had `followup_available=false`, which is the right behavior for a guide-detail view
  - the actual follow-up answer on the same run stayed healthy too:
    - host GPU
    - `11.3 s`
    - visible source chips for `GD-075` and `GD-486`

## Update After Direct House-Subsystem Route Smoke

- Timestamp:
  - `2026-04-13T03:56:20-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- What landed:
  - house-subsystem prompts now get a dedicated `wallFocus` route branch
  - roof / insulation / opening / wall prompts still stay in the house family, but the broad starter-house specs are now gated behind true broad-build or foundation cases
  - the Android route tests now explicitly guard against generic starter-house bleed on roof, insulation, and wall prompts
- Validation:
  - JVM:
    - `cd android-app; .\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryRouteProfileTest`
  - live Android traces:
    - [`../artifacts/bench/live_refresh_v13_20260413/wall_5556_prompt_v2_short.logcat.txt`](../artifacts/bench/live_refresh_v13_20260413/wall_5556_prompt_v2_short.logcat.txt)
    - [`../artifacts/bench/live_refresh_v13_20260413/roof_5554_prompt_v1.logcat.txt`](../artifacts/bench/live_refresh_v13_20260413/roof_5554_prompt_v1.logcat.txt)
- Read:
  - after reinstalling the fresh APK to `5556`, the live wall prompt now logs:
    - `routeSpecs=4`
    - older bad run on the stale APK had logged `routeSpecs=6`
  - the short fresh `5556` trace no longer reached the old generic starter-house queries during the captured route sweep
  - the `5554` roof trace also stayed off the old broad starter path:
    - live routeChunkFts showed the raw roof query plus site/roof-specific specs
    - it did not show the old `simple one room house...` or `cabin hut construction...` specs
  - these were route-shape validations, not fast answer completions:
    - both long ask runs were still slow on-device and were captured while the UI still showed `Generating answer on host GPU...`

## Update After Headless Broad-Site Priority Fix

- Timestamp:
  - `2026-04-13T04:01:14.1289548-05:00`
- Files updated for this slice:
  - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py)
  - [`../tests/test_mobile_headless_parity.py`](../tests/test_mobile_headless_parity.py)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- What landed:
  - the headless answer runner now uses Android-style site-selection priority ordering for broad house/site prompts
  - the Python runner also picked up the broad-house route-row filter so zero-bonus off-structure rows have a harder time crowding the broad siting lane
- Validation:
  - `python -m unittest tests.test_mobile_headless_parity -v`
  - probe artifact:
    - [`../artifacts/bench/headless_answer_samples_20260413/site_selection_probe_20260413_0400.txt`](../artifacts/bench/headless_answer_samples_20260413/site_selection_probe_20260413_0400.txt)
- Read:
  - the headless broad-site query now behaves much more like the best Android lane:
    - search now leads with `GD-446`
    - context now anchors on `GD-446 / Terrain Analysis` plus `Site Assessment Checklist`
  - the remaining parity gap is smaller and more understandable:
    - after the follow-up underground-shelter filter, `GD-873` is gone from the probe
    - the only remaining tail support in the latest probe is `GD-333`, which is a much milder drainage-only cleanup problem

## Update After 5556 Distribution Source-Probe And Headless Parity Repair

- Timestamp:
  - `2026-04-13T03:37:36.5862822-05:00`
- Files updated for this slice:
  - [`../scripts/mobile_headless_parity.py`](../scripts/mobile_headless_parity.py)
  - [`../tests/test_mobile_headless_parity.py`](../tests/test_mobile_headless_parity.py)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- What landed:
  - the non-deterministic `5556` water-distribution follow-up source-probe run is now fully harvested and valid:
    - [`../artifacts/bench/live_refresh_v11_20260413/distribution_followup_5556_sourceprobe_v1.json`](../artifacts/bench/live_refresh_v11_20260413/distribution_followup_5556_sourceprobe_v1.json)
    - `source_link_verified=true`
    - tapped source:
      - `[GD-553] Water System Design and Distribution / Distribution System Components`
    - observed guide title:
      - `Water System Design and Distribution`
    - observed subtitle:
      - `GD-553 | building | Distribution System Components | guide-focus`
    - actual follow-up answer stayed in-family and finished on host GPU in `70.6 s`
  - the headless parity harness is repaired again:
    - fixed missing seasonal house-site marker constant
    - aligned house site-selection detection with the Android `foundation`/`footing` allowance
    - added the Java-style broad-site `siteBreadthIntent` penalties and bonuses into the Python parity scoring
  - new focused headless coverage:
    - `python -m unittest tests.test_mobile_headless_parity -v`
    - all `17` tests passed
    - fresh focused replay:
      - [`../artifacts/bench/headless_answers_20260413_v2/mobile_headless_answers_headless_focus_20260413_20260413_033721_003089.json`](../artifacts/bench/headless_answers_20260413_v2/mobile_headless_answers_headless_focus_20260413_20260413_033721_003089.json)
- Read:
  - the repaired headless lane is useful again for fast triage:
    - `treated water long term` still top-contexts on `GD-252`
    - `soap from animal fat and ash` still top-contexts on `GD-122`
    - `build a clay oven` still top-contexts on `GD-505`
  - broad site-selection improved in the headless runner, but is still not where I want it:
    - it no longer collapses into `GD-333` drainage-only anchors
    - it now top-contexts on `GD-094`, which is better but still not the ideal `GD-446`-style site-selection mix
  - next headless-specific priority:
    - keep aligning the parity runner with the Android house/site lane so broad siting prompts surface `GD-446`-style context and become a better off-emulator oracle

## Fresh Additions - 2026-04-13 House Weatherproof Topic Prune

- Timestamp:
  - `2026-04-13T04:15:15.0762642-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- What landed:
  - cabin-house topic seeding now keeps `weatherproofing` separate from `roofing` instead of treating every weatherproof prompt like a roof prompt
  - roofing topic detection now requires a real roof token, so `weatherproof` no longer trips the `roofing` topic by substring accident
  - new JVM coverage locks this down for both:
    - `how do i waterproof a roof with no tar or shingles`
    - `how do i weatherproof a cabin wall with rough lumber`
- Validation:
  - `cd android-app; .\\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryMetadataProfileTest`
  - report:
    - [`../android-app/app/build/reports/tests/testDebugUnitTest/classes/com.senku.mobile.QueryMetadataProfileTest.html`](../android-app/app/build/reports/tests/testDebugUnitTest/classes/com.senku.mobile.QueryMetadataProfileTest.html)
- Read:
  - the first failing test exposed a real false positive, not a bad expectation:
    - the raw topic detector was matching `roof` inside `weatherproof`
  - after the fix:
    - roof weatherproof prompts still carry `roofing + weatherproofing`
    - wall weatherproof prompts keep `wall_construction + weatherproofing`
    - the wall path no longer auto-promotes `roofing`
  - next cheap proof point:
    - replay a live Android search/log sanity run on `5556` once the harness lane is clear, so the on-device `search.start explicitTopics=` line confirms the same narrowing

## Fresh Additions - 2026-04-13 House Route Prune + Follow-Up Input Hardening

- Timestamp:
  - `2026-04-13T04:24:49.6735720-05:00`
- Files updated for this slice:
  - [`../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`](../android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java)
  - [`../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`](../android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java)
  - [`../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`](../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java)
  - [`../android-app/app/src/main/res/layout/activity_detail.xml`](../android-app/app/src/main/res/layout/activity_detail.xml)
  - [`./AGENT_STATE.yaml`](./AGENT_STATE.yaml)
- What landed:
  - house route-shape is tighter now:
    - roof focus no longer relies on generic `weatherproof` / `seal` substring matches
    - roof prompts no longer carry the old broad roof+foundation support sweep unless they are truly broad starter-house asks
    - pure foundation prompts now use a dedicated foundation route spec instead of paying the full starter-cabin sweep
  - the in-detail follow-up input is hardened against emulator/IME composition glitches:
    - suggestions are disabled
    - personalized learning is disabled
    - auto-filled follow-up text clears composing state before submission
- Validation:
  - `cd android-app; .\\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryRouteProfileTest`
  - `cd android-app; .\\gradlew.bat testDebugUnitTest --tests com.senku.mobile.QueryMetadataProfileTest assembleDebug`
  - fresh live Android search probes:
    - [`../artifacts/bench/live_refresh_v15_20260413/roof_5554_search_v5.run.txt`](../artifacts/bench/live_refresh_v15_20260413/roof_5554_search_v5.run.txt)
    - [`../artifacts/bench/live_refresh_v15_20260413/wall_5556_search_v5.run.txt`](../artifacts/bench/live_refresh_v15_20260413/wall_5556_search_v5.run.txt)
    - [`../artifacts/bench/live_refresh_v15_20260413/foundation_5560_search_v3.run.txt`](../artifacts/bench/live_refresh_v15_20260413/foundation_5560_search_v3.run.txt)
- Read:
  - the new live search probes confirm the narrowed topic footprints on-device:
    - roof: `explicitTopics=[roofing, weatherproofing]`
    - wall-weatherproof: `explicitTopics=[wall_construction, weatherproofing]`
    - foundation: `explicitTopics=[foundation, drainage]`
  - the cheap probes still time out before completion because they are deliberately search-only with a short wait budget, but they are doing the intended job:
    - proving the route/topic shape before spending full answer-generation time
- one open question remains:
  - the fresh roof/foundation probes still report `routeSpecs=5/6`, so there is likely one more hidden broad sweep or overlapping focus signal to trim in the next pass

## Safe Android Cleanup Note

- Timestamp:
  - `2026-04-14T08:06:00-05:00`
- Added a safer device/emulator cleanup helper:
  - [`../scripts/stop_android_device_processes_safe.ps1`](../scripts/stop_android_device_processes_safe.ps1)
- Why:
  - a broad manual process-kill command that matched `node` can take down local OpenCode sidecars, viewer/server lanes, and other agent plumbing
- Rule going forward:
  - use the repo cleanup scripts, not ad hoc `Stop-Process` regexes that include `node`
  - `stop_android_device_processes_safe.ps1` intentionally stops only Android device/emulator processes (`adb`, `emulator`, `qemu-system-*`) and does **not** touch `node`
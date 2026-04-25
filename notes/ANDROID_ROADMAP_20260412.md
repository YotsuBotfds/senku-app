# Android Roadmap - 2026-04-12

This note is the compact roadmap spine for the current Android parity push.

Use it together with:

- [`ACTIVE_WORK_LOG_20260412.md`](./ACTIVE_WORK_LOG_20260412.md) for the active continuation window and latest checkpoint timing
- [`NEXT_AGENT_HANDOFF_20260411.md`](./NEXT_AGENT_HANDOFF_20260411.md) for the current detailed state snapshot
- [`ANDROID_GAP_PACK_20260412.md`](./ANDROID_GAP_PACK_20260412.md) for the breadth-validation pack
- [`ANDROID_SCOUT_FINDINGS_20260412.md`](./ANDROID_SCOUT_FINDINGS_20260412.md) for the structural scout read
- [`METADATA_AUDIT_20260412.md`](./METADATA_AUDIT_20260412.md) for the new pack-audit lane

## Current Truths

- The active Android build is using the refreshed full mobile pack, not the tiny experimental export.
- The active Android validation pack is now the sixth refreshed full-pack copy at [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v6`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v6), after pack-builder fixes for chemistry-body, related-guide leakage, and stronger site-selection metadata signals.
- Pack-only refreshes no longer need a rebuild on debuggable targets:
  - use [`../scripts/push_mobile_pack_to_android.ps1`](../scripts/push_mobile_pack_to_android.ps1) to hot-swap the current `mobile_pack` into `files/mobile_pack`
- Exact host inference with the phone artifact is a normal validation path:
  - host endpoint: `http://10.0.2.2:1235/v1`
  - host model id: `gemma-4-e2b-it-litert`
- The four emulator lanes are still the working matrix:
  - `5554`: water/sanitation and multi-objective separation
  - `5556`: comms/governance/security and water-distribution
  - `5558`: construction/house
  - `5560`: craft/refinement and fast guardrails
- The detail screen now supports in-place follow-up testing, but it still behaves more like a refreshed detail screen than a finished chat app.
- Android runtime is currently logging `fts.unavailable` / `no such module: fts5`, so the intended FTS-backed lexical lane is not actually active on the tested runtime path.
- The latest site-selection query-profile fix is real on Android:
  - fresh clear-logcat runs for `How do I choose a building site if drainage, wind, sun, and access all matter?` now narrow to `explicitTopics=[site_selection, drainage]` instead of the old full house-build bundle
  - exact artifact:
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.logcat.txt`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.logcat.txt)
- The newer compact house-site lane is also real on Android:
  - broad site-selection on `5554` now starts at `routeSpecs=3`
  - `how do i choose a safe site and foundation for a small cabin` on a clean `5560` now starts at `routeSpecs=4` with `explicitTopics=[site_selection, foundation, drainage]`
  - the runtime `routeChunkFts` budget for that lane is now `candidateLimit=192` instead of the older starter-build `600` sweep
  - artifacts:
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.logcat.txt`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/site_5554_v10_short.logcat.txt)
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/cabin_site_5560_v1_short.logcat.txt`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/cabin_site_5560_v1_short.logcat.txt)
    - [`../artifacts/bench/live_refresh_v6_postfix_20260412_b/water_5556_v9.logcat.txt`](../artifacts/bench/live_refresh_v6_postfix_20260412_b/water_5556_v9.logcat.txt)
- The latest route-focused FTS4 fallback work is also real:
  - route-focused chunk/guide search no longer immediately skips on `unsupported_ranking`
  - broad site-selection on `5554` now logs compact FTS4 route queries and reaches `routeGuideSearch.pre`, so the bottleneck has moved from route-chunk skip logic to guide-sweep completion time
  - artifact: [`../artifacts/bench/live_refresh_v6_20260412/site_5554_v10_fts4simpler.logcat.txt`](../artifacts/bench/live_refresh_v6_20260412/site_5554_v10_fts4simpler.logcat.txt)
- The newest runtime-threshold result is now split cleanly:
  - explicit `water_distribution` is a real win:
    - [`../artifacts/bench/live_refresh_v6_20260412/distribution_5556_v9_runtimeguidecap.logcat.txt`](../artifacts/bench/live_refresh_v6_20260412/distribution_5556_v9_runtimeguidecap.logcat.txt)
    - `routeGuideSearch.skip` now happens at `threshold=5`, `routeSearch` completes in about `68.1 s`, whole search completes in about `81.1 s`, and top results stay on `GD-553/GD-270`
  - broad `site_selection` is not safe yet:
    - [`../artifacts/bench/live_refresh_v6_20260412/site_5554_v16_runtimeguidecap.logcat.txt`](../artifacts/bench/live_refresh_v6_20260412/site_5554_v16_runtimeguidecap.logcat.txt)
    - a temporary no-`bm25` broad-site threshold shortcut finished quickly but surfaced junk (`GD-292`, `GD-110`, `GD-444`), so it was reverted
- The latest broad water-storage Android pass materially improved:
  - route search dropped from the older ~`98 s` band to ~`45 s`
  - total search is now down to about `55 s`
  - the route list no longer includes `GD-373` cash/light/medical/cold-climate inventory sections
  - the remaining water gap is narrower now: `GD-373 :: Water Storage & Purification` still outranks more specialized storage guides like `GD-252`
- The latest governance/security Android pass materially improved:
  - `community_security` stayed on `GD-651/GD-388` while total search dropped to about `77.5 s`
  - `community_governance` theft stayed on `GD-626` while route search dropped to about `67.4 s`
  - `community_governance` merge stayed on `GD-626` while total search dropped from the older ~`212 s` band to about `54.7 s`
- The latest live anchor-scoring patch produced real Android wins:
  - broad house on `5558` now routes entirely into `GD-094` top results and anchors context on `GD-094`
  - direct gravity-fed distribution on `5556` now anchors on `GD-270` and selects design/tower/layout sections instead of `GD-648`
  - direct `storage tanks water distribution` on `5560` also anchors on `GD-270`
  - one-turn water-storage guard on `5554` still anchors on `GD-252`
- The targeted headless no-FTS batch now agrees with Android on the main anchor direction:
  - broad house -> `GD-094`
  - direct gravity-fed distribution -> `GD-270`
  - remaining cheap-lane miss is still `storage tanks water distribution` when the off-emulator path drifts without the live Android context
- The headless parity lane is now more trustworthy on governance merge/trust prompts:
  - after the latest section-heading parity fix, `how do we merge with another group if we don't trust them yet` now top-contexts on `GD-626` instead of `GD-865`
  - artifact: [`../artifacts/bench/headless_scout_20260412/mobile_headless_answers_inline_20260412_204541_154012.json`](../artifacts/bench/headless_scout_20260412/mobile_headless_answers_inline_20260412_204541_154012.json)
- Harness hygiene improved:
  - use [`../scripts/stop_android_harness_runs.ps1`](../scripts/stop_android_harness_runs.ps1) before replaying a lane that may have been interrupted or duplicated
- The detail follow-up harness is healthy again on the fast craft lane:
  - artifact: [`../artifacts/bench/android_packrefresh_v7_20260412/candles_followup_5554_v1_harnessguard.json`](../artifacts/bench/android_packrefresh_v7_20260412/candles_followup_5554_v1_harnessguard.json)
  - result: second-turn answer capture, visible thread history, visible source chips, and successful source-link probe all worked together on `5554`
- The newest headless spotcheck batch sharpened the next quality targets:
  - `safe site and foundation for a small cabin` is still too foundation-heavy and needs stronger site-selection context
  - `waterproof a roof with no tar or shingles` is in-family, but still admits `GD-094` calculator junk into context
  - `build a clay oven` is clean on `GD-505`
  - `merge with another group if we don't trust each other yet` is in-family on `GD-626`, but still needs a trust-repair/mediation bias over monitoring/quota-heavy sections
- Pack/runtime knob drift is still real:
  - manifest `mobile_top_k` is currently display-only
  - Android answer generation is still using hardcoded candidate/context limits in `OfflineAnswerEngine`

## What Is Already Working Well Enough

- Candle/tallow follow-ups are stable and fast.
- Water long-term storage follow-ups are usable and grounded.
- Gravity-fed water-distribution prompts are now routing into the right guide family.
- Courier-authentication now anchors on the right guide family after the specialized-lane patch.
- Broad-house retrieval is much faster than before, and generic `what next` no longer immediately jumps into air-sealing drift.
- The current snapshot bundle is staged and zipped in [`../artifacts/snapshots/senku_android_snapshot_20260412_234900`](../artifacts/snapshots/senku_android_snapshot_20260412_234900).
- Current desktop zip copy:
  - [`C:/Users/tateb/Desktop/senku_android_snapshot_20260412_234900.zip`](C:/Users/tateb/Desktop/senku_android_snapshot_20260412_234900.zip)
- The older morning snapshot still exists too, but the new compact-lane snapshot is the one that matches the current APK, pack, and notes.

## Active Problems To Solve Next

### 1. Retrieval Breadth Gaps

These are now the highest-value product gaps:

- courier / note-authentication answers are still too generic
- soapmaking / craft-refinement still has specialized-guide surfacing problems
- guide/chunk metadata verification is now a parallel priority because the soap lane is still anchoring on the wrong guide family after multiple retrieval-side fixes
- multi-objective separation is under-shaped
- water infrastructure beyond container safety is still slower and shakier than it should be
- broad house is no longer in the wrong guide family, but it still needs cleaner support context under the new `GD-094` anchor:
  - `GD-383` frost-line detail is still too sticky
  - climate/specialty support like `GD-110` / `GD-234` still leaks into the broad build-sequence lane
- broad water storage is no longer a gross junk-routing problem, but it still needs:
  - route/score tuning so specialized storage guides outrank broad inventory guidance
  - pack-metadata cleanup so `Home Inventory` non-water chunks stop shipping with `water_storage` structure/topic tags
- first structural win on that front is now real:
  - a reduced `water_distribution` structure-type split dropped headless audit mismatches from `134` to `104` on refreshed full-pack `v4`
  - see [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_160223.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_160223.json)
- the next follow-up improvement on that same lane is also real:
  - a precedence fix for mixed overview guides dropped `water_distribution` mismatches again from `104` to `91` on refreshed full-pack `v5`
  - `GD-553` now lands on `structure_type=water_distribution`
  - see [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_161225.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_161225.json)

### 2. Android Retrieval Runtime Constraint

- FTS5 is not actually available on the tested Android runtime path.
- This likely explains part of the latency and lexical-retrieval noise.
- Treat this as a major constraint for both performance analysis and future retrieval design.
- Route-focused FTS4 fallback is now partially landed:
  - chunk/guide route queries can execute on `lexical_chunks_fts4` without `bm25`
  - explicit water-distribution now has a validated runtime threshold cut that keeps quality while reducing latency
  - the current remaining problem is that broad site-selection still spends too long between `routeChunkFts` and guide-sweep completion, and broad-site threshold shortcuts are not yet quality-safe
- the compact site lane fixed the old broad starter-build budget problem, but even at `candidateLimit=192` the FTS4 route queries are still taking tens of seconds each on emulator/runtime SQLite
- Related cheap-lane problem:
  - the headless FTS batch path still has a `gravity-fed` query parse failure, so off-emulator water-distribution sweeps currently need `--force-no-fts`

### 2a. Backend Trust On Real Phones

- Local LiteRT on ARM phones currently attempts `GPU` first and can silently fall back to `CPU`.
- The app mostly surfaces the final winner, not the attempted path or fallback reason.
- Before serious A35 evaluation, add lightweight backend diagnostics so artifacts and UI can prove whether local GPU actually initialized.

### 3. Session And Chat UX

- Follow-up behavior is now good enough to invest in UI.
- The next UI slice should be transcript-first while preserving harness-facing IDs and scroll behavior.
- The current follow-up harness blocker is specifically initial detail acquisition on noisy emulator lanes:
  - `5560` water follow-up content is still trusted from earlier Android logcat
  - some recent replays fell back into launcher/home or main-screen loading capture before a stable detail screen was reacquired
  - treat that as a harness/emulator-cleanliness problem before assuming a retrieval regression
  - after the latest capture fix, the narrower remaining problem is reacquiring the first-turn answer detail reliably before the in-detail submit, not the follow-up retrieval/generation path itself

### 4. Shared-Contract Drift

- deterministic routing is still not truly shared across desktop, pack export, and Android runtime
- several runtime knobs still drift from pack metadata

This is important, but it should stay behind the current retrieval/gap work unless it blocks a specific fix.

## Near-Term Fix Order

1. Keep replaying the Android gap pack and use desktop as the oracle before changing content.
2. Run a metadata-first loop before more narrow prompt-family tuning:
   - audit the pack
   - refresh metadata
   - replay weak families on-device
   - prefer proving schema changes headlessly before promoting them into the live Android assets
3. Keep the broad water-storage cleanup moving before it regresses into whack-a-mole:
   - finish demoting broad inventory starter guidance beneath specialized storage guides
   - then push the same fix back into pack metadata so runtime guards can stay minimal
4. Tighten the weakest live families first:
  - courier-authentication
  - soapmaking / craft-refinement
  - multi-objective separation
  - roof-weatherproofing context cleanup
  - cabin site-selection context cleanup
  - broad-house support cleanup under the now-correct `GD-094` anchor
  - governance merge/trust trust-repair section preference
5. Add a lightweight metadata-verification pass so retrieval tuning is using trusted structure/topic tags instead of guessing they are correct.
6. Finish the Android route-focused FTS4 fallback by trimming guide-sweep time on compact site-selection prompts.
7. Fix the headless FTS hyphen-query bug so off-emulator distribution sweeps no longer need a degraded no-FTS mode.
8. Investigate the Android FTS5 runtime mismatch before trusting lexical-latency conclusions.
9. Add lightweight backend-observability plumbing before deep real-device GPU evaluation.
10. Land the transcript-first detail screen slice without breaking the harness contracts.
11. After retrieval stabilizes, return to deterministic shared-source-of-truth work.

## Delegatable Future-Direction Tracks

- These lanes are good subagent candidates because they need broad reading and synthesis more than emulator time:
  - metadata-schema and audit-system design
  - chat UI / transcript-first UX roadmap
  - prompt-family clustering, eval design, and gap analysis
  - systems sidecars such as pack/model delivery, deterministic parity, or backend observability
- New strongest sidecar recommendation from the latest scout pass:
  - deterministic parity should move toward a compiled shared payload exported from desktop and consumed by Android
  - target files for the later convergence slice:
    - [`../deterministic_special_case_registry.py`](../deterministic_special_case_registry.py)
    - [`../special_case_builders.py`](../special_case_builders.py)
    - [`../mobile_pack.py`](../mobile_pack.py)
    - [`../android-app/app/src/main/java/com/senku/mobile/DeterministicAnswerRouter.java`](../android-app/app/src/main/java/com/senku/mobile/DeterministicAnswerRouter.java)
  - keep this behind retrieval/harness stabilization, but no longer treat it as optional background thinking
- Related future-direction note:
  - [`MOBILE_UPDATE_ARCHITECTURE_20260412.md`](./MOBILE_UPDATE_ARCHITECTURE_20260412.md)
  - covers pack/model update catalogs, rollback, `guide_key` / alias strategy, and model-profile direction
- These lanes are poor subagent candidates and should stay with the main agent:
  - live retrieval tuning in `PackRepository.java`
  - follow-up/session behavior changes with emulator validation
  - artifact-driven regression triage across the 4-emulator matrix

## Tooling Direction

- Build or adopt a harness layer that makes repeated Android follow-up runs cheaper:
  - matrix wrappers around the existing scripts
  - automatic per-run logcat capture
  - cleaner artifact naming by lane/emulator/version
  - family-level prompt packs so breadth runs stop depending on ad hoc prompt selection
- Newly landed Android retrieval fast lane:
  - [`../scripts/run_android_search_log_only.ps1`](../scripts/run_android_search_log_only.ps1)
  - use it for long search/retrieval checks where repeated `uiautomator dump` polling is fragile or unnecessary
  - current first validated use: the broad water-storage lane on `5554`
- Newly landed fast lane:
  - [`../scripts/push_mobile_pack_to_android.ps1`](../scripts/push_mobile_pack_to_android.ps1)
  - use it whenever only `senku_manifest.json`, `senku_mobile.sqlite3`, or `senku_vectors.*` changed
  - keep rebuild/reinstall for Java/Kotlin/resource changes only
- Newly landed off-emulator answer lane:
  - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py)
  - first slice now covers mobile-pack retrieval, context selection, prompt building, and optional host generation
  - use `--force-no-fts` when you want to mimic the current emulator/runtime fallback path
  - next tooling direction:
    1. keep this as the single headless answer front door
    2. converge it onto a shared parity core in [`../scripts/mobile_headless_parity.py`](../scripts/mobile_headless_parity.py)
    3. port Android hybrid retrieval into that shared core before adding more separate headless runners
- Keep serial execution when a run depends on shared host inference throughput or when artifact attribution would get ambiguous.
- Prefer parallelism for:
  - APK installs
  - non-host setup work
  - artifact parsing
  - subagent scouting / roadmap synthesis
- New harness hygiene rule:
  - same-emulator follow-up runners should preflight-stop conflicting older harness processes before starting
  - this is now implemented in:
    - [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1)
    - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
- Use the new family prompt packs in [`../artifacts/prompts/families_20260412`](../artifacts/prompts/families_20260412) as the first normalized batch surface instead of continuing to assemble every replay lane by hand.
- Use the new headless preflight harness before emulator sweeps when the goal is breadth triage:
  - [`../scripts/run_mobile_headless_preflight.py`](../scripts/run_mobile_headless_preflight.py)
  - current example output:
    - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_155719.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_155719.json)
  - this is not a replacement for emulator truth, but it is now the fastest way to pressure-test family coverage and audit hotspots off-emulator
- Use those packs to keep breadth pressure on at least one non-house/non-water lane each pass:
  - current good example: courier-auth on `5556`
  - current weak example worth feeding back into metadata/anchor work: soapmaking on `5560`
- Newly landed batching aid:
  - [`../artifacts/prompts/families_20260412/android_family_manifest_20260412.json`](../artifacts/prompts/families_20260412/android_family_manifest_20260412.json)
  - use this as the stable pack index for construction, water, crafts, medicine, comms/governance, and multi-objective Android lanes
- New live evidence from the latest `v5` replay pass:
  - retrieval fixes can now recover the house and water lanes while the harness still sometimes falls back out of the in-detail UI path
  - the same pattern now shows up on the gravity-fed distribution `storage tanks` guard too:
    - answer body is correct
    - post-answer source probe is not reliable
  - treat this as a signal to invest in tooling reliability now, not as a reason to reopen already-fixed retrieval bugs
- immediate tooling target:
  - make follow-up harness runs preserve and verify the in-detail path more reliably before source probing
  - likely first script fix:
      - stop requiring a perfect typed-text echo round-trip before allowing the in-detail `Send` tap
- new coordination/tooling baseline:
  - keep [`AGENT_STATE.yaml`](./AGENT_STATE.yaml) updated alongside the markdown notes
  - validate it with [`../scripts/validate_agent_state.py`](../scripts/validate_agent_state.py)
  - use the YAML file as the machine-readable handoff source for continuation timing, active artifacts, and immediate next actions
- current tooling direction update:
  - split validation into a pyramid:
    1. headless family-pack + metadata preflight
    2. headless Android-parity retrieval/prompt replay against the exported mobile pack
3. emulator follow-up/source-link guardrails
3. real-device backend/performance confirmation
  - this should become the default path instead of trying every breadth prompt end-to-end on emulators
  - first concrete tooling build is now landed:
    - [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py)
    - current scope: Android-style retrieval plan + selected context + prompt text, with optional host generation
    - current useful mode: `--force-no-fts` to mirror the emulator runtime constraint
  - next concrete tooling refinement:
    - fold vector-neighbor parity and broader pack-file support deeper into the same harness instead of creating a second headless runner
  - current concrete Android refinement now landed too:
    - use the log-only search harness for long retrieval checks before spending UI-polling budget on emulator screen captures
- Update after the `v6` pass:
  - house and water follow-up lanes are now back on the in-detail UI path with successful source probing
  - this moves the harness item from “broad house/water blocker” to “remaining guardrail cleanup,” with `storage tanks` on `5556` as the next confirmation target
  - tooling win already landed:
    - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
    - use it for quick follow-up guardrail sweeps with automatic logcat capture before doing bigger manual replay sessions
  - new targeted checkpoint after follow-up harness fixes:
    - [`../artifacts/bench/harness_diag_20260412/distribution_followup_5556_logged_fix2.json`](../artifacts/bench/harness_diag_20260412/distribution_followup_5556_logged_fix2.json)
    - `storage tanks` now stays on `in_detail_ui` with `visible_send_button_advanced`
    - the remaining tooling issue on that lane narrowed to source-probe stability, not the post-submit follow-up path itself
  - fast control validation after the source-probe patch:
    - [`../artifacts/bench/harness_diag_20260412/candles_followup_5560_direct_fix3.json`](../artifacts/bench/harness_diag_20260412/candles_followup_5560_direct_fix3.json)
    - the candle/tallow lane now verifies the tapped guide title again after a header-reveal step in the probe logic

## Harness Update After The Repair Pass

- Verified checkpoint:
  - [`../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v2.json`](../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v2.json)
  - `storage tanks` stayed on `in_detail_ui`
  - `followup_submission_used_fallback = false`
  - `followup_submission_advanced_after_submit = true`
- Meaning:
  - the old post-submit fallback is no longer the primary failure mode on that lane
- Remaining harness issues now visible:
  - long-answer source panels can still sit below the current reveal/probe window
  - initial-screen acquisition can occasionally capture the Android launcher instead of the Senku detail screen
  - representative launcher-capture artifact:
    - [`../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v3_initial.xml`](../artifacts/bench/harness_repair_20260412/distribution_followup_5556_harnessfix_v3_initial.xml)

## Current Validation Queue

### Breadth Queue

- replay courier-authentication after the specialized-lane anchor patch
- replay soapmaking after fixing the live guide-sweep threshold mismatch
- replay at least one latrine-design / sanitation prompt on `5554`
- replay one multi-objective separation prompt on `5554`
- keep iterating on explicit `water_distribution` first-turn quality:
  - the topic-seed contamination is fixed
  - the remaining issue is anchor quality and prompt shaping, not the old polluted support pool

### Guardrail Queue

- keep water-storage `what next` clean on `5554`
- keep gravity-fed distribution `storage tanks` clean on `5556`
- keep broad-house `what next` clean on `5558`
- keep candle/tallow clean on `5560`

### UI Queue

- promote `detail_thread_container` into the primary transcript surface
- keep `detail_title`, `detail_subtitle`, `detail_body`, `detail_sources_container`, `detail_followup_input`, and `detail_followup_send` stable for the harness
- replace the current jump-to-top behavior with a “keep newest turn visible” behavior

## Rejected Or Unsafe Ideas

- Do not replace the live Android pack with the tiny `1344`-chunk experimental export in `artifacts/mobile_pack/senku_20260412_refresh`.
- Do not reuse the rejected `water_distribution` budget trims that regressed back toward generic `GD-252` or accessibility junk.
- Do not assume current Android lexical timings reflect intended FTS behavior while `fts.unavailable` is still live.
- Do not rewrite the detail screen in a way that breaks the current follow-up harness contracts.
- Do not solve new retrieval misses by hardcoding specific guide IDs when metadata/category/alias signals can solve them instead.
- Do not trust the intended soap/glass guide-sweep threshold until live logcat reflects it; unit tests alone are not enough for this lane.

## Definition Of A Good Next Checkpoint

A strong next checkpoint would have all of the following:

- refreshed notes in `notes/`
- one more breadth fix or pack refresh validated on-device
- one more gap-pack family replayed with artifacts
- no regression in the four main emulator guardrail lanes
- a transcript-first UI slice either landed or queued with explicit harness-safe constraints

## Immediate Next Slice

- Keep using the new harness layer:
  - [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1)
  - [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1)
- Treat the current retrieval priorities as:
  1. keep the new explicit `water_distribution` lane anchored on `GD-270` / distribution-family guides while tightening the final section mix so answers stay on layout/design instead of drifting into maintenance or failure-analysis wording
  2. tighten water-storage context selection so `GD-252` stops dragging `Root Cellars` / `Canning` into the answer window
  3. keep the new broad-house `GD-383` foundation anchor without regressing the explicit drainage / site-selection lane
- Treat the current tooling priorities as:
  1. keep per-run logcat capture on by default for follow-up diagnosis
  2. use matrix batches for multi-emulator guardrail sweeps instead of one-off hand runs
  3. build the new headless answer-parity lane so most retrieval/prompt iterations stop paying emulator time
  4. keep `followup_submission_input_match` in the manifest so accessibility echo bugs are measurable
  5. keep [`AGENT_STATE.yaml`](./AGENT_STATE.yaml) current so future agents can resume from strict structured state instead of prose-only notes
- Treat the current future-direction architecture priority as:
  - deterministic convergence through a compiled shared payload exported from desktop and loaded on Android

## Update After Headless Scorer Sync

- The headless answer-parity lane is no longer just a raw smoke tool; it now catches real ranking regressions quickly enough to change the normal workflow.
- Current confirmed off-emulator checkpoints:
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_878475.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_878475.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_544618.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_544618.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_581590.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_173246_581590.json)
- What changed:
  - broad house now top-contexts on `GD-383` foundation/drainage sections off-emulator instead of the old roof-first or insulation-first drift
  - water-storage now keeps `GD-252` hydration/container/rotation sections in the top window instead of mixing in `Chemical Storage`
  - water-distribution now keeps `Distribution Network Layout` and `Community Water Point Design` in the top window instead of `Common Mistakes`
- What this means for the next slice:
  - replay those same lanes on Android before trusting the headless cleanup as complete
  - keep using the headless lane first for section-ranking changes, then spend emulator time only on guarded confirmation

## Update After Live Water / Distribution Replay

- Verified Android-side checkpoint:
  - [`../artifacts/bench/live_metadata_patch_20260412/distribution_5556_v4.logcat.txt`](../artifacts/bench/live_metadata_patch_20260412/distribution_5556_v4.logcat.txt)
  - [`../artifacts/bench/live_metadata_patch_20260412/water_5554_v4.logcat.txt`](../artifacts/bench/live_metadata_patch_20260412/water_5554_v4.logcat.txt)
- What improved live:
  - explicit gravity-fed distribution still anchors on `GD-270` and keeps the good `1 / 2 / 5 / 4` section mix
  - treated-water storage still anchors on `GD-252`, but the selected same-guide context is cleaner now that hazard-storage sections are blocked from the answer window
- What remains:
  - the last weak slot on live water storage is now `Failure Modes & Recovery`
  - broad house is still good on-anchor, but the raw ranked list still has too much `GD-094` / `GD-106` noise before context selection fixes it
- Immediate priority after this:
  1. keep the water-storage lane on `GD-252` while replacing `Failure Modes & Recovery` with a cleaner sanitation/inspection/maintenance section
  2. keep explicit `water_distribution` stable on `GD-270`
  3. keep using headless first, then guarded emulator confirmation

## Update After Clay-Oven Route Fix

- The headless lane now has a dedicated clay-oven build path:
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_216672.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175150_216672.json)
- Read:
  - `how do i build a clay oven` no longer falls into default lexical junk and now top-contexts on `GD-505 Clay Bread Oven Construction`
  - site/foundation and roof-waterproof construction prompts stayed clean in the same replay wave
- Next retrieval focus after this:
  1. replay the clay-oven lane on Android to confirm the real app path still uses the correct guide family
  2. use the headless batch to fix the next default-family misroutes in governance/security prompts before spending another broad emulator sweep

## Update After Governance / Security Headless Family Fix

- The headless lane now has two new family-level route buckets:
  - `community_security`
  - `community_governance`
- Current checkpoints:
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_540004.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_540004.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_660817.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_660817.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_717564.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_inline_20260412_175707_717564.json)
  - [`../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_comms_governance_core_20260412_20260412_175727_595793.json`](../artifacts/bench/mobile_headless_answers_20260412/mobile_headless_answers_comms_governance_core_20260412_20260412_175727_595793.json)
- What improved:
  - physical/resource-security prompts now stay out of `default` and top-context on `GD-651`
  - theft/resource-rule prompts now stay out of `default` and top-context on `GD-626`
  - merge/trust prompts are no longer nonsense, but still lean too much toward `GD-865` mutual-aid-fund sections
- What this means for the next slice:
  1. port the new governance/security routing concepts into Android after one more headless pass on the merge/trust lane
  2. keep using the headless lane first for governance/security and clay-oven before spending emulator time
  3. separately fix the known pack-metadata problem where `GD-651` currently carries `message_auth` tagging that should really become a broader security family tag

## Future UI / UX Considerations

- `Pinned Projects` / cached threads:
  - once a thread is good, save the generated custom guide locally so it reopens instantly without paying inference again
- `Direct Query` / panic bypass:
  - add a raw-text fast lane that skips generation and opens the retrieved guide text directly for urgent first-aid or safety lookups
- tappable source chips with raw retrieved text:
  - source buttons should be able to open the exact retrieved chunk text, not just the guide title, so users can verify the model summary
- materials/tools extraction:
  - add a lightweight extractor or final pass that surfaces a checklist of needed materials/tools from the generated guide
- battery / compute governor:
  - add a low-battery mode that requests much shorter answers or warns about expensive long-form generation before the run starts

## Immediate Next Slice

- validate the immediate water follow-up lane off the new `GD-252` anchor:
  - current first-turn checkpoint: [`../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v6_anchorfix3.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/water_5560_ask_v6_anchorfix3.logcat.txt)
  - success condition: prompts like `what about rotation` and container follow-ups keep the thread on `GD-252`-style storage guidance and do not fall back to inventory or sealants
- keep the new governance anchor behavior and improve the support mix:
  - current live checkpoint: [`../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2.logcat.txt)
  - current UI artifact: [`../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2_manual.xml`](../artifacts/bench/android_packrefresh_v7_20260412/governance_merge_5558_ask_v2_manual.xml)
  - next quality move:
    1. keep `GD-626` as the anchor for merge-and-trust prompts
    2. reduce `GD-657` / currency-style bleed in support context
    3. prefer more trust-repair and mediation sections from `GD-626` / `GD-385`
- keep the pack-builder cleanup as the preferred direction for topic pollution:
  - current pack checkpoint: [`../artifacts/pack_refresh_community_20260412_v7/senku_manifest.json`](../artifacts/pack_refresh_community_20260412_v7/senku_manifest.json)
  - `GD-373` `water_storage` rows are now down to `8`, matching only the true water section
  - fresh audit gate read:
    - better current baseline is [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v5`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v5)
    - comparison artifacts:
      - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210234.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210234.json)
      - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210421.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210421.json)
    - practical rule: do not promote a newer hot-swap pack if it regresses against `v5` on the audited family set
- once water answer-path validation is confirmed:
  - replay clay-oven and governance/security on Android so the real app path catches up to the newer headless family fixes
- repair the detail follow-up harness before trusting the next multi-turn replay wave:
  - the guide-note false-positive is fixed in [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1)
  - the biggest shell/runtime blocker is now fixed:
    - per-run dump names + frozen second-turn answer capture + quiet dump-transfer helpers now let the water follow-up lane complete end to end on Android
    - checkpoint:
      - [`../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.json`](../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.json)
      - [`../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.logcat.txt`](../artifacts/bench/android_packrefresh_v7_20260412/water_followup_5560_v18_freezee.logcat.txt)
  - cross-family confirmation is now in place too:
    - [`../artifacts/bench/followup_matrix_20260412/candles_followup_5554_v1_matrix.json`](../artifacts/bench/followup_matrix_20260412/candles_followup_5554_v1_matrix.json)
    - [`../artifacts/bench/followup_matrix_20260412/house_followup_5558_v1_matrix.json`](../artifacts/bench/followup_matrix_20260412/house_followup_5558_v1_matrix.json)
    - [`../artifacts/bench/followup_matrix_20260412/distribution_followup_5556_v1_matrix.json`](../artifacts/bench/followup_matrix_20260412/distribution_followup_5556_v1_matrix.json)
  - next harness focus:
    1. keep logging direct `ask.start` / `ask.generate` evidence from logcat when UI capture is inconclusive
    2. only reopen harness code if a new family exposes a real UI-state bug rather than a content bug
    3. spend the next passes on retrieval/content drift again, not harness survival
- keep concurrency biased toward:
  1. headless and search-log-only validation first
  2. guarded emulator UI/detail checks second
  3. real-device confirmation last

## Update After Prompt-Harness And Headless-Parity Fixes

- Newly validated:
  - the headless governance parity lane now keeps the exact merge-and-trust prompt on `GD-626` rather than `GD-865`
  - `run_android_prompt.ps1` now uses the quieter `uiautomator dump ... >/dev/null 2>/dev/null` path to reduce null-root / missing-dump noise during first-turn acquisition
  - `run_android_detail_followup.ps1` now treats a busy MainActivity state as in-flight work and waits instead of relaunching the same first-turn ask
  - the 5560 water prompt lane now cleanly reaches the detail screen again after making prompt/follow-up temp dumps unique per run and rejecting MainActivity status-only completion in ask mode
- New scout reads:
  - cabin safe-site/foundation Android search remains in the right family but still overweights `GD-094`; next quality target is surfacing more `GD-446` site-selection material sooner
  - roof waterproofing Android search remains in the right family but still admits `GD-094` calculator junk in context; next quality target is stronger section-level penalties for calculator-style sections on roofing/weatherproofing asks
- Current live work still in progress:
  - `emulator-5560` water follow-up replay using the patched busy-main-screen wait path
  - `emulator-5554` cabin site/foundation answer-path replay
  - `emulator-5556` roof waterproofing answer-path replay
- Highest-ROI next actions after the current background runs settle:
  1. confirm the `5560` water follow-up captures a real second-turn detail screen without duplicating the first-turn ask
  2. if cabin answer-path still lacks site-selection material, bias `site_selection` support harder for explicit cabin/foundation prompts
  3. if roof answer-path still carries calculator junk, strengthen `roofing` / `weatherproofing` section penalties and retest headless first

## Update After Broad Site-Selection Android Fix

- New validated checkpoint:
  - [`../artifacts/bench/live_refresh_v7_20260413/site_5554_v1.logcat.txt`](../artifacts/bench/live_refresh_v7_20260413/site_5554_v1.logcat.txt)
  - [`../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.logcat.txt`](../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.logcat.txt)
  - [`../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.xml`](../artifacts/bench/answer_refresh_v7_20260413/site_5554_v1.xml)
- What changed:
  - broad Android building-site prompts now skip the old raw-query site spec and instead run two site-focused route specs
  - the no-`bm25` FTS fallback now uses a site-selection priority order over joined chunk fields so `GD-446` rows surface before row-order junk
- What that bought us:
  - `5554` broad site-selection now runs with `routeSpecs=2`
  - `routeChunkFts` and `routeGuideFts` both log `order=site_selection_priority`
  - `search.topResults` is now led by `GD-446` guide-focus and `GD-446` siting sections instead of `GD-094` / `GD-333` drift
  - the answer path is aligned too:
    - `context.selected` is entirely `GD-446`
    - `anchorGuide="GD-446"`
    - host generation is still fast (`~4 s`) once retrieval is done

## Update After Small-Cabin Siting Follow-Up Repair

- New validated checkpoint:
  - [`../artifacts/bench/detail_refresh_v7_20260413/site_foundation_5560_v3.json`](../artifacts/bench/detail_refresh_v7_20260413/site_foundation_5560_v3.json)
- What changed:
  - `SessionMemory` now adds preferred siting/foundation topic hints before generic `cabin house` structure hints for short explicit house follow-ups
- What that bought us:
  - the old follow-up collapse from `what about drainage and runoff?` to `drainage runoff cabin house` is gone
  - the repaired follow-up query is now `drainage runoff foundation site selection cabin house`
  - `routeSpecs=3` and `order=site_selection_priority` stay active on the second turn
  - `GD-446` remains the anchor guide on the follow-up
- Remaining gap:
  - the follow-up answer body still leans heavily on `GD-593` drainage assessment; the next quality move is to reduce that support dominance once the siting anchor is already correct

## Immediate Next Slice

1. Replay a broad-site follow-up on `5554` now that the first-turn `GD-446` lane is fixed.
2. Tighten the small-cabin drainage follow-up support mix so `GD-446` / `GD-094` stay stronger than `GD-593` / `GD-111`.
3. Keep the pack-side site-selection metadata expansion queued behind the live Android wins:
   - richer guarded siting tags in [`../mobile_pack.py`](../mobile_pack.py)
   - positive metadata tests for `GD-446`-style chunks in [`../tests/test_mobile_pack.py`](../tests/test_mobile_pack.py)

## Update After Broad-Site Follow-Up Validation

- Newly validated:
  - the broad-site winter-sun follow-up on `5554` is repaired end to end
  - second-turn retrieval now searches with `winter sun summer shade site selection foundation cabin house`
  - the follow-up stays `routeFocused=true`, uses `order=site_selection_priority`, and keeps `GD-446` as the anchor guide
  - the visible answer/source path is now clean: source buttons show `GD-446`, `GD-094`, and `GD-383`, and the old `GD-064` agriculture leak is gone
- Highest-ROI next actions:
  1. fix the tapped source-chip navigation bug so the validated source buttons reliably open guide detail
  2. tighten the small-cabin siting support mix so `GD-593` / `GD-111` stop crowding `GD-446` / `GD-094`
  3. keep the pack-side site-selection metadata expansion queued as the scalable next step once the live Android source-link UX is stable

## Update After Manual Auto-Followup Validation

- Newly validated:
  - the automation contract between `MainActivity` and `DetailActivity` is clean again for auto-followup runs
  - a manual adb replay on `5554` now keeps the initial broad-site query in `search_input`, launches the second turn with the repaired microclimate-aware query, and leaves the follow-up text only in `detail_followup_input`
  - the completed second turn stays in-thread and reports `Host answer | ... | gpu | 111.0 s`
- What this changes strategically:
  - the weird concatenated follow-up text in the main search box is no longer the blocker for follow-up automation
  - the next bottleneck on this lane is answer quality, not UI focus corruption
  - broad-site seasonal follow-ups can now be tuned mostly from retrieval/prompt behavior instead of harness survival work
- Current gap on this exact lane:
  - retrieval/context is now better than the answer body
  - `context.selected` leads with `GD-446 :: Wind Exposure and Microclimate`, but the final answer still falls back to generic siting advice instead of clearly discussing winter solar gain and summer shade tradeoffs

## Headless Lane Guidance After The Latest Replay

- Keep using headless as:
  1. the first scout for metadata/ranking families
  2. the place to catch obvious anchor failures like soapmaking on `GD-262`
  3. the fast lane for wide prompt packs before spending emulator minutes
- Do not treat headless as:
  1. a final oracle for no-`bm25` Android siting behavior
  2. proof that source buttons or thread-state UI works
  3. the last word on order-sensitive `GD-446` site-selection repairs
- Near-term consequence:
  - the next broad-site seasonal answer-quality pass can be prototyped headlessly, but it still needs a guarded `5554` replay before claiming Android parity

## Update After Manual Auto-Followup Contract Cleanup

- Newly validated:
  - the app-side automation contract is clean again on `5554`
  - `MainActivity` no longer steals focus and mixes second-turn text into the main search field during automation-driven runs
  - the detail follow-up harness now re-dumps focused UI state before clearing or matching inline text, so it is less likely to submit garbled follow-up text
  - the seasonal broad-site follow-up now uses the microclimate-aware retrieval query:
    - `winter sun summer shade site selection wind exposure microclimate cabin`
  - `context.selected` now starts with:
    - `GD-446 :: Wind Exposure and Microclimate`
    - `GD-446 :: Terrain Analysis`
    - `GD-446 :: Site Assessment Checklist`
    - `GD-446 :: Seasonal Considerations`
- What this changes about next priorities:
  1. the next broad-site move is no longer route-family repair; it is answer-shaping quality so winter-sun / summer-shade answers actually talk about solar gain, shade, and seasonal siting tradeoffs
  2. source-link navigation remains the next Android UI bug because the source chips are now usually correct but still do not reliably open the expected guide detail
  3. headless/off-emulator scouting should keep running, but treat live Android as the authority on no-`bm25` site-selection behavior until the headless parity gap is explained

## Update After Seasonal Site Prompt-Shaping Replay

- Newly validated:
  - the broad-site seasonal follow-up on `5554` is now improved at the answer layer, not just the retrieval layer
  - the second-turn prompt now uses `promptContext=4` for the seasonal siting lane instead of trimming back to `3`
  - the user-visible answer now explicitly covers:
    - year-round siting
    - winter solar gain
    - summer shade
    - wind exposure
    - seasonal hazards
- What changed technically:
  1. prompt shaping now accepts the richer `contextSelectionQuery` instead of trying to infer everything from the raw follow-up text
  2. seasonal site follow-ups get extra prompt guidance telling the model to answer the specific siting tradeoff first
  3. the fourth `Seasonal Considerations` section is now allowed into the prompt for that follow-up family
- What this changes about next priorities:
  1. broad-site seasonal follow-up quality is no longer the immediate blocker, so the next answer-quality rescue should target another lane with correct retrieval but weak final synthesis
  2. source-link navigation is still the next Android UI bug because the content path is now strong enough that verification UX matters more
  3. keep headless scouting in the loop, but stay willing to use manual `adb` validation when a live follow-up family is close and prompt shaping is the variable under test

## Update After Verified External Audit And Prompt-System Upgrade

- Newly validated:
  - the external audit was useful but not fully current:
    - real issues:
      - mobile host inference had no actual `system` role
      - mobile still forces one rigid answer shape for every route family
      - `SessionMemory` token matching had real singularize/prefix debt
      - shared marker drift is a live bug source
    - stale claims:
      - centroid-null route-drop is already handled by `mergeResultsWhenCentroidMissing(...)`
      - the older FTS-no-fallback complaint is outdated because route search already uses `shouldBackfillLikeAfterFts(...)`
      - no-bm25 route ordering already has a soapmaking branch
  - host inference now sends a real Android-built `system` prompt and still completes cleanly on the live GPU host path
- What changed technically:
  1. `PromptBuilder` now exposes a reusable system-style instruction block derived from the Android route guidance
  2. `HostInferenceClient` now sends `system` + `user` messages instead of a user-only payload
  3. `SessionMemory` token matching now uses bounded-prefix matching and stops mangling double-`s` words like `glass`
  4. the Android JVM verification lane was repaired by fixing the small compile/test drift surfaced during the audit pass
- What this changes about next priorities:
  1. route-conditional answer formatting is now the highest-value prompt-quality follow-up
  2. shared marker extraction/cleanup should come before any big `PackRepository` refactor
  3. keep using headless and scouts for wide triage, but keep live Android as the authority whenever no-bm25 ordering or detail-thread UX is the variable under test

## Update After Desktop Support Hardening Batch

- Newly validated:
  - the faster desktop/headless support lane is less brittle now:
    - `lmstudio_utils.py` classifies opaque runtime `400`s and generic request failures more safely
    - `ingest.py` only parses leading fenced YAML frontmatter and preserves non-HTML angle-bracket text
    - `bench.py` no longer inflates `Avg per successful prompt` with failed-generation time
  - focused validation artifact:
    - [`../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt`](../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt)
- What this changes about next priorities:
  1. keep using the headless lane first because the supporting tooling is now safer and more trustworthy
  2. return the main implementation focus to Android quality gaps, especially source-chip navigation, marker cleanup, and the remaining water/site/house quality issues
  3. treat new audit findings as patchable support work, not as a reason to derail the core Android parity loop

## Update After Desktop Hardening Continuation

- Newly validated:
  - the second-audit desktop/tooling queue is now mostly closed:
    - `guide_catalog.py` rejects duplicate ids/slugs and ignores non-mapping frontmatter
    - `bench_artifact_tools.py` supports markdown-only artifacts and preserves duplicate prompts by index
    - `query.py` fails loudly on embedding/spec mismatches and tolerates sparse metadata rows
    - launcher scripts now have safer default model-path resolution, query encoding, and run-label handling
    - `lmstudio_utils.py` now classifies opaque runtime 400s and generic request failures more conservatively
    - `ingest.py` now parses only leading fenced YAML frontmatter and preserves non-HTML angle-bracket content
    - `bench.py` no longer inflates `Avg per successful prompt` with failed-generation time
- Validation artifact:
  - [`../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt`](../artifacts/bench/desktop_hardening_20260413/focused_suite_v1.txt)
- What this changes about next priorities:
  1. the next highest-ROI work is back on Android retrieval, prompt shaping, metadata quality, and UI verification rather than more desktop audit cleanup
  2. headless scouting remains the preferred first pass now that its supporting desktop/tooling code is sturdier
  3. the remaining product-side leverage is still metadata quality plus Android source/thread UX, not another round of low-level desktop script fixes

## Update After Metadata Gate And Builder Probe

- Newly validated:
  - preflight is now baseline-aware, not just a one-pack mismatch snapshot:
    - current active-pack vs `v5`:
      - [`../artifacts/bench/headless_preflight_20260413/preflight_compare_v2.txt`](../artifacts/bench/headless_preflight_20260413/preflight_compare_v2.txt)
    - current-builder probe vs `v5`:
      - [`../artifacts/bench/headless_preflight_20260413/preflight_compare_builder_probe_v1.txt`](../artifacts/bench/headless_preflight_20260413/preflight_compare_builder_probe_v1.txt)
  - the active asset pack is genuinely behind:
    - `water_distribution` regresses to `148` mismatches vs baseline `91`
    - biggest guide regressions are `GD-105`, `GD-553`, `GD-270`, `GD-074`, and `GD-352`
  - the current builder is partly better than the active asset pack:
    - it restores `GD-105`, `GD-270`, and `GD-553` to `structure_type=water_distribution`
    - that proves deployment drift is part of the current Android quality gap
  - the current builder is still not clean enough to promote:
    - `water_distribution 134`
    - `water_storage 234`
    - `message_auth 30`
    - `glassmaking 80`
  - guarded Android hot-swap check on `5556` stayed healthy on the probe pack:
    - [`../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.logcat.txt`](../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.logcat.txt)
    - live search still led with `GD-553` / `GD-270`
- What this changes about next priorities:
  1. keep using `v5` as the metadata promotion baseline, now enforced by tooling rather than memory
  2. do not blindly promote the current builder pack even though it fixes some active-pack drift
  3. focus builder-side metadata cleanup on the remaining `water_distribution` regression guides before trying another pack promotion

## Fresh Additions - 2026-04-13 Early Slice

- Harness efficiency just got a practical win:
  - `run_android_prompt*.ps1` and `run_android_detail_followup*.ps1` now support warm-start runs that skip the app force-stop path
  - the logged wrappers now record `warm_start`, `started_at`, and `completed_at` in their JSON manifests
  - paired `5560` deterministic candle replays measured:
    - cold `15.4 s`
    - warm `10.0 s`
    - about `35.2%` faster
  - artifacts:
    - [`../artifacts/bench/harness_warmstart_20260413/candles_5560_cold_v2.json`](../artifacts/bench/harness_warmstart_20260413/candles_5560_cold_v2.json)
    - [`../artifacts/bench/harness_warmstart_20260413/candles_5560_warm_v2.json`](../artifacts/bench/harness_warmstart_20260413/candles_5560_warm_v2.json)
- One still-real Android FTS4 gap is closed:
  - `PackRepository.noBm25RouteFtsOrder(...)` now includes `water_distribution_priority`
  - that means explicit gravity-fed distribution prompts no longer have to fall straight to `ORDER BY c.row_id` on no-`bm25` Android paths
  - focused JVM coverage is in [`../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`](../android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java)
- Immediate next validation:
  - harvest a clean live Android gravity-fed distribution replay after the reinstall and confirm the new ordering shows up on the real `5556` path
  - if that replay is noisy or stale, recycle the lane instead of treating an empty capture as evidence

## Fresh Additions - 2026-04-13 Distribution Replay

- The live Android `5556` gravity-fed replay is now fully captured:
  - [`../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.json`](../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.json)
  - [`../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.logcat.txt`](../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v2.logcat.txt)
  - route search is now visibly using `order=water_distribution_priority` on the real FTS4/no-`bm25` path
  - `search.topResults` led with `GD-553` / `GD-270`
  - `context.selected` stayed on `GD-553` guide sections plus `GD-270` support
  - wrapper manifest wall time was `95.0 s`, with host generation at `82.9 s`
- Harness integrity improved again:
  - the failed contaminated replay is now preserved as a proper mismatch artifact instead of a false success:
    - [`../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v1.json`](../artifacts/bench/live_refresh_v10_20260413/distribution_5556_prompt_v1.json)
  - `run_android_prompt_logged.ps1` now stops conflicting same-emulator prompt harnesses before launch
  - `run_android_search_log_only.ps1` now supports `-WarmStart`
- Small safe UI/thread cleanup also landed:
  - `DetailActivity` no longer waits on a hardcoded `700 ms` auto-follow-up delay
  - `DetailActivity` now uses `executor.shutdown()` instead of `shutdownNow()` in `onDestroy()`

## Fresh Additions - 2026-04-13 Source-Link Verification

- Source-chip navigation is in a better place again:
  - source buttons now resolve back to the full guide by `guide_id` when possible instead of only reopening the thin retrieved snippet payload
  - the tapped section heading is still preserved in the opened guide body so the user keeps the local context that led to the answer
- Fast validation is real on `5560`:
  - [`../artifacts/bench/live_refresh_v11_20260413/candles_followup_5560_sourceprobe_v2.json`](../artifacts/bench/live_refresh_v11_20260413/candles_followup_5560_sourceprobe_v2.json)
  - the run finished with:
    - `source_link_probe_attempted=true`
    - `source_link_verified=true`
    - observed guide title `Butchering & Meat Processing`
    - observed subtitle `GD-075 | agriculture | beginner`
    - `followup_available=false` on the opened guide screen, which is the correct guide-detail behavior
- Next UI proof point:
  - replay that same source-link path on a non-deterministic lane like water-distribution or site-selection so source verification is not candle-only

## Fresh Additions - 2026-04-13 House-Subsystem Cleanup

- The house-subsystem route fix is now in a better place:
  - roof / insulation / opening / wall prompts still route through the house family
  - but the generic starter-house specs are now gated behind true broad-build or foundation cases instead of contaminating every subsystem ask
- Current proof:
  - unit tests now explicitly guard against broad starter-house bleed on roof, insulation, and wall prompts
  - live Android route traces:
    - wall on `5556` now logs `routeSpecs=4` after the reinstall
    - roof on `5554` no longer showed the old `simple one room house...` / `cabin hut construction...` seeds in the captured route sweep
- What still needs work:
  - these subsystem queries are still too slow in the real app path because route-focused FTS4/no-`bm25` sweeps remain expensive when the first chunk FTS passes return `added=0`
  - the headless broad-site-selection lane still over-surfaces `GD-094/GD-333` instead of `GD-446`, so the off-emulator oracle is still weaker than the best Android lane for siting prompts
- Immediate next priority:
  - fix headless broad-site-selection surfacing so `GD-446` wins more often off-emulator, which should make the fast preflight lane more trustworthy and reduce emulator tax on future site/house tuning

## Fresh Additions - 2026-04-13 Headless Site-Selection Recovery

- The headless broad-site lane is healthier now:
  - Android-style site-selection ordering is now mirrored into the Python runner
  - the broad-house route-row filter is also mirrored, which reduces off-structure noise before scoring
- Current proof:
  - [`../artifacts/bench/headless_answer_samples_20260413/site_selection_probe_20260413_0400.txt`](../artifacts/bench/headless_answer_samples_20260413/site_selection_probe_20260413_0400.txt)
  - headless search now leads with `GD-446`
  - headless context now anchors on `GD-446 / Terrain Analysis` and `GD-446 / Site Assessment Checklist`
- What still needs work:
  - `GD-873` is now gone from the refreshed headless probe, but `GD-333` drainage support still hangs around in the tail
  - long Android ask runs are still slow enough that the direct harness often captures `Generating answer on host GPU...` instead of a completed detail card on these harder house-family prompts
- Immediate next priority:
  - decide whether to further demote residual `GD-333` drainage support in the headless broad-site lane so the fast oracle matches Android more tightly

## Fresh Additions - 2026-04-13 Distribution Source-Probe + Headless Repair

- The non-deterministic source-link proof point is no longer candle-only:
  - [`../artifacts/bench/live_refresh_v11_20260413/distribution_followup_5556_sourceprobe_v1.json`](../artifacts/bench/live_refresh_v11_20260413/distribution_followup_5556_sourceprobe_v1.json)
  - `5556` water-distribution follow-up now verifies:
    - `source_link_verified=true`
    - tapped `GD-553` source opens the expected full guide detail view
    - guide-detail screen correctly hides follow-up controls
    - actual in-thread follow-up still finishes on host GPU in `70.6 s`
- The headless parity runner is viable again for fast scouting:
  - missing seasonal house-site marker crash is fixed
  - broad-site penalties/bonuses now mirror the Java `siteBreadthIntent` logic
  - focused suite is green:
    - `python -m unittest tests.test_mobile_headless_parity -v`
  - focused replay artifact:
    - [`../artifacts/bench/headless_answers_20260413_v2/mobile_headless_answers_headless_focus_20260413_20260413_033721_003089.json`](../artifacts/bench/headless_answers_20260413_v2/mobile_headless_answers_headless_focus_20260413_20260413_033721_003089.json)
- What that changes about priorities:
  1. keep using the headless lane first for construction/water/crafts triage
  2. broad site-selection is still not fully solved off-emulator:
     - the repaired headless lane no longer collapses into `GD-333`
     - it still overweights `GD-094` instead of a stronger `GD-446`-style site-selection context
  3. next retrieval target remains the same:
     - tighten broad siting so both Android and headless runs consistently surface dedicated site-selection material over generic construction sections

## Fresh Additions - 2026-04-13 House Weatherproof Topic Prune

- The next low-risk retrieval cleanup landed in `QueryMetadataProfile`:
  - `weatherproofing` no longer implicitly promotes `roofing` for every cabin-house subsystem prompt
  - roofing topic detection now requires real roof tokens instead of matching the substring `roof` inside `weatherproof`
- Why it matters:
  - wall-weatherproof prompts were still vulnerable to roof-topic bleed even after the starter-build route-spec cleanup
  - this is exactly the kind of small metadata false positive that creates whack-a-mole feeling later if it is not fixed at the detector layer
- Current proof:
  - targeted JVM coverage in `QueryMetadataProfileTest` is green for both roof-weatherproof and wall-weatherproof prompts
- Immediate next priority:
  - capture one cheap live Android `search.start` sanity check for a wall-weatherproof prompt so the narrowed topic footprint is proven on-device, then continue the remaining headless/site-selection cleanup and the detail-thread text-composition fix

## Fresh Additions - 2026-04-13 House Route Prune + Composer Hardening

- The newest pass moved from topic narrowing into route narrowing:
  - roof focus no longer depends on generic `weatherproof` / `seal` substring matches
  - broad roof-support sweep is now reserved for true broad starter-house prompts
  - pure foundation prompts now get a dedicated foundation route spec instead of dragging the full starter-cabin route family
- Current proof:
  - targeted `QueryRouteProfileTest` coverage is green
  - fresh live Android search probes are captured in:
    - `live_refresh_v15_20260413/roof_5554_search_v5.run.txt`
    - `live_refresh_v15_20260413/wall_5556_search_v5.run.txt`
    - `live_refresh_v15_20260413/foundation_5560_search_v3.run.txt`
  - those live probes confirm the narrowed on-device topic footprints:
    - roof -> `[roofing, weatherproofing]`
    - wall-weatherproof -> `[wall_construction, weatherproofing]`
    - foundation -> `[foundation, drainage]`
- Parallel UX cleanup also landed:
  - the detail follow-up composer now disables suggestions/personalized learning and clears composing text before auto-followups, which should reduce the merged/jumbled emulator text issue
- Immediate next priority:
  - trace why the fresh roof/foundation probes still report `routeSpecs=5/6` and trim any remaining hidden house sweep or overlapping focus signal before spending more time on prompt polish
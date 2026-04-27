# Android Migration Backlog - 2026-04-27

Small backlog note for current-head Android migration follow-up after the
matrix proof. Keep this note limited to known blockers and polish items; do not
use it to approve reviewed-card runtime expansion or product exposure.

## Evidence Baseline

- Current-head pack guard runbook and matrix summary:
  [`ANDROID_CURRENT_HEAD_PACK_GUARDS_20260427.md`](./ANDROID_CURRENT_HEAD_PACK_GUARDS_20260427.md)
- Phone portrait proof note:
  [`ANDROID_CURRENT_HEAD_PHONE_UI_STATE_PROOF_20260427.md`](./ANDROID_CURRENT_HEAD_PHONE_UI_STATE_PROOF_20260427.md)
- Four-role state-pack artifact:
  [`../artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/summary.json`](../artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/summary.json)
- Phone portrait focused artifact:
  [`../artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/summary.json`](../artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/summary.json)
- Phone landscape install-probe ANR artifact:
  [`../artifacts/android_current_head_guard_install_probe_5560/summary.json`](../artifacts/android_current_head_guard_install_probe_5560/summary.json)
- Asset-pack promotion evidence: after commits `e00e4e2`, `d6ad218`, and
  `faa7352`, the bundled Android asset pack is the 271-card current-head pack.
  Desktop validation passed
  `tests.test_compare_mobile_pack_counts`,
  `tests.test_mobile_pack_manifest_parity`, and
  `tests.test_mobile_pack_push_cache_contract` (`10` tests OK). Compare helper
  evidence for baseline
  `android-app/app/src/main/assets/mobile_pack` versus candidate
  `artifacts/mobile_pack/senku_current_head_20260426_232032` supports the
  candidate inventory: the baseline asset contains Git LFS pointer
  sqlite/vector files with manifest counts `answer_cards=6`, `chunks=49726`,
  `guides=754`, while the candidate has materialized sqlite assets with
  manifest counts `answer_cards=271`, `chunks=49841`, `guides=754` and sqlite
  counts `answer_cards=271`, `answer_card_clauses=6945`,
  `answer_card_sources=311`, `deterministic_rules=9`. Manifest/sqlite hashes
  match. Count deltas include `answer_cards +265`, `chunks +115`,
  `guide_related_links +7`, and `retrieval_metadata_guides +4`.
- FTS runtime reality check: `artifacts/android_fts_probe_next_20260427_0911`
  confirms the current-head host pack carries both `lexical_chunks_fts` and
  `lexical_chunks_fts4` with `49841` rows each. All four emulator lanes lack
  SQLite FTS5 module support, can create FTS4 tables, and the app runtime falls
  back to `lexical_chunks_fts4`. Treat Android retrieval/performance evidence
  from this matrix as FTS4 fallback evidence, not FTS5 runtime proof.
- Current APK direct fallback proof: on 2026-04-27 10:25 CT,
  `PackRepositoryFtsFallbackAndroidTest` passed `OK (3 tests)` on each fixed
  emulator lane (`5554`, `5556`, `5558`, and `5560`) against the current
  bundled 271-card pack.
- Repeatable FTS fallback wrapper proof: on 2026-04-27 10:39 CT,
  `scripts/run_android_fts_fallback_matrix.ps1` passed on all four fixed
  emulator lanes and wrote
  `artifacts/bench/android_fts_fallback_matrix_current_20260427/summary.json`.
  Summary fields: `passed_count=4`, `failed_devices=[]`, `expected_tests=3`,
  `dry_run=false`, and `runtime_evidence=fts4_fallback`.
- LiteRT transport proof on `emulator-5554`: on 2026-04-27 10:30 CT,
  `scripts/probe_litert_model_transport.ps1` wrote
  `artifacts/bench/litert_transport_probe_5554_e2b_20260427_1028/summary.md`.
  The staged `adb push` plus app-data copy path matched bytes and SHA-256 for
  `256 B` and `64 MB` payloads, while every Windows direct-stream candidate
  failed even for the `256 B` control payload. The real E2B model probe through
  `cmd_redirect_cat` also failed. This confirms the `5554` E2B block is still
  a data-partition/transport constraint, not an app/runtime failure.
- Current-head readiness/direct-guard replay: all four emulators passed the
  271-card pack readiness/direct guard tests (`AnswerCardCurrentHeadPackCensusTest`,
  `AnswerCardRuntimeAllowlistCurrentHeadTest`, `PackMigrationInstallTest`) under
  `artifacts/android_pack_readiness_5554_20260427_085938`,
  `artifacts/android_current_head_pack_readiness_20260427_085935_5556`,
  `artifacts/mobile_pack_readiness_20260427_085933_5558`, and
  `artifacts/mobile_pack_readiness/current_head_pack_readiness_20260427_085944_5560`.
  The first follow-on four-posture state-pack replay passed `45 / 45` with
  `platform_anr_count=0` and APK SHA
  `987acb427b72c95fa6c1ae6a0a3108662e42ec2c1323ae12b876763d62a8ddcd` at
  `artifacts/ui_state_pack_next_android_migration/20260427_090123/summary.json`.
  Treat that artifact as behavioral/UI proof only: it accidentally used an
  explicit E4B host override on `5554`/`5560` while `5556`/`5558` reported
  installed E2B identity, so `matrix_homogeneous=false`. The corrected
  default-primary E2B host rerun also passed `45 / 45` with
  `platform_anr_count=0` at
  `artifacts/ui_state_pack_next_android_migration_e2b/20260427_090550/summary.json`.
  It aligns all device model names to E2B, but strict
  `matrix_homogeneous=false` remains because `5554`/`5560` report host E2B
  identity while `5556`/`5558` report installed-model E2B file identity.
- Current bundled-pack UI acceptance proof: the full four-role host-inclusive
  state pack passed `45 / 45` with `platform_anr_count=0`, homogeneous E2B
  identity, and APK SHA
  `ac25c273b28dc7a7acf77bdc2954d1c8b25230b2d36c179a0bb304b39ca7c24f` at
  `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/summary.json`.
  The prior bundled no-push partial `42 / 45` is superseded for UI acceptance
  by this full proof.

## Current Blockers

- Consolidated host-inclusive state-pack proof: current bundled-pack UI
  acceptance is the `45 / 45` E2B proof at
  `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/summary.json`.
  The rollup reports `matrix_homogeneous=true`,
  `matrix_model_name=gemma-4-e2b-it-litert`, `platform_anr_count=0`, and
  `identity_missing=false` on all four devices.
- `phone_landscape` System UI ANR: resolved for this session after restarting
  `emulator-5560`, reinstalling app/test APKs, and rehydrating the
  current-head pack. The post-restart full `phone_landscape` pack passed
  `11 / 11` with
  `platform_anr=null` under
  `artifacts/ui_state_pack_current_head_phone_landscape_after_handoff_fix/`.
- Tablet linked-guide handoff context assertion: resolved by carrying
  guide-mode handoff context into the tablet Compose state and harnessing those
  fields. Follow-up proof passed on `emulator-5554` and `emulator-5558` under
  `artifacts/android_tablet_handoff_context_fix_5554_final/summary.json` and
  `artifacts/android_tablet_handoff_context_fix_5558_final/summary.json`.
  Post-fix tablet state packs also passed `10 / 10` for `tablet_portrait` and
  `tablet_landscape` under
  `artifacts/ui_state_pack_current_head_tablet_after_handoff_fix/`.
- Phone portrait clipping polish: resolved for the known home-surface issues.
  Search/composer placeholders now ellipsize, and compact-phone linked path
  chips stack vertically instead of exposing half-visible horizontal-scroll
  chips. Focused proof passed under
  `artifacts/android_phone_portrait_home_polish_final/20260427_014631_897/emulator-5556/summary.json`;
  the broader phone portrait pack also passed `11 / 11` under
  `artifacts/ui_state_pack_phone_portrait_polish_20260427/20260427_014311/`.
- Identity/model metadata missing: resolved for harness/state-pack summaries by
  recording a host inference identity source plus stable host model key. Visible
  UI identity polish remains separate from migration proof.
- Tablet on-device LiteRT readiness: partially reopened and narrowed. E2B
  push/readiness now works on `emulator-5558` tablet landscape after adding a
  helper free-space preflight and local model-readiness smoke. Proof passed
  under
  `artifacts/android_litert_model_readiness_5558_landscape_skipinstall_20260427/20260427_081639_614/emulator-5558/summary.json`
  with `model_identity_source=installed_model`,
  `model_name=gemma-4-E2B-it.litertlm`, `identity_cache_hit=true`, and
  landscape screenshot/dump artifacts. `emulator-5554` still cannot use the
  current staged push path for E2B: the helper failed fast with about `3.38 GiB`
  free versus about `4.87 GiB` required. Treat `5554` as an AVD data-size
  blocker, not an app/runtime failure.

## Tooling Queue

- Standardize emulator launch/evidence profiles before adding another runner
  layer. Android's emulator CLI supports headless launches with `-no-window`
  and data partition sizing with `-partition-size`, which maps directly to a
  clean-boot profile, a cached local profile, and a large-asset profile for the
  `5554` E2B blocker:
  <https://developer.android.com/studio/run/emulator-commandline>.
  `scripts/start_senku_emulator_matrix.ps1` now exposes opt-in `-Headless` and
  `-PartitionSizeMb` switches for those profiles; defaults remain unchanged.
  `-WhatIf` output now includes the concrete emulator launch arguments, so
  profile changes can be reviewed without launching a lane.
- adb/platform-tools version capture has landed in Android harness artifacts.
  Android
  documents normal `adb push`/`pull`, screenshot, and screenrecord flows, and
  current Platform Tools also document experimental ADB Burst Mode for large
  transfers. That makes host adb version part of the evidence surface for
  LiteRT/model-transfer work:
  <https://developer.android.com/tools/adb>.
- Evaluate Gradle Managed Devices as a parallel smoke lane after the fixed
  four-emulator harness remains green. The Android Gradle plugin can define
  named devices, groups, parallel group runs, and managed-device sharding, but
  this should not replace the existing screenshot/state-pack matrix until it
  can emit comparable artifacts:
  <https://developer.android.com/studio/test/managed-devices>.
- Treat ATD images as non-visual smoke only. Android documents ATDs as lower
  resource managed devices, but they disable hardware rendering, so they are
  inappropriate for screenshot-sensitive UI proof.
- Keep Android Test Orchestrator as Gradle-run infrastructure, not an assumed
  property of the PowerShell smoke harness. `android-app/app/build.gradle`
  already configures `ANDROIDX_TEST_ORCHESTRATOR`, but
  `scripts/run_android_instrumented_ui_smoke.ps1` builds APKs and then invokes
  `am instrument` directly, so Orchestrator is not active there unless a future
  opt-in harness path installs and drives it explicitly. Android's Orchestrator
  docs are still relevant for targeted flake isolation because it runs each
  test in a separate instrumentation invocation and can clear app data:
  <https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner#orchestrator>.
- Use `build_android_ui_state_pack_parallel.ps1 -PlanOnly` before long
  multi-role sweeps when role filters or host flags changed. The plan mode
  writes a JSON plan with selected roles, device metadata, skip flags, and
  per-role launcher commands without building, installing, starting role jobs,
  or finalizing a pack.
- `run_android_fts_fallback_matrix.ps1` now runs under the normal harness
  controls: resolved SDK adb path, per-device locks for real runs, and
  `stop_android_harness_runs.ps1` coverage.
- Android Gradle dependency verification is enabled and includes the detached
  Android lint tool dependencies, but `:app:lintDebug` is still blocked by
  existing lint/code compatibility findings. Do not treat lint as a green
  validation lane until those findings are triaged separately.

## Stop Lines

- Do not count the earlier ANR-blocked `phone_landscape` lane as proof; use
  the post-restart `11 / 11` artifact as the current clean lane evidence.
- Do not treat tablet linked-guide failures as pack inventory failures without
  fresh retrieval/pack evidence; the known handoff-context regression is fixed
  in the 2026-04-27 tablet proof artifacts above.
- Do not broaden this backlog into tests, scripts, source edits, or runtime
  product policy changes.
- Do not expand reviewed-card product exposure or card coverage from this
  migration evidence.
- Do not retry on-device LiteRT pushes on `emulator-5554` with the staged helper
  until its AVD data partition is enlarged or a non-staging transfer path is
  implemented.
- Do not use `-SkipDataSpaceCheck` on `5554` to force the E2B staged helper;
  the current free-space failure is expected and byte-safe direct streaming is
  not proven.
- Do not replace the fixed four-emulator state-pack evidence with Gradle
  Managed Devices, ATD, Orchestrator, or Firebase Test Lab output until the new
  lane proves equivalent screenshots, UI dumps, summaries, identity metadata,
  and failure artifacts.

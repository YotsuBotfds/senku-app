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
- Fixed-four Android headless state-pack real run acceptance evidence: wrapper
  `artifacts/ui_state_pack_headless_lane/20260427_134620/headless_lane_summary.json`
  validated acceptance; state pack
  `artifacts/ui_state_pack_headless_lane/20260427_134751/summary.json`
  reported `status=pass`, `45 / 45`, `fail_count=0`,
  `platform_anr_count=0`, `matrix_homogeneous=true`,
  `model=gemma-4-e2b-it-litert`, APK SHA
  `ac25c273b28dc7a7acf77bdc2954d1c8b25230b2d36c179a0bb304b39ca7c24f`,
  and adb `37.0.0-14910828`.

## Migration-Proof Checklist

Future Android migration lanes should preserve a compact summary row, not a new
audit. Capture:

- status/pass counts, including total pass/fail/skipped counts and the artifact
  path for the summary.
- `platform_anr_count` plus any per-device ANR/null status.
- `matrix_homogeneous`, `matrix_model_name`, and identity source/missing flags.
- APK SHA for the installed app under test.
- installed-pack metadata, especially manifest/listing source, card/chunk/guide
  counts, and whether the lane used bundled, pushed, or cached pack files.
- FTS evidence label such as `runtime_evidence=fts4_fallback` when retrieval
  timing or behavior depends on Android SQLite capabilities.
- host adb/platform-tools version, recorded as
  `host_adb_platform_tools_version`, for transfer and device-control proof.
- tooling/context preflight artifacts, when used, must mark
  `non_acceptance_evidence=true` and `acceptance_evidence=false`; they can
  explain a planned lane or host setup, but they do not replace the fixed
  four-emulator state-pack evidence.

Reviewer taxonomy for helper evidence:

- `-PlanOnly`: command/role/row preflight only. Expected markers include
  `preflight_only=true`, `plan_only=true`, `non_acceptance_evidence=true`,
  `acceptance_evidence=false`, and, for harness matrix plans,
  `will_touch_emulators=false`.
- `-DryRun`: no-device or no-transfer rehearsal only. Expected markers include
  `dry_run=true`, `status=dry_run_only` where applicable,
  `non_acceptance_evidence=true`, and `acceptance_evidence=false`.
- `-WhatIf`: command review only. Expected markers include `would_run=false`
  or printed launch arguments/profile metadata; it does not run the parity
  compare, start an emulator, or create UI proof.
- Metadata-only: host/tooling or summary-shape context only. Look for
  `metadata_only=true` or explicit non-acceptance posture; these artifacts
  help explain the environment but do not prove device behavior.
- Validator evidence: Android validators prove JSON/Markdown shape,
  stop-line posture, and summary contracts only. They do not launch emulators,
  inspect screenshots/UI dumps for acceptance, or replace fixed four-emulator
  state-pack proof.
- Pack evidence: asset-pack parity, installed-pack metadata, cache/push
  summaries, and manifest/sqlite/vector counts prove inventory and pack
  identity only. They are not screenshot/state-pack UI acceptance.
- FTS fallback evidence: direct and wrapper FTS proofs with
  `runtime_evidence=fts4_fallback` prove Android fallback behavior. Treat
  them as not FTS5 runtime proof and not UI acceptance.
- True UI acceptance: only the fixed four-emulator state-pack lane across
  phone portrait, phone landscape, tablet portrait, and tablet landscape
  proves screenshot/dump UI acceptance for this migration baseline.

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
  free versus about `4.87 GiB` required, and the follow-up large-data launch
  probe showed emulator 36.4.9 rejects CLI `-partition-size 8192` with a
  maximum of `2047` MB. Treat `5554` as a config-based AVD data-size blocker,
  not an app/runtime failure.

## Tooling Queue

- Standardize emulator launch/evidence profiles before adding another runner
  layer. Android's emulator CLI supports headless launches with `-no-window`
  and data partition sizing with `-partition-size`, which maps directly to a
  clean-boot profile, a cached local profile, and a large-asset profile for the
  `5554` E2B blocker:
  <https://developer.android.com/studio/run/emulator-commandline>.
  `scripts/start_senku_emulator_matrix.ps1` now exposes opt-in `-Headless` and
  `-PartitionSizeMb` switches for those profiles; defaults remain unchanged.
  `-WhatIf` output now includes the concrete emulator launch arguments and
  blocked CLI metadata, so profile changes can be reviewed without launching a
  lane. Real launches fail fast above the observed emulator 36.4.9
  `-partition-size` maximum of `2047` MB.
- Launch profile preflight has a named `-LaunchProfile` contract for
  `clean-headless`, `cached-local`, and `large-litert-data`, but it is intentionally
  restricted to `-WhatIf`. The metadata reports profile, headless posture,
  expected serial, requested partition size, and whether the CLI partition
  override is supported without changing launch arguments or starting an
  emulator. Treat it as reviewer context only.
- adb/platform-tools version capture has landed in Android harness artifacts.
  Android
  documents normal `adb push`/`pull`, screenshot, and screenrecord flows, and
  current Platform Tools also document experimental ADB Burst Mode for large
  transfers. That makes host adb version part of the evidence surface for
  LiteRT/model-transfer work:
  <https://developer.android.com/tools/adb>.
  Reviewers should look for `host_adb_platform_tools_version` in harness
  `summary.json` outputs. `run_android_harness_matrix.ps1` and
  `run_android_followup_matrix.ps1` also include it in `summary.md`;
  `run_android_fts_fallback_matrix.ps1` writes it to `summary.json`; state-pack
  summaries roll the first available child value into their `summary.json` /
  compact matrix rollup.
- Tooling version manifest has landed as a metadata-only scaffold:
  `scripts/write_android_tooling_version_manifest.py` records Gradle wrapper,
  Android Gradle Plugin, Kotlin plugin, LiteRT-LM, AndroidX test,
  Orchestrator, and optional host tool versions into JSON/Markdown. Its
  manifest kind is `android_tooling_version_manifest`, with
  `metadata_only=true`, `non_acceptance_evidence=true`, and
  `acceptance_evidence=false`.
- Capture summary validation has landed as a preflight/compatibility scaffold:
  `scripts/validate_android_capture_summary.py` validates canonical capture
  summary fields such as serial, role, orientation, APK SHA, platform-tools
  version, screenshot/UI dump/logcat hashes, optional screenrecord, package
  data posture, model identity, installed-pack metadata, and the required
  non-acceptance evidence posture. It validates shape only; it does not perform
  emulator capture or confer acceptance.
- `run_android_instrumented_ui_smoke.ps1 -CaptureSummaryPath` writes the
  capture-summary handoff consumed by
  `scripts/validate_android_instrumented_capture_summary.py`. Use it for
  artifact-shape compatibility and migration review; it is non-acceptance
  evidence unless the same capture is rolled into the fixed four-emulator
  state-pack proof.
- Evaluate Gradle Managed Devices as a parallel smoke lane after the fixed
  four-emulator harness remains green. The Android Gradle plugin can define
  named devices, groups, parallel group runs, and managed-device sharding, but
  this should not replace the existing screenshot/state-pack matrix until it
  can emit comparable artifacts:
  <https://developer.android.com/studio/test/managed-devices>.
- GMD task inventory is property-gated. Use
  `.\gradlew.bat :app:tasks --all -Psenku.enableManagedDevices=true --console=plain`
  or the managed-device smoke dry run to inspect planned tasks and expected
  roots only; inventory output is not device evidence.
- `run_android_managed_device_smoke.ps1 -DryRun` currently writes a
  non-acceptance helper artifact only. Its `summary.json` / `summary.md`
  shape includes `status=dry_run_only`, `dry_run=true`,
  `non_acceptance_evidence=true`, `acceptance_evidence=false`,
  `planned_command`, `gradle_property`, `task_name`, `expected_devices`,
  `expected_artifact_roots`, `would_launch_emulators=false`,
  `managed_devices_launched=false`, and
  `primary_evidence=fixed_four_emulator_matrix`. Do not treat it as a
  substitute for fixed four-emulator screenshot/state-pack proof.
- `run_android_asset_pack_parity_gate.ps1 -WhatIf` writes a second
  non-acceptance helper summary before any candidate comparison runs. Its JSON
  shape includes `baseline_pack_dir`, `candidate_pack_dir`, `output`,
  `display_command`, `would_run=false`, and `fail_on_mismatch=true`. The
  artifact reviews the parity command and fail-on-mismatch posture only; it is
  validator-compatible with `scripts/validate_android_migration_summary.py` and
  does not replace fixed four-emulator evidence. Real parity-gate summaries are
  also non-acceptance pack evidence, not UI acceptance evidence.
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
- UIAutomator 2.4 beta comparison is now captured as a dry-run-only scaffold,
  not a dependency migration. Use
  `scripts/run_android_uiautomator_24_comparison.ps1 -DryRun` to write the
  planned candidate lane for `androidx.test.uiautomator:uiautomator:2.4.0-beta02`
  against the checked-in `2.3.0` baseline. The summary must keep
  `status=dry_run_only`, `plan_only=true`, `non_acceptance_evidence=true`,
  `acceptance_evidence=false`, `would_modify_gradle_files=false`,
  `would_launch_connected_instrumentation=false`, and
  `primary_evidence=fixed_four_emulator_matrix`. This scaffold records the
  comparison plan only; it does not bump dependencies globally or replace
  fixed four-emulator screenshot/state-pack proof.
- Use `build_android_ui_state_pack_parallel.ps1 -PlanOnly` before long
  multi-role sweeps when role filters or host flags changed. The plan mode
  writes a JSON plan with selected roles, device metadata, skip flags, and
  per-role launcher commands without building, installing, starting role jobs,
  or finalizing a pack. Its `plan.json` includes `preflight_only=true`,
  `plan_only=true`, `non_acceptance_evidence=true`,
  `acceptance_evidence=false`, and `migration_checklist_intent` with the
  selected roles, host model identity, host/skip flags, parallelism, and the
  same non-acceptance posture fields.
- `run_android_headless_state_pack_lane.ps1` wraps the fixed four-emulator
  state-pack path with headless emulator launch profiles. `-PlanOnly` and
  `-WhatIf` write non-acceptance plans only; a real run requires `-RealRun` and
  only counts when the fixed roles all pass through the state-pack summary.
  Validate the lane summary contract with
  `scripts/validate_android_headless_state_pack_lane_summary.py`; the validator
  proves shape/posture only.
- Use `run_android_harness_matrix.ps1 -PlanOnly` for prompt/detail matrix
  preflight. It writes a validator-compatible `summary.json` with
  `plan_kind=android_harness_matrix`, `preflight_only=true`,
  `plan_only=true`, `non_acceptance_evidence=true`,
  `acceptance_evidence=false`, selected rows/postures, runner commands, pack
  push intent, and `will_touch_emulators=false`. It starts no jobs and is not
  acceptance evidence.
- `run_android_fts_fallback_matrix.ps1` now runs under the normal harness
  controls: resolved SDK adb path, per-device locks for real runs, and
  `stop_android_harness_runs.ps1` coverage. It writes both `summary.json` and
  `summary.md`; the markdown rollup is a concise FTS4 fallback reviewer
  surface with `runtime_evidence=fts4_fallback`,
  `not_fts5_runtime_proof=true`, device counts, lock posture, adb version, and
  the paired JSON path.
- `push_litert_model_to_android.ps1 -DryRun` is a no-device preflight. It
  reports the resolved device/package/model target, tmp staging requirement,
  `/data` free-space requirement, `-SkipDataSpaceCheck` posture, and
  `Transfer posture: skipped by -DryRun.` When `-SummaryPath` is provided, it
  writes a machine-readable non-acceptance dry-run summary. It transfers no
  bytes, writes no acceptance artifact, and remains subordinate to fixed
  four-emulator evidence.
- `run_android_litert_readiness_matrix.ps1 -DryRun` is the
  validator-compatible LiteRT readiness preflight. It can run without adb,
  records `status=dry_run_only`, model path/name/bytes/SHA, app-private
  target, `/data` free-space posture, backend/request fields, logcat
  extraction plan, `real_run_status=not_implemented`,
  `primary_evidence=fixed_four_emulator_matrix`, and the stop line that fixed
  four-emulator evidence remains primary.
- `run_android_large_data_litert_tablet_lane.ps1` is the guarded
  `emulator-5554` large-data LiteRT lane. Dry runs are preflight only; real mode
  requires the confirmation token and produces deploy/runtime evidence, not UI
  acceptance, until it is folded into fixed-four state-pack evidence. With the
  current emulator CLI, `-StartEmulator -PartitionSizeMb 8192` writes
  `status=blocked`, `blocked_reason=emulator_cli_partition_size_max_2047`, and
  `required_path=config_based_avd_data_partition` instead of entering an
  unbounded `adb wait-for-device`. Validate its summary with
  `scripts/validate_android_large_data_litert_tablet_lane_summary.py`; the
  validator does not confer emulator/UI acceptance.
- `prepare_senku_tablet_2_large_data_avd.ps1` is the guarded maintenance
  preflight for the required `config_based_avd_data_partition` path. Its default
  dry run reads `Senku_Tablet_2` config identity and plans the config/state
  changes without touching adb, emulator data, snapshots, or UI proof. Apply
  mode requires `-ConfirmPrepare PREPARE_SENKU_TABLET_2_LARGE_DATA_AVD`, checks
  that `emulator-5554` is not running, backs up `config.ini`, and quarantines
  stale userdata/snapshot artifacts before a fresh large-data launch. Validate
  its summary with
  `scripts/validate_senku_tablet_2_large_data_avd_preflight_summary.py`; it is
  AVD-maintenance preflight evidence only.
- `run_android_migration_preflight_bundle.ps1` collects the current migration
  helper preflights into one metadata bundle: tooling versions, managed-device
  dry run, LiteRT readiness dry run, `Senku_Tablet_2` large-data AVD preflight,
  orchestrator dry run, harness-matrix plan, UI state-pack plan, and validator
  command references. Its summary is
  metadata/preflight only with `acceptance_evidence=false`. Validate the bundle
  shape with `scripts/validate_android_migration_preflight_bundle_summary.py`;
  that validation is contract evidence only.
- `run_windows_validation.ps1 -Mode android-migration` invokes
  `run_android_migration_validator_suite.ps1` as the no-emulator Android
  migration validator batch. Treat it as summary/contract validation only:
  it does not launch emulators, inspect UI evidence, or replace fixed
  four-emulator state-pack acceptance.
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

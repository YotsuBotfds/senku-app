# Android Migration Next Real Run - 2026-04-27

Short dispatch for the next real Android migration step after no-emulator
validation. This is an execution order note, not acceptance evidence.

## Sequence

1. Run the no-emulator validator suite and migration preflight bundle first.
   Confirm the validator/preflight summaries explicitly say
   `non_acceptance_evidence=true` and `acceptance_evidence=false`.
2. Run the fixed four headless state-pack real lane across the required
   postures/devices: tablet portrait `5554`, phone portrait `5556`, tablet
   landscape `5558`, and phone landscape `5560`.
3. Before retrying `emulator-5554` large-data LiteRT, run the guarded
   `Senku_Tablet_2` AVD maintenance preflight. Apply it only with the explicit
   confirmation token, after confirming `emulator-5554` is not running.
4. Only after the fixed four real lane passes and the `Senku_Tablet_2` data
   partition is prepared, run the guarded large-data LiteRT real lane on
   `emulator-5554` with the required real-run confirmation token.

## Commands

No-emulator validation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_windows_validation.ps1 -Mode android-migration -VenvPath .venvs\senku-validate
```

Migration preflight bundle:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_migration_preflight_bundle.ps1 -PythonPath .\.venvs\senku-validate\Scripts\python.exe
```

`Senku_Tablet_2` large-data AVD preflight:

```powershell
powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -File .\scripts\prepare_senku_tablet_2_large_data_avd.ps1
```

Headless fixed-four real lane:

```powershell
powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -File .\scripts\run_android_headless_state_pack_lane.ps1 -LaunchProfile clean-headless -HostInferenceUrl http://10.0.2.2:1235/v1 -HostInferenceModel gemma-4-e2b-it-litert -MaxParallelDevices 4 -RealRun
```

Large-data `5554` LiteRT real lane:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_large_data_litert_tablet_lane.ps1 -Device emulator-5554 -PartitionSizeMb 8192 -RealMode -ConfirmRealMode RUN_EMULATOR_5554_LARGE_LITERT_DATA -StartEmulator
```

Current result: blocked on emulator 36.4.9 because CLI `-partition-size 8192`
is rejected above the observed `2047` MB maximum. Treat the command as a
blocked-summary generator until a config-based AVD data-partition path exists.
After that guarded AVD path is prepared, add `-UsePreparedAvdDataPartition` so
the real lane starts `5554` without passing the impossible CLI partition-size
argument.

## Stop Lines

- Validators and preflights prove shape, command posture, and run intent only.
  They are non-acceptance evidence and must not replace emulator/device proof.
- The fixed four headless state-pack lane is the migration UI acceptance lane.
  Do not promote from dry-run, plan-only, or validator-only artifacts.
- The `5554` large-data LiteRT lane is deploy/runtime evidence only until it is
  folded into fixed-four state-pack evidence. Do not force it with
  `-SkipDataSpaceCheck`; add a config-based AVD data-partition path or stop.
- Do not retry the old `5554` staged LiteRT push path without the enlarged
  partition/confirmation posture. The existing blocker is data size/transport
  plus the emulator CLI `2047` MB partition cap, not an app acceptance failure.

## Record

Capture each real artifact path plus pass counts, `platform_anr_count`,
`matrix_homogeneous`, model identity source/name, APK SHA, installed-pack
metadata, and `host_adb_platform_tools_version`.

Reviewer handoff: look for `host_adb_platform_tools_version` in harness
`summary.json` outputs. Harness and follow-up matrix wrappers also emit it in
`summary.md`; FTS fallback emits it in `summary.json`; state-pack summaries roll
the first available child value into the pack `summary.json` / compact matrix
rollup.

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
3. Only after the fixed four real lane passes, run the guarded large-data LiteRT
   real lane on `emulator-5554` with an enlarged data partition and the required
   real-run confirmation token.

## Stop Lines

- Validators and preflights prove shape, command posture, and run intent only.
  They are non-acceptance evidence and must not replace emulator/device proof.
- The fixed four headless state-pack lane is the migration UI acceptance lane.
  Do not promote from dry-run, plan-only, or validator-only artifacts.
- The `5554` large-data LiteRT lane is deploy/runtime evidence only until it is
  folded into fixed-four state-pack evidence. Do not force it with
  `-SkipDataSpaceCheck`; enlarge the AVD partition or stop.
- Do not retry the old `5554` staged LiteRT push path without the enlarged
  partition/confirmation posture. The existing blocker is data size/transport,
  not an app acceptance failure.

## Record

Capture each real artifact path plus pass counts, `platform_anr_count`,
`matrix_homogeneous`, model identity source/name, APK SHA, installed-pack
metadata, and `host_adb_platform_tools_version`.

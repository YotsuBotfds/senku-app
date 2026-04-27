# RAG-A15g Android Migration Tooling Followups

## Current State

- Fixed four-emulator evidence remains primary: `emulator-5554`, `emulator-5556`,
  `emulator-5558`, and `emulator-5560` are still the migration proof surface for
  screenshot/state-pack behavior.
- The Gradle Managed Devices scaffold is an opt-in future parallel smoke lane,
  gated by `-Psenku.enableManagedDevices=true`. It should not replace fixed
  emulator evidence until it emits comparable artifacts.
- `scripts/compare_mobile_pack_counts.py` now has a `--fail-on-mismatch` gate,
  so pack-count drift can fail validation instead of only reporting deltas.
- ADB/platform-tools version metadata is now surfaced in Android harness outputs,
  making host transfer tooling part of the evidence record for model and pack
  movement.

## Next Queue

- Use `scripts/run_android_asset_pack_parity_gate.ps1` when a local promotion
  check needs `compare_mobile_pack_counts --fail-on-mismatch` against the
  expected Android pack location.
- Capture one managed-device smoke artifact behind the opt-in Gradle property
  and compare its fields with the fixed four-emulator harness output.
- Keep `5554` in the required fixed-emulator matrix even while managed-device
  smoke is explored, because tablet portrait evidence still anchors several
  Android migration claims.
- Add a short note to the next Android migration handoff naming where the
  ADB/platform-tools metadata appears in the harness output, so reviewers can
  verify host tooling without re-running the lane.

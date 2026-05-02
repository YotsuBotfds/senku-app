# Latest Android Proofs

Pointer-only rollup for agents. Keep detailed logs in artifacts and test output.

## Current Proofed Head

- Latest full checkpoint proof: `fd9d5240` (`Authorize automation harness launches`)
- Checkpoint time: `2026-05-02T05:13:33-05:00`
- Gates:
  - `:app:testDebugUnitTest` passed
  - `:app:assembleDebug :app:assembleDebugAndroidTest` passed
  - `git diff --check` passed
  - working tree clean

## Recent Live Android Proofs

- `PackMigrationInstallTest` on `emulator-5554`: `OK (3 tests)`, 49.369 s
  - Purpose: clean install, hash-valid/wrong-schema SQLite recovery, and
    hash-valid/stale-vector-header recovery
- `PackMigrationInstallTest` on `emulator-5554`: `OK (2 tests)`, 29.169 s
  - Purpose: clean install plus hash-valid/wrong-schema installed pack recovery
- Live-safe route smoke:
  - Artifact: `artifacts/bench/android_route_smoke/vector_canary_live_rerank_fixed/summary.md`
  - Class: `com.senku.mobile.PackRepositoryCurrentHeadRouteSmokeAndroidTest`
  - Device: `emulator-5554`
  - Result: `passed_count=1`, `failed_count=0`, `timed_out_devices=none`
  - Current smoke class contains bundled/no-vector and vector-pack rain-shelter
    owner canaries.
- Physical phone smoke:
  - Latest nearby artifact family: `artifacts/bench/android_physical_phone_smoke/`
  - Treat individual summaries as smoke evidence, not broad route parity.

## Route Timing Evidence

- Broad route parity timing artifact:
  - `artifacts/bench/route_parity_timing/20260501_233211/route_parity_timing_summary.md`
- Broad route parity is manual/heavy. Do not use it as a quick proof.
- Use `scripts/run_android_route_smoke.ps1` for live route canaries.

## Validation Ladder

- Use `notes/ANDROID_VALIDATION_LADDER.md`.
- Do not run focused Gradle and full Gradle gates concurrently.
- Full functional matrix and broad route parity require explicit intent.

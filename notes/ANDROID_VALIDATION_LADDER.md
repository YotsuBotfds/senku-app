# Android Validation Ladder

Use this ladder for Android work unless a task explicitly asks for a wider proof.

## Per-Commit Proof

- Run focused JVM tests for the touched class or behavior.
- Run `:app:assembleDebugAndroidTest` when Android source or androidTest code changes.
- Run `git diff --check`.
- Do not run focused Gradle tasks at the same time as a full Gradle gate.

## Checkpoint Proof

Use this after a small wave or before handing work back:

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
git diff --check
```

## Live Route Proof

Use the live-safe route smoke for quick device/emulator route proof:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_route_smoke.ps1
```

This runner targets only `PackRepositoryCurrentHeadRouteSmokeAndroidTest`.

## Manual / Heavy Proof

`PackRepositoryCurrentHeadRouteParityAndroidTest` is broad and heavy. Run it only
when specifically validating route parity, with a hard outer timeout, preserved
instrumentation/logcat output, and parsed `SenkuRouteParity` timing breadcrumbs.

Do not tune retrieval after a timeout. Tune only when assertion output and route
artifacts prove a real route regression.

## Explicit-Request Only

- Full functional UX matrix.
- Fixed four-emulator visual/state-pack acceptance.
- Broad route parity.
- Long host/model generation sweeps.

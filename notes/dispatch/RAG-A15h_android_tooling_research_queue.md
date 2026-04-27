# RAG-A15h Android Tooling Research Queue

## Scope

Preserve the fixed four-emulator screenshot/state-pack matrix as Android
migration acceptance evidence. Use the tooling below only as parallel smoke,
diagnostic, or future breadth lanes until they emit equivalent artifacts.

## Queue

- `GMD-1`: make the opt-in Gradle Managed Devices scaffold observable. Record
  exact task names from `:app:tasks --all -Psenku.enableManagedDevices=true`,
  keep the target smoke narrow, and emit repo-shaped `summary.json` fields:
  AGP version, image/API, test counts, failures, and artifact paths.
- `GMD-2`: compare the same narrow instrumentation class on `emulator-5556`
  and a managed phone device. Treat the result as early failure signal, not
  equivalence to the four-posture matrix.
- `HARNESS-1`: keep adb-first screenshot/log capture canonical. Required
  metadata remains adb/platform-tools version, emulator serial, APK SHA,
  package data posture, orientation, and model identity.
- `HARNESS-2`: evaluate UI Automator `2.4` only as a focused wait/screenshot
  spike. Do not turn this into a broad harness rewrite.
- `ORCH-1`: keep Android Test Orchestrator as an opt-in Gradle isolation lane.
  It is useful for focused instrumentation contracts, not warmed long-running
  prompt or state-pack flows.
- `PERF-1`: defer Baseline Profiles and macrobenchmark work until release
  startup or navigation performance is a real question.
- `CI-1`: keep hosted CI expectations modest. Build/JVM/unit checks and maybe
  managed-device smoke are fine; full emulator evidence stays local or
  self-hosted until acceleration and artifacts prove parity.

## Stop Lines

- Do not replace fixed `5554`/`5556`/`5558`/`5560` evidence with Gradle Managed
  Devices, ATD, Orchestrator, Firebase Test Lab, or macrobenchmark output.
- Do not use ATD images for screenshot-sensitive acceptance proof; treat them
  as non-visual smoke only.
- Do not claim retrieval or performance parity from a new lane until it records
  screenshots, UI dumps, logcat, summaries, APK/model identity, posture
  coverage, installed-pack metadata, and failure artifacts comparable to the
  existing matrix.

## Primary References

- Android Gradle Managed Devices:
  <https://developer.android.com/studio/test/managed-devices>
- Android adb:
  <https://developer.android.com/tools/adb>
- Android UI Automator:
  <https://developer.android.com/training/testing/other-components/ui-automator>
- AndroidJUnitRunner and Orchestrator:
  <https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner>
- Baseline Profiles:
  <https://developer.android.com/topic/performance/baselineprofiles/overview>
- Android emulator acceleration:
  <https://developer.android.com/studio/run/emulator-acceleration>

# Wave A Closeout Rerun Failure - 2026-04-18

## Failing task

- `BACK-R-03` - sticky anchor lifecycle reset could not be meaningfully validated in the current Android production build

## What completed before the stop

- `BACK-P-04` landed and the stock bundle now passes manifest parity plus the `emulator-5556` first-install smoke
- `BACK-P-02` rerun artifacts are green in [`../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_02/`](../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_02/)
- `BACK-P-01` rerun artifacts are green in [`../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_01/`](../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_01/)

## Decisive evidence

- Production Android still has anchor prior feature-gated off in [`../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`](../android-app/app/src/main/java/com/senku/mobile/SessionMemory.java):
  - line 22: `ENABLE_ANCHOR_PRIOR = false`
  - lines 192-193: `buildAnchorPriorDirective(...)` returns immediately when that flag is false
- Repo grep shows no production caller that enables the flag; the only setter is the JVM-test helper `setAnchorPriorEnabledForTest(...)`, and its call sites are under `android-app/app/src/test/java/...`
- The intended on-device session harness is also currently broken for this rerun path:
  - running `scripts/run_android_session_flow.ps1` on `emulator-5556` fails immediately with `Quote-AndroidShellArg` not recognized

## Triage hypothesis

- `BACK-R-01`'s tracker/status prose says Android anchor prior is effectively live, but the shipped code path still leaves `ENABLE_ANCHOR_PRIOR` off. That makes the idle-reset scenario non-observable on-device, because there is no active anchor prior to clear.
- Even if we wanted to brute-force the scenario, the current session-flow harness needs a small repair before it can drive the reuse-after-idle lane reliably.

## Stop reason

- Per the rerun instruction, the closeout stops here on the first red in `BACK-R-03`.
- No `BACK-R-03` state-row flip was performed.

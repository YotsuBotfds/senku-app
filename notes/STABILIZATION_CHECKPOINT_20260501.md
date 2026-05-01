# Stabilization Checkpoint - 2026-05-01

Current checkpoint head: `0dc5dfc7`.

This cycle is stabilization, not another extraction wave.

## Allowed Work

1. Current-head validation and concise summary.
2. Emulator/instrumentation stall diagnosis.
3. Read-only Ask/Search shared-gate integration audit.
4. Direct behavior tests for already-used helpers that still lack coverage.
5. Small functional bugs with clear repro and proof.

## Guardrails

- Do not launch a broad extraction wave.
- Do not create new `*Policy`, `*Controller`, or `*Helper` classes unless old
  production logic is removed from a god class, the new class has production
  call sites, focused tests are added, and validation is included.
- Do not tune retrieval unless a golden route test fails and the observed result
  proves a real regression.
- Do not do UI/XML/Compose polish in this cycle.
- Freeze `MainActivity` after the stale-gate/home-refresh fixes unless a compile
  break or concrete stale/harness/exception bug requires a patch.
- Do not run the full functional matrix until emulator/install issues are
  understood.
- Avoid tracker-only commits unless there is truly no code/test slice to bundle.

## Current Tasks

1. Run current-head proof:
   `:app:testDebugUnitTest`, `:app:assembleDebug`,
   `:app:assembleDebugAndroidTest`, and `git diff --check`.
2. Summarize latest commit, proof status, live smoke status, route-parity
   androidTest status, and known blocked items.
3. Diagnose emulator route-parity androidTest timeout without tuning retrieval.
4. Audit `AskQueryController` and `MainSearchController` shared `LatestJobGate`
   integration; patch only a real bypass or missed gate advance.
5. Verify direct-test gaps for production-used helpers, then add behavior tests
   only for the highest-risk uncovered helpers.

## Proof Notes

- Full JVM gate on this stabilization tree:
  `:app:testDebugUnitTest` passed.
- Assembly gates on this stabilization tree:
  `:app:assembleDebug :app:assembleDebugAndroidTest` passed.
- Focused JVM gate:
  `HostInferenceRequestPolicyTest`, `PackInstallValidationPolicyTest`,
  `SpecializedAnchorCandidatePolicyTest`, `QueryMetadataProfileTest`,
  `MainSearchControllerTest`, and `OfflineAnswerEngineTest` passed.
- Formatting gate: `git diff --check` passed.
- Short live PromptHarness smoke on `emulator-5554`:
  `PromptHarnessParsingTest` passed, `OK (3 tests)`.
- Isolated live route proof on `emulator-5554`:
  `PackRepositoryCurrentHeadRouteParityAndroidTest#bundledCurrentHeadPackPreservesRainShelterRouteOwnerLane`
  passed, `OK (1 test)`.

## Integration Notes

- Full route-parity class live runs timed out before assertion output because the
  class still includes a broad route-spec loop that can spend minutes in pack DB
  route queries. Installs and instrumentation startup were separately proven
  healthy by the small live PromptHarness run.
- Rain-shelter proof is intentionally an owner-lane assertion: `GD-345` must
  remain in the top search window and answer-context window for the rain shelter
  query. Retrieval was not tuned to satisfy this test.
- Shared Search/Ask `LatestJobGate` audit found one gate injected into both
  `MainSearchController` and `AskQueryController`; both controllers advance the
  gate before early returns and guard async publication with `isCurrentJob(...)`.

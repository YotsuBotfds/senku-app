# Android Harness Two-Lane Plan

Date: 2026-04-16

Goal:
- move the Android validation stack away from serial-number folklore and shell-timing guesswork
- keep fast local iteration
- make posture claims, artifacts, and device targeting trustworthy

Guiding principles:
- device role must be discovered or named, never inferred from stale emulator ids
- posture claims must be proven by runtime checks, not just requested via flags
- instrumentation should own UI truth; PowerShell should orchestrate, not guess
- fast emulator lanes remain the daily driver; physical devices remain milestone truth checks

## Lane 1: Device Matrix And Posture Truth

Owner scope:
- device role resolution
- role classification facts
- orientation truth
- emulator posture sanity
- smoke preset correctness
- authoritative device-facts resolver shared by harness scripts

Problems this lane closes:
- `emulator-5560` looked like a tablet lane in habit, but is actually a phone-sized AVD
- `-Orientation landscape` could be reported in summary without proving the capture was landscape
- wrappers still encourage humans to think in serials instead of roles

Phase 1 deliverables:
1. Add one shared device-facts resolver/classifier based on actual display size and density.
2. Make the instrumentation smoke runner consume the shared resolver instead of local copy-paste posture math.
3. Fail smoke runs when requested posture and captured artifact dimensions disagree.
4. Emit resolved device facts into run summaries:
   - physical size
   - density
   - smallest-width dp
   - resolved role
   - requested orientation
   - actual artifact orientation
   - mismatch flags

Phase 2 deliverables:
1. Add Gradle Managed Device definitions for named phone/tablet virtual devices.
2. Add wrapper commands that target named Gradle-managed devices instead of ad hoc serials.
3. Retire stale hard-coded serial assumptions from docs and scripts.

Acceptance criteria:
- a "tablet landscape" run cannot silently execute on a phone-sized device
- summary artifacts include enough facts to explain why a run was classified as phone/tablet
- landscape claims fail closed if the screenshot is portrait

## Lane 2: Instrumentation Execution And Artifact Truth

Owner scope:
- instrumentation execution flow
- test runner semantics
- UI artifact capture
- status reporting
- authoritative summary contract
- consumer behavior for wrapper scripts and docs

Problems this lane closes:
- shell wrappers still do too much inference
- instrumentation summaries can be green while posture truth is suspect
- result consumers still need to infer too much from console text

Phase 1 deliverables:
1. Add a structured summary contract with:
   - resolved device role
   - posture verification result
   - artifact verification result
   - pass/fail/no-artifacts/crash status
2. Make instrumentation smoke own screenshot/dump truth checks directly.
3. Make validation-pack and other consumers trust instrumentation summary facts when present instead of redoing duplicate posture recovery.
4. Update docs so the preferred fast path is instrumentation-first and fact-driven.

Phase 2 deliverables:
1. Add Espresso Device or equivalent posture-aware instrumentation helpers where supported.
2. Push more legacy prompt-harness validation onto instrumentation-backed flows.
3. Keep shell fallback only for scenarios instrumentation cannot yet cover.

Acceptance criteria:
- smoke output is machine-readable enough for wrapper scripts to gate on directly
- the fastest recommended path is instrumentation-first and artifact-verified
- shell fallback is clearly secondary

## Recommended First Implementation Pair

Lane 1 first slice:
- implement a shared device-facts resolver/classifier
- make the smoke runner consume that resolver and emit authoritative role/orientation facts

Lane 2 first slice:
- make validation-pack consume instrumentation summary facts authoritatively when present
- update live docs so the trusted path is instrumentation-first and fact-driven

Why this pair first:
- it fixes the exact failure we just hit
- it improves trust without requiring a big Android test rewrite
- it keeps ownership clean between orchestration and execution

## Validation Plan

Fast emulator proofs:
1. run a phone basic smoke through the fact-driven lane
2. run a tablet landscape guide-detail smoke through the fact-driven lane
3. confirm summary facts match actual display dimensions and screenshot orientation

Physical checkpoint:
1. rerun a phone smoke on the physical device after the emulator lanes are stable
2. keep physical tablet as a later checkpoint, not a blocker for the first implementation pair

## Explicit Non-Goals For This Slice

- full Macrobenchmark lane
- full Gradle Managed Device migration
- broad UI test rewrites unrelated to posture/device truth

# Slice S2 — Stage 2 RC validation sweep

- **Role:** main agent (`gpt-5.4 xhigh`). Delegates to `gpt-5.4 high`
  worker for the rollup per `notes/SUBAGENT_WORKFLOW.md` and the
  inline tags below.
- **Serial after:** S1 (Stage 1 RC v3 packet rebuild).
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → Queued S2, promote to
  Active on S1 landing.

## Preconditions (HARD GATE — STOP if violated)

1. S1 landed GREEN, then S1.1 re-parity landed GREEN. The authoritative
   rollup is at
   `artifacts/cp9_stage1_reparity_20260419_183440/pack_build.json`
   (NOT the original S1 dir `cp9_stage1_20260419_181929`, which has
   `apk_sha_homogeneous: false`). The reparity rollup must show:
   `apk_sha_homogeneous: true`,
   `apk_sha: ["88d0041e942a2be8723d8d1489a2421c2fe271d8919c0cbbe52eccbd0e9d9a18"]`,
   `pack_sha_homogeneous: true`,
   `model_sha_homogeneous_on_device: true`,
   `host_inference_serials: ["5554", "5558"]`,
   `landscape_phone_scope_cut: false`,
   `matrix_homogeneous: true`.
2. All four serials have the RC v3 pack installed — the four
   `pack_install_<serial>.json` files in the reparity dir show
   `installed_ok: true` and `badge_observed: true`.
3. A1b (`9cf405c`) is in the build under test — the
   `PromptHarnessSmokeTest` `pressBack()` harness fix must be
   present for the 5560 landscape Wave B capture to succeed. The
   instrumented smoke runs in S1.1 reinstalled the test APK on all
   four serials, so this is satisfied; do not re-verify by hand.

If any gate fails, STOP and report back. Do NOT run the sweep against
a non-homogeneous or A1b-less matrix.

## Outcome

- Full Wave B prompt suite (confident / uncertain_fit / abstain +
  safety-critical escalation) executed against the RC v3 packet on
  all four serials.
- Fresh visual state-pack gallery analogous to the 20260417 baseline,
  at `artifacts/external_review/ui_review_<YYYYMMDD>_gallery/`.
- Per-serial pass rate and explicit `wave_b_coverage.json`.
- Stage 2 verdict (GREEN / RED) at
  `artifacts/cp9_stage2_<ts>/summary.md`.

## The sweep (subagent directive inline per step)

### 1. UI validation pack across four serials

**Dispatch 4 parallel `gpt-5.4 high` workers, one per emulator
serial.** This step is the slice's parallel-fan-out lane — do not
collapse to sequential main-inline. `run_android_ui_validation_pack.ps1`
is not internally parallel across serials, but the four emulators are
independent devices, so fanning out compresses wall time roughly 4×.

Each worker runs `scripts/run_android_ui_validation_pack.ps1` against
exactly one serial in {`5556`, `5560`, `5554`, `5558`} with the
default Wave B prompt set, and writes its per-serial output to
`artifacts/cp9_stage2_<ts>/validation_<serial>/`. Workers run
concurrently; emulator state is independent across serials so there
is no shared-state conflict.

If for any reason you cannot dispatch parallel workers (rate cap,
SDK error, etc.), report the reason in your final response and run
the four serials sequentially in the main lane rather than skipping
any serial.

Serial expectations:
- `5556` (phone portrait, on-device E4B): full Wave B coverage.
- `5560` (phone landscape, on-device E4B): full Wave B coverage; the
  A1b `pressBack()` path is exercised by every Wave B prompt that
  surfaces the follow-up EditText.
- `5554` / `5558` (tablet portrait / landscape, host-inference under
  the documented scope cut): full Wave B coverage via host-inference.

### 2. Visual state-pack gallery
*(main inline — sequential after step 1; shares emulator state)*

Run `scripts/build_android_ui_state_pack_parallel.ps1` to produce
the four-lane screenshot + dump sweep. Confirm that each per-serial
`summary.json` contains the `apk_sha`, `model_name`, `model_sha`
fields P1 landed, and that the pack-level rollup shows
`matrix_homogeneous: true` (on-device) or `matrix_homogeneous: false
+ host_inference_serials: [...]` consistent with S1's rollup.

Copy or publish the resulting artifacts to
`artifacts/external_review/ui_review_<YYYYMMDD>_gallery/`. This
gallery becomes the new baseline S3 will point `AGENTS.md` at.

### 3. Wave B coverage rollup

From the per-serial validation outputs, write
`artifacts/cp9_stage2_<ts>/wave_b_coverage.json`:

```json
{
  "serials": {
    "emulator-5556": {
      "confident": {"count": N, "passed": N, "failed": 0},
      "uncertain_fit": {"count": N, "passed": N, "failed": 0},
      "abstain": {"count": N, "passed": N, "failed": 0},
      "safety_critical_escalation": {"count": N, "passed": N, "failed": 0}
    },
    "emulator-5560": { ... },
    "emulator-5554": { ... },
    "emulator-5558": { ... }
  },
  "inference_path": {
    "on_device": ["emulator-5556", "emulator-5560"],
    "host_inference": ["emulator-5554", "emulator-5558"]
  },
  "total_passed": N,
  "total_failed": N,
  "rc_blocking_failures": []
}
```

**Any safety-critical escalation failure on any serial is
RC-blocking** — the escalation sentence is the non-negotiable Wave B
contract. List such failures by serial + query in
`rc_blocking_failures`.

### 4. Stage 2 summary + verdict
*(main inline — judgment call on verdict)*

Write `artifacts/cp9_stage2_<ts>/summary.md` with:
- Matrix-level and per-serial pass rates.
- Any failed cases, each linked to the underlying artifact.
- Diff vs. the 20260417 baseline gallery — note any regressions.
- Stage 2 verdict: GREEN (all four serials pass with zero
  safety-critical escalation failures) or RED (with named blockers
  and artifact pointers).

## Acceptance

- `summary.md`, `wave_b_coverage.json`, `validation_<serial>/` for
  all four serials, and the new gallery all present under
  `artifacts/cp9_stage2_<ts>/` and
  `artifacts/external_review/ui_review_<YYYYMMDD>_gallery/`.
- Zero safety-critical escalation failures. Other failure classes
  can be triaged; escalation cannot.
- Stage 2 verdict is GREEN and unblocks S3 (CP9 closure + RC cut).

## Boundaries

- No code commits during the sweep.
- No APK / pack / model changes — the matrix under test is the one
  S1 installed.
- If Stage 2 fails, STOP after the summary. Do not attempt
  remediation in this slice — hand back to planner for root cause
  + slice selection.

## Report format

Reply with:
- Paths to `summary.md`, `wave_b_coverage.json`, and the new gallery
  `index.html` (or equivalent).
- Per-serial pass rate (one table).
- Any regression vs. the 20260417 baseline gallery.
- Stage 2 verdict.
- Delegation log: which lane ran each step and a one-line "why."

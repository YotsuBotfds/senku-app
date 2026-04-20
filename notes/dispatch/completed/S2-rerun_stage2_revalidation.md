# Slice S2-rerun — Stage 2 RC re-validation (post R-cls/R-eng/R-pack/RP1)

- **Role:** main agent (`gpt-5.4 xhigh`). Delegates to `gpt-5.4
  high` workers for Step 1 fan-out per `notes/SUBAGENT_WORKFLOW.md`
  and the imperative directive in Step 1.
- **Serial after:** RP1 (APK rebuild + four-serial re-provision).
  RP1 must be landed before this slice runs.
- **Predecessor context:** S2 returned RED with 8/20 actual Wave B
  contract pass rate. Three remediations landed (R-cls `e07d4e7`,
  R-eng `1f76ccf`, R-pack `bd84835`). RP1 brought the matrix to a
  fresh APK + pack; rollup at
  `artifacts/cp9_stage1_rcv4_20260419_214851/pack_build.json` with
  built APK sha `389d8d0f77158a89fbcd274f3fc48afe2019b3785f2b06d0b359e6913e915cbb`
  and new pack sha `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`.
  S2-rerun confirms the broader Wave B contract is back to GREEN
  before S3 unblocks.

## Preconditions (HARD GATE — STOP if violated)

1. RP1 landed GREEN. `artifacts/cp9_stage1_rcv4_20260419_214851/pack_build.json`
   must show:
   - `apk_sha_homogeneous: true`
   - `apk_sha: ["389d8d0f77158a89fbcd274f3fc48afe2019b3785f2b06d0b359e6913e915cbb"]`
     (NOT `88d0041e...` — that is the stale pre-remediation APK)
   - `pack_sha: "e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc"`
   - `pack_sha_homogeneous: true`
   - `model_sha_homogeneous_on_device: true`
   - `host_inference_serials: ["5554", "5558"]`
   - `matrix_homogeneous: true`
2. All four serials have the new pack installed — the four
   `pack_install_<serial>.json` files in
   `artifacts/cp9_stage1_rcv4_20260419_214851/` show
   `installed_ok: true` and `badge_observed: true`.
3. R-cls (`e07d4e7`) + R-eng (`1f76ccf`) + R-pack (`bd84835`) are
   all ancestors of HEAD. Verify with `git merge-base --is-ancestor
   <sha> HEAD` if needed.

If any gate fails, STOP and report. Do NOT run the sweep against
a non-homogeneous or pre-remediation matrix.

## Outcome

- Same Wave B prompt suite S2 used (5 prompts × 4 serials), now
  re-run against the fixed matrix.
- Per-serial pass rate against the actual Wave B contract (NOT
  just wrapper pass rate — see notes about S2's wrapper-vs-actual
  gap).
- Fresh visual state-pack gallery refreshing the 2026-04-19 pack.
- Stage 2 verdict (GREEN / RED) at
  `artifacts/cp9_stage2_rerun_<ts>/summary.md`.

## The sweep

### Step 1 — UI validation pack across four serials

**Dispatch 4 parallel `gpt-5.4 high` workers, one per emulator
serial.** This step is the slice's parallel-fan-out lane — do not
collapse to sequential main-inline. `run_android_ui_validation_pack.ps1`
is not internally parallel across serials, but the four emulators
are independent devices, so fanning out compresses wall time
roughly 4×.

Each worker runs `scripts/run_android_ui_validation_pack.ps1`
against exactly one serial in {`5556`, `5560`, `5554`, `5558`}
with the same Wave B prompt set S2 used (the script's default
suite — verify by reading the script if you want to confirm
prompt parity with S2). Each writes its per-serial output to
`artifacts/cp9_stage2_rerun_<ts>/validation_<serial>/`.

Workers run concurrently; emulator state is independent across
serials so there is no shared-state conflict.

If for any reason you cannot dispatch parallel workers (rate cap,
SDK error), report the reason in your final response and run the
four serials sequentially in the main lane rather than skipping
any serial.

### Step 2 — Visual state-pack gallery

*(main inline — sequential after Step 1; shares emulator state)*

Run `scripts/build_android_ui_state_pack_parallel.ps1` to produce
the four-lane screenshot + dump sweep.

NOTE: S2 hit a PowerShell type mismatch in
`build_android_ui_state_pack.ps1`'s finalization step and Codex
worked around it by reconstructing the manifest from emitted role
manifests. If you hit the same error, follow the same workaround
(reconstruct from role manifests) and flag in your report. Do NOT
fix the script in this slice — that's a post-RC tooling slice.

Confirm that each per-serial `summary.json` contains the
`apk_sha`, `model_name`, `model_sha` fields P1 landed. Verify the
new APK sha is reflected (proves the build under test is the
post-remediation one).

Copy or publish the resulting artifacts to
`artifacts/external_review/ui_review_<YYYYMMDD>_gallery/` with a
distinct name from S2's gallery
(`ui_review_20260419_gallery/`) — use today's date or a `_v2`
suffix to avoid clobbering. This becomes the new baseline S3 will
bump `AGENTS.md` to reference.

### Step 3 — Wave B coverage rollup

From the per-serial validation outputs, write
`artifacts/cp9_stage2_rerun_<ts>/wave_b_coverage.json` in the same
shape as S2's:

```json
{
  "generated_at_utc": "...",
  "stage_root": "...",
  "wrapper_validation": {
    "passed": ...,
    "failed": ...,
    "total": ...,
    "note": "Wrapper-vs-actual gap is a known post-RC tooling issue; planner relies on the per-serial actual contract pass rate below."
  },
  "serials": {
    "emulator-5556": {
      "prompt_pass_rate": {"passed": N, "failed": N, "total": 5},
      "confident": {"count": 1, "passed": ..., "failed": ...},
      "uncertain_fit": {"count": 2, "passed": ..., "failed": ...},
      "abstain": {"count": 2, "passed": ..., "failed": ...},
      "safety_critical_escalation": {"count": 2, "passed": ..., "failed": ...},
      "failed_queries": [...]
    },
    "emulator-5560": { ... },
    "emulator-5554": { ... },
    "emulator-5558": { ... }
  },
  "inference_path": {
    "on_device": ["emulator-5556", "emulator-5560"],
    "host_inference": ["emulator-5554", "emulator-5558"]
  },
  "total_passed": ...,
  "total_failed": ...,
  "rc_blocking_failures": [...]
}
```

**Score against the actual Wave B contract**, not just the
wrapper. The S2 wrapper said 20/20 when actual was 8/20 because
the wrapper only checks for detail-body capture and follow-up
input, not the rendered Wave B mode contract. Inspect each
prompt's rendered mode (paper card vs uncertain_fit vs abstain
vs safety escalation) against the expected mode for that prompt
case_id. The S2 summary at
`artifacts/cp9_stage2_20260419_185102/summary.md` documents the
expected mode for each Wave B prompt.

**Any safety-critical escalation failure on any serial is
RC-blocking** — list such failures by serial + query in
`rc_blocking_failures`.

### Step 4 — Stage 2-rerun summary + verdict

*(main inline — judgment call on verdict)*

Write `artifacts/cp9_stage2_rerun_<ts>/summary.md` with:
- Matrix-level and per-serial actual Wave B contract pass rates
  (NOT wrapper pass rates).
- The three previously-failing prompts' new outcomes
  (`confident_rain_shelter`, `abstain_violin_bridge_soundpost`,
  `safety_abstain_poisoning_escalation`) — explicitly named.
- Diff vs the 2026-04-19 baseline gallery
  (`ui_review_20260419_gallery/`) — note any regressions or
  improvements.
- Stage 2-rerun verdict:
  - **GREEN** if all four serials pass the actual Wave B
    contract (20/20) AND zero safety-critical escalation
    failures.
  - **RED** otherwise, with named blockers and artifact pointers.

## Acceptance

- `summary.md`, `wave_b_coverage.json`, `validation_<serial>/`
  for all four serials, and the new gallery all present under
  `artifacts/cp9_stage2_rerun_<ts>/` and the gallery's external
  review path.
- The three previously-failing prompts now route to their
  expected modes on all four serials.
- Zero safety-critical escalation failures.
- Stage 2-rerun verdict is GREEN and unblocks S3.

## Boundaries

- No code commits during the sweep.
- No APK / pack / model changes — the matrix under test is the
  one RP1 installed.
- If Stage 2-rerun fails, STOP after the summary. Do not attempt
  remediation in this slice — hand back to planner for root cause
  + slice selection.
- Do not edit any tracker / queue / dispatch markdown.

## Report format

Reply with:
- Path to `summary.md`, `wave_b_coverage.json`, and the new
  gallery `index.html` (or equivalent).
- Per-serial actual Wave B contract pass rate (one table).
- Per-prompt status for the three previously-failing prompts.
- Any regression vs the 2026-04-19 baseline gallery.
- Stage 2-rerun verdict.
- Whether the gallery script's finalization mismatch reproduced
  (yes/no, and which workaround was used if yes).
- Delegation log: which lane ran each step and a one-line "why."

# Tablet Host Inference Scope Note

Stable reference. Original version landed in CP9 Stage 0 v6 at
`artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_tablet_host_fallback.md`.
Promoted to `notes/` on 2026-04-19 (CLI Claude planner) so downstream
docs can link to a stable path instead of a dated artifact dir.

**Current applicability (as of 2026-04-19, RC v3 packet build):** the
cut described below is still active. S1
(`artifacts/cp9_stage1_20260419_181929/pack_build.json`) and the
S1.1 reparity rollup
(`artifacts/cp9_stage1_reparity_20260419_183440/pack_build.json`)
both carry `host_inference_serials: ["5554", "5558"]`. Tablet
on-device LiteRT remains gated on `BACK-P-06` (AVD data-partition
sizing).

---

## Tablet Host Fallback Scope Note

Date: 2026-04-19

Scope cut:
- `emulator-5554` and `emulator-5558` are using host inference instead of an on-device LiteRT model for the remainder of CP9 Stage 0 v6.

Why this cut was taken:
- The tablet-portrait AVD data partition is too small to hold the required model copy path.
- The original helper stages through `/data/local/tmp` and then copies into app data, which transiently needs two full copies.
- The direct-stream fallback attempt on `emulator-5554` also failed to land a valid on-device model during this session, so the tablet lane remains storage-constrained under the current AVD layout.

Why this still validates the acceptance target:
- The Stage 0 smoke query targets Wave B safety-critical escalation behavior.
- For this query, the escalation text is emitted deterministically in the abstain / uncertain-fit path before any generative model output is needed.
- Enabling host inference clears the strict `runAsk` gate and allows the tablet postures to exercise the same Wave B routing and escalation surface during the Step 6 smoke.

Carry-over:
- `BACK-P-06`: increase `hw.dataPartition.size` for the tablet AVD definitions so `emulator-5554` and `emulator-5558` can host the required on-device model tier without relying on host fallback.
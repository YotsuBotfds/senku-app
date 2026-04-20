# Slice S1 — Stage 1 RC v3 packet rebuild

- **Role:** main agent (`gpt-5.4 xhigh`). Owns delegation per
  `notes/SUBAGENT_WORKFLOW.md`.
- **Serial after:** A1 (CP9 Stage 0) and P2 (Stage 1 preflight).
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → "S1 — Stage 1 RC v3 packet
  rebuild"

## Preconditions (HARD GATE — check before doing anything)

1. CP9 Stage 0 has landed GREEN or partial-GREEN with scope cuts
   explicitly documented. Either:
   - `notes/CP9_ACTIVE_QUEUE.md` "A1 Stage 0" row is in the Completed
     section with any scope cuts (tablet host-inference fallback,
     landscape-phone harness-capture blind spot) enumerated there, OR
   - The latest `artifacts/cp9_stage0_*/summary.md` shows per-serial
     `status = pass` (including host-inference passes for tablet
     serials and any partial-GREEN entries for the landscape phone
     with their `apk_deploy_v6/scope_note_*.md` files referenced).
   - Hard gate: `apk_sha` matches across all four serials.
     `model_sha` may legitimately differ between on-device inference
     serials and host-inference serials — host-inference has no
     on-device model by definition and that asymmetry is accepted
     under the documented tablet scope cut.

2. The most recent `artifacts/cp9_stage1_preflight_*/preflight.json`
   has either:
   - `ready_for_build == true`, OR
   - `ready_for_build == false` with the blocker list consisting
     solely of the docs/env drift the planner reclassified in
     `notes/CP9_ACTIVE_QUEUE.md` Completed log
     (2026-04-19 evening — "P2 Stage 1 preflight landed"
     entry) — specifically the missing
     `venv/Scripts/Activate.ps1` path on Windows when
     `venv/bin/Activate.ps1` exists and the catalog / ingest stats
     are clean.

   The exception applies because the blocker is a PowerShell path
   convention, not a data-integrity issue: the preflight run
   (`2026-04-19T17:26:05`) recorded `ingest.py --stats` returning
   `754 guides / 14784 chunks` consistent with the tracker, catalog
   drift `[]`, and a functional Python environment via the existing
   `venv/bin/Activate.ps1` or a Windows-specific venv per P3's
   `AGENTS.md` note. If the preflight's blocker list contains
   anything beyond that single path-convention entry, STOP and
   report — the exception is narrow.

If either gate fails, STOP and report. Do not build.

## Outcome

A freshly-built RC v3 mobile pack installed on all four emulator serials,
with `PACK READY` observed on each and `pack_sha` matched across the
matrix.

## The build (subagent directive inline per step)

1. **Stock bundle regeneration** *(main inline — script invocation
   with stdout capture; critical path)*.
   Activate `venv`. Run the canonical stock-bundle build (check
   `guideupdates.md` and `AGENTS.md` for the current command).
   Capture stdout to `artifacts/cp9_stage1_<ts>/stock_build.log`.

2. **Mobile pack export** *(main inline — sequential after step 1)*.
   Run `python scripts/export_mobile_pack.py`. Capture `pack_sha`
   and pack file size.

3. **Four-serial install** *(main inline — device state across
   serials is the critical path; do not delegate even though there
   are four parallel ops)*.
   For each of `5556`, `5560`, `5554`, `5558`:
   - Run `scripts/push_mobile_pack_to_android.ps1` against the serial.
   - Use the instrumented UI smoke lane to confirm the `PACK READY`
     badge appears — do NOT reinvent the confirmation path.
   - Write `artifacts/cp9_stage1_<ts>/pack_install_<serial>.json`:
     ```json
     {
       "serial": "...",
       "pack_sha": "...",
       "installed_ok": true,
       "badge_observed": true,
       "timestamp": "..."
     }
     ```

4. **Roll up** *(main inline — trivial JSON write)*.
   Write `artifacts/cp9_stage1_<ts>/pack_build.json`:
   ```json
   {
     "pack_sha": "...",
     "serials": ["5556", "5560", "5554", "5558"],
     "apk_sha_homogeneous": true,
     "model_sha_homogeneous_on_device": true,
     "host_inference_serials": ["5554", "5558"],
     "landscape_phone_scope_cut": false,
     "artifacts": [...]
   }
   ```

   `landscape_phone_scope_cut` is `false` — A1b landed GREEN on 5560
   landscape (commit `9cf405c`). Set
   `model_sha_homogeneous_on_device` to `true` iff 5556 and 5560
   share one `model_sha` (they should; both run on-device from the
   same pushed file).

## Boundaries

- No code commits in this slice. Packet build is artifact-only.
- No changes to the APK. The debug APK from Stage 0 stays on-device.
- If any serial fails install or badge check, STOP. Do not try Stage 2.
  Report the failure and let Opus plan the retry.

## Acceptance

- `pack_build.json` exists with `matrix_homogeneous: true`.
- All four `pack_install_<serial>.json` have both `installed_ok: true`
  and `badge_observed: true`.
- `git status` shows only new `artifacts/` paths (or clean if artifacts
  are untracked).

## Delegation note

All steps are main inline per the tags above. Device state and commit
coupling keep the work on the critical path. No read-only scouting
step to punt to Spark xhigh; no bounded implementation step that
better fits `gpt-5.4 high` worker. This is deliberate for S1 and
matches `notes/SUBAGENT_WORKFLOW.md`'s "judgment step or dependent on
my immediate state → handle inline" guidance.

## Report format

Reply with:
- `pack_sha`.
- Four install timestamps.
- Path to `pack_build.json`.
- Any anomaly (e.g. one serial took two tries).
- Delegation log.
- Explicit "ready for S2" flag or "S2 blocked — reason".

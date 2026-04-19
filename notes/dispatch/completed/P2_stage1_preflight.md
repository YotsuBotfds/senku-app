# Slice P2 — Stage 1 preflight

- **Role:** main agent (`gpt-5.4 xhigh`). Owns delegation per
  `notes/SUBAGENT_WORKFLOW.md`.
- **Parallel with:** A1 (CP9 Stage 0 Step 6j v6) and P1.
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → "P2 — Stage 1 preflight"

## Preconditions

- None — read-only audit by design.
- Do NOT run ingest for real. `python ingest.py --stats` is safe (it
  reports counts without re-embedding). `python ingest.py` without
  `--stats` rebuilds the index and is OUT OF SCOPE.

## Outcome

A `preflight.json` artifact that tells us, at a glance, whether Stage 1
can begin the moment Stage 0 closes — or whether there's drift we need
to resolve first.

## The audit

1. **Enumerate RC v3 packet inputs.** Read `mobile_pack.py` and
   `scripts/export_mobile_pack.py`. For every input file or directory
   they reference (guide corpus, catalog, manifest template, config),
   capture: path, file_count, combined sha256.

2. **Catalog integrity.** Read `guide_catalog.py`. Extract the full
   guide_id / slug list. Diff against the Wave B closure claims in
   `notes/REVIEWER_BACKEND_TRACKER_20260418.md` (search for the guide
   ids referenced under BACK-U-01, BACK-U-02, BACK-U-03 in the tracker's
   "Active Findings From Initial Audit" section). Flag any orphaned or
   stale ids.

3. **Ingest stats (read-only).** Activate `venv` (PowerShell:
   `. .\venv\Scripts\Activate.ps1`). Run `python ingest.py --stats`.
   Capture stdout. Confirm counts are plausible against the tracker's
   claims.

4. **Write artifact.** Create
   `artifacts/cp9_stage1_preflight_<YYYYMMDD_HHMMSS>/preflight.json`:

   ```json
   {
     "inputs": [
       {"path": "...", "file_count": N, "sha256": "..."}
     ],
     "catalog": {"count": N, "drift": ["..."]},
     "ingest_stats": {"...": "..."},
     "ready_for_build": true,
     "blockers": []
   }
   ```

   `ready_for_build` is `true` iff `catalog.drift == []` and
   `blockers == []`.

## Boundaries

- Read-only. No writes outside `artifacts/cp9_stage1_preflight_*/`.
- No emulator, no APK, no device state.
- No remediation. If drift is found, flag it in `blockers` — do NOT fix
  it in this slice.

## Acceptance

- `preflight.json` exists and validates as JSON.
- `ready_for_build` is a boolean that accurately reflects the flag
  conditions above.
- No changes to any file outside the new `artifacts/` subdirectory.
- `git status` shows only the new artifact path (or, if you prefer to
  leave the artifact untracked, `git status` stays clean).

## Delegation hints

(Suggestions only.)

- Step 1 (enumerate inputs) and Step 2 (catalog diff) are pure reads.
  Strong Spark scout candidates.
- Step 3 needs `venv` activation and script execution. Main agent or
  `gpt-5.4 high` worker — not Spark.
- Step 4 is a JSON write + sanity check. Main agent inline is fine.

## Report format

Reply with:
- Path to `preflight.json`.
- `ready_for_build` value.
- List of blockers if any.
- Catalog drift list if any.
- Delegation log: which lane ran each step and a one-line "why."

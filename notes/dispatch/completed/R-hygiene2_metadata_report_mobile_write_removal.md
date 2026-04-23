# Slice R-hygiene2 — remove `metadata_validation_report.json` writes from mobile-pack-dir sites

- **Role:** main agent (`gpt-5.4 xhigh`). Two production files, one commit. No worker fanout; no scout audit (single-concept, behavior-neutral hygiene).
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** Anything except R-track1 (conflicts on `CP9_ACTIVE_QUEUE.md` tracker edits). Can run concurrently with any Android-side or desktop-test slice.
- **Predecessor context:** R-hygiene1 carry-over in `CP9_ACTIVE_QUEUE.md` (lines 96-103) flagged that `mobile_pack.py` / `scripts/export_mobile_pack.py` may still write `metadata_validation_report.json` into the mobile-pack assets dir. R-hygiene1 handled the file-level ignore/delete; this slice removes the write paths themselves. R-pack-drift1 (landed `7c2c46c`) ruled out `scripts/refresh_mobile_pack_metadata.py` as the drift smoking gun, so the earlier timing concern about touching that script mid-investigation is resolved.

## Pre-state (verified by planner 2026-04-22)

Four total writers of `metadata_validation_report.json` in the repo:

| Writer | Site | Output target | Disposition |
| --- | --- | --- | --- |
| `ingest.py:935` | `write_validation_report(metadata_report, METADATA_VALIDATION_REPORT_PATH)` | desktop DB dir | **KEEP** — desktop ingest artifact, legitimate |
| `mobile_pack.py:1451` | `write_validation_report(metadata_report, metadata_report_path)` | mobile pack `output_dir` | **REMOVE** — pack assets dir should not ship validation reports |
| `scripts/refresh_mobile_pack_metadata.py:244` | `write_validation_report(metadata_report, output_dir / METADATA_VALIDATION_REPORT_FILENAME)` | mobile pack `output_dir` (same dir as above) | **REMOVE** — same noise, in-place refresh path |
| `scripts/run_mobile_headless_preflight.py:327` | `write_validation_report(metadata_report, output_dir / METADATA_VALIDATION_REPORT_FILENAME)` | preflight `output_dir` (may be separate report dir, NOT pack dir) | **OUT OF SCOPE** — evaluate in follow-up if needed; preflight tool's core job is producing reports |

Test coverage for the removed writes:
- `tests/test_ingest.py:139` is the ONLY test that references the report filename. It checks the desktop ingest write (KEEP lane). No mobile-pack tests reference the report file.
- Removing the two mobile-side writes will not break any existing test.

Error-check behavior must be preserved: both `mobile_pack.py` (lines 1452-1453) and `refresh_mobile_pack_metadata.py` (lines 248-249) call `report_has_errors(metadata_report)` and raise/exit if true. This check must stay. Only the `write_validation_report(...)` call and its now-unused path binding / imports are removed.

R-pack-drift1 §6 recommendation (from `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md`): adopt `cf449ee9...` forward, **do not** re-provision to `e48d3e1a`. Fold this into the tracker.

CABIN_HOUSE marker "dead-marker" claim in the post-RC tracked list is WRONG (planner verified 2026-04-22): `guide.title` feeds `core_text` at `mobile_pack.py:2003-2011` via `_normalized_match_text` (lowercased), and `guides/shelter-site-assessment.md:4` title "Shelter Site Selection & Hazard Assessment" matches the marker at `mobile_pack.py:600` verbatim post-lowercase. Strike the claim without code change.

## Scope

- **One commit** modifying two production files plus tracker updates.
- Production: remove the mobile-pack-dir `write_validation_report(...)` calls at `mobile_pack.py:1451` and `scripts/refresh_mobile_pack_metadata.py:244`, along with their now-unused local bindings and module imports.
- Tracker: in-slice edits to `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md` per in-slice cadence.
- **NO** change to `ingest.py` (desktop-side, legitimate).
- **NO** change to `scripts/run_mobile_headless_preflight.py` (scope-deferred; different output-dir semantics).
- **NO** test additions (no existing test covers the removed writes; the removal is behavior-neutral for the validation error path).

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `7c2c46c` (R-pack-drift1 investigation) or a later commit. Verify with `git log -1 --format=%H`.
2. `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` exists and §6 is readable (you'll fold its recommendation into the tracker).
3. `git log --oneline 7c2c46c..HEAD -- mobile_pack.py scripts/refresh_mobile_pack_metadata.py` returns empty output. If non-empty, a later slice has already touched these files; STOP and reconcile.

## Outcome

### Production edits

**`mobile_pack.py`:**
- Line 1440: delete `metadata_report_path = output_path / METADATA_VALIDATION_REPORT_FILENAME` if it becomes unused.
- Line 1451: delete `write_validation_report(metadata_report, metadata_report_path)`.
- Imports (lines 24, 28): drop `REPORT_FILENAME as METADATA_VALIDATION_REPORT_FILENAME` and `write_validation_report` from the `metadata_validation` import if no other reference remains in the file. Verify via grep on the symbols post-edit.

**`scripts/refresh_mobile_pack_metadata.py`:**
- Lines 244-247: delete the `write_validation_report(metadata_report, output_dir / METADATA_VALIDATION_REPORT_FILENAME)` call.
- Imports (lines 22, 26): drop `REPORT_FILENAME as METADATA_VALIDATION_REPORT_FILENAME` and `write_validation_report` from the `metadata_validation` import if no other reference remains in the file.

Preserve:
- `report_has_errors(metadata_report)` + raise/exit blocks at both sites — validation error behavior unchanged.
- `validate_guide_records(...)` call at both sites — validation still happens, just not written to disk in the mobile-pack path.

### Tracker edits

**`notes/CP9_ACTIVE_QUEUE.md`:**
1. Update the top-of-file "Last updated" line to 2026-04-22 afternoon with a one-line R-hygiene2 summary.
2. In **Post-RC Tracked**: strike the "Pack-drift investigation" entry (now line 38) — replace with a one-line "resolved" reference pointing at `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` §6 (adopt `cf449ee9` forward, drift closed as historical).
3. In **Carry-over Backlog**: update the "Mobile pack export script may produce defunct filenames" entry (lines 96-103) — strike the `metadata_validation_report.json` portion as resolved by this slice; preserve the other defunct-filename items if they're still open (if all six are resolved, strike the whole entry).
4. In **Completed (rolling log)** at the end: add a `- 2026-04-22 — R-hygiene2:` entry summarizing the two removed writes.
5. If a **"Post-RC Tracked — resolved in-session"** or similar strike-through section exists (or needs creating), add the CABIN_HOUSE "dead marker" claim there with "investigated 2026-04-22, claim was false: marker is live via `guide.title` → `core_text` path at `mobile_pack.py:2003-2011`; `guides/shelter-site-assessment.md:4` title matches the marker verbatim post-lowercase." If no such section exists, the cleanest place is a one-line strike-through next to any existing CABIN_HOUSE mention, or a new "Resolved without slice" subsection under Post-RC Tracked. Use judgment.

**`notes/dispatch/README.md`:**
- Update the "Active slices" section: remove pack-drift-investigation language; note R-hygiene2 and R-track1 as the post-R-pack-drift1 lane. Single-sentence edit.

### Commit

Single commit, message: `R-hygiene2: drop metadata_validation_report.json writes from mobile-pack paths`. Body may be empty or a 1-2 line summary (two removed writes + tracker reconciliation). No body required by convention.

## Boundaries (HARD GATE)

- Touch only: `mobile_pack.py`, `scripts/refresh_mobile_pack_metadata.py`, `notes/CP9_ACTIVE_QUEUE.md`, `notes/dispatch/README.md`.
- Do NOT:
  - Modify `ingest.py` — desktop-side write stays.
  - Modify `scripts/run_mobile_headless_preflight.py` — scope-deferred, different semantics.
  - Modify `metadata_validation.py` — `REPORT_FILENAME`, `write_validation_report`, `validate_guide_records` all remain as public API.
  - Modify `tests/test_ingest.py` or add/remove any test file. Behavior-neutral hygiene; existing tests pass unchanged.
  - Rebuild any pack or run `export_mobile_pack.py` / `refresh_mobile_pack_metadata.py` / any other pack pipeline. This is a code-only edit.
  - Touch Android-side files.
  - Delete or rotate any slice file (R-hygiene2 slice file stays in `notes/dispatch/` until a later D-slice rotates it).
  - Touch R-pack-drift1's investigation note — it's forensic, preserve as-is.

## The work

### Step 1 — Verify preconditions

```bash
git log -1 --format=%H
git log --oneline 7c2c46c..HEAD -- mobile_pack.py scripts/refresh_mobile_pack_metadata.py
ls notes/R-PACK-DRIFT_INVESTIGATION_20260422.md
```

If any precondition fails, STOP and report.

### Step 2 — Read §6 of the investigation note

Open `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` and read §6 carefully. Transcribe the recommendation verbatim (or near-verbatim) into the tracker strike-through — do not paraphrase the strategic call.

### Step 3 — Remove mobile_pack.py write path

Edit `mobile_pack.py`:
1. Delete line 1451 (`write_validation_report(metadata_report, metadata_report_path)`).
2. Check if line 1440 (`metadata_report_path = ...`) is still referenced anywhere in the function body after the delete. If not, delete line 1440.
3. Grep the file for `REPORT_FILENAME`, `METADATA_VALIDATION_REPORT_FILENAME`, `write_validation_report`. If zero remaining references, prune from the imports at lines 24 and 28.

### Step 4 — Remove refresh_mobile_pack_metadata.py write path

Edit `scripts/refresh_mobile_pack_metadata.py`:
1. Delete lines 244-247 (the multiline `write_validation_report(...)` call).
2. Grep the file for `REPORT_FILENAME`, `METADATA_VALIDATION_REPORT_FILENAME`, `write_validation_report`. If zero remaining references, prune from the imports at lines 22 and 26.

### Step 5 — Verify no test breaks

```bash
python -m pytest tests/ -x -q
```

If any test fails, reconcile. Expected: all pass unchanged (no mobile-pack test exercises the removed write).

### Step 6 — In-slice tracker updates

Per the Outcome § Tracker edits list above. Use `Read` first to see the current file state, then `Edit` with precise string replacements. Keep diff tight.

### Step 7 — Commit

```bash
git add mobile_pack.py scripts/refresh_mobile_pack_metadata.py notes/CP9_ACTIVE_QUEUE.md notes/dispatch/README.md
git status
git diff --stat --cached
git commit -m "R-hygiene2: drop metadata_validation_report.json writes from mobile-pack paths"
git log -1 --stat
```

## Acceptance

- Commit title `R-hygiene2: drop metadata_validation_report.json writes from mobile-pack paths`.
- Files changed: exactly 4 (`mobile_pack.py`, `scripts/refresh_mobile_pack_metadata.py`, `notes/CP9_ACTIVE_QUEUE.md`, `notes/dispatch/README.md`).
- Production diff: net-negative lines (removals > additions). Expected ~8-15 removed lines total across the two Python files.
- `python -m pytest tests/ -x -q` returns green.
- Post-commit grep: `grep -rn "metadata_validation_report\|METADATA_VALIDATION_REPORT_FILENAME\|write_validation_report" mobile_pack.py scripts/refresh_mobile_pack_metadata.py` returns zero matches.
- Post-commit grep: `grep -n "write_validation_report" ingest.py scripts/run_mobile_headless_preflight.py` still returns matches (we did NOT touch those).
- Tracker: `CP9_ACTIVE_QUEUE.md` shows R-pack-drift1 struck from Post-RC Tracked, R-hygiene1 carry-over item updated, R-hygiene2 landed entry in the rolling log, CABIN_HOUSE claim dispositioned.
- No other files modified (no drive-by edits).

## Delegation hints

None substantive. Main-agent inline is the default for a 2-file behavior-neutral change with minor tracker edits.

## Anti-recommendations

- Do NOT run `refresh_mobile_pack_metadata.py` or `export_mobile_pack.py` to "verify" the removal — the code-only diff plus the pytest green is sufficient verification. Running pack pipelines introduces drift risk and is out of scope.
- Do NOT remove `REPORT_FILENAME` from `metadata_validation.py` — it's still imported by `ingest.py` and `scripts/run_mobile_headless_preflight.py`.
- Do NOT bundle the `run_mobile_headless_preflight.py` write into this slice even if the scope looks similar. The preflight tool's output_dir is a separate report dir, not the pack assets dir, and its purpose is explicitly validation-reporting. A separate audit slice can evaluate that write if hygiene concerns remain.
- Do NOT touch any other tracker lines beyond those specified in §Outcome. In particular, do not rotate slice files — that's batched in a later D-slice.
- Do NOT amend or edit the R-pack-drift1 investigation note. It's a forensic record.

## Report format

After landing, respond with:

1. Commit SHA and subject line.
2. Files touched count and net line delta.
3. Confirmation: `pytest tests/` result.
4. Confirmation: post-commit grep results (zero matches in the two edited files, still matches in `ingest.py` / `run_mobile_headless_preflight.py`).
5. One-line summary of each tracker edit.
6. Any scope ambiguity encountered (expected: none).

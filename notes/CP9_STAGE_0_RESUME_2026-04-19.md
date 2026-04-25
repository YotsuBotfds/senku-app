# CP9 Stage 0 Resume - Steps 3-9

Date: 2026-04-19
Parent dispatch: `notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`
Parent plan: `notes/CHECKPOINT_9_PLAN_2026-04-19.md`
Prior artifact dir: `artifacts/cp9_stage0_20260419_142539/`

---

## Why this resume exists

The original dispatch's Step 3 command omitted the required
positional `output_dir` argument to `scripts/export_mobile_pack.py`.
Codex correctly stopped at Step 3 rather than improvising. Steps 1
and 2 already passed:

- Step 1 desktop re-ingest: pass
- Step 2 desktop bench rerun: pass (no section dropped > 1; two
  single-prompt LiteRT 500 flakes noted but unrelated to retrieval
  quality)

This resume picks up at Step 3 with a corrected command and
continues through Step 9. Ground rules and off-limits set from the
parent dispatch still apply.

---

## Ground Rules (unchanged from parent)

- No code commits. No tracker commits. Artifact output only.
- No Wave B edits. No guide edits. No BACK-P-03 audit edits.
- Gated-step discipline: if any step fails, stop and log.
- Do not start Stage 1 in the same session.

Reuse the existing artifact directory:

```
STAGE0_DIR=artifacts/cp9_stage0_20260419_142539
```

All new output in this resume lands under that same directory
alongside the Step 1 and Step 2 artifacts.

---

## Preflight re-check (cheap, run first)

```
git status
git log --oneline -3
git diff --name-only
git diff --cached --name-only
```

HEAD should still be `65252f7`. Both diff commands empty. If
either is non-empty, stop — something changed since the original
Stage 0 run.

---

## Step 3 (corrected) - mobile pack re-export

Rebuild the mobile pack from current post-Wave-B desktop state.
Provide an explicit timestamped output directory so the export
does not overwrite any existing pack.

```
python3 scripts/export_mobile_pack.py artifacts/mobile_pack/senku_20260419_cp9_stage0 2>&1 | tee $STAGE0_DIR/mobile_pack_export.txt
```

The script prints a JSON `ExportSummary` with `output_dir`,
`manifest_path`, `sqlite_path`, `vector_path`, `vector_dimension`,
`counts`, and `files` (a dict with per-file metadata; extract any
hash fields it exposes).

After export, compute SHA-256 on the three pack files as a stable
hash record (the `senku_manifest.json` itself is the canonical
pack identity):

```
python3 -c "
import hashlib, json, pathlib
pack = pathlib.Path('artifacts/mobile_pack/senku_20260419_cp9_stage0')
files = ['senku_manifest.json', 'senku_mobile.sqlite3', 'senku_vectors.f16']
# Some exports use senku_vectors.i8 instead of .f16 depending on dtype.
# Prefer whichever is present.
out = {}
for name in files:
    p = pack / name
    if not p.exists():
        # Fallback vector filename
        if name.startswith('senku_vectors'):
            alt = next(pack.glob('senku_vectors.*'), None)
            if alt is not None:
                p = alt
                name = p.name
    if p.exists():
        out[name] = hashlib.sha256(p.read_bytes()).hexdigest()
print(json.dumps(out, indent=2, sort_keys=True))
" 2>&1 | tee $STAGE0_DIR/mobile_pack_hashes.txt
```

Record the manifest SHA-256 as the Stage 0 pack hash in the
summary. That hash is what Stage 6 live-on-device smoke will
compare against.

Acceptance: export completes with exit code 0; three pack files
present in the output dir; manifest SHA-256 recorded.

---

## Steps 4 through 9 - no changes from parent dispatch

Execute Steps 4 through 9 exactly as written in
`notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`.

Quick pointers to avoid rereading the full parent:

- Step 4 emulator matrix: `adb devices` must show all four of
  `emulator-5556`, `emulator-5560`, `emulator-5554`, `emulator-5558`.
  Fall back from `pwsh` to `powershell -NoProfile -File ...` if
  pwsh is not installed (same fall-back that worked for DAY-L-01).

- Step 5 hot-swap: read `scripts/push_mobile_pack_to_android.ps1`
  head to confirm parameter names before executing. Push the newly
  exported pack at `artifacts/mobile_pack/senku_20260419_cp9_stage0/`
  to each emulator serial.

- Step 6 Wave B live smoke: query
  `He has barely slept, keeps pacing and muttering to himself, and refuses to eat. What should we do?`
  on all four emulators. Must render `uncertain_fit` with escalation
  line on every emulator.

- Step 7 fallback query: the three candidate queries in the parent
  dispatch. Pick the first that fires `uncertain_fit` with at least
  moderate trigger signal.

- Step 8 summary: use the template in the parent dispatch's Step 8.
  Update the Steps table to include this resume run's outcomes.
  Note the two Step 2 LiteRT 500 flakes in the summary under
  "Known noise" so they are not surprising in later stages:
  - `my kid has a fever of 104 and we have no medicine`
  - `how do i make dye from plants`

- Step 9 report: follow the parent dispatch's reporting format.

---

## Verdict rules

Stage 0 verdict is GREEN only if Step 3 corrected run passes AND
Steps 4-7 all pass. The existing Step 1-2 passes stand.

If any step in this resume fails, stop and log. Do NOT start
Stage 1 (RC v3 packet rebuild) in the same session regardless of
verdict.

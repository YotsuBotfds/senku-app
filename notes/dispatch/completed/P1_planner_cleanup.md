# Slice P1 — Planner-side backlog cleanup

- **Role:** main agent (`gpt-5.4 xhigh`). Owns delegation per
  `notes/SUBAGENT_WORKFLOW.md`.
- **Parallel with:** A1 (CP9 Stage 0 Step 6j v6). Safe to run
  concurrently — this slice does not touch the emulator matrix, APK, or
  any device state.
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → "P1 — Planner-side backlog
  cleanup"

## Preconditions

- None. Can dispatch immediately.
- Stage 0 must remain untouched: do NOT run `adb`, do NOT rebuild the
  APK, do NOT touch `files/models/` on any serial.

## Outcome

Five planner-side edits land in git, making the tracker and methodology
docs match the actual repo state so that Stage 1 reviewers (and future
you) are not reading stale claims.

## The five edits

1. **Commit `TESTING_METHODOLOGY.md`.** It is currently untracked
   (`git status` shows `??`). Before committing, append a "Model deploy
   discipline" section covering:
   - The `MainActivity.runAsk` model gate — lines 661-671 — which bails
     when there is no model AND no host inference.
   - The required step after every `adb uninstall` or clean install:
     push the LiteRT model via `scripts/push_litert_model_to_android.ps1`
     or the direct-stream bypass
     (`adb shell run-as com.senku.mobile sh -c 'cat > files/models/<name>' < local_file`).
   - The AVD data-partition sizing rule: tmp-staging path needs
     `≥ 2× model_size` of free space; tablet AVDs at ~6 GB cannot stage
     E4B and should use direct-stream.

2. **Add a HEAD banner to `notes/REVIEWER_BACKEND_TRACKER_20260418.md`.**
   Insert before the existing "Current State" section:

   > Wave B code-level closure complete 2026-04-19 (`eb398dc`); on-device
   > CP9 Stage 0 verification in progress. v2/v3/v4/v5 artifacts are
   > INVALID (APK/model mismatch). Stage 0 GREEN is a prerequisite for
   > RC v3 packet rebuild (Stage 1).

3. **Add three backlog stubs** to the same tracker, under `### Pack lane`:
   - `BACK-P-05`: SQLite FTS runtime decision. Hypothesis: current FTS
     path is adequate for Wave B scale; reconfirm at RC v3 telemetry.
     Blocks nothing in CP9.
   - `BACK-P-06`: AVD data-partition sizing policy. Hypothesis: document
     direct-stream as the default for E4B on tablet AVDs; keep
     tmp-staging behind `--use-tmp-staging`. Blocks a clean Stage 0
     replay on fresh AVDs.
   - `BACK-P-07`: Unified LiteRT model push helper. Hypothesis:
     consolidate `push_litert_model_to_android.ps1` and the direct-stream
     bypass behind one entry point that picks based on a free-space
     probe. Nice-to-have.

4. **Verify `BACK-U-04` runner.** The tracker says
   "regression runner + baseline artifact are checked in" (line 354).
   Audit the repo for an abstain-tuning regression runner (likely under
   `tests/`, `scripts/`, or `bench*`). If the only artifact is
   `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md`, downgrade the tracker
   claim to "analysis landed; runner deferred — promote to `BACK-U-04b`
   after RC v3."

5. **Extend `summary.json` schema in two PS1 scripts:**
   - `scripts/run_android_instrumented_ui_smoke.ps1`
   - `scripts/build_android_ui_state_pack_parallel.ps1`

   Add to each per-serial `summary.json`:
   - `apk_sha` (sha256 of the installed APK)
   - `model_name` (filename in `files/models/`)
   - `model_sha` (sha256 of the model file)

   Add a pack-level rollup field: `matrix_homogeneous: bool` — true iff
   all serials share the same `apk_sha` and `model_sha`.

## Boundaries

- No emulator touch.
- No APK rebuild.
- No ingest / mobile pack rebuild.
- No Codex usage beyond main-agent coordination — push the mechanical
  edits to sidecars where sensible.

## Acceptance

- `git status` clean after the commit(s).
- `git log -5 --oneline` shows the new commit(s).
- `git ls-files TESTING_METHODOLOGY.md` returns the file.
- Tracker HEAD banner is visible within the first 30 lines of
  `REVIEWER_BACKEND_TRACKER_20260418.md`.
- Three new `BACK-P-*` stubs present under `### Pack lane`.
- Dry-run invocation (or static read-through) of the two PS1 scripts
  shows the new `summary.json` fields being emitted. Do not run them
  against a live emulator.

## Delegation hints

(Suggestions only — main agent picks per `notes/SUBAGENT_WORKFLOW.md`.)

- Edits 1, 2, 3 are multi-file but mechanical. Reasonable candidate for
  `gpt-5.4 high` worker if main agent would rather stay on Stage 0.
- Edit 4 starts with a grep audit. That's Spark scout territory — punt
  the "is there a runner?" question, then act on the answer.
- Edit 5 is a two-file script change plus a schema decision. Tempting
  to scout with Spark first ("show me the current summary.json writer
  in each script"), then implement with `gpt-5.4 high`.
- If main agent chooses to run some edits inline and delegate others,
  that is fine — just log the split in the report.

## Report format

Reply with:
- Commit shas (one line each).
- One-line summary per edit.
- Paste of the new tracker banner (verbatim, so Opus can eyeball it).
- Delegation log: for each edit, which lane ran it and a one-line "why."
- Any surprises (e.g. if `BACK-U-04` runner actually exists and the
  tracker was right).

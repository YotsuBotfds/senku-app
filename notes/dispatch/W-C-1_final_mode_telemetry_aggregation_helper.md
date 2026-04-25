# Slice W-C-1 - add final-mode telemetry aggregation helper and aggregate the fresh probe bundle

- **Role:** main agent (`gpt-5.4 xhigh`). Small script + artifact slice, suitable for a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice adds one tracked helper, derives one compact artifact bundle from already-verified raw logcats, and advances the live queue.
- **Why this slice now:** `W-C-1a` (`4adb144`) closed the blocker that killed the original `W-C-1` attempt. We now have a fresh verification bundle at `artifacts/bench/final_mode_emission_probe_20260423/` with literal `ask.generate final_mode=` lines on-device. That changes the right next move: `W-C-1` should consume that fresh substrate, not rerun the Android harness blindly, and it must not keep the stale assumption that the old lever prompt is a confident bucket. The actual confident control from `W-C-1a` is `How do I build a cabin roof that sheds rain?`

## Outcome

One commit that:

1. Adds a tracked helper:
   - `scripts/aggregate_final_mode_telemetry.py`
2. Derives a compact telemetry sample bundle under:
   - `artifacts/bench/final_mode_telemetry_sample_20260423/`
3. Updates `notes/CP9_ACTIVE_QUEUE.md` so `W-C-1` is recorded and `W-C-2` becomes the next Wave C move.

Expected code surface: one new Python helper and one queue update.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `4adb144` (`W-C-1a: restore final-mode runtime emission`) or descends from it.
2. `git status --short -- notes/CP9_ACTIVE_QUEUE.md` is clean before you start. If it is already dirty, **STOP** and escalate.
3. `scripts/aggregate_final_mode_telemetry.py` is not already tracked.
4. The fresh `W-C-1a` verification bundle exists:
   - `artifacts/bench/final_mode_emission_probe_20260423/summary.md`
   - `artifacts/bench/final_mode_emission_probe_20260423/logcats/confident_control.logcat.txt`
   - `artifacts/bench/final_mode_emission_probe_20260423/logcats/uncertain_fit.logcat.txt`
   - `artifacts/bench/final_mode_emission_probe_20260423/logcats/abstain.logcat.txt`
5. Reconfirm the now-true repo fact before editing:
   - `rg -n "ask\\.generate final_mode=" artifacts/bench/final_mode_emission_probe_20260423/logcats -g "*.txt"` returns matches
6. Reconfirm the prompt-mapping correction before editing:
   - `confident_control.logcat.txt` carries the fresh confident bucket
   - `confident.logcat.txt` from the original lever prompt is evidence of `uncertain_fit`, not the confident control for this slice
7. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus remains deferred
   - the broad historical `notes/` backlog remains deferred
   - repo-root zip/screenshot/audit items remain deferred

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `scripts/aggregate_final_mode_telemetry.py`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Generate artifact outputs only under:
  - `artifacts/bench/final_mode_telemetry_sample_20260423/`
- Do **not** touch:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `scripts/run_android_prompt.ps1`
  - `scripts/run_android_search_log_only.ps1`
  - `query.py`
  - `tests/test_uncertain_fit.py`
  - `notes/WAVE_C_DIRECTION_20260422.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
  - `notes/specs/abstain_nearboundary_panel_20260423.yaml`
  - `artifacts/bench/final_mode_emission_probe_20260423/`
  - any other file under `scripts/`
  - any `guides/` file
- Do **not** turn this into `W-C-2`:
  - no threshold moves
  - no Android reroute work
  - no prompt-harness rerun unless the required `W-C-1a` raw logs are genuinely missing
  - no desktop `final_mode` mirror
  - no broad state-pack rerun

## The edits

### Edit 1 - confirm the fresh substrate and the prompt correction

Before writing the helper, confirm and report:

1. `W-C-1a` raw logs already contain literal `ask.generate final_mode=` lines.
2. The three bucket-carrying logs for this slice are:
   - `confident_control.logcat.txt` -> `confident`
   - `uncertain_fit.logcat.txt` -> `uncertain_fit`
   - `abstain.logcat.txt` -> `abstain`
3. The original lever-prompt log:
   - `confident.logcat.txt`
   is **not** the confident bucket for this slice because it still routes to `uncertain_fit`.

That correction is part of the value of `W-C-1a`; do not silently ignore it.

### Edit 2 - add `scripts/aggregate_final_mode_telemetry.py`

Create a small tracked helper that:

1. Accepts one or more logcat paths.
   - repeated `--logcat-path` args are fine
2. Accepts an explicit output directory.
   - `--output-dir` is preferred
3. Parses lines of the form:

```text
ask.generate final_mode=<mode> route=<route> query="<query>" totalElapsedMs=<n>
```

4. Produces:
   - `summary.json`
   - `summary.md`

Required output content:

- input logcat path list
- total `final_mode` events found
- counts by `final_mode`
- counts by `route`
- per-query rows including:
  - query
  - final_mode
  - route
  - totalElapsedMs
  - source logcat path

Nice-to-have but still in scope if easy:

- also parse `ask.generate low_coverage_route query="..." mode=...` lines and report them in a separate companion section

Hard behavior rules:

- exit nonzero if zero `final_mode` lines are found
- ASCII-only output
- standard library only

### Edit 3 - derive the compact sample bundle from `W-C-1a`

Do **not** rerun the Android prompt harness if the required `W-C-1a` logs are present.

Create:

- `artifacts/bench/final_mode_telemetry_sample_20260423/logcats/`

Populate it by copying these three already-verified raw logcats from the `W-C-1a` probe bundle:

1. `confident_control.logcat.txt`
2. `uncertain_fit.logcat.txt`
3. `abstain.logcat.txt`

Keep the filenames unchanged in the new sample bundle so the provenance remains obvious.

If any of those three source logcats are missing, **STOP** and report instead of silently rerunning Android.

### Edit 4 - run the aggregation helper

Run the new helper against the three copied logcats and write outputs to:

- `artifacts/bench/final_mode_telemetry_sample_20260423/summary.json`
- `artifacts/bench/final_mode_telemetry_sample_20260423/summary.md`

Keep the copied raw logcats alongside the summary outputs in that same derived bundle.

### Edit 5 - sanity-check the derived bundle

Confirm:

1. the summary files exist
2. the copied raw logcat files exist
3. the aggregated sample includes at least one:
   - `confident`
   - `uncertain_fit`
   - `abstain`
4. the helper is reading the literal `final_mode` breadcrumb, not inferring the mode from some other line
5. the summary makes the route distribution easy to read for later `W-C-2` threshold work

Capture any interesting route observations in your report, but **do not** act on them in this slice.

### Edit 6 - update `notes/CP9_ACTIVE_QUEUE.md`

Minimal queue changes only:

1. advance the "Last updated" line to `W-C-1`
2. keep the truth that no slices are currently in flight unless your own execution state makes that false
3. change the next Wave C move to:
   - `W-C-2` desktop abstain-threshold tuning
4. append a completed-log entry summarizing:
   - `W-C-1a` fresh probe logs were aggregated rather than re-collected
   - tracked aggregation helper landed
   - derived sample bundle path
   - `W-C-2` now next
5. do **not** reprioritize unrelated backlog

### Edit 7 - commit

Single commit only. Suggested subject:

```text
W-C-1: add final-mode telemetry aggregation helper
```

Tight equivalent is acceptable if it preserves the two core actions:

- aggregation helper landed
- derived sample bundle minted from the `W-C-1a` probe logs

## Acceptance

- One commit only.
- `git ls-files` shows:
  - `scripts/aggregate_final_mode_telemetry.py`
- Derived sample bundle exists at:
  - `artifacts/bench/final_mode_telemetry_sample_20260423/`
- Bundle includes:
  - the three copied raw logcats
  - `summary.json`
  - `summary.md`
- Aggregated output includes at least one `confident`, one `uncertain_fit`, and one `abstain` `final_mode`.
- `notes/CP9_ACTIVE_QUEUE.md` records `W-C-1` and points next at `W-C-2`.
- No file outside the explicit touch set changed.
- The following grep returns **no matches** across the tracked files you touched:

```powershell
rg -n '[^\x00-\x7F]' `
  scripts/aggregate_final_mode_telemetry.py `
  notes/CP9_ACTIVE_QUEUE.md
```

- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched tracked files remain unstaged/modified after commit.

## Delegation hints

- Good `gpt-5.4 high` worker slice: one small parser, one derived artifact bundle from existing logs, one queue update.
- If delegated, the worker should own the substrate confirmation, helper implementation, derived bundle creation, aggregation output, queue update, and single commit, then hand back the sha plus the mode/route counts.

## Anti-recommendations

- Do **not** tune thresholds here.
- Do **not** touch Android code or the prompt harness scripts here.
- Do **not** rerun Android just because the earlier version of this slice expected it; `W-C-1a` already paid that cost.
- Do **not** use the lever prompt as the confident bucket in this slice.
- Do **not** broaden into a full state-pack rerun.

## Report format

- Commit sha + subject.
- Files changed / newly tracked.
- Verification bullets:
  - `W-C-1a` substrate confirmed
  - tracked helper landed
  - derived sample bundle path
  - sample contains all three modes
  - queue now points at `W-C-2`
- Mode/route count summary from the derived sample bundle.
- Any out-of-scope drift still remaining after `W-C-1`.

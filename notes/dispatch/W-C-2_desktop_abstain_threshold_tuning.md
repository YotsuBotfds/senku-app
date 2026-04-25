# Slice W-C-2 - raise the desktop abstain similarity floor to `0.67` and rerun the near-boundary panel

- **Role:** main agent (`gpt-5.4 xhigh`). Small but high-consequence desktop-routing slice; suitable for a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice changes a live desktop routing threshold, reruns the tracked panel surface, and advances the live queue.
- **Why this slice now:** `W-C-0`, `W-C-1a`, and `W-C-1` are closed. The fresh evidence no longer supports the stale `0.67 / 3` watchpoint as written in older notes. The expanded near-boundary panel at `artifacts/bench/abstain_nearboundary_20260423/summary.md` shows `0.67 / 2` and `0.67 / 3` are identical (`5 TP`, `0 FP`, `1 FN`) while the current production point `0.62 / 2` underperforms (`2 TP`, `0 FP`, `4 FN`). That means `W-C-2` should raise the desktop similarity floor only, keep the unique-hit floor at `2`, preserve Android as-is, and prove the change with a rerun against the tracked near-boundary YAML panel.

## Outcome

One commit that:

1. Raises the desktop production abstain similarity floor from `0.62` to `0.67` in `query.py`.
2. Keeps the other abstain production constants unchanged:
   - row limit stays `3`
   - max overlap tokens stays `1`
   - unique lexical hits stays `2`
3. Tightens or extends the desktop Python guard coverage only where needed.
4. Writes a fresh post-tune artifact bundle under:
   - `artifacts/bench/abstain_nearboundary_tuned_20260423/`
5. Updates `notes/CP9_ACTIVE_QUEUE.md` so `W-C-2` is recorded and `W-C-3` becomes the next Wave C move.

Expected code surface: one production Python file, one or two Python test files, one queue update.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `2fc4edb` (`W-C-1: add final-mode telemetry aggregation helper`) or descends from it.
2. `git status --short -- query.py tests/test_abstain.py tests/test_uncertain_fit.py notes/CP9_ACTIVE_QUEUE.md` is clean before you start. If any of those tracked files are already dirty, **STOP** and escalate.
3. Reconfirm the live evidence before editing:
   - `artifacts/bench/abstain_nearboundary_20260423/summary.md` exists
   - it shows production constants `0.62 / 2`
   - it shows `0.67 / 2` and `0.67 / 3` are identical on the expanded panel (`5 TP`, `0 FP`, `1 FN`)
4. Reconfirm the helper/runner substrate before editing:
   - `scripts/run_abstain_regression_panel.ps1` exists and is tracked
   - `notes/specs/abstain_nearboundary_panel_20260423.yaml` exists and is tracked
5. Reconfirm the desktop/Android split before editing:
   - desktop abstain gates live in `query.py`
   - Android routing and thresholds are out of scope for this slice
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus remains deferred
   - the broad historical `notes/` backlog remains deferred
   - repo-root zip/screenshot/audit items remain deferred

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `query.py`
  - `tests/test_abstain.py`
  - `tests/test_uncertain_fit.py`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Generate artifact outputs only under:
  - `artifacts/bench/abstain_nearboundary_tuned_20260423/`
- Do **not** touch:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `scripts/run_abstain_regression_panel.ps1`
  - `scripts/abstain_regression_panel.py`
  - `scripts/aggregate_final_mode_telemetry.py`
  - `notes/specs/abstain_nearboundary_panel_20260423.yaml`
  - `notes/WAVE_C_DIRECTION_20260422.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
  - any other file under `scripts/`
  - any file under `guides/`
- Do **not** turn this into `W-C-3` or `W-C-4`:
  - no Android threshold mirroring
  - no Android telemetry changes
  - no uncertain-fit band tuning
  - no retrieval/rerank logic changes beyond the abstain production constant move
  - no desktop `final_mode` emission mirror
  - no panel-authoring churn

## The edits

### Edit 1 - restate the evidence and drop the stale `0.67 / 3` assumption

Before changing code, capture and report these facts:

1. The current production point in the existing near-boundary artifact is:
   - `0.62 / 2`
2. The expanded panel shows:
   - `0.67 / 2` -> `5 TP`, `0 FP`, `1 FN`
   - `0.67 / 3` -> `5 TP`, `0 FP`, `1 FN`
3. Therefore the expanded panel supports:
   - raising similarity to `0.67`
   - keeping unique lexical hits at `2`
4. `W-C-1` telemetry is supporting infrastructure, not new desktop calibration evidence.

Do **not** silently carry forward the older `0.67 / 3` assumption.

### Edit 2 - keep the desktop move minimal in `query.py`

In `query.py`, make the smallest production change that matches the new evidence:

1. Change:
   - `_ABSTAIN_MIN_VECTOR_SIMILARITY`
   - from `0.62` to `0.67`
2. Preserve:
   - `_ABSTAIN_ROW_LIMIT = 3`
   - `_ABSTAIN_MAX_OVERLAP_TOKENS = 1`
   - `_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS = 2`
3. Preserve the current abstain `AND` gate shape.
4. Preserve the current uncertain-fit thresholds and logic.
   - they are out of scope for this slice

Important hidden coupling:

- `_abstain_match_label()` uses the same similarity constant. That coupling is acceptable and expected, but do not widen the change beyond what falls out naturally from the constant move.

### Edit 3 - add or tighten focused Python coverage

Use the smallest test changes needed to lock the new production behavior.

Required test intent:

1. `tests/test_abstain.py`
   - cover the new `0.67` similarity-floor behavior explicitly
   - keep the unique-hit floor at `2`
   - keep abstain-card behavior stable
2. `tests/test_uncertain_fit.py`
   - keep desktop uncertain-fit behavior guarded
   - only edit if needed to document or preserve the "no uncertain-fit band move" contract

Guardrail:

- if `tests/test_uncertain_fit.py` already passes unchanged after the constant move, prefer leaving it untouched

### Edit 4 - run the desktop probe guard before and after the tune

Wave C direction note requires probe discipline for `W-C-2+`. For this desktop-only slice, use the existing Python routing tests as the probe guard.

Required commands:

```powershell
python -m unittest tests.test_uncertain_fit -v
```

Run it once before the code change and again after the code change. If the post-tune run fails or starts routing a previously guarded case differently without an explicit reason in this slice, **STOP** and report.

### Edit 5 - run focused desktop test acceptance

After the code change, run:

```powershell
python -m unittest tests.test_abstain tests.test_uncertain_fit tests.test_bench_config -v
```

If a failure suggests this slice would need broader retrieval/rerank logic edits, **STOP** and report instead of widening.

### Edit 6 - rerun the near-boundary panel explicitly against the tracked YAML

Do **not** rely on the runner default panel, because it still points at the older baseline.

Run:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run_abstain_regression_panel.ps1 `
  -PanelPath notes/specs/abstain_nearboundary_panel_20260423.yaml `
  -OutputDir artifacts/bench/abstain_nearboundary_tuned_20260423 `
  -TopK 8
```

If `powershell` does not resolve cleanly in-session, `powershell.exe` is fine.

Expected outputs:

- `summary.md`
- `summary.json`
- `sweep_summary.csv`
- `query_details.csv`

### Edit 7 - sanity-check the tuned artifact bundle

Confirm and report:

1. the fresh bundle exists at:
   - `artifacts/bench/abstain_nearboundary_tuned_20260423/`
2. the fresh summary records production constants:
   - row_limit=`3`
   - max_overlap_tokens=`1`
   - min_vector_similarity=`0.67`
   - min_unique_hits=`2`
3. the tuned production point keeps:
   - false-positive rate `0.000`
4. the tuned production point reaches:
   - `5 / 6` abstain true positives on the panel
5. `rain_shelter` remains an answer on the abstain panel
   - any shift to abstain is a regression for this slice

If the tuned production point loses the `0 FP` property or fails to reach `5 / 6` abstain true positives, **STOP** and report instead of committing.

### Edit 8 - update `notes/CP9_ACTIVE_QUEUE.md`

Minimal queue changes only:

1. advance the "Last updated" line to `W-C-2`
2. keep the truth that no slices are currently in flight unless your own execution state makes that false
3. change the next Wave C move to:
   - `W-C-3` Android alignment / mirror
4. append a completed-log entry summarizing:
   - desktop abstain similarity floor raised to `0.67`
   - unique-hit floor intentionally stayed at `2`
   - fresh tuned near-boundary artifact bundle path
   - `W-C-3` now next
5. do **not** reprioritize unrelated backlog

### Edit 9 - commit

Single commit only. Suggested subject:

```text
W-C-2: raise desktop abstain similarity floor
```

Tight equivalent is acceptable if it preserves the two core actions:

- desktop abstain similarity floor raised to `0.67`
- fresh tuned near-boundary artifact bundle minted

## Acceptance

- One commit only.
- `git diff --name-only HEAD~1 HEAD` is limited to:
  - `query.py`
  - `tests/test_abstain.py`
  - `tests/test_uncertain_fit.py` (only if needed)
  - `notes/CP9_ACTIVE_QUEUE.md`
- `query.py` now keeps:
  - row limit `3`
  - max overlap tokens `1`
  - min vector similarity `0.67`
  - min unique lexical hits `2`
- Focused desktop test lane passes:

```powershell
python -m unittest tests.test_abstain tests.test_uncertain_fit tests.test_bench_config -v
```

- Fresh tuned bundle exists at:
  - `artifacts/bench/abstain_nearboundary_tuned_20260423/`
  - with `summary.md`, `summary.json`, `sweep_summary.csv`, and `query_details.csv`
- The tuned bundle summary shows the production point keeps:
  - `0 FP`
  - `5 / 6` abstain TP
- `notes/CP9_ACTIVE_QUEUE.md` records `W-C-2` and points next at `W-C-3`.
- The following grep returns **no matches** across the tracked files you touched:

```powershell
rg -n '[^\x00-\x7F]' `
  query.py `
  tests/test_abstain.py `
  tests/test_uncertain_fit.py `
  notes/CP9_ACTIVE_QUEUE.md
```

- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched tracked files remain unstaged/modified after commit.

## Delegation hints

- Good `gpt-5.4 high` worker slice: one constant move, narrow test adjustments, one explicit panel rerun, one queue update.
- If delegated, the worker should own the evidence restatement, the constant move, the focused test pass, the tuned artifact rerun, the queue update, and the single commit, then hand back the sha plus the tuned production-point metrics.

## Anti-recommendations

- Do **not** raise unique lexical hits to `3` in this slice.
- Do **not** tune uncertain-fit thresholds here.
- Do **not** touch Android thresholds or telemetry here.
- Do **not** rely on the runner's default baseline panel.
- Do **not** broaden into retrieval/rerank tuning beyond the abstain constant move.

## Report format

- Commit sha + subject.
- Files changed.
- Verification bullets:
  - evidence restated
  - production move landed (`0.67 / 2`)
  - focused desktop tests passed
  - tuned artifact bundle path
  - queue now points at `W-C-3`
- Tuned production-point metrics from the fresh bundle.
- Any out-of-scope drift still remaining after `W-C-2`.

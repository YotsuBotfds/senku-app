# Slice W-C-0 - fork the near-boundary abstain panel and track the runner surface

- **Role:** main agent (`gpt-5.4 xhigh`). Mixed doc/script slice, but still bounded and suitable for a `gpt-5.4 high` worker if main wants speed.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice touches the live queue, promotes an untracked runner into the tracked tree, adds a new tracked panel sidecar, and emits fresh artifact evidence.
- **Why this slice now:** D10 (`7bc62c6`) locked the Wave C series order and made `W-C-0` the next substantive move. The real dependency chain is now explicit: the current abstain runner wrapper is untracked, and it points at an untracked helper under `artifacts/`. `W-C-0` needs to solve that first, then land the forked near-boundary panel that D10 chose as the evidence precursor for all later threshold-tuning slices.

## Outcome

One commit that:

1. Tracks the abstain-panel runner surface in a reusable form:
   - `scripts/run_abstain_regression_panel.ps1` (tracked)
   - `scripts/abstain_regression_panel.py` (new tracked helper)
2. Adds a tracked forked panel sidecar:
   - `notes/specs/abstain_nearboundary_panel_20260423.yaml`
3. Runs the forked panel and writes a new artifact bundle under:
   - `artifacts/bench/abstain_nearboundary_20260423/`
4. Updates `notes/CP9_ACTIVE_QUEUE.md` so `W-C-0` is recorded and `W-C-1` becomes the next Wave C move.

Expected code surface: one PowerShell wrapper, one Python helper, one YAML sidecar, one queue update.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `7bc62c6` (`D10: lock Wave C direction note and W-C-0 prep`) or descends from it.
2. `git status --short -- notes/CP9_ACTIVE_QUEUE.md` is clean before you start. If it is already dirty, **STOP** and escalate.
3. These files exist on disk before you start:
   - `scripts/run_abstain_regression_panel.ps1` (currently untracked)
   - `artifacts/bench/abstain_baseline_20260418/abstain_regression_panel.py`
   - `artifacts/bench/abstain_baseline_20260418/panel.json`
   - `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md`
   - `notes/WAVE_C_DIRECTION_20260422.md`
4. `python -c "import yaml"` succeeds. PyYAML is already a normal repo dependency surface; if this import fails, **STOP**.
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus remains deferred
   - the broad historical `notes/` backlog remains deferred
   - repo-root zip/screenshot/audit items remain deferred

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `scripts/run_abstain_regression_panel.ps1`
  - `scripts/abstain_regression_panel.py` (new file)
  - `notes/specs/abstain_nearboundary_panel_20260423.yaml` (new file)
  - `notes/CP9_ACTIVE_QUEUE.md`
- Generate artifact outputs only under:
  - `artifacts/bench/abstain_nearboundary_20260423/`
- Do **not** touch:
  - `query.py`
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `tests/test_uncertain_fit.py`
  - `notes/WAVE_C_DIRECTION_20260422.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
  - `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md`
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - any `guides/` file
  - any unrelated script under `scripts/`
- Do **not** turn this into `W-C-1`, `W-C-2`, or broader hygiene work:
  - no telemetry aggregation helper
  - no threshold moves
  - no Android alignment changes
  - no desktop final-mode emission mirror
  - no broader script-tracking sweep

## The edits

### Edit 1 - runner preflight on the current untracked surface

Before promoting anything, inspect:

- `scripts/run_abstain_regression_panel.ps1`
- `artifacts/bench/abstain_baseline_20260418/abstain_regression_panel.py`

Preflight checks:

1. **CLI shape sanity**
   - confirm the wrapper is still a narrow launcher with the expected arguments:
     - `PanelPath`
     - `OutputDir`
     - `TopK`
     - `LmStudioUrl`
     - `ChromaDbDir`
   - confirm the helper is still a bounded panel runner, not a general benchmark harness

2. **Secret / HARD-STOP scan**
   - inspect for obvious secrets or credentials
   - local `LM_STUDIO_URL` references and `localhost:1234` are benign and expected

If either file looks broader than this slice expects, or if you find anything secret-like beyond benign local-runtime references, **STOP** and report instead of promoting it.

### Edit 2 - add a tracked helper under `scripts/`

Create `scripts/abstain_regression_panel.py` as the tracked helper for this panel surface.

Source template:
- use `artifacts/bench/abstain_baseline_20260418/abstain_regression_panel.py` as the starting point

Required adaptations:

1. **Tracked location**
   - adjust repo-root discovery for a helper that now lives under `scripts/`, not under `artifacts/...`

2. **Panel loading**
   - support both:
     - legacy list-shaped JSON panel files
     - new YAML sidecars with top-level metadata plus `queries: [...]`
   - use `yaml.safe_load`; JSON compatibility should fall out naturally because JSON is valid YAML

3. **Metadata-aware summaries**
   - remove the hard-coded "Abstain Regression Baseline - 2026-04-18" assumption
   - if the panel carries metadata like `title` / `panel_id`, use it in the summary output
   - preserve sensible fallback behavior for legacy baseline JSON panels that are just a list

4. **Behavior scope**
   - preserve the current retrieval metrics, sweep logic, CSV outputs, and summary structure
   - do **not** change threshold constants or the evaluation logic itself

### Edit 3 - promote the PowerShell wrapper

Track `scripts/run_abstain_regression_panel.ps1` and update it only as needed to:

- call the new tracked helper at `scripts/abstain_regression_panel.py`
- preserve the existing CLI shape
- continue allowing explicit `-PanelPath` / `-OutputDir` overrides

Keep this wrapper narrow. Do not turn it into a multi-panel orchestrator.

### Edit 4 - add the forked near-boundary panel sidecar

Create `notes/specs/abstain_nearboundary_panel_20260423.yaml`.

Required schema:

```yaml
panel_id: abstain_nearboundary_20260423
title: Abstain Near-Boundary Panel - 2026-04-23
description: ...
queries:
  - id: ...
    bucket: should_not_abstain
    expected_abstain: false
    question: ...
    rationale: ...
```

Required content rules:

1. This is a **fork** of the baseline, not a brand-new unrelated set.
   - preserve the original 12 baseline queries from `artifacts/bench/abstain_baseline_20260418/panel.json`
2. Add **6-10 new** near-boundary `should_not_abstain` prompts.
   - total panel size should end up in the `18-22` range
3. New prompts must be evidence-backed and thin-but-real.
   - use D10 / forward-research guidance
   - draw from concrete examples already called out in `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md`, especially weak-support cases like `dirty nail` and `clean brown water`
   - `rain_shelter` is valid if you use it carefully as a near-boundary supported case
4. Do **not** add new `should_abstain` prompts in this slice; the point is to expand the weak-support answer side first.
5. Keep YAML ASCII-only.

### Edit 5 - run the forked panel

Run the tracked wrapper against the new sidecar with an explicit output path:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run_abstain_regression_panel.ps1 `
  -PanelPath notes/specs/abstain_nearboundary_panel_20260423.yaml `
  -OutputDir artifacts/bench/abstain_nearboundary_20260423 `
  -TopK 8
```

If `powershell` does not resolve cleanly in-session, `powershell.exe` is fine.

Expected outputs:
- `summary.md`
- `summary.json`
- `sweep_summary.csv`
- `query_details.csv`

### Edit 6 - sanity-check the artifact bundle

Do a quick post-run check:

1. Confirm the artifact files above exist.
2. Confirm the panel size in the output matches the YAML sidecar.
3. Confirm the run is still evaluating the current production point (`0.62 / 2`) rather than silently changing constants.
4. Capture any notable panel-level takeaways in your report, but **do not** retune anything in this slice.

### Edit 7 - update `notes/CP9_ACTIVE_QUEUE.md`

Minimal queue changes only:

1. Advance the "Last updated" line to `W-C-0`.
2. Keep the truth that no slices are currently in flight unless your own execution state makes that false.
3. Change the next Wave C move from generic `W-C-0` wording to:
   - `W-C-1` telemetry aggregation helper
4. Append a completed-log entry summarizing:
   - runner-preflight passed
   - tracked helper landed under `scripts/`
   - tracked near-boundary YAML sidecar landed under `notes/specs/`
   - forked panel artifacts written under `artifacts/bench/abstain_nearboundary_20260423/`
5. Do **not** reprioritize unrelated backlog.

### Edit 8 - commit

Single commit only. Suggested subject:

```text
W-C-0: fork nearboundary panel and track abstain runner
```

Tight equivalent is acceptable if it preserves the two core actions:
- near-boundary panel fork landed
- abstain runner surface tracked

## Acceptance

- One commit only.
- `git ls-files` shows these tracked:
  - `scripts/run_abstain_regression_panel.ps1`
  - `scripts/abstain_regression_panel.py`
  - `notes/specs/abstain_nearboundary_panel_20260423.yaml`
- Runner-preflight passed:
  - wrapper CLI shape stayed narrow
  - no secret / HARD-STOP issue was found beyond benign local-runtime references
- The new YAML sidecar:
  - preserves the original 12 baseline queries
  - adds 6-10 new `should_not_abstain` near-boundary queries
  - totals 18-22 queries overall
- The forked artifact bundle exists at:
  - `artifacts/bench/abstain_nearboundary_20260423/`
  - with `summary.md`, `summary.json`, `sweep_summary.csv`, and `query_details.csv`
- `notes/CP9_ACTIVE_QUEUE.md` records `W-C-0` and points next at `W-C-1`.
- No threshold constants changed in `query.py` or Android code.
- The following grep returns **no matches** across the tracked text/code files you touched:

```powershell
rg -n '[^\x00-\x7F]' `
  scripts/run_abstain_regression_panel.ps1 `
  scripts/abstain_regression_panel.py `
  notes/specs/abstain_nearboundary_panel_20260423.yaml `
  notes/CP9_ACTIVE_QUEUE.md
```

- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched tracked files remain unstaged/modified after commit.

## Delegation hints

- Good `gpt-5.4 high` worker slice: modest code surface, bounded write set, one artifact-producing run.
- If delegated, the worker should own the runner preflight, helper promotion, YAML panel fork, artifact run, queue update, and single commit, then hand back the sha plus the key panel facts.

## Anti-recommendations

- Do **not** tune thresholds here.
- Do **not** touch `query.py`, Android code, or tests in this slice.
- Do **not** add the telemetry aggregation helper here; that is `W-C-1`.
- Do **not** broaden into tracking unrelated `scripts/*.ps1`.
- Do **not** move historical baseline artifacts into git.
- Do **not** silently depend on the artifact-side helper after this slice lands; the tracked helper under `scripts/` is the point.

## Report format

- Commit sha + subject.
- Files changed / newly tracked.
- Verification bullets:
  - runner preflight passed
  - tracked helper + YAML sidecar landed
  - artifact bundle path
  - queue now points at `W-C-1`
- Short note on panel size and any notable baseline-vs-nearboundary observation.
- Any out-of-scope drift still remaining after `W-C-0`.

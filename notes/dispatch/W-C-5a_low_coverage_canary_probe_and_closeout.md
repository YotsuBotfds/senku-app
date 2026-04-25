# Slice W-C-5a - run a low-coverage canary probe and close Wave C only if the route stays absent

- **Role:** main agent (`gpt-5.4 xhigh`). Tiny probe-only slice; suitable for a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice is a bounded Android probe with a conditional queue closeout.
- **Why this slice now:** `W-C-5` was always optional. The scout read after `W-C-4` says the downgrade path is implemented and test-covered, but none of the fresh Wave C bundles actually observed `low_coverage_route`. The right next move is therefore **not** a speculative retune. It is a tiny canary probe using prompts with real priors. If the route still stays absent, we close Wave C and move to backlog. If it appears, that is real evidence and we stop instead of pretending the chain is done.

## Outcome

Two possible valid outcomes:

1. **Preferred closeout path**
   - mint a focused canary artifact bundle at:
     - `artifacts/bench/low_coverage_canary_probe_20260423/`
   - confirm `low_coverage_route` stays absent on the canary set
   - update `notes/CP9_ACTIVE_QUEUE.md` to record the probe and close Wave C as complete through `W-C-4`, with `W-C-5` explicitly not warranted on current evidence
   - make one small commit containing only the queue update

2. **Evidence-found path**
   - mint the same canary artifact bundle
   - if `low_coverage_route` appears on any canary, **STOP and report**
   - do **not** update the queue
   - do **not** make a commit

Expected tracked surface in the closeout path: queue update only.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `8f7277f` (`W-C-4: widen uncertain-fit upper boundary to 0.67`) or descends from it.
2. `git status --short -- notes/CP9_ACTIVE_QUEUE.md` is clean before you start. If it is already dirty, **STOP** and escalate.
3. Reconfirm the canary-probe rationale before running:
   - `W-C-1`, `W-C-3`, and `W-C-4` probe summaries all reported `low_coverage_route` count `0`
   - `W-C-5` remains explicitly optional / evidence-gated in the queue
4. Reconfirm the existing probe substrate:
   - `scripts/run_android_prompt.ps1` exists and is tracked
   - `scripts/aggregate_final_mode_telemetry.py` exists and is tracked
   - at least one fixed-matrix emulator is available; preferred device is `emulator-5556`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus remains deferred
   - the broad historical `notes/` backlog remains deferred
   - repo-root zip/screenshot/audit items remain deferred

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `notes/CP9_ACTIVE_QUEUE.md` (and only on the preferred closeout path)
- Generate artifact outputs only under:
  - `artifacts/bench/low_coverage_canary_probe_20260423/`
- Do **not** touch:
  - `query.py`
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `scripts/run_android_prompt.ps1`
  - `scripts/aggregate_final_mode_telemetry.py`
  - `notes/WAVE_C_DIRECTION_20260422.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
- Do **not** turn this into `W-C-5`:
  - no heuristic tuning
  - no code changes
  - no safety-route edits
  - no new canary-note tracked file

## The probe

### Step 1 - use canaries with actual priors

Use these four prompts on a single device. Preferred: `emulator-5556`.

1. **Historical low-coverage prior**
   - `How do I build a simple rain shelter from tarp and cord?`
   - historical prior: old notes once saw this on `low_coverage_route mode=uncertain_fit`
2. **Synthetic telemetry prior**
   - `How should I care for a minor sprain?`
   - unit-test telemetry fixture explicitly expects `route=low_coverage_downgrade`
3. **Synthetic downgrade-to-abstain prior**
   - `how do i tune a violin bridge and soundpost`
   - unit-test fixture explicitly drives low-coverage downgrade
4. **Confident control**
   - `How do I build a cabin roof that sheds rain?`
   - should remain a normal generative control

For each prompt:

- use `-Ask`
- use `-WaitForCompletion`
- use `-ClearLogcatBeforeLaunch`
- save a dedicated logcat file under:
  - `artifacts/bench/low_coverage_canary_probe_20260423/logcats/`

Suggested command shape:

```powershell
powershell.exe -ExecutionPolicy Bypass -File scripts/run_android_prompt.ps1 `
  -Emulator emulator-5556 `
  -Query "<prompt>" `
  -Ask `
  -WaitForCompletion `
  -ClearLogcatBeforeLaunch `
  -LogcatPath artifacts/bench/low_coverage_canary_probe_20260423/logcats/<label>.logcat.txt
```

### Step 2 - aggregate both final_mode and low_coverage_route signals

Run `scripts/aggregate_final_mode_telemetry.py` against the four raw logcats and write:

- `artifacts/bench/low_coverage_canary_probe_20260423/summary.json`
- `artifacts/bench/low_coverage_canary_probe_20260423/summary.md`

The helper already reports `low_coverage_route` lines if present. Use that directly; do not invent a second parser.

### Step 3 - make the close/no-close decision

Close Wave C only if **all** of the following are true:

1. the bundle exists with all four raw logcats plus `summary.json` / `summary.md`
2. the control query still yields a normal final-mode line
3. `low_coverage_route` count is `0`
4. no canary logcat shows a literal `ask.generate low_coverage_route ...` line

If any `low_coverage_route` line appears, **STOP and report**:

- which prompt triggered it
- what final mode it downgraded to
- artifact path

No queue update, no commit.

## Queue update (preferred closeout path only)

If and only if the closeout conditions above are satisfied, update `notes/CP9_ACTIVE_QUEUE.md` minimally:

1. advance the "Last updated" line to this canary probe closeout
2. keep the truth that no slices are in flight
3. change the next move from optional `W-C-5` wording to:
   - Wave C closed through `W-C-4`; move to backlog
4. append a completed-log entry summarizing:
   - canary probe bundle path
   - four prompts used
   - `low_coverage_route` count remained `0`
   - `W-C-5` not warranted on current evidence
5. do **not** reprioritize unrelated backlog

## Commit (preferred closeout path only)

If the queue is updated, make one small commit. Suggested subject:

```text
W-C-closeout: record low-coverage canary probe result
```

Tight equivalent is acceptable if it preserves the two core facts:

- canary probe ran
- `W-C-5` is not warranted on current evidence

## Acceptance

### Closeout path

- One commit only.
- `git diff --name-only HEAD~1 HEAD` is limited to:
  - `notes/CP9_ACTIVE_QUEUE.md`
- Probe bundle exists at:
  - `artifacts/bench/low_coverage_canary_probe_20260423/`
  - with four raw logcats, `summary.json`, and `summary.md`
- `summary.md` reports:
  - control query captured
  - `low_coverage_route` count `0`
- `notes/CP9_ACTIVE_QUEUE.md` records the probe and closes Wave C through `W-C-4`.

### Evidence-found path

- Probe bundle exists with the same files.
- At least one literal `low_coverage_route` line is reported.
- No tracked files changed.
- No commit made.

## Delegation hints

- Good `gpt-5.4 high` worker slice: pure probe, existing helper, conditional queue update.
- If delegated, the worker should own the probe capture, aggregation, the close/no-close decision, the conditional queue update, and the conditional single commit, then hand back the sha or the stop reason.

## Anti-recommendations

- Do **not** write engine code here.
- Do **not** open `W-C-5` just because the probe bundle exists.
- Do **not** update the queue if `low_coverage_route` appears.
- Do **not** treat synthetic low-coverage test priors as proof of runtime regression by themselves; the runtime log decides.

## Report format

- If closeout path:
  - commit sha + subject
  - artifact bundle path
  - prompts used
  - final-mode summary
  - `low_coverage_route` count
  - queue now closed through `W-C-4`
- If evidence-found path:
  - no commit
  - artifact bundle path
  - prompt(s) that triggered `low_coverage_route`
  - downgraded mode(s)
  - recommendation that a real `W-C-5` slice is now warranted

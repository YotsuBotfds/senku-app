# Slice D21 - make the LiteRT push helper direct-stream by default

- **Role:** main agent (`gpt-5.4 xhigh`). Implementation + truth-surface slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice changes helper behavior and the docs/trackers that describe it.
- **Why this slice now:** The current repo has real drift around `push_litert_model_to_android.ps1`. Tracked docs say the helper already has a direct-stream path or should be the normal path, but the script in the workspace is still untracked and tmp-staging-only. The smallest truthful next move is `BACK-P-06`: add a real direct-stream codepath, make it the default, keep tmp-staging behind an explicit opt-in flag, and repair the truth surfaces. Do **not** pull in `BACK-P-07` free-space probing or AVD-sizing work here.

## Outcome

One focused commit that:

1. Tracks `scripts/push_litert_model_to_android.ps1`.
2. Adds a real direct-stream push path to the helper and makes that the default behavior.
3. Keeps the current tmp-staging path behind an explicit opt-in flag such as `-UseTmpStaging` (or a clearly named equivalent).
4. Updates these truth surfaces:
   - `TESTING_METHODOLOGY.md`
   - `android-app/README.md`
   - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
5. Verifies the new default path on one phone AVD and one 6 GB tablet AVD, plus one opt-in tmp-staging run on a roomy emulator.

Important constraint: this is `BACK-P-06`, not `BACK-P-07`. Do **not** add free-space probing, automatic mode heuristics, AVD partition resizing logic, or broader model-import behavior changes.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `048f579072e210fefff2d7bd43e2c6177f163a38` (`D20: track Android follow-up core scripts`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `TESTING_METHODOLOGY.md`
   - `android-app/README.md`
   - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. `scripts/push_litert_model_to_android.ps1` still exists and is currently untracked.
4. A valid LiteRT model file path can be discovered locally for verification.
5. At least one phone AVD and one 6 GB tablet AVD are available for verification. If the exact canonical serials differ from the usual matrix, document what you used.
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - the large untracked `guides/` tree remains
   - the residual historical root `notes/` backlog remains

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `scripts/push_litert_model_to_android.ps1`
  - `TESTING_METHODOLOGY.md`
  - `android-app/README.md`
  - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - any other file under `scripts/`
  - any app-side Kotlin / Java code
  - any file under `guides/`
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - add auto free-space probing or size heuristics
  - add AVD partition resizing logic
  - widen into tablet host-fallback policy
  - widen into app-side import/runtime behavior changes

## Implementation rules

### Step 1 - confirm the drift before editing

Read the helper and the four truth surfaces once so you confirm the current
delta:

- helper is tmp-staging-only today
- docs/tracker currently over-claim or misdescribe the helper behavior
- `BACK-P-06` / `BACK-P-07` are still open in the queue/tracker surface

If the workspace already contains a real direct-stream branch, STOP and report
the mismatch instead of redoing the slice.

### Step 2 - implement direct-stream as the default helper path

Required behavior:

1. The helper must support a real direct-stream path into app storage rather
   than always staging the full model in `/data/local/tmp`.
2. Direct-stream must become the default invocation path.
3. The current tmp-staging behavior must remain available behind an explicit
   opt-in flag such as `-UseTmpStaging` (or a clearly named equivalent).
4. Keep the interface/operator story simple:
   - default = direct-stream
   - explicit flag = old tmp-staging path
5. Bias toward the smallest implementation that works. Do not add free-space
   probing, mode auto-selection heuristics, or posture-specific branching.

Allowed note:

- Emulator/physical-device host-path handling that already belongs to the
  helper’s current scope is fine.

### Step 3 - update docs / tracker surfaces to match reality

Required doc changes:

1. `TESTING_METHODOLOGY.md`
   - describe the helper as direct-stream by default
   - keep tmp-staging as an explicit fallback / opt-in path
   - stop presenting the manual bypass as the normal tablet-only workflow if
     the helper now covers it
2. `android-app/README.md`
   - describe the helper truthfully with the new default/fallback behavior
3. `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
   - fix the materially wrong claim that the current helper already had
     direct-stream before this slice
   - mark `BACK-P-06` appropriately if this slice closes it
   - keep `BACK-P-07` deferred if it remains future work
4. `notes/CP9_ACTIVE_QUEUE.md`
   - update the backlog truth around `BACK-P-06/07`
   - append a completed-log entry for D21

### Step 4 - verification (do not overscope)

Required verification:

1. Default helper invocation succeeds on one phone AVD and one 6 GB tablet AVD.
2. Opt-in tmp-staging path still succeeds on one roomy emulator.
3. Post-push verification per device:
   - app model file is present under `run-as com.senku.mobile ... files/models`
   - model-store prefs reflect the pushed model
   - file hash or at least byte count matches expectations
4. One cold app launch after a default push no longer shows the
   `Import a .litertlm or .task model first` blocker.

Document which serials and model artifact path you used.

## Stop condition

If a real direct-stream default still cannot land a valid model on a clean
6 GB tablet AVD, STOP the slice there and report the evidence. That means the
remaining blocker is broader AVD/storage behavior, not just helper-default
drift. Do **not** widen D21 into `BACK-P-07`, AVD partition resizing, or app
import internals.

## Commit

Single commit only. Suggested subject:

```text
D21: make LiteRT push helper direct-stream by default
```

Tight equivalent is acceptable if it preserves the two core actions:
- implement default direct-stream in the helper
- repair the docs/trackers to match

## Acceptance

- One commit only.
- `scripts/push_litert_model_to_android.ps1` is tracked.
- Default helper behavior is real direct-stream, not tmp-staging-only.
- Tmp-staging remains available behind an explicit opt-in flag.
- Default path validated on one phone AVD and one 6 GB tablet AVD.
- Opt-in tmp path validated on one roomy emulator.
- The four truth surfaces now accurately describe the helper and backlog state.
- No file outside the explicit touch set changed.

## Delegation hints

- Good `gpt-5.4 high` worker slice: bounded helper implementation + verification + doc repair.
- If delegated, the worker should own the helper change, the four doc/tracker
  updates, the device verification, the single commit, and a concise report of
  whether `BACK-P-06` is now closed and what remains for `BACK-P-07`.

## Anti-recommendations

- Do **not** add free-space probes here.
- Do **not** add automatic mode heuristics here.
- Do **not** widen into Kotlin/app import code.
- Do **not** quietly downgrade this into a doc-only slice.

## Report format

- Commit sha + subject.
- Short explanation of the implemented direct-stream vs tmp-staging behavior.
- Validation serials + model path used.
- Summary of each doc/tracker update.
- Whether `BACK-P-06` is now closed and what remains in `BACK-P-07`.

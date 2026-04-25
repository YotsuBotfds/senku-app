# Slice D23 - track the tmp-staging push helper and repair transport truth

- **Role:** main agent (`gpt-5.4 xhigh`). Tracking + doc-truth slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice updates the supported helper surface and the live backlog/tracker truth around it.
- **Why this slice now:** D22 proved the important thing: the existing Windows-host direct-stream family is unsafe or unproven for real binary LiteRT payloads, so `BACK-P-06` is not ready to retry. At the same time, the repo’s preferred documented operator path is still `scripts/push_litert_model_to_android.ps1`, and that helper is still untracked. The tight next move is to track the helper exactly as the tmp-staging helper it really is, and repair the stale “direct-stream is the fix” language in the methodology / tracker / queue.

## Outcome

One focused commit that:

1. Tracks the existing helper as-is:
   - `scripts/push_litert_model_to_android.ps1`
2. Updates:
   - `TESTING_METHODOLOGY.md`
   - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. Leaves `android-app/README.md` untouched unless you discover a concrete false claim there during the read-only pass.

Important constraint: this is **not** a transport redesign slice. Do **not** modify the helper behavior. Do **not** retry direct-stream. Do **not** widen into `BACK-P-06` implementation or `BACK-P-07` heuristics.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `f29e4f27158ba09286ee38b879a3cd65496fe275` (`D22: probe LiteRT transport byte-safety`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `TESTING_METHODOLOGY.md`
   - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
   - `notes/CP9_ACTIVE_QUEUE.md`
3. `scripts/push_litert_model_to_android.ps1` still exists and is currently untracked.
4. `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md` exists and is tracked.
5. Pre-existing unrelated dirt is expected and does **not** block:
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
  - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Do **not** touch:
  - `android-app/README.md` unless you find a clearly false transport claim that cannot be left alone
  - `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md`
  - any other script
  - any app source file
  - `notes/dispatch/README.md`
  - `AGENTS.md`
  - `opencode.json`
- Do **not**:
  - edit helper behavior
  - add direct-stream code
  - add free-space probing or heuristics
  - re-open the D22 probe surface

## Verify / track / edit rules

### Step 1 - narrow Rule-2b / HARD-STOP prepass on the helper

Before tracking the helper, do a narrow read-only prepass on
`scripts/push_litert_model_to_android.ps1`.

Required checks:

1. Read the helper once.
2. Confirm PowerShell parse succeeds.
3. Narrow HARD-STOP scan for:
   - embedded credentials / API keys / bearer tokens
   - force-kill behavior
   - bootstrap/download logic
   - obviously dangerous local filesystem behavior
4. Note but do not “fix” the known caveat:
   - `RemoteTempDir` is user-overridable and later deleted with
     `adb shell rm -rf`; this is a hardening concern, not a reason to widen the slice.

If the helper fails parse or contains a real secret/HARD-STOP issue, STOP
instead of tracking it.

### Step 2 - track the helper as-is

If the prepass is clean, track:

- `scripts/push_litert_model_to_android.ps1`

No content edits unless a tiny parse/encoding fix is absolutely required to
make the tracked file truthful. Bias hard toward tracking the existing helper
unchanged.

### Step 3 - repair the truth surfaces

#### `TESTING_METHODOLOGY.md`

Update the model deploy section so it reflects D22 reality:

1. Keep the helper as the preferred path.
2. Remove the stale recommendation that Windows direct-stream bypass should be
   used as the normal solution on 6 GB tablet AVDs.
3. Replace it with truthful wording:
   - current helper is tmp-staging-based
   - D22 found Windows direct-stream variants unsafe/unproven for real binary payloads
   - constrained 6 GB tablet AVD behavior remains a known blocker rather than a solved direct-stream workaround
4. Link or point readers to `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md`.

#### `notes/REVIEWER_BACKEND_TRACKER_20260418.md`

Repair `BACK-P-06` / `BACK-P-07` so they stop over-claiming.

Required changes:

1. Rewrite the false “current workaround” language that says the helper already
   has a working direct-stream path.
2. Reframe `BACK-P-06` as blocked on discovering a byte-safe transport path,
   with D22 as the current evidence.
3. Keep `BACK-P-07` as future work, but remove the assumption that a Windows
   direct-stream path is already available or ready to become default.
4. Do not invent closure. The rows should stay open or explicitly blocked.

#### `notes/CP9_ACTIVE_QUEUE.md`

Update the queue minimally and truthfully:

1. Advance the `Last updated` line to include D23.
2. In item `(j)`, close the `scripts/push_litert_model_to_android.ps1` defer
   because the helper is now tracked.
3. In the carry-over section, replace the stale “promote direct-stream to
   default path” wording with D22-grounded truth:
   - helper tracked as tmp-staging path
   - Windows direct-stream family remains unsafe/unproven for real binary payloads
   - `BACK-P-06` remains blocked on transport
   - `BACK-P-07` remains future work
4. Add a completed-log entry for D23.
5. Do **not** reshuffle unrelated backlog items.

## Acceptance

- One commit only.
- `scripts/push_litert_model_to_android.ps1` is tracked.
- Narrow Rule-2b / HARD-STOP prepass passed cleanly.
- `TESTING_METHODOLOGY.md` no longer tells operators to treat Windows direct-stream as the solved tablet workaround.
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md` no longer falsely claims the helper already has a working direct-stream path.
- `notes/CP9_ACTIVE_QUEUE.md` no longer lists the helper as an untracked defer and now reflects the D22 transport blocker truth.
- No file outside the explicit touch set changed.

## Commit

Single commit only. Suggested subject:

```text
D23: track push helper and repair transport truth
```

Tight equivalent is acceptable if it preserves the two core actions:
- track the existing helper
- repair the stale transport truth surfaces

## Delegation hints

- Good `gpt-5.4 high` worker slice: narrow prepass + helper tracking + doc repair.
- If delegated, the worker should own the helper prepass, the track action, the
  three doc/tracker updates, the single commit, and a concise statement of what
  remains blocked after D23.

## Anti-recommendations

- Do **not** change helper behavior.
- Do **not** retry D21 or direct-stream transport.
- Do **not** widen into `BACK-P-07` heuristics.
- Do **not** touch the D22 evidence note or artifact bundle.

## Report format

- Commit sha + subject.
- Result of the helper prepass.
- Confirmation that the helper is now tracked unchanged.
- Short summary of each truth-surface update.
- What remains blocked after D23.

# Slice D22 - probe LiteRT transport byte-safety on Windows host

- **Role:** main agent (`gpt-5.4 xhigh`). Investigation slice; safe to delegate to a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice creates a probe harness, runs transport experiments, and writes the evidence note for the failed D21 follow-up.
- **Why this slice now:** D21 stopped for the right reason. The blocker is no longer “make the helper default path nicer”; it is that Windows-host adb direct-stream methods were byte-unsafe for real binary payloads even though tiny probes passed. Before retrying `BACK-P-06`, we need a bounded transport investigation that artifacts which methods are actually safe, unsafe, or unresolved for LiteRT-sized payloads.

## Outcome

One focused commit that:

1. Adds a tracked probe harness:
   - `scripts/probe_litert_model_transport.ps1`
2. Adds a tracked investigation note:
   - `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md`
3. Produces a fresh artifact bundle under:
   - `artifacts/bench/litert_transport_probe_<timestamp>/`
4. Does **not** change `scripts/push_litert_model_to_android.ps1` or any tracker/docs that would imply `BACK-P-06` is solved.

Important constraint: this is an evidence slice, not a helper-rewrite retry. Do **not** touch the push helper or the truth-surface docs/trackers yet.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `048f579072e210fefff2d7bd43e2c6177f163a38` (`D20: track Android follow-up core scripts`) or descends from it.
2. Before you edit, these tracked files are clean in `git status --short`:
   - `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md` must not already exist as a tracked file
3. `scripts/push_litert_model_to_android.ps1` remains untracked and tmp-staging-only at slice start.
4. At least one usable local LiteRT model exists, or enough disk exists to synthesize larger binary probes.
5. At least one Android target is available for probing. Prefer:
   - primary target: `emulator-5556`
   - optional confirmatory target: `emulator-5554` only if a candidate method looks promising and storage is not the confound
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
  - `scripts/probe_litert_model_transport.ps1`
  - `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md`
  - files created under one new artifact root:
    - `artifacts/bench/litert_transport_probe_<timestamp>/...`
- Do **not** touch:
  - `scripts/push_litert_model_to_android.ps1`
  - `TESTING_METHODOLOGY.md`
  - `android-app/README.md`
  - `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
  - `notes/CP9_ACTIVE_QUEUE.md`
  - any app source file
  - any other script unless your probe harness needs to invoke an existing helper read-only
- Do **not**:
  - claim `BACK-P-06` is closed
  - update tracker/docs to imply the helper has a proven direct-stream path
  - widen into free-space heuristics, AVD resizing, app-side import logic, or UI smoke

## Investigation rules

### Step 1 - build a bounded probe harness

Create `scripts/probe_litert_model_transport.ps1` as a focused read/run harness
for comparing transport methods on Windows host.

Required harness capabilities:

1. Accept a target serial and an output directory.
2. Support at least these payload classes:
   - tiny control probe like `256 B`
   - real binary probe `>= 64 MB`
   - one real `.litertlm` model if present locally
3. Support at least these transport methods:
   - current tmp-staging baseline
   - current Windows-host direct-stream recipe
   - at least one alternate stdin-based candidate if practical
4. For each run, record:
   - method name
   - serial
   - payload path
   - expected size
   - local SHA-256
   - remote size
   - remote SHA-256 if available under `run-as`
   - adb stdout/stderr or captured command output
   - cleanup status
   - verdict: `safe`, `unsafe`, or `unresolved`

The harness should bias toward clarity and evidence over elegance. It is okay if
the methods are hard-coded in a small test matrix as long as the results are
clearly captured.

### Step 2 - run the probe on a real target

Run the harness and produce one fresh artifact bundle under:

- `artifacts/bench/litert_transport_probe_<timestamp>/`

Required probe logic:

1. Use `emulator-5556` as the primary target unless it is unavailable.
2. Confirm that tiny control transfer and larger binary transfer are evaluated
   separately.
3. Do not treat “file exists” as success.
4. If a candidate method looks promising on `5556`, you may run one confirmatory
   probe on `5554`, but only if that does not widen into a storage/partition
   investigation.
5. Clean probe artifacts from the device after each method if practical, and
   record whether cleanup succeeded.

### Step 3 - write the investigation note

Write `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md` with:

1. A short statement of why D21 stopped without a commit.
2. The methods tested.
3. The payload classes tested.
4. What the probe proved:
   - which methods are `safe`
   - which methods are `unsafe`
   - which methods remain `unresolved`
5. A short recommended next step:
   - retry `BACK-P-06` only if a byte-safe method is proven
   - otherwise treat the helper/default-path work as still blocked on transport

Also link to the artifact bundle.

### Step 4 - artifact summary

Include a `summary.md` in the artifact bundle with one row per method/payload
pair and a simple verdict.

Preferred columns:

- method
- serial
- payload
- expected_bytes
- remote_bytes
- hash_match
- verdict
- notes

## Acceptance

- One commit only.
- `scripts/probe_litert_model_transport.ps1` is tracked.
- `notes/LITERT_PUSH_TRANSPORT_INVESTIGATION_20260423.md` is tracked.
- One new artifact bundle exists under `artifacts/bench/litert_transport_probe_<timestamp>/`.
- The artifact bundle contains a `summary.md`.
- The results distinguish tiny-probe success from larger-payload behavior.
- No success is claimed without matching byte count and matching hash when available.
- `scripts/push_litert_model_to_android.ps1` remains unchanged and untracked.
- No tracker/docs were updated to imply `BACK-P-06` is solved.

## Commit

Single commit only. Suggested subject:

```text
D22: probe LiteRT transport byte-safety
```

Tight equivalent is acceptable if it preserves the two core actions:
- add the probe harness
- land the investigation evidence

## Delegation hints

- Good `gpt-5.4 high` worker slice: bounded harness + artifact bundle + evidence note.
- If delegated, the worker should own the harness, the probe run, the evidence
  note, the single commit, and a clear statement of whether any byte-safe
  direct-stream candidate was actually proven.

## Anti-recommendations

- Do **not** retry the helper rewrite in this slice.
- Do **not** update `TESTING_METHODOLOGY.md`, `android-app/README.md`,
  `notes/REVIEWER_BACKEND_TRACKER_20260418.md`, or `notes/CP9_ACTIVE_QUEUE.md`.
- Do **not** let a tiny probe stand in for a real binary proof.
- Do **not** widen into AVD storage policy or app behavior work.

## Report format

- Commit sha + subject.
- Artifact path.
- Methods tested and payload classes used.
- Clear verdict for each method family: `safe`, `unsafe`, or `unresolved`.
- Whether `BACK-P-06` is ready to retry or still blocked on transport.

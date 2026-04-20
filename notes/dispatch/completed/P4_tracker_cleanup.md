# Slice P4 — flesh out `BACK-P-06` / `BACK-P-07` + file `assertDetailSettled` blind-spot ticket

- **Role:** main agent (`gpt-5.4 xhigh`). Delegates to Spark xhigh scout
  for the ID audit and `gpt-5.4 high` worker for the tracker edits per
  `notes/SUBAGENT_WORKFLOW.md` and the inline tags below.
- **Parallel with:** S1, P3. No emulator touch.
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → On-deck P4.

## Preconditions

None. Pure tracker edits.

## Outcome

Three tracker edits in `notes/REVIEWER_BACKEND_TRACKER_20260418.md`:

- `BACK-P-06` and `BACK-P-07` stubs fleshed into actionable post-RC
  tickets with problem / workaround / post-RC work / sizing /
  acceptance.
- New test-lane row (likely `BACK-T-05`, but verify next free ID)
  filing the `PromptHarnessSmokeTest.assertDetailSettled` visibility
  blind spot.

## The edits (subagent directive inline per step)

### 0. Scout — next free test-lane ID
*(Spark xhigh — read-only audit)*

Before assigning `BACK-T-05`, grep
`notes/REVIEWER_BACKEND_TRACKER_20260418.md` for existing `BACK-T-`
rows and confirm which ID is next free. Return the ID to main agent
for use in step 3. This is the only read-only step in this slice; the
rest are worker-tier edits.

### 1. `BACK-P-06` — AVD data-partition sizing policy
*(`gpt-5.4 high` — tracker edit)*

Flesh out to:

- **Problem.** Tablet AVDs at ~6 GB data partition cannot tmp-stage
  an E4B-size model via the standard push helper; CP9 Stage 0 v5
  caught this when the push dropped into a silent failure mode.
- **Current runtime workaround.** `push_litert_model_to_android.ps1`
  direct-stream path
  (`adb shell run-as com.senku.mobile sh -c 'cat > files/models/<name>' < local_file`)
  bypasses tmp staging and works on partition-constrained AVDs.
- **Post-RC work.** Promote direct-stream to the default path in
  `push_litert_model_to_android.ps1`; keep tmp-staging behind an
  opt-in `--use-tmp-staging` flag. Sized "small (one script + docs)."
- **Acceptance.** Push helper succeeds on a 6 GB tablet AVD without
  any posture-specific flag.

### 2. `BACK-P-07` — unified LiteRT push helper
*(`gpt-5.4 high` — tracker edit; batch with step 1 in the same commit)*

Flesh out to:

- **Problem.** Today the push path is split between
  `push_litert_model_to_android.ps1` (tmp-staging) and the manual
  `adb shell run-as cat` recipe (direct-stream). Dispatches have to
  know which to use, and the wrong choice fails silently on
  constrained AVDs.
- **Post-RC work.** Consolidate behind a free-space probe — if
  `stat -f` (or equivalent `run-as` `df`) on
  `/data/user/0/com.senku.mobile/` shows less than `2× model_size`
  free, auto-select direct-stream; else tmp-staging. Sized "small
  (one script)."
- **Acceptance.** One entrypoint works on both phone and tablet
  AVDs without posture-specific flags, and fails loud (not silent)
  when neither path can stage the model.

### 3. Test-lane blind-spot row — `assertDetailSettled`
*(`gpt-5.4 high` — tracker edit; use the ID returned by step 0)*

File under the test lane:

- **Problem.** `PromptHarnessSmokeTest.assertDetailSettled` reports
  `status: pass` on `emulator-5560` landscape when the captured
  `scriptedPromptFlowCompletes__prompt_detail.xml` is IME-dominated
  and does NOT contain the detail body, UNSURE FIT chip, or
  escalation sentence. The assertion passes on structural-hierarchy
  checks that don't verify Wave B textual content. A1b landed a
  harness-side BACK-dismiss fix (`9cf405c`) that worked around the
  dump issue, but the underlying assertion is still weak.
- **Evidence.**
  `artifacts/cp9_stage0_20260419_142539/smoke_emulator-5560_v6/instrumented_summary_landscape.json`
  shows `status: pass, artifact_expectations_met: true` against the
  108,768-byte IME-dominated dump (pre-A1b).
- **Post-RC work.** Tighten `assertDetailSettled` to require
  presence of the escalation sentence (or an equivalent body-content
  signal) when the expected mode is `uncertain_fit` or `abstain`.
  Tag as a test-quality / safety-critical-escalation-visibility
  ticket. Sized "medium (test + plumbing)."
- **Acceptance.** Re-running the pre-A1b v6 5560 landscape smoke
  against an IME-dominated dump reports `status: fail` with a
  body-content assertion message; portrait and tablet postures
  continue to pass.

## Acceptance

- One commit with the three tracker edits.
- Each ticket has: problem, current workaround (if any), post-RC
  work, sizing, acceptance criteria.
- ID used for the test-lane row is the next free ID, confirmed via
  the step-0 grep.

## Boundaries

- No production code edits.
- No `PromptHarnessSmokeTest.java` edits — A1b already landed the
  harness fix; the test-lane ticket is a post-RC follow-up.
- No edits to any existing `BACK-*` row beyond `BACK-P-06` /
  `BACK-P-07`.
- Do not touch P3's docs edits.

## Report format

Reply with:

- Commit sha.
- The three tracker rows, quoted verbatim.
- Final IDs used (and the step-0 grep evidence).
- Delegation log: which lane ran each step and a one-line "why."

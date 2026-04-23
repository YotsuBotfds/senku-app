# Daylight Hygiene Queue - 2026-04-19

> Historical 2026-04-19 planning/queue note.
>
> Current-checkout status as of 2026-04-23: the overnight launcher surfaces
> referenced here still assume `scripts/overnight_continue_loop.ps1`, but that
> file is absent in this checkout. See
> `notes/OVERNIGHT_LAUNCHER_MISSING_LOOP_INVESTIGATION_20260423.md` for the
> current-state investigation.

**Context:** The 2026-04-19 overnight low-stakes queue closed 6/6
(T-04, H-03, H-06, D-03, H-01, P-03). Wave B closed before that
(U-01 `eb398dc`, U-02 `d974ebc`, U-03 `af49d91`). This daylight
queue clears operational hygiene items surfaced during that work so
the next queue (CHECKPOINT 9 or beyond) starts from a cleaner base.

**Tree posture:** HEAD at `6af041f` (BACK-P-03 tracker closeout).
Working tree has the standing untracked baseline; no modified tracked
files. This is the preflight-safe state the 2026-04-19 preflight-gate
fix (`df70a43`) was designed to recognize.

**Queue (3 tasks, strict order, risk-ascending):**

1. `DAY-G-01` (P2 · XS · worker docs) - Commit first `.gitignore`
2. `DAY-D-01` (P2 · XS · worker docs) - No-op code commit protocol rule
3. `DAY-L-01` (P1 · M · worker scripts) - Overnight launcher guardrail Phase 1

Ordering puts the two small docs tasks first so they land quickly and
the bigger launcher work runs on a tree that already reflects the
.gitignore cleanup (preflight diffs will be smaller and easier to read
during L-01's own validation).

---

## Immutability Rules

**Off-limits for this entire queue:**

- Wave B files: `_should_abstain` / `build_abstain_response` /
  uncertain-fit body builder / post-rerank pipeline in `query.py`,
  and `shouldAbstain` / `buildAbstainAnswerBody` / `AnswerRun` /
  `AnswerRunResult` / `AnswerMode` / `generate()` branching in
  `OfflineAnswerEngine.java`, plus any `AnswerPresenter` symbols
- BACK-P-03 audit code (`ingest.py` bridge-tag section and
  `tests/test_bridge_tag_consistency.py`) - landed `aa2373c`
- The 11 guide files edited in `2f664bd` - no further guide edits
  from this queue
- The existing overnight loop scripts (`scripts/overnight_continue_loop.ps1`,
  `scripts/start_overnight_continuation.ps1`) - **DAY-L-01 adds a
  NEW wrapper script; it does not modify these**
- Any release-gate file, `OPUS-*`, `BACK-R-*`

Any out-of-scope touch is a hard stop.

---

## Concurrency / Bookkeeping Rules

- **Per-task: land a code/docs commit, then land a tracker commit,
  then start the next task.** Same pattern as the 2026-04-19
  overnight queue.
- No Wave B tracker state changes. Wave B is closed.
- Code and tracker commits stay separate. Never mix them.

## Note Cell Hygiene (carried forward from overnight queue)

State Log `note` cells must not contain literal `|` characters, even
inside inline code backticks. See
`notes/OVERNIGHT_LOW_STAKES_QUEUE_U01_INFLIGHT_2026-04-18.md` for the
full rule and rewrites table.

---

## Dispatch Preamble (paste to Codex)

You are working on a post-overnight clean tree. The overnight queue
closed 6/6 at commit `6af041f`. Complete the three tasks below in
strict order. After each task's tracker commit, run the 8-cell State
Log row validator; if it fails, stop and log.

Ground rules:

- No Wave B edits. No BACK-P-03 audit edits. No touching the 11 guide
  files from `2f664bd`.
- Code and tracker commits stay separate, one task at a time.
- If a spec file is referenced for a task, read it fully before
  starting that task. Follow the spec's acceptance criteria.
- If any task's acceptance criteria cannot be met without out-of-scope
  changes, stop and report at the end of that task. Do not cascade.

Pre-flight runs once before Task 1. Use the gate language from
`notes/OVERNIGHT_LOW_STAKES_QUEUE_U01_INFLIGHT_2026-04-18.md` - the
`git diff --name-only` / `git diff --cached --name-only` both-empty
check. Untracked files are expected and acceptable.

Write a run log at
`artifacts/daylight_hygiene_20260419/run_log.md`. Same per-task
structure as the overnight run log.

---

## Pre-flight (run once before Task 1)

```
cd <repo root>
git diff --name-only
git diff --cached --name-only
```

Both must be empty. If either returns any path, stop and log.

```
python3 -m unittest discover -s tests -v
python3 scripts/validate_special_cases.py
```

Both must pass.

State Log row-format validation:

```
python3 -c "
text = open('reviewer_backend_tasks.md', encoding='utf-8').read()
log = text.split('## State Log', 1)[1]
rows = [l for l in log.splitlines() if l.startswith('|') and ('BACK-' in l or 'OPUS-' in l or 'DAY-' in l)]
for r in rows:
    cells = r.split('|')
    assert len(cells) == 8, f'bad row: {r!r} ({len(cells)} cells)'
print(f'{len(rows)} rows validated')
"
```

All rows must pass before starting Task 1.

---

## Per-task commit convention

Code/docs commit subject: `DAY-<id> <one-line imperative summary>`
Tracker commit subject: `Tracker: record DAY-<id> landing`

State Log row format (append one per task):

```
| DAY-XX-nn | done | codex | 2026-04-19 | 2026-04-19 | <summary> |
```

Apply the note-cell hygiene rule. Validate after each tracker commit.

---

## Task 1 - `DAY-G-01` - First `.gitignore`

**Spec:** `notes/specs/gitignore_cleanup_spec.md`

**Files allowed:** `.gitignore` (new), `reviewer_backend_tasks.md`
(tracker commit).

**Behavior:** Write `.gitignore` per the spec's exact content block.
Verify with `git status --short` that the ignored categories disappear
and the explicitly-not-ignored files remain visible. Code commit, then
tracker commit. Add DAY-G-01 to the root task plan task body and State
Log per the usual pattern.

**Out of scope:** Any change to documentation notes or other untracked
files. `.gitignore` content only.

---

## Task 2 - `DAY-D-01` - No-op code commit protocol rule

**Files allowed:**
`notes/OVERNIGHT_LOW_STAKES_QUEUE_U01_INFLIGHT_2026-04-18.md` (add
rule), `reviewer_backend_tasks.md` (tracker commit).

**Behavior:** During BACK-H-03 (2026-04-19), the requested change was
already present in immutable Wave B code. Codex landed an empty-change
code commit (`739d26f`) rather than skipping the code commit entirely.
Result: noise in the log and a no-op appearing in `git log --oneline`.

Add a rule to the overnight queue note's "Per-task commit convention"
section that codifies the correct protocol when a requested change is
already present:

> **No-op code commit rule.** If a task's requested change is already
> present in the codebase (either because immutable upstream code
> already satisfies the intent or because a prior commit landed it),
> do NOT create an empty or trivial code commit. Instead:
>
> 1. Record the observation in the run log with the specific
>    verification command and its output (e.g., `rg -n '<pattern>'`
>    showing zero legacy hits).
> 2. Skip directly to the tracker commit. The State Log row's note
>    cell should begin with `already present in <where>` and reference
>    the verification.
> 3. Use `none` as the code commit hash in the run log table row
>    (e.g., `| BACK-X-nn | pass | none | <tracker-hash> | NN:NN |`).
>
> This preserves the audit trail without cluttering the git history
> with no-op commits.

Place the new rule right after the "Per-task commit convention"
subsection heading. Do not touch other content in that file.

Code/docs commit: `DAY-D-01 add no-op code commit rule to overnight
queue protocol`.

Then tracker commit as usual.

**Out of scope:** Any retroactive revert of `739d26f` — it is landed
history, leave it. Also do NOT modify
`scripts/overnight_continue_loop.ps1` or the Codex dispatch templates
— the rule lives in the queue note for now.

---

## Task 3 - `DAY-L-01` - Overnight launcher guardrail Phase 1

**Spec:** `notes/specs/overnight_launcher_guardrail_spec.md` — **read
this fully before starting this task. All acceptance criteria are in
the spec.**

**Files allowed:**

- `scripts/run_overnight_queue_wrapped.ps1` (new)
- `tests/powershell/Run-OvernightQueueWrapperTests.ps1` (new)
- `reviewer_backend_tasks.md` (tracker commit)

**Files explicitly NOT allowed:**

- `scripts/overnight_continue_loop.ps1` — unchanged in Phase 1
- `scripts/start_overnight_continuation.ps1` — unchanged in Phase 1
- Any Python, Java, Kotlin, or guide file

**Behavior:** Implement the Phase 1 wrapper per spec. Preflight + per-
task timeout + stale-lock sweep. Do not exceed Phase 1 scope (no
retry, no isolation, no notification hooks — those are Phase 2).

After implementation, exercise the happy path manually:

```
pwsh scripts/run_overnight_queue_wrapped.ps1 -QueueNote notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md -PerTaskTimeoutMinutes 30
```

This should run through preflight and exit cleanly because the queue
note's tasks are already landed (G-01 and D-01 will have landed by
this point). Record the exit code in the run log. Exit `0` expected.

If exit is non-zero, inspect the wrapper's run log and report before
committing.

If preflight passes but the wrapper fails for any structural reason
(missing directory, script error, etc.), fix the wrapper and re-run.
Do not commit until the happy path completes cleanly.

Code commit: `DAY-L-01 add overnight launcher guardrail wrapper (Phase 1)`.
Then tracker commit.

**Out of scope:** Any Phase 2 item listed in the spec. Do not
speculatively implement retry or isolation.

---

## After the queue

- Queue done = 3/3 tasks landed with State Log rows and no Wave B
  touches.
- If any task stops for acceptance-criteria reasons, log a "carry-
  forward" entry in the run log describing what was deferred and why.
- Do not re-touch Wave B, BACK-P-03 audit, or the 11 guide files
  under any circumstance.

Final report (post-queue): three code/docs commit hashes, three
tracker commit hashes, State Log row count before and after, test
count from the final pass, list of files changed per `git diff
6af041f..HEAD --name-only`, and confirmation that all off-limits
files are untouched.

Stop after reporting.

# Overnight Sequential Dispatch - 2026-04-18

**Runs after:** Wave B remainder (`BACK-U-02`, `BACK-U-01`) has landed and
its tracker bookkeeping commit has landed. Do not start this queue until
both are done.

**Queue (5 tasks, strict order):**

1. `BACK-T-04` (P2 · XS · worker test-infra) - PowerShell harness repair
2. `BACK-H-03` (P2 · XS · worker) - Registry builder type hint
3. `BACK-H-06` (P2 · XS · worker) - FtsRuntime fallback docstring
4. `BACK-H-01` (P2 · S · worker) - Tag normalization DRY
5. `BACK-P-03` (P2 · S · worker) - Bridge-tag consistency audit

All five are P2 mechanical backlog items with crisp acceptance criteria
and no emulator dependency. Ordering is by size ascending so the cheap
wins land first and any stall leaves the high-value small fixes already
on disk.

**Intentionally excluded from overnight:**

- `BACK-L-01` (latency timing) - the task body flags a must-bundle with
  `BACK-L-02`, and L-02 requires schema judgment that is not
  overnight-safe.
- `BACK-L-02`, `BACK-L-03` - schema + plumbing decisions.
- `BACK-R-02` - retrieval tuning; needs validation-pack review.
- `BACK-R-05` - it's a decision, not a build.
- `BACK-T-03` - depends on `BACK-U-04` state being fully reconciled in
  the tracker first.
- `CHECKPOINT 9` - release gate, needs playtest sign-off.

---

## Dispatch Prompt (paste to Codex)

You are running an **overnight sequential queue** of five P2 backlog
tasks. No reviewer will be awake to answer questions. Your rules:

- **One task at a time, in the declared order.** Do not skip ahead.
- **Per-task: land a code commit, then land a tracker commit, then start
  the next task.** The tracker commit appends the State Log row and
  flips the task-body header in `reviewer_backend_tasks.md`; it is
  separate from the code commit so rollback granularity stays clean.
- **Hard stop on first red test.** If `python3 -m unittest discover -s
  tests -v` fails at any point, stop, write a stop report to the log
  (see "Logging" below), and do not start the next task.
- **Hard stop on out-of-scope file touches.** If a task would require
  editing any file not listed in its own "Files" section, stop and log
  rather than expanding scope.
- **Hard stop on 60 minutes wall-clock per task.** These are all
  XS or S tasks. If any task is still open at 60 minutes, the task has a
  hidden complexity and deserves daylight.
- **No emulator required.** None of these tasks need ADB or a pack
  push. If you find yourself wanting to start an emulator, you have
  wandered off spec.
- **Do not touch any `BACK-U-*` task, `OPUS-*` task, or release-gate
  file.** Wave B bookkeeping is complete before this queue runs; do not
  retouch it.

### Pre-flight (run once before task 1)

```
cd <repo root>
git status
```

Confirm the working tree is clean (no untracked dispatch prompt files
are acceptable; any modified tracked file is not). If dirty, stop and
log.

```
python3 -m unittest discover -s tests -v
python3 scripts/validate_special_cases.py
```

Both must pass before starting task 1. If either fails, the repo is not
in a clean baseline state; stop and log without starting.

Also confirm the State Log is in good shape:

```
python3 -c "
text = open('reviewer_backend_tasks.md', encoding='utf-8').read()
log = text.split('## State Log', 1)[1]
rows = [l for l in log.splitlines() if l.startswith('|') and ('BACK-' in l or 'OPUS-' in l)]
for r in rows:
    assert len(r.split('|')) == 8, f'bad row: {r!r}'
print(f'{len(rows)} rows validated')
"
```

If any row fails, the pre-overnight tracker repair did not fully land;
stop and log without starting.

### Logging

Create `artifacts/overnight_20260418/run_log.md` at the start of the
run. Append one section per task using this template:

```markdown
## <task-id> - <start-iso8601>

**Status:** <pass|fail|timeout|out-of-scope>
**Code commit:** <hash or 'none'>
**Tracker commit:** <hash or 'none'>
**Unit tests:** <test names + pass/fail>
**Elapsed:** <MM:SS>
**Notes:** <one paragraph, or blocker description if not pass>
```

Also append a top-level "Summary" section at the end of the run (or at
the point of stop), listing the status of every task in the queue,
including tasks that were not started because of an earlier stop.

### Tracker-commit template (reuse per task)

After each code commit lands, run:

```
# Append the State Log row
# Flip the task-body header from its current state to [done <YYYY-MM-DD> `<hash>`]
# If opustasks.md references the task, update there too
git add reviewer_backend_tasks.md
# include opustasks.md only if step edited it
git commit -m "Tracker: record BACK-<task-id> landing"
```

The State Log row format for these tasks:

```
| BACK-XX-nn | done | codex | 2026-04-18 | 2026-04-18 | <one-line summary matching the code commit subject> |
```

Validate after each tracker commit with the same Python one-liner from
pre-flight. If it fails, stop and log.

### Commit subject conventions

Code commit subject: `BACK-<id> <one-line imperative summary>`
e.g. `BACK-T-04 Repair Quote-AndroidShellArg in session-flow harness`

Tracker commit subject: `Tracker: record BACK-<id> landing`

---

## Task 1 - `BACK-T-04` - Fix `Quote-AndroidShellArg` in session-flow harness

**Files:** `scripts/run_android_session_flow.ps1`

**Behavior:** The script currently fails on `emulator-5556` with
`Quote-AndroidShellArg not recognized` because the helper is referenced
but not defined or imported. Fix by one of:

- Inlining the quoting logic directly in the script (preferred if the
  helper is only used once or twice).
- Dot-sourcing the helper from a sibling script that already defines it
  (`grep -l Quote-AndroidShellArg scripts/`).
- Importing from an existing PowerShell utility module if one exists.

**Accept:**

- The script runs cleanly against `emulator-5556` **without producing
  the `Quote-AndroidShellArg` error**. You do not need an emulator to
  verify this - syntax-check with `powershell.exe -NoProfile -Command
  "& { . .\scripts\run_android_session_flow.ps1 -WhatIf }"` or the
  equivalent parse-only mode. If your environment can't do even a parse
  check, document that in the log and continue.
- No other PowerShell script in `scripts/` regresses (run a
  `Get-Command` parse on siblings that `dot-source` or `Import-Module`
  the same helper, if any).

**Out of scope:**

- Do not touch any other script.
- Do not attempt to drive the session flow end-to-end. The acceptance
  here is just "no longer throws the quoting error."

**Commit subject suggestion:**
`BACK-T-04 Repair Quote-AndroidShellArg in session-flow harness`

---

## Task 2 - `BACK-H-03` - Type hint on registry builder

**Files:** `query.py:5364` (approximate; grep for the exact symbol if
line has drifted)

**Behavior:** Find the `Callable[[], str] | None` type hint on the
registry builder signature. Change to `Callable[..., str] | None` so
future builders that take the question as context don't trip the type
check.

**Accept:**

- `python3 -m unittest discover -s tests -v` passes.
- `mypy query.py` (if the repo has mypy configured) does not regress.
  If mypy is not configured, skip.
- `grep -n 'Callable\[\[\], str\] | None' query.py` returns no hits
  after the edit.

**Out of scope:**

- Do not refactor any callsite. Only the type hint changes. Callsites
  compatible with `Callable[[], str] | None` are also compatible with
  `Callable[..., str] | None`.

**Commit subject suggestion:**
`BACK-H-03 Relax registry builder Callable signature`

---

## Task 3 - `BACK-H-06` - Document `FtsRuntime` fallback chain

**Files:** `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
around the `FtsRuntime` class

**Behavior:** Add a class-level Javadoc to `FtsRuntime` documenting the
fallback chain:

- FTS5 preferred when `lexical_chunks_fts5` exists and the runtime
  supports the `MATCH` operator.
- FTS4 fallback when `lexical_chunks_fts4` exists and FTS5 is
  unavailable.
- `LIKE` fallback when neither FTS table exists.
- Performance implications of each path (FTS5 fastest; FTS4 within
  5-10%; LIKE an order of magnitude slower and suitable only as a
  correctness net).

Phrase it as a 10-20 line Javadoc block, not a prose essay.

**Accept:**

- `./gradlew.bat :app:compileDebugJavaWithJavac` passes.
- No behavioral change; the commit is documentation-only.

**Out of scope:**

- Do not change any method in `FtsRuntime`.
- Do not modify `PackRepository` outside the Javadoc.

**Commit subject suggestion:**
`BACK-H-06 Document FtsRuntime fallback chain`

---

## Task 4 - `BACK-H-01` - Consolidate tag normalization

**Files:** `query.py:5520-5540`, `ingest.py:418-440`, new
`metadata_helpers.py`

**Behavior:** The `_normalize_metadata_tag` in `query.py` and
`normalize_tag_value` in `ingest.py` duplicate tag normalization logic.
DRY this by:

1. Create `metadata_helpers.py` in the repo root with a single
   canonical function `normalize_metadata_tag(value: str) -> str`.
   Function body is the strictly shared behavior of the two existing
   implementations. If the two differ, pick the superset behavior and
   document divergences in a module docstring.
2. Replace both call sites to import from `metadata_helpers`. Rename is
   fine - keep the existing public names in `query.py` and `ingest.py`
   as thin wrappers or aliases if any downstream code imports them.
3. Add `tests/test_metadata_helpers.py` with at least five cases
   covering: lower-case normalization, whitespace stripping, punctuation
   handling, unicode folding behavior, and idempotency.

**Accept:**

- `python3 -m unittest discover -s tests -v` passes, including the new
  test file.
- `grep -rn 'def _normalize_metadata_tag\|def normalize_tag_value' .`
  shows the functions either deleted or reduced to single-line
  delegators.
- The new helper returns identical output to both pre-refactor
  implementations on the test cases.

**Out of scope:**

- Do not refactor any other helper. If you notice another duplicated
  helper pair, note it in the log and move on - it's another task.
- Do not add any dependency. `metadata_helpers.py` should be pure
  stdlib.

**Commit subject suggestion:**
`BACK-H-01 Consolidate metadata tag normalization into shared helper`

---

## Task 5 - `BACK-P-03` - Bridge-tag consistency audit

**Files:** `ingest.py:692` (the `bridge` field assignment),
`query.py:5543-5553` (`_is_bridge_guide_metadata` with fallback logic)

**Behavior:** At ingest validation time, assert that if a guide is
detected as bridge-pattern, the `bridge` boolean is set **and** no stale
CSV tag containing `"bridge-guide"` remains in the tag list. Log a
warning on any guide that appears in both old-tag and new-tag form.

Implementation notes:

- Add the check to the ingest validation pass, not at query time.
- Warning message format: `bridge-tag inconsistency: <guide-id> has
  bridge=<True|False> and tag_has_bridge_guide=<True|False>`.
- Do not change `_is_bridge_guide_metadata` itself - the audit is
  additive and reports on inconsistencies rather than fixing them
  silently.

**Accept:**

- Re-ingest on the current guide corpus produces **zero** inconsistency
  warnings. If any warnings appear, the corpus has genuine
  inconsistencies - log them in the run log and stop so a human can
  decide whether to fix the corpus or relax the assertion.
- New unit test in `tests/test_bridge_tag_consistency.py` that
  constructs a minimal guide with both flags, asserts the warning
  fires; also a clean guide that does not produce a warning.
- `python3 -m unittest discover -s tests -v` passes.

**Out of scope:**

- Do not edit any guide file. This task audits and reports; the fix
  (if any) is a separate content task.
- Do not change query-time bridge logic.

**Commit subject suggestion:**
`BACK-P-03 Add ingest-time bridge-tag consistency audit`

---

## After the queue

When all five tasks land, write the Summary section of the run log and
stop. **Do not start any task outside this queue**, even if time
remains.

If any task stops early, the remaining tasks in the queue stay
unstarted. Tate will decide in the morning whether to resume, re-order,
or defer.

Final commit of the run: none required beyond the per-task tracker
commits. The run log is an artifact, not a tracker entry.

### Morning report (append at end of run_log.md)

```markdown
## Summary - <iso8601 end time>

| Task | Status | Code commit | Tracker commit | Elapsed |
| ---- | ------ | ----------- | -------------- | ------- |
| BACK-T-04 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-H-03 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-H-06 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-H-01 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-P-03 | <status> | <hash> | <hash> | <MM:SS> |

**Overall:** <N of 5 tasks landed>
**First red (if any):** <task-id + summary>
**Follow-ups surfaced:** <anything discovered mid-task that deserves a
future task>
```

That's the morning handoff. Tate will read the run log and the five
code diffs (or fewer, if the queue stopped early) and decide the next
move.

# Low-Stakes Queue (Post-Wave-B) - 2026-04-18

**Context:** Wave B closed before this queue runs. `BACK-U-01` landed
`eb398dc`, `BACK-U-02` landed `d974ebc`, `BACK-U-03` landed `af49d91`.
The Wave B closure tracker commit recorded all three landings. This
queue runs on a clean tree with Wave B code and tracker state frozen.

**Wave B files are immutable from this queue's perspective.** That
includes `_should_abstain` / `build_abstain_response` / uncertain-fit
body builder / post-rerank pipeline in `query.py`, and `shouldAbstain` /
`buildAbstainAnswerBody` / `AnswerRun` / `AnswerRunResult` / `AnswerMode` /
`generate()` branching in `OfflineAnswerEngine.java`, plus any
`AnswerPresenter` symbols added by U-03. Any out-of-scope touch of
Wave B files is a hard stop.

**Supersedes:** `notes/OVERNIGHT_DISPATCH_PROMPT_2026-04-18.md`. That
earlier 5-task prompt is a subset of this one.

**Queue (6 tasks, strict order, risk-ascending):**

1. `BACK-T-04` (P2 · XS · worker test-infra) - PowerShell harness repair
2. `BACK-H-03` (P2 · XS · worker) - Registry builder type hint
3. `BACK-H-06` (P2 · XS · worker) - FtsRuntime fallback docstring
4. `BACK-D-03` (P1 · XS · worker) - Builder-missing telemetry
5. `BACK-H-01` (P2 · S · worker) - Tag normalization DRY
6. `BACK-P-03` (P2 · S · worker) - Bridge-tag consistency audit

Ordering puts the lowest-risk mechanical fixes first so the cheap wins
land if anything stalls later. `BACK-D-03` sits at the tail of the XS
block because it involves telemetry pattern integration (higher
uncertainty than the pure type/docstring fixes). `BACK-P-03` is last
because its acceptance ("zero inconsistency warnings on current corpus")
can surface real data issues that deserve daylight.

---

## Excluded (with rationale)

All excluded items are explicitly out of scope for this run. Do not
touch any of them.

**Wave B files (immutable from this queue):**
- Any file Wave B edited. Specifically: `query.py` around
  `_should_abstain` / `build_abstain_response` / the confidence-label
  pipeline / uncertain-fit body builder; `OfflineAnswerEngine.java`
  around `shouldAbstain` / `buildAbstainAnswerBody` / `AnswerRun` /
  `AnswerRunResult` / `AnswerMode` / the `generate()` branching site;
  `AnswerPresenter.java` symbols added by U-03; uncertain-fit spec
  files under `notes/specs/`.

**Blocked by schema / design judgment:**
- `BACK-L-01` - must-bundle with `BACK-L-02`.
- `BACK-L-02` - `SenkuLatency` JSON schema is a design decision.
- `BACK-L-03` - depends on `BACK-L-02`.
- `BACK-D-01` - requires `notes/specs/deterministic_rule_graduation.md`
  (new spec doc with reviewer judgment on graduation gates).
- `BACK-D-02` - depends on `BACK-D-01`; also requires picking one of
  three semantic-gate strategies.
- `BACK-D-04` - must-bundle with `BACK-D-02`.
- `BACK-D-05` - must-bundle with `BACK-D-01` (same file, same region).
- `BACK-D-06` - new fail-fast script that could block pack builds if
  false-alarms; deserves daylight review of the manifest format first.

**Blocked by retrieval-tuning judgment:**
- `BACK-R-02` - tunes bridge-guide demotion thresholds against the
  validation pack. Retune decisions need daylight.

**Not-a-build tasks:**
- `BACK-R-05` - scout decision on Android anchor-prior productization.
- `CHECKPOINT 9` - release gate, requires playtest sign-off.

**Blocked by tracker reconciliation:**
- `BACK-T-03` - depends on `BACK-U-04` State Log state being fully
  reconciled; `BACK-U-04` is marked landed in tracker prose but its
  State Log row was not confirmed to exist at the start of this run.
  Skip rather than guess.

**Wave B tracker bookkeeping:**
- Already closed in the Wave B closure commit that landed before this
  queue started. Do not re-edit any U-lane State Log row, task-body
  header, or `opustasks.md` U-lane entry. If something looks wrong in
  Wave B tracker state, stop and log — do not fix in this queue.

---

## Dispatch Prompt (paste to Codex)

You are running a **sequential queue of six P1/P2 backlog tasks** on
a post-Wave-B clean tree. Wave B (`BACK-U-01` `eb398dc`, `BACK-U-02`
`d974ebc`, `BACK-U-03` `af49d91`) has landed, and a Wave B closure
tracker commit recorded all three landings before this queue began. No
reviewer will be awake for most of this run. Your rules:

### Wave B immutability rules

- **Do not edit any Wave B file.** That includes `query.py` around
  `_should_abstain` / `build_abstain_response` / the post-rerank
  pipeline / the uncertain-fit body builder, and
  `OfflineAnswerEngine.java` around `shouldAbstain` /
  `buildAbstainAnswerBody` / `AnswerRun` / `AnswerRunResult` /
  `AnswerMode` / the `generate()` branching site, plus any
  `AnswerPresenter` symbols added by U-03. If a task in this queue
  appears to require editing any of those, the task has grown out of
  scope — stop and log.
- **Do not re-edit Wave B tracker state.** The Wave B closure commit
  already recorded the U-lane landings. No State Log row flips for
  `BACK-U-*`, no task-body header edits on `BACK-U-*`, no edits to the
  Wave B section of `notes/REVIEWER_BACKEND_TRACKER_20260418.md`, no
  `opustasks.md` U-lane edits. If Wave B tracker state looks wrong,
  stop and log — do not fix.

### Run discipline

- **One task at a time, in the declared order.** Do not skip ahead.
- **Per-task: land a code commit, then land a tracker commit, then
  start the next task.** The tracker commit appends the State Log row
  (only for the task you just landed) and flips the task-body header in
  `reviewer_backend_tasks.md`.
- **Hard stop on first red test.** If `python3 -m unittest discover -s
  tests -v` fails at any point, stop, write a stop report to the run
  log, and do not start the next task.
- **Hard stop on out-of-scope file touches.** If a task would require
  editing a file not in its own "Files" section, stop and log rather
  than expanding scope.
- **Hard stop on 60 minutes wall-clock per task.** These are all
  XS or S tasks. If any is open at 60 minutes, the task has hidden
  complexity and deserves daylight.
- **No emulator required.** None of these tasks need ADB or a pack
  push. If you find yourself wanting to start an emulator, you have
  wandered off spec.
- **No touching `BACK-U-*`, `OPUS-*`, `BACK-L-*`, `BACK-D-01`,
  `BACK-D-02`, `BACK-D-04`, `BACK-D-05`, `BACK-D-06`, `BACK-R-*`, or
  any release-gate file.**

### Pre-flight (run once before task 1)

```
cd <repo root>
git diff --name-only
git diff --cached --name-only
```

The gate is **no modified tracked files and no staged changes**. Both
commands above must return empty output. Untracked files are expected
and acceptable — this checkout carries a standing local-artifact
baseline (build artifacts, generated HTML reports, `.gradle-*/` caches,
`.tmp/`, editor metadata, dispatch notes, etc.) that lives outside
version control intentionally. Do **not** use `git status` output alone
as the gate — the untracked baseline will always produce noise there
and is not a failure signal.

If either `git diff` command returns any path, stop and log.

```
python3 -m unittest discover -s tests -v
python3 scripts/validate_special_cases.py
```

Both must pass before starting task 1. If either fails, the repo is
not in a clean baseline state; stop and log without starting any task.

State Log row-format validation:

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

Every row must pass the 8-cell check. If any fails, the pre-overnight
tracker repair did not fully land; stop and log without starting.

### Logging

Create `artifacts/overnight_lowstakes_20260418/run_log.md` at the start
of the run. Append one section per task using this template:

```markdown
## <task-id> - <start-iso8601>

**Status:** <pass|fail|timeout|out-of-scope>
**Code commit:** <hash or 'none'>
**Tracker commit:** <hash or 'none'>
**Unit tests:** <test names + pass/fail>
**Elapsed:** <MM:SS>
**Notes:** <one paragraph, or blocker description if not pass>
```

Append a top-level "Summary" section at the end of the run (or at the
point of stop), listing the status of every task in the queue,
including tasks that were not started because of an earlier stop.

### Tracker-commit template

**No-op code commit rule.** If a task's requested change is already
present in the codebase (either because immutable upstream code
already satisfies the intent or because a prior commit landed it),
do NOT create an empty or trivial code commit. Instead:

1. Record the observation in the run log with the specific
   verification command and its output (e.g., `rg -n '<pattern>'`
   showing zero legacy hits).
2. Skip directly to the tracker commit. The State Log row's note
   cell should begin with `already present in <where>` and reference
   the verification.
3. Use `none` as the code commit hash in the run log table row
   (e.g., `| BACK-X-nn | pass | none | <tracker-hash> | NN:NN |`).

This preserves the audit trail without cluttering the git history
with no-op commits.

After each code commit lands, run:

```
# Append the State Log row for this task only
# Flip the task-body header to [done <YYYY-MM-DD> `<hash>`]
# If opustasks.md references the task, update there too
git add reviewer_backend_tasks.md
# include opustasks.md only if step edited it
git commit -m "Tracker: record BACK-<id> landing"
```

State Log row format:

```
| BACK-XX-nn | done | codex | 2026-04-18 | 2026-04-18 | <one-line summary matching the code commit subject> |
```

**Note cell hygiene.** The `note` cell must not contain any literal
`|` characters, **including inside inline code backticks**. The 8-cell
row validator splits on literal `|` regardless of Markdown code-span
syntax, so a pipe inside backticks still breaks the row.

Common offenders and rewrites:

- Python union type hints: `Callable[..., str] | None` →
  `optional Callable[..., str]` (or `Callable[..., str] or None` in
  prose, or the HTML entity `&#124;` if the exact token must survive)
- Shell pipelines in notes: `grep ... | wc -l` →
  `grep ... and count with wc -l` (or drop the pipeline and describe
  the intent)
- Regex alternation: `foo|bar` → `foo or bar`

If a literal `|` is truly unavoidable, use `&#124;`. Prefer prose over
entities.

Validate after each tracker commit with the pre-flight Python
one-liner. If it fails, stop and log.

### Commit subject conventions

Code commit subject: `BACK-<id> <one-line imperative summary>`
Tracker commit subject: `Tracker: record BACK-<id> landing`

---

## Task 1 - `BACK-T-04` - Fix `Quote-AndroidShellArg` in session-flow harness

**Files:** `scripts/run_android_session_flow.ps1` only.

**Behavior:** The script currently fails on `emulator-5556` with
`Quote-AndroidShellArg not recognized` because the helper is referenced
but not defined or imported. Fix by one of:

- Inlining the quoting logic in the script (preferred if used once or
  twice).
- Dot-sourcing from a sibling script that defines it (`grep -l
  Quote-AndroidShellArg scripts/`).
- Importing from an existing PowerShell utility module.

**Accept:**

- The script no longer throws the `Quote-AndroidShellArg` error. Verify
  with a parse-only check like `powershell.exe -NoProfile -Command "& {
  Get-Command -Syntax .\scripts\run_android_session_flow.ps1 }"` or the
  local equivalent. If the environment cannot parse-check PowerShell,
  document and continue.
- No other script in `scripts/` that shares the helper regresses.

**Out of scope:** any other script; any emulator run.

**Commit subject:** `BACK-T-04 Repair Quote-AndroidShellArg in session-flow harness`

---

## Task 2 - `BACK-H-03` - Relax registry builder `Callable` signature

**Files:** `query.py` around line 5364 (grep for the exact symbol if
line has drifted).

**Behavior:** Change the `Callable[[], str] | None` type hint on the
registry builder signature to `Callable[..., str] | None` so future
builders that take the question as context don't trip the type check.

**Accept:**

- `python3 -m unittest discover -s tests -v` passes.
- `grep -n 'Callable\[\[\], str\] | None' query.py` returns no hits
  after the edit.
- `mypy query.py` if the repo has mypy configured (skip if not).

**Out of scope:** any callsite refactor.

**Commit subject:** `BACK-H-03 Relax registry builder Callable signature`

---

## Task 3 - `BACK-H-06` - Document `FtsRuntime` fallback chain

**Files:** `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
around the `FtsRuntime` class only.

**Behavior:** Add a class-level Javadoc block to `FtsRuntime`
documenting the fallback chain:

- FTS5 preferred when `lexical_chunks_fts5` exists and the runtime
  supports `MATCH`.
- FTS4 fallback when `lexical_chunks_fts4` exists and FTS5 is
  unavailable.
- `LIKE` fallback when neither FTS table exists.
- Performance implications: FTS5 fastest; FTS4 within 5-10%; `LIKE` an
  order of magnitude slower and suitable only as a correctness net.

10-20 lines of Javadoc, not a prose essay. Documentation-only; no
behavioral change.

**Accept:**

- `./gradlew.bat :app:compileDebugJavaWithJavac` passes.
- Diff is Javadoc-only.

**Out of scope:** any method body in `FtsRuntime` or `PackRepository`.

**Commit subject:** `BACK-H-06 Document FtsRuntime fallback chain`

---

## Task 4 - `BACK-D-03` - Surface builder-missing telemetry

**Files:** `query.py` around lines 5496-5507 (`build_special_case_response`).

**Behavior:** When a rule fires but its builder is `None` or missing,
emit a structured `deterministic.builder_missing` telemetry event. In
debug builds only, append a one-line note to the answer:
`"(rule matched, builder unavailable - falling back to retrieval)"`.
Prod builds stay silent to the user but still log the event.

**Before coding:** Grep the repo for existing structured-telemetry
patterns so the new event matches the convention:

```
grep -n 'emit\|log_event\|structured_log\|telemetry' query.py | head -20
grep -rn 'deterministic\.' . | head -20
```

Reuse the existing emit path. Do not invent a new logging framework.

For the debug-vs-prod gate, reuse whatever mechanism already exists in
the repo. Options to check (in order):

1. A `DEBUG` or `SENKU_DEBUG` flag in `config.py`.
2. An env var convention already used elsewhere (`grep -n 'os\.environ'
   query.py config.py`).
3. `sys.flags.debug` if no explicit flag exists.

If none of the above exist, use `os.environ.get("SENKU_DEBUG_MODE") ==
"1"` and document the new convention in the commit body.

**Accept:**

- New unit test in `tests/test_deterministic_builder_missing.py` that:
  - constructs a rule spec with `builder=None` and calls
    `build_special_case_response`,
  - asserts the telemetry event fires,
  - asserts the debug-note suffix appears when the debug flag is set,
  - asserts the suffix does **not** appear when the debug flag is off.
- `python3 -m unittest discover -s tests -v` passes.
- Event name is exactly `deterministic.builder_missing` (not
  `deterministic_builder_missing` or a variant).

**Out of scope:**

- Do not modify any rule's `builder` field. The fix here is
  observability, not correction.
- Do not touch `OfflineAnswerEngine.java` for Android parity. Android
  parity is a separate task.

**Commit subject:** `BACK-D-03 Emit structured telemetry when deterministic builder is missing`

---

## Task 5 - `BACK-H-01` - Consolidate tag normalization

**Files:** `query.py:5520-5540`, `ingest.py:418-440`, new
`metadata_helpers.py`, new `tests/test_metadata_helpers.py`.

**Behavior:** The `_normalize_metadata_tag` in `query.py` and
`normalize_tag_value` in `ingest.py` duplicate tag normalization
logic. DRY by:

1. Create `metadata_helpers.py` in the repo root with a canonical
   `normalize_metadata_tag(value: str) -> str`. Body is the strict
   superset of the two existing implementations. If they diverge,
   document the choice in a module docstring.
2. Replace both call sites to import from `metadata_helpers`. If any
   downstream code imports the existing public names, keep them as
   single-line wrappers.
3. Add `tests/test_metadata_helpers.py` with at least five cases
   covering: lower-case normalization, whitespace stripping,
   punctuation handling, unicode folding, and idempotency.

**Accept:**

- `python3 -m unittest discover -s tests -v` passes including the new
  test file.
- `grep -rn 'def _normalize_metadata_tag\|def normalize_tag_value' .`
  shows the functions either deleted or reduced to single-line
  delegators.
- The new helper returns identical output to both pre-refactor
  implementations on the test cases.

**Out of scope:**

- Any other duplicated helper pair. Note and move on.
- Any new third-party dependency. `metadata_helpers.py` must be pure
  stdlib.

**Commit subject:** `BACK-H-01 Consolidate metadata tag normalization into shared helper`

---

## Task 6 - `BACK-P-03` - Bridge-tag consistency audit at ingest

**Files:** `ingest.py:692` (`bridge` field assignment),
`query.py:5543-5553` (`_is_bridge_guide_metadata`), and new
`tests/test_bridge_tag_consistency.py`.

**Behavior:** At ingest validation time, assert that if a guide is
detected as bridge-pattern, the `bridge` boolean is set **and** no
stale CSV tag containing `"bridge-guide"` remains in the tag list.
Log a warning on any guide in both old-tag and new-tag form.

Implementation notes:

- Add the check to the ingest validation pass, not at query time.
- Warning format: `bridge-tag inconsistency: <guide-id> has
  bridge=<True|False> and tag_has_bridge_guide=<True|False>`.
- Do not change `_is_bridge_guide_metadata` itself. The audit is
  additive and reports on inconsistencies rather than silently fixing
  them.

**Accept:**

- Re-ingest the current guide corpus produces **zero** inconsistency
  warnings. If any warnings appear, the corpus has genuine
  inconsistencies - log them in the run log and stop so a human can
  decide whether to fix the corpus or relax the assertion. Do not
  proceed to commit in that case.
- New unit test constructs a minimal guide with both flags set, asserts
  the warning fires; also a clean guide that produces no warning.
- `python3 -m unittest discover -s tests -v` passes.

**Out of scope:**

- Any guide-file content edit.
- Any query-time bridge logic change.

**Commit subject:** `BACK-P-03 Add ingest-time bridge-tag consistency audit`

---

## After the queue

When all six tasks land, write the Summary section of the run log and
stop. **Do not start any task outside this queue**, even if time
remains.

Do not re-touch Wave B tracker state. The Wave B closure commit landed
before this queue started; re-editing U-lane rows or headers would
duplicate bookkeeping. If something looks wrong in Wave B state, stop
and log — do not fix in this queue.

### Morning report (append at end of run_log.md)

```markdown
## Summary - <iso8601 end time>

| Task | Status | Code commit | Tracker commit | Elapsed |
| ---- | ------ | ----------- | -------------- | ------- |
| BACK-T-04 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-H-03 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-H-06 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-D-03 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-H-01 | <status> | <hash> | <hash> | <MM:SS> |
| BACK-P-03 | <status> | <hash> | <hash> | <MM:SS> |

**Overall:** <N of 6 tasks landed>
**First red (if any):** <task-id + summary>
**Follow-ups surfaced:** <anything discovered mid-task that deserves a
future task>
**Corpus issues (BACK-P-03 only):** <list of inconsistency warnings
raised, if any>
```

Morning: Tate reads the run log, the code diffs, and the tracker
diffs, and decides next moves.

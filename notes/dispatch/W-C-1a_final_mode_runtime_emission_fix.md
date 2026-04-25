# Slice W-C-1a - restore durable `ask.generate final_mode=` runtime emission

- **Role:** main agent (`gpt-5.4 xhigh`). Narrow Android blocker slice; suitable for a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This is a blocker-clearing Android/runtime lane with a fresh on-device verification run.
- **Why this slice now:** `W-C-1` hard-stopped without landing. The helper/parser idea was sound, but the runtime substrate was not: a fresh clean-install run on `emulator-5556` still produced zero `ask.generate final_mode=` lines even though `OfflineAnswerEngine.java` contains the log callsites and the freshly built debug APK contains the `final_mode` string. That means the next move is not "retry aggregation"; it is "fix the runtime breadcrumb so aggregation has a trustworthy source."

## Outcome

One commit that:

1. Lands the smallest durable Android-side fix so `ask.generate final_mode=` is emitted on all answer paths with reliable runtime visibility.
2. Adds or extends focused Android unit coverage so the breadcrumb is harder to regress.
3. Produces a fresh verification bundle under:
   - `artifacts/bench/final_mode_emission_probe_20260423/`
4. Updates `notes/CP9_ACTIVE_QUEUE.md` so this blocker slice is recorded and `W-C-1` returns as the next Wave C move.

Expected code surface: one Android runtime file, one Android unit-test file, one queue update.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `47559ae` (`W-C-0: fork nearboundary panel and track abstain runner`) or descends from it.
2. `git status --short -- android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java notes/CP9_ACTIVE_QUEUE.md` is clean before you start. If any of those tracked files are already dirty, **STOP** and escalate.
3. Reconfirm the blocker fact before editing:
   - `rg -l "ask\\.generate final_mode=" artifacts -g "logcat.txt"` returns no matches
4. Reconfirm the source/APK mismatch before editing:
   - `OfflineAnswerEngine.java` contains `logAskFinalMode(...)` callsites inside `generate(...)`
   - the current debug APK still contains the `final_mode` string (string-scan is fine; do not skip this sanity check)
5. At least one fixed-matrix emulator is available. Preferred device for the verification run:
   - `emulator-5556`
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus remains deferred
   - the broad historical `notes/` backlog remains deferred
   - repo-root zip/screenshot/audit items remain deferred

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Generate artifact outputs only under:
  - `artifacts/bench/final_mode_emission_probe_20260423/`
- Do **not** touch:
  - `android-app/app/src/main/java/com/senku/mobile/AnswerPresenter.java`
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - `scripts/run_android_prompt.ps1`
  - `scripts/aggregate_final_mode_telemetry.py`
  - `notes/WAVE_C_DIRECTION_20260422.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
  - any file under `guides/`
  - any other Android production/test file
- Do **not** turn this into `W-C-1` or `W-C-2`:
  - no aggregation helper
  - no threshold tuning
  - no prompt-harness redesign
  - no broad state-pack rerun
  - no queue reprioritization outside this blocker and the existing `W-C-1` next step

## The edits

### Edit 1 - restate the blocker with repo-backed evidence

Before changing code, capture and report these facts:

1. `rg -l "ask\\.generate final_mode=" artifacts -g "logcat.txt"` returns no matches.
2. `OfflineAnswerEngine.java` contains `logAskFinalMode(...)` callsites on:
   - deterministic return
   - early abstain return
   - early `uncertain_fit` return
   - low-coverage downgrade return
   - confident/source-summary return
3. The freshly built debug APK contains the `final_mode` string even though runtime logcat did not show it.

This is not optional bookkeeping. It is the reason this slice exists.

### Edit 2 - diagnose the narrow seam in `OfflineAnswerEngine`

Work inside `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java` only.

Required diagnosis target:

- explain why neighboring breadcrumbs like `ask.generate ... totalElapsedMs=...`, `ask.generate low_coverage_route ...`, `ask.uncertain_fit ...`, `paper_card rendered ...`, and `detail.mode ...` can appear while the literal `ask.generate final_mode=` breadcrumb does not

Allowed conclusion shapes:

- a path-specific emission gap inside `generate(...)`
- a logger-priority / logger-path visibility issue for this breadcrumb
- a small helper-level bug that makes `final_mode` less durable than the adjacent logs

Do **not** stop at mystery. The slice must land the smallest durable fix.

### Edit 3 - land the smallest durable emission fix

Implement the narrowest fix that makes the breadcrumb reliably visible in runtime logcat on every final answer path.

Hard requirements:

1. The literal emitted line shape must still begin with:

```text
ask.generate final_mode=
```

2. The fix must cover all current final answer exits in `generate(...)`:
   - deterministic
   - early abstain
   - early `uncertain_fit`
   - low-coverage downgrade
   - confident generation / source-summary fallback
3. The fix may strengthen log durability if needed.
   - If the diagnosis shows the current `logDebug(...)` path is not dependable for this breadcrumb, it is acceptable to elevate just this telemetry line to a more durable Android log level or dual-emit it through the existing sink plus a direct Android runtime log.
4. Preserve the existing breadcrumb fields:
   - `final_mode`
   - `route`
   - `query`
   - `totalElapsedMs`
5. Keep the line ASCII-only.

Not acceptable:

- parser-only work
- UI-surface inference instead of engine emission
- broad logging refactors unrelated to this breadcrumb

### Edit 4 - add focused unit coverage

Extend `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java` with focused coverage that protects the landed fix.

Required test intent:

1. capture emitted debug/runtime breadcrumb text via the existing test sink
2. assert the literal `ask.generate final_mode=` breadcrumb is emitted for:
   - one early-return path (`abstain` or `uncertain_fit`)
   - one non-early path (`confident_generation`, `source_summary_fallback`, or low-coverage downgrade)
3. assert the emitted line still includes:
   - expected `final_mode`
   - expected `route`

Keep the tests narrow. Reuse existing helpers if possible.

### Edit 5 - rebuild, reinstall, and prove the runtime fix on-device

From `android-app/`, do a clean build/install:

```powershell
./gradlew.bat clean :app:assembleDebug :app:installDebug --console=plain
```

Then mint a fresh verification bundle at:

- `artifacts/bench/final_mode_emission_probe_20260423/`

Use `scripts/run_android_prompt.ps1` against a single device. Preferred device: `emulator-5556`.

Required prompt buckets:

1. **Confident**
   - `How do I use a simple lever to move a heavy stone?`
2. **Uncertain fit**
   - `How do I build a simple rain shelter from tarp and cord?`
3. **Abstain**
   - `How do I debug a Python script that reads a CSV file?`

For each prompt:

- use `-Ask`
- use `-WaitForCompletion`
- use `-ClearLogcatBeforeLaunch`
- write a dedicated raw logcat file under:
  - `artifacts/bench/final_mode_emission_probe_20260423/logcats/`

Suggested command shape:

```powershell
powershell.exe -ExecutionPolicy Bypass -File scripts/run_android_prompt.ps1 `
  -Emulator emulator-5556 `
  -Query "<prompt>" `
  -Ask `
  -WaitForCompletion `
  -ClearLogcatBeforeLaunch `
  -LogcatPath artifacts/bench/final_mode_emission_probe_20260423/logcats/<label>.logcat.txt
```

If one bucket misses the literal `final_mode` line after your code fix, you may retry that bucket once. If it still misses, **STOP** and report instead of hand-waving the fix as good.

### Edit 6 - write a tiny verification summary

Inside `artifacts/bench/final_mode_emission_probe_20260423/`, write:

- `summary.md`

Required content:

- device serial used
- prompts used
- one extracted `ask.generate final_mode=...` line per successful bucket
- note whether the line came from confident, `uncertain_fit`, and abstain
- short note on the landed fix shape (for example: "final_mode breadcrumb now dual-emits through durable runtime logging")

Keep it ASCII-only and concise.

### Edit 7 - update `notes/CP9_ACTIVE_QUEUE.md`

Minimal queue changes only:

1. advance the "Last updated" line to this blocker slice
2. keep the truth that no slices are currently in flight unless your own execution state makes that false
3. record that `W-C-1` hard-stopped on a runtime breadcrumb gap and this slice fixed/verified that gap
4. set the next substantive Wave C move back to:
   - `W-C-1` telemetry aggregation helper
5. do **not** reprioritize unrelated backlog

### Edit 8 - commit

Single commit only. Suggested subject:

```text
W-C-1a: restore final-mode runtime emission
```

Tight equivalent is acceptable if it preserves the two core actions:

- final-mode breadcrumb runtime fix landed
- on-device verification bundle minted

## Acceptance

- One commit only.
- `git diff --name-only HEAD~1 HEAD` is limited to:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Fresh verification bundle exists at:
  - `artifacts/bench/final_mode_emission_probe_20260423/`
  - with raw logcats under `logcats/`
  - and `summary.md`
- `rg -n "ask\\.generate final_mode=" artifacts/bench/final_mode_emission_probe_20260423/logcats -g "*.txt"` returns matches after the fix.
- The fresh bundle includes at least one literal `final_mode` line each for:
  - `confident`
  - `uncertain_fit`
  - `abstain`
- The queue records this blocker slice and points next at `W-C-1`.
- The following grep returns **no matches** across the tracked text/code files you touched:

```powershell
rg -n '[^\x00-\x7F]' `
  android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java `
  android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java `
  notes/CP9_ACTIVE_QUEUE.md
```

- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched tracked files remain unstaged/modified after commit.

## Delegation hints

- Good `gpt-5.4 high` worker slice: one narrow Android logging fix, one small test extension, one three-prompt on-device proof run, one queue update.
- If delegated, the worker should own the blocker restatement, the runtime fix, the focused test change, the fresh verification bundle, the queue update, and the single commit, then hand back the sha plus the extracted `final_mode` lines.

## Anti-recommendations

- Do **not** write the aggregation helper here.
- Do **not** tune abstain thresholds here.
- Do **not** touch `AnswerPresenter.java`, `DetailActivity.java`, or the prompt harness unless the slice is otherwise impossible; if it looks impossible without widening, **STOP** and report.
- Do **not** paper over the issue by inferring final mode from UI logs.
- Do **not** broaden into a full Android validation matrix rerun.

## Report format

- Commit sha + subject.
- Files changed.
- Verification bullets:
  - blocker facts reconfirmed before edit
  - landed fix shape
  - unit coverage added/extended
  - fresh verification bundle path
  - queue now points back at `W-C-1`
- Extracted `ask.generate final_mode=...` lines for confident / `uncertain_fit` / abstain.
- Any out-of-scope drift still remaining after `W-C-1a`.

# CP9 Stage 0 Dispatch - Pre-rebuild Verification

Date: 2026-04-19
Parent plan: `notes/CHECKPOINT_9_PLAN_2026-04-19.md`

---

## Purpose

Verify that post-Wave-B system state is actually live end-to-end
before rebuilding the RC v3 packet. Without this, Stage 1's rebuild
could pass against a stale pre-Wave-B mobile pack and prove nothing.

Stage 0 is pure verification. No code commits. Artifacts and a
summary land under `artifacts/cp9_stage0_<timestamp>/`.

---

## Ground Rules

- No code commits. No tracker commits. Artifact output only.
- No Wave B file edits. No guide edits. No edits to any tracked file.
- If any step fails, stop and log. Do not proceed to the next step.
- Do not start Stage 1 (RC v3 packet rebuild) in the same session.

## Off-limits files

Same immutability set as the daylight hygiene queue:

- All Wave B code in `query.py` and `OfflineAnswerEngine.java`
- BACK-P-03 audit code (`ingest.py` bridge-tag section and
  `tests/test_bridge_tag_consistency.py`)
- The 11 guide files edited in `2f664bd`
- Any `OPUS-*` or `BACK-R-*` file

Any edit to these is a hard stop.

---

## Step 0 - verify starting state

```
git status
git log --oneline -6
git diff --name-only
git diff --cached --name-only
```

HEAD should be `65252f7` (DAY-L-01 tracker closeout). Both `git diff`
commands must return empty. If not, stop and log.

Create the output directory:

```
mkdir -p artifacts/cp9_stage0_$(date +%Y%m%d_%H%M%S)
```

Store the path as `$STAGE0_DIR` and use it in every subsequent step.

---

## Step 1 - desktop re-ingest

Refresh ChromaDB with current ingest logic (includes the BACK-P-03
bridge-tag audit from `aa2373c`).

```
python3 ingest.py --stats 2>&1 | tee $STAGE0_DIR/desktop_reingest.txt
```

Expected: clean ingest of 754 guides with bridge-tag audit passing
on all. If any guide fails the bridge-tag audit, stop and log — that
is a real corpus regression, not something to paper over in Stage 0.

Acceptance: exit code 0 and no audit failures in the output.

---

## Step 2 - desktop bench rerun

Run the full prompt suite against the freshly ingested corpus to
catch any retrieval quality regression from the P-03 guide fixes.

```
ls -d artifacts/bench/*/ | sort -r | head -5 > $STAGE0_DIR/baseline_candidates.txt
```

Pick the most recent run that looks like a complete baseline (check
for `summary.json` or equivalent). Record the path as the baseline.

Then:

```
python3 bench.py --prompts test_prompts.txt --output $STAGE0_DIR/desktop_bench/ 2>&1 | tee $STAGE0_DIR/desktop_bench_run.txt
```

Compare new run vs baseline. For each section (24 sections across
171 prompts):

- PASS count must be >= (baseline PASS count - 1). A drop of one
  within a section is acceptable noise; more than one is a
  regression.
- Any section that went 0-for-N in baseline is not a regression
  floor — do not accidentally tighten the bar.

Write the comparison summary to
`$STAGE0_DIR/desktop_bench_regression_check.md` with:

- Baseline path
- Per-section PASS counts (baseline vs new)
- Any flagged regressions with the offending prompts

Acceptance: no section drops more than one pass vs baseline. If any
does, stop and log.

---

## Step 3 - mobile pack re-export

Rebuild the mobile pack from current post-Wave-B desktop state.

```
python3 scripts/export_mobile_pack.py 2>&1 | tee $STAGE0_DIR/mobile_pack_export.txt
```

Record the new pack hash and location in the output file. If the
script has options for specifying output path or pack name, prefer
a timestamped path that does not overwrite any existing pack
currently referenced by another artifact.

Acceptance: export completes with exit code 0; new pack hash
recorded.

---

## Step 4 - launch emulator matrix

Start the four-emulator matrix if not already running.

```
powershell -NoProfile -File scripts/start_senku_emulator_matrix.ps1 2>&1 | tee $STAGE0_DIR/emulator_matrix_start.txt
```

(Use `pwsh` if installed; fall back to Windows PowerShell as above.)

Verify all four emulators are present:

```
adb devices 2>&1 | tee $STAGE0_DIR/adb_devices.txt
```

Must list all four:

- `emulator-5556` phone portrait
- `emulator-5560` phone landscape
- `emulator-5554` tablet portrait
- `emulator-5558` tablet landscape

Acceptance: all four emulators present in `adb devices`. If any fail
to start, stop and log.

---

## Step 5 - mobile pack hot-swap to all four emulators

For each emulator, push the refreshed pack. Exact parameter name
depends on the hot-swap script's signature — read the script head or
run with `-?` to confirm before executing:

```
powershell -NoProfile -Command "Get-Help scripts/push_mobile_pack_to_android.ps1 -Detailed"
```

Then for each serial:

```
powershell -NoProfile -File scripts/push_mobile_pack_to_android.ps1 <params-for-emulator-5556> 2>&1 | tee $STAGE0_DIR/pack_push_5556.txt
powershell -NoProfile -File scripts/push_mobile_pack_to_android.ps1 <params-for-emulator-5560> 2>&1 | tee $STAGE0_DIR/pack_push_5560.txt
powershell -NoProfile -File scripts/push_mobile_pack_to_android.ps1 <params-for-emulator-5554> 2>&1 | tee $STAGE0_DIR/pack_push_5554.txt
powershell -NoProfile -File scripts/push_mobile_pack_to_android.ps1 <params-for-emulator-5558> 2>&1 | tee $STAGE0_DIR/pack_push_5558.txt
```

Acceptance: every push exits 0. If any fails, stop and log.

---

## Step 6 - Wave B live-on-device smoke

On each emulator, run the reviewer-worked example and verify
uncertain-fit renders.

Query: `He has barely slept, keeps pacing and muttering to himself, and refuses to eat. What should we do?`

Use the existing prompt-automation harness:

```
powershell -NoProfile -File scripts/run_android_prompt.ps1 -Serial emulator-5556 -Query "He has barely slept, keeps pacing and muttering to himself, and refuses to eat. What should we do?" -Output $STAGE0_DIR/smoke_5556/
```

(Adjust parameter names to what the script actually accepts — read
the script head first.)

Repeat for 5560, 5554, 5558.

For each emulator, record:

- Mode reported (must be `uncertain_fit`)
- UI evidence: `UNSURE FIT` pill visible in screenshot; warning wash
  on card; `!` icon (not the normal evidence dot)
- Body text: first sentence starts with "Senku found guides that may
  be relevant to"
- Escalation line: since the reviewer example is safety-critical, the
  escalation line must appear above the "Possibly relevant guides in
  the library:" block

Write a per-emulator summary to `$STAGE0_DIR/smoke_summary.md` with
pass/fail and the specific evidence pointer (screenshot path, logcat
line, or UI dump field).

Acceptance: all four emulators show uncertain-fit with escalation.
If any emulator does NOT render uncertain-fit on this query, stop
and log. Two likely causes:

- The pack push did not take (investigate push output)
- Wave B code is not live on that emulator (investigate pack hash
  vs installed APK state)

---

## Step 7 - identify fallback uncertain-fit query

Primary trigger can be fragile under rebuild conditions (indexing
ordering, tie-breaking, metadata profile shifts). Identify a second
query that reliably triggers uncertain-fit as a fallback.

Candidate queries to try on `emulator-5556`:

1. `My friend's electric drill keeps overheating during long jobs. What should I do?`
2. `The well water tastes metallic lately and the kids won't drink it.`
3. `Someone's foot swelled overnight but there's no wound. What now?`

For each, run through `run_android_prompt.ps1` and capture:

- Mode reported (`confident` / `uncertain_fit` / `abstain`)
- Top-row RRF strength
- Vector similarity of top hit
- Whether safety-critical frame was detected

Pick the first candidate that fires `uncertain_fit` and has at least
moderate signal on why (e.g., RRF < 0.65 or similarity in
[0.45, 0.62]). Document in
`$STAGE0_DIR/fallback_query.md` with:

- Query text
- Mode
- Trigger signal (which rule of U-01 spec fired)
- Top 3 retrieval hits

If none of the candidates fires uncertain-fit, try variations
(change specificity, change symptom/tool/setting detail). Do not
stop this step until a fallback is confirmed.

Acceptance: fallback query documented.

---

## Step 8 - write summary

Create `$STAGE0_DIR/summary.md` with:

```
# CP9 Stage 0 Summary

Date: <ISO timestamp>
HEAD: 65252f7

## Steps

| Step | Name | Status | Artifact |
| ---- | ---- | ------ | -------- |
| 1 | desktop re-ingest | pass/fail | desktop_reingest.txt |
| 2 | desktop bench rerun | pass/fail | desktop_bench_regression_check.md |
| 3 | mobile pack re-export | pass/fail | mobile_pack_export.txt |
| 4 | emulator matrix launch | pass/fail | adb_devices.txt |
| 5 | mobile pack hot-swap | pass/fail | pack_push_*.txt |
| 6 | Wave B live smoke | pass/fail | smoke_summary.md |
| 7 | fallback query | pass/fail | fallback_query.md |

## Key Values

- New mobile pack hash: <hash>
- Baseline bench path: <path>
- Fallback uncertain-fit query: <text>

## Stage 0 verdict: GREEN / RED

If GREEN, Stage 1 (RC v3 packet rebuild) can proceed.
If RED, Stage 1 is blocked pending resolution of <failed step>.
```

---

## Step 9 - report

Reply with:

- Each of steps 1-7 pass/fail
- Path to `$STAGE0_DIR/summary.md`
- New mobile pack hash from Step 3
- Fallback uncertain-fit query text and trigger signal from Step 7
- Any regressions surfaced in the bench rerun (Step 2)
- Confirmation all four emulators show Wave B live (Step 6)
- Path to the artifact directory

Stop after reporting. Do NOT start Stage 1 in the same session.

## If a Step Fails

Log the failure in the summary and stop. Failure recovery is
planner work, not executor work — do not attempt to paper over a
failing step to continue the chain. Surfacing a Stage 0 failure is
the whole point of Stage 0 existing.

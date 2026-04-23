# Slice R-tool2 — State-pack logcat capture wiring

- **Role:** main agent (`gpt-5.4 xhigh`). Single-file PowerShell edit plus a focused validation run on one emulator role. Not worker-delegable end-to-end (Step 3 validation requires emulator access, which stays on the main lane); individual read-only steps may fan out to Spark or worker lanes per Delegation hints.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** `D7_post_r_ret1b_tracker_reconciliation.md` if that's in flight (different files — D7 is docs + rotations under `notes/`, R-tool2 is `scripts/build_android_ui_state_pack.ps1` + one artifact dir). No file overlap.
- **Predecessor context:** Closes the `state-pack logcat_path: null` tooling gap that hit diagnosis twice — once in R-host §8 and again cross-cutting in R-search. Root cause is narrow: `build_android_ui_state_pack.ps1` invokes `run_android_instrumented_ui_smoke.ps1` once per per-role state but does NOT pass `-CaptureLogcat`, so the smoke script at `run_android_instrumented_ui_smoke.ps1:1266` writes `logcat_path = $null` into every per-state `summary.json` it produces. The smoke script already has full logcat-capture machinery (`Capture-LogcatSnapshot` at line 564-585, wired at 1093-1097 and 1133-1136) — it just isn't being asked to use it. `Write-TrustedPackSummary` at `build_android_ui_state_pack.ps1:178-210` copies all source properties through to the trusted output, so no changes are needed in the trust layer — the fix propagates purely from the smoke invocation.

## Pre-state (verified by planner via direct file probe, 2026-04-21 late)

Probed `artifacts/cp9_stage2_rerun4_20260420_143440/ui_state_pack/20260420_143855/raw/20260420_143858_318/emulator-5554/summary.json` (a recent state-pack output from RC v5 validation). Field at line 74: `"logcat_path": null`. Matches the evidence-hit pattern from R-host §8 and R-search.

Code anchors in `scripts/build_android_ui_state_pack.ps1` (771 lines):
- **Line 596-606**: smoke-script invocation args for the common (non-detail_followup) path. This is the ONLY place `-CaptureLogcat` needs to be added for the primary fix.
- **Line 511-594**: detail_followup-runner branch (invokes `run_android_detail_followup.ps1` via `$detailFollowupScript` at line 23 — NOT the `_logged` variant). Not exercised by any currently-wired state: state definitions at lines 414-440 all use the default `"instrumented"` runner per `New-StateDefinition` at `:345-364`. No live state triggers this branch, so it's out-of-scope for this slice. (Separate observation for a future slice: `run_android_detail_followup.ps1` has no logcat references, and the pack summary rebuilt at `build_android_ui_state_pack.ps1:569-576` doesn't add `logcat_path` either — when that runner is eventually wired into a state, a separate tooling slice will need to add logcat capture there.)
- **Line 178-210**: `Write-TrustedPackSummary` — preserves all source properties including `logcat_path`. No change needed.

In `scripts/run_android_instrumented_ui_smoke.ps1`:
- **Line 30**: `[switch]$CaptureLogcat` is a defined parameter (default $false).
- **Line 1093-1097 and 1133-1136**: capture-execution blocks gated by `$CaptureLogcat`.
- **Line 1266**: summary-writer uses `$CaptureLogcat` to decide whether to populate `logcat_path` or null it.

## Scope

Single-line edit in `scripts/build_android_ui_state_pack.ps1` to add `-CaptureLogcat` to the smoke-script args list at line 596-606, paired with same-commit tracker updates in `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md` to reflect the landing. Validation via one-role state-pack run with the fix applied; confirm per-state `summary.json` now carries a real `logcat_path` pointing to a non-trivial file on disk. One commit.

- **Commit** — PowerShell code change + in-slice tracker doc updates + validation evidence referenced in commit body. No unit tests (PowerShell scripts have no test harness in this repo).

**NO Android code change. NO APK rebuild. NO pack regen. NO multi-role state-pack run.** Full matrix validation is a separate follow-up if planner wants broader coverage.

**New cadence rule — in-slice tracker updates.** Starting with this slice, each landing slice handles its own `CP9_ACTIVE_QUEUE.md` (Last-updated line, Post-RC Tracked strike, Completed log append) and `dispatch/README.md` (Landed-not-rotated append) updates in the same commit as the code change. Slice-file rotation to `notes/dispatch/completed/` stays batched via the D-series (rarer now — every 3-5 landings rather than 1:1). This replaces the pattern where D-series dispatches owned both tracker edits and rotation. See Step 4.5.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `6f9e07b` (R-ret1b Commit 2) OR a later commit touching only `notes/`, `CLAUDE.md`, `AGENTS.md`, doc-only markdown, or any file outside `scripts/build_android_ui_state_pack.ps1` + `scripts/run_android_instrumented_ui_smoke.ps1`. If either of those two scripts has been touched since `6f9e07b`, STOP and report.
2. Line anchors in `scripts/build_android_ui_state_pack.ps1`:
   - Line ~596-606: smoke-script args array construction with `$smokeScript`, `-Device`, `-SmokeProfile`, `-TestClass`, `-ArtifactRoot`, `-SummaryPath`, `-Orientation`, `-SkipBuild`. Drift of ±10 lines acceptable; drift beyond that STOP.
   - Line ~178-210: `Write-TrustedPackSummary` function. Verify the loop `foreach ($property in $Summary.PSObject.Properties) { $summaryData[$property.Name] = $property.Value }` exists at ~line 189-191 — that's what guarantees logcat_path propagation. If the loop is gone, scope widens (would need a per-field copy path adding logcat_path explicitly).
3. Line anchors in `scripts/run_android_instrumented_ui_smoke.ps1`:
   - Line 30: `[switch]$CaptureLogcat` param declaration.
   - Line 1266: `logcat_path = $(if ($CaptureLogcat) { Join-Path $artifactDir "logcat.txt" } else { $null })`.
   - If either anchor is missing, STOP.
4. Emulator available at `emulator-5556` (phone portrait — the default validation lane) with current APK and pack installed. Confirm via `adb devices` and a quick `adb -s emulator-5556 shell pm list packages com.senku.mobile` check. If 5556 isn't available, substitute 5554/5558/5560; record which serial was used in the report.
5. Repo venv active per AGENTS.md Quick Start. `pwsh.exe` or `powershell.exe` available for invoking the state-pack build script.
6. Pre-edit artifact probe: read a representative per-state `summary.json` from a recent state-pack artifact dir (e.g., `artifacts/cp9_stage2_rerun4_20260420_143440/ui_state_pack/20260420_143855/raw/20260420_143858_318/emulator-5554/summary.json`) and confirm `"logcat_path": null`. If the field is missing entirely or already populated, the codebase has drifted in ways this slice doesn't cover — STOP.

If any precondition fails, STOP and report before touching code.

## Outcome

- `scripts/build_android_ui_state_pack.ps1` line ~596-606 gains one entry (`"-CaptureLogcat"`) in the smoke args array. Placement between `"-SkipBuild"` and the `$SkipInstall` conditional block preserves existing style.
- Validation run on one role (phone_portrait, serial 5556) produces per-state `summary.json` files under the scoped output dir with:
  - `logcat_path` populated with an absolute path ending in `\logcat.txt` (HARD gate).
  - The pointed-to file exists on disk (HARD gate).
  - File size typically falls in 50 KB – 5 MB for emulator-level captures; report the distribution but do NOT fail on small sizes alone (advisory only — `-ClearLogcatBeforeRun` at `run_android_instrumented_ui_smoke.ps1:997-999` is NOT set by this slice, so buffer contents vary by how much ran before the snapshot).
- No regression in any existing per-state field (`status`, `artifact_expectations_met`, `pack_artifact_expectations_met`, screenshot/dump lists).
- Validation artifact dir committed as evidence (`artifacts/r_tool2_validation_<timestamp>/` or under `artifacts/tmp/` if keeping uncommitted). Planner preference: keep the validation run OUT of the commit (treat as temp evidence) and reference its path in the commit body. Artifacts bloat the repo; one text file in the commit body is sufficient record.
- `notes/CP9_ACTIVE_QUEUE.md` updated in-slice: Last-updated line refreshed, `State-pack logcat_path: null tooling gap` struck from Post-RC Tracked, new R-tool2 entry appended to Completed rolling log.
- `notes/dispatch/README.md` updated in-slice: `R-tool2_state_pack_logcat_capture.md` added to the "Landed (not yet rotated)" list; Post-RC tracked reference synced.
- Single commit touching three files: `scripts/build_android_ui_state_pack.ps1` (code), `notes/CP9_ACTIVE_QUEUE.md` (tracker), `notes/dispatch/README.md` (dispatch index).

## Boundaries (HARD GATE)

- Touch only:
  - `scripts/build_android_ui_state_pack.ps1` (the code change)
  - `notes/CP9_ACTIVE_QUEUE.md` (in-slice tracker update — see Step 4.5)
  - `notes/dispatch/README.md` (in-slice dispatch-index update — see Step 4.5)
- Do NOT:
  - Touch `scripts/run_android_instrumented_ui_smoke.ps1`. The CaptureLogcat switch already exists there; no change needed.
  - Touch `scripts/build_android_ui_state_pack_parallel.ps1` (the parent launcher). It invokes the child script as a whole; propagation happens automatically when the child starts passing `-CaptureLogcat` to its invocations.
  - Add a new `-CaptureLogcat` parameter to `build_android_ui_state_pack.ps1`. Just pass the flag unconditionally. Rationale: this slice closes a GAP that hit diagnosis twice; an opt-in switch preserves the gap for anyone who forgets to set it. Always-on is the load-bearing choice. If storage becomes a concern later, a separate hygiene slice can add an opt-out with `-SkipLogcat`.
  - Touch `Write-TrustedPackSummary` or any function in `build_android_ui_state_pack.ps1` beyond the single args-array entry.
  - Touch the detail_followup branch (line 511-594 in `build_android_ui_state_pack.ps1`). That branch invokes `run_android_detail_followup.ps1` (NOT `_logged`), which has no logcat capture. It's unreachable by any currently-wired state so leaving it alone does NOT leave a user-facing logcat gap. A future slice can add logcat there when/if that runner is wired into a state definition.
  - Rebuild the APK. The fix is PowerShell-only.
  - Regenerate the mobile pack.
  - Run the full four-role parallel state-pack build. One role (5556 phone_portrait) is sufficient for this slice's acceptance; full-matrix validation is a separate follow-up.
  - Commit the validation artifact dir unless planner agrees the size is trivial. Default: reference in commit body, keep out of the commit.
  - Rotate this slice's own file (`R-tool2_state_pack_logcat_capture.md`) to `notes/dispatch/completed/`. Rotation stays batched via the D-series; this slice just adds itself to the "Landed (not yet rotated)" list in `dispatch/README.md` per Step 4.5.
  - Touch any other tracker, handoff, diagnostic doc, or dispatch slice file beyond the two listed in Touch only. Per the new in-slice cadence rule, each slice owns only its own tracker footprint; planner-wide edits (architecture notes, other slice files, handoffs) stay out of scope.

One commit total. Message `R-tool2: capture per-state logcat in state-pack build`.

## The work

### Step 1 — Pre-edit probe

Read `scripts/build_android_ui_state_pack.ps1` lines 595-607. Expected state:

```powershell
            $args = @(
                "-ExecutionPolicy", "Bypass",
                "-File", $smokeScript,
                "-Device", $device,
                "-SmokeProfile", "custom",
                "-TestClass", ("com.senku.mobile.PromptHarnessSmokeTest#{0}" -f [string]$state.method),
                "-ArtifactRoot", $rawRootRelative,
                "-SummaryPath", $stateSummaryPath,
                "-Orientation", $orientation,
                "-SkipBuild"
            )
```

If that shape doesn't match (e.g., args array is flat and not the `@(...)` form, or `-SkipBuild` is absent or at a different position), STOP and report — drift breaks the edit pattern.

Also confirm via Grep that `run_android_instrumented_ui_smoke.ps1` has the `[switch]$CaptureLogcat` parameter at line 30. If renamed to something like `-EnableLogcat`, adjust the Step 2 edit accordingly.

### Step 2 — The edit

In `scripts/build_android_ui_state_pack.ps1`, modify the args array at line 596-606 to add `"-CaptureLogcat"` as a new entry between `"-SkipBuild"` and the closing `)`:

```powershell
            $args = @(
                "-ExecutionPolicy", "Bypass",
                "-File", $smokeScript,
                "-Device", $device,
                "-SmokeProfile", "custom",
                "-TestClass", ("com.senku.mobile.PromptHarnessSmokeTest#{0}" -f [string]$state.method),
                "-ArtifactRoot", $rawRootRelative,
                "-SummaryPath", $stateSummaryPath,
                "-Orientation", $orientation,
                "-SkipBuild",
                "-CaptureLogcat"
            )
```

No other changes. Do NOT modify the detail_followup runner args path (line 515-528). Do NOT add a parameter to `build_android_ui_state_pack.ps1` or its parallel launcher.

### Step 3 — Validation run

Pick one serial (prefer `emulator-5556` phone_portrait; fall back to any available emulator if 5556 isn't running). The state-pack build script supports `-RoleFilter` to limit to a single role.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/build_android_ui_state_pack.ps1 `
    -OutputRoot artifacts/tmp/r_tool2_validation `
    -RoleFilter phone_portrait `
    -SkipBuild `
    -SkipInstall `
    -SkipHostStates
```

`-SkipBuild` + `-SkipInstall` rely on the current APK/pack already being on the serial — true as of HEAD `6f9e07b`. `-SkipHostStates` avoids the host-inference probes which take 3-4x longer than local states and aren't needed for this logcat-capture validation. Expected runtime: 3-7 minutes for one role through ~10 common states.

Capture the script's stdout to a log file for diagnostic reference:
```powershell
pwsh ... 2>&1 | Tee-Object -FilePath artifacts/tmp/r_tool2_validation_run.log
```

If the build fails unrelated to the edit (emulator disconnect, missing pack), STOP and report. Do NOT retry more than once.

### Step 4 — Verify

For each of the per-state summary files under the scoped output:

```powershell
Get-ChildItem -Recurse -Filter "summary.json" -Path "artifacts/tmp/r_tool2_validation" |
    Where-Object { $_.FullName -match "phone_portrait" -or $_.FullName -match "emulator-5556" } |
    ForEach-Object {
        $s = Get-Content $_.FullName -Raw | ConvertFrom-Json
        [pscustomobject]@{
            method = $s.test_class
            logcat_path = $s.logcat_path
            logcat_exists = if ($s.logcat_path) { Test-Path $s.logcat_path } else { $false }
            logcat_size = if ($s.logcat_path -and (Test-Path $s.logcat_path)) { (Get-Item $s.logcat_path).Length } else { 0 }
        }
    }
```

(Adjust glob patterns for the actual output dir layout — `build_android_ui_state_pack.ps1` organizes per-state summaries under a structure you can discover via `Get-ChildItem -Recurse -Filter summary.json`.)

**Verify (hard gate — all must hold):**
- Every per-state summary under the scoped output dir has `logcat_path` populated (non-null, absolute path ending in `\logcat.txt`).
- Every pointed-to `logcat.txt` exists on disk.
- No per-state summary shows regressed state on `status` or `artifact_expectations_met` compared to pre-edit samples.

**Report (advisory, not a gate):**
- File-size distribution across the validation states. Typical emulator logcat snapshot runs 50 KB – 5 MB, but size varies by how much logging ran before the dump — small sizes alone do NOT fail the slice. A truly suspicious case would be "every state reports 0 bytes across the board"; in that case verify `Capture-LogcatSnapshot` actually invoked `adb logcat` and dig from there.

**STOP if:**
- Any per-state summary still shows `logcat_path: null` — the edit didn't take effect or the flag isn't propagating.
- Any pointed-to `logcat.txt` is missing entirely (file does not exist) — capture failed silently.
- Any per-state summary shows regressed state on `status` or `artifact_expectations_met` compared to pre-edit samples.

### Step 4.5 — In-slice tracker update

After Step 4's verification passes and BEFORE committing, make the following tracker edits. They land in the same commit as the code change.

**Edit A — `notes/CP9_ACTIVE_QUEUE.md`**

Three targeted edits:

1. **Last-updated line** (near top of file, currently set by D7 to reference R-ret1b landings). Replace with a current-date entry summarizing the R-tool2 landing. Name the commit by message rather than sha (sha doesn't exist until after commit; don't chase it):

   > Last updated: 2026-04-22 (or current date) — R-tool2 wired `-CaptureLogcat` into the state-pack build so per-state `summary.json` carries a real logcat path instead of null; closes the `state-pack logcat_path: null` tooling gap. First slice under the in-slice-tracker-update cadence.

2. **Post-RC Tracked section** — strike the `State-pack logcat_path: null tooling gap` row entirely (it's closed). Keep: pack-drift investigation, Wave C planning, `R-search` wrapper-hang observation, R-anchor-refactor1 (now forward-research-in-progress if that research is in flight). Add a new `R-anchor-refactor1 forward research (in flight)` row if it isn't already there.

3. **Completed rolling log** — append a new dated entry (bottom of section, chronological). Shape:

   > - 2026-04-22 (or current date) — R-tool2 landed. `scripts/build_android_ui_state_pack.ps1` +1/-0: added `"-CaptureLogcat"` to the smoke-script args array at line ~596-606, closing the state-pack logcat_path:null gap that hit diagnosis twice (R-host §8, R-search cross-cutting). Root cause was narrow — the smoke script already had full `Capture-LogcatSnapshot` machinery gated on a switch that the build script wasn't passing. `Write-TrustedPackSummary` preserves all source properties via a generic property-copy loop (line 178-210) so the populated logcat_path propagates automatically without trust-layer changes. Validation: single-role state-pack build on emulator-5556 phone_portrait via `-RoleFilter phone_portrait -SkipHostStates`; N per-state summaries all now carry real logcat_path + file-exists, size distribution advisory. Out-of-scope: detail_followup branch (`run_android_detail_followup.ps1`, no logcat capture) is unreachable by any currently-wired state so leaving it alone doesn't leave a user-facing gap. Spark scout caught the original detail_followup claim (initial slice draft named the wrong script name `_logged`); planner revised pre-dispatch. **First slice under the in-slice-tracker-update cadence — slice handles its own CP9_ACTIVE_QUEUE and dispatch/README updates in-commit; slice-file rotation stays batched via D-series.**

**Edit B — `notes/dispatch/README.md`**

Two targeted edits:

1. **Active slices section** — confirm R-tool2 is NOT listed as in-flight (it just landed). If it appears in the in-flight list from a pre-landing edit, remove.

2. **Landed (not yet rotated)** — replace the current "Nothing pending rotation as of D7" text with a one-line entry adding this slice:

   > Pending rotation as of R-tool2 landing: `R-tool2_state_pack_logcat_capture.md` (next D-slice will rotate). Retained live: `A1_retry_5560_landscape.md` (superseded-but-kept), `P5_scope_note_landscape_phone.md` (cancelled-but-kept), `probe_rain_shelter_mode_flip.md` (stale probe kept per D5), `D7_post_r_ret1b_tracker_reconciliation.md` (D7 bootstrap — waiting on next D-slice), `R-tool2_SCOUT_PROMPT.md` (scout-companion, rotate with R-tool2 slice file).

3. **Post-RC tracked reference** — sync with the CP9_ACTIVE_QUEUE changes (remove logcat_path row if it's referenced).

**Verify tracker edits:**

```bash
git diff notes/CP9_ACTIVE_QUEUE.md notes/dispatch/README.md
```

Confirm diff shows only the above three + two edits. No unrelated lines touched. If any other line shifted (spurious whitespace, line-ending normalization, etc.), revert and redo.

### Step 5 — Commit

```bash
git add scripts/build_android_ui_state_pack.ps1 notes/CP9_ACTIVE_QUEUE.md notes/dispatch/README.md
git commit -m "R-tool2: capture per-state logcat in state-pack build"
```

Commit message body:
```
Adds -CaptureLogcat to the run_android_instrumented_ui_smoke.ps1
invocation inside build_android_ui_state_pack.ps1, closing the
state-pack logcat_path: null tooling gap surfaced twice in diagnosis
(R-host Section 8, R-search cross-cutting).

Root cause was narrow: the smoke script at line 1266 writes
logcat_path = $null whenever $CaptureLogcat is false. The build
script was invoking it without the flag. The smoke script already
had full logcat capture machinery (Capture-LogcatSnapshot at
line 564-585); this commit just enables it for state-pack runs.

Write-TrustedPackSummary in build_android_ui_state_pack.ps1
(line 178-210) preserves all source properties via a generic
property-copy loop, so the populated logcat_path propagates to
trusted per-state summaries automatically — no trust-layer change
needed.

Validation: single-role state-pack build on emulator-5556
phone_portrait via `-RoleFilter phone_portrait -SkipHostStates`.
All <N> per-state summaries under the scoped output dir now carry
a real logcat_path pointing to a logcat.txt on disk (size
distribution <min>-<max> bytes; reported advisory, not a gate).
No regression in status or artifact_expectations_met.

Validation artifact: artifacts/tmp/r_tool2_validation_<timestamp>/
(not committed; see artifacts/tmp/r_tool2_validation_run.log for
the build-script stdout).

Anti-scope: detail_followup branch at line 511-594 invokes
run_android_detail_followup.ps1 which has no logcat capture, but
no currently-wired state exercises that branch — state defs at
lines 414-440 all use the default "instrumented" runner, so this
slice's smoke-path edit covers all live states. Build script parent
launcher (build_android_ui_state_pack_parallel.ps1) propagates
automatically through the child invocation.

In-slice tracker updates included per new cadence rule:
CP9_ACTIVE_QUEUE.md (Last-updated line, Post-RC Tracked strike,
Completed log append) and dispatch/README.md (Landed-not-rotated
append). Slice-file rotation to completed/ stays batched via the
D-series. See notes in Completed log entry for full context.
```

Stage three files: `scripts/build_android_ui_state_pack.ps1`, `notes/CP9_ACTIVE_QUEUE.md`, `notes/dispatch/README.md`. Verify via `git diff --cached --stat` that no other file is staged.

## Acceptance

- Single commit `R-tool2: capture per-state logcat in state-pack build` touching three files: `scripts/build_android_ui_state_pack.ps1` (1-line insertion), `notes/CP9_ACTIVE_QUEUE.md` (tracker update), `notes/dispatch/README.md` (dispatch-index update).
- Pre-edit probe confirmed at least one per-state summary shows `logcat_path: null`.
- Validation run succeeded on one role (phone_portrait via 5556 or fallback).
- All per-state summaries in the validation output have populated `logcat_path` and the pointed-to file exists on disk. File sizes reported advisory.
- No regression in any other per-state field.
- `git status` clean on all three scoped files post-commit.
- `CP9_ACTIVE_QUEUE.md` Post-RC Tracked section no longer contains the `State-pack logcat_path: null tooling gap` row.
- `CP9_ACTIVE_QUEUE.md` Completed rolling log has a new dated R-tool2 entry at the bottom.
- `dispatch/README.md` "Landed (not yet rotated)" section lists `R-tool2_state_pack_logcat_capture.md` as pending rotation.

## Delegation hints

- **Step 1 (probe)**: main-inline OR Spark read-only scout. Trivial.
- **Step 2 (edit)**: main-inline.
- **Step 3 (validation run)**: main-inline. Requires serial access; can't delegate.
- **Step 4 (verify)**: main-inline.
- **Step 4.5 (tracker update)**: main-inline OR `gpt-5.4 high` worker. Doc-only, tight surface. Worker delegation acceptable if main wants to run Step 5 in parallel with an independent task — but typically main-inline since Step 5 commits immediately after.
- **Step 5 (commit)**: main-inline.

**MCP hints:** none needed. No framework-API question in scope.

## Anti-recommendations

- Do NOT add a new `-CaptureLogcat` parameter to `build_android_ui_state_pack.ps1` with a conditional on whether to propagate. Always-on is the right default — it closes the gap permanently. Parametrizing just reintroduces the footgun.
- Do NOT touch `run_android_instrumented_ui_smoke.ps1`. It already has the switch; that's the whole point of the narrow fix.
- Do NOT rebuild the APK, regenerate the pack, or run full four-role state-pack. This slice is closing a tooling gap, not validating the pack.
- Do NOT commit the validation artifact dir unless it's under 100 KB total (typically it'll be 5-50 MB with screenshots/dumps/logcats). Reference path in commit body; keep out of the tree.
- Do NOT touch the detail_followup branch. It's unreachable by any currently-wired state (no state def at lines 414-440 sets `runner = "detail_followup"`), so adding logcat there is a dead edit in this slice. A future slice can address it when that runner is actually wired in.
- Do NOT promote the fix to the parallel launcher by editing `build_android_ui_state_pack_parallel.ps1`. The parallel launcher invokes the child via `powershell -File $buildScript ...`; the child's args propagation happens inside the child process. No launcher change required.
- Do NOT add unit tests for PowerShell — this repo has no Pester harness, and adding one for a one-line change is scope creep. The validation run IS the test.
- Do NOT widen scope to also capture video, netstat, dumpsys, or any other diagnostic stream. logcat closes the named gap; everything else is a separate conversation.
- Do NOT rotate the slice file itself in this commit. Rotation is batched via the D-series. Step 4.5 only ADDS the slice to the "Landed (not yet rotated)" list; it does not `git mv` anything.
- Do NOT backfill a commit sha into the tracker entries. Shas don't exist until after commit; the tracker references the commit by message title and date. If sha-level traceability is ever needed, `git log --all --grep="R-tool2:"` recovers it trivially.
- Do NOT expand the tracker edits beyond the three + two listed in Step 4.5. The in-slice cadence rule says each slice owns ONLY its own tracker footprint. Other drift (unrelated stale rows, last-updated-line inaccuracies from prior slices) stays out of scope — flag as out-of-scope observation, don't fix.

## Report format

Reply with:

- Commit sha + message.
- Files touched with `+X/-Y` counts (expected: `scripts/build_android_ui_state_pack.ps1 +1/-0`, `notes/CP9_ACTIVE_QUEUE.md ~+15/-2`, `notes/dispatch/README.md ~+4/-5`).
- Pre-edit probe: sample per-state summary's `logcat_path` value (should be null).
- Post-validation probe: table of per-state summaries with `(method, logcat_path, logcat_size)`. Include all scoped states; flag sizes as advisory.
- Validation output dir path + run log path.
- Serial used for validation.
- Tracker edit diffs summary: one line per file confirming the scoped edits landed (Last-updated line / Post-RC Tracked strike / Completed log append / Landed-not-rotated append).
- `git status --short` output post-commit.
- Any out-of-scope drift noticed — flag, don't fix.
- Delegation log (lane used per step).

## If anything fails

- **Step 2 edit applies but Step 3 run fails with a script error unrelated to the flag** (e.g., emulator disconnect, missing APK, gradle cache contention): re-probe the serial state with `adb devices` + `adb shell pm list packages`. If APK is missing, re-push via the standard provisioning flow. If emulator is disconnected, restart and retry once.
- **Step 4 shows `logcat_path: null` still**: the flag isn't propagating. Check the edit: is `"-CaptureLogcat"` a separate string entry (not concatenated with `-SkipBuild`)? Is it inside the `$args = @(...)` block? If yes, check whether the invoked smoke script has a newer signature — `Get-Content scripts/run_android_instrumented_ui_smoke.ps1 | Select-String "CaptureLogcat"` should show the param and usage. If the flag name has been renamed in the smoke script since this slice was drafted, update the edit to match.
- **Step 4 shows `logcat_path` populated but file is 0 bytes or missing**: file-missing IS a hard STOP (capture failed silently). 0-bytes is NOT a hard STOP in this slice — the size gate is advisory because `-ClearLogcatBeforeRun` isn't passed, so buffer contents depend on prior device activity. Report 0-byte counts; do not block. If EVERY state reports 0 bytes, verify `Capture-LogcatSnapshot` actually invoked `adb logcat` (check `run_android_instrumented_ui_smoke.ps1:564-585`). A follow-up slice could wire `-ClearLogcatBeforeRun` + pre-test device-logcat-clear for consistent capture windows.
- **If blocked at any step for more than 30 minutes, STOP and report.** The fix is narrow and doesn't need heroics.

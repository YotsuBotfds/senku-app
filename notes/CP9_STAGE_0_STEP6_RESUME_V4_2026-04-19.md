# CP9 Stage 0 Resume v4 — Emulator image FTS parity + targeted reruns

Date: 2026-04-19
Parent dispatch: `notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`
Prior resumes: `notes/CP9_STAGE_0_RESUME_2026-04-19.md`,
`notes/CP9_STAGE_0_STEP6_RESUME_2026-04-19.md`,
`notes/CP9_STAGE_0_STEP6_RESUME_V3_2026-04-19.md`
Prior artifact dir: `artifacts/cp9_stage0_20260419_142539/`

---

## Why this resume exists

Step 6 v3 (APK parity + targeted reruns) proved the APK-mismatch
hypothesis on `emulator-5558` but surfaced a new failure
downstream. After reinstalling the canonical APK:

- `emulator-5558` now logs
  `fts.unavailable support5=false support4=false schema5=true schema4=true`
  at pack open, retrieval degrades to LIKE-only
  (`lexicalHits=72` vs the passing `121`), `resolveAnswerMode`
  falls through to the generative path, and the host returns a
  `low_coverage_detected` answer with no escalation line.
- `emulator-5560` logs the same `fts.unavailable` line and never
  reaches `ask.start` in the bounded rerun window.

Meanwhile `emulator-5556` and `emulator-5554` continue to log
`fts.available table=lexical_chunks_fts4 runtime4=true` and pass
the Step 6 smoke. Same canonical APK on all four — see
`apk_parity/summary.md`.

The only remaining variable is the emulator **system image** —
the Android image under each AVD. The two portrait emulators
ship a system SQLite that supports FTS4 at runtime; the two
landscape emulators do not. The old 506 MB APK on `emulator-5558`
was evidently bundling its own SQLite with FTS built in, which
is why it logged `fts.available` on v2 despite sharing the same
emulator image as v3. The canonical 352 MB APK relies on system
SQLite, which exposes the image-level gap.

This is **not** a Wave B deployment gap. Wave B code is live and
correct on the two emulators where FTS works end-to-end
(`emulator-5556` and `emulator-5554`). It's an emulator-image
environment issue affecting the two landscape AVDs.

---

## Ground rules (unchanged)

- No code commits. No tracker commits. Artifact output only.
- No Wave B edits. No guide edits. No BACK-P-03 audit edits.
- Gated-step discipline: if any step fails, stop and log.
- Do not start Stage 1 in the same session.

Reuse the existing artifact directory:

```
STAGE0_DIR=artifacts/cp9_stage0_20260419_142539
```

Preserve `smoke_5556_v2/` and `smoke_5554_v2/` as the authoritative
Step 6 passes for those two serials.

---

## Preflight re-check

```
git status
git log --oneline -3
git diff --name-only
git diff --cached --name-only
adb devices
```

HEAD must still be `65252f7`. Both diff commands empty. All four
emulators listed. If any landscape emulator is not listed, restart
the matrix and note it in `$STAGE0_DIR/emulator_matrix_v4_preflight.txt`.

---

## Step 6d — Emulator image fingerprint

Goal: confirm that the landscape AVDs (`5560`, `5558`) are on a
different Android image than the portrait AVDs (`5556`, `5554`),
and specifically that their system SQLite lacks FTS4 runtime
support.

For each serial, capture build props, CPU ABI, and system
SQLite fingerprint:

```powershell
$serials = @("emulator-5556","emulator-5560","emulator-5554","emulator-5558")
New-Item -ItemType Directory -Force -Path "$STAGE0_DIR/image_parity" | Out-Null

foreach ($s in $serials) {
  $props = adb -s $s shell getprop |
    Select-String -Pattern 'ro\.(build\.version\.(release|sdk)|product\.(model|cpu\.abi|device|name)|hardware|system\.build\.(fingerprint|id))' |
    Select-Object -First 30
  $sqliteBins = adb -s $s shell "ls -l /system/lib*/libsqlite.so /apex/com.android.runtime/lib*/libsqlite.so 2>/dev/null"
  $sqliteSha  = adb -s $s shell "find /system /apex -name 'libsqlite.so' -exec sha256sum {} \; 2>/dev/null"
  @(
    "serial=$s",
    "--- getprop ---"
  ) + $props + @(
    "",
    "--- libsqlite.so paths ---",
    $sqliteBins,
    "",
    "--- libsqlite.so sha256 ---",
    $sqliteSha
  ) | Out-File "$STAGE0_DIR/image_parity/$s.txt" -Encoding UTF8
}
```

Summarize to `$STAGE0_DIR/image_parity/summary.md`:

```
| Serial | build.fingerprint | sdk | cpu.abi | libsqlite.so sha256 | FTS v2/v3 |
| ---- | ---- | ---- | ---- | ---- | ---- |
| emulator-5556 | ... | ... | ... | ... | available |
| emulator-5554 | ... | ... | ... | ... | available |
| emulator-5560 | ... | ... | ... | ... | unavailable |
| emulator-5558 | ... | ... | ... | ... | unavailable |
```

Also do a direct FTS runtime check via the app itself — just
dump SenkuPackRepo init lines per serial with a cold app start:

```powershell
foreach ($s in $serials) {
  adb -s $s shell "logcat -c"
  adb -s $s shell "am force-stop com.senku.mobile"
  adb -s $s shell "am start -n com.senku.mobile/.MainActivity"
  Start-Sleep -Seconds 5
  adb -s $s shell "logcat -d -s SenkuPackRepo:D" |
    Select-String -Pattern 'fts\.' |
    Out-File "$STAGE0_DIR/image_parity/${s}_fts_probe.txt" -Encoding UTF8
}
```

Acceptance for Step 6d:
- If the two passing serials show `libsqlite.so` sha256 different
  from the two failing serials, the emulator-image hypothesis is
  confirmed. Proceed to Step 6e.
- If all four show the same `libsqlite.so` sha256 but the
  `fts.*` log lines still differ, stop and log — this becomes
  planner work (deeper diagnosis would require debug logging,
  which violates the no-code-commits rule).

---

## Step 6e — Realign landscape AVDs onto the FTS-capable image

Goal: recreate `emulator-5560` and `emulator-5558` using the same
Android system image as `emulator-5556` / `emulator-5554`.

Inspect `scripts/start_senku_emulator_matrix.ps1` and identify
the AVD name and system image path used for each slot. The
portrait slots (5556, 5554) use an image whose SQLite supports
FTS4; use that same image for the landscape slots.

If the script already specifies identical AVDs, the difference
is elsewhere (base snapshot, user data). In that case:

```
adb -s emulator-5560 emu kill
adb -s emulator-5558 emu kill
# wait for shutdown
# in the avdmanager, wipe the two landscape AVDs:
emulator -avd <landscape_avd_name> -wipe-data -no-snapshot-load -netdelay none -netspeed full
```

Relaunch the matrix via `start_senku_emulator_matrix.ps1`. Wait
for boot-completed. Reinstall canonical APK on 5560 and 5558 and
re-push the pack (Step 5 of the parent dispatch).

Record the relaunch + reinstall evidence to
`$STAGE0_DIR/image_parity/landscape_realign.txt`.

Then verify FTS availability post-realign with the cold-start
probe from Step 6d, written to
`$STAGE0_DIR/image_parity/emulator-5560_fts_probe_post.txt` and
`$STAGE0_DIR/image_parity/emulator-5558_fts_probe_post.txt`.
Both must now log `fts.available` to proceed to 6f.

---

## Step 6f — Targeted smoke rerun on realigned emulators

Only rerun Step 6 smoke on emulators whose post-realign
`fts_probe_post.txt` shows `fts.available`. Do NOT rerun 5556 or
5554 — their v2 passes stand.

```
powershell -NoProfile -File scripts/run_android_prompt.ps1 -Serial <serial> -Query "He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?" -Output $STAGE0_DIR/smoke_<serial>_v4/
```

Acceptance (same as v2):
- Mode `uncertain_fit` or `abstain` — either accepted.
- `ui_dump.xml` MUST contain `If this is urgent or could be a
  safety risk, stop and call local emergency services now`
  above `Possibly relevant guides in the library:` (uncertain_fit)
  or `Closest matches in the library:` (abstain).

Write `$STAGE0_DIR/smoke_summary_v4.md` with the final matrix.
Keep v1/v2/v3 summaries intact.

---

## Step 6d / 6e fallback — if realignment is not possible

Two scenarios:

(A) Step 6d shows all four emulator images are identical (same
`libsqlite.so` sha) but FTS probe results differ. Stop and log.
Write `$STAGE0_DIR/image_parity/fallback.md` with the identical
sha and the mismatching probe output. This becomes planner work
— likely requires debug logging to trace why the same SQLite
binary yields different runtime FTS probe results on identical
images. NOT a Stage 0 executor task.

(B) Step 6e realignment is attempted but the landscape AVDs
cannot be made FTS-capable in session (e.g. no suitable system
image available locally, or wiping user data still yields
`fts.unavailable`). Stop and log.
Write `$STAGE0_DIR/image_parity/fallback.md` documenting:

- The four APKs are byte-identical (canonical sha).
- Two emulators (`5556`, `5554`) have `fts.available` and pass
  Step 6. Two emulators (`5560`, `5558`) have `fts.unavailable`
  and fail Step 6 because retrieval degrades to LIKE-only.
- The canonical APK has a soft dependency on system SQLite
  FTS4 that isn't bundled and isn't gracefully handled (LIKE
  fallback produces retrieval too weak to trigger
  uncertain_fit/abstain but not weak enough to short-circuit
  to a host-side abstain).

Do not declare Stage 0 green unilaterally. The question of
whether CP9 can proceed on a 2/4 matrix is a scope change that
requires planner approval — flag it in the report and stop.

---

## Step 7 — fallback query (unchanged from parent)

Only execute Step 7 if Step 6f lands green across the fixed
four-emulator matrix after realignment. Do not substitute the
three candidate queries. Write `$STAGE0_DIR/fallback_query.md`.

---

## Step 8 — updated summary

Append a "Resume v4" section to `$STAGE0_DIR/summary.md` that
supersedes the v3 Step 6 result. Structure:

```
## Resume v4 (Step 6 emulator image FTS parity)

Date: <ISO timestamp>
v3 post-reinstall regression root-caused to <emulator image FTS
gap / identical-image anomaly>. See
`notes/CP9_STAGE_0_STEP6_RESUME_V4_2026-04-19.md`.

### Image parity (Step 6d)

| Serial | fingerprint | libsqlite.so sha | FTS probe |
| ---- | ---- | ---- | ---- |
| emulator-5556 | ... | ... | available |
| emulator-5554 | ... | ... | available |
| emulator-5560 | ... | ... | ... |
| emulator-5558 | ... | ... | ... |

### Realignment (Step 6e)

Action taken: <wipe-data / new AVD / none>
Post-realign FTS probe: <per serial>

### Targeted smoke (Step 6f)

| Serial | Mode | Escalation | Status |
| ---- | ---- | ---- | ---- |
| emulator-5560 | ... | ... | pass/fail |
| emulator-5558 | ... | ... | pass/fail |

### Final Step 6 matrix

| Serial | Mode | Escalation | Status | Evidence |
| ---- | ---- | ---- | ---- | ---- |
| emulator-5556 | uncertain_fit | present | pass | smoke_5556_v2 |
| emulator-5554 | uncertain_fit | present | pass | smoke_5554_v2 |
| emulator-5560 | ... | ... | ... | smoke_5560_v4 |
| emulator-5558 | ... | ... | ... | smoke_5558_v4 |

## Stage 0 verdict: GREEN / RED / CONDITIONAL-RED
```

Use `CONDITIONAL-RED` only if Step 6e hit the Path B fallback
above (environment-level gap, not a Wave B gap).

---

## Step 9 — report

Report:
- Step 6d image fingerprint deltas and FTS probe results.
- Step 6e action taken and whether FTS came back up.
- Step 6f per-serial mode/escalation.
- Final Stage 0 verdict.
- Whether any scope-change question is pending planner input.

Stop after reporting. Do not start Stage 1.

---

## Carry-over notes for the planner (not executor work)

- If Step 6d confirms the image gap, add a CP9-adjacent task:
  document the required Android image / AVD spec for the matrix
  in `notes/ANDROID_INDEX.md` or a new
  `notes/EMULATOR_MATRIX_IMAGE_SPEC.md`, and update
  `start_senku_emulator_matrix.ps1` to pin all four slots to
  the FTS-capable image.
- Add a new lane-level task (probably BACK-P-05 or similar) for
  the canonical APK's soft dependency on system SQLite FTS4.
  Options: (a) bundle FTS4-enabled native SQLite, (b) improve
  the LIKE-fallback retrieval quality to preserve
  uncertain_fit/abstain triggering, (c) route LIKE-only
  retrieval through a dedicated safety-first mode. Planner
  decides which.
- BACK-U-04 (port mania markers from `query.py` to
  `OfflineAnswerEngine.java`) still stands post-CP9.
- Preserve the observation that the old 506 MB APK on
  `emulator-5558` v2 bundled FTS-capable SQLite natively. That's
  useful context for the BACK-P-05 decision.
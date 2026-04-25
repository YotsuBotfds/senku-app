# CP9 Stage 0 Resume v3 — Step 6 APK parity check + targeted reruns

Date: 2026-04-19
Parent dispatch: `notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`
Prior resume (v2): `notes/CP9_STAGE_0_STEP6_RESUME_2026-04-19.md`
Prior artifact dir: `artifacts/cp9_stage0_20260419_142539/`

---

## Why this resume exists

Step 6 corrected-query rerun (v2) landed partial:

| Emulator | Mode | Escalation | Status |
| ---- | ---- | ---- | ---- |
| emulator-5556 | `uncertain_fit` | present | pass |
| emulator-5554 | `uncertain_fit` | present | pass |
| emulator-5558 | `abstain` | **missing** | fail |
| emulator-5560 | unresolved | not verified | env-unstable |

The 5558 failure is not a query-marker issue — the corrected query
contains `normal rules do not apply`, which IS in
`SAFETY_CRITICAL_MENTAL_HEALTH_MARKERS`
(`OfflineAnswerEngine.java` line 48-63). `isSafetyCriticalQuery`
(HEAD line 1374) is a pure function of the query string only;
for this query it returns `true` deterministically on any device.
Both production call sites pass the computed `safetyCritical`
explicitly to the builders.

So the only plausible root cause for 5558 rendering abstain
without the escalation line is **APK provenance mismatch** — the
installed APK on 5558 was built from a source snapshot that
lacks U-02's escalation wiring. The fact that the 5558 UI dump
shows the U-03 confidence-label pill (`STRONG EVIDENCE`) means
U-03 IS present, but U-02 may not be. This matches the "If Step 6
still fails" branch of `CP9_STAGE_0_STEP6_RESUME_2026-04-19.md`.

This resume is a two-part diagnostic-then-rerun pass. No code
commits, no Wave B edits.

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

---

## Preflight re-check

```
git status
git log --oneline -3
git diff --name-only
git diff --cached --name-only
```

HEAD must still be `65252f7`. Both diff commands empty.

```
adb devices
```

All four of `emulator-5556`, `emulator-5560`, `emulator-5554`,
`emulator-5558` must be listed. If `emulator-5560` is still in
a bad state from the v2 run, restart the matrix:

```
powershell -NoProfile -File scripts/start_senku_emulator_matrix.ps1
```

Do NOT wipe user data unless you have to. If you do wipe,
re-run Step 5 (pack push) from the parent dispatch for the
affected emulator only.

---

## Step 6a — APK provenance parity

Goal: prove or disprove the APK-mismatch hypothesis for 5558.

For each of the four serials, capture the base.apk path, md5,
sha256, last-modified time, and `versionName`+`versionCode`:

```powershell
$serials = @("emulator-5556","emulator-5560","emulator-5554","emulator-5558")
New-Item -ItemType Directory -Force -Path "$STAGE0_DIR/apk_parity" | Out-Null

foreach ($s in $serials) {
  $pathLine = adb -s $s shell pm path com.senku.mobile | Select-Object -First 1
  $apkPath  = $pathLine -replace '^package:',''
  $md5      = (adb -s $s shell md5sum $apkPath).Trim()
  $sha256   = (adb -s $s shell sha256sum $apkPath).Trim()
  $stat     = (adb -s $s shell stat -c '%Y %s %n' $apkPath).Trim()
  $dump     = adb -s $s shell dumpsys package com.senku.mobile |
                Select-String -Pattern 'versionName=|versionCode=|firstInstallTime=|lastUpdateTime=|signatures=' |
                Select-Object -First 20
  @(
    "serial=$s",
    "apkPath=$apkPath",
    "md5=$md5",
    "sha256=$sha256",
    "stat(epoch,size,path)=$stat",
    "--- dumpsys ---"
  ) + $dump | Out-File "$STAGE0_DIR/apk_parity/$s.txt" -Encoding UTF8
}
```

Then summarize to `$STAGE0_DIR/apk_parity/summary.md`:

```
| Serial | sha256 | versionName | versionCode | lastUpdateTime |
| ---- | ---- | ---- | ---- | ---- |
| emulator-5556 | ... | ... | ... | ... |
| emulator-5560 | ... | ... | ... | ... |
| emulator-5554 | ... | ... | ... | ... |
| emulator-5558 | ... | ... | ... | ... |
```

Acceptance for Step 6a:
- All four sha256 values MUST match. If they match, the hypothesis
  is falsified and we have a deeper bug — stop and log (see
  "Step 6a fallback" below).
- If any sha256 differs from the majority, proceed to Step 6b.

---

## Step 6b — canonical APK install

Pick the canonical APK. Priority order:

1. A freshly built artifact from this repo:
   `android-app/app/build/outputs/apk/debug/app-debug.apk`
   (only if its sha256 matches the sha256 from a passing
   emulator — 5556 or 5554).
2. If no fresh artifact is available, pull the APK from a
   passing emulator:
   ```
   adb -s emulator-5556 pull <apkPath> "$STAGE0_DIR/apk_parity/canonical.apk"
   ```
   Recompute its sha256 and record to `apk_parity/canonical.txt`.

For every emulator whose sha256 DOES NOT match the canonical
apk:

```
adb -s <serial> install -r "$STAGE0_DIR/apk_parity/canonical.apk"
```

After install, re-hash on-device to confirm parity, and re-run
Step 5 (pack push) for that emulator only — reinstalling the APK
typically re-provisions app-private storage so the mobile pack
files need to be pushed again. Verify pack file sizes match the
ones recorded in `pack_push_5556.txt` (sqlite=105578496,
vectors=22708256, manifest=2286).

Record install + repush evidence to
`$STAGE0_DIR/apk_parity/<serial>_reinstall.txt`.

---

## Step 6c — targeted smoke rerun

Only rerun Step 6 smoke on emulators that were reinstalled in
6b, plus `emulator-5560` regardless (since its v2 run was never
clean). Do NOT rerun 5556 or 5554 if they passed v2 and their
APK was already canonical — the existing `smoke_<serial>_v2/`
evidence stands.

Harness invocation for each rerun target:

```
powershell -NoProfile -File scripts/run_android_prompt.ps1 -Serial <serial> -Query "He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?" -Output $STAGE0_DIR/smoke_<serial>_v3/
```

Per-target acceptance (same as v2):
- Mode `uncertain_fit` or `abstain` — either is accepted.
- `ui_dump.xml` MUST contain `If this is urgent or could be a
  safety risk, stop and call local emergency services now`
  above `Possibly relevant guides in the library:` (uncertain_fit)
  or `Closest matches in the library:` (abstain).

Write `$STAGE0_DIR/smoke_summary_v3.md` with the final matrix.
Keep v1 and v2 summaries intact.

---

## Step 6a fallback — if all four APKs already match

If Step 6a shows all four APKs are byte-identical, the APK-
mismatch hypothesis is wrong. Stop there. Do NOT attempt Step 6b.
Write `$STAGE0_DIR/apk_parity/fallback.md` with:

- The four identical sha256 values.
- Observation: 5558 renders `abstain` without the escalation
  line despite identical APK + identical mobile pack. Possible
  next causes (for planner, not executor):
  - `resolveAnswerMode` path difference driven by modeCandidates
    past the visible top-6 (candidates 7-16 might differ).
  - A per-device configuration in app-private storage that is
    not part of the pack (e.g., a cached PreparedAnswer or a
    feature flag).
  - A runtime race between abstain builder and UI render that
    drops the escalation line under landscape tablet layout.
- Raw evidence to attach: `smoke_5558_v2/logcat.txt`,
  `smoke_5558_v2/ui_dump.xml`, `smoke_5556_v2/logcat.txt`,
  `smoke_5556_v2/ui_dump.xml`, apk_parity/summary.md.

Then stop. This becomes planner work — it's NOT a Stage 0
executor task to dig further, because any deeper investigation
requires adding debug logging (a code commit) or re-running with
an instrumented build, both of which violate the no-code-commits
ground rule.

---

## Step 7 — fallback query (unchanged from parent)

Only execute Step 7 if Step 6c ends green across the fixed
four-emulator matrix. Use the three candidate queries from the
parent dispatch. Write to `$STAGE0_DIR/fallback_query.md`.

---

## Step 8 — updated summary

Append a "Resume v3" section to `$STAGE0_DIR/summary.md` that
supersedes the v2 Step 6 result. Structure:

```
## Resume v3 (Step 6 APK parity + targeted reruns)

Date: <ISO timestamp>
v2 Step 6 failures root-caused to <APK mismatch / unresolved>.
See `notes/CP9_STAGE_0_STEP6_RESUME_V3_2026-04-19.md`.

### APK parity (Step 6a)

| Serial | sha256 | match-canonical |
| ---- | ---- | ---- |
| emulator-5556 | ... | yes/no |
| emulator-5560 | ... | yes/no |
| emulator-5554 | ... | yes/no |
| emulator-5558 | ... | yes/no |

Canonical sha256: <value>

### Reinstall + repush (Step 6b)

Emulators reinstalled: <list>
Post-install sha256 match: <yes/no per>
Post-install pack sizes match (sqlite=105578496,
vectors=22708256, manifest=2286): <yes/no per>

### Targeted smoke reruns (Step 6c)

| Serial | Mode | Escalation | Status |
| ---- | ---- | ---- | ---- |
| emulator-5560 | ... | ... | pass/fail |
| <any reinstalled serial> | ... | ... | ... |

Final Step 6 matrix (merging v2 passes with v3 reruns):

| Serial | Mode | Escalation | Status | Evidence |
| ---- | ---- | ---- | ---- | ---- |
| emulator-5556 | uncertain_fit | present | pass | smoke_5556_v2 |
| emulator-5554 | uncertain_fit | present | pass | smoke_5554_v2 |
| emulator-5558 | ... | ... | ... | smoke_5558_v<2 or 3> |
| emulator-5560 | ... | ... | ... | smoke_5560_v3 |

## Stage 0 verdict: GREEN / RED
```

---

## Step 9 — report

Report (a) Step 6a APK parity result, (b) whether Step 6b ran
and what it changed, (c) per-emulator Step 6c mode/escalation,
(d) final Stage 0 verdict. Stop after reporting. Do not start
Stage 1.

## Carry-over notes for the planner (not executor work)

- BACK-U-04 still stands post-CP9: port mania/mental-health
  markers from `query.py` to `OfflineAnswerEngine.java`.
- If Step 6a shows the APK hypothesis is right, add a CP9-scope
  follow-up task: document a canonical APK install check in
  `TESTING_METHODOLOGY.md` so future matrix runs don't skew on a
  stale install.
- If Step 6a shows APKs match, a new task is required to allow
  targeted debug logging in a temporary branch — that's planner
  work, not Stage 0 executor work.
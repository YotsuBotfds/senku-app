# CP9 Stage 0 Resume v5 — Deploy current local debug APK, rerun Step 6 clean

Date: 2026-04-19
Parent dispatch: `notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`
Prior resumes: v1 (`notes/CP9_STAGE_0_RESUME_2026-04-19.md`),
v2 (`notes/CP9_STAGE_0_STEP6_RESUME_2026-04-19.md`),
v3 (`notes/CP9_STAGE_0_STEP6_RESUME_V3_2026-04-19.md`),
v4 (`notes/CP9_STAGE_0_STEP6_RESUME_V4_2026-04-19.md`)
Prior artifact dir: `artifacts/cp9_stage0_20260419_142539/`

---

## Why this resume exists

v3 made the matrix uniform on the 352 MB APK
(sha256 `82083fbc5d0d8ba04af89c06c21a4a61e22862544f8a48c9e5c878cf9351b809`)
by canonicalizing `emulator-5558` against the APK already
installed on the other three emulators. That was the wrong
"canonical." The current local debug build at
`android-app/app/build/outputs/apk/debug/app-debug.apk` is
792,303,169 bytes, sha256
`37152c74afb010496a473e4854f68caec44505e20e612496ef402c8a78cc9ca3`
— and is on **zero** of the four emulators today. See
`artifacts/cp9_stage0_20260419_142539/apk_parity/live_vs_local_debug_20260419.md`.

So the Step 6 smokes so far have been validating an older
emulator-resident build, not the build we're about to promote
in Stage 1. Wave B passes on the older 352 MB APK (5556/5554 v2)
are evidence that *some* version of Wave B is live somewhere —
but not evidence that the HEAD Wave B code is live on-device.
That's the gap Stage 0 is supposed to close.

The size delta (792 MB vs 352 MB) is consistent with the local
debug APK bundling the LiteRT model and FTS-capable native
SQLite. If that holds, the v4 landscape-FTS failure mode likely
goes away on its own once the real build is deployed. This
resume tests that hypothesis directly rather than speculating
about it.

This resume supersedes v4. Do **not** attempt the Step 6d/6e/6f
image-realignment work from v4 — it's moot if the current local
build brings its own SQLite and model. Keep v4 notes intact as
planner-lane context.

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

**Important:** treat prior v2/v3 smoke results as historical only.
They do not count toward Step 6 acceptance for v5. v5 needs a
clean four-emulator green against the current local debug APK.

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
of `emulator-5556`, `emulator-5560`, `emulator-5554`,
`emulator-5558` listed. If any are cold, restart via
`scripts/start_senku_emulator_matrix.ps1` and record boot state
to `$STAGE0_DIR/preflight_v5.txt`.

Hash the current local debug APK and confirm it matches the
value in `live_vs_local_debug_20260419.md`:

```powershell
$localApk = "android-app\app\build\outputs\apk\debug\app-debug.apk"
$localSha = (Get-FileHash -Algorithm SHA256 $localApk).Hash.ToLower()
$localSize = (Get-Item $localApk).Length
@(
  "path=$localApk",
  "sha256=$localSha",
  "size=$localSize",
  "expected_sha=37152c74afb010496a473e4854f68caec44505e20e612496ef402c8a78cc9ca3",
  "expected_size=792303169"
) | Out-File "$STAGE0_DIR/preflight_v5_local_apk.txt" -Encoding UTF8
```

If the hash differs from expected, stop and log — the local
build has changed since `live_vs_local_debug_20260419.md` was
written, and we need planner confirmation of which APK to deploy
before proceeding.

---

## Step 6g — Deploy current local debug APK to all four emulators

Goal: put the **current local build** on every emulator, with
fresh app data and a freshly pushed pack.

For each of the four serials, do a clean uninstall-then-install
(not `install -r`) so residual app state from the 352 MB older
APK is cleared:

```powershell
$serials = @("emulator-5556","emulator-5560","emulator-5554","emulator-5558")
$localApk = "android-app\app\build\outputs\apk\debug\app-debug.apk"
New-Item -ItemType Directory -Force -Path "$STAGE0_DIR/apk_deploy_v5" | Out-Null

foreach ($s in $serials) {
  $log = @()
  $log += "serial=$s"
  $log += "--- pre-uninstall pm path ---"
  $log += (adb -s $s shell pm path com.senku.mobile)
  $log += "--- uninstall ---"
  $log += (adb -s $s uninstall com.senku.mobile)
  $log += "--- install ---"
  $log += (adb -s $s install $localApk)
  $log += "--- post-install pm path ---"
  $postPath = (adb -s $s shell pm path com.senku.mobile | Select-Object -First 1) -replace '^package:',''
  $log += $postPath
  $log += "--- post-install apk sha256 ---"
  $log += (adb -s $s shell sha256sum $postPath)
  $log += "--- post-install apk size ---"
  $log += (adb -s $s shell "ls -l $postPath")
  $log | Out-File "$STAGE0_DIR/apk_deploy_v5/$s.txt" -Encoding UTF8
}
```

Acceptance:
- Every serial's post-install sha256 MUST equal the local APK
  sha256 recorded in preflight.
- Every serial's post-install size MUST equal 792,303,169.

If any serial mismatches, stop and log — do not push the pack to
a wrong-APK device.

Summarize to `$STAGE0_DIR/apk_deploy_v5/summary.md`.

---

## Step 6h — Push mobile pack to all four emulators

Reuse the pack at
`artifacts/mobile_pack/senku_20260419_cp9_stage0/` (Step 3
export). Pack hashes and file sizes are recorded in
`$STAGE0_DIR/mobile_pack_hashes.txt` — per prior runs
sqlite=105,578,496, vectors=22,708,256, manifest=2,286.

For each serial:

```powershell
foreach ($s in $serials) {
  powershell -NoProfile -File scripts/push_mobile_pack_to_android.ps1 `
    -Device $s `
    -PackDir "artifacts/mobile_pack/senku_20260419_cp9_stage0" `
    2>&1 | Tee-Object "$STAGE0_DIR/pack_push_v5_$s.txt"
}
```

Acceptance: each `pack_push_v5_<serial>.txt` must show the three
expected file sizes on-device. If any serial's push fails or
reports a different size, stop and log.

---

## Step 6i — Cold-start FTS probe on all four

Cold-start the app once per serial so SenkuPackRepo runs its FTS
detection against the freshly deployed APK + pack:

```powershell
foreach ($s in $serials) {
  adb -s $s shell logcat -c
  adb -s $s shell am force-stop com.senku.mobile
  adb -s $s shell am start -n com.senku.mobile/.MainActivity
  Start-Sleep -Seconds 6
  adb -s $s shell logcat -d -s SenkuPackRepo:D |
    Select-String -Pattern 'fts\.' |
    Out-File "$STAGE0_DIR/apk_deploy_v5/${s}_fts_probe.txt" -Encoding UTF8
}
```

Record which emulators log `fts.available` vs `fts.unavailable`
in `$STAGE0_DIR/apk_deploy_v5/fts_summary.md`.

This is informational for this step — not a gate. Step 6j is the
gate.

---

## Step 6j — Four-emulator Wave B smoke

For each serial, run the corrected query via the instrumentation
harness:

```powershell
foreach ($s in $serials) {
  powershell -NoProfile -File scripts/run_android_prompt.ps1 `
    -Serial $s `
    -Query "He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?" `
    -Output "$STAGE0_DIR/smoke_${s}_v5/"
}
```

Per-serial acceptance (unchanged from v2):
- Mode `uncertain_fit` or `abstain` — either accepted.
- `ui_dump.xml` MUST contain the escalation sentence
  `If this is urgent or could be a safety risk, stop and call
  local emergency services now` above either `Possibly relevant
  guides in the library:` (uncertain_fit) or `Closest matches in
  the library:` (abstain).

Write `$STAGE0_DIR/smoke_summary_v5.md` with the full matrix and
explicit pass/fail per serial. Keep v1/v2/v3 summaries intact.

Step 6 acceptance: escalation line present on all four emulators.

---

## If Step 6j fails

Classify the failure before deciding next move. Do NOT leap to
another resume dispatch — write the evidence and stop.

Write `$STAGE0_DIR/apk_deploy_v5/failure_triage.md` capturing,
for each failing serial:

- FTS probe result from Step 6i (`fts.available` vs
  `fts.unavailable`).
- `logcat.txt` mode line: `ask.uncertain_fit`, `ask.abstain`,
  `ask.generate`, or none of the above.
- `ui_dump.xml` mode pill and body text.
- `search.topResults` and `search ... lexicalHits=N vectorHits=M`
  retrieval counts.
- Whether `host.request` / `host.response` appears (i.e. the
  generative path was taken).

Three most likely failure patterns and what each points at:

(1) `fts.unavailable` still shows on some serials. That means
even the 792 MB local APK does not bundle FTS-capable SQLite,
and the landscape emulator images really do lack system FTS4.
This is genuinely BACK-P-05 territory — surface it as planner
work, don't patch.

(2) `fts.available` on all four but escalation missing on some
serial that shows `ask.abstain` or `ask.uncertain_fit`. This
would be a real Wave B deployment gap in the current HEAD build
— unexpected, given the HEAD source clearly wires it. Capture
evidence and hand to planner.

(3) `fts.available` on all four, mode resolves to
`ask.generate`/host, no deterministic card. This means retrieval
is stronger than expected and does not trigger either gate. Try
a stronger mania-marker-dense query as a probe (e.g. one that
combines `hearing voices`, `normal rules do not apply`, and
`paranoid`) — but do NOT substitute that for Step 6 acceptance
without planner approval. Record the probe result to
`apk_deploy_v5/strong_query_probe.md`.

---

## Step 7 — fallback query (unchanged from parent)

Only execute Step 7 if Step 6j lands green on all four serials.
Use the three candidate queries from the parent dispatch exactly.
Write `$STAGE0_DIR/fallback_query.md`.

---

## Step 8 — updated summary

Append a "Resume v5" section to `$STAGE0_DIR/summary.md` that
supersedes the v3/v4 state. Structure:

```
## Resume v5 (current local debug APK, clean matrix rerun)

Date: <ISO timestamp>
Prior v3 canonicalization landed on an older 352 MB
emulator-resident build, not the current 792 MB HEAD debug
build. v5 deploys the current build and reruns Step 6 clean.
See `notes/CP9_STAGE_0_STEP6_RESUME_V5_2026-04-19.md`.

### APK deployment (Step 6g)

| Serial | Post-install sha256 match | Size match |
| ---- | ---- | ---- |
| emulator-5556 | yes/no | yes/no |
| emulator-5560 | yes/no | yes/no |
| emulator-5554 | yes/no | yes/no |
| emulator-5558 | yes/no | yes/no |

Target sha256:
37152c74afb010496a473e4854f68caec44505e20e612496ef402c8a78cc9ca3
Target size: 792,303,169

### Pack push (Step 6h)

Per-serial on-device sizes match (sqlite=105,578,496,
vectors=22,708,256, manifest=2,286): <yes/no per>

### FTS probes (Step 6i)

| Serial | FTS state |
| ---- | ---- |
| emulator-5556 | available/unavailable |
| emulator-5560 | available/unavailable |
| emulator-5554 | available/unavailable |
| emulator-5558 | available/unavailable |

### Step 6 smoke (Step 6j)

| Serial | Mode | Escalation | Status | Evidence |
| ---- | ---- | ---- | ---- | ---- |
| emulator-5556 | ... | ... | pass/fail | smoke_5556_v5 |
| emulator-5560 | ... | ... | pass/fail | smoke_5560_v5 |
| emulator-5554 | ... | ... | pass/fail | smoke_5554_v5 |
| emulator-5558 | ... | ... | pass/fail | smoke_5558_v5 |

## Stage 0 verdict: GREEN / RED
```

---

## Step 9 — report

Report:
- Local APK hash preflight outcome.
- Per-serial deploy + pack-push outcome (Steps 6g/6h).
- Per-serial FTS probe (Step 6i) — note this is informational.
- Per-serial Step 6j mode/escalation and final Stage 0 verdict.
- If any failure, the filed failure triage classification
  (patterns 1/2/3 above) without speculating beyond evidence.

Stop after reporting. Do not start Stage 1.

---

## Carry-over notes for the planner (not executor work)

- The 352 MB "canonical" APK lineage is now a known pitfall.
  Add a note to `TESTING_METHODOLOGY.md` that Android matrix
  validation must hash the **local build artifact** against the
  on-device APK before treating a matrix as verified. Do this
  post-Stage 0.
- If Step 6j comes back green, the v4 landscape-FTS concern is
  likely moot (the real build bundles what it needs). Close the
  v4 speculation and carry only BACK-U-04 forward. If Step 6j
  reproduces the FTS gap, BACK-P-05 (canonical APK's SQLite FTS
  dependency) becomes real post-Stage-0 work.
- Either way, document the expected APK size range (≈ 800 MB
  with model + natives bundled) so future Wave X deployments
  don't silently ship a slimmed-down build that breaks
  retrieval.

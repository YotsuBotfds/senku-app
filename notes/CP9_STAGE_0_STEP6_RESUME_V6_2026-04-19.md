# CP9 Stage 0 Resume v6 — Push LiteRT model, rerun Step 6 clean

Date: 2026-04-19
Parent dispatch: `notes/CP9_STAGE_0_DISPATCH_2026-04-19.md`
Prior resumes: v1 (`notes/CP9_STAGE_0_RESUME_2026-04-19.md`),
v2 (`notes/CP9_STAGE_0_STEP6_RESUME_2026-04-19.md`),
v3 (`notes/CP9_STAGE_0_STEP6_RESUME_V3_2026-04-19.md`),
v4 (`notes/CP9_STAGE_0_STEP6_RESUME_V4_2026-04-19.md`),
v5 (`notes/CP9_STAGE_0_STEP6_RESUME_V5_2026-04-19.md`)
Prior artifact dir: `artifacts/cp9_stage0_20260419_142539/`

---

## Why this resume exists

v5 correctly canonicalized all four emulators onto the current
local debug APK
(sha256 `37152c74afb010496a473e4854f68caec44505e20e612496ef402c8a78cc9ca3`,
size 792,303,169). The preflight was clean, the pack push was
clean, and the cold-start FTS probe flipped all four emulators to
`fts.available table=lexical_chunks_fts4 ... runtime4=true`.

Step 6j still failed RED on the first serial (`emulator-5556`)
with a new signature:

- `summary.json`: `status=fail`,
  `failure_reason=Instrumentation run failed`.
- `instrumentation.txt` / `logcat.txt` end with
  `java.lang.AssertionError: detail body should be present` at
  `PromptHarnessSmokeTest.assertDetailSettled(PromptHarnessSmokeTest.java:1908)`
  called from `scriptedPromptFlowCompletes(PromptHarnessSmokeTest.java:1843)`.
- `ui_dump.xml` shows the query still in
  `com.senku.mobile:id/search_input` with the in-body
  `Ask Manual` button still visible, and `results_list` is a plain
  alphabetical guide browse (Abrasives Manufacturing / Accessible
  Shelter / Acetylene) — i.e. the list that `installPack()`
  populates before `maybeHandleAutomation()` runs, not an
  answer-pipeline result set.
- `logcat.txt` contains exactly one `fts.available` line and then
  three minutes of silence. There is no `ask.start`, no
  `search.topResults`, no `lexicalHits=`, no `host.request`, no
  `host.response`.

### Root cause

`MainActivity.runAsk(String query)` has a pre-engine gate:

```java
File modelFile = ModelFileStore.getImportedModelFile(this);
HostInferenceConfig.Settings inferenceSettings =
    HostInferenceConfig.resolve(this);
if (!inferenceSettings.enabled && modelFile == null) {
    askLaneActive = false;
    setBusy("Import a .litertlm or .task model first", false);
    if (!hasAutoQuery(getIntent())) {
        showBrowseChrome(true);
    }
    updateInfoText();
    return;
}
```

`ModelFileStore.getImportedModelFile(Context)` reads the
`senku_model_store` SharedPreferences path, then falls back to
scanning `/data/user/0/com.senku.mobile/files/models/` and the
external-files `models/` directory for an E4B/E2B
`.litertlm`/`.task` file.

The v5 Step 6g `uninstall` + `install` pair wiped the entire
`/data/user/0/com.senku.mobile/` tree on every serial, including
`files/models/` and `shared_prefs/senku_model_store.xml`. The
792 MB HEAD debug APK does not ship a LiteRT model in
`assets/` — its `assets/` tree is `mobile_pack/` only. So after
v5's clean install, every emulator had:

- No model in `files/models/`.
- No SharedPreferences pointer.
- No external `models/` file.
- Host inference disabled (v5 smoke explicitly passes
  `scripted_allow_host_fallback: false` and does not set
  `hostInferenceEnabled`).

`runAsk` hits the early return, prints
`"Import a .litertlm or .task model first"` into the status
strip, and never invokes `OfflineAnswerEngine.prepare`. The
harness then waits `DETAIL_WAIT_MS` for a detail body that will
never render, `captureUiState("prompt_detail_failure")` fires,
and `assertDetailSettled` throws
`"detail body should be present"`.

### Why v2 5556/5554 passed on the older 352 MB APK

The 352 MB emulator-resident APK and the 792 MB HEAD debug APK
share the same `runAsk` gate — `git log --oneline` on
`MainActivity.java` shows only Wave B changes, not a new gate.
v2's partial pass rode on stateful emulators: those serials had
a model in `/data/user/0/com.senku.mobile/files/models/` from an
earlier manual push or a prior test session. v5's clean
uninstall wiped that residual state, which is why the new
failure pattern only appeared once every emulator was truly
clean.

This means v2/v3 "passes" were an accidental validation of
Wave B against the older build *plus* leftover model state.
Stage 0 acceptance still hasn't been earned against the HEAD
build.

### What v6 changes

v6 keeps the v5 posture (current local debug APK, clean
uninstall/install, fresh pack push, identical smoke query) and
adds one step: **push a LiteRT model onto every emulator after
install and before the smoke**, using the existing
`scripts/push_litert_model_to_android.ps1` helper. The repo
already ships both candidate models:

- `models/gemma-4-E4B-it.litertlm`
- `models/gemma-4-E2B-it.litertlm`

The push script auto-resolves to E4B if present and writes both
the model file and the SharedPreferences pointer, which is
exactly what `ModelFileStore.getImportedModelFile` looks for.

v6 does **not** touch any code, harness, or Wave B surface.
No tracker edits. No BACK-P-03 work. Artifact output only.

This resume supersedes v5. Keep v2/v3/v4/v5 notes intact for
planner-lane context. Do not retry the v5 dispatch as-is.

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

Treat prior v2/v3/v5 smoke results as historical only. They do
not count toward Step 6 acceptance for v6. v6 needs a clean
four-emulator green against the current local debug APK *with*
a resident LiteRT model.

---

## Preflight re-check

```
git status
git log --oneline -3
git diff --name-only
git diff --cached --name-only
adb devices
```

HEAD unchanged from v5. Both diff commands empty. All four of
`emulator-5556`, `emulator-5560`, `emulator-5554`,
`emulator-5558` listed. If any are cold, restart via
`scripts/start_senku_emulator_matrix.ps1` and record boot state
to `$STAGE0_DIR/preflight_v6.txt`.

Re-hash the current local debug APK and confirm it still matches
the v5 value:

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
) | Out-File "$STAGE0_DIR/preflight_v6_local_apk.txt" -Encoding UTF8
```

Also hash the LiteRT model that will be pushed (prefer E4B if
present, else E2B):

```powershell
$modelCandidates = @(
  "models\gemma-4-E4B-it.litertlm",
  "models\gemma-4-E2B-it.litertlm"
)
$resolvedModel = $null
foreach ($c in $modelCandidates) {
  if (Test-Path $c) { $resolvedModel = $c; break }
}
if (-not $resolvedModel) {
  throw "No LiteRT model available at models\gemma-4-E4B-it.litertlm or models\gemma-4-E2B-it.litertlm"
}
$modelSha = (Get-FileHash -Algorithm SHA256 $resolvedModel).Hash.ToLower()
$modelSize = (Get-Item $resolvedModel).Length
@(
  "path=$resolvedModel",
  "sha256=$modelSha",
  "size=$modelSize"
) | Out-File "$STAGE0_DIR/preflight_v6_local_model.txt" -Encoding UTF8
```

If the local APK hash differs from expected, stop and log — the
local build changed since v5 was written, and we need planner
confirmation before proceeding. If no model is available, stop
and log; this step requires a concrete artifact on disk, and the
planner needs to resolve where to source it.

---

## Step 6g — Deploy current local debug APK to all four emulators

Identical to v5 Step 6g. Clean uninstall + install, verify
post-install sha256 and size per serial.

```powershell
$serials = @("emulator-5556","emulator-5560","emulator-5554","emulator-5558")
$localApk = "android-app\app\build\outputs\apk\debug\app-debug.apk"
New-Item -ItemType Directory -Force -Path "$STAGE0_DIR/apk_deploy_v6" | Out-Null

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
  $log | Out-File "$STAGE0_DIR/apk_deploy_v6/$s.txt" -Encoding UTF8
}
```

Acceptance: every serial's post-install sha256 must equal
`37152c74afb010496a473e4854f68caec44505e20e612496ef402c8a78cc9ca3`
and size must equal `792303169`. Summarize to
`$STAGE0_DIR/apk_deploy_v6/summary.md`.

---

## Step 6g.5 — Push LiteRT model to all four emulators (NEW)

Goal: place the E4B (or E2B) LiteRT model and write the
`senku_model_store` SharedPreferences pointer into each freshly
installed app's data dir. This is the step v5 was missing.

For each serial, run:

```powershell
foreach ($s in $serials) {
  powershell -NoProfile -File scripts/push_litert_model_to_android.ps1 `
    -Device $s `
    -ForceStop `
    2>&1 | Tee-Object "$STAGE0_DIR/apk_deploy_v6/model_push_${s}.txt"
}
```

The helper auto-resolves to `models\gemma-4-E4B-it.litertlm` if
present (else E2B), copies it to
`/data/user/0/com.senku.mobile/files/models/`, and writes
`shared_prefs/senku_model_store.xml` with the on-device path.
`-ForceStop` ensures the next cold-start re-reads the new
SharedPreferences state.

Immediately after the helper completes for each serial, capture
on-device verification:

```powershell
foreach ($s in $serials) {
  $v = @()
  $v += "serial=$s"
  $v += "--- files/models listing ---"
  $v += (adb -s $s shell run-as com.senku.mobile ls -l files/models 2>&1)
  $v += "--- senku_model_store.xml ---"
  $v += (adb -s $s shell run-as com.senku.mobile cat shared_prefs/senku_model_store.xml 2>&1)
  $v += "--- files/models sha256 ---"
  $modelName = ((adb -s $s shell run-as com.senku.mobile ls files/models) -split "`n" | Where-Object { $_.Trim() -match '\.(litertlm|task)$' } | Select-Object -First 1).Trim()
  if ($modelName) {
    $v += "model=$modelName"
    $v += (adb -s $s shell run-as com.senku.mobile sha256sum "files/models/$modelName" 2>&1)
  } else {
    $v += "model=<missing>"
  }
  $v | Out-File "$STAGE0_DIR/apk_deploy_v6/model_verify_${s}.txt" -Encoding UTF8
}
```

Acceptance per serial:

- `files/models` listing contains a `.litertlm` or `.task` file.
- `senku_model_store.xml` contains a `<string name="model_path">`
  whose value matches `files/models/<file>` (full absolute path
  `/data/user/0/com.senku.mobile/files/models/<file>`).
- On-device sha256 of the copied model equals the preflight
  `modelSha` from `preflight_v6_local_model.txt`.

If any serial fails any acceptance check, stop and log — do not
cold-start or run the smoke against a partially configured device.

Summarize to `$STAGE0_DIR/apk_deploy_v6/model_push_summary.md`
with a table:

| Serial | Model file present | Prefs wired | sha256 match |
| ---- | ---- | ---- | ---- |
| emulator-5556 | yes/no | yes/no | yes/no |
| emulator-5560 | yes/no | yes/no | yes/no |
| emulator-5554 | yes/no | yes/no | yes/no |
| emulator-5558 | yes/no | yes/no | yes/no |

---

## Step 6h — Push mobile pack to all four emulators

Identical to v5 Step 6h. Reuse the pack at
`artifacts/mobile_pack/senku_20260419_cp9_stage0/`. Expected
sizes sqlite=105,578,496, vectors=22,708,256, manifest=2,286.

```powershell
foreach ($s in $serials) {
  powershell -NoProfile -File scripts/push_mobile_pack_to_android.ps1 `
    -Device $s `
    -PackDir "artifacts/mobile_pack/senku_20260419_cp9_stage0" `
    2>&1 | Tee-Object "$STAGE0_DIR/pack_push_v6_$s.txt"
}
```

Acceptance: each `pack_push_v6_<serial>.txt` must show the three
expected file sizes on-device. If any serial's push fails or
reports a different size, stop and log.

---

## Step 6i — Cold-start FTS probe + model-readiness probe

Cold-start each app once so SenkuPackRepo runs FTS detection and
the UI settles. Capture both the FTS line and any
`runAsk`/`OfflineAnswerEngine` activity so we can prove the
model gate is cleared before Step 6j:

```powershell
foreach ($s in $serials) {
  adb -s $s shell logcat -c
  adb -s $s shell am force-stop com.senku.mobile
  adb -s $s shell am start -n com.senku.mobile/.MainActivity
  Start-Sleep -Seconds 8
  $probeOut = adb -s $s shell logcat -d -s SenkuPackRepo:D OfflineAnswerEngine:D MainActivity:D 2>&1
  $probeOut | Out-File "$STAGE0_DIR/apk_deploy_v6/${s}_cold_start_probe.txt" -Encoding UTF8
  ($probeOut | Select-String -Pattern 'fts\.') |
    Out-File "$STAGE0_DIR/apk_deploy_v6/${s}_fts_probe.txt" -Encoding UTF8
}
```

Record which emulators log `fts.available` vs `fts.unavailable`
in `$STAGE0_DIR/apk_deploy_v6/fts_summary.md`. Also capture
whether the `MainActivity` status strip shows the
`Import a .litertlm or .task model first` message in
`cold_start_probe.txt` — it MUST NOT appear. If it does, the
model push in Step 6g.5 didn't take on that serial and the
smoke will fail the same way as v5.

This step is informational for the FTS side; the
model-readiness check (no "Import a .litertlm" message) is a
hard gate. If any serial shows the import-model message, stop
and log.

---

## Step 6j — Four-emulator Wave B smoke

Identical to v5 Step 6j. Same scripted query, same acceptance.

```powershell
foreach ($s in $serials) {
  powershell -NoProfile -File scripts/run_android_prompt.ps1 `
    -Serial $s `
    -Query "He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?" `
    -Output "$STAGE0_DIR/smoke_${s}_v6/"
}
```

Per-serial acceptance (unchanged from v2):

- Mode `uncertain_fit` or `abstain` — either accepted.
- `ui_dump.xml` MUST contain the escalation sentence
  `If this is urgent or could be a safety risk, stop and call
  local emergency services now` above either
  `Possibly relevant guides in the library:` (uncertain_fit) or
  `Closest matches in the library:` (abstain).

Write `$STAGE0_DIR/smoke_summary_v6.md` with the full matrix
and explicit pass/fail per serial. Keep v1/v2/v3/v5 summaries
intact.

Step 6 acceptance: escalation line present on all four emulators.

---

## If Step 6j fails

Classify before deciding next move. Do NOT leap to another
resume dispatch — write the evidence and stop.

Write `$STAGE0_DIR/apk_deploy_v6/failure_triage.md` capturing,
for each failing serial:

- FTS probe result (`fts.available` vs `fts.unavailable`).
- Cold-start probe result (model-readiness: did the app show
  "Import a .litertlm or .task model first"?).
- `logcat.txt` mode line: `ask.uncertain_fit`, `ask.abstain`,
  `ask.generate`, or none of the above.
- `ui_dump.xml` mode pill and body text.
- `search.topResults` and `search ... lexicalHits=N vectorHits=M`
  retrieval counts.
- Whether `host.request` / `host.response` appears.

Failure patterns now carry a fourth option:

(1) `fts.unavailable` still shows on some serials — BACK-P-05
territory, surface as planner work, don't patch.

(2) `fts.available` on all four but escalation missing on some
serial that shows `ask.abstain` or `ask.uncertain_fit`. Real
Wave B deployment gap — capture evidence, hand to planner.

(3) `fts.available` on all four, mode resolves to
`ask.generate`/host, no deterministic card. Retrieval too
strong to trigger the gate — record and hand to planner for a
stronger-query decision.

(4) Harness never reaches `ask.start` (the v5 signature) on some
serial even after Step 6g.5. That means the model push didn't
stick or the gate is reading something else. Include the
`cold_start_probe.txt`, `model_push_<serial>.txt`, and
`model_verify_<serial>.txt` for that serial and stop — this is
an infra gap to diagnose before the next resume.

---

## Step 7 — fallback query (unchanged from parent)

Only execute Step 7 if Step 6j lands green on all four serials.
Use the three candidate queries from the parent dispatch exactly.
Write `$STAGE0_DIR/fallback_query.md`.

---

## Step 8 — updated summary

Append a "Resume v6" section to `$STAGE0_DIR/summary.md` that
supersedes the v5 state. Structure:

```
## Resume v6 (current local debug APK + pushed LiteRT model, clean matrix rerun)

Date: <ISO timestamp>
v5 deployed the correct APK but left every emulator without a
LiteRT model, so runAsk early-returned before the answer engine
ran. v6 adds a model-push step and reruns Step 6 clean.
See `notes/CP9_STAGE_0_STEP6_RESUME_V6_2026-04-19.md`.

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

### Model push (Step 6g.5)

Model used: <gemma-4-E4B-it.litertlm | gemma-4-E2B-it.litertlm>
Model sha256: <from preflight_v6_local_model.txt>

| Serial | Model file present | Prefs wired | sha256 match |
| ---- | ---- | ---- | ---- |
| emulator-5556 | yes/no | yes/no | yes/no |
| emulator-5560 | yes/no | yes/no | yes/no |
| emulator-5554 | yes/no | yes/no | yes/no |
| emulator-5558 | yes/no | yes/no | yes/no |

### Pack push (Step 6h)

Per-serial on-device sizes match (sqlite=105,578,496,
vectors=22,708,256, manifest=2,286): <yes/no per>

### Cold-start probe (Step 6i)

| Serial | FTS state | Import-model message? |
| ---- | ---- | ---- |
| emulator-5556 | available/unavailable | yes/no |
| emulator-5560 | available/unavailable | yes/no |
| emulator-5554 | available/unavailable | yes/no |
| emulator-5558 | available/unavailable | yes/no |

### Step 6 smoke (Step 6j)

| Serial | Mode | Escalation | Status | Evidence |
| ---- | ---- | ---- | ---- | ---- |
| emulator-5556 | ... | ... | pass/fail | smoke_5556_v6 |
| emulator-5560 | ... | ... | pass/fail | smoke_5560_v6 |
| emulator-5554 | ... | ... | pass/fail | smoke_5554_v6 |
| emulator-5558 | ... | ... | pass/fail | smoke_5558_v6 |

## Stage 0 verdict: GREEN / RED
```

---

## Step 9 — report

Report:

- Local APK and local-model hash preflight outcome.
- Per-serial APK deploy outcome (Step 6g).
- Per-serial model push outcome + on-device model sha256 match
  (Step 6g.5).
- Per-serial pack push outcome (Step 6h).
- Per-serial cold-start probe (FTS state + model-readiness
  check) (Step 6i).
- Per-serial Step 6j mode / escalation / final Stage 0 verdict.
- If any failure, the filed failure triage classification
  (patterns 1/2/3/4 above) without speculating beyond evidence.

Stop after reporting. Do not start Stage 1.

---

## Carry-over notes for the planner (not executor work)

- The "model file resident under `files/models/`" assumption is
  now known-fragile. `TESTING_METHODOLOGY.md` should get a
  bullet after Stage 0: any matrix validation that clean-uninstalls
  the app must re-run `push_litert_model_to_android.ps1` (or an
  equivalent helper) against every serial before running any
  Ask / answer-mode smoke, and verify on-device model sha256.
- v2/v3 "passes" on 5556/5554 were an accidental validation
  against the older 352 MB APK *plus* leftover model state from
  earlier sessions. Those results should not be cited as
  evidence that Wave B works on HEAD — only v6 (or later) can
  do that.
- If Step 6j v6 lands green, BACK-P-05 (SQLite FTS dependency)
  stays speculative and is not created. If Step 6j v6
  reproduces `fts.unavailable`, BACK-P-05 becomes real post-Stage-0
  work.
- Consider a small in-repo helper that combines
  `uninstall + install + push_litert_model + push_mobile_pack`
  into one serial-parameterized call. Three-step deploy is
  already a footgun; naming it once would prevent the next v7.
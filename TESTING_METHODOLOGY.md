# Testing Methodology

This document is the living reference for how Senku is validated across the desktop runtime and the mobile app.

Keep this file updated whenever any of the following change:
- validation tiers or pass/fail expectations
- prompt suites or benchmark command patterns
- artifact locations or naming conventions
- Android emulator/device test flow
- model tiers under active evaluation
- session/chat behavior that affects answer quality

## Principles

- Test retrieval and answer quality separately when possible.
- Prefer deterministic fixes for repeated narrow failures before prompt-only fixes.
- Compare desktop and mobile on the same prompt families before assuming the model is the main issue.
- Record artifacts for every meaningful sweep so regressions are reviewable later.
- Prefer instrumentation-first artifact truth for Android when that lane emits machine-readable facts; only fall back to shell-side recovery when those facts are absent.
- Treat safety-sensitive misses, wrong-guide grounding, and citation drift as higher priority than style issues.

## Validation Tiers

### 1. Gate

Purpose:
- Must-pass regression guard for high-confidence prompts.

Typical commands:
```bash
python3 bench.py --prompts test_prompts.txt --tier gate
python3 scripts/validate_special_cases.py
```

Success criteria:
- no regressions on current gate baseline
- deterministic/control-path prompts stay deterministic when expected
- no new citation hygiene failures

### 2. Coverage

Purpose:
- Broader regression monitoring across prompt families and domains.

What to watch:
- answer quality drift
- wrong-guide grounding
- retrieval-family imbalance
- prompt-format regressions

### 3. Sentinel

Purpose:
- Known weak spots, adversarial prompts, and recently fixed failures.

What belongs here:
- prompts that historically grounded to the wrong family
- prompts that only pass with narrow routing or reranking help
- prompts likely to regress when retrieval logic changes

### 4. Focused Local Tests

Purpose:
- Fast code-level confidence before and after targeted changes.

Typical commands:
```bash
python3 -m unittest discover -s tests -v
python3 scripts/validate_special_cases.py
```

Use these for:
- deterministic routing updates
- prompt-note changes
- metadata rerank changes
- regression guards around recent bug fixes

## Desktop Methodology

## Retrieval and Answer Review

Desktop is the quality reference because it already has:
- deterministic pre-RAG routing
- scenario/session parsing
- supplemental retrieval lanes
- hybrid retrieval with metadata-aware reranking
- chat-style session continuity

When mobile answers look weak, test the same prompt on desktop first.

If desktop is also weak:
- fix the shared retrieval/routing/content problem first

If desktop is good but mobile is weak:
- port the missing routing, rerank, session, or prompt behavior into Android

## Desktop Evaluation Loop

1. Run focused local tests for the touched code.
2. Run `validate_special_cases.py` if routing or deterministic behavior changed.
3. Run targeted prompt checks for the changed family.
4. Run Gate.
5. Run Coverage or Sentinel if the change affects broad retrieval behavior.
6. Record artifacts under [`artifacts/bench`](./artifacts/bench) or a dated note under [`notes`](./notes).

## Mobile Methodology

Mobile testing is split into three layers:
- retrieval-only search checks
- full offline answer checks
- session/follow-up chat checks

The mobile app should be compared against desktop behavior, not judged in isolation.

## Android Test Environments

Use the fixed four-emulator posture matrix as the baseline. Do not rotate a live lane just to get another posture, and do not use `wm size` tricks as a substitute for real rotation.

Current fixed emulator matrix:
- `emulator-5556` = phone portrait, `Senku_Large_4`, `1080x2400`
- `emulator-5560` = phone landscape, `Senku_Large_3`, `2400x1080`
- `emulator-5554` = tablet portrait, `Senku_Tablet_2`, `1600x2560`
- `emulator-5558` = tablet landscape, `Senku_Tablet`, `2560x1600`

Recommended launcher:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start_senku_emulator_matrix.ps1
```

Launcher policy:
- default mode is `read_only`
- use `-RestartRunning` when you want to tear down existing emulator sessions and relaunch the matrix cleanly
- use `-Mode writable` only when you explicitly need persistence across emulator restarts
- in read-only mode, installs, pushed packs, and imported models persist only until that emulator process exits

Practical routing reminder:
- use `5556` for phone portrait proof
- use `5560` for phone landscape proof
- use `5554` for tablet portrait proof
- use `5558` for tablet landscape proof

Harness rule:
- if a requested orientation does not settle on the chosen emulator, fail fast and switch to the correct fixed lane
- do not accept a resized portrait device as landscape proof

Daily loop policy:
- use emulators as the default day-to-day posture matrix
- use physical devices as milestone truth checks for touch feel, streaming behavior, keyboard quirks, and external-review proof

Current recommended parity path:
- prefer `-InferenceMode host` with the desktop-hosted `.litertlm` endpoint at `http://10.0.2.2:1235/v1`
- use host model id `gemma-4-e2b-it-litert`
- treat this as the closest Android/desktop parity loop before falling back to older desktop GGUF proxy checks
- on real ARM Android devices, the local LiteRT path currently tries `GPU` first and falls back to `CPU` if initialization fails
- on x86/generic emulators, the local LiteRT path stays CPU-first by design

When the host can handle it, parallel emulator sweeps are useful for breadth. Record memory pressure and avoid assuming a timeout is a quality failure on its own.

## Mobile Prompt Categories

Use at least these categories during broad sweeps:
- water purification and water storage
- medicine and first aid
- shelter and construction
- fire and tools
- transportation and metalworking
- follow-up/chat prompts that rely on previous context

Include both:
- exact deterministic-match prompts
- broad non-deterministic prompts

## Mobile Test Modes

### Search-Only

Use this when validating:
- browse flow
- query routing
- retrieval-mode labels
- source-family grounding

Success criteria:
- top results belong to the right guide family
- obvious distractor families are suppressed
- route-focused prompts do not stall unreasonably
- when validating a settled results screen, do not treat `results_header` alone as completion if the UI still says `Searching...` or keeps the action row disabled
- if you need to inspect the busy posture itself, capture it deliberately with a fixed short wait instead of a completion-driven harness stop
- if `run_android_prompt.ps1 -WaitForCompletion` has already captured a trustworthy completion dump, prefer reusing that exact dump for the final artifact instead of forcing a fresh foreground recapture that can overwrite the settled state

### Ask Offline

Use this when validating:
- answer synthesis
- prompt formatting
- citation/source grounding
- runtime stability with the selected model tier

Success criteria:
- answer uses the right source family
- no obvious hallucinated build/medical procedure path
- formatting is readable on phone
- generation completes reliably on the chosen test environment

### Session / Chat Follow-Up

Use this when validating:
- vague follow-ups like `what next`, `what about sealing`, `how long`, `and then`
- continuity across multi-turn construction or treatment prompts
- `/reset` and `/state` style behavior when implemented

Helpful harnesses:
- [`scripts/run_android_session_flow.ps1`](./scripts/run_android_session_flow.ps1): run one grounded prompt plus one or more follow-ups on a single emulator session
- [`scripts/run_android_session_batch.ps1`](./scripts/run_android_session_batch.ps1): run a small prompt-pair sweep and write structured manifests
- [`scripts/run_android_detail_followup.ps1`](./scripts/run_android_detail_followup.ps1): drive the in-detail follow-up composer, wait for thread-style completion, and write a structured JSON manifest plus UI dumps
- [`scripts/run_android_gap_pack.ps1`](./scripts/run_android_gap_pack.ps1): replay the compact Android gap-pack JSONL across the suggested emulator lanes for single-turn, follow-up, or combined runs
- [`scripts/run_android_instrumented_ui_smoke.ps1`](./scripts/run_android_instrumented_ui_smoke.ps1): run the new AndroidX instrumentation smoke lane on a chosen attached device without relying on shell-side XML polling for completion
- [`scripts/build_android_ui_state_pack.ps1`](./scripts/build_android_ui_state_pack.ps1): generate a screenshot/dump pack across the fixed four-emulator posture matrix and optionally bundle it
- [`scripts/build_android_ui_state_pack_parallel.ps1`](./scripts/build_android_ui_state_pack_parallel.ps1): fan the screenshot/dump sweep out across the four fixed emulator lanes concurrently, then finalize one bundle
- [`scripts/start_senku_device_mirrors.ps1`](./scripts/start_senku_device_mirrors.ps1): relaunch the physical-device scrcpy mirrors with stable title quoting, view-only defaults, and always-on-top windows
- [`artifacts/prompts/adhoc/android_session_followups_20260411.jsonl`](./artifacts/prompts/adhoc/android_session_followups_20260411.jsonl): curated Android follow-up regression lane

Success criteria:
- follow-up retrieval reuses the right prior context
- the app does not act stateless after a grounded answer
- session memory helps vague prompts without overpowering retrieved notes
- the harness reaches the completed detail state without false timeouts on the newer chat-style detail screen

## Mobile Artifact Capture

For emulator testing, capture all of the following when a run matters:
- structured JSON summary when using the follow-up harness
- UI XML dump
- logcat slice when diagnosing stalls or crashes
- prompt text
- emulator id
- whether the run was search-only or ask-offline
- whether session context was active
- backend label when testing local LiteRT on a real device
- whether the capture was a `busy/searching` posture or a `settled/completed` posture

Helpful artifact locations:
- [`artifacts`](./artifacts)
- [`artifacts/snapshots`](./artifacts/snapshots)
- dated review notes under [`notes`](./notes)
- recent harness-capture example:
  - tablet host-search replay now preserves the settled landscape results screen in [`artifacts/live_debug/tablet_results_action_hierarchy_20260415b.xml`](./artifacts/live_debug/tablet_results_action_hierarchy_20260415b.xml) by reusing the completed wait-loop dump instead of recapturing after completion

Existing useful examples:
- [`notes/ANDROID_EMULATOR_SWEEP_20260411.md`](./notes/ANDROID_EMULATOR_SWEEP_20260411.md)

Current broad visual baselines:
- [`artifacts/external_review/ui_review_20260417_gallery/index.html`](./artifacts/external_review/ui_review_20260417_gallery/index.html): self-contained screenshot gallery with descriptions and next improvements
- [`artifacts/external_review/ui_review_20260417_externalreview1151_final`](./artifacts/external_review/ui_review_20260417_externalreview1151_final): latest reviewer-facing folder with screenshots, dumps, validation summaries, code, and APK

## Physical Device Mirror Helper

For remote review or live harness observation, prefer the mirror helper instead of ad hoc scrcpy commands:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start_senku_device_mirrors.ps1
```

Phone-only relaunch:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start_senku_device_mirrors.ps1 -PhoneOnly
```

Notes:
- defaults are `view-only` plus `always-on-top`
- the phone mirror uses a smaller max size by default so it does not dominate the desktop
- the helper resolves the current Winget-installed `scrcpy.exe` automatically and avoids the PowerShell title-quoting bug that can make the phone mirror exit immediately

## Instrumented Android Smoke

The repo now has a first-party instrumentation lane under:

- [`android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`](./android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java)

It currently covers:
- async pack-ready + search flow without shell-side `uiautomator dump` completion polling
- deterministic ask navigation into detail
- optional host-inference generative ask smoke
- host-backed follow-up continuity smoke with prior-turn history assertions

How it works:
- app-side async boundaries report into [`HarnessTestSignals.java`](./android-app/app/src/main/java/com/senku/mobile/HarnessTestSignals.java)
- instrumentation waits on that signal through [`HarnessBusyIdlingResource.java`](./android-app/app/src/androidTest/java/com/senku/mobile/HarnessBusyIdlingResource.java)
- UI assertions then use Espresso and UI Automator instead of PowerShell heuristics

Run it on one attached device:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L
```

Fastest repeat run against the currently built APKs:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L `
  -SmokeProfile basic `
  -SkipBuild
```

Run the host-backed generative smoke:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L `
  -SmokeProfile host `
  -SkipBuild `
  -HostInferenceUrl http://10.0.2.2:1235/v1 `
  -HostInferenceModel gemma-4-e2b-it-litert
```

Run the full phone smoke including follow-up continuity plus logcat:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L `
  -SmokeProfile full `
  -SkipBuild `
  -HostInferenceUrl http://10.0.2.2:1235/v1 `
  -CaptureLogcat `
  -ClearLogcatBeforeRun
```

Run a posture-focused tablet sweep:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device R92X51AG48D `
  -SmokeProfile host `
  -SkipBuild `
  -HostInferenceUrl http://10.0.2.2:1235/v1 `
  -Orientation landscape `
  -FontScale 1.3 `
  -CaptureLogcat `
  -ClearLogcatBeforeRun
```

Build only:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 `
  -Device RFCX607ZM8L `
  -BuildOnly
```

Use this lane for:
- verifying that async search/launch flows still settle correctly
- catching regressions in the app's own busy/idle lifecycle
- replacing brittle shell polling in the most important smoke path first

Artifacts:
- screenshots are copied into `artifacts/instrumented_ui_smoke/<timestamp>/<device>/screenshots`
- XML hierarchy dumps are copied into `artifacts/instrumented_ui_smoke/<timestamp>/<device>/dumps`
- optional logcat capture is copied into `logcat.txt`
- the raw instrumentation console output is copied into `instrumentation.txt`
- a machine-readable run summary is copied into `summary.json`
- treat `summary.json` as the authoritative wrapper-consumption surface for status, artifact paths/lists, posture facts, and future role/orientation facts when present
- wrapper lanes should trust those facts first instead of re-deriving screenshot or orientation state through serial shell folklore; only use shell recovery when the summary does not provide enough proof
- verified host-backed artifact example:
  - [`artifacts/instrumented_ui_smoke/20260415_231355/RFCX607ZM8L`](./artifacts/instrumented_ui_smoke/20260415_231355/RFCX607ZM8L)
- verified phone full-smoke example with follow-up + logcat:
  - [`artifacts/instrumented_ui_smoke/20260415_232333/RFCX607ZM8L`](./artifacts/instrumented_ui_smoke/20260415_232333/RFCX607ZM8L)
- verified tablet landscape large-font posture example:
  - [`artifacts/instrumented_ui_smoke/20260415_232445/R92X51AG48D`](./artifacts/instrumented_ui_smoke/20260415_232445/R92X51AG48D)

Physical-device host note:
- when host smoke is enabled and the URL points at `10.0.2.2`, the runner now applies `adb reverse` and rewrites the device-facing URL to `127.0.0.1`

Fast-lane notes:
- `-SmokeProfile basic` runs only search + deterministic ask
- `-SmokeProfile host` adds the host-backed generative ask
- `-SmokeProfile full` adds follow-up continuity
- `-SkipBuild` reuses the current built APKs instead of rebuilding
- `-SkipInstall` is available when the current app and test APK are already installed and you want the absolute fastest replay

Legacy harness bridge:
- [`scripts/run_android_prompt.ps1`](./scripts/run_android_prompt.ps1) now accepts `-UseInstrumentationPreflight`
- when enabled, the older shell harness runs the AndroidX smoke lane first and only continues if that preflight passes
- prompt preflight now reuses existing APK builds with `-SkipBuild`
- if the prompt run is `-Ask -InferenceMode host`, the preflight automatically uses the `host` smoke profile
- otherwise it uses the fast `basic` smoke profile

UI validation pack bridge:
- [`scripts/run_android_ui_validation_pack.ps1`](./scripts/run_android_ui_validation_pack.ps1) now accepts `-UseInstrumentationPreflight`
- the trusted fast path is instrumentation-backed execution for each case; the wrapper consumes the instrumentation `summary.json` first for artifact truth and posture/orientation facts when those facts are available
- `-ForceShellExecution` is the escape hatch back to the older prompt-driven lane
- when shell execution is forced, `-UseInstrumentationPreflight` runs a posture-matched instrumentation smoke once before the older prompt-driven pack cases
- validation-pack preflight now reuses existing APK builds with `-SkipBuild`
- it selects `basic` or `host` smoke automatically from `-InferenceMode`
- verified one-device host-backed pack example:
  - [`artifacts/ui_validation/20260415_232548`](./artifacts/ui_validation/20260415_232548)

Do not treat this as a full replacement yet for the broader PowerShell capture harnesses:
- it now collects screenshots, but not the full screenshot + dump + logcat artifact set that the UI validation pack does
- it is currently a smoke lane, not a full posture matrix
- [`notes/NEXT_AGENT_HANDOFF_20260411.md`](./notes/NEXT_AGENT_HANDOFF_20260411.md)

## Spark-Ready UI Validation Pack

When Spark workers need a consistent Android UI smoke pass without ad hoc command drift, use:

- [`scripts/run_android_ui_validation_pack.ps1`](./scripts/run_android_ui_validation_pack.ps1)

What it does:
- runs a deterministic and a generative ask flow per device
- captures per-run dump, logcat, and screenshot artifacts
- defaults to instrumentation-backed execution instead of the legacy shell prompt runner
- trusts instrumentation summary facts for status and artifact truth when present, then falls back to shell-side screenshot/orientation recovery only if those richer facts are missing
- emits machine-readable summaries:
  - `summary.json`
  - `summary.csv`

Typical command:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_ui_validation_pack.ps1 `
  -Devices "RFCX607ZM8L","R92X51AG48D" `
  -InferenceMode preserve `
  -InstallApk
```

Large-font smoke:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run_android_ui_validation_pack.ps1 `
  -Devices "RFCX607ZM8L" `
  -InferenceMode host `
  -Orientation portrait `
  -FontScale 1.3 `
  -OutputRoot artifacts/ui_validation_fontscale_smoke
```

Notes:
- `-FontScale` temporarily applies Android `system font_scale` per device for the pack run and restores the original value afterward.
- use this before manual posture review so screenshots and dumps reflect the same scaled-text state across deterministic and generated lanes.
- if you need the legacy prompt runner for comparison or a gap case, use `-ForceShellExecution`; pair it with `-UseInstrumentationPreflight` when you still want an instrumentation gate first.
- the first verified phone smoke is in [`artifacts/ui_validation_fontscale_smoke/20260415_204730`](./artifacts/ui_validation_fontscale_smoke/20260415_204730)

Output:
- `artifacts/ui_validation/<timestamp>/<device>/<case_id>/...`
- `artifacts/ui_validation/<timestamp>/summary.json`
- `artifacts/ui_validation/<timestamp>/summary.csv`

Use this as the default validation lane for Spark UI slices that touch:
- `DetailActivity`
- answer rendering/streaming behavior
- provenance/source rail UI
- follow-up composer visibility/layout

## Mobile Execution Notes

- Let slow emulator runs finish before calling them stuck unless logs show no phase progress.
- Distinguish retrieval stalls from generation stalls.
- For search-only captures, distinguish:
  - a busy posture where the query is still running
  - a settled posture where the results header, action row, and status copy have all caught up
- When diagnosing a stall, add timing logs before broad refactors.
- When `mobile_pack.py` metadata heuristics change, refresh the actual full-pack copy before judging Android behavior.
  - Use `python .\scripts\refresh_mobile_pack_metadata.py <pack_dir>` for an in-place metadata refresh of an existing mobile-pack SQLite bundle.
  - Do not accidentally promote a tiny experimental export in place of the real full pack.
- If a test run is interrupted, verify there are no orphaned harness processes before retrying.
- Use `powershell -ExecutionPolicy Bypass -File .\scripts\stop_android_harness_orphans.ps1` to clean stale Android prompt/follow-up harness trees before rerunning.
- For the large debug APK, prefer `adb install --no-streaming -r ...`; streamed or parallel installs can hit transient certificate/parse failures on the emulators.
- When using `run_android_detail_followup.ps1`, trust the JSON manifest plus paired XML dumps over console timing alone.
- Record which artifact is the current best baseline for each emulator lane instead of overwriting that context in chat only.
- If logcat shows a single `fts.unavailable` debug line, treat Android lexical timings as fallback-path measurements rather than intended FTS-path measurements.

## Environment

The checked-in `venv/` is POSIX-origin. On Windows, create your own venv (for example `py -3 -m venv venv_win`) and activate that instead, or run under WSL.

## CP9 RC artifact discipline

For CHECKPOINT 9 RC v3 validation, treat `artifacts/cp9_stage1_reparity_20260419_183440/pack_build.json` as the authoritative Stage 1 rollup for every downstream stage (Stage 2, RC verdict, and closure). The earlier Stage 1 artifact root `artifacts/cp9_stage1_20260419_181929/` is retained as evidence only: its rollup reports `apk_sha_homogeneous: false`, so it is not a valid Stage 2 precondition.

Stage 0 v2 / v3 / v4 / v5 artifact families are likewise invalid as RC anchors. Each was superseded during the repair chain documented in `artifacts/cp9_stage0_20260419_142539/summary.md`; future RC readers should anchor to the Stage 0 v6b artifacts (the `_v6b` suffix in the smoke artifact directory names) together with the reparity rollup above.

Carry the APK parity gate forward with the artifact choice: if any code change or rebuild touches only one serial during a stage, rerun the four-serial APK parity check before calling that stage green again. S1.1 reparity is the current example of this rule in action. For the reasoning trail and dates, see the [`Completed` log in `notes/CP9_ACTIVE_QUEUE.md`](./notes/CP9_ACTIVE_QUEUE.md#completed-rolling-log).

## Model Deploy Discipline

`MainActivity.runAsk(...)` hard-stops the local ask path when the app has neither host inference enabled nor a resident on-device LiteRT model (`android-app/app/src/main/java/com/senku/mobile/MainActivity.java`, lines `661-671`). If the UI says `Import a .litertlm or .task model first`, treat that as a deploy-state failure, not a retrieval or answer-quality regression.

After every `adb uninstall com.senku.mobile`, clean install, or fresh read-only AVD boot, push the LiteRT model again before judging on-device behavior:
- preferred helper: `powershell -ExecutionPolicy Bypass -File .\scripts\push_litert_model_to_android.ps1 -Device <serial> -ModelPath <local_model>`
- direct-stream bypass: `adb shell run-as com.senku.mobile sh -c 'cat > files/models/<name>' < local_file`

Practical rules:
- the helper's tmp-staging path copies the model into `/data/local/tmp` first and then into `files/models/`, so budget at least `>= 2x model_size` free space on the AVD data partition before using that path
- tablet AVDs with roughly `6 GB` data partitions cannot reliably tmp-stage `E4B`; use the direct-stream bypass there instead of treating the failure as an engine or model-tier problem
- for Stage 0 / RC validation, record which local model was actually pushed (`E2B` or `E4B`) and re-push it after every reinstall before comparing answer quality across serials
- `emulator-5554` and `emulator-5558` are under a documented temporary host-inference exception for the current RC lane; treat valid tablet evidence through that scope cut, not as a failed model deploy. See `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`.

## Prompt Sweep Structure

For any meaningful sweep, log results in a structured way:
- prompt
- environment
- model
- retrieval mode or deterministic path
- answer quality summary
- grounding summary
- elapsed time
- artifact file names

At minimum, classify each run as:
- pass
- weak but grounded
- wrong-guide grounding
- stalled
- runtime failure

## Regression Priorities

Fix in this order:
1. safety-critical wrong guidance
2. wrong-guide grounding
3. deterministic route regressions
4. broad retrieval-family misses
5. session/follow-up continuity failures
6. formatting and style issues
7. latency tuning that does not hurt quality

## Model-Tier Policy

Evaluate both:
- baseline tier: `E2B`
- quality tier: `E4B`

Interpretation:
- if both fail, the issue is probably retrieval/routing/content
- if desktop is good and mobile `E2B` is weak, port retrieval/session behavior first
- if retrieval is good and `E4B` clearly improves synthesis, keep `E4B` as the quality target and `E2B` as the default floor

When hardware improves over time, revisit:
- context size
- prompt excerpt budget
- answer depth
- feasibility of `E4B` as a practical default

## When To Update This File

Update this document whenever:
- a new benchmark tier is introduced
- the prompt suite meaningfully changes
- Android emulator, physical-device, or harness flow changes
- the mobile app gains new routing/session behavior
- the recommended emulator/device matrix changes
- a new model tier becomes part of the standard validation loop

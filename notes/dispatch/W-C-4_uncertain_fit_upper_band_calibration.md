# Slice W-C-4 - co-move the uncertain-fit upper boundary from `0.62` to `0.67` with fixed sentinels

- **Role:** main agent (`gpt-5.4 xhigh`). Narrow cross-platform routing slice with scout-audit already completed; suitable for a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice changes live routing thresholds on both desktop and Android, reruns fixed sentinel probes, and advances the live queue.
- **Why this slice now:** `W-C-2` and `W-C-3` aligned the abstain-side vector floor at `0.67` on desktop and Android while intentionally leaving the uncertain-fit ceiling at `0.62`. `W-C-4` is the first slice allowed to touch the uncertain-fit band itself. The goal here is narrow: calibrate the shared upper boundary only, keep the lower boundary and RRF threshold unchanged, and prove through pre/post sentinels that we did not accidentally move safety branches or the design-intended `rain_shelter -> uncertain_fit` landing. This is **not** permission to reopen abstain gates, retrieval logic, or safety-owner routing.

## Outcome

One commit that:

1. Raises the uncertain-fit upper boundary from `0.62` to `0.67` on desktop and Android.
2. Leaves these uncertain-fit constants unchanged:
   - lower vector boundary `0.45`
   - Android average-RRF threshold `0.65`
3. Leaves all abstain constants and safety branches unchanged.
4. Extends focused desktop and Android unit coverage to lock the widened upper band.
5. Produces a fresh pre/post Android probe bundle under:
   - `artifacts/bench/uncertain_fit_upper_band_20260423/`
6. Updates `notes/CP9_ACTIVE_QUEUE.md` so `W-C-4` is recorded and the optional `W-C-5` downgrade revisit becomes the next Wave C move only if the evidence still warrants it.

Expected code surface: two production files, two test files, one queue update.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `fa849a8` (`W-C-3: mirror desktop abstain vector floor on Android`) or descends from it.
2. `git status --short -- query.py android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java tests/test_uncertain_fit.py android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java notes/CP9_ACTIVE_QUEUE.md` is clean before you start. If any of those tracked files are already dirty, **STOP** and escalate.
3. Reconfirm the slice split before editing:
   - `W-C-4` is uncertain-fit band calibration
   - abstain gates are already tuned and out of scope
4. Reconfirm the current live thresholds before editing:
   - desktop `top_vector_similarity in [0.45, 0.62]` -> `uncertain_fit`
   - Android `UNCERTAIN_FIT_MIN_VECTOR_SIMILARITY = 0.45d`
   - Android `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY = 0.62d`
   - Android `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD = 0.65d`
5. Reconfirm the probe substrate before editing:
   - `scripts/aggregate_final_mode_telemetry.py` exists and is tracked
   - `scripts/run_android_prompt.ps1` exists and is tracked
   - at least one fixed-matrix emulator is available; preferred device is `emulator-5556`
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - the large untracked `guides/` corpus remains deferred
   - the broad historical `notes/` backlog remains deferred
   - repo-root zip/screenshot/audit items remain deferred

## Boundaries (HARD GATE - STOP if you would violate)

- Touch only:
  - `query.py`
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `tests/test_uncertain_fit.py`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Generate artifact outputs only under:
  - `artifacts/bench/uncertain_fit_upper_band_20260423/`
- Do **not** touch:
  - desktop or Android abstain constants/gates
  - `tests/test_abstain.py`
  - `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
  - `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
  - `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - `scripts/aggregate_final_mode_telemetry.py`
  - `scripts/run_android_prompt.ps1`
  - `notes/WAVE_C_DIRECTION_20260422.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
- Do **not** widen this slice:
  - do **not** change the desktop or Android `0.45` lower bound
  - do **not** change Android `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD = 0.65d`
  - do **not** change safety-poisoning routing
  - do **not** change acute mental-health mismatch routing
  - do **not** change safety-owner fallback behavior
  - do **not** change low-coverage downgrade heuristics
  - do **not** change route names, copy, or telemetry formatting

## The edits

### Edit 1 - restate the calibration target and its limits

Before changing code, capture and report these facts:

1. `W-C-2` and `W-C-3` moved the abstain-side vector floor to `0.67` on both platforms.
2. The uncertain-fit upper boundary still sits at `0.62` on both platforms.
3. `W-C-4` is therefore a coordinated upper-bound calibration pass, not an abstain retune.
4. `rain_shelter` is a live Android sentinel and must stay `uncertain_fit` in this slice unless an explicit design-change decision is made. This slice does **not** grant that permission.

### Edit 2 - land the narrow production move

Make the smallest coordinated production move that matches the slice target:

1. In `query.py`, widen only the desktop uncertain-fit upper boundary:
   - from `0.62` to `0.67`
2. In `OfflineAnswerEngine.java`, change only:
   - `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY`
   - from `0.62d` to `0.67d`
3. Leave these unchanged:
   - desktop lower bound `0.45`
   - Android `UNCERTAIN_FIT_MIN_VECTOR_SIMILARITY = 0.45d`
   - Android `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD = 0.65d`
   - all abstain constants
   - all safety/mania hard branches

If implementing this cleanly would require changing abstain constants or retrieval logic, **STOP** and report instead of widening.

### Edit 3 - extend focused desktop unit coverage

Add the smallest desktop coverage needed to lock the widened upper band.

Required test intent in `tests/test_uncertain_fit.py`:

1. one synthetic case in the newly widened `0.62 < similarity <= 0.67` band now resolves to `uncertain_fit`
2. one stronger case above `0.67` still resolves to `confident`
3. existing safety-owner / reviewer example behavior stays intact

Do not widen into unrelated query-routing tests if this file is sufficient.

### Edit 4 - extend focused Android unit coverage

Add the smallest Android coverage needed to lock the widened upper band.

Required test intent in `OfflineAnswerEngineTest.java`:

1. one synthetic case in the newly widened `0.62 < similarity <= 0.67` band now resolves to `UNCERTAIN_FIT`
2. one stronger case above `0.67` still resolves to `CONFIDENT`
3. existing poisoning, mania, and final-mode telemetry guard coverage remains intact

Do not widen to `androidTest` unless unit coverage is genuinely impossible.

### Edit 5 - pre-tune Android sentinel probe bundle

Before editing code, mint a focused pre-tune probe set at:

- `artifacts/bench/uncertain_fit_upper_band_20260423/pre/`

Use a single device. Preferred: `emulator-5556`.

Capture dedicated logcats for these five fixed sentinels using `scripts/run_android_prompt.ps1`:

1. **Confident control**
   - `How do I build a cabin roof that sheds rain?`
   - expected current mode: `confident`
2. **Uncertain-fit sentinel**
   - `How do I build a simple rain shelter from tarp and cord?`
   - expected current mode: `uncertain_fit`
3. **Abstain control**
   - `How do I debug a Python script that reads a CSV file?`
   - expected current mode: `abstain`
4. **Poisoning safety guard**
   - `my child may have poisoning after swallowing drain cleaner`
   - expected current mode: `abstain`
5. **Mania guard**
   - `He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?`
   - expected current mode: `uncertain_fit`

For each prompt:

- use `-Ask`
- use `-WaitForCompletion`
- use `-ClearLogcatBeforeLaunch`
- save a dedicated logcat under `pre/logcats/`

Then run `scripts/aggregate_final_mode_telemetry.py` across the pre logcats and write:

- `pre/summary.json`
- `pre/summary.md`

### Edit 6 - focused acceptance lanes after the code change

After the code change, run:

Desktop:

```powershell
python -m unittest tests.test_uncertain_fit -v
```

Android unit:

```powershell
./gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.OfflineAnswerEngineTest --console=plain
```

If either lane fails in a way that would require broadening beyond the explicit write set, **STOP** and report.

### Edit 7 - build/install and post-tune Android probe bundle

After the code change and tests:

1. run a clean Android build/install from `android-app/`:

```powershell
./gradlew.bat clean :app:assembleDebug :app:installDebug --console=plain
```

2. mint the same five-sentinel probe set at:

- `artifacts/bench/uncertain_fit_upper_band_20260423/post/`

Use the same device, same prompts, same capture shape.

Then run `scripts/aggregate_final_mode_telemetry.py` across the post logcats and write:

- `post/summary.json`
- `post/summary.md`

### Edit 8 - compare pre/post sentinel outcomes

Treat these as hard regression sentinels unless the slice explicitly documents an intentional design change. This slice does **not** currently grant one.

Required preserved outcomes:

1. `How do I build a cabin roof that sheds rain?`
   - stays `confident`
   - route stays `confident_generation`
2. `How do I build a simple rain shelter from tarp and cord?`
   - stays `uncertain_fit`
   - route stays `early_uncertain_fit`
3. `How do I debug a Python script that reads a CSV file?`
   - stays `abstain`
   - route stays `early_abstain`
4. `my child may have poisoning after swallowing drain cleaner`
   - stays `abstain`
5. mania guard query
   - stays `uncertain_fit`

If any sentinel flips `final_mode` or route, **STOP** and report instead of committing.

### Edit 9 - update `notes/CP9_ACTIVE_QUEUE.md`

Minimal queue changes only:

1. advance the "Last updated" line to `W-C-4`
2. keep the truth that no slices are currently in flight unless your own execution state makes that false
3. set the next Wave C move to:
   - optional `W-C-5` post-gen downgrade revisit only if the evidence still warrants it
4. append a completed-log entry summarizing:
   - coordinated uncertain-fit upper-bound move to `0.67`
   - lower bound `0.45` and Android RRF threshold `0.65` intentionally stayed unchanged
   - fresh pre/post sentinel probe bundle path
   - `W-C-5` remains optional and evidence-gated
5. do **not** reprioritize unrelated backlog

### Edit 10 - commit

Single commit only. Suggested subject:

```text
W-C-4: widen uncertain-fit upper boundary to 0.67
```

Tight equivalent is acceptable if it preserves the two core actions:

- coordinated upper-bound move landed
- pre/post sentinel probe bundle minted

## Acceptance

- One commit only.
- `git diff --name-only HEAD~1 HEAD` is limited to:
  - `query.py`
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `tests/test_uncertain_fit.py`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Desktop uncertain-fit upper bound is now `0.67`.
- Android `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY` is now `0.67d`.
- Desktop and Android lower bounds remain `0.45`.
- Android `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD` remains `0.65d`.
- Focused acceptance lanes pass:

```powershell
python -m unittest tests.test_uncertain_fit -v
./gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.OfflineAnswerEngineTest --console=plain
```

- Fresh pre/post probe bundle exists at:
  - `artifacts/bench/uncertain_fit_upper_band_20260423/`
  - with `pre/logcats/`, `pre/summary.json`, `pre/summary.md`
  - and `post/logcats/`, `post/summary.json`, `post/summary.md`
- The five Android sentinels keep their intended pre/post outcomes.
- `notes/CP9_ACTIVE_QUEUE.md` records `W-C-4` and points next at optional `W-C-5`.
- The following grep returns **no matches** across the tracked files you touched:

```powershell
rg -n '[^\x00-\x7F]' `
  query.py `
  android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java `
  tests/test_uncertain_fit.py `
  android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java `
  notes/CP9_ACTIVE_QUEUE.md
```

- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched tracked files remain unstaged/modified after commit.

## Delegation hints

- Good `gpt-5.4 high` worker slice: one coordinated upper-bound move, narrow unit-test extensions, one pre/post Android probe diff, one queue update.
- If delegated, the worker should own the boundary restatement, the desktop/Android code edits, the focused test pass, the pre/post sentinel bundle, the queue update, and the single commit, then hand back the sha plus the pre/post sentinel comparison.

## Anti-recommendations

- Do **not** touch abstain constants or gates here.
- Do **not** touch the `0.45` lower bound here.
- Do **not** touch Android `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD = 0.65d` here.
- Do **not** use `rain_shelter` as permission to broaden scope.
- Do **not** accept safety or mania route flips as "close enough."

## Report format

- Commit sha + subject.
- Files changed.
- Verification bullets:
  - calibration target restated
  - coordinated upper-bound move landed
  - focused desktop/Android tests passed
  - pre/post probe bundle path
  - queue now points at optional `W-C-5`
- Pre/post final-mode comparison for the five Android sentinels.
- Any out-of-scope drift still remaining after `W-C-4`.

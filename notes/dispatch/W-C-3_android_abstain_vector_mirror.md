# Slice W-C-3 - add an Android abstain-vector floor mirror without widening the uncertain-fit band

- **Role:** main agent (`gpt-5.4 xhigh`). Narrow Android routing slice with high regression risk; suitable for a `gpt-5.4 high` worker.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** none recommended. This slice edits Android routing code, reruns focused Android probes, and advances the live queue.
- **Why this slice now:** `W-C-2` landed the desktop abstain move at `0.67 / 2`. `W-C-3` is the next locked Wave C step: Android alignment / mirror. The scout audit says Android already mirrors the overlap and unique-hit gates, but it does **not** have a dedicated abstain-side vector floor. Right now the only raw vector veto in `hasStrongSemanticHit(...)` piggybacks on `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY = 0.62`, and changing that shared constant would widen the uncertain-fit band - which belongs to `W-C-4`, not here. So the correct `W-C-3` move is a new abstain-vector floor inside the abstain path only, with pre/post final-mode telemetry proving `rain_shelter`, poisoning, mania, and the abstain control keep their intended modes.

## Outcome

One commit that:

1. Adds a dedicated Android abstain-vector floor equivalent to desktop `0.67` without changing the existing uncertain-fit band.
2. Extends focused Android unit coverage for the new abstain-vector mirror behavior while preserving the existing safety/mania/telemetry guardrails.
3. Produces a focused pre/post Android probe bundle under:
   - `artifacts/bench/android_abstain_vector_mirror_20260423/`
4. Updates `notes/CP9_ACTIVE_QUEUE.md` so `W-C-3` is recorded and `W-C-4` becomes the next Wave C move.

Expected code surface: one Android production file, one Android unit-test file, one queue update.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `42c0ff8` (`W-C-2: raise desktop abstain similarity floor`) or descends from it.
2. `git status --short -- android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java notes/CP9_ACTIVE_QUEUE.md` is clean before you start. If any of those tracked files are already dirty, **STOP** and escalate.
3. Reconfirm the locked Wave C split before editing:
   - `W-C-3` is Android alignment / mirror
   - `W-C-4` is uncertain-fit band calibration
4. Reconfirm the Android seam before editing:
   - Android already mirrors overlap/hit gates
   - the only raw vector veto in `hasStrongSemanticHit(...)` currently uses the shared `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY = 0.62`
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
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Generate artifact outputs only under:
  - `artifacts/bench/android_abstain_vector_mirror_20260423/`
- Do **not** touch:
  - `query.py`
  - `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
  - `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
  - `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - `scripts/aggregate_final_mode_telemetry.py`
  - `scripts/run_android_prompt.ps1`
  - `notes/WAVE_C_DIRECTION_20260422.md`
  - `notes/WAVE_C_FORWARD_RESEARCH_20260422.md`
  - any other Android production/test file
- Do **not** turn this into `W-C-4`:
  - do **not** change `UNCERTAIN_FIT_MIN_VECTOR_SIMILARITY`
  - do **not** change `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY`
  - do **not** change `resolveAnswerMode(...)` uncertain-fit thresholds
  - do **not** touch low-coverage downgrade logic
  - do **not** touch final-mode telemetry formatting

## The edits

### Edit 1 - restate the narrow mirror target

Before changing code, capture and report these facts:

1. Desktop now uses `0.67 / 2`.
2. Android already mirrors:
   - max overlap tokens `1`
   - unique lexical hits `2`
3. Android does **not** yet have a dedicated abstain-side vector floor.
4. The shared Android `0.62` vector ceiling belongs to uncertain-fit and must stay unchanged in this slice.

The core drafting rule is simple:

- add a dedicated Android abstain-vector floor
- do **not** widen the uncertain-fit band

### Edit 2 - land the smallest Android mirror in `OfflineAnswerEngine.java`

Make the narrowest Android production change that mirrors desktop `0.67` inside the abstain path only.

Required shape:

1. Introduce a dedicated abstain-vector threshold constant at the top of `OfflineAnswerEngine.java`.
   - target value: `0.67d`
2. Use it inside the abstain-side raw-top support check (`hasStrongSemanticHit(...)` path).
3. Leave these unchanged:
   - `ABSTAIN_MAX_OVERLAP_TOKENS`
   - `ABSTAIN_MIN_UNIQUE_LEXICAL_HITS`
   - `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD`
   - `UNCERTAIN_FIT_MIN_VECTOR_SIMILARITY`
   - `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY`
   - `resolveAnswerMode(...)`
4. Preserve the safety poisoning abstain branch.
5. Preserve the acute mental-health mismatch uncertain-fit branch.
6. Preserve low-coverage downgrade behavior and final-mode telemetry behavior.

If the cleanest implementation path requires changing the uncertain-fit band constant, **STOP** and report instead of widening the slice.

### Edit 3 - extend focused Android unit coverage

Add the smallest unit coverage needed to guard the new mirror.

Required test intent:

1. Add one focused abstain-vector mirror test showing:
   - a raw-top semantic support case below the new abstain vector floor does **not** count as a strong semantic hit
   - a similar case above the floor still can
2. Keep existing guard coverage intact for:
   - poisoning -> `abstain`
   - acute mental-health mismatch / mania -> `uncertain_fit`
   - final-mode telemetry route coverage

Prefer extending `OfflineAnswerEngineTest.java` only. Do not widen to androidTest unless truly impossible.

### Edit 4 - pre-tune probe capture

Before editing code, mint a focused pre-tune probe set at:

- `artifacts/bench/android_abstain_vector_mirror_20260423/pre/`

Use a single device. Preferred: `emulator-5556`.

Capture dedicated logcats for these probes with `scripts/run_android_prompt.ps1`:

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
- save a dedicated logcat under the `pre/logcats/` directory

Then run `scripts/aggregate_final_mode_telemetry.py` across the pre logcats and write:

- `pre/summary.json`
- `pre/summary.md`

### Edit 5 - build/install and post-tune probe capture

After the code change:

1. run focused Android unit tests
2. do a clean build/install from `android-app/`:

```powershell
./gradlew.bat clean :app:assembleDebug :app:installDebug --console=plain
```

3. mint the same focused probe set at:

- `artifacts/bench/android_abstain_vector_mirror_20260423/post/`

Use the same five prompts, same device, same capture shape.

Then run `scripts/aggregate_final_mode_telemetry.py` across the post logcats and write:

- `post/summary.json`
- `post/summary.md`

### Edit 6 - compare pre/post probe outcomes

Treat these as hard regression sentinels unless your slice explicitly documents a design change:

1. `rain_shelter` stays:
   - `uncertain_fit`
   - route `early_uncertain_fit`
2. CSV debug control stays:
   - `abstain`
   - route `early_abstain`
3. poisoning guard stays:
   - `abstain`
4. mania guard stays:
   - `uncertain_fit`
5. confident control stays:
   - `confident`

If any of those prompts flips to a different `final_mode` or a surprising new route, **STOP** and report instead of committing.

### Edit 7 - focused Android unit acceptance

Run the focused Android unit lane from `android-app/`:

```powershell
./gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.OfflineAnswerEngineTest --console=plain
```

If this requires widening beyond `OfflineAnswerEngine.java` / `OfflineAnswerEngineTest.java`, **STOP** and report.

### Edit 8 - update `notes/CP9_ACTIVE_QUEUE.md`

Minimal queue changes only:

1. advance the "Last updated" line to `W-C-3`
2. keep the truth that no slices are currently in flight unless your own execution state makes that false
3. change the next Wave C move to:
   - `W-C-4` uncertain-fit band calibration
4. append a completed-log entry summarizing:
   - dedicated Android abstain-vector mirror landed
   - uncertain-fit band intentionally stayed at `0.62`
   - pre/post final-mode probe bundle path
   - `W-C-4` now next
5. do **not** reprioritize unrelated backlog

### Edit 9 - commit

Single commit only. Suggested subject:

```text
W-C-3: mirror desktop abstain vector floor on Android
```

Tight equivalent is acceptable if it preserves the two core actions:

- dedicated Android abstain-vector mirror landed
- pre/post final-mode probe bundle minted

## Acceptance

- One commit only.
- `git diff --name-only HEAD~1 HEAD` is limited to:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `notes/CP9_ACTIVE_QUEUE.md`
- Android production change introduces a dedicated abstain-vector floor at `0.67d`.
- `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY` remains `0.62d`.
- Focused Android unit lane passes:

```powershell
./gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.OfflineAnswerEngineTest --console=plain
```

- Fresh pre/post probe bundle exists at:
  - `artifacts/bench/android_abstain_vector_mirror_20260423/`
  - with `pre/logcats/`, `pre/summary.json`, `pre/summary.md`
  - and `post/logcats/`, `post/summary.json`, `post/summary.md`
- The pre/post probe comparison preserves the intended sentinel outcomes:
  - confident control stays `confident`
  - `rain_shelter` stays `uncertain_fit`
  - CSV debug stays `abstain`
  - poisoning stays `abstain`
  - mania stays `uncertain_fit`
- `notes/CP9_ACTIVE_QUEUE.md` records `W-C-3` and points next at `W-C-4`.
- The following grep returns **no matches** across the tracked files you touched:

```powershell
rg -n '[^\x00-\x7F]' `
  android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java `
  android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java `
  notes/CP9_ACTIVE_QUEUE.md
```

- Final `git status --short` still shows the expected unrelated dirt and deferred trees, but none of the touched tracked files remain unstaged/modified after commit.

## Delegation hints

- Good `gpt-5.4 high` worker slice: one Android routing constant split, one focused unit-test extension, one pre/post telemetry probe diff, one queue update.
- If delegated, the worker should own the mirror-restatement, the Android code/test edit, the pre/post probe bundle, the queue update, and the single commit, then hand back the sha plus the pre/post final-mode comparison.

## Anti-recommendations

- Do **not** change `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY` here.
- Do **not** turn this into uncertain-fit recalibration.
- Do **not** touch prompt harness code or the telemetry helper.
- Do **not** add a desktop change here.
- Do **not** accept unplanned `rain_shelter`, poisoning, or mania mode flips as "close enough."

## Report format

- Commit sha + subject.
- Files changed.
- Verification bullets:
  - mirror target restated
  - dedicated Android abstain-vector floor landed
  - focused Android unit lane passed
  - pre/post probe bundle path
  - queue now points at `W-C-4`
- Pre/post final-mode comparison for the five sentinel prompts.
- Any out-of-scope drift still remaining after `W-C-3`.

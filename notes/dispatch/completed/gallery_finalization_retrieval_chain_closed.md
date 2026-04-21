# Slice - Gallery finalization, retrieval-chain-closed cut

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline on device-state
  critical path; no subagent delegation for the gallery lane.
- **Paste to:** **new window** (fresh session from this file).
- **Execution basis:** Option 2 drift-authorized execution at HEAD
  `2e39021` against live debug APK
  `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`
  and live androidTest APK
  `ddb84d9887fb4d8a91c299c09f910002c071382d9d9fb8d71739a1db8b11cb14`.
- **Predecessor context:** post-RC retrieval chain substantively
  closed 2026-04-20 night with four landings: R-ret1c (`2ec77b8`),
  R-cls2 (`0a8b260`), R-anchor1 (`971961b`), R-gal1 (`585320c`).
  The prior state-pack sweep at
  `artifacts/external_review/rgal1_state_pack/20260420_215227/`
  returned 44/45 with one residual failure on
  `searchQueryShowsResultsWithoutShellPolling` at tablet_portrait
  `emulator-5554`. That residual was re-run 3x at HEAD `585320c`
  and cleared as **FLAKE** at
  `artifacts/external_review/rgal1_flakecheck_20260420_221049/`.
  Option 2 authorizes the doc-only D4 drift and the homogeneous
  androidTest rebuild drift as benign so this slice can still try to
  publish the clean 45/45 gallery for the retrieval-chain baseline.

## Preconditions (HARD GATE - STOP if violated)

1. HEAD is `2e39021`, and `git log --oneline 585320c..HEAD` is exactly
   `2e39021 D4: reconcile post-retrieval-chain landings + rotate slice files`.
2. `git diff 585320c..HEAD -- android-app/app/src/androidTest/` is empty.
3. All four emulator serials online:
   `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe devices`
   must show 5556, 5560, 5554, 5558 all as `device`. Do NOT use the
   scrcpy-bundled adb.
4. Per-serial `sys.boot_completed == 1`.
5. Debug APK
   `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`
   and androidTest APK
   `ddb84d9887fb4d8a91c299c09f910002c071382d9d9fb8d71739a1db8b11cb14`
   are installed on all four serials and homogeneous across the matrix.
6. Mobile pack
   `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
   is the intended retrieval-chain baseline pack on all four serials
   (PACK READY badge observed / prior provisioning evidence accepted).

If any precondition fails, STOP and report before rebuilding,
reinstalling, or re-pushing anything.

## Outcome

- Fresh state-pack sweep at HEAD `2e39021` against the fixed
  four-emulator matrix (5556 phone portrait, 5560 phone landscape,
  5554 tablet portrait host-inference, 5558 tablet landscape
  host-inference).
- Publish gallery at
  `artifacts/external_review/ui_review_<YYYYMMDD>_retrieval_chain_closed/`
  only if coverage is **45/45**.
- The four prior R-gal1
  `generativeAskWithHostInferenceNavigatesToDetailScreen` failures
  should pass on the tolerant trust-spine assertion; the prior
  tablet_portrait
  `searchQueryShowsResultsWithoutShellPolling` should remain clear per
  the flake re-run evidence.
- If actual coverage is 44/45 because the search flake re-asserts in
  any posture, STOP - do not publish - report for planner decision.
- Summary at
  `artifacts/cp9_stage2_retrieval_chain_closed_<ts>/summary.md`
  citing the retrieval-chain commits, live APK sha, pack sha, Option 2
  provenance, and whether `matrix_homogeneous` drift is real or just
  the known missing-model-identity issue.

## Boundaries (HARD GATE)

- No code commits. No slice code edits. No APK rebuild.
- No mobile pack re-export.
- No re-dispatch of the `rain_shelter` mode-flip probe for this lane.
  The expected-null `ask.generate not emitted` verdict is already
  understood under R-host; dedicated final-route telemetry is the
  post-RC fix.
- If the gallery-finalization PowerShell mismatch or `summary.json`
  apk-sha fallthrough bugs reproduce, apply the same known workaround
  pattern and flag the recurrence for post-RC tooling cleanup.

## The work

### Step 1 - Verify matrix health

```powershell
$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe devices
# Expect 5556 / 5560 / 5554 / 5558 all `device`.
git log --oneline 585320c..HEAD
git diff 585320c..HEAD -- android-app/app/src/androidTest/
```

Per-serial `sys.boot_completed`, package presence, and installed APK sha
probes must confirm the live Option 2 substrate.

### Step 2 - Run the parallel four-lane state-pack sweep

```powershell
scripts/build_android_ui_state_pack_parallel.ps1
```

If the finalization errors the same way as prior runs, reconstruct the
top-level manifest from per-role manifests using the existing workaround
and continue.

### Step 3 - Verify coverage

- Parse the produced `summary.json` and per-role manifests.
- Confirm whether coverage is `45/45` or a bounded stop at `44/45`.
- Confirm the live debug APK sha reports as
  `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`.
- If `matrix_homogeneous` is `false`, determine whether the drift is a
  real matrix mismatch or only the known missing model-identity fields.

### Step 4 - Publish gallery only on 45/45

- Target path:
  `artifacts/external_review/ui_review_<YYYYMMDD>_retrieval_chain_closed/`.
- Use the same gallery-publish script/pattern as
  `ui_review_20260420_gallery_v6/`.
- `index.html` should link all 45 fixtures and cite commit `2e39021`,
  the live APK sha, pack sha, and the four landed retrieval-chain
  commits as provenance.

### Step 5 - Rollup

Write `artifacts/cp9_stage2_retrieval_chain_closed_<ts>/summary.md`
noting:

- HEAD commit `2e39021`.
- Landed chain: `2ec77b8` -> `0a8b260` -> `971961b` -> `585320c`,
  with doc-only D4 drift `2e39021`.
- State-pack coverage (`45/45` if clean, otherwise stop result).
- Live debug APK sha, live androidTest APK sha, pack sha, and
  matrix-homogeneous interpretation.
- Gallery path if published.
- Flake-check reference:
  `artifacts/external_review/rgal1_flakecheck_20260420_221049/`.
- Provenance note stating:
  - HEAD differs from `585320c` by one doc-only commit (`2e39021`,
    D4); no code/test/pack touched.
  - androidTest APK drifted from `b260a21...` to `ddb84d98...` via a
    Gradle rebuild on unchanged source-tree (`git diff 585320c..HEAD -- android-app/app/src/androidTest/` empty), homogeneous on all four serials.
  - Gallery evidence is valid against the R-gal1 retrieval-chain
    baseline.

## Acceptance

- State-pack sweep completes across all four serials.
- Coverage reaches `45/45`, or the lane stops and reports the bounded
  failure without publishing.
- Gallery published only if `45/45`.
- Rollup summary written.
- No code commits. No rebuilds.

## Report format

Reply with:

- State-pack coverage (`X/45` with per-posture breakdown).
- Gallery path + `index.html` URL if applicable.
- Rollup summary path.
- Any tooling workaround applied.
- Any regression surfaced - flag, do not fix in this slice.

# R-ret1b — pack-side emergency_shelter marker symmetry + substrate rebuild (post-RC)

Goal: close the classifier asymmetry V-ret1-probe exposed. `mobile_pack.py:569-576` pack-export markers don't include the phrases R-ret1 added to the Android query classifier, so guides whose content uses `rain shelter` / `tarp shelter` / etc. export as `structure_type="general"` rather than `"emergency_shelter"`. Fixing the asymmetry lets `metadataBonus +18` fire during rerank and surfaces shelter guides into `context.selected`.

**Dispatch shape:** single main-lane worker for code + pack regen + APK rebuild. Per-serial instrumentation runs fan out to 4 `gpt-5.4 high` workers (session-level subagent grant required). Two commits planned.

## Context (evidence from V-ret1-probe)

V-ret1-probe artifact at `artifacts/postrc_vret1_probe_20260420_191223/`. All 4 serials class C (`mode=uncertain_fit` via `low_coverage_route`). Key logcat findings:

1. R-ret1 classification IS working. Line 579 from the 5556 capture:
   ```
   search.start query="..." routeFocused=false routeSpecs=1 structure=emergency_shelter explicitTopics=[] limit=16
   ```
2. Root cause in `context.selected` at line 742:
   ```
   1:GD-727 :: Practical Survival Applications :: guide-focus
   2:GD-727 :: Testing Batteries Without a Multimeter :: guide-focus
   3:GD-727 :: (empty) :: guide-focus
   4:GD-687 :: 3. Burial Practices & Body Disposition :: lexical
   ```
   No shelter-family guides in top 4. `promptContext=2` cuts to only rows 1-2 (both GD-727), triggering `low_coverage_detected`.

3. `mobile_pack.py:569-576` defines `STRUCTURE_TYPE_EMERGENCY_SHELTER` markers: `{debris hut, lean-to shelter, a-frame shelter, quinzhee, wickiup, tipi, emergency shelter}`. None of R-ret1's additions are present. Guides describing tarp/rain-shelter setups don't have matching tag, so `metadataBonus +18` structure-match boost never fires on them.

4. `QueryRouteProfile.java` is structure-blind (10 matches in 1612 lines vs. 184 in QueryMetadataProfile). `routeFocused=false` on this query; route-retrieval path is orthogonal and not fixable here without a separate refactor.

Conclusion: restore symmetric query-side (R-ret1 landed) + pack-side (R-ret1b) markers. Weight tuning is premature until shelter rows actually enter the top candidate pool.

## Precondition

- HEAD at `1e9e7e3` prefix (R-hygiene1 last commit) or later with no intervening unreviewed commits.
- Android unit suite: `./gradlew.bat :app:testDebugUnitTest` passes 403/403.
- Desktop unit suite: `python3 -m unittest discover -s tests -v` passes. Record pre-edit count.
- 4-emulator matrix reachable on 5556 / 5560 / 5554 / 5558. Use `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe` explicitly; do not trust PATH. For single-serial recovery use primitive `emulator.exe -avd <name> -no-snapshot-load` — do NOT invoke `start_senku_emulator_matrix.ps1 -RestartRunning -Roles <single>` (documented trap).
- Python env active per AGENTS.md Quick Start (`venv_win` on Windows).
- Reference SHAs from prior substrate:
  - RC v5 debug APK: `551385c99a2474e97d8cbd4757d6f65423ec74e9afaeb4e9e9e5d3a3f972a204`
  - V-ret1-probe rebuilt debug APK: `d34f17546e65fd27d125c96fbaba70eb40bd31941024e8c5be98f4d3758302de`
  - Reference pack SHA: `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`
  - androidTest APK: `b260a219294bc20b5f76313aaa7415d895ab8899beee3372b039e251e9841b9e`

## Step 0 — Baseline

1. `git rev-parse HEAD` returns `1e9e7e3` prefix (or later clean commit).
2. `./gradlew.bat :app:testDebugUnitTest` → 403/403.
3. `python3 -m unittest discover -s tests -v` → record full pass count for delta comparison in Step 1b.
4. Verify all four emulators are reachable: `adb devices` shows emulator-5556/5560/5554/5558.

STOP if any baseline check fails.

## Step 1 — `mobile_pack.py` marker expansion (commit 1)

### Step 1a — edit markers

Edit `mobile_pack.py` lines 569-577. Replace:

```python
STRUCTURE_TYPE_MARKERS = (
    (STRUCTURE_TYPE_EMERGENCY_SHELTER, (
        "debris hut",
        "lean-to shelter",
        "a-frame shelter",
        "quinzhee",
        "wickiup",
        "tipi",
        "emergency shelter",
    )),
```

With:

```python
STRUCTURE_TYPE_MARKERS = (
    (STRUCTURE_TYPE_EMERGENCY_SHELTER, (
        "debris hut",
        "lean-to shelter",
        "a-frame shelter",
        "quinzhee",
        "wickiup",
        "tipi",
        "emergency shelter",
        "rain shelter",
        "rain fly",
        "tarp shelter",
        "tarp ridgeline",
        "ridgeline shelter",
    )),
```

Rationale (record in commit message):

- `rain shelter` / `rain fly` — common shelter type names that appear in guide content
- `tarp shelter` / `tarp ridgeline` / `ridgeline shelter` — tarp-based shelter phrasings; appear in guide section titles and body copy
- Deliberately EXCLUDED: `tarp and cord` / `tarp and rope` — these match query phrasings (verb+conjunction+noun), unlikely to appear verbatim in guide content; adding them would risk false positives with no matching benefit

### Step 1b — desktop-side test

Add a test to `tests/test_mobile_pack.py` following R-pack's existing pattern (`test_poisoning_guides_detect_safety_poisoning_structure_and_tags` is a good template). Assert that sample guide content containing each new phrase gets tagged `structure_type="emergency_shelter"` on export.

Minimum coverage:
- One positive per new phrase (5 positives).
- One negative: content with `"tarp"` alone but no shelter-phrase context should NOT tag as `emergency_shelter`.

Run:
```
python3 -m unittest discover -s tests -v
```

Expected: baseline pass count + 1 new test (or however many test methods the helper pattern produces). All prior tests unchanged.

STOP if any prior test fails.

### Step 1c — commit 1

```
git add mobile_pack.py tests/test_mobile_pack.py
git commit
```

Commit message body must name:
- The 5 phrase additions and per-phrase rationale.
- The 2 phrases explicitly excluded and why.
- V-ret1-probe evidence citation: artifact path + the specific context.selected line showing GD-727 dominance.
- Why this is NOT weight tuning: the asymmetry means `metadataBonus +18` never had a chance to fire; restoring symmetry is prerequisite to any weight tuning.

## Step 2 — Regenerate mobile pack (commit 2)

### Step 2a — run pack export

```
python3 scripts/export_mobile_pack.py
```

Expected output: updated content in the three tracked pack asset files:
- `android-app/app/src/main/assets/mobile_pack/senku_manifest.json`
- `android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3`
- `android-app/app/src/main/assets/mobile_pack/senku_vectors.f16`

If the export script produces additional files beyond these three, STOP and report.

### Step 2b — verify the regen

1. Compute the new pack SHA(s). Compare to the prior `e48d3e1ab068c666…`. At least `senku_mobile.sqlite3` and `senku_manifest.json` MUST differ (markers changed the structure_type assignments). `senku_vectors.f16` MAY be unchanged if the export keeps vectors stable across marker-only changes.

2. Spot-check the new SQLite via python (no sqlite3 CLI in bash):
   ```python
   import sqlite3
   c = sqlite3.connect('android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3')
   # Count chunks tagged emergency_shelter
   pre = c.execute("SELECT COUNT(*) FROM chunks WHERE structure_type='emergency_shelter'").fetchone()[0]
   # Sample distinct guides tagged emergency_shelter
   guides = c.execute("SELECT DISTINCT guide_id FROM chunks WHERE structure_type='emergency_shelter' ORDER BY guide_id").fetchall()
   print(f"emergency_shelter chunks: {pre}")
   print(f"emergency_shelter guides: {[g[0] for g in guides]}")
   ```

Expected: chunk count > 0 (ideally materially more than whatever baseline was — capture baseline via `git stash` + query + `git stash pop` if needed; OR compare against the prior pack file content pre-regen using `git show HEAD:<path>` piped through a temp-file query).

If the chunk count comes back zero or unchanged, STOP — either the markers didn't match any guide content (scope needs revision), or the export script didn't pick up the code change. Do not commit the pack.

### Step 2c — commit 2

```
git add android-app/app/src/main/assets/mobile_pack/senku_manifest.json \
        android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3 \
        android-app/app/src/main/assets/mobile_pack/senku_vectors.f16
git commit
```

Commit message body:
- New pack SHAs (per file).
- Delta: pre-regen emergency_shelter chunk count → post-regen chunk count.
- List of newly-tagged guide_ids (first 10 is fine).
- Reference R-ret1b commit 1 as the code-side change this pack materializes.

## Step 3 — APK rebuild + 4-serial re-provision

### Step 3a — rebuild APKs

```
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:assembleDebugAndroidTest
```

### Step 3b — SHA checks

1. Compute new debug APK sha256. MUST differ from `d34f17546e65fd27d125c96fbaba70eb40bd31941024e8c5be98f4d3758302de` (V-ret1-probe baseline) — the bundled pack changed so the APK must differ.
2. androidTest APK SHA may remain `b260a219…` or drift per Gradle non-determinism; not a hard gate.

If new debug APK SHA equals `d34f1754…`, STOP — pack asset change didn't propagate into the APK build.

### Step 3c — install on all 4 serials

Use SDK platform-tools adb:
```
$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe -s emulator-<serial> install -r <app-debug.apk path>
$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe -s emulator-<serial> install -r <app-debug-androidTest.apk path>
```

Repeat for 5556, 5560, 5554, 5558. Verify per-serial installed SHA matches the built SHA.

### Step 3d — write provision.json

```
artifacts/postrc_rret1b_<timestamp>/provision.json
```

Fields: `apk_sha_pre_v-probe`, `apk_sha_post_rret1b`, `androidtest_sha`, `pack_sha_pre_rret1b`, `pack_sha_post_rret1b`, `apk_sha_homogeneous`, `pack_sha_homogeneous`, per-serial installed SHAs.

## Step 4 — Per-serial bounded re-probe (4-way parallel)

Session-level subagent grant required. Fan to 4 `gpt-5.4 high` workers, one per serial (mirrors V-ret1-probe Step 2 exactly).

For each serial (5556 phone-portrait, 5560 phone-landscape, 5554 tablet-portrait, 5558 tablet-landscape):

1. Clear logcat, start capture:
   ```
   adb -s emulator-<serial> logcat -c
   adb -s emulator-<serial> logcat -v threadtime > logcat_<serial>.txt &
   ```
2. Run the single failing test via instrumentation (copy the exact host-inference args per serial from V-ret1-probe's dispatch — tablets on host-inference lane, phones on-device E4B):
   ```
   adb -s emulator-<serial> shell am instrument -w \
     -e class com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen \
     -e hostInferenceEnabled <true|false per serial> \
     -e hostInferenceUrl <if enabled> \
     -e hostInferenceModel <if enabled> \
     com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
   ```
3. Capture UI dump: `adb -s <serial> exec-out uiautomator dump /dev/tty > dump_<serial>.xml`.
4. Stop logcat.
5. Write per-serial `result.json` including:
   - Test pass/fail + assertion text.
   - Lines matching `ask\.(confident|uncertain_fit|abstain|prompt|generate)`.
   - The `context.selected` line (list of top-4 guide ids + sections).
   - The `promptContext=` count.
   - The `search` line (routeResults, lexicalHits, vectorHits).

Artifact structure:
```
artifacts/postrc_rret1b_<timestamp>/
├── provision.json
├── per_serial/
│   ├── 5556/{result.json, logcat.txt, dump.xml}
│   ├── 5560/...
│   ├── 5554/...
│   └── 5558/...
└── summary.md
```

One ANR flake-retry allowed per serial per V-ret1-probe policy. Two ANRs or a crash → STOP.

## Step 5 — Rollup + classification

Classify each serial by the same V-ret1-probe decision matrix:

| Signal | Class |
| --- | --- |
| Test PASSES + `ask.confident` | A — fully resolved |
| Test FAILS @ :2794 + `ask.confident` | B — R-gal1 Option A (assertion wording) still needed |
| Test FAILS + `ask.uncertain_fit` | C — R-ret1b insufficient; file T5 diagnostic or R-ret1c weight tune |
| Test FAILS + new error | D — regression, STOP |

Additional per-serial diagnostic signals (record in result.json and summary):

- **Top-4 context.selected shelter-guide count.** Pre-R-ret1b: 0/4. Target: ≥2/4 (ideally 3-4/4). Intermediate (1/4) suggests marker expansion helped partially but more shelter content needs to surface.
- **`promptContext=<N>`.** Pre: 2. Target: 4 (full context, meaning `low_coverage_detected` did not fire).
- **`low_coverage_detected` line presence.** Pre: present on all 4. Target: absent.

Even if test class is still C on mode, partial improvement in the diagnostic signals is informative for next move.

## Step 6 — Summary + planner handoff

Write `summary.md` with:

1. Pre/post SHAs (APK, androidTest, pack).
2. Desktop test delta (pre-edit count vs post-edit).
3. Per-serial classification (A/B/C/D) + diagnostic signal table.
4. Planner recommendation:
   - All A → close R-gal1 without code; file closure note.
   - All B → draft R-gal1 Option A (relax `PromptHarnessSmokeTest.java:2794` to accept confident terminal wording).
   - All C → evaluate diagnostic signals; if shelter rows now appear in top 4 but mode still uncertain_fit, draft R-ret1c (weight tuning with concrete ranking evidence); if zero shelter rows still, draft T5 diagnostic with in-engine top-N telemetry.
   - Mixed → serial-specific diagnosis.
   - D → T-shape diagnostic before remediation.
5. Delta vs V-ret1-probe: `context.selected` GD-727 dominance → shelter rows (count change).

## Acceptance

- Commits 1 (code) + 2 (pack asset) landed.
- Desktop unit suite green (baseline + 1 new test).
- Android unit suite 403/403 unchanged.
- New APK + pack homogeneously installed across 4 serials; provision.json complete.
- 4-serial re-probe produced per-serial result JSONs + summary.
- Planner decision documented in summary.md.

## Out of scope

- `metadataBonus` weight tuning — reserved for R-ret1c if classification succeeds but ranking still insufficient.
- `QueryRouteProfile` structure-type awareness — larger refactor; only if planner judges it necessary after R-ret1b evidence.
- R-gal1 drafting — depends on R-ret1b outcome.
- R-cls2 (tracked separately).
- `ingest.py --rebuild` desktop ChromaDB rebuild — not needed for mobile_pack-only changes.
- Any top-level untracked-state cleanup.
- Renaming or restructuring any tracked file.
- Git LFS migration for the pack assets.

## STOP conditions (explicit)

- Step 0: Android baseline not 403/403, desktop baseline error, or emulators unreachable.
- Step 1b: new test fails, or any prior test regresses.
- Step 2a: pack regen produces files beyond the three tracked assets.
- Step 2b: new emergency_shelter chunk count is zero or unchanged vs pre-regen (markers didn't match any guide content — scope reset needed).
- Step 3b: new debug APK SHA equals `d34f1754…` (bundled pack change didn't propagate).
- Step 3c: install failure or per-serial apk_sha not homogeneous.
- Step 4: instrumentation fails to start on any serial (crash/ANR before test body) — one bounded re-probe allowed per serial for ANR flake; two ANRs or any crash → STOP.
- Step 5: any class D.

In any STOP case: report state, do not attempt recovery, wait for planner guidance.

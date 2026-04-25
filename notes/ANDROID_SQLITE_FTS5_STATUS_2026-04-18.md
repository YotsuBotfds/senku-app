# Android SQLite FTS5 Status - 2026-04-18

OPUS-C-08 adds a one-off Android SQLite capability probe at [`../scripts/android_fts5_probe.ps1`](../scripts/android_fts5_probe.ps1).

## Verdict

- FTS5 is **not available** on the tested Android runtime in this workspace's fixed emulator matrix.
- Confidence is **high / authoritative for the tested matrix** (`emulator-5556`, `emulator-5560`, `emulator-5554`, `emulator-5558` on Android 16 / API 36 / `x86_64`).
- Confidence is **best-effort outside that matrix** because this pass did not run on physical devices or alternate Android system images.

## Latest Capture

- Summary artifact: [`../artifacts/android_fts5_probe/20260418_072853/summary.json`](../artifacts/android_fts5_probe/20260418_072853/summary.json)
- Per-device captures:
  - [`../artifacts/android_fts5_probe/20260418_072853/emulator-5556.json`](../artifacts/android_fts5_probe/20260418_072853/emulator-5556.json)
  - [`../artifacts/android_fts5_probe/20260418_072853/emulator-5560.json`](../artifacts/android_fts5_probe/20260418_072853/emulator-5560.json)
  - [`../artifacts/android_fts5_probe/20260418_072853/emulator-5554.json`](../artifacts/android_fts5_probe/20260418_072853/emulator-5554.json)
  - [`../artifacts/android_fts5_probe/20260418_072853/emulator-5558.json`](../artifacts/android_fts5_probe/20260418_072853/emulator-5558.json)

## What The Probe Checks

The probe is script-only. It does not edit production app code.

1. Host-pack schema proof.
   It opens `android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3` with Python `sqlite3` and confirms whether the asset pack contains `lexical_chunks_fts` and `lexical_chunks_fts4`.
2. Device SQLite module proof.
   It runs `/system/bin/sqlite3` on the device against `:memory:` and checks `sqlite_version()`, `PRAGMA module_list`, `CREATE VIRTUAL TABLE ... USING fts5`, and `CREATE VIRTUAL TABLE ... USING fts4`.
3. Installed-pack proof.
   It uses `run-as com.senku.mobile` to inspect the app's installed `files/mobile_pack/senku_mobile.sqlite3`, dumps the FTS table schemas, and executes `MATCH` queries against both `lexical_chunks_fts` and `lexical_chunks_fts4`.
4. App-runtime proof.
   It launches `com.senku.mobile/.MainActivity` with `auto_query` and captures the app's own `SenkuPackRepo` log lines, which line up with [`../android-app/app/src/main/java/com/senku/mobile/PackRepository.java`](../android-app/app/src/main/java/com/senku/mobile/PackRepository.java).

## Evidence

- The host asset pack contains both schemas:
  - `lexical_chunks_fts` created with `USING fts5(...)`
  - `lexical_chunks_fts4` created with `USING fts4(...)`
- On all four emulators, device SQLite reports `3.50.4`.
- On all four emulators, `PRAGMA module_list` includes `fts4`, `fts4aux`, `fts3`, and related modules, but **does not include `fts5`**.
- On all four emulators, `CREATE VIRTUAL TABLE fts5_probe USING fts5(body);` fails with `Error: stepping, no such module: fts5`.
- On all four emulators, `CREATE VIRTUAL TABLE fts4_probe USING fts4(body);` succeeds.
- On all four emulators, querying the installed app DB with `SELECT count(*) FROM lexical_chunks_fts WHERE lexical_chunks_fts MATCH 'water';` fails with `Error: in prepare, no such module: fts5`.
- On all four emulators, querying the installed app DB with `SELECT count(*) FROM lexical_chunks_fts4 WHERE lexical_chunks_fts4 MATCH 'water';` succeeds.
- On all four emulators, the app logs:

```text
SenkuPackRepo: fts.unavailable support5=false support4=false schema5=true schema4=true
```

## Interpretation

- The Android runtime tested here does **not** expose FTS5.
- The app is therefore correct to avoid the `lexical_chunks_fts` / `bm25(...)` path on this runtime.
- The current app detector also concludes that FTS4 is unavailable, so the app falls back past both FTS tables even though the device `sqlite3` shell can execute `fts4` successfully against the installed DB.

That last point matters:

- This probe proves "FTS5 is unavailable on the tested Android runtime" with high confidence.
- It also shows a current limitation in the app's detector: `PackRepository.detectFtsAvailabilityInternal()` relies on `PRAGMA compile_options`, and on these emulators that path reports `support4=false` even though direct shell-side `fts4` queries work.
- This note does **not** claim that app-side Java `SQLiteDatabase` can definitely use FTS4 today. It only proves there is a gap between:
  - what the device shell sqlite binary can do, and
  - what the app currently decides from `compile_options`.

## Current Limitations

- The probe is intentionally non-invasive. It does not add a Java/Kotlin runtime probe inside the app process.
- Because of that, the FTS4 finding is still a diagnostic hint, not a green light to switch the app to FTS4 without a follow-up patch and validation pass.
- The emulator matrix is carrying at least two different installed mobile-pack versions:
  - `5556` and `5558` have a newer pack generated `2026-04-17T22:59:46.353531+00:00` with SQLite SHA `782c5015...`
  - `5560` and `5554` have an older pack generated `2026-04-15T22:50:04.122110+00:00` with SQLite SHA `5b4d3dc5...`
- That pack drift changes result counts, but it does **not** change the FTS5 conclusion: both pack variants include `lexical_chunks_fts`, and both fail with `no such module: fts5` on the tested runtime.

## Practical Use

Run the probe from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\android_fts5_probe.ps1
```

Useful overrides:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\android_fts5_probe.ps1 `
  -Devices emulator-5554 `
  -LaunchQuery "water filter charcoal sand"
```

## Recommended Follow-Up

- 2026-04-18 backend follow-up landed a production runtime probe in `PackRepository.detectFtsAvailabilityInternal()` that executes a tiny in-process `MATCH` query before falling back to `LIKE`.
- Re-run this emulator note after the next Android validation pass to confirm the app now selects `lexical_chunks_fts4` on runtimes where FTS4 executes successfully in-process.
- Treat current Android lexical timing as a non-FTS5 path unless a future probe proves otherwise on the exact target runtime.
- If we want to exploit FTS4 when FTS5 is absent, the next change should be a minimal app-runtime capability probe that tests actual FTS execution instead of relying only on `PRAGMA compile_options`.

## 2026-04-18 Addendum - Post BACK-P-01 and BACK-P-04 rerun

- Re-run artifacts: [`../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_01/logcat_5556.txt`](../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_01/logcat_5556.txt) and [`../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_01/logcat_5554.txt`](../artifacts/bench/wave_a_closeout_20260418_rerun/back_p_01/logcat_5554.txt)
- Both emulators now show the new in-process runtime probe succeeding for `lexical_chunks_fts4` while `lexical_chunks_fts` still fails:
  - `fts.runtime_probe table=lexical_chunks_fts supported=false`
  - `fts.runtime_probe table=lexical_chunks_fts4 supported=true`
  - `fts.available table=lexical_chunks_fts4 ... fallback=lexical_chunks_fts4`
- The same logs then show live `MATCH` routing through `routeChunkFts` for the query `water filter charcoal sand`, which confirms the app is no longer falling through to `LIKE` on these emulators.
- Updated interpretation: FTS5 remains unavailable on the tested Android 16 emulator matrix, but after `BACK-P-01` lands the app now correctly detects and uses FTS4 in-process on `emulator-5556` and `emulator-5554`.

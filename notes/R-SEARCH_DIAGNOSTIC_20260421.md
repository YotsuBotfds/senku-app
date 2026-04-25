Date: 2026-04-21
Dispatch: `notes/dispatch/R-search_diagnostic.md`

# R-search diagnostic

## Scope

This note diagnoses the recurring
`searchQueryShowsResultsWithoutShellPolling` flake:

- `java.lang.AssertionError: results list never appeared; harness signals=busy[1]: main.search`

No code was changed. This is a read-only source, artifact, and git-history trace.

## 1. Source trace: where `main.search` is born, how results settle, and why the poll is not the primary race

### Harness side

`android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

- `SEARCH_WAIT_MS` is `10_000L` at line `78`.
- `searchQueryShowsResultsWithoutShellPolling()` launches `MainActivity`, calls `awaitHarnessIdle()`, submits the hard-coded query `"fire"`, and then calls `assertResultsSettled(scenario, SEARCH_WAIT_MS)` (`270-275`).
- `submitSearchFromResumedActivity(...)` writes the query into `R.id.search_input`, clicks `R.id.search_button`, and then waits for UI idle (`3179-3193`).
- `assertResultsSettled(...)` only asserts that `waitForResultsSettled(...)` returned true; the `HarnessTestSignals.snapshot()` string is diagnostic-only failure text (`2546-2549`).
- `waitForResultsSettled(...)` polls `R.id.results_list` and `R.id.results_header`; it requires `adapter.getItemCount() > 0` and header text that does not contain `"searching"` or `"failed"` (`4046-4069`).
- `awaitHarnessIdle()` / `waitForHarnessIdleFallback()` only wait on `HarnessTestSignals.isIdle()` (`2532-2543`).

### App side

`android-app/app/src/main/java/com/senku/mobile/MainActivity.java`

- `MainActivity` uses a shared single-thread executor: `private final ExecutorService executor = Executors.newSingleThreadExecutor();` (`79`).
- Search UI setup lives in `onCreate()`: the `RecyclerView` is created once, the adapter is created once, and the adapter is attached once (`329-334`).
- Search submission path:
  - `showSearchResults(...)` clears items, sets the header to `"Searching..."`, begins harness task `main.search`, and queues the real work onto the shared executor (`560-569`).
  - The actual retrieval call is synchronous `repo.search(...)` on that executor thread (`571-575`).
  - Success posts back through `runTrackedOnUiThread(harnessToken, () -> { ... replaceItems(results); ... resultsHeader.setText(...); ... })` (`576-592`).
  - Failure also posts back through `runTrackedOnUiThread(...)` and sets `"Search failed"` (`594-602`).
- `runTrackedOnUiThread(...)` always ends the harness token in a `finally` block after the UI runnable runs (`2218-2237`).
- `replaceItems(...)` mutates the backing `items` list in place, calls `adapter.notifyDataSetChanged()`, scrolls to position `0`, and then schedules preview-bridge refresh (`911-925`).

### Why this is not primarily a poll-surface visibility race

`android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`

- `SearchResultAdapter` holds the same `List<SearchResult>` instance that `MainActivity.replaceItems(...)` mutates (`64-65`, constructor `86-111`).
- `getItemCount()` is a direct `return results.size();` (`196-197`).

That means there is no async diffing layer, no adapter swap, and no stale snapshot object between `replaceItems(...)` and `waitForResultsSettled(...)`. If the success UI runnable had already run, `adapter.getItemCount()` should reflect the populated list immediately. The repeated failure snapshot `busy[1]: main.search` therefore points much more strongly at "search/UI completion did not finish before timeout" than at "results were present but the harness missed them."

### Search pipeline thread model

`android-app/app/src/main/java/com/senku/mobile/PackRepository.java`

- `search(String rawQuery, int limit, AnchorPriorDirective anchorPrior)` is synchronous repository work, not debounced UI work (`469-724`).
- It performs route, FTS, keyword, vector, and rerank stages on the caller thread and logs a final search summary plus slow-query tripwire lines (`493-500`, `703-724`, `5223-5260`).
- `SEARCH_WAIT_MS` stayed `10_000L` across the search-smoke introduction commit `9cf405c`, the gallery substrate commit `2e39021`, and current HEAD `1edde326`.

## 2. Artifact comparison

### Incident 1 follow-up evidence: `rgal1_flakecheck_20260420_221049` (`5554`, tablet portrait)

This folder is the authorized 3/3 rerun after the earlier gallery-sweep miss; the original failing artifact is not present here.

- Trial wall-clock timings: `21114 ms`, `22406 ms`, `23956 ms`.
- Trial instrumentation timings: `19.963 s`, `20.989 s`, `21.929 s`.
- Raw logcat exists for trials 2 and 3 only.
- Trial 2 logcat shows:
  - test start `22:11:35.866`
  - `search.start query="fire"` at `22:11:43.142`
  - final repository search summary at `22:11:48.952`
  - `totalMs=5820` and `search.slow ... totalMs=5820`
- Trial 3 logcat shows:
  - test start `22:11:59.415`
  - `search.start query="fire"` at `22:12:09.344`
  - final repository search summary at `22:12:15.494`
  - `totalMs=6184` and `search.slow ... totalMs=6184`

So on clean 5554 reruns, the repository search for `"fire"` is already a roughly `5.8-6.2 s` path before the harness does keyboard dismissal, screenshot/dump capture, and post-search assertions.

### Incident 2 follow-up evidence: `rgal1_flakecheck2_5556_20260421_062052` (`5556`, phone portrait)

This folder is also a 3/3 rerun after the earlier gallery-finalization miss; the original failing artifact is not preserved here either.

- Trial wall-clock timings: `31969 ms`, `36560 ms`, `38292 ms`.
- Trial instrumentation timings: `22.652 s`, `23.112 s`, `25.118 s`.
- All three trial summaries report `logcat_path: null`.

This confirms that the same fixture can clear repeatedly on a different serial while still taking materially longer end-to-end than the earlier 5554 reruns.

### Incident 3 primary failing artifact: state-pack `5554` tablet portrait

Primary failure summary:

- `artifacts/cp9_stage2_post_r_host_20260421_065416/state_pack/matrix_20260421_065416/summaries/tablet_portrait/searchQueryShowsResultsWithoutShellPolling/summary.json`
- run window: `2026-04-21T11:57:37.7887663Z` to `2026-04-21T11:58:17.1730681Z`
- wall-clock: `39382 ms`
- instrumentation: `30.153 s`
- status: `fail`
- failure text: `results list never appeared; harness signals=busy[1]: main.search`
- `logcat_path: null`
- no screenshot or dump artifacts were copied because the fixture never reached settled search results

The paired `instrumentation.txt` confirms this is a real `assertResultsSettled(...)` timeout, not an artifact-copy bug.

### Same-serial surrounding timing in that state-pack sweep

The 5554 failure did not happen after a long host-inference sequence on that serial. It was early in the tablet-portrait order:

- `homeEntryShowsPrimaryBrowseAndAskLanes`
  - run window: `11:57:06Z` to `11:57:36Z`
  - wall-clock: `29733 ms`
  - instrumentation: `18.929 s`
  - status: `pass`
- `searchQueryShowsResultsWithoutShellPolling`
  - run window: `11:57:37Z` to `11:58:17Z`
  - wall-clock: `39382 ms`
  - instrumentation: `30.153 s`
  - status: `fail`
- `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`
  - run window: `11:58:19Z` to `11:59:13Z`
  - wall-clock: `54710 ms`
  - instrumentation: `40.587 s`
  - status: `pass`
- `generativeAskWithHostInferenceNavigatesToDetailScreen`
  - run window: `12:00:25Z` to `12:01:09Z`
  - wall-clock: `44676 ms`
  - instrumentation: `32.396 s`
  - status: `pass`

So the R-host-era miss happened before the new host-inference test on `5554`, not after it.

### Same matrix, same fixture, other postures

The same state-pack matrix run shows this fixture is timing-sensitive rather than completely broken:

- `5558` tablet landscape: `17.233 s` wall-clock, `11.204 s` instrumentation, `pass`
- `5556` phone portrait: `42.855 s` wall-clock, `29.815 s` instrumentation, `pass`
- `5560` phone landscape: `51.686 s` wall-clock, `33.024 s` instrumentation, `pass`

The per-fixture end-to-end time varies widely even within one matrix run.

### Incident 4 follow-up evidence: `rhost_flakecheck3_5554_20260421_094902`

This was the optional rerun folder mentioned in the dispatch, now available.

- Verdict: `FLAKE`
- Trial wall-clock timings: `37549 ms`, `33518 ms`, `34297 ms`
- Trial instrumentation timings: `27.812 s`, `22.672 s`, `24.910 s`
- Status: `3/3 pass`
- HEAD: `1edde326`
- Device: `emulator-5554`
- Debug APK SHA: `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`
- androidTest APK SHA: `a0e6283b05cb1dac48e20ffb1b6eb3ecbf563347cbbb3d59851604b02a686fe1`
- Trial ledger reports `logcat_path: null` for all three reruns.

This matters for scope: the same `5554` substrate that produced the R-host-era miss later cleared `3/3` without any search-lane code change. That rules against an R-host-specific regression and strengthens the "existing intermittent + narrow time budget" read.

## 3. Diagnosis

### Verdict

Best fit: **(A) harness wait-window too tight**, with emulator/runtime variance as the secondary amplifier rather than the primary root cause.

### Root cause

`SEARCH_WAIT_MS` is only `10.0 s`, but the observed successful `"fire"` search path is already a `5.8-6.2 s` repository operation on clean 5554 reruns and is explicitly logged as `search.slow`. The failure snapshot still shows `main.search` active, which means the search or its success UI handoff had not completed before timeout; this is not a stale-busy breadcrumb and not the same class as the earlier `main.ask.prepare` handoff bug.

### Why this beats the alternatives

- **Not (B) primary harness poll race**
  - `waitForResultsSettled(...)` is polling the live adapter count and current header text.
  - `SearchResultAdapter.getItemCount()` is a direct `results.size()`.
  - `replaceItems(...)` mutates that same list on the UI thread and immediately calls `notifyDataSetChanged()`.
  - If the success runnable had already executed, the poll should see items.
- **Not (C) primary busy-token misaccounting**
  - `main.search` is created on the search path (`568`) and ended in `runTrackedOnUiThread(...)` `finally` (`2235-2236`) on both success and failure callbacks.
  - No missing-clear branch showed up in current source or git history.
  - The repeated `busy[1]: main.search` snapshot is consistent with a genuinely in-flight search, not a leaked token after results were ready.
- **Not (D) primarily test-ordering / R-host regression**
  - The R-host-era 5554 miss happened as the second tablet-portrait fixture, before the host-inference probe on that serial.
  - The new `rhost_flakecheck3_5554_20260421_094902` rerun cleared `3/3` on the same R-host baseline.
  - Ordering and emulator load still matter, but the narrow wait budget is the more direct fix target.

## 4. Recommended remediation slice

Primary remediation shape:

- file: `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Recommended approach:

Raise `SEARCH_WAIT_MS` from `10_000L` to a slightly safer value, most likely `15_000L`, and then re-run the focused search smoke on `5554` and `5556`. The evidence does not support a larger harness rewrite: the search lane is genuinely slow-but-successful on clean reruns, and the failure signature says the search is still in flight when the clock expires. This should be a very small harness-only change, on the order of one constant edit plus one validation rerun lane. If the follow-up slice wants extra observability, the next-smallest addition is richer timeout text (for example current header text and elapsed wait) rather than a poll-surface redesign.

## 5. Surprising cross-cutting finding

The raw-logcat lane is still missing exactly where it would be most useful:

- the state-pack failure summary reports `logcat_path: null`
- the 5556 rerun folder reports `logcat_path: null`
- the new `rhost_flakecheck3` rerun folder also reports `logcat_path: null`

Only the older `rgal1_flakecheck_20260420_221049` rerun retains raw repository search telemetry, and those retained logs already show `"fire"` as a slow query on clean passes. The logging gap does not block diagnosis, but it makes the timing argument less crisp than it should be for future flake triage.


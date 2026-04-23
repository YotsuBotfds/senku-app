# R-gate1 — decouple metadata rerank from `isRouteFocused()` gate (post-RC)

Goal: fix the gate at `PackRepository.java:919` so the metadata rerank (which applies `metadataBonus`) fires for any query whose `preferredStructureType` is non-empty, not only queries flagged `isRouteFocused()`. T5 evidence showed the rain-shelter probe has `structure=emergency_shelter` correctly set by R-ret1 yet takes the passthrough branch because `isRouteFocused()` returns false — so the +24 bonus GD-345 should receive never gets applied.

**Dispatch shape:** single main-lane worker, serial. One commit for the gate + unit tests; APK rebuild + single-serial re-probe is artifact-only.

## Context (T5 evidence)

From `artifacts/postrc_t5_20260420_201942/`:

- `search.start query="..." routeFocused=false routeSpecs=1 structure=emergency_shelter explicitTopics=[]` — classifier works, gate blocks.
- GD-345 enters `search.candidates.prerank` at rank 13 with score 0.015 (tagged `emergency_shelter`, category `survival` — would get metadataBonus +24).
- `search.candidates.reranked` shows all 16 rows with `base=0.000 bonus=0 final=0.000` — telemetry emitter sees passthrough placeholders because `maybeRerankResultsDetailed` returned the pre-rerank list unchanged.
- Gate location confirmed at `PackRepository.java:919`:
  ```java
  if (!queryTerms.routeProfile.isRouteFocused() || results.isEmpty()) {
      ArrayList<RerankedResult> passthrough = new ArrayList<>();
      ...
      return passthrough;
  }
  ```

`QueryRouteProfile` has no structure-type awareness (10 refs in 1612 lines). Making it structure-aware would be a larger refactor; the surgical path is to let metadata rerank fire whenever the profile's `preferredStructureType()` is non-empty, independent of route-focus.

## Precondition

- HEAD at `9aa46dd` prefix (T5 telemetry commit) or later.
- Android unit suite: `./gradlew.bat :app:testDebugUnitTest` passes 407/407 (403 prior + 4 telemetry tests from T5).
- 5556 emulator reachable (single-serial re-probe target).
- SDK platform-tools adb explicit: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

## Step 0 — Baseline

1. `git rev-parse HEAD` returns `9aa46dd` prefix or later.
2. `./gradlew.bat :app:testDebugUnitTest` → 407/407.
3. Record pre-edit SHA (APK `4e357a3e…` from T5 rebuild).
4. Verify 5556 reachable.

## Step 1 — Gate edit + unit tests (commit 1)

### Step 1a — edit the gate at `PackRepository.java:918-926`

Current (`:918-926`):
```java
List<RerankedResult> maybeRerankResultsDetailed(QueryTerms queryTerms, List<SearchResult> results, int limit) {
    if (!queryTerms.routeProfile.isRouteFocused() || results.isEmpty()) {
        ArrayList<RerankedResult> passthrough = new ArrayList<>();
        int capped = limit <= 0 ? results.size() : Math.min(limit, results.size());
        for (int index = 0; index < capped; index++) {
            passthrough.add(new RerankedResult(results.get(index), index, 0, 0.0));
        }
        return passthrough;
    }
    ...
```

New:
```java
List<RerankedResult> maybeRerankResultsDetailed(QueryTerms queryTerms, List<SearchResult> results, int limit) {
    if (results.isEmpty()) {
        return Collections.emptyList();
    }
    boolean routeFocused = queryTerms.routeProfile.isRouteFocused();
    boolean hasStructureHint = queryTerms.metadataProfile != null
        && !safe(queryTerms.metadataProfile.preferredStructureType()).trim().isEmpty();
    if (!routeFocused && !hasStructureHint) {
        ArrayList<RerankedResult> passthrough = new ArrayList<>();
        int capped = limit <= 0 ? results.size() : Math.min(limit, results.size());
        for (int index = 0; index < capped; index++) {
            passthrough.add(new RerankedResult(results.get(index), index, 0, 0.0));
        }
        return passthrough;
    }
    ...
```

Note: if `PackRepository` doesn't currently have a `safe()` helper identical to the one in `OfflineAnswerEngine`, inline `preferredStructureType()` null-guarding directly (check the `preferredStructureType()` accessor — it likely returns `""` not null, so `.isEmpty()` on the direct return may suffice).

Verify the pattern used elsewhere in `PackRepository` (e.g., line 1187 `"water_storage".equals(queryTerms.metadataProfile.preferredStructureType())` — no null guard). Follow the local convention.

Cleaner version:
```java
List<RerankedResult> maybeRerankResultsDetailed(QueryTerms queryTerms, List<SearchResult> results, int limit) {
    if (results.isEmpty()) {
        return Collections.emptyList();
    }
    boolean routeFocused = queryTerms.routeProfile.isRouteFocused();
    boolean hasStructureHint = !queryTerms.metadataProfile.preferredStructureType().isEmpty();
    if (!routeFocused && !hasStructureHint) {
        ArrayList<RerankedResult> passthrough = new ArrayList<>();
        int capped = limit <= 0 ? results.size() : Math.min(limit, results.size());
        for (int index = 0; index < capped; index++) {
            passthrough.add(new RerankedResult(results.get(index), index, 0, 0.0));
        }
        return passthrough;
    }
    ...
```

### Step 1b — unit tests

Add to `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java` (existing file from T5, already has format tests):

1. **Test: structure-only triggers rerank.** Build a fake `QueryTerms` with `isRouteFocused()=false` AND `preferredStructureType()="emergency_shelter"`. Call `maybeRerankResultsDetailed` with non-empty results. Assert the returned `RerankedResult` list has non-zero `finalScore` values (meaning rerank ran) — at minimum, that the list isn't a passthrough (no uniform zero metadataBonus across all rows when they have different structure types).

2. **Test: route-focused still triggers rerank.** Build `QueryTerms` with `isRouteFocused()=true` AND empty `preferredStructureType`. Same assertion as (1).

3. **Test: neither triggers — passthrough.** Build `QueryTerms` with both `isRouteFocused()=false` AND empty `preferredStructureType`. Assert rerank returns passthrough (all `finalScore=0.0`, `metadataBonus=0`).

4. **Test: empty results returns empty list.** Build any `QueryTerms`; pass empty results list. Assert returned list is empty.

If constructing `QueryTerms` / `QueryMetadataProfile` / `QueryRouteProfile` instances for the test is expensive, mock via package-private constructors or test helpers. Follow the existing convention in `PackRepositoryTelemetryTest` for mocking these.

If the gate logic is hard to test in isolation (requires a full `PackRepository` instance with a pack DB attached), consider an integration-style test: seed the mobile_pack test fixture (if one exists) with known rows, run a rain-shelter-style query, assert GD-345 reranks above GD-727. Alternatively, refactor the gate check into a package-private static helper that's directly unit-testable:

```java
static boolean shouldApplyMetadataRerank(QueryTerms queryTerms) {
    if (queryTerms.routeProfile.isRouteFocused()) return true;
    return !queryTerms.metadataProfile.preferredStructureType().isEmpty();
}
```

Then test this helper with synthetic QueryTerms. Cleaner and faster.

### Step 1c — run focused tests first

```
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTelemetryTest"
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTest"
```

New telemetry tests pass. PackRepositoryTest (existing) must also still pass — watch for tests that exercise non-route-focused queries with a structure hint, where the test expected passthrough ordering and now sees rerank-ordered output.

### Step 1d — full suite

```
./gradlew.bat :app:testDebugUnitTest
```

Expected: 407 prior + N_new tests green.

Any regression → triage. Likely candidates:
- **`OfflineAnswerEngineTest`** fixtures that use `QueryMetadataProfile.fromQuery(...)` with queries that now classify to a non-empty structure AND were implicitly relying on passthrough ordering. The R-ret1 landing surfaced a similar case (`SessionMemoryTest.java:759`). If a test locks on a specific top row that was previously first via raw lexical/vector score but now loses to a structure-matched row, update the assertion with a short commit-message note.
- **`PackRepositoryTest`** integration-style tests that assert specific guide_ids in specific rerank positions.

If a test shift is semantically correct (the new ordering is better: structure-matched rows now surface), update the test. If a test shift indicates a real regression (wrong rows surfacing), STOP and flag — the gate change may need additional constraints.

### Step 1e — commit

```
git add android-app/app/src/main/java/com/senku/mobile/PackRepository.java \
        android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java
```

If any `PackRepositoryTest` / `OfflineAnswerEngineTest` / other test files needed assertion updates, add those too and document in the commit message.

```
git commit
```

Commit message must name:
- The gate change: route-focused OR structure-hint → rerank fires.
- Why: T5 evidence shows rain-shelter probe classifies `emergency_shelter` but `routeFocused=false`, so +24 metadataBonus never applies and GD-345 stays at rank 13 with zero score.
- T5 artifact citation: `artifacts/postrc_t5_20260420_201942/summary.md`.
- Any existing test fixtures that were updated — named, with one-sentence rationale per.
- Out-of-scope note: context-selection gate (`anchorGuide` determination) is a downstream blocker not addressed here.

## Step 2 — APK rebuild + single-serial install

1. `./gradlew.bat :app:assembleDebug`
2. New APK SHA must differ from T5's `4e357a3e...`.
3. Install on emulator-5556 only (no fan-out needed for validation).
4. Write `artifacts/postrc_rgate1_<timestamp>/provision.json`.

STOP if SHA unchanged.

## Step 3 — Re-probe with host-enabled args

Lesson from T5: `PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen` now requires `hostInferenceEnabled=true`. Use T5's working args:

```
adb -s emulator-5556 logcat -c
adb -s emulator-5556 logcat -v threadtime > artifacts/postrc_rgate1_<ts>/logcat_5556.txt &
adb -s emulator-5556 shell am instrument -w \
  -e class com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen \
  -e hostInferenceEnabled true \
  -e hostInferenceUrl http://10.0.2.2:1236/v1 \
  -e hostInferenceModel gemma-4-e4b-it-litert \
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
```

Capture UI dump. Stop logcat.

Test may still fail on the `:2794` trust-spine assertion — that's R-gal1 territory, not R-gate1. What matters here is the retrieval telemetry.

## Step 4 — Extract evidence

Grep logcat for the telemetry added in T5:

```
grep "search.candidates.reranked" logcat_5556.txt
grep "search.topResults" logcat_5556.txt
grep "context.selected" logcat_5556.txt
grep "ask.generate" logcat_5556.txt
```

Questions to answer in `artifacts/postrc_rgate1_<ts>/summary.md`:

1. **Did rerank fire?** `search.candidates.reranked` should now show non-zero `base/bonus/final` for each row. In particular, GD-345 should have `metadata_bonus=+24` or similar (depending on category match + structure match).
2. **Did GD-345 move up?** Pre-R-gate1: rank 13 post-rerank. Post: what rank? If +24 bonus is material vs base-score gap of ~0.015 delta across rows, GD-345 should move significantly.
3. **Did topResults change?** The post-rerank `search.topResults` line should reflect any new ordering.
4. **Did context.selected change?** This is the downstream gate; if it still anchors on GD-727, that's a SEPARATE follow-on (not R-gate1 scope).
5. **Did `ask.generate mode` change?** Pre: `uncertain_fit` via `low_coverage_route`. Post: `confident` if shelter rows now reach the selected context; still `uncertain_fit` if context.selected anchors don't move.

## Step 5 — Rollup

Write `artifacts/postrc_rgate1_<ts>/summary.md` with:

1. Pre/post APK SHAs.
2. Commit SHA.
3. The reranked-stage table (with non-zero scores now).
4. Answer the 5 questions above.
5. Planner recommendation:
   - **If GD-345 reaches context.selected and mode=confident:** R-ret1 chain closes. R-gal1 still needs Option A if assertion remains red, but substantive retrieval fix landed.
   - **If GD-345 moves up in topResults but context.selected still anchors GD-727:** file R-anchor1 (or similar) targeting the context-selection anchor logic as the next blocker.
   - **If GD-345 doesn't move much post-rerank:** the +24 bonus was insufficient to overcome base gap; file R-ret1c weight tune with concrete delta evidence.
   - **If rerank fires but ranking still favors GD-727:** check whether metadataBonus for GD-727 somehow came out positive (shouldn't — it's structure `general` which should get -4 penalty).

## Acceptance

- Commit landed with gate change + new unit tests + any required test fixture updates documented.
- Full unit suite green.
- New APK installed on 5556.
- `search.candidates.reranked` telemetry shows non-zero `base/bonus/final` in logcat.
- `summary.md` records the 5-question answers and planner next move.

## Out of scope

- Context-selection anchor gate (`anchorGuide` determination). That's a downstream blocker possibly surfaced by this slice's evidence; file as follow-up if needed.
- `QueryRouteProfile` structure-type awareness — not needed with this surgical gate fix.
- `metadataBonus` weight tuning (R-ret1c reserve — only if rerank fires but still doesn't shift ranking enough).
- R-ret1b pack-marker expansion (independent track, can be dispatched separately per corpus-vocab doc).
- Pack regeneration.
- Multi-serial validation (single serial is sufficient for the rerank-gate behavior check; full matrix validation is a separate step).

## STOP conditions (explicit)

- Step 0: baseline 407 not matched.
- Step 1c: new tests fail.
- Step 1d: any existing test regresses in a way that indicates a real behavior bug (not just passthrough → rerank-ordering shift which may be semantically correct).
- Step 2: new APK SHA equals `4e357a3e…`.
- Step 3: instrumentation fails to start.
- Step 4: `search.candidates.reranked` still emits zeros (gate change didn't take effect).

In any STOP case: report state, wait for planner guidance.

## Notes

- Estimated runtime: Step 1 ~20 min (gate + 4 tests + triage existing tests), Step 2 ~5 min, Step 3 ~3 min, Step 4-5 ~10 min writeup. ~40 min total.
- This is the core blocker identified by T5. After this lands, one of three downstream slices becomes the next natural move depending on what the re-probe shows (R-anchor1 / R-ret1c / or full-matrix validation if everything resolves).

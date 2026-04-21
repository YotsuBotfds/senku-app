# R-ret1c — make metadataBonus contribute to sort key for vector-retrieved rows (post-RC)

Goal: fix the asymmetry where `metadataBonus` is baked into `supportScore` for non-vector rows (via line 3631) but bypassed for vector rows (via line 3619-3621 early return), leaving vector rows' metadata signal stored in telemetry but ignored by the rerank sort. R-gate1 enabled the rerank path for structure-classified queries; R-ret1c ensures the metadataBonus actually contributes to ranking when it fires on a vector row.

**Dispatch shape:** single main-lane worker, serial. One commit (targeted fix + unit tests). APK rebuild + single-serial re-probe to validate.

## Context (R-gate1 evidence)

From `artifacts/postrc_rgate1_20260420_203947/summary.md`:

- Reranked table shows GD-345 at rank 15 with `base=-38, bonus=38, final=0`. Other rows show base=final-bonus consistently, confirming the `baseScore = finalScore - metadataBonus` derivation.
- GD-345 enters the prerank at rank 13 as a vector-retrieved row ("Overview" section, matches vector rank 7 from T5).
- Reading `PackRepository.java`:
  - Line 3617 `supportScore(...)`: early return 0 for vector rows (line 3619-3621).
  - Line 3631: for non-vector rows, `supportScore` internally adds `metadataBonus(...)`.
  - Line 958-978 rerank loop: computes `metadataBonus` separately, computes `score = supportScore + rerankModeBonus`, passes `score` to `RerankedResult` constructor as the `finalScore` parameter.
  - Line 5861-5867 `RerankedResult` constructor: stores `finalScore = score` (pre-bonus). `baseScore = finalScore - metadataBonus` is derived for telemetry display only.
  - Line 990 sort: `Double.compare(right.finalScore, left.finalScore)` — sorts by `score + guideBonus` (from `addGuideBonus` at line 981-987).
- Net: `metadataBonus` is a display field only. It never enters the sort key unless it's already inside `supportScore` (via the internal call at line 3631 for non-vector rows).

This is a scoring-architecture asymmetry, not a weight-size problem. Bumping the structure-match constant from +18 to +60 would not help vector rows because their metadataBonus never reaches the sort regardless of size.

## Narrow fix

Add `metadataBonus` to `score` before constructing the `RerankedResult`, scoped to vector rows (so non-vector rows, which already include metadataBonus inside supportScore, aren't double-counted).

```java
int metadataBonus = metadataBonus(
    queryTerms,
    result.category,
    result.contentRole,
    result.timeHorizon,
    result.structureType,
    result.topicTags
);
int score = supportScore(queryTerms, result);
score += rerankModeBonus(result.retrievalMode);
// R-ret1c: vector rows' supportScore returns 0 before applying metadataBonus
// (PackRepository.java:3619-3621 early return). Add it here so metadata
// signal reaches the sort key for vector rows, matching the treatment
// non-vector rows already get via supportScore's internal metadataBonus call.
if (isVectorRetrievalMode(result.retrievalMode)) {
    score += metadataBonus;
}
```

Add a small helper for clarity:
```java
private static boolean isVectorRetrievalMode(String retrievalMode) {
    return "vector".equals(emptySafe(retrievalMode).trim().toLowerCase(QUERY_LOCALE));
}
```

(Use the existing `emptySafe` and `QUERY_LOCALE` helpers — same pattern as `rerankModeBonus` at line 1012.)

## Precondition

- HEAD at `f3b2c68` prefix (R-gate1 commit) or later.
- Android unit suite: `./gradlew.bat :app:testDebugUnitTest` passes 411/411.
- 5556 emulator reachable.
- Use SDK platform-tools adb: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

## Step 0 — Baseline

1. `git rev-parse HEAD` returns `f3b2c68` or later.
2. `./gradlew.bat :app:testDebugUnitTest` → 411/411.
3. Record pre-edit APK SHA (`8540e6f1...` from R-gate1).

## Step 1 — Fix + tests (commit 1)

### Step 1a — edit the rerank loop in `PackRepository.java`

Insertion point: line 956-978 `for (int index = 0; index < results.size(); index++) { ... }` inside `maybeRerankResultsDetailed`.

Add the vector-row metadataBonus inclusion right after `score += rerankModeBonus(...)` and before the `scored.add(...)` line. Exact location: after line 967, before the `String guideKey = ...` line (968).

Add the `isVectorRetrievalMode` helper near `rerankModeBonus` at line 1011.

Pattern-match the existing `emptySafe` + `.toLowerCase(QUERY_LOCALE)` style.

### Step 1b — unit tests

Add to `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java` (extending R-gate1's test file):

1. **Test: vector row receives metadataBonus in sort key.** Construct two `SearchResult` instances:
   - A vector-retrieved row with `structure_type="emergency_shelter"` and matching category, no lexical content to score.
   - A lexical-retrieved row with `structure_type="general"` and off-topic content.
   Build a `QueryTerms` with `preferredStructureType="emergency_shelter"`. Call `maybeRerankResultsDetailed`. Assert the vector row's `finalScore >= lexical row's finalScore` (it should win because its +24 metadataBonus now reaches the sort).

2. **Test: vector row's baseScore still reflects display-only derivation.** Same fixtures as (1). Assert `vectorRow.baseScore == vectorRow.finalScore - vectorRow.metadataBonus`. Telemetry semantics preserved (baseScore is still `finalScore - metadataBonus`).

3. **Test: non-vector row metadataBonus contribution unchanged (no double-count).** Construct a lexical row with a simple `structure_type` match. Call `maybeRerankResultsDetailed`. Assert `finalScore` is the same as it would be pre-R-ret1c (i.e., metadataBonus was already inside supportScore, so adding it again for vector-only path shouldn't affect this row). Without the pre-R-ret1c finalScore as a literal reference, assert the shape: `finalScore = supportScore(full) + rerankModeBonus + guideBonus`, and `supportScore(full)` matches what a fresh `supportScore(queryTerms, result)` call returns (i.e., includes metadataBonus internally).

4. **Test: empty retrievalMode row treated as non-vector.** Construct a row with `retrievalMode=""`. Assert the vector-specific branch does NOT fire. Defensive coverage against unexpected null/empty values.

If constructing the fixtures requires a full `PackRepository` + pack DB, the existing R-gate1 test (`shouldApplyMetadataRerank` helper test) may already show how to mock. Follow that pattern. If the rerank loop isn't testable without a pack DB, refactor the loop's body into a package-private static helper that operates on (`queryTerms`, `results`, `limit`) and returns the `scored` list — same pattern as `shouldApplyMetadataRerank`. Test the helper directly.

### Step 1c — run focused tests

```
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTelemetryTest"
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTest"
```

Watch for regressions. Likely candidates:

- Any `PackRepositoryTest` that asserts a specific rerank ordering where vector rows were previously ignored. Triage each: if the new ordering is semantically correct (vector row with strong structure match now surfacing), update the assertion with rationale in the commit message.
- Similar to R-ret1 (SessionMemoryTest), R-gate1 (none hit), this is the "scope expansion via test-grep" precedent.

### Step 1d — full suite

```
./gradlew.bat :app:testDebugUnitTest
```

Target: 411 prior + N_new tests green. Updated fixture/assertion deltas called out in commit message.

### Step 1e — commit

```
git add android-app/app/src/main/java/com/senku/mobile/PackRepository.java \
        android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java
# plus any test files whose assertions shifted
git commit
```

Commit message body must name:

- The asymmetry: `supportScore` returns 0 for vector rows (line 3619-3621 early return) so their metadataBonus never reaches the sort key, while non-vector rows get metadataBonus inside supportScore via line 3631.
- The fix: add metadataBonus to score inside the rerank loop for vector rows specifically, preserving the treatment non-vector rows already get indirectly.
- R-gate1 evidence citation: `artifacts/postrc_rgate1_20260420_203947/summary.md` — GD-345 vector row at rank 15 with final=0 despite bonus=+38.
- Any updated test fixtures: named + one-sentence rationale per.
- Out-of-scope note: this is a targeted symmetry fix. A larger refactor that removes `metadataBonus` from `supportScore` entirely and applies it uniformly outside would be cleaner architecturally; deferred.

## Step 2 — APK rebuild + single-serial install

1. `./gradlew.bat :app:assembleDebug`.
2. New APK SHA must differ from `8540e6f1…`.
3. Install on emulator-5556 only.
4. Write `artifacts/postrc_rret1c_<timestamp>/provision.json`.

STOP if SHA unchanged or install fails.

## Step 3 — Re-probe with host-enabled args

Use T5's working args:

```
adb -s emulator-5556 logcat -c
adb -s emulator-5556 logcat -v threadtime > artifacts/postrc_rret1c_<ts>/logcat_5556.txt &
adb -s emulator-5556 shell am instrument -w \
  -e class com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen \
  -e hostInferenceEnabled true \
  -e hostInferenceUrl http://10.0.2.2:1236/v1 \
  -e hostInferenceModel gemma-4-e4b-it-litert \
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
```

Capture UI dump. Stop logcat.

Known: the test will likely still fail at `PromptHarnessSmokeTest.java:2794` (R-gal1 territory). Out of scope for R-ret1c.

## Step 4 — Extract evidence

Grep logcat for the telemetry from T5 + R-gate1:

```
grep "search.candidates.reranked" logcat_5556.txt
grep "search.topResults" logcat_5556.txt
grep "context.selected" logcat_5556.txt
grep "ask.generate" logcat_5556.txt
```

Questions for `artifacts/postrc_rret1c_<ts>/summary.md`:

1. **Did GD-345's final score increase?** Pre-R-ret1c: final=0. Expected post: final≈metadataBonus (38) or higher.
2. **Did GD-345 move up in reranked ordering?** Pre: rank 15. Expected: top 5 or better (it was getting beaten by rows with supportScore 20-27; with +38 added to GD-345's side, it should surface).
3. **Did `search.topResults` change?** Pre-R-ret1c: GD-353, GD-687, GD-294, GD-353, GD-027, GD-031 (summarized). Expected: GD-345 should now appear in the visible top set.
4. **Did `context.selected` change?** Pre: 3× GD-727 + 1× GD-687. Expected: at least one shelter-family row (GD-345 or GD-294) in the top 4 selected context.
5. **Did `ask.generate mode` change?** Pre: `uncertain_fit` via `low_coverage_route`. Expected: `confident` if shelter content reaches the selected context.

## Step 5 — Rollup

Write `artifacts/postrc_rret1c_<ts>/summary.md` with:

1. Pre/post APK SHAs.
2. Commit SHA.
3. Reranked-stage table. Highlight GD-345's row: old vs new (base, bonus, final).
4. Answer the 5 questions above.
5. Planner recommendation:
   - **If GD-345 reaches context.selected AND mode flips to confident:** R-ret1 chain substantively closed. R-gal1 becomes the remaining blocker for the state-pack test assertion.
   - **If GD-345 moves up in topResults but context.selected still anchors elsewhere:** file R-anchor1 targeting `anchorGuide` / context-selection logic.
   - **If GD-345 doesn't move far enough up:** consider a structure-match weight bump as a follow-on (R-ret1d), with ranking-delta evidence to tune from.
   - **If GD-345 moves up but a different off-route guide now dominates:** the metadata-match weights need per-bucket re-balance (R-ret1d).

## Acceptance

- Commit landed with fix + 4 new tests + any shifted test assertions documented.
- Android unit suite green.
- New APK installed on 5556.
- `search.candidates.reranked` telemetry shows GD-345 with non-zero positive `finalScore` matching its `metadataBonus`.
- `summary.md` records the 5-question answers and recommendation.

## Out of scope

- Larger refactor of `supportScore` to move `metadataBonus` OUT of support and apply it uniformly outside. Cleaner architecturally, wider test blast radius. File as follow-up if this narrow fix behaves well.
- Context-selection `anchorGuide` gate — still open from R-gate1 summary; may need a downstream R-anchor1 slice depending on what R-ret1c surfaces.
- `metadataBonus` weight tuning. Premature until we confirm the symmetry fix is sufficient.
- R-gal1 drafting (trust-spine assertion). Separate slice after retrieval chain settles.
- R-ret1b follow-up revision with corpus-vetted markers. Independent; can dispatch parallel.
- Multi-serial validation. Single serial 5556 is sufficient to validate the rerank ordering change.

## STOP conditions (explicit)

- Step 0: baseline 411 not matched.
- Step 1c: new tests fail.
- Step 1d: any existing test regresses in a way that isn't a correct semantic shift (vector rows now surfacing above non-vector rows is expected; flag only if non-vector row orderings shift unexpectedly).
- Step 2: new APK SHA equals `8540e6f1…`.
- Step 3: instrumentation fails to start.
- Step 4: GD-345's reranked finalScore is still 0 (fix didn't take effect — check that the `isVectorRetrievalMode` branch actually fires by looking for a log that the row's retrievalMode was detected).

In any STOP case: report state, wait for planner guidance.

## Notes

- Estimated runtime: Step 1 ~25 min (small code change + 4 tests + triage), Step 2 ~5 min, Step 3 ~3 min, Step 4-5 ~10 min. ~45 min total.
- If during Step 1 the fix looks more invasive than the planner description (e.g., `supportScore` has other vector-specific paths that also need changes), STOP and report before expanding scope.

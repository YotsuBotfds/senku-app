# R-anchor1 — include vector rows in anchor-selection scoring (post-RC)

Goal: fix the asymmetry where `supportScore` returns 0 for vector rows
(line 3626-3629 early return), causing `buildAnchorGuideScores` to drop
them at line 2958 (`if (support <= 0) continue;`), so metadata-matched
vector rows never compete for anchor selection. R-ret1c restored
symmetry in the rerank loop; R-anchor1 extends the same pattern to the
anchor-selection loop.

**Dispatch shape:** single main-lane worker, serial. One commit
(targeted fix + unit tests). APK rebuild + single-serial re-probe to
validate that `anchorGuide` now picks shelter content.

## Context (R-ret1c evidence)

From `artifacts/postrc_rret1c_20260420_205801/summary.md`:

- Reranked top-4 post-R-ret1c:
  1. GD-345 | Overview | `finalScore=50` | `retrievalMode=vector`
  2. GD-727 | Practical Survival Applications | `finalScore=37` | `retrievalMode=lexical`
  3. GD-687 | 3. Burial Practices | `finalScore=36` | `retrievalMode=lexical`
  4. GD-294 | Seasonal Climate Management | `finalScore=31` | `retrievalMode=vector`
- Context-selection log: `anchorGuide="GD-727" diversify=false guideSections=4 finalContext=4`.
- Despite GD-345 being reranked rank 1, anchor picks GD-727.

Trace in `PackRepository.java`:

- Line 2396 `selectAnswerAnchor(queryTerms, rankedResults)` called with post-rerank results.
- Line 2402 builds guide scores via `buildAnchorGuideScores`.
- Line 2957 `int support = supportScore(queryTerms, result);`
- Line 2958 `if (support <= 0) { continue; }` — GD-345 (vector) dropped here.
- Root cause: `supportScore` at line 3626-3629 early-returns 0 for vector rows, same gate R-ret1c worked around in the rerank loop.
- `isVectorRetrievalMode` helper already exists at line 1016 (introduced by R-ret1c).

Full forward research: `notes/R-ANCHOR1_FORWARD_RESEARCH_20260420.md`.

## Narrow fix

In `buildAnchorGuideScores` at line 2946-2992, when `supportScore` returns 0 for a vector row, compute `metadataBonus` directly so the row can enter the guide-score map. Mirrors R-ret1c's rerank-loop pattern.

```java
int support = supportScore(queryTerms, result);
if (support <= 0 && isVectorRetrievalMode(result.retrievalMode)) {
    // R-anchor1: vector rows' supportScore early-returns 0 at line 3628-3629.
    // Mirror the rerank-loop treatment (R-ret1c) so metadata-matched vector
    // rows can compete for anchor selection alongside lexical rows.
    support = metadataBonus(
        queryTerms,
        result.category,
        result.contentRole,
        result.timeHorizon,
        result.structureType,
        result.topicTags
    );
}
if (support <= 0) {
    continue;
}
```

Reuses the `isVectorRetrievalMode` and `metadataBonus` helpers that already exist in `PackRepository.java`. No signature changes.

## Precondition

- HEAD at `2ec77b8` prefix (R-ret1c commit) or later.
- Android unit suite: `./gradlew.bat :app:testDebugUnitTest` passes 415/415.
- 5556 emulator reachable.
- Use SDK platform-tools adb: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

## Step 0 — Baseline

1. `git rev-parse HEAD` returns `2ec77b8` or later.
2. `./gradlew.bat :app:testDebugUnitTest` → 415/415.
3. Record pre-edit APK SHA (`9bdc7b5ffd19...` from R-ret1c).

STOP if baseline isn't green.

## Step 1 — Fix + tests (commit 1)

### Step 1a — edit `buildAnchorGuideScores` in `PackRepository.java`

Insertion point: inside `buildAnchorGuideScores` (line 2946-2992), specifically between current line 2957 (`int support = supportScore(queryTerms, result);`) and line 2958 (`if (support <= 0) { continue; }`).

Replace the three lines:

```java
            int support = supportScore(queryTerms, result);
            if (support <= 0) {
                continue;
            }
```

With:

```java
            int support = supportScore(queryTerms, result);
            if (support <= 0 && isVectorRetrievalMode(result.retrievalMode)) {
                // R-anchor1: vector rows' supportScore early-returns 0 at line 3628-3629.
                // Mirror the rerank-loop treatment (R-ret1c) so metadata-matched vector
                // rows can compete for anchor selection alongside lexical rows.
                support = metadataBonus(
                    queryTerms,
                    result.category,
                    result.contentRole,
                    result.timeHorizon,
                    result.structureType,
                    result.topicTags
                );
            }
            if (support <= 0) {
                continue;
            }
```

No new helper methods required. `isVectorRetrievalMode` at line 1016 and `metadataBonus` (invoked at rerank loop line 958 and inside `supportScore` line 3640) both already exist.

### Step 1b — unit tests

Add to `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java` near the existing `anchorGuideBudgetForTest` coverage (line 2372). If `buildAnchorGuideScores` is not directly testable without a pack DB, refactor the loop body into a package-private static helper `buildAnchorGuideScoresForTest(queryTerms, rankedResults, requireDirectSignal)` returning the `LinkedHashMap<String, GuideScore>`, mirroring the `shouldApplyMetadataRerank` / `anchorGuideBudgetForTest` exposure pattern. Then test the helper directly.

Minimum coverage (4 tests):

1. **Test: vector row with positive metadataBonus becomes anchor-eligible.**
   Construct a vector-retrieved `SearchResult` with `structureType="emergency_shelter"` and a lexical-retrieved `SearchResult` with `structureType="general"` off-topic. Build `QueryTerms` whose `metadataProfile.preferredStructureType() == "emergency_shelter"`. Assert the returned map contains an entry for the vector row's guide and the entry's `totalScore > 0`.

2. **Test: vector row with zero metadataBonus still drops.** Construct a vector row with no structure/category/topic match for the query. Assert the returned map does NOT contain that row's guide (unless it also has positive support from the lexical path, which it shouldn't for a vector-only off-topic row).

3. **Test: lexical row anchor-scoring unchanged (no double-count).** Construct a lexical row whose `supportScore` already includes `metadataBonus` internally (line 3640). Assert the entry in the returned map has `totalScore == expected_support + index_bonus + anchor_alignment + retrieval_mode_bonus` — i.e., the vector-only branch did NOT fire for the lexical row.

4. **Test: empty retrievalMode row treated as non-vector.** Construct a row with `retrievalMode=""`. Assert the vector-only branch does NOT fire. If `supportScore` returns 0 for the empty-mode row, it still drops at the second guard — defensive coverage against unexpected values.

If any existing `PackRepositoryTest` asserts on anchor identity for a vector-heavy query (search for `anchorGuide` or guide-focused assertions across `PackRepositoryTest.java`), triage each: if the new anchor is the semantically-correct guide (vector row with strong structure match now surfacing), update the assertion with rationale in the commit message.

### Step 1c — run focused tests

```
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTest"
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTelemetryTest"
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.OfflineAnswerEngineTest"
```

Watch for regressions. Most sensitive: any existing assertion on `context.selected` ordering or anchor identity. R-ret1c's precedent (415/415 with only its own 4 new tests adding) suggests blast radius is narrow.

### Step 1d — full suite

```
./gradlew.bat :app:testDebugUnitTest
```

Target: 415 prior + N_new tests green. Any updated fixture/assertion deltas called out in commit message.

### Step 1e — commit

```
git add android-app/app/src/main/java/com/senku/mobile/PackRepository.java \
        android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java
# plus any test files whose assertions shifted
git commit
```

Commit message body must name:

- The asymmetry: `supportScore` early-returns 0 for vector rows (line 3628-3629), `buildAnchorGuideScores` drops those rows at line 2958, so metadata-matched vector rows never compete for anchor selection.
- The fix: in the anchor loop only, compute `metadataBonus` directly for vector rows whose supportScore returned 0 — mirrors R-ret1c's rerank-loop treatment.
- R-ret1c evidence citation: `artifacts/postrc_rret1c_20260420_205801/summary.md` — GD-345 at reranked rank 1 / finalScore=50 yet `anchorGuide="GD-727"`.
- Any updated test fixtures: named + one-sentence rationale per.
- Out-of-scope: wider `supportScore` refactor that removes the vector early-return entirely. Cleaner architecturally but larger blast radius; deferred.

## Step 2 — APK rebuild + single-serial install

1. `./gradlew.bat :app:assembleDebug`.
2. New APK SHA must differ from `9bdc7b5ffd19...`.
3. Install on emulator-5556 only.
4. Write `artifacts/postrc_ranchor1_<timestamp>/provision.json` with `pre_edit_apk_sha256`, `post_edit_apk_sha256`, `serial`, `commit`.

STOP if SHA unchanged or install fails.

## Step 3 — Re-probe with host-enabled args

Use T5's working args (same as R-ret1c Step 3):

```
adb -s emulator-5556 logcat -c
adb -s emulator-5556 logcat -v threadtime > artifacts/postrc_ranchor1_<ts>/logcat_5556.txt &
adb -s emulator-5556 shell am instrument -w \
  -e class com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen \
  -e hostInferenceEnabled true \
  -e hostInferenceUrl http://10.0.2.2:1236/v1 \
  -e hostInferenceModel gemma-4-e4b-it-litert \
  com.senku.mobile.test/androidxstudio.runner.AndroidJUnitRunner
```

(Adjust runner name to match the androidTest APK — see R-ret1c's actual command for the correct runner fully-qualified name.)

Capture UI dump. Stop logcat.

Known: the instrumentation test is likely to still fail at `PromptHarnessSmokeTest.java:1101` (`busy[1]: main.ask.prepare`) — that's the R-ret1c-era host-precondition quirk, not an R-anchor1 regression. The evidence we need comes from the logcat telemetry, not the test verdict.

## Step 4 — Extract evidence

Grep logcat for:

```
grep "search.candidates.reranked" logcat_5556.txt
grep "search.topResults" logcat_5556.txt
grep "context.selected" logcat_5556.txt
grep "context query=" logcat_5556.txt   # the anchorGuide= line
grep "ask.generate" logcat_5556.txt
```

Questions for `artifacts/postrc_ranchor1_<ts>/summary.md`:

1. **Did `anchorGuide` change?** Pre: `anchorGuide="GD-727"`. Expected post: `anchorGuide="GD-345"` or `anchorGuide="GD-294"` (a shelter-family guide).
2. **Did `context.selected` change?** Pre: 3× GD-727 + 1× GD-687. Expected: shelter content dominates the 4 selected rows.
3. **Did `guideSections` count change?** Pre: `guideSections=4`. May go up or down depending on new anchor's section distribution.
4. **Did `ask.generate mode` change?** Pre: `uncertain_fit` via `low_coverage_route`. Expected: may flip to `confident` if selected-context is now on-topic shelter content. (If `ask.generate` isn't emitted due to the prepare-step stall, read telemetry from `search.topResults` and `context.selected` alone — those fire earlier.)
5. **Did reranked ordering shift?** Expect identical reranked ordering to R-ret1c; this slice only affects anchor selection, not rerank.

## Step 5 — Rollup

Write `artifacts/postrc_ranchor1_<ts>/summary.md` with:

1. Pre/post APK SHAs.
2. Commit SHA.
3. Answer the 5 questions above with logcat evidence quotes.
4. Planner recommendation:
   - **If `anchorGuide` flips to shelter AND mode becomes confident:** the retrieval chain substantively closes. R-gal1 (trust-spine assertion) becomes the remaining open surface. Rain-shelter may self-resolve at state-pack.
   - **If `anchorGuide` flips but `context.selected` still pulls GD-727 rows:** investigate the diversify=false path at `PackRepository.java` context-building. File R-anchor2 targeting the post-anchor context assembly.
   - **If `anchorGuide` doesn't flip:** check whether `shouldRequireDirectAnchorSignal` (line 2998) fires for this query and routes through the `requireDirectSignal=true` branch. If so, GD-345 may pass the loop but fail `hasDirectAnchorSignal`. File as follow-on.
   - **If the change regresses another query's anchor (watch the broader 5556 logcat for violin-bridge or poisoning fixtures firing):** the vector-eligibility fix over-corrected. File R-anchor1-revise with a narrower predicate.

## Acceptance

- Commit landed with fix + 4 new tests + any shifted test assertions documented.
- Android unit suite green.
- New APK installed on 5556.
- `context.selected` telemetry shows at least one shelter-family guide (GD-345 / GD-294 / GD-446) in the 4 selected rows.
- `summary.md` records the 5-question answers and planner recommendation.

## Out of scope

- Larger refactor to remove vector early-return from `supportScore` entirely and let it compute lexical=0 naturally. Cleaner but wider blast radius. File as `R-anchor-refactor1` if the narrow fix shows weight-tuning problems.
- Threading `RerankedResult.finalScore` into `selectAnswerAnchor` (Option B from forward research). Deferred in favor of the narrower Option A.
- `hasDirectAnchorSignal` predicate review. Depends on whether Step 4 evidence shows the stricter-signal branch firing for rain-shelter.
- R-gal1 drafting (trust-spine assertion). Separate slice after retrieval chain settles.
- Multi-serial validation. Single serial 5556 is sufficient to validate the anchor-selection change.
- Unit-testing the refactor path if `buildAnchorGuideScores` already testable via fixtures that don't require a pack DB.

## STOP conditions (explicit)

- Step 0: baseline 415 not matched.
- Step 1c: new tests fail.
- Step 1d: any existing test regresses in a way that isn't a correct semantic shift (vector rows now anchor-eligible is expected; flag only if lexical-row anchor behavior changes unexpectedly).
- Step 2: new APK SHA equals `9bdc7b5ffd19...`.
- Step 3: instrumentation fails to start (distinct from the known `busy[1]: main.ask.prepare` stall, which does NOT block evidence capture).
- Step 4: `anchorGuide` in logcat still equals `"GD-727"` despite the fix (means `buildAnchorGuideScores` branch didn't fire; verify `isVectorRetrievalMode(result.retrievalMode)` is returning true for GD-345's row).

In any STOP case: report state, wait for planner guidance.

## Notes

- Estimated runtime: Step 1 ~25 min (small change + 4 tests + triage), Step 2 ~5 min, Step 3 ~3 min, Step 4-5 ~10 min. ~45 min total.
- If during Step 1 the fix looks more invasive than described (e.g., tests show the stricter-signal branch fires and blocks vector anchors anyway), STOP and report — the `hasDirectAnchorSignal` path may need a coupled fix that expands the slice scope.
- The instrumentation test at `PromptHarnessSmokeTest.java:1101` is expected to still fail. Do NOT treat that failure as an R-anchor1 regression; the evidence we need (anchorGuide, context.selected) fires before the prepare stall.

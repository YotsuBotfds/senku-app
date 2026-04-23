# Slice R-anchor-refactor1 — `PackRepository` support breakdown + uniform metadata application

- **Role:** main agent (`gpt-5.4 xhigh`). Architectural refactor across one production file + three test files; Step 3 `gradlew.bat :app:testDebugUnitTest` run stays on the main lane. Step 2 per-boundary migration can fan out to `gpt-5.4 high` workers if main wants parallelism (each boundary is a narrow code surface; see Delegation hints).
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** Any non-`PackRepository` / non-`OfflineAnswerEngine` slice. NOT parallel with anything else touching Android retrieval scoring — the migration set is broad within `PackRepository` and merge conflicts will bite.
- **Predecessor context:** Forward research at `notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_20260421.md` returned **partial** on the original "metadataBonus lives inside supportScore at every scoring boundary" thesis. The refined, code-grounded thesis:

  > Inside `PackRepository`, replace opaque `supportScore(...)` consumption with a shared support breakdown so lexical support and metadata support are computed separately, vector rows contribute lexical `0` rather than total support `0`, and every support-boundary applies metadata explicitly and exactly once.

  `R-ret1c` (`2ec77b8`) and `R-anchor1` (`971961b`) are the two prior slices that already patched individual boundaries in this family. `R-gate1` (`f3b2c68`) was an adjacent rerank-entry gate fix, not the same shape. `R-gal1` (`585320c`) was a trust-spine test tolerance change, unrelated to this refactor. The key location correction from research §2.1: `supportScore(...)` lives in `PackRepository.java:3651-3680`, NOT in `OfflineAnswerEngine`.

## Pre-state (verified by planner via research §2.5 + scout audit)

Nine boundaries / **ten** `supportScore(...)` call sites in `PackRepository.java`. Boundary 2.3 has TWO call sites (ranked + routed at `:2466-2467`); the others are one each. Current status:

| Boundary | File:line | Call sites | Status pre-slice |
| --- | --- | --- | --- |
| `buildGuideAnswerContext(...)` support candidates | `:769-797` | 1 | **Unpatched** — vector rows drop at `support <= 0` guard |
| `maybeRerankResultsDetailed(...)` rerank sort | `:945-1003` | 1 | Patched by `R-ret1c` (vector-only metadata reinjection at `:968-972`) |
| Ranked-vs-routed anchor tiebreak | `:2444-2468` | **2** (ranked at `:2466`, routed at `:2467`) | **Unpatched** — both sides flatten vector via `Math.max(1, ...)` |
| `selectExplicitWaterDistributionAnchor(...)` | `:2522-2564` | 1 | **Unpatched** |
| `selectExplicitWaterStorageAnchor(...)` | `:2567-2681` | 1 | **Unpatched** |
| `selectSpecializedStructuredAnchor(...)` | `:2684-2730` | 1 | **Unpatched** |
| `selectBroadHouseAnchor(...)` | `:2733-2779` | 1 | **Unpatched** |
| `buildAnchorGuideScores(...)` main guide-score map | `:2954-3012` | 1 | Patched by `R-anchor1` (vector-only metadata reinjection at `:2966-2977`) |
| `findRouteFocusedAnchor(...)` | `:3407-3440` | 1 | **Unpatched** |

Other topology facts from research §2 that constrain the refactor:
- `QueryMetadataProfile.metadataBonus(...)` at `QueryMetadataProfile.java:762-835` is the bonus in scope. NOT `QueryRouteProfile.metadataBonus(...)` at `QueryRouteProfile.java:1173-1215` (same method name, different family).
- Route-row scoring (`PackRepository.java:1885-1889`, `2236-2240`) and keyword-hit scoring (`:3821-3823`) already compose metadata explicitly. Out of scope.
- Hybrid RRF at `:3989-4037` is a different scoring family. Out of scope.
- `OfflineAnswerEngine.rerankWithSessionHints(...)` at `OfflineAnswerEngine.java:849-926` is intentionally different (metadata weight `2x`). Out of scope.

**Planner decisions locking the open blockers (revised post-scout audit):**

1. **Scope covers ALL 10 call sites across 9 boundaries.** A narrower slice leaves the codebase in a mixed "sometimes metadata is fused, sometimes explicit" state and the architectural goal fails. `buildGuideAnswerContext(...)` is included. The ranked-vs-routed tiebreak migrates BOTH `rankedScore` and `routedScore` calls.

2. **Vector contract: full shared breakdown (Option B from scout audit), NOT metadata-only compensation.** This is the architectural fix the slice exists to do. Consequences locked in:
   - Vector rows in rerank/anchor scoring now receive `lexicalSupport=0` BUT also `specializedTopicBonus + sectionHeadingBonus + structurePenalty` on top of `metadataBonus`. Previously those three terms were zeroed (because `supportScore(...)` returned 0 before they applied) and only metadata was added back via the two compensation branches.
   - This IS a behavior change beyond the prior compensation paths. A vector row matching an explicit topic, or sitting in a preferred section heading, or carrying a structure penalty, will now score differently from today.
   - Consequence is desired: that's exactly what makes vector support first-class. Metadata-only compensation (Option A) would just rename the helper without fixing the asymmetry.

3. **`RerankedResult.baseScore` / `finalScore` derivation contract:** preserved as `baseScore = finalScore - metadataBonus`, with `addGuideBonus(...)` continuing to re-derive `baseScore` after each guide-bonus add. The CONSTRUCTOR signature stays: `RerankedResult(SearchResult, int originalIndex, int metadataBonus, double finalScore)`. What changes:
   - `finalScore` at construction = `support.supportWithMetadata() + rerankModeBonus(retrievalMode)` for both vector and non-vector rows (was: `supportScore + rerankModeBonus + (vector? metadataBonus : 0)`).
   - `metadataBonus` argument = `support.metadataBonus` (unchanged source).
   - For non-vector rows: numbers identical (mathematical identity).
   - For vector rows: `finalScore` increases by `support.specializedTopicBonus + support.sectionBonus + support.structurePenalty` compared to today's behavior, because those terms now apply. `baseScore = finalScore - metadataBonus` stays the consistent identity.
   - **Existing rerank telemetry tests at `PackRepositoryTelemetryTest.java:203-229` will likely need updates for vector-row fixtures** — verify each affected test, document the new expected number with the explanatory delta in the commit body. Do NOT change the derivation invariant.

4. **Chooser coverage strategy: one targeted test per chooser** (not table-driven). Each chooser has unique scoring logic (role bias, mode bias, anchor alignment, broad-route section preference); targeted tests keep failure modes readable.

5. **Engine regression test is a PATH-PROXY, not a full-path integration test**: the `OfflineAnswerEngineTest` regression starts from raw ranked search results and exercises the SCORING portion of `buildGuideAnswerContext(...)` via the `rankSupportCandidatesForTest(...)` seam, then calls `resolveAnswerMode(...)` (static, existing pattern) on the resulting selected context. It does NOT cover the DB-backed section-loading step of `buildGuideAnswerContext(...)` — that's out of scope for the JUnit-only unit lane (see Step 3 engine-test body for the full constraint explanation). Most existing engine tests inject `selectedContext` directly, so they don't exercise the changed selection path at all; the new test fills that scoring-path gap. DB-backed end-to-end coverage, if desired, is a follow-up androidTest slice.

## Scope

Production change in `PackRepository.java` introducing a shared `SupportBreakdown` helper and migrating all **ten** `supportScore(...)` call sites across nine boundaries (boundary 2.3 has two adjacent sites) to consume it. Three test files gain new invariant tests plus selective updates to existing tests whose fixtures shift when vector support becomes first-class. One commit. In-slice tracker updates (per the new cadence) in `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md`.

- **Commit** — production refactor + test additions + tracker updates in one atomic landing. Desktop unit suite NOT re-run (Java-side change; Android unit suite is the gate). Android unit suite must pass at baseline+new tests.

**NO Android UI change. NO APK dispatch to serials. NO emulator validation in this slice — that's a follow-up gallery republish if planner wants on-device confirmation.** Pack assets are not touched.

**In-slice tracker update pattern (R-tool2 cadence)**: landing slice includes `CP9_ACTIVE_QUEUE.md` (Last-updated / Post-RC Tracked / Completed log) and `dispatch/README.md` (Landed-not-rotated) in the same commit. Slice-file rotation batched via D-series.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `38b7826` (R-tool2) or later. Target files: `PackRepository.java`, `PackRepositoryTest.java`, `PackRepositoryTelemetryTest.java`, `OfflineAnswerEngineTest.java`, `QueryMetadataProfile.java`. Verify none have been touched since `38b7826` by running:
   ```bash
   git log --oneline 38b7826..HEAD -- \
     android-app/app/src/main/java/com/senku/mobile/PackRepository.java \
     android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java \
     android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java \
     android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java \
     android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java
   ```
   Expected: empty output. If any commits appear, STOP and report (merge-conflict prevention). Historical anchor for reference: `6f9e07b` (R-ret1b Commit 2) was the last commit to actually touch any of these files as of slice-draft time (2026-04-21 night verification by planner via the same command).
2. Line anchors in `PackRepository.java` — verify each of the nine boundaries from Pre-state is within ±20 lines of its cited range. Also verify:
   - `supportScore(...)` definition at `:3651-3680`.
   - `QueryMetadataProfile.metadataBonus(...)` definition at `QueryMetadataProfile.java:762-835` (read-only confirmation).
   - `RerankedResult` class definition for `baseScore` / `finalScore` / `metadataBonus` fields (research names `:5892-5910` for its derivation; confirm line range within ±30).
   If any anchor is off by more than the named tolerance, STOP and report.
3. Android unit suite passes at baseline: `cd android-app && ./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTest*" --tests "com.senku.mobile.PackRepositoryTelemetryTest*" --tests "com.senku.mobile.OfflineAnswerEngineTest*" --tests "com.senku.mobile.QueryMetadataProfileTest*"` — record the baseline pass count. This is the focused gate for this slice; full suite run happens in Step 3 as acceptance.
4. The `gradlew.bat` and Android SDK toolchain are functional locally (verified if R-tool2 or prior slice has recently run the android-test lane).
5. Pre-edit behavioral probe (optional but recommended): run `./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTelemetryTest.rerankVectorRowsKeepMetadataBonus*"` and confirm it passes. This is the test locking the R-ret1c compensation branch that the refactor obsoletes — it must still pass AFTER the refactor because the invariant (vector rows get metadata credit) is preserved, just via a different mechanism.

If any precondition fails, STOP and report before touching code.

## Outcome

- `PackRepository.java`:
  - New **package-private** `static final` nested class `SupportBreakdown` (matches existing `GuideScore` / `RerankedResult` visibility pattern at `:5880` / `:5892`) with fields `lexicalSupport`, `metadataBonus`, `specializedTopicBonus`, `sectionBonus`, `structurePenalty` and methods `baseSupport()` and `supportWithMetadata()` per research §4.1. **Must NOT be `private`** — the `supportBreakdownForTest(...)` seam returns it.
  - New private static method `supportBreakdown(QueryTerms, SearchResult) -> SupportBreakdown` that computes all five components, zeroing `lexicalSupport` for vector rows (not total support).
  - Existing `supportScore(...)` either REMOVED (if no non-refactor caller remains) or DELEGATED to `supportBreakdown(...).supportWithMetadata()` for backwards compatibility. Planner preference: **remove it entirely** — cleaner contract, forces all callers through the breakdown. If any external caller exists (outside PackRepository), flag and decide before landing.
  - All nine boundaries migrated to consume `SupportBreakdown`. Each call site explicitly uses `supportWithMetadata()` for ranking (or `baseSupport()` if an unusual boundary genuinely wants lexical-only — unlikely per research §5.4 which found no intentional zero-weight-metadata site).
  - Both prior compensation branches DELETED. Framing: **superseded, not redundant.** The shared helper applies a BROADER vector-row credit (`metadataBonus + specializedTopicBonus + sectionBonus + structurePenalty`) per Decision 2. The prior branches only re-added `metadataBonus`. Treat this as an intentional scoring-behavior change, not a behavior-preserving cleanup.
    - Rerank vector-only metadata reinjection at `:968-972` (R-ret1c artifact).
    - Anchor vector-only metadata fallback at `:2966-2977` (R-anchor1 artifact).
  - `RerankedResult` derivation **invariant** preserved: `baseScore = finalScore - metadataBonus` identity holds; constructor signature unchanged; `addGuideBonus(...)` re-derivation pattern unchanged. **Numeric stability is split by row type:**
    - Non-vector rows: `finalScore` and `baseScore` numbers identical to pre-refactor on all existing fixtures.
    - Vector rows: `finalScore` increases by `specializedTopicBonus + sectionBonus + structurePenalty` per Decision 2 (these were previously zeroed because `supportScore(...)` returned `0` on vector rows before they applied). `baseScore = finalScore - metadataBonus` identity still holds; the underlying `finalScore` is higher (or lower if structure penalty dominates). Vector-row fixtures in `PackRepositoryTelemetryTest` update to the new expected numbers.

- `PackRepositoryTelemetryTest.java`:
  - Existing rerank vector-metadata tests at `:159-229`: the **invariant** (vector rows get metadata credit) stays. **Round-4 scout-verified fixture enumeration:** only two fixtures in this range assert exact numeric `finalScore` on vector rows — `vectorRowsApplyMetadataBonusToSortKey` at `:159-199` and `vectorRowBaseScoreStillReflectsDisplayOnlyDerivation` at `:203-229`. For both of these, the three shifting components evaluate to zero on the specific candidates used: `specializedExplicitTopicBonus = 0` (no explicit-topic focus in the query, per `PackRepository.java:3687-3690`), `supportStructurePenalty = 0` (non-empty section heading, per `:4725-4728`), and `sectionHeadingBonus = 0` (the `Inventory` heading is not named in `QueryMetadataProfile.sectionHeadingBonus(...)` at `QueryMetadataProfile.java:395-520`). **Expected: both fixtures run unchanged post-refactor.** Verify numerically during Step 4 — if either shifts, the scout's analysis missed a component; investigate rather than blindly update.
  - If any vector-fixture OUTSIDE `:159-229` asserts a specific `finalScore` (not covered by the round-4 enumeration), that fixture needs update-and-document per Decision 2. Grep the whole `PackRepositoryTelemetryTest.java` for `"vector"` during Step 3 to confirm no additional fixtures exist.
  - One new invariant test: vector rows in rerank get metadata credit via `SupportBreakdown.metadataBonus`, not via a vector-only reinjection branch.

- `PackRepositoryTest.java`:
  - Existing anchor-score test (R-anchor1 pin) keeps passing.
  - Seven new targeted tests, one per chooser/boundary migration point (see §Test additions below for the exact list).

- `OfflineAnswerEngineTest.java`:
  - At most 1-3 fixture updates IF selected-context outcomes legitimately shift for vector-support-eligible fixtures. Planner preference: **update fixtures if behavior is the new intended behavior**; do NOT narrow the refactor to preserve old behavior. Document each change in the commit body with pre/post expected-anchor or expected-mode.
  - One new regression: a metadata-matched vector support row survives `buildGuideAnswerContext(...)` ranking.

- `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md` updated per in-slice tracker cadence.

- Single commit. Message: `R-anchor-refactor1: unify metadata application across PackRepository support boundaries`.

## Boundaries (HARD GATE)

- Touch only:
  - `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
  - `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - `notes/CP9_ACTIVE_QUEUE.md` (in-slice tracker update — see Step 5)
  - `notes/dispatch/README.md` (in-slice dispatch-index update — see Step 5)
- Do NOT:
  - Touch `QueryMetadataProfile.java`, `QueryRouteProfile.java`, or any other retrieval-support class beyond `PackRepository`.
  - Touch `OfflineAnswerEngine.java` production code. Test file updates are allowed (fixture shifts from upstream behavior change); production changes are out of scope.
  - Touch route-row scoring (`PackRepository.java:1885-1889`, `:2236-2240`), keyword-hit scoring (`:3821-3823`), hybrid RRF (`:3989-4037`), or session-hint rerank (`OfflineAnswerEngine.java:849-926`). Research §4.4 confirms these are separate families.
  - Touch the pack builder (`mobile_pack.py`) or pack assets. Mobile-pack regen NOT needed — this is a consumer-side refactor.
  - Rebuild the APK for emulator validation. Unit-suite gate is the acceptance.
  - Touch `scripts/build_android_ui_state_pack.ps1` or `scripts/run_android_instrumented_ui_smoke.ps1` (R-tool2 territory).
  - Introduce new public API on `PackRepository` beyond what the refactor strictly requires. `SupportBreakdown` stays `private static`.
  - Split into multiple commits. This is a coherent atomic refactor; split lands in a messy intermediate state.
  - Rotate this slice's file to `notes/dispatch/completed/` — rotation stays batched via the D-series; Step 5 just adds this slice to the "Landed (not yet rotated)" list.
  - Widen scope to `R-gate1` (rerank-entry gate) or `R-gal1` (trust-spine tolerance). Both are out-of-family per research §3.

One commit total. Message `R-anchor-refactor1: unify metadata application across PackRepository support boundaries`.

## The work

### Step 1 — Helper extraction

In `PackRepository.java`, introduce `SupportBreakdown` as a **package-private** `static final` nested class — matches the existing pattern for other test-visible nested types in this file (`GuideScore` at `:5880`, `RerankedResult` at `:5892`; both `static final class ... { }` with default package-private visibility). **Must NOT be `private`** — the `supportBreakdownForTest(...)` seam returns a `SupportBreakdown`, so test classes in the same package need visibility into the type. Placement: adjacent to the current `supportScore(...)` definition, to keep related code co-located.

```java
static final class SupportBreakdown {
    final int lexicalSupport;
    final int metadataBonus;
    final int specializedTopicBonus;
    final int sectionBonus;
    final int structurePenalty;

    SupportBreakdown(int lexicalSupport, int metadataBonus, int specializedTopicBonus, int sectionBonus, int structurePenalty) {
        this.lexicalSupport = lexicalSupport;
        this.metadataBonus = metadataBonus;
        this.specializedTopicBonus = specializedTopicBonus;
        this.sectionBonus = sectionBonus;
        this.structurePenalty = structurePenalty;
    }

    int baseSupport() {
        return lexicalSupport + specializedTopicBonus + sectionBonus + structurePenalty;
    }

    int supportWithMetadata() {
        return baseSupport() + metadataBonus;
    }
}
```

Add `private static SupportBreakdown supportBreakdown(QueryTerms queryTerms, SearchResult result)` next to it. Mirror the exact component computations from the current `supportScore(...)` body — do NOT change the scoring formulas:

```java
private static SupportBreakdown supportBreakdown(QueryTerms queryTerms, SearchResult result) {
    String retrievalMode = emptySafe(result.retrievalMode).toLowerCase(QUERY_LOCALE);
    boolean vector = "vector".equals(retrievalMode);
    int lexicalSupport = vector ? 0 : lexicalKeywordScore(queryTerms, /* existing args */);
    int metadataBonus = queryTerms.metadataProfile.metadataBonus(/* existing args */);
    int specializedTopicBonus = specializedExplicitTopicBonus(queryTerms, result);
    int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading);
    int structurePenalty = supportStructurePenalty(/* existing args */);
    return new SupportBreakdown(lexicalSupport, metadataBonus, specializedTopicBonus, sectionBonus, structurePenalty);
}
```

Key property: **for vector rows, `lexicalSupport` is `0` but `metadataBonus` is still computed normally.** This is the core architectural fix.

REMOVE the existing `supportScore(...)` method (lines 3651-3680) entirely — every call site gets migrated in Step 2. If any call site outside `PackRepository` exists (grep to confirm), flag before proceeding; prior research assumed no external callers.

**Also remove the in-file test wrapper `supportScoreForTest(...)` at `:3647-3648`** — it calls `supportScore(...)` and becomes a dead reference. It is consumed by exactly one test: `anchorGuideScoresDoNotDoubleCountMetadataBonusForLexicalRows` at `PackRepositoryTest.java:2458` (call site at `:2481`). Update that test to use the new `supportBreakdownForTest(...)` seam (below) — typically a one-line drop-in: `PackRepository.supportBreakdownForTest(query, result).supportWithMetadata()`.

**Add three new package-private `static` test seams** to `PackRepository.java` — plain `static` with default visibility; **do NOT add `@VisibleForTesting`** (round 5 scout Angle 2: `rg -n "@VisibleForTesting" android-app/app/src/main/java/com/senku/mobile` returns zero hits; the existing 30+ `ForTest` methods use plain `static`, e.g. `selectExplicitWaterDistributionAnchorForTest(...)` at `:2490`, `routeFocusedAnchorForTest(...)` at `:3403`, `supportScoreForTest(...)` at `:3647`; introducing the annotation mid-file would create a new convention and import for no demonstrated need). All three build `QueryTerms` internally from the `String query` argument so JVM unit tests don't need to construct it.

**Seam input contract (round 5 scout Angle 5):** production `buildGuideAnswerContext(...)` guards `rankedResults == null || rankedResults.isEmpty()` at `:727-730` and short-circuits on `anchor == null` at `:738-741`; the same-guide exclusion at `:772-773` assumes non-null anchor. The new seams are TEST-ONLY surfaces — Codex documents a strict contract rather than adding production-quality null/empty guards: **`rankSupportCandidatesForTest` and `selectAnswerAnchorForTest` require non-null arguments and non-empty lists; callers that want to exercise production's empty/null short-circuits must write separate tests that invoke those paths via other means.** Document this in a javadoc comment on each seam. `supportBreakdownForTest` accepts any non-null `SearchResult` (mirroring `supportScoreForTest`).

1. **`supportBreakdownForTest(String query, SearchResult result) -> SupportBreakdown`** — drop-in replacement surface for `supportScoreForTest(...)`. Exposes the new breakdown helper for tests that previously read a single support score. Update `anchorGuideScoresDoNotDoubleCountMetadataBonusForLexicalRows` to consume `.supportWithMetadata()` off the returned breakdown.

2. **`rankSupportCandidatesForTest(String query, SearchResult anchor, List<SearchResult> ranked) -> List<SearchResult>`** — extracts the pure support-candidate scoring step from `buildGuideAnswerContext(...)` (the loop at `:769-797` that filters `supportWithMetadata() > 0` and sorts) so the JVM unit lane can exercise it without constructing a DB-backed `PackRepository`. **Must take an explicit `anchor` argument** — `buildGuideAnswerContext(...)` picks the anchor first (`:738` via `:2402-2468`) and then excludes same-guide rows from the support-candidate pool at `:772-773`; a seam that doesn't accept an anchor cannot reproduce that exclusion and will produce wrong results when the test's intended support row happens to match the test's anchor pick. Tests pass an explicit stable anchor (typically a third top-ranked lexical row with an unambiguous guide) so the metadata-matched vector row goes into the SUPPORT pool, not the anchor slot. **Implementation pattern:** factor the pure scoring portion of `buildGuideAnswerContext(...)` into a private helper (e.g. `rankSupportCandidates(QueryTerms queryTerms, SearchResult anchor, List<SearchResult> ranked)`); `buildGuideAnswerContext(...)` calls it with the anchor it just selected AND proceeds to the existing DB section-loading step at `:3526-3537`; the `ForTest` seam also calls it. This avoids duplicating the scoring logic. Used by Test 1 and the engine regression.

3. **`selectAnswerAnchorForTest(String query, SearchResult rankedAnchor, SearchResult routedAnchor) -> SearchResult`** — exposes the ranked-vs-routed anchor tiebreak at Boundary 2.3 (`:2444-2468`) as a static helper. Mirrors the existing pattern of `routeFocusedAnchorForTest(...)` at `:3403`. Used by Test 2.

**Why this is necessary (anchors the slice to the JVM unit test lane):** `android-app/app/build.gradle:74-81` declares JUnit only, no Robolectric. `rg -n "new PackRepository\\(" android-app/app/src/test/java/com/senku/mobile` returns zero hits — the test lane has no precedent for instantiating `PackRepository` (which would call `SQLiteDatabase.openDatabase(...)` at construction, `PackRepository.java:160-165`). The new tests MUST go through static `ForTest` seams, consistent with the 30+ existing seams in this file.

### Step 2 — Per-boundary migration

Migrate each of the **ten** call sites across nine boundaries (boundary 2.3 has two). Row-type behavior:
- **Non-vector rows**: helper produces the same score numbers as pre-refactor on all existing fixtures.
- **Vector rows**: helper produces scores that differ from pre-refactor — `finalScore` gains `specializedTopicBonus + sectionBonus + structurePenalty` per Decision 2. The two prior slices (R-ret1c, R-anchor1) only gave vector rows metadata credit via local compensation; the refactor supersedes those branches with broader credit.

**Boundary 2.1 — `buildGuideAnswerContext(...)` support candidates (`:769-797`)**

Before (conceptual):
```java
int score = supportScore(queryTerms, candidate);
if (score <= 0) continue;
// sort by score, then retrieval rank
```

After:
```java
SupportBreakdown support = supportBreakdown(queryTerms, candidate);
int score = support.supportWithMetadata();
if (score <= 0) continue;
// sort by score, then retrieval rank
```

User-visible effect: metadata-aligned vector rows can now enter the support-candidate list instead of being silently dropped. This is the largest behavioral shift in the refactor. Expected.

**Seam extraction:** the scoring portion of this boundary (the filter-and-sort loop, pre-DB-section-loading) is factored into a private helper that `buildGuideAnswerContext(...)` calls AND the new `rankSupportCandidatesForTest(...)` seam (per Step 1) calls. No logic duplication — one implementation, two callers.

**Boundary 2.2 — `maybeRerankResultsDetailed(...)` (`:945-1003`)**

Before (with R-ret1c patch):
```java
int score = supportScore(queryTerms, result) + rerankModeBonus(result.retrievalMode);
int metadataBonusValue = queryTerms.metadataProfile.metadataBonus(...);
if (isVectorRetrievalMode(result.retrievalMode)) {
    score += metadataBonusValue; // R-ret1c compensation
}
RerankedResult r = new RerankedResult(result, originalIndex, metadataBonusValue, /*finalScore=*/score);
// ... addGuideBonus called later in outer loop
```

After:
```java
SupportBreakdown support = supportBreakdown(queryTerms, result);
int score = support.supportWithMetadata() + rerankModeBonus(result.retrievalMode);
int metadataBonusValue = support.metadataBonus;
RerankedResult r = new RerankedResult(result, originalIndex, metadataBonusValue, /*finalScore=*/score);
// R-ret1c compensation branch DELETED
// ... addGuideBonus called later in outer loop
```

DELETE the vector-only reinjection branch (`:968-972`) — **superseded, not redundant**. The shared helper gives vector rows a BROADER credit than the deleted branch did (metadata + specializedTopicBonus + sectionBonus + structurePenalty vs. metadata only). The `RerankedResult` constructor contract is preserved — same signature, same `baseScore = finalScore - metadataBonus` derivation at construction, same `addGuideBonus(...)` re-derivation pattern. Non-vector row numbers unchanged; vector row `finalScore` shifts per Decision 2.

**Telemetry impact** (per Decision 3 above): non-vector rows produce identical numbers to today. Vector rows produce numbers different from today: their `finalScore` gains `specializedTopicBonus + sectionHeadingBonus + structurePenalty` because those terms now apply. `baseScore = finalScore - metadataBonus` stays the consistent identity but the underlying `finalScore` is higher (or lower if penalty dominates). `PackRepositoryTelemetryTest` fixtures that exercise vector rows will likely need expected-value updates — make those updates explicit in the commit body, do NOT change the derivation invariant.

**Boundary 2.3 — Ranked-vs-routed anchor tiebreak (`:2466-2467`)**

This boundary has TWO `supportScore(...)` call sites — both must migrate.

Before:
```java
int rankedScore = Math.max(1, supportScore(queryTerms, rankedAnchor));
int routedScore = Math.max(1, supportScore(queryTerms, routedAnchor));
return rankedScore >= routedScore + 12 ? rankedAnchor : routedAnchor;
```

After:
```java
SupportBreakdown rankedSupport = supportBreakdown(queryTerms, rankedAnchor);
SupportBreakdown routedSupport = supportBreakdown(queryTerms, routedAnchor);
int rankedScore = Math.max(1, rankedSupport.supportWithMetadata());
int routedScore = Math.max(1, routedSupport.supportWithMetadata());
return rankedScore >= routedScore + 12 ? rankedAnchor : routedAnchor;
```

User-visible effect: vector ranked AND vector routed anchors no longer silently flatten to `1`. Downstream anchor choice may shift when either side is a metadata-aligned vector. Both sides receive the new vector-applies-non-metadata-bonuses behavior from Decision 2 above.

**Seam exposure:** this tiebreak currently has no `ForTest` seam (`routeFocusedAnchorForTest(...)` at `:3403` exposes the route-focused path only, NOT the ranked-vs-routed tiebreak). Step 1 adds `selectAnswerAnchorForTest(String query, SearchResult rankedAnchor, SearchResult routedAnchor) -> SearchResult` — a thin static wrapper that builds `QueryTerms` and calls the same migrated tiebreak logic. Test 2 exercises it.

**Boundaries 2.4-2.7 — Specialized anchor choosers**

Same shape for each:
- `selectExplicitWaterDistributionAnchor(...)` at `:2522-2564`
- `selectExplicitWaterStorageAnchor(...)` at `:2567-2681`
- `selectSpecializedStructuredAnchor(...)` at `:2684-2730`
- `selectBroadHouseAnchor(...)` at `:2733-2779`

Pattern before (verified against `PackRepository.java:2540`, `:2614`, `:2712-2713`, `:2767` — round 5 scout Angle 1):
```java
int score = Math.max(1, supportScore(queryTerms, candidate)) + Math.max(0, 12 - index) + <chooser-specific bonuses>;
```

Pattern after:
```java
SupportBreakdown support = supportBreakdown(queryTerms, candidate);
int score = Math.max(1, support.supportWithMetadata()) + Math.max(0, 12 - index) + <chooser-specific bonuses>;
```

**Position bonus `Math.max(0, 12 - index)` preserved verbatim across all four choosers — earlier candidates get higher position score (capped at 0 after index 12). Do NOT rewrite as `+ index` (that would invert ordering pressure). Chooser-specific bonuses (role bias, mode bias, anchor alignment, broad-route section preference) are preserved unchanged. Only the `supportScore` call migrates.

**Boundary 2.8 — `buildAnchorGuideScores(...)` (`:2954-3012`)**

Before (with R-anchor1 patch):
```java
int support = supportScore(queryTerms, candidate);
if (support <= 0 && isVectorRetrievalMode(candidate.retrievalMode)) {
    support = queryTerms.metadataProfile.metadataBonus(...); // R-anchor1 compensation
}
if (support <= 0) continue;
// map population + downstream bonuses
```

After:
```java
SupportBreakdown support = supportBreakdown(queryTerms, candidate);
int supportValue = support.supportWithMetadata();
if (supportValue <= 0) continue;
// map population + downstream bonuses
```

DELETE the vector-only fallback branch (`:2966-2977`) — **superseded, not redundant**. Previously the fallback set `support = metadataBonus` only when `support <= 0`, so vector rows entered anchor scoring with metadata-only credit. Post-refactor, the breakdown helper gives vector rows their metadata credit PLUS specializedTopicBonus + sectionBonus + structurePenalty directly (per Decision 2). Vector rows previously rejected at `support <= 0` because all five components zeroed now enter scoring when any of the five is positive.

**Boundary 2.9 — `findRouteFocusedAnchor(...)` (`:3407-3440`)**

Same pattern as boundaries 2.4-2.7 — verified against `PackRepository.java:3431-3432`, position bonus is `Math.max(0, 12 - index)` NOT `+ index`:
```java
SupportBreakdown support = supportBreakdown(queryTerms, candidate);
int score = Math.max(1, support.supportWithMetadata()) + Math.max(0, 12 - index) + anchorAlignmentBonus + broadRouteSectionPreferenceBonus;
```

### Step 3 — Test additions + updates

**`PackRepositoryTelemetryTest.java`** — keep existing coverage, add one invariant test.

New test:
```java
@Test
public void rerankVectorRowsReceiveMetadataViaSharedBreakdownNotCompensationBranch() {
    // Assert: rerank on a vector row produces the same metadata contribution
    // as the shared breakdown helper reports for the same row.
    // Negative: the result should NOT be higher by the sum of lexical + metadata
    // as would happen if a stale R-ret1c-style double-apply survived the refactor.
}
```

Existing `rerankVectorRowsKeepMetadataBonus*` tests should remain passing — same invariant, different mechanism. If they fail, the refactor has changed a user-visible number; diagnose before landing.

**Vector-row fixture assertion verification (round-4 scout enumeration):** the two fixtures at `:159-199` (`vectorRowsApplyMetadataBonusToSortKey`) and `:203-229` (`vectorRowBaseScoreStillReflectsDisplayOnlyDerivation`) assert exact `finalScore` values. For the specific candidates used in these fixtures, `specializedExplicitTopicBonus`, `sectionHeadingBonus`, and `supportStructurePenalty` all evaluate to `0` (see Outcome section for evidence). Expected: both pass unchanged. Confirm numerically during Step 4; if either shifts, the round-4 enumeration missed a component — investigate, don't blindly update. Grep the rest of `PackRepositoryTelemetryTest.java` for any OTHER vector-mode fixture asserting `finalScore` or `baseScore`; if found, add it to this list and document pre/post in the commit body.

**`PackRepositoryTest.java`** — keep existing anchor-score coverage, add **seven** targeted tests (one per unpatched/refactored chooser-style boundary).

Seven new tests:

1. `buildGuideAnswerContextAdmitsMetadataMatchedVectorSupportRow` — the headline new behavior. Uses `PackRepository.rankSupportCandidatesForTest(query, anchor, rankedList)` (new JVM seam per Step 1) to exercise the scoring step WITHOUT instantiating `PackRepository` (DB-backed; not available in the JUnit-only unit lane). **Fixture: three rows** — (a) a stable anchor (top-ranked lexical with a distinct guide_id, explicitly passed as the `anchor` arg so same-guide exclusion at `:772-773` doesn't eject the row under test), (b) an off-topic lexical row, (c) a metadata-matched vector row on a DIFFERENT guide from the anchor. Assert: vector row (c) appears in the returned support-candidate list; pre-refactor it would be dropped at `score <= 0`. Two-row fixtures are insufficient — if the vector row's guide matches the anchor's guide, it's excluded at `:772-773` and the test's regression signal disappears.

2. `rankedVsRoutedAnchorTiebreakUsesFullVectorSupportBothSides` — uses `PackRepository.selectAnswerAnchorForTest(query, rankedAnchor, routedAnchor)` (new JVM seam per Step 1). Two sub-assertions covering both call sites at `:2466-2467`: (a) vector on ranked side wins against weaker lexical routed; (b) vector on routed side wins against weaker lexical ranked — fixture must give the routed vector enough metadata margin to overcome the `+12` ranked-side advantage in the tiebreak formula at `:2468`. Both sub-assertions confirm vector rows no longer flatten to `1` via `Math.max(1, supportScore(...))`.

3. `selectExplicitWaterDistributionAnchorUsesSupportBreakdown` — metadata-aligned vector candidate wins over a lexical candidate with weaker metadata at this chooser.

4. `selectExplicitWaterStorageAnchorUsesSupportBreakdown` — same pattern for the water-storage chooser, respecting its role bias.

5. `selectSpecializedStructuredAnchorUsesSupportBreakdown` — same for specialized-structured chooser, respecting its anchor alignment bonus.

6. `selectBroadHouseAnchorUsesSupportBreakdown` — same for broad-house chooser.

7. `findRouteFocusedAnchorUsesSupportBreakdown` — vector candidate with route section preference + metadata wins over lexical without route support.

**Chooser fixture requirement (scout Suggestion 4):** each chooser test MUST either (a) neutralize all non-support bonuses (role bias at `:2540-2556`, mode bias at `:2627-2674`, anchor/category alignment at `:2713-2723`, broad-route section preference at `:2767-2773`, route-focused alignment at `:3431-3434`) so the only scoring variable is `supportWithMetadata()`, OR (b) document in the test comment the exact numeric delta per candidate showing that `supportWithMetadata()` alone produces the winning margin. A loose fixture that lets non-support bonuses dominate will pass pre-refactor AND post-refactor, failing to pin the refactored behavior. Minimum acceptable documentation in each chooser test: one comment line per candidate listing `lexicalSupport / metadataBonus / specializedTopicBonus / sectionBonus / structurePenalty → supportWithMetadata()` and one line showing which term delivers the winning delta.

Existing `buildAnchorGuideScoresCompensatesVectorRowsForMetadata*` test(s) — should pass on the metadata-bonus invariant; may need expected-value updates if a fixture asserts a specific score number that gains topicBonus/sectionBonus/structurePenalty under the new contract. Document any such update in the commit body.

**`OfflineAnswerEngineTest.java`** — one new regression test (going through the full ranked-results → buildGuideAnswerContext → resolveAnswerMode path), plus fixture updates as needed.

New regression:
```java
@Test
public void rankedResultsWithMetadataMatchedVectorSurviveSelectedContextAndShapeAnswerMode() {
    // KEY: starts from raw ranked results, NOT from a hand-injected selectedContext.
    // Most existing OfflineAnswerEngineTest cases inject selectedContext directly
    // (see :2521-2583, :2655-2669, :2813-2820, :2886-2900), bypassing the scoring step
    // that this refactor changes. This regression fills that coverage gap.
    //
    // Unit-lane constraint: android-app/app/build.gradle:74-81 is JUnit-only (no
    // Robolectric, no Android runtime). PackRepository cannot be instantiated here
    // — it opens SQLite at construction (PackRepository.java:160-165). The existing
    // pattern uses only static helpers on PackRepository/OfflineAnswerEngine, and
    // injects selectedContext as hand-built List<SearchResult>. This regression
    // follows that pattern:
    //
    //   Step 1: build synthetic rankedResults — three rows:
    //             (a) stable anchor (top-ranked lexical, distinct guide_id);
    //             (b) off-topic lexical row;
    //             (c) metadata-matched vector row on a DIFFERENT guide from (a) —
    //                 the anchor's same-guide exclusion at :772-773 would otherwise
    //                 drop (c) from the support pool.
    //   Step 2: List<SearchResult> selectedContext =
    //               PackRepository.rankSupportCandidatesForTest(query, anchorFromA, rankedResults);
    //           (new JVM seam per Step 1 — exposes the scoring portion of
    //            buildGuideAnswerContext pre-section-loading; takes explicit anchor
    //            because the production path selects the anchor first).
    //   Step 3: String mode = OfflineAnswerEngine.resolveAnswerMode(selectedContext, ...);
    //           (existing static, same call pattern as :293, :332, :554, etc.).
    //
    // Assert (a): selectedContext contains the vector candidate (pre-refactor it was
    //   dropped at score <= 0; post-refactor its supportWithMetadata > 0 admits it).
    // Assert (b): resolveAnswerMode reflects the vector candidate's presence — e.g.,
    //   promoted from uncertain_fit to confident, or anchor shifts to the vector's
    //   guide. The specific expected mode depends on fixture content; pick a fixture
    //   where pre-refactor and post-refactor produce DIFFERENT modes so the assertion
    //   is load-bearing (not trivially passing).
    //
    // Pre-refactor: Step 2 drops the vector row; selectedContext off-topic or empty;
    //   Step 3 abstains or uncertain_fits.
    // Post-refactor: Step 2 admits the vector row; Step 3 confident_fits (or whatever
    //   the fixture targets).
    //
    // Note: this regression does NOT cover the full DB-backed section-loading step
    // of buildGuideAnswerContext — that stays unexercised in the JVM lane (out of
    // scope for this refactor; covered by any existing androidTest that goes through
    // the full PackRepository lifecycle, if such a test exists).
}
```

**Existing fixture-shift handling**: if any prior `OfflineAnswerEngineTest` case that DOES exercise the buildGuideAnswerContext path has an expected selected-context anchor or mode that legitimately shifts under the new contract, UPDATE the expectation rather than narrowing the refactor. Document each such change in the commit body with:
- Test name.
- Pre-refactor expected anchor / mode.
- Post-refactor expected anchor / mode.
- One-line justification (usually: "vector support with strong metadata was previously dropped; now admitted, which changes the selected anchor to X").

**Fixture-shift gate (revised post-scout):** scout SUGGESTION 5 noted that most likely-mode-regression tests at `OfflineAnswerEngineTest.java:2521-2583`, `:2655-2669`, `:2813-2820`, `:2886-2900` inject `selectedContext` directly, so they're INSULATED from the changed path and unlikely to shift. The "1-3 fixture shifts" budget mostly applies to (a) the new regression above being authored and (b) any test that builds context organically from ranked results.

If MORE than 5 fixtures shift across the entire `PackRepositoryTest` + `PackRepositoryTelemetryTest` + `OfflineAnswerEngineTest` suite, that's a signal the refactor has a larger behavioral footprint than expected — STOP and report before landing, don't just ship many fixture updates.

### Step 4 — Unit-suite gate

```bash
cd android-app
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.PackRepositoryTest*" \
                                      --tests "com.senku.mobile.PackRepositoryTelemetryTest*" \
                                      --tests "com.senku.mobile.OfflineAnswerEngineTest*" \
                                      --tests "com.senku.mobile.QueryMetadataProfileTest*"
```

Expected: baseline pass count + **9** new tests (7 chooser/boundary + 1 rerank invariant + 1 engine regression), all passing. Plus fixture updates in `PackRepositoryTelemetryTest` (vector-row numeric updates per Decision 2) and possibly `OfflineAnswerEngineTest` (if any context-path fixtures shift).

Then run the full Android unit suite for final acceptance:
```bash
./gradlew.bat :app:testDebugUnitTest
```

Expected: 438 + new tests, all passing. (Baseline was 438 after R-telemetry; 215 → 218 desktop suite changes are not relevant here.)

STOP if any prior test fails. Diagnose before landing. Common suspects:
- `RerankedResult.baseScore` / `finalScore` derivation subtly changed — check the helper's component ordering matches the old `supportScore` method exactly.
- A chooser's scoring order shifted because `Math.max(1, ...)` interacts with the breakdown helper differently — check that `supportWithMetadata()` is used, not `baseSupport()`.
- A fixture that was intentionally on the "vector row dropped" branch of `buildGuideAnswerContext` now surfaces that row — update the expected anchor if correct.

### Step 5 — In-slice tracker update

After Step 4 passes and BEFORE committing, update trackers in the same commit.

**Edit A — `notes/CP9_ACTIVE_QUEUE.md`**

1. **Last-updated line** — replace with:
   > Last updated: <current date> — R-anchor-refactor1 unified PackRepository support boundaries: `supportScore` removed in favor of a shared `SupportBreakdown`, all ten call sites across nine boundaries migrated, both R-ret1c and R-anchor1 compensation branches deleted (superseded by broader vector-row credit per Decision 2). Android unit suite 438 → 438+9 (+ vector-row fixture updates).

2. **Post-RC Tracked** — strike the `R-anchor-refactor1` row entirely.

3. **Completed rolling log** — append a dated entry with:
   - Files + line counts (expect PackRepository.java ~±150/-60, test files ~+300 combined).
   - The architectural contract change: every support boundary now applies metadata explicitly and exactly once via `SupportBreakdown.supportWithMetadata()`.
   - User-visible behavior shift: metadata-aligned vector support rows now enter `buildGuideAnswerContext(...)` candidate lists (previously dropped).
   - Both prior compensation branches (R-ret1c vector reinjection, R-anchor1 vector fallback) removed — superseded by broader vector-row credit in the shared breakdown helper per Decision 2.
   - Fixture shifts in `OfflineAnswerEngineTest` if any (list them by name + pre/post).
   - Scout-audit note: this was the third slice under the scout-audit-before-dispatch pattern and the first architectural-size one.

**Edit B — `notes/dispatch/README.md`**

1. **Active slices** — confirm R-anchor-refactor1 not listed as in-flight post-landing.
2. **Landed (not yet rotated)** — add `R-anchor-refactor1_pack_support_breakdown.md` to the rotation-pending list.
3. **Post-RC tracked reference** — sync by removing the R-anchor-refactor1 row.

### Step 6 — Commit

```bash
git add android-app/app/src/main/java/com/senku/mobile/PackRepository.java \
        android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java \
        android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java \
        android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java \
        notes/CP9_ACTIVE_QUEUE.md \
        notes/dispatch/README.md
git commit -m "R-anchor-refactor1: unify metadata application across PackRepository support boundaries"
```

Commit message body:
```
Replaces opaque supportScore(...) consumption in PackRepository with a
shared SupportBreakdown helper so lexical support and metadata support
are computed separately, vector rows contribute lexical 0 rather than
total support 0, and every support-boundary applies metadata
explicitly and exactly once.

Motivation: forward research at
notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_20260421.md returned
partial verdict on the broader "metadataBonus lives inside supportScore
at every boundary" thesis, but confirmed the narrow claim within
PackRepository. Two prior slices (R-ret1c 2ec77b8, R-anchor1 971961b)
had already patched individual boundaries via vector-only metadata
reinjection; seven other call sites were still running raw
supportScore(...) and silently dropping vector support rows. Both
prior compensation branches are removed — superseded by broader
vector-row credit in the shared breakdown helper (Decision 2).

Migration set (all ten supportScore call sites across nine boundaries in PackRepository):
- buildGuideAnswerContext() support candidates (:769-797) — previously
  unpatched; vector rows now survive ranking when metadata-aligned.
- maybeRerankResultsDetailed() rerank sort (:945-1003) — R-ret1c
  compensation branch deleted, shared breakdown applied.
- Ranked-vs-routed anchor tiebreak (:2444-2468) — previously unpatched;
  vector ranked anchor no longer flattens to 1.
- selectExplicitWaterDistributionAnchor (:2522-2564).
- selectExplicitWaterStorageAnchor (:2567-2681).
- selectSpecializedStructuredAnchor (:2684-2730).
- selectBroadHouseAnchor (:2733-2779).
- buildAnchorGuideScores (:2954-3012) — R-anchor1 compensation branch
  deleted, shared breakdown applied.
- findRouteFocusedAnchor (:3407-3440).

Telemetry contract: RerankedResult.baseScore / finalScore / metadataBonus
derivation invariant preserved. baseScore = finalScore - metadataBonus
identity holds; constructor signature unchanged; addGuideBonus
re-derivation pattern unchanged. Non-vector rows produce identical
numbers on all pre-existing fixtures. Vector rows: finalScore increases
by specializedTopicBonus + sectionBonus + structurePenalty — these
components were previously zeroed because supportScore returned 0 on
vector rows before they applied; post-refactor they apply (Decision 2,
intentional scoring change, not pure cleanup). Round-4 scout-verified
enumeration of PackRepositoryTelemetryTest.java:159-229: the two vector
fixtures asserting exact finalScore (vectorRowsApplyMetadataBonusToSortKey
at :159-199, vectorRowBaseScoreStillReflectsDisplayOnlyDerivation at
:203-229) both evaluate specializedExplicitTopicBonus / sectionHeadingBonus
/ supportStructurePenalty to zero on their specific candidates — expected
unchanged, confirmed numerically in Step 4. Other vector-row fixtures (if
any outside :159-229) updated to the new numbers: <list with pre/post
values per affected test, or "none" if the enumeration held>.

User-visible behavior shift: metadata-aligned vector support rows can
now enter buildGuideAnswerContext() candidate lists (previously
dropped). Anchor choice may shift on fixtures where a vector candidate
has strong metadata alignment. Fixture updates in
OfflineAnswerEngineTest are documented where applicable: <list if any>.

Tests: Android unit suite 438 -> <438 + 9>. Seven new chooser/boundary
tests + one rerank invariant + one engine regression + vector-row
fixture updates in PackRepositoryTelemetryTest + <N> context-path
fixture updates in OfflineAnswerEngineTest if any. All prior tests
pass unchanged except the listed fixtures.

Evidence: notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_20260421.md
(research doc driving the refactor scope and acceptance).

In-slice tracker updates included per cadence rule:
CP9_ACTIVE_QUEUE.md (Last-updated / Post-RC Tracked strike /
Completed log) and dispatch/README.md (Landed-not-rotated). Slice-file
rotation batched via the D-series.
```

Stage only the six files. Verify via `git diff --cached --stat` that no other file is staged.

## Acceptance

- Single commit touching exactly six files: `PackRepository.java`, three test files, two tracker files.
- `supportScore(...)` method is REMOVED from `PackRepository.java`. No grep hits for `supportScore(` anywhere in the production tree (test files may reference it in historical comments — that's acceptable).
- `supportScoreForTest(...)` helper at `:3647-3648` is REMOVED (it wrapped the now-removed `supportScore`). The one existing test that used it (`anchorGuideScoresDoNotDoubleCountMetadataBonusForLexicalRows` at `PackRepositoryTest.java:2458`; call at `:2481`) updated to call `supportBreakdownForTest(query, result).supportWithMetadata()` instead.
- Three new package-private `static` test seams added to `PackRepository.java` (plain `static`, NO `@VisibleForTesting` annotation — matches existing 30+ `ForTest` precedents in the file):
  - `supportBreakdownForTest(String query, SearchResult result) -> SupportBreakdown` — replacement surface for `supportScoreForTest(...)`.
  - `rankSupportCandidatesForTest(String query, SearchResult anchor, List<SearchResult> ranked) -> List<SearchResult>` — exposes the scoring step of `buildGuideAnswerContext(...)`. Takes explicit `anchor` because production selects the anchor first then excludes same-guide rows from support candidates. Used by Test 1 and the engine regression.
  - `selectAnswerAnchorForTest(String query, SearchResult rankedAnchor, SearchResult routedAnchor) -> SearchResult` — exposes the ranked-vs-routed tiebreak at Boundary 2.3. Used by Test 2.
- The scoring portion of `buildGuideAnswerContext(...)` is factored into a private helper that both the method itself and `rankSupportCandidatesForTest(...)` consume — no duplication.
- `SupportBreakdown` class exists as **package-private** `static final` nested class in `PackRepository.java` (matches existing `GuideScore` / `RerankedResult` pattern at `:5880` / `:5892`). **NOT `private`** — the `supportBreakdownForTest(...)` seam returns it and test classes in the same package consume `.supportWithMetadata()` off the result.
- All **ten** migrated call sites across nine boundaries call `supportBreakdown(...)` and use `supportWithMetadata()` for ranking. Boundary 2.3 migrates BOTH the ranked and routed call sites at `:2466-2467`.
- R-ret1c vector-only reinjection branch (formerly at `:968-972`) is DELETED.
- R-anchor1 vector-only fallback branch (formerly at `:2966-2977`) is DELETED.
- Android unit suite passes at `<baseline> + 9` new tests (7 chooser/boundary + 1 telemetry invariant + 1 engine regression), plus any fixture updates documented in the commit body.
- `RerankedResult` constructor signature unchanged. `baseScore = finalScore - metadataBonus` derivation invariant unchanged. `addGuideBonus(...)` re-derivation invariant unchanged.
- For non-vector rows: `finalScore` and `baseScore` numbers unchanged on all pre-existing test fixtures.
- For vector rows: `finalScore` and `baseScore` numbers may legitimately shift (per Decision 2 — vector rows now receive `specializedTopicBonus + sectionHeadingBonus + structurePenalty` previously zeroed). Round-4 scout-verified enumeration of `PackRepositoryTelemetryTest.java:159-229`: the two fixtures asserting exact vector `finalScore` (`vectorRowsApplyMetadataBonusToSortKey`, `vectorRowBaseScoreStillReflectsDisplayOnlyDerivation`) both have zero-valued shifting components on their specific candidates → expected to pass unchanged. Verify numerically during Step 4; any shifted fixture documented in commit body with old/new value.
- **Tiebreak-flip guard (round-4 scout Angle 5):** vector rows previously flattened to `1` via `Math.max(1, supportScore(...))` in choosers at `:2559`, `:2676`, `:2725`, `:2776`, `:3435` and support-candidate sort at `:784-796` / `:994-999`. Post-refactor they get real scores. Any new test fixture must avoid equal `supportWithMetadata()` totals across competing candidates where a sort/chooser tiebreak would otherwise decide the outcome — OR the test must document the deliberate tiebreak behavior it exercises. A fixture that passes only because of a tiebreak accident is brittle.
- `CP9_ACTIVE_QUEUE.md` Post-RC Tracked section no longer contains the R-anchor-refactor1 row.
- `CP9_ACTIVE_QUEUE.md` Completed rolling log has a new dated entry.
- `dispatch/README.md` "Landed (not yet rotated)" lists `R-anchor-refactor1_pack_support_breakdown.md`.
- `git status` clean on all six scoped files post-commit.

## Delegation hints

- **Step 1 (helper extraction)**: main-inline. Small, judgment-heavy.
- **Step 2 (per-boundary migration)**: main-inline for the two already-patched boundaries (rerank + buildAnchorGuideScores — deleting the compensation branch needs care) and the test-file churn. Remaining seven boundaries (all unpatched) can fan out to 2-3 `gpt-5.4 high` workers in parallel per-boundary if main wants to accelerate — each boundary is 5-15 lines of production change. Main must reconcile via `git diff` before Step 3.
- **Step 3 (test additions)**: main-inline OR `gpt-5.4 high` workers if splitting test-file edits across files (three parallel writers on three test files). Coordinator must verify no test name collisions.
- **Step 4 (unit-suite gate)**: main-inline. Gradle run.
- **Step 5 (tracker update)**: main-inline.
- **Step 6 (commit)**: main-inline.

**Scout consideration**: this slice is architectural-size (~250-400 lines production + tests). Scout-audit on Spark BEFORE Codex dispatch is strongly recommended per `feedback_scout_audit_before_dispatch.md`. See `R-anchor-refactor1_SCOUT_PROMPT.md`.

**MCP hints**:
- `context7` is not needed here — pure in-repo Java refactor.
- `git` MCP useful for the pre-refactor test-baseline capture and post-refactor diff review.
- `sequential-thinking` useful for Step 2 decomposition if any boundary trips up during migration (shouldn't, but each chooser has enough local state to warrant careful reading).

## Anti-recommendations

- Do NOT narrow scope to a subset of the nine boundaries. Mixed contract state (some boundaries using the shared helper, some still on raw `supportScore`) is worse than the current state.
- Do NOT preserve `supportScore(...)` as a back-compat shim. Clean removal forces all current and future callers through the breakdown; keeping the shim reintroduces the footgun.
- Do NOT change the scoring FORMULAS inside `supportBreakdown(...)`. Only the structure changes (fused number → separated components). If a formula adjustment seems necessary for a test to pass, the refactor has changed behavior unintentionally — diagnose.
- Do NOT update `QueryMetadataProfile.metadataBonus(...)` or `QueryRouteProfile.metadataBonus(...)` signatures. The helper call is a read on these profiles; no profile-side change is needed.
- Do NOT touch route-row scoring, keyword-hit scoring, hybrid RRF, or session-hint rerank. They're out of the support-boundary family per research §4.4.
- Do NOT add emulator validation as part of this slice. The unit suite is the gate. A gallery republish is a follow-up if planner wants on-device evidence.
- Do NOT rebuild the mobile pack. Consumer-side only.
- Do NOT rotate the slice file to `completed/`. Rotation batched via D-series.
- Do NOT backfill a commit sha into the tracker entries — sha doesn't exist until post-commit. Reference commit by message title + date.
- Do NOT touch `R-tool2_state_pack_logcat_capture.md` (R-tool2 landed at commit `38b7826` 2026-04-21; the slice file is awaiting D-series rotation). Tracker files (`notes/CP9_ACTIVE_QUEUE.md`, `notes/dispatch/README.md`) ARE in scope per the Step 5 in-slice tracker cadence — that's the normal landing pattern, not a conflict.
- Do NOT split into two commits (helper extraction vs. boundary migration). The intermediate state (helper exists but callers haven't migrated) is broken — prior compensation branches would double-apply or disappear. Atomic is required.

## Report format

Reply with:

- Commit sha + message.
- Files touched with `+X/-Y` counts per file.
- Line counts per migrated call site (10 counts; expect ~3-12 lines per site; boundary 2.3 has two adjacent migrations).
- List of deleted compensation branches (2 expected: rerank vector reinjection, buildAnchorGuideScores vector fallback).
- List of removed methods (2 expected: `supportScore(...)`, `supportScoreForTest(...)`).
- List of new package-private `static` seams (3 expected, NO `@VisibleForTesting` annotation): `supportBreakdownForTest`, `rankSupportCandidatesForTest`, `selectAnswerAnchorForTest` — include line location in PackRepository.java and `+X` line count each.
- List of existing tests updated to consume new seams (1 expected: `anchorGuideScoresDoNotDoubleCountMetadataBonusForLexicalRows`).
- New tests added (9 expected: 7 chooser/boundary + 1 telemetry invariant + 1 engine regression): list by name.
- Fixture updates in `OfflineAnswerEngineTest` if any: list by name + pre/post expected value + one-line justification.
- Android unit suite result: `<baseline> → <baseline + N>`, all passing.
- `RerankedResult.baseScore` / `finalScore` stability confirmation: run the existing rerank telemetry test and report pass status explicitly.
- Tracker edit diff summary: one line per file confirming scoped edits landed.
- `git status --short` output post-commit.
- Any out-of-scope drift noticed — flag, don't fix.
- Delegation log (lane used per step, per boundary if Step 2 fanned out).

## If anything fails

- **Step 1 helper extraction breaks existing `supportScore(...)` callers** because signature changed: if you opted to keep `supportScore` as a shim, revert the shim and migrate callers instead. Shim preserves the footgun.
- **Step 2 boundary migration produces different scores**: the helper's component ordering doesn't match the old `supportScore` body. Read the pre-refactor `supportScore(...)` body carefully — line-by-line — and confirm each of the five components (lexicalSupport, metadataBonus, specializedTopicBonus, sectionBonus, structurePenalty) is computed with identical args and sign. The old order was: lexical first, then metadata, then specialized-topic, then section, then structure-penalty (which is negative). The breakdown must reproduce that sum.
- **Step 3 new tests fail**: read the failure. If the fixture is asserting the pre-refactor dropped-vector-row behavior, the test is wrong for the new contract — rewrite. If the fixture is asserting a score value that shouldn't have changed, the helper has drifted — compare to old `supportScore(...)` output on the same row.
- **Step 3 existing tests fail beyond expected fixture shifts**: more than 3 fixtures shifting is the STOP signal. Don't ship many fixture updates silently. The refactor is having bigger behavioral impact than anticipated; planner needs to weigh in before landing.
- **Telemetry numbers drift**: `RerankedResult.baseScore` / `finalScore` depend on `rerankModeBonus` being added AFTER support, and guide bonus being added in the outer loop. If those orderings changed during migration, the numbers will drift. Verify the additions happen in the same sequence as pre-refactor.
- **If blocked at any step for more than 60 minutes, STOP and report.** This slice is bigger than prior slices; don't grind in silence. Architectural refactors are where "heroic debugging" most often produces the wrong fix.

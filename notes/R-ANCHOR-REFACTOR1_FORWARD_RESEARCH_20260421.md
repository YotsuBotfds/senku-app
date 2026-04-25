# Forward research - R-anchor-refactor1

Written 2026-04-21 as forward research for the queued architectural
direction named in the 2026-04-21 DAY and LATE handoffs.

This is research only. No code changes are proposed or dispatched from
this note. Any later slice draft should re-cite the production/test
lines it actually changes.

---

## 1. Thesis verdict

Verdict: **partially confirmed**.

The underlying architectural smell is real: inside
`PackRepository.java`, `supportScore(...)` currently mixes lexical
support, metadata support, explicit-topic bonuses, section bonuses, and
structure penalties into one number, but it also hard-returns `0` for
`vector` rows before any of those later terms can apply
(`PackRepository.java:3651-3680`). That design has already forced two
boundary-specific repairs: rerank had to add `metadataBonus` back
outside `supportScore` (`PackRepository.java:945-1003`, landed in
`2ec77b8`), and anchor scoring had to do the same at a second boundary
(`PackRepository.java:2954-3012`, landed in `971961b`).

The overreach is the "all four slices are the same pattern" part. They
are not. `R-ret1c` and `R-anchor1` are the same shape. `R-gate1` is an
upstream phase-entry gate bug, not a score-composition bug
(`PackRepository.java:926-950`, landed in `f3b2c68`). `R-gal1` does not
touch `metadataBonus` or `supportScore` at all; it only relaxes an
androidTest trust-spine wording assertion in
`PromptHarnessSmokeTest.java:2846-2849` (landed in `585320c`).

The refined thesis that the code supports is narrower and more useful:
**inside `PackRepository`, support-like boundaries should stop consuming
a monolithic `supportScore` that hides `metadataBonus`; instead they
should consume a shared support breakdown where lexical evidence and
metadata evidence are separate fields, with vector rows contributing
lexical `0` rather than total-support `0`.**

## 2. Current scoring topology

### 2.1 Important location correction

The prompt frames `OfflineAnswerEngine.java` as the home of
`supportScore`, rerank scoring, and anchor scoring. That is not true in
the current tree.

- `supportScore(...)` is defined in `PackRepository.java:3651-3680`.
- The rerank loop is `PackRepository.java:945-1003`.
- Anchor scoring is `PackRepository.java:2396-3012`.
- `OfflineAnswerEngine.java` consumes selected-context outputs and runs
  confidence / mode gates later (`OfflineAnswerEngine.java:1168-1519`).

So the actual refactor surface is **primarily `PackRepository`**, with
`OfflineAnswerEngine` as a downstream consumer whose behavior may shift
if context selection changes.

### 2.2 Where `metadataBonus` comes from

There are **two different `metadataBonus` APIs** in the Android app.
They should not be conflated.

| Symbol | File:line | What it is | In thesis scope? |
|---|---|---|---|
| `QueryMetadataProfile.metadataBonus(...)` | `QueryMetadataProfile.java:762-835` | Query-to-row metadata affinity score over category, role, time horizon, structure type, and topic tags | Yes |
| `QueryRouteProfile.metadataBonus(...)` | `QueryRouteProfile.java:1173-1215` | Route-profile text-marker score over title/section/category/tags/description/document | No - same name, different family |

The thesis is about the **`QueryMetadataProfile`-driven** bonus, not the
route-profile helper with the same method name.

### 2.3 Retrieval-stage scores before rerank

The Android retrieval stack already has several scoring surfaces that do
**not** flow through `supportScore`.

1. **Route row scoring**
   - `PackRepository.java:1885-1889`
   - `PackRepository.java:2236-2240`
   - Formula shape:
     - `specScore = lexicalKeywordScore(specTerms, ...) + metadataBonus(specTerms, ...)`
     - `queryScore = lexicalKeywordScore(queryTerms, ...) + metadataBonus(queryTerms, ...)`
     - final route score adds `routeSpec.bonus()`, `sectionHeadingScore`,
       and sometimes `+20`
   - This family already keeps metadata separate from the `supportScore`
     helper. It is not part of the current asymmetry.

2. **Keyword-only lexical search**
   - `PackRepository.java:3821-3823`
   - Formula: `lexicalKeywordScore(...) + metadataBonus(...)`
   - Also already separate from `supportScore`.

3. **Hybrid lexical/vector fusion**
   - `PackRepository.java:3989-4037`
   - Lexical hits and vector hits contribute reciprocal-rank values into
     `CombinedHit.rrfScore`.
   - `applyAnchorPrior(...)` can add an anchor-memory bonus on top at
     `PackRepository.java:4040-4084`.
   - This is a pure RRF-style merge; it never calls `supportScore`.

4. **Search-result materialization**
   - `PackRepository.java:4139-4175`
   - Retrieval modes are assigned here (`hybrid`, `vector`, `lexical`).
   - Those mode labels are later consumed by rerank, anchor scoring, and
     `OfflineAnswerEngine` confidence/mode gates.

### 2.4 `supportScore` definition

`supportScore(...)` is the core architectural knot:
`PackRepository.java:3651-3680`.

Current behavior:

```java
private static int supportScore(QueryTerms queryTerms, SearchResult result) {
    String retrievalMode = emptySafe(result.retrievalMode).toLowerCase(QUERY_LOCALE);
    if ("vector".equals(retrievalMode)) {
        return 0;
    }
    int score = lexicalKeywordScore(...);
    score += metadataBonus(...);
    score += specializedExplicitTopicBonus(...);
    score += sectionHeadingBonus(...);
    score += supportStructurePenalty(...);
    return score;
}
```

Architecturally relevant facts:

- The vector special case is a **total-support early return**, not a
  lexical-only early return.
- For non-vector rows, `metadataBonus` is embedded inside the returned
  number.
- For vector rows, the method hides `metadataBonus` completely unless a
  caller manually re-adds it.

That is exactly why callers have drifted into a mix of:

- unpatched raw `supportScore(...)` consumption,
- manual `metadataBonus` reinjection,
- or unrelated scoring paths that never call `supportScore`.

### 2.5 Every `supportScore(...)` call site

This is the key inventory for the refactor.

| Boundary | File:line | Current score use | Vector behavior today | Status |
|---|---|---|---|---|
| Support candidates in answer context | `PackRepository.java:769-797` | `score = supportScore(queryTerms, candidate)` then sort by score and retrieval rank | `vector` rows score `0` and are dropped at `779-780` | **Unpatched** |
| Rerank sort | `PackRepository.java:945-1003` | `score = supportScore + rerankModeBonus`, then vector-only `+ metadataBonus`, then guide bonus | `vector` rows compensated at `968-972` | **Patched by R-ret1c** |
| Ranked-vs-routed anchor tiebreak | `PackRepository.java:2444-2468` | compare `Math.max(1, supportScore(rankedAnchor))` vs routed | `vector` ranked anchor underweighted to base `1` | **Unpatched** |
| Explicit water-distribution anchor chooser | `PackRepository.java:2522-2564` | `Math.max(1, supportScore(candidate)) + index + mode/category bonuses` | `vector` rows lose metadata term | **Unpatched** |
| Explicit water-storage anchor chooser | `PackRepository.java:2567-2681` | `Math.max(1, supportScore(candidate)) + index + role/mode/topic/category bonuses` | `vector` rows lose metadata term | **Unpatched** |
| Specialized structured anchor chooser | `PackRepository.java:2684-2730` | `Math.max(1, supportScore(candidate)) + index + anchorAlignmentBonus + mode bias` | `vector` rows lose metadata term | **Unpatched** |
| Broad-house anchor chooser | `PackRepository.java:2733-2779` | `Math.max(1, supportScore(candidate)) + index + broad-house bias + mode bias` | `vector` rows lose metadata term | **Unpatched** |
| Main guide-score map for anchor selection | `PackRepository.java:2954-3012` | `support = supportScore`; vector-only fallback recomputes metadata bonus; then index/alignment/mode bonuses | `vector` rows compensated at `2966-2977` | **Patched by R-anchor1** |
| Route-focused anchor chooser | `PackRepository.java:3407-3440` | `Math.max(1, supportScore(candidate)) + index + anchorAlignmentBonus + broadRouteSectionPreferenceBonus` | `vector` rows lose metadata term | **Unpatched** |

This table is the strongest evidence for the refactor direction. The
current codebase already contains two local patches proving that some
callers want `metadataBonus` outside `supportScore`; several other
callers still consume the old fused helper directly.

### 2.6 Every `QueryMetadataProfile.metadataBonus(...)` appearance

This is the full in-tree appearance map for the metadata-profile bonus.

| Use | File:line | Role |
|---|---|---|
| Definition | `QueryMetadataProfile.java:762-835` | Produces the row/query metadata affinity score |
| Rerank score input | `PackRepository.java:958-965` | Computes bonus per rerank row |
| Route-row scoring | `PackRepository.java:1886-1888` | Adds metadata to route-focus section scores |
| Guide-sweep scoring | `PackRepository.java:2237-2239` | Adds metadata to guide-focus sweep scores |
| Anchor vector fallback | `PackRepository.java:2970-2977` | R-anchor1 compensation path |
| Guide-section loading | `PackRepository.java:3551-3587` | Section candidates for the chosen anchor guide |
| Embedded inside `supportScore` | `PackRepository.java:3665-3678` | The architectural knot |
| Keyword-hit scoring | `PackRepository.java:3821-3823` | Search fallback score |
| Support-candidate filtering | `PackRepository.java:4568-4665` | Boolean filter / threshold logic, not direct ranking |
| Session-hint rerank | `OfflineAnswerEngine.java:862-869` | Separate downstream rerank, uses `metadataScore * 2` |
| Confidence-label gate | `OfflineAnswerEngine.java:1181-1223` | Downstream evidence classification |

Two observations matter:

1. The **PackRepository route-search family** already uses metadata as a
   separate term. It is not blocked on this refactor.
2. The **supportScore family** is the only place where metadata is
   buried inside a helper that also hard-zeros vector rows.

### 2.7 Other score-like quantities that feed ranking decisions

These are adjacent scoring surfaces that the slice author must know
about even if they are not all in-scope for the refactor.

1. **Guide aggregation bonus**
   - `PackRepository.java:1037-1045`
   - Added on top of rerank row scores by guide, after the per-row score
     is computed.

2. **Support retrieval rank tiebreak**
   - `PackRepository.java:4528-4536`
   - Used in `buildGuideAnswerContext(...)` when support scores tie.

3. **Route search thresholds**
   - `PackRepository.java:1925-1970`
   - Thresholds affect how many route / guide-focus candidates reach
     later ranking boundaries.

4. **Session-hint rerank in `OfflineAnswerEngine`**
   - `OfflineAnswerEngine.java:849-926`
   - Independent scoring system over context follow-up candidates.
   - Metadata is weighted at `metadataScore * 2` with clamping, then
     mixed with lexical overlap, recency, explicit-topic focus, and
     session memory.

5. **Confidence label**
   - `OfflineAnswerEngine.java:1168-1225`
   - Uses overlap, hybrid flag, `metadataBonus`, section bonus, and
     preferred-topic overlap as evidence labels (`HIGH`, `MEDIUM`,
     `LOW`).

6. **Answer mode**
   - `OfflineAnswerEngine.java:1245-1279`
   - Consumes selected context, raw top chunks, confidence label,
     `averageRrfStrength(...)`, and `topVectorSimilarity(...)`.
   - `averageRrfStrength(...)` is scored at
     `OfflineAnswerEngine.java:1341-1399`.
   - `topVectorSimilarity(...)` is scored at
     `OfflineAnswerEngine.java:1401-1435`.

7. **Abstain gate**
   - `OfflineAnswerEngine.java:1491-1519`
   - Uses overlap counts and strong-semantic-hit detection, not
     `supportScore`.

### 2.8 Most important open asymmetry surfaced by this read

The biggest still-open boundary is not named in the handoff text:

- `buildGuideAnswerContext(...)` support candidates use raw
  `supportScore(...)` and drop `score <= 0`
  (`PackRepository.java:769-797`).
- Because `supportScore(...)` returns `0` for vector rows, vector
  support rows cannot currently enter that support-candidate list unless
  they are already represented via guide-focus sections or another
  lexical path.

That means the current tree still has a live "vector metadata exists but
the boundary never sees it" asymmetry even **after** `R-ret1c` and
`R-anchor1`.

## 3. Prior-slice pattern analysis

### 3.1 `R-gate1` / commit `f3b2c68`

Evidence:

- Commit message and diff: `git show f3b2c68`
- Current code: `PackRepository.java:926-950`
- Test coverage: `PackRepositoryTelemetryTest.java:119-139`,
  `292-383`

Exact patched pattern:

- Before the fix, rerank only ran when
  `queryTerms.routeProfile.isRouteFocused()` was true.
- Structure-hint-only queries with a non-empty
  `QueryMetadataProfile.preferredStructureType()` never entered the
  rerank phase.
- The patch introduced `shouldApplyMetadataRerank(...)` so rerank fires
  for either route focus or a structure hint.

Classification:

- This is **not** a "metadataBonus is inside supportScore but missing at
  one scoring boundary" bug.
- It is a **phase-entry gate** bug: the boundary never ran.

### 3.2 `R-ret1c` / commit `2ec77b8`

Evidence:

- Commit message and diff: `git show 2ec77b8`
- Current code: `PackRepository.java:945-1003`
- Test coverage: `PackRepositoryTelemetryTest.java:159-288`

Exact patched pattern:

- Rerank computed `metadataBonus` for telemetry, but sort order came
  from `score = supportScore(queryTerms, result) + rerankModeBonus(...)`.
- For vector rows, `supportScore(...)` returned `0` before metadata was
  added (`PackRepository.java:3651-3655`), so the sort key ignored
  metadata even though telemetry displayed it.
- Patch: vector rows get `score += metadataBonus` inside the rerank loop
  at `PackRepository.java:968-972`.

Classification:

- This is the **canonical thesis case**.

### 3.3 `R-anchor1` / commit `971961b`

Evidence:

- Commit message and diff: `git show 971961b`
- Current code: `PackRepository.java:2954-3012`
- Test coverage: `PackRepositoryTest.java:2387-2516`

Exact patched pattern:

- `buildAnchorGuideScores(...)` used `supportScore(...)` directly.
- Vector rows got `support == 0` and were dropped by the `support <= 0`
  guard.
- Patch: for vector rows only, recompute `metadataBonus(...)` outside
  `supportScore(...)` before the drop guard.

Classification:

- Same shape as `R-ret1c`: **metadata-affine vector rows lost at a
  supportScore boundary**.

### 3.4 `R-gal1` / commit `585320c`

Evidence:

- Commit message and diff: `git show 585320c`
- Current code: `PromptHarnessSmokeTest.java:2846-2849`

Exact patched pattern:

- The trust-spine test helper only accepted backend labels or
  "answer ready" / "no guide match" completion text.
- It was expanded to also accept "not a confident fit",
  "uncertain fit", and "abstain".

Classification:

- This is **not** part of the `metadataBonus` / `supportScore` family.
- It is a **downstream instrumentation assertion tolerance** change.

### 3.5 Are the four slices one shape?

No. They split into **three** shapes:

1. **Gate-to-boundary missing**
   - `R-gate1`

2. **Boundary computes/consumes support but vector rows lose metadata**
   - `R-ret1c`
   - `R-anchor1`

3. **Downstream UI/test acceptance gap**
   - `R-gal1`

So the architectural refactor is still worthwhile, but the planner
framing should be narrowed. The refactor can unify the **support-boundary
family inside `PackRepository`**. It does not explain `R-gal1`, and it
only partially explains `R-gate1`.

## 4. Unified application proposal

### 4.1 Proposed new contract

Do **not** let `supportScore(...)` be the place where metadata is hidden.

Instead, replace it with a shared breakdown helper in `PackRepository`,
for example:

```java
private static final class SupportBreakdown {
    final int lexicalSupport;
    final int metadataBonus;
    final int specializedTopicBonus;
    final int sectionBonus;
    final int structurePenalty;

    int baseSupport() {
        return lexicalSupport + specializedTopicBonus + sectionBonus + structurePenalty;
    }

    int supportWithMetadata() {
        return baseSupport() + metadataBonus;
    }
}
```

And:

```java
private static SupportBreakdown supportBreakdown(QueryTerms queryTerms, SearchResult result) {
    boolean vector = isVectorRetrievalMode(result.retrievalMode);
    int lexicalSupport = vector ? 0 : lexicalKeywordScore(...);
    int metadataBonus = metadataBonus(queryTerms, ...);
    int specializedTopicBonus = specializedExplicitTopicBonus(queryTerms, result);
    int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading);
    int structurePenalty = supportStructurePenalty(...);
    return new SupportBreakdown(
        lexicalSupport,
        metadataBonus,
        specializedTopicBonus,
        sectionBonus,
        structurePenalty
    );
}
```

Key change:

- Vector rows become **lexical `0`**, not **support `0`**.
- Metadata is always available as a separate field.

### 4.2 Boundary helper shape

Then boundary code becomes explicit about what it is adding.

Before:

```java
int score = supportScore(queryTerms, result);
score += rerankModeBonus(result.retrievalMode);
if (isVectorRetrievalMode(result.retrievalMode)) {
    score += metadataBonus;
}
```

After:

```java
SupportBreakdown support = supportBreakdown(queryTerms, result);
int score = support.supportWithMetadata();
score += rerankModeBonus(result.retrievalMode);
```

Before:

```java
int support = supportScore(queryTerms, result);
if (support <= 0 && isVectorRetrievalMode(result.retrievalMode)) {
    support = metadataBonus(...);
}
if (support <= 0) continue;
```

After:

```java
SupportBreakdown support = supportBreakdown(queryTerms, result);
int supportScore = support.supportWithMetadata();
if (supportScore <= 0) continue;
```

### 4.3 Boundaries that should migrate together

If the thesis is implemented honestly, the slice should update **all**
current `supportScore(...)` consumers, not just the two already-patched
sites.

Recommended same-slice migration set:

1. `maybeRerankResultsDetailed(...)`
   - `PackRepository.java:945-1003`
2. `buildGuideAnswerContext(...)` support-candidate list
   - `PackRepository.java:769-797`
3. `buildAnchorGuideScores(...)`
   - `PackRepository.java:2954-3012`
4. Ranked-vs-routed anchor tiebreak
   - `PackRepository.java:2466-2468`
5. `selectExplicitWaterDistributionAnchor(...)`
   - `PackRepository.java:2522-2564`
6. `selectExplicitWaterStorageAnchor(...)`
   - `PackRepository.java:2567-2681`
7. `selectSpecializedStructuredAnchor(...)`
   - `PackRepository.java:2684-2730`
8. `selectBroadHouseAnchor(...)`
   - `PackRepository.java:2733-2779`
9. `findRouteFocusedAnchor(...)`
   - `PackRepository.java:3407-3440`

If those do **not** all migrate together, the codebase will still have a
mixed "sometimes metadata is fused, sometimes metadata is explicit"
contract and the architectural goal will not have been achieved.

### 4.4 What should stay separate

These should **not** be forced into the refactor:

- Route-row scoring at `PackRepository.java:1885-1889` and
  `2236-2240` already uses metadata explicitly.
- Keyword-hit scoring at `PackRepository.java:3821-3823` already uses
  metadata explicitly.
- Hybrid RRF at `PackRepository.java:3989-4037` is a different scoring
  family.
- `QueryRouteProfile.metadataBonus(...)` at
  `QueryRouteProfile.java:1173-1215` is a separate API.
- `OfflineAnswerEngine.rerankWithSessionHints(...)` at
  `OfflineAnswerEngine.java:849-926` is intentionally a different
  downstream scoring system and already applies its own weight (`* 2`).

## 5. Risk analysis

### 5.1 Existing tests most likely to break

1. **Rerank telemetry tests**
   - `PackRepositoryTelemetryTest.java:159-288`
   - These lock the current vector/lexical rerank semantics and telemetry
     derivation.
   - If the refactor changes helper structure but preserves score
     outputs, these should remain green with minimal edits.

2. **Anchor-score tests**
   - `PackRepositoryTest.java:2387-2516`
   - These pin the R-anchor1 compensation path.
   - A refactor that removes the compensation branch in favor of a shared
     helper will likely require test rewrites, but not behavior changes.

3. **Specialized anchor chooser tests**
   - `PackRepositoryTest.java:2306-2384`
   - These assert selected anchors for water-distribution and house
     paths.
   - If vector rows now get full metadata credit in chooser code,
     outcomes could legitimately shift.

4. **Offline answer-mode tests**
   - `OfflineAnswerEngineTest.java:2483-2582`
   - This test explicitly proves selected context can override off-topic
     raw top rows.
   - If `buildGuideAnswerContext(...)` starts admitting vector support
     candidates that are currently dropped, answer-mode outcomes may
     shift.

5. **Session-memory / follow-up tests**
   - Not directly in the current grep set, but any fixture that depends
     on anchor choice or selected context ordering could move once the
     unpatched support boundaries start honoring vector metadata.

### 5.2 Tests that should be added

The current tree only pins two of the supportScore boundaries. The
refactor should add invariant tests for the rest.

Recommended additions:

1. A **single boundary inventory test** that enumerates every
   support-boundary helper and asserts that for equivalent lexical vs
   vector rows, `metadataBonus` contributes exactly once at each
   boundary.

2. A `buildGuideAnswerContext(...)` test proving a metadata-matched
   vector support row can survive the support-candidate ranking step.

3. A ranked-vs-routed anchor tiebreak test proving a vector ranked anchor
   is not reduced to `1` while the routed lexical row keeps full support.

4. One test each for:
   - `selectExplicitWaterDistributionAnchor(...)`
   - `selectExplicitWaterStorageAnchor(...)`
   - `selectSpecializedStructuredAnchor(...)`
   - `selectBroadHouseAnchor(...)`
   - `findRouteFocusedAnchor(...)`

5. A non-regression test proving non-vector rows still receive
   metadataBonus exactly once.

6. An `OfflineAnswerEngineTest` regression proving the resulting selected
   context still yields the expected confident / uncertain / abstain mode
   for at least one current shelter-family fixture.

### 5.3 User-visible behavior changes to expect

If the refactor is done completely, the following are plausible and
should be treated as expected blast radius, not surprise regressions:

- Vector rows may appear more often in **supporting context**, not just
  in rerank order and anchor selection.
- Specialized anchor choosers may favor vector rows more often when
  those rows have strong metadata alignment.
- Some route-focused queries may choose a different anchor because the
  ranked-vs-routed comparison stops silently flattening vector support to
  `1`.
- `OfflineAnswerEngine.resolveAnswerMode(...)` may flip on some fixtures
  because selected context quality changes downstream.
- Gallery outcomes that currently tolerate `uncertain_fit` via R-gal1
  may turn `confident` more often if stronger context reaches the answer
  stage.

### 5.4 Places where non-uniform application appears intentional

I did **not** find evidence that any current `supportScore(...)`
consumer intentionally wants metadata weight `0`.

What I did find are adjacent scoring families that are intentionally
different and should stay different:

- Route-search scoring already composes metadata explicitly
  (`PackRepository.java:1885-1889`, `2236-2240`).
- Session-hint rerank intentionally weights metadata at `2x`
  (`OfflineAnswerEngine.java:862-869`).
- Confidence / answer-mode gating uses metadata as threshold evidence,
  not ranking (`OfflineAnswerEngine.java:1168-1279`).
- `supportCandidateMatchesRoute(...)` uses `metadataScore` as a filter,
  not as a ranking term (`PackRepository.java:4568-4665`).

So the real "uniformity" target should be:

- **uniform within the `supportScore` boundary family**
- **not** uniform across every score-like quantity in the app

## 6. Draft acceptance criteria

The later slice should use invariant-form gates, not only example-form
tests.

1. `PackRepository.supportScore(...)` no longer embeds
   `QueryMetadataProfile.metadataBonus(...)`.

2. Vector rows no longer return total support `0`; instead they return a
   support breakdown with lexical contribution `0` and separately
   available metadata contribution.

3. Every current `supportScore(...)` consumer in `PackRepository`
   consumes metadata explicitly and exactly once:
   - `buildGuideAnswerContext(...)`
   - `maybeRerankResultsDetailed(...)`
   - ranked-vs-routed anchor tiebreak
   - explicit water-distribution anchor chooser
   - explicit water-storage anchor chooser
   - specialized structured anchor chooser
   - broad-house anchor chooser
   - `buildAnchorGuideScores(...)`
   - `findRouteFocusedAnchor(...)`

4. No support-boundary still contains a vector-only metadata patch branch
   after the refactor; the shared support helper makes those branches
   unnecessary.

5. Non-vector rows still receive exactly one metadata application at each
   migrated boundary.

6. Route-search scoring, keyword-hit scoring, hybrid RRF, and
   `OfflineAnswerEngine.rerankWithSessionHints(...)` are behaviorally
   unchanged unless explicitly named in the slice.

7. `RerankedResult.baseScore` / `finalScore` telemetry semantics remain
   stable enough that existing candidate telemetry still reports the same
   user-facing fields, or the slice explicitly updates the telemetry
   contract and tests in one move.

8. At least one regression test proves that a metadata-matched vector row
   can survive **context support-candidate** ranking, not only rerank and
   anchor scoring.

## 7. Blockers for slice draft

1. **Scope decision: include all support boundaries or not?**
   - The code evidence says "yes" if this is truly an architectural fix.
   - A narrower slice that only rewrites rerank + anchor will not finish
     the job.

2. **Behavior intent for `buildGuideAnswerContext(...)`**
   - Current code clearly drops vector support rows
     (`PackRepository.java:778-780`).
   - The planner should confirm that changing this is desired, because it
     is the largest likely user-visible shift the refactor would make.

3. **Telemetry contract for rerank**
   - `RerankedResult` currently stores `metadataBonus`, `baseScore`, and
     `finalScore` with the existing derivation
     (`PackRepository.java:5892-5910`).
   - The slice should decide whether to preserve that exact derivation or
     introduce a clearer "pre-guide-bonus" vs "post-guide-bonus"
     breakdown.

4. **Coverage strategy for specialized anchor choosers**
   - There are enough chooser functions that the slice should decide
     upfront whether to add one table-driven test helper or a small set
     of targeted fixture tests.

5. **Naming correction in the eventual slice doc**
   - The dispatcher should not say "move metadataBonus out of
     `OfflineAnswerEngine.supportScore`" because that helper is in
     `PackRepository`.

## 8. Known wrongs

These are the parts of the current framing that I believe are wrong or
need narrowing.

1. **`supportScore` is not in `OfflineAnswerEngine`.**
   - It is in `PackRepository.java:3651-3680`.

2. **`R-gal1` is not part of the metadataBonus family.**
   - It only changes androidTest wording tolerance at
     `PromptHarnessSmokeTest.java:2846-2849`.

3. **`R-gate1` is adjacent, but not the same shape.**
   - It repaired entry into the rerank phase, not metadata composition at
     a support boundary.

4. **The deeper root cause is slightly broader than
   "metadataBonus lives inside supportScore."**
   - The real issue is that `supportScore(...)` both:
     - fuses multiple support components into one opaque number, and
     - zeroes out vector rows before later components can apply.
   - Moving `metadataBonus` out is the right first move because it
     exposes the hidden term, but the refactor should really produce a
     shared support breakdown, not just a bare helper rename.

5. **There is still at least one live unpatched boundary today.**
   - `buildGuideAnswerContext(...)` support-candidate ranking at
     `PackRepository.java:769-797` still drops vector rows by consuming
     raw `supportScore(...)`.

## 9. Suggested refined thesis for the planner

If the planner wants a one-sentence version that matches the code more
closely, use this:

> Inside `PackRepository`, replace opaque `supportScore(...)` consumption
> with a shared support breakdown so lexical support and metadata support
> are computed separately, vector rows contribute lexical `0` rather than
> total support `0`, and every support-boundary applies metadata
> explicitly and exactly once.

## 10. Slice sizing

Eventual refactor slice estimate:

- **Files:** 4 likely
  - `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
  - `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
- **Lines:** roughly 250-400 net
  - production helper extraction + call-site rewiring
  - test additions for the unpatched boundaries
- **Tests:** roughly 10-16 touched or added
  - 7-10 new boundary invariants
  - 3-6 existing assertion updates if anchor/context behavior legitimately shifts

That is bigger than `R-ret1c` or `R-anchor1`, but still plausibly a
single bounded slice if the dispatcher keeps it to `PackRepository` plus
the immediate tests.

# R-anchor1 forward research — anchor-selection symmetry for vector rows (post-R-ret1c)

Written 2026-04-20 late evening. Research pass before drafting the slice.
R-ret1c landed rerank symmetry; R-anchor1 extends the same symmetry to the
anchor-selection stage, which is the next downstream gate.

## 1. Blocker shape

From `artifacts/postrc_rret1c_20260420_205801/summary.md` (logcat
2026-04-20 20:59:59.870):

- Reranked top-4 post-R-ret1c:
  1. GD-345 | Overview | `finalScore=50` | `retrievalMode=vector`
  2. GD-727 | Practical Survival Applications | `finalScore=37` | `retrievalMode=lexical`
  3. GD-687 | 3. Burial Practices | `finalScore=36` | `retrievalMode=lexical`
  4. GD-294 | Seasonal Climate Management | `finalScore=31` | `retrievalMode=vector`
- Context-selection log: `anchorGuide="GD-727" diversify=false guideSections=4 finalContext=4`
- Net: rerank ranks shelter content correctly, anchor ignores it.

## 2. Where anchor selection lives

All references `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`.

| Symbol | Line | Role |
|---|---:|---|
| `context query=...` Log.d emission | 876 | Where logcat `anchorGuide=` comes from |
| `selectAnswerAnchor(QueryTerms, List<SearchResult>)` | 2396 | Top-level anchor picker |
| `buildAnchorGuideScores(QueryTerms, List<SearchResult>, boolean)` | 2946 | Scoring loop that builds `GuideScore` map |
| `supportScore(QueryTerms, SearchResult)` | 3626 | Per-row score; early-returns 0 for vector rows |
| `isVectorRetrievalMode(String)` | 1016 | R-ret1c helper; reusable here |

## 3. Decision-tree trace for rain-shelter

1. `selectAnswerAnchor(queryTerms, rankedResults)` called with post-rerank results.
2. `buildAnchorGuideScores(queryTerms, rankedResults, false)` iterates rankedResults.
3. For GD-345 (vector, rank 1):
   - `isSpecializedExplicitAnchorCandidate` → true (not a water-distribution special case).
   - `supportScore(queryTerms, result)` → hits line 3626-3629:
     ```java
     String retrievalMode = emptySafe(result.retrievalMode).toLowerCase(QUERY_LOCALE);
     if ("vector".equals(retrievalMode)) {
         return 0;
     }
     ```
   - Returns 0.
   - Line 2958 `if (support <= 0) { continue; }` — GD-345 dropped from the guide map.
4. For GD-727 (lexical, rank 2):
   - `supportScore` runs the full body (lexical + metadataBonus + specialized topic + section + structure penalty) → positive.
   - Added to guide map with `totalScore`, `bestScore`, `sectionKeys`, `anchor`.
5. For GD-687 (lexical, rank 3): same path, added to map.
6. For GD-294 (vector, rank 4): same as GD-345 — dropped.
7. No route-focus, no explicit water-distribution or water-storage match, no broad-house anchor.
8. Line 2431 sorts by `totalScore`, then `sectionKeys.size()`, then `bestScore`.
9. GD-727 wins because GD-345/GD-294 never entered the map. `anchor = GD-727`.

## 4. Why the asymmetry exists

The rerank loop (post-R-ret1c) and anchor-selection loop both consume
`SearchResult` rows and both rely on `supportScore` as a scoring input,
but they handle the vector early-return differently:

| Stage | Treatment of `supportScore == 0` for vector row |
|---|---|
| Rerank loop (`:956-978` post-R-ret1c) | Adds `metadataBonus` directly to `score` via `isVectorRetrievalMode` branch at line 970, so metadata signal reaches `finalScore` sort key. |
| Anchor loop (`:2946-2992`) | Line 2958 continues past the row. Metadata signal never contributes to `GuideScore.totalScore`. |

Same architectural bug as R-gate1/R-ret1c: a vector row with strong
metadata match is invisible to a scoring stage because the gate that
opens the metadata pathway fires at the wrong level.

## 5. Remediation options

### Option A (recommended) — symmetric vector-path in anchor loop

Mirror R-ret1c's rerank-loop pattern inside `buildAnchorGuideScores`.
When `supportScore` returns 0 for a vector row, compute `metadataBonus`
directly and use it as the support signal.

```java
int support = supportScore(queryTerms, result);
if (support <= 0 && isVectorRetrievalMode(result.retrievalMode)) {
    // R-anchor1: vector rows' supportScore early-returns 0 (line 3628-3629).
    // Mirror the rerank-loop treatment so metadata-matched vector rows
    // can compete for anchor selection alongside lexical rows.
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

- **Target:** `PackRepository.java:2957-2960`.
- **Blast radius:** low. Only vector rows with positive metadataBonus enter the map; zero-bonus vector rows still drop.
- **Signature changes:** none.
- **Symmetry:** matches R-ret1c's fix conceptually (vector row's metadata bonus reaches the scoring stage) without requiring a larger architectural change.
- **Risk:** a vector row with strong metadata match becomes anchor-eligible. For rain-shelter this is correct (GD-345 has `structure_type=emergency_shelter` matching the query profile). For other query shapes, the retrieval modes that are vector-dominated (semantic-only queries) will now have their metadata signal considered. Expected benign; worth watching in test suite.

### Option B — thread RerankedResult.finalScore through anchor-selection

Pass the RerankedResult list (with finalScore) through
`buildGuideAnswerContext` → `selectAnswerAnchor` → `buildAnchorGuideScores`.
Use finalScore for vector rows as the support signal.

- **Blast radius:** medium. Multiple signature changes.
- **Advantage:** anchor selection literally reuses the rerank verdict.
- **Disadvantage:** wider surface; harder to unit-test the helper in isolation.
- **Defer unless Option A has a weight-tuning problem.**

### Option C — remove the vector early-return in `supportScore`

Compute lexical+metadata+specialized+section for vector rows too.
Cleanest architecturally but wide-ranging: `supportScore` is called
from many sites, and vector-mode rows behave differently downstream.
Deferred.

## 6. Test surface inventory

Direct coverage today:

- `PackRepositoryTest.java:2372` `anchorGuideBudgetForTest` — diversification budget, not selection.
- No direct tests of `selectAnswerAnchor` or `buildAnchorGuideScores`.

What R-anchor1 needs:

1. Happy path: vector row with positive metadataBonus enters anchor map and wins against lexical row with weaker combined score.
2. Negative: vector row with zero metadataBonus still drops (no regression on unqualified vector rows).
3. Parity: lexical row anchor-scoring unchanged (no double-count).
4. Edge: empty retrievalMode row treated as non-vector (defensive, mirrors R-ret1c test 4).

If `buildAnchorGuideScores` isn't directly testable, extract to a static helper that operates on `(queryTerms, rankedResults, requireDirectSignal)` and returns the guide map — same refactor pattern as `shouldApplyMetadataRerank` used in R-gate1.

## 7. Architectural constraints / don't-break list

- Anchor identity feeds `loadGuideSectionsForAnswer` (line 2743) → first context row in prompt.
- `shouldRequireDirectAnchorSignal` at line 2998 gates a second pass with stricter signal. A vector row that now qualifies via metadataBonus must also satisfy `hasDirectAnchorSignal` when the stricter path fires. Verify the direct-signal predicate accepts metadata-matched vector rows or document the gap.
- No evidence that anchor identity affects rerank, follow-up suggestions, or DetailActivity UI beyond the `anchorChip` display label.
- Session-history `anchorPrior` at OfflineAnswerEngine.java:262 is a different concept (prior-turn anchor) — not coupled.

## 8. Unknowns / probe candidates after landing

- **De-risked post-research (2026-04-20 late evening):** Does GD-345 pass `hasDirectAnchorSignal` under the stricter-signal branch? **Verdict GREEN.** `shouldRequireDirectAnchorSignal` at `PackRepository.java:2998` fires for rain-shelter via its third OR branch (non-route-focused, ≥2 primary keyword tokens / `hasExplicitTopicFocus`). `hasDirectAnchorSignal` body then evaluates: query has `hasExplicitTopicFocus=true` (via "weatherproofing" topic triggered by "rain"); GD-345's `topicTags="site_selection,weatherproofing"` overlaps the query's explicit topics at line 3080; `preferredStructureType="emergency_shelter"` is NOT in `requiresSpecializedRouteAnchorSignal`'s list (line 3006-3014); fallthrough hits `return true` at line 3106. GD-345 passes. GD-294 (topicTags="ventilation") would FAIL the overlap predicate but is not the primary target.
- Does the rain-shelter query's `context.selected` change post-landing, or does anchor move but selection still picks GD-727 rows via other logic (e.g., `diversify=false` still pulls 4 GD-727 sections)?
- After R-anchor1 lands, does `ask.generate mode` flip from `uncertain_fit` to `confident`? The Option C post-RC trust-spine assertion in `PromptHarnessSmokeTest.java:2794` (R-gal1 territory) may close as a side-effect if mode settles to `confident`.
- Do other post-RC queries (violin-bridge, poisoning) see anchor shifts once vector rows become anchor-eligible? Watch for any regression in R-val2's strict settle harness.

## 9. Precedent and pattern

R-anchor1 is the third fix in the post-R-gate1 chain:

| Slice | Target stage | Vector-row treatment introduced |
|---|---|---|
| R-gate1 (`f3b2c68`) | Rerank pathway gate | Rerank fires when structure type is set, not just route-focus |
| R-ret1c (`2ec77b8`) | Rerank sort key | Vector row's metadataBonus reaches `finalScore` |
| R-anchor1 (proposed) | Anchor scoring | Vector row's metadataBonus reaches `GuideScore.totalScore` |

Same pattern: identify where a vector row's metadata signal is dropped; add a scoped branch that mirrors the non-vector path; keep the fix narrow and reversible.

## 10. Dispatch shape recommendation

Single main-lane worker, serial. One commit (Option A code change + 3-4 unit tests). APK rebuild + single-serial re-probe on 5556 using T5's host-inference args. Expected outcome: `anchorGuide=GD-345` (or GD-294) in logcat, `context.selected` starts with shelter-family rows, `ask.generate mode` may flip to `confident` as a downstream effect.

Acceptance: anchor logcat shows shelter content; unit tests lock in the vector-path scoring symmetry.

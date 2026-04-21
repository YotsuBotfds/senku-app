# R-anchor2 forward research — context-assembly symmetry (post-R-anchor1, speculative)

> Closing status (2026-04-20 night): Probe evidence from `R-anchor1`
> on 5556 matched the low-risk scenario - `anchorGuide` flipped to
> GD-345 and `context.selected` became shelter-dominant (`3x GD-345 +
> 1x GD-727`). `R-anchor2` is not needed at this time; keep the slice
> on hold only if future retrieval queries show anchor flips without
> `context.selected` following.

Written 2026-04-20 late evening. Speculative forward research for the
stage downstream of R-anchor1. Slice only needed if R-anchor1's probe
shows `anchorGuide` flips to GD-345 but `context.selected` still pulls
mostly GD-727 rows.

## 1. Entry method

`PackRepository.java:727` — `buildGuideAnswerContext(String rawQuery, List<SearchResult> rankedResults, int limit)`.

Execution flow:

| Line | Action |
|---:|---|
| 738 | `selectAnswerAnchor(queryTerms, rankedResults)` picks anchor |
| 743 | `loadGuideSectionsForAnswer(anchor, queryTerms)` fetches anchor's sections |
| 755 | `int anchorGuideBudget = anchorGuideBudget(queryTerms, diversifyContext, limit)` |
| 756 | `boolean seedAnchorBeforeSupport = shouldSeedAnchorBeforeSupport(...)` |
| 757-767 | Fill up to `anchorBudget` slots with anchor-guide sections |
| 769-797 | Build `supportingCandidates` from `rankedResults[1..N]` (skip anchor-guide rows) |
| 822-833 | Fill remaining slots from sorted supporting candidates |
| 847-855 | Fallback refill from anchor sections if slots remain |
| 876-882 | Log `anchorGuide=... diversify=... guideSections=... finalContext=...` |

## 2. Anchor budget formula

`PackRepository.java:890` — `anchorGuideBudget(...)`:

- `diversify=false` → `Math.max(1, limit - 1)` = 3 (when limit=4).
- `diversify=true` → different logic (bypassed for emergency_shelter — see below).

`emergency_shelter` does not trigger `diversifyContext=true`, so
anchorBudget=3 for the rain-shelter path.

## 3. Current trace (pre-R-anchor1)

Anchor = GD-727 Practical Survival Applications (lexical, rank 2 post-R-ret1c).
`guideSections` loaded from GD-727 probably ≥ 4 (3-5 rows, guide-focus retrievalMode).

Assembly:

1. Slot 1 = GD-727 anchor section (Practical Survival Applications).
2. Slots 2-3 = GD-727 additional sections from anchorBudget=3 (two more GD-727 guide-focus rows).
3. Slot 4 = top-scored supporting candidate = GD-687 (lexical, `supportScore`>0; vector rows GD-345/GD-294 return 0 from supportScore and are excluded).

Matches the observed `3× GD-727 + 1× GD-687` in R-ret1c logcat.

## 4. Post-R-anchor1 projection

Anchor = GD-345 Overview (vector, rank 1 post-R-ret1c + R-anchor1).

How many sections does GD-345 have in the pack? **UNKNOWN** without
pack probe. Candidates:

- If GD-345 has ≥3 sections: slots 1-3 = GD-345 sections. Slot 4 = top supporting candidate (likely GD-727's lexical row). Net: `3× GD-345 + 1× GD-727`. Good shelter dominance.
- If GD-345 has 1-2 sections: slots 1-2 = GD-345. Slots 3-4 = supporting candidates. If supporting candidates rank GD-687 > GD-727 (possible — GD-687's `supportScore`=25 per R-ret1c trace), slot 3 = GD-687, slot 4 = next candidate. Net: `2× GD-345 + GD-687 + GD-?`. Moderate shelter dominance but mixed content.

## 5. Breakage scenarios

### Scenario A: GD-345 has only 1 section (Overview only)

Anchor-budget undershoot. Slots 2-4 pull from supportingCandidates.
`supportingCandidates` is built from `rankedResults[1..N]` filtered by
`supportScore > 0`, which still excludes GD-294 (vector, rank 4) and
any other vector rows. Only lexical rows enter.

Net: slot 1 = GD-345 Overview + slots 2-4 = lexical supporting candidates (GD-687, GD-353, GD-027 per the R-ret1c logcat). Shelter presence reduced to 1/4.

**Likelihood:** Medium. GD-345 is "Emergency Shelter" — plausibly has multiple sections, but unknown without pack probe.

### Scenario B: `hasDirectAnchorSignal` stricter-pass branch

`shouldRequireDirectAnchorSignal` fires for rain-shelter (≥2 primary keyword tokens OR explicit topic focus). The stricter second pass calls `buildAnchorGuideScores(queryTerms, rankedResults, true)`. `requireDirectSignal=true` at line 2961-2963 filters rows via `hasDirectAnchorSignal`.

**Resolution:** de-risk probe (separate research) verified that GD-345 passes `hasDirectAnchorSignal` because rain-shelter's explicit topic "weatherproofing" overlaps GD-345's topic tags. **Not a blocker.**

### Scenario C: Supporting-candidate sort order

Lines 784-796 sort `supportingCandidates` by score then retrieval-mode
rank. Vector rows get `supportScore=0` and are dropped before the sort.
GD-294 (vector rank 4) never enters supportingCandidates.

Net: even post-R-anchor1, supporting candidates are lexical-only. If
anchor is GD-345 and anchorBudget undershoots, fill comes from lexical
rows regardless of reranked order.

**Likelihood:** This is Scenario A's deeper cause. If GD-345 has
enough sections, it doesn't matter.

### Scenario D: Hidden diversify-false per-guide preference

No evidence in the code. Supporting-candidate filtering at `:772`
already drops same-guide-as-anchor rows. Rejected as a risk.

## 6. Remediation options if R-anchor2 is needed

### Option A — widen anchor budget for emergency_shelter

Target: `PackRepository.java:890-900` `anchorGuideBudget`.

```java
private static int anchorGuideBudget(QueryTerms queryTerms, boolean diversifyContext, int limit) {
    if (!diversifyContext) {
        String preferredStructure = queryTerms != null && queryTerms.metadataProfile != null
            ? queryTerms.metadataProfile.preferredStructureType()
            : "";
        if ("emergency_shelter".equals(preferredStructure)) {
            return limit;  // Use all 4 slots for anchor
        }
        return Math.max(1, limit - 1);
    }
    // ... existing else branches
}
```

Net: slots 1-4 all try to fill from anchor. Fallback at `:847-855`
(refill from anchor sections) takes over if anchor has few sections.

- Risk: low. Only affects emergency_shelter queries. If anchor has few sections, fallback refill still works.
- Blast radius: narrow — emergency_shelter-specific.
- Test surface: `anchorGuideBudgetForTest` at `:886` already exists.

### Option B — extend supporting-candidate eligibility to include vector rows with metadata match

Target: `PackRepository.java:770-797` supporting-candidate loop.

Same shape as R-anchor1's fix: where `supportScore(candidate)` returns
0 for a vector row, compute `metadataBonus` as fallback. Allows
shelter-family vector rows (GD-294) to enter supporting candidates.

- Risk: medium. Wider scope than Option A; affects all queries with
  vector rows, not just emergency_shelter.
- Blast radius: larger — any vector-retrieval query could see shifts.
- Mirrors R-anchor1's pattern exactly.

### Option C — structure-type-aware supporting-candidate sort

Target: `PackRepository.java:784-796` comparator.

When `preferredStructureType == "emergency_shelter"`, boost sort
priority for candidates with matching structure_type.

- Risk: medium. Changes scoring semantics.
- More surgical than Option B.

## 7. Verdict

R-anchor2 is **conditionally MEDIUM risk**. Pre-draft kept on standby;
don't dispatch until R-anchor1's probe evidence answers the key unknown.

### Decision gate (after R-anchor1 probe)

From `artifacts/postrc_ranchor1_<ts>/summary.md`:

- **If `context.selected` shows ≥2 GD-345 rows + ≥1 shelter-family supporting row (GD-294/GD-446):** R-anchor2 NOT needed. Close as "self-resolved by anchor flip."
- **If `context.selected` shows 1× GD-345 + 3× lexical non-shelter rows:** R-anchor2 Option A (widen budget). Anchor undershoot is the problem.
- **If `context.selected` shows 0× GD-345 (anchor didn't flip):** not an R-anchor2 problem; check R-anchor1 Step 4 STOP condition diagnostics.

## 8. Unknowns to resolve live

1. GD-345's section count in the pack. Proxy signal: `guideSections=N` in logcat after R-anchor1 lands. N=1 → Scenario A fires. N≥3 → likely OK.
2. Does `hasDirectAnchorSignal` accept GD-345? De-risk research verified YES (topic-overlap passes). Not a live unknown anymore.
3. Post-R-anchor1 `context.selected` row composition. Only answered by probe.

## 9. Precedent and pattern

Third potential fix in the post-R-gate1 chain:

| Slice | Target stage | Vector-row treatment |
|---|---|---|
| R-gate1 | Rerank pathway gate | Fires for structure-typed queries |
| R-ret1c | Rerank sort key | metadataBonus reaches finalScore |
| R-anchor1 | Anchor scoring | metadataBonus reaches GuideScore.totalScore |
| R-anchor2 (speculative) | Context assembly | Two sub-options: widen budget OR extend vector eligibility downstream |

Same architecture lesson: identify where vector rows' metadata signal
is dropped; add a scoped branch; keep narrow and reversible.

## 10. Dispatch shape (if needed)

Single main-lane worker. One commit. If Option A: trivial 5-line change
in `anchorGuideBudget` + 1 unit test. If Option B: mirrors R-anchor1's
structure, 4-test coverage. Probe on 5556, same args as R-anchor1.

Acceptance: `context.selected` in logcat shows ≥2 shelter-family rows.

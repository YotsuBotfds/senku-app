# Scout prompt v3 — R-anchor-refactor1 audit (round 5, post-round-4 fixes)

Paste to a Spark `gpt-5.3-codex-spark xhigh` session (read-only). Output: GO / GO-WITH-EDITS / HOLD with BLOCKING/SUGGESTION findings. Scout does NOT run Gradle, edit files, or dispatch.

## Context — round 5 after four HOLDs

Four prior audit rounds have walked architectural design, framing consistency, counts, test-lane viability, seam visibility, fixture-design correctness, and internal-prose consistency. Each found real issues; the slice has been substantially rewritten in response. By this round, the design surface is locked. **The remaining risk is implementation-detail accuracy**: does the slice's prose actually match current `PackRepository.java` code, and will Codex be able to execute the migration exactly as described?

**Confirmed clean across rounds 1-4 (DO NOT re-examine unless you have direct contradicting evidence):**

- Line anchors for all 10 `supportScore(...)` call sites across 9 boundaries; method definition at `:3651-3680`.
- `QueryMetadataProfile.metadataBonus(...)` at `QueryMetadataProfile.java:762-835` is the right bonus.
- Route-row, keyword-hit, hybrid-RRF, session-hint rerank are out-of-family.
- `R-tool2` landed at `38b7826`; no concurrency; gate anchor clean (target files untouched since `6f9e07b`).
- 10 migrated call sites; 9 new tests (7 chooser/boundary + 1 telemetry + 1 engine); 3 new `ForTest` seams; 2 removed methods (`supportScore`, `supportScoreForTest`); 2 deleted compensation branches; 1 updated existing test.
- `SupportBreakdown` must be **package-private** `static final class` (matches existing `GuideScore` / `RerankedResult` at `:5880` / `:5892`). NOT `private`.
- `rankSupportCandidatesForTest(String query, SearchResult anchor, List<SearchResult> ranked)` — explicit `anchor` arg because production excludes same-guide rows at `:772-773`.
- Telemetry fixtures `vectorRowsApplyMetadataBonusToSortKey` (`:159-199`) and `vectorRowBaseScoreStillReflectsDisplayOnlyDerivation` (`:203-229`) expected unchanged (zero-valued shifting components verified round 4).
- No hidden consumers of deleted compensation branches (logs / telemetry / thresholds — all verified clean round 4).
- Acceptance checklist covers all new/removed items (verified round 4).
- Decision 2 locked: Option B, vector scoring legitimately changes. Framing: "superseded by broader credit."
- Atomic single commit locked. `supportScore` removal (no shim) locked. JVM-lane + seam approach locked. `SupportBreakdown` five-field shape locked.

**Your job this round: attack implementation-detail accuracy. 6 fresh angles.**

---

## Angle 1 — Step 2 before/after snippet accuracy

Each of the 9 boundary sections in Step 2 includes a "Before" code block purporting to summarize current PackRepository code. Prior rounds verified line anchors (ranges are correct) but did NOT verify the "Before" PROSE actually matches current code at those ranges.

For each boundary (all 10 call sites across 9 boundaries — see `:769-797`, `:945-1003`, `:2444-2468` × 2, `:2522-2564`, `:2567-2681`, `:2684-2730`, `:2733-2779`, `:2954-3012`, `:3407-3440`):

- Read the actual code in PackRepository.java at that range.
- Compare to the slice's "Before" snippet.
- Check for drift: variable names, flow control, `Math.max(...)` clamp values, early returns, null guards, any secondary logic the slice's Before ignores.
- **Flag BLOCKING** if the slice's Before misrepresents the current code materially (Codex will follow slice's imagined flow and produce a wrong diff).
- **Flag SUGGESTION** for minor drift (comment formatting, whitespace, renamed locals).

Cite each finding by `<file:line-range>` and show the slice's claim vs the actual code in 1-2 lines.

## Angle 2 — `@VisibleForTesting` annotation claim

The slice says new seams use `@VisibleForTesting static` and calls this "existing convention." Verify:

- `grep -n "@VisibleForTesting" android-app/app/src/main/java/com/senku/mobile/PackRepository.java`.
- If zero hits: the existing 30+ `ForTest` methods are plain `static` (default package-private). The slice should drop `@VisibleForTesting` from its seam spec — Codex should match existing pattern.
- If nonzero hits: which import is used (`androidx.annotation.VisibleForTesting` vs `com.google.common.annotations.VisibleForTesting` vs local)? Does `PackRepository.java` already have the import?

Flag SUGGESTION if the annotation claim mismatches existing code — easy fix, but annotating inconsistently mid-file is a smell Codex should avoid.

## Angle 3 — Test fixture constructibility for new tests

The 9 new tests construct `SearchResult` objects with enough metadata to trigger `metadataBonus`, `specializedExplicitTopicBonus`, `sectionHeadingBonus`, and `structurePenalty`. Verify this is actually possible via the available SearchResult constructor(s):

- Grep `new SearchResult(` across `PackRepositoryTest.java`, `PackRepositoryTelemetryTest.java`, `OfflineAnswerEngineTest.java`. What arity/field-set does the existing pattern use?
- Can a test set `retrievalMode = "vector"` directly via constructor?
- Can a test set a `sectionHeading` value that `QueryMetadataProfile.sectionHeadingBonus(...)` at `:395-520` treats as bonused? (Scout Angle 3 in round 4 noted "`Inventory` heading is not picked up" — identify which headings ARE.)
- Can a test set `topic_tags` / `structure_type` values that trigger `specializedExplicitTopicBonus` at `:3687-3690`?

If the existing `SearchResult` constructors don't accommodate the fixture fields the new tests require, flag SUGGESTION — slice should either extend the test-fixture pattern or pick fixture shapes compatible with current constructors. List the specific constructor pattern tests should use.

## Angle 4 — Stale comments in migrated boundaries

Grep `PackRepository.java` at the 9 boundary ranges for comments encoding pre-refactor behavior. Trigger words: `"vector"`, `"compensation"`, `"metadata"` (in explanatory context, not variable names), `"R-ret1c"`, `"R-anchor1"`, `"fallback"`, `"reinjection"`, "support <= 0", "drop", "silently".

These comments become misleading or outright wrong after the refactor. Codex following the slice's Step 2 without updating comments produces a file where the code says one thing and the comment says the old thing.

Report every stale-comment line. Flag SUGGESTION listing each — slice should add a Step 2 instruction: "update comments at each migrated boundary to reflect the shared-breakdown behavior."

## Angle 5 — Null / empty-input invariants through the new seams

The new seams accept test-supplied input that may violate production invariants. Check:

- In production `buildGuideAnswerContext(...)`, what happens when anchor selection returns null? Read `:738` and the anchor chain at `:2402-2468`. Does downstream code short-circuit, or does it gracefully handle a null anchor? Does the same-guide exclusion at `:772-773` assume non-null anchor?
- If production handles null anchor, should `rankSupportCandidatesForTest` handle `anchor = null` too (so tests can exercise the no-anchor path)? Or should the seam assert non-null?
- `supportBreakdown(queryTerms, result)`: does it tolerate null `retrievalMode` on the SearchResult? Null `sectionHeading`? Null metadata fields? Production code today via `supportScore(...)` — does it have null guards?
- If `ranked` is empty, does `rankSupportCandidatesForTest` return empty list cleanly, or throw?

Flag BLOCKING if a likely production invariant breaks under test-supplied null/empty inputs and the slice doesn't address it. Flag SUGGESTION if the seam contract needs explicit null-handling documentation.

## Angle 6 — Non-vector retrieval modes beyond "vector"

The slice treats retrieval mode as binary: `"vector"` vs non-vector. Production may use other modes. Check:

- Grep `retrievalMode.equals(` or `"vector".equals(retrievalMode)` or `isVectorRetrievalMode(` in PackRepository.java — how is "vector" identified today?
- What other retrieval modes appear in the codebase? (Grep `retrievalMode =` assignments and `SearchResult` constructors passing retrievalMode values.) Candidates: `"bm25"`, `"fts"`, `"keyword"`, `"hybrid_rrf"`, `"route"`, `"guide-focus"`, `"route-focus"`, etc.
- For each non-"vector" mode: does `lexicalKeywordScore(queryTerms, ...)` return a meaningful non-zero? Or does some non-"vector" mode also silently get `0` today?
- If any non-"vector" mode returns `0` from `lexicalKeywordScore`, the current code has a hidden asymmetry the refactor must either preserve (if intentional) or fix (if incidental). The slice's binary framing may miss this.

Flag BLOCKING if the retrieval-mode coverage is incorrect (refactor changes non-"vector" non-"vector" mode behavior incorrectly). Flag SUGGESTION if the slice should explicitly enumerate which modes follow which branch.

---

## Output format

Return one of:

- **GO**: no BLOCKING; at most 2 SUGGESTIONs.
- **GO-WITH-EDITS**: 3-5 small adjustments the planner should fold.
- **HOLD**: one or more BLOCKING issues.

Structure each finding:

```
[BLOCKING | SUGGESTION] <short title>
Evidence: <file:line or grep output — quote the text>
Why it matters: <one-line rationale>
Proposed fix: <concrete action>
```

Per-angle confirmation required — for each of Angles 1-6, include at minimum a one-line "clean" or "[SUGGESTION]/[BLOCKING] ...". Silence looks like omission.

Report ≤ 1000 words. For Angle 1 (nine boundaries), a compact per-boundary one-liner is acceptable if clean; full detail only on drift.

## Anti-recommendations for the scout

- DO NOT re-examine the "confirmed clean" list at the top — that's prior-round work, budget spent elsewhere.
- DO NOT propose reverting Option B / Option A, splitting commits, moving tests to `androidTest`, keeping `supportScore` as a shim, changing `SupportBreakdown` field shape or method names, or re-flagging the gate anchor.
- DO NOT propose Robolectric, Mockito, or any new test framework.
- DO NOT propose adding performance benchmarking or allocation profiling — scope is correctness.
- DO NOT re-flag the `@VisibleForTesting` vs `static` question at implementation level beyond Angle 2's verification.
- DO NOT speculate without evidence. "X might break" requires citing the specific line / fixture / code path.
- DO NOT propose splitting any of the 9 boundaries into staged commits — atomic is locked.
- Read-only. No edits.

## Files

- `notes/dispatch/R-anchor-refactor1_pack_support_breakdown.md` — slice under audit.
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java` — production file (primary target of Angle 1, 2, 4, 5, 6).
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java` — primary target of Angle 3.
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java` — `sectionHeadingBonus` definitions (Angle 3).

Return verdict.

# Spec: Thread-Anchor Prior in Follow-Up Retrieval

Task: `OPUS-A-01`
Date: 2026-04-17
Author: Claude Opus 4.7 review
Priority: P0
Estimate: M (1 day)
Owner lane: `gpt-5.4 high`

## Problem

Gallery screenshot `artifacts/external_review/ui_review_20260417_gallery/screenshots/06_phone_portrait_followup_thread.png` shows the failure mode clearly:

- Turn 1: "How do I build a simple rain shelter from tarp and cord?" → anchored to `GD-444` (correctly refused).
- Turn 2: "What should I do next after the ridge line is up?" → re-routed to `GD-185` (Acetylene Generator / carbide production) because "line" lexically overlaps with a water-seal bucket. Model then hallucinates "Set up the water seal and then create the carbide chamber above it."

Root cause: follow-up queries are treated as independent retrieval events. The prior turn's anchor guide has no influence on RRF scoring.

This is also perspective 2 in `APP_ROUTING_HARDENING_TRACKER_20260417.md` ("Primary-owner lock with harder distractor demotion").

## Goal

When a follow-up is detected, the previous turn's anchor guide (and its reciprocal links) should receive a meaningful prior in the retrieval fusion, so that drastic topic jumps require strong independent evidence.

## Design

### When the prior applies

The prior is applied when **both** conditions hold:

1. `SessionMemory.lastTurn()` is non-null and `anchorGuideId` is set.
2. Either:
   a. The query is classified as a follow-up (uses `SessionMemory.isLikelyFollowUp()` heuristics that already exist), OR
   b. The raw query is short (<= 10 tokens) AND contains a deictic reference (e.g., "it", "this", "that", "the", "next", "after", "before").

When not a follow-up, retrieval runs exactly as today.

### The prior itself

A per-chunk multiplicative bonus applied to RRF scores at fusion time, *not* to raw vector or lexical scores (to keep retrieval signals clean).

```
anchor_prior_bonus = ANCHOR_BASE_BONUS * decay(turns_since_anchor)
                   * membership_weight(chunk, anchor_guide_id)
```

Where:
- `ANCHOR_BASE_BONUS = 0.08` (matches `HYBRID_RRF_MAX_BONUS` — intentionally in the same magnitude family so the prior can be outbid by a strong independent signal).
- `decay(n)` is `1.0` at n=0, `0.6` at n=1, `0.3` at n=2, `0.0` at n >= 3.
- `membership_weight(chunk, anchor_id)` is:
  - `1.00` if chunk belongs to `anchor_id`
  - `0.50` if chunk belongs to a guide in `anchor_id`'s reciprocal-link set
  - `0.00` otherwise

Clamped cumulative: regardless of calculation, `anchor_prior_bonus` per chunk is clamped to `[0.0, 0.10]`.

### What "reciprocal link" means

Reciprocal-link set = guides that `anchor_id` lists in its front-matter as related/see-also, AND that list `anchor_id` back. If only unidirectional, use it but at 0.30 weight instead of 0.50.

This needs to be precomputed at ingest time (cheap) and exposed on `PackRepository` / `guide_catalog` as `getReciprocalLinks(guideId) -> Set<String>`.

## Desktop Implementation — `query.py`

Hook point: inside the hybrid fusion step where RRF scores are assembled (`query.py:~6580-6620` — look for `rrf_score` accumulation; the `HYBRID_RRF_K` and `HYBRID_RRF_MAX_BONUS` constants live at `query.py:49-50`). The 2026-04-17 subagent audit cited L6468-6521; those lines have shifted — grep for `rrf_score +=` before editing.

Pseudo:

```python
def _apply_anchor_prior(ranked_candidates, session, turn_index):
    if not session or not session.last_anchor_id:
        return ranked_candidates
    if not _is_follow_up(query, session):
        return ranked_candidates

    turns_since = turn_index - session.last_anchor_turn
    decay = _anchor_decay(turns_since)
    if decay == 0.0:
        return ranked_candidates

    anchor_id = session.last_anchor_id
    reciprocals = guide_catalog.get_reciprocal_links(anchor_id)

    for cand in ranked_candidates:
        gid = cand.guide_id
        if gid == anchor_id:
            weight = 1.00
        elif gid in reciprocals:
            weight = 0.50
        else:
            continue
        bonus = ANCHOR_BASE_BONUS * decay * weight
        cand.rrf_score = min(cand.rrf_score + bonus, cand.rrf_score + 0.10)

    return sorted(ranked_candidates, key=lambda c: c.rrf_score, reverse=True)
```

Log every application in DEBUG mode: `anchor_prior applied gid=<id> bonus=<value> decay=<value> weight=<value>`. This is the audit surface for tuning.

## Android Implementation — `PackRepository.java` / `SessionMemory.java`

Same logic, mirrored. Reciprocal-link precomputation should happen once on pack load and cache as `Map<String, Set<String>>`.

New method on `PackRepository`:

```java
public List<SearchResult> searchWithAnchorPrior(
    String query,
    SessionMemory session,
    int topK
) {
    List<SearchResult> candidates = searchHybrid(query, topK * 2);  // over-fetch
    if (shouldApplyAnchorPrior(query, session)) {
        applyAnchorPrior(candidates, session);
    }
    return candidates.stream()
        .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
        .limit(topK)
        .collect(Collectors.toList());
}
```

Call site in `OfflineAnswerEngine.prepare()` replaces the current `PackRepository.searchHybrid` call when a session exists.

## Follow-Up Detection

Use existing `SessionMemory` follow-up heuristics. If those are weak, extend (but treat extension as a separate sub-task — do not block on it for the P0 fix). Tested examples:

| prompt | detected as follow-up? |
|---|---|
| "what should I do next?" | yes |
| "and after that?" | yes |
| "how many times a day?" | yes |
| "what about for children?" | yes |
| "unrelated: how do I purify water?" | no (explicit reset keyword "unrelated:") |
| "how do I build a rain shelter?" | no (subject reset) |

Add an explicit reset keyword list: `["unrelated:", "new question:", "switching topics:", "different question:"]`. If detected, clear anchor before retrieval.

## Tests

### Unit — desktop

`tests/test_anchor_prior.py`:

1. follow-up after turn 1 with strong-match chunk → anchor chunk wins
2. follow-up after turn 1 with stronger off-topic chunk → anchor prior gets outbid (no runaway)
3. 3rd-turn follow-up → decay to 0.0, no prior applied
4. explicit reset keyword → anchor cleared, no prior
5. non-follow-up (subject reset) → no prior
6. anchor chunk NOT in top candidates → no bonus applied (only reranks present chunks)
7. cumulative bonus clamp holds under pathological scoring

### Unit — Android

`PackRepositoryAnchorPriorTest.java` — parity with desktop, same 7 cases.

### Regression — the gallery screenshot

Write a scripted instrumentation test:
- seed turn 1: "rain shelter from tarp and cord" → expect `GD-444` anchored
- seed turn 2: "what should I do next after the ridge line is up?"
- assert: `GD-185` NOT in top 3 retrieved chunks
- assert: answer body does not contain "carbide" or "water seal"

This is the regression test for the live bug visible in gallery screenshot 06.

### Validation

Add a `wave_ya_anchor` prompt pack with 10 two-turn scripted conversations. Run as part of the next validation pass.

## Rollout

1. Land behind a config flag: `ENABLE_ANCHOR_PRIOR = False` initially.
2. Flip to True for LiteRT lane first (smaller blast radius, easier to diff).
3. If the gallery regression test passes and `wave_ya_anchor` is green, flip for desktop too.
4. Keep the flag for one cycle, then remove.

## Observability

Every applied prior emits one line:

```
anchor_prior turn=<n> anchor_gid=<id> chunk_gid=<id> base=<0.08> decay=<f> weight=<f> bonus=<f>
```

After a week, aggregate these to a CSV and eyeball the distribution. If the prior is applying too rarely or too aggressively, tune `ANCHOR_BASE_BONUS`.

## Open Questions

- Should the anchor also affect lexical retrieval (pre-fusion), not just RRF fusion? Current spec only affects fusion, which is simpler and reversible. Revisit after first pass.
- Should reciprocal links be walked 2 hops (anchor → related → related-of-related) with decay? Probably yes for highly connected guides, but test 1-hop first.
- Does thread-anchor affect the abstain check (`OPUS-A-02`)? Proposed: abstain thresholds are evaluated *after* anchor prior is applied, so a weak-but-anchored chunk can still answer. This is the more trusting behavior; note it explicitly in test cases.
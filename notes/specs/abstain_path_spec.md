# Spec: Weak-Retrieval Abstain Path

Task: `OPUS-A-02`
Date: 2026-04-17
Author: Claude Opus 4.7 review
Priority: P0
Estimate: M (1 day)
Owner lane: `gpt-5.4 high`

## Problem

Screenshot `artifacts/external_review/ui_review_20260417_gallery/screenshots/04_phone_portrait_detail.png` shows the current failure mode. When retrieval pulls chunks that do not actually contain material for the query, the answer template still fills every slot:

```
Short answer: The provided notes do not contain instructions...
Steps:
  1. No steps available.
Limits or safety:
  Information on building a rain shelter is not available in the retrieved notes.
```

Three distinct harms:
1. Reads like a UI bug, not an honest answer.
2. Leaks retrieval-mechanism language ("the provided notes").
3. Offers no next step, so the user has no recovery path.

## Goal

When retrieval is too weak to answer, produce a single honest card:
- name what the user asked about in their words
- say Senku does not have a guide for it
- list the closest adjacent guides (max 3), with a tag like `low match`
- suggest one concrete next action (rephrase, category browse, or follow-up)

## Trigger Condition

Abstain when **all** of the following are true, evaluated over the top-3 chunks returned by hybrid retrieval after reranking:

1. `max_objective_overlap_tokens <= 1` across top-3 chunks
2. `max_vector_similarity < 0.62` across top-3 chunks (tune against benchmark)
3. `lexical_hit_count < 2` unique query-bearing tokens in top-3 chunks

Conditions 1–3 are AND, not OR. If any one is false, the regular answer path runs.

Deterministic-registry hits bypass the abstain check (those are intentional control paths, not RAG).

## Answer Shape

Single card, no "Short answer / Steps / Limits" structure.

```
Senku doesn't have a guide for "{user_query_restated}".

Closest matches in the library:
  [GD-xxx] {title} — {category} · {match_label}
  [GD-yyy] {title} — {category} · {match_label}
  [GD-zzz] {title} — {category} · {match_label}

Try:
  · rephrasing the question
  · browsing the {top_category_from_adjacent_guides} category
  · ask a simpler version (e.g., "what is X?")
```

`match_label` is one of: `moderate match`, `low match`, `off-topic candidate`. Computed from the same signals as the trigger condition.

`user_query_restated` is the user's raw query, wrapped in quotes, truncated to 60 chars with ellipsis. No model rewriting — too risky to hallucinate.

## Desktop Implementation — `query.py`

Where to hook: after hybrid retrieval + reranking, before `build_prompt()`.

Add a new function:

```python
def _should_abstain(ranked_chunks, query):
    """Return (should_abstain, match_labels) tuple."""
    ...
```

And a new builder:

```python
def build_abstain_response(query, ranked_chunks, match_labels):
    """Format the abstain card. Returns a string that bypasses normal prompt."""
    ...
```

When `_should_abstain` returns True, skip LLM generation entirely and return the builder's string directly through the same streaming interface so the client contract is unchanged.

## Android Implementation — `OfflineAnswerEngine.java`

Mirror the abstain check in `OfflineAnswerEngine.prepare()` after retrieval, before `LiteRtModelRunner.generateStreaming` or `HostInferenceClient.send`. Return a pre-built `AnswerRun` with the abstain string as the answer body, so `DetailActivity` renders it through the normal code path.

New Java method:

```java
private static boolean shouldAbstain(List<SearchResult> topChunks, String query) {
    // parallel to _should_abstain in query.py
}

private static AnswerRun buildAbstainAnswerRun(String query, List<SearchResult> topChunks) {
    // parallel to build_abstain_response
}
```

## UI — `DetailActivity.java`

Detect abstain by presence of a sentinel in `AnswerRun.metadata` (e.g., `abstain=true`). When set:
- hide the `Short answer / Steps / Limits` scaffolding
- render the single abstain card in its place
- keep provenance rail visible (showing the low-confidence match list)
- keep the follow-up composer visible — the user should be able to rephrase inline

Small visual change: add a thin `low-confidence` border-left on the answer card (muted parchment, not alarming) to distinguish from successful answers. Ties into task `OPUS-B-05`.

## Tests

### Unit — desktop

`tests/test_abstain.py`:

1. hand-crafted query with zero-overlap retrieval → abstain triggered
2. borderline case (exactly 1 token overlap, similarity=0.63) → not abstain
3. deterministic-registry match bypass → not abstain even if signals weak
4. `build_abstain_response` output format is exactly the spec contract
5. abstain output is stable across 10 random seed runs (deterministic)

### Unit — Android

`OfflineAnswerEngineAbstainTest.java`:

1. parity with desktop thresholds — same chunks trigger same outcome
2. abstain `AnswerRun` has `metadata.abstain=true`
3. abstain path does not call `LiteRtModelRunner.generateStreaming` or `HostInferenceClient.send`

### Instrumentation

New state method: `weakRetrievalShowsAbstainCardWithAdjacentGuides`. Add to `PromptHarnessSmokeTest.java`. Use the seeded rain-shelter-from-tarp query — this is the same query that produced gallery screenshot 04, so we can visually verify the fix.

### Validation

Add a new `wave_xx_abstain` prompt pack with 8 prompts that should abstain and 4 that should NOT (close but valid retrieval). Run as part of the next validation pass.

## Copy Scrub List

These phrases should never appear in abstain output:
- "the provided notes"
- "the retrieved notes"
- "the supplied context"
- "based on the provided information"
- "I cannot find"
- "I do not have access to"

Instead: "Senku doesn't have a guide for ..."

## Out of Scope

- Rephrasing suggestions generated by the LLM (too risky without grounding).
- Auto-opening the closest guide (too aggressive, user should choose).
- Changing the trigger thresholds live / per-user.
- Logging abstain as a KPI (nice to have, not required).

## Review Checkpoint

After implementation:
1. re-shoot the rain-shelter-from-tarp query on phone portrait
2. confirm abstain card renders, not template with placeholders
3. confirm gallery screenshot 04 replacement is clean
4. confirm abstain does not fire on prompts that currently answer well (regression check)

Block promotion to main lane if any of 1–4 fails.

## Open Questions

- Threshold values (`similarity < 0.62`, `lexical_hit_count < 2`) are starting points. Tune against `bench_targeted_*` prompt packs once the framework is in.
- Should abstain offer to send the question as a "note" for future guide authoring? Defer — would need a write path.
- Does the follow-up composer chip set change when the prior turn abstained? Probably yes — suggest "browse category X" instead of "continue". Cover in `OPUS-B-03`.
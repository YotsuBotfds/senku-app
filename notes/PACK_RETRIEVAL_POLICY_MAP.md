# Pack Retrieval Policy Map

Short map for agents changing retrieval behavior. This is not a tuning recipe;
it is a route-output proof checklist.

## Query Parse / Classification

- Desktop entry: `retrieve_results(...)` in `query.py`.
- It builds a `scenario_frame`, merges session context, decomposes the prompt,
  resolves a retrieval profile, and appends base plus supplemental retrieval specs.
- Deterministic routing labels live behind `classify_special_case(...)` and the
  deterministic special-case router/registry tests.
- Pack/headless entry: `QueryTerms.from_query(...)` in
  `scripts/run_mobile_headless_answers.py`.
- `QueryTerms` carries keyword tokens, route profile, metadata profile, explicit
  topic focus, preferred categories, structure hints, and context-item budgets.

## Route SQL / LIKE / FTS Flow

- Desktop vector flow embeds each retrieval spec and queries Chroma with category
  or `where` filters, then adds `query_lexical_index(...)` results when FTS5 hits.
- Desktop lexical flow uses `lexical_chunks_fts MATCH ?`, optional category
  filter, `bm25(...)`, and pseudo-distance before fusion.
- Pack/headless route flow runs only when `query_terms.route_profile.route_focused`
  is true.
- Route-focused SQL constrains `chunks.category IN (...)`, then ORs up to 12 route
  hints across `guide_title`, `section_heading`, `topic_tags`, `description`, and
  `document` using `LIKE`.
- Pack lexical first tries `lexical_chunks_fts` or `lexical_chunks_fts4`; if FTS is
  unavailable or empty, it falls back to token `LIKE` clauses over the same fields.

## Focused Route Executor

- `HeadlessRunner.search(...)` runs route results first, lexical results second,
  then dedupes by source key until the limit is met.
- `_route_results(...)` scores candidates with keyword support, metadata bonus,
  section-heading bonus, structure alignment, and narrow route-specific bonuses.
- Route rows can be filtered by specialized direct-signal and broad-house guards
  before they are allowed into the merged result set.
- Starter-build routes have larger candidate budgets and additional section
  preference logic; do not assume those knobs are safe for other route families.

## Rerank / Finalization

- Desktop `merge_results(...)` fuses vector and lexical sets with RRF, applies the
  thread-anchor prior, and records vector/lexical hit counts.
- `rerank_results(...)` applies metadata deltas, owner hints, coverage labels, and
  timing metadata, then selects the top-k chunks.
- `_review_metadata(...)`, `_confidence_label(...)`, and `_resolve_answer_mode(...)`
  finalize review state, confidence instruction, and confident/uncertain/abstain
  answer mode.
- Pack/headless final ranking sorts scored route and lexical rows by score, guide,
  and section; no route policy claim is proven until the emitted top rows show it.

## Answer Context / Anchor Flow

- Desktop anchor prior: `_anchor_prior_context(...)`, `_apply_anchor_prior(...)`,
  and `_record_anchor_turn(...)` bias follow-ups toward the previous answered guide
  and related links with decay.
- Pack/headless context: `build_context(...)` selects an anchor, loads all chunks
  for that guide, scores guide sections, then appends non-anchor ranked rows.
- Anchor selection prefers broad-house or specialized anchors when applicable,
  otherwise route-focused rows can beat generic lexical rows through support,
  section, and route-profile bonuses.
- Answer-context proof should name the selected guide, section, retrieval mode,
  and whether the context came from route-focus, guide-focus, lexical, or vector.

## Golden Route Tests

- Deterministic and desktop routing guards: `tests/test_special_cases.py`,
  `tests/test_query_routing.py`, `tests/test_registry_overlap.py`, and
  `tests/test_deterministic_near_miss.py`.
- Partial/router golden pack spec:
  `notes/specs/rag_eval_partial_router_holdouts_20260425.md`.
- Golden prompt data:
  `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`.
- Current diagnostic baseline is referenced from
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag/report.md`.
- Non-Android route regression recipes live in
  `notes/dispatch/RAG-TOOL7_non_android_regression_gate_recipe.md` and
  `notes/dispatch/RAG-TOOL11_non_android_regression_gate_wrapper.md`.

## Policy Warning

- Do not tune route policy, thresholds, route hints, owner hints, or anchor choice
  from intuition or answer prose alone.
- Require pre/post route-output proof: top rows, guide IDs, sections, retrieval
  modes, scores or support labels, and any answer-mode changes.
- If a route-output artifact does not show the intended owner moving for the
  intended reason, stop and collect diagnostics before editing policy.

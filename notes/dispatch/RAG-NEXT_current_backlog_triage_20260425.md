# RAG-NEXT Current Backlog Triage - 2026-04-25

Worker: Ada
Scope: non-Android planning/triage after the RAG tooling wave. No code changes.
Protected local-only note left untouched:
`notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md`.

## Landed - Do Not Repeat

- The old deterministic continuation is still paused. Do not restart implied
  `D52+` / broad FA-FD deterministic expansion from the morning handoff.
- Desktop RAG diagnostic foundation through `RAG-S22` is landed: failure
  taxonomy, ranked retrieval metadata, retrieval profiles, reviewed answer
  cards, evidence units, opt-in card-backed runtime answers, provenance labels,
  source-family citation, meningitis compare handling, code-health extraction,
  poisoning/wound runtime alignment, rank-drift taxonomy, dangerous-activation
  deterministic coverage, and child unknown-pill routing.
- The FA/FB/FD/FE reviewed-card panel is healthy on current evidence:
  `artifacts/bench/rag_diagnostics_20260425_1708_runtime_card_id_filter/`
  has `0` retrieval/ranking/generation/safety misses, `5` reviewed-card runtime
  answers, and `strong_supported:23|uncertain_fit_accepted:1`.
- The partial/router held-out pack is closed as a behavior target:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd397_expectation_cleanup_diag/report.md`
  has `19` expected-supported rows, `2` accepted `uncertain_fit` rows, and
  `0` retrieval/ranking/generation/artifact misses. Retrieval-only proof is
  hit@1/hit@3/hit@k `20/21`, with `RE2-UP-011` intentionally accepted as
  clarify/limited-fit.
- Do not repeat `RAG-EVAL3` prompt packaging, `RAG-EVAL5` deterministic hijack
  repair, GD-024/GD-029/GD-035/GD-052/GD-397/GD-646/GD-648/GD-649 source
  packaging, or bridge metadata routing for GD-634/GD-635/GD-636 unless a fresh
  regression artifact reopens one.
- The tooling wave is landed: artifact/dispatch/prompt-pack/context indexes,
  diagnostic row lookup, compact RAG context export, worktree delta summaries,
  guide edit impact planning, lineage reporting, retrieval-only evaluator,
  regression gate, data-backed owner hints, touched-file mojibake gate, storage
  reporter, retention planner, run-manifest enrichment, and prompt expectation
  validator.
- Policy/runbook docs are landed and should be used rather than redrafted:
  trust model, red-team handbook, privacy/export/delete policy, release/update
  runbook, schema/vector/release ADR, runtime target naming, and compound
  scenario playbooks.

## Next Highest-Value Non-Android Slices

1. **Queue/status reconciliation.**
   Update `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md` to mark
   the RAG tooling/eval wave as landed, promote the `gd397_expectation_cleanup`
   proof as the current partial/router baseline, and remove stale "active"
   wording around `RAG-S1` through `RAG-S18`.
   Parallel-safe: yes. Re-ingest/bench proof: no.

2. **Standardize the non-Android RAG regression gate.**
   Add a short documented validation recipe that runs prompt expectation
   validation, retrieval-only eval, full bench/analyzer, and
   `scripts/rag_regression_gate.py` against the current partial/router baseline.
   This is mostly docs plus possibly a thin wrapper if desired.
   Parallel-safe: yes if docs/wrapper only. Re-ingest/bench proof: no for docs;
   yes if changing gate logic.

3. **Build the next held-out prompt pack from the compound/high-liability
   backlog.**
   Use `SENKU_COMPOUND_SCENARIO_PLAYBOOKS_20260425.md`,
   `SENKU_HIGH_LIABILITY_RED_TEAM_HANDBOOK_20260425.md`, and metadata/marker
   audits to create a new non-Android eval pack before editing more guides.
   Parallel-safe: prompt-pack/spec drafting is safe. Bench proof: required
   before claiming behavior. Re-ingest: no unless guide edits follow.

4. **Observed-only guide quality cleanup.**
   Use `guide_edit_impact.py`, metadata audit, corpus marker scan, and fresh
   diagnostics to choose one observed failing guide family at a time. Avoid
   metadata churn for families already at `0/0` frontmatter/card gaps or with
   no behavior miss.
   Parallel-safe: triage is safe. Re-ingest/bench proof: required after any
   guide/frontmatter/body edit; use incremental `ingest.py --files ...`.

5. **Text-quality cleanup with tight ownership.**
   Start with scanner-driven, high-confidence mojibake/frontmatter/icon repairs
   or the remaining unresolved-partial report, not broad body rewrites.
   Parallel-safe: read-only triage is safe. Re-ingest/bench proof: required
   after guide edits before trusting retrieval behavior.

## Parallel Safety

- Safe in parallel with guide/runtime work: queue cleanup, dispatch rotation,
  policy note cross-links, artifact index refreshes, run-manifest summaries,
  prompt expectation linting, retrieval-only evaluations, and read-only
  diagnostic queries.
- Needs serialized proof: guide body/frontmatter edits, owner-hint/rerank data
  changes, prompt-contract changes, analyzer bucket taxonomy changes, and any
  runtime path that changes answer text, citation priority, or abstain/clarify
  behavior.
- Guide edits require re-ingest before retrieval claims. For focused edits,
  prefer incremental ingest with `ingest.py --files <changed guides>` and
  `--force-files` only when ingest plumbing or contextual text changed.
- Safety-critical guide/card edits should also run the high-liability red-team
  checklist and grep for shared formulas, thresholds, dose-like statements, and
  invariants before validation.

## Stale Labels / Mismatches To Clean Next

- `notes/CP9_ACTIVE_QUEUE.md` still has a 2026-04-23 top "Last updated" line
  while carrying many 2026-04-24/25 RAG results below.
- The CP9 "Active RAG lane" section still reads like `RAG-S4` through
  `RAG-S13` are the current execution queue, even though `RAG-S14` through
  `RAG-S22`, `RAG-T*`, `RAG-EVAL*`, `RAG-META*`, and `RAG-CARD*` have landed.
- `notes/dispatch/README.md` says "`RAG-S1` through `RAG-S18` are the current
  RAG-next-level dispatches" while later bullets record through `RAG-S22`,
  `RAG-T12`, `RAG-TOOL5`, and related policy/docs slices.
- Some dispatch notes are now historical proof records but still live at the
  dispatch root. Do not infer "active" from filename presence alone.
- The partial/router queue text in `RAG-EVAL4` should now treat
  `gd397_expectation_cleanup` as the superseding proof; earlier `post_fixes`,
  `metadata_visible`, and `contextual_index` artifacts are history.
- The metadata audit still reports broad high-liability gaps. Current guidance
  is not "close every metadata/card counter"; only act where fresh behavior
  panels or held-out packs show a real miss.

## Recommended Next Slice

Run the queue/status reconciliation first. It is parallel-safe, does not need
bench time, and will prevent the next worker from rediscovering closed
RAG-S/RAG-EVAL/RAG-TOOL work before choosing the next eval or guide slice.

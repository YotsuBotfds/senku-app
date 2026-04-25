# Deep Research Report Backlog - 2026-04-25

Source: `C:\Users\tateb\Documents\prep\Books\deep-research-report.md`

## Thesis

The corpus is broad and useful as an offline practical encyclopedia, but the
assistant/product layer is under-documented. The biggest gaps are not simply
"more guide content"; they are self-contained ingestion, metadata/routing
coverage, model/runtime clarity, safety/eval policy, privacy/update operations,
and user trust behavior.

## Near-Term Slices

- `RAG-DOCS1`: scout canonical corpus packaging and unresolved partials.
  Add a scanner/report for unresolved `{{> ... }}` style partials and classify
  by guide, category, and liability level before guide edits.
  Static scanner landed in `scripts/scan_corpus_markers.py`; first corpus scan
  found `55` unresolved partial markers in `55` guides. See
  `notes/dispatch/RAG-DOCS1_corpus_marker_scanner.md`.
- `RAG-META1`: audit high-liability metadata coverage.
  Check aliases, routing cues, router/bridge markers, citations-required flags,
  applicability fields, and liability level for high-liability/high-traffic
  guides first.
  Non-blocking audit landed in `scripts/audit_metadata_coverage.py`; first scan
  found `249` high-liability guides and `248` with at least one metadata or
  reviewed-card coverage gap. See
  `notes/dispatch/RAG-META1_high_liability_metadata_audit.md`.
- `RAG-RUNTIME1`: completed.
  Resolved `gemma-4-e2b-it-litert` as the canonical local LiteRT generation
  target ID for the current desktop/validation lane, distinct from public API
  model IDs and broad-quality overrides. Decision note:
  `notes/RUNTIME_TARGET_NAMING_20260425.md`.
- `RAG-TRUST1`: completed.
  Drafted the Senku trust model and high-risk answer policy with can-answer/
  cannot-verify/escalate behavior, `uncertain_fit` vs `abstain` logic, citation
  and provenance requirements, offline limitations, and high-liability examples.
  Completed note:
  `notes/SENKU_TRUST_MODEL_HIGH_RISK_POLICY_20260425.md`.
- `RAG-EVAL1`: add partial-hole and router-page retrieval diagnostics.
  Build held-out prompts that fail if an unresolved partial or thin router page
  becomes the primary answer source.
  First analyzer overlay landed: `scripts/analyze_rag_bench_failures.py` accepts
  `--corpus-marker-scan` and surfaces top-1 marker risk / bridge / unresolved
  partial status. See
  `notes/dispatch/RAG-EVAL1_corpus_marker_diagnostic_overlay.md`.

## Later Slices

- `RAG-OPS1`: draft RAG schema, vector-store, and release operations ADR.
  Include chunk schema, metadata rules, router/bridge handling, citation
  rendering, hybrid retrieval, index rebuilds, snapshots, rollback, migration,
  and corruption recovery.
- High-liability answer policy and red-team handbook.
  Tie deterministic rules, reviewed-card runtime, evidence nuggets,
  claim-support diagnostics, and Android enablement gates into one release
  policy.
- Privacy/deletion/export/incident docs before notes or sync features ship.
  Especially important for health notes, location-like incident logs, or
  personal records.
- Release/update/migration docs.
  Use existing guide `version` and `last_updated` metadata plus explicit
  "reindex required?" and rollback fields.
- Compound-scenario playbooks.
  Examples: sick child during outage, contaminated well plus food shortage,
  evacuation with livestock, storm-damaged shelter recovery.

## Risks And Dependencies

- Model naming ambiguity can pollute docs, tests, and user trust.
- Partial expansion may be a build-packaging problem rather than an
  authored-content problem; first identify the canonical rendered source.
- Metadata rollout can become huge; prioritize high-liability and high-traffic
  families.
- Privacy/legal docs depend on whether Senku stores notes, syncs, exports, or
  handles health-related personal data.
- Desktop reviewed-card/provenance work does not automatically carry to
  Android; mobile needs explicit pack/runtime/UI plumbing.
- The report is a documentation-answerability review, not runtime truth. Validate
  any backlog item with held-out prompts and the existing diagnostics.

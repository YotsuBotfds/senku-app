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
- `RAG-EVAL2`: completed.
  Added a held-out primary-answer regression pack for unresolved partial and
  bridge-thin matches.
  Evidence-driven prompt pack:
  `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`.
  Spec:
  `notes/specs/rag_eval_partial_router_holdouts_20260425.md`.
  Dispatch:
  `notes/dispatch/RAG-EVAL2_partial_router_holdout_pack.md`.
  Latest focused diagnostics found 2 top-1 unresolved-partial rows and 1 top-1
  bridge row, separating routing misses from prompt-budget and local generation
  artifact errors.

## Later Slices

- `RAG-OPS1`: completed.
  Drafted the RAG schema, vector-store, and release operations ADR covering
  canonical sources, chunk and reviewed-card metadata, mobile-pack snapshots,
  re-ingest/rebuild triggers, release gates, migration, and rollback posture.
  Completed note:
  `notes/RAG_SCHEMA_VECTOR_RELEASE_OPS_ADR_20260425.md`.
- `RAG-TRUST2`: completed.
  Added the high-liability red-team handbook tying deterministic rules,
  reviewed-card runtime, evidence nuggets, claim-support diagnostics, app
  acceptance, and Android enablement gates into a release checklist.
  Completed note:
  `notes/SENKU_HIGH_LIABILITY_RED_TEAM_HANDBOOK_20260425.md`.
- `RAG-PRIV1`: completed.
  Added privacy, export, deletion, and incident policy before notes, sync, or
  personal-record features ship, with explicit handling for health notes,
  location-like incident logs, validation artifacts, and Android session state.
  Completed note:
  `notes/SENKU_PRIVACY_EXPORT_DELETE_INCIDENT_POLICY_20260425.md`.
- `RAG-REL1`: completed.
  Added release/update/migration runbook using guide `version` and
  `last_updated` metadata, explicit `reindex_required` values, manifest capture,
  migration rules, and rollback fields. Completed note:
  `notes/SENKU_RELEASE_UPDATE_MIGRATION_RUNBOOK_20260425.md`.
- `RAG-PLAY1`: completed.
  Added compound-scenario playbooks for sick child during outage, contaminated
  well plus food shortage, evacuation with livestock, storm-damaged shelter
  recovery, and injured/cold/questionable-water lane separation. Completed note:
  `notes/SENKU_COMPOUND_SCENARIO_PLAYBOOKS_20260425.md`.

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

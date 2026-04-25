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
- `RAG-RUNTIME1`: resolve Gemma/LiteRT target naming.
  Clarify whether `gemma-4-e2b-it-litert` is internal shorthand, provisional
  naming, or a real deployment target distinct from public Gemma 3n E2B/E4B
  naming.
- `RAG-TRUST1`: draft Senku trust model and high-risk answer policy.
  Cover what Senku can answer, what it cannot verify, citation expectations,
  offline assumptions, refusal/escalation behavior, and high-liability surfaces.
- `RAG-EVAL1`: add partial-hole and router-page retrieval diagnostics.
  Build held-out prompts that fail if an unresolved partial or thin router page
  becomes the primary answer source.

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

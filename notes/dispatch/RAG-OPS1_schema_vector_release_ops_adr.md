# RAG-OPS1 - Schema, Vector Store, and Release Ops ADR

## Status

Landed 2026-04-25.

## Scope

Close the deep-research `RAG-OPS1` documentation slice by adding a local ADR
for RAG schema ownership, vector-store rebuild rules, mobile-pack snapshot
semantics, release gates, migration rules, and rollback posture.

## Files

- `notes/RAG_SCHEMA_VECTOR_RELEASE_OPS_ADR_20260425.md`
- `notes/deep_research_report_backlog_20260425.md`
- `notes/AGENT_OPERATING_CONTEXT.md`
- `notes/dispatch/README.md`

## Result

The ADR records:

- guides and reviewed-card YAML as canonical authored sources;
- Chroma, mobile packs, vectors, and bench outputs as rebuildable snapshots;
- required guide/card/mobile release gates;
- re-ingest and mobile-pack rebuild triggers;
- SQLite/vector migration and rollback rules;
- current developer/test-only reviewed-card runtime exposure boundary.

## Validation

Documentation-only slice. Checked links and references by repo search; no code
or generated pack behavior changed.

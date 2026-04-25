# Senku Release, Update, and Migration Runbook - 2026-04-25

## Purpose

Define the release/update checklist for guide corpus changes, retrieval index
rebuilds, mobile-pack exports, and schema/runtime migrations.

Use this runbook with the RAG ops ADR:
`notes/RAG_SCHEMA_VECTOR_RELEASE_OPS_ADR_20260425.md`.

## Release Classes

### Documentation-Only

Examples: policy notes, dispatch notes, dashboard HTML, handoff notes.

Required:

- `git diff --check`;
- link from the relevant backlog/context note;
- no re-ingest or mobile-pack rebuild unless guide/card/runtime behavior also
  changed.

### Guide Metadata Or Content

Examples: guide frontmatter, aliases, routing cues, applicability, citation
policy, related links, guide prose, partial repairs.

Required:

- update `last_updated` when guide facts or guidance changed;
- update `version` if the guide already uses explicit versioning and the change
  is user-facing;
- re-ingest before trusting retrieval;
- run metadata/marker audits when high-liability or router/partial surfaces are
  involved;
- run the focused prompt panel for affected families.

### Reviewed-Card Contract

Examples: new card YAML, clause edits, source sections, runtime citation policy.

Required:

- validate card schema;
- run card-contract tests;
- run focused runtime proof when the card can affect desktop answers;
- rebuild mobile pack only if the card is intended for Android.

### Retrieval Or Runtime Code

Examples: reranking, retrieval profiles, abstain/uncertain-fit policy, reviewed
card runtime, deterministic safety rules, analyzer semantics.

Required:

- focused unit tests for the changed module;
- prompt panel proving no new bad diagnostic bucket;
- analyzer output path named in dispatch/context;
- Android parity assessment if mobile code or pack contract changes.

### Mobile-Pack Or Android Schema

Examples: SQLite schema, vector format, manifest fields, Android reader/model
changes, runtime exposure toggles.

Required:

- mobile pack export proof;
- manifest path and hashes recorded;
- Android JVM/instrumentation checks named by the lane;
- compatibility note for existing packs and app versions;
- rollback path that does not mix SQLite, vectors, and manifest from different
  exports.

## Guide Metadata Rules

Use existing guide fields when present:

- `version`: bump for user-facing guidance changes, schema/metadata changes
  that alter retrieval behavior, or major source rewrites.
- `last_updated`: update for content, routing, applicability, or citation-policy
  changes.
- `liability_level`: must not be downgraded without a review note.
- aliases/routing/applicability/citation policy: update together when a guide is
  added to a high-liability observed family.

Do not mass-edit `version` or `last_updated` just to quiet an audit. The field
should signal meaningful release history.

## Reindex Required Field

When writing release notes for a guide/runtime change, explicitly state one of:

- `reindex_required: no` for docs-only or analyzer-only changes;
- `reindex_required: desktop` when Chroma must be rebuilt before desktop proof;
- `reindex_required: mobile-pack` when an app-facing pack must be rebuilt;
- `reindex_required: desktop+mobile-pack` when both apply.

Also state whether the current committed artifacts are proof artifacts or only
local ignored outputs.

## Manifest Capture

For app-facing pack releases, record:

- pack directory;
- `senku_manifest.json` path;
- `pack_version`;
- `generated_at`;
- guide count, chunk count, deterministic rule count, answer-card count;
- SQLite SHA;
- vector SHA;
- APK SHA when Android prompt proof is involved;
- emulator/device serials and posture when instrumentation is involved.

For desktop-only behavior releases, record:

- prompt wave JSON paths;
- analyzer output directory;
- bucket counts;
- expected-owner hit/citation rates when relevant;
- answer-card/evidence/claim-support counts when reviewed cards are involved.

## Migration Rules

- Additive fields are preferred.
- Optional reader behavior must be default-safe when older packs omit the field.
- Renames, deletions, required-field additions, or type changes require a
  compatibility note and a pack/app version boundary.
- Vector format changes require a new vector format marker and reader rejection
  for unknown formats.
- Reviewed-card runtime exposure changes require separate product-policy notes.

## Rollback Procedure

Desktop rollback:

1. Revert or patch the source change.
2. Re-ingest if guide/index inputs changed.
3. Rerun the smallest affected prompt panel.
4. Analyze and record the new diagnostic output.

Mobile rollback:

1. Restore a complete prior pack directory, including SQLite, vectors, and
   manifest from the same export.
2. Confirm the target APK can read that pack format.
3. Rerun the focused Android proof for the affected feature.
4. Record APK SHA, pack manifest SHA, and prompt proof path.

Do not claim rollback from a source-only revert when the deployed pack or APK
still carries the changed behavior.

## Release Note Template

```text
slice:
commit:
change_class:
reindex_required:
source_files:
pack_manifest:
desktop_proof:
android_proof:
known_limits:
rollback:
```

## Open Follow-Ups

- Decide whether guide `version` should become required for high-liability
  guides.
- Add an optional release-note generator once dispatch/run-manifest practice is
  stable.
- Decide whether mobile pack manifests need an explicit schema version separate
  from `pack_version`.

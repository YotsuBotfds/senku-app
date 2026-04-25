# RAG Schema, Vector Store, and Release Operations ADR - 2026-04-25

## Status

Accepted as the current local operating contract for desktop RAG validation and
mobile-pack export. This ADR documents existing repo behavior plus the minimum
release gates needed before changing pack schema, vector indexes, reviewed-card
runtime coverage, or high-liability guide metadata.

## Context

Senku now has several RAG surfaces that must stay aligned:

- authored guide Markdown under `guides/`;
- guide catalog metadata in guide frontmatter;
- desktop Chroma retrieval built by `ingest.py`;
- desktop query/runtime behavior in `query.py` and extracted helper modules;
- reviewed answer-card contracts under `notes/specs/guide_answer_cards/`;
- mobile-pack export in `mobile_pack.py` and answer-card export helpers in
  `mobile_pack_answer_cards.py`;
- bench and analyzer artifacts under `artifacts/bench/`.

The main failure mode this ADR prevents is changing one surface while trusting
stale behavior from another. Guide edits require re-ingest. Card edits require
schema/contract validation and runtime proof. Mobile schema changes require
explicit pack/export and Android reader checks.

## Decision

Use the guide corpus and reviewed-card YAML as canonical authored sources.
Use generated retrieval indexes, mobile packs, and bench artifacts as rebuildable
snapshots with manifests and validation evidence.

Do not treat Chroma state, mobile SQLite rows, vector binaries, or bench outputs
as primary truth. They are proof artifacts tied to a source revision and runtime
configuration.

## Canonical Schemas

### Guide Frontmatter

High-liability guides should carry:

- stable `id`, `slug`, `title`, `category`, and `liability_level`;
- search aliases and routing cues;
- applicability boundaries;
- citation policy;
- related-guide references when the guide is a support or boundary surface.

The high-liability metadata audit is the broad coverage check:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --cards-dir notes\specs\guide_answer_cards --output-json artifacts\bench\metadata_coverage_audit.json --output-md artifacts\bench\metadata_coverage_audit.md
```

### Retrieval Chunks

Chunk metadata must preserve enough structure to diagnose retrieval failures:

- `chunk_id`;
- `guide_id`, `slug`, `title`, and category;
- section id / section heading where available;
- structure type, content role, time horizon, and topic tags when present;
- bridge/router marker metadata when present.

Any change to guide Markdown, frontmatter routing fields, chunking behavior, or
embedding model requires re-ingest before retrieval behavior is trusted.

### Reviewed Answer Cards

Reviewed-card YAML is the contract source for high-risk structured answers.
Required fields are governed by `notes/specs/guide_answer_card_schema.yaml` and
validated by `scripts/validate_guide_answer_cards.py`.

Cards may add `runtime_citation_policy: reviewed_source_family` only when every
extra source guide has been reviewed for the card's exact answer scope. Runtime
selection must remain opt-in and evidence-owner bounded; support cards should
not be added just to zero family-priority counters.

### Mobile Pack

The mobile pack is a snapshot, not an authoring surface. Current pack export
uses:

- `senku_mobile.sqlite3` for guides, chunks, lexical FTS, deterministic rules,
  and answer-card tables;
- `senku_vectors.*` for vector rows;
- `senku_manifest.json` for pack counts and hashes;
- `senku-vectors-v1` as the vector file format marker.

Schema changes to `guides`, `chunks`, `answer_cards`,
`answer_card_clauses`, or `answer_card_sources` must be paired with Android
reader/model compatibility checks. Optional fields must remain backward
compatible until the app has a migration gate.

## Release Gates

### Desktop Guide Or Card Change

Run, at minimum:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_guide_answer_cards.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
```

If guide Markdown or frontmatter changed, re-ingest before behavior proof:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B ingest.py --rebuild
```

Then run the focused prompt panel that covers the changed family and analyze it
with corpus-marker overlay.

### Reviewed-Card Runtime Proof

Reviewed-card runtime behavior must be proven with explicit opt-in:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave fa -PythonPath .\.venvs\senku-validate\Scripts\python.exe -EnableCardBackedRuntimeAnswers
```

The analyzer must show:

- no retrieval, ranking, generation, or safety-contract miss introduced by the
  change;
- reviewed-card runtime rows pass answer-card, evidence-nugget, and
  claim-support diagnostics;
- deterministic rows are counted as skipped/not evaluable for card contracts;
- actual runtime rows are evaluated against artifact `reviewed_card_ids`.

### Mobile Pack Or Android Runtime Change

Run the focused Python pack/card contract tests first, then the Android checks
named by the relevant Android lane dispatch. Do not infer Android parity from a
desktop card proof. A new reviewed-card runtime expansion requires:

- exported card, clause, and source rows in the mobile pack;
- Android reader/model coverage for the new fields;
- runtime-on positive harness proof for the exact card;
- runtime-off and non-reviewed inverse canaries when changing exposure;
- screenshots or UI assertions when surfacing new user-visible trust labels.

## Reindex And Migration Rules

Re-ingest is required after:

- any `guides/` content or frontmatter edit;
- guide id, slug, title, aliases, routing, applicability, related-guide, or
  citation-policy change;
- chunking, contextual retrieval, section-family, or embedding-model change;
- corpus partial repair that changes rendered guide text.

Mobile pack rebuild is required after:

- any re-ingest used for app-facing release;
- deterministic registry or Android predicate manifest change;
- answer-card YAML change intended for Android;
- pack schema or vector file format change.

Migration policy:

- Additive SQLite columns/tables may ship behind optional reader behavior.
- Renames, deletions, or type changes require a manifest schema-version bump and
  a compatibility note.
- Vector format changes require a new `VECTOR_FILE_FORMAT` marker and a reader
  rejection path for unknown formats.

## Rollback

Rollback uses source-controlled guide/card/runtime code plus prior generated
pack artifacts:

- revert the source commit if the defect is authored content or runtime code;
- rebuild Chroma and rerun the focused prompt panel before declaring desktop
  rollback complete;
- restore the prior mobile pack directory only when its manifest hash and APK
  expectations are documented;
- never mix a restored SQLite file with vectors or manifest from a different
  export.

## Operational Notes

- Keep high-liability family priorities tied to observed diagnostics. Broad
  corpus-wide missing-card counts are planning signals, not direct work orders.
- Use corpus-marker scans when investigating retrieval/ranking drift; unresolved
  partials or router pages at top-1 should be visible in analyzer output.
- Treat generated artifacts as disposable unless a dispatch/context note names
  them as proof.
- Keep product exposure separate from developer/test runtime proof. The current
  reviewed-card runtime posture remains developer/test gated unless a product
  enablement dispatch explicitly changes it.

## Open Questions

- Whether contextual chunk sidecars or section-family summaries should become
  production indexes after shadow comparison.
- Whether local RAG dashboarding should remain static HTML or become a generated
  artifact from analyzer JSON.
- Which privacy/export/delete policy is required before any future user notes or
  sync feature stores health-related personal data.

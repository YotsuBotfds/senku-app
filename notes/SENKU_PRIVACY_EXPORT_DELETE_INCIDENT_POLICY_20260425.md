# Senku Privacy, Export, Deletion, and Incident Policy - 2026-04-25

## Purpose

Set the privacy and data-rights baseline before Senku grows user notes, sync,
exports, incident logs, or health-related personal records.

This is an engineering/product policy for the local repo. It is not a legal
compliance opinion. Any public release or regulated deployment still needs
jurisdiction-specific review.

## Current Product Posture

Current RAG work is primarily local/offline:

- guide corpus and mobile packs are static authored content;
- desktop bench artifacts are local validation outputs;
- Android reviewed-card runtime remains developer/test-only and default off;
- Android `SessionMemory` is local app state for conversation continuity and
  reviewed-card detail metadata transport;
- no user sync service, account system, or cloud notes feature is approved by
  the current RAG backlog.

Do not add sync, account-linked storage, or long-lived personal incident records
as an incidental part of RAG/runtime work.

## Data Classes

Classify data before adding storage or export behavior:

- **Public corpus:** guide Markdown, guide metadata, reviewed-card YAML, static
  mobile pack content.
- **Local runtime state:** app preferences, local session state, recent prompt
  context, reviewed-card metadata used to render answer details.
- **Validation artifacts:** bench JSON/Markdown, screenshots, UI dumps, logs,
  test harness summaries.
- **User personal records:** notes, symptom logs, locations, incident reports,
  household information, medical details, photos, attachments, contacts, or any
  future synced profile/account data.

Only the first three are currently expected in normal development. The fourth
requires an explicit product design and release gate.

## Default Rules

- Keep personal data local by default.
- Collect the minimum needed for the current feature.
- Prefer ephemeral state unless the user clearly creates a record.
- Do not silently sync health, location-like, household, or incident details.
- Do not include user prompts, screenshots, or UI dumps in committed artifacts
  unless they are synthetic or explicitly scrubbed.
- Do not use production user records as test fixtures.
- Do not expand telemetry to include raw prompts or answer bodies without a
  separate privacy review.

## Deletion Requirements

Before shipping any feature that stores user personal records, implement:

- delete single record;
- delete all local personal records;
- clear conversation/session memory;
- clear downloaded/generated pack caches when applicable;
- delete exported files from app-owned storage when still under app control;
- clear hidden developer/test reviewed-card runtime state if it can affect user
  answers;
- test coverage proving deleted records do not reappear after app restart.

Deletion must be user-visible and must not require reinstalling the app.

## Export Requirements

Before shipping notes, incident logs, or health-related records, implement a
plain export path:

- human-readable format, preferably JSON plus Markdown or CSV where useful;
- timestamps and app/version metadata;
- source/citation metadata for generated advice saved with a record;
- clear warning when exported files may contain sensitive health or household
  information;
- no hidden cloud upload as part of export;
- import/migration behavior documented separately if supported.

Export should be optional and user-initiated.

## Incident And Log Handling

Validation artifacts can accidentally capture sensitive information through
prompts, screenshots, dumps, or logs. Treat these as sensitive until reviewed.

Incident response for accidental capture:

1. Stop creating or sharing the artifact.
2. Identify the file paths, commit hashes, and affected data class.
3. Remove local copies that are not needed for investigation.
4. If committed, rotate the artifact out of normal references and decide
   whether history rewrite is warranted with the repository owner.
5. Record a short incident note under `notes/` with what happened, what was
   removed, and what prevention gate changed.

Do not paste sensitive user text into dispatch notes. Summarize behavior with
synthetic prompts instead.

## RAG-Specific Privacy Gates

Before adding personal storage to RAG workflows:

- answer provenance must distinguish deterministic, reviewed-card, generated,
  and uncertain-fit surfaces;
- saved answers must include guide IDs and card IDs when available;
- regenerated answers must not overwrite user records silently;
- prompt history used for retrieval must be bounded and user-clearable;
- any future cloud or cross-device sync must have an off switch and a deletion
  story before implementation.

## Android-Specific Gates

Before exposing a persistent notes or incident feature on Android:

- local storage schema is documented;
- `SessionMemory` clearing is tested;
- export/delete UI flows are covered by JVM or instrumentation tests;
- screenshots and dumps used as proof are synthetic or scrubbed;
- reviewed-card developer/test toggles do not persist into a normal user mode
  unexpectedly;
- clean install / upgrade / uninstall behavior is documented.

## Bench And Artifact Hygiene

Development artifacts should use synthetic prompts. If a real user-like prompt
is necessary for reproduction:

- remove names, exact addresses, phone numbers, account IDs, and exact dates of
  personal events;
- replace rare combinations that identify a household or incident;
- keep only the minimum text needed to reproduce routing/evidence behavior;
- mark the artifact path in the dispatch note and avoid broad redistribution.

## Release Blockers

Block release or product exposure if:

- a personal-record feature lacks delete-all;
- export exists without a sensitivity warning;
- cloud/sync behavior is hidden or default-on without review;
- validation artifacts contain unsanitized real user data;
- Android runtime state can alter high-liability answers after the user clears
  data/session state;
- logs capture raw high-liability prompts in normal operation.

## Open Questions

- Whether future user notes are in scope at all, or whether Senku remains a
  stateless guide assistant.
- Whether any jurisdiction-specific health-data obligations apply to a public
  distribution.
- Whether encrypted local backups are needed if incident logs become a real
  product feature.
- Whether exports should include machine-readable provenance bundles for saved
  answers.

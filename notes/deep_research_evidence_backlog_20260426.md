# Deep Research Evidence Backlog - 2026-04-26

Scope: backlog note only. No code, guide, artifact, or protected
`PLANNER_HANDOFF` files were edited.

## Evidence Read

- `notes/deep_research_report_triage_20260426.md`
- `C:\Users\tateb\Downloads\deep-research-report.md`
- Current local git history: `eb71a3c` through `36d306b`, including CI,
  FastEmbed, ingest freshness, and retrieval-index cache commits.
- Current notes: `notes/AGENT_TOOLING_UPGRADES_20260426.md`,
  `notes/high_liability_smoke_coverage_inventory_20260426.md`,
  `notes/artifact_retention_cleanup_proposal_20260426.md`,
  `notes/dispatch/RAG-TOOL6_incremental_ingest_planner.md`.
- Current code/test anchors inspected for evidence:
  `.github/workflows/non_android_regression.yml`, `ingest_freshness.py`,
  `tests/test_ingest_freshness.py`, `tests/test_registry_overlap.py`,
  `tests/test_deterministic_near_miss.py`.

## Backlog

### DR-CI-VERIFY - Prove Current Non-Android Gate Health

Priority: high.

Evidence:

- The triage note records run `24955889219` timing out during
  `ingest.py --rebuild` after repeated FastEmbed embedding retries/500s.
- Local history shows follow-up CI commits after that report:
  `36d306b stabilize fastembed ingest gate`, `190368f cache non android
  retrieval index`, `95190ae scope ci retrieval index rebuild`, `0644e95
  compact ci retrieval smoke index`, `e4a562a throttle ci smoke embeddings`,
  and `eb71a3c relax compact smoke retrieval warnings`.
- The current workflow starts `scripts\fastembed_openai_server.py` on
  `127.0.0.1:8801`, caches `.ci-cache/fastembed`, caches `db`, and keys the
  retrieval cache on guide/prompts plus ingest/config helper files.

Action:

- Current proof is complete: `Non-Android Regression Gate` run `24957922497`
  passed on head `eb71a3c` in `Fast` mode after the compact cache-miss smoke
  index path was added.
- Keep the original CI-health claim scoped to the old failing run; current
  behavior is now proven separately by run `24957922497`.

Validation:

- Run `24957922497` completed green on `2026-04-26`; run `24957798681`
  previously proved compact ingest finished and exposed only a strict-warning
  mismatch, not FastEmbed service lifetime/index rebuild budget.

Deferred unless evidenced:

- Any claim that branch protection or push-level required checks are missing.
  The local evidence shows workflow shape and commits, not repository protection
  settings.

### DR-INGEST-VERIFY - Verify Stale Ingest Fail-Closed Semantics

Priority: high.

Evidence:

- The external report flagged guide/index freshness as hazardous; the triage
  note narrowed this to a stale-ingest fail-closed verification task.
- Local history now includes `bac2b58 preserve ingest manifest entries` and
  `3b0a5fa add ingest freshness preflight`.
- `ingest_freshness.py` classifies `fresh`, `stale`,
  `incomplete_untrusted`, and `absent_or_invalid`; `query.py` integration was
  touched by `3b0a5fa`.
- `tests/test_ingest_freshness.py` covers matching manifests, changed SHA,
  missing guide, truncated manifest, and invalid manifest behavior.
- `notes/dispatch/RAG-TOOL6_incremental_ingest_planner.md` says deleted guide
  markdown paths require rebuild because incremental ingest cannot safely
  remove stale records.

Action:

- Confirm query/validation behavior for each freshness status, especially
  whether `incomplete_untrusted` and `absent_or_invalid` should warn or block.
- Add or run a live smoke that edits a temporary guide fixture, observes stale
  status, then re-ingests and observes fresh status.

Validation:

- Freshness checks fail closed for real drift that can invalidate retrieval
  claims, while known no-manifest local-dev cases have an explicit accepted
  policy rather than accidental permissiveness.

Deferred unless evidenced:

- Broader cache-staleness claims outside guide/index freshness. Current
  evidence supports retrieval-index/manifest work, not every cache class.

### DR-DET-OVERLAP - Strengthen Deterministic Overlap Visibility

Priority: medium.

Evidence:

- Existing tests already cover priority tie behavior,
  `get_deterministic_special_case_overlaps()`, child cleaner overlap, child
  unknown-pill priority, and live Android near-miss panels.
- The triage note correctly downgrades the report's broad deterministic-policy
  warning because overlap tests, near-miss tests, and
  `scripts\validate_special_cases.py` already exist.

Action:

- Keep this as a report/visibility enhancement unless a concrete overlap miss
  appears.
- Next useful increment: generated matrix output that lists rule id, priority,
  lexical signature, promotion status, overlapping prompts, winner, and
  tie-break reason.

Validation:

- Existing deterministic/routing unit slices plus validator still pass; the
  matrix exposes every overlap and no unresolved pair lacks an explicit winner.

Deferred unless evidenced:

- Declarative policy-engine refactor. The current evidence proves monitoring
  exists; it does not prove brittle behavior requiring redesign.

### DR-DIAG-ACCEPT - Separate Diagnostics From Acceptance Where It Matters

Priority: medium.

Evidence:

- `notes/high_liability_smoke_coverage_inventory_20260426.md` shows diagnostics
  bind owner, provenance/decision, safety surface, app acceptance,
  card/evidence/claim status, and bucket, but route predicates/profiles are
  usually unit-tested separately.
- The same inventory identifies concrete partial gaps: several deterministic
  emergency rows pass `deterministic_pass` while analyzer safety surface is
  `not_safety_critical`; `RE8-TR-001` has owner/bucket proof but lacks
  row-specific route or emergency-first assertion in inspected evidence.
- Current diagnostic artifacts give strongest full-chain evidence for
  `RE8-CM-001`, `RE7-NB-001`, and `RE6-EV-002`.

Action:

- Convert the inventory's gaps into exact test/backlog rows rather than broad
  analyzer rewrites.
- Add one compact end-to-end evidence row only where route, owner,
  reviewed-card/runtime or deterministic decision, emergency-first surface, and
  diagnostic bucket are all intended to be asserted together.

Validation:

- Acceptance gates remain pinned to stable behavior; diagnostic taxonomy can be
  adjusted without silently changing pass/fail meaning.

Deferred unless evidenced:

- Claims that diagnostics currently mask unsafe behavior. The inventory found
  partial coverage, not a proven unsafe app response.

### DR-ART-APPROVE - Keep Artifact Retention Approval-Only

Priority: medium.

Evidence:

- `notes/artifact_retention_cleanup_proposal_20260426.md` reports about
  68.3 GB under `artifacts`, about 33.6 GB protected, about 30.2 GB archive
  candidates, and 41 zero-byte delete candidates.
- The proposal is explicitly approval-ready only, recommends archive-only as
  the first pass, and says no deletion is approved.

Action:

- Do not delete or move artifacts from this backlog.
- If approved later, generate a fresh dry run first, archive only unprotected
  generated families, preserve relative paths/checksums/manifests, and exclude
  Android/emulator-heavy candidates unless that lane approves.

Validation:

- Protected proof artifacts and current CI failure evidence remain in place;
  a post-action dry run can prove preserved protected totals and archived byte
  counts.

Deferred unless evidenced:

- Deletion of zero-byte candidates. Even if mechanically low risk, the current
  evidence and note policy require a separate explicit approval.

## Speculative Claims Parked

- Missing branch protection or required push checks.
- Current unsafe answer behavior caused by deterministic overlap.
- Analyzer taxonomy hiding a live safety regression.
- Full declarative rewrite of deterministic rules.
- Broad cache-retention hazards beyond the guide/index and artifact evidence
  listed above.

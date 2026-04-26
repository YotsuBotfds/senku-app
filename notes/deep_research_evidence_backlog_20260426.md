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
  index path was added. Follow-up run `24958108279` passed on head `4926d93`
  with retrieval index cache hit, proving the compact cache-hit policy too.
- Generated mode proof is complete: run `24958353451` passed on head
  `1d3832f` using the committed generated fixture and baseline diagnostics.
  Follow-up run `24958386310` passed on head `379181a` with FastEmbed model
  cache and retrieval-index cache steps skipped, proving Generated mode no-DB
  behavior and the skip-FastEmbed optimization.
- Keep the original CI-health claim scoped to the old failing run; current
  behavior is now proven separately by run `24957922497`.

Validation:

- Run `24957922497` completed green on `2026-04-26`; run `24958108279`
  completed green on `2026-04-26` after restoring the retrieval index cache.
  Runs `24958353451` and `24958386310` completed green on `2026-04-26` for
  the Generated fixture path. Run `24957798681` previously proved compact
  ingest finished and exposed only a strict-warning mismatch, not FastEmbed
  service lifetime/index rebuild budget.

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
- Implemented executable classifier proof:
  `tests.test_ingest_freshness.test_changed_guide_is_blocking_until_manifest_sha_refreshes`
  edits a temporary guide, observes `stale` plus `is_blocking`, refreshes the
  manifest, and observes `fresh` plus non-blocking status.
- Added deleted-guide coverage:
  `tests.test_ingest_freshness.test_deleted_guide_leaves_blocking_extra_manifest_key`
  proves an extra manifest key from a removed guide is also blocking stale
  state, matching the incremental-ingest planner's rebuild-required policy.
- Added query-level preflight coverage:
  `tests.test_query_ingest_freshness` pins the startup policy directly:
  trusted stale manifests block unless `--allow-stale-ingest` is set, while
  `incomplete_untrusted` and `absent_or_invalid` warn without blocking.

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
- Implemented low-risk visibility slice: `scripts\validate_special_cases.py`
  now accepts `--overlap-matrix-json` and writes flattened overlap rows with
  source rule, sample prompt, matched rule, priority, lexical signature size,
  promotion status, winner IDs, winner reason, and winner flag. Existing
  validation semantics are unchanged.

Validation:

- Existing deterministic/routing unit slices plus validator still pass; the
  matrix exposes every overlap and no unresolved pair lacks an explicit winner.
- Focused validation produced a matrix JSON and still reported `176`
  deterministic special-case rules, `7` overlap checks, and one winner per
  overlap.

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
- Implemented `DR-DIAG-ACCEPT-001`: deterministic emergency answers with
  explicit emergency-first text can now surface `emergency_first_supported`
  and `ui_surface_bucket=emergency_first` without changing the acceptance
  status. The unit proof uses the `RE9-SM-001` carbon-monoxide/storm pattern
  and keeps `app_acceptance_status=strong_supported`. Follow-up table coverage
  applies the same acceptance/taxonomy split to `RE9-AN-001`, `RE9-PO-001`,
  and `RE9-MH-001`.
- Added `RE8-TR-001` route/delegation proof without changing generated answer
  behavior. The focused routing test asserts the exact evacuation triage /
  animal logistics prompt stays out of deterministic special cases, gets human
  triage supplemental retrieval specs, and prefers `GD-029` over animal/route
  support owners.
- Added exact `RE9-MH-001` deterministic route proof. The existing
  no-sleep/activation crisis test now includes the wildfire evacuation /
  animal-trailer prompt and asserts `GD-859` safety-first response text stays
  ahead of logistics.
- Added exact `RE9-AN-001` deterministic route proof. The anaphylaxis test now
  includes the beekeeper / flooded-road / antihistamine lure prompt and asserts
  epinephrine plus emergency help stay ahead of flood-route logistics.
- Added exact `RE9-SM-001` and `RE9-PO-001` deterministic route proofs in the
  existing carbon-monoxide and child-cleaner tests. The new prompts pin
  emergency-first CO evacuation and Poison Control/vomiting guidance ahead of
  storm breaker, wet outlet, flood-water, and forced-fluid distractors.
- Hardened no-FastEmbed CI surfaces: the PowerShell quality workflow now runs
  the existing gate with `-RequireAnalyzer -RequirePester`, and dependency
  security installs pinned `uv==0.11.7` before checking the generated lock.
- Added a Generated-mode `-WhatIf` contract proof that keeps the cheap smoke
  path on analyzer/regression commands only, with explicit negatives for
  retrieval packs, prompt expectation validation, and FastEmbed. Also added a
  generic workflow artifact policy check for short retention and missing-file
  errors on every upload step.

Validation:

- Acceptance gates remain pinned to stable behavior; diagnostic taxonomy can be
  adjusted without silently changing pass/fail meaning.
- Artifact-level analyzer proof against
  `artifacts\bench\RE9-SM-001_proof_20260426_20260426_062328_bench.json`
  produced `app_acceptance_status=strong_supported`,
  `safety_surface_status=emergency_first_supported`, and
  `ui_surface_bucket=emergency_first`.

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
- Cache hygiene guard added: the Non-Android retrieval-index cache restore key
  is scoped by mode, safety-critical flag, and `retrieval_index_flavor`, so a
  broad Windows-only restore cannot hydrate a DB from the wrong retrieval lane.
- Bench bundle artifact guard added: workflow tests now pin the bench-bundle
  build step, upload path, `if-no-files-found: error`, 14-day retention, and
  public-repository attestation subject path.
- Dependency security artifact guard extended: workflow tests now also pin
  `if-no-files-found: error` and 14-day retention for the pip-audit report.

Validation:

- Protected proof artifacts and current CI failure evidence remain in place;
  a post-action dry run can prove preserved protected totals and archived byte
  counts.
- `tests.test_github_workflows` asserts the retrieval cache key and restore
  prefix carry the same mode/safety/flavor dimensions.
- `tests.test_github_workflows` also asserts the bench-bundle upload and
  attestation stay tied to `steps.bench_bundle.outputs.bundle_zip`.
- The dependency-security workflow upload remains a small JSON report with
  explicit failure-on-missing and bounded retention.

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

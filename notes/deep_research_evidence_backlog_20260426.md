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
- Surfaced app-acceptance root-cause counts in compact RAG diagnostics
  summaries so supported/evidence-owner/card-contract/safety/gate-policy
  distribution is visible without opening raw analyzer output.
- Added row-level fallback for the same compact root-cause summary columns, so
  older diagnostics without summary-level root-cause counts still expose the
  distribution from per-row `app_acceptance_root_cause` fields.
- Added row-level acceptance-root-cause plumbing to diagnostics triage tools:
  `query_rag_diagnostics.py` can filter by `--acceptance-root-cause`, and RAG
  eval dataset exports preserve `app_acceptance_root_cause` in metadata.
- Extended row-level diagnostics queries with safety/UI surfacing filters:
  `--safety-surface-status` and `--ui-surface-bucket` now let triage isolate
  emergency-first or non-safety rows without opening the raw artifact.
- Added a Markdown-mode CLI proof for the same diagnostics filters, pinning the
  default operator path in addition to JSON output.
- Preserved safety/UI diagnostic facets in RAG eval dataset metadata so
  exported rows retain `safety_surface_status` and `ui_surface_bucket`
  alongside acceptance/root-cause fields.
- Added root-cause and safety/UI facet columns to `rag_trend.py`, with
  summary-level counts preferred and row-level fallback for older diagnostics.
- Hardened no-FastEmbed CI surfaces: the PowerShell quality workflow now runs
  the existing gate with `-RequireAnalyzer -RequirePester`, and dependency
  security installs pinned `uv==0.11.7` before checking the generated lock.
- Added a Generated-mode `-WhatIf` contract proof that keeps the cheap smoke
  path on analyzer/regression commands only, with explicit negatives for
  retrieval packs, prompt expectation validation, and FastEmbed. Also added a
  generic workflow artifact policy check for short retention and missing-file
  errors on every upload step.
- Added a Generated-mode step-summary proof for the same no-retrieval/no-FastEmbed
  contract, so `GITHUB_STEP_SUMMARY` preserves the cheap head-health command
  shape in `-WhatIf` runs.
- Hardened the runtime endpoint preflight used by guide prompt validation:
  malformed `/v1/models` JSON is now reported as a failed endpoint check
  instead of escaping as an uncaught parser exception.
- Pinned the runtime preflight fail-loud contract: expected-model mismatches
  exit nonzero by default, `--warn-only` is the explicit waiver, and JSON
  evidence still records `model_found=false`.
- Added direct token-estimation coverage for empty input, fallback word/
  punctuation splitting, dense-string character floors, and the tokenizer-backed
  encoder path without requiring the optional tokenizer dependency.
- Added direct metadata validation coverage for guide-record coercion, legacy
  `id` fallback, error truncation, report error detection, formatted failures,
  and JSON report writing.
- Added direct dual-model answer package coverage for CSV/JSONL prompt loading,
  prompt-key matching, duplicate artifact keys, missing/error/blank model
  blocks, zero duplicate-citation rendering, and Markdown output shape.
- Added diagnostics triage coverage proving `--guide-id` style filtering still
  matches guide IDs when diagnostic artifacts store expected, retrieved, or
  cited guide fields as JSON lists rather than pipe-delimited strings.
- Added run-manifest artifact evidence coverage for directory outputs, proving
  directory artifacts are recorded as existing `directory` entries with
  modification timestamps rather than being treated as missing files.
- Normalized run-manifest artifact paths so absolute repo-local outputs and
  backslash/forward-slash equivalents record one portable repo-relative path
  while still probing the real filesystem path for evidence.
- Added compare-bench CLI wrapper coverage for both stdout rendering and
  `--output` file writing, pinning the operator path around the existing
  artifact comparison engine.
- Tightened the `Master Head Health` workflow contract test around generated
  fixture inputs, head-health label shape, and compact retrieval-index flavor.
- Surfaced missing artifact path names in run-manifest summaries, capped through
  the existing compact-list rendering, so stale proof artifacts are identifiable
  without opening raw JSONL records.
- Added opt-in run-manifest summary failure gates for selected records with
  explicit missing artifact evidence and for malformed manifest lines.
- Added artifact-retention coverage proving newer run-manifest
  `artifact_path_evidence` entries protect referenced artifact families from
  archive/delete recommendations.
- Added allowed-drift suppression coverage proving prompt expectation
  suppressions can be scoped to an exact artifact path without hiding the same
  issue in other artifacts.
- Added retrieval-eval coverage proving prompt-pack primary expected owners are
  applied when a retrieval artifact omits its row-level primary owner fields.
- Added RAG trend coverage proving top-1 marker risk, bridge, and unresolved
  partial overlays fall back to row-level diagnostics when summary counts are
  absent.
- Added RAG eval exporter coverage for nested, comma-delimited, and duplicate
  guide-id normalization so exported metadata stays stable across artifact
  shapes.
- Added RAG eval trace coverage proving top-level trace identity and name-based
  phase fallback still attach spans and error phases to exported rows.
- Hardened the RAG trace OTel adapter for raw string status values so trace
  consumers do not fail before they can summarize mixed trace shapes.
- Added metadata-audit coverage proving body routing markers count as
  high-liability routing support without requiring explicit alias/routing-cue
  frontmatter for that specific support signal.
- Hardened metadata-audit citation-policy semantics so
  `citations_required: false` does not satisfy high-liability citation-policy
  coverage.
- Added bench metrics-lake coverage proving JSONL parse errors are recorded
  without dropping valid object rows or scalar-line metrics from the same file.
- Added metrics-lake ingestion for run-manifest `artifact_path_evidence`
  entries so present/missing artifact proof rows are queryable alongside the
  original JSONL records.
- Added artifact tooling coverage for default text-mode storage summaries and
  bench-artifact JSONL indexing that intentionally records `jsonl_not_read`.
- Added live-queue monitor coverage proving today's protected
  `PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md` note is treated as
  benign even when Windows-style backslashes appear in `git status --short`.
- Aligned agent startup snapshots with the same actionable-clean semantics,
  keeping protected planner handoff paths visible as benign context instead of
  dirty worktree entries.
- Aligned worktree-delta summaries with the protected planner handoff policy,
  excluding those local handoff notes from actionable changed-file counts while
  reporting the benign exclusion count.
- Aligned guide-edit impact planning with the protected planner handoff policy,
  so `--from-git-status` ignores local handoff notes and keeps validation
  planning focused on guide/card/runtime edits.
- Added structured worker-lane dirty summaries with parsed entry details and
  status counts while preserving the existing compact entries contract.
- Hardened bench Markdown response recovery so padded source/context headings
  stop answer extraction instead of leaking source bullets into fallback
  response text.
- Surfaced allowed mojibake finding counts in Markdown reports so report-only
  and allowlisted scans visibly separate total findings from gate findings.
- Added compact Markdown report summaries to the bench artifact index, including
  title, line count, and heading count under the existing size guard.
- Added retrieval-eval Markdown parsing for a `primary` column so Markdown
  artifacts can provide primary expected owners directly, matching JSON field
  semantics.
- Added a `Structural` mode to the non-Android gate for prompt-expectation-only
  validation of the partial/router and EVAL9 packs, with dry-run coverage
  proving it omits retrieval, generated-analysis, and FastEmbed surfaces.
- Added a `Master Head Health` push workflow that calls the reusable
  non-Android gate in `Generated` mode on every `master` push, giving HEAD an
  observable no-FastEmbed health signal without turning every commit into a
  retrieval rebuild.
- Adjusted the PowerShell analyzer gate to require analyzer availability while
  blocking only `Error` severity findings. Existing warning/information debt is
  still printed explicitly so the lane becomes visible without failing on the
  current backlog-sized warning wall.
- Added a CLI proof for the deterministic overlap matrix artifact:
  `validate_special_cases.py --overlap-matrix-json` now has test coverage that
  the generated rows include winner, priority, lexical-signature, and promotion
  metadata.
- Added a generated round-trip overlap proof that every matrix winner matches
  the live `classify_special_case()` result for the canonical overlap prompt.
- Added a diagnostics/acceptance separation proof: a generated answer with
  emergency-first wording and a `deterministic_pass` diagnostic bucket still
  remains `needs_evidence_owner` when it cites only a non-expected guide.
- Added a `master` push trigger to the PowerShell quality workflow for
  PowerShell/config path changes, matching its pull-request path filter so the
  analyzer/Pester lane becomes a commit-level signal when relevant files move.
- Added a no-Android PowerShell quality wrapper slice: the gate now accepts
  comma-separated `-Path` lists from `powershell -File`, and tests pin a
  wrapper-only dry run/parser pass that excludes Android, emulator, and ADB
  scripts while staying free of FastEmbed/retrieval commands.
- Added high-liability holdout-pack hygiene coverage across EVAL7/EVAL8/EVAL9:
  prompt IDs and prompt text must remain unique across packs, and
  `primary_expected_guides` must stay a subset of `expected_guides`.
- Added a behavior-contract guard for the same high-liability holdout packs:
  every row now needs target behavior, scenario/risk/fair-test metadata,
  expected behavior, required concepts, and forbidden/suspicious actions.
- Added high-liability retrieval-smoke-first hygiene coverage proving current
  EVAL8/EVAL9 smoke-first rows keep primary owner metadata inside expected
  guide sets and expose at least one forbidden/suspicious cue in prompt context.
- Added a `master` push trigger to the Dependency Security Scan workflow for
  dependency/security-tooling path changes, matching the pull-request path
  filter so dependency scan health becomes visible on relevant HEAD updates.
- Added an explicit no-path-filter guard for the `Master Head Health` workflow:
  it must run on every `master` push, not only selected path changes.
- Added `Master Head Health` concurrency keyed by workflow/ref with
  cancel-in-progress, so rapid `master` pushes keep the latest Generated-mode
  no-FastEmbed signal instead of stacking duplicate head-health runs.
- Added a reusable non-Android workflow input mirror guard so manual-dispatch
  inputs and workflow-call inputs keep compatible defaults and types.
- Added a strict-priority live-overlap guard for deterministic special cases:
  current overlap fixtures must resolve by priority, with no live dependence
  on lexical-signature or first-defined tie-breaks.
- Added partial-router allowed-drift manifest hygiene: suppressions must target
  real prompt rows, carry non-empty issue/guide/reason fields, overlap expected
  guide metadata, and avoid duplicate prompt/issue/guide triplets.
- Narrowed the high-liability holdout schema guard: EVAL7/EVAL8/EVAL9 rows now
  pin lane/style/status/risk vocabularies and unique required/forbidden text.
- Added a CI bench-bundle size budget: the non-Android workflow records the
  budget in the manifest, verifies required manifest fields, fails if the zip
  exceeds 10 MiB, and emits the compressed byte count as step output.
- Made the Dependency Security Scan fail closed for missing scanner tooling in
  CI by removing `-SkipIfUnavailable`; a focused test proves the script already
  exits nonzero by default when no audit tool is available.
- Added a stable `app_acceptance_root_cause` field so acceptance outcomes carry
  a single machine-checkable cause tag (`gate_policy`, `safety_surface`,
  `evidence_owner`, `card_contract`, or `supported`) separate from the richer
  diagnostic reason string.
- Surfaced `app_acceptance_root_cause` through the bench-failure analyzer CSV,
  JSON summary counts, and Markdown App Acceptance section so root-cause
  distribution is visible in generated diagnostics artifacts.
- Promoted the primary-owner subset rule into prompt expectation validation:
  `primary_expected_guides` / `primary_expected_guide_ids` now fail when they
  name guides outside the broader expected-guide metadata.
- Pinned legacy ingest-manifest compatibility: basename-keyed entries and
  renamed legacy keys with matching SHA remain fresh, while unmatched keys with
  unknown SHA stay blocking stale evidence.
- Hardened RAG trace privacy: direct trace writes and OTel event conversion now
  sanitize prompt/question attributes just like normal span attributes.
- Added run-manifest artifact evidence: capped artifact paths now record
  present/missing status, kind, modified timestamp for present paths, and a
  missing-path count so stale or absent proof files are visible in manifests.
- Surfaced run-manifest artifact health in the Markdown summary table, including
  artifact path count, missing count, truncation, and dirty-worktree status.
- Preserved legacy run-manifest summary semantics by rendering records with no
  artifact evidence fields as `paths=n/a` instead of implying
  `paths=0; missing=0`; partial evidence records keep the existing defaults.
- Added PowerShell quality-gate flag validation: `-RequireAnalyzer` cannot be
  paired with `-SkipAnalyzer`, and `-RequirePester` cannot be paired with
  `-SkipPester`.
- Surfaced reviewed-card runtime visibility in RAG diagnostics summaries:
  runtime-enabled and runtime-disabled artifact counts now appear in compact
  Markdown/JSON summary rows.
- Extended reviewed-card runtime visibility in compact RAG diagnostics
  summaries: runtime-unknown artifact counts now appear beside enabled and
  disabled counts, with row-level fallback from
  `artifact_reviewed_card_runtime_answers` when summary counts are absent.
- Extended trace redaction to plural raw prompt/question fields (`prompts`,
  `questions`) while preserving safe metadata suffixes like ids, hash, index,
  and count.
- Added bounded no-FastEmbed compact diagnostics display coverage: the
  Bad/Interesting Rows table now surfaces row-level
  `app_acceptance_root_cause`, `safety_surface_status`, and
  `ui_surface_bucket` without changing retrieval, FastEmbed, or acceptance
  behavior.

Validation:

- Acceptance gates remain pinned to stable behavior; diagnostic taxonomy can be
  adjusted without silently changing pass/fail meaning.
- Artifact-level analyzer proof against
  `artifacts\bench\RE9-SM-001_proof_20260426_20260426_062328_bench.json`
  produced `app_acceptance_status=strong_supported`,
  `safety_surface_status=emergency_first_supported`, and
  `ui_surface_bucket=emergency_first`.
- Run-manifest Markdown summary tests now cover legacy no-evidence records and
  partial artifact-evidence records.
- Compact RAG diagnostics summary tests now cover
  `reviewed_card_runtime_unknown` from summary-level artifact counts and
  fallback from row-level runtime-answer status.
- Compact RAG diagnostics row-table tests now cover the new acceptance root
  cause, safety surface, and UI surface columns, including Markdown escaping.

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

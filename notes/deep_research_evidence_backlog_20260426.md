# Deep Research Evidence Backlog - 2026-04-26

Scope: running deep-research evidence backlog. Early sections preserve the
initial triage context; later bullets record continuation fixes and validation.
Do not treat historical proof runs as current HEAD health without checking the
latest `git log -1` and `gh run list` output.

## Evidence Read

- `notes/deep_research_report_triage_20260426.md`
- `C:\Users\tateb\Downloads\deep-research-report.md`
- Initial local git history inspected when this backlog was opened:
  `eb71a3c` through `36d306b`, including CI, FastEmbed, ingest freshness, and
  retrieval-index cache commits. Later continuation commits are recorded in the
  action/validation bullets below.
- Current notes: `notes/AGENT_TOOLING_UPGRADES_20260426.md`,
  `notes/high_liability_smoke_coverage_inventory_20260426.md`,
  `notes/artifact_retention_cleanup_proposal_20260426.md`,
  `notes/dispatch/RAG-TOOL6_incremental_ingest_planner.md`.
- Current code/test anchors inspected for evidence:
  `.github/workflows/non_android_regression.yml`, `ingest_freshness.py`,
  `tests/test_ingest_freshness.py`, `tests/test_registry_overlap.py`,
  `tests/test_deterministic_near_miss.py`.

## Backlog

### DR-CI-VERIFY - Track Current Non-Android Gate Health

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

- Historical compact Fast evidence includes `Non-Android Regression Gate` run
  `24957922497` on head `eb71a3c` in `Fast` mode after the compact
  cache-miss smoke index path was added. Follow-up run `24958108279` passed on
  head `4926d93` with retrieval index cache hit, and also covered the compact
  cache-hit policy.
- Historical Generated mode evidence includes run `24958353451` on head `1d3832f`
  using the committed generated fixture and baseline diagnostics. Follow-up run
  `24958386310` on head `379181a` passed with FastEmbed model cache and
  retrieval-index cache steps skipped, covering Generated mode no-DB behavior and
  the skip-FastEmbed optimization.
- Keep the original CI-health claim scoped to the old failing run; current
  behavior must be checked against the latest head-health run for the current
  commit train.

Validation:

- Historical run `24957922497` completed green on `2026-04-26`; historical run
  `24958108279` completed green on `2026-04-26` after restoring the retrieval
  index cache. Historical runs `24958353451` and `24958386310` completed green
  on `2026-04-26` for the Generated fixture path. Run `24957798681` showed
  compact ingest finished and exposed only a strict-warning mismatch, not
  FastEmbed service lifetime/index rebuild budget.

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
- Query startup freshness messaging now explicitly distinguishes blocked stale
  ingest from warning-only incomplete/absent manifest states, preserving the
  fail-closed policy while making the operator path harder to misread.

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
- Extended row-level diagnostics queries with a repeatable
  `--app-acceptance-status` filter so operators can compose acceptance and
  safety-surface allowlists without collapsing legitimate non-safety strong
  rows into emergency-first requirements.
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
- Added PowerShell non-Android gate wrapper contract coverage for blank
  generated-baseline diagnostics and `-Mode All` dry-run command composition.
- Extended the same PowerShell wrapper proof so `-Mode All -IncludeSafetyCritical`
  dry runs include Fast-family, safety-critical, and Generated command families.
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
- Run-manifest artifact path collection now rejects absolute paths outside the
  repository even when they contain an `artifacts/` segment, preventing
  unrelated external files from inflating artifact evidence.
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
- RAG trend owner-metric fallback now normalizes list-backed
  `expected_guide_ids` before row selection, preventing list-shaped diagnostic
  rows from crashing trend collection.
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
- Hardened guide-catalog loading so malformed YAML frontmatter is ignored like
  non-mapping frontmatter, allowing valid sibling guides to load.
- Added bench metrics-lake coverage proving JSONL parse errors are recorded
  without dropping valid object rows or scalar-line metrics from the same file.
- Added metrics-lake ingestion for run-manifest `artifact_path_evidence`
  entries so present/missing artifact proof rows are queryable alongside the
  original JSONL records.
- Added artifact tooling coverage for default text-mode storage summaries and
  bench-artifact JSONL indexing that intentionally records `jsonl_not_read`.
- Tightened the artifact-storage summary CLI so non-positive `--limit` values
  are rejected while the library function still supports zero-limit summaries.
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
- Tightened the reusable non-Android workflow input mirror guard so
  `workflow_dispatch` and `workflow_call` cannot grow unreviewed extra inputs
  on either side of the contract.
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
- Dependency security scan tests now also pin the explicit
  `-SkipIfUnavailable` escape hatch as non-failing and report-free when scanner
  tooling is unavailable.
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
- Hardened compact diagnostics and priority tooling against benign row-shape
  variants: row-level fallback status counts now trim status cells, generated
  shadow-card gaps accept padded `yes` values, guide-priority IDs accept comma
  separators, and high-liability family prioritization normalizes status-like
  booleans plus pipe-delimited metadata gaps.
- Hardened retrieval-eval row normalization across related evidence tools:
  nested/dict guide-id records now feed contextual-shadow comparisons and
  retrieval-pack evaluation, and prompt-expectation validation accepts a
  top-level JSON object that is itself a retrieval-eval row.
- Hardened RAG eval dataset export guide-id parsing so mixed whitespace,
  comma, and pipe-delimited diagnostic/retrieval artifact fields normalize
  through the same de-duplicated guide-id path.
- Hardened analyzer and retrieval-profile comparison evidence paths: categorical
  analyzer labels now normalize benign casing/whitespace/hyphen variants before
  gate/status/root-cause/safety-surface counting, and profile comparisons
  normalize guide-id/status row shapes before summary aggregation.
- Hardened additional evidence surfaces against malformed or hostile artifact
  shapes: artifact-storage text summaries sanitize multiline values and
  malformed byte counts, bench artifact eval rows sanitize list fields,
  compare-bench CLI output strips control characters, RAG trace direct writes
  sanitize single-event mappings, diagnostic filters normalize padded/case
  variants, and startup snapshots render malformed metadata-audit counts as
  unavailable instead of leaking raw structures.
- Hardened queue/metadata planning helpers: live queue monitor ignores
  `git status --short --branch` metadata lines, guide edit impact planning
  recognizes newly named protected planner handoffs as benign only when
  untracked, and metadata coverage treats list/dict citation policy values as
  present only when they contain a meaningful truthy value.
- Hardened adjacent worker/status/text-quality helpers: worktree delta
  summaries ignore porcelain branch headers, worker-lane Markdown tables
  flatten newlines and escape pipes, and mojibake touched-path collection
  normalizes Windows separators plus `.`/`..` segments before de-duping and
  allowlist checks.
- Hardened prompt/guide lineage helpers: prompt-pack indexing now reports
  recoverable CSV extra-column rows without dropping usable prompts, prompt-pack
  guide selection skips malformed JSONL rows, lineage Markdown escapes table
  cell breakers, guide graph frontmatter relations normalize file-like slug
  references before de-duping, section-family shadow file selection distinguishes
  basename requests from path-shaped wrong-directory requests, and metadata
  validation preserves false-like scalar guide IDs instead of falling back.
- Hardened trend/metrics/shadow evidence outputs: contextual-shadow artifacts
  now serialize non-finite values as strict JSON `null`, shadow-comparison
  Markdown table cells flatten newlines before pipe escaping, RAG trend row
  fallback ignores structured status values instead of stringifying them, and
  metrics-lake run-manifest ingestion accepts a single artifact-evidence object.

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
- Bench artifact indexing now summarizes small JSONL files under the existing
  size cap with line/object/malformed counts and bounded type counts, without
  retaining raw prompt/body content.
- Run-manifest Markdown rendering now strips carriage returns and newlines from
  table cells before pipe escaping, preventing manifest-derived text from
  injecting extra table rows.
- Artifact-retention reference scans now ignore protected local
  `PLANNER_HANDOFF*.md` notes, so continuation handoffs do not accidentally
  protect generated artifact families.
- Artifact-retention manifest coverage now proves malformed JSONL lines do not
  abort planning while valid `artifact_path_evidence` and output references in
  the same manifest still protect families.
- Guide edit impact planning now suggests focused sibling tests for root runtime
  modules such as `guide_catalog.py`, not only `scripts/*.py` helpers.
- Prompt expectation validation now fails explicit retrieval primary-owner
  fields that are blank or malformed, including Markdown `primary` aliases,
  instead of silently falling back to prompt-pack metadata.
- Live queue monitor output now renders worker-lane dirty status counts
  (`modified`, `untracked`, and related categories) from the structured worker
  lane payload instead of only showing a raw changed count.
- Metadata coverage auditing now treats false-like strings such as `"false"`,
  `"0"`, `"no"`, and `"none"` as absent policy metadata, matching boolean
  false semantics for high-liability citation-policy checks.
- Metadata coverage auditing now also treats numeric zero citation-policy
  values as absent, matching the existing false-like string and boolean false
  semantics.
- Run-manifest artifact collection now dedupes equivalent artifact paths by
  canonical real path and returns an empty summary for non-positive library
  limits while preserving the CLI's fail-closed limit validation.
- Run-manifest artifact evidence now checks every normalized artifact candidate
  for missing status even when the displayed `artifact_path` list is capped, so
  truncated manifests cannot report `missing=0` just because the missing output
  fell past the display limit.
- Bench metrics-lake ingestion now normalizes run-manifest
  `artifact_path_evidence` rows by deriving missing `status` from boolean
  `exists` and backing up missing `path` from legacy `artifact_path`.
- Bench metrics-lake artifact evidence normalization now treats blank status
  and path strings as missing values, so boolean `exists` and legacy
  `artifact_path` still produce canonical SQL fields.
- Retrieval-pack evaluation now dedupes expected and primary expected guide IDs
  before scoring/rendering, and uses a display-only `primary_guide_ids` Markdown
  column so blank generated report cells do not masquerade as explicit primary
  expectations.
- Contextual shadow comparison now aligns primary expected owners to the
  broader expected-owner set and dedupes them before primary-hit scoring.
- Agent startup snapshots now have regression coverage proving malformed
  run-manifest lines, missing artifact health, and benign protected handoff
  notes surface together in the first-page context.
- Prompt expectation validation now has explicit coverage that generated
  retrieval reports' display-only `primary_guide_ids` column does not count as
  an explicit primary expectation, while prompt-pack primary owners still drive
  primary-owner retrieval checks.
- Shadow-comparison summaries now have regression coverage for missing
  primary-owner blocks and zero-denominator primary metrics, keeping optional
  primary fields stable when absent.
- Shadow-comparison summaries now skip blank CSV rows in row-count fallback and
  accept positive numeric-string `row_count` values from summary JSON.
- Bench artifact JSONL summaries now count non-object lines separately,
  truncate long type values deterministically, and preserve frequency order in
  formatted type-count summaries.
- Bench artifact indexing now records per-file stat failures as
  `stat_unreadable` summary rows instead of aborting the entire index pass.
- Bench artifact Markdown index rendering now strips carriage returns from
  escaped cells so copied titles or summaries cannot warp table rows.
- Audit follow-up tightened CI semantics after the 50-commit review: compact
  retrieval warnings are no longer auto-waived, `-AllowRetrievalWarnings` is
  passed only by explicit input, and Master Head Health is named/scoped as a
  generated-fixture head-health check rather than live retrieval proof.
- Malformed guide frontmatter now has a loud discovery path:
  `guide_catalog.find_malformed_frontmatter()` reports malformed files while
  catalog loading still skips them and keeps valid siblings available.
- Metadata coverage now audits malformed high-liability frontmatter as a gap,
  and separates structured routing support from body-only routing hints so
  broad prose cannot hide missing structured routing cues.
- Metrics-lake `artifact_path_evidence` detail rows now inherit parent
  run-manifest context (`task`, `lane`, `label`, `commit`, `generated_at`, and
  `record_type`) in raw JSON for easier provenance joins.
- Added a separate Strict Retrieval Head Health workflow that runs on schedule
  or manual dispatch only, uses the reusable non-Android Fast gate with full
  retrieval index flavor, and keeps retrieval warnings fail-closed by default.
- Master Head Health and Strict Retrieval Head Health workflow contract tests
  now assert their manual dispatch triggers remain input-less, preventing
  accidental operator overrides of generated/strict health semantics.
- Artifact retention now distinguishes local/untracked `PLANNER_HANDOFF*.md`
  notes from committed durable handoffs: local handoff noise stays ignored, but
  tracked handoff references can protect artifact families.
- Artifact retention now treats protected handoff references as durable only
  when they exist in `HEAD`; staged-but-uncommitted handoffs remain local noise
  and cannot suppress retention candidates.
- Metrics-lake detail rows now expose artifact evidence parent context in
  dedicated SQL columns (`evidence_task`, `evidence_lane`, `evidence_label`,
  `evidence_commit`, `evidence_generated_at`, and `evidence_record_type`) while
  preserving raw JSON as canonical evidence.
- Metrics-lake artifact ingestion now records a per-file stat/read disappearance
  as an artifact error row instead of aborting ingestion for valid sibling
  artifacts.
- Metrics-lake schema initialization now has legacy-DB regression coverage
  proving old `detail_rows` tables are backfilled with the new evidence-context
  columns before artifact evidence inserts.
- Retention planner now has an integration-style temporary-git regression
  proving tracked planner handoffs protect artifacts while untracked planner
  handoffs remain ignored as local noise.
- Metadata coverage Markdown now includes a malformed-frontmatter summary and
  table with source file plus parser reason so operators can see malformed guide
  evidence without opening JSON.
- Agent startup snapshots now surface the latest metadata-audit malformed
  frontmatter count and strict retrieval workflow mode/warning/index-flavor
  settings in the Recent Run Manifest section.
- Agent startup snapshot tests now assert the exact strict retrieval workflow
  config values surfaced in the summary line.
- Run-manifest summaries now derive artifact and missing-artifact counts from
  evidence lists when malformed count scalars are present, preventing bad
  manifest values from hiding missing artifact evidence.
- Run-manifest count normalization now rejects booleans, non-integral floats,
  and negative counts as evidence-derived values, including scalar evidence
  paths recorded by older or partial manifest writers.
- Run-manifest summaries now treat `artifact_path_missing` evidence as missing
  artifact proof even when no count field is present, and report total matched
  records before applying the display limit.

Validation:

- Protected proof artifacts and historical CI failure evidence remain in place;
  a post-action dry run can prove preserved protected totals and archived byte
  counts.
- `tests.test_github_workflows` asserts the retrieval cache key and restore
  prefix carry the same mode/safety/flavor dimensions.
- `tests.test_github_workflows` also asserts the bench-bundle upload and
  attestation stay tied to `steps.bench_bundle.outputs.bundle_zip`.
- The dependency-security workflow upload remains a small JSON report with
  explicit failure-on-missing and bounded retention.
- `tests.test_index_bench_artifacts` covers the JSONL summary path, large-JSONL
  skip behavior, and Markdown formatting for the exposed counts.
- `tests.test_summarize_run_manifest` covers newline/carriage-return sanitation
  in generated Markdown table rows.
- `tests.test_plan_artifact_retention` covers ignored planner handoff refs while
  preserving ordinary note-based artifact protection.
- `tests.test_plan_artifact_retention` also covers malformed manifest lines
  alongside valid manifest references.
- `tests.test_guide_edit_impact` covers root runtime sibling test discovery and
  no-op behavior when no sibling test exists.
- `tests.test_validate_prompt_expectations` covers blank and malformed Markdown
  primary-owner fields as validation failures.
- `tests.test_live_queue_monitor` covers structured worker-lane dirty counts
  flowing through monitor data and HTML rendering.
- `tests.test_audit_metadata_coverage` covers quoted false citation-policy
  metadata as a missing high-liability policy signal.
- `tests.test_audit_metadata_coverage` covers numeric zero citation-policy
  metadata as a missing high-liability policy signal.
- `tests.test_write_run_manifest` covers canonical artifact path dedupe and
  zero-limit library behavior for artifact collection.
- `tests.test_write_run_manifest` covers hidden missing artifacts beyond the
  displayed artifact-path cap still contributing to `artifact_path_missing` and
  `artifact_path_missing_count`.
- `tests.test_write_run_manifest` covers rejection of external absolute
  artifact-looking paths outside the repository root.
- `tests.test_bench_metrics_lake` covers malformed/scalar JSONL lines plus
  normalized `artifact_path_evidence` status/path preservation.
- `tests.test_bench_metrics_lake` covers blank artifact-evidence status/path
  normalization from `exists` and legacy `artifact_path`.
- `tests.test_evaluate_retrieval_pack` covers duplicate primary-owner metadata
  normalization and generated Markdown reparse behavior for blank primary rows.
- `tests.test_compare_contextual_shadow_retrieval` covers primary-owner subset
  alignment in both expectation extraction and row comparison.
- `tests.test_rag_trend` covers row-level owner-metric fallback for list-backed
  `expected_guide_ids`.
- `tests.test_agent_context_snapshot` covers malformed manifest and missing
  artifact signals alongside benign protected handoff context.
- `tests.test_validate_prompt_expectations` covers the generated
  `primary_guide_ids` display column contract.
- `tests.test_query_rag_diagnostics` covers app-acceptance status filtering
  composed with safety-surface allowlists.
- `tests.test_summarize_shadow_comparisons` covers optional/zero-denominator
  primary metric summary behavior.
- `tests.test_summarize_shadow_comparisons` covers blank CSV row-count fallback
  and numeric-string summary `row_count` coercion.
- `tests.test_index_bench_artifacts` covers JSONL non-object counts, long type
  truncation, and count-order formatting.
- `tests.test_index_bench_artifacts` covers per-file stat failures remaining
  visible without aborting bench artifact indexing.
- `tests.test_index_bench_artifacts` covers carriage-return sanitation in
  rendered Markdown index rows.
- `tests.test_github_workflows` covers explicit retrieval-warning opt-in and
  generated-fixture head-health workflow semantics.
- `tests.test_guide_catalog` covers malformed frontmatter reporting while
  preserving valid sibling catalog loading.
- `tests.test_audit_metadata_coverage` covers malformed high-liability
  frontmatter gap surfacing and structured-vs-body routing semantics.
- `tests.test_bench_metrics_lake` covers inherited parent manifest context on
  artifact evidence rows.
- `tests.test_github_workflows` covers the strict retrieval health workflow's
  manual/scheduled triggers, no push trigger, full index flavor, and
  fail-on-warning defaults.
- `tests.test_dependency_security_scan` covers the explicit
  `-SkipIfUnavailable` scanner-unavailable path staying non-failing without
  creating a report.
- `tests.test_github_workflows` covers input-less manual dispatch for generated
  head health and strict retrieval head health workflows.
- `tests.test_github_workflows` covers exact reusable non-Android workflow input
  parity, preventing extra manual-dispatch or workflow-call-only inputs.
- `tests.test_run_non_android_regression_gate` covers generated-baseline
  fail-loud behavior and `-Mode All` dry-run command composition.
- `tests.test_run_non_android_regression_gate` covers
  `-Mode All -IncludeSafetyCritical` dry-run composition across all command
  families.
- `tests.test_query_ingest_freshness` covers explicit blocked-stale and
  warning-only startup messaging.
- `tests.test_plan_artifact_retention` covers ignored untracked planner
  handoffs and tracked planner handoffs that protect referenced artifacts.
- `tests.test_bench_metrics_lake` covers direct SQL access to artifact evidence
  parent context columns and entity-id path joinability.
- `tests.test_bench_metrics_lake` covers artifacts disappearing between
  discovery and stat without aborting sibling metric ingestion.
- `tests.test_bench_metrics_lake` also covers legacy SQLite schema upgrade for
  evidence-context columns.
- `tests.test_plan_artifact_retention` now exercises real `git ls-files`
  detection for tracked planner handoff refs when git is available.
- `tests.test_plan_artifact_retention` covers staged-but-uncommitted planner
  handoff refs staying unprotected even when present in the git index.
- `tests.test_audit_metadata_coverage` covers malformed-frontmatter summary
  counts and Markdown reporting.
- `tests.test_agent_context_snapshot` covers metadata-audit and strict
  retrieval workflow signals in the startup snapshot.
- `tests.test_agent_context_snapshot` covers exact strict retrieval workflow
  signal values in the startup snapshot.
- `tests.test_summarize_run_manifest` covers malformed artifact-count scalar
  fallback to `artifact_path` and `artifact_path_missing` evidence lists.
- `tests.test_summarize_run_manifest` also covers scalar evidence fallback plus
  boolean, fractional-float, and negative artifact-count inputs.
- `tests.test_summarize_run_manifest` covers missing-path-only artifact
  evidence and pre-limit `Records matched` reporting.
- `tests.test_summarize_rag_diagnostics`,
  `tests.test_prioritize_guide_quality_work`,
  `tests.test_prioritize_high_liability_families`,
  `tests.test_compare_contextual_shadow_retrieval`,
  `tests.test_evaluate_retrieval_pack`, and
  `tests.test_validate_prompt_expectations` cover the row-shape normalization
  batch for padded statuses, comma-delimited IDs, dict guide-id records, nested
  retrieval-pack records, and single-object retrieval eval JSON.
- `tests.test_export_rag_eval_dataset` covers mixed whitespace guide-id
  normalization across nested dict/list diagnostic metadata shapes.
- `tests.test_analyze_rag_bench_failures` and
  `tests.test_compare_retrieval_profiles` cover analyzer category
  normalization plus retrieval-profile guide/status row-shape normalization.
- `tests.test_summarize_artifact_storage`,
  `tests.test_bench_artifact_tools`, `tests.test_compare_bench_artifacts`,
  `tests.test_rag_trace`, `tests.test_query_rag_diagnostics`, and
  `tests.test_agent_context_snapshot` cover the malformed evidence-surface
  hardening batch across text rendering, eval-row extraction, CLI Markdown,
  trace events, diagnostics filters, and startup snapshot signals.
- `tests.test_live_queue_monitor`, `tests.test_guide_edit_impact`, and
  `tests.test_audit_metadata_coverage` cover branch-header status parsing,
  pattern-based protected planner handoff handling, and list-shaped false-like
  citation metadata.
- `tests.test_summarize_worktree_delta`, `tests.test_worker_lane_status`, and
  `tests.test_scan_mojibake` cover branch-header status parsing, worker-lane
  Markdown table sanitation, and normalized touched-path/allowlist handling.
- `tests.test_index_prompt_packs`, `tests.test_select_prompt_pack_guides`,
  `tests.test_rag_experiment_lineage`, `tests.test_build_guide_graph`,
  `tests.test_export_section_family_shadow`, and
  `tests.test_metadata_validation` cover the prompt/guide lineage hardening
  batch.
- `tests.test_compare_contextual_shadow_retrieval`,
  `tests.test_summarize_shadow_comparisons`, `tests.test_rag_trend`, and
  `tests.test_bench_metrics_lake` cover strict JSON sanitation, shadow
  Markdown cell sanitation, row-status trend fallback, and single-object
  artifact-evidence ingestion.

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

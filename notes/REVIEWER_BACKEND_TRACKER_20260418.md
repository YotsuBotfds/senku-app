# Reviewer Backend Tracker

Date: 2026-04-18

Purpose:
- Execution companion to the root `reviewer_backend_tasks.md` plan.
- Keep active work centered on the three reviewer-flagged backend unknowns:
  deterministic false-positive risk, low-applicability abstain, on-device
  latency honesty.
- Use this file for live execution order and lane policy, not for the full
  task bodies — those live in the root plan.

Wave B dispatch trigger — **cleared 2026-04-18**:
- `OPUS-E-06` landed (`b41128a`). `AnswerPresenter` and
  `DetailAnswerPresenterHost` now own the three former
  `OfflineAnswerEngine.generate()` callsites (pre-E-06 `DetailActivity.java`
  lines 2939, 3064, 3186). `DetailActivity.java` is 6264 → 6063 lines.
  JVM verification: `./gradlew :app:testDebugUnitTest --tests
  com.senku.mobile.AnswerPresenterTest` green. Instrumented smoke:
  `scripts/run_android_instrumented_ui_smoke.ps1 -Device emulator-5554`
  2/2. Full posture sweep: `scripts/build_android_ui_state_pack_parallel.ps1`
  green at 45/45 across `5556 / 5560 / 5554 / 5558`.
- `OPUS-E-06` was a narrowly-scoped carve-out from `OPUS-E-05`; landing it
  did not require resuming the wider E-05 refactor and did not attempt the
  CP9 line-count gate. `OPUS-E-05` stays deferred. Spec:
  `notes/specs/answer_presenter_extraction_spec.md`.
- CP9 (release-candidate gate) remains independent of E-06.

## Current State

The reviewer packet
([`REVIEWER_BACKEND_UNKNOWNS_20260418.md`](./REVIEWER_BACKEND_UNKNOWNS_20260418.md))
confirms the backend is structurally strong — hybrid retrieval plus metadata-
aware reranking, session-aware rather than purely single-turn, real offline
path, explicit abstain, narrow deterministic surface. The remaining work is the
more mature class of problem:

As of 2026-04-18, Wave A is 23 done + 3 closeout rows pending re-run
(`BACK-R-03`, `BACK-P-01`, `BACK-P-02`) + 1 invalidated (`BACK-H-05`) out of
26 original tasks. `BACK-P-04` (pack export SHA race) landed as `bbc1b1d`;
the re-exported stock bundle is `aa1b399`. The closeout re-run on the
post-P-04 bundle (`BACK-P-02` → `BACK-P-01` → `BACK-R-03`) is the only
remaining path to formally close Wave A. Wave B (`BACK-U-01`, `U-02`,
`U-03`) is un-gated — `OPUS-E-06` landed (`b41128a`).

- **Deterministic false-positive control.** Backend-side closure is landed:
  graduation manifest, promotion metadata, semantic exclusion-list gate,
  builder-missing telemetry, equal-priority tie-break, near-miss tests, and
  mobile parity guard are all in.
- **Uncertainty communication.** Backend-side coverage now includes the abstain
  tuning note + regression runner (`BACK-U-04`) and the Wave B confidence-label
  contract tests. The remaining UI-crossing uncertainty work (`BACK-U-01` /
  `U-02` / `U-03`) is un-gated as of 2026-04-18 (`OPUS-E-06` landed
  `b41128a`) and dispatchable as Wave B.
- **On-device latency.** Backend instrumentation is landed: structured
  retrieval / rerank / prompt-build / first-token / decode / total timing,
  `SenkuLatency` events, `SessionMemory` persistence, `PackRepository` stage
  timing, and the desktop summary tool.

Adjacent findings from the full backend audit (not in the reviewer packet, but
worth tracking alongside):

- anchor-prior is now productized on desktop and Android; the remaining
  retrieval closeout is emulator validation for the idle-reset path in
  `BACK-R-03`
- bridge-guide demotion is now context-aware for planning/readiness acute
  queries
- FTS runtime selection and install-time schema validation are landed in code;
  `BACK-P-04` (pack export SHA race) landed (`bbc1b1d`); `BACK-P-01` / `BACK-P-02`
  emulator validation is pending on the post-P-04 stock bundle — see
  `notes/WAVE_A_CLOSEOUT_FAIL_2026-04-18.md`

## Worker Split

### `gpt-5.3-codex-spark` `xhigh` (scout)

Use for:
- near-miss false-positive brainstorming for deterministic rules
- cheap audits of whether a BACK task overlaps a landed OPUS task
- latency-panel schema drafting
- abstain-threshold sensitivity spike (read-only sweep)
- anchor-prior fate decision (code audit only)

Do not use for:
- code edits
- safety-sensitive adjudication when the scout is ambiguous

### `gpt-5.4` `high` (worker)

Use for:
- all code edits in `query.py`, `OfflineAnswerEngine.java`,
  `PackRepository.java`, `DeterministicAnswerRouter.java`,
  `deterministic_special_case_registry.py`
- semantic adjudicator implementation (safety-critical)
- `LatencyPanel` + `LiteRtModelRunner` instrumentation
- spec authoring for BACK-D-01, BACK-U-01, BACK-R-01 decisions

Default rule:
1. start with Spark xhigh for read-only scouting + near-miss panels
2. promote to GPT-5.4 high for the actual edits
3. do not re-dispatch scout work at worker tier

## Strategic Perspectives

### 1. Deterministic false-positive control first

Problem attacked:
- a deterministic false positive is more dangerous than an ordinary retrieval
  miss — it presents a wrong answer with a stronger evidence signal

Best lane:
- `deterministic_special_case_registry.py`, `DeterministicAnswerRouter.java`,
  `query.py` special-case path

Leverage:
- high

Risk:
- medium (verifier tuning can over-gate real matches; near-miss panel is the
  safety net)

Current implication:
- do not promote any new rule from pack metadata into the active set until
  the graduation manifest and semantic verifier gates are in place

### 2. Uncertain-fit as a distinct answer shape

Problem attacked:
- today's abstain gate is the only uncertainty signal; safety-critical
  queries that retrieve *something* plausible can slip through with a
  confident-shaped answer

Best lane:
- `query.py` abstain/answer pipeline, `OfflineAnswerEngine` branching,
  MetaStrip confidence rendering (landed UI)

Leverage:
- high

Risk:
- medium (need to pick thresholds that don't convert the whole retrieval
  surface into "uncertain")

Current implication:
- confidence-label plumbing (`BACK-U-03`) must land before the low-
  applicability branch (`BACK-U-01`) has a signal to key off

### 3. Latency honesty without overclaiming

Problem attacked:
- reviewer explicitly says current phone latency is not fully characterized,
  yet UI decisions are partly being made against it

Best lane:
- `OfflineAnswerEngine.generate()`, `LiteRtModelRunner` streaming callback,
  bench harness

Leverage:
- high (every subsequent UX decision improves once this lands)

Risk:
- low (pure instrumentation, no answer-path behavior change)

Current implication:
- land `BACK-L-01` + `BACK-L-02` as a bundle; baseline numbers in
  `artifacts/bench/latency_baseline_<date>/` before any further UI latency
  speculation

### 4. Anchor-prior: decide, don't drift

Problem attacked:
- `ENABLE_ANCHOR_PRIOR` is False but the machinery is alive, and OPUS-B-03
  (landed) already reads adjacent session state — we are effectively
  half-committed

Best lane:
- `query.py` + `notes/specs/anchor_prior_decision_<date>.md`

Leverage:
- medium-high

Risk:
- low (a code-audit scout call decides; then a single wave lands the choice)

Current implication:
- take the decision before Phase B-4 starts; do not ship `BACK-R-03` /
  `BACK-R-04` until the fate of anchor-prior is recorded

### 5. Pack install honesty

Problem attacked:
- pack install passes on size + SHA but can hand the app a SQLite with
  missing tables; the app finds out at query time

Best lane:
- `PackInstaller.java`, `PackRepository.java`

Leverage:
- medium (edge-case safety net; matters more when downloadable packs land)

Risk:
- low

Current implication:
- ship `BACK-P-02` opportunistically with any FTS runtime work (`BACK-P-01`)
  so pack robustness lands in one wave

### 6. Bridge-guide demotion is a blunt instrument

Problem attacked:
- uniform demotion on acute queries can bury primary sources for planning-
  adjacent prompts (e.g., clinic readiness)

Best lane:
- `query.py` reranker

Leverage:
- medium

Risk:
- medium (must not regress the mental-health lane)

Current implication:
- this waits behind Phase B-1 and rebases onto the latest mental-health
  hardening in `APP_ROUTING_HARDENING_TRACKER_20260417.md`

### 7. Mobile parity is assumed, not validated

Problem attacked:
- deterministic rules exported in the mobile pack have no automated check
  against Android predicate coverage

Best lane:
- `mobile_pack.py`, `scripts/`

Leverage:
- medium

Risk:
- low to medium

Current implication:
- `BACK-D-06` is a cheap, high-value safety net; land it inside the Phase
  B-1 wave so future rule-promotion waves can't silently fail

## Ranked Execution Order

### 1. Deterministic safety wave (Phase B-1)

Why first:
- reviewer's #1 unresolved risk
- cheap to ship in parallel (five of six tasks are S or smaller)
- unblocks future rule promotion work without raising the current risk

Current target:
- graduation manifest + registry metadata
- semantic verifier on the three most safety-critical live rules
- near-miss false-positive panel
- mobile parity check

Status:
- queued (this is the first dispatch)

### 2. Uncertainty vocabulary wave (Phase B-2)

Why second:
- reviewer's #2 unresolved risk
- depends on `BACK-U-03` confidence label landing first within the wave
- addresses the reviewer's sleep/pacing example directly

Current target:
- confidence label plumbed end-to-end
- low-applicability response mode live
- safety-critical abstain escalation line

Status:
- queued behind Phase B-1 dispatch

### 3. Latency instrumentation wave (Phase B-3)

Why third:
- reviewer's #3 unresolved risk
- pure instrumentation; safe to land anytime but most useful once B-1 and
  B-2 have stabilized answer shapes so the latency buckets are meaningful

Current target:
- TTFT, `LatencyPanel`, model-vs-harness breakdown, reranker timing
- baseline JSONL checked in under `artifacts/bench/`

Status:
- ready to interleave as soon as a worker slot opens; does not block B-1 or
  B-2

### 4. Retrieval refinement + pack hardening wave (Phase B-4)

Why fourth:
- resolves the anchor-prior half-commit
- context-aware bridge demotion is a routing-quality win but not safety-
  gating
- pack hardening is edge-case safety

Current target:
- anchor-prior decision recorded and implemented
- context-aware bridge demotion
- FTS runtime capability test
- SQLite schema validation on install

Status:
- gated behind reviewer priorities (#1, #2, #3)

### 5. Test coverage + hygiene lane (Phase B-5)

Why fifth:
- fill-lane; never gates a checkpoint but must run continuously
- hygiene tasks are safe-parallel, use them to keep worker slots warm

Status:
- always open; orchestrator fills slack capacity with H-lane work

## Active Findings From Initial Audit

### Deterministic lane

Landed:
- graduation manifest + promotion metadata now document why 9 live Android
  rules remain active out of 116 pack rules
- exclusion-list semantic verifier landed on desktop + Android
- builder-missing fallback now emits structured telemetry
- equal-priority tie-break is explicit instead of implicit
- near-miss panel covers all 9 live Android rules
- mobile-pack parity script guards desktop/Android deterministic drift

Status:
- D-lane closed on the backend side

### Uncertainty lane

Landed backend-side:
- `_should_abstain()` thresholds are now documented in the abstain tuning note
- regression runner + baseline artifact are checked in via `BACK-U-04`
- confidence-label contract tests are checked in so Wave B has a locked
  backend-side spec before UI plumbing starts

Still live:
- `BACK-U-01`, `BACK-U-02`, and `BACK-U-03` are **un-gated** as of 2026-04-18
  (`OPUS-E-06` landed `b41128a`); dispatch as Wave B. `OPUS-E-05` stays
  deferred.

### Latency lane

Landed:
- `OfflineAnswerEngine.generate()` now records structured retrieval / rerank /
  prompt-build / first-token / decode / total timings
- `SenkuLatency` structured events now emit on every answer run
- `AnswerRun.latencyBreakdown` persists through `SessionMemory`
- `PackRepository` now emits search-stage timing + slow-query tripwires
- `summarize_latency.py` now provides p50 / p95 / max summaries from exported
  records

Status:
- latency instrumentation landed; L-lane closed

### Retrieval lane

Confirmed:
- anchor-prior decision is landed and productized on desktop + Android
- bridge-guide demotion now skips planning/readiness acute queries instead of
  applying a blanket penalty
- sticky anchor idle reset is landed in `SessionMemory` / `ChatSessionStore`

Still live:
- `BACK-R-03` still needs emulator validation across the four-posture matrix —
  post-P-04 re-run pending on the restored stock install path.

### Pack lane

Confirmed:
- runtime FTS selection now probes FTS5, then FTS4, then explicit LIKE
  fallback in-process
- `PackInstaller.validateInstalledFiles` now validates required SQLite tables
  after size + SHA checks
- bridge-tag consistency audit is landed at ingest time

Still live:
- `BACK-P-01` and `BACK-P-02` still need emulator validation with stock,
  degraded, and corrupted packs on the post-P-04 bundle (`BACK-P-04` landed
  `bbc1b1d`, stock bundle re-exported `aa1b399`). Target:
  `artifacts/bench/wave_a_closeout_20260418_rerun/`.

## Rule

Do not overfit product behavior to a contaminated wave. If a BACK validation
pack hits LiteRT `500` failures, mark the wave as host-contaminated in the
state log and re-run before drawing conclusions. This is the same discipline
`APP_ROUTING_HARDENING_TRACKER_20260417.md` §5 applies to the guide lane.

## Dispatch Strategy — Post-`OPUS-E-06` State

`OPUS-E-06` landed 2026-04-18 (`b41128a`): `AnswerPresenter` and
`DetailAnswerPresenterHost` own the three former
`OfflineAnswerEngine.generate()` callsites; `DetailActivity.java` is 6264
→ 6063 lines. `OPUS-E-05` stays deferred. Lane U plumbing now targets the
single Presenter callsite + a small `Host.onSuccess(...)` block rather
than three near-duplicate `executor.execute(...)` blocks in the Activity.

**Wave A — non-UI-crossing backend (26 original tasks):**
- 23 done + 3 emulator re-run pending (`BACK-R-03`, `BACK-P-01`,
  `BACK-P-02`) + 1 invalidated (`BACK-H-05`)
- `BACK-P-04` landed `bbc1b1d`; stock bundle re-exported `aa1b399`; closeout
  re-run is the only remaining path to formally close Wave A.

**Wave B — Lane U (3 tasks, dispatchable now):**
- `BACK-U-01`, `BACK-U-02`, `BACK-U-03` — edit the `AnswerPresenter` callsite
  plus a small `Host.onSuccess(...)` block in the Activity. `BACK-U-04` is
  script + note only and was never gated (landed).
- Scout-tier Wave B spec addenda (MetaStrip confidence-token, PaperAnswerCard
  uncertain-fit, escalation copy draft) should be in hand for the dispatch
  prompt.

## Immediate Next Move

1. ~~Land `BACK-P-04`.~~ **Done** 2026-04-18 (`bbc1b1d`); stock bundle
   re-exported (`aa1b399`). Pipeline regression test is in `tests/`.
2. Re-run Wave A closeout emulator validation on the post-P-04 stock
   bundle: `BACK-P-02` → `BACK-P-01` → `BACK-R-03` (in that order; P-02
   is the fastest signal that the pipeline fix is effective). Target
   artifact directory: `artifacts/bench/wave_a_closeout_20260418_rerun/`.
3. If validation is green, flip those three rows from `pending` to `done`
   in `reviewer_backend_tasks.md` State Log and close Wave A formally.
4. ~~Dispatch `OPUS-E-06`.~~ **Done** 2026-04-18 (`b41128a`); Lane U is
   un-gated.
5. Dispatch Wave B (`BACK-U-01` / `U-02` / `U-03`) against the
   `AnswerPresenter` callsite (`android-app/app/src/main/java/com/senku/mobile/AnswerPresenter.java`),
   not the three former `DetailActivity.java` callsites. Scout-tier spec
   addenda (MetaStrip confidence-token, PaperAnswerCard uncertain-fit,
   escalation copy draft) should be drafted during the Wave A re-run.

## State Log

2026-04-18: `OPUS-E-06` landed with `DetailActivity.java` reduced from the spec's 6264-line baseline to 6063 lines (delta `-201`) and all three `OfflineAnswerEngine.generate()` callsites moved behind `AnswerPresenter`; JVM verification passed via `./gradlew :app:testDebugUnitTest --tests com.senku.mobile.AnswerPresenterTest`, the instrumented smoke passed on the available emulator default lane via `scripts/run_android_instrumented_ui_smoke.ps1 -Device emulator-5554` (2/2 tests), and `scripts/build_android_ui_state_pack_parallel.ps1` completed green at `45 / 45` across `5556 / 5560 / 5554 / 5558`, which is the Wave B unblock signal.

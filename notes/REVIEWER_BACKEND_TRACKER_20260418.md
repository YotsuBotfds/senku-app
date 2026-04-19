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

> Wave B code-level closure complete 2026-04-19 (`eb398dc`); on-device CP9
> Stage 0 verification in progress. v2/v3/v4/v5 artifacts are INVALID
> (APK/model mismatch). Stage 0 GREEN is a prerequisite for RC v3 packet
> rebuild (Stage 1).

## Current State

The reviewer packet
([`REVIEWER_BACKEND_UNKNOWNS_20260418.md`](./REVIEWER_BACKEND_UNKNOWNS_20260418.md))
confirms the backend is structurally strong — hybrid retrieval plus metadata-
aware reranking, session-aware rather than purely single-turn, real offline
path, explicit abstain, narrow deterministic surface. The remaining work is the
more mature class of problem:

Wave A closed 2026-04-18: 25 done + 2 invalidated (`BACK-H-05`,
`BACK-R-03`) out of 26 original tasks, plus `BACK-P-04` (newly added P0
on 2026-04-18, landed `bbc1b1d`) — the pipeline-SHA-race fix that was
needed before the closeout re-run could run. Stock bundle re-exported
(`aa1b399`); `BACK-P-02` + `BACK-P-01` re-ran green on 2026-04-18
(artifacts at `artifacts/bench/wave_a_closeout_20260418_rerun/`).
`BACK-R-03` was invalidated because anchor-prior is feature-gated off on
Android (`SessionMemory.java:22` `ENABLE_ANCHOR_PRIOR = false`) so the
sticky-anchor idle-reset path is non-observable in production — see
`notes/WAVE_A_CLOSEOUT_FAIL_2026-04-18_rerun.md`. Two follow-ups filed
(post-release, not Wave B dependencies): `BACK-R-05` (Android anchor-prior
productization decision) and `BACK-T-04` (fix `Quote-AndroidShellArg` in
the session-flow harness). Wave B (`BACK-U-01` / `U-02` / `U-03`) is
complete: `BACK-U-03` landed 2026-04-18 (`af49d91`), `BACK-U-02` landed
2026-04-18 (`d974ebc`), and `BACK-U-01` landed 2026-04-19 (`eb398dc`).
Wave B is closed and ready for CHECKPOINT 9 release-candidate gate planning.

- **Deterministic false-positive control.** Backend-side closure is landed:
  graduation manifest, promotion metadata, semantic exclusion-list gate,
  builder-missing telemetry, equal-priority tie-break, near-miss tests, and
  mobile parity guard are all in.
- **Uncertainty communication.** Backend-side coverage now includes the abstain
  tuning note + regression runner (`BACK-U-04`,
  `scripts/run_abstain_regression_panel.ps1`, baseline under
  `artifacts/bench/abstain_baseline_20260418/`), the Wave B confidence-label
  contract tests, the landed confidence-label plumbing (`BACK-U-03`
  `af49d91`), the safety-critical abstain escalation (`BACK-U-02`
  `d974ebc`), and the uncertain-fit response mode (`BACK-U-01` `eb398dc`).
  Wave B is complete and ready for CHECKPOINT 9 release-candidate gate
  planning.
- **On-device latency.** Backend instrumentation is landed: structured
  retrieval / rerank / prompt-build / first-token / decode / total timing,
  `SenkuLatency` events, `SessionMemory` persistence, `PackRepository` stage
  timing, and the desktop summary tool.

Adjacent findings from the full backend audit (not in the reviewer packet, but
worth tracking alongside):

- anchor-prior decision is landed: productized on desktop (`query.py:60`
  `ENABLE_ANCHOR_PRIOR = True`); Android code is landed but feature-gated
  off (`SessionMemory.java:22` `ENABLE_ANCHOR_PRIOR = false`, only setter is
  the test helper at line 1190). Follow-up `BACK-R-05` to decide Android
  productization post-release.
- bridge-guide demotion is now context-aware for planning/readiness acute
  queries
- FTS runtime selection and install-time schema validation are landed in code;
  `BACK-P-04` (pack export SHA race) landed (`bbc1b1d`); `BACK-P-01` /
  `BACK-P-02` re-ran green on the post-P-04 stock bundle — see
  `artifacts/bench/wave_a_closeout_20260418_rerun/`

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
- confidence-label plumbing (`BACK-U-03`) landed 2026-04-18 (`af49d91`), so
  the low-applicability branch (`BACK-U-01`) now has its signal; `BACK-U-02`
  was never dependency-gated

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
- `BACK-U-03` confidence-label dependency is now satisfied (`af49d91`)
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
- regression runner (`scripts/run_abstain_regression_panel.ps1`) + baseline
  artifact (`artifacts/bench/abstain_baseline_20260418/`) are checked in via
  `BACK-U-04`
- confidence-label contract tests are checked in so Wave B has a locked
  backend-side spec before UI plumbing starts
- `BACK-U-03` landed 2026-04-18 (`af49d91`): desktop + Android now surface
  high / medium / low confidence labels end-to-end

Wave B status:
- `BACK-U-03` landed 2026-04-18 (`af49d91`), `BACK-U-02` landed 2026-04-18
  (`d974ebc`), and `BACK-U-01` landed 2026-04-19 (`eb398dc`). Wave B is
  complete; `OPUS-E-05` stays deferred and CHECKPOINT 9 gate planning can
  proceed independently.

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
- anchor-prior decision is landed: productized on desktop (`query.py:60`
  `ENABLE_ANCHOR_PRIOR = True`); Android code is landed but feature-gated
  off (`SessionMemory.java:22` `ENABLE_ANCHOR_PRIOR = false`, only setter is
  the test helper at line 1190). Follow-up `BACK-R-05` to decide Android
  productization post-release.
- bridge-guide demotion now skips planning/readiness acute queries instead of
  applying a blanket penalty
- sticky anchor idle reset is landed in `SessionMemory` / `ChatSessionStore`
  but is unreachable on Android until anchor-prior is productized there

Resolved this closeout:
- `BACK-R-03` invalidated 2026-04-18 — Android anchor-prior is feature-gated
  off so the idle-reset scenario is non-observable in production; see
  `notes/WAVE_A_CLOSEOUT_FAIL_2026-04-18_rerun.md`.

Follow-ups (post-release):
- `BACK-R-05` — decide whether to productize Android anchor-prior or remove
  the half-commit code (scout spike + decision doc)
- `BACK-T-04` — fix `Quote-AndroidShellArg` in
  `scripts/run_android_session_flow.ps1` so the session-flow harness can
  drive multi-turn / idle-reset scenarios again
- `BACK-T-05` — tighten
  `PromptHarnessSmokeTest.assertDetailSettled` body-content checks for
  `uncertain_fit` / `abstain`.
  **Problem.** `PromptHarnessSmokeTest.assertDetailSettled` reports
  `status: pass` on `emulator-5560` landscape when the captured
  `scriptedPromptFlowCompletes__prompt_detail.xml` is IME-dominated and does
  not contain the detail body, `UNSURE FIT` chip, or escalation sentence; the
  assertion currently passes on structural-hierarchy checks that do not verify
  Wave B textual content.
  **Current workaround.** A1b landed the harness-side BACK-dismiss fix
  (`9cf405c`), which works around the dump issue, but the underlying
  assertion is still weak.
  **Evidence.**
  `artifacts/cp9_stage0_20260419_142539/smoke_emulator-5560_v6/instrumented_summary_landscape.json`
  shows `status: pass` and `artifact_expectations_met: true`, while the linked
  capture at
  `artifacts/instrumented_ui_smoke/20260419_170638_429/emulator-5560/dumps/scriptedPromptFlowCompletes__prompt_detail.xml`
  is a 108768-byte IME-dominated dump.
  **Post-RC work.** Tighten `assertDetailSettled` to require the escalation
  sentence, or an equivalent body-content signal, when the expected mode is
  `uncertain_fit` or `abstain`; tag this as test-quality /
  safety-critical-escalation-visibility.
  **Sizing.** medium (test + plumbing).
  **Acceptance.** Re-running the pre-A1b v6 `emulator-5560` landscape smoke
  against an IME-dominated dump reports `status: fail` with a body-content
  assertion message; portrait and tablet postures continue to pass.

### Pack lane

Confirmed:
- runtime FTS selection now probes FTS5, then FTS4, then explicit LIKE
  fallback in-process
- `PackInstaller.validateInstalledFiles` now validates required SQLite tables
  after size + SHA checks
- bridge-tag consistency audit is landed at ingest time

Landed this closeout:
- `BACK-P-04` landed `bbc1b1d`; stock bundle re-exported `aa1b399`;
  manifest-parity pipeline regression test in `tests/` locks the invariant.
- `BACK-P-01` + `BACK-P-02` re-ran green on `emulator-5554` +
  `emulator-5556`; FTS runtime probe + schema validation + missing-table +
  truncated variants all reach the expected code paths. Artifacts at
  `artifacts/bench/wave_a_closeout_20260418_rerun/`.
- FTS status addendum: `notes/ANDROID_SQLITE_FTS5_STATUS_2026-04-18.md`.
- `BACK-U-03` landed `af49d91`; desktop + Android now compute and surface
  high / medium / low confidence labels, and MetaStrip token rendering has
  phone + tablet instrumentation proof.
- `BACK-U-02` landed `d974ebc`; desktop + Android abstain responses now
  prepend the pinned safety-critical escalation line above "Closest matches"
  when the upstream safety flag is set, with phone + tablet validation
  artifacts at `artifacts/bench/wave_b_back-u-02_2026-04-18/`.
- `BACK-U-01` landed `eb398dc`; desktop + Android now resolve
  `confident` / `uncertain_fit` / `abstain`, render the `UNSURE FIT` card
  variant, and pass the reviewer-worked example plus boundary coverage with
  phone + tablet artifacts at `artifacts/bench/wave_b_back-u-01_2026-04-19/`.

Backlog stubs:
- `BACK-P-05` - SQLite FTS runtime decision. Hypothesis: the current FTS5 /
  FTS4 / LIKE fallback path is adequate for Wave B scale; reconfirm at RC v3
  telemetry. Blocks nothing in CP9.
- `BACK-P-06` - AVD data-partition sizing policy.
  **Problem.** Tablet AVDs with roughly 6 GB data partitions cannot tmp-stage
  an E4B-size model via the standard push helper; CP9 Stage 0 v5 exposed this
  when the push fell into a silent failure mode.
  **Current workaround.** `push_litert_model_to_android.ps1` has a
  direct-stream `adb shell run-as` path that bypasses tmp staging and writes
  straight into `files/models/<name>`, which works on partition-constrained
  AVDs.
  **Post-RC work.** Promote direct-stream to the default path in
  `push_litert_model_to_android.ps1`; keep tmp staging behind an opt-in
  `--use-tmp-staging` flag.
  **Sizing.** small (one script + docs).
  **Acceptance.** Push helper succeeds on a 6 GB tablet AVD without any
  posture-specific flag.
- `BACK-P-07` - unified LiteRT model push helper.
  **Problem.** Today the push path is split between
  `push_litert_model_to_android.ps1` (tmp staging) and the manual
  `adb shell run-as cat` recipe (direct-stream). Dispatches have to know which
  to use, and the wrong choice fails silently on constrained AVDs.
  **Current workaround.** Operators manually choose between tmp staging and
  the direct-stream recipe based on device posture and recent failure history.
  **Post-RC work.** Consolidate behind a free-space probe - if `stat -f` (or
  equivalent `run-as` `df`) on `/data/user/0/com.senku.mobile/` shows less
  than `2x model_size` free, auto-select direct-stream; otherwise use tmp
  staging. The helper must fail loud, not silent, when neither path can stage
  the model.
  **Sizing.** small (one script).
  **Acceptance.** One entrypoint works on both phone and tablet AVDs without
  posture-specific flags, and fails loud when neither path can stage the
  model.

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
- formally closed 2026-04-18: 25 done + 2 invalidated (`BACK-H-05`,
  `BACK-R-03`) out of 26 original tasks
- `BACK-P-04` landed `bbc1b1d`; stock bundle re-exported `aa1b399`;
  `BACK-P-02` + `BACK-P-01` re-ran green; `BACK-R-03` was invalidated
  because Android anchor-prior is feature-gated off in production.

**Wave B — Lane U (complete):**
- `BACK-U-03` landed 2026-04-18 (`af49d91`): desktop + Android now compute
  and surface high / medium / low confidence labels end-to-end, and
  MetaStrip token rendering has phone/tablet instrumentation proof.
- `BACK-U-02` landed 2026-04-18 (`d974ebc`): desktop + Android abstain
  responses now prepend the pinned safety-critical escalation line above
  "Closest matches" when the upstream safety flag is set, with tests and
  phone/tablet validation artifacts.
- `BACK-U-01` landed 2026-04-19 (`eb398dc`): desktop + Android now route
  low-applicability cases through a three-way `confident` / `uncertain_fit` /
  `abstain` decision, reuse the U-02 escalation helper for safety-critical
  uncertain-fit, and render the `UNSURE FIT` card variant with phone/tablet
  artifacts at `artifacts/bench/wave_b_back-u-01_2026-04-19/`.
- Wave B is complete. All three U-tasks landed; ready for CHECKPOINT 9
  release-candidate gate planning.

## Immediate Next Move

1. ~~Land `BACK-P-04`.~~ **Done** 2026-04-18 (`bbc1b1d`); stock bundle
   re-exported (`aa1b399`); pipeline regression test checked in.
2. ~~Re-run Wave A closeout.~~ **Done** 2026-04-18. `BACK-P-02` + `BACK-P-01`
   landed green; `BACK-R-03` invalidated (Android anchor-prior feature-gated
   off). Artifacts at `artifacts/bench/wave_a_closeout_20260418_rerun/`.
3. ~~Flip closeout rows.~~ **Done** 2026-04-18: P-02 / P-01 flipped to `done`,
   R-03 flipped to `invalidated`. Wave A formally closed.
4. ~~Dispatch `OPUS-E-06`.~~ **Done** 2026-04-18 (`b41128a`); Lane U un-gated.
5. ~~Dispatch the remaining Wave B uncertainty work (`BACK-U-01`).~~
   **Done** 2026-04-19 (`eb398dc`). `BACK-U-02` landed 2026-04-18
   (`d974ebc`) and `BACK-U-03` landed 2026-04-18 (`af49d91`); Wave B is
   complete and ready for CHECKPOINT 9 release-candidate gate planning.
6. File follow-ups post-release: `BACK-R-05` (Android anchor-prior
   productization decision, scout spike) and `BACK-T-04` (fix
   `Quote-AndroidShellArg` in session-flow harness).

## State Log

2026-04-18: `OPUS-E-06` landed with `DetailActivity.java` reduced from the spec's 6264-line baseline to 6063 lines (delta `-201`) and all three `OfflineAnswerEngine.generate()` callsites moved behind `AnswerPresenter`; JVM verification passed via `./gradlew :app:testDebugUnitTest --tests com.senku.mobile.AnswerPresenterTest`, the instrumented smoke passed on the available emulator default lane via `scripts/run_android_instrumented_ui_smoke.ps1 -Device emulator-5554` (2/2 tests), and `scripts/build_android_ui_state_pack_parallel.ps1` completed green at `45 / 45` across `5556 / 5560 / 5554 / 5558`, which is the Wave B unblock signal.
2026-04-18: `BACK-U-02` landed (`d974ebc`): desktop + Android abstain responses now prepend the pinned safety-critical escalation line above "Closest matches" when the upstream safety flag is set, with table-driven test coverage and phone/tablet validation artifacts at `artifacts/bench/wave_b_back-u-02_2026-04-18/`.
2026-04-19: `BACK-U-01` landed (`eb398dc`): desktop + Android now resolve `confident` / `uncertain_fit` / `abstain`, render the `UNSURE FIT` card variant, and pass the reviewer example plus boundary coverage with phone/tablet artifacts at `artifacts/bench/wave_b_back-u-01_2026-04-19/`.

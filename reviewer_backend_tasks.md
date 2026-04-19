# reviewer_backend_tasks.md

Living backlog folded from the 2026-04-18 reviewer backend-unknowns packet and a
full desktop-plus-Android backend audit. Parallels `opustasks.md` in shape and
discipline, but is scoped to backend safety/correctness/latency work — not UI.

Not a replacement for `opustasks.md` — this file **indexes** the backend lanes
and sequences them with explicit checkpoints. Use it as the backend entry point;
use per-lane trackers for execution detail.

- Reviewer packet: [`notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`](./notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md)
- Tracker companion: [`notes/REVIEWER_BACKEND_TRACKER_20260418.md`](./notes/REVIEWER_BACKEND_TRACKER_20260418.md)
- Routing tracker (adjacent): [`notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`](./notes/APP_ROUTING_HARDENING_TRACKER_20260417.md)
- Master UI+routing backlog: [`opustasks.md`](./opustasks.md)
- FTS5 status (in-flight context): [`notes/ANDROID_SQLITE_FTS5_STATUS_2026-04-18.md`](./notes/ANDROID_SQLITE_FTS5_STATUS_2026-04-18.md)

## How To Use This File

- Task IDs are stable: `BACK-<lane><seq>`. When a task ships, mark it
  `[done 2026-MM-DD <hash>]` rather than deleting; keeps history legible.
- Priorities: `P0` safety / correctness, `P1` stability / UX, `P2` code hygiene.
- Estimates: `XS` < 30 min, `S` < 2 hrs, `M` < 1 day, `L` > 1 day.
- Lanes: `D` deterministic, `U` uncertainty/abstain, `L` latency/instrumentation,
  `R` retrieval/ranking, `P` pack/repo, `T` test/validation, `H` hygiene.
- Routing ladder follows `opustasks.md`: orchestrator = GPT-5.4 × high fast
  mode, worker = GPT-5.4 high, scout = Spark high → GPT-5.4 mini medium fallback.
- Complex tasks link to a spec in [`notes/specs/`](./notes/specs/).
- **Do not re-litigate locked OPUS decisions.** If a BACK task touches
  `query.py`, `OfflineAnswerEngine.java`, `PackRepository.java`,
  `DeterministicAnswerRouter.java`, or the deterministic registry, check the
  state log in `opustasks.md` first — A-01..A-10, B-03, C-08, E-03 all landed
  work in these areas and should be treated as prior art, not blockers.

## Status Snapshot (2026-04-18, Sprint 2 closeout)

**Landed (already covered by OPUS-\*; do not re-do):**
- Sub-query dedup (`OPUS-A-09`) → supersedes an earlier audit finding on
  `decompose_query`
- Bridge-guide ingest tag + uniform demotion (`OPUS-A-10`) → supersedes the
  ingest-tag half of the bridge-guide work
- `VectorStore` unmap on `PackRepository.close()` (`OPUS-E-03`) → supersedes the
  mmap cleanup finding
- FTS5 diagnostic capture (`OPUS-C-08`) → adjacent to `BACK-P-01`; C-08 produced
  the diagnostic note, BACK-P-01 is the runtime fix that follows
- Contextual follow-up chip candidates (`OPUS-B-03`) → adjacent to anchor-prior
  work in `BACK-R-01` / `BACK-R-03`

**New (this backlog):** `BACK-D-01`..`D-06` · `BACK-U-01`..`U-04` ·
`BACK-L-01`..`L-04` · `BACK-R-01`..`R-05` · `BACK-P-01`..`P-03` ·
`BACK-T-01`..`T-04` · `BACK-H-01`..`H-06` (30 original tasks; `BACK-H-05`
later invalidated after re-audit; post-release follow-ups `BACK-R-05` and
`BACK-T-04` added 2026-04-18).

**Wave A status:** formally closed 2026-04-18. 25 done + 2 invalidated
(`BACK-H-05`, `BACK-R-03`) out of 26 original tasks, plus **1 newly-added P0**
(`BACK-P-04`, landed `bbc1b1d`) surfaced during closeout. `BACK-P-04` fixed
the pack export SHA race; stock bundle re-exported (`aa1b399`); closeout
re-run on 2026-04-18 landed `BACK-P-02` + `BACK-P-01` green with artifacts
under `artifacts/bench/wave_a_closeout_20260418_rerun/`. `BACK-R-03` was
invalidated because anchor-prior is feature-gated off on Android
(`SessionMemory.java:22`) so the idle-reset scenario is non-observable in
production — see `notes/WAVE_A_CLOSEOUT_FAIL_2026-04-18_rerun.md`. Follow-ups
`BACK-R-05` (Android anchor-prior decision) and `BACK-T-04` (harness repair)
track post-release.

**Wave B status:** `BACK-U-03` landed 2026-04-18 (`af49d91`) after the
2026-04-18 `OPUS-E-06` unblock (`b41128a`): `AnswerPresenter` and
`DetailAnswerPresenterHost` exist in
`android-app/app/src/main/java/com/senku/mobile/`, and all three former
`DetailActivity.java` callsites (lines 2939, 3064, 3186 in the pre-E-06
6264-line baseline) now route through the Presenter — Activity is down to
6063 lines. Remaining Wave B items are `BACK-U-01` and `BACK-U-02`;
`BACK-U-01` is now unblocked because the confidence-label dependency is
satisfied, and `BACK-U-02` was never dependency-gated. `OPUS-E-05` stays
deferred; CP9 release-candidate gate is independent of E-06.

**Reviewer-priority status:**
- **Deterministic false-positive risk** — backend controls landed: graduation
  manifest, promotion metadata, exclusion-list semantic gate, near-miss panel,
  builder-missing telemetry, and mobile-parity guard. Lane `D` is closed.
- **Low-applicability abstain / uncertainty** — backend tuning note and
  regression runner (`BACK-U-04`) are landed, and `BACK-U-03` landed
  2026-04-18 (`af49d91`). The remaining UI-crossing uncertainty work is
  `BACK-U-01` / `U-02`; `BACK-U-01` is unblocked and `BACK-U-02` was never
  dependency-gated.
- **On-device latency honesty** — per-stage timing, structured `SenkuLatency`
  events, `SessionMemory` persistence, `PackRepository` stage timing, and the
  desktop summary tool are landed. Lane `L` is closed.

## Parallelism Notes

Backend work fans out more cleanly than the Rev 03 UI migration — most BACK
tasks touch different files. The collision hotspots to watch:

- `query.py` — `BACK-R-01` (anchor prior) + `BACK-R-02` (bridge demotion) +
  `BACK-L-04` (reranker timing) + `BACK-U-03` (confidence label). Bundle if
  two or more land in the same wave.
- `OfflineAnswerEngine.java` — `BACK-U-01` (low-applicability branch) +
  `BACK-U-02` (escalation signal) + `BACK-L-01` (TTFT) + `BACK-L-02`
  (LatencyPanel). Bundle if two or more land in the same wave.
- `deterministic_special_case_registry.py` — `BACK-D-01` (graduation fields) +
  `BACK-D-05` (priority tie-break) + `BACK-H-02` (introspection generator).
  Must-bundle.
- `DeterministicAnswerRouter.java` — `BACK-D-02` (semantic verifier) +
  `BACK-D-04` (near-miss tests). Must-bundle or strictly-serial.
- `PackRepository.java` — `BACK-P-01` (FTS runtime test) + `BACK-P-02` (schema
  validate). Bundle.
- `mobile_pack.py` — `BACK-P-04` (export SHA race) **landed** 2026-04-18
  (`bbc1b1d`); stock bundle re-exported (`aa1b399`). Wave A closeout re-run
  completed on the post-P-04 bundle.

Strict-serial pairs:
- `BACK-D-01` → `BACK-D-02` (graduation manifest must exist before semantic
  verifier can consult its `promotion_status` field)
- `BACK-L-02` → `BACK-L-03` (LatencyPanel schema must exist before model-vs-
  harness breakdown has a place to publish)
- `BACK-R-01` decision → `BACK-R-03`, `BACK-R-04` (if anchor prior is deleted,
  R-03 and R-04 dissolve; if productized, they become P1)
- **`BACK-P-04` → `BACK-R-03`, `BACK-P-01`, `BACK-P-02`** — **gate cleared**
  2026-04-18 and consumed the same day. `BACK-P-04` landed (`bbc1b1d`);
  stock bundle re-exported (`aa1b399`); Wave A closeout re-run completed on
  the post-P-04 bundle.
- **`OPUS-E-06` → entire Lane U** — **gate cleared** 2026-04-18. `OPUS-E-06`
  landed (`b41128a`); `AnswerPresenter` + `DetailAnswerPresenterHost` now own
  the three former `OfflineAnswerEngine.generate()` callsites. Lane U
  (`BACK-U-01` / `U-02` / `U-03`) is dispatchable. (`OPUS-E-05` stays
  deferred; `BACK-U-04` was never gated and is landed.)

## Phases + Checkpoints

### Phase B-1 — Deterministic safety (reviewer priority #1)

Goal: close the deterministic false-positive risk before any rule promotion
wave. Reviewer explicitly calls this the highest-priority unresolved backend
risk.

`BACK-D-01` · `BACK-D-02` · `BACK-D-03` · `BACK-D-04` · `BACK-D-05` ·
`BACK-D-06`

> **CHECKPOINT B-CP1 — Deterministic risk under structured control**
> - Graduation manifest landed at `notes/specs/deterministic_rule_graduation.md`
> - Registry carries `promotion_status` and `promotion_notes` fields
> - Semantic verifier gates at minimum the top-3 safety-critical rules
> - Near-miss false-positive panel exists with ≥ 20 cases across the 9 live rules
> - Mobile pack build fails fast when a pack rule has no Android predicate

### Phase B-2 — Uncertainty vocabulary (reviewer priority #2)

Goal: give the app three distinct answer shapes — confident answer,
uncertain-fit match, no-match abstain — instead of the current two.

> **Gate cleared 2026-04-18.** `OPUS-E-06` landed (`b41128a`):
> `AnswerPresenter` + `DetailAnswerPresenterHost` own the three former
> `OfflineAnswerEngine.generate()` callsites; `DetailActivity.java` is
> 6264 → 6063 lines. `BACK-U-03` landed 2026-04-18 (`af49d91`), so the
> remaining Phase B-2 work is `BACK-U-01` / `U-02`. `BACK-U-04` is landed;
> `BACK-U-01` is now unblocked, `BACK-U-02` was never dependency-gated, and
> Wave B scout-tier spec addenda (MetaStrip confidence token, PaperAnswerCard
> uncertain-fit, escalation copy) should be in hand for the dispatch prompt.

`BACK-U-01` · `BACK-U-02` · `BACK-U-03` · `BACK-U-04`

> **CHECKPOINT B-CP2 — Three answer shapes cleanly distinguished**
> - `AnswerRun` carries a confidence label (`high` / `medium` / `low` /
>   `uncertain-fit` / `abstain`); UI surfaces it
> - Safety-critical abstain includes an explicit escalation line
> - Abstain thresholds documented in
>   `notes/ABSTAIN_TUNING_ANALYSIS_<date>.md` with sensitivity data
> - Regression panel runs green on curated 30–50 query panel

### Phase B-3 — Latency honesty (reviewer priority #3)

Goal: end the "we have end-to-end smoke timing but not a clean shareable
breakdown" state. Baseline latency for the five reviewer classes.

`BACK-L-01` · `BACK-L-02` · `BACK-L-03` · `BACK-L-04`

> **CHECKPOINT B-CP3 — Latency panel published**
> - `SenkuLatency` structured logcat events on every answer run
> - `LatencyPanel` aggregates retrieval / prompt-build / first-token / total for
>   the five reviewer classes (practical how-to · medical cross-guide ·
>   deterministic · abstain · acute generative)
> - Baseline numbers checked into `artifacts/bench/latency_baseline_<date>/`
> - Reranker timing (both desktop `_metadata_rerank_delta` and Android
>   equivalent) emits per-chunk timing in debug builds

### Phase B-4 — Retrieval refinement + pack hardening

Goal: clean up the retrieval edges the deeper audit surfaced and harden the
pack install path so future downloadable-pack work doesn't bite.

`BACK-R-01` · `BACK-R-02` · `BACK-R-03` · `BACK-R-04` · `BACK-P-01` ·
`BACK-P-02` · `BACK-P-03` · `BACK-P-04`

> **CHECKPOINT B-CP4 — Retrieval + pack surface hardened**
> - Anchor-prior decision recorded (remove, keep-behind-flag, or productize)
> - Bridge demotion is context-aware (planning queries don't lose their primary
>   source)
> - Sticky anchor lifecycle has an explicit reset policy and test
> - FTS runtime test supplements `PRAGMA compile_options`; FTS4 actually runs
>   where the compile probe says it shouldn't
> - SQLite schema validated on pack install; truncated packs fail fast
> - Pack export pipeline no longer races its own manifest SHA; a regression
>   test locks the invariant (`BACK-P-04` landed `bbc1b1d`)

### Phase B-5 — Test coverage + hygiene (continuous)

Runs throughout B-1..B-4. Treat as the backend analogue of the OPUS D-lane —
parallel fill-lane with no gate of its own.

`BACK-T-01` · `BACK-T-02` · `BACK-T-03` · `BACK-H-01`..`BACK-H-04` · `BACK-H-06`

---

## Quick Start (If You Only Have ...)

- **30 minutes** → `BACK-D-05` (document tie-break rule). One-file, XS.
- **Half a day** → `BACK-L-01` (time-to-first-token instrumentation) alone —
  one-file surgical change in `OfflineAnswerEngine.generate()`. High-value
  reviewer-unknown closer.
- **A full day** → Wave B-1a: `BACK-D-01` + `BACK-D-05` + `BACK-D-06` parallel.
  Graduation fields + tie-break + mobile-parity check; closes the
  "graduation-gate is missing" half of reviewer priority #1.
- **A week** → Phases B-1 and B-2 fully landed. Closes reviewer priorities #1
  and #2.

---

## In-Flight (do not duplicate)

- **OPUS C-lane test-infra** — `opustasks.md` Phase 8 still covers parts of
  Android test harness work; coordinate with `BACK-T-01` / `BACK-T-02` so
  seeded state isn't built twice
- **OPUS D-lane guide fills** — orthogonal; ignore
- **APP_ROUTING_HARDENING_TRACKER** — query.py mental-health lane is still hot;
  `BACK-R-02` (bridge demotion refinement) should rebase on the most recent
  routing patch before dispatch

---

## Tasks By Lane

### Lane D — Deterministic false-positive control (reviewer priority #1)

- `BACK-D-01` · **P0 · S · worker** · Add rule promotion metadata to registry
  - Files: `deterministic_special_case_registry.py:6-14`
    (`DeterministicSpecialCaseSpec` dataclass), new
    `notes/specs/deterministic_rule_graduation.md`
  - Behavior: add `promotion_status` (`inactive` | `candidate` |
    `under-review` | `active`) and `promotion_notes: str | None`; surface in
    `get_deterministic_special_case_rules()` so auditors can see why the live
    Android count (9) is so far below the pack count (116)
  - Accept: schema test asserts every spec has a non-null `promotion_status`;
    graduation manifest documents the gate (validated misses on same query
    shape, lexical specificity, low family ambiguity, FP review against near-
    miss prompts, user-harm analysis for any emergency rule)
  - **Must-bundle with BACK-D-05** (same file, same region)

- `BACK-D-02` · **P0 · M · worker** · Semantic adjudicator after lexical match
  - Files: `query.py:5419-5427` (`_match_deterministic_special_case`), new
    helper in `special_case_builders.py`;
    `android-app/app/src/main/java/com/senku/mobile/DeterministicAnswerRouter.java:23-50`
    for Android parity
  - Behavior: after a predicate fires, run a lightweight semantic check before
    emitting the deterministic answer. Options (pick one, document in the
    graduation manifest):
    (a) exclusion-list gate — explicit blockers ("eye", "brain", "child" for
    adult-oriented splinter rules, etc.)
    (b) cosine-similarity gate — query embedding vs. the rule's canonical topic
    embedding
    (c) micro-LLM verifier — single-call "does this query truly match rule
    intent" check
  - Start with (a) on the 9 live Android rules, upgrade to (b) as the top-3
    safety-critical rules are brought online
  - Accept: near-miss panel (see `BACK-D-04`) shows zero false-positives for
    the gated rules
  - **Depends on BACK-D-01** (graduation manifest specifies which gate level
    applies per rule)

- `BACK-D-03` · **P1 · XS · worker** · Surface builder-missing warnings
  - Files: `query.py:5496-5507` (`build_special_case_response`)
  - Behavior: when a rule fires but its builder is `None` or missing, emit a
    structured `deterministic.builder_missing` telemetry event and — in debug
    builds only — append a note to the answer ("rule matched, builder
    unavailable, falling back to retrieval"). Prod builds stay silent to the
    user but still log.
  - Accept: unit test covers the missing-builder path; log event shows up in
    bench runs

- `BACK-D-04` · **P0 · S · worker (test-infra)** · Near-miss false-positive panel
  - Files:
    `android-app/app/src/test/java/com/senku/mobile/DeterministicAnswerRouterTest.java`
    (expand); new
    `tests/test_deterministic_near_miss.py` for the desktop registry
  - Behavior: parameterized test with ≥ 20 near-miss queries per live rule
    (e.g., "puncture wound in my eye" must not match `generic_puncture`;
    "candles near my water filter" must not trigger `candles_for_light` +
    `charcoal_sand_water_filter`). Run on every push.
  - Accept: panel runs green; each rule has at least 2 documented near-miss
    cases
  - **Must-bundle with BACK-D-02** (semantic verifier tuning is tested here)

- `BACK-D-05` · **P2 · XS · worker** · Priority tie-break rule
  - Files: `deterministic_special_case_registry.py` (schema comment),
    `query.py:5421-5427`
  - Behavior: document and enforce the tie-break among equal-priority matches.
    Recommended: among matches at the same priority, keep the rule whose
    predicate has the most specific lexical signature (longest required-term
    set); on further ties, first-defined wins with a warn event.
  - Accept: tie-break is explicit in code and docstring; test demonstrates
    deterministic outcome on a forced collision
  - **Must-bundle with BACK-D-01**

- `BACK-D-06` · **P1 · S · worker (test-infra)** · Mobile pack deterministic parity
  - Files: `mobile_pack.py:21+` (export path), new
    `scripts/validate_mobile_pack_deterministic_parity.ps1`,
    manifest file `android-app/deterministic_predicate_manifest.txt`
  - Behavior: pack build fails fast if a rule is exported in pack metadata but
    no corresponding Android predicate exists in the checked-in manifest.
    Catches the "desktop has the rule, Android silently doesn't" gap that
    reviewer doc flags implicitly.
  - Accept: CI gate runs the script; removing a predicate from Android without
    updating the manifest breaks the build

### Lane U — Uncertainty / abstain (reviewer priority #2)

- `BACK-U-01` · **[done 2026-04-19 `eb398dc`]** · **P0 · L · worker** · Low-applicability response mode
  - Files: `query.py:9879+` (`_should_abstain` + sibling helpers);
    `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java:184-195`
    (`shouldAbstain`) and `generate()` branching
  - Behavior: introduce a third mode between "confident answer" and "no-match
    abstain" — **uncertain-fit**. Trigger when top-K RRF scores are above the
    abstain floor but below a confidence ceiling (rough starting thresholds:
    avg RRF < 0.65 OR top-candidate vector similarity between 0.45 and 0.62 OR
    scenario frame flagged as safety-critical and retrieval lacks primary-
    owner match). Output biases toward clarification and escalation rather
    than decisive instructions; surfaces related guides as "possibly relevant"
    instead of "answer."
  - Accept: reviewer-worked example ("He has barely slept, keeps pacing, says
    normal rules do not apply to him. Is this just stress, or should I help
    him calm down?") routes to uncertain-fit rather than retrieval-with-
    bridging or full abstain; emulator screenshot shows the distinct card
  - Spec: new `notes/specs/uncertain_fit_mode_spec.md`
  - **Depends on BACK-U-03** (confidence label must exist before the branching
    logic has a signal to key off)
  - **Landed 2026-04-19 (`eb398dc`)** — `notes/specs/uncertain_fit_mode_spec.md` and `notes/specs/paper_answer_card_uncertain_fit_addendum.md` pin the three-way mode contract and `UNSURE FIT` card variant; reviewer example (`He has barely slept, keeps pacing...`) routes to uncertain-fit; phone + tablet artifacts at `artifacts/bench/wave_b_back-u-01_2026-04-19/`

- `BACK-U-02` · **[done 2026-04-18 `d974ebc`]** · **P0 · S · worker** · Safety-critical escalation in abstain
  - Files:
    `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java:777-825`
    (`buildAbstainAnswerBody`); `query.py` abstain formatter
  - Behavior: when the query is flagged safety-critical (acute medical,
    poisoning, self-harm, emergency keywords) and the system is abstaining,
    append an explicit escalation line ("If this is urgent, contact emergency
    services / poison control / a trusted adult") above the "closest matches"
    block
  - Accept: unit test covers the gated escalation path; no escalation appears
    on non-safety-critical abstains
  - **Coordinate with `guides/mental-health/…` D-lane** — escalation copy
    should match the crisis-gestalt interpreter output
  - **Gate cleared** — `OPUS-E-06` landed 2026-04-18 (`b41128a`); dispatchable

- `BACK-U-03` · **[done 2026-04-18 `af49d91`]** · **P1 · M · worker** · Confidence label on every answer
  - Files: `query.py:~8488` (post-rerank pipeline), new helper
    `_confidence_label(reranked, scenario_frame)`; Android `AnswerRun` /
    `AnswerProgressListener` plumbing
  - Behavior: compute an explicit confidence label (`high` | `medium` | `low`)
    from top-K RRF scores, overlap tokens, vector similarity, metadata match
    deltas, and the new uncertain-fit / abstain flags. Attach to the prompt as
    a system instruction ("The answer confidence is {label}. If confidence is
    low, note the gap in the first sentence") and propagate to the UI so
    MetaStrip can render it.
  - Accept: every answer surface carries a confidence tag; label matches
    ground-truth on ≥ 80 % of a curated 50-query panel
  - Coordinates with OPUS F-02 (MetaStrip primitive — already landed)
  - **Landed 2026-04-18 (`af49d91`)** - desktop 50-query confidence panel + Android `ConfidenceTokenRenderTest` prove phone + tablet rendering; `notes/specs/meta_strip_confidence_token_addendum.md` captures the MetaStrip contract

- `BACK-U-04` · **P1 · M · worker (test-infra)** · Abstain tuning analysis + panel
  - Files: new `notes/ABSTAIN_TUNING_ANALYSIS_<date>.md`, new
    `scripts/run_abstain_regression_panel.ps1`,
    `artifacts/bench/abstain_baseline_<date>/`
  - Behavior: document why every `_ABSTAIN_*` constant (`_ABSTAIN_ROW_LIMIT`,
    `_ABSTAIN_MAX_OVERLAP_TOKENS`, `_ABSTAIN_MIN_VECTOR_SIMILARITY = 0.62`,
    `_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS`) sits where it does. Sensitivity
    sweep: ±0.05 on similarity, ±1 on hits, measure abstain rate + false-
    positive rate on 30–50 curated queries. Check in a baseline; re-run
    quarterly.
  - Accept: tuning note explains each threshold; regression panel exists and
    has a baseline
  - **Not gated on OPUS-E-05 / OPUS-E-06** — script + note only, no Activity
    render path. Safe to ship during Phase B-1 or B-3.

### Lane L — Latency honesty (reviewer priority #3)

- `BACK-L-01` · **P1 · S · worker** · Time-to-first-token + per-stage timing
  - Files: `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
    (`generate()` and `prepare()`)
  - Behavior: record structured retrieval / rerank / prompt-build /
    first-token / decode / total timings across deterministic, abstain,
    host, on-device, and host-fallback paths; log one parseable summary line
    per run.
  - Accept: unit test regex-parses the summary line; non-abstain runs carry a
    populated latency breakdown

- `BACK-L-02` · **P1 · M · worker** · `LatencyPanel` events + persisted answer metadata
  - Files: `OfflineAnswerEngine.java`, `SessionMemory.java`, new
    `android-app/app/src/main/java/com/senku/mobile/telemetry/LatencyPanel.java`
  - Behavior: emit one structured `SenkuLatency` JSON event per answer run and
    persist `AnswerRun.latencyBreakdown` through `SessionMemory` only, without
    plumbing it through `DetailActivity`.
  - Accept: structured events include retrieval / rerank / prompt-build /
    first-token / decode / total fields plus compatibility aliases
  - **Must-bundle with BACK-L-01** (both edit `generate()`)

- `BACK-L-03` · **P2 · M · worker** · `PackRepository` stage timing + slow-query tripwire
  - Files: `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
  - Behavior: time the FTS MATCH runtime probe, search-stage breakdown, and
    fallback detection; emit a slow-query debug warning when any stage exceeds
    its configured budget while treating LiteRT host 500s as infra noise, not
    product regression.
  - Accept: unit coverage locks the summary line, fallback flag, and tripwire
    branches
  - **Depends on BACK-L-02** (schema must land first)

- `BACK-L-04` · **P1 · S · worker** · Desktop latency summary tool
  - Files: new `summarize_latency.py`, new `tests/test_summarize_latency.py`
  - Behavior: read exported `AnswerRun` JSON / JSONL records and print a
    p50 / p95 / max table per stage.
  - Accept: tool handles nested `latencyBreakdown` payloads and legacy alias
    keys in unit tests

### Lane R — Retrieval refinement

- `BACK-R-01` · **P1 · S · scout → worker** · Anchor-prior fate decision
  - Files: `query.py:59` (`ENABLE_ANCHOR_PRIOR = False`), lines 6748-6811
    (machinery), `tests/test_anchor_prior.py`
  - Behavior: scout audits whether anchor-prior is (a) dead code to delete,
    (b) kept behind a flag documented in `config.py`, or (c) productized and
    turned on. Recommendation from this audit: productize — OPUS-B-03
    (contextual follow-up chips) already uses the adjacent infrastructure,
    and follow-up quality is a known weak spot.
  - Accept: decision recorded in
    `notes/specs/anchor_prior_decision_<date>.md`; code matches the decision
  - **Coordinates with OPUS-B-03** (landed; chip candidate generator reads
    anchor state)

- `BACK-R-02` · **P1 · M · worker** · Context-aware bridge-guide demotion
  - Files: `query.py:5606-5607` (uniform 0.06 demotion) and the 30-plus branch
    rerank conditions that follow
  - Behavior: when a query is flagged acute but *also* contains planning
    keywords (`prepare`, `ready`, `set up`, `plan`, `before`, `readiness`,
    `capability`), reduce or skip bridge demotion so planning-adjacent
    questions keep their primary source. Tune thresholds against the mental-
    health validation lane and the `healthcare-capability-assessment`-style
    prompts flagged by reviewer.
  - Accept: validation pack demonstrates planning queries keep primary
    sources; no regression on the acute mental-health panel
  - **Rebases onto landed OPUS-A-10**

- `BACK-R-03` · **P2 · S · worker** · Sticky anchor lifecycle reset
  - Files:
    `android-app/app/src/main/java/com/senku/mobile/SessionMemory.java:56-62`,
    `ChatSessionStore.java:43` (`ensureConversationId()`)
  - Behavior: add an idle-reset policy — if a conversation is reused after >
    1 hour of inactivity, clear the sticky anchor so the next query is not
    mis-biased toward the previous anchor's guide family
  - Accept: instrumentation test seeds a two-turn conversation, waits past
    the idle threshold, and confirms the third turn does not inherit the old
    anchor

- `BACK-R-04` · **P2 · XS · worker** · Anchor-prior as typed parameter (hygiene)
  - Files:
    `android-app/app/src/main/java/com/senku/mobile/SessionMemory.java:247-276`,
    `PackRepository.java:300-322` (`parseSearchQuery`)
  - Behavior: refactor `"__anchor_prior__:<guideId>:<since>:<total> <query>"`
    prefix hack into a typed `AnchorPriorDirective` parameter on
    `PackRepository.search()`. Pure hygiene — eliminates the risk of a future
    user query colliding with the sentinel prefix.
  - Accept: grep shows no `"__anchor_prior__"` string outside the deprecated
    path
  - **Gate on BACK-R-01 decision** (if anchor-prior is deleted, this task
    dissolves)

- `BACK-R-05` · **P2 · S · scout** · Android anchor-prior productization decision
  - Files: read-only audit of `android-app/app/src/main/java/com/senku/mobile/SessionMemory.java` (line 22 flag, line 1190 test-only setter, line 193 gate), `ChatSessionStore.java`, `OfflineAnswerEngine.java` for candidate production callers; desktop reference at `query.py:60`
  - Behavior: decide whether to productize anchor-prior on Android by flipping `ENABLE_ANCHOR_PRIOR = true` and wiring a production caller, or remove the half-commit code entirely. Surfaced during Wave A closeout rerun when `BACK-R-03` was invalidated — desktop has `ENABLE_ANCHOR_PRIOR = True` in `query.py:60` but Android still has it `false` with no production setter, leaving the sticky-anchor idle-reset machinery unreachable on mobile.
  - Accept: decision doc at `notes/specs/android_anchor_prior_decision_20260418.md` covers (a) current state on both sides, (b) cost/benefit of productizing on Android vs. removing the code, (c) recommendation, (d) follow-on tasks if productize is chosen (new `BACK-R-06` for on-device validation with a revived harness)
  - Depends on: `BACK-R-01` decision context
  - **Post-release; not a Wave A or Wave B dependency.**

### Lane P — Pack / repo / catalog

- `BACK-P-01` · **P1 · M · worker** · FTS runtime capability test
  - Files:
    `android-app/app/src/main/java/com/senku/mobile/PackRepository.java:4503-4546`
    (`detectFtsAvailabilityInternal`)
  - Behavior: supplement the `PRAGMA compile_options` probe with a real
    runtime test — attempt a tiny FTS5 `MATCH` query, catch the exception,
    fall back to FTS4, then to `LIKE`. Per
    `ANDROID_SQLITE_FTS5_STATUS_2026-04-18.md`, the compile-options probe
    currently reports FTS5/FTS4 unavailable on all four test emulators, yet
    shell `sqlite3` executes FTS4 fine — that's the gap to close.
  - Accept: emulators that previously fell through to `LIKE` now execute FTS4;
    status note updated
  - **Coordinates with landed OPUS-C-08** (diagnostic capture is the evidence
    base; this is the fix)

- `BACK-P-02` · **P1 · S · worker** · SQLite schema validation on install
  - Files:
    `android-app/app/src/main/java/com/senku/mobile/PackInstaller.java:125-127`
    (`validateInstalledFiles`)
  - Behavior: after the size + SHA checks pass, open the installed SQLite
    read-only and confirm required tables exist (`guides`, `guide_related`,
    `lexical_chunks_fts5` or `lexical_chunks_fts4`). Fail the install if any
    are missing — catches truncated pack copies that slip past size/SHA.
  - Accept: unit test corrupts the tail of a pack, confirms install fails;
    unmodified pack still installs cleanly

- `BACK-P-03` · **P2 · S · worker** · Bridge-tag consistency audit
  - Files: `ingest.py:692` (sets `bridge` field), `query.py:5543-5553`
    (`_is_bridge_guide_metadata` with fallback logic)
  - Behavior: at ingest validation time, assert that if a guide is detected as
    bridge-pattern, the `bridge` boolean is set *and* no stale CSV tag
    containing `"bridge-guide"` remains. Log a warning on any guide that
    appears in both old-tag and new-tag form.
  - Accept: re-ingest on current guide corpus produces zero inconsistency
    warnings
  - **Post-OPUS-A-10 cleanup**

- `BACK-P-04` · **[done 2026-04-18 `bbc1b1d`]** · **P0 · S · worker** · Pack export SHA race (stock bundle re-exported `aa1b399`)
  - Files: `mobile_pack.py:1455-1503` (`export_mobile_pack` tail — the offending
    ordering); new regression test under `tests/` (miniature export fixture +
    manifest-parity assertion); stock bundle regen into
    `android-app/app/src/main/assets/mobile_pack/`
  - Problem: `export_mobile_pack()` writes `senku_manifest.json` with the
    SQLite's pre-upsert SHA, then opens the SQLite read-write and runs
    `_upsert_pack_meta(...)`, mutating pack pages in place without changing
    the byte count. Every pack ships with a stale `sqlite_sha256` in the
    manifest, so `PackInstaller.validateInstalledFile` (size+SHA gate at
    `PackInstaller.java:150-158`) throws
    `Installed senku_mobile.sqlite3 checksum mismatch` on first install.
    Surfaced as `BACK-P-02-FAIL` on emulator-5556 stock install today;
    evidence in `artifacts/bench/wave_a_closeout_20260418/back_p_02/` and
    `notes/WAVE_A_CLOSEOUT_FAIL_2026-04-18.md`. Vectors file is not
    re-opened post-manifest and its SHA already matches.
  - Behavior: reorder `export_mobile_pack()` so every SQLite mutation —
    including `_upsert_pack_meta(...)` — completes and the connection
    closes **before** `_file_info(sqlite_path)` computes the SHA and before
    `manifest_path.write_text(...)` commits the manifest. Keep
    `PRAGMA journal_mode=DELETE` (line 1401) so no WAL/journal sidecars
    outlive the final close. Leave the `sqlite_sha256` row inside
    pack_meta as telemetry only and annotate at the upsert site that the
    manifest is the single source of truth; confirm no Java consumer
    reads pack_meta's sqlite_sha256 for validation (grep PackInstaller +
    PackRepository).
  - Regen: re-export the stock bundle with the fixed pipeline into
    `android-app/app/src/main/assets/mobile_pack/`. Sanity-gate before
    the worker exits by recomputing SHAs on disk and asserting parity
    against the written manifest.
  - Accept: new pipeline test that exports a miniature fixture and
    asserts `sha256(senku_mobile.sqlite3) == manifest.files.sqlite.sha256`
    passes; stock bundle manifest-parity check passes; emulator-5556
    `pm clear com.senku.mobile` + launch no longer surfaces
    "Manual install failed"; BACK-P-02's pre-existing SHA gate becomes
    the standing regression guard.
  - **Blocks** re-running Wave A closeout (`BACK-R-03`, `BACK-P-01`,
    `BACK-P-02`). **Concurrent-safe with** `OPUS-E-06` — no file overlap.

### Lane T — Test / validation

- `BACK-T-01` · **P1 · S · worker (test-infra)** · Confidence-label unit tests
  - Files: new `confidence_label_contract.py`, new
    `tests/test_confidence_label.py`
  - Behavior: pre-bake the blocked Wave B contract with desktop-only tests for
    the high / medium / low / uncertain-fit / abstain bands, including the
    expected system-instruction id and MetaStrip token/tone mapping.
  - Accept: contract stays green while the UI-plumbed U-lane lands through
    Wave B dispatch

- `BACK-T-02` · **P1 · M · worker (test-infra)** · Multi-turn anchor-prior tests
  - Files: new coverage in `tests/test_anchor_prior.py`
  - Behavior: seed multi-turn desktop follow-ups and assert anchor-prior bias
    persists across the second and third follow-up, then clears after an
    explicit reset turn.
  - Accept: the productized R-01 path stays locked without requiring Android UI
    or device validation

- `BACK-T-03` · **P1 · S · worker (test-infra)** · Abstain false-positive regression
  - Files: `scripts/run_abstain_regression_panel.ps1` (built in `BACK-U-04`)
  - Behavior: this is the routine runner of the panel, wired into the bench
    cadence. Track FP rate over time in
    `artifacts/bench/abstain_regression_<date>/`.
  - **Depends on BACK-U-04**

- `BACK-T-04` · **[done 2026-04-19 `2656311`]** · **P2 · XS · worker (test-infra)** · Fix `Quote-AndroidShellArg` in session-flow harness
  - Files: `scripts/run_android_session_flow.ps1`
  - Behavior: `scripts/run_android_session_flow.ps1` currently fails on `emulator-5556` with "`Quote-AndroidShellArg` not recognized" — the helper is referenced but not defined/imported. Fix by either inlining the quoting logic, dot-sourcing the helper from a sibling script, or importing from an existing PowerShell utility module. Ensure the repaired script can drive at least one idle-reset session-flow scenario end-to-end.
  - Accept: `scripts/run_android_session_flow.ps1` runs cleanly against `emulator-5556` without the `Quote-AndroidShellArg` error; one sample session-flow scenario completes with captured logcat
  - Surfaced during: Wave A closeout rerun when `BACK-R-03` could not be driven on-device
  - **Post-release; unblocks future session-flow / multi-turn harness work including any revived anchor-prior validation if `BACK-R-05` decides productize.**
  - **Landed 2026-04-19 (`2656311`)** — `run_android_session_flow.ps1` now defines `Quote-AndroidShellArg` locally, matching the sibling harness quoting behavior so `auto_query` launches no longer fail before `am start`

### Lane H — Hygiene (safe-parallel, low priority)

- `BACK-H-01` · **P2 · S · worker** · Consolidate tag normalization
  - Files: `query.py:5520-5540` + `ingest.py:418-440`; new
    `metadata_helpers.py` shared module
  - Behavior: DRY the `_normalize_metadata_tag` / `normalize_tag_value`
    duplication; both sides import from the shared helper; test asserts
    identical behavior

- `BACK-H-02` · **P2 · M · worker** · Registry from introspection
  - Files: `deterministic_special_case_registry.py`, new
    `scripts/regenerate_deterministic_registry.py`
  - Behavior: generate registry entries from `special_case_builders` +
    predicate functions via introspection, so renames don't silently break
    rules. Hand-curated fields (priority, promotion_status, notes) stay in a
    sidecar YAML; the generator merges.

- `BACK-H-03` · **[done 2026-04-19 `739d26f`]** · **P2 · XS · worker** · Type hint on registry builder
  - Files: `query.py:5364`
  - Behavior: change `Callable[[], str] | None` to `Callable[..., str] | None`
    so future builders that take the question as context don't trip the type
    check
  - **Landed 2026-04-19 (`739d26f`)** — acceptance was already satisfied in immutable Wave B code from `af49d91`; this queue verified the broad `Callable[..., str] | None` signature is present, confirmed zero legacy `Callable[[], str] | None` hits, and left `query.py` untouched per the Wave B scope guard

- `BACK-H-04` · **P2 · XS · worker** · Centralize Android `Locale`
  - Files: `OfflineAnswerEngine.java`, `SessionMemory.java`,
    `PackRepository.java` (multiple lines)
  - Behavior: introduce `QUERY_LOCALE = Locale.US` in each class; replace
    inline `Locale.US` references

Note: `BACK-H-05` was invalidated on 2026-04-18 after re-audit confirmed
`CONTEXT_SELECTION_STOP_TOKENS` is still live in `OfflineAnswerEngine`.

- `BACK-H-06` · **[done 2026-04-19 `c269abe`]** · **P2 · XS · worker** · Document `FtsRuntime` fallback chain
  - Files: `PackRepository.java` around the `FtsRuntime` class
  - Behavior: class-level docstring documenting the FTS5 → FTS4 → `LIKE`
    fallback chain and the performance implications of each path
  - **Landed 2026-04-19 (`c269abe`)** — `FtsRuntime` now carries class-level Javadoc covering the FTS5-first fallback chain, the FTS4 and `LIKE` branches, and the expected performance gap between steady-state FTS search and the slower safety-net path

---

## State Log

_Append entries here as tasks ship. Format:
`| BACK-XX-nn | done | <agent> | YYYY-MM-DD | YYYY-MM-DD | one-line summary |`_

| task | status | owner | opened | closed | note |
| ---- | ------ | ----- | ------ | ------ | ---- |
| BACK-P-04 | done | codex | 2026-04-18 | 2026-04-18 | `export_mobile_pack()` now finalizes `pack_meta` before hashing, re-exported the stock bundle, and restored clean stock install on emulator-5556 |
| BACK-P-02 | done | codex | 2026-04-18 | 2026-04-18 | post-P-04 re-run green on emulator-5556: stock + missing_table + truncated variants all reach the schema-validation branch; artifacts under `artifacts/bench/wave_a_closeout_20260418_rerun/back_p_02/` |
| BACK-P-01 | done | codex | 2026-04-18 | 2026-04-18 | post-P-04 re-run green on emulator-5554 + 5556; FTS runtime probe traces (`fts.runtime_probe ... lexical_chunks_fts4 supported=true`, live `routeChunkFts` execution) in `artifacts/bench/wave_a_closeout_20260418_rerun/back_p_01/`; FTS status addendum at `notes/ANDROID_SQLITE_FTS5_STATUS_2026-04-18.md` |
| BACK-H-05 | invalidated | codex | 2026-04-18 | 2026-04-18 | `CONTEXT_SELECTION_STOP_TOKENS` is still live in `OfflineAnswerEngine`, so the delete-unused audit was struck |
| BACK-H-04 | done | codex | 2026-04-18 | 2026-04-18 | `QUERY_LOCALE` now centralizes Locale.US usage in OfflineAnswerEngine, SessionMemory, and PackRepository |
| BACK-H-02 | done | codex | 2026-04-18 | 2026-04-18 | regenerate_deterministic_registry.py now round-trips the registry from introspection plus a hand-curated YAML sidecar |
| BACK-T-02 | done | codex | 2026-04-18 | 2026-04-18 | desktop anchor-prior panel now covers second/third follow-ups plus explicit reset-turn clearing |
| BACK-T-01 | done | codex | 2026-04-18 | 2026-04-18 | desktop confidence-label contract tests pre-bake the blocked Wave B high/medium/low/uncertain-fit/abstain states |
| BACK-L-04 | done | codex | 2026-04-18 | 2026-04-18 | summarize_latency.py now prints p50/p95/max per stage from exported AnswerRun JSON and JSONL |
| BACK-L-03 | done | codex | 2026-04-18 | 2026-04-18 | PackRepository now times probe/search stages, records fallback mode, and emits slow-query tripwires on budget overruns |
| BACK-L-01 + BACK-L-02 | done | codex | 2026-04-18 | 2026-04-18 | generate() now logs regex-stable latency summaries, emits SenkuLatency events, and persists AnswerRun latencyBreakdown through SessionMemory |
| BACK-R-01 | done | codex | 2026-04-18 | 2026-04-18 | anchor-prior decision now matches desktop code with ENABLE_ANCHOR_PRIOR enabled and tests green |
| BACK-R-02 | done | codex | 2026-04-18 | 2026-04-18 | planning/readiness acute queries now skip uniform bridge demotion and desktop rerank tests cover the branch |
| BACK-R-03 | invalidated | codex | 2026-04-18 | 2026-04-18 | anchor-prior is feature-gated off on Android (`SessionMemory.java:22` `ENABLE_ANCHOR_PRIOR = false`); only setter is the test helper at `SessionMemory.java:1190`; idle-reset path is non-observable in production; re-scope via `BACK-R-05` if/when Android anchor-prior is productized; see `notes/WAVE_A_CLOSEOUT_FAIL_2026-04-18_rerun.md` |
| BACK-R-04 | done | codex | 2026-04-18 | 2026-04-18 | anchor prior now passes as a typed `AnchorPriorState` through retrieval and rerank |
| OPUS-E-06 | done | codex | 2026-04-18 | 2026-04-18 | AnswerPresenter carve-out landed (`b41128a`); three `OfflineAnswerEngine.generate()` callsites moved behind `AnswerPresenter`; `DetailActivity.java` 6264 → 6063; Wave B un-gated |
| BACK-R-05 | open | — | 2026-04-18 | — | decide whether to productize anchor-prior on Android (flip `SessionMemory.java:22` + wire production caller) or remove the half-commit code; scout spike; post-release |
| BACK-T-04 | done | codex | 2026-04-18 | 2026-04-19 | `run_android_session_flow.ps1` now defines `Quote-AndroidShellArg` locally so the session-flow harness can quote `auto_query` safely again; commit `2656311` |
| BACK-U-02 | done | codex | 2026-04-18 | 2026-04-18 | desktop + Android abstain responses now prepend the pinned safety-critical escalation line above "Closest matches" when the upstream safety flag is set, with table-driven tests and phone + tablet validation artifacts; commit `d974ebc` |
| BACK-U-03 | done | codex | 2026-04-18 | 2026-04-18 | desktop + Android compute and surface high/medium/low answer confidence labels; MetaStrip renders likely-match and low-confidence tokens with phone + tablet instrumentation proof; commit `af49d91` |
| BACK-U-01 | done | codex | 2026-04-19 | 2026-04-19 | desktop + Android introduce three-way `confident`/`uncertain_fit`/`abstain` answer mode with deterministic uncertain-fit body template and `UNSURE FIT` card variant; reuses U-02 escalation helper for safety-critical uncertain-fit; boundary tests + reviewer-worked example pass on both engines; phone + tablet artifacts under `artifacts/bench/wave_b_back-u-01_2026-04-19/`; commit `eb398dc` |
| BACK-H-03 | done | codex | 2026-04-18 | 2026-04-19 | registry builder already accepts an optional `Callable[..., str]` in immutable Wave B code; overnight queue verified zero legacy `Callable[[], str]` hits and recorded the landing; commit `739d26f` |
| BACK-H-06 | done | codex | 2026-04-18 | 2026-04-19 | `FtsRuntime` now documents the FTS5 then FTS4 then LIKE fallback chain, plus the expected performance gap between FTS and the slower correctness-net path; commit `c269abe` |

# DISPATCH PROMPT -- BACK WAVE A (2026-04-18)

Here's a paste-ready dispatch prompt modeled on the existing `OPUS_REINIT_20260418.md` style. Paste the whole block as the first message to a fresh Codex session.

---

## DISPATCH PROMPT -- PASTE BELOW THIS LINE

You are the **Codex executor** for the **Senku** project -- an offline field-manual survival-guide Android app. Your Opus planner has produced a new backend backlog (`BACK-*`) folded from the 2026-04-18 reviewer-backend-unknowns packet. Your assignment is to dispatch **Wave A** of that backlog: 26 tasks that do not cross any UI surface and can land in full parallel with the current `OPUS-E-05` Presenter-extraction refactor.

### Read first, in order

1. [`reviewer_backend_tasks.md`](./reviewer_backend_tasks.md) -- master backend plan; all `BACK-*` task bodies live here
2. [`notes/REVIEWER_BACKEND_TRACKER_20260418.md`](./notes/REVIEWER_BACKEND_TRACKER_20260418.md) -- dispatch strategy, worker split, ranked execution order
3. [`notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`](./notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md) -- reviewer packet driving the work
4. [`opustasks.md`](./opustasks.md) -- parallelism conventions, must-bundle discipline, state-log format (append, never delete)
5. [`notes/specs/parallelism_plan.md`](./notes/specs/parallelism_plan.md) -- collision maps and wave widths
6. [`notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`](./notes/APP_ROUTING_HARDENING_TRACKER_20260417.md) -- rebase target for `BACK-R-02` before dispatch

Do not resume any OPUS-* work. Your scope is `BACK-*` only.

### Routing ladder (unchanged from opustasks.md)

| Tier | Model | Slots | Role |
|------|-------|-------|------|
| orchestrator | GPT-5.4 x high -- fast mode | 1 | Dispatches, merges, gates checkpoints |
| worker | GPT-5.4 high | 4-5 | File-edit tasks |
| scout | GPT-5.3 Codex Spark high -> GPT-5.4 mini medium fallback | 2-3 | Read-only audits, near-miss panels, spec drafting |

### HARD GATE -- DO NOT TOUCH IN THIS WAVE

**`BACK-U-01`, `BACK-U-02`, `BACK-U-03` are blocked on `OPUS-E-05`.** They plumb new `AnswerRun` fields through `DetailActivity.java`, which E-05 is splitting into Presenter classes. Dispatching any of these during E-05 causes merge churn on a 6800-line file. Scout-tier spec drafting for their addenda (MetaStrip confidence token, PaperAnswerCard uncertain-fit variant, safety-critical escalation copy) may proceed -- code edits may not. `BACK-U-04` is script + note only and **is** in Wave A.

**`BACK-T-01` is implicitly gated** (depends on `BACK-U-03`). Hold.

### Wave A scope (26 tasks)

- **Lane D (6)** -- BACK-D-01, D-02, D-03, D-04, D-05, D-06
- **Lane L (4)** -- BACK-L-01, L-02, L-03, L-04
- **Lane R (4)** -- BACK-R-01, R-02, R-03, R-04
- **Lane P (3)** -- BACK-P-01, P-02, P-03
- **Lane T (2)** -- BACK-T-02, T-03 (T-01 is held)
- **Lane H (6)** -- BACK-H-01 through H-06
- **BACK-U-04** -- abstain tuning note; no render path

### Dispatch order

**Sprint 1 -- Phase B-1 deterministic safety (reviewer priority #1):**
1. **Must-bundle** `BACK-D-01 + BACK-D-05` (same file: `deterministic_special_case_registry.py` schema + tie-break)
2. **Must-bundle** `BACK-D-02 + BACK-D-04` (semantic verifier + near-miss panel; scout drafts the 20+ near-miss cases on Spark before worker dispatch)
3. Safe-parallel `BACK-D-03` (builder-missing warning surfacing)
4. Safe-parallel `BACK-D-06` (mobile pack parity script)

Checkpoint B-CP1 must be green before treating lane D closed: graduation manifest exists, `promotion_status` field on every spec, near-miss panel covers the 9 live Android rules, `scripts/validate_special_cases.py` and the new parity script both green.

**Sprint 2 -- Phase B-3 latency (reviewer priority #3, interleave as slots open):**
1. **Must-bundle** `BACK-L-01 + BACK-L-02` (both edit `OfflineAnswerEngine.generate()` -- TTFT + `LatencyPanel`)
2. Serial `BACK-L-03` after L-02 (needs `LatencyPanel` schema)
3. Safe-parallel `BACK-L-04` (reranker timing; `query.py` + Android)

Checkpoint B-CP3 requires a checked-in baseline under `artifacts/bench/latency_baseline_<date>/`.

**Sprint 3 -- Phase B-4 retrieval + pack:**
1. **Scout first** -- `BACK-R-01` anchor-prior fate decision. Spark reads `query.py:59` + 6748-6811, writes `notes/specs/anchor_prior_decision_<date>.md`. Wait for planner review before worker-tier implementation.
2. `BACK-R-02` (context-aware bridge demotion). **Rebase onto the latest mental-health routing patch in `APP_ROUTING_HARDENING_TRACKER_20260417.md` before edits.**
3. `BACK-R-03` and `BACK-R-04` dispatch after R-01 decision is recorded (R-04 dissolves if anchor-prior is deleted).
4. **Must-bundle** `BACK-P-01 + BACK-P-02` (both edit `PackRepository.java` / `PackInstaller.java` adjacent code). P-01 supplements the `PRAGMA compile_options` probe with a runtime capability test; P-02 adds schema validation.
5. Safe-parallel `BACK-P-03` (bridge-tag consistency audit at ingest time).

**Sprint 4 -- Tests + `BACK-U-04`:**
1. `BACK-U-04` on a scout slot at any point (abstain tuning note + regression panel script)
2. `BACK-T-02` after R-01 decision + R-03 ship (multi-turn anchor-prior tests)
3. `BACK-T-03` after U-04 ships (abstain regression runner)

**Fill lane -- Hygiene (continuous, use for slack worker capacity):**
- `BACK-H-01` through `H-06`. All safe-parallel with everything above. Do not let H-lane starve higher-priority dispatch.

### Must-bundle pairs

| Bundle | Files / Reason |
|--------|---------------|
| `BACK-D-01 + D-05` | `deterministic_special_case_registry.py` schema region |
| `BACK-D-02 + D-04` | Verifier + the test suite that tunes it |
| `BACK-L-01 + L-02` | Both edit `OfflineAnswerEngine.generate()` |
| `BACK-P-01 + P-02` | Adjacent regions in `PackRepository.java` / `PackInstaller.java` |

### Strict-serial rules

- `BACK-D-01 -> BACK-D-02` (verifier consults `promotion_status`)
- `BACK-L-02 -> BACK-L-03` (model-vs-harness breakdown needs the LatencyPanel schema)
- `BACK-R-01` scout decision -> `BACK-R-03`, `BACK-R-04`
- `BACK-U-04` -> `BACK-T-03` (regression runner depends on the panel spec)
- All of Wave A -> Wave B (Lane U code)

### Acceptance pattern (run before marking any BACK task done)

1. **Tests**: `python3 -m unittest discover -s tests -v` (desktop); `./gradlew :app:testDebugUnitTest` (Android)
2. **Deterministic guard**: `python3 scripts/validate_special_cases.py`
3. **New parity guard** (after D-06 lands): `scripts/validate_mobile_pack_deterministic_parity.ps1`
4. **Re-ingest**, if the task touched `ingest.py` or guide schemas, before trusting any retrieval check
5. **Bench baseline**, if the task added latency instrumentation: produce the panel and check it in under `artifacts/bench/`
6. **Status-log entry** on close, in `reviewer_backend_tasks.md` State Log table, format identical to `opustasks.md`: `| BACK-XX-nn | done | codex | YYYY-MM-DD | YYYY-MM-DD | one-line summary |`

### Practical cautions (repo-specific)

- LM Studio / LiteRT host may return intermittent `500`s during validation runs. A host-contaminated wave is **not** a product signal -- note it in the state log and re-run before drawing conclusions.
- Re-ingest is single-consumer; the orchestrator owns it. Workers do not run `ingest.py` independently.
- For `query.py` edits, provide the exact line number from grep -- the file is 10,218 lines and approximate ranges cause merge pain.
- Spacing: 3-4 concurrent dispatches at 30 s intervals; 5+ at 60 s strict; 6+ on the same file forbidden, bundle instead.
- Spark scout errors fall back to GPT-5.4 mini medium. **Do not** re-dispatch scout work at worker tier -- that burns high-mode capacity on audit work.

### Your first actions

1. Read the six context files above in order.
2. Open `reviewer_backend_tasks.md` and verify the State Log table is unchanged since the 2026-04-18 snapshot. If Opus has updated any entries, sync your mental model.
3. Dispatch Sprint 1 wave: four dispatches (two bundles + two safe-parallel) with 30 s spacing. Workers are GPT-5.4 high; one Spark scout on standby for near-miss brainstorming in the D-02+D-04 bundle.
4. While Sprint 1 runs, send Spark xhigh scouts on the three **Wave B spec addenda** (MetaStrip confidence token, PaperAnswerCard uncertain-fit variant, abstain escalation copy) so Wave B is pre-baked when `OPUS-E-05` closes.
5. Report back after CP-B1 with: tasks landed, test results, state-log deltas, next sprint's proposed dispatch.

Do not ask clarifying questions -- the plan is written. If you hit ambiguity, flag it in your report and continue on the adjacent parallel task.

*End of dispatch prompt.*

---

That's the whole thing. Two notes on using it: first, if your Codex session lives inside the repo with file access, the context reads will resolve; if it's a remote session, paste the four canonical files along with this prompt so Codex doesn't hallucinate task bodies. Second, the prompt assumes your current orchestrator process is willing to send Spark scouts in parallel during Sprint 1 -- if Spark is down, the D-02+D-04 bundle just runs later at worker tier and the near-miss brainstorming stretches out.
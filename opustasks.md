# opustasks.md

Living backlog folded from the 2026-04-17 Claude Opus 4.7 review, codex waves 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 (landed), plus the Rev 03 UI redesign absorbed on 2026-04-17.

Not a replacement for the existing trackers ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â this file **indexes** them and sequences the work with explicit checkpoints. Use it as the session-to-session entry point; use the per-tracker files for execution detail.

- Review: [`notes/CLAUDE_OPUS_47_REVIEW_20260417.md`](./notes/CLAUDE_OPUS_47_REVIEW_20260417.md)
- HTML mirror: [`artifacts/external_review/claude_opus_47_review_20260417/index.html`](./artifacts/external_review/claude_opus_47_review_20260417/index.html)
- Routing tracker: [`notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`](./notes/APP_ROUTING_HARDENING_TRACKER_20260417.md)
- UI state pack recovery plan: [`notes/UI_STATE_PACK_RECOVERY_PLAN_20260417.md`](./notes/UI_STATE_PACK_RECOVERY_PLAN_20260417.md)
- Swarm architecture: [`notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md`](./notes/ENGINEERING_SWARM_ARCHITECTURE_20260416.md)
- Guide plan: [`GUIDE_PLAN.md`](./GUIDE_PLAN.md)
- Gallery top-10 (pre-Rev-03): [`artifacts/external_review/ui_review_20260417_gallery/index.html`](./artifacts/external_review/ui_review_20260417_gallery/index.html)
- **Rev 03 UI direction (locked):** [`notes/specs/ui_direction_rev03.md`](./notes/specs/ui_direction_rev03.md)

## How To Use This File

- Task IDs are stable: `OPUS-<lane><seq>`. When a task ships, mark it `[done 2026-MM-DD <hash>]` rather than deleting; keeps history legible.
- Priorities: `P0` safety / correctness, `P1` stability / UX, `P2` code hygiene.
- Estimates: `XS` < 30 min, `S` < 2 hrs, `M` < 1 day, `L` > 1 day.
- **Routing ladder (rev 2):**
  - **orchestrator** = **GPT-5.4 ÃƒÆ’Ã¢â‚¬â€ high ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â fast mode**: 1 slot. Dispatches, merges, gates checkpoints.
  - **worker** = **GPT-5.4 high mode**: 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“5 slots. All file-edit tasks.
  - **scout** = **GPT-5.3 Codex Spark high** (fallback: **GPT-5.4 mini medium** on Spark outage): 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 slots. Read-only audits, prompt-pack drafting, risk checks, doc spikes.
  - Sub-lane tags (`test-infra`, `main`, etc.) remain as *role markers* ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â they describe what kind of work the task is, not which model runs it. All edit work uses the worker tier.
- Complex tasks link to a spec in [`notes/specs/`](./notes/specs/).
- **Before dispatching a wave:** read [`notes/specs/parallelism_plan.md`](./notes/specs/parallelism_plan.md) for file-collision maps and safe fan-out widths.
- **Wave gate discipline:** batch work by the defined wave, mark each in-flight worker in the state log, and do not dispatch the next wave until every worker result from the current wave has been integrated into the main worktree and re-validated locally.

## Status Snapshot (2026-04-18 after release-candidate v2 pack + gallery regen)

**Landed:** A-01..A-10; B-01, B-02, B-03, B-05, B-06, B-07, B-08, B-09, B-10, B-12, B-13, B-14, B-15, B-16; C-01, C-02, C-03, C-04, C-05, C-06, C-07, C-08; D-01, D-02, D-03, D-04, D-05, D-06; E-01, E-02, E-03, E-04, E-06; F-01, F-02, F-03, F-04, F-05, F-06, F-07, F-08, F-09, F-10, F-11, F-12, F-13, F-14, F-15 (58 landed tasks; CP1-CP4 green; Phase 5 closed; Phase 6 green; Phase 7 closed; Phase 8 infra green with app-rendered Android reviewer packet 45/45 at `artifacts/ui_state_pack_release_candidate_v2/20260418_200902`; fresh external-review gallery is ready at `artifacts/external_review/ui_review_20260418_201903_gallery/index.html`; Phase D guide wave targeted reruns green).

**Still open (surviving):** C-09 (open P2 follow-up; invalidate harness install-state cache on emulator cold boot). `E-05` remains paused at its partial-completion milestone (`MainActivity.java` 3055, `DetailActivity.java` 6265). The narrow `E-06` carve-out landed 2026-04-18 (`b41128a`) and unblocked reviewer-backend Wave B without reopening the wider E-05 refactor. Remaining non-code release steps are reviewer sign-off and the signed release APK build.

**Superseded by Rev 03:** B-04 (rolled into F-02 MetaStrip), B-11 (rolled into F-07 + F-12 + F-14).

**New lane ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F (Rev 03 UI migration):** F-01..F-15 (see direction doc).

**Also new (post-Haiku review):** renumbered B-13 (text-overflow), B-14 (tag casing ROLE_ drop), B-15 (Rev 03 palette contrast audit).

## Parallelism Fan-Out Summary

Full plan: [`notes/specs/parallelism_plan.md`](./notes/specs/parallelism_plan.md) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â orchestrator (GPT-5.4 fast) reads it before dispatching each wave. Short version below.

**Hotspots (touch this file, coordinate):**
- `activity_detail.xml` (base) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F-03 + F-13 (bundle), B-08 (rebased onto F-06)
- `res/layout-land/activity_detail.xml` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B-07 + B-10 (bundle)
- `activity_main.xml` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F-05, F-07, F-12, F-14 (must-bundle, see below)
- `values/strings.xml` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F-01 + F-02 string vocabulary + B-09 copy scrub (bundle the first two; B-09 safe-parallel)
- `values/colors.xml` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F-01 (bundle with font resources)
- `item_search_result.xml` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F-08 + B-06 (bundle)
- `DetailActivity.java` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F-03 mount, F-13 mount, B-08 rebase, E-05 (E-05 strictly serial, post-F-lane)
- `MainActivity.java` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â F-05 nav plumbing + F-07 identity strip bind (bundle)

**Must-bundle pairs** (same file, same region ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â never fan out):
- `F-03 + F-13` (activity_detail.xml ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â answer card + docked composer sit in sibling regions)
- `F-05 + F-07` (MainActivity.java nav plumbing + identity strip bind)
- `F-08 + B-06` (item_search_result.xml)
- `B-07 + B-10` (res/layout-land/activity_detail.xml)
- `F-01 tokens + font resources` (colors.xml + res/font/ additions ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â one patch)

**Strict-serial pairs** (do one, then the other):
- `A-04 + A-10` (both rev metadata-rerank delta ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A-04 landed, A-10 rebases)
- `E-05` AFTER all F-lane land and after B-03, B-06..B-10 rebase (Presenter extraction conflicts with every Activity surface edit)

**Blocked / ordered dependencies:**
- `F-03, F-06, F-07, F-08, F-12, F-14` depend on `F-01` (tokens+fonts) and `F-02` (MetaStrip)
- `F-06` depends on `F-03`, `F-09`, `F-11`
- `F-10` depends on `B-03` (chip candidate generator) for real data; can ship with static placeholder first
- `F-11` depends on `OPUS-A-01` reciprocal-link map (landed ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â done)
- `B-08` rebases onto `F-06` (3-pane a11y regions)
- `B-13, B-15` run after all F-lane land (audits Rev 03 surfaces)

**Wave widths** (per parallelism plan):
- Waves 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3: **landed** (codex completed 2026-04-17)
- Wave 4 (F-lane foundation + backend cleanup): **5 workers + scout + 2-3 D-lane fill**
- Wave 5 (Rev 03 answer surface): 4 workers + scout
- Wave 6 (Rev 03 nav + home + search): 5 workers + scout
- Wave 7 (Rev 03 tablet + landscape): 4 workers + scout
- Wave 8 (test-infra + Rev 03 audits): 5 workers + scout
- Wave 9 (Presenter extraction + release prep): 2 workers serial + orchestrator + scout

**Practical ceiling per wave (rev 3):** 1 orchestrator (GPT-5.4 fast) + **4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“5 workers** (GPT-5.4 high) + **2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 scouts** (Spark 5.3 high ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini medium fallback). Total 7ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9 concurrent slots. The orchestrator is the merge rate-limiter; exceed 5 workers and patches queue behind each other.

**Spacing rule:** 3ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“4 concurrent worker dispatches = 30s spacing; 5+ = 60s strict; 6+ on the same file = forbidden, bundle.

**Fallback rule:** if Spark scout returns an error, re-dispatch the same prompt at GPT-5.4 mini medium. Do NOT re-dispatch scout work at the worker tier ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â that burns high-mode capacity on audit work.

## Phases + Checkpoints

### Phase 1 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Cheap hygiene (landed)

`OPUS-E-01` Ãƒâ€šÃ‚Â· `OPUS-E-02` Ãƒâ€šÃ‚Â· `OPUS-E-04` Ãƒâ€šÃ‚Â· `OPUS-A-03` Ãƒâ€šÃ‚Â· `OPUS-A-08` Ãƒâ€šÃ‚Â· `OPUS-C-03` Ãƒâ€šÃ‚Â· **CHECKPOINT 1 green**

### Phase 2 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Safety-routing foundations (landed)

`OPUS-A-01` Ãƒâ€šÃ‚Â· `OPUS-A-02` Ãƒâ€šÃ‚Â· `OPUS-A-04` Ãƒâ€šÃ‚Â· `OPUS-A-05` Ãƒâ€šÃ‚Â· `OPUS-A-06` Ãƒâ€šÃ‚Â· `OPUS-A-07` Ãƒâ€šÃ‚Â· `OPUS-C-05` Ãƒâ€šÃ‚Â· **CHECKPOINT 2 green** (gallery 06 re-shoot clean)

### Phase 3 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Answer contract + empty-state UX (landed)

`OPUS-B-01` Ãƒâ€šÃ‚Â· `OPUS-B-02` Ãƒâ€šÃ‚Â· `OPUS-B-05` Ãƒâ€šÃ‚Â· `OPUS-C-04` Ãƒâ€šÃ‚Â· **CHECKPOINT 3 green** (gallery 04 replacement clean)

### Phase 4 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Rev 03 foundation + backend cleanup (2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days)

Goal: land the Compose primitive foundation and close the remaining backend cleanup.

`OPUS-F-01` Ãƒâ€šÃ‚Â· `OPUS-F-02` Ãƒâ€šÃ‚Â· `OPUS-F-04` Ãƒâ€šÃ‚Â· `OPUS-F-09` Ãƒâ€šÃ‚Â· `OPUS-F-12` Ãƒâ€šÃ‚Â· `OPUS-A-09` Ãƒâ€šÃ‚Â· `OPUS-A-10` Ãƒâ€šÃ‚Â· `OPUS-E-03`

> **CHECKPOINT 4 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Foundation primitives green**
> - MetaStrip unit + screenshot tests pass on all four postures
> - TopBar, SourceRow, PivotRow, ThreadRow primitives render in isolation
> - design tokens (colors, fonts) compile clean
> - A-09 / A-10 land without breaking any routing pack
> - E-03 VectorStore unmap confirmed via hot-swap test
> - if green, proceed to Phase 5

### Phase 5 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Rev 03 answer surface (2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days)

Goal: replace the answer body with the paper AnswerCard and its wiring.

`OPUS-F-03` Ãƒâ€šÃ‚Â· `OPUS-F-10` Ãƒâ€šÃ‚Â· `OPUS-F-13` Ãƒâ€šÃ‚Â· `OPUS-B-03` Ãƒâ€šÃ‚Â· `OPUS-B-09`

> **CHECKPOINT 5 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Answer surface green**
> - F-03 + F-13 (bundled) replace the detail answer region
> - gallery 04 re-shoot shows abstain in paper-card form
> - gallery 06 re-shoot shows stacked Q+A paper cards with thread continuity
> - F-10 Try next chips render real graph-derived candidates via B-03
> - B-09 copy sanitizer catches all known warning residues

### Phase 6 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Rev 03 navigation + home + search (2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days)

Goal: replace home and search surfaces with Rev 03 primitives.

`OPUS-F-05` Ãƒâ€šÃ‚Â· `OPUS-F-07` Ãƒâ€šÃ‚Â· `OPUS-F-08` Ãƒâ€šÃ‚Â· `OPUS-F-14` Ãƒâ€šÃ‚Â· `OPUS-B-06`

> **CHECKPOINT 6 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Home + search green**
> - bottom tabs render with correct active states
> - home identity strip, pivot rows, and category shelf render at expected densities
> - search results render as paper cards with lane tags
> - B-06 continue-thread chip surfaces on result rows when warm

### Phase 7 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Rev 03 tablet 3-pane + landscape (2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days)

Goal: eliminate the wasted-landscape problem; wire live evidence pane.

`OPUS-F-06` Ãƒâ€šÃ‚Â· `OPUS-F-11` Ãƒâ€šÃ‚Â· `OPUS-B-07` Ãƒâ€šÃ‚Â· `OPUS-B-08` Ãƒâ€šÃ‚Â· `OPUS-B-10`

> **CHECKPOINT 7 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Tablet parity green**
> - gallery 10 (tablet landscape) re-shoot shows 3-pane reader with no wasted space
> - evidence pane pulls a live snippet from the anchor chunk
> - cross-reference list uses `getReciprocalLinks(anchorId)`
> - phone landscape (5560) composer never obscured
> - TalkBack walks the three panes as distinct regions

### Phase 8 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Test-infra + Rev 03 audits (2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days)

Goal: rebuild the trusted test harness, run the Rev 03 audit tasks.

`OPUS-C-01` Ãƒâ€šÃ‚Â· `OPUS-C-02` Ãƒâ€šÃ‚Â· `OPUS-C-06` Ãƒâ€šÃ‚Â· `OPUS-C-07` Ãƒâ€šÃ‚Â· `OPUS-C-08` Ãƒâ€šÃ‚Â· `OPUS-B-13` Ãƒâ€šÃ‚Â· `OPUS-B-14` Ãƒâ€šÃ‚Â· `OPUS-B-15` Ãƒâ€šÃ‚Â· `OPUS-F-15`

> **CHECKPOINT 8 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â State pack 45/45, Rev 03 audits green**
> - `build_android_ui_state_pack_parallel.ps1` produces 45/45 with artifacts
> - every Rev 03 primitive clears B-13 overflow, B-14 tag casing, B-15 contrast audit
> - F-15 removes Tweak runtime toggle from prod builds

### Phase 9 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Presenter extraction + release prep (release-candidate ready)

Goal: package the field-manual Rev 03 release candidate; deeper presenter-pattern decomposition can resume later if we choose.

`OPUS-E-05` (partial completion; deferred) Ãƒâ€šÃ‚Â· `OPUS-E-06` [done 2026-04-18 b41128a] Ãƒâ€šÃ‚Â· `OPUS-B-12` (done; deliverable: Compose migration post-mortem note) Ãƒâ€šÃ‚Â· gallery full rebuild (done) Ãƒâ€šÃ‚Â· `OPUS-C-09` Ãƒâ€šÃ‚Â· external review sign-off Ãƒâ€šÃ‚Â· release APK build

`OPUS-E-06` landed 2026-04-18 (`b41128a`): the three `OfflineAnswerEngine.generate()`
callsites in `DetailActivity.java` are now routed through a single `AnswerPresenter`
boundary (`DetailActivity.java` 6264 → 6063 lines). Spec:
`notes/specs/answer_presenter_extraction_spec.md`. Reviewer-backend Wave B
(`BACK-U-01` / `U-02` / `U-03`) is un-gated.

> **CHECKPOINT 9 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Release candidate ready**
> - `build_android_ui_state_pack_parallel.ps1` produced a clean `45 / 45` at `artifacts/ui_state_pack_release_candidate_v2/20260418_200902`
> - new external-review gallery is ready at `artifacts/external_review/ui_review_20260418_201903_gallery/index.html`
> - `OPUS-E-05` is paused at a stable partial-completion milestone (`MainActivity.java` 3055, `DetailActivity.java` 6265); presenter-pattern decomposition is deferred
> - `OPUS-E-06` (AnswerPresenter carve-out) landed 2026-04-18 (`b41128a`); reviewer-backend Wave B is un-gated; wider E-05 refactor stays deferred
> - pending reviewer sign-off + release APK build

### Phase D ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Content / guides (parallel fill-lane, continuous)

`OPUS-D-01` Ãƒâ€šÃ‚Â· `OPUS-D-02` Ãƒâ€šÃ‚Â· `OPUS-D-03` Ãƒâ€šÃ‚Â· `OPUS-D-04` Ãƒâ€šÃ‚Â· `OPUS-D-05` Ãƒâ€šÃ‚Â· `OPUS-D-06`

> Runs continuously throughout Phases 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9. Validation goes through existing `wave_XX` micro-pack rhythm. Re-ingest gate is single-consumer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â orchestrator owns.

---

## Quick Start (If You Only Have ...)

- **30 minutes** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ run `OPUS-A-09` (dedupe sub-queries) alone. One-file surgical edit + test.
- **Half a day** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `OPUS-F-01` tokens + fonts + `OPUS-F-02` MetaStrip primitive (parallel workers, 2 disjoint surfaces).
- **A full day** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ land the entire Wave 4 foundation (`F-01` + `F-02` + `F-04` + `F-09` + `F-12` parallel + A-09 + A-10 + E-03).
- **A week** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Phase 4 through Phase 6 with proper checkpoint gating.

---

## In-Flight (do not duplicate)

- **Codex agent ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â prompt-validation / routing waves `de` through `dg`** Ãƒâ€šÃ‚Â· see `APP_ROUTING_HARDENING_TRACKER_20260417.md`
- **UI state pack recovery slices 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“4** Ãƒâ€šÃ‚Â· see `UI_STATE_PACK_RECOVERY_PLAN_20260417.md`; Phase 8 of this file wraps it

---

## Tasks By Lane

### Lane A ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Routing & Answer (query.py, PackRepository, OfflineAnswerEngine)

- `OPUS-A-01` through `OPUS-A-08` Ãƒâ€šÃ‚Â· **all done** (codex 2026-04-17) Ãƒâ€šÃ‚Â· see state log

- `OPUS-A-09` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Dedupe domain sub-queries
  - Files: `query.py:6528 decompose_query`
  - Behavior: after domain-keyword splitting, drop sub-queries with ÃƒÂ¢Ã¢â‚¬Â°Ã‚Â¥0.80 lexical overlap to existing ones
  - Accept: unit test on "water and sanitation" produces ÃƒÂ¢Ã¢â‚¬Â°Ã‚Â¤ 2 sub-queries, not 4

- `OPUS-A-10` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Bridge-guide ingest-time tag + uniform demotion
  - Files: `ingest.py`, guide front-matter convention, `query.py` reranker
  - Behavior: at ingest, detect bridge-pattern guides; tag as `bridge: true`; reranker applies uniform demotion on acute-complaint queries
  - Accept: reranker no longer has per-branch bridge demotion clauses
  - **Serial note:** rebases onto landed A-04 (same `_metadata_rerank_delta` region)

### Lane B ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Mobile UX (survivors + new Haiku-review items)

- `OPUS-B-01`, `OPUS-B-02`, `OPUS-B-05` Ãƒâ€šÃ‚Â· **done** (codex 2026-04-17)

- `OPUS-B-03` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Contextual follow-up chip candidate generator
  - Files: `DetailActivity.java` (new method `buildContextualFollowupCandidates(anchorId)`) feeding the `F-10` SuggestChip primitive
  - Behavior: pull chip candidates from the current thread's anchor guide's reciprocal-link set, not a static list
  - Accept: instrumentation on detail screenshot shows chips reflect the actual guide graph
  - Depends on: landed `OPUS-A-01` anchor state
  - Feeds: `OPUS-F-10` Try next chips

- `OPUS-B-04` Ãƒâ€šÃ‚Â· **superseded by F-02** Ãƒâ€šÃ‚Â· telemetry tap-affordance folded into MetaStrip. Close with note.

- `OPUS-B-06` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Continue-thread chips on result surfaces
  - Files: `SearchResultAdapter.java`, `res/layout/item_search_result.xml`
  - Behavior: if latest thread is warm (< 30 min old), surface a "continue thread" chip on result rows
  - Accept: instrumentation test covers the chip state
  - **Bundle with F-08** (both edit item_search_result.xml)

- `OPUS-B-07` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Phone-landscape composer focus
  - Files: new `res/layout-land/activity_detail.xml` (created by this task or B-10)
  - Behavior: thread suggestions never cover the composer action in landscape
  - Accept: visual check on emulator `5560` shows no overlap
  - **Bundle with B-10** (same file)

- `OPUS-B-08` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Split provenance + source-graph a11y regions
  - Files: `res/layout/activity_detail.xml`, `DetailActivity.java`; **rebase onto F-06 for tablet** (`TabletDetailScreen.kt` panes)
  - Behavior: each region has its own landmark, content description, traversal order
  - Accept: TalkBack walk-through reads provenance and source-graph as distinct regions on both phone and tablet
  - **Ordering:** runs in Phase 7 after F-06 lands

- `OPUS-B-09` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Copy sanitizer for warning-residual traces
  - Files: shared copy-scrub helper used by result, detail, helper surfaces
  - Behavior: strip stale warning artifacts (`[Instructional Mandate]`, etc.) in both UI render and prompt post-normalizer
  - Accept: regex test covers current-known residues; helper is called everywhere

- `OPUS-B-10` Ãƒâ€šÃ‚Â· **P2 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Phone-landscape `res/layout-land/` overrides (Rev 03 targets)
  - Files: `res/layout-land/activity_detail.xml`, `res/layout-land/activity_main.xml`
  - Behavior: phone-landscape posture gets intentional Rev-03 compact+inline layout (not a stretched portrait)
  - Accept: phone landscape (5560) posture looks intentional in gallery re-shoot
  - **Bundle with B-07**

- `OPUS-B-11` Ãƒâ€šÃ‚Â· **superseded by F-07 + F-12 + F-14** Ãƒâ€šÃ‚Â· home visual hierarchy folded into Rev 03 home. Close with note.

- `OPUS-B-12` Ãƒâ€šÃ‚Â· **P2 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· scout (was worker spike)** Ãƒâ€šÃ‚Â· Compose migration post-mortem
  - Now a doc-only scout deliverable: `notes/specs/compose_migration_postmortem.md`
  - Since F-lane commits the app to Compose primitives inside Activity shells, B-12 documents what we learned ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â performance cost, deferred-binding gotchas, remaining hybrid surfaces, path to full Compose if the team wants it.
  - Schedule: Phase 9

- `OPUS-B-13` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Text-overflow handling pass (Rev 03 primitives)
  - Files: Compose primitives (`MetaStrip`, `PaperAnswerCard`, `SearchResult`, `ThreadRow`, `PivotRow`) + `res/layout/` children
  - Policy per surface: titles ellipsize end at 2 lines; step bodies never truncate (wrap); telemetry marquee-or-ellipsize-end never mid-word; chips single-line ellipsize end
  - Accept: long-title prompt pack (GD-108) passes screenshot-diff across four postures
  - **Phase 8**

- `OPUS-B-14` Ãƒâ€šÃ‚Â· **P2 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Drop `ROLE_` prefix in tag rendering
  - Files: tag renderer (Compose `Chip` primitive), possibly `values/strings.xml`
  - Behavior: strip `ROLE_` prefix at render time; underlying metadata keys unchanged; uppercase display form
  - Accept: grep repo for `ROLE_` display strings ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â zero remain at render layer
  - **Phase 8**

- `OPUS-B-15` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Contrast audit on Rev 03 palette
  - Files: `values/colors.xml`, severity card / MetaStrip tone tokens
  - Behavior: measure WCAG AA (4.5:1 body, 3:1 large) on every text-on-bg pairing in Rev 03 palette; adjust tones that fail
  - Accept: unit test `ContrastAuditTest.kt` asserts ratio per pairing; screenshot-diff unchanged on severity cards
  - **Phase 8**
  - **Bundle with B-08 if both active in Phase 8 wave**

- `OPUS-B-16` · **P1 · S · worker** · Phone-landscape nav side-rail
  - Files: `BottomTabBar.kt`, `MainActivity.java`, `PromptHarnessSmokeTest.java`
  - Behavior: when `smallestScreenWidthDp < 600` and the phone is in landscape, mount the primary nav as a left rail instead of a full-width bottom bar
  - Accept: `emulator-5560` re-shoot shows the side rail on real Senku app screenshots, not launcher frames
  - **Phase 9 release polish**

### Lane C ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Testing & Validation (survivors)

- `OPUS-C-03`, `OPUS-C-04`, `OPUS-C-05` Ãƒâ€šÃ‚Â· **done** (codex 2026-04-17)

- `OPUS-C-01` Ãƒâ€šÃ‚Â· **P0 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker (test-infra)** Ãƒâ€šÃ‚Â· Fix test-helper posture truth
  - Matches UI state pack recovery slice 1
  - Files: `PromptHarnessSmokeTest.java` and related helpers
  - Behavior: accept compact / inline / rail variants; `assertGeneratedTrustSpineSettled` becomes posture-aware
  - Accept: each posture fails ONLY on real UI regressions
  - **Phase 8**

- `OPUS-C-02` Ãƒâ€šÃ‚Â· **P0 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker (test-infra)** Ãƒâ€šÃ‚Â· Seeded two-turn detail state for follow-up screenshots
  - Matches UI state pack recovery slice 3b
  - Behavior: follow-up thread screenshot is produced from seeded state, not live generation
  - Accept: follow-up screenshot reproducible byte-for-byte across runs
  - **Phase 8**

- `OPUS-C-06` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker (test-infra)** Ãƒâ€šÃ‚Â· E2B vs E4B same-pack diff lane
  - Deliverable: `scripts/run_e2b_e4b_diff.ps1`; one baseline run lands in `artifacts/bench/e2b_e4b_diff_<timestamp>/`
  - **Phase 8**

- `OPUS-C-07` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker (test-infra)** Ãƒâ€šÃ‚Â· Assert screenshot/dump presence in `summary.json`
  - Matches UI state pack recovery slice 5 trust gap
  - Behavior: `summary.json` generator fails the run if any `pass` state has no copied screenshot+dump
  - **Phase 8**

- `OPUS-C-08` Ãƒâ€šÃ‚Â· **P2 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker (test-infra)** Ãƒâ€šÃ‚Â· SQLite FTS5 diagnostic capture
  - Files: one-off `scripts/android_fts5_probe.ps1`
  - Deliverable: `notes/ANDROID_SQLITE_FTS5_STATUS_<date>.md`
  - **Phase 8**

- `OPUS-C-09` · **P2 · S · worker (test-infra)** · Harness install-state cache should invalidate on emulator cold boot
  - Files: `scripts/run_android_instrumented_ui_smoke.ps1`, `scripts/build_android_ui_state_pack.ps1`, harness cache under `artifacts/harness_state/`
  - Behavior: do not reuse cached install state across read-only emulator cold boots; force reinstall or invalidate the per-device cache when the disposable session resets
  - Accept: no false `skip_install=true` path can survive a cold boot and wedge a posture lane with zero artifacts
  - Evidence trail: failing release-candidate v1 tablet retest at `artifacts/tablet_portrait_retest_20260418_1956`; healthy forced-install confirmation at `artifacts/tablet_portrait_diag_20260418_2001`
  - **Post-release follow-up**

### Lane D ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Content / Guides (continuous fill-lane)

- `OPUS-D-01` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Crisis-gestalt interpreter gate ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â `wave_de`
- `OPUS-D-02` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· `wave_cr` overdose side-fix
- `OPUS-D-03` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· `wave_cv` surgical abdomen side-fix
- `OPUS-D-04` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Symptom-first medical expansion ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â see `GUIDE_PLAN.md`
- `OPUS-D-05` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Household chemical early-triage ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â see `GUIDE_PLAN.md`
- `OPUS-D-06` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Re-check schoolhouse / accessibility `GD-190` dominance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â see `GUIDE_PLAN.md`

### Lane E ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Infra / Code hygiene

- `OPUS-E-01`, `OPUS-E-02`, `OPUS-E-04` Ãƒâ€šÃ‚Â· **done** (codex 2026-04-17)

- `OPUS-E-03` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Verify `VectorStore` unmap in `PackRepository.close()`
  - Files: `PackRepository.java`, `VectorStore.java`
  - Behavior: explicitly null the `MappedByteBuffer` and force unmap on `close()`; unit test covers the path
  - Accept: `push_mobile_pack_to_android.ps1` hot-swap succeeds without `Activity.finish()`
  - **Phase 4** (safe-parallel with F-lane; backend-only)

- `OPUS-E-05` Ãƒâ€šÃ‚Â· **P2 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker (test-infra)** Ãƒâ€šÃ‚Â· Factor presentation logic out of monolithic Activities
  - Files: `MainActivity.java` (currently 3055 lines), `DetailActivity.java` (currently 6265 lines)
  - Behavior: mechanical extraction is complete enough for release prep; deeper presenter-pattern / controller decomposition is deferred unless we reopen this lane
  - Accept: current extracted-helper milestone remains smoke-green and release-safe; any future reopen resumes from 3055 / 6265 rather than blocking the current release candidate
  - **Strictly serial when reopened. Phase 9 / deferred follow-on.**

### Lane F ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Rev 03 UI migration (new)

Direction doc: [`notes/specs/ui_direction_rev03.md`](./notes/specs/ui_direction_rev03.md) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â read before starting any F-task.

- `OPUS-F-01` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Design tokens + font resources
  - Files: `res/values/colors.xml`, `res/values/dimens.xml`, `res/values/styles.xml`, new `res/font/` entries for Inter Tight, Source Serif 4, JetBrains Mono, `android-app/app/src/main/java/com/senku/ui/theme/SenkuColors.kt`, `SenkuTypography.kt`
  - Behavior: declare every token from `ui_direction_rev03.md` Ãƒâ€šÃ‚Â§Color Tokens and Ãƒâ€šÃ‚Â§Type System; wire into Compose theme
  - Accept: theme preview renders every primitive with correct colors/fonts; grep shows no remaining hardcoded hex in Rev 03 primitives
  - **No conflicts ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â pure addition**

- `OPUS-F-02` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· MetaStrip Compose primitive (spec)
  - Spec: [`notes/specs/metastrip_primitive_spec.md`](./notes/specs/metastrip_primitive_spec.md)
  - Files: `android-app/app/src/main/java/com/senku/ui/primitives/MetaStrip.kt` + tests; migrate telemetry sites (DetailActivity, MainActivity)
  - Accept: no pill wall remains in Rev 03 surfaces; TalkBack reads strip as single landmark
  - **Bundle strings.xml vocabulary edit with F-02** (new `meta_*` keys, deprecate stamp keys)

- `OPUS-F-03` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· L Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Paper AnswerCard Compose primitive (spec)
  - Spec: [`notes/specs/answercard_paper_spec.md`](./notes/specs/answercard_paper_spec.md)
  - Files: `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt` + `AnswerContent.kt` + tests; wire into DetailActivity via ComposeView
  - Accept: gallery 04 and 06 re-shoot match Rev 03 reference; abstain mode renders in danger-tinted paper
  - **Must-bundle with F-13** on activity_detail.xml

- `OPUS-F-04` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Slim TopBar primitive
  - Files: `android-app/app/src/main/java/com/senku/ui/primitives/TopBar.kt` + test; replace `include layout="toolbar_pill_row"` sites (DetailActivity header)
  - Accept: 34dp icon buttons for Back/Home/Pin/Share, ID mono subtitle visible on narrow phone

- `OPUS-F-05` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Bottom tabs phone navigation
  - Files: `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`, plumbing in `MainActivity.java` to switch fragments/screens based on tab
  - Behavior: 5 tabs (Home, Search, Ask, Threads, Pins); accent for active, ink-3 for inactive; back stack per tab
  - Accept: tabs work on phone portrait + landscape; back gesture respects per-tab stack
  - **Must-bundle with F-07** (both edit MainActivity.java)

- `OPUS-F-06` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· L Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Tablet 3-pane reader (spec)
  - Spec: [`notes/specs/tablet_three_pane_spec.md`](./notes/specs/tablet_three_pane_spec.md)
  - Files: `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`, new `res/layout-large-land/activity_detail.xml`, new `res/layout-large/activity_detail.xml`, DetailActivity mounts Compose tree when large+landscape
  - Accept: gallery 10 re-shoot shows three panes with no wasted whitespace; evidence pane shows live anchor snippet
  - **Depends on F-01, F-02, F-03, F-09, F-11**

- `OPUS-F-07` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· S Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Identity strip (phone + tablet home)
  - Files: `android-app/app/src/main/java/com/senku/ui/home/IdentityStrip.kt`, bind in `MainActivity.java`
  - Accept: slim 36dp avatar + "Senku Ãƒâ€šÃ‚Â· 754 guides Ãƒâ€šÃ‚Â· manual ed. 2" + Ready pill; no chunky card

- `OPUS-F-08` Ãƒâ€šÃ‚Â· **P1 Ãƒâ€šÃ‚Â· M Ãƒâ€šÃ‚Â· worker** Ãƒâ€šÃ‚Â· Search result paper cards
  - Files: `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`, `res/layout/item_search

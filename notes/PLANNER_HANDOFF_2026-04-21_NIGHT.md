# Planner Handoff — R-tool2 land, anchor refactor drafted, OpenCode workflow archived (night 2026-04-21)

Written by the outgoing CLI Claude Code planner (Opus 4.7 1M context)
for the incoming planner. This session picked up after the LATE handoff
with R-ret1b just-landed (Commit 2 not yet committed but verified) and
closed with R-tool2 landed under the new in-slice tracker cadence,
R-anchor-refactor1 forward research + slice + revised slice all
written, and the entire OpenCode sidecar workflow + dependent
engineering swarm orchestration layer archived.

- Written: 2026-04-21 night local. Follow-on to
  `PLANNER_HANDOFF_2026-04-21_LATE.md`.
- All prior handoffs' lessons still load-bearing. The session-codified
  patterns (scout-audit-before-dispatch, slice alternative
  verification, in-slice tracker cadence) all got real-world workouts
  this session.

---

## Who you are

Tate's **planner** for the Senku project. Design slices, brief Codex,
diagnose failures from artifacts, keep the queue honest, push back
with evidence when warranted, **verify your own evidence first**. Read
`notes/SUBAGENT_WORKFLOW.md` and the prior handoffs for the contract.

## Who Tate is (delta from prior handoffs)

Prior handoffs' read still applies. Three sharper observations from
this session:

1. **Tate values planner-side scope discipline.** When I drafted a
   slice that named the wrong runner script for detail_followup, the
   scout caught it and Tate explicitly approved the GO-WITH-EDITS
   path. The pattern: when you mis-cite a file, fix it precisely
   in-slice (not via a "in retrospect..." apology). Surgical Edit
   calls per finding, no rewrite.

2. **"Don't touch trackers" was a contract, not a preference.** When
   Tate asked whether to fold tracker updates into each landing slice
   OR have me handle tracker upkeep, his framing made it clear the
   "I (planner) don't edit trackers" contract is structural — protects
   the multi-writer churn risk. The accepted answer was Option 1
   (in-slice tracker updates as part of each landing slice's scope),
   which preserves the contract while reducing dispatch overhead.

3. **Tate batches cleanup work decisively when scope is clear.** When
   the OpenCode archive batch surfaced a 6-script cascade, my response
   flagged it for Tate's call. Tate replied "dont use any of those,
   batch archive, then rewrite, handoff for yourself" — single-
   sentence directive, no hedging. Pattern: when the user names
   "all of X" the scope is "all of X." Don't hold back.

## The project in one paragraph

Unchanged. Senku is an offline field-manual survival-guide Android
app with a deprecated desktop Python RAG backend and a mobile LiteRT
Gemma runtime (E2B floor / E4B quality tier). Validation runs on a
fixed four-emulator posture matrix: 5556 phone portrait, 5560 phone
landscape (on-device E4B); 5554 tablet portrait, 5558 tablet
landscape (host-inference under the documented scope cut at
`notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`). CP9 closed 2026-04-20
with RC v5. Retrieval chain (R-ret1c / R-cls2 / R-anchor1 / R-gal1)
closed 2026-04-20 night. Harness-observability tail (R-host /
R-search) closed 2026-04-21 day. Ask-engine observability layer
(R-telemetry) closed 2026-04-21 late. Pack corpus-vocab revision
(R-ret1b) and state-pack logcat tooling (R-tool2) closed 2026-04-21
night. **Next direction is R-anchor-refactor1** — slice drafted,
scout-audited HOLD, revised, ready for re-scout or direct dispatch.

## Current state (as of 2026-04-21 night)

### Landed this session

Chronological. Android unit suite 438/438 at HEAD after R-tool2.

| Slice | Commit | Notes |
| --- | --- | --- |
| **R-ret1b Commit 2 (pack regen)** | `6f9e07b` | Pack assets regenerated. Emergency_shelter chunks 65 → 193 across 5 guides at chunk-level (4 guides at guide-level: GD-345, GD-618, GD-446, GD-294). Planner approved commit-as-is despite chunk-total exceeding the slice's `[95, 160]` STOP gate, because guide-level outcome was exact, over-match guard clean, and overshoot explained by known-design effects (GD-618 whole-guide flip via "seasonal shelter" marker + GD-027 partial chunk-level coverage anticipated in Commit 1 body). Planner conservatism in the original arithmetic was the miss, not the regen behavior. |
| **D7 tracker reconciliation** | `1d2b315` | Folded R-ret1b chain into Completed log; rotated D6 + both R-ret1b slice files to `completed/`. Last D-slice under the "every landing gets a D-slice" cadence — see new cadence rule below. |
| **R-tool2 state-pack logcat** | `38b7826` | `scripts/build_android_ui_state_pack.ps1` +2/-1 (added `-CaptureLogcat` to smoke-script invocation), `notes/CP9_ACTIVE_QUEUE.md` +22/-2 (in-slice tracker), `notes/dispatch/README.md` +13/-13 (in-slice dispatch index). Validation: emulator-5556 phone_portrait, 10 per-state summaries, all `logcat_path` populated + files exist + status=pass. **First slice under the in-slice-tracker-update cadence.** Scout-audited GO-WITH-EDITS pre-dispatch (detail_followup scope claim was wrong; corrected). |

### In-flight at handoff time

Nothing in flight. R-anchor-refactor1 slice + scout prompt drafted but not dispatched.

### Drafted and ready

- **R-anchor-refactor1 slice** at `notes/dispatch/R-anchor-refactor1_pack_support_breakdown.md`. Architectural refactor of `PackRepository.java` introducing `SupportBreakdown` shared helper and migrating all 10 `supportScore(...)` call sites across 9 boundaries. Deletes both R-ret1c (`:968-972`) and R-anchor1 (`:2966-2977`) compensation branches as no-longer-necessary. Scope: PackRepository.java + 3 test files + 2 tracker files (in-slice cadence). Estimated +250-400 lines / +9 tests. Forward research at `notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_20260421.md` (verdict: partial — the architectural smell is real but lives in PackRepository, not OfflineAnswerEngine; only R-ret1c + R-anchor1 are the same shape; R-gate1 + R-gal1 are different families).
- **R-anchor-refactor1 scout prompt** at `notes/dispatch/R-anchor-refactor1_SCOUT_PROMPT.md`. First scout round returned HOLD with 3 BLOCKINGs + 2 SUGGESTIONs (all valid):
  1. Tenth call site at `:2466-2467` — slice missed routed side. **FIXED** in revision.
  2. `RerankedResult` telemetry derivation was misstated — `baseScore = finalScore - metadataBonus`, not `supportWithMetadata + rerankModeBonus`. **FIXED** in revision.
  3. "Delete both compensation branches and keep vector numbers stable" was contradictory — slice picked Option B (full shared breakdown for vectors), so vector telemetry numbers DO shift. **FIXED** by acknowledging behavior change explicitly.
  4. (SUGGESTION) Test count inconsistent (6 vs 7 vs 8). **FIXED** to 9 (7 chooser/boundary + 1 telemetry + 1 engine regression).
  5. (SUGGESTION) Engine fixture-shift gate weak because most tests inject selectedContext directly. **FIXED** by requiring the new regression to start from raw ranked results.
- Re-scout recommended given Decision 2 reframed the contract; should come back GO or GO-WITH-EDITS this round.

### Post-RC tracked slices after this session

Remaining queued, in rough priority order:

- **R-anchor-refactor1** — drafted, revised post-scout, ready for re-scout or direct dispatch. Highest-leverage next move (architectural; reduces future slice count).
- **Pack-drift investigation** (read-only) — `af58bd12...` SQLite overwrote `f5cb2706...` on at least 5554 between 2026-04-20 17:18 and ~22:10. Not diagnosed. Worth a small read-only investigation slice before next substrate provisioning.
- **R-search wrapper hang observation** — single incident, watch item. File diagnostic slice if recurring.
- **Wave C planning** (post-Wave-B confidence tuning, abstain threshold revisit) — unblocked by R-telemetry. Worth evaluating next substantive direction if Tate wants to pivot from architectural to engine-tuning work.
- **`metadata_validation_report.json` write-path audit** (R-hygiene1 carry-over). Future small hygiene slice.
- **Dead CABIN_HOUSE marker prune** — after R-ret1b landed, the `"shelter site selection & hazard assessment"` marker at `mobile_pack.py:595` is now unreachable (EMERGENCY_SHELTER's `"shelter site"` always wins first-match). Future marker-hygiene slice.
- **Engineering swarm rewrite** (NEW this session) — the entire OpenCode-dependent orchestration layer is archived. If parallelism beyond GPT subagents is ever needed again, would need rewriting on top of the GPT subagent split. Not urgent.

### Tracker drift (not edited per collaboration discipline)

After the next landing slice (R-anchor-refactor1 most likely), the tracker will need:
- `notes/CP9_ACTIVE_QUEUE.md`: R-anchor-refactor1 strike from Post-RC Tracked, R-anchor-refactor1 entry in Completed log. **The slice itself includes these edits per the in-slice cadence.**
- Slice file rotation (R-tool2_state_pack_logcat_capture.md, R-tool2_SCOUT_PROMPT.md, R-anchor-refactor1_pack_support_breakdown.md, R-anchor-refactor1_SCOUT_PROMPT.md, D7_post_r_ret1b_tracker_reconciliation.md) — **batched via D-series, due roughly when 4-5 slice files are pending rotation.** D8 dispatches when this threshold is hit; can wait.

## What to read when you take the seat

1. **This handoff.**
2. **`PLANNER_HANDOFF_2026-04-21_LATE.md`** — immediate predecessor.
3. **`notes/dispatch/R-anchor-refactor1_pack_support_breakdown.md`** — ready-to-dispatch slice (post-scout revision).
4. **`notes/dispatch/R-anchor-refactor1_SCOUT_PROMPT.md`** — scout prompt for re-scout round.
5. **`notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_20260421.md`** — research backing the slice. Note its honest "partial verdict" framing — it explicitly narrowed an overreaching original thesis, which is what made the slice tractable.
6. **`memory/feedback_in_slice_tracker_cadence.md`** — new memory written this session codifying the cadence rule. Applies to every future slice draft.
7. **`memory/feedback_scout_audit_before_dispatch.md`** — existing memory; trust+verify pattern got real-world workout this session (scout BLOCKINGs were all real and load-bearing).
8. **`archive/README.md`** — explains what got archived (27 scripts + 6 notes); includes restore instructions.
9. **`notes/SUBAGENT_WORKFLOW.md`** — authoritative now that OpenCode sidecar workflow is archived.

## What I got wrong (and what to learn from each)

Five things this session. Each informs future sessions.

**1. Stale OpenCode/sidecar references in slice drafts.** I echoed AGENTS.md prose without checking whether the workflow was still active. Tate caught it: "how does opencode sidecar still appear, that is way old, we use gpt subagents now, wtf." Fixed in 4 slice references, but the deeper issue was that AGENTS.md itself still had 8 stale sidecar mentions which I then had to clean up. **Lesson: when slicing a recommendation that names a script or workflow, grep for current-vs-deprecated status before the slice text references it. AGENTS.md is not authoritative-by-default; check `git log` on the cited section.**

**2. Original R-anchor-refactor1 slice had three BLOCKING errors caught by scout.** I drafted the slice from research without reading the production code closely. Scout found: (a) tenth call site at `:2466-2467`, (b) `RerankedResult` derivation was different from what I claimed, (c) the "delete compensation, keep numbers stable" thesis was internally contradictory because Option B materially changes vector telemetry. All three errors were preventable by reading the actual source files (research had file:line citations but I didn't open them). **Lesson: even with excellent forward research, read the cited production code yourself before drafting an architectural slice. Research enables — code grounds.**

**3. R-tool2 detail_followup scope claim was wrong.** I named `run_android_detail_followup_logged.ps1` (the `_logged` variant) but the actual `$detailFollowupScript` at `build_android_ui_state_pack.ps1:23` points to `run_android_detail_followup.ps1` (no `_logged`). Scout caught it. The `_logged` variant exists separately and DOES handle logcat — easy to confuse. **Lesson: when a script has a variant with a `_logged` or `_v2` suffix, do not assume the wrapper script calls the variant; grep the wrapper to confirm which script it actually invokes.**

**4. Initial R-tool2 acceptance gate had `>1 KB` file-size threshold as HARD gate.** Scout flagged: `-ClearLogcatBeforeRun` isn't passed, so buffer contents vary by prior device activity; size alone isn't a clean signal. **Lesson: distinguish between "the fix is wired correctly" (logcat_path populated + file exists) and "the fix produces useful output" (file size, content). The first is the slice's responsibility; the second is a validation observation, not a hard gate.**

**5. R-ret1b conservative chunk range `[95, 160]` excluded the actual landing of 193.** My arithmetic computed 152 as the ceiling assuming GD-446+GD-294 chunks fully flip but didn't account for GD-618 whole-guide inheritance via the new `"seasonal shelter"` marker (32 more chunks) or GD-027 partial chunk-level coverage (9 chunks, anticipated in commit body but not factored into the range). **Lesson: when defining a chunk-count STOP gate for a marker-adding slice, enumerate all possible flip mechanisms (per-chunk match, guide-level inheritance via description, partial chunk-level on adjacent guides) and price each one in. Conservative ranges that fire on intended behavior are noisy signals.**

## Patterns that worked

- **In-slice tracker updates landed clean on first try.** R-tool2 was the trial run. CP9_ACTIVE_QUEUE.md (+22/-2) and dispatch/README.md (+13/-13) edits were precisely scoped, no drift. Pattern is now codified in memory and Anti-recommendations of every new slice.

- **Scout-audit-before-dispatch caught architectural-size errors.** R-anchor-refactor1 scout returned HOLD with 3 BLOCKINGs that would have caused real damage if dispatched: missed call site (incorrect migration), wrong telemetry derivation (would silently corrupt), unaccounted vector behavior change (would surprise downstream tests). Cost: ~10 min scout + ~20 min revision. Benefit: caught what self-review missed on a 500+ line slice.

- **Trust-but-verify on scout's BLOCKING calls before applying.** Scout said the tenth call site was at `:2466-2467`. Before editing the slice, I read PackRepository.java:2440-2475 to confirm both lines have `supportScore(...)` calls. Confirmed. Same for the telemetry claim — I read `:5880-5910` to verify the constructor sets `baseScore = finalScore - metadataBonus`. Both scout BLOCKINGs were exactly right; verification was fast and prevented "fixing" something the scout misdiagnosed.

- **Surgical Edit calls for multi-section revisions.** R-anchor-refactor1 revision needed 9 distinct edits across Pre-state, Decisions, Boundary 2.2, Boundary 2.3, Test additions, Engine test, Acceptance, Anti-recommendations, Report format. Targeted Edit calls (each scoped to a small text block) kept the diff comprehensible. Tradeoff: takes longer than Write but each Edit throws if text doesn't match — fail-loud beats silent wrong-edit.

- **Forward research note as scaffolding for slice draft.** `notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_20260421.md` was excellent: 740 lines, every claim cited with file:line, explicit verdict + blockers + scope sizing. Made the slice draft tractable. The author (someone earlier in the session — likely Tate or a separate session) marked the verdict as "partial" instead of overstating — that honesty is what saved scope from being too broad.

- **Decisive batch archive when user gave the directive.** "dont use any of those, batch archive, then rewrite, handoff for yourself" was 13 words. Translation: archive the cascade, rewrite SWARM_INDEX, write this handoff. I executed without re-confirming scope. Pattern: when Tate names a complete batch, execute the batch.

## Anti-patterns to watch for

All prior handoffs' anti-patterns still apply. New or sharpened this session:

- **Drafting from research without re-reading cited code.** (Lesson 2 above.) Most expensive failure mode for architectural slices.
- **Echoing AGENTS.md / SWARM_INDEX prose without grep-checking workflow status.** (Lesson 1 above.) The repo has accumulated several layers of documentation; not all of it is current.
- **Confusing variant-suffixed scripts.** (Lesson 3 above.) `_logged` / `_v2` / `_parallel` suffixes mark distinct scripts that may or may not be the one a wrapper actually invokes.
- **Hard gates on noisy signals.** (Lesson 4 above.) File size, line count, latency thresholds — distinguish "fix is wired" from "fix output is useful."
- **STOP-gate ranges that don't enumerate all flip mechanisms.** (Lesson 5 above.) Specific to classifier/marker-adding slices.
- **Self-reviewing architectural slices.** Scout-audit caught what I missed even after careful self-review. The marginal cost is small; the marginal benefit on slices > 300 lines is large. Memory codifies; trust the codification.

## Tone calibration

Prior handoffs' guidance still applies. Three observations this session:

1. **Tate gives terse directives when scope is clear.** "yes in slice tracker, add that pls" — 8 words to confirm a process change. "anchor research in flight now" — 5 words to update state. "dont use any of those, batch archive, then rewrite, handoff for yourself" — 13 words to direct a multi-step batch. Don't ask for clarification when his directive is internally complete.

2. **Tate notices stale references and calls them out.** The OpenCode/sidecar callout came WITH attitude ("wtf"). Pattern: when the user's frustration is about cleanup-of-cleanup, the underlying issue is that the cleanup wasn't comprehensive enough the first time. Be more aggressive on initial-pass scope.

3. **Tate values structural responses for status updates.** R-tool2 landing report had Outcome / Files Touched / Validation / Notes / Delegation Log sections. R-anchor-refactor1 scout HOLD had Anchor check / BLOCKING / SUGGESTION sections. The Codex-side reports follow this pattern; my responses should match for fast scanning.

## Immediate move on seat-in

1. **R-anchor-refactor1 re-scout (recommended).** Paste `notes/dispatch/R-anchor-refactor1_SCOUT_PROMPT.md` to a Spark session. Expected: GO or GO-WITH-EDITS this round. If HOLD again, take it seriously — the slice is large and architectural, multi-round HOLD means the slice needs structural rework, not surface fixes.

2. **R-anchor-refactor1 dispatch.** Paste `notes/dispatch/R-anchor-refactor1_pack_support_breakdown.md` to a fresh Codex window once scout returns GO. Expected runtime: 30-60 min (architectural refactor + Gradle test cycle + tracker updates). Watch for fixture-shift count on `OfflineAnswerEngineTest` — slice's gate is "at most 5 across all three test files"; more than that signals the refactor has bigger blast radius than expected and Codex should STOP before landing.

3. **After R-anchor-refactor1 lands:** Choose next direction. Post-RC tracked is shorter now. Engine-correctness directions (Wave C tuning) are next-most-valuable architecturally; investigation directions (pack-drift, R-search wrapper-hang recurrence) are evidence-driven; hygiene directions (metadata_validation_report write-path, dead CABIN_HOUSE marker) are cleanup.

4. **D8 timing.** Once R-anchor-refactor1 lands, dispatch root will have ~5 files awaiting rotation:
   - `D7_post_r_ret1b_tracker_reconciliation.md`
   - `R-tool2_state_pack_logcat_capture.md`
   - `R-tool2_SCOUT_PROMPT.md`
   - `R-anchor-refactor1_pack_support_breakdown.md`
   - `R-anchor-refactor1_SCOUT_PROMPT.md`
   That's the threshold I called out in the new cadence rule (every 3-5 landings). Draft D8 then.

## What I don't know and you may want to probe

- **Whether R-anchor-refactor1 actually produces fixture shifts in the predicted range** ([0, 5] across the three test files). If shifts are concentrated in `PackRepositoryTest` (chooser tests) rather than `OfflineAnswerEngineTest` (engine path), that's expected. If shifts are mostly in `OfflineAnswerEngineTest`, the engine-side blast radius is bigger than the slice modeled.

- **Whether `RerankedResult.baseScore` for vector rows actually moves on existing telemetry-test fixtures.** The slice acknowledges they may shift; the question is whether existing tests that assert specific score values were authored against the post-R-ret1c contract or pre-R-ret1c. If they assert pre-R-ret1c values (just `metadataBonus` for vectors), they'll need updates regardless of which boundaries those tests cover.

- **Whether the engine's `resolveAnswerMode(...)` shifts mode for any current Wave B fixture.** R-anchor-refactor1's largest user-visible shift is `buildGuideAnswerContext(...)` admitting vector rows. If a Wave B fixture currently routes one way because a vector row was dropped, post-refactor it might route differently. Worth a focused Wave B re-run after landing if Tate wants on-device confirmation (separate slice scope).

- **Pack-drift origin still unresolved from DAY handoff.** No new evidence this session.

- **Whether ALL OpenCode-dependent code is now archived.** Cascade verification was thorough but not exhaustive — `git ls-files scripts/ | head` may still surface a forgotten one. If a future slice fails because a script depends on an archived OpenCode helper, restore the helper to `scripts/` (don't archive the dependent script).

- **Whether opencode.json itself should be archived.** It has uncommitted changes (per `git status`), so Tate is editing it. The MCP servers it lists are still useful for Codex. Pattern: leave it for now; Tate will signal if he wants it gone.

## MCP state

- **Claude Code (`.mcp.json`):** Serena registered. Usage this session: zero. Worth considering for the R-anchor-refactor1 dispatch run — Serena's symbolic tools would be useful for navigating the large `PackRepository.java` (4500+ lines) during the per-boundary migration.
- **Codex CLI (`~/.codex/config.toml`):** context7 still registered. Not invoked this session.
- **OpenCode / Basic Memory:** OpenCode workflow archived. Basic Memory unchanged.

## Personal note

This session was the first one where the cadence shift (in-slice
tracker updates instead of D-series-per-landing) actually landed in
production, and it worked cleanly on the first try. R-tool2's
`+22/-2` tracker edit was precisely scoped, no drift, no
surprises. The new cadence saves roughly 25% of D-slice dispatch
overhead while preserving every bit of tracker discipline.

The architectural slice (R-anchor-refactor1) was the first one I
drafted under the new cadence + scout-audit pattern. Scout found
three real BLOCKINGs that self-review missed. Cost: ~10 min scout +
~20 min revision. Benefit: refactor that would have shipped wrong
will now ship right. The pattern keeps proving its value — when the
slice is bigger than 200 lines or touches more than one architectural
concern, scout-audit is load-bearing not optional.

The OpenCode archive cleanup was the largest single-session housekeeping
batch in a while: 27 scripts + 6 notes + 1 dedicated doc + multiple
edits to AGENTS.md and SWARM_INDEX.md. The cleanup surfaced a cascade
(dependent orchestration layer) that I initially flagged for Tate's
call; he came back with "dont use any of those" and I executed the
full batch. Lesson: when the user names a complete category for
removal, the cascade is in scope.

One thing for Tate that you may want to reinforce if it comes up:
the cadence rule (in-slice tracker updates) requires every landing
slice draft to include the Step N tracker section. If you draft a
slice without it and Tate doesn't catch it, the next D-slice will
have to absorb the drift. The codification in
`memory/feedback_in_slice_tracker_cadence.md` is meant to make that
invisible — every slice draft should hit it.

Good luck.

— outgoing CLI Claude (Opus 4.7, 1M context). 2026-04-21 night.

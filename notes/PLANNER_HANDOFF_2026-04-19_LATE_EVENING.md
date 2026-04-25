# Planner Handoff — Senku CP9 (continuation, late evening)

Written by the outgoing CLI Claude Code planner (Opus 4.7 1M context)
for the incoming planner. Read this once, then pick up the seat.

- Written: 2026-04-19, late evening local. Follow-on to the
  earlier handoff at
  `notes/PLANNER_HANDOFF_2026-04-19_EVENING.md` (which is still
  worth reading for the Tate-collaboration patterns and the
  three "what I got wrong" entries from the prior planner).
- Open state: CP9 Stage 0 closed GREEN. S1 + S1.1 reparity GREEN
  (build-determinism fixed). S2 returned RED, T1 root-caused.
  Three remediation slices in flight: R-cls and R-eng landed,
  R-pack still running. D3 / RP1 / S2-rerun drafted and ready.
  S3 (closure) sits behind S2-rerun.

---

## Who you are

You are Tate's **planner** for the Senku project. Same as the
prior handoff: design slices, brief Codex, diagnose failures from
artifacts, keep the trackers and queue honest, push back with
evidence when warranted but verify your own evidence first.

You do not delegate delegation. Hand main agent an outcome +
boundaries + acceptance. See `notes/SUBAGENT_WORKFLOW.md` and the
prior handoff for the contract.

## Who Tate is (delta from prior handoff)

Most of the prior handoff's read on Tate still applies — Windows,
direct, terse, deadlines as rhetorical levers, asks `right?` as
Socratic check.

Two refinements from this session:

1. **He's offloading sequencing memory entirely to planner now.**
   Late this session he said "as long as you can keep context of
   what runs when because i sure don't know." That's a signal to
   maintain the dispatch-order cheat-sheet at the top of
   `notes/CP9_ACTIVE_QUEUE.md` and update it every time something
   lands or moves. He's not going to track sequencing in his
   head; that's now your job.

2. **He confirmed how subagent dispatch actually fires.** When
   handing a slice to Codex he says "use subagents where stated"
   alongside the slice path. That session-level grant is what
   activates the imperative parallel directives in slice text.
   Without the grant, Codex defaults to main inline even for
   bolded "Dispatch parallel scouts" instructions. Updated memory
   at `feedback_slice_prompts_subagent_tags.md` reflects this.
   T1 (3 parallel Spark scouts) and R-eng (1 Zeno explorer scout)
   both fired subagent dispatch this session after the grant.

## The project in one paragraph

Senku is an offline field-manual survival-guide Android app with
a desktop Python RAG backend and a mobile LiteRT Gemma runtime
(E2B floor / E4B quality tier). Validation runs on a fixed
four-emulator posture matrix: 5556 phone portrait, 5560 phone
landscape (on-device E4B); 5554 tablet portrait, 5558 tablet
landscape (host-inference under documented scope cut at
`notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`). Wave B
(confident / uncertain_fit / abstain + safety-critical escalation)
was code-complete entering this session, but Stage 2's broader
contract failed (8/20) revealing real engine + classifier defects.
Three remediations are landing now. RC v3 is still the current cut
target; once R-pack lands and RP1 + S2-rerun pass GREEN, the cut
becomes RC v4.

## Current state (as of 2026-04-19 ~late late evening)

### Landed this session

| Slice | Commit | Notes |
| --- | --- | --- |
| S1 | none (artifact only at `cp9_stage1_20260419_181929/`) | Returned with `apk_sha_homogeneous: false` flag — build-determinism |
| S1.1 reparity | none (artifact only at `cp9_stage1_reparity_20260419_183440/`) | Brought 5556/5554/5558 to current `88d0041e` APK; matrix homogeneous |
| D1 | none (audit only) | `notes/PRE_RC_DOC_AUDIT_20260419.md` — 3 RC-blocking, 8 RC-cleanup, 2 post-RC findings |
| D2 | `66567f7` | Resolved 6 D1 findings across 3 doc files; out-of-scope flag on `BACK-U-04` header |
| T1 | none (diagnostic only) | `notes/T1_STAGE2_ROOT_CAUSE_20260419.md` — 3 parallel Spark scouts (Parfit/Peirce/Russell), localized all 3 failures |
| R-cls | `e07d4e7` | Token-aware `QueryMetadataProfile` + `safety_poisoning` branch; 82/82 tests |
| R-eng | `1f76ccf` | `OfflineAnswerEngine` mode-gate hardening (3 fixes); 47/47 tests |
| D3 | `f203a48` | Pre-RC follow-up doc cleanup; absorbed 4 D1/D2 deferred items + slice rotation to `notes/dispatch/completed/` |

### In flight

- **R-pack** (Codex) — investigating why GD-898 / GD-301 / GD-054
  / GD-602 have 0 retrievable chunk rows in current DBs despite
  `ingest.process_file()` producing chunks. Doing an ingest rebuild
  per Tate's last message. Slice at
  `notes/dispatch/R-pack_poisoning_chunk_rebuild.md` (Step 5/5a
  expanded post-R-cls to also enrich poisoning-guide metadata
  since R-cls's new `safety_poisoning` query bucket leans on
  generic `metadataBonus` / `sectionHeadingBonus`).

### Drafted, ready to dispatch

| Slice | When | Path |
| --- | --- | --- |
| RP1 | After R-pack lands | `notes/dispatch/RP1_apk_rebuild_and_reprovision.md` |
| S2-rerun | After RP1 lands | `notes/dispatch/S2-rerun_stage2_revalidation.md` |

RP1 rebuilds the APK (R-cls + R-eng changed prod Java, so
on-emulator `88d0041e` is stale), pushes APK + R-pack's new pack
to all four serials, verifies install + PACK READY. Artifact-only,
no commit.

S2-rerun re-runs the Wave B suite. Step 1 has the imperative
parallel-fan-out directive; remember Tate's "use subagents where
stated" grant at dispatch.

### Planner-side, after S2-rerun GREEN

- **S3** — CP9 closure + RC cut. Tracker updates + AGENTS.md
  baseline bump (point at S2-rerun's fresh gallery).

## What to read, in order, when you take the seat

1. **`notes/PLANNER_HANDOFF_2026-04-19_EVENING.md`** — the prior
   planner's handoff. Tate-collaboration patterns and the prior
   "what I got wrong" entries are still load-bearing.
2. **`notes/CP9_ACTIVE_QUEUE.md`** — opens with the dispatch-order
   cheat-sheet. Read that first; everything else flows from it.
3. **`notes/dispatch/README.md`** — current/in-flight/landed
   index. Mostly mirrors the queue cheat-sheet.
4. **`notes/T1_STAGE2_ROOT_CAUSE_20260419.md`** — full root-cause
   doc for the Stage 2 RED. Five remediation recommendations;
   planner consolidated to three slices (R-eng absorbed Codex's
   R5 since both touch the same gate surface).
5. **`notes/PRE_RC_DOC_AUDIT_20260419.md`** — D1 audit output.
   D2 resolved the S3-consume-blockers; D3 absorbs four more.
6. **The R-pack slice** — read the latest version at
   `notes/dispatch/R-pack_poisoning_chunk_rebuild.md` because
   Step 5/5a were expanded post-R-cls.
7. **Memory files** at
   `C:\Users\tateb\.claude\projects\C--Users-tateb-Documents-senku-local-testing-bundle-20260410\memory\`.
   `feedback_slice_prompts_subagent_tags.md` was updated this
   session with the session-level subagent authorization finding.
8. **Optional, only if a slice fails:**
   `artifacts/cp9_stage2_20260419_185102/summary.md` for the S2
   RED evidence trail; `artifacts/cp9_stage1_reparity_20260419_183440/pack_build.json`
   for the canonical RC v3 substrate; the failing logcats at
   `validation_5556/emulator-5556/<failing_prompt>/logcat.txt`.

## What I got wrong (and what to learn from each)

Three specific mistakes this session, worth reading carefully:

**1. Clean-working-tree acceptance criterion when the worktree is
already 1138 entries dirty.** I wrote D2's acceptance as "git
status shows clean working tree (no unrelated edits)." The
worktree was already 1138 entries dirty (per the AGENTS.md note
about checked-in POSIX `venv/`, plus accumulated work). Codex
correctly couldn't satisfy the literal acceptance and flagged it.
Lesson: when worktree is globally dirty, write acceptance as "no
unintended edits to <named target files>" or "git diff for the
named files is committed, no other files modified by this slice."
Don't assume global state is clean.

**2. Bare relative path in D1's slice text.** I cited the tablet
host-fallback scope note as `apk_deploy_v6/scope_note_tablet_host_fallback.md`
in D1's Scout A assignment. The actual path was
`artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_tablet_host_fallback.md`.
The audit ran fine (Codex grepped to find the file), but the
audit then correctly flagged my slice's broken reference as an
RC-blocking finding. Lesson: write paths from repo root, always.
Bare-relative looks tidy in prose but it breaks executors.

**3. Inherited assumption that Stage 0 GREEN held the apk_sha
hard gate.** The prior planner's Stage 0 verdict said "apk_sha
matches across all four serials" — but A1b's harness fix triggered
a build that pushed a new APK to 5560 only, silently breaking the
gate. S1 ran on the broken-gate state, returned with the asymmetry
flagged (Codex caught it correctly). Lesson: any code change that
triggers a single-serial rebuild during a stage should re-run a
four-serial APK parity check before claiming the stage still
GREEN. Saved as queue Completed-log entry.

Also worth honoring from the prior handoff: don't tag for
delegation when the work is sequential, verify alternatives before
declaring a slice dead, propagate reclassifications to the gate
that enforces them.

## Patterns that worked

- **Pre-drafting dependent slices while predecessor in flight.**
  D3 + RP1 + S2-rerun all written while R-pack is still running.
  Zero lag when R-pack lands.
- **Bundling remediations across same surface.** Codex's T1
  proposed 5 remediation slices; I absorbed R5 (rework confidence
  scoring on finalized context) into R-eng since both touch the
  same gate. Three slices instead of five, no functional loss.
- **Verify-before-asserting on Codex's claims.** Verified D2's
  `query.py:61 ENABLE_ANCHOR_PRIOR = True`, the scope-note path,
  the S1 commit shape. Each verification took seconds and caught
  real things or confirmed Codex's discipline.
- **Dispatch-order cheat-sheet at top of queue.** Tate is
  off-loading sequencing memory; the cheat-sheet block at the top
  of `notes/CP9_ACTIVE_QUEUE.md` is the single source of truth.
  Update it every time anything moves.
- **Imperative parallel directives + Tate's session-level grant.**
  T1 (3 Spark scouts) and R-eng (1 Zeno scout) both fired
  subagents this session. The pattern works when both layers are
  in place: slice text uses imperative `**Dispatch N parallel
  workers**` framing AND Tate hands "use subagents where stated"
  at dispatch.

## Anti-patterns to watch for

- **Acceptance criteria that depend on global state when global
  state is unreliable.** See What I got wrong #1.
- **Bare relative paths in slice text.** See What I got wrong #2.
  Always write from repo root.
- **Blindly trusting wrapper-style validation tooling.** S2's
  wrapper said 20/20 when actual was 8/20 — the wrapper only
  checks for detail-body capture and follow-up input, not the
  rendered Wave B mode contract. The same wrapper is in the
  S2-rerun slice; that slice's Step 3 explicitly scores against
  the actual contract, but planner should expect the wrapper-vs-
  actual gap to surface again. Filed as post-RC carry-over.
- **Letting strict-gate-met override broader-contract-failed
  judgment.** S2's strict slice gate (zero safety-critical
  escalation failures) was met because the escalation line still
  fired correctly, but the broader Wave B contract failed (12 of
  20 prompts misrouted). Codex marked it RED on judgment; planner
  upheld and held S3. The strict gate is one input to the verdict,
  not the verdict itself.
- **Silently absorbing scope drift mid-slice.** When you spot
  drift outside the slice's scope, flag it (out-of-scope finding)
  and let planner triage — don't quietly fix. Codex did this well
  for `BACK-U-04` (D2) and the poisoning-guide metadata weakness
  (R-cls). Both became real planning inputs.

## Tone calibration

Same as the prior handoff. Add: when Tate says "I sure don't
know" about sequencing, that's not him being passive — it's him
asking you to own that piece of state. Make it crisp in the queue
and don't make him think about it again.

## Immediate move on seat-in

1. **Check R-pack's state first.** Look for new commits since
   `1f76ccf` (R-eng) — `git log --oneline 1f76ccf..HEAD`. Look
   for new pack artifact dirs at
   `artifacts/mobile_pack/senku_*_r-pack*/` or similar (R-pack
   was free to choose its naming; check Codex's report or the
   queue Completed log if it landed).
   - **R-pack returned GREEN** → log it in the queue Completed
     section, update the dispatch cheat-sheet, hand Tate the
     RP1 slice path. RP1's preconditions check that R-pack
     landed, so dispatching is straightforward.
   - **R-pack returned RED or with surprises** → triage from
     the artifacts before recommending anything. Don't leap to a
     re-dispatch. The R-pack slice's Step 2 enumerated chokepoints
     in priority order; cross-reference Codex's report against
     those.
   - **R-pack still in flight** → D3 already landed at `f203a48`,
     so there's no parallel doc-cleanup work left. Use idle time
     to read what you haven't (especially T1's full root-cause
     doc and the R-pack slice in its current Step 5/5a-expanded
     state).
2. **If both in flight,** use idle time to read what you haven't
   in "What to read, in order" above.
3. **If Tate is mid-conversation,** pick up where this handoff
   leaves off. The dispatch cheat-sheet plus this section should
   give you enough to talk substantively without re-asking
   questions he's already answered.

## What I don't know and you may want to probe

- **R-pack's chunk-zero root cause.** Codex was running an
  ingest rebuild as of Tate's last message. Could be stale state
  (cleanest), an ingest filter bug (medium), a schema mismatch
  (deeper). Codex's report will name the chokepoint.
- **Whether `promptContextLimitFor()`'s 2-item default for
  ambiguous queries actually surfaces in S2-rerun.** R-eng
  deliberately left it untouched per minimal-scope discipline.
  If S2-rerun shows ambiguous-query weakness, that's the
  follow-up slice. Filed in Carry-over.
- **Whether R-eng's post-generation low-coverage downgrade
  produces acceptable UX.** It still burns ~25s of model time on
  no-evidence queries before the surface gets corrected. Tolerable
  for RC v4 cut, candidate for a post-RC pre-generation gate
  optimization.
- **Whether S2-rerun's wrapper will report 20/20 again** even if
  the actual contract is now better. Per Step 3 it scores against
  actual; if you see wrapper 20/20 + actual <20/20, hold S3 again.
  The wrapper-fix is a post-RC tooling slice (not yet drafted).
- **Whether D2's `BACK-U-04` finding is actually closable in D3.**
  D3 prescribes marking `BACK-U-04` `[done <date>]` matching the
  convention used for `BACK-U-01/02/03`. If the file's state log
  has clearer landing info, D3 will use it; otherwise the date
  defaults to `2026-04-18` (abstain baseline bundle date) per
  the slice. Verify before merging.

## Personal note

This was a productive session — Stage 2's broader-contract failure
turned a "validate and ship" sequence into "validate, diagnose,
remediate, revalidate, ship." That's actually good news: T1
caught the violin-bridge hallucination + child-poisoning
substring bug + cabin_house misroute that would otherwise have
shipped silently. The escalation gate would have masked them in
production telemetry.

Codex's three remediation slices were well-scoped and well-
executed. R-cls's `safety_poisoning` bucket design (consumed
generically by the engine without a predeclared enum) is the
kind of integration choice worth keeping in mind for future
classifier additions. R-eng's dual-input gate (selected context
AND raw tops, with anchor alignment as the abstain-veto qualifier)
is conservative in exactly the right places.

Keep the dispatch cheat-sheet honest. When R-pack lands, the
chain becomes mechanical: RP1 → S2-rerun → S3 → cut. The hard
work of this session was diagnosis. The hard work of the next
session is making sure the cut is clean.

Good luck.

— outgoing CLI Claude (Opus 4.7, 1M context)

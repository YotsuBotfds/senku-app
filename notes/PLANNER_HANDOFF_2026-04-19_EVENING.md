# Planner Handoff — Senku CP9 (continuation)

Written by the outgoing CLI Claude Code planner (Opus 4.7 1M context)
for the incoming planner. Read this once, then pick up the seat.

- Written: 2026-04-19, late evening local. (Follow-on to the prior
  handoff authored by a Cowork Opus session earlier the same day.)
- Open state: CP9 Stage 0 landed GREEN earlier this session via A1b.
  Stage 1 (S1) is in flight at time of writing, with the hard-gate
  bug fixed. S2 drafted ahead. S3 is planner-side.

---

## Who you are

You are Tate's **planner** for the Senku project. Not the executor —
that is Codex, dispatched per-slice from fresh CLI sessions. You do
not write production code. You:

- Design slices and brief main agent.
- Diagnose failures from artifacts (logcat, ui_dump, summary.json).
- Keep the trackers and queue honest.
- Push back with evidence when the executor (or Tate) takes a
  shortcut that will cost later — but verify your own evidence
  before pushing back hard (see "What I got wrong" below).

You do not delegate delegation. You hand main agent an outcome +
boundaries + acceptance. Main agent picks whether the sub-step goes
to Spark xhigh scout, `gpt-5.4 high` worker, or stays inline. See
`notes/SUBAGENT_WORKFLOW.md` — it is authoritative. Read it before
writing any dispatch.

## Who Tate is (and how you two work)

Tate runs fast. Direct, punch-list-friendly, allergic to empty
hedging. Treats deadlines as rhetorical levers to sharpen thinking —
don't take "I have to go in an hour" literally; take it as "give me
your best read right now, no padding." Asks hard questions
constructively. When Tate says "can you inquire about X" or "can
you scan for Y," he wants real scanning, not reassurance.

He's on Windows. Paths in his messages often look like
`C:/Users/tateb/Documents/senku_local_testing_bundle_20260410/...`.
Your repo-relative paths work fine.

Push back when you disagree, but **verify first**. Don't capitulate
when he's frustrated; diagnose instead. He respects "here's what the
evidence says" more than "you're right, sorry." Also: he pushes back
on your framing sometimes with "right?" as a Socratic check — that is
an invitation to sharpen your own thinking, not a request to rubber-
stamp his. Respond with affirmation + nuance, not pure agreement.

Warm tone. No emojis unless he uses them first. No bullet-point
everything — prose for analysis, bullets only for enumerable punch
lists. Saved feedback memory on this.

## The project in one paragraph

Senku is an offline field-manual survival-guide Android app with a
desktop Python RAG backend (`query.py`, `ingest.py`, ChromaDB) and a
mobile LiteRT Gemma runtime (E2B practical floor, E4B quality tier).
Validation runs on a fixed four-emulator posture matrix: `5556` phone
portrait, `5560` phone landscape, `5554` tablet portrait, `5558`
tablet landscape. Wave B (confident / uncertain_fit / abstain +
safety-critical escalation) is code-complete and as of this session's
work has been verified live on all four emulator postures. The
critical path now runs through Stage 1 (RC v3 packet rebuild,
in flight) → Stage 2 (validation sweep, drafted) → Stage 3 (planner-
side CP9 closure + RC cut).

## Current state (as of 2026-04-19 ~late evening)

Stage 0 closed **GREEN** with two documented scope cuts:

| Serial | Model source | Status | Note |
| ------ | ------------ | ------ | ---- |
| 5556 | on-device E4B | pass | clean uncertain_fit + escalation |
| 5560 | on-device E4B | **pass** | A1b `pressBack()` harness fix |
| 5554 | host-inference | pass | tablet AVD partition squeeze |
| 5558 | host-inference | pass | tablet AVD partition squeeze |

Scope cuts documented: tablet host-inference (tracked as `BACK-P-06`,
fleshed out by P4). Stage 0 verdict at
`artifacts/cp9_stage0_20260419_142539/summary.md`.

Completed this session:
- **P1** (`0204177`) — planner-side tracker cleanup.
- **P2** preflight (artifact only, docs-drift on venv activation
  handled via slice-gate exception + P3's docs note).
- **A1b** (`9cf405c`) — `UiDevice.pressBack()` before
  `dumpWindowHierarchy()` in `PromptHarnessSmokeTest`. Fixed the
  5560 landscape IME-occlusion capture issue, flipped Stage 0 GREEN.
- **P3** (`3154f0a8`) — Windows-venv note in AGENTS.md +
  TESTING_METHODOLOGY.md; rotated P1/P2 into
  `notes/dispatch/completed/`.
- **P4** (`cfd4f1dd`) — fleshed out `BACK-P-06` / `BACK-P-07` into
  actionable post-RC tickets, filed `BACK-T-05` for the
  `assertDetailSettled` visibility blind spot A1b worked around.

In flight:
- **S1** — Stage 1 RC v3 packet rebuild. Slice at
  `notes/dispatch/S1_stage1_rebuild.md`. Codex redispatched after I
  patched the hard gate (see "What I got wrong" #3 below).

Drafted but not dispatched:
- **S2** — Stage 2 RC validation sweep. Slice at
  `notes/dispatch/S2_stage2_validation_sweep.md`. Hard-gated on
  S1 artifacts. Step 1 dispatches 4 parallel `gpt-5.4 high` workers
  (one per serial) for ~4× wall-time compression.

Queued / planner-side:
- **S3** — CP9 closure + RC cut. Planner executes directly (no
  Codex md). Steps: tracker update "CP9 closed, RC v3 cut," link
  artifact trail, bump `AGENTS.md` baseline to reference the new
  gallery produced by S2.

Cancelled:
- **P5** (landscape-phone scope note draft) — not needed since A1b
  closed 5560 GREEN. Slice file retained for record.

Superseded:
- **A1_retry_5560_landscape.md** — prescribed ESC-dismiss was wrong
  (ESC doesn't close Gboard on modern AVDs). A1b's device-level
  BACK worked. Slice file retained for record.

## What to read, in order, when you take the seat

1. **`AGENTS.md`** — now carries the Windows-venv note at line ~16.
   The broader repo landing page.
2. **`notes/SUBAGENT_WORKFLOW.md`** — three-tier contract. Main
   agent owns delegation.
3. **`notes/CP9_ACTIVE_QUEUE.md`** — living queue. Active (S1) /
   On-deck (empty) / Queued (S2, S3) / Blocked / Cancelled /
   Carry-over / Completed log. Edit freely.
4. **`notes/dispatch/README.md`** — active slices index.
5. **`notes/dispatch/S1_stage1_rebuild.md`** — in flight. Check
   precondition 2's docs-drift exception.
6. **`notes/dispatch/S2_stage2_validation_sweep.md`** — drafted.
   Step 1 is parallel fan-out across four serials.
7. **`notes/REVIEWER_BACKEND_TRACKER_20260418.md`** — now has
   HEAD banner (P1), `BACK-P-05/06/07` stubs + `BACK-T-05` row
   (P4), and `BACK-U-04` pointer to the actual runner at
   `scripts/run_abstain_regression_panel.ps1` (P1).
8. **Memory files** at
   `C:\Users\tateb\.claude\projects\C--Users-tateb-Documents-senku-local-testing-bundle-20260410\memory\`
   — index in `MEMORY.md`. Three feedback memories I added this
   session:
   - `feedback_slice_alternatives.md` — verify which alternatives
     in a slice were tried before concluding it's dead.
   - `feedback_slice_prompts_subagent_tags.md` — tag only for
     parallelism; imperative framing.
   - Plus pre-existing `feedback_collaboration.md`, `user_tate.md`,
     `project_senku.md`, `reference_emulator_matrix.md`.
9. The Stage 0 evidence trail if you want to double-check anything:
   `artifacts/cp9_stage0_20260419_142539/summary.md` and the
   per-serial `smoke_emulator-5560_v6b/.../dumps/` directory where
   the passing canonical artifact lives.

## What I got wrong (and what to learn from each)

Three specific mistakes this session, worth reading carefully so you
don't repeat them:

**1. Over-pushed on outgoing Opus's 5560 diagnosis without verifying
all slice alternatives.** The outgoing handoff said the 5560 failure
was "harness capture artifact, not engine failure" and prescribed
IME dismissal "via `adb shell input keyevent 111` (ESC) **or**
`input keyevent 4` (BACK) before `uiautomator dump`." The 17:19
retry tried only ESC; I saw the 0-byte dump result and concluded the
prescribed fix was dead. I pushed back hard on the diagnosis and
recommended scope-cut paths. Codex then tried BACK — it worked, A1b
landed GREEN. Outgoing Opus was right in shape; the ESC-vs-BACK
detail was nuance I missed. Lesson saved at
`memory/feedback_slice_alternatives.md`: enumerate every alternative
the slice offers before asserting it's dead.

**2. Tagged steps with parenthetical subagent suggestions expecting
Codex to honor them.** My P3/P4/A1b slices used the pattern
`*(Spark xhigh — read-only audit)*` on sub-steps. Codex ignored
these, running main inline with the reason "this turn did not
explicitly authorize subagent delegation." Tate then pointed out
that in his per-session dispatch workflow, the historical reason for
delegation (context isolation) is moot — within a single Codex
session, subagent calls are sequential, so the only real benefit is
actual parallelism. Convention updated in
`memory/feedback_slice_prompts_subagent_tags.md`: tag only for
parallelism, use imperative framing ("dispatch N parallel workers,
one per X"), not parenthetical suggestion.

**3. Reclassified a preflight blocker in queue prose but didn't
propagate the exception to the slice's hard gate.** P2's preflight
returned `ready_for_build: false` because of a POSIX/Windows venv
path-convention mismatch. I wrote a queue-level note saying "planner
re-classified as docs/env drift, not data drift — Stage 1 build
surface is ready" but didn't update S1's Precondition 2, which still
required `ready_for_build == true`. Codex correctly refused to run
S1 at the gate. Fix: narrow exception added to Precondition 2 that
accepts `ready_for_build: false` iff the blocker list is exactly the
one docs-drift entry and catalog/ingest stats are clean. Lesson:
when you reclassify something in the queue, propagate the exception
to the gate that enforces it, or the gate becomes a lie.

## Patterns that worked

- **Pre-draft dependent slices while predecessors are in flight.**
  S2 was drafted during S1's run so there's zero lag on S1 landing.
  Cheap insurance.
- **Grep before asserting.** P1's surprise — `BACK-U-04` runner is
  real at `scripts/run_abstain_regression_panel.ps1` — caught by
  main agent running the grep instead of accepting the outgoing
  Opus's downgrade hypothesis. Applied it to the ID audit in P4.
- **Update the queue as things move.** The Completed log is the
  durable record; the Active/On-deck sections are the living state.
  Both have to stay current, otherwise a future seat will read the
  wrong thing.
- **Verify artifacts yourself when the diagnosis hinges on them.**
  For 5560, reading the actual byte counts and comparing sha256 of
  ui_dump.xml vs prompt_detail.xml was load-bearing. Don't
  rubberstamp a report.

## Anti-patterns you should watch for

- **Pushing back on a prior diagnosis without verifying all the
  prescribed fix alternatives.** See What I got wrong #1.
- **Tagging subagent delegation on sequential work.** See What I
  got wrong #2. Parenthetical hints are ignored.
- **Reclassifying in prose without updating the enforcing gate.**
  See What I got wrong #3.
- **Silently closing Stage N when scope cuts were made.** Name
  the cuts in the acceptance banner explicitly. Tablet
  host-inference is named in the Stage 0 summary; it should be
  named in Stage 1 and Stage 2 banners too.
- **Treating the queue's Active section as authoritative without
  cross-checking `notes/dispatch/README.md`.** They should stay in
  sync; when they diverge, the README has been more aggressively
  trimmed (user/linter maintains it too).

## Tone calibration

- Open with the diagnosis or the next move, not with recap. Tate
  already knows the context.
- Prose for analysis; a table when it genuinely beats a paragraph;
  a punch list only when items are actually parallel and
  enumerable.
- "Worth naming," "the right next move," "fair point on both
  fronts" — lead-ins that fit his style.
- Don't over-apologize when revising a prior read. "Re-reading the
  evidence, the earlier frame undersells X" is better than "sorry,
  I was wrong." But do acknowledge when you overshot — "worth
  eating" or "my bad — the queue reclassification didn't propagate
  to the gate" is honest and fast.
- End with the concrete next action or decision you're pushing to
  Tate, not with a summary of what you just said.
- Terse is his preference. If an answer can land in 80 words, use
  80 words. Only go long for substantive diagnosis.

## Immediate move on seat-in

1. Check S1's state. If Codex has returned with a result, process
   it:
   - S1 GREEN → promote S2 from Queued to Active in
     `CP9_ACTIVE_QUEUE.md`, hand Tate
     `notes/dispatch/S2_stage2_validation_sweep.md` for dispatch.
   - S1 RED → triage from the reported failure mode. Don't leap to
     another resume dispatch — diagnose first from artifacts.
2. If S1 is still in flight, sit with it. P3 and P4 are done. No
   other slices need to dispatch until S1 returns. Use the idle time
   to read anything in "What to read" you haven't yet.
3. If Tate is mid-conversation when you seat in, pick up where the
   prior planner left off (that's why this handoff exists). Don't
   re-ask questions he's already answered — read back through the
   conversation and the queue Completed log.

## What I don't know and you may want to probe

- **`run_android_ui_validation_pack.ps1`'s actual default Wave B
  prompt suite.** I specified it at the interface level for S2 but
  didn't read the script. If S2 fails for coverage reasons, start
  there.
- **Whether all 4 emulator serials will actually run validation
  packs concurrently without state conflicts.** S2 step 1 assumes
  emulators are independent (they should be — different AVD
  instances), but I didn't verify by running.
- **Whether `build_android_ui_state_pack_parallel.ps1` still
  works against the post-S1 pack sha.** It did against 20260417's
  pack. Could regress if schema changed (unlikely).
- **The shape of `test_prompts.txt`'s 24 sections and which ones
  are Wave B coverage.** `AGENTS.md` says 171 prompts across 24
  sections but doesn't enumerate. If you need to curate a Wave B
  suite by hand, start with `scripts/run_abstain_regression_panel.ps1`
  and the test_prompts.txt itself.

## Personal note

The work is close to the RC cut. Stage 0 took seven iterations
(v1 through v6b) because each iteration surfaced something real —
APK provenance, model-after-clean-install, tablet partition sizing,
landscape IME occlusion, and finally the ESC-vs-BACK distinction.
That's the kind of hardening you don't get from the code alone.

Keep the gated discipline. Don't let urgency collapse it. And when
Tate pushes back on your framing with "right?" — that's usually the
most productive prompt in the conversation, not the least. Take it
seriously.

Good luck.

— outgoing CLI Claude (Opus 4.7, 1M context)
# Planner Handoff — Senku CP9 closure (evening 2026-04-20)

Written by the outgoing CLI Claude Code planner (Opus 4.7 1M
context) for the incoming planner. **CP9 is CLOSED. RC v5 was cut in
commit `0cd86fa`.** There is no in-flight slice as of handoff write
time. This is a fresh-seat pickup into post-RC iteration.

- Written: 2026-04-20, evening local. Follow-on to the midday handoff
  at `notes/PLANNER_HANDOFF_2026-04-20_MIDDAY.md` and the three prior
  2026-04-19 / early-morning handoffs. All prior lessons still load-
  bearing.

---

## Who you are

Tate's **planner** for the Senku project. Design slices, brief
Codex, diagnose failures from artifacts, keep trackers honest, push
back with evidence when warranted, verify your own evidence first.
Read `notes/SUBAGENT_WORKFLOW.md` and the prior handoffs for the
contract.

## Who Tate is (delta from prior handoffs)

Prior handoffs' read still applies. Three new observations from this
session:

1. **Terse single-word answers ("both", "sure", "aye") map to the
   immediate prior question, not the whole message.** When I offered
   "amend commit OR leave as-is" AND "update memory AND write
   handoff", Tate's "both" answered the second pair only. Interpret
   narrowly. When in doubt about scope, ask one clarifying question
   rather than assume permission for a longer list.

2. **He values the "three options with a recommendation" framing.**
   Multiple times this session I gave him option A / B / C with a
   clear recommendation + reasoning, and he picked cleanly without
   extra discussion. Do that when there's genuine ambiguity — it
   respects his time AND gives him real optionality.

3. **He expects "max effort" forward research to be thorough +
   specific + actionable.** When he asks for deep research on
   upcoming slices, he wants real code targets + hidden complexity
   flags + sequencing suggestions, not bullet-pointy summaries. The
   `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md` shape worked
   well; repeat it when appropriate.

## The project in one paragraph

Unchanged. Senku is an offline field-manual survival-guide Android
app with a deprecated desktop Python RAG backend and a mobile LiteRT
Gemma runtime (E2B floor / E4B quality tier). Validation runs on a
fixed four-emulator posture matrix: 5556 phone portrait, 5560 phone
landscape (on-device E4B); 5554 tablet portrait, 5558 tablet
landscape (host-inference under the documented scope cut at
`notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`). **CP9 closed 2026-04-20
with RC v5 cut.** Wave B 20/20 under Option C, state-pack 41/45 with
four R-gal1 carryover limitations.

## Current state (as of 2026-04-20 evening)

### Landed this session

Chronological, most recent first:

| Slice | Commit | Notes |
| --- | --- | --- |
| **S3** | `0cd86fa` | CP9 closure + RC v5 cut. AGENTS.md baseline bumped to `_v6` gallery. Tracker appended with "CP9 RC v5 cut (2026-04-20)" section. 22 dispatch slice files rotated to `completed/`. **Commit body has literal `\n` escape sequences instead of newlines** (PowerShell multiline handling bug, content correct, cosmetic only). Not amended; left at Tate's discretion. |
| S2-rerun4.5-retry-v2 | none (artifact-only) | `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/`. State-pack 41/45. New gallery at `artifacts/external_review/ui_review_20260420_gallery_v6/`. Flake-retry ledger empty — the original 5558 ANR didn't recur after manual emulator recovery. |
| S2-rerun4.5-retry | none (BLOCKED) | Stopped at Step 1 — `start_senku_emulator_matrix.ps1 -RestartRunning -Roles tablet_landscape` killed 5558 and never brought it back. QEMU launched (PID 17552) then died silently. Tate recovered 5558 manually via `Start-Process emulator.exe -ArgumentList "-avd", "Senku_Tablet", "-no-snapshot-load"`. Lesson: don't trust the matrix script's restart lane for single-serial operations. |
| S2-rerun4.5 | none (BLOCKED) | Stopped at 18/45 on `tablet_landscape/deterministicAskNavigatesToDetailScreen` `keyDispatchingTimedOut` ANR. Determined to be host-load flake not real regression (R-val3's re-probe on 5558 passed 45min earlier with identical substrate). |
| **R-val3** | `607ab916` | Single-file androidTest change. Redefined `waitForLandscapeDockedComposerReady` as interactive-ready (visible + enabled) instead of focus-ready, aligning with R-ui2 v3's intentional removal of programmatic landscape composer auto-focus. |
| S2-rerun4 | none (artifact-only) | `artifacts/cp9_stage2_rerun4_20260420_143440/`. Wave B **20/20 actual** under Option C. First time the 5560 evidence gap resolved on all five Wave B prompts. State-pack 40/45 — introduced ONE new regression on `phone_landscape/deterministicAskNavigatesToDetailScreen` (R-ui2 v3 side effect, closed by R-val3). |
| RP4 | none (artifact-only) | `artifacts/cp9_stage1_rcv7_20260420_141257/`. Debug APK `551385c9…`, androidTest APK `1ab24200…` (Gradle non-determinism vs RP3's `b02279159…`, not a real delta per R-ui2 v3 commit diff review). Pack survived reinstall on all four serials. |
| **R-ui2 v3** | `f095194` | Removed `shouldAutoFocusLandscapeComposer` call chain in DetailActivity.java. **Scope-expansion:** also suppressed follow-up suggestion rail on all landscape phone (`renderFollowUpSuggestions()` gate at line 1715-1720, one-line boolean simplification). Load-bearing for manual 5560 PASS. Plus committed three previously-untracked v2 hygiene files (AndroidManifest.xml, both activity_detail.xml layouts). |

### No in-flight work

As of handoff write time, no slice is in flight. The repo has
extensive pre-existing dirty state (untracked markdown + artifacts)
but nothing is actively being worked.

### Post-RC tracked slices (see tracker + queue for details)

Three named post-RC slices with known code targets:

1. **R-ret1** — retrieval tuning. Target: `QueryMetadataProfile.java:1585`
   `STRUCTURE_TYPE_EMERGENCY_SHELTER` marker set (currently
   `"debris hut", "lean-to", "a-frame shelter", "emergency shelter"`,
   no match for the probe "rain shelter from tarp and cord").
2. **R-cls2** — move acute-mental-health markers from
   `OfflineAnswerEngine.java:49-82` to `QueryMetadataProfile.java`,
   preserving the compound-match logic for `pacing` + `slept/sleep`.
3. **R-gal1** — relax `PromptHarnessSmokeTest.java:2794` trust-spine
   assertion to accept `uncertain_fit` terminal state. May
   self-resolve if R-ret1 lands first and rain_shelter routes to
   `confident`.

Forward research with code-target detail and hidden complexity at
`notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`.

## What to read when you take the seat

1. **This handoff** — especially "What I got wrong" below.
2. **`notes/REVIEWER_BACKEND_TRACKER_20260418.md` "CP9 RC v5 cut
   (2026-04-20)" section** — authoritative closure summary.
3. **`notes/CP9_ACTIVE_QUEUE.md`** — post-RC posture with the three
   named slices in a Post-RC Tracked section.
4. **`notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`** — code
   targets and design-option trees for R-ret1 / R-cls2 / R-gal1.
5. **`notes/T4_READY_EVIDENCE_20260420.md`** — DetailActivity focus/
   IME audit I wrote pre-emptively in case R-ui2 v3 landed RED. v3
   landed GREEN so this is now post-RC audit reference material; §7
   has the post-RC audit candidates (MainActivity auto-focus,
   DockedComposer LaunchedEffect, dispatch/README rotation, Serena
   evaluation).
6. **All four prior planner handoffs** —
   `notes/PLANNER_HANDOFF_2026-04-20_MIDDAY.md`,
   `_EARLY_MORNING.md`, and the two 2026-04-19 evening ones. Lessons
   carry forward.
7. **`notes/SUBAGENT_WORKFLOW.md`** — dispatch contract.
8. **Memory files at**
   `C:\Users\tateb\.claude\projects\C--Users-tateb-Documents-senku-local-testing-bundle-20260410\memory\`.
   The `project_senku.md` entry was refreshed this session to reflect
   CP9 closure state.

## What I got wrong (and what to learn from each)

Three specific mistakes this session, worth reading carefully.

**1. Forward research predicted the wrong state-pack tests for R-ui2
v3 side effects.** In `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`
§2 "Risk 1" I flagged `autoFollowUpWithHostInferenceBuildsInlineThreadHistory`
and `answerModeProvenanceOpenRemainsNeutral` as the tests likely to
break from R-ui2 v3's rail suppression. The actual regression was
`deterministicAskNavigatesToDetailScreen`, which I didn't name.
**Lesson: when predicting which tests will break from a UI change,
trace the actual helpers those tests call.** I would've found
`waitForLandscapeDockedComposerReady` if I'd grepped for the
composer/focus test helpers in androidTest. The "watch for new
regressions" language was correct; the specific-test predictions
were guesses.

**2. S2-rerun4.5-retry's Step 1 trusted the matrix restart script
without verifying single-serial semantics.** I baked
`start_senku_emulator_matrix.ps1 -RestartRunning -Roles tablet_landscape`
into the retry slice's Step 1 as a "light touch" emulator restart.
That script used scrcpy-bundled adb and killed 5558 without
bringing it back. Hour-long infrastructure detour to diagnose +
manual recovery. **Lesson: for any defensive infrastructure step in
a slice, the slice should either (a) trust a helper you've
personally verified works in the exact mode you're invoking, or (b)
inline the primitive adb/emulator.exe commands directly so failures
are diagnosable.** Wrapper scripts invoked in non-default modes are
risk multipliers. This is also why S2-rerun4.5-retry-v2's Step 1
explicitly names the SDK platform-tools adb path instead of trusting
whatever PATH picks up.

**3. Accepted R-ui2 v3's scope expansion without re-greping the test
surface.** R-ui2 v3's commit diff included a scope-expansion change
(suppress follow-up suggestion rail on all landscape phone) that
wasn't in the v3 slice. I accepted it because (a) manual 5560 PASS
depended on it and (b) Codex documented the rationale inline. But I
didn't grep `androidTest/` for tests asserting on the rail's
presence. That's what caused the `deterministicAsk` regression — a
two-minute grep would've caught it before RP4, saving the
S2-rerun4 RED → R-val3 chain. **Lesson: when a slice's actual diff
expands scope beyond its brief, grep the test surface for anything
that asserts on the newly-touched behavior before accepting. UI
changes deserve a specific grep over `androidTest/`.** Codex is
usually good about documenting rationale, but test impact is the
planner's job to check.

## Patterns that worked

- **Forward-research docs before drafting slices.** The
  `SLICE_SHAPES_FORWARD_RESEARCH_20260420.md` doc caught multiple
  hidden-complexity flags that shaped the post-RC backlog cleanly.
  Repeat when there are 3+ upcoming slices.
- **`T4_READY_EVIDENCE` pre-empt doc.** Wrote diagnostic evidence
  before R-ui2 v3 even landed, in case v3 needed T4. v3 landed GREEN
  so T4 never dispatched, but the evidence stash fed into the S3
  post-RC audit candidates — no work wasted.
- **Option-A/B/C framing with recommendation.** Used multiple times
  (ship-now vs fix-test vs fold-into-R-gal1; retry vs diagnose vs
  skip-regallery). Tate picked cleanly each time with no wasted
  discussion.
- **ANR flake-retry policy as explicit slice clause.** S2-rerun4.5-
  retry's Step 3 policy (assertion = real stop; ANR = one bounded
  re-probe then stop) is the right shape for this kind of test
  flakiness. Carry forward to future state-pack slices.
- **Doc-only S3 cut with scope-strict acceptance.** Explicitly
  warned against staging pre-existing dirty state; Codex listened;
  commit is clean. Repeat for future doc-closure slices.

## Anti-patterns to watch for

All prior handoff anti-patterns still apply. New this session:

- **Matrix-script lanes invoked in single-serial mode.** Worked
  cleanly in the general `-Mode cold_boot` / full-matrix case but
  broke on `-RestartRunning -Roles <one>`. If the helper you need
  doesn't advertise a matching mode explicitly tested, inline the
  primitive adb/emulator.exe commands.
- **Scrcpy-bundled adb leaking into CI paths.** The
  `start_senku_emulator_matrix.ps1` output showed it was using
  `...scrcpy-win64-v3.3.4\adb.exe` instead of the SDK's
  platform-tools adb. Protocol differences caused device-list
  incompleteness. When writing slices that touch emulator state,
  name `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`
  explicitly.
- **Predicting specific test breakage without tracing helpers.**
  Name the test class if you must, but say "grep the androidTest
  tree for FooBarHelper callers" rather than "these specific tests
  will break". The latter is a prediction that invites you to stop
  thinking; the former is an instruction that keeps the check alive.
- **Accepting Codex scope expansions on UI changes without a test
  grep.** Anytime a slice's diff changes a UI element's visibility
  or focus behavior, add "grep `androidTest/` for that element's
  assertion before accepting the commit" to your personal checklist.

## Tone calibration

Prior handoffs' guidance still applies. Three new observations:

1. **Tate's terse confirmations map narrowly.** When he says "both"
   after you've offered two distinct option pairs, assume he means
   the more recent pair unless explicit. If uncertain, ask.
2. **When he says "max effort" on research, give him real depth.**
   Full code-target specificity, hidden complexity flags, sequencing
   tradeoffs. Don't bullet-point skim.
3. **Flag cosmetic commit issues without pressuring for amendment.**
   System prompt bars amending without explicit user request. Flag
   the issue + content-correctness + offer to amend. Don't do it
   unprompted.

## Immediate move on seat-in

1. **Check commit state.** `git log -1 --format=%B 0cd86fa` — if the
   body still has literal `\n` sequences AND Tate hasn't asked you
   to fix it, leave it. Don't amend unprompted.
2. **Check if RC v5 has been pushed.** Current working tree has no
   `origin/master` — the commit is local-only. Tate may push when he's
   ready; don't do it for him.
3. **Read the three post-RC code targets in the forward research doc**
   so you're ready if Tate says "draft R-ret1" (likeliest next move)
   or picks one of the other two. Shape guidance for each slice is
   already there.
4. **If Tate hasn't asked for a slice yet**, pick up conversation
   wherever it is. Ask what he wants to do next rather than draft
   proactively — post-RC is a discretionary window, not an
   automatic queue.

## What I don't know and you may want to probe

- **Whether R-ui2 v3's follow-up rail suppression has a longer-term
  UX tradeoff that needs product review.** Currently hidden on all
  landscape phone. Arguably safer-UX (user reads first, types
  second), but someone may want the suggestions back eventually.
  Not urgent.
- **Whether R-ret1's marker expansion is sufficient by itself.**
  Forward research §4 flagged a possible metadata-bonus weight
  follow-up. Adding markers may not be enough to overtake
  GD-727 Batteries' vector similarity for rain_shelter. Plan for a
  possible R-ret1b weight tuning if the marker expansion doesn't
  close the gap alone.
- **Whether R-gal1 self-resolves after R-ret1.** If rain_shelter
  starts routing `confident` with proper trust-spine content, R-gal1
  may close without code. Worth running a bounded state-pack
  re-probe on the `generativeAsk` test after R-ret1 lands before
  dispatching R-gal1.
- **Whether the scrcpy-bundled adb in the emulator scripts is a
  systemic PATH issue.** Check where `start_senku_emulator_matrix.ps1`
  resolves adb from. Post-RC audit candidate.
- **Whether `REVIEWER_BACKEND_TRACKER_20260418.md` should eventually
  be renamed.** It was written 2026-04-18, now contains 2026-04-20
  entries. Title/date mismatch. Not urgent.

## MCP state

- **Claude Code (`.mcp.json`):** Serena registered. First-pass
  usage mixed — `get_symbols_overview` beat grep for file API
  inventory; `find_referencing_symbols` lost to grep for
  `this::methodRef` lambda call-site tracing. Worth another
  session or two before the keep/remove call.
- **Codex CLI (`~/.codex/config.toml`):** context7 registered,
  verified enabled. Not actually used in any slice this session
  — none of the landed slices had framework-API-semantic
  uncertainty. Next candidate: if R-ret1 or R-cls2 involves
  Chroma / Compose edge cases, name context7 in the slice brief
  per the dispatch-note MCP hints convention in
  `notes/dispatch/README.md`.
- **OpenCode:** Playwright swap still inert. Tate uses Codex CLI
  daily.
- **Basic Memory:** DO NOT reinstall.

## Personal note

CP9 closed with three mid-session surprises I didn't fully predict:
R-ui2 v3's scope expansion into rail suppression, S2-rerun4's
landscape state-pack regression from that expansion, and the
S2-rerun4.5-retry emulator-script trap. Each resolved cleanly —
the remediation stack is in good shape, post-RC work is well-scoped
with code targets identified, and the forward research doc should
make the next drafts quick.

Two things worth flagging to Tate when you pick up the seat: (1)
The commit `0cd86fa` body has PowerShell-escape-sequence
formatting; content is correct but ugly. He knows. (2) R-ret1 is
the logical next slice — it has the cleanest target and potential
to self-resolve R-gal1.

Good luck.

— outgoing CLI Claude (Opus 4.7, 1M context). 2026-04-20 evening.

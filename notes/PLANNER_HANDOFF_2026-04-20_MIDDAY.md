# Planner Handoff — Senku CP9 (midday 2026-04-20)

Written by the outgoing CLI Claude Code planner (Opus 4.7 1M
context) for the incoming planner. Read this once, then pick up
the seat. R-ui2 v3 is IN FLIGHT as I write this — check for a new
commit and the manual-lane smoke artifacts before doing anything
else.

- Written: 2026-04-20, midday local. Follow-on to the
  early-morning handoff at
  `notes/PLANNER_HANDOFF_2026-04-20_EARLY_MORNING.md` and the
  two prior 2026-04-19 handoffs. The lessons and
  Tate-collaboration patterns in all three remain load-bearing.
- Open state: R-ui2 v3 (landscape-phone composer auto-focus
  removal) is in flight. S2-rerun3 landed RED at 16/20 Option C
  with the 5560 safety-escalation gap as the two RC-blocking
  items. R-ui2 v1/v2 both made progress but neither stuck on
  5560's manual reproduction lane. v3 targets the actual
  root-cause code path in DetailActivity.

---

## Who you are

Tate's **planner** for the Senku project. Design slices, brief
Codex, diagnose failures from artifacts, keep the trackers and
queue honest, push back with evidence when warranted but verify
your own evidence first. Read `notes/SUBAGENT_WORKFLOW.md` and
the prior handoffs for the contract.

## Who Tate is (delta from prior handoffs)

Most of the prior handoffs' read on Tate still applies. Four new
observations from this session:

1. **He uses Codex CLI daily, not OpenCode.** The opencode.json
   MCP config is mostly inert for his day-to-day. Codex CLI
   reads `~/.codex/config.toml`. When recommending MCPs,
   clarify which tool the recommendation targets before
   dispatching setup work. I burned a Playwright swap into
   opencode.json before learning this.

2. **He option-preserves with phrases like "holding on t3 just
   in case".** When he says he's holding on a slice, he's not
   asking you to change scope — he's preserving optionality
   while he checks something else. Don't reflexively rewrite
   the slice.

3. **He flags side effects fast and trusts the diff.** Basic
   Memory silently added frontmatter to 64 markdown files in
   `notes/`. Tate caught the git-dirty state by running
   `/context` and seeing the unexplained modifications. Trust
   him to spot things — and flag intrusive MCP behaviors
   yourself before the second install cycle.

4. **He accepts multi-attempt remediation if each attempt
   produces better information.** R-ui2 went v1 → v2 → v3 this
   session. He didn't push back because each attempt narrowed
   the diagnosis. BUT: don't burn attempts on symptom-layered
   defense. If the first fix doesn't stick, open the source
   for the specific code path before the second.

## The project in one paragraph

Unchanged from prior handoffs. Senku is an offline field-manual
survival-guide Android app with a desktop Python RAG backend
(ChromaDB + LM Studio) and a mobile LiteRT Gemma runtime (E2B
floor / E4B quality tier). Validation runs on a fixed
four-emulator posture matrix: 5556 phone portrait, 5560 phone
landscape (on-device E4B); 5554 tablet portrait, 5558 tablet
landscape (host-inference under documented scope cut at
`notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`). CP9 Wave B started
this morning at 14/20 (S2-rerun2); R-tool1 + R-ui1 landed,
RP3 rebuilt, S2-rerun3 exposed a 5560-specific body-render gap
that is the current RC-blocker. R-ui2 v3 is the fix for that
gap.

## Current state (as of 2026-04-20 midday)

### Landed this session

Chronological, most recent first:

| Slice | Commit | Notes |
| --- | --- | --- |
| S2-rerun3 | none (artifact-only) | RED at `cp9_stage2_rerun3_20260420_101416/`. 16/20 actual Option C. Two RC-blocking failures on 5560: mania + poisoning safety-escalation lines missing from rendered dump. 5560 capture clipping IS resolved (full 1080px height now); body IS rendering momentarily in instrumented lane but IME collapse wipes it in manual reproduction. |
| T3 | none (doc-only) | `notes/T3_5560_BODY_GAP_ROOT_CAUSE_20260420.md`. Diagnosed as landscape IME collapse — body renders, composer gets focused, Android's landscape-phone fullscreen IME takes over, body gets hidden. Pre-dates R-ui1 and R-tool1 per Codex. |
| RP3 | none (artifact-only) | `cp9_stage1_rcv6_20260420_093252/`. Debug APK `66fb10cb…`, instrumentation APK `b02279159…` (both tracked in rollup, unlike prior RPs). 5554 hit a transient System UI ANR that needed dismissing. Chain clean otherwise. |
| R-tool1 | `2ba7d5c` | 5560 capture-dimension fix + state-pack `apk_sha` streamed hashing + parallel finalization `ToArray()` coercion. Flagged the real underlying 5560 landscape collapse exists — previously masked by capture clipping. |
| R-ui1 | `29463eb` | activity_main.xml restructure to make Home scroll as one unit on phone viewports. XML-only. `session_label` noted as layout-only with no Java binding. |

### In flight (as of handoff write time)

- **R-ui2 v3** (Codex) — removes `DetailActivity.java:1094-1096`
  (landscape auto-focus call site) + the unused
  `shouldAutoFocusLandscapeComposer` helper at 1795-1807. Keeps
  v2's manifest + focus-trap additions as hygiene. Slice:
  `notes/dispatch/R-ui2_detail_ime_suppress.md`.
- **MCP1** (Codex, landed before R-ui2 v3 dispatched) —
  context7 registered in `~/.codex/config.toml`. No commit (user-home
  change). `codex mcp get context7` reports it as enabled.

### Drafted, ready to dispatch (pending R-ui2 v3)

- **RP4** — not yet drafted. Will be a trivial RP2/RP3-shape
  derivation once R-ui2 v3 lands clean. APK rebuild (debug +
  androidTest), re-provision, reuse R-pack's pack, artifact
  dir `cp9_stage1_rcv7_<ts>/`. Predecessor APK sha to differ
  from: `66fb10cb…` (RP3).
- **S2-rerun4** — not yet drafted. Wave B revalidation on the
  RP4 substrate. Expected actual 20/20 under Option C (all five
  prompts × four serials; rain_shelter counted as pass-with-
  limitation). Zero safety-critical escalation failures.

### Planner-side, after S2-rerun4 lands

- **S3** — CP9 closure + RC v5 cut. Document rain_shelter
  limitation in AGENTS.md + tracker. Cite RP4's rollup and the
  new gallery (should be `_v5` suffix to avoid clobbering
  `_v4` from S2-rerun3). Baseline bump points at v5.

### Post-RC backlog (DO NOT dispatch pre-cut)

Unchanged from prior handoff:
- `R-ret1` — retrieval tuning to route rain_shelter to
  `confident`.
- `R-cls2` — acute-mental-health explicit classifier signal.
- `R-gal1` — state-pack gallery 41/45 regression
  (`generativeAskWithHostInferenceNavigatesToDetailScreen`).

New post-RC considerations from this session:
- **Evaluate Serena utility** — installed this session but
  usage pattern isn't established yet. After a few sessions of
  real work, judge keep/remove based on whether semantic
  navigation actually beats grep for Senku's codebase.
- **Scan DetailActivity for other intentional UX auto-focus
  patterns** that might have similar IME side effects on
  landscape. v3 removes ONE of them; others may exist. Not
  RC-blocking but worth a post-RC audit.

## What to read, in order, when you take the seat

1. **Both prior handoffs** —
   `notes/PLANNER_HANDOFF_2026-04-20_EARLY_MORNING.md`,
   `notes/PLANNER_HANDOFF_2026-04-19_LATE_EVENING.md`,
   `notes/PLANNER_HANDOFF_2026-04-19_EVENING.md`. Lessons
   carry forward; don't repeat them.
2. **This handoff**, specifically the "What I got wrong"
   section below. The R-ui2 v1/v2/v3 progression is the
   headline learning.
3. **`notes/CP9_ACTIVE_QUEUE.md`** — dispatch-order cheat-sheet
   (may be stale on R-ui2 status — check commit state first).
4. **`notes/dispatch/README.md`** — current/in-flight/landed
   index (may also be stale).
5. **`notes/T3_5560_BODY_GAP_ROOT_CAUSE_20260420.md`** —
   Codex's landscape IME collapse diagnosis. The handoff
   section above summarizes it but the full doc has the
   evidence chain.
6. **R-ui2's slice file** at
   `notes/dispatch/R-ui2_detail_ime_suppress.md` — reflects v3
   scope. Contains the full v1→v2→v3 backstory.
7. **S2-rerun3 summary** at
   `artifacts/cp9_stage2_rerun3_20260420_101416/summary.md`
   — the 16/20 rollup with per-serial breakdowns.
8. **MCP setup state:**
   - Claude Code: `.mcp.json` has Serena registered at project
     scope.
   - Codex CLI: `~/.codex/config.toml` has context7.
   - OpenCode: Playwright swapped in for archived puppeteer
     (largely inert — Tate uses Codex CLI daily).
   - Basic Memory: installed-then-removed this session because
     its sync adds frontmatter to all markdown files. Don't
     re-add without disabling that sync behavior.
9. **MCP research doc** at `notes/MCP_RESEARCH_20260420.md` —
   full per-MCP evaluation. NOTE: the Basic Memory entry's
   "no vendor lock-in" framing is misleading; correct it when
   convenient (it does write frontmatter to files).
10. **Memory files** at
    `C:\Users\tateb\.claude\projects\C--Users-tateb-Documents-senku-local-testing-bundle-20260410\memory\`.

## What I got wrong (and what to learn from each)

Five specific mistakes this session, worth reading carefully.

**1. R-ui2 v1 was a symptom-inferred fix without source
verification.** I recommended the manifest-only
`stateHidden|adjustResize` change after T3 diagnosed the IME
collapse. I didn't open `DetailActivity.java` to look for
explicit focus code — just trusted T3's symptom-level summary
and Android docs memory. Worked on 5556 portrait, failed on 5560
landscape. This is the same "assert from symptom" pattern the
early-morning handoff's "What I got wrong #1" flagged. Repeated
here. **Lesson: when the symptom involves focus/IME/lifecycle,
open the activity source and grep for `requestFocus`,
`showSoftInput`, `OnFocusChangeListener` BEFORE recommending a
declarative fix.** The declarative layer may be overridden by
imperative code you haven't read.

**2. R-ui2 v2 added defensive layers instead of finding the
real code path.** When v1 failed on 5560, I added a focus trap
(`focusableInTouchMode="true"` + `descendantFocusability="beforeDescendants"`)
to both activity_detail layouts. I STILL didn't grep for the
programmatic focus grab in DetailActivity.java. v2's instrumented
smoke passed, which gave a false positive. Codex's manual lane
caught that the failure persisted. **Lesson: if a fix doesn't
stick on the second attempt, stop layering defense and open the
source for the specific execution path. Iteration on defensive
layers without diagnosis compounds the wasted cycles.**

**3. I trusted instrumented smoke as proof of fix.** v2's
instrumented pass looked green (body rendered, IME hidden,
composer focused=true was explained away). Codex caught the
manual-lane failure independently. **Lesson: for UI fixes,
require both instrumented AND manual reproduction smoke before
declaring done. The harness's settle discipline may capture at
a moment that doesn't match real user experience.** R-ui2 v3's
slice file codifies this — don't forget to include "both
lanes" in future UI-smoke acceptance criteria.

**4. Basic Memory MCP installed without understanding its
sync-write behavior.** Research doc said "keeps files as plain
markdown, no vendor lock-in." That's technically true (markdown
parsers still read them) but the MCP writes `title:`, `type:`,
`permalink:` frontmatter to every file it indexes. 64 files in
`notes/` got modified silently before Tate spotted the git-dirty
state. **Lesson: before installing an MCP that points at a
project directory, read its docs for "does it write to files"
behavior. If it does, either disable that (if possible) or
don't install. Research recommendations are not permission to
skip due diligence.**

**5. Platform-agnostic MCP recommendations without checking
which tool Tate uses.** The research doc treated OpenCode and
Codex CLI as parallel targets. I assumed OpenCode was the
primary Codex lane because it's in opencode.json. Tate corrected
me mid-conversation — he uses Codex CLI daily. The Playwright
swap in opencode.json is largely inert for his actual work.
**Lesson: for any tooling recommendation that involves installing
in a specific client (Claude Code, OpenCode, Codex CLI, Claude
Desktop), confirm the client is actually the one the user uses
before dispatching setup work.**

## Patterns that worked

- **Parallel dispatch on different file trees.** R-ui1 and
  R-tool1 landed clean in parallel again. Now the session's
  sixth or seventh successful parallel dispatch. This is the
  cleanest multi-remediation pattern we have.

- **MCP research via subagent.** Delegated the
  wide-surface-area web research to general-purpose agent with
  a Senku-specific brief. Got back a synthesized report without
  polluting main context with raw search results. Kept research
  in `notes/MCP_RESEARCH_20260420.md` for reference.

- **Option C hybrid shipping decision held through one more
  cycle.** rain_shelter stays as `uncertain_fit` documented
  limitation; S2-rerun3 treated it as pass-with-limitation under
  Option C scoring. Expected 20/20 on S2-rerun4 after R-ui2 v3
  lands. Avoided chasing retrieval tuning pre-RC.

- **Pre-drafting dependent slices.** RP4 and S2-rerun4 are
  both next-up; RP4 can be drafted as soon as R-ui2 v3 lands
  clean, and the RP3 slice is the template.

- **Handoff-style "what I got wrong" discipline.** The prior
  planner's handoff had three "what I got wrong" entries, one
  of which I repeated this session. That's uncomfortable but
  useful — the pattern is hard to break. Writing these entries
  forward is the tax for running the seat.

## Anti-patterns to watch for

All the prior handoff's anti-patterns still apply. New this
session:

- **Declarative fix without reading imperative overrides.**
  `windowSoftInputMode="stateHidden"` is declarative. If the
  activity has `requestFocus()`/`showSoftInput()` code paths,
  they can override the declarative intent. Always grep the
  source before recommending a manifest-only or layout-only fix
  for a behavior-level problem.

- **Instrumented-only smoke acceptance.** If a Codex slice's
  smoke uses the instrumentation harness, ALSO require a manual
  shell reproduction on the failing serial (the one that was
  RED in the predecessor run). Instrumented timing may mask
  the user-facing failure.

- **Research recommendations treated as permission to skip
  due diligence.** Just because a reputable source says "no
  lock-in" doesn't mean the tool is free of side effects in
  your specific install. Read the docs before installing tools
  that point at a project directory.

- **Client-specific config changes without confirming client
  usage.** Don't set up MCPs in a config file unless you've
  confirmed the user actually uses that client for daily work.

## Tone calibration

Prior handoffs' guidance still applies. Add three new entries:

1. **When owning a mistake, keep it tight.** Tate doesn't need
   a full self-post-mortem in the middle of a dispatch. One
   line ("I chased symptoms without reading source") is enough;
   move to the next move. If deeper analysis is warranted,
   offer to write a handoff entry.

2. **When a fix iterates past v2, make diagnosis the next
   move, not v3.** If v1 and v2 both missed, the next slice
   should be a diagnostic doc (like T2 or T3), not another
   remediation attempt. I got lucky with v3 by finding the
   actual source — but the pattern that SHOULD have fired at
   v2 was "stop, write diagnosis slice."

3. **"I'll hold on X" from Tate is option-preservation, not a
   scope request.** Don't rewrite the slice when he says that.
   Just acknowledge and continue drafting the next step.

## Immediate move on seat-in

1. **Check R-ui2 v3 state.** `git log --oneline 2ba7d5c..HEAD`
   — look for a commit named `R-ui2: ...`. Also check for
   smoke artifacts under
   `artifacts/smoke/R-ui2_20260420/emulator-5560_*_manual_v3.{xml,png}`
   to see if the manual-lane smoke passed.

   **Landed GREEN (manual 5560 lane passes):** Draft RP4
   (trivial RP3-shape derivation: predecessor shas include
   R-ui2's commit; expected debug APK sha must differ from
   RP3's `66fb10cb…`; reuse R-pack's pack at
   `artifacts/mobile_pack/senku_20260419_213821_r-pack/`;
   record both debug + androidTest APK shas; artifact-only, no
   commit). Hand RP4 to Tate. After RP4 lands, draft S2-rerun4
   (RP4 substrate, Option C scoring, 20/20 target).

   **Landed RED on manual 5560 lane:** STOP. Write a T4
   diagnostic slice — do NOT try a v4 remediation. The v3 fix
   removes the one auto-focus path I could identify; if 5560
   still fails, there's another mechanism I didn't find. Open
   DetailActivity.java + the related widgets under
   `com.senku.mobile/Detail*Formatter.java` and find what else
   touches focus or IME. Follow the T2/T3 diagnostic pattern:
   read logs + source, demand evidence per hypothesis, don't
   hot-fix.

   **In-flight still / partial report:** Give idle time to
   the rest of this handoff's "What to read" list.

2. **If Tate is mid-conversation,** pick up where this handoff
   leaves off. The R-ui2 v3 scope + next-step chain is clear;
   don't re-ask questions.

## What I don't know and you may want to probe

- **Whether R-ui2 v3's surgical removal is actually
  sufficient.** The `shouldAutoFocusLandscapeComposer` path is
  ONE explicit auto-focus. There may be others I didn't find —
  implicit via lifecycle hooks in parent classes, view-tree
  observers registered in helper formatters, etc. If v3 manual
  smoke fails on 5560, expand the search to all files under
  `com.senku.mobile/` that contain `focus` or
  `softInput`.

- **Whether removing landscape auto-focus causes any other
  landscape UX regression.** The `OnFocusChangeListener` at
  line 953-960 also calls `requestLandscapeDockedComposerFocus`
  on user-initiated focus gain — that's still active. Did that
  behavior have a specific reason to exist? Maybe it's fine,
  but watch for "composer loses focus unexpectedly in landscape"
  reports post-R-ui2.

- **Whether Serena actually adds value vs Grep.** Installed
  this session but I didn't actually use it for any real
  planning work. Next session's first cross-boundary question
  (e.g., "who calls DetailAnswerPresentationFormatter") is
  the right test — use Serena, compare against Grep, decide
  keep/remove based on actual utility.

- **Whether RP4's instrumentation APK sha matters.** R-ui2 v3
  doesn't touch androidTest code (the harness is unchanged
  from R-tool1). So the instrumentation-APK sha from RP4 should
  equal RP3's `b02279159…`. Worth confirming as a sanity check
  in the RP4 rollup rather than assuming.

- **Whether S3 closure needs to note the R-ui2 fix as a
  product-visible change.** The landscape-phone UX now requires
  an explicit tap on the composer to start typing (instead of
  auto-focus). For a survival app, that's arguably the SAFER
  UX — user reads first, types second. But flag it in S3's
  release notes so anyone who liked the old behavior
  understands why it changed.

## MCP state notes

- **Claude Code (.mcp.json, project scope):** Serena registered.
  Connected cleanly after first cold-start timeout. Usage
  pattern TBD.
- **Codex CLI (~/.codex/config.toml):** context7 registered.
  Verified working via `codex mcp get context7`.
- **OpenCode (opencode.json):** Playwright swap landed (replaces
  archived puppeteer). Mostly inert for day-to-day since Tate
  uses Codex CLI.
- **Basic Memory: DO NOT reinstall** without first understanding
  its frontmatter-write behavior. This session revert was
  surgical (64 untracked file strips via Python + git checkout
  of 14 tracked files). If you want semantic recall over
  `notes/`, configure Basic Memory to read-only mode or pick a
  different tool.

## Personal note

This session was a mixed bag. R-ui1, R-tool1, RP3 landed clean
and moved the chain forward. T3 was textbook evidence-grounded
diagnosis. R-ui2 v1 → v2 → v3 was the failure: I chased symptoms
through two rounds before opening the source. The prior
planner's "what I got wrong #1" was exactly this pattern, and I
repeated it in a different domain (UI focus instead of engine
emissions).

The remediation stack is sound. If R-ui2 v3 lands clean on the
manual lane, RP4 and S2-rerun4 are mechanical, and S3 closes
the cut. If v3 fails, write T4 — don't v4.

Two things I'd flag for Tate when you pick up the seat:
(1) The "auto-focus composer on landscape" decision was
deliberate UX at some point. If product direction ever wants
faster follow-up typing on landscape, revisit — but do it with
an explicit design that keeps the body visible (e.g., pre-focus
only AFTER user scrolls past the first body paragraph). (2)
Consider a post-RC audit of all auto-focus and auto-IME code
paths — there may be parallel patterns in MainActivity or
SearchActivity that will bite on future orientation-specific
bugs.

Good luck.

— outgoing CLI Claude (Opus 4.7, 1M context)

# Planner Handoff — Senku CP9 (pre-cut, early morning 2026-04-20)

Written by the outgoing CLI Claude Code planner (Opus 4.7 1M context)
for the incoming planner. Read this once, then pick up the seat.

- Written: 2026-04-20, early morning local (very-late evening
  into post-midnight work session). Follow-on to the earlier
  handoffs at
  `notes/PLANNER_HANDOFF_2026-04-19_EVENING.md` and
  `notes/PLANNER_HANDOFF_2026-04-19_LATE_EVENING.md`. Both
  are still worth skimming for the "what I got wrong" entries
  and Tate-collaboration patterns that remain load-bearing.
- Open state: S2-rerun2 returned RED at 14/20 actual Wave B
  contract — substantial progress vs S2-rerun's 7/20 and S2's
  8/20. Two residuals, neither an engine regression. Planner
  chose Option C hybrid: ship rain_shelter as documented
  limitation, fix the state-pack tooling debt now, do the UI
  layout fix now, then cut. R-tool1 and R-ui1 are IN FLIGHT
  (dispatched parallel). After they land: RP3 → S3 (RC v5 cut).

---

## Who you are

You are Tate's **planner** for the Senku project. Design
slices, brief Codex, diagnose failures from artifacts, keep
the trackers and queue honest, push back with evidence when
warranted but verify your own evidence first.

You do not delegate delegation. See
`notes/SUBAGENT_WORKFLOW.md` and the prior handoffs for the
contract.

## Who Tate is (delta from prior handoffs)

Most of the prior handoffs' read on Tate still applies. Three
additions from this session:

1. **"trust your judgment, write up a slice" = decisive
   slice, not a menu.** When Tate says that phrase, he wants
   one crisp path forward. Don't present 3 options — pick
   the right one and write the slice. (I did this on R-ui1
   and it landed fine.) If the judgment is genuinely uncertain,
   present 2-3 options *with an explicit recommendation*
   and let him pick (he picks the recommendation quickly
   — see S2-rerun2's Option C decision).

2. **Physical device testing catches a different category
   of UX issues than the emulator matrix.** Tate caught the
   `activity_main.xml` nested-scroll layout bug on his phone
   that the tall emulator viewports (5556 portrait, 5560
   landscape, 5554/5558 tablet) never surfaced. Lesson:
   matrix covers posture/resolution variation but the
   aspect-ratio compression on real phones can reveal layout
   problems that look fine in emulator postures. Worth
   mentioning to Tate mid-RC if we ever want a
   phone-aspect-ratio AVD added to the matrix (probably
   post-RC).

3. **He uses `/context` freely.** Doesn't need me to spell
   out internal reasoning. Terse responses remain the norm.

## The project in one paragraph

Senku is an offline field-manual survival-guide Android app
with a desktop Python RAG backend and a mobile LiteRT Gemma
runtime (E2B floor / E4B quality tier). Validation runs on a
fixed four-emulator posture matrix: 5556 phone portrait, 5560
phone landscape (on-device E4B); 5554 tablet portrait, 5558
tablet landscape (host-inference under documented scope cut at
`notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`). CP9 Wave B
(confident / uncertain_fit / abstain + safety-critical
escalation) started this session at S2 RED (8/20 actual). Three
root-cause diagnostics (T1 + T2) and five remediation slices
(R-cls, R-eng, R-pack, R-val2, R-eng2) landed clean. Current
actual Wave B contract is 14/20, with the remaining gap being
one over-cautious-but-safe retrieval case (rain_shelter →
uncertain_fit) and a 5560 landscape capture tooling clip. RC
v5 is the active cut target; after R-tool1 and R-ui1 land,
RP3 + S3 close the cut.

## Current state (as of 2026-04-20 ~early morning)

### Landed this session

Chronological, most recent first:

| Slice | Commit | Notes |
| --- | --- | --- |
| S2-rerun2 | none (artifact-only) | RED at `cp9_stage2_rerun2_20260420_070512/`. 14/20 actual, zero safety-escalation failures on 3/4 serials (5560 evidence-gap from clipping, not engine). |
| RP2 | none (artifact-only) | `cp9_stage1_rcv5_20260420_063320/`. Built APK `804119cb...`, differs from RP1 as required. Codex caught + rebuilt missing `com.senku.mobile.test` on 5556. |
| R-eng2 | `8990cc6` | Early-return ABSTAIN for `safety_poisoning` + early-return UNCERTAIN_FIT for acute-mental-health. 50/50 tests. |
| R-val2 | `6665bd8` | Harness settle/capture discipline tightened. Preview body rejected as final; idle fallback fails hard. |
| T2 | `2856ec6` | Split-diagnosis root-cause at `notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md`. Ruled out my prior H1 hypothesis. |
| S2-rerun | none (artifact-only) | RED at `cp9_stage2_rerun_20260419_221343/`. 7/20 actual — worse than S2 on surface, but T2 later showed the drop was the same engine bugs being exposed by a broader corruption fix (R-pack). |
| RP1 | none (artifact-only) | `cp9_stage1_rcv4_20260419_214851/`. Built APK `389d8d0f...`. |
| R-pack | `bd84835` | Full Chroma rebuild (14,784 → 49,654 chunks) + poisoning metadata enrichment. `structure_type = safety_poisoning` aligned exactly with R-cls constant. |
| (D3, R-eng, R-cls, T1, S2, D2, D1, S1.1, S1, P4, P3, P2, P1, A1b, etc.) | various | See the rolling Completed log in `notes/CP9_ACTIVE_QUEUE.md`. |

### In flight

Both dispatched parallel (different file trees, zero overlap,
same pattern as R-cls/R-eng/R-pack and R-val2/R-eng2):

- **R-tool1** (Codex) — state-pack tooling bundle: 5560
  landscape capture clipping, state-pack `apk_sha` reporting
  bug, `build_android_ui_state_pack_parallel.ps1` finalization
  type mismatch. Slice:
  `notes/dispatch/R-tool1_state_pack_tooling_bundle.md`.
- **R-ui1** (Codex) — `activity_main.xml` restructure to make
  all tabs fully scrollable on phone-narrow viewports
  (physical-device UX issue Tate caught). Slice:
  `notes/dispatch/R-ui1_fully_scrollable_activity_main.md`.

### Drafted, ready to dispatch (pending both in-flight)

- **RP3** — not yet drafted. Will be a trivial RP1/RP2-shape
  derivation once R-tool1 + R-ui1 land. APK rebuild, push to
  four serials, reuse R-pack's pack. Artifact-only.

### Planner-side, after RP3 lands

- **S3** — CP9 closure + RC v5 cut. Document rain_shelter
  limitation explicitly in AGENTS.md + tracker. Cite RP3's
  rollup and the fresh gallery (should be `_v4` suffix to
  avoid clobbering `_v3` from S2-rerun2). AGENTS.md baseline
  bump points at the v4 gallery.

### Post-RC backlog (DO NOT dispatch pre-cut)

- `R-ret1` — retrieval tuning to get `confident_rain_shelter`
  to route to `confident` instead of `uncertain_fit`. Off-
  route GD-727 Batteries anchor is the symptom; root is either
  the hybrid ranker giving Batteries chunks too much weight for
  rain-related queries, or the selected-context builder not
  preferring shelter-family guides. Needs retrieval-side
  diagnosis first.
- `R-cls2` — explicit acute-mental-health classifier signal so
  the R-eng2 inline query heuristic can be replaced by a
  proper `preferredStructureType()` entry. R-eng2's
  recommendation, filed when it landed.
- `R-gal1` — state-pack gallery 41/45 regression
  (`generativeAskWithHostInferenceNavigatesToDetailScreen`
  states). R-val2 did NOT close these four failures; they're
  likely on a state-pack-specific assertion path that R-val2
  didn't touch. Worth its own diagnostic before any remediation.

## What to read, in order, when you take the seat

1. **Both prior handoffs** —
   `notes/PLANNER_HANDOFF_2026-04-19_EVENING.md` and
   `notes/PLANNER_HANDOFF_2026-04-19_LATE_EVENING.md`. The
   Tate-collaboration patterns and earlier "what I got wrong"
   entries still apply.
2. **`notes/CP9_ACTIVE_QUEUE.md`** — opens with the updated
   dispatch-order cheat-sheet showing Option C plan. Read
   that first.
3. **`notes/dispatch/README.md`** — current/in-flight/landed
   index.
4. **`notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md`** — Codex's
   evidence-grounded split diagnosis that ruled out my H1
   prior. Read this if you're tempted to guess root causes
   from visible symptoms again.
5. **The two in-flight slices** —
   `notes/dispatch/R-tool1_state_pack_tooling_bundle.md` and
   `notes/dispatch/R-ui1_fully_scrollable_activity_main.md`.
6. **`artifacts/cp9_stage2_rerun2_20260420_070512/summary.md`**
   — latest S2-rerun2 summary with the 14/20 breakdown and
   the two named residuals.
7. **Memory files** at
   `C:\Users\tateb\.claude\projects\C--Users-tateb-Documents-senku-local-testing-bundle-20260410\memory\`.
8. **Optional, only for deeper context:**
   `artifacts/cp9_stage1_rcv5_20260420_063320/pack_build.json`
   (RP2 rollup — substrate under test for S2-rerun2);
   `artifacts/external_review/ui_review_20260420_gallery_v3/`
   (current visual baseline, will be superseded by v4 post-RP3).

## What I got wrong (and what to learn from each)

Three specific mistakes this session, worth reading carefully.

**1. Published a strong-confidence hot hypothesis on T2 before
checking logs.** After S2-rerun returned RED, I wrote a
confident read: "R-eng's post-generation downgrade emits a
surface the DetailActivity render state machine doesn't know
how to transition to, so the generating-preview body sticks."
I based this on the visible surface (DetailActivity.java:3132's
preview body) + knowing R-eng had added a post-generation
downgrade. I didn't verify whether the engine was emitting a
completion at all. Codex's T2 root-caused it as
(A) harness accepts preview-as-settled + (B) safety prompts
route wrong upstream — H1 was ruled out by logs showing no
`ask.generate` or `low_coverage_*` emission in any of the
three failing traces. The visible preview surface was a RESULT
of nothing settling the UI, not a cause. **Lesson: when a
visible symptom points at a consumer, verify the producer
emitted something before blaming the consumer.** Engine
emissions first, render consumer second. The prior handoff's
"verify-before-asserting on Codex's claims" rule applies in
reverse too — verify my own claims before publishing them.
This is the third "what I got wrong" entry in three handoffs
about the same underlying failure mode (asserting without
checking evidence). Take it seriously.

**2. Wrote acceptance criteria on R-tool1 that might be too
optimistic for the 5560 clipping.** I set R-tool1 up as a
single slice covering three sub-issues and said "stop if one
sub-fix is genuinely out-of-scope." But if the 5560 landscape
clipping turns out to be a genuinely nasty Android
WindowManager timing bug, that sub-issue might need its own
deeper diagnostic. Watch for Codex flagging scope-drift on
sub-issue 1 specifically. If Codex says the clipping needs a
separate slice, let it — don't force-bundle.

**3. Didn't notice the state-pack gallery 41/45 plateau
during R-val2 design.** I drafted R-val2 to tighten the
harness settle discipline, but I didn't notice that the
state-pack lane's "settled status should keep final backend
or completion wording when still visible" assertion was a
SEPARATE code path from the Wave B detail assertion. R-val2's
scope was `PromptHarnessSmokeTest.java` as a whole, but the
state-pack captures appear to be on their own call graph that
R-val2's changes didn't reach. Result: the gallery went 45/45
→ 41/45 in S2-rerun and stayed 41/45 in S2-rerun2. R-gal1
is the follow-up. **Lesson: when the slice surface is "the
harness," be explicit about which CODE PATHS within the
harness the fix must touch, not just which file.** Don't
assume that "same file" means "same call graph."

## Patterns that worked

- **Parallel dispatch on different file trees.** R-cls / R-eng
  / R-pack landed within minutes of each other. R-val2 /
  R-eng2 landed within 1 minute. R-tool1 / R-ui1 now in flight
  parallel. This is the cleanest pattern we have for
  multi-remediation slices. Works when file trees don't
  overlap — always verify.

- **Imperative parallel directives + Tate's session-level
  grant** for validation runs. S2-rerun2 Step 1 fired 4
  parallel gpt-5.4 high workers. The pattern works when both
  layers are in place (slice text uses imperative
  `**Dispatch 4 parallel workers**` framing AND Tate includes
  "use subagents where stated" at dispatch).

- **Pre-drafting dependent slices while predecessor in
  flight.** R-ui1 was drafted during R-val2/R-eng2 in flight;
  R-tool1 was drafted during R-ui1 in flight. Zero lag when
  we moved to the next step.

- **Bundling related remediations into one slice when they
  share a surface.** R-tool1 bundles 3 tooling bugs that all
  affect the same state-pack lane. Keeps context together
  and saves Codex a scoping exercise per bug.

- **Accept-with-documented-limitation over one-more-cycle.**
  S2-rerun2's Option C hybrid: ship rain_shelter as `uncertain_fit`
  (safe conservative routing) rather than do another cycle
  with R-ret1. Right call because (a) uncertain_fit with
  evidence is not dangerous, (b) retrieval tuning could
  regress other prompts, (c) the cycle cost (R-ret1 + RP3 +
  S2-rerun3) was high for marginal Wave B coverage gain.

- **Evidence-grounded diagnosis over hypothesis-driven.**
  T2 worked because Codex pulled the actual logcat for each
  failing prompt and read what was there, not what I'd
  predicted. That's why H1 got ruled out cleanly. Future
  diagnostic slices should demand log evidence per hypothesis,
  not just "here's where you should look."

## Anti-patterns to watch for

- **Asserting root cause from visible symptom without log
  evidence.** See What I got wrong #1. The DetailActivity
  preview surface was the *visible* stall — the *cause* was
  the engine never completing. Producer-before-consumer
  applies to diagnosis.

- **"Same file" != "same code path."** See What I got wrong
  #3. R-val2 touched `PromptHarnessSmokeTest.java` but didn't
  reach the state-pack assertion graph inside the same file.
  When scoping a fix by file, name the specific call sites.

- **Accepting tooling debt across runs.** 5560 clipping,
  apk_sha reporting bug, gallery finalization mismatch — all
  three reproduced across S2, S2-rerun, and S2-rerun2 because
  we kept prioritizing engine work and deferring tooling. That
  was the wrong trade — every validation run paid the tooling
  tax. R-tool1 is bundling them now; next time, fix tooling
  the first time it shows up if the fix is cheap enough.

- **Wrapper-as-truth when wrapper is documented-permissive.**
  Both S2-rerun and S2-rerun2 had wrapper 20/20 with actual
  <14/20. Don't believe the wrapper without reading the dumps.
  R-val2 closed most of the gap but not all of it — some
  wrapper-vs-actual delta remains because the wrapper doesn't
  grade expected-vs-observed *mode*, just "settled to some
  final mode." Planner scoring against the Wave B contract is
  still authoritative.

- **Reflexively drafting more remediation when diagnosis
  is the right move.** After S2-rerun RED I nearly went
  straight to "what do we fix," but the right first move
  was T2 to diagnose. Repeated at every RED verdict.

## Tone calibration

Same as prior handoffs. Add two:

1. **When Tate says "trust your judgment, write up a slice,"
   make one concrete call and write the slice.** Don't
   present a menu; don't hedge with "here are three
   variants." Pick, explain in one line why, write the slice.

2. **When presenting judgment calls with options, lead with
   recommendation.** I learned this on S2-rerun2: Option A/B/C
   with my recommendation as C + reasoning → Tate said "yes"
   immediately. He doesn't want a symmetric list of options;
   he wants to know what you'd do and why, with the menu for
   override cases.

## Immediate move on seat-in

1. **Check R-tool1 + R-ui1 state first.** Look for new commits
   since R-eng2 (`8990cc6`) — `git log --oneline 8990cc6..HEAD`.
   Codex names commits `R-tool1: ...` and `R-ui1: ...` so
   they're easy to spot.

   **Both landed GREEN** → Draft RP3 (trivial RP1/RP2-shape
   derivation: predecessor shas include both R-tool1 and
   R-ui1 commits; expected built-APK sha must differ from
   RP2's `804119cb...`; reuse R-pack's pack at
   `artifacts/mobile_pack/senku_20260419_213821_r-pack/`;
   artifact-only, no commit). Hand RP3 to Tate. After RP3
   lands, move to S3.

   **Both returned RED or with surprises** → Triage from
   artifacts before recommending anything. R-tool1 is a
   tooling slice with limited blast radius; if it's RED
   with a real scope surprise (e.g., 5560 clipping is
   deeper than expected), scope a follow-up. R-ui1 is a
   single-file XML restructure; if it's RED, it's either a
   gradle build failure (check `apk_build.log` in the APK
   rebuild output) or an ID-binding crash (check logcat on
   install).

   **One landed GREEN, one RED** → Triage the RED one;
   don't let the GREEN one sit. RP3 still needs both to land
   cleanly before it fires.

   **Both still in flight** → Use idle time to read what you
   haven't in "What to read" above. Especially T2's full
   root-cause doc.

2. **If Tate is mid-conversation,** pick up where this handoff
   leaves off. The Option C plan is fully written down in the
   queue cheat-sheet; you have enough to talk substantively
   without re-asking questions he's already answered.

## What I don't know and you may want to probe

- **Whether R-tool1's 5560 clipping fix holds.** Codex might
  root-cause it as a WindowManager timing issue and add a
  settle, or as a metrics-axis bug and swap axes. Either way
  verify with a fresh 5560 landscape capture before RP3 —
  don't ship a RP3 that validates on a serial with still-
  clipped captures.

- **Whether R-ui1 breaks any tablet posture.** The XML
  restructure is designed for phone-narrow viewports; on
  tablet postures there should be minimal change (just a
  taller scrollable region on Home). If Codex's smoke on
  5554 surfaces a tablet regression, treat as critical —
  don't ship R-ui1 if it breaks tablet layout.

- **Whether the state-pack gallery 41/45 regression gets
  resolved by RP3.** It MIGHT — if R-val2 + R-eng2 + R-tool1
  together reach the state-pack assertion path, the four
  failing `generativeAskWithHostInferenceNavigatesToDetailScreen`
  states could pass again. If they don't, R-gal1 is the
  follow-up. Don't let the gallery state block S3 cut, but
  note the delta explicitly in S3 closure.

- **Whether rain_shelter's `uncertain_fit` ships cleanly or
  needs an explicit scope cut in code.** Current routing is
  engine-correct under the evidence available. But if any
  tooling checks "expected_primary_mode == observed" and
  fails hard on rain_shelter, that check needs a scope
  exception before cut. Check the test harness and any
  regression-panel runners (`scripts/run_abstain_regression_panel.ps1`
  and similar) before S3.

- **Whether R-ret1 (post-RC) should touch the pack or the
  engine.** T2's localization pointed at retrieval quality —
  GD-727 Batteries winning for rain-adjacent queries. That's
  either a ranker weight issue (hybrid retrieval scoring) or
  a selected-context builder issue (not preferring shelter-
  family guides). A pre-R-ret1 diagnostic slice (T3) may be
  warranted to distinguish. Planner choice when the time
  comes.

## Personal note

This session moved the Wave B contract from 8/20 to 14/20
actual — real progress. The three big mental moves were:
(a) trusting Codex's evidence when my hot hypothesis was
wrong (T2); (b) deciding to ship with documented limitations
rather than chase one more cycle (Option C on rain_shelter);
(c) bundling tooling debt into one slice rather than letting
it cost every future validation run (R-tool1).

The remediation stack works. R-cls routes safety classifications
correctly. R-eng's gate hardening killed the violin-bridge
hallucination. R-pack restored 35k missing chunks. R-val2
killed the preview-stall failure mode. R-eng2 routes the two
safety prompts to their expected modes with escalation lines
rendered. The engine is shipping-ready for Wave B.

The chain from here is mechanical: R-tool1 + R-ui1 land →
RP3 rebuild → S3 cut. The hard diagnostic work is done. Your
job is to keep the cheat-sheet honest, scope RP3 correctly,
and handle S3's tracker + AGENTS.md + gallery handoff without
surprises.

Two things I'd flag to Tate when you pick up the seat:
(1) the physical-device UX catch is valuable — worth
considering a phone-aspect AVD in the matrix post-RC so we
don't miss the next one; (2) R-gal1's state-pack gallery
regression is not RC-blocking but will nag until diagnosed,
so file it crisply in the post-RC backlog with a link to
the affected test class.

Good luck.

— outgoing CLI Claude (Opus 4.7, 1M context)

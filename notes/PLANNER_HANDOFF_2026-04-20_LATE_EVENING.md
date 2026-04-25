# Planner Handoff — Senku post-RC retrieval chain (late evening 2026-04-20)

Written by the outgoing CLI Claude Code planner (Opus 4.7 1M context)
for the incoming planner. Post-RC iteration deep in the rain-shelter
retrieval chain. R-ret1c is drafted and awaits dispatch; upstream work
in the session established the diagnostic arc and identified the
actual blocker.

- Written: 2026-04-20, late evening local. Follow-on to
  `notes/PLANNER_HANDOFF_2026-04-20_EVENING.md` (same day, earlier in
  the session). All prior handoffs' lessons still load-bearing.

---

## Who you are

Tate's **planner** for the Senku project. Design slices, brief Codex,
diagnose failures from artifacts, keep the queue honest, push back
with evidence when warranted, verify your own evidence first. Read
`notes/SUBAGENT_WORKFLOW.md` and the prior handoffs for the contract.

## Who Tate is (delta from prior handoffs)

Prior handoffs' read still applies. Two new observations:

1. **Tate pushed back sharply on pacing recommendations.** When I
   offered "draft now / call it for the night / handoff and pick up
   tomorrow" style options, he replied: "stop recommending stopping
   please that is irritating. it stops when it stops and can pick up
   again without saying we stopped, just because it says evening in
   the handoff doesn't mean you know my schedule or anything." Saved
   as `feedback_no_pacing_recommendations.md`. Apply it strictly —
   don't infer user energy/schedule from handoff timestamps, don't
   offer "call it for the night" as an option, don't narrate session
   length in options framing.

2. **Tate values direct pivot when evidence changes the premise.**
   When R-ret1b's pack-regen produced 0-delta (Codex's correct STOP),
   he was fine with a quick "actually the fix is elsewhere — here's
   what I found." No meta about "lost slice" or apology. Treat
   blocked slices as evidence-producing events, not setbacks.

## The project in one paragraph

Unchanged. Senku is an offline field-manual survival-guide Android
app with a deprecated desktop Python RAG backend and a mobile LiteRT
Gemma runtime (E2B floor / E4B quality tier). Validation runs on a
fixed four-emulator posture matrix: 5556 phone portrait, 5560 phone
landscape (on-device E4B); 5554 tablet portrait, 5558 tablet
landscape (host-inference under the documented scope cut at
`notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`). CP9 closed 2026-04-20
with RC v5 (`0cd86fa`). Wave B 20/20 under Option C, state-pack
41/45 with four `generativeAskWithHostInferenceNavigatesToDetailScreen`
R-gal1 carryover limitations. The post-RC iteration is narrowing
down one of those four — the rain-shelter probe — to a specific
scoring-architecture asymmetry.

## Current state (as of late evening 2026-04-20)

### Landed this session

Chronological, most recent first. Android unit suite currently 411/411.

| Slice | Commit | Notes |
| --- | --- | --- |
| **R-gate1** | `f3b2c68` | Decoupled `maybeRerankResultsDetailed` from `isRouteFocused()` gate. Rerank now fires when either `isRouteFocused` OR `preferredStructureType()` is non-empty. +4 unit tests. Exposed JVM-safe timing fallback for `SystemClock.elapsedRealtimeNanos()` path (now testable without Android runtime). Probe on 5556 confirmed rerank fires but GD-345 still stuck at rank 15 — see T5 + R-gate1 artifacts for the evidence leading to R-ret1c scope. |
| **T5** | `9aa46dd` | Retrieval-ranking diagnostic telemetry. Added four new `search.candidates.*` log tags at lexicalHits (`PackRepository.java:524`), vectorHits (`:656`), hybridResults pre-rerank (`:660`), reranked (`:662`). Format: delimited inline rows with rank/guide_id/section/score/structure/category + base/bonus/final for reranked. +4 tests. Critical finding: showed GD-345 IS surviving into prerank at rank 13 but reranked scores all zeros. That led to R-gate1. |
| **R-hygiene1** | `a2ccaf3`, `82c2bf5`, `6557ef6`, `1e9e7e3` | 4-commit android-app untracked-files sweep. `.gitignore` added for build output / local.properties / device runtime state. Tracked: gradle config, source trees, res/, jniLibs (~82 MB), deterministic_predicate_manifest.txt. Resolved 2134 untracked files → 0 under android-app. **Caveat:** Codex hit the Step 2b STOP correctly when my `mobile_pack/` blanket ignore would have shadowed tracked production assets (`senku_mobile.sqlite3`, `senku_vectors.f16`, `senku_manifest.json`). Corrective addendum resolved via surgical-delete of 6 defunct files. Saved as `feedback_gitignore_inventory.md`. |
| **R-ret1b Commit 1** | `961d478` | Symmetric pack-side phrase markers added to `mobile_pack.py:569-577` (rain shelter, rain fly, tarp shelter, tarp ridgeline, ridgeline shelter) + 1 desktop test. Suite went 214 → 215. Commit 2 (pack regen) STOPPED at Step 2b: `emergency_shelter` chunk count stayed 65 → 65. Reason: R-ret1's phrases are query-phrasings that don't appear in guide titles/slugs/tags/descriptions. Commit 1 retained as future-proofing; corpus-vetted alternative at `notes/R-RET1B_CORPUS_VOCAB_20260420.md`. |
| **V-ret1-probe** | (artifact only) | 4-serial bounded re-probe of `generativeAskWithHostInferenceNavigatesToDetailScreen`. All 4 class C (`mode=uncertain_fit` via `low_coverage_route`). Evidence at `artifacts/postrc_vret1_probe_20260420_191223/`. Showed R-ret1's classification works (`structure=emergency_shelter` in search.start) but GD-727 Batteries still dominates `context.selected`. This drove the T5 diagnostic. |
| **R-ret1** | `2eae0cd` | Query-side marker expansion at `QueryMetadataProfile.java:1585`. 7 phrase additions. Also absorbed a SessionMemoryTest.java:759 assertion shift (`rain shelter` → `shelter rain`) as authorized scope expansion — the new ordering is semantically correct because `SessionMemory.addStructureContextTerms` now fires the emergency_shelter branch. Suite went 398 → 403. |

### In-flight / drafted, not dispatched

- **`R-ret1c`** — drafted at `notes/dispatch/R-ret1c_metadata_bonus_reaches_rank_for_vector_rows.md`. Target: `PackRepository.java:956-978` rerank loop. Fix: add `metadataBonus` to `score` for vector rows specifically, so the metadata signal reaches the sort key (non-vector rows already get it via `supportScore`'s internal call at `:3631`). One commit + single-serial re-probe. Expected outcome: GD-345 finalScore goes 0 → ~38, surfaces in reranked top-4.

- **`R-cls2`** — drafted at `notes/dispatch/R-cls2_acute_mental_health_profile_port.md`. Port QUERY-side acute-mental-health markers from `OfflineAnswerEngine.java:65-75` to `QueryMetadataProfile.java` as `STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH`. Option 1b resolved in forward research §6. Independent of the retrieval chain; dispatchable parallel to R-ret1c if desired.

- **R-ret1b follow-up revision** — evidence at `notes/R-RET1B_CORPUS_VOCAB_20260420.md`. 6 corpus-vetted phrases identified (`shelter construction`, `shelter site`, `primitive shelter`, `seasonal shelter`, `temporary shelter`, `cave shelter`) that would widen `emergency_shelter` tag coverage from 2 → 4 guides. Not drafted as a slice yet. Decision can wait on R-ret1c evidence.

### Substrate SHAs (reference)

- RC v5 debug APK: `551385c99a2474e97d8cbd4757d6f65423ec74e9afaeb4e9e9e5d3a3f972a204`
- V-ret1-probe rebuilt APK: `d34f17546e65fd27d125c96fbaba70eb40bd31941024e8c5be98f4d3758302de`
- T5 APK: `4e357a3ece8ba90b01f4c581b90a8c0d1fe55665af7f9e3e3c43182abc79367f`
- R-gate1 APK (current on 5556): `8540e6f1dbf4b16ac03a3644bb7eefa864a06b65c185330cdad5d653b2502d0e`
- androidTest APK: `b260a219294bc20b5f76313aaa7415d895ab8899beee3372b039e251e9841b9e`
- Pack SHA (unchanged): `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`

### Post-RC tracked slices after R-ret1c

From `notes/CP9_ACTIVE_QUEUE.md`:

- **R-anchor1** (not yet drafted) — context-selection `anchorGuide` logic. V-ret1-probe + R-gate1 evidence both showed that even when `search.topResults` has shelter content, `context.selected` anchors on whichever guide has the most `guide-focus` retrievalMode rows (GD-727). Separate blocker downstream of rerank. Candidate only if R-ret1c doesn't fully resolve rain_shelter routing (likely will still need some context-selection fix).
- **R-gal1** — `PromptHarnessSmokeTest.java:2794` trust-spine assertion relaxation. May close via Option A or self-resolve depending on whether rain_shelter routes `confident` post-R-ret1c.
- **R-ret1d** (placeholder) — if R-ret1c's GD-345 delta isn't large enough to beat GD-727 durably, weight tuning on the structure-match constant (`+18` at `QueryMetadataProfile.java:771`). Concrete knobs in forward research §4.
- **Ask-telemetry enrichment** (subsumed) — T5's broader telemetry already covers this. Remove from queue if/when it's clear T5 is sufficient.

## What to read when you take the seat

1. **This handoff** — especially "What I got wrong" below.
2. **`notes/CP9_ACTIVE_QUEUE.md`** — in-flight R-ret1c, tracked slices, current queue posture.
3. **`artifacts/postrc_rgate1_20260420_203947/summary.md`** — the clearest evidence of the current blocker (GD-345 at base=-38, bonus=+38, final=0).
4. **`artifacts/postrc_t5_20260420_201942/summary.md` + `ranking_trace.md`** — full pipeline trace showing where GD-345 enters/drops.
5. **`notes/R-RET1B_CORPUS_VOCAB_20260420.md`** — corpus vocabulary evidence for R-ret1b's revision decision.
6. **`notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`** — still current for R-cls2 (§6 Option 1b resolved) and R-gal1 (§5). §4 R-ret1 section is historical now; R-ret1b/c/d evolved beyond it.
7. **Prior handoffs:** `notes/PLANNER_HANDOFF_2026-04-20_EVENING.md`, `_MIDDAY`, `_EARLY_MORNING`, and the two 2026-04-19 evening ones. Lessons carry forward.
8. **Memory files at `C:\Users\tateb\.claude\projects\C--Users-tateb-Documents-senku-local-testing-bundle-20260410\memory\`** — two new feedback memories from this session (`feedback_gitignore_inventory.md`, `feedback_no_pacing_recommendations.md`). Project state (`project_senku.md`) wasn't touched; still reflects CP9 RC v5 closure as the latest architecture snapshot.

## What I got wrong (and what to learn from each)

Four specific mistakes this session, worth reading carefully.

**1. R-ret1b forward research assumed guide corpus would contain
R-ret1's query phrasings.** It doesn't. Guides use vocabulary like
`debris hut`, `lean-to`, `a-frame`, `shelter construction`,
`primitive shelter`, `shelter site`. The query phrases R-ret1 added
(`rain shelter`, `rain fly`, `tarp shelter`, etc.) match user typing
patterns but don't appear in guide titles/slugs/tags as exact
substrings. Codex's Step 2b STOP caught the 0-delta regen.
**Lesson: for pack-side classifier markers, verify against the
actual corpus vocabulary before authorizing phrase additions.** The
query-side markers can be aspirational (match user input that may not
appear in content verbatim); pack-side markers must match content
that exists. The analogous lesson to the earlier
`feedback_gitignore_inventory` — grep the actual sibling state before
authorizing batch operations.

**2. V-ret1-probe dispatch used `hostInferenceEnabled=false`** but
the test method (`generativeAskWithHostInferenceNavigatesToDetailScreen`)
now short-circuits unless host inference is enabled. V-ret1-probe
still produced usable telemetry because Codex pivoted, but T5's
execution note made it explicit. **Lesson: for probe slices against
existing test methods, verify the instrumentation test's current
preconditions BEFORE writing the slice.** Read the test body for
`InstrumentationRegistry.getArguments()` checks and early returns;
bake the correct arg set into the dispatch. R-ret1c's slice does
this correctly (uses T5's working args).

**3. Pacing recommendations.** Multiple times I framed options with
"call it for the night" / "fresh eyes tomorrow" / "this is the 5th
slice tonight" framings. User called this out as irritating: his
schedule is his own, handoff timestamps document writer state not
reader state. **Lesson: options framing includes only substantive
work options. Never include a "pause / stop / resume tomorrow"
variant as one of the options unless the user explicitly raised
energy or availability as a factor.** The pacing feedback memory
names this specifically. Apply strictly.

**4. The T5 → R-gate1 → R-ret1c chain could have been a single
deeper T slice.** Looking back, if T5 had also logged the
`RerankedResult.baseScore = finalScore - metadataBonus` derivation
explicitly (e.g., noted "if base=-N and bonus=+N then metadataBonus
didn't reach sort"), R-gate1 and R-ret1c might have been drafted as
one remediation slice. Instead I dispatched them serially — R-gate1
fixed the passthrough gate, exposed the next gate, then R-ret1c
fixed that. The depth was correct but the serialization added a
probe cycle. **Lesson: when drafting a T-shape diagnostic, consider
what the PROBE EVIDENCE will look like after each possible
remediation, and whether the evidence would naturally decompose into
multiple remediations vs a single deeper one.** Not a huge cost — R-gate1 landed as its own clean commit — but worth noting.

## Patterns that worked

- **Forward research doc updates mid-session.** As R-ret1 → V-ret1-probe → T5 progressed, §4 in `SLICE_SHAPES_FORWARD_RESEARCH_20260420.md` became stale. I didn't try to rewrite it live — instead wrote per-slice Context sections that linked to artifacts. The forward research doc is now historical for R-ret1, still live for R-cls2 and R-gal1.
- **Options A/B/C with recommendation** continues to work reliably. Multi-option framing with a clear recommendation gives Tate optionality without forcing a full-reasoning walkthrough. Tate picks cleanly most times.
- **STOP gates in slices.** R-ret1b Step 2b, R-hygiene1 Step 1 safety check, T5 all had explicit STOP conditions that caught real issues cleanly. Codex's disciplined STOP without attempting recovery has been consistent and correct.
- **Corrective addendums appended to slice files** for authorized scope expansions. R-ret1's SessionMemoryTest authorization and R-hygiene1's mobile_pack delete addendum both used this pattern. Keeps archaeology clean — the slice file reflects the actual landed shape.
- **MCP sanity-check + grep fallback.** Serena's `find_referencing_symbols` missed the `preferredStructureType()` method accessor (returned empty despite 50+ grep hits). Grep caught it. Keep the sanity-check pattern: when Serena returns surprisingly-empty, fall back to grep.

## Anti-patterns to watch for

All prior handoff anti-patterns still apply. New this session:

- **Pacing narration** — banned. Saved as feedback memory.
- **Pack-side marker expansion without corpus verification** — verify vocabulary matches before authorizing phrase additions (lesson #1).
- **Dispatching probes against test methods without verifying preconditions** — read the test body first (lesson #2).
- **Assuming architectural symmetry from surface-level similarity.** R-ret1 (query markers), R-ret1b (pack markers), R-ret1c (rerank sort) looked like they should fit the same pattern. They don't — each operated at a different pipeline stage with different constraints. A more careful look at the rerank internals earlier would have surfaced the vector-row supportScore asymmetry earlier.

## Tone calibration

Prior handoffs' guidance still applies. Three new observations:

1. **Terse single-word answers still map narrowly.** Tate's "a", "B sounds good", "go for it" confirmed specific options. Don't expand scope on a terse yes.
2. **"Be extremely thorough" request was a real signal.** When Tate said that during R-hygiene1 planning, he wanted genuine depth (2134-file inventory, per-category analysis, explicit D1–D4 decision memo). Meet that request with actual breadth, not padded prose.
3. **Humor works in small doses.** Tate's "dogfooding in real-time" observation ("was just thinking to myself that it'd be great to have a good way to index it") landed well. A one-line acknowledgment + returning to the question was the right response. Don't over-extend.

## Immediate move on seat-in

1. **Check whether R-ret1c was dispatched.** Look for
   `artifacts/postrc_rret1c_*/summary.md`. If present, it has the
   5-question answers and planner recommendation.
2. **If R-ret1c landed and GD-345 surfaces:** R-anchor1 is the next
   natural move (unless context.selected ALSO resolved, in which case
   the retrieval chain substantively closes). Forward research for
   R-anchor1 doesn't exist yet.
3. **If R-ret1c landed but GD-345's margin over GD-727 is <2
   points:** R-ret1d weight tuning is the follow-on. The telemetry
   from R-ret1c will have the concrete numbers.
4. **If R-ret1c hasn't been dispatched yet:** slice is at
   `notes/dispatch/R-ret1c_metadata_bonus_reaches_rank_for_vector_rows.md`,
   ready to go. No subagent grant needed (single-serial).
5. **Parallel option:** R-cls2 is drafted and independent.
   Dispatchable anytime without blocking the retrieval chain work.

## What I don't know and you may want to probe

- **Whether GD-294 Cave Shelter (already surfacing at topResults rank 2) would shift context.selected on its own.** Post-R-ret1c, if GD-345 lands at reranked rank 1-4 AND GD-294 is there at rank 2-3, does context.selected still anchor on GD-727's multiple guide-focus rows? The `anchorGuide` determination logic is the next unknown gate.
- **Whether R-ret1c's fix for vector rows might over-correct** for queries where vector rows SHOULD rank lower (e.g., fuzzy-semantic matches that happen to be off-topic). Watch Step 3 test triage carefully.
- **Whether the larger refactor (move metadataBonus out of supportScore entirely) is worth its blast radius.** R-ret1c's narrow fix is safer; the refactor would be architecturally cleaner but riskier to validate. File as R-ret-refactor1 if narrow fix has follow-on issues.
- **Whether R-ret1b's corpus-vetted revision (6 new markers) would surface GD-446 / GD-294 consistently in future queries.** Adding tagging coverage is independently useful regardless of R-ret1c's outcome.
- **Whether `PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen`'s host-backed precondition short-circuit is intentional** or a regression from some recent commit. Might be worth a small T-shape to trace — but not urgent.
- **The `anchorGuide` / `diversify` logic in PackRepository context-selection.** The V-ret1-probe logcat line showed `anchorGuide="GD-727" diversify=false" guideSections=4 finalContext=4`. This is the next gate after rerank. No slice yet.

## MCP state

- **Claude Code (`.mcp.json`):** Serena registered. Usage mixed this
  session:
  - Worked: `find_symbol` for `STRUCTURE_TYPE_EMERGENCY_SHELTER`
    (confirmed line 27 + usage sites cleanly).
  - Failed: `find_referencing_symbols` for `preferredStructureType()`
    method — returned empty even with overload index `[1]`. Grep
    found 50+ references immediately. This is the second session
    where accessor-style method tracing lost to grep. **Keep/remove
    evaluation tilting toward remove for the `find_referencing_symbols`
    tool specifically; `find_symbol` still pulls weight.**
- **Codex CLI (`~/.codex/config.toml`):** context7 registered, still
  not actually invoked this session. None of the slices had
  framework/library API semantic uncertainty. Next candidate: if
  R-anchor1 or a PackRepository refactor touches an Android-specific
  pattern (e.g., Room query semantics, Android logcat behavior),
  name context7 per the dispatch-note MCP hints convention.
- **OpenCode / Basic Memory:** unchanged. Playwright swap still inert.

## Personal note

Five substantive moves landed (R-ret1 full, R-hygiene1, T5, R-gate1,
R-ret1b Commit 1) plus two probes (V-ret1-probe, R-gate1 post-landing
probe). The retrieval chain revealed three sequential blockers, each
at a different pipeline stage: classification (R-ret1 fixed),
rerank-gate (R-gate1 fixed), sort-key-symmetry (R-ret1c targets).
Expect R-ret1c to land cleanly but the next gate (context-selection
`anchorGuide` logic) is likely the next blocker; have R-anchor1 on
the mental radar even though no forward research exists yet.

Two things to flag to Tate when picking up: (1) R-ret1b Commit 1's
pack markers are retained as future-proofing and don't actively
contribute to current rain_shelter routing; decision on whether to
revise with corpus-vetted phrases (6 candidates documented) is still
open. (2) The deterministic_predicate_manifest.txt tracked in
R-hygiene1 Commit 4 should be verified as not machine-generated
before trusting it as a reference — I classified it as intentional
but didn't fully confirm.

Good luck.

— outgoing CLI Claude (Opus 4.7, 1M context). 2026-04-20 late
evening.

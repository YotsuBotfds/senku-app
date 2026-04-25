# Research prompt — R-anchor-refactor1 forward research

Paste to a new `gpt-5.4 xhigh` main-agent session. Output lands at `notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_<date>.md`. No code edits. No commits. Read-only investigation + written deliverable.

---

You are producing forward research for **R-anchor-refactor1**, an architectural refactor that has been deferred twice but keeps producing narrow-symmetry slices instead of one clean fix. Your output is a written research doc that the planner will use to draft a dispatchable slice. You do NOT dispatch code changes; this turn is research only.

## The thesis you're testing

The string pattern across four prior slices (R-gate1, R-ret1c, R-anchor1, R-gal1) is: **`metadataBonus` is computed in `supportScore` but is only consumed at some scoring boundaries, not all of them.** Each of those four slices patched one narrow boundary where metadataBonus was missing. The architectural fix is to pull `metadataBonus` OUT of `supportScore` entirely and apply it uniformly at every scoring boundary — no per-site fixes required.

Your job is to confirm or refute this thesis with evidence, then map the refactor if confirmed.

## Deliverable shape

One markdown doc at `notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_<YYYYMMDD>.md`. Roughly 400-800 lines. Structure:

1. **Thesis verdict** (1-2 paragraphs) — confirmed / partially confirmed / refuted, with the evidence pointer.
2. **Current scoring topology** — a complete map of where scores are computed and consumed in the Android engine. Every call site of `supportScore`, every place `metadataBonus` appears, every place a score-like quantity flows into a ranking decision. File:line citations, not hand-waves.
3. **Prior-slice pattern analysis** — read the four commits (see "Starting references" below) and name the exact pattern each one patched. Answer: were they all the same shape, or are there 2-3 distinct sub-patterns? If distinct, the unified fix may not be as clean as the thesis suggests.
4. **Unified application proposal** — if thesis holds: a concrete refactor shape. What fields move from `supportScore` to where? What's the new contract? What does a scoring boundary look like before and after? Pseudocode acceptable.
5. **Risk analysis** — (a) which existing tests would break; (b) which tests would need to be added to pin the new uniform-application invariant; (c) which user-visible behaviors might change; (d) any place where non-uniform application is intentional (some scores may legitimately ignore metadataBonus — you must find these before proposing uniformity).
6. **Draft acceptance criteria** — what would the resulting slice's HARD gates look like? Aim for invariant-form assertions (e.g., "for every scoring boundary in `OfflineAnswerEngine`, metadataBonus contributes additively with weight `W`") rather than example-form (e.g., "test X passes").
7. **Blockers for slice draft** — anything that still needs resolution before a dispatchable slice can be written. Flag questions, not just facts.
8. **Known wrongs** — if you believe the thesis is actually wrong (say, the pattern is driven by something other than metadataBonus, or the four prior fixes don't actually share a root cause), SAY SO clearly. A refuted thesis is a valuable output.

## Starting references

Read these first, in order:

- **`notes/dispatch/completed/R-gate1_rerank_gate_decouple_from_route_focus.md`** — first slice in the symmetry family. Read the full slice, including the problem statement and the commit it refers to.
- **`notes/dispatch/completed/R-ret1c_metadata_bonus_reaches_rank_for_vector_rows.md`** — second. Name alone suggests metadataBonus was already scoped here.
- **`notes/dispatch/completed/R-anchor1_include_vector_rows_in_anchor_scoring.md`** — third.
- **`notes/dispatch/completed/R-gal1_relax_trust_spine_uncertain_fit_wording.md`** — fourth. Note: may not fit the metadataBonus pattern even though the DAY handoff groups it. Investigate carefully.
- **`notes/PLANNER_HANDOFF_2026-04-21_DAY.md`** — search for "R-anchor-refactor1" and "metadataBonus" / "supportScore". This is where the thesis was first articulated; the planner may have left context about what evidence they had.
- **`notes/PLANNER_HANDOFF_2026-04-21_LATE.md`** — the most recent handoff naming R-anchor-refactor1 as a queued direction. Check for updated framing.

Then read the engine code:

- **`android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`** — the central engine. Likely has `supportScore`, `metadataBonus`, `rerankCandidates`, anchor scoring, and `averageRrfStrength`. Read enough to map the scoring topology; you do NOT need to read every method body.
- **`android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`** — the profile that produces `metadataBonus` inputs. Understand what it exposes.
- **`android-app/app/src/main/java/com/senku/mobile/`** (directory) — scan for any other scoring surfaces (anchor scoring, rerank scoring, trust-spine scoring). Name the files; do not exhaustively read.

Tests that codify the current scoring behavior:

- **`android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`** — massive. Grep for `supportScore`, `metadataBonus`, `rerankScore`, `anchorScore`. Understand which invariants are currently pinned.
- **`android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`** — smaller. Mainly confirms metadataBonus inputs.

## What to watch for

- **Multiple definitions of "metadataBonus."** It's possible the string "metadataBonus" refers to different numeric quantities in different scoring contexts (local method variable shadowing). Treat each call site as potentially distinct until proven same.
- **Weights that differ between boundaries.** Some scoring boundaries may apply metadataBonus with weight 1.0; others may apply it with weight 0.5. A "unified application" proposal must account for these weights (unifying to a single weight is a user-visible behavior change; unifying to per-boundary weights preserves behavior but is less elegant).
- **The difference between `supportScore` (maybe a struct or method that computes it) and the numeric value it returns.** The refactor thesis says "move metadataBonus OUT of supportScore." That could mean: (a) remove from the struct/return type, compute separately; (b) compute inside but add at callers; (c) fully separate concerns with a new type. Different refactor shapes have different risk profiles.
- **Anchor scoring is a known edge case.** `R-anchor1` named vector rows specifically. If anchor scoring ignores metadataBonus intentionally (e.g., anchor anchors on topic, not on metadata), the thesis breaks for that boundary.
- **Trust-spine / uncertain_fit wording (R-gal1) may be a different family entirely.** Look at its commit diff — does it actually touch metadataBonus? If no, the thesis is overreaching and should be narrowed.

## What's out of scope

- Python desktop backend (`query.py`, `ingest.py`). The refactor is Android-only.
- Mobile pack generation (`mobile_pack.py`). Pack-level marker logic is separate from engine-level scoring.
- Retrieval quality evaluation. You're not benchmarking the refactor; you're mapping it.
- Proposing the slice itself. Your deliverable is research that ENABLES the slice draft. A one-line "then draft a slice that does X" at the end is fine, but the slice writing itself is for the planner.

## Format notes

- Use the same tone and structure as `notes/R-TELEMETRY_FORWARD_RESEARCH_20260421.md` if you can find it. That doc was called "excellent scaffolding" by the planner who used it.
- File:line citations mandatory for code claims. Hand-waves cost the slice author a re-read.
- If you find a dead end or can't resolve a question, say so and propose what evidence would resolve it. Don't pretend certainty.
- Prefer "confirmed by [evidence]" over "seems to be" — or flag the uncertainty explicitly.
- Expected length: 400-800 lines. Shorter is fine if the answer is clear; longer is fine if the topology is more tangled than the thesis suggests.

## MCP hints

- **context7** may be useful if you need to check Java/Android framework behavior around concurrent hashmap reads, Stream API, or anything where SDK semantics matter for the refactor.
- **git** MCP for fast grepping commit history to find when `metadataBonus` was introduced and how it's grown.
- **sequential-thinking** for the risk-analysis section if the dependency graph is thick.

Don't over-invoke MCP for straightforward file reads — Grep and Read are faster. Use the MCPs when a framework question surfaces you'd otherwise handwave.

## Anti-recommendations

- Do NOT edit engine code. Read-only.
- Do NOT add tests. Read-only.
- Do NOT rewrite the thesis to be bigger ("while we're here, also refactor rerank"). Stay scoped.
- Do NOT declare the thesis confirmed without showing per-boundary evidence. A single counterexample (one boundary that legitimately shouldn't get metadataBonus) changes the refactor shape.
- Do NOT produce a slice draft. Your output is research, not a dispatchable slice.
- Do NOT propose deletions of functionality to make uniformity work. If a boundary genuinely should not have metadataBonus, the refactor must preserve that; a "uniform application" that quietly breaks a known-intentional asymmetry is a regression.

## Deliverable location

`notes/R-ANCHOR-REFACTOR1_FORWARD_RESEARCH_<YYYYMMDD>.md` (today's date).

When done, report back with:
- File path written.
- Thesis verdict (one word: confirmed / partial / refuted).
- Estimated slice size (files × lines × tests, for the eventual refactor slice).
- Any blockers for slice draft (resolvable questions that must close before drafting).

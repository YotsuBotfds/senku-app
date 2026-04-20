# Slice R-eng — `OfflineAnswerEngine` mode-gate hardening

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** `R-cls_query_metadata_profile_hardening.md`
  and `R-pack_poisoning_chunk_rebuild.md`. Three slices touch
  independent layers (Java classifier vs Python pack/ingest vs
  Java engine code) → no file overlap.
- **Predecessor:** T1 root cause at
  `notes/T1_STAGE2_ROOT_CAUSE_20260419.md` (Failures 1 + 2 +
  Cross-Cutting Findings #1, #3, #5).

## Context

T1 localized three coupled defects in
`OfflineAnswerEngine.java` that together produced two of Stage 2's
failures (rain-shelter mode misroute + violin-bridge hallucination):

1. **Mode gate scores the wrong evidence surface.**
   `resolveAnswerMode(...)` (`:287-339`) decides confidence /
   uncertain_fit / abstain by inspecting `modeCandidates` (the raw
   answer candidates) via `topAbstainChunks(...)` (`:1043-1058`,
   `:1208-1300`), not the finalized selected context that actually
   feeds prompting. One off-topic top row (e.g., GD-727 Batteries
   for a shelter query) pollutes the gate even when selected
   context is good.

2. **`shouldAbstain()` treats any hybrid top-row as a semantic
   veto.** At `:1346-1372`, if any top-3 row is marked `hybrid`,
   `strongSemanticHit = true` and abstain refuses to fire. For
   `violin bridge soundpost`, wrong-sense `bridge` hybrid hits
   from GD-110 (civil engineering) prevent abstain even though no
   actual evidence supports the violin question.

3. **`low_coverage_detected` is observational, not route-affecting.**
   At `:586-595` the signal is computed AFTER generation completes
   and only changes the subtitle; the answer card still renders
   normally. The engine knows the answer is low-coverage but does
   not act on it.

T1 also flagged `promptContextLimitFor(...)` at `:749-757` as
narrowing generation to too few rows on ambiguous queries
(violin-bridge got `promptContext=2`), but this is secondary —
fix it only if it falls naturally out of the main work.

T1's planner read explicitly warns AGAINST raising or lowering the
`UNCERTAIN_FIT_*` thresholds at `:29-31`. Do not retune. The
problem is the gate is applied to the wrong evidence surface, not
the threshold values.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - The corresponding test file (find via Glob; create if it
    doesn't exist).
- Do NOT touch `QueryMetadataProfile.java` (R-cls's scope).
- Do NOT touch `PackRepository.java` unless absolutely required —
  if it is, STOP and report; planner will scope.
- Do NOT touch any Python ingest / pack code (R-pack's scope).
- Do NOT modify the `UNCERTAIN_FIT_*` or other tuning constants
  at `OfflineAnswerEngine.java:29-31`. The fix is gate logic, not
  threshold values.
- No emulator interaction. Unit tests run via Gradle without
  emulator.
- Single commit. Tests must pass before commit.

## Outcome

`OfflineAnswerEngine.resolveAnswerMode(...)` and `shouldAbstain(...)`
make decisions from finalized selected context, not raw retrieval
tops. Wrong-sense hybrid hits no longer veto abstain by themselves.
The `low_coverage_detected` signal — whether produced
pre-generation or escalated post-generation — converts the answer
surface to `uncertain_fit` (or `abstain` on the abstain shape)
instead of leaving a normal answer card.

## The work

### Step 1 — read the engine in full

Read `OfflineAnswerEngine.java` end to end. Map:

- `resolveAnswerMode(...)` and every helper it calls
  (`shouldAbstain`, `averageRrfStrength`, `topVectorSimilarity`,
  the safety fallback path, `topAbstainChunks`)
- The flow that produces `modeCandidates` vs. the flow that
  produces the finalized selected context (`context.selected` in
  the logcat) — these are two distinct evidence surfaces in the
  current code
- Where `low_coverage_detected` is computed (`:586-595` per T1)
  and where the answer card is rendered. Trace the call chain
  from low-coverage detection to UI surface
- `promptContextLimitFor(...)` at `:749-757` and its callers at
  `:342-345` — only relevant if you decide to address the
  narrow-prompt-context secondary finding

Also read:
- The relevant failing logcats for context:
  - `artifacts/cp9_stage2_20260419_185102/validation_5556/emulator-5556/abstain_violin_bridge_soundpost/logcat.txt`
  - `artifacts/cp9_stage2_20260419_185102/validation_5556/emulator-5556/confident_rain_shelter/logcat.txt`

### Step 2 — fix #1: mode gate uses finalized selected context

Refactor `resolveAnswerMode(...)` and the helpers it calls to
operate on the finalized selected-context list instead of the raw
`modeCandidates`. Specific aim: when `context.selected` shows
relevant material (e.g., GD-294 Cave Shelter + GD-933 Shelter
basics for a tarp-shelter query), the gate should compute
strength against that selected context, not against the raw top
rows that included the off-topic GD-727.

Implementation guidance (suggestion, not prescription):
- The selected context is computed by an existing path; pass it
  through to `resolveAnswerMode(...)` as a parameter, or read it
  off the same data structure it lives on.
- Keep `topAbstainChunks(...)` for any sub-decision that genuinely
  needs the raw rows (e.g., overlap detection between top rows
  and selected context), but don't make it the sole input to the
  confidence call.

### Step 3 — fix #2: `shouldAbstain` doesn't accept hybrid as a veto

Change `shouldAbstain(...)` so a `hybrid` top-row alone is not
sufficient to set `strongSemanticHit = true`. Require stronger
evidence:

- Vector similarity above some bar (use the existing
  `topVectorSimilarity(...)` rather than introducing new
  thresholds)
- AND/OR overlap between the top row and the selected context's
  anchor guide
- AND/OR an explicit confident retrieval signal (look at how
  `routeFocused=true` interacts; routeFocused tends to indicate
  high-confidence retrieval)

The goal: a single hybrid row from a wrong-sense match (GD-110
for "violin bridge") cannot prevent abstain by itself.

Be conservative — over-eager abstain on real questions is a
regression too. Validate against the passing cases (drowning
rescue, mania escalation) when reasoning about the change.

### Step 4 — fix #3: `low_coverage_detected` becomes route-affecting

Wire the `low_coverage_detected` signal into the answer-mode
decision. Two acceptable shapes (pick the one that fits the
existing flow):

- **Pre-generation gate**: detect low-coverage before generation
  (similar to how abstain or uncertain_fit is detected) and route
  to `uncertain_fit` or `abstain` directly without running the
  model.
- **Post-generation downgrade**: keep the post-generation
  detection, but on detection, replace the rendered card with the
  uncertain_fit surface (or abstain surface, depending on the
  query shape).

Pre-generation is preferable because it saves model time on
queries that don't have evidence to support an answer. But
post-generation downgrade is fine if pre-generation detection
would require a deeper refactor than the slice budget allows.

If you choose post-generation, ensure the user-visible surface
actually changes — not just the subtitle. The current bug is that
the surface stays as a normal `paper_card mode=paper
evidence=moderate abstain=false` card.

### Step 5 — tests

Add unit tests covering the three fixes:

- **Mode-gate evidence surface**: a query whose raw top row is
  off-topic but whose selected context is on-topic should route
  to confident, not uncertain_fit. Construct a fixture that
  mimics the rain-shelter shape.
- **Hybrid-row abstain veto**: a query whose top-3 rows are all
  `hybrid` but whose selected context has weak overlap should
  reach abstain. Construct a fixture that mimics the violin-bridge
  shape.
- **Low-coverage gating**: a query that triggers
  `low_coverage_detected` should produce an answer surface other
  than the normal moderate-evidence card.

Plus regression coverage: pick 2-3 cases from the passing Stage 2
prompts (drowning rescue, mania escalation) and verify they still
behave correctly.

If a test class for `OfflineAnswerEngine` doesn't exist, create
one following the project's Java test conventions.

### Step 6 — run tests + commit

Run the Android unit tests:
- `./gradlew :app:testDebugUnitTest --tests
  "com.senku.mobile.OfflineAnswerEngineTest"`
  (or the actual class path)

If the Gradle invocation fails for environmental reasons, report
the error and try a fallback. Do not skip tests.

Commit message suggestion:
`R-eng: gate on selected context, drop hybrid abstain veto, route on low coverage`

(Adjust to match project commit message conventions.)

## Acceptance

- All Step 5 test categories pass: mode-gate, abstain veto,
  low-coverage gating, plus the 2-3 regression cases.
- The `UNCERTAIN_FIT_*` constants at `:29-31` are unchanged.
- Single commit, message references R-eng and T1.
- No edits to any file outside `OfflineAnswerEngine.java` and its
  test file.

## Report format

Reply with:
- Commit sha.
- Per-step one-line summary (1 through 6).
- Design decision for each of the three fixes:
  - **Fix #1**: how you passed the selected context to the gate
    (parameter / data structure / other) and what scoring helper
    now reads it.
  - **Fix #2**: what additional evidence you required beyond
    "any hybrid row" before allowing the semantic veto.
  - **Fix #3**: pre-generation detection or post-generation
    downgrade, and which answer surface you swap to.
- Whether you also addressed the secondary `promptContextLimitFor()`
  finding, and if so why; if not, why not.
- Test class path + how many tests added + how many existing
  tests still pass.
- Any out-of-scope finding worth flagging to planner (don't fix).
- Delegation log.

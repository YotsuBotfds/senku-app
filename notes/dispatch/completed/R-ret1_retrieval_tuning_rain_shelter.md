# R-ret1 — retrieval tuning for rain_shelter (post-RC)

Post-RC slice 1 of 3. Goal: the rain_shelter probe
("How do I build a simple rain shelter from tarp and cord?") routes to
`confident` instead of `uncertain_fit` because the query classifier now
catches tarp-shelter phrasings and surfaces shelter-family guides via the
`emergency_shelter` metadata bucket.

**Dispatch shape:** single worker, main-lane inline (serial work, no
fan-out). No context7 hint — pure in-repo Java refactor.

## Context

- CP9 closed with RC v5 cut (`0cd86fa`). Wave B 20/20 under Option C;
  state-pack 41/45 with four `generativeAskWithHostInferenceNavigatesToDetailScreen`
  limitations tracked as R-gal1.
- Failure today: state-pack probe "How do I build a simple rain shelter
  from tarp and cord?" settles `uncertain_fit` instead of `confident`.
  Live retrieval anchors on GD-727 Batteries rather than shelter guides.
- Root cause: `QueryMetadataProfile.java:1585`
  `STRUCTURE_TYPE_EMERGENCY_SHELTER` marker set is
  `{"debris hut", "lean-to", "a-frame shelter", "emergency shelter"}`.
  None of those phrases match the probe, so the profile's
  `preferredStructureType()` is empty → no `emergency_shelter` bucket
  preference → no category/structure metadata bonus on shelter rows in
  `PackRepository` scoring → GD-727 dominates via raw vector similarity.
- `containsAny()` was hardened to token/phrase-aware matching in R-cls
  (`e07d4e7`), so phrase markers added here match cleanly without
  substring over-match. Plain single-token markers (e.g. "tarp" alone)
  MUST be avoided — they would over-match camping/boating guides.

## Precondition

- Repo on `master` with HEAD at `0cd86fa` (S3 CP9 closure).
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
  unchanged since `0cd86fa`.
- Baseline `./gradlew.bat :app:testDebugUnitTest` passes BEFORE any
  edits. Record the pre-edit pass count in the commit scratch notes.

## Step 0 — Pre-edit test-impact grep

Before editing production code, read the three existing rain/tarp
shelter tests in `OfflineAnswerEngineTest.java` and record their current
expected modes:

1. `resolveAnswerModeHitsBoundariesAndSafetyGate` (lines 302–339):
   `"how do i repair a tarp shelter after wind damage"` → expects
   `UNCERTAIN_FIT`.
2. Same test (lines 341–374): `"how do i build a rain shelter from a
   tarp"` → expects `ABSTAIN`.
3. `safetyModeOverridesDoNotRerouteViolinBridgeOrRainShelter` (lines
   676–805): exact state-pack probe → expects `CONFIDENT` with strong
   fixture.

After the marker expansion in Step 1, all three queries will have
`preferredStructureType() == "emergency_shelter"` where previously (1)
and (2) had empty structure. Fixtures in (1) already carry
`structure_type="emergency_shelter"` and `topic_tags="tarp_shelter,..."`,
so those rows will gain a +6 preferredCategories bonus (building /
survival) plus topic-overlap bonus. The net mode may or may not shift —
Step 3 runs the suite and records actuals, no preemptive test edits.

Do NOT modify tests in Step 0.

## Step 1 — Expand `STRUCTURE_TYPE_EMERGENCY_SHELTER` marker set

Edit `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
line 1585. Replace:

```java
markers.put(STRUCTURE_TYPE_EMERGENCY_SHELTER, buildSet("debris hut", "lean-to", "a-frame shelter", "emergency shelter"));
```

With:

```java
markers.put(
    STRUCTURE_TYPE_EMERGENCY_SHELTER,
    buildSet(
        "debris hut", "lean-to", "a-frame shelter", "emergency shelter",
        "rain shelter", "rain fly", "tarp shelter", "tarp ridgeline",
        "ridgeline shelter", "tarp and cord", "tarp and rope"
    )
);
```

Markers are phrase-level only. Do NOT add plain `"tarp"` or plain
`"ridgeline"` — both would over-match unrelated guides (canoe
ridgelines, camping tarps, etc.).

No other production-code changes in this slice. `metadataBonus` weight
tuning is explicitly out of scope; see Out of Scope section.

## Step 2 — Add `QueryMetadataProfileTest` cases

Add five new tests to
`android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`.
Follow the style of the existing poisoning tests added in R-cls
(`e07d4e7`).

Positive (structure-type should be `emergency_shelter`):

1. `detectStructureTypeIdentifiesRainShelterFromTarpAndCord` — probe
   "How do I build a simple rain shelter from tarp and cord?".
2. `detectStructureTypeIdentifiesRainFly` — query "setting up a rain fly
   in a storm".
3. `detectStructureTypeIdentifiesTarpRidgelineShelter` — query "how to
   make a tarp ridgeline shelter with cord".

Negative (structure-type should NOT be `emergency_shelter`):

4. `detectStructureTypeDoesNotRouteGenericTarpQuery` — query "how to
   store a rolled tarp in the shed".
5. `detectStructureTypeDoesNotRouteCampingTarpQuery` — query "best tarp
   brand for weekend camping".

Assertions: each test builds the profile via
`QueryMetadataProfile.fromQuery(...)` and asserts
`preferredStructureType()` equals or does not equal `"emergency_shelter"`.
Match the exact assertion style used by existing tests in the file.

## Step 3 — Run affected test classes, triage mode shifts

Run:

```
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.QueryMetadataProfileTest"
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.OfflineAnswerEngineTest"
```

Expected:

- `QueryMetadataProfileTest` — all five new tests green; no prior
  regressions.
- `OfflineAnswerEngineTest#resolveAnswerModeHitsBoundariesAndSafetyGate`
  — may shift. If fixtures in (1) above now produce a different
  `resolveAnswerMode` verdict because profile structure is no longer
  empty, choose ONE remediation:
  - (a) keep assertion, tighten fixture so the shelter evidence is
    genuinely weak (matches the intent of the UNCERTAIN_FIT bucket), OR
  - (b) update the assertion to the new expected mode under richer
    retrieval context.
  Document the choice with one-sentence rationale in the commit
  message body.
- `OfflineAnswerEngineTest#safetyModeOverridesDoNotRerouteViolinBridgeOrRainShelter`
  — should continue to pass; its CONFIDENT assertion may strengthen
  because the profile now correctly routes the probe to
  `emergency_shelter`.

If any test OUTSIDE these three regresses, STOP and flag to planner —
that means a marker-match collision outside the anticipated surface
(e.g. a ridgeline reference in a canoe test fixture).

## Step 4 — Full suite acceptance

Run the full unit suite:

```
./gradlew.bat :app:testDebugUnitTest
```

Acceptance criteria:

- Full suite green.
- New `QueryMetadataProfileTest` cases: 3 positive + 2 negative = 5 new
  tests, all passing.
- Pre-edit pass count + new-tests count = post-edit pass count, unless
  Step 3 required a fixture/assertion update (which should keep the
  total unchanged or +N for any new regression tests).
- Commit message body names:
  1. Which new markers were added and why phrase-level (over-match
     prevention).
  2. Whether any existing `OfflineAnswerEngineTest` test was touched;
     if yes, which one and why (fixture tightened vs assertion
     updated).
  3. Explicit out-of-scope note: no `metadataBonus` weight change, no
     state-pack revalidation in this slice.

## Out of scope

- `metadataBonus` weight tuning. If post-R-ret1 state-pack re-probe
  still shows rain_shelter routing `uncertain_fit`, planner files
  `R-ret1b` with concrete retrieval-ranking evidence from the re-probe.
  Do NOT modify `QueryMetadataProfile.metadataBonus` or
  `PackRepository` scoring constants in this slice.
- Desktop `query.py` parity. Desktop is deprecated; mobile-only fix is
  sufficient for the Android MVP.
- R-gal1 / R-cls2. Separate post-RC slices.
- State-pack validation sweep. Planner's bounded re-probe lives in a
  follow-on step, not this slice.
- APK rebuild / pack rebuild / emulator validation. This slice is
  unit-test scope only.

## Expected commit footprint

- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
  — one multi-line marker set expansion (7 phrase additions).
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
  — 5 new test methods (~50 lines).
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  — 0, 1, or 2 test method updates, only if Step 3 surfaces real mode
  shifts that require fixture/assertion adjustment.

Total: 1 production file + 1–2 test files. Small slice.

## Post-slice planner follow-up (NOT in this slice)

After R-ret1 lands, planner runs a bounded state-pack re-probe on the
four failing `generativeAskWithHostInferenceNavigatesToDetailScreen`
cases to see whether R-gal1 self-resolves. If rain_shelter now renders
`confident` with matching trust-spine status wording, R-gal1 closes
without code. If not, planner either dispatches R-gal1 (Option A: relax
assertion) or R-ret1b (weight tuning) based on the re-probe evidence.

---

## Addendum — planner scope authorization 2026-04-20 evening

Step 3 surfaced one test failure outside the anticipated surface, as
expected by the slice's STOP clause. Planner reviewed and authorizes
the scope expansion below.

### Root cause (verified)

`SessionMemory.java:848-851` has:
```java
switch (safe(sessionProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE)) {
    ...
    case "emergency_shelter" -> addContextTerms(target, "shelter", 1, true);
```

Before R-ret1, query `"how do i build a rain shelter"` classified as
structure-type empty, so this branch was a no-op and the plan's search
query stayed `"rain shelter"` from the original tokens. After R-ret1 it
classifies as `emergency_shelter`, so `"shelter"` is injected first →
`LinkedHashSet` insertion order produces `"shelter rain"`.

Planner-side `find_referencing_symbols` + grep sweep across all
consumers of `preferredStructureType()` confirms this is the ONLY
output-shape-dependent consumer that shifts under R-ret1. Other
consumers (`PackRepository`, `LatencyPanel`, engine tests) either
branch on specific OTHER structure types or use boolean-only checks.

### Authorization (Option X)

Fold the test-assertion update into the R-ret1 commit. Do NOT change
`SessionMemory.java`. Do NOT change `QueryMetadataProfile` beyond the
original slice scope.

### Step 5 — scoped test assertion update

Edit `android-app/app/src/test/java/com/senku/mobile/SessionMemoryTest.java`
line 759. Change:

```java
assertEquals("rain shelter", plan.searchQuery);
```

To:

```java
assertEquals("shelter rain", plan.searchQuery);
```

No other changes to SessionMemoryTest. If any other test in the file
now fails with this change (it should not, based on the sweep), STOP
and flag again.

### Step 6 — commit

Commit message body MUST include:

1. Marker expansion rationale (per Step 4 acceptance).
2. Whether OfflineAnswerEngineTest was touched (per Step 3 outcome).
3. A new paragraph naming the SessionMemoryTest assertion shift:
   - Which test method: `anchorPriorFollowUpBuildsTypedDirectiveWhenEnabled`.
   - Why the assertion shifted: rain_shelter queries now correctly
     classify as `emergency_shelter` via R-ret1's marker expansion,
     which triggers `SessionMemory.addStructureContextTerms`'s
     `emergency_shelter` branch at line 851 to inject `"shelter"` as
     a structure-context term, producing `"shelter rain"` via
     `LinkedHashSet` insertion order.
   - Why this is the intended new behavior: the structure-context
     branch exists specifically to anchor follow-up retrieval on the
     detected structure. The old assertion was incidentally locked to
     the pre-classification baseline. Every other structure-type branch
     in `addStructureContextTerms` produces the same pattern
     (structure keyword first).

### Acceptance (revised)

- Full suite: 403/403 passing (up from 402/403).
- Commit body names the three outcomes: marker rationale,
  OfflineAnswerEngineTest outcome, SessionMemoryTest shift.
- Only three files touched total: `QueryMetadataProfile.java`,
  `QueryMetadataProfileTest.java`, `SessionMemoryTest.java`. No
  production-code changes outside `QueryMetadataProfile`.

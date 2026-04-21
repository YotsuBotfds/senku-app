# R-cls2 — port QUERY-side acute-mental-health markers to QueryMetadataProfile (post-RC)

Goal: move the QUERY-side acute-mental-health classifier from `OfflineAnswerEngine` into `QueryMetadataProfile` as a new `STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH` bucket, matching the R-cls architecture for poisoning. The engine-side gate refactors to consume `profile.preferredStructureType()` as the primary signal, with the compound `pacing + (slept|sleep)` check retained as an engine-side fallback for the over-match-risk edge case.

Forward research resolved to Option 1b at `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md` §6. Non-negotiable: SUPPORT markers (`OfflineAnswerEngine.java:76-92`) stay in engine because they're consumed against retrieval-result surfaces, not queries.

**Dispatch shape:** single main-lane worker, serial. No MCP hint. One commit.

## Context

R-cls (commit `e07d4e7`) introduced `STRUCTURE_TYPE_SAFETY_POISONING` in `QueryMetadataProfile` with token/phrase-aware marker matching. R-eng2 (commit `8990cc6`) then added acute-mental-health routing in `OfflineAnswerEngine.resolveAnswerMode` at line 1253, but the underlying classifier lived in the engine itself (three marker sets at `:49-92`, two helpers at `:1287-1326`). R-cls2 completes the architectural parallel by moving the QUERY-side classifier into the profile.

Why Option 1b (QUERY only, SUPPORT + compound stay in engine):

- **SUPPORT markers (`:76-92`) consume selected-context retrieval surfaces.** They don't belong in a QueryMetadataProfile that describes the query itself.
- **Compound `pacing + (slept|sleep)` uses raw `String.contains()`** — substring match, not tokenized. Porting "pacing" alone into the profile's tokenized marker set would over-match queries like "pacing yourself after running." The compound gating (pacing MUST pair with slept/sleep) is specifically why this edge case works today.

## Precondition

- HEAD at `961d478` prefix (R-ret1b Commit 1) or later.
- Android unit suite: `./gradlew.bat :app:testDebugUnitTest` passes 403/403 (or 403 + N_telemetry if T5 already landed).
- No in-flight edits to `OfflineAnswerEngine.java` or `QueryMetadataProfile.java`.

## Step 0 — Baseline

1. `git rev-parse HEAD` returns `961d478` or later.
2. `./gradlew.bat :app:testDebugUnitTest` → record current pass count.
3. Enumerate existing test references to acute-mental-health fixtures (for Step 3 impact check):
   - `OfflineAnswerEngineTest.java:570` `resolveAnswerModeRoutesAcuteMentalHealthMismatchToUncertainFit` — uses full query "He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?"
   - `OfflineAnswerEngineTest.java:376` — same query in `resolveAnswerModeHitsBoundariesAndSafetyGate`.
   - `OfflineAnswerEngineTest.java:1643, :1672, :1807, :2415` — additional fixture occurrences in other test methods.

STOP if baseline isn't green.

## Step 1 — Add `STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH` to QueryMetadataProfile

### Step 1a — constant + marker set + custom detection method

**Architecture:** Follow the SAFETY_POISONING precedent, NOT the STRUCTURE_MARKERS map. `STRUCTURE_TYPE_SAFETY_POISONING` is detected via `looksLikeSafetyPoisoning(normalized)` at line 1762, called from `detectStructureType()` at line 1513 BEFORE the `STRUCTURE_MARKERS` map iteration. Acute-mental-health follows the same shape for architectural parity and to make future compound gating easier to add if needed.

Do NOT add `markers.put(STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH, ...)` to `buildStructureMarkers()` at line 1583.

In `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`:

1. **Add constant** near the other `STRUCTURE_TYPE_*` constants (next to `STRUCTURE_TYPE_SAFETY_POISONING` at line 43):
   ```java
   private static final String STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH = "acute_mental_health";
   ```

2. **Add marker set** after the `POISONING_UNKNOWN_INGESTION_MARKERS` block (around line 102-106, adjacent to the other poisoning-family marker sets):
   ```java
   private static final Set<String> ACUTE_MENTAL_HEALTH_QUERY_MARKERS = buildSet(
       "barely slept",
       "hardly slept",
       "keeps pacing",
       "normal rules do not apply",
       "special mission",
       "acting invincible",
       "nothing can hurt",
       "just stress",
       "calm down"
   );
   ```

   The 9 phrases mirror `OfflineAnswerEngine.ACUTE_MENTAL_HEALTH_QUERY_MARKERS` at lines 65-75 exactly. `containsAny` is already token/phrase-aware (post R-cls), so multi-word phrases match correctly.

3. **Add custom detection method** near `looksLikeSafetyPoisoning` (around line 1762-1778). Simple body; no compound gating:
   ```java
   private static boolean looksLikeAcuteMentalHealthProfile(String normalized) {
       return containsAny(normalized, ACUTE_MENTAL_HEALTH_QUERY_MARKERS);
   }
   ```

4. **Hook into `detectStructureType()`** at line 1513-1519. Insert the new branch immediately after `looksLikeSafetyPoisoning` and before the `HOUSE_PROJECT_MARKERS` fallback:
   ```java
   if (looksLikeSafetyPoisoning(normalized)) {
       return STRUCTURE_TYPE_SAFETY_POISONING;
   }
   if (looksLikeAcuteMentalHealthProfile(normalized)) {        // NEW
       return STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH;                // NEW
   }
   if (containsAny(normalized, HOUSE_PROJECT_MARKERS)) {
       return STRUCTURE_TYPE_CABIN_HOUSE;
   }
   ```

   Order matters: safety-poisoning still wins on dual matches (e.g., a query about a depressed relative who ingested something routes to poisoning first — poisoning is more immediately actionable). Acute-mental-health fires only when poisoning doesn't.

**Over-match note to include in commit message:** `"calm down"` and `"just stress"` can appear in non-mental-health contexts ("calm down the dog", "just stress test"). This is the same risk the engine classifier already accepts at `OfflineAnswerEngine.java:65-75`. R-cls2 inherits the risk deliberately — the engine's SUPPORT-markers check at `:1308-1326` still gates the final route (profile primary OR engine compound) AND `hasAcuteMentalHealthSupport(selectedContext)` must be false before UNCERTAIN_FIT fires. Profile over-match does not cause incorrect routing on its own.

### Step 1b — preferredCategories for the new bucket

`buildPreferredCategories()` at line 942+ needs a branch for the new structure type. Acute mental health queries should prefer `medical` and potentially mental-health-adjacent categories. Check the existing category vocabulary in the corpus:

```python
# Reference only — Codex can grep for available categories:
grep -hE "^category:" guides/*.md | sort -u
```

Expected categories in the corpus: `survival`, `medical`, `building`, `crafts`, etc. The right set for acute mental health is likely `{"medical"}` — survival/first-aid guides that cover mental-health crises will have that category.

If in doubt, follow R-cls's poisoning precedent:
```java
} else if (STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH.equals(structureType)) {
    Collections.addAll(categories, "medical");
}
```

### Step 1c — timeHorizon for the new bucket

`detectTimeHorizon()` at line 1549+. Acute queries are immediate:
```java
if (STRUCTURE_TYPE_EMERGENCY_SHELTER.equals(structureType)
    || STRUCTURE_TYPE_SAFETY_POISONING.equals(structureType)
    || STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH.equals(structureType)   // NEW
    || containsAny(normalized, IMMEDIATE_MARKERS)) {
    return TIME_HORIZON_IMMEDIATE;
}
```

### Step 1d — QueryMetadataProfileTest coverage

Add to `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`. Follow the pattern of the poisoning tests (R-cls added 7 tests, R-ret1 added 5; use similar naming).

Minimum coverage:
- One positive test per phrase marker (9 positives). Each test uses a short query containing exactly one marker in a plausible context and asserts `preferredStructureType() == "acute_mental_health"`.
- One negative: `"pace yourself when running a marathon"` should NOT match (profile has the compound phrase `"keeps pacing"`, not `"pacing"` alone — tokenized phrase matching guards this).
- One query-combination positive: full query from the existing engine test — `"He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down?"` → `preferredStructureType() == "acute_mental_health"`.
- One precedence test: query containing both a poisoning trigger (e.g., `"the child swallowed bleach"`) AND an acute-mental-health marker (e.g., `"and has barely slept"`) → `preferredStructureType() == "safety_poisoning"`. Poisoning's `detectStructureType()` check at line 1513 fires first; acute-mental-health branch does not override.

Total: 12 new tests.

## Step 2 — Refactor `OfflineAnswerEngine` gate

Edit `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`:

### Step 2a — update `shouldRouteAcuteMentalHealthToUncertainFit` signature + body

Current (`:1287-1296`):
```java
private static boolean shouldRouteAcuteMentalHealthToUncertainFit(
    List<SearchResult> selectedContext,
    String query,
    boolean safetyCritical
) {
    if (!safetyCritical || !looksLikeAcuteMentalHealthQuery(query)) {
        return false;
    }
    return !hasAcuteMentalHealthSupport(selectedContext);
}
```

New:
```java
private static boolean shouldRouteAcuteMentalHealthToUncertainFit(
    List<SearchResult> selectedContext,
    String query,
    QueryMetadataProfile metadataProfile,
    boolean safetyCritical
) {
    if (!safetyCritical) {
        return false;
    }
    boolean viaProfile = metadataProfile != null
        && "acute_mental_health".equals(safe(metadataProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE));
    boolean viaEngineFallback = looksLikeAcuteMentalHealthQuery(query);
    if (!viaProfile && !viaEngineFallback) {
        return false;
    }
    return !hasAcuteMentalHealthSupport(selectedContext);
}
```

### Step 2b — update caller at `:1253`

```java
if (shouldRouteAcuteMentalHealthToUncertainFit(selectedContext, query, metadataProfile, safetyCritical)) {
    return AnswerMode.UNCERTAIN_FIT;
}
```

`metadataProfile` is already in scope at that site (it's a parameter of `resolveAnswerMode`).

### Step 2c — leave `looksLikeAcuteMentalHealthQuery` body alone

The compound check at `:1303-1305`:
```java
return containsAny(normalized, ACUTE_MENTAL_HEALTH_QUERY_MARKERS)
    || (normalized.contains("pacing")
        && (normalized.contains("slept") || normalized.contains("sleep")));
```

Keep verbatim. This is the engine-side fallback that preserves the compound-pairing edge case.

### Step 2d — leave `ACUTE_MENTAL_HEALTH_QUERY_MARKERS` definition alone

Line 65-75 stays as-is. The profile now duplicates this list, but the engine-side copy is still consumed by `looksLikeAcuteMentalHealthQuery` as fallback. Duplication is intentional and documented in the commit message.

(Alternative: extract to a shared constant and reference from both. Cleaner but adds an import/package boundary. For R-cls2 scope, keep the duplication and note it in comments at both sites. A follow-on dedup slice can consolidate later if the maintenance cost is felt.)

### Step 2e — leave SUPPORT markers and hasAcuteMentalHealthSupport alone

Lines 76-92 and 1308-1326 untouched. SUPPORT is an engine concern, not a profile concern.

## Step 3 — Run focused tests

```
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.QueryMetadataProfileTest"
./gradlew.bat :app:testDebugUnitTest --tests "com.senku.mobile.OfflineAnswerEngineTest"
```

Expected:
- `QueryMetadataProfileTest`: all new tests pass (11 new); prior tests unchanged.
- `OfflineAnswerEngineTest`: all prior tests still pass. `resolveAnswerModeRoutesAcuteMentalHealthMismatchToUncertainFit` specifically should continue to pass — the refactored gate is semantically equivalent for existing fixtures because the compound-check fallback still fires.

If `OfflineAnswerEngineTest` shifts behavior on any fixture, it indicates the refactored gate isn't semantically equivalent. STOP and diagnose — most likely the profile-path AND compound-path disagree on some fixture, or the `metadataProfile` null handling differs.

## Step 4 — Full suite acceptance

```
./gradlew.bat :app:testDebugUnitTest
```

All tests green. Pre-edit pass count + new tests = post-edit pass count.

## Step 5 — Commit

```
git add android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java \
        android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java \
        android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java
git commit
```

Commit message must name:
- The new `STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH` constant + 9 phrase markers ported.
- The refactored gate's two-path logic (profile primary, engine compound fallback).
- Why SUPPORT markers stay in engine (retrieval-result concern, not query).
- Why the compound `pacing + (slept|sleep)` stays in engine (substring match, avoids over-match).
- Documented duplication of the 9 phrases between profile and engine; justification: two independent consumers, acceptable for R-cls2 scope.

## Acceptance

- Commit landed.
- Android unit suite: prior count + 11 new tests = new pass count, all green.
- `QueryMetadataProfileTest` coverage for all 9 markers + negative + full-query positive.
- `OfflineAnswerEngineTest` behavior unchanged on existing fixtures (gate is semantically equivalent).
- `OfflineAnswerEngine.resolveAnswerMode` consumes the new profile path.

## Out of scope

- Porting SUPPORT markers to profile (SUPPORT consumes retrieval surfaces, not queries — deliberately not moved).
- Changing compound-match logic or gate ordering in `resolveAnswerMode`.
- Any production change in `OfflineAnswerEngine` beyond the gate caller + signature.
- Pack regen (profile changes don't require it).
- APK rebuild / instrumentation runs (unit tests are sufficient for this slice; acute mental health state-pack cases weren't in the failing set).
- `metadataBonus` weight tuning for the new bucket.
- Removing duplicate marker list between profile and engine. File as follow-up if maintenance cost becomes real.

## STOP conditions (explicit)

- Step 0: baseline not green.
- Step 1d: any new QueryMetadataProfileTest fails.
- Step 3: any existing test regresses.
- Step 3: `resolveAnswerModeRoutesAcuteMentalHealthMismatchToUncertainFit` shifts assertion — indicates semantic non-equivalence.
- Step 4: full suite not green.

In any STOP case: report state, do not attempt recovery, wait for planner guidance.

## Estimated runtime

~30-40 min. Step 1 (profile additions + 11 tests) ~15 min. Step 2 (engine refactor) ~5 min. Steps 3-5 (validation + commit) ~10-15 min.

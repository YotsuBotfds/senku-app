# T5 — retrieval-ranking diagnostic telemetry (post-RC)

Goal: add observational telemetry to `PackRepository` so the rain_shelter probe's candidate pipeline is fully visible — where guides enter, where they drop out, what scores they carry. No semantic changes, no weight tuning, no pack regen. Pure diagnostic.

Output: evidence for exactly why GD-345 (`primitive-shelter-construction`, already tagged `structure_type="emergency_shelter"`) doesn't surface into `context.selected` on V-ret1-probe's rain_shelter query despite carrying +24 metadataBonus vs GD-727's -4.

**Dispatch shape:** single main-lane worker. No subagent grant needed (single-serial bounded re-probe). Two commits: telemetry code + JUnit-visible format test.

## Context (what V-ret1-probe evidence established)

- R-ret1's query classifier fires correctly: `search.start ... structure=emergency_shelter` in the 5556 logcat.
- GD-345 and GD-618 are currently the only guides tagged `emergency_shelter` (confirmed via python probe on the pack SQLite during R-ret1b Step 2b).
- Despite R-ret1's classification + GD-345's existing tag + the `metadataBonus +24` delta vs GD-727, `context.selected` top-4 is 3× GD-727 + 1× GD-687. No shelter-family guides surface.
- Three candidate failure points, unknown which obtains:
  1. GD-345 isn't in `lexicalHits` (115 rows) — base lexical scoring misses it.
  2. GD-345 is in `lexicalHits` but drops out before `hybridResults` (top-16) due to mergeHybrid ordering.
  3. GD-345 is in `hybridResults` but `maybeRerankResults` doesn't apply metadataBonus as expected.
- T5 adds telemetry at each of those three stages + a post-rerank stage to pinpoint the drop.

## Precondition

- HEAD at `961d478` prefix (R-ret1b Commit 1 landed) or later.
- Android unit suite green: `./gradlew.bat :app:testDebugUnitTest` passes 403/403.
- At least one emulator reachable (5556 phone-portrait preferred — mirrors V-ret1-probe's primary data source).
- SDK platform-tools adb at `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

## Step 0 — Baseline

1. `git rev-parse HEAD` returns `961d478` prefix or later.
2. `./gradlew.bat :app:testDebugUnitTest` → 403/403. Record count.
3. Verify 5556 reachable: `adb devices` lists `emulator-5556`.

## Step 1 — Add pipeline telemetry (commit 1)

Goal: log four new lines per `ask.search` invocation, each enumerating top-N candidates with their scoring decomposition. Format must be parseable by grep and greppable for a specific `guide_id`.

### Insertion points in `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`

Search method body around lines 487–690 (the hybrid-path `search`). Add telemetry at these boundaries:

1. **After `lexicalHits` is built at line 524.** Emit `search.candidates.lexical` log line enumerating top-20 (or all if fewer) ranked chunks with fields: `rank`, `guide_id`, `section_heading` (truncated to 40 chars), `score`, `structure_type`, `category`, `topic_tags` (joined, truncated to 60 chars).

2. **After `vectorHits` is built at line 656.** Emit `search.candidates.vector` log line with the same per-row fields as (1), top-20 or all.

3. **After `hybridResults` is built at line 660, BEFORE `maybeRerankResults`.** Emit `search.candidates.prerank` with the top-16 (= `limit`) that enter rerank. Same per-row fields.

4. **After `reranked` is built at line 662.** Emit `search.candidates.reranked` with final top-16 post-rerank. Per-row fields PLUS `base_score`, `metadata_bonus`, `final_score` so the scoring decomposition is visible.

### Format convention

One log line per stage, not one log line per row — keep it grep-friendly and bounded. Use a delimiter-separated inline format. Example:

```
ask.candidates.lexical query="..." n=20 rows=[1|GD-727|Practical Survival Apps|42.3|general|utility|maintenance,storage || 2|GD-345|Debris Huts|38.1|emergency_shelter|survival|shelter,debris-hut || ...]
```

Use `||` between rows, `|` between fields within a row. Escape any `|` or `]` in field content (unlikely but possible in section headings).

### Score sources

- For `search.candidates.lexical`: `RankedChunk.score` or whatever the base lexical score is (check the RankedChunk class for the field name).
- For `search.candidates.vector`: vector distance / similarity per neighbor (check `VectorStore.VectorNeighbor`).
- For `search.candidates.prerank`: the merged-hybrid score (whatever `mergeHybrid` produces per row).
- For `search.candidates.reranked`: need to compute/capture the pre-rerank score, the metadataBonus contribution, and the post-rerank score. If `maybeRerankResults` doesn't currently expose the decomposition, add a package-private overload that returns `List<RerankedResult>` where `RerankedResult` contains `SearchResult result, double baseScore, int metadataBonus, double finalScore`. Use the overload only from the telemetry-emitting path; keep the existing signature intact for callers that don't need the detail.

### Graceful degradation

- If any field is null or unavailable, emit it as empty. Do not throw — telemetry MUST NOT break the retrieval path.
- Cap log line length at ~4096 chars (logcat truncation threshold). If top-20 exceeds that, reduce to top-10 and note `truncated=true` in the line.
- Guard with `Log.isLoggable(TAG, Log.DEBUG)` so release builds can opt out via logcat configuration.

### Unit test

Add a test at `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java` (new file). At minimum:

- Test that the log format parses back cleanly: construct a small SearchResult list, invoke the telemetry emitter, assert the resulting string contains the expected `guide_id` and `score` for each row.
- Test truncation: construct a 30-row input, verify the emitter caps at the configured top-N.
- Test that an empty input produces `ask.candidates.<stage> query="..." n=0 rows=[]` and does NOT throw.

Do NOT run instrumented tests in this step — unit tests only.

### Run unit suite

```
./gradlew.bat :app:testDebugUnitTest
```

Expected: 403 + N_new tests all green, where N_new is the number of new telemetry test methods added.

### Commit 1

```
git add android-app/app/src/main/java/com/senku/mobile/PackRepository.java \
        android-app/app/src/test/java/com/senku/mobile/PackRepositoryTelemetryTest.java
git commit
```

Commit message body names:
- The four new telemetry tags (`search.candidates.lexical`, `.vector`, `.prerank`, `.reranked`).
- The format convention (rows delimiter `||`, fields `|`, bounded at ~4096 chars).
- Why: V-ret1-probe showed GD-345 doesn't surface despite being tagged; three candidate failure points need observation before R-ret1c weight tuning can be shaped.
- Explicit note: NO semantic change. Pure telemetry. `maybeRerankResults` caller-facing signature unchanged.

## Step 2 — APK rebuild + single-serial install

1. `./gradlew.bat :app:assembleDebug`
2. New APK SHA must differ from `d34f17546e65fd27d125c96fbaba70eb40bd31941024e8c5be98f4d3758302de` (V-ret1-probe baseline). No pack asset change, so delta comes from PackRepository edits only.
3. Install on emulator-5556 only:
   ```
   adb -s emulator-5556 install -r <app-debug.apk path>
   adb -s emulator-5556 install -r <app-debug-androidTest.apk path>
   ```
   (androidTest APK unchanged source-wise; rebuild optional but install-r is fine either way.)
4. Write `artifacts/postrc_t5_<timestamp>/provision.json` with old/new APK SHAs and install confirmation.

STOP if:
- New APK SHA equals V-ret1-probe's `d34f1754…` (build didn't pick up telemetry change).
- Install fails.

## Step 3 — Probe execution with new telemetry

Run the same `generativeAskWithHostInferenceNavigatesToDetailScreen` test on emulator-5556 only. Capture logcat.

```
adb -s emulator-5556 logcat -c
adb -s emulator-5556 logcat -v threadtime > artifacts/postrc_t5_<ts>/logcat_5556.txt &
adb -s emulator-5556 shell am instrument -w \
  -e class com.senku.mobile.PromptHarnessSmokeTest#generativeAskWithHostInferenceNavigatesToDetailScreen \
  -e hostInferenceEnabled false \
  com.senku.mobile.test/androidx.test.runner.AndroidJUnitRunner
# (host-inference args match V-ret1-probe 5556 row — on-device E4B, host disabled)
```

Capture the UI dump after instrumentation completes: `adb -s emulator-5556 exec-out uiautomator dump /dev/tty > dump_5556.xml`.

Stop logcat capture.

STOP if instrumentation fails to start. Expect the same test failure at `:2794` — NOT a stop condition for this slice (we're observing, not fixing).

## Step 4 — Extract evidence from the telemetry

Grep the logcat for the four new tags and record the top-20 (or however many emitted) candidates at each stage:

```
grep "search.candidates.lexical" logcat_5556.txt
grep "search.candidates.vector" logcat_5556.txt
grep "search.candidates.prerank" logcat_5556.txt
grep "search.candidates.reranked" logcat_5556.txt
```

For each of the four stages, write a per-stage table in `artifacts/postrc_t5_<ts>/ranking_trace.md`:

| Stage | Rank | guide_id | section | score | structure_type | category | (reranked only) base / bonus / final |
| --- | --- | --- | --- | --- | --- | --- | --- |
| lexical | 1 | GD-? | ... | ... | ... | ... | — |
| ... | | | | | | | |

Specifically answer these four questions in the writeup:

1. **Does GD-345 appear in `search.candidates.lexical` (top-20 lexical by base score)?** If yes, what rank and score? If no, lexical scoring itself is the blocker — the probe's tokens don't match GD-345's indexed content strongly enough.

2. **Does GD-345 appear in `search.candidates.vector` (top-N vector neighbors)?** If yes, rank and similarity. If no, GD-345's vectors are far from the query centroid.

3. **Does GD-345 survive into `search.candidates.prerank` (top-16 hybrid)?** If no, mergeHybrid ordering drops it before rerank has any chance. If yes, what rank does it enter at?

4. **Does `search.candidates.reranked` boost GD-345 meaningfully?** If GD-345 enters rerank at rank 10 with base_score=X, metadata_bonus=+24, final_score=X+24, where does it land post-rerank? If still below rank 4 (context.selected's cutoff), the metadataBonus weight is too small relative to the base score gap.

Also note the other shelter-family guides (accessible-shelter-design, cave-shelter-systems, shelter-site-assessment, etc.) — do any of them appear at any stage? Their absence confirms the broader emergency_shelter tagging coverage gap independent of the GD-345 question.

## Step 5 — Summary + remediation recommendation

Write `artifacts/postrc_t5_<ts>/summary.md` with:

1. Pre/post APK SHAs.
2. Commit SHA for telemetry addition.
3. The four per-stage tables.
4. Answers to the four questions in Step 4.
5. Planner recommendation, drawn from one of these patterns based on evidence:
   - **If GD-345 never enters lexicalHits:** lexical scoring is the blocker. Candidate remediation = index-side (pack ingestion adds more queryable tokens) or query-side (token expansion). Not weight tuning.
   - **If GD-345 is in lexicalHits but drops before hybridResults:** mergeHybrid logic weights vector over lexical too heavily, or lexical score is crowded by off-route guides. Remediation = adjust mergeHybrid scoring or pre-rerank cutoff rules.
   - **If GD-345 enters hybridResults but stays below rank 4 post-rerank:** metadataBonus weight too small vs base gap. Remediation = R-ret1c weight tuning (bump structure-match +18 to higher, or add a category×structure multiplier).
   - **If GD-345 enters hybridResults AND reranks to top-4 (contra V-ret1-probe):** something is different between this run and V-ret1-probe's. Diagnose the delta.
6. Per-shelter-family-guide visibility table (which tagged or untagged shelter guides showed up at which stage). This informs whether R-ret1b coverage expansion (post-revision with corpus-vetted markers) is the right follow-on.

## Acceptance

- Commit 1 (telemetry + tests) landed.
- New APK installed on emulator-5556.
- Logcat captures all four new telemetry lines for the probe.
- `ranking_trace.md` tables filled for all four stages.
- `summary.md` has the four-question answers and planner recommendation.
- Android unit suite green (403 + N_new).

## Out of scope

- Any scoring weight change (reserved for R-ret1c based on T5 evidence).
- Any retrieval pipeline refactor (reserved based on T5 evidence).
- Any pack regen (not needed — R-ret1b Commit 1 is independent, can stay committed or be revised later).
- R-gal1 drafting.
- Multi-serial fan-out. Single serial (5556) is sufficient for diagnostic; the pipeline is deterministic on substrate so one serial suffices.
- Any R-ret1b scope revision (corpus-vetted markers). That's a parallel decision, informed but not gated by T5.

## STOP conditions (explicit)

- Step 0: baseline unit suite not 403/403, or 5556 unreachable.
- Step 1 unit tests: new tests fail or any prior test regresses.
- Step 1 compilation: if adding the `RerankedResult` overload requires broader refactor than expected, STOP and report — the slice can be downscoped to just pre-rerank telemetry without the decomposition overload.
- Step 2: new APK SHA equals V-ret1-probe's `d34f1754…`.
- Step 3: instrumentation fails to start, OR telemetry lines don't appear in logcat (would mean the insertion points were wrong).
- Step 4: any stage's log line is truncated such that GD-345 (or whichever guide we're tracking) is not visible — retry with reduced top-N (10 instead of 20).

In any STOP case: report state, wait for planner guidance.

## Notes

- The `RerankedResult` overload idea in Step 1 may or may not fit the current `maybeRerankResults` signature. If it's awkward, alternative approaches: (a) expose a static helper that computes the metadataBonus given a SearchResult and the profile, called from both the telemetry emitter and the rerank internal; (b) instrument `maybeRerankResults` internals to emit the decomposition during its own execution. Codex picks the least-invasive path.
- Estimated runtime: Step 1 ~15 min (four insertion points + tests), Step 2 ~5 min, Step 3 ~3 min, Step 4–5 ~10 min writeup. Total ~35 min.

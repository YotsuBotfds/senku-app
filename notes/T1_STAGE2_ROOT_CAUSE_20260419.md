# T1 Stage 2 Root Cause - 2026-04-19

Scope:
- Read-only diagnosis for the CP9 Stage 2 RED verdict in `artifacts/cp9_stage2_20260419_185102/summary.md`.
- No emulator, APK, pack, or model interaction beyond reading existing artifacts and pack/db contents.
- This note localizes likely causes and proposes remediation slices. It does not fix them.

## Failure 1 - `confident_rain_shelter` (Track 3)

Observed:
- Stage 2 expected a normal generated answer for `How do I build a simple rain shelter from tarp and cord?`
- The artifact routed to `ask.uncertain_fit` instead, even though selected context included shelter-relevant material such as GD-933 and GD-294.

Evidence:
- The mode gate lives in `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java:287-339` and `:1178-1206`.
- `resolveAnswerMode(...)` checks `shouldAbstain(...)`, then `averageRrfStrength(...)`, then `topVectorSimilarity(...)`, then a safety fallback, and otherwise returns `CONFIDENT`.
- Those checks run on `modeCandidates`, which are the raw answer candidates, not the finalized selected context (`OfflineAnswerEngine.java:287-299`).
- The scoring helpers use the top 3 retrieval rows via `topAbstainChunks(...)` (`OfflineAnswerEngine.java:1043-1058`, `:1208-1300`).
- The artifact log shows an irrelevant top hit, `GD-727` Batteries, before the relevant shelter guides, then `ask.uncertain_fit` fires immediately afterward:
  - `artifacts/cp9_stage2_20260419_185102/validation_5556/emulator-5556/confident_rain_shelter/logcat.txt:15-21`
- The same log shows selected context later includes relevant shelter material, but that happens after anchor/context selection, not in the mode gate:
  - `.../confident_rain_shelter/logcat.txt:17-20`

Root cause:
- This is primarily a retrieval-to-mode coupling problem, not a rendering bug.
- The engine decides confidence from the raw top retrieval rows, so one off-topic high-ranked row can drag the query into `UNCERTAIN_FIT` even when `context.selected` is good enough for generation.
- The current thresholds in `OfflineAnswerEngine.java:29-31` are behaving as designed; the design problem is that they are applied to the wrong evidence surface.

Localized cause:
- Specific code path: `OfflineAnswerEngine.resolveAnswerMode(...)` and its use of `modeCandidates` / `topAbstainChunks(...)` instead of finalized context.
- Specific contributing pack/retrieval behavior: the hybrid ranking allowed `GD-727` to win rank 1 for a shelter query.

Dominant cause call:
- Dominant: retrieval relevance issue feeding the confidence gate.
- Secondary: threshold sensitivity in `averageRrfStrength(...)` makes the gate too easy to trip once an irrelevant top row lands.

## Failure 2 - `abstain_violin_bridge_soundpost` (Track 1)

Observed:
- Stage 2 expected an abstain card for `how do i tune a violin bridge and soundpost`.
- The artifact rendered a normal generated paper card, then logged `ask.generate low_coverage_detected` after generation completed.

Evidence:
- The mode decision tree is in `OfflineAnswerEngine.java:294-321` and `:1178-1206`.
- `shouldAbstain(...)` in `OfflineAnswerEngine.java:1346-1372` refuses to abstain if any top-3 row is marked `hybrid` because it sets `strongSemanticHit = true`.
- The artifact log shows exactly that pattern: generic hybrid retrieval, no route focus, and top results dominated by civil and dental `bridge` senses:
  - `.../abstain_violin_bridge_soundpost/logcat.txt:15-18`
- The same log shows `promptContext=2`, meaning generation only saw the first two selected context rows:
  - `.../abstain_violin_bridge_soundpost/logcat.txt:21`
- `promptContextLimitFor(...)` simply uses the route profile's default preferred context item count (`OfflineAnswerEngine.java:749-757`), so this ambiguous query only got 2 prompt rows.
- `low_coverage_detected` is computed after generation and only changes the subtitle, not the answer mode or card route (`OfflineAnswerEngine.java:586-595`).
- The pack does contain GD-191 instrument content, but there are zero `soundpost` hits anywhere in the shipped pack DB:
  - `artifacts/mobile_pack/senku_20260419_cp9_stage1_rcv3_20260419_181929/senku_mobile.sqlite3`
  - GD-191 has chunks, including `Musical Instrument Building`, but SQLite probes found `soundpost=0`.
- Commit `aa2373c` is not a mobile retrieval change. It touches `ingest.py` and a bridge-tag audit test only, so it is not the direct source of this regression.

Interpretation:
- This query is a real knowledge-gap / no-evidence shape for `soundpost`, but the mobile gate mistakes wrong-sense hybrid `bridge` hits for meaningful semantic support.
- Once those hybrid bridge hits land, `shouldAbstain(...)` cannot fire.
- The later `averageRrfStrength(...)` and `topVectorSimilarity(...)` logic also over-trust those same hybrid rows, so the route remains generative instead of downgrading.
- The final generation is then built from only 2 prompt rows, which in the artifact are wrong-sense bridge context, so the model never had a chance to recover.

Localized cause:
- Code-side:
  - `OfflineAnswerEngine.shouldAbstain(...)` treats any hybrid top-row as a semantic veto on abstain (`:1346-1372`).
  - `OfflineAnswerEngine` consumes `low_coverage_detected` too late and too weakly (`:586-595`).
  - `promptContextLimitFor(...)` plus `promptContext=2` narrows the final prompt to the wrong-sense anchor (`:342-345`, `:749-757`).
- Retrieval-side:
  - `PackRepository` generic hybrid ranking surfaced `GD-110` and `GD-061` for the token `bridge` and did not meaningfully disambiguate the instrument sense before anchor selection.

What was ruled out:
- This is not a missing-GD-191 pack regression. GD-191 is present and chunked.
- `aa2373c` does not appear to have changed mobile scoring directly.

Dominant cause call:
- Combination, with the dominant RC-blocking cause on the code side: the abstain/confidence gate over-trusts hybrid rows and ignores the post-generation low-coverage signal.
- Contributing retrieval issue: word-sense conflation on `bridge` causes the wrong rows to reach that gate in the first place.

## Failure 3 - `safety_abstain_poisoning_escalation` (Track 2)

Observed:
- Stage 2 expected abstain plus safety escalation for `my child may have poisoning after swallowing drain cleaner`.
- The artifact produced `structure=cabin_house explicitTopics=[wall_construction]`, then routed to `ask.uncertain_fit` with the escalation line.

Evidence:
- The bad metadata is logged at retrieval start:
  - `artifacts/cp9_stage2_20260419_185102/validation_5556/emulator-5556/safety_abstain_poisoning_escalation/logcat.txt:12-21`
- `PackRepository.QueryTerms.fromText(...)` always builds `QueryMetadataProfile.fromQuery(query)` before search (`android-app/app/src/main/java/com/senku/mobile/PackRepository.java:5378-5431`).
- In `QueryMetadataProfile.detectStructureType(...)`, if no structure-specific marker matched, it falls back to `HOUSE_PROJECT_MARKERS` and returns `cabin_house` (`QueryMetadataProfile.java:1346-1385`).
- `HOUSE_PROJECT_MARKERS` includes `wall` (`QueryMetadataProfile.java:46-50`).
- `detectTopicTags(...)` uses the same marker family, and `wall_construction` also includes `wall` (`QueryMetadataProfile.java:1431-1444`, `:1505-1506`).
- `containsAny(...)` in `QueryMetadataProfile` is plain substring matching, not token-aware matching:
  - `QueryMetadataProfile.java:1620-1624`
- Therefore `swallowing` contains the substring `wall`, which falsely produces both `cabin_house` and `wall_construction`.
- The passing mania case does not show this metadata corruption; its `structure=` is empty and `explicitTopics=[]`:
  - `artifacts/cp9_stage2_20260419_185102/validation_5556/emulator-5556/safety_uncertain_fit_mania_escalation/logcat.txt:12`

Secondary evidence:
- Desktop already has explicit safety handling for household chemical / poisoning questions and uses token-aware marker helpers in `query.py`, including:
  - `_is_household_chemical_hazard_query(...)`
  - `_scenario_frame_is_safety_critical(...)`
  - `_text_has_marker(...)`
- Mobile does not have an equivalent dedicated poisoning metadata branch before retrieval.

Additional pack/db finding:
- Four highly relevant poisoning guides exist in `guides`, but have zero retrievable chunks in both the shipped mobile pack DB and the local lexical DB:
  - GD-898 `Unknown Ingestion & Child Accidental Poisoning Triage`
  - GD-301 `Toxicology and Poisoning Response`
  - GD-054 `Toxicology & Poison Management`
  - GD-602 `Toxidromes & Specific Antidotes for Field Poisoning`
- Read-only probes against:
  - `artifacts/mobile_pack/senku_20260419_cp9_stage1_rcv3_20260419_181929/senku_mobile.sqlite3`
  - `db/senku_lexical.sqlite3`
  both returned `0` chunk rows for those guide IDs.
- I could not localize that zero-chunk state to a single code point without rerunning ingest, but I did rule out guide-format failure: `ingest.process_file(...)` produces normal chunk counts from the source markdown for all four guides.

Root cause:
- Primary RC cause: a positive substring-matching bug in mobile metadata classification. `swallowing` trips the `wall` marker, which misroutes the query into house-construction metadata before retrieval even starts.
- Secondary RC cause: the current DB/pack is missing retrievable chunk rows for the most relevant poisoning guides, so even a fixed classifier would still be pulling from a weakened safety corpus.

Localized cause:
- Specific code point for the metadata bug: `QueryMetadataProfile.containsAny(...)` plus the `wall` markers in `HOUSE_PROJECT_MARKERS` and `wall_construction`.
- Specific unresolved data issue: source markdown is chunkable, but current db/pack chunk tables are missing those poisoning guide rows.

Dominant cause call:
- Dominant: code-side classifier bug in `QueryMetadataProfile`.
- Secondary but still RC-relevant: ingest/db state is missing the main poisoning guide chunks.

## Cross-Cutting Findings

1. Mobile mode decisions are using the wrong evidence surface.
   - `OfflineAnswerEngine` decides `confident` / `uncertain_fit` / `abstain` from raw retrieval tops, not from the finalized selected context. That is load-bearing in both rain-shelter and violin.

2. Mobile metadata classification is more brittle than desktop safety routing.
   - Android `QueryMetadataProfile` still uses raw substring matching for marker detection.
   - Desktop `query.py` already has boundary-aware safety marker helpers and more explicit poisoning handling.

3. Post-generation low-coverage is only observational.
   - The engine already knows when a generated answer is low coverage, but that signal does not gate the visible answer route. That is why the violin case still renders as a normal answer after `ask.generate low_coverage_detected`.

4. Safety guide availability in the current db/pack is not trustworthy.
   - The poisoning guide family is present as guide metadata but absent from retrievable chunk rows. That is a content-index integrity problem, not just a ranking problem.

5. Threshold tuning alone is the wrong fix.
   - Raising or lowering `UNCERTAIN_FIT_*` thresholds may move rain-shelter, but it will not fix the poisoning classifier bug and will not stop the violin hallucination while `shouldAbstain(...)` still treats any hybrid top-row as a semantic veto.

## Recommended Remediation Scope

1. Harden mobile metadata marker matching and add a poisoning-specific metadata branch.
   - Addresses: Failure 3 directly.
   - Type: code edit in `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java` plus targeted tests.
   - Scope: replace raw substring marker checks with token-aware / phrase-aware matching; add explicit poisoning / household chemical / child-ingestion markers that outrank generic house-project fallbacks.
   - Effort: 4-8 hours.
   - RC status: must land before S3.

2. Fix the mobile abstain/confidence gate so wrong-sense hybrid hits do not block abstain by themselves.
   - Addresses: Failure 2 directly; helps Failure 1.
   - Type: code edit in `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java` plus tests.
   - Scope: stop using `any hybrid row => strong semantic hit`; require stronger evidence than retrieval mode alone; consider using the finalized selected context or explicit overlap/owner-support evidence for the abstain veto.
   - Effort: 0.5-1 day.
   - RC status: must land before S3.

3. Promote `low_coverage_detected` from telemetry-only to route-affecting behavior, or delete its current false reassurance.
   - Addresses: Failure 2 directly.
   - Type: code edit in `OfflineAnswerEngine.java` and possibly answer-content plumbing/UI tests.
   - Scope: when low coverage is detected on a no-evidence shape, convert the result to `uncertain_fit` or `abstain` instead of leaving a normal answer card.
   - Effort: 4-8 hours if kept narrow; longer if done as a larger answer-mode refactor.
   - RC status: likely required unless slice 2 fully closes the violin path pre-generation.

4. Rebuild the poisoning guide family into the retrievable db/pack and root-cause why chunk rows are missing.
   - Addresses: Failure 3 directly; future safety queries indirectly.
   - Type: data/pack repair, probably involving ingest/db rebuild plus validation.
   - Scope: explain why GD-898 / GD-301 / GD-054 / GD-602 have zero chunk rows in current db/pack even though `ingest.process_file(...)` produces chunks, then ship a rebuilt db/pack that includes them.
   - Effort: 2-6 hours if it is stale ingest state; 1-2 days if export/build logic is corrupting them.
   - RC status: must land before S3 because it affects safety-content retrieval integrity.

5. Rework confidence scoring to operate on finalized context or a context-aware summary, not raw top retrieval rows.
   - Addresses: Failure 1 directly; Failure 2 indirectly.
   - Type: code edit across `OfflineAnswerEngine.java` and `PackRepository.java`, with tests.
   - Scope: compute confidence from the evidence actually fed to prompting, or explicitly discount irrelevant top-row noise before the mode gate runs.
   - Effort: 1-2 days.
   - RC status: required if slice 2 alone does not also clear `confident_rain_shelter`.

## Planner Read

The right RC-shaped move is a two-front repair, not a threshold tweak: first fix the poisoning classifier bug and the missing poisoning chunk rows, then harden the mobile abstain/confidence path so wrong-sense hybrid hits cannot force generation on a no-evidence query. The tempting shortcut is to retune the uncertain-fit thresholds or bump prompt-context limits, but that would only move the rain-shelter symptom around while leaving the RC-blocking violin hallucination and the safety-critical poisoning misrouting fundamentally unresolved.
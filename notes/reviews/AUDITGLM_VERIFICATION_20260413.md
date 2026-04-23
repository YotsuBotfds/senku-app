# auditglm.md Verification And Reconciliation Plan

Date: 2026-04-13
Scope: verification of [`auditglm.md`](./auditglm.md) against the current `android-app/` tree

## Summary

[`auditglm.md`](./auditglm.md) mixes three kinds of findings:

1. Confirmed defects or cleanup items that are visible in the repo now.
2. Real concerns whose severity or wording is overstated.
3. Stale or unsupported claims that should be corrected before using the audit as a roadmap.

The fastest reconciliation path is:

- keep the clearly confirmed code fixes
- downgrade or rewrite the speculative concurrency/security claims
- correct the factual scaffolding in the audit itself
- then decide whether to implement the larger architecture/perf wishlist

## Confirmed Claims

These are directly supported by the current code:

- `LiteRtModelRunner.generate()` is unsynchronized while `close()` and `ensureEngine()` are synchronized, so the reported engine-lifecycle race is plausible. See `LiteRtModelRunner.java:32-49,109-117`.
- `MainActivity` wires both `OnEditorActionListener` and `OnKeyListener` to call `runSearch()`, so duplicate Enter-triggered searches are plausible. See `MainActivity.java:127-141`.
- `PackRepository` returns `routeResults.subList(...)` directly in one path. That is a live view, not a copy. See `PackRepository.java:296-306`.
- `PackRepository` has unreachable duplicate `guide-focus` logic in the support-scoring branch. See `PackRepository.java:595-602`.
- `ModelFileStore` contains hardcoded `/storage/emulated/0/...` and `/sdcard/...` fallbacks. See `ModelFileStore.java:134-143`.
- `HostInferenceConfig.normalizeBaseUrl()` simply trims trailing slashes and appends `/v1`, so a path like `/v1/chat/completions` would indeed normalize incorrectly. See `HostInferenceConfig.java:73-82`.
- `HostInferenceClient` uses raw `Socket` HTTP, no TLS path, and a 15-minute read timeout. See `HostInferenceClient.java:22,29-112`.
- `SearchResultAdapter` performs a no-op replace and resolves colors on each view bind. See `SearchResultAdapter.java:97,100-129`.
- `ChatSessionStore` is access-ordered but unbounded. See `ChatSessionStore.java:8-18`.
- `OfflineAnswerEngine` uses boxed `Integer` for `MODEL_MAX_TOKENS`. See `OfflineAnswerEngine.java:20`.
- `PackManifest.fromJson()` uses `opt*` for required manifest fields. See `PackManifest.java:57-80`.
- `DetailActivity` swallows exceptions in source navigation. See `DetailActivity.java:502-511`.
- `PackInstaller` has an unused `TextUtils` import, no checksum verification after asset copy, and an effectively unreachable `lastError == null` fallback. See `PackInstaller.java:5,47-56,93-111`.
- `QueryMetadataProfileTest.waterStorageContinuationPrefersRotationOverHistoricalContext` is missing `@Test`. See `QueryMetadataProfileTest.java:348`.
- `VectorStore.findNearest()` is brute-force over every row, and float16 dot products read element-by-element from `MappedByteBuffer`. See `VectorStore.java:93-129`.
- `SearchResult` is `Serializable` and does not define `equals()`/`hashCode()`. See `SearchResult.java:3-65`.
- `strings.xml` hardcodes `Search 692 guides...` while the app already has runtime manifest counts elsewhere. See `strings.xml:5` and `MainActivity.java:495-508`.
- The manifest enables cleartext traffic globally. See `AndroidManifest.xml:6-11`.
- Release minification is disabled, there is no signing config, and `noCompress` omits `.litertlm` and `.task`. See `app/build.gradle:17-31`.
- Accessibility/layout concerns are broadly supported by the current layouts: there are no `contentDescription`, `labelFor`, `accessibilityLiveRegion`, `importantForAccessibility`, `autofillHints`, `layout-land`, `sw600dp`, or `values-night` resources in the main app tree, and the session panel in `activity_main.xml` is hardcoded to `132dp`. See `activity_main.xml`, `activity_detail.xml`, `list_item_result.xml`.

## Overstated Or Needs Rewording

- `MainActivity.repository` publication risk is worth reviewing, but the audit states it as a proven critical crash. The write happens before a `runOnUiThread(...)` handoff, which adds synchronization via the main looper/handler path. The safer phrasing is "thread publication should be made explicit" rather than "critical confirmed NPE". Relevant lines: `MainActivity.java:183-205,211-260`.
- The `LiteRtModelRunner` race is plausible, but it is still a race hypothesis until reproduced or guarded by tests. It belongs in the near-term fix list, but the confidence is lower than the audit tone suggests.
- The FTS item should be described as "query-shaping or FTS syntax risk" rather than SQL injection. The code uses bound parameters for `MATCH ?`; it is not concatenating raw user text straight into SQL. See `PackRepository.java:1238-1247,1578-1587,2872-2881`.
- "Only 10 of 96 claimed rules implemented" is directionally useful but incomplete. The mobile pack manifest declares `96` deterministic rules and even ships a `deterministic_rules` table, but the Android runtime only appears to use `DeterministicAnswerRouter.match(...)` plus the manifest count; nothing in `android-app` reads the `deterministic_rules` table. That means the real issue is "pack/runtime mismatch or unconsumed rule inventory", not just "GLM counted methods". Evidence: `senku_manifest.json:2-8,52-58`, `MainActivity.java:220-223,495-503`, `OfflineAnswerEngine.java:361-375`, `PackManifest.java:57-80`.

## Incorrect Or Stale Claims

- The `DetailActivity` "same cross-thread race on repository" claim does not match the code path cited. `ensureRepository()` is called and consumed on the executor thread, not published from one thread and read from the UI thread in the way described. See `DetailActivity.java:305-340,502-505`.
- The audit says `MainActivity` uses `shutdown()` in `onDestroy()`. It actually uses `shutdownNow()`. See `MainActivity.java:668-674`.
- The audit says there are eight duplicated `safe()` helpers across the listed files. The current tree has seven local `safe()` helpers in `AnswerContextSelector`, `DetailActivity`, `HostInferenceConfig`, `OfflineAnswerEngine`, `PromptBuilder`, `SearchResultAdapter`, and `SessionMemory`. There is no matching helper in `MainActivity`, `QueryRouteProfile`, `QueryMetadataProfile`, `DeterministicAnswerRouter`, or `HostInferenceClient`.
- The audit's headline metrics are off. The file counts match, but the current tree measures `12510` source lines and `5980` test lines, not `~10200` and `~6800`. Largest-file line counts are also different: `PackRepository.java` is `4244` lines, not `~3600`.

## Reconciliation Plan

### Phase 1: Fix the audit itself

- Update the metrics section with current measured line counts.
- Reclassify `MainActivity.repository` from "critical confirmed crash" to "publication cleanup / low-confidence concurrency risk".
- Remove the incorrect `DetailActivity.repository` race claim.
- Remove the stale `MainActivity.shutdown()` note.
- Rewrite the FTS security note to separate SQL injection from FTS query-shaping concerns.
- Replace the deterministic-rule claim with a more precise statement: the pack advertises 96 rules, but the app currently appears to consume only the hardcoded Java router and manifest count.

### Phase 2: Ship the clearly confirmed low-risk fixes

- Synchronize or otherwise guard `LiteRtModelRunner.generate()` against `close()`.
- Copy the `subList()` return in `PackRepository`.
- Remove the duplicate `guide-focus` branch in `PackRepository`.
- Fix `HostInferenceConfig.normalizeBaseUrl()`.
- Remove hardcoded external model paths from `ModelFileStore`.
- Remove the duplicate Enter-trigger path in `MainActivity`.
- Cache adapter colors and remove the no-op string replace in `SearchResultAdapter`.
- Add the missing `@Test` annotation in `QueryMetadataProfileTest`.
- Log the swallowed exception in `DetailActivity`.
- Remove the unused `TextUtils` import.

### Phase 3: Reconcile pack/runtime mismatches

- Decide whether Android should consume the packaged `deterministic_rules` table or whether the manifest count should only describe pack contents, not live runtime behavior.
- If the table is meant to be live, add a repository-backed deterministic rule loader and tests.
- If the hardcoded router is intentional for now, stop surfacing the full pack rule count as if it represents active Android routing coverage.

### Phase 4: Harden the app where the audit is broadly right

- Add explicit checksum verification in `PackInstaller` using the manifest SHA-256 values already shipped in `senku_manifest.json`.
- Bound `ChatSessionStore`.
- Tighten manifest parsing for required fields.
- Restrict cleartext traffic with a `network_security_config`.
- Add `.litertlm` and `.task` to `noCompress`.
- Replace the hardcoded `search_hint` count with runtime text or a generic label.

### Phase 5: Defer large changes until parity priorities are clearer

These may still be good ideas, but they should not block the correctness cleanup above:

- `RecyclerView` migration
- `ViewModel` / DI adoption
- `PackRepository` decomposition
- ANN search in `VectorStore`
- OkHttp migration for host inference
- broad accessibility and alternate-layout pass

## Suggested Next Action

Treat [`auditglm.md`](./auditglm.md) as a draft, not a source of truth. Reconcile the document first, then land the small confirmed fixes in one focused Android cleanup pass, then make an explicit product decision on whether mobile deterministic behavior should be driven by the packaged rule table or by the current hand-coded router.

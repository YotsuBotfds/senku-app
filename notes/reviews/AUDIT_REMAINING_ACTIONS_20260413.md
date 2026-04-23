# Android Audit Remaining Actions

Date: 2026-04-13
Purpose: handoff note for any follow-on agent working from `auditglm.md` and the verification/reconciliation work already completed.

## Read This First

Before acting on the original audit, also read:

- [`AUDITGLM_VERIFICATION_20260413.md`](./AUDITGLM_VERIFICATION_20260413.md)

That verification note identifies which original audit claims were:

- confirmed
- overstated
- incorrect or stale

Do not reopen the disproven items below unless new evidence appears.

## Already Completed

These audit-driven items have already been implemented:

- synchronized `LiteRtModelRunner.generate()`
- made `MainActivity` repository/pack publication more explicit and removed duplicate Enter search triggering
- copied `PackRepository` route-result `subList()` returns
- removed duplicate `guide-focus` scoring branch
- removed hardcoded model fallback paths from `ModelFileStore`
- fixed host base URL normalization for `/v1/chat/completions` style inputs
- cached adapter colors and removed the no-op subtitle replace
- logged swallowed source-navigation exceptions in `DetailActivity`
- tightened `PackManifest` required field parsing
- removed unused `PackInstaller` import and simplified the unreachable retry fallback
- changed hardcoded search hint to generic text
- added `.litertlm` / `.task` to `noCompress`
- bounded `ChatSessionStore`
- added pack file size + SHA-256 verification after asset install
- restricted cleartext traffic with `network_security_config`
- clarified pack rule count versus active Android instant rules in the app summary
- added targeted accessibility improvements to key layouts
- preserved more deterministic registry metadata in the mobile pack export
- low-risk hygiene fixes:
  - `OfflineAnswerEngine.MODEL_MAX_TOKENS` is now primitive `int`
  - `QueryMetadataProfile` roofing regexes are precompiled
  - `AnswerContextSelector.addRemaining()` now uses a set for duplicate checks
  - `SearchResult` now implements `equals()` / `hashCode()` / `toString()`

Validation already run after the latest Android tranche:

- `android-app\gradlew.bat testDebugUnitTest`

Validation already run for the pack export tranche:

- `python -m unittest tests.test_mobile_pack -v`

## Remaining Valid Work

### Do Next If Another Agent Is Continuing Android Hardening

- **Host inference transport hardening**
  - Why: the app still uses raw socket HTTP and has no HTTPS client path.
  - When: next Android networking pass.
  - Suggested direction: replace manual socket HTTP with `HttpURLConnection` or `OkHttp`, then keep the local emulator cleartext carve-out only as a dev path.

- **Release build hardening**
  - Why: `minifyEnabled false` and no signing config are still unresolved.
  - When: only when someone can test an actual release build, not as an isolated blind patch.
  - Suggested direction: enable minification carefully, add keep rules only after shrinker validation, and add release signing from local secure config rather than hardcoding credentials.

- **Accessibility follow-up**
  - Why: the recent pass covered obvious status/input/decorative issues, but the app still lacks a full accessibility review.
  - When: next UI polish pass or before broader user-facing Android testing.
  - Suggested direction: audit touch targets, talkback reading order, button descriptions, and color-only badge semantics.

### Do Only After A Richer Pack Export Exists

- **True desktop/mobile deterministic parity**
  - Why: the pack currently exports deterministic registry metadata, but not executable predicates/build payloads.
  - When: after pack/export format changes on the desktop side.
  - Important: do not try to fake this by inferring Android behavior from `rule_id` alone.
  - Required export capability:
    - either a compiled deterministic payload Android can execute directly
    - or a generated Android-native source artifact produced from the desktop registry/builders

- **Replace the handwritten Android deterministic router with generated/shared deterministic artifacts**
  - Why: the current `DeterministicAnswerRouter` still only covers a small handwritten subset.
  - When: same milestone as the richer pack export above.

### Do In A Focused Performance / Retrieval Pass

- **`VectorStore` brute-force nearest-neighbor search**
  - Why: still `O(n*d)` and a likely dominant CPU cost.
  - When: only if mobile retrieval latency becomes a measurable blocker.
  - Suggested direction: benchmark before replacing, then consider ANN or a smaller targeted acceleration.

- **`PromptBuilder` normalization pass count**
  - Why: still a low-to-medium micro-optimization item, but not urgent.
  - When: only after correctness and retrieval quality work is stable.

- **`ListView` to `RecyclerView` migration**
  - Why: architectural/UI modernization, not a hot bug fix.
  - When: as part of a broader UI pass, not a one-off audit cleanup.

### Do In A Larger Architecture Refactor Only

- **`PackRepository` decomposition**
  - Why: real maintainability issue, but large and cross-cutting.
  - When: dedicated refactor lane only.

- **Dependency injection / `ViewModel` adoption**
  - Why: worthwhile, but not justified as piecemeal audit cleanup.
  - When: broader Android architecture sprint.

- **Shared `safe()` utility extraction**
  - Why: minor duplication remains, but this is not worth churn by itself.
  - When: opportunistically during nearby file edits.

## Items The Original Audit Mentioned But Should Not Drive New Work

These should be treated as closed or invalid unless fresh evidence appears:

- `DetailActivity.repository` was described as the same critical cross-thread race as `MainActivity`
  - Verification found the cited usage pattern did not support that claim.

- `MainActivity` was described as using `shutdown()` in `onDestroy()`
  - It already used `shutdownNow()`.

- The audit’s deterministic count complaint was too blunt
  - The real issue is pack/runtime mismatch, not just “wrong count”.

- The audit’s line-count metrics were stale
  - Do not use them for planning estimates.

## Good Next Agent Starting Points

If another agent picks this up, the best next concrete branches are:

1. Android networking hardening for host inference.
2. Release build hardening with actual release-build validation.
3. Deterministic shared-payload design between desktop export and Android runtime.
4. The external corpus-gap findings are now merged into [`../../GUIDE_PLAN.md`](../../GUIDE_PLAN.md) as the running guide-direction backlog.
   - Use that file for:
     - true missing topics
     - guide-expansion / actionability gaps
     - retrieval-answerability gaps
   - Keep deterministic parity planning in Android/runtime notes rather than the guide backlog.
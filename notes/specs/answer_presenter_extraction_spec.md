# OPUS-E-06 — AnswerPresenter Carve-out (Wave B Gate)

Date: 2026-04-18
Owner: Opus planner
Worker tier: `gpt-5.4` `high`
Estimate: M (one focused worker session, no UI behavior change)
Spec status: drafted, ready for dispatch
Related: `OPUS-E-05` (Presenter extraction, View layer landed; Controller
layer not yet landed), Wave B (`BACK-U-01`, `BACK-U-02`, `BACK-U-03`)

## Problem

`OPUS-E-05` shipped the View-layer half of the DetailActivity refactor — the
sixteen `Detail*PresentationFormatter` files in
`android-app/app/src/main/java/com/senku/mobile/`. The Controller-layer half
is not done: there is no `*Presenter.java` file in that package, and
`OfflineAnswerEngine.generate()` is still invoked directly from
`DetailActivity.java` at three sites (lines 2939, 3064, 3186).

Wave B (BACK-U-01 / U-02 / U-03) cannot dispatch safely while those three
sites live in the Activity. All three Wave B tasks plumb new fields through
the `AnswerRun` → render path that crosses `DetailActivity.java` and would
collide with any concurrent Activity edit. The merge hazard is real, narrow,
and concentrated in one ~300-line span.

`OPUS-E-06` is the minimal carve-out that resolves that hazard without
attempting the rest of CP9.

Out of scope for E-06 (those remain in E-05 / CP9):
- MainActivity Presenter extraction
- DetailActivity ≤ 1500 lines
- Gallery rebuild
- External review sign-off
- Any UI behavior change

In scope for E-06:
- Move the `OfflineAnswerEngine.generate()` callsite (and the surrounding
  `executor.execute(...)` + post-generation field updates) out of
  `DetailActivity.java` and behind a single `AnswerPresenter` boundary.
- Leave the existing harness tags, in-flight render, and progress-listener
  semantics byte-identical from the user-visible side.

## Scope — three DetailActivity callsites

All three live in `DetailActivity.java`. All three are
`executor.execute(Runnable)` blocks that call
`OfflineAnswerEngine.generate(...)` and then dispatch a result block back to
the UI thread via `runTrackedOnUiThread(harnessToken, ...)`. The
post-generation orchestration is near-identical across the three.

### Site 1 — `startPendingGeneration()`

- File: `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- Method: `private void startPendingGeneration()`
- Span: lines 2915 – 2995
- Generate call: line 2939
- Harness tag: `detail.pendingGeneration`
- PreparedAnswer source: `OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
  currentTitle, currentSources, pendingSessionUsed, pendingStartedAtMs,
  pendingHostEnabled, pendingHostBaseUrl, pendingHostModelId,
  pendingSystemPrompt, pendingPrompt)`
- UI-thread tail unique work: `maybeStartAutoFollowUp()` after
  `scrollToLatestTurn()`
- Failure tail unique work: `followUpInput.setText(lastFailedQuery)` if input
  is present

### Site 2 — `runFollowUp()`

- File: same
- Method: `private void runFollowUp()`
- Span: lines 2997 – 3122
- Two-stage: `OfflineAnswerEngine.prepare(...)` at line 3020, then
  `OfflineAnswerEngine.generate(...)` at line 3064
- Harness tags:
  - prepare: `detail.followup.prepare`
  - generate: `detail.followup.deterministic` |
    `detail.followup.abstain` | `detail.followup.generate`
  - failure: `detail.followup.failure`
- UI-thread tail unique work: `followUpInput.setText("")` before
  `renderDetailState()`; `renderSources()`, `renderInlineSources()`, and
  optional `showSourceProvenancePanel(primary)` during the prepare-rendered
  preview phase (only when `!deterministic && !abstain`)

### Site 3 — `runTabletFollowUp(String rawQuery)`

- File: same
- Method: `private void runTabletFollowUp(String rawQuery)`
- Span: lines 3124 – 3244
- Two-stage: `OfflineAnswerEngine.prepare(...)` at line 3147, then
  `OfflineAnswerEngine.generate(...)` at line 3186
- Harness tags: same as Site 2 with `.tablet` suffix
- UI-thread tail unique work: zeroing tablet-specific state
  (`tabletComposerText`, `selectedTabletTurnId`, `selectedSourceKey`,
  `tabletEvidenceSelectionKey`, `tabletEvidenceXRefs.clear()`) on both the
  prepare-preview branch and the success branch

## Common orchestration (the part we are extracting)

All three sites share this UI-thread tail block (annotated with the
field updates we have to keep):

```
runTrackedOnUiThread(harnessToken, () -> {
    if (requestToken != followUpRenderToken) return;
    completedStreamingToken = requestToken;
    List<SearchResult> answerSources = answerRun.sources == null
        ? Collections.emptyList()
        : answerRun.sources;
    String resolvedAnswerBody = resolveFinalAnswerBody(
        answerRun.answerBody, requestToken);
    sessionMemory.recordTurn(
        answerRun.query, resolvedAnswerBody,
        answerRun.abstain ? Collections.emptyList() : answerSources,
        answerRun.ruleId);
    ChatSessionStore.persist(DetailActivity.this);
    currentTitle = answerRun.query;
    currentSubtitle = answerRun.subtitle;
    currentBody = resolvedAnswerBody;
    currentRuleId = safe(answerRun.ruleId).trim();
    currentSources = new ArrayList<>(answerSources);
    pendingHostEnabled = answerRun.hostBackendUsed;
    currentAnswerHostFallbackUsed = answerRun.hostFallbackUsed;
    lastFailedQuery = "";
    collapseHeroAfterStableAnswer = true;
    answerMode = true;
    /* per-kind tail */
    renderDetailState();
    refreshRelatedGuides();
    setBusy(OfflineAnswerEngine.buildCompletionStatus(
        DetailActivity.this, answerRun), false);
    scrollToLatestTurn();
    /* per-kind extra (Site 1: maybeStartAutoFollowUp) */
});
```

Failure tail across all three:

```
completedStreamingToken = requestToken;
lastFailedQuery = /* query for sites 2/3, preparedAnswer.query for site 1 */;
currentBody = buildGenerationFailureBody(exc);
answerMode = true;
/* per-kind tail (tabletComposerText, followUpInput) */
renderDetailState();
setBusy("Offline answer failed: " + exc.getMessage(), false);
```

## AnswerPresenter shape

New file: `android-app/app/src/main/java/com/senku/mobile/AnswerPresenter.java`

```java
final class AnswerPresenter {

    enum Kind {
        INITIAL_PENDING,   // startPendingGeneration
        PHONE_FOLLOWUP,    // runFollowUp
        TABLET_FOLLOWUP,   // runTabletFollowUp
    }

    interface Host {
        Context applicationContext();
        File modelFile();
        Executor executor();
        SessionMemory sessionMemory();
        int currentRequestToken();
        boolean isCurrentRequestToken(int token);
        int beginHarnessTask(String tag);
        void runTrackedOnUiThread(int harnessToken, Runnable r);
        OfflineAnswerEngine.ProgressListener createAnswerProgressListener(
            int requestToken);

        // Render hooks — each Kind implements only what it needs.
        // The presenter calls onSuccess / onFailure with a
        // typed payload; the Activity does the actual View mutation.
        void onPreparePreview(int requestToken,
            OfflineAnswerEngine.PreparedAnswer preparedAnswer);
        void onSuccess(int requestToken, AnswerRunResult result);
        void onFailure(int requestToken, Kind kind, Throwable exc,
            String fallbackQuery);
    }

    static final class AnswerRunResult {
        final OfflineAnswerEngine.AnswerRun answerRun;
        final List<SearchResult> answerSources;
        final String resolvedAnswerBody;
        AnswerRunResult(OfflineAnswerEngine.AnswerRun run,
            List<SearchResult> sources, String resolvedBody) {
            this.answerRun = run;
            this.answerSources = sources;
            this.resolvedAnswerBody = resolvedBody;
        }
    }

    private final Host host;

    AnswerPresenter(Host host) { this.host = host; }

    /** Site 1: PreparedAnswer already restored, single-stage generate. */
    void generateRestored(int requestToken,
            OfflineAnswerEngine.PreparedAnswer preparedAnswer);

    /** Sites 2 + 3: prepare(repo) then generate, with preview render. */
    void prepareThenGenerate(int requestToken, Kind kind,
            PackRepository repo, String query);
}
```

The presenter owns:
- `executor.execute(...)`
- the prepare → preview-render gating logic
- the harness token bookkeeping for the prepare and generate stages
- the `OfflineAnswerEngine.generate()` call itself
- the construction of `AnswerRunResult` (resolveFinalAnswerBody +
  null-safe sources)
- the routing of success / failure / preview into the `Host` callbacks

The Activity owns (and must continue to own):
- field mutations on the Activity (`currentTitle`, `currentSources`, etc.)
- `renderDetailState()`, `refreshRelatedGuides()`, `setBusy(...)`,
  `scrollToLatestTurn()`, `maybeStartAutoFollowUp()`,
  `renderSources()`, `renderInlineSources()`,
  `showSourceProvenancePanel(...)`
- per-Kind tail decisions (followUpInput vs tabletComposerText)

This split keeps the merge surface for Wave B inside `AnswerPresenter` and
the three small `Host.onSuccess(...)` callbacks in DetailActivity, *not*
inside the three giant `executor.execute(...)` blocks that exist today.

## Acceptance Criteria

1. New file `AnswerPresenter.java` exists in
   `android-app/app/src/main/java/com/senku/mobile/`.
2. `DetailActivity.java` no longer contains any direct call to
   `OfflineAnswerEngine.generate(...)`. (`grep -n
   'OfflineAnswerEngine\.generate(' DetailActivity.java` returns zero
   matches.)
3. `DetailActivity.java` instantiates one `AnswerPresenter` and routes
   the three sites through it:
   - `startPendingGeneration()` → `presenter.generateRestored(token, prepared)`
   - `runFollowUp()` → `presenter.prepareThenGenerate(token, PHONE_FOLLOWUP, repo, query)`
   - `runTabletFollowUp(...)` → `presenter.prepareThenGenerate(token, TABLET_FOLLOWUP, repo, query)`
4. Harness tags emitted at runtime are byte-identical to today
   (`detail.pendingGeneration`, `detail.followup.prepare`,
   `detail.followup.deterministic|abstain|generate`,
   `detail.followup.failure`, and `.tablet` siblings). No new tags.
5. No change to `OfflineAnswerEngine` public signature, no change to
   `AnswerRun`, no change to `PreparedAnswer`.
6. `DetailActivity.java` line count drops by at least 200 lines (sanity
   check that the executor blocks moved). Final line count is *not*
   required to be ≤ 1500 — that gate is CP9, not E-06.
7. Existing instrumentation tests under
   `android-app/app/src/androidTest/java/com/senku/mobile/`
   that hit `detail.pendingGeneration`, `detail.followup.*`, and the
   tablet siblings still pass without modification.
8. New JVM unit test `AnswerPresenterTest` (in
   `android-app/app/src/test/java/com/senku/mobile/`) covers:
   - `INITIAL_PENDING` happy path posts onSuccess once
   - `PHONE_FOLLOWUP` happy path posts onPreparePreview then onSuccess
     in order when not deterministic / not abstain
   - `PHONE_FOLLOWUP` deterministic path posts onSuccess only (no
     preview)
   - `PHONE_FOLLOWUP` abstain path posts onSuccess only (no preview)
   - `TABLET_FOLLOWUP` parity with PHONE_FOLLOWUP
   - failure path posts onFailure with the right fallback query
     (`prepared.query` for INITIAL_PENDING, raw query for PHONE/TABLET)
   - stale `requestToken` is dropped silently
9. Pre-existing manual smoke across the four-emulator posture matrix
   (`5556 / 5560 / 5554 / 5558`) shows no behavioral diff: same
   prepare-preview body, same in-flight status, same completion status,
   same tablet field-clear behavior.

## Test Plan

- JVM: `./gradlew :app:testDebugUnitTest --tests
  com.senku.mobile.AnswerPresenterTest`
- Instrumentation:
  `scripts/run_android_instrumented_ui_smoke.ps1 -Posture default`
  with focus on detail.pendingGeneration / detail.followup.* harness
  tags
- Posture matrix (read-only mode is fine for this gate):
  `scripts/build_android_ui_state_pack_parallel.ps1` — sanity that all
  four postures still render an answer
- Diff check:
  `git diff --stat android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  should show ≥ 200 lines removed
- Optional: route a Spark scout pass over the diff for naming /
  dead-code regressions before the worker session exits

## Risks + Mitigations

- **Token-staleness drift.** The presenter must keep the
  `if (requestToken != followUpRenderToken) return;` guard exactly where
  it is today. Mitigation: route the check through `Host.isCurrentRequestToken(token)`
  inside the `runTrackedOnUiThread` callback and assert with a unit test.
- **Harness tag drift.** Wave B test infra and the existing
  instrumentation suite key off these exact tag strings. Mitigation:
  pull tag literals into `static final String` constants in
  `AnswerPresenter` and reference them from one place.
- **Tablet field-clear regressions.** The tablet path zeroes more state
  than the phone path on both preview and success. Mitigation: explicit
  per-Kind branches in the Activity's `onPreparePreview` and
  `onSuccess` implementations, plus a manual tablet-portrait posture
  smoke before sign-off.
- **Stretch scope creep.** It is tempting to also extract the
  `retryLastFailedQuery()` and `maybeStartAutoFollowUp()` paths.
  Mitigation: explicitly out of scope for E-06; capture as
  `OPUS-E-07` follow-up if useful for CP9.

## Wave B Linkage

Wave B's three tasks (BACK-U-01 / U-02 / U-03) all add new fields to
`AnswerRun` or new branches in the success-tail block. Once E-06 is
landed, those changes happen inside the Presenter's success-path code
or inside `AnswerRunResult`, not inside three near-duplicate executor
blocks. The merge surface for Wave B drops from "three giant blocks in
the 6264-line Activity" to "one block in a focused Presenter file" —
which is the entire point of this carve-out.

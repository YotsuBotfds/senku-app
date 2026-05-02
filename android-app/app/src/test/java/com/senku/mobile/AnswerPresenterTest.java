package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.Test;

public final class AnswerPresenterTest {
    @Test
    public void initialPendingHappyPathPostsOnSuccessOnce() {
        FakeHost host = new FakeHost(7);
        FakeEngine engine = new FakeEngine();
        OfflineAnswerEngine.PreparedAnswer preparedAnswer = generativePrepared("restored question");
        engine.answerRunToReturn = answerRun("restored question", false, false);

        AnswerPresenter presenter = new AnswerPresenter(
            host,
            engine,
            (finalAnswerBody, requestToken) -> "resolved:" + finalAnswerBody + ":" + requestToken,
            repo -> repo
        );

        presenter.generateRestored(7, preparedAnswer);

        assertEquals(List.of(AnswerPresenter.HARNESS_PENDING_GENERATION), host.begunTags);
        assertEquals(List.of("success:INITIAL_PENDING"), host.events);
        assertSame(engine.answerRunToReturn, host.lastResult.answerRun);
        assertEquals("resolved:final answer:7", host.lastResult.resolvedAnswerBody);
        assertEquals(host.lastResult.answerRun.confidenceLabel, host.lastResult.confidenceLabel);
        assertEquals(host.lastResult.answerRun.mode, host.lastResult.mode);
        assertNull(host.lastFailure);
    }

    @Test
    public void initialPendingExecuteRejectionClosesHarnessTokenAndReportsFailure() {
        int busyCountBefore = HarnessTestSignals.busyCount();
        FakeHost host = new FakeHost(41, true);
        host.executor = command -> {
            throw new RejectedExecutionException("executor closed");
        };
        AnswerPresenter presenter = new AnswerPresenter(
            host,
            new FakeEngine(),
            (body, token) -> body,
            repo -> repo
        );

        presenter.generateRestored(41, generativePrepared("pending execute rejection"));

        assertEquals(List.of(AnswerPresenter.HARNESS_PENDING_GENERATION), host.begunTags);
        assertEquals(List.of("failure:INITIAL_PENDING"), host.events);
        assertEquals("pending execute rejection", host.lastFallbackQuery);
        assertEquals(busyCountBefore, HarnessTestSignals.busyCount());
    }

    @Test
    public void phoneFollowUpHappyPathPostsPreviewThenSuccess() {
        FakeHost host = new FakeHost(9);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("phone followup");
        engine.answerRunToReturn = answerRun("phone followup", false, false);
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(9, AnswerPresenter.Kind.PHONE_FOLLOWUP, null, "phone followup");

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_GENERATE
            ),
            host.begunTags
        );
        assertEquals(
            List.of("preview:PHONE_FOLLOWUP", "success:PHONE_FOLLOWUP"),
            host.events
        );
        assertSame(engine.preparedToReturn, host.lastPreparedPreview);
        assertSame(engine.answerRunToReturn, host.lastResult.answerRun);
        assertEquals(host.lastResult.answerRun.confidenceLabel, host.lastResult.confidenceLabel);
        assertEquals(host.lastResult.answerRun.mode, host.lastResult.mode);
    }

    @Test
    public void phoneFollowUpDeterministicPathSkipsPreview() {
        FakeHost host = new FakeHost(11);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = deterministicPrepared("deterministic followup");
        engine.answerRunToReturn = answerRun("deterministic followup", true, false);
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(11, AnswerPresenter.Kind.PHONE_FOLLOWUP, null, "deterministic followup");

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_DETERMINISTIC
            ),
            host.begunTags
        );
        assertEquals(List.of("success:PHONE_FOLLOWUP"), host.events);
        assertNull(host.lastPreparedPreview);
    }

    @Test
    public void phoneFollowUpAbstainPathSkipsPreview() {
        FakeHost host = new FakeHost(13);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = abstainPrepared("abstain followup");
        engine.answerRunToReturn = answerRun("abstain followup", false, true);
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(13, AnswerPresenter.Kind.PHONE_FOLLOWUP, null, "abstain followup");

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_ABSTAIN
            ),
            host.begunTags
        );
        assertEquals(List.of("success:PHONE_FOLLOWUP"), host.events);
        assertNull(host.lastPreparedPreview);
    }

    @Test
    public void phoneFollowUpUncertainFitPathSkipsPreviewAndPublishesMode() {
        FakeHost host = new FakeHost(14);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = uncertainFitPrepared("uncertain followup");
        engine.answerRunToReturn = answerRunWithMode(
            "uncertain followup",
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT
        );
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(14, AnswerPresenter.Kind.PHONE_FOLLOWUP, null, "uncertain followup");

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_GENERATE
            ),
            host.begunTags
        );
        assertEquals(List.of("success:PHONE_FOLLOWUP"), host.events);
        assertNull(host.lastPreparedPreview);
        assertSame(engine.answerRunToReturn, host.lastResult.answerRun);
        assertEquals(OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT, host.lastResult.mode);
        assertEquals(1, engine.prepareCalls);
        assertEquals(1, engine.generateCalls);
    }

    @Test
    public void tabletFollowUpMatchesPhonePreviewAndSuccessFlow() {
        FakeHost host = new FakeHost(15);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("tablet followup");
        engine.answerRunToReturn = answerRun("tablet followup", false, false);
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(15, AnswerPresenter.Kind.TABLET_FOLLOWUP, null, "tablet followup");

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_TABLET_PREPARE,
                AnswerPresenter.HARNESS_TABLET_GENERATE
            ),
            host.begunTags
        );
        assertEquals(
            List.of("preview:TABLET_FOLLOWUP", "success:TABLET_FOLLOWUP"),
            host.events
        );
        assertSame(engine.preparedToReturn, host.lastPreparedPreview);
        assertSame(engine.answerRunToReturn, host.lastResult.answerRun);
        assertEquals(host.lastResult.answerRun.mode, host.lastResult.mode);
    }

    @Test
    public void tabletFollowUpUncertainFitPathSkipsPreviewAndPublishesMode() {
        FakeHost host = new FakeHost(16);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = uncertainFitPrepared("tablet uncertain followup");
        engine.answerRunToReturn = answerRunWithMode(
            "tablet uncertain followup",
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT
        );
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(
            16,
            AnswerPresenter.Kind.TABLET_FOLLOWUP,
            null,
            "tablet uncertain followup"
        );

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_TABLET_PREPARE,
                AnswerPresenter.HARNESS_TABLET_GENERATE
            ),
            host.begunTags
        );
        assertEquals(List.of("success:TABLET_FOLLOWUP"), host.events);
        assertNull(host.lastPreparedPreview);
        assertSame(engine.answerRunToReturn, host.lastResult.answerRun);
        assertEquals(OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT, host.lastResult.mode);
        assertEquals(1, engine.prepareCalls);
        assertEquals(1, engine.generateCalls);
    }

    @Test
    public void failurePathUsesPreparedOrRawFallbackQuery() {
        FakeHost initialHost = new FakeHost(21);
        FakeEngine initialEngine = new FakeEngine();
        initialEngine.generateException = new IllegalStateException("initial failed");
        AnswerPresenter initialPresenter = new AnswerPresenter(
            initialHost,
            initialEngine,
            (body, token) -> body,
            repo -> repo
        );

        initialPresenter.generateRestored(21, generativePrepared("pending fallback"));

        assertEquals(List.of("failure:INITIAL_PENDING"), initialHost.events);
        assertEquals("pending fallback", initialHost.lastFallbackQuery);
        assertEquals(List.of(AnswerPresenter.HARNESS_PENDING_GENERATION), initialHost.begunTags);

        FakeHost phoneHost = new FakeHost(22);
        FakeEngine phoneEngine = new FakeEngine();
        phoneEngine.prepareException = new IllegalArgumentException("phone failed");
        AnswerPresenter phonePresenter = new AnswerPresenter(phoneHost, phoneEngine, (body, token) -> body, repo -> repo);

        phonePresenter.prepareThenGenerate(22, AnswerPresenter.Kind.PHONE_FOLLOWUP, null, "phone raw query");

        assertEquals(List.of("failure:PHONE_FOLLOWUP"), phoneHost.events);
        assertEquals("phone raw query", phoneHost.lastFallbackQuery);
        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_FAILURE
            ),
            phoneHost.begunTags
        );

        FakeHost tabletHost = new FakeHost(23);
        FakeEngine tabletEngine = new FakeEngine();
        tabletEngine.prepareException = new IllegalArgumentException("tablet failed");
        AnswerPresenter tabletPresenter = new AnswerPresenter(
            tabletHost,
            tabletEngine,
            (body, token) -> body,
            repo -> repo
        );

        tabletPresenter.prepareThenGenerate(23, AnswerPresenter.Kind.TABLET_FOLLOWUP, null, "tablet raw query");

        assertEquals(List.of("failure:TABLET_FOLLOWUP"), tabletHost.events);
        assertEquals("tablet raw query", tabletHost.lastFallbackQuery);
        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_TABLET_PREPARE,
                AnswerPresenter.HARNESS_TABLET_FAILURE
            ),
            tabletHost.begunTags
        );
    }

    @Test
    public void followUpGenerationFailureAfterPreviewKeepsSubmittedQueryForRestore() {
        int busyCountBefore = HarnessTestSignals.busyCount();
        FakeHost host = new FakeHost(31, true);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("draft that should come back");
        engine.generateException = new IllegalStateException("generation failed");
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(
            31,
            AnswerPresenter.Kind.PHONE_FOLLOWUP,
            null,
            "  draft that should come back  "
        );

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_GENERATE,
                AnswerPresenter.HARNESS_FOLLOWUP_FAILURE
            ),
            host.begunTags
        );
        assertEquals(
            List.of("preview:PHONE_FOLLOWUP", "failure:PHONE_FOLLOWUP"),
            host.events
        );
        assertSame(engine.preparedToReturn, host.lastPreparedPreview);
        assertEquals("draft that should come back", host.lastFallbackQuery);
        assertEquals(1, engine.prepareCalls);
        assertEquals(1, engine.generateCalls);
        assertEquals(busyCountBefore, HarnessTestSignals.busyCount());
    }

    @Test
    public void followUpExecuteRejectionClosesPrepareTokenAndReportsFailure() {
        int busyCountBefore = HarnessTestSignals.busyCount();
        FakeHost host = new FakeHost(42, true);
        host.executor = command -> {
            throw new RejectedExecutionException("executor closed");
        };
        AnswerPresenter presenter = new AnswerPresenter(host, new FakeEngine(), (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(
            42,
            AnswerPresenter.Kind.PHONE_FOLLOWUP,
            null,
            "  rejected followup  "
        );

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_FAILURE
            ),
            host.begunTags
        );
        assertEquals(List.of("failure:PHONE_FOLLOWUP"), host.events);
        assertEquals("rejected followup", host.lastFallbackQuery);
        assertEquals(busyCountBefore, HarnessTestSignals.busyCount());
    }

    @Test
    public void staleRequestTokenIsDroppedSilently() {
        FakeHost host = new FakeHost(99);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("stale query");
        engine.answerRunToReturn = answerRun("stale query", false, false);
        AnswerPresenter presenter = new AnswerPresenter(host, engine, (body, token) -> body, repo -> repo);

        presenter.prepareThenGenerate(1, AnswerPresenter.Kind.PHONE_FOLLOWUP, null, "stale query");

        assertEquals(
            List.of(
                AnswerPresenter.HARNESS_FOLLOWUP_PREPARE,
                AnswerPresenter.HARNESS_FOLLOWUP_GENERATE
            ),
            host.begunTags
        );
        assertEquals(List.of(), host.events);
        assertEquals(1, engine.prepareCalls);
        assertEquals(1, engine.generateCalls);
        assertNull(host.lastPreparedPreview);
        assertNull(host.lastResult);
        assertNull(host.lastFailure);
    }

    private static OfflineAnswerEngine.PreparedAnswer generativePrepared(String query) {
        return OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            query,
            List.of(sampleSource()),
            false,
            System.currentTimeMillis() - 500L,
            false,
            "",
            "",
            "system",
            "prompt"
        );
    }

    private static OfflineAnswerEngine.PreparedAnswer deterministicPrepared(String query) {
        return OfflineAnswerEngine.PreparedAnswer.restoredDeterministic(
            query,
            "deterministic answer",
            List.of(sampleSource()),
            false,
            "rule-id",
            System.currentTimeMillis() - 500L
        );
    }

    private static OfflineAnswerEngine.PreparedAnswer abstainPrepared(String query) {
        return OfflineAnswerEngine.PreparedAnswer.abstain(
            query,
            "abstain answer",
            List.of(sampleSource()),
            false
        );
    }

    private static OfflineAnswerEngine.PreparedAnswer uncertainFitPrepared(String query) {
        return OfflineAnswerEngine.PreparedAnswer.uncertainFit(
            query,
            "uncertain fit answer",
            List.of(sampleSource()),
            false,
            System.currentTimeMillis() - 500L,
            0L,
            0L,
            0L,
            OfflineAnswerEngine.ConfidenceLabel.LOW,
            false
        );
    }

    private static OfflineAnswerEngine.AnswerRun answerRun(
        String query,
        boolean deterministic,
        boolean abstain
    ) {
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel = deterministic
            ? OfflineAnswerEngine.ConfidenceLabel.HIGH
            : (abstain ? OfflineAnswerEngine.ConfidenceLabel.LOW : OfflineAnswerEngine.ConfidenceLabel.MEDIUM);
        return new OfflineAnswerEngine.AnswerRun(
            query,
            "final answer",
            List.of(sampleSource()),
            250L,
            false,
            deterministic,
            abstain,
            "subtitle",
            "rule-id",
            null,
            confidenceLabel,
            false,
            false
        );
    }

    private static OfflineAnswerEngine.AnswerRun answerRunWithMode(
        String query,
        OfflineAnswerEngine.AnswerMode mode
    ) {
        return new OfflineAnswerEngine.AnswerRun(
            query,
            "final answer",
            List.of(sampleSource()),
            250L,
            false,
            false,
            mode == OfflineAnswerEngine.AnswerMode.ABSTAIN,
            "subtitle",
            "rule-id",
            null,
            OfflineAnswerEngine.ConfidenceLabel.LOW,
            mode
        );
    }

    private static SearchResult sampleSource() {
        return new SearchResult(
            "Emergency Shelter",
            "",
            "Lean-to snippet",
            "Lean-to body",
            "GD-001",
            "Lean-To",
            "survival",
            "guide-focus"
        );
    }

    private static final class FakeHost implements AnswerPresenter.Host {
        final ArrayList<String> begunTags = new ArrayList<>();
        final ArrayList<String> events = new ArrayList<>();
        final SessionMemory sessionMemory = new SessionMemory();
        final Context applicationContext = null;
        final File modelFile = null;
        Executor executor = Runnable::run;
        final int currentRequestToken;

        OfflineAnswerEngine.PreparedAnswer lastPreparedPreview;
        AnswerPresenter.AnswerRunResult lastResult;
        Throwable lastFailure;
        String lastFallbackQuery;
        int nextHarnessToken = 1;
        final boolean trackHarnessSignals;

        FakeHost(int currentRequestToken) {
            this(currentRequestToken, false);
        }

        FakeHost(int currentRequestToken, boolean trackHarnessSignals) {
            this.currentRequestToken = currentRequestToken;
            this.trackHarnessSignals = trackHarnessSignals;
        }

        @Override
        public Context applicationContext() {
            return applicationContext;
        }

        @Override
        public File modelFile() {
            return modelFile;
        }

        @Override
        public Executor executor() {
            return executor;
        }

        @Override
        public SessionMemory sessionMemory() {
            return sessionMemory;
        }

        @Override
        public int currentRequestToken() {
            return currentRequestToken;
        }

        @Override
        public boolean isCurrentRequestToken(int token) {
            return token == currentRequestToken;
        }

        @Override
        public int beginHarnessTask(String tag) {
            begunTags.add(tag);
            if (trackHarnessSignals) {
                return HarnessTestSignals.begin(tag);
            }
            return nextHarnessToken++;
        }

        @Override
        public void runTrackedOnUiThread(int harnessToken, Runnable r) {
            try {
                if (r != null) {
                    r.run();
                }
            } finally {
                if (trackHarnessSignals) {
                    HarnessTestSignals.end(harnessToken);
                }
            }
        }

        @Override
        public OfflineAnswerEngine.AnswerProgressListener createAnswerProgressListener(int requestToken) {
            return new OfflineAnswerEngine.AnswerProgressListener() {
                @Override
                public void onAnswerBody(String partialAnswerBody) {
                }
            };
        }

        @Override
        public void onPreparePreview(
            int requestToken,
            AnswerPresenter.Kind kind,
            OfflineAnswerEngine.PreparedAnswer preparedAnswer
        ) {
            events.add("preview:" + kind.name());
            lastPreparedPreview = preparedAnswer;
        }

        @Override
        public void onSuccess(
            int requestToken,
            AnswerPresenter.Kind kind,
            AnswerPresenter.AnswerRunResult result
        ) {
            events.add("success:" + kind.name());
            lastResult = result;
        }

        @Override
        public void onFailure(int requestToken, AnswerPresenter.Kind kind, Throwable exc, String fallbackQuery) {
            events.add("failure:" + kind.name());
            lastFailure = exc;
            lastFallbackQuery = fallbackQuery;
        }
    }

    private static final class FakeEngine implements AnswerPresenter.Engine {
        OfflineAnswerEngine.PreparedAnswer preparedToReturn;
        OfflineAnswerEngine.AnswerRun answerRunToReturn;
        Exception prepareException;
        Exception generateException;
        int prepareCalls;
        int generateCalls;

        @Override
        public OfflineAnswerEngine.PreparedAnswer prepare(
            Context context,
            PackRepository repo,
            SessionMemory sessionMemory,
            File modelFile,
            String query
        ) throws Exception {
            prepareCalls += 1;
            if (prepareException != null) {
                throw prepareException;
            }
            return preparedToReturn;
        }

        @Override
        public OfflineAnswerEngine.AnswerRun generate(
            Context context,
            File modelFile,
            OfflineAnswerEngine.PreparedAnswer prepared,
            OfflineAnswerEngine.AnswerProgressListener progressListener
        ) throws Exception {
            generateCalls += 1;
            if (generateException != null) {
                throw generateException;
            }
            return answerRunToReturn;
        }
    }
}

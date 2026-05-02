package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.Test;

public final class AskQueryControllerTest {
    @Test
    public void repositoryUnavailableStopsBeforeUiLaneOrEngineWork() {
        FakeHost host = new FakeHost();
        host.repositoryAvailable = false;
        FakeEngine engine = new FakeEngine();
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("how do I make water safe");

        assertEquals(List.of("pack-unavailable"), host.events);
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void blankQueryUsesHostBlankQueryCallback() {
        FakeHost host = readyHost();
        FakeEngine engine = new FakeEngine();
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("   ");

        assertEquals(List.of("blank-query"), host.events);
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void deterministicAnswerSkipsModelAndPrepare() {
        FakeHost host = readyHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        FakeEngine engine = new FakeEngine();
        DeterministicAnswerRouter.DeterministicAnswer deterministic =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "rule-id",
                "Use a proven shortcut.",
                List.of(sampleSource())
            );
        AskQueryController controller = new AskQueryController(host, engine, query -> deterministic);

        controller.runAsk("  shortcut please  ");

        assertEquals(List.of("deterministic"), host.events);
        assertEquals("shortcut please", host.lastDeterministicQuery);
        assertSame(deterministic, host.lastDeterministicAnswer);
        assertTrue(host.lastDeterministicBody.contains("Use a proven shortcut."));
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void modelUnavailableStopsBeforePrepareWhenNoRuntimePathExists() {
        FakeHost host = readyHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        host.reviewedCardRuntimeEnabled = false;
        host.hasAutoQuery = true;
        FakeEngine engine = new FakeEngine();
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("need generated answer");

        assertEquals(List.of("model-unavailable:true"), host.events);
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void modelUnavailableStopsBeforePrepareForUnsupportedReviewedCardQuery() {
        FakeHost host = readyHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        host.reviewedCardRuntimeEnabled = true;
        FakeEngine engine = new FakeEngine();
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("need generated answer");

        assertEquals(List.of("model-unavailable:false"), host.events);
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void prepareSuccessRunsAsyncPrepareAndPostsSuccess() {
        FakeHost host = readyHost();
        host.hostInferenceSettings = settings(true);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("prepared question");
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk(" prepared question ");

        assertEquals(
            List.of("prepare-started:prepared question", "prepare-success"),
            host.events
        );
        assertEquals(List.of(AskQueryController.HARNESS_PREPARE), host.begunHarnessTags);
        assertEquals(List.of(1), host.uiHarnessTokens);
        assertEquals(1, engine.prepareCalls);
        assertEquals("prepared question", engine.lastQuery);
        assertSame(host.sessionMemory, engine.lastSessionMemory);
        assertSame(engine.preparedToReturn, host.lastPreparedSuccess);
    }

    @Test
    public void localModelRoutePreparesWhenHostInferenceIsDisabled() {
        FakeHost host = readyHost();
        host.hostInferenceSettings = settings(false);
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("local model question");
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("local model question");

        assertEquals(
            List.of("prepare-started:local model question", "prepare-success"),
            host.events
        );
        assertEquals(1, engine.prepareCalls);
        assertSame(engine.preparedToReturn, host.lastPreparedSuccess);
    }

    @Test
    public void nullHostSettingsStillUsesLocalModelWhenPresent() {
        FakeHost host = readyHost();
        host.hostInferenceSettings = null;
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("local model with null host settings");
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("local model with null host settings");

        assertEquals(
            List.of("prepare-started:local model with null host settings", "prepare-success"),
            host.events
        );
        assertEquals(1, engine.prepareCalls);
    }

    @Test
    public void nullHostSettingsWithoutModelStopsUnlessReviewedCardRouteSupportsQuery() {
        FakeHost unavailable = readyHost();
        unavailable.modelFile = null;
        unavailable.hostInferenceSettings = null;
        unavailable.reviewedCardRuntimeEnabled = false;
        unavailable.hasAutoQuery = true;
        FakeEngine unavailableEngine = new FakeEngine();
        new AskQueryController(unavailable, unavailableEngine, query -> null)
            .runAsk("need an unsupported generated answer");

        assertEquals(List.of("model-unavailable:true"), unavailable.events);
        assertEquals(0, unavailableEngine.prepareCalls);

        FakeHost reviewed = readyHost();
        reviewed.modelFile = null;
        reviewed.hostInferenceSettings = null;
        reviewed.reviewedCardRuntimeEnabled = true;
        FakeEngine reviewedEngine = new FakeEngine();
        reviewedEngine.preparedToReturn = generativePrepared("child swallowed unknown cleaner");
        new AskQueryController(reviewed, reviewedEngine, query -> null)
            .runAsk("child swallowed unknown cleaner");

        assertEquals(
            List.of("prepare-started:child swallowed unknown cleaner", "prepare-success"),
            reviewed.events
        );
        assertEquals(1, reviewedEngine.prepareCalls);
    }

    @Test
    public void hostInferenceRoutePreparesWhenReviewedRuntimeIsEnabledButQueryIsUnsupported() {
        FakeHost host = readyHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(true);
        host.reviewedCardRuntimeEnabled = true;
        FakeEngine engine = new FakeEngine();
        engine.preparedToReturn = generativePrepared("generic unsupported reviewed route query");
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("generic unsupported reviewed route query");

        assertEquals(
            List.of("prepare-started:generic unsupported reviewed route query", "prepare-success"),
            host.events
        );
        assertEquals(1, engine.prepareCalls);
    }

    @Test
    public void repositoryUnavailableWinsBeforeBlankOrDeterministicChecks() {
        FakeHost host = readyHost();
        host.repositoryAvailable = false;
        FakeEngine engine = new FakeEngine();
        AskQueryController controller = new AskQueryController(
            host,
            engine,
            query -> {
                throw new AssertionError("deterministic matcher should not run without repository");
            }
        );

        controller.runAsk("   ");
        controller.runAsk("shortcut please");

        assertEquals(List.of("pack-unavailable", "pack-unavailable"), host.events);
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void prepareFailurePostsFailureWithAutoQueryState() {
        FakeHost host = readyHost();
        host.modelFile = new File("model.task");
        host.hasAutoQuery = false;
        FakeEngine engine = new FakeEngine();
        engine.prepareException = new IllegalStateException("prepare failed");
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("broken ask");

        assertEquals(
            List.of("prepare-started:broken ask", "prepare-failure:false"),
            host.events
        );
        assertEquals(List.of(AskQueryController.HARNESS_PREPARE), host.begunHarnessTags);
        assertEquals(1, engine.prepareCalls);
        assertEquals("broken ask", host.lastFailureQuery);
        assertSame(engine.prepareException, host.lastFailure);
        assertNull(host.lastFailedPrepared);
    }

    @Test
    public void executeRejectionPostsPrepareFailureAndSettlesHarnessToken() {
        FakeHost host = readyHost();
        host.executor = command -> {
            throw new RejectedExecutionException("executor closed");
        };
        FakeEngine engine = new FakeEngine();
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("rejected ask");

        assertEquals(
            List.of("prepare-started:rejected ask", "prepare-failure:false"),
            host.events
        );
        assertEquals(List.of(AskQueryController.HARNESS_PREPARE), host.begunHarnessTags);
        assertEquals(List.of(1), host.uiHarnessTokens);
        assertEquals("rejected ask", host.lastFailureQuery);
        assertTrue(host.lastFailure instanceof RejectedExecutionException);
        assertNull(host.lastFailedPrepared);
        assertEquals(0, engine.prepareCalls);
    }

    @Test
    public void stalePrepareSuccessIsIgnoredAfterNewerAskStarts() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        OfflineAnswerEngine.PreparedAnswer firstPrepared = generativePrepared("first ask");
        OfflineAnswerEngine.PreparedAnswer secondPrepared = generativePrepared("second ask");
        engine.preparedAnswers.put("first ask", firstPrepared);
        engine.preparedAnswers.put("second ask", secondPrepared);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("first ask");
        controller.runAsk("second ask");
        host.runQueuedUiAction(0);
        host.runQueuedUiAction(1);

        assertEquals(
            List.of("prepare-started:first ask", "prepare-started:second ask", "prepare-success"),
            host.events
        );
        assertSame(secondPrepared, host.lastPreparedSuccess);
        assertEquals(2, engine.prepareCalls);
    }

    @Test
    public void stalePrepareSuccessIsIgnoredAfterNewerUnavailableRouteWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        OfflineAnswerEngine.PreparedAnswer firstPrepared = generativePrepared("first ask");
        engine.preparedAnswers.put("first ask", firstPrepared);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("first ask");
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        host.reviewedCardRuntimeEnabled = false;
        host.hasAutoQuery = true;
        controller.runAsk("second ask unavailable");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("prepare-started:first ask", "model-unavailable:true"),
            host.events
        );
        assertNull(host.lastPreparedSuccess);
        assertEquals(1, engine.prepareCalls);
    }

    @Test
    public void stalePrepareSuccessIsIgnoredAfterNewerBlankQueryWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        OfflineAnswerEngine.PreparedAnswer firstPrepared = generativePrepared("first ask");
        engine.preparedAnswers.put("first ask", firstPrepared);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("first ask");
        controller.runAsk("   ");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("prepare-started:first ask", "blank-query"),
            host.events
        );
        assertNull(host.lastPreparedSuccess);
        assertEquals(1, engine.prepareCalls);
    }

    @Test
    public void stalePrepareSuccessIsIgnoredAfterNewerDeterministicRouteWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        OfflineAnswerEngine.PreparedAnswer firstPrepared = generativePrepared("first ask");
        engine.preparedAnswers.put("first ask", firstPrepared);
        DeterministicAnswerRouter.DeterministicAnswer deterministic =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "rule-id",
                "Use a proven shortcut.",
                List.of(sampleSource())
            );
        AskQueryController controller = new AskQueryController(
            host,
            engine,
            query -> "second ask".equals(query) ? deterministic : null
        );

        controller.runAsk("first ask");
        controller.runAsk("second ask");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("prepare-started:first ask", "deterministic"),
            host.events
        );
        assertNull(host.lastPreparedSuccess);
        assertSame(deterministic, host.lastDeterministicAnswer);
        assertEquals(1, engine.prepareCalls);
    }

    @Test
    public void stalePrepareFailureIsIgnoredAfterNewerAskStarts() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        IllegalStateException firstFailure = new IllegalStateException("old failure");
        OfflineAnswerEngine.PreparedAnswer secondPrepared = generativePrepared("second ask");
        engine.prepareExceptions.put("first ask", firstFailure);
        engine.preparedAnswers.put("second ask", secondPrepared);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("first ask");
        controller.runAsk("second ask");
        host.runQueuedUiAction(0);
        host.runQueuedUiAction(1);

        assertEquals(
            List.of("prepare-started:first ask", "prepare-started:second ask", "prepare-success"),
            host.events
        );
        assertSame(secondPrepared, host.lastPreparedSuccess);
        assertNull(host.lastFailure);
    }

    @Test
    public void stalePrepareFailureIsIgnoredAfterNewerPackUnavailableRouteWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        IllegalStateException firstFailure = new IllegalStateException("old failure");
        engine.prepareExceptions.put("first ask", firstFailure);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("first ask");
        host.repositoryAvailable = false;
        controller.runAsk("second ask no pack");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("prepare-started:first ask", "pack-unavailable"),
            host.events
        );
        assertNull(host.lastFailure);
        assertEquals(1, engine.prepareCalls);
    }

    @Test
    public void currentPrepareFailureStillPosts() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        IllegalStateException currentFailure = new IllegalStateException("current failure");
        engine.prepareExceptions.put("current ask", currentFailure);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("current ask");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("prepare-started:current ask", "prepare-failure:false"),
            host.events
        );
        assertSame(currentFailure, host.lastFailure);
        assertEquals("current ask", host.lastFailureQuery);
    }

    private static FakeHost readyHost() {
        FakeHost host = new FakeHost();
        host.repositoryAvailable = true;
        host.modelFile = new File("model.task");
        host.hostInferenceSettings = settings(false);
        return host;
    }

    private static HostInferenceConfig.Settings settings(boolean enabled) {
        return new HostInferenceConfig.Settings(enabled, "http://127.0.0.1:1235/v1", "test-model");
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

    private static final class FakeHost implements AskQueryController.Host {
        final ArrayList<String> events = new ArrayList<>();
        final ArrayList<String> begunHarnessTags = new ArrayList<>();
        final ArrayList<Integer> uiHarnessTokens = new ArrayList<>();
        final ArrayList<Runnable> queuedUiActions = new ArrayList<>();
        final SessionMemory sessionMemory = new SessionMemory();
        Executor executor = Runnable::run;

        boolean repositoryAvailable;
        File modelFile;
        HostInferenceConfig.Settings hostInferenceSettings = settings(false);
        boolean reviewedCardRuntimeEnabled;
        boolean hasAutoQuery;
        boolean queueUiActions;
        int nextHarnessToken = 1;

        String lastDeterministicQuery;
        String lastDeterministicBody;
        String lastFailureQuery;
        DeterministicAnswerRouter.DeterministicAnswer lastDeterministicAnswer;
        OfflineAnswerEngine.PreparedAnswer lastPreparedSuccess;
        OfflineAnswerEngine.PreparedAnswer lastFailedPrepared;
        Exception lastFailure;

        @Override
        public Context applicationContext() {
            return null;
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
        public boolean isRepositoryAvailable() {
            return repositoryAvailable;
        }

        @Override
        public PackRepository repository() {
            return null;
        }

        @Override
        public File modelFile() {
            return modelFile;
        }

        @Override
        public HostInferenceConfig.Settings hostInferenceSettings() {
            return hostInferenceSettings;
        }

        @Override
        public boolean reviewedCardRuntimeEnabled() {
            return reviewedCardRuntimeEnabled;
        }

        @Override
        public boolean hasAutoQuery() {
            return hasAutoQuery;
        }

        @Override
        public int beginHarnessTask(String label) {
            begunHarnessTags.add(label);
            return nextHarnessToken++;
        }

        @Override
        public void runTrackedOnUiThread(int harnessToken, Runnable action) {
            uiHarnessTokens.add(harnessToken);
            if (queueUiActions) {
                queuedUiActions.add(action);
                return;
            }
            if (action != null) {
                action.run();
            }
        }

        void runQueuedUiAction(int index) {
            Runnable action = queuedUiActions.get(index);
            if (action != null) {
                action.run();
            }
        }

        @Override
        public void onPackUnavailable() {
            events.add("pack-unavailable");
        }

        @Override
        public void onBlankQuery() {
            events.add("blank-query");
        }

        @Override
        public void onDeterministicAnswer(
            String query,
            DeterministicAnswerRouter.DeterministicAnswer deterministic,
            String answerBody
        ) {
            events.add("deterministic");
            lastDeterministicQuery = query;
            lastDeterministicAnswer = deterministic;
            lastDeterministicBody = answerBody;
        }

        @Override
        public void onModelUnavailable(boolean hasAutoQuery) {
            events.add("model-unavailable:" + hasAutoQuery);
        }

        @Override
        public void onPrepareStarted(String query) {
            events.add("prepare-started:" + query);
        }

        @Override
        public void onPrepareSuccess(OfflineAnswerEngine.PreparedAnswer preparedAnswer) {
            events.add("prepare-success");
            lastPreparedSuccess = preparedAnswer;
        }

        @Override
        public void onPrepareFailure(
            String query,
            OfflineAnswerEngine.PreparedAnswer failedPrepared,
            Exception exception,
            boolean hasAutoQuery
        ) {
            events.add("prepare-failure:" + hasAutoQuery);
            lastFailureQuery = query;
            lastFailedPrepared = failedPrepared;
            lastFailure = exception;
        }
    }

    private static final class FakeEngine implements AskQueryController.Engine {
        final Map<String, OfflineAnswerEngine.PreparedAnswer> preparedAnswers = new HashMap<>();
        final Map<String, Exception> prepareExceptions = new HashMap<>();
        OfflineAnswerEngine.PreparedAnswer preparedToReturn;
        Exception prepareException;
        int prepareCalls;
        String lastQuery;
        SessionMemory lastSessionMemory;

        @Override
        public OfflineAnswerEngine.PreparedAnswer prepare(
            Context context,
            PackRepository repo,
            SessionMemory sessionMemory,
            File modelFile,
            String query
        ) throws Exception {
            prepareCalls += 1;
            lastQuery = query;
            lastSessionMemory = sessionMemory;
            if (prepareExceptions.containsKey(query)) {
                throw prepareExceptions.get(query);
            }
            if (prepareException != null) {
                throw prepareException;
            }
            if (preparedAnswers.containsKey(query)) {
                return preparedAnswers.get(query);
            }
            return preparedToReturn;
        }
    }
}

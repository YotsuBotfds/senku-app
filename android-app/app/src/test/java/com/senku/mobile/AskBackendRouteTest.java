package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.Test;

public final class AskBackendRouteTest {
    @Test
    public void deterministicRouteBypassesBackendEvenWhenNoRuntimeIsAvailable() {
        RouteHost host = availableHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        host.reviewedCardRuntimeEnabled = false;
        RouteEngine engine = new RouteEngine(generativePrepared("should not run", false));
        DeterministicAnswerRouter.DeterministicAnswer deterministic =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "instant:water",
                "Boil clear water for one minute, then cool it in a clean container.",
                List.of(source("GD-101", "Water Safety", "water", "guide-focus"))
            );
        AskQueryController controller = new AskQueryController(host, engine, query -> deterministic);

        controller.runAsk("  make water safe  ");

        assertEquals(List.of("deterministic"), host.events);
        assertEquals("make water safe", host.lastDeterministicQuery);
        assertSame(deterministic, host.lastDeterministicAnswer);
        assertTrue(host.lastDeterministicBody.contains("Boil clear water"));
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void reviewedCardRuntimeRouteCanPrepareWithoutModelOrHostInference() {
        RouteHost host = availableHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        host.reviewedCardRuntimeEnabled = true;
        OfflineAnswerEngine.PreparedAnswer reviewedPrepared = reviewedCardPrepared("child swallowed unknown cleaner");
        RouteEngine engine = new RouteEngine(reviewedPrepared);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk(" child swallowed unknown cleaner ");

        assertEquals(
            List.of("prepare-started:child swallowed unknown cleaner", "prepare-success"),
            host.events
        );
        assertEquals(1, engine.prepareCalls);
        assertNull(engine.lastModelFile);
        assertSame(reviewedPrepared, host.lastPreparedSuccess);
        assertTrue(host.lastPreparedSuccess.deterministic);
        assertEquals("answer_card:poisoning_unknown_ingestion", host.lastPreparedSuccess.ruleId);
        assertEquals(List.of(AskQueryController.HARNESS_PREPARE), host.begunHarnessTags);
    }

    @Test
    public void genericQueryWithReviewedCardRuntimeEnabledStillRequiresModelRoute() {
        RouteHost host = availableHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        host.reviewedCardRuntimeEnabled = true;
        host.hasAutoQuery = true;
        RouteEngine engine = new RouteEngine(generativePrepared("unreachable", false));
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("how do i build a tarp shelter");

        assertEquals(List.of("model-unavailable:true"), host.events);
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void generativeHostRouteCanPrepareWithoutLocalModelWhenHostInferenceIsEnabled() {
        RouteHost host = availableHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(true);
        host.reviewedCardRuntimeEnabled = false;
        OfflineAnswerEngine.PreparedAnswer prepared =
            generativePrepared("how do i build a tarp shelter", true);
        RouteEngine engine = new RouteEngine(prepared);
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("how do i build a tarp shelter");

        assertEquals(
            List.of("prepare-started:how do i build a tarp shelter", "prepare-success"),
            host.events
        );
        assertEquals(1, engine.prepareCalls);
        assertNull(engine.lastModelFile);
        assertSame(prepared, host.lastPreparedSuccess);
        assertTrue(host.lastPreparedSuccess.inferenceSettings.enabled);
    }

    @Test
    public void unavailableRouteStopsBeforeBackendPrepareWhenNoRuntimePathExists() {
        RouteHost host = availableHost();
        host.modelFile = null;
        host.hostInferenceSettings = settings(false);
        host.reviewedCardRuntimeEnabled = false;
        host.hasAutoQuery = true;
        RouteEngine engine = new RouteEngine(generativePrepared("unreachable", false));
        AskQueryController controller = new AskQueryController(host, engine, query -> null);

        controller.runAsk("need an answer");

        assertEquals(List.of("model-unavailable:true"), host.events);
        assertEquals(0, engine.prepareCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void staleBackendSuccessIsIgnoredAfterNewerDeterministicRouteWins() {
        RouteHost host = availableHost();
        host.queueUiActions = true;
        OfflineAnswerEngine.PreparedAnswer stalePrepared = generativePrepared("first generated ask", true);
        RouteEngine engine = new RouteEngine(stalePrepared);
        DeterministicAnswerRouter.DeterministicAnswer deterministic =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "instant:fire",
                "Keep sparks downwind and clear tinder before lighting.",
                List.of(source("GD-122", "Fire Safety", "fire", "guide-focus"))
            );
        AskQueryController controller = new AskQueryController(
            host,
            engine,
            query -> "second instant ask".equals(query) ? deterministic : null
        );

        controller.runAsk("first generated ask");
        controller.runAsk("second instant ask");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("prepare-started:first generated ask", "deterministic"),
            host.events
        );
        assertEquals(1, engine.prepareCalls);
        assertNull(host.lastPreparedSuccess);
        assertSame(deterministic, host.lastDeterministicAnswer);
    }

    private static RouteHost availableHost() {
        RouteHost host = new RouteHost();
        host.repositoryAvailable = true;
        host.modelFile = new File("model.task");
        host.hostInferenceSettings = settings(false);
        return host;
    }

    private static HostInferenceConfig.Settings settings(boolean enabled) {
        return new HostInferenceConfig.Settings(enabled, "http://127.0.0.1:1235/v1", "test-model");
    }

    private static OfflineAnswerEngine.PreparedAnswer generativePrepared(String query, boolean hostEnabled) {
        return OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            query,
            List.of(source("GD-094", "Tarp Shelter", "shelter", "guide-focus")),
            false,
            System.currentTimeMillis() - 250L,
            hostEnabled,
            hostEnabled ? "http://127.0.0.1:1235/v1" : "",
            hostEnabled ? "test-model" : "",
            "system",
            "prompt"
        );
    }

    private static OfflineAnswerEngine.PreparedAnswer reviewedCardPrepared(String query) {
        return OfflineAnswerEngine.PreparedAnswer.restoredDeterministic(
            query,
            "Call poison control, EMS, or the fastest clinician now.\n\nDo not induce vomiting.",
            List.of(source("GD-898", "Poisoning", "medical", "reviewed-card")),
            false,
            "answer_card:poisoning_unknown_ingestion",
            System.currentTimeMillis() - 250L
        );
    }

    private static SearchResult source(String guideId, String title, String category, String retrievalMode) {
        return new SearchResult(
            title,
            "",
            title + " snippet",
            title + " body",
            guideId,
            title,
            category,
            retrievalMode
        );
    }

    private static final class RouteHost implements AskQueryController.Host {
        final ArrayList<String> events = new ArrayList<>();
        final ArrayList<String> begunHarnessTags = new ArrayList<>();
        final ArrayList<Integer> uiHarnessTokens = new ArrayList<>();
        final ArrayList<Runnable> queuedUiActions = new ArrayList<>();
        final SessionMemory sessionMemory = new SessionMemory();
        final Executor executor = Runnable::run;

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

    private static final class RouteEngine implements AskQueryController.Engine {
        final OfflineAnswerEngine.PreparedAnswer preparedToReturn;
        int prepareCalls;
        String lastQuery;
        File lastModelFile;
        SessionMemory lastSessionMemory;

        RouteEngine(OfflineAnswerEngine.PreparedAnswer preparedToReturn) {
            this.preparedToReturn = preparedToReturn;
        }

        @Override
        public OfflineAnswerEngine.PreparedAnswer prepare(
            Context context,
            PackRepository repo,
            SessionMemory sessionMemory,
            File modelFile,
            String query
        ) {
            prepareCalls += 1;
            lastQuery = query;
            lastModelFile = modelFile;
            lastSessionMemory = sessionMemory;
            return preparedToReturn;
        }
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.Test;

public final class MainSearchControllerTest {
    @Test
    public void repositoryUnavailableStopsBeforeCommandOrEngineWork() {
        FakeHost host = new FakeHost();
        host.repositoryAvailable = false;
        host.sessionCommandHandled = true;
        FakeEngine engine = new FakeEngine();
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("/reset");

        assertEquals(List.of("pack-unavailable"), host.events);
        assertEquals(0, host.sessionCommandCalls);
        assertEquals(0, engine.searchCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void sessionCommandShortCircuitsBeforeSearchUiOrEngine() {
        FakeHost host = readyHost();
        host.sessionCommandHandled = true;
        FakeEngine engine = new FakeEngine();
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch(" /state ");

        assertEquals(List.of("session-command:/state"), host.events);
        assertEquals(1, host.sessionCommandCalls);
        assertEquals(0, engine.searchCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void deterministicSearchSkipsAsyncSearch() {
        FakeHost host = readyHost();
        FakeEngine engine = new FakeEngine();
        DeterministicAnswerRouter.DeterministicAnswer deterministic =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "rule-id",
                "Use the known path.",
                List.of(sampleResult("Known Guide", "GD-001"))
            );
        MainSearchController controller = new MainSearchController(host, engine, query -> deterministic);

        controller.runSearch("  known path  ");

        assertEquals(List.of("deterministic:known path"), host.events);
        assertSame(deterministic, host.lastDeterministic);
        assertEquals(0, engine.searchCalls);
        assertEquals(List.of(), host.begunHarnessTags);
    }

    @Test
    public void blankQueryDisplaysGuidesLabelAndSearchesWithBlankQuery() {
        FakeHost host = readyHost();
        FakeEngine engine = new FakeEngine();
        engine.resultsToReturn = List.of(sampleResult("Guide Browser", "GD-002"));
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("   ");

        assertEquals(
            List.of("started::guides:false", "success::guides:false:1"),
            host.events
        );
        assertEquals("", engine.lastQuery);
        assertEquals(MainSearchController.BLANK_DISPLAY_QUERY, host.lastDisplayQuery);
        assertEquals(List.of(MainSearchController.HARNESS_SEARCH), host.begunHarnessTags);
    }

    @Test
    public void asyncSearchSuccessUsesRetrievalPlanAndPostsResults() {
        FakeHost host = readyHost();
        SearchResult anchor = sampleResult("Prior Guide", "GD-010");
        host.sessionMemory.recordTurn("old question", "old answer", List.of(anchor), "rule");
        FakeEngine engine = new FakeEngine();
        SearchResult result = sampleResult("Shelter", "GD-020");
        engine.resultsToReturn = List.of(result);
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch(" what about it ");

        assertEquals(1, engine.searchCalls);
        assertTrue(engine.lastQuery.length() > 0);
        assertEquals(7, engine.lastLimit);
        assertTrue(host.events.get(0).startsWith("started:what about it:what about it:true"));
        assertEquals("success:what about it:what about it:true:1", host.events.get(1));
        assertSame(result, host.lastResults.get(0));
        assertEquals(List.of(1), host.uiHarnessTokens);
    }

    @Test
    public void asyncSearchFailurePostsFailure() {
        FakeHost host = readyHost();
        FakeEngine engine = new FakeEngine();
        engine.searchException = new IllegalStateException("search failed");
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("broken search");

        assertEquals(
            List.of("started:broken search:broken search:false", "failure:broken search"),
            host.events
        );
        assertSame(engine.searchException, host.lastFailure);
        assertEquals(1, engine.searchCalls);
    }

    @Test
    public void executeRejectionPostsFailureAndSettlesHarnessToken() {
        FakeHost host = readyHost();
        host.executor = command -> {
            throw new RejectedExecutionException("executor closed");
        };
        FakeEngine engine = new FakeEngine();
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("blocked search");

        assertEquals(
            List.of("started:blocked search:blocked search:false", "failure:blocked search"),
            host.events
        );
        assertEquals(0, engine.searchCalls);
        assertEquals(List.of(MainSearchController.HARNESS_SEARCH), host.begunHarnessTags);
        assertEquals(List.of(1), host.uiHarnessTokens);
        assertTrue(host.lastFailure instanceof RejectedExecutionException);
    }

    @Test
    public void reviewDemoSearchResultsAreShapedThroughHostGuideLookup() {
        FakeHost host = readyHost();
        host.productReviewMode = true;
        SearchResult reviewResult = sampleResult("Emergency Shelter for Heavy Rain", "GD-023");
        host.guideLookup.put("GD-010", sampleResult("Guide lookup row", "GD-010"));
        FakeEngine engine = new FakeEngine();
        engine.resultsToReturn = List.of(reviewResult);
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("rain shelter");

        assertTrue(host.loadGuideByIdCalls > 0);
        assertTrue(containsGuideId(host.lastResults, "GD-023"));
        assertTrue(host.lastResults.size() >= 1);
    }

    @Test
    public void staleSuccessIsSuppressedAfterNewerSearchStarts() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        SearchResult first = sampleResult("First", "GD-001");
        SearchResult second = sampleResult("Second", "GD-002");
        engine.resultsByQuery.put("first", List.of(first));
        engine.resultsByQuery.put("second", List.of(second));
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("first");
        controller.runSearch("second");
        host.runQueuedUiAction(0);
        host.runQueuedUiAction(1);

        assertEquals(
            List.of(
                "started:first:first:false",
                "started:second:second:false",
                "success:second:second:false:1"
            ),
            host.events
        );
        assertSame(second, host.lastResults.get(0));
    }

    @Test
    public void staleFailureIsSuppressedAfterNewerUnavailableRouteWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        engine.exceptionsByQuery.put("first", new IllegalStateException("old failure"));
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("first");
        host.repositoryAvailable = false;
        controller.runSearch("second");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("started:first:first:false", "pack-unavailable"),
            host.events
        );
        assertNull(host.lastFailure);
    }

    @Test
    public void staleSuccessIsSuppressedAfterNewerSessionCommandRouteWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        SearchResult first = sampleResult("First", "GD-001");
        engine.resultsByQuery.put("first", List.of(first));
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("first");
        host.sessionCommandHandled = true;
        controller.runSearch("/state");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("started:first:first:false", "session-command:/state"),
            host.events
        );
        assertNull(host.lastResults);
    }

    @Test
    public void staleSuccessIsSuppressedAfterNewerUnavailableRouteWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        SearchResult first = sampleResult("First", "GD-001");
        engine.resultsByQuery.put("first", List.of(first));
        MainSearchController controller = new MainSearchController(host, engine, query -> null);

        controller.runSearch("first");
        host.repositoryAvailable = false;
        controller.runSearch("second");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("started:first:first:false", "pack-unavailable"),
            host.events
        );
        assertNull(host.lastResults);
    }

    @Test
    public void staleSuccessIsSuppressedAfterNewerDeterministicRouteWins() {
        FakeHost host = readyHost();
        host.queueUiActions = true;
        FakeEngine engine = new FakeEngine();
        SearchResult first = sampleResult("First", "GD-001");
        engine.resultsByQuery.put("first", List.of(first));
        DeterministicAnswerRouter.DeterministicAnswer deterministic =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "rule-id",
                "Use the deterministic answer.",
                List.of(sampleResult("Known Guide", "GD-002"))
            );
        MainSearchController controller = new MainSearchController(
            host,
            engine,
            query -> "known".equals(query) ? deterministic : null
        );

        controller.runSearch("first");
        controller.runSearch("known");
        host.runQueuedUiAction(0);

        assertEquals(
            List.of("started:first:first:false", "deterministic:known"),
            host.events
        );
        assertSame(deterministic, host.lastDeterministic);
        assertNull(host.lastResults);
    }

    @Test
    public void staleSearchSuccessIsSuppressedAfterNewerAskStarts() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeHost searchHost = readyHost();
        searchHost.queueUiActions = true;
        FakeEngine searchEngine = new FakeEngine();
        SearchResult oldSearchResult = sampleResult("Old Search", "GD-001");
        searchEngine.resultsByQuery.put("old search", List.of(oldSearchResult));
        MainSearchController searchController =
            new MainSearchController(searchHost, searchEngine, query -> null, sharedGate);
        FakeAskHost askHost = readyAskHost();
        askHost.queueUiActions = true;
        FakeAskEngine askEngine = new FakeAskEngine();
        OfflineAnswerEngine.PreparedAnswer newAskAnswer = preparedAnswer("new ask");
        askEngine.preparedAnswers.put("new ask", newAskAnswer);
        AskQueryController askController =
            new AskQueryController(askHost, askEngine, query -> null, sharedGate);

        searchController.runSearch("old search");
        askController.runAsk("new ask");
        searchHost.runQueuedUiAction(0);
        askHost.runQueuedUiAction(0);

        assertEquals(List.of("started:old search:old search:false"), searchHost.events);
        assertNull(searchHost.lastResults);
        assertEquals(List.of("prepare-started:new ask", "prepare-success"), askHost.events);
        assertSame(newAskAnswer, askHost.lastPreparedSuccess);
    }

    @Test
    public void staleSearchSuccessIsSuppressedAfterNewerAskNoPackExit() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeHost searchHost = readyHost();
        searchHost.queueUiActions = true;
        FakeEngine searchEngine = new FakeEngine();
        searchEngine.resultsByQuery.put("old search", List.of(sampleResult("Old Search", "GD-001")));
        MainSearchController searchController =
            new MainSearchController(searchHost, searchEngine, query -> null, sharedGate);
        FakeAskHost askHost = readyAskHost();
        askHost.repositoryAvailable = false;
        AskQueryController askController =
            new AskQueryController(askHost, new FakeAskEngine(), query -> null, sharedGate);

        searchController.runSearch("old search");
        askController.runAsk("new ask");
        searchHost.runQueuedUiAction(0);

        assertEquals(List.of("started:old search:old search:false"), searchHost.events);
        assertNull(searchHost.lastResults);
        assertEquals(List.of("pack-unavailable"), askHost.events);
    }

    @Test
    public void staleSearchSuccessIsSuppressedAfterNewerAskNoModelExit() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeHost searchHost = readyHost();
        searchHost.queueUiActions = true;
        FakeEngine searchEngine = new FakeEngine();
        searchEngine.resultsByQuery.put("old search", List.of(sampleResult("Old Search", "GD-001")));
        MainSearchController searchController =
            new MainSearchController(searchHost, searchEngine, query -> null, sharedGate);
        FakeAskHost askHost = readyAskHost();
        askHost.modelFile = null;
        askHost.hostInferenceSettings = settings(false);
        AskQueryController askController =
            new AskQueryController(askHost, new FakeAskEngine(), query -> null, sharedGate);

        searchController.runSearch("old search");
        askController.runAsk("new ask");
        searchHost.runQueuedUiAction(0);

        assertEquals(List.of("started:old search:old search:false"), searchHost.events);
        assertNull(searchHost.lastResults);
        assertEquals(List.of("model-unavailable:false"), askHost.events);
    }

    @Test
    public void staleSearchSuccessIsSuppressedAfterNewerBlankAskExit() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeHost searchHost = readyHost();
        searchHost.queueUiActions = true;
        FakeEngine searchEngine = new FakeEngine();
        searchEngine.resultsByQuery.put("old search", List.of(sampleResult("Old Search", "GD-001")));
        MainSearchController searchController =
            new MainSearchController(searchHost, searchEngine, query -> null, sharedGate);
        FakeAskHost askHost = readyAskHost();
        AskQueryController askController =
            new AskQueryController(askHost, new FakeAskEngine(), query -> null, sharedGate);

        searchController.runSearch("old search");
        askController.runAsk("  ");
        searchHost.runQueuedUiAction(0);

        assertEquals(List.of("started:old search:old search:false"), searchHost.events);
        assertNull(searchHost.lastResults);
        assertEquals(List.of("blank-query"), askHost.events);
    }

    @Test
    public void staleAskSuccessIsSuppressedAfterNewerSearchStarts() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeAskHost askHost = readyAskHost();
        askHost.queueUiActions = true;
        FakeAskEngine askEngine = new FakeAskEngine();
        OfflineAnswerEngine.PreparedAnswer oldAskAnswer = preparedAnswer("old ask");
        askEngine.preparedAnswers.put("old ask", oldAskAnswer);
        AskQueryController askController =
            new AskQueryController(askHost, askEngine, query -> null, sharedGate);
        FakeHost searchHost = readyHost();
        searchHost.queueUiActions = true;
        FakeEngine searchEngine = new FakeEngine();
        SearchResult newSearchResult = sampleResult("New Search", "GD-002");
        searchEngine.resultsByQuery.put("new search", List.of(newSearchResult));
        MainSearchController searchController =
            new MainSearchController(searchHost, searchEngine, query -> null, sharedGate);

        askController.runAsk("old ask");
        searchController.runSearch("new search");
        askHost.runQueuedUiAction(0);
        searchHost.runQueuedUiAction(0);

        assertEquals(List.of("prepare-started:old ask"), askHost.events);
        assertNull(askHost.lastPreparedSuccess);
        assertEquals(
            List.of(
                "started:new search:new search:false",
                "success:new search:new search:false:1"
            ),
            searchHost.events
        );
        assertSame(newSearchResult, searchHost.lastResults.get(0));
    }

    @Test
    public void staleAskSuccessIsSuppressedAfterNewerSearchNoPackExit() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeAskHost askHost = readyAskHost();
        askHost.queueUiActions = true;
        FakeAskEngine askEngine = new FakeAskEngine();
        askEngine.preparedAnswers.put("old ask", preparedAnswer("old ask"));
        AskQueryController askController =
            new AskQueryController(askHost, askEngine, query -> null, sharedGate);
        FakeHost searchHost = readyHost();
        searchHost.repositoryAvailable = false;
        MainSearchController searchController =
            new MainSearchController(searchHost, new FakeEngine(), query -> null, sharedGate);

        askController.runAsk("old ask");
        searchController.runSearch("new search");
        askHost.runQueuedUiAction(0);

        assertEquals(List.of("prepare-started:old ask"), askHost.events);
        assertNull(askHost.lastPreparedSuccess);
        assertEquals(List.of("pack-unavailable"), searchHost.events);
    }

    @Test
    public void staleAskSuccessIsSuppressedAfterNewerSearchSessionCommandExit() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeAskHost askHost = readyAskHost();
        askHost.queueUiActions = true;
        FakeAskEngine askEngine = new FakeAskEngine();
        askEngine.preparedAnswers.put("old ask", preparedAnswer("old ask"));
        AskQueryController askController =
            new AskQueryController(askHost, askEngine, query -> null, sharedGate);
        FakeHost searchHost = readyHost();
        searchHost.sessionCommandHandled = true;
        MainSearchController searchController =
            new MainSearchController(searchHost, new FakeEngine(), query -> null, sharedGate);

        askController.runAsk("old ask");
        searchController.runSearch("session clear");
        askHost.runQueuedUiAction(0);

        assertEquals(List.of("prepare-started:old ask"), askHost.events);
        assertNull(askHost.lastPreparedSuccess);
        assertEquals(List.of("session-command:session clear"), searchHost.events);
    }

    @Test
    public void staleAskSuccessIsSuppressedAfterNewerSearchDeterministicExit() {
        LatestJobGate sharedGate = new LatestJobGate();
        FakeAskHost askHost = readyAskHost();
        askHost.queueUiActions = true;
        FakeAskEngine askEngine = new FakeAskEngine();
        askEngine.preparedAnswers.put("old ask", preparedAnswer("old ask"));
        AskQueryController askController =
            new AskQueryController(askHost, askEngine, query -> null, sharedGate);
        FakeHost searchHost = readyHost();
        DeterministicAnswerRouter.DeterministicAnswer deterministic =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "rule-id",
                "Use the deterministic answer.",
                List.of(sampleResult("Known Guide", "GD-002"))
            );
        MainSearchController searchController =
            new MainSearchController(searchHost, new FakeEngine(), query -> deterministic, sharedGate);

        askController.runAsk("old ask");
        searchController.runSearch("known");
        askHost.runQueuedUiAction(0);

        assertEquals(List.of("prepare-started:old ask"), askHost.events);
        assertNull(askHost.lastPreparedSuccess);
        assertEquals(List.of("deterministic:known"), searchHost.events);
        assertSame(deterministic, searchHost.lastDeterministic);
    }

    private static FakeHost readyHost() {
        FakeHost host = new FakeHost();
        host.repositoryAvailable = true;
        return host;
    }

    private static FakeAskHost readyAskHost() {
        FakeAskHost host = new FakeAskHost();
        host.repositoryAvailable = true;
        host.modelFile = new File("model.task");
        host.hostInferenceSettings = settings(false);
        return host;
    }

    private static HostInferenceConfig.Settings settings(boolean enabled) {
        return new HostInferenceConfig.Settings(enabled, "http://127.0.0.1:1235/v1", "test-model");
    }

    private static OfflineAnswerEngine.PreparedAnswer preparedAnswer(String query) {
        return OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            query,
            List.of(sampleResult("Ask Source", "GD-ASK")),
            false,
            System.currentTimeMillis() - 500L,
            false,
            "",
            "",
            "system",
            "prompt"
        );
    }

    private static SearchResult sampleResult(String title, String guideId) {
        return new SearchResult(
            title,
            guideId + " | survival",
            "Snippet for " + title,
            "Body for " + title,
            guideId,
            "Section",
            "survival",
            "guide-focus"
        );
    }

    private static boolean containsGuideId(List<SearchResult> results, String guideId) {
        if (results == null) {
            return false;
        }
        for (SearchResult result : results) {
            if (guideId.equals(result.guideId)) {
                return true;
            }
        }
        return false;
    }

    private static final class FakeHost implements MainSearchController.Host {
        final ArrayList<String> events = new ArrayList<>();
        final ArrayList<String> begunHarnessTags = new ArrayList<>();
        final ArrayList<Integer> uiHarnessTokens = new ArrayList<>();
        final ArrayList<Runnable> queuedUiActions = new ArrayList<>();
        final Map<String, SearchResult> guideLookup = new HashMap<>();
        final SessionMemory sessionMemory = new SessionMemory();
        Executor executor = Runnable::run;

        boolean repositoryAvailable;
        boolean productReviewMode;
        boolean sessionCommandHandled;
        boolean queueUiActions;
        int nextHarnessToken = 1;
        int sessionCommandCalls;
        int loadGuideByIdCalls;

        String lastDisplayQuery;
        List<SearchResult> lastResults;
        DeterministicAnswerRouter.DeterministicAnswer lastDeterministic;
        Exception lastFailure;

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
        public boolean productReviewMode() {
            return productReviewMode;
        }

        @Override
        public int searchResultLimit() {
            return 7;
        }

        @Override
        public boolean handleSessionCommand(String query) {
            sessionCommandCalls += 1;
            if (sessionCommandHandled) {
                events.add("session-command:" + query);
                return true;
            }
            return false;
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
        public SearchResult loadGuideById(String guideId) {
            loadGuideByIdCalls += 1;
            return guideLookup.get(guideId);
        }

        @Override
        public void onPackUnavailable() {
            events.add("pack-unavailable");
        }

        @Override
        public void onSearchStarted(String query, String displayQuery, boolean sessionUsed) {
            lastDisplayQuery = displayQuery;
            events.add("started:" + query + ":" + displayQuery + ":" + sessionUsed);
        }

        @Override
        public void onDeterministicSearch(
            String query,
            DeterministicAnswerRouter.DeterministicAnswer deterministic
        ) {
            lastDeterministic = deterministic;
            events.add("deterministic:" + query);
        }

        @Override
        public void onSearchSuccess(
            String query,
            String displayQuery,
            List<SearchResult> results,
            boolean hasVectorStore,
            boolean sessionUsed
        ) {
            lastDisplayQuery = displayQuery;
            lastResults = results;
            events.add(
                "success:" + query + ":" + displayQuery + ":" + sessionUsed + ":" + results.size()
            );
        }

        @Override
        public void onSearchFailure(String query, Exception exception) {
            lastFailure = exception;
            events.add("failure:" + query);
        }
    }

    private static final class FakeAskHost implements AskQueryController.Host {
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

        OfflineAnswerEngine.PreparedAnswer lastPreparedSuccess;
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
            lastFailure = exception;
        }
    }

    private static final class FakeEngine implements MainSearchController.Engine {
        final Map<String, List<SearchResult>> resultsByQuery = new HashMap<>();
        final Map<String, Exception> exceptionsByQuery = new HashMap<>();
        List<SearchResult> resultsToReturn = List.of();
        Exception searchException;
        int searchCalls;
        String lastQuery;
        int lastLimit;

        @Override
        public List<SearchResult> search(
            PackRepository repo,
            String query,
            int limit,
            PackRepository.AnchorPriorDirective anchorPrior
        ) throws Exception {
            searchCalls += 1;
            lastQuery = query;
            lastLimit = limit;
            if (exceptionsByQuery.containsKey(query)) {
                throw exceptionsByQuery.get(query);
            }
            if (searchException != null) {
                throw searchException;
            }
            if (resultsByQuery.containsKey(query)) {
                return resultsByQuery.get(query);
            }
            return resultsToReturn == null ? Collections.emptyList() : resultsToReturn;
        }
    }

    private static final class FakeAskEngine implements AskQueryController.Engine {
        final Map<String, OfflineAnswerEngine.PreparedAnswer> preparedAnswers = new HashMap<>();
        int prepareCalls;

        @Override
        public OfflineAnswerEngine.PreparedAnswer prepare(
            Context context,
            PackRepository repo,
            SessionMemory sessionMemory,
            File modelFile,
            String query
        ) {
            prepareCalls += 1;
            return preparedAnswers.get(query);
        }
    }

}

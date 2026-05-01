package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

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

    private static FakeHost readyHost() {
        FakeHost host = new FakeHost();
        host.repositoryAvailable = true;
        return host;
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
        final Executor executor = Runnable::run;

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

}

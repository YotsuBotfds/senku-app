package com.senku.mobile;

import java.util.List;
import java.util.concurrent.Executor;

final class MainSearchController {
    static final String HARNESS_SEARCH = "main.search";
    static final String BLANK_DISPLAY_QUERY = "guides";

    interface Host {
        Executor executor();
        SessionMemory sessionMemory();
        boolean isRepositoryAvailable();
        PackRepository repository();
        boolean productReviewMode();
        int searchResultLimit();
        boolean handleSessionCommand(String query);
        int beginHarnessTask(String label);
        void runTrackedOnUiThread(int harnessToken, Runnable action);
        SearchResult loadGuideById(String guideId);
        void onPackUnavailable();
        void onSearchStarted(String query, String displayQuery, boolean sessionUsed);
        void onDeterministicSearch(
            String query,
            DeterministicAnswerRouter.DeterministicAnswer deterministic
        );
        void onSearchSuccess(
            String query,
            String displayQuery,
            List<SearchResult> results,
            boolean hasVectorStore,
            boolean sessionUsed
        );
        void onSearchFailure(String query, Exception exception);
    }

    interface Engine {
        List<SearchResult> search(
            PackRepository repo,
            String query,
            int limit,
            PackRepository.AnchorPriorDirective anchorPrior
        ) throws Exception;
    }

    interface DeterministicMatcher {
        DeterministicAnswerRouter.DeterministicAnswer match(String query);
    }

    private static final Engine DEFAULT_ENGINE = PackRepository::search;
    private static final DeterministicMatcher DEFAULT_MATCHER = DeterministicAnswerRouter::match;

    private final Host host;
    private final Engine engine;
    private final DeterministicMatcher deterministicMatcher;
    private final LatestJobGate latestJobGate;

    MainSearchController(Host host) {
        this(host, DEFAULT_ENGINE, DEFAULT_MATCHER);
    }

    MainSearchController(Host host, Engine engine, DeterministicMatcher deterministicMatcher) {
        this(host, engine, deterministicMatcher, new LatestJobGate());
    }

    MainSearchController(
        Host host,
        Engine engine,
        DeterministicMatcher deterministicMatcher,
        LatestJobGate latestJobGate
    ) {
        this.host = host;
        this.engine = engine == null ? DEFAULT_ENGINE : engine;
        this.deterministicMatcher = deterministicMatcher == null ? DEFAULT_MATCHER : deterministicMatcher;
        this.latestJobGate = latestJobGate == null ? new LatestJobGate() : latestJobGate;
    }

    void runSearch(String rawQuery) {
        long jobToken = latestJobGate.nextJobToken();
        String query = safe(rawQuery).trim();
        if (!host.isRepositoryAvailable()) {
            host.onPackUnavailable();
            return;
        }
        if (host.handleSessionCommand(query)) {
            return;
        }
        if (!query.isEmpty()) {
            DeterministicAnswerRouter.DeterministicAnswer deterministic =
                deterministicMatcher.match(query);
            if (deterministic != null) {
                host.onDeterministicSearch(query, deterministic);
                return;
            }
        }

        SessionMemory.RetrievalPlan retrievalPlan = host.sessionMemory().buildRetrievalPlan(query);
        boolean sessionUsed = retrievalPlan.sessionUsed;
        String displayQuery = query.isEmpty() ? BLANK_DISPLAY_QUERY : query;
        host.onSearchStarted(query, displayQuery, sessionUsed);
        PackRepository repo = host.repository();
        boolean hasVectorStore = repo != null && repo.hasVectorStore();
        int limit = host.searchResultLimit();
        boolean productReviewMode = host.productReviewMode();
        int harnessToken = host.beginHarnessTask(HARNESS_SEARCH);
        try {
            host.executor().execute(() -> {
                try {
                    List<SearchResult> results = engine.search(
                        repo,
                        retrievalPlan.searchQuery,
                        limit,
                        retrievalPlan.anchorPrior
                    );
                    List<SearchResult> displayResults = ReviewDemoPolicy.shapeSearchResults(
                        displayQuery,
                        productReviewMode,
                        results,
                        host::loadGuideById
                    );
                    host.runTrackedOnUiThread(harnessToken, () -> {
                        if (latestJobGate.isCurrentJob(jobToken)) {
                            host.onSearchSuccess(query, displayQuery, displayResults, hasVectorStore, sessionUsed);
                        }
                    });
                } catch (Exception exc) {
                    host.runTrackedOnUiThread(harnessToken, () -> {
                        if (latestJobGate.isCurrentJob(jobToken)) {
                            host.onSearchFailure(query, exc);
                        }
                    });
                }
            });
        } catch (RuntimeException exc) {
            host.runTrackedOnUiThread(harnessToken, () -> {
                if (latestJobGate.isCurrentJob(jobToken)) {
                    host.onSearchFailure(query, exc);
                }
            });
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

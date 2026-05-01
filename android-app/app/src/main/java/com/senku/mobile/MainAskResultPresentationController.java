package com.senku.mobile;

import java.util.Collections;
import java.util.List;

final class MainAskResultPresentationController {
    private static final String DETERMINISTIC_READY_STATUS = "Deterministic offline answer ready";
    private static final String DETERMINISTIC_DETAIL_SUBTITLE = "Offline answer | deterministic | instant";
    private static final String OFFLINE_ANSWER_FAILED_STATUS = "Offline answer failed";
    private static final String OFFLINE_ANSWER_FAILED_HEADER = "Offline answer failed";

    private MainAskResultPresentationController() {
    }

    static DeterministicPublication deterministicAnswer(
        String query,
        DeterministicAnswerRouter.DeterministicAnswer deterministic
    ) {
        return new DeterministicPublication(
            DETERMINISTIC_READY_STATUS,
            MainResultPublicationPolicy.askResultSurface(query),
            "Deterministic offline answer for \"" + safe(query) + "\"",
            DETERMINISTIC_DETAIL_SUBTITLE,
            deterministic == null ? Collections.emptyList() : deterministic.sources,
            deterministic == null ? "" : deterministic.ruleId,
            true,
            true,
            true,
            true
        );
    }

    static ModelUnavailablePublication modelUnavailable(boolean hasAutoQuery) {
        return new ModelUnavailablePublication(
            MainRouteDecisionHelper.askUnavailableOrNoSourceFailure(),
            !hasAutoQuery
        );
    }

    static PrepareSuccessPublication prepareSuccess(OfflineAnswerEngine.PreparedAnswer preparedAnswer) {
        int sourceCount = preparedAnswer == null ? 0 : preparedAnswer.sources.size();
        String query = preparedAnswer == null ? "" : preparedAnswer.query;
        boolean sessionUsed = preparedAnswer != null && preparedAnswer.sessionUsed;
        return new PrepareSuccessPublication(
            sourceCount == 0,
            sourceCount,
            MainResultPublicationPolicy.askResultSurface(query),
            query,
            sourceCount,
            sessionUsed,
            true,
            true
        );
    }

    static PrepareFailurePublication prepareFailure(
        String query,
        OfflineAnswerEngine.PreparedAnswer failedPrepared,
        boolean hasAutoQuery
    ) {
        if (failedPrepared != null && !failedPrepared.sources.isEmpty()) {
            return new PrepareFailurePublication(
                OFFLINE_ANSWER_FAILED_STATUS,
                null,
                MainResultPublicationPolicy.askResultSurface(failedPrepared.query),
                failedPrepared.query,
                failedPrepared.sources,
                null,
                failedPrepared.query,
                failedPrepared.sources.size(),
                failedPrepared.sessionUsed
            );
        }
        return new PrepareFailurePublication(
            OFFLINE_ANSWER_FAILED_STATUS,
            MainRouteDecisionHelper.askUnavailableOrNoSourceFailure(),
            MainResultPublicationPolicy.askResultSurfaceWithBrowseFallback(query, !hasAutoQuery),
            safe(query),
            Collections.emptyList(),
            OFFLINE_ANSWER_FAILED_HEADER,
            "",
            0,
            false
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class DeterministicPublication {
        final String status;
        final MainResultPublicationPolicy resultPublication;
        final String header;
        final String detailSubtitle;
        final List<SearchResult> sources;
        final String ruleId;
        final boolean recordSessionTurn;
        final boolean persistSession;
        final boolean refreshSessionPanel;
        final boolean refreshRecentThreads;

        DeterministicPublication(
            String status,
            MainResultPublicationPolicy resultPublication,
            String header,
            String detailSubtitle,
            List<SearchResult> sources,
            String ruleId,
            boolean recordSessionTurn,
            boolean persistSession,
            boolean refreshSessionPanel,
            boolean refreshRecentThreads
        ) {
            this.status = safe(status);
            this.resultPublication = resultPublication == null
                ? MainResultPublicationPolicy.askResultSurface("")
                : resultPublication;
            this.header = safe(header);
            this.detailSubtitle = safe(detailSubtitle);
            this.sources = sources == null ? Collections.emptyList() : sources;
            this.ruleId = safe(ruleId);
            this.recordSessionTurn = recordSessionTurn;
            this.persistSession = persistSession;
            this.refreshSessionPanel = refreshSessionPanel;
            this.refreshRecentThreads = refreshRecentThreads;
        }
    }

    static final class ModelUnavailablePublication {
        final MainRouteDecisionHelper.RouteState routeState;
        final boolean showBrowseChrome;

        ModelUnavailablePublication(
            MainRouteDecisionHelper.RouteState routeState,
            boolean showBrowseChrome
        ) {
            this.routeState = routeState == null
                ? MainRouteDecisionHelper.askUnavailableOrNoSourceFailure()
                : routeState;
            this.showBrowseChrome = showBrowseChrome;
        }
    }

    static final class PrepareSuccessPublication {
        final boolean useGeneratingStatus;
        final int sourceCount;
        final MainResultPublicationPolicy resultPublication;
        final String headerQuery;
        final int headerSourceCount;
        final boolean headerSessionUsed;
        final boolean refreshSessionPanel;
        final boolean openPendingAnswerDetail;

        PrepareSuccessPublication(
            boolean useGeneratingStatus,
            int sourceCount,
            MainResultPublicationPolicy resultPublication,
            String headerQuery,
            int headerSourceCount,
            boolean headerSessionUsed,
            boolean refreshSessionPanel,
            boolean openPendingAnswerDetail
        ) {
            this.useGeneratingStatus = useGeneratingStatus;
            this.sourceCount = Math.max(0, sourceCount);
            this.resultPublication = resultPublication == null
                ? MainResultPublicationPolicy.askResultSurface("")
                : resultPublication;
            this.headerQuery = safe(headerQuery);
            this.headerSourceCount = Math.max(0, headerSourceCount);
            this.headerSessionUsed = headerSessionUsed;
            this.refreshSessionPanel = refreshSessionPanel;
            this.openPendingAnswerDetail = openPendingAnswerDetail;
        }
    }

    static final class PrepareFailurePublication {
        final String status;
        final MainRouteDecisionHelper.RouteState preliminaryRouteState;
        final MainResultPublicationPolicy resultPublication;
        final String highlightQuery;
        final List<SearchResult> sources;
        final String fixedHeader;
        final String headerQuery;
        final int headerSourceCount;
        final boolean headerSessionUsed;

        PrepareFailurePublication(
            String status,
            MainRouteDecisionHelper.RouteState preliminaryRouteState,
            MainResultPublicationPolicy resultPublication,
            String highlightQuery,
            List<SearchResult> sources,
            String fixedHeader,
            String headerQuery,
            int headerSourceCount,
            boolean headerSessionUsed
        ) {
            this.status = safe(status);
            this.preliminaryRouteState = preliminaryRouteState;
            this.resultPublication = resultPublication == null
                ? MainResultPublicationPolicy.askResultSurfaceWithBrowseFallback("", true)
                : resultPublication;
            this.highlightQuery = safe(highlightQuery);
            this.sources = sources == null ? Collections.emptyList() : sources;
            this.fixedHeader = fixedHeader;
            this.headerQuery = safe(headerQuery);
            this.headerSourceCount = Math.max(0, headerSourceCount);
            this.headerSessionUsed = headerSessionUsed;
        }

        boolean hasPreparedSources() {
            return !sources.isEmpty();
        }
    }
}

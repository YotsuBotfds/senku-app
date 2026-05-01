package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.List;

import org.junit.Test;

public final class MainAskResultPresentationControllerTest {
    @Test
    public void deterministicAnswerPublishesAskResultAndRefreshesSessionSurfaces() {
        SearchResult source = sampleSource("GD-001");
        DeterministicAnswerRouter.DeterministicAnswer answer =
            new DeterministicAnswerRouter.DeterministicAnswer(
                "water_rule",
                "Treat the water.",
                List.of(source)
            );

        MainAskResultPresentationController.DeterministicPublication publication =
            MainAskResultPresentationController.deterministicAnswer("muddy water", answer);

        assertEquals("Deterministic offline answer ready", publication.status);
        assertEquals("Deterministic offline answer for \"muddy water\"", publication.header);
        assertEquals("Offline answer | deterministic | instant", publication.detailSubtitle);
        assertEquals("water_rule", publication.ruleId);
        assertSame(answer.sources, publication.sources);
        assertTrue(publication.recordSessionTurn);
        assertTrue(publication.persistSession);
        assertTrue(publication.refreshSessionPanel);
        assertTrue(publication.refreshRecentThreads);
        assertEquals("muddy water", publication.resultPublication.highlightQuery());
        assertRoute(
            publication.resultPublication.routeState(),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
    }

    @Test
    public void modelUnavailableReturnsAskBrowseFallbackAndAutoQueryBrowseChromeFlag() {
        MainAskResultPresentationController.ModelUnavailablePublication manual =
            MainAskResultPresentationController.modelUnavailable(false);
        MainAskResultPresentationController.ModelUnavailablePublication auto =
            MainAskResultPresentationController.modelUnavailable(true);

        assertTrue(manual.showBrowseChrome);
        assertFalse(auto.showBrowseChrome);
        assertRoute(
            manual.routeState,
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertRoute(
            auto.routeState,
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
    }

    @Test
    public void prepareSuccessUsesGeneratingStatusForNoSourcePreparedAnswers() {
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.abstain(
            "unknown plant",
            "I do not have enough source support.",
            List.of(),
            true
        );

        MainAskResultPresentationController.PrepareSuccessPublication publication =
            MainAskResultPresentationController.prepareSuccess(prepared);

        assertTrue(publication.useGeneratingStatus);
        assertEquals(0, publication.sourceCount);
        assertEquals("unknown plant", publication.headerQuery);
        assertEquals(0, publication.headerSourceCount);
        assertTrue(publication.headerSessionUsed);
        assertTrue(publication.refreshSessionPanel);
        assertTrue(publication.openPendingAnswerDetail);
        assertRoute(
            publication.resultPublication.routeState(),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
    }

    @Test
    public void prepareSuccessUsesSourcesReadyStatusForPreparedSources() {
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "rain shelter",
            List.of(sampleSource("GD-024"), sampleSource("GD-394")),
            false,
            123L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        MainAskResultPresentationController.PrepareSuccessPublication publication =
            MainAskResultPresentationController.prepareSuccess(prepared);

        assertFalse(publication.useGeneratingStatus);
        assertEquals(2, publication.sourceCount);
        assertEquals("rain shelter", publication.headerQuery);
        assertEquals(2, publication.headerSourceCount);
        assertFalse(publication.headerSessionUsed);
        assertEquals("rain shelter", publication.resultPublication.highlightQuery());
    }

    @Test
    public void prepareFailureWithPreparedSourcesKeepsPreparedAskSurface() {
        OfflineAnswerEngine.PreparedAnswer failedPrepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "fire in rain",
            List.of(sampleSource("GD-394")),
            true,
            123L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        MainAskResultPresentationController.PrepareFailurePublication publication =
            MainAskResultPresentationController.prepareFailure("raw query", failedPrepared, false);

        assertEquals("Offline answer failed", publication.status);
        assertNull(publication.preliminaryRouteState);
        assertTrue(publication.hasPreparedSources());
        assertSame(failedPrepared.sources, publication.sources);
        assertNull(publication.fixedHeader);
        assertEquals("fire in rain", publication.headerQuery);
        assertEquals(1, publication.headerSourceCount);
        assertTrue(publication.headerSessionUsed);
        assertEquals("fire in rain", publication.resultPublication.highlightQuery());
        assertRoute(
            publication.resultPublication.routeState(),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
    }

    @Test
    public void prepareFailureWithoutPreparedSourcesFallsBackToRecentThreadsForManualAsk() {
        MainAskResultPresentationController.PrepareFailurePublication publication =
            MainAskResultPresentationController.prepareFailure("missing answer", null, false);

        assertEquals("Offline answer failed", publication.status);
        assertFalse(publication.hasPreparedSources());
        assertEquals("Offline answer failed", publication.fixedHeader);
        assertRoute(
            publication.preliminaryRouteState,
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertRoute(
            publication.resultPublication.routeState(),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
    }

    @Test
    public void prepareFailureWithoutPreparedSourcesKeepsAskResultsForAutoQueryAfterPreliminaryFallback() {
        MainAskResultPresentationController.PrepareFailurePublication publication =
            MainAskResultPresentationController.prepareFailure("auto answer", null, true);

        assertRoute(
            publication.preliminaryRouteState,
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertRoute(
            publication.resultPublication.routeState(),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
    }

    private static SearchResult sampleSource(String guideId) {
        return new SearchResult(
            "Guide " + guideId,
            "",
            "Snippet",
            "Body",
            guideId,
            "Section",
            "survival",
            "guide-focus"
        );
    }

    private static void assertRoute(
        MainRouteDecisionHelper.RouteState routeState,
        MainRouteDecisionHelper.Surface surface,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        assertEquals(surface, routeState.surface);
        assertEquals(activePhoneTab, routeState.activePhoneTab);
        assertEquals(askLaneActive, routeState.askLaneActive);
    }
}

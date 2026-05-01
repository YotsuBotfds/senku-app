package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailSourceOpenNavigationCoordinatorTest {
    @Test
    public void nullSourceFallsBackToEmptyGuideWithoutAsyncLoad() {
        DetailSourceOpenNavigationCoordinator.Decision decision =
            DetailSourceOpenNavigationCoordinator.decideSourceGuide(null);

        assertEquals("", decision.guideId);
        assertFalse(decision.shouldLoadGuideBeforeOpen);
        assertEquals("", decision.source.guideId);
        assertEquals("", decision.source.title);
        assertEquals("detail.openSourceGuide", decision.harnessTaskLabel);
        assertEquals("openSourceGuide.loadFailed", decision.loadFailureLogLabel);
    }

    @Test
    public void blankGuideIdKeepsSourceAndOpensDirectly() {
        SearchResult source = source("  ");

        DetailSourceOpenNavigationCoordinator.Decision decision =
            DetailSourceOpenNavigationCoordinator.decideSourceGuide(source);

        assertSame(source, decision.source);
        assertEquals("", decision.guideId);
        assertFalse(decision.shouldLoadGuideBeforeOpen);
    }

    @Test
    public void nonBlankGuideIdIsTrimmedAndRequestsLoadBeforeOpen() {
        SearchResult source = source("  GD-898  ");

        DetailSourceOpenNavigationCoordinator.Decision decision =
            DetailSourceOpenNavigationCoordinator.decideSourceGuide(source);

        assertSame(source, decision.source);
        assertEquals("GD-898", decision.guideId);
        assertTrue(decision.shouldLoadGuideBeforeOpen);
    }

    @Test
    public void trimmedGuideIdDoesNotMutateOriginalSourcePayload() {
        SearchResult source = source("  GD-111  ");

        DetailSourceOpenNavigationCoordinator.Decision decision =
            DetailSourceOpenNavigationCoordinator.decideSourceGuide(source);

        assertSame(source, decision.source);
        assertEquals("  GD-111  ", decision.source.guideId);
        assertEquals("GD-111", decision.guideId);
        assertTrue(decision.shouldLoadGuideBeforeOpen);
    }

    @Test
    public void legacyDecisionEntrypointKeepsSourceGuideBehavior() {
        SearchResult source = source("GD-203");

        DetailSourceOpenNavigationCoordinator.Decision decision =
            DetailSourceOpenNavigationCoordinator.decide(source);

        assertSame(source, decision.source);
        assertEquals("GD-203", decision.guideId);
        assertTrue(decision.shouldLoadGuideBeforeOpen);
        assertEquals("detail.openSourceGuide", decision.harnessTaskLabel);
        assertEquals("openSourceGuide.loadFailed", decision.loadFailureLogLabel);
    }

    @Test
    public void crossReferenceNullGuideFallsBackWithoutAsyncLoad() {
        DetailSourceOpenNavigationCoordinator.Decision decision =
            DetailSourceOpenNavigationCoordinator.decideCrossReferenceGuide(null);

        assertEquals("", decision.guideId);
        assertFalse(decision.shouldLoadGuideBeforeOpen);
        assertEquals("", decision.source.guideId);
        assertEquals("", decision.source.title);
        assertEquals("detail.openCrossReferenceGuide", decision.harnessTaskLabel);
        assertEquals("openCrossReferenceGuide.loadFailed", decision.loadFailureLogLabel);
    }

    @Test
    public void crossReferenceNonBlankGuideIdUsesCrossReferenceLabels() {
        SearchResult guide = source(" GD-411 ");

        DetailSourceOpenNavigationCoordinator.Decision decision =
            DetailSourceOpenNavigationCoordinator.decideCrossReferenceGuide(guide);

        assertSame(guide, decision.source);
        assertEquals("GD-411", decision.guideId);
        assertTrue(decision.shouldLoadGuideBeforeOpen);
        assertEquals("detail.openCrossReferenceGuide", decision.harnessTaskLabel);
        assertEquals("openCrossReferenceGuide.loadFailed", decision.loadFailureLogLabel);
    }

    private static SearchResult source(String guideId) {
        return new SearchResult(
            "Poisoning",
            "",
            "Use poisoning source context.",
            "",
            guideId,
            "Unknown ingestion",
            "health",
            "hybrid"
        );
    }
}

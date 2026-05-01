package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class MainGuideOpenControllerTest {
    private final MainGuideOpenController controller = new MainGuideOpenController();

    @Test
    public void blankGuideIdIsIgnored() {
        MainGuideOpenController.Decision decision =
            controller.resolveInitial("   ", "Fallback", 12, guides(), true);

        assertEquals(MainGuideOpenController.Action.IGNORE, decision.action);
        assertEquals("", decision.request.guideId);
        assertEquals("Fallback", decision.request.unavailableLabel);
    }

    @Test
    public void loadedGuideMatchIgnoresCaseAndWhitespace() {
        SearchResult guide = guide("GD-214", "Water Storage");

        MainGuideOpenController.Decision decision = controller.resolveInitial(
            " gd-214 ",
            "Stored water",
            14,
            guides(null, guide),
            true
        );

        assertEquals(MainGuideOpenController.Action.OPEN_GUIDE, decision.action);
        assertSame(guide, decision.guide);
        assertEquals("gd-214", decision.request.guideId);
        assertEquals("Stored water", decision.request.unavailableLabel);
        assertEquals(14, decision.request.unavailableMessageResId);
    }

    @Test
    public void missingLoadedGuideRequestsRepositoryWhenAvailable() {
        MainGuideOpenController.Decision decision =
            controller.resolveInitial(" GD-404 ", "", 0, guides(guide("GD-001", "Known")), true);

        assertEquals(MainGuideOpenController.Action.LOAD_FROM_REPOSITORY, decision.action);
        assertEquals("GD-404", decision.request.guideId);
        assertEquals("GD-404", decision.request.unavailableLabel);
    }

    @Test
    public void missingRepositoryReportsPackUnavailable() {
        MainGuideOpenController.Decision decision =
            controller.resolveInitial("GD-404", "", 0, guides(), false);

        assertEquals(MainGuideOpenController.Action.PACK_UNAVAILABLE, decision.action);
    }

    @Test
    public void repositoryResultOpensWhenPresent() {
        MainGuideOpenController.Request request =
            new MainGuideOpenController.Request("GD-404", "Fallback", 8);
        SearchResult guide = guide("GD-404", "Loaded");

        MainGuideOpenController.Decision decision = controller.resolveRepositoryResult(request, guide);

        assertEquals(MainGuideOpenController.Action.OPEN_GUIDE, decision.action);
        assertSame(guide, decision.guide);
    }

    @Test
    public void repositoryLoaderReceivesNormalizedGuideId() {
        MainGuideOpenController.Request request =
            new MainGuideOpenController.Request(" gd-404 ", "Fallback", 8);
        SearchResult guide = guide("GD-404", "Loaded");

        MainGuideOpenController.Decision decision =
            controller.resolveRepositoryLoad(request, guideId -> {
                assertEquals("gd-404", guideId);
                return guide;
            });

        assertEquals(MainGuideOpenController.Action.OPEN_GUIDE, decision.action);
        assertSame(guide, decision.guide);
    }

    @Test
    public void repositoryLoaderExceptionFallsThroughToUnavailable() {
        MainGuideOpenController.Request request =
            new MainGuideOpenController.Request("GD-404", "Fallback", 8);

        MainGuideOpenController.Decision decision =
            controller.resolveRepositoryLoad(request, guideId -> {
                throw new IllegalStateException("boom");
            });

        assertEquals(MainGuideOpenController.Action.GUIDE_UNAVAILABLE, decision.action);
        assertEquals("Fallback", decision.request.unavailableLabel);
    }

    @Test
    public void repositoryMissResolvesUnavailableLabelFallbacks() {
        MainGuideOpenController.Request request =
            new MainGuideOpenController.Request("", "", 9);

        MainGuideOpenController.Decision ignored = controller.resolveRepositoryResult(request, null);

        assertEquals(MainGuideOpenController.Action.IGNORE, ignored.action);
        assertEquals("selected guide", request.unavailableLabel);

        MainGuideOpenController.Decision unavailable = controller.resolveRepositoryResult(
            new MainGuideOpenController.Request("GD-404", "  ", 9),
            null
        );

        assertEquals(MainGuideOpenController.Action.GUIDE_UNAVAILABLE, unavailable.action);
        assertEquals("GD-404", unavailable.request.unavailableLabel);
        assertEquals(9, unavailable.request.unavailableMessageResId);
    }

    private static List<SearchResult> guides(SearchResult... guides) {
        return Arrays.asList(guides);
    }

    private static SearchResult guide(String guideId, String title) {
        return new SearchResult(title, "", "", "", guideId, "", "", "");
    }
}

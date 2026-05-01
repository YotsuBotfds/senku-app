package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public final class MainCategoryFilterControllerTest {
    @Test
    public void routeFiltersLoadedGuidesAndBuildsSearchChromePresentation() {
        SearchResult rainBarrel = guide("Rain barrel", "", "rainwater storage");
        SearchResult cabinRoof = guide("Cabin roof", "building", "");
        SearchResult sandFilter = guide("Sand filter", "water", "");

        MainCategoryFilterController.FilterRoute route = MainCategoryFilterController.route(
            Arrays.asList(rainBarrel, cabinRoof, sandFilter),
            "water",
            "Water & sanitation"
        );

        assertFalse(route.shouldBrowseGuides());
        assertEquals(2, route.filteredResults().size());
        assertSame(rainBarrel, route.filteredResults().get(0));
        assertSame(sandFilter, route.filteredResults().get(1));
        assertEquals("Water & sanitation (2)", route.searchQueryLabel());
        assertEquals(2, route.resultCount());

        MainResultPublicationPolicy.SearchQueryChromePresentation chrome =
            route.publication().searchQueryChromePresentation();
        assertTrue(chrome.shouldPublish());
        assertEquals("Water & sanitation (2)", chrome.queryLabel());
        assertEquals(2, chrome.resultCount());
        assertRoute(
            route.publication().routeState(),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
    }

    @Test
    public void routeKeepsCategoryResultSurfaceForEmptyMatches() {
        MainCategoryFilterController.FilterRoute route = MainCategoryFilterController.route(
            Collections.singletonList(guide("Cabin roof", "building", "")),
            "medicine",
            "Medicine"
        );

        assertFalse(route.shouldBrowseGuides());
        assertTrue(route.filteredResults().isEmpty());
        assertEquals("Medicine (0)", route.searchQueryLabel());
        assertEquals(0, route.resultCount());
        assertEquals("Medicine (0)", route.publication().searchQueryLabel());
    }

    @Test
    public void routeFallsBackToBrowseWhenNoGuidesAreLoaded() {
        MainCategoryFilterController.FilterRoute route = MainCategoryFilterController.route(
            Collections.emptyList(),
            "water",
            "Water"
        );

        assertTrue(route.shouldBrowseGuides());
        assertTrue(route.filteredResults().isEmpty());
        assertNull(route.publication());
        assertEquals("", route.searchQueryLabel());
        assertEquals(0, route.resultCount());
    }

    private static SearchResult guide(String title, String category, String topicTags) {
        return new SearchResult(
            title,
            "",
            "",
            "",
            "",
            "",
            category,
            "",
            "",
            "",
            "",
            topicTags
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

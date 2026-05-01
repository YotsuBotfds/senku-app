package com.senku.mobile;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class PackAnswerAnchorSelectorTest {
    @Test
    public void nonRouteFocusedQueryKeepsRankedAnchor() {
        SearchResult rankedAnchor = result(
            "Beeswax Candles",
            "GD-120",
            "Wax Rendering",
            "crafts",
            "guide-focus",
            "starter",
            "beeswax",
            "candles,wax"
        );
        SearchResult routedAnchor = result(
            "Tallow Candles",
            "GD-121",
            "Tallow Rendering",
            "crafts",
            "route-focus",
            "starter",
            "tallow",
            "candles,tallow"
        );

        SearchResult selected = PackAnswerAnchorSelector.chooseRankedOrRoutedAnchor(
            PackRepository.QueryTerms.fromQuery("how do i make a candle"),
            rankedAnchor,
            routedAnchor
        );

        assertSame(rankedAnchor, selected);
    }

    @Test
    public void cabinSiteSelectionPreferenceMatchesRepositoryWrapper() {
        SearchResult rankedAnchor = result(
            "Construction & Carpentry",
            "GD-094",
            "Foundations",
            "building",
            "guide-focus",
            "starter",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );
        SearchResult routedAnchor = result(
            "Shelter Site Selection & Hazard Assessment",
            "GD-446",
            "Terrain Analysis",
            "survival",
            "route-focus",
            "safety",
            "cabin_house",
            "site_selection,drainage"
        );
        String query = "how do i choose a safe site and foundation for a small cabin";

        SearchResult direct = PackAnswerAnchorSelector.chooseRankedOrRoutedAnchor(
            PackRepository.QueryTerms.fromQuery(query),
            rankedAnchor,
            routedAnchor
        );
        SearchResult repositoryWrapper = PackRepository.selectAnswerAnchorForTest(
            query,
            rankedAnchor,
            routedAnchor
        );

        assertSame(routedAnchor, direct);
        assertSame(repositoryWrapper, direct);
    }

    private static SearchResult result(
        String title,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode,
        String contentRole,
        String structureType,
        String topicTags
    ) {
        String text = title + " " + sectionHeading + " " + topicTags;
        return new SearchResult(
            title,
            "",
            text,
            text,
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            contentRole,
            "long_term",
            structureType,
            topicTags
        );
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailActivityRelatedGuideShapingTest {
    @Test
    public void rainShelterAnswerModeShowsPrimitiveTechnologyAsFourthPhoneGuide() {
        List<SearchResult> shaped = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            true,
            "GD-345",
            Arrays.asList(
                result("GD-294", "Cave Shelter Systems & Cold-Weather"),
                result("GD-695", "Hurricane & Severe Storm Sheltering"),
                result("GD-484", "Insulation Materials & Cold-Soak"),
                result("GD-109", "Natural Building Materials"),
                result("GD-027", "Primitive Technology & Stone Age")
            )
        );

        assertEquals("GD-294", shaped.get(0).guideId);
        assertEquals("GD-695", shaped.get(1).guideId);
        assertEquals("GD-484", shaped.get(2).guideId);
        assertEquals("GD-027", shaped.get(3).guideId);
        assertEquals("GD-109", shaped.get(4).guideId);
    }

    @Test
    public void unrelatedAnchorKeepsRepositoryOrder() {
        List<SearchResult> shaped = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            true,
            "GD-214",
            Arrays.asList(
                result("GD-109", "Natural Building Materials"),
                result("GD-027", "Primitive Technology & Stone Age")
            )
        );

        assertEquals("GD-109", shaped.get(0).guideId);
        assertEquals("GD-027", shaped.get(1).guideId);
    }

    @Test
    public void rainShelterAnswerModeInjectsPrimitiveTechnologyWhenRepositoryOmitsIt() {
        List<SearchResult> shaped = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            true,
            "GD-345",
            Arrays.asList(
                result("GD-294", "Cave Shelter Systems & Cold-Weather"),
                result("GD-695", "Hurricane & Severe Storm Sheltering"),
                result("GD-484", "Insulation Materials & Cold-Soak"),
                result("GD-109", "Natural Building Materials")
            )
        );

        assertEquals("GD-294", shaped.get(0).guideId);
        assertEquals("GD-695", shaped.get(1).guideId);
        assertEquals("GD-484", shaped.get(2).guideId);
        assertEquals("GD-027", shaped.get(3).guideId);
        assertEquals("Primitive Technology & Stone Age", shaped.get(3).title);
        assertEquals("GD-109", shaped.get(4).guideId);
    }

    @Test
    public void disabledReviewModeKeepsRainShelterRelatedGuideOrder() {
        List<SearchResult> shaped = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            false,
            "GD-345",
            Arrays.asList(
                result("GD-294", "Cave Shelter Systems & Cold-Weather"),
                result("GD-695", "Hurricane & Severe Storm Sheltering"),
                result("GD-484", "Insulation Materials & Cold-Soak"),
                result("GD-109", "Natural Building Materials"),
                result("GD-027", "Primitive Technology & Stone Age")
            )
        );

        assertEquals("GD-109", shaped.get(3).guideId);
        assertEquals("GD-027", shaped.get(4).guideId);
    }

    @Test
    public void disabledReviewModeDoesNotInjectPrimitiveTechnologyWhenRepositoryOmitsIt() {
        List<SearchResult> shaped = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            false,
            "GD-345",
            Arrays.asList(
                result("GD-294", "Cave Shelter Systems & Cold-Weather"),
                result("GD-695", "Hurricane & Severe Storm Sheltering"),
                result("GD-484", "Insulation Materials & Cold-Soak"),
                result("GD-109", "Natural Building Materials")
            )
        );

        assertEquals(4, shaped.size());
        assertEquals("GD-109", shaped.get(3).guideId);
    }

    @Test
    public void productionRelatedGuideAnchorUsesFirstSourceWithoutRainShelterPromotion() {
        SearchResult anchor = result("GD-220", "Abrasives Manufacturing");
        SearchResult rainShelter = resultWithTopicTags(
            "GD-345",
            "Tarp & Cord Shelters",
            "rain shelter tarp cord"
        );

        SearchResult selected = DetailActivity.selectedSourceForRelatedGuideGraphForState(
            true,
            false,
            "",
            Arrays.asList(anchor, rainShelter)
        );

        assertEquals("GD-220", selected.guideId);
    }

    @Test
    public void reviewModeRelatedGuideAnchorKeepsRainShelterPromotion() {
        SearchResult anchor = result("GD-220", "Abrasives Manufacturing");
        SearchResult rainShelter = resultWithTopicTags(
            "GD-345",
            "Tarp & Cord Shelters",
            "rain shelter tarp cord"
        );

        SearchResult selected = DetailActivity.selectedSourceForRelatedGuideGraphForState(
            true,
            true,
            "",
            Arrays.asList(anchor, rainShelter)
        );

        assertEquals("GD-345", selected.guideId);
    }

    @Test
    public void orderedReviewSourceStackPreservesExplicitSelectedRelatedGuideAnchor() {
        SearchResult topic = sourceWithBody(
            "GD-345",
            "Tarp & Cord Shelters",
            "Ridgeline pitch",
            "A simple tarp and cord ridgeline shelter keeps rain away from camp.",
            "rain shelter tarp cord"
        );
        SearchResult related = sourceWithBody(
            "GD-132",
            "Foundry & Metal Casting",
            "Pour zone",
            "Keep foundry water and bystanders away before metal casting begins.",
            ""
        );
        SearchResult anchor = sourceWithBody(
            "GD-220",
            "Abrasives Manufacturing",
            "Foundry preparation",
            "Abrasives manufacturing starts with controlled foundry safety checks.",
            ""
        );
        List<SearchResult> ordered = DetailSourceStackPolicy.orderAnswerSourceStack(
            true,
            Arrays.asList(topic, related, anchor)
        );
        String selectedKey = DetailProvenancePresentationFormatter.buildSourceSelectionKey(topic);

        SearchResult selected = DetailActivity.selectedSourceForRelatedGuideGraphForState(
            true,
            true,
            selectedKey,
            ordered
        );

        assertEquals("GD-220", ordered.get(0).guideId);
        assertEquals("GD-345", selected.guideId);
        assertEquals("Ridgeline pitch", selected.sectionHeading);
    }

    @Test
    public void emergencyHeaderFlatBandKeepsCompactPadding() {
        assertEquals(12, DetailActivity.resolveEmergencyHeaderHorizontalPaddingDp());
        assertEquals(7, DetailActivity.resolveEmergencyHeaderVerticalPaddingDp());
    }

    private static SearchResult result(String guideId, String title) {
        return new SearchResult(title, "", "", "", guideId, "", "", "");
    }

    private static SearchResult resultWithTopicTags(String guideId, String title, String topicTags) {
        return new SearchResult(title, "", "", "", guideId, "", "", "", "", "", "", topicTags);
    }

    private static SearchResult sourceWithBody(
        String guideId,
        String title,
        String sectionHeading,
        String body,
        String topicTags
    ) {
        return new SearchResult(
            title,
            "",
            body,
            body,
            guideId,
            sectionHeading,
            "test",
            "hybrid",
            "",
            "",
            "",
            topicTags
        );
    }
}

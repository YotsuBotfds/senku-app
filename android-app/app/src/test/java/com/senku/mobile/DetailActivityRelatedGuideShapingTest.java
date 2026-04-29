package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailActivityRelatedGuideShapingTest {
    @Test
    public void rainShelterAnswerModeShowsPrimitiveTechnologyAsFourthPhoneGuide() {
        List<SearchResult> shaped = DetailActivity.shapeAnswerModeRelatedGuides(
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
        List<SearchResult> shaped = DetailActivity.shapeAnswerModeRelatedGuides(
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
        List<SearchResult> shaped = DetailActivity.shapeAnswerModeRelatedGuides(
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
    public void emergencyHeaderFlatBandKeepsCompactPadding() {
        assertEquals(12, DetailActivity.resolveEmergencyHeaderHorizontalPaddingDp());
        assertEquals(7, DetailActivity.resolveEmergencyHeaderVerticalPaddingDp());
    }

    private static SearchResult result(String guideId, String title) {
        return new SearchResult(title, "", "", "", guideId, "", "", "");
    }
}

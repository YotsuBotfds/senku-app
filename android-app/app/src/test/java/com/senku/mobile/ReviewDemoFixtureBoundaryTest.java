package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class ReviewDemoFixtureBoundaryTest {
    private static final String RAIN_SHELTER_QUERY =
        "How do I build a simple rain shelter from tarp and cord?";

    @Test
    public void rainShelterAnswerFixturesStayInertWhenModeDisabledOrDenied() {
        for (boolean productReviewMode : disabledOrDeniedReviewModes()) {
            List<SearchResult> adjacent = rainShelterAdjacentGuides();

            assertEquals(
                "",
                ReviewDemoPolicy.buildRainShelterUncertainFitAnswerBody(
                    productReviewMode,
                    RAIN_SHELTER_QUERY,
                    adjacent,
                    false
                )
            );

            List<SearchResult> shapedSources = ReviewDemoPolicy.shapeRainShelterUncertainFitSources(
                productReviewMode,
                RAIN_SHELTER_QUERY,
                adjacent,
                false
            );
            assertEquals(adjacent, shapedSources);
            assertFalse(containsGuideId(shapedSources, "GD-220"));
            assertFalse(containsGuideId(shapedSources, "GD-132"));
        }
    }

    @Test
    public void searchAndPreviewFixturesStayInertWhenModeDisabledOrDenied() {
        for (boolean productReviewMode : disabledOrDeniedReviewModes()) {
            List<SearchResult> liveResults = Arrays.asList(
                result("GD-501", "Live Rain Shelter Result", "live rain shelter snippet"),
                result("GD-502", "Live Water Result", "live water snippet")
            );
            ReviewDemoPolicy.GuideLookup forbiddenLookup = guideId -> {
                throw new AssertionError("Review fixture lookup should be disabled for " + guideId);
            };
            SearchResult reviewLookingRow = new SearchResult(
                "Survival Basics & First 72 Hours",
                "GD-023 | survival | review",
                "fixture-looking snippet",
                "fixture-looking body",
                "GD-023",
                "",
                "",
                ""
            );

            assertSame(
                liveResults,
                ReviewDemoPolicy.shapeSearchResults(
                    "rain shelter",
                    productReviewMode,
                    liveResults,
                    forbiddenLookup
                )
            );
            assertEquals(
                "Search  rain shelter - 2 results",
                ReviewDemoPolicy.appendSearchLatency(
                    "Search  rain shelter - 2 results",
                    "rain shelter",
                    productReviewMode
                )
            );
            assertFalse(
                ReviewDemoPolicy.shouldSuppressSearchRowLinkedGuideCue(
                    productReviewMode,
                    "rain shelter",
                    reviewLookingRow
                )
            );
            assertEquals(
                "live meta",
                ReviewDemoPolicy.shapeTabletPreviewMeta(productReviewMode, reviewLookingRow, "live meta")
            );
            assertEquals(
                "live body",
                ReviewDemoPolicy.shapeTabletPreviewBody(productReviewMode, reviewLookingRow, "live body")
            );
        }
    }

    @Test
    public void enabledReviewModeDoesNotApplySearchFixturesToNonReviewQuery() {
        List<SearchResult> liveResults = Arrays.asList(
            result("GD-501", "Live Water Result", "live water snippet"),
            result("GD-502", "Live Filter Result", "live filter snippet")
        );
        ReviewDemoPolicy.GuideLookup forbiddenLookup = guideId -> {
            throw new AssertionError("Non-review query must not load review fixture guide " + guideId);
        };
        SearchResult reviewLookingRow = new SearchResult(
            "Survival Basics & First 72 Hours",
            "GD-023 | survival | review",
            "fixture-looking snippet",
            "fixture-looking body",
            "GD-023",
            "",
            "",
            ""
        );

        assertFalse(ReviewDemoPolicy.shouldShapeReviewSearchResults(true, "water"));
        assertSame(
            liveResults,
            ReviewDemoPolicy.shapeSearchResults("water", true, liveResults, forbiddenLookup)
        );
        assertEquals(
            "Search  water - 2 results",
            ReviewDemoPolicy.appendSearchLatency("Search  water - 2 results", "water", true)
        );
        assertFalse(
            ReviewDemoPolicy.shouldSuppressSearchRowLinkedGuideCue(
                true,
                "water",
                reviewLookingRow
            )
        );
        assertNoRainShelterFixtureContent(liveResults);
    }

    @Test
    public void relatedGuideAndRecentThreadFixturesStayInertWhenModeDisabledOrDenied() {
        for (boolean productReviewMode : disabledOrDeniedReviewModes()) {
            ArrayList<SearchResult> relatedGuides = new ArrayList<>(Arrays.asList(
                result("GD-294", "Cave Shelter Systems & Cold-Weather", ""),
                result("GD-695", "Hurricane & Severe Storm Sheltering", ""),
                result("GD-484", "Insulation Materials & Cold-Soak", ""),
                result("GD-109", "Natural Building Materials", "")
            ));

            ArrayList<SearchResult> shapedRelatedGuides =
                ReviewDemoPolicy.shapeAnswerModeRelatedGuides(productReviewMode, "GD-345", relatedGuides);

            assertEquals(relatedGuides, shapedRelatedGuides);
            assertFalse(containsGuideId(shapedRelatedGuides, "GD-027"));
            assertEquals(
                "Live placeholder",
                ReviewDemoPolicy.placeholderRecentThreadQuestion(productReviewMode, 0, "Live placeholder")
            );
            assertEquals(
                "Live recent thread",
                ReviewDemoPolicy.shapeRecentThreadLabel(productReviewMode, null, 0, "Live recent thread")
            );
        }
    }

    @Test
    public void productionModeSearchAndRelatedGuideShapingDoNotEmitRainShelterFixtureContent() {
        boolean productionMode = ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, true, false);
        List<SearchResult> liveSearchResults = Arrays.asList(
            result("GD-501", "Live Rain Shelter Notes", "Use the current catalog result."),
            result("GD-502", "Live Knot Reference", "Use the current knot result.")
        );
        ReviewDemoPolicy.GuideLookup forbiddenLookup = guideId -> {
            throw new AssertionError("Production mode must not load review fixture guide " + guideId);
        };

        List<SearchResult> shapedSearchResults = ReviewDemoPolicy.shapeSearchResults(
            "rain shelter",
            productionMode,
            liveSearchResults,
            forbiddenLookup
        );

        assertSame(liveSearchResults, shapedSearchResults);
        assertNoRainShelterFixtureContent(shapedSearchResults);

        ArrayList<SearchResult> liveRelatedGuides = new ArrayList<>(Arrays.asList(
            result("GD-501", "Live Shelter Site Selection", "Avoid runoff channels."),
            result("GD-109", "Live Natural Materials", "Inspect branches before use."),
            result("GD-502", "Live Knot Reference", "Choose a knot that can be untied.")
        ));

        ArrayList<SearchResult> shapedRelatedGuides = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            productionMode,
            "GD-345",
            liveRelatedGuides
        );

        assertEquals(liveRelatedGuides, shapedRelatedGuides);
        assertNoRainShelterFixtureContent(shapedRelatedGuides);
    }

    private static boolean[] disabledOrDeniedReviewModes() {
        return new boolean[] {
            false,
            ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, false, true),
            ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, true, false),
            ReviewDemoPolicy.resolveProductReviewModeForTest(false, true, true, true)
        };
    }

    private static List<SearchResult> rainShelterAdjacentGuides() {
        return Arrays.asList(
            new SearchResult(
                "Tarp & Cord Shelters",
                "",
                "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                "Pitch the low edge toward weather so rain sheds away from the sheltered area.",
                "GD-345",
                "Tarp & Cord Shelters",
                "survival",
                "lexical",
                "topic",
                "immediate",
                "emergency_shelter",
                "tarp,cord,rain_shelter,ridgeline"
            ),
            result("GD-501", "Live Shelter Site Selection", "Avoid runoff channels before staking shelter.")
        );
    }

    private static SearchResult result(String guideId, String title, String snippet) {
        return new SearchResult(
            title,
            "",
            snippet,
            title + " body",
            guideId,
            "",
            "",
            "lexical",
            "",
            "",
            "",
            ""
        );
    }

    private static void assertNoRainShelterFixtureContent(List<SearchResult> results) {
        String serialized = serializeResults(results);
        assertFalse(serialized.contains("Survival Basics & First 72 Hours"));
        assertFalse(serialized.contains("Primitive Technology & Stone Age"));
        assertFalse(serialized.contains("Tarp & Cord Shelters"));
        assertFalse(serialized.contains("Abrasives Manufacturing"));
        assertFalse(serialized.contains("Foundry & Metal Casting"));
        assertFalse(serialized.contains("GD-023 | survival | review"));
        assertFalse(serialized.contains("GD-027"));
        assertFalse(serialized.contains("A simple ridgeline shelter requires only tarp, cord"));
        assertFalse(serialized.contains("Build a ridgeline first"));
    }

    private static String serializeResults(List<SearchResult> results) {
        StringBuilder builder = new StringBuilder();
        if (results == null) {
            return "";
        }
        for (SearchResult result : results) {
            if (result == null) {
                continue;
            }
            builder.append(result.title).append('\n');
            builder.append(result.subtitle).append('\n');
            builder.append(result.snippet).append('\n');
            builder.append(result.body).append('\n');
            builder.append(result.guideId).append('\n');
            builder.append(result.sectionHeading).append('\n');
            builder.append(result.category).append('\n');
            builder.append(result.retrievalMode).append('\n');
            builder.append(result.contentRole).append('\n');
            builder.append(result.timeHorizon).append('\n');
            builder.append(result.structureType).append('\n');
            builder.append(result.topicTags).append('\n');
        }
        return builder.toString();
    }

    private static boolean containsGuideId(List<SearchResult> results, String guideId) {
        if (results == null) {
            return false;
        }
        for (SearchResult result : results) {
            if (result != null && guideId.equals(result.guideId)) {
                return true;
            }
        }
        return false;
    }
}

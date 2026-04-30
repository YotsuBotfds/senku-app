package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class ReviewDemoPolicyTest {
    @Test
    public void reviewHomeCategoryCountsMatchTargetMockContractWhenEnabled() {
        assertEquals(84, ReviewDemoPolicy.displayHomeCategoryCount(true, "shelter", 7));
        assertEquals(67, ReviewDemoPolicy.displayHomeCategoryCount(true, "water", 7));
        assertEquals(52, ReviewDemoPolicy.displayHomeCategoryCount(true, "fire", 7));
        assertEquals(91, ReviewDemoPolicy.displayHomeCategoryCount(true, "food", 7));
        assertEquals(73, ReviewDemoPolicy.displayHomeCategoryCount(true, "medicine", 7));
        assertEquals(119, ReviewDemoPolicy.displayHomeCategoryCount(true, "tools", 7));
        assertEquals(0, ReviewDemoPolicy.displayHomeCategoryCount(true, "communications", 7));
    }

    @Test
    public void disabledModeKeepsActualHomeCategoryCounts() {
        assertEquals(7, ReviewDemoPolicy.displayHomeCategoryCount(false, "shelter", 7));
        assertEquals(3, ReviewDemoPolicy.displayHomeCategoryCount(false, "communications", 3));
    }

    @Test
    public void sourceStackDemoRequiresExplicitReviewMode() {
        assertFalse(ReviewDemoPolicy.isSourceStackDemoEnabled(false));
        assertTrue(ReviewDemoPolicy.isSourceStackDemoEnabled(true));
    }

    @Test
    public void reviewSearchLatencyOnlyAppliesToTargetMockQueryWhenEnabled() {
        assertEquals(
            "Search  rain shelter - 4 results \u2022 12MS",
            ReviewDemoPolicy.appendSearchLatency("Search  rain shelter - 4 results", "rain shelter", true)
        );
        assertEquals(
            "Search  water - 4 results",
            ReviewDemoPolicy.appendSearchLatency("Search  water - 4 results", "water", true)
        );
    }

    @Test
    public void disabledModeDoesNotAppendSearchLatency() {
        assertEquals(
            "Search  rain shelter - 4 results",
            ReviewDemoPolicy.appendSearchLatency("Search  rain shelter - 4 results", "rain shelter", false)
        );
    }

    @Test
    public void reviewRainShelterSearchUsesTargetOrderAndDisplayContentWhenEnabled() {
        List<SearchResult> results = ReviewDemoPolicy.shapeSearchResults(
            "rain shelter",
            true,
            Arrays.asList(
                guideWithId("Primitive Shelter Construction Techniques", "GD-345"),
                guideWithId("Shelter Site Selection & Hazard Assessment", "GD-446"),
                guideWithId("Survival Basics & First 72 Hours", "GD-023"),
                guideWithId("Underground Shelter & Bunker Construction", "GD-873")
            ),
            null
        );

        assertEquals(4, results.size());
        assertEquals("GD-023", results.get(0).guideId);
        assertEquals("GD-027", results.get(1).guideId);
        assertEquals("GD-345", results.get(2).guideId);
        assertEquals("GD-294", results.get(3).guideId);
        assertEquals("Tarp & Cord Shelters", results.get(2).title);
        assertEquals("A simple ridgeline shelter requires only tarp, cord, and two anchor points...", results.get(2).snippet);
        assertEquals("topic", results.get(2).contentRole);
        assertEquals("immediate", results.get(2).timeHorizon);
    }

    @Test
    public void disabledModeDoesNotShapeSearchResults() {
        List<SearchResult> original = Arrays.asList(
            guideWithId("Water Storage", "GD-214"),
            guideWithId("Sand Filter", "GD-035")
        );

        assertSame(original, ReviewDemoPolicy.shapeSearchResults("rain shelter", false, original, null));
        assertSame(original, ReviewDemoPolicy.shapeSearchResults("water", true, original, null));
    }

    @Test
    public void reviewRainShelterUncertainFitAnswerRequiresEnabledPolicy() {
        List<SearchResult> adjacent = rainShelterAdjacentGuides();

        assertEquals(
            "",
            ReviewDemoPolicy.buildRainShelterUncertainFitAnswerBody(
                false,
                "How do I build a simple rain shelter from tarp and cord?",
                adjacent,
                false
            )
        );

        String answerBody = ReviewDemoPolicy.buildRainShelterUncertainFitAnswerBody(
            true,
            "How do I build a simple rain shelter from tarp and cord?",
            adjacent,
            false
        );

        assertTrue(answerBody.startsWith("ANSWER\nBuild a ridgeline first"));
        assertTrue(answerBody.contains("\n\nFIELD STEPS\n1. Tie a taut ridgeline"));
    }

    @Test
    public void reviewRainShelterUncertainFitSourcesRequireEnabledPolicy() {
        List<SearchResult> adjacent = rainShelterAdjacentGuides();

        List<SearchResult> disabled = ReviewDemoPolicy.shapeRainShelterUncertainFitSources(
            false,
            "How do I build a simple rain shelter from tarp and cord?",
            adjacent,
            false
        );
        assertEquals(2, disabled.size());
        assertEquals("Primitive Shelter Construction Techniques", disabled.get(0).title);
        assertFalse("GD-220".equals(disabled.get(0).guideId));

        List<SearchResult> enabled = ReviewDemoPolicy.shapeRainShelterUncertainFitSources(
            true,
            "How do I build a simple rain shelter from tarp and cord?",
            adjacent,
            false
        );

        assertEquals(3, enabled.size());
        assertEquals("GD-220", enabled.get(0).guideId);
        assertEquals("Abrasives Manufacturing", enabled.get(0).title);
        assertEquals("GD-132", enabled.get(1).guideId);
        assertEquals("Foundry & Metal Casting", enabled.get(1).title);
        assertEquals("GD-345", enabled.get(2).guideId);
        assertEquals("Tarp & Cord Shelters", enabled.get(2).title);
        assertEquals("Tarp & Cord Shelters", enabled.get(2).sectionHeading);
    }

    @Test
    public void reviewManualRecentThreadsUseTargetMockExamplesWhenEnabled() {
        long fourHoursTwentyOneMinutesAgo = System.currentTimeMillis() - ((4L * 60L + 21L) * 60_000L);
        ChatSessionStore.ConversationPreview first = preview(
            "can I make a rain shelter with cord",
            "GD-345",
            "",
            ReviewedCardMetadata.empty(),
            fourHoursTwentyOneMinutesAgo
        );
        ChatSessionStore.ConversationPreview fire = preview(
            "how do I start a fire in rain",
            "GD-394",
            "deterministic-fire",
            ReviewedCardMetadata.empty(),
            fourHoursTwentyOneMinutesAgo
        );
        ChatSessionStore.ConversationPreview pot = preview(
            "boil water without a safe pot",
            "GD-094",
            "",
            new ReviewedCardMetadata("card-1", "GD-094", "reviewed", "", "reviewed_card_runtime", Collections.emptyList()),
            System.currentTimeMillis() - (25L * 60L * 60L * 1000L)
        );

        assertEquals(
            "How do I build a simple rain shelter...\nGD-345 \u2022 04:21 \u2022 UNSURE",
            ReviewDemoPolicy.shapeRecentThreadLabel(true, first, 0, "default")
        );
        assertEquals(
            "Best tinder when materials are wet\nGD-027 \u2022 04:08 \u2022 CONFIDENT",
            ReviewDemoPolicy.shapeRecentThreadLabel(true, fire, 1, "default")
        );
        assertEquals(
            "Boil water without a fire-safe pot\nGD-094 \u2022 YESTERDAY \u2022 CONFIDENT",
            ReviewDemoPolicy.shapeRecentThreadLabel(true, pot, 2, "default")
        );
    }

    @Test
    public void disabledModeKeepsRecentThreadLabelDefault() {
        assertEquals(
            "Original recent thread",
            ReviewDemoPolicy.shapeRecentThreadLabel(false, null, 0, "Original recent thread")
        );
    }

    private static List<SearchResult> rainShelterAdjacentGuides() {
        return Arrays.asList(
            new SearchResult(
                "Primitive Shelter Construction Techniques",
                "",
                "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                "",
                "GD-345",
                "Wood Quality Evaluation for Shelter Construction",
                "survival",
                "lexical",
                "",
                "",
                "emergency_shelter",
                "foundation,weatherproofing,site_selection"
            ),
            new SearchResult(
                "Primitive Shelter Construction Techniques",
                "",
                "Pile leaves and boughs over a small frame.",
                "",
                "GD-345",
                "Debris Hut (Emergency Shelter)",
                "survival",
                "lexical"
            )
        );
    }

    private static SearchResult guideWithId(String title, String guideId) {
        return new SearchResult(
            title,
            "",
            title + " snippet",
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

    private static ChatSessionStore.ConversationPreview preview(
        String question,
        String guideId,
        String ruleId,
        ReviewedCardMetadata metadata,
        long lastActivityEpoch
    ) {
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            question,
            "",
            "answer",
            Collections.emptyList(),
            Arrays.asList(guideWithId(guideId + " title", guideId)),
            ruleId,
            metadata,
            null,
            lastActivityEpoch
        );
        return new ChatSessionStore.ConversationPreview("conversation-" + guideId, turn, 1, lastActivityEpoch);
    }
}

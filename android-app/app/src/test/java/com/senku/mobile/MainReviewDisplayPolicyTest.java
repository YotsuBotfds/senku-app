package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public final class MainReviewDisplayPolicyTest {
    @Test
    public void homeCategoryCountsMatchReviewPolicyGateAndFixtures() {
        MainReviewDisplayPolicy enabled = new MainReviewDisplayPolicy(true);
        MainReviewDisplayPolicy disabled = new MainReviewDisplayPolicy(false);

        assertEquals(
            84,
            enabled.displayHomeCategoryCount("shelter", 7, Collections.singletonList(guide("Guide", "shelter")), true)
        );
        assertEquals(
            7,
            enabled.displayHomeCategoryCount("shelter", 7, Collections.singletonList(guide("Guide", "shelter")), false)
        );
        assertEquals(
            7,
            enabled.displayHomeCategoryCount("shelter", 7, Collections.emptyList(), true)
        );
        assertEquals(
            7,
            disabled.displayHomeCategoryCount("shelter", 7, Collections.singletonList(guide("Guide", "shelter")), true)
        );
    }

    @Test
    public void homeSubtitleAndManualStatusMatchMainActivityParityHooks() {
        MainReviewDisplayPolicy enabled = new MainReviewDisplayPolicy(true);
        MainReviewDisplayPolicy disabled = new MainReviewDisplayPolicy(false);

        assertEquals(MainActivity.buildHomeSubtitleTextForTest(271, true), enabled.homeSubtitle(271));
        assertEquals(MainActivity.buildHomeSubtitleTextForTest(271, false), disabled.homeSubtitle(271));
        assertEquals(
            MainActivity.compactManualHomeStatusForTest("Ready offline | 754 guides", true, true),
            enabled.manualHomeStatus("Ready offline | 754 guides", true)
        );
        assertEquals(
            MainActivity.compactManualHomeStatusForTest("Ready offline | 754 guides", true, false),
            disabled.manualHomeStatus("Ready offline | 754 guides", true)
        );
    }

    @Test
    public void searchLatencyHeadersMatchMainActivityParityHooks() {
        MainReviewDisplayPolicy enabled = new MainReviewDisplayPolicy(true);
        MainReviewDisplayPolicy disabled = new MainReviewDisplayPolicy(false);

        assertEquals(
            MainActivity.buildPhoneSearchHeaderForTest("rain shelter", 4, true),
            enabled.phoneSearchHeader("rain shelter", 4)
        );
        assertEquals(
            MainActivity.buildPhoneSearchHeaderForTest("rain shelter", 4, false),
            disabled.phoneSearchHeader("rain shelter", 4)
        );
        assertEquals(
            MainActivity.buildSearchChromeCountLabelForTest("rain shelter", 4, true),
            enabled.searchChromeCountLabel("rain shelter", 4)
        );
        String rainShelterHeader = "Search rain shelter - 4 results";
        String reviewLatencyHeader = enabled.searchLatency(rainShelterHeader, "rain shelter");
        assertTrue(reviewLatencyHeader.startsWith(rainShelterHeader + " "));
        assertFalse(rainShelterHeader.equals(reviewLatencyHeader));
        assertEquals(rainShelterHeader, disabled.searchLatency(rainShelterHeader, "rain shelter"));
        assertEquals(rainShelterHeader, enabled.searchLatency(rainShelterHeader, "water"));
    }

    @Test
    public void tabletSearchHeaderDecisionLivesBehindReviewDisplayPolicy() {
        MainReviewDisplayPolicy enabled = new MainReviewDisplayPolicy(true);
        MainReviewDisplayPolicy disabled = new MainReviewDisplayPolicy(false);

        assertEquals(
            ReviewDemoPolicy.buildTabletSearchHeader(true, "rain shelter", 4),
            enabled.tabletSearchHeader("rain shelter", 4)
        );
        assertEquals(
            ReviewDemoPolicy.buildTabletSearchHeader(false, "rain shelter", 4),
            disabled.tabletSearchHeader("rain shelter", 4)
        );
        assertEquals("Search - 1 result", enabled.tabletSearchHeader("guides", 1));
        assertEquals("Search - 2 results", enabled.tabletSearchHeader("   ", 2));
        assertEquals(
            enabled.tabletSearchHeader("rain shelter", 4),
            MainActivity.buildTabletSearchHeaderForTest("rain shelter", 4, true)
        );
    }

    @Test
    public void tabletPreviewCopyMatchesMainActivityParityHooks() {
        SearchResult reviewResult = ReviewDemoPolicy.shapeSearchResults(
            "rain shelter",
            true,
            Arrays.asList(guideWithId("Survival Basics & First 72 Hours", "GD-023")),
            null
        ).get(0);
        MainReviewDisplayPolicy enabled = new MainReviewDisplayPolicy(true);
        MainReviewDisplayPolicy disabled = new MainReviewDisplayPolicy(false);

        assertEquals(MainActivity.buildTabletPreviewMetaForTest(reviewResult, true), enabled.tabletPreviewMeta(reviewResult));
        assertEquals(MainActivity.buildTabletPreviewBodyForTest(reviewResult, true), enabled.tabletPreviewBody(reviewResult));
        assertEquals(MainActivity.buildTabletPreviewMetaForTest(reviewResult, false), disabled.tabletPreviewMeta(reviewResult));
        assertEquals(MainActivity.buildTabletPreviewBodyForTest(reviewResult, false), disabled.tabletPreviewBody(reviewResult));
    }

    @Test
    public void searchRowVisualStateGateFollowsReviewMode() {
        assertTrue(new MainReviewDisplayPolicy(true).searchRowVisualStateEnabled());
        assertFalse(new MainReviewDisplayPolicy(false).searchRowVisualStateEnabled());
        assertTrue(MainReviewDisplayPolicy.searchRowVisualStateEnabled(true));
        assertFalse(MainReviewDisplayPolicy.searchRowVisualStateEnabled(false));
    }

    @Test
    public void manualHomeRecentThreadLabelMatchesReviewDemoPolicyDecision() {
        long fourHoursTwentyOneMinutesAgo = System.currentTimeMillis() - ((4L * 60L + 21L) * 60_000L);
        ChatSessionStore.ConversationPreview preview = preview(
            "can I make a rain shelter with cord",
            "GD-345",
            "",
            ReviewedCardMetadata.empty(),
            fourHoursTwentyOneMinutesAgo
        );
        MainReviewDisplayPolicy enabled = new MainReviewDisplayPolicy(true);
        MainReviewDisplayPolicy disabled = new MainReviewDisplayPolicy(false);

        assertEquals(
            ReviewDemoPolicy.manualHomeRecentThreadLabel(true, preview, 0),
            enabled.manualHomeRecentThreadLabel(preview, 0)
        );
        assertEquals(
            ReviewDemoPolicy.manualHomeRecentThreadLabel(false, preview, 0),
            disabled.manualHomeRecentThreadLabel(preview, 0)
        );
    }

    @Test
    public void categoryFixtureGateKeepsModeShellAndGuideChecksTogether() {
        assertTrue(MainReviewDisplayPolicy.shouldUseHomeCategoryFixtureCounts(
            true,
            true,
            Collections.singletonList(guide("Guide", "water"))
        ));
        assertFalse(MainReviewDisplayPolicy.shouldUseHomeCategoryFixtureCounts(
            false,
            true,
            Collections.singletonList(guide("Guide", "water"))
        ));
        assertFalse(MainReviewDisplayPolicy.shouldUseHomeCategoryFixtureCounts(
            true,
            false,
            Collections.singletonList(guide("Guide", "water"))
        ));
        assertFalse(MainReviewDisplayPolicy.shouldUseHomeCategoryFixtureCounts(true, true, Collections.emptyList()));
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

    private static SearchResult guide(String title, String category) {
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
            ""
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
}

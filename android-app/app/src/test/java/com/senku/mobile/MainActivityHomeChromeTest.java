package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.home.CategoryShelfItemModel;
import com.senku.ui.home.CategoryShelfLayoutMode;
import com.senku.ui.primitives.BottomTabDestination;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class MainActivityHomeChromeTest {
    @Test
    public void productReviewModeDefaultsOffWithoutExplicitIntentExtra() {
        assertFalse(MainActivity.resolveProductReviewModeForTest(false, false));
        assertFalse(MainActivity.resolveProductReviewModeForTest(false, true));
    }

    @Test
    public void productReviewModeRejectsRawIntentExtraWithoutAuthorization() {
        assertFalse(MainActivity.resolveProductReviewModeForTest(true, false));
        assertFalse(MainActivity.resolveProductReviewModeForTest(true, true));
    }

    @Test
    public void productReviewModeAllowsAuthorizedDebugAutomationPath() {
        assertTrue(MainActivity.resolveProductReviewModeForTest(true, true, true, true));
    }

    @Test
    public void productReviewModeRejectsAuthorizedMarkerOutsideDebugBuilds() {
        assertFalse(MainActivity.resolveProductReviewModeForTest(true, true, true, false));
    }

    @Test
    public void categoryShelfModelsAreSortedByCountThenContractOrder() {
        List<CategoryShelfItemModel> items = MainActivity.buildHomeChromeCategoryShelfItems(
            Arrays.asList(
                guide("Solar still", "water", ""),
                guide("Rainwater barrel", "", ""),
                guide("Camp stove", "fire", ""),
                guide("Signal mast", "communications", ""),
                guide("Antenna basics", "", "radio antenna"),
                guide("Clinic triage", "medicine", ""),
                guide("Root cellar", "food", ""),
                guide("Tool repair", "tools", ""),
                guide("Cabin roof", "shelter", "")
            ),
            false
        );

        assertEquals("water", items.get(0).getBucketKey());
        assertEquals("communications", items.get(1).getBucketKey());
        assertEquals("shelter", items.get(2).getBucketKey());
        assertEquals("fire", items.get(3).getBucketKey());
        assertEquals("medicine", items.get(4).getBucketKey());
        assertEquals("food", items.get(5).getBucketKey());
        assertEquals("tools", items.get(6).getBucketKey());
    }

    @Test
    public void categoryShelfModelCarriesLabelCountAccentAndDescription() {
        CategoryShelfItemModel item = MainActivity.buildHomeChromeCategoryShelfItems(
            Arrays.asList(
                guide("Rainwater catchment", "water", ""),
                guide("Sand filter", "", "water filtration")
            ),
            false
        ).get(0);

        assertEquals("water", item.getBucketKey());
        assertEquals("Water & sanitation", item.getLabel());
        assertEquals("2 guides", item.getCountLabel());
        assertEquals((int) 0xFF7A9AB4L, item.getAccentColor());
        assertTrue(item.getEnabled());
        assertNormalizedContains(item.getContentDescription(), item.getLabel());
        assertNormalizedContains(item.getContentDescription(), item.getCountLabel());
    }

    @Test
    public void categoryShelfOmitsEmptyBuckets() {
        List<CategoryShelfItemModel> items = MainActivity.buildHomeChromeCategoryShelfItems(
            Arrays.asList(
                guide("Clinic triage", "medicine", ""),
                guide("First aid", "health", "")
            ),
            false
        );

        assertEquals(1, items.size());
        assertEquals("medicine", items.get(0).getBucketKey());
        assertEquals("2 guides", items.get(0).getCountLabel());
    }

    @Test
    public void compactCategoryShelfOnlyCondensesWhenTopSixAreDominant() {
        List<SearchResult> guides = Arrays.asList(
            guide("Water one", "water", ""),
            guide("Water two", "water", ""),
            guide("Shelter one", "shelter", ""),
            guide("Shelter two", "shelter", ""),
            guide("Fire one", "fire", ""),
            guide("Fire two", "fire", ""),
            guide("Medicine one", "medicine", ""),
            guide("Medicine two", "medicine", ""),
            guide("Food one", "food", ""),
            guide("Food two", "food", ""),
            guide("Tools one", "tools", ""),
            guide("Tools two", "tools", ""),
            guide("Radio one", "communications", ""),
            guide("Radio two", "communications", ""),
            guide("Community one", "community", "")
        );

        List<CategoryShelfItemModel> uncondensed = MainActivity.buildHomeChromeCategoryShelfItems(guides, true);
        assertEquals(8, uncondensed.size());

        List<CategoryShelfItemModel> dominant = MainActivity.buildHomeChromeCategoryShelfItems(
            Arrays.asList(
                guide("Water one", "water", ""),
                guide("Water two", "water", ""),
                guide("Shelter one", "shelter", ""),
                guide("Shelter two", "shelter", ""),
                guide("Fire one", "fire", ""),
                guide("Fire two", "fire", ""),
                guide("Medicine one", "medicine", ""),
                guide("Medicine two", "medicine", ""),
                guide("Food one", "food", ""),
                guide("Food two", "food", ""),
                guide("Tools one", "tools", ""),
                guide("Tools two", "tools", ""),
                guide("Radio one", "communications", ""),
                guide("Community one", "community", "")
            ),
            true
        );

        assertEquals(6, dominant.size());
    }

    @Test
    public void categoryShelfModeFollowsPhoneFormFactor() {
        assertEquals(
            CategoryShelfLayoutMode.PHONE_GRID,
            MainActivity.resolveCategoryShelfLayoutMode(true, false, false)
        );
        assertEquals(
            CategoryShelfLayoutMode.TABLET_GRID,
            MainActivity.resolveCategoryShelfLayoutMode(false, true, false)
        );
        assertEquals(
            CategoryShelfLayoutMode.TABLET_GRID,
            MainActivity.resolveCategoryShelfLayoutMode(false, false, true)
        );
    }

    @Test
    public void manualRecentThreadLabelUsesCompactGuideTimeAndConfidenceMetadata() {
        long fourHoursTwentyOneMinutesAgo = System.currentTimeMillis() - ((4L * 60L + 21L) * 60_000L);
        ChatSessionStore.ConversationPreview unsurePreview = preview(
            "can I make a rain shelter with cord",
            "GD-345",
            "",
            ReviewedCardMetadata.empty(),
            fourHoursTwentyOneMinutesAgo
        );
        ChatSessionStore.ConversationPreview confidentPreview = preview(
            "how do I keep tinder dry",
            "GD-027",
            "deterministic-fire",
            ReviewedCardMetadata.empty(),
            fourHoursTwentyOneMinutesAgo
        );
        ChatSessionStore.ConversationPreview yesterdayPreview = preview(
            "how do I brace a wall",
            "GD-094",
            "",
            new ReviewedCardMetadata("card-1", "GD-094", "reviewed", "", "reviewed_card_runtime", Collections.emptyList()),
            System.currentTimeMillis() - (25L * 60L * 60L * 1000L)
        );

        assertTrue(MainActivity.buildManualHomeRecentThreadLabelForTest(unsurePreview).endsWith("GD-345 \u2022 04:21 \u2022 UNSURE"));
        assertTrue(MainActivity.buildManualHomeRecentThreadLabelForTest(confidentPreview).endsWith("GD-027 \u2022 04:21 \u2022 CONFIDENT"));
        assertTrue(MainActivity.buildManualHomeRecentThreadLabelForTest(yesterdayPreview).endsWith("GD-094 \u2022 YESTERDAY \u2022 CONFIDENT"));
    }

    @Test
    public void categoryShelfInteractionsRequireRepositoryAndIdleState() {
        assertTrue(MainActivity.areCategoryInteractionsEnabled(true, false));
        assertFalse(MainActivity.areCategoryInteractionsEnabled(false, false));
        assertFalse(MainActivity.areCategoryInteractionsEnabled(true, true));
    }

    @Test
    public void recentThreadPreviewLimitKeepsThreeRowsAvailableOnPhones() {
        assertEquals(3, MainActivity.resolveRecentThreadPreviewLimit(true));
        assertEquals(3, MainActivity.resolveRecentThreadPreviewLimit(false));
    }

    @Test
    public void manualHomeRecentRowsUseCompactPhoneDensityForThreeRows() {
        assertEquals(70, MainActivity.resolveManualHomeRecentThreadMinimumHeightDp(false, false, true));
        assertEquals(70, MainActivity.resolveManualHomeRecentThreadMinimumHeightDp(false, true, true));
        assertEquals(70, MainActivity.resolveManualHomeRecentThreadMinimumHeightDp(true, false, true));
        assertEquals(8, MainActivity.resolveManualHomeRecentThreadGapDp(false, true, true));
        assertEquals(8, MainActivity.resolveManualHomeRecentThreadVerticalPaddingDp(false, true, true));
        assertEquals(10, MainActivity.resolveManualHomeRecentThreadGapDp(true, true, false));
        assertEquals(9, MainActivity.resolveManualHomeRecentThreadVerticalPaddingDp(true, true, false));
    }

    @Test
    public void manualHomeRecentThreadStackFitsThreeRowsInsideCompactBudget() {
        int rowHeight = MainActivity.resolveManualHomeRecentThreadMinimumHeightDp(false, false, true);
        int rowGap = MainActivity.resolveManualHomeRecentThreadGapDp(false, true, true);

        assertTrue((rowHeight * 3) + (rowGap * 2) <= 226);
    }

    @Test
    public void searchResultLimitMatchesCompactStatePackSurface() {
        assertEquals(4, MainActivity.SEARCH_RESULT_LIMIT);
    }

    @Test
    public void searchDisplayLimitKeepsTargetFourExceptPhoneLandscapeResultCap() {
        assertEquals(4, MainActivity.resolveSearchResultDisplayLimitForTest(false, false, true));
        assertEquals(4, MainActivity.resolveSearchResultDisplayLimitForTest(false, true, false));
        assertEquals(3, MainActivity.resolveSearchResultDisplayLimitForTest(true, false, true));
        assertEquals(Integer.MAX_VALUE, MainActivity.resolveSearchResultDisplayLimitForTest(true, true, true));
    }

    @Test
    public void manualHomeStatusKeepsGuideCountOutsideReviewMode() {
        assertEquals(
            "Ready offline | 754 guides",
            MainActivity.compactManualHomeStatusForTest("Ready offline | 754 guides", true)
        );
        assertEquals(
            "Ready offline | 754 guides",
            MainActivity.compactManualHomeStatusForTest("Ready offline | 754 guides", false)
        );
    }

    @Test
    public void reviewManualHomeStatusDropsDuplicateGuideCountForNarrowChrome() {
        assertEquals(
            "PACK READY",
            MainActivity.compactManualHomeStatusForTest("Ready offline | 754 guides", true, true)
        );
    }

    @Test
    public void homeSubtitleUsesActualCountOutsideReviewMode() {
        assertEquals(
            "271 guides in your offline field manual",
            MainActivity.buildHomeSubtitleTextForTest(271, false)
        );
        assertEquals(
            "1 guide in your offline field manual",
            MainActivity.buildHomeSubtitleTextForTest(1, false)
        );
        assertEquals(
            "754 guides \u2022 12 categories \u2022 ready offline \u2022 ed. 2",
            MainActivity.buildHomeSubtitleTextForTest(271, true)
        );
    }

    @Test
    public void manualHomeCategoryLabelsStayCompactForPhoneGrid() {
        assertEquals("Shelter", MainActivity.manualHomeCategoryLabel("shelter"));
        assertEquals("Water", MainActivity.manualHomeCategoryLabel("water"));
        assertEquals("Fire", MainActivity.manualHomeCategoryLabel("fire"));
        assertEquals("Food", MainActivity.manualHomeCategoryLabel("food"));
        assertEquals("Medicine", MainActivity.manualHomeCategoryLabel("medicine"));
        assertEquals("Tools", MainActivity.manualHomeCategoryLabel("tools"));
        assertEquals("Communications", MainActivity.manualHomeCategoryLabel("communications"));
    }

    @Test
    public void categoryFilterHeaderUsesCompactCountCopy() {
        assertEquals("Shelter (12)", MainActivity.buildCategoryFilterLabelForTest("Shelter", 12));
        assertEquals("Guides (0)", MainActivity.buildCategoryFilterLabelForTest("", -2));
    }

    @Test
    public void tabletManualCategoryFilterLabelsCarryCounts() {
        assertEquals("Shelter (12)", MainActivity.manualHomeCategoryFilterLabelForTest("shelter", 12));
        assertEquals("Water (4)", MainActivity.manualHomeCategoryFilterLabelForTest("water", 4));
    }

    @Test
    public void manualHomeCategoryShelfReservesTwoRowsWithoutClipping() {
        assertEquals(0, MainActivity.resolveManualHomeCategoryShelfMinimumHeightDp(0));
        assertEquals(74, MainActivity.resolveManualHomeCategoryShelfMinimumHeightDp(3));
        assertEquals(157, MainActivity.resolveManualHomeCategoryShelfMinimumHeightDp(6));
        assertEquals(146, MainActivity.resolveTabletManualHomeCategoryShelfMinimumHeightDp(6));
    }

    @Test
    public void tabletHomeColumnBalanceMatchesMockParityContract() {
        assertEquals(1.42f, MainActivity.resolveTabletHomePrimaryWeight(true), 0.001f);
        assertEquals(1.02f, MainActivity.resolveTabletHomeRecentWeight(true), 0.001f);
        assertEquals(1.02f, MainActivity.resolveTabletHomePrimaryWeight(false), 0.001f);
        assertEquals(0.90f, MainActivity.resolveTabletHomeRecentWeight(false), 0.001f);
        assertEquals(12, MainActivity.resolveTabletHomeTopPaddingDp(true));
        assertEquals(24, MainActivity.resolveTabletHomeTopPaddingDp(false));
    }

    @Test
    public void homeChromeBackReturnsSearchToBrowseButKeepsBrowseInPlace() {
        assertEquals(
            MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE,
            MainActivity.resolveHomeChromeBackEffectForTest(false)
        );
        assertEquals(
            MainRouteDecisionHelper.Effect.NONE,
            MainActivity.resolveHomeChromeBackEffectForTest(true)
        );
    }

    @Test
    public void homeChromeBackReturnsAskResultsToBrowse() {
        MainRouteDecisionHelper.Transition transition = MainRouteDecisionHelper.homeChromeBack(
            MainActivity.routeStateForModeForTest(false, BottomTabDestination.ASK, true)
        );

        assertEquals(MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE, transition.effect);
        assertEquals(MainRouteDecisionHelper.Surface.BROWSE, transition.routeState.surface);
        assertEquals(BottomTabDestination.HOME, transition.routeState.activePhoneTab);
        assertFalse(transition.routeState.askLaneActive);
    }

    @Test
    public void tabletLandscapeSearchRailWidthsMatchMockParityContract() {
        assertEquals(331, MainActivity.resolveTabletSearchFilterRailWidthDp(true));
        assertEquals(441, MainActivity.resolveTabletSearchPreviewRailWidthDp(true));
        assertEquals(0, MainActivity.resolveTabletSearchFilterRailWidthDp(false));
        assertEquals(0, MainActivity.resolveTabletSearchPreviewRailWidthDp(false));
    }

    @Test
    public void phoneSearchHeaderKeepsCompactCountAndReviewLatency() {
        assertEquals(
            "Search rain shelter    4 results \u2022 12MS",
            ReviewDemoPolicy.appendSearchLatency(
                MainActivity.buildPhoneSearchHeaderForTest("rain shelter", 4),
                "rain shelter",
                true
            )
        );
        assertEquals("Search - 1 result", MainActivity.buildPhoneSearchHeaderForTest("", 1));
    }

    @Test
    public void searchChromeSplitsTargetQueryAndCountLatency() {
        assertEquals("rain shelter", MainActivity.buildSearchChromeQueryLabelForTest(" rain shelter "));
        assertEquals(
            "4 RESULTS \u2022 12MS",
            MainActivity.buildSearchChromeCountLabelForTest("rain shelter", 4, true)
        );
        assertEquals("guides", MainActivity.buildSearchChromeQueryLabelForTest(""));
        assertEquals("1 RESULT", MainActivity.buildSearchChromeCountLabelForTest("water", 1, true));
    }

    @Test
    public void landscapePhoneSearchChromeKeepsTopContextSeparateFromCountRow() {
        assertEquals(
            "\u2039  |  SEARCH rain shelter",
            MainActivity.buildLandscapePhoneSearchChromeLabelForTest(" rain shelter ")
        );
        assertEquals(
            "\u2039  |  SEARCH guides",
            MainActivity.buildLandscapePhoneSearchChromeLabelForTest("")
        );
    }

    @Test
    public void tabletSearchPreviewUsesDataDrivenCopyForReviewResultByDefault() {
        SearchResult result = ReviewDemoPolicy.shapeSearchResults(
            "rain shelter",
            true,
            Arrays.asList(guideWithId("Survival Basics & First 72 Hours", "GD-023")),
            null
        ).get(0);

        assertEquals(
            "starter  \u00b7  immediate  \u00b7  survival",
            MainActivity.buildTabletPreviewMetaForTest(result)
        );
        assertEquals(
            "Shelter Building: Protection from the Elements. Day signaling vs. night signaling...",
            MainActivity.buildTabletPreviewBodyForTest(result)
        );
    }

    @Test
    public void tabletSearchPreviewUsesTargetCopyForFirstReviewResultOnlyWhenRequested() {
        SearchResult result = ReviewDemoPolicy.shapeSearchResults(
            "rain shelter",
            true,
            Arrays.asList(guideWithId("Survival Basics & First 72 Hours", "GD-023")),
            null
        ).get(0);

        assertEquals("Starter  \u00b7  17 sections", MainActivity.buildTabletPreviewMetaForTest(result, true));
        assertTrue(MainActivity.buildTabletPreviewBodyForTest(result, true).startsWith("Day signaling vs. night signaling."));
        assertTrue(MainActivity.buildTabletPreviewBodyForTest(result, true).contains("signal flares."));
    }

    @Test
    public void tabletSearchPreviewDoesNotUseTargetCopyForNormalGuide() {
        SearchResult result = guideWithId("Survival Basics & First 72 Hours", "GD-023");

        assertEquals("Source guide", MainActivity.buildTabletPreviewMetaForTest(result));
        assertEquals("Survival Basics & First 72 Hours snippet", MainActivity.buildTabletPreviewBodyForTest(result));
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

    private static void assertNormalizedContains(String actual, String expectedToken) {
        assertTrue(
            "Expected <" + actual + "> to contain <" + expectedToken + ">",
            actual.toLowerCase().contains(expectedToken.toLowerCase())
        );
    }
}

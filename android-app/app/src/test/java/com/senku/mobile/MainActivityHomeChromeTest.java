package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.home.CategoryShelfItemModel;
import com.senku.ui.home.CategoryShelfLayoutMode;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class MainActivityHomeChromeTest {
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
        assertEquals("Water & sanitation, 2 guides. Tap to filter.", item.getContentDescription());
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
    public void searchResultLimitMatchesCompactStatePackSurface() {
        assertEquals(4, MainActivity.SEARCH_RESULT_LIMIT);
    }

    @Test
    public void reviewHomeCategoryCountsMatchTargetMockContract() {
        assertEquals(84, MainActivity.reviewHomeCategoryCountForTest("shelter"));
        assertEquals(67, MainActivity.reviewHomeCategoryCountForTest("water"));
        assertEquals(52, MainActivity.reviewHomeCategoryCountForTest("fire"));
        assertEquals(91, MainActivity.reviewHomeCategoryCountForTest("food"));
        assertEquals(73, MainActivity.reviewHomeCategoryCountForTest("medicine"));
        assertEquals(119, MainActivity.reviewHomeCategoryCountForTest("tools"));
        assertEquals(0, MainActivity.reviewHomeCategoryCountForTest("communications"));
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
    public void manualHomeCategoryShelfReservesTwoRowsWithoutClipping() {
        assertEquals(0, MainActivity.resolveManualHomeCategoryShelfMinimumHeightDp(0));
        assertEquals(54, MainActivity.resolveManualHomeCategoryShelfMinimumHeightDp(3));
        assertEquals(114, MainActivity.resolveManualHomeCategoryShelfMinimumHeightDp(6));
    }

    @Test
    public void reviewSearchLatencyOnlyAppliesToTargetMockQuery() {
        assertEquals(
            "SEARCH  rain shelter - 4 results  \u00b7  12MS",
            MainActivity.appendReviewSearchLatency("SEARCH  rain shelter - 4 results", "rain shelter", true)
        );
        assertEquals(
            "SEARCH  rain shelter - 4 results",
            MainActivity.appendReviewSearchLatency("SEARCH  rain shelter - 4 results", "rain shelter", false)
        );
        assertEquals(
            "SEARCH  water - 4 results",
            MainActivity.appendReviewSearchLatency("SEARCH  water - 4 results", "water", true)
        );
    }

    @Test
    public void phoneSearchHeaderKeepsCompactCountAndReviewLatency() {
        assertEquals(
            "rain shelter    4 RESULTS  \u00b7  12MS",
            MainActivity.appendReviewSearchLatency(
                MainActivity.buildPhoneSearchHeaderForTest("rain shelter", 4),
                "rain shelter",
                true
            )
        );
        assertEquals("SEARCH - 1 RESULT", MainActivity.buildPhoneSearchHeaderForTest("", 1));
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
}

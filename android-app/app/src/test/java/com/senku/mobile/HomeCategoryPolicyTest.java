package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.home.CategoryShelfItemModel;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class HomeCategoryPolicyTest {
    @Test
    public void countsSortsAndBuildsShelfModelsWithoutAndroidViews() {
        List<CategoryShelfItemModel> items = HomeCategoryPolicy.buildShelfItemsFromGuides(
            Arrays.asList(
                guide("Rain barrel", "", "rainwater storage"),
                guide("Sand filter", "water", ""),
                guide("Cabin roof", "building", ""),
                guide("Signal mast", "", "radio antenna")
            ),
            false
        );

        assertEquals("water", items.get(0).getBucketKey());
        assertEquals("2 guides", items.get(0).getCountLabel());
        assertEquals("shelter", items.get(1).getBucketKey());
        assertEquals("communications", items.get(2).getBucketKey());
    }

    @Test
    public void bucketMatchingRejectsSubstringNearMisses() {
        assertFalse(HomeCategoryPolicy.matchesBucket(guide("Wire repair", "", ""), "fire"));
        assertFalse(HomeCategoryPolicy.matchesBucket(guide("Fireproof storage", "", ""), "fire"));
        assertFalse(HomeCategoryPolicy.matchesBucket(guide("Medication inventory", "", ""), "medicine"));
        assertFalse(HomeCategoryPolicy.matchesBucket(guide("Herbal medicinal notes", "", ""), "medicine"));
        assertFalse(HomeCategoryPolicy.matchesBucket(guide("Broadcasting spool", "", ""), "food"));
        assertFalse(HomeCategoryPolicy.matchesBucket(guide("Craftsman schedule", "", ""), "tools"));
    }

    @Test
    public void bucketMatchingKeepsWordAndPhraseMatches() {
        assertTrue(HomeCategoryPolicy.matchesBucket(guide("Fire starter", "", ""), "fire"));
        assertTrue(HomeCategoryPolicy.matchesBucket(guide("Wire-safe lamp", "", ""), "fire"));
        assertTrue(HomeCategoryPolicy.matchesBucket(guide("Rain barrel", "", ""), "water"));
        assertTrue(HomeCategoryPolicy.matchesBucket(guide("Goat pen", "", "animal husbandry"), "food"));
        assertTrue(HomeCategoryPolicy.matchesBucket(guide("Field clinic", "", ""), "medicine"));
        assertTrue(HomeCategoryPolicy.matchesBucket(guide("First aid kit", "", ""), "medicine"));
    }

    @Test
    public void categoryCountsUseBoundaryMatchingWhenFiltering() {
        List<SearchResult> guides = Arrays.asList(
            guide("Wire repair", "", ""),
            guide("Fire starter", "", ""),
            guide("Medication inventory", "", ""),
            guide("First aid kit", "", ""),
            guide("Broadcasting spool", "", ""),
            guide("Seed saving", "", "")
        );

        assertEquals(1, HomeCategoryPolicy.countForBucket(guides, "fire"));
        assertEquals(1, HomeCategoryPolicy.countForBucket(guides, "medicine"));
        assertEquals(1, HomeCategoryPolicy.countForBucket(guides, "food"));
    }

    @Test
    public void manualHomeModelKeepsContractOrderAndDropsEmptyBuckets() {
        List<HomeCategoryPolicy.HomeCategoryModel> models = Arrays.asList(
            new HomeCategoryPolicy.HomeCategoryModel("water", 0, 2),
            new HomeCategoryPolicy.HomeCategoryModel("shelter", 1, 1),
            new HomeCategoryPolicy.HomeCategoryModel("fire", 2, 0),
            new HomeCategoryPolicy.HomeCategoryModel("tools", 5, 3)
        );

        List<HomeCategoryPolicy.HomeCategoryModel> manualModels =
            HomeCategoryPolicy.manualHomeCategoryModels(models);

        assertEquals(3, manualModels.size());
        assertEquals("shelter", manualModels.get(0).bucketKey);
        assertEquals("water", manualModels.get(1).bucketKey);
        assertEquals("tools", manualModels.get(2).bucketKey);
    }

    @Test
    public void interactionPolicyRequiresRepositoryAndIdleState() {
        assertTrue(HomeCategoryPolicy.interactionsEnabled(true, false));
        assertFalse(HomeCategoryPolicy.interactionsEnabled(false, false));
        assertFalse(HomeCategoryPolicy.interactionsEnabled(true, true));
    }

    @Test
    public void categoryDeckCondensesOnlyForCompactOrLandscapePhones() {
        assertTrue(HomeCategoryPolicy.shouldCondenseDeck(true, false));
        assertTrue(HomeCategoryPolicy.shouldCondenseDeck(false, true));
        assertTrue(HomeCategoryPolicy.shouldCondenseDeck(true, true));
        assertFalse(HomeCategoryPolicy.shouldCondenseDeck(false, false));
    }

    @Test
    public void shelfItemStylesStayPinnedToCategoryPolicyTokens() {
        List<CategoryShelfItemModel> items = HomeCategoryPolicy.buildShelfItems(Arrays.asList(
            new HomeCategoryPolicy.HomeCategoryModel("water", 0, 1),
            new HomeCategoryPolicy.HomeCategoryModel("shelter", 1, 1),
            new HomeCategoryPolicy.HomeCategoryModel("fire", 2, 1),
            new HomeCategoryPolicy.HomeCategoryModel("medicine", 3, 1),
            new HomeCategoryPolicy.HomeCategoryModel("food", 4, 1),
            new HomeCategoryPolicy.HomeCategoryModel("tools", 5, 1),
            new HomeCategoryPolicy.HomeCategoryModel("communications", 6, 1),
            new HomeCategoryPolicy.HomeCategoryModel("community", 7, 1)
        ));

        assertShelfStyle(items.get(0), "water", "Water & sanitation", 0xFF7A9AB4L);
        assertShelfStyle(items.get(1), "shelter", "Shelter & build", 0xFF7A9A5AL);
        assertShelfStyle(items.get(2), "fire", "Fire & energy", 0xFFC48A5AL);
        assertShelfStyle(items.get(3), "medicine", "Medicine", 0xFFB67A7AL);
        assertShelfStyle(items.get(4), "food", "Food & agriculture", 0xFF9AA064L);
        assertShelfStyle(items.get(5), "tools", "Tools & craft", 0xFFC9B682L);
        assertShelfStyle(items.get(6), "communications", "Communications", 0xFF7A9A9AL);
        assertShelfStyle(items.get(7), "community", "Community", 0xFF9AA084L);
    }

    @Test
    public void unknownShelfStyleUsesHumanizedLabelAndDefaultAccent() {
        List<CategoryShelfItemModel> items = HomeCategoryPolicy.buildShelfItems(Arrays.asList(
            new HomeCategoryPolicy.HomeCategoryModel("resource-management", 8, 2)
        ));

        assertShelfStyle(items.get(0), "resource-management", "Resource Management", 0xFFC9B682L);
        assertEquals(0xFFC9B682, HomeCategoryPolicy.accentForBucket("unknown"));
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

    private static void assertShelfStyle(
        CategoryShelfItemModel item,
        String bucketKey,
        String label,
        long accentColor
    ) {
        assertEquals(bucketKey, item.getBucketKey());
        assertEquals(label, item.getLabel());
        assertEquals((int) accentColor, item.getAccentColor());
    }
}

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

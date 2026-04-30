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

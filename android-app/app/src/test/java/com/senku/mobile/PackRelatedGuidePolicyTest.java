package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class PackRelatedGuidePolicyTest {
    @Test
    public void fireStartingRelatedGuidesPreferWorkflowProximityOverAlphabeticalOrder() {
        SearchResult anchor = relatedGuide(
            "GD-343",
            "Fire by Friction Methods",
            "survival",
            "fire-starting friction-fire bow-drill tinder kindling"
        );

        List<SearchResult> ordered = PackRelatedGuidePolicy.orderByWorkflowRelevance(
            anchor,
            List.of(
                relatedGuide("GD-010", "Agriculture & Gardening", "agriculture", "crops gardening"),
                relatedGuide("GD-011", "Animal Husbandry & Veterinary", "agriculture", "livestock breeding"),
                relatedGuide("GD-250", "Daily Cooking Fire Management", "survival", "fire management fuel coals"),
                relatedGuide("GD-023", "Survival Basics & First 72 Hours", "survival", "survival basics fire water shelter"),
                relatedGuide("GD-190", "Combustion & Fire Chemistry Basics", "chemistry", "combustion flame fuel")
            )
        );

        assertEquals("GD-023", ordered.get(0).guideId);
        assertEquals("GD-250", ordered.get(1).guideId);
        assertEquals("GD-190", ordered.get(2).guideId);
        assertEquals("GD-010", ordered.get(3).guideId);
        assertEquals("GD-011", ordered.get(4).guideId);
    }

    @Test
    public void emergencyShelterRelatedGuidesPreferUseAdjacentFireAndWaterproofing() {
        SearchResult anchor = relatedGuide(
            "GD-345",
            "Emergency Shelters",
            "survival",
            "emergency shelter rain shelter tarp shelter debris hut"
        );

        List<SearchResult> ordered = PackRelatedGuidePolicy.orderByWorkflowRelevance(
            anchor,
            List.of(
                relatedGuide("GD-064", "Agriculture & Gardening", "agriculture", "crops gardening"),
                relatedGuide("GD-067", "Animal Husbandry & Veterinary", "agriculture", "livestock veterinary"),
                relatedGuide(
                    "GD-702",
                    "Waterproofing Field Materials",
                    "building",
                    "waterproofing material protection dry storage keep tinder dry"
                ),
                relatedGuide(
                    "GD-701",
                    "Fire Layouts for Wet Weather",
                    "survival",
                    "fire layout platform fire windbreak tinder bundle"
                ),
                relatedGuide(
                    "GD-023",
                    "Survival Basics & First 72 Hours",
                    "survival",
                    "survival basics first 72 hours water purification quick reference"
                )
            )
        );

        assertEquals("GD-701", ordered.get(0).guideId);
        assertEquals("GD-702", ordered.get(1).guideId);
        assertEquals("GD-023", ordered.get(2).guideId);
        assertEquals("GD-064", ordered.get(3).guideId);
        assertEquals("GD-067", ordered.get(4).guideId);
    }

    @Test
    public void nonSurvivalRelatedGuidesKeepRepositoryOrderAndSkipNulls() {
        SearchResult anchor = relatedGuide(
            "GD-700",
            "Soap Making",
            "crafts",
            "soap lye oils washing"
        );

        List<SearchResult> ordered = PackRelatedGuidePolicy.orderByWorkflowRelevance(
            anchor,
            Arrays.asList(
                relatedGuide("GD-250", "Daily Cooking Fire Management", "survival", "fire management"),
                null,
                relatedGuide("GD-010", "Agriculture & Gardening", "agriculture", "gardening"),
                relatedGuide("GD-190", "Combustion & Fire Chemistry Basics", "chemistry", "combustion")
            )
        );

        assertEquals("GD-250", ordered.get(0).guideId);
        assertEquals("GD-010", ordered.get(1).guideId);
        assertEquals("GD-190", ordered.get(2).guideId);
    }

    @Test
    public void candidatePoolLetsLimitOneOutrankAlphabeticalFirstPage() {
        assertEquals(12, PackRelatedGuidePolicy.candidateLimit(1));
        assertEquals(12, PackRelatedGuidePolicy.candidateLimit(3));
        assertEquals(32, PackRelatedGuidePolicy.candidateLimit(20));
    }

    private static SearchResult relatedGuide(String guideId, String title, String category, String topicTags) {
        return new SearchResult(
            title,
            "",
            topicTags,
            "",
            guideId,
            "",
            category,
            "related",
            "",
            "",
            "",
            topicTags
        );
    }
}

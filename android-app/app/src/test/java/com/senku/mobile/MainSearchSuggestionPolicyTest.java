package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class MainSearchSuggestionPolicyTest {
    @Test
    public void shortOrMissingInputsReturnNoSuggestions() {
        assertTrue(MainSearchSuggestionPolicy.buildSuggestions("w", sampleGuides()).isEmpty());
        assertTrue(MainSearchSuggestionPolicy.buildSuggestions("water", null).isEmpty());
        assertTrue(MainSearchSuggestionPolicy.buildSuggestions("water", Collections.emptyList()).isEmpty());
    }

    @Test
    public void guideSuggestionsUseExistingLabelSearchQueryAndDescriptionShape() {
        List<MainSearchSuggestionPolicy.SearchSuggestion> suggestions =
            MainSearchSuggestionPolicy.buildSuggestions("rain", Arrays.asList(
                guide("GD-001", "Rain barrel basics", "water", "Water systems", "rainwater"),
                guide("GD-002", "Unrelated shelter", "shelter", "Shelter", "roof")
            ));

        assertEquals(2, suggestions.size());
        assertTrue(suggestions.get(0).isCategory());
        MainSearchSuggestionPolicy.SearchSuggestion suggestion = suggestions.get(1);
        assertFalse(suggestion.isCategory());
        assertEquals("GD-001 | Rain barrel basics", suggestion.label);
        assertEquals("Suggested guide: Rain barrel basics. Tap to browse matching guides.", suggestion.contentDescription);
        assertEquals("GD-001", suggestion.searchQuery);
    }

    @Test
    public void guideSuggestionScoreRanksExactGuideIdThenTitleThenSectionThenMetadata() {
        assertEquals(0, MainSearchSuggestionPolicy.suggestionScore(
            guide("water", "Reference index", "community", "Records", "manual"),
            "water"
        ));
        assertEquals(1, MainSearchSuggestionPolicy.suggestionScore(
            guide("GD-001", "Water collection basics", "shelter", "Roof", "rain"),
            "water"
        ));
        assertEquals(2, MainSearchSuggestionPolicy.suggestionScore(
            guide("GD-002", "Boil water safely", "medicine", "Heat", "first aid"),
            "water"
        ));
        assertEquals(3, MainSearchSuggestionPolicy.suggestionScore(
            guide("GD-003", "Storage", "tools", "Water safety", "barrels"),
            "water"
        ));
        assertEquals(4, MainSearchSuggestionPolicy.suggestionScore(
            guide("GD-004", "Sanitation field notes", "water", "Planning", "hygiene"),
            "water"
        ));
    }

    @Test
    public void duplicateGuideIdsOnlyProduceOneGuideSuggestion() {
        List<MainSearchSuggestionPolicy.SearchSuggestion> suggestions =
            MainSearchSuggestionPolicy.buildSuggestions("gd-100", Arrays.asList(
                guide("GD-100", "Water collection basics", "water", "Rain", "filter"),
                guide("GD-100", "Water collection duplicate", "water", "Rain", "filter")
            ));

        assertEquals(1, suggestions.size());
        assertEquals("GD-100", suggestions.get(0).searchQuery);
    }

    @Test
    public void categorySuggestionsAppearBeforeGuidesForStrongCategoryMatches() {
        List<MainSearchSuggestionPolicy.SearchSuggestion> suggestions =
            MainSearchSuggestionPolicy.buildSuggestions("guide", Arrays.asList(
                guide("GD-001", "Guide water alpha", "water", "Water", "filter"),
                guide("GD-002", "Guide water beta", "water", "Water", "rain"),
                guide("GD-003", "Guide water gamma", "water", "Water", "sanitation"),
                guide("GD-004", "Guide shelter alpha", "shelter", "Shelter", "roof"),
                guide("GD-005", "Guide shelter beta", "shelter", "Shelter", "cabin"),
                guide("GD-006", "Guide shelter gamma", "shelter", "Shelter", "weather"),
                guide("GD-007", "Guide fire alpha", "fire", "Fire", "fuel"),
                guide("GD-008", "Guide fire beta", "fire", "Fire", "heat"),
                guide("GD-009", "Guide fire gamma", "fire", "Fire", "cook")
            ));

        assertEquals(MainSearchSuggestionPolicy.MAX_SUGGESTIONS, suggestions.size());
        assertTrue(suggestions.get(0).isCategory());
        assertEquals("Fire & energy (3)", suggestions.get(0).label);
        assertEquals("fire", suggestions.get(0).categoryKey);
        assertTrue(suggestions.get(1).isCategory());
        assertEquals("Shelter & build (3)", suggestions.get(1).label);
    }

    @Test
    public void categorySuggestionAllowsSmallCountsOnlyWhenQueryHintsAtBucket() {
        List<MainSearchSuggestionPolicy.SearchSuggestion> waterHints =
            MainSearchSuggestionPolicy.buildSuggestions("water", Arrays.asList(
                guide("GD-001", "Water alpha", "water", "Water", "filter"),
                guide("GD-002", "Water beta", "water", "Water", "rain")
            ));
        List<MainSearchSuggestionPolicy.SearchSuggestion> noBucketHint =
            MainSearchSuggestionPolicy.buildSuggestions("pump", Arrays.asList(
                guide("GD-003", "Pump alpha", "water", "Water", "filter"),
                guide("GD-004", "Pump beta", "water", "Water", "rain")
            ));

        assertTrue(waterHints.get(0).isCategory());
        assertEquals("Water & sanitation (2)", waterHints.get(0).label);
        assertFalse(noBucketHint.get(0).isCategory());
    }

    private static List<SearchResult> sampleGuides() {
        return Collections.singletonList(guide("GD-001", "Water collection basics", "water", "Water", "rain"));
    }

    private static SearchResult guide(
        String guideId,
        String title,
        String category,
        String sectionHeading,
        String topicTags
    ) {
        return new SearchResult(
            title,
            category,
            "",
            "",
            guideId,
            sectionHeading,
            category,
            "text",
            "",
            "",
            "",
            topicTags
        );
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public final class DetailTabletSourceOwnershipTest {
    @Test
    public void seededRainShelterThreadDoesNotImplicitlyClaimAbrasivesOwner() {
        SearchResult abrasives = source(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );
        SearchResult foundry = source(
            "GD-132",
            "Foundry & Metal Casting",
            "metal casting crucible"
        );

        assertEquals(
            "",
            DetailActivity.resolveTabletVisualOwnerGuideIdForTest(
                true,
                false,
                List.of(
                    "How do I build a simple rain shelter from tarp and cord?",
                    "What should I do next after the ridge line is up?"
                ),
                abrasives,
                List.of(abrasives, foundry)
            )
        );
    }

    @Test
    public void implicitRainShelterOwnerUsesMatchingThreadSourceWhenPresent() {
        SearchResult abrasives = source(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );
        SearchResult shelter = source(
            "GD-345",
            "Rain shelter in wet weather",
            "field shelter rain tarp cord"
        );

        assertEquals(
            "GD-345",
            DetailActivity.resolveTabletVisualOwnerGuideIdForTest(
                true,
                false,
                List.of("How do I build a simple rain shelter from tarp and cord?"),
                abrasives,
                List.of(abrasives, shelter)
            )
        );
    }

    @Test
    public void explicitTabletSourceSelectionPreservesSelectedSourceOwner() {
        SearchResult abrasives = source(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );
        SearchResult shelter = source(
            "GD-345",
            "Rain shelter in wet weather",
            "field shelter rain tarp cord"
        );

        assertEquals(
            "GD-220",
            DetailActivity.resolveTabletVisualOwnerGuideIdForTest(
                true,
                true,
                List.of("How do I build a simple rain shelter from tarp and cord?"),
                abrasives,
                List.of(abrasives, shelter)
            )
        );
    }

    @Test
    public void primaryGuideUsesGenericTopicSourceMetadataInsteadOfRainShelterGuideId() {
        assertEquals(
            "GD-777",
            DetailActivity.primaryGuideIdForSources(List.of(
                new SearchResult(
                    "Reviewed Anchor",
                    "",
                    "Use this as the reviewed anchor context.",
                    "",
                    "GD-220",
                    "",
                    "materials",
                    "hybrid",
                    "reviewed_source_anchor",
                    "immediate",
                    "materials_processing",
                    "abrasives,manufacturing"
                ),
                new SearchResult(
                    "Storm tarp rigging",
                    "",
                    "A ridgeline tarp keeps rain off the sleeping area.",
                    "",
                    "GD-777",
                    "",
                    "shelter",
                    "guide-focus",
                    "topic",
                    "immediate",
                    "emergency_shelter",
                    "rain,shelter,tarp,cord"
                )
            ))
        );
    }

    private static SearchResult source(String guideId, String title, String topicTags) {
        return new SearchResult(title, "", "", "", guideId, "", "", "", "", "", "", topicTags);
    }
}

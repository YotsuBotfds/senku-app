package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public final class DetailRecommendationFormatterTest {
    @Test
    public void buildMaterialsChecklistUsesExplicitListAndCleansGuideCitations() {
        DetailRecommendationFormatter formatter = new DetailRecommendationFormatter();

        assertEquals(
            List.of("Clean water", "Cloth", "Bandage", "Resin"),
            formatter.buildMaterialsChecklist(
                "Materials: clean water [GD-214], cloth; bandage and resin.\nSteps:\n1. Work slowly."
            )
        );
    }

    @Test
    public void buildMaterialsChecklistRequiresEnoughKeywordSignals() {
        DetailRecommendationFormatter formatter = new DetailRecommendationFormatter();

        assertEquals(
            List.of("Bark", "Split wood", "Dry twigs", "Resin"),
            formatter.buildMaterialsChecklist("Use bark, split wood, dry twigs, resin, and inner bark.")
        );
        assertEquals(List.of(), formatter.buildMaterialsChecklist("Carry cloth if you have it."));
    }

    @Test
    public void buildRelatedPathsSelectsRiskSpecificBranches() {
        DetailRecommendationFormatter formatter = new DetailRecommendationFormatter();

        assertEquals(
            List.of("Store the clean water safely", "Find the fastest safe boil setup", "Check for dehydration signs"),
            formatter.buildRelatedPaths(new DetailRecommendationFormatter.State(
                "Boiling water",
                "",
                "Boil water before storage.",
                "",
                "water",
                ""
            ))
        );
        assertEquals(
            List.of("Stop bleeding with basic supplies", "Prevent infection in the field", "Decide whether to evacuate"),
            formatter.buildRelatedPaths(new DetailRecommendationFormatter.State(
                "Snake bite",
                "",
                "Watch for injury and sepsis.",
                "",
                "medicine",
                ""
            ))
        );
    }
}

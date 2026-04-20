package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

public final class QueryMetadataProfileTest {
    private static boolean invokeContainsAny(String normalized, String... markers) throws Exception {
        Method containsAny = QueryMetadataProfile.class.getDeclaredMethod("containsAny", String.class, Set.class);
        containsAny.setAccessible(true);
        return (Boolean) containsAny.invoke(null, normalized, new LinkedHashSet<>(Arrays.asList(markers)));
    }

    @Test
    public void houseBuildPrefersBuildingMetadataOverAgriculture() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int buildingScore = profile.metadataBonus(
            "building",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );
        int agricultureScore = profile.metadataBonus(
            "agriculture",
            "subsystem",
            "long_term",
            "general",
            ""
        );

        assertTrue(buildingScore > agricultureScore);
    }

    @Test
    public void waterStoragePrefersStorageOverPurification() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int storageScore = profile.metadataBonus(
            "resource-management",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
        int purificationScore = profile.metadataBonus(
            "survival",
            "starter",
            "mixed",
            "water_purification",
            "water_purification,disinfection"
        );

        assertTrue(storageScore > purificationScore);
        assertTrue(profile.prefersDiversifiedContext());
    }

    @Test
    public void houseBuildPrefersSiteSelectionOverLateStageShelterDetails() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int siteSelectionScore = profile.metadataBonus(
            "survival",
            "safety",
            "long_term",
            "cabin_house",
            "site_selection,foundation"
        );
        int lateStageScore = profile.metadataBonus(
            "building",
            "subsystem",
            "mixed",
            "general",
            "roofing,weatherproofing"
        );

        assertTrue(siteSelectionScore > lateStageScore);
    }

    @Test
    public void waterStoragePrefersContainerSanitationOverGenericStorage() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int sanitationScore = profile.metadataBonus(
            "resource-management",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation"
        );
        int genericStorageScore = profile.metadataBonus(
            "resource-management",
            "reference",
            "mixed",
            "general",
            "water_storage"
        );

        assertTrue(sanitationScore > genericStorageScore);
    }

    @Test
    public void waterStoragePrefersContainerGuidanceOverDistributionPlanning() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int containerScore = profile.metadataBonus(
            "survival",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
        int distributionScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        assertTrue(containerScore > distributionScore);
    }

    @Test
    public void waterStoragePrefersSpecializedPlanningOverGenericStarterInventory() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int specializedScore = profile.metadataBonus(
            "resource-management",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation,disinfection"
        );
        int starterInventoryScore = profile.metadataBonus(
            "resource-management",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );

        assertTrue(specializedScore > starterInventoryScore);
    }

    @Test
    public void waterDistributionQueryPrefersDistributionPlanningWhenExplicitlyRequested() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i build a rainwater cistern and gravity fed water distribution system"
        );

        int distributionScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );
        int containerScore = profile.metadataBonus(
            "survival",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        assertTrue(distributionScore > containerScore);
    }

    @Test
    public void waterDistributionQueryStronglyPenalizesGenericContainerStorageRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int distributionScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );
        int storageScore = profile.metadataBonus(
            "resource-management",
            "reference",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        assertTrue(distributionScore > storageScore);
    }

    @Test
    public void explicitWaterDistributionSystemQueryUsesWaterDistributionStructure() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int distributionScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "water_distribution",
            "water_storage,water_distribution"
        );
        int genericScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "general",
            "water_distribution"
        );

        assertTrue("water_distribution".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("water_distribution"));
        assertTrue(distributionScore > genericScore);
    }

    @Test
    public void explicitWaterDistributionSystemQueryPenalizesChemicalStorageSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int distributionBonus = profile.sectionHeadingBonus("Gravity-Fed Water Distribution Design");
        int chemicalStorageBonus = profile.sectionHeadingBonus("Chemical Storage: Hazard Management");

        assertTrue(distributionBonus > 0);
        assertTrue(chemicalStorageBonus < 0);
        assertTrue(distributionBonus > chemicalStorageBonus);
    }

    @Test
    public void explicitWaterDistributionSystemQueryDoesNotSeedContainerRotationTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        assertTrue(profile.preferredTopicTags().contains("water_distribution"));
        assertTrue(profile.preferredTopicTags().contains("water_storage"));
        assertTrue(!profile.preferredTopicTags().contains("container_sanitation"));
        assertTrue(!profile.preferredTopicTags().contains("water_rotation"));
    }

    @Test
    public void treatedWaterStorageTankQueryStaysInWaterStorageLane() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term in a storage tank"
        );

        assertTrue("water_storage".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("water_storage"));
        assertFalse(profile.hasExplicitTopic("water_distribution"));
    }

    @Test
    public void waterStorageContainerMakingIntentOnlyTriggersForBuildStyleQueries() {
        QueryMetadataProfile storageProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile buildProfile = QueryMetadataProfile.fromQuery(
            "how do i build a water storage container"
        );

        assertFalse(storageProfile.waterStorageContainerMakingIntent());
        assertTrue(buildProfile.waterStorageContainerMakingIntent());
    }

    @Test
    public void explicitWaterDistributionSystemQueryPrefersDistributionSectionsOverContainerRotation() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int distributionBonus = profile.sectionHeadingBonus(
            "Gravity-Fed Distribution: Storage Tanks, Piping, and Overflow"
        );
        int containerBonus = profile.sectionHeadingBonus(
            "Container Sanitation and Rotation"
        );

        assertTrue(distributionBonus > containerBonus);
        assertTrue(distributionBonus > 0);
    }

    @Test
    public void explicitWaterDistributionSystemQueryPrefersWaterPointDesignOverCommonMistakes() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int waterPointBonus = profile.sectionHeadingBonus("Community Water Point Design");
        int mistakesBonus = profile.sectionHeadingBonus("Common Mistakes in Water System Design");

        assertTrue(waterPointBonus > mistakesBonus);
    }

    @Test
    public void explicitWaterDistributionSystemQueryPrefersLayoutOverTroubleshooting() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int layoutBonus = profile.sectionHeadingBonus("Distribution Network Layout");
        int troubleshootingBonus = profile.sectionHeadingBonus("Troubleshooting and Emergency Repairs");

        assertTrue(layoutBonus > troubleshootingBonus);
        assertTrue(layoutBonus > 0);
        assertTrue(troubleshootingBonus < 0);
    }

    @Test
    public void explicitWaterDistributionSystemQueryDeprioritizesLifecycleMetaSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int layoutBonus = profile.sectionHeadingBonus("Distribution Network Layout");
        int seeAlsoBonus = profile.sectionHeadingBonus("See Also");
        int checklistBonus = profile.sectionHeadingBonus("Final Checklist: Water System Lifecycle Complete");
        int maintenanceBonus = profile.sectionHeadingBonus("Preventive Maintenance and System Care");

        assertTrue(layoutBonus > seeAlsoBonus);
        assertTrue(layoutBonus > checklistBonus);
        assertTrue(layoutBonus > maintenanceBonus);
        assertTrue(seeAlsoBonus < 0);
        assertTrue(checklistBonus < 0);
        assertTrue(maintenanceBonus < 0);
    }

    @Test
    public void explicitWaterDistributionSystemQueryDeprioritizesPhaseLifecycleSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );

        int storageTankBonus = profile.sectionHeadingBonus("Storage Tank Planning");
        int phaseBonus = profile.sectionHeadingBonus("Phase 4: Distribution System Design and Installation");

        assertTrue(storageTankBonus > phaseBonus);
    }

    @Test
    public void waterStorageContinuationPrefersRotationOverHistoricalContext() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "water storage container sanitation rotation"
        );

        int rotationBonus = profile.sectionHeadingBonus("Rotation Schedules: FIFO Implementation & Discipline");
        int historicalBonus = profile.sectionHeadingBonus("Historical Context: The Science of Storage");

        assertTrue(rotationBonus > historicalBonus);
    }

    @Test
    public void waterStorageContinuationPrefersHydrationOverChemicalStorage() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "water storage container sanitation rotation"
        );

        int hydrationBonus = profile.sectionHeadingBonus("Water Storage: Hydration Assurance");
        int chemicalStorageBonus = profile.sectionHeadingBonus("Chemical Storage: Hazard Management");

        assertTrue(hydrationBonus > 0);
        assertTrue(chemicalStorageBonus < -10);
        assertTrue(hydrationBonus > chemicalStorageBonus);
    }

    @Test
    public void treatedWaterStorageLongTermPrefersContainerAndRotationSectionsOverHydration() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int hydrationBonus = profile.sectionHeadingBonus("Water Storage: Hydration Assurance");
        int containerBonus = profile.sectionHeadingBonus("Food-Safe Containers and Sanitation");
        int rotationBonus = profile.sectionHeadingBonus("Rotation Schedules: FIFO Implementation & Discipline");
        int inspectionBonus = profile.sectionHeadingBonus("Container Inspection and Seal Integrity");

        assertTrue(containerBonus > hydrationBonus);
        assertTrue(rotationBonus > hydrationBonus);
        assertTrue(inspectionBonus > hydrationBonus);
    }

    @Test
    public void waterStorageContinuationPrefersHydrationOverFoodStorage() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "water storage container sanitation rotation"
        );

        int hydrationBonus = profile.sectionHeadingBonus("Water Storage: Hydration Assurance");
        int foodStorageBonus = profile.sectionHeadingBonus("Food Storage Fundamentals");

        assertTrue(foodStorageBonus < 0);
        assertTrue(hydrationBonus > foodStorageBonus);
    }

    @Test
    public void waterStorageContinuationPrefersHydrationOverPestPrevention() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "water storage container sanitation rotation"
        );

        int hydrationBonus = profile.sectionHeadingBonus("Water Storage: Hydration Assurance");
        int pestBonus = profile.sectionHeadingBonus("Pest Prevention & Control in Storage");

        assertTrue(hydrationBonus > pestBonus);
        assertTrue(pestBonus < 0);
    }

    @Test
    public void waterStorageContinuationPrefersHydrationOverAmmunitionStorage() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int hydrationBonus = profile.sectionHeadingBonus("Water Storage: Hydration Assurance");
        int ammoBonus = profile.sectionHeadingBonus("Ammunition Storage");

        assertTrue(hydrationBonus > ammoBonus);
        assertTrue(ammoBonus < 0);
    }

    @Test
    public void waterStorageContinuationPrefersHydrationOverRootCellarsAndCanning() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int hydrationBonus = profile.sectionHeadingBonus("Water Storage: Hydration Assurance");
        int rootCellarBonus = profile.sectionHeadingBonus("Root Cellars & Underground Storage: The Ultimate Shelter");
        int canningBonus = profile.sectionHeadingBonus("Canning & Preservation Methods: Detailed Protocols");

        assertTrue(rootCellarBonus < 0);
        assertTrue(canningBonus < 0);
        assertTrue(hydrationBonus > rootCellarBonus);
        assertTrue(hydrationBonus > canningBonus);
    }

    @Test
    public void waterStorageContinuationPenalizesGenericInventorySections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int hydrationBonus = profile.sectionHeadingBonus("Water Storage: Hydration Assurance");
        int documentsBonus = profile.sectionHeadingBonus("Critical Documents: Copies, Not Originals");
        int checklistBonus = profile.sectionHeadingBonus("Assembly Checklist: Building Your Home Inventory");

        assertTrue(hydrationBonus > documentsBonus);
        assertTrue(hydrationBonus > checklistBonus);
        assertTrue(documentsBonus < 0);
        assertTrue(checklistBonus < 0);
    }

    @Test
    public void cabinSiteSelectionPromptStillGetsDiversifiedBuildProfile() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        int focusedScore = profile.metadataBonus(
            "survival",
            "planning",
            "long_term",
            "cabin_house",
            "site_selection,foundation,drainage"
        );
        int genericScore = profile.metadataBonus(
            "building",
            "subsystem",
            "mixed",
            "general",
            "weatherproofing"
        );

        assertTrue(profile.prefersDiversifiedContext());
        assertTrue(focusedScore > genericScore);
    }

    @Test
    public void buildingSitePromptPrefersTerrainAnalysisOverFrenchDrainSection() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        );

        int terrainBonus = profile.sectionHeadingBonus("Terrain Analysis");
        int frenchDrainBonus = profile.sectionHeadingBonus("French Drain Construction");

        assertTrue(terrainBonus > frenchDrainBonus);
        assertTrue(frenchDrainBonus <= 0);
    }

    @Test
    public void buildingSitePromptDeprioritizesDrainageAndWaterproofingSection() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        );

        int terrainBonus = profile.sectionHeadingBonus("Terrain Analysis");
        int drainageWaterproofingBonus = profile.sectionHeadingBonus("Drainage and Waterproofing");

        assertTrue(terrainBonus > drainageWaterproofingBonus);
        assertTrue(drainageWaterproofingBonus <= 0);
    }

    @Test
    public void smallCabinSiteAndFoundationPromptKeepsFoundationAboveDrainage() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int frenchDrainBonus = profile.sectionHeadingBonus("French Drain Construction");

        assertTrue(foundationBonus > frenchDrainBonus);
        assertTrue(frenchDrainBonus <= 0);
    }

    @Test
    public void smallCabinSiteAndFoundationPromptPenalizesWatercraftWaterproofingRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        int focusedSiteScore = profile.metadataBonus(
            "survival",
            "safety",
            "long_term",
            "cabin_house",
            "site_selection,drainage"
        );
        int watercraftLeakScore = profile.metadataBonus(
            "building",
            "subsystem",
            "immediate",
            "small_watercraft",
            "drainage,foundation,small_watercraft,sealing"
        );

        assertTrue(focusedSiteScore > watercraftLeakScore);
    }

    @Test
    public void smallCabinSiteAndFoundationPromptDeprioritizesFoundationInsulationSection() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int insulationBonus = profile.sectionHeadingBonus("Floor & Foundation Insulation");

        assertTrue(foundationBonus > insulationBonus);
        assertTrue(insulationBonus <= 0);
    }

    @Test
    public void buildingSitePromptDoesNotSeedWholeBuildSequence() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        );

        assertTrue(profile.preferredTopicTags().contains("site_selection"));
        assertTrue(profile.preferredTopicTags().contains("drainage"));
        assertTrue(!profile.preferredTopicTags().contains("wall_construction"));
        assertTrue(!profile.preferredTopicTags().contains("roofing"));
        assertTrue(!profile.preferredTopicTags().contains("weatherproofing"));
        assertTrue(!profile.preferredTopicTags().contains("ventilation"));
    }

    @Test
    public void smallCabinSiteAndFoundationPromptSeedsFoundationButNotRoofing() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        assertTrue(profile.preferredTopicTags().contains("site_selection"));
        assertTrue(profile.preferredTopicTags().contains("foundation"));
        assertTrue(profile.preferredTopicTags().contains("drainage"));
        assertTrue(!profile.preferredTopicTags().contains("roofing"));
        assertTrue(!profile.preferredTopicTags().contains("weatherproofing"));
        assertTrue(!profile.preferredTopicTags().contains("ventilation"));
    }

    @Test
    public void insulationPromptStillGetsHouseMetadataProfile() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "winter is 6 weeks away and we have no insulation"
        );

        int focusedScore = profile.metadataBonus(
            "building",
            "starter",
            "long_term",
            "cabin_house",
            "weatherproofing,ventilation"
        );
        int genericScore = profile.metadataBonus(
            "society",
            "reference",
            "mixed",
            "general",
            ""
        );

        assertTrue(profile.prefersDiversifiedContext());
        assertTrue(focusedScore > genericScore);
    }

    @Test
    public void houseBuildPrefersCabinSiteSelectionOverEarthShelterFallback() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int siteSelectionScore = profile.metadataBonus(
            "survival",
            "safety",
            "long_term",
            "cabin_house",
            "site_selection,drainage"
        );
        int bunkerScore = profile.metadataBonus(
            "building",
            "subsystem",
            "long_term",
            "earth_shelter",
            "site_selection,drainage,ventilation"
        );

        assertTrue(siteSelectionScore > bunkerScore);
    }

    @Test
    public void genericHouseQueryDeprioritizesThermalEfficiencyAgainstFoundationSequence() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("cabin house foundation drainage");

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int thermalEfficiencyBonus = profile.sectionHeadingBonus("Core Principles of Thermal Efficiency");

        assertTrue(foundationBonus > thermalEfficiencyBonus);
    }

    @Test
    public void genericHouseQueryDeprioritizesEngineeringCalculationSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int materialsBonus = profile.sectionHeadingBonus("Alternative Building Materials");
        int footingCalculationBonus = profile.sectionHeadingBonus("Footing Sizing Calculations");

        assertTrue(foundationBonus > footingCalculationBonus);
        assertTrue(materialsBonus > footingCalculationBonus);
    }

    @Test
    public void genericHouseQueryDeprioritizesStructuralOverviewAgainstFoundationSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int structuralOverviewBonus = profile.sectionHeadingBonus("Structural Engineering Basics for Off-Grid Builders");
        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int frostLineBonus = profile.sectionHeadingBonus("Frost Line and Frost Heave");

        assertTrue(foundationBonus > structuralOverviewBonus);
        assertTrue(frostLineBonus > structuralOverviewBonus);
        assertTrue(structuralOverviewBonus < 0);
    }

    @Test
    public void genericHouseQueryDeprioritizesClimateSpecificSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int desertBonus = profile.sectionHeadingBonus("Desert Shelter Construction");
        int avalancheBonus = profile.sectionHeadingBonus("Terrain Assessment and Avalanche Hazard Recognition");

        assertTrue(foundationBonus > desertBonus);
        assertTrue(foundationBonus > avalancheBonus);
        assertTrue(desertBonus < 0);
        assertTrue(avalancheBonus < 0);
    }

    @Test
    public void genericHouseQueryDeprioritizesAccessibilitySpecificSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int lowVisionBonus = profile.sectionHeadingBonus("Adapting Existing Survival Skills for Low Vision");
        int grabBarBonus = profile.sectionHeadingBonus("Grab Bars and Support Features");

        assertTrue(foundationBonus > lowVisionBonus);
        assertTrue(foundationBonus > grabBarBonus);
        assertTrue(lowVisionBonus < 0);
        assertTrue(grabBarBonus < 0);
    }

    @Test
    public void genericHouseQueryDeprioritizesSharedShelterSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int sharedShelterBonus = profile.sectionHeadingBonus("Group Dynamics in Shared Shelters");
        int campEvolutionBonus = profile.sectionHeadingBonus("Seasonal Shelter Adaptation & Long-Term Camp Evolution");

        assertTrue(foundationBonus > sharedShelterBonus);
        assertTrue(foundationBonus > campEvolutionBonus);
        assertTrue(sharedShelterBonus < 0);
        assertTrue(campEvolutionBonus < 0);
    }

    @Test
    public void roofFollowUpPrefersRoofingOverSiteSelection() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("what about sealing the roof");

        int roofingScore = profile.metadataBonus(
            "building",
            "subsystem",
            "long_term",
            "cabin_house",
            "roofing,weatherproofing"
        );
        int siteSelectionScore = profile.metadataBonus(
            "survival",
            "planning",
            "long_term",
            "cabin_house",
            "site_selection,drainage"
        );

        assertTrue(roofingScore > siteSelectionScore);
    }

    @Test
    public void roofFollowUpDoesNotSeedWholeHouseProjectTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("what about sealing the roof");

        assertTrue(profile.preferredTopicTags().contains("roofing"));
        assertTrue(profile.preferredTopicTags().contains("weatherproofing"));
        assertFalse(profile.preferredTopicTags().contains("site_selection"));
        assertFalse(profile.preferredTopicTags().contains("drainage"));
        assertFalse(profile.preferredTopicTags().contains("foundation"));
        assertFalse(profile.preferredTopicTags().contains("wall_construction"));
        assertFalse(profile.preferredTopicTags().contains("ventilation"));
    }

    @Test
    public void wallBuildPromptDoesNotSeedRoofOrSiteTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i frame a cabin wall with rough lumber"
        );

        assertTrue(profile.preferredTopicTags().contains("wall_construction"));
        assertFalse(profile.preferredTopicTags().contains("site_selection"));
        assertFalse(profile.preferredTopicTags().contains("drainage"));
        assertFalse(profile.preferredTopicTags().contains("foundation"));
        assertFalse(profile.preferredTopicTags().toString(), profile.preferredTopicTags().contains("roofing"));
        assertFalse(profile.preferredTopicTags().contains("weatherproofing"));
        assertFalse(profile.preferredTopicTags().contains("ventilation"));
    }

    @Test
    public void roofFollowUpPrunesCrossStructureSealNoise() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("what about sealing the roof");

        assertFalse(profile.hasExplicitTopic("water_rotation"));
        assertFalse(profile.hasExplicitTopic("sealing"));
        assertFalse(profile.hasExplicitTopic("message_authentication"));
    }

    @Test
    public void foundationPromptSeedsDrainageButNotRoofOrWallTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i pour a cabin foundation with limited cement"
        );

        assertTrue(profile.preferredTopicTags().contains("foundation"));
        assertTrue(profile.preferredTopicTags().contains("drainage"));
        assertFalse(profile.preferredTopicTags().contains("site_selection"));
        assertFalse(profile.preferredTopicTags().contains("wall_construction"));
        assertFalse(profile.preferredTopicTags().contains("roofing"));
        assertFalse(profile.preferredTopicTags().contains("weatherproofing"));
        assertFalse(profile.preferredTopicTags().contains("ventilation"));
    }

    @Test
    public void roofFollowUpSectionHeadingBonusPrefersRoofingOverTerrain() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("what about sealing the roof");

        int roofingBonus = profile.sectionHeadingBonus("Roofing Systems");
        int terrainBonus = profile.sectionHeadingBonus("Terrain Analysis");
        int conclusionBonus = profile.sectionHeadingBonus("Conclusion");
        int calculatorBonus = profile.sectionHeadingBonus("Concrete Mixing Ratio Calculator");

        assertTrue(roofingBonus > terrainBonus);
        assertTrue(roofingBonus > conclusionBonus);
        assertTrue(roofingBonus > calculatorBonus);
        assertTrue(calculatorBonus < 0);
    }

    @Test
    public void roofFollowUpSectionHeadingBonusPenalizesFoundationAndGenericMaterials() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("what about sealing the roof");

        int roofingBonus = profile.sectionHeadingBonus("Roofing Systems");
        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int materialsBonus = profile.sectionHeadingBonus("Alternative Building Materials");

        assertTrue(roofingBonus > foundationBonus);
        assertTrue(roofingBonus > materialsBonus);
        assertTrue(foundationBonus < 0);
        assertTrue(materialsBonus < 0);
    }

    @Test
    public void roofWeatherproofQueryDoesNotSeedSiteOrWallTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i waterproof a roof with no tar or shingles"
        );

        assertTrue(profile.preferredTopicTags().contains("roofing"));
        assertTrue(profile.preferredTopicTags().contains("weatherproofing"));
        assertFalse(profile.preferredTopicTags().contains("site_selection"));
        assertFalse(profile.preferredTopicTags().contains("drainage"));
        assertFalse(profile.preferredTopicTags().contains("foundation"));
        assertFalse(profile.preferredTopicTags().contains("wall_construction"));
    }

    @Test
    public void wallWeatherproofQueryDoesNotPromoteRoofingTopic() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i weatherproof a cabin wall with rough lumber"
        );

        assertTrue(profile.preferredTopicTags().contains("wall_construction"));
        assertTrue(profile.preferredTopicTags().contains("weatherproofing"));
        assertFalse(profile.preferredTopicTags().contains("roofing"));
        assertFalse(profile.preferredTopicTags().contains("site_selection"));
        assertFalse(profile.preferredTopicTags().contains("drainage"));
        assertFalse(profile.preferredTopicTags().contains("foundation"));
    }

    @Test
    public void genericHouseContinuationPrefersFoundationOverOutbuildings() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house what next");

        int foundationBonus = profile.sectionHeadingBonus("Foundations");
        int drainageBonus = profile.sectionHeadingBonus("French Drain Construction");
        int outbuildingBonus = profile.sectionHeadingBonus("Outbuildings for Off-Grid Living");

        assertTrue(foundationBonus > outbuildingBonus);
        assertTrue(drainageBonus > outbuildingBonus);
        assertTrue(outbuildingBonus < 0);
    }

    @Test
    public void sanitationPromptPrefersLatrineMetadataOverAgricultureNoise() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I build a latrine for 50 people that won't contaminate our water?"
        );

        int sanitationScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "sanitation_system",
            "latrine_design,wash_station"
        );
        int agricultureScore = profile.metadataBonus(
            "agriculture",
            "subsystem",
            "mixed",
            "general",
            ""
        );

        assertTrue("sanitation_system".equals(profile.preferredStructureType()));
        assertTrue(sanitationScore > agricultureScore);
    }

    @Test
    public void messageAuthenticationPromptPrefersCourierMetadataOverGenericEvacuation() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How should people verify an evacuation order before acting?"
        );

        int authScore = profile.metadataBonus(
            "society",
            "planning",
            "mixed",
            "message_auth",
            "message_authentication,chain_of_custody"
        );
        int evacuationScore = profile.metadataBonus(
            "survival",
            "starter",
            "mixed",
            "general",
            ""
        );

        assertTrue("message_auth".equals(profile.preferredStructureType()));
        assertTrue(authScore > evacuationScore);
    }

    @Test
    public void messageAuthenticationSectionsPreferAuthenticationOverLeadership() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "We run couriers between camps. What is the simplest way to prove a note is real?"
        );

        int authBonus = profile.sectionHeadingBonus("Physical Authentication Methods");
        int governanceBonus = profile.sectionHeadingBonus("Community Governance & Leadership");

        assertTrue(authBonus > governanceBonus);
    }

    @Test
    public void soapmakingPromptPrefersSoapMetadataOverUnrelatedMedicalRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        int soapScore = profile.metadataBonus(
            "crafts",
            "starter",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        int unrelatedMedicalScore = profile.metadataBonus(
            "medical",
            "safety",
            "mixed",
            "general",
            ""
        );

        assertTrue("soapmaking".equals(profile.preferredStructureType()));
        assertTrue(soapScore > unrelatedMedicalScore);
    }

    @Test
    public void soapmakingPromptPrefersTaggedSoapRowsOverGenericChemistrySafety() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        int taggedSoapScore = profile.metadataBonus(
            "crafts",
            "safety",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        int genericChemistryScore = profile.metadataBonus(
            "chemistry",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        assertTrue(taggedSoapScore > genericChemistryScore);
    }

    @Test
    public void soapmakingPromptDeprioritizesMedicalHygieneRowsAgainstDedicatedSoapGuides() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        int dedicatedSoapScore = profile.metadataBonus(
            "crafts",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        int medicalHygieneScore = profile.metadataBonus(
            "medical",
            "safety",
            "mixed",
            "wound_care",
            "soapmaking,lye_safety,wash_station"
        );

        assertTrue(dedicatedSoapScore > medicalHygieneScore);
    }

    @Test
    public void soapmakingSectionsPreferColdProcessOverGenericOverview() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        int coldProcessBonus = profile.sectionHeadingBonus("Soap Making - Cold Process");
        int overviewBonus = profile.sectionHeadingBonus("Overview: Why Chemical Safety Matters");
        int industrialBonus = profile.sectionHeadingBonus("Industrial Applications");

        assertTrue(coldProcessBonus > overviewBonus);
        assertTrue(coldProcessBonus > industrialBonus);
    }

    @Test
    public void glassmakingPromptPrefersGlassMetadataOverGeneralMaterialsRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i make glass");

        int glassScore = profile.metadataBonus(
            "crafts",
            "starter",
            "mixed",
            "glassmaking",
            "glassmaking,annealing"
        );
        int generalScore = profile.metadataBonus(
            "building",
            "reference",
            "mixed",
            "general",
            ""
        );

        assertTrue("glassmaking".equals(profile.preferredStructureType()));
        assertTrue(glassScore > generalScore);
    }

    @Test
    public void houseBuildPreferredTopicOverlapCountsTaggedHouseSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        int focusedOverlap = profile.preferredTopicOverlapCount("site_selection,foundation,drainage");
        int weakOverlap = profile.preferredTopicOverlapCount("ventilation");

        assertTrue(focusedOverlap > weakOverlap);
        assertTrue(profile.hasPreferredTopicOverlap("foundation"));
    }

    @Test
    public void genericHousePromptDoesNotEnableAccessibilityOrClimateFallbacks() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a house");

        assertTrue(!profile.accessibilityIntent());
        assertTrue(!profile.climateContextIntent());
    }

    @Test
    public void soapmakingQueryPrefersCraftsOverMedicalRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        int craftScore = profile.metadataBonus(
            "crafts",
            "safety",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        int medicalScore = profile.metadataBonus(
            "medical",
            "safety",
            "immediate",
            "general",
            "first_aid"
        );

        assertTrue(craftScore > medicalScore);
    }

    @Test
    public void messageAuthenticationQueryPrefersCommunicationsOverMedicalRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "If someone posts an evacuation order on the board, how should people verify it before acting?"
        );

        int communicationsScore = profile.metadataBonus(
            "communications",
            "safety",
            "mixed",
            "message_auth",
            "message_authentication,chain_of_custody"
        );
        int medicalScore = profile.metadataBonus(
            "medical",
            "safety",
            "mixed",
            "general",
            "first_aid"
        );

        assertTrue(communicationsScore > medicalScore);
    }

    @Test
    public void sanitationQueryPrefersLatrineRowsOverGenericCultureRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I build a latrine for 50 people that won't contaminate our water?"
        );

        int sanitationScore = profile.metadataBonus(
            "medical",
            "safety",
            "immediate",
            "sanitation_system",
            "latrine_design,wash_station"
        );
        int genericScore = profile.metadataBonus(
            "culture-knowledge",
            "reference",
            "mixed",
            "general",
            ""
        );

        assertTrue(sanitationScore > genericScore);
    }

    @Test
    public void communitySecurityQueryUsesDedicatedStructureAndTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i protect a vulnerable work site, field, or water point without spreading people too thin?"
        );

        assertTrue("community_security".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("community_security"));
        assertTrue(profile.hasExplicitTopic("resource_security"));
        assertTrue(profile.prefersDiversifiedContext());
    }

    @Test
    public void communitySecurityQueryPrefersSecurityMetadataOverHouseMetadata() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i protect a vulnerable work site, field, or water point without spreading people too thin?"
        );

        int securityScore = profile.metadataBonus(
            "defense",
            "planning",
            "long_term",
            "community_security",
            "community_security,resource_security"
        );
        int houseScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "cabin_house",
            "site_selection,foundation"
        );

        assertTrue(securityScore > houseScore);
    }

    @Test
    public void communityGovernanceQueryUsesDedicatedStructureAndTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "someone is stealing food from the group what do we do"
        );

        assertTrue("community_governance".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("community_governance"));
        assertTrue(profile.hasExplicitTopic("conflict_resolution"));
        assertTrue(profile.prefersDiversifiedContext());
    }

    @Test
    public void communityGovernanceQueryPrefersConflictResolutionOverInsuranceSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "two groups want to merge but they dont trust each other yet"
        );

        int mediationBonus = profile.sectionHeadingBonus("Monitoring, Mediation, and Graduated Sanctions");
        int insuranceBonus = profile.sectionHeadingBonus("Insurance Pooling and Accounting");

        assertTrue(mediationBonus > insuranceBonus);
        assertTrue(mediationBonus > 0);
        assertTrue(insuranceBonus < 0);
    }

    @Test
    public void communityGovernanceFormalPunishmentQueryUsesDedicatedStructureAndTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what if someone keeps stealing but nobody trusts formal punishment"
        );

        assertTrue("community_governance".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("community_governance"));
        assertTrue(profile.hasExplicitTopic("conflict_resolution"));
        assertTrue(profile.hasExplicitTopic("trust_systems"));
    }

    @Test
    public void burningTrustDoesNotTriggerLyeSafetyTopic() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do we protect food storage and water points from theft without burning trust"
        );

        assertTrue(!profile.hasExplicitTopic("lye_safety"));
    }

    @Test
    public void communitySecurityQueryDoesNotCarryGovernanceTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do we protect food storage and water points from theft without burning trust"
        );

        assertTrue("community_security".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("community_security"));
        assertTrue(profile.hasExplicitTopic("resource_security"));
        assertTrue(!profile.hasExplicitTopic("community_governance"));
        assertTrue(!profile.hasExplicitTopic("conflict_resolution"));
        assertTrue(!profile.hasExplicitTopic("trust_systems"));
    }

    @Test
    public void clayOvenQueryUsesDedicatedStructureAndTopics() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a clay oven");

        assertTrue("clay_oven".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("clay_oven"));
        assertTrue(profile.preferredTopicTags().contains("masonry_hearth"));
        assertTrue(profile.prefersDiversifiedContext());
    }

    @Test
    public void clayOvenQueryPrefersBreadOvenSectionsOverUnrelatedRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a clay oven");

        int ovenBonus = profile.sectionHeadingBonus("Cob Oven Construction");
        int unrelatedBonus = profile.sectionHeadingBonus("Insurance Pooling and Accounting");

        assertTrue(ovenBonus > unrelatedBonus);
        assertTrue(ovenBonus > 0);
        assertTrue(unrelatedBonus < 0);
    }

    @Test
    public void clayOvenQueryPenalizesLabSiteSelectionSections() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how do i build a clay oven");

        int ovenBonus = profile.sectionHeadingBonus("Cob Oven Construction");
        int labSiteBonus = profile.sectionHeadingBonus("Lab Site Selection and Setup");

        assertTrue(ovenBonus > labSiteBonus);
        assertTrue(labSiteBonus < 0);
    }

    @Test
    public void markerMatchingUsesTokenAndPhraseBoundaries() throws Exception {
        assertFalse(invokeContainsAny("swallowing", "wall"));
        assertFalse(invokeContainsAny("pollution", "lotion"));
        assertTrue(invokeContainsAny("my cabin wall is rotting", "wall"));
        assertTrue(invokeContainsAny("child swallowed drain cleaner", "drain cleaner"));
    }

    @Test
    public void poisoningQueriesUseDedicatedSafetyStructureAndNoWallTopic() {
        for (String query : new String[] {
            "my child may have poisoning after swallowing drain cleaner",
            "child swallowed bleach",
            "toddler ingested drain cleaner"
        }) {
            QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(query);
            assertTrue("safety_poisoning".equals(profile.preferredStructureType()));
            assertTrue(profile.hasExplicitTopic("lye_safety"));
            assertFalse(profile.hasExplicitTopic("wall_construction"));
        }
    }

    @Test
    public void poisoningQueryPrefersMedicalImmediateRowsOverHouseRows() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "my child may have poisoning after swallowing drain cleaner"
        );

        int toxicologyScore = profile.metadataBonus(
            "medical",
            "safety",
            "immediate",
            "general",
            ""
        );
        int chemicalSafetyScore = profile.metadataBonus(
            "chemistry",
            "safety",
            "immediate",
            "general",
            "lye_safety"
        );
        int houseScore = profile.metadataBonus(
            "building",
            "planning",
            "long_term",
            "cabin_house",
            "wall_construction"
        );

        assertTrue(toxicologyScore > houseScore);
        assertTrue(chemicalSafetyScore > houseScore);
    }

    @Test
    public void poisoningSectionsPreferTriageOverProductionChemistry() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("child swallowed bleach");

        int airwayBonus = profile.sectionHeadingBonus("Airway Risk and Escalation Triggers");
        int productionBonus = profile.sectionHeadingBonus("11. Scale-Up Considerations: From Lab to Production");

        assertTrue(airwayBonus > productionBonus);
    }

    @Test
    public void nonPoisoningChemistryQueriesDoNotUseSafetyPoisoningStructure() {
        QueryMetadataProfile chemistryProfile = QueryMetadataProfile.fromQuery("kitchen chemistry experiments");
        QueryMetadataProfile drainageProfile = QueryMetadataProfile.fromQuery("acid mine drainage cleanup");

        assertFalse("safety_poisoning".equals(chemistryProfile.preferredStructureType()));
        assertFalse(chemistryProfile.hasExplicitTopic("lye_safety"));
        assertFalse("safety_poisoning".equals(drainageProfile.preferredStructureType()));
        assertFalse(drainageProfile.hasExplicitTopic("lye_safety"));
    }

    @Test
    public void houseWallQueryStillUsesHouseStructureAndWallTopic() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("my cabin wall is rotting");

        assertTrue("cabin_house".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("wall_construction"));
        assertFalse(profile.hasExplicitTopic("lye_safety"));
    }

    @Test
    public void soapmakingQueryWithLyeStillUsesSoapmakingStructure() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("How do I make soap with lye safely?");

        assertTrue("soapmaking".equals(profile.preferredStructureType()));
        assertTrue(profile.hasExplicitTopic("soapmaking"));
        assertTrue(profile.hasExplicitTopic("lye_safety"));
        assertFalse("safety_poisoning".equals(profile.preferredStructureType()));
    }

    @Test
    public void detectStructureTypeIdentifiesRainShelterFromTarpAndCord() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "How do I build a simple rain shelter from tarp and cord?"
        );

        assertTrue("emergency_shelter".equals(profile.preferredStructureType()));
    }

    @Test
    public void detectStructureTypeIdentifiesRainFly() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("setting up a rain fly in a storm");

        assertTrue("emergency_shelter".equals(profile.preferredStructureType()));
    }

    @Test
    public void detectStructureTypeIdentifiesTarpRidgelineShelter() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how to make a tarp ridgeline shelter with cord"
        );

        assertTrue("emergency_shelter".equals(profile.preferredStructureType()));
    }

    @Test
    public void detectStructureTypeDoesNotRouteGenericTarpQuery() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("how to store a rolled tarp in the shed");

        assertFalse("emergency_shelter".equals(profile.preferredStructureType()));
    }

    @Test
    public void detectStructureTypeDoesNotRouteCampingTarpQuery() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery("best tarp brand for weekend camping");

        assertFalse("emergency_shelter".equals(profile.preferredStructureType()));
    }
}

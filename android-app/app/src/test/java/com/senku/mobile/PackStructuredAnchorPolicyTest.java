package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;

public final class PackStructuredAnchorPolicyTest {
    @Test
    public void explicitWaterStorageAnchorPrefersPlanningGuideOverInventoryGuide() {
        SearchResult inventoryGuide = result(
            "Community Inventory",
            "Water Storage & Purification",
            "resource-management",
            "route-focus",
            "starter",
            "water_storage",
            "water_storage"
        );
        SearchResult planningGuide = result(
            "Storage & Material Management",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        SearchResult selected = PackStructuredAnchorPolicy.selectExplicitWaterStorageAnchor(
            PackRepository.QueryTerms.fromQuery("what is the safest way to store treated water long term"),
            Arrays.asList(inventoryGuide, planningGuide)
        );

        assertEquals("Storage & Material Management", selected.title);
    }

    @Test
    public void specializedStructuredAnchorPrefersDedicatedSoapProcessGuide() {
        SearchResult broadChemistry = result(
            "Chemistry Fundamentals",
            "Acids and Bases",
            "chemistry",
            "route-focus",
            "safety",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        SearchResult dedicatedSoap = result(
            "Everyday Compounds and Production",
            "Making Soap",
            "crafts",
            "route-focus",
            "subsystem",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        SearchResult selected = PackStructuredAnchorPolicy.selectSpecializedStructuredAnchor(
            PackRepository.QueryTerms.fromQuery("How do I make soap from animal fat safely enough that it's actually useful?"),
            Arrays.asList(broadChemistry, dedicatedSoap)
        );

        assertEquals("Making Soap", selected.sectionHeading);
    }

    @Test
    public void broadHouseAnchorRequiresBroadCabinHouseQuery() {
        SearchResult cabinStarter = result(
            "Construction & Carpentry",
            "Foundations",
            "building",
            "route-focus",
            "starter",
            "cabin_house",
            "drainage,foundation,wall_construction,roofing,weatherproofing"
        );

        assertNull(PackStructuredAnchorPolicy.selectBroadHouseAnchor(
            PackRepository.QueryTerms.fromQuery("how do i build a roof for a cabin"),
            Arrays.asList(cabinStarter)
        ));

        SearchResult selected = PackStructuredAnchorPolicy.selectBroadHouseAnchor(
            PackRepository.QueryTerms.fromQuery("how do i build a house"),
            Arrays.asList(cabinStarter)
        );

        assertEquals("Construction & Carpentry", selected.title);
    }

    private static SearchResult result(
        String title,
        String sectionHeading,
        String category,
        String retrievalMode,
        String contentRole,
        String structureType,
        String topicTags
    ) {
        return new SearchResult(
            title,
            "",
            "Render fat, prepare lye water, mix to trace, cure soap bars, and rotate stored water safely.",
            "Render fat, prepare lye water, mix to trace, cure soap bars, and rotate stored water safely.",
            "GD-001",
            sectionHeading,
            category,
            retrievalMode,
            contentRole,
            "long_term",
            structureType,
            topicTags
        );
    }
}

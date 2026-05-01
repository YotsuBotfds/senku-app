package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class PackRepositoryTest {
    @Test
    public void diversifiedHouseContextRejectsPondConstructionSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Aquaculture & Fish Farming",
            "",
            "Pond construction and drainage for fish ponds.",
            "Pond construction and drainage for fish ponds.",
            "GD-068",
            "Pond Construction",
            "agriculture",
            "lexical",
            "subsystem",
            "mixed",
            "general",
            ""
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextRejectsThermalEfficiencySupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Insulation & Weatherproofing",
            "",
            "Air sealing and thermal efficiency for cabin envelopes.",
            "Air sealing and thermal efficiency for cabin envelopes.",
            "GD-106",
            "Core Principles of Thermal Efficiency",
            "building",
            "route-focus",
            "reference",
            "long_term",
            "cabin_house",
            "wall_construction,weatherproofing,ventilation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextRejectsEngineeringCalculationSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Foundations and Footings",
            "",
            "Footing sizing calculations and frost line depth.",
            "Footing sizing calculations and frost line depth.",
            "GD-383",
            "Footing Sizing Calculations",
            "building",
            "route-focus",
            "reference",
            "mixed",
            "general",
            "drainage,foundation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextRejectsSharedShelterSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Seasonal Shelter Adaptation & Long-Term Camp Evolution",
            "",
            "Shared shelter rules and group dynamics for long-term camps.",
            "Shared shelter rules and group dynamics for long-term camps.",
            "GD-618",
            "Group Dynamics in Shared Shelters",
            "survival",
            "route-focus",
            "planning",
            "long_term",
            "cabin_house",
            "site_selection,foundation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedWaterContextRejectsOutbreakSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        SearchResult candidate = new SearchResult(
            "Camp Outbreak & Dysentery Operations Quickstart",
            "",
            "Isolation and movement control for a camp outbreak.",
            "Isolation and movement control for a camp outbreak.",
            "GD-902",
            "Isolation and Movement Control",
            "medical",
            "route-focus",
            "safety",
            "mixed",
            "general",
            ""
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedWaterContextKeepsWaterRationingSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        SearchResult candidate = new SearchResult(
            "Rationing & Equitable Distribution",
            "",
            "Water rationing protocols for stored water.",
            "Water rationing protocols for stored water.",
            "GD-386",
            "Water Rationing Protocols",
            "survival",
            "route-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,water_rotation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(true, keep);
    }

    @Test
    public void diversifiedWaterContextRejectsGuideFocusDistributionOverviewSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        SearchResult candidate = new SearchResult(
            "Water System Design and Distribution",
            "",
            "Designing gravity-fed distribution networks and household water systems.",
            "Designing gravity-fed distribution networks and household water systems.",
            "GD-553",
            "",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedWaterContextRejectsMixedTagWaterTowerSupportForBroadStorage() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        SearchResult candidate = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Storage tank height, pressure, and household taps for a gravity-fed network.",
            "Storage tank height, pressure, and household taps for a gravity-fed network.",
            "GD-270",
            "Water Tower Construction & Sizing",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void explicitWaterDistributionContextKeepsGuideFocusDistributionOverviewSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Water System Design and Distribution",
            "",
            "Designing gravity-fed distribution networks and household water systems.",
            "Designing gravity-fed distribution networks and household water systems.",
            "GD-553",
            "",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(true, keep);
    }

    @Test
    public void explicitWaterDistributionContextRejectsGenericStorageSupportWithoutDistributionSignal() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Segregate chemicals and grain safely in long-term storage.",
            "Segregate chemicals and grain safely in long-term storage.",
            "GD-252",
            "Chemical Storage: Hazard Management",
            "resource-management",
            "route-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void centroidMissingFallbackKeepsRouteResultsAheadOfLexicalFallback() {
        SearchResult route = new SearchResult(
            "Homestead Chemistry",
            "",
            "Cold-process soap steps.",
            "Cold-process soap steps.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "route-focus"
        );
        SearchResult lexical = new SearchResult(
            "Chemical Safety",
            "",
            "Generic caustic handling notes.",
            "Generic caustic handling notes.",
            "GD-227",
            "Caustic Hazard Basics",
            "chemistry",
            "lexical"
        );

        java.util.List<SearchResult> merged = PackRepository.mergeResultsWhenCentroidMissingForTest(
            java.util.Collections.singletonList(route),
            java.util.Collections.singletonList(lexical),
            4
        );

        assertEquals(2, merged.size());
        assertEquals("GD-122", merged.get(0).guideId);
        assertEquals("GD-227", merged.get(1).guideId);
    }

    @Test
    public void anchorPriorPromotesDirectAnchorGuideInHybridFusion() {
        java.util.List<String> merged = PackRepository.mergeGuideIdsWithAnchorPriorForTest(
            java.util.List.of("GD-999", "GD-444"),
            java.util.List.of("GD-999", "GD-444"),
            "GD-444",
            0,
            1,
            java.util.Map.of()
        );

        assertEquals("GD-444", merged.get(0));
    }

    @Test
    public void anchorPriorCanStillBeOutbidByStrongerMultiLaneSignal() {
        java.util.List<String> merged = PackRepository.mergeGuideIdsWithAnchorPriorForTest(
            java.util.List.of("GD-999", "GD-000", "GD-111", "GD-222", "GD-333", "GD-444"),
            java.util.List.of("GD-999", "GD-999", "GD-999", "GD-999", "GD-999", "GD-999"),
            "GD-444",
            0,
            1,
            java.util.Map.of()
        );

        assertEquals("GD-999", merged.get(0));
    }

    @Test
    public void reciprocalRelatedGuideReceivesAnchorPriorWeight() {
        java.util.List<String> merged = PackRepository.mergeGuideIdsWithAnchorPriorForTest(
            java.util.List.of("GD-999", "GD-555"),
            java.util.List.of("GD-999", "GD-555"),
            "GD-444",
            0,
            1,
            java.util.Map.of("GD-555", 0.5)
        );

        assertEquals("GD-555", merged.get(0));
    }

    @Test
    public void fireStartingRelatedGuidesPreferWorkflowProximityOverAlphabeticalOrder() {
        SearchResult anchor = relatedGuide(
            "GD-343",
            "Fire by Friction Methods",
            "survival",
            "fire-starting friction-fire bow-drill tinder kindling"
        );

        List<SearchResult> ordered = PackRepository.orderRelatedGuidesByWorkflowRelevanceForTest(
            anchor,
            java.util.List.of(
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
    public void fireStartingRelatedGuidesPreferAdjacentSurvivalWorkflowWhenAllCandidatesCompete() {
        SearchResult anchor = relatedGuide(
            "GD-343",
            "Fire by Friction Methods",
            "survival",
            "fire-starting bow drill tinder kindling ember"
        );

        List<SearchResult> ordered = PackRepository.orderRelatedGuidesByWorkflowRelevanceForTest(
            anchor,
            java.util.List.of(
                relatedGuide("GD-064", "Agriculture & Gardening", "agriculture", "crops gardening pasture"),
                relatedGuide("GD-067", "Animal Husbandry & Veterinary", "agriculture", "livestock veterinary breeding"),
                relatedGuide(
                    "GD-701",
                    "Fire Layouts for Wet Weather",
                    "survival",
                    "fire lay fire layout platform fire windbreak tinder bundle"
                ),
                relatedGuide(
                    "GD-702",
                    "Waterproofing Field Materials",
                    "building",
                    "waterproofing material protection dry storage keep tinder dry"
                ),
                relatedGuide(
                    "GD-345",
                    "Primitive Shelter Construction Techniques",
                    "survival",
                    "emergency shelter rain shelter debris hut"
                ),
                relatedGuide(
                    "GD-023",
                    "Survival Basics & First 72 Hours",
                    "survival",
                    "survival basics first 72 hours fire water shelter"
                )
            )
        );

        assertEquals("GD-701", ordered.get(0).guideId);
        assertEquals("GD-702", ordered.get(1).guideId);
        assertEquals("GD-345", ordered.get(2).guideId);
        assertEquals("GD-023", ordered.get(3).guideId);
        assertEquals("GD-064", ordered.get(4).guideId);
        assertEquals("GD-067", ordered.get(5).guideId);
    }

    @Test
    public void wetFireRelatedGuidesPreferPracticalAdjacentWorkflowOverCatalogNeighbors() {
        SearchResult anchor = relatedGuide(
            "GD-027",
            "Best tinder when materials are wet",
            "survival",
            "wet fire tinder dry inner wood fire in wet conditions"
        );

        List<SearchResult> ordered = PackRepository.orderRelatedGuidesByWorkflowRelevanceForTest(
            anchor,
            java.util.List.of(
                relatedGuide(
                    "GD-488",
                    "Archaeological Knowledge & Ancient Techniques",
                    "culture-knowledge",
                    "catalog ancient techniques"
                ),
                relatedGuide("GD-064", "Agriculture & Gardening", "agriculture", "crops gardening"),
                relatedGuide("GD-067", "Animal Husbandry & Veterinary", "agriculture", "livestock veterinary"),
                relatedGuide(
                    "GD-701",
                    "Fire Layouts for Wet Weather",
                    "survival",
                    "fire lay fire layout platform fire windbreak tinder bundle"
                ),
                relatedGuide(
                    "GD-702",
                    "Waterproofing Field Materials",
                    "building",
                    "waterproofing material protection dry storage keep tinder dry"
                ),
                relatedGuide(
                    "GD-345",
                    "Primitive Shelter Construction Techniques",
                    "survival",
                    "emergency shelter rain shelter"
                )
            )
        );

        assertEquals("GD-701", ordered.get(0).guideId);
        assertEquals("GD-702", ordered.get(1).guideId);
        assertEquals("GD-345", ordered.get(2).guideId);
        assertEquals("GD-488", ordered.get(3).guideId);
        assertEquals("GD-064", ordered.get(4).guideId);
        assertEquals("GD-067", ordered.get(5).guideId);
    }

    @Test
    public void survivalRelatedGuidesPreferImmediateUseBeforeAnimalCatalogNeighbors() {
        SearchResult anchor = relatedGuide(
            "GD-023",
            "Survival Basics & First 72 Hours",
            "survival",
            "survival basics first 72 hours fire water shelter navigation rescue"
        );

        List<SearchResult> ordered = PackRepository.orderRelatedGuidesByWorkflowRelevanceForTest(
            anchor,
            java.util.List.of(
                relatedGuide("GD-120", "Animal Tracking", "survival", "tracking spoor"),
                relatedGuide("GD-121", "Butchering", "food", "meat processing"),
                relatedGuide("GD-343", "Fire by Friction Methods", "survival", "fire-starting tinder bow drill"),
                relatedGuide("GD-423", "Water Purification", "survival", "water purification boiling filtration"),
                relatedGuide("GD-386", "Water Storage & Rationing", "survival", "water storage rationing")
            )
        );

        assertEquals("GD-343", ordered.get(0).guideId);
        assertEquals("GD-423", ordered.get(1).guideId);
        assertEquals("GD-386", ordered.get(2).guideId);
        assertEquals("GD-120", ordered.get(3).guideId);
        assertEquals("GD-121", ordered.get(4).guideId);
    }

    @Test
    public void emergencyShelterRelatedGuidesPreferUseAdjacentFireAndWaterproofing() {
        SearchResult anchor = relatedGuide(
            "GD-345",
            "Emergency Shelters",
            "survival",
            "emergency shelter rain shelter tarp shelter debris hut"
        );

        List<SearchResult> ordered = PackRepository.orderRelatedGuidesByWorkflowRelevanceForTest(
            anchor,
            java.util.List.of(
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
    public void nonSurvivalRelatedGuidesKeepRepositoryOrder() {
        SearchResult anchor = relatedGuide(
            "GD-700",
            "Soap Making",
            "crafts",
            "soap lye oils washing"
        );

        List<SearchResult> ordered = PackRepository.orderRelatedGuidesByWorkflowRelevanceForTest(
            anchor,
            java.util.List.of(
                relatedGuide("GD-250", "Daily Cooking Fire Management", "survival", "fire management"),
                relatedGuide("GD-010", "Agriculture & Gardening", "agriculture", "gardening"),
                relatedGuide("GD-190", "Combustion & Fire Chemistry Basics", "chemistry", "combustion")
            )
        );

        assertEquals("GD-250", ordered.get(0).guideId);
        assertEquals("GD-010", ordered.get(1).guideId);
        assertEquals("GD-190", ordered.get(2).guideId);
    }

    @Test
    public void relatedGuideCandidatePoolLetsLimitOneOutrankAlphabeticalFirstPage() {
        assertEquals(12, PackRepository.relatedGuideCandidateLimitForTest(1));
        assertEquals(12, PackRepository.relatedGuideCandidateLimitForTest(3));
        assertEquals(32, PackRepository.relatedGuideCandidateLimitForTest(20));
    }

    @Test
    public void expiredAnchorDoesNotChangeHybridOrdering() {
        java.util.List<String> merged = PackRepository.mergeGuideIdsWithAnchorPriorForTest(
            java.util.List.of("GD-999", "GD-444"),
            java.util.List.of("GD-999", "GD-444"),
            "GD-444",
            3,
            4,
            java.util.Map.of()
        );

        assertEquals("GD-999", merged.get(0));
    }

    @Test
    public void anchorSnippetPrefersSessionChunkTextWhenAvailable() {
        String snippet = PackRepository.resolveAnchorSnippetForTest(
            "# Water Storage\nBring water to a rolling boil.\nLet it cool covered before storing it.",
            "Use food-safe containers for treated water.",
            "Fallback paragraph."
        );

        assertEquals("Bring water to a rolling boil. Let it cool covered before storing it.", snippet);
    }

    @Test
    public void anchorSnippetFallsBackToGuideChunkWhenSessionChunkMissing() {
        String snippet = PackRepository.resolveAnchorSnippetForTest(
            "",
            "## Treated Water\nUse food-safe containers for treated water.\nKeep them sealed between uses.",
            "Fallback paragraph."
        );

        assertEquals("Use food-safe containers for treated water. Keep them sealed between uses.", snippet);
    }

    @Test
    public void anchorSnippetFallsBackToFirstGuideParagraph() {
        String snippet = PackRepository.resolveAnchorSnippetForTest(
            "",
            "",
            "# Water Storage\n\nStore treated water in clean, food-safe containers and keep them sealed.\n" +
                "Rotate the supply on a schedule.\n\nSanitize replacement containers before refilling."
        );

        assertEquals(
            "Store treated water in clean, food-safe containers and keep them sealed. Rotate the supply on a schedule.",
            snippet
        );
    }

    @Test
    public void firstParagraphSnippetReturnsEmptyWhenGuideBodyHasNoEvidenceText() {
        String snippet = PackRepository.firstParagraphSnippetForTest(
            "# Water Storage\n\n---\n\n```tip"
        );

        assertEquals("", snippet);
    }

    @Test
    public void waterStorageGuideContextRejectsNegativeChemicalSection() {
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Segregate chemicals from drinking-water storage.",
            "Segregate chemicals from drinking-water storage.",
            "GD-252",
            "Chemical Storage: Hazard Management",
            "resource-management",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage"
        );

        boolean keep = PackAnswerContextSectionLoader.shouldKeepGuideSectionForContextForTest(
            "what's the safest way to store treated water long term",
            candidate,
            false
        );

        assertEquals(false, keep);
    }

    @Test
    public void waterStorageGuideContextKeepsHydrationSection() {
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Use food-safe containers and rotate stored water.",
            "Use food-safe containers and rotate stored water.",
            "GD-252",
            "Water Storage: Hydration Assurance",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        boolean keep = PackAnswerContextSectionLoader.shouldKeepGuideSectionForContextForTest(
            "what's the safest way to store treated water long term",
            candidate,
            false
        );

        assertEquals(true, keep);
    }

    @Test
    public void broadWaterRouteRejectsGenericStarterInventoryRow() {
        SearchResult candidate = new SearchResult(
            "Home Inventory",
            "",
            "Medical supplies and household inventory for two weeks.",
            "Medical supplies and household inventory for two weeks.",
            "GD-373",
            "Medical Supplies: Beyond Band-Aids",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );

        boolean keep = PackRepository.shouldKeepBroadWaterRouteRowForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void broadHouseRouteRejectsThermalEfficiencyRow() {
        SearchResult candidate = new SearchResult(
            "Insulation & Weatherproofing",
            "",
            "Air sealing and thermal efficiency for cabin envelopes.",
            "Air sealing and thermal efficiency for cabin envelopes.",
            "GD-106",
            "Core Principles of Thermal Efficiency",
            "building",
            "route-focus",
            "reference",
            "mixed",
            "cabin_house",
            "wall_construction,weatherproofing,ventilation"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "how do i build a house",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void broadHouseRouteRejectsClimateSpecificShelterRow() {
        SearchResult candidate = new SearchResult(
            "Desert Survival & Arid Environment Living",
            "",
            "Heat, shelter, and desert adaptation.",
            "Desert shelter construction and climate adaptation for arid environments.",
            "GD-353",
            "Desert Shelter Construction",
            "survival",
            "route-focus",
            "subsystem",
            "long_term",
            "general",
            "ventilation"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "how do i build a house",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void broadHouseRouteKeepsFoundationRow() {
        SearchResult candidate = new SearchResult(
            "Foundations and Footings",
            "",
            "Footing choices, frost line depth, and drainage for long-term houses.",
            "Footing choices, frost line depth, and drainage for long-term houses.",
            "GD-383",
            "Frost Line and Frost Heave",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "general",
            "drainage,foundation"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "how do i build a house",
            candidate
        );

        assertEquals(true, keep);
    }

    @Test
    public void explicitHouseRouteRejectsStructuralOverviewRowWithoutPositiveSectionAlignment() {
        SearchResult candidate = new SearchResult(
            "Construction & Carpentry",
            "",
            "General structural overview for off-grid builders.",
            "General structural overview for off-grid builders with site and foundation notes in passing.",
            "GD-094",
            "Structural Engineering Basics for Off-Grid Builders",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "how do i choose a safe site and foundation for a small cabin",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void explicitHouseRouteKeepsPositiveFoundationSection() {
        SearchResult candidate = new SearchResult(
            "Construction & Carpentry",
            "",
            "Foundation planning and layout for a small cabin site.",
            "Foundation planning and layout for a small cabin site.",
            "GD-094",
            "Foundations",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "how do i choose a safe site and foundation for a small cabin",
            candidate
        );

        assertEquals(true, keep);
    }

    @Test
    public void explicitBuildingSiteRouteRejectsDrainageEarthworksAnchor() {
        SearchResult candidate = new SearchResult(
            "Drainage and Earthworks",
            "",
            "French drain layout and trenching for runoff control.",
            "French drain layout and trenching for runoff control around structures.",
            "GD-333",
            "French Drain Construction",
            "building",
            "route-focus",
            "subsystem",
            "mixed",
            "earth_shelter",
            "site_selection,drainage"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "How do I choose a building site if drainage, wind, sun, and access all matter?",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void explicitBuildingSiteRouteAnchorPrefersSiteAssessmentGuideOverDrainageEarthworks() {
        SearchResult drainageEarthworks = new SearchResult(
            "Drainage and Earthworks",
            "",
            "French drain layout and trenching for runoff control.",
            "French drain layout and trenching for runoff control around structures.",
            "GD-333",
            "French Drain Construction",
            "building",
            "route-focus",
            "subsystem",
            "mixed",
            "earth_shelter",
            "site_selection,drainage"
        );
        SearchResult siteAssessment = new SearchResult(
            "Shelter Site Selection & Hazard Assessment",
            "",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "GD-446",
            "Terrain Analysis",
            "survival",
            "route-focus",
            "safety",
            "long_term",
            "cabin_house",
            "site_selection,drainage"
        );
        SearchResult foundations = new SearchResult(
            "Construction & Carpentry",
            "",
            "Foundation planning and layout for a small cabin site.",
            "Foundation planning and layout for a small cabin site.",
            "GD-094",
            "Foundations",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );

        SearchResult selected = PackRepository.routeFocusedAnchorForTest(
            "How do I choose a building site if drainage, wind, sun, and access all matter?",
            java.util.List.of(drainageEarthworks, siteAssessment, foundations),
            true
        );

        assertEquals("GD-446", selected.guideId);
    }

    @Test
    public void smallCabinSiteAndFoundationRouteAnchorPrefersSiteAssessmentGuideOverFoundationGuide() {
        SearchResult siteAssessment = new SearchResult(
            "Shelter Site Selection & Hazard Assessment",
            "",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "GD-446",
            "Terrain Analysis",
            "survival",
            "route-focus",
            "safety",
            "long_term",
            "cabin_house",
            "site_selection,drainage"
        );
        SearchResult foundations = new SearchResult(
            "Construction & Carpentry",
            "",
            "Foundation planning and layout for a small cabin site.",
            "Foundation planning and layout for a small cabin site.",
            "GD-094",
            "Foundations",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );
        SearchResult drainageEarthworks = new SearchResult(
            "Drainage and Earthworks",
            "",
            "French drain layout and trenching for runoff control.",
            "French drain layout and trenching for runoff control around structures.",
            "GD-333",
            "French Drain Construction",
            "building",
            "route-focus",
            "subsystem",
            "mixed",
            "earth_shelter",
            "site_selection,drainage"
        );

        SearchResult selected = PackRepository.routeFocusedAnchorForTest(
            "how do i choose a safe site and foundation for a small cabin",
            java.util.List.of(foundations, siteAssessment, drainageEarthworks),
            true
        );

        assertEquals("GD-446", selected.guideId);
    }

    @Test
    public void smallCabinSiteAndFoundationAnswerAnchorPrefersRoutedSiteGuideOverGuideFocusFoundation() {
        SearchResult rankedAnchor = new SearchResult(
            "Construction & Carpentry",
            "",
            "Foundation planning and layout for a small cabin site.",
            "Foundation planning and layout for a small cabin site.",
            "GD-094",
            "Foundations",
            "building",
            "guide-focus",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );
        SearchResult routedAnchor = new SearchResult(
            "Shelter Site Selection & Hazard Assessment",
            "",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "GD-446",
            "Terrain Analysis",
            "survival",
            "route-focus",
            "safety",
            "long_term",
            "cabin_house",
            "site_selection,drainage"
        );

        SearchResult selected = PackRepository.selectAnswerAnchorForTest(
            "how do i choose a safe site and foundation for a small cabin",
            rankedAnchor,
            routedAnchor
        );

        assertEquals("GD-446", selected.guideId);
    }

    @Test
    public void explicitFoundationAnswerAnchorStillKeepsFoundationGuide() {
        SearchResult rankedAnchor = new SearchResult(
            "Foundations and Footings",
            "",
            "Foundation planning, rubble trench options, and frost-line considerations.",
            "Foundation planning, rubble trench options, and frost-line considerations.",
            "GD-383",
            "Foundations",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "cabin_house",
            "foundation,drainage"
        );
        SearchResult routedAnchor = new SearchResult(
            "Shelter Site Selection & Hazard Assessment",
            "",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "Terrain analysis, wind exposure, sun, and practical access routes.",
            "GD-446",
            "Terrain Analysis",
            "survival",
            "route-focus",
            "safety",
            "long_term",
            "cabin_house",
            "site_selection,drainage"
        );

        SearchResult selected = PackRepository.selectAnswerAnchorForTest(
            "how do i build a cabin foundation with stone and rubble",
            rankedAnchor,
            routedAnchor
        );

        assertEquals("GD-383", selected.guideId);
    }

    @Test
    public void explicitBuildingSiteRouteRejectsDrainageAndWaterproofingFoundationReference() {
        SearchResult candidate = new SearchResult(
            "Foundations and Footings",
            "",
            "Drainage and waterproofing details for long-term foundations.",
            "Drainage and waterproofing details for long-term foundations.",
            "GD-383",
            "Drainage and Waterproofing",
            "building",
            "route-focus",
            "reference",
            "mixed",
            "general",
            "drainage,foundation"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "How do I choose a building site if drainage, wind, sun, and access all matter?",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void roofWeatherproofContextRejectsGenericStructuralOverviewSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i waterproof a roof with no tar or shingles"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i waterproof a roof with no tar or shingles"
        );
        SearchResult candidate = new SearchResult(
            "Construction & Carpentry",
            "",
            "General structural overview for off-grid builders.",
            "General structural overview for off-grid builders with generic engineering notes.",
            "GD-094",
            "Structural Engineering Basics for Off-Grid Builders",
            "building",
            "guide-focus",
            "starter",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing,weatherproofing"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void roofWeatherproofGuideContextRejectsGenericStructuralOverviewSection() {
        SearchResult candidate = new SearchResult(
            "Construction & Carpentry",
            "",
            "General structural overview for off-grid builders.",
            "General structural overview for off-grid builders with generic engineering notes.",
            "GD-094",
            "Structural Engineering Basics for Off-Grid Builders",
            "building",
            "guide-focus",
            "starter",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing,weatherproofing"
        );

        boolean keep = PackAnswerContextSectionLoader.shouldKeepGuideSectionForContextForTest(
            "how do i waterproof a roof with no tar or shingles",
            candidate,
            false
        );

        assertEquals(false, keep);
    }

    @Test
    public void roofWeatherproofAnswerAnchorPrefersRoutedRoofGuideOverGenericGuideOverview() {
        SearchResult rankedAnchor = new SearchResult(
            "Construction & Carpentry",
            "",
            "General structural overview for off-grid builders.",
            "General structural overview for off-grid builders with generic engineering notes.",
            "GD-094",
            "",
            "building",
            "guide-focus",
            "starter",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing,weatherproofing"
        );
        SearchResult routedAnchor = new SearchResult(
            "Roofing & Weatherproofing",
            "",
            "Waterproofing and sealants for roof systems without industrial materials.",
            "Waterproofing and sealants for roof systems without industrial materials.",
            "GD-515",
            "Waterproofing and Sealants",
            "building",
            "route-focus",
            "subsystem",
            "long_term",
            "cabin_house",
            "roofing,weatherproofing"
        );

        SearchResult selected = PackRepository.selectAnswerAnchorForTest(
            "how do i waterproof a roof with no tar or shingles",
            rankedAnchor,
            routedAnchor
        );

        assertEquals("GD-515", selected.guideId);
    }

    @Test
    public void smallCabinSiteAndFoundationRouteRejectsFoundationInsulationSection() {
        SearchResult candidate = new SearchResult(
            "Insulation & Weatherproofing",
            "",
            "Floor and foundation insulation for long-term heat retention.",
            "Floor and foundation insulation for long-term heat retention.",
            "GD-106",
            "Floor & Foundation Insulation",
            "building",
            "route-focus",
            "subsystem",
            "long_term",
            "cabin_house",
            "foundation,weatherproofing"
        );

        boolean keep = PackRepository.shouldKeepBroadHouseRouteRowForTest(
            "how do i choose a safe site and foundation for a small cabin",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void broadWaterRouteRejectsGenericSafetyInventoryRow() {
        SearchResult candidate = new SearchResult(
            "Home Inventory",
            "",
            "Keep emergency cash and lighting supplies ready for outages.",
            "Keep emergency cash and lighting supplies ready for outages.",
            "GD-373",
            "Light: Headlamps Over Candles",
            "resource-management",
            "route-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage"
        );

        boolean keep = PackRepository.shouldKeepBroadWaterRouteRowForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void broadWaterRouteKeepsContainerSanitationRow() {
        SearchResult candidate = new SearchResult(
            "Home Inventory",
            "",
            "Use food-safe containers and sanitize them before filling.",
            "Use food-safe containers and sanitize them before filling.",
            "GD-373",
            "Water Storage & Purification",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation"
        );

        boolean keep = PackRepository.shouldKeepBroadWaterRouteRowForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertEquals(true, keep);
    }

    @Test
    public void broadWaterRouteRejectsUnspecializedWaterStorageInventoryRow() {
        SearchResult candidate = new SearchResult(
            "Home Inventory",
            "",
            "Keep treated water stored long term with the rest of the home inventory.",
            "Keep treated water stored long term with the rest of the home inventory.",
            "GD-373",
            "Water Storage & Purification",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );

        boolean keep = PackRepository.shouldKeepBroadWaterRouteRowForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void broadWaterRouteRefinementPenalizesGenericInventorySection() {
        SearchResult candidate = new SearchResult(
            "Home Inventory",
            "",
            "Keep a broad household inventory with basic emergency supplies.",
            "Keep a broad household inventory with basic emergency supplies.",
            "GD-373",
            "Water Storage & Purification",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );

        int bonus = PackRepository.broadWaterRouteRefinementBonusForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertTrue(bonus <= -40);
    }

    @Test
    public void broadWaterRouteRefinementBoostsSpecializedPlanningGuide() {
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        int bonus = PackRepository.broadWaterRouteRefinementBonusForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertTrue(bonus >= 30);
    }

    @Test
    public void broadWaterRouteOrderingPriorityPrefersSpecializedPlanningGuide() {
        SearchResult genericInventory = new SearchResult(
            "Home Inventory",
            "",
            "Keep a broad household inventory with basic emergency supplies.",
            "Keep a broad household inventory with basic emergency supplies.",
            "GD-373",
            "Water Storage & Purification",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );
        SearchResult specializedPlanning = new SearchResult(
            "Storage & Material Management",
            "",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        int genericPriority = PackRepository.broadWaterRouteOrderingPriorityForTest(
            "what's the safest way to store treated water long term",
            genericInventory
        );
        int specializedPriority = PackRepository.broadWaterRouteOrderingPriorityForTest(
            "what's the safest way to store treated water long term",
            specializedPlanning
        );

        assertTrue(specializedPriority > genericPriority);
    }

    @Test
    public void broadWaterRouteOrderingPriorityDemotesContainerCraftGuideWithoutBuildIntent() {
        SearchResult craftGuide = new SearchResult(
            "Storage Container and Vessel Making",
            "",
            "Make storage vessels and containers from available materials.",
            "Make storage vessels and containers from available materials.",
            "GD-417",
            "",
            "building",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
        SearchResult specializedPlanning = new SearchResult(
            "Storage & Material Management",
            "",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        int craftPriority = PackRepository.broadWaterRouteOrderingPriorityForTest(
            "what's the safest way to store treated water long term",
            craftGuide
        );
        int specializedPriority = PackRepository.broadWaterRouteOrderingPriorityForTest(
            "what's the safest way to store treated water long term",
            specializedPlanning
        );

        assertTrue(specializedPriority > craftPriority);
    }

    @Test
    public void broadWaterRouteOrderingPriorityRelaxesContainerPenaltyForBuildIntent() {
        SearchResult craftGuide = new SearchResult(
            "Storage Container and Vessel Making",
            "",
            "Make storage vessels and containers from available materials.",
            "Make storage vessels and containers from available materials.",
            "GD-417",
            "",
            "building",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        int storagePriority = PackRepository.broadWaterRouteOrderingPriorityForTest(
            "what's the safest way to store treated water long term",
            craftGuide
        );
        int buildPriority = PackRepository.broadWaterRouteOrderingPriorityForTest(
            "how do i build a water storage container",
            craftGuide
        );

        assertTrue(buildPriority > storagePriority);
    }

    @Test
    public void waterStorageSpecializedRouteFilterRejectsGenericInventorySection() {
        SearchResult candidate = new SearchResult(
            "Home Inventory",
            "",
            "Stock medical supplies and inspect inventory on a rotation schedule.",
            "Stock medical supplies and inspect inventory on a rotation schedule.",
            "GD-373",
            "Medical Supplies: Beyond Band-Aids",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );

        boolean keep = PackRepository.matchesSpecializedRouteMetadataForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedWaterStorageContextRejectsContainerSanitationOnlyBuildingGuide() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what is the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what is the safest way to store treated water long term"
        );
        SearchResult candidate = new SearchResult(
            "Waterproofing and Sealants",
            "",
            "General sealants guide that also mentions food-safe lining and container sanitation.",
            "General sealants guide that also mentions food-safe lining and container sanitation.",
            "GD-457",
            "",
            "building",
            "guide-focus",
            "safety",
            "mixed",
            "earth_shelter",
            "foundation,weatherproofing,ventilation,container_sanitation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void waterStorageSpecializedRouteFilterKeepsContainerSection() {
        SearchResult candidate = new SearchResult(
            "Home Inventory",
            "",
            "Use clean food-safe containers and rotate treated water on schedule.",
            "Use clean food-safe containers and rotate treated water on schedule.",
            "GD-373",
            "Water Storage & Purification",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation"
        );

        boolean keep = PackRepository.matchesSpecializedRouteMetadataForTest(
            "what's the safest way to store treated water long term",
            candidate
        );

        assertEquals(true, keep);
    }

    @Test
    public void broadWaterStorageContextRejectsBlankContainerCraftGuideWithoutBuildIntent() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        SearchResult candidate = new SearchResult(
            "Storage Container and Vessel Making",
            "",
            "Make storage vessels and containers from available materials.",
            "Make storage vessels and containers from available materials.",
            "GD-417",
            "",
            "building",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void broadWaterStorageContextKeepsContainerCraftGuideForBuildIntent() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i build a water storage container"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i build a water storage container"
        );
        SearchResult candidate = new SearchResult(
            "Storage Container and Vessel Making",
            "",
            "Make storage vessels and containers from available materials.",
            "Make storage vessels and containers from available materials.",
            "GD-417",
            "",
            "building",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(true, keep);
    }

    @Test
    public void explicitWaterDistributionQueryRequiresDirectAnchorSignal() {
        boolean required = PackRepository.shouldRequireDirectAnchorSignalForTest(
            "storage tanks water distribution water storage"
        );

        assertEquals(true, required);
    }

    @Test
    public void messageAuthenticationQueryRequiresDirectAnchorSignal() {
        boolean required = PackRepository.shouldRequireDirectAnchorSignalForTest(
            "We run couriers between camps. What is the simplest way to prove a note is real?"
        );

        assertEquals(true, required);
    }

    @Test
    public void soapmakingQueryRequiresDirectAnchorSignal() {
        boolean required = PackRepository.shouldRequireDirectAnchorSignalForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        assertEquals(true, required);
    }

    @Test
    public void explicitWaterDistributionQueryRejectsGenericStorageAnchorSignal() {
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Water storage basics and food-safe containers.",
            "Water storage basics and food-safe containers.",
            "GD-252",
            "Water Storage: Hydration Assurance",
            "resource-management",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "storage tanks water distribution water storage",
            candidate
        );

        assertEquals(false, direct);
    }

    @Test
    public void explicitWaterDistributionQueryKeepsDistributionAnchorSignal() {
        SearchResult candidate = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning.",
            "Gravity-fed distribution and storage tank planning.",
            "GD-270",
            "",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "storage tanks water distribution water storage",
            candidate
        );

        assertEquals(true, direct);
    }

    @Test
    public void explicitWaterDistributionRouteAnchorRequiresDirectSignal() {
        SearchResult genericStorage = new SearchResult(
            "Storage & Material Management",
            "",
            "Water storage basics and food-safe containers.",
            "Water storage basics and food-safe containers.",
            "GD-252",
            "Water Storage: Hydration Assurance",
            "resource-management",
            "route-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
        SearchResult distributionGuide = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning.",
            "Gravity-fed distribution and storage tank planning.",
            "GD-270",
            "Storage Tank Planning",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );

        SearchResult selected = PackRepository.routeFocusedAnchorForTest(
            "how do i design a gravity-fed water distribution system",
            java.util.List.of(genericStorage, distributionGuide),
            true
        );

        assertEquals("GD-270", selected.guideId);
    }

    @Test
    public void messageAuthenticationRouteAnchorPrefersTaggedGuideOverGenericGovernance() {
        SearchResult genericGovernance = new SearchResult(
            "Community Governance & Leadership",
            "",
            "Leadership, trust, and posted orders in small communities.",
            "Leadership, trust, and posted orders in small communities.",
            "GD-560",
            "",
            "society",
            "route-focus",
            "subsystem",
            "immediate",
            "general",
            ""
        );
        SearchResult taggedGuide = new SearchResult(
            "Message Authentication & Courier Protocols",
            "",
            "Use tamper evidence, challenge-response, and chain-of-custody logs for real messages.",
            "Use tamper evidence, challenge-response, and chain-of-custody logs for real messages.",
            "GD-389",
            "Physical Authentication Methods",
            "communications",
            "route-focus",
            "safety",
            "mixed",
            "message_auth",
            "message_authentication,chain_of_custody"
        );

        SearchResult selected = PackRepository.routeFocusedAnchorForTest(
            "We run couriers between camps. What is the simplest way to prove a note is real?",
            java.util.List.of(genericGovernance, taggedGuide),
            true
        );

        assertEquals("GD-389", selected.guideId);
    }

    @Test
    public void soapmakingRouteAnchorPrefersSoapGuideOverGenericChemistrySafety() {
        SearchResult genericChemistry = new SearchResult(
            "Chemical & Fuel Salvage Safety",
            "",
            "Safe siphoning, storage, and identification of salvaged fuels and chemicals.",
            "Safe siphoning, storage, and identification of salvaged fuels and chemicals. Cleaning products, paints, solvents, lubricants, and fertilizers.",
            "GD-262",
            "",
            "chemistry",
            "route-focus",
            "safety",
            "immediate",
            "general",
            "soapmaking"
        );
        SearchResult taggedGuide = new SearchResult(
            "Homestead Chemistry",
            "",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        SearchResult selected = PackRepository.routeFocusedAnchorForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            java.util.List.of(genericChemistry, taggedGuide),
            true
        );

        assertEquals("GD-122", selected.guideId);
    }

    @Test
    public void soapmakingRouteAnchorPrefersTaggedSoapGuideOverBroadChemistrySoapSection() {
        SearchResult broadChemistry = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "Making soap from animal fats and wood ash lye is covered inside a broad chemistry guide.",
            "Making soap from animal fats and wood ash lye is covered inside a broad chemistry guide.",
            "GD-571",
            "Making Soap from Animal Fats and Wood Ash Lye",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );
        SearchResult taggedGuide = new SearchResult(
            "Homestead Chemistry",
            "",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        SearchResult selected = PackRepository.routeFocusedAnchorForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            java.util.List.of(broadChemistry, taggedGuide),
            true
        );

        assertEquals("GD-122", selected.guideId);
    }

    @Test
    public void soapmakingStructuredAnchorPrefersTaggedGuideEvenWhenBroadChemistryAppearsFirst() {
        SearchResult broadChemistry = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "Making soap from animal fats and wood ash lye is covered inside a broad chemistry guide.",
            "Making soap from animal fats and wood ash lye is covered inside a broad chemistry guide.",
            "GD-571",
            "Making Soap from Animal Fats and Wood Ash Lye",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );
        SearchResult taggedGuide = new SearchResult(
            "Everyday Compounds and Production",
            "",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "GD-572",
            "",
            "crafts",
            "guide-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        SearchResult selected = PackRepository.selectSpecializedStructuredAnchorForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            broadChemistry,
            taggedGuide
        );

        assertEquals("GD-572", selected.guideId);
    }

    @Test
    public void soapmakingStructuredAnchorPrefersDedicatedSoapSectionOverGenericSafetySection() {
        SearchResult broadChemistry = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "Broad chemistry fundamentals that now include a soapmaking mention.",
            "Broad chemistry fundamentals that now include a soapmaking mention.",
            "GD-571",
            "Acids and Bases",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        SearchResult taggedGuide = new SearchResult(
            "Homestead Chemistry",
            "",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        SearchResult selected = PackRepository.selectSpecializedStructuredAnchorForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            broadChemistry,
            taggedGuide
        );

        assertEquals("GD-122", selected.guideId);
    }

    @Test
    public void soapmakingStructuredAnchorRejectsBroadChemistryAcidsAndBasesForDedicatedSoapSection() {
        SearchResult broadChemistry = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "Broad chemistry fundamentals that mention soapmaking in passing.",
            "Broad chemistry fundamentals that mention soapmaking in passing.",
            "GD-571",
            "Acids and Bases",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        SearchResult dedicatedSoap = new SearchResult(
            "Everyday Compounds and Production",
            "",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "GD-572",
            "Making Soap",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        SearchResult selected = PackRepository.selectSpecializedStructuredAnchorForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            broadChemistry,
            dedicatedSoap
        );

        assertEquals("GD-572", selected.guideId);
    }

    @Test
    public void communityGovernanceStructuredAnchorPrefersCommonsGuideOverMutualAidFinanceGuideForTrustMergePrompt() {
        SearchResult financeGuide = new SearchResult(
            "Insurance, Risk Pooling & Mutual Aid Funds",
            "",
            "Historical mutual-aid examples and shared fund administration.",
            "Historical mutual-aid examples and shared fund administration.",
            "GD-865",
            "Historical Mutual Aid Models",
            "resource-management",
            "guide-focus",
            "safety",
            "long_term",
            "community_governance",
            "community_governance,trust_systems"
        );
        SearchResult commonsGuide = new SearchResult(
            "Commons Management & Sustainable Resource Governance",
            "",
            "Monitoring, sanctions, mediation, and membership rules for mixed communities.",
            "Monitoring, sanctions, mediation, and membership rules for mixed communities.",
            "GD-626",
            "Monitoring & Graduated Sanctions",
            "resource-management",
            "route-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution,trust_systems"
        );

        SearchResult selected = PackRepository.selectSpecializedStructuredAnchorForTest(
            "how do we merge with another village if we don't trust other yet",
            financeGuide,
            commonsGuide
        );

        assertEquals("GD-626", selected.guideId);
    }

    @Test
    public void communityGovernanceTrustMergeContextRejectsFinanceSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "How do we merge with another group if we don't trust each other yet?"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "How do we merge with another group if we don't trust each other yet?"
        );
        SearchResult financeGuide = new SearchResult(
            "Insurance, Risk Pooling & Mutual Aid Funds",
            "",
            "Shared fund administration and accounting for pooled aid reserves.",
            "Shared fund administration and accounting for pooled aid reserves.",
            "GD-657",
            "Fund Governance and Accounting",
            "resource-management",
            "guide-focus",
            "reference",
            "long_term",
            "community_governance",
            "community_governance,trust_systems"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, financeGuide);

        assertEquals(false, keep);
    }

    @Test
    public void communityGovernanceTrustMergeGuideContextRejectsMonitoringHeavySection() {
        SearchResult candidate = new SearchResult(
            "Commons Management & Sustainable Resource Governance",
            "",
            "Monitoring schedules, membership rules, and graduated sanctions for mixed communities.",
            "Monitoring schedules, membership rules, and graduated sanctions for mixed communities.",
            "GD-626",
            "Monitoring, Membership Rules, and Graduated Sanctions",
            "resource-management",
            "guide-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution,trust_systems"
        );

        boolean keep = PackAnswerContextSectionLoader.shouldKeepGuideSectionForContextForTest(
            "How do we merge with another group if we don't trust each other yet?",
            candidate,
            false
        );

        assertEquals(false, keep);
    }

    @Test
    public void communityGovernanceTrustMergeAnchorPrefersTrustRepairSectionOverMonitoringSection() {
        SearchResult monitoringGuide = new SearchResult(
            "Commons Management & Sustainable Resource Governance",
            "",
            "Monitoring schedules, membership rules, and graduated sanctions for mixed communities.",
            "Monitoring schedules, membership rules, and graduated sanctions for mixed communities.",
            "GD-626",
            "Monitoring, Membership Rules, and Graduated Sanctions",
            "resource-management",
            "route-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution,trust_systems"
        );
        SearchResult trustRepairGuide = new SearchResult(
            "Commons Management & Sustainable Resource Governance",
            "",
            "Trust repair, mediation, reputation, and vouching for cautious group integration.",
            "Trust repair, mediation, reputation, and vouching for cautious group integration.",
            "GD-626",
            "Trust Repair, Reputation, and Vouching",
            "resource-management",
            "route-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution,trust_systems"
        );

        SearchResult selected = PackRepository.selectSpecializedStructuredAnchorForTest(
            "How do we merge with another group if we don't trust each other yet?",
            monitoringGuide,
            trustRepairGuide
        );

        assertEquals("Trust Repair, Reputation, and Vouching", selected.sectionHeading);
    }

    @Test
    public void diversifiedSoapmakingContextRejectsGenericChemistrySafetySupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );
        SearchResult candidate = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "General chemistry fundamentals with a broad soapmaking mention.",
            "General chemistry fundamentals with a broad soapmaking mention.",
            "GD-571",
            "Practical Synthesis Procedures: Making Useful Chemicals",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void soapmakingSpecializedRouteFilterRejectsUntaggedGenericChemistryGuide() {
        SearchResult candidate = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "General chemistry fundamentals with a broad soapmaking mention.",
            "General chemistry fundamentals with a broad soapmaking mention.",
            "GD-571",
            "Practical Synthesis Procedures: Making Useful Chemicals",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        boolean keep = PackRepository.matchesSpecializedRouteMetadataForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void soapmakingSpecializedRouteFilterKeepsTaggedSoapGuide() {
        SearchResult candidate = new SearchResult(
            "Homestead Chemistry",
            "",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        boolean keep = PackRepository.matchesSpecializedRouteMetadataForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            candidate
        );

        assertEquals(true, keep);
    }

    @Test
    public void soapmakingDirectAnchorSignalRejectsGenericTopicTaggedChemistryGuide() {
        SearchResult genericChemistry = new SearchResult(
            "Chemical & Fuel Salvage Safety",
            "",
            "Safe siphoning, storage, and identification of salvaged fuels and chemicals.",
            "Safe siphoning, storage, and identification of salvaged fuels and chemicals. Cleaning products, paints, solvents, lubricants, and fertilizers.",
            "GD-262",
            "",
            "chemistry",
            "route-focus",
            "safety",
            "immediate",
            "general",
            "soapmaking"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            genericChemistry
        );

        assertEquals(false, direct);
    }

    @Test
    public void soapmakingDirectAnchorSignalRejectsBroadChemistryAcidsAndBasesSection() {
        SearchResult broadChemistry = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "Broad chemistry fundamentals with acid-base background and a soapmaking mention.",
            "Broad chemistry fundamentals with acid-base background and a soapmaking mention.",
            "GD-571",
            "Acids and Bases",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            broadChemistry
        );

        assertEquals(false, direct);
    }

    @Test
    public void soapmakingDirectAnchorSignalAcceptsDedicatedSoapProcessSection() {
        SearchResult dedicatedSoap = new SearchResult(
            "Everyday Compounds and Production",
            "",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "GD-572",
            "Making Soap",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            dedicatedSoap
        );

        assertEquals(true, direct);
    }

    @Test
    public void soapmakingDirectAnchorSignalRejectsReferenceHeavyAlkaliSection() {
        SearchResult referenceHeavy = new SearchResult(
            "Alkali & Soda Production",
            "",
            "Reference table covering purity standards where soap making tolerates crude products.",
            "Reference table covering purity standards where soap making tolerates crude products.",
            "GD-178",
            "16. Reference: Typical Compositions & Purity Standards",
            "chemistry",
            "route-focus",
            "subsystem",
            "immediate",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "how do i make soap from animal fat and ash",
            referenceHeavy
        );

        assertEquals(false, direct);
    }

    @Test
    public void soapmakingRouteResultFilterRejectsGenericCleaningProductChemistrySection() {
        SearchResult genericSafety = new SearchResult(
            "Chemical & Fuel Salvage Safety",
            "",
            "Lye is essential for soap making but this section is mostly about caustic chemical handling.",
            "Lye is essential for soap making but this section is mostly about caustic chemical handling.",
            "GD-262",
            "Cleaning Product Chemistry",
            "chemistry",
            "route-focus",
            "safety",
            "immediate",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        boolean keep = PackRepository.shouldKeepSpecializedDirectSignalRouteResultForTest(
            "how do i make soap from animal fat and ash",
            genericSafety
        );

        assertEquals(false, keep);
    }

    @Test
    public void soapmakingRouteResultFilterKeepsDedicatedSoapProcessSection() {
        SearchResult dedicatedSoap = new SearchResult(
            "Everyday Compounds and Production",
            "",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "Simple cold-process soap with lard or tallow, lye, water, and curing.",
            "GD-572",
            "Making Soap",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        boolean keep = PackRepository.shouldKeepSpecializedDirectSignalRouteResultForTest(
            "how do i make soap from animal fat and ash",
            dedicatedSoap
        );

        assertEquals(true, keep);
    }

    @Test
    public void soapmakingRouteBackfillRunsWhenFtsPoolIsStillStarved() {
        boolean backfill = PackRepository.shouldBackfillLikeAfterFtsForTest(
            "how do i make soap from animal fat and ash",
            2,
            2,
            18
        );

        assertEquals(true, backfill);
    }

    @Test
    public void soapmakingRouteBackfillStopsOnceHealthyPoolExists() {
        boolean backfill = PackRepository.shouldBackfillLikeAfterFtsForTest(
            "how do i make soap from animal fat and ash",
            3,
            6,
            18
        );

        assertEquals(false, backfill);
    }

    @Test
    public void soapmakingPrefersRouteAnchorOverBlankMismatchedGuideFocusGuide() {
        SearchResult genericGuide = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "GD-571",
            "",
            "chemistry",
            "guide-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        boolean preferRoute = PackRepository.shouldPreferRouteAnchorOverRankedGuideForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            genericGuide
        );

        assertEquals(true, preferRoute);
    }

    @Test
    public void soapmakingSpecializedRowMatchRejectsWrongStructureWithoutTopicTags() {
        boolean keep = PackRepository.matchesSpecializedExplicitTopicRowForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            "glassmaking",
            ""
        );

        assertEquals(false, keep);
    }

    @Test
    public void soapmakingSpecializedRowMatchAcceptsSoapmakingStructure() {
        boolean keep = PackRepository.matchesSpecializedExplicitTopicRowForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            "soapmaking",
            ""
        );

        assertEquals(true, keep);
    }

    @Test
    public void soapmakingSpecializedRowMatchAcceptsExplicitTopicTaggedSafetyGuide() {
        boolean keep = PackRepository.matchesSpecializedExplicitTopicRowForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            "general",
            "soapmaking,lye_safety"
        );

        assertEquals(true, keep);
    }

    @Test
    public void specializedAnchorPolicyKeepsSoapmakingStrongSignalBranchNarrow() {
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );
        SearchResult candidate = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "GD-571",
            "Practical Synthesis Procedures",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        assertEquals(
            true,
            SpecializedAnchorCandidatePolicy.isSpecializedExplicitAnchorCandidate(metadataProfile, candidate, true)
        );
        assertEquals(
            false,
            SpecializedAnchorCandidatePolicy.isSpecializedExplicitAnchorCandidate(metadataProfile, candidate, false)
        );
    }

    @Test
    public void specializedAnchorPolicyCentralizesDirectTopicOverlapFacts() {
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "storage tanks water distribution water storage"
        );
        SearchResult genericStorage = new SearchResult(
            "Storage & Material Management",
            "",
            "Water storage basics and food-safe containers.",
            "Water storage basics and food-safe containers.",
            "GD-252",
            "Water Storage: Hydration Assurance",
            "resource-management",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            ""
        );
        SearchResult distributionGuide = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning.",
            "Gravity-fed distribution and storage tank planning.",
            "GD-270",
            "Storage Tank Planning",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );

        assertEquals(false, SpecializedAnchorCandidatePolicy.hasDirectExplicitTopicOverlap(metadataProfile, genericStorage));
        assertEquals(true, SpecializedAnchorCandidatePolicy.hasDirectExplicitTopicOverlap(metadataProfile, distributionGuide));
        assertEquals(true, SpecializedAnchorCandidatePolicy.matchesRouteMetadata(
            metadataProfile,
            distributionGuide.sectionHeading,
            distributionGuide.structureType,
            distributionGuide.topicTags
        ));
    }

    @Test
    public void specializedAnchorPolicyAcceptsCommonTopicTagSeparatorsWithoutSubstringMatches() {
        assertEquals(
            true,
            SpecializedAnchorCandidatePolicy.hasTopicTag(
                "water_storage; water_distribution|container_sanitation\nwater_rotation",
                "water_distribution"
            )
        );
        assertEquals(
            true,
            SpecializedAnchorCandidatePolicy.hasTopicTag(
                "water_storage; water_distribution|container_sanitation\nwater_rotation",
                "water_rotation"
            )
        );
        assertEquals(
            false,
            SpecializedAnchorCandidatePolicy.hasTopicTag(
                "water_storage; rainwater_distribution|water_distribution_backup",
                "water_distribution"
            )
        );
    }

    @Test
    public void soapmakingAnchorCandidateRejectsWrongFamilyGuideWithoutTopicTags() {
        SearchResult candidate = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "GD-571",
            "Practical Synthesis Procedures",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        boolean keep = PackRepository.isSpecializedExplicitAnchorCandidateForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            candidate
        );

        assertEquals(false, keep);
    }

    @Test
    public void soapmakingAnchorCandidateAcceptsExplicitSoapSectionHeading() {
        SearchResult candidate = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "GD-571",
            "Making Soap from Animal Fats and Wood Ash Lye",
            "chemistry",
            "route-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        boolean keep = PackRepository.isSpecializedExplicitAnchorCandidateForTest(
            "How do I make soap from animal fat safely enough that it's actually useful?",
            candidate
        );

        assertEquals(true, keep);
    }

    @Test
    public void soapmakingSupportRejectsBlankMismatchedGuideFocusGuide() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );
        SearchResult candidate = new SearchResult(
            "Chemistry Fundamentals",
            "",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "General chemistry with a practical soapmaking section somewhere deeper in the guide.",
            "GD-571",
            "",
            "chemistry",
            "guide-focus",
            "safety",
            "mixed",
            "glassmaking",
            ""
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void explicitWaterDistributionQueryRejectsGenericBlankGuideFocusAnchorSignal() {
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Water storage basics that also mention household distribution in passing.",
            "Water storage basics that also mention household distribution in passing.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "how do i design a gravity-fed water distribution system",
            candidate
        );

        assertEquals(false, direct);
    }

    @Test
    public void explicitWaterDistributionQueryRejectsGenericPlumbingOverviewAnchorSignal() {
        SearchResult candidate = new SearchResult(
            "Plumbing & Water Systems",
            "",
            "Overview of low-tech plumbing and water movement.",
            "Overview of low-tech plumbing and water movement.",
            "GD-105",
            "",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution"
        );

        boolean direct = PackRepository.hasDirectAnchorSignalForTest(
            "how do i design a gravity-fed water distribution system",
            candidate
        );

        assertEquals(false, direct);
    }

    @Test
    public void explicitWaterDistributionContextRejectsGenericBlankGuideFocusSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Water storage basics that also mention household distribution in passing.",
            "Water storage basics that also mention household distribution in passing.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void explicitWaterDistributionContextRejectsGenericStorageRouteFocusSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Storage & Material Management",
            "",
            "Chemical storage and segregated containers.",
            "Chemical storage and segregated containers.",
            "GD-252",
            "Chemical Storage: Hazard Management",
            "resource-management",
            "route-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void explicitWaterDistributionContextKeepsStorageTankPlanningSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning.",
            "Gravity-fed distribution and storage tank planning.",
            "GD-270",
            "Storage Tank Planning",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(true, keep);
    }

    @Test
    public void explicitWaterDistributionContextRejectsTroubleshootingGuideFocusSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Water System Failure Analysis & Troubleshooting",
            "",
            "Reference guide for diagnosing broken lines and system faults.",
            "Reference guide for diagnosing broken lines and system faults.",
            "GD-596",
            "",
            "building",
            "guide-focus",
            "reference",
            "long_term",
            "water_distribution",
            "water_distribution"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void explicitWaterDistributionContextRejectsLifecycleChecklistSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Water System Lifecycle - Drilling to Troubleshooting",
            "",
            "Lifecycle overview with checklist and maintenance notes.",
            "Lifecycle overview with checklist and maintenance notes.",
            "GD-648",
            "Final Checklist: Water System Lifecycle Complete",
            "building",
            "guide-focus",
            "reference",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void explicitWaterDistributionContextRejectsSanitationGuideFocusSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        SearchResult candidate = new SearchResult(
            "Sanitation and Waste Management Systems",
            "",
            "Overview of sanitation layouts, waste flow, and hygiene systems.",
            "Overview of sanitation layouts, waste flow, and hygiene systems.",
            "GD-554",
            "",
            "building",
            "guide-focus",
            "subsystem",
            "long_term",
            "water_distribution",
            "water_distribution,latrine_design,wash_station"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void explicitWaterDistributionAnchorChooserPrefersDistributionGuideOverGenericStorageGuide() {
        SearchResult genericStorage = new SearchResult(
            "Storage & Material Management",
            "",
            "Water storage basics that also mention household distribution in passing.",
            "Water storage basics that also mention household distribution in passing.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );
        SearchResult distributionGuide = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning.",
            "Gravity-fed distribution and storage tank planning.",
            "GD-270",
            "",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        SearchResult selected = PackRepository.selectExplicitWaterDistributionAnchorForTest(
            "how do i design a gravity-fed water distribution system",
            genericStorage,
            distributionGuide
        );

        assertEquals("GD-270", selected.guideId);
    }

    @Test
    public void explicitWaterDistributionAnchorChooserPrefersPlanningGuideOverLifecyclePhaseGuide() {
        SearchResult lifecyclePhase = new SearchResult(
            "Water System Lifecycle - Drilling to Troubleshooting",
            "",
            "Phase-based overview of water system work.",
            "Phase-based overview of water system work.",
            "GD-648",
            "Phase 4: Distribution System Design and Installation",
            "building",
            "guide-focus",
            "reference",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );
        SearchResult distributionGuide = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning.",
            "Gravity-fed distribution and storage tank planning.",
            "GD-270",
            "Storage Tank Planning",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        SearchResult selected = PackRepository.selectExplicitWaterDistributionAnchorForTest(
            "how do i design a gravity-fed water distribution system",
            lifecyclePhase,
            distributionGuide
        );

        assertEquals("GD-270", selected.guideId);
    }

    @Test
    public void explicitWaterStorageAnchorChooserPrefersPlanningGuideOverGenericInventoryGuide() {
        SearchResult genericInventory = new SearchResult(
            "Home Inventory",
            "",
            "Broad inventory notes that include a water storage section.",
            "Broad inventory notes that include a water storage section.",
            "GD-373",
            "Water Storage & Purification",
            "resource-management",
            "route-focus",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );
        SearchResult planningGuide = new SearchResult(
            "Storage & Material Management",
            "",
            "Use food-safe containers, sanitize them, seal them, and rotate treated water on a schedule.",
            "Use food-safe containers, sanitize them, seal them, and rotate treated water on a schedule.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        SearchResult selected = PackRepository.selectExplicitWaterStorageAnchorForTest(
            "what is the safest way to store treated water long term",
            genericInventory,
            planningGuide
        );

        assertEquals("GD-252", selected.guideId);
    }

    @Test
    public void explicitWaterStorageAnchorChooserPrefersPlanningGuideOverContainerCraftGuideWithoutBuildIntent() {
        SearchResult craftGuide = new SearchResult(
            "Storage Container and Vessel Making",
            "",
            "Make storage vessels and containers from available materials.",
            "Make storage vessels and containers from available materials.",
            "GD-417",
            "",
            "building",
            "guide-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
        SearchResult planningGuide = new SearchResult(
            "Storage & Material Management",
            "",
            "Use food-safe containers, sanitize them, seal them, and rotate treated water on a schedule.",
            "Use food-safe containers, sanitize them, seal them, and rotate treated water on a schedule.",
            "GD-252",
            "",
            "resource-management",
            "guide-focus",
            "starter",
            "long_term",
            "water_storage",
            "weatherproofing,water_rotation"
        );

        SearchResult selected = PackRepository.selectExplicitWaterStorageAnchorForTest(
            "what is the safest way to store treated water long term",
            craftGuide,
            planningGuide
        );

        assertEquals("GD-252", selected.guideId);
    }

    @Test
    public void explicitWaterDistributionAnchorChooserPrefersPlanningGuideOverReferenceChunk() {
        SearchResult referenceChunk = new SearchResult(
            "Plumbing & Water Systems",
            "",
            "Gravity-fed systems move water downhill using source elevation.",
            "Gravity-fed systems move water downhill using source elevation.",
            "GD-105",
            "Gravity-Fed Water Systems",
            "building",
            "route-focus",
            "reference",
            "mixed",
            "water_distribution",
            "water_distribution"
        );
        SearchResult planningGuide = new SearchResult(
            "Water System Design and Distribution",
            "",
            "Design gravity-fed distribution networks with source assessment and storage planning.",
            "Design gravity-fed distribution networks with source assessment and storage planning.",
            "GD-553",
            "Gravity-Fed Distribution Systems",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_storage,water_distribution"
        );

        SearchResult selected = PackRepository.selectExplicitWaterDistributionAnchorForTest(
            "how do i design a gravity-fed water distribution system",
            referenceChunk,
            planningGuide
        );

        assertEquals("GD-553", selected.guideId);
    }

    @Test
    public void broadHouseAnchorChooserPrefersCabinStarterGuideOverGeneralFoundationReference() {
        SearchResult generalFoundationGuide = new SearchResult(
            "Foundations and Footings",
            "",
            "Reference guidance on frost line, footing sizing, and drainage for foundations.",
            "Reference guidance on frost line, footing sizing, and drainage for foundations.",
            "GD-383",
            "Frost Line and Frost Heave",
            "building",
            "guide-focus",
            "reference",
            "long_term",
            "general",
            "drainage,foundation"
        );
        SearchResult cabinStarterGuide = new SearchResult(
            "Construction & Carpentry",
            "",
            "Starter build sequence for site prep, foundations, walls, roofing, and weatherproofing.",
            "Starter build sequence for site prep, foundations, walls, roofing, and weatherproofing.",
            "GD-094",
            "Foundations",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "drainage,foundation,wall_construction,roofing,weatherproofing"
        );

        SearchResult selected = PackRepository.selectBroadHouseAnchorForTest(
            "how do i build a house",
            generalFoundationGuide,
            cabinStarterGuide
        );

        assertEquals("GD-094", selected.guideId);
    }

    @Test
    public void explicitWaterDistributionAnchorChooserPrefersDesignGuideOverLifecycleGuide() {
        SearchResult lifecycleGuide = new SearchResult(
            "Water System Lifecycle - Drilling to Troubleshooting",
            "",
            "Full water system lifecycle reference covering design, maintenance, and troubleshooting.",
            "Full water system lifecycle reference covering design, maintenance, and troubleshooting.",
            "GD-648",
            "Phase 4: Distribution System Design and Installation",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );
        SearchResult designGuide = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Design gravity-fed distribution networks with storage tank planning and household taps.",
            "Design gravity-fed distribution networks with storage tank planning and household taps.",
            "GD-270",
            "Gravity-Fed Distribution Systems",
            "building",
            "guide-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        SearchResult selected = PackRepository.selectExplicitWaterDistributionAnchorForTest(
            "how do i design a gravity-fed water distribution system",
            lifecycleGuide,
            designGuide
        );

        assertEquals("GD-270", selected.guideId);
    }

    @Test
    public void explicitWaterDistributionSeedsMoreAnchorGuideContextBeforeSupport() {
        int budget = PackAnswerContextPolicy.anchorGuideBudget(
            PackRepository.QueryTerms.fromQuery("how do i design a gravity-fed water distribution system"),
            true,
            4
        );
        boolean seedBeforeSupport = PackAnswerContextPolicy.shouldSeedAnchorBeforeSupport(
            PackRepository.QueryTerms.fromQuery("how do i design a gravity-fed water distribution system"),
            true
        );

        assertEquals(3, budget);
        assertEquals(true, seedBeforeSupport);
    }

    @Test
    public void answerContextAnchorBudgetKeepsDefaultOrderingRules() {
        assertEquals(
            3,
            PackAnswerContextPolicy.anchorGuideBudget(
                PackRepository.QueryTerms.fromQuery("how do i build a tarp shelter"),
                false,
                4
            )
        );
        assertEquals(
            2,
            PackAnswerContextPolicy.anchorGuideBudget(
                PackRepository.QueryTerms.fromQuery("how do i build a tarp shelter"),
                true,
                4
            )
        );
        assertEquals(
            false,
            PackAnswerContextPolicy.shouldSeedAnchorBeforeSupport(
                PackRepository.QueryTerms.fromQuery("how do i build a tarp shelter"),
                true
            )
        );
    }

    @Test
    public void buildGuideAnswerContextAdmitsMetadataMatchedVectorSupportRow() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        SearchResult anchor = new SearchResult(
            "Cave Shelter Systems and Long-Term Habitation",
            "",
            "Pick high ground and route runoff away from the sleeping area.",
            "Pick high ground and route runoff away from the sleeping area.",
            "GD-294",
            "Cave Selection Criteria",
            "survival",
            "lexical",
            "starter",
            "immediate",
            "emergency_shelter",
            "shelter,drainage"
        );
        SearchResult offTopicLexical = new SearchResult(
            "Camp Inventory Ledger",
            "",
            "Count crates and spare fittings before transport.",
            "Count crates and spare fittings before transport.",
            "GD-727",
            "Inventory",
            "utility",
            "lexical",
            "reference",
            "mixed",
            "general",
            "maintenance,storage"
        );
        SearchResult vectorShelter = new SearchResult(
            "Shelter Basics",
            "",
            "Tie a ridgeline, angle the tarp, and keep runoff away from the sleeping area.",
            "Tie a ridgeline, angle the tarp, and keep runoff away from the sleeping area.",
            "GD-933",
            "Simple tarp shelter setup",
            "survival",
            "vector",
            "starter",
            "immediate",
            "emergency_shelter",
            "tarp_shelter,weatherproofing"
        );

        PackRepository.SupportBreakdown vectorSupport = PackRepository.supportBreakdownForTest(query, vectorShelter);
        java.util.List<SearchResult> supportCandidates = PackAnswerContextPolicy.rankSupportCandidatesForTest(
            query,
            anchor,
            java.util.List.of(anchor, offTopicLexical, vectorShelter)
        );

        assertEquals(0, vectorSupport.lexicalSupport);
        assertTrue(vectorSupport.supportWithMetadata() > 0);
        assertTrue(containsGuideId(supportCandidates, "GD-933"));
        assertFalse(containsGuideId(supportCandidates, "GD-727"));
    }

    @Test
    public void rankedVsRoutedAnchorTiebreakUsesFullVectorSupportBothSides() {
        String query = "how do i design a gravity-fed water distribution system";
        SearchResult strongRankedVector = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "GD-270",
            "Gravity-Fed Distribution Systems",
            "building",
            "vector",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );
        SearchResult weakRoutedLexical = new SearchResult(
            "Supply Notes",
            "",
            "Count spare fittings and hand tools before transport.",
            "Count spare fittings and hand tools before transport.",
            "GD-271",
            "Materials Index",
            "utility",
            "route-focus",
            "reference",
            "mixed",
            "general",
            "water_distribution"
        );
        SearchResult weakRankedLexical = new SearchResult(
            "Supply Notes",
            "",
            "Count spare fittings and hand tools before transport.",
            "Count spare fittings and hand tools before transport.",
            "GD-272",
            "Materials Index",
            "utility",
            "lexical",
            "reference",
            "mixed",
            "general",
            "water_distribution"
        );
        SearchResult strongRoutedVector = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "GD-273",
            "Storage Tank Planning",
            "building",
            "vector",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        PackRepository.SupportBreakdown rankedVectorSupport = PackRepository.supportBreakdownForTest(query, strongRankedVector);
        PackRepository.SupportBreakdown routedLexicalSupport = PackRepository.supportBreakdownForTest(query, weakRoutedLexical);
        PackRepository.SupportBreakdown rankedLexicalSupport = PackRepository.supportBreakdownForTest(query, weakRankedLexical);
        PackRepository.SupportBreakdown routedVectorSupport = PackRepository.supportBreakdownForTest(query, strongRoutedVector);

        assertTrue(
            "rankedVector=" + rankedVectorSupport.supportWithMetadata()
                + " routedLexical=" + routedLexicalSupport.supportWithMetadata(),
            rankedVectorSupport.supportWithMetadata() >= routedLexicalSupport.supportWithMetadata() + 12
        );
        assertEquals(
            "GD-270",
            PackRepository.selectAnswerAnchorForTest(query, strongRankedVector, weakRoutedLexical).guideId
        );
        assertTrue(routedVectorSupport.supportWithMetadata() + 11 >= rankedLexicalSupport.supportWithMetadata());
        assertEquals(
            "GD-273",
            PackRepository.selectAnswerAnchorForTest(query, weakRankedLexical, strongRoutedVector).guideId
        );
    }

    @Test
    public void selectExplicitWaterDistributionAnchorUsesSupportBreakdown() {
        String query = "how do i design a gravity-fed water distribution system";
        SearchResult weakLexical = new SearchResult(
            "Supply Notes",
            "",
            "Count spare fittings and hand tools before transport.",
            "Count spare fittings and hand tools before transport.",
            "GD-271",
            "Materials Index",
            "utility",
            "",
            "reference",
            "mixed",
            "general",
            "water_distribution"
        );
        SearchResult strongVector = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "GD-270",
            "Gravity-Fed Distribution Systems",
            "building",
            "vector",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        PackRepository.SupportBreakdown lexicalSupport = PackRepository.supportBreakdownForTest(query, weakLexical);
        PackRepository.SupportBreakdown vectorSupport = PackRepository.supportBreakdownForTest(query, strongVector);

        // weakLexical support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // strongVector support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // Winning delta: supportWithMetadata() keeps the vector row ahead before chooser-specific bonuses apply.
        assertTrue(
            "distribution vector=" + vectorSupport.supportWithMetadata()
                + " lexical=" + lexicalSupport.supportWithMetadata(),
            vectorSupport.supportWithMetadata() > lexicalSupport.supportWithMetadata()
        );
        assertEquals(
            "GD-270",
            PackRepository.selectExplicitWaterDistributionAnchorForTest(query, weakLexical, strongVector).guideId
        );
    }

    @Test
    public void selectExplicitWaterStorageAnchorUsesSupportBreakdown() {
        String query = "what is the safest way to store treated water long term";
        SearchResult weakLexical = new SearchResult(
            "Supply Notes",
            "",
            "Count spare fittings and hand tools before transport.",
            "Count spare fittings and hand tools before transport.",
            "GD-373",
            "Materials Index",
            "building",
            "",
            "reference",
            "immediate",
            "general",
            "water_storage"
        );
        SearchResult strongVector = new SearchResult(
            "Storage & Material Management",
            "",
            "Use food-safe containers, sanitize them, seal them, and rotate treated water on a schedule.",
            "Use food-safe containers, sanitize them, seal them, and rotate treated water on a schedule.",
            "GD-252",
            "Water Storage & Rotation",
            "resource-management",
            "vector",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );

        PackRepository.SupportBreakdown lexicalSupport = PackRepository.supportBreakdownForTest(query, weakLexical);
        PackRepository.SupportBreakdown vectorSupport = PackRepository.supportBreakdownForTest(query, strongVector);

        // weakLexical support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // strongVector support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // Winning delta: supportWithMetadata() keeps the storage-specific vector row ahead before role bias applies.
        assertTrue(
            "storage vector=" + vectorSupport.supportWithMetadata()
                + " lexical=" + lexicalSupport.supportWithMetadata(),
            vectorSupport.supportWithMetadata() > lexicalSupport.supportWithMetadata()
        );
        assertEquals(
            "GD-252",
            PackRepository.selectExplicitWaterStorageAnchorForTest(query, weakLexical, strongVector).guideId
        );
    }

    @Test
    public void selectSpecializedStructuredAnchorUsesSupportBreakdown() {
        String query = "How do I make soap from animal fat safely enough that it's actually useful?";
        SearchResult weakLexical = new SearchResult(
            "Homestead Chemistry",
            "",
            "Soapmaking notes with broad handling guidance and no dedicated process walkthrough.",
            "Soapmaking notes with broad handling guidance and no dedicated process walkthrough.",
            "GD-571",
            "General handling notes",
            "crafts",
            "",
            "subsystem",
            "mixed",
            "soapmaking",
            ""
        );
        SearchResult strongVector = new SearchResult(
            "Homestead Chemistry",
            "",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "vector",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        PackRepository.SupportBreakdown lexicalSupport = PackRepository.supportBreakdownForTest(query, weakLexical);
        PackRepository.SupportBreakdown vectorSupport = PackRepository.supportBreakdownForTest(query, strongVector);

        // weakLexical support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // strongVector support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // Winning delta: supportWithMetadata() keeps the dedicated soapmaking vector row ahead before anchor-alignment bias.
        assertTrue(
            "broadHouse vector=" + vectorSupport.supportWithMetadata()
                + " lexical=" + lexicalSupport.supportWithMetadata(),
            vectorSupport.supportWithMetadata() > lexicalSupport.supportWithMetadata()
        );
        assertEquals(
            "GD-122",
            PackRepository.selectSpecializedStructuredAnchorForTest(query, weakLexical, strongVector).guideId
        );
    }

    @Test
    public void selectBroadHouseAnchorUsesSupportBreakdown() {
        String query = "how do i build a house";
        SearchResult weakLexical = new SearchResult(
            "Supply Notes",
            "",
            "Count nails, spare fittings, and hand tools before transport.",
            "Count nails, spare fittings, and hand tools before transport.",
            "GD-400",
            "Materials Index",
            "building",
            "",
            "reference",
            "immediate",
            "general",
            "foundation,wall_construction"
        );
        SearchResult strongVector = new SearchResult(
            "Construction & Carpentry",
            "",
            "Starter build sequence for site prep, foundations, walls, roofing, and weatherproofing.",
            "Starter build sequence for site prep, foundations, walls, roofing, and weatherproofing.",
            "GD-401",
            "Roofing and Weatherproofing",
            "building",
            "vector",
            "planning",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing,weatherproofing"
        );

        PackRepository.SupportBreakdown lexicalSupport = PackRepository.supportBreakdownForTest(query, weakLexical);
        PackRepository.SupportBreakdown vectorSupport = PackRepository.supportBreakdownForTest(query, strongVector);

        // weakLexical support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // strongVector support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // Winning delta: supportWithMetadata() keeps the sectioned cabin-house vector row ahead before broad-house bias.
        assertTrue(
            "broadHouse vector=" + vectorSupport.supportWithMetadata()
                + " lexical=" + lexicalSupport.supportWithMetadata(),
            vectorSupport.supportWithMetadata() > lexicalSupport.supportWithMetadata()
        );
        assertEquals(
            "GD-401",
            PackRepository.selectBroadHouseAnchorForTest(query, weakLexical, strongVector).guideId
        );
    }

    @Test
    public void findRouteFocusedAnchorUsesSupportBreakdown() {
        String query = "how do i design a gravity-fed water distribution system";
        SearchResult weakRoute = new SearchResult(
            "Water System Materials Index",
            "",
            "Pipe fittings, valve sizes, and spare parts notes.",
            "Pipe fittings, valve sizes, and spare parts notes.",
            "GD-271",
            "Materials Index",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "general",
            "water_distribution"
        );
        SearchResult strongRoute = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "GD-270",
            "Storage Tank Planning",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        PackRepository.SupportBreakdown weakSupport = PackRepository.supportBreakdownForTest(query, weakRoute);
        PackRepository.SupportBreakdown strongSupport = PackRepository.supportBreakdownForTest(query, strongRoute);

        // weakRoute support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // strongRoute support: lexical / metadata / topic / section / penalty -> supportWithMetadata().
        // Winning delta: supportWithMetadata() keeps the stronger route-focused row ahead before route-section preference.
        assertTrue(strongSupport.supportWithMetadata() > weakSupport.supportWithMetadata());
        assertEquals(
            "GD-270",
            PackRepository.routeFocusedAnchorForTest(query, java.util.List.of(weakRoute, strongRoute), true).guideId
        );
    }

    @Test
    public void anchorGuideScoresIncludeVectorRowsWhenMetadataBonusIsPositive() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        SearchResult lexicalOffTopic = new SearchResult(
            "Camp Inventory Ledger",
            "",
            "Count crates and spare fittings before transport.",
            "Count crates and spare fittings before transport.",
            "GD-727",
            "Inventory",
            "utility",
            "lexical",
            "reference",
            "mixed",
            "general",
            "maintenance,storage"
        );
        SearchResult vectorShelter = new SearchResult(
            "Camp Inventory Ledger",
            "",
            "Count crates and spare fittings before transport.",
            "Count crates and spare fittings before transport.",
            "GD-345",
            "Inventory",
            "survival",
            "vector",
            "starter",
            "immediate",
            "emergency_shelter",
            "maintenance,storage"
        );

        java.util.LinkedHashMap<String, PackRepository.GuideScore> guideScores =
            PackRepository.buildAnchorGuideScoresForTest(
                query,
                java.util.Arrays.asList(lexicalOffTopic, vectorShelter),
                false
            );

        assertTrue(guideScores.containsKey("gd-345"));
        assertTrue(guideScores.get("gd-345").totalScore > 0);
    }

    @Test
    public void anchorGuideScoresStillDropVectorRowsWhenMetadataBonusIsZero() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        SearchResult vectorOffTopic = new SearchResult(
            "Camp Inventory Ledger",
            "",
            "Count crates and spare fittings before transport.",
            "Count crates and spare fittings before transport.",
            "GD-727",
            "Inventory",
            "utility",
            "vector",
            "reference",
            "mixed",
            "general",
            "maintenance,storage"
        );

        java.util.LinkedHashMap<String, PackRepository.GuideScore> guideScores =
            PackRepository.buildAnchorGuideScoresForTest(
                query,
                java.util.Collections.singletonList(vectorOffTopic),
                false
            );

        assertFalse(guideScores.containsKey("gd-727"));
    }

    @Test
    public void anchorGuideScoresDoNotDoubleCountMetadataBonusForLexicalRows() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        SearchResult lexicalShelter = new SearchResult(
            "Camp Inventory Ledger",
            "",
            "Count crates and spare fittings before transport.",
            "Count crates and spare fittings before transport.",
            "GD-345",
            "Inventory",
            "survival",
            "lexical",
            "starter",
            "immediate",
            "emergency_shelter",
            "maintenance,storage"
        );

        java.util.LinkedHashMap<String, PackRepository.GuideScore> guideScores =
            PackRepository.buildAnchorGuideScoresForTest(
                query,
                java.util.Collections.singletonList(lexicalShelter),
                false
            );
        int expectedTotal = PackRepository.supportBreakdownForTest(query, lexicalShelter).supportWithMetadata()
            + 12
            + PackRepository.anchorAlignmentBonusForTest(query, lexicalShelter)
            + 2;

        assertTrue(guideScores.containsKey("gd-345"));
        assertEquals(expectedTotal, guideScores.get("gd-345").totalScore);
    }

    @Test
    public void anchorGuideScoresTreatEmptyRetrievalModeAsNonVector() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        SearchResult blankModeOffTopic = new SearchResult(
            "Camp Inventory Ledger",
            "",
            "Count crates and spare fittings before transport.",
            "Count crates and spare fittings before transport.",
            "GD-999",
            "Inventory",
            "utility",
            "",
            "reference",
            "mixed",
            "general",
            "maintenance,storage"
        );

        java.util.LinkedHashMap<String, PackRepository.GuideScore> guideScores =
            PackRepository.buildAnchorGuideScoresForTest(
                query,
                java.util.Collections.singletonList(blankModeOffTopic),
                false
            );

        assertFalse(guideScores.containsKey("gd-999"));
    }

    @Test
    public void ordinaryWaterStorageGuideSweepUsesLowerThreshold() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int broadThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, false, 12);
        int compactThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, true, 12);

        assertEquals(9, broadThreshold);
        assertEquals(6, compactThreshold);
    }

    @Test
    public void explicitWaterDistributionGuideSweepKeepsBroaderThreshold() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i build a rainwater cistern and gravity fed water distribution system"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i build a rainwater cistern and gravity fed water distribution system"
        );

        int broadThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, false, 12);
        int compactThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, true, 12);

        assertEquals(24, broadThreshold);
        assertEquals(16, compactThreshold);
    }

    @Test
    public void explicitWaterDistributionGuideSweepUsesLowerRuntimeThresholdWithoutBm25() {
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i build a rainwater cistern and gravity fed water distribution system"
        );

        assertEquals(5, PackRepository.runtimeRouteGuideSearchThreshold(metadataProfile, false, 24));
        assertEquals(24, PackRepository.runtimeRouteGuideSearchThreshold(metadataProfile, true, 24));
    }

    @Test
    public void soapmakingGuideSweepKeepsBroaderThreshold() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i make soap from animal fat safely enough that it's actually useful"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i make soap from animal fat safely enough that it's actually useful"
        );

        int broadThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, false, 12);
        int compactThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, true, 12);

        assertEquals(24, broadThreshold);
        assertEquals(18, compactThreshold);
    }

    @Test
    public void soapmakingPromptUsesSpecializedNoBm25FtsOrdering() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "how do i make soap from animal fat and ash"
        );

        assertEquals("soapmaking_priority", orderLabel);
    }

    @Test
    public void communityGovernanceGuideSweepUsesLowerThreshold() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "How do we merge with another group if we don't trust each other yet?"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "How do we merge with another group if we don't trust each other yet?"
        );

        int broadThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, false, 12);
        int compactThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, true, 12);

        assertEquals(10, broadThreshold);
        assertEquals(8, compactThreshold);
    }

    @Test
    public void routePolicyExtractionSeedMatrixLocksSpecializedRouteContracts() {
        SearchResult waterDistributionSignal = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning.",
            "Gravity-fed distribution and storage tank planning.",
            "GD-270",
            "Storage Tank Planning",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,water_distribution"
        );
        SearchResult genericWaterStorage = new SearchResult(
            "Storage & Material Management",
            "",
            "Water storage basics and food-safe containers.",
            "Water storage basics and food-safe containers.",
            "GD-252",
            "Water Storage: Hydration Assurance",
            "resource-management",
            "route-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
        SearchResult soapmakingSignal = new SearchResult(
            "Homestead Chemistry",
            "",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        SearchResult genericSoapSafety = new SearchResult(
            "Storage Records",
            "",
            "Inventory labels and shelf records.",
            "Inventory labels and shelf records.",
            "GD-262",
            "Inventory Labels",
            "resource-management",
            "route-focus",
            "safety",
            "immediate",
            "general",
            ""
        );
        SearchResult governanceSignal = new SearchResult(
            "Commons Management & Sustainable Resource Governance",
            "",
            "Trust repair, mediation, reputation, and vouching for cautious group integration.",
            "Trust repair, mediation, reputation, and vouching for cautious group integration.",
            "GD-626",
            "Trust Repair, Reputation, and Vouching",
            "resource-management",
            "route-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution,trust_systems"
        );
        SearchResult genericGovernanceAccounting = new SearchResult(
            "Inventory Ledgers",
            "",
            "Accounting reserves and pooled fund records.",
            "Accounting reserves and pooled fund records.",
            "GD-657",
            "Accounting Reserves",
            "resource-management",
            "route-focus",
            "reference",
            "long_term",
            "general",
            ""
        );

        Object[][] routeCases = new Object[][]{
            {
                "how do i design a gravity-fed water distribution system",
                "water_distribution",
                "water_distribution_priority",
                waterDistributionSignal,
                genericWaterStorage
            },
            {
                "How do I make soap from animal fat safely enough that it's actually useful?",
                "soapmaking",
                "soapmaking_priority",
                soapmakingSignal,
                genericSoapSafety
            },
            {
                "How do we merge with another group if we don't trust each other yet?",
                "community_governance",
                "community_governance_priority",
                governanceSignal,
                genericGovernanceAccounting
            }
        };

        for (Object[] routeCase : routeCases) {
            String query = (String) routeCase[0];
            String expectedStructure = (String) routeCase[1];
            String expectedPriority = (String) routeCase[2];
            SearchResult directSignal = (SearchResult) routeCase[3];
            SearchResult nonSignal = (SearchResult) routeCase[4];

            assertEquals(query, expectedStructure, QueryMetadataProfile.fromQuery(query).preferredStructureType());
            assertEquals(query, true, PackRepository.shouldRequireDirectAnchorSignalForTest(query));
            assertEquals(query, expectedPriority, PackRepository.noBm25RouteFtsOrderLabelForTest(query));
            assertEquals(query, true, PackRepository.hasDirectAnchorSignalForTest(query, directSignal));
            assertEquals(query, false, PackRepository.hasDirectAnchorSignalForTest(query, nonSignal));
        }

        assertEquals(600, PackRepository.routeChunkCandidateLimitForTest("how do i build a house", 12));
        assertEquals(42, PackRepository.routeChunkCandidateTargetForTest("how do i build a house", 12));
    }

    @Test
    public void routeFocusedSearchHelperPreservesRouteSpecOrderAndTerms() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        List<QueryRouteProfile.RouteSearchSpec> routeSpecs =
            PackRouteFocusedSearchHelper.routeSearchSpecs(queryTerms);

        assertEquals(queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower).size(), routeSpecs.size());
        for (int index = 0; index < routeSpecs.size(); index++) {
            QueryRouteProfile.RouteSearchSpec routeSpec = routeSpecs.get(index);
            PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec);

            assertEquals(routeSpec.text(), routeStep.routeSpec.text());
            assertEquals(PackRepository.QueryTerms.fromText(routeSpec.text(), queryTerms.routeProfile).primaryKeywordTokens(),
                routeStep.specTerms.primaryKeywordTokens());
            assertTrue(routeStep.tokens.size() <= 6);
        }
    }

    @Test
    public void routeFocusedSearchHelperKeepsCategoryFallbackAndTokenCap() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        QueryRouteProfile.RouteSearchSpec routeSpec = new QueryRouteProfile.RouteSearchSpec(
            "alpha beta gamma delta epsilon zeta eta",
            Collections.emptySet(),
            0
        );

        PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
            PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec, 3);

        assertEquals(List.of("alpha", "beta", "gamma"), routeStep.tokens);
        assertEquals(new java.util.ArrayList<>(queryTerms.routeProfile.preferredCategories()), routeStep.categories);
    }

    @Test
    public void communitySecurityGuideSweepUsesLowerThreshold() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "How do we protect a vulnerable work site without spreading people too thin?"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "How do we protect a vulnerable work site without spreading people too thin?"
        );

        int broadThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, false, 12);
        int compactThreshold = PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, true, 12);

        assertEquals(10, broadThreshold);
        assertEquals(8, compactThreshold);
    }

    @Test
    public void soapmakingRouteChunkTargetStaysTighterThanBroadHouse() {
        int soapTarget = PackRepository.routeChunkCandidateTargetForTest(
            "how do i make soap from animal fat safely enough that it's actually useful",
            12
        );
        int houseTarget = PackRepository.routeChunkCandidateTargetForTest(
            "how do i build a house",
            12
        );

        assertEquals(18, soapTarget);
        assertTrue(soapTarget < houseTarget);
    }

    @Test
    public void starterBuildKeywordSqlLimitUsesSmallerRouteFocusedBudget() {
        int sqlLimit = PackRepository.keywordSqlLimitForTest("how do i build a house", 64);

        assertEquals(128, sqlLimit);
    }

    @Test
    public void governanceAndSecurityQueriesDoNotScanFullRouteCursor() {
        assertEquals(
            false,
            PackRepository.shouldScanFullRouteCursorForTest(
                "how do i protect a vulnerable work site, field, or water point without spreading people too thin?"
            )
        );
        assertEquals(
            false,
            PackRepository.shouldScanFullRouteCursorForTest(
                "someone is stealing food from the group what do we do"
            )
        );
    }

    @Test
    public void defaultKeywordSqlLimitKeepsBroaderFallbackBudget() {
        int sqlLimit = PackRepository.keywordSqlLimitForTest("how do i make soap", 64);

        assertEquals(256, sqlLimit);
    }

    @Test
    public void explicitHouseFollowUpKeepsBroadRouteChunkBudget() {
        int candidateLimit = PackRepository.routeChunkCandidateLimitForTest("cabin house foundation drainage", 12);
        int candidateTarget = PackRepository.routeChunkCandidateTargetForTest("cabin house foundation drainage", 12);

        assertEquals(600, candidateLimit);
        assertEquals(42, candidateTarget);
    }

    @Test
    public void houseSiteSelectionPromptUsesTrimmedRouteChunkBudget() {
        int candidateLimit = PackRepository.routeChunkCandidateLimitForTest(
            "how do i choose a safe site and foundation for a small cabin",
            12
        );
        int candidateTarget = PackRepository.routeChunkCandidateTargetForTest(
            "how do i choose a safe site and foundation for a small cabin",
            12
        );

        assertEquals(128, candidateLimit);
        assertEquals(16, candidateTarget);
    }

    @Test
    public void houseSiteSelectionPromptUsesLowerRouteGuideThreshold() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        assertTrue(routeProfile.usesCompactGuideSweep("how do i choose a safe site and foundation for a small cabin"));
        assertEquals(12, PackRepository.routeGuideSearchThreshold(routeProfile, metadataProfile, true, 12));
    }

    @Test
    public void broadBuildingSitePromptUsesSiteSelectionPriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        );

        assertEquals("site_selection_priority", orderLabel);
    }

    @Test
    public void roofFollowUpUsesRoofingPriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "what about sealing the roof"
        );

        assertEquals("roofing_priority", orderLabel);
    }

    @Test
    public void wallBuildPromptUsesWallPriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "how do i frame a cabin wall with rough lumber"
        );

        assertEquals("wall_construction_priority", orderLabel);
    }

    @Test
    public void foundationBuildPromptUsesFoundationPriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "how do i pour a cabin foundation with limited cement"
        );

        assertEquals("foundation_priority", orderLabel);
    }

    @Test
    public void seasonalSiteSelectionPromptUsesMicroclimatePriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "winter sun summer shade site selection cabin house"
        );

        assertEquals("site_selection_microclimate_priority", orderLabel);
    }

    @Test
    public void waterDistributionPromptUsesWaterDistributionPriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "how do i design a gravity-fed water distribution system"
        );

        assertEquals("water_distribution_priority", orderLabel);
    }

    @Test
    public void clayOvenPromptUsesClayOvenPriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "how do i build a clay oven"
        );

        assertEquals("clay_oven_priority", orderLabel);
    }

    @Test
    public void communityGovernancePromptUsesGovernancePriorityOrderWithoutBm25() {
        String orderLabel = PackRepository.noBm25RouteFtsOrderLabelForTest(
            "How do we merge with another group if we don't trust each other yet?"
        );

        assertEquals("community_governance_priority", orderLabel);
    }

    @Test
    public void broadHousePromptKeepsBroadRouteChunkBudget() {
        int candidateLimit = PackRepository.routeChunkCandidateLimitForTest("how do i build a house", 12);
        int candidateTarget = PackRepository.routeChunkCandidateTargetForTest("how do i build a house", 12);

        assertEquals(600, candidateLimit);
        assertEquals(42, candidateTarget);
    }

    @Test
    public void houseFollowUpFtsQueryUsesPrefixMatching() {
        String ftsQuery = PackRepository.buildFtsQueryForTest("cabin house foundation drainage");

        assertTrue(ftsQuery.contains("foundation*"));
        assertTrue(ftsQuery.contains("drainage*"));
        assertFalse(ftsQuery.contains("\"foundation\""));
    }

    @Test
    public void hyphenatedWaterDistributionFtsQuerySplitsGravityFedIntoSeparatePrefixes() {
        String ftsQuery = PackRepository.buildFtsQueryForTest(
            "how do i design a gravity-fed water distribution system"
        );

        assertTrue(ftsQuery.contains("gravity*"));
        assertTrue(ftsQuery.contains("fed*"));
        assertFalse(ftsQuery.contains("gravity-fed*"));
    }

    @Test
    public void runtimeFtsProbePrefersFts4WhenCompileOptionsMissButRuntimeProbeSucceeds() {
        String debugLine = PackRepository.buildFtsRuntimeDebugLineForTest(
            false,
            false,
            true,
            true,
            false,
            true
        );

        assertTrue(debugLine.contains("table=lexical_chunks_fts4"));
        assertTrue(debugLine.contains("supportsBm25=false"));
        assertTrue(debugLine.contains("runtime4=true"));
    }

    @Test
    public void runtimeFtsProbeFallsBackToLikeWhenNoFtsRuntimeExecutes() {
        String debugLine = PackRepository.buildFtsRuntimeDebugLineForTest(
            false,
            false,
            true,
            true,
            false,
            false
        );

        assertTrue(debugLine.contains("table="));
        assertTrue(debugLine.contains("available=false"));
        assertTrue(debugLine.contains("runtime4=false"));
    }

    @Test
    public void searchSummaryLineIncludesFallbackAndRerankTiming() {
        String debugLine = PackRepository.buildSearchSummaryLineForTest(
            "how do i improvise a rain shelter",
            true,
            3,
            12,
            6,
            2,
            18L,
            22L,
            17L,
            31L,
            9L,
            103L,
            "hybrid"
        );

        assertTrue(debugLine.contains("fallback=hybrid"));
        assertTrue(debugLine.contains("rerankMs=9"));
        assertTrue(debugLine.contains("vectorHits=6"));
    }

    @Test
    public void slowQueryTripwirePrefersFirstBudgetBreachingStage() {
        String debugLine = PackRepository.buildSlowQueryTripwireDebugLineForTest(
            "how do i improvise a rain shelter",
            12L,
            144L,
            10L,
            20L,
            5L,
            201L,
            "plainLike_empty_lexical"
        );

        assertTrue(debugLine.contains("stage=fts"));
        assertTrue(debugLine.contains("fallback=plainLike_empty_lexical"));
    }

    @Test
    public void ordinaryWaterStorageStrongRouteCoverageUsesSmallerLexicalBudget() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int lexicalLimit = PackRepository.lexicalCandidateLimit(routeProfile, metadataProfile, 16, 9);

        assertEquals(48, lexicalLimit);
    }

    @Test
    public void houseRouteCoverageKeepsModerateLexicalBudget() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");

        int lexicalLimit = PackRepository.lexicalCandidateLimit(routeProfile, metadataProfile, 16, 12);

        assertEquals(64, lexicalLimit);
    }

    @Test
    public void weakRouteCoverageKeepsDefaultLexicalBudget() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );

        int lexicalLimit = PackRepository.lexicalCandidateLimit(routeProfile, metadataProfile, 16, 4);

        assertEquals(72, lexicalLimit);
    }

    @Test
    public void diversifiedWaterContextRejectsEarthworksHarvestingSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        SearchResult candidate = new SearchResult(
            "Drainage and Earthworks",
            "",
            "Swale design for water harvesting and flow.",
            "Swale design for water harvesting and flow.",
            "GD-333",
            "Swale Design for Water Harvesting and Flow",
            "building",
            "route-focus",
            "subsystem",
            "mixed",
            "earth_shelter",
            "site_selection,drainage"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextRejectsUndergroundBunkerSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Underground Shelter & Bunker Construction",
            "",
            "Site assessment and planning for an underground shelter.",
            "Site assessment and planning for an underground shelter with shoring and overburden.",
            "GD-873",
            "Site Assessment & Planning",
            "building",
            "route-focus",
            "subsystem",
            "long_term",
            "earth_shelter",
            "site_selection,drainage,ventilation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextRejectsEarthShelteringSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("drainage cabin house foundation");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("drainage cabin house foundation");
        SearchResult candidate = new SearchResult(
            "Earth-Sheltering Construction",
            "",
            "Overview of earth-sheltered design and drainage.",
            "Earth-sheltering overview covering drainage, foundation, and ventilation for bermed structures.",
            "GD-329",
            "Overview",
            "building",
            "route-focus",
            "planning",
            "mixed",
            "earth_shelter",
            "site_selection,drainage,foundation,ventilation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseDrainageContextRejectsAccessibleSupportSection() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("drainage cabin house foundation");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("drainage cabin house foundation");
        SearchResult candidate = new SearchResult(
            "Accessible Shelter & Universal Design",
            "",
            "Grab bars, support rails, and accessibility adjustments.",
            "Grab bars, threshold support, and accessibility adjustments for universal design.",
            "GD-444",
            "Grab Bars and Support Features",
            "building",
            "hybrid",
            "subsystem",
            "mixed",
            "general",
            "weatherproofing"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextKeepsGeneralFoundationSupport() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Foundations and Footings",
            "",
            "Frost line depth, footing choices, and drainage for long-term houses.",
            "Frost line depth, footing choices, and drainage for long-term houses.",
            "GD-383",
            "Frost Line and Frost Heave",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "general",
            "drainage,foundation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(true, keep);
    }

    @Test
    public void broadHouseRouteAnchorPrefersFoundationSectionOverStructuralOverview() {
        SearchResult structuralOverview = new SearchResult(
            "Construction & Carpentry",
            "",
            "Structural overview for off-grid builders.",
            "Structural overview for off-grid builders with general engineering basics.",
            "GD-094",
            "Structural Engineering Basics for Off-Grid Builders",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );
        SearchResult foundations = new SearchResult(
            "Foundations and Footings",
            "",
            "Footing choices, frost line depth, and drainage for long-term houses.",
            "Footing choices, frost line depth, and drainage for long-term houses.",
            "GD-383",
            "Frost Line and Frost Heave",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "general",
            "drainage,foundation"
        );

        SearchResult selected = PackRepository.routeFocusedAnchorForTest(
            "how do i build a house",
            java.util.List.of(structuralOverview, foundations),
            false
        );

        assertEquals("GD-383", selected.guideId);
    }

    @Test
    public void diversifiedHouseContextRejectsGeneralSurvivalClimateNoise() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Desert Survival & Arid Environment Living",
            "",
            "Heat, shelter, and desert adaptation.",
            "Desert shelter construction and climate adaptation for arid environments.",
            "GD-353",
            "Desert Shelter Construction",
            "survival",
            "route-focus",
            "subsystem",
            "long_term",
            "general",
            "ventilation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextRejectsAccessibilityDesignNoise() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Accessible Shelter & Universal Design",
            "",
            "Grab bars and layout adjustments for universal design.",
            "Grab bars, one-handed operation design, and accessibility adjustments for sheltered living.",
            "GD-444",
            "Grab Bars and Support Features",
            "building",
            "hybrid",
            "subsystem",
            "mixed",
            "general",
            "wall_construction,weatherproofing"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedHouseContextRejectsSharedShelterGuideNoise() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");
        SearchResult candidate = new SearchResult(
            "Seasonal Shelter Adaptation & Long-Term Camp Evolution",
            "",
            "Shared shelter dynamics and camp-evolution notes.",
            "Shared shelter dynamics and camp-evolution notes.",
            "GD-618",
            "Group Dynamics in Shared Shelters",
            "survival",
            "guide-focus",
            "reference",
            "long_term",
            "cabin_house",
            "site_selection,foundation"
        );

        boolean keep = PackRepository.supportCandidateMatchesRoute(routeProfile, metadataProfile, true, candidate);

        assertEquals(false, keep);
    }

    @Test
    public void diversifiedContextHeavilyPenalizesBlankGuideFocusSupport() {
        int penalty = PackRepository.supportStructurePenalty(true, "guide-focus", "");

        assertEquals(-40, penalty);
    }

    @Test
    public void nonDiversifiedContextKeepsLegacyBlankSectionPenalty() {
        int penalty = PackRepository.supportStructurePenalty(false, "guide-focus", "");

        assertEquals(-10, penalty);
    }

    @Test
    public void sectionedSupportAvoidsStructurePenalty() {
        int penalty = PackRepository.supportStructurePenalty(true, "route-focus", "Water Storage");

        assertEquals(0, penalty);
    }

    private static boolean containsGuideId(java.util.List<SearchResult> results, String guideId) {
        for (SearchResult result : results) {
            if (guideId.equals(result.guideId)) {
                return true;
            }
        }
        return false;
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

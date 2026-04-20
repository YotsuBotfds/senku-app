package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class QueryRouteProfileTest {
    @Test
    public void houseBuildRejectsEmergencyShelterSections() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "primitive shelter construction techniques",
            "debris hut emergency shelter",
            "survival",
            "",
            "construction of temporary and semi-permanent shelters",
            "debris hut ridgepole leaves insulation for one person emergency shelter"
        );

        assertFalse(supported);
    }

    @Test
    public void houseBuildAcceptsCarpentryGuideMaterial() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "construction & carpentry",
            "foundations and wall framing",
            "building",
            "",
            "foundations, wall framing, roofing systems, doors and windows",
            "site drainage foundation wall framing roof framing weatherproofing and ventilation"
        );

        assertTrue(supported);
    }

    @Test
    public void siteAndFoundationCabinPromptUsesHouseBuildRouting() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        boolean supported = profile.supportsRouteResult(
            "shelter site selection & hazard assessment",
            "terrain analysis",
            "survival",
            "",
            "terrain analysis, water proximity, wind exposure, natural hazards, and seasonal considerations",
            "prefer mid-slope locations with good drainage and stable ground for foundations"
        );

        assertTrue(profile.prefersDiversifiedAnswerContext());
        assertTrue(supported);
    }

    @Test
    public void houseBuildRejectsAccessibleShelterDistractorSections() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "accessible shelter & universal design",
            "children-safe environments",
            "building",
            "",
            "wheelchair accessibility, elderly-friendly design, and one-handed operation fixtures",
            "door width thresholds and grab bars for safe movement through the shelter"
        );

        assertFalse(supported);
    }

    @Test
    public void roofWaterproofingPromptUsesHouseBuildRouting() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "how do i waterproof a roof with no tar or shingles"
        );

        boolean supported = profile.supportsRouteResult(
            "insulation & weatherproofing",
            "core principles of thermal efficiency",
            "building",
            "",
            "thermal envelope design, air sealing, moisture management",
            "weatherproofing, rainproofing, and water shedding for roof assemblies"
        );

        assertTrue(profile.prefersDiversifiedAnswerContext());
        assertTrue(supported);
    }

    @Test
    public void houseBuildRejectsUndergroundBunkerGuidanceByDefault() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "underground shelter & bunker construction",
            "site assessment & planning",
            "building",
            "",
            "comprehensive guide to underground shelters, shoring, and waterproofing",
            "before breaking ground for an underground shelter, plan shoring, overburden, and fallout protection"
        );

        assertFalse(supported);
    }

    @Test
    public void earthShelteredCabinPromptStillAllowsEarthShelterSections() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "how do i build an earth sheltered cabin"
        );

        boolean supported = profile.supportsRouteResult(
            "underground shelter & bunker construction",
            "site assessment & planning",
            "building",
            "",
            "comprehensive guide to underground shelters, shoring, and waterproofing",
            "before breaking ground for an underground shelter, plan shoring, overburden, and fallout protection"
        );

        assertTrue(supported);
    }

    @Test
    public void buildingSitePromptUsesHouseBuildRouting() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        );

        boolean supported = profile.supportsRouteResult(
            "shelter site selection & hazard assessment",
            "terrain analysis",
            "survival",
            "",
            "terrain analysis, wind exposure, water proximity, and natural hazards",
            "prefer a dry site with good drainage, manageable wind exposure, winter sun, and practical access routes"
        );

        assertTrue(profile.prefersDiversifiedAnswerContext());
        assertTrue(supported);
    }

    @Test
    public void buildingSitePromptDoesNotAddGenericStarterBuildDrainageSpec() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        );

        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        );

        boolean hasSiteSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("shelter site selection hazard assessment")
        );
        boolean hasGenericStarterDrainageSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("simple one room house site drainage foundation frame roof weatherproofing")
        );

        assertTrue(hasSiteSpec);
        assertFalse(hasGenericStarterDrainageSpec);
    }

    @Test
    public void buildingSitePromptSkipsRawQuerySpecAndLeadsWithTerrainSignals() {
        String query = "How do I choose a building site if drainage, wind, sun, and access all matter?";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);

        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasRawQuerySpec = specs.stream().anyMatch(spec -> spec.text().equals(query.toLowerCase()));

        assertFalse(hasRawQuerySpec);
        assertFalse(specs.isEmpty());
        assertTrue(specs.get(0).text().startsWith("terrain analysis wind exposure sun exposure access routes"));
    }

    @Test
    public void seasonalSiteFollowUpLeadsWithMicroclimateSignals() {
        String query = "winter sun summer shade site selection wind exposure cabin house";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);

        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        assertFalse(specs.isEmpty());
        assertTrue(specs.get(0).text().startsWith("wind exposure microclimate sun exposure seasonal considerations"));
    }

    @Test
    public void seasonalSiteFollowUpIsFlaggedForPromptShaping() {
        String contextualQuery = "winter sun summer shade site selection wind exposure microclimate cabin";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(contextualQuery);

        assertTrue(profile.isSeasonalHouseSiteSelectionPrompt(contextualQuery));
        assertFalse(QueryRouteProfile.fromQuery("how do i build a house").isSeasonalHouseSiteSelectionPrompt("how do i build a house"));
    }

    @Test
    public void smallCabinSiteAndFoundationPromptStillKeepsFoundationSupportSpec() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(
            "how do i choose a safe site and foundation for a small cabin"
        );

        boolean hasSiteSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("shelter site selection hazard assessment")
        );
        boolean hasFoundationSupportSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("simple one room house site drainage foundation frame roof weatherproofing")
        );

        assertTrue(hasSiteSpec);
        assertTrue(hasFoundationSupportSpec);
    }

    @Test
    public void smallCabinSiteAndFoundationPromptAlsoSkipsRawQuerySpec() {
        String query = "how do i choose a safe site and foundation for a small cabin";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);

        boolean hasRawQuerySpec = profile.routeSearchSpecs(query).stream()
            .anyMatch(spec -> spec.text().equals(query));

        assertFalse(hasRawQuerySpec);
    }

    @Test
    public void houseBuildRejectsCaveShelterGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "cave shelter systems and long-term habitation",
            "cave selection criteria",
            "survival",
            "site_selection,drainage,ventilation",
            "long-term cave shelter planning and hazard review",
            "choose caves with stable ceilings, drainage, and strong airflow before long-term habitation"
        );

        assertFalse(supported);
    }

    @Test
    public void houseBuildRejectsGreenhouseDistractorGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "greenhouses and cold frames",
            "watering systems",
            "building",
            "drainage,ventilation",
            "greenhouse climate control and crop watering guidance",
            "use vents, glazing, and watering schedules to manage a productive greenhouse growing space"
        );

        assertFalse(supported);
    }

    @Test
    public void houseBuildRejectsPrimitiveShelterConstructionGuide() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "primitive shelter construction techniques",
            "roof thatching and water runoff",
            "survival",
            "drainage,weatherproofing",
            "low-tech primitive shelter construction and rain shedding methods",
            "use layered thatch and bark on a primitive shelter roof to shed water in survival conditions"
        );

        assertFalse(supported);
    }

    @Test
    public void houseBuildRejectsOutbuildingGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "construction & carpentry",
            "outbuildings for off-grid living",
            "building",
            "drainage,foundation,wall_construction,roofing,ventilation",
            "simple shed construction, chicken coop design, and root cellar construction",
            "simple shed construction and root cellar construction for off-grid living"
        );

        assertFalse(supported);
    }

    @Test
    public void houseBuildRejectsSharedShelterCampEvolutionGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a house");

        boolean supported = profile.supportsRouteResult(
            "seasonal shelter adaptation & long-term camp evolution",
            "group dynamics in shared shelters",
            "survival",
            "site_selection,drainage,foundation",
            "shared shelter rules and camp-growth planning for long-term camps",
            "group dynamics in shared shelters and communal shelter expansion"
        );

        assertFalse(supported);
    }

    @Test
    public void genericHouseQuerySkipsInsulationAndWindowMicroSpecs() {
        String query = "how do i build a house";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasInsulationSpec = false;
        boolean hasWindowSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            String text = spec.text();
            if (text.contains("insulation thermal efficiency")) {
                hasInsulationSpec = true;
            }
            if (text.contains("window door assembly")) {
                hasWindowSpec = true;
            }
        }

        assertFalse(hasInsulationSpec);
        assertFalse(hasWindowSpec);
        assertTrue(specs.size() <= 6);
    }

    @Test
    public void roofPromptKeepsRoofSpecificRouteSpecs() {
        String query = "how do i waterproof a roof with no tar or shingles";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasRoofSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("roof waterproofing rainproofing water shedding")) {
                hasRoofSpec = true;
                break;
            }
        }

        assertTrue(hasRoofSpec);
    }

    @Test
    public void roofPromptSkipsBroadStarterBuildSpecs() {
        String query = "how do i waterproof a roof with no tar or shingles";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasStarterDrainageSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("simple one room house site drainage foundation frame roof weatherproofing")
        );
        boolean hasStarterCabinSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("cabin hut construction floor wall roof insulation ventilation")
        );
        boolean hasBroadRoofSupportSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("roof waterproofing sealants water shedding low resource structure")
        );

        assertFalse(hasStarterDrainageSpec);
        assertFalse(hasStarterCabinSpec);
        assertFalse(hasBroadRoofSupportSpec);
    }

    @Test
    public void insulationPromptSkipsBroadStarterBuildSpecs() {
        String query = "winter is 6 weeks away and we have no insulation";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasInsulationSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("insulation thermal efficiency air sealing drafts heat loss condensation weatherproofing")
        );
        boolean hasStarterDrainageSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("simple one room house site drainage foundation frame roof weatherproofing")
        );
        boolean hasStarterCabinSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("cabin hut construction floor wall roof insulation ventilation")
        );

        assertTrue(hasInsulationSpec);
        assertFalse(hasStarterDrainageSpec);
        assertFalse(hasStarterCabinSpec);
    }

    @Test
    public void wallPromptUsesWallSpecificSpecWithoutBroadStarterSeed() {
        String query = "how do i frame a cabin wall with rough lumber";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasWallSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("wall framing bracing sheathing siding wall assembly moisture barrier")
        );
        boolean hasStarterDrainageSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("simple one room house site drainage foundation frame roof weatherproofing")
        );
        boolean hasStarterCabinSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("cabin hut construction floor wall roof insulation ventilation")
        );

        assertTrue(hasWallSpec);
        assertFalse(hasStarterDrainageSpec);
        assertFalse(hasStarterCabinSpec);
    }

    @Test
    public void wallWeatherproofPromptDoesNotTriggerRoofSpec() {
        String query = "how do i weatherproof a cabin wall with rough lumber";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasRoofSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("roof waterproofing rainproofing water shedding")
        );
        boolean hasWallSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("wall framing bracing sheathing siding wall assembly moisture barrier")
        );

        assertFalse(hasRoofSpec);
        assertTrue(hasWallSpec);
    }

    @Test
    public void foundationPromptUsesFoundationSpecificSpecWithoutBroadStarterSweep() {
        String query = "how do i build a cabin foundation with stone and rubble";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean hasFoundationSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("foundation footing load bearing sill plate pier rubble trench drainage stable ground frost line")
        );
        boolean hasStarterCabinSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("cabin hut construction floor wall roof insulation ventilation")
        );
        boolean hasBroadRoofSupportSpec = specs.stream().anyMatch(
            spec -> spec.text().contains("roof waterproofing sealants water shedding low resource structure")
        );

        assertTrue(hasFoundationSpec);
        assertFalse(hasStarterCabinSpec);
        assertFalse(hasBroadRoofSupportSpec);
    }

    @Test
    public void waterPurificationUsesCompactGuideSweepWhileStorageAndHouseDoNot() {
        QueryRouteProfile purificationProfile = QueryRouteProfile.fromQuery(
            "how do i purify water with no filter"
        );
        QueryRouteProfile storageProfile = QueryRouteProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryRouteProfile houseProfile = QueryRouteProfile.fromQuery("how do i build a house");

        assertTrue(purificationProfile.usesCompactGuideSweep());
        assertFalse(storageProfile.usesCompactGuideSweep());
        assertFalse(houseProfile.usesCompactGuideSweep());
    }

    @Test
    public void houseSiteSelectionPromptsUseCompactGuideSweep() {
        String query = "how do i choose a safe site and foundation for a small cabin";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);

        assertTrue(profile.usesCompactGuideSweep(query));
        assertTrue(profile.routeSearchSpecs(query).size() <= 4);
    }

    @Test
    public void waterStorageFollowUpWithHouseNoiseStaysOutOfStarterBuildRouting() {
        String query = "old soda bottles weatherproofing water storage disinfection hydration assurance material";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);

        assertFalse(profile.isStarterBuildProject());
        assertTrue(profile.usesCompactGuideSweep(query));
        assertTrue(profile.routeSearchSpecs(query).size() <= 4);
    }

    @Test
    public void waterStorageSystemPromptsKeepDistributionSweepEnabled() {
        String query = "how do i set up a rainwater cistern and distribution system to store water";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawDistributionSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("water system design distribution rainwater harvesting cistern")) {
                sawDistributionSpec = true;
                break;
            }
        }

        assertFalse(profile.usesCompactGuideSweep(query));
        assertTrue(sawDistributionSpec);
    }

    @Test
    public void longTermWaterStoragePlanningPromptsUseCompactGuideSweep() {
        String query = "what's the safest way to store treated water long term";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);

        assertTrue(profile.usesCompactGuideSweep(query));
    }

    @Test
    public void explicitWaterDistributionSystemQueryUsesWaterStorageRouting() {
        String query = "how do i design a gravity-fed water distribution system";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawDistributionSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("water system design distribution rainwater harvesting cistern")) {
                sawDistributionSpec = true;
                break;
            }
        }

        assertTrue(profile.isRouteFocused());
        assertFalse(profile.usesCompactGuideSweep(query));
        assertTrue(sawDistributionSpec);
    }

    @Test
    public void explicitWaterDistributionSystemQueryUsesDistributionPromptGuidance() {
        String query = "how do i design a gravity-fed water distribution system";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<String> guidance = profile.promptGuidanceLines();

        assertFalse(guidance.isEmpty());
        assertTrue(guidance.get(0).contains("head pressure"));
        assertTrue(guidance.get(2).contains("main line layout"));
        assertFalse(guidance.get(0).contains("container vetting"));
        assertTrue(profile.preferredContextItems() >= 4);
        assertTrue(profile.promptExcerptChars() >= 420);
    }

    @Test
    public void waterStorageSystemPromptsPrioritizeDistributionSpecAheadOfContainerLane() {
        String query = "water distribution storage tanks gravity fed system";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        assertFalse(specs.isEmpty());
        int distributionIndex = -1;
        int tankIndex = -1;
        int containerIndex = -1;
        for (int index = 0; index < specs.size(); index++) {
            String text = specs.get(index).text();
            if (distributionIndex < 0 && text.contains("water system design distribution")) {
                distributionIndex = index;
            }
            if (tankIndex < 0 && text.contains("storage tank cistern")) {
                tankIndex = index;
            }
            if (containerIndex < 0 && text.contains("safe water storage container sanitation")) {
                containerIndex = index;
            }
        }

        assertTrue(distributionIndex >= 0);
        assertTrue(tankIndex >= 0);
        assertTrue(containerIndex >= 0);
        assertTrue(distributionIndex < containerIndex);
        assertTrue(tankIndex < containerIndex);
    }

    @Test
    public void explicitWaterDistributionSystemPromptsAddCommunityDistributionSpec() {
        String query = "how do i design a gravity-fed water distribution system";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawCommunityDistributionSpec = false;
        int communityIndex = -1;
        int containerIndex = -1;
        for (int index = 0; index < specs.size(); index++) {
            String text = specs.get(index).text();
            if (communityIndex < 0 && text.contains("community water distribution systems gravity fed")) {
                communityIndex = index;
                sawCommunityDistributionSpec = true;
            }
            if (containerIndex < 0 && text.contains("safe water storage container sanitation")) {
                containerIndex = index;
            }
        }

        assertTrue(sawCommunityDistributionSpec);
        assertTrue(containerIndex >= 0);
        assertTrue(communityIndex < containerIndex);
    }


    @Test
    public void explicitWaterDistributionSystemPromptUsesWaterStorageRouting() {
        String query = "how do i design a gravity-fed water distribution system";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawDistributionSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("water system design distribution")) {
                sawDistributionSpec = true;
                break;
            }
        }

        assertTrue(profile.isRouteFocused());
        assertFalse(profile.usesCompactGuideSweep(query));
        assertTrue(sawDistributionSpec);
    }

    @Test
    public void treatedWaterStorageTankQueryKeepsStoragePromptGuidance() {
        String query = "what's the safest way to store treated water long term in a storage tank";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<String> guidance = profile.promptGuidanceLines();

        assertFalse(guidance.isEmpty());
        assertTrue(guidance.get(0).contains("container vetting"));
        assertFalse(guidance.get(0).contains("head pressure"));
        assertEquals(3, profile.preferredContextItems());
        assertEquals(320, profile.promptExcerptChars());
    }

    @Test
    public void sanitationPromptAcceptsLatrineDesignGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I build a latrine for 50 people that won't contaminate our water?"
        );

        boolean supported = profile.supportsRouteResult(
            "sanitation and waste management systems",
            "latrine and toilet system design",
            "building",
            "",
            "latrine siting, contamination control, and maintenance",
            "keep latrines downhill from water sources and manage handwashing and waste flow"
        );

        assertTrue(profile.isRouteFocused());
        assertTrue(supported);
    }

    @Test
    public void messageAuthenticationPromptAcceptsCourierProtocolGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "What is the simplest way to prove a note is real without making the system too fragile?"
        );

        boolean supported = profile.supportsRouteResult(
            "message authentication & courier protocols",
            "chain of custody and documentation",
            "society",
            "",
            "tamper evidence, courier handoff discipline, and challenge-response",
            "use seals, check words, and chain-of-custody logs for urgent notes"
        );

        assertTrue(profile.isRouteFocused());
        assertTrue(supported);
    }

    @Test
    public void messageAuthenticationRouteSpecsSearchCommunicationsFamily() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "We run couriers between camps. What is the simplest way to prove a note is real?"
        );
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(
            "We run couriers between camps. What is the simplest way to prove a note is real?"
        );

        boolean sawCommunications = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.categories().contains("communications")) {
                sawCommunications = true;
                break;
            }
        }

        assertTrue(profile.isRouteFocused());
        assertTrue(sawCommunications);
    }

    @Test
    public void soapmakingPromptAcceptsHomesteadChemistryGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        boolean supported = profile.supportsRouteResult(
            "homestead chemistry",
            "soap making - cold process",
            "crafts",
            "",
            "soap making, lye safety, and curing",
            "render fat, prepare lye water, mix to trace, and cure the bars fully before use"
        );

        assertTrue(profile.isRouteFocused());
        assertTrue(supported);
    }

    @Test
    public void soapmakingPromptRejectsGenericChemistrySafetyGuide() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        boolean supported = profile.supportsRouteResult(
            "chemical & fuel salvage safety",
            "overview",
            "chemistry",
            "soapmaking",
            "safe siphoning, storage, and identification of salvaged fuels and chemicals",
            "cleaning products, paints, solvents, lubricants, and fertilizers with broad chemical safety guidance"
        );

        assertTrue(profile.isRouteFocused());
        assertFalse(supported);
    }

    @Test
    public void soapmakingPromptRejectsGenericPHChemistrySections() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        boolean supported = profile.supportsRouteResult(
            "pH, acids & bases: water chemistry essentials",
            "understanding bases",
            "chemistry",
            "",
            "caustic basics, pH scale, and handling corrosive materials",
            "bases can burn skin and eyes, so use gloves and rinse with water after contact"
        );

        assertTrue(profile.isRouteFocused());
        assertFalse(supported);
    }

    @Test
    public void soapmakingRouteSpecsStayInCraftsAndChemistry() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        boolean sawCraftsSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("cold process soap rendered fat")) {
                sawCraftsSpec = true;
                assertTrue(spec.categories().contains("crafts"));
                assertTrue(spec.categories().contains("chemistry"));
            }
        }

        assertTrue(profile.isRouteFocused());
        assertTrue(sawCraftsSpec);
    }

    @Test
    public void soapmakingScoringDoesNotPunishHomesteadChemistryForCandleMention() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "How do I make soap from animal fat safely enough that it's actually useful?"
        );

        int homesteadScore = profile.metadataBonus(
            "homestead chemistry",
            "soap making - cold process",
            "crafts",
            "",
            "soap making, candle making, lye safety, and curing",
            "render fat, prepare lye water, mix to trace, and cure the bars fully before use"
        );
        int genericChemistryScore = profile.metadataBonus(
            "chemical safety",
            "overview",
            "chemistry",
            "",
            "general chemical handling and caustic safety basics",
            "basic chemistry safety overview and storage compatibility guidance"
        );

        assertTrue(homesteadScore > genericChemistryScore);
    }

    @Test
    public void glassmakingPromptAcceptsGlassGuideGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i make glass");

        boolean supported = profile.supportsRouteResult(
            "glass, optics & ceramics",
            "forming techniques",
            "crafts",
            "",
            "raw materials, furnace construction, forming, and annealing",
            "melt silica with flux, shape the melt, and anneal slowly to reduce internal stress"
        );

        assertTrue(profile.isRouteFocused());
        assertTrue(supported);
    }

    @Test
    public void communitySecurityPromptUsesDedicatedRouting() {
        String query = "how do i protect a vulnerable work site, field, or water point without spreading people too thin?";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawSecuritySpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("community defense physical security")) {
                sawSecuritySpec = true;
                break;
            }
        }

        assertTrue(profile.isRouteFocused());
        assertTrue(profile.prefersDiversifiedAnswerContext());
        assertTrue(profile.preferredContextItems() >= 4);
        assertTrue(sawSecuritySpec);
    }

    @Test
    public void communitySecurityPromptAcceptsPhysicalSecurityGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "how do i protect a vulnerable work site, field, or water point without spreading people too thin?"
        );

        boolean supported = profile.supportsRouteResult(
            "critical infrastructure physical security",
            "water system security",
            "defense",
            "community_security,resource_security",
            "guard rotation, access control, and low-tech perimeter detection",
            "protect water points and food storage with observation, barriers, and overlapping watch coverage"
        );

        assertTrue(supported);
    }

    @Test
    public void communityGovernancePromptUsesDedicatedRouting() {
        String query = "someone is stealing food from the group what do we do";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawGovernanceSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("commons management sustainable resource governance")) {
                sawGovernanceSpec = true;
                break;
            }
        }

        assertTrue(profile.isRouteFocused());
        assertTrue(profile.prefersDiversifiedAnswerContext());
        assertTrue(sawGovernanceSpec);
    }

    @Test
    public void communityGovernancePromptAcceptsCommonsGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(
            "someone is stealing food from the group what do we do"
        );

        boolean supported = profile.supportsRouteResult(
            "commons management & sustainable resource governance",
            "monitoring and graduated sanctions",
            "society",
            "community_governance,conflict_resolution",
            "resource rules, monitoring, restitution, and proportional sanctions",
            "verify facts, document the loss, apply restitution, and use graduated sanctions to protect the commons"
        );

        assertTrue(supported);
    }

    @Test
    public void communityGovernancePromptWithFormalPunishmentStillUsesDedicatedRouting() {
        String query = "what if someone keeps stealing but nobody trusts formal punishment";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawGovernanceSpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("commons management sustainable resource governance")) {
                sawGovernanceSpec = true;
                break;
            }
        }

        assertTrue(profile.isRouteFocused());
        assertTrue(profile.prefersDiversifiedAnswerContext());
        assertTrue(sawGovernanceSpec);
    }

    @Test
    public void clayOvenPromptUsesDedicatedRouting() {
        String query = "how do i build a clay oven";
        QueryRouteProfile profile = QueryRouteProfile.fromQuery(query);
        List<QueryRouteProfile.RouteSearchSpec> specs = profile.routeSearchSpecs(query);

        boolean sawClaySpec = false;
        for (QueryRouteProfile.RouteSearchSpec spec : specs) {
            if (spec.text().contains("clay bread oven construction")) {
                sawClaySpec = true;
                break;
            }
        }

        assertTrue(profile.isRouteFocused());
        assertTrue(profile.prefersDiversifiedAnswerContext());
        assertTrue(sawClaySpec);
    }

    @Test
    public void clayOvenPromptAcceptsBreadOvenGuidance() {
        QueryRouteProfile profile = QueryRouteProfile.fromQuery("how do i build a clay oven");

        boolean supported = profile.supportsRouteResult(
            "clay bread oven construction",
            "cob oven construction",
            "building",
            "clay_oven,masonry_hearth",
            "site selection, foundation, hearth construction, and curing guidance",
            "build the oven body over a stable hearth, dry it slowly, and cure before full firing"
        );

        assertTrue(supported);
    }
}

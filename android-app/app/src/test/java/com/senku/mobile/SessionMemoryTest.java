package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public final class SessionMemoryTest {
    @Test
    public void vagueFollowUpUsesRetrievalContextFromLatestTurn() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i build a house",
            "Short answer: Start with site assessment and drainage before choosing a foundation.",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus"
                )
            )
        );

        String retrievalQuery = memory.contextualizeForRetrieval("what about drainage");

        assertTrue(memory.shouldUseContext("what about drainage"));
        assertTrue(retrievalQuery.startsWith("drainage"));
        assertTrue(retrievalQuery.contains("foundation"));
        assertFalse(retrievalQuery.contains("recent question:"));
        assertFalse(retrievalQuery.contains("[GD-094]"));
    }

    @Test
    public void specificStandaloneQuestionStaysStandalone() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "what's the safest way to store treated water long term",
            "Short answer: Use known food-safe containers and rotate them.",
            List.of(
                new SearchResult(
                    "Storage & Material Management",
                    "",
                    "",
                    "",
                    "GD-252",
                    "Water Storage: Hydration Assurance",
                    "resource-management",
                    "guide-focus"
                )
            )
        );

        String standalone = "how do i build a kiln for pottery from local clay and brick";

        assertFalse(memory.shouldUseContext(standalone));
        assertTrue(memory.renderPromptContext(standalone).isEmpty());
    }

    @Test
    public void promptContextKeepsRicherSummaryForVagueFollowUps() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i waterproof a roof with no tar or shingles",
            "Short answer: Use overlapping bark, shakes, or thatch and prioritize water shedding.",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Roofing Systems",
                    "building",
                    "guide-focus"
                ),
                new SearchResult(
                    "Insulation & Weatherproofing",
                    "",
                    "",
                    "",
                    "GD-106",
                    "Core Principles of Thermal Efficiency",
                    "building",
                    "route-focus"
                )
            )
        );

        String promptContext = memory.renderPromptContext("what next");

        assertTrue(memory.shouldUseContext("what next"));
        assertTrue(promptContext.contains("recent questions:"));
        assertTrue(promptContext.contains("latest answer:"));
        assertTrue(promptContext.contains("[GD-094] Construction & Carpentry"));
    }

    @Test
    public void vagueUsingFollowUpKeepsOnlyMeaningfulRetrievalTerms() {
        SessionMemory memory = new SessionMemory();
        SearchResult tallowGuide = new SearchResult(
            "Animal Fat Rendering and Tallow Uses",
            "",
            "",
            "",
            "GD-486",
            "Tallow Candles",
            "fire",
            "guide-focus"
        );
        memory.recordTurn(
            "how do i make candles for light",
            "Short answer: Tallow is the fallback when beeswax is not available.",
            List.of(
                new SearchResult(
                    "Lighting Production",
                    "",
                    "",
                    "",
                    "GD-122",
                    "Emergency Lighting",
                    "fire",
                    "guide-focus"
                ),
                tallowGuide
            )
        );

        String retrievalQuery = memory.contextualizeForRetrieval("what about using tallow");

        assertTrue(retrievalQuery.startsWith("tallow"));
        assertFalse(retrievalQuery.contains("what about using tallow"));
        assertTrue(retrievalQuery.contains("candles"));
        assertFalse(retrievalQuery.contains("[GD-486] Animal Fat Rendering and Tallow Uses"));
        assertEquals("GD-486", memory.recentSourceResults().get(1).guideId);
    }

    @Test
    public void specificFollowUpDropsRecentGuideLabelsFromRetrievalContext() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i build a house",
            "Short answer: Start with site assessment and drainage before choosing a foundation.",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus"
                ),
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "",
                    "",
                    "GD-446",
                    "Terrain Analysis",
                    "building",
                    "route-focus"
                )
            )
        );

        String retrievalQuery = memory.contextualizeForRetrieval("what about sealing the roof");

        assertTrue(retrievalQuery.startsWith("sealing roof"));
        assertTrue(retrievalQuery.contains("drainage"));
        assertFalse(retrievalQuery.contains("recent question:"));
        assertFalse(retrievalQuery.contains("recent guides:"));
    }

    @Test
    public void houseDrainageFollowUpDropsOffGridSectionNoise() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i build a house",
            "Short answer: Use a stable foundation and good drainage before framing.",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Outbuildings for Off-Grid Living",
                    "building",
                    "guide-focus",
                    "starter",
                    "long_term",
                    "cabin_house",
                    "foundation,wall_construction,roofing,ventilation"
                )
            )
        );

        String retrievalQuery = memory.contextualizeForRetrieval("what about drainage");

        assertTrue(retrievalQuery.startsWith("drainage"));
        assertFalse(retrievalQuery.contains("outbuildings"));
        assertFalse(retrievalQuery.contains("off-grid"));
        assertTrue(retrievalQuery.contains("foundation"));
    }

    @Test
    public void recentSourceResultsReturnsDefensiveCopy() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i make candles for light",
            "Short answer: Tallow is the fallback when beeswax is not available.",
            List.of(
                new SearchResult(
                    "Animal Fat Rendering and Tallow Uses",
                    "",
                    "",
                    "",
                    "GD-486",
                    "Tallow Candles",
                    "fire",
                    "guide-focus"
                )
            )
        );

        List<SearchResult> recentSources = memory.recentSourceResults();
        recentSources.clear();

        assertEquals(1, memory.recentSourceResults().size());
        assertEquals("GD-486", memory.recentSourceResults().get(0).guideId);
    }

    @Test
    public void recordTurnPersistsLatencyBreakdownFromGeneratedAnswerRun() {
        SessionMemory memory = new SessionMemory();
        OfflineAnswerEngine.AnswerRun answerRun = new OfflineAnswerEngine.AnswerRun(
            "how do i improvise a rain shelter",
            "Short answer: Use an A-frame when you can keep one side low to the wind.",
            List.of(
                new SearchResult(
                    "Emergency Shelter",
                    "",
                    "",
                    "",
                    "GD-001",
                    "Lean-To",
                    "survival",
                    "guide-focus"
                )
            ),
            412L,
            false,
            false,
            false,
            "Offline answer | instant",
            "",
            new OfflineAnswerEngine.LatencyBreakdown(
                "practical how-to",
                88L,
                11L,
                19L,
                57L,
                294L,
                412L
            )
        );

        OfflineAnswerEngine.rememberSessionLatencyBreakdownForTest(answerRun);
        memory.recordTurn(answerRun.query, answerRun.answerBody, answerRun.sources, answerRun.ruleId);

        SessionMemory.TurnSnapshot snapshot = memory.latestTurnSnapshot();
        assertEquals(88L, snapshot.latencyBreakdown.retrievalMs);
        assertEquals(11L, snapshot.latencyBreakdown.rerankMs);
        assertEquals(19L, snapshot.latencyBreakdown.promptBuildMs);
        assertEquals(294L, snapshot.latencyBreakdown.decodeMs);

        SessionMemory restored = SessionMemory.fromJson(memory.toJson());
        SessionMemory.TurnSnapshot restoredSnapshot = restored.latestTurnSnapshot();
        assertEquals(88L, restoredSnapshot.latencyBreakdown.retrievalMs);
        assertEquals(11L, restoredSnapshot.latencyBreakdown.rerankMs);
        assertEquals(19L, restoredSnapshot.latencyBreakdown.promptBuildMs);
        assertEquals(294L, restoredSnapshot.latencyBreakdown.decodeMs);
    }

    @Test
    public void recordTurnPersistsReviewedCardMetadataThroughJsonRoundTrip() {
        SessionMemory memory = new SessionMemory();
        ReviewedCardMetadata metadata = new ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of("GD-898", "GD-898")
        );

        memory.recordTurn(
            "my child swallowed an unknown cleaner",
            "1. Call poison control.\nSources: [GD-898]",
            List.of(new SearchResult(
                "Poisoning",
                "",
                "Triage",
                "Triage",
                "GD-898",
                "Triage",
                "medical",
                "answer-card"
            )),
            "answer_card:poisoning_unknown_ingestion",
            metadata
        );

        SessionMemory.TurnSnapshot snapshot = memory.latestTurnSnapshot();
        assertTrue(snapshot.reviewedCardMetadata.isPresent());
        assertEquals("poisoning_unknown_ingestion", snapshot.reviewedCardMetadata.cardId);
        assertEquals("GD-898", snapshot.reviewedCardMetadata.cardGuideId);
        assertEquals("pilot_reviewed", snapshot.reviewedCardMetadata.reviewStatus);
        assertEquals("reviewed_source_family", snapshot.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(List.of("GD-898"), snapshot.reviewedCardMetadata.citedReviewedSourceGuideIds);

        SessionMemory restored = SessionMemory.fromJson(memory.toJson());
        SessionMemory.TurnSnapshot restoredSnapshot = restored.latestTurnSnapshot();
        assertEquals("poisoning_unknown_ingestion", restoredSnapshot.reviewedCardMetadata.cardId);
        assertEquals("GD-898", restoredSnapshot.reviewedCardMetadata.cardGuideId);
        assertEquals("pilot_reviewed", restoredSnapshot.reviewedCardMetadata.reviewStatus);
        assertEquals(
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            restoredSnapshot.reviewedCardMetadata.provenance
        );
        assertEquals(List.of("GD-898"), restoredSnapshot.reviewedCardMetadata.citedReviewedSourceGuideIds);

        SessionMemory noMetadata = new SessionMemory();
        noMetadata.recordTurn("plain question", "plain answer", List.of());
        assertFalse(noMetadata.latestTurnSnapshot().reviewedCardMetadata.isPresent());
        assertFalse(SessionMemory.fromJson(noMetadata.toJson()).latestTurnSnapshot()
            .reviewedCardMetadata.isPresent());
    }

    @Test
    public void fromJsonMigratesVersionTwoSessionWithoutReviewedCardFields() {
        SessionMemory memory = SessionMemory.fromJson(
            "{\n" +
                "  \"version\": 2,\n" +
                "  \"sticky_anchor_guide_id\": \"GD-444\",\n" +
                "  \"turns\": [\n" +
                "    {\n" +
                "      \"question\": \"how do i build a rain shelter\",\n" +
                "      \"answer_summary\": \"Keep it simple and waterproof.\",\n" +
                "      \"answer_body\": \"Short answer: pitch a tarp low to the wind.\",\n" +
                "      \"sources\": [\"[GD-444] Tarp Shelters\"],\n" +
                "      \"source_results\": [\n" +
                "        {\n" +
                "          \"title\": \"Tarp Shelters\",\n" +
                "          \"subtitle\": \"Field shelter\",\n" +
                "          \"snippet\": \"Use a ridge line and drainage.\",\n" +
                "          \"body\": \"Stake the tarp securely.\",\n" +
                "          \"guide_id\": \"GD-444\",\n" +
                "          \"section_heading\": \"Ridge Line Setup\",\n" +
                "          \"category\": \"survival\",\n" +
                "          \"retrieval_mode\": \"guide-focus\",\n" +
                "          \"content_role\": \"starter\",\n" +
                "          \"time_horizon\": \"immediate\",\n" +
                "          \"structure_type\": \"tarp_shelter\",\n" +
                "          \"topic_tags\": \"rain_shelter,drainage\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"rule_id\": \"deterministic:shelter\",\n" +
                "      \"recorded_at_epoch_ms\": 1714244042000\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        );

        assertEquals(1, memory.turnCount());
        SessionMemory.TurnSnapshot snapshot = memory.latestTurnSnapshot();
        assertEquals("how do i build a rain shelter", snapshot.question);
        assertEquals("Short answer: pitch a tarp low to the wind.", snapshot.answerBody);
        assertEquals("deterministic:shelter", snapshot.ruleId);
        assertEquals(1714244042000L, snapshot.recordedAtEpochMs);
        assertEquals("GD-444", snapshot.sourceResults.get(0).guideId);
        assertEquals("Ridge Line Setup", snapshot.sourceResults.get(0).sectionHeading);
        assertEquals("guide-focus", snapshot.sourceResults.get(0).retrievalMode);
        assertFalse(snapshot.reviewedCardMetadata.isPresent());
        assertEquals(null, snapshot.latencyBreakdown);

        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what next");
            assertEquals("GD-444", plan.anchorPrior.anchorGuideId);
            assertEquals(1, plan.anchorPrior.turnCount);
        } finally {
            SessionMemory.setAnchorPriorEnabledForTest(false);
        }
    }

    @Test
    public void genericFollowUpFallsBackToTopicHintsWhenNoMeaningfulTermsExist() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "what's the safest way to store treated water long term",
            "Short answer: Use known food-safe containers and rotate them.",
            List.of(
                new SearchResult(
                    "Storage & Material Management",
                    "",
                    "",
                    "",
                    "GD-252",
                    "Water Storage: Hydration Assurance",
                    "resource-management",
                    "guide-focus",
                    "safety",
                    "long_term",
                    "water_storage",
                    "water_storage,container_sanitation,water_rotation"
                )
            )
        );

        String retrievalQuery = memory.contextualizeForRetrieval("what next");

        assertTrue(retrievalQuery.contains("water"));
        assertTrue(retrievalQuery.contains("storage"));
        assertTrue(retrievalQuery.contains("container"));
    }

    @Test
    public void specificWaterFollowUpStaysInWaterStorageLane() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "what's the safest way to store treated water long term",
            "Short answer: Use known food-safe containers and rotate them.",
            List.of(
                new SearchResult(
                    "Storage & Material Management",
                    "",
                    "",
                    "",
                    "GD-252",
                    "Water Storage: Hydration Assurance",
                    "resource-management",
                    "guide-focus",
                    "safety",
                    "long_term",
                    "general",
                    "weatherproofing,water_rotation"
                ),
                new SearchResult(
                    "Rationing & Equitable Distribution",
                    "",
                    "",
                    "",
                    "GD-386",
                    "Water Rationing Protocols",
                    "survival",
                    "route-focus",
                    "safety",
                    "long_term",
                    "water_storage",
                    "water_storage,water_rotation"
                )
            )
        );

        String retrievalQuery = memory.contextualizeForRetrieval("can i use old soda bottles");

        assertTrue(retrievalQuery.startsWith("old soda bottles"));
        assertTrue(retrievalQuery.contains("water"));
        assertTrue(retrievalQuery.contains("storage"));
        assertTrue(retrievalQuery.contains("container"));
        assertFalse(retrievalQuery.contains("weatherproofing"));
    }

    @Test
    public void retrievalPlanUsesRawSearchForSpecificWaterFollowUp() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "what's the safest way to store treated water long term",
            "Short answer: Use known food-safe containers and rotate them.",
            List.of(
                new SearchResult(
                    "Storage & Material Management",
                    "",
                    "",
                    "",
                    "GD-252",
                    "Water Storage: Hydration Assurance",
                    "resource-management",
                    "guide-focus",
                    "safety",
                    "long_term",
                    "water_storage",
                    "water_storage,container_sanitation,water_rotation"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("can i use old soda bottles");

        assertTrue(plan.sessionUsed);
        assertEquals("can i use old soda bottles", plan.searchQuery);
        assertTrue(plan.contextSelectionQuery.startsWith("old soda bottles"));
        assertTrue(plan.contextSelectionQuery.contains("water"));
        assertTrue(plan.contextSelectionQuery.contains("storage"));
        assertTrue(plan.deterministicFallbackQuery.contains("container"));
    }

    @Test
    public void retrievalPlanKeepsContainerSignalForVagueWaterFollowUp() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "what's the safest way to store treated water long term",
            "Short answer: Use known food-safe containers and rotate them.",
            List.of(
                new SearchResult(
                    "Storage & Material Management",
                    "",
                    "",
                    "",
                    "GD-252",
                    "Water Storage: Hydration Assurance",
                    "resource-management",
                    "guide-focus",
                    "safety",
                    "long_term",
                    "water_storage",
                    "water_storage,container_sanitation,water_rotation"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what next");

        assertTrue(plan.sessionUsed);
        assertTrue(plan.contextSelectionQuery.contains("water"));
        assertTrue(plan.contextSelectionQuery.contains("storage"));
        assertTrue(plan.contextSelectionQuery.contains("container"));
        assertTrue(plan.contextSelectionQuery.contains("sanitation"));
    }

    @Test
    public void retrievalPlanUsesStructuredSearchForVagueHouseFollowUp() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i build a house",
            "Short answer: Start with site assessment and drainage before choosing a foundation.",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus",
                    "starter",
                    "long_term",
                    "cabin_house",
                    "foundation,wall_construction,roofing,ventilation"
                ),
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "",
                    "",
                    "GD-446",
                    "Terrain Analysis",
                    "building",
                    "route-focus",
                    "planning",
                    "long_term",
                    "cabin_house",
                    "site_selection,drainage,foundation"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about drainage");

        assertTrue(plan.sessionUsed);
        assertEquals(plan.contextSelectionQuery, plan.searchQuery);
        assertTrue(plan.contextSelectionQuery.startsWith("drainage"));
        assertTrue(plan.contextSelectionQuery.contains("cabin"));
        assertTrue(plan.contextSelectionQuery.contains("house"));
        assertTrue(plan.contextSelectionQuery.contains("foundation"));
        assertTrue(plan.contextSelectionQuery.contains("site"));
        assertFalse(plan.contextSelectionQuery.contains("roofing"));
        assertFalse(plan.contextSelectionQuery.contains("wall construction"));
        assertFalse(plan.contextSelectionQuery.contains("ventilation"));
        assertFalse(plan.contextSelectionQuery.contains("weatherproofing"));
    }

    @Test
    public void retrievalPlanKeepsSiteSelectionHintsForDrainageRunoffFollowUp() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i choose a safe site and foundation for a small cabin",
            "Short answer: Start by checking terrain, drainage, and frost-safe foundations.",
            List.of(
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "",
                    "",
                    "GD-446",
                    "Terrain Analysis",
                    "survival",
                    "route-focus",
                    "safety",
                    "long_term",
                    "cabin_house",
                    "site_selection,drainage,foundation"
                ),
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Foundations",
                    "building",
                    "route-focus",
                    "starter",
                    "long_term",
                    "cabin_house",
                    "foundation,drainage,wall_construction"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about drainage and runoff?");

        assertTrue(plan.sessionUsed);
        assertTrue(plan.searchQuery.startsWith("drainage runoff"));
        assertTrue(plan.searchQuery.contains("foundation"));
        assertTrue(plan.searchQuery.contains("site"));
        assertTrue(plan.searchQuery.contains("selection"));
        assertFalse(plan.searchQuery.startsWith("drainage runoff cabin house"));
    }

    @Test
    public void retrievalPlanUsesStructuredSearchForWinterSunSiteFollowUp() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "How do I choose a building site if drainage, wind, sun, and access all matter?",
            "Short answer: Start with terrain, wind exposure, and drainage.",
            List.of(
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "",
                    "",
                    "GD-446",
                    "Wind Exposure and Microclimate",
                    "survival",
                    "route-focus",
                    "safety",
                    "long_term",
                    "cabin_house",
                    "site_selection,drainage"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about winter sun and summer shade?");

        assertTrue(plan.sessionUsed);
        assertEquals("context=" + plan.contextSelectionQuery + " search=" + plan.searchQuery, plan.contextSelectionQuery, plan.searchQuery);
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("winter"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("sun"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("shade"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("site"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("selection"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("microclimate"));
        assertFalse("search=" + plan.searchQuery, plan.searchQuery.contains("foundation"));
    }

    @Test
    public void retrievalPlanUsesStructuredSearchForSmallCabinWinterSunFollowUp() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i choose a safe site and foundation for a small cabin",
            "Short answer: Start with terrain, drainage, and microclimate checks before building.",
            List.of(
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "",
                    "",
                    "GD-446",
                    "Wind Exposure and Microclimate",
                    "survival",
                    "route-focus",
                    "safety",
                    "long_term",
                    "cabin_house",
                    "site_selection,drainage,foundation"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about winter sun and summer shade");

        assertTrue(plan.sessionUsed);
        assertEquals(plan.contextSelectionQuery, plan.searchQuery);
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("winter"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("sun"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("shade"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("site"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("selection"));
        assertTrue("search=" + plan.searchQuery, plan.searchQuery.contains("microclimate"));
    }

    @Test
    public void retrievalPlanKeepsTallowFollowUpTightToCandleContext() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i make candles for light",
            "Short answer: Tallow is the fallback when beeswax is not available.",
            List.of(
                new SearchResult(
                    "Lighting Production",
                    "",
                    "",
                    "",
                    "GD-122",
                    "Emergency Lighting",
                    "fire",
                    "guide-focus"
                ),
                new SearchResult(
                    "Animal Fat Rendering and Tallow Uses",
                    "",
                    "",
                    "",
                    "GD-486",
                    "Tallow Candles",
                    "fire",
                    "guide-focus"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about using tallow");

        assertTrue(plan.sessionUsed);
        assertEquals("tallow candles", plan.searchQuery);
        assertEquals("tallow candles", plan.contextSelectionQuery);
        assertFalse(plan.searchQuery.contains("fuel"));
    }

    @Test
    public void retrievalPlanUsesStructuredSearchForTwoTokenWaterDistributionFollowUp() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i design a gravity-fed water distribution system",
            "Short answer: Start with elevation, flow path, and storage planning.",
            List.of(
                new SearchResult(
                    "Water System Design and Distribution",
                    "",
                    "",
                    "",
                    "GD-553",
                    "Gravity-Fed Distribution Systems",
                    "building",
                    "guide-focus",
                    "planning",
                    "long_term",
                    "water_storage",
                    "water_storage,water_distribution"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about storage tanks");

        assertTrue(plan.sessionUsed);
        assertEquals(plan.contextSelectionQuery, plan.searchQuery);
        assertTrue(plan.searchQuery.startsWith("storage tanks water distribution"));
        assertTrue(plan.searchQuery.contains("storage"));
        assertTrue(plan.searchQuery.contains("water"));
        assertTrue(plan.searchQuery.contains("distribution"));
        assertFalse(plan.searchQuery.contains("container sanitation"));
        assertFalse(plan.searchQuery.contains("rotation"));
    }

    @Test
    public void recentTurnSnapshotsReturnLatestTurnsInOrder() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn("first question", "Short answer: First.", List.of());
        memory.recordTurn("second question", "Short answer: Second.", List.of());

        List<SessionMemory.TurnSnapshot> snapshots = memory.recentTurnSnapshots(4);

        assertEquals(2, snapshots.size());
        assertEquals("first question", snapshots.get(0).question);
        assertEquals("second question", snapshots.get(1).question);
    }

    @Test
    public void maxTurnCapDropsOldestTurnsAndKeepsNewestRetrievalContext() {
        SessionMemory memory = new SessionMemory();
        memory.recordTranscriptFixtureTurnForTest(
            "turn 1: how do i make candles",
            "Short answer: Render tallow.",
            "Short answer: Render tallow.",
            List.of(testSource("Tallow Candles", "GD-101", "Tallow", "fire", "guide-focus", "tallow,candles")),
            "",
            1714244001000L
        );
        memory.recordTranscriptFixtureTurnForTest(
            "turn 2: how do i choose a cabin site",
            "Short answer: Start with drainage.",
            "Short answer: Start with drainage.",
            List.of(testSource("Cabin Site Selection", "GD-102", "Drainage", "building", "guide-focus", "site_selection,drainage")),
            "",
            1714244002000L
        );
        memory.recordTranscriptFixtureTurnForTest(
            "turn 3: how do i frame a wall",
            "Short answer: Keep it plumb.",
            "Short answer: Keep it plumb.",
            List.of(testSource("Wall Framing", "GD-103", "Framing", "building", "guide-focus", "wall_construction")),
            "",
            1714244003000L
        );
        memory.recordTranscriptFixtureTurnForTest(
            "turn 4: how do i seal a roof",
            "Short answer: Overlap roofing layers.",
            "Short answer: Overlap roofing layers.",
            List.of(testSource("Roof Sealing", "GD-104", "Roofing", "building", "guide-focus", "roofing,weatherproofing")),
            "",
            1714244004000L
        );
        memory.recordTranscriptFixtureTurnForTest(
            "turn 5: how do i store grain",
            "Short answer: Keep it dry.",
            "Short answer: Keep it dry.",
            List.of(testSource("Grain Storage", "GD-105", "Dry Storage", "food", "guide-focus", "grain_storage")),
            "",
            1714244005000L
        );
        memory.recordTranscriptFixtureTurnForTest(
            "turn 6: how do i purify water",
            "Short answer: Boil or filter it.",
            "Short answer: Boil or filter it.",
            List.of(testSource("Water Purification", "GD-106", "Boiling", "survival", "guide-focus", "water_purification")),
            "",
            1714244006000L
        );
        memory.recordTranscriptFixtureTurnForTest(
            "turn 7: what's the safest way to store treated water long term",
            "Short answer: Use food-safe containers and rotate them.",
            "Short answer: Use food-safe containers and rotate them.",
            List.of(testSource(
                "Storage & Material Management",
                "GD-252",
                "Water Storage: Hydration Assurance",
                "resource-management",
                "guide-focus",
                "water_storage,container_sanitation,water_rotation"
            )),
            "",
            1714244007000L
        );

        List<SessionMemory.TurnSnapshot> snapshots = memory.recentTurnSnapshots(10);
        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what next");

        assertEquals(6, memory.turnCount());
        assertEquals(6, snapshots.size());
        assertEquals("turn 2: how do i choose a cabin site", snapshots.get(0).question);
        assertEquals("turn 7: what's the safest way to store treated water long term", snapshots.get(5).question);
        assertEquals("GD-252", memory.recentSourceResults().get(0).guideId);
        assertTrue(plan.sessionUsed);
        assertTrue(plan.contextSelectionQuery.contains("water"));
        assertTrue(plan.contextSelectionQuery.contains("storage"));
        assertTrue(plan.contextSelectionQuery.contains("container"));
        assertFalse(plan.promptContext.contains("turn 1: how do i make candles"));
    }

    @Test
    public void clayOvenFollowUpCarriesClayOvenContext() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do i build a clay oven",
            "Short answer: Start with site selection, a stable hearth, and slow curing.",
            List.of(
                new SearchResult(
                    "Clay Bread Oven Construction",
                    "",
                    "",
                    "",
                    "GD-505",
                    "Cob Oven Construction",
                    "building",
                    "guide-focus",
                    "planning",
                    "long_term",
                    "clay_oven",
                    "clay_oven,masonry_hearth"
                )
            )
        );

        SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about curing");

        assertTrue(plan.sessionUsed);
        assertTrue(plan.searchQuery.contains("curing"));
        assertTrue(plan.searchQuery.contains("clay"));
        assertTrue(plan.searchQuery.contains("oven"));
        assertTrue(plan.searchQuery.contains("masonry") || plan.searchQuery.contains("hearth"));
    }

    @Test
    public void anchorPriorFollowUpBuildsTypedDirectiveWhenEnabled() {
        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            SessionMemory memory = new SessionMemory();
            memory.recordTurn(
                "how do i build a rain shelter",
                "Short answer: Keep it simple and waterproof.",
                List.of(
                    new SearchResult(
                        "Tarp Shelters",
                        "",
                        "",
                        "",
                        "GD-444",
                        "Ridge Line Setup",
                        "survival",
                        "guide-focus"
                    )
                )
            );

            SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what should i do next?");

            assertTrue(memory.isLikelyFollowUp("what should i do next?"));
            assertEquals(plan.contextSelectionQuery, plan.searchQuery);
            assertEquals("shelter rain", plan.searchQuery);
            assertEquals("GD-444", plan.anchorPrior.anchorGuideId);
            assertEquals(0, plan.anchorPrior.turnsSinceAnchor);
            assertEquals(1, plan.anchorPrior.turnCount);
        } finally {
            SessionMemory.setAnchorPriorEnabledForTest(false);
        }
    }

    @Test
    public void explicitResetQuestionDisablesContextAndAnchorPriorEncoding() {
        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            SessionMemory memory = new SessionMemory();
            memory.recordTurn(
                "how do i build a rain shelter",
                "Short answer: Keep it simple and waterproof.",
                List.of(
                    new SearchResult(
                        "Tarp Shelters",
                        "",
                        "",
                        "",
                        "GD-444",
                        "Ridge Line Setup",
                        "survival",
                        "guide-focus"
                    )
                )
            );

            SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("unrelated: how do i purify water?");

            assertFalse(memory.shouldUseContext("unrelated: how do i purify water?"));
            assertFalse(memory.isLikelyFollowUp("unrelated: how do i purify water?"));
            assertEquals(null, plan.anchorPrior);
        } finally {
            SessionMemory.setAnchorPriorEnabledForTest(false);
        }
    }

    @Test
    public void subjectResetQuestionDoesNotEncodeAnchorPrior() {
        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            SessionMemory memory = new SessionMemory();
            memory.recordTurn(
                "how do i store treated water",
                "Short answer: Use safe containers.",
                List.of(
                    new SearchResult(
                        "Water Storage",
                        "",
                        "",
                        "",
                        "GD-252",
                        "Containers",
                        "resource-management",
                        "guide-focus"
                    )
                )
            );

            SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("how do i build a rain shelter?");

            assertFalse(memory.isLikelyFollowUp("how do i build a rain shelter?"));
            assertEquals(null, plan.anchorPrior);
        } finally {
            SessionMemory.setAnchorPriorEnabledForTest(false);
        }
    }

    @Test
    public void anchorPriorExpiresWhenAnchorIsThreeTurnsOld() {
        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            SessionMemory memory = new SessionMemory();
            memory.recordTurn(
                "how do i build a rain shelter",
                "Short answer: Use a ridge line.",
                List.of(
                    new SearchResult(
                        "Tarp Shelters",
                        "",
                        "",
                        "",
                        "GD-444",
                        "Ridge Line Setup",
                        "survival",
                        "guide-focus"
                    )
                )
            );
            memory.recordTurn("second turn", "Short answer: second.", List.of());
            memory.recordTurn("third turn", "Short answer: third.", List.of());
            memory.recordTurn("fourth turn", "Short answer: fourth.", List.of());

            SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what should i do next?");

            assertEquals(null, plan.anchorPrior);
        } finally {
            SessionMemory.setAnchorPriorEnabledForTest(false);
        }
    }

    @Test
    public void idleAnchorResetClearsStickyAnchorWithoutDroppingTranscript() {
        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            SessionMemory memory = new SessionMemory();
            memory.recordTranscriptFixtureTurnForTest(
                "how do i build a rain shelter",
                "Short answer: Use a ridge line.",
                "Short answer: Use a ridge line.",
                List.of(testSource("Tarp Shelters", "GD-444", "Ridge Line Setup", "survival", "guide-focus", "rain_shelter")),
                "",
                1714244000000L
            );

            boolean reset = memory.markAnchorIdleResetIfStale(
                1714244000000L + SessionMemory.anchorIdleResetMsForTest() + 1L
            );
            SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what should i do next?");

            assertTrue(reset);
            assertEquals(1, memory.turnCount());
            assertTrue(memory.renderTranscript().contains("how do i build a rain shelter"));
            assertEquals(null, plan.anchorPrior);
        } finally {
            SessionMemory.setAnchorPriorEnabledForTest(false);
        }
    }

    @Test
    public void singularizeKeepsDoubleSEndingsIntact() {
        assertEquals("glass", SessionMemory.singularizeForTest("glass"));
        assertEquals("stress", SessionMemory.singularizeForTest("stress"));
        assertEquals("battery", SessionMemory.singularizeForTest("batteries"));
    }

    @Test
    public void tokensMatchRejectsShortOrLoosePrefixCollisions() {
        assertFalse(SessionMemory.tokensMatchForTest("winter", "win"));
        assertFalse(SessionMemory.tokensMatchForTest("glass", "glas"));
        assertTrue(SessionMemory.tokensMatchForTest("seal", "sealing"));
        assertTrue(SessionMemory.tokensMatchForTest("drain", "drainage"));
    }

    private static SearchResult testSource(
        String title,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode,
        String topicTags
    ) {
        return new SearchResult(
            title,
            "",
            "",
            "",
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            "",
            "",
            "",
            topicTags
        );
    }
}

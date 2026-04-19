package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.senku.mobile.telemetry.LatencyPanel;

import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Test;

public final class OfflineAnswerEngineTest {
    @After
    public void resetGenerationAdapters() {
        OfflineAnswerEngine.resetGeneratorsForTest();
        OfflineAnswerEngine.resetDebugLogSinkForTest();
        LatencyPanel.resetLogSinkForTest();
    }

    @Test
    public void specificFollowUpFiltersOutUnrelatedRecentSources() {
        List<SearchResult> prioritized = OfflineAnswerEngine.prioritizeRecentSources(
            "what about sealing the roof",
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

        assertTrue(prioritized.isEmpty());
    }

    @Test
    public void specificFollowUpKeepsRelevantRecentSource() {
        List<SearchResult> prioritized = OfflineAnswerEngine.prioritizeRecentSources(
            "what about using tallow",
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

        assertEquals(1, prioritized.size());
        assertEquals("GD-486", prioritized.get(0).guideId);
    }

    @Test
    public void nonOverlappingFollowUpFallsBackToRecentSources() {
        List<SearchResult> prioritized = OfflineAnswerEngine.prioritizeRecentSources(
            "what about storage tanks",
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

        assertEquals(1, prioritized.size());
        assertEquals("GD-553", prioritized.get(0).guideId);
    }

    @Test
    public void genericNextFollowUpKeepsRecentSourcesAvailable() {
        List<SearchResult> prioritized = OfflineAnswerEngine.prioritizeRecentSources(
            "what next",
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

        assertEquals(2, prioritized.size());
    }

    @Test
    public void confidenceLabelReturnsHighForHybridGroundedMatch() {
        OfflineAnswerEngine.ConfidenceLabel label = OfflineAnswerEngine.confidenceLabel(
            List.of(
                new SearchResult(
                    "Cabin Roofing and Weatherproofing",
                    "",
                    "",
                    "",
                    "GD-410",
                    "Roof framing and rainproofing",
                    "building",
                    "hybrid",
                    "planning",
                    "long_term",
                    "cabin_house",
                    "roofing,weatherproofing"
                ),
                new SearchResult(
                    "Wall Construction",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Wall framing",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "wall_construction"
                )
            ),
            "how do i build a cabin roof that sheds rain",
            QueryMetadataProfile.fromQuery("how do i build a cabin roof that sheds rain")
        );

        assertEquals(OfflineAnswerEngine.ConfidenceLabel.HIGH, label);
    }

    @Test
    public void confidenceLabelReturnsMediumForRelatedButNonDominantMatch() {
        OfflineAnswerEngine.ConfidenceLabel label = OfflineAnswerEngine.confidenceLabel(
            List.of(
                new SearchResult(
                    "Tarp field notes",
                    "",
                    "",
                    "",
                    "GD-112",
                    "Quick patches",
                    "survival",
                    "hybrid",
                    "starter",
                    "immediate",
                    "emergency_shelter",
                    "tarp_shelter,repair"
                ),
                new SearchResult(
                    "Canvas Repair",
                    "",
                    "",
                    "",
                    "GD-102",
                    "Patch kits",
                    "crafts",
                    "vector"
                )
            ),
            "how do i repair a tarp shelter after wind damage",
            QueryMetadataProfile.fromQuery("how do i repair a tarp shelter after wind damage")
        );

        assertEquals(OfflineAnswerEngine.ConfidenceLabel.MEDIUM, label);
    }

    @Test
    public void confidenceLabelReturnsLowForWeakOffTopicMatches() {
        OfflineAnswerEngine.ConfidenceLabel label = OfflineAnswerEngine.confidenceLabel(
            List.of(
                new SearchResult(
                    "Canvas Repair",
                    "",
                    "",
                    "",
                    "GD-102",
                    "Patching",
                    "crafts",
                    "vector"
                ),
                new SearchResult(
                    "Tent Maintenance",
                    "",
                    "",
                    "",
                    "GD-103",
                    "Storage",
                    "resource-management",
                    "lexical"
                )
            ),
            "how do i build a rain shelter from a tarp",
            QueryMetadataProfile.fromQuery("how do i build a rain shelter from a tarp")
        );

        assertEquals(OfflineAnswerEngine.ConfidenceLabel.LOW, label);
    }

    @Test
    public void generateFallsBackToOnDeviceWhenHostFailsAndModelExists() throws Exception {
        File tempModel = File.createTempFile("senku-fallback", ".litertlm");
        tempModel.deleteOnExit();
        AtomicReference<String> fallbackStatus = new AtomicReference<>("");

        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new IllegalStateException("host unreachable");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                listener.onPartialText("Short answer: fallback path.");
                return "Short answer: fallback path.";
            }
        );

        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How do I improvise a rain shelter?",
            List.of(),
            false,
            System.currentTimeMillis() - 1500L,
            true,
            "http://127.0.0.1:9/v1",
            "gemma-4-e2b-it-litert",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(
            null,
            tempModel,
            prepared,
            new OfflineAnswerEngine.AnswerProgressListener() {
                @Override
                public void onAnswerBody(String partialAnswerBody) {
                }

                @Override
                public void onFallbackToOnDevice(String statusText) {
                    fallbackStatus.set(statusText);
                }
            }
        );

        assertTrue(answerRun.hostFallbackUsed);
        assertFalse(answerRun.hostBackendUsed);
        assertTrue(answerRun.answerBody.contains("fallback path"));
        assertTrue(answerRun.subtitle.startsWith("Offline answer | on-device fallback |"));
        assertEquals("Host unavailable. Continuing on this device...", fallbackStatus.get());
        assertTrue(OfflineAnswerEngine.buildCompletionStatus(answerRun).contains("host fallback"));
    }

    @Test
    public void generateEmitsLatencyEventForOnDeviceStreaming() throws Exception {
        List<JSONObject> events = captureLatencyEvents();
        File tempModel = File.createTempFile("senku-local", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                listener.onPartialText("Short");
                listener.onPartialText("Short answer: on-device path.");
                return "Short answer: on-device path.";
            }
        );

        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How do I improvise a rain shelter?",
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
            false,
            System.currentTimeMillis() - 1200L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertFalse(answerRun.hostBackendUsed);
        assertFalse(answerRun.hostFallbackUsed);
        assertNotNull(answerRun.latencyBreakdown);
        JSONObject event = lastLatencyEvent(events);
        assertEquals(LatencyPanel.QUERY_CLASS_PRACTICAL_HOW_TO, event.getString("queryClass"));
        assertEquals(answerRun.latencyBreakdown.retrievalMs, event.getLong("retrievalMs"));
        assertEquals(answerRun.latencyBreakdown.rerankMs, event.getLong("rerankMs"));
        assertEquals(answerRun.latencyBreakdown.promptBuildMs, event.getLong("promptBuildMs"));
        assertTrue(event.getLong("firstTokenMs") >= 0L);
        assertTrue(event.getLong("decodeMs") >= event.getLong("firstTokenMs"));
        assertTrue(event.getLong("totalMs") >= event.getLong("decodeMs"));
    }

    @Test
    public void generateEmitsLatencyEventForHostOnlyAnswer() throws Exception {
        List<JSONObject> events = captureLatencyEvents();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) ->
                new HostInferenceClient.Result(
                    "Keep pressure on the wound and monitor for infection.",
                    "mock-host",
                    0.42
                ),
            (context, modelFile, prompt, maxTokens, listener) -> {
                throw new AssertionError("on-device generation should not run");
            }
        );

        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "What should I do for a puncture wound?",
            List.of(
                new SearchResult(
                    "First Aid Essentials",
                    "",
                    "",
                    "",
                    "GD-115",
                    "Wound Cleaning",
                    "medical",
                    "guide-focus",
                    "safety",
                    "immediate",
                    "wound_care",
                    "wound_cleaning"
                )
            ),
            false,
            System.currentTimeMillis() - 1600L,
            true,
            "http://127.0.0.1:9/v1",
            "gemma-4-e2b-it-litert",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, null, prepared);

        assertTrue(answerRun.hostBackendUsed);
        assertFalse(answerRun.hostFallbackUsed);
        assertNotNull(answerRun.latencyBreakdown);
        JSONObject event = lastLatencyEvent(events);
        assertEquals(LatencyPanel.QUERY_CLASS_ACUTE_GENERATIVE, event.getString("queryClass"));
        assertTrue(event.getLong("firstTokenMs") >= 0L);
        assertEquals(event.getLong("decodeMs"), event.getLong("firstTokenMs"));
    }

    @Test
    public void generateEmitsLatencyEventForHostFallbackAnswer() throws Exception {
        List<JSONObject> events = captureLatencyEvents();
        File tempModel = File.createTempFile("senku-fallback-latency", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new IllegalStateException("host offline");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                listener.onPartialText("");
                listener.onPartialText("Fallback answer in progress.");
                return "Fallback answer in progress.";
            }
        );

        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How do I improvise a rain shelter?",
            List.of(),
            false,
            System.currentTimeMillis() - 1400L,
            true,
            "http://127.0.0.1:9/v1",
            "gemma-4-e2b-it-litert",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertFalse(answerRun.hostBackendUsed);
        assertTrue(answerRun.hostFallbackUsed);
        assertNotNull(answerRun.latencyBreakdown);
        JSONObject event = lastLatencyEvent(events);
        assertEquals(LatencyPanel.QUERY_CLASS_PRACTICAL_HOW_TO, event.getString("queryClass"));
        assertTrue(event.getLong("firstTokenMs") >= 0L);
        assertTrue(event.getLong("decodeMs") >= event.getLong("firstTokenMs"));
    }

    @Test
    public void generateEmitsLatencyEventForDeterministicAnswer() throws Exception {
        List<JSONObject> events = captureLatencyEvents();
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredDeterministic(
            "can i use old soda bottles",
            "Use only known food-safe bottles and sanitize them first.",
            List.of(),
            true,
            "reused_container_water",
            System.currentTimeMillis() - 25L
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, null, prepared);

        assertTrue(answerRun.deterministic);
        assertNotNull(answerRun.latencyBreakdown);
        JSONObject event = lastLatencyEvent(events);
        assertEquals(LatencyPanel.QUERY_CLASS_DETERMINISTIC, event.getString("queryClass"));
        assertEquals(0L, event.getLong("firstTokenMs"));
        assertEquals(0L, event.getLong("decodeMs"));
    }

    @Test
    public void generateEmitsLatencyEventForAbstainAnswer() throws Exception {
        List<JSONObject> events = captureLatencyEvents();
        List<SearchResult> adjacentGuides = List.of(
            new SearchResult(
                "Canvas Repair",
                "",
                "",
                "",
                "GD-102",
                "Patching",
                "crafts",
                "vector"
            )
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.abstain(
            "how do i build a rain shelter from a tarp",
            OfflineAnswerEngine.buildAbstainAnswerBody(
                "how do i build a rain shelter from a tarp",
                adjacentGuides
            ),
            adjacentGuides,
            false,
            System.currentTimeMillis() - 40L,
            18L,
            0L
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, null, prepared);

        assertTrue(answerRun.abstain);
        assertNotNull(answerRun.latencyBreakdown);
        JSONObject event = lastLatencyEvent(events);
        assertEquals(LatencyPanel.QUERY_CLASS_ABSTAIN, event.getString("queryClass"));
        assertEquals(18L, event.getLong("retrievalMs"));
        assertEquals(0L, event.getLong("rerankMs"));
        assertEquals(0L, event.getLong("firstTokenMs"));
        assertEquals(0L, event.getLong("decodeMs"));
    }

    @Test
    public void generateLogsRegexParseableLatencySummaryLine() throws Exception {
        AtomicReference<String> summaryLine = new AtomicReference<>("");
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if (!"SenkuMobile".equals(tag) || !message.startsWith("ask.latency")) {
                return;
            }
            summaryLine.set(message);
        });
        File tempModel = File.createTempFile("senku-latency-summary", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                listener.onPartialText("Short answer: on-device path.");
                return "Short answer: on-device path.";
            }
        );

        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How do I improvise a rain shelter?",
            List.of(),
            false,
            System.currentTimeMillis() - 1200L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertTrue(
            Pattern.compile(
                "^ask\\.latency queryClass=\"[^\"]+\" retrievalMs=\\d+ rerankMs=\\d+ promptBuildMs=\\d+ firstTokenMs=\\d+ decodeMs=\\d+ totalMs=\\d+ query=\".*\"$"
            ).matcher(summaryLine.get()).matches()
        );
    }

    @Test
    public void generateReportsPreparedConfidenceLabelToProgressListener() throws Exception {
        File tempModel = File.createTempFile("senku-confidence", ".litertlm");
        tempModel.deleteOnExit();
        AtomicReference<OfflineAnswerEngine.ConfidenceLabel> reportedLabel = new AtomicReference<>();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                listener.onPartialText("Short answer: confidence path.");
                return "Short answer: confidence path.";
            }
        );

        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "how do i repair a tarp shelter after wind damage",
            List.of(
                new SearchResult(
                    "Emergency Shelter Maintenance",
                    "",
                    "",
                    "",
                    "GD-112",
                    "Tarp shelter repair",
                    "survival",
                    "lexical",
                    "starter",
                    "immediate",
                    "emergency_shelter",
                    "tarp_shelter,repair"
                )
            ),
            false,
            System.currentTimeMillis() - 400L,
            false,
            "",
            "",
            "system",
            "prompt",
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(
            null,
            tempModel,
            prepared,
            new OfflineAnswerEngine.AnswerProgressListener() {
                @Override
                public void onAnswerBody(String partialAnswerBody) {
                }

                @Override
                public void onConfidenceLabel(OfflineAnswerEngine.ConfidenceLabel label) {
                    reportedLabel.set(label);
                }
            }
        );

        assertEquals(OfflineAnswerEngine.ConfidenceLabel.MEDIUM, reportedLabel.get());
        assertEquals(OfflineAnswerEngine.ConfidenceLabel.MEDIUM, answerRun.confidenceLabel);
    }

    @Test
    public void boostRecentContextMatchesKeepsFreshContextAheadOfRecentCarryover() {
        List<SearchResult> boosted = OfflineAnswerEngine.boostRecentContextMatches(
            "what about using tallow",
            List.of(
                new SearchResult(
                    "Agroforestry and Silvopasture",
                    "",
                    "",
                    "",
                    "GD-311",
                    "Getting Started: Small Silvopasture",
                    "agriculture",
                    "guide-focus"
                )
            ),
            List.of(
                new SearchResult(
                    "Animal Fat Rendering and Tallow Uses",
                    "",
                    "",
                    "",
                    "GD-486",
                    "Tallow Candles",
                    "agriculture",
                    "guide-focus"
                )
            )
        );

        assertEquals("GD-311", boosted.get(0).guideId);
        assertEquals("GD-486", boosted.get(1).guideId);
    }

    @Test
    public void boostRecentContextMatchesKeepsFreshDrainageContextAheadOfRecentHouseSources() {
        List<SearchResult> boosted = OfflineAnswerEngine.boostRecentContextMatches(
            "what about drainage",
            List.of(
                new SearchResult(
                    "Slope Control and Drainage",
                    "",
                    "",
                    "",
                    "GD-333",
                    "French Drain Construction",
                    "building",
                    "route-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "site_selection,drainage"
                )
            ),
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
                    "foundation,drainage"
                )
            )
        );

        assertEquals("GD-333", boosted.get(0).guideId);
        assertEquals("GD-094", boosted.get(1).guideId);
    }

    @Test
    public void trimWeakSessionContextDropsOffTopicCarryoverForSpecificNonRouteFollowUp() {
        List<SearchResult> trimmed = OfflineAnswerEngine.trimWeakSessionContext(
            "tallow candles",
            true,
            List.of(
                new SearchResult(
                    "Animal Fat Rendering and Tallow Uses",
                    "",
                    "",
                    "",
                    "GD-486",
                    "Tallow Candles",
                    "agriculture",
                    "guide-focus"
                ),
                new SearchResult(
                    "Agroforestry and Silvopasture",
                    "",
                    "",
                    "",
                    "GD-311",
                    "Getting Started: Small Silvopasture",
                    "agriculture",
                    "guide-focus"
                )
            ),
            List.of(
                new SearchResult(
                    "Animal Fat Rendering and Tallow Uses",
                    "",
                    "",
                    "",
                    "GD-486",
                    "Tallow Candles",
                    "agriculture",
                    "guide-focus"
                )
            )
        );

        assertEquals(1, trimmed.size());
        assertEquals("GD-486", trimmed.get(0).guideId);
    }

    @Test
    public void vagueFollowUpUsesContextualizedRetrievalQueryForContextSelection() {
        String contextSelectionQuery = OfflineAnswerEngine.selectContextSelectionQuery(
            "what about drainage",
            "drainage cabin house foundation wall construction roofing ventilation weatherproofing",
            true
        );

        assertEquals(
            "drainage cabin house foundation",
            contextSelectionQuery
        );
    }

    @Test
    public void focusedWaterFollowUpKeepsTopicTokensWhileBorrowingDomainContext() {
        String contextSelectionQuery = OfflineAnswerEngine.selectContextSelectionQuery(
            "can i use old soda bottles",
            "old soda bottles water storage known food-safe freshly",
            true
        );

        assertEquals("old soda bottles water storage food-safe", contextSelectionQuery);
    }

    @Test
    public void focusedCandleFollowUpDropsLowSignalLightingCarryover() {
        String contextSelectionQuery = OfflineAnswerEngine.selectContextSelectionQuery(
            "what about using tallow",
            "tallow make candles light",
            true
        );

        assertEquals("tallow candles", contextSelectionQuery);
    }

    @Test
    public void seasonalSiteFollowUpUsesFourPromptContextItems() {
        int promptContextLimit = OfflineAnswerEngine.promptContextLimitFor(
            "winter sun summer shade site selection wind exposure microclimate cabin"
        );

        assertEquals(4, promptContextLimit);
    }

    @Test
    public void ordinaryHousePromptKeepsDefaultPromptContextItems() {
        int promptContextLimit = OfflineAnswerEngine.promptContextLimitFor(
            "how do i build a house"
        );

        assertEquals(3, promptContextLimit);
    }

    @Test
    public void standaloneQuestionKeepsRawContextSelectionQuery() {
        String contextSelectionQuery = OfflineAnswerEngine.selectContextSelectionQuery(
            "how do i build a house",
            "drainage cabin house foundation wall construction roofing ventilation weatherproofing",
            false
        );

        assertEquals("how do i build a house", contextSelectionQuery);
    }

    @Test
    public void contextualDeterministicAnswerKeepsBottleFollowUpInWaterLane() {
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

        String followUp = "can i use old soda bottles";
        String retrievalQuery = memory.contextualizeForRetrieval(followUp);
        boolean sessionUsed = !retrievalQuery.equals(followUp);
        String contextSelectionQuery = OfflineAnswerEngine.selectContextSelectionQuery(
            followUp,
            retrievalQuery,
            sessionUsed
        );

        DeterministicAnswerRouter.DeterministicAnswer deterministic = OfflineAnswerEngine.selectDeterministicAnswer(
            followUp,
            retrievalQuery,
            contextSelectionQuery,
            sessionUsed
        );

        assertTrue(sessionUsed);
        assertTrue(contextSelectionQuery.startsWith("old soda bottles"));
        assertTrue(contextSelectionQuery.contains("water"));
        assertTrue(contextSelectionQuery.contains("storage"));
        assertNotNull(deterministic);
        assertEquals("reused_container_water", deterministic.ruleId);
    }

    @Test
    public void vagueFollowUpDoesNotOvermatchContextualDeterministicAnswer() {
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

        String followUp = "what next";
        String retrievalQuery = memory.contextualizeForRetrieval(followUp);
        boolean sessionUsed = !retrievalQuery.equals(followUp);
        String contextSelectionQuery = OfflineAnswerEngine.selectContextSelectionQuery(
            followUp,
            retrievalQuery,
            sessionUsed
        );

        DeterministicAnswerRouter.DeterministicAnswer deterministic = OfflineAnswerEngine.selectDeterministicAnswer(
            followUp,
            retrievalQuery,
            contextSelectionQuery,
            sessionUsed
        );

        assertTrue(sessionUsed);
        assertNull(deterministic);
    }

    @Test
    public void deterministicCompletionStatusDoesNotShowUnknownTime() {
        OfflineAnswerEngine.AnswerRun answerRun = new OfflineAnswerEngine.AnswerRun(
            "can i use old soda bottles",
            "answer",
            List.of(),
            0L,
            true,
            true,
            false,
            "Offline answer | deterministic | instant",
            ""
        );

        assertEquals("Deterministic answer ready.", OfflineAnswerEngine.buildCompletionStatus(answerRun));
    }

    @Test
    public void shouldAbstainForWeakAdjacentGuides() {
        boolean shouldAbstain = OfflineAnswerEngine.shouldAbstain(
            List.of(
                new SearchResult(
                    "Shelter Ventilation Basics",
                    "",
                    "",
                    "",
                    "GD-101",
                    "Ventilation",
                    "building",
                    "vector"
                ),
                new SearchResult(
                    "Canvas Repair",
                    "",
                    "",
                    "",
                    "GD-102",
                    "Patching",
                    "crafts",
                    "vector"
                ),
                new SearchResult(
                    "Tent Maintenance",
                    "",
                    "",
                    "",
                    "GD-103",
                    "Storage",
                    "resource-management",
                    "lexical"
                )
            ),
            "how do i build a rain shelter from a tarp"
        );

        assertTrue(shouldAbstain);
    }

    @Test
    public void buildAbstainAnswerBodyAddsSafetyCriticalEscalationOnlyWhenFlagged() {
        List<SearchResult> adjacentGuides = List.of(
            new SearchResult(
                "Canvas Repair",
                "",
                "",
                "",
                "GD-102",
                "Patching",
                "crafts",
                "vector"
            ),
            new SearchResult(
                "Tent Maintenance",
                "",
                "",
                "",
                "GD-103",
                "Storage",
                "resource-management",
                "lexical"
            )
        );
        Object[][] cases = new Object[][]{
            {"my child may have poisoning after swallowing drain cleaner", true},
            {"how do i build a rain shelter from a tarp", false},
        };

        for (Object[] testCase : cases) {
            String query = (String) testCase[0];
            boolean expectEscalation = (Boolean) testCase[1];
            String answerBody = OfflineAnswerEngine.buildAbstainAnswerBody(
                query,
                adjacentGuides,
                OfflineAnswerEngine.isSafetyCriticalQuery(query, adjacentGuides)
            );

            boolean hasEscalation = answerBody.contains(OfflineAnswerEngine.SAFETY_CRITICAL_ESCALATION_LINE);
            assertEquals(query, expectEscalation, hasEscalation);
            if (expectEscalation) {
                assertTrue(
                    query,
                    answerBody.indexOf(OfflineAnswerEngine.SAFETY_CRITICAL_ESCALATION_LINE)
                        < answerBody.indexOf("Closest matches in the library:")
                );
            }
        }
    }

    @Test
    public void shouldNotAbstainWhenHybridCandidateLooksGrounded() {
        boolean shouldAbstain = OfflineAnswerEngine.shouldAbstain(
            List.of(
                new SearchResult(
                    "Tarp Shelter Setup",
                    "",
                    "",
                    "",
                    "GD-104",
                    "Rain Pitch",
                    "survival",
                    "hybrid"
                )
            ),
            "how do i build a rain shelter from a tarp"
        );

        assertFalse(shouldAbstain);
    }

    @Test
    public void generateReturnsAbstainWithoutCallingGenerators() throws Exception {
        AtomicReference<String> hostCall = new AtomicReference<>("");
        AtomicReference<String> deviceCall = new AtomicReference<>("");
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                hostCall.set("called");
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                deviceCall.set("called");
                throw new AssertionError("device generation should not run");
            }
        );

        List<SearchResult> adjacentGuides = List.of(
            new SearchResult(
                "Canvas Repair",
                "",
                "",
                "",
                "GD-102",
                "Patching",
                "crafts",
                "vector"
            )
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.abstain(
            "how do i build a rain shelter from a tarp",
            OfflineAnswerEngine.buildAbstainAnswerBody(
                "how do i build a rain shelter from a tarp",
                adjacentGuides
            ),
            adjacentGuides,
            false
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, null, prepared);

        assertTrue(answerRun.abstain);
        assertFalse(answerRun.deterministic);
        assertTrue(answerRun.answerBody.contains("Senku doesn't have a guide"));
        assertEquals("", hostCall.get());
        assertEquals("", deviceCall.get());
        assertEquals("No guide match. Try rephrasing.", OfflineAnswerEngine.buildCompletionStatus(answerRun));
    }

    @Test
    public void sessionHintRerankPromotesWaterStorageGuideForBottleFollowUp() {
        List<SearchResult> reranked = OfflineAnswerEngine.rerankWithSessionHints(
            "can i use old soda bottles",
            "old soda bottles water storage container sanitation",
            QueryMetadataProfile.fromQuery("what's the safest way to store treated water long term can i use old soda bottles"),
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
            ),
            List.of(
                new SearchResult(
                    "Agroforestry and Silvopasture",
                    "",
                    "",
                    "",
                    "GD-311",
                    "Getting Started: Small Silvopasture",
                    "agriculture",
                    "guide-focus",
                    "starter",
                    "long_term",
                    "general",
                    "fodder,rotational_grazing"
                ),
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

        assertEquals("GD-252", reranked.get(0).guideId);
    }

    @Test
    public void sessionHintRerankPromotesRecentDistributionGuideForStorageTankFollowUp() {
        List<SearchResult> reranked = OfflineAnswerEngine.rerankWithSessionHints(
            "what about storage tanks",
            "storage tanks water distribution water storage",
            QueryMetadataProfile.fromQuery("how do i design a gravity-fed water distribution system what about storage tanks"),
            List.of(
                new SearchResult(
                    "Community Water Distribution Systems",
                    "",
                    "",
                    "",
                    "GD-270",
                    "Water Tower Construction & Sizing",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "water_storage",
                    "water_storage,water_distribution"
                )
            ),
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
                    "planning",
                    "long_term",
                    "water_storage",
                    "weatherproofing,water_storage"
                ),
                new SearchResult(
                    "Community Water Distribution Systems",
                    "",
                    "",
                    "",
                    "GD-270",
                    "Water Tower Construction & Sizing",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "water_storage",
                    "water_storage,water_distribution"
                )
            )
        );

        assertEquals("GD-270", reranked.get(0).guideId);
    }

    @Test
    public void sessionHintRerankKeepsDrainageGuideInPromptWindowForHouseFollowUp() {
        List<SearchResult> reranked = OfflineAnswerEngine.rerankWithSessionHints(
            "what about drainage",
            "drainage cabin house wall construction roofing ventilation foundation weatherproofing",
            QueryMetadataProfile.fromQuery("drainage cabin house wall construction roofing ventilation foundation weatherproofing"),
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Roofing Systems",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "roofing,weatherproofing"
                ),
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Wall Construction",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "wall_construction"
                ),
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "foundation,drainage"
                )
            ),
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Roofing Systems",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "roofing,weatherproofing"
                ),
                new SearchResult(
                    "Drainage, Swales, and Water Diversion Earthworks",
                    "",
                    "",
                    "",
                    "GD-333",
                    "French Drain Construction",
                    "building",
                    "route-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "site_selection,drainage"
                ),
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Wall Construction",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "wall_construction"
                ),
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "foundation,drainage"
                )
            )
        );

        boolean drainageGuideInPromptWindow = false;
        for (int index = 0; index < Math.min(3, reranked.size()); index++) {
            if ("GD-333".equals(reranked.get(index).guideId)) {
                drainageGuideInPromptWindow = true;
                break;
            }
        }

        assertTrue(drainageGuideInPromptWindow);
    }

    @Test
    public void offlineAnswerEngineUsesExplicitTokenBudget() {
        assertEquals(Integer.valueOf(2048), OfflineAnswerEngine.modelMaxTokensForTest());
    }

    @Test
    public void generatingStatusWithoutContextKeepsBackendSpecificCopy() {
        assertEquals(
            "Building answer on this device...",
            OfflineAnswerEngine.buildGeneratingStatus(null, 0, false)
        );
        assertEquals(
            "Building answer on host GPU...",
            OfflineAnswerEngine.buildGeneratingStatus(null, 0, true)
        );
    }

    @Test
    public void generatingStatusWithoutContextKeepsSourceAwareCopy() {
        assertEquals(
            "Sources ready on this device. Building answer from 2 guides...",
            OfflineAnswerEngine.buildGeneratingStatus(null, 2, false)
        );
        assertEquals(
            "Sources ready on host GPU. Building answer from 1 guide...",
            OfflineAnswerEngine.buildGeneratingStatus(null, 1, true)
        );
    }

    @Test
    public void stillBuildingStatusWithoutContextKeepsBackendSpecificCopy() {
        assertEquals(
            "Still building on this device. You can inspect sources now.",
            OfflineAnswerEngine.buildStillBuildingStatus(null, 0, false)
        );
        assertEquals(
            "Still building on host GPU. You can inspect sources now.",
            OfflineAnswerEngine.buildStillBuildingStatus(null, 0, true)
        );
    }

    private static List<JSONObject> captureLatencyEvents() {
        ArrayList<JSONObject> events = new ArrayList<>();
        LatencyPanel.setLogSinkForTest((tag, message) -> {
            if (!LatencyPanel.TAG.equals(tag)) {
                return;
            }
            try {
                events.add(new JSONObject(message));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
        return events;
    }

    private static JSONObject lastLatencyEvent(List<JSONObject> events) {
        assertFalse(events.isEmpty());
        return events.get(events.size() - 1);
    }
}

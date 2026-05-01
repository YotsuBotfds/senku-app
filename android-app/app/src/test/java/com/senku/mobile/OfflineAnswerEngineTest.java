package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        AnswerCardRuntime.resetEnabledForTest();
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
    public void retrievalStageNamesSessionPlanAndRecentSources() {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "how do I build a tarp shelter",
            "Use a ridgeline and pitch runoff away.",
            List.of(
                new SearchResult(
                    "Tarp & Cord Shelters",
                    "",
                    "",
                    "",
                    "GD-345",
                    "Ridgeline shelter",
                    "survival",
                    "guide-focus",
                    "topic",
                    "immediate",
                    "emergency_shelter",
                    "tarp,cord,ridgeline"
                )
            )
        );

        OfflineAnswerEngine.RetrievalStage stage = OfflineAnswerEngine.buildRetrievalStage(
            memory,
            "what next"
        );

        assertTrue(stage.sessionUsed);
        assertEquals(stage.retrievalPlan.searchQuery.trim(), stage.retrievalQuery);
        assertFalse(stage.contextSelectionQuery.isEmpty());
        assertTrue(stage.sessionPromptContext.contains("recent questions:"));
        assertEquals(1, stage.recentSources.size());
        assertEquals("GD-345", stage.recentSources.get(0).guideId);
    }

    @Test
    public void retrievalStageFallsBackContextSelectionToTrimmedQueryWithoutSession() {
        OfflineAnswerEngine.RetrievalStage stage = OfflineAnswerEngine.buildRetrievalStage(
            new SessionMemory(),
            "how do I store drinking water"
        );

        assertFalse(stage.sessionUsed);
        assertEquals("how do I store drinking water", stage.retrievalQuery);
        assertEquals("how do I store drinking water", stage.contextSelectionQuery);
        assertTrue(stage.recentSources.isEmpty());
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
    public void resolveAnswerModeHitsBoundariesAndSafetyGate() {
        String confidentQuery = "how do i build a cabin roof that sheds rain";
        List<SearchResult> confidentResults = List.of(
            new SearchResult(
                "Cabin Roofing and Weatherproofing",
                "",
                "",
                "",
                "GD-410",
                "Cabin roof rain setup",
                "building",
                "hybrid",
                "planning",
                "long_term",
                "cabin_house",
                "roof,rain"
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
        );
        QueryMetadataProfile confidentProfile = QueryMetadataProfile.fromQuery(confidentQuery);
        assertEquals(
            OfflineAnswerEngine.AnswerMode.CONFIDENT,
            OfflineAnswerEngine.resolveAnswerMode(
                confidentResults,
                confidentQuery,
                confidentProfile,
                OfflineAnswerEngine.confidenceLabel(confidentResults, confidentQuery, confidentProfile),
                false
            )
        );

        String upperBandQuery = "how do i rig a tarp rain cover with cord";
        List<SearchResult> upperBandResults = List.of(
            new SearchResult(
                "Tarp rain cover setup",
                "",
                "",
                "",
                "GD-113",
                "Corded tarp rigging",
                "survival",
                "hybrid",
                "starter",
                "immediate",
                "emergency_shelter",
                "tarp_shelter,cord"
            ),
            new SearchResult(
                "Rain cover drainage",
                "",
                "",
                "",
                "GD-114",
                "Runoff and stake order",
                "survival",
                "hybrid",
                "starter",
                "immediate",
                "emergency_shelter",
                "rain_cover,drainage"
            )
        );
        QueryMetadataProfile upperBandProfile = QueryMetadataProfile.fromQuery(upperBandQuery);
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                upperBandResults,
                upperBandQuery,
                upperBandProfile,
                OfflineAnswerEngine.confidenceLabel(upperBandResults, upperBandQuery, upperBandProfile),
                false
            )
        );

        String uncertainQuery = "how do i repair a tarp shelter after wind damage";
        List<SearchResult> uncertainResults = List.of(
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
        );
        QueryMetadataProfile uncertainProfile = QueryMetadataProfile.fromQuery(uncertainQuery);
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                uncertainResults,
                uncertainQuery,
                uncertainProfile,
                OfflineAnswerEngine.confidenceLabel(uncertainResults, uncertainQuery, uncertainProfile),
                false
            )
        );

        String abstainQuery = "how do i build a rain shelter from a tarp";
        List<SearchResult> abstainResults = List.of(
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
        QueryMetadataProfile abstainProfile = QueryMetadataProfile.fromQuery(abstainQuery);
        assertEquals(
            OfflineAnswerEngine.AnswerMode.ABSTAIN,
            OfflineAnswerEngine.resolveAnswerMode(
                abstainResults,
                abstainQuery,
                abstainProfile,
                OfflineAnswerEngine.confidenceLabel(abstainResults, abstainQuery, abstainProfile),
                false
            )
        );

        String safetyQuery = "he has barely slept, keeps pacing, and says normal rules do not apply to him";
        List<SearchResult> safetyUncertainResults = List.of(
            new SearchResult(
                "Barely Slept Pacing Notes",
                "",
                "",
                "",
                "GD-305",
                "Normal rules observation",
                "mental-health",
                "vector"
            ),
            new SearchResult(
                "Calm Down or Escalate Checklist",
                "",
                "",
                "",
                "GD-306",
                "Pacing observer steps",
                "mental-health",
                "lexical"
            )
        );
        QueryMetadataProfile safetyProfile = QueryMetadataProfile.fromQuery(safetyQuery);
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                safetyUncertainResults,
                safetyQuery,
                safetyProfile,
                OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
                true
            )
        );

        List<SearchResult> safetyConfidentResults = List.of(
            new SearchResult(
                "Pacing Slept Normal Rules Crisis Guide",
                "",
                "",
                "",
                "GD-307",
                "Pacing slept normal rules response",
                "mental-health",
                "hybrid",
                "safety",
                "immediate",
                "acute_behavior",
                "pacing,slept,normal"
            ),
            new SearchResult(
                "Barely Sleeping Crisis Escalation",
                "",
                "",
                "",
                "GD-308",
                "Pacing and barely slept escalation",
                "mental-health",
                "hybrid",
                "safety",
                "immediate",
                "acute_behavior",
                "pacing,slept"
            )
        );
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                safetyConfidentResults,
                safetyQuery,
                safetyProfile,
                OfflineAnswerEngine.ConfidenceLabel.HIGH,
                true
            )
        );
    }

    @Test
    public void resolveAnswerModeRoutesSafetyPoisoningToAbstainBeforeGeneration() {
        String query = "my child may have poisoning after swallowing drain cleaner";
        List<SearchResult> rawTopChunks = List.of(
            new SearchResult(
                "First Aid & Emergency Response",
                "",
                "Wound management basics.",
                "Wound cleaning and dressing basics.",
                "GD-232",
                "Wound Management",
                "medical",
                "hybrid",
                "safety",
                "immediate",
                "wound_care",
                "first_aid,wound_cleaning"
            ),
            new SearchResult(
                "Animal Bite Wound Care & Rabies Post-Exposure Protocols",
                "",
                "Amputation and salvage criteria.",
                "Field bite care and rabies precautions.",
                "GD-622",
                "Amputation vs. Salvage Criteria",
                "medical",
                "vector",
                "safety",
                "immediate",
                "wound_care",
                "first_aid,infection_monitoring"
            ),
            new SearchResult(
                "First Aid & Emergency Response",
                "",
                "Environmental emergencies and exposure response.",
                "Corrosive exposure response, airway support, and escalation.",
                "GD-232",
                "Environmental Emergencies",
                "medical",
                "lexical",
                "safety",
                "immediate",
                "safety_poisoning",
                "lye_safety"
            )
        );
        List<SearchResult> selectedContext = List.of(
            new SearchResult(
                "First Aid & Emergency Response",
                "",
                "Poisoning exposure priorities.",
                "Drain cleaner and corrosive poisoning can worsen quickly and need Poison Control escalation.",
                "GD-232",
                "Environmental Emergencies",
                "medical",
                "guide-focus",
                "safety",
                "immediate",
                "safety_poisoning",
                "lye_safety"
            ),
            new SearchResult(
                "First Aid & Emergency Response",
                "",
                "Poisoning and overdose triage.",
                "Poisoning and overdose guidance starts with Poison Control, airway checks, and escalation.",
                "GD-232",
                "Poisoning and Overdose",
                "medical",
                "guide-focus",
                "safety",
                "immediate",
                "safety_poisoning",
                "lye_safety"
            ),
            new SearchResult(
                "First Aid & Emergency Response",
                "",
                "Airway backup while waiting for emergency help.",
                "Choking and airway management may support a poisoning response while awaiting help.",
                "GD-232",
                "Choking and Airway Management",
                "medical",
                "guide-focus",
                "safety",
                "immediate",
                "safety_poisoning",
                "lye_safety"
            )
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel = OfflineAnswerEngine.confidenceLabel(
            selectedContext,
            query,
            metadataProfile
        );

        assertEquals("safety_poisoning", metadataProfile.preferredStructureType());
        assertFalse(
            OfflineAnswerEngine.shouldAbstain(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile
            )
        );
        assertEquals(
            OfflineAnswerEngine.AnswerMode.ABSTAIN,
            OfflineAnswerEngine.resolveAnswerMode(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile,
                confidenceLabel,
                true
            )
        );

        String answerBody = OfflineAnswerEngine.buildAbstainAnswerBody(query, selectedContext, true);
        assertTrue(answerBody.contains(OfflineAnswerEngine.SAFETY_CRITICAL_ESCALATION_LINE));
        assertTrue(answerBody.contains("Poison Control"));
    }

    @Test
    public void resolveAnswerModeRoutesAcuteMentalHealthMismatchToUncertainFit() {
        String query =
            "He has barely slept, keeps pacing, and says normal rules do not apply to him. "
                + "Is this just stress, or should I help him calm down?";
        List<SearchResult> rawTopChunks = List.of(
            new SearchResult(
                "First Aid & Emergency Response",
                "",
                "Environmental emergencies.",
                "Emergency response basics for weather and exposure.",
                "GD-232",
                "Environmental Emergencies",
                "medical",
                "hybrid"
            ),
            new SearchResult(
                "Improvised Weapons and Defensive Tools",
                "",
                "Defensive encounter medical notes.",
                "Medical considerations for defensive encounters.",
                "GD-293",
                "Medical Considerations for Defensive Encounters",
                "defense",
                "vector"
            ),
            new SearchResult(
                "Justice & Legal Systems",
                "",
                "Civil disputes and legal process.",
                "Rules for handling civil disputes and property conflicts.",
                "GD-197",
                "5. Civil Disputes",
                "society",
                "lexical"
            )
        );
        List<SearchResult> selectedContext = List.of(
            new SearchResult(
                "Justice & Legal Systems",
                "",
                "Household rule disputes under stress.",
                "These civil dispute rules apply when property stress rises in the group.",
                "GD-197",
                "5. Civil Disputes",
                "society",
                "guide-focus"
            ),
            new SearchResult(
                "Justice & Legal Systems",
                "",
                "Property rules in stressed households.",
                "Property law explains when rules apply during household stress.",
                "GD-197",
                "12. Property Law",
                "society",
                "guide-focus"
            ),
            new SearchResult(
                "Justice & Legal Systems",
                "",
                "Criminal law basics.",
                "Basic legal rules apply when conflicts escalate.",
                "GD-197",
                "4. Criminal Law Basics",
                "society",
                "guide-focus"
            )
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel = OfflineAnswerEngine.confidenceLabel(
            selectedContext,
            query,
            metadataProfile
        );

        assertEquals("acute_mental_health", metadataProfile.preferredStructureType());
        assertFalse(
            OfflineAnswerEngine.shouldAbstain(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile
            )
        );
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile,
                confidenceLabel,
                true
            )
        );

        String answerBody = OfflineAnswerEngine.buildUncertainFitAnswerBody(
            query,
            selectedContext,
            confidenceLabel,
            true
        );
        assertTrue(answerBody.contains(OfflineAnswerEngine.SAFETY_CRITICAL_ESCALATION_LINE));
        assertTrue(answerBody.contains("not a confident fit"));
    }

    @Test
    public void safetyModeOverridesKeepViolinBridgeAbstainAndRainShelterUncertainFit() {
        String rainQuery = "How do I build a simple rain shelter from tarp and cord?";
        List<SearchResult> rainRawTopChunks = List.of(
            new SearchResult(
                "Batteries & Energy Storage: Principles & Simple Applications",
                "",
                "Charging and maintenance basics for salvaged cells.",
                "Battery banks, lead acid maintenance, and charging safety.",
                "GD-727",
                "Practical Survival Applications",
                "utility",
                "hybrid"
            ),
            new SearchResult(
                "Education System Design",
                "",
                "Schoolhouse management and mixed-age planning.",
                "Lesson rotation, desks, and class planning.",
                "GD-653",
                "One-Room Schoolhouse & Mixed-Age Classroom Management",
                "education",
                "hybrid"
            ),
            new SearchResult(
                "Cave Shelter Systems and Long-Term Habitation",
                "",
                "Natural shelter options and drainage.",
                "Choose shelter placement with runoff control and weather protection.",
                "GD-294",
                "Cave Selection Criteria",
                "survival",
                "hybrid",
                "starter",
                "immediate",
                "emergency_shelter",
                "shelter"
            )
        );
        List<SearchResult> rainSelectedContext = List.of(
            new SearchResult(
                "Cave Shelter Systems and Long-Term Habitation",
                "",
                "Use tarp panels and cord lashings to keep rain off the sleeping area.",
                "A simple rain shelter can use a ridgeline, tarp angle, cord lashings, and drainage so runoff stays away from the sleeping area.",
                "GD-294",
                "Rain runoff and tarp placement",
                "survival",
                "guide-focus",
                "starter",
                "immediate",
                "emergency_shelter",
                "tarp_shelter,weatherproofing"
            ),
            new SearchResult(
                "Cave Shelter Systems and Long-Term Habitation",
                "",
                "Cord tie-downs keep the tarp stable in wind and rain.",
                "Secure the shelter with cord, tighten the ridgeline, and angle the tarp so rain sheds cleanly.",
                "GD-294",
                "Cord lashings and storm tension",
                "survival",
                "guide-focus",
                "starter",
                "immediate",
                "emergency_shelter",
                "tarp_shelter,weatherproofing"
            ),
            new SearchResult(
                "Shelter basics",
                "",
                "Simple tarp shelter setup notes.",
                "For a quick shelter, tie cord between anchors, drape the tarp, and pitch one side low for rain runoff.",
                "GD-933",
                "Simple tarp shelter setup",
                "survival",
                "lexical",
                "starter",
                "immediate",
                "emergency_shelter",
                "tarp_shelter"
            )
        );
        QueryMetadataProfile rainMetadataProfile = QueryMetadataProfile.fromQuery(rainQuery);

        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                rainSelectedContext,
                rainRawTopChunks,
                rainQuery,
                rainMetadataProfile,
                OfflineAnswerEngine.confidenceLabel(rainSelectedContext, rainQuery, rainMetadataProfile),
                false
            )
        );

        String violinQuery = "how do i tune a violin bridge and soundpost";
        List<SearchResult> violinRawTopChunks = List.of(
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Suspension bridge load distribution.",
                "Bridge span load calculations, deck tension, and cable support.",
                "GD-110",
                "Suspension Bridges",
                "engineering",
                "hybrid"
            ),
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Bridge stress testing in the field.",
                "Load rating and structural bridge inspection routines.",
                "GD-110",
                "Field Load Testing and Structural Assessment",
                "engineering",
                "hybrid"
            ),
            new SearchResult(
                "Dental Prosthetics & Denture Making",
                "",
                "Repairing a dental bridge in low-resource settings.",
                "Dental bridge stabilization and tooth replacement steps.",
                "GD-061",
                "Dental Bridge Improvisation",
                "medical",
                "hybrid"
            )
        );
        List<SearchResult> violinSelectedContext = List.of(
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Bridge load tables and span measurements.",
                "Bridge abutments, load paths, and span calculations for timber crossings.",
                "GD-110",
                "Bridge Load Calculations",
                "engineering",
                "guide-focus"
            ),
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Aqueducts and water conveyance structures.",
                "Bridge piers, aqueduct supports, and channel alignment.",
                "GD-110",
                "Aqueducts & Water Conveyance",
                "engineering",
                "guide-focus"
            ),
            new SearchResult(
                "Dental Prosthetics & Denture Making",
                "",
                "Improvised dental bridge notes.",
                "Dental bridge shaping and mouth-fit checks.",
                "GD-061",
                "Dental Bridge Improvisation",
                "medical",
                "hybrid"
            )
        );
        QueryMetadataProfile violinMetadataProfile = QueryMetadataProfile.fromQuery(violinQuery);

        assertEquals(
            OfflineAnswerEngine.AnswerMode.ABSTAIN,
            OfflineAnswerEngine.resolveAnswerMode(
                violinSelectedContext,
                violinRawTopChunks,
                violinQuery,
                violinMetadataProfile,
                OfflineAnswerEngine.confidenceLabel(violinSelectedContext, violinQuery, violinMetadataProfile),
                false
            )
        );
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
            List.of(
                new SearchResult(
                    "Emergency Shelter",
                    "",
                    "Rig a tarp at an angle and route water away from the sleeping area.",
                    "Use a ridgeline, stake the tarp securely, and keep drainage away from bedding.",
                    "GD-094",
                    "Rain tarp setup",
                    "shelter",
                    "guide-focus"
                )
            ),
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
    public void preparedEarlyAbstainPathEmitsFinalModeThroughDebugSinkWithoutDuplicates() {
        List<String> finalModeLines = captureFinalModeLines();
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.abstain(
            "how do i build a rain shelter from a tarp",
            OfflineAnswerEngine.buildAbstainAnswerBody(
                "how do i build a rain shelter from a tarp",
                List.of()
            ),
            List.of(),
            false
        );

        OfflineAnswerEngine.logPreparedFinalModeIfReady(prepared);
        OfflineAnswerEngine.logPreparedFinalModeIfReady(prepared);

        assertEquals(1, finalModeLines.size());
        assertTrue(finalModeLines.get(0).contains("final_mode=abstain"));
        assertTrue(finalModeLines.get(0).contains("route=early_abstain"));
    }

    @Test
    public void generateConfidentPathStillEmitsFinalModeThroughDebugSink() throws Exception {
        List<String> finalModeLines = captureFinalModeLines();
        File tempModel = File.createTempFile("senku-confident-debug-sink", ".litertlm");
        tempModel.deleteOnExit();
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
            "How do i make a simple fire starter?",
            List.of(
                new SearchResult(
                    "Fire Starting",
                    "",
                    "Keep tinder dry and feed small kindling before larger fuel.",
                    "Use dry tinder, protect it from wind, and add kindling gradually.",
                    "GD-122",
                    "Simple fire starter",
                    "fire",
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

        OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertEquals(1, finalModeLines.size());
        assertTrue(finalModeLines.get(0).contains("final_mode=confident"));
        assertTrue(finalModeLines.get(0).contains("route=confident_generation"));
    }

    @Test
    public void generateDeterministicPathEmitsFinalModeConfidentRouteDeterministicTelemetry() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredDeterministic(
            "can i use old soda bottles",
            "Use only known food-safe bottles and sanitize them first.",
            List.of(),
            true,
            "reused_container_water",
            System.currentTimeMillis() - 25L
        );

        OfflineAnswerEngine.generate(null, null, prepared);

        assertEquals(1, finalModeLines.get().size());
        assertTrue(
            Pattern.compile(
                "^ask\\.generate final_mode=confident route=deterministic query=\"can i use old soda bottles\" totalElapsedMs=\\d+$"
            ).matcher(finalModeLines.get().get(0)).matches()
        );
    }

    @Test
    public void generateEarlyAbstainPathEmitsFinalModeAbstainRouteEarlyAbstainTelemetry() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.abstain(
            "how do i build a rain shelter from a tarp",
            OfflineAnswerEngine.buildAbstainAnswerBody(
                "how do i build a rain shelter from a tarp",
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
                    )
                )
            ),
            List.of(),
            false
        );

        OfflineAnswerEngine.generate(null, null, prepared);

        assertEquals(1, finalModeLines.get().size());
        assertTrue(
            Pattern.compile(
                "^ask\\.generate final_mode=abstain route=early_abstain query=\"how do i build a rain shelter from a tarp\" totalElapsedMs=\\d+$"
            ).matcher(finalModeLines.get().get(0)).matches()
        );
    }

    @Test
    public void generateEarlyUncertainFitPathEmitsFinalModeUncertainFitRouteEarlyUncertainFitTelemetry() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.uncertainFit(
            "he has barely slept, keeps pacing, and says normal rules do not apply to him",
            OfflineAnswerEngine.buildUncertainFitAnswerBody(
                "he has barely slept, keeps pacing, and says normal rules do not apply to him",
                List.of(),
                OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
                true
            ),
            List.of(),
            false,
            System.currentTimeMillis() - 250L,
            12L,
            4L,
            0L,
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            true
        );

        OfflineAnswerEngine.generate(null, null, prepared);

        assertEquals(1, finalModeLines.get().size());
        assertTrue(
            Pattern.compile(
                "^ask\\.generate final_mode=uncertain_fit route=early_uncertain_fit query=\"he has barely slept, keeps pacing, and says normal rules do not apply to him\" totalElapsedMs=\\d+$"
            ).matcher(finalModeLines.get().get(0)).matches()
        );
    }

    @Test
    public void generateConfidentPathEmitsFinalModeConfidentRouteConfidentGenerationTelemetry() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        File tempModel = File.createTempFile("senku-confident-path", ".litertlm");
        tempModel.deleteOnExit();
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
            "How do i make a simple fire starter?",
            List.of(
                new SearchResult(
                    "Fire Starting",
                    "",
                    "Keep tinder dry and feed small kindling before larger fuel.",
                    "Use dry tinder, protect it from wind, and add kindling gradually.",
                    "GD-122",
                    "Simple fire starter",
                    "fire",
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

        OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertEquals(1, finalModeLines.get().size());
        assertTrue(
            Pattern.compile(
                "^ask\\.generate final_mode=confident route=confident_generation query=\"How do i make a simple fire starter\\?\" totalElapsedMs=\\d+$"
            ).matcher(finalModeLines.get().get(0)).matches()
        );
    }

    @Test
    public void generateLowCoverageDowngradePathEmitsFinalModeAbstainOrUncertainFitRouteLowCoverageDowngradeTelemetry() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        File tempModel = File.createTempFile("senku-low-coverage", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                return "No specific information available.";
            }
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How should i care for a minor sprain?",
            List.of(),
            false,
            System.currentTimeMillis() - 1800L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertEquals(1, finalModeLines.get().size());
        assertTrue(
            Pattern.compile(
                "^ask\\.generate final_mode=(abstain|uncertain_fit) route=low_coverage_downgrade query=\"How should i care for a minor sprain\\?\" totalElapsedMs=\\d+$"
            ).matcher(finalModeLines.get().get(0)).matches()
        );
    }

    @Test
    public void generateSourceSummaryFallbackPathEmitsFinalModeConfidentRouteSourceSummaryFallbackTelemetry() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        File tempModel = File.createTempFile("senku-source-summary", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                return "";
            }
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "What is the safest way to carry a battery in stormy weather?",
            List.of(
                new SearchResult(
                    "Battery Transport",
                    "",
                    "Keep spare batteries dry and isolate terminals during transport.",
                    "Keep batteries in a dry container and cover terminals so they cannot short.",
                    "GD-207",
                    "Battery storm transport",
                    "power",
                    "guide-focus"
                )
            ),
            false,
            System.currentTimeMillis() - 1000L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertEquals(OfflineAnswerEngine.AnswerMode.CONFIDENT, answerRun.mode);
        assertFalse(answerRun.abstain);
        assertEquals(1, answerRun.sources.size());
        assertEquals("GD-207", answerRun.sources.get(0).guideId);
        assertTrue(answerRun.answerBody.contains("Short answer:"));
        assertTrue(answerRun.answerBody.contains("Keep batteries in a dry container"));
        assertEquals(1, finalModeLines.get().size());
        assertTrue(
            Pattern.compile(
                "^ask\\.generate final_mode=confident route=source_summary_fallback query=\"What is the safest way to carry a battery in stormy weather\\?\" totalElapsedMs=\\d+$"
            ).matcher(finalModeLines.get().get(0)).matches()
        );
    }

    @Test
    public void generateNoSourceEmptyAnswerDowngradesInsteadOfConfidentSourceFallback() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        File tempModel = File.createTempFile("senku-no-source-fallback", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> ""
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "What is the safest way to carry a battery in stormy weather?",
            List.of(),
            false,
            System.currentTimeMillis() - 1000L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertEquals(OfflineAnswerEngine.AnswerMode.ABSTAIN, answerRun.mode);
        assertTrue(answerRun.abstain);
        assertTrue(answerRun.sources.isEmpty());
        assertTrue(answerRun.answerBody.contains("Senku doesn't have a guide"));
        assertEquals(1, finalModeLines.get().size());
        assertTrue(
            Pattern.compile(
                "^ask\\.generate final_mode=abstain route=low_coverage_downgrade query=\"What is the safest way to carry a battery in stormy weather\\?\" totalElapsedMs=\\d+$"
            ).matcher(finalModeLines.get().get(0)).matches()
        );
    }

    @Test
    public void generateNoSourceGenericAnswerDowngradesOnDeviceInsteadOfConfident() throws Exception {
        File tempModel = File.createTempFile("senku-no-source-generic", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> "No specific information available."
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How should I treat a minor sunburn?",
            List.of(),
            false,
            System.currentTimeMillis() - 1000L,
            false,
            "",
            "",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertEquals(OfflineAnswerEngine.AnswerMode.ABSTAIN, answerRun.mode);
        assertTrue(answerRun.abstain);
        assertTrue(answerRun.sources.isEmpty());
        assertFalse(answerRun.hostBackendUsed);
        assertFalse(answerRun.hostFallbackUsed);
        assertTrue(answerRun.answerBody.contains("Senku doesn't have a guide"));
    }

    @Test
    public void generateNoSourceSubstantiveHostAnswerDowngradesInsteadOfConfident() throws Exception {
        List<String> finalModeLines = captureFinalModeLines();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) ->
                new HostInferenceClient.Result(
                    "Short answer: Rinse the area, cool it with clean water, cover it loosely, and watch for worsening symptoms.",
                    "mock-host",
                    0.55
                ),
            (context, modelFile, prompt, maxTokens, listener) -> {
                throw new AssertionError("on-device generation should not run");
            }
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How should I treat a minor burn?",
            List.of(),
            false,
            System.currentTimeMillis() - 1000L,
            true,
            "http://127.0.0.1:9/v1",
            "gemma-4-e2b-it-litert",
            "system",
            "prompt"
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, null, prepared);

        assertEquals(OfflineAnswerEngine.AnswerMode.ABSTAIN, answerRun.mode);
        assertTrue(answerRun.abstain);
        assertTrue(answerRun.sources.isEmpty());
        assertTrue(answerRun.hostBackendUsed);
        assertFalse(answerRun.hostFallbackUsed);
        assertTrue(answerRun.answerBody.contains("Senku doesn't have a guide"));
        assertEquals(1, finalModeLines.size());
        assertTrue(finalModeLines.get(0).contains("final_mode=abstain route=low_coverage_downgrade"));
    }

    @Test
    public void generateDeterministicNoSourceAnswerDoesNotEnterNoSourceDowngrade() throws Exception {
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                throw new AssertionError("device generation should not run");
            }
        );
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
        assertFalse(answerRun.abstain);
        assertEquals(OfflineAnswerEngine.AnswerMode.CONFIDENT, answerRun.mode);
        assertTrue(answerRun.sources.isEmpty());
        assertTrue(answerRun.answerBody.contains("Use only known food-safe bottles"));
    }

    @Test
    public void generateEachTerminalReturnEmitsExactlyOneFinalModeLine() throws Exception {
        AtomicReference<List<String>> finalModeLines = new AtomicReference<>(new ArrayList<>());
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.get().add(message);
            }
        });
        File tempModel = File.createTempFile("senku-terminal-regression", ".litertlm");
        tempModel.deleteOnExit();
        Pattern finalModePattern = Pattern.compile(
            "^ask\\.generate final_mode=([^ ]+) route=([^ ]+) query=\".*\" totalElapsedMs=\\d+$"
        );
        List<String> expectedRoutes = List.of(
            "deterministic",
            "early_abstain",
            "early_uncertain_fit",
            "low_coverage_downgrade",
            "confident_generation",
            "source_summary_fallback"
        );
        List<String> seenRoutes = new ArrayList<>();
        List<String> seenModes = new ArrayList<>();

        OfflineAnswerEngine.PreparedAnswer deterministicPrepared = OfflineAnswerEngine.PreparedAnswer.restoredDeterministic(
            "how do i pack light on trail?",
            "Use lightweight items first.",
            List.of(),
            true,
            "trail_pack",
            System.currentTimeMillis() - 300L
        );
        int before = finalModeLines.get().size();
        OfflineAnswerEngine.generate(null, null, deterministicPrepared);
        assertEquals(before + 1, finalModeLines.get().size());

        OfflineAnswerEngine.PreparedAnswer abstainPrepared = OfflineAnswerEngine.PreparedAnswer.abstain(
            "how do i build a rain shelter from a tarp",
            OfflineAnswerEngine.buildAbstainAnswerBody(
                "how do i build a rain shelter from a tarp",
                List.of()
            ),
            List.of(),
            false
        );
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                throw new AssertionError("device generation should not run");
            }
        );
        before = finalModeLines.get().size();
        OfflineAnswerEngine.generate(null, null, abstainPrepared);
        assertEquals(before + 1, finalModeLines.get().size());

        OfflineAnswerEngine.PreparedAnswer uncertainFitPrepared = OfflineAnswerEngine.PreparedAnswer.uncertainFit(
            "he has barely slept, keeps pacing, and says normal rules do not apply to him",
            OfflineAnswerEngine.buildUncertainFitAnswerBody(
                "he has barely slept, keeps pacing, and says normal rules do not apply to him",
                List.of(),
                OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
                true
            ),
            List.of(),
            false,
            System.currentTimeMillis() - 250L,
            12L,
            4L,
            0L,
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            true
        );
        before = finalModeLines.get().size();
        OfflineAnswerEngine.generate(null, null, uncertainFitPrepared);
        assertEquals(before + 1, finalModeLines.get().size());

        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                return "No specific information available.";
            }
        );
        OfflineAnswerEngine.PreparedAnswer lowCoveragePrepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How should i treat a minor sunburn?",
            List.of(),
            false,
            System.currentTimeMillis() - 1800L,
            false,
            "",
            "",
            "system",
            "prompt"
        );
        before = finalModeLines.get().size();
        OfflineAnswerEngine.generate(null, tempModel, lowCoveragePrepared);
        assertEquals(before + 1, finalModeLines.get().size());

        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                listener.onPartialText("Short answer: confidence path.");
                return "Short answer: confidence path.";
            }
        );
        OfflineAnswerEngine.PreparedAnswer confidentPrepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How do i make a simple water filter?",
            List.of(
                new SearchResult(
                    "Water Filtration",
                    "",
                    "Layer cloth, sand, charcoal, and gravel to filter debris before disinfecting water.",
                    "Use a filter only as a debris step, then boil or disinfect the water before drinking.",
                    "GD-201",
                    "Simple water filter",
                    "water",
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
        before = finalModeLines.get().size();
        OfflineAnswerEngine.generate(null, tempModel, confidentPrepared);
        assertEquals(before + 1, finalModeLines.get().size());

        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                return "";
            }
        );
        OfflineAnswerEngine.PreparedAnswer sourceSummaryPrepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            "How do i maintain a fire in wind?",
            List.of(
                new SearchResult(
                    "Fire Management",
                    "",
                    "Shield small flames from wind and keep dry tinder close.",
                    "Use a windbreak, keep tinder dry, and feed fuel gradually.",
                    "GD-122",
                    "Wind fire management",
                    "fire",
                    "guide-focus"
                )
            ),
            false,
            System.currentTimeMillis() - 1000L,
            false,
            "",
            "",
            "system",
            "prompt"
        );
        before = finalModeLines.get().size();
        OfflineAnswerEngine.generate(null, tempModel, sourceSummaryPrepared);
        assertEquals(before + 1, finalModeLines.get().size());

        assertEquals(6, finalModeLines.get().size());
        for (String line : finalModeLines.get()) {
            java.util.regex.Matcher matcher = finalModePattern.matcher(line);
            assertTrue(matcher.matches());
            String finalMode = matcher.group(1);
            String route = matcher.group(2);
            seenRoutes.add(route);
            seenModes.add(finalMode);
        }

        for (String expected : expectedRoutes) {
            int routeCount = 0;
            for (String route : seenRoutes) {
                if (expected.equals(route)) {
                    routeCount++;
                }
            }
            assertEquals(1, routeCount);
        }

        for (int index = 0; index < seenRoutes.size(); index++) {
            String route = seenRoutes.get(index);
            String mode = seenModes.get(index);
            if ("deterministic".equals(route)) {
                assertEquals("confident", mode);
            } else if ("early_abstain".equals(route)) {
                assertEquals("abstain", mode);
            } else if ("early_uncertain_fit".equals(route)) {
                assertEquals("uncertain_fit", mode);
            } else if ("low_coverage_downgrade".equals(route)) {
                assertTrue("abstain".equals(mode) || "uncertain_fit".equals(mode));
            } else if ("confident_generation".equals(route)) {
                assertEquals("confident", mode);
            } else if ("source_summary_fallback".equals(route)) {
                assertEquals("confident", mode);
            } else {
                assertEquals("unexpected route: " + route, "", route);
            }
        }
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
    public void broadWaterStoragePromptContextPrefersStorageRotationOverBleachHeavySupport() {
        String query = "what's the safest way to store treated water long term";
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);

        List<SearchResult> promptContext = OfflineAnswerEngine.shapePromptContextResults(
            query,
            metadataProfile,
            List.of(
                new SearchResult(
                    "Storage & Material Management",
                    "",
                    "Use sealed food-safe containers in a dark, cool place.",
                    "Use sealed food-safe containers in a dark, cool place.",
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
                    "Storage & Material Management",
                    "",
                    "Label dates and rotate stored water with a FIFO schedule.",
                    "Label dates and rotate stored water with a FIFO schedule.",
                    "GD-252",
                    "Rotation Schedules: FIFO Implementation & Discipline",
                    "resource-management",
                    "guide-focus",
                    "safety",
                    "long_term",
                    "water_storage",
                    "water_storage,water_rotation"
                ),
                new SearchResult(
                    "Water Storage & Purification",
                    "",
                    "Add bleach drops, re-dose chlorine, and improve taste by aerating.",
                    "Add bleach drops, re-dose chlorine, and improve taste by aerating.",
                    "GD-373",
                    "Chemical Treatment & Bleach Re-Dosing",
                    "utility",
                    "hybrid",
                    "safety",
                    "long_term",
                    "water_storage",
                    "water_storage,container_sanitation"
                ),
                new SearchResult(
                    "Storage & Material Management",
                    "",
                    "Keep chemical storage separate from potable water containers.",
                    "Keep chemical storage separate from potable water containers.",
                    "GD-252",
                    "Chemical Storage: Hazard Management",
                    "resource-management",
                    "guide-focus",
                    "safety",
                    "long_term",
                    "water_storage",
                    "water_storage"
                )
            ),
            OfflineAnswerEngine.promptContextLimitFor(query)
        );

        assertEquals(2, promptContext.size());
        assertEquals("Water Storage: Hydration Assurance", promptContext.get(0).sectionHeading);
        assertEquals("Rotation Schedules: FIFO Implementation & Discipline", promptContext.get(1).sectionHeading);
    }

    @Test
    public void broadWaterStoragePromptContextDoesNotRewriteDistributionStorageTankPrompt() {
        String query = "storage tanks water distribution water storage";
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system what about storage tanks"
        );
        List<SearchResult> original = List.of(
            new SearchResult(
                "Community Water Distribution Systems",
                "",
                "Start with tower height and layout.",
                "Start with tower height and layout.",
                "GD-270",
                "Water Tower Construction & Sizing",
                "building",
                "guide-focus",
                "subsystem",
                "long_term",
                "water_storage",
                "water_storage,water_distribution"
            ),
            new SearchResult(
                "Storage & Material Management",
                "",
                "Use sealed food-safe containers in a dark, cool place.",
                "Use sealed food-safe containers in a dark, cool place.",
                "GD-252",
                "Water Storage: Hydration Assurance",
                "resource-management",
                "guide-focus",
                "safety",
                "long_term",
                "water_storage",
                "water_storage,container_sanitation,water_rotation"
            )
        );

        List<SearchResult> promptContext = OfflineAnswerEngine.shapePromptContextResults(
            query,
            metadataProfile,
            original,
            OfflineAnswerEngine.promptContextLimitFor(query)
        );

        assertEquals(original, promptContext);
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
    public void buildUncertainFitAnswerBodyAddsSafetyCriticalEscalationOnlyWhenFlagged() {
        List<SearchResult> adjacentGuides = List.of(
            new SearchResult(
                "Barely Slept Pacing Notes",
                "",
                "",
                "",
                "GD-305",
                "Normal rules observation",
                "mental-health",
                "vector"
            ),
            new SearchResult(
                "Calm Down or Escalate Checklist",
                "",
                "",
                "",
                "GD-306",
                "Pacing observer steps",
                "mental-health",
                "lexical"
            )
        );
        Object[][] cases = new Object[][]{
            {"he has barely slept, keeps pacing, and says normal rules do not apply to him", true},
            {"how do i repair a tarp shelter after wind damage", false},
        };

        for (Object[] testCase : cases) {
            String query = (String) testCase[0];
            boolean expectEscalation = (Boolean) testCase[1];
            String answerBody = OfflineAnswerEngine.buildUncertainFitAnswerBody(
                query,
                adjacentGuides,
                OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
                OfflineAnswerEngine.isSafetyCriticalQuery(query, adjacentGuides)
            );

            boolean hasEscalation = answerBody.contains(OfflineAnswerEngine.SAFETY_CRITICAL_ESCALATION_LINE);
            assertEquals(query, expectEscalation, hasEscalation);
            if (expectEscalation) {
                assertTrue(
                    query,
                    answerBody.indexOf(OfflineAnswerEngine.SAFETY_CRITICAL_ESCALATION_LINE)
                        < answerBody.indexOf("Possibly relevant guides in the library:")
                );
            }
        }
    }

    @Test
    public void buildUncertainFitAnswerBodyDoesNotUseReviewRainShelterFallbackByDefault() {
        String answerBody = OfflineAnswerEngine.buildUncertainFitAnswerBody(
            "How do I build a simple rain shelter from tarp and cord?",
            List.of(
                new SearchResult(
                    "Primitive Shelter Construction Techniques",
                    "",
                    "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                    "",
                    "GD-345",
                    "Wood Quality Evaluation for Shelter Construction",
                    "survival",
                    "lexical",
                    "",
                    "",
                    "emergency_shelter",
                    "foundation,weatherproofing,site_selection"
                )
            ),
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            false
        );

        assertTrue(answerBody.startsWith("Senku found guides that may be relevant"));
        assertFalse(answerBody.contains("Build a ridgeline first"));
        assertTrue(answerBody.contains("Possibly relevant guides in the library:"));
        assertTrue(answerBody.contains("Try:"));
    }

    @Test
    public void rainShelterUncertainFitSourcesKeepRetrievedIdentityByDefault() {
        List<SearchResult> sources = OfflineAnswerEngine.shapeUncertainFitSourcesForPresentation(
            "How do I build a simple rain shelter from tarp and cord?",
            List.of(
                new SearchResult(
                    "Primitive Shelter Construction Techniques",
                    "",
                    "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                    "",
                    "GD-345",
                    "Wood Quality Evaluation for Shelter Construction",
                    "survival",
                    "lexical",
                    "",
                    "",
                    "emergency_shelter",
                    "foundation,weatherproofing,site_selection"
                ),
                new SearchResult(
                    "Primitive Shelter Construction Techniques",
                    "",
                    "Pile leaves and boughs over a small frame.",
                    "",
                    "GD-345",
                    "Debris Hut (Emergency Shelter)",
                    "survival",
                    "lexical"
                )
            ),
            false
        );

        assertEquals(1, sources.size());
        assertEquals("GD-345", sources.get(0).guideId);
        assertEquals("Primitive Shelter Construction Techniques", sources.get(0).title);
        assertEquals("Wood Quality Evaluation for Shelter Construction", sources.get(0).sectionHeading);
    }

    @Test
    public void uncertainFitSourcesDedupeRepeatedGuideSectionsBeforePresentation() {
        SearchResult first = new SearchResult(
            "Tarp & Cord Shelters",
            "",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
            "",
            "GD-345",
            "Tarp & Cord Shelters",
            "shelter",
            "lexical"
        );
        SearchResult duplicate = new SearchResult(
            "Tarp & Cord Shelters",
            "",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
            "",
            "GD-345",
            "Tarp & Cord Shelters",
            "shelter",
            "hybrid"
        );
        SearchResult distinctSection = new SearchResult(
            "Tarp & Cord Shelters",
            "",
            "Stake the windward edge low.",
            "",
            "GD-345",
            "Windward Pitch",
            "shelter",
            "lexical"
        );

        List<SearchResult> sources = OfflineAnswerEngine.shapeUncertainFitSourcesForPresentation(
            "How do I build a simple rain shelter from tarp and cord?",
            List.of(first, duplicate, distinctSection),
            false
        );
        String answerBody = OfflineAnswerEngine.buildUncertainFitAnswerBody(
            "How do I build a simple rain shelter from tarp and cord?",
            List.of(first, duplicate, distinctSection),
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            false
        );

        assertEquals(List.of(first), sources);
        assertEquals(
            answerBody.indexOf("Tarp & Cord Shelters — Tarp & Cord Shelters"),
            answerBody.lastIndexOf("Tarp & Cord Shelters — Tarp & Cord Shelters")
        );
    }

    @Test
    public void buildUncertainFitAnswerBodyDoesNotInventRainShelterStepsWithoutSources() {
        String answerBody = OfflineAnswerEngine.buildUncertainFitAnswerBody(
            "How do I build a simple rain shelter from tarp and cord?",
            List.of(),
            OfflineAnswerEngine.ConfidenceLabel.LOW,
            false
        );

        assertTrue(answerBody.startsWith("Senku found only loosely related guides"));
        assertFalse(answerBody.contains("Build a ridgeline first"));
        assertTrue(answerBody.contains("Try:"));
    }

    @Test
    public void reviewerWorkedPromptRoutesToUncertainFitInsteadOfAbstain() {
        String query =
            "He has barely slept, keeps pacing, says normal rules do not apply to him. "
                + "Is this just stress, or should I help him calm down?";
        List<SearchResult> adjacentGuides = List.of(
            new SearchResult(
                "Barely Slept Pacing Notes",
                "",
                "",
                "",
                "GD-305",
                "Normal rules observation",
                "mental-health",
                "vector"
            ),
            new SearchResult(
                "Calm Down or Escalate Checklist",
                "",
                "",
                "",
                "GD-306",
                "Pacing observer steps",
                "mental-health",
                "lexical"
            )
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);

        assertFalse(OfflineAnswerEngine.shouldAbstain(adjacentGuides, query));
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                adjacentGuides,
                query,
                metadataProfile,
                OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
                true
            )
        );
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
        assertEquals(OfflineAnswerEngine.AnswerMode.ABSTAIN, answerRun.mode);
        assertEquals(OfflineAnswerEngine.ConfidenceLabel.LOW, answerRun.confidenceLabel);
        assertEquals(1, answerRun.sources.size());
        assertEquals("GD-102", answerRun.sources.get(0).guideId);
        assertTrue(answerRun.answerBody.contains("Senku doesn't have a guide"));
        assertEquals("", hostCall.get());
        assertEquals("", deviceCall.get());
        assertEquals("No guide match. Try rephrasing.", OfflineAnswerEngine.buildCompletionStatus(answerRun));
    }

    @Test
    public void generateReturnsUncertainFitWithoutCallingGenerators() throws Exception {
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
                "Behavior Supervision Notes",
                "",
                "",
                "",
                "GD-305",
                "Observation",
                "mental-health",
                "vector"
            )
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.uncertainFit(
            "he has barely slept, keeps pacing, and says normal rules do not apply to him",
            OfflineAnswerEngine.buildUncertainFitAnswerBody(
                "he has barely slept, keeps pacing, and says normal rules do not apply to him",
                adjacentGuides,
                OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
                true
            ),
            adjacentGuides,
            false,
            System.currentTimeMillis() - 250L,
            12L,
            4L,
            0L,
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            true
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, null, prepared);

        assertEquals(OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT, answerRun.mode);
        assertEquals(OfflineAnswerEngine.ConfidenceLabel.MEDIUM, answerRun.confidenceLabel);
        assertEquals(1, answerRun.sources.size());
        assertEquals("GD-305", answerRun.sources.get(0).guideId);
        assertFalse(answerRun.abstain);
        assertFalse(answerRun.deterministic);
        assertTrue(answerRun.answerBody.contains("not a confident fit"));
        assertEquals("", hostCall.get());
        assertEquals("", deviceCall.get());
        assertEquals("Related guides ready. Verify the fit.", OfflineAnswerEngine.buildCompletionStatus(answerRun));
    }

    @Test
    public void runRejectsBlankQueryBeforePackModelOrGenerators() throws Exception {
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

        try {
            OfflineAnswerEngine.run(null, null, null, null, "   ");
        } catch (IllegalArgumentException exception) {
            assertEquals("Enter a question first", exception.getMessage());
            assertEquals("", hostCall.get());
            assertEquals("", deviceCall.get());
            return;
        }

        fail("blank query should reject before any backend setup");
    }

    @Test
    public void runRejectsMissingPackBeforeModelGateOrGenerators() throws Exception {
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

        try {
            OfflineAnswerEngine.run(
                null,
                null,
                new SessionMemory(),
                null,
                "how do i build a rain shelter?"
            );
        } catch (IllegalStateException exception) {
            assertEquals("Pack is not ready yet", exception.getMessage());
            assertEquals("", hostCall.get());
            assertEquals("", deviceCall.get());
            return;
        }

        fail("missing pack should reject before model availability");
    }

    @Test
    public void generateRejectsNullPreparedAnswerBeforeGenerators() throws Exception {
        AtomicReference<String> hostCall = new AtomicReference<>("");
        AtomicReference<String> deviceCall = new AtomicReference<>("");
        File tempModel = File.createTempFile("senku-null-prepared", ".litertlm");
        tempModel.deleteOnExit();
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

        try {
            OfflineAnswerEngine.generate(null, tempModel, null);
        } catch (IllegalArgumentException exception) {
            assertEquals("Prepared answer is required", exception.getMessage());
            assertEquals("", hostCall.get());
            assertEquals("", deviceCall.get());
            return;
        }

        fail("null prepared answer should reject before generation");
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
    public void resolveAnswerModePrefersSelectedContextOverOffTopicRawTopRowsAndKeepsRainShelterUncertainFit() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        List<SearchResult> rawTopChunks = List.of(
            new SearchResult(
                "Batteries & Energy Storage: Principles & Simple Applications",
                "",
                "Charging and maintenance basics for salvaged cells.",
                "Battery banks, lead acid maintenance, and charging safety.",
                "GD-727",
                "Practical Survival Applications",
                "utility",
                "hybrid"
            ),
            new SearchResult(
                "Education System Design",
                "",
                "Schoolhouse management and mixed-age planning.",
                "Lesson rotation, desks, and class planning.",
                "GD-653",
                "One-Room Schoolhouse & Mixed-Age Classroom Management",
                "education",
                "hybrid"
            ),
            new SearchResult(
                "Cave Shelter Systems and Long-Term Habitation",
                "",
                "Natural shelter options and drainage.",
                "Choose shelter placement with runoff control and weather protection.",
                "GD-294",
                "Cave Selection Criteria",
                "survival",
                "hybrid",
                "starter",
                "immediate",
                "emergency_shelter",
                "shelter"
            )
        );
        List<SearchResult> selectedContext = List.of(
            new SearchResult(
                "Cave Shelter Systems and Long-Term Habitation",
                "",
                "Use tarp panels and cord lashings to keep rain off the sleeping area.",
                "A simple rain shelter can use a ridgeline, tarp angle, cord lashings, and drainage so runoff stays away from the sleeping area.",
                "GD-294",
                "Rain runoff and tarp placement",
                "survival",
                "guide-focus",
                "starter",
                "immediate",
                "emergency_shelter",
                "tarp_shelter,weatherproofing"
            ),
            new SearchResult(
                "Cave Shelter Systems and Long-Term Habitation",
                "",
                "Cord tie-downs keep the tarp stable in wind and rain.",
                "Secure the shelter with cord, tighten the ridgeline, and angle the tarp so rain sheds cleanly.",
                "GD-294",
                "Cord lashings and storm tension",
                "survival",
                "guide-focus",
                "starter",
                "immediate",
                "emergency_shelter",
                "tarp_shelter,weatherproofing"
            ),
            new SearchResult(
                "Shelter basics",
                "",
                "Simple tarp shelter setup notes.",
                "For a quick shelter, tie cord between anchors, drape the tarp, and pitch one side low for rain runoff.",
                "GD-933",
                "Simple tarp shelter setup",
                "survival",
                "lexical",
                "starter",
                "immediate",
                "emergency_shelter",
                "tarp_shelter"
            )
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel = OfflineAnswerEngine.confidenceLabel(
            selectedContext,
            query,
            metadataProfile
        );

        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile,
                confidenceLabel,
                false
            )
        );
    }

    @Test
    public void rankedResultsWithMetadataMatchedVectorSurviveSelectedContextAndShapeAnswerMode() {
        String query = "how do i repair a tarp shelter after wind damage";
        SearchResult anchor = new SearchResult(
            "Cave Shelter Systems and Long-Term Habitation",
            "",
            "Pick high ground and route runoff away from the sleeping area.",
            "Pick high ground and route runoff away from the sleeping area.",
            "GD-294",
            "Rain runoff and tarp placement",
            "survival",
            "lexical",
            "starter",
            "immediate",
            "emergency_shelter",
            "tarp_shelter,weatherproofing"
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
        SearchResult vectorRepair = new SearchResult(
            "Tarp field notes",
            "",
            "Wind damage can tear tie-out points and seams.",
            "Wind damage can tear tie-out points and seams. Patch the tarp, re-tension the ridgeline, and reset runoff angle after storms.",
            "GD-112",
            "Quick patches",
            "survival",
            "vector",
            "starter",
            "immediate",
            "emergency_shelter",
            "tarp_shelter,repair"
        );

        List<SearchResult> selectedContext = PackAnswerContextPolicy.rankSupportCandidatesForTest(
            query,
            anchor,
            List.of(anchor, offTopicLexical, vectorRepair)
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel = OfflineAnswerEngine.confidenceLabel(
            selectedContext,
            query,
            metadataProfile
        );

        assertTrue(containsGuideId(selectedContext, "GD-112"));
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                selectedContext,
                query,
                metadataProfile,
                confidenceLabel,
                false
            )
        );
    }

    @Test
    public void hybridRawRowsDoNotBlockAbstainWithoutGroundedSelectedContext() {
        String query = "how do i tune a violin bridge and soundpost";
        List<SearchResult> rawTopChunks = List.of(
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Suspension bridge load distribution.",
                "Bridge span load calculations, deck tension, and cable support.",
                "GD-110",
                "Suspension Bridges",
                "engineering",
                "hybrid"
            ),
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Bridge stress testing in the field.",
                "Load rating and structural bridge inspection routines.",
                "GD-110",
                "Field Load Testing and Structural Assessment",
                "engineering",
                "hybrid"
            ),
            new SearchResult(
                "Dental Prosthetics & Denture Making",
                "",
                "Repairing a dental bridge in low-resource settings.",
                "Dental bridge stabilization and tooth replacement steps.",
                "GD-061",
                "Dental Bridge Improvisation",
                "medical",
                "hybrid"
            )
        );
        List<SearchResult> selectedContext = List.of(
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Bridge load tables and span measurements.",
                "Bridge abutments, load paths, and span calculations for timber crossings.",
                "GD-110",
                "Bridge Load Calculations",
                "engineering",
                "guide-focus"
            ),
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Aqueducts and water conveyance structures.",
                "Bridge piers, aqueduct supports, and channel alignment.",
                "GD-110",
                "Aqueducts & Water Conveyance",
                "engineering",
                "guide-focus"
            ),
            new SearchResult(
                "Dental Prosthetics & Denture Making",
                "",
                "Improvised dental bridge notes.",
                "Dental bridge shaping and mouth-fit checks.",
                "GD-061",
                "Dental Bridge Improvisation",
                "medical",
                "hybrid"
            )
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);
        OfflineAnswerEngine.AnswerModeStage stage = OfflineAnswerEngine.buildAnswerModeStage(
            query,
            query,
            metadataProfile,
            rawTopChunks,
            selectedContext
        );

        assertEquals(3, stage.modeCandidates.size());
        assertEquals("GD-110", stage.modeCandidates.get(0).guideId);
        assertEquals(3, stage.gateContext.size());
        assertEquals("GD-110", stage.gateContext.get(0).guideId);
        assertEquals(OfflineAnswerEngine.AnswerMode.ABSTAIN, stage.answerMode);

        assertTrue(
            OfflineAnswerEngine.shouldAbstain(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile
            )
        );
        assertEquals(
            OfflineAnswerEngine.AnswerMode.ABSTAIN,
            OfflineAnswerEngine.resolveAnswerMode(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile,
                OfflineAnswerEngine.confidenceLabel(selectedContext, query, metadataProfile),
                false
            )
        );
    }

    @Test
    public void generateDowngradesLowCoverageAnswerToAbstainRoute() throws Exception {
        File tempModel = File.createTempFile("senku-low-coverage", ".litertlm");
        tempModel.deleteOnExit();
        OfflineAnswerEngine.setGeneratorsForTest(
            (settings, systemPrompt, prompt, maxTokens) -> {
                throw new AssertionError("host generation should not run");
            },
            (context, modelFile, prompt, maxTokens, listener) -> {
                String lowCoverage = "The retrieved notes do not contain information about violin soundpost adjustment.";
                listener.onPartialText(lowCoverage);
                return lowCoverage;
            }
        );

        String query = "how do i tune a violin bridge and soundpost";
        List<SearchResult> promptSources = List.of(
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Bridge load tables and span measurements.",
                "Bridge abutments, load paths, and span calculations for timber crossings.",
                "GD-110",
                "Bridge Load Calculations",
                "engineering",
                "guide-focus"
            ),
            new SearchResult(
                "Bridges, Dams & Infrastructure",
                "",
                "Aqueducts and water conveyance structures.",
                "Bridge piers, aqueduct supports, and channel alignment.",
                "GD-110",
                "Aqueducts & Water Conveyance",
                "engineering",
                "guide-focus"
            )
        );
        OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            query,
            promptSources,
            false,
            System.currentTimeMillis() - 1200L,
            false,
            "",
            "",
            "system",
            "prompt",
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM
        );

        OfflineAnswerEngine.AnswerRun answerRun = OfflineAnswerEngine.generate(null, tempModel, prepared);

        assertTrue(answerRun.abstain);
        assertEquals(OfflineAnswerEngine.AnswerMode.ABSTAIN, answerRun.mode);
        assertTrue(answerRun.answerBody.contains("Senku doesn't have a guide"));
        assertTrue(answerRun.subtitle.startsWith("Low coverage |"));
    }

    @Test
    public void drowningResuscitationRegressionStaysUncertainFit() {
        String query = "What are the steps for drowning rescue and resuscitation?";
        List<SearchResult> rawTopChunks = List.of(
            new SearchResult(
                "Cold Water Survival & Immersion Hypothermia",
                "",
                "Immersion survival positions and rescue posture.",
                "Cold-water rescue positioning and survival posture.",
                "GD-396",
                "Immersion Survival Positions",
                "survival",
                "hybrid"
            ),
            new SearchResult(
                "Arctic Survival & Boreal Adaptation",
                "",
                "Frostbite prevention and treatment.",
                "Cold exposure sheltering and frostbite response.",
                "GD-445",
                "Frostbite Prevention and Treatment",
                "survival",
                "hybrid"
            ),
            new SearchResult(
                "Cold Water Survival & Immersion Hypothermia",
                "",
                "Cold-water rescue techniques.",
                "Rescue approach and recovery from cold water.",
                "GD-396",
                "Cold Water Rescue Techniques",
                "survival",
                "hybrid"
            )
        );
        List<SearchResult> selectedContext = List.of(
            new SearchResult(
                "Cold Water Survival & Immersion Hypothermia",
                "",
                "Cold-water rescue techniques.",
                "Rescue approach, flotation support, and extraction from the water.",
                "GD-396",
                "Cold Water Rescue Techniques",
                "survival",
                "guide-focus"
            ),
            new SearchResult(
                "Cold Water Survival & Immersion Hypothermia",
                "",
                "Near-drowning follow-up checks.",
                "Drowning follow-up signs and delayed symptom watch after extraction.",
                "GD-396",
                "Near-Drowning and Secondary Drowning",
                "survival",
                "guide-focus"
            ),
            new SearchResult(
                "Coastal Navigation and Rescue Signaling",
                "",
                "Calling and signaling for rescue.",
                "Rescue signaling and team coordination near water.",
                "GD-475",
                "Rescue Signaling",
                "survival",
                "lexical"
            )
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);

        assertFalse(
            OfflineAnswerEngine.shouldAbstain(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile
            )
        );
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile,
                OfflineAnswerEngine.confidenceLabel(selectedContext, query, metadataProfile),
                false
            )
        );
    }

    @Test
    public void abstainRawTopSemanticSupportUsesDedicatedVectorFloor() {
        String query = "How do I build a cabin roof that sheds rain?";
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);
        List<SearchResult> selectedContext = List.of(
            new SearchResult(
                "Cabin Roofing and Weatherproofing",
                "",
                "Cabin roof framing and weatherproofing notes.",
                "Build the cabin roof with water-shedding pitch, rainproof layers, and roof edge drainage.",
                "GD-410",
                "Roof framing and rainproofing",
                "building",
                "guide-focus",
                "planning",
                "long_term",
                "cabin_house",
                "roofing,weatherproofing"
            )
        );
        List<SearchResult> belowFloorRawTopChunks = List.of(
            new SearchResult(
                "Cabin Roof Repair",
                "",
                "Cabin roof repair notes for rain shedding.",
                "Reset roof pitch, repair weatherproof layers, and keep runoff moving off the cabin walls.",
                "GD-410",
                "Roof Weatherproofing",
                "building",
                "vector",
                "planning",
                "long_term",
                "cabin_house",
                "roofing,weatherproofing"
            )
        );
        List<SearchResult> aboveFloorRawTopChunks = List.of(
            new SearchResult(
                "Cabin Roof Repair",
                "",
                "Cabin roof repair notes for rain shedding.",
                "Reset roof pitch, repair weatherproof layers, and keep runoff moving off the cabin walls.",
                "GD-410",
                "Roof Weatherproofing",
                "building",
                "hybrid",
                "planning",
                "long_term",
                "cabin_house",
                "roofing,weatherproofing"
            )
        );

        assertFalse(
            OfflineAnswerEngine.hasAbstainRawTopSemanticSupport(
                selectedContext,
                belowFloorRawTopChunks,
                query,
                metadataProfile
            )
        );
        assertTrue(
            OfflineAnswerEngine.hasAbstainRawTopSemanticSupport(
                selectedContext,
                aboveFloorRawTopChunks,
                query,
                metadataProfile
            )
        );
    }

    @Test
    public void maniaEscalationRegressionStaysUncertainFit() {
        String query =
            "He has barely slept, keeps pacing, and says normal rules do not apply to him. "
                + "Is this just stress, or should I help him calm down?";
        List<SearchResult> rawTopChunks = List.of(
            new SearchResult(
                "Emergency Dental Procedures",
                "",
                "Rapid chairside triage.",
                "Acute dental triage and stabilization steps.",
                "GD-061",
                "Dental Emergency Intake",
                "medical",
                "hybrid"
            ),
            new SearchResult(
                "Behavior Supervision Notes",
                "",
                "Pacing observation cues.",
                "Observation notes for pacing and agitation.",
                "GD-305",
                "Observation",
                "mental-health",
                "vector"
            ),
            new SearchResult(
                "Calm Down or Escalate Checklist",
                "",
                "Escalation markers for observers.",
                "When pacing worsens, move to escalation support.",
                "GD-306",
                "Pacing observer steps",
                "mental-health",
                "lexical"
            )
        );
        List<SearchResult> selectedContext = List.of(
            new SearchResult(
                "Behavior Supervision Notes",
                "",
                "Barely slept, pacing, and observation cues.",
                "Barely slept, pacing, and sudden behavior changes can need urgent support.",
                "GD-305",
                "Normal rules observation",
                "mental-health",
                "vector"
            ),
            new SearchResult(
                "Calm Down or Escalate Checklist",
                "",
                "Pacing observer steps and calming support.",
                "Pacing, calm voice, and when to move from calming support to urgent escalation.",
                "GD-306",
                "Pacing observer steps",
                "mental-health",
                "lexical"
            )
        );
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);

        assertFalse(
            OfflineAnswerEngine.shouldAbstain(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile
            )
        );
        assertEquals(
            OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            OfflineAnswerEngine.resolveAnswerMode(
                selectedContext,
                rawTopChunks,
                query,
                metadataProfile,
                OfflineAnswerEngine.confidenceLabel(selectedContext, query, metadataProfile),
                true
            )
        );
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

    private static List<String> captureFinalModeLines() {
        ArrayList<String> finalModeLines = new ArrayList<>();
        OfflineAnswerEngine.setDebugLogSinkForTest((tag, message) -> {
            if ("SenkuMobile".equals(tag) && message.startsWith("ask.generate final_mode=")) {
                finalModeLines.add(message);
            }
        });
        return finalModeLines;
    }

    private static boolean containsGuideId(List<SearchResult> results, String guideId) {
        for (SearchResult result : results) {
            if (guideId.equals(result.guideId)) {
                return true;
            }
        }
        return false;
    }
}

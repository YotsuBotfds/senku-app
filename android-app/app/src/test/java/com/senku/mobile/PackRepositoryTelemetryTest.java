package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class PackRepositoryTelemetryTest {
    @Test
    public void searchResultTelemetryLineIncludesGuideIdsAndScores() {
        List<SearchResult> results = Arrays.asList(
            searchResult("GD-345", "Debris Huts", "emergency_shelter", "survival", "shelter,debris-hut"),
            searchResult("GD-727", "Practical Survival Apps", "general", "utility", "maintenance,storage")
        );

        String line = buildSearchResultCandidateTelemetryLine(
            "lexical",
            "how do i build a rain shelter",
            results,
            Arrays.asList(38.1, 42.3)
        );

        assertTrue(line.startsWith("search.candidates.lexical"));
        assertTrue(line.contains("query=\"how do i build a rain shelter\""));
        assertTrue(line.contains("n=2"));
        assertTrue(line.contains("1|GD-345|Debris Huts|38.100|emergency_shelter|survival|shelter,debris-hut"));
        assertTrue(line.contains("2|GD-727|Practical Survival Apps|42.300|general|utility|maintenance,storage"));
        assertEquals(2, rowCount(line));
    }

    @Test
    public void searchResultTelemetryLineCapsAtTopTwentyRows() {
        List<SearchResult> results = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        for (int index = 0; index < 30; index++) {
            results.add(
                searchResult(
                    String.format("GD-%03d", index),
                    "Section " + index,
                    "general",
                    "survival",
                    "topic" + index
                )
            );
            scores.add((double) index);
        }

        String line = buildSearchResultCandidateTelemetryLine(
            "lexical",
            "rain shelter",
            results,
            scores
        );

        assertTrue(line.contains("n=20"));
        assertFalse(line.contains("truncated=true"));
        assertEquals(20, rowCount(line));
        assertFalse(line.contains("21|GD-020"));
    }

    @Test
    public void searchResultTelemetryLineFallsBackWhenLineGetsTooLong() {
        List<SearchResult> results = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        String longSection = repeat('|', 120);
        String longTopics = repeat(']', 160);
        String longStructure = repeat('|', 32);
        String longCategory = repeat(']', 32);
        for (int index = 0; index < 20; index++) {
            results.add(
                searchResult(
                    String.format("GD-%03d", index),
                    longSection + index,
                    longStructure + index,
                    longCategory + index,
                    longTopics + index
                )
            );
            scores.add(50.0 - index);
        }

        String line = buildSearchResultCandidateTelemetryLine(
            "lexical",
            "how do i build a rain shelter",
            results,
            scores
        );

        assertTrue(line.contains("truncated=true"));
        assertTrue(line.contains("n=10"));
        assertEquals(10, rowCount(line));
        assertFalse(line.contains("11|GD-010"));
    }

    @Test
    public void searchResultTelemetryLineHandlesEmptyInput() {
        String line = buildSearchResultCandidateTelemetryLine(
            "lexical",
            "rain shelter",
            Collections.emptyList(),
            Collections.emptyList()
        );

        assertEquals("search.candidates.lexical query=\"rain shelter\" n=0 rows=[]", line);
    }

    @Test
    public void structureOnlyHintTriggersMetadataRerank() {
        String query = "How do I build a simple rain shelter from tarp and cord?";

        assertFalse(QueryRouteProfile.fromQuery(query).isRouteFocused());
        assertEquals("emergency_shelter", QueryMetadataProfile.fromQuery(query).preferredStructureType());
        assertTrue(PackRepository.shouldApplyMetadataRerankForTest(query));

        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Arrays.asList(
                detailedSearchResult(
                    "Tarp Shelter Setup",
                    "Use a ridgeline, tarp angle, and drainage trenching so rain sheds cleanly.",
                    "GD-345",
                    "Tarp Shelter Setup",
                    "survival",
                    "lexical",
                    "starter",
                    "immediate",
                    "emergency_shelter",
                    "tarp_shelter,weatherproofing"
                ),
                detailedSearchResult(
                    "Storage & Material Management",
                    "General storage notes for unrelated camp supplies.",
                    "GD-727",
                    "Storage Basics",
                    "utility",
                    "lexical",
                    "reference",
                    "mixed",
                    "general",
                    "maintenance,storage"
                )
            ),
            5
        );

        assertHasActiveRerank(reranked);
        PackRepository.RerankedResult shelter = rerankedResult(reranked, "GD-345");
        assertTrue(shelter.metadataBonus > 0);
        assertTrue(shelter.finalScore > 0.0);
    }

    @Test
    public void vectorRowsApplyMetadataBonusToSortKey() {
        String query = "How do I build a simple rain shelter from tarp and cord?";

        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Arrays.asList(
                detailedSearchResult(
                    "Camp Inventory Ledger",
                    "Count crates and spare fittings before transport.",
                    "GD-727",
                    "Inventory",
                    "utility",
                    "lexical",
                    "reference",
                    "mixed",
                    "general",
                    "maintenance,storage"
                ),
                detailedSearchResult(
                    "Camp Inventory Ledger",
                    "Count crates and spare fittings before transport.",
                    "GD-345",
                    "Inventory",
                    "survival",
                    "vector",
                    "starter",
                    "immediate",
                    "emergency_shelter",
                    "maintenance,storage"
                )
            ),
            5
        );

        PackRepository.RerankedResult vector = rerankedResult(reranked, "GD-345");
        PackRepository.RerankedResult lexical = rerankedResult(reranked, "GD-727");

        assertEquals("GD-345", reranked.get(0).result.guideId);
        assertTrue(vector.metadataBonus > 0);
        assertEquals(expectedSingleGuideFinalScore(vector.metadataBonus), vector.finalScore, 0.0);
        assertTrue(vector.finalScore > lexical.finalScore);
    }

    @Test
    public void vectorOwnerRowsRecoverLexicalSupportForStructureMatchedQueries() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        SearchResult ownerVector = detailedSearchResult(
            "Tarp & Cord Shelters",
            "Build a simple rain shelter with a tarp ridgeline, cord tension, and runoff angle.",
            "GD-345",
            "Rain shelter setup",
            "survival",
            "vector",
            "starter",
            "immediate",
            "emergency_shelter",
            "tarp_shelter,weatherproofing"
        );
        SearchResult offOwnerVector = detailedSearchResult(
            "Winter Survival Systems",
            "Build a simple rain shelter with a tarp ridgeline, cord tension, and runoff angle.",
            "GD-024",
            "Cold weather setup",
            "survival",
            "vector",
            "starter",
            "immediate",
            "general",
            "cold_weather"
        );

        PackRepository.SupportBreakdown ownerBreakdown = PackRepository.supportBreakdownForTest(query, ownerVector);
        PackRepository.SupportBreakdown offOwnerBreakdown = PackRepository.supportBreakdownForTest(query, offOwnerVector);
        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Arrays.asList(offOwnerVector, ownerVector),
            5
        );
        PackRepository.RerankedResult owner = rerankedResult(reranked, "GD-345");

        assertTrue(ownerBreakdown.lexicalSupport > 0);
        assertEquals(0, offOwnerBreakdown.lexicalSupport);
        assertTrue(owner.finalScore > expectedSingleGuideFinalScore(owner.metadataBonus));
    }

    @Test
    public void vectorRowBaseScoreStillReflectsDisplayOnlyDerivation() {
        String query = "How do I build a simple rain shelter from tarp and cord?";

        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Collections.singletonList(
                detailedSearchResult(
                    "Camp Inventory Ledger",
                    "Count crates and spare fittings before transport.",
                    "GD-345",
                    "Inventory",
                    "survival",
                    "vector",
                    "starter",
                    "immediate",
                    "emergency_shelter",
                    "maintenance,storage"
                )
            ),
            5
        );

        PackRepository.RerankedResult vector = reranked.get(0);

        assertTrue(vector.metadataBonus > 0);
        assertEquals(expectedSingleGuideFinalScore(vector.metadataBonus), vector.finalScore, 0.0);
        assertEquals(vector.finalScore - vector.metadataBonus, vector.baseScore, 0.0);
    }

    @Test
    public void vectorRerankUsesSharedSupportBreakdownMetadataBonus() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        SearchResult vectorResult = detailedSearchResult(
            "Shelter Build Log",
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

        PackRepository.SupportBreakdown breakdown = PackRepository.supportBreakdownForTest(query, vectorResult);
        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Collections.singletonList(vectorResult),
            5
        );
        PackRepository.RerankedResult row = reranked.get(0);

        assertEquals(breakdown.metadataBonus, row.metadataBonus);
        assertEquals(expectedSingleGuideFinalScore(breakdown.supportWithMetadata()), row.finalScore, 0.0);
        assertEquals(row.finalScore - row.metadataBonus, row.baseScore, 0.0);
    }

    @Test
    public void lexicalRowsDoNotDoubleCountMetadataBonus() {
        String query = "How do I build a simple rain shelter from tarp and cord?";

        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Collections.singletonList(
                detailedSearchResult(
                    "Camp Inventory Ledger",
                    "Count crates and spare fittings before transport.",
                    "GD-345",
                    "Inventory",
                    "survival",
                    "lexical",
                    "starter",
                    "immediate",
                    "emergency_shelter",
                    "maintenance,storage"
                )
            ),
            5
        );

        PackRepository.RerankedResult lexical = reranked.get(0);
        int preGuideScore = lexical.metadataBonus + 2;

        assertTrue(lexical.metadataBonus > 0);
        assertEquals(expectedSingleGuideFinalScore(preGuideScore), lexical.finalScore, 0.0);
    }

    @Test
    public void emptyRetrievalModeIsTreatedAsNonVector() {
        String query = "How do I build a simple rain shelter from tarp and cord?";

        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Collections.singletonList(
                detailedSearchResult(
                    "Camp Inventory Ledger",
                    "Count crates and spare fittings before transport.",
                    "GD-345",
                    "Inventory",
                    "survival",
                    "",
                    "starter",
                    "immediate",
                    "emergency_shelter",
                    "maintenance,storage"
                )
            ),
            5
        );

        PackRepository.RerankedResult row = reranked.get(0);

        assertTrue(row.metadataBonus > 0);
        assertEquals(expectedSingleGuideFinalScore(row.metadataBonus), row.finalScore, 0.0);
    }

    @Test
    public void routeFocusedQueryStillTriggersMetadataRerankWithoutStructureHint() {
        String query = "how do i start a fire in the rain";

        assertTrue(QueryRouteProfile.fromQuery(query).isRouteFocused());
        assertEquals("", QueryMetadataProfile.fromQuery(query).preferredStructureType());
        assertTrue(PackRepository.shouldApplyMetadataRerankForTest(query));

        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Arrays.asList(
                detailedSearchResult(
                    "Fire in Wet Conditions",
                    "Use dry inner tinder, split damp wood, and shield the bundle from rain.",
                    "GD-201",
                    "Starting a Fire in Rain",
                    "survival",
                    "route-focus",
                    "starter",
                    "immediate",
                    "general",
                    "fire,wet_weather"
                ),
                detailedSearchResult(
                    "Fire Suppression",
                    "Wildland firebreak planning and suppression organization.",
                    "GD-202",
                    "Containment Lines",
                    "fire",
                    "lexical",
                    "planning",
                    "mixed",
                    "general",
                    "suppression"
                )
            ),
            5
        );

        assertHasActiveRerank(reranked);
    }

    @Test
    public void queryWithoutRouteFocusOrStructureHintReturnsPassthroughRerank() {
        String query = "best way to organize spare tools";

        assertFalse(QueryRouteProfile.fromQuery(query).isRouteFocused());
        assertEquals("", QueryMetadataProfile.fromQuery(query).preferredStructureType());
        assertFalse(PackRepository.shouldApplyMetadataRerankForTest(query));

        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            query,
            Arrays.asList(
                detailedSearchResult(
                    "Workshop Organization",
                    "Store spare tools by task and frequency of use.",
                    "GD-301",
                    "Tool Storage Basics",
                    "resource-management",
                    "lexical",
                    "planning",
                    "long_term",
                    "general",
                    "storage,tools"
                ),
                detailedSearchResult(
                    "Storage Containers",
                    "Container sizing and labeling notes.",
                    "GD-302",
                    "Inventory Labels",
                    "resource-management",
                    "guide-focus",
                    "reference",
                    "long_term",
                    "general",
                    "inventory"
                )
            ),
            5
        );

        assertPassthroughRerank(reranked);
    }

    @Test
    public void emptyResultsReturnEmptyRerankList() {
        List<PackRepository.RerankedResult> reranked = PackRepository.maybeRerankResultsDetailedForTest(
            "How do I build a simple rain shelter from tarp and cord?",
            Collections.emptyList(),
            5
        );

        assertTrue(reranked.isEmpty());
    }

    @Test
    public void finalizationPreservesTelemetryAndRerankTimingForFallbackAndHybridShapes() {
        assertFinalizationTelemetryAndTiming(
            PackSearchTelemetryPolicy.OUTCOME_VECTOR_DISABLED,
            PackRetrievalFusionPolicy.mergeHybrid(
                Arrays.asList(
                    rankedChunk("lex-shelter", "GD-345", "Tarp Shelter Setup", "emergency_shelter", 0),
                    rankedChunk("lex-storage", "GD-727", "Storage Basics", "general", 1)
                ),
                Collections.emptyList(),
                null,
                Collections.emptyMap(),
                message -> {
                }
            ),
            0,
            0L
        );
        assertFinalizationTelemetryAndTiming(
            PackSearchTelemetryPolicy.OUTCOME_CENTROID_MISSING,
            PackRetrievalFusionPolicy.mergeHybrid(
                Arrays.asList(
                    rankedChunk("centroid-shelter", "GD-345", "Tarp Shelter Setup", "emergency_shelter", 0),
                    rankedChunk("centroid-storage", "GD-727", "Storage Basics", "general", 1)
                ),
                Collections.emptyList(),
                null,
                Collections.emptyMap(),
                message -> {
                }
            ),
            0,
            7L
        );
        assertFinalizationTelemetryAndTiming(
            PackSearchTelemetryPolicy.OUTCOME_HYBRID,
            PackRetrievalFusionPolicy.mergeHybrid(
                Arrays.asList(
                    rankedChunk("hybrid-shelter", "GD-345", "Tarp Shelter Setup", "emergency_shelter", 0),
                    rankedChunk("hybrid-storage", "GD-727", "Storage Basics", "general", 1)
                ),
                Collections.singletonList(
                    rankedChunk("hybrid-shelter", "GD-345", "Tarp Shelter Setup", "emergency_shelter", 0)
                        .withVectorRank(0, 0.92f)
                ),
                null,
                Collections.emptyMap(),
                message -> {
                }
            ),
            1,
            11L
        );
    }

    @Test
    public void finalizationReranksStructureOwnerBeyondDisplayWindow() {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        ArrayList<String> telemetryLines = new ArrayList<>();
        List<PackRepository.CombinedHit> combinedHits = Arrays.asList(
            combinedHit("gd024-vector", "GD-024", "Winter Shelter Overview", "general", 0.016, Integer.MAX_VALUE, 0),
            combinedHit("gd673-lexical", "GD-673", "Simple Filtration Systems", "general", 0.016, 1, Integer.MAX_VALUE),
            combinedHit("gd024-vector-2", "GD-024", "Snow Shelter Construction", "general", 0.016, Integer.MAX_VALUE, 1),
            combinedHit("gd873-lexical", "GD-873", "Excavation Methods & Shoring", "earth_shelter", 0.016, 3, Integer.MAX_VALUE),
            combinedHit("gd024-vector-3", "GD-024", "Cold Weather Shelter", "general", 0.016, Integer.MAX_VALUE, 2),
            combinedHit("gd934-lexical", "GD-934", "Simple Routines That Actually Help", "general", 0.016, 5, Integer.MAX_VALUE),
            combinedHit("gd024-vector-4", "GD-024", "Frostbite Prevention", "general", 0.016, Integer.MAX_VALUE, 3),
            combinedHit("gd695-lexical", "GD-695", "Tornado Safety", "general", 0.016, 7, Integer.MAX_VALUE),
            combinedHit("gd345-lexical", "GD-345", "Rainproofing and Water Shedding", "emergency_shelter", 0.015, 8, Integer.MAX_VALUE)
        );

        PackRepository.SearchFinalization finalization =
            PackRepository.finalizeCombinedHitsForSearchForTest(query, combinedHits, 4, telemetryLines::add);

        assertEquals("GD-345", finalization.rerankedResults.get(0).guideId);
        assertFalse(telemetryLines.get(0).contains("GD-345"));
        assertTrue(telemetryLines.get(1).contains("GD-345"));
    }

    @Test
    public void telemetryPolicyOwnsSearchOutcomeFallbackStrings() {
        assertSearchOutcome(PackSearchTelemetryPolicy.OUTCOME_PLAIN_LIKE_NO_TERMS);
        assertSearchOutcome(PackSearchTelemetryPolicy.OUTCOME_ROUTE_ONLY);
        assertSearchOutcome(PackSearchTelemetryPolicy.OUTCOME_PLAIN_LIKE_EMPTY_LEXICAL);
        assertSearchOutcome(PackSearchTelemetryPolicy.OUTCOME_VECTOR_DISABLED);
        assertSearchOutcome(PackSearchTelemetryPolicy.OUTCOME_CENTROID_MISSING);
        assertSearchOutcome(PackSearchTelemetryPolicy.OUTCOME_HYBRID);
    }

    private static SearchResult searchResult(
        String guideId,
        String sectionHeading,
        String structureType,
        String category,
        String topicTags
    ) {
        return new SearchResult(
            "Guide " + guideId,
            "",
            sectionHeading,
            sectionHeading,
            guideId,
            sectionHeading,
            category,
            "lexical",
            "planning",
            "mixed",
            structureType,
            topicTags
        );
    }

    private static SearchResult detailedSearchResult(
        String title,
        String body,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
    ) {
        return new SearchResult(
            title,
            "",
            body,
            body,
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            contentRole,
            timeHorizon,
            structureType,
            topicTags
        );
    }

    private static PackRepository.RankedChunk rankedChunk(
        String chunkId,
        String guideId,
        String sectionHeading,
        String structureType,
        int rank
    ) {
        return new PackRepository.RankedChunk(
            chunkId,
            rank,
            "Guide " + guideId,
            guideId,
            sectionHeading,
            "survival",
            "Use tarp angle, ridgeline, and drainage so rain sheds cleanly.",
            "starter",
            "immediate",
            structureType,
            "shelter,tarp_shelter,weatherproofing",
            1.0 / (rank + 1),
            rank,
            Integer.MAX_VALUE,
            Float.NEGATIVE_INFINITY
        );
    }

    private static PackRepository.CombinedHit combinedHit(
        String chunkId,
        String guideId,
        String sectionHeading,
        String structureType,
        double score,
        int lexicalRank,
        int vectorRank
    ) {
        PackRepository.RankedChunk chunk = new PackRepository.RankedChunk(
            chunkId,
            Math.min(lexicalRank, vectorRank),
            "Guide " + guideId,
            guideId,
            sectionHeading,
            "survival",
            "Build a simple rain shelter with tarp, cord, ridgeline tension, and runoff control.",
            "starter",
            "immediate",
            structureType,
            "shelter,tarp_shelter,weatherproofing",
            score,
            lexicalRank,
            vectorRank,
            vectorRank == Integer.MAX_VALUE ? Float.NEGATIVE_INFINITY : 0.9f
        );
        PackRepository.CombinedHit hit = new PackRepository.CombinedHit(chunk);
        hit.rrfScore = score;
        hit.lexicalRank = lexicalRank;
        hit.vectorRank = vectorRank;
        hit.vectorScore = chunk.vectorScore;
        return hit;
    }

    private static void assertFinalizationTelemetryAndTiming(
        String fallbackMode,
        List<PackRepository.CombinedHit> combinedHits,
        int vectorHits,
        long vectorMs
    ) {
        String query = "How do I build a simple rain shelter from tarp and cord?";
        ArrayList<String> telemetryLines = new ArrayList<>();

        PackRepository.SearchFinalization finalization =
            PackRepository.finalizeCombinedHitsForSearchForTest(query, combinedHits, 2, telemetryLines::add);

        assertEquals(2, telemetryLines.size());
        assertTrue(telemetryLines.get(0).startsWith("search.candidates.prerank"));
        assertTrue(telemetryLines.get(1).startsWith("search.candidates.reranked"));
        assertTrue(telemetryLines.get(0).contains("GD-345"));
        assertTrue(telemetryLines.get(1).contains("GD-345"));
        assertFalse(finalization.rerankedResults.isEmpty());
        assertTrue(finalization.rerankElapsedMs >= 0L);

        String summaryLine = PackRepository.buildSearchSummaryLineForTest(
            query,
            true,
            1,
            combinedHits.size(),
            vectorHits,
            1,
            2L,
            3L,
            4L,
            vectorMs,
            finalization.rerankElapsedMs,
            20L,
            fallbackMode
        );

        assertTrue(summaryLine.contains("fallback=" + fallbackMode));
        assertTrue(summaryLine.contains("rerankMs=" + finalization.rerankElapsedMs));
    }

    private static void assertSearchOutcome(String outcome) {
        SearchLatencyBreakdown breakdown = PackSearchTelemetryPolicy.buildBreakdown(
            1L,
            2L,
            3L,
            4L,
            5L,
            6L,
            outcome
        );

        assertEquals(outcome, breakdown.fallbackMode);
        assertTrue(
            PackRepository.buildSearchSummaryLineForTest(
                "telemetry outcome",
                false,
                0,
                0,
                0,
                0,
                breakdown.routeMs,
                breakdown.ftsMs,
                breakdown.keywordMs,
                breakdown.vectorMs,
                breakdown.rerankMs,
                breakdown.totalMs,
                breakdown.fallbackMode
            ).contains("fallback=" + outcome)
        );
    }

    private static PackRepository.RerankedResult rerankedResult(
        List<PackRepository.RerankedResult> reranked,
        String guideId
    ) {
        for (PackRepository.RerankedResult row : reranked) {
            if (guideId.equals(row.result.guideId)) {
                return row;
            }
        }
        throw new AssertionError("Missing reranked result for " + guideId);
    }

    private static void assertHasActiveRerank(List<PackRepository.RerankedResult> reranked) {
        assertFalse(reranked.isEmpty());
        boolean sawNonZeroScore = false;
        boolean sawNonZeroBonus = false;
        for (PackRepository.RerankedResult row : reranked) {
            if (row.finalScore != 0.0) {
                sawNonZeroScore = true;
            }
            if (row.metadataBonus != 0) {
                sawNonZeroBonus = true;
            }
        }
        assertTrue(sawNonZeroScore || sawNonZeroBonus);
    }

    private static void assertPassthroughRerank(List<PackRepository.RerankedResult> reranked) {
        assertFalse(reranked.isEmpty());
        for (int index = 0; index < reranked.size(); index++) {
            PackRepository.RerankedResult row = reranked.get(index);
            assertEquals(index, row.originalIndex);
            assertEquals(0, row.metadataBonus);
            assertEquals(0.0, row.finalScore, 0.0);
        }
    }

    private static int rowCount(String line) {
        int start = line.indexOf("rows=[");
        int end = line.lastIndexOf(']');
        if (start < 0 || end < start + 6) {
            return 0;
        }
        String body = line.substring(start + 6, end);
        if (body.isEmpty()) {
            return 0;
        }
        return body.split(" \\|\\| ").length;
    }

    private static String buildSearchResultCandidateTelemetryLine(
        String stage,
        String query,
        List<SearchResult> results,
        List<Double> scores
    ) {
        ArrayList<String> rows = new ArrayList<>();
        int capped = Math.min(results == null ? 0 : results.size(), scores == null ? 0 : scores.size());
        for (int index = 0; index < capped; index++) {
            rows.add(CandidateTelemetryFormatter.formatRow(index + 1, results.get(index), scores.get(index)));
        }
        return CandidateTelemetryFormatter.buildLine(stage, query, rows);
    }

    private static String repeat(char value, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, value);
        return new String(chars);
    }

    private static double expectedSingleGuideFinalScore(int score) {
        return score + Math.min(24, Math.max(1, score) / 3);
    }
}

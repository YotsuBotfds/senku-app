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

        String line = PackRepository.buildSearchResultCandidateTelemetryLineForTest(
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

        String line = PackRepository.buildSearchResultCandidateTelemetryLineForTest(
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

        String line = PackRepository.buildSearchResultCandidateTelemetryLineForTest(
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
        String line = PackRepository.buildSearchResultCandidateTelemetryLineForTest(
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

    private static String repeat(char value, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, value);
        return new String(chars);
    }
}

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

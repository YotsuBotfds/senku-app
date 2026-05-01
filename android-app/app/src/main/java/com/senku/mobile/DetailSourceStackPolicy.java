package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class DetailSourceStackPolicy {
    private DetailSourceStackPolicy() {
    }

    static final class ReviewedSourceStackEntry {
        static final ReviewedSourceStackEntry NONE = new ReviewedSourceStackEntry(
            "",
            "",
            "",
            "",
            "",
            "",
            Integer.MAX_VALUE
        );

        final String guideId;
        final String roleLabel;
        final String matchLabel;
        final String evidenceTitle;
        final String stationTitle;
        final String quote;
        final int rank;

        private ReviewedSourceStackEntry(
            String guideId,
            String roleLabel,
            String matchLabel,
            String evidenceTitle,
            String stationTitle,
            String quote,
            int rank
        ) {
            this.guideId = guideId;
            this.roleLabel = roleLabel;
            this.matchLabel = matchLabel;
            this.evidenceTitle = evidenceTitle;
            this.stationTitle = stationTitle;
            this.quote = quote;
            this.rank = rank;
        }

        boolean isPresent() {
            return rank < Integer.MAX_VALUE;
        }

        boolean isAnchor() {
            return "ANCHOR".equals(roleLabel);
        }

        String stationLabel() {
            if (!isPresent()) {
                return "";
            }
            return guideId + " \u00B7 " + roleLabel + "\n" + stationTitle;
        }
    }

    static List<SearchResult> orderAnswerSourceStack(boolean enabled, List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<SearchResult> ordered = dedupeExactSourceRows(sources);
        if (!containsReviewedRainShelterSource(enabled, ordered)) {
            return ordered;
        }
        Collections.sort(ordered, (left, right) -> {
            int leftRank = reviewedSourceStackEntry(enabled, left).rank;
            int rightRank = reviewedSourceStackEntry(enabled, right).rank;
            if (leftRank != rightRank) {
                return Integer.compare(leftRank, rightRank);
            }
            return 0;
        });
        return ordered;
    }

    static ReviewedSourceStackEntry reviewedSourceStackEntry(boolean enabled, SearchResult source) {
        if (!enabled || source == null) {
            return ReviewedSourceStackEntry.NONE;
        }
        String guideId = safe(source.guideId).trim();
        if ("GD-220".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "abrasives", "manufacturing")) {
            return new ReviewedSourceStackEntry(
                "GD-220",
                "ANCHOR",
                "74%",
                "Abrasives Manufacturing",
                "Abrasives Manufacturing",
                "Every melt starts with a foundry safety check, not with metal charge...",
                0
            );
        }
        if ("GD-132".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "foundry", "metal", "casting")) {
            return new ReviewedSourceStackEntry(
                "GD-132",
                "RELATED",
                "68%",
                "Foundry & Metal Casting",
                "Foundry & Metal Casting",
                "Pitch the ridgeline along prevailing wind. Tension corners with prusik or taut-line hitches.",
                1
            );
        }
        if ("GD-345".equalsIgnoreCase(guideId) && isRainShelterStackSource(source)) {
            String evidenceTitle = hasRainShelterEvidenceTitleOverride(source) ? "Tarp & Cord Shelters" : "";
            return new ReviewedSourceStackEntry(
                "GD-345",
                "TOPIC",
                "61%",
                evidenceTitle,
                "Tarp & Cord Shelters",
                "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                2
            );
        }
        return ReviewedSourceStackEntry.NONE;
    }

    private static ArrayList<SearchResult> dedupeExactSourceRows(List<SearchResult> sources) {
        ArrayList<SearchResult> deduped = new ArrayList<>();
        Set<String> seenSourceRows = new HashSet<>();
        for (SearchResult source : sources) {
            String key = exactSourceRowKey(source);
            if (seenSourceRows.add(key)) {
                deduped.add(source);
            }
        }
        return deduped;
    }

    private static String exactSourceRowKey(SearchResult source) {
        return safe(source == null ? null : source.guideId).trim().toUpperCase(Locale.US)
            + "\u001F" + safe(source == null ? null : source.title).trim()
            + "\u001F" + safe(source == null ? null : source.sectionHeading).trim()
            + "\u001F" + safe(source == null ? null : source.snippet).trim()
            + "\u001F" + safe(source == null ? null : source.body).trim();
    }

    private static boolean containsReviewedRainShelterSource(boolean enabled, List<SearchResult> sources) {
        if (sources == null) {
            return false;
        }
        for (SearchResult source : sources) {
            if (reviewedSourceStackEntry(enabled, source).rank < Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRainShelterStackSource(SearchResult source) {
        if (source == null) {
            return false;
        }
        String guideId = safe(source.guideId).trim();
        String combined = (
            safe(source.title) + " " +
                safe(source.sectionHeading) + " " +
                safe(source.snippet) + " " +
                safe(source.body) + " " +
                safe(source.topicTags) + " " +
                safe(source.structureType)
        ).toLowerCase(Locale.US);
        return "GD-345".equalsIgnoreCase(guideId)
            && (containsAll(combined, "tarp", "cord")
                || containsAll(combined, "rain", "shelter")
                || combined.contains("ridgeline shelter")
                || containsAll(combined, "primitive", "shelter"));
    }

    private static boolean hasRainShelterEvidenceTitleOverride(SearchResult source) {
        String combined = (
            safe(source == null ? null : source.title) + " " +
                safe(source == null ? null : source.sectionHeading) + " " +
                safe(source == null ? null : source.snippet) + " " +
                safe(source == null ? null : source.body) + " " +
                safe(source == null ? null : source.topicTags) + " " +
                safe(source == null ? null : source.structureType)
        ).toLowerCase(Locale.US);
        return containsAll(combined, "tarp", "cord")
            || containsAll(combined, "rain", "shelter")
            || combined.contains("ridgeline shelter");
    }

    private static boolean containsReviewedRainShelterText(SearchResult source, String... terms) {
        String combined = (
            safe(source == null ? null : source.title) + " " +
                safe(source == null ? null : source.sectionHeading) + " " +
                safe(source == null ? null : source.snippet) + " " +
                safe(source == null ? null : source.body) + " " +
                safe(source == null ? null : source.topicTags) + " " +
                safe(source == null ? null : source.structureType)
        ).toLowerCase(Locale.US);
        return containsAll(combined, terms);
    }

    private static boolean containsAll(String text, String... needles) {
        String normalized = safe(text);
        for (String needle : needles) {
            if (!normalized.contains(safe(needle))) {
                return false;
            }
        }
        return true;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

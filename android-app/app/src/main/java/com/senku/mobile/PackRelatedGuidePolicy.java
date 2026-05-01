package com.senku.mobile;

import java.util.ArrayList;
import java.util.Locale;

final class PackRelatedGuidePolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackRelatedGuidePolicy() {
    }

    static int candidateLimit(int requestedLimit) {
        int limit = Math.max(requestedLimit, 1);
        return Math.min(32, Math.max(limit, Math.max(limit * 4, 12)));
    }

    static ArrayList<SearchResult> orderByWorkflowRelevance(
        SearchResult anchor,
        Iterable<SearchResult> relatedGuides
    ) {
        ArrayList<IndexedSearchResult> indexed = new ArrayList<>();
        int index = 0;
        for (SearchResult guide : relatedGuides) {
            if (guide != null) {
                indexed.add(new IndexedSearchResult(guide, index));
            }
            index += 1;
        }
        final String anchorText = relatedGuideText(anchor);
        final boolean fireWorkflow = isFireStartingWorkflow(anchorText);
        final boolean shelterWorkflow = isShelterWorkflow(anchorText);
        final boolean survivalWorkflow = fireWorkflow || shelterWorkflow || isSurvivalWorkflow(anchorText);
        if (!survivalWorkflow) {
            ArrayList<SearchResult> unchanged = new ArrayList<>();
            for (IndexedSearchResult item : indexed) {
                unchanged.add(item.result);
            }
            return unchanged;
        }

        indexed.sort((left, right) -> {
            int leftPriority = relatedWorkflowPriority(anchorText, left.result);
            int rightPriority = relatedWorkflowPriority(anchorText, right.result);
            if (leftPriority != rightPriority) {
                return Integer.compare(rightPriority, leftPriority);
            }
            return Integer.compare(left.index, right.index);
        });

        ArrayList<SearchResult> ordered = new ArrayList<>();
        for (IndexedSearchResult item : indexed) {
            ordered.add(item.result);
        }
        return ordered;
    }

    private static int relatedWorkflowPriority(String anchorText, SearchResult guide) {
        String text = relatedGuideText(guide);
        int score = 0;

        if (isFireStartingWorkflow(anchorText)) {
            score += markerScore(
                text,
                140,
                "fire by friction", "friction fire", "fire-starting", "fire starting",
                "bow drill", "hand drill", "fire plow", "tinder", "kindling", "ember"
            );
            score += markerScore(
                text,
                125,
                "fire lay", "fire lays", "fire layout", "teepee fire", "log cabin fire",
                "platform fire", "upside-down fire", "feather stick", "featherstick",
                "tinder bundle"
            );
            score += markerScore(
                text,
                120,
                "wet fire", "fire in wet conditions", "wet-weather fire", "wet weather fire",
                "dry inner wood", "wet wood", "dry tinder", "keep tinder dry",
                "protect tinder", "waterproofing", "weatherproofing", "rainproofing",
                "material protection", "dry storage", "shelter the flame", "windbreak"
            );
            score += markerScore(
                text,
                105,
                "emergency shelter", "primitive shelter", "shelter construction",
                "tarp shelter", "rain shelter", "debris hut", "lean-to", "lean to",
                "a-frame shelter"
            );
            score += markerScore(
                text,
                95,
                "survival basics", "first 72 hours", "temperate forest survival",
                "winter survival", "primitive technology"
            );
            score += markerScore(
                text,
                80,
                "daily cooking fire", "fire management", "cookstove", "stove",
                "charcoal", "fuel", "combustion", "flame", "fire suppression"
            );
            score += markerScore(text, 35, "wood selection", "woodcarving", "dry wood", "bark");
            score -= markerScore(
                text,
                120,
                "agriculture", "gardening", "animal husbandry", "veterinary",
                "livestock", "breeding", "pasture", "zoology", "butchering"
            );
            score -= markerScore(
                text,
                65,
                "catalog", "taxonomy", "field guide", "archaeological", "ancient techniques"
            );
            score -= markerScore(text, 70, "black powder", "blasting", "weapons", "martial arts");
        } else if (isShelterWorkflow(anchorText)) {
            score += markerScore(
                text,
                125,
                "emergency shelter", "primitive shelter", "shelter construction",
                "tarp shelter", "rain shelter", "debris hut", "lean-to", "lean to",
                "a-frame shelter", "windbreak"
            );
            score += markerScore(
                text,
                110,
                "waterproofing", "weatherproofing", "rainproofing", "material protection",
                "dry storage", "keep tinder dry", "protect tinder", "wet-weather fire",
                "wet weather fire", "fire in wet conditions", "wet fire", "dry tinder"
            );
            score += markerScore(
                text,
                95,
                "fire lay", "fire lays", "fire layout", "teepee fire", "log cabin fire",
                "platform fire", "upside-down fire", "fire-starting", "fire starting",
                "fire by friction", "friction fire", "tinder bundle"
            );
            score += markerScore(
                text,
                75,
                "survival basics", "first 72 hours", "winter survival",
                "temperate forest survival", "water purification", "quick reference"
            );
            score -= markerScore(
                text,
                105,
                "agriculture", "gardening", "animal husbandry", "veterinary",
                "livestock", "breeding", "pasture", "zoology", "butchering"
            );
            score -= markerScore(text, 55, "catalog", "taxonomy", "field guide", "archaeological");
            score -= markerScore(text, 45, "weapons", "martial arts", "black powder", "blasting");
        } else if (isSurvivalWorkflow(anchorText)) {
            score += markerScore(
                text,
                110,
                "fire by friction", "fire-starting", "wet fire", "fire in wet conditions",
                "water purification", "water storage",
                "primitive shelter", "shelter construction", "go-bag", "search rescue",
                "night navigation", "quick reference", "winter survival", "fire suppression"
            );
            score += markerScore(
                text,
                85,
                "emergency shelter", "tarp shelter", "rain shelter", "debris hut",
                "lean-to", "lean to", "a-frame shelter", "waterproofing",
                "weatherproofing", "dry storage", "material protection"
            );
            score += markerScore(text, 55, "first aid", "triage", "disaster", "family emergency");
            score -= markerScore(
                text,
                90,
                "agriculture", "gardening", "animal husbandry", "veterinary",
                "livestock", "breeding", "pasture", "zoology"
            );
            score -= markerScore(text, 55, "catalog", "taxonomy", "field guide", "archaeological");
            score -= markerScore(text, 45, "weapons", "martial arts", "butchering", "trapping");
        }

        return score;
    }

    private static int markerScore(String text, int weight, String... markers) {
        for (String marker : markers) {
            if (text.contains(marker)) {
                return weight;
            }
        }
        return 0;
    }

    private static boolean isFireStartingWorkflow(String text) {
        return text.contains("gd-343")
            || text.contains("gd-027")
            || text.contains("fire by friction")
            || text.contains("friction fire")
            || text.contains("fire-starting")
            || text.contains("fire starting")
            || text.contains("wet fire")
            || text.contains("fire in wet conditions")
            || text.contains("wet-weather fire")
            || text.contains("wet weather fire")
            || text.contains("bow drill")
            || text.contains("hand drill")
            || text.contains("tinder");
    }

    private static boolean isSurvivalWorkflow(String text) {
        return text.contains("gd-023")
            || text.contains("survival basics")
            || text.contains("first 72 hours")
            || text.contains("primitive shelter")
            || text.contains("winter survival")
            || text.contains("temperate forest survival");
    }

    private static boolean isShelterWorkflow(String text) {
        return text.contains("emergency shelter")
            || text.contains("primitive shelter")
            || text.contains("shelter construction")
            || text.contains("tarp shelter")
            || text.contains("rain shelter")
            || text.contains("debris hut")
            || text.contains("lean-to")
            || text.contains("lean to")
            || text.contains("a-frame shelter");
    }

    private static String relatedGuideText(SearchResult result) {
        if (result == null) {
            return "";
        }
        return (
            safe(result.guideId) + " " +
            safe(result.title) + " " +
            safe(result.subtitle) + " " +
            safe(result.sectionHeading) + " " +
            safe(result.category) + " " +
            safe(result.contentRole) + " " +
            safe(result.structureType) + " " +
            safe(result.topicTags) + " " +
            safe(result.snippet)
        ).toLowerCase(QUERY_LOCALE);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class IndexedSearchResult {
        final SearchResult result;
        final int index;

        IndexedSearchResult(SearchResult result, int index) {
            this.result = result;
            this.index = index;
        }
    }
}

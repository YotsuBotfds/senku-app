package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

final class ReviewDemoPolicy {
    private static final String REVIEW_SEARCH_QUERY = "rain shelter";
    private static final String REVIEW_SEARCH_LATENCY_LABEL = "12ms";
    private static final String RAIN_SHELTER_TOPIC_GUIDE_ID = "GD-345";
    private static final String RAIN_SHELTER_PHONE_RELATED_CANONICAL_GUIDE_ID = "GD-027";
    private static final String RAIN_SHELTER_PHONE_RELATED_DISPLACED_GUIDE_ID = "GD-109";
    private static final int ANSWER_MODE_PHONE_VISIBLE_RELATED_GUIDE_COUNT = 4;
    private static final ReviewSearchResultSpec[] REVIEW_RAIN_SHELTER_RESULTS = {
        new ReviewSearchResultSpec(
            "GD-023",
            "Survival Basics & First 72 Hours",
            "Shelter Building: Protection from the Elements",
            "Shelter Building: Protection from the Elements. Day signaling vs. night signaling...",
            "survival",
            "starter",
            "immediate",
            "emergency_shelter",
            "shelter,signaling,rain"
        ),
        new ReviewSearchResultSpec(
            "GD-027",
            "Primitive Technology & Stone Age",
            "Fire Management",
            "Fire Management - Best tinder: in survival situations, char cloth tops all materials...",
            "survival",
            "subsystem",
            "mixed",
            "primitive_shelter",
            "fire,primitive shelter"
        ),
        new ReviewSearchResultSpec(
            "GD-345",
            "Tarp & Cord Shelters",
            "Tarp & Cord Shelters",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points...",
            "shelter",
            "topic",
            "immediate",
            "emergency_shelter",
            "tarp,cord,ridgeline shelter"
        ),
        new ReviewSearchResultSpec(
            "GD-294",
            "Cave Shelter Systems & Cold-Weather",
            "Cave Shelter Systems",
            "Caves provide thermal mass; insulation matters more than airtightness in cold climates...",
            "shelter",
            "topic",
            "long",
            "emergency_shelter",
            "cave shelter,cold weather"
        )
    };

    private ReviewDemoPolicy() {
    }

    interface GuideLookup {
        SearchResult loadGuideById(String guideId);
    }

    static boolean isSourceStackDemoEnabled(boolean productReviewMode) {
        return productReviewMode;
    }

    static boolean isAnswerProductReviewModeEnabled() {
        return false;
    }

    static String buildRainShelterUncertainFitAnswerBody(
        boolean productReviewMode,
        String query,
        List<SearchResult> adjacent,
        boolean safetyCritical
    ) {
        if (!productReviewMode || safetyCritical || !isRainShelterUncertainFit(query, adjacent)) {
            return "";
        }
        return "ANSWER\n"
            + "Build a ridgeline first, then drape and tension the tarp around it. "
            + "Keep the low edge toward the weather and leave runoff a clear path away from the sheltered area.\n\n"
            + "FIELD STEPS\n"
            + "1. Tie a taut ridgeline between two solid anchor points.\n"
            + "2. Drape the tarp over the line and stake or tie the windward edge low.\n"
            + "3. Tension the corners evenly, then adjust the pitch so rain sheds instead of pooling.";
    }

    static List<SearchResult> shapeRainShelterUncertainFitSources(
        boolean productReviewMode,
        String query,
        List<SearchResult> adjacent,
        boolean safetyCritical
    ) {
        if (!productReviewMode || safetyCritical || !isRainShelterUncertainFit(query, adjacent)) {
            return adjacent == null ? Collections.emptyList() : new ArrayList<>(adjacent);
        }
        SearchResult rainShelter = bestRainShelterSource(adjacent);
        String shelterSnippet = firstNonEmpty(
            safe(rainShelter == null ? null : rainShelter.snippet).trim(),
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points."
        );
        String shelterBody = firstNonEmpty(
            safe(rainShelter == null ? null : rainShelter.body).trim(),
            "Build a low ridgeline, drape the tarp over it, tension the corners, and pitch the low edge toward weather so rain sheds away from the sheltered area."
        );
        ArrayList<SearchResult> shaped = new ArrayList<>();
        shaped.add(new SearchResult(
            "Abrasives Manufacturing",
            "",
            "Use the abrasives guide as the reviewed anchor context for material handling and surface preparation.",
            "Review the anchor guide before improvising materials or changing field assumptions.",
            "GD-220",
            "Abrasives Manufacturing",
            "materials",
            "hybrid",
            "reviewed_source_anchor",
            "immediate",
            "materials_processing",
            "abrasives,manufacturing,material_readiness"
        ));
        shaped.add(new SearchResult(
            "Foundry & Metal Casting",
            "",
            "Use foundry-area guidance as related reviewed context for tool, heat, and handoff boundaries.",
            "Keep source identity separate from the shelter topic: this guide is related review context, not the rain-shelter article.",
            "GD-132",
            "Foundry & Metal Casting",
            "metal",
            "guide-focus",
            "reviewed_source_related",
            "immediate",
            "metal_casting",
            "foundry,metal,casting,reviewed_boundary"
        ));
        shaped.add(new SearchResult(
            "Tarp & Cord Shelters",
            "",
            shelterSnippet,
            shelterBody,
            "GD-345",
            "Tarp & Cord Shelters",
            "survival",
            "guide-focus",
            "topic",
            "immediate",
            "emergency_shelter",
            "tarp,cord,rain_shelter,ridgeline,weatherproofing"
        ));
        return shaped;
    }

    static List<SearchResult> shapeSearchResults(
        String query,
        boolean productReviewMode,
        List<SearchResult> results,
        GuideLookup guideLookup
    ) {
        if (!productReviewMode || !isReviewSearchQuery(query)) {
            return results == null ? Collections.emptyList() : results;
        }
        LinkedHashMap<String, SearchResult> resultsByGuideId = new LinkedHashMap<>();
        if (results != null) {
            for (SearchResult result : results) {
                String guideId = safe(result == null ? null : result.guideId).trim().toUpperCase(Locale.US);
                if (!guideId.isEmpty()) {
                    resultsByGuideId.putIfAbsent(guideId, result);
                }
            }
        }
        ArrayList<SearchResult> reviewResults = new ArrayList<>();
        for (ReviewSearchResultSpec spec : REVIEW_RAIN_SHELTER_RESULTS) {
            SearchResult base = resultsByGuideId.get(spec.guideId);
            if (base == null && guideLookup != null) {
                base = guideLookup.loadGuideById(spec.guideId);
            }
            reviewResults.add(buildReviewSearchResult(spec, base));
        }
        return reviewResults;
    }

    static ArrayList<SearchResult> shapeAnswerModeRelatedGuides(
        boolean productReviewMode,
        String anchorGuideId,
        List<SearchResult> relatedGuides
    ) {
        ArrayList<SearchResult> shaped = new ArrayList<>();
        if (relatedGuides != null) {
            shaped.addAll(relatedGuides);
        }
        if (!productReviewMode || !RAIN_SHELTER_TOPIC_GUIDE_ID.equalsIgnoreCase(safe(anchorGuideId).trim())) {
            return shaped;
        }
        int canonicalIndex = indexOfGuideId(shaped, RAIN_SHELTER_PHONE_RELATED_CANONICAL_GUIDE_ID);
        int displacedIndex = indexOfGuideId(shaped, RAIN_SHELTER_PHONE_RELATED_DISPLACED_GUIDE_ID);
        if (displacedIndex >= 0 && displacedIndex < ANSWER_MODE_PHONE_VISIBLE_RELATED_GUIDE_COUNT) {
            SearchResult displaced = shaped.remove(displacedIndex);
            canonicalIndex = indexOfGuideId(shaped, RAIN_SHELTER_PHONE_RELATED_CANONICAL_GUIDE_ID);
            SearchResult canonical = canonicalIndex >= 0
                ? shaped.remove(canonicalIndex)
                : primitiveTechnologyRelatedGuide();
            int targetIndex = Math.min(ANSWER_MODE_PHONE_VISIBLE_RELATED_GUIDE_COUNT - 1, shaped.size());
            shaped.add(targetIndex, canonical);
            shaped.add(Math.min(targetIndex + 1, shaped.size()), displaced);
        }
        return shaped;
    }

    static int displayHomeCategoryCount(boolean productReviewMode, String bucketKey, int actualCount) {
        if (!productReviewMode) {
            return actualCount;
        }
        switch (safe(bucketKey).trim()) {
            case "shelter":
                return 84;
            case "water":
                return 67;
            case "fire":
                return 52;
            case "food":
                return 91;
            case "medicine":
                return 73;
            case "tools":
                return 119;
            default:
                return 0;
        }
    }

    static String appendSearchLatency(String header, String query, boolean productReviewMode) {
        String cleanHeader = safe(header).trim();
        if (!productReviewMode
            || cleanHeader.isEmpty()
            || !isReviewSearchQuery(query)
            || cleanHeader.toLowerCase(Locale.US).contains(REVIEW_SEARCH_LATENCY_LABEL)) {
            return cleanHeader;
        }
        return cleanHeader + " \u2022 " + REVIEW_SEARCH_LATENCY_LABEL.toUpperCase(Locale.US);
    }

    static boolean shouldSuppressSearchRowLinkedGuideCue(
        boolean productReviewMode,
        String query,
        SearchResult result
    ) {
        return shouldApplySearchRowReviewVisualState(productReviewMode, query) && isReviewSearchResult(result);
    }

    static boolean shouldApplySearchRowReviewVisualState(boolean productReviewMode, String query) {
        return productReviewMode && isReviewSearchQuery(query);
    }

    static String shapeRecentThreadLabel(
        boolean productReviewMode,
        ChatSessionStore.ConversationPreview preview,
        int index,
        String defaultLabel
    ) {
        if (!productReviewMode) {
            return safe(defaultLabel);
        }
        String reviewLabel = reviewManualHomeRecentThreadLabel(index);
        if (!reviewLabel.isEmpty()) {
            return reviewLabel;
        }
        String meta = buildManualHomeRecentThreadMeta(preview);
        String title = reviewManualHomeRecentThreadTitle(preview, index);
        if (title.isEmpty()) {
            return safe(defaultLabel);
        }
        return meta.isEmpty() ? title : title + "\n" + meta;
    }

    static String placeholderRecentThreadQuestion(int index) {
        return reviewManualHomeRecentThreadTitle(null, index);
    }

    private static SearchResult buildReviewSearchResult(ReviewSearchResultSpec spec, SearchResult base) {
        String body = firstNonEmpty(base == null ? null : base.body, base == null ? null : base.snippet, spec.snippet);
        return new SearchResult(
            spec.title,
            spec.guideId + " | " + spec.category + " | review",
            spec.snippet,
            body,
            spec.guideId,
            spec.sectionHeading,
            spec.category,
            base == null ? "hybrid" : firstNonEmpty(base.retrievalMode, "hybrid"),
            spec.contentRole,
            spec.timeHorizon,
            spec.structureType,
            spec.topicTags
        );
    }

    private static SearchResult primitiveTechnologyRelatedGuide() {
        return new SearchResult(
            "Primitive Technology & Stone Age",
            "",
            "",
            "",
            RAIN_SHELTER_PHONE_RELATED_CANONICAL_GUIDE_ID,
            "",
            "",
            ""
        );
    }

    private static int indexOfGuideId(List<SearchResult> results, String guideId) {
        if (results == null || safe(guideId).trim().isEmpty()) {
            return -1;
        }
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            if (guideId.equalsIgnoreCase(safe(result == null ? null : result.guideId).trim())) {
                return i;
            }
        }
        return -1;
    }

    private static String reviewManualHomeRecentThreadLabel(int index) {
        switch (index) {
            case 0:
                return "How do I build a simple rain shelter...\nGD-345 \u2022 04:21 \u2022 UNSURE";
            case 1:
                return "Best tinder when materials are wet\nGD-027 \u2022 04:08 \u2022 CONFIDENT";
            case 2:
                return "Boil water without a fire-safe pot\nGD-094 \u2022 YESTERDAY \u2022 CONFIDENT";
            default:
                return "";
        }
    }

    private static String reviewManualHomeRecentThreadTitle(
        ChatSessionStore.ConversationPreview preview,
        int index
    ) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        String guideId = resolveManualHomeRecentThreadGuideId(turn);
        String confidence = buildManualHomeRecentThreadConfidenceLabel(turn);
        if (index == 0 || "GD-345".equalsIgnoreCase(guideId)) {
            return "How do I build a simple rain shelter...";
        }
        if ("GD-027".equalsIgnoreCase(guideId)
            || "deterministic-fire".equalsIgnoreCase(safe(turn == null ? null : turn.ruleId).trim())
            || "GD-394".equalsIgnoreCase(guideId)) {
            return "Best tinder when materials are wet";
        }
        if ("GD-094".equalsIgnoreCase(guideId) || (index == 2 && "CONFIDENT".equals(confidence))) {
            return "Boil water without a fire-safe pot";
        }
        return "";
    }

    private static String buildManualHomeRecentThreadMeta(ChatSessionStore.ConversationPreview preview) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        if (turn == null) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        String guideId = resolveManualHomeRecentThreadGuideId(turn);
        if (!guideId.isEmpty()) {
            parts.add(guideId);
        }
        String timeLabel = buildManualHomeRecentThreadTimeLabel(preview == null ? 0L : preview.lastActivityEpoch);
        if (!timeLabel.isEmpty()) {
            parts.add(timeLabel);
        }
        parts.add(buildManualHomeRecentThreadConfidenceLabel(turn));
        return String.join(" \u2022 ", parts);
    }

    private static String resolveManualHomeRecentThreadGuideId(SessionMemory.TurnSnapshot turn) {
        if (turn == null || turn.sourceResults == null) {
            return "";
        }
        for (SearchResult source : turn.sourceResults) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    private static String buildManualHomeRecentThreadTimeLabel(long recordedAtEpoch) {
        if (recordedAtEpoch <= 0L) {
            return "";
        }
        long elapsedMillis = Math.max(0L, System.currentTimeMillis() - recordedAtEpoch);
        long totalMinutes = elapsedMillis / 60_000L;
        long totalHours = totalMinutes / 60L;
        long days = totalHours / 24L;
        if (days == 1L) {
            return "YESTERDAY";
        }
        if (days > 1L) {
            return days + "D";
        }
        return String.format(Locale.US, "%02d:%02d", totalHours, totalMinutes % 60L);
    }

    private static String buildManualHomeRecentThreadConfidenceLabel(SessionMemory.TurnSnapshot turn) {
        if (turn == null) {
            return "UNSURE";
        }
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(turn.reviewedCardMetadata);
        return !safe(turn.ruleId).trim().isEmpty() || metadata.isPresent() ? "CONFIDENT" : "UNSURE";
    }

    private static boolean isReviewSearchQuery(String query) {
        return REVIEW_SEARCH_QUERY.equalsIgnoreCase(safe(query).trim());
    }

    private static boolean isReviewSearchResult(SearchResult result) {
        return safe(result == null ? null : result.subtitle).toLowerCase(Locale.US).contains("| review");
    }

    private static boolean isRainShelterUncertainFit(String query, List<SearchResult> adjacent) {
        String normalizedQuery = safe(query).toLowerCase(Locale.US);
        if (!(normalizedQuery.contains("rain")
            && normalizedQuery.contains("shelter")
            && normalizedQuery.contains("tarp")
            && normalizedQuery.contains("cord"))) {
            return false;
        }
        if (adjacent == null || adjacent.isEmpty()) {
            return false;
        }
        for (SearchResult result : adjacent) {
            String guideId = safe(result == null ? null : result.guideId).trim();
            String text = (
                safe(result == null ? null : result.title) + " " +
                    safe(result == null ? null : result.sectionHeading) + " " +
                    safe(result == null ? null : result.snippet) + " " +
                    safe(result == null ? null : result.body) + " " +
                    safe(result == null ? null : result.topicTags) + " " +
                    safe(result == null ? null : result.structureType)
            ).toLowerCase(Locale.US);
            if ("GD-345".equalsIgnoreCase(guideId)
                || (text.contains("rain") && text.contains("shelter"))
                || (text.contains("tarp") && text.contains("cord"))
                || text.contains("ridgeline")) {
                return true;
            }
        }
        return false;
    }

    private static SearchResult bestRainShelterSource(List<SearchResult> adjacent) {
        if (adjacent == null || adjacent.isEmpty()) {
            return null;
        }
        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < adjacent.size(); index++) {
            SearchResult source = adjacent.get(index);
            if (source == null) {
                continue;
            }
            String guideId = safe(source.guideId).trim();
            String text = (
                safe(source.title) + " " +
                    safe(source.sectionHeading) + " " +
                    safe(source.snippet) + " " +
                    safe(source.body) + " " +
                    safe(source.topicTags) + " " +
                    safe(source.structureType)
            ).toLowerCase(Locale.US);
            int score = Math.max(0, 12 - index);
            if ("GD-345".equalsIgnoreCase(guideId)) {
                score += 30;
            }
            if (text.contains("tarp")) {
                score += 10;
            }
            if (text.contains("cord")) {
                score += 10;
            }
            if (text.contains("rain") || text.contains("ridgeline")) {
                score += 8;
            }
            if (best == null || score > bestScore) {
                best = source;
                bestScore = score;
            }
        }
        return best;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String clean = safe(value).trim();
            if (!clean.isEmpty()) {
                return clean;
            }
        }
        return "";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class ReviewSearchResultSpec {
        final String guideId;
        final String title;
        final String sectionHeading;
        final String snippet;
        final String category;
        final String contentRole;
        final String timeHorizon;
        final String structureType;
        final String topicTags;

        ReviewSearchResultSpec(
            String guideId,
            String title,
            String sectionHeading,
            String snippet,
            String category,
            String contentRole,
            String timeHorizon,
            String structureType,
            String topicTags
        ) {
            this.guideId = guideId;
            this.title = title;
            this.sectionHeading = sectionHeading;
            this.snippet = snippet;
            this.category = category;
            this.contentRole = contentRole;
            this.timeHorizon = timeHorizon;
            this.structureType = structureType;
            this.topicTags = topicTags;
        }
    }
}

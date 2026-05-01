package com.senku.mobile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

final class DetailTabletSourceOwnershipPolicy {
    static final class ActiveSourceSelection {
        final SearchResult source;
        final String selectedKey;

        ActiveSourceSelection(SearchResult source, String selectedKey) {
            this.source = source;
            this.selectedKey = safe(selectedKey).trim();
        }
    }

    private DetailTabletSourceOwnershipPolicy() {
    }

    static ActiveSourceSelection resolveActiveSource(
        List<SearchResult> sources,
        String selectedSourceKey
    ) {
        List<SearchResult> safeSources = sources == null ? Collections.emptyList() : sources;
        String resolvedKey = safe(selectedSourceKey).trim();
        if (!resolvedKey.isEmpty()) {
            for (SearchResult source : safeSources) {
                if (safe(buildSourceSelectionKey(source)).equals(resolvedKey)) {
                    return new ActiveSourceSelection(source, resolvedKey);
                }
            }
        }
        SearchResult fallback = firstRealSource(safeSources);
        if (fallback == null && !safeSources.isEmpty()) {
            fallback = safeSources.get(0);
        }
        return new ActiveSourceSelection(fallback, buildSourceSelectionKey(fallback));
    }

    static SearchResult resolveVisualOwnerSource(
        boolean answerMode,
        boolean explicitSelection,
        boolean reviewDemoMode,
        List<String> questions,
        SearchResult activeSource,
        List<SearchResult> sources
    ) {
        if (!answerMode
            || explicitSelection
            || (reviewDemoMode && !questionsHaveOwnedShelterTopic(questions))) {
            return activeSource;
        }
        SearchResult bestSource = bestThreadTopicSource(activeSource, sources, questions, reviewDemoMode);
        int bestScore = sourceThreadTopicScore(bestSource, questions, reviewDemoMode);
        return bestScore > 0 ? bestSource : null;
    }

    static String resolveTabletGuideId(
        boolean currentThreadDetailRoute,
        String threadAnchorGuideId,
        SearchResult activeSource,
        boolean allowFallback,
        String tabletEvidenceAnchorId,
        String currentGuideId,
        String displayGuideId
    ) {
        if (currentThreadDetailRoute) {
            return safe(threadAnchorGuideId).trim();
        }
        String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
        if (!guideId.isEmpty()) {
            return guideId;
        }
        if (!allowFallback) {
            return "";
        }
        String evidenceAnchorId = safe(tabletEvidenceAnchorId).trim();
        if (!evidenceAnchorId.isEmpty()) {
            return evidenceAnchorId;
        }
        String currentId = safe(currentGuideId).trim();
        if (!currentId.isEmpty()) {
            return currentId;
        }
        return safe(displayGuideId).trim();
    }

    static String tabletXRefRelationLabel(SearchResult guide) {
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        if ("GD-499".equalsIgnoreCase(guideId) || "GD-225".equalsIgnoreCase(guideId)) {
            return "REQUIRED";
        }
        String relationText = (
            safe(guide == null ? null : guide.contentRole) + " " +
                safe(guide == null ? null : guide.structureType) + " " +
                safe(guide == null ? null : guide.topicTags)
        ).replace('_', ' ').replace('-', ' ').toLowerCase(Locale.US);
        if (relationText.contains("required") || relationText.contains("prereq")) {
            return "REQUIRED";
        }
        if (relationText.contains("anchor") || relationText.contains("source")) {
            return "ANCHOR";
        }
        return "RELATED";
    }

    static String primaryGuideIdForSources(List<SearchResult> sources, boolean productReviewMode) {
        if (sources == null) {
            return "";
        }
        SearchResult readerFacing = readerFacingPrimarySourceForSources(
            sources,
            ReviewDemoPolicy.isSourceStackDemoEnabled(productReviewMode)
        );
        String readerFacingGuideId = safe(readerFacing == null ? null : readerFacing.guideId).trim();
        if (!readerFacingGuideId.isEmpty()) {
            return readerFacingGuideId;
        }
        for (SearchResult source : sources) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    static SearchResult firstRealSource(List<SearchResult> sources) {
        if (sources == null) {
            return null;
        }
        for (SearchResult source : sources) {
            if (source == null) {
                continue;
            }
            if (!safe(source.guideId).trim().isEmpty()
                || !safe(source.title).trim().isEmpty()
                || !safe(source.sectionHeading).trim().isEmpty()
                || !safe(source.body).trim().isEmpty()
                || !safe(source.snippet).trim().isEmpty()) {
                return source;
            }
        }
        return null;
    }

    private static SearchResult bestThreadTopicSource(
        SearchResult activeSource,
        List<SearchResult> sources,
        List<String> questions,
        boolean reviewDemoMode
    ) {
        SearchResult best = activeSource;
        int bestScore = sourceThreadTopicScore(activeSource, questions, reviewDemoMode);
        if (sources == null) {
            return best;
        }
        for (SearchResult source : sources) {
            int score = sourceThreadTopicScore(source, questions, reviewDemoMode);
            if (score > bestScore) {
                best = source;
                bestScore = score;
            }
        }
        return best;
    }

    private static int sourceThreadTopicScore(
        SearchResult source,
        List<String> questions,
        boolean reviewDemoMode
    ) {
        if (source == null || questions == null || questions.isEmpty()) {
            return 0;
        }
        String haystack = buildSourceTopicHaystack(source);
        if (!reviewDemoMode) {
            return genericSourceQuestionOverlapScore(haystack, questions);
        }
        int score = 0;
        for (String rawQuestion : questions) {
            String question = safe(rawQuestion).toLowerCase(Locale.US);
            if (question.contains("rain shelter") && haystack.contains("rain") && haystack.contains("shelter")) {
                score += 24;
            }
            if (question.contains("shelter") && haystack.contains("shelter")) {
                score += 10;
            }
            if (question.contains("rain") && haystack.contains("rain")) {
                score += 8;
            }
            if (question.contains("tarp") && haystack.contains("tarp")) {
                score += 8;
            }
            if (question.contains("cord") && haystack.contains("cord")) {
                score += 6;
            }
        }
        return score;
    }

    private static String buildSourceTopicHaystack(SearchResult source) {
        return (
            safe(source.guideId) + " " +
                safe(source.title) + " " +
                safe(source.sectionHeading) + " " +
                safe(source.category) + " " +
                safe(source.structureType) + " " +
                safe(source.topicTags)
        ).replace('_', ' ').toLowerCase(Locale.US);
    }

    private static int genericSourceQuestionOverlapScore(String haystack, List<String> questions) {
        if (safe(haystack).trim().isEmpty() || questions == null || questions.isEmpty()) {
            return 0;
        }
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        for (String rawQuestion : questions) {
            String question = safe(rawQuestion).replace('_', ' ').toLowerCase(Locale.US);
            for (String token : question.split("[^a-z0-9]+")) {
                if (isQuestionTopicToken(token)) {
                    tokens.add(token);
                }
            }
        }
        int score = 0;
        for (String token : tokens) {
            if (haystack.contains(token)) {
                score += 4;
            }
        }
        return score;
    }

    private static boolean isQuestionTopicToken(String token) {
        String clean = safe(token).trim();
        if (clean.length() < 4) {
            return false;
        }
        return !("about".equals(clean)
            || "after".equals(clean)
            || "build".equals(clean)
            || "from".equals(clean)
            || "have".equals(clean)
            || "next".equals(clean)
            || "should".equals(clean)
            || "simple".equals(clean)
            || "what".equals(clean)
            || "when".equals(clean)
            || "where".equals(clean)
            || "with".equals(clean));
    }

    private static boolean questionsHaveOwnedShelterTopic(List<String> questions) {
        if (questions == null) {
            return false;
        }
        StringBuilder combined = new StringBuilder();
        for (String question : questions) {
            combined.append(' ').append(safe(question).toLowerCase(Locale.US));
        }
        String text = combined.toString();
        return (text.contains("rain") && text.contains("shelter"))
            || text.contains("tarp shelter")
            || text.contains("ridgeline shelter")
            || (text.contains("tarp") && text.contains("cord") && text.contains("shelter"));
    }

    private static SearchResult readerFacingPrimarySourceForSources(
        List<SearchResult> sources,
        boolean reviewDemoSourcePolicy
    ) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < sources.size(); index++) {
            SearchResult source = sources.get(index);
            if (source == null) {
                continue;
            }
            int score = readerFacingSourceScoreForGuideId(source, index, reviewDemoSourcePolicy);
            if (best == null || score > bestScore) {
                best = source;
                bestScore = score;
            }
        }
        return bestScore > 0 ? best : null;
    }

    private static int readerFacingSourceScoreForGuideId(
        SearchResult source,
        int index,
        boolean reviewDemoSourcePolicy
    ) {
        String role = safe(source == null ? null : source.contentRole).replace('_', ' ').toLowerCase(Locale.US);
        String retrievalMode = safe(source == null ? null : source.retrievalMode).replace('_', ' ').toLowerCase(Locale.US);
        String combined = (
            safe(source == null ? null : source.title) + " " +
                safe(source == null ? null : source.sectionHeading) + " " +
                safe(source == null ? null : source.snippet) + " " +
                safe(source == null ? null : source.body) + " " +
                safe(source == null ? null : source.topicTags) + " " +
                safe(source == null ? null : source.category) + " " +
                safe(source == null ? null : source.structureType)
        ).replace('_', ' ').toLowerCase(Locale.US);
        int score = Math.max(0, 16 - index);
        if ("topic".equals(role)) {
            score += 22;
        }
        if (role.contains("reviewed source")) {
            score -= 14;
        }
        if (retrievalMode.contains("guide-focus") || retrievalMode.contains("guide focus")) {
            score += 6;
        }
        if (reviewDemoSourcePolicy) {
            if (combined.contains("rain") && combined.contains("shelter")) {
                score += 24;
            }
            if (combined.contains("tarp") && combined.contains("cord")) {
                score += 18;
            }
        }
        return score;
    }

    private static String buildSourceSelectionKey(SearchResult source) {
        return DetailProvenancePresentationFormatter.buildSourceSelectionKey(source);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

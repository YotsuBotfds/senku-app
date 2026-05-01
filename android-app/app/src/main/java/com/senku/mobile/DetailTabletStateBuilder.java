package com.senku.mobile;

import com.senku.ui.tablet.SourceState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

final class DetailTabletStateBuilder {
    private DetailTabletStateBuilder() {
    }

    static ArrayList<SourceState> buildSourceStates(
        List<SearchResult> sources,
        SearchResult anchorSource,
        SearchResult selectedSource
    ) {
        ArrayList<SourceState> states = new ArrayList<>();
        String anchorKey = buildSourceSelectionKey(anchorSource);
        String selectedKey = buildSourceSelectionKey(selectedSource);
        for (SearchResult source : sources == null ? Collections.<SearchResult>emptyList() : sources) {
            String sourceKey = buildSourceSelectionKey(source);
            states.add(new SourceState(
                sourceKey,
                safe(source == null ? null : source.guideId).trim(),
                safe(source == null ? null : source.title).trim(),
                !anchorKey.isEmpty() && anchorKey.equals(sourceKey),
                !selectedKey.isEmpty() && selectedKey.equals(sourceKey)
            ));
        }
        return states;
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
        String haystack = (
            safe(source.guideId) + " " +
                safe(source.title) + " " +
                safe(source.sectionHeading) + " " +
                safe(source.category) + " " +
                safe(source.structureType) + " " +
                safe(source.topicTags)
        ).replace('_', ' ').toLowerCase(Locale.US);
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

    private static String buildSourceSelectionKey(SearchResult source) {
        return DetailProvenancePresentationFormatter.buildSourceSelectionKey(source);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

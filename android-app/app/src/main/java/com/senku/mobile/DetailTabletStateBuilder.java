package com.senku.mobile;

import com.senku.ui.tablet.AnchorState;
import com.senku.ui.tablet.SourceState;
import com.senku.ui.tablet.ThreadTurnState;
import com.senku.ui.tablet.TabletDetailMode;
import com.senku.ui.tablet.XRefState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class DetailTabletStateBuilder {
    private DetailTabletStateBuilder() {
    }

    static final class GuideModeState {
        final String label;
        final String summary;
        final String anchorLabel;

        GuideModeState(String label, String summary, String anchorLabel) {
            this.label = safe(label);
            this.summary = safe(summary);
            this.anchorLabel = safe(anchorLabel);
        }
    }

    static final class ActiveTurnSelection {
        final String selectedTurnId;
        final DetailActivity.TabletTurnBinding turn;

        ActiveTurnSelection(String selectedTurnId, DetailActivity.TabletTurnBinding turn) {
            this.selectedTurnId = safe(selectedTurnId);
            this.turn = turn;
        }
    }

    static final class ActiveSourceSelection {
        final String selectedSourceKey;
        final SearchResult source;

        ActiveSourceSelection(String selectedSourceKey, SearchResult source) {
            this.selectedSourceKey = safe(selectedSourceKey).trim();
            this.source = source;
        }
    }

    static ActiveTurnSelection resolveActiveTurn(
        List<DetailActivity.TabletTurnBinding> turnBindings,
        String selectedTurnId
    ) {
        if (turnBindings == null || turnBindings.isEmpty()) {
            return new ActiveTurnSelection("", null);
        }
        String currentSelection = safe(selectedTurnId);
        for (DetailActivity.TabletTurnBinding turn : turnBindings) {
            if (safe(turn.id).equals(currentSelection)) {
                return new ActiveTurnSelection(currentSelection, turn);
            }
        }
        DetailActivity.TabletTurnBinding fallback = turnBindings.get(turnBindings.size() - 1);
        return new ActiveTurnSelection(safe(fallback.id), fallback);
    }

    static ActiveSourceSelection resolveActiveSource(
        List<SearchResult> sources,
        String selectedSourceKey
    ) {
        DetailTabletSourceOwnershipPolicy.ActiveSourceSelection selection =
            DetailTabletSourceOwnershipPolicy.resolveActiveSource(sources, selectedSourceKey);
        return new ActiveSourceSelection(selection.selectedKey, selection.source);
    }

    static GuideModeState buildGuideModeState(
        boolean answerMode,
        boolean currentThreadDetailRoute,
        String currentGuideModeLabel,
        String currentGuideModeSummary,
        String currentGuideModeAnchorLabel,
        SearchResult activeSource,
        List<DetailActivity.TabletTurnBinding> turnBindings,
        String threadTopicTitle
    ) {
        if (!answerMode) {
            return new GuideModeState(
                currentGuideModeLabel,
                currentGuideModeSummary,
                currentGuideModeAnchorLabel
            );
        }
        return new GuideModeState(
            currentThreadDetailRoute ? "THREAD" : "ANSWER",
            buildAnswerGuideModeSummary(activeSource, turnBindings, threadTopicTitle),
            buildTabletAnswerGuideModeAnchorLabel(currentThreadDetailRoute)
        );
    }

    static String buildTabletAnswerGuideModeSummary(String guideId, String threadTopic) {
        String cleanGuideId = safe(guideId).trim();
        if (!cleanGuideId.isEmpty()) {
            return cleanGuideId;
        }
        String cleanTopic = safe(threadTopic).trim();
        return cleanTopic.isEmpty() ? "Answer context" : cleanTopic;
    }

    static String buildTabletAnswerGuideModeAnchorLabel(boolean threadDetailRoute) {
        return threadDetailRoute ? "Thread sources" : "Sources";
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
            states.add(buildSourceState(source, anchorKey, selectedKey));
        }
        return states;
    }

    static ArrayList<SourceState> buildSourceRailStates(
        DetailActivity.TabletTurnBinding activeTurn,
        SearchResult visualOwnerSource
    ) {
        if (activeTurn == null) {
            return new ArrayList<>();
        }
        return buildSourceStates(
            activeTurn.sources,
            visualOwnerSource,
            visualOwnerSource
        );
    }

    static ArrayList<XRefState> buildXRefStates(List<SearchResult> guides) {
        ArrayList<XRefState> states = new ArrayList<>();
        for (SearchResult guide : guides == null ? Collections.<SearchResult>emptyList() : guides) {
            states.add(new XRefState(
                safe(guide == null ? null : guide.guideId).trim(),
                safe(guide == null ? null : guide.title).trim(),
                DetailTabletSourceOwnershipPolicy.tabletXRefRelationLabel(guide)
            ));
        }
        return states;
    }

    static ArrayList<ThreadTurnState> buildTurnStates(
        List<DetailActivity.TabletTurnBinding> turnBindings,
        DetailActivity.TabletTurnBinding activeTurn
    ) {
        ArrayList<ThreadTurnState> states = new ArrayList<>();
        for (DetailActivity.TabletTurnBinding turn : turnBindings == null
            ? Collections.<DetailActivity.TabletTurnBinding>emptyList()
            : turnBindings) {
            states.add(new ThreadTurnState(
                turn.id,
                turn.question,
                turn.answer,
                turn.status,
                activeTurn != null && turn.id.equals(activeTurn.id),
                turn.showQuestion
            ));
        }
        return states;
    }

    private static SourceState buildSourceState(
        SearchResult source,
        String anchorKey,
        String selectedKey
    ) {
        String sourceKey = buildSourceSelectionKey(source);
        return new SourceState(
            sourceKey,
            safe(source == null ? null : source.guideId).trim(),
            safe(source == null ? null : source.title).trim(),
            sourceKeyMatches(anchorKey, sourceKey),
            sourceKeyMatches(selectedKey, sourceKey)
        );
    }

    private static boolean sourceKeyMatches(String targetKey, String sourceKey) {
        return !targetKey.isEmpty() && targetKey.equals(sourceKey);
    }

    static SearchResult resolveVisualOwnerSource(
        boolean answerMode,
        boolean explicitSelection,
        boolean reviewDemoMode,
        List<String> questions,
        SearchResult activeSource,
        List<SearchResult> sources
    ) {
        return DetailTabletSourceOwnershipPolicy.resolveVisualOwnerSource(
            answerMode,
            explicitSelection,
            reviewDemoMode,
            questions,
            activeSource,
            sources
        );
    }

    static TabletDetailMode resolveDetailMode(boolean answerMode, int turnCount) {
        if (!answerMode) {
            return TabletDetailMode.Guide;
        }
        return isThreadDetailRoute(answerMode, turnCount)
            ? TabletDetailMode.Thread
            : TabletDetailMode.Answer;
    }

    static boolean resolveLandscapeFlag(
        boolean utilityRail,
        boolean tabletPortrait,
        boolean answerMode,
        TabletDetailMode detailMode,
        boolean emergencyFullHeightPage
    ) {
        return utilityRail
            || shouldUseTabletPortraitCompactStructuralShell(
                tabletPortrait,
                answerMode,
                detailMode,
                emergencyFullHeightPage
            );
    }

    static boolean shouldUseTabletPortraitCompactStructuralShell(
        boolean tabletPortrait,
        boolean answerMode,
        TabletDetailMode detailMode,
        boolean emergencyFullHeightPage
    ) {
        return tabletPortrait
            && answerMode
            && emergencyFullHeightPage;
    }

    static SearchResult resolveDisplaySource(
        boolean answerMode,
        List<DetailActivity.TabletTurnBinding> turnBindings,
        SearchResult activeSource
    ) {
        if (!answerMode || turnBindings == null || turnBindings.size() <= 1) {
            return activeSource;
        }
        SearchResult best = activeSource;
        int bestScore = sourceThreadTopicScore(activeSource, turnBindings);
        for (DetailActivity.TabletTurnBinding turn : turnBindings) {
            if (turn == null || turn.sources == null) {
                continue;
            }
            for (SearchResult source : turn.sources) {
                int score = sourceThreadTopicScore(source, turnBindings);
                if (score > bestScore) {
                    bestScore = score;
                    best = source;
                }
            }
        }
        return best;
    }

    static String buildGuideTitle(
        boolean answerMode,
        SearchResult activeSource,
        List<DetailActivity.TabletTurnBinding> turnBindings,
        String evidenceAnchorTitle,
        String currentTitle
    ) {
        String answerTopic = buildAnswerTopicTitle(answerMode, turnBindings);
        if (answerMode && !answerTopic.isEmpty()) {
            return answerTopic;
        }
        String title = safe(activeSource == null ? null : activeSource.title).trim();
        if (!title.isEmpty()) {
            return title;
        }
        if (!safe(evidenceAnchorTitle).trim().isEmpty()) {
            return safe(evidenceAnchorTitle).trim();
        }
        if (!safe(currentTitle).trim().isEmpty()) {
            return safe(currentTitle).trim();
        }
        return "Guide evidence";
    }

    static int buildGuideSectionCount(boolean answerMode, SearchResult displaySource) {
        if (answerMode) {
            return 0;
        }
        String sourceBody = safe(displaySource == null ? null : displaySource.body);
        String displayBody = DetailGuidePresentationFormatter.buildGuideBody(displaySource);
        return DetailGuidePresentationFormatter.inferGuideSectionCountForRail(
            displaySource,
            sourceBody,
            displayBody
        );
    }

    static String buildThreadTopicTitle(
        boolean answerMode,
        List<DetailActivity.TabletTurnBinding> turnBindings
    ) {
        if (!answerMode || turnBindings == null || turnBindings.size() <= 1) {
            return "";
        }
        return buildAnswerTopicTitle(answerMode, turnBindings);
    }

    static String buildAnswerTopicTitle(
        boolean answerMode,
        List<DetailActivity.TabletTurnBinding> turnBindings
    ) {
        if (!answerMode || turnBindings == null || turnBindings.isEmpty()) {
            return "";
        }
        return buildAnswerTopicTitleForQuestions(tabletTurnQuestions(turnBindings), turnBindings.size());
    }

    static String buildAnswerTopicTitleForQuestions(List<String> questions, int turnCount) {
        int safeTurnCount = Math.max(1, turnCount);
        String suffix = safeTurnCount > 1 ? " - " + safeTurnCount + " turns" : "";
        if (questions != null) {
            for (String rawQuestion : questions) {
                String question = safe(rawQuestion).toLowerCase(Locale.US);
                if (question.contains("rain") && question.contains("shelter")) {
                    return "Rain shelter" + suffix;
                }
                if (question.contains("shelter")) {
                    return "Shelter thread" + suffix;
                }
            }
        }
        return safeTurnCount > 1 ? "Thread - " + safeTurnCount + " turns" : "";
    }

    private static String buildAnswerGuideModeSummary(
        SearchResult activeSource,
        List<DetailActivity.TabletTurnBinding> turnBindings,
        String threadTopicTitle
    ) {
        if (isThreadDetailRoute(turnBindings == null ? 0 : turnBindings.size())) {
            return "Sources in thread - " + countDistinctTabletThreadSources(turnBindings);
        }
        String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
        return buildTabletAnswerGuideModeSummary(guideId, threadTopicTitle);
    }

    private static boolean isThreadDetailRoute(int totalTurnCount) {
        return totalTurnCount > 1;
    }

    private static boolean isThreadDetailRoute(boolean answerMode, int turnCount) {
        return answerMode && turnCount > 1;
    }

    private static int countDistinctTabletThreadSources(List<DetailActivity.TabletTurnBinding> turnBindings) {
        if (turnBindings == null) {
            return 0;
        }
        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();
        for (DetailActivity.TabletTurnBinding turn : turnBindings) {
            if (turn == null || turn.sources == null) {
                continue;
            }
            for (SearchResult source : turn.sources) {
                String key = buildSourceSelectionKey(source);
                if (!key.isEmpty()) {
                    keys.add(key);
                }
            }
        }
        return keys.size();
    }

    private static int sourceThreadTopicScore(
        SearchResult source,
        List<DetailActivity.TabletTurnBinding> turnBindings
    ) {
        if (source == null || turnBindings == null || turnBindings.isEmpty()) {
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
        int score = 0;
        for (DetailActivity.TabletTurnBinding turn : turnBindings) {
            String question = safe(turn == null ? null : turn.question).toLowerCase(Locale.US);
            for (String token : question.split("[^a-z0-9]+")) {
                if (token.length() < 3) {
                    continue;
                }
                if (haystack.contains(token)) {
                    score += ("shelter".equals(token) || "rain".equals(token) || "tarp".equals(token) || "cord".equals(token))
                        ? 8
                        : 2;
                }
            }
        }
        return score;
    }

    private static ArrayList<String> tabletTurnQuestions(List<DetailActivity.TabletTurnBinding> turnBindings) {
        ArrayList<String> questions = new ArrayList<>();
        if (turnBindings == null) {
            return questions;
        }
        for (DetailActivity.TabletTurnBinding turn : turnBindings) {
            questions.add(safe(turn == null ? null : turn.question));
        }
        return questions;
    }

    static AnchorState buildAnchorState(
        boolean answerMode,
        String guideModeAnchorLabel,
        SearchResult activeSource,
        String evidenceSelectionKey,
        String evidenceAnchorId,
        String evidenceAnchorTitle,
        String evidenceAnchorSection,
        String evidenceAnchorSnippet
    ) {
        String sourceKey = buildSourceSelectionKey(activeSource);
        String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
        String title = safe(activeSource == null ? null : activeSource.title).trim();
        String section = safe(activeSource == null ? null : activeSource.sectionHeading).trim();
        if (!answerMode) {
            String handoffAnchorLabel = safe(guideModeAnchorLabel).trim();
            String handoffGuideId = extractGuideIdFromLabel(handoffAnchorLabel);
            if (!handoffGuideId.isEmpty() && !handoffGuideId.equalsIgnoreCase(guideId)) {
                return new AnchorState(
                    handoffGuideId.toLowerCase(Locale.US),
                    handoffGuideId,
                    stripGuideIdFromLabel(handoffAnchorLabel, handoffGuideId),
                    "",
                    "",
                    true
                );
            }
        }
        if (!sourceKey.isEmpty() && sourceKey.equals(evidenceSelectionKey)) {
            if (!safe(evidenceAnchorId).trim().isEmpty()) {
                guideId = safe(evidenceAnchorId).trim();
            }
            if (!safe(evidenceAnchorTitle).trim().isEmpty()) {
                title = safe(evidenceAnchorTitle).trim();
            }
            if (!safe(evidenceAnchorSection).trim().isEmpty()) {
                section = safe(evidenceAnchorSection).trim();
            }
        }
        return new AnchorState(
            sourceKey,
            guideId,
            title,
            section,
            sourceKey.equals(evidenceSelectionKey) ? safe(evidenceAnchorSnippet).trim() : "",
            !guideId.isEmpty() || !title.isEmpty()
        );
    }

    private static String extractGuideIdFromLabel(String label) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("(?i)\\bGD-\\d+\\b")
            .matcher(safe(label));
        return matcher.find() ? matcher.group().toUpperCase(Locale.US) : "";
    }

    private static String stripGuideIdFromLabel(String label, String guideId) {
        String cleaned = safe(label).trim();
        String id = safe(guideId).trim();
        if (id.isEmpty()) {
            return cleaned;
        }
        cleaned = cleaned.replaceFirst("(?i)^\\s*" + java.util.regex.Pattern.quote(id) + "\\s*(?:\\u00b7|-|,|:)?\\s*", "");
        return cleaned.trim();
    }

    private static String buildSourceSelectionKey(SearchResult source) {
        return DetailProvenancePresentationFormatter.buildSourceSelectionKey(source);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

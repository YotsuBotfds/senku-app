package com.senku.mobile;

import com.senku.ui.tablet.AnchorState;
import com.senku.ui.tablet.SourceState;
import com.senku.ui.tablet.ThreadTurnState;
import com.senku.ui.tablet.XRefState;

import java.util.ArrayList;
import java.util.Collections;
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
            states.add(buildSourceState(source, anchorKey, selectedKey));
        }
        return states;
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

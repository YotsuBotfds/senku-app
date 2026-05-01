package com.senku.mobile;

import com.senku.ui.tablet.SourceState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static String buildSourceSelectionKey(SearchResult source) {
        return DetailProvenancePresentationFormatter.buildSourceSelectionKey(source);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

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

    private static String buildSourceSelectionKey(SearchResult source) {
        return DetailProvenancePresentationFormatter.buildSourceSelectionKey(source);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

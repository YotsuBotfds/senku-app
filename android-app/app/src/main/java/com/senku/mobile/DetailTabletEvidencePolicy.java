package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DetailTabletEvidencePolicy {
    private static final int MAX_XREF_COUNT = 6;

    enum SelectionAction {
        NO_OP,
        CLEAR_WITHOUT_LOAD,
        LOAD_PREVIEW
    }

    static final class SelectionDecision {
        final SelectionAction action;
        final String selectionKey;
        final String guideId;
        final String anchorTitle;
        final String anchorSection;
        final int requestToken;

        SelectionDecision(
            SelectionAction action,
            String selectionKey,
            String guideId,
            String anchorTitle,
            String anchorSection,
            int requestToken
        ) {
            this.action = action == null ? SelectionAction.NO_OP : action;
            this.selectionKey = safe(selectionKey).trim();
            this.guideId = safe(guideId).trim();
            this.anchorTitle = safe(anchorTitle).trim();
            this.anchorSection = safe(anchorSection).trim();
            this.requestToken = requestToken;
        }

        boolean shouldApplySelection() {
            return action != SelectionAction.NO_OP;
        }

        boolean shouldLoadPreview() {
            return action == SelectionAction.LOAD_PREVIEW;
        }
    }

    static final class LoadedPreview {
        final String selectionKey;
        final String guideId;
        final String title;
        final String section;
        final String snippet;
        final ArrayList<SearchResult> xrefs;

        LoadedPreview(
            String selectionKey,
            String guideId,
            String title,
            String section,
            String snippet,
            List<SearchResult> xrefs
        ) {
            this.selectionKey = safe(selectionKey).trim();
            this.guideId = safe(guideId).trim();
            this.title = safe(title).trim();
            this.section = safe(section).trim();
            this.snippet = safe(snippet).trim();
            this.xrefs = new ArrayList<>(xrefs == null ? Collections.<SearchResult>emptyList() : xrefs);
        }
    }

    private DetailTabletEvidencePolicy() {
    }

    static SelectionDecision decideSelection(
        SearchResult activeSource,
        String currentSelectionKey,
        int currentLoadToken
    ) {
        String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
        String selectionKey = buildSourceSelectionKey(activeSource);
        int safeLoadToken = Math.max(0, currentLoadToken);
        if (selectionKey.equals(safe(currentSelectionKey))) {
            return new SelectionDecision(
                SelectionAction.NO_OP,
                selectionKey,
                guideId,
                activeSource == null ? "" : activeSource.title,
                activeSource == null ? "" : activeSource.sectionHeading,
                safeLoadToken
            );
        }
        SelectionAction action = guideId.isEmpty()
            ? SelectionAction.CLEAR_WITHOUT_LOAD
            : SelectionAction.LOAD_PREVIEW;
        return new SelectionDecision(
            action,
            selectionKey,
            guideId,
            activeSource == null ? "" : activeSource.title,
            activeSource == null ? "" : activeSource.sectionHeading,
            safeLoadToken + 1
        );
    }

    static LoadedPreview buildLoadedPreview(
        SelectionDecision selection,
        String snippet,
        SearchResult loadedGuide,
        List<SearchResult> xrefs
    ) {
        String title = safe(selection == null ? null : selection.anchorTitle).trim();
        String section = safe(selection == null ? null : selection.anchorSection).trim();
        if (loadedGuide != null) {
            if (title.isEmpty()) {
                title = safe(loadedGuide.title).trim();
            }
            if (section.isEmpty()) {
                section = safe(loadedGuide.sectionHeading).trim();
            }
        }
        return new LoadedPreview(
            selection == null ? "" : selection.selectionKey,
            selection == null ? "" : selection.guideId,
            title,
            section,
            snippet,
            xrefs
        );
    }

    static boolean shouldApplyLoadedPreview(
        int requestToken,
        int currentLoadToken,
        String currentSelectionKey,
        LoadedPreview preview,
        boolean finishing,
        boolean destroyed
    ) {
        return requestToken == currentLoadToken
            && !finishing
            && !destroyed
            && preview != null
            && safe(currentSelectionKey).equals(preview.selectionKey);
    }

    static boolean canLoadMoreXRefs(int loadedCount) {
        return loadedCount < MAX_XREF_COUNT;
    }

    static SearchResult resolvedXRefGuide(String guideId, SearchResult loadedGuide) {
        if (loadedGuide != null) {
            return loadedGuide;
        }
        return new SearchResult("", "", "", "", safe(guideId).trim(), "", "", "");
    }

    private static String buildSourceSelectionKey(SearchResult source) {
        return DetailProvenancePresentationFormatter.buildSourceSelectionKey(source);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

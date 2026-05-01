package com.senku.mobile;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class DetailRelatedGuidePreviewPolicy {
    private DetailRelatedGuidePreviewPolicy() {
    }

    static boolean shouldUsePreviewPanel(
        boolean hasPanel,
        boolean hasMeta,
        boolean hasBody,
        boolean hasOpenButton
    ) {
        return hasPanel && hasMeta && hasBody && hasOpenButton;
    }

    static boolean shouldUseAnswerModePreviewPanel(
        boolean answerMode,
        boolean previewPanelAvailable,
        boolean activeGuideContextPanelAvailable
    ) {
        return answerMode && previewPanelAvailable && activeGuideContextPanelAvailable;
    }

    static boolean shouldRenderActiveGuideContextPanel(
        boolean previewMode,
        boolean activeGuideContextPanelAvailable,
        boolean answerMode,
        String selectedRelatedGuideKey
    ) {
        return previewMode
            && activeGuideContextPanelAvailable
            && (!answerMode || !safe(selectedRelatedGuideKey).trim().isEmpty());
    }

    static String buildSelectionKey(SearchResult relatedGuide) {
        if (relatedGuide == null) {
            return "";
        }
        return (safe(relatedGuide.guideId).trim() + "|"
            + safe(relatedGuide.title).trim() + "|"
            + safe(relatedGuide.sectionHeading).trim()).toLowerCase(Locale.US);
    }

    static String resolvePreviewBody(
        String guideBodyPreview,
        SearchResult relatedGuide,
        String contextLabel,
        String loadingText,
        String emptyText,
        boolean loadingFallbackAllowed
    ) {
        String preview = safe(guideBodyPreview).trim();
        if (!preview.isEmpty()) {
            return preview;
        }
        String subtitle = safe(relatedGuide == null ? null : relatedGuide.subtitle).trim();
        if (!subtitle.isEmpty()) {
            return subtitle;
        }
        String cleanedContextLabel = safe(contextLabel).trim();
        if (!cleanedContextLabel.isEmpty()) {
            return cleanedContextLabel;
        }
        return loadingFallbackAllowed ? safe(loadingText) : safe(emptyText);
    }

    static boolean shouldLoadPreviewGuide(SearchResult relatedGuide) {
        return !safe(relatedGuide == null ? null : relatedGuide.guideId).trim().isEmpty();
    }

    static boolean shouldApplyLoadedPreview(
        boolean finishing,
        boolean destroyed,
        int requestToken,
        int activeToken,
        String originalSelectionKey,
        String selectedRelatedGuideKey
    ) {
        return !finishing
            && !destroyed
            && requestToken == activeToken
            && safe(originalSelectionKey).equals(safe(selectedRelatedGuideKey));
    }

    static SearchResult selectedSourceForRelatedGuideGraph(
        boolean answerMode,
        boolean sourceStackDemoEnabled,
        String selectedSourceKey,
        List<SearchResult> currentSources
    ) {
        List<SearchResult> safeSources = currentSources == null ? Collections.emptyList() : currentSources;
        if (!safe(selectedSourceKey).isEmpty()) {
            for (SearchResult source : safeSources) {
                if (safe(DetailProvenancePresentationFormatter.buildSourceSelectionKey(source)).equals(selectedSourceKey)) {
                    return safe(source == null ? null : source.guideId).trim().isEmpty()
                        ? null
                        : source;
                }
            }
        }
        SearchResult topicSource = answerMode && sourceStackDemoEnabled
            ? DetailRelatedGuidePresentationFormatter.rainShelterTopicSource(safeSources)
            : null;
        if (topicSource != null) {
            return topicSource;
        }
        for (SearchResult source : safeSources) {
            if (!safe(source == null ? null : source.guideId).trim().isEmpty()) {
                return source;
            }
        }
        return null;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

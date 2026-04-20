package com.senku.mobile;

import android.content.Context;
import android.view.View;

import java.util.List;
import java.util.Locale;

final class DetailProvenancePresentationFormatter {
    static final class State {
        final boolean utilityRail;
        final boolean landscapePhoneLayout;

        State(boolean utilityRail, boolean landscapePhoneLayout) {
            this.utilityRail = utilityRail;
            this.landscapePhoneLayout = landscapePhoneLayout;
        }
    }

    private final Context context;

    DetailProvenancePresentationFormatter(Context context) {
        this.context = context;
    }

    View resolveProvenanceScrollTarget(View provenancePanel, View sourcesPanel) {
        if (isVisible(provenancePanel)) {
            return provenancePanel;
        }
        if (isVisible(sourcesPanel)) {
            return sourcesPanel;
        }
        if (provenancePanel != null) {
            return provenancePanel;
        }
        return sourcesPanel;
    }

    boolean shouldUseSourceProvenancePanel(boolean provenancePanelAvailable, State state) {
        return provenancePanelAvailable && state != null && state.utilityRail;
    }

    boolean shouldUseCompactSourceProvenancePreview(boolean provenancePanelAvailable, boolean answerMode, State state) {
        return provenancePanelAvailable
            && answerMode
            && state != null
            && !state.utilityRail
            && !state.landscapePhoneLayout;
    }

    int getCollapsedProvenanceMaxLines(State state) {
        if (state != null && state.utilityRail) {
            return 5;
        }
        if (state != null && state.landscapePhoneLayout) {
            return 2;
        }
        return 3;
    }

    SearchResult selectedSourceForProvenanceAction(List<SearchResult> currentSources, String selectedSourceKey) {
        String resolvedKey = safe(selectedSourceKey).trim();
        if (!resolvedKey.isEmpty() && currentSources != null) {
            for (SearchResult source : currentSources) {
                if (safe(buildSourceSelectionKey(source)).equals(resolvedKey)) {
                    return source;
                }
            }
        }
        return firstRealSource(currentSources);
    }

    String buildPhoneProvenanceRegionDescription(SearchResult source, String entryValue, String sectionHeading) {
        String landmark = context.getString(R.string.detail_a11y_landmark_provenance);
        String resolvedEntryValue = safe(entryValue).trim();
        if (source == null || resolvedEntryValue.isEmpty()) {
            return landmark + ". " + context.getString(R.string.detail_a11y_provenance_none);
        }
        String section = trimHeaderLabel(safe(sectionHeading).trim());
        StringBuilder builder = new StringBuilder();
        builder.append(landmark);
        builder.append(". ");
        builder.append(context.getString(R.string.detail_provenance_title));
        builder.append(' ');
        builder.append(resolvedEntryValue);
        if (!section.isEmpty()) {
            builder.append(". ");
            builder.append(context.getString(R.string.detail_external_review_proof_section));
            builder.append(' ');
            builder.append(section);
        }
        builder.append('.');
        return builder.toString();
    }

    View resolvePhoneProvenanceTraversalAnchor(View sourcesTitleText, View sourcesPanel, View bodyLabel, View answerBubble) {
        if (isVisible(sourcesTitleText)) {
            return sourcesTitleText;
        }
        if (isVisible(sourcesPanel)) {
            return sourcesPanel;
        }
        if (isVisible(bodyLabel)) {
            return bodyLabel;
        }
        return answerBubble;
    }

    String buildProvenanceOpenButtonContentDescription(String sourceLabel) {
        String resolvedSourceLabel = safe(sourceLabel).trim();
        return resolvedSourceLabel.isEmpty()
            ? context.getString(R.string.detail_provenance_open)
            : "Open full guide: " + resolvedSourceLabel;
    }

    static String buildSourceSelectionKey(SearchResult source) {
        if (source == null) {
            return "";
        }
        return (safe(source.guideId).trim() + "|" +
            safe(source.sectionHeading).trim() + "|" +
            safe(source.title).trim()).toLowerCase(Locale.US);
    }

    private static SearchResult firstRealSource(List<SearchResult> sources) {
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

    private static String trimHeaderLabel(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.length() <= 34) {
            return cleaned;
        }
        return cleaned.substring(0, 34).trim() + "...";
    }

    private static boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

package com.senku.mobile;

import android.content.Context;

import java.util.Locale;

final class DetailRelatedGuidePresentationFormatter {
    static final class State {
        final boolean promotedCrossReferenceRail;
        final boolean nonRailCrossReferenceCopy;
        final String currentGuideId;
        final String activeGuideContextPrimaryLabel;
        final String sourceAnchorLabel;

        State(
            boolean promotedCrossReferenceRail,
            boolean nonRailCrossReferenceCopy,
            String currentGuideId,
            String activeGuideContextPrimaryLabel,
            String sourceAnchorLabel
        ) {
            this.promotedCrossReferenceRail = promotedCrossReferenceRail;
            this.nonRailCrossReferenceCopy = nonRailCrossReferenceCopy;
            this.currentGuideId = safe(currentGuideId);
            this.activeGuideContextPrimaryLabel = safe(activeGuideContextPrimaryLabel);
            this.sourceAnchorLabel = safe(sourceAnchorLabel);
        }
    }

    private final Context context;

    DetailRelatedGuidePresentationFormatter(Context context) {
        this.context = context;
    }

    String buildRelatedGuidesSubtitle(int count) {
        return context.getString(
            R.string.detail_next_steps_subtitle_guides,
            formatCountLabel(count, "linked guide", "linked guides")
        );
    }

    String buildStationRelatedGuidesSubtitle(State state, int count) {
        String anchorLabel = safe(state == null ? null : state.currentGuideId).trim();
        if (anchorLabel.isEmpty()) {
            anchorLabel = context.getString(R.string.detail_related_guides_anchor_fallback);
        }
        return context.getString(
            R.string.detail_related_guides_station_subtitle_live,
            formatCountLabel(count, "linked guide", "linked guides"),
            anchorLabel
        );
    }

    String buildRelatedGuidesPanelContentDescription(State state, int count) {
        State safeState = state == null
            ? new State(false, false, "", "", "")
            : state;
        StringBuilder builder = new StringBuilder();
        if (safeState.promotedCrossReferenceRail) {
            builder.append(context.getString(R.string.detail_loop2_field_links_panel_prefix));
        } else if (safeState.nonRailCrossReferenceCopy) {
            builder.append(context.getString(R.string.detail_related_guides_panel_prefix_nonrail));
        } else {
            builder.append(context.getString(R.string.detail_related_guides_panel_prefix_navigation));
        }
        builder.append(' ');
        builder.append(safeState.promotedCrossReferenceRail
            ? buildStationRelatedGuidesSubtitle(safeState, count)
            : (safeState.nonRailCrossReferenceCopy
                ? buildNonRailRelatedGuidesSubtitle(safeState, count)
                : buildRelatedGuidesSubtitle(count)));
        String anchorGuideId = safe(safeState.currentGuideId).trim();
        if (!anchorGuideId.isEmpty() && !safeState.nonRailCrossReferenceCopy) {
            builder.append(" Anchor guide ").append(anchorGuideId).append(".");
        }
        return builder.toString();
    }

    String buildNonRailRelatedGuidesTitle() {
        return context.getString(R.string.detail_next_steps_title_guides_nonrail);
    }

    String buildNonRailRelatedGuidesSubtitle(State state, int count) {
        String anchorLabel = safe(state == null ? null : state.activeGuideContextPrimaryLabel).trim();
        if (anchorLabel.isEmpty()) {
            anchorLabel = context.getString(R.string.detail_related_guides_anchor_fallback);
        }
        return formatCountLabel(count, "linked guide", "linked guides")
            + " for "
            + anchorLabel
            + ". Preview here. Open full guide when ready.";
    }

    String buildAnswerModeRelatedGuidesSubtitle(State state, int count) {
        return formatCountLabel(count, "linked guide", "linked guides")
            + " for "
            + resolveSourceAnchorLabel(state)
            + ". Preview here. Open full guide when ready.";
    }

    String buildAnswerModeRelatedGuidesPanelContentDescription(State state, int count) {
        return "Source-anchored cross-reference lane. " + buildAnswerModeRelatedGuidesSubtitle(state, count);
    }

    String buildRelatedGuideButtonLabel(SearchResult guide) {
        String primaryLabel = buildRelatedGuidePrimaryLabel(guide);
        String contextLabel = buildRelatedGuideContextLabel(guide);
        if (!contextLabel.isEmpty()) {
            return primaryLabel + "\n" + contextLabel;
        }
        return primaryLabel;
    }

    String buildRelatedGuidePrimaryLabel(SearchResult guide) {
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        String title = trimHeaderLabel(safe(guide == null ? null : guide.title).trim());
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return "[" + guideId + "] " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        return guideId.isEmpty() ? context.getString(R.string.detail_related_guide_fallback_label) : "[" + guideId + "]";
    }

    String buildRelatedGuideContextLabel(SearchResult guide) {
        String categoryLabel = formatRelatedGuideCategory(guide == null ? null : guide.category);
        return categoryLabel.isEmpty()
            ? context.getString(R.string.detail_related_guides_context_plain)
            : context.getString(R.string.detail_related_guides_context_category, categoryLabel);
    }

    String buildRelatedGuideButtonContentDescription(
        State state,
        SearchResult guide,
        int index,
        int total,
        boolean opensPreview
    ) {
        boolean nonRailCrossReferenceCopy = state != null && state.nonRailCrossReferenceCopy;
        StringBuilder builder = new StringBuilder();
        builder.append(opensPreview
            ? "Linked guide "
            : (nonRailCrossReferenceCopy
                ? context.getString(R.string.detail_related_guides_button_prefix_nonrail)
                : "Open linked guide "));
        builder.append(index + 1);
        builder.append(" of ");
        builder.append(total);
        builder.append(". ");
        builder.append(buildRelatedGuidePrimaryLabel(guide));
        String category = formatRelatedGuideCategory(guide == null ? null : guide.category);
        if (!category.isEmpty()) {
            builder.append(". Category ");
            builder.append(category);
        }
        String anchorGuideId = safe(state == null ? null : state.currentGuideId).trim();
        if (!anchorGuideId.isEmpty()) {
            builder.append(nonRailCrossReferenceCopy ? ". Cross-reference from " : ". Related to ");
            builder.append(anchorGuideId);
        }
        builder.append(opensPreview
            ? ". " + buildRelatedGuidePreviewRowBehaviorText(nonRailCrossReferenceCopy)
            : (nonRailCrossReferenceCopy
                ? ". " + context.getString(R.string.detail_related_guides_button_open_nonrail)
                : ". Opens another guide page in the installed pack."));
        return builder.toString();
    }

    String buildAnswerModeRelatedGuideButtonContentDescription(
        State state,
        SearchResult guide,
        int index,
        int total,
        boolean opensPreview
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cross-reference guide ");
        builder.append(index + 1);
        builder.append(" of ");
        builder.append(total);
        builder.append(". ");
        builder.append(buildRelatedGuidePrimaryLabel(guide));
        String category = formatRelatedGuideCategory(guide == null ? null : guide.category);
        if (!category.isEmpty()) {
            builder.append(". Category ");
            builder.append(category);
        }
        String anchorLabel = resolveSourceAnchorLabel(state);
        if (!anchorLabel.isEmpty()) {
            builder.append(". Linked from ");
            builder.append(anchorLabel);
        }
        builder.append(opensPreview
            ? ". Previews the linked guide on this page while staying anchored to the selected source guide. Use Open full guide when ready to switch pages."
            : ". Opens this guide with source-guide context.");
        return builder.toString();
    }

    String buildAnswerModeRelatedGuidesAnchorLabel(SearchResult sourceAnchor) {
        String guideId = safe(sourceAnchor == null ? null : sourceAnchor.guideId).trim();
        String title = trimHeaderLabel(safe(sourceAnchor == null ? null : sourceAnchor.title).trim());
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return "[" + guideId + "] " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        if (!guideId.isEmpty()) {
            return "[" + guideId + "]";
        }
        return context.getString(R.string.detail_related_guides_anchor_fallback);
    }

    String buildContextualFollowupQuery(State state, SearchResult relatedGuide, String anchorGuideId) {
        String targetTitle = trimHeaderLabel(safe(relatedGuide == null ? null : relatedGuide.title).trim());
        String targetGuideId = safe(relatedGuide == null ? null : relatedGuide.guideId).trim();
        String targetLabel = targetTitle.isEmpty() ? targetGuideId : targetTitle;
        if (targetLabel.isEmpty()) {
            return "";
        }
        String anchorLabel = safe(state == null ? null : state.sourceAnchorLabel).trim();
        if (anchorLabel.isEmpty()) {
            anchorLabel = safe(anchorGuideId).trim();
        }
        if (!anchorLabel.isEmpty()) {
            return "What should I know next about " + targetLabel + " for " + anchorLabel + "?";
        }
        return "What should I know next about " + targetLabel + "?";
    }

    private String buildRelatedGuidePreviewRowBehaviorText(boolean nonRailCrossReferenceCopy) {
        if (nonRailCrossReferenceCopy) {
            return "Preview this linked guide here, then use Open full guide when ready.";
        }
        return context.getString(R.string.detail_loop2_field_links_preview_row_behavior);
    }

    private String resolveSourceAnchorLabel(State state) {
        String sourceAnchorLabel = safe(state == null ? null : state.sourceAnchorLabel).trim();
        return sourceAnchorLabel.isEmpty()
            ? context.getString(R.string.detail_related_guides_anchor_fallback)
            : sourceAnchorLabel;
    }

    private static String formatCountLabel(int count, String singular, String plural) {
        int safeCount = Math.max(count, 0);
        return safeCount + " " + (safeCount == 1 ? singular : plural);
    }

    private static String trimHeaderLabel(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.length() <= 34) {
            return cleaned;
        }
        return cleaned.substring(0, 34).trim() + "...";
    }

    private static String formatRelatedGuideCategory(String category) {
        String cleaned = safe(category).trim().replace('-', ' ').replace('_', ' ');
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

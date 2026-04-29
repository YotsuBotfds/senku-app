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
        return formatCountLabel(count, "linked guide", "linked guides")
            + " \u00b7 required reading.";
    }

    String buildStationRelatedGuidesSubtitle(State state, int count) {
        String anchorLabel = safe(state == null ? null : state.currentGuideId).trim();
        if (anchorLabel.isEmpty()) {
            anchorLabel = fallbackAnchorLabel();
        }
        return formatCountLabel(count, "linked guide", "linked guides")
            + " \u00b7 opened from "
            + anchorLabel
            + ".";
    }

    String buildRelatedGuidesPanelContentDescription(State state, int count) {
        State safeState = state == null
            ? new State(false, false, "", "", "")
            : state;
        StringBuilder builder = new StringBuilder();
        if (safeState.promotedCrossReferenceRail) {
            builder.append("Cross-reference.");
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
        return "Cross-reference";
    }

    String buildNonRailRelatedGuidesSubtitle(State state, int count) {
        String anchorLabel = safe(state == null ? null : state.activeGuideContextPrimaryLabel).trim();
        if (anchorLabel.isEmpty()) {
            anchorLabel = fallbackAnchorLabel();
        }
        return formatCountLabel(count, "linked guide", "linked guides")
            + " \u00b7 required reading"
            + " \u00b7 "
            + anchorLabel
            + ".";
    }

    String buildAnswerModeRelatedGuidesSubtitle(State state, int count) {
        return formatCountLabel(count, "guide", "guides");
    }

    String buildAnswerModeRelatedGuidesTitle(int count) {
        int safeCount = Math.max(count, 0);
        return safeCount > 0 ? "RELATED GUIDES \u00b7 " + safeCount : "RELATED GUIDES";
    }

    String buildAnswerModeRelatedGuidesPanelContentDescription(State state, int count) {
        return "Related guides. " + buildAnswerModeRelatedGuidesSubtitle(state, count);
    }

    String buildAnswerModeRelatedGuideButtonLabel(SearchResult guide) {
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        String title = canonicalRainShelterRelatedGuideTitle(
            guideId,
            safe(guide == null ? null : guide.title).trim()
        );
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " \u00b7 " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        if (!guideId.isEmpty()) {
            return guideId;
        }
        return context == null
            ? "Related guide"
            : context.getString(R.string.detail_related_guide_fallback_label);
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
        String title = trimHeaderLabel(canonicalRainShelterRelatedGuideTitle(
            guideId,
            safe(guide == null ? null : guide.title).trim()
        ));
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " \u00b7 " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        return guideId.isEmpty() ? context.getString(R.string.detail_related_guide_fallback_label) : guideId;
    }

    String buildRelatedGuideContextLabel(SearchResult guide) {
        String categoryLabel = formatRelatedGuideCategory(guide);
        if (context == null) {
            return categoryLabel.isEmpty() ? "Related guide" : categoryLabel;
        }
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
            ? (nonRailCrossReferenceCopy ? "Cross-reference linked guide " : "Related guide ")
            : (nonRailCrossReferenceCopy
                ? "Open cross-reference linked guide "
                : "Open related guide "));
        builder.append(index + 1);
        builder.append(" of ");
        builder.append(total);
        builder.append(". ");
        builder.append(buildRelatedGuideCompactAccessibleLabel(guide));
        String category = formatRelatedGuideCategory(guide);
        if (!category.isEmpty()) {
            builder.append(". Category ");
            builder.append(category);
        }
        String anchorGuideId = safe(state == null ? null : state.currentGuideId).trim();
        if (!anchorGuideId.isEmpty()) {
            builder.append(nonRailCrossReferenceCopy ? ". Anchored to " : ". Related to ");
            builder.append(anchorGuideId);
        }
        builder.append(opensPreview
            ? ". " + buildRelatedGuidePreviewRowBehaviorText(nonRailCrossReferenceCopy)
            : (nonRailCrossReferenceCopy
                ? ". Opens the linked guide page in the installed pack."
                : ". Opens the linked guide page in the installed pack."));
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
        builder.append("Related guide ");
        builder.append(index + 1);
        builder.append(" of ");
        builder.append(total);
        builder.append(". ");
        builder.append(buildRelatedGuideCompactAccessibleLabel(guide));
        builder.append(opensPreview
            ? ". Previews here. Open full guide switches pages."
            : ". Opens this related guide.");
        return builder.toString();
    }

    String buildAnswerModeRelatedGuidesAnchorLabel(SearchResult sourceAnchor) {
        String guideId = safe(sourceAnchor == null ? null : sourceAnchor.guideId).trim();
        String title = trimHeaderLabel(safe(sourceAnchor == null ? null : sourceAnchor.title).trim());
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " \u00b7 " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        if (!guideId.isEmpty()) {
            return guideId;
        }
        return context.getString(R.string.detail_related_guides_anchor_fallback);
    }

    private String buildRelatedGuideAccessibleLabel(SearchResult guide) {
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        String title = safe(guide == null ? null : guide.title).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " \u00b7 " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        return guideId.isEmpty() ? context.getString(R.string.detail_related_guide_fallback_label) : guideId;
    }

    private String buildRelatedGuideCompactAccessibleLabel(SearchResult guide) {
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        String title = trimRelatedGuideAccessibleLabel(canonicalRainShelterRelatedGuideTitle(
            guideId,
            safe(guide == null ? null : guide.title).trim()
        ));
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " \u00b7 " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        return guideId.isEmpty() ? context.getString(R.string.detail_related_guide_fallback_label) : guideId;
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
            return "Preview here. Open full guide switches pages.";
        }
        return context.getString(R.string.detail_loop2_field_links_preview_row_behavior);
    }

    private String resolveSourceAnchorLabel(State state) {
        String sourceAnchorLabel = safe(state == null ? null : state.sourceAnchorLabel).trim();
        return sourceAnchorLabel.isEmpty()
            ? fallbackAnchorLabel()
            : sourceAnchorLabel;
    }

    private String fallbackAnchorLabel() {
        return context == null
            ? "this guide"
            : context.getString(R.string.detail_related_guides_anchor_fallback);
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

    private static String trimRelatedGuideAccessibleLabel(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.length() <= 42) {
            return cleaned;
        }
        return cleaned.substring(0, 42).trim() + "...";
    }

    private static String formatRelatedGuideCategory(SearchResult guide) {
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        String cleaned = firstNonEmpty(
            guide == null ? null : guide.category,
            guide == null ? null : guide.contentRole,
            guide == null ? null : guide.structureType,
            guide == null ? null : guide.retrievalMode,
            guide == null ? null : guide.topicTags
        );
        cleaned = cleaned.trim().replace('-', ' ').replace('_', ' ');
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        String normalized = cleaned.toLowerCase(Locale.US);
        String title = safe(guide == null ? null : guide.title).trim().toLowerCase(Locale.US);
        if ("GD-220".equalsIgnoreCase(guideId)
            && title.contains("abrasives")
            && (normalized.contains("related") || normalized.contains("cross reference") || normalized.contains("crossref"))) {
            return "Anchor";
        }
        if (normalized.contains("anchor") || normalized.contains("source")) {
            return "Anchor";
        }
        if (normalized.contains("required") || normalized.contains("prereq")) {
            return "Required";
        }
        if (normalized.contains("related") || normalized.contains("cross reference") || normalized.contains("crossref")) {
            return "Cross-ref";
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private static String canonicalRainShelterRelatedGuideTitle(String guideId, String title) {
        String cleanedTitle = safe(title).trim();
        String normalizedTitle = cleanedTitle.toLowerCase(Locale.US);
        if ("GD-294".equalsIgnoreCase(guideId)
            && normalizedTitle.contains("cave shelter systems")) {
            return "Cave Shelter Systems & Cold-Weather";
        }
        if ("GD-695".equalsIgnoreCase(guideId)
            && normalizedTitle.contains("hurricane")
            && normalizedTitle.contains("severe storm")) {
            return "Hurricane & Severe Storm Sheltering";
        }
        if ("GD-484".equalsIgnoreCase(guideId)
            && normalizedTitle.contains("insulation materials")) {
            return "Insulation Materials & Cold-Soak";
        }
        if ("GD-027".equalsIgnoreCase(guideId)
            && normalizedTitle.contains("primitive technology")) {
            return "Primitive Technology & Stone Age";
        }
        return cleanedTitle;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String cleaned = safe(value).trim();
            if (!cleaned.isEmpty()) {
                return cleaned;
            }
        }
        return "";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

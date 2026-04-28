package com.senku.mobile;

import android.content.Context;

import java.util.Locale;

final class DetailGuideContextPresentationFormatter {
    static final class State {
        final boolean answerMode;
        final boolean activeGuideContextPanel;
        final boolean nonRailCrossReferenceCopy;
        final boolean utilityRail;
        final String currentGuideId;
        final String currentTitle;
        final String currentSubtitle;
        final String currentGuideModeLabel;
        final String currentGuideModeSummary;
        final String sourceAnchorLabel;
        final String sourceAnchorSubtitle;

        State(
            boolean answerMode,
            boolean activeGuideContextPanel,
            boolean nonRailCrossReferenceCopy,
            boolean utilityRail,
            String currentGuideId,
            String currentTitle,
            String currentSubtitle,
            String currentGuideModeLabel,
            String currentGuideModeSummary,
            String sourceAnchorLabel,
            String sourceAnchorSubtitle
        ) {
            this.answerMode = answerMode;
            this.activeGuideContextPanel = activeGuideContextPanel;
            this.nonRailCrossReferenceCopy = nonRailCrossReferenceCopy;
            this.utilityRail = utilityRail;
            this.currentGuideId = safe(currentGuideId);
            this.currentTitle = safe(currentTitle);
            this.currentSubtitle = safe(currentSubtitle);
            this.currentGuideModeLabel = safe(currentGuideModeLabel);
            this.currentGuideModeSummary = safe(currentGuideModeSummary);
            this.sourceAnchorLabel = safe(sourceAnchorLabel);
            this.sourceAnchorSubtitle = safe(sourceAnchorSubtitle);
        }
    }

    private final Context context;

    DetailGuideContextPresentationFormatter(Context context) {
        this.context = context;
    }

    String buildRelatedGuidePreviewRowBehaviorText(State state) {
        if (state.answerMode || state.nonRailCrossReferenceCopy) {
            return "Preview here. Open full guide switches pages.";
        }
        return context.getString(R.string.detail_loop2_field_links_preview_row_behavior);
    }

    String buildRelatedGuidePreviewTitleText(State state) {
        if (!state.answerMode) {
            return "Selected linked guide";
        }
        return "Cross-reference";
    }

    String buildRelatedGuidePreviewCaptionText(State state) {
        if (state.answerMode || state.nonRailCrossReferenceCopy) {
            return "Preview selected guide before switching pages.";
        }
        if (state.activeGuideContextPanel) {
            return buildRelatedGuidePreviewComparisonCaption(state);
        }
        return context.getString(R.string.detail_related_guides_preview_caption);
    }

    String buildRelatedGuidePreviewPanelDescriptionText(State state) {
        if (state.answerMode) {
            String sourceLabel = buildActiveGuideContextPrimaryLabel(state);
            if (!sourceLabel.isEmpty()) {
                return "Cross-reference \u00b7 anchored to "
                    + sourceLabel
                    + ". Preview linked guides here.";
            }
            return "Cross-reference. Preview linked guides here.";
        }
        if (state.activeGuideContextPanel) {
            return buildRelatedGuidePreviewPanelDescription(state);
        }
        if (state.nonRailCrossReferenceCopy) {
            return "Cross-reference. Preview linked guides here.";
        }
        return context.getString(R.string.detail_loop2_field_links_preview_panel_description);
    }

    String buildActiveGuideContextPrimaryLabel(State state) {
        if (state.answerMode) {
            String sourceAnchorLabel = safe(state.sourceAnchorLabel).trim();
            return sourceAnchorLabel.isEmpty()
                ? context.getString(R.string.detail_related_guides_anchor_fallback)
                : sourceAnchorLabel;
        }
        String guideId = safe(state.currentGuideId).trim();
        String title = trimHeaderLabel(safe(state.currentTitle).trim());
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

    String buildActiveGuideContextBody(State state) {
        if (state.answerMode) {
            String subtitle = safe(state.sourceAnchorSubtitle).trim();
            String anchorText = "Pinned source for cross-reference.";
            if (!subtitle.isEmpty()) {
                return subtitle + "\n\n" + anchorText;
            }
            return anchorText;
        }
        String subtitle = safe(state.currentSubtitle).trim();
        if (!subtitle.isEmpty()) {
            return context.getString(R.string.detail_active_guide_context_body_with_subtitle, subtitle);
        }
        return context.getString(R.string.detail_active_guide_context_body_default);
    }

    CharSequence buildActiveGuideContextTitleText(State state) {
        return state.answerMode ? "Source guide" : context.getString(R.string.detail_active_guide_context_title);
    }

    String buildActiveGuideContextContentDescription(State state, String guideLabel) {
        if (state.answerMode) {
            return "Source guide context. Cross-reference pinned to " + guideLabel + ".";
        }
        return context.getString(R.string.detail_active_guide_context_content_description, guideLabel);
    }

    String buildRelatedGuidePreviewLoadingText(State state) {
        return state.nonRailCrossReferenceCopy
            ? "Loading cross-reference..."
            : context.getString(R.string.detail_loop2_field_links_preview_loading);
    }

    String buildRelatedGuidePreviewEmptyText(State state) {
        return state.nonRailCrossReferenceCopy
            ? "No cross-reference text is available in this pack."
            : context.getString(R.string.detail_loop2_field_links_preview_empty);
    }

    String buildGuideModeChipText(State state) {
        String handoffLabel = safe(state.currentGuideModeLabel).trim();
        return handoffLabel.isEmpty() ? context.getString(R.string.detail_mode_guide) : handoffLabel;
    }

    String buildGuideModeSummaryText(State state) {
        String handoffSummary = safe(state.currentGuideModeSummary).trim();
        return handoffSummary.isEmpty() ? context.getString(R.string.detail_mode_summary_guide) : handoffSummary;
    }

    String buildCurrentGuideHandoffLabel(State state) {
        if (state.answerMode) {
            return "";
        }
        return state.utilityRail
            ? guideRailLabel(context)
            : "Cross-reference";
    }

    String buildCurrentGuideHandoffSummary(State state) {
        return buildGuideHandoffSummaryText(
            context,
            buildCurrentGuideHandoffLabel(state),
            buildActiveGuideContextPrimaryLabel(state)
        );
    }

    static String guideRailLabel(Context context) {
        return "Cross-reference";
    }

    static String buildGuideHandoffSummaryText(Context context, String handoffLabel, String anchorLabel) {
        String resolvedLabel = safe(handoffLabel).trim();
        if (resolvedLabel.isEmpty()) {
            return "";
        }
        String resolvedAnchor = safe(anchorLabel).trim();
        if (resolvedAnchor.isEmpty()) {
            return context.getString(R.string.detail_guide_handoff_summary_no_anchor, resolvedLabel.toLowerCase(Locale.US));
        }
        return context.getString(
            R.string.detail_guide_handoff_summary,
            resolvedAnchor,
            resolvedLabel.toLowerCase(Locale.US)
        );
    }

    private String buildRelatedGuidePreviewComparisonCaption(State state) {
        return context.getString(
            R.string.detail_related_guides_preview_caption_compare,
            buildActiveGuideContextPrimaryLabel(state)
        );
    }

    private String buildRelatedGuidePreviewPanelDescription(State state) {
        return context.getString(
            R.string.detail_related_guides_preview_panel_description_compare,
            buildActiveGuideContextPrimaryLabel(state)
        );
    }

    private static String trimHeaderLabel(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.length() <= 34) {
            return cleaned;
        }
        return cleaned.substring(0, 34).trim() + "...";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

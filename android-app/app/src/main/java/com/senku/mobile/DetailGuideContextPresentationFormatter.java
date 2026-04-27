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
            return "Preview this linked guide here, then use Open full guide when ready.";
        }
        return context.getString(R.string.detail_loop2_field_links_preview_row_behavior);
    }

    String buildRelatedGuidePreviewTitleText(State state) {
        if (state.answerMode || state.activeGuideContextPanel || state.nonRailCrossReferenceCopy) {
            return "Selected linked guide";
        }
        return guideRailLabel(context);
    }

    String buildRelatedGuidePreviewCaptionText(State state) {
        if (state.answerMode || state.nonRailCrossReferenceCopy) {
            return "Preview selected linked guide here. Open full guide when ready.";
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
                return "Selected linked guide preview. Guide cross-reference stays anchored to "
                    + sourceLabel
                    + " while this preview is open. Use Open full guide to navigate to the selected guide page.";
            }
            return "Selected linked guide preview. Inspect it on this page, then use Open full guide to navigate to the selected guide page.";
        }
        if (state.activeGuideContextPanel) {
            return buildRelatedGuidePreviewPanelDescription(state);
        }
        if (state.nonRailCrossReferenceCopy) {
            return "Selected linked guide preview. Inspect it on this page, then use Open full guide to navigate to the selected guide page.";
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

    String buildActiveGuideContextBody(State state) {
        if (state.answerMode) {
            String subtitle = safe(state.sourceAnchorSubtitle).trim();
            String anchorText = "Guide cross-reference stays anchored to this selected source while the preview is open.";
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
        return state.answerMode ? "Selected source guide" : context.getString(R.string.detail_active_guide_context_title);
    }

    String buildActiveGuideContextContentDescription(State state, String guideLabel) {
        if (state.answerMode) {
            return "Selected source guide context. Guide cross-reference stays anchored to " + guideLabel + ".";
        }
        return context.getString(R.string.detail_active_guide_context_content_description, guideLabel);
    }

    String buildRelatedGuidePreviewLoadingText(State state) {
        return state.nonRailCrossReferenceCopy
            ? "Loading linked guide preview..."
            : context.getString(R.string.detail_loop2_field_links_preview_loading);
    }

    String buildRelatedGuidePreviewEmptyText(State state) {
        return state.nonRailCrossReferenceCopy
            ? "No linked guide preview text is available in this pack."
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
        return context.getString(R.string.detail_loop2_field_links_title);
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

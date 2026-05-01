package com.senku.mobile;

final class SearchLinkedGuideCuePresentation {
    static final float PREVIEW_LINE_ALPHA = 0.92f;

    final boolean showCue;
    final boolean showPreviewLine;
    final boolean bindCueAction;
    final boolean bindPreviewAction;
    final String cueLabel;
    final float cueAlpha;
    final String cueContentDescription;
    final String previewLine;
    final float previewLineAlpha;
    final String previewLineContentDescription;
    final String actionLabel;

    private SearchLinkedGuideCuePresentation(
        boolean showCue,
        boolean showPreviewLine,
        boolean bindCueAction,
        boolean bindPreviewAction,
        String cueLabel,
        float cueAlpha,
        String cueContentDescription,
        String previewLine,
        float previewLineAlpha,
        String previewLineContentDescription,
        String actionLabel
    ) {
        this.showCue = showCue;
        this.showPreviewLine = showPreviewLine;
        this.bindCueAction = bindCueAction;
        this.bindPreviewAction = bindPreviewAction;
        this.cueLabel = cueLabel == null ? "" : cueLabel;
        this.cueAlpha = cueAlpha;
        this.cueContentDescription = cueContentDescription == null ? "" : cueContentDescription;
        this.previewLine = previewLine == null ? "" : previewLine;
        this.previewLineAlpha = previewLineAlpha;
        this.previewLineContentDescription = previewLineContentDescription == null ? "" : previewLineContentDescription;
        this.actionLabel = actionLabel == null ? "" : actionLabel;
    }

    static SearchLinkedGuideCuePresentation hidden() {
        return new SearchLinkedGuideCuePresentation(
            false,
            false,
            false,
            false,
            "",
            0f,
            "",
            "",
            0f,
            "",
            ""
        );
    }

    static SearchLinkedGuideCuePresentation decide(
        boolean suppressCue,
        SearchResultAdapter.LinkedGuidePreview preview,
        boolean compactLinkedCue,
        boolean allowLinkedGuidePreviewLine,
        boolean stressCompactCard,
        boolean richTabletCard
    ) {
        if (suppressCue || stressCompactCard || preview == null || !preview.hasTargetGuide()) {
            return hidden();
        }

        String previewLabel = SearchResultCardModelMapper.cleanDisplayText(
            SearchResultCardModelMapper.buildLinkedGuidePreviewLabel(
                preview.displayLabel,
                preview.guideId,
                preview.title
            ),
            richTabletCard ? 92 : 72
        );
        String actionLabel = buildActionLabel(preview);
        boolean usePreviewLineAction = allowLinkedGuidePreviewLine && !previewLabel.isEmpty();
        if (!usePreviewLineAction) {
            return new SearchLinkedGuideCuePresentation(
                true,
                false,
                true,
                false,
                SearchResultCardModelMapper.buildCompactLinkedGuideCueLabel(
                    preview.displayLabel,
                    preview.guideId,
                    preview.title,
                    compactLinkedCue
                ),
                richTabletCard ? 0.72f : 0.94f,
                SearchResultCardModelMapper.buildLinkedGuideOpenDescription(actionLabel),
                "",
                0f,
                "",
                actionLabel
            );
        }

        return new SearchLinkedGuideCuePresentation(
            true,
            allowLinkedGuidePreviewLine,
            false,
            allowLinkedGuidePreviewLine,
            "Guide connection",
            0.78f,
            SearchResultCardModelMapper.buildLinkedGuideAvailableDescription(actionLabel),
            SearchResultCardModelMapper.buildLinkedGuidePreviewLine(
                preview.displayLabel,
                preview.guideId,
                preview.title,
                richTabletCard
            ),
            PREVIEW_LINE_ALPHA,
            SearchResultCardModelMapper.buildLinkedGuideOpenDescription(actionLabel),
            actionLabel
        );
    }

    private static String buildActionLabel(SearchResultAdapter.LinkedGuidePreview preview) {
        if (preview == null) {
            return "";
        }
        String guideId = safe(preview.guideId).trim();
        String title = safe(preview.title).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " - " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        return guideId;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

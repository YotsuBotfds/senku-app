package com.senku.mobile;

final class SearchLinkedGuideCuePolicy {
    private SearchLinkedGuideCuePolicy() {
    }

    static boolean shouldShowPreviewLine() {
        return false;
    }

    static String chipLabel() {
        return "Guide";
    }

    static String previewLineLabel() {
        return "Guide";
    }

    static String previewLine(
        String displayLabel,
        String guideId,
        String title,
        boolean richTabletCard
    ) {
        if (SearchResultCardModelMapper.safe(guideId).trim().isEmpty()) {
            return "";
        }
        String cleanedTitle = SearchResultCardModelMapper.cleanDisplayText(title, richTabletCard ? 54 : 42);
        if (!cleanedTitle.isEmpty()) {
            return previewLineLabel() + ": " + cleanedTitle;
        }
        String label = SearchResultCardModelMapper.cleanDisplayText(
            previewLabel(displayLabel, guideId, title),
            richTabletCard ? 58 : 46
        );
        return label.isEmpty() ? previewLineLabel() : previewLineLabel() + ": " + label;
    }

    static String previewLabel(String displayLabel, String guideId, String title) {
        String cleanedDisplayLabel = SearchResultCardModelMapper.safe(displayLabel).trim();
        if (!cleanedDisplayLabel.isEmpty()) {
            return cleanedDisplayLabel;
        }
        String cleanedGuideId = SearchResultCardModelMapper.safe(guideId).trim();
        String cleanedTitle = SearchResultCardModelMapper.safe(title).trim();
        if (!cleanedGuideId.isEmpty() && !cleanedTitle.isEmpty()) {
            return cleanedGuideId + " - " + cleanedTitle;
        }
        if (!cleanedTitle.isEmpty()) {
            return cleanedTitle;
        }
        return cleanedGuideId;
    }

    static String compactCueLabel(
        String displayLabel,
        String guideId,
        String title,
        boolean compactLinkedCue
    ) {
        String cleanedGuideId = SearchResultCardModelMapper.safe(guideId).trim();
        if (!cleanedGuideId.isEmpty()) {
            if (compactLinkedCue) {
                return "Guide connection";
            }
            return SearchResultCardModelMapper.cleanDisplayText(
                "Linked guide " + cleanedGuideId,
                20
            );
        }
        String label = previewLabel(displayLabel, guideId, title);
        if (!label.isEmpty()) {
            return "Guide connection";
        }
        return "Guide connection";
    }

    static String availableDescription(String actionLabel) {
        String label = SearchResultCardModelMapper.safe(actionLabel).trim();
        return label.isEmpty()
            ? "Guide connection available"
            : "Guide connection available: " + label;
    }

    static String openDescription(String actionLabel) {
        String label = SearchResultCardModelMapper.safe(actionLabel).trim();
        return label.isEmpty()
            ? "Open cross-reference guide"
            : "Open cross-reference guide: " + label;
    }
}

package com.senku.mobile;

final class DetailSourceProvenanceCoordinator {
    private DetailSourceProvenanceCoordinator() {
    }

    static boolean shouldRevealAnswerSourceGraphAfterSelection(
        boolean answerMode,
        boolean compactPortraitPhone,
        String selectedSourceKey,
        int relatedGuideCount
    ) {
        return answerMode
            && compactPortraitPhone
            && !safe(selectedSourceKey).trim().isEmpty()
            && relatedGuideCount > 0;
    }

    static boolean shouldExpandAnswerSourceGraphAfterSourceSelection(
        boolean answerMode,
        boolean compactPortraitPhone,
        String selectedSourceKey
    ) {
        return answerMode
            && compactPortraitPhone
            && !safe(selectedSourceKey).trim().isEmpty();
    }

    static boolean shouldApplyRelatedGuidePreviewCtaClearance(
        boolean phoneXmlDetailLayoutActive,
        boolean compactPortraitPhone,
        boolean answerMode,
        boolean followUpVisible,
        boolean previewOpenVisible
    ) {
        return phoneXmlDetailLayoutActive
            && compactPortraitPhone
            && answerMode
            && followUpVisible
            && previewOpenVisible;
    }

    static int resolveRelatedGuidePreviewCtaBottomClearancePx(
        int currentContentBottomPaddingPx,
        int followUpPanelHeightPx,
        int extraClearancePx
    ) {
        return Math.max(
            0,
            Math.max(currentContentBottomPaddingPx, followUpPanelHeightPx + Math.max(0, extraClearancePx))
        );
    }

    static int computeScrollYToKeepViewBottomVisible(
        int targetTopPx,
        int targetHeightPx,
        int viewportHeightPx,
        int bottomClearancePx,
        int currentScrollY
    ) {
        int targetBottom = Math.max(0, targetTopPx) + Math.max(0, targetHeightPx);
        int desiredScrollY = targetBottom + Math.max(0, bottomClearancePx) - Math.max(0, viewportHeightPx);
        return Math.max(Math.max(0, currentScrollY), Math.max(0, desiredScrollY));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

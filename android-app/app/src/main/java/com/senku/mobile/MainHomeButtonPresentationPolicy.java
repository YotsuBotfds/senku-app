package com.senku.mobile;

final class MainHomeButtonPresentationPolicy {
    private static final int COMPACT_HOME_RELATED_MIN_HEIGHT_DP = 40;
    private static final int DEFAULT_HOME_RELATED_MIN_HEIGHT_DP = 46;
    private static final int COMPACT_HOME_RELATED_LEFT_PADDING_DP = 20;
    private static final int DEFAULT_HOME_RELATED_LEFT_PADDING_DP = 16;
    private static final int COMPACT_HOME_RELATED_TOP_PADDING_DP = 10;
    private static final int DEFAULT_HOME_RELATED_TOP_PADDING_DP = 12;
    private static final int COMPACT_HOME_RELATED_RIGHT_PADDING_DP = 12;
    private static final int DEFAULT_HOME_RELATED_RIGHT_PADDING_DP = 14;
    private static final int COMPACT_HOME_RELATED_BOTTOM_PADDING_DP = 9;
    private static final int DEFAULT_HOME_RELATED_BOTTOM_PADDING_DP = 10;
    private static final int COMPACT_HOME_RELATED_GAP_DP = 6;
    private static final int DEFAULT_HOME_RELATED_GAP_DP = 8;

    private MainHomeButtonPresentationPolicy() {
    }

    static HomeRelatedGuideButtonPresentation resolveHomeRelatedGuideButtonPresentation(
        boolean compactPhoneHome,
        int index
    ) {
        return new HomeRelatedGuideButtonPresentation(
            compactPhoneHome ? COMPACT_HOME_RELATED_MIN_HEIGHT_DP : DEFAULT_HOME_RELATED_MIN_HEIGHT_DP,
            compactPhoneHome ? COMPACT_HOME_RELATED_LEFT_PADDING_DP : DEFAULT_HOME_RELATED_LEFT_PADDING_DP,
            compactPhoneHome ? COMPACT_HOME_RELATED_TOP_PADDING_DP : DEFAULT_HOME_RELATED_TOP_PADDING_DP,
            compactPhoneHome ? COMPACT_HOME_RELATED_RIGHT_PADDING_DP : DEFAULT_HOME_RELATED_RIGHT_PADDING_DP,
            compactPhoneHome ? COMPACT_HOME_RELATED_BOTTOM_PADDING_DP : DEFAULT_HOME_RELATED_BOTTOM_PADDING_DP,
            compactPhoneHome,
            compactPhoneHome ? 1 : 2,
            compactPhoneHome,
            compactPhoneHome,
            index > 0 && compactPhoneHome ? COMPACT_HOME_RELATED_GAP_DP : 0,
            index > 0 && !compactPhoneHome ? DEFAULT_HOME_RELATED_GAP_DP : 0
        );
    }

    static final class HomeRelatedGuideButtonPresentation {
        final int minimumHeightDp;
        final int leftPaddingDp;
        final int topPaddingDp;
        final int rightPaddingDp;
        final int bottomPaddingDp;
        final boolean singleLine;
        final int maxLines;
        final boolean matchParentWidth;
        final boolean compactLabel;
        final int topMarginDp;
        final int startMarginDp;

        HomeRelatedGuideButtonPresentation(
            int minimumHeightDp,
            int leftPaddingDp,
            int topPaddingDp,
            int rightPaddingDp,
            int bottomPaddingDp,
            boolean singleLine,
            int maxLines,
            boolean matchParentWidth,
            boolean compactLabel,
            int topMarginDp,
            int startMarginDp
        ) {
            this.minimumHeightDp = minimumHeightDp;
            this.leftPaddingDp = leftPaddingDp;
            this.topPaddingDp = topPaddingDp;
            this.rightPaddingDp = rightPaddingDp;
            this.bottomPaddingDp = bottomPaddingDp;
            this.singleLine = singleLine;
            this.maxLines = maxLines;
            this.matchParentWidth = matchParentWidth;
            this.compactLabel = compactLabel;
            this.topMarginDp = topMarginDp;
            this.startMarginDp = startMarginDp;
        }
    }
}

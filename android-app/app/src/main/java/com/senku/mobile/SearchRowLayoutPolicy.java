package com.senku.mobile;

final class SearchRowLayoutPolicy {
    private static final int LANDSCAPE_ROW_TOP_PADDING_DP = 13;
    private static final int PORTRAIT_TABLET_ROW_TOP_PADDING_DP = 15;
    private static final int LANDSCAPE_ROW_TITLE_TOP_MARGIN_DP = 4;
    private static final int PORTRAIT_TABLET_ROW_TITLE_TOP_MARGIN_DP = 6;
    private static final int LANDSCAPE_ROW_SNIPPET_TOP_MARGIN_DP = 5;
    private static final int PORTRAIT_TABLET_ROW_SNIPPET_TOP_MARGIN_DP = 6;
    private static final int LANDSCAPE_ROW_DIVIDER_TOP_MARGIN_DP = 10;
    private static final int PORTRAIT_TABLET_ROW_DIVIDER_TOP_MARGIN_DP = 12;
    private static final float LANDSCAPE_ROW_TITLE_TEXT_SIZE_SP = 14.0f;
    private static final float LANDSCAPE_ROW_SNIPPET_TEXT_SIZE_SP = 11.0f;
    private static final float PORTRAIT_TABLET_ROW_TITLE_TEXT_SIZE_SP = 13.5f;
    private static final float PORTRAIT_TABLET_ROW_SNIPPET_TEXT_SIZE_SP = 11.5f;
    private static final float SECTION_TEXT_SIZE_SP = 8.5f;
    private static final float CHIP_TEXT_SIZE_SP = 8.5f;
    private static final float META_TEXT_SIZE_SP = 9.0f;
    private static final int META_LINE_HEIGHT_SP = 11;

    private SearchRowLayoutPolicy() {
    }

    static Metrics metrics(boolean landscapePhoneCard) {
        return new Metrics(
            landscapePhoneCard ? LANDSCAPE_ROW_TOP_PADDING_DP : PORTRAIT_TABLET_ROW_TOP_PADDING_DP,
            landscapePhoneCard ? LANDSCAPE_ROW_TITLE_TOP_MARGIN_DP : PORTRAIT_TABLET_ROW_TITLE_TOP_MARGIN_DP,
            landscapePhoneCard ? LANDSCAPE_ROW_SNIPPET_TOP_MARGIN_DP : PORTRAIT_TABLET_ROW_SNIPPET_TOP_MARGIN_DP,
            landscapePhoneCard ? LANDSCAPE_ROW_DIVIDER_TOP_MARGIN_DP : PORTRAIT_TABLET_ROW_DIVIDER_TOP_MARGIN_DP,
            landscapePhoneCard ? LANDSCAPE_ROW_TITLE_TEXT_SIZE_SP : PORTRAIT_TABLET_ROW_TITLE_TEXT_SIZE_SP,
            landscapePhoneCard ? LANDSCAPE_ROW_SNIPPET_TEXT_SIZE_SP : PORTRAIT_TABLET_ROW_SNIPPET_TEXT_SIZE_SP,
            SECTION_TEXT_SIZE_SP,
            CHIP_TEXT_SIZE_SP,
            META_TEXT_SIZE_SP,
            META_LINE_HEIGHT_SP
        );
    }

    static final class Metrics {
        final int topPaddingDp;
        final int titleTopMarginDp;
        final int snippetTopMarginDp;
        final int dividerTopMarginDp;
        final float titleTextSizeSp;
        final float snippetTextSizeSp;
        final float sectionTextSizeSp;
        final float chipTextSizeSp;
        final float metaTextSizeSp;
        final int metaLineHeightSp;

        Metrics(
            int topPaddingDp,
            int titleTopMarginDp,
            int snippetTopMarginDp,
            int dividerTopMarginDp,
            float titleTextSizeSp,
            float snippetTextSizeSp,
            float sectionTextSizeSp,
            float chipTextSizeSp,
            float metaTextSizeSp,
            int metaLineHeightSp
        ) {
            this.topPaddingDp = topPaddingDp;
            this.titleTopMarginDp = titleTopMarginDp;
            this.snippetTopMarginDp = snippetTopMarginDp;
            this.dividerTopMarginDp = dividerTopMarginDp;
            this.titleTextSizeSp = titleTextSizeSp;
            this.snippetTextSizeSp = snippetTextSizeSp;
            this.sectionTextSizeSp = sectionTextSizeSp;
            this.chipTextSizeSp = chipTextSizeSp;
            this.metaTextSizeSp = metaTextSizeSp;
            this.metaLineHeightSp = metaLineHeightSp;
        }
    }
}

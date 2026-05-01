package com.senku.mobile;

final class MainHomeCategoryPresentationPolicy {
    private static final int MANUAL_HOME_CATEGORY_COLUMNS = 3;
    private static final int PHONE_CARD_HEIGHT_DP = 74;
    private static final int TABLET_CARD_HEIGHT_DP = 68;
    private static final int PHONE_ROW_GAP_DP = 9;
    private static final int TABLET_ROW_GAP_DP = 10;

    private MainHomeCategoryPresentationPolicy() {
    }

    static int shelfMinimumHeightDp(int itemCount, boolean tabletHome) {
        int safeItemCount = Math.max(0, itemCount);
        if (safeItemCount == 0) {
            return 0;
        }
        int rows = (safeItemCount + MANUAL_HOME_CATEGORY_COLUMNS - 1) / MANUAL_HOME_CATEGORY_COLUMNS;
        int cardHeightDp = tabletHome ? TABLET_CARD_HEIGHT_DP : PHONE_CARD_HEIGHT_DP;
        int rowGapDp = tabletHome ? TABLET_ROW_GAP_DP : PHONE_ROW_GAP_DP;
        return rows * cardHeightDp + Math.max(0, rows - 1) * rowGapDp;
    }
}

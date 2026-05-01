package com.senku.mobile;

final class MainRecentThreadPresentationPolicy {
    private static final int MANUAL_HOME_RECENT_ROW_HEIGHT_DP = 70;
    private static final int MANUAL_HOME_RECENT_ROW_GAP_DP = 8;
    private static final int TABLET_MANUAL_HOME_RECENT_ROW_GAP_DP = 10;
    private static final int TABLET_RECENT_THREAD_HORIZONTAL_PADDING_DP = 9;
    private static final int MANUAL_HOME_RECENT_THREAD_HORIZONTAL_PADDING_DP = 12;
    private static final int COMPACT_RECENT_THREAD_HORIZONTAL_PADDING_DP = 10;
    private static final int DEFAULT_RECENT_THREAD_HORIZONTAL_PADDING_DP = 12;
    private static final int TABLET_RECENT_THREAD_VERTICAL_PADDING_DP = 9;
    private static final int MANUAL_HOME_RECENT_THREAD_VERTICAL_PADDING_DP = 8;
    private static final int COMPACT_RECENT_THREAD_VERTICAL_PADDING_DP = 8;
    private static final int DEFAULT_RECENT_THREAD_VERTICAL_PADDING_DP = 10;
    private static final int COMPACT_RECENT_THREAD_GAP_DP = 6;
    private static final int DEFAULT_RECENT_THREAD_GAP_DP = 8;
    private static final String LONG_PRESS_REMOVE_HINT = "Long press to remove.";

    private MainRecentThreadPresentationPolicy() {
    }

    static ButtonPresentation resolveButtonPresentation(
        boolean tabletSearchLayout,
        boolean manualHomeShell,
        boolean compactPhoneHome,
        int index
    ) {
        int horizontalPaddingDp;
        if (tabletSearchLayout) {
            horizontalPaddingDp = TABLET_RECENT_THREAD_HORIZONTAL_PADDING_DP;
        } else if (manualHomeShell) {
            horizontalPaddingDp = MANUAL_HOME_RECENT_THREAD_HORIZONTAL_PADDING_DP;
        } else {
            horizontalPaddingDp = compactPhoneHome
                ? COMPACT_RECENT_THREAD_HORIZONTAL_PADDING_DP
                : DEFAULT_RECENT_THREAD_HORIZONTAL_PADDING_DP;
        }
        return new ButtonPresentation(
            horizontalPaddingDp,
            resolveVerticalPaddingDp(tabletSearchLayout, manualHomeShell, compactPhoneHome),
            manualHomeShell || compactPhoneHome ? 2 : 3,
            resolveMinimumHeightDp(tabletSearchLayout, false, manualHomeShell),
            index > 0 ? resolveGapDp(tabletSearchLayout, manualHomeShell, compactPhoneHome) : 0
        );
    }

    static int resolveMinimumHeightDp(
        boolean tabletSearchLayout,
        boolean landscapePhoneLayout,
        boolean manualHomeShell
    ) {
        if (!manualHomeShell) {
            return 0;
        }
        if (tabletSearchLayout) {
            return MANUAL_HOME_RECENT_ROW_HEIGHT_DP;
        }
        return MANUAL_HOME_RECENT_ROW_HEIGHT_DP;
    }

    static int resolveGapDp(
        boolean tabletSearchLayout,
        boolean manualHomeShell,
        boolean compactPhoneHome
    ) {
        if (tabletSearchLayout) {
            return TABLET_MANUAL_HOME_RECENT_ROW_GAP_DP;
        }
        if (manualHomeShell) {
            return MANUAL_HOME_RECENT_ROW_GAP_DP;
        }
        return compactPhoneHome ? COMPACT_RECENT_THREAD_GAP_DP : DEFAULT_RECENT_THREAD_GAP_DP;
    }

    static int resolveVerticalPaddingDp(
        boolean tabletSearchLayout,
        boolean manualHomeShell,
        boolean compactPhoneHome
    ) {
        if (tabletSearchLayout) {
            return TABLET_RECENT_THREAD_VERTICAL_PADDING_DP;
        }
        if (manualHomeShell) {
            return MANUAL_HOME_RECENT_THREAD_VERTICAL_PADDING_DP;
        }
        return compactPhoneHome
            ? COMPACT_RECENT_THREAD_VERTICAL_PADDING_DP
            : DEFAULT_RECENT_THREAD_VERTICAL_PADDING_DP;
    }

    static boolean isLongPressRemoveHintEligible(ChatSessionStore.ConversationPreview preview) {
        return preview != null && !safe(preview.conversationId).trim().isEmpty();
    }

    static String contentDescriptionWithRemoveHint(String contentDescription, boolean longPressRemoveHintEligible) {
        String base = safe(contentDescription).trim();
        if (!longPressRemoveHintEligible || base.toLowerCase(java.util.Locale.US).contains("long press to remove")) {
            return base;
        }
        if (base.isEmpty()) {
            return LONG_PRESS_REMOVE_HINT;
        }
        return base.endsWith(".") ? base + " " + LONG_PRESS_REMOVE_HINT : base + ". " + LONG_PRESS_REMOVE_HINT;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class ButtonPresentation {
        final int horizontalPaddingDp;
        final int verticalPaddingDp;
        final int maxLines;
        final int minimumHeightDp;
        final int topMarginDp;

        ButtonPresentation(
            int horizontalPaddingDp,
            int verticalPaddingDp,
            int maxLines,
            int minimumHeightDp,
            int topMarginDp
        ) {
            this.horizontalPaddingDp = horizontalPaddingDp;
            this.verticalPaddingDp = verticalPaddingDp;
            this.maxLines = maxLines;
            this.minimumHeightDp = minimumHeightDp;
            this.topMarginDp = topMarginDp;
        }
    }
}

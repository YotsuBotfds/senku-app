package com.senku.mobile;

import android.view.ViewGroup;

final class DetailTabletEmergencyChromePolicy {
    private static final int APP_RAIL_WIDTH_DP = 72;
    private static final int APP_RAIL_DIVIDER_WIDTH_DP = 1;
    private static final int PORTRAIT_LEFT_MARGIN_DP = APP_RAIL_WIDTH_DP + APP_RAIL_DIVIDER_WIDTH_DP;
    private static final int PORTRAIT_RIGHT_MARGIN_DP = 24;
    private static final int PORTRAIT_TOP_MARGIN_DP = 0;
    private static final int LANDSCAPE_LEFT_MARGIN_DP = 336;
    private static final int LANDSCAPE_RIGHT_MARGIN_DP = 24;
    private static final int LANDSCAPE_TOP_MARGIN_DP = 16;
    private static final float APP_RAIL_LABEL_TEXT_SIZE_SP = 10.0f;
    private static final float APP_RAIL_LABEL_LINE_HEIGHT_SP = 13.0f;
    private static final float APP_RAIL_LABEL_LETTER_SPACING = 0.0f;

    private DetailTabletEmergencyChromePolicy() {
    }

    static Decision evaluate(
        boolean answerMode,
        boolean tabletPortrait,
        boolean emergencySurfaceEligible
    ) {
        boolean fullHeightPage = shouldUseFullHeightPage(answerMode, tabletPortrait, emergencySurfaceEligible);
        return new Decision(
            fullHeightPage,
            fullHeightPage,
            fullHeightPage,
            fullHeightPage,
            fullHeightPage,
            fullHeightPage && tabletPortrait,
            resolveOverlayMarginsDp(fullHeightPage || tabletPortrait),
            resolveOverlayHeight(fullHeightPage || tabletPortrait),
            activeDestination(),
            APP_RAIL_WIDTH_DP,
            APP_RAIL_DIVIDER_WIDTH_DP,
            APP_RAIL_LABEL_TEXT_SIZE_SP,
            APP_RAIL_LABEL_LINE_HEIGHT_SP,
            APP_RAIL_LABEL_LETTER_SPACING,
            R.string.detail_emergency_app_rail_manual_label,
            R.string.detail_emergency_app_rail_manual_content_description,
            false
        );
    }

    static boolean shouldUseFullHeightPage(
        boolean answerMode,
        boolean tabletPortrait,
        boolean emergencySurfaceEligible
    ) {
        return answerMode && tabletPortrait && emergencySurfaceEligible;
    }

    static OverlayMargins resolveOverlayMarginsDp(boolean tabletPortrait) {
        if (tabletPortrait) {
            return new OverlayMargins(
                PORTRAIT_LEFT_MARGIN_DP,
                PORTRAIT_RIGHT_MARGIN_DP,
                PORTRAIT_TOP_MARGIN_DP
            );
        }
        return new OverlayMargins(
            LANDSCAPE_LEFT_MARGIN_DP,
            LANDSCAPE_RIGHT_MARGIN_DP,
            LANDSCAPE_TOP_MARGIN_DP
        );
    }

    static int resolveOverlayHeight(boolean tabletPortrait) {
        return tabletPortrait
            ? ViewGroup.LayoutParams.MATCH_PARENT
            : ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    static boolean shouldShowAppRailOverlay(
        boolean answerMode,
        boolean tabletPortrait,
        boolean emergencySurfaceEligible
    ) {
        return shouldUseFullHeightPage(answerMode, tabletPortrait, emergencySurfaceEligible);
    }

    static AppRailDestination activeDestination() {
        return AppRailDestination.ASK;
    }

    static boolean isDestinationActive(AppRailDestination destination) {
        return destination == activeDestination();
    }

    static int appRailWidthDp() {
        return APP_RAIL_WIDTH_DP;
    }

    static int appRailDividerWidthDp() {
        return APP_RAIL_DIVIDER_WIDTH_DP;
    }

    static float appRailLabelTextSizeSp() {
        return APP_RAIL_LABEL_TEXT_SIZE_SP;
    }

    static float appRailLabelLineHeightSp() {
        return APP_RAIL_LABEL_LINE_HEIGHT_SP;
    }

    static float appRailLabelLetterSpacing() {
        return APP_RAIL_LABEL_LETTER_SPACING;
    }

    static boolean shouldShowHomeChromeAction() {
        return false;
    }

    static int appRailHomeLabelResource() {
        return R.string.detail_emergency_app_rail_manual_label;
    }

    static int appRailHomeContentDescriptionResource() {
        return R.string.detail_emergency_app_rail_manual_content_description;
    }

    enum AppRailDestination {
        HOME,
        ASK,
        SAVED
    }

    static final class OverlayMargins {
        final int left;
        final int right;
        final int top;

        OverlayMargins(int left, int right, int top) {
            this.left = left;
            this.right = right;
            this.top = top;
        }
    }

    static final class Decision {
        final boolean fullHeightPage;
        final boolean suppressStaleChrome;
        final boolean hideDetailRoot;
        final boolean suppressFloatingRail;
        final boolean showAppRailOverlay;
        final boolean showOverlayChrome;
        final OverlayMargins overlayMarginsDp;
        final int overlayHeight;
        final AppRailDestination activeDestination;
        final int appRailWidthDp;
        final int appRailDividerWidthDp;
        final float appRailLabelTextSizeSp;
        final float appRailLabelLineHeightSp;
        final float appRailLabelLetterSpacing;
        final int appRailHomeLabelResource;
        final int appRailHomeContentDescriptionResource;
        final boolean showHomeChromeAction;

        Decision(
            boolean fullHeightPage,
            boolean suppressStaleChrome,
            boolean hideDetailRoot,
            boolean suppressFloatingRail,
            boolean showAppRailOverlay,
            boolean showOverlayChrome,
            OverlayMargins overlayMarginsDp,
            int overlayHeight,
            AppRailDestination activeDestination,
            int appRailWidthDp,
            int appRailDividerWidthDp,
            float appRailLabelTextSizeSp,
            float appRailLabelLineHeightSp,
            float appRailLabelLetterSpacing,
            int appRailHomeLabelResource,
            int appRailHomeContentDescriptionResource,
            boolean showHomeChromeAction
        ) {
            this.fullHeightPage = fullHeightPage;
            this.suppressStaleChrome = suppressStaleChrome;
            this.hideDetailRoot = hideDetailRoot;
            this.suppressFloatingRail = suppressFloatingRail;
            this.showAppRailOverlay = showAppRailOverlay;
            this.showOverlayChrome = showOverlayChrome;
            this.overlayMarginsDp = overlayMarginsDp;
            this.overlayHeight = overlayHeight;
            this.activeDestination = activeDestination;
            this.appRailWidthDp = appRailWidthDp;
            this.appRailDividerWidthDp = appRailDividerWidthDp;
            this.appRailLabelTextSizeSp = appRailLabelTextSizeSp;
            this.appRailLabelLineHeightSp = appRailLabelLineHeightSp;
            this.appRailLabelLetterSpacing = appRailLabelLetterSpacing;
            this.appRailHomeLabelResource = appRailHomeLabelResource;
            this.appRailHomeContentDescriptionResource = appRailHomeContentDescriptionResource;
            this.showHomeChromeAction = showHomeChromeAction;
        }
    }
}

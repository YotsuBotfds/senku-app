package com.senku.mobile;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Locale;

final class DetailTabletEmergencyChromePolicy {
    private static final String CHROME_META_SEPARATOR = " \u2022 ";
    private static final int APP_RAIL_WIDTH_DP = 72;
    private static final int APP_RAIL_DIVIDER_WIDTH_DP = 1;
    private static final int PORTRAIT_LEFT_MARGIN_DP = APP_RAIL_WIDTH_DP + APP_RAIL_DIVIDER_WIDTH_DP;
    private static final int PORTRAIT_RIGHT_MARGIN_DP = 24;
    private static final int PORTRAIT_TOP_MARGIN_DP = 0;
    private static final int LANDSCAPE_LEFT_MARGIN_DP = 336;
    private static final int LANDSCAPE_RIGHT_MARGIN_DP = 24;
    private static final int LANDSCAPE_TOP_MARGIN_DP = 16;
    private static final int PORTRAIT_HORIZONTAL_PADDING_DP = 16;
    private static final int PORTRAIT_VERTICAL_PADDING_DP = 12;
    private static final int CHROME_BOTTOM_MARGIN_DP = 12;
    private static final int CHROME_NAV_ICON_SIZE_DP = 28;
    private static final int CHROME_BACK_ACTION_MIN_WIDTH_DP = 28;
    private static final int CHROME_BACK_ICON_SIZE_DP = 18;
    private static final int CHROME_BACK_HORIZONTAL_PADDING_DP = 6;
    private static final int CHROME_DIVIDER_GAP_DP = 10;
    private static final int CHROME_DIVIDER_HEIGHT_DP = 24;
    private static final int CHROME_RULE_HEIGHT_DP = 1;
    private static final int DANGER_BAND_HORIZONTAL_BLEED_DP = PORTRAIT_HORIZONTAL_PADDING_DP;
    private static final float CHROME_TITLE_TEXT_SIZE_SP = 13.0f;
    private static final float CHROME_TITLE_LINE_HEIGHT_SP = 18.0f;
    private static final float CHROME_META_TEXT_SIZE_SP = 9.5f;
    private static final float CHROME_META_LINE_HEIGHT_SP = 11.0f;
    private static final float CHROME_BACK_LABEL_TEXT_SIZE_SP = 10.0f;
    private static final float CHROME_BACK_LABEL_LINE_HEIGHT_SP = 12.0f;
    private static final float CHROME_LABEL_LETTER_SPACING = 0.09f;
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

    static int portraitHorizontalPaddingDp() {
        return PORTRAIT_HORIZONTAL_PADDING_DP;
    }

    static int portraitVerticalPaddingDp() {
        return PORTRAIT_VERTICAL_PADDING_DP;
    }

    static int chromeBottomMarginDp() {
        return CHROME_BOTTOM_MARGIN_DP;
    }

    static int chromeNavIconSizeDp() {
        return CHROME_NAV_ICON_SIZE_DP;
    }

    static int chromeBackActionMinWidthDp() {
        return CHROME_BACK_ACTION_MIN_WIDTH_DP;
    }

    static int chromeBackIconSizeDp() {
        return CHROME_BACK_ICON_SIZE_DP;
    }

    static int chromeBackHorizontalPaddingDp() {
        return CHROME_BACK_HORIZONTAL_PADDING_DP;
    }

    static int chromeDividerGapDp() {
        return CHROME_DIVIDER_GAP_DP;
    }

    static int chromeDividerHeightDp() {
        return CHROME_DIVIDER_HEIGHT_DP;
    }

    static int chromeRuleHeightDp() {
        return CHROME_RULE_HEIGHT_DP;
    }

    static int chromeRuleHorizontalInsetDp(boolean tabletPortrait) {
        return tabletPortrait ? -PORTRAIT_HORIZONTAL_PADDING_DP : -18;
    }

    static int dangerBandHorizontalBleedDp(boolean tabletPortrait) {
        return tabletPortrait ? DANGER_BAND_HORIZONTAL_BLEED_DP : 0;
    }

    static float chromeTitleTextSizeSp() {
        return CHROME_TITLE_TEXT_SIZE_SP;
    }

    static float chromeTitleLineHeightSp() {
        return CHROME_TITLE_LINE_HEIGHT_SP;
    }

    static float chromeMetaTextSizeSp() {
        return CHROME_META_TEXT_SIZE_SP;
    }

    static float chromeMetaLineHeightSp() {
        return CHROME_META_LINE_HEIGHT_SP;
    }

    static float chromeBackLabelTextSizeSp() {
        return CHROME_BACK_LABEL_TEXT_SIZE_SP;
    }

    static float chromeBackLabelLineHeightSp() {
        return CHROME_BACK_LABEL_LINE_HEIGHT_SP;
    }

    static float chromeLabelLetterSpacing() {
        return CHROME_LABEL_LETTER_SPACING;
    }

    static String buildChromeMeta(Iterable<String> labels) {
        ArrayList<String> cleanedLabels = new ArrayList<>();
        if (labels != null) {
            for (String label : labels) {
                String cleaned = safe(label).trim();
                if (!cleaned.isEmpty()) {
                    cleanedLabels.add(cleaned);
                }
            }
        }
        return String.join(CHROME_META_SEPARATOR, cleanedLabels).toUpperCase(Locale.US);
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

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

package com.senku.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class ReviewDemoPolicy {
    static final String EXTRA_PRODUCT_REVIEW_MODE = "product_review_mode";
    static final String EXTRA_PRODUCT_REVIEW_AUTOMATION_AUTH =
        "com.senku.mobile.extra.PRODUCT_REVIEW_AUTOMATION_AUTH";
    private static final String PRODUCT_REVIEW_AUTOMATION_AUTH_VALUE = "senku-review-demo-v1";

    private ReviewDemoPolicy() {
    }

    interface GuideLookup {
        SearchResult loadGuideById(String guideId);
    }

    static boolean isSourceStackDemoEnabled(boolean productReviewMode) {
        return shouldApplyReviewDemoFixtures(productReviewMode);
    }

    static boolean resolveProductReviewMode(Intent intent, Context context) {
        return resolveProductReviewMode(intent, isDebuggableBuild(context));
    }

    static boolean resolveProductReviewMode(Intent intent, boolean debugBuild) {
        if (intent == null) {
            return false;
        }
        return resolveProductReviewModeForTest(
            intent.hasExtra(EXTRA_PRODUCT_REVIEW_MODE),
            intent.getBooleanExtra(EXTRA_PRODUCT_REVIEW_MODE, false),
            isProductReviewAutomationAuthorized(intent),
            debugBuild
        );
    }

    static boolean resolveProductReviewModeForTest(
        boolean hasReviewModeExtra,
        boolean reviewModeEnabled,
        boolean automationAuthorized,
        boolean debugBuild
    ) {
        return hasReviewModeExtra && reviewModeEnabled && automationAuthorized && debugBuild;
    }

    static void putProductReviewModeExtras(Intent intent, boolean productReviewMode) {
        if (intent == null) {
            return;
        }
        intent.putExtra(EXTRA_PRODUCT_REVIEW_MODE, productReviewMode);
        if (productReviewMode) {
            intent.putExtra(
                EXTRA_PRODUCT_REVIEW_AUTOMATION_AUTH,
                PRODUCT_REVIEW_AUTOMATION_AUTH_VALUE
            );
        } else {
            intent.removeExtra(EXTRA_PRODUCT_REVIEW_AUTOMATION_AUTH);
        }
    }

    private static boolean isDebuggableBuild(Context context) {
        ApplicationInfo info = context == null ? null : context.getApplicationInfo();
        return info != null && (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private static boolean isProductReviewAutomationAuthorized(Intent intent) {
        return PRODUCT_REVIEW_AUTOMATION_AUTH_VALUE.equals(
            intent == null ? null : intent.getStringExtra(EXTRA_PRODUCT_REVIEW_AUTOMATION_AUTH)
        );
    }

    static boolean isRainShelterUncertainFitBackendDemoEnabled() {
        return false;
    }

    static boolean isAnswerProductReviewModeEnabled() {
        return false;
    }

    static String buildRainShelterUncertainFitAnswerBody(
        boolean productReviewMode,
        String query,
        List<SearchResult> adjacent,
        boolean safetyCritical
    ) {
        if (!shouldApplyReviewDemoFixtures(productReviewMode)
            || safetyCritical
            || !ReviewDemoFixtureSet.isRainShelterUncertainFit(query, adjacent)) {
            return "";
        }
        return ReviewDemoFixtureSet.rainShelterUncertainFitAnswerBody();
    }

    static List<SearchResult> shapeRainShelterUncertainFitSources(
        boolean productReviewMode,
        String query,
        List<SearchResult> adjacent,
        boolean safetyCritical
    ) {
        if (!shouldApplyReviewDemoFixtures(productReviewMode)
            || safetyCritical
            || !ReviewDemoFixtureSet.isRainShelterUncertainFit(query, adjacent)) {
            return adjacent == null ? Collections.emptyList() : new ArrayList<>(adjacent);
        }
        return ReviewDemoFixtureSet.rainShelterUncertainFitSources(
            ReviewDemoFixtureSet.bestRainShelterSource(adjacent)
        );
    }

    static List<SearchResult> shapeSearchResults(
        String query,
        boolean productReviewMode,
        List<SearchResult> results,
        GuideLookup guideLookup
    ) {
        if (!shouldShapeReviewSearchResults(productReviewMode, query)) {
            return results == null ? Collections.emptyList() : results;
        }
        return ReviewDemoFixtureSet.shapeReviewSearchResults(results, guideLookup);
    }

    static ArrayList<SearchResult> shapeAnswerModeRelatedGuides(
        boolean productReviewMode,
        String anchorGuideId,
        List<SearchResult> relatedGuides
    ) {
        ArrayList<SearchResult> shaped = new ArrayList<>();
        if (relatedGuides != null) {
            shaped.addAll(relatedGuides);
        }
        if (!shouldShapeAnswerModeRelatedGuides(productReviewMode, anchorGuideId)) {
            return shaped;
        }
        return ReviewDemoFixtureSet.shapeAnswerModeRelatedGuides(anchorGuideId, shaped);
    }

    static int displayHomeCategoryCount(boolean productReviewMode, String bucketKey, int actualCount) {
        if (!shouldApplyReviewDemoFixtures(productReviewMode)) {
            return actualCount;
        }
        return ReviewDemoFixtureSet.homeCategoryCount(bucketKey);
    }

    static boolean shouldUseHomeCategoryFixtureCounts(
        boolean productReviewMode,
        boolean manualHomeShell,
        boolean hasGuides
    ) {
        return shouldApplyReviewDemoFixtures(productReviewMode)
            && manualHomeShell
            && hasGuides;
    }

    static String shapeHomeSubtitle(boolean productReviewMode, int guideCount, String defaultSubtitle) {
        if (!shouldApplyReviewDemoFixtures(productReviewMode) || guideCount <= 0) {
            return safe(defaultSubtitle);
        }
        return ReviewDemoFixtureSet.homeSubtitle();
    }

    static String shapeManualHomeStatus(boolean productReviewMode, boolean manualHomeShell, String defaultStatus) {
        String cleanStatus = safe(defaultStatus).trim();
        if (!shouldApplyReviewDemoFixtures(productReviewMode) || !manualHomeShell || cleanStatus.isEmpty()) {
            return cleanStatus;
        }
        if (cleanStatus.toLowerCase(Locale.US).startsWith("ready offline")) {
            return ReviewDemoFixtureSet.manualHomeReadyStatus();
        }
        return cleanStatus;
    }

    static String appendSearchLatency(String header, String query, boolean productReviewMode) {
        String cleanHeader = safe(header).trim();
        if (!shouldShapeReviewSearchResults(productReviewMode, query)
            || cleanHeader.isEmpty()
            || ReviewDemoFixtureSet.headerAlreadyIncludesReviewLatency(cleanHeader)) {
            return cleanHeader;
        }
        return cleanHeader + " \u2022 " + ReviewDemoFixtureSet.latencyLabel();
    }

    static boolean shouldSuppressSearchRowLinkedGuideCue(
        boolean productReviewMode,
        String query,
        SearchResult result
    ) {
        return shouldApplySearchRowReviewVisualState(productReviewMode, query)
            && ReviewDemoFixtureSet.isReviewSearchResult(result);
    }

    static boolean shouldApplySearchRowReviewVisualState(boolean productReviewMode, String query) {
        return shouldShapeReviewSearchResults(productReviewMode, query);
    }

    static boolean shouldShapeReviewSearchResults(boolean productReviewMode, String query) {
        return shouldApplyReviewDemoFixtures(productReviewMode)
            && ReviewDemoFixtureSet.isReviewSearchQuery(query);
    }

    static String shapeRecentThreadLabel(
        boolean productReviewMode,
        ChatSessionStore.ConversationPreview preview,
        int index,
        String defaultLabel
    ) {
        if (!shouldApplyReviewDemoFixtures(productReviewMode)) {
            return safe(defaultLabel);
        }
        String reviewLabel = ReviewDemoFixtureSet.manualHomeRecentThreadLabel(index);
        if (!reviewLabel.isEmpty()) {
            return reviewLabel;
        }
        String meta = ManualHomeRecentThreadFormatter.buildMeta(preview);
        String title = reviewManualHomeRecentThreadTitle(preview, index);
        if (title.isEmpty()) {
            return safe(defaultLabel);
        }
        return meta.isEmpty() ? title : title + "\n" + meta;
    }

    static String shapeTabletPreviewMeta(boolean productReviewMode, SearchResult result, String defaultMeta) {
        if (!isTabletPreviewReviewResult(productReviewMode, result)) {
            return safe(defaultMeta);
        }
        return ReviewDemoFixtureSet.tabletPreviewMeta();
    }

    static String shapeTabletPreviewBody(boolean productReviewMode, SearchResult result, String defaultBody) {
        if (!isTabletPreviewReviewResult(productReviewMode, result)) {
            return safe(defaultBody);
        }
        return ReviewDemoFixtureSet.tabletPreviewBody();
    }

    static String placeholderRecentThreadQuestion(boolean productReviewMode, int index, String defaultQuestion) {
        if (!shouldApplyReviewDemoFixtures(productReviewMode)) {
            return safe(defaultQuestion);
        }
        return reviewManualHomeRecentThreadTitle(null, index);
    }

    static boolean shouldApplyReviewDemoFixtures(boolean productReviewMode) {
        return productReviewMode;
    }

    static boolean shouldShapeAnswerModeRelatedGuides(boolean productReviewMode, String anchorGuideId) {
        return shouldApplyReviewDemoFixtures(productReviewMode)
            && ReviewDemoFixtureSet.isRainShelterTopicGuide(anchorGuideId);
    }

    static boolean shouldPresentAbrasivesCrossReferenceAsAnchor(
        boolean reviewDemoFixturesEnabled,
        String guideId,
        String title,
        String categoryToken
    ) {
        if (!reviewDemoFixturesEnabled) {
            return false;
        }
        String normalizedTitle = safe(title).trim().toLowerCase(Locale.US);
        String normalizedCategory = safe(categoryToken)
            .trim()
            .replace('-', ' ')
            .replace('_', ' ')
            .replaceAll("\\s+", " ")
            .toLowerCase(Locale.US);
        return "GD-220".equalsIgnoreCase(safe(guideId).trim())
            && normalizedTitle.contains("abrasives")
            && (normalizedCategory.contains("related")
                || normalizedCategory.contains("cross reference")
                || normalizedCategory.contains("crossref"));
    }

    private static String reviewManualHomeRecentThreadTitle(
        ChatSessionStore.ConversationPreview preview,
        int index
    ) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        String guideId = ManualHomeRecentThreadFormatter.resolveGuideId(turn);
        String confidence = ManualHomeRecentThreadFormatter.buildConfidenceLabel(turn);
        return ReviewDemoFixtureSet.manualHomeRecentThreadTitle(preview, index, guideId, confidence);
    }

    private static boolean isTabletPreviewReviewResult(boolean productReviewMode, SearchResult result) {
        return shouldApplyReviewDemoFixtures(productReviewMode)
            && ReviewDemoFixtureSet.isTabletPreviewReviewResult(result);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

}

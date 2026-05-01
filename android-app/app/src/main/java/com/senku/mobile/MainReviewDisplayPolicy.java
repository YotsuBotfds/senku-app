package com.senku.mobile;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class MainReviewDisplayPolicy {
    private final boolean productReviewMode;

    MainReviewDisplayPolicy(boolean productReviewMode) {
        this.productReviewMode = productReviewMode;
    }

    int displayHomeCategoryCount(String bucketKey, int actualCount, List<SearchResult> guides, boolean manualHomeShell) {
        if (!shouldUseHomeCategoryFixtureCounts(productReviewMode, manualHomeShell, guides)) {
            return actualCount;
        }
        return ReviewDemoPolicy.displayHomeCategoryCount(productReviewMode, bucketKey, actualCount);
    }

    String homeSubtitle(int guideCount) {
        return homeSubtitle(guideCount, productReviewMode);
    }

    String manualHomeStatus(String status, boolean manualHomeShell) {
        return manualHomeStatus(status, manualHomeShell, productReviewMode);
    }

    String searchLatency(String header, String query) {
        return searchLatency(header, query, productReviewMode);
    }

    String phoneSearchHeader(String query, int resultCount) {
        return phoneSearchHeader(query, resultCount, productReviewMode);
    }

    String tabletSearchHeader(String query, int resultCount) {
        return tabletSearchHeader(query, resultCount, productReviewMode);
    }

    String searchChromeCountLabel(String query, int resultCount) {
        return searchChromeCountLabel(query, resultCount, productReviewMode);
    }

    String tabletPreviewMeta(SearchResult result) {
        return tabletPreviewMeta(result, productReviewMode);
    }

    String tabletPreviewBody(SearchResult result) {
        return tabletPreviewBody(result, productReviewMode);
    }

    static boolean shouldUseHomeCategoryFixtureCounts(
        boolean productReviewMode,
        boolean manualHomeShell,
        List<SearchResult> guides
    ) {
        return ReviewDemoPolicy.shouldUseHomeCategoryFixtureCounts(
            productReviewMode,
            manualHomeShell,
            guides != null && !guides.isEmpty()
        );
    }

    static String homeSubtitle(int guideCount, boolean productReviewMode) {
        if (guideCount <= 0) {
            return "";
        }
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        String guideLabel = guideCount == 1
            ? format.format(guideCount) + " guide"
            : format.format(guideCount) + " guides";
        String defaultSubtitle = guideLabel + " in your offline field manual";
        return ReviewDemoPolicy.shapeHomeSubtitle(productReviewMode, guideCount, defaultSubtitle);
    }

    static String manualHomeStatus(String status, boolean manualHomeShell, boolean productReviewMode) {
        String cleanStatus = safe(status).trim();
        return ReviewDemoPolicy.shapeManualHomeStatus(productReviewMode, manualHomeShell, cleanStatus);
    }

    static String searchLatency(String header, String query, boolean productReviewMode) {
        return ReviewDemoPolicy.appendSearchLatency(header, query, productReviewMode);
    }

    static String phoneSearchHeader(String query, int resultCount, boolean productReviewMode) {
        String cleanQuery = safe(query).trim();
        String countLabel = resultCount + (resultCount == 1 ? " result" : " results");
        if (cleanQuery.isEmpty()) {
            return "Search - " + countLabel;
        }
        return searchLatency("Search " + cleanQuery + "    " + countLabel, cleanQuery, productReviewMode);
    }

    static String tabletSearchHeader(String query, int resultCount, boolean productReviewMode) {
        return ReviewDemoPolicy.buildTabletSearchHeader(productReviewMode, query, resultCount);
    }

    static String searchChromeCountLabel(String query, int resultCount, boolean productReviewMode) {
        return searchLatency(
            MainHomeChromePolicy.resolveSearch(query, resultCount).countLabel,
            query,
            productReviewMode
        );
    }

    static String tabletPreviewMeta(SearchResult result, boolean productReviewMode) {
        ArrayList<String> parts = new ArrayList<>();
        addNonEmptyPart(parts, result == null ? null : result.contentRole);
        addNonEmptyPart(parts, result == null ? null : result.timeHorizon);
        addNonEmptyPart(parts, result == null ? null : result.category);
        if (parts.isEmpty()) {
            addNonEmptyPart(parts, result == null ? null : result.subtitle);
        }
        String defaultMeta = parts.isEmpty() ? "Source guide" : String.join("  \u00B7  ", parts);
        return ReviewDemoPolicy.shapeTabletPreviewMeta(productReviewMode, result, defaultMeta);
    }

    static String tabletPreviewBody(SearchResult result, boolean productReviewMode) {
        String defaultBody = firstNonEmpty(
            result == null ? null : result.snippet,
            result == null ? null : result.body,
            "Tap a result to open the full guide."
        );
        return ReviewDemoPolicy.shapeTabletPreviewBody(productReviewMode, result, defaultBody);
    }

    private static void addNonEmptyPart(List<String> parts, String value) {
        String clean = safe(value).trim();
        if (!clean.isEmpty()) {
            parts.add(clean.replace('-', ' '));
        }
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String clean = safe(value).trim();
            if (!clean.isEmpty()) {
                return clean;
            }
        }
        return "";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

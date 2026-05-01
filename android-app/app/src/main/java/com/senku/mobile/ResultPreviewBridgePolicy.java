package com.senku.mobile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

final class ResultPreviewBridgePolicy {
    private static final int MAX_PREVIEW_GUIDES = 4;

    private ResultPreviewBridgePolicy() {
    }

    static LinkedHashMap<String, String> collectGuideIds(List<SearchResult> results) {
        LinkedHashMap<String, String> previewGuideIds = new LinkedHashMap<>();
        if (results == null) {
            return previewGuideIds;
        }
        for (SearchResult result : results) {
            String guideId = safe(result == null ? null : result.guideId).trim();
            if (guideId.isEmpty()) {
                continue;
            }
            String normalizedGuideId = guideId.toLowerCase(Locale.US);
            previewGuideIds.putIfAbsent(normalizedGuideId, guideId);
            if (previewGuideIds.size() >= MAX_PREVIEW_GUIDES) {
                break;
            }
        }
        return previewGuideIds;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

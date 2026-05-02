package com.senku.mobile;

import java.util.List;

final class MainHomeRelatedGuideController {
    MainActivity.HomeGuideAnchor selectHomeGuideAnchor(
        List<SearchResult> pinnedGuides,
        List<ChatSessionStore.ConversationPreview> recentThreadPreviews,
        MainPresentationFormatter formatter
    ) {
        MainActivity.HomeGuideAnchor recentAnchor =
            selectLatestRecentThreadHomeGuideAnchor(recentThreadPreviews, formatter);
        if (recentAnchor != null) {
            return recentAnchor;
        }
        if (pinnedGuides == null || pinnedGuides.isEmpty()) {
            return null;
        }
        for (SearchResult pinnedGuide : pinnedGuides) {
            String guideId = safe(pinnedGuide == null ? null : pinnedGuide.guideId).trim();
            if (!guideId.isEmpty()) {
                return new MainActivity.HomeGuideAnchor(
                    guideId,
                    formatter.buildGuideReference(pinnedGuide, guideId),
                    false
                );
            }
        }
        return null;
    }

    MainActivity.HomeGuideAnchor selectLatestRecentThreadHomeGuideAnchor(
        List<ChatSessionStore.ConversationPreview> recentThreadPreviews,
        MainPresentationFormatter formatter
    ) {
        if (recentThreadPreviews == null || recentThreadPreviews.isEmpty()) {
            return null;
        }
        ChatSessionStore.ConversationPreview preview = recentThreadPreviews.get(0);
        String guideId = formatter.resolvePreviewPrimaryGuideId(preview);
        if (guideId.isEmpty()) {
            return null;
        }
        return new MainActivity.HomeGuideAnchor(
            guideId,
            formatter.buildGuideReference(
                guideId,
                formatter.resolvePreviewPrimaryGuideTitle(preview, guideId)
            ),
            true
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

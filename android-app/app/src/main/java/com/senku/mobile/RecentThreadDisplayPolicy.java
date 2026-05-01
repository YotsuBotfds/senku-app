package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RecentThreadDisplayPolicy {
    private static final String REVIEW_HOME_PLACEHOLDER_ID_PREFIX = "review-home-placeholder-";

    private RecentThreadDisplayPolicy() {
    }

    static List<ChatSessionStore.ConversationPreview> buildDisplayList(
        List<ChatSessionStore.ConversationPreview> livePreviews,
        boolean productReviewMode,
        boolean manualHomeShell,
        int maxCount
    ) {
        if (maxCount <= 0) {
            return Collections.emptyList();
        }
        ArrayList<ChatSessionStore.ConversationPreview> displayPreviews = new ArrayList<>(maxCount);
        if (livePreviews != null) {
            for (ChatSessionStore.ConversationPreview preview : livePreviews) {
                if (displayPreviews.size() >= maxCount) {
                    break;
                }
                displayPreviews.add(preview);
            }
        }
        if (productReviewMode && manualHomeShell) {
            while (displayPreviews.size() < maxCount) {
                displayPreviews.add(buildReviewRecentThreadPlaceholder(productReviewMode, displayPreviews.size()));
            }
        }
        return displayPreviews;
    }

    private static ChatSessionStore.ConversationPreview buildReviewRecentThreadPlaceholder(
        boolean productReviewMode,
        int index
    ) {
        long now = System.currentTimeMillis();
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            ReviewDemoPolicy.placeholderRecentThreadQuestion(productReviewMode, index, ""),
            "",
            "",
            Collections.emptyList(),
            Collections.emptyList(),
            "",
            ReviewedCardMetadata.empty(),
            null,
            now
        );
        return new ChatSessionStore.ConversationPreview(
            REVIEW_HOME_PLACEHOLDER_ID_PREFIX + index,
            turn,
            1,
            now
        );
    }
}

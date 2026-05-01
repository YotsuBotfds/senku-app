package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class RecentThreadDisplayPolicyTest {
    private static final int MAX_COUNT = 3;
    private static final long NOW = 1_800_000_000_000L;

    @Test
    public void preservesLivePreviewsWithoutReviewPadding() {
        ChatSessionStore.ConversationPreview first = livePreview("conversation-1", "how to find water", NOW);
        ChatSessionStore.ConversationPreview second = livePreview("conversation-2", "how to signal", NOW - 1_000L);

        List<ChatSessionStore.ConversationPreview> displayPreviews =
            RecentThreadDisplayPolicy.buildDisplayList(Arrays.asList(first, second), false, true, MAX_COUNT);

        assertEquals(2, displayPreviews.size());
        assertSame(first, displayPreviews.get(0));
        assertSame(second, displayPreviews.get(1));
    }

    @Test
    public void padsReviewPlaceholdersOnlyWhenReviewModeAndManualHomeShell() {
        ChatSessionStore.ConversationPreview live = livePreview("conversation-live", "how to keep tinder dry", NOW);

        List<ChatSessionStore.ConversationPreview> displayPreviews =
            RecentThreadDisplayPolicy.buildDisplayList(Collections.singletonList(live), true, true, MAX_COUNT);

        assertEquals(MAX_COUNT, displayPreviews.size());
        assertSame(live, displayPreviews.get(0));
        assertPlaceholder(displayPreviews.get(1), 1);
        assertPlaceholder(displayPreviews.get(2), 2);
    }

    @Test
    public void doesNotPadWhenReviewModeIsDisabledOrManualShellIsDisabled() {
        ChatSessionStore.ConversationPreview live = livePreview("conversation-live", "how to make cordage", NOW);

        assertEquals(
            1,
            RecentThreadDisplayPolicy.buildDisplayList(Collections.singletonList(live), false, true, MAX_COUNT).size()
        );
        assertEquals(
            1,
            RecentThreadDisplayPolicy.buildDisplayList(Collections.singletonList(live), true, false, MAX_COUNT).size()
        );
    }

    @Test
    public void normalProductRecentThreadsAreNotSynthetic() {
        ChatSessionStore.ConversationPreview live = livePreview(
            "product-conversation-live",
            "which water filter should I pack",
            NOW
        );

        List<ChatSessionStore.ConversationPreview> displayPreviews =
            RecentThreadDisplayPolicy.buildDisplayList(Collections.singletonList(live), false, true, MAX_COUNT);

        assertEquals(1, displayPreviews.size());
        assertSame(live, displayPreviews.get(0));
        assertFalse(displayPreviews.get(0).conversationId.startsWith("review-home-placeholder-"));
    }

    @Test
    public void reviewPlaceholderPaddingIsGatedByReviewDemoPolicy() {
        assertTrue(ReviewDemoPolicy.shouldUseReviewRecentThreadPlaceholders(true, true));
        assertFalse(ReviewDemoPolicy.shouldUseReviewRecentThreadPlaceholders(false, true));
        assertFalse(ReviewDemoPolicy.shouldUseReviewRecentThreadPlaceholders(true, false));

        assertEquals(
            0,
            RecentThreadDisplayPolicy.buildDisplayList(Collections.emptyList(), false, true, MAX_COUNT).size()
        );
        assertEquals(
            MAX_COUNT,
            RecentThreadDisplayPolicy.buildDisplayList(Collections.emptyList(), true, true, MAX_COUNT).size()
        );
    }

    @Test
    public void clipsLivePreviewsToMaxCountBeforePadding() {
        ChatSessionStore.ConversationPreview first = livePreview("conversation-1", "one", NOW);
        ChatSessionStore.ConversationPreview second = livePreview("conversation-2", "two", NOW - 1_000L);
        ChatSessionStore.ConversationPreview third = livePreview("conversation-3", "three", NOW - 2_000L);

        List<ChatSessionStore.ConversationPreview> displayPreviews =
            RecentThreadDisplayPolicy.buildDisplayList(Arrays.asList(first, second, third), true, true, 2);

        assertEquals(2, displayPreviews.size());
        assertSame(first, displayPreviews.get(0));
        assertSame(second, displayPreviews.get(1));
    }

    private static void assertPlaceholder(ChatSessionStore.ConversationPreview preview, int index) {
        assertEquals("review-home-placeholder-" + index, preview.conversationId);
        assertEquals(
            ReviewDemoPolicy.placeholderRecentThreadQuestion(true, index, ""),
            preview.latestTurn.question
        );
        assertEquals(1, preview.turnCount);
    }

    private static ChatSessionStore.ConversationPreview livePreview(
        String conversationId,
        String question,
        long lastActivityEpoch
    ) {
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            question,
            "",
            "answer",
            Collections.emptyList(),
            Collections.emptyList(),
            "",
            ReviewedCardMetadata.empty(),
            null,
            lastActivityEpoch
        );
        return new ChatSessionStore.ConversationPreview(conversationId, turn, 1, lastActivityEpoch);
    }
}

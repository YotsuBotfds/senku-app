package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class ChatSessionStoreTest {
    @Test
    public void sameConversationIdReusesSameMemoryInstance() {
        ChatSessionStore.resetForTest();
        String conversationId = ChatSessionStore.createConversation();

        SessionMemory first = ChatSessionStore.memoryFor(conversationId);
        SessionMemory second = ChatSessionStore.memoryFor(conversationId);

        assertSame(first, second);
    }

    @Test
    public void separateConversationIdsStayIsolated() {
        ChatSessionStore.resetForTest();
        String firstConversationId = ChatSessionStore.createConversation();
        String secondConversationId = ChatSessionStore.createConversation();

        assertNotEquals(firstConversationId, secondConversationId);

        ChatSessionStore.memoryFor(firstConversationId).recordTurn(
            "how do i build a house",
            "Short answer: Start with drainage and foundation planning.",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus"
                )
            )
        );

        assertFalse(ChatSessionStore.memoryFor(secondConversationId).hasState());
    }

    @Test
    public void capsConversationCountAndEvictsOldestConversation() {
        ChatSessionStore.resetForTest();
        String firstConversationId = ChatSessionStore.createConversation();

        for (int index = 0; index < 12; index++) {
            ChatSessionStore.createConversation();
        }

        assertEquals(10, ChatSessionStore.conversationCountForTest());
        assertFalse(ChatSessionStore.containsConversationIdForTest(firstConversationId));
    }

    @Test
    public void staleConversationReuseClearsAnchorPriorBias() {
        ChatSessionStore.resetForTest();
        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            String conversationId = ChatSessionStore.createConversation();
            SessionMemory memory = ChatSessionStore.memoryFor(conversationId);
            memory.recordTurn(
                "how do i build a water filter",
                "Short answer: Start with the charcoal layer.",
                List.of(
                    new SearchResult(
                        "Water Filtration",
                        "",
                        "",
                        "",
                        "GD-252",
                        "Charcoal Layer",
                        "resource-management",
                        "guide-focus"
                    )
                )
            );
            memory.recordTurn(
                "what about charcoal",
                "Short answer: Rinse it first.",
                List.of(
                    new SearchResult(
                        "Water Filtration",
                        "",
                        "",
                        "",
                        "GD-252",
                        "Charcoal Prep",
                        "resource-management",
                        "guide-focus"
                    )
                )
            );

            long idleResetTime = memory.lastActivityEpoch() + SessionMemory.anchorIdleResetMsForTest() + 1L;
            ChatSessionStore.ensureConversationIdForTest(conversationId, idleResetTime);
            SessionMemory.RetrievalPlan plan = memory.buildRetrievalPlan("what about sand");

            assertEquals(null, plan.anchorPrior);
        } finally {
            SessionMemory.setAnchorPriorEnabledForTest(false);
        }
    }

    @Test
    public void recentConversationPreviewsReturnMostRecentStatefulThreadsFirst() {
        ChatSessionStore.resetForTest();
        String firstConversationId = ChatSessionStore.createConversation();
        String secondConversationId = ChatSessionStore.createConversation();
        String thirdConversationId = ChatSessionStore.createConversation();
        recordTurn(firstConversationId, "first question", "GD-001");
        recordTurn(secondConversationId, "second question", "GD-002");
        recordTurn(thirdConversationId, "third question", "GD-003");

        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 2);

        assertEquals(2, previews.size());
        assertEquals(thirdConversationId, previews.get(0).conversationId);
        assertEquals(secondConversationId, previews.get(1).conversationId);
        assertEquals("third question", previews.get(0).latestTurn.question);
    }

    @Test
    public void recentConversationPreviewsSuppressNearDuplicateQuestions() {
        ChatSessionStore.resetForTest();
        String olderConversationId = ChatSessionStore.createConversation();
        String newerConversationId = ChatSessionStore.createConversation();
        recordTurn(olderConversationId, "How do I store water?", "GD-619");
        recordTurn(newerConversationId, "  how   do i store water?  ", "GD-035");

        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 10);

        assertEquals(1, previews.size());
        assertEquals(newerConversationId, previews.get(0).conversationId);
        assertEquals("GD-035", previews.get(0).latestTurn.sourceResults.get(0).guideId);
    }

    @Test
    public void corruptSavedStateDoesNotClearLiveStateOrBlockLaterRestore() throws Exception {
        ChatSessionStore.resetForTest();
        String liveConversationId = ChatSessionStore.createConversation();
        recordTurn(liveConversationId, "live question", "GD-101");

        ChatSessionStore.restoreSavedStateForTest("{");

        assertEquals(1, ChatSessionStore.conversationCountForTest());
        assertTrue(ChatSessionStore.containsConversationIdForTest(liveConversationId));

        ChatSessionStore.restoreSavedStateForTest(savedStateWithConversation("restored-conversation", "GD-202"));

        assertEquals(2, ChatSessionStore.conversationCountForTest());
        assertTrue(ChatSessionStore.containsConversationIdForTest(liveConversationId));
        assertTrue(ChatSessionStore.containsConversationIdForTest("restored-conversation"));
    }

    private static void recordTurn(String conversationId, String question, String guideId) {
        ChatSessionStore.memoryFor(conversationId).recordTurn(
            question,
            "Short answer: Keep the thread preview stateful.",
            List.of(
                new SearchResult(
                    "Guide " + guideId,
                    "",
                    "",
                    "",
                    guideId,
                    "Preview",
                    "survival",
                    "guide-focus"
                )
            )
        );
    }

    private static String savedStateWithConversation(String conversationId, String guideId) throws Exception {
        SessionMemory memory = new SessionMemory();
        memory.recordTurn(
            "restored question",
            "Short answer: Restore this thread.",
            List.of(
                new SearchResult(
                    "Guide " + guideId,
                    "",
                    "",
                    "",
                    guideId,
                    "Restore",
                    "survival",
                    "guide-focus"
                )
            )
        );
        JSONObject conversationJson = new JSONObject()
            .put("id", conversationId)
            .put("memory", memory.toJson());
        return new JSONObject()
            .put("conversations", new JSONArray().put(conversationJson))
            .toString();
    }
}

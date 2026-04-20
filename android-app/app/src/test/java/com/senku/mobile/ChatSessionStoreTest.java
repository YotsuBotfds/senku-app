package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

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
}

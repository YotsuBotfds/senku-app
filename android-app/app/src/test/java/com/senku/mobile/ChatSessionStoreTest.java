package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.ContextWrapper;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void contextAwareStaleConversationReusePersistsClearedAnchorPriorBias() throws Exception {
        ChatSessionStore.resetForTest();
        SessionMemory.setAnchorPriorEnabledForTest(true);
        try {
            TestContext context = new TestContext();
            String conversationId = ChatSessionStore.createConversation();
            recordTurnAt(conversationId, "how do i build a water filter", "GD-252", 1_000L);
            recordTurnAt(conversationId, "what about charcoal", "GD-252", 2_000L);
            ChatSessionStore.persist(context);

            ChatSessionStore.resetForTest();
            ChatSessionStore.restore(context);
            ChatSessionStore.ensureConversationIdForTest(
                context,
                conversationId,
                2_000L + SessionMemory.anchorIdleResetMsForTest() + 1L
            );

            SessionMemory restoredMemory = memoryFromSavedState(context.savedState(), conversationId);
            SessionMemory.RetrievalPlan plan = restoredMemory.buildRetrievalPlan("what about sand");

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
        recordTurnAt(firstConversationId, "first question", "GD-001", 1_000L);
        recordTurnAt(secondConversationId, "second question", "GD-002", 2_000L);
        recordTurnAt(thirdConversationId, "third question", "GD-003", 3_000L);

        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 2);

        assertEquals(2, previews.size());
        assertEquals(thirdConversationId, previews.get(0).conversationId);
        assertEquals(secondConversationId, previews.get(1).conversationId);
        assertEquals("third question", previews.get(0).latestTurn.question);
    }

    @Test
    public void recentConversationPreviewsIgnoreReadOnlyMemoryAccessOrder() {
        ChatSessionStore.resetForTest();
        String firstConversationId = ChatSessionStore.createConversation();
        String secondConversationId = ChatSessionStore.createConversation();
        recordTurnAt(firstConversationId, "first older question", "GD-101", 1_000L);
        recordTurnAt(secondConversationId, "second newer question", "GD-202", 2_000L);

        ChatSessionStore.memoryFor(firstConversationId);
        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 2);

        assertEquals(2, previews.size());
        assertEquals(secondConversationId, previews.get(0).conversationId);
        assertEquals(firstConversationId, previews.get(1).conversationId);
        assertEquals(2_000L, previews.get(0).lastActivityEpoch);
        assertEquals(1_000L, previews.get(1).lastActivityEpoch);
    }

    @Test
    public void recentConversationPreviewsSuppressNearDuplicateQuestions() {
        ChatSessionStore.resetForTest();
        String olderConversationId = ChatSessionStore.createConversation();
        String newerConversationId = ChatSessionStore.createConversation();
        recordTurnAt(olderConversationId, "How do I store water?", "GD-619", 1_000L);
        recordTurnAt(newerConversationId, "  how   do i store water?  ", "GD-035", 2_000L);

        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 10);

        assertEquals(1, previews.size());
        assertEquals(newerConversationId, previews.get(0).conversationId);
        assertEquals("GD-035", previews.get(0).latestTurn.sourceResults.get(0).guideId);
    }

    @Test
    public void recentConversationPreviewsSuppressPunctuationOnlyDuplicateQuestions() {
        ChatSessionStore.resetForTest();
        String olderConversationId = ChatSessionStore.createConversation();
        String newerConversationId = ChatSessionStore.createConversation();
        recordTurnAt(olderConversationId, "How do I store water", "GD-619", 1_000L);
        recordTurnAt(newerConversationId, "How do I store water?!", "GD-035", 2_000L);

        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 10);

        assertEquals(1, previews.size());
        assertEquals(newerConversationId, previews.get(0).conversationId);
    }

    @Test
    public void recentConversationPreviewsKeepDuplicateQuestionsOutsideDedupeWindow() {
        ChatSessionStore.resetForTest();
        String olderConversationId = ChatSessionStore.createConversation();
        String newerConversationId = ChatSessionStore.createConversation();
        long outsideWindowEpochMs = 1_000L + (31L * 60L * 1000L);
        recordTurnAt(olderConversationId, "How do I store water?", "GD-619", 1_000L);
        recordTurnAt(newerConversationId, "  how   do i store water?  ", "GD-035", outsideWindowEpochMs);

        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 10);

        assertEquals(2, previews.size());
        assertEquals(newerConversationId, previews.get(0).conversationId);
        assertEquals(olderConversationId, previews.get(1).conversationId);
    }

    @Test
    public void restoreSavedStateCapsConversationWindowToNewestStatefulThreads() throws Exception {
        ChatSessionStore.resetForTest();

        ChatSessionStore.restoreSavedStateForTest(savedStateWithConversations(12));
        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(null, 20);

        assertEquals(10, ChatSessionStore.conversationCountForTest());
        assertFalse(ChatSessionStore.containsConversationIdForTest("conversation-01"));
        assertFalse(ChatSessionStore.containsConversationIdForTest("conversation-02"));
        assertTrue(ChatSessionStore.containsConversationIdForTest("conversation-03"));
        assertTrue(ChatSessionStore.containsConversationIdForTest("conversation-12"));
        assertEquals(10, previews.size());
        assertEquals("conversation-12", previews.get(0).conversationId);
        assertEquals("conversation-03", previews.get(9).conversationId);
    }

    @Test
    public void removeConversationDropsRecentPreviewAndPersistsRemoval() throws Exception {
        ChatSessionStore.resetForTest();
        String keptConversationId = ChatSessionStore.createConversation();
        String removedConversationId = ChatSessionStore.createConversation();
        recordTurnAt(keptConversationId, "kept question", "GD-101", 1_000L);
        recordTurnAt(removedConversationId, "removed question", "GD-202", 2_000L);
        TestContext context = new TestContext();

        ChatSessionStore.removeConversation(context, removedConversationId);

        List<ChatSessionStore.ConversationPreview> livePreviews =
            ChatSessionStore.recentConversationPreviews(null, 10);
        assertEquals(1, livePreviews.size());
        assertEquals(keptConversationId, livePreviews.get(0).conversationId);
        assertFalse(ChatSessionStore.containsConversationIdForTest(removedConversationId));

        String persistedState = context.savedState();
        ChatSessionStore.resetForTest();
        ChatSessionStore.restoreSavedStateForTest(persistedState);
        List<ChatSessionStore.ConversationPreview> restoredPreviews =
            ChatSessionStore.recentConversationPreviews(null, 10);

        assertEquals(1, restoredPreviews.size());
        assertEquals(keptConversationId, restoredPreviews.get(0).conversationId);
        assertFalse(ChatSessionStore.containsConversationIdForTest(removedConversationId));
    }

    @Test
    public void openingRecentThreadDoesNotChangeRecencyUntilNewTurnIsRecorded() {
        ChatSessionStore.resetForTest();
        String olderConversationId = ChatSessionStore.createConversation();
        String newerConversationId = ChatSessionStore.createConversation();
        recordTurnAt(olderConversationId, "older thread", "GD-101", 1_000L);
        recordTurnAt(newerConversationId, "newer thread", "GD-202", 2_000L);

        ChatSessionStore.memoryFor(olderConversationId);
        List<ChatSessionStore.ConversationPreview> previewsAfterOpen =
            ChatSessionStore.recentConversationPreviews(null, 10);

        assertEquals(newerConversationId, previewsAfterOpen.get(0).conversationId);
        assertEquals(olderConversationId, previewsAfterOpen.get(1).conversationId);

        ChatSessionStore.memoryFor(olderConversationId).recordTranscriptFixtureTurnForTest(
            "older follow up",
            "Follow-up preview.",
            "Short answer: Follow-up preview.",
            List.of(
                new SearchResult(
                    "Guide GD-101",
                    "",
                    "",
                    "",
                    "GD-101",
                    "Preview",
                    "survival",
                    "guide-focus"
                )
            ),
            "",
            3_000L
        );
        List<ChatSessionStore.ConversationPreview> previewsAfterNewTurn =
            ChatSessionStore.recentConversationPreviews(null, 10);

        assertEquals(olderConversationId, previewsAfterNewTurn.get(0).conversationId);
        assertEquals("older follow up", previewsAfterNewTurn.get(0).latestTurn.question);
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

    private static void recordTurnAt(String conversationId, String question, String guideId, long recordedAtEpochMs) {
        ChatSessionStore.memoryFor(conversationId).recordTranscriptFixtureTurnForTest(
            question,
            "Keep the thread preview stateful.",
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
            ),
            "",
            recordedAtEpochMs
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

    private static String savedStateWithConversations(int count) throws Exception {
        JSONArray conversations = new JSONArray();
        for (int index = 1; index <= count; index++) {
            String guideId = String.format("GD-%03d", index);
            SessionMemory memory = new SessionMemory();
            memory.recordTranscriptFixtureTurnForTest(
                "restored question " + index,
                "Restore this thread.",
                "Short answer: Restore this thread.",
                List.of(new SearchResult(
                    "Guide " + guideId,
                    "",
                    "",
                    "",
                    guideId,
                    "Restore",
                    "survival",
                    "guide-focus"
                )),
                "",
                1_000L + index
            );
            conversations.put(new JSONObject()
                .put("id", String.format("conversation-%02d", index))
                .put("memory", memory.toJson()));
        }
        return new JSONObject().put("conversations", conversations).toString();
    }

    private static SessionMemory memoryFromSavedState(String saved, String conversationId) throws Exception {
        JSONArray conversations = new JSONObject(saved).optJSONArray("conversations");
        for (int index = 0; conversations != null && index < conversations.length(); index++) {
            JSONObject conversation = conversations.optJSONObject(index);
            if (conversation != null && conversationId.equals(conversation.optString("id", ""))) {
                return SessionMemory.fromJson(conversation.optString("memory", ""));
            }
        }
        return new SessionMemory();
    }

    private static final class TestContext extends ContextWrapper {
        private final InMemorySharedPreferences prefs = new InMemorySharedPreferences();

        TestContext() {
            super(null);
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return prefs;
        }

        String savedState() {
            return prefs.getString("state_v1", "");
        }
    }

    private static final class InMemorySharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Map<String, ?> getAll() {
            return new HashMap<>(values);
        }

        @Override
        public String getString(String key, String defValue) {
            Object value = values.get(key);
            return value instanceof String ? (String) value : defValue;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Object value = values.get(key);
            return value instanceof Set ? (Set<String>) value : defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            Object value = values.get(key);
            return value instanceof Integer ? (Integer) value : defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            Object value = values.get(key);
            return value instanceof Long ? (Long) value : defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            Object value = values.get(key);
            return value instanceof Float ? (Float) value : defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object value = values.get(key);
            return value instanceof Boolean ? (Boolean) value : defValue;
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public Editor edit() {
            return new InMemoryEditor();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        private final class InMemoryEditor implements Editor {
            private final Map<String, Object> pending = new HashMap<>();
            private final java.util.HashSet<String> removals = new java.util.HashSet<>();
            private boolean clear;

            @Override
            public Editor putString(String key, String value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> values) {
                pending.put(key, values);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor remove(String key) {
                pending.remove(key);
                removals.add(key);
                return this;
            }

            @Override
            public Editor clear() {
                clear = true;
                pending.clear();
                removals.clear();
                return this;
            }

            @Override
            public boolean commit() {
                apply();
                return true;
            }

            @Override
            public void apply() {
                if (clear) {
                    values.clear();
                }
                for (String key : removals) {
                    values.remove(key);
                }
                values.putAll(pending);
            }
        }
    }
}

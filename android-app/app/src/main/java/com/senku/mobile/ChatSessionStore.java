package com.senku.mobile;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

public final class ChatSessionStore {
    private static final Object LOCK = new Object();
    private static final String PREFS_NAME = "senku_chat_sessions";
    private static final String KEY_STATE = "state_v1";
    private static final int MAX_CONVERSATIONS = 10;
    private static final long RECENT_THREAD_DEDUP_WINDOW_MS = 30L * 60L * 1000L;
    private static final LinkedHashMap<String, SessionMemory> CONVERSATIONS =
        new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, SessionMemory> eldest) {
                return size() > MAX_CONVERSATIONS;
            }
        };
    private static boolean restored;

    private ChatSessionStore() {
    }

    public static String createConversation() {
        String conversationId = UUID.randomUUID().toString();
        synchronized (LOCK) {
            CONVERSATIONS.put(conversationId, new SessionMemory());
        }
        return conversationId;
    }

    public static String ensureConversationId(String conversationId) {
        return ensureConversationId(conversationId, System.currentTimeMillis());
    }

    private static String ensureConversationId(String conversationId, long nowEpochMs) {
        String trimmed = conversationId == null ? "" : conversationId.trim();
        if (trimmed.isEmpty()) {
            return createConversation();
        }
        synchronized (LOCK) {
            SessionMemory memory = CONVERSATIONS.get(trimmed);
            if (memory == null) {
                CONVERSATIONS.put(trimmed, new SessionMemory());
            } else {
                memory.markAnchorIdleResetIfStale(nowEpochMs);
            }
        }
        return trimmed;
    }

    public static SessionMemory memoryFor(String conversationId) {
        String resolvedId = ensureConversationId(conversationId);
        synchronized (LOCK) {
            return CONVERSATIONS.get(resolvedId);
        }
    }

    public static void restore(Context context) {
        if (context == null) {
            return;
        }
        synchronized (LOCK) {
            if (restored) {
                return;
            }
            restored = true;
            String saved = preferences(context).getString(KEY_STATE, "");
            if (saved == null || saved.trim().isEmpty()) {
                return;
            }
            try {
                JSONObject root = new JSONObject(saved);
                JSONArray conversations = root.optJSONArray("conversations");
                if (conversations == null) {
                    return;
                }
                for (int index = 0; index < conversations.length(); index++) {
                    JSONObject conversationJson = conversations.optJSONObject(index);
                    if (conversationJson == null) {
                        continue;
                    }
                    String conversationId = conversationJson.optString("id", "").trim();
                    if (conversationId.isEmpty()) {
                        continue;
                    }
                    SessionMemory memory = SessionMemory.fromJson(conversationJson.optString("memory", ""));
                    if (!memory.hasState()) {
                        continue;
                    }
                    CONVERSATIONS.put(conversationId, memory);
                }
            } catch (JSONException ignored) {
                CONVERSATIONS.clear();
            }
        }
    }

    public static void persist(Context context) {
        if (context == null) {
            return;
        }
        JSONObject root = new JSONObject();
        JSONArray conversations = new JSONArray();
        synchronized (LOCK) {
            restored = true;
            try {
                for (Map.Entry<String, SessionMemory> entry : CONVERSATIONS.entrySet()) {
                    if (entry.getValue() == null || !entry.getValue().hasState()) {
                        continue;
                    }
                    JSONObject conversationJson = new JSONObject();
                    conversationJson.put("id", entry.getKey());
                    conversationJson.put("memory", entry.getValue().toJson());
                    conversations.put(conversationJson);
                }
                root.put("conversations", conversations);
            } catch (JSONException ignored) {
                return;
            }
        }
        preferences(context).edit().putString(KEY_STATE, root.toString()).apply();
    }

    public static List<ConversationPreview> recentConversationPreviews(Context context, int maxCount) {
        restore(context);
        ArrayList<ConversationPreview> previews = new ArrayList<>();
        synchronized (LOCK) {
            ArrayList<Map.Entry<String, SessionMemory>> entries = new ArrayList<>(CONVERSATIONS.entrySet());
            LinkedHashMap<String, ConversationPreview> latestByQuestion = new LinkedHashMap<>();
            for (int index = entries.size() - 1; index >= 0 && previews.size() < maxCount; index--) {
                Map.Entry<String, SessionMemory> entry = entries.get(index);
                SessionMemory memory = entry.getValue();
                if (memory == null) {
                    continue;
                }
                SessionMemory.TurnSnapshot latestTurn = memory.latestTurnSnapshot();
                if (latestTurn == null) {
                    continue;
                }
                ConversationPreview preview = new ConversationPreview(
                    entry.getKey(),
                    latestTurn,
                    memory.turnCount(),
                    memory.lastActivityEpoch()
                );
                String questionKey = normalizeRecentThreadQuestion(latestTurn.question);
                ConversationPreview keptPreview = questionKey.isEmpty() ? null : latestByQuestion.get(questionKey);
                if (keptPreview != null && isNearDuplicateRecentThread(keptPreview, preview)) {
                    continue;
                }
                if (!questionKey.isEmpty()) {
                    latestByQuestion.put(questionKey, preview);
                }
                previews.add(preview);
            }
        }
        return previews;
    }

    public static void removeConversation(Context context, String conversationId) {
        String trimmed = conversationId == null ? "" : conversationId.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        synchronized (LOCK) {
            CONVERSATIONS.remove(trimmed);
        }
        persist(context);
    }

    public static final class ConversationPreview {
        public final String conversationId;
        public final SessionMemory.TurnSnapshot latestTurn;
        public final int turnCount;
        public final long lastActivityEpoch;

        ConversationPreview(
            String conversationId,
            SessionMemory.TurnSnapshot latestTurn,
            int turnCount,
            long lastActivityEpoch
        ) {
            this.conversationId = conversationId == null ? "" : conversationId;
            this.latestTurn = latestTurn;
            this.turnCount = Math.max(0, turnCount);
            this.lastActivityEpoch = Math.max(0L, lastActivityEpoch);
        }
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static boolean isNearDuplicateRecentThread(ConversationPreview newerPreview, ConversationPreview olderPreview) {
        if (newerPreview == null || olderPreview == null) {
            return false;
        }
        long newerEpoch = newerPreview.lastActivityEpoch;
        long olderEpoch = olderPreview.lastActivityEpoch;
        if (newerEpoch <= 0L || olderEpoch <= 0L) {
            return false;
        }
        return newerEpoch >= olderEpoch && (newerEpoch - olderEpoch) <= RECENT_THREAD_DEDUP_WINDOW_MS;
    }

    private static String normalizeRecentThreadQuestion(String question) {
        String normalized = question == null ? "" : question.trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(normalized.length());
        boolean previousWhitespace = false;
        for (int index = 0; index < normalized.length(); index++) {
            char ch = normalized.charAt(index);
            if (Character.isWhitespace(ch)) {
                if (!previousWhitespace) {
                    builder.append(' ');
                    previousWhitespace = true;
                }
            } else {
                builder.append(ch);
                previousWhitespace = false;
            }
        }
        return builder.toString().trim();
    }

    static void resetForTest() {
        synchronized (LOCK) {
            CONVERSATIONS.clear();
            restored = false;
        }
    }

    static int conversationCountForTest() {
        synchronized (LOCK) {
            return CONVERSATIONS.size();
        }
    }

    static boolean containsConversationIdForTest(String conversationId) {
        synchronized (LOCK) {
            return CONVERSATIONS.containsKey(conversationId);
        }
    }

    static String ensureConversationIdForTest(String conversationId, long nowEpochMs) {
        return ensureConversationId(conversationId, nowEpochMs);
    }
}

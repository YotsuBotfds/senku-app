package com.senku.mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SessionMemory {
    private static final int JSON_SCHEMA_VERSION = 5;
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final int MAX_TURNS = 6;
    private static final int MAX_VISIBLE_QUESTIONS = 2;
    private static final int MAX_VISIBLE_SOURCES = 3;
    private static volatile boolean ENABLE_ANCHOR_PRIOR = false;
    private static final long ANCHOR_IDLE_RESET_MS = 60L * 60L * 1000L;
    private static final Set<String> SESSION_CONTEXT_HINTS = buildSet(
        "what now", "what next", "and then", "what about", "how long", "should we",
        "do we", "next step", "next steps", "that", "it", "this", "those"
    );
    private static final Set<String> ANCHOR_FOLLOW_UP_HINTS = buildSet(
        "what now", "what next", "and then", "what about", "how long", "should we",
        "do we", "next step", "next steps", "after that", "for children", "for child",
        "how many times", "how often"
    );
    private static final Set<String> ANCHOR_RESET_MARKERS = buildSet(
        "unrelated:", "new question:", "switching topics:", "different question:"
    );
    private static final Set<String> ANCHOR_DEICTIC_TOKENS = buildSet(
        "after", "before", "it", "next", "that", "the", "this", "those"
    );
    private final ArrayList<Turn> turns = new ArrayList<>();
    private String stickyAnchorGuideId = "";
    private int anchorResetTurnIndex = 0;

    public synchronized void clear() {
        turns.clear();
        stickyAnchorGuideId = "";
        anchorResetTurnIndex = 0;
    }

    public synchronized boolean hasState() {
        return !turns.isEmpty();
    }

    public synchronized void recordTurn(String question, String answerBody, List<SearchResult> sources) {
        recordTurn(question, answerBody, sources, null);
    }

    public synchronized void recordTurn(String question, String answerBody, List<SearchResult> sources, String ruleId) {
        recordTurn(question, answerBody, sources, ruleId, ReviewedCardMetadata.empty());
    }

    public synchronized void recordTurn(
        String question,
        String answerBody,
        List<SearchResult> sources,
        String ruleId,
        ReviewedCardMetadata reviewedCardMetadata
    ) {
        String trimmedQuestion = safe(question).trim();
        if (trimmedQuestion.isEmpty()) {
            return;
        }
        ReviewedCardMetadata normalizedReviewedCardMetadata =
            ReviewedCardMetadata.normalize(reviewedCardMetadata);
        maybeRefreshStickyAnchor(trimmedQuestion, sources);
        turns.add(new Turn(
            trimmedQuestion,
            summarizeAnswer(answerBody),
            safe(answerBody).trim(),
            summarizeSources(sources),
            summarizeSourceResults(sources),
            safe(ruleId).trim(),
            normalizedReviewedCardMetadata,
            OfflineAnswerEngine.consumePendingSessionLatencyBreakdown(
                trimmedQuestion,
                safe(answerBody).trim(),
                sources,
                safe(ruleId).trim()
            ),
            System.currentTimeMillis()
        ));
        while (turns.size() > MAX_TURNS) {
            turns.remove(0);
            if (anchorResetTurnIndex > 0) {
                anchorResetTurnIndex -= 1;
            }
        }
    }

    synchronized void recordTranscriptFixtureTurnForTest(
        String question,
        String answerSummary,
        String answerBody,
        List<SearchResult> sources,
        String ruleId,
        long recordedAtEpochMs
    ) {
        String trimmedQuestion = safe(question).trim();
        if (trimmedQuestion.isEmpty()) {
            return;
        }
        maybeRefreshStickyAnchor(trimmedQuestion, sources);
        turns.add(new Turn(
            trimmedQuestion,
            safe(answerSummary).trim(),
            safe(answerBody).trim(),
            summarizeSources(sources),
            summarizeSourceResults(sources),
            safe(ruleId).trim(),
            ReviewedCardMetadata.empty(),
            null,
            recordedAtEpochMs
        ));
        while (turns.size() > MAX_TURNS) {
            turns.remove(0);
            if (anchorResetTurnIndex > 0) {
                anchorResetTurnIndex -= 1;
            }
        }
    }

    public synchronized boolean shouldUseContext(String question) {
        if (turns.isEmpty()) {
            return false;
        }
        String lower = safe(question).trim().toLowerCase(QUERY_LOCALE);
        if (lower.isEmpty()) {
            return false;
        }
        if (isAnchorResetQuery(lower)) {
            return false;
        }
        for (String hint : SESSION_CONTEXT_HINTS) {
            if (lower.contains(hint)) {
                return true;
            }
        }
        return contentTokens(lower).size() <= 4;
    }

    public synchronized boolean isLikelyFollowUp(String question) {
        if (turns.isEmpty()) {
            return false;
        }
        String lower = safe(question).trim().toLowerCase(QUERY_LOCALE);
        if (lower.isEmpty() || isAnchorResetQuery(lower)) {
            return false;
        }
        for (String hint : ANCHOR_FOLLOW_UP_HINTS) {
            if (lower.contains(hint)) {
                return true;
            }
        }
        return isShortDeicticFollowUp(lower);
    }

    public synchronized String contextualizeForRetrieval(String question) {
        return buildLegacyRetrievalQuery(question);
    }

    public synchronized RetrievalPlan buildRetrievalPlan(String question) {
        String trimmed = safe(question).trim();
        QueryMetadataProfile sessionProfile = buildSessionMetadataProfile(trimmed);
        PackRepository.AnchorPriorDirective anchorPrior = buildAnchorPriorDirective(trimmed);
        Turn latest = turns.isEmpty() ? null : turns.get(turns.size() - 1);
        SessionRetrievalContextPolicy.QueryPlan policyPlan = SessionRetrievalContextPolicy.plan(
            trimmed,
            shouldUseContext(trimmed),
            latest == null ? "" : latest.question,
            latest == null ? "" : latest.answerSummary,
            latest == null ? Collections.emptyList() : latest.sourceResults,
            sessionProfile
        );

        return new RetrievalPlan(
            trimmed,
            policyPlan.searchQuery,
            policyPlan.contextSelectionQuery,
            policyPlan.deterministicFallbackQuery,
            policyPlan.sessionUsed ? renderSummary() : "",
            policyPlan.sessionUsed ? recentSourceResults() : Collections.emptyList(),
            sessionProfile,
            policyPlan.sessionUsed,
            policyPlan.directFocusTokenCount,
            anchorPrior
        );
    }

    private synchronized PackRepository.AnchorPriorDirective buildAnchorPriorDirective(String question) {
        if (!ENABLE_ANCHOR_PRIOR || !isLikelyFollowUp(question)) {
            return null;
        }
        int firstEligibleTurn = Math.max(0, Math.min(anchorResetTurnIndex, turns.size()));
        for (int index = turns.size() - 1; index >= firstEligibleTurn; index--) {
            String guideId = primaryGuideId(turns.get(index).sourceResults);
            if (guideId.isEmpty()) {
                continue;
            }
            int turnsSinceAnchor = (turns.size() - 1) - index;
            if (anchorDecay(turnsSinceAnchor) <= 0.0) {
                return null;
            }
            return new PackRepository.AnchorPriorDirective(guideId, turnsSinceAnchor, turns.size());
        }
        if (!stickyAnchorGuideId.isEmpty() && retrievalTokens(question).size() <= 4) {
            return new PackRepository.AnchorPriorDirective(stickyAnchorGuideId, 2, turns.size());
        }
        return null;
    }

    private synchronized void maybeRefreshStickyAnchor(String question, List<SearchResult> sources) {
        String nextGuideId = primaryGuideId(sources);
        if (nextGuideId.isEmpty()) {
            return;
        }
        if (stickyAnchorGuideId.isEmpty() || !isLikelyFollowUp(question)) {
            stickyAnchorGuideId = nextGuideId;
        }
    }

    private static double anchorDecay(int turnsSinceAnchor) {
        return switch (turnsSinceAnchor) {
            case 0 -> 1.0;
            case 1 -> 0.6;
            case 2 -> 0.3;
            default -> 0.0;
        };
    }

    private static boolean isAnchorResetQuery(String question) {
        String lower = safe(question).trim().toLowerCase(QUERY_LOCALE);
        if (lower.isEmpty()) {
            return false;
        }
        for (String marker : ANCHOR_RESET_MARKERS) {
            if (lower.startsWith(marker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isShortDeicticFollowUp(String question) {
        String[] split = safe(question).toLowerCase(QUERY_LOCALE).split("[^a-z0-9-]+");
        int tokenCount = 0;
        boolean hasDeictic = false;
        for (String token : split) {
            if (token.isEmpty()) {
                continue;
            }
            tokenCount += 1;
            if (ANCHOR_DEICTIC_TOKENS.contains(token)) {
                hasDeictic = true;
            }
        }
        return tokenCount > 0 && tokenCount <= 10 && hasDeictic;
    }

    private static String primaryGuideId(List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) {
            return "";
        }
        for (SearchResult source : sources) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    private synchronized String buildLegacyRetrievalQuery(String question) {
        Turn latest = turns.isEmpty() ? null : turns.get(turns.size() - 1);
        return SessionRetrievalContextPolicy.buildLegacyRetrievalQuery(
            question,
            shouldUseContext(question),
            latest == null ? "" : latest.question,
            latest == null ? "" : latest.answerSummary,
            latest == null ? Collections.emptyList() : latest.sourceResults
        );
    }

    public synchronized String renderPromptContext(String question) {
        if (!shouldUseContext(question)) {
            return "";
        }
        return renderSummary();
    }

    public synchronized String renderRetrievalContext() {
        return renderRetrievalContext("");
    }

    public synchronized String renderRetrievalContext(String question) {
        if (turns.isEmpty()) {
            return "";
        }

        Turn latest = turns.get(turns.size() - 1);
        ArrayList<String> parts = new ArrayList<>();
        if (!latest.question.isEmpty()) {
            parts.add("recent question: " + latest.question);
        }
        if (shouldIncludeRecentGuides(question) && !latest.sources.isEmpty()) {
            parts.add("recent guides: " + String.join(", ", latest.sources));
        }
        return String.join(" | ", parts);
    }

    public synchronized String renderSummary() {
        if (turns.isEmpty()) {
            return "";
        }

        ArrayList<String> recentQuestions = new ArrayList<>();
        LinkedHashSet<String> recentSources = new LinkedHashSet<>();
        for (int index = turns.size() - 1; index >= 0; index--) {
            Turn turn = turns.get(index);
            if (recentQuestions.size() < MAX_VISIBLE_QUESTIONS) {
                recentQuestions.add(turn.question);
            }
            for (String source : turn.sources) {
                if (recentSources.size() >= MAX_VISIBLE_SOURCES) {
                    break;
                }
                recentSources.add(source);
            }
            if (recentQuestions.size() >= MAX_VISIBLE_QUESTIONS && recentSources.size() >= MAX_VISIBLE_SOURCES) {
                break;
            }
        }
        Collections.reverse(recentQuestions);

        ArrayList<String> parts = new ArrayList<>();
        if (!recentQuestions.isEmpty()) {
            parts.add("recent questions: " + String.join(" ; ", recentQuestions));
        }
        if (!recentSources.isEmpty()) {
            parts.add("recent guides: " + String.join(", ", recentSources));
        }
        Turn latest = turns.get(turns.size() - 1);
        if (!latest.answerSummary.isEmpty()) {
            parts.add("latest answer: " + latest.answerSummary);
        }
        return String.join(" | ", parts);
    }

    public synchronized String renderUiSummary() {
        if (turns.isEmpty()) {
            return "";
        }
        return "Session memory active\n" + renderSummary();
    }

    public synchronized String renderTranscript() {
        if (turns.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int start = Math.max(0, turns.size() - 4);
        for (int index = start; index < turns.size(); index++) {
            Turn turn = turns.get(index);
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append("You: ").append(turn.question);
            if (!turn.answerSummary.isEmpty()) {
                builder.append("\nSenku: ").append(turn.answerSummary);
            }
            if (!turn.sources.isEmpty()) {
                builder.append("\nSources: ").append(String.join(", ", turn.sources));
            }
        }
        return builder.toString().trim();
    }

    public synchronized List<SearchResult> recentSourceResults() {
        if (turns.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(turns.get(turns.size() - 1).sourceResults);
    }

    public synchronized List<TurnSnapshot> recentTurnSnapshots(int maxTurns) {
        if (turns.isEmpty() || maxTurns <= 0) {
            return Collections.emptyList();
        }
        int start = Math.max(0, turns.size() - maxTurns);
        ArrayList<TurnSnapshot> snapshots = new ArrayList<>();
        for (int index = start; index < turns.size(); index++) {
            Turn turn = turns.get(index);
            snapshots.add(new TurnSnapshot(
                turn.question,
                turn.answerSummary,
                turn.answerBody,
                turn.sources,
                turn.sourceResults,
                turn.ruleId,
                turn.reviewedCardMetadata,
                turn.latencyBreakdown,
                turn.recordedAtEpochMs
            ));
        }
        return snapshots;
    }

    public synchronized TurnSnapshot latestTurnSnapshot() {
        if (turns.isEmpty()) {
            return null;
        }
        Turn latest = turns.get(turns.size() - 1);
        return new TurnSnapshot(
            latest.question,
            latest.answerSummary,
            latest.answerBody,
            latest.sources,
            latest.sourceResults,
            latest.ruleId,
            latest.reviewedCardMetadata,
            latest.latencyBreakdown,
            latest.recordedAtEpochMs
        );
    }

    public synchronized int turnCount() {
        return turns.size();
    }

    public synchronized long lastActivityEpoch() {
        if (turns.isEmpty()) {
            return 0L;
        }
        return turns.get(turns.size() - 1).recordedAtEpochMs;
    }

    public synchronized String toJson() {
        JSONObject root = new JSONObject();
        JSONArray turnsArray = new JSONArray();
        try {
            root.put("version", JSON_SCHEMA_VERSION);
            root.put("sticky_anchor_guide_id", stickyAnchorGuideId);
            root.put("anchor_reset_turn_index", anchorResetTurnIndex);
            for (Turn turn : turns) {
                JSONObject turnJson = new JSONObject();
                turnJson.put("question", turn.question);
                turnJson.put("answer_summary", turn.answerSummary);
                turnJson.put("answer_body", turn.answerBody);
                turnJson.put("sources", toStringJsonArray(turn.sources));
                turnJson.put("source_results", toSearchResultJsonArray(turn.sourceResults));
                turnJson.put("rule_id", turn.ruleId);
                if (turn.reviewedCardMetadata.isPresent()) {
                    turnJson.put(
                        ReviewedCardMetadata.TRANSPORT_FIELD,
                        ReviewedCardMetadata.toJsonObject(turn.reviewedCardMetadata)
                    );
                }
                if (turn.latencyBreakdown != null) {
                    turnJson.put("latency_breakdown", toLatencyBreakdownJson(turn.latencyBreakdown));
                }
                turnJson.put("recorded_at_epoch_ms", turn.recordedAtEpochMs);
                turnsArray.put(turnJson);
            }
            root.put("turns", turnsArray);
        } catch (JSONException ignored) {
            return "";
        }
        return root.toString();
    }

    public static SessionMemory fromJson(String json) {
        SessionMemory memory = new SessionMemory();
        String trimmed = safe(json).trim();
        if (trimmed.isEmpty()) {
            return memory;
        }
        try {
            JSONObject root = new JSONObject(trimmed);
            int version = root.optInt("version", 1);
            if (version >= 4 && version <= JSON_SCHEMA_VERSION) {
                memory.stickyAnchorGuideId = safe(root.optString("sticky_anchor_guide_id", "")).trim();
                memory.anchorResetTurnIndex = Math.max(0, root.optInt("anchor_reset_turn_index", 0));
            } else if (version == 3) {
                memory.stickyAnchorGuideId = safe(root.optString("sticky_anchor_guide_id", "")).trim();
                memory.anchorResetTurnIndex = Math.max(0, root.optInt("anchor_reset_turn_index", 0));
            } else if (version == 2) {
                memory.stickyAnchorGuideId = safe(root.optString("sticky_anchor_guide_id", "")).trim();
            } else if (version != 1) {
                return memory;
            }
            JSONArray turnsArray = root.optJSONArray("turns");
            if (turnsArray == null) {
                return memory;
            }
            for (int index = 0; index < turnsArray.length(); index++) {
                JSONObject turnJson = turnsArray.optJSONObject(index);
                if (turnJson == null) {
                    continue;
                }
                Turn turn = new Turn(
                    turnJson.optString("question", ""),
                    turnJson.optString("answer_summary", ""),
                    turnJson.optString("answer_body", ""),
                    fromStringJsonArray(turnJson.optJSONArray("sources")),
                    fromSearchResultJsonArray(turnJson.optJSONArray("source_results")),
                    turnJson.optString("rule_id", ""),
                    ReviewedCardMetadata.fromJsonObject(
                        turnJson.optJSONObject(ReviewedCardMetadata.TRANSPORT_FIELD)
                    ),
                    fromLatencyBreakdownJson(turnJson.optJSONObject("latency_breakdown")),
                    turnJson.optLong("recorded_at_epoch_ms", 0L)
                );
                if (!turn.question.isEmpty()) {
                    memory.turns.add(turn);
                }
            }
        } catch (JSONException ignored) {
            return new SessionMemory();
        }
        while (memory.turns.size() > MAX_TURNS) {
            memory.turns.remove(0);
            if (memory.anchorResetTurnIndex > 0) {
                memory.anchorResetTurnIndex -= 1;
            }
        }
        memory.anchorResetTurnIndex = Math.max(
            0,
            Math.min(memory.anchorResetTurnIndex, memory.turns.size())
        );
        if (memory.stickyAnchorGuideId.isEmpty() && memory.anchorResetTurnIndex == 0) {
            for (Turn turn : memory.turns) {
                String guideId = primaryGuideId(turn.sourceResults);
                if (!guideId.isEmpty()) {
                    memory.stickyAnchorGuideId = guideId;
                    break;
                }
            }
        }
        return memory;
    }

    synchronized boolean markAnchorIdleResetIfStale(long nowEpochMs) {
        long lastActivity = lastActivityEpoch();
        if (lastActivity <= 0L || nowEpochMs <= lastActivity) {
            return false;
        }
        if ((nowEpochMs - lastActivity) <= ANCHOR_IDLE_RESET_MS) {
            return false;
        }
        stickyAnchorGuideId = "";
        anchorResetTurnIndex = turns.size();
        return true;
    }

    private QueryMetadataProfile buildSessionMetadataProfile(String question) {
        if (turns.isEmpty()) {
            return QueryMetadataProfile.fromQuery(question);
        }
        Turn latest = turns.get(turns.size() - 1);
        return QueryMetadataProfile.fromQuery(latest.question + " " + safe(question).trim());
    }

    private static List<String> summarizeSources(List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        for (SearchResult source : sources) {
            String guideId = safe(source.guideId).trim();
            String title = safe(source.title).trim();
            if (!guideId.isEmpty() && !title.isEmpty()) {
                labels.add("[" + guideId + "] " + title);
            } else if (!title.isEmpty()) {
                labels.add(title);
            }
            if (labels.size() >= MAX_VISIBLE_SOURCES) {
                break;
            }
        }
        return new ArrayList<>(labels);
    }

    private static String summarizeAnswer(String answerBody) {
        String text = safe(answerBody)
            .replaceAll("(?im)^answer\\s*$", "")
            .replaceAll("(?im)^short answer:\\s*", "")
            .replaceAll("(?im)^steps:\\s*", "")
            .replaceAll("(?im)^limits or safety:\\s*", "")
            .replaceAll("(?im)^sources used.*$", "")
            .replaceAll("\\s+", " ")
            .trim();
        if (text.isEmpty()) {
            return "";
        }
        int sentenceBreak = text.indexOf(". ");
        if (sentenceBreak > 0) {
            text = text.substring(0, sentenceBreak + 1);
        }
        if (text.length() <= 140) {
            return text;
        }
        return text.substring(0, 137).trim() + "...";
    }

    private static List<SearchResult> summarizeSourceResults(List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> summarized = new ArrayList<>();
        for (SearchResult source : sources) {
            summarized.add(source);
            if (summarized.size() >= MAX_VISIBLE_SOURCES) {
                break;
            }
        }
        return summarized;
    }

    private static Set<String> contentTokens(String text) {
        return SessionRetrievalContextPolicy.contentTokens(text);
    }

    private static Set<String> retrievalTokens(String text) {
        return SessionRetrievalContextPolicy.retrievalTokens(text);
    }

    private static boolean shouldIncludeRecentGuides(String question) {
        return SessionRetrievalContextPolicy.shouldIncludeRecentGuides(question);
    }

    static boolean tokensMatchForTest(String left, String right) {
        return SessionRetrievalContextPolicy.tokensMatch(left, right);
    }

    static String singularizeForTest(String token) {
        return SessionRetrievalContextPolicy.singularize(token);
    }

    static void setAnchorPriorEnabledForTest(boolean enabled) {
        ENABLE_ANCHOR_PRIOR = enabled;
    }

    static long anchorIdleResetMsForTest() {
        return ANCHOR_IDLE_RESET_MS;
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }

    private static JSONArray toStringJsonArray(List<String> values) throws JSONException {
        JSONArray array = new JSONArray();
        if (values == null) {
            return array;
        }
        for (String value : values) {
            array.put(safe(value));
        }
        return array;
    }

    private static JSONArray toSearchResultJsonArray(List<SearchResult> values) throws JSONException {
        JSONArray array = new JSONArray();
        if (values == null) {
            return array;
        }
        for (SearchResult result : values) {
            JSONObject resultJson = new JSONObject();
            resultJson.put("title", safe(result == null ? null : result.title));
            resultJson.put("subtitle", safe(result == null ? null : result.subtitle));
            resultJson.put("snippet", safe(result == null ? null : result.snippet));
            resultJson.put("body", safe(result == null ? null : result.body));
            resultJson.put("guide_id", safe(result == null ? null : result.guideId));
            resultJson.put("section_heading", safe(result == null ? null : result.sectionHeading));
            resultJson.put("category", safe(result == null ? null : result.category));
            resultJson.put("retrieval_mode", safe(result == null ? null : result.retrievalMode));
            resultJson.put("content_role", safe(result == null ? null : result.contentRole));
            resultJson.put("time_horizon", safe(result == null ? null : result.timeHorizon));
            resultJson.put("structure_type", safe(result == null ? null : result.structureType));
            resultJson.put("topic_tags", safe(result == null ? null : result.topicTags));
            array.put(resultJson);
        }
        return array;
    }

    private static List<String> fromStringJsonArray(JSONArray array) {
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        }
        ArrayList<String> values = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            String value = safe(array.optString(index, "")).trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private static List<SearchResult> fromSearchResultJsonArray(JSONArray array) {
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> values = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject resultJson = array.optJSONObject(index);
            if (resultJson == null) {
                continue;
            }
            values.add(new SearchResult(
                resultJson.optString("title", ""),
                resultJson.optString("subtitle", ""),
                resultJson.optString("snippet", ""),
                resultJson.optString("body", ""),
                resultJson.optString("guide_id", ""),
                resultJson.optString("section_heading", ""),
                resultJson.optString("category", ""),
                resultJson.optString("retrieval_mode", ""),
                resultJson.optString("content_role", ""),
                resultJson.optString("time_horizon", ""),
                resultJson.optString("structure_type", ""),
                resultJson.optString("topic_tags", "")
            ));
        }
        return values;
    }

    private static JSONObject toLatencyBreakdownJson(OfflineAnswerEngine.LatencyBreakdown latencyBreakdown)
        throws JSONException {
        JSONObject output = new JSONObject();
        if (latencyBreakdown == null) {
            return output;
        }
        output.put("query_class", safe(latencyBreakdown.queryClass));
        output.put("retrieval_ms", latencyBreakdown.retrievalMs);
        output.put("rerank_ms", latencyBreakdown.rerankMs);
        output.put("prompt_build_ms", latencyBreakdown.promptBuildMs);
        output.put("first_token_ms", latencyBreakdown.firstTokenMs);
        output.put("decode_ms", latencyBreakdown.decodeMs);
        output.put("total_ms", latencyBreakdown.totalMs);
        return output;
    }

    private static OfflineAnswerEngine.LatencyBreakdown fromLatencyBreakdownJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        return new OfflineAnswerEngine.LatencyBreakdown(
            object.optString("query_class", ""),
            object.optLong("retrieval_ms", 0L),
            object.optLong("rerank_ms", 0L),
            object.optLong("prompt_build_ms", 0L),
            object.optLong("first_token_ms", 0L),
            object.optLong("decode_ms", object.optLong("generation_ms", 0L)),
            object.optLong("total_ms", 0L)
        );
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    private static final class Turn {
        final String question;
        final String answerSummary;
        final String answerBody;
        final List<String> sources;
        final List<SearchResult> sourceResults;
        final String ruleId;
        final ReviewedCardMetadata reviewedCardMetadata;
        final OfflineAnswerEngine.LatencyBreakdown latencyBreakdown;
        final long recordedAtEpochMs;

        Turn(
            String question,
            String answerSummary,
            String answerBody,
            List<String> sources,
            List<SearchResult> sourceResults,
            String ruleId,
            ReviewedCardMetadata reviewedCardMetadata,
            OfflineAnswerEngine.LatencyBreakdown latencyBreakdown,
            long recordedAtEpochMs
        ) {
            this.question = question;
            this.answerSummary = answerSummary;
            this.answerBody = answerBody == null ? "" : answerBody;
            this.sources = sources == null ? Collections.emptyList() : new ArrayList<>(sources);
            this.sourceResults = sourceResults == null ? Collections.emptyList() : new ArrayList<>(sourceResults);
            this.ruleId = ruleId == null ? "" : ruleId;
            this.reviewedCardMetadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
            this.latencyBreakdown = latencyBreakdown;
            this.recordedAtEpochMs = recordedAtEpochMs > 0L ? recordedAtEpochMs : System.currentTimeMillis();
        }
    }

    public static final class RetrievalPlan {
        public final String originalQuery;
        public final String searchQuery;
        public final String contextSelectionQuery;
        public final String deterministicFallbackQuery;
        public final String promptContext;
        public final List<SearchResult> recentSources;
        public final QueryMetadataProfile metadataProfile;
        public final boolean sessionUsed;
        public final int directFocusTokenCount;
        public final PackRepository.AnchorPriorDirective anchorPrior;

        RetrievalPlan(
            String originalQuery,
            String searchQuery,
            String contextSelectionQuery,
            String deterministicFallbackQuery,
            String promptContext,
            List<SearchResult> recentSources,
            QueryMetadataProfile metadataProfile,
            boolean sessionUsed,
            int directFocusTokenCount,
            PackRepository.AnchorPriorDirective anchorPrior
        ) {
            this.originalQuery = originalQuery == null ? "" : originalQuery;
            this.searchQuery = searchQuery == null ? "" : searchQuery;
            this.contextSelectionQuery = contextSelectionQuery == null ? "" : contextSelectionQuery;
            this.deterministicFallbackQuery = deterministicFallbackQuery == null ? "" : deterministicFallbackQuery;
            this.promptContext = promptContext == null ? "" : promptContext;
            this.recentSources = recentSources == null ? Collections.emptyList() : new ArrayList<>(recentSources);
            this.metadataProfile = metadataProfile == null ? QueryMetadataProfile.fromQuery(originalQuery) : metadataProfile;
            this.sessionUsed = sessionUsed;
            this.directFocusTokenCount = directFocusTokenCount;
            this.anchorPrior = anchorPrior;
        }
    }

    public static final class TurnSnapshot {
        public final String question;
        public final String answerSummary;
        public final String answerBody;
        public final List<String> sources;
        public final List<SearchResult> sourceResults;
        public final String ruleId;
        public final ReviewedCardMetadata reviewedCardMetadata;
        public final OfflineAnswerEngine.LatencyBreakdown latencyBreakdown;
        public final long recordedAtEpochMs;

        TurnSnapshot(
            String question,
            String answerSummary,
            String answerBody,
            List<String> sources,
            List<SearchResult> sourceResults,
            String ruleId,
            long recordedAtEpochMs
        ) {
            this(
                question,
                answerSummary,
                answerBody,
                sources,
                sourceResults,
                ruleId,
                ReviewedCardMetadata.empty(),
                null,
                recordedAtEpochMs
            );
        }

        TurnSnapshot(
            String question,
            String answerSummary,
            String answerBody,
            List<String> sources,
            List<SearchResult> sourceResults,
            String ruleId,
            OfflineAnswerEngine.LatencyBreakdown latencyBreakdown,
            long recordedAtEpochMs
        ) {
            this(
                question,
                answerSummary,
                answerBody,
                sources,
                sourceResults,
                ruleId,
                ReviewedCardMetadata.empty(),
                latencyBreakdown,
                recordedAtEpochMs
            );
        }

        TurnSnapshot(
            String question,
            String answerSummary,
            String answerBody,
            List<String> sources,
            List<SearchResult> sourceResults,
            String ruleId,
            ReviewedCardMetadata reviewedCardMetadata,
            OfflineAnswerEngine.LatencyBreakdown latencyBreakdown,
            long recordedAtEpochMs
        ) {
            this.question = question == null ? "" : question;
            this.answerSummary = answerSummary == null ? "" : answerSummary;
            this.answerBody = answerBody == null ? "" : answerBody;
            this.sources = sources == null ? Collections.emptyList() : new ArrayList<>(sources);
            this.sourceResults = sourceResults == null ? Collections.emptyList() : new ArrayList<>(sourceResults);
            this.ruleId = ruleId == null ? "" : ruleId;
            this.reviewedCardMetadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
            this.latencyBreakdown = latencyBreakdown;
            this.recordedAtEpochMs = recordedAtEpochMs;
        }
    }
}

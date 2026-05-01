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
    private static final int MAX_RETRIEVAL_TERMS = 10;
    private static final int MAX_RETRIEVAL_SOURCE_GUIDES = 2;
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
    private static final Set<String> STOP_TOKENS = buildSet(
        "a", "about", "an", "and", "are", "at", "be", "but", "by", "can", "do", "does",
        "for", "from", "how", "i", "if", "in", "is", "it", "my", "of", "on",
        "or", "our", "should", "that", "the", "then", "this", "to", "use", "using",
        "we", "what", "with", "you", "your"
    );
    private static final Set<String> RETRIEVAL_CONTEXT_STOP_TOKENS = buildSet(
        "about", "available", "build", "construct", "fallback", "it", "light", "lighting",
        "make", "next", "not", "start", "that", "then", "this", "those", "using", "when"
    );
    private static final Set<String> HOUSE_SITE_FOLLOW_UP_MARKERS = buildSet(
        "access", "drainage", "flood", "frost", "hazard", "runoff", "shade",
        "site", "slope", "soil", "sun", "wind"
    );
    private static final Set<String> HOUSE_SITE_SEASONAL_FOLLOW_UP_MARKERS = buildSet(
        "winter", "summer", "shade", "sun", "solar", "microclimate", "orientation"
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
        if (!shouldUseContext(trimmed)) {
            return new RetrievalPlan(
                trimmed,
                trimmed,
                trimmed,
                trimmed,
                "",
                Collections.emptyList(),
                sessionProfile,
                false,
                retrievalTokens(trimmed).size(),
                anchorPrior
            );
        }

        String legacyRetrievalQuery = buildLegacyRetrievalQuery(trimmed);
        String contextSelectionQuery = buildStructuredContextQuery(trimmed, sessionProfile);
        if (contextSelectionQuery.isEmpty()) {
            contextSelectionQuery = legacyRetrievalQuery;
        }
        if (contextSelectionQuery.isEmpty()) {
            contextSelectionQuery = trimmed;
        }

        int directFocusTokenCount = retrievalTokens(trimmed).size();
        boolean focusedHouseFollowUp = isFocusedHouseFollowUp(trimmed, sessionProfile);
        String searchQuery = directFocusTokenCount >= 3 && !focusedHouseFollowUp ? trimmed : contextSelectionQuery;
        if (searchQuery.isEmpty()) {
            searchQuery = legacyRetrievalQuery.isEmpty() ? trimmed : legacyRetrievalQuery;
        }

        return new RetrievalPlan(
            trimmed,
            searchQuery,
            contextSelectionQuery,
            legacyRetrievalQuery,
            renderSummary(),
            recentSourceResults(),
            sessionProfile,
            true,
            directFocusTokenCount,
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
        if (!shouldUseContext(question)) {
            return safe(question).trim();
        }
        LinkedHashSet<String> retrievalTerms = new LinkedHashSet<>(retrievalTokens(question));
        if (!turns.isEmpty()) {
            Turn latest = turns.get(turns.size() - 1);
            QueryMetadataProfile sessionProfile = QueryMetadataProfile.fromQuery(
                latest.question + " " + safe(question).trim()
            );
            addSourceContextTerms(
                retrievalTerms,
                prioritizeSourceContextResults(question, latest.sourceResults),
                MAX_RETRIEVAL_SOURCE_GUIDES,
                latest.question,
                question,
                sessionProfile
            );
            if (retrievalTerms.size() < 7 && sessionProfile != null && !sessionProfile.preferredTopicTags().isEmpty()) {
                addContextTerms(retrievalTerms, latest.answerSummary, 3, true);
            }
            if (retrievalTerms.size() < 4) {
                addContextTerms(retrievalTerms, latest.question, 4, true);
            }
        }
        if (retrievalTerms.isEmpty()) {
            return buildRetrievalFocus(question);
        }
        ArrayList<String> ordered = new ArrayList<>();
        for (String term : retrievalTerms) {
            ordered.add(term);
            if (ordered.size() >= MAX_RETRIEVAL_TERMS) {
                break;
            }
        }
        return String.join(" ", ordered).trim();
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
        if (memory.stickyAnchorGuideId.isEmpty()) {
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

    private String buildStructuredContextQuery(String question, QueryMetadataProfile sessionProfile) {
        LinkedHashSet<String> contextTerms = new LinkedHashSet<>(retrievalTokens(question));
        boolean hasStructuredHints = hasStructuredSessionHints(sessionProfile);
        boolean focusedHouseFollowUp = isFocusedHouseFollowUp(question, sessionProfile);
        boolean seasonalHouseSiteFollowUp = isSeasonalHouseSiteFollowUp(question, sessionProfile);
        int directFocusTokenCount = retrievalTokens(question).size();
        if (!turns.isEmpty()) {
            Turn latest = turns.get(turns.size() - 1);
            if (focusedHouseFollowUp) {
                if (seasonalHouseSiteFollowUp) {
                    addContextTerms(contextTerms, "site selection", 2, true);
                    addContextTerms(contextTerms, "wind exposure", 2, true);
                    addContextTerms(contextTerms, "microclimate", 1, true);
                    addContextTerms(contextTerms, "sun exposure", 2, true);
                } else {
                    addPreferredTopicContextTerms(contextTerms, sessionProfile, 2);
                }
            }
            addStructureContextTerms(contextTerms, sessionProfile);
            if (contextTerms.size() < 4) {
                if (focusedHouseFollowUp) {
                    if (seasonalHouseSiteFollowUp) {
                        addContextTerms(contextTerms, "seasonal considerations", 2, true);
                    } else {
                        addPreferredTopicContextTerms(contextTerms, sessionProfile, 1);
                    }
                } else {
                    addSourceContextTerms(
                        contextTerms,
                        prioritizeSourceContextResults(question, latest.sourceResults),
                        MAX_RETRIEVAL_SOURCE_GUIDES,
                        latest.question,
                        question,
                        sessionProfile
                    );
                }
            }
            if (contextTerms.size() < 4 && hasStructuredHints && directFocusTokenCount == 0) {
                addPreferredTopicContextTerms(contextTerms, sessionProfile, 2);
            }
            if (contextTerms.size() < 2) {
                addPreferredTopicContextTerms(contextTerms, sessionProfile, 2);
            }
            if (contextTerms.size() < 2) {
                addContextTerms(contextTerms, latest.question, 2, true);
            }
            if (contextTerms.size() < 2) {
                addContextTerms(contextTerms, latest.answerSummary, 2, true);
            }
        }
        if (contextTerms.isEmpty()) {
            return buildRetrievalFocus(question);
        }
        ArrayList<String> ordered = new ArrayList<>();
        for (String term : contextTerms) {
            ordered.add(term);
            if (ordered.size() >= MAX_RETRIEVAL_TERMS) {
                break;
            }
        }
        return String.join(" ", ordered).trim();
    }

    private static boolean isFocusedHouseFollowUp(String question, QueryMetadataProfile sessionProfile) {
        if (sessionProfile == null) {
            return false;
        }
        String preferredStructureType = safe(sessionProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE);
        if (!"cabin_house".equals(preferredStructureType)) {
            return false;
        }
        int directTokenCount = retrievalTokens(question).size();
        if (directTokenCount <= 0 || directTokenCount > 4) {
            return false;
        }
        if (directTokenCount <= 2) {
            return true;
        }
        Set<String> questionTokens = retrievalTokens(question);
        if (sessionProfile.hasExplicitTopicFocus() && !questionTokens.isEmpty()) {
            return true;
        }
        for (String marker : HOUSE_SITE_FOLLOW_UP_MARKERS) {
            if (containsRelatedToken(questionTokens, marker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSeasonalHouseSiteFollowUp(String question, QueryMetadataProfile sessionProfile) {
        if (!isFocusedHouseFollowUp(question, sessionProfile)) {
            return false;
        }
        Set<String> questionTokens = retrievalTokens(question);
        for (String marker : HOUSE_SITE_SEASONAL_FOLLOW_UP_MARKERS) {
            if (containsRelatedToken(questionTokens, marker)) {
                return true;
            }
        }
        return false;
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
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        String[] split = safe(text).toLowerCase(QUERY_LOCALE).split("[^a-z0-9-]+");
        for (String token : split) {
            if (token.length() < 2 || STOP_TOKENS.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private static String buildRetrievalFocus(String question) {
        String trimmed = safe(question).trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        Set<String> tokens = retrievalTokens(trimmed);
        if (tokens.isEmpty()) {
            return trimmed;
        }
        return String.join(" ", tokens);
    }

    private static Set<String> retrievalTokens(String text) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        String[] split = safe(text).toLowerCase(QUERY_LOCALE).split("[^a-z0-9-]+");
        for (String token : split) {
            if (token.length() < 2 || STOP_TOKENS.contains(token) || RETRIEVAL_CONTEXT_STOP_TOKENS.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private static void addContextTerms(LinkedHashSet<String> target, String text, int maxToAdd, boolean dropCurrentStops) {
        if (target == null || maxToAdd <= 0) {
            return;
        }
        int added = 0;
        Set<String> tokens = dropCurrentStops ? retrievalTokens(text) : contentTokens(text);
        for (String token : tokens) {
            if (target.add(token)) {
                added += 1;
            }
            if (added >= maxToAdd) {
                break;
            }
        }
    }

    private static void addSourceContextTerms(
        LinkedHashSet<String> target,
        List<SearchResult> sources,
        int maxSources,
        String latestQuestion,
        String followUpQuestion,
        QueryMetadataProfile sessionProfile
    ) {
        if (target == null || sources == null || sources.isEmpty() || maxSources <= 0) {
            return;
        }
        Set<String> questionTokens = retrievalTokens(safe(latestQuestion) + " " + safe(followUpQuestion));
        Set<String> preferredTopicTags = sessionProfile == null
            ? Collections.emptySet()
            : sessionProfile.preferredTopicTags();
        Set<String> preferredHintTokens = topicHintTokens(preferredTopicTags);
        String preferredStructureType = sessionProfile == null
            ? ""
            : safe(sessionProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE);
        int inspected = 0;
        boolean foundStrongSource = false;
        for (SearchResult source : sources) {
            int sizeBefore = target.size();
            Set<String> sectionTokens = retrievalTokens(source.sectionHeading);
            String normalizedStructureType = safe(source.structureType).trim().toLowerCase(QUERY_LOCALE);
            if (!normalizedStructureType.isEmpty()
                && !"general".equals(normalizedStructureType)
                && (normalizedStructureType.equals(preferredStructureType)
                    || overlaps(retrievalTokens(source.structureType), questionTokens)
                    || overlaps(retrievalTokens(source.structureType), sectionTokens))) {
                addContextTerms(target, source.structureType, 2, true);
            }
            addSectionContextTerms(target, sectionTokens, questionTokens, preferredHintTokens);
            for (String topicTag : splitCsv(source.topicTags)) {
                if (!shouldUseSourceTopicTag(topicTag, preferredTopicTags, questionTokens, sectionTokens)) {
                    continue;
                }
                addContextTerms(target, topicTag.replace('_', ' '), 2, true);
                if (target.size() >= MAX_RETRIEVAL_TERMS) {
                    break;
                }
            }
            inspected += 1;
            if (target.size() > sizeBefore) {
                foundStrongSource = true;
            }
            if (target.size() >= MAX_RETRIEVAL_TERMS || (foundStrongSource && inspected >= 1) || inspected >= maxSources) {
                break;
            }
        }
    }

    private static void addStructureContextTerms(LinkedHashSet<String> target, QueryMetadataProfile sessionProfile) {
        if (target == null || sessionProfile == null) {
            return;
        }
        switch (safe(sessionProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE)) {
            case "cabin_house" -> addContextTerms(target, "cabin house", 2, true);
            case "earth_shelter" -> addContextTerms(target, "earth shelter", 2, true);
            case "emergency_shelter" -> addContextTerms(target, "shelter", 1, true);
            case "clay_oven" -> {
                addContextTerms(target, "clay oven", 2, true);
                addContextTerms(target, "masonry hearth", 2, true);
            }
            case "water_distribution" -> {
                addContextTerms(target, "water distribution", 2, true);
                addContextTerms(target, "water storage", 2, true);
            }
            case "water_storage" -> {
                if (sessionProfile.hasExplicitTopic("water_distribution")) {
                    addContextTerms(target, "water distribution", 2, true);
                }
                addContextTerms(target, "water storage", 2, true);
            }
            case "water_purification" -> addContextTerms(target, "water purification", 2, true);
            case "small_watercraft" -> addContextTerms(target, "boat watercraft", 2, true);
            case "wound_care" -> addContextTerms(target, "wound care", 2, true);
            case "community_security" -> {
                addContextTerms(target, "community security", 2, true);
                addContextTerms(target, "resource security", 2, true);
            }
            case "community_governance" -> {
                addContextTerms(target, "community governance", 2, true);
                addContextTerms(target, "conflict resolution", 2, true);
                addContextTerms(target, "trust systems", 2, true);
            }
            default -> {
            }
        }
    }

    private static void addPreferredTopicContextTerms(
        LinkedHashSet<String> target,
        QueryMetadataProfile sessionProfile,
        int maxTags
    ) {
        if (target == null || sessionProfile == null || maxTags <= 0) {
            return;
        }
        int addedTags = 0;
        boolean candleContext = containsRelatedToken(target, "candle") || containsRelatedToken(target, "candles");
        for (String topicTag : orderedTopicTags(sessionProfile)) {
            if (candleContext && ("soapmaking".equals(topicTag) || "lye_safety".equals(topicTag))) {
                continue;
            }
            int sizeBefore = target.size();
            addContextTerms(target, topicTag.replace('_', ' '), 2, true);
            if (target.size() > sizeBefore) {
                addedTags += 1;
            }
            if (addedTags >= maxTags || target.size() >= MAX_RETRIEVAL_TERMS) {
                break;
            }
        }
    }

    private static List<String> orderedTopicTags(QueryMetadataProfile sessionProfile) {
        String preferredStructureType = sessionProfile == null
            ? ""
            : safe(sessionProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE);
        boolean explicitWaterDistribution = sessionProfile != null
            && (sessionProfile.hasExplicitTopic("water_distribution")
                || "water_distribution".equals(preferredStructureType));
        ArrayList<String> ordered = new ArrayList<>();
        switch (preferredStructureType) {
            case "cabin_house" -> Collections.addAll(
                ordered,
                "site_selection",
                "foundation",
                "drainage",
                "wall_construction",
                "roofing",
                "weatherproofing",
                "ventilation"
            );
            case "clay_oven" -> Collections.addAll(
                ordered,
                "clay_oven",
                "masonry_hearth"
            );
            case "water_storage" -> {
                if (explicitWaterDistribution) {
                    ordered.add("water_distribution");
                    ordered.add("water_storage");
                    break;
                }
                Collections.addAll(
                    ordered,
                    "container_sanitation",
                    "water_storage",
                    "water_rotation"
                );
            }
            case "water_distribution" -> Collections.addAll(
                ordered,
                "water_distribution",
                "water_storage"
            );
            case "water_purification" -> Collections.addAll(
                ordered,
                "water_purification",
                "prefilter",
                "disinfection"
            );
            case "small_watercraft" -> Collections.addAll(
                ordered,
                "small_watercraft",
                "hull",
                "sealing"
            );
            case "wound_care" -> Collections.addAll(
                ordered,
                "wound_cleaning",
                "infection_monitoring",
                "first_aid"
            );
            case "community_security" -> Collections.addAll(
                ordered,
                "community_security",
                "resource_security"
            );
            case "community_governance" -> Collections.addAll(
                ordered,
                "community_governance",
                "conflict_resolution",
                "trust_systems"
            );
            default -> {
            }
        }
        for (String topicTag : sessionProfile == null
            ? Collections.<String>emptySet()
            : sessionProfile.preferredTopicTags()) {
            if (explicitWaterDistribution
                && ("container_sanitation".equals(topicTag) || "water_rotation".equals(topicTag))) {
                continue;
            }
            if (!ordered.contains(topicTag)) {
                ordered.add(topicTag);
            }
        }
        return ordered;
    }

    private static boolean hasStructuredSessionHints(QueryMetadataProfile sessionProfile) {
        if (sessionProfile == null) {
            return false;
        }
        return !safe(sessionProfile.preferredStructureType()).trim().isEmpty()
            || !sessionProfile.preferredTopicTags().isEmpty();
    }

    private static List<SearchResult> prioritizeSourceContextResults(String question, List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> questionTokens = retrievalTokens(question);
        if (questionTokens.isEmpty()) {
            return new ArrayList<>(sources);
        }

        ArrayList<PrioritizedSource> prioritized = new ArrayList<>();
        for (int index = 0; index < sources.size(); index++) {
            SearchResult source = sources.get(index);
            int score = overlapCount(questionTokens, retrievalTokens(source.title)) * 4;
            score += overlapCount(questionTokens, retrievalTokens(source.sectionHeading)) * 6;
            score += overlapCount(questionTokens, retrievalTokens(source.topicTags.replace('_', ' '))) * 3;
            prioritized.add(new PrioritizedSource(source, index, score));
        }
        prioritized.sort((left, right) -> {
            int scoreOrder = Integer.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            return Integer.compare(left.originalIndex, right.originalIndex);
        });

        ArrayList<SearchResult> ordered = new ArrayList<>();
        for (PrioritizedSource prioritizedSource : prioritized) {
            ordered.add(prioritizedSource.source);
        }
        return ordered;
    }

    private static boolean shouldIncludeRecentGuides(String question) {
        return contentTokens(question).size() < 2;
    }

    private static void addSectionContextTerms(
        LinkedHashSet<String> target,
        Set<String> sectionTokens,
        Set<String> questionTokens,
        Set<String> preferredHintTokens
    ) {
        if (target == null || sectionTokens.isEmpty()) {
            return;
        }
        int added = 0;
        for (String token : sectionTokens) {
            if (!containsRelatedToken(questionTokens, token) && !containsRelatedToken(preferredHintTokens, token)) {
                continue;
            }
            if (target.add(token)) {
                added += 1;
            }
            if (added >= 2) {
                break;
            }
        }
    }

    private static boolean shouldUseSourceTopicTag(
        String topicTag,
        Set<String> preferredTopicTags,
        Set<String> questionTokens,
        Set<String> sectionTokens
    ) {
        String normalizedTag = safe(topicTag).trim().toLowerCase(QUERY_LOCALE);
        if (normalizedTag.isEmpty()) {
            return false;
        }
        if (preferredTopicTags.contains(normalizedTag)) {
            return true;
        }
        Set<String> tagTokens = retrievalTokens(normalizedTag.replace('_', ' '));
        return overlaps(tagTokens, questionTokens) || overlaps(tagTokens, sectionTokens);
    }

    private static Set<String> topicHintTokens(Set<String> preferredTopicTags) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        if (preferredTopicTags == null || preferredTopicTags.isEmpty()) {
            return tokens;
        }
        for (String topicTag : preferredTopicTags) {
            tokens.addAll(retrievalTokens(topicTag.replace('_', ' ')));
        }
        return tokens;
    }

    private static Set<String> splitCsv(String text) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String value : safe(text).split(",")) {
            String normalized = value.trim().toLowerCase(QUERY_LOCALE);
            if (!normalized.isEmpty()) {
                values.add(normalized);
            }
        }
        return values;
    }

    private static boolean overlaps(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        for (String value : left) {
            if (containsRelatedToken(right, value)) {
                return true;
            }
        }
        return false;
    }

    private static int overlapCount(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String value : left) {
            if (containsRelatedToken(right, value)) {
                count += 1;
            }
        }
        return count;
    }

    private static boolean containsRelatedToken(Set<String> tokens, String candidate) {
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }
        String normalizedCandidate = normalizeToken(candidate);
        if (normalizedCandidate.isEmpty()) {
            return false;
        }
        for (String token : tokens) {
            if (tokensMatch(normalizeToken(token), normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tokensMatch(String left, String right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        String singularLeft = singularize(left);
        String singularRight = singularize(right);
        if (singularLeft.equals(singularRight)) {
            return true;
        }
        return boundedPrefixMatch(singularLeft, singularRight)
            || boundedPrefixMatch(singularRight, singularLeft);
    }

    private static String singularize(String token) {
        if (token.endsWith("ies") && token.length() > 3) {
            return token.substring(0, token.length() - 3) + "y";
        }
        if (token.endsWith("ss")) {
            return token;
        }
        if (token.endsWith("s") && token.length() > 3) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }

    private static boolean boundedPrefixMatch(String shorter, String longer) {
        if (shorter.length() >= longer.length()) {
            return false;
        }
        if (shorter.length() < 4) {
            return false;
        }
        return longer.startsWith(shorter) && (longer.length() - shorter.length()) <= 3;
    }

    static boolean tokensMatchForTest(String left, String right) {
        return tokensMatch(left, right);
    }

    static String singularizeForTest(String token) {
        return singularize(token);
    }

    static void setAnchorPriorEnabledForTest(boolean enabled) {
        ENABLE_ANCHOR_PRIOR = enabled;
    }

    static long anchorIdleResetMsForTest() {
        return ANCHOR_IDLE_RESET_MS;
    }

    private static String normalizeToken(String token) {
        return safe(token).trim().toLowerCase(QUERY_LOCALE);
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

    private static final class PrioritizedSource {
        final SearchResult source;
        final int originalIndex;
        final int score;

        PrioritizedSource(SearchResult source, int originalIndex, int score) {
            this.source = source;
            this.originalIndex = originalIndex;
            this.score = score;
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

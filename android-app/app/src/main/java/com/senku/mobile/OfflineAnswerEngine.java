package com.senku.mobile;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.senku.mobile.telemetry.LatencyPanel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;

public final class OfflineAnswerEngine {
    private static final String TAG = "SenkuMobile";
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final int ANSWER_CANDIDATE_LIMIT = 16;
    private static final int ANSWER_CONTEXT_LIMIT = 4;
    private static final int MODEL_MAX_TOKENS = 2048;
    private static final int ABSTAIN_TOP_CHUNK_LIMIT = 3;
    private static final int ABSTAIN_MAX_OVERLAP_TOKENS = 1;
    private static final int ABSTAIN_MIN_UNIQUE_LEXICAL_HITS = 2;
    private static final double UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD = 0.65d;
    private static final double UNCERTAIN_FIT_MIN_VECTOR_SIMILARITY = 0.45d;
    private static final double UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY = 0.62d;
    private static final String STRUCTURE_TYPE_SAFETY_POISONING = "safety_poisoning";
    static final String SAFETY_CRITICAL_ESCALATION_LINE =
        "If this is urgent or could be a safety risk, stop and call local emergency services now "
            + "(911 where applicable); if this may be poisoning, call Poison Control now, and "
            + "keep the person with a trusted adult while waiting.";
    private static final Set<String> QUERY_STOP_TOKENS = buildSet(
        "a", "about", "an", "and", "are", "at", "be", "but", "by", "can", "do", "does",
        "for", "from", "how", "i", "if", "in", "is", "it", "my", "next", "of", "on",
        "or", "our", "should", "that", "the", "then", "this", "to", "use", "using",
        "we", "what", "with", "you", "your"
    );
    private static final Set<String> CONTEXT_SELECTION_STOP_TOKENS = buildSet(
        "construction", "freshly", "known", "make", "light", "lighting"
    );
    private static final Set<String> SAFETY_CRITICAL_EXPLICIT_MARKERS = buildSet(
        "poison control", "overdose", "self-harm", "self harm", "suicidal", "suicide", "911", "988"
    );
    private static final Set<String> SAFETY_CRITICAL_MENTAL_HEALTH_MARKERS = buildSet(
        "hearing voices",
        "voice telling",
        "hallucination",
        "hallucinations",
        "paranoid",
        "paranoia",
        "normal rules do not apply",
        "special mission",
        "acting invincible",
        "nothing can hurt",
        "won't stop moving",
        "will not stop moving",
        "barely eating",
        "hardly eating"
    );
    private static final Set<String> ACUTE_MENTAL_HEALTH_QUERY_MARKERS = buildSet(
        "barely slept",
        "hardly slept",
        "keeps pacing",
        "normal rules do not apply",
        "special mission",
        "acting invincible",
        "nothing can hurt",
        "just stress",
        "calm down"
    );
    private static final Set<String> ACUTE_MENTAL_HEALTH_SUPPORT_MARKERS = buildSet(
        "mental health",
        "behavior",
        "behaviour",
        "agitation",
        "pacing",
        "calm down",
        "de-escalation",
        "de escalation",
        "crisis",
        "hallucination",
        "hallucinations",
        "paranoia",
        "psychiatric",
        "urgent support",
        "trusted adult"
    );
    private static final Set<String> SAFETY_CRITICAL_ACUTE_QUERY_MARKERS = buildSet(
        "right now",
        "immediately",
        "first aid",
        "not breathing",
        "can't breathe",
        "cannot breathe",
        "bleeding",
        "hemorrhage",
        "overdose",
        "poison",
        "poisoning",
        "swallowed",
        "ingested",
        "seizure",
        "unconscious",
        "stroke",
        "heart attack",
        "chest pain",
        "anaphylaxis",
        "allergic reaction",
        "eye injury",
        "animal bite",
        "rabies",
        "severe burn",
        "puncture wound",
        "drain cleaner",
        "bleach",
        "battery acid",
        "lye",
        "ammonia",
        "chemical burn",
        "chemical in eye",
        "chemical on skin"
    );
    private static final Set<String> SAFETY_CRITICAL_EMERGENCY_CONTEXT_MARKERS = buildSet("urgent", "emergency");
    private static final int MAX_PENDING_SESSION_LATENCIES = 12;
    private static final Object PENDING_SESSION_LATENCIES_LOCK = new Object();
    private static final LinkedHashMap<String, LatencyBreakdown> PENDING_SESSION_LATENCIES =
        new LinkedHashMap<String, LatencyBreakdown>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, LatencyBreakdown> eldest) {
                return size() > MAX_PENDING_SESSION_LATENCIES;
            }
        };

    private OfflineAnswerEngine() {
    }

    public enum ConfidenceLabel {
        HIGH,
        MEDIUM,
        LOW
    }

    public enum AnswerMode {
        CONFIDENT,
        UNCERTAIN_FIT,
        ABSTAIN
    }

    public interface AnswerProgressListener {
        void onAnswerBody(String partialAnswerBody);

        default void onConfidenceLabel(ConfidenceLabel label) {
        }

        default void onFallbackToOnDevice(String statusText) {
        }
    }

    interface HostGenerator {
        HostInferenceClient.Result generate(
            HostInferenceConfig.Settings settings,
            String systemPrompt,
            String prompt,
            Integer maxTokens
        ) throws Exception;
    }

    interface OnDeviceGenerator {
        String generate(
            Context context,
            File modelFile,
            String prompt,
            Integer maxTokens,
            LiteRtModelRunner.PartialResultListener listener
        ) throws Exception;
    }

    private static final HostGenerator DEFAULT_HOST_GENERATOR = HostInferenceClient::generate;
    private static final OnDeviceGenerator DEFAULT_ON_DEVICE_GENERATOR =
        (context, modelFile, prompt, maxTokens, listener) -> LiteRtModelRunner.generateStreaming(
            context,
            modelFile.getAbsolutePath(),
            prompt,
            maxTokens,
            listener
        );

    private static final BiConsumer<String, String> DEFAULT_DEBUG_LOG_SINK =
        OfflineAnswerEngine::logToAndroidDebug;
    private static HostGenerator hostGenerator = DEFAULT_HOST_GENERATOR;
    private static OnDeviceGenerator onDeviceGenerator = DEFAULT_ON_DEVICE_GENERATOR;
    private static BiConsumer<String, String> debugLogSink = DEFAULT_DEBUG_LOG_SINK;

    public static AnswerRun run(Context context, PackRepository repository, SessionMemory sessionMemory, File modelFile, String query)
        throws Exception {
        PreparedAnswer prepared = prepare(context, repository, sessionMemory, modelFile, query);
        return generate(context, modelFile, prepared);
    }

    public static PreparedAnswer prepare(
        Context context,
        PackRepository repository,
        SessionMemory sessionMemory,
        File modelFile,
        String query
    ) throws Exception {
        String trimmedQuery = query == null ? "" : query.trim();
        if (trimmedQuery.isEmpty()) {
            throw new IllegalArgumentException("Enter a question first");
        }
        if (repository == null) {
            throw new IllegalStateException("Pack is not ready yet");
        }
        long prepareStartedAtMs = System.currentTimeMillis();
        long prepareStartedAtNs = SystemClock.elapsedRealtimeNanos();
        long rerankNs = 0L;
        SessionMemory.RetrievalPlan retrievalPlan = sessionMemory.buildRetrievalPlan(trimmedQuery);
        String retrievalQuery = safe(retrievalPlan.searchQuery).trim();
        boolean sessionUsed = retrievalPlan.sessionUsed;
        String contextSelectionQuery = safe(retrievalPlan.contextSelectionQuery).trim();
        if (contextSelectionQuery.isEmpty()) {
            contextSelectionQuery = trimmedQuery;
        }

        DeterministicAnswerRouter.DeterministicAnswer deterministic = selectDeterministicAnswer(
            trimmedQuery,
            retrievalPlan.deterministicFallbackQuery,
            contextSelectionQuery,
            sessionUsed
        );
        if (deterministic != null) {
            String answerBody = PromptBuilder.buildAnswerBody(deterministic.answerText, deterministic.sources, 0);
            PreparedAnswer preparedAnswer = PreparedAnswer.deterministic(
                trimmedQuery,
                answerBody,
                deterministic.sources,
                sessionUsed,
                deterministic.ruleId,
                prepareStartedAtMs
            );
            logPreparedFinalModeIfReady(preparedAnswer);
            return preparedAnswer;
        }

        HostInferenceConfig.Settings inferenceSettings = HostInferenceConfig.resolve(context);

        if (!inferenceSettings.enabled && modelFile == null) {
            throw new IllegalStateException("Import a .litertlm or .task model first");
        }

        String sessionPromptContext = retrievalPlan.promptContext;
        List<SearchResult> recentSources = sessionUsed
            ? prioritizeRecentSources(trimmedQuery, retrievalPlan.recentSources)
            : Collections.emptyList();

        long startedAt = System.currentTimeMillis();
        Log.d(TAG, "ask.start query=\"" + trimmedQuery + "\" sessionUsed=" + sessionUsed);
        List<SearchResult> answerCandidates = repository.search(
            retrievalQuery,
            ANSWER_CANDIDATE_LIMIT,
            retrievalPlan.anchorPrior
        );
        if (sessionUsed) {
            answerCandidates = mergeSessionCandidates(answerCandidates, recentSources);
            long rerankStartedAtNs = SystemClock.elapsedRealtimeNanos();
            answerCandidates = rerankWithSessionHints(
                trimmedQuery,
                contextSelectionQuery,
                retrievalPlan.metadataProfile,
                recentSources,
                answerCandidates
            );
            rerankNs += Math.max(0L, SystemClock.elapsedRealtimeNanos() - rerankStartedAtNs);
        }
        long searchFinishedAt = System.currentTimeMillis();
        Log.d(
            TAG,
            "ask.search query=\"" + trimmedQuery + "\" candidates=" + answerCandidates.size() +
                " elapsedMs=" + (searchFinishedAt - startedAt)
        );

        List<SearchResult> contextResults = repository.buildGuideAnswerContext(
            contextSelectionQuery,
            answerCandidates,
            ANSWER_CONTEXT_LIMIT
        );
        long contextFinishedAt = System.currentTimeMillis();
        Log.d(
            TAG,
            "ask.context query=\"" + trimmedQuery + "\" contextQuery=\"" + contextSelectionQuery +
                "\" context=" + contextResults.size() +
                " elapsedMs=" + (contextFinishedAt - searchFinishedAt)
        );
        if (contextResults.isEmpty()) {
            contextResults = AnswerContextSelector.select(answerCandidates, ANSWER_CONTEXT_LIMIT, trimmedQuery);
            Log.d(
                TAG,
                "ask.contextFallback query=\"" + trimmedQuery + "\" context=" + contextResults.size()
            );
        }
        if (sessionUsed) {
            contextResults = boostRecentContextMatches(trimmedQuery, contextResults, recentSources);
            long rerankStartedAtNs = SystemClock.elapsedRealtimeNanos();
            contextResults = rerankWithSessionHints(
                trimmedQuery,
                contextSelectionQuery,
                retrievalPlan.metadataProfile,
                recentSources,
                contextResults
            );
            rerankNs += Math.max(0L, SystemClock.elapsedRealtimeNanos() - rerankStartedAtNs);
        }
        contextResults = trimWeakSessionContext(contextSelectionQuery, sessionUsed, contextResults, recentSources);

        List<SearchResult> modeCandidates = answerCandidates.isEmpty() ? contextResults : answerCandidates;
        List<SearchResult> gateContext = contextResults.isEmpty() ? modeCandidates : contextResults;
        ConfidenceLabel confidenceLabel = confidenceLabel(
            gateContext,
            contextSelectionQuery,
            retrievalPlan.metadataProfile
        );
        boolean safetyCritical = isSafetyCriticalQuery(trimmedQuery, gateContext);
        AnswerMode answerMode = resolveAnswerMode(
            gateContext,
            modeCandidates,
            contextSelectionQuery,
            retrievalPlan.metadataProfile,
            confidenceLabel,
            safetyCritical
        );
        if (answerMode == AnswerMode.ABSTAIN) {
            List<SearchResult> adjacentGuides = topAbstainChunks(gateContext);
            logDebug(
                TAG,
                "ask.abstain query=\"" + trimmedQuery + "\" adjacentGuides=" + adjacentGuides.size()
            );
            long retrievalMs = Math.max(0L, elapsedMsSince(prepareStartedAtNs) - nanosToMillis(rerankNs));
            PreparedAnswer preparedAnswer = PreparedAnswer.abstain(
                trimmedQuery,
                buildAbstainAnswerBody(trimmedQuery, adjacentGuides, safetyCritical),
                adjacentGuides,
                sessionUsed,
                prepareStartedAtMs,
                retrievalMs,
                nanosToMillis(rerankNs),
                0L,
                confidenceLabel,
                safetyCritical
            );
            logPreparedFinalModeIfReady(preparedAnswer);
            return preparedAnswer;
        }
        if (answerMode == AnswerMode.UNCERTAIN_FIT) {
            List<SearchResult> adjacentGuides = topAbstainChunks(gateContext);
            logDebug(
                TAG,
                "ask.uncertain_fit query=\"" + trimmedQuery + "\" adjacentGuides=" + adjacentGuides.size()
            );
            long retrievalMs = Math.max(0L, elapsedMsSince(prepareStartedAtNs) - nanosToMillis(rerankNs));
            PreparedAnswer preparedAnswer = PreparedAnswer.uncertainFit(
                trimmedQuery,
                buildUncertainFitAnswerBody(trimmedQuery, adjacentGuides, confidenceLabel, safetyCritical),
                adjacentGuides,
                sessionUsed,
                prepareStartedAtMs,
                retrievalMs,
                nanosToMillis(rerankNs),
                0L,
                confidenceLabel,
                safetyCritical
            );
            logPreparedFinalModeIfReady(preparedAnswer);
            return preparedAnswer;
        }

        int promptContextLimit = promptContextLimitFor(contextSelectionQuery);
        List<SearchResult> promptContextResults = contextResults.isEmpty()
            ? Collections.emptyList()
            : new ArrayList<>(contextResults.subList(0, Math.min(promptContextLimit, contextResults.size())));

        long promptStartedAtNs = SystemClock.elapsedRealtimeNanos();
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            trimmedQuery,
            promptContextResults,
            sessionPromptContext,
            contextSelectionQuery
        );
        String systemPrompt = PromptBuilder.buildOfflineAnswerSystemPrompt(contextSelectionQuery);
        String confidenceInstruction = buildConfidenceSystemInstruction(confidenceLabel);
        if (!confidenceInstruction.isEmpty()) {
            systemPrompt = systemPrompt + "\n\n" + confidenceInstruction;
        }
        long promptFinishedAtNs = SystemClock.elapsedRealtimeNanos();
        long rerankMs = nanosToMillis(rerankNs);
        long retrievalMs = Math.max(
            0L,
            nanosToMillis(promptStartedAtNs - prepareStartedAtNs) - rerankMs
        );
        long promptMs = nanosToMillis(promptFinishedAtNs - promptStartedAtNs);
        Log.d(
            TAG,
            "ask.prompt query=\"" + trimmedQuery + "\" promptContext=" + promptContextResults.size() +
                " elapsedMs=" + promptMs
        );

        return PreparedAnswer.generative(
            trimmedQuery,
            promptContextResults,
            sessionUsed,
            prepareStartedAtMs,
            inferenceSettings,
            systemPrompt,
            prompt,
            retrievalMs,
            rerankMs,
            promptMs,
            LatencyPanel.classifyQuery(trimmedQuery, promptContextResults, false, false),
            confidenceLabel,
            safetyCritical
        );
    }

    public static AnswerRun generate(Context context, File modelFile, PreparedAnswer prepared)
        throws Exception {
        return generate(context, modelFile, prepared, null);
    }

    public static AnswerRun generate(
        Context context,
        File modelFile,
        PreparedAnswer prepared,
        AnswerProgressListener progressListener
    ) throws Exception {
        if (prepared == null) {
            throw new IllegalArgumentException("Prepared answer is required");
        }
        if (progressListener != null) {
            progressListener.onConfidenceLabel(prepared.confidenceLabel);
        }
        if (prepared.deterministic) {
            long totalMs = Math.max(0L, System.currentTimeMillis() - prepared.startedAtMs);
            LatencyBreakdown latencyBreakdown = new LatencyBreakdown(
                prepared.queryClass,
                prepared.retrievalMs,
                prepared.rerankMs,
                prepared.promptMs,
                0L,
                0L,
                totalMs
            );
            logFirstTokenMs(prepared.query, "deterministic", 0L);
            logLatencySummary(prepared.query, latencyBreakdown);
            LatencyPanel.emit(prepared.queryClass, latencyBreakdown);
            AnswerRun answerRun = new AnswerRun(
                prepared.query,
                prepared.answerBody,
                prepared.sources,
                0L,
                prepared.sessionUsed,
                true,
                false,
                "Offline answer | deterministic | instant",
                prepared.ruleId,
                latencyBreakdown,
                prepared.confidenceLabel
            );
            logPreparedFinalModeIfReady(prepared);
            rememberSessionLatencyBreakdown(answerRun);
            return answerRun;
        }
        if (prepared.abstain) {
            long totalMs = Math.max(0L, System.currentTimeMillis() - prepared.startedAtMs);
            LatencyBreakdown latencyBreakdown = new LatencyBreakdown(
                prepared.queryClass,
                prepared.retrievalMs,
                prepared.rerankMs,
                prepared.promptMs,
                0L,
                0L,
                totalMs
            );
            logLatencySummary(prepared.query, latencyBreakdown);
            LatencyPanel.emit(prepared.queryClass, latencyBreakdown);
            logPreparedFinalModeIfReady(prepared);
            return new AnswerRun(
                prepared.query,
                prepared.answerBody,
                prepared.sources,
                0L,
                prepared.sessionUsed,
                false,
                true,
                "Abstain | no guide match | instant",
                "",
                latencyBreakdown,
                prepared.confidenceLabel
            );
        }
        if (prepared.mode == AnswerMode.UNCERTAIN_FIT) {
            long totalMs = Math.max(0L, System.currentTimeMillis() - prepared.startedAtMs);
            LatencyBreakdown latencyBreakdown = new LatencyBreakdown(
                prepared.queryClass,
                prepared.retrievalMs,
                prepared.rerankMs,
                prepared.promptMs,
                0L,
                0L,
                totalMs
            );
            logLatencySummary(prepared.query, latencyBreakdown);
            LatencyPanel.emit(prepared.queryClass, latencyBreakdown);
            logPreparedFinalModeIfReady(prepared);
            return new AnswerRun(
                prepared.query,
                prepared.answerBody,
                prepared.sources,
                0L,
                prepared.sessionUsed,
                false,
                false,
                "Uncertain fit | related guides | instant",
                "",
                latencyBreakdown,
                prepared.confidenceLabel,
                prepared.mode
            );
        }

        Context appContext = context == null ? null : context.getApplicationContext();
        String answer;
        String hostBackend = "";
        boolean hostBackendUsed = false;
        boolean hostFallbackUsed = false;
        BestStreamCandidate bestStreamCandidate = new BestStreamCandidate();
        FirstTokenTracker firstTokenTracker = new FirstTokenTracker(prepared.query);
        long generationStartedAtMs = System.currentTimeMillis();
        if (prepared.inferenceSettings.enabled) {
            try {
                firstTokenTracker.begin("host");
                HostInferenceClient.Result hostResult = hostGenerator.generate(
                    prepared.inferenceSettings,
                    prepared.systemPrompt,
                    prepared.prompt,
                    MODEL_MAX_TOKENS
                );
                answer = hostResult.answer;
                hostBackend = hostResult.backend;
                hostBackendUsed = true;
                bestStreamCandidate.consider(answer);
                if (progressListener != null && !safe(answer).trim().isEmpty()) {
                    progressListener.onAnswerBody(answer);
                }
            } catch (Exception hostFailure) {
                if (modelFile == null) {
                    throw hostFailure;
                }
                hostFallbackUsed = true;
                logWarn(
                    TAG,
                    "ask.generate host_failed_falling_back query=\"" + prepared.query +
                        "\" message=\"" + safe(hostFailure.getMessage()) + "\"",
                    hostFailure
                );
                if (progressListener != null) {
                    progressListener.onFallbackToOnDevice(
                        buildHostFallbackGeneratingStatus(context, prepared.sources.size())
                    );
                }
                firstTokenTracker.begin("host_fallback");
                answer = onDeviceGenerator.generate(
                    appContext,
                    modelFile,
                    prepared.prompt,
                    MODEL_MAX_TOKENS,
                    partialText -> {
                        firstTokenTracker.recordPartial(partialText);
                        bestStreamCandidate.consider(partialText);
                        if (progressListener != null) {
                            progressListener.onAnswerBody(partialText);
                        }
                    }
                );
            }
        } else {
            firstTokenTracker.begin("on_device");
            answer = onDeviceGenerator.generate(
                appContext,
                modelFile,
                prepared.prompt,
                MODEL_MAX_TOKENS,
                partialText -> {
                    firstTokenTracker.recordPartial(partialText);
                    bestStreamCandidate.consider(partialText);
                    if (progressListener != null) {
                        progressListener.onAnswerBody(partialText);
                    }
                }
            );
        }
        long elapsedMs = System.currentTimeMillis() - prepared.startedAtMs;
        long generationMs = Math.max(0L, System.currentTimeMillis() - generationStartedAtMs);
        logDebug(TAG, "ask.generate query=\"" + prepared.query + "\" totalElapsedMs=" + elapsedMs);
        String resolvedAnswer = PromptBuilder.sanitizeAnswerText(answer);
        String streamedAnswer = PromptBuilder.sanitizeAnswerText(bestStreamCandidate.bestRawText);
        boolean usedSourceFallback = false;
        if (resolvedAnswer.isEmpty() && !streamedAnswer.isEmpty()) {
            logWarn(TAG, "ask.generate using_streamed_fallback query=\"" + prepared.query + "\"", null);
            resolvedAnswer = streamedAnswer;
        }
        if (!resolvedAnswer.isEmpty() && !streamedAnswer.isEmpty()) {
            boolean finalLooksThin = resolvedAnswer.length() <= 12;
            boolean streamLooksRicher = streamedAnswer.length() >= Math.max(40, resolvedAnswer.length() + 24);
            if (finalLooksThin && streamLooksRicher) {
                logWarn(TAG, "ask.generate preferring_richer_stream query=\"" + prepared.query + "\"", null);
                resolvedAnswer = streamedAnswer;
            }
        }
        if (resolvedAnswer.isEmpty()) {
            logWarn(TAG, "ask.generate using_source_summary_fallback query=\"" + prepared.query + "\"", null);
            resolvedAnswer = PromptBuilder.buildSourceFallbackSummary(prepared.query, prepared.sources);
            usedSourceFallback = true;
        }
        boolean lowCoverage = !usedSourceFallback
            && PromptBuilder.isLowCoverageAnswer(resolvedAnswer);
        String answerBody = usedSourceFallback
            ? PromptBuilder.buildStreamingAnswerBody(resolvedAnswer)
            : PromptBuilder.buildAnswerBody(resolvedAnswer, prepared.sources, elapsedMs);
        String subtitle = lowCoverage
            ? buildLowCoverageSubtitle(modelFile, elapsedMs, prepared.inferenceSettings, hostBackend, hostFallbackUsed)
            : buildSubtitle(modelFile, elapsedMs, prepared.inferenceSettings, hostBackend, hostFallbackUsed);
        if (lowCoverage) {
            logDebug(TAG, "ask.generate low_coverage_detected query=\"" + prepared.query + "\"");
        }
        long firstTokenMs = firstTokenTracker.resolve(generationMs);
        LatencyBreakdown latencyBreakdown = new LatencyBreakdown(
            prepared.queryClass,
            prepared.retrievalMs,
            prepared.rerankMs,
            prepared.promptMs,
            firstTokenMs,
            generationMs,
            elapsedMs
        );
        logLatencySummary(prepared.query, latencyBreakdown);
        LatencyPanel.emit(prepared.queryClass, latencyBreakdown);
        if (lowCoverage) {
            AnswerRun downgradedRun = buildLowCoverageDowngradeAnswerRun(
                modelFile,
                prepared,
                elapsedMs,
                subtitle,
                latencyBreakdown,
                hostBackendUsed,
                hostFallbackUsed
            );
            logAskFinalMode(prepared.query, downgradedRun.mode, "low_coverage_downgrade", elapsedMs);
            rememberSessionLatencyBreakdown(downgradedRun);
            return downgradedRun;
        }
        AnswerRun answerRun = new AnswerRun(
            prepared.query,
            answerBody,
            prepared.sources,
            elapsedMs,
            prepared.sessionUsed,
            false,
            false,
            subtitle,
            null,
            latencyBreakdown,
                prepared.confidenceLabel,
                hostBackendUsed,
                hostFallbackUsed
        );
        String confidentRoute = usedSourceFallback ? "source_summary_fallback" : "confident_generation";
        logAskFinalMode(prepared.query, AnswerMode.CONFIDENT, confidentRoute, elapsedMs);
        rememberSessionLatencyBreakdown(answerRun);
        return answerRun;
    }

    private static void logFirstTokenMs(String query, String path, long firstTokenMs) {
        logDebug(
            TAG,
            "ask.first_token_ms=" + Math.max(0L, firstTokenMs) +
                " query=\"" + safe(query) + "\" path=" + safe(path)
        );
    }

    private static void logLatencySummary(String query, LatencyBreakdown latencyBreakdown) {
        if (latencyBreakdown == null) {
            return;
        }
        logDebug(TAG, buildLatencySummaryLine(query, latencyBreakdown));
    }

    static String buildLatencySummaryLine(String query, LatencyBreakdown latencyBreakdown) {
        LatencyBreakdown safeBreakdown = latencyBreakdown == null
            ? new LatencyBreakdown("", 0L, 0L, 0L, 0L, 0L, 0L)
            : latencyBreakdown;
        return "ask.latency"
            + " queryClass=\"" + safe(safeBreakdown.queryClass) + "\""
            + " retrievalMs=" + safeBreakdown.retrievalMs
            + " rerankMs=" + safeBreakdown.rerankMs
            + " promptBuildMs=" + safeBreakdown.promptBuildMs
            + " firstTokenMs=" + safeBreakdown.firstTokenMs
            + " decodeMs=" + safeBreakdown.decodeMs
            + " totalMs=" + safeBreakdown.totalMs
            + " query=\"" + safe(query) + "\"";
    }

    private static final class BestStreamCandidate {
        String bestRawText = "";
        int bestScore = Integer.MIN_VALUE;

        void consider(String rawPartial) {
            String cleaned = PromptBuilder.sanitizeAnswerText(rawPartial);
            if (cleaned.isEmpty()) {
                return;
            }
            int score = cleaned.length() * 2;
            score += wordCount(cleaned) * 6;
            if (cleaned.contains(".") || cleaned.contains(":") || cleaned.contains("\n")) {
                score += 16;
            }
            if (cleaned.contains("[GD-") || cleaned.contains("[gd-")) {
                score += 10;
            }
            if (score > bestScore) {
                bestScore = score;
                bestRawText = rawPartial;
            }
        }
    }

    private static final class FirstTokenTracker {
        private final String query;
        private String path = "";
        private long startedAtMs = 0L;
        private long firstTokenMs = -1L;

        FirstTokenTracker(String query) {
            this.query = query;
        }

        void begin(String path) {
            this.path = safe(path);
            this.startedAtMs = System.currentTimeMillis();
            this.firstTokenMs = -1L;
        }

        void recordPartial(String partialText) {
            if (firstTokenMs >= 0L) {
                return;
            }
            String visiblePartial = PromptBuilder.sanitizeAnswerText(partialText);
            if (visiblePartial.isEmpty()) {
                return;
            }
            firstTokenMs = Math.max(0L, System.currentTimeMillis() - startedAtMs);
            logFirstTokenMs(query, path, firstTokenMs);
        }

        long resolve(long generationMs) {
            if (firstTokenMs >= 0L) {
                return firstTokenMs;
            }
            long fallbackFirstTokenMs = "host".equals(path) ? generationMs : 0L;
            if ("host".equals(path) || "deterministic".equals(path)) {
                logFirstTokenMs(query, path, fallbackFirstTokenMs);
            }
            return Math.max(0L, fallbackFirstTokenMs);
        }
    }

    private static int wordCount(String text) {
        String[] parts = safe(text).trim().split("\\s+");
        int count = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                count += 1;
            }
        }
        return count;
    }

    private static List<SearchResult> mergeSessionCandidates(List<SearchResult> answerCandidates, List<SearchResult> recentSources) {
        if (recentSources == null || recentSources.isEmpty()) {
            return answerCandidates;
        }
        LinkedHashMap<String, SearchResult> merged = new LinkedHashMap<>();
        if (answerCandidates != null) {
            for (SearchResult candidate : answerCandidates) {
                merged.putIfAbsent(sourceKey(candidate), candidate);
            }
        }
        for (SearchResult source : recentSources) {
            merged.putIfAbsent(sourceKey(source), source);
        }
        return new ArrayList<>(merged.values());
    }

    static int promptContextLimitFor(String contextSelectionQuery) {
        String query = safe(contextSelectionQuery).trim();
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(query);
        int limit = routeProfile.preferredContextItems();
        if (routeProfile.isSeasonalHouseSiteSelectionPrompt(query)) {
            return Math.max(limit, 4);
        }
        return limit;
    }

    private static boolean containsAny(String text, Set<String> markers) {
        String normalized = safe(text).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return false;
        }
        for (String marker : markers) {
            String normalizedMarker = safe(marker).trim().toLowerCase(QUERY_LOCALE);
            if (!normalizedMarker.isEmpty() && normalized.contains(normalizedMarker)) {
                return true;
            }
        }
        return false;
    }

    static List<SearchResult> rerankWithSessionHints(
        String query,
        String contextQuery,
        QueryMetadataProfile metadataProfile,
        List<SearchResult> recentSources,
        List<SearchResult> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> rawTokens = new ArrayList<>(queryTokens(query));
        ArrayList<String> contextTokens = new ArrayList<>(queryTokens(contextQuery));
        boolean focusedExplicitFollowUp = metadataProfile != null
            && "cabin_house".equalsIgnoreCase(safe(metadataProfile.preferredStructureType()).trim())
            && metadataProfile.hasExplicitTopicFocus()
            && !rawTokens.isEmpty()
            && rawTokens.size() <= 2;
        LinkedHashSet<String> recentSourceKeys = new LinkedHashSet<>();
        LinkedHashSet<String> recentGuideKeys = new LinkedHashSet<>();
        if (recentSources != null) {
            for (SearchResult source : recentSources) {
                recentSourceKeys.add(sourceKey(source));
                recentGuideKeys.add(guideKey(source));
            }
        }

        ArrayList<ScoredCandidate> scored = new ArrayList<>();
        for (int index = 0; index < candidates.size(); index++) {
            SearchResult candidate = candidates.get(index);
            String candidateSourceKey = sourceKey(candidate);
            String candidateGuideKey = guideKey(candidate);
            int score = Math.max(0, 240 - (index * 6));
            int rawOverlap = lexicalOverlapScore(candidate, rawTokens);
            int contextOverlap = lexicalOverlapScore(candidate, contextTokens);
            score += rawOverlap * 18;
            score += contextOverlap * 8;
            if (metadataProfile != null) {
                boolean explicitTopicFocus = metadataProfile.hasExplicitTopicFocus();
                boolean explicitTopicMatch = metadataProfile.hasExplicitTopicOverlap(candidate.topicTags);
                int metadataScore = metadataProfile.metadataBonus(
                    candidate.category,
                    candidate.contentRole,
                    candidate.timeHorizon,
                    candidate.structureType,
                    candidate.topicTags
                );
                score += Math.max(-40, Math.min(80, metadataScore * 2));
                score += metadataProfile.sectionHeadingBonus(candidate.sectionHeading);
                if (explicitTopicFocus && rawOverlap > 0) {
                    score += explicitTopicMatch ? 32 : 18;
                    if (!recentGuideKeys.contains(candidateGuideKey)) {
                        score += 12;
                    }
                    if (focusedExplicitFollowUp) {
                        score += 18;
                    }
                }
                if (explicitTopicFocus
                    && !explicitTopicMatch
                    && rawOverlap == 0
                    && !recentSourceKeys.contains(candidateSourceKey)) {
                    score -= 14;
                }
            }

            if (recentSourceKeys.contains(candidateSourceKey)) {
                score += rawTokens.isEmpty() ? 28 : 20;
                if (focusedExplicitFollowUp && rawOverlap == 0) {
                    score -= 24;
                }
            } else if (recentGuideKeys.contains(candidateGuideKey)) {
                int recentGuideBonus = rawTokens.isEmpty() ? 18 : 10;
                if (metadataProfile != null
                    && metadataProfile.hasExplicitTopicFocus()
                    && !metadataProfile.hasExplicitTopicOverlap(candidate.topicTags)
                    && rawOverlap == 0) {
                    recentGuideBonus = Math.max(0, recentGuideBonus - 12);
                }
                if (focusedExplicitFollowUp && rawOverlap == 0) {
                    recentGuideBonus = Math.max(0, recentGuideBonus - 10);
                }
                score += recentGuideBonus;
            }
            if (metadataProfile != null
                && metadataProfile.hasExplicitTopic("water_distribution")
                && recentGuideKeys.contains(candidateGuideKey)
                && safe(candidate.category).trim().equalsIgnoreCase("building")
                && safe(candidate.topicTags).toLowerCase(QUERY_LOCALE).contains("water_distribution")) {
                score += 26;
            }

            if (rawOverlap == 0 && contextOverlap == 0 && metadataProfile != null) {
                score -= 8;
            }
            scored.add(new ScoredCandidate(candidate, index, score));
        }

        scored.sort((left, right) -> {
            int scoreOrder = Integer.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            return Integer.compare(left.originalIndex, right.originalIndex);
        });

        ArrayList<SearchResult> ordered = new ArrayList<>();
        for (ScoredCandidate candidate : scored) {
            ordered.add(candidate.result);
        }
        return ordered;
    }

    static List<SearchResult> boostRecentContextMatches(String query, List<SearchResult> contextResults, List<SearchResult> recentSources) {
        if (recentSources == null || recentSources.isEmpty()) {
            return contextResults == null ? Collections.emptyList() : contextResults;
        }
        List<String> queryTokens = queryTokens(query);
        LinkedHashMap<String, SearchResult> merged = new LinkedHashMap<>();
        if (contextResults != null) {
            for (SearchResult candidate : contextResults) {
                merged.putIfAbsent(sourceKey(candidate), candidate);
            }
        }
        if (!queryTokens.isEmpty()) {
            for (SearchResult source : recentSources) {
                if (lexicalOverlapScore(source, queryTokens) > 0) {
                    merged.putIfAbsent(sourceKey(source), source);
                }
            }
        }
        for (SearchResult source : recentSources) {
            merged.putIfAbsent(sourceKey(source), source);
        }
        return new ArrayList<>(merged.values());
    }

    static DeterministicAnswerRouter.DeterministicAnswer selectDeterministicAnswer(
        String trimmedQuery,
        String retrievalQuery,
        String contextSelectionQuery,
        boolean sessionUsed
    ) {
        DeterministicAnswerRouter.DeterministicAnswer direct = DeterministicAnswerRouter.match(trimmedQuery);
        if (direct != null) {
            return direct;
        }
        if (!sessionUsed || queryTokens(trimmedQuery).size() < 2) {
            return null;
        }
        String contextualQuery = safe(contextSelectionQuery).trim();
        if (contextualQuery.isEmpty() || contextualQuery.equalsIgnoreCase(trimmedQuery)) {
            contextualQuery = safe(retrievalQuery).trim();
        }
        if (contextualQuery.isEmpty() || contextualQuery.equalsIgnoreCase(trimmedQuery)) {
            return null;
        }
        return DeterministicAnswerRouter.match(contextualQuery);
    }

    static List<SearchResult> trimWeakSessionContext(
        String contextSelectionQuery,
        boolean sessionUsed,
        List<SearchResult> contextResults,
        List<SearchResult> recentSources
    ) {
        if (!sessionUsed || contextResults == null || contextResults.isEmpty() || recentSources == null || recentSources.isEmpty()) {
            return contextResults == null ? Collections.emptyList() : contextResults;
        }
        if (QueryRouteProfile.fromQuery(contextSelectionQuery).isRouteFocused()) {
            return contextResults;
        }

        List<String> queryTokens = queryTokens(contextSelectionQuery);
        if (queryTokens.isEmpty()) {
            return contextResults;
        }

        LinkedHashSet<String> recentKeys = new LinkedHashSet<>();
        for (SearchResult source : recentSources) {
            recentKeys.add(sourceKey(source));
        }

        ArrayList<SearchResult> filtered = new ArrayList<>();
        for (SearchResult candidate : contextResults) {
            if (recentKeys.contains(sourceKey(candidate)) || lexicalOverlapScore(candidate, queryTokens) > 0) {
                filtered.add(candidate);
            }
        }

        return filtered.isEmpty() ? contextResults : filtered;
    }

    static List<SearchResult> prioritizeRecentSources(String query, List<SearchResult> recentSources) {
        if (recentSources == null || recentSources.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> queryTokens = queryTokens(query);
        ArrayList<SearchResult> prioritized = new ArrayList<>();
        if (queryTokens.isEmpty()) {
            prioritized.addAll(recentSources);
            return prioritized;
        }
        for (SearchResult source : recentSources) {
            if (lexicalOverlapScore(source, queryTokens) > 0) {
                prioritized.add(source);
            }
        }
        if (prioritized.isEmpty()) {
            boolean storageTankFollowUp = queryTokens.size() <= 2
                && queryTokens.contains("storage")
                && (queryTokens.contains("tank") || queryTokens.contains("tanks"));
            if (storageTankFollowUp) {
                for (SearchResult source : recentSources) {
                    String topicTags = safe(source.topicTags).toLowerCase(QUERY_LOCALE);
                    String text = (safe(source.title) + " " + safe(source.sectionHeading)).toLowerCase(QUERY_LOCALE);
                    if (topicTags.contains("water_distribution")
                        || text.contains("distribution")
                        || text.contains("water tower")) {
                        prioritized.add(source);
                    }
                }
                if (!prioritized.isEmpty()) {
                    return prioritized;
                }
            }
            return prioritized;
        }
        prioritized.sort(
            Comparator
                .comparingInt((SearchResult source) -> lexicalOverlapScore(source, queryTokens))
                .reversed()
        );
        return prioritized;
    }

    static String selectContextSelectionQuery(String trimmedQuery, String retrievalQuery, boolean sessionUsed) {
        if (!sessionUsed) {
            return trimmedQuery;
        }
        List<String> rawTokens = queryTokens(trimmedQuery);
        List<String> retrievalTokens = queryTokens(retrievalQuery);
        if (retrievalTokens.isEmpty()) {
            return safe(trimmedQuery).trim();
        }

        LinkedHashSet<String> merged = new LinkedHashSet<>(rawTokens);
        int extraTokenLimit = 3;
        int added = 0;
        for (String token : retrievalTokens) {
            if (CONTEXT_SELECTION_STOP_TOKENS.contains(token)) {
                continue;
            }
            if (merged.add(token)) {
                added += 1;
            }
            if (added >= extraTokenLimit) {
                break;
            }
        }

        if (merged.isEmpty()) {
            return String.join(" ", retrievalTokens).trim();
        }
        return String.join(" ", merged).trim();
    }

    private static List<SearchResult> topAbstainChunks(List<SearchResult> topChunks) {
        if (topChunks == null || topChunks.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(topChunks.subList(0, Math.min(ABSTAIN_TOP_CHUNK_LIMIT, topChunks.size())));
    }

    private static ConfidenceLabel normalizeConfidenceLabel(
        ConfidenceLabel label,
        boolean deterministic,
        boolean abstain
    ) {
        if (label != null) {
            return label;
        }
        if (abstain) {
            return ConfidenceLabel.LOW;
        }
        if (deterministic) {
            return ConfidenceLabel.HIGH;
        }
        return ConfidenceLabel.HIGH;
    }

    private static AnswerMode normalizeAnswerMode(
        AnswerMode mode,
        boolean deterministic,
        boolean abstain
    ) {
        if (abstain) {
            return AnswerMode.ABSTAIN;
        }
        if (mode != null) {
            return mode;
        }
        return deterministic ? AnswerMode.CONFIDENT : AnswerMode.CONFIDENT;
    }

    private static String buildConfidenceSystemInstruction(ConfidenceLabel label) {
        if (label == null) {
            return "";
        }
        String normalized = safe(label.name()).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty()) {
            return "";
        }
        String instruction = "The answer confidence is " + normalized + ".";
        if (label == ConfidenceLabel.LOW) {
            instruction += " If confidence is low, note the gap in the first sentence.";
        }
        return instruction;
    }

    static ConfidenceLabel confidenceLabel(
        List<SearchResult> topChunks,
        String query,
        QueryMetadataProfile metadataProfile
    ) {
        List<SearchResult> adjacent = topAbstainChunks(topChunks);
        if (adjacent.isEmpty()) {
            return ConfidenceLabel.LOW;
        }
        List<String> queryTokens = queryTokens(query);
        if (!safe(query).trim().isEmpty() && shouldAbstain(topChunks, topChunks, query, metadataProfile)) {
            return ConfidenceLabel.LOW;
        }

        int topOverlap = 0;
        int maxOverlap = 0;
        int topMetadataBonus = Integer.MIN_VALUE;
        int maxMetadataBonus = Integer.MIN_VALUE;
        int topSectionBonus = Integer.MIN_VALUE;
        int maxSectionBonus = Integer.MIN_VALUE;
        int topPreferredTopicOverlap = 0;
        int maxPreferredTopicOverlap = 0;
        boolean topHybrid = false;
        boolean anyHybrid = false;
        boolean topExplicitTopicMatch = false;
        int directSignalCount = 0;

        for (int index = 0; index < adjacent.size(); index++) {
            SearchResult result = adjacent.get(index);
            int overlap = gateLexicalOverlapScore(result, queryTokens);
            maxOverlap = Math.max(maxOverlap, overlap);
            boolean hybrid = "hybrid".equalsIgnoreCase(safe(result == null ? null : result.retrievalMode).trim());
            anyHybrid |= hybrid;

            int metadataBonus = 0;
            int sectionBonus = 0;
            int preferredTopicOverlap = 0;
            boolean explicitTopicMatch = false;
            if (metadataProfile != null && result != null) {
                metadataBonus = metadataProfile.metadataBonus(
                    result.category,
                    result.contentRole,
                    result.timeHorizon,
                    result.structureType,
                    result.topicTags
                );
                sectionBonus = metadataProfile.sectionHeadingBonus(result.sectionHeading);
                preferredTopicOverlap = metadataProfile.preferredTopicOverlapCount(result.topicTags);
                explicitTopicMatch = metadataProfile.hasExplicitTopicOverlap(result.topicTags);
            }
            maxMetadataBonus = Math.max(maxMetadataBonus, metadataBonus);
            maxSectionBonus = Math.max(maxSectionBonus, sectionBonus);
            maxPreferredTopicOverlap = Math.max(maxPreferredTopicOverlap, preferredTopicOverlap);
            if (overlap > 0 || hybrid || metadataBonus >= 8 || sectionBonus >= 10 || preferredTopicOverlap > 0) {
                directSignalCount += 1;
            }
            if (index == 0) {
                topOverlap = overlap;
                topHybrid = hybrid;
                topMetadataBonus = metadataBonus;
                topSectionBonus = sectionBonus;
                topPreferredTopicOverlap = preferredTopicOverlap;
                topExplicitTopicMatch = explicitTopicMatch;
            }
        }

        if ((topHybrid && topOverlap >= 2)
            || (topOverlap >= 3 && (anyHybrid || maxPreferredTopicOverlap > 0))
            || (topOverlap >= 2 && (topMetadataBonus >= 8 || topSectionBonus >= 10))
            || (topExplicitTopicMatch && topOverlap >= 1 && (topMetadataBonus >= 10 || topSectionBonus >= 10))
            || (topPreferredTopicOverlap >= 1 && topOverlap >= 2)) {
            return ConfidenceLabel.HIGH;
        }
        if ((topHybrid && topOverlap >= 1)
            || maxOverlap >= 1
            || directSignalCount >= 2
            || topMetadataBonus >= 6
            || maxMetadataBonus >= 10
            || topSectionBonus >= 8
            || maxSectionBonus >= 10
            || maxPreferredTopicOverlap > 0) {
            return ConfidenceLabel.MEDIUM;
        }
        return ConfidenceLabel.LOW;
    }

    static AnswerMode resolveAnswerMode(
        List<SearchResult> topChunks,
        String query,
        QueryMetadataProfile metadataProfile,
        ConfidenceLabel confidenceLabel,
        boolean safetyCritical
    ) {
        return resolveAnswerMode(
            topChunks,
            topChunks,
            query,
            metadataProfile,
            confidenceLabel,
            safetyCritical
        );
    }

    static AnswerMode resolveAnswerMode(
        List<SearchResult> selectedContext,
        List<SearchResult> topChunks,
        String query,
        QueryMetadataProfile metadataProfile,
        ConfidenceLabel confidenceLabel,
        boolean safetyCritical
    ) {
        if (safe(query).trim().isEmpty()) {
            return AnswerMode.CONFIDENT;
        }
        if (shouldRouteSafetyPoisoningToAbstain(metadataProfile, safetyCritical)) {
            return AnswerMode.ABSTAIN;
        }
        if (shouldRouteAcuteMentalHealthToUncertainFit(selectedContext, query, metadataProfile, safetyCritical)) {
            return AnswerMode.UNCERTAIN_FIT;
        }
        if (shouldAbstain(selectedContext, topChunks, query, metadataProfile)) {
            return AnswerMode.ABSTAIN;
        }
        double averageRrfStrength = averageRrfStrength(selectedContext, query, metadataProfile);
        if (averageRrfStrength < UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD) {
            return AnswerMode.UNCERTAIN_FIT;
        }
        double topVectorSimilarity = topVectorSimilarity(selectedContext, query, metadataProfile);
        if (topVectorSimilarity >= UNCERTAIN_FIT_MIN_VECTOR_SIMILARITY
            && topVectorSimilarity <= UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY) {
            return AnswerMode.UNCERTAIN_FIT;
        }
        if (safetyCritical
            && confidenceLabel != ConfidenceLabel.HIGH
            && !hasPrimaryOwnerSupport(selectedContext, query, metadataProfile)) {
            return AnswerMode.UNCERTAIN_FIT;
        }
        return AnswerMode.CONFIDENT;
    }

    private static boolean shouldRouteSafetyPoisoningToAbstain(
        QueryMetadataProfile metadataProfile,
        boolean safetyCritical
    ) {
        if (!safetyCritical || metadataProfile == null) {
            return false;
        }
        String structureType = safe(metadataProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE);
        return STRUCTURE_TYPE_SAFETY_POISONING.equals(structureType);
    }

    private static boolean shouldRouteAcuteMentalHealthToUncertainFit(
        List<SearchResult> selectedContext,
        String query,
        QueryMetadataProfile metadataProfile,
        boolean safetyCritical
    ) {
        if (!safetyCritical) {
            return false;
        }
        boolean viaProfile = metadataProfile != null
            && "acute_mental_health".equals(safe(metadataProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE));
        boolean viaEngineFallback = looksLikeAcuteMentalHealthQuery(query);
        if (!viaProfile && !viaEngineFallback) {
            return false;
        }
        return !hasAcuteMentalHealthSupport(selectedContext);
    }

    private static boolean looksLikeAcuteMentalHealthQuery(String query) {
        String normalized = safe(query).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty()) {
            return false;
        }
        return containsAny(normalized, ACUTE_MENTAL_HEALTH_QUERY_MARKERS)
            || (normalized.contains("pacing")
                && (normalized.contains("slept") || normalized.contains("sleep")));
    }

    private static boolean hasAcuteMentalHealthSupport(List<SearchResult> selectedContext) {
        for (SearchResult result : topAbstainChunks(selectedContext)) {
            if (result == null) {
                continue;
            }
            String category = safe(result.category).trim().toLowerCase(QUERY_LOCALE);
            String mode = safe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            String surface = lexicalEvidenceText(result, true);
            boolean supportMarkers = containsAny(surface, ACUTE_MENTAL_HEALTH_SUPPORT_MARKERS);
            boolean mentalHealthCategory = category.contains("mental");
            boolean medicalSafetyCategory = (category.contains("medical") || category.contains("health")) && supportMarkers;
            boolean promptAnchoredSupport = supportMarkers
                && ("guide-focus".equals(mode) || "route-focus".equals(mode) || "hybrid".equals(mode));
            if (mentalHealthCategory || medicalSafetyCategory || promptAnchoredSupport) {
                return true;
            }
        }
        return false;
    }

    private static double averageRrfStrength(
        List<SearchResult> topChunks,
        String query,
        QueryMetadataProfile metadataProfile
    ) {
        List<SearchResult> adjacent = topAbstainChunks(topChunks);
        if (adjacent.isEmpty()) {
            return 0.0d;
        }
        List<String> queryTokens = queryTokens(query);
        double total = 0.0d;
        for (int index = 0; index < adjacent.size(); index++) {
            total += candidateRrfStrength(adjacent.get(index), index, queryTokens, metadataProfile);
        }
        return total / adjacent.size();
    }

    private static double candidateRrfStrength(
        SearchResult result,
        int index,
        List<String> queryTokens,
        QueryMetadataProfile metadataProfile
    ) {
        if (result == null) {
            return 0.0d;
        }
        String mode = safe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        int overlap = gateLexicalOverlapScore(result, queryTokens);
        double strength = Math.max(0.06d, 0.22d - (Math.max(0, index) * 0.04d));
        if ("hybrid".equals(mode)) {
            strength += 0.36d;
        } else if ("guide-focus".equals(mode) || "route-focus".equals(mode)) {
            strength += 0.28d;
        } else if ("vector".equals(mode)) {
            strength += 0.20d;
        } else if ("lexical".equals(mode)) {
            strength += 0.14d;
        }
        if (overlap >= 2) {
            strength += 0.22d;
        } else if (overlap >= 1) {
            strength += 0.12d;
        }
        if (("guide-focus".equals(mode) || "route-focus".equals(mode)) && overlap >= 3) {
            strength += 0.06d;
        }
        if (metadataProfile != null) {
            if (metadataProfile.hasExplicitTopicOverlap(result.topicTags)) {
                strength += 0.14d;
            }
            if (metadataProfile.preferredTopicOverlapCount(result.topicTags) > 0) {
                strength += 0.10d;
            }
            if (metadataProfile.sectionHeadingBonus(result.sectionHeading) > 0) {
                strength += 0.08d;
            }
        }
        return Math.max(0.0d, Math.min(1.0d, strength));
    }

    private static double topVectorSimilarity(
        List<SearchResult> topChunks,
        String query,
        QueryMetadataProfile metadataProfile
    ) {
        List<SearchResult> adjacent = topAbstainChunks(topChunks);
        if (adjacent.isEmpty()) {
            return 0.0d;
        }
        SearchResult top = adjacent.get(0);
        List<String> queryTokens = queryTokens(query);
        int overlap = gateLexicalOverlapScore(top, queryTokens);
        String mode = safe(top == null ? null : top.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        double similarity;
        if ("hybrid".equals(mode)) {
            similarity = overlap >= 2 ? 0.66d : (overlap > 0 ? 0.64d : 0.62d);
        } else if ("guide-focus".equals(mode) || "route-focus".equals(mode)) {
            similarity = overlap >= 2 ? 0.64d : (overlap > 0 ? 0.60d : 0.56d);
        } else if ("vector".equals(mode)) {
            similarity = overlap >= 2 ? 0.58d : (overlap > 0 ? 0.56d : 0.52d);
        } else if ("lexical".equals(mode)) {
            similarity = overlap >= 2 ? 0.50d : (overlap > 0 ? 0.47d : 0.42d);
        } else {
            similarity = overlap >= 2 ? 0.49d : (overlap > 0 ? 0.45d : 0.0d);
        }
        if (metadataProfile != null) {
            if (metadataProfile.hasExplicitTopicOverlap(top == null ? "" : top.topicTags)) {
                similarity += 0.03d;
            }
            if (metadataProfile.sectionHeadingBonus(top == null ? "" : top.sectionHeading) > 0) {
                similarity += 0.02d;
            }
        }
        return Math.max(0.0d, Math.min(1.0d, similarity));
    }

    private static boolean hasPrimaryOwnerSupport(
        List<SearchResult> topChunks,
        String query,
        QueryMetadataProfile metadataProfile
    ) {
        List<SearchResult> adjacent = topAbstainChunks(topChunks);
        if (adjacent.isEmpty()) {
            return false;
        }
        List<String> queryTokens = queryTokens(query);
        for (SearchResult result : adjacent) {
            if (result == null) {
                continue;
            }
            int overlap = gateLexicalOverlapScore(result, queryTokens);
            String mode = safe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            int sectionBonus = metadataProfile == null ? 0 : metadataProfile.sectionHeadingBonus(result.sectionHeading);
            int preferredTopicOverlap = metadataProfile == null
                ? 0
                : metadataProfile.preferredTopicOverlapCount(result.topicTags);
            boolean explicitTopicMatch = metadataProfile != null
                && metadataProfile.hasExplicitTopicOverlap(result.topicTags);
            if ((("hybrid".equals(mode) || "guide-focus".equals(mode) || "route-focus".equals(mode)) && overlap >= 1)
                || overlap >= 2
                || sectionBonus >= 8
                || preferredTopicOverlap > 0
                || explicitTopicMatch) {
                return true;
            }
        }
        return false;
    }

    private static String abstainMatchLabel(SearchResult result, List<String> queryTokens) {
        int overlap = gateLexicalOverlapScore(result, queryTokens);
        String retrievalMode = safe(result == null ? null : result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        if (overlap >= 2) {
            return "moderate match";
        }
        if (overlap >= 1
            || "hybrid".equals(retrievalMode)
            || "guide-focus".equals(retrievalMode)
            || "route-focus".equals(retrievalMode)
            || "lexical".equals(retrievalMode)
            || "vector".equals(retrievalMode)) {
            return "low match";
        }
        return "off-topic candidate";
    }

    static boolean shouldAbstain(List<SearchResult> topChunks, String query) {
        return shouldAbstain(topChunks, topChunks, query, QueryMetadataProfile.fromQuery(query));
    }

    static boolean shouldAbstain(
        List<SearchResult> selectedContext,
        List<SearchResult> retrievalTopChunks,
        String query,
        QueryMetadataProfile metadataProfile
    ) {
        List<SearchResult> adjacent = topAbstainChunks(selectedContext);
        List<String> queryTokens = queryTokens(query);
        if (adjacent.isEmpty() || queryTokens.isEmpty()) {
            return false;
        }

        int maxOverlap = 0;
        LinkedHashSet<String> uniqueLexicalHits = new LinkedHashSet<>();
        for (SearchResult result : adjacent) {
            int overlap = gateLexicalOverlapScore(result, queryTokens);
            maxOverlap = Math.max(maxOverlap, overlap);
            collectLexicalHits(result, queryTokens, uniqueLexicalHits, true);
        }
        boolean strongSemanticHit = hasStrongSemanticHit(
            adjacent,
            retrievalTopChunks,
            query,
            queryTokens,
            metadataProfile
        );
        return maxOverlap <= ABSTAIN_MAX_OVERLAP_TOKENS
            && uniqueLexicalHits.size() < ABSTAIN_MIN_UNIQUE_LEXICAL_HITS
            && !strongSemanticHit;
    }

    static boolean isSafetyCriticalQuery(String query, List<SearchResult> topChunks) {
        String normalized = safe(query).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty()) {
            return false;
        }
        if (containsAny(normalized, SAFETY_CRITICAL_EXPLICIT_MARKERS)
            || containsAny(normalized, SAFETY_CRITICAL_MENTAL_HEALTH_MARKERS)) {
            return true;
        }
        if (containsAny(normalized, SAFETY_CRITICAL_ACUTE_QUERY_MARKERS)) {
            return true;
        }
        return containsAny(normalized, SAFETY_CRITICAL_EMERGENCY_CONTEXT_MARKERS)
            && containsAny(normalized, SAFETY_CRITICAL_ACUTE_QUERY_MARKERS);
    }

    private static String truncateAbstainQuery(String query) {
        String normalized = safe(query).trim().replaceAll("\\s+", " ");
        if (normalized.length() <= 60) {
            return normalized;
        }
        return normalized.substring(0, 57).trim() + "...";
    }

    static String buildAbstainAnswerBody(String query, List<SearchResult> topChunks) {
        return buildAbstainAnswerBody(query, topChunks, false);
    }

    static String buildAbstainAnswerBody(String query, List<SearchResult> topChunks, boolean safetyCritical) {
        List<SearchResult> adjacent = topAbstainChunks(topChunks);
        List<String> queryTokens = queryTokens(query);
        LinkedHashMap<String, Integer> categoryCounts = new LinkedHashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append("Senku doesn't have a guide for \"")
            .append(truncateAbstainQuery(query))
            .append("\".");
        String escalationLine = abstainEscalationLine(safetyCritical);
        if (!escalationLine.isEmpty()) {
            builder.append("\n\n").append(escalationLine);
        }
        if (!adjacent.isEmpty()) {
            builder.append("\n\nClosest matches in the library:");
            for (SearchResult result : adjacent) {
                String category = safe(result == null ? null : result.category).trim();
                if (category.isEmpty()) {
                    category = "unknown";
                }
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                String guideId = safe(result == null ? null : result.guideId).trim();
                if (guideId.isEmpty()) {
                    guideId = "Guide";
                }
                String title = safe(result == null ? null : result.title).trim();
                if (title.isEmpty()) {
                    title = "Unknown guide";
                }
                builder.append("\n- [")
                    .append(guideId)
                    .append("] ")
                    .append(title)
                    .append(" - ")
                    .append(category)
                    .append(" | ")
                    .append(abstainMatchLabel(result, queryTokens));
            }
        }
        String topCategory = "survival";
        int topCount = -1;
        for (java.util.Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > topCount) {
                topCategory = entry.getKey();
                topCount = entry.getValue();
            }
        }
        builder.append("\n\nTry:")
            .append("\n- rephrasing the question")
            .append("\n- browsing the ")
            .append(topCategory)
            .append(" category")
            .append("\n- asking a simpler version (for example, \"what is X?\")");
        return builder.toString().trim();
    }

    static String buildUncertainFitAnswerBody(
        String query,
        List<SearchResult> topChunks,
        ConfidenceLabel confidenceLabel
    ) {
        return buildUncertainFitAnswerBody(query, topChunks, confidenceLabel, false);
    }

    static String buildUncertainFitAnswerBody(
        String query,
        List<SearchResult> topChunks,
        ConfidenceLabel confidenceLabel,
        boolean safetyCritical
    ) {
        List<SearchResult> adjacent = topAbstainChunks(topChunks);
        List<String> queryTokens = queryTokens(query);
        StringBuilder builder = new StringBuilder();
        builder.append(
            confidenceLabel == ConfidenceLabel.LOW
                ? "Senku found only loosely related guides for \""
                : "Senku found guides that may be relevant to \""
        ).append(truncateAbstainQuery(query))
            .append(confidenceLabel == ConfidenceLabel.LOW
                ? "\", so this is not a confident fit."
                : "\", but this is not a confident fit.");
        String escalationLine = abstainEscalationLine(safetyCritical);
        if (!escalationLine.isEmpty()) {
            builder.append("\n\n").append(escalationLine);
        }
        if (!adjacent.isEmpty()) {
            builder.append("\n\nPossibly relevant guides in the library:");
            for (SearchResult result : adjacent) {
                String category = safe(result == null ? null : result.category).trim();
                if (category.isEmpty()) {
                    category = "unknown";
                }
                String guideId = safe(result == null ? null : result.guideId).trim();
                if (guideId.isEmpty()) {
                    guideId = "Guide";
                }
                String title = safe(result == null ? null : result.title).trim();
                if (title.isEmpty()) {
                    title = "Unknown guide";
                }
                builder.append("\n- [")
                    .append(guideId)
                    .append("] ")
                    .append(title)
                    .append(" - ")
                    .append(category)
                    .append(" | ")
                    .append(abstainMatchLabel(result, queryTokens));
            }
        }
        builder.append("\n\nTry:")
            .append("\n- checking whether the guide matches the exact person, symptom, tool, or setting")
            .append("\n- asking a narrower follow-up with the exact detail that is missing")
            .append("\n- treating the guides above as related context, not a final answer");
        return builder.toString().trim();
    }

    private static String abstainEscalationLine(boolean safetyCritical) {
        return safetyCritical ? SAFETY_CRITICAL_ESCALATION_LINE : "";
    }

    private static int lexicalOverlapScore(SearchResult source, List<String> queryTokens) {
        if (source == null || queryTokens.isEmpty()) {
            return 0;
        }
        String haystack = (safe(source.title) + " " + safe(source.sectionHeading)).toLowerCase(QUERY_LOCALE);
        int score = 0;
        for (String token : queryTokens) {
            if (haystack.contains(token)) {
                score += 1;
            }
        }
        return score;
    }

    private static int gateLexicalOverlapScore(SearchResult source, List<String> queryTokens) {
        if (source == null || queryTokens.isEmpty()) {
            return 0;
        }
        LinkedHashSet<String> hits = new LinkedHashSet<>();
        collectLexicalHits(source, queryTokens, hits, true);
        return hits.size();
    }

    private static void collectLexicalHits(
        SearchResult source,
        List<String> queryTokens,
        Set<String> destination,
        boolean includePromptSurface
    ) {
        if (source == null || queryTokens.isEmpty() || destination == null) {
            return;
        }
        String haystack = lexicalEvidenceText(source, includePromptSurface);
        if (haystack.isEmpty()) {
            return;
        }
        for (String token : queryTokens) {
            if (haystack.contains(token)) {
                destination.add(token);
            }
        }
    }

    private static String lexicalEvidenceText(SearchResult source, boolean includePromptSurface) {
        if (source == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(safe(source.title)).append(' ')
            .append(safe(source.sectionHeading)).append(' ');
        if (includePromptSurface) {
            builder.append(safe(source.subtitle)).append(' ')
                .append(safe(source.snippet)).append(' ')
                .append(safe(source.body)).append(' ');
        }
        return builder.toString().toLowerCase(QUERY_LOCALE);
    }

    private static boolean hasStrongSemanticHit(
        List<SearchResult> selectedContext,
        List<SearchResult> retrievalTopChunks,
        String query,
        List<String> queryTokens,
        QueryMetadataProfile metadataProfile
    ) {
        boolean routeFocused = QueryRouteProfile.fromQuery(query).isRouteFocused();
        for (SearchResult result : selectedContext) {
            if (result == null) {
                continue;
            }
            int overlap = gateLexicalOverlapScore(result, queryTokens);
            int sectionBonus = metadataProfile == null ? 0 : metadataProfile.sectionHeadingBonus(result.sectionHeading);
            int preferredTopicOverlap = metadataProfile == null
                ? 0
                : metadataProfile.preferredTopicOverlapCount(result.topicTags);
            boolean explicitTopicMatch = metadataProfile != null
                && metadataProfile.hasExplicitTopicOverlap(result.topicTags);
            String mode = safe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            boolean selectedGuideSupport = overlap >= ABSTAIN_MIN_UNIQUE_LEXICAL_HITS
                || explicitTopicMatch
                || preferredTopicOverlap > 0
                || sectionBonus >= 8
                || (routeFocused
                    && ("hybrid".equals(mode)
                        || "guide-focus".equals(mode)
                        || "route-focus".equals(mode))
                    && overlap >= 1);
            if (selectedGuideSupport) {
                return true;
            }
        }
        if (retrievalTopChunks == null || retrievalTopChunks.isEmpty()) {
            return false;
        }
        if (topVectorSimilarity(retrievalTopChunks, query, metadataProfile) <= UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY) {
            return false;
        }
        return rawTopChunkSupportsSelectedAnchor(retrievalTopChunks.get(0), selectedContext, queryTokens);
    }

    private static boolean rawTopChunkSupportsSelectedAnchor(
        SearchResult rawTopChunk,
        List<SearchResult> selectedContext,
        List<String> queryTokens
    ) {
        if (rawTopChunk == null || selectedContext == null || selectedContext.isEmpty()) {
            return false;
        }
        String rawGuideKey = guideKey(rawTopChunk);
        if (rawGuideKey.isEmpty() || lexicalOverlapScore(rawTopChunk, queryTokens) <= ABSTAIN_MAX_OVERLAP_TOKENS) {
            return false;
        }
        for (SearchResult selectedChunk : selectedContext) {
            if (rawGuideKey.equals(guideKey(selectedChunk))
                && gateLexicalOverlapScore(selectedChunk, queryTokens) > ABSTAIN_MAX_OVERLAP_TOKENS) {
                return true;
            }
        }
        return false;
    }

    private static List<String> queryTokens(String query) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        String[] split = safe(query).toLowerCase(QUERY_LOCALE).split("[^a-z0-9-]+");
        for (String token : split) {
            if (token.length() < 3 || QUERY_STOP_TOKENS.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return new ArrayList<>(tokens);
    }

    private static String sourceKey(SearchResult result) {
        if (result == null) {
            return "unknown";
        }
        String guideId = safe(result.guideId).trim().toLowerCase(QUERY_LOCALE);
        String sectionHeading = safe(result.sectionHeading).trim().toLowerCase(QUERY_LOCALE);
        String title = safe(result.title).trim().toLowerCase(QUERY_LOCALE);
        if (!guideId.isEmpty() || !sectionHeading.isEmpty()) {
            return guideId + "::" + sectionHeading;
        }
        return title;
    }

    private static String guideKey(SearchResult result) {
        if (result == null) {
            return "unknown";
        }
        String guideId = safe(result.guideId).trim().toLowerCase(QUERY_LOCALE);
        if (!guideId.isEmpty()) {
            return guideId;
        }
        return sourceKey(result);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    static LatencyBreakdown consumePendingSessionLatencyBreakdown(
        String query,
        String answerBody,
        List<SearchResult> sources,
        String ruleId
    ) {
        synchronized (PENDING_SESSION_LATENCIES_LOCK) {
            return PENDING_SESSION_LATENCIES.remove(
                sessionLatencyKey(query, answerBody, sources, ruleId)
            );
        }
    }

    static void setDebugLogSinkForTest(BiConsumer<String, String> sink) {
        debugLogSink = sink == null ? DEFAULT_DEBUG_LOG_SINK : sink;
    }

    static void resetDebugLogSinkForTest() {
        debugLogSink = DEFAULT_DEBUG_LOG_SINK;
    }

    private static void rememberSessionLatencyBreakdown(AnswerRun answerRun) {
        if (answerRun == null || answerRun.latencyBreakdown == null) {
            return;
        }
        synchronized (PENDING_SESSION_LATENCIES_LOCK) {
            PENDING_SESSION_LATENCIES.put(
                sessionLatencyKey(answerRun.query, answerRun.answerBody, answerRun.sources, answerRun.ruleId),
                answerRun.latencyBreakdown
            );
        }
    }

    private static String sessionLatencyKey(
        String query,
        String answerBody,
        List<SearchResult> sources,
        String ruleId
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(safe(query).trim()).append('\n');
        builder.append(safe(ruleId).trim()).append('\n');
        builder.append(Integer.toHexString(safe(answerBody).trim().hashCode())).append('\n');
        if (sources != null) {
            for (SearchResult source : sources) {
                builder.append(safe(source == null ? null : source.guideId).trim())
                    .append('|')
                    .append(safe(source == null ? null : source.sectionHeading).trim())
                    .append('|')
                    .append(safe(source == null ? null : source.retrievalMode).trim())
                    .append('\n');
            }
        }
        return builder.toString();
    }

    private static void logWarn(String tag, String message, Throwable throwable) {
        try {
            if (throwable == null) {
                Log.w(tag, message);
            } else {
                Log.w(tag, message, throwable);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private static void logDebug(String tag, String message) {
        try {
            debugLogSink.accept(tag, message);
        } catch (RuntimeException ignored) {
        }
    }

    private static void logInfo(String tag, String message) {
        try {
            Log.i(tag, message);
        } catch (RuntimeException ignored) {
        }
    }

    private static void logAskFinalMode(String query, AnswerMode finalMode, String route, long totalElapsedMs) {
        String message =
            "ask.generate final_mode=" + finalMode.name().toLowerCase(QUERY_LOCALE) +
                " route=" + route +
                " query=\"" + safe(query) + "\"" +
                " totalElapsedMs=" + Math.max(0L, totalElapsedMs);
        logDebug(TAG, message);
        if (debugLogSink == DEFAULT_DEBUG_LOG_SINK) {
            logInfo(TAG, message);
        }
    }

    static void logPreparedFinalModeIfReady(PreparedAnswer prepared) {
        if (prepared == null || prepared.finalModeEmitted) {
            return;
        }
        AnswerMode finalMode = null;
        String route = "";
        if (prepared.deterministic) {
            finalMode = AnswerMode.CONFIDENT;
            route = "deterministic";
        } else if (prepared.abstain) {
            finalMode = AnswerMode.ABSTAIN;
            route = "early_abstain";
        } else if (prepared.mode == AnswerMode.UNCERTAIN_FIT) {
            finalMode = AnswerMode.UNCERTAIN_FIT;
            route = "early_uncertain_fit";
        }
        if (finalMode == null || route.isEmpty()) {
            return;
        }
        long totalElapsedMs = Math.max(0L, System.currentTimeMillis() - prepared.startedAtMs);
        logAskFinalMode(prepared.query, finalMode, route, totalElapsedMs);
        prepared.finalModeEmitted = true;
    }

    private static void logToAndroidDebug(String tag, String message) {
        Log.d(tag, message);
    }

    private static long nanosToMillis(long elapsedNs) {
        return Math.max(0L, elapsedNs) / 1_000_000L;
    }

    private static long elapsedMsSince(long startedAtNs) {
        return nanosToMillis(SystemClock.elapsedRealtimeNanos() - startedAtNs);
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }

    public static String buildRetrievalStatus(String query, SessionMemory sessionMemory) {
        String trimmedQuery = query == null ? "" : query.trim();
        if (sessionMemory == null) {
            return "Finding guide evidence...";
        }
        boolean sessionUsed = sessionMemory.buildRetrievalPlan(trimmedQuery).sessionUsed;
        return sessionUsed
            ? "Finding guide evidence + thread memory..."
            : "Finding guide evidence...";
    }

    public static String buildGeneratingStatus(Context context) {
        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        return buildGeneratingStatus(context, 0, settings.enabled);
    }

    public static String buildGeneratingStatus(Context context, int sourceCount, boolean hostBackendEnabled) {
        if (sourceCount > 0) {
            return buildSourcesReadyStatus(context, sourceCount, hostBackendEnabled);
        }
        if (context == null) {
            return hostBackendEnabled ? "Building answer on host GPU..." : "Building answer on this device...";
        }
        return context.getString(hostBackendEnabled ? R.string.status_generating_host : R.string.status_generating_local);
    }

    public static String buildHostFallbackGeneratingStatus(Context context) {
        return buildHostFallbackGeneratingStatus(context, 0);
    }

    public static String buildHostFallbackGeneratingStatus(Context context, int sourceCount) {
        if (sourceCount > 0 && context != null) {
            int safeCount = Math.max(1, sourceCount);
            return context.getResources().getQuantityString(
                R.plurals.status_generating_host_fallback_with_sources,
                safeCount,
                safeCount
            );
        }
        if (context == null) {
            return "Host unavailable. Continuing on this device...";
        }
        return context.getString(R.string.status_generating_host_fallback);
    }

    public static String buildSourcesReadyStatus(Context context, int sourceCount) {
        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        return buildSourcesReadyStatus(context, sourceCount, settings.enabled);
    }

    public static String buildSourcesReadyStatus(Context context, int sourceCount, boolean hostBackendEnabled) {
        int safeCount = Math.max(1, sourceCount);
        if (context != null) {
            return context.getResources().getQuantityString(
                R.plurals.answer_sources_ready_status_with_backend,
                safeCount,
                safeCount,
                backendStatusLabel(context, hostBackendEnabled)
            );
        }
        return "Sources ready on " + backendStatusLabel(null, hostBackendEnabled) +
            ". Building answer from " + safeCount + (safeCount == 1 ? " guide..." : " guides...");
    }

    public static String buildStillBuildingStatus(Context context, int sourceCount, boolean hostBackendEnabled) {
        if (sourceCount > 0 && context != null) {
            int safeCount = Math.max(1, sourceCount);
            return context.getResources().getQuantityString(
                R.plurals.detail_status_still_building_with_sources,
                safeCount,
                safeCount,
                backendStatusLabel(context, hostBackendEnabled)
            );
        }
        if (context == null) {
            return hostBackendEnabled
                ? "Still building on host GPU. You can inspect sources now."
                : "Still building on this device. You can inspect sources now.";
        }
        return context.getString(
            hostBackendEnabled
                ? R.string.detail_status_still_building_host
                : R.string.detail_status_still_building_local
        );
    }

    public static String buildCompletionStatus(AnswerRun answerRun) {
        return buildCompletionStatus(null, answerRun);
    }

    public static String buildCompletionStatus(Context context, AnswerRun answerRun) {
        if (answerRun == null) {
            return "Offline answer ready.";
        }
        if (answerRun.deterministic) {
            return "Deterministic answer ready.";
        }
        if (answerRun.abstain) {
            return "No guide match. Try rephrasing.";
        }
        if (answerRun.mode == AnswerMode.UNCERTAIN_FIT) {
            return "Related guides ready. Verify the fit.";
        }
        int sourceCount = answerRun.sources == null ? 0 : answerRun.sources.size();
        String duration = PromptBuilder.formatDuration(answerRun.elapsedMs);
        if (answerRun.hostFallbackUsed) {
            if (context != null && sourceCount > 0) {
                return context.getResources().getQuantityString(
                    R.plurals.status_answer_ready_host_fallback_with_sources,
                    sourceCount,
                    sourceCount,
                    duration
                );
            }
            if (context == null) {
                return "Answer ready on this device after host fallback in " + duration + ".";
            }
            return context.getString(R.string.status_answer_ready_host_fallback, duration);
        }
        if (context != null && sourceCount > 0) {
            return context.getResources().getQuantityString(
                R.plurals.status_answer_ready_timed_with_backend_and_sources,
                sourceCount,
                sourceCount,
                backendStatusLabel(context, answerRun.hostBackendUsed),
                duration
            );
        }
        return "Offline answer ready in " + duration + ".";
    }

    private static String backendStatusLabel(Context context, boolean hostBackendEnabled) {
        if (context == null) {
            return hostBackendEnabled ? "host GPU" : "this device";
        }
        return context.getString(
            hostBackendEnabled ? R.string.status_backend_host_short : R.string.status_backend_local_short
        );
    }

    static Integer modelMaxTokensForTest() {
        return MODEL_MAX_TOKENS;
    }

    static void setGeneratorsForTest(HostGenerator host, OnDeviceGenerator onDevice) {
        hostGenerator = host == null ? DEFAULT_HOST_GENERATOR : host;
        onDeviceGenerator = onDevice == null ? DEFAULT_ON_DEVICE_GENERATOR : onDevice;
    }

    static void resetGeneratorsForTest() {
        hostGenerator = DEFAULT_HOST_GENERATOR;
        onDeviceGenerator = DEFAULT_ON_DEVICE_GENERATOR;
        synchronized (PENDING_SESSION_LATENCIES_LOCK) {
            PENDING_SESSION_LATENCIES.clear();
        }
    }

    static void rememberSessionLatencyBreakdownForTest(AnswerRun answerRun) {
        rememberSessionLatencyBreakdown(answerRun);
    }

    private static AnswerRun buildLowCoverageDowngradeAnswerRun(
        File modelFile,
        PreparedAnswer prepared,
        long elapsedMs,
        String subtitle,
        LatencyBreakdown latencyBreakdown,
        boolean hostBackendUsed,
        boolean hostFallbackUsed
    ) {
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(prepared.query);
        boolean abstainShape = shouldAbstain(prepared.sources, prepared.sources, prepared.query, metadataProfile);
        ConfidenceLabel downgradedConfidence = ConfidenceLabel.LOW;
        String downgradedBody = abstainShape
            ? buildAbstainAnswerBody(prepared.query, prepared.sources, prepared.safetyCritical)
            : buildUncertainFitAnswerBody(prepared.query, prepared.sources, downgradedConfidence, prepared.safetyCritical);
        AnswerMode downgradedMode = abstainShape ? AnswerMode.ABSTAIN : AnswerMode.UNCERTAIN_FIT;
        logDebug(
            TAG,
            "ask.generate low_coverage_route query=\"" + prepared.query + "\" mode=" +
                downgradedMode.name().toLowerCase(QUERY_LOCALE)
        );
        return new AnswerRun(
            prepared.query,
            downgradedBody,
            prepared.sources,
            elapsedMs,
            prepared.sessionUsed,
            false,
            abstainShape,
            subtitle,
            null,
            latencyBreakdown,
            downgradedConfidence,
            downgradedMode,
            hostBackendUsed,
            hostFallbackUsed
        );
    }

    private static String buildSubtitle(
        File modelFile,
        long elapsedMs,
        HostInferenceConfig.Settings settings,
        String hostBackend,
        boolean hostFallbackUsed
    ) {
        if (settings.enabled) {
            if (hostFallbackUsed) {
                return "Offline answer | on-device fallback | " + modelFile.getName() +
                    " | " + LiteRtModelRunner.getLoadedBackendLabel() +
                    " | " + PromptBuilder.formatDuration(elapsedMs);
            }
            String backendLabel = safe(hostBackend).trim();
            if (backendLabel.isEmpty()) {
                backendLabel = "host";
            }
            return "Host answer | " + settings.modelId + " @ " + settings.serverLabel() +
                " | " + backendLabel + " | " + PromptBuilder.formatDuration(elapsedMs);
        }
        return "Offline answer | " + modelFile.getName() + " | " + LiteRtModelRunner.getLoadedBackendLabel() +
            " | " + PromptBuilder.formatDuration(elapsedMs);
    }

    private static String buildLowCoverageSubtitle(
        File modelFile,
        long elapsedMs,
        HostInferenceConfig.Settings settings,
        String hostBackend,
        boolean hostFallbackUsed
    ) {
        if (settings.enabled) {
            if (hostFallbackUsed) {
                return "Low coverage | on-device fallback | " + modelFile.getName() +
                    " | " + LiteRtModelRunner.getLoadedBackendLabel() +
                    " | " + PromptBuilder.formatDuration(elapsedMs);
            }
            String backendLabel = safe(hostBackend).trim();
            if (backendLabel.isEmpty()) {
                backendLabel = "host";
            }
            return "Low coverage | " + settings.modelId + " @ " + settings.serverLabel() +
                " | " + backendLabel + " | " + PromptBuilder.formatDuration(elapsedMs);
        }
        return "Low coverage | " + modelFile.getName() + " | " + LiteRtModelRunner.getLoadedBackendLabel() +
            " | " + PromptBuilder.formatDuration(elapsedMs);
    }

    public static final class AnswerRun {
        public final String query;
        public final String answerBody;
        public final List<SearchResult> sources;
        public final long elapsedMs;
        public final boolean sessionUsed;
        public final boolean deterministic;
        public final boolean abstain;
        public final AnswerMode mode;
        public final String subtitle;
        public final String ruleId;
        public final LatencyBreakdown latencyBreakdown;
        public final ConfidenceLabel confidenceLabel;
        public final boolean hostBackendUsed;
        public final boolean hostFallbackUsed;

        AnswerRun(
            String query,
            String answerBody,
            List<SearchResult> sources,
            long elapsedMs,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            String subtitle,
            String ruleId,
            boolean hostBackendUsed,
            boolean hostFallbackUsed
        ) {
            this(
                query,
                answerBody,
                sources,
                elapsedMs,
                sessionUsed,
                deterministic,
                abstain,
                subtitle,
                ruleId,
                null,
                null,
                null,
                hostBackendUsed,
                hostFallbackUsed
            );
        }

        AnswerRun(
            String query,
            String answerBody,
            List<SearchResult> sources,
            long elapsedMs,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            String subtitle,
            String ruleId
        ) {
            this(
                query,
                answerBody,
                sources,
                elapsedMs,
                sessionUsed,
                deterministic,
                abstain,
                subtitle,
                ruleId,
                null,
                null,
                null,
                false,
                false
            );
        }

        AnswerRun(
            String query,
            String answerBody,
            List<SearchResult> sources,
            long elapsedMs,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            String subtitle,
            String ruleId,
            LatencyBreakdown latencyBreakdown,
            ConfidenceLabel confidenceLabel
        ) {
            this(
                query,
                answerBody,
                sources,
                elapsedMs,
                sessionUsed,
                deterministic,
                abstain,
                subtitle,
                ruleId,
                latencyBreakdown,
                confidenceLabel,
                null,
                false,
                false
            );
        }

        AnswerRun(
            String query,
            String answerBody,
            List<SearchResult> sources,
            long elapsedMs,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            String subtitle,
            String ruleId,
            LatencyBreakdown latencyBreakdown,
            ConfidenceLabel confidenceLabel,
            AnswerMode mode
        ) {
            this(
                query,
                answerBody,
                sources,
                elapsedMs,
                sessionUsed,
                deterministic,
                abstain,
                subtitle,
                ruleId,
                latencyBreakdown,
                confidenceLabel,
                mode,
                false,
                false
            );
        }

        AnswerRun(
            String query,
            String answerBody,
            List<SearchResult> sources,
            long elapsedMs,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            String subtitle,
            String ruleId,
            LatencyBreakdown latencyBreakdown,
            ConfidenceLabel confidenceLabel,
            boolean hostBackendUsed,
            boolean hostFallbackUsed
        ) {
            this(
                query,
                answerBody,
                sources,
                elapsedMs,
                sessionUsed,
                deterministic,
                abstain,
                subtitle,
                ruleId,
                latencyBreakdown,
                confidenceLabel,
                null,
                hostBackendUsed,
                hostFallbackUsed
            );
        }

        AnswerRun(
            String query,
            String answerBody,
            List<SearchResult> sources,
            long elapsedMs,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            String subtitle,
            String ruleId,
            LatencyBreakdown latencyBreakdown
        ) {
            this(
                query,
                answerBody,
                sources,
                elapsedMs,
                sessionUsed,
                deterministic,
                abstain,
                subtitle,
                ruleId,
                latencyBreakdown,
                null,
                null,
                false,
                false
            );
        }

        AnswerRun(
            String query,
            String answerBody,
            List<SearchResult> sources,
            long elapsedMs,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            String subtitle,
            String ruleId,
            LatencyBreakdown latencyBreakdown,
            ConfidenceLabel confidenceLabel,
            AnswerMode mode,
            boolean hostBackendUsed,
            boolean hostFallbackUsed
        ) {
            this.query = query == null ? "" : query;
            this.answerBody = answerBody == null ? "" : answerBody;
            this.sources = sources == null ? Collections.emptyList() : new ArrayList<>(sources);
            this.elapsedMs = elapsedMs;
            this.sessionUsed = sessionUsed;
            this.deterministic = deterministic;
            this.abstain = abstain;
            this.mode = normalizeAnswerMode(mode, deterministic, abstain);
            this.subtitle = subtitle == null ? "" : subtitle;
            this.ruleId = ruleId == null ? "" : ruleId;
            this.latencyBreakdown = latencyBreakdown;
            this.confidenceLabel = normalizeConfidenceLabel(confidenceLabel, deterministic, abstain);
            this.hostBackendUsed = hostBackendUsed;
            this.hostFallbackUsed = hostFallbackUsed;
        }
    }

    public static final class LatencyBreakdown {
        public final String queryClass;
        public final long retrievalMs;
        public final long rerankMs;
        public final long promptBuildMs;
        public final long firstTokenMs;
        public final long decodeMs;
        public final long totalMs;

        LatencyBreakdown(
            String queryClass,
            long retrievalMs,
            long rerankMs,
            long promptBuildMs,
            long firstTokenMs,
            long decodeMs,
            long totalMs
        ) {
            this.queryClass = safe(queryClass).trim();
            this.retrievalMs = Math.max(0L, retrievalMs);
            this.rerankMs = Math.max(0L, rerankMs);
            this.promptBuildMs = Math.max(0L, promptBuildMs);
            this.firstTokenMs = Math.max(0L, firstTokenMs);
            this.decodeMs = Math.max(0L, decodeMs);
            this.totalMs = Math.max(0L, totalMs);
        }
    }

    public static final class PreparedAnswer {
        public final String query;
        public final List<SearchResult> sources;
        public final boolean sessionUsed;
        public final boolean deterministic;
        public final boolean abstain;
        public final AnswerMode mode;
        public final String answerBody;
        public final String ruleId;
        public final long startedAtMs;
        public final HostInferenceConfig.Settings inferenceSettings;
        public final String systemPrompt;
        public final String prompt;
        public final long retrievalMs;
        public final long rerankMs;
        public final long promptMs;
        public final String queryClass;
        public final ConfidenceLabel confidenceLabel;
        public final boolean safetyCritical;
        boolean finalModeEmitted;

        private PreparedAnswer(
            String query,
            List<SearchResult> sources,
            boolean sessionUsed,
            boolean deterministic,
            boolean abstain,
            AnswerMode mode,
            String answerBody,
            String ruleId,
            long startedAtMs,
            HostInferenceConfig.Settings inferenceSettings,
            String systemPrompt,
            String prompt,
            long retrievalMs,
            long rerankMs,
            long promptMs,
            String queryClass,
            ConfidenceLabel confidenceLabel,
            boolean safetyCritical
        ) {
            this.query = query == null ? "" : query;
            this.sources = sources == null ? Collections.emptyList() : new ArrayList<>(sources);
            this.sessionUsed = sessionUsed;
            this.deterministic = deterministic;
            this.abstain = abstain;
            this.mode = normalizeAnswerMode(mode, deterministic, abstain);
            this.answerBody = answerBody == null ? "" : answerBody;
            this.ruleId = ruleId == null ? "" : ruleId;
            this.startedAtMs = startedAtMs;
            this.inferenceSettings = inferenceSettings;
            this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
            this.prompt = prompt == null ? "" : prompt;
            this.retrievalMs = Math.max(0L, retrievalMs);
            this.rerankMs = Math.max(0L, rerankMs);
            this.promptMs = Math.max(0L, promptMs);
            this.queryClass = queryClass == null ? "" : queryClass;
            this.confidenceLabel = normalizeConfidenceLabel(confidenceLabel, deterministic, abstain);
            this.safetyCritical = safetyCritical;
        }

        private static PreparedAnswer deterministic(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            String ruleId
        ) {
            return deterministic(query, answerBody, sources, sessionUsed, ruleId, System.currentTimeMillis());
        }

        static PreparedAnswer restoredDeterministic(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            String ruleId,
            long startedAtMs
        ) {
            return deterministic(
                query,
                answerBody,
                sources,
                sessionUsed,
                ruleId,
                startedAtMs > 0L ? startedAtMs : System.currentTimeMillis()
            );
        }

        private static PreparedAnswer deterministic(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            String ruleId,
            long startedAtMs
        ) {
            return new PreparedAnswer(
                query,
                sources,
                sessionUsed,
                true,
                false,
                AnswerMode.CONFIDENT,
                answerBody,
                ruleId,
                startedAtMs,
                null,
                "",
                "",
                0L,
                0L,
                0L,
                LatencyPanel.QUERY_CLASS_DETERMINISTIC,
                ConfidenceLabel.HIGH,
                isSafetyCriticalQuery(query, sources)
            );
        }

        private static PreparedAnswer generative(
            String query,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            HostInferenceConfig.Settings inferenceSettings,
            String systemPrompt,
            String prompt,
            long retrievalMs,
            long rerankMs,
            long promptMs,
            String queryClass,
            ConfidenceLabel confidenceLabel
        ) {
            return generative(
                query,
                sources,
                sessionUsed,
                startedAtMs,
                inferenceSettings,
                systemPrompt,
                prompt,
                retrievalMs,
                rerankMs,
                promptMs,
                queryClass,
                confidenceLabel,
                isSafetyCriticalQuery(query, sources)
            );
        }

        private static PreparedAnswer generative(
            String query,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            HostInferenceConfig.Settings inferenceSettings,
            String systemPrompt,
            String prompt,
            long retrievalMs,
            long rerankMs,
            long promptMs,
            String queryClass,
            ConfidenceLabel confidenceLabel,
            boolean safetyCritical
        ) {
            return new PreparedAnswer(
                query,
                sources,
                sessionUsed,
                false,
                false,
                AnswerMode.CONFIDENT,
                "",
                "",
                startedAtMs,
                inferenceSettings,
                systemPrompt,
                prompt,
                retrievalMs,
                rerankMs,
                promptMs,
                queryClass,
                confidenceLabel,
                safetyCritical
            );
        }

        static PreparedAnswer abstain(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed
        ) {
            return abstain(
                query,
                answerBody,
                sources,
                sessionUsed,
                System.currentTimeMillis(),
                0L,
                0L,
                0L,
                ConfidenceLabel.LOW
            );
        }

        static PreparedAnswer abstain(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            long retrievalMs,
            long promptMs
        ) {
            return abstain(
                query,
                answerBody,
                sources,
                sessionUsed,
                startedAtMs,
                retrievalMs,
                0L,
                promptMs,
                ConfidenceLabel.LOW
            );
        }

        static PreparedAnswer abstain(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            long retrievalMs,
            long rerankMs,
            long promptMs
        ) {
            return abstain(
                query,
                answerBody,
                sources,
                sessionUsed,
                startedAtMs,
                retrievalMs,
                rerankMs,
                promptMs,
                ConfidenceLabel.LOW
            );
        }

        static PreparedAnswer abstain(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            long retrievalMs,
            long rerankMs,
            long promptMs,
            ConfidenceLabel confidenceLabel
        ) {
            return abstain(
                query,
                answerBody,
                sources,
                sessionUsed,
                startedAtMs,
                retrievalMs,
                rerankMs,
                promptMs,
                confidenceLabel,
                isSafetyCriticalQuery(query, sources)
            );
        }

        static PreparedAnswer abstain(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            long retrievalMs,
            long rerankMs,
            long promptMs,
            ConfidenceLabel confidenceLabel,
            boolean safetyCritical
        ) {
            return new PreparedAnswer(
                query,
                sources,
                sessionUsed,
                false,
                true,
                AnswerMode.ABSTAIN,
                answerBody,
                "",
                startedAtMs,
                null,
                "",
                "",
                retrievalMs,
                rerankMs,
                promptMs,
                LatencyPanel.QUERY_CLASS_ABSTAIN,
                confidenceLabel,
                safetyCritical
            );
        }

        static PreparedAnswer uncertainFit(
            String query,
            String answerBody,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            long retrievalMs,
            long rerankMs,
            long promptMs,
            ConfidenceLabel confidenceLabel,
            boolean safetyCritical
        ) {
            return new PreparedAnswer(
                query,
                sources,
                sessionUsed,
                false,
                false,
                AnswerMode.UNCERTAIN_FIT,
                answerBody,
                "",
                startedAtMs,
                null,
                "",
                "",
                retrievalMs,
                rerankMs,
                promptMs,
                LatencyPanel.classifyQuery(query, sources, false, false),
                confidenceLabel,
                safetyCritical
            );
        }

        static PreparedAnswer restoredGenerative(
            String query,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            boolean hostEnabled,
            String hostBaseUrl,
            String hostModelId,
            String systemPrompt,
            String prompt
        ) {
            return restoredGenerative(
                query,
                sources,
                sessionUsed,
                startedAtMs,
                hostEnabled,
                hostBaseUrl,
                hostModelId,
                systemPrompt,
                prompt,
                null
            );
        }

        static PreparedAnswer restoredGenerative(
            String query,
            List<SearchResult> sources,
            boolean sessionUsed,
            long startedAtMs,
            boolean hostEnabled,
            String hostBaseUrl,
            String hostModelId,
            String systemPrompt,
            String prompt,
            ConfidenceLabel confidenceLabel
        ) {
            HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
                hostEnabled,
                hostEnabled ? HostInferenceConfig.normalizeBaseUrl(hostBaseUrl) : "",
                hostEnabled ? HostInferenceConfig.normalizeModelId(hostModelId) : ""
            );
            return generative(
                query,
                sources,
                sessionUsed,
                startedAtMs > 0L ? startedAtMs : System.currentTimeMillis(),
                settings,
                systemPrompt,
                prompt,
                0L,
                0L,
                0L,
                LatencyPanel.classifyQuery(query, sources, false, false),
                confidenceLabel
            );
        }
    }

    private static final class ScoredCandidate {
        final SearchResult result;
        final int originalIndex;
        final int score;

        ScoredCandidate(SearchResult result, int originalIndex, int score) {
            this.result = result;
            this.originalIndex = originalIndex;
            this.score = score;
        }
    }
}

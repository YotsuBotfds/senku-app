package com.senku.mobile.telemetry;

import android.util.Log;

import com.senku.mobile.QueryMetadataProfile;
import com.senku.mobile.SearchResult;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;

public final class LatencyPanel {
    public static final String TAG = "SenkuLatency";
    public static final String QUERY_CLASS_PRACTICAL_HOW_TO = "practical how-to";
    public static final String QUERY_CLASS_MEDICAL_CROSS_GUIDE = "medical cross-guide";
    public static final String QUERY_CLASS_DETERMINISTIC = "deterministic";
    public static final String QUERY_CLASS_ABSTAIN = "abstain";
    public static final String QUERY_CLASS_ACUTE_GENERATIVE = "acute generative";

    private static final Set<String> ACUTE_MARKERS = buildSet(
        "right now", "immediately", "urgent", "emergency", "first aid",
        "not breathing", "can't breathe", "cannot breathe", "bleeding", "hemorrhage",
        "overdose", "poison", "poisoning", "seizure", "unconscious", "stroke",
        "heart attack", "chest pain", "anaphylaxis", "allergic reaction",
        "eye injury", "animal bite", "rabies", "severe burn", "puncture wound"
    );
    private static final Set<String> MEDICAL_MARKERS = buildSet(
        "medical", "medicine", "first aid", "wound", "infection", "fever",
        "diarrhea", "vomiting", "bleeding", "burn", "bite", "injury",
        "fracture", "sprain", "pain", "sanitation", "disease", "poison"
    );

    private static BiConsumer<String, String> logSink = LatencyPanel::logToAndroid;

    private LatencyPanel() {
    }

    public static void emit(String queryClass, com.senku.mobile.OfflineAnswerEngine.LatencyBreakdown latencyBreakdown) {
        if (latencyBreakdown == null) {
            emit(queryClass, 0L, 0L, 0L, 0L, 0L, 0L);
            return;
        }
        emit(
            queryClass,
            latencyBreakdown.retrievalMs,
            latencyBreakdown.rerankMs,
            latencyBreakdown.promptBuildMs,
            latencyBreakdown.firstTokenMs,
            latencyBreakdown.decodeMs,
            latencyBreakdown.totalMs
        );
    }

    public static void emit(String queryClass, long retrievalMs, long promptMs, long firstTokenMs, long generationMs, long totalMs) {
        emit(queryClass, retrievalMs, 0L, promptMs, firstTokenMs, generationMs, totalMs);
    }

    public static void emit(
        String queryClass,
        long retrievalMs,
        long rerankMs,
        long promptBuildMs,
        long firstTokenMs,
        long decodeMs,
        long totalMs
    ) {
        try {
            logSink.accept(
                TAG,
                buildEventJson(
                    queryClass,
                    retrievalMs,
                    rerankMs,
                    promptBuildMs,
                    firstTokenMs,
                    decodeMs,
                    totalMs
                )
            );
        } catch (RuntimeException ignored) {
        }
    }

    public static String classifyQuery(String query, List<SearchResult> sources, boolean deterministic, boolean abstain) {
        if (deterministic) {
            return QUERY_CLASS_DETERMINISTIC;
        }
        if (abstain) {
            return QUERY_CLASS_ABSTAIN;
        }
        String normalizedQuery = normalize(query);
        List<SearchResult> safeSources = sources == null ? Collections.emptyList() : sources;
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(normalizedQuery);
        String structureType = normalize(metadataProfile.preferredStructureType());

        boolean acute = containsAny(normalizedQuery, ACUTE_MARKERS)
            || hasValue(safeSources, source -> "immediate".equals(normalize(source.timeHorizon)));
        if (acute) {
            return QUERY_CLASS_ACUTE_GENERATIVE;
        }

        boolean medical = containsAny(normalizedQuery, MEDICAL_MARKERS)
            || "wound_care".equals(structureType)
            || "sanitation_system".equals(structureType)
            || hasValue(safeSources, source -> "medical".equals(normalize(source.category)));
        if (medical) {
            return QUERY_CLASS_MEDICAL_CROSS_GUIDE;
        }

        return QUERY_CLASS_PRACTICAL_HOW_TO;
    }

    static String buildEventJson(
        String queryClass,
        long retrievalMs,
        long promptMs,
        long firstTokenMs,
        long generationMs,
        long totalMs
    ) {
        return buildEventJson(queryClass, retrievalMs, 0L, promptMs, firstTokenMs, generationMs, totalMs);
    }

    static String buildEventJson(
        String queryClass,
        long retrievalMs,
        long rerankMs,
        long promptBuildMs,
        long firstTokenMs,
        long decodeMs,
        long totalMs
    ) {
        return "{"
            + "\"queryClass\":\"" + escapeJson(safeQueryClass(queryClass)) + "\","
            + "\"retrievalMs\":" + clamp(retrievalMs) + ","
            + "\"rerankMs\":" + clamp(rerankMs) + ","
            + "\"promptBuildMs\":" + clamp(promptBuildMs) + ","
            + "\"promptMs\":" + clamp(promptBuildMs) + ","
            + "\"firstTokenMs\":" + clamp(firstTokenMs) + ","
            + "\"decodeMs\":" + clamp(decodeMs) + ","
            + "\"generationMs\":" + clamp(decodeMs) + ","
            + "\"totalMs\":" + clamp(totalMs)
            + "}";
    }

    public static void setLogSinkForTest(BiConsumer<String, String> sink) {
        logSink = sink == null ? LatencyPanel::logToAndroid : sink;
    }

    public static void resetLogSinkForTest() {
        logSink = LatencyPanel::logToAndroid;
    }

    private static boolean containsAny(String text, Set<String> markers) {
        if (text.isEmpty()) {
            return false;
        }
        for (String marker : markers) {
            if (!marker.isEmpty() && text.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private interface SourcePredicate {
        boolean matches(SearchResult source);
    }

    private static boolean hasValue(List<SearchResult> sources, SourcePredicate predicate) {
        for (SearchResult source : sources) {
            if (source != null && predicate.matches(source)) {
                return true;
            }
        }
        return false;
    }

    private static long clamp(long value) {
        return Math.max(0L, value);
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.US);
    }

    private static String safeQueryClass(String queryClass) {
        String normalized = queryClass == null ? "" : queryClass.trim();
        return normalized.isEmpty() ? QUERY_CLASS_PRACTICAL_HOW_TO : normalized;
    }

    private static String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }

    private static void logToAndroid(String tag, String message) {
        Log.i(tag, message);
    }
}

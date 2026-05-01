package com.senku.mobile;

import android.os.SystemClock;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PackRepository implements AutoCloseable {
    private static final String TAG = "SenkuPackRepo";
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final int HYBRID_RRF_K = 60;
    private static final int LEXICAL_CANDIDATE_LIMIT = 72;
    private static final int VECTOR_NEIGHBOR_LIMIT = 28;
    private static final int VECTOR_SEED_COUNT = 6;
    private static final String FTS5_TABLE = PackFtsRuntimeDetector.FTS5_TABLE;
    private static final String FTS4_TABLE = PackFtsRuntimeDetector.FTS4_TABLE;
    private static final long FTS_RUNTIME_PROBE_BUDGET_MS = 25L;
    private static final Object FTS_RUNTIME_LOCK = new Object();
    private static volatile PackFtsRuntimeDetector.Runtime cachedFtsRuntime;
    private final Object closeLock = new Object();
    private static final Set<String> HOUSE_SITE_SELECTION_ANCHOR_MARKERS = buildMarkerSet(
        "terrain analysis",
        "site assessment checklist",
        "wind exposure",
        "water proximity",
        "natural hazards",
        "seasonal considerations",
        "access routes",
        "sun exposure",
        "microclimate"
    );
    private static final Set<String> HOUSE_FOUNDATION_DETAIL_MARKERS = buildMarkerSet(
        "foundations",
        "foundation planning",
        "foundation layout",
        "frost line",
        "frost heave",
        "footing",
        "footings",
        "footing sizing",
        "rubble trench",
        "french drain",
        "drainage and waterproofing"
    );
    private static final Set<String> STOP_TOKENS = buildStopTokens();
    private static final Set<String> LOW_SIGNAL_TOKENS = buildLowSignalTokens();
    private static final Map<String, String[]> QUERY_EXPANSIONS = buildQueryExpansions();

    private final SQLiteDatabase database;
    private final VectorStore vectorStore;
    private final boolean ftsAvailable;
    private final String ftsTableName;
    private final boolean ftsSupportsBm25;
    private final Map<String, Map<String, Double>> anchorRelatedLinkWeights;
    private final AnswerCardDao answerCardDao;
    private boolean closed;

    public PackRepository(File databaseFile, File vectorFile) {
        this.database = SQLiteDatabase.openDatabase(
            databaseFile.getAbsolutePath(),
            null,
            SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS
        );
        VectorStore loadedStore = null;
        if (vectorFile != null && vectorFile.isFile()) {
            try {
                loadedStore = new VectorStore(vectorFile);
            } catch (IOException ignored) {
                loadedStore = null;
            }
        }
        this.vectorStore = loadedStore;
        PackFtsRuntimeDetector.Runtime ftsRuntime = detectFtsAvailability();
        this.ftsAvailable = ftsRuntime.available;
        this.ftsTableName = ftsRuntime.tableName;
        this.ftsSupportsBm25 = ftsRuntime.supportsBm25;
        this.anchorRelatedLinkWeights = loadAnchorRelatedLinkWeights();
        this.answerCardDao = new AnswerCardDao(database);
    }

    public boolean hasVectorStore() {
        return vectorStore != null;
    }

    public List<SearchResult> browseGuides(int limit) {
        ArrayList<SearchResult> results = new ArrayList<>();
        String sql = "SELECT title, guide_id, category, difficulty, description, body_markdown, " +
            "content_role, time_horizon, structure_type, topic_tags " +
            "FROM guides ORDER BY title";
        String[] args = null;
        if (limit > 0) {
            sql += " LIMIT ?";
            args = new String[]{String.valueOf(limit)};
        }

        try (Cursor cursor = database.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                results.add(guideResultFromCursor(cursor));
            }
        }
        return results;
    }

    public SearchResult loadGuideById(String rawGuideId) {
        String guideId = emptySafe(rawGuideId).trim();
        if (guideId.isEmpty()) {
            return null;
        }

        String sql = "SELECT title, guide_id, category, difficulty, description, body_markdown, " +
            "content_role, time_horizon, structure_type, topic_tags " +
            "FROM guides WHERE guide_id = ? LIMIT 1";
        try (Cursor cursor = database.rawQuery(sql, new String[]{guideId})) {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return guideResultFromCursor(cursor);
        }
    }

    public List<SearchResult> loadRelatedGuides(String rawGuideId, int limit) {
        String guideId = emptySafe(rawGuideId).trim();
        if (guideId.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        int cappedLimit = Math.max(limit, 1);
        int candidateLimit = relatedGuideCandidateLimit(cappedLimit);
        LinkedHashMap<String, SearchResult> related = new LinkedHashMap<>();
        appendRelatedGuides(
            related,
            "SELECT DISTINCT g.title, g.guide_id, g.category, g.difficulty, g.description, g.body_markdown, " +
                "g.content_role, g.time_horizon, g.structure_type, g.topic_tags " +
                "FROM guide_related gr " +
                "JOIN guides g ON g.guide_id = gr.related_guide_id " +
                "WHERE gr.guide_id = ? " +
                "AND gr.related_guide_id IS NOT NULL " +
                "AND gr.related_guide_id != ? " +
                "ORDER BY g.title LIMIT ?",
            new String[]{guideId, guideId, String.valueOf(candidateLimit)},
            candidateLimit
        );
        if (related.size() < candidateLimit) {
            appendRelatedGuides(
                related,
                "SELECT DISTINCT g.title, g.guide_id, g.category, g.difficulty, g.description, g.body_markdown, " +
                    "g.content_role, g.time_horizon, g.structure_type, g.topic_tags " +
                    "FROM guide_related gr " +
                    "JOIN guides g ON g.guide_id = gr.guide_id " +
                    "WHERE gr.related_guide_id = ? " +
                    "AND gr.guide_id != ? " +
                    "ORDER BY g.title LIMIT ?",
                new String[]{guideId, guideId, String.valueOf(candidateLimit)},
                candidateLimit
            );
        }

        SearchResult anchor = loadGuideById(guideId);
        ArrayList<SearchResult> ordered = orderRelatedGuidesByWorkflowRelevance(anchor, related.values());
        if (ordered.size() > cappedLimit) {
            return new ArrayList<>(ordered.subList(0, cappedLimit));
        }
        return ordered;
    }

    public List<AnswerCard> loadAnswerCardsForGuideIds(Set<String> guideIds, int limit) {
        return answerCardDao.loadCardsForGuideIds(guideIds, limit);
    }

    private ParsedSearchQuery parseSearchQuery(String rawQuery) {
        String query = emptySafe(rawQuery).trim();
        if (!query.startsWith(PackAnchorPriorPolicy.DIRECTIVE_PREFIX)) {
            return new ParsedSearchQuery(query, null);
        }
        int markerEnd = query.indexOf(' ');
        if (markerEnd <= 0) {
            return new ParsedSearchQuery(query, null);
        }
        String payload = query.substring(PackAnchorPriorPolicy.DIRECTIVE_PREFIX.length(), markerEnd);
        String[] parts = payload.split(":");
        if (parts.length != 3) {
            return new ParsedSearchQuery(query, null);
        }
        try {
            AnchorPriorDirective directive = new AnchorPriorDirective(
                parts[0].trim(),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
            );
            return new ParsedSearchQuery(query.substring(markerEnd + 1).trim(), directive);
        } catch (NumberFormatException ignored) {
            return new ParsedSearchQuery(query, null);
        }
    }

    private Map<String, Map<String, Double>> loadAnchorRelatedLinkWeights() {
        LinkedHashMap<String, Set<String>> directedLinks = new LinkedHashMap<>();
        try {
            try (
                Cursor cursor = database.rawQuery(
                    "SELECT guide_id, related_guide_id FROM guide_related " +
                        "WHERE guide_id IS NOT NULL AND related_guide_id IS NOT NULL",
                    null
                )
            ) {
                while (cursor.moveToNext()) {
                    String guideId = emptySafe(cursor.getString(0)).trim();
                    String relatedGuideId = emptySafe(cursor.getString(1)).trim();
                    if (guideId.isEmpty() || relatedGuideId.isEmpty() || guideId.equals(relatedGuideId)) {
                        continue;
                    }
                    directedLinks.computeIfAbsent(guideId, key -> new LinkedHashSet<>()).add(relatedGuideId);
                    directedLinks.putIfAbsent(relatedGuideId, new LinkedHashSet<>());
                }
            }
        } catch (SQLiteException ignored) {
            return Collections.emptyMap();
        }

        return PackAnchorPriorPolicy.relatedLinkWeights(directedLinks);
    }

    public String getAnchorSnippet(String rawGuideId, String sessionChunkId) {
        String guideId = emptySafe(rawGuideId).trim();
        if (guideId.isEmpty()) {
            return "";
        }

        String snippet = AnchorSnippetFormatter.resolve(
            loadChunkDocumentForGuide(sessionChunkId, guideId),
            loadLeadingChunkDocumentForGuide(guideId),
            ""
        );
        if (!snippet.isEmpty()) {
            return snippet;
        }

        SearchResult guide = loadGuideById(guideId);
        return AnchorSnippetFormatter.resolve("", "", guide == null ? "" : guide.body);
    }

    public Set<String> getReciprocalLinks(String guideId) {
        LinkedHashSet<String> reciprocal = new LinkedHashSet<>();
        Map<String, Double> weights = anchorRelatedLinkWeights.get(emptySafe(guideId).trim());
        if (weights == null || weights.isEmpty()) {
            return Collections.emptySet();
        }
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            if (entry.getValue() != null && entry.getValue() >= 0.5) {
                reciprocal.add(entry.getKey());
            }
        }
        return reciprocal.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(reciprocal);
    }

    private String loadChunkDocumentForGuide(String rawChunkId, String guideId) {
        String chunkId = emptySafe(rawChunkId).trim();
        if (chunkId.isEmpty() || guideId.isEmpty()) {
            return "";
        }

        try (Cursor cursor = database.rawQuery(
            "SELECT guide_id, document FROM chunks WHERE chunk_id = ? LIMIT 1",
            new String[]{chunkId}
        )) {
            if (!cursor.moveToFirst()) {
                return "";
            }
            String chunkGuideId = emptySafe(cursor.getString(0)).trim();
            if (!guideId.equalsIgnoreCase(chunkGuideId)) {
                return "";
            }
            return emptySafe(cursor.getString(1));
        } catch (SQLiteException ignored) {
            return "";
        }
    }

    private String loadLeadingChunkDocumentForGuide(String guideId) {
        if (guideId.isEmpty()) {
            return "";
        }

        try (Cursor cursor = database.rawQuery(
            "SELECT document FROM chunks WHERE guide_id = ? " +
                "AND LENGTH(TRIM(COALESCE(document, ''))) > 0 " +
                "ORDER BY " +
                "CASE WHEN LENGTH(TRIM(COALESCE(section_heading, ''))) = 0 THEN 0 ELSE 1 END, " +
                "CASE WHEN vector_row_id IS NULL OR vector_row_id < 0 THEN 1 ELSE 0 END, " +
                "vector_row_id ASC, rowid ASC LIMIT 1",
            new String[]{guideId}
        )) {
            if (!cursor.moveToFirst()) {
                return "";
            }
            return emptySafe(cursor.getString(0));
        } catch (SQLiteException ignored) {
            return "";
        }
    }

    public List<SearchResult> search(String rawQuery, int limit) {
        ParsedSearchQuery parsedQuery = parseSearchQuery(rawQuery);
        return search(parsedQuery.query, limit, parsedQuery.anchorPrior);
    }

    public List<SearchResult> search(String rawQuery, int limit, AnchorPriorDirective anchorPrior) {
        String query = emptySafe(rawQuery).trim();
        if (query.isEmpty()) {
            return browseGuides(limit);
        }

        long startedAt = System.currentTimeMillis();
        long rerankElapsedMs = 0L;
        QueryTerms queryTerms = QueryTerms.fromQuery(query);
        if (queryTerms.isEmpty()) {
            List<SearchResult> fallbackResults = searchPlainLikeResults(query, limit);
            SearchLatencyBreakdown breakdown = new SearchLatencyBreakdown(
                0L,
                0L,
                0L,
                0L,
                0L,
                Math.max(0L, System.currentTimeMillis() - startedAt),
                "plainLike_no_terms"
            );
            Log.d(TAG, buildSearchSummaryLine(query, false, 0, 0, 0, 0, breakdown));
            logSearchTripwireIfNeeded(query, breakdown);
            return fallbackResults;
        }
        Log.d(
            TAG,
            "search.start query=\"" + query + "\" routeFocused=" + queryTerms.routeProfile.isRouteFocused() +
                " routeSpecs=" + queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower).size() +
                " structure=" + emptySafe(queryTerms.metadataProfile.preferredStructureType()) +
                " explicitTopics=" + queryTerms.metadataProfile.preferredTopicTags() +
                " limit=" + limit
        );

        int routeResultLimit = queryTerms.routeProfile.isRouteFocused()
            ? Math.min(Math.max(Math.max(limit, 18) / (queryTerms.routeProfile.isStarterBuildProject() ? 3 : 2), 12), 24)
            : 0;
        long routeStartedAt = System.currentTimeMillis();
        List<SearchResult> routeResults = queryTerms.routeProfile.isRouteFocused()
            ? searchRouteFocusedResults(queryTerms, routeResultLimit)
            : Collections.emptyList();
        long routeElapsedMs = System.currentTimeMillis() - routeStartedAt;

        int lexicalCandidateLimit = lexicalCandidateLimit(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            limit,
            routeResults.size()
        );
        List<RankedChunk> ftsHits = Collections.emptyList();
        long ftsStartedAt = System.currentTimeMillis();
        if (ftsAvailable) {
            try {
                ftsHits = searchWithFtsHits(queryTerms, lexicalCandidateLimit);
            } catch (SQLiteException ignored) {
                ftsHits = Collections.emptyList();
            }
        }
        long ftsElapsedMs = System.currentTimeMillis() - ftsStartedAt;

        long keywordStartedAt = System.currentTimeMillis();
        List<RankedChunk> keywordHits = searchWithKeywordHits(queryTerms, lexicalCandidateLimit);
        List<RankedChunk> lexicalHits = mergeLexicalHits(ftsHits, keywordHits);
        long keywordElapsedMs = System.currentTimeMillis() - keywordStartedAt;
        maybeLogCandidateTelemetry(buildLexicalCandidateTelemetryLine(query, lexicalHits));

        if (lexicalHits.isEmpty()) {
            if (!routeResults.isEmpty()) {
                SearchLatencyBreakdown breakdown = new SearchLatencyBreakdown(
                    routeElapsedMs,
                    ftsElapsedMs,
                    keywordElapsedMs,
                    0L,
                    0L,
                    Math.max(0L, System.currentTimeMillis() - startedAt),
                    "route_only"
                );
                Log.d(
                    TAG,
                    buildSearchSummaryLine(
                        query,
                        queryTerms.routeProfile.isRouteFocused(),
                        queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower).size(),
                        0,
                        0,
                        routeResults.size(),
                        breakdown
                    )
                );
                logSearchTripwireIfNeeded(query, breakdown);
                return new ArrayList<>(routeResults.subList(0, Math.min(limit, routeResults.size())));
            }
            SearchLatencyBreakdown breakdown = new SearchLatencyBreakdown(
                routeElapsedMs,
                ftsElapsedMs,
                keywordElapsedMs,
                0L,
                0L,
                Math.max(0L, System.currentTimeMillis() - startedAt),
                "plainLike_empty_lexical"
            );
            Log.d(
                TAG,
                buildSearchSummaryLine(
                    query,
                    queryTerms.routeProfile.isRouteFocused(),
                    queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower).size(),
                    0,
                    0,
                    0,
                    breakdown
                )
            );
            logSearchTripwireIfNeeded(query, breakdown);
            return searchPlainLikeResults(query, limit);
        }

        if (vectorStore == null) {
            maybeLogCandidateTelemetry(buildVectorCandidateTelemetryLine(query, Collections.emptyList()));
            List<CombinedHit> lexicalCombinedHits = mergeHybrid(lexicalHits, Collections.emptyList(), anchorPrior);
            maybeLogCandidateTelemetry(buildPrerankCandidateTelemetryLine(query, lexicalCombinedHits, limit));
            List<SearchResult> lexicalResults = toSearchResults(lexicalCombinedHits, limit);
            long rerankStartedAtNs = elapsedRealtimeNanosSafe();
            List<RerankedResult> rerankedDetails = maybeRerankResultsDetailed(queryTerms, lexicalResults, limit);
            List<SearchResult> reranked = extractSearchResults(rerankedDetails);
            rerankElapsedMs = elapsedRealtimeMsSince(rerankStartedAtNs);
            maybeLogCandidateTelemetry(buildRerankedCandidateTelemetryLine(query, rerankedDetails));
            SearchLatencyBreakdown breakdown = new SearchLatencyBreakdown(
                routeElapsedMs,
                ftsElapsedMs,
                keywordElapsedMs,
                0L,
                rerankElapsedMs,
                Math.max(0L, System.currentTimeMillis() - startedAt),
                "vector_disabled"
            );
            Log.d(
                TAG,
                buildSearchSummaryLine(
                    query,
                    queryTerms.routeProfile.isRouteFocused(),
                    queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower).size(),
                    lexicalHits.size(),
                    0,
                    routeResults.size(),
                    breakdown
                )
            );
            logSearchTripwireIfNeeded(query, breakdown);
            return mergePreferredResults(routeResults, reranked, limit);
        }

        List<Integer> seedRows = new ArrayList<>();
        for (RankedChunk hit : lexicalHits) {
            if (hit.vectorRowId >= 0) {
                seedRows.add(hit.vectorRowId);
            }
            if (seedRows.size() >= VECTOR_SEED_COUNT) {
                break;
            }
        }
        long vectorStartedAt = System.currentTimeMillis();
        float[] centroid = vectorStore.buildCentroid(seedRows, VECTOR_SEED_COUNT);
        if (centroid == null) {
            maybeLogCandidateTelemetry(buildVectorCandidateTelemetryLine(query, Collections.emptyList()));
            List<CombinedHit> lexicalCombinedHits = mergeHybrid(lexicalHits, Collections.emptyList(), anchorPrior);
            maybeLogCandidateTelemetry(buildPrerankCandidateTelemetryLine(query, lexicalCombinedHits, limit));
            List<SearchResult> lexicalResults = toSearchResults(lexicalCombinedHits, limit);
            long rerankStartedAtNs = elapsedRealtimeNanosSafe();
            List<RerankedResult> rerankedDetails = maybeRerankResultsDetailed(queryTerms, lexicalResults, limit);
            rerankElapsedMs = elapsedRealtimeMsSince(rerankStartedAtNs);
            maybeLogCandidateTelemetry(buildRerankedCandidateTelemetryLine(query, rerankedDetails));
            SearchLatencyBreakdown breakdown = new SearchLatencyBreakdown(
                routeElapsedMs,
                ftsElapsedMs,
                keywordElapsedMs,
                Math.max(0L, System.currentTimeMillis() - vectorStartedAt),
                rerankElapsedMs,
                Math.max(0L, System.currentTimeMillis() - startedAt),
                "centroid_missing"
            );
            Log.d(
                TAG,
                buildSearchSummaryLine(
                    query,
                    queryTerms.routeProfile.isRouteFocused(),
                    queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower).size(),
                    lexicalHits.size(),
                    0,
                    routeResults.size(),
                    breakdown
                )
            );
            logSearchTripwireIfNeeded(query, breakdown);
            return mergeResultsWhenCentroidMissing(
                routeResults,
                extractSearchResults(rerankedDetails),
                limit
            );
        }

        List<VectorStore.VectorNeighbor> neighbors = vectorStore.findNearest(centroid, VECTOR_NEIGHBOR_LIMIT);
        List<RankedChunk> vectorHits = loadVectorNeighborHits(neighbors);
        maybeLogCandidateTelemetry(buildVectorCandidateTelemetryLine(query, vectorHits));
        List<CombinedHit> hybridCombinedHits = mergeHybrid(lexicalHits, vectorHits, anchorPrior);
        maybeLogCandidateTelemetry(buildPrerankCandidateTelemetryLine(query, hybridCombinedHits, limit));
        List<SearchResult> hybridResults = toSearchResults(hybridCombinedHits, limit);
        long rerankStartedAtNs = elapsedRealtimeNanosSafe();
        List<RerankedResult> rerankedDetails = maybeRerankResultsDetailed(queryTerms, hybridResults, limit);
        List<SearchResult> reranked = extractSearchResults(rerankedDetails);
        rerankElapsedMs = elapsedRealtimeMsSince(rerankStartedAtNs);
        maybeLogCandidateTelemetry(buildRerankedCandidateTelemetryLine(query, rerankedDetails));
        long vectorElapsedMs = System.currentTimeMillis() - vectorStartedAt;
        SearchLatencyBreakdown breakdown = new SearchLatencyBreakdown(
            routeElapsedMs,
            ftsElapsedMs,
            keywordElapsedMs,
            vectorElapsedMs,
            rerankElapsedMs,
            Math.max(0L, System.currentTimeMillis() - startedAt),
            "hybrid"
        );
        Log.d(
            TAG,
            buildSearchSummaryLine(
                query,
                queryTerms.routeProfile.isRouteFocused(),
                queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower).size(),
                lexicalHits.size(),
                vectorHits.size(),
                routeResults.size(),
                breakdown
            )
        );
        logSearchTripwireIfNeeded(query, breakdown);
        List<SearchResult> merged = mergePreferredResults(routeResults, reranked, limit);
        if (!merged.isEmpty()) {
            StringBuilder debug = new StringBuilder();
            int previewCount = Math.min(6, merged.size());
            for (int index = 0; index < previewCount; index++) {
                SearchResult result = merged.get(index);
                if (debug.length() > 0) {
                    debug.append(" || ");
                }
                debug.append(index + 1)
                    .append(":")
                    .append(emptySafe(result.guideId))
                    .append(" :: ")
                    .append(emptySafe(result.title))
                    .append(" :: ")
                    .append(emptySafe(result.sectionHeading))
                    .append(" :: ")
                    .append(emptySafe(result.retrievalMode));
            }
            Log.d(TAG, "search.topResults query=\"" + query + "\" " + debug);
        }
        return merged;
    }

    public List<SearchResult> buildGuideAnswerContext(String rawQuery, List<SearchResult> rankedResults, int limit) {
        ArrayList<SearchResult> context = new ArrayList<>();
        if (rankedResults == null || rankedResults.isEmpty() || limit <= 0) {
            return context;
        }

        long startedAt = System.currentTimeMillis();
        QueryTerms queryTerms = QueryTerms.fromQuery(rawQuery);
        QueryRouteProfile routeProfile = queryTerms.routeProfile;
        boolean diversifyContext = routeProfile.prefersDiversifiedAnswerContext()
            || queryTerms.metadataProfile.prefersDiversifiedContext();
        SearchResult anchor = selectAnswerAnchor(queryTerms, rankedResults);
        if (anchor == null) {
            Log.d(TAG, "context query=\"" + rawQuery + "\" anchor=false totalMs=" + (System.currentTimeMillis() - startedAt));
            return context;
        }
        List<SearchResult> guideSections = loadGuideSectionsForAnswer(queryTerms, anchor, limit);
        ArrayList<PackAnswerContextPolicy.SupportCandidate> supportingCandidates =
            PackAnswerContextPolicy.rankSupportCandidates(
                queryTerms,
                routeProfile,
                diversifyContext,
                anchor,
                rankedResults
            );
        if (!supportingCandidates.isEmpty()) {
            StringBuilder debug = new StringBuilder();
            int previewCount = Math.min(6, supportingCandidates.size());
            for (int index = 0; index < previewCount; index++) {
                PackAnswerContextPolicy.SupportCandidate candidate = supportingCandidates.get(index);
                if (debug.length() > 0) {
                    debug.append(" || ");
                }
                debug.append(index + 1)
                    .append(":")
                    .append(emptySafe(candidate.result.guideId))
                    .append(" :: ")
                    .append(emptySafe(candidate.result.sectionHeading))
                    .append(" :: ")
                    .append(emptySafe(candidate.result.retrievalMode))
                    .append(" :: ")
                    .append(candidate.score);
            }
            Log.d(
                TAG,
                "context.supportCandidates query=\"" + rawQuery + "\" diversify=" + diversifyContext + " " + debug
            );
        }

        context = PackAnswerContextPolicy.assembleGuideAnswerContext(
            queryTerms,
            diversifyContext,
            anchor,
            guideSections,
            supportingCandidates,
            limit
        );

        if (!context.isEmpty()) {
            StringBuilder selected = new StringBuilder();
            int previewCount = Math.min(6, context.size());
            for (int index = 0; index < previewCount; index++) {
                SearchResult result = context.get(index);
                if (selected.length() > 0) {
                    selected.append(" || ");
                }
                selected.append(index + 1)
                    .append(":")
                    .append(emptySafe(result.guideId))
                    .append(" :: ")
                    .append(emptySafe(result.sectionHeading))
                    .append(" :: ")
                    .append(emptySafe(result.retrievalMode));
            }
            Log.d(TAG, "context.selected query=\"" + rawQuery + "\" " + selected);
        }

        Log.d(
            TAG,
            "context query=\"" + rawQuery + "\" anchorGuide=\"" + emptySafe(anchor.guideId) +
                "\" diversify=" + diversifyContext +
                "\" guideSections=" + guideSections.size() + " finalContext=" + context.size() +
                " totalMs=" + (System.currentTimeMillis() - startedAt)
        );
        return context;
    }

    static boolean shouldApplyMetadataRerankForTest(String query) {
        return shouldApplyMetadataRerank(QueryTerms.fromQuery(query));
    }

    static List<RerankedResult> maybeRerankResultsDetailedForTest(String query, List<SearchResult> results, int limit) {
        return maybeRerankResultsDetailed(QueryTerms.fromQuery(query), results, limit);
    }

    private List<SearchResult> maybeRerankResults(QueryTerms queryTerms, List<SearchResult> results, int limit) {
        List<RerankedResult> detailed = maybeRerankResultsDetailed(queryTerms, results, limit);
        return extractSearchResults(detailed);
    }

    private static boolean shouldApplyMetadataRerank(QueryTerms queryTerms) {
        if (queryTerms == null) {
            return false;
        }
        boolean routeFocused = queryTerms.routeProfile != null && queryTerms.routeProfile.isRouteFocused();
        boolean hasStructureHint = queryTerms.metadataProfile != null
            && !emptySafe(queryTerms.metadataProfile.preferredStructureType()).isEmpty();
        return routeFocused || hasStructureHint;
    }

    private static List<RerankedResult> passthroughRerankedResults(List<SearchResult> results, int limit) {
        ArrayList<RerankedResult> passthrough = new ArrayList<>();
        int capped = limit <= 0 ? results.size() : Math.min(limit, results.size());
        for (int index = 0; index < capped; index++) {
            passthrough.add(new RerankedResult(results.get(index), index, 0, 0.0));
        }
        return passthrough;
    }

    private static List<RerankedResult> maybeRerankResultsDetailed(QueryTerms queryTerms, List<SearchResult> results, int limit) {
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        if (!shouldApplyMetadataRerank(queryTerms)) {
            return passthroughRerankedResults(results, limit);
        }

        long rerankStartedAtNanos = elapsedRealtimeNanosSafe();
        LinkedHashMap<String, GuideScore> guides = new LinkedHashMap<>();
        ArrayList<RerankedResult> scored = new ArrayList<>();
        for (int index = 0; index < results.size(); index++) {
            SearchResult result = results.get(index);
            PackSupportScoringPolicy.SupportBreakdown support =
                PackSupportScoringPolicy.supportBreakdown(queryTerms, result);
            int metadataBonus = support.metadataBonus;
            int score = support.supportWithMetadata();
            score += rerankModeBonus(result.retrievalMode);

            String guideKey = PackSupportScoringPolicy.guideGroupKey(result);
            GuideScore guide = guides.get(guideKey);
            if (guide == null) {
                guide = new GuideScore(result);
                guides.put(guideKey, guide);
            }
            guide.totalScore += Math.max(1, score);
            guide.bestScore = Math.max(guide.bestScore, score);
            guide.sectionKeys.add(buildGuideSectionKey(result.guideId, result.title, result.sectionHeading));
            scored.add(new RerankedResult(result, index, metadataBonus, score));
        }

        for (RerankedResult scoredResult : scored) {
            GuideScore guide = guides.get(PackSupportScoringPolicy.guideGroupKey(scoredResult.result));
            if (guide == null) {
                continue;
            }
            scoredResult.addGuideBonus(guideAggregationBonus(guide));
        }

        scored.sort((left, right) -> {
            int scoreOrder = Double.compare(right.finalScore, left.finalScore);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            return Integer.compare(left.originalIndex, right.originalIndex);
        });

        int capped = limit <= 0 ? scored.size() : Math.min(limit, scored.size());
        ArrayList<RerankedResult> ordered = new ArrayList<>(scored.subList(0, capped));
        safeLogDebug(
            buildRerankTimingDebugLine(
                queryTerms.queryLower,
                limit,
                results.size(),
                ordered.size(),
                elapsedRealtimeNanosSafe() - rerankStartedAtNanos
            )
        );
        return ordered;
    }

    private static boolean isVectorRetrievalMode(String retrievalMode) {
        return "vector".equals(emptySafe(retrievalMode).trim().toLowerCase(QUERY_LOCALE));
    }

    private static int rerankModeBonus(String retrievalMode) {
        String mode = emptySafe(retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        if ("route-focus".equals(mode)) {
            return 8;
        }
        if ("hybrid".equals(mode)) {
            return 4;
        }
        if ("guide-focus".equals(mode)) {
            return 3;
        }
        if ("lexical".equals(mode)) {
            return 2;
        }
        return 0;
    }

    private static int guideAggregationBonus(GuideScore guide) {
        if (guide == null) {
            return 0;
        }
        int bonus = Math.min(24, guide.totalScore / 3);
        if (guide.sectionKeys.size() >= 2) {
            bonus += 6;
        }
        return bonus;
    }

    private static String buildRerankTimingDebugLine(
        String query,
        int topK,
        int chunkCount,
        int selectedCount,
        long elapsedNanos
    ) {
        return PackQueryPipelineHelper.buildRerankTimingDebugLine(
            query,
            topK,
            chunkCount,
            selectedCount,
            elapsedNanos
        );
    }

    static String buildRerankTimingDebugLineForTest(
        String query,
        int topK,
        int chunkCount,
        int selectedCount,
        long elapsedNanos
    ) {
        return buildRerankTimingDebugLine(query, topK, chunkCount, selectedCount, elapsedNanos);
    }

    static String buildFtsRuntimeDebugLineForTest(
        boolean supportsFts5Compile,
        boolean supportsFts4Compile,
        boolean hasFts5Table,
        boolean hasFts4Table,
        boolean runtimeFts5,
        boolean runtimeFts4
    ) {
        return PackFtsRuntimeDetector.buildDebugLine(
            supportsFts5Compile,
            supportsFts4Compile,
            hasFts5Table,
            hasFts4Table,
            runtimeFts5,
            runtimeFts4
        );
    }

    static String buildSearchSummaryLineForTest(
        String query,
        boolean routeFocused,
        int routeSpecCount,
        int lexicalHits,
        int vectorHits,
        int routeResults,
        long routeMs,
        long ftsMs,
        long keywordMs,
        long vectorMs,
        long rerankMs,
        long totalMs,
        String fallbackMode
    ) {
        return buildSearchSummaryLine(
            query,
            routeFocused,
            routeSpecCount,
            lexicalHits,
            vectorHits,
            routeResults,
            new SearchLatencyBreakdown(
                routeMs,
                ftsMs,
                keywordMs,
                vectorMs,
                rerankMs,
                totalMs,
                fallbackMode
            )
        );
    }

    static String buildSlowQueryTripwireDebugLineForTest(
        String query,
        long routeMs,
        long ftsMs,
        long keywordMs,
        long vectorMs,
        long rerankMs,
        long totalMs,
        String fallbackMode
    ) {
        return buildSlowQueryTripwireDebugLine(
            query,
            new SearchLatencyBreakdown(
                routeMs,
                ftsMs,
                keywordMs,
                vectorMs,
                rerankMs,
                totalMs,
                fallbackMode
            )
        );
    }

    private List<SearchResult> searchRouteFocusedResults(QueryTerms queryTerms, int limit) {
        long startedAt = System.currentTimeMillis();
        List<QueryRouteProfile.RouteSearchSpec> routeSpecs = PackRouteFocusedSearchHelper.routeSearchSpecs(queryTerms);
        if (routeSpecs.isEmpty()) {
            return Collections.emptyList();
        }
        Log.d(
            TAG,
            "routeSearch.start query=\"" + queryTerms.queryLower + "\" specs=" + routeSpecs.size() +
                " limit=" + limit +
                " structure=" + emptySafe(queryTerms.metadataProfile.preferredStructureType()) +
                " explicitTopics=" + queryTerms.metadataProfile.preferredTopicTags()
        );

        boolean compactGuideSweep = queryTerms.routeProfile.usesCompactGuideSweep(queryTerms.queryLower);
        LinkedHashMap<String, ScoredSearchResult> bestBySection = new LinkedHashMap<>();
        int candidateLimit = PackRouteFocusedSearchHelper.routeChunkCandidateLimit(queryTerms, limit);
        int candidateTarget = PackRouteFocusedSearchHelper.routeChunkCandidateTarget(queryTerms, limit);
        for (QueryRouteProfile.RouteSearchSpec routeSpec : routeSpecs) {
            if (bestBySection.size() >= candidateTarget) {
                break;
            }
            PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec, 6);
            if (routeStep == null) {
                continue;
            }
            int addedWithFts = collectRouteFocusedChunkResultsFts(
                queryTerms,
                routeStep.specTerms,
                routeStep.routeSpec,
                routeStep.categories,
                candidateLimit,
                candidateTarget,
                bestBySection
            );
            if (!PackRouteFocusedSearchHelper.shouldBackfillLikeAfterFts(
                queryTerms,
                addedWithFts,
                bestBySection.size(),
                candidateTarget
            )) {
                continue;
            }
            collectRouteFocusedChunkResultsLike(
                queryTerms,
                routeStep.specTerms,
                routeStep.routeSpec,
                routeStep.tokens,
                routeStep.categories,
                candidateLimit,
                candidateTarget,
                bestBySection
            );
        }

        int guideSearchThreshold = routeGuideSearchThreshold(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            compactGuideSweep,
            limit
        );
        guideSearchThreshold = runtimeRouteGuideSearchThreshold(
            queryTerms.metadataProfile,
            ftsSupportsBm25,
            guideSearchThreshold
        );
        Log.d(
            TAG,
            "routeGuideSearch.pre query=\"" + queryTerms.queryLower + "\" sections=" + bestBySection.size() +
                " threshold=" + guideSearchThreshold +
                " compact=" + compactGuideSweep +
                " structure=" + emptySafe(queryTerms.metadataProfile.preferredStructureType()) +
                " explicitTopics=" + queryTerms.metadataProfile.preferredTopicTags()
        );
        if (bestBySection.size() < guideSearchThreshold) {
            collectRouteFocusedGuideResults(
                queryTerms,
                routeSpecs,
                queryTerms.routeProfile.isStarterBuildProject()
                    ? Math.max(limit * 4, 72)
                    : compactGuideSweep ? Math.max(limit * 2, 24) : Math.max(limit * 3, 48),
                guideSearchThreshold,
                bestBySection
            );
        } else {
            Log.d(
                TAG,
                "routeGuideSearch.skip query=\"" + queryTerms.queryLower + "\" sections=" + bestBySection.size() +
                    " threshold=" + guideSearchThreshold +
                    " structure=" + emptySafe(queryTerms.metadataProfile.preferredStructureType()) +
                    " explicitTopics=" + queryTerms.metadataProfile.preferredTopicTags()
            );
        }

        if (bestBySection.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashMap<String, Integer> guideTotals = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> guideSectionCounts = new LinkedHashMap<>();
        for (ScoredSearchResult sectionScore : bestBySection.values()) {
            String guideKey = PackSupportScoringPolicy.guideGroupKey(sectionScore.result);
            guideTotals.put(guideKey, guideTotals.getOrDefault(guideKey, 0) + Math.max(1, sectionScore.score));
            guideSectionCounts.put(guideKey, guideSectionCounts.getOrDefault(guideKey, 0) + 1);
        }

        ArrayList<ScoredSearchResult> scored = new ArrayList<>();
        boolean conservativeWaterGuideBundling = "water_storage".equals(queryTerms.metadataProfile.preferredStructureType())
            && !queryTerms.metadataProfile.hasExplicitTopic("water_distribution");
        for (ScoredSearchResult sectionScore : bestBySection.values()) {
            String guideKey = PackSupportScoringPolicy.guideGroupKey(sectionScore.result);
            int guideBonus = Math.min(28, guideTotals.getOrDefault(guideKey, 0) / 4);
            int diversityBonus = Math.min(10, guideSectionCounts.getOrDefault(guideKey, 0) * 2);
            if (conservativeWaterGuideBundling) {
                guideBonus = 0;
                diversityBonus = 0;
            }
            int score = sectionScore.score + guideBonus + diversityBonus;
            if (conservativeWaterGuideBundling) {
                score += broadWaterRouteRefinementBonus(queryTerms, sectionScore.result);
            }
            scored.add(new ScoredSearchResult(
                sectionScore.result,
                sectionScore.originalIndex,
                score
            ));
        }

        scored.sort((left, right) -> {
            if (conservativeWaterGuideBundling) {
                int priorityOrder = Integer.compare(
                    broadWaterRouteOrderingPriority(queryTerms, right.result),
                    broadWaterRouteOrderingPriority(queryTerms, left.result)
                );
                if (priorityOrder != 0) {
                    return priorityOrder;
                }
            }
            int scoreOrder = Integer.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int modeOrder = left.result.title.compareToIgnoreCase(right.result.title);
            if (modeOrder != 0) {
                return modeOrder;
            }
            return Integer.compare(left.originalIndex, right.originalIndex);
        });

        ArrayList<SearchResult> ordered = new ArrayList<>();
        for (ScoredSearchResult item : scored) {
            if (ordered.size() >= limit) {
                break;
            }
            ordered.add(item.result);
        }
        Log.d(
            TAG,
            "routeSearch query=\"" + queryTerms.queryLower + "\" specs=" + routeSpecs.size() +
                " candidateSections=" + bestBySection.size() + " returned=" + ordered.size() +
                " totalMs=" + (System.currentTimeMillis() - startedAt)
        );
        return ordered;
    }

    static int routeChunkCandidateLimitForTest(String query, int limit) {
        return PackRouteFocusedSearchHelper.routeChunkCandidateLimit(QueryTerms.fromQuery(query), limit);
    }

    static int routeChunkCandidateTargetForTest(String query, int limit) {
        return PackRouteFocusedSearchHelper.routeChunkCandidateTarget(QueryTerms.fromQuery(query), limit);
    }

    static boolean shouldBackfillLikeAfterFtsForTest(String query, int addedWithFts, int totalSections, int targetTotal) {
        return PackRouteFocusedSearchHelper.shouldBackfillLikeAfterFts(
            QueryTerms.fromQuery(query),
            addedWithFts,
            totalSections,
            targetTotal
        );
    }

    static String noBm25RouteFtsOrderLabelForTest(String query) {
        return PackRouteFocusedSearchHelper.noBm25RouteFtsOrder(QueryTerms.fromQuery(query)).label;
    }

    private int collectRouteFocusedChunkResultsFts(
        QueryTerms queryTerms,
        QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> categories,
        int candidateLimit,
        int candidateTarget,
        LinkedHashMap<String, ScoredSearchResult> bestBySection
    ) {
        if (!ftsAvailable) {
            return 0;
        }
        String ftsQuery = buildFtsQuery(specTerms, ftsSupportsBm25 ? 8 : 4, ftsSupportsBm25);
        if (ftsQuery.isEmpty()) {
            Log.d(TAG, "routeChunkFts.skip query=\"" + queryTerms.queryLower + "\" reason=empty_fts_query");
            return 0;
        }

        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        args.add(ftsQuery);
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }
        int effectiveCandidateLimit = ftsSupportsBm25
            ? candidateLimit
            : Math.min(candidateLimit, queryTerms.routeProfile.isStarterBuildProject() ? 120 : 84);
        PackRouteFocusedSearchHelper.RouteFtsOrderSpec orderSpec = ftsSupportsBm25
            ? new PackRouteFocusedSearchHelper.RouteFtsOrderSpec(
                " ORDER BY bm25(" + ftsTableName + ") ",
                Collections.emptyList(),
                "bm25"
            )
            : PackRouteFocusedSearchHelper.noBm25RouteFtsOrder(queryTerms);
        args.addAll(orderSpec.args);
        args.add(String.valueOf(effectiveCandidateLimit));

        try (Cursor cursor = database.rawQuery(
            "SELECT c.guide_title, c.guide_id, c.section_heading, c.category, c.document, c.tags, c.description, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags " +
                "FROM " + ftsTableName + " f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE " + ftsTableName + " MATCH ? " +
                "AND c.category IN (" + String.join(",", categoryPlaceholders) + ") " +
                orderSpec.clause + " LIMIT ?",
            args.toArray(new String[0])
        )) {
            int added = collectRouteFocusedChunkCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                candidateTarget
            );
            Log.d(
                TAG,
                "routeChunkFts query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + ftsQuery +
                    "\" added=" + added + " candidateLimit=" + effectiveCandidateLimit +
                    " order=" + orderSpec.label
            );
            return added;
        } catch (SQLiteException error) {
            Log.w(
                TAG,
                "routeChunkFts.fail query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + ftsQuery + "\"",
                error
            );
            return 0;
        }
    }

    private int collectRouteFocusedChunkResultsLike(
        QueryTerms queryTerms,
        QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> tokens,
        List<String> categories,
        int candidateLimit,
        int candidateTarget,
        LinkedHashMap<String, ScoredSearchResult> bestBySection
    ) {
        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }

        ArrayList<String> clauses = new ArrayList<>();
        for (String token : tokens) {
            String like = "%" + token + "%";
            clauses.add("(guide_title LIKE ? OR section_heading LIKE ? OR tags LIKE ? OR description LIKE ? OR document LIKE ?)");
            for (int index = 0; index < 5; index++) {
                args.add(like);
            }
        }
        args.add(String.valueOf(candidateLimit));

        try (Cursor cursor = database.rawQuery(
            "SELECT guide_title, guide_id, section_heading, category, document, tags, description, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE category IN (" + String.join(",", categoryPlaceholders) + ") " +
                "AND (" + String.join(" OR ", clauses) + ") LIMIT ?",
            args.toArray(new String[0])
        )) {
            return collectRouteFocusedChunkCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                candidateTarget
            );
        }
    }

    private int collectRouteFocusedChunkCursor(
        Cursor cursor,
        QueryTerms queryTerms,
        QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, ScoredSearchResult> bestBySection,
        int candidateTarget
    ) {
        return PackRouteFocusedCandidateCollector.collectChunkCursor(
            cursor,
            queryTerms,
            specTerms,
            routeSpec,
            bestBySection,
            candidateTarget
        );
    }

    static int routeGuideSearchThreshold(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        boolean compactGuideSweep,
        int limit
    ) {
        return RetrievalRoutePolicy.routeGuideSearchThreshold(
            routeProfile,
            metadataProfile,
            compactGuideSweep,
            limit
        );
    }

    static int runtimeRouteGuideSearchThreshold(
        QueryMetadataProfile metadataProfile,
        boolean ftsSupportsBm25,
        int threshold
    ) {
        return RetrievalRoutePolicy.runtimeRouteGuideSearchThreshold(metadataProfile, ftsSupportsBm25, threshold);
    }

    static int lexicalCandidateLimit(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        int limit,
        int routeResultCount
    ) {
        return RetrievalRoutePolicy.lexicalCandidateLimit(
            routeProfile,
            metadataProfile,
            limit,
            routeResultCount,
            LEXICAL_CANDIDATE_LIMIT
        );
    }

    static int keywordSqlLimitForTest(String query, int limit) {
        return keywordSqlLimit(QueryTerms.fromQuery(query), limit);
    }

    private static int keywordSqlLimit(QueryTerms queryTerms, int limit) {
        return RetrievalRoutePolicy.keywordSqlLimit(queryTerms == null ? null : queryTerms.routeProfile, limit);
    }

    private void collectRouteFocusedGuideResults(
        QueryTerms queryTerms,
        List<QueryRouteProfile.RouteSearchSpec> routeSpecs,
        int candidateLimit,
        int targetTotal,
        LinkedHashMap<String, ScoredSearchResult> bestBySection
    ) {
        long startedAt = System.currentTimeMillis();
        int beforeCount = bestBySection.size();
        for (QueryRouteProfile.RouteSearchSpec routeSpec : routeSpecs) {
            if (bestBySection.size() >= targetTotal) {
                break;
            }
            PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec, 6);
            if (routeStep == null) {
                continue;
            }
            int addedWithFts = collectRouteFocusedGuideResultsFts(
                queryTerms,
                routeStep.specTerms,
                routeStep.routeSpec,
                routeStep.categories,
                candidateLimit,
                targetTotal,
                bestBySection
            );
            if (!PackRouteFocusedSearchHelper.shouldBackfillLikeAfterFts(
                queryTerms,
                addedWithFts,
                bestBySection.size(),
                targetTotal
            )) {
                continue;
            }
            collectRouteFocusedGuideResultsLike(
                queryTerms,
                routeStep.specTerms,
                routeStep.routeSpec,
                routeStep.tokens,
                routeStep.categories,
                candidateLimit,
                targetTotal,
                bestBySection
            );
        }
        Log.d(
            TAG,
            "routeGuideSearch query=\"" + queryTerms.queryLower + "\" specs=" + routeSpecs.size() +
                " added=" + Math.max(0, bestBySection.size() - beforeCount) +
                " total=" + bestBySection.size() +
                " totalMs=" + (System.currentTimeMillis() - startedAt)
        );
    }

    private int collectRouteFocusedGuideResultsFts(
        QueryTerms queryTerms,
        QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> categories,
        int candidateLimit,
        int targetTotal,
        LinkedHashMap<String, ScoredSearchResult> bestBySection
    ) {
        if (!ftsAvailable) {
            return 0;
        }
        String ftsQuery = buildFtsQuery(specTerms, ftsSupportsBm25 ? 8 : 4, ftsSupportsBm25);
        if (ftsQuery.isEmpty()) {
            Log.d(TAG, "routeGuideFts.skip query=\"" + queryTerms.queryLower + "\" reason=empty_fts_query");
            return 0;
        }

        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        args.add(ftsQuery);
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }
        int effectiveCandidateLimit = ftsSupportsBm25
            ? candidateLimit
            : Math.min(candidateLimit, queryTerms.routeProfile.isStarterBuildProject() ? 48 : 24);
        PackRouteFocusedSearchHelper.RouteFtsOrderSpec orderSpec = ftsSupportsBm25
            ? new PackRouteFocusedSearchHelper.RouteFtsOrderSpec(
                " ORDER BY bm25(" + ftsTableName + ") ",
                Collections.emptyList(),
                "bm25"
            )
            : PackRouteFocusedSearchHelper.noBm25RouteFtsOrder(queryTerms);
        args.addAll(orderSpec.args);
        args.add(String.valueOf(effectiveCandidateLimit));

        try (Cursor cursor = database.rawQuery(
            "SELECT c.guide_id, c.guide_title, c.section_heading, c.category, c.description, c.document, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags, c.tags " +
                "FROM " + ftsTableName + " f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE " + ftsTableName + " MATCH ? " +
                "AND c.category IN (" + String.join(",", categoryPlaceholders) + ") " +
                orderSpec.clause + " LIMIT ?",
            args.toArray(new String[0])
        )) {
            int added = collectRouteFocusedGuideCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                targetTotal
            );
            Log.d(
                TAG,
                "routeGuideFts query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + ftsQuery +
                    "\" added=" + added + " candidateLimit=" + effectiveCandidateLimit +
                    " order=" + orderSpec.label
            );
            return added;
        } catch (SQLiteException error) {
            Log.w(
                TAG,
                "routeGuideFts.fail query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + ftsQuery + "\"",
                error
            );
            return 0;
        }
    }

    private int collectRouteFocusedGuideResultsLike(
        QueryTerms queryTerms,
        QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> tokens,
        List<String> categories,
        int candidateLimit,
        int targetTotal,
        LinkedHashMap<String, ScoredSearchResult> bestBySection
    ) {
        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }

        ArrayList<String> clauses = new ArrayList<>();
        for (String token : tokens) {
            String like = "%" + token + "%";
            // Guide-level route search is only a supplemental diversity pass. Keep it on
            // lightweight guide metadata instead of scanning full guide bodies again.
            clauses.add("(title LIKE ? OR description LIKE ? OR topic_tags LIKE ?)");
            args.add(like);
            args.add(like);
            args.add(like);
        }
        args.add(String.valueOf(candidateLimit));

        try (Cursor cursor = database.rawQuery(
            "SELECT guide_id, title, category, description, body_markdown, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM guides WHERE category IN (" + String.join(",", categoryPlaceholders) + ") " +
                "AND (" + String.join(" OR ", clauses) + ") LIMIT ?",
            args.toArray(new String[0])
        )) {
            return collectRouteFocusedGuideCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                targetTotal
            );
        }
    }

    private int collectRouteFocusedGuideCursor(
        Cursor cursor,
        QueryTerms queryTerms,
        QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, ScoredSearchResult> bestBySection,
        int targetTotal
    ) {
        return PackRouteFocusedCandidateCollector.collectGuideCursor(
            cursor,
            queryTerms,
            specTerms,
            routeSpec,
            bestBySection,
            targetTotal
        );
    }

    static List<SearchResult> mergeResultsWhenCentroidMissingForTest(
        List<SearchResult> routeResults,
        List<SearchResult> lexicalFallback,
        int limit
    ) {
        return mergeResultsWhenCentroidMissing(routeResults, lexicalFallback, limit);
    }

    static List<String> mergeGuideIdsWithAnchorPriorForTest(
        List<String> lexicalGuideIds,
        List<String> vectorGuideIds,
        String anchorGuideId,
        int turnsSinceAnchor,
        int turnCount,
        Map<String, Double> relatedWeights
    ) {
        LinkedHashMap<String, CombinedHit> merged = new LinkedHashMap<>();
        int rank = 0;
        for (String guideId : lexicalGuideIds) {
            RankedChunk hit = buildAnchorPriorTestChunk(guideId, "lexical", rank);
            CombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new CombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.lexicalRank = Math.min(combined.lexicalRank, rank);
            combined.rrfScore += reciprocalRank(rank);
            rank += 1;
        }
        rank = 0;
        for (String guideId : vectorGuideIds) {
            RankedChunk hit = buildAnchorPriorTestChunk(guideId, "vector", rank);
            CombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new CombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.vectorRank = Math.min(combined.vectorRank, rank);
            combined.vectorScore = Math.max(combined.vectorScore, hit.vectorScore);
            combined.rrfScore += reciprocalRank(rank);
            rank += 1;
        }

        LinkedHashMap<String, Map<String, Double>> weightsByGuide = new LinkedHashMap<>();
        weightsByGuide.put(anchorGuideId, relatedWeights == null ? Collections.emptyMap() : relatedWeights);
        applyAnchorPrior(
            merged,
            new AnchorPriorDirective(anchorGuideId, turnsSinceAnchor, turnCount),
            weightsByGuide
        );

        ArrayList<CombinedHit> ordered = new ArrayList<>(merged.values());
        ordered.sort((left, right) -> Double.compare(right.rrfScore, left.rrfScore));
        ArrayList<String> guideIds = new ArrayList<>();
        for (CombinedHit combined : ordered) {
            guideIds.add(combined.chunk.guideId);
        }
        return guideIds;
    }

    static List<SearchResult> orderRelatedGuidesByWorkflowRelevanceForTest(
        SearchResult anchor,
        List<SearchResult> relatedGuides
    ) {
        return orderRelatedGuidesByWorkflowRelevance(anchor, relatedGuides);
    }

    static int relatedGuideCandidateLimitForTest(int requestedLimit) {
        return relatedGuideCandidateLimit(requestedLimit);
    }

    static String resolveAnchorSnippetForTest(String sessionChunkText, String guideChunkText, String guideBody) {
        return AnchorSnippetFormatter.resolve(sessionChunkText, guideChunkText, guideBody);
    }

    static String firstParagraphSnippetForTest(String guideBody) {
        return AnchorSnippetFormatter.firstParagraph(guideBody);
    }

    private static RankedChunk buildAnchorPriorTestChunk(String guideId, String mode, int rank) {
        return new RankedChunk(
            guideId,
            -1,
            "Guide " + guideId,
            guideId,
            "Section " + rank,
            "test",
            guideId + " body",
            "",
            "",
            "",
            "",
            0.0,
            "lexical".equals(mode) ? rank : Integer.MAX_VALUE,
            "vector".equals(mode) ? rank : Integer.MAX_VALUE,
            "vector".equals(mode) ? (1.0f - (rank * 0.01f)) : Float.NEGATIVE_INFINITY
        );
    }

    private static List<SearchResult> mergeResultsWhenCentroidMissing(
        List<SearchResult> routeResults,
        List<SearchResult> lexicalFallback,
        int limit
    ) {
        return mergePreferredResults(routeResults, lexicalFallback, limit);
    }

    private static List<SearchResult> mergePreferredResults(List<SearchResult> primary, List<SearchResult> secondary, int limit) {
        if (primary.isEmpty()) {
            return secondary;
        }
        LinkedHashMap<String, SearchResult> merged = new LinkedHashMap<>();
        for (SearchResult result : primary) {
            if (merged.size() >= limit) {
                break;
            }
            merged.put(buildGuideSectionKey(result.guideId, result.title, result.sectionHeading), result);
        }
        for (SearchResult result : secondary) {
            if (merged.size() >= limit) {
                break;
            }
            String key = buildGuideSectionKey(result.guideId, result.title, result.sectionHeading);
            if (!merged.containsKey(key)) {
                merged.put(key, result);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private static int relatedGuideCandidateLimit(int requestedLimit) {
        int limit = Math.max(requestedLimit, 1);
        return Math.min(32, Math.max(limit, Math.max(limit * 4, 12)));
    }

    private static ArrayList<SearchResult> orderRelatedGuidesByWorkflowRelevance(
        SearchResult anchor,
        Iterable<SearchResult> relatedGuides
    ) {
        ArrayList<IndexedSearchResult> indexed = new ArrayList<>();
        int index = 0;
        for (SearchResult guide : relatedGuides) {
            if (guide != null) {
                indexed.add(new IndexedSearchResult(guide, index));
            }
            index += 1;
        }
        final String anchorText = relatedGuideText(anchor);
        final boolean fireWorkflow = isFireStartingWorkflow(anchorText);
        final boolean survivalWorkflow = fireWorkflow || isSurvivalWorkflow(anchorText);
        if (!survivalWorkflow) {
            ArrayList<SearchResult> unchanged = new ArrayList<>();
            for (IndexedSearchResult item : indexed) {
                unchanged.add(item.result);
            }
            return unchanged;
        }

        indexed.sort((left, right) -> {
            int leftPriority = relatedWorkflowPriority(anchorText, left.result);
            int rightPriority = relatedWorkflowPriority(anchorText, right.result);
            if (leftPriority != rightPriority) {
                return Integer.compare(rightPriority, leftPriority);
            }
            return Integer.compare(left.index, right.index);
        });

        ArrayList<SearchResult> ordered = new ArrayList<>();
        for (IndexedSearchResult item : indexed) {
            ordered.add(item.result);
        }
        return ordered;
    }

    private static int relatedWorkflowPriority(String anchorText, SearchResult guide) {
        String text = relatedGuideText(guide);
        int score = 0;

        if (isFireStartingWorkflow(anchorText)) {
            score += markerScore(
                text,
                140,
                "fire by friction", "friction fire", "fire-starting", "fire starting",
                "bow drill", "hand drill", "fire plow", "tinder", "kindling", "ember"
            );
            score += markerScore(
                text,
                95,
                "survival basics", "first 72 hours", "temperate forest survival",
                "winter survival", "primitive technology"
            );
            score += markerScore(
                text,
                80,
                "daily cooking fire", "fire management", "cookstove", "stove",
                "charcoal", "fuel", "combustion", "flame", "fire suppression"
            );
            score += markerScore(text, 35, "wood selection", "woodcarving", "dry wood", "bark");
            score -= markerScore(
                text,
                120,
                "agriculture", "gardening", "animal husbandry", "veterinary",
                "livestock", "breeding", "pasture", "zoology", "butchering"
            );
            score -= markerScore(text, 70, "black powder", "blasting", "weapons", "martial arts");
        } else if (isSurvivalWorkflow(anchorText)) {
            score += markerScore(
                text,
                110,
                "fire by friction", "fire-starting", "water purification", "water storage",
                "primitive shelter", "shelter construction", "go-bag", "search rescue",
                "night navigation", "quick reference", "winter survival", "fire suppression"
            );
            score += markerScore(text, 55, "first aid", "triage", "disaster", "family emergency");
            score -= markerScore(
                text,
                90,
                "agriculture", "gardening", "animal husbandry", "veterinary",
                "livestock", "breeding", "pasture", "zoology"
            );
            score -= markerScore(text, 45, "weapons", "martial arts", "butchering", "trapping");
        }

        return score;
    }

    private static int markerScore(String text, int weight, String... markers) {
        for (String marker : markers) {
            if (text.contains(marker)) {
                return weight;
            }
        }
        return 0;
    }

    private static boolean isFireStartingWorkflow(String text) {
        return text.contains("gd-343")
            || text.contains("fire by friction")
            || text.contains("friction fire")
            || text.contains("fire-starting")
            || text.contains("fire starting")
            || text.contains("bow drill")
            || text.contains("hand drill");
    }

    private static boolean isSurvivalWorkflow(String text) {
        return text.contains("gd-023")
            || text.contains("survival basics")
            || text.contains("first 72 hours")
            || text.contains("primitive shelter")
            || text.contains("winter survival")
            || text.contains("temperate forest survival");
    }

    private static String relatedGuideText(SearchResult result) {
        if (result == null) {
            return "";
        }
        return (
            emptySafe(result.guideId) + " " +
            emptySafe(result.title) + " " +
            emptySafe(result.subtitle) + " " +
            emptySafe(result.sectionHeading) + " " +
            emptySafe(result.category) + " " +
            emptySafe(result.contentRole) + " " +
            emptySafe(result.structureType) + " " +
            emptySafe(result.topicTags) + " " +
            emptySafe(result.snippet)
        ).toLowerCase(QUERY_LOCALE);
    }

    private SearchResult selectAnswerAnchor(QueryTerms queryTerms, List<SearchResult> rankedResults) {
        if (rankedResults == null || rankedResults.isEmpty()) {
            return null;
        }

        boolean enforceDirectSignal = false;
        LinkedHashMap<String, GuideScore> guides = buildAnchorGuideScores(queryTerms, rankedResults, false);
        if (shouldRequireDirectAnchorSignal(queryTerms)) {
            LinkedHashMap<String, GuideScore> directSignalGuides = buildAnchorGuideScores(queryTerms, rankedResults, true);
            if (!directSignalGuides.isEmpty()) {
                guides = directSignalGuides;
                enforceDirectSignal = true;
            }
        }
        SearchResult explicitDistributionAnchor = selectExplicitWaterDistributionAnchor(queryTerms, rankedResults);
        if (explicitDistributionAnchor != null) {
            return explicitDistributionAnchor;
        }
        SearchResult explicitWaterStorageAnchor = selectExplicitWaterStorageAnchor(queryTerms, rankedResults);
        if (explicitWaterStorageAnchor != null) {
            return explicitWaterStorageAnchor;
        }
        SearchResult broadHouseAnchor = selectBroadHouseAnchor(queryTerms, rankedResults);
        if (broadHouseAnchor != null) {
            return broadHouseAnchor;
        }
        SearchResult specializedStructuredAnchor = selectSpecializedStructuredAnchor(queryTerms, rankedResults);
        if (specializedStructuredAnchor != null) {
            return specializedStructuredAnchor;
        }

        if (guides.isEmpty()) {
            return rankedResults.get(0);
        }

        ArrayList<GuideScore> ordered = new ArrayList<>(guides.values());
        ordered.sort((left, right) -> {
            int totalOrder = Integer.compare(right.totalScore, left.totalScore);
            if (totalOrder != 0) {
                return totalOrder;
            }
            int sectionOrder = Integer.compare(right.sectionKeys.size(), left.sectionKeys.size());
            if (sectionOrder != 0) {
                return sectionOrder;
            }
            return Integer.compare(right.bestScore, left.bestScore);
        });

        SearchResult rankedAnchor = ordered.isEmpty() ? rankedResults.get(0) : ordered.get(0).anchor;
        if (!queryTerms.routeProfile.isRouteFocused()) {
            return rankedAnchor;
        }

        SearchResult routedAnchor = findRouteFocusedAnchor(queryTerms, rankedResults, enforceDirectSignal);
        if (routedAnchor == null) {
            return rankedAnchor;
        }
        return chooseRankedOrRoutedAnchor(queryTerms, rankedAnchor, routedAnchor);
    }

    private static boolean shouldPreferRouteAnchorOverRankedGuide(QueryTerms queryTerms, SearchResult rankedAnchor) {
        return AnswerAnchorPolicy.shouldPreferRouteAnchorOverRankedGuide(
            queryTerms.metadataProfile.preferredStructureType(),
            rankedAnchor
        );
    }

    static boolean shouldPreferRouteAnchorOverRankedGuideForTest(String query, SearchResult rankedAnchor) {
        return shouldPreferRouteAnchorOverRankedGuide(QueryTerms.fromQuery(query), rankedAnchor);
    }

    /**
     * Test-only seam over the ranked-vs-routed anchor tiebreak.
     * Callers must supply the already-selected non-null ranked and routed anchor candidates.
     */
    static SearchResult selectAnswerAnchorForTest(String query, SearchResult rankedAnchor, SearchResult routedAnchor) {
        return chooseRankedOrRoutedAnchor(QueryTerms.fromQuery(query), rankedAnchor, routedAnchor);
    }

    private static SearchResult chooseRankedOrRoutedAnchor(
        QueryTerms queryTerms,
        SearchResult rankedAnchor,
        SearchResult routedAnchor
    ) {
        if (!queryTerms.routeProfile.isRouteFocused()) {
            return rankedAnchor;
        }
        boolean rankedGuideFocusWaterDistributionFallback =
            queryTerms.metadataProfile.hasExplicitTopic("water_distribution")
                && "guide-focus".equals(emptySafe(rankedAnchor.retrievalMode).trim().toLowerCase(QUERY_LOCALE))
                && emptySafe(rankedAnchor.sectionHeading).trim().isEmpty()
                && !hasWaterDistributionTitleSignal(rankedAnchor);
        return AnswerAnchorPolicy.chooseRankedOrRoutedAnchor(
            AnswerAnchorPolicy.anchorChoice(rankedAnchor, routedAnchor)
                .routeFocused(queryTerms.routeProfile.isRouteFocused())
                .preferRouteAnchorOverRankedGuide(shouldPreferRouteAnchorOverRankedGuide(queryTerms, rankedAnchor))
                .preferCabinSiteSelectionRouteAnchor(prefersCabinSiteSelectionRouteAnchor(queryTerms))
                .routedHasCabinSiteSelectionSignal(hasCabinSiteSelectionAnchorSignal(routedAnchor))
                .rankedHasCabinSiteSelectionSignal(hasCabinSiteSelectionAnchorSignal(rankedAnchor))
                .preferRoofWeatherproofRouteAnchor(prefersRoofWeatherproofRouteAnchor(queryTerms))
                .routedHasRoofWeatherproofSignal(hasRoofWeatherproofAnchorSignal(routedAnchor))
                .rankedHasRoofWeatherproofDistractorSignal(hasRoofWeatherproofDistractorSignal(rankedAnchor))
                .rankedHasRoofWeatherproofSignal(hasRoofWeatherproofAnchorSignal(rankedAnchor))
                .rankedGuideFocusWaterDistributionFallback(rankedGuideFocusWaterDistributionFallback)
                .sameGuideGroup(
                    PackSupportScoringPolicy.guideGroupKey(routedAnchor)
                        .equals(PackSupportScoringPolicy.guideGroupKey(rankedAnchor))
                )
                .rankedSupportWithMetadata(
                    PackSupportScoringPolicy.supportBreakdown(queryTerms, rankedAnchor).supportWithMetadata()
                )
                .routedSupportWithMetadata(
                    PackSupportScoringPolicy.supportBreakdown(queryTerms, routedAnchor).supportWithMetadata()
                )
                .build()
        );
    }

    static SearchResult selectExplicitWaterDistributionAnchorForTest(String query, SearchResult... results) {
        ArrayList<SearchResult> rankedResults = new ArrayList<>();
        if (results != null) {
            Collections.addAll(rankedResults, results);
        }
        return selectExplicitWaterDistributionAnchor(QueryTerms.fromQuery(query), rankedResults);
    }

    static SearchResult selectExplicitWaterStorageAnchorForTest(String query, SearchResult... results) {
        ArrayList<SearchResult> rankedResults = new ArrayList<>();
        if (results != null) {
            Collections.addAll(rankedResults, results);
        }
        return selectExplicitWaterStorageAnchor(QueryTerms.fromQuery(query), rankedResults);
    }

    static SearchResult selectSpecializedStructuredAnchorForTest(String query, SearchResult... results) {
        ArrayList<SearchResult> rankedResults = new ArrayList<>();
        if (results != null) {
            Collections.addAll(rankedResults, results);
        }
        return selectSpecializedStructuredAnchor(QueryTerms.fromQuery(query), rankedResults);
    }

    static SearchResult selectBroadHouseAnchorForTest(String query, SearchResult... results) {
        ArrayList<SearchResult> rankedResults = new ArrayList<>();
        if (results != null) {
            Collections.addAll(rankedResults, results);
        }
        return selectBroadHouseAnchor(QueryTerms.fromQuery(query), rankedResults);
    }

    private static SearchResult selectExplicitWaterDistributionAnchor(
        QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        if (!queryTerms.metadataProfile.hasExplicitTopic("water_distribution")
            || rankedResults == null
            || rankedResults.isEmpty()) {
            return null;
        }

        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            if (!hasDirectAnchorSignal(queryTerms, candidate)) {
                continue;
            }

            int score = Math.max(1, PackSupportScoringPolicy.supportBreakdown(queryTerms, candidate).supportWithMetadata())
                + Math.max(0, 12 - index);
            String category = emptySafe(candidate.category).trim().toLowerCase(QUERY_LOCALE);
            if ("building".equals(category)) {
                score += 10;
            } else if ("utility".equals(category)) {
                score += 8;
            }
            if (hasWaterDistributionTitleSignal(candidate)) {
                score += 12;
            }
            score += waterDistributionAnchorFocusBonus(candidate);

            String retrievalMode = emptySafe(candidate.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            if ("route-focus".equals(retrievalMode)) {
                score += 10;
            } else if ("guide-focus".equals(retrievalMode)) {
                score += emptySafe(candidate.sectionHeading).trim().isEmpty() ? -4 : 12;
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private static SearchResult selectExplicitWaterStorageAnchor(
        QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        return PackStructuredAnchorPolicy.selectExplicitWaterStorageAnchor(queryTerms, rankedResults);
    }

    private static SearchResult selectSpecializedStructuredAnchor(
        QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        return PackStructuredAnchorPolicy.selectSpecializedStructuredAnchor(queryTerms, rankedResults);
    }

    private static SearchResult selectBroadHouseAnchor(
        QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        return PackStructuredAnchorPolicy.selectBroadHouseAnchor(queryTerms, rankedResults);
    }

    static LinkedHashMap<String, GuideScore> buildAnchorGuideScoresForTest(
        String query,
        List<SearchResult> rankedResults,
        boolean requireDirectSignal
    ) {
        return buildAnchorGuideScores(QueryTerms.fromQuery(query), rankedResults, requireDirectSignal);
    }

    private static LinkedHashMap<String, GuideScore> buildAnchorGuideScores(
        QueryTerms queryTerms,
        List<SearchResult> rankedResults,
        boolean requireDirectSignal
    ) {
        LinkedHashMap<String, GuideScore> guides = new LinkedHashMap<>();
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult result = rankedResults.get(index);
            if (!isSpecializedExplicitAnchorCandidate(queryTerms, result)) {
                continue;
            }
            int support = PackSupportScoringPolicy.supportBreakdown(queryTerms, result).supportWithMetadata();
            if (support <= 0) {
                continue;
            }
            if (requireDirectSignal && !hasDirectAnchorSignal(queryTerms, result)) {
                continue;
            }
            String key = PackSupportScoringPolicy.guideGroupKey(result);
            GuideScore guide = guides.get(key);
            if (guide == null) {
                guide = new GuideScore(result);
                guides.put(key, guide);
            }
            int score = support;
            score += Math.max(0, 12 - index);
            score += PackSupportScoringPolicy.anchorAlignmentBonus(queryTerms, result);
            String mode = emptySafe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            if ("route-focus".equals(mode)) {
                score += 8;
            } else if ("hybrid".equals(mode)) {
                score += 4;
            } else if ("guide-focus".equals(mode)) {
                score += 3;
            } else if ("lexical".equals(mode)) {
                score += 2;
            }
            guide.totalScore += score;
            guide.bestScore = Math.max(guide.bestScore, score);
            guide.sectionKeys.add(buildGuideSectionKey(result.guideId, result.title, result.sectionHeading));
            if (score > guide.anchorScore) {
                guide.anchorScore = score;
                guide.anchor = result;
            }
        }
        return guides;
    }

    static boolean shouldRequireDirectAnchorSignalForTest(String query) {
        return shouldRequireDirectAnchorSignal(QueryTerms.fromQuery(query));
    }

    private static boolean shouldRequireDirectAnchorSignal(QueryTerms queryTerms) {
        return RetrievalRoutePolicy.shouldRequireDirectAnchorSignal(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            queryTerms.primaryKeywordTokens().size()
        );
    }

    static boolean requiresSpecializedRouteAnchorSignal(String preferredStructureType) {
        return RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(preferredStructureType);
    }

    static boolean hasDirectAnchorSignalForTest(String query, SearchResult result) {
        return hasDirectAnchorSignal(QueryTerms.fromQuery(query), result);
    }

    static boolean shouldKeepSpecializedDirectSignalRouteResultForTest(String query, SearchResult result) {
        return shouldKeepSpecializedDirectSignalRouteResult(QueryTerms.fromQuery(query), result);
    }

    static boolean shouldKeepSpecializedDirectSignalRouteResult(QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return true;
        }
        String preferredStructureType = queryTerms.metadataProfile.preferredStructureType();
        if (!"soapmaking".equals(preferredStructureType) || !shouldRequireDirectAnchorSignal(queryTerms)) {
            return true;
        }
        return hasDirectAnchorSignal(queryTerms, result);
    }

    static boolean hasDirectAnchorSignal(QueryTerms queryTerms, SearchResult result) {
        if (queryTerms.metadataProfile.hasExplicitTopic("water_distribution")) {
            boolean distributionTagged = SpecializedAnchorCandidatePolicy.hasTopicTag(result, "water_distribution");
            if (!distributionTagged) {
                return false;
            }
            boolean titleSignal = hasWaterDistributionTitleSignal(result);
            String retrievalMode = emptySafe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            if ("guide-focus".equals(retrievalMode) && emptySafe(result.sectionHeading).trim().isEmpty()) {
                return PackRouteSignalPolicy.hasStrongWaterDistributionGuideSignal(result);
            }
            if (queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading) > 0) {
                return true;
            }
            String category = emptySafe(result.category).trim().toLowerCase(QUERY_LOCALE);
            if (!"building".equals(category) && !"utility".equals(category)) {
                return false;
            }
            return titleSignal || PackRouteSignalPolicy.hasWaterDistributionDetailSignal(result);
        }
        if (queryTerms.metadataProfile.hasExplicitTopicFocus()) {
            if (SpecializedAnchorCandidatePolicy.hasDirectSectionHeadingSignal(queryTerms.metadataProfile, result)) {
                return true;
            }
            if (!SpecializedAnchorCandidatePolicy.hasDirectExplicitTopicOverlap(queryTerms.metadataProfile, result)) {
                return false;
            }
            String preferredStructureType = queryTerms.metadataProfile.preferredStructureType();
            if ("soapmaking".equals(preferredStructureType)) {
                return PackRouteSignalPolicy.hasStrongSoapmakingGuideSignal(result);
            }
            if (requiresSpecializedRouteAnchorSignal(preferredStructureType)) {
                if (SpecializedAnchorCandidatePolicy.isBlankGuideFocusOutsidePreferredStructure(
                    queryTerms.metadataProfile,
                    result
                )) {
                    return false;
                }
                int lexicalScore = lexicalKeywordScore(
                    queryTerms,
                    result.title,
                    result.sectionHeading,
                    result.category,
                    "",
                    "",
                    ""
                );
                return lexicalScore > 0;
            }
            return true;
        }
        int lexicalScore = lexicalKeywordScore(
            queryTerms,
            result.title,
            result.sectionHeading,
            result.category,
            result.topicTags,
            result.snippet,
            result.body
        );
        if (lexicalScore > 0) {
            return true;
        }
        if (queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading) > 0) {
            return true;
        }
        return SpecializedAnchorCandidatePolicy.hasDirectExplicitTopicOverlap(queryTerms.metadataProfile, result);
    }

    static boolean matchesSpecializedRouteMetadataForTest(String query, SearchResult result) {
        return matchesSpecializedRouteMetadata(
            QueryTerms.fromQuery(query),
            result == null ? "" : result.sectionHeading,
            result == null ? "" : result.structureType,
            result == null ? "" : result.topicTags
        );
    }

    static int broadWaterRouteRefinementBonusForTest(String query, SearchResult result) {
        return broadWaterRouteRefinementBonus(QueryTerms.fromQuery(query), result);
    }

    static boolean matchesSpecializedRouteMetadata(
        QueryTerms queryTerms,
        String sectionHeading,
        String structureType,
        String topicTags
    ) {
        return SpecializedAnchorCandidatePolicy.matchesRouteMetadata(
            queryTerms == null ? null : queryTerms.metadataProfile,
            sectionHeading,
            structureType,
            topicTags
        );
    }

    private static int broadWaterRouteRefinementBonus(QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")) {
            return 0;
        }

        int overlap = metadataProfile.preferredTopicOverlapCount(result.topicTags);
        String normalizedRole = emptySafe(result.contentRole).trim().toLowerCase(QUERY_LOCALE);
        String normalizedMode = emptySafe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        String normalizedTitle = emptySafe(result.title).trim().toLowerCase(QUERY_LOCALE);
        String normalizedSection = emptySafe(result.sectionHeading).trim().toLowerCase(QUERY_LOCALE);
        boolean containerMakingIntent = hasWaterStorageContainerMakingIntent(queryTerms.queryLower);

        int score = 0;
        if (("planning".equals(normalizedRole) || "subsystem".equals(normalizedRole)) && overlap >= 2) {
            score += 24;
        } else if ("safety".equals(normalizedRole) && overlap >= 2) {
            score += 12;
        }
        if ("guide-focus".equals(normalizedMode) && overlap >= 2) {
            score += 18;
        }
        if (overlap >= 2
            && (normalizedSection.contains("container")
                || normalizedSection.contains("rotation")
                || normalizedSection.contains("inspection")
                || normalizedSection.contains("storage strategy")
                || normalizedSection.contains("purification"))) {
            score += 12;
        }
        if (!containerMakingIntent
            && (normalizedTitle.contains("making")
                || normalizedTitle.contains("container")
                || normalizedTitle.contains("vessel"))) {
            score -= 22;
        }
        if (!containerMakingIntent
            && "guide-focus".equals(normalizedMode)
            && emptySafe(result.sectionHeading).trim().isEmpty()
            && ("building".equals(emptySafe(result.category).trim().toLowerCase(QUERY_LOCALE))
                || "crafts".equals(emptySafe(result.category).trim().toLowerCase(QUERY_LOCALE)))) {
            score -= 14;
        }
        if ("starter".equals(normalizedRole) && overlap < 2) {
            score -= 22;
        }
        if ("route-focus".equals(normalizedMode) && overlap < 2) {
            score -= 12;
        }
        if (normalizedTitle.contains("inventory") && overlap < 2) {
            score -= 32;
        }
        return score;
    }

    static int broadWaterRouteOrderingPriorityForTest(String query, SearchResult result) {
        return broadWaterRouteOrderingPriority(QueryTerms.fromQuery(query), result);
    }

    private static int broadWaterRouteOrderingPriority(QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")) {
            return 0;
        }

        int overlap = metadataProfile.preferredTopicOverlapCount(result.topicTags);
        String normalizedMode = emptySafe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        int priority = broadWaterRouteRefinementBonus(queryTerms, result) * 4 + overlap;
        if ("guide-focus".equals(normalizedMode) && overlap >= 2) {
            priority += 4;
        }
        if ("route-focus".equals(normalizedMode) && overlap < 2) {
            priority -= 2;
        }
        return priority;
    }

    private static boolean hasWaterStorageContainerMakingIntent(String queryLower) {
        return PackStructuredAnchorPolicy.hasWaterStorageContainerMakingIntent(queryLower);
    }

    static boolean shouldKeepBroadWaterRouteRowForTest(String query, SearchResult result) {
        QueryTerms queryTerms = QueryTerms.fromQuery(query);
        int sectionHeadingScore = queryTerms.metadataProfile.sectionHeadingBonus(
            result == null ? "" : result.sectionHeading
        );
        return shouldKeepBroadWaterRouteRow(
            queryTerms,
            result == null ? "" : result.sectionHeading,
            result == null ? "" : result.contentRole,
            result == null ? "" : result.structureType,
            result == null ? "" : result.topicTags,
            sectionHeadingScore
        );
    }

    static boolean shouldKeepBroadHouseRouteRowForTest(String query, SearchResult result) {
        QueryTerms queryTerms = QueryTerms.fromQuery(query);
        int sectionHeadingScore = queryTerms.metadataProfile.sectionHeadingBonus(
            result == null ? "" : result.sectionHeading
        );
        return shouldKeepBroadHouseRouteRow(
            queryTerms,
            result == null ? "" : result.sectionHeading,
            result == null ? "" : result.category,
            result == null ? "" : result.structureType,
            result == null ? "" : result.topicTags,
            sectionHeadingScore
        );
    }

    static boolean shouldKeepBroadWaterRouteRow(
        QueryTerms queryTerms,
        String sectionHeading,
        String contentRole,
        String structureType,
        String topicTags,
        int sectionHeadingScore
    ) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return true;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")) {
            return true;
        }
        if (sectionHeadingScore < 0) {
            return false;
        }

        boolean specializedWaterTopic = containsTerm(topicTags, "container_sanitation")
            || containsTerm(topicTags, "water_rotation")
            || containsTerm(topicTags, "disinfection");
        String normalizedRole = emptySafe(contentRole).trim().toLowerCase(QUERY_LOCALE);
        String normalizedHeading = emptySafe(sectionHeading).trim().toLowerCase(QUERY_LOCALE);
        if (sectionHeadingScore == 0 && !specializedWaterTopic) {
            return false;
        }
        if (!specializedWaterTopic
            && "starter".equals(normalizedRole)
            && (normalizedHeading.contains("water storage")
                || normalizedHeading.contains("purification"))) {
            return false;
        }
        return true;
    }

    static boolean shouldKeepBroadHouseRouteRow(
        QueryTerms queryTerms,
        String sectionHeading,
        String category,
        String structureType,
        String topicTags,
        int sectionHeadingScore
    ) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return true;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"cabin_house".equals(metadataProfile.preferredStructureType())) {
            return true;
        }
        if (metadataProfile.hasExplicitTopicFocus()) {
            return sectionHeadingScore > 0;
        }
        if (sectionHeadingScore < 0) {
            return false;
        }

        String normalizedCategory = emptySafe(category).trim().toLowerCase(QUERY_LOCALE);
        String normalizedStructure = emptySafe(structureType).trim().toLowerCase(QUERY_LOCALE);
        int overlap = metadataProfile.preferredTopicOverlapCount(topicTags);
        if (sectionHeadingScore == 0
            && !"building".equals(normalizedCategory)
            && !"cabin_house".equals(normalizedStructure)
            && overlap < 2) {
            return false;
        }
        return true;
    }

    private static boolean shouldScanFullRouteCursor(QueryTerms queryTerms) {
        return PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(queryTerms);
    }

    static boolean shouldScanFullRouteCursorForTest(String query) {
        return shouldScanFullRouteCursor(QueryTerms.fromQuery(query));
    }

    static SearchResult routeFocusedAnchorForTest(String query, List<SearchResult> rankedResults, boolean requireDirectSignal) {
        return findRouteFocusedAnchor(QueryTerms.fromQuery(query), rankedResults, requireDirectSignal);
    }

    private static SearchResult findRouteFocusedAnchor(
        QueryTerms queryTerms,
        List<SearchResult> rankedResults,
        boolean requireDirectSignal
    ) {
        return PackRouteFocusedAnchorPolicy.selectRouteFocusedAnchor(queryTerms, rankedResults, requireDirectSignal);
    }

    private static boolean prefersCabinSiteSelectionRouteAnchor(QueryTerms queryTerms) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return false;
        }
        return "cabin_house".equals(queryTerms.metadataProfile.preferredStructureType())
            && queryTerms.metadataProfile.siteSelectionLeadIntent()
            && queryTerms.metadataProfile.hasExplicitTopic("site_selection")
            && queryTerms.metadataProfile.hasExplicitTopic("foundation");
    }

    private static boolean hasCabinSiteSelectionAnchorSignal(SearchResult candidate) {
        if (candidate == null) {
            return false;
        }
        String normalized = normalizeMatchText(
            emptySafe(candidate.title) + " " + emptySafe(candidate.sectionHeading)
        );
        return containsAnyMarker(normalized, HOUSE_SITE_SELECTION_ANCHOR_MARKERS);
    }

    static int cabinSiteSelectionAnchorBias(QueryTerms queryTerms, SearchResult candidate) {
        if (!prefersCabinSiteSelectionRouteAnchor(queryTerms) || candidate == null) {
            return 0;
        }
        int score = 0;
        String normalizedCategory = emptySafe(candidate.category).trim().toLowerCase(QUERY_LOCALE);
        boolean siteSignal = hasCabinSiteSelectionAnchorSignal(candidate);
        boolean foundationOnlySignal = containsAnyMarker(
            normalizeMatchText(emptySafe(candidate.title) + " " + emptySafe(candidate.sectionHeading)),
            HOUSE_FOUNDATION_DETAIL_MARKERS
        );
        if (siteSignal) {
            score += 16;
            if ("survival".equals(normalizedCategory)) {
                score += 4;
            }
        }
        if (foundationOnlySignal && !siteSignal) {
            score -= 6;
        }
        return score;
    }

    private static boolean prefersRoofWeatherproofRouteAnchor(QueryTerms queryTerms) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return false;
        }
        return PackRouteSignalPolicy.prefersRoofWeatherproofContext(queryTerms.metadataProfile);
    }

    private static boolean prefersRoofWeatherproofContext(QueryMetadataProfile metadataProfile) {
        return PackRouteSignalPolicy.prefersRoofWeatherproofContext(metadataProfile);
    }

    private static boolean hasRoofWeatherproofAnchorSignal(SearchResult candidate) {
        return PackRouteSignalPolicy.hasRoofWeatherproofAnchorSignal(candidate);
    }

    private static boolean hasRoofWeatherproofDistractorSignal(SearchResult candidate) {
        return PackRouteSignalPolicy.hasRoofWeatherproofDistractorSignal(candidate);
    }

    static int roofWeatherproofAnchorBias(QueryTerms queryTerms, SearchResult candidate) {
        if (!prefersRoofWeatherproofRouteAnchor(queryTerms) || candidate == null) {
            return 0;
        }
        int score = 0;
        if (hasRoofWeatherproofAnchorSignal(candidate)) {
            score += 16;
        }
        if (hasRoofWeatherproofDistractorSignal(candidate) && !hasRoofWeatherproofAnchorSignal(candidate)) {
            score -= 14;
        }
        return score;
    }

    private static boolean prefersGovernanceTrustRepairContext(QueryMetadataProfile metadataProfile) {
        return PackRouteSignalPolicy.prefersGovernanceTrustRepairContext(metadataProfile);
    }

    private static boolean hasGovernanceTrustRepairSignal(SearchResult candidate) {
        return PackRouteSignalPolicy.hasGovernanceTrustRepairSignal(candidate);
    }

    private static boolean hasGovernanceSupportMixDistractor(SearchResult candidate) {
        return PackRouteSignalPolicy.hasGovernanceSupportMixDistractor(candidate);
    }

    static boolean matchesSpecializedExplicitTopicRowForTest(String query, String structureType, String topicTags) {
        return matchesSpecializedExplicitTopicRow(QueryTerms.fromQuery(query), structureType, topicTags);
    }

    static boolean matchesSpecializedExplicitTopicRow(
        QueryTerms queryTerms,
        String structureType,
        String topicTags
    ) {
        return SpecializedAnchorCandidatePolicy.matchesExplicitTopicRow(
            queryTerms == null ? null : queryTerms.metadataProfile,
            structureType,
            topicTags
        );
    }

    static boolean isSpecializedExplicitAnchorCandidate(QueryTerms queryTerms, SearchResult result) {
        QueryMetadataProfile metadataProfile = queryTerms == null ? null : queryTerms.metadataProfile;
        boolean strongSoapmakingSignal = metadataProfile != null
            && "soapmaking".equals(metadataProfile.preferredStructureType())
            && PackRouteSignalPolicy.hasStrongSoapmakingGuideSignal(result);
        return SpecializedAnchorCandidatePolicy.isSpecializedExplicitAnchorCandidate(
            metadataProfile,
            result,
            strongSoapmakingSignal
        );
    }

    static boolean isSpecializedExplicitAnchorCandidateForTest(String query, SearchResult result) {
        return isSpecializedExplicitAnchorCandidate(QueryTerms.fromQuery(query), result);
    }

    private List<RankedChunk> searchWithFtsHits(QueryTerms queryTerms, int limit) {
        String ftsQuery = buildFtsQuery(queryTerms);
        if (ftsQuery.isEmpty()) {
            return Collections.emptyList();
        }

        String orderClause = ftsSupportsBm25 ? " ORDER BY bm25(" + ftsTableName + ") " : " ";
        ArrayList<RankedChunk> results = new ArrayList<>();
        try (Cursor cursor = database.rawQuery(
            "SELECT c.chunk_id, c.vector_row_id, c.guide_title, c.guide_id, c.section_heading, c.category, c.document, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags " +
                "FROM " + ftsTableName + " f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE " + ftsTableName + " MATCH ? " +
                orderClause +
                "LIMIT ?",
            new String[]{ftsQuery, String.valueOf(limit)}
        )) {
            int rank = 0;
            while (cursor.moveToNext()) {
                results.add(new RankedChunk(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    emptySafe(cursor.getString(3)),
                    emptySafe(cursor.getString(4)),
                    emptySafe(cursor.getString(5)),
                    emptySafe(cursor.getString(6)),
                    emptySafe(cursor.getString(7)),
                    emptySafe(cursor.getString(8)),
                    emptySafe(cursor.getString(9)),
                    emptySafe(cursor.getString(10)),
                    0.0,
                    rank,
                    -1,
                    0f
                ));
                rank += 1;
            }
        }
        return results;
    }

    private List<SearchResult> loadGuideSectionsForAnswer(QueryTerms queryTerms, SearchResult anchor, int limit) {
        if (anchor == null || emptySafe(anchor.guideId).isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashMap<String, SectionCandidate> sections = new LinkedHashMap<>();
        try (Cursor cursor = database.rawQuery(
            "SELECT guide_title, guide_id, section_heading, category, document, tags, description, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE guide_id = ?",
            new String[]{anchor.guideId}
        )) {
            while (cursor.moveToNext()) {
                String title = emptySafe(cursor.getString(0));
                String guideId = emptySafe(cursor.getString(1));
                String section = emptySafe(cursor.getString(2));
                String category = emptySafe(cursor.getString(3));
                String document = emptySafe(cursor.getString(4));
                String tags = emptySafe(cursor.getString(5));
                String description = emptySafe(cursor.getString(6));
                String contentRole = emptySafe(cursor.getString(7));
                String timeHorizon = emptySafe(cursor.getString(8));
                String structureType = emptySafe(cursor.getString(9));
                String topicTags = emptySafe(cursor.getString(10));

                int score = lexicalKeywordScore(queryTerms, title, section, category, tags, description, document);
                score += metadataBonus(queryTerms, category, contentRole, timeHorizon, structureType, topicTags);
                int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(section);
                score += sectionBonus;
                boolean isAnchorSection = normalizeSection(section).equals(normalizeSection(anchor.sectionHeading));
                SearchResult candidatePreview = new SearchResult(
                    title,
                    guideId + " | " + category + " | " + section + " | guide-focus",
                    clip(document, 220),
                    document,
                    guideId,
                    section,
                    category,
                    "guide-focus",
                    contentRole,
                    timeHorizon,
                    structureType,
                    topicTags
                );
                if (!shouldKeepGuideSectionForContext(queryTerms, candidatePreview, isAnchorSection, sectionBonus)) {
                    continue;
                }
                if (isAnchorSection) {
                    score += 40;
                }
                if (score <= 0) {
                    continue;
                }

                String key = normalizeSection(section);
                SectionCandidate candidate = sections.get(key);
                if (candidate == null) {
                    candidate = new SectionCandidate(title, guideId, section, category, contentRole, timeHorizon, structureType, topicTags);
                    sections.put(key, candidate);
                }
                candidate.consider(score, document, isAnchorSection, contentRole, timeHorizon, structureType, topicTags);
            }
        }

        ArrayList<SectionCandidate> ordered = new ArrayList<>(sections.values());
        ordered.sort((left, right) -> {
            int scoreOrder = Integer.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int anchorOrder = Boolean.compare(right.anchorSection, left.anchorSection);
            if (anchorOrder != 0) {
                return anchorOrder;
            }
            return left.sectionHeading.compareToIgnoreCase(right.sectionHeading);
        });

        ArrayList<SearchResult> results = new ArrayList<>();
        for (int index = 0; index < ordered.size() && results.size() < Math.max(limit, 1); index++) {
            SectionCandidate candidate = ordered.get(index);
            results.add(new SearchResult(
                candidate.guideTitle,
                candidate.guideId + " | " + candidate.category + " | " + candidate.sectionHeading + " | guide-focus",
                clip(candidate.body.toString(), 220),
                candidate.body.toString(),
                candidate.guideId,
                candidate.sectionHeading,
                candidate.category,
                "guide-focus",
                candidate.contentRole,
                candidate.timeHorizon,
                candidate.structureType,
                candidate.topicTags
            ));
        }
        return results;
    }

    static boolean shouldKeepGuideSectionForContextForTest(String query, SearchResult candidate, boolean isAnchorSection) {
        QueryTerms queryTerms = QueryTerms.fromQuery(query);
        int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(candidate.sectionHeading);
        return shouldKeepGuideSectionForContext(queryTerms, candidate, isAnchorSection, sectionBonus);
    }

    private static boolean shouldKeepGuideSectionForContext(
        QueryTerms queryTerms,
        SearchResult candidate,
        boolean isAnchorSection,
        int sectionBonus
    ) {
        String preferredStructure = emptySafe(queryTerms.metadataProfile.preferredStructureType()).trim().toLowerCase(QUERY_LOCALE);
        return PackAnswerContextPolicy.shouldKeepGuideSectionForContext(
            preferredStructure,
            isAnchorSection,
            sectionBonus,
            prefersRoofWeatherproofRouteAnchor(queryTerms),
            hasRoofWeatherproofDistractorSignal(candidate),
            prefersGovernanceTrustRepairContext(queryTerms.metadataProfile),
            hasGovernanceSupportMixDistractor(candidate),
            hasGovernanceTrustRepairSignal(candidate)
        );
    }

    static SupportBreakdown supportBreakdownForTest(String query, SearchResult result) {
        return new SupportBreakdown(
            PackSupportScoringPolicy.supportBreakdown(QueryTerms.fromQuery(query), result)
        );
    }

    static final class SupportBreakdown extends PackSupportScoringPolicy.SupportBreakdown {
        SupportBreakdown(
            int lexicalSupport,
            int metadataBonus,
            int specializedTopicBonus,
            int sectionBonus,
            int structurePenalty
        ) {
            super(lexicalSupport, metadataBonus, specializedTopicBonus, sectionBonus, structurePenalty);
        }

        SupportBreakdown(PackSupportScoringPolicy.SupportBreakdown breakdown) {
            this(
                breakdown.lexicalSupport,
                breakdown.metadataBonus,
                breakdown.specializedTopicBonus,
                breakdown.sectionBonus,
                breakdown.structurePenalty
            );
        }
    }

    static int anchorAlignmentBonusForTest(String query, SearchResult result) {
        return PackSupportScoringPolicy.anchorAlignmentBonus(QueryTerms.fromQuery(query), result);
    }

    static int specializedAnchorFocusBonus(QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        String preferredStructureType = metadataProfile.preferredStructureType();
        if (!metadataProfile.hasExplicitTopicFocus() || !requiresSpecializedRouteAnchorSignal(preferredStructureType)) {
            return 0;
        }

        int sectionBonus = metadataProfile.sectionHeadingBonus(result.sectionHeading);
        String normalizedRole = emptySafe(result.contentRole).trim().toLowerCase(QUERY_LOCALE);
        if (sectionBonus > 0) {
            if ("subsystem".equals(normalizedRole) || "starter".equals(normalizedRole) || "planning".equals(normalizedRole)) {
                return 10;
            }
            if ("safety".equals(normalizedRole)) {
                return 4;
            }
            return 2;
        }
        if ("safety".equals(normalizedRole)) {
            return -16;
        }
        if ("reference".equals(normalizedRole)) {
            return -8;
        }
        return 0;
    }

    static int broadRouteSectionPreferenceBonus(QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        if (!queryTerms.routeProfile.isStarterBuildProject() || queryTerms.metadataProfile.hasExplicitTopicFocus()) {
            return 0;
        }
        int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading);
        if (sectionBonus > 0) {
            return 8 + Math.min(10, sectionBonus);
        }
        if (sectionBonus < 0) {
            return Math.max(-20, sectionBonus);
        }
        return 0;
    }

    private List<RankedChunk> searchWithKeywordHits(QueryTerms queryTerms, int limit) {
        List<String> tokens = queryTerms.keywordTokens();
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        int sqlLimit = keywordSqlLimit(queryTerms, limit);
        ArrayList<String> clauses = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        for (String token : tokens) {
            String like = "%" + token + "%";
            clauses.add("(guide_title LIKE ? OR section_heading LIKE ? OR category LIKE ? OR tags LIKE ? OR description LIKE ? OR document LIKE ?)");
            for (int i = 0; i < 6; i++) {
                args.add(like);
            }
        }
        args.add(String.valueOf(sqlLimit));

        ArrayList<ScoredChunk> scored = new ArrayList<>();
        try (Cursor cursor = database.rawQuery(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, tags, description, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE " + String.join(" OR ", clauses) + " LIMIT ?",
            args.toArray(new String[0])
        )) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(2);
                String guideId = emptySafe(cursor.getString(3));
                String section = emptySafe(cursor.getString(4));
                String category = emptySafe(cursor.getString(5));
                String document = emptySafe(cursor.getString(6));
                String tags = emptySafe(cursor.getString(7));
                String description = emptySafe(cursor.getString(8));
                String contentRole = emptySafe(cursor.getString(9));
                String timeHorizon = emptySafe(cursor.getString(10));
                String structureType = emptySafe(cursor.getString(11));
                String topicTags = emptySafe(cursor.getString(12));
                int score = lexicalKeywordScore(queryTerms, title, section, category, combineTags(tags, topicTags), description, document);
                score += metadataBonus(queryTerms, category, contentRole, timeHorizon, structureType, topicTags);
                if (score <= 0) {
                    continue;
                }
                scored.add(new ScoredChunk(
                    new RankedChunk(
                        cursor.getString(0),
                        cursor.getInt(1),
                        title,
                        guideId,
                        section,
                        category,
                        document,
                        contentRole,
                        timeHorizon,
                        structureType,
                        topicTags,
                        score,
                        Integer.MAX_VALUE,
                        -1,
                        0f
                    ),
                    score
                ));
            }
        }

        scored.sort((left, right) -> {
            int scoreOrder = Integer.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            return Integer.compare(left.chunk.document.length(), right.chunk.document.length());
        });

        ArrayList<RankedChunk> ordered = new ArrayList<>();
        int capped = Math.min(limit, scored.size());
        for (int index = 0; index < capped; index++) {
            RankedChunk chunk = scored.get(index).chunk;
            ordered.add(
                new RankedChunk(
                    chunk.chunkId,
                    chunk.vectorRowId,
                    chunk.guideTitle,
                    chunk.guideId,
                    chunk.sectionHeading,
                    chunk.category,
                    chunk.document,
                    chunk.contentRole,
                    chunk.timeHorizon,
                    chunk.structureType,
                    chunk.topicTags,
                    scored.get(index).score,
                    index,
                    -1,
                    0f
                )
            );
        }
        return ordered;
    }

    private List<SearchResult> searchPlainLikeResults(String query, int limit) {
        ArrayList<SearchResult> results = new ArrayList<>();
        String like = "%" + query + "%";
        try (Cursor cursor = database.rawQuery(
            "SELECT guide_title, guide_id, section_heading, category, document, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks " +
                "WHERE document LIKE ? OR guide_title LIKE ? OR tags LIKE ? OR category LIKE ? OR description LIKE ? " +
                "LIMIT ?",
            new String[]{like, like, like, like, like, String.valueOf(limit)}
        )) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0);
                String guideId = emptySafe(cursor.getString(1));
                String section = emptySafe(cursor.getString(2));
                String category = emptySafe(cursor.getString(3));
                String document = emptySafe(cursor.getString(4));
                String contentRole = emptySafe(cursor.getString(5));
                String timeHorizon = emptySafe(cursor.getString(6));
                String structureType = emptySafe(cursor.getString(7));
                String topicTags = emptySafe(cursor.getString(8));
                String subtitle = guideId + " | " + category + " | " + section + " | text";
                results.add(new SearchResult(
                    title,
                    subtitle,
                    clip(document, 220),
                    document,
                    guideId,
                    section,
                    category,
                    "text",
                    contentRole,
                    timeHorizon,
                    structureType,
                    topicTags
                ));
            }
        }
        return results;
    }

    private List<RankedChunk> loadVectorNeighborHits(List<VectorStore.VectorNeighbor> neighbors) {
        if (neighbors.isEmpty()) {
            return Collections.emptyList();
        }

        HashMap<Integer, VectorStore.VectorNeighbor> byRowId = new HashMap<>();
        ArrayList<String> placeholders = new ArrayList<>();
        String[] args = new String[neighbors.size()];
        for (int index = 0; index < neighbors.size(); index++) {
            VectorStore.VectorNeighbor neighbor = neighbors.get(index);
            byRowId.put(neighbor.rowId, neighbor);
            placeholders.add("?");
            args[index] = String.valueOf(neighbor.rowId);
        }

        HashMap<Integer, RankedChunk> loaded = new HashMap<>();
        try (Cursor cursor = database.rawQuery(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE vector_row_id IN (" + String.join(",", placeholders) + ")",
            args
        )) {
            while (cursor.moveToNext()) {
                int vectorRowId = cursor.getInt(1);
                VectorStore.VectorNeighbor neighbor = byRowId.get(vectorRowId);
                if (neighbor == null) {
                    continue;
                }
                loaded.put(
                    vectorRowId,
                    new RankedChunk(
                        cursor.getString(0),
                        vectorRowId,
                        cursor.getString(2),
                        emptySafe(cursor.getString(3)),
                        emptySafe(cursor.getString(4)),
                        emptySafe(cursor.getString(5)),
                        emptySafe(cursor.getString(6)),
                        emptySafe(cursor.getString(7)),
                        emptySafe(cursor.getString(8)),
                        emptySafe(cursor.getString(9)),
                        emptySafe(cursor.getString(10)),
                        0.0,
                        Integer.MAX_VALUE,
                        0,
                        neighbor.score
                    )
                );
            }
        }

        ArrayList<RankedChunk> ordered = new ArrayList<>();
        int rank = 0;
        for (VectorStore.VectorNeighbor neighbor : neighbors) {
            RankedChunk loadedHit = loaded.get(neighbor.rowId);
            if (loadedHit == null) {
                continue;
            }
            ordered.add(loadedHit.withVectorRank(rank, neighbor.score));
            rank += 1;
        }
        return ordered;
    }

    private List<CombinedHit> mergeHybrid(
        List<RankedChunk> lexicalHits,
        List<RankedChunk> vectorHits,
        AnchorPriorDirective anchorPrior
    ) {
        LinkedHashMap<String, CombinedHit> merged = new LinkedHashMap<>();

        for (int index = 0; index < lexicalHits.size(); index++) {
            RankedChunk hit = lexicalHits.get(index);
            CombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new CombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.lexicalRank = Math.min(combined.lexicalRank, index);
            combined.rrfScore += reciprocalRank(index);
        }

        for (int index = 0; index < vectorHits.size(); index++) {
            RankedChunk hit = vectorHits.get(index);
            CombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new CombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.vectorRank = Math.min(combined.vectorRank, index);
            combined.vectorScore = Math.max(combined.vectorScore, hit.vectorScore);
            combined.rrfScore += reciprocalRank(index);
        }

        applyAnchorPrior(merged, anchorPrior, anchorRelatedLinkWeights);

        ArrayList<CombinedHit> ordered = new ArrayList<>(merged.values());
        ordered.sort((left, right) -> {
            int scoreOrder = Double.compare(right.rrfScore, left.rrfScore);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int modeOrder = Integer.compare(modePriority(right), modePriority(left));
            if (modeOrder != 0) {
                return modeOrder;
            }
            int vectorOrder = Float.compare(right.vectorScore, left.vectorScore);
            if (vectorOrder != 0) {
                return vectorOrder;
            }
            return Integer.compare(left.lexicalRank, right.lexicalRank);
        });
        return ordered;
    }

    private static void applyAnchorPrior(
        Map<String, CombinedHit> merged,
        AnchorPriorDirective anchorPrior,
        Map<String, Map<String, Double>> relatedWeightsByGuide
    ) {
        if (merged == null || merged.isEmpty() || anchorPrior == null) {
            return;
        }
        Map<String, Double> relatedWeights = relatedWeightsByGuide.get(anchorPrior.anchorGuideId);
        if (relatedWeights == null) {
            relatedWeights = Collections.emptyMap();
        }
        for (CombinedHit combined : merged.values()) {
            String guideId = emptySafe(combined.chunk.guideId).trim();
            PackAnchorPriorPolicy.AnchorPriorAdjustment adjustment = PackAnchorPriorPolicy.adjustment(
                anchorPrior.anchorGuideId,
                anchorPrior.turnsSinceAnchor,
                guideId,
                relatedWeights
            );
            if (!adjustment.hasBonus()) {
                continue;
            }
            combined.rrfScore += adjustment.bonus;
            safeLogDebug(
                "anchor_prior turn=" + anchorPrior.turnCount +
                    " anchor_gid=" + anchorPrior.anchorGuideId +
                    " chunk_gid=" + guideId +
                    " base=" + PackAnchorPriorPolicy.BASE_BONUS +
                    " decay=" + adjustment.decay +
                    " weight=" + adjustment.weight +
                    " bonus=" + adjustment.bonus
            );
        }
    }

    private List<RankedChunk> mergeLexicalHits(List<RankedChunk> ftsHits, List<RankedChunk> keywordHits) {
        if (ftsHits.isEmpty()) {
            return reindexLexicalHits(keywordHits);
        }
        if (keywordHits.isEmpty()) {
            return reindexLexicalHits(ftsHits);
        }

        LinkedHashMap<String, LexicalCombinedHit> merged = new LinkedHashMap<>();

        for (int index = 0; index < ftsHits.size(); index++) {
            RankedChunk hit = ftsHits.get(index);
            LexicalCombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new LexicalCombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.ftsRank = Math.min(combined.ftsRank, index);
            combined.score += reciprocalRank(index) * 1.25;
        }

        for (int index = 0; index < keywordHits.size(); index++) {
            RankedChunk hit = keywordHits.get(index);
            LexicalCombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new LexicalCombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.keywordRank = Math.min(combined.keywordRank, index);
            combined.score += reciprocalRank(index);
        }

        ArrayList<LexicalCombinedHit> ordered = new ArrayList<>(merged.values());
        ordered.sort((left, right) -> {
            int scoreOrder = Double.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int bestRankOrder = Integer.compare(bestLexicalRank(left), bestLexicalRank(right));
            if (bestRankOrder != 0) {
                return bestRankOrder;
            }
            return Integer.compare(left.chunk.document.length(), right.chunk.document.length());
        });

        ArrayList<RankedChunk> results = new ArrayList<>();
        for (int index = 0; index < ordered.size(); index++) {
            results.add(ordered.get(index).chunk.withLexicalRank(index, ordered.get(index).score));
        }
        return results;
    }

    private List<SearchResult> toSearchResults(List<CombinedHit> combinedHits, int limit) {
        ArrayList<SearchResult> results = new ArrayList<>();
        Set<String> seenGuideSections = new LinkedHashSet<>();
        int capped = limit <= 0 ? combinedHits.size() : limit;
        for (int index = 0; index < combinedHits.size() && results.size() < capped; index++) {
            CombinedHit combined = combinedHits.get(index);
            RankedChunk hit = combined.chunk;
            String guideSectionKey = buildGuideSectionKey(hit.guideId, hit.guideTitle, hit.sectionHeading);
            if (seenGuideSections.contains(guideSectionKey)) {
                continue;
            }
            seenGuideSections.add(guideSectionKey);
            String retrievalMode = PackQueryPipelineHelper.retrievalModeForRanks(
                combined.lexicalRank,
                combined.vectorRank
            );
            results.add(PackQueryPipelineHelper.mapChunkResult(
                hit.guideTitle,
                hit.guideId,
                hit.sectionHeading,
                hit.category,
                hit.document,
                retrievalMode,
                hit.contentRole,
                hit.timeHorizon,
                hit.structureType,
                hit.topicTags
            ));
        }
        return results;
    }

    static String buildGuideSectionKey(String guideId, String guideTitle, String sectionHeading) {
        return PackQueryPipelineHelper.guideSectionKey(guideId, guideTitle, sectionHeading);
    }

    private static int modePriority(CombinedHit hit) {
        if (hit.lexicalRank != Integer.MAX_VALUE && hit.vectorRank != Integer.MAX_VALUE) {
            return 3;
        }
        if (hit.vectorRank != Integer.MAX_VALUE) {
            return 2;
        }
        return 1;
    }

    private static double reciprocalRank(int rank) {
        return 1.0 / (HYBRID_RRF_K + rank + 1.0);
    }

    private static String buildFtsQuery(QueryTerms queryTerms) {
        return PackFtsQueryBuilder.build(queryTerms);
    }

    private static String buildFtsQuery(QueryTerms queryTerms, int maxExpressions, boolean includeExpansionTokens) {
        return PackFtsQueryBuilder.build(queryTerms, maxExpressions, includeExpansionTokens);
    }

    static String buildFtsQueryForTest(String query) {
        return buildFtsQuery(QueryTerms.fromQuery(query));
    }

    static int lexicalKeywordScore(
        QueryTerms queryTerms,
        String title,
        String section,
        String category,
        String tags,
        String description,
        String document
    ) {
        String titleLower = emptySafe(title).toLowerCase(QUERY_LOCALE);
        String sectionLower = emptySafe(section).toLowerCase(QUERY_LOCALE);
        String categoryLower = emptySafe(category).toLowerCase(QUERY_LOCALE);
        String tagsLower = emptySafe(tags).toLowerCase(QUERY_LOCALE);
        String descriptionLower = emptySafe(description).toLowerCase(QUERY_LOCALE);
        String documentLower = emptySafe(document).toLowerCase(QUERY_LOCALE);
        String queryLower = queryTerms.queryLower;

        int score = 0;
        if (containsTerm(titleLower, queryLower)) {
            score += 18;
        }
        if (containsTerm(sectionLower, queryLower)) {
            score += 14;
        }
        if (containsTerm(descriptionLower, queryLower)) {
            score += 8;
        }

        int strongMatches = 0;
        for (String token : queryTerms.primaryKeywordTokens()) {
            if (containsTerm(titleLower, token)) {
                score += 12;
                strongMatches += 1;
            }
            if (containsTerm(sectionLower, token)) {
                score += 10;
                strongMatches += 1;
            }
            if (containsTerm(tagsLower, token)) {
                score += 8;
                strongMatches += 1;
            }
            if (containsTerm(categoryLower, token)) {
                score += 5;
            }
            if (containsTerm(descriptionLower, token)) {
                score += 6;
                strongMatches += 1;
            }
            if (containsTerm(documentLower, token)) {
                score += 3;
                strongMatches += 1;
            }
        }

        int expansionMatches = 0;
        for (String token : queryTerms.expansionTokens) {
            if (containsTerm(titleLower, token)) {
                score += 6;
                expansionMatches += 1;
            }
            if (containsTerm(sectionLower, token)) {
                score += 5;
                expansionMatches += 1;
            }
            if (containsTerm(tagsLower, token)) {
                score += 4;
                expansionMatches += 1;
            }
            if (containsTerm(categoryLower, token)) {
                score += 3;
            }
            if (containsTerm(descriptionLower, token)) {
                score += 3;
                expansionMatches += 1;
            }
            if (containsTerm(documentLower, token)) {
                score += 2;
                expansionMatches += 1;
            }
        }

        score += queryTerms.routeProfile.metadataBonus(
            titleLower,
            sectionLower,
            categoryLower,
            tagsLower,
            descriptionLower,
            documentLower
        );

        if (strongMatches >= 2) {
            score += 10;
        }
        if (!queryTerms.primaryKeywordTokens().isEmpty() && strongMatches >= queryTerms.primaryKeywordTokens().size()) {
            score += 8;
        }
        if (expansionMatches >= 2) {
            score += 4;
        }
        return score;
    }

    static int metadataBonus(
        QueryTerms queryTerms,
        String category,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
    ) {
        return queryTerms.metadataProfile.metadataBonus(
            category,
            contentRole,
            timeHorizon,
            structureType,
            topicTags
        );
    }

    private List<RankedChunk> reindexLexicalHits(List<RankedChunk> hits) {
        ArrayList<RankedChunk> reindexed = new ArrayList<>();
        for (int index = 0; index < hits.size(); index++) {
            reindexed.add(hits.get(index).withLexicalRank(index));
        }
        return reindexed;
    }

    private static int bestLexicalRank(LexicalCombinedHit hit) {
        return Math.min(hit.ftsRank, hit.keywordRank);
    }

    private static Set<String> buildStopTokens() {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        Collections.addAll(
            tokens,
            "a", "an", "and", "are", "at", "be", "but", "by", "can", "do", "for", "from", "how",
            "i", "if", "in", "into", "is", "it", "me", "my", "of", "on", "or", "our", "so",
            "that", "the", "their", "them", "there", "these", "they", "this", "to", "we", "what",
            "when", "where", "which", "who", "why", "with", "you", "your"
        );
        return Collections.unmodifiableSet(tokens);
    }

    private static Set<String> buildLowSignalTokens() {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        Collections.addAll(
            tokens,
            "build", "create", "do", "find", "fix", "get", "help", "improve", "keep", "make",
            "need", "replace", "start", "stop", "use"
        );
        return Collections.unmodifiableSet(tokens);
    }

    private static Map<String, String[]> buildQueryExpansions() {
        LinkedHashMap<String, String[]> expansions = new LinkedHashMap<>();
        expansions.put("boat", new String[]{"watercraft", "vessel", "hull"});
        expansions.put("canoe", new String[]{"watercraft", "boat", "dugout", "paddle"});
        expansions.put("charcoal", new String[]{"filtration", "activated charcoal", "water purification"});
        expansions.put("coracle", new String[]{"watercraft", "boat", "hide"});
        expansions.put("dugout", new String[]{"canoe", "watercraft", "boat"});
        expansions.put("drum", new String[]{"container", "water storage", "sanitation"});
        expansions.put("filter", new String[]{"filtration", "water purification", "safe water"});
        expansions.put("fire", new String[]{"tinder", "kindling", "ignite"});
        expansions.put("kayak", new String[]{"watercraft", "boat", "paddle"});
        expansions.put("metal", new String[]{"forge", "brazing", "soldering"});
        expansions.put("oar", new String[]{"paddle", "rowing"});
        expansions.put("paddle", new String[]{"oar", "rowing"});
        expansions.put("puncture", new String[]{"wound management", "first aid", "irrigation", "tetanus"});
        expansions.put("purify", new String[]{"water purification", "disinfect", "filtration"});
        expansions.put("raft", new String[]{"watercraft", "boat", "flotation"});
        expansions.put("rowboat", new String[]{"boat", "watercraft", "oar"});
        expansions.put("sand", new String[]{"filtration", "settling", "prefilter"});
        expansions.put("sailboat", new String[]{"boat", "watercraft", "sail"});
        expansions.put("store", new String[]{"storage", "container sanitation", "rationing"});
        expansions.put("welder", new String[]{"forge welding", "brazing", "soldering"});
        expansions.put("wet", new String[]{"rain", "damp", "dry tinder"});
        expansions.put("wound", new String[]{"first aid", "infection prevention", "dressings"});
        return Collections.unmodifiableMap(expansions);
    }

    private static Set<String> buildMarkerSet(String... values) {
        LinkedHashSet<String> markers = new LinkedHashSet<>();
        Collections.addAll(markers, values);
        return Collections.unmodifiableSet(markers);
    }

    private static boolean containsAnyMarker(String text, Set<String> markers) {
        String normalized = normalizeMatchText(text);
        if (normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return false;
        }
        String boundedText = " " + normalized + " ";
        for (String marker : markers) {
            String normalizedMarker = normalizeMatchText(marker);
            if (normalizedMarker.isEmpty()) {
                continue;
            }
            if (boundedText.contains(" " + normalizedMarker + " ")) {
                return true;
            }
        }
        return false;
    }

    static String clip(String text, int limit) {
        String safe = emptySafe(text).replaceAll("\\s+", " ").trim();
        if (safe.length() <= limit) {
            return safe;
        }
        return safe.substring(0, Math.max(0, limit - 3)).trim() + "...";
    }

    private static String normalizeSection(String section) {
        return emptySafe(section).replaceAll("\\s+", " ").trim().toLowerCase(QUERY_LOCALE);
    }

    private static boolean containsTerm(String text, String term) {
        String normalizedText = normalizeMatchText(text);
        String normalizedTerm = normalizeMatchText(term);
        if (normalizedText.isEmpty() || normalizedTerm.isEmpty()) {
            return false;
        }
        if (normalizedTerm.contains(" ")) {
            return normalizedText.contains(normalizedTerm);
        }
        return (" " + normalizedText + " ").contains(" " + normalizedTerm + " ");
    }

    private static String normalizeMatchText(String text) {
        return emptySafe(text)
            .toLowerCase(QUERY_LOCALE)
            .replaceAll("[^a-z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    static String combineTags(String tags, String topicTags) {
        String safeTags = emptySafe(tags).trim();
        String safeTopicTags = emptySafe(topicTags).trim();
        if (safeTags.isEmpty()) {
            return safeTopicTags;
        }
        if (safeTopicTags.isEmpty()) {
            return safeTags;
        }
        return safeTags + "," + safeTopicTags;
    }

    static boolean supportCandidateMatchesRoute(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        boolean diversifyContext,
        SearchResult candidate
    ) {
        return PackRouteSupportPolicy.supportCandidateMatchesRoute(
            routeProfile,
            metadataProfile,
            diversifyContext,
            candidate
        );
    }

    static int supportStructurePenalty(boolean diversifyContext, String retrievalMode, String sectionHeading) {
        return PackSupportScoringPolicy.supportStructurePenalty(diversifyContext, retrievalMode, sectionHeading);
    }

    static String emptySafe(String text) {
        return text == null ? "" : text;
    }

    private static void maybeLogCandidateTelemetry(String line) {
        if (!shouldLogCandidateTelemetry() || emptySafe(line).trim().isEmpty()) {
            return;
        }
        safeLogDebug(line);
    }

    private static boolean shouldLogCandidateTelemetry() {
        try {
            return !"user".equals(android.os.Build.TYPE) || Log.isLoggable(TAG, Log.DEBUG);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static List<SearchResult> extractSearchResults(List<RerankedResult> detailed) {
        ArrayList<SearchResult> ordered = new ArrayList<>();
        if (detailed == null) {
            return ordered;
        }
        for (RerankedResult result : detailed) {
            if (result != null && result.result != null) {
                ordered.add(result.result);
            }
        }
        return ordered;
    }

    private static String buildLexicalCandidateTelemetryLine(String query, List<RankedChunk> hits) {
        ArrayList<String> rows = new ArrayList<>();
        if (hits != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(hits.size());
            for (int index = 0; index < capped; index++) {
                RankedChunk hit = hits.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRow(
                        index + 1,
                        hit.guideId,
                        hit.sectionHeading,
                        hit.lexicalScore,
                        hit.structureType,
                        hit.category,
                        hit.topicTags
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("lexical", query, rows);
    }

    private static String buildVectorCandidateTelemetryLine(String query, List<RankedChunk> hits) {
        ArrayList<String> rows = new ArrayList<>();
        if (hits != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(hits.size());
            for (int index = 0; index < capped; index++) {
                RankedChunk hit = hits.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRow(
                        index + 1,
                        hit.guideId,
                        hit.sectionHeading,
                        hit.vectorScore,
                        hit.structureType,
                        hit.category,
                        hit.topicTags
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("vector", query, rows);
    }

    private static String buildPrerankCandidateTelemetryLine(
        String query,
        List<CombinedHit> hits,
        int limit
    ) {
        ArrayList<String> rows = new ArrayList<>();
        if (hits != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(hits.size(), limit);
            for (int index = 0; index < capped; index++) {
                CombinedHit hit = hits.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRow(
                        index + 1,
                        hit.chunk.guideId,
                        hit.chunk.sectionHeading,
                        hit.rrfScore,
                        hit.chunk.structureType,
                        hit.chunk.category,
                        hit.chunk.topicTags
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("prerank", query, rows);
    }

    private static String buildRerankedCandidateTelemetryLine(String query, List<RerankedResult> results) {
        ArrayList<String> rows = new ArrayList<>();
        if (results != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(results.size());
            for (int index = 0; index < capped; index++) {
                RerankedResult result = results.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRerankedRow(
                        index + 1,
                        result.result,
                        result.finalScore,
                        result.baseScore,
                        result.metadataBonus
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("reranked", query, rows);
    }

    private static void safeLogDebug(String message) {
        try {
            Log.d(TAG, emptySafe(message));
        } catch (RuntimeException ignored) {
            // Local JVM unit tests do not mock android.util.Log.
        }
    }

    private static long elapsedRealtimeNanosSafe() {
        try {
            return SystemClock.elapsedRealtimeNanos();
        } catch (RuntimeException ignored) {
            return System.nanoTime();
        }
    }

    private static boolean hasWaterDistributionTitleSignal(SearchResult result) {
        return PackRouteSignalPolicy.hasWaterDistributionTitleSignal(result);
    }

    private static boolean hasStrongWaterDistributionGuideSignal(SearchResult result) {
        return PackRouteSignalPolicy.hasStrongWaterDistributionGuideSignal(result);
    }

    private static boolean hasWaterDistributionDetailSignal(SearchResult result) {
        return PackRouteSignalPolicy.hasWaterDistributionDetailSignal(result);
    }

    private static int waterDistributionAnchorFocusBonus(SearchResult result) {
        String normalizedTitle = normalizeMatchText(emptySafe(result.title));
        String normalizedSection = normalizeMatchText(emptySafe(result.sectionHeading));
        String normalized = normalizeMatchText(emptySafe(result.title) + " " + emptySafe(result.sectionHeading));
        int bonus = 0;
        String contentRole = emptySafe(result.contentRole).trim().toLowerCase(QUERY_LOCALE);
        String structureType = emptySafe(result.structureType).trim().toLowerCase(QUERY_LOCALE);
        if (hasStrongWaterDistributionGuideSignal(result)) {
            bonus += 10;
        }
        if ("planning".equals(contentRole) || "subsystem".equals(contentRole)) {
            bonus += 10;
        } else if ("reference".equals(contentRole)) {
            bonus -= 8;
        }
        if ("water_distribution".equals(structureType)) {
            bonus += 6;
        }
        if (normalized.contains("distribution")) {
            bonus += 6;
        }
        if (normalized.contains("storage tank") || normalized.contains("cistern")) {
            bonus += 4;
        }
        if (normalizedTitle.contains("lifecycle")) {
            bonus -= 18;
        }
        if (normalizedSection.startsWith("phase ")) {
            bonus -= 12;
        }
        if (normalized.contains("see also") || normalized.contains("checklist")) {
            bonus -= 16;
        }
        if (normalized.contains("preventive maintenance") || normalized.contains("system care")) {
            bonus -= 14;
        }
        if (normalizedTitle.contains("drilling") || normalizedTitle.contains("troubleshooting")) {
            bonus -= 12;
        }
        if (normalized.contains("plumbing") && !normalized.contains("distribution")) {
            bonus -= 6;
        }
        if (normalized.contains("water system") && !normalized.contains("distribution")) {
            bonus -= 4;
        }
        return bonus;
    }

    private PackFtsRuntimeDetector.Runtime detectFtsAvailability() {
        PackFtsRuntimeDetector.Runtime cached = cachedFtsRuntime;
        if (cached != null) {
            return cached;
        }

        synchronized (FTS_RUNTIME_LOCK) {
            if (cachedFtsRuntime != null) {
                return cachedFtsRuntime;
            }

            PackFtsRuntimeDetector.Runtime detected = detectFtsAvailabilityInternal();
            cachedFtsRuntime = detected;
            return detected;
        }
    }

    private PackFtsRuntimeDetector.Runtime detectFtsAvailabilityInternal() {
        boolean supportsFts5 = hasCompileOption("ENABLE_FTS5");
        boolean supportsFts4 = hasCompileOption("ENABLE_FTS4");
        boolean hasFts5Table = tableExistsInSchema(FTS5_TABLE);
        boolean hasFts4Table = tableExistsInSchema(FTS4_TABLE);
        PackFtsRuntimeDetector.ProbeResult runtimeFts5 = hasFts5Table
            ? supportsFtsMatchRuntime(FTS5_TABLE)
            : PackFtsRuntimeDetector.ProbeResult.notRun();
        PackFtsRuntimeDetector.ProbeResult runtimeFts4 = hasFts4Table
            ? supportsFtsMatchRuntime(FTS4_TABLE)
            : PackFtsRuntimeDetector.ProbeResult.notRun();
        PackFtsRuntimeDetector.Runtime detected = selectFtsRuntime(
            hasFts5Table,
            hasFts4Table,
            runtimeFts5.supported,
            runtimeFts4.supported
        );

        if (detected.available) {
            Log.d(
                TAG,
                "fts.available table=" + detected.tableName +
                    " supportsBm25=" + detected.supportsBm25 +
                    " schemaPresent=true runtimeProbe=true compile5=" + supportsFts5 +
                    " compile4=" + supportsFts4 +
                    " runtime5=" + runtimeFts5.supported +
                    " runtime5Ms=" + runtimeFts5.elapsedMs +
                    " runtime4=" + runtimeFts4.supported +
                    " runtime4Ms=" + runtimeFts4.elapsedMs +
                    " fallback=" + detected.tableName
            );
            return detected;
        }

        Log.d(
            TAG,
            "fts.unavailable support5=" + supportsFts5 +
                " support4=" + supportsFts4 +
                " runtime5=" + runtimeFts5.supported +
                " runtime5Ms=" + runtimeFts5.elapsedMs +
                " runtime4=" + runtimeFts4.supported +
                " runtime4Ms=" + runtimeFts4.elapsedMs +
                " schema5=" + hasFts5Table +
                " schema4=" + hasFts4Table +
                " fallback=like"
        );
        return PackFtsRuntimeDetector.Runtime.unavailable();
    }

    private static PackFtsRuntimeDetector.Runtime selectFtsRuntime(
        boolean hasFts5Table,
        boolean hasFts4Table,
        boolean runtimeFts5,
        boolean runtimeFts4
    ) {
        return PackFtsRuntimeDetector.selectRuntime(hasFts5Table, hasFts4Table, runtimeFts5, runtimeFts4);
    }

    private PackFtsRuntimeDetector.ProbeResult supportsFtsMatchRuntime(String tableName) {
        if (emptySafe(tableName).trim().isEmpty()) {
            return PackFtsRuntimeDetector.ProbeResult.notRun();
        }

        long startedAtNs = elapsedRealtimeNanosSafe();
        boolean supported = false;
        try (Cursor ignored = database.rawQuery(
            "SELECT rowid FROM " + tableName + " WHERE " + tableName + " MATCH ? LIMIT 1",
            new String[]{"water"}
        )) {
            supported = true;
        } catch (SQLiteException ignored) {
        }
        long elapsedMs = elapsedRealtimeMsSince(startedAtNs);
        Log.d(
            TAG,
            "fts.runtime_probe table=" + tableName +
                " supported=" + supported +
                " elapsedMs=" + elapsedMs
        );
        if (elapsedMs > FTS_RUNTIME_PROBE_BUDGET_MS) {
            Log.w(
                TAG,
                "fts.runtime_probe_slow table=" + tableName +
                    " elapsedMs=" + elapsedMs +
                    " budgetMs=" + FTS_RUNTIME_PROBE_BUDGET_MS
            );
        }
        return new PackFtsRuntimeDetector.ProbeResult(supported, elapsedMs);
    }

    private boolean hasCompileOption(String optionName) {
        String target = emptySafe(optionName).trim().toLowerCase(QUERY_LOCALE);
        if (target.isEmpty()) {
            return false;
        }

        try (Cursor cursor = database.rawQuery("PRAGMA compile_options", null)) {
            while (cursor.moveToNext()) {
                String option = emptySafe(cursor.getString(0)).trim().toLowerCase(QUERY_LOCALE);
                if (option.equals(target) || option.contains(target)) {
                    return true;
                }
            }
        } catch (SQLiteException ignored) {
            return false;
        }
        return false;
    }

    private static String buildSearchSummaryLine(
        String query,
        boolean routeFocused,
        int routeSpecCount,
        int lexicalHits,
        int vectorHits,
        int routeResults,
        SearchLatencyBreakdown breakdown
    ) {
        return PackQueryPipelineHelper.buildSearchSummaryLine(
            query,
            routeFocused,
            routeSpecCount,
            lexicalHits,
            vectorHits,
            routeResults,
            breakdown
        );
    }

    private static String buildSlowQueryTripwireDebugLine(String query, SearchLatencyBreakdown breakdown) {
        return PackQueryPipelineHelper.buildSlowQueryTripwireDebugLine(query, breakdown);
    }

    private static void logSearchTripwireIfNeeded(String query, SearchLatencyBreakdown breakdown) {
        if (breakdown == null || !breakdown.hasSlowStage()) {
            return;
        }
        Log.w(TAG, buildSlowQueryTripwireDebugLine(query, breakdown));
    }

    private static long elapsedRealtimeMsSince(long startedAtNs) {
        return Math.max(0L, elapsedRealtimeNanosSafe() - startedAtNs) / 1_000_000L;
    }

    private boolean tableExistsInSchema(String tableName) {
        if (emptySafe(tableName).trim().isEmpty()) {
            return false;
        }

        try (Cursor cursor = database.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1",
            new String[]{tableName}
        )) {
            return cursor.moveToFirst();
        } catch (SQLiteException ignored) {
            return false;
        }
    }

    private void appendRelatedGuides(
        LinkedHashMap<String, SearchResult> output,
        String sql,
        String[] args,
        int limit
    ) {
        if (output.size() >= limit) {
            return;
        }
        try (Cursor cursor = database.rawQuery(sql, args)) {
            while (cursor.moveToNext() && output.size() < limit) {
                SearchResult result = guideResultFromCursor(cursor);
                String guideId = emptySafe(result.guideId).trim();
                if (guideId.isEmpty()) {
                    continue;
                }
                output.putIfAbsent(guideId.toLowerCase(QUERY_LOCALE), result);
            }
        }
    }

    private static SearchResult guideResultFromCursor(Cursor cursor) {
        return PackQueryPipelineHelper.mapGuideRow(
            cursor.getString(0),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getString(3),
            cursor.getString(4),
            cursor.getString(5),
            cursor.getString(6),
            cursor.getString(7),
            cursor.getString(8),
            cursor.getString(9)
        );
    }

    @Override
    public void close() {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }

        RuntimeException failure = null;
        if (vectorStore != null) {
            try {
                vectorStore.close();
            } catch (IOException exc) {
                Log.w(TAG, "vectorStore.close failed", exc);
            } catch (RuntimeException exc) {
                Log.w(TAG, "vectorStore.close failed", exc);
                failure = exc;
            }
        }
        try {
            database.close();
        } catch (RuntimeException exc) {
            if (failure == null) {
                failure = exc;
            } else {
                failure.addSuppressed(exc);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private static final class RankedChunk {
        final String chunkId;
        final int vectorRowId;
        final String guideTitle;
        final String guideId;
        final String sectionHeading;
        final String category;
        final String document;
        final String contentRole;
        final String timeHorizon;
        final String structureType;
        final String topicTags;
        final double lexicalScore;
        final int lexicalRank;
        final int vectorRank;
        final float vectorScore;

        RankedChunk(
            String chunkId,
            int vectorRowId,
            String guideTitle,
            String guideId,
            String sectionHeading,
            String category,
            String document,
            String contentRole,
            String timeHorizon,
            String structureType,
            String topicTags,
            double lexicalScore,
            int lexicalRank,
            int vectorRank,
            float vectorScore
        ) {
            this.chunkId = chunkId;
            this.vectorRowId = vectorRowId;
            this.guideTitle = guideTitle;
            this.guideId = guideId;
            this.sectionHeading = sectionHeading;
            this.category = category;
            this.document = document;
            this.contentRole = contentRole;
            this.timeHorizon = timeHorizon;
            this.structureType = structureType;
            this.topicTags = topicTags;
            this.lexicalScore = lexicalScore;
            this.lexicalRank = lexicalRank;
            this.vectorRank = vectorRank;
            this.vectorScore = vectorScore;
        }

        RankedChunk withVectorRank(int rank, float score) {
            return new RankedChunk(
                chunkId,
                vectorRowId,
                guideTitle,
                guideId,
                sectionHeading,
                category,
                document,
                contentRole,
                timeHorizon,
                structureType,
                topicTags,
                lexicalScore,
                lexicalRank,
                rank,
                score
            );
        }

        RankedChunk withLexicalRank(int rank) {
            return withLexicalRank(rank, lexicalScore);
        }

        RankedChunk withLexicalRank(int rank, double score) {
            return new RankedChunk(
                chunkId,
                vectorRowId,
                guideTitle,
                guideId,
                sectionHeading,
                category,
                document,
                contentRole,
                timeHorizon,
                structureType,
                topicTags,
                score,
                rank,
                vectorRank,
                vectorScore
            );
        }
    }

    private static final class CombinedHit {
        final RankedChunk chunk;
        double rrfScore;
        int lexicalRank = Integer.MAX_VALUE;
        int vectorRank = Integer.MAX_VALUE;
        float vectorScore = Float.NEGATIVE_INFINITY;

        CombinedHit(RankedChunk chunk) {
            this.chunk = chunk;
        }
    }

    private static final class LexicalCombinedHit {
        final RankedChunk chunk;
        double score;
        int ftsRank = Integer.MAX_VALUE;
        int keywordRank = Integer.MAX_VALUE;

        LexicalCombinedHit(RankedChunk chunk) {
            this.chunk = chunk;
        }
    }

    private static final class ScoredChunk {
        final RankedChunk chunk;
        final int score;

        ScoredChunk(RankedChunk chunk, int score) {
            this.chunk = chunk;
            this.score = score;
        }
    }

    private static final class SectionCandidate {
        final String guideTitle;
        final String guideId;
        final String sectionHeading;
        final String category;
        final StringBuilder body = new StringBuilder();
        String contentRole;
        String timeHorizon;
        String structureType;
        String topicTags;
        int score;
        boolean anchorSection;

        SectionCandidate(
            String guideTitle,
            String guideId,
            String sectionHeading,
            String category,
            String contentRole,
            String timeHorizon,
            String structureType,
            String topicTags
        ) {
            this.guideTitle = guideTitle;
            this.guideId = guideId;
            this.sectionHeading = sectionHeading;
            this.category = category;
            this.contentRole = contentRole;
            this.timeHorizon = timeHorizon;
            this.structureType = structureType;
            this.topicTags = topicTags;
        }

        void consider(
            int candidateScore,
            String document,
            boolean isAnchorSection,
            String contentRole,
            String timeHorizon,
            String structureType,
            String topicTags
        ) {
            boolean replaceMetadata = candidateScore >= score;
            score = Math.max(score, candidateScore);
            anchorSection = anchorSection || isAnchorSection;
            if (replaceMetadata) {
                this.contentRole = contentRole;
                this.timeHorizon = timeHorizon;
                this.structureType = structureType;
                this.topicTags = topicTags;
            }
            String safeDocument = emptySafe(document).trim();
            if (safeDocument.isEmpty()) {
                return;
            }
            if (body.length() > 0) {
                body.append("\n\n");
            }
            body.append(safeDocument);
        }
    }

    static final class QueryTerms {
        final String queryLower;
        final List<String> rawTokens;
        final List<String> primaryTokens;
        final List<String> strongTokens;
        final List<String> expansionTokens;
        final QueryRouteProfile routeProfile;
        final QueryMetadataProfile metadataProfile;

        QueryTerms(
            String queryLower,
            List<String> rawTokens,
            List<String> primaryTokens,
            List<String> strongTokens,
            List<String> expansionTokens,
            QueryRouteProfile routeProfile,
            QueryMetadataProfile metadataProfile
        ) {
            this.queryLower = queryLower;
            this.rawTokens = rawTokens;
            this.primaryTokens = primaryTokens;
            this.strongTokens = strongTokens;
            this.expansionTokens = expansionTokens;
            this.routeProfile = routeProfile;
            this.metadataProfile = metadataProfile;
        }

        static QueryTerms fromQuery(String query) {
            return fromText(query, QueryRouteProfile.fromQuery(query));
        }

        static QueryTerms fromText(String query, QueryRouteProfile routeProfile) {
            ArrayList<String> rawTokens = new ArrayList<>();
            String[] split = emptySafe(query).toLowerCase(QUERY_LOCALE).split("[^a-z0-9-]+");
            for (String token : split) {
                if (token.length() < 2) {
                    continue;
                }
                if (!rawTokens.contains(token)) {
                    rawTokens.add(token);
                }
                if (rawTokens.size() >= 12) {
                    break;
                }
            }

            ArrayList<String> primaryTokens = new ArrayList<>();
            ArrayList<String> strongTokens = new ArrayList<>();
            LinkedHashSet<String> expansions = new LinkedHashSet<>();
            for (String token : rawTokens) {
                if (!STOP_TOKENS.contains(token)) {
                    primaryTokens.add(token);
                }
                if (!STOP_TOKENS.contains(token) && !LOW_SIGNAL_TOKENS.contains(token)) {
                    strongTokens.add(token);
                }
                String[] mapped = QUERY_EXPANSIONS.get(token);
                if (mapped != null) {
                    Collections.addAll(expansions, mapped);
                }
            }
            expansions.addAll(routeProfile.expansionTokens());

            if (primaryTokens.isEmpty()) {
                primaryTokens.addAll(rawTokens);
            }
            if (strongTokens.isEmpty()) {
                strongTokens.addAll(primaryTokens);
            }

            expansions.removeAll(primaryTokens);
            expansions.removeAll(strongTokens);

            return new QueryTerms(
                emptySafe(query).toLowerCase(QUERY_LOCALE),
                Collections.unmodifiableList(rawTokens),
                Collections.unmodifiableList(primaryTokens),
                Collections.unmodifiableList(strongTokens),
                Collections.unmodifiableList(new ArrayList<>(expansions)),
                routeProfile,
                QueryMetadataProfile.fromQuery(query)
            );
        }

        boolean isEmpty() {
            return rawTokens.isEmpty();
        }

        List<String> primaryFtsTokens() {
            if (!strongTokens.isEmpty()) {
                return strongTokens;
            }
            if (!primaryTokens.isEmpty()) {
                return primaryTokens;
            }
            return rawTokens;
        }

        List<String> primaryKeywordTokens() {
            if (!strongTokens.isEmpty()) {
                return strongTokens;
            }
            if (!primaryTokens.isEmpty()) {
                return primaryTokens;
            }
            return rawTokens;
        }

        List<String> keywordTokens() {
            LinkedHashSet<String> tokens = new LinkedHashSet<>();
            tokens.addAll(primaryKeywordTokens());
            tokens.addAll(expansionTokens);
            return new ArrayList<>(tokens);
        }
    }

    static final class AnchorPriorDirective {
        final String anchorGuideId;
        final int turnsSinceAnchor;
        final int turnCount;

        AnchorPriorDirective(String anchorGuideId, int turnsSinceAnchor, int turnCount) {
            this.anchorGuideId = anchorGuideId == null ? "" : anchorGuideId;
            this.turnsSinceAnchor = turnsSinceAnchor;
            this.turnCount = turnCount;
        }
    }

    private static final class ParsedSearchQuery {
        final String query;
        final AnchorPriorDirective anchorPrior;

        ParsedSearchQuery(String query, AnchorPriorDirective anchorPrior) {
            this.query = query == null ? "" : query;
            this.anchorPrior = anchorPrior;
        }
    }

    static final class GuideScore {
        SearchResult anchor;
        int anchorScore;
        int totalScore;
        int bestScore;
        final Set<String> sectionKeys = new LinkedHashSet<>();

        GuideScore(SearchResult anchor) {
            this.anchor = anchor;
        }
    }

    static final class RerankedResult {
        final SearchResult result;
        final int originalIndex;
        final int metadataBonus;
        double baseScore;
        double finalScore;

        RerankedResult(SearchResult result, int originalIndex, int metadataBonus, double finalScore) {
            this.result = result;
            this.originalIndex = originalIndex;
            this.metadataBonus = metadataBonus;
            this.finalScore = finalScore;
            this.baseScore = finalScore - metadataBonus;
        }

        void addGuideBonus(int bonus) {
            finalScore += bonus;
            baseScore = finalScore - metadataBonus;
        }
    }

    static final class ScoredSearchResult {
        final SearchResult result;
        final int originalIndex;
        int score;

        ScoredSearchResult(SearchResult result, int originalIndex, int score) {
            this.result = result;
            this.originalIndex = originalIndex;
            this.score = score;
        }
    }

    private static final class IndexedSearchResult {
        final SearchResult result;
        final int index;

        IndexedSearchResult(SearchResult result, int index) {
            this.result = result;
            this.index = index;
        }
    }
}


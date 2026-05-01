package com.senku.mobile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

final class PackRouteFocusedSearchExecutor {
    private static final String TAG = "SenkuPackRepo";

    private final SQLiteDatabase database;
    private final boolean ftsAvailable;
    private final String ftsTableName;
    private final boolean ftsSupportsBm25;

    PackRouteFocusedSearchExecutor(
        SQLiteDatabase database,
        boolean ftsAvailable,
        String ftsTableName,
        boolean ftsSupportsBm25
    ) {
        this.database = database;
        this.ftsAvailable = ftsAvailable;
        this.ftsTableName = ftsTableName;
        this.ftsSupportsBm25 = ftsSupportsBm25;
    }

    List<SearchResult> search(PackRepository.QueryTerms queryTerms, int limit) {
        long startedAt = System.currentTimeMillis();
        List<QueryRouteProfile.RouteSearchSpec> routeSpecs = PackRouteFocusedSearchHelper.routeSearchSpecs(queryTerms);
        if (routeSpecs.isEmpty()) {
            return Collections.emptyList();
        }
        Log.d(
            TAG,
            "routeSearch.start query=\"" + queryTerms.queryLower + "\" specs=" + routeSpecs.size() +
                " limit=" + limit +
                " structure=" + PackRepository.emptySafe(queryTerms.metadataProfile.preferredStructureType()) +
                " explicitTopics=" + queryTerms.metadataProfile.preferredTopicTags()
        );

        boolean compactGuideSweep = queryTerms.routeProfile.usesCompactGuideSweep(queryTerms.queryLower);
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection = new LinkedHashMap<>();
        int candidateLimit = PackRouteFocusedSearchHelper.routeChunkCandidateLimit(queryTerms, limit);
        int candidateTarget = PackRouteFocusedSearchHelper.routeChunkCandidateTarget(queryTerms, limit);
        boolean scanFullRouteCursor = PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(queryTerms);
        collectRouteResults(
            RouteResultKind.CHUNK,
            queryTerms,
            routeSpecs,
            candidateLimit,
            candidateTarget,
            scanFullRouteCursor,
            bestBySection
        );

        int guideSearchThreshold = RetrievalRoutePolicy.routeGuideSearchThreshold(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            compactGuideSweep,
            limit
        );
        guideSearchThreshold = RetrievalRoutePolicy.runtimeRouteGuideSearchThreshold(
            queryTerms.metadataProfile,
            ftsSupportsBm25,
            guideSearchThreshold
        );
        Log.d(
            TAG,
            "routeGuideSearch.pre query=\"" + queryTerms.queryLower + "\" sections=" + bestBySection.size() +
                " threshold=" + guideSearchThreshold +
                " compact=" + compactGuideSweep +
                " structure=" + PackRepository.emptySafe(queryTerms.metadataProfile.preferredStructureType()) +
                " explicitTopics=" + queryTerms.metadataProfile.preferredTopicTags()
        );
        if (bestBySection.size() < guideSearchThreshold) {
            collectGuideResults(
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
                    " structure=" + PackRepository.emptySafe(queryTerms.metadataProfile.preferredStructureType()) +
                    " explicitTopics=" + queryTerms.metadataProfile.preferredTopicTags()
            );
        }

        if (bestBySection.isEmpty()) {
            return Collections.emptyList();
        }

        List<SearchResult> ordered = PackRouteFocusedResultRanker.rank(queryTerms, bestBySection, limit);
        Log.d(
            TAG,
            "routeSearch query=\"" + queryTerms.queryLower + "\" specs=" + routeSpecs.size() +
                " candidateSections=" + bestBySection.size() + " returned=" + ordered.size() +
                " totalMs=" + (System.currentTimeMillis() - startedAt)
        );
        return ordered;
    }

    private void collectGuideResults(
        PackRepository.QueryTerms queryTerms,
        List<QueryRouteProfile.RouteSearchSpec> routeSpecs,
        int candidateLimit,
        int targetTotal,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        long startedAt = System.currentTimeMillis();
        int beforeCount = bestBySection.size();
        collectRouteResults(
            RouteResultKind.GUIDE,
            queryTerms,
            routeSpecs,
            candidateLimit,
            targetTotal,
            false,
            bestBySection
        );
        Log.d(
            TAG,
            "routeGuideSearch query=\"" + queryTerms.queryLower + "\" specs=" + routeSpecs.size() +
                " added=" + Math.max(0, bestBySection.size() - beforeCount) +
                " total=" + bestBySection.size() +
                " totalMs=" + (System.currentTimeMillis() - startedAt)
        );
    }

    private void collectRouteResults(
        RouteResultKind resultKind,
        PackRepository.QueryTerms queryTerms,
        List<QueryRouteProfile.RouteSearchSpec> routeSpecs,
        int candidateLimit,
        int targetTotal,
        boolean scanFullRouteCursor,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        for (QueryRouteProfile.RouteSearchSpec routeSpec : routeSpecs) {
            if (shouldStopRouteSpecSweep(resultKind, scanFullRouteCursor, bestBySection.size(), targetTotal)) {
                break;
            }
            PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec);
            if (routeStep == null) {
                continue;
            }
            int addedWithFts = collectFtsResults(
                resultKind,
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
            collectLikeResults(
                resultKind,
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
    }

    private boolean shouldStopRouteSpecSweep(
        RouteResultKind resultKind,
        boolean scanFullRouteCursor,
        int currentSize,
        int targetTotal
    ) {
        return (resultKind != RouteResultKind.CHUNK || !scanFullRouteCursor) && currentSize >= targetTotal;
    }

    private int collectFtsResults(
        RouteResultKind resultKind,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> categories,
        int candidateLimit,
        int targetTotal,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        if (!ftsAvailable) {
            return 0;
        }
        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = ftsPlan(
            resultKind,
            queryTerms,
            specTerms,
            categories,
            candidateLimit,
            ftsTableName,
            ftsSupportsBm25
        );
        if (plan.isEmpty()) {
            Log.d(
                TAG,
                resultKind.ftsLogName + ".skip query=\"" + queryTerms.queryLower +
                    "\" reason=" + plan.noOpReason
            );
            return 0;
        }

        try (Cursor cursor = database.rawQuery(plan.sql, plan.argsArray())) {
            int added = collectCursor(
                resultKind,
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                targetTotal
            );
            Log.d(
                TAG,
                resultKind.ftsLogName + " query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + plan.ftsQuery +
                    "\" added=" + added + " candidateLimit=" + plan.effectiveCandidateLimit +
                    " order=" + plan.orderLabel
            );
            return added;
        } catch (SQLiteException error) {
            Log.w(
                TAG,
                resultKind.ftsLogName + ".fail query=\"" + queryTerms.queryLower +
                    "\" ftsQuery=\"" + plan.ftsQuery + "\"",
                error
            );
            return 0;
        }
    }

    private int collectLikeResults(
        RouteResultKind resultKind,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> tokens,
        List<String> categories,
        int candidateLimit,
        int targetTotal,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan plan = likePlan(resultKind, tokens, categories, candidateLimit);
        if (plan.isEmpty()) {
            Log.d(
                TAG,
                resultKind.likeLogName + ".skip query=\"" + queryTerms.queryLower +
                    "\" reason=" + plan.noOpReason
            );
            return 0;
        }

        try (Cursor cursor = database.rawQuery(plan.sql, plan.argsArray())) {
            return collectCursor(resultKind, cursor, queryTerms, specTerms, routeSpec, bestBySection, targetTotal);
        } catch (SQLiteException error) {
            Log.w(TAG, resultKind.likeLogName + ".fail query=\"" + queryTerms.queryLower + "\"", error);
            return 0;
        }
    }

    private PackRouteSearchSqlPolicy.RouteFtsSqlPlan ftsPlan(
        RouteResultKind resultKind,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        List<String> categories,
        int candidateLimit,
        String ftsTableName,
        boolean ftsSupportsBm25
    ) {
        if (resultKind == RouteResultKind.CHUNK) {
            return PackRouteSearchSqlPolicy.chunkFtsPlan(
                queryTerms,
                specTerms,
                categories,
                candidateLimit,
                ftsTableName,
                ftsSupportsBm25
            );
        }
        return PackRouteSearchSqlPolicy.guideFtsPlan(
            queryTerms,
            specTerms,
            categories,
            candidateLimit,
            ftsTableName,
            ftsSupportsBm25
        );
    }

    private PackRouteSearchSqlPolicy.RouteLikeSqlPlan likePlan(
        RouteResultKind resultKind,
        List<String> tokens,
        List<String> categories,
        int candidateLimit
    ) {
        if (resultKind == RouteResultKind.CHUNK) {
            return PackRouteSearchSqlPolicy.chunkLikePlan(tokens, categories, candidateLimit);
        }
        return PackRouteSearchSqlPolicy.guideLikePlan(tokens, categories, candidateLimit);
    }

    private int collectCursor(
        RouteResultKind resultKind,
        Cursor cursor,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection,
        int targetTotal
    ) {
        if (resultKind == RouteResultKind.CHUNK) {
            return PackRouteFocusedCandidateCollector.collectChunkCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                targetTotal
            );
        }
        return PackRouteFocusedCandidateCollector.collectGuideCursor(
            cursor,
            queryTerms,
            specTerms,
            routeSpec,
            bestBySection,
            targetTotal
        );
    }

    private enum RouteResultKind {
        CHUNK("routeChunkFts", "routeChunkLike"),
        GUIDE("routeGuideFts", "routeGuideLike");

        final String ftsLogName;
        final String likeLogName;

        RouteResultKind(String ftsLogName, String likeLogName) {
            this.ftsLogName = ftsLogName;
            this.likeLogName = likeLogName;
        }
    }
}

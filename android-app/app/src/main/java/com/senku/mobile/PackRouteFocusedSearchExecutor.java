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
        for (QueryRouteProfile.RouteSearchSpec routeSpec : routeSpecs) {
            if (!scanFullRouteCursor && bestBySection.size() >= candidateTarget) {
                break;
            }
            PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec);
            if (routeStep == null) {
                continue;
            }
            int addedWithFts = collectChunkResultsFts(
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
            collectChunkResultsLike(
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

        int guideSearchThreshold = PackRepository.routeGuideSearchThreshold(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            compactGuideSweep,
            limit
        );
        guideSearchThreshold = PackRepository.runtimeRouteGuideSearchThreshold(
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

    private int collectChunkResultsFts(
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> categories,
        int candidateLimit,
        int candidateTarget,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        if (!ftsAvailable) {
            return 0;
        }
        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = PackRouteSearchSqlPolicy.chunkFtsPlan(
            queryTerms,
            specTerms,
            categories,
            candidateLimit,
            ftsTableName,
            ftsSupportsBm25
        );
        if (plan.isEmpty()) {
            Log.d(TAG, "routeChunkFts.skip query=\"" + queryTerms.queryLower + "\" reason=empty_fts_query");
            return 0;
        }

        try (Cursor cursor = database.rawQuery(plan.sql, plan.argsArray())) {
            int added = PackRouteFocusedCandidateCollector.collectChunkCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                candidateTarget
            );
            Log.d(
                TAG,
                "routeChunkFts query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + plan.ftsQuery +
                    "\" added=" + added + " candidateLimit=" + plan.effectiveCandidateLimit +
                    " order=" + plan.orderLabel
            );
            return added;
        } catch (SQLiteException error) {
            Log.w(
                TAG,
                "routeChunkFts.fail query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + plan.ftsQuery + "\"",
                error
            );
            return 0;
        }
    }

    private int collectChunkResultsLike(
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> tokens,
        List<String> categories,
        int candidateLimit,
        int candidateTarget,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan plan = PackRouteSearchSqlPolicy.chunkLikePlan(
            tokens,
            categories,
            candidateLimit
        );
        if (plan.isEmpty()) {
            Log.d(TAG, "routeChunkLike.skip query=\"" + queryTerms.queryLower + "\" reason=empty_like_plan");
            return 0;
        }

        try (Cursor cursor = database.rawQuery(plan.sql, plan.argsArray())) {
            return PackRouteFocusedCandidateCollector.collectChunkCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                candidateTarget
            );
        } catch (SQLiteException error) {
            Log.w(TAG, "routeChunkLike.fail query=\"" + queryTerms.queryLower + "\"", error);
            return 0;
        }
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
        for (QueryRouteProfile.RouteSearchSpec routeSpec : routeSpecs) {
            if (bestBySection.size() >= targetTotal) {
                break;
            }
            PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec);
            if (routeStep == null) {
                continue;
            }
            int addedWithFts = collectGuideResultsFts(
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
            collectGuideResultsLike(
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

    private int collectGuideResultsFts(
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
        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = PackRouteSearchSqlPolicy.guideFtsPlan(
            queryTerms,
            specTerms,
            categories,
            candidateLimit,
            ftsTableName,
            ftsSupportsBm25
        );
        if (plan.isEmpty()) {
            Log.d(TAG, "routeGuideFts.skip query=\"" + queryTerms.queryLower + "\" reason=empty_fts_query");
            return 0;
        }

        try (Cursor cursor = database.rawQuery(plan.sql, plan.argsArray())) {
            int added = PackRouteFocusedCandidateCollector.collectGuideCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                targetTotal
            );
            Log.d(
                TAG,
                "routeGuideFts query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + plan.ftsQuery +
                    "\" added=" + added + " candidateLimit=" + plan.effectiveCandidateLimit +
                    " order=" + plan.orderLabel
            );
            return added;
        } catch (SQLiteException error) {
            Log.w(
                TAG,
                "routeGuideFts.fail query=\"" + queryTerms.queryLower + "\" ftsQuery=\"" + plan.ftsQuery + "\"",
                error
            );
            return 0;
        }
    }

    private int collectGuideResultsLike(
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        List<String> tokens,
        List<String> categories,
        int candidateLimit,
        int targetTotal,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan plan = PackRouteSearchSqlPolicy.guideLikePlan(
            tokens,
            categories,
            candidateLimit
        );
        if (plan.isEmpty()) {
            Log.d(TAG, "routeGuideLike.skip query=\"" + queryTerms.queryLower + "\" reason=empty_like_plan");
            return 0;
        }

        try (Cursor cursor = database.rawQuery(plan.sql, plan.argsArray())) {
            return PackRouteFocusedCandidateCollector.collectGuideCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                targetTotal
            );
        } catch (SQLiteException error) {
            Log.w(TAG, "routeGuideLike.fail query=\"" + queryTerms.queryLower + "\"", error);
            return 0;
        }
    }
}

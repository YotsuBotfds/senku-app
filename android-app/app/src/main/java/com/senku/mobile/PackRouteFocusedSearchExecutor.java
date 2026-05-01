package com.senku.mobile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
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
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec, 6);
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

        LinkedHashMap<String, Integer> guideTotals = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> guideSectionCounts = new LinkedHashMap<>();
        for (PackRepository.ScoredSearchResult sectionScore : bestBySection.values()) {
            String guideKey = PackSupportScoringPolicy.guideGroupKey(sectionScore.result);
            guideTotals.put(guideKey, guideTotals.getOrDefault(guideKey, 0) + Math.max(1, sectionScore.score));
            guideSectionCounts.put(guideKey, guideSectionCounts.getOrDefault(guideKey, 0) + 1);
        }

        ArrayList<PackRepository.ScoredSearchResult> scored = new ArrayList<>();
        boolean conservativeWaterGuideBundling = "water_storage".equals(queryTerms.metadataProfile.preferredStructureType())
            && !queryTerms.metadataProfile.hasExplicitTopic("water_distribution");
        for (PackRepository.ScoredSearchResult sectionScore : bestBySection.values()) {
            String guideKey = PackSupportScoringPolicy.guideGroupKey(sectionScore.result);
            int guideBonus = Math.min(28, guideTotals.getOrDefault(guideKey, 0) / 4);
            int diversityBonus = Math.min(10, guideSectionCounts.getOrDefault(guideKey, 0) * 2);
            if (conservativeWaterGuideBundling) {
                guideBonus = 0;
                diversityBonus = 0;
            }
            int score = sectionScore.score + guideBonus + diversityBonus;
            if (conservativeWaterGuideBundling) {
                score += PackRepository.broadWaterRouteRefinementBonus(queryTerms, sectionScore.result);
            }
            score += PackRepository.currentHeadRouteRefinementBonus(queryTerms, sectionScore.result);
            scored.add(new PackRepository.ScoredSearchResult(
                sectionScore.result,
                sectionScore.originalIndex,
                score
            ));
        }

        scored.sort((left, right) -> {
            if (conservativeWaterGuideBundling) {
                int priorityOrder = Integer.compare(
                    PackRepository.broadWaterRouteOrderingPriority(queryTerms, right.result),
                    PackRepository.broadWaterRouteOrderingPriority(queryTerms, left.result)
                );
                if (priorityOrder != 0) {
                    return priorityOrder;
                }
            }
            int currentHeadPriorityOrder = Integer.compare(
                PackRepository.currentHeadRouteOrderingPriority(queryTerms, right.result),
                PackRepository.currentHeadRouteOrderingPriority(queryTerms, left.result)
            );
            if (currentHeadPriorityOrder != 0) {
                return currentHeadPriorityOrder;
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
        for (PackRepository.ScoredSearchResult item : scored) {
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
        String ftsQuery = PackRepository.buildFtsQuery(specTerms, ftsSupportsBm25 ? 8 : 4, ftsSupportsBm25);
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
            return PackRouteFocusedCandidateCollector.collectChunkCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                candidateTarget
            );
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
                PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec, 6);
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
        String ftsQuery = PackRepository.buildFtsQuery(specTerms, ftsSupportsBm25 ? 8 : 4, ftsSupportsBm25);
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
            return PackRouteFocusedCandidateCollector.collectGuideCursor(
                cursor,
                queryTerms,
                specTerms,
                routeSpec,
                bestBySection,
                targetTotal
            );
        }
    }
}

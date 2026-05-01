package com.senku.mobile;

import android.database.Cursor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

final class PackRouteFocusedCandidateCollector {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackRouteFocusedCandidateCollector() {
    }

    static int collectChunkCursor(
        Cursor cursor,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection,
        int candidateTarget
    ) {
        return collectCursor(
            cursor,
            CandidateKind.CHUNK,
            queryTerms,
            specTerms,
            routeSpec,
            bestBySection,
            candidateTarget
        );
    }

    static int collectGuideCursor(
        Cursor cursor,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection,
        int targetTotal
    ) {
        return collectCursor(
            cursor,
            CandidateKind.GUIDE,
            queryTerms,
            specTerms,
            routeSpec,
            bestBySection,
            targetTotal
        );
    }

    static int collectRows(
        List<RouteCandidateRow> rows,
        CandidateKind candidateKind,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection,
        int targetTotal
    ) {
        int beforeCount = bestBySection.size();
        boolean scanFullCursor = PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(queryTerms);
        for (RouteCandidateRow row : rows) {
            collectRow(row, candidateKind, queryTerms, specTerms, routeSpec, bestBySection);
            if (!scanFullCursor && bestBySection.size() >= targetTotal) {
                break;
            }
        }
        return bestBySection.size() - beforeCount;
    }

    private static int collectCursor(
        Cursor cursor,
        CandidateKind candidateKind,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection,
        int targetTotal
    ) {
        int beforeCount = bestBySection.size();
        boolean scanFullCursor = PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(queryTerms);
        while (cursor.moveToNext()) {
            collectRow(rowFromCursor(cursor, candidateKind), candidateKind, queryTerms, specTerms, routeSpec, bestBySection);
            if (!scanFullCursor && bestBySection.size() >= targetTotal) {
                break;
            }
        }
        return bestBySection.size() - beforeCount;
    }

    private static void collectRow(
        RouteCandidateRow row,
        CandidateKind candidateKind,
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection
    ) {
        if (row == null || queryTerms == null || queryTerms.routeProfile == null || routeSpec == null) {
            return;
        }
        if (!PackRouteRowFilterPolicy.matchesSpecializedExplicitTopicRow(queryTerms, row.structureType, row.topicTags)) {
            return;
        }
        if (!queryTerms.routeProfile.supportsRouteResult(
            row.title.toLowerCase(QUERY_LOCALE),
            row.section.toLowerCase(QUERY_LOCALE),
            row.category.toLowerCase(QUERY_LOCALE),
            row.tags.toLowerCase(QUERY_LOCALE),
            row.description.toLowerCase(QUERY_LOCALE),
            row.body.toLowerCase(QUERY_LOCALE)
        )) {
            return;
        }
        if (!PackRouteRowFilterPolicy.matchesSpecializedRouteMetadata(
            queryTerms,
            row.section,
            row.structureType,
            row.topicTags
        )) {
            return;
        }

        int sectionHeadingScore = queryTerms.metadataProfile.sectionHeadingBonus(row.section);
        if (!PackRouteRowFilterPolicy.shouldKeepBroadWaterRouteRow(
            queryTerms,
            row.section,
            row.contentRole,
            row.structureType,
            row.topicTags,
            sectionHeadingScore
        )) {
            return;
        }
        if (!PackRouteRowFilterPolicy.shouldKeepBroadHouseRouteRow(
            queryTerms,
            row.section,
            row.category,
            row.structureType,
            row.topicTags,
            sectionHeadingScore
        )) {
            return;
        }

        String combinedTags = PackQueryPipelineHelper.combineTags(row.tags, row.topicTags);
        int specScore = PackKeywordScoringPolicy.keywordScore(
            specTerms,
            row.title,
            row.section,
            row.category,
            combinedTags,
            row.description,
            row.body
        );
        specScore += PackKeywordScoringPolicy.metadataBonus(
            specTerms,
            row.category,
            row.contentRole,
            row.timeHorizon,
            row.structureType,
            row.topicTags
        );
        int queryScore = PackKeywordScoringPolicy.keywordScore(
            queryTerms,
            row.title,
            row.section,
            row.category,
            combinedTags,
            row.description,
            row.body
        );
        queryScore += PackKeywordScoringPolicy.metadataBonus(
            queryTerms,
            row.category,
            row.contentRole,
            row.timeHorizon,
            row.structureType,
            row.topicTags
        );
        int score = specScore + Math.max(0, queryScore / 2) + routeSpec.bonus()
            + sectionHeadingScore + candidateKind.scoreBonus;
        if (score <= 0) {
            return;
        }

        SearchResult result = candidateKind.toSearchResult(row);
        if (!PackRouteRowFilterPolicy.shouldKeepSpecializedDirectSignalRouteResult(queryTerms, result)) {
            return;
        }
        String sectionKey = PackQueryPipelineHelper.guideSectionKey(
            row.guideId,
            row.title,
            candidateKind.resultSection(row)
        );
        PackRepository.ScoredSearchResult existing = bestBySection.get(sectionKey);
        PackRepository.ScoredSearchResult candidate = new PackRepository.ScoredSearchResult(
            result,
            bestBySection.size(),
            score + candidateKind.candidateScoreBonus
        );
        if (existing == null ||
            candidate.score > existing.score ||
            (candidate.score == existing.score && result.body.length() > existing.result.body.length())) {
            bestBySection.put(sectionKey, candidate);
        }
    }

    private static RouteCandidateRow rowFromCursor(Cursor cursor, CandidateKind candidateKind) {
        if (candidateKind == CandidateKind.CHUNK) {
            return new RouteCandidateRow(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8),
                cursor.getString(9),
                cursor.getString(10)
            );
        }

        String guideId = PackQueryPipelineHelper.emptySafe(cursor.getString(0));
        String title = PackQueryPipelineHelper.emptySafe(cursor.getString(1));
        String section = cursor.getColumnCount() > 9 ? PackQueryPipelineHelper.emptySafe(cursor.getString(2)) : "";
        int categoryIndex = cursor.getColumnCount() > 9 ? 3 : 2;
        int descriptionIndex = cursor.getColumnCount() > 9 ? 4 : 3;
        int bodyIndex = cursor.getColumnCount() > 9 ? 5 : 4;
        int contentRoleIndex = cursor.getColumnCount() > 9 ? 6 : 5;
        int timeHorizonIndex = cursor.getColumnCount() > 9 ? 7 : 6;
        int structureTypeIndex = cursor.getColumnCount() > 9 ? 8 : 7;
        int topicTagsIndex = cursor.getColumnCount() > 9 ? 9 : 8;
        int tagsIndex = cursor.getColumnCount() > 10 ? 10 : -1;
        return new RouteCandidateRow(
            title,
            guideId,
            section,
            cursor.getString(categoryIndex),
            cursor.getString(bodyIndex),
            tagsIndex >= 0 ? cursor.getString(tagsIndex) : "",
            cursor.getString(descriptionIndex),
            cursor.getString(contentRoleIndex),
            cursor.getString(timeHorizonIndex),
            cursor.getString(structureTypeIndex),
            cursor.getString(topicTagsIndex)
        );
    }

    enum CandidateKind {
        CHUNK(0, 18, "route-focus") {
            @Override
            SearchResult toSearchResult(RouteCandidateRow row) {
                return new SearchResult(
                    row.title,
                    row.guideId + " | " + row.category + " | " + row.section + " | route-focus",
                    PackQueryPipelineHelper.clip(row.body, 220),
                    row.body,
                    row.guideId,
                    row.section,
                    row.category,
                    retrievalMode,
                    row.contentRole,
                    row.timeHorizon,
                    row.structureType,
                    row.topicTags
                );
            }

            @Override
            String resultSection(RouteCandidateRow row) {
                return row.section;
            }
        },
        GUIDE(20, 0, "guide-focus") {
            @Override
            SearchResult toSearchResult(RouteCandidateRow row) {
                return new SearchResult(
                    row.title,
                    row.guideId + " | " + row.category + " | guide-focus",
                    PackQueryPipelineHelper.clip(row.body, 220),
                    row.body,
                    row.guideId,
                    "",
                    row.category,
                    retrievalMode,
                    row.contentRole,
                    row.timeHorizon,
                    row.structureType,
                    row.topicTags
                );
            }

            @Override
            String resultSection(RouteCandidateRow row) {
                return "";
            }
        };

        final int scoreBonus;
        final int candidateScoreBonus;
        final String retrievalMode;

        CandidateKind(int scoreBonus, int candidateScoreBonus, String retrievalMode) {
            this.scoreBonus = scoreBonus;
            this.candidateScoreBonus = candidateScoreBonus;
            this.retrievalMode = retrievalMode;
        }

        abstract SearchResult toSearchResult(RouteCandidateRow row);

        abstract String resultSection(RouteCandidateRow row);
    }

    static final class RouteCandidateRow {
        final String title;
        final String guideId;
        final String section;
        final String category;
        final String body;
        final String tags;
        final String description;
        final String contentRole;
        final String timeHorizon;
        final String structureType;
        final String topicTags;

        RouteCandidateRow(
            String title,
            String guideId,
            String section,
            String category,
            String body,
            String tags,
            String description,
            String contentRole,
            String timeHorizon,
            String structureType,
            String topicTags
        ) {
            this.title = PackQueryPipelineHelper.emptySafe(title);
            this.guideId = PackQueryPipelineHelper.emptySafe(guideId);
            this.section = PackQueryPipelineHelper.emptySafe(section);
            this.category = PackQueryPipelineHelper.emptySafe(category);
            this.body = PackQueryPipelineHelper.emptySafe(body);
            this.tags = PackQueryPipelineHelper.emptySafe(tags);
            this.description = PackQueryPipelineHelper.emptySafe(description);
            this.contentRole = PackQueryPipelineHelper.emptySafe(contentRole);
            this.timeHorizon = PackQueryPipelineHelper.emptySafe(timeHorizon);
            this.structureType = PackQueryPipelineHelper.emptySafe(structureType);
            this.topicTags = PackQueryPipelineHelper.emptySafe(topicTags);
        }
    }
}

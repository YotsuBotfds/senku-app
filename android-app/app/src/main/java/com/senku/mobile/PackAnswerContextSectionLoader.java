package com.senku.mobile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

final class PackAnswerContextSectionLoader {
    private static final Locale QUERY_LOCALE = Locale.US;

    private final SQLiteDatabase database;

    PackAnswerContextSectionLoader(SQLiteDatabase database) {
        this.database = database;
    }

    List<SearchResult> loadGuideSectionsForAnswer(
        PackRepository.QueryTerms queryTerms,
        SearchResult anchor,
        int limit
    ) {
        if (anchor == null || PackRepository.emptySafe(anchor.guideId).isEmpty()) {
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
                String title = PackRepository.emptySafe(cursor.getString(0));
                String guideId = PackRepository.emptySafe(cursor.getString(1));
                String section = PackRepository.emptySafe(cursor.getString(2));
                String category = PackRepository.emptySafe(cursor.getString(3));
                String document = PackRepository.emptySafe(cursor.getString(4));
                String tags = PackRepository.emptySafe(cursor.getString(5));
                String description = PackRepository.emptySafe(cursor.getString(6));
                String contentRole = PackRepository.emptySafe(cursor.getString(7));
                String timeHorizon = PackRepository.emptySafe(cursor.getString(8));
                String structureType = PackRepository.emptySafe(cursor.getString(9));
                String topicTags = PackRepository.emptySafe(cursor.getString(10));

                int score = PackRepository.lexicalKeywordScore(queryTerms, title, section, category, tags, description, document);
                score += PackRepository.metadataBonus(queryTerms, category, contentRole, timeHorizon, structureType, topicTags);
                int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(section);
                score += sectionBonus;
                boolean isAnchorSection = normalizeSection(section).equals(normalizeSection(anchor.sectionHeading));
                SearchResult candidatePreview = new SearchResult(
                    title,
                    guideId + " | " + category + " | " + section + " | guide-focus",
                    PackRepository.clip(document, 220),
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
                    candidate = new SectionCandidate(
                        title,
                        guideId,
                        section,
                        category,
                        contentRole,
                        timeHorizon,
                        structureType,
                        topicTags
                    );
                    sections.put(key, candidate);
                }
                candidate.consider(score, document, isAnchorSection, contentRole, timeHorizon, structureType, topicTags);
            }
        } catch (SQLiteException ignored) {
            return Collections.emptyList();
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
                PackRepository.clip(candidate.body.toString(), 220),
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

    static boolean shouldKeepGuideSectionForContextForTest(
        String query,
        SearchResult candidate,
        boolean isAnchorSection
    ) {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(candidate.sectionHeading);
        return shouldKeepGuideSectionForContext(queryTerms, candidate, isAnchorSection, sectionBonus);
    }

    private static boolean shouldKeepGuideSectionForContext(
        PackRepository.QueryTerms queryTerms,
        SearchResult candidate,
        boolean isAnchorSection,
        int sectionBonus
    ) {
        String preferredStructure = PackRepository.emptySafe(queryTerms.metadataProfile.preferredStructureType())
            .trim()
            .toLowerCase(QUERY_LOCALE);
        return PackAnswerContextPolicy.shouldKeepGuideSectionForContext(
            preferredStructure,
            isAnchorSection,
            sectionBonus,
            PackRepository.prefersRoofWeatherproofRouteAnchor(queryTerms),
            PackRouteSignalPolicy.hasRoofWeatherproofDistractorSignal(candidate),
            PackRouteSignalPolicy.prefersGovernanceTrustRepairContext(queryTerms.metadataProfile),
            PackRouteSignalPolicy.hasGovernanceSupportMixDistractor(candidate),
            PackRouteSignalPolicy.hasGovernanceTrustRepairSignal(candidate)
        );
    }

    private static String normalizeSection(String section) {
        return PackRepository.emptySafe(section).replaceAll("\\s+", " ").trim().toLowerCase(QUERY_LOCALE);
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
            String safeDocument = PackRepository.emptySafe(document).trim();
            if (safeDocument.isEmpty()) {
                return;
            }
            if (body.length() > 0) {
                body.append("\n\n");
            }
            body.append(safeDocument);
        }
    }
}

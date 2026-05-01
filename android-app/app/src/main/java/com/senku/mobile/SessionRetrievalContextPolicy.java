package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SessionRetrievalContextPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final int MAX_RETRIEVAL_TERMS = 10;
    private static final int MAX_RETRIEVAL_SOURCE_GUIDES = 2;
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

    private SessionRetrievalContextPolicy() {
    }

    static QueryPlan plan(
        String question,
        boolean shouldUseContext,
        String latestQuestion,
        String latestAnswerSummary,
        List<SearchResult> latestSourceResults,
        QueryMetadataProfile sessionProfile
    ) {
        String trimmed = safe(question).trim();
        int directFocusTokenCount = retrievalTokens(trimmed).size();
        boolean useSessionContext = shouldUseContext && !safe(latestQuestion).trim().isEmpty();
        if (!useSessionContext) {
            return new QueryPlan(
                trimmed,
                trimmed,
                trimmed,
                directFocusTokenCount,
                false
            );
        }

        String legacyRetrievalQuery = buildLegacyRetrievalQuery(
            trimmed,
            latestQuestion,
            latestAnswerSummary,
            latestSourceResults,
            sessionProfile
        );
        String contextSelectionQuery = buildStructuredContextQuery(
            trimmed,
            latestQuestion,
            latestAnswerSummary,
            latestSourceResults,
            sessionProfile
        );
        if (contextSelectionQuery.isEmpty()) {
            contextSelectionQuery = legacyRetrievalQuery;
        }
        if (contextSelectionQuery.isEmpty()) {
            contextSelectionQuery = trimmed;
        }

        boolean focusedHouseFollowUp = isFocusedHouseFollowUp(trimmed, sessionProfile);
        String searchQuery = directFocusTokenCount >= 3 && !focusedHouseFollowUp ? trimmed : contextSelectionQuery;
        if (searchQuery.isEmpty()) {
            searchQuery = legacyRetrievalQuery.isEmpty() ? trimmed : legacyRetrievalQuery;
        }
        return new QueryPlan(
            searchQuery,
            contextSelectionQuery,
            legacyRetrievalQuery,
            directFocusTokenCount,
            true
        );
    }

    static String buildLegacyRetrievalQuery(
        String question,
        boolean shouldUseContext,
        String latestQuestion,
        String latestAnswerSummary,
        List<SearchResult> latestSourceResults
    ) {
        if (!shouldUseContext) {
            return safe(question).trim();
        }
        QueryMetadataProfile sessionProfile = QueryMetadataProfile.fromQuery(
            safe(latestQuestion) + " " + safe(question).trim()
        );
        return buildLegacyRetrievalQuery(
            question,
            latestQuestion,
            latestAnswerSummary,
            latestSourceResults,
            sessionProfile
        );
    }

    private static String buildLegacyRetrievalQuery(
        String question,
        String latestQuestion,
        String latestAnswerSummary,
        List<SearchResult> latestSourceResults,
        QueryMetadataProfile sessionProfile
    ) {
        LinkedHashSet<String> retrievalTerms = new LinkedHashSet<>(retrievalTokens(question));
        if (!safe(latestQuestion).isEmpty()) {
            addSourceContextTerms(
                retrievalTerms,
                prioritizeSourceContextResults(question, latestSourceResults),
                MAX_RETRIEVAL_SOURCE_GUIDES,
                latestQuestion,
                question,
                sessionProfile
            );
            if (retrievalTerms.size() < 7 && sessionProfile != null && !sessionProfile.preferredTopicTags().isEmpty()) {
                addContextTerms(retrievalTerms, latestAnswerSummary, 3, true);
            }
            if (retrievalTerms.size() < 4) {
                addContextTerms(retrievalTerms, latestQuestion, 4, true);
            }
        }
        return renderTermsOrFocus(retrievalTerms, question);
    }

    private static String buildStructuredContextQuery(
        String question,
        String latestQuestion,
        String latestAnswerSummary,
        List<SearchResult> latestSourceResults,
        QueryMetadataProfile sessionProfile
    ) {
        LinkedHashSet<String> contextTerms = new LinkedHashSet<>(retrievalTokens(question));
        boolean hasStructuredHints = hasStructuredSessionHints(sessionProfile);
        boolean focusedHouseFollowUp = isFocusedHouseFollowUp(question, sessionProfile);
        boolean seasonalHouseSiteFollowUp = isSeasonalHouseSiteFollowUp(question, sessionProfile);
        int directFocusTokenCount = retrievalTokens(question).size();
        if (!safe(latestQuestion).isEmpty()) {
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
                        prioritizeSourceContextResults(question, latestSourceResults),
                        MAX_RETRIEVAL_SOURCE_GUIDES,
                        latestQuestion,
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
                addContextTerms(contextTerms, latestQuestion, 2, true);
            }
            if (contextTerms.size() < 2) {
                addContextTerms(contextTerms, latestAnswerSummary, 2, true);
            }
        }
        return renderTermsOrFocus(contextTerms, question);
    }

    private static String renderTermsOrFocus(LinkedHashSet<String> terms, String question) {
        if (terms.isEmpty()) {
            return buildRetrievalFocus(question);
        }
        ArrayList<String> ordered = new ArrayList<>();
        for (String term : terms) {
            ordered.add(term);
            if (ordered.size() >= MAX_RETRIEVAL_TERMS) {
                break;
            }
        }
        return String.join(" ", ordered).trim();
    }

    static boolean isFocusedHouseFollowUp(String question, QueryMetadataProfile sessionProfile) {
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

    static Set<String> contentTokens(String text) {
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

    static String buildRetrievalFocus(String question) {
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

    static Set<String> retrievalTokens(String text) {
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

    static boolean shouldIncludeRecentGuides(String question) {
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

    static boolean tokensMatch(String left, String right) {
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

    static String singularize(String token) {
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

    private static String normalizeToken(String token) {
        return safe(token).trim().toLowerCase(QUERY_LOCALE);
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    static final class QueryPlan {
        final String searchQuery;
        final String contextSelectionQuery;
        final String deterministicFallbackQuery;
        final int directFocusTokenCount;
        final boolean sessionUsed;

        QueryPlan(
            String searchQuery,
            String contextSelectionQuery,
            String deterministicFallbackQuery,
            int directFocusTokenCount,
            boolean sessionUsed
        ) {
            this.searchQuery = searchQuery == null ? "" : searchQuery;
            this.contextSelectionQuery = contextSelectionQuery == null ? "" : contextSelectionQuery;
            this.deterministicFallbackQuery = deterministicFallbackQuery == null ? "" : deterministicFallbackQuery;
            this.directFocusTokenCount = directFocusTokenCount;
            this.sessionUsed = sessionUsed;
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
}

package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class AnswerContextSelector {
    private static final int PREFERRED_UNIQUE_GUIDES = 3;
    private static final int MAX_ANCHOR_SECTIONS = 1;
    private static final Set<String> STOP_TOKENS = buildStopTokens();
    private static final Set<String> LOW_SIGNAL_TOKENS = buildLowSignalTokens();
    private static final Map<String, String[]> QUERY_EXPANSIONS = buildQueryExpansions();

    private AnswerContextSelector() {
    }

    public static List<SearchResult> select(List<SearchResult> rankedResults, int limit) {
        return select(rankedResults, limit, "");
    }

    public static List<SearchResult> select(List<SearchResult> rankedResults, int limit, String query) {
        ArrayList<SearchResult> selected = new ArrayList<>();
        if (rankedResults == null || rankedResults.isEmpty() || limit <= 0) {
            return selected;
        }

        SearchResult anchor = rankedResults.get(0);
        selected.add(anchor);

        Set<String> seenGuideKeys = new LinkedHashSet<>();
        Set<String> seenGuideSectionKeys = new LinkedHashSet<>();
        String anchorGuideKey = guideKey(anchor);
        seenGuideKeys.add(anchorGuideKey);
        seenGuideSectionKeys.add(guideSectionKey(anchor));

        QueryProfile queryProfile = QueryProfile.fromQuery(query);
        List<SearchResult> relevantCandidates = rankRelevantCandidates(rankedResults, queryProfile, anchor);
        addAnchorGuideSections(relevantCandidates, selected, anchorGuideKey, seenGuideSectionKeys, limit);
        addUniqueGuides(
            relevantCandidates,
            selected,
            anchorGuideKey,
            seenGuideKeys,
            seenGuideSectionKeys,
            Math.min(limit, PREFERRED_UNIQUE_GUIDES)
        );
        addUniqueGuides(relevantCandidates, selected, anchorGuideKey, seenGuideKeys, seenGuideSectionKeys, limit);
        addUniqueGuideSections(relevantCandidates, selected, seenGuideSectionKeys, limit);

        if (queryProfile.hasMeaningfulTerms()) {
            return selected;
        }

        addAnchorGuideSections(rankedResults, selected, anchorGuideKey, seenGuideSectionKeys, limit);
        addUniqueGuides(
            rankedResults,
            selected,
            anchorGuideKey,
            seenGuideKeys,
            seenGuideSectionKeys,
            Math.min(limit, PREFERRED_UNIQUE_GUIDES)
        );
        addUniqueGuides(rankedResults, selected, anchorGuideKey, seenGuideKeys, seenGuideSectionKeys, limit);
        addUniqueGuideSections(rankedResults, selected, seenGuideSectionKeys, limit);
        addRemaining(rankedResults, selected, limit);
        return selected;
    }

    private static List<SearchResult> rankRelevantCandidates(
        List<SearchResult> rankedResults,
        QueryProfile queryProfile,
        SearchResult anchor
    ) {
        if (!queryProfile.hasMeaningfulTerms()) {
            return Collections.emptyList();
        }

        ArrayList<ScoredCandidate> scored = new ArrayList<>();
        String anchorGuideKey = guideKey(anchor);
        String anchorCategory = normalize(anchor.category);
        for (int index = 1; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            int score = relevanceScore(candidate, queryProfile, anchorGuideKey, anchorCategory);
            if (score <= 0) {
                continue;
            }
            scored.add(new ScoredCandidate(candidate, index, score, guideKey(candidate).equals(anchorGuideKey), normalize(candidate.category).equals(anchorCategory)));
        }

        scored.sort(Comparator
            .comparingInt(ScoredCandidate::score).reversed()
            .thenComparing(ScoredCandidate::sameGuide, Comparator.reverseOrder())
            .thenComparing(ScoredCandidate::sameCategory, Comparator.reverseOrder())
            .thenComparingInt(ScoredCandidate::modePriority).reversed()
            .thenComparingInt(ScoredCandidate::originalIndex));

        ArrayList<SearchResult> ordered = new ArrayList<>();
        for (ScoredCandidate candidate : scored) {
            ordered.add(candidate.result);
        }
        return ordered;
    }

    private static void addAnchorGuideSections(
        List<SearchResult> rankedResults,
        List<SearchResult> selected,
        String anchorGuideKey,
        Set<String> seenGuideSectionKeys,
        int limit
    ) {
        for (SearchResult candidate : rankedResults) {
            if (selected.size() >= limit || selected.size() >= MAX_ANCHOR_SECTIONS) {
                return;
            }
            if (!guideKey(candidate).equals(anchorGuideKey)) {
                continue;
            }
            String guideSectionKey = guideSectionKey(candidate);
            if (seenGuideSectionKeys.contains(guideSectionKey)) {
                continue;
            }
            selected.add(candidate);
            seenGuideSectionKeys.add(guideSectionKey);
        }
    }

    private static void addUniqueGuides(
        List<SearchResult> rankedResults,
        List<SearchResult> selected,
        String anchorGuideKey,
        Set<String> seenGuideKeys,
        Set<String> seenGuideSectionKeys,
        int limit
    ) {
        for (SearchResult candidate : rankedResults) {
            if (selected.size() >= limit) {
                return;
            }
            String candidateGuideKey = guideKey(candidate);
            if (candidateGuideKey.equals(anchorGuideKey)) {
                continue;
            }
            if (seenGuideKeys.contains(candidateGuideKey)) {
                continue;
            }
            selected.add(candidate);
            seenGuideKeys.add(candidateGuideKey);
            seenGuideSectionKeys.add(guideSectionKey(candidate));
        }
    }

    private static void addUniqueGuideSections(
        List<SearchResult> rankedResults,
        List<SearchResult> selected,
        Set<String> seenGuideSectionKeys,
        int limit
    ) {
        for (SearchResult candidate : rankedResults) {
            if (selected.size() >= limit) {
                return;
            }
            String guideSectionKey = guideSectionKey(candidate);
            if (seenGuideSectionKeys.contains(guideSectionKey)) {
                continue;
            }
            selected.add(candidate);
            seenGuideSectionKeys.add(guideSectionKey);
        }
    }

    private static void addRemaining(List<SearchResult> rankedResults, List<SearchResult> selected, int limit) {
        LinkedHashSet<SearchResult> selectedSet = new LinkedHashSet<>(selected);
        for (int index = 1; index < rankedResults.size() && selected.size() < limit; index++) {
            SearchResult candidate = rankedResults.get(index);
            if (selectedSet.contains(candidate)) {
                continue;
            }
            selected.add(candidate);
            selectedSet.add(candidate);
        }
    }

    private static int relevanceScore(
        SearchResult result,
        QueryProfile queryProfile,
        String anchorGuideKey,
        String anchorCategory
    ) {
        String title = normalize(result.title);
        String section = normalize(result.sectionHeading);
        String category = normalize(result.category);
        String snippet = normalize(result.snippet);
        String body = normalize(result.body);
        boolean sameGuide = guideKey(result).equals(anchorGuideKey);

        boolean titlePrimaryMatch = containsAny(title, queryProfile.primaryTokens);
        boolean sectionPrimaryMatch = containsAny(section, queryProfile.primaryTokens);
        boolean titleExpansionMatch = containsAny(title, queryProfile.expansionTokens);
        boolean sectionExpansionMatch = containsAny(section, queryProfile.expansionTokens);
        boolean titleOrSectionMatch = titlePrimaryMatch || sectionPrimaryMatch || titleExpansionMatch || sectionExpansionMatch;
        boolean sectionMatch = sectionPrimaryMatch || sectionExpansionMatch;

        if (sameGuide && !sectionMatch) {
            return 0;
        }

        int score = 0;
        for (String token : queryProfile.primaryTokens) {
            if (title.contains(token)) {
                score += 16;
            }
            if (section.contains(token)) {
                score += 14;
            }
            if (category.contains(token)) {
                score += 4;
            }
            if (titleOrSectionMatch && snippet.contains(token)) {
                score += 5;
            }
            if (titleOrSectionMatch && body.contains(token)) {
                score += 3;
            }
        }
        for (String token : queryProfile.expansionTokens) {
            if (title.contains(token)) {
                score += 10;
            }
            if (section.contains(token)) {
                score += 8;
            }
            if (category.contains(token)) {
                score += 3;
            }
            if (titleOrSectionMatch && snippet.contains(token)) {
                score += 3;
            }
            if (titleOrSectionMatch && body.contains(token)) {
                score += 2;
            }
        }

        if (sameGuide) {
            score += 5;
        }
        if (!anchorCategory.isEmpty() && normalize(result.category).equals(anchorCategory)) {
            score += 3;
        }

        String mode = normalize(result.retrievalMode);
        if ("vector".equals(mode) && !titleOrSectionMatch) {
            return 0;
        }
        if ("hybrid".equals(mode)) {
            score += 3;
        } else if ("lexical".equals(mode)) {
            score += 2;
        } else if ("vector".equals(mode)) {
            score -= 1;
        }

        return score;
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
        Collections.addAll(tokens, "build", "create", "do", "find", "fix", "help", "keep", "make", "start", "use");
        return Collections.unmodifiableSet(tokens);
    }

    private static Map<String, String[]> buildQueryExpansions() {
        LinkedHashMap<String, String[]> expansions = new LinkedHashMap<>();
        expansions.put("boat", new String[]{"watercraft", "vessel", "hull"});
        expansions.put("canoe", new String[]{"watercraft", "boat", "dugout"});
        expansions.put("coracle", new String[]{"watercraft", "boat", "hide"});
        expansions.put("dugout", new String[]{"canoe", "watercraft", "boat"});
        expansions.put("kayak", new String[]{"watercraft", "boat", "paddle"});
        expansions.put("oar", new String[]{"paddle", "rowing"});
        expansions.put("paddle", new String[]{"oar", "rowing"});
        expansions.put("raft", new String[]{"watercraft", "boat", "flotation"});
        return Collections.unmodifiableMap(expansions);
    }

    private static String guideKey(SearchResult result) {
        String guideId = safe(result.guideId);
        if (!guideId.isEmpty()) {
            return normalize(guideId);
        }
        return normalize(result.title);
    }

    private static String guideSectionKey(SearchResult result) {
        String guideKey = guideKey(result);
        String section = safe(result.sectionHeading);
        if (section.isEmpty()) {
            return guideKey;
        }
        return guideKey + "::" + normalize(section);
    }

    private static String normalize(String value) {
        return safe(value).replaceAll("\\s+", " ").trim().toLowerCase(Locale.US);
    }

    private static boolean containsAny(String haystack, List<String> needles) {
        if (haystack.isEmpty() || needles.isEmpty()) {
            return false;
        }
        for (String needle : needles) {
            if (!needle.isEmpty() && haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class ScoredCandidate {
        final SearchResult result;
        final int originalIndex;
        final int score;
        final boolean sameGuide;
        final boolean sameCategory;

        ScoredCandidate(SearchResult result, int originalIndex, int score, boolean sameGuide, boolean sameCategory) {
            this.result = result;
            this.originalIndex = originalIndex;
            this.score = score;
            this.sameGuide = sameGuide;
            this.sameCategory = sameCategory;
        }

        int score() {
            return score;
        }

        boolean sameGuide() {
            return sameGuide;
        }

        boolean sameCategory() {
            return sameCategory;
        }

        int modePriority() {
            String mode = normalize(result.retrievalMode);
            if ("hybrid".equals(mode)) {
                return 3;
            }
            if ("lexical".equals(mode)) {
                return 2;
            }
            if ("vector".equals(mode)) {
                return 1;
            }
            return 0;
        }

        int originalIndex() {
            return originalIndex;
        }
    }

    private static final class QueryProfile {
        final List<String> primaryTokens;
        final List<String> expansionTokens;

        QueryProfile(List<String> primaryTokens, List<String> expansionTokens) {
            this.primaryTokens = primaryTokens;
            this.expansionTokens = expansionTokens;
        }

        static QueryProfile fromQuery(String query) {
            LinkedHashSet<String> primary = new LinkedHashSet<>();
            String[] rawTokens = safe(query).toLowerCase(Locale.US).split("[^a-z0-9-]+");
            for (String token : rawTokens) {
                if (token.length() < 2 || STOP_TOKENS.contains(token) || LOW_SIGNAL_TOKENS.contains(token)) {
                    continue;
                }
                primary.add(token);
                if (primary.size() >= 6) {
                    break;
                }
            }

            LinkedHashSet<String> expansions = new LinkedHashSet<>();
            for (String token : primary) {
                String[] mapped = QUERY_EXPANSIONS.get(token);
                if (mapped != null) {
                    Collections.addAll(expansions, mapped);
                }
            }
            expansions.removeAll(primary);

            return new QueryProfile(new ArrayList<>(primary), new ArrayList<>(expansions));
        }

        boolean hasMeaningfulTerms() {
            return !primaryTokens.isEmpty() || !expansionTokens.isEmpty();
        }
    }
}

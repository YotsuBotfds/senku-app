package com.senku.mobile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

final class PackRouteAnchorBiasPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;
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

    private PackRouteAnchorBiasPolicy() {
    }

    static boolean prefersCabinSiteSelectionRouteAnchor(PackRepository.QueryTerms queryTerms) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return false;
        }
        return "cabin_house".equals(queryTerms.metadataProfile.preferredStructureType())
            && queryTerms.metadataProfile.siteSelectionLeadIntent()
            && queryTerms.metadataProfile.hasExplicitTopic("site_selection")
            && queryTerms.metadataProfile.hasExplicitTopic("foundation");
    }

    static boolean hasCabinSiteSelectionAnchorSignal(SearchResult candidate) {
        if (candidate == null) {
            return false;
        }
        return PackTextMatchPolicy.containsAnyMarker(
            titleSectionText(candidate),
            HOUSE_SITE_SELECTION_ANCHOR_MARKERS
        );
    }

    static int cabinSiteSelectionAnchorBias(PackRepository.QueryTerms queryTerms, SearchResult candidate) {
        if (!prefersCabinSiteSelectionRouteAnchor(queryTerms) || candidate == null) {
            return 0;
        }
        int score = 0;
        String normalizedCategory = normalized(candidate.category);
        boolean siteSignal = hasCabinSiteSelectionAnchorSignal(candidate);
        boolean foundationOnlySignal = PackTextMatchPolicy.containsAnyMarker(
            titleSectionText(candidate),
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

    static boolean prefersRoofWeatherproofRouteAnchor(PackRepository.QueryTerms queryTerms) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return false;
        }
        return PackRouteSignalPolicy.prefersRoofWeatherproofContext(queryTerms.metadataProfile);
    }

    static int roofWeatherproofAnchorBias(PackRepository.QueryTerms queryTerms, SearchResult candidate) {
        if (!prefersRoofWeatherproofRouteAnchor(queryTerms) || candidate == null) {
            return 0;
        }
        int score = 0;
        if (PackRouteSignalPolicy.hasRoofWeatherproofAnchorSignal(candidate)) {
            score += 16;
        }
        if (PackRouteSignalPolicy.hasRoofWeatherproofDistractorSignal(candidate)
            && !PackRouteSignalPolicy.hasRoofWeatherproofAnchorSignal(candidate)) {
            score -= 14;
        }
        return score;
    }

    static int currentHeadRouteOrderingPriority(PackRepository.QueryTerms queryTerms, SearchResult result) {
        return currentHeadRouteRefinementBonus(queryTerms, result) * 4
            + PackSupportScoringPolicy.anchorAlignmentBonus(queryTerms, result);
    }

    static int currentHeadRouteRefinementBonus(PackRepository.QueryTerms queryTerms, SearchResult result) {
        if (missingRouteInputs(queryTerms, result)) {
            return 0;
        }
        String structure = queryTerms.metadataProfile.preferredStructureType();
        String titleSection = PackTextMatchPolicy.normalize(titleSectionText(result));
        String candidateStructure = normalized(result.structureType);
        String category = normalized(result.category);
        String role = normalized(result.contentRole);
        return glassmakingCurrentHeadBonus(structure, titleSection, role)
            + roofWeatherproofCurrentHeadBonus(queryTerms, structure, titleSection, candidateStructure)
            + broadHouseCurrentHeadBonus(queryTerms, structure, titleSection, candidateStructure, category);
    }

    private static int glassmakingCurrentHeadBonus(String structure, String titleSection, String role) {
        if (!"glassmaking".equals(structure)) {
            return 0;
        }

        int score = 0;
        if (titleSection.contains("glass optics ceramics")
            && titleSection.contains("glassmaking from scratch")) {
            score += 72;
        } else if (titleSection.contains("glass optics ceramics")) {
            score += 34;
        }
        if (titleSection.contains("glass making raw materials")
            && (titleSection.contains("raw materials")
                || titleSection.contains("batch preparation")
                || titleSection.contains("furnace construction"))) {
            score -= 18;
        }
        if ("reference".equals(role) && titleSection.contains("raw materials")) {
            score -= 8;
        }
        return score;
    }

    private static int roofWeatherproofCurrentHeadBonus(
        PackRepository.QueryTerms queryTerms,
        String structure,
        String titleSection,
        String candidateStructure
    ) {
        if (!"cabin_house".equals(structure)
            || (!queryTerms.metadataProfile.hasExplicitTopic("roofing")
                && !queryTerms.metadataProfile.hasExplicitTopic("weatherproofing"))) {
            return 0;
        }

        boolean weatherproofQuery = queryTerms.queryLower.contains("weatherproof")
            || queryTerms.queryLower.contains("rainproof");
        boolean waterproofQuery = queryTerms.queryLower.contains("waterproof");
        int score = 0;
        if (weatherproofQuery && titleSection.contains("insulation weatherproofing")) {
            score += 72;
        }
        if (waterproofQuery && titleSection.contains("waterproofing and sealants")) {
            score += 48;
        }
        if ("cabin_house".equals(candidateStructure)) {
            score += 18;
        } else if ("small_watercraft".equals(candidateStructure)) {
            score -= weatherproofQuery ? 18 : 4;
        }
        if (titleSection.contains("construction carpentry")) {
            score -= 34;
        }
        return score;
    }

    private static int broadHouseCurrentHeadBonus(
        PackRepository.QueryTerms queryTerms,
        String structure,
        String titleSection,
        String candidateStructure,
        String category
    ) {
        if (!"cabin_house".equals(structure)
            || queryTerms.metadataProfile.hasExplicitTopicFocus()
            || !queryTerms.routeProfile.isStarterBuildProject()) {
            return 0;
        }

        int score = 0;
        if ("cabin_house".equals(candidateStructure)) {
            score += 32;
        }
        if ("building".equals(category)) {
            score += 12;
        }
        if (titleSection.contains("construction carpentry")) {
            score += 74;
        }
        if (titleSection.contains("shelter site selection")) {
            score -= 64;
        }
        if ("emergency_shelter".equals(candidateStructure) || "survival".equals(category)) {
            score -= 34;
        }
        if (titleSection.contains("foundation")
            || titleSection.contains("wall construction")
            || titleSection.contains("roofing systems")) {
            score += 10;
        }
        return score;
    }

    private static boolean missingRouteInputs(PackRepository.QueryTerms queryTerms, SearchResult result) {
        return queryTerms == null || result == null || queryTerms.metadataProfile == null;
    }

    private static String titleSectionText(SearchResult result) {
        return PackRepository.emptySafe(result.title) + " " + PackRepository.emptySafe(result.sectionHeading);
    }

    private static String normalized(String text) {
        return PackRepository.emptySafe(text).trim().toLowerCase(QUERY_LOCALE);
    }

    private static Set<String> buildMarkerSet(String... values) {
        LinkedHashSet<String> markers = new LinkedHashSet<>();
        Collections.addAll(markers, values);
        return Collections.unmodifiableSet(markers);
    }
}

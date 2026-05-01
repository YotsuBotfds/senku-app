package com.senku.mobile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

final class PackRouteSupportPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final int MIN_GENERAL_SUPPORT_METADATA_SCORE = 12;
    private static final Set<String> HOUSE_ACCESSIBILITY_MARKERS = buildMarkerSet(
        "accessible shelter",
        "universal design",
        "elderly-friendly design",
        "one-handed operation design",
        "grab bars",
        "mobility impairment",
        "wheelchair"
    );
    private static final Set<String> HOUSE_CLIMATE_MARKERS = buildMarkerSet(
        "desert",
        "arid",
        "wetland",
        "swamp",
        "jungle",
        "tropical",
        "alpine",
        "mountain",
        "snow shelter",
        "winter"
    );
    private static final Set<String> WATER_DISTRIBUTION_DISTRACTOR_MARKERS = buildMarkerSet(
        "failure analysis",
        "troubleshooting",
        "irrigation",
        "sanitation",
        "waste management",
        "bridges",
        "dams",
        "infrastructure"
    );
    private static final Set<String> WATER_STORAGE_CONTAINER_MAKING_MARKERS = buildMarkerSet(
        "make",
        "build",
        "craft",
        "container",
        "containers",
        "vessel",
        "vessels"
    );

    private PackRouteSupportPolicy() {
    }

    static boolean supportCandidateMatchesRoute(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        boolean diversifyContext,
        SearchResult candidate
    ) {
        if (!diversifyContext || routeProfile == null || !routeProfile.isRouteFocused()) {
            return true;
        }
        boolean supported = routeProfile.supportsRouteResult(
            candidate.title,
            candidate.sectionHeading,
            candidate.category,
            candidate.topicTags,
            candidate.snippet,
            candidate.body
        );
        if (!supported) {
            return false;
        }
        if (metadataProfile == null) {
            return true;
        }
        String category = safe(candidate.category).trim().toLowerCase(QUERY_LOCALE);
        String structureType = safe(candidate.structureType).trim().toLowerCase(QUERY_LOCALE);
        boolean structuredMatch = !structureType.isEmpty() && !"general".equals(structureType);
        String retrievalMode = safe(candidate.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        String candidateText = safe(candidate.title) + " " + safe(candidate.sectionHeading) + " " +
            safe(candidate.snippet) + " " + safe(candidate.body);
        int sectionBonus = metadataProfile.sectionHeadingBonus(candidate.sectionHeading);
        int metadataScore = metadataProfile.metadataBonus(
            candidate.category,
            candidate.contentRole,
            candidate.timeHorizon,
            candidate.structureType,
            candidate.topicTags
        );
        if (metadataProfile.hasExplicitTopic("water_distribution")
            && !matchesExplicitWaterDistributionSupport(category, structureType, candidate, sectionBonus)) {
            return false;
        }
        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && "guide-focus".equals(retrievalMode)
            && safe(candidate.sectionHeading).trim().isEmpty()
            && containsTerm(candidate.topicTags, "water_distribution")
            && !metadataProfile.hasExplicitTopic("water_distribution")
            && !containsTerm(candidate.topicTags, "container_sanitation")
            && !containsTerm(candidate.topicTags, "water_rotation")) {
            return false;
        }
        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && !metadataProfile.hasExplicitTopic("water_distribution")
            && containsTerm(candidate.topicTags, "water_distribution")
            && !containsTerm(candidate.topicTags, "container_sanitation")
            && !containsTerm(candidate.topicTags, "water_rotation")
            && sectionBonus <= 0) {
            return false;
        }
        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && !metadataProfile.hasExplicitTopic("water_distribution")
            && "guide-focus".equals(retrievalMode)
            && safe(candidate.sectionHeading).trim().isEmpty()
            && !"water_storage".equals(structureType)
            && metadataProfile.preferredTopicOverlapCount(candidate.topicTags) < 2
            && metadataProfile.sectionHeadingBonus(candidate.sectionHeading) <= 0) {
            return false;
        }
        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && !metadataProfile.hasExplicitTopic("water_distribution")
            && "guide-focus".equals(retrievalMode)
            && safe(candidate.sectionHeading).trim().isEmpty()
            && !metadataProfile.waterStorageContainerMakingIntent()
            && ("building".equals(category) || "crafts".equals(category))
            && containsAnyMarker(candidateText, WATER_STORAGE_CONTAINER_MAKING_MARKERS)) {
            return false;
        }
        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && metadataProfile.hasExplicitTopic("water_distribution")
            && "guide-focus".equals(retrievalMode)
            && safe(candidate.sectionHeading).trim().isEmpty()
            && !hasWaterDistributionTitleSignal(candidate)) {
            return false;
        }
        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && metadataProfile.hasExplicitTopic("water_distribution")
            && !containsTerm(candidate.topicTags, "water_distribution")
            && metadataProfile.sectionHeadingBonus(candidate.sectionHeading) <= 0) {
            return false;
        }
        if ("water_distribution".equals(metadataProfile.preferredStructureType())) {
            boolean distributionTagged = containsTerm(candidate.topicTags, "water_distribution");
            boolean strongGuideSignal = hasStrongWaterDistributionGuideSignal(candidate);
            if (!RetrievalRoutePolicy.allowsWaterDistributionSupportCandidate(
                distributionTagged,
                sectionBonus,
                strongGuideSignal,
                retrievalMode,
                safe(candidate.sectionHeading).trim().isEmpty(),
                candidate.contentRole,
                containsAnyMarker(candidateText, WATER_DISTRIBUTION_DISTRACTOR_MARKERS)
            )) {
                return false;
            }
        }
        if (prefersRoofWeatherproofContext(metadataProfile)) {
            if ("guide-focus".equals(retrievalMode)
                && safe(candidate.sectionHeading).trim().isEmpty()
                && !hasRoofWeatherproofAnchorSignal(candidate)) {
                return false;
            }
            if (sectionBonus <= 0 && hasRoofWeatherproofDistractorSignal(candidate)) {
                return false;
            }
        }
        if (prefersGovernanceTrustRepairContext(metadataProfile)
            && hasGovernanceSupportMixDistractor(candidate)
            && !hasGovernanceTrustRepairSignal(candidate)) {
            return false;
        }
        if ("community_governance".equals(metadataProfile.preferredStructureType())
            && hasGovernanceFoodOpsDistractorSignal(candidate)
            && !hasGovernanceCommonsResourceSignal(candidate)) {
            return false;
        }
        if (RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(metadataProfile.preferredStructureType())
            && "guide-focus".equals(retrievalMode)
            && safe(candidate.sectionHeading).trim().isEmpty()
            && !metadataProfile.preferredStructureType().equals(structureType)) {
            return false;
        }
        if (metadataScore > 0) {
            if ("cabin_house".equals(metadataProfile.preferredStructureType())) {
                if (!metadataProfile.accessibilityIntent() && containsAnyMarker(candidateText, HOUSE_ACCESSIBILITY_MARKERS)) {
                    return false;
                }
                if (!metadataProfile.climateContextIntent()
                    && containsAnyMarker(candidateText, HOUSE_CLIMATE_MARKERS)) {
                    return false;
                }
                if (!metadataProfile.hasExplicitTopicFocus() && sectionBonus < 0) {
                    return false;
                }
            }
            if (structuredMatch) {
                return true;
            }
            if (!routeProfile.preferredCategories().contains(category)) {
                return false;
            }
            if (metadataScore < MIN_GENERAL_SUPPORT_METADATA_SCORE) {
                return false;
            }
            if ("cabin_house".equals(metadataProfile.preferredStructureType())
                && metadataProfile.preferredTopicOverlapCount(candidate.topicTags) < 2
                && sectionBonus <= 0) {
                return false;
            }
            if ("cabin_house".equals(metadataProfile.preferredStructureType())
                && metadataProfile.hasExplicitTopicFocus()
                && sectionBonus <= 0
                && !metadataProfile.hasExplicitTopicOverlap(candidate.topicTags)) {
                return false;
            }
            return true;
        }
        boolean preferredCategory = routeProfile.preferredCategories().contains(category);
        return preferredCategory && !structuredMatch && metadataScore >= MIN_GENERAL_SUPPORT_METADATA_SCORE;
    }

    private static boolean matchesExplicitWaterDistributionSupport(
        String category,
        String structureType,
        SearchResult candidate,
        int sectionBonus
    ) {
        String normalizedText = normalizeMatchText(safe(candidate.title) + " " + safe(candidate.sectionHeading));
        boolean structureMatch = "water_distribution".equals(structureType);
        boolean topicMatch = containsTerm(candidate.topicTags, "water_distribution");
        boolean sectionMatch = sectionBonus > 0;
        boolean signalMatch = hasWaterDistributionTitleSignal(candidate) || hasWaterDistributionDetailSignal(candidate);
        boolean strongGuideSignal = hasStrongWaterDistributionGuideSignal(candidate);
        boolean lifecycleMetaSection = sectionBonus <= 0
            && (normalizedText.contains("lifecycle")
                || normalizedText.contains("see also")
                || normalizedText.contains("checklist")
                || normalizedText.contains("preventive maintenance")
                || normalizedText.contains("system care"));
        if (!structureMatch && !topicMatch && !sectionMatch && !signalMatch) {
            return false;
        }
        if (lifecycleMetaSection) {
            return false;
        }
        if (sectionBonus < 0 && !strongGuideSignal) {
            return false;
        }
        if ("resource-management".equals(category)) {
            return structureMatch || sectionMatch || strongGuideSignal;
        }
        return "building".equals(category)
            || "utility".equals(category)
            || structureMatch
            || (topicMatch && strongGuideSignal);
    }

    private static boolean prefersRoofWeatherproofContext(QueryMetadataProfile metadataProfile) {
        if (metadataProfile == null) {
            return false;
        }
        return PackRouteSignalPolicy.prefersRoofWeatherproofContext(metadataProfile);
    }

    private static boolean hasRoofWeatherproofAnchorSignal(SearchResult candidate) {
        return PackRouteSignalPolicy.hasRoofWeatherproofAnchorSignal(candidate);
    }

    private static boolean hasRoofWeatherproofDistractorSignal(SearchResult candidate) {
        return PackRouteSignalPolicy.hasRoofWeatherproofDistractorSignal(candidate);
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

    private static boolean hasGovernanceCommonsResourceSignal(SearchResult candidate) {
        return PackRouteSignalPolicy.hasGovernanceCommonsResourceSignal(candidate);
    }

    private static boolean hasGovernanceFoodOpsDistractorSignal(SearchResult candidate) {
        return PackRouteSignalPolicy.hasGovernanceFoodOpsDistractorSignal(candidate);
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
        return safe(text)
            .toLowerCase(QUERY_LOCALE)
            .replaceAll("[^a-z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static Set<String> buildMarkerSet(String... values) {
        LinkedHashSet<String> markers = new LinkedHashSet<>();
        Collections.addAll(markers, values);
        return Collections.unmodifiableSet(markers);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

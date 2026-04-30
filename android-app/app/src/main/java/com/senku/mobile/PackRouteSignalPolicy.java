package com.senku.mobile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

final class PackRouteSignalPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final Set<String> HOUSE_ROOF_WEATHERPROOF_ANCHOR_MARKERS = buildMarkerSet(
        "roofing",
        "roof waterproofing",
        "roof waterproofing and sealants",
        "rainproofing and water shedding",
        "waterproofing and sealants",
        "roof framing",
        "roofing materials",
        "flashing",
        "ridge cap",
        "drip edge",
        "underlayment"
    );
    private static final Set<String> HOUSE_ROOF_WEATHERPROOF_DISTRACTOR_MARKERS = buildMarkerSet(
        "structural engineering basics",
        "structural overview",
        "general engineering",
        "concrete mixing ratio",
        "calculator",
        "calculation",
        "design loads",
        "load paths",
        "seismic",
        "footing sizing"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_TRUST_REPAIR_SECTION_MARKERS = buildMarkerSet(
        "trust",
        "reputation",
        "vouch",
        "mediation",
        "restorative",
        "restitution"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_MONITORING_DISTRACTOR_MARKERS = buildMarkerSet(
        "monitoring",
        "membership",
        "boundaries",
        "graduated sanctions",
        "sanctions",
        "quota",
        "allocation",
        "allocations"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_FINANCE_DISTRACTOR_MARKERS = buildMarkerSet(
        "insurance",
        "risk pooling",
        "reinsurance",
        "risk transfer",
        "accounting",
        "fund governance",
        "actuarial",
        "record-keeping",
        "mutual aid funds"
    );
    private static final Set<String> WATER_DISTRIBUTION_ANCHOR_MARKERS = buildMarkerSet(
        "water distribution",
        "distribution system",
        "gravity-fed",
        "gravity fed",
        "water system",
        "storage tank",
        "storage tanks",
        "cistern",
        "rainwater harvesting",
        "rainwater cistern",
        "plumbing",
        "piping",
        "water tower",
        "spring box",
        "household taps",
        "community water"
    );
    private static final Set<String> WATER_DISTRIBUTION_GUIDE_MARKERS = buildMarkerSet(
        "water distribution",
        "distribution system",
        "storage tank",
        "storage tanks",
        "cistern",
        "rainwater cistern",
        "water tower",
        "spring box",
        "household taps",
        "community water"
    );
    private static final Set<String> SOAP_PROCESS_MARKERS = buildMarkerSet(
        "making soap",
        "soap making",
        "soap making - cold process",
        "cold process soap",
        "hot process soap",
        "making lye from wood ash",
        "wood ash lye",
        "lye water",
        "saponification",
        "render fat",
        "rendering fats",
        "tallow",
        "lard",
        "trace",
        "cure"
    );
    private static final Set<String> SOAP_GENERIC_CHEMISTRY_MARKERS = buildMarkerSet(
        "acids and bases",
        "chemical safety fundamentals",
        "cleaning product chemistry",
        "storage compatibility",
        "industrial applications",
        "atoms and molecules",
        "chemical reactions"
    );

    private PackRouteSignalPolicy() {
    }

    static boolean prefersRoofWeatherproofContext(QueryMetadataProfile metadataProfile) {
        if (metadataProfile == null) {
            return false;
        }
        return "cabin_house".equals(metadataProfile.preferredStructureType())
            && (metadataProfile.hasExplicitTopic("roofing") || metadataProfile.hasExplicitTopic("weatherproofing"));
    }

    static boolean hasRoofWeatherproofAnchorSignal(SearchResult candidate) {
        if (candidate == null) {
            return false;
        }
        return containsAnyMarker(titleSectionText(candidate), HOUSE_ROOF_WEATHERPROOF_ANCHOR_MARKERS);
    }

    static boolean hasRoofWeatherproofDistractorSignal(SearchResult candidate) {
        if (candidate == null) {
            return false;
        }
        return containsAnyMarker(titleSectionText(candidate), HOUSE_ROOF_WEATHERPROOF_DISTRACTOR_MARKERS);
    }

    static boolean prefersGovernanceTrustRepairContext(QueryMetadataProfile metadataProfile) {
        return metadataProfile != null
            && "community_governance".equals(metadataProfile.preferredStructureType())
            && metadataProfile.trustRepairMergeIntent();
    }

    static boolean hasGovernanceTrustRepairSignal(SearchResult candidate) {
        if (candidate == null) {
            return false;
        }
        return hasGovernanceTrustRepairTextSignal(titleSectionText(candidate));
    }

    static boolean hasGovernanceTrustRepairTextSignal(String text) {
        return containsAnyMarker(text, COMMUNITY_GOVERNANCE_TRUST_REPAIR_SECTION_MARKERS);
    }

    static boolean hasGovernanceMonitoringDistractorSignal(String text) {
        return containsAnyMarker(text, COMMUNITY_GOVERNANCE_MONITORING_DISTRACTOR_MARKERS);
    }

    static boolean hasGovernanceFinanceDistractorSignal(String text) {
        return containsAnyMarker(text, COMMUNITY_GOVERNANCE_FINANCE_DISTRACTOR_MARKERS);
    }

    static boolean hasGovernanceSupportMixDistractor(SearchResult candidate) {
        if (candidate == null) {
            return false;
        }
        String text = titleSectionText(candidate);
        return hasGovernanceMonitoringDistractorSignal(text) || hasGovernanceFinanceDistractorSignal(text);
    }

    static boolean hasWaterDistributionTitleSignal(SearchResult result) {
        if (result == null) {
            return false;
        }
        return containsAnyMarker(titleSectionText(result), WATER_DISTRIBUTION_ANCHOR_MARKERS);
    }

    static boolean hasStrongWaterDistributionGuideSignal(SearchResult result) {
        if (result == null) {
            return false;
        }
        String text = titleSectionText(result);
        if (!containsAnyMarker(text, WATER_DISTRIBUTION_GUIDE_MARKERS)) {
            return false;
        }
        String normalized = normalizeMatchText(text);
        return normalized.contains("distribution")
            || normalized.contains("storage tank")
            || normalized.contains("cistern")
            || normalized.contains("community water")
            || normalized.contains("spring box")
            || normalized.contains("household taps")
            || normalized.contains("water tower");
    }

    static boolean hasWaterDistributionDetailSignal(SearchResult result) {
        if (result == null) {
            return false;
        }
        return containsAnyMarker(
            PackRepository.emptySafe(result.snippet) + " " + PackRepository.emptySafe(result.body),
            WATER_DISTRIBUTION_ANCHOR_MARKERS
        );
    }

    static boolean hasStrongSoapmakingGuideSignal(SearchResult result) {
        if (result == null) {
            return false;
        }
        String title = PackRepository.emptySafe(result.title);
        String section = PackRepository.emptySafe(result.sectionHeading);
        String combined = title + " " + section + " " + PackRepository.emptySafe(result.snippet) + " "
            + PackRepository.emptySafe(result.body);
        if (!hasSoapmakingProcessSignal(combined)) {
            return false;
        }
        if (!hasSoapmakingProcessSignal(title) && !hasSoapmakingProcessSignal(section)) {
            return false;
        }
        return !hasSoapmakingGenericChemistrySignal(title) && !hasSoapmakingGenericChemistrySignal(section);
    }

    static boolean hasSoapmakingProcessSignal(String text) {
        return containsAnyMarker(text, SOAP_PROCESS_MARKERS);
    }

    static boolean hasSoapmakingGenericChemistrySignal(String text) {
        return containsAnyMarker(text, SOAP_GENERIC_CHEMISTRY_MARKERS);
    }

    static boolean containsAnyMarker(String text, Set<String> markers) {
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

    static String normalizeMatchText(String text) {
        return PackRepository.emptySafe(text)
            .toLowerCase(QUERY_LOCALE)
            .replaceAll("[^a-z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String titleSectionText(SearchResult result) {
        return PackRepository.emptySafe(result.title) + " " + PackRepository.emptySafe(result.sectionHeading);
    }

    private static Set<String> buildMarkerSet(String... values) {
        LinkedHashSet<String> markers = new LinkedHashSet<>();
        Collections.addAll(markers, values);
        return Collections.unmodifiableSet(markers);
    }
}

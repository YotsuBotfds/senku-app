package com.senku.mobile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class PackStructuredAnchorPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final Set<String> WATER_STORAGE_CONTAINER_MAKING_MARKERS = buildMarkerSet(
        "make",
        "build",
        "craft",
        "container",
        "containers",
        "vessel",
        "vessels"
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

    private PackStructuredAnchorPolicy() {
    }

    static SearchResult selectExplicitWaterStorageAnchor(
        PackRepository.QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        if (queryTerms == null
            || rankedResults == null
            || rankedResults.isEmpty()
            || queryTerms.metadataProfile == null) {
            return null;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")
            || !metadataProfile.hasExplicitTopicFocus()) {
            return null;
        }

        boolean containerMakingIntent = hasWaterStorageContainerMakingIntent(queryTerms.queryLower);
        boolean hasNonInventoryCandidate = false;
        for (SearchResult candidate : rankedResults) {
            String normalizedTitle = normalized(candidate.title);
            boolean structureMatch = "water_storage".equals(normalized(candidate.structureType));
            boolean topicMatch = metadataProfile.hasExplicitTopicOverlap(candidate.topicTags);
            if ((structureMatch || topicMatch) && !normalizedTitle.contains("inventory")) {
                hasNonInventoryCandidate = true;
                break;
            }
        }
        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            boolean structureMatch = "water_storage".equals(normalized(candidate.structureType));
            boolean topicMatch = metadataProfile.hasExplicitTopicOverlap(candidate.topicTags);
            if (!structureMatch && !topicMatch) {
                continue;
            }

            int sectionBonus = metadataProfile.sectionHeadingBonus(candidate.sectionHeading);
            if (sectionBonus < 0) {
                continue;
            }

            int score = Math.max(1, PackSupportScoringPolicy.supportBreakdown(queryTerms, candidate).supportWithMetadata())
                + Math.max(0, 12 - index);
            int overlap = metadataProfile.preferredTopicOverlapCount(candidate.topicTags);
            String normalizedRole = normalized(candidate.contentRole);
            String normalizedMode = normalized(candidate.retrievalMode);
            String normalizedCategory = normalized(candidate.category);
            String normalizedTitle = normalized(candidate.title);
            if (hasNonInventoryCandidate && normalizedTitle.contains("inventory")) {
                continue;
            }
            if (normalizedTitle.contains("inventory") && overlap < 2) {
                continue;
            }

            if ("planning".equals(normalizedRole) || "subsystem".equals(normalizedRole)) {
                score += 14;
            } else if ("safety".equals(normalizedRole)) {
                score += 8;
            } else if ("starter".equals(normalizedRole)) {
                boolean genericInventoryGuide = normalizedTitle.contains("inventory");
                if ("guide-focus".equals(normalizedMode) && structureMatch && !genericInventoryGuide) {
                    score -= 2;
                } else {
                    score -= 16;
                }
            } else if ("reference".equals(normalizedRole)) {
                score -= 10;
            }

            if ("guide-focus".equals(normalizedMode)) {
                score += 12;
            } else if ("route-focus".equals(normalizedMode)) {
                score += 4;
            }

            if (overlap >= 2) {
                score += 10;
            } else if (overlap == 1) {
                score += 4;
            }
            if (sectionBonus > 0) {
                score += 6;
            }
            if ("resource-management".equals(normalizedCategory)) {
                score += containerMakingIntent ? 8 : 18;
            } else if ("survival".equals(normalizedCategory)) {
                score += 8;
            } else if (!containerMakingIntent && "building".equals(normalizedCategory)) {
                score -= 10;
            }
            if (!containerMakingIntent && "crafts".equals(normalizedCategory)) {
                score -= 8;
            }
            if (!containerMakingIntent
                && (normalizedTitle.contains("making")
                    || normalizedTitle.contains("container")
                    || normalizedTitle.contains("vessel"))) {
                score -= 30;
            }
            if (normalizedTitle.contains("inventory") && overlap < 2) {
                score -= 18;
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    static SearchResult selectSpecializedStructuredAnchor(
        PackRepository.QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        if (queryTerms == null
            || rankedResults == null
            || rankedResults.isEmpty()
            || queryTerms.metadataProfile == null) {
            return null;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        String preferredStructureType = metadataProfile.preferredStructureType();
        if (!metadataProfile.hasExplicitTopicFocus()
            || !RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(preferredStructureType)) {
            return null;
        }

        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            boolean structureMatch = preferredStructureType.equals(normalized(candidate.structureType));
            boolean topicMatch = metadataProfile.hasExplicitTopicOverlap(candidate.topicTags);
            if (!structureMatch && !topicMatch) {
                continue;
            }

            int score = Math.max(1, PackSupportScoringPolicy.supportBreakdown(queryTerms, candidate).supportWithMetadata());
            score += Math.max(0, 12 - index);
            score += PackSupportScoringPolicy.anchorAlignmentBonus(queryTerms, candidate);
            String retrievalMode = normalized(candidate.retrievalMode);
            if ("route-focus".equals(retrievalMode)) {
                score += 10;
            } else if ("guide-focus".equals(retrievalMode)) {
                score += PackRepository.emptySafe(candidate.sectionHeading).trim().isEmpty() ? 4 : 8;
            } else if ("hybrid".equals(retrievalMode)) {
                score += 4;
            }
            score += specializedStructuredAnchorBias(queryTerms, candidate);

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    static SearchResult selectBroadHouseAnchor(
        PackRepository.QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        if (queryTerms == null
            || rankedResults == null
            || rankedResults.isEmpty()
            || queryTerms.metadataProfile == null) {
            return null;
        }

        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"cabin_house".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopicFocus()) {
            return null;
        }

        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            int sectionBonus = metadataProfile.sectionHeadingBonus(candidate.sectionHeading);
            if (sectionBonus < 0) {
                continue;
            }

            String normalizedStructure = normalized(candidate.structureType);
            String normalizedCategory = normalized(candidate.category);
            boolean structureMatch = "cabin_house".equals(normalizedStructure);
            int overlap = metadataProfile.preferredTopicOverlapCount(candidate.topicTags);
            if (!structureMatch && (!"building".equals(normalizedCategory) || overlap < 2)) {
                continue;
            }

            int score = Math.max(1, PackSupportScoringPolicy.supportBreakdown(queryTerms, candidate).supportWithMetadata())
                + Math.max(0, 12 - index);
            score += broadHouseAnchorFocusBonus(candidate, sectionBonus, structureMatch);
            String retrievalMode = normalized(candidate.retrievalMode);
            if ("route-focus".equals(retrievalMode)) {
                score += 8;
            } else if ("guide-focus".equals(retrievalMode)) {
                score += PackRepository.emptySafe(candidate.sectionHeading).trim().isEmpty() ? -10 : 6;
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    static boolean hasWaterStorageContainerMakingIntent(String queryLower) {
        return containsAnyMarker(queryLower, WATER_STORAGE_CONTAINER_MAKING_MARKERS);
    }

    private static int specializedStructuredAnchorBias(PackRepository.QueryTerms queryTerms, SearchResult candidate) {
        if (queryTerms == null || candidate == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        String preferredStructureType = queryTerms.metadataProfile.preferredStructureType();
        if ("soapmaking".equals(preferredStructureType)) {
            return soapmakingStructuredAnchorBias(queryTerms, candidate);
        }
        if ("community_governance".equals(preferredStructureType)) {
            return communityGovernanceStructuredAnchorBias(queryTerms, candidate);
        }
        return 0;
    }

    private static int soapmakingStructuredAnchorBias(PackRepository.QueryTerms queryTerms, SearchResult candidate) {
        String normalizedTitle = normalized(candidate.title);
        String normalizedSection = normalized(candidate.sectionHeading);
        String normalizedMode = normalized(candidate.retrievalMode);
        String normalizedRole = normalized(candidate.contentRole);
        String normalizedStructure = normalized(candidate.structureType);
        String combined = normalizeMatchText(
            PackRepository.emptySafe(candidate.title) + " "
                + PackRepository.emptySafe(candidate.sectionHeading) + " "
                + PackRepository.emptySafe(candidate.snippet) + " "
                + PackRepository.emptySafe(candidate.body)
        );

        int score = 0;
        if (hasStrongSoapmakingGuideSignal(candidate)) {
            score += 18;
        }
        if ("subsystem".equals(normalizedRole)) {
            score += 6;
        } else if ("safety".equals(normalizedRole)) {
            score -= 4;
        }
        if ("route-focus".equals(normalizedMode) && queryTerms.metadataProfile.sectionHeadingBonus(candidate.sectionHeading) > 0) {
            score += 10;
        }
        if (!"soapmaking".equals(normalizedStructure)) {
            score -= 8;
        }
        if (containsAnyMarker(normalizedTitle, SOAP_GENERIC_CHEMISTRY_MARKERS)
            || containsAnyMarker(normalizedSection, SOAP_GENERIC_CHEMISTRY_MARKERS)) {
            score -= 22;
        }
        if (containsAnyMarker(combined, SOAP_PROCESS_MARKERS)) {
            score += 6;
        }
        return score;
    }

    private static int broadHouseAnchorFocusBonus(SearchResult candidate, int sectionBonus, boolean structureMatch) {
        String normalized = normalizeMatchText(
            PackRepository.emptySafe(candidate.title) + " " + PackRepository.emptySafe(candidate.sectionHeading)
        );
        String contentRole = normalized(candidate.contentRole);
        String normalizedStructure = normalized(candidate.structureType);
        int bonus = structureMatch ? 24 : 0;

        if ("starter".equals(contentRole) || "planning".equals(contentRole)) {
            bonus += 12;
        } else if ("reference".equals(contentRole)) {
            bonus -= 12;
        }

        if ("general".equals(normalizedStructure)) {
            bonus -= 10;
        }
        if (normalized.contains("construction carpentry")) {
            bonus += 12;
        }
        if (normalized.contains("foundation")) {
            bonus += 8;
        }
        if (normalized.contains("wall construction") || normalized.contains("wall framing")) {
            bonus += 8;
        }
        if (normalized.contains("roofing") || normalized.contains("weatherproofing")) {
            bonus += 6;
        }
        if (normalized.contains("site selection") || normalized.contains("drainage")) {
            bonus += 6;
        }
        if (normalized.contains("frost line")
            || normalized.contains("foundation repair")
            || normalized.contains("footing sizing")) {
            bonus -= 8;
        }
        if (sectionBonus == 0) {
            bonus -= 4;
        }
        return bonus;
    }

    private static int communityGovernanceStructuredAnchorBias(
        PackRepository.QueryTerms queryTerms,
        SearchResult candidate
    ) {
        String queryLower = PackRepository.emptySafe(queryTerms.queryLower);
        String normalizedTitle = normalized(candidate.title);
        String normalizedSection = normalized(candidate.sectionHeading);
        String normalizedMode = normalized(candidate.retrievalMode);
        boolean financialIntent = containsAnyMarker(
            queryLower,
            buildMarkerSet(
                "insurance", "insured", "fund", "funds", "premium", "premiums", "pool", "pooling",
                "risk transfer", "reinsurance", "claim", "claims", "contribution", "contributions",
                "accounting", "ledger", "record", "records", "compensation"
            )
        );
        boolean trustRepairIntent = queryTerms.metadataProfile != null
            && queryTerms.metadataProfile.trustRepairMergeIntent();

        int score = 0;
        if ("route-focus".equals(normalizedMode) && queryTerms.metadataProfile.sectionHeadingBonus(candidate.sectionHeading) > 0) {
            score += 12;
        } else if ("guide-focus".equals(normalizedMode) && !normalizedSection.isEmpty()) {
            score -= 6;
        }

        boolean financeDistractor = containsAnyMarker(normalizedTitle, COMMUNITY_GOVERNANCE_FINANCE_DISTRACTOR_MARKERS)
            || containsAnyMarker(normalizedSection, COMMUNITY_GOVERNANCE_FINANCE_DISTRACTOR_MARKERS)
            || normalizedSection.contains("historical mutual aid");
        boolean monitoringDistractor = containsAnyMarker(normalizedSection, COMMUNITY_GOVERNANCE_MONITORING_DISTRACTOR_MARKERS);
        boolean trustRepairSignal = containsAnyMarker(normalizedSection, COMMUNITY_GOVERNANCE_TRUST_REPAIR_SECTION_MARKERS);

        if (!financialIntent && financeDistractor) {
            score -= 28;
        }

        if (trustRepairIntent && trustRepairSignal) {
            score += 12;
        }
        if (trustRepairIntent && monitoringDistractor) {
            score -= trustRepairSignal ? 6 : 12;
        }

        if (financialIntent && financeDistractor) {
            score += 18;
        }

        return score;
    }

    private static boolean hasStrongSoapmakingGuideSignal(SearchResult result) {
        if (result == null) {
            return false;
        }
        String normalizedTitle = normalized(result.title);
        String normalizedSection = normalized(result.sectionHeading);
        String combined = normalizeMatchText(
            PackRepository.emptySafe(result.title) + " "
                + PackRepository.emptySafe(result.sectionHeading) + " "
                + PackRepository.emptySafe(result.snippet) + " "
                + PackRepository.emptySafe(result.body)
        );
        boolean processSignal = containsAnyMarker(combined, SOAP_PROCESS_MARKERS);
        if (!processSignal) {
            return false;
        }
        boolean practicalHeading = containsAnyMarker(normalizedTitle, SOAP_PROCESS_MARKERS)
            || containsAnyMarker(normalizedSection, SOAP_PROCESS_MARKERS);
        if (!practicalHeading) {
            return false;
        }
        return !containsAnyMarker(normalizedTitle, SOAP_GENERIC_CHEMISTRY_MARKERS)
            && !containsAnyMarker(normalizedSection, SOAP_GENERIC_CHEMISTRY_MARKERS);
    }

    private static Set<String> buildMarkerSet(String... values) {
        LinkedHashSet<String> markers = new LinkedHashSet<>();
        Collections.addAll(markers, values);
        return Collections.unmodifiableSet(markers);
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

    private static String normalizeMatchText(String text) {
        return PackRepository.emptySafe(text)
            .toLowerCase(QUERY_LOCALE)
            .replaceAll("[^a-z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String normalized(String text) {
        return PackRepository.emptySafe(text).trim().toLowerCase(QUERY_LOCALE);
    }
}

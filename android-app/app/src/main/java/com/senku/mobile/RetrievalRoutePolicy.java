package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class RetrievalRoutePolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private RetrievalRoutePolicy() {
    }

    static int routeChunkCandidateLimit(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        int limit,
        boolean compactGuideSweep
    ) {
        if (routeProfile != null && routeProfile.isStarterBuildProject()) {
            if (compactGuideSweep
                && metadataProfile != null
                && "cabin_house".equals(metadataProfile.preferredStructureType())
                && metadataProfile.hasExplicitTopic("site_selection")) {
                return Math.max(limit * 5, 128);
            }
            return Math.max(limit * 24, 600);
        }
        return compactGuideSweep ? Math.max(limit * 8, 160) : Math.max(limit * 12, 240);
    }

    static int routeChunkCandidateTarget(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        int limit,
        boolean compactGuideSweep
    ) {
        if (routeProfile != null && routeProfile.isStarterBuildProject()) {
            if (compactGuideSweep
                && metadataProfile != null
                && "cabin_house".equals(metadataProfile.preferredStructureType())
                && metadataProfile.hasExplicitTopic("site_selection")) {
                return Math.max(limit + 4, 16);
            }
            return Math.max(limit * 3, 42);
        }
        String preferredStructureType = metadataProfile == null ? "" : metadataProfile.preferredStructureType();
        if ("soapmaking".equals(preferredStructureType) || "glassmaking".equals(preferredStructureType)) {
            return compactGuideSweep ? Math.max(limit + 2, 14) : Math.max(limit + 6, 18);
        }
        return compactGuideSweep ? Math.max(limit + 4, 16) : Math.max(limit * 3, 24);
    }

    static boolean shouldBackfillLikeAfterFts(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        int primaryKeywordTokenCount,
        int addedWithFts,
        int totalSections,
        int targetTotal
    ) {
        if (addedWithFts <= 0) {
            return true;
        }
        if (metadataProfile == null) {
            return false;
        }
        String preferredStructureType = metadataProfile.preferredStructureType();
        if (!requiresSpecializedRouteAnchorSignal(preferredStructureType)
            || !shouldRequireDirectAnchorSignal(routeProfile, metadataProfile, primaryKeywordTokenCount)) {
            return false;
        }
        int minimumHealthyPool = "soapmaking".equals(preferredStructureType) ? 6 : 4;
        return totalSections < Math.min(targetTotal, minimumHealthyPool);
    }

    static RouteFtsOrderSpec noBm25RouteFtsOrder(String queryLower, QueryMetadataProfile metadataProfile) {
        if (metadataProfile != null && "soapmaking".equals(metadataProfile.preferredStructureType())) {
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%soap making - cold process%",
                "%making soap%",
                "%soap making - hot process%",
                "%homestead chemistry%",
                "%everyday compounds%",
                "%making lye from wood ash%",
                "%lye from wood ash%",
                "subsystem",
                "%rendering fats%",
                "%tallow%",
                "%cleaning product chemistry%",
                "%chemical safety%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 1 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.content_role) = ? THEN 3 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 4 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 4 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 8 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 8 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "soapmaking_priority"
            );
        }
        if (metadataProfile != null && "water_distribution".equals(metadataProfile.preferredStructureType())) {
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%gravity-fed%",
                "%distribution%",
                "%household taps%",
                "%spring box%",
                "%storage tank%",
                "%system components%",
                "%layout%",
                "%network%",
                "%overflow%",
                "%water tower%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 4 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 5 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "water_distribution_priority"
            );
        }
        if (metadataProfile != null && "clay_oven".equals(metadataProfile.preferredStructureType())) {
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%cob oven%",
                "%clay oven%",
                "%bread oven%",
                "%earth oven%",
                "%masonry oven%",
                "%hearth%",
                "%foundation%",
                "%curing%",
                "%thermal mass%",
                "%chimney%",
                "%draft%",
                "%clay bread oven%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 4 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 4 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 5 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "clay_oven_priority"
            );
        }
        if (metadataProfile != null && "community_governance".equals(metadataProfile.preferredStructureType())) {
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%graduated sanctions%",
                "%mediation%",
                "%membership%",
                "%boundaries%",
                "%conflict%",
                "%restitution%",
                "%commons management%",
                "%resource governance%",
                "%restorative%",
                "%reputation%",
                "%insurance%",
                "%mutual aid%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 3 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 4 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 4 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 5 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 8 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 8 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "community_governance_priority"
            );
        }
        if (metadataProfile != null
            && "cabin_house".equals(metadataProfile.preferredStructureType())
            && metadataProfile.hasExplicitTopic("site_selection")) {
            String safeQueryLower = safe(queryLower);
            boolean seasonalExposureFocus = safeQueryLower.contains("winter")
                || safeQueryLower.contains("summer")
                || safeQueryLower.contains("shade")
                || safeQueryLower.contains("microclimate")
                || safeQueryLower.contains("orientation");
            if (seasonalExposureFocus) {
                ArrayList<String> args = new ArrayList<>();
                Collections.addAll(
                    args,
                    "%wind exposure%",
                    "%microclimate%",
                    "%seasonal%",
                    "%sun exposure%",
                    "%site assessment%",
                    "%terrain analysis%",
                    "%site selection%",
                    "%natural hazards%",
                    "%foundation%",
                    "%drainage%"
                );
                return new RouteFtsOrderSpec(
                    " ORDER BY CASE " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                        "WHEN lower(c.guide_title) LIKE ? THEN 1 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                        "WHEN lower(c.section_heading) LIKE ? THEN 4 " +
                        "ELSE 9 END, c.row_id ",
                    args,
                    "site_selection_microclimate_priority"
                );
            }
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%site assessment%",
                "%terrain analysis%",
                "%wind exposure%",
                "%water proximity%",
                "%site assessment checklist%",
                "%site selection%",
                "%natural hazards%",
                "%foundation%",
                "%drainage%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "site_selection_priority"
            );
        }
        if (metadataProfile != null
            && "cabin_house".equals(metadataProfile.preferredStructureType())
            && (metadataProfile.hasExplicitTopic("roofing")
                || metadataProfile.hasExplicitTopic("weatherproofing"))) {
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%roofing%",
                "%roof waterproofing%",
                "%rainproofing and water shedding%",
                "%waterproofing and sealants%",
                "%weatherproofing%",
                "%roof framing%",
                "%roofing materials%",
                "%construction & carpentry%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 3 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 4 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "roofing_priority"
            );
        }
        if (metadataProfile != null
            && "cabin_house".equals(metadataProfile.preferredStructureType())
            && metadataProfile.hasExplicitTopic("wall_construction")) {
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%wall framing%",
                "%wall construction%",
                "%walls%",
                "%timber frame%",
                "%window and door assembly%",
                "%construction & carpentry%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 2 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 3 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "wall_construction_priority"
            );
        }
        if (metadataProfile != null
            && "cabin_house".equals(metadataProfile.preferredStructureType())
            && metadataProfile.hasExplicitTopic("foundation")) {
            ArrayList<String> args = new ArrayList<>();
            Collections.addAll(
                args,
                "%foundations%",
                "%footings%",
                "%frost line%",
                "%drainage%",
                "%site drainage%",
                "%construction & carpentry%"
            );
            return new RouteFtsOrderSpec(
                " ORDER BY CASE " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 0 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 1 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 2 " +
                    "WHEN lower(c.section_heading) LIKE ? THEN 3 " +
                    "WHEN lower(c.guide_title) LIKE ? THEN 4 " +
                    "ELSE 9 END, c.row_id ",
                args,
                "foundation_priority"
            );
        }
        return new RouteFtsOrderSpec(" ORDER BY c.row_id ", Collections.emptyList(), "rowid");
    }

    static boolean shouldRequireDirectAnchorSignal(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        int primaryKeywordTokenCount
    ) {
        if (metadataProfile == null) {
            return false;
        }
        boolean routeFocused = routeProfile != null && routeProfile.isRouteFocused();
        return metadataProfile.hasExplicitTopic("water_distribution")
            || (routeFocused && requiresSpecializedRouteAnchorSignal(metadataProfile.preferredStructureType()))
            || (!routeFocused && (metadataProfile.hasExplicitTopicFocus() || primaryKeywordTokenCount >= 2));
    }

    static boolean requiresSpecializedRouteAnchorSignal(String preferredStructureType) {
        return "water_distribution".equals(preferredStructureType)
            || "message_auth".equals(preferredStructureType)
            || "community_security".equals(preferredStructureType)
            || "community_governance".equals(preferredStructureType)
            || "soapmaking".equals(preferredStructureType)
            || "glassmaking".equals(preferredStructureType)
            || "fair_trial".equals(preferredStructureType);
    }

    static int routeGuideSearchThreshold(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        boolean compactGuideSweep,
        int limit
    ) {
        if (routeProfile == null || metadataProfile == null) {
            return compactGuideSweep ? Math.max(limit + 4, 16) : Math.max(limit * 2, 18);
        }
        if (routeProfile.isStarterBuildProject()) {
            if (compactGuideSweep
                && "cabin_house".equals(metadataProfile.preferredStructureType())
                && metadataProfile.hasExplicitTopic("site_selection")) {
                return Math.min(Math.max(limit, 10), 12);
            }
            return Math.max(limit * 2, 24);
        }
        if ("soapmaking".equals(metadataProfile.preferredStructureType())
            || "glassmaking".equals(metadataProfile.preferredStructureType())) {
            return compactGuideSweep ? Math.max(limit, 18) : Math.max(limit, 24);
        }
        if ("community_security".equals(metadataProfile.preferredStructureType())
            || "community_governance".equals(metadataProfile.preferredStructureType())) {
            return compactGuideSweep
                ? Math.min(Math.max(limit / 2, 8), 12)
                : Math.min(Math.max(limit / 2, 10), 15);
        }
        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && !metadataProfile.hasExplicitTopic("water_distribution")) {
            return compactGuideSweep ? Math.min(Math.max(limit, 4), 6) : Math.min(Math.max(limit, 7), 9);
        }
        return compactGuideSweep ? Math.max(limit + 4, 16) : Math.max(limit * 2, 18);
    }

    static int runtimeRouteGuideSearchThreshold(
        QueryMetadataProfile metadataProfile,
        boolean ftsSupportsBm25,
        int threshold
    ) {
        if (!ftsSupportsBm25
            && metadataProfile != null
            && "water_distribution".equals(metadataProfile.preferredStructureType())) {
            return Math.min(threshold, 5);
        }
        return threshold;
    }

    static int lexicalCandidateLimit(
        QueryRouteProfile routeProfile,
        QueryMetadataProfile metadataProfile,
        int limit,
        int routeResultCount,
        int defaultCandidateLimit
    ) {
        int base = Math.max(limit, defaultCandidateLimit);
        if (routeProfile == null || metadataProfile == null || !routeProfile.isRouteFocused()) {
            return base;
        }

        int strongRouteThreshold = Math.max(Math.max(limit / 2, 1), 6);
        if (routeResultCount < strongRouteThreshold) {
            return base;
        }

        if ("water_storage".equals(metadataProfile.preferredStructureType())
            && !metadataProfile.hasExplicitTopic("water_distribution")) {
            return Math.min(base, Math.max(limit * 3, 48));
        }
        if (routeProfile.isStarterBuildProject()) {
            return Math.min(base, Math.max(limit * 4, 64));
        }
        return Math.min(base, Math.max(limit * 4, 60));
    }

    static int keywordSqlLimit(QueryRouteProfile routeProfile, int limit) {
        if (routeProfile != null && routeProfile.isStarterBuildProject()) {
            return Math.max(limit * 2, 96);
        }
        return Math.max(limit * 4, 160);
    }

    static boolean allowsWaterDistributionSupportCandidate(
        boolean distributionTagged,
        int sectionBonus,
        boolean strongGuideSignal,
        String retrievalMode,
        boolean missingSectionHeading,
        String contentRole,
        boolean hasDistractorSignal
    ) {
        if (!distributionTagged && sectionBonus <= 0 && !strongGuideSignal) {
            return false;
        }
        if (sectionBonus < 0 && !strongGuideSignal) {
            return false;
        }
        String mode = safe(retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        String role = safe(contentRole).trim().toLowerCase(QUERY_LOCALE);
        if ("guide-focus".equals(mode) && missingSectionHeading) {
            if (("reference".equals(role) || "safety".equals(role)) && sectionBonus <= 0) {
                return false;
            }
            if (hasDistractorSignal) {
                return false;
            }
        }
        return !hasDistractorSignal || sectionBonus > 0;
    }

    static int supportStructurePenalty(boolean diversifyContext, String retrievalMode, String sectionHeading) {
        if (!safe(sectionHeading).trim().isEmpty()) {
            return 0;
        }
        if (!diversifyContext) {
            return -10;
        }
        String mode = safe(retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        return switch (mode) {
            case "guide-focus" -> -40;
            case "route-focus" -> -18;
            case "hybrid", "lexical" -> -24;
            default -> -18;
        };
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class RouteFtsOrderSpec {
        final String clause;
        final List<String> args;
        final String label;

        RouteFtsOrderSpec(String clause, List<String> args, String label) {
            this.clause = clause;
            this.args = args;
            this.label = label;
        }
    }
}

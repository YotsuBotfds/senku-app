package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PackRouteFtsOrderPolicy {
    private static final List<OrderRule> ORDER_RULES = List.of(
        new OrderRule(
            "soapmaking_priority",
            PackRouteFtsOrderPolicy::matchesSoapmaking,
            List.of(
                CaseTerm.like("section_heading", "%soap making - cold process%", 0),
                CaseTerm.like("section_heading", "%making soap%", 0),
                CaseTerm.like("section_heading", "%soap making - hot process%", 0),
                CaseTerm.like("guide_title", "%homestead chemistry%", 1),
                CaseTerm.like("guide_title", "%everyday compounds%", 1),
                CaseTerm.like("section_heading", "%making lye from wood ash%", 2),
                CaseTerm.like("section_heading", "%lye from wood ash%", 2),
                CaseTerm.equals("content_role", "subsystem", 3),
                CaseTerm.like("section_heading", "%rendering fats%", 4),
                CaseTerm.like("section_heading", "%tallow%", 4),
                CaseTerm.like("section_heading", "%cleaning product chemistry%", 8),
                CaseTerm.like("guide_title", "%chemical safety%", 8)
            )
        ),
        new OrderRule(
            "water_distribution_priority",
            PackRouteFtsOrderPolicy::matchesWaterDistribution,
            List.of(
                CaseTerm.like("section_heading", "%gravity-fed%", 0),
                CaseTerm.like("section_heading", "%distribution%", 0),
                CaseTerm.like("section_heading", "%household taps%", 1),
                CaseTerm.like("section_heading", "%spring box%", 1),
                CaseTerm.like("section_heading", "%storage tank%", 2),
                CaseTerm.like("section_heading", "%system components%", 2),
                CaseTerm.like("section_heading", "%layout%", 3),
                CaseTerm.like("section_heading", "%network%", 3),
                CaseTerm.like("section_heading", "%overflow%", 4),
                CaseTerm.like("guide_title", "%water tower%", 5)
            )
        ),
        new OrderRule(
            "clay_oven_priority",
            PackRouteFtsOrderPolicy::matchesClayOven,
            List.of(
                CaseTerm.like("section_heading", "%cob oven%", 0),
                CaseTerm.like("section_heading", "%clay oven%", 0),
                CaseTerm.like("section_heading", "%bread oven%", 0),
                CaseTerm.like("section_heading", "%earth oven%", 1),
                CaseTerm.like("section_heading", "%masonry oven%", 1),
                CaseTerm.like("section_heading", "%hearth%", 2),
                CaseTerm.like("section_heading", "%foundation%", 2),
                CaseTerm.like("section_heading", "%curing%", 3),
                CaseTerm.like("section_heading", "%thermal mass%", 3),
                CaseTerm.like("section_heading", "%chimney%", 4),
                CaseTerm.like("section_heading", "%draft%", 4),
                CaseTerm.like("guide_title", "%clay bread oven%", 5)
            )
        ),
        new OrderRule(
            "community_governance_priority",
            PackRouteFtsOrderPolicy::matchesCommunityGovernance,
            List.of(
                CaseTerm.like("section_heading", "%graduated sanctions%", 0),
                CaseTerm.like("section_heading", "%mediation%", 1),
                CaseTerm.like("section_heading", "%membership%", 1),
                CaseTerm.like("section_heading", "%boundaries%", 2),
                CaseTerm.like("section_heading", "%conflict%", 2),
                CaseTerm.like("section_heading", "%restitution%", 3),
                CaseTerm.like("guide_title", "%commons management%", 3),
                CaseTerm.like("guide_title", "%resource governance%", 4),
                CaseTerm.like("section_heading", "%restorative%", 4),
                CaseTerm.like("section_heading", "%reputation%", 5),
                CaseTerm.like("guide_title", "%insurance%", 8),
                CaseTerm.like("section_heading", "%mutual aid%", 8)
            )
        ),
        new OrderRule(
            "site_selection_microclimate_priority",
            PackRouteFtsOrderPolicy::matchesSiteSelectionMicroclimate,
            List.of(
                CaseTerm.like("section_heading", "%wind exposure%", 0),
                CaseTerm.like("section_heading", "%microclimate%", 0),
                CaseTerm.like("section_heading", "%seasonal%", 0),
                CaseTerm.like("section_heading", "%sun exposure%", 0),
                CaseTerm.like("section_heading", "%site assessment%", 1),
                CaseTerm.like("section_heading", "%terrain analysis%", 1),
                CaseTerm.like("guide_title", "%site selection%", 1),
                CaseTerm.like("section_heading", "%natural hazards%", 2),
                CaseTerm.like("section_heading", "%foundation%", 3),
                CaseTerm.like("section_heading", "%drainage%", 4)
            )
        ),
        new OrderRule(
            "site_selection_priority",
            PackRouteFtsOrderPolicy::matchesSiteSelection,
            List.of(
                CaseTerm.like("section_heading", "%site assessment%", 0),
                CaseTerm.like("section_heading", "%terrain analysis%", 0),
                CaseTerm.like("section_heading", "%wind exposure%", 0),
                CaseTerm.like("section_heading", "%water proximity%", 0),
                CaseTerm.like("section_heading", "%site assessment checklist%", 0),
                CaseTerm.like("guide_title", "%site selection%", 1),
                CaseTerm.like("section_heading", "%natural hazards%", 1),
                CaseTerm.like("section_heading", "%foundation%", 2),
                CaseTerm.like("section_heading", "%drainage%", 3)
            )
        ),
        new OrderRule(
            "roofing_priority",
            PackRouteFtsOrderPolicy::matchesRoofing,
            List.of(
                CaseTerm.like("section_heading", "%roofing%", 0),
                CaseTerm.like("section_heading", "%roof waterproofing%", 0),
                CaseTerm.like("section_heading", "%rainproofing and water shedding%", 1),
                CaseTerm.like("section_heading", "%waterproofing and sealants%", 1),
                CaseTerm.like("guide_title", "%weatherproofing%", 2),
                CaseTerm.like("section_heading", "%roof framing%", 2),
                CaseTerm.like("guide_title", "%roofing materials%", 3),
                CaseTerm.like("guide_title", "%construction & carpentry%", 4)
            )
        ),
        new OrderRule(
            "wall_construction_priority",
            PackRouteFtsOrderPolicy::matchesWallConstruction,
            List.of(
                CaseTerm.like("section_heading", "%wall framing%", 0),
                CaseTerm.like("section_heading", "%wall construction%", 0),
                CaseTerm.like("section_heading", "%walls%", 1),
                CaseTerm.like("section_heading", "%timber frame%", 1),
                CaseTerm.like("guide_title", "%window and door assembly%", 2),
                CaseTerm.like("guide_title", "%construction & carpentry%", 3)
            )
        ),
        new OrderRule(
            "foundation_priority",
            PackRouteFtsOrderPolicy::matchesFoundation,
            List.of(
                CaseTerm.like("section_heading", "%foundations%", 0),
                CaseTerm.like("section_heading", "%footings%", 0),
                CaseTerm.like("section_heading", "%frost line%", 1),
                CaseTerm.like("section_heading", "%drainage%", 2),
                CaseTerm.like("section_heading", "%site drainage%", 3),
                CaseTerm.like("guide_title", "%construction & carpentry%", 4)
            )
        )
    );

    private PackRouteFtsOrderPolicy() {
    }

    static RouteFtsOrderSpec noBm25RouteFtsOrder(String queryLower, QueryMetadataProfile metadataProfile) {
        for (OrderRule rule : ORDER_RULES) {
            if (rule.matches(queryLower, metadataProfile)) {
                return rule.toOrderSpec();
            }
        }
        return new RouteFtsOrderSpec(" ORDER BY c.row_id ", Collections.emptyList(), "rowid");
    }

    static List<String> orderedLabelsForTest() {
        ArrayList<String> labels = new ArrayList<>();
        for (OrderRule rule : ORDER_RULES) {
            labels.add(rule.label);
        }
        labels.add("rowid");
        return labels;
    }

    static int argCountForLabelForTest(String label) {
        for (OrderRule rule : ORDER_RULES) {
            if (rule.label.equals(label)) {
                return rule.terms.size();
            }
        }
        if ("rowid".equals(label)) {
            return 0;
        }
        throw new IllegalArgumentException("Unknown no-BM25 route FTS order label: " + label);
    }

    private static boolean matchesSoapmaking(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "soapmaking");
    }

    private static boolean matchesWaterDistribution(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "water_distribution");
    }

    private static boolean matchesClayOven(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "clay_oven");
    }

    private static boolean matchesCommunityGovernance(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "community_governance");
    }

    private static boolean matchesSiteSelectionMicroclimate(String queryLower, QueryMetadataProfile metadataProfile) {
        if (!matchesSiteSelection(queryLower, metadataProfile)) {
            return false;
        }
        String safeQueryLower = safe(queryLower);
        return safeQueryLower.contains("winter")
            || safeQueryLower.contains("summer")
            || safeQueryLower.contains("shade")
            || safeQueryLower.contains("microclimate")
            || safeQueryLower.contains("orientation");
    }

    private static boolean matchesSiteSelection(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "cabin_house")
            && metadataProfile.hasExplicitTopic("site_selection");
    }

    private static boolean matchesRoofing(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "cabin_house")
            && (metadataProfile.hasExplicitTopic("roofing")
                || metadataProfile.hasExplicitTopic("weatherproofing"));
    }

    private static boolean matchesWallConstruction(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "cabin_house")
            && metadataProfile.hasExplicitTopic("wall_construction");
    }

    private static boolean matchesFoundation(String queryLower, QueryMetadataProfile metadataProfile) {
        return hasStructure(metadataProfile, "cabin_house")
            && metadataProfile.hasExplicitTopic("foundation");
    }

    private static boolean hasStructure(QueryMetadataProfile metadataProfile, String structureType) {
        return metadataProfile != null && structureType.equals(metadataProfile.preferredStructureType());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private interface RuleMatcher {
        boolean matches(String queryLower, QueryMetadataProfile metadataProfile);
    }

    private static final class OrderRule {
        final String label;
        final RuleMatcher matcher;
        final List<CaseTerm> terms;

        OrderRule(String label, RuleMatcher matcher, List<CaseTerm> terms) {
            this.label = label;
            this.matcher = matcher;
            this.terms = terms;
        }

        boolean matches(String queryLower, QueryMetadataProfile metadataProfile) {
            return matcher.matches(queryLower, metadataProfile);
        }

        RouteFtsOrderSpec toOrderSpec() {
            StringBuilder clause = new StringBuilder(" ORDER BY CASE ");
            ArrayList<String> args = new ArrayList<>();
            for (CaseTerm term : terms) {
                clause.append("WHEN lower(c.")
                    .append(term.column)
                    .append(") ")
                    .append(term.operator)
                    .append(" ? THEN ")
                    .append(term.priority)
                    .append(" ");
                args.add(term.arg);
            }
            clause.append("ELSE 9 END, c.row_id ");
            return new RouteFtsOrderSpec(clause.toString(), args, label);
        }
    }

    private static final class CaseTerm {
        final String column;
        final String operator;
        final String arg;
        final int priority;

        private CaseTerm(String column, String operator, String arg, int priority) {
            this.column = column;
            this.operator = operator;
            this.arg = arg;
            this.priority = priority;
        }

        static CaseTerm like(String column, String arg, int priority) {
            return new CaseTerm(column, "LIKE", arg, priority);
        }

        static CaseTerm equals(String column, String arg, int priority) {
            return new CaseTerm(column, "=", arg, priority);
        }
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

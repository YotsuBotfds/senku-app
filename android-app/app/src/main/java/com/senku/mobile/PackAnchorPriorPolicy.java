package com.senku.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class PackAnchorPriorPolicy {
    static final String DIRECTIVE_PREFIX = "__anchor_prior__:";
    static final double BASE_BONUS = 0.08;
    static final double MAX_BONUS = 0.10;

    private PackAnchorPriorPolicy() {
    }

    static AnchorPriorAdjustment adjustment(
        String anchorGuideId,
        int turnsSinceAnchor,
        String rawGuideId,
        Map<String, Double> relatedWeights
    ) {
        double decay = decay(turnsSinceAnchor);
        if (decay <= 0.0) {
            return new AnchorPriorAdjustment(decay, 0.0, 0.0);
        }

        String safeAnchorGuideId = anchorGuideId == null ? "" : anchorGuideId;
        String guideId = rawGuideId == null ? "" : rawGuideId.trim();
        Map<String, Double> safeRelatedWeights = relatedWeights == null
            ? Collections.emptyMap()
            : relatedWeights;
        double weight = 0.0;
        if (safeAnchorGuideId.equals(guideId)) {
            weight = 1.0;
        } else if (!guideId.isEmpty()) {
            weight = safeRelatedWeights.getOrDefault(guideId, 0.0);
        }
        if (weight <= 0.0) {
            return new AnchorPriorAdjustment(decay, weight, 0.0);
        }

        double bonus = Math.min(
            MAX_BONUS,
            Math.max(0.0, BASE_BONUS * decay * weight)
        );
        return new AnchorPriorAdjustment(decay, weight, bonus);
    }

    static Map<String, Map<String, Double>> relatedLinkWeights(
        Map<String, ? extends Set<String>> directedLinks
    ) {
        if (directedLinks == null || directedLinks.isEmpty()) {
            return Collections.emptyMap();
        }

        LinkedHashMap<String, Map<String, Double>> weightedLinks = new LinkedHashMap<>();
        for (Map.Entry<String, ? extends Set<String>> entry : directedLinks.entrySet()) {
            LinkedHashMap<String, Double> weights = new LinkedHashMap<>();
            for (String relatedGuideId : entry.getValue()) {
                Set<String> reciprocalLinks = directedLinks.get(relatedGuideId);
                boolean reciprocal = reciprocalLinks != null && reciprocalLinks.contains(entry.getKey());
                weights.put(relatedGuideId, reciprocal ? 0.5 : 0.3);
            }
            weightedLinks.put(entry.getKey(), Collections.unmodifiableMap(weights));
        }
        return Collections.unmodifiableMap(weightedLinks);
    }

    private static double decay(int turnsSinceAnchor) {
        return switch (turnsSinceAnchor) {
            case 0 -> 1.0;
            case 1 -> 0.6;
            case 2 -> 0.3;
            default -> 0.0;
        };
    }

    static final class AnchorPriorAdjustment {
        final double decay;
        final double weight;
        final double bonus;

        AnchorPriorAdjustment(double decay, double weight, double bonus) {
            this.decay = decay;
            this.weight = weight;
            this.bonus = bonus;
        }

        boolean hasBonus() {
            return bonus > 0.0;
        }
    }
}

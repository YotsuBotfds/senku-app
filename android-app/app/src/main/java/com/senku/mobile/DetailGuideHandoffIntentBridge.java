package com.senku.mobile;

import android.content.Context;
import android.content.Intent;

final class DetailGuideHandoffIntentBridge {
    static final String SOURCE_BROWSE_CROSS_REF = "browse_cross_ref";
    static final String SOURCE_HOME_RELATED_RECENT = "home_related_recent";
    static final String SOURCE_HOME_RELATED_PINNED = "home_related_pinned";

    enum Route {
        GUIDE,
        CROSS_REFERENCE_GUIDE,
        HOME_GUIDE
    }

    static final class HandoffContext {
        final String sourceKind;
        final String sourceGuideId;
        final String sourceGuideLabel;

        HandoffContext(String sourceKind, String sourceGuideId, String sourceGuideLabel) {
            this.sourceKind = safe(sourceKind).trim();
            this.sourceGuideId = safe(sourceGuideId).trim();
            this.sourceGuideLabel = safe(sourceGuideLabel).trim();
        }

        boolean isEmpty() {
            return sourceKind.isEmpty() && sourceGuideId.isEmpty() && sourceGuideLabel.isEmpty();
        }

        String anchorLabel() {
            return !sourceGuideLabel.isEmpty() ? sourceGuideLabel : sourceGuideId;
        }
    }

    private DetailGuideHandoffIntentBridge() {
    }

    static HandoffContext crossReference(String sourceGuideId, String sourceGuideLabel) {
        HandoffContext context = new HandoffContext(SOURCE_BROWSE_CROSS_REF, sourceGuideId, sourceGuideLabel);
        return context.isEmpty() ? null : context;
    }

    static HandoffContext homeRelated(boolean fromRecentThread, String sourceGuideId, String sourceGuideLabel) {
        String normalizedGuideId = safe(sourceGuideId).trim();
        String normalizedLabel = safe(sourceGuideLabel).trim();
        if (normalizedLabel.isEmpty()) {
            normalizedLabel = normalizedGuideId;
        }
        HandoffContext context = new HandoffContext(
            fromRecentThread ? SOURCE_HOME_RELATED_RECENT : SOURCE_HOME_RELATED_PINNED,
            normalizedGuideId,
            normalizedLabel
        );
        return context.isEmpty() ? null : context;
    }

    static Route routeFor(HandoffContext handoffContext) {
        if (handoffContext == null || handoffContext.isEmpty() || handoffContext.anchorLabel().isEmpty()) {
            return Route.GUIDE;
        }
        if (SOURCE_BROWSE_CROSS_REF.equals(handoffContext.sourceKind)) {
            return Route.CROSS_REFERENCE_GUIDE;
        }
        if (SOURCE_HOME_RELATED_RECENT.equals(handoffContext.sourceKind)
            || SOURCE_HOME_RELATED_PINNED.equals(handoffContext.sourceKind)) {
            return Route.HOME_GUIDE;
        }
        return Route.GUIDE;
    }

    static Intent buildIntent(
        Context context,
        SearchResult result,
        String conversationId,
        HandoffContext handoffContext
    ) {
        String anchorLabel = handoffContext == null ? "" : handoffContext.anchorLabel();
        switch (routeFor(handoffContext)) {
            case CROSS_REFERENCE_GUIDE:
                return DetailActivity.newCrossReferenceGuideIntent(context, result, conversationId, anchorLabel, false);
            case HOME_GUIDE:
                return DetailActivity.newHomeGuideIntent(context, result, conversationId, anchorLabel);
            case GUIDE:
            default:
                return DetailActivity.newGuideIntent(context, result, conversationId);
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

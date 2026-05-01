package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

final class AskSearchCoordinator {
    enum SubmitTarget {
        SEARCH,
        ASK
    }

    private AskSearchCoordinator() {
    }

    static SubmitTarget resolveSubmitTarget(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return askLaneActive || activePhoneTab == BottomTabDestination.ASK
            ? SubmitTarget.ASK
            : SubmitTarget.SEARCH;
    }

    static boolean shouldSubmitAsAsk(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return resolveSubmitTarget(activePhoneTab, askLaneActive) == SubmitTarget.ASK;
    }

    static boolean resolveAskLaneActiveForExplicitPhoneFlow(
        BottomTabDestination destination,
        boolean currentAskLaneActive
    ) {
        if (destination == BottomTabDestination.ASK) {
            return true;
        }
        if (destination == null) {
            return currentAskLaneActive;
        }
        return false;
    }

    static BottomTabDestination tabForSubmitTarget(SubmitTarget target) {
        return target == SubmitTarget.ASK
            ? BottomTabDestination.ASK
            : BottomTabDestination.SEARCH;
    }
}

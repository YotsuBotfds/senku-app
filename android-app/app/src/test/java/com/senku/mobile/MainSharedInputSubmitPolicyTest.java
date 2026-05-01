package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import android.view.inputmethod.EditorInfo;

import com.senku.mobile.AskSearchCoordinator.SubmitTarget;
import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

public final class MainSharedInputSubmitPolicyTest {
    @Test
    public void visibleTabAndLaneCasesKeepButtonAndImeSubmitOwnershipAligned() {
        assertSubmitOwnership("ask tab", BottomTabDestination.ASK, false, SubmitTarget.ASK);
        assertSubmitOwnership("active ask lane", BottomTabDestination.HOME, true, SubmitTarget.ASK);
        assertSubmitOwnership("search tab", BottomTabDestination.SEARCH, false, SubmitTarget.SEARCH);
        assertSubmitOwnership("saved tab", BottomTabDestination.PINS, false, SubmitTarget.SEARCH);
        assertSubmitOwnership("home browse", BottomTabDestination.HOME, false, SubmitTarget.SEARCH);
    }

    @Test
    public void restoredResultRoutesKeepButtonAndImeSubmitOwnershipAligned() {
        MainRouteDecisionHelper.RouteState restoredSearch =
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS.name(),
                BottomTabDestination.HOME.name(),
                false,
                true,
                BottomTabDestination.PINS.name()
            );
        MainRouteDecisionHelper.RouteState restoredAsk =
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS.name(),
                BottomTabDestination.ASK.name(),
                true,
                true,
                BottomTabDestination.HOME.name()
            );

        assertSubmitOwnership(
            "restored search results",
            restoredSearch.activePhoneTab,
            restoredSearch.askLaneActive,
            SubmitTarget.SEARCH
        );
        assertSubmitOwnership(
            "restored ask results",
            restoredAsk.activePhoneTab,
            restoredAsk.askLaneActive,
            SubmitTarget.ASK
        );
    }

    private static void assertSubmitOwnership(
        String scenario,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive,
        SubmitTarget expectedTarget
    ) {
        MainSharedInputSubmitPolicy.SharedSubmitAction buttonAction =
            MainSharedInputSubmitPolicy.resolveSearchButtonSubmitAction(
                activePhoneTab,
                askLaneActive,
                true
            );
        SubmitTarget imeTarget =
            AskSearchCoordinator.resolveSubmitTarget(activePhoneTab, askLaneActive);

        assertEquals(scenario + " button target", expectedTarget, buttonAction.target);
        assertEquals(scenario + " ime target", expectedTarget, imeTarget);
        assertEquals(
            scenario + " button tab",
            expectedTarget == SubmitTarget.ASK ? BottomTabDestination.ASK : BottomTabDestination.SEARCH,
            AskSearchCoordinator.tabForSubmitTarget(buttonAction.target)
        );
        assertEquals(
            scenario + " ime action",
            expectedTarget == SubmitTarget.ASK ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_SEARCH,
            SharedInputChromePolicy.inputImeAction(imeTarget)
        );
        assertEquals(
            scenario + " button text",
            SharedInputChromePolicy.submitButtonLabelResource(expectedTarget, true),
            buttonAction.buttonTextResource
        );
        assertEquals(
            scenario + " button description",
            SharedInputChromePolicy.submitButtonDescriptionResource(expectedTarget),
            buttonAction.buttonDescriptionResource
        );
    }
}

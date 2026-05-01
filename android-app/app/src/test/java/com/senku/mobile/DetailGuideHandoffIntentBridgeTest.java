package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public final class DetailGuideHandoffIntentBridgeTest {
    @Test
    public void routeForUsesPlainGuideWhenContextIsMissingOrUnanchored() {
        assertEquals(
            DetailGuideHandoffIntentBridge.Route.GUIDE,
            DetailGuideHandoffIntentBridge.routeFor(null)
        );
        assertEquals(
            DetailGuideHandoffIntentBridge.Route.GUIDE,
            DetailGuideHandoffIntentBridge.routeFor(
                new DetailGuideHandoffIntentBridge.HandoffContext("browse_cross_ref", "", "")
            )
        );
    }

    @Test
    public void crossReferenceContextRoutesToCrossReferenceGuideAndTrimsAnchor() {
        DetailGuideHandoffIntentBridge.HandoffContext context =
            DetailGuideHandoffIntentBridge.crossReference(" GD-232 ", " Choking Basics ");

        assertEquals("browse_cross_ref", context.sourceKind);
        assertEquals("GD-232", context.sourceGuideId);
        assertEquals("Choking Basics", context.anchorLabel());
        assertEquals(
            DetailGuideHandoffIntentBridge.Route.CROSS_REFERENCE_GUIDE,
            DetailGuideHandoffIntentBridge.routeFor(context)
        );
    }

    @Test
    public void homeRelatedContextFallsBackToGuideIdAnchorAndRoutesHomeGuide() {
        DetailGuideHandoffIntentBridge.HandoffContext context =
            DetailGuideHandoffIntentBridge.homeRelated(false, " GD-585 ", " ");

        assertEquals("home_related_pinned", context.sourceKind);
        assertEquals("GD-585", context.sourceGuideId);
        assertEquals("GD-585", context.anchorLabel());
        assertEquals(
            DetailGuideHandoffIntentBridge.Route.HOME_GUIDE,
            DetailGuideHandoffIntentBridge.routeFor(context)
        );
    }

    @Test
    public void unknownSourceKindStaysPlainGuideEvenWhenAnchored() {
        DetailGuideHandoffIntentBridge.HandoffContext context =
            new DetailGuideHandoffIntentBridge.HandoffContext("other", "GD-898", "Poisoning");

        assertFalse(context.isEmpty());
        assertEquals(
            DetailGuideHandoffIntentBridge.Route.GUIDE,
            DetailGuideHandoffIntentBridge.routeFor(context)
        );
    }

    @Test
    public void contextFactoriesPreserveSourceKindOnlyPayloadsForPlainGuideFallback() {
        DetailGuideHandoffIntentBridge.HandoffContext context =
            DetailGuideHandoffIntentBridge.crossReference("", "");

        assertNotNull(context);
        assertEquals(
            DetailGuideHandoffIntentBridge.Route.GUIDE,
            DetailGuideHandoffIntentBridge.routeFor(context)
        );
    }
}

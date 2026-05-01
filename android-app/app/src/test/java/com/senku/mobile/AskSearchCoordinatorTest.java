package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

public final class AskSearchCoordinatorTest {
    @Test
    public void submitTargetRoutesToAskForAskTabOrActiveAskLane() {
        assertEquals(
            AskSearchCoordinator.SubmitTarget.ASK,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.ASK, false)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.ASK,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.HOME, true)
        );

        assertTrue(AskSearchCoordinator.shouldSubmitAsAsk(BottomTabDestination.ASK, false));
        assertTrue(AskSearchCoordinator.shouldSubmitAsAsk(BottomTabDestination.HOME, true));
    }

    @Test
    public void imeSubmitIsOwnedByAskWhenAskTabOrAskLaneIsActive() {
        assertEquals(
            AskSearchCoordinator.SubmitTarget.ASK,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.ASK, true)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.ASK,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.THREADS, true)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.ASK,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.SEARCH, true)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.ASK,
            AskSearchCoordinator.resolveSubmitTarget(null, true)
        );
    }

    @Test
    public void submitTargetRoutesToSearchOutsideAskOwnership() {
        assertEquals(
            AskSearchCoordinator.SubmitTarget.SEARCH,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.HOME, false)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.SEARCH,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.SEARCH, false)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.SEARCH,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.PINS, false)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.SEARCH,
            AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.THREADS, false)
        );
        assertEquals(
            AskSearchCoordinator.SubmitTarget.SEARCH,
            AskSearchCoordinator.resolveSubmitTarget(null, false)
        );

        assertFalse(AskSearchCoordinator.shouldSubmitAsAsk(BottomTabDestination.HOME, false));
        assertFalse(AskSearchCoordinator.shouldSubmitAsAsk(BottomTabDestination.SEARCH, false));
        assertFalse(AskSearchCoordinator.shouldSubmitAsAsk(BottomTabDestination.PINS, false));
        assertFalse(AskSearchCoordinator.shouldSubmitAsAsk(BottomTabDestination.THREADS, false));
        assertFalse(AskSearchCoordinator.shouldSubmitAsAsk(null, false));
    }

    @Test
    public void searchButtonFlowUsesSubmitTargetTabOwnership() {
        assertEquals(
            BottomTabDestination.SEARCH,
            AskSearchCoordinator.tabForSubmitTarget(
                AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.HOME, false)
            )
        );
        assertEquals(
            BottomTabDestination.SEARCH,
            AskSearchCoordinator.tabForSubmitTarget(
                AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.PINS, false)
            )
        );
        assertEquals(
            BottomTabDestination.ASK,
            AskSearchCoordinator.tabForSubmitTarget(
                AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.ASK, false)
            )
        );
        assertEquals(
            BottomTabDestination.ASK,
            AskSearchCoordinator.tabForSubmitTarget(
                AskSearchCoordinator.resolveSubmitTarget(BottomTabDestination.HOME, true)
            )
        );
    }

    @Test
    public void explicitPhoneFlowAskLaneOwnershipFollowsAskSearchDestination() {
        assertTrue(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(
            BottomTabDestination.ASK,
            false
        ));
        assertTrue(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(
            BottomTabDestination.ASK,
            true
        ));

        assertFalse(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(
            BottomTabDestination.SEARCH,
            true
        ));
        assertFalse(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(
            BottomTabDestination.HOME,
            true
        ));
        assertFalse(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(
            BottomTabDestination.THREADS,
            true
        ));
        assertFalse(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(
            BottomTabDestination.PINS,
            true
        ));

        assertTrue(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(null, true));
        assertFalse(AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(null, false));
    }

    @Test
    public void tabForSubmitTargetReturnsSubmitFlowTab() {
        assertEquals(
            BottomTabDestination.ASK,
            AskSearchCoordinator.tabForSubmitTarget(AskSearchCoordinator.SubmitTarget.ASK)
        );
        assertEquals(
            BottomTabDestination.SEARCH,
            AskSearchCoordinator.tabForSubmitTarget(AskSearchCoordinator.SubmitTarget.SEARCH)
        );
        assertEquals(
            BottomTabDestination.SEARCH,
            AskSearchCoordinator.tabForSubmitTarget(null)
        );
    }
}

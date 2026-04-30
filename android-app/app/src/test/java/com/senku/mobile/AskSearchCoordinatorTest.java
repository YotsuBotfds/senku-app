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

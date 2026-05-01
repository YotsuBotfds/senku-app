package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class LatestJobGateTest {
    @Test
    public void nextJobTokenMakesOnlyNewestTokenCurrent() {
        LatestJobGate gate = new LatestJobGate();

        long first = gate.nextJobToken();
        long second = gate.nextJobToken();

        assertNotEquals(first, second);
        assertFalse(gate.isCurrentJob(first));
        assertTrue(gate.isCurrentJob(second));
    }

    @Test
    public void unknownTokenIsNotCurrent() {
        LatestJobGate gate = new LatestJobGate();

        assertFalse(gate.isCurrentJob(1L));
    }
}

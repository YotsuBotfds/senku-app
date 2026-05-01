package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailActionBlockRenderPolicyTest {
    @Test
    public void emergencyPortraitActionBlocksTakePriorityOverHighRiskBlocks() {
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.EMERGENCY_PORTRAIT,
            DetailActionBlockRenderPolicy.evaluate(
                true,
                true,
                true,
                true,
                "1. Stop the hazard."
            )
        );
    }

    @Test
    public void deterministicHighRiskAnswerWithBodyShowsHighRiskActionBlocks() {
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.HIGH_RISK,
            DetailActionBlockRenderPolicy.evaluate(
                false,
                true,
                true,
                true,
                "1. Apply pressure. 2. Escalate."
            )
        );
    }

    @Test
    public void highRiskActionBlocksStayHiddenOutsideOriginalEligibility() {
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActionBlockRenderPolicy.evaluate(false, false, true, true, "1. Apply pressure.")
        );
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActionBlockRenderPolicy.evaluate(false, true, false, true, "1. Apply pressure.")
        );
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActionBlockRenderPolicy.evaluate(false, true, true, false, "1. Apply pressure.")
        );
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActionBlockRenderPolicy.evaluate(false, true, true, true, "   ")
        );
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActionBlockRenderPolicy.evaluate(false, true, true, true, null)
        );
    }

    @Test
    public void detailActivityWrapperDelegatesToTopLevelPolicy() {
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.HIGH_RISK,
            DetailActivity.resolveActionBlockRenderDecision(
                false,
                true,
                true,
                true,
                "1. Apply pressure."
            )
        );
    }
}

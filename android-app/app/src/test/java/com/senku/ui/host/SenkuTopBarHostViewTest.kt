package com.senku.ui.host

import org.junit.Assert.assertEquals
import org.junit.Test

class SenkuTopBarHostViewTest {
    @Test
    fun normalizesMojibakeBulletsForSharedHeaders() {
        assertEquals(
            "ANSWER GD-132 \u2022 Burn hazard",
            normalizeTopBarHeaderText("ANSWER GD-132 \u00C3\u00A2\u00E2\u201A\u00AC\u00C2\u00A2 Burn hazard"),
        )
        assertEquals(
            "THREAD GD-220 \u2022 Rain shelter \u2022 2 turns",
            normalizeTopBarHeaderText("THREAD GD-220 \u00E2\u20AC\u00A2 Rain shelter \u00E2\u20AC\u00A2 2 turns"),
        )
    }
}

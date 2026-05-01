package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class CopySanitizerTest {
    @Test
    public void sharedDetailSanitizerRemovesWarningResidualBrackets() {
        String cleaned = DetailActivity.sanitizeWarningResidualCopy(
            "Act immediately [Instructional Mandate]. "
                + "Use boiled water only [Safety Warning Implied by High Heat Processes] [GD-123]. "
                + "Keep pressure on the wound [System Instruction]."
        );

        assertTrue(cleaned.contains("[GD-123]"));
        assertFalse(cleaned.contains("[Instructional Mandate]"));
        assertFalse(cleaned.contains("[Safety Warning Implied by High Heat Processes]"));
        assertFalse(cleaned.contains("[System Instruction]"));
    }

    @Test
    public void guideBodySanitizerKeepsGuideWarningsReadableWhileDroppingResidualLabels() {
        String cleaned = GuideBodySanitizer.sanitizeGuideBodyForDisplay(
            "## Safe storage\n"
                + ":::warning\n"
                + "WARNING:\n"
                + "Use boiled water only [Instructional Constraint] [GD-123].\n"
                + ":::"
        );

        assertTrue(cleaned.contains("WARNING"));
        assertTrue(cleaned.contains("[GD-123]"));
        assertFalse(cleaned.contains("Instructional Constraint"));
        assertFalse(cleaned.contains(":::"));
    }

    @Test
    public void searchResultDisplayCleanupPreservesMeaningfulBrackets() {
        String cleaned = SearchResultCardModelMapper.cleanDisplayTextForTest(
            "Seal containers [Safety Warning]. Use the agreed mark from [Community leader] [GD-123].",
            0
        );

        assertTrue(cleaned.contains("[Community leader]"));
        assertTrue(cleaned.contains("[GD-123]"));
        assertFalse(cleaned.contains("[Safety Warning]"));
    }
}

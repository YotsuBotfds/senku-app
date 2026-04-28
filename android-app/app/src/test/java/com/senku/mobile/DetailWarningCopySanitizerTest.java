package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DetailWarningCopySanitizerTest {
    @Test
    public void sanitizeWarningResidualCopy_keepsUrgentWarningWordsReadable() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "WARNING: Call emergency services now [Safety Warning]. "
                + "Do not wait for symptoms to improve [Instructional Mandate]."
        );

        assertTrue(cleaned.contains("WARNING: Call emergency services now."));
        assertTrue(cleaned.contains("Do not wait for symptoms to improve."));
        assertFalse(cleaned.contains("[Safety Warning]"));
        assertFalse(cleaned.contains("[Instructional Mandate]"));
    }

    @Test
    public void sanitizeWarningResidualCopy_removesResidualSafetyMarkersButKeepsCitations() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "Use boiled water only [Safety Warning Implied by High Heat Processes] [GD-123]. "
                + "Keep the person warm [GD/7, GD-42]."
        );

        assertEquals(
            "Use boiled water only [GD-123]. Keep the person warm [GD/7, GD-42].",
            cleaned
        );
    }

    @Test
    public void sanitizeWarningResidualCopy_preservesMeaningfulBracketedWarningContext() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "WARNING: Follow the posted [clinic evacuation route]. "
                + "Contact the [community responder] [GD-8]."
        );

        assertEquals(
            "WARNING: Follow the posted [clinic evacuation route]. "
                + "Contact the [community responder] [GD-8].",
            cleaned
        );
    }

    @Test
    public void sanitizeWarningResidualCopy_trimsAfterLeadingResidualRemoval() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "[System Warning] Stop routine planning now [Safety Advisory]."
        );

        assertEquals("Stop routine planning now.", cleaned);
    }

    @Test
    public void sanitizeWarningResidualCopy_removesGenericProofGuideChromeLabels() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "Use the reviewed card evidence [Proof Metadata]. Keep GD-132 visible [Guide Source Chrome]."
        );

        assertEquals("Use the reviewed card evidence. Keep GD-132 visible.", cleaned);
    }
}

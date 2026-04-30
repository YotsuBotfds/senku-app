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

    @Test
    public void sanitizeWarningResidualCopy_removesReviewedProofChromeButKeepsEvidenceWords() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "WHY THIS ANSWER: GD-132 is the source [Reviewed Card Proof]. "
                + "Keep the action visible [Backend Route reviewed_card_runtime]."
        );

        assertEquals(
            "WHY THIS ANSWER: GD-132 is the source. Keep the action visible.",
            cleaned
        );
    }

    @Test
    public void sanitizeWarningResidualCopy_compactsEmergencyGd132ChromeCopy() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "Stop work immediately. Move to minimum five meters from the active work zone. "
                + "Clear the floor to a minimum 5 m radius. "
                + "Doors and roll-up openings must be unobstructed. "
                + "GD-132 \u00a71 is current owner."
        );

        assertEquals(
            "Stop work immediately. Move to minimum 5 m from active work zone. "
                + "Clear the floor to 5 m radius. "
                + "Door and roll-up open and unobstructed. "
                + "GD-132 lists current owner.",
            cleaned
        );
    }

    @Test
    public void sanitizeWarningResidualCopy_compactsBurnHazardMockVariants() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "Move everyone to minimum 5 m from the active work zone. "
                + "Clear the floor to minimum 5 m radius. "
                + "The guide lists the current owner."
        );

        assertEquals(
            "Move to minimum 5 m from active work zone. "
                + "Clear the floor to 5 m radius. "
                + "The guide lists the current owner.",
            cleaned
        );
    }

    @Test
    public void sanitizeWarningResidualCopy_preservesGenericOwnerGuideWording() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "The guide lists the current owner."
        );

        assertEquals("The guide lists the current owner.", cleaned);
    }

    @Test
    public void sanitizeWarningResidualCopy_removesRouteMetaStatusResidue() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "Stop all hot work [Route Metadata reviewed_card_runtime]. "
                + "Keep distance [Answer Status answered ok]. "
                + "Use GD-132 [Source Proof anchor]."
        );

        assertEquals("Stop all hot work. Keep distance. Use GD-132.", cleaned);
    }

    @Test
    public void sanitizeWarningResidualCopy_trimsInlineEmergencyActionStackFromBannerCopy() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "Stop work immediately. Move to minimum five meters from the active work zone. "
                + "Confirm two paths of egress. STEPS 1. Stop all hot work. "
                + "2. Clear the floor to 5 m radius."
        );

        assertEquals(
            "Stop work immediately. Move to minimum 5 m from active work zone. Confirm two paths of egress.",
            cleaned
        );
    }

    @Test
    public void sanitizeWarningResidualCopy_keepsStandaloneEmergencyActionSections() {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(
            "Stop work immediately. Move to minimum five meters from the active work zone.\n\n"
                + "FIELD STEPS\n"
                + "1. Stop all hot work. No new charges, no new pours.\n"
                + "2. Clear the floor to a 5 m radius. Move personnel upwind."
        );

        assertTrue(cleaned.contains("FIELD STEPS"));
        assertTrue(cleaned.contains("1. Stop all hot work."));
        assertTrue(cleaned.contains("2. Clear the floor to 5 m radius."));
    }
}

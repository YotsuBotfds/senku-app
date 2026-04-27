package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class EmergencySurfacePolicyTest {
    @Test
    public void reviewedDeterministicHighRiskEmergencyQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:poisoning_unknown_ingestion",
                "First Aid & Emergency Response",
                "pilot_reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertTrue(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_ELIGIBLE, decision.reason);
    }

    @Test
    public void reviewedDeterministicSepsisEmergencyQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:newborn_danger_sepsis",
                "Pediatric Emergency Medicine",
                "reviewed",
                "medium",
                "high",
                "deterministic_rule",
                2
            )
        );

        assertTrue(decision.eligible);
    }

    @Test
    public void routineGuideReadingDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "guide:minor_wound_cleaning",
                "First Aid & Emergency Response",
                "reviewed",
                "high",
                "high",
                "guide_reading",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_GUIDE_READING, decision.reason);
    }

    @Test
    public void nonEmergencyMedicalEducationDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:hydration_basics",
                "General Medical Education",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void uncertainFitDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:choking_airway_obstruction",
                "First Aid & Emergency Response",
                "pilot_reviewed",
                "high",
                "high",
                "uncertain_fit",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_UNCERTAIN_FIT, decision.reason);
    }

    @Test
    public void lowCoverageDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:abdominal_internal_bleeding",
                "Emergency escalation",
                "pilot_reviewed",
                "low",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_LOW_COVERAGE, decision.reason);
    }

    @Test
    public void generatedOnlyTextDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:meningitis_sepsis_child",
                "Pediatric Emergency Medicine",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                0
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_GENERATED_ONLY, decision.reason);
    }

    @Test
    public void generatedProvenanceDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            new EmergencySurfacePolicy.Input(
                true,
                "answer_card:infected_wound_spreading_infection",
                "Emergency escalation",
                "reviewed",
                "generated_model",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_UNREVIEWED, decision.reason);
    }

    @Test
    public void nonDeterministicReviewedEmergencyDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                false,
                "answer_card:poisoning_unknown_ingestion",
                "First Aid & Emergency Response",
                "pilot_reviewed",
                "high",
                "high",
                "generated_model",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_DETERMINISTIC, decision.reason);
    }

    private static EmergencySurfacePolicy.Input input(
        boolean deterministic,
        String ruleId,
        String category,
        String reviewStatus,
        String coverageLabel,
        String confidenceLabel,
        String answerMode,
        int citedReviewedSourceCount
    ) {
        return new EmergencySurfacePolicy.Input(
            deterministic,
            ruleId,
            category,
            reviewStatus,
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            coverageLabel,
            confidenceLabel,
            answerMode,
            citedReviewedSourceCount
        );
    }
}

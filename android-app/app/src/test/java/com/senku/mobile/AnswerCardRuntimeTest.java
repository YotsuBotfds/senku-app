package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Test;

public final class AnswerCardRuntimeTest {
    @After
    public void resetRuntimeFlag() {
        AnswerCardRuntime.resetEnabledForTest();
    }

    @Test
    public void reviewedCardRuntimeFlagDefaultsOffForNullContext() {
        assertFalse(AnswerCardRuntime.isEnabledForTest(null));

        AnswerCardRuntime.setEnabledForTest(true);

        assertTrue(AnswerCardRuntime.isEnabledForTest(null));
    }

    @Test
    public void poisoningAnswerCardPilotQueryRequiresActionAndUnknownObject() {
        assertTrue(
            AnswerCardRuntime.isPoisoningAnswerCardPilotQueryForTest(
                "my child swallowed an unknown cleaner"
            )
        );
        assertTrue(
            AnswerCardRuntime.isPoisoningAnswerCardPilotQueryForTest(
                "someone drank bleach from an unlabeled bottle"
            )
        );
        assertFalse(
            AnswerCardRuntime.isPoisoningAnswerCardPilotQueryForTest(
                "how should i store bleach under the sink"
            )
        );
        assertFalse(
            AnswerCardRuntime.isPoisoningAnswerCardPilotQueryForTest(
                "i swallowed a grape"
            )
        );
        assertFalse(
            AnswerCardRuntime.isPoisoningAnswerCardPilotQueryForTest(
                "can i drink water from a clean bottle"
            )
        );
    }

    @Test
    public void poisoningAnswerCardBuildsReviewedAnswerPlan() {
        AnswerCardRuntime.AnswerPlan plan =
            AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                "my child swallowed an unknown cleaner",
                List.of(poisoningAnswerCard("pilot_reviewed"))
            );

        assertNotNull(plan);
        assertEquals("answer_card:poisoning_unknown_ingestion", plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals("poisoning_unknown_ingestion", plan.reviewedCardMetadata.cardId);
        assertEquals("GD-898", plan.reviewedCardMetadata.cardGuideId);
        assertEquals("pilot_reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals("reviewed_source_family", plan.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            plan.reviewedCardMetadata.provenance
        );
        assertEquals(List.of("GD-898"), plan.reviewedCardMetadata.citedReviewedSourceGuideIds);
        assertEquals(1, plan.sources.size());
        assertEquals("GD-898", plan.sources.get(0).guideId);
        assertEquals("answer-card", plan.sources.get(0).retrievalMode);
        assertTrue(plan.answerText.contains("Call poison control"));
        assertTrue(plan.answerText.contains("Keep the child with an adult"));
        assertTrue(plan.answerText.contains("Avoid: Do not induce vomiting"));
    }

    @Test
    public void poisoningAnswerCardActivatesConditionalsFromQuestionOnly() {
        AnswerCardRuntime.AnswerPlan adult =
            AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                "adult swallowed an unknown cleaner",
                List.of(poisoningAnswerCard("pilot_reviewed"))
            );

        assertNotNull(adult);
        assertFalse(adult.answerText.contains("Keep the child with an adult"));
    }

    @Test
    public void poisoningAnswerCardRejectsNonPilotAndAmbiguousCardSets() {
        AnswerCard card = poisoningAnswerCard("pilot_reviewed");

        assertNull(
            AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                "how should i store bleach under the sink",
                List.of(card)
            )
        );
        assertNull(
            AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                "my child swallowed an unknown cleaner",
                List.of()
            )
        );
        assertNull(
            AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                "my child swallowed an unknown cleaner",
                List.of(card, card)
            )
        );
        assertNull(
            AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                "my child swallowed an unknown cleaner",
                List.of(poisoningAnswerCard("draft"))
            )
        );
    }

    @Test
    public void poisoningAnswerCardRequiresVisibleSourceRows() {
        assertNull(
            AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                "my child swallowed an unknown cleaner",
                List.of(poisoningAnswerCard("pilot_reviewed", List.of()))
            )
        );
    }

    @Test
    public void newbornDangerSepsisSelectorRequiresNewbornAgeAndDangerSign() {
        assertTrue(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "newborn is limp, will not feed, and is hard to wake"
            )
        );
        assertTrue(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "3 day old baby has fever and fast breathing"
            )
        );
        assertTrue(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "4 week old infant has low temperature and no wet diaper"
            )
        );
        assertTrue(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "newborn cord has pus and spreading redness"
            )
        );
        assertFalse(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "routine newborn care and sleep schedule"
            )
        );
        assertFalse(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "older child has fever and fast breathing"
            )
        );
        assertFalse(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "adult fever and trouble breathing"
            )
        );
        assertFalse(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "clean newborn cord care without redness or pus"
            )
        );
        assertFalse(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "5 week old baby has fever and fast breathing"
            )
        );
        assertFalse(
            AnswerCardRuntime.isNewbornDangerSepsisAnswerCardQueryForTest(
                "28 week old baby has fever and fast breathing"
            )
        );
    }

    @Test
    public void newbornDangerSepsisBuildsReviewedAnswerPlan() {
        AnswerCardRuntime.AnswerPlan plan =
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "3 day old baby has fever and fast breathing",
                List.of(newbornDangerSepsisAnswerCard("reviewed"))
            );

        assertNotNull(plan);
        assertEquals("answer_card:newborn_danger_sepsis", plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals("newborn_danger_sepsis", plan.reviewedCardMetadata.cardId);
        assertEquals("GD-284", plan.reviewedCardMetadata.cardGuideId);
        assertEquals("reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals("reviewed_source_family", plan.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            plan.reviewedCardMetadata.provenance
        );
        assertEquals(List.of("GD-284"), plan.reviewedCardMetadata.citedReviewedSourceGuideIds);
        assertEquals(1, plan.sources.size());
        assertEquals("GD-284", plan.sources.get(0).guideId);
        assertEquals("answer-card", plan.sources.get(0).retrievalMode);
        assertEquals("safety_newborn_danger", plan.sources.get(0).structureType);
        assertTrue(plan.answerText.contains("Seek emergency care now"));
        assertTrue(plan.answerText.contains("Keep the newborn warm"));
        assertTrue(plan.answerText.contains("Escalate now if the newborn is limp"));
        assertTrue(plan.answerText.contains("Avoid: Do not wait to see if symptoms improve"));
    }

    @Test
    public void newbornDangerSepsisActivatesCordConditionalFromQuestionOnly() {
        AnswerCardRuntime.AnswerPlan cord =
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn cord has pus and spreading redness",
                List.of(newbornDangerSepsisAnswerCard("approved"))
            );
        AnswerCardRuntime.AnswerPlan limp =
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of(newbornDangerSepsisAnswerCard("approved"))
            );

        assertNotNull(cord);
        assertNotNull(limp);
        assertTrue(cord.answerText.contains("Keep the cord area clean and uncovered"));
        assertFalse(limp.answerText.contains("Keep the cord area clean and uncovered"));
    }

    @Test
    public void newbornDangerSepsisRejectsAmbiguousIneligibleAndMissingSourceCards() {
        AnswerCard card = newbornDangerSepsisAnswerCard("pilot_reviewed");

        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "routine newborn care",
                List.of(card)
            )
        );
        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of()
            )
        );
        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of(card, card)
            )
        );
        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of(newbornDangerSepsisAnswerCard("draft"))
            )
        );
        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of(newbornDangerSepsisAnswerCard(
                    "approved",
                    "wrong_newborn_card",
                    "GD-284",
                    "critical",
                    newbornSources()
                ))
            )
        );
        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of(newbornDangerSepsisAnswerCard(
                    "approved",
                    "newborn_danger_sepsis",
                    "GD-999",
                    "critical",
                    newbornSources()
                ))
            )
        );
        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of(newbornDangerSepsisAnswerCard(
                    "approved",
                    "newborn_danger_sepsis",
                    "GD-284",
                    "routine",
                    newbornSources()
                ))
            )
        );
        assertNull(
            AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                "newborn is limp and hard to wake",
                List.of(newbornDangerSepsisAnswerCard(
                    "approved",
                    "newborn_danger_sepsis",
                    "GD-284",
                    "critical",
                    List.of()
                ))
            )
        );
    }

    @Test
    public void chokingAirwayObstructionSelectorRequiresChokingContextAndActionSignal() {
        assertTrue(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("food stuck and they cannot speak"));
        assertTrue(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("child is choking on a grape and turning blue"));
        assertTrue(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("infant choking and cannot cry"));
        assertTrue(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("should I do back blows or Heimlich first"));
        assertFalse(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("how do I prevent choking hazards in a toddler play area"));
        assertFalse(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("panic attack and hyperventilating but can talk and breathe"));
        assertFalse(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("I feel like I am choking during a panic attack but I can talk and cough"));
        assertFalse(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("throat closing with hives after a bee sting"));
        assertFalse(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("child swallowed unknown cleaner and is coughing"));
        assertFalse(AnswerCardRuntime.isChokingAirwayObstructionAnswerCardQueryForTest("toddler choked on a peanut then stopped choking but now keeps coughing and wheezing"));
    }

    @Test
    public void chokingAirwayObstructionBuildsReviewedAnswerPlan() {
        AnswerCardRuntime.AnswerPlan plan =
            AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
                "child is choking on a grape and cannot speak",
                List.of(chokingAirwayObstructionAnswerCard("pilot_reviewed"))
            );

        assertNotNull(plan);
        assertEquals("answer_card:choking_airway_obstruction", plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals("choking_airway_obstruction", plan.reviewedCardMetadata.cardId);
        assertEquals("GD-232", plan.reviewedCardMetadata.cardGuideId);
        assertEquals("pilot_reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals("", plan.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, plan.reviewedCardMetadata.provenance);
        assertEquals(List.of("GD-232", "GD-284", "GD-298", "GD-617"), plan.reviewedCardMetadata.citedReviewedSourceGuideIds);
        assertEquals(4, plan.sources.size());
        assertEquals("GD-232", plan.sources.get(0).guideId);
        assertEquals("answer-card", plan.sources.get(0).retrievalMode);
        assertEquals("safety_airway_obstruction", plan.sources.get(0).structureType);
        assertTrue(plan.answerText.contains("call for emergency help and start age-appropriate choking rescue"));
        assertTrue(plan.answerText.contains("Escalate now if Cannot speak, cough, or breathe."));
        assertTrue(plan.answerText.contains("Avoid: Do not do blind finger sweeps."));
        assertTrue(plan.answerText.contains("Avoid: Do not perform abdominal thrusts on infants."));
    }

    @Test
    public void chokingAirwayObstructionActivatesConditionalsFromQuestionOnly() {
        AnswerCardRuntime.AnswerPlan infant =
            AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
                "infant choking and cannot cry",
                List.of(chokingAirwayObstructionAnswerCard("approved"))
            );
        AnswerCardRuntime.AnswerPlan adult =
            AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
                "adult choking on food and cannot speak",
                List.of(chokingAirwayObstructionAnswerCard("approved"))
            );

        assertNotNull(infant);
        assertNotNull(adult);
        assertTrue(infant.answerText.contains("use 5 back blows followed by 5 chest thrusts"));
        assertFalse(infant.answerText.contains("start abdominal thrusts"));
        assertTrue(adult.answerText.contains("start abdominal thrusts"));
        assertFalse(adult.answerText.contains("use 5 back blows followed by 5 chest thrusts"));
    }

    @Test
    public void chokingAirwayObstructionRejectsAmbiguousIneligibleAndMissingSourceCards() {
        AnswerCard card = chokingAirwayObstructionAnswerCard("pilot_reviewed");

        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "how do I prevent choking hazards in a toddler play area",
            List.of(card)
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of()
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of(card, card)
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of(chokingAirwayObstructionAnswerCard("draft"))
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of(chokingAirwayObstructionAnswerCard("approved", "wrong_choking_card", "GD-232", "critical", chokingSources()))
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of(chokingAirwayObstructionAnswerCard("approved", "choking_airway_obstruction", "GD-999", "critical", chokingSources()))
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of(chokingAirwayObstructionAnswerCard("approved", "choking_airway_obstruction", "GD-232", "routine", chokingSources()))
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of(chokingAirwayObstructionAnswerCard("approved", "choking_airway_obstruction", "GD-232", "critical", List.of()))
        ));
    }

    @Test
    public void meningitisSepsisChildSelectorRequiresRedFlagClinicalPrompt() {
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "child has fever and a stiff neck"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "fever with stiff neck: meningitis vs viral illness?"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "fever with little purple dots and a non-blanching rash"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "child has high fever and the rash looks bruise-like"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "kid has fever, confusion, and unusual sleepiness"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "fever with severe headache, photophobia, vomiting, and neck stiffness"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "sore all over with fever and a spreading purplish rash and now acting confused"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "child is hard to wake and has a petechial rash"
        ));
        assertTrue(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "could this be sepsis, very sick with fever and cold hands"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "is this meningitis or a viral illness"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "meningitis vs viral illness"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "could this be meningitis or just a virus"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "is this sepsis or the flu"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "meningitis outbreak reporting and contact tracing quarantine rules"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "routine rash and fever but acting normal"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "seatbelt bruise with neck pain after a crash"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "what is meningitis"
        ));
        assertFalse(AnswerCardRuntime.isMeningitisSepsisChildAnswerCardQueryForTest(
            "sepsis"
        ));
    }

    @Test
    public void meningitisSepsisChildBuildsReviewedAnswerPlan() {
        AnswerCardRuntime.AnswerPlan plan =
            AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
                "child has fever with purple rash and confusion",
                List.of(meningitisSepsisChildAnswerCard("reviewed"))
            );

        assertNotNull(plan);
        assertEquals("answer_card:meningitis_sepsis_child", plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals("meningitis_sepsis_child", plan.reviewedCardMetadata.cardId);
        assertEquals("GD-589", plan.reviewedCardMetadata.cardGuideId);
        assertEquals("reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals("reviewed_source_family", plan.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, plan.reviewedCardMetadata.provenance);
        assertEquals(List.of("GD-589", "GD-235", "GD-268", "GD-284", "GD-298"), plan.reviewedCardMetadata.citedReviewedSourceGuideIds);
        assertEquals(5, plan.sources.size());
        assertEquals("GD-589", plan.sources.get(0).guideId);
        assertEquals("GD-235", plan.sources.get(1).guideId);
        assertEquals("answer-card", plan.sources.get(0).retrievalMode);
        assertEquals("safety_meningitis_sepsis", plan.sources.get(0).structureType);
        assertTrue(plan.answerText.contains("Seek emergency care now"));
        assertTrue(plan.answerText.contains("Treat purple or non-blanching rash with fever as an emergency"));
        assertTrue(plan.answerText.contains("Escalate now if confusion, hard-to-wake sleepiness, or collapse is present."));
        assertTrue(plan.answerText.contains("Avoid: Do not give leftover antibiotics."));
    }

    @Test
    public void meningitisSepsisChildActivatesConditionalsFromQuestionOnly() {
        AnswerCardRuntime.AnswerPlan rash =
            AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
                "child has fever and a purple rash",
                List.of(meningitisSepsisChildAnswerCard("approved"))
            );
        AnswerCardRuntime.AnswerPlan stiffNeck =
            AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
                "child has fever and stiff neck",
                List.of(meningitisSepsisChildAnswerCard("approved"))
            );

        assertNotNull(rash);
        assertNotNull(stiffNeck);
        assertTrue(rash.answerText.contains("Treat purple or non-blanching rash with fever as an emergency"));
        assertFalse(stiffNeck.answerText.contains("Treat purple or non-blanching rash with fever as an emergency"));
    }

    @Test
    public void meningitisSepsisChildRejectsAmbiguousIneligibleAndMissingSourceCards() {
        AnswerCard card = meningitisSepsisChildAnswerCard("pilot_reviewed");

        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "is this meningitis or a viral illness",
            List.of(card)
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and stiff neck",
            List.of()
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and stiff neck",
            List.of(card, card)
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and stiff neck",
            List.of(meningitisSepsisChildAnswerCard("draft"))
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and stiff neck",
            List.of(meningitisSepsisChildAnswerCard("approved", "wrong_meningitis_card", "GD-589", "critical", meningitisSources()))
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and stiff neck",
            List.of(meningitisSepsisChildAnswerCard("approved", "meningitis_sepsis_child", "GD-999", "critical", meningitisSources()))
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and stiff neck",
            List.of(meningitisSepsisChildAnswerCard("approved", "meningitis_sepsis_child", "GD-589", "routine", meningitisSources()))
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and stiff neck",
            List.of(meningitisSepsisChildAnswerCard("approved", "meningitis_sepsis_child", "GD-589", "critical", List.of()))
        ));
    }

    @Test
    public void infectedWoundSpreadingInfectionSelectorRequiresWoundAndDangerSigns() {
        assertTrue(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm"
        ));
        assertTrue(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "puncture wound in my foot is swollen hot and leaking pus"
        ));
        assertTrue(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "scrape looks infected and now I have fever and chills"
        ));
        assertTrue(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "wound redness is spreading past the line I marked"
        ));
        assertTrue(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "small cut smells bad and the skin is turning dark"
        ));
        assertTrue(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "bite wound is getting redder by the hour and hurts to move my hand"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "how do I clean a small scrape with no redness or pus"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "routine dressing change for a healing cut"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "deep puncture wound but no fever pus swelling or redness"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "what antibiotic should I stockpile for minor cuts"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "what is cellulitis in general"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "what are the signs of an infected wound"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "red rash on leg but no wound or injury"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "newborn cord has pus and spreading redness"
        ));
        assertFalse(AnswerCardRuntime.isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(
            "fever stiff neck and purple rash"
        ));
    }

    @Test
    public void infectedWoundSpreadingInfectionBuildsReviewedAnswerPlan() {
        AnswerCardRuntime.AnswerPlan plan =
            AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
                "wound redness is spreading past the line I marked",
                List.of(infectedWoundSpreadingInfectionAnswerCard("reviewed"))
            );

        assertNotNull(plan);
        assertEquals("answer_card:infected_wound_spreading_infection", plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals("infected_wound_spreading_infection", plan.reviewedCardMetadata.cardId);
        assertEquals("GD-585", plan.reviewedCardMetadata.cardGuideId);
        assertEquals("reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals("reviewed_source_family", plan.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, plan.reviewedCardMetadata.provenance);
        assertEquals(List.of("GD-585"), plan.reviewedCardMetadata.citedReviewedSourceGuideIds);
        assertEquals(1, plan.sources.size());
        assertEquals("GD-585", plan.sources.get(0).guideId);
        assertEquals("answer-card", plan.sources.get(0).retrievalMode);
        assertEquals("safety_infected_wound", plan.sources.get(0).structureType);
        assertTrue(plan.answerText.contains("Assess whether redness is spreading, mark its edge, and recheck every 2 hours."));
        assertTrue(plan.answerText.contains("Escalate urgently if red streaking"));
        assertTrue(plan.answerText.contains("For spreading infection, clean 2 to 3 times daily"));
        assertTrue(plan.answerText.contains("Avoid: Do not reassure rapidly spreading redness"));
    }

    @Test
    public void infectedWoundSpreadingInfectionRejectsAmbiguousIneligibleAndMissingSourceCards() {
        AnswerCard card = infectedWoundSpreadingInfectionAnswerCard("pilot_reviewed");

        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "routine dressing change for a healing cut",
            List.of(card)
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm",
            List.of()
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm",
            List.of(card, card)
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm",
            List.of(infectedWoundSpreadingInfectionAnswerCard("draft"))
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm",
            List.of(infectedWoundSpreadingInfectionAnswerCard("approved", "wrong_wound_card", "GD-585", "critical", infectedWoundSources()))
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm",
            List.of(infectedWoundSpreadingInfectionAnswerCard("approved", "infected_wound_spreading_infection", "GD-999", "critical", infectedWoundSources()))
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm",
            List.of(infectedWoundSpreadingInfectionAnswerCard("approved", "infected_wound_spreading_infection", "GD-585", "routine", infectedWoundSources()))
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "cut on my hand yesterday and now a red streak is moving up my arm",
            List.of(infectedWoundSpreadingInfectionAnswerCard("approved", "infected_wound_spreading_infection", "GD-585", "critical", List.of()))
        ));
    }

    @Test
    public void abdominalInternalBleedingSelectorRequiresTraumaOrGiBleedWithDanger() {
        assertTrue(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "bike handlebar hit his belly and now he is pale and dizzy"
        ));
        assertTrue(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "after a car crash the seatbelt bruise hurts with severe abdominal pain and vomiting"
        ));
        assertTrue(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "fell hard on my side and have severe flank pain with a rapid pulse"
        ));
        assertTrue(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "left side pain after handlebar injury and he looks faint"
        ));
        assertTrue(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "vomiting blood and feeling faint and weak"
        ));
        assertTrue(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "black tarry stool with dizziness and pale skin"
        ));
        assertTrue(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "rectal blood and severe belly pain with rapid pulse"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "pregnant with severe belly pain and dizziness after a fall"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "possible ectopic pregnancy with fainting and abdominal pain"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "stomach flu with vomiting and dizziness"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "reflux and mild cramps after dinner"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "constipation with black stool but no dizziness or weakness"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "hemorrhoids with rectal blood and dizziness"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "black stool after iron tablets but no danger signs"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "black tarry stool but no dizziness or weakness"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "fell on my side and now dizzy but no belly pain"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "generic surgical abdomen with guarding but no trauma bleeding or shock"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "child swallowed unknown cleaner and is pale"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "choking and cannot breathe"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "newborn is pale and vomiting"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "wound with red streak and rapid pulse"
        ));
        assertFalse(AnswerCardRuntime.isAbdominalInternalBleedingAnswerCardQueryForTest(
            "fever stiff neck and purple rash"
        ));
    }

    @Test
    public void abdominalInternalBleedingBuildsReviewedAnswerPlan() {
        AnswerCardRuntime.AnswerPlan plan =
            AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
                "bike handlebar hit his belly and now he is pale and dizzy",
                List.of(abdominalInternalBleedingAnswerCard("pilot_reviewed"))
            );

        assertNotNull(plan);
        assertEquals("answer_card:abdominal_internal_bleeding", plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals("abdominal_internal_bleeding", plan.reviewedCardMetadata.cardId);
        assertEquals("GD-380", plan.reviewedCardMetadata.cardGuideId);
        assertEquals("pilot_reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals("reviewed_source_family", plan.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, plan.reviewedCardMetadata.provenance);
        assertEquals(List.of("GD-380"), plan.reviewedCardMetadata.citedReviewedSourceGuideIds);
        assertEquals(1, plan.sources.size());
        assertEquals("GD-380", plan.sources.get(0).guideId);
        assertEquals("answer-card", plan.sources.get(0).retrievalMode);
        assertEquals("safety_internal_bleeding", plan.sources.get(0).structureType);
        assertTrue(plan.answerText.contains("Treat abdominal trauma or GI bleeding with shock signs as an emergency."));
        assertTrue(plan.answerText.contains("Keep the person still and lying down while getting emergency help."));
        assertTrue(plan.answerText.contains("Escalate now if pale, dizzy, faint, clammy, rapid pulse"));
        assertTrue(plan.answerText.contains("Avoid: Do not give food, drink, or pain medicine"));
    }

    @Test
    public void abdominalInternalBleedingRejectsAmbiguousIneligibleAndMissingSourceCards() {
        AnswerCard card = abdominalInternalBleedingAnswerCard("pilot_reviewed");

        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "black stool after iron tablets but no danger signs",
            List.of(card)
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of()
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of(card, card)
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of(abdominalInternalBleedingAnswerCard("draft"))
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of(abdominalInternalBleedingAnswerCard("approved", "wrong_abdominal_card", "GD-380", "critical", abdominalInternalBleedingSources()))
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of(abdominalInternalBleedingAnswerCard("approved", "abdominal_internal_bleeding", "GD-999", "critical", abdominalInternalBleedingSources()))
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of(abdominalInternalBleedingAnswerCard("approved", "abdominal_internal_bleeding", "GD-380", "routine", abdominalInternalBleedingSources()))
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of(abdominalInternalBleedingAnswerCard("approved", "abdominal_internal_bleeding", "GD-380", "critical", List.of()))
        ));
    }

    @Test
    public void foundryCastingAreaReadinessSelectorStaysOnBoundaryReadiness() {
        assertTrue(AnswerCardRuntime.isFoundryCastingAreaReadinessAnswerCardQueryForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work."
        ));
        assertTrue(AnswerCardRuntime.isFoundryCastingAreaReadinessAnswerCardQueryForTest(
            "What should we record about wet floors, cracked crucibles, unknown scrap, ventilation concerns, and owner handoff before casting?"
        ));

        assertFalse(AnswerCardRuntime.isFoundryCastingAreaReadinessAnswerCardQueryForTest(
            "Give me a bronze melt schedule and pouring temperature."
        ));
        assertFalse(AnswerCardRuntime.isFoundryCastingAreaReadinessAnswerCardQueryForTest(
            "Show me how to set up the furnace and tune the air blast."
        ));
        assertFalse(AnswerCardRuntime.isFoundryCastingAreaReadinessAnswerCardQueryForTest(
            "Design the gating and risers for my mold."
        ));
        assertFalse(AnswerCardRuntime.isFoundryCastingAreaReadinessAnswerCardQueryForTest(
            "General workshop organization checklist for labels and access control."
        ));
    }

    @Test
    public void foundryCastingAreaReadinessBuildsReviewedAnswerPlan() {
        AnswerCardRuntime.AnswerPlan plan =
            AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
                "What should we record about wet floors, cracked crucibles, unknown scrap, ventilation concerns, and owner handoff before casting?",
                List.of(foundryCastingAreaReadinessAnswerCard("pilot_reviewed"))
            );

        assertNotNull(plan);
        assertEquals("answer_card:foundry_casting_area_readiness_boundary", plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals("foundry_casting_area_readiness_boundary", plan.reviewedCardMetadata.cardId);
        assertEquals("GD-132", plan.reviewedCardMetadata.cardGuideId);
        assertEquals("pilot_reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals("reviewed_source_family", plan.reviewedCardMetadata.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, plan.reviewedCardMetadata.provenance);
        assertEquals(List.of("GD-132"), plan.reviewedCardMetadata.citedReviewedSourceGuideIds);
        assertEquals(1, plan.sources.size());
        assertEquals("GD-132", plan.sources.get(0).guideId);
        assertEquals("answer-card", plan.sources.get(0).retrievalMode);
        assertEquals("foundry_area_readiness", plan.sources.get(0).structureType);
        assertTrue(plan.answerText.contains("Start with boundary-only readiness"));
        assertTrue(plan.answerText.contains("Treat as a no-go or owner-handoff screen"));
        assertTrue(plan.answerText.contains("End with foundry owner"));
        assertTrue(plan.answerText.contains("Escalate now if Any moisture"));
        assertTrue(plan.answerText.contains("Avoid: Do not provide mold-making steps"));
    }

    @Test
    public void foundryCastingAreaReadinessRejectsAmbiguousIneligibleAndMissingSourceCards() {
        AnswerCard card = foundryCastingAreaReadinessAnswerCard("pilot_reviewed");

        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Give me a bronze melt schedule and pouring temperature.",
            List.of(card)
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of()
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of(card, card)
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of(foundryCastingAreaReadinessAnswerCard("draft"))
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of(foundryCastingAreaReadinessAnswerCard("approved", "wrong_foundry_card", "GD-132", "high", foundryCastingAreaReadinessSources()))
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of(foundryCastingAreaReadinessAnswerCard("approved", "foundry_casting_area_readiness_boundary", "GD-999", "high", foundryCastingAreaReadinessSources()))
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of(foundryCastingAreaReadinessAnswerCard("approved", "foundry_casting_area_readiness_boundary", "GD-132", "routine", foundryCastingAreaReadinessSources()))
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of(foundryCastingAreaReadinessAnswerCard("approved", "foundry_casting_area_readiness_boundary", "GD-132", "high", List.of()))
        ));
    }

    @Test
    public void syntheticHighCriticalNonPilotCardsDoNotBypassPlannerAllowlist() {
        AnswerCard highNonPilotCard = temptingNonPilotAnswerCard("non_pilot_high_card", "GD-585", "high");
        AnswerCard criticalNonPilotCard = temptingNonPilotAnswerCard("non_pilot_critical_card", "GD-898", "critical");

        assertNull(AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
            "my child swallowed an unknown cleaner",
            List.of(criticalNonPilotCard)
        ));
        assertNull(AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
            "3 day old baby has fever and fast breathing",
            List.of(criticalNonPilotCard)
        ));
        assertNull(AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
            "child is choking on a grape and cannot speak",
            List.of(criticalNonPilotCard)
        ));
        assertNull(AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
            "child has fever and a stiff neck",
            List.of(criticalNonPilotCard)
        ));
        assertNull(AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
            "wound has spreading redness and fever",
            List.of(highNonPilotCard)
        ));
        assertNull(AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
            "bike handlebar hit his belly and now he is pale and dizzy",
            List.of(criticalNonPilotCard)
        ));
        assertNull(AnswerCardRuntime.planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work.",
            List.of(highNonPilotCard)
        ));
    }

    private static AnswerCard poisoningAnswerCard(String reviewStatus) {
        return poisoningAnswerCard(
            reviewStatus,
            List.of(new AnswerCardSource(
                "poisoning_unknown_ingestion",
                "GD-898",
                "poisoning",
                "Poisoning",
                List.of("Triage"),
                true
            ))
        );
    }

    private static AnswerCard poisoningAnswerCard(String reviewStatus, List<AnswerCardSource> sources) {
        return new AnswerCard(
            "poisoning_unknown_ingestion",
            "GD-898",
            "poisoning_unknown_ingestion",
            "Poisoning / Unknown Ingestion",
            "critical",
            "guide-corpus",
            reviewStatus,
            "reviewed_source_family",
            "Do not wait for symptoms after an unknown ingestion.",
            "Say if the substance is unknown.",
            "Synthetic reviewed-card test fixture.",
            List.of(
                clause(
                    "poisoning_unknown_ingestion",
                    "required_first_action",
                    "Call poison control, EMS, or the fastest clinician now."
                ),
                clause(
                    "poisoning_unknown_ingestion",
                    "conditional_required_action",
                    "Keep the child with an adult.",
                    "child",
                    "kid"
                ),
                clause("poisoning_unknown_ingestion", "first_action", "Check airway and breathing first."),
                clause(
                    "poisoning_unknown_ingestion",
                    "urgent_red_flag",
                    "the person is sleepy, confused, or having trouble breathing."
                ),
                clause("poisoning_unknown_ingestion", "forbidden_advice", "Do not induce vomiting."),
                clause("poisoning_unknown_ingestion", "do_not", "Do not taste the substance.")
            ),
            sources
        );
    }

    private static AnswerCard temptingNonPilotAnswerCard(String cardId, String guideId, String riskTier) {
        return new AnswerCard(
            cardId,
            guideId,
            "synthetic-non-pilot",
            "Synthetic non-pilot card",
            riskTier,
            "guide-corpus",
            "approved",
            "reviewed_source_family",
            "Tempting synthetic reviewed-card fixture.",
            "This card should never be selected by planner hooks unless explicitly allowlisted.",
            "Synthetic non-pilot reviewed-card test fixture.",
            List.of(
                clause(cardId, "required_first_action", "Seek emergency care now."),
                clause(cardId, "first_action", "Keep the person observed while getting help."),
                clause(cardId, "urgent_red_flag", "danger signs are present."),
                clause(cardId, "forbidden_advice", "Do not delay care.")
            ),
            List.of(new AnswerCardSource(
                cardId,
                guideId,
                "synthetic-non-pilot",
                "Synthetic Non-Pilot Card",
                List.of("Emergency escalation"),
                true
            ))
        );
    }

    private static AnswerCard newbornDangerSepsisAnswerCard(String reviewStatus) {
        return newbornDangerSepsisAnswerCard(
            reviewStatus,
            "newborn_danger_sepsis",
            "GD-284",
            "critical",
            newbornSources()
        );
    }

    private static AnswerCard newbornDangerSepsisAnswerCard(
        String reviewStatus,
        String cardId,
        String guideId,
        String riskTier,
        List<AnswerCardSource> sources
    ) {
        return new AnswerCard(
            cardId,
            guideId,
            "newborn_danger_sepsis",
            "Newborn danger signs / possible sepsis",
            riskTier,
            "guide-corpus",
            reviewStatus,
            "reviewed_source_family",
            "Routine newborn care does not use this card without danger signs.",
            "Use when newborn-age wording and danger signs are both present.",
            "Synthetic newborn reviewed-card test fixture.",
            List.of(
                clause(cardId, "required_first_action", "Seek emergency care now."),
                clause(
                    cardId,
                    "conditional_required_action",
                    "Keep the cord area clean and uncovered while getting urgent care.",
                    "cord",
                    "umbilical",
                    "belly button"
                ),
                clause(cardId, "first_action", "Keep the newborn warm and close to a caregiver."),
                clause(cardId, "urgent_red_flag", "the newborn is limp, not feeding, or hard to wake."),
                clause(cardId, "forbidden_advice", "Do not wait to see if symptoms improve."),
                clause(cardId, "do_not", "Do not give leftover medicine.")
            ),
            sources
        );
    }

    private static List<AnswerCardSource> newbornSources() {
        return List.of(new AnswerCardSource(
            "newborn_danger_sepsis",
            "GD-284",
            "newborn_danger_sepsis",
            "Newborn Danger Signs",
            List.of("Danger signs"),
            true
        ));
    }

    private static AnswerCard chokingAirwayObstructionAnswerCard(String reviewStatus) {
        return chokingAirwayObstructionAnswerCard(
            reviewStatus,
            "choking_airway_obstruction",
            "GD-232",
            "critical",
            chokingSources()
        );
    }

    private static AnswerCard chokingAirwayObstructionAnswerCard(
        String reviewStatus,
        String cardId,
        String guideId,
        String riskTier,
        List<AnswerCardSource> sources
    ) {
        return new AnswerCard(
            cardId,
            guideId,
            "first-aid",
            "First Aid & Emergency Response",
            riskTier,
            "guide-corpus",
            reviewStatus,
            "",
            "Any inability to speak, cough, breathe, cry, or any cyanosis/collapse is an airway emergency.",
            "Use when choking context and complete-obstruction signs are present.",
            "Synthetic choking reviewed-card test fixture.",
            List.of(
                clause(
                    cardId,
                    "required_first_action",
                    "If the person is coughing and talking, keep them upright, encourage coughing, and watch closely."
                ),
                clause(
                    cardId,
                    "required_first_action",
                    "If the person cannot speak, cough, or breathe, call for emergency help and start age-appropriate choking rescue."
                ),
                clause(
                    cardId,
                    "required_first_action",
                    "If the choking person collapses, lower them to the floor, call for help/AED, start CPR, and remove only a clearly visible object."
                ),
                clause(
                    cardId,
                    "conditional_required_action",
                    "If an adult or older child cannot speak, cough, or breathe, call for emergency help and start abdominal thrusts.",
                    "adult",
                    "older child",
                    "teen",
                    "teenager"
                ),
                clause(
                    cardId,
                    "conditional_required_action",
                    "If an infant cannot cry, cough, or breathe, use 5 back blows followed by 5 chest thrusts.",
                    "infant",
                    "baby",
                    "newborn",
                    "under 1",
                    "under one"
                ),
                clause(
                    cardId,
                    "first_action",
                    "For a conscious adult or older child with complete obstruction, stand behind them and repeat quick upward abdominal thrusts until the object clears or they become unconscious."
                ),
                clause(cardId, "urgent_red_flag", "Cannot speak, cough, or breathe."),
                clause(cardId, "urgent_red_flag", "Blue lips or cyanosis."),
                clause(cardId, "forbidden_advice", "Do not do blind finger sweeps."),
                clause(cardId, "forbidden_advice", "Do not perform abdominal thrusts on infants."),
                clause(cardId, "do_not", "Do not put fingers into the mouth unless the object is clearly visible.")
            ),
            sources
        );
    }

    private static List<AnswerCardSource> chokingSources() {
        return List.of(
            new AnswerCardSource(
                "choking_airway_obstruction",
                "GD-232",
                "first-aid",
                "First Aid & Emergency Response",
                List.of("#choking", "#choking > Choking Response", "#choking > Infant Choking"),
                true
            ),
            new AnswerCardSource(
                "choking_airway_obstruction",
                "GD-284",
                "infant-child-care",
                "Infant & Child Care (Birth to Age 5)",
                List.of("Safety & Injury Prevention > Suffocation & Choking Prevention"),
                false
            ),
            new AnswerCardSource(
                "choking_airway_obstruction",
                "GD-298",
                "pediatric-emergency-medicine",
                "Pediatric Emergency Medicine",
                List.of("#airway-anatomy", "Foreign Body Airway Obstruction"),
                false
            ),
            new AnswerCardSource(
                "choking_airway_obstruction",
                "GD-617",
                "pediatric-emergencies-field",
                "Pediatric Emergencies: Sepsis, Dehydration & Respiratory Distress",
                List.of("Airway Obstruction / Choking", "Respiratory Distress Assessment"),
                false
            )
        );
    }

    private static AnswerCard meningitisSepsisChildAnswerCard(String reviewStatus) {
        return meningitisSepsisChildAnswerCard(
            reviewStatus,
            "meningitis_sepsis_child",
            "GD-589",
            "critical",
            meningitisSources()
        );
    }

    private static AnswerCard meningitisSepsisChildAnswerCard(
        String reviewStatus,
        String cardId,
        String guideId,
        String riskTier,
        List<AnswerCardSource> sources
    ) {
        return new AnswerCard(
            cardId,
            guideId,
            "meningitis-sepsis",
            "Meningitis and sepsis red flags in children",
            riskTier,
            "guide-corpus",
            reviewStatus,
            "reviewed_source_family",
            "Fever with stiff neck, non-blanching rash, confusion, hard-to-wake sleepiness, or shock signs needs emergency care.",
            "Use only for red-flag clinical prompts, not public-health or comparison prompts.",
            "Synthetic meningitis/sepsis reviewed-card test fixture.",
            List.of(
                clause(cardId, "required_first_action", "Seek emergency care now."),
                clause(
                    cardId,
                    "conditional_required_action",
                    "Treat purple or non-blanching rash with fever as an emergency.",
                    "rash",
                    "purple",
                    "non blanching",
                    "nonblanching",
                    "petechial",
                    "little purple dots"
                ),
                clause(cardId, "first_action", "Keep the child lying comfortably and observed by an adult."),
                clause(cardId, "urgent_red_flag", "confusion, hard-to-wake sleepiness, or collapse is present."),
                clause(cardId, "forbidden_advice", "Do not give leftover antibiotics."),
                clause(cardId, "do_not", "Do not wait for a rash to appear before seeking help.")
            ),
            sources
        );
    }

    private static List<AnswerCardSource> meningitisSources() {
        return List.of(
            new AnswerCardSource(
                "meningitis_sepsis_child",
                "GD-589",
                "meningitis-sepsis",
                "Meningitis and Sepsis in Children",
                List.of("Red flags", "Emergency escalation"),
                true
            ),
            new AnswerCardSource(
                "meningitis_sepsis_child",
                "GD-235",
                "fever-rash",
                "Fever and Rash",
                List.of("Non-blanching rash", "Urgent assessment"),
                false
            ),
            new AnswerCardSource(
                "meningitis_sepsis_child",
                "GD-268",
                "neurologic-emergency",
                "Neurologic Emergency Signs",
                List.of("Altered mental status", "Severe headache"),
                false
            ),
            new AnswerCardSource(
                "meningitis_sepsis_child",
                "GD-284",
                "infant-child-care",
                "Infant & Child Care (Birth to Age 5)",
                List.of("When to seek urgent care"),
                false
            ),
            new AnswerCardSource(
                "meningitis_sepsis_child",
                "GD-298",
                "pediatric-emergency-medicine",
                "Pediatric Emergency Medicine",
                List.of("Shock", "Sepsis recognition"),
                false
            )
        );
    }

    private static AnswerCard infectedWoundSpreadingInfectionAnswerCard(String reviewStatus) {
        return infectedWoundSpreadingInfectionAnswerCard(
            reviewStatus,
            "infected_wound_spreading_infection",
            "GD-585",
            "high",
            infectedWoundSources()
        );
    }

    private static AnswerCard infectedWoundSpreadingInfectionAnswerCard(
        String reviewStatus,
        String cardId,
        String guideId,
        String riskTier,
        List<AnswerCardSource> sources
    ) {
        return new AnswerCard(
            cardId,
            guideId,
            "infected-wound",
            "Infected wound with spreading infection",
            riskTier,
            "guide-corpus",
            reviewStatus,
            "reviewed_source_family",
            "Spreading redness, red streaking, pus, foul odor, dark skin, or systemic symptoms after a wound need urgent escalation.",
            "Use only when wound context and spreading/local/systemic infection danger signs are present.",
            "Synthetic infected wound reviewed-card test fixture.",
            List.of(
                clause(cardId, "required_first_action", "Assess whether redness is spreading, mark its edge, and recheck every 2 hours."),
                clause(cardId, "first_action", "For spreading infection, clean 2 to 3 times daily and keep the area covered."),
                clause(cardId, "urgent_red_flag", "Escalate urgently if red streaking, fever, chills, or rapidly spreading redness is present."),
                clause(cardId, "forbidden_advice", "Do not reassure rapidly spreading redness as routine healing."),
                clause(cardId, "do_not", "Do not delay urgent care for dark, black, or foul-smelling skin.")
            ),
            sources
        );
    }

    private static List<AnswerCardSource> infectedWoundSources() {
        return List.of(new AnswerCardSource(
            "infected_wound_spreading_infection",
            "GD-585",
            "infected-wound",
            "Infected Wound and Spreading Infection",
            List.of("Escalation", "Wound infection red flags"),
            true
        ));
    }

    private static AnswerCard abdominalInternalBleedingAnswerCard(String reviewStatus) {
        return abdominalInternalBleedingAnswerCard(
            reviewStatus,
            "abdominal_internal_bleeding",
            "GD-380",
            "critical",
            abdominalInternalBleedingSources()
        );
    }

    private static AnswerCard abdominalInternalBleedingAnswerCard(
        String reviewStatus,
        String cardId,
        String guideId,
        String riskTier,
        List<AnswerCardSource> sources
    ) {
        return new AnswerCard(
            cardId,
            guideId,
            "abdominal-internal-bleeding",
            "Abdominal trauma or GI bleeding with shock danger signs",
            riskTier,
            "guide-corpus",
            reviewStatus,
            "reviewed_source_family",
            "Do not route abdominal trauma with shock signs or GI bleeding with danger signs to routine GI care.",
            "Use only when query text names abdominal impact or GI bleeding plus danger signs.",
            "Synthetic abdominal internal bleeding reviewed-card test fixture.",
            List.of(
                clause(cardId, "required_first_action", "Treat abdominal trauma or GI bleeding with shock signs as an emergency."),
                clause(cardId, "first_action", "Keep the person still and lying down while getting emergency help."),
                clause(cardId, "urgent_red_flag", "pale, dizzy, faint, clammy, rapid pulse, vomiting, rigid abdomen, or severe belly pain is present."),
                clause(cardId, "forbidden_advice", "Do not give food, drink, or pain medicine when internal bleeding is possible."),
                clause(cardId, "do_not", "Do not delay emergency care for black tarry stool or vomiting blood with shock signs.")
            ),
            sources
        );
    }

    private static List<AnswerCardSource> abdominalInternalBleedingSources() {
        return List.of(new AnswerCardSource(
            "abdominal_internal_bleeding",
            "GD-380",
            "abdominal-internal-bleeding",
            "Abdominal Internal Bleeding",
            List.of("Emergency escalation", "Abdominal trauma and GI bleed danger signs"),
            true
        ));
    }

    private static AnswerCard foundryCastingAreaReadinessAnswerCard(String reviewStatus) {
        return foundryCastingAreaReadinessAnswerCard(
            reviewStatus,
            "foundry_casting_area_readiness_boundary",
            "GD-132",
            "high",
            foundryCastingAreaReadinessSources()
        );
    }

    private static AnswerCard foundryCastingAreaReadinessAnswerCard(
        String reviewStatus,
        String cardId,
        String guideId,
        String riskTier,
        List<AnswerCardSource> sources
    ) {
        return new AnswerCard(
            cardId,
            guideId,
            "foundry-casting",
            "Foundry casting area readiness boundary",
            riskTier,
            "guide-corpus",
            reviewStatus,
            "reviewed_source_family",
            "Foundry-area readiness is limited to visible hazard screening, labels, no-go triggers, access control, and owner handoff.",
            "Use only for boundary readiness logs, not casting procedure, furnace setup, recipes, calculations, or certification.",
            "Synthetic GD-132 reviewed-card test fixture.",
            List.of(
                clause(cardId, "required_first_action", "Start with boundary-only readiness and non-invasive observation; do not provide casting procedure, setup, recipe, schedule, temperature, calculation, or safety-certification advice."),
                clause(cardId, "required_first_action", "Identify the activity status, foundry-area owner, responsible operator, access control, material/source labels, visible hazards, and whether work is already paused."),
                clause(cardId, "conditional_required_action", "Treat as a no-go or owner-handoff screen, not routine readiness.", "water", "wet", "cracked crucible", "unknown scrap", "ventilation concerns"),
                clause(cardId, "first_action", "Make a foundry-area inventory with location, owner or responsible operator, current activity status, access status, date, observer, and whether work is paused."),
                clause(cardId, "first_action", "End with foundry owner, experienced operator, fire-safety owner, chemical-safety owner, emergency service, or local-authority routing whenever hazards, labels, ownership, readiness, or authority are uncertain."),
                clause(cardId, "urgent_red_flag", "Any moisture, wet mold or tool uncertainty, wet floor, rain exposure, water near molten-metal work, or pressure to proceed despite dryness uncertainty."),
                clause(cardId, "forbidden_advice", "Do not provide mold-making steps, sand preparation, sand chemistry, investment casting steps, core making, pattern changes, or mold-drying procedures."),
                clause(cardId, "do_not", "Do not convert the source guide's furnace, crucible, mold, metal, pouring, defect, or finishing passages into reviewed-card instructions.")
            ),
            sources
        );
    }

    private static List<AnswerCardSource> foundryCastingAreaReadinessSources() {
        return List.of(new AnswerCardSource(
            "foundry_casting_area_readiness_boundary",
            "GD-132",
            "foundry-casting",
            "Foundry & Metal Casting",
            List.of("Reviewed Answer-Card Boundary", "Foundry Safety Quickstart", "Emergency Procedures"),
            true
        ));
    }

    private static AnswerCardClause clause(String cardId, String kind, String text, String... triggerTerms) {
        return new AnswerCardClause(
            cardId,
            kind,
            1,
            text,
            List.of(triggerTerms)
        );
    }
}

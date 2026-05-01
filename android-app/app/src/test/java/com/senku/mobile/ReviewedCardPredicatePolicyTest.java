package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ReviewedCardPredicatePolicyTest {
    @Test
    public void poisoningAnswerCardPilotQueryRequiresActionAndUnknownObject() {
        assertTrue(ReviewedCardPredicatePolicy.isPoisoningAnswerCardPilotQuery(
            "my child swallowed an unknown cleaner"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isPoisoningAnswerCardPilotQuery(
            "someone drank bleach from an unlabeled bottle"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isPoisoningAnswerCardPilotQuery(
            "how should i store bleach under the sink"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isPoisoningAnswerCardPilotQuery(
            "i swallowed a grape"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isPoisoningAnswerCardPilotQuery(
            "can i drink water from a clean bottle"
        ));
    }

    @Test
    public void newbornDangerSepsisSelectorRequiresNewbornAgeAndDangerSign() {
        assertTrue(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "newborn is limp, will not feed, and is hard to wake"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "3 day old baby has fever and fast breathing"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "4 week old infant has low temperature and no wet diaper"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "newborn cord has pus and spreading redness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "routine newborn care and sleep schedule"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "older child has fever and fast breathing"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "adult fever and trouble breathing"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "clean newborn cord care without redness or pus"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "5 week old baby has fever and fast breathing"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isNewbornDangerSepsisAnswerCardQuery(
            "28 week old baby has fever and fast breathing"
        ));
    }

    @Test
    public void chokingAirwayObstructionSelectorRequiresChokingContextAndActionSignal() {
        assertTrue(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("food stuck and they cannot speak"));
        assertTrue(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("child is choking on a grape and turning blue"));
        assertTrue(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("infant choking and cannot cry"));
        assertTrue(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("should I do back blows or Heimlich first"));
        assertFalse(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("how do I prevent choking hazards in a toddler play area"));
        assertFalse(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("panic attack and hyperventilating but can talk and breathe"));
        assertFalse(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("I feel like I am choking during a panic attack but I can talk and cough"));
        assertFalse(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("throat closing with hives after a bee sting"));
        assertFalse(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("child swallowed unknown cleaner and is coughing"));
        assertFalse(ReviewedCardPredicatePolicy.isChokingAirwayObstructionAnswerCardQuery("toddler choked on a peanut then stopped choking but now keeps coughing and wheezing"));
    }

    @Test
    public void meningitisSepsisChildSelectorRequiresRedFlagClinicalPrompt() {
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "child has fever and a stiff neck"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "fever with stiff neck: meningitis vs viral illness?"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "fever with little purple dots and a non-blanching rash"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "child has high fever and the rash looks bruise-like"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "kid has fever, confusion, and unusual sleepiness"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "fever with severe headache, photophobia, vomiting, and neck stiffness"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "sore all over with fever and a spreading purplish rash and now acting confused"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "child is hard to wake and has a petechial rash"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "could this be sepsis, very sick with fever and cold hands"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "is this meningitis or a viral illness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "meningitis vs viral illness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "could this be meningitis or just a virus"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "is this sepsis or the flu"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "meningitis outbreak reporting and contact tracing quarantine rules"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "routine rash and fever but acting normal"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "seatbelt bruise with neck pain after a crash"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "what is meningitis"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isMeningitisSepsisChildAnswerCardQuery(
            "sepsis"
        ));
    }

    @Test
    public void infectedWoundSpreadingInfectionSelectorRequiresWoundAndDangerSigns() {
        assertTrue(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "cut on my hand yesterday and now a red streak is moving up my arm"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "puncture wound in my foot is swollen hot and leaking pus"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "scrape looks infected and now I have fever and chills"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "wound redness is spreading past the line I marked"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "small cut smells bad and the skin is turning dark"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "bite wound is getting redder by the hour and hurts to move my hand"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "how do I clean a small scrape with no redness or pus"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "routine dressing change for a healing cut"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "deep puncture wound but no fever pus swelling or redness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "what antibiotic should I stockpile for minor cuts"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "what is cellulitis in general"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "what are the signs of an infected wound"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "red rash on leg but no wound or injury"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "newborn cord has pus and spreading redness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isInfectedWoundSpreadingInfectionAnswerCardQuery(
            "fever stiff neck and purple rash"
        ));
    }

    @Test
    public void abdominalInternalBleedingSelectorRequiresTraumaOrGiBleedWithDanger() {
        assertTrue(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "bike handlebar hit his belly and now he is pale and dizzy"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "after a car crash the seatbelt bruise hurts with severe abdominal pain and vomiting"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "fell hard on my side and have severe flank pain with a rapid pulse"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "left side pain after handlebar injury and he looks faint"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "vomiting blood and feeling faint and weak"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "black tarry stool with dizziness and pale skin"
        ));
        assertTrue(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "rectal blood and severe belly pain with rapid pulse"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "pregnant with severe belly pain and dizziness after a fall"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "possible ectopic pregnancy with fainting and abdominal pain"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "stomach flu with vomiting and dizziness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "reflux and mild cramps after dinner"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "constipation with black stool but no dizziness or weakness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "hemorrhoids with rectal blood and dizziness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "black stool after iron tablets but no danger signs"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "black tarry stool but no dizziness or weakness"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "fell on my side and now dizzy but no belly pain"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "generic surgical abdomen with guarding but no trauma bleeding or shock"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "child swallowed unknown cleaner and is pale"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "choking and cannot breathe"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "newborn is pale and vomiting"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "wound with red streak and rapid pulse"
        ));
        assertFalse(ReviewedCardPredicatePolicy.isAbdominalInternalBleedingAnswerCardQuery(
            "fever stiff neck and purple rash"
        ));
    }

    @Test
    public void foundryCastingAreaReadinessSelectorStaysOnBoundaryReadiness() {
        assertTrue(ReviewedCardPredicatePolicy.isFoundryCastingAreaReadinessAnswerCardQuery(
            "Make a pre-work foundry readiness log for visible hazards, labels, access control, and who can pause work."
        ));
        assertTrue(ReviewedCardPredicatePolicy.isFoundryCastingAreaReadinessAnswerCardQuery(
            "What should we record about wet floors, cracked crucibles, unknown scrap, ventilation concerns, and owner handoff before casting?"
        ));

        assertFalse(ReviewedCardPredicatePolicy.isFoundryCastingAreaReadinessAnswerCardQuery(
            "Give me a bronze melt schedule and pouring temperature."
        ));
        assertFalse(ReviewedCardPredicatePolicy.isFoundryCastingAreaReadinessAnswerCardQuery(
            "Show me how to set up the furnace and tune the air blast."
        ));
        assertFalse(ReviewedCardPredicatePolicy.isFoundryCastingAreaReadinessAnswerCardQuery(
            "Design the gating and risers for my mold."
        ));
        assertFalse(ReviewedCardPredicatePolicy.isFoundryCastingAreaReadinessAnswerCardQuery(
            "General workshop organization checklist for labels and access control."
        ));
    }
}

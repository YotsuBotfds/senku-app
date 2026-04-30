package com.senku.mobile;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

final class AnswerCardRuntime {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final String STRUCTURE_TYPE_SAFETY_POISONING = "safety_poisoning";
    private static final String STRUCTURE_TYPE_SAFETY_NEWBORN_DANGER = "safety_newborn_danger";
    private static final String STRUCTURE_TYPE_SAFETY_AIRWAY_OBSTRUCTION = "safety_airway_obstruction";
    private static final String STRUCTURE_TYPE_SAFETY_MENINGITIS_SEPSIS = "safety_meningitis_sepsis";
    private static final String STRUCTURE_TYPE_SAFETY_INFECTED_WOUND = "safety_infected_wound";
    private static final String STRUCTURE_TYPE_SAFETY_INTERNAL_BLEEDING = "safety_internal_bleeding";
    private static final String STRUCTURE_TYPE_FOUNDRY_AREA_READINESS = "foundry_area_readiness";
    private static final String ANSWER_CARD_RULE_PREFIX = "answer_card:";
    private static final String POISONING_UNKNOWN_INGESTION_CARD_ID = "poisoning_unknown_ingestion";
    private static final String POISONING_UNKNOWN_INGESTION_GUIDE_ID = "GD-898";
    private static final String NEWBORN_DANGER_SEPSIS_CARD_ID = "newborn_danger_sepsis";
    private static final String NEWBORN_DANGER_SEPSIS_GUIDE_ID = "GD-284";
    private static final String CHOKING_AIRWAY_OBSTRUCTION_CARD_ID = "choking_airway_obstruction";
    private static final String CHOKING_AIRWAY_OBSTRUCTION_GUIDE_ID = "GD-232";
    private static final String MENINGITIS_SEPSIS_CHILD_CARD_ID = "meningitis_sepsis_child";
    private static final String MENINGITIS_SEPSIS_CHILD_GUIDE_ID = "GD-589";
    private static final String INFECTED_WOUND_SPREADING_INFECTION_CARD_ID = "infected_wound_spreading_infection";
    private static final String INFECTED_WOUND_SPREADING_INFECTION_GUIDE_ID = "GD-585";
    private static final String ABDOMINAL_INTERNAL_BLEEDING_CARD_ID = "abdominal_internal_bleeding";
    private static final String ABDOMINAL_INTERNAL_BLEEDING_GUIDE_ID = "GD-380";
    private static final String FOUNDRY_CASTING_AREA_READINESS_CARD_ID = "foundry_casting_area_readiness_boundary";
    private static final String FOUNDRY_CASTING_AREA_READINESS_GUIDE_ID = "GD-132";
    private static final Pattern NEWBORN_AGE_PATTERN = Pattern.compile(
        "\\b(?:(?:new\\s*born|newborn|neonate|neonatal)|(?:(?:\\d{1,2}|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|twenty\\s+one|twenty\\s+two|twenty\\s+three|twenty\\s+four|twenty\\s+five|twenty\\s+six|twenty\\s+seven|twenty\\s+eight)\\s+(?:day|days)\\s+old\\s+(?:baby|infant))|(?:(?:1|2|3|4|one|two|three|four)\\s+(?:week|weeks)\\s+old\\s+(?:baby|infant)))\\b"
    );
    private static final Set<String> POISONING_PILOT_ACTION_MARKERS = buildSet(
        "swallowed",
        "swallow",
        "ingested",
        "ingestion",
        "ate",
        "eaten",
        "drank",
        "drink",
        "licked",
        "took",
        "got into"
    );
    private static final Set<String> POISONING_PILOT_OBJECT_MARKERS = buildSet(
        "unknown",
        "unlabeled",
        "unlabelled",
        "cleaner",
        "chemical",
        "pill",
        "medication",
        "medicine",
        "poison",
        "under sink",
        "detergent",
        "pod",
        "bleach"
    );
    private static final Set<String> NEWBORN_DANGER_MARKERS = buildSet(
        "limp",
        "floppy",
        "will not feed",
        "won't feed",
        "not feeding",
        "refuses to feed",
        "hard to wake",
        "difficult to wake",
        "unresponsive",
        "fever",
        "low temperature",
        "hypothermia",
        "fast breathing",
        "trouble breathing",
        "breathing fast",
        "seizure",
        "jerking",
        "green vomiting",
        "bilious",
        "no urine",
        "not peeing",
        "no wet diaper",
        "cord has pus",
        "pus",
        "spreading redness",
        "redness spreading",
        "cord redness",
        "cord swelling",
        "foul odor"
    );
    private static final Set<String> CHOKING_CONTEXT_MARKERS = buildSet(
        "choking",
        "choked on",
        "choking on",
        "food stuck",
        "food bolus",
        "stuck in the throat",
        "swallowed wrong",
        "food went down wrong",
        "after a bite",
        "bite of food",
        "after one bite",
        "one bite",
        "back blows",
        "heimlich",
        "abdominal thrust",
        "airway obstruction",
        "foreign body airway obstruction"
    );
    private static final Set<String> CHOKING_HIGH_RISK_MARKERS = buildSet(
        "cannot speak",
        "can't speak",
        "cant speak",
        "unable to speak",
        "cannot talk",
        "can't talk",
        "cant talk",
        "unable to talk",
        "no words",
        "cannot cough",
        "can't cough",
        "cant cough",
        "cannot breathe",
        "can't breathe",
        "cant breathe",
        "unable to breathe",
        "turning blue",
        "blue lips",
        "cyanosis",
        "clutching throat",
        "clutching his throat",
        "clutching her throat",
        "clutching their throat",
        "weak cough",
        "silent cough",
        "drooling",
        "cannot swallow",
        "can't swallow",
        "cant swallow",
        "collapsed",
        "collapse",
        "unresponsive",
        "cannot cry",
        "can't cry",
        "cant cry"
    );
    private static final Set<String> CHOKING_HELP_MARKERS = buildSet(
        "help",
        "what do i do",
        "what should i do",
        "first move",
        "first step",
        "call now",
        "start cpr",
        "dont know the heimlich",
        "don't know the heimlich",
        "back blows or heimlich",
        "heimlich first"
    );
    private static final Set<String> CHOKING_ALLERGY_DRIFT_MARKERS = buildSet(
        "allergy",
        "allergic",
        "allergic reaction",
        "anaphylaxis",
        "epinephrine",
        "epi pen",
        "epipen",
        "hives",
        "bee sting",
        "wasp sting",
        "hornet sting",
        "stung",
        "new medicine",
        "medicine reaction",
        "throat tight",
        "throat feels tight",
        "throat closing",
        "tongue swelling",
        "lip swelling",
        "lips swelling",
        "face swelling",
        "facial swelling"
    );
    private static final Set<String> CHOKING_POISONING_DRIFT_MARKERS = buildSet(
        "poison",
        "toxin",
        "unknown substance",
        "unknown cleaner",
        "unlabeled bottle",
        "unlabelled bottle",
        "cleaner",
        "chemical",
        "pills",
        "medication",
        "medicine",
        "detergent",
        "bleach",
        "fuel",
        "gasoline",
        "kerosene",
        "hydrocarbon"
    );
    private static final Set<String> CHOKING_PANIC_DRIFT_MARKERS = buildSet(
        "panic attack",
        "anxiety attack",
        "hyperventilating",
        "hyperventilation"
    );
    private static final Set<String> MENINGITIS_SEPSIS_PUBLIC_HEALTH_DRIFT_MARKERS = buildSet(
        "outbreak",
        "reporting",
        "reportable",
        "contact tracing",
        "quarantine",
        "isolation policy",
        "public health",
        "school notification",
        "notify the health department"
    );
    private static final Set<String> MENINGITIS_SEPSIS_COMPARISON_DRIFT_MARKERS = buildSet(
        "meningitis or a viral illness",
        "meningitis vs viral illness",
        "meningitis or just a virus",
        "sepsis or the flu",
        "compare meningitis",
        "difference between meningitis",
        "viral illness vs meningitis",
        "virus vs meningitis",
        "flu vs sepsis"
    );
    private static final Set<String> MENINGITIS_SEPSIS_FEVER_MARKERS = buildSet(
        "fever",
        "febrile",
        "high temp",
        "temperature"
    );
    private static final Set<String> MENINGITIS_SEPSIS_STIFF_NECK_MARKERS = buildSet(
        "stiff neck",
        "rigid neck",
        "neck stiffness",
        "neck is stiff",
        "neck feels stiff",
        "cannot bend neck",
        "can't bend neck",
        "cant bend neck"
    );
    private static final Set<String> MENINGITIS_SEPSIS_RASH_MARKERS = buildSet(
        "non blanching rash",
        "nonblanching rash",
        "non-blanching rash",
        "purple rash",
        "dark rash",
        "bruise like rash",
        "bruise-like rash",
        "petechial rash",
        "petechiae",
        "little purple dots",
        "tiny purple dots",
        "purplish rash",
        "purple spots",
        "dark spots",
        "rash looks bruise-like",
        "rash looks bruise like",
        "does not fade when pressed",
        "do not fade when pressed",
        "doesn't fade when pressed"
    );
    private static final Set<String> MENINGITIS_SEPSIS_MENTAL_STATUS_MARKERS = buildSet(
        "confusion",
        "confused",
        "altered mental status",
        "unusual sleepiness",
        "very sleepy",
        "hard to wake",
        "difficult to wake",
        "can't wake",
        "cant wake",
        "not waking",
        "unresponsive",
        "lethargic"
    );
    private static final Set<String> MENINGITIS_SEPSIS_HEADACHE_CLUSTER_MARKERS = buildSet(
        "severe headache",
        "worst headache",
        "photophobia",
        "light hurts",
        "sensitive to light",
        "vomiting",
        "throwing up",
        "neck stiffness",
        "stiff neck"
    );
    private static final Set<String> MENINGITIS_SEPSIS_INFECTION_MARKERS = buildSet(
        "infection",
        "fever",
        "febrile",
        "high temp",
        "temperature",
        "rash",
        "vomiting",
        "not drinking",
        "poor intake"
    );
    private static final Set<String> MENINGITIS_SEPSIS_SHOCK_MARKERS = buildSet(
        "sepsis",
        "septic",
        "shock",
        "very sick",
        "looks very sick",
        "extremely sick",
        "toxic appearing",
        "cold hands",
        "cold feet",
        "mottled",
        "blue lips",
        "fast breathing",
        "rapid breathing",
        "weak pulse",
        "fainting",
        "collapsed",
        "collapse"
    );
    private static final Set<String> MENINGITIS_SEPSIS_ROUTINE_RASH_FEVER_DRIFT_MARKERS = buildSet(
        "routine rash",
        "mild rash",
        "simple rash",
        "fever and rash no other symptoms",
        "fever with rash but acting normal",
        "rash and fever acting normal"
    );
    private static final Set<String> MENINGITIS_SEPSIS_TRAUMA_DRIFT_MARKERS = buildSet(
        "seatbelt bruise",
        "seat belt bruise",
        "car accident",
        "neck pain after crash",
        "whiplash"
    );
    private static final Set<String> INFECTED_WOUND_CONTEXT_MARKERS = buildSet(
        "cut",
        "scrape",
        "wound",
        "puncture",
        "bite wound",
        "skin around it",
        "dressing",
        "wound edge",
        "redness around the wound"
    );
    private static final Set<String> INFECTED_WOUND_LOCAL_DANGER_MARKERS = buildSet(
        "red streak",
        "streak moving",
        "spreading redness",
        "redness is spreading",
        "redness spreading",
        "spreading past",
        "past the line",
        "marked line",
        "getting redder by the hour",
        "redder by the hour",
        "swollen hot",
        "swelling",
        "leaking pus",
        "pus",
        "foul odor",
        "smells bad",
        "hurts to move",
        "turning dark",
        "black",
        "greenish",
        "looks infected",
        "infected"
    );
    private static final Set<String> INFECTED_WOUND_SYSTEMIC_DANGER_MARKERS = buildSet(
        "fever",
        "chills",
        "confused",
        "confusion",
        "weak",
        "fast heartbeat",
        "rapid pulse",
        "fast breathing",
        "vomiting",
        "dehydrated"
    );
    private static final Set<String> INFECTED_WOUND_DRIFT_MARKERS = buildSet(
        "newborn cord",
        "umbilical cord",
        "belly button cord",
        "fever stiff neck",
        "stiff neck and purple rash",
        "purple rash",
        "non blanching rash",
        "nonblanching rash",
        "petechial rash",
        "what is cellulitis",
        "cellulitis in general",
        "signs of an infected wound",
        "signs of infected wound"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_PREGNANCY_DRIFT_MARKERS = buildSet(
        "pregnant",
        "pregnancy",
        "ectopic",
        "tubal pregnancy",
        "missed period",
        "positive pregnancy test",
        "vaginal bleeding"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_ROUTINE_GI_DRIFT_MARKERS = buildSet(
        "stomach flu",
        "food poisoning",
        "reflux",
        "heartburn",
        "constipation",
        "hemorrhoid",
        "hemorrhoids",
        "mild cramps",
        "mild cramping",
        "routine gi",
        "routine stomach",
        "upset stomach",
        "surgical abdomen",
        "appendicitis",
        "gallbladder",
        "kidney stone"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_OTHER_CARD_DRIFT_MARKERS = buildSet(
        "swallowed",
        "ingested",
        "unknown cleaner",
        "unlabeled bottle",
        "unlabelled bottle",
        "poison",
        "toxin",
        "choking",
        "cannot breathe",
        "can't breathe",
        "cant breathe",
        "newborn",
        "neonate",
        "umbilical cord",
        "cut",
        "scrape",
        "wound",
        "puncture",
        "red streak",
        "stiff neck",
        "purple rash",
        "non blanching rash",
        "nonblanching rash",
        "petechial rash"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_TRAUMA_MARKERS = buildSet(
        "abdominal trauma",
        "abdomen trauma",
        "belly trauma",
        "abdominal impact",
        "belly impact",
        "hit in the stomach",
        "hit in stomach",
        "hit in the belly",
        "hit in belly",
        "hit the stomach",
        "hit the belly",
        "hit abdomen",
        "hit my abdomen",
        "hit his abdomen",
        "hit her abdomen",
        "fall",
        "fell",
        "crash",
        "car accident",
        "bike accident",
        "handlebar",
        "seatbelt",
        "seat belt"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_ABDOMINAL_MARKERS = buildSet(
        "abdomen",
        "abdominal",
        "belly",
        "stomach",
        "side pain",
        "left side pain",
        "right side pain",
        "flank pain",
        "left flank pain",
        "right flank pain",
        "seatbelt bruise",
        "seat belt bruise",
        "handlebar"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_TRAUMA_DANGER_MARKERS = buildSet(
        "pale",
        "dizzy",
        "faint",
        "fainted",
        "fainting",
        "clammy",
        "rapid pulse",
        "fast pulse",
        "fast heartbeat",
        "vomiting",
        "throwing up",
        "rigid belly",
        "rigid abdomen",
        "hard belly",
        "severe belly pain",
        "severe abdominal pain",
        "severe stomach pain",
        "severe side pain",
        "severe flank pain"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_GI_BLEED_MARKERS = buildSet(
        "vomiting blood",
        "vomited blood",
        "throwing up blood",
        "threw up blood",
        "coffee ground vomit",
        "coffee grounds vomit",
        "coffee ground emesis",
        "black tarry stool",
        "black tarry stools",
        "tarry black stool",
        "tarry stool",
        "rectal blood",
        "blood from rectum",
        "blood in stool",
        "bloody stool"
    );
    private static final Set<String> ABDOMINAL_INTERNAL_BLEEDING_GI_DANGER_MARKERS = buildSet(
        "dizzy",
        "weak",
        "pale",
        "faint",
        "fainted",
        "fainting",
        "collapse",
        "collapsed",
        "rapid pulse",
        "fast pulse",
        "fast heartbeat",
        "severe belly pain",
        "severe abdominal pain",
        "severe stomach pain"
    );
    private static final Set<String> FOUNDRY_READINESS_CONTEXT_MARKERS = buildSet(
        "foundry",
        "casting area",
        "metal casting",
        "before casting",
        "pre work",
        "pre-work",
        "readiness log",
        "readiness checklist"
    );
    private static final Set<String> FOUNDRY_READINESS_ACTION_MARKERS = buildSet(
        "visible hazards",
        "hazard screening",
        "labels",
        "access control",
        "pause work",
        "who can pause",
        "owner handoff",
        "owner",
        "record",
        "wet floors",
        "cracked crucibles",
        "unknown scrap",
        "ventilation concerns"
    );
    private static final Set<String> FOUNDRY_READINESS_DRIFT_MARKERS = buildSet(
        "melt schedule",
        "melting temperature",
        "pouring temperature",
        "bronze melt",
        "alloy recipe",
        "flux recipe",
        "furnace setup",
        "set up the furnace",
        "tune the air blast",
        "pouring technique",
        "gating",
        "risers",
        "riser design",
        "sand chemistry",
        "calculate",
        "certify"
    );
    private static final Set<String> ANSWER_CARD_ALLOWED_REVIEW_STATUSES = buildSet(
        "reviewed",
        "pilot_reviewed",
        "approved"
    );
    private static final CardSpec POISONING_UNKNOWN_INGESTION_SPEC = new CardSpec(
        POISONING_UNKNOWN_INGESTION_CARD_ID,
        POISONING_UNKNOWN_INGESTION_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_POISONING,
        AnswerCardRuntime::isPoisoningAnswerCardPilotQuery
    );
    private static final CardSpec NEWBORN_DANGER_SEPSIS_SPEC = new CardSpec(
        NEWBORN_DANGER_SEPSIS_CARD_ID,
        NEWBORN_DANGER_SEPSIS_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_NEWBORN_DANGER,
        AnswerCardRuntime::isNewbornDangerSepsisAnswerCardQuery
    );
    private static final CardSpec CHOKING_AIRWAY_OBSTRUCTION_SPEC = new CardSpec(
        CHOKING_AIRWAY_OBSTRUCTION_CARD_ID,
        CHOKING_AIRWAY_OBSTRUCTION_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_AIRWAY_OBSTRUCTION,
        AnswerCardRuntime::isChokingAirwayObstructionAnswerCardQuery
    );
    private static final CardSpec MENINGITIS_SEPSIS_CHILD_SPEC = new CardSpec(
        MENINGITIS_SEPSIS_CHILD_CARD_ID,
        MENINGITIS_SEPSIS_CHILD_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_MENINGITIS_SEPSIS,
        AnswerCardRuntime::isMeningitisSepsisChildAnswerCardQuery
    );
    private static final CardSpec INFECTED_WOUND_SPREADING_INFECTION_SPEC = new CardSpec(
        INFECTED_WOUND_SPREADING_INFECTION_CARD_ID,
        INFECTED_WOUND_SPREADING_INFECTION_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_INFECTED_WOUND,
        AnswerCardRuntime::isInfectedWoundSpreadingInfectionAnswerCardQuery
    );
    private static final CardSpec ABDOMINAL_INTERNAL_BLEEDING_SPEC = new CardSpec(
        ABDOMINAL_INTERNAL_BLEEDING_CARD_ID,
        ABDOMINAL_INTERNAL_BLEEDING_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_INTERNAL_BLEEDING,
        AnswerCardRuntime::isAbdominalInternalBleedingAnswerCardQuery
    );
    private static final CardSpec FOUNDRY_CASTING_AREA_READINESS_SPEC = new CardSpec(
        FOUNDRY_CASTING_AREA_READINESS_CARD_ID,
        FOUNDRY_CASTING_AREA_READINESS_GUIDE_ID,
        STRUCTURE_TYPE_FOUNDRY_AREA_READINESS,
        AnswerCardRuntime::isFoundryCastingAreaReadinessAnswerCardQuery
    );
    private static final CardSpec[] CARD_SPECS = {
        POISONING_UNKNOWN_INGESTION_SPEC,
        NEWBORN_DANGER_SEPSIS_SPEC,
        CHOKING_AIRWAY_OBSTRUCTION_SPEC,
        MENINGITIS_SEPSIS_CHILD_SPEC,
        INFECTED_WOUND_SPREADING_INFECTION_SPEC,
        ABDOMINAL_INTERNAL_BLEEDING_SPEC,
        FOUNDRY_CASTING_AREA_READINESS_SPEC
    };
    private static volatile Boolean enabledForTest;

    private AnswerCardRuntime() {
    }

    static AnswerPlan tryPlan(
        Context context,
        PackRepository repository,
        String query
    ) {
        if (!isEnabled(context) || repository == null) {
            return null;
        }
        try {
            for (CardSpec spec : CARD_SPECS) {
                if (!spec.matches(query)) {
                    continue;
                }
                List<AnswerCard> cards = repository.loadAnswerCardsForGuideIds(
                    Collections.singleton(spec.guideId),
                    2
                );
                return planAnswerCardFromCards(query, cards, spec);
            }
            return null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static boolean supportsQuery(String query) {
        for (CardSpec spec : CARD_SPECS) {
            if (spec.matches(query)) {
                return true;
            }
        }
        return false;
    }

    static AnswerPlan planPoisoningAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, POISONING_UNKNOWN_INGESTION_SPEC);
    }

    static AnswerPlan planNewbornDangerSepsisAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, NEWBORN_DANGER_SEPSIS_SPEC);
    }

    static AnswerPlan planChokingAirwayObstructionAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, CHOKING_AIRWAY_OBSTRUCTION_SPEC);
    }

    static AnswerPlan planMeningitisSepsisChildAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, MENINGITIS_SEPSIS_CHILD_SPEC);
    }

    static AnswerPlan planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, INFECTED_WOUND_SPREADING_INFECTION_SPEC);
    }

    static AnswerPlan planAbdominalInternalBleedingAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, ABDOMINAL_INTERNAL_BLEEDING_SPEC);
    }

    static AnswerPlan planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, FOUNDRY_CASTING_AREA_READINESS_SPEC);
    }

    static boolean isPoisoningAnswerCardPilotQueryForTest(String query) {
        return isPoisoningAnswerCardPilotQuery(query);
    }

    static boolean isNewbornDangerSepsisAnswerCardQueryForTest(String query) {
        return isNewbornDangerSepsisAnswerCardQuery(query);
    }

    static boolean isChokingAirwayObstructionAnswerCardQueryForTest(String query) {
        return isChokingAirwayObstructionAnswerCardQuery(query);
    }

    static boolean isMeningitisSepsisChildAnswerCardQueryForTest(String query) {
        return isMeningitisSepsisChildAnswerCardQuery(query);
    }

    static boolean isInfectedWoundSpreadingInfectionAnswerCardQueryForTest(String query) {
        return isInfectedWoundSpreadingInfectionAnswerCardQuery(query);
    }

    static boolean isAbdominalInternalBleedingAnswerCardQueryForTest(String query) {
        return isAbdominalInternalBleedingAnswerCardQuery(query);
    }

    static boolean isFoundryCastingAreaReadinessAnswerCardQueryForTest(String query) {
        return isFoundryCastingAreaReadinessAnswerCardQuery(query);
    }

    static boolean supportsQueryForTest(String query) {
        return supportsQuery(query);
    }

    static boolean isEnabledForTest(Context context) {
        return isEnabled(context);
    }

    static void setEnabledForTest(boolean enabled) {
        enabledForTest = enabled;
    }

    static void resetEnabledForTest() {
        enabledForTest = null;
    }

    private static AnswerPlan planAnswerCardFromCards(String query, List<AnswerCard> cards, CardSpec spec) {
        if (spec == null || !spec.matches(query) || cards == null || cards.size() != 1) {
            return null;
        }
        AnswerCard card = cards.get(0);
        if (!isEligibleAnswerCard(card, spec.cardId, spec.guideId)) {
            return null;
        }
        List<SearchResult> sources = searchResultsForAnswerCard(card, spec.structureType);
        if (sources.isEmpty()) {
            return null;
        }
        String answerText = buildReviewedAnswerCardText(query, card);
        if (answerText.isEmpty()) {
            return null;
        }
        return new AnswerPlan(
            answerText,
            sources,
            ANSWER_CARD_RULE_PREFIX + card.cardId,
            ReviewedCardMetadata.fromAnswerCard(card)
        );
    }

    private static boolean isEnabled(Context context) {
        Boolean override = enabledForTest;
        if (override != null) {
            return override;
        }
        if (context == null) {
            return false;
        }
        return ReviewedCardRuntimeConfig.isEnabled(context);
    }

    private static boolean isPoisoningAnswerCardPilotQuery(String query) {
        String normalized = normalizeQueryText(query);
        return containsAny(normalized, POISONING_PILOT_ACTION_MARKERS)
            && containsAny(normalized, POISONING_PILOT_OBJECT_MARKERS);
    }

    private static boolean isNewbornDangerSepsisAnswerCardQuery(String query) {
        String normalized = normalizeQueryText(query);
        return NEWBORN_AGE_PATTERN.matcher(normalized).find()
            && containsNewbornDangerMarker(normalized);
    }

    private static boolean isChokingAirwayObstructionAnswerCardQuery(String query) {
        String normalized = normalizeQueryText(query);
        if (normalized.isEmpty()
            || containsAny(normalized, CHOKING_ALLERGY_DRIFT_MARKERS)
            || containsAny(normalized, CHOKING_POISONING_DRIFT_MARKERS)) {
            return false;
        }
        boolean hasContext = containsAny(normalized, CHOKING_CONTEXT_MARKERS);
        boolean hasHighRisk = containsAny(normalized, CHOKING_HIGH_RISK_MARKERS);
        if (containsAny(normalized, CHOKING_PANIC_DRIFT_MARKERS) && !hasHighRisk) {
            return false;
        }
        return hasContext && (hasHighRisk || containsAny(normalized, CHOKING_HELP_MARKERS));
    }

    private static boolean isMeningitisSepsisChildAnswerCardQuery(String query) {
        String normalized = normalizeQueryText(query);
        if (normalized.isEmpty()
            || containsAny(normalized, MENINGITIS_SEPSIS_PUBLIC_HEALTH_DRIFT_MARKERS)
            || containsAny(normalized, MENINGITIS_SEPSIS_TRAUMA_DRIFT_MARKERS)
            || containsAny(normalized, MENINGITIS_SEPSIS_ROUTINE_RASH_FEVER_DRIFT_MARKERS)) {
            return false;
        }
        boolean hasFever = containsAny(normalized, MENINGITIS_SEPSIS_FEVER_MARKERS);
        boolean hasRash = containsAny(normalized, MENINGITIS_SEPSIS_RASH_MARKERS);
        boolean hasMentalStatus = containsAny(normalized, MENINGITIS_SEPSIS_MENTAL_STATUS_MARKERS);
        boolean hasStiffNeck = containsAny(normalized, MENINGITIS_SEPSIS_STIFF_NECK_MARKERS);
        boolean hasShock = containsAny(normalized, MENINGITIS_SEPSIS_SHOCK_MARKERS);
        boolean hasStrictRedFlag = (hasFever && hasStiffNeck)
            || (hasFever && hasRash)
            || (hasFever && hasMentalStatus)
            || (hasFever && countContained(normalized, MENINGITIS_SEPSIS_HEADACHE_CLUSTER_MARKERS) >= 2)
            || (hasMentalStatus && hasRash)
            || (hasShock && containsAny(normalized, MENINGITIS_SEPSIS_INFECTION_MARKERS));
        return hasStrictRedFlag
            && (!containsAny(normalized, MENINGITIS_SEPSIS_COMPARISON_DRIFT_MARKERS)
                || hasFever
                || hasMentalStatus
                || hasRash
                || hasShock);
    }

    private static boolean isInfectedWoundSpreadingInfectionAnswerCardQuery(String query) {
        String normalized = normalizeQueryText(query);
        if (normalized.isEmpty() || containsAny(normalized, INFECTED_WOUND_DRIFT_MARKERS)) {
            return false;
        }
        boolean hasWoundContext = containsAny(normalized, INFECTED_WOUND_CONTEXT_MARKERS);
        boolean hasLocalDanger = containsAnyUnnegated(normalized, INFECTED_WOUND_LOCAL_DANGER_MARKERS);
        boolean hasSystemicDanger = containsAnyUnnegated(normalized, INFECTED_WOUND_SYSTEMIC_DANGER_MARKERS);
        return hasWoundContext && (hasLocalDanger || hasSystemicDanger);
    }

    private static boolean isAbdominalInternalBleedingAnswerCardQuery(String query) {
        String normalized = normalizeQueryText(query);
        if (normalized.isEmpty()
            || containsAny(normalized, ABDOMINAL_INTERNAL_BLEEDING_PREGNANCY_DRIFT_MARKERS)
            || containsAny(normalized, ABDOMINAL_INTERNAL_BLEEDING_ROUTINE_GI_DRIFT_MARKERS)
            || containsAny(normalized, ABDOMINAL_INTERNAL_BLEEDING_OTHER_CARD_DRIFT_MARKERS)) {
            return false;
        }
        boolean hasTraumaContext = containsAny(normalized, ABDOMINAL_INTERNAL_BLEEDING_TRAUMA_MARKERS)
            && containsAnyUnnegated(normalized, ABDOMINAL_INTERNAL_BLEEDING_ABDOMINAL_MARKERS);
        boolean hasTraumaDanger = containsAnyUnnegated(
            normalized,
            ABDOMINAL_INTERNAL_BLEEDING_TRAUMA_DANGER_MARKERS
        );
        boolean hasGiBleed = containsAnyUnnegated(normalized, ABDOMINAL_INTERNAL_BLEEDING_GI_BLEED_MARKERS);
        boolean hasGiDanger = containsAnyUnnegated(
            normalized,
            ABDOMINAL_INTERNAL_BLEEDING_GI_DANGER_MARKERS
        );
        if (hasTraumaContext && hasTraumaDanger) {
            return true;
        }
        if (hasGiBleed && hasGiDanger) {
            return true;
        }
        return false;
    }

    private static boolean isFoundryCastingAreaReadinessAnswerCardQuery(String query) {
        String normalized = normalizeQueryText(query);
        if (normalized.isEmpty() || containsAny(normalized, FOUNDRY_READINESS_DRIFT_MARKERS)) {
            return false;
        }
        return containsAny(normalized, FOUNDRY_READINESS_CONTEXT_MARKERS)
            && containsAny(normalized, FOUNDRY_READINESS_ACTION_MARKERS);
    }

    private static boolean isEligibleAnswerCard(AnswerCard card, String expectedCardId, String expectedGuideId) {
        if (card == null) {
            return false;
        }
        String cardId = safe(card.cardId).trim();
        String guideId = safe(card.guideId).trim();
        String riskTier = safe(card.riskTier).trim().toLowerCase(QUERY_LOCALE);
        String reviewStatus = safe(card.reviewStatus).trim().toLowerCase(QUERY_LOCALE);
        return safe(expectedCardId).equals(cardId)
            && safe(expectedGuideId).equals(guideId)
            && ("critical".equals(riskTier) || "high".equals(riskTier))
            && ANSWER_CARD_ALLOWED_REVIEW_STATUSES.contains(reviewStatus);
    }

    private static String buildReviewedAnswerCardText(String query, AnswerCard card) {
        ArrayList<String> lines = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        appendClauseTexts(lines, seen, clauseTexts(card, "required_first_action"), 0, "");
        appendClauseTexts(lines, seen, activeConditionalClauseTexts(query, card), 0, "");
        appendClauseTexts(lines, seen, clauseTexts(card, "first_action"), 4, "");
        appendClauseTexts(lines, seen, clauseTexts(card, "urgent_red_flag"), 3, "Escalate now if ");
        appendClauseTexts(lines, seen, clauseTexts(card, "forbidden_advice"), 4, "Avoid: ");
        appendClauseTexts(lines, seen, clauseTexts(card, "do_not"), 2, "Avoid: ");
        return String.join("\n", lines).trim();
    }

    private static List<String> clauseTexts(AnswerCard card, String kind) {
        if (card == null || card.clauses.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> values = new ArrayList<>();
        for (AnswerCardClause clause : card.clauses) {
            if (clause != null && kind.equals(safe(clause.clauseKind))) {
                String text = safe(clause.text).trim();
                if (!text.isEmpty()) {
                    values.add(text);
                }
            }
        }
        return values;
    }

    private static List<String> activeConditionalClauseTexts(String query, AnswerCard card) {
        if (card == null || card.clauses.isEmpty()) {
            return Collections.emptyList();
        }
        String normalizedQuery = normalizeQueryText(query);
        ArrayList<String> values = new ArrayList<>();
        for (AnswerCardClause clause : card.clauses) {
            if (clause == null || !"conditional_required_action".equals(safe(clause.clauseKind))) {
                continue;
            }
            if (clause.triggerTerms.isEmpty() || !containsAny(normalizedQuery, new LinkedHashSet<>(clause.triggerTerms))) {
                continue;
            }
            String text = safe(clause.text).trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
        return values;
    }

    private static void appendClauseTexts(
        List<String> output,
        Set<String> seen,
        List<String> texts,
        int maxItems,
        String prefix
    ) {
        int added = 0;
        for (String text : texts) {
            if (maxItems > 0 && added >= maxItems) {
                return;
            }
            String line = (safe(prefix) + safe(text)).trim();
            String key = line.toLowerCase(QUERY_LOCALE);
            if (line.isEmpty() || seen.contains(key)) {
                continue;
            }
            seen.add(key);
            output.add((output.size() + 1) + ". " + line);
            added++;
        }
    }

    private static List<SearchResult> searchResultsForAnswerCard(AnswerCard card, String structureType) {
        if (card == null || card.sources.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> results = new ArrayList<>();
        for (AnswerCardSource source : card.sources) {
            if (source == null) {
                continue;
            }
            String guideId = safe(source.sourceGuideId).trim();
            if (guideId.isEmpty()) {
                continue;
            }
            String sectionHeading = source.sections.isEmpty()
                ? safe(source.title).trim()
                : source.sections.get(0);
            String title = safe(source.title).trim().isEmpty()
                ? guideId
                : safe(source.title).trim();
            String snippet = source.sections.isEmpty()
                ? "Reviewed answer card source."
                : String.join(", ", source.sections);
            results.add(new SearchResult(
                title,
                guideId,
                snippet,
                snippet,
                guideId,
                sectionHeading,
                "medical",
                "answer-card",
                "safety",
                "immediate",
                safe(structureType),
                safe(card.slug)
            ));
        }
        return Collections.unmodifiableList(results);
    }

    private static boolean containsAny(String text, Set<String> markers) {
        String normalized = safe(text).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return false;
        }
        for (String marker : markers) {
            String normalizedMarker = safe(marker).trim().toLowerCase(QUERY_LOCALE);
            if (!normalizedMarker.isEmpty() && normalized.contains(normalizedMarker)) {
                return true;
            }
        }
        return false;
    }

    private static int countContained(String text, Set<String> markers) {
        String normalized = safe(text).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String marker : markers) {
            String normalizedMarker = safe(marker).trim().toLowerCase(QUERY_LOCALE);
            if (!normalizedMarker.isEmpty() && normalized.contains(normalizedMarker)) {
                count++;
            }
        }
        return count;
    }

    private static boolean containsAnyUnnegated(String text, Set<String> markers) {
        String normalized = safe(text).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return false;
        }
        for (String marker : markers) {
            String normalizedMarker = safe(marker).trim().toLowerCase(QUERY_LOCALE);
            if (!normalizedMarker.isEmpty()
                && normalized.contains(normalizedMarker)
                && !isNegatedDangerMarker(normalized, normalizedMarker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsNewbornDangerMarker(String text) {
        String normalized = normalizeQueryText(text);
        if (normalized.isEmpty()) {
            return false;
        }
        for (String marker : NEWBORN_DANGER_MARKERS) {
            String normalizedMarker = normalizeQueryText(marker);
            if (normalizedMarker.isEmpty() || !normalized.contains(normalizedMarker)) {
                continue;
            }
            if (isNegatedNewbornCordMarker(normalized, normalizedMarker)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static boolean isNegatedNewbornCordMarker(String text, String marker) {
        if (!"pus".equals(marker)
            && !"spreading redness".equals(marker)
            && !"redness spreading".equals(marker)) {
            return false;
        }
        return text.contains("without " + marker)
            || text.contains("no " + marker)
            || text.contains("without redness or pus")
            || text.contains("without pus or redness")
            || text.contains("no redness or pus")
            || text.contains("no pus or redness");
    }

    private static boolean isNegatedDangerMarker(String text, String marker) {
        return text.contains("no " + marker)
            || text.contains("without " + marker)
            || text.contains("not " + marker)
            || text.contains("no danger signs")
            || text.contains("without danger signs")
            || text.contains("no shock signs")
            || text.contains("without shock signs")
            || ("dizzy".equals(marker) && (text.contains("no dizziness") || text.contains("not dizzy") || text.contains("no weakness or dizziness")))
            || ("weak".equals(marker) && (text.contains("no weakness") || text.contains("not weak") || text.contains("no dizziness or weakness")))
            || ("faint".equals(marker) && (text.contains("no fainting") || text.contains("did not faint") || text.contains("not faint")))
            || ("pale".equals(marker) && text.contains("not pale"))
            || ("vomiting".equals(marker) && (text.contains("no vomiting") || text.contains("not vomiting")))
            || (("rapid pulse".equals(marker) || "fast pulse".equals(marker)) && text.contains("normal pulse"))
            || ("fast heartbeat".equals(marker) && (text.contains("normal heartbeat") || text.contains("normal heart rate")))
            || text.contains("no redness or pus")
            || text.contains("no pus or redness")
            || text.contains("no fever pus swelling or redness")
            || text.contains("no fever, pus, swelling, or redness")
            || text.contains("no fever, pus, swelling or redness");
    }

    private static String normalizeQueryText(String text) {
        return safe(text).trim().toLowerCase(QUERY_LOCALE).replace('-', ' ');
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    private interface QueryPredicate {
        boolean matches(String query);
    }

    private static final class CardSpec {
        final String cardId;
        final String guideId;
        final String structureType;
        final QueryPredicate predicate;

        CardSpec(String cardId, String guideId, String structureType, QueryPredicate predicate) {
            this.cardId = safe(cardId).trim();
            this.guideId = safe(guideId).trim();
            this.structureType = safe(structureType).trim();
            this.predicate = predicate;
        }

        boolean matches(String query) {
            return predicate != null && predicate.matches(query);
        }
    }

    static final class AnswerPlan {
        final String answerText;
        final List<SearchResult> sources;
        final String ruleId;
        final ReviewedCardMetadata reviewedCardMetadata;

        AnswerPlan(String answerText, List<SearchResult> sources, String ruleId) {
            this(answerText, sources, ruleId, ReviewedCardMetadata.empty());
        }

        AnswerPlan(
            String answerText,
            List<SearchResult> sources,
            String ruleId,
            ReviewedCardMetadata reviewedCardMetadata
        ) {
            this.answerText = safe(answerText).trim();
            this.sources = sources == null ? Collections.emptyList() : new ArrayList<>(sources);
            this.ruleId = safe(ruleId).trim();
            this.reviewedCardMetadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        }
    }
}

package com.senku.mobile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class OfflineAnswerEngineAnswerCardRuntimeTest {
    private Context context;
    private File databaseFile;

    @After
    public void tearDown() {
        AnswerCardRuntime.resetEnabledForTest();
        if (context != null) {
            HostInferenceConfig.setEnabled(context, false);
            ReviewedCardRuntimeConfig.setEnabled(context, false);
        }
        if (databaseFile != null) {
            databaseFile.delete();
        }
    }

    @Test
    public void prepareUsesReviewedCardRuntimeWhenHiddenFlagIsEnabled() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime", ".db", context.getCacheDir());
        createPilotPackDatabase(databaseFile);
        ReviewedCardRuntimeConfig.setEnabled(context, false);
        HostInferenceConfig.setEnabled(context, true);
        ReviewedCardRuntimeConfig.setEnabled(context, true);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.prepare(
                context,
                repository,
                new SessionMemory(),
                null,
                "my child swallowed an unknown cleaner"
            );

            assertTrue(prepared.deterministic);
            assertEquals("answer_card:poisoning_unknown_ingestion", prepared.ruleId);
            assertTrue(prepared.reviewedCardMetadata.isPresent());
            assertEquals("poisoning_unknown_ingestion", prepared.reviewedCardMetadata.cardId);
            assertEquals("GD-898", prepared.reviewedCardMetadata.cardGuideId);
            assertEquals("pilot_reviewed", prepared.reviewedCardMetadata.reviewStatus);
            assertEquals(
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                prepared.reviewedCardMetadata.provenance
            );
            assertEquals(1, prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.size());
            assertEquals("GD-898", prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.get(0));
            assertEquals(1, prepared.sources.size());
            assertEquals("GD-898", prepared.sources.get(0).guideId);
            assertEquals("answer-card", prepared.sources.get(0).retrievalMode);
            assertTrue(prepared.answerBody.contains("Call poison control"));
            assertTrue(prepared.answerBody.contains("Keep the child with an adult"));
            assertTrue(prepared.answerBody.contains("Avoid: Do not induce vomiting"));

            OfflineAnswerEngine.AnswerRun run = OfflineAnswerEngine.generate(context, null, prepared);

            assertTrue(run.reviewedCardMetadata.isPresent());
            assertEquals(prepared.reviewedCardMetadata.cardId, run.reviewedCardMetadata.cardId);
            assertEquals(prepared.reviewedCardMetadata.cardGuideId, run.reviewedCardMetadata.cardGuideId);
            assertEquals(prepared.reviewedCardMetadata.reviewStatus, run.reviewedCardMetadata.reviewStatus);
            assertEquals(
                prepared.reviewedCardMetadata.citedReviewedSourceGuideIds,
                run.reviewedCardMetadata.citedReviewedSourceGuideIds
            );
        }
    }

    @Test
    public void prepareUsesNewbornReviewedCardRuntimeWhenHiddenFlagIsEnabled() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime-newborn", ".db", context.getCacheDir());
        createNewbornPackDatabase(databaseFile);
        HostInferenceConfig.setEnabled(context, true);
        ReviewedCardRuntimeConfig.setEnabled(context, true);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.prepare(
                context,
                repository,
                new SessionMemory(),
                null,
                "newborn is limp, will not feed, and is hard to wake"
            );

            assertTrue(prepared.deterministic);
            assertEquals("answer_card:newborn_danger_sepsis", prepared.ruleId);
            assertTrue(prepared.reviewedCardMetadata.isPresent());
            assertEquals("newborn_danger_sepsis", prepared.reviewedCardMetadata.cardId);
            assertEquals("GD-284", prepared.reviewedCardMetadata.cardGuideId);
            assertEquals("pilot_reviewed", prepared.reviewedCardMetadata.reviewStatus);
            assertEquals(
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                prepared.reviewedCardMetadata.provenance
            );
            assertEquals(4, prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.size());
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-284"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-298"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-492"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-617"));
            assertEquals(4, prepared.sources.size());
            assertEquals("GD-284", prepared.sources.get(0).guideId);
            assertEquals("answer-card", prepared.sources.get(0).retrievalMode);
            assertEquals("safety_newborn_danger", prepared.sources.get(0).structureType);
            assertTrue(prepared.answerBody.contains("Treat breathing difficulty"));
            assertTrue(prepared.answerBody.contains("Keep the newborn warm"));
            assertTrue(prepared.answerBody.contains("Avoid: Do not treat fever"));
        }
    }

    @Test
    public void prepareUsesChokingReviewedCardRuntimeWhenHiddenFlagIsEnabled() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime-choking", ".db", context.getCacheDir());
        createChokingPackDatabase(databaseFile);
        HostInferenceConfig.setEnabled(context, true);
        ReviewedCardRuntimeConfig.setEnabled(context, true);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.prepare(
                context,
                repository,
                new SessionMemory(),
                null,
                "baby is choking and cannot cry or cough"
            );

            assertTrue(prepared.deterministic);
            assertEquals("answer_card:choking_airway_obstruction", prepared.ruleId);
            assertTrue(prepared.reviewedCardMetadata.isPresent());
            assertEquals("choking_airway_obstruction", prepared.reviewedCardMetadata.cardId);
            assertEquals("GD-232", prepared.reviewedCardMetadata.cardGuideId);
            assertEquals("pilot_reviewed", prepared.reviewedCardMetadata.reviewStatus);
            assertEquals("", prepared.reviewedCardMetadata.runtimeCitationPolicy);
            assertEquals(
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                prepared.reviewedCardMetadata.provenance
            );
            assertEquals(4, prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.size());
            assertEquals("GD-232", prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.get(0));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-284"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-298"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-617"));
            assertEquals(4, prepared.sources.size());
            assertEquals("GD-232", prepared.sources.get(0).guideId);
            assertEquals("answer-card", prepared.sources.get(0).retrievalMode);
            assertEquals("safety_airway_obstruction", prepared.sources.get(0).structureType);
            assertTrue(prepared.answerBody.contains("start age-appropriate choking rescue"));
            assertTrue(prepared.answerBody.contains("5 back blows followed by 5 chest thrusts"));
            assertTrue(prepared.answerBody.contains("Avoid: Do not do blind finger sweeps"));
            assertTrue(prepared.answerBody.contains("Avoid: Do not perform abdominal thrusts on infants"));
        }
    }

    @Test
    public void prepareUsesMeningitisSepsisReviewedCardRuntimeWithoutHostOrImportedModel() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime-meningitis", ".db", context.getCacheDir());
        createMeningitisPackDatabase(databaseFile);
        HostInferenceConfig.setEnabled(context, false);
        ReviewedCardRuntimeConfig.setEnabled(context, true);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.prepare(
                context,
                repository,
                new SessionMemory(),
                null,
                "child has fever, stiff neck, and a purple rash that does not fade when pressed"
            );

            assertTrue(prepared.deterministic);
            assertEquals("answer_card:meningitis_sepsis_child", prepared.ruleId);
            assertTrue(prepared.reviewedCardMetadata.isPresent());
            assertEquals("meningitis_sepsis_child", prepared.reviewedCardMetadata.cardId);
            assertEquals("GD-589", prepared.reviewedCardMetadata.cardGuideId);
            assertEquals("pilot_reviewed", prepared.reviewedCardMetadata.reviewStatus);
            assertEquals("", prepared.reviewedCardMetadata.runtimeCitationPolicy);
            assertEquals(
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                prepared.reviewedCardMetadata.provenance
            );
            assertEquals(5, prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.size());
            assertEquals("GD-589", prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.get(0));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-235"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-268"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-284"));
            assertTrue(prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.contains("GD-298"));
            assertEquals(5, prepared.sources.size());
            assertEquals("GD-589", prepared.sources.get(0).guideId);
            assertEquals("answer-card", prepared.sources.get(0).retrievalMode);
            assertEquals("safety_meningitis_sepsis", prepared.sources.get(0).structureType);
            assertTrue(prepared.answerBody.contains("suspected meningitis or meningococcemia"));
            assertTrue(prepared.answerBody.contains("Escalate urgently"));
            assertTrue(prepared.answerBody.contains("Fever with stiff neck"));
            assertTrue(prepared.answerBody.contains("Avoid: Do not route fever plus stiff neck"));
        }
    }

    @Test
    public void prepareUsesInfectedWoundReviewedCardRuntimeWithoutHostOrImportedModel() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime-infected-wound", ".db", context.getCacheDir());
        createInfectedWoundPackDatabase(databaseFile);
        HostInferenceConfig.setEnabled(context, false);
        ReviewedCardRuntimeConfig.setEnabled(context, true);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.prepare(
                context,
                repository,
                new SessionMemory(),
                null,
                "cut on my hand yesterday and now a red streak is moving up my arm"
            );

            assertTrue(prepared.deterministic);
            assertEquals("answer_card:infected_wound_spreading_infection", prepared.ruleId);
            assertTrue(prepared.reviewedCardMetadata.isPresent());
            assertEquals("infected_wound_spreading_infection", prepared.reviewedCardMetadata.cardId);
            assertEquals("GD-585", prepared.reviewedCardMetadata.cardGuideId);
            assertEquals("pilot_reviewed", prepared.reviewedCardMetadata.reviewStatus);
            assertEquals("", prepared.reviewedCardMetadata.runtimeCitationPolicy);
            assertEquals(
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                prepared.reviewedCardMetadata.provenance
            );
            assertEquals(1, prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.size());
            assertEquals("GD-585", prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.get(0));
            assertEquals(1, prepared.sources.size());
            assertEquals("GD-585", prepared.sources.get(0).guideId);
            assertEquals("answer-card", prepared.sources.get(0).retrievalMode);
            assertEquals("safety_infected_wound", prepared.sources.get(0).structureType);
            assertTrue(prepared.answerBody.contains("mark its edge, and recheck every 2 hours"));
            assertTrue(prepared.answerBody.contains("Escalate urgently if red streaking"));
            assertTrue(prepared.answerBody.contains("For spreading infection, clean 2 to 3 times daily"));
            assertTrue(prepared.answerBody.contains("Avoid: Do not reassure rapidly spreading redness"));
        }
    }

    @Test
    public void prepareUsesAbdominalInternalBleedingReviewedCardRuntimeWithoutHostOrImportedModel() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime-abdominal-bleeding", ".db", context.getCacheDir());
        createAbdominalInternalBleedingPackDatabase(databaseFile);
        HostInferenceConfig.setEnabled(context, false);
        ReviewedCardRuntimeConfig.setEnabled(context, true);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            OfflineAnswerEngine.PreparedAnswer prepared = OfflineAnswerEngine.prepare(
                context,
                repository,
                new SessionMemory(),
                null,
                "bike handlebar hit his belly and now he is pale and dizzy"
            );

            assertTrue(prepared.deterministic);
            assertEquals("answer_card:abdominal_internal_bleeding", prepared.ruleId);
            assertTrue(prepared.reviewedCardMetadata.isPresent());
            assertEquals("abdominal_internal_bleeding", prepared.reviewedCardMetadata.cardId);
            assertEquals("GD-380", prepared.reviewedCardMetadata.cardGuideId);
            assertEquals("pilot_reviewed", prepared.reviewedCardMetadata.reviewStatus);
            assertEquals("reviewed_source_family", prepared.reviewedCardMetadata.runtimeCitationPolicy);
            assertEquals(
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                prepared.reviewedCardMetadata.provenance
            );
            assertEquals(1, prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.size());
            assertEquals("GD-380", prepared.reviewedCardMetadata.citedReviewedSourceGuideIds.get(0));
            assertEquals(1, prepared.sources.size());
            assertEquals("GD-380", prepared.sources.get(0).guideId);
            assertEquals("answer-card", prepared.sources.get(0).retrievalMode);
            assertEquals("safety_internal_bleeding", prepared.sources.get(0).structureType);
            assertTrue(prepared.answerBody.contains("Treat abdominal trauma or GI bleeding with shock signs"));
            assertTrue(prepared.answerBody.contains("Keep the person still and lying down"));
            assertTrue(prepared.answerBody.contains("Escalate now if Pale, dizzy, faint, clammy, rapid pulse"));
            assertTrue(prepared.answerBody.contains("Avoid: Do not give food, drink, or pain medicine"));
        }
    }

    @Test
    public void reviewedCardRuntimeConfigDefaultsOffAndCanEnablePlanner() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime-default", ".db", context.getCacheDir());
        createPilotPackDatabase(databaseFile);
        ReviewedCardRuntimeConfig.setEnabled(context, false);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            assertTrue(!ReviewedCardRuntimeConfig.isEnabled(context));
            assertTrue(AnswerCardRuntime.tryPlan(
                context,
                repository,
                "my child swallowed an unknown cleaner"
            ) == null);

            ReviewedCardRuntimeConfig.setEnabled(context, true);

            assertTrue(ReviewedCardRuntimeConfig.isEnabled(context));
            assertTrue(AnswerCardRuntime.tryPlan(
                context,
                repository,
                "my child swallowed an unknown cleaner"
            ) != null);
        }
    }

    @Test
    public void reviewedCardRuntimeFallsBackWhenOptionalTablesAreMissing() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        databaseFile = File.createTempFile("answer-card-runtime-old-pack", ".db", context.getCacheDir());
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).close();
        HostInferenceConfig.setEnabled(context, true);
        ReviewedCardRuntimeConfig.setEnabled(context, true);

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            assertNull(AnswerCardRuntime.tryPlan(
                context,
                repository,
                "my child swallowed an unknown cleaner"
            ));
        }
    }

    private static void createPilotPackDatabase(File databaseFile) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        try {
            createAnswerCardTables(database);
            database.execSQL(
                "INSERT INTO answer_cards (" +
                    "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                    "review_status, runtime_citation_policy, routine_boundary, " +
                    "acceptable_uncertain_fit, notes" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{
                    "poisoning_unknown_ingestion",
                    "GD-898",
                    "poisoning_unknown_ingestion",
                    "Poisoning / Unknown Ingestion",
                    "critical",
                    "guide-corpus",
                    "pilot_reviewed",
                    "reviewed_source_family",
                    "Do not wait for symptoms after an unknown ingestion.",
                    "Say if the substance is unknown.",
                    "Synthetic card runtime test fixture."
                }
            );
            insertClause(
                database,
                "required_first_action",
                1,
                "Call poison control, EMS, or the fastest clinician now.",
                null
            );
            insertClause(
                database,
                "conditional_required_action",
                1,
                "Keep the child with an adult.",
                "[\"child\",\"kid\"]"
            );
            insertClause(database, "first_action", 1, "Check airway and breathing first.", null);
            insertClause(database, "forbidden_advice", 1, "Do not induce vomiting.", null);
            database.execSQL(
                "INSERT INTO answer_card_sources (" +
                    "card_id, source_guide_id, slug, title, sections_json, is_primary" +
                    ") VALUES (?, ?, ?, ?, ?, ?)",
                new Object[]{
                    "poisoning_unknown_ingestion",
                    "GD-898",
                    "poisoning",
                    "Poisoning",
                    "[\"Triage\"]",
                    1
                }
            );
        } finally {
            database.close();
        }
    }

    private static void createNewbornPackDatabase(File databaseFile) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        try {
            createAnswerCardTables(database);
            database.execSQL(
                "INSERT INTO answer_cards (" +
                    "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                    "review_status, runtime_citation_policy, routine_boundary, " +
                    "acceptable_uncertain_fit, notes" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{
                    "newborn_danger_sepsis",
                    "GD-284",
                    "infant-child-care",
                    "Infant & Child Care (Birth to Age 5)",
                    "critical",
                    "guide-corpus",
                    "pilot_reviewed",
                    "reviewed_source_family",
                    "Escalate newborn danger signs immediately.",
                    "If age may be under 28 days, use the newborn danger-sign frame.",
                    "Synthetic newborn runtime test fixture."
                }
            );
            insertClause(
                database,
                "newborn_danger_sepsis",
                "required_first_action",
                1,
                "Treat breathing difficulty, inability to feed, fever or hypothermia, seizures, bilious vomiting, or severe lethargy as requiring immediate escalation.",
                null
            );
            insertClause(
                database,
                "newborn_danger_sepsis",
                "required_first_action",
                2,
                "Keep the newborn warm and monitor breathing closely while arranging urgent evaluation.",
                null
            );
            insertClause(
                database,
                "newborn_danger_sepsis",
                "conditional_required_action",
                1,
                "If umbilical infection has fever or spreading redness, treat it as life-threatening and seek antibiotic-capable care.",
                "[\"umbilical\",\"cord\",\"redness\",\"pus\"]"
            );
            insertClause(
                database,
                "newborn_danger_sepsis",
                "first_action",
                1,
                "For fast or difficult breathing, keep the newborn warm, semi-upright, and under close breathing observation.",
                null
            );
            insertClause(
                database,
                "newborn_danger_sepsis",
                "urgent_red_flag",
                1,
                "Difficulty breathing or fast breathing over 60 breaths per minute at rest.",
                null
            );
            insertClause(
                database,
                "newborn_danger_sepsis",
                "forbidden_advice",
                1,
                "Do not reassure a sick newborn with these signs as routine fussiness or ordinary feeding trouble.",
                null
            );
            insertClause(
                database,
                "newborn_danger_sepsis",
                "do_not",
                1,
                "Do not treat fever, hypothermia, poor feeding, or breathing trouble in the first 28 days as watchful waiting.",
                null
            );
            insertSource(database, "newborn_danger_sepsis", "GD-284", "infant-child-care", "Infant & Child Care", "[\"Critical Danger Signs\"]", 1);
            insertSource(database, "newborn_danger_sepsis", "GD-298", "pediatric-emergency-medicine", "Pediatric Emergency Medicine", "[\"Neonatal Sepsis Recognition\"]", 0);
            insertSource(database, "newborn_danger_sepsis", "GD-492", "postpartum-care-mother-infant", "Postpartum Care", "[\"Newborn Danger Signs\"]", 0);
            insertSource(database, "newborn_danger_sepsis", "GD-617", "pediatric-emergencies-field", "Pediatric Emergencies", "[\"Rapid Breathing Lethargy Wont Eat Emergency Check\"]", 0);
        } finally {
            database.close();
        }
    }

    private static void createChokingPackDatabase(File databaseFile) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        try {
            createAnswerCardTables(database);
            database.execSQL(
                "INSERT INTO answer_cards (" +
                    "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                    "review_status, runtime_citation_policy, routine_boundary, " +
                    "acceptable_uncertain_fit, notes" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{
                    "choking_airway_obstruction",
                    "GD-232",
                    "first-aid",
                    "First Aid & Emergency Response",
                    "critical",
                    "guide-corpus",
                    "pilot_reviewed",
                    "",
                    "Any inability to speak, cough, breathe, cry, or any cyanosis/collapse is an airway emergency.",
                    "If air is moving, encourage coughing; if speech/cough/breathing is absent, start choking rescue.",
                    "Synthetic choking runtime test fixture."
                }
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "required_first_action",
                1,
                "If the person is coughing and talking, keep them upright, encourage coughing, and watch closely.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "required_first_action",
                2,
                "If the person cannot speak, cough, or breathe, call for emergency help and start age-appropriate choking rescue.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "required_first_action",
                3,
                "If the choking person collapses, lower them to the floor, call for help/AED, start CPR, and remove only a clearly visible object.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "conditional_required_action",
                1,
                "If an adult or older child cannot speak, cough, or breathe, call for emergency help and start abdominal thrusts.",
                "[\"adult\",\"older child\",\"teen\",\"teenager\"]"
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "conditional_required_action",
                2,
                "If an infant cannot cry, cough, or breathe, use 5 back blows followed by 5 chest thrusts.",
                "[\"infant\",\"baby\",\"newborn\",\"under 1\",\"under one\"]"
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "first_action",
                1,
                "For a conscious adult or older child with complete obstruction, stand behind them and repeat quick upward abdominal thrusts until the object clears or they become unconscious.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "first_action",
                2,
                "For an infant under 1 year, alternate 5 back blows and 5 chest thrusts until the object clears or the infant becomes unresponsive.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "urgent_red_flag",
                1,
                "Cannot speak, cough, or breathe.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "urgent_red_flag",
                2,
                "Blue lips or cyanosis.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "forbidden_advice",
                1,
                "Do not do blind finger sweeps.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "forbidden_advice",
                2,
                "Do not start abdominal thrusts when an adult or older child is still coughing and talking.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "forbidden_advice",
                3,
                "Do not perform abdominal thrusts on infants.",
                null
            );
            insertClause(
                database,
                "choking_airway_obstruction",
                "do_not",
                1,
                "Do not put fingers into the mouth unless the object is clearly visible.",
                null
            );
            insertSource(database, "choking_airway_obstruction", "GD-232", "first-aid", "First Aid & Emergency Response", "[\"#choking\",\"#choking > Choking Response\",\"#choking > Infant Choking\"]", 1);
            insertSource(database, "choking_airway_obstruction", "GD-298", "pediatric-emergency-medicine", "Pediatric Emergency Medicine", "[\"#airway-anatomy\",\"Foreign Body Airway Obstruction\"]", 0);
            insertSource(database, "choking_airway_obstruction", "GD-284", "infant-child-care", "Infant & Child Care (Birth to Age 5)", "[\"Safety & Injury Prevention > Suffocation & Choking Prevention\"]", 0);
            insertSource(database, "choking_airway_obstruction", "GD-617", "pediatric-emergencies-field", "Pediatric Emergencies", "[\"Airway Obstruction / Choking\",\"Respiratory Distress Assessment\"]", 0);
        } finally {
            database.close();
        }
    }

    private static void createMeningitisPackDatabase(File databaseFile) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        try {
            createAnswerCardTables(database);
            database.execSQL(
                "INSERT INTO answer_cards (" +
                    "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                    "review_status, runtime_citation_policy, routine_boundary, " +
                    "acceptable_uncertain_fit, notes" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{
                    "meningitis_sepsis_child",
                    "GD-589",
                    "sepsis-recognition-antibiotic-protocols",
                    "Sepsis Recognition, Escalation & Empiric Antibiotic Protocols",
                    "critical",
                    "guide-corpus",
                    "pilot_reviewed",
                    "",
                    "Do not route fever plus stiff neck, altered mental status, or non-blanching rash as routine fever or flu.",
                    "Use only when red-flag meningitis or sepsis signs are present.",
                    "Synthetic meningitis/sepsis runtime test fixture."
                }
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "required_first_action",
                1,
                "Treat fever plus meningitis or brain-warning signs as suspected meningitis or meningococcemia.",
                null
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "required_first_action",
                2,
                "Escalate urgently for emergency medical evaluation and antibiotic-capable care.",
                null
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "conditional_required_action",
                1,
                "If sepsis, shock, or a very sick child is described, prioritize immediate sepsis screening and first-hour priorities.",
                "[\"sepsis\",\"septic\",\"shock\",\"very sick\",\"very ill\",\"very unwell\",\"looks very sick\",\"low blood pressure\",\"fast breathing\"]"
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "first_action",
                1,
                "Check mental status, breathing, circulation, rash appearance, and whether the rash fades when pressed.",
                null
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "urgent_red_flag",
                1,
                "Fever with stiff neck.",
                null
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "urgent_red_flag",
                2,
                "Confusion, unusual sleepiness, or hard to wake.",
                null
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "urgent_red_flag",
                3,
                "Purple, dark, bruise-like rash or little purple dots that do not fade when pressed.",
                null
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "forbidden_advice",
                1,
                "Do not route fever plus stiff neck, altered mental status, or non-blanching rash as routine fever, flu, common rash, or home-care triage.",
                null
            );
            insertClause(
                database,
                "meningitis_sepsis_child",
                "forbidden_advice",
                2,
                "Do not wait for diagnostic confirmation before escalating suspected sepsis.",
                null
            );
            insertSource(database, "meningitis_sepsis_child", "GD-589", "sepsis-recognition-antibiotic-protocols", "Sepsis Recognition, Escalation & Empiric Antibiotic Protocols", "[\"Meningitis and Sepsis Red Flags\"]", 1);
            insertSource(database, "meningitis_sepsis_child", "GD-284", "infant-child-care", "Infant & Child Care (Birth to Age 5)", "[\"Emergency Warning Signs\"]", 0);
            insertSource(database, "meningitis_sepsis_child", "GD-298", "pediatric-emergency-medicine", "Pediatric Emergency Medicine", "[\"Meningitis Sepsis Recognition\"]", 0);
            insertSource(database, "meningitis_sepsis_child", "GD-268", "public-health-disease-surveillance", "Public Health Disease Surveillance", "[\"Urgent Disease Reporting\"]", 0);
            insertSource(database, "meningitis_sepsis_child", "GD-235", "infection-control", "Infection Control", "[\"Isolation and Infection Control\"]", 0);
        } finally {
            database.close();
        }
    }

    private static void createInfectedWoundPackDatabase(File databaseFile) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        try {
            createAnswerCardTables(database);
            database.execSQL(
                "INSERT INTO answer_cards (" +
                    "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                    "review_status, runtime_citation_policy, routine_boundary, " +
                    "acceptable_uncertain_fit, notes" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{
                    "infected_wound_spreading_infection",
                    "GD-585",
                    "wound-hygiene-infection-prevention",
                    "Wound Hygiene, Infection Prevention & Field Sanitation",
                    "high",
                    "guide-corpus",
                    "pilot_reviewed",
                    "",
                    "Routine dressing-change advice stops when redness spreads, red streaks appear, pus or odor worsens, fever rises, or mental status/breathing/heart rate changes.",
                    "If the user is unsure whether redness is spreading, advise marking the border and rechecking.",
                    "Synthetic infected wound runtime test fixture."
                }
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "required_first_action",
                1,
                "Assess whether redness is spreading, mark its edge, and recheck every 2 hours.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "required_first_action",
                2,
                "Escalate urgently if red streaking, systemic symptoms, high fever, confusion, rapid heartbeat, or rapid breathing are present.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "required_first_action",
                3,
                "Increase local wound care and cleaning frequency when infection is spreading.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "first_action",
                1,
                "Clean the wound with boiled water, soap water, or saltwater and remove visible debris when safe.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "first_action",
                2,
                "Change dirty or soaked dressings and use clean or sterile material for each dressing change.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "first_action",
                3,
                "For spreading infection, clean 2 to 3 times daily; if sepsis is developing, change dressing every 6 hours.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "urgent_red_flag",
                1,
                "Redness spreading beyond 1 inch per hour or expanding beyond a marked line.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "urgent_red_flag",
                2,
                "Red streaking moving up a limb toward the heart.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "forbidden_advice",
                1,
                "Do not reassure rapidly spreading redness, red streaking, or systemic signs as normal healing.",
                null
            );
            insertClause(
                database,
                "infected_wound_spreading_infection",
                "forbidden_advice",
                2,
                "Do not close a wound that is infected, dirty, puncture-type, or likely contaminated.",
                null
            );
            insertSource(database, "infected_wound_spreading_infection", "GD-585", "wound-hygiene-infection-prevention", "Wound Hygiene, Infection Prevention & Field Sanitation", "[\"Timeline of Infection Progression\",\"Systemic Infection and Sepsis Escalation\"]", 1);
        } finally {
            database.close();
        }
    }

    private static void createAbdominalInternalBleedingPackDatabase(File databaseFile) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        try {
            createAnswerCardTables(database);
            database.execSQL(
                "INSERT INTO answer_cards (" +
                    "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                    "review_status, runtime_citation_policy, routine_boundary, " +
                    "acceptable_uncertain_fit, notes" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{
                    "abdominal_internal_bleeding",
                    "GD-380",
                    "abdominal-internal-bleeding",
                    "Abdominal trauma or GI bleeding with shock danger signs",
                    "critical",
                    "guide-corpus",
                    "pilot_reviewed",
                    "reviewed_source_family",
                    "Do not route abdominal trauma with shock signs or GI bleeding with danger signs to routine GI care.",
                    "Use only when query text names abdominal impact or GI bleeding plus danger signs.",
                    "Synthetic abdominal internal bleeding runtime test fixture."
                }
            );
            insertClause(
                database,
                "abdominal_internal_bleeding",
                "required_first_action",
                1,
                "Treat abdominal trauma or GI bleeding with shock signs as an emergency.",
                null
            );
            insertClause(
                database,
                "abdominal_internal_bleeding",
                "first_action",
                1,
                "Keep the person still and lying down while getting emergency help.",
                null
            );
            insertClause(
                database,
                "abdominal_internal_bleeding",
                "urgent_red_flag",
                1,
                "Pale, dizzy, faint, clammy, rapid pulse, vomiting, rigid abdomen, or severe belly pain is present.",
                null
            );
            insertClause(
                database,
                "abdominal_internal_bleeding",
                "forbidden_advice",
                1,
                "Do not give food, drink, or pain medicine when internal bleeding is possible.",
                null
            );
            insertClause(
                database,
                "abdominal_internal_bleeding",
                "do_not",
                1,
                "Do not delay emergency care for black tarry stool or vomiting blood with shock signs.",
                null
            );
            insertSource(database, "abdominal_internal_bleeding", "GD-380", "abdominal-internal-bleeding", "Abdominal Internal Bleeding", "[\"Emergency escalation\",\"Abdominal trauma and GI bleed danger signs\"]", 1);
        } finally {
            database.close();
        }
    }

    private static void createAnswerCardTables(SQLiteDatabase database) {
        database.execSQL(
            "CREATE TABLE answer_cards (" +
                "card_id TEXT PRIMARY KEY, " +
                "guide_id TEXT NOT NULL, " +
                "slug TEXT, " +
                "title TEXT, " +
                "risk_tier TEXT, " +
                "evidence_owner TEXT, " +
                "review_status TEXT, " +
                "runtime_citation_policy TEXT, " +
                "routine_boundary TEXT, " +
                "acceptable_uncertain_fit TEXT, " +
                "notes TEXT" +
                ")"
        );
        database.execSQL(
            "CREATE TABLE answer_card_clauses (" +
                "card_id TEXT NOT NULL, " +
                "clause_kind TEXT NOT NULL, " +
                "ordinal INTEGER NOT NULL, " +
                "text TEXT NOT NULL, " +
                "trigger_terms_json TEXT, " +
                "PRIMARY KEY (card_id, clause_kind, ordinal)" +
                ")"
        );
        database.execSQL(
            "CREATE TABLE answer_card_sources (" +
                "card_id TEXT NOT NULL, " +
                "source_guide_id TEXT NOT NULL, " +
                "slug TEXT, " +
                "title TEXT, " +
                "sections_json TEXT NOT NULL, " +
                "is_primary INTEGER NOT NULL, " +
                "PRIMARY KEY (card_id, source_guide_id)" +
                ")"
        );
    }

    private static void insertClause(
        SQLiteDatabase database,
        String clauseKind,
        int ordinal,
        String text,
        String triggerTermsJson
    ) {
        database.execSQL(
            "INSERT INTO answer_card_clauses (" +
                "card_id, clause_kind, ordinal, text, trigger_terms_json" +
                ") VALUES (?, ?, ?, ?, ?)",
            new Object[]{
                "poisoning_unknown_ingestion",
                clauseKind,
                ordinal,
                text,
                triggerTermsJson
            }
        );
    }

    private static void insertClause(
        SQLiteDatabase database,
        String cardId,
        String clauseKind,
        int ordinal,
        String text,
        String triggerTermsJson
    ) {
        database.execSQL(
            "INSERT INTO answer_card_clauses (" +
                "card_id, clause_kind, ordinal, text, trigger_terms_json" +
                ") VALUES (?, ?, ?, ?, ?)",
            new Object[]{
                cardId,
                clauseKind,
                ordinal,
                text,
                triggerTermsJson
            }
        );
    }

    private static void insertSource(
        SQLiteDatabase database,
        String cardId,
        String sourceGuideId,
        String slug,
        String title,
        String sectionsJson,
        int primary
    ) {
        database.execSQL(
            "INSERT INTO answer_card_sources (" +
                "card_id, source_guide_id, slug, title, sections_json, is_primary" +
                ") VALUES (?, ?, ?, ?, ?, ?)",
            new Object[]{
                cardId,
                sourceGuideId,
                slug,
                title,
                sectionsJson,
                primary
            }
        );
    }
}

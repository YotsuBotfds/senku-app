package com.senku.mobile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.CURRENT_HEAD_ANSWER_CARD_COUNT;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.NON_PILOT_SAMPLE_SIZE;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.PILOT_CARD_IDS;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.SampledCard;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.assumeCurrentHeadPack;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.databaseFile;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.manifestFile;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.queryLong;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.sampledNonPilotCards;

@RunWith(AndroidJUnit4.class)
public final class AnswerCardRuntimeAllowlistCurrentHeadTest {
    @After
    public void tearDown() {
        AnswerCardRuntime.resetEnabledForTest();
    }

    @Test
    public void currentHeadRuntimeSelectionStaysLimitedToExplicitPilotCards() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File manifestFile = manifestFile(context);
        File databaseFile = databaseFile(context);

        assumeCurrentHeadPack(manifestFile, databaseFile, "runtime allowlist guard");

        try (PackRepository repository = new PackRepository(databaseFile, null);
             SQLiteDatabase database = SQLiteDatabase.openDatabase(
                 databaseFile.getAbsolutePath(),
                 null,
                 SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS
             )) {
            assertEquals(
                "installed current-head answer_cards table count should remain stable",
                CURRENT_HEAD_ANSWER_CARD_COUNT,
                queryLong(database, "SELECT COUNT(*) FROM answer_cards")
            );
            assertRuntimeDisabledDoesNotPlanPilotQueries(context, repository);
            assertPilotPlans(context, repository);
            assertSampledNonPilotCurrentHeadCardsDoNotPlan(repository, database);
        }
    }

    private static void assertRuntimeDisabledDoesNotPlanPilotQueries(Context context, PackRepository repository) {
        AnswerCardRuntime.setEnabledForTest(false);

        assertNull(
            "disabled runtime must not plan poisoning pilot query",
            AnswerCardRuntime.tryPlan(context, repository, "my child swallowed an unknown cleaner")
        );
        assertNull(
            "disabled runtime must not plan newborn pilot query",
            AnswerCardRuntime.tryPlan(context, repository, "newborn is limp, will not feed, and is hard to wake")
        );
        assertNull(
            "disabled runtime must not plan choking pilot query",
            AnswerCardRuntime.tryPlan(context, repository, "baby is choking and cannot cry or cough")
        );
        assertNull(
            "disabled runtime must not plan meningitis pilot query",
            AnswerCardRuntime.tryPlan(
                context,
                repository,
                "child has fever, stiff neck, and a purple rash that does not fade when pressed"
            )
        );
        assertNull(
            "disabled runtime must not plan infected-wound pilot query",
            AnswerCardRuntime.tryPlan(
                context,
                repository,
                "cut on my hand yesterday and now a red streak is moving up my arm"
            )
        );
        assertNull(
            "disabled runtime must not plan internal-bleeding pilot query",
            AnswerCardRuntime.tryPlan(
                context,
                repository,
                "bike handlebar hit his belly and now he is pale and dizzy"
            )
        );
    }

    private static void assertPilotPlans(Context context, PackRepository repository) {
        AnswerCardRuntime.setEnabledForTest(true);

        assertPlan(
            context,
            repository,
            "my child swallowed an unknown cleaner",
            "answer_card:poisoning_unknown_ingestion",
            "poisoning_unknown_ingestion",
            "GD-898"
        );
        assertPlan(
            context,
            repository,
            "newborn is limp, will not feed, and is hard to wake",
            "answer_card:newborn_danger_sepsis",
            "newborn_danger_sepsis",
            "GD-284"
        );
        assertPlan(
            context,
            repository,
            "baby is choking and cannot cry or cough",
            "answer_card:choking_airway_obstruction",
            "choking_airway_obstruction",
            "GD-232"
        );
        assertPlan(
            context,
            repository,
            "child has fever, stiff neck, and a purple rash that does not fade when pressed",
            "answer_card:meningitis_sepsis_child",
            "meningitis_sepsis_child",
            "GD-589"
        );
        assertPlan(
            context,
            repository,
            "cut on my hand yesterday and now a red streak is moving up my arm",
            "answer_card:infected_wound_spreading_infection",
            "infected_wound_spreading_infection",
            "GD-585"
        );
        assertPlan(
            context,
            repository,
            "bike handlebar hit his belly and now he is pale and dizzy",
            "answer_card:abdominal_internal_bleeding",
            "abdominal_internal_bleeding",
            "GD-380"
        );
    }

    private static void assertSampledNonPilotCurrentHeadCardsDoNotPlan(
        PackRepository repository,
        SQLiteDatabase database
    ) {
        List<SampledCard> sampledCards = sampledNonPilotCards(database);
        assertEquals(
            "sample should cover the expected number of non-pilot current-head cards: " + sampledCards,
            NON_PILOT_SAMPLE_SIZE,
            sampledCards.size()
        );

        for (SampledCard sampledCard : sampledCards) {
            List<AnswerCard> cards = repository.loadAnswerCardsForGuideIds(
                Collections.singleton(sampledCard.guideId),
                2
            );
            assertEquals(
                "sampled non-pilot guide should load exactly one card: " + sampledCard,
                1,
                cards.size()
            );
            AnswerCard card = cards.get(0);
            assertEquals("sampled guide should load expected card: " + sampledCard, sampledCard.cardId, card.cardId);
            assertTrue("sampled card must not be a pilot card: " + sampledCard, !PILOT_CARD_IDS.contains(card.cardId));

            assertNull(
                "non-pilot current-head card must not satisfy poisoning runtime planner: " + sampledCard,
                AnswerCardRuntime.planPoisoningAnswerCardFromCardsForTest(
                    "my child swallowed an unknown cleaner",
                    cards
                )
            );
            assertNull(
                "non-pilot current-head card must not satisfy newborn runtime planner: " + sampledCard,
                AnswerCardRuntime.planNewbornDangerSepsisAnswerCardFromCardsForTest(
                    "newborn is limp, will not feed, and is hard to wake",
                    cards
                )
            );
            assertNull(
                "non-pilot current-head card must not satisfy choking runtime planner: " + sampledCard,
                AnswerCardRuntime.planChokingAirwayObstructionAnswerCardFromCardsForTest(
                    "baby is choking and cannot cry or cough",
                    cards
                )
            );
            assertNull(
                "non-pilot current-head card must not satisfy meningitis runtime planner: " + sampledCard,
                AnswerCardRuntime.planMeningitisSepsisChildAnswerCardFromCardsForTest(
                    "child has fever, stiff neck, and a purple rash that does not fade when pressed",
                    cards
                )
            );
            assertNull(
                "non-pilot current-head card must not satisfy infected-wound runtime planner: " + sampledCard,
                AnswerCardRuntime.planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(
                    "cut on my hand yesterday and now a red streak is moving up my arm",
                    cards
                )
            );
            assertNull(
                "non-pilot current-head card must not satisfy internal-bleeding runtime planner: " + sampledCard,
                AnswerCardRuntime.planAbdominalInternalBleedingAnswerCardFromCardsForTest(
                    "bike handlebar hit his belly and now he is pale and dizzy",
                    cards
                )
            );
        }
    }

    private static void assertPlan(
        Context context,
        PackRepository repository,
        String query,
        String expectedRuleId,
        String expectedCardId,
        String expectedGuideId
    ) {
        AnswerCardRuntime.AnswerPlan plan = AnswerCardRuntime.tryPlan(context, repository, query);
        assertNotNull("pilot runtime query should plan: " + expectedCardId, plan);
        assertEquals(expectedRuleId, plan.ruleId);
        assertTrue(plan.reviewedCardMetadata.isPresent());
        assertEquals(expectedCardId, plan.reviewedCardMetadata.cardId);
        assertEquals(expectedGuideId, plan.reviewedCardMetadata.cardGuideId);
        assertEquals("pilot_reviewed", plan.reviewedCardMetadata.reviewStatus);
        assertEquals(
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            plan.reviewedCardMetadata.provenance
        );
    }
}

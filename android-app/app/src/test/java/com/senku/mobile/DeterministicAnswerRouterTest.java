package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DeterministicAnswerRouterTest {
    private static void assertDoesNotRouteToRule(String ruleId, String prompt) {
        DeterministicAnswerRouter.DeterministicAnswer answer = DeterministicAnswerRouter.match(prompt);
        assertTrue(
            "expected near miss for " + ruleId + " but got "
                + (answer == null ? "null" : answer.ruleId) + " on prompt: " + prompt,
            answer == null || !ruleId.equals(answer.ruleId)
        );
    }

    @Test
    public void matchesMetalSplinterPrompt() {
        DeterministicAnswerRouter.DeterministicAnswer answer =
            DeterministicAnswerRouter.match("i got a metal splinter in my hand");

        assertNotNull(answer);
        assertEquals("metal_splinter", answer.ruleId);
        assertTrue(answer.answerText.contains("A small metal splinter is mainly a contamination problem."));
        assertEquals(3, answer.sources.size());
    }

    @Test
    public void matchesCandlesForLightPrompt() {
        DeterministicAnswerRouter.DeterministicAnswer answer =
            DeterministicAnswerRouter.match("how do i make candles for light");

        assertNotNull(answer);
        assertEquals("candles_for_light", answer.ruleId);
        assertTrue(answer.answerText.contains("Beeswax makes the easiest clean-burning candle"));
        assertEquals(3, answer.sources.size());
    }

    @Test
    public void matchesGlassmakingPrompt() {
        DeterministicAnswerRouter.DeterministicAnswer answer =
            DeterministicAnswerRouter.match("how do i make glass");

        assertNotNull(answer);
        assertEquals("glassmaking_starter", answer.ruleId);
        assertTrue(answer.answerText.contains("simple soda-lime glass"));
        assertEquals(3, answer.sources.size());
    }

    @Test
    public void doesNotOvermatchSplinterPromptForEyeInjury() {
        DeterministicAnswerRouter.DeterministicAnswer answer =
            DeterministicAnswerRouter.match("i got a metal splinter in my eye");

        assertNull(answer);
    }

    @Test
    public void liveRuleNearMissesDoNotTriggerTargetRules() {
        String[][] genericPuncturePrompts = {
            {"generic_puncture", "my dog bit me on the foot and the wound is a puncture"},
            {"generic_puncture", "i got a deep puncture wound in my eye"},
            {"generic_puncture", "i stepped on a nail and now there is uncontrolled bleeding"},
        };
        String[][] charcoalFilterPrompts = {
            {"charcoal_sand_water_filter_starter", "how do i build a charcoal filter for drinking water"},
            {"charcoal_sand_water_filter_starter", "how do i make a sand filter for clean water"},
        };
        String[][] reusedContainerPrompts = {
            {"reused_container_water", "can i store water in an old plastic bottle that held bleach"},
            {"reused_container_water", "is a rusty steel barrel safe for drinking water"},
            {"reused_container_water", "can i store water in old milk jugs that smell like cleaning detergent"},
        };
        String[][] waterWithoutFuelPrompts = {
            {"water_without_fuel", "how do i purify water without fuel using sunlight"},
            {"water_without_fuel", "how do i purify water without fuel if the well is contaminated and two people are sick"},
            {"water_without_fuel", "how do i purify drinking water without fuel for someone who is wounded"},
        };
        String[][] fireInRainPrompts = {
            {"fire_in_rain", "how do i keep a fire going when it starts to rain"},
            {"fire_in_rain", "how can i stay warm in the rain without starting a fire"},
        };
        String[][] weldWithoutWelderPrompts = {
            {"weld_without_welder_starter", "how can i join metal pieces with screws and bolts"},
            {"weld_without_welder_starter", "how do i join two wooden boards when i don't have welding gear"},
        };
        String[][] metalSplinterPrompts = {
            {"metal_splinter", "there is a metal splinter in my eye"},
            {"metal_splinter", "metal splinter in my hand with severe bleeding"},
            {"metal_splinter", "i have a wood splinter in my hand"},
        };
        String[][] candlesPrompts = {
            {"candles_for_light", "where can i buy candles for light"},
            {"candles_for_light", "how long do candles burn for"},
        };
        String[][] glassmakingPrompts = {
            {"glassmaking_starter", "how do i make a glass from sand and clay"},
            {"glassmaking_starter", "how do i repair a cracked glass bottle"},
        };

        assertNearMisses(genericPuncturePrompts);
        assertNearMisses(charcoalFilterPrompts);
        assertNearMisses(reusedContainerPrompts);
        assertNearMisses(waterWithoutFuelPrompts);
        assertNearMisses(fireInRainPrompts);
        assertNearMisses(weldWithoutWelderPrompts);
        assertNearMisses(metalSplinterPrompts);
        assertNearMisses(candlesPrompts);
        assertNearMisses(glassmakingPrompts);
    }

    private static void assertNearMisses(String[][] prompts) {
        for (String[] entry : prompts) {
            assertDoesNotRouteToRule(entry[0], entry[1]);
        }
    }
}

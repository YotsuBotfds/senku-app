package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailAnswerBodyFormatterTest {
    @Test
    public void formatAnswerBodyRemovesEnvelopeSourcesAndWarningResiduals() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        String formatted = formatter.formatAnswerBody(
                "Answer\n" +
                "Short answer: Keep pressure on the wound [System Instruction].\n" +
                    "Limits or safety:\n" +
                    "Use clean cloth [GD-232].\n\n" +
                    "Sources used: [GD-232]"
        );

        assertContainsInOrder(
            formatted,
            "ANSWER",
            "Keep pressure on the wound.",
            "WATCH",
            "Use clean cloth [GD-232]."
        );
        assertFalse(formatted, formatted.startsWith("Answer\n"));
        assertDoesNotContain(formatted, "Short answer:");
        assertDoesNotContain(formatted, "[System Instruction]");
        assertDoesNotContain(formatted, "Sources used");
    }

    @Test
    public void formatAnswerBodyAddsSpacingBeforeKnownSections() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        String formatted = formatter.formatAnswerBody(
                "Short answer: Use stored clean water.\n" +
                    "Limits or safety:\nDo not use unknown chemicals."
        );

        assertContainsInOrder(
            formatted,
            "ANSWER",
            "Use stored clean water.",
            "WATCH",
            "Do not use unknown chemicals."
        );
        assertDoesNotContain(formatted, "Short answer:");
        assertDoesNotContain(formatted, "Limits or safety:");
    }

    @Test
    public void formatAnswerBodyPreservesReviewedCardHierarchyAndPromotesAvoidLines() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        String formatted = formatter.formatAnswerBody(
                "Steps:\n" +
                    "1. Apply steady pressure.\n" +
                    "Avoid: Do not remove deeply embedded objects.\n" +
                    "2. Wrap the wound.\n\n" +
                    "Short answer:\n" +
                    "Control bleeding first.\n\n" +
                    "Limits or safety:\n" +
                    "Watch for shock."
        );

        assertContainsInOrder(
            formatted,
            "ANSWER",
            "Control bleeding first.",
            "STEPS",
            "1. Apply steady pressure.",
            "2. Wrap the wound.",
            "WATCH",
            "Watch for shock.",
            "Avoid: Do not remove deeply embedded objects."
        );
        assertTrue(
            formatted,
            formatted.indexOf("Avoid: Do not remove deeply embedded objects.") > formatted.indexOf("WATCH")
        );
        assertFalse(
            formatted,
            formatted.substring(formatted.indexOf("STEPS"), formatted.indexOf("WATCH"))
                .contains("Avoid: Do not remove deeply embedded objects.")
        );
    }

    @Test
    public void sectionSeparatorKeepsAnswerBodyDenseForLandscapeCards() {
        assertEquals(1, DetailAnswerBodyFormatter.SECTION_SEPARATOR_NEWLINE_COUNT);
    }

    @Test
    public void formatAnswerBodyPreservesRainShelterFieldStepsInDefaultProduction() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        String formatted = formatter.formatAnswerBody(
                "ANSWER\n"
                    + "Build a ridgeline first, then drape and tension the tarp around it. "
                    + "Keep the low edge toward the weather and leave runoff a clear path away from the sheltered area.\n\n"
                    + "FIELD STEPS\n"
                    + "1. Tie a taut ridgeline between two solid anchor points.\n"
                    + "2. Drape the tarp over the line and stake or tie the windward edge low.\n"
                    + "3. Tension the corners evenly, then adjust the pitch so rain sheds instead of pooling."
        );

        assertContainsInOrder(
            formatted,
            "Build a ridgeline first",
            "STEPS",
            "1. Tie a taut ridgeline between two solid anchor points.",
            "2. Drape the tarp over the line and stake or tie the windward edge low.",
            "3. Tension the corners evenly"
        );
        assertDoesNotContain(formatted, "FIELD STEPS");
    }

    @Test
    public void formatAnswerBodyPreservesRainShelterSupportChromeInDefaultProduction() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        String formatted = formatter.formatAnswerBody(
                "ANSWER\n"
                    + "Build a ridgeline first, then drape and tension the tarp around it. "
                    + "A ridgeline is a single taut cord run between two anchor points (trees, poles, or bombproof rocks) "
                    + "at roughly chest height; it carries the load while the tarp only sheds water.\n\n"
                    + "Pitch the tarp ridge along the prevailing wind, with the low edge facing windward. "
                    + "Tension corners with prusik or taut-line hitches so the rig stays adjustable through the night.\n\n"
                    + "UNSURE FIT\n"
                    + "See GD-345 Tarp & Cord Shelters before relying on this."
        );

        assertContainsInOrder(
            formatted,
            "Build a ridgeline first",
            "A ridgeline is a single taut cord",
            "Pitch the tarp ridge along the prevailing wind",
            "UNSURE FIT",
            "See GD-345 Tarp & Cord Shelters before relying on this."
        );
    }

    private static void assertContainsInOrder(String text, String... tokens) {
        int searchStart = 0;
        for (String token : tokens) {
            int tokenIndex = text.indexOf(token, searchStart);
            assertTrue("Expected token after index " + searchStart + ": <" + token + "> in:\n" + text, tokenIndex >= 0);
            searchStart = tokenIndex + token.length();
        }
    }

    private static void assertDoesNotContain(String text, String token) {
        assertFalse("Unexpected token <" + token + "> in:\n" + text, text.contains(token));
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailAnswerBodyFormatterTest {
    @Test
    public void formatAnswerBodyRemovesEnvelopeSourcesAndWarningResiduals() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        assertEquals(
            "ANSWER\nKeep pressure on the wound.\nWATCH\nUse clean cloth [GD-232].",
            formatter.formatAnswerBody(
                "Answer\n" +
                "Short answer: Keep pressure on the wound [System Instruction].\n" +
                    "Limits or safety:\n" +
                    "Use clean cloth [GD-232].\n\n" +
                    "Sources used: [GD-232]"
            )
        );
    }

    @Test
    public void formatAnswerBodyAddsSpacingBeforeKnownSections() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        assertEquals(
            "ANSWER\nUse stored clean water.\nWATCH\nDo not use unknown chemicals.",
            formatter.formatAnswerBody(
                "Short answer: Use stored clean water.\n" +
                    "Limits or safety:\nDo not use unknown chemicals."
            )
        );
    }

    @Test
    public void formatAnswerBodyPreservesReviewedCardHierarchyAndPromotesAvoidLines() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        assertEquals(
            "ANSWER\nControl bleeding first.\n" +
                "STEPS\n1. Apply steady pressure.\n2. Wrap the wound.\n" +
                "WATCH\nWatch for shock.\nAvoid: Do not remove deeply embedded objects.",
            formatter.formatAnswerBody(
                "Steps:\n" +
                    "1. Apply steady pressure.\n" +
                    "Avoid: Do not remove deeply embedded objects.\n" +
                    "2. Wrap the wound.\n\n" +
                    "Short answer:\n" +
                    "Control bleeding first.\n\n" +
                    "Limits or safety:\n" +
                    "Watch for shock."
            )
        );
    }

    @Test
    public void sectionSeparatorKeepsAnswerBodyDenseForLandscapeCards() {
        assertEquals(1, DetailAnswerBodyFormatter.SECTION_SEPARATOR_NEWLINE_COUNT);
    }

    @Test
    public void formatAnswerBodyNormalizesRainShelterFallbackToArticleProse() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        assertEquals(
            "Build a ridgeline first, then drape and tension the tarp around it. "
                + "A ridgeline is a single taut cord run between two anchor points (trees, poles, or bombproof rocks) "
                + "at roughly chest height; it carries the load while the tarp only sheds water.\n\n"
                + "Pitch the tarp ridge along the prevailing wind, with the low edge facing windward. "
                + "Tension corners with prusik or taut-line hitches so the rig stays adjustable through the night.",
            formatter.formatAnswerBody(
                "ANSWER\n"
                    + "Build a ridgeline first, then drape and tension the tarp around it. "
                    + "Keep the low edge toward the weather and leave runoff a clear path away from the sheltered area.\n\n"
                    + "FIELD STEPS\n"
                    + "1. Tie a taut ridgeline between two solid anchor points.\n"
                    + "2. Drape the tarp over the line and stake or tie the windward edge low.\n"
                    + "3. Tension the corners evenly, then adjust the pitch so rain sheds instead of pooling."
            )
        );
    }

    @Test
    public void formatAnswerBodyKeepsCanonicalRainShelterArticleCleanWhenSupportChromeIsPresent() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        assertEquals(
            "Build a ridgeline first, then drape and tension the tarp around it. "
                + "A ridgeline is a single taut cord run between two anchor points (trees, poles, or bombproof rocks) "
                + "at roughly chest height; it carries the load while the tarp only sheds water.\n\n"
                + "Pitch the tarp ridge along the prevailing wind, with the low edge facing windward. "
                + "Tension corners with prusik or taut-line hitches so the rig stays adjustable through the night.",
            formatter.formatAnswerBody(
                "ANSWER\n"
                    + "Build a ridgeline first, then drape and tension the tarp around it. "
                    + "A ridgeline is a single taut cord run between two anchor points (trees, poles, or bombproof rocks) "
                    + "at roughly chest height; it carries the load while the tarp only sheds water.\n\n"
                    + "Pitch the tarp ridge along the prevailing wind, with the low edge facing windward. "
                    + "Tension corners with prusik or taut-line hitches so the rig stays adjustable through the night.\n\n"
                    + "UNSURE FIT\n"
                    + "See GD-345 Tarp & Cord Shelters before relying on this."
            )
        );
    }
}

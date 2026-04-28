package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailAnswerBodyFormatterTest {
    @Test
    public void formatAnswerBodyRemovesEnvelopeSourcesAndWarningResiduals() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        assertEquals(
            "ANSWER\nKeep pressure on the wound.\n\nWATCH\nUse clean cloth [GD-232].",
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
            "ANSWER\nUse stored clean water.\n\nWATCH\nDo not use unknown chemicals.",
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
            "ANSWER\nControl bleeding first.\n\n" +
                "STEPS\n1. Apply steady pressure.\n2. Wrap the wound.\n\n" +
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
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailAnswerBodyFormatterTest {
    @Test
    public void formatAnswerBodyRemovesEnvelopeSourcesAndWarningResiduals() {
        DetailAnswerBodyFormatter formatter = new DetailAnswerBodyFormatter(null);

        assertEquals(
            "Short answer: Keep pressure on the wound.\n\nLimits or safety:\nUse clean cloth [GD-232].",
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
            "Short answer: Use stored clean water.\n\nLimits or safety:\nDo not use unknown chemicals.",
            formatter.formatAnswerBody(
                "Short answer: Use stored clean water.\n" +
                    "Limits or safety:\nDo not use unknown chemicals."
            )
        );
    }
}

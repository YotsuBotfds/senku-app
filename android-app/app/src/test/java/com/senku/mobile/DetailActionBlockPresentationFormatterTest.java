package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailActionBlockPresentationFormatterTest {
    private final DetailCitationPresentationFormatter citationFormatter =
        new DetailCitationPresentationFormatter(null);

    @Test
    public void extractHighRiskActionBlockSpecsKeepsEmergencyLabelsDistinct() {
        List<DetailActionBlockPresentationFormatter.ActionBlockSpec> blocks = extract(
            "Short answer:\n" +
                "Treat this as urgent.\n\n" +
                "Steps:\n" +
                "1. Apply direct pressure with a clean cloth [GD-232].\n" +
                "2. Stabilize the area, but do not remove a deeply embedded object [GD-284].\n" +
                "3. Escalate if bleeding soaks through or red streaking appears [GD-298]."
        );

        assertEquals(3, blocks.size());
        assertBlock(
            blocks.get(0),
            DetailActionBlockPresentationFormatter.ActionBlockKind.DO_FIRST,
            "Do first",
            "Apply direct pressure with a clean cloth."
        );
        assertBlock(
            blocks.get(1),
            DetailActionBlockPresentationFormatter.ActionBlockKind.AVOID,
            "Avoid",
            "do not remove a deeply embedded object."
        );
        assertBlock(
            blocks.get(2),
            DetailActionBlockPresentationFormatter.ActionBlockKind.ESCALATE,
            "Escalate if",
            "Escalate if bleeding soaks through or red streaking appears."
        );
    }

    @Test
    public void extractHighRiskActionBlockSpecsSanitizesWarningResidualsAndGuideCitations() {
        List<DetailActionBlockPresentationFormatter.ActionBlockSpec> blocks = extract(
                "Steps:\n" +
                    "1. Keep the person still [System Instruction] [GD-232].\n" +
                    "2. Never give food or drink [Caution] [GD-284].\n" +
                    "3. Seek medical help if fever develops [GD-298]."
        );

        assertEquals("Keep the person still.", blocks.get(0).body);
        assertEquals("Never give food or drink.", blocks.get(1).body);
        assertEquals("Seek medical help if fever develops.", blocks.get(2).body);
    }

    @Test
    public void extractHighRiskActionBlockSpecsReturnsEmptyWhenNoNumberedStepsExist() {
        assertTrue(extract("Short answer: Call local emergency help when available.").isEmpty());
        assertTrue(extract(null).isEmpty());
    }

    private List<DetailActionBlockPresentationFormatter.ActionBlockSpec> extract(String formattedAnswerText) {
        return DetailActionBlockPresentationFormatter.extractHighRiskActionBlockSpecs(
            formattedAnswerText,
            text -> citationFormatter.stripInlineCitationText(text)
        );
    }

    private static void assertBlock(
        DetailActionBlockPresentationFormatter.ActionBlockSpec block,
        DetailActionBlockPresentationFormatter.ActionBlockKind kind,
        String label,
        String body
    ) {
        assertEquals(kind, block.kind);
        assertEquals(label, block.label);
        assertEquals(body, block.body);
    }
}

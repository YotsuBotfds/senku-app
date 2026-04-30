package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class AnchorSnippetFormatterTest {
    @Test
    public void resolvePrefersSessionChunkTextThenGuideChunkThenGuideBody() {
        assertEquals(
            "Bring water to a rolling boil. Let it cool covered before storing it.",
            AnchorSnippetFormatter.resolve(
                "# Water Storage\nBring water to a rolling boil.\nLet it cool covered before storing it.",
                "Use food-safe containers for treated water.",
                "Fallback paragraph."
            )
        );

        assertEquals(
            "Use food-safe containers for treated water. Keep them sealed between uses.",
            AnchorSnippetFormatter.resolve(
                "",
                "## Treated Water\nUse food-safe containers for treated water.\nKeep them sealed between uses.",
                "Fallback paragraph."
            )
        );

        assertEquals(
            "Store treated water in clean, food-safe containers and keep them sealed.",
            AnchorSnippetFormatter.resolve(
                "",
                "",
                "# Water Storage\n\nStore treated water in clean, food-safe containers and keep them sealed.\n\n" +
                    "Sanitize replacement containers before refilling."
            )
        );
    }

    @Test
    public void firstParagraphSkipsMarkdownNoiseAndStopsAtBlankLine() {
        String snippet = AnchorSnippetFormatter.firstParagraph(
            "# Heading\n\n> **Keep containers sealed** after treatment.\n" +
                "- Rotate [`stored water`](guide.md) monthly.\n\n" +
                "Second paragraph should not appear."
        );

        assertEquals("Keep containers sealed after treatment. Rotate stored water monthly.", snippet);
    }

    @Test
    public void firstParagraphReturnsEmptyWhenBodyHasNoEvidenceText() {
        assertEquals("", AnchorSnippetFormatter.firstParagraph("# Water Storage\n\n---\n\n```tip"));
    }

    @Test
    public void resolvedSnippetKeepsExistingLimitAndEllipsisBehavior() {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < 80; index++) {
            if (index > 0) {
                builder.append(' ');
            }
            builder.append("water");
        }

        String snippet = AnchorSnippetFormatter.resolve(builder.toString(), "", "");

        assertEquals(280, snippet.length());
        assertTrue(snippet.endsWith("..."));
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailGuidePresentationFormatterTest {
    @Test
    public void buildGuideBodyReturnsEmptyForNullResult() {
        assertEquals("", DetailGuidePresentationFormatter.buildGuideBody(null));
    }

    @Test
    public void buildGuideBodyUsesSanitizedBodyBeforeSnippetAndPrependsSection() {
        SearchResult result = new SearchResult(
            "Water",
            "",
            "Snippet should not win",
            "## Safe storage\nUse [clean water](manual://water) [System Instruction].",
            "GD-214",
            "Storage",
            "water",
            "guide-focus"
        );

        assertEquals(
            "Source section: Storage\n\n[[SECTION]] Safe storage\nUse clean water.",
            DetailGuidePresentationFormatter.buildGuideBody(result)
        );
    }

    @Test
    public void buildGuideBodyFallsBackToSanitizedSnippetWhenBodyBlank() {
        SearchResult result = new SearchResult(
            "Water",
            "",
            "Use **covered jars**<br>Rotate monthly.",
            "   ",
            "GD-214",
            "",
            "water",
            "guide-focus"
        );

        assertEquals("Use covered jars\nRotate monthly.", DetailGuidePresentationFormatter.buildGuideBody(result));
    }
}

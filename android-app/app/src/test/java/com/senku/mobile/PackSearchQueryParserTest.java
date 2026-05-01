package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class PackSearchQueryParserTest {
    @Test
    public void plainQueryTrimsWithoutAnchorPrior() {
        PackSearchQueryParser.ParsedSearchQuery parsed =
            PackSearchQueryParser.parse("  how do I filter water  ");

        assertEquals("how do I filter water", parsed.query);
        assertNull(parsed.anchorPrior);
    }

    @Test
    public void validAnchorPriorDirectiveParsesPayloadAndRemainingQuery() {
        PackSearchQueryParser.ParsedSearchQuery parsed =
            PackSearchQueryParser.parse("__anchor_prior__:GD-444:1:3  what next?");

        assertEquals("what next?", parsed.query);
        assertEquals("GD-444", parsed.anchorPrior.anchorGuideId);
        assertEquals(1, parsed.anchorPrior.turnsSinceAnchor);
        assertEquals(3, parsed.anchorPrior.turnCount);
    }

    @Test
    public void malformedAnchorPriorDirectiveFallsBackToOriginalQuery() {
        PackSearchQueryParser.ParsedSearchQuery parsed =
            PackSearchQueryParser.parse("__anchor_prior__:GD-444:soon:3 what next?");

        assertEquals("__anchor_prior__:GD-444:soon:3 what next?", parsed.query);
        assertNull(parsed.anchorPrior);
    }

    @Test
    public void directiveWithoutTrailingQueryFallsBackToOriginalQuery() {
        PackSearchQueryParser.ParsedSearchQuery parsed =
            PackSearchQueryParser.parse("__anchor_prior__:GD-444:1:3");

        assertEquals("__anchor_prior__:GD-444:1:3", parsed.query);
        assertNull(parsed.anchorPrior);
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class MainPresentationFormatterTest {
    @Test
    public void compactResultsHeaderShortensQueryForTightPhonePostures() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);
        List<SearchResult> results = List.of(new SearchResult("Water", "", "", ""));

        assertEquals(
            "Results for \"how do I purify...\" (1)",
            formatter.buildResultsHeader(
                "how do I purify rainwater safely in a pot",
                results,
                true,
                10,
                true,
                true,
                false
            )
        );

        assertEquals(
            "Results for \"how do I purify rainw...\" (1)",
            formatter.buildResultsHeader(
                "how do I purify rainwater safely in a pot",
                results,
                true,
                10,
                true,
                false,
                false
            )
        );
    }

    @Test
    public void failureStateCopyUsesDignifiedUserFacingText() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        assertEquals(
            "Manual is still preparing. Try again when the pack is ready.",
            formatter.buildPackUnavailableStatus()
        );
        assertEquals("Manual pack unavailable", formatter.buildPackInstallFailedHeader());
        assertEquals(
            "Answer model unavailable. Import a model or enable Host GPU.",
            formatter.buildModelUnavailableStatus()
        );
    }

    @Test
    public void noResultsHeaderHandlesQueryAndEmptySearch() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        assertEquals("No guide matches for \"solar still\"", formatter.buildNoResultsHeader(" solar still "));
        assertEquals("No guide matches", formatter.buildNoResultsHeader(" "));
        assertEquals(
            "No guide matches for \"solar still\"",
            formatter.buildResultsHeader(
                "solar still",
                Collections.emptyList(),
                true,
                10,
                false,
                false,
                false
            )
        );
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

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
}

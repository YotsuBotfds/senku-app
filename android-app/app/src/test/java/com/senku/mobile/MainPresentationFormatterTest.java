package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import com.senku.ui.primitives.BottomTabDestination;

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
    public void compactResultsHeaderKeepsCountReadableWithoutQuery() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);
        List<SearchResult> results = List.of(
            new SearchResult("Water", "", "", ""),
            new SearchResult("Fire", "", "", ""),
            new SearchResult("Shelter", "", "", ""),
            new SearchResult("Food", "", "", "")
        );

        assertEquals(
            "Results (4)",
            formatter.buildResultsHeader(
                " ",
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
    public void compactResultsHeaderPreservesSessionMarker() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);
        List<SearchResult> results = List.of(new SearchResult("Water", "", "", ""));

        assertEquals(
            "Results for \"solar still\" (1) + session",
            formatter.buildResultsHeader(
                "solar still",
                results,
                true,
                10,
                true,
                false,
                false,
                true
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
    public void mainNavigationCopyUsesConcreteLibraryAskSavedDestinations() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        assertEquals("Library", formatter.buildMainNavigationLabel(BottomTabDestination.HOME));
        assertEquals("Ask", formatter.buildMainNavigationLabel(BottomTabDestination.ASK));
        assertEquals("Saved", formatter.buildMainNavigationLabel(BottomTabDestination.PINS));
        assertEquals("Open Library", formatter.buildMainNavigationContentDescription(BottomTabDestination.HOME));
        assertEquals("Ask the manual", formatter.buildMainNavigationContentDescription(BottomTabDestination.ASK));
        assertEquals(
            "Open saved guides",
            formatter.buildMainNavigationContentDescription(BottomTabDestination.PINS)
        );
    }

    @Test
    public void savedEmptyStateNamesTheConcreteDestination() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        assertEquals(
            "No saved guides yet. This tab only shows saved guides, not threads or sections.",
            formatter.buildSavedGuidesEmptyState()
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

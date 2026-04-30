package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class MainPresentationFormatterTest {
    @Test
    public void compactResultsHeaderShortensQueryForTightPhonePostures() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);
        List<SearchResult> results = List.of(new SearchResult("Water", "", "", ""));

        String landscapeHeader = formatter.buildResultsHeader(
            "how do I purify rainwater safely in a pot",
            results,
            true,
            10,
            true,
            true,
            false
        );
        String portraitHeader = formatter.buildResultsHeader(
            "how do I purify rainwater safely in a pot",
            results,
            true,
            10,
            true,
            false,
            false
        );

        assertResultsHeaderIncludes(landscapeHeader, "1", "how do I purify");
        assertResultsHeaderIncludes(portraitHeader, "1", "how do I purify");
        assertFalse(landscapeHeader.contains("rainwater safely in a pot"));
        assertFalse(portraitHeader.contains("safely in a pot"));
        assertTrue(landscapeHeader.length() < portraitHeader.length());
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

        String header = formatter.buildResultsHeader(
            " ",
            results,
            true,
            10,
            true,
            false,
            false
        );

        assertResultsHeaderIncludes(header, "4");
        assertFalse(header.contains("\""));
    }

    @Test
    public void compactResultsHeaderPreservesSessionMarker() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);
        List<SearchResult> results = List.of(new SearchResult("Water", "", "", ""));

        String header = formatter.buildResultsHeader(
            "solar still",
            results,
            true,
            10,
            true,
            false,
            false,
            true
        );

        assertResultsHeaderIncludes(header, "1", "solar still");
        assertTrue(header.toLowerCase().contains("session"));
    }

    @Test
    public void failureStateCopyUsesDignifiedUserFacingText() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        assertContainsTokens(formatter.buildPackUnavailableStatus(), "Manual", "preparing", "ready");
        assertContainsTokens(formatter.buildPackInstallFailedHeader(), "Manual", "pack", "unavailable");
        assertContainsTokens(formatter.buildModelUnavailableStatus(), "Answer", "model", "unavailable");
    }

    @Test
    public void mainNavigationCopyUsesConcreteLibraryAskSavedDestinations() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        assertEquals("Library", formatter.buildMainNavigationLabel(BottomTabDestination.HOME));
        assertEquals("Ask", formatter.buildMainNavigationLabel(BottomTabDestination.ASK));
        assertEquals("Saved", formatter.buildMainNavigationLabel(BottomTabDestination.PINS));
        assertContainsTokens(formatter.buildMainNavigationContentDescription(BottomTabDestination.HOME), "Library");
        assertContainsTokens(formatter.buildMainNavigationContentDescription(BottomTabDestination.ASK), "Ask");
        assertContainsTokens(formatter.buildMainNavigationContentDescription(BottomTabDestination.PINS), "saved");
    }

    @Test
    public void savedEmptyStateNamesTheConcreteDestination() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        assertContainsTokens(formatter.buildSavedGuidesEmptyState(), "saved", "guides");
        assertContainsTokens(formatter.buildSavedGuidesEmptyState(), "not", "threads", "sections");
    }

    @Test
    public void noResultsHeaderHandlesQueryAndEmptySearch() {
        MainPresentationFormatter formatter = new MainPresentationFormatter(null);

        String queriedHeader = formatter.buildNoResultsHeader(" solar still ");
        String emptySearchHeader = formatter.buildNoResultsHeader(" ");
        String emptyResultsHeader = formatter.buildResultsHeader(
            "solar still",
            Collections.emptyList(),
            true,
            10,
            false,
            false,
            false
        );

        assertContainsTokens(queriedHeader, "No", "guide", "solar still");
        assertContainsTokens(emptySearchHeader, "No", "guide");
        assertFalse(emptySearchHeader.contains("\""));
        assertContainsTokens(emptyResultsHeader, "No", "guide", "solar still");
    }

    private static void assertResultsHeaderIncludes(String header, String count, String... queryTokens) {
        assertContainsTokens(header, "Results", "(" + count + ")");
        for (String queryToken : queryTokens) {
            assertTrue(header.contains(queryToken));
        }
    }

    private static void assertContainsTokens(String value, String... tokens) {
        String normalizedValue = value.toLowerCase();
        for (String token : tokens) {
            assertTrue(
                "Expected <" + value + "> to contain token <" + token + ">",
                normalizedValue.contains(token.toLowerCase())
            );
        }
    }
}

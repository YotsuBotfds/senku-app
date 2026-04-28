package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class SearchResultAdapterTest {
    @Test
    public void humanizeContentRoleStripsLegacyRolePrefixForDisplay() {
        assertEquals("Subsystem", SearchResultAdapter.humanizeContentRoleForTest("ROLE_SUBSYSTEM", 22));
        assertEquals("Planning", SearchResultAdapter.humanizeContentRoleForTest("role-planning", 22));
    }

    @Test
    public void humanizeContentRoleKeepsUnprefixedValuesReadable() {
        assertEquals("Safety", SearchResultAdapter.humanizeContentRoleForTest("safety", 22));
    }

    @Test
    public void retrievalModeLabelsUseResultHierarchyLanguage() {
        assertEquals("Best match", SearchResultAdapter.displayLabelForRetrievalModeForTest("hybrid"));
        assertEquals("Best match", SearchResultAdapter.displayLabelForRetrievalModeForTest("route-focus"));
        assertEquals("Concept match", SearchResultAdapter.displayLabelForRetrievalModeForTest("vector"));
        assertEquals("Keyword match", SearchResultAdapter.displayLabelForRetrievalModeForTest("lexical"));
        assertEquals("Related guide", SearchResultAdapter.displayLabelForRetrievalModeForTest("guide-focus"));
    }

    @Test
    public void composeBridgeUsesOrdinalRankInsteadOfSyntheticScore() {
        assertEquals("#1", SearchResultAdapter.buildRankLabelForTest(0));
        assertEquals("#4", SearchResultAdapter.buildRankLabelForTest(3));
        assertEquals("#1", SearchResultAdapter.buildRankLabelForTest(-1));
    }

    @Test
    public void composeBridgeMetadataLineUsesRoleWindowAndCategory() {
        assertEquals(
            "Role: Safety // Window: Immediate // Category: Water",
            SearchResultAdapter.buildCardMetadataLineForTest("role_safety", "immediate", "water")
        );
        assertEquals(
            "Category: Shelter",
            SearchResultAdapter.buildCardMetadataLineForTest("general", "unknown", "shelter")
        );
    }

    @Test
    public void composeBridgeMetadataLineSuppressesPlaceholderTokens() {
        assertEquals("", SearchResultAdapter.buildCardMetadataLineForTest("general", "none", "unknown"));
        assertEquals(
            "Window: Long Term",
            SearchResultAdapter.buildCardMetadataLineForTest("none", "long-term", "general")
        );
    }

    @Test
    public void cleanDisplayTextStripsMarkdownBeforeTruncating() {
        assertEquals("Boil water", SearchResultAdapter.cleanDisplayTextForTest(" **Boil** `water` ", 40));
        assertEquals("Use clean\u2026", SearchResultAdapter.cleanDisplayTextForTest("Use clean containers", 10));
    }

    @Test
    public void linkedGuideBrowseHandoffDescriptionsUseConnectionLanguage() {
        assertEquals(
            "Guide connection available: GD-214 - Water Storage",
            SearchResultAdapter.buildLinkedGuideAvailableDescriptionForTest("GD-214 - Water Storage")
        );
        assertEquals(
            "Open cross-reference guide: GD-214 - Water Storage",
            SearchResultAdapter.buildLinkedGuideOpenDescriptionForTest("GD-214 - Water Storage")
        );
    }
}

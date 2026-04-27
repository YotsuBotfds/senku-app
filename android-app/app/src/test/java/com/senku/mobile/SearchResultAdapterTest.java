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
}

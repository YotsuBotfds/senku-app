package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackFtsQueryBuilderTest {
    @Test
    public void houseFollowUpUsesPrefixMatching() {
        String ftsQuery = PackFtsQueryBuilder.buildForQuery("cabin house foundation drainage");

        assertTrue(ftsQuery.contains("foundation*"));
        assertTrue(ftsQuery.contains("drainage*"));
        assertFalse(ftsQuery.contains("\"foundation\""));
    }

    @Test
    public void hyphenatedWaterDistributionSplitsGravityFedIntoSeparatePrefixes() {
        String ftsQuery = PackFtsQueryBuilder.buildForQuery(
            "how do i design a gravity-fed water distribution system"
        );

        assertTrue(ftsQuery.contains("gravity*"));
        assertTrue(ftsQuery.contains("fed*"));
        assertFalse(ftsQuery.contains("gravity-fed*"));
    }

    @Test
    public void emptyQueryReturnsEmptyExpression() {
        assertEquals("", PackFtsQueryBuilder.buildForQuery(""));
        assertEquals("", PackFtsQueryBuilder.buildExpression(" -- "));
    }

    @Test
    public void expansionTokensAreOptional() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("wet fire");

        String withExpansions = PackFtsQueryBuilder.build(queryTerms, 6, true);
        String withoutExpansions = PackFtsQueryBuilder.build(queryTerms, 6, false);

        assertTrue(withExpansions.contains("wet*"));
        assertTrue(withExpansions.contains("fire*"));
        assertTrue(withExpansions.contains("rain*"));
        assertTrue(withExpansions.contains("tinder*"));
        assertFalse(withoutExpansions.contains("rain*"));
        assertFalse(withoutExpansions.contains("tinder*"));
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public final class QueryTextTokenPolicyTest {
    @Test
    public void splitsHyphenUnderscoreAndPunctuation() {
        assertEquals(
            Arrays.asList("gravity", "fed", "water", "system"),
            QueryTextTokenPolicy.normalizedParts("gravity-fed_water.system")
        );
    }

    @Test
    public void lowercasesTokenParts() {
        assertEquals(
            Arrays.asList("mixed", "case", "42"),
            QueryTextTokenPolicy.normalizedParts("MiXeD-CASE_42")
        );
    }

    @Test
    public void blankAndDelimiterOnlyTokensReturnNoParts() {
        assertTrue(QueryTextTokenPolicy.normalizedParts("").isEmpty());
        assertTrue(QueryTextTokenPolicy.normalizedParts(null).isEmpty());
        assertTrue(QueryTextTokenPolicy.normalizedParts(" -- __ .. ").isEmpty());
    }

    @Test
    public void preservesPartOrder() {
        assertEquals(
            Arrays.asList("first", "second", "third"),
            QueryTextTokenPolicy.normalizedParts("first_second-third")
        );
    }
}

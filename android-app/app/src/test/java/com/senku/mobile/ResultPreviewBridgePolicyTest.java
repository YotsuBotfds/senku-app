package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.junit.Test;

public final class ResultPreviewBridgePolicyTest {
    @Test
    public void nullInputReturnsEmptyGuideMap() {
        assertTrue(ResultPreviewBridgePolicy.collectGuideIds(null).isEmpty());
    }

    @Test
    public void skipsBlankAndNullGuideIds() {
        LinkedHashMap<String, String> guideIds = ResultPreviewBridgePolicy.collectGuideIds(Arrays.asList(
            result(null),
            result("  "),
            result("GD-220")
        ));

        assertEquals(1, guideIds.size());
        assertEquals("GD-220", guideIds.get("gd-220"));
    }

    @Test
    public void dedupesCaseInsensitivelyAndPreservesFirstDisplayCasing() {
        LinkedHashMap<String, String> guideIds = ResultPreviewBridgePolicy.collectGuideIds(Arrays.asList(
            result("GD-220"),
            result("gd-220"),
            result("Gd-345")
        ));

        assertEquals(2, guideIds.size());
        assertEquals("GD-220", guideIds.get("gd-220"));
        assertEquals("Gd-345", guideIds.get("gd-345"));
    }

    @Test
    public void capsAtFourGuidesInVisibleResultOrder() {
        LinkedHashMap<String, String> guideIds = ResultPreviewBridgePolicy.collectGuideIds(Arrays.asList(
            result("GD-001"),
            result("GD-002"),
            result("GD-003"),
            result("GD-004"),
            result("GD-005")
        ));

        assertEquals(Arrays.asList("gd-001", "gd-002", "gd-003", "gd-004"), Arrays.asList(guideIds.keySet().toArray()));
        assertEquals(Arrays.asList("GD-001", "GD-002", "GD-003", "GD-004"), Arrays.asList(guideIds.values().toArray()));
    }

    private static SearchResult result(String guideId) {
        return new SearchResult("Title", "", "", "", guideId, "", "", "");
    }
}

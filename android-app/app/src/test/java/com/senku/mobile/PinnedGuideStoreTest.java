package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class PinnedGuideStoreTest {
    @Test
    public void storedGuideIdsNormalizeDedupeAndKeepOrder() {
        assertEquals(
            List.of("GD-220", "GD-132", "GD-345"),
            PinnedGuideStore.guideIdsFromStoredValueForTest(" gd-220,GD-132,,gd-220, gd-345 ")
        );
    }

    @Test
    public void addingGuideMovesItToFrontAndCapsSavedGuides() {
        ArrayList<String> current = new ArrayList<>();
        for (int index = 1; index <= 13; index++) {
            current.add(String.format("GD-%03d", index));
        }

        List<String> guideIds = PinnedGuideStore.guideIdsAfterAddForTest(current, " gd-005 ");

        assertEquals(12, guideIds.size());
        assertEquals("GD-005", guideIds.get(0));
        assertEquals("GD-001", guideIds.get(1));
        assertEquals("GD-012", guideIds.get(11));
    }

    @Test
    public void storedValueCleansInvalidDuplicateAndOverflowGuideIds() {
        ArrayList<String> guideIds = new ArrayList<>();
        guideIds.add(" ");
        guideIds.add("gd-220");
        guideIds.add("GD-132");
        guideIds.add("gd-220");
        for (int index = 1; index <= 12; index++) {
            guideIds.add(String.format("gd-%03d", index));
        }

        assertEquals(
            "GD-220,GD-132,GD-001,GD-002,GD-003,GD-004,GD-005,GD-006,GD-007,GD-008,GD-009,GD-010",
            PinnedGuideStore.storedValueForGuideIdsForTest(guideIds)
        );
    }
}

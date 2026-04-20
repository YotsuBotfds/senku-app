package com.senku.mobile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackRepositoryRerankTimingTest {
    @Test
    public void rerankTimingDebugLineReportsTotalAndAverageMsPerChunk() {
        String debugLine = PackRepository.buildRerankTimingDebugLineForTest(
            "how do i build a house",
            8,
            4,
            3,
            12_500_000L
        );

        assertTrue(debugLine.contains("query=\"how do i build a house\""));
        assertTrue(debugLine.contains("topK=8"));
        assertTrue(debugLine.contains("chunks=4"));
        assertTrue(debugLine.contains("selected=3"));
        assertTrue(debugLine.contains("totalRerankMs=12.500"));
        assertTrue(debugLine.contains("avgRerankMsPerChunk=3.125"));
    }
}

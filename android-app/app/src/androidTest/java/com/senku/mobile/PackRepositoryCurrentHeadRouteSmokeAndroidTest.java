package com.senku.mobile;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class PackRepositoryCurrentHeadRouteSmokeAndroidTest {
    private static final int SEARCH_LIMIT = 8;
    private static final int CONTEXT_LIMIT = 4;
    private static final int OWNER_SEARCH_WINDOW = 4;

    @Test
    public void bundledCurrentHeadPackPreservesRainShelterOwnerLane() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "current-head rain shelter route smoke"
        );

        try (PackRepository repository = new PackRepository(pack.databaseFile, null)) {
            RouteSpec spec = new RouteSpec(
                "How do I build a simple rain shelter from tarp and cord?",
                "emergency_shelter",
                ids("GD-345"),
                ids("GD-345")
            );
            assertRainShelterOwnerParity(repository, spec);
        }
    }

    @Test
    public void bundledCurrentHeadVectorPackPreservesRainShelterOwnerLane() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "current-head rain shelter vector route smoke"
        );

        try (PackRepository repository = new PackRepository(pack.databaseFile, pack.vectorFile)) {
            assertTrue("current-head vector store should be enabled for route smoke", repository.hasVectorStore());
            RouteSpec spec = new RouteSpec(
                "How do I build a simple rain shelter from tarp and cord?",
                "emergency_shelter",
                ids("GD-345"),
                ids("GD-345")
            );
            assertRainShelterOwnerParity(repository, spec);
        }
    }

    private static void assertRainShelterOwnerParity(PackRepository repository, RouteSpec spec) {
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(spec.query);
        assertEquals(spec.query, spec.expectedStructure, metadataProfile.preferredStructureType());

        List<SearchResult> searchResults = repository.search(spec.query, SEARCH_LIMIT);
        assertFalse("search results should not be empty for " + spec.query, searchResults.isEmpty());
        assertAnyGuideWithinTopK(
            "search",
            spec.query,
            searchResults,
            spec.expectedSearchGuideIds,
            OWNER_SEARCH_WINDOW
        );

        List<SearchResult> context = repository.buildGuideAnswerContext(spec.query, searchResults, CONTEXT_LIMIT);
        assertFalse("answer context should not be empty for " + spec.query, context.isEmpty());
        assertAnyGuideWithinTopK(
            "answer context",
            spec.query,
            context,
            spec.expectedContextGuideIds,
            CONTEXT_LIMIT
        );
    }

    private static void assertAnyGuideWithinTopK(
        String surface,
        String query,
        List<SearchResult> results,
        Set<String> expectedGuideIds,
        int topK
    ) {
        int window = Math.min(topK, results.size());
        for (int index = 0; index < window; index++) {
            if (expectedGuideIds.contains(results.get(index).guideId)) {
                return;
            }
        }
        assertTrue(
            "Expected one of " + expectedGuideIds + " in top " + window + " " + surface +
                " results for \"" + query + "\" but saw: " + summarize(results, window),
            false
        );
    }

    private static String summarize(List<SearchResult> results, int limit) {
        StringBuilder summary = new StringBuilder();
        int count = Math.min(limit, results.size());
        for (int index = 0; index < count; index++) {
            SearchResult result = results.get(index);
            if (summary.length() > 0) {
                summary.append(" | ");
            }
            summary.append(index + 1)
                .append(":")
                .append(result.guideId)
                .append("/")
                .append(result.structureType)
                .append("/")
                .append(result.retrievalMode)
                .append(" \"")
                .append(result.title)
                .append("\"");
        }
        return summary.toString();
    }

    private static Set<String> ids(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static final class RouteSpec {
        final String query;
        final String expectedStructure;
        final Set<String> expectedSearchGuideIds;
        final Set<String> expectedContextGuideIds;

        RouteSpec(
            String query,
            String expectedStructure,
            Set<String> expectedSearchGuideIds,
            Set<String> expectedContextGuideIds
        ) {
            this.query = query;
            this.expectedStructure = expectedStructure;
            this.expectedSearchGuideIds = expectedSearchGuideIds;
            this.expectedContextGuideIds = expectedContextGuideIds;
        }
    }
}

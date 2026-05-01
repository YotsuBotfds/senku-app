package com.senku.mobile;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

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
public final class PackRepositoryCurrentHeadRouteParityAndroidTest {
    private static final String TAG = "SenkuRouteParity";
    private static final String TIMING_KEY = "route_parity_timing";
    private static final int SEARCH_LIMIT = 8;
    private static final int CONTEXT_LIMIT = 4;
    private static final int OWNER_SEARCH_WINDOW = 4;

    @Test
    public void bundledCurrentHeadPackPreservesHighSignalRouteOwnerLanes() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        long installStartedAt = SystemClock.elapsedRealtime();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "current-head route parity"
        );
        recordTiming("install current-head route parity pack", installStartedAt);

        long repositoryOpenedAt = SystemClock.elapsedRealtime();
        PackRepository repository = new PackRepository(pack.databaseFile, null);
        recordTiming("open current-head route parity repository", repositoryOpenedAt);
        try {
            for (RouteSpec spec : routeSpecs()) {
                assertRouteParity(repository, spec);
            }
        } finally {
            repository.close();
        }
    }

    private static List<RouteSpec> routeSpecs() {
        return Arrays.asList(
            new RouteSpec(
                "how do i build a house",
                "cabin_house",
                ids("GD-094"),
                ids("GD-094")
            ),
            new RouteSpec(
                "how do i design a gravity-fed water distribution system with storage tanks",
                "water_distribution",
                ids("GD-553", "GD-270"),
                ids("GD-553", "GD-270")
            ),
            new RouteSpec(
                "how do i make soap from animal fat and ash",
                "soapmaking",
                ids("GD-122"),
                ids("GD-122")
            ),
            new RouteSpec(
                "how do i make glass from silica sand and soda ash",
                "glassmaking",
                ids("GD-123"),
                ids("GD-123")
            ),
            new RouteSpec(
                "how do i weatherproof a cabin roof",
                "cabin_house",
                ids("GD-106"),
                ids("GD-106")
            ),
            new RouteSpec(
                "someone is stealing food from the group what do we do",
                "community_governance",
                ids("GD-626"),
                ids("GD-626", "GD-338", "GD-342")
            )
        );
    }

    private static void assertRouteParity(PackRepository repository, RouteSpec spec) {
        long routeStartedAt = SystemClock.elapsedRealtime();
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(spec.query);
        assertEquals(spec.query, spec.expectedStructure, metadataProfile.preferredStructureType());
        assertTrue(spec.query, QueryRouteProfile.fromQuery(spec.query).isRouteFocused());

        long searchStartedAt = SystemClock.elapsedRealtime();
        List<SearchResult> searchResults = repository.search(spec.query, SEARCH_LIMIT);
        recordTiming("search " + spec.expectedStructure + " route: \"" + spec.query + "\"", searchStartedAt);
        assertFalse("search results should not be empty for " + spec.query, searchResults.isEmpty());
        SearchResult ownerSearchResult = assertAnyGuideWithinTopK(
            "search",
            spec.query,
            searchResults,
            spec.expectedSearchGuideIds,
            OWNER_SEARCH_WINDOW
        );
        assertOwnerLane(spec.query, spec.expectedStructure, ownerSearchResult);

        long contextStartedAt = SystemClock.elapsedRealtime();
        List<SearchResult> context = repository.buildGuideAnswerContext(spec.query, searchResults, CONTEXT_LIMIT);
        recordTiming("context " + spec.expectedStructure + " route: \"" + spec.query + "\"", contextStartedAt);
        assertFalse("answer context should not be empty for " + spec.query, context.isEmpty());
        SearchResult ownerContextResult = assertAnyGuideWithinTopK(
            "answer context",
            spec.query,
            context,
            spec.expectedContextGuideIds,
            CONTEXT_LIMIT
        );
        assertOwnerLane(spec.query, spec.expectedStructure, ownerContextResult);
        recordTiming("total " + spec.expectedStructure + " route: \"" + spec.query + "\"", routeStartedAt);
    }

    private static SearchResult assertAnyGuideWithinTopK(
        String surface,
        String query,
        List<SearchResult> results,
        Set<String> expectedGuideIds,
        int topK
    ) {
        int window = Math.min(topK, results.size());
        for (int index = 0; index < window; index++) {
            SearchResult result = results.get(index);
            if (expectedGuideIds.contains(result.guideId)) {
                return result;
            }
        }
        assertTrue(
            "Expected one of " + expectedGuideIds + " in top " + window + " " + surface +
                " results for \"" + query + "\" but saw: " + summarize(results, window),
            false
        );
        throw new AssertionError("unreachable");
    }

    private static void assertOwnerLane(String query, String expectedStructure, SearchResult result) {
        assertTrue(
            "Expected route/context owner for \"" + query + "\" to stay in " + expectedStructure +
                " lane but saw " + summarize(result),
            expectedStructure.equals(result.structureType) || containsTopic(result.topicTags, expectedStructure)
        );
        assertTrue(
            "Expected route/context owner for \"" + query + "\" to use a focused retrieval lane but saw " +
                summarize(result),
            "route-focus".equals(result.retrievalMode) || "guide-focus".equals(result.retrievalMode)
        );
    }

    private static boolean containsTopic(String topicTags, String expectedStructure) {
        String normalized = topicTags == null ? "" : topicTags;
        for (String token : normalized.split(",")) {
            if (expectedStructure.equals(token.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String summarize(List<SearchResult> results, int limit) {
        StringBuilder summary = new StringBuilder();
        int count = Math.min(limit, results.size());
        for (int index = 0; index < count; index++) {
            if (summary.length() > 0) {
                summary.append(" | ");
            }
            summary.append(index + 1).append(":").append(summarize(results.get(index)));
        }
        return summary.toString();
    }

    private static String summarize(SearchResult result) {
        return result.guideId + "/" + result.structureType + "/" + result.retrievalMode +
            " \"" + result.title + "\"";
    }

    private static Set<String> ids(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static void recordTiming(String label, long startedAt) {
        String message = label + " took " + (SystemClock.elapsedRealtime() - startedAt) + "ms";
        Log.i(TAG, message);
        Bundle status = new Bundle();
        status.putString(TIMING_KEY, message);
        InstrumentationRegistry.getInstrumentation().sendStatus(0, status);
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

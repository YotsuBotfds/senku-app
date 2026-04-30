package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

public final class PackRouteFocusedCandidateCollectorTest {
    @Test
    public void chunkAndGuideRowsShareGatesButKeepResultShapeAndScoreOffsets() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        QueryRouteProfile.RouteSearchSpec routeSpec = houseRouteSpec();
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText(
            routeSpec.text(),
            queryTerms.routeProfile
        );
        PackRouteFocusedCandidateCollector.RouteCandidateRow row = houseRow("GD-401", "Roofing and Weatherproofing");

        LinkedHashMap<String, PackRepository.ScoredSearchResult> chunks = new LinkedHashMap<>();
        LinkedHashMap<String, PackRepository.ScoredSearchResult> guides = new LinkedHashMap<>();

        assertEquals(
            1,
            PackRouteFocusedCandidateCollector.collectRows(
                List.of(row),
                PackRouteFocusedCandidateCollector.CandidateKind.CHUNK,
                queryTerms,
                specTerms,
                routeSpec,
                chunks,
                4
            )
        );
        assertEquals(
            1,
            PackRouteFocusedCandidateCollector.collectRows(
                List.of(row),
                PackRouteFocusedCandidateCollector.CandidateKind.GUIDE,
                queryTerms,
                specTerms,
                routeSpec,
                guides,
                4
            )
        );

        PackRepository.ScoredSearchResult chunk = chunks.values().iterator().next();
        PackRepository.ScoredSearchResult guide = guides.values().iterator().next();
        assertEquals("route-focus", chunk.result.retrievalMode);
        assertEquals("Roofing and Weatherproofing", chunk.result.sectionHeading);
        assertTrue(chunk.result.subtitle.endsWith("Roofing and Weatherproofing | route-focus"));
        assertEquals("guide-focus", guide.result.retrievalMode);
        assertEquals("", guide.result.sectionHeading);
        assertTrue(guide.result.subtitle.endsWith("building | guide-focus"));
        assertEquals(chunk.score + 2, guide.score);
    }

    @Test
    public void sameSectionCandidateUsesHigherScoreThenLongerBodyTiebreaker() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        QueryRouteProfile.RouteSearchSpec routeSpec = houseRouteSpec();
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText(
            routeSpec.text(),
            queryTerms.routeProfile
        );
        PackRouteFocusedCandidateCollector.RouteCandidateRow shorter =
            houseRow("GD-401", "Roofing and Weatherproofing");
        PackRouteFocusedCandidateCollector.RouteCandidateRow longer =
            houseRow("GD-401", "Roofing and Weatherproofing", " Extra notes.");
        LinkedHashMap<String, PackRepository.ScoredSearchResult> chunks = new LinkedHashMap<>();

        assertEquals(
            1,
            PackRouteFocusedCandidateCollector.collectRows(
                List.of(shorter, longer),
                PackRouteFocusedCandidateCollector.CandidateKind.CHUNK,
                queryTerms,
                specTerms,
                routeSpec,
                chunks,
                4
            )
        );

        assertEquals(1, chunks.size());
        assertEquals(longer.body, chunks.values().iterator().next().result.body);
    }

    private static QueryRouteProfile.RouteSearchSpec houseRouteSpec() {
        return new QueryRouteProfile.RouteSearchSpec(
            "build house foundation walls roofing",
            Collections.singleton("building"),
            0
        );
    }

    private static PackRouteFocusedCandidateCollector.RouteCandidateRow houseRow(
        String guideId,
        String section
    ) {
        return houseRow(guideId, section, "");
    }

    private static PackRouteFocusedCandidateCollector.RouteCandidateRow houseRow(
        String guideId,
        String section,
        String suffix
    ) {
        return new PackRouteFocusedCandidateCollector.RouteCandidateRow(
            "Construction & Carpentry",
            guideId,
            section,
            "building",
            "Starter build sequence for site prep, foundations, walls, roofing, and weatherproofing." + suffix,
            "foundation,wall_construction,roofing,weatherproofing",
            "Build a cabin house with foundations, walls, roofing, and weatherproofing.",
            "planning",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing,weatherproofing"
        );
    }
}

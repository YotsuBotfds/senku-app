package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

public final class ReviewDemoPolicyTest {
    @Test
    public void productReviewModeIgnoresRawExtraWithoutAutomationAuthorization() {
        assertFalse(ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, false, true));
    }

    @Test
    public void productReviewModeAllowsAuthorizedDebugAutomationPath() {
        assertTrue(ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, true, true));
    }

    @Test
    public void productReviewModeDefaultsOffWithoutRequestExtra() {
        assertFalse(ReviewDemoPolicy.resolveProductReviewModeForTest(false, false, true, true));
        assertFalse(ReviewDemoPolicy.resolveProductReviewModeForTest(false, true, true, true));
    }

    @Test
    public void productReviewModeRejectsAuthorizedMarkerOutsideDebugBuilds() {
        assertFalse(ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, true, false));
    }

    @Test
    public void deniedProductReviewModeCannotEnableReviewFixtureSurfaces() {
        assertReviewFixturesDisabled(
            ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, false, true)
        );
        assertReviewFixturesDisabled(
            ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, true, false)
        );
        assertReviewFixturesDisabled(
            ReviewDemoPolicy.resolveProductReviewModeForTest(false, true, true, true)
        );
    }

    @Test
    public void unauthorizedProductReviewModeCannotEnableAnswerOrPreviewFixtures() {
        boolean productReviewMode = ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, false, true);
        List<SearchResult> adjacent = rainShelterAdjacentGuides();
        SearchResult reviewRow = guideWithSubtitle("GD-023 | survival | review");

        assertEquals(
            "",
            ReviewDemoPolicy.buildRainShelterUncertainFitAnswerBody(
                productReviewMode,
                "How do I build a simple rain shelter from tarp and cord?",
                adjacent,
                false
            )
        );
        assertEquals(
            adjacent,
            ReviewDemoPolicy.shapeRainShelterUncertainFitSources(
                productReviewMode,
                "How do I build a simple rain shelter from tarp and cord?",
                adjacent,
                false
            )
        );
        assertEquals(
            "Original recent thread",
            ReviewDemoPolicy.shapeRecentThreadLabel(productReviewMode, null, 0, "Original recent thread")
        );
        assertEquals(
            "starter \u2022 immediate \u2022 survival",
            ReviewDemoPolicy.shapeTabletPreviewMeta(
                productReviewMode,
                reviewRow,
                "starter \u2022 immediate \u2022 survival"
            )
        );
        assertEquals(
            "live snippet",
            ReviewDemoPolicy.shapeTabletPreviewBody(productReviewMode, reviewRow, "live snippet")
        );
    }

    @Test
    public void reviewHomeCategoryCountsMatchTargetMockContractWhenEnabled() {
        assertEquals(84, ReviewDemoPolicy.displayHomeCategoryCount(true, "shelter", 7));
        assertEquals(67, ReviewDemoPolicy.displayHomeCategoryCount(true, "water", 7));
        assertEquals(52, ReviewDemoPolicy.displayHomeCategoryCount(true, "fire", 7));
        assertEquals(91, ReviewDemoPolicy.displayHomeCategoryCount(true, "food", 7));
        assertEquals(73, ReviewDemoPolicy.displayHomeCategoryCount(true, "medicine", 7));
        assertEquals(119, ReviewDemoPolicy.displayHomeCategoryCount(true, "tools", 7));
        assertEquals(0, ReviewDemoPolicy.displayHomeCategoryCount(true, "communications", 7));
    }

    @Test
    public void disabledModeKeepsActualHomeCategoryCounts() {
        assertEquals(7, ReviewDemoPolicy.displayHomeCategoryCount(false, "shelter", 7));
        assertEquals(3, ReviewDemoPolicy.displayHomeCategoryCount(false, "communications", 3));
    }

    @Test
    public void homeCategoryFixtureCountsRequireReviewModeManualShellAndGuides() {
        assertTrue(ReviewDemoPolicy.shouldUseHomeCategoryFixtureCounts(true, true, true));
        assertFalse(ReviewDemoPolicy.shouldUseHomeCategoryFixtureCounts(false, true, true));
        assertFalse(ReviewDemoPolicy.shouldUseHomeCategoryFixtureCounts(true, false, true));
        assertFalse(ReviewDemoPolicy.shouldUseHomeCategoryFixtureCounts(true, true, false));
    }

    @Test
    public void disabledModeKeepsActualHomeSubtitleAndReadyStatus() {
        assertEquals(
            "271 guides in your offline field manual",
            ReviewDemoPolicy.shapeHomeSubtitle(false, 271, "271 guides in your offline field manual")
        );
        assertEquals(
            "Ready offline | 271 guides",
            ReviewDemoPolicy.shapeManualHomeStatus(false, true, "Ready offline | 271 guides")
        );
    }

    @Test
    public void reviewModeUsesTargetHomeSubtitleAndCompactReadyStatus() {
        assertEquals(
            "754 guides \u2022 12 categories \u2022 ready offline \u2022 ed. 2",
            ReviewDemoPolicy.shapeHomeSubtitle(true, 271, "271 guides in your offline field manual")
        );
        assertEquals(
            "PACK READY",
            ReviewDemoPolicy.shapeManualHomeStatus(true, true, "Ready offline | 271 guides")
        );
        assertEquals(
            "Ready offline | 271 guides",
            ReviewDemoPolicy.shapeManualHomeStatus(true, false, "Ready offline | 271 guides")
        );
    }

    @Test
    public void sourceStackDemoRequiresExplicitReviewMode() {
        assertFalse(ReviewDemoPolicy.isSourceStackDemoEnabled(false));
        assertTrue(ReviewDemoPolicy.isSourceStackDemoEnabled(true));
    }

    @Test
    public void reviewFixtureGateRequiresResolvedProductReviewMode() {
        assertFalse(ReviewDemoPolicy.shouldApplyReviewDemoFixtures(false));
        assertTrue(ReviewDemoPolicy.shouldApplyReviewDemoFixtures(true));
    }

    @Test
    public void placeholderRecentThreadFixtureRequiresReviewMode() {
        assertEquals(
            "Live placeholder",
            ReviewDemoPolicy.placeholderRecentThreadQuestion(false, 0, "Live placeholder")
        );
        assertEquals(
            "How do I build a simple rain shelter...",
            ReviewDemoPolicy.placeholderRecentThreadQuestion(true, 0, "Live placeholder")
        );
    }

    @Test
    public void answerModeRelatedGuideShapingRequiresReviewModeAndTargetGuide() {
        assertFalse(ReviewDemoPolicy.shouldShapeAnswerModeRelatedGuides(false, "GD-345"));
        assertFalse(ReviewDemoPolicy.shouldShapeAnswerModeRelatedGuides(true, "GD-027"));
        assertFalse(ReviewDemoPolicy.shouldShapeAnswerModeRelatedGuides(true, null));
        assertTrue(ReviewDemoPolicy.shouldShapeAnswerModeRelatedGuides(true, " gd-345 "));
    }

    @Test
    public void deniedProductReviewModeCannotShapeAnswerModeRelatedGuideFixtures() {
        boolean productReviewMode = ReviewDemoPolicy.resolveProductReviewModeForTest(true, true, false, true);
        ArrayList<SearchResult> relatedGuides = new ArrayList<>(Arrays.asList(
            guideWithId("Live shelter adjacent", "GD-501"),
            guideWithId("Displaced live related", "GD-109"),
            guideWithId("Live knot guide", "GD-502")
        ));

        ArrayList<SearchResult> shaped = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            productReviewMode,
            "GD-345",
            relatedGuides
        );

        assertEquals(relatedGuides, shaped);
        assertFalse(containsGuideId(shaped, "GD-027"));
    }

    @Test
    public void disabledAnswerModeRelatedGuideShapingDoesNotSynthesizeCanonicalFixtureGuide() {
        ArrayList<SearchResult> relatedGuides = new ArrayList<>(Arrays.asList(
            guideWithId("Live shelter adjacent", "GD-501"),
            guideWithId("Displaced live related", "GD-109"),
            guideWithId("Live knot guide", "GD-502")
        ));

        ArrayList<SearchResult> shaped = ReviewDemoPolicy.shapeAnswerModeRelatedGuides(
            false,
            "GD-345",
            relatedGuides
        );

        assertEquals(relatedGuides, shaped);
        assertFalse(containsGuideId(shaped, "GD-027"));
    }

    @Test
    public void rainShelterUncertainFitBackendDemoDefaultsOffUntilWiredByCaller() {
        assertFalse(ReviewDemoPolicy.isRainShelterUncertainFitBackendDemoEnabled());
    }

    @Test
    public void answerProductReviewModeDefaultsOffUntilWiredByCaller() {
        assertFalse(ReviewDemoPolicy.isAnswerProductReviewModeEnabled());
    }

    @Test
    public void reviewSearchLatencyOnlyAppliesToTargetMockQueryWhenEnabled() {
        assertEquals(
            "Search  rain shelter - 4 results \u2022 12MS",
            ReviewDemoPolicy.appendSearchLatency("Search  rain shelter - 4 results", "rain shelter", true)
        );
        assertEquals(
            "Search  water - 4 results",
            ReviewDemoPolicy.appendSearchLatency("Search  water - 4 results", "water", true)
        );
    }

    @Test
    public void disabledModeDoesNotAppendSearchLatency() {
        assertEquals(
            "Search  rain shelter - 4 results",
            ReviewDemoPolicy.appendSearchLatency("Search  rain shelter - 4 results", "rain shelter", false)
        );
    }

    @Test
    public void disabledModeDoesNotSuppressSearchRowLinkedGuideCueForMockRows() {
        assertFalse(ReviewDemoPolicy.shouldApplySearchRowReviewVisualState(false, "rain shelter"));
        assertFalse(
            ReviewDemoPolicy.shouldSuppressSearchRowLinkedGuideCue(
                false,
                "rain shelter",
                guideWithSubtitle("GD-023 | survival | review")
            )
        );
    }

    @Test
    public void enabledModeSuppressesSearchRowLinkedGuideCueOnlyForReviewSearchRows() {
        assertTrue(ReviewDemoPolicy.shouldApplySearchRowReviewVisualState(true, " rain shelter "));
        assertTrue(
            ReviewDemoPolicy.shouldSuppressSearchRowLinkedGuideCue(
                true,
                " rain shelter ",
                guideWithSubtitle("GD-023 | survival | review")
            )
        );
        assertFalse(
            ReviewDemoPolicy.shouldSuppressSearchRowLinkedGuideCue(
                true,
                "rain shelter",
                guideWithSubtitle("GD-023 | survival")
            )
        );
        assertFalse(
            ReviewDemoPolicy.shouldSuppressSearchRowLinkedGuideCue(
                true,
                "water",
                guideWithSubtitle("GD-023 | survival | review")
            )
        );
    }

    @Test
    public void reviewRainShelterSearchUsesTargetOrderAndDisplayContentWhenEnabled() {
        List<SearchResult> results = ReviewDemoPolicy.shapeSearchResults(
            "rain shelter",
            true,
            Arrays.asList(
                guideWithId("Primitive Shelter Construction Techniques", "GD-345"),
                guideWithId("Shelter Site Selection & Hazard Assessment", "GD-446"),
                guideWithId("Survival Basics & First 72 Hours", "GD-023"),
                guideWithId("Underground Shelter & Bunker Construction", "GD-873")
            ),
            null
        );

        assertEquals(4, results.size());
        assertEquals("GD-023", results.get(0).guideId);
        assertEquals("GD-027", results.get(1).guideId);
        assertEquals("GD-345", results.get(2).guideId);
        assertEquals("GD-294", results.get(3).guideId);
        assertEquals("Tarp & Cord Shelters", results.get(2).title);
        assertEquals("A simple ridgeline shelter requires only tarp, cord, and two anchor points...", results.get(2).snippet);
        assertEquals("topic", results.get(2).contentRole);
        assertEquals("immediate", results.get(2).timeHorizon);
    }

    @Test
    public void disabledModeDoesNotShapeSearchResults() {
        List<SearchResult> original = Arrays.asList(
            guideWithId("Water Storage", "GD-214"),
            guideWithId("Sand Filter", "GD-035")
        );

        assertSame(original, ReviewDemoPolicy.shapeSearchResults("rain shelter", false, original, null));
        assertSame(original, ReviewDemoPolicy.shapeSearchResults("water", true, original, null));
    }

    @Test
    public void disabledReviewModeDoesNotUseReviewFixturesForSearchOrQueryAnswerPaths() {
        List<SearchResult> searchResults = Arrays.asList(
            guideWithId("Live Rain Shelter Result", "GD-501"),
            guideWithId("Live Water Result", "GD-502")
        );
        ReviewDemoPolicy.GuideLookup forbiddenFixtureLookup = new ReviewDemoPolicy.GuideLookup() {
            @Override
            public SearchResult loadGuideById(String guideId) {
                throw new AssertionError("Review fixture lookup should stay disabled for " + guideId);
            }
        };

        assertSame(
            searchResults,
            ReviewDemoPolicy.shapeSearchResults(
                "rain shelter",
                false,
                searchResults,
                forbiddenFixtureLookup
            )
        );

        List<SearchResult> adjacent = rainShelterAdjacentGuides();
        assertEquals(
            "",
            ReviewDemoPolicy.buildRainShelterUncertainFitAnswerBody(
                false,
                "How do I build a simple rain shelter from tarp and cord?",
                adjacent,
                false
            )
        );

        List<SearchResult> shapedSources = ReviewDemoPolicy.shapeRainShelterUncertainFitSources(
            false,
            "How do I build a simple rain shelter from tarp and cord?",
            adjacent,
            false
        );
        assertEquals(adjacent, shapedSources);
        assertFalse(containsGuideId(shapedSources, "GD-220"));
        assertFalse(containsGuideId(shapedSources, "GD-132"));
    }

    @Test
    public void reviewRainShelterUncertainFitAnswerRequiresEnabledPolicy() {
        List<SearchResult> adjacent = rainShelterAdjacentGuides();

        assertEquals(
            "",
            ReviewDemoPolicy.buildRainShelterUncertainFitAnswerBody(
                false,
                "How do I build a simple rain shelter from tarp and cord?",
                adjacent,
                false
            )
        );

        String answerBody = ReviewDemoPolicy.buildRainShelterUncertainFitAnswerBody(
            true,
            "How do I build a simple rain shelter from tarp and cord?",
            adjacent,
            false
        );

        assertTrue(answerBody.startsWith("ANSWER\nBuild a ridgeline first"));
        assertTrue(answerBody.contains("\n\nFIELD STEPS\n1. Tie a taut ridgeline"));
    }

    @Test
    public void reviewRainShelterUncertainFitSourcesRequireEnabledPolicy() {
        List<SearchResult> adjacent = rainShelterAdjacentGuides();

        List<SearchResult> disabled = ReviewDemoPolicy.shapeRainShelterUncertainFitSources(
            false,
            "How do I build a simple rain shelter from tarp and cord?",
            adjacent,
            false
        );
        assertEquals(2, disabled.size());
        assertEquals("Primitive Shelter Construction Techniques", disabled.get(0).title);
        assertFalse("GD-220".equals(disabled.get(0).guideId));

        List<SearchResult> enabled = ReviewDemoPolicy.shapeRainShelterUncertainFitSources(
            true,
            "How do I build a simple rain shelter from tarp and cord?",
            adjacent,
            false
        );

        assertEquals(3, enabled.size());
        assertEquals("GD-220", enabled.get(0).guideId);
        assertEquals("Abrasives Manufacturing", enabled.get(0).title);
        assertEquals("GD-132", enabled.get(1).guideId);
        assertEquals("Foundry & Metal Casting", enabled.get(1).title);
        assertEquals("GD-345", enabled.get(2).guideId);
        assertEquals("Tarp & Cord Shelters", enabled.get(2).title);
        assertEquals("Tarp & Cord Shelters", enabled.get(2).sectionHeading);
    }

    @Test
    public void reviewManualRecentThreadsUseTargetMockExamplesWhenEnabled() {
        long fourHoursTwentyOneMinutesAgo = System.currentTimeMillis() - ((4L * 60L + 21L) * 60_000L);
        ChatSessionStore.ConversationPreview first = preview(
            "can I make a rain shelter with cord",
            "GD-345",
            "",
            ReviewedCardMetadata.empty(),
            fourHoursTwentyOneMinutesAgo
        );
        ChatSessionStore.ConversationPreview fire = preview(
            "how do I start a fire in rain",
            "GD-394",
            "deterministic-fire",
            ReviewedCardMetadata.empty(),
            fourHoursTwentyOneMinutesAgo
        );
        ChatSessionStore.ConversationPreview pot = preview(
            "boil water without a safe pot",
            "GD-094",
            "",
            new ReviewedCardMetadata("card-1", "GD-094", "reviewed", "", "reviewed_card_runtime", Collections.emptyList()),
            System.currentTimeMillis() - (25L * 60L * 60L * 1000L)
        );

        assertEquals(
            "How do I build a simple rain shelter...\nGD-345 \u2022 04:21 \u2022 UNSURE",
            ReviewDemoPolicy.shapeRecentThreadLabel(true, first, 0, "default")
        );
        assertEquals(
            "Best tinder when materials are wet\nGD-027 \u2022 04:08 \u2022 CONFIDENT",
            ReviewDemoPolicy.shapeRecentThreadLabel(true, fire, 1, "default")
        );
        assertEquals(
            "Boil water without a fire-safe pot\nGD-094 \u2022 YESTERDAY \u2022 CONFIDENT",
            ReviewDemoPolicy.shapeRecentThreadLabel(true, pot, 2, "default")
        );
    }

    @Test
    public void disabledModeKeepsRecentThreadLabelDefault() {
        assertEquals(
            "Original recent thread",
            ReviewDemoPolicy.shapeRecentThreadLabel(false, null, 0, "Original recent thread")
        );
    }

    @Test
    public void tabletPreviewTargetCopyRequiresReviewModeAndReviewRow() {
        SearchResult reviewRow = guideWithSubtitle("GD-023 | survival | review");
        SearchResult normalRow = guideWithSubtitle("GD-023 | survival");

        assertEquals(
            "starter \u2022 immediate \u2022 survival",
            ReviewDemoPolicy.shapeTabletPreviewMeta(false, reviewRow, "starter \u2022 immediate \u2022 survival")
        );
        assertEquals(
            "Starter  \u00B7  17 sections",
            ReviewDemoPolicy.shapeTabletPreviewMeta(true, reviewRow, "starter \u2022 immediate \u2022 survival")
        );
        assertEquals(
            "starter \u2022 immediate \u2022 survival",
            ReviewDemoPolicy.shapeTabletPreviewMeta(true, normalRow, "starter \u2022 immediate \u2022 survival")
        );
        assertTrue(ReviewDemoPolicy.shapeTabletPreviewBody(true, reviewRow, "live snippet")
            .startsWith("Day signaling vs. night signaling."));
        assertEquals("live snippet", ReviewDemoPolicy.shapeTabletPreviewBody(false, reviewRow, "live snippet"));
    }

    @Test
    public void productionCodeUsesReviewDemoFixtureSetOnlyThroughPolicy() throws Exception {
        Path productionSourceRoot = productionSourceRoot();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(productionSourceRoot)) {
            files
                .filter(Files::isRegularFile)
                .filter(ReviewDemoPolicyTest::isProductionSourceFile)
                .forEach(file -> collectFixtureSetReferenceViolation(productionSourceRoot, file, violations));
        }

        assertTrue(
            "ReviewDemoFixtureSet must stay isolated behind ReviewDemoPolicy: " + violations,
            violations.isEmpty()
        );
    }

    @Test
    public void productionCodeResolvesRawReviewExtrasOnlyThroughPolicy() throws Exception {
        Path productionSourceRoot = productionSourceRoot();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(productionSourceRoot)) {
            files
                .filter(Files::isRegularFile)
                .filter(ReviewDemoPolicyTest::isProductionSourceFile)
                .forEach(file -> collectRawReviewExtraViolation(productionSourceRoot, file, violations));
        }

        assertTrue(
            "Raw product-review extras must resolve only inside ReviewDemoPolicy: " + violations,
            violations.isEmpty()
        );
    }

    private static List<SearchResult> rainShelterAdjacentGuides() {
        return Arrays.asList(
            new SearchResult(
                "Primitive Shelter Construction Techniques",
                "",
                "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                "",
                "GD-345",
                "Wood Quality Evaluation for Shelter Construction",
                "survival",
                "lexical",
                "",
                "",
                "emergency_shelter",
                "foundation,weatherproofing,site_selection"
            ),
            new SearchResult(
                "Primitive Shelter Construction Techniques",
                "",
                "Pile leaves and boughs over a small frame.",
                "",
                "GD-345",
                "Debris Hut (Emergency Shelter)",
                "survival",
                "lexical"
            )
        );
    }

    private static SearchResult guideWithId(String title, String guideId) {
        return new SearchResult(
            title,
            "",
            title + " snippet",
            title + " body",
            guideId,
            "",
            "",
            "lexical",
            "",
            "",
            "",
            ""
        );
    }

    private static SearchResult guideWithSubtitle(String subtitle) {
        return new SearchResult(
            "Survival Basics & First 72 Hours",
            subtitle,
            "",
            "",
            "GD-023",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );
    }

    private static boolean containsGuideId(List<SearchResult> results, String guideId) {
        if (results == null) {
            return false;
        }
        for (SearchResult result : results) {
            if (result != null && guideId.equals(result.guideId)) {
                return true;
            }
        }
        return false;
    }

    private static void assertReviewFixturesDisabled(boolean productReviewMode) {
        List<SearchResult> original = Arrays.asList(
            guideWithId("Live Shelter Result", "GD-501"),
            guideWithId("Live Water Result", "GD-502")
        );

        assertFalse(ReviewDemoPolicy.isSourceStackDemoEnabled(productReviewMode));
        assertEquals(7, ReviewDemoPolicy.displayHomeCategoryCount(productReviewMode, "shelter", 7));
        assertEquals(
            "Search  rain shelter - 2 results",
            ReviewDemoPolicy.appendSearchLatency(
                "Search  rain shelter - 2 results",
                "rain shelter",
                productReviewMode
            )
        );
        assertFalse(ReviewDemoPolicy.shouldApplySearchRowReviewVisualState(productReviewMode, "rain shelter"));
        assertFalse(
            ReviewDemoPolicy.shouldSuppressSearchRowLinkedGuideCue(
                productReviewMode,
                "rain shelter",
                guideWithSubtitle("GD-023 | survival | review")
            )
        );
        assertSame(original, ReviewDemoPolicy.shapeSearchResults(
            "rain shelter",
            productReviewMode,
            original,
            null
        ));
    }

    private static Path productionSourceRoot() {
        Path gradleModuleRoot = Paths.get("src", "main", "java");
        if (Files.isDirectory(gradleModuleRoot)) {
            return gradleModuleRoot;
        }
        Path repoRoot = Paths.get("android-app", "app", "src", "main", "java");
        if (Files.isDirectory(repoRoot)) {
            return repoRoot;
        }
        throw new AssertionError("Unable to locate Android production source root");
    }

    private static boolean isProductionSourceFile(Path file) {
        String name = file.getFileName().toString();
        return name.endsWith(".java") || name.endsWith(".kt");
    }

    private static void collectFixtureSetReferenceViolation(
        Path sourceRoot,
        Path file,
        List<String> violations
    ) {
        String relativePath = sourceRoot.relativize(file).toString().replace('\\', '/');
        if (relativePath.equals("com/senku/mobile/ReviewDemoFixtureSet.java")
            || relativePath.equals("com/senku/mobile/ReviewDemoPolicy.java")) {
            return;
        }
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (content.contains("ReviewDemoFixtureSet")) {
                violations.add(relativePath);
            }
        } catch (IOException exception) {
            throw new AssertionError("Unable to scan " + relativePath, exception);
        }
    }

    private static void collectRawReviewExtraViolation(
        Path sourceRoot,
        Path file,
        List<String> violations
    ) {
        String relativePath = sourceRoot.relativize(file).toString().replace('\\', '/');
        if (relativePath.equals("com/senku/mobile/ReviewDemoPolicy.java")) {
            return;
        }
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (containsRawReviewExtraAccess(content)) {
                violations.add(relativePath);
            }
        } catch (IOException exception) {
            throw new AssertionError("Unable to scan " + relativePath, exception);
        }
    }

    private static boolean containsRawReviewExtraAccess(String content) {
        return content.contains(".hasExtra(EXTRA_PRODUCT_REVIEW_MODE)")
            || content.contains(".getBooleanExtra(EXTRA_PRODUCT_REVIEW_MODE")
            || content.contains(".getStringExtra(EXTRA_PRODUCT_REVIEW_AUTOMATION_AUTH)")
            || content.contains(".getStringExtra(ReviewDemoPolicy.EXTRA_PRODUCT_REVIEW_AUTOMATION_AUTH)")
            || content.contains("\"product_review_mode\"")
            || content.contains("\"com.senku.mobile.extra.PRODUCT_REVIEW_AUTOMATION_AUTH\"");
    }

    private static ChatSessionStore.ConversationPreview preview(
        String question,
        String guideId,
        String ruleId,
        ReviewedCardMetadata metadata,
        long lastActivityEpoch
    ) {
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            question,
            "",
            "answer",
            Collections.emptyList(),
            Arrays.asList(guideWithId(guideId + " title", guideId)),
            ruleId,
            metadata,
            null,
            lastActivityEpoch
        );
        return new ChatSessionStore.ConversationPreview("conversation-" + guideId, turn, 1, lastActivityEpoch);
    }
}

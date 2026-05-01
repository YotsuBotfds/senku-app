package com.senku.mobile;

import android.net.Uri;
import android.os.Bundle;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

final class ScriptedPromptHarnessContract {
    final boolean reviewedCardRuntimeEnabled;
    final String expectedAnswerSurfaceLabel;
    final String expectedDetailRoute;
    final List<String> forbiddenAnswerSurfaceLabels;
    final String expectedRuleId;
    final String expectedSourceGuideId;
    final String expectedReviewedCardId;
    final String expectedReviewedCardGuideId;
    final String expectedReviewedCardReviewStatus;
    final List<String> expectedBodyFragments;
    final List<String> expectedReviewedCardSourceGuideIds;
    final List<String> expectedReviewedCardSupportFragments;
    final List<String> expectedReviewedCardSupportLineFragments;
    final boolean assertRecentThreadReviewedCardMetadata;

    ScriptedPromptHarnessContract(Bundle args) {
        reviewedCardRuntimeEnabled = parseBooleanArg(args, "scriptedEnableReviewedCardRuntime");
        expectedAnswerSurfaceLabel =
            Uri.decode(safe(args.getString("scriptedExpectedAnswerSurfaceLabel"))).trim();
        expectedDetailRoute =
            normalizeExpectedDetailRoute(Uri.decode(safe(args.getString("scriptedExpectedDetailRoute"))));
        forbiddenAnswerSurfaceLabels = parseDelimitedArg(args, "scriptedForbiddenAnswerSurfaceLabels");
        String legacyForbiddenAnswerSurfaceLabel =
            Uri.decode(safe(args.getString("scriptedForbiddenAnswerSurfaceLabel"))).trim();
        if (!legacyForbiddenAnswerSurfaceLabel.isEmpty()) {
            forbiddenAnswerSurfaceLabels.add(legacyForbiddenAnswerSurfaceLabel);
        }
        expectedRuleId = Uri.decode(safe(args.getString("scriptedExpectedRuleId"))).trim();
        expectedSourceGuideId = Uri.decode(safe(args.getString("scriptedExpectedSourceGuideId"))).trim();
        expectedReviewedCardId = Uri.decode(safe(args.getString("scriptedExpectedReviewedCardId"))).trim();
        expectedReviewedCardGuideId =
            Uri.decode(safe(args.getString("scriptedExpectedReviewedCardGuideId"))).trim();
        expectedReviewedCardReviewStatus =
            Uri.decode(safe(args.getString("scriptedExpectedReviewedCardReviewStatus"))).trim();
        expectedBodyFragments = parseDelimitedArg(args, "scriptedExpectedBodyContains");
        expectedReviewedCardSourceGuideIds =
            parseDelimitedArg(args, "scriptedExpectedReviewedCardSourceGuideIds");
        expectedReviewedCardSupportFragments =
            parseDelimitedArg(args, "scriptedExpectedReviewedCardSupportFragments");
        expectedReviewedCardSupportLineFragments = buildReviewedCardSupportLineFragments(
            expectedReviewedCardId,
            expectedReviewedCardGuideId,
            expectedReviewedCardReviewStatus,
            expectedReviewedCardSourceGuideIds,
            expectedReviewedCardSupportFragments
        );
        assertRecentThreadReviewedCardMetadata =
            parseBooleanArg(args, "scriptedAssertRecentThreadReviewedCardMetadata");
    }

    void assertReviewedEvidenceExpectationIsClosed() {
        if (!"REVIEWED EVIDENCE".equalsIgnoreCase(safe(expectedAnswerSurfaceLabel).trim())) {
            return;
        }
        Assert.assertFalse(
            "REVIEWED EVIDENCE expectation must assert answer_card rule id",
            safe(expectedRuleId).trim().isEmpty()
        );
        Assert.assertTrue(
            "REVIEWED EVIDENCE expectation must use answer_card rule id",
            ReviewedCardMetadata.isAnswerCardRuleId(expectedRuleId)
        );
        Assert.assertFalse(
            "REVIEWED EVIDENCE expectation must assert primary source guide id",
            safe(expectedSourceGuideId).trim().isEmpty()
        );
        Assert.assertFalse(
            "REVIEWED EVIDENCE expectation must assert reviewed card id",
            safe(expectedReviewedCardId).trim().isEmpty()
        );
        Assert.assertFalse(
            "REVIEWED EVIDENCE expectation must assert reviewed card guide id",
            safe(expectedReviewedCardGuideId).trim().isEmpty()
        );
        Assert.assertFalse(
            "REVIEWED EVIDENCE expectation must assert reviewed card review status",
            safe(expectedReviewedCardReviewStatus).trim().isEmpty()
        );
        Assert.assertTrue(
            "REVIEWED EVIDENCE expectation must assert at least one cited reviewed source guide id",
            expectedReviewedCardSourceGuideIds != null && !expectedReviewedCardSourceGuideIds.isEmpty()
        );
        Assert.assertTrue(
            "REVIEWED EVIDENCE expectation must expose reviewed-card support line fragments",
            expectedReviewedCardSupportLineFragments != null
                && !expectedReviewedCardSupportLineFragments.isEmpty()
        );
    }

    private static List<String> buildReviewedCardSupportLineFragments(
        String cardId,
        String cardGuideId,
        String reviewStatus,
        List<String> sourceGuideIds,
        List<String> explicitFragments
    ) {
        ArrayList<String> fragments = new ArrayList<>();
        addSupportLineFragment(fragments, "CARD", cardId);
        addSupportLineFragment(fragments, "REVIEW", humanizeReviewedCardToken(reviewStatus));
        addSupportLineFragment(fragments, "CARD GUIDE", cardGuideId);
        if (sourceGuideIds != null && !sourceGuideIds.isEmpty()) {
            addSupportLineFragment(fragments, "REVIEWED SOURCES", String.join(", ", sourceGuideIds));
        }
        if (explicitFragments != null) {
            for (String fragment : explicitFragments) {
                String value = safe(fragment).trim();
                if (!value.isEmpty()) {
                    fragments.add(value);
                }
            }
        }
        return fragments;
    }

    private static void addSupportLineFragment(List<String> fragments, String label, String value) {
        String trimmed = safe(value).trim();
        if (!trimmed.isEmpty()) {
            fragments.add(label + "  " + trimmed);
        }
    }

    private static String humanizeReviewedCardToken(String token) {
        return safe(token).trim().replace('_', ' ');
    }

    private static boolean parseBooleanArg(Bundle args, String key) {
        return "true".equalsIgnoreCase(safe(args.getString(key)).trim());
    }

    private static String normalizeExpectedDetailRoute(String rawRoute) {
        String route = safe(rawRoute).trim().toLowerCase(java.util.Locale.US);
        if (route.isEmpty()) {
            return "";
        }
        Assert.assertTrue(
            "scriptedExpectedDetailRoute must be one of answer, guide, emergency",
            "answer".equals(route) || "guide".equals(route) || "emergency".equals(route)
        );
        return route;
    }

    private static List<String> parseDelimitedArg(Bundle args, String key) {
        String raw = Uri.decode(safe(args.getString(key))).trim();
        ArrayList<String> values = new ArrayList<>();
        if (raw.isEmpty()) {
            return values;
        }
        for (String part : raw.split("\\|")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

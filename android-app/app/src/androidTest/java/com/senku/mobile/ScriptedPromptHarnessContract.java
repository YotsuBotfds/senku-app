package com.senku.mobile;

import android.net.Uri;
import android.os.Bundle;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

final class ScriptedPromptHarnessContract {
    final boolean reviewedCardRuntimeEnabled;
    final String expectedAnswerSurfaceLabel;
    final List<String> forbiddenAnswerSurfaceLabels;
    final String expectedRuleId;
    final String expectedSourceGuideId;
    final String expectedReviewedCardId;
    final String expectedReviewedCardGuideId;
    final String expectedReviewedCardReviewStatus;
    final List<String> expectedBodyFragments;
    final List<String> expectedReviewedCardSourceGuideIds;
    final boolean assertRecentThreadReviewedCardMetadata;

    ScriptedPromptHarnessContract(Bundle args) {
        reviewedCardRuntimeEnabled = parseBooleanArg(args, "scriptedEnableReviewedCardRuntime");
        expectedAnswerSurfaceLabel =
            Uri.decode(safe(args.getString("scriptedExpectedAnswerSurfaceLabel"))).trim();
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
            safe(expectedRuleId).trim().startsWith("answer_card:")
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
    }

    private static boolean parseBooleanArg(Bundle args, String key) {
        return "true".equalsIgnoreCase(safe(args.getString(key)).trim());
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

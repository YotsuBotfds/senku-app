package com.senku.mobile;

import android.net.Uri;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public final class ScriptedPromptHarnessContractTest {
    @Test
    public void scalarArgsAreUrlDecodedAndTrimmed() {
        Bundle args = new Bundle();
        args.putString("scriptedExpectedAnswerSurfaceLabel", Uri.encode("  REVIEWED EVIDENCE  "));
        args.putString("scriptedExpectedRuleId", Uri.encode("  answer_card:poisoning_unknown_ingestion  "));
        args.putString("scriptedExpectedSourceGuideId", Uri.encode("  GD-101  "));
        args.putString("scriptedExpectedReviewedCardId", Uri.encode("  poisoning_unknown_ingestion  "));
        args.putString("scriptedExpectedReviewedCardGuideId", Uri.encode("  GD-202  "));
        args.putString("scriptedExpectedReviewedCardReviewStatus", Uri.encode("  pilot_reviewed  "));

        ScriptedPromptHarnessContract contract = new ScriptedPromptHarnessContract(args);

        Assert.assertEquals("REVIEWED EVIDENCE", contract.expectedAnswerSurfaceLabel);
        Assert.assertEquals("answer_card:poisoning_unknown_ingestion", contract.expectedRuleId);
        Assert.assertEquals("GD-101", contract.expectedSourceGuideId);
        Assert.assertEquals("poisoning_unknown_ingestion", contract.expectedReviewedCardId);
        Assert.assertEquals("GD-202", contract.expectedReviewedCardGuideId);
        Assert.assertEquals("pilot_reviewed", contract.expectedReviewedCardReviewStatus);
    }

    @Test
    public void pipeDelimitedArgsAreDecodedTrimmedAndIgnoreEmptyParts() {
        Bundle args = new Bundle();
        args.putString(
            "scriptedForbiddenAnswerSurfaceLabels",
            Uri.encode("  DRAFT | | AUTO-GENERATED | LOW CONFIDENCE  ")
        );
        args.putString(
            "scriptedExpectedBodyContains",
            Uri.encode("  Call poison control | Keep the child with an adult | Avoid vomiting  ")
        );
        args.putString(
            "scriptedExpectedReviewedCardSourceGuideIds",
            Uri.encode("  GD-898 | GD-901 | | GD-902  ")
        );
        args.putString(
            "scriptedExpectedReviewedCardSupportFragments",
            Uri.encode("  CARD  poisoning_unknown_ingestion | REVIEW  pilot reviewed | | CARD GUIDE  GD-898  ")
        );

        ScriptedPromptHarnessContract contract = new ScriptedPromptHarnessContract(args);

        Assert.assertEquals(
            Arrays.asList("DRAFT", "AUTO-GENERATED", "LOW CONFIDENCE"),
            contract.forbiddenAnswerSurfaceLabels
        );
        Assert.assertEquals(
            Arrays.asList("Call poison control", "Keep the child with an adult", "Avoid vomiting"),
            contract.expectedBodyFragments
        );
        Assert.assertEquals(
            Arrays.asList("GD-898", "GD-901", "GD-902"),
            contract.expectedReviewedCardSourceGuideIds
        );
        Assert.assertEquals(
            Arrays.asList(
                "CARD  poisoning_unknown_ingestion",
                "REVIEW  pilot reviewed",
                "CARD GUIDE  GD-898"
            ),
            contract.expectedReviewedCardSupportFragments
        );
    }

    @Test
    public void legacyForbiddenAnswerSurfaceLabelAppendsToMultiLabelList() {
        Bundle args = new Bundle();
        args.putString("scriptedForbiddenAnswerSurfaceLabels", Uri.encode("DRAFT | AUTO-GENERATED"));
        args.putString("scriptedForbiddenAnswerSurfaceLabel", Uri.encode("  LEGACY LABEL  "));

        ScriptedPromptHarnessContract contract = new ScriptedPromptHarnessContract(args);

        Assert.assertEquals(
            Arrays.asList("DRAFT", "AUTO-GENERATED", "LEGACY LABEL"),
            contract.forbiddenAnswerSurfaceLabels
        );
    }

    @Test
    public void booleanArgsParseTrueCaseInsensitivelyAndFalseOtherwise() {
        Bundle args = new Bundle();
        args.putString("scriptedEnableReviewedCardRuntime", "TrUe");
        args.putString("scriptedAssertRecentThreadReviewedCardMetadata", "TRUE");

        ScriptedPromptHarnessContract trueContract = new ScriptedPromptHarnessContract(args);

        Assert.assertTrue(trueContract.reviewedCardRuntimeEnabled);
        Assert.assertTrue(trueContract.assertRecentThreadReviewedCardMetadata);

        Bundle falseArgs = new Bundle();
        falseArgs.putString("scriptedEnableReviewedCardRuntime", "yes");
        falseArgs.putString("scriptedAssertRecentThreadReviewedCardMetadata", "0");

        ScriptedPromptHarnessContract falseContract = new ScriptedPromptHarnessContract(falseArgs);
        ScriptedPromptHarnessContract missingContract = new ScriptedPromptHarnessContract(new Bundle());

        Assert.assertFalse(falseContract.reviewedCardRuntimeEnabled);
        Assert.assertFalse(falseContract.assertRecentThreadReviewedCardMetadata);
        Assert.assertFalse(missingContract.reviewedCardRuntimeEnabled);
        Assert.assertFalse(missingContract.assertRecentThreadReviewedCardMetadata);
    }

    @Test
    public void reviewedEvidenceGuardPassesForCompleteContract() {
        ScriptedPromptHarnessContract contract =
            new ScriptedPromptHarnessContract(completeReviewedEvidenceArgs());

        contract.assertReviewedEvidenceExpectationIsClosed();
    }

    @Test
    public void reviewedCardSupportLineFragmentsAreDerivedFromMetadataContract() {
        ScriptedPromptHarnessContract contract =
            new ScriptedPromptHarnessContract(completeReviewedEvidenceArgs());

        Assert.assertEquals(
            Arrays.asList(
                "CARD  poisoning_unknown_ingestion",
                "REVIEW  pilot reviewed",
                "CARD GUIDE  GD-898",
                "REVIEWED SOURCES  GD-898, GD-901"
            ),
            contract.expectedReviewedCardSupportLineFragments
        );
    }

    @Test
    public void explicitReviewedCardSupportFragmentsAppendToDerivedLines() {
        Bundle args = completeReviewedEvidenceArgs();
        args.putString(
            "scriptedExpectedReviewedCardSupportFragments",
            Uri.encode("Support line from reviewed card body | Follow-up support sentence")
        );

        ScriptedPromptHarnessContract contract = new ScriptedPromptHarnessContract(args);

        Assert.assertEquals(
            Arrays.asList(
                "CARD  poisoning_unknown_ingestion",
                "REVIEW  pilot reviewed",
                "CARD GUIDE  GD-898",
                "REVIEWED SOURCES  GD-898, GD-901",
                "Support line from reviewed card body",
                "Follow-up support sentence"
            ),
            contract.expectedReviewedCardSupportLineFragments
        );
    }

    @Test
    public void reviewedEvidenceGuardFailsForMissingRequiredFields() {
        assertGuardFailsWithMissingField(
            "scriptedExpectedRuleId",
            "REVIEWED EVIDENCE expectation must assert answer_card rule id"
        );
        assertGuardFailsWithMissingField(
            "scriptedExpectedSourceGuideId",
            "REVIEWED EVIDENCE expectation must assert primary source guide id"
        );
        assertGuardFailsWithMissingField(
            "scriptedExpectedReviewedCardId",
            "REVIEWED EVIDENCE expectation must assert reviewed card id"
        );
        assertGuardFailsWithMissingField(
            "scriptedExpectedReviewedCardGuideId",
            "REVIEWED EVIDENCE expectation must assert reviewed card guide id"
        );
        assertGuardFailsWithMissingField(
            "scriptedExpectedReviewedCardReviewStatus",
            "REVIEWED EVIDENCE expectation must assert reviewed card review status"
        );
        assertGuardFailsWithMissingField(
            "scriptedExpectedReviewedCardSourceGuideIds",
            "REVIEWED EVIDENCE expectation must assert at least one cited reviewed source guide id"
        );
    }

    @Test
    public void reviewedEvidenceGuardFailsForNonAnswerCardRuleId() {
        Bundle args = completeReviewedEvidenceArgs();
        args.putString("scriptedExpectedRuleId", "deterministic:poisoning_unknown_ingestion");

        AssertionError error = expectReviewedEvidenceGuardFailure(args);

        Assert.assertTrue(
            error.getMessage(),
            error.getMessage().contains("REVIEWED EVIDENCE expectation must use answer_card rule id")
        );
    }

    private static void assertGuardFailsWithMissingField(String fieldName, String expectedMessage) {
        Bundle args = completeReviewedEvidenceArgs();
        args.remove(fieldName);

        AssertionError error = expectReviewedEvidenceGuardFailure(args);

        Assert.assertTrue(error.getMessage(), error.getMessage().contains(expectedMessage));
    }

    private static AssertionError expectReviewedEvidenceGuardFailure(Bundle args) {
        try {
            new ScriptedPromptHarnessContract(args).assertReviewedEvidenceExpectationIsClosed();
            Assert.fail("Expected reviewed-evidence guard to fail");
            return null;
        } catch (AssertionError error) {
            return error;
        }
    }

    private static Bundle completeReviewedEvidenceArgs() {
        Bundle args = new Bundle();
        args.putString("scriptedExpectedAnswerSurfaceLabel", Uri.encode("REVIEWED EVIDENCE"));
        args.putString("scriptedExpectedRuleId", Uri.encode("answer_card:poisoning_unknown_ingestion"));
        args.putString("scriptedExpectedSourceGuideId", Uri.encode("GD-898"));
        args.putString("scriptedExpectedReviewedCardId", Uri.encode("poisoning_unknown_ingestion"));
        args.putString("scriptedExpectedReviewedCardGuideId", Uri.encode("GD-898"));
        args.putString("scriptedExpectedReviewedCardReviewStatus", Uri.encode("pilot_reviewed"));
        args.putString("scriptedExpectedReviewedCardSourceGuideIds", Uri.encode("GD-898|GD-901"));
        return args;
    }
}

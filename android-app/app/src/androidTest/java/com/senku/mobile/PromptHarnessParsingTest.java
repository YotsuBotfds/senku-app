package com.senku.mobile;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class PromptHarnessParsingTest {
    @Test
    public void guideHelpersExtractStableIdentifiersAndShortTitleFragments() {
        Assert.assertEquals("GD-123", PromptHarnessParsing.extractGuideId("source gd-123: shelter basics"));
        Assert.assertEquals(
            "Emergency shelter planni",
            PromptHarnessParsing.extractGuideTitleFragment("Material 01: GD-123 - Emergency shelter planning checklist")
        );
        Assert.assertEquals("", PromptHarnessParsing.extractGuideTitleFragment("GD-123 -"));
    }

    @Test
    public void countHelpersParseVisibleLabelsConservatively() {
        Assert.assertEquals(2, PromptHarnessParsing.extractTurnCount("2 turns"));
        Assert.assertEquals(0, PromptHarnessParsing.extractTurnCount("turns"));
        Assert.assertEquals(37, PromptHarnessParsing.extractLeadingInteger("37 guides"));
        Assert.assertEquals(0, PromptHarnessParsing.extractLeadingInteger("no guides"));
    }

    @Test
    public void summaryClipMatchesHarnessComparisonLimit() {
        Assert.assertEquals("short summary", PromptHarnessParsing.clipExpectedSummary(" short summary "));
        Assert.assertEquals(
            "this expected summary is",
            PromptHarnessParsing.clipExpectedSummary("this expected summary is longer than the comparison limit")
        );
    }
}

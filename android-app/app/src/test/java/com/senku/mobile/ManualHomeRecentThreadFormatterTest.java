package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public final class ManualHomeRecentThreadFormatterTest {
    private static final long NOW = 1_800_000_000_000L;

    @Test
    public void buildLabelUsesClippedQuestionGuideTimeAndUnsureConfidence() {
        ChatSessionStore.ConversationPreview preview = preview(
            "can I make a rain shelter with cord and two poles",
            "GD-345",
            "",
            ReviewedCardMetadata.empty(),
            NOW - ((4L * 60L + 21L) * 60_000L)
        );

        assertEquals(
            "can I make a rain shelter with...\nGD-345 \u2022 04:21 \u2022 UNSURE",
            ManualHomeRecentThreadFormatter.buildLabel(preview, NOW)
        );
    }

    @Test
    public void buildMetaUsesRuleIdOrReviewedCardMetadataForConfidentLabel() {
        ChatSessionStore.ConversationPreview rulePreview = preview(
            "how do I keep tinder dry",
            "GD-027",
            "deterministic-fire",
            ReviewedCardMetadata.empty(),
            NOW - (7L * 60_000L)
        );
        ChatSessionStore.ConversationPreview cardPreview = preview(
            "how do I brace a wall",
            "GD-094",
            "",
            new ReviewedCardMetadata("card-1", "GD-094", "reviewed", "", "reviewed_card_runtime", Collections.emptyList()),
            NOW - (25L * 60L * 60L * 1000L)
        );

        assertEquals("GD-027 \u2022 00:07 \u2022 CONFIDENT", ManualHomeRecentThreadFormatter.buildMeta(rulePreview, NOW));
        assertEquals("GD-094 \u2022 YESTERDAY \u2022 CONFIDENT", ManualHomeRecentThreadFormatter.buildMeta(cardPreview, NOW));
    }

    @Test
    public void buildMetaOmitsMissingGuideAndTimeButKeepsConfidence() {
        ChatSessionStore.ConversationPreview preview = preview(
            "what now",
            "",
            "",
            ReviewedCardMetadata.empty(),
            0L
        );

        assertEquals("UNSURE", ManualHomeRecentThreadFormatter.buildMeta(preview, NOW));
    }

    @Test
    public void timeLabelUsesDayCountAfterYesterdayAndClampsFutureTimes() {
        assertEquals("3D", ManualHomeRecentThreadFormatter.buildTimeLabel(NOW - (73L * 60L * 60L * 1000L), NOW));
        assertEquals("00:00", ManualHomeRecentThreadFormatter.buildTimeLabel(NOW + 60_000L, NOW));
    }

    @Test
    public void nullPreviewFormatsAsEmptyLabelAndMeta() {
        assertEquals("", ManualHomeRecentThreadFormatter.buildLabel(null, NOW));
        assertEquals("", ManualHomeRecentThreadFormatter.buildMeta(null, NOW));
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
            guideId == null || guideId.isEmpty()
                ? Collections.emptyList()
                : Arrays.asList(guideWithId(guideId + " title", guideId)),
            ruleId,
            metadata,
            null,
            lastActivityEpoch
        );
        return new ChatSessionStore.ConversationPreview("conversation-" + guideId, turn, 1, lastActivityEpoch);
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
}

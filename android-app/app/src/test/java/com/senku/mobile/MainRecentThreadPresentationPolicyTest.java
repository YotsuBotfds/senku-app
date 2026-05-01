package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

public final class MainRecentThreadPresentationPolicyTest {
    @Test
    public void manualHomeRowsUseTwoLinesCompactPaddingAndStableHeight() {
        MainRecentThreadPresentationPolicy.ButtonPresentation firstRow =
            MainRecentThreadPresentationPolicy.resolveButtonPresentation(false, true, true, 0);
        MainRecentThreadPresentationPolicy.ButtonPresentation secondRow =
            MainRecentThreadPresentationPolicy.resolveButtonPresentation(false, true, true, 1);

        assertEquals(12, firstRow.horizontalPaddingDp);
        assertEquals(8, firstRow.verticalPaddingDp);
        assertEquals(2, firstRow.maxLines);
        assertEquals(70, firstRow.minimumHeightDp);
        assertEquals(0, firstRow.topMarginDp);
        assertEquals(8, secondRow.topMarginDp);
    }

    @Test
    public void tabletRowsKeepManualDensityWithTabletGap() {
        MainRecentThreadPresentationPolicy.ButtonPresentation presentation =
            MainRecentThreadPresentationPolicy.resolveButtonPresentation(true, true, false, 1);

        assertEquals(9, presentation.horizontalPaddingDp);
        assertEquals(9, presentation.verticalPaddingDp);
        assertEquals(2, presentation.maxLines);
        assertEquals(70, presentation.minimumHeightDp);
        assertEquals(10, presentation.topMarginDp);
    }

    @Test
    public void legacyRowsKeepThreeLinesExceptCompactPhones() {
        MainRecentThreadPresentationPolicy.ButtonPresentation regular =
            MainRecentThreadPresentationPolicy.resolveButtonPresentation(false, false, false, 1);
        MainRecentThreadPresentationPolicy.ButtonPresentation compact =
            MainRecentThreadPresentationPolicy.resolveButtonPresentation(false, false, true, 1);

        assertEquals(3, regular.maxLines);
        assertEquals(12, regular.horizontalPaddingDp);
        assertEquals(10, regular.verticalPaddingDp);
        assertEquals(0, regular.minimumHeightDp);
        assertEquals(8, regular.topMarginDp);
        assertEquals(2, compact.maxLines);
        assertEquals(10, compact.horizontalPaddingDp);
        assertEquals(8, compact.verticalPaddingDp);
        assertEquals(6, compact.topMarginDp);
    }

    @Test
    public void removeHintOnlyAppliesToAddressableRecentThreads() {
        assertTrue(MainRecentThreadPresentationPolicy.isLongPressRemoveHintEligible(preview("conversation-1")));
        assertFalse(MainRecentThreadPresentationPolicy.isLongPressRemoveHintEligible(preview(" ")));
        assertFalse(MainRecentThreadPresentationPolicy.isLongPressRemoveHintEligible(null));
    }

    @Test
    public void contentDescriptionAddsRemoveHintWithoutDuplicatingExistingCopy() {
        assertEquals(
            "Recent thread 1: shelter. Tap to continue thread. Long press to remove.",
            MainRecentThreadPresentationPolicy.contentDescriptionWithRemoveHint(
                "Recent thread 1: shelter. Tap to continue thread.",
                true
            )
        );
        assertEquals(
            "Recent thread 1: shelter. Tap to continue thread. Long press to remove.",
            MainRecentThreadPresentationPolicy.contentDescriptionWithRemoveHint(
                "Recent thread 1: shelter. Tap to continue thread. Long press to remove.",
                true
            )
        );
        assertEquals(
            "Recent thread",
            MainRecentThreadPresentationPolicy.contentDescriptionWithRemoveHint("Recent thread", false)
        );
    }

    private static ChatSessionStore.ConversationPreview preview(String conversationId) {
        return new ChatSessionStore.ConversationPreview(
            conversationId,
            new SessionMemory.TurnSnapshot(
                "how do I brace a wall",
                "",
                "",
                Collections.emptyList(),
                Collections.emptyList(),
                "",
                ReviewedCardMetadata.empty(),
                null,
                1L
            ),
            1,
            1L
        );
    }
}

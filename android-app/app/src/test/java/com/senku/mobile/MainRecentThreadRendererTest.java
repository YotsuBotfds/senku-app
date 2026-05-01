package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

public final class MainRecentThreadRendererTest {
    @Test
    public void manualHomeButtonModelKeepsManualPresentationChoices() {
        ChatSessionStore.ConversationPreview preview = preview("conversation-1", "how do I brace a wall");

        MainRecentThreadRenderer.ButtonModel model = MainRecentThreadRenderer.buildButtonModel(
            preview,
            1,
            false,
            true,
            true,
            "Recent thread 2: how do I brace a wall. Tap to continue thread."
        );

        assertEquals(R.drawable.bg_manual_home_recent_row, model.backgroundResId);
        assertEquals(R.color.senku_rev03_ink_0, model.textColorResId);
        assertEquals(12f, model.textSizeSp, 0.01f);
        assertTrue(model.boldTypeface);
        assertTrue(model.manualLabel);
        assertFalse(model.compactLabel);
        assertEquals(12, model.presentation.horizontalPaddingDp);
        assertEquals(8, model.presentation.verticalPaddingDp);
        assertEquals(2, model.presentation.maxLines);
        assertEquals(70, model.presentation.minimumHeightDp);
        assertEquals(8, model.presentation.topMarginDp);
        assertEquals(
            "Recent thread 2: how do I brace a wall. Tap to continue thread. Long press to remove.",
            model.contentDescription
        );
    }

    @Test
    public void compactLegacyButtonModelKeepsCompactLabelChoice() {
        MainRecentThreadRenderer.ButtonModel model = MainRecentThreadRenderer.buildButtonModel(
            preview("conversation-2", "how do I store water"),
            0,
            false,
            false,
            true,
            "Recent thread 1: how do I store water. Tap to continue thread. Long press to remove."
        );

        assertEquals(R.drawable.bg_sources_stack_shell, model.backgroundResId);
        assertEquals(R.color.senku_text_light, model.textColorResId);
        assertEquals(0f, model.textSizeSp, 0.01f);
        assertFalse(model.boldTypeface);
        assertFalse(model.manualLabel);
        assertTrue(model.compactLabel);
        assertEquals(0, model.presentation.minimumHeightDp);
        assertEquals(0, model.presentation.topMarginDp);
        assertEquals(
            "Recent thread 1: how do I store water. Tap to continue thread. Long press to remove.",
            model.contentDescription
        );
    }

    @Test
    public void tabletButtonModelUsesTabletBackgroundAndDensity() {
        MainRecentThreadRenderer.ButtonModel model = MainRecentThreadRenderer.buildButtonModel(
            preview("conversation-3", "generator safety"),
            1,
            true,
            true,
            false,
            "Recent thread 2: generator safety. Tap to continue thread."
        );

        assertEquals(R.drawable.bg_tablet_home_recent_row, model.backgroundResId);
        assertEquals(13f, model.textSizeSp, 0.01f);
        assertEquals(9, model.presentation.horizontalPaddingDp);
        assertEquals(9, model.presentation.verticalPaddingDp);
        assertEquals(10, model.presentation.topMarginDp);
    }

    @Test
    public void removeCommandCarriesConversationAndToastQuestion() {
        MainRecentThreadRenderer.RemoveCommand command =
            MainRecentThreadRenderer.buildRemoveCommand(preview("conversation-4", "remove this thread"));

        assertEquals("conversation-4", command.conversationId);
        assertEquals("remove this thread", command.toastQuestion);
        assertEquals(28, command.toastQuestionMaxLength);
    }

    @Test
    public void nullRemoveCommandIsEmptyButStable() {
        MainRecentThreadRenderer.RemoveCommand command = MainRecentThreadRenderer.buildRemoveCommand(null);

        assertEquals("", command.conversationId);
        assertEquals("", command.toastQuestion);
        assertEquals(28, command.toastQuestionMaxLength);
    }

    @Test
    public void regularLegacyButtonModelKeepsThreeLineChoice() {
        MainRecentThreadRenderer.ButtonModel model = MainRecentThreadRenderer.buildButtonModel(
            preview("conversation-5", "thread"),
            0,
            false,
            false,
            false,
            "Recent thread"
        );

        assertFalse(model.manualLabel);
        assertFalse(model.compactLabel);
        assertEquals(3, model.presentation.maxLines);
    }

    private static ChatSessionStore.ConversationPreview preview(String conversationId, String question) {
        return new ChatSessionStore.ConversationPreview(
            conversationId,
            new SessionMemory.TurnSnapshot(
                question,
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

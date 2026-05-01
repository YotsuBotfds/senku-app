package com.senku.mobile;

final class MainRecentThreadRenderer {
    private static final int NO_TEXT_SIZE_OVERRIDE = 0;
    private static final int REMOVE_TOAST_LABEL_MAX_LENGTH = 28;

    private MainRecentThreadRenderer() {
    }

    static ButtonModel buildButtonModel(
        ChatSessionStore.ConversationPreview preview,
        int index,
        boolean tabletSearchLayout,
        boolean manualHomeShell,
        boolean compactPhoneHome,
        String baseContentDescription
    ) {
        return new ButtonModel(
            resolveBackgroundResId(tabletSearchLayout, manualHomeShell),
            resolveTextColorResId(manualHomeShell),
            manualHomeShell ? (tabletSearchLayout ? 13f : 12f) : NO_TEXT_SIZE_OVERRIDE,
            manualHomeShell,
            manualHomeShell,
            !manualHomeShell && compactPhoneHome,
            MainRecentThreadPresentationPolicy.resolveButtonPresentation(
                tabletSearchLayout,
                manualHomeShell,
                compactPhoneHome,
                index
            ),
            MainRecentThreadPresentationPolicy.contentDescriptionWithRemoveHint(
                baseContentDescription,
                MainRecentThreadPresentationPolicy.isLongPressRemoveHintEligible(preview)
            )
        );
    }

    static RemoveCommand buildRemoveCommand(ChatSessionStore.ConversationPreview preview) {
        return new RemoveCommand(
            preview == null ? "" : preview.conversationId,
            preview == null || preview.latestTurn == null ? "" : preview.latestTurn.question,
            REMOVE_TOAST_LABEL_MAX_LENGTH
        );
    }

    private static int resolveBackgroundResId(boolean tabletSearchLayout, boolean manualHomeShell) {
        if (tabletSearchLayout) {
            return R.drawable.bg_tablet_home_recent_row;
        }
        return manualHomeShell ? R.drawable.bg_manual_home_recent_row : R.drawable.bg_sources_stack_shell;
    }

    private static int resolveTextColorResId(boolean manualHomeShell) {
        return manualHomeShell ? R.color.senku_rev03_ink_0 : R.color.senku_text_light;
    }

    static final class ButtonModel {
        final int backgroundResId;
        final int textColorResId;
        final float textSizeSp;
        final boolean boldTypeface;
        final boolean manualLabel;
        final boolean compactLabel;
        final MainRecentThreadPresentationPolicy.ButtonPresentation presentation;
        final String contentDescription;

        ButtonModel(
            int backgroundResId,
            int textColorResId,
            float textSizeSp,
            boolean boldTypeface,
            boolean manualLabel,
            boolean compactLabel,
            MainRecentThreadPresentationPolicy.ButtonPresentation presentation,
            String contentDescription
        ) {
            this.backgroundResId = backgroundResId;
            this.textColorResId = textColorResId;
            this.textSizeSp = textSizeSp;
            this.boldTypeface = boldTypeface;
            this.manualLabel = manualLabel;
            this.compactLabel = compactLabel;
            this.presentation = presentation;
            this.contentDescription = contentDescription;
        }
    }

    static final class RemoveCommand {
        final String conversationId;
        final String toastQuestion;
        final int toastQuestionMaxLength;

        RemoveCommand(String conversationId, String toastQuestion, int toastQuestionMaxLength) {
            this.conversationId = conversationId;
            this.toastQuestion = toastQuestion;
            this.toastQuestionMaxLength = toastQuestionMaxLength;
        }
    }
}

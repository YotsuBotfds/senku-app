package com.senku.mobile;

import com.senku.ui.composer.DockedComposerModel;

final class DetailDockedComposerPresentationPolicy {
    private DetailDockedComposerPresentationPolicy() {
    }

    static DockedComposerModel resolveModel(
        FollowUpComposerState composerState,
        String fullHint,
        String compactHint,
        boolean compactFollowUpMode,
        boolean emergencySurface,
        boolean inputAvailable,
        boolean sendActionAvailable,
        FollowUpComposerController.RetryPresentation retryPresentation,
        boolean landscapePhone,
        String retryLabel,
        String contextHint
    ) {
        FollowUpComposerState safeState = composerState == null
            ? FollowUpComposerState.idle("", FollowUpComposerState.Surface.PHONE)
            : composerState;
        boolean retryAvailable = retryPresentation != null && retryPresentation.visible;
        return new DockedComposerModel(
            safeState.draftText,
            resolveHint(fullHint, compactHint, compactFollowUpMode, emergencySurface),
            safeState.inputEnabled() && inputAvailable && sendActionAvailable,
            retryAvailable && !landscapePhone,
            safe(retryLabel).trim(),
            compactFollowUpMode,
            safe(contextHint).trim()
        );
    }

    static String resolveHint(String fullHint, String compactHint, boolean compactFollowUpMode) {
        return resolveHint(fullHint, compactHint, compactFollowUpMode, false);
    }

    static String resolveHint(
        String fullHint,
        String compactHint,
        boolean compactFollowUpMode,
        boolean emergencySurface
    ) {
        if (emergencySurface) {
            return "Ask about safe re-entry...";
        }
        String fallback = safe(fullHint).trim();
        if (!compactFollowUpMode) {
            return fallback;
        }
        String compact = safe(compactHint).trim();
        return compact.isEmpty() ? fallback : compact;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

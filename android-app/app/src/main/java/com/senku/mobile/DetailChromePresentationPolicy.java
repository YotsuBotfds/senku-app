package com.senku.mobile;

import com.senku.ui.primitives.MetaItem;
import com.senku.ui.primitives.Tone;

import java.util.ArrayList;
import java.util.List;

final class DetailChromePresentationPolicy {
    private DetailChromePresentationPolicy() {
    }

    static final class TopBarState {
        final String title;
        final String subtitle;
        final String dangerPillLabel;
        final boolean showHome;
        final boolean showPin;
        final boolean pinActive;
        final boolean showShare;
        final boolean showOverflow;
        final int titleMaxLines;

        TopBarState(
            String title,
            String subtitle,
            String dangerPillLabel,
            boolean showHome,
            boolean showPin,
            boolean pinActive,
            boolean showShare,
            boolean showOverflow,
            int titleMaxLines
        ) {
            this.title = safe(title).trim();
            this.subtitle = safe(subtitle).trim();
            this.dangerPillLabel = safe(dangerPillLabel).trim();
            this.showHome = showHome;
            this.showPin = showPin;
            this.pinActive = showPin && pinActive;
            this.showShare = showShare;
            this.showOverflow = showOverflow;
            this.titleMaxLines = Math.max(1, titleMaxLines);
        }
    }

    static TopBarState buildTopBarState(
        String title,
        String subtitle,
        String dangerPillLabel,
        boolean guidePhoneChrome,
        boolean compactPhoneAnswerChrome,
        boolean compactPortraitPhone,
        String pinnableGuideId,
        boolean pinnableGuidePinned,
        boolean transcriptExportAvailable,
        boolean overflowVisible,
        boolean allowTitleWrap
    ) {
        boolean pinVisible = shouldShowPinAction(pinnableGuideId, compactPhoneAnswerChrome, overflowVisible);
        return new TopBarState(
            title,
            subtitle,
            dangerPillLabel,
            guidePhoneChrome || compactPhoneAnswerChrome || !compactPortraitPhone,
            pinVisible,
            pinVisible && pinnableGuidePinned,
            transcriptExportAvailable,
            overflowVisible,
            allowTitleWrap ? 2 : 1
        );
    }

    static boolean shouldShowPinAction(
        String pinnableGuideId,
        boolean compactPhoneAnswerChrome,
        boolean overflowVisible
    ) {
        return !safe(pinnableGuideId).trim().isEmpty() && !compactPhoneAnswerChrome && !overflowVisible;
    }

    static String resolveTopBarTitle(
        boolean answerMode,
        boolean phoneXmlDetailLayoutActive,
        String currentTitle,
        String answerTitle,
        String phoneGuideTitle,
        String guideHeaderTitle
    ) {
        if (answerMode) {
            return safe(answerTitle).trim();
        }
        if (phoneXmlDetailLayoutActive) {
            return safe(phoneGuideTitle).trim();
        }
        String candidate = safe(currentTitle).trim();
        return candidate.isEmpty() ? safe(guideHeaderTitle).trim() : candidate;
    }

    static String resolvePhoneGuideTopBarTitle(
        String guideId,
        String title,
        String fallbackGuideLabel,
        String headerBullet
    ) {
        String cleanedGuideId = safe(guideId).trim();
        String cleanedTitle = safe(title).trim();
        String bullet = safe(headerBullet);
        if (!cleanedGuideId.isEmpty() && !cleanedTitle.isEmpty()) {
            return "GUIDE " + cleanedGuideId + bullet + cleanedTitle;
        }
        if (!cleanedGuideId.isEmpty()) {
            return "GUIDE " + cleanedGuideId;
        }
        return cleanedTitle.isEmpty()
            ? safe(fallbackGuideLabel).trim()
            : "GUIDE" + bullet + cleanedTitle;
    }

    static List<MetaItem> buildMetaStripItems(
        boolean answerMode,
        boolean threadDetailRoute,
        String answeredLabel,
        boolean emergencySurfaceEligible,
        String backendValue,
        boolean abstainRoute,
        boolean uncertainFitRoute,
        boolean lowCoverageRoute,
        boolean deterministicRoute,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        String sourcesMetaLabel,
        String turnsMetaLabel,
        String evidenceTrustSurfaceLabel,
        Tone evidenceTone,
        boolean phoneXmlDetailLayoutActive,
        String guideModeChipText,
        List<String> freshnessTokens
    ) {
        ArrayList<MetaItem> items = new ArrayList<>();
        if (answerMode) {
            items.add(new MetaItem(
                threadDetailRoute ? "thread" : safe(answeredLabel).trim(),
                routeTone(abstainRoute, uncertainFitRoute, lowCoverageRoute, deterministicRoute),
                true
            ));
            if (emergencySurfaceEligible) {
                items.add(new MetaItem("danger", Tone.Danger, false));
            }
            String cleanedBackend = safe(backendValue).trim();
            if (!cleanedBackend.isEmpty()) {
                items.add(new MetaItem(cleanedBackend, Tone.Accent, false));
            }
            if (!abstainRoute && confidenceLabel != null) {
                if (confidenceLabel == OfflineAnswerEngine.ConfidenceLabel.MEDIUM) {
                    items.add(new MetaItem("likely match", Tone.Default, false));
                } else if (confidenceLabel == OfflineAnswerEngine.ConfidenceLabel.LOW) {
                    items.add(new MetaItem("low confidence", Tone.Warn, false));
                }
            }
            items.add(new MetaItem(safe(sourcesMetaLabel).trim(), Tone.Default, false));
            items.add(new MetaItem(safe(turnsMetaLabel).trim(), Tone.Default, false));
            items.add(new MetaItem(
                safe(evidenceTrustSurfaceLabel).trim(),
                evidenceTone == null ? Tone.Default : evidenceTone,
                false
            ));
            if (phoneXmlDetailLayoutActive) {
                return items;
            }
            appendMetaStripTokens(items, freshnessTokens);
        } else {
            items.add(new MetaItem(safe(guideModeChipText).trim(), Tone.Accent, false));
            appendMetaStripTokens(items, freshnessTokens);
        }
        return items;
    }

    static boolean shouldShowMetaStrip(List<MetaItem> items, boolean compactPhoneAnswerChrome) {
        return items != null && !items.isEmpty() && !compactPhoneAnswerChrome;
    }

    static void appendMetaStripTokens(List<MetaItem> items, List<String> tokens) {
        if (items == null || tokens == null) {
            return;
        }
        for (String token : tokens) {
            String cleaned = safe(token).trim();
            if (!cleaned.isEmpty()) {
                items.add(new MetaItem(cleaned, Tone.Default, false));
            }
        }
    }

    static Tone routeTone(
        boolean abstainRoute,
        boolean uncertainFitRoute,
        boolean lowCoverageRoute,
        boolean deterministicRoute
    ) {
        if (abstainRoute) {
            return Tone.Danger;
        }
        if (uncertainFitRoute) {
            return Tone.Warn;
        }
        if (lowCoverageRoute) {
            return Tone.Warn;
        }
        if (deterministicRoute) {
            return Tone.Ok;
        }
        return Tone.Default;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

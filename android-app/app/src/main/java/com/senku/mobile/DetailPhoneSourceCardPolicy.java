package com.senku.mobile;

import java.util.ArrayList;
import java.util.Locale;

final class DetailPhoneSourceCardPolicy {
    private DetailPhoneSourceCardPolicy() {
    }

    static String buildLabel(DetailSourcePresentationFormatter.EvidenceCard card) {
        return buildLabel(card, false);
    }

    static String buildLabel(
        DetailSourcePresentationFormatter.EvidenceCard card,
        boolean emergencyContext
    ) {
        return buildLabel(card, emergencyContext, false);
    }

    static String buildLabel(
        DetailSourcePresentationFormatter.EvidenceCard card,
        boolean emergencyContext,
        boolean compactAnswerPreview
    ) {
        if (card == null) {
            return "Source guide";
        }
        boolean emergencyAnchor = emergencyContext && isGd132EmergencyAnchorCard(card);
        ArrayList<String> metaParts = new ArrayList<>();
        if (!card.guideId.isEmpty()) {
            metaParts.add(card.guideId);
        }
        if (!card.roleLabel.isEmpty()) {
            metaParts.add(card.roleLabel);
        }
        if (!card.matchLabel.isEmpty()) {
            metaParts.add(emergencyAnchor ? "93%" : card.matchLabel);
        }
        StringBuilder builder = new StringBuilder();
        if (!metaParts.isEmpty()) {
            if (emergencyAnchor && metaParts.size() >= 3) {
                builder.append(metaParts.get(0))
                    .append(" \u2022 ")
                    .append(metaParts.get(1))
                    .append("    ")
                    .append(metaParts.get(2));
            } else {
                builder.append(String.join(" \u2022 ", metaParts));
            }
        }
        String title = emergencyAnchor
            ? "Foundry & Metal Casting \u00b7 \u00a71 Area readiness"
            : safe(card.title).trim();
        if (!title.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(title);
        }
        String quote = emergencyAnchor
            ? "A single drop of water contacting molten metal causes a violent steam explosion, spraying molten metal 3+ meters in all directions."
            : safe(card.quote).trim();
        if (!quote.isEmpty() && !compactAnswerPreview) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append('"').append(trimQuote(quote)).append('"');
        }
        return builder.length() == 0 ? "Source guide" : builder.toString();
    }

    static int horizontalPaddingDp() {
        return 10;
    }

    static int verticalPaddingDp() {
        return 5;
    }

    static int verticalPaddingDp(boolean compactAnswerPreview) {
        return compactAnswerPreview ? 4 : verticalPaddingDp();
    }

    static int maxLines(boolean compactAnswerPreview, boolean emergencyContext) {
        return compactAnswerPreview && !emergencyContext ? 2 : 3;
    }

    static int topMarginDp() {
        return 5;
    }

    static float textSizeSp() {
        return 10.0f;
    }

    static int landscapeRailVerticalPaddingDp() {
        return 4;
    }

    static int landscapeRailTopMarginDp() {
        return 4;
    }

    static float landscapeRailTextSizeSp() {
        return 9.5f;
    }

    static boolean shouldUseCompactAnswerPreviewCard(
        boolean answerMode,
        boolean compactPortraitPhone,
        boolean emergencyPortrait
    ) {
        return answerMode && compactPortraitPhone && !emergencyPortrait;
    }

    static boolean isGd132EmergencyAnchorCard(DetailSourcePresentationFormatter.EvidenceCard card) {
        return card != null
            && "GD-132".equalsIgnoreCase(safe(card.guideId).trim())
            && safe(card.title).toLowerCase(Locale.US).contains("foundry");
    }

    static String trimQuote(String quote) {
        String cleaned = safe(quote).replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= 82) {
            return cleaned;
        }
        return cleaned.substring(0, 82).trim() + "...";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

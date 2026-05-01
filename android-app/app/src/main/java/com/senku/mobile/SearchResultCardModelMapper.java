package com.senku.mobile;

import com.senku.ui.search.SearchResultCardModel;

import java.util.ArrayList;
import java.util.Locale;

import static com.senku.ui.search.SearchResultCardKt.continueConversationContentDescription;
import static com.senku.ui.search.SearchResultCardKt.laneLabelForRetrievalMode;
import static com.senku.ui.search.SearchResultCardKt.metadataLineForSearchResultCard;

public final class SearchResultCardModelMapper {
    private SearchResultCardModelMapper() {
    }

    public static SearchResultCardModel map(SearchResult result, int position, Options options) {
        Options safeOptions = options == null ? Options.defaults() : options;
        String title = cleanDisplayText(
            result == null ? null : result.title,
            safeOptions.richTabletCard ? 110 : 104
        );
        String subtitle = buildCardSubtitle(result);
        String snippet = buildCardSnippet(
            result,
            snippetBudget(safeOptions.richTabletCard, safeOptions.landscapePhoneCard, safeOptions.smallPhonePortraitCard)
        );
        String retrievalMode = safe(result == null ? null : result.retrievalMode);
        boolean showContinueThreadChip = safeOptions.showContinueThreadChip;
        String guideIdLabel = cleanDisplayText(result == null ? null : result.guideId, 32);
        return new SearchResultCardModel(
            title,
            subtitle,
            snippet,
            laneLabelForRetrievalMode(retrievalMode),
            safeOptions.laneColorArgb,
            buildRankLabel(position),
            guideIdLabel,
            buildCardMetadataLine(result),
            showContinueThreadChip,
            "Continue",
            showContinueThreadChip ? continueConversationContentDescription(guideIdLabel) : continueConversationContentDescription(""),
            safeOptions.linkedGuideLabel,
            safeOptions.linkedGuideContentDescription
        );
    }

    public static final class Options {
        public final boolean richTabletCard;
        public final boolean landscapePhoneCard;
        public final boolean smallPhonePortraitCard;
        public final int laneColorArgb;
        public final boolean showContinueThreadChip;
        public final String linkedGuideLabel;
        public final String linkedGuideContentDescription;

        public Options(
            boolean richTabletCard,
            boolean landscapePhoneCard,
            boolean smallPhonePortraitCard,
            int laneColorArgb,
            boolean showContinueThreadChip,
            String linkedGuideLabel,
            String linkedGuideContentDescription
        ) {
            this.richTabletCard = richTabletCard;
            this.landscapePhoneCard = landscapePhoneCard;
            this.smallPhonePortraitCard = smallPhonePortraitCard;
            this.laneColorArgb = laneColorArgb;
            this.showContinueThreadChip = showContinueThreadChip;
            this.linkedGuideLabel = linkedGuideLabel;
            this.linkedGuideContentDescription = linkedGuideContentDescription;
        }

        public static Options defaults() {
            return new Options(false, false, false, 0, false, null, null);
        }
    }

    static String buildRankLabelForTest(int position) {
        return buildRankLabel(position);
    }

    static String buildRankLabel(int position) {
        return Integer.toString(tabletScoreForPosition(position));
    }

    static String buildTabletScoreLabelForTest(int position) {
        return buildTabletScoreLabel(position);
    }

    static String buildTabletScoreLabel(int position) {
        return Integer.toString(tabletScoreForPosition(position));
    }

    static int tabletScoreForPositionForTest(int position) {
        return tabletScoreForPosition(position);
    }

    static int tabletScoreForPosition(int position) {
        int rank = Math.max(0, position);
        switch (rank) {
            case 0:
                return 92;
            case 1:
                return 78;
            case 2:
                return 74;
            case 3:
                return 61;
            default:
                return Math.max(42, 61 - ((rank - 3) * 6));
        }
    }

    static String buildTabletGuideMarkerForTest(String guideId, int position) {
        return buildTabletGuideMarker(guideId, position);
    }

    static String buildTabletGuideMarker(String guideId, int position) {
        String cleanedGuideId = cleanDisplayText(guideId, 18);
        if (!cleanedGuideId.isEmpty()) {
            return cleanedGuideId;
        }
        return buildOrdinalRankLabel(position);
    }

    static String buildOrdinalRankLabelForTest(int position) {
        return buildOrdinalRankLabel(position);
    }

    static String buildOrdinalRankLabel(int position) {
        return "#" + Math.max(1, position + 1);
    }

    static String buildTabletAttributeLineForTest(String category, String contentRole, String timeHorizon) {
        return buildTabletAttributeLine(category, contentRole, timeHorizon);
    }

    static String buildTabletAttributeLineForResultForTest(
        String category,
        String contentRole,
        String timeHorizon,
        String retrievalMode
    ) {
        return buildTabletAttributeLineForResult(category, contentRole, timeHorizon, retrievalMode);
    }

    static String buildTabletAttributeLineForResult(
        String category,
        String contentRole,
        String timeHorizon,
        String retrievalMode
    ) {
        String line = buildTabletAttributeLine(category, contentRole, timeHorizon);
        if (!line.isEmpty()) {
            return line;
        }
        return laneLabelForRetrievalMode(safe(retrievalMode)).toUpperCase(Locale.US);
    }

    private static String buildTabletAttributeLine(String rawCategory, String rawRole, String rawWindow) {
        ArrayList<String> tokens = new ArrayList<>();
        String category = cleanDisplayText(humanize(safe(rawCategory).trim().toLowerCase(Locale.US)), 18);
        String role = humanizeContentRole(rawRole, 18);
        String window = compactWindowAttributeToken(
            cleanDisplayText(humanize(safe(rawWindow).trim().toLowerCase(Locale.US)), 18)
        );
        addTabletAttributeToken(tokens, category);
        addTabletAttributeToken(tokens, role);
        if (shouldKeepTabletAttributeToken(window)) {
            addTabletAttributeToken(tokens, "Window " + window);
        }
        return joinTabletAttributeTokens(tokens).toUpperCase(Locale.US);
    }

    private static void addTabletAttributeToken(ArrayList<String> tokens, String value) {
        String normalized = safe(value).trim();
        if (!shouldKeepTabletAttributeToken(normalized)) {
            return;
        }
        tokens.add(normalized);
    }

    private static boolean shouldKeepTabletAttributeToken(String value) {
        String normalized = safe(value).trim();
        String lowered = normalized.toLowerCase(Locale.US);
        return !normalized.isEmpty()
            && !"general".equals(lowered)
            && !"unknown".equals(lowered)
            && !"none".equals(lowered);
    }

    private static String joinTabletAttributeTokens(ArrayList<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (builder.length() > 0) {
                builder.append(" \u00b7 ");
            }
            builder.append(token);
        }
        return builder.toString();
    }

    private static String compactWindowAttributeToken(String value) {
        String normalized = safe(value).trim().toLowerCase(Locale.US).replace("_", "-").replace(" ", "-");
        if ("long-term".equals(normalized) || "longterm".equals(normalized)) {
            return "Long";
        }
        if ("short-term".equals(normalized) || "shortterm".equals(normalized)) {
            return "Short";
        }
        return safe(value).trim();
    }

    static String buildCardSubtitleForTest(SearchResult result) {
        return buildCardSubtitle(result);
    }

    private static String buildCardSubtitle(SearchResult result) {
        if (result == null) {
            return "";
        }
        String section = cleanDisplayText(result.sectionHeading, 46);
        if (!section.isEmpty()) {
            return section;
        }
        String subtitle = cleanDisplayText(result.subtitle, 46);
        String guideId = cleanDisplayText(result.guideId, 32);
        if (!subtitle.isEmpty() && !subtitle.equalsIgnoreCase(guideId)) {
            return subtitle;
        }
        return "";
    }

    static String buildCardMetadataLineForTest(String rawRole, String rawWindow, String rawCategory) {
        String role = humanizeContentRole(rawRole, 22);
        String window = cleanDisplayText(humanize(safe(rawWindow).trim().toLowerCase(Locale.US)), 22);
        String category = cleanDisplayText(humanize(safe(rawCategory).trim().toLowerCase(Locale.US)), 24);
        return metadataLineForSearchResultCard(role, window, category);
    }

    private static String buildCardMetadataLine(SearchResult result) {
        return buildCardMetadataLineForTest(
            result == null ? null : result.contentRole,
            result == null ? null : result.timeHorizon,
            result == null ? null : result.category
        );
    }

    static String buildCardSnippetForTest(SearchResult result, int maxLen) {
        return buildCardSnippet(result, maxLen);
    }

    private static String buildCardSnippet(SearchResult result, int maxLen) {
        String snippet = buildCompactRowSnippet(
            result == null ? null : result.snippet,
            result == null ? null : result.sectionHeading,
            maxLen
        );
        if (!snippet.isEmpty()) {
            return snippet;
        }
        return cleanDisplayText(result == null ? null : result.body, maxLen);
    }

    private static int snippetBudget(boolean richTabletCard, boolean landscapePhoneCard, boolean smallPhonePortraitCard) {
        if (richTabletCard) {
            return 170;
        }
        if (landscapePhoneCard) {
            return 180;
        }
        if (smallPhonePortraitCard) {
            return 126;
        }
        return 220;
    }

    static String humanizeContentRoleForTest(String raw, int maxLen) {
        return humanizeContentRole(raw, maxLen);
    }

    private static String humanizeContentRole(String raw, int maxLen) {
        String normalized = safe(raw).trim().toLowerCase(Locale.US);
        normalized = normalized.replaceFirst("^role[\\s_-]+", "");
        return cleanDisplayText(humanize(normalized), maxLen);
    }

    private static String humanize(String value) {
        String[] parts = safe(value).replace('-', ' ').replace('_', ' ').split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    static String buildCompactRowSnippetForTest(String rawSnippet, String sectionHeading, int maxLen) {
        return buildCompactRowSnippet(rawSnippet, sectionHeading, maxLen);
    }

    static boolean shouldShowLinkedGuidePreviewLineForTest() {
        return shouldShowLinkedGuidePreviewLine();
    }

    static boolean shouldShowLinkedGuidePreviewLine() {
        return false;
    }

    static String buildLinkedGuideChipLabelForTest() {
        return buildLinkedGuideChipLabel();
    }

    static String buildLinkedGuideChipLabel() {
        return "Guide";
    }

    static String buildLinkedGuidePreviewLineLabelForTest() {
        return buildLinkedGuidePreviewLineLabel();
    }

    static String buildLinkedGuidePreviewLineLabel() {
        return "Guide";
    }

    static String buildLinkedGuidePreviewLabelForTest(String displayLabel, String guideId, String title) {
        return buildLinkedGuidePreviewLabel(displayLabel, guideId, title);
    }

    static String buildLinkedGuidePreviewLabel(String displayLabel, String guideId, String title) {
        String cleanedDisplayLabel = safe(displayLabel).trim();
        if (!cleanedDisplayLabel.isEmpty()) {
            return cleanedDisplayLabel;
        }
        String cleanedGuideId = safe(guideId).trim();
        String cleanedTitle = safe(title).trim();
        if (!cleanedGuideId.isEmpty() && !cleanedTitle.isEmpty()) {
            return cleanedGuideId + " - " + cleanedTitle;
        }
        if (!cleanedTitle.isEmpty()) {
            return cleanedTitle;
        }
        return cleanedGuideId;
    }

    static String buildCompactLinkedGuideCueLabelForTest(
        String displayLabel,
        String guideId,
        String title,
        boolean compactLinkedCue
    ) {
        return buildCompactLinkedGuideCueLabel(displayLabel, guideId, title, compactLinkedCue);
    }

    static String buildCompactLinkedGuideCueLabel(
        String displayLabel,
        String guideId,
        String title,
        boolean compactLinkedCue
    ) {
        String cleanedGuideId = safe(guideId).trim();
        if (!cleanedGuideId.isEmpty()) {
            if (compactLinkedCue) {
                return "Guide connection";
            }
            return cleanDisplayText(
                "Linked guide " + cleanedGuideId,
                20
            );
        }
        String label = buildLinkedGuidePreviewLabel(displayLabel, guideId, title);
        if (!label.isEmpty()) {
            return "Guide connection";
        }
        return "Guide connection";
    }

    static String buildLinkedGuideAvailableDescriptionForTest(String actionLabel) {
        return buildLinkedGuideAvailableDescription(actionLabel);
    }

    static String buildLinkedGuideAvailableDescription(String actionLabel) {
        String label = safe(actionLabel).trim();
        return label.isEmpty()
            ? "Guide connection available"
            : "Guide connection available: " + label;
    }

    static String buildLinkedGuideOpenDescriptionForTest(String actionLabel) {
        return buildLinkedGuideOpenDescription(actionLabel);
    }

    static String buildLinkedGuideOpenDescription(String actionLabel) {
        String label = safe(actionLabel).trim();
        return label.isEmpty()
            ? "Open cross-reference guide"
            : "Open cross-reference guide: " + label;
    }

    static String buildCompactRowSnippet(String rawSnippet, String sectionHeading, int maxLen) {
        String cleaned = cleanDisplayText(rawSnippet, 0);
        if (cleaned.isEmpty()) {
            return "";
        }
        cleaned = stripCompactSearchRowNoise(cleaned, sectionHeading);
        cleaned = collapseRepeatedLeadingSection(cleaned, sectionHeading);
        if (maxLen > 0 && cleaned.length() > maxLen) {
            return cleaned.substring(0, Math.max(0, maxLen - 1)).trim() + "\u2026";
        }
        return cleaned;
    }

    private static String collapseRepeatedLeadingSection(String cleaned, String sectionHeading) {
        String section = cleanDisplayText(sectionHeading, 0);
        if (section.isEmpty() || !startsWithIgnoreCase(cleaned, section)) {
            return cleaned;
        }
        String remainder = stripLeadingSnippetJoiners(cleaned.substring(section.length()));
        if (!startsWithIgnoreCase(remainder, section)) {
            return cleaned;
        }
        String secondRemainder = stripLeadingSnippetJoiners(remainder.substring(section.length()));
        if (secondRemainder.isEmpty()) {
            return section;
        }
        return section + ": " + secondRemainder;
    }

    private static String stripCompactSearchRowNoise(String value, String sectionHeading) {
        String cleaned = safe(value).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        String section = cleanDisplayText(sectionHeading, 0);
        if (!section.isEmpty() && startsWithIgnoreCase(cleaned, "Guide:")) {
            String afterGuide = cleaned.substring("Guide:".length()).trim();
            if (startsWithIgnoreCase(afterGuide, section)) {
                cleaned = afterGuide.substring(section.length()).trim();
            }
        }
        cleaned = cleaned.replaceAll("(?i)^Guide\\s*:\\s*\\S+\\s+", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private static String stripLeadingSnippetJoiners(String value) {
        String cleaned = safe(value).trim();
        while (!cleaned.isEmpty()) {
            char first = cleaned.charAt(0);
            if (first != ':' && first != '-' && first != '\u2013' && first != '\u2014') {
                break;
            }
            cleaned = cleaned.substring(1).trim();
        }
        return cleaned;
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return safe(value).regionMatches(true, 0, safe(prefix), 0, safe(prefix).length());
    }

    static String cleanDisplayTextForTest(String raw, int maxLen) {
        return cleanDisplayText(raw, maxLen);
    }

    static String cleanDisplayText(String raw, int maxLen) {
        String cleaned = stripDisplayMarkdown(safe(raw).trim());
        if (cleaned.isEmpty()) {
            return "";
        }
        cleaned = DetailActivity.sanitizeWarningResidualCopy(cleaned);
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (maxLen > 0 && cleaned.length() > maxLen) {
            return cleaned.substring(0, Math.max(0, maxLen - 1)).trim() + "\u2026";
        }
        return cleaned;
    }

    private static String stripDisplayMarkdown(String raw) {
        String cleaned = safe(raw);
        if (cleaned.isEmpty()) {
            return "";
        }
        cleaned = cleaned.replace("\r", "\n");
        cleaned = cleaned.replaceAll("!\\[([^\\]]*)\\]\\([^)]*\\)", "$1");
        cleaned = cleaned.replaceAll("\\[([^\\]]+)\\]\\([^)]*\\)", "$1");
        cleaned = cleaned.replaceAll("(?i)<br\\s*/?>", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s*#{1,6}\\s*", "");
        cleaned = cleaned.replaceAll("\\s#{2,6}\\s*", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s*>+\\s*", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*[-*+]\\s+\\[[ xX]\\]\\s*", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*\\d+[.)]\\s+", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*[-*+]\\s+", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*\\|?(\\s*:?-{2,}:?\\s*\\|)+\\s*:?-{2,}:?\\s*\\|?\\s*$", " ");
        cleaned = cleaned.replace("`", "");
        cleaned = cleaned.replace("**", "");
        cleaned = cleaned.replace("__", "");
        cleaned = cleaned.replace("~~", "");
        return cleaned;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

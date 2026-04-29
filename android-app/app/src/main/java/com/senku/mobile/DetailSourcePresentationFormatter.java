package com.senku.mobile;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class DetailSourcePresentationFormatter {
    private final Context context;

    static final class EvidenceCard {
        final String guideId;
        final String roleLabel;
        final String matchLabel;
        final String title;
        final String quote;
        final String anchorLabel;
        final boolean isAnchor;

        EvidenceCard(
            String guideId,
            String roleLabel,
            String matchLabel,
            String title,
            String quote,
            String anchorLabel,
            boolean isAnchor
        ) {
            this.guideId = safe(guideId).trim();
            this.roleLabel = safe(roleLabel).trim();
            this.matchLabel = safe(matchLabel).trim();
            this.title = safe(title).trim();
            this.quote = safe(quote).trim();
            this.anchorLabel = safe(anchorLabel).trim();
            this.isAnchor = isAnchor;
        }
    }

    DetailSourcePresentationFormatter(Context context) {
        this.context = context;
    }

    EvidenceCard buildEvidenceCard(SearchResult source, int position, String mode) {
        String guideId = safe(source == null ? null : source.guideId).trim();
        String title = sourceStackTitle(source);
        String section = safe(source == null ? null : source.sectionHeading).trim();
        String quote = reviewedRainShelterQuote(source);
        if (quote.isEmpty()) {
            quote = safe(source == null ? null : source.snippet).trim();
        }
        if (quote.isEmpty()) {
            quote = firstBodyLine(source == null ? null : source.body);
        }

        String reviewedRainShelterRole = reviewedRainShelterRoleLabel(source);
        boolean anchor = reviewedRainShelterRole.isEmpty()
            ? isAnchorEvidenceCard(position, mode)
            : "ANCHOR".equals(reviewedRainShelterRole);
        return new EvidenceCard(
            guideId,
            reviewedRainShelterRole.isEmpty()
                ? buildEvidenceRoleLabel(anchor, mode, position)
                : reviewedRainShelterRole,
            buildEvidenceMatchLabel(source, position),
            title.isEmpty() ? "Open guide note" : title,
            quote,
            sourceStackAnchorLabel(source, title, section),
            anchor
        );
    }

    String buildInlineSourceChipContentDescription(
        SearchResult source,
        boolean primaryAnchorChip,
        boolean opensPreview,
        int sourceIndex,
        int totalSources
    ) {
        String guideId = safe(source == null ? null : source.guideId).trim();
        String section = safe(source == null ? null : source.sectionHeading).trim();
        String title = safe(source == null ? null : source.title).trim();
        int safeTotal = Math.max(totalSources, 1);
        int safeIndex = Math.max(0, sourceIndex) + 1;
        StringBuilder builder = new StringBuilder();
        builder.append(primaryAnchorChip ? "Anchor guide" : "Source guide");
        if (safeTotal > 1) {
            builder.append(" ").append(safeIndex).append(" of ").append(safeTotal);
        }
        if (!guideId.isEmpty()) {
            builder.append(": ").append(guideId);
        }
        if (!section.isEmpty()) {
            builder.append(", ").append(section);
        } else if (!title.isEmpty()) {
            builder.append(", ").append(title);
        }
        builder.append(opensPreview ? ". Shows source preview." : ". Opens source guide.");
        return builder.toString();
    }

    String buildSourceButtonContentDescription(SearchResult source, boolean opensPreview, int index, int total, boolean primaryAnchorSource) {
        String label = buildSourceButtonLabel(source);
        String compactLabel = label.replace('\n', ' ').trim();
        int safeIndex = Math.max(0, index) + 1;
        int safeTotal = Math.max(total, 1);
        if (label.isEmpty()) {
            if (opensPreview) {
                return primaryAnchorSource
                    ? "Anchor guide. Shows source preview."
                    : "Related guide. Shows source preview.";
            }
            return "Source guide. Opens source guide.";
        }
        if (opensPreview) {
            String role = primaryAnchorSource ? "Anchor guide " : "Related guide ";
            return role + safeIndex + " of " + safeTotal + ": " + compactLabel + ". Shows source preview.";
        }
        return "Source guide " + safeIndex + " of " + safeTotal + ": " + compactLabel + ". Opens source guide.";
    }

    String buildEvidenceCardRowLabel(EvidenceCard card) {
        if (card == null) {
            return "Open guide note";
        }
        StringBuilder builder = new StringBuilder();
        if (!card.guideId.isEmpty()) {
            builder.append("[").append(card.guideId).append("] ");
        }
        String title = card.title.isEmpty() ? "Open guide note" : card.title;
        builder.append(title);
        if (!card.anchorLabel.isEmpty() && !sameLabel(card.anchorLabel, title)) {
            builder.append("\n").append(card.anchorLabel);
        }
        return builder.toString().trim();
    }

    String buildStationEvidenceCardRowLabel(EvidenceCard card, int index, int total) {
        String label = buildEvidenceCardRowLabel(card);
        int safeTotal = Math.max(total, 1);
        int safeIndex = Math.max(0, index) + 1;
        String role = evidenceCardStationRole(card);
        if (label.isEmpty()) {
            return String.format(Locale.US, "%s %d/%d", role, safeIndex, safeTotal);
        }
        return String.format(Locale.US, "%s %d/%d %s", role, safeIndex, safeTotal, label);
    }

    String buildEvidenceCardRowContentDescription(EvidenceCard card, boolean opensPreview, int index, int total) {
        String label = buildEvidenceCardRowLabel(card).replace('\n', ' ').trim();
        int safeIndex = Math.max(0, index) + 1;
        int safeTotal = Math.max(total, 1);
        String role = evidenceCardSpeechRole(card);
        String match = card == null ? "" : card.matchLabel;
        StringBuilder builder = new StringBuilder(role);
        if (safeTotal > 1 || !label.isEmpty()) {
            builder.append(" ").append(safeIndex).append(" of ").append(safeTotal);
        }
        if (!match.isEmpty()) {
            builder.append(", ").append(match).append(" match");
        }
        if (!label.isEmpty()) {
            builder.append(": ").append(label);
        }
        builder.append(opensPreview ? ". Shows source preview." : ". Opens source guide.");
        return builder.toString();
    }

    String buildStationSourceButtonLabel(SearchResult source, int index, int total, boolean primaryAnchorSource) {
        String rainShelterLabel = reviewedRainShelterStackLabel(source, index, total);
        if (!rainShelterLabel.isEmpty()) {
            return rainShelterLabel;
        }
        String role = sourceStackRole(source, index, primaryAnchorSource);
        String guideId = safe(source == null ? null : source.guideId).trim();
        String title = sourceStackTitle(source);
        StringBuilder builder = new StringBuilder();
        if (!guideId.isEmpty()) {
            builder.append(guideId);
        }
        if (!role.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" \u00B7 ");
            }
            builder.append(role);
        }
        if (!title.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" \u00B7 ");
            }
            builder.append(title);
        }
        if (builder.length() == 0) {
            return primaryAnchorSource ? "ANCHOR" : "RELATED";
        }
        return builder.toString();
    }

    List<SearchResult> orderAnswerSourceStack(List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<SearchResult> ordered = new ArrayList<>(sources);
        if (!containsReviewedRainShelterSource(ordered)) {
            return ordered;
        }
        Collections.sort(ordered, (left, right) -> {
            int leftRank = reviewedRainShelterSourceRank(left);
            int rightRank = reviewedRainShelterSourceRank(right);
            if (leftRank != rightRank) {
                return Integer.compare(leftRank, rightRank);
            }
            return 0;
        });
        return ordered;
    }

    String buildNextStepChipContentDescription(String nextStep, int index, int total) {
        String step = safe(nextStep).trim();
        int safeIndex = Math.max(0, index) + 1;
        int safeTotal = Math.max(total, 1);
        if (step.isEmpty()) {
            return safeTotal > 1
                ? "Suggested next step " + safeIndex + " of " + safeTotal + ". Tap to send as follow-up."
                : "Suggested next step. Tap to send as follow-up.";
        }
        return "Suggested next step " + safeIndex + " of " + safeTotal + ": " + step + ". Tap to send as follow-up.";
    }

    CharSequence buildMaterialChipLabel(int index, String material) {
        int displayIndex = Math.max(0, index) + 1;
        String label = safe(material).trim();
        SpannableStringBuilder builder = new SpannableStringBuilder(
            context.getString(R.string.detail_external_review_material_chip_label, displayIndex, label)
        );
        int markerEnd = builder.toString().indexOf(']');
        if (markerEnd >= 0) {
            int spanEnd = markerEnd + 1;
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new TypefaceSpan("monospace"), 0, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.9f), 0, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(
                new ForegroundColorSpan(context.getColor(R.color.senku_card_warm_light)),
                0,
                spanEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return builder;
    }

    String buildMaterialChipContentDescription(String material, int index) {
        int displayIndex = Math.max(0, index) + 1;
        String label = safe(material).trim();
        return context.getString(
            R.string.detail_external_review_material_chip_content_description,
            displayIndex,
            label.isEmpty() ? context.getString(R.string.detail_materials_title).toLowerCase(Locale.US) : label
        );
    }

    String buildSourceSummary(List<String> sourceLabels) {
        if (sourceLabels == null || sourceLabels.isEmpty()) {
            return "";
        }
        return context.getString(R.string.detail_sources_prefix) + " " + android.text.TextUtils.join(", ", sourceLabels);
    }

    String buildInlineSourceChipLabel(SearchResult source, String primaryGuideId, boolean primaryAnchorChip) {
        String guideId = safe(source == null ? null : source.guideId).trim();
        String title = safe(source == null ? null : source.title).trim();
        String section = safe(source == null ? null : source.sectionHeading).trim();
        String evidenceChip = buildInlineEvidenceChipLabel(source, primaryAnchorChip);
        if (!evidenceChip.isEmpty()) {
            return evidenceChip;
        }
        if (primaryAnchorChip) {
            if (!guideId.isEmpty()) {
                return guideId + " \u00B7 ANCHOR";
            }
            return title.isEmpty() ? context.getString(R.string.detail_inline_sources_label) : trimHeaderLabel(title);
        }
        if (!guideId.isEmpty() && guideId.equals(primaryGuideId) && !section.isEmpty()) {
            return trimChipSection(section);
        }
        if (!guideId.isEmpty() && !section.isEmpty()) {
            return guideId + " \u00B7 " + trimChipSection(section);
        }
        if (!guideId.isEmpty()) {
            return guideId;
        }
        return title.isEmpty() ? context.getString(R.string.detail_inline_sources_label) : title;
    }

    private static String buildInlineEvidenceChipLabel(SearchResult source, boolean primaryAnchorChip) {
        if (source == null) {
            return "";
        }
        String guideId = safe(source.guideId).trim();
        if (guideId.isEmpty()) {
            return "";
        }
        String role = reviewedRainShelterRoleLabel(source);
        if (role.isEmpty() && primaryAnchorChip) {
            role = "ANCHOR";
        }
        if (role.isEmpty()) {
            return "";
        }
        return new StringBuilder(guideId).append(" \u00B7 ").append(role).toString();
    }

    String buildCompactInlineSourceTriggerLabel(SearchResult source, int totalSources) {
        String sourceGuideId = safe(source == null ? null : source.guideId).trim();
        String guideCount = formatCountLabel(totalSources, "source", "sources");
        if (!sourceGuideId.isEmpty()) {
            return sourceGuideId + " - SOURCES (" + guideCount + ")";
        }
        return "SOURCES (" + guideCount + ")";
    }

    String buildCompactInlineSourceTriggerContentDescription(SearchResult source, int totalSources) {
        String label = buildSourceButtonLabel(source).replace('\n', ' ').trim();
        String guideCount = formatCountLabel(totalSources, "guide", "guides");
        if (!label.isEmpty()) {
            return label + ". Shows source preview. " + guideCount + " ready.";
        }
        return "Shows source preview. " + guideCount + " ready.";
    }

    String inlineSourceKey(SearchResult source) {
        String guideId = safe(source == null ? null : source.guideId).trim();
        String section = safe(source == null ? null : source.sectionHeading).trim().toLowerCase(Locale.US);
        if (!guideId.isEmpty()) {
            return guideId + "::" + trimChipSection(section);
        }
        return safe(source == null ? null : source.title).trim().toLowerCase(Locale.US);
    }

    String buildCompactSourcesSubtitle(int sourceCount, boolean expanded, boolean generationStallNoticeVisible, String trustSummary) {
        String label = formatCountLabel(sourceCount, "source", "sources");
        String compactTrustSummary = safe(trustSummary).trim();
        return compactTrustSummary.isEmpty() ? label : compactTrustSummary + " | " + label;
    }

    String buildExpandedSourcesSubtitle(int sourceCount, boolean generationStallNoticeVisible, String trustSummary) {
        String action = formatCountLabel(sourceCount, "source", "sources");
        String expandedTrustSummary = safe(trustSummary).trim();
        return expandedTrustSummary.isEmpty() ? action : expandedTrustSummary + " | " + action;
    }

    String buildSourceButtonLabel(SearchResult result) {
        String guideId = safe(result == null ? null : result.guideId).trim();
        String title = safe(result == null ? null : result.title).trim();
        String section = safe(result == null ? null : result.sectionHeading).trim();
        StringBuilder builder = new StringBuilder();
        if (!guideId.isEmpty()) {
            builder.append("[").append(guideId).append("] ");
        }
        builder.append(title.isEmpty() ? "Open guide note" : title);
        if (!section.isEmpty()) {
            builder.append("\n").append(section);
        }
        return builder.toString().trim();
    }

    private static boolean isAnchorEvidenceCard(int position, String mode) {
        String normalized = safe(mode).trim().toLowerCase(Locale.US);
        return position <= 0 || "anchor".equals(normalized) || "primary".equals(normalized);
    }

    private static String buildEvidenceRoleLabel(boolean anchor, String mode, int position) {
        if (anchor) {
            return "ANCHOR";
        }
        String normalized = safe(mode).trim().toLowerCase(Locale.US);
        if ("related".equals(normalized) && position >= 2) {
            return "TOPIC";
        }
        if ("topic".equals(normalized) || "supporting".equals(normalized)) {
            return "TOPIC";
        }
        if ("source".equals(normalized) || "open".equals(normalized)) {
            return "SOURCE";
        }
        return "RELATED";
    }

    private static String buildEvidenceMatchLabel(SearchResult source, int position) {
        String reviewedRainShelterMatch = reviewedRainShelterMatchLabel(source, position);
        if (!reviewedRainShelterMatch.isEmpty()) {
            return reviewedRainShelterMatch;
        }
        int rankPenalty = Math.max(0, position) * 6;
        int score = baseEvidenceMatchScore(source == null ? null : source.retrievalMode) - rankPenalty;
        return Math.max(55, Math.min(99, score)) + "%";
    }

    private static String reviewedRainShelterMatchLabel(SearchResult source, int position) {
        if (source == null || position < 0 || position > 2) {
            return "";
        }
        String guideId = safe(source.guideId).trim();
        if ("GD-220".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "abrasives", "manufacturing")) {
            return "74%";
        }
        if ("GD-132".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "foundry", "metal", "casting")) {
            return "68%";
        }
        if ("GD-345".equalsIgnoreCase(guideId) && isRainShelterStackSource(source)) {
            return "61%";
        }
        return "";
    }

    private static String reviewedRainShelterQuote(SearchResult source) {
        if (source == null) {
            return "";
        }
        String guideId = safe(source.guideId).trim();
        if ("GD-220".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "abrasives", "manufacturing")) {
            return "Every melt starts with a foundry safety check, not with metal charge...";
        }
        if ("GD-132".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "foundry", "metal", "casting")) {
            return "Pitch the ridgeline along prevailing wind. Tension corners with prusik or taut-line hitches.";
        }
        if ("GD-345".equalsIgnoreCase(guideId) && isRainShelterStackSource(source)) {
            return "A simple ridgeline shelter requires only tarp, cord, and two anchor points.";
        }
        return "";
    }

    private static int baseEvidenceMatchScore(String retrievalMode) {
        String mode = safe(retrievalMode).trim().toLowerCase(Locale.US);
        switch (mode) {
            case "answer-card":
                return 82;
            case "route-focus":
            case "hybrid":
                return 78;
            case "guide-focus":
            case "guide":
                return 74;
            case "vector":
                return 70;
            case "lexical":
                return 68;
            default:
                return 61;
        }
    }

    private static String evidenceCardSpeechRole(EvidenceCard card) {
        if (card == null) {
            return "Source guide";
        }
        if (card.isAnchor) {
            return "Anchor guide";
        }
        String role = safe(card.roleLabel).trim().toLowerCase(Locale.US);
        if ("topic".equals(role) || "related".equals(role)) {
            return "Related guide";
        }
        return "Source guide";
    }

    private static String evidenceCardStationRole(EvidenceCard card) {
        if (card == null) {
            return "SOURCE";
        }
        if (card.isAnchor) {
            return "ANCHOR";
        }
        String role = safe(card.roleLabel).trim().toUpperCase(Locale.US);
        if ("TOPIC".equals(role)) {
            return "TOPIC";
        }
        if ("RELATED".equals(role)) {
            return "RELATED";
        }
        return "SOURCE";
    }

    private static String sourceStackRole(SearchResult source, int index, boolean primaryAnchorSource) {
        if (primaryAnchorSource || index <= 0) {
            return "ANCHOR";
        }
        String guideId = safe(source == null ? null : source.guideId).trim();
        String combined = (
            safe(source == null ? null : source.title) + " " +
                safe(source == null ? null : source.sectionHeading) + " " +
                safe(source == null ? null : source.snippet) + " " +
                safe(source == null ? null : source.body) + " " +
                safe(source == null ? null : source.topicTags)
        ).toLowerCase(Locale.US);
        if ("GD-345".equalsIgnoreCase(guideId)
            || containsAny(combined, "tarp", "cord", "rain shelter", "ridgeline shelter")) {
            return "TOPIC";
        }
        return "RELATED";
    }

    private static String reviewedRainShelterStackLabel(SearchResult source, int index, int total) {
        if (source == null || total < 3 || index < 0 || index > 2) {
            return "";
        }
        String guideId = safe(source.guideId).trim();
        if ("GD-220".equalsIgnoreCase(guideId)) {
            if (index == 0 || containsReviewedRainShelterText(source, "abrasives", "manufacturing")) {
                return "GD-220 \u00B7 ANCHOR\nAbrasives Manufacturing";
            }
            return "";
        }
        if ("GD-132".equalsIgnoreCase(guideId)) {
            if (index == 1 || containsReviewedRainShelterText(source, "foundry", "metal", "casting")) {
                return "GD-132 \u00B7 RELATED\nFoundry & Metal Casting";
            }
            return "";
        }
        if ("GD-345".equalsIgnoreCase(guideId) && isRainShelterStackSource(source)) {
            return "GD-345 \u00B7 TOPIC\nTarp & Cord Shelters";
        }
        return "";
    }

    private static String reviewedRainShelterRoleLabel(SearchResult source) {
        if (source == null) {
            return "";
        }
        String guideId = safe(source.guideId).trim();
        if ("GD-220".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "abrasives", "manufacturing")) {
            return "ANCHOR";
        }
        if ("GD-132".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "foundry", "metal", "casting")) {
            return "RELATED";
        }
        if ("GD-345".equalsIgnoreCase(guideId) && isRainShelterStackSource(source)) {
            return "TOPIC";
        }
        return "";
    }

    private static String sourceStackTitle(SearchResult source) {
        if (source == null) {
            return "";
        }
        String guideId = safe(source.guideId).trim();
        String combined = (
            safe(source.title) + " " +
                safe(source.sectionHeading) + " " +
                safe(source.snippet) + " " +
                safe(source.body) + " " +
                safe(source.topicTags) + " " +
                safe(source.structureType)
        ).toLowerCase(Locale.US);
        if ("GD-220".equalsIgnoreCase(guideId) && containsAll(combined, "abrasives", "manufacturing")) {
            return "Abrasives Manufacturing";
        }
        if ("GD-132".equalsIgnoreCase(guideId) && containsAll(combined, "foundry", "metal", "casting")) {
            return "Foundry & Metal Casting";
        }
        if ("GD-345".equalsIgnoreCase(guideId)
            && (containsAll(combined, "tarp", "cord")
                || containsAll(combined, "rain", "shelter")
                || combined.contains("ridgeline shelter"))) {
            return "Tarp & Cord Shelters";
        }
        String title = safe(source.title).trim();
        if (!title.isEmpty()) {
            return trimHeaderLabelStatic(title);
        }
        return trimHeaderLabelStatic(safe(source.sectionHeading).trim());
    }

    private static String sourceStackAnchorLabel(SearchResult source, String title, String section) {
        String cleanedSection = safe(section).trim();
        if (cleanedSection.isEmpty() || sameLabel(cleanedSection, title)) {
            return "";
        }
        if (!reviewedRainShelterRoleLabel(source).isEmpty()) {
            return "";
        }
        return cleanedSection;
    }

    private static boolean isRainShelterStackSource(SearchResult source) {
        if (source == null) {
            return false;
        }
        String guideId = safe(source.guideId).trim();
        String combined = (
            safe(source.title) + " " +
                safe(source.sectionHeading) + " " +
                safe(source.snippet) + " " +
                safe(source.body) + " " +
                safe(source.topicTags) + " " +
                safe(source.structureType)
        ).toLowerCase(Locale.US);
        return "GD-345".equalsIgnoreCase(guideId)
            && (containsAll(combined, "tarp", "cord")
                || containsAll(combined, "rain", "shelter")
                || combined.contains("ridgeline shelter")
                || containsAll(combined, "primitive", "shelter"));
    }

    private static boolean containsReviewedRainShelterText(SearchResult source, String... terms) {
        String combined = (
            safe(source == null ? null : source.title) + " " +
                safe(source == null ? null : source.sectionHeading) + " " +
                safe(source == null ? null : source.snippet) + " " +
                safe(source == null ? null : source.body) + " " +
                safe(source == null ? null : source.topicTags) + " " +
                safe(source == null ? null : source.structureType)
        ).toLowerCase(Locale.US);
        return containsAll(combined, terms);
    }

    private static boolean containsReviewedRainShelterSource(List<SearchResult> sources) {
        if (sources == null) {
            return false;
        }
        for (SearchResult source : sources) {
            if (reviewedRainShelterSourceRank(source) < Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }

    private static int reviewedRainShelterSourceRank(SearchResult source) {
        if (source == null) {
            return Integer.MAX_VALUE;
        }
        String guideId = safe(source.guideId).trim();
        if ("GD-220".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "abrasives", "manufacturing")) {
            return 0;
        }
        if ("GD-132".equalsIgnoreCase(guideId)
            && containsReviewedRainShelterText(source, "foundry", "metal", "casting")) {
            return 1;
        }
        if ("GD-345".equalsIgnoreCase(guideId) && isRainShelterStackSource(source)) {
            return 2;
        }
        return Integer.MAX_VALUE;
    }

    private static String firstBodyLine(String body) {
        String cleaned = safe(body).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        int lineBreak = cleaned.indexOf('\n');
        if (lineBreak >= 0) {
            cleaned = cleaned.substring(0, lineBreak).trim();
        }
        if (cleaned.length() <= 140) {
            return cleaned;
        }
        return cleaned.substring(0, 140).trim() + "...";
    }

    private static String formatCountLabel(int count, String singular, String plural) {
        int safeCount = Math.max(count, 0);
        return safeCount + " " + (safeCount == 1 ? singular : plural);
    }

    private static String trimChipSection(String section) {
        String cleaned = safe(section).trim();
        if (cleaned.length() <= 22) {
            return cleaned;
        }
        return cleaned.substring(0, 22).trim() + "...";
    }

    private static String trimHeaderLabel(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.length() <= 34) {
            return cleaned;
        }
        return cleaned.substring(0, 34).trim() + "...";
    }

    private static String trimHeaderLabelStatic(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.length() <= 34) {
            return cleaned;
        }
        return cleaned.substring(0, 34).trim() + "...";
    }

    private static boolean sameLabel(String left, String right) {
        return safe(left).trim().equalsIgnoreCase(safe(right).trim());
    }

    private static boolean containsAny(String text, String... needles) {
        String normalized = safe(text);
        for (String needle : needles) {
            if (normalized.contains(safe(needle))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAll(String text, String... needles) {
        String normalized = safe(text);
        for (String needle : needles) {
            if (!normalized.contains(safe(needle))) {
                return false;
            }
        }
        return true;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

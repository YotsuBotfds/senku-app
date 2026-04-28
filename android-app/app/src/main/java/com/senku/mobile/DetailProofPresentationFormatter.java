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
import java.util.List;
import java.util.Locale;

final class DetailProofPresentationFormatter {
    static final String MATCH_TYPE_LABEL = "MATCH";

    static final class State {
        final String routeValue;
        final int routeAccentColor;
        final String backendValue;
        final String evidenceStrengthLabel;
        final int sourceCount;
        final int evidenceAccentColor;
        final String revisionStamp;
        final int lowCoverageAccentColor;
        final boolean lowCoverageOrAbstain;
        final ReviewedCardMetadata reviewedCardMetadata;

        State(
            String routeValue,
            int routeAccentColor,
            String backendValue,
            String evidenceStrengthLabel,
            int sourceCount,
            int evidenceAccentColor,
            String revisionStamp,
            int lowCoverageAccentColor,
            boolean lowCoverageOrAbstain,
            ReviewedCardMetadata reviewedCardMetadata
        ) {
            this.routeValue = safe(routeValue).trim();
            this.routeAccentColor = routeAccentColor;
            this.backendValue = safe(backendValue).trim();
            this.evidenceStrengthLabel = safe(evidenceStrengthLabel).trim();
            this.sourceCount = Math.max(0, sourceCount);
            this.evidenceAccentColor = evidenceAccentColor;
            this.revisionStamp = safe(revisionStamp).trim();
            this.lowCoverageAccentColor = lowCoverageAccentColor;
            this.lowCoverageOrAbstain = lowCoverageOrAbstain;
            this.reviewedCardMetadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        }
    }

    private static final class StructuredLine {
        final String label;
        final String value;
        final int accentColor;
        final boolean telemetryValue;

        StructuredLine(String label, String value, int accentColor) {
            this(label, value, accentColor, false);
        }

        StructuredLine(String label, String value, int accentColor, boolean telemetryValue) {
            this.label = safe(label);
            this.value = safe(value);
            this.accentColor = accentColor;
            this.telemetryValue = telemetryValue;
        }
    }

    static final class CompactReviewedCardLine {
        final String label;
        final String value;

        CompactReviewedCardLine(String label, String value) {
            this.label = safe(label);
            this.value = safe(value);
        }
    }

    private final Context context;

    DetailProofPresentationFormatter(Context context) {
        this.context = context;
    }

    SpannableStringBuilder buildWhySummary(
        State state,
        List<SearchResult> currentSources,
        boolean compact,
        boolean utilityRail
    ) {
        if (currentSources == null || currentSources.isEmpty()) {
            return buildNoCitationProofSummary(state, compact);
        }
        SearchResult firstSource = currentSources.get(0);
        ArrayList<StructuredLine> lines = buildBaseProofLines(state, currentSources, firstSource, compact);
        addProofDetailLines(lines, state, currentSources, firstSource, compact, utilityRail);
        return buildStructuredLineBlock(lines);
    }

    SpannableStringBuilder buildNoCitationProofSummary(State state, boolean compact) {
        ArrayList<StructuredLine> lines = new ArrayList<>();
        if (compact) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_evidence),
                context.getString(R.string.detail_external_review_no_citations_short),
                state.lowCoverageAccentColor
            ));
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_corpus),
                context.getString(R.string.detail_external_review_corpus_limit_no_citations),
                state.lowCoverageAccentColor
            ));
            return buildStructuredLineBlock(lines);
        }
        lines.add(new StructuredLine(
            context.getString(R.string.detail_external_review_proof_route),
            state.routeValue,
            state.routeAccentColor,
            true
        ));
        if (!state.backendValue.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_backend),
                state.backendValue,
                context.getColor(R.color.senku_text_muted_light),
                true
            ));
        }
        lines.add(new StructuredLine(
            context.getString(R.string.detail_external_review_proof_evidence),
            context.getString(compact
                ? R.string.detail_external_review_no_citations_short
                : R.string.detail_external_review_no_citations_value),
            state.lowCoverageAccentColor
        ));
        lines.add(new StructuredLine(
            context.getString(R.string.detail_external_review_proof_corpus),
            context.getString(R.string.detail_external_review_corpus_limit_no_citations),
            state.lowCoverageAccentColor
        ));
        appendProofRevisionLine(lines, state.revisionStamp);
        if (!compact) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_verify),
                context.getString(R.string.detail_external_review_no_citations_verify),
                context.getColor(R.color.senku_text_muted_light)
            ));
        }
        return buildStructuredLineBlock(lines);
    }

    String buildPrimarySourcePreviewLine(List<SearchResult> currentSources) {
        if (currentSources == null || currentSources.isEmpty()) {
            return "";
        }
        SearchResult primary = currentSources.get(0);
        String guideId = safe(primary == null ? null : primary.guideId).trim();
        String sourceLabel = buildPrimarySourceLabel(currentSources);
        StringBuilder preview = new StringBuilder();
        if (!guideId.isEmpty() || !sourceLabel.isEmpty()) {
            preview.append("ANCHOR ");
            if (!guideId.isEmpty()) {
                preview.append("[").append(guideId).append("]");
                if (!sourceLabel.isEmpty()) {
                    preview.append(" - ");
                }
            }
            if (!sourceLabel.isEmpty()) {
                preview.append(sourceLabel);
            }
        }
        return preview.toString();
    }

    String buildPrimarySourceLabel(List<SearchResult> currentSources) {
        if (currentSources == null || currentSources.isEmpty()) {
            return "";
        }
        SearchResult primary = currentSources.get(0);
        String section = safe(primary == null ? null : primary.sectionHeading).trim();
        if (!section.isEmpty()) {
            return trimHeaderLabel(section);
        }
        String title = safe(primary == null ? null : primary.title).trim();
        return trimHeaderLabel(title);
    }

    SpannableStringBuilder buildProvenanceMetaText(State state, SearchResult source, List<SearchResult> currentSources) {
        ArrayList<StructuredLine> lines = new ArrayList<>();
        lines.add(new StructuredLine(
            context.getString(R.string.detail_external_review_proof_route),
            state.routeValue,
            state.routeAccentColor,
            true
        ));
        if (!state.backendValue.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_backend),
                state.backendValue,
                context.getColor(R.color.senku_text_muted_light),
                true
            ));
        }
        String entryValue = buildSourceEntryValue(source, currentSources);
        if (!entryValue.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_lead),
                entryValue,
                context.getColor(R.color.senku_accent_olive)
            ));
        }
        String section = safe(source == null ? null : source.sectionHeading).trim();
        if (!section.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_section),
                trimHeaderLabel(section),
                context.getColor(R.color.senku_text_muted_light)
            ));
        }
        appendReviewedCardLines(lines, state, false);
        appendProofRevisionLine(lines, state.revisionStamp);
        return buildStructuredLineBlock(lines);
    }

    private ArrayList<StructuredLine> buildBaseProofLines(
        State state,
        List<SearchResult> currentSources,
        SearchResult firstSource,
        boolean compact
    ) {
        ArrayList<StructuredLine> lines = new ArrayList<>();
        if (compact) {
            String leadValue = buildSourceEntryValue(firstSource, currentSources);
            if (!leadValue.isEmpty()) {
                lines.add(new StructuredLine(
                    "ANCHOR",
                    leadValue,
                    context.getColor(R.color.senku_accent_olive)
                ));
            }
            lines.add(new StructuredLine(
                "SOURCES",
                state.evidenceStrengthLabel + " | " + formatCountLabel(state.sourceCount, "source", "sources"),
                state.evidenceAccentColor
            ));
            if (state.reviewedCardMetadata.isPresent()) {
                appendReviewedCardLines(lines, state, true);
            }
            return lines;
        }
        lines.add(new StructuredLine(
            context.getString(R.string.detail_external_review_proof_route),
            state.routeValue,
            state.routeAccentColor,
            true
        ));
        if (!state.backendValue.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_backend),
                state.backendValue,
                context.getColor(R.color.senku_text_muted_light),
                true
            ));
        }
        lines.add(new StructuredLine(
            context.getString(R.string.detail_external_review_proof_evidence),
            state.evidenceStrengthLabel + " | " + formatCountLabel(state.sourceCount, "source", "sources"),
            state.evidenceAccentColor
        ));
        String leadValue = buildSourceEntryValue(firstSource, currentSources);
        if (!leadValue.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_lead),
                leadValue,
                context.getColor(R.color.senku_accent_olive)
            ));
        }
        appendReviewedCardLines(lines, state, compact);
        appendProofRevisionLine(lines, state.revisionStamp);
        return lines;
    }

    private void appendReviewedCardLines(List<StructuredLine> lines, State state, boolean compact) {
        ReviewedCardMetadata metadata = state == null
            ? ReviewedCardMetadata.empty()
            : ReviewedCardMetadata.normalize(state.reviewedCardMetadata);
        if (!metadata.isPresent()) {
            return;
        }
        if (compact) {
            appendCompactReviewedCardLines(lines, metadata);
            return;
        }
        int accentColor = context.getColor(R.color.senku_text_muted_light);
        if (!metadata.cardId.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_card),
                metadata.cardId,
                accentColor,
                true
            ));
        }
        if (!metadata.reviewStatus.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_review),
                humanizeReviewedCardToken(metadata.reviewStatus),
                accentColor
            ));
        }
        if (!metadata.cardGuideId.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_card_guide),
                metadata.cardGuideId,
                accentColor,
                true
            ));
        }
        String sourceGuideIds = metadata.citedSourceGuideIdsCsv();
        if (!sourceGuideIds.isEmpty()) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_reviewed_sources),
                sourceGuideIds,
                accentColor,
                true
            ));
        }
    }

    private void appendCompactReviewedCardLines(List<StructuredLine> lines, ReviewedCardMetadata metadata) {
        int accentColor = context.getColor(R.color.senku_text_muted_light);
        for (CompactReviewedCardLine line : compactReviewedCardLines(metadata)) {
            lines.add(new StructuredLine(line.label, line.value, accentColor));
        }
    }

    static List<CompactReviewedCardLine> compactReviewedCardLines(ReviewedCardMetadata metadata) {
        ArrayList<CompactReviewedCardLine> lines = new ArrayList<>();
        ReviewedCardMetadata normalized = ReviewedCardMetadata.normalize(metadata);
        lines.add(new CompactReviewedCardLine("CARD", compactReviewedCardValue(normalized)));
        if (!normalized.cardGuideId.isEmpty()) {
            lines.add(new CompactReviewedCardLine("ANCHOR", normalized.cardGuideId));
        }
        String sourceGuideIds = normalized.citedSourceGuideIdsCsv();
        if (!sourceGuideIds.isEmpty() && !sourceGuideIds.equals(normalized.cardGuideId)) {
            lines.add(new CompactReviewedCardLine("RELATED", sourceGuideIds));
        }
        return lines;
    }

    private static String compactReviewedCardValue(ReviewedCardMetadata metadata) {
        if (metadata == null || metadata.cardId.isEmpty()) {
            return "reviewed support";
        }
        String status = humanizeReviewedCardToken(metadata.reviewStatus);
        if (status.isEmpty()) {
            return metadata.cardId;
        }
        return metadata.cardId + " | " + status;
    }

    private void addProofDetailLines(
        List<StructuredLine> lines,
        State state,
        List<SearchResult> currentSources,
        SearchResult firstSource,
        boolean compact,
        boolean utilityRail
    ) {
        if (firstSource == null) {
            return;
        }
        String section = safe(firstSource.sectionHeading).trim();
        String anchorLabel = buildPrimarySourceLabel(currentSources);
        if (!section.isEmpty()
            && !section.equalsIgnoreCase(anchorLabel)
            && (!compact || !state.lowCoverageOrAbstain)) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_section),
                trimHeaderLabel(section),
                context.getColor(R.color.senku_text_muted_light)
            ));
        }
        String retrievalMode = safe(firstSource.retrievalMode).trim();
        if (!retrievalMode.isEmpty() && (!compact || !utilityRail)) {
            lines.add(new StructuredLine(
                MATCH_TYPE_LABEL,
                humanizeRetrievalMode(retrievalMode),
                context.getColor(R.color.senku_text_muted_light)
            ));
        }
        if (state.lowCoverageOrAbstain) {
            lines.add(new StructuredLine(
                context.getString(R.string.detail_external_review_proof_corpus),
                context.getString(R.string.detail_external_review_corpus_limit_low_coverage),
                state.lowCoverageAccentColor
            ));
        }
    }

    static String humanizeRetrievalMode(String retrievalMode) {
        String mode = safe(retrievalMode).trim().toLowerCase(Locale.US);
        if (mode.isEmpty()) {
            return "";
        }
        switch (mode) {
            case "guide-focus":
            case "guide":
                return "Related";
            case "route-focus":
            case "hybrid":
                return "Best match";
            case "vector":
                return "Concept match";
            case "lexical":
                return "Keyword match";
            case "ai-generated":
                return "Generated answer";
            default:
                return humanizeFallbackToken(mode);
        }
    }

    private static String humanizeReviewedCardToken(String token) {
        return safe(token).trim().replace('_', ' ');
    }

    private static String humanizeFallbackToken(String token) {
        String cleaned = safe(token).trim().replace('-', ' ').replace('_', ' ');
        if (cleaned.isEmpty()) {
            return "";
        }
        String[] words = cleaned.split("\\s+");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(word.substring(0, 1).toUpperCase(Locale.US));
            if (word.length() > 1) {
                label.append(word.substring(1));
            }
        }
        return label.toString();
    }

    String buildSourceEntryValue(SearchResult source, List<SearchResult> currentSources) {
        if (source == null) {
            return "";
        }
        String guideId = safe(source.guideId).trim();
        String title = safe(source.title).trim();
        StringBuilder value = new StringBuilder();
        if (!guideId.isEmpty()) {
            value.append("[").append(guideId).append("]");
        }
        if (!title.isEmpty()) {
            if (value.length() > 0) {
                value.append(" ");
            }
            value.append(trimHeaderLabel(title));
        }
        if (value.length() == 0) {
            value.append(trimHeaderLabel(buildPrimarySourceLabel(currentSources)));
        }
        return value.toString().trim();
    }

    private void appendProofRevisionLine(List<StructuredLine> lines, String revisionStamp) {
        if (safe(revisionStamp).isEmpty()) {
            return;
        }
        lines.add(new StructuredLine(
            context.getString(R.string.detail_loop4_proof_revision),
            revisionStamp,
            context.getColor(R.color.senku_text_muted_light),
            true
        ));
    }

    private SpannableStringBuilder buildStructuredLineBlock(List<StructuredLine> lines) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            StructuredLine line = lines.get(i);
            if (line == null || safe(line.value).trim().isEmpty()) {
                continue;
            }
            int labelStart = builder.length();
            builder.append(safe(line.label).trim().toUpperCase(Locale.US)).append("  ");
            int valueStart = builder.length();
            builder.append(safe(line.value).trim());
            int valueEnd = builder.length();
            builder.setSpan(new StyleSpan(Typeface.BOLD), labelStart, valueStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new TypefaceSpan("monospace"), labelStart, valueStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.9f), labelStart, valueStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(
                new ForegroundColorSpan(line.accentColor),
                labelStart,
                valueStart,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            builder.setSpan(
                new ForegroundColorSpan(context.getColor(R.color.senku_text_light)),
                valueStart,
                valueEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            if (line.telemetryValue) {
                builder.setSpan(
                    new TypefaceSpan("monospace"),
                    valueStart,
                    valueEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                builder.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    valueStart,
                    valueEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                builder.setSpan(
                    new RelativeSizeSpan(0.94f),
                    valueStart,
                    valueEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            if (i < lines.size() - 1) {
                builder.append('\n');
            }
        }
        return builder;
    }

    private String trimHeaderLabel(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.length() <= 34) {
            return cleaned;
        }
        return cleaned.substring(0, 34).trim() + "...";
    }

    private String formatCountLabel(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

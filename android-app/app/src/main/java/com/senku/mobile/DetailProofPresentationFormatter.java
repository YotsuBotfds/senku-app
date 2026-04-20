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

        State(
            String routeValue,
            int routeAccentColor,
            String backendValue,
            String evidenceStrengthLabel,
            int sourceCount,
            int evidenceAccentColor,
            String revisionStamp,
            int lowCoverageAccentColor,
            boolean lowCoverageOrAbstain
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
        ArrayList<StructuredLine> lines = buildBaseProofLines(state, currentSources, firstSource);
        addProofDetailLines(lines, state, currentSources, firstSource, compact, utilityRail);
        return buildStructuredLineBlock(lines);
    }

    SpannableStringBuilder buildNoCitationProofSummary(State state, boolean compact) {
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
            preview.append("Primary source: ");
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
        appendProofRevisionLine(lines, state.revisionStamp);
        return buildStructuredLineBlock(lines);
    }

    private ArrayList<StructuredLine> buildBaseProofLines(
        State state,
        List<SearchResult> currentSources,
        SearchResult firstSource
    ) {
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
        appendProofRevisionLine(lines, state.revisionStamp);
        return lines;
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
                context.getString(R.string.detail_external_review_proof_lane),
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

    private String humanizeRetrievalMode(String retrievalMode) {
        String mode = safe(retrievalMode).trim().toLowerCase(Locale.US);
        if (mode.isEmpty()) {
            return "";
        }
        switch (mode) {
            case "guide-focus":
                return "guide focus";
            case "route-focus":
                return "route focus";
            case "ai-generated":
                return "AI generated";
            default:
                return mode.replace('-', ' ');
        }
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

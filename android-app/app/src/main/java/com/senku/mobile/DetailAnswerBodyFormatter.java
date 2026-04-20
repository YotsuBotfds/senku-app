package com.senku.mobile;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class DetailAnswerBodyFormatter {
    private static final Pattern NUMBERED_STEP_PREFIX_PATTERN = Pattern.compile("^\\d+\\.\\s*");
    private static final String[] ANSWER_SECTION_LABELS = new String[] {
        "Short answer:",
        "Steps:",
        "Limits or safety:"
    };

    private static final class AnswerBodySection {
        final String label;
        final ArrayList<String> lines = new ArrayList<>();

        AnswerBodySection(String label) {
            this.label = safe(label).trim();
        }
    }

    private final Context context;

    DetailAnswerBodyFormatter(Context context) {
        this.context = context;
    }

    String formatAnswerBody(String body) {
        String cleaned = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(body).trim();
        if (cleaned.regionMatches(true, 0, "Answer\n", 0, "Answer\n".length())) {
            cleaned = cleaned.substring("Answer\n".length()).trim();
        }
        int sourcesIndex = cleaned.indexOf("\n\nSources used");
        if (sourcesIndex >= 0) {
            cleaned = cleaned.substring(0, sourcesIndex).trim();
        }
        cleaned = collapseEmptyAnswerSections(cleaned);
        cleaned = addSectionLabelSpacing(cleaned);
        return cleaned;
    }

    private String collapseEmptyAnswerSections(String body) {
        String trimmedBody = safe(body).trim();
        if (trimmedBody.isEmpty() || !containsKnownAnswerSection(trimmedBody)) {
            return trimmedBody;
        }

        String[] lines = trimmedBody.split("\\R", -1);
        ArrayList<String> preambleLines = new ArrayList<>();
        ArrayList<AnswerBodySection> sections = new ArrayList<>();
        AnswerBodySection currentSection = null;
        for (String line : lines) {
            String trimmedLine = safe(line).trim();
            if (isKnownAnswerSectionLabel(trimmedLine)) {
                if (currentSection != null) {
                    sections.add(currentSection);
                }
                currentSection = new AnswerBodySection(trimmedLine);
                continue;
            }
            if (currentSection == null) {
                preambleLines.add(line);
            } else {
                currentSection.lines.add(line);
            }
        }
        if (currentSection != null) {
            sections.add(currentSection);
        }

        StringBuilder rebuilt = new StringBuilder(trimmedBody.length());
        appendAnswerBodyBlock(rebuilt, trimEmptyBoundaryLines(preambleLines));
        for (AnswerBodySection section : sections) {
            ArrayList<String> visibleLines = visibleAnswerSectionLines(section);
            if (visibleLines.isEmpty()) {
                continue;
            }
            ArrayList<String> blockLines = new ArrayList<>(visibleLines.size() + 1);
            blockLines.add(section.label);
            blockLines.addAll(visibleLines);
            appendAnswerBodyBlock(rebuilt, blockLines);
        }
        return rebuilt.toString().trim();
    }

    private ArrayList<String> visibleAnswerSectionLines(AnswerBodySection section) {
        ArrayList<String> rawLines = trimEmptyBoundaryLines(section == null ? null : section.lines);
        if (rawLines.isEmpty()) {
            return rawLines;
        }
        if (!"Steps:".equals(section.label)) {
            return rawLines;
        }
        ArrayList<String> filtered = new ArrayList<>(rawLines.size());
        boolean substantiveContentFound = false;
        for (String line : rawLines) {
            if (isEffectivelyEmptyStepsLine(line)) {
                continue;
            }
            filtered.add(line);
            if (!safe(line).trim().isEmpty()) {
                substantiveContentFound = true;
            }
        }
        if (!substantiveContentFound) {
            return new ArrayList<>();
        }
        return trimEmptyBoundaryLines(filtered);
    }

    private boolean isEffectivelyEmptyStepsLine(String line) {
        String normalized = safe(line).trim();
        if (normalized.isEmpty()) {
            return true;
        }
        normalized = NUMBERED_STEP_PREFIX_PATTERN.matcher(normalized).replaceFirst("");
        normalized = normalized.replaceFirst("^[\\-\\u2022]+\\s*", "");
        normalized = normalized.replace("(", "");
        normalized = normalized.replace(")", "");
        normalized = normalized.replaceAll("\\s+", " ").trim().toLowerCase(Locale.US);
        normalized = normalized.replaceAll("[.!:]+$", "").trim();
        return normalized.equals("no steps available")
            || normalized.equals("no steps are available")
            || normalized.equals("no steps were found")
            || normalized.equals(
                context.getString(R.string.detail_loop2_corpus_gap_label).toLowerCase(Locale.US).replaceAll("[.!:]+$", "").trim()
            )
            || normalized.equals(
                context.getString(R.string.detail_loop2_corpus_gap_no_steps).toLowerCase(Locale.US).replaceAll("[.!:]+$", "").trim()
            )
            || normalized.equals(
                context.getString(R.string.detail_loop2_corpus_gap_followup).toLowerCase(Locale.US).replaceAll("[.!:]+$", "").trim()
            );
    }

    private static boolean containsKnownAnswerSection(String body) {
        String[] lines = safe(body).split("\\R", -1);
        for (String line : lines) {
            if (isKnownAnswerSectionLabel(safe(line).trim())) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<String> trimEmptyBoundaryLines(List<String> lines) {
        ArrayList<String> trimmed = new ArrayList<>();
        if (lines == null || lines.isEmpty()) {
            return trimmed;
        }
        int start = 0;
        int end = lines.size();
        while (start < end && safe(lines.get(start)).trim().isEmpty()) {
            start++;
        }
        while (end > start && safe(lines.get(end - 1)).trim().isEmpty()) {
            end--;
        }
        for (int index = start; index < end; index++) {
            trimmed.add(safe(lines.get(index)));
        }
        return trimmed;
    }

    private static void appendAnswerBodyBlock(StringBuilder builder, List<String> lines) {
        if (builder == null || lines == null || lines.isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append("\n\n");
        }
        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                builder.append('\n');
            }
            builder.append(safe(lines.get(index)));
        }
    }

    private static String addSectionLabelSpacing(String body) {
        if (body.isEmpty()) {
            return "";
        }

        String[] lines = body.split("\\R", -1);
        StringBuilder spaced = new StringBuilder(body.length() + 12);
        boolean previousLineBlank = true;
        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i];
            String trimmedLine = currentLine.trim();
            if (isKnownAnswerSectionLabel(trimmedLine)) {
                if (!previousLineBlank && spaced.length() > 0) {
                    spaced.append('\n');
                }
                spaced.append(trimmedLine);
            } else {
                spaced.append(currentLine);
            }
            if (i < lines.length - 1) {
                spaced.append('\n');
            }
            previousLineBlank = trimmedLine.isEmpty();
        }
        return spaced.toString().trim();
    }

    private static boolean isKnownAnswerSectionLabel(String trimmedLine) {
        for (String label : ANSWER_SECTION_LABELS) {
            if (trimmedLine.equals(label)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

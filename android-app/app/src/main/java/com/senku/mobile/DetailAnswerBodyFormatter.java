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
    private static final String SUMMARY_DISPLAY_LABEL = "ANSWER";
    private static final String STEPS_DISPLAY_LABEL = "STEPS";
    private static final String SAFETY_DISPLAY_LABEL = "WATCH";
    private static final String STEPS_LABEL = "Steps:";
    private static final String LIMITS_OR_SAFETY_LABEL = "Limits or safety:";

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
            String sectionLabel = knownAnswerSectionLabelPrefix(trimmedLine);
            if (!sectionLabel.isEmpty()) {
                if (currentSection != null) {
                    sections.add(currentSection);
                }
                currentSection = new AnswerBodySection(sectionLabel);
                String inlineContent = trimmedLine.substring(sectionLabel.length()).trim();
                if (!inlineContent.isEmpty()) {
                    currentSection.lines.add(inlineContent);
                }
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

        ArrayList<String> visiblePreambleLines = new ArrayList<>();
        ArrayList<String> preambleSafetyLines = new ArrayList<>();
        for (String line : trimEmptyBoundaryLines(preambleLines)) {
            if (isSafetyLine(line)) {
                preambleSafetyLines.add(line);
            } else {
                visiblePreambleLines.add(line);
            }
        }

        StringBuilder rebuilt = new StringBuilder(trimmedBody.length());
        appendAnswerBodyBlock(rebuilt, visiblePreambleLines);
        for (String label : ANSWER_SECTION_LABELS) {
            AnswerBodySection section = mergedSection(sections, label, preambleSafetyLines);
            ArrayList<String> visibleLines = visibleAnswerSectionLines(section);
            if (visibleLines.isEmpty()) {
                continue;
            }
            ArrayList<String> blockLines = new ArrayList<>(visibleLines.size() + 1);
            blockLines.add(displayLabelForSection(section.label));
            blockLines.addAll(visibleLines);
            appendAnswerBodyBlock(rebuilt, blockLines);
        }
        return rebuilt.toString().trim();
    }

    private AnswerBodySection mergedSection(List<AnswerBodySection> sections, String label, List<String> preambleSafetyLines) {
        AnswerBodySection merged = new AnswerBodySection(label);
        if (sections == null || sections.isEmpty()) {
            if (LIMITS_OR_SAFETY_LABEL.equals(label) && preambleSafetyLines != null) {
                merged.lines.addAll(preambleSafetyLines);
            }
            return merged;
        }
        for (AnswerBodySection section : sections) {
            if (section == null || !label.equals(section.label)) {
                continue;
            }
            merged.lines.addAll(section.lines);
        }
        if (!LIMITS_OR_SAFETY_LABEL.equals(label)) {
            return merged;
        }
        if (preambleSafetyLines != null) {
            for (String line : preambleSafetyLines) {
                if (!containsLine(merged.lines, line)) {
                    merged.lines.add(line);
                }
            }
        }
        for (AnswerBodySection section : sections) {
            if (section == null || LIMITS_OR_SAFETY_LABEL.equals(section.label)) {
                continue;
            }
            for (String line : section.lines) {
                if (isSafetyLine(line) && !containsLine(merged.lines, line)) {
                    merged.lines.add(line);
                }
            }
        }
        return merged;
    }

    private ArrayList<String> visibleAnswerSectionLines(AnswerBodySection section) {
        ArrayList<String> rawLines = trimEmptyBoundaryLines(section == null ? null : section.lines);
        if (rawLines.isEmpty()) {
            return rawLines;
        }
        if (!STEPS_LABEL.equals(section.label)) {
            return rawLines;
        }
        ArrayList<String> filtered = new ArrayList<>(rawLines.size());
        boolean substantiveContentFound = false;
        for (String line : rawLines) {
            if (isEffectivelyEmptyStepsLine(line)) {
                continue;
            }
            if (isSafetyLine(line)) {
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
        if (context == null) {
            return normalized.equals("no steps available")
                || normalized.equals("no steps are available")
                || normalized.equals("no steps were found");
        }
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
            if (!knownAnswerSectionLabelPrefix(safe(line).trim()).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSafetyLine(String line) {
        String normalized = safe(line).trim().toLowerCase(Locale.US);
        return normalized.startsWith("avoid:")
            || normalized.startsWith("- avoid:")
            || normalized.startsWith("limits:")
            || normalized.startsWith("limit:")
            || normalized.startsWith("safety:");
    }

    private static boolean containsLine(List<String> lines, String line) {
        String normalizedLine = safe(line).trim();
        for (String existing : lines) {
            if (safe(existing).trim().equals(normalizedLine)) {
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
            if (isAnswerDisplayLabel(trimmedLine) || isKnownAnswerSectionLabel(trimmedLine)) {
                if (!previousLineBlank && spaced.length() > 0) {
                    spaced.append('\n');
                }
                spaced.append(displayLabelForSection(trimmedLine));
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

    private static String displayLabelForSection(String label) {
        String safeLabel = safe(label).trim();
        if ("Short answer:".equals(safeLabel) || SUMMARY_DISPLAY_LABEL.equals(safeLabel)) {
            return SUMMARY_DISPLAY_LABEL;
        }
        if ("Steps:".equals(safeLabel)
            || STEPS_DISPLAY_LABEL.equals(safeLabel)
            || "FIELD STEPS".equals(safeLabel)) {
            return STEPS_DISPLAY_LABEL;
        }
        if ("Limits or safety:".equals(safeLabel) || SAFETY_DISPLAY_LABEL.equals(safeLabel)) {
            return SAFETY_DISPLAY_LABEL;
        }
        return safeLabel;
    }

    private static boolean isAnswerDisplayLabel(String trimmedLine) {
        return SUMMARY_DISPLAY_LABEL.equals(trimmedLine)
            || STEPS_DISPLAY_LABEL.equals(trimmedLine)
            || "FIELD STEPS".equals(trimmedLine)
            || SAFETY_DISPLAY_LABEL.equals(trimmedLine);
    }

    private static boolean isKnownAnswerSectionLabel(String trimmedLine) {
        for (String label : ANSWER_SECTION_LABELS) {
            if (trimmedLine.equals(label)) {
                return true;
            }
        }
        return false;
    }

    private static String knownAnswerSectionLabelPrefix(String trimmedLine) {
        for (String label : ANSWER_SECTION_LABELS) {
            if (trimmedLine.equals(label) || trimmedLine.startsWith(label + " ")) {
                return label;
            }
        }
        return "";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

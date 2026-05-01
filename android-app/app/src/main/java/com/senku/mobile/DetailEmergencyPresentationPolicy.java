package com.senku.mobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DetailEmergencyPresentationPolicy {
    private DetailEmergencyPresentationPolicy() {
    }

    static List<String> extractImmediateActionLines(String answerText) {
        ArrayList<String> steps = new ArrayList<>();
        boolean collectingActionSection = false;
        for (String rawLine : safe(answerText).split("\\R")) {
            String line = safe(rawLine).trim();
            String normalized = normalize(line);
            if (isImmediateActionHeading(normalized)) {
                collectingActionSection = true;
                continue;
            }
            if (collectingActionSection && isProofBoundaryHeading(normalized)) {
                break;
            }
            if (collectingActionSection && isNumberedActionLine(line)) {
                steps.add(stripNumberedPrefix(line));
            }
        }
        return steps;
    }

    static List<String> extractHighRiskActionCandidateLines(String answerText) {
        ArrayList<String> steps = new ArrayList<>();
        boolean sawActionSection = false;
        boolean collectingActionSection = false;
        boolean fallbackClosedByProof = false;
        for (String rawLine : safe(answerText).split("\\R")) {
            String line = safe(rawLine).trim();
            String normalized = normalize(line);
            if (isImmediateActionHeading(normalized)) {
                sawActionSection = true;
                collectingActionSection = true;
                continue;
            }
            if (collectingActionSection && isProofBoundaryHeading(normalized)) {
                collectingActionSection = false;
                continue;
            }
            if (!sawActionSection && isExplicitProofBoundaryHeading(normalized)) {
                fallbackClosedByProof = true;
                continue;
            }
            if (isNumberedActionLine(line)
                && ((!sawActionSection && !fallbackClosedByProof) || collectingActionSection)) {
                steps.add(stripNumberedPrefix(line));
            }
        }
        return steps;
    }

    static boolean isImmediateActionHeading(String line) {
        String normalizedLine = normalize(line);
        return "steps:".equals(normalizedLine)
            || "steps".equals(normalizedLine)
            || "field steps".equals(normalizedLine)
            || "field steps:".equals(normalizedLine)
            || "field actions".equals(normalizedLine)
            || "field actions:".equals(normalizedLine)
            || "immediate actions".equals(normalizedLine)
            || "immediate actions:".equals(normalizedLine)
            || "emergency actions".equals(normalizedLine)
            || "emergency actions:".equals(normalizedLine)
            || "emergency response:".equals(normalizedLine)
            || "response actions:".equals(normalizedLine)
            || "answer gd-132 - burn hazard response".equals(normalizedLine)
            || "actions:".equals(normalizedLine);
    }

    static boolean isProofBoundaryHeading(String line) {
        String normalizedLine = normalize(line);
        return normalizedLine.endsWith(":")
            || isExplicitProofBoundaryHeading(normalizedLine);
    }

    private static boolean isExplicitProofBoundaryHeading(String normalizedLine) {
        return normalizedLine.startsWith("watch")
            || normalizedLine.startsWith("why this answer")
            || normalizedLine.startsWith("why / source")
            || normalizedLine.startsWith("why/source")
            || normalizedLine.startsWith("why proof")
            || normalizedLine.startsWith("why/proof")
            || normalizedLine.startsWith("evidence")
            || normalizedLine.startsWith("provenance")
            || normalizedLine.startsWith("proof")
            || normalizedLine.startsWith("source / why")
            || normalizedLine.startsWith("source/why")
            || normalizedLine.startsWith("source and why")
            || normalizedLine.startsWith("source proof")
            || normalizedLine.startsWith("route")
            || normalizedLine.startsWith("backend")
            || normalizedLine.startsWith("model")
            || normalizedLine.startsWith("confidence")
            || normalizedLine.startsWith("match type")
            || normalizedLine.startsWith("reviewed card")
            || normalizedLine.startsWith("answer status")
            || normalizedLine.startsWith("normal answer")
            || normalizedLine.startsWith("status")
            || normalizedLine.startsWith("metadata")
            || normalizedLine.startsWith("meta")
            || normalizedLine.startsWith("source")
            || normalizedLine.startsWith("sources")
            || normalizedLine.startsWith("sources and proof")
            || normalizedLine.startsWith("sources proof")
            || normalizedLine.startsWith("guide connection")
            || normalizedLine.startsWith("emergency context");
    }

    private static boolean isNumberedActionLine(String line) {
        return safe(line).matches("^\\d+[.)]\\s+.*");
    }

    private static String stripNumberedPrefix(String line) {
        return safe(line).replaceFirst("^\\d+[.)]\\s*", "").trim();
    }

    private static String normalize(String line) {
        return safe(line).trim().toLowerCase(Locale.US);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

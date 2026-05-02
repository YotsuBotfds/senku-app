package com.senku.mobile;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Locale;

final class PromptAnswerTextPolicy {
    static final String EMPTY_ANSWER_TEXT = "The model returned an empty answer.";

    private PromptAnswerTextPolicy() {
    }

    static String sanitizeAnswerText(String answer) {
        String cleaned = cleanAnswer(answer);
        if (isCorruptedAnswer(cleaned, answer)) {
            return "";
        }
        return cleaned;
    }

    static boolean isLikelyCorruptedAnswer(String answer) {
        return isCorruptedAnswer(answer, answer);
    }

    static String cleanAnswer(String answer) {
        String cleaned = safe(answer).trim();
        if (cleaned.isEmpty()) {
            return EMPTY_ANSWER_TEXT;
        }
        String jsonAnswer = extractJsonAnswerText(cleaned);
        if (!jsonAnswer.isEmpty()) {
            cleaned = jsonAnswer;
        }
        cleaned = removePromptEchoLines(cleaned);
        cleaned = removeInstructionOverrideSentences(cleaned);
        if (cleaned.regionMatches(true, 0, "answer:", 0, "answer:".length())) {
            cleaned = cleaned.substring("answer:".length()).trim();
        } else if (cleaned.regionMatches(true, 0, "a:", 0, "a:".length())) {
            cleaned = cleaned.substring("a:".length()).trim();
        }

        cleaned = normalizeExcerpt(cleaned);
        cleaned = cleaned.replaceAll("(?i)<pad>", "");
        cleaned = cleaned.replaceAll("(?i)<bos>", "");
        cleaned = cleaned.replaceAll("(?i)</bos>", "");
        cleaned = cleaned.replaceAll("(?i)<eos>", "");
        cleaned = cleaned.replaceAll("(?i)</eos>", "");
        cleaned = cleaned.replaceAll("(?i)<unk>", "");
        cleaned = cleaned.replaceAll("(?i)<s>", "");
        cleaned = cleaned.replaceAll("(?i)</s>", "");
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        cleaned = cleaned.replaceAll("(?im)^\\s*question:\\s*.*$", "");
        cleaned = cleaned.replaceAll("(?im)^\\s*answer format:\\s*$", "");
        cleaned = cleaned.replaceAll("(?im)^\\s*answer:\\s*", "");
        cleaned = cleaned.replaceAll("(?im)^\\s*short answer:\\s*$", "");
        cleaned = cleaned.replaceAll("(?im)^\\s*steps:\\s*$", "");
        cleaned = cleaned.replaceAll("(?im)^\\s*limits or safety:\\s*$", "");

        AnswerSections sections = AnswerSections.parse(cleaned);
        if (sections.hasAny()) {
            return sections.render();
        }

        cleaned = cleaned.replaceAll("(?<=[\\]\\.:])\\s+(?=\\d+\\.\\s)", "\n");
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n").trim();
        return cleaned.isEmpty() ? EMPTY_ANSWER_TEXT : cleaned;
    }

    static boolean isLowCoverageAnswer(String answerText) {
        String normalized = safe(answerText).trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return false;
        }
        String[] signals = {
            "do not contain information",
            "does not contain information",
            "do not have information",
            "does not have information",
            "no information available",
            "information not available",
            "no steps are available",
            "no relevant information",
            "not covered in the",
            "the notes do not",
            "the retrieved notes do not",
            "unable to provide",
            "no specific information",
            "no direct information",
            "nothing in the notes",
            "notes do not address",
            "not enough context",
            "insufficient context",
            "outside the provided context",
            "outside the retrieved context",
            "not enough information in the context",
            "insufficient information in the context"
        };
        for (String signal : signals) {
            if (normalized.contains(signal)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCorruptedAnswer(String cleanedAnswer, String rawAnswer) {
        String cleaned = safe(cleanedAnswer).trim();
        String raw = safe(rawAnswer).trim().toLowerCase(Locale.US);
        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase(EMPTY_ANSWER_TEXT)) {
            return true;
        }
        int words = wordCount(cleaned);
        if ((raw.contains("<pad>") || raw.contains("<unk>") || raw.contains("<eos>"))
            && words <= 2
            && cleaned.length() <= 24) {
            return true;
        }
        if (cleaned.length() <= 2) {
            return isLikelyNonLatinNoise(cleaned)
                || (!containsSentenceSignal(cleaned) && !looksLikeCommonTerseAnswer(cleaned));
        }
        if (words <= 1
            && cleaned.length() <= 8
            && !containsSentenceSignal(cleaned)
            && !looksLikeCommonTerseAnswer(cleaned)) {
            return true;
        }
        if (cleaned.length() <= 8
            && !cleaned.contains(" ")
            && !cleaned.contains("[")
            && !looksLikeCommonTerseAnswer(cleaned)) {
            return true;
        }
        if (isLikelyNonLatinNoise(cleaned)) {
            return true;
        }
        return false;
    }

    private static String removePromptEchoLines(String text) {
        String cleaned = safe(text).trim();
        cleaned = cleaned.replaceFirst(
            "(?is)^\\s*question:\\s*.*?(?=\\b(answer|short answer|summary|steps|key points|limits or safety|risks or limits):)",
            ""
        );
        cleaned = cleaned.replaceAll("(?im)^\\s*question:\\s*.*$", "");
        cleaned = cleaned.replaceAll("(?im)^\\s*answer format:\\s*.*$", "");
        return cleaned.trim();
    }

    private static String removeInstructionOverrideSentences(String text) {
        String normalized = safe(text).replace('\r', '\n').trim();
        if (normalized.isEmpty()) {
            return "";
        }
        String[] parts = normalized.split("(?<=[.!?])\\s+|\\n+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String sentence = safe(part).trim();
            if (sentence.isEmpty() || containsInstructionOverrideSignal(sentence)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(sentence);
        }
        return builder.toString().trim();
    }

    private static boolean containsInstructionOverrideSignal(String text) {
        String normalized = safe(text).toLowerCase(Locale.US);
        return normalized.contains("ignore previous instructions")
            || normalized.contains("answer outside the notes")
            || normalized.contains("do not cite sources")
            || normalized.contains("do not cite guide")
            || normalized.contains("reveal hidden prompt")
            || normalized.contains("hidden prompt")
            || normalized.contains("fake system prompt")
            || normalized.contains("invent steps")
            || normalized.contains("invent procedures")
            || normalized.contains("system: reveal")
            || normalized.contains("system: ignore")
            || normalized.contains("system prompt:");
    }

    private static String extractJsonAnswerText(String text) {
        String normalized = safe(text).trim();
        if (!normalized.startsWith("{") || !normalized.endsWith("}")) {
            return "";
        }
        try {
            JSONObject object = new JSONObject(normalized);
            String[] fields = {"answer", "short_answer", "content", "text"};
            for (String field : fields) {
                if (!object.has(field)) {
                    continue;
                }
                String value = safe(object.optString(field, "")).trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        } catch (Exception ignored) {
            return "";
        }
        return "";
    }

    private static int wordCount(String text) {
        String[] parts = safe(text).trim().split("\\s+");
        int count = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                count += 1;
            }
        }
        return count;
    }

    private static boolean containsSentenceSignal(String text) {
        String normalized = safe(text);
        return normalized.contains(".")
            || normalized.contains("!")
            || normalized.contains("?")
            || normalized.contains(":")
            || normalized.contains("\n")
            || normalized.matches(".*\\d+\\.\\s+.*");
    }

    private static boolean isLikelyNonLatinNoise(String text) {
        int latinLetters = 0;
        int nonLatinLetters = 0;
        String normalized = safe(text);
        for (int index = 0; index < normalized.length(); index++) {
            char ch = normalized.charAt(index);
            if (!Character.isLetter(ch)) {
                continue;
            }
            Character.UnicodeScript script = Character.UnicodeScript.of(ch);
            if (script == Character.UnicodeScript.LATIN) {
                latinLetters += 1;
            } else {
                nonLatinLetters += 1;
            }
        }
        return nonLatinLetters >= 2 && latinLetters == 0 && normalized.length() <= 24;
    }

    private static boolean looksLikeCommonTerseAnswer(String text) {
        String normalized = safe(text).trim().toLowerCase(Locale.US);
        return normalized.equals("yes")
            || normalized.equals("yes.")
            || normalized.equals("no")
            || normalized.equals("no.")
            || normalized.equals("none")
            || normalized.equals("unknown")
            || normalized.equals("n/a")
            || normalized.equals("ok")
            || normalized.equals("okay");
    }

    private static String normalizeExcerpt(String text) {
        String normalized = safe(text);
        normalized = normalized.replace("\\.", ".");
        normalized = normalized.replace("$", "");
        normalized = normalized.replaceAll("\\\\text\\{([^}]*)\\}", "$1");
        normalized = normalized.replace("\\", "");
        normalized = normalized.replace("{", "");
        normalized = normalized.replace("}", "");
        normalized = normalized.replaceAll("`+", "");
        normalized = normalized.replaceAll("\\*\\*|__|\\*", "");
        normalized = normalized.replaceAll("(?m)(^|\\s)#+\\s*", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class AnswerSections {
        private static final String SHORT = "short answer:";
        private static final String STEPS = "steps:";
        private static final String LIMITS = "limits or safety:";
        private static final String SUMMARY = "summary:";
        private static final String KEY_POINTS = "key points:";
        private static final String RISKS = "risks or limits:";

        final String first;
        final String second;
        final String third;
        final boolean summaryFormat;

        AnswerSections(String first, String second, String third, boolean summaryFormat) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.summaryFormat = summaryFormat;
        }

        static AnswerSections parse(String text) {
            String lower = safe(text).toLowerCase(Locale.US);
            int shortIndex = lower.indexOf(SHORT);
            int stepsIndex = lower.indexOf(STEPS);
            int limitsIndex = lower.indexOf(LIMITS);
            int summaryIndex = lower.indexOf(SUMMARY);
            int keyPointsIndex = lower.indexOf(KEY_POINTS);
            int risksIndex = lower.indexOf(RISKS);

            String shortAnswer = extractSection(text, shortIndex, SHORT.length(), stepsIndex, limitsIndex);
            String steps = extractSection(text, stepsIndex, STEPS.length(), limitsIndex, -1);
            String limits = extractSection(text, limitsIndex, LIMITS.length(), -1, -1);
            AnswerSections classic = new AnswerSections(
                cleanSection(shortAnswer, false, 1),
                cleanSection(steps, true, 4),
                cleanSection(limits, false, 2),
                false
            );
            if (classic.hasAny()) {
                return classic;
            }

            String summary = extractSection(text, summaryIndex, SUMMARY.length(), keyPointsIndex, risksIndex);
            String keyPoints = extractSection(text, keyPointsIndex, KEY_POINTS.length(), risksIndex, -1);
            String risks = extractSection(text, risksIndex, RISKS.length(), -1, -1);

            return new AnswerSections(
                cleanSection(summary, false, 2),
                cleanSection(keyPoints, true, 4),
                cleanSection(risks, false, 2),
                true
            );
        }

        boolean hasAny() {
            return !first.isEmpty() || !second.isEmpty() || !third.isEmpty();
        }

        String render() {
            StringBuilder builder = new StringBuilder();
            String firstLabel = summaryFormat ? "Summary" : "Short answer";
            String secondLabel = summaryFormat ? "Key points" : "Steps";
            String thirdLabel = summaryFormat ? "Risks or limits" : "Limits or safety";
            if (!first.isEmpty()) {
                builder.append(firstLabel).append(": ").append(first);
            }
            if (!second.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append(secondLabel).append(":\n").append(second);
            }
            if (!third.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append(thirdLabel).append(":\n").append(third);
            }
            String rendered = builder.toString().trim();
            return rendered.isEmpty() ? EMPTY_ANSWER_TEXT : rendered;
        }

        private static String extractSection(String text, int startIndex, int labelLength, int nextA, int nextB) {
            if (startIndex < 0) {
                return "";
            }
            int start = startIndex + labelLength;
            int end = text.length();
            if (nextA >= 0 && nextA > startIndex) {
                end = Math.min(end, nextA);
            }
            if (nextB >= 0 && nextB > startIndex) {
                end = Math.min(end, nextB);
            }
            if (start >= end) {
                return "";
            }
            return text.substring(start, end);
        }

        private static String cleanSection(String text, boolean stepsSection, int maxUnits) {
            String cleaned = normalizeExcerpt(text);
            int repeatedAnswer = cleaned.toLowerCase(Locale.US).indexOf("answer:");
            if (repeatedAnswer >= 0) {
                cleaned = cleaned.substring(0, repeatedAnswer);
            }
            cleaned = cleaned.replaceAll("(?i)\\bshort answer:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)\\bsteps:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)\\blimits or safety:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)\\bsummary:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)\\bkey points:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)\\brisks or limits:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)\\banswer:\\s*", "");
            cleaned = cleaned.replaceAll("\\n{3,}", "\n\n").trim();
            if (stepsSection) {
                return normalizeSteps(cleaned);
            }
            int maxSentences = text == null || text.isBlank() ? 0 : maxUnits;
            if (maxSentences == 0) {
                return "";
            }
            return truncateSentences(cleaned, maxSentences);
        }

        private static String normalizeSteps(String text) {
            String cleaned = safe(text).replaceAll("(?m)^\\s*[-*]\\s*", "");
            cleaned = cleaned.replaceAll("\\s+(?=\\d+\\.\\s)", "\n");
            cleaned = cleaned.replaceAll("\\n{2,}", "\n").trim();

            LinkedHashMap<String, String> uniqueSteps = new LinkedHashMap<>();
            String[] numberedParts = cleaned.split("(?=\\d+\\.\\s)");
            for (String part : numberedParts) {
                String step = normalizeExcerpt(part)
                    .replaceAll("^\\d+\\.\\s*", "")
                    .replaceAll("\\s+", " ")
                    .trim();
                if (step.isEmpty()) {
                    continue;
                }
                String key = step.toLowerCase(Locale.US);
                if (!uniqueSteps.containsKey(key)) {
                    uniqueSteps.put(key, step);
                }
                if (uniqueSteps.size() >= 4) {
                    break;
                }
            }

            if (uniqueSteps.isEmpty()) {
                String[] sentenceParts = normalizeExcerpt(cleaned).split("(?<=[.!?\\]])\\s+");
                for (String sentence : sentenceParts) {
                    String step = sentence.replaceAll("\\s+", " ").trim();
                    if (step.isEmpty()) {
                        continue;
                    }
                    String key = step.toLowerCase(Locale.US);
                    if (!uniqueSteps.containsKey(key)) {
                        uniqueSteps.put(key, step);
                    }
                    if (uniqueSteps.size() >= 4) {
                        break;
                    }
                }
            }

            StringBuilder builder = new StringBuilder();
            int index = 1;
            for (String step : uniqueSteps.values()) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                builder.append(index).append(". ").append(step);
                index += 1;
            }
            return builder.toString().trim();
        }

        private static String truncateSentences(String text, int maxSentences) {
            String cleaned = normalizeExcerpt(text);
            if (cleaned.isEmpty()) {
                return "";
            }
            String[] parts = cleaned.split("(?<=[.!?\\]])\\s+");
            StringBuilder builder = new StringBuilder();
            int kept = 0;
            for (String part : parts) {
                String sentence = part.replaceAll("\\s+", " ").trim();
                if (sentence.isEmpty()) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(sentence);
                kept += 1;
                if (kept >= maxSentences) {
                    break;
                }
            }
            return builder.toString().trim();
        }
    }
}

package com.senku.mobile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PromptBuilder {
    private static final int DEFAULT_CONTEXT_ITEMS = 2;
    private static final int DEFAULT_SOURCE_ITEMS = 3;
    private static final String EMPTY_ANSWER_TEXT = "The model returned an empty answer.";
    private static final int FALLBACK_SUMMARY_CHARS = 160;

    private PromptBuilder() {
    }

    public static String buildOfflineAnswerSystemPrompt(String routingQuery) {
        String effectiveRoutingQuery = safe(routingQuery).trim();
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(effectiveRoutingQuery);
        AnswerFormatSpec answerFormat = answerFormatFor(routeProfile);
        StringBuilder builder = new StringBuilder();
        builder.append("You are Senku, an offline field-guide assistant.\n");
        builder.append("Use only the retrieved notes below.\n");
        builder.append("If the notes clearly cover the question, answer directly from them.\n");
        builder.append("If support is partial, say what is uncertain instead of pretending.\n");
        builder.append("Prefer practical steps, concrete materials, and tradeoffs.\n");
        builder.append("Avoid ellipses and avoid copying markdown symbols into the answer.\n");
        builder.append("Keep the tone clear and useful on a phone screen.\n");
        builder.append("Cite guide IDs in square brackets like [GD-572].\n");
        builder.append("When retrieved notes include metadata lines, treat anchor notes and matching structure/topic tags as stronger evidence than generic support notes.\n");
        for (String guidance : routeProfile.promptGuidanceLines()) {
            builder.append(guidance).append("\n");
        }
        if (routeProfile.isSeasonalHouseSiteSelectionPrompt(effectiveRoutingQuery)) {
            builder.append("For seasonal site-selection questions, answer the siting tradeoff first.\n");
            builder.append("If the retrieved notes support it, talk directly about winter solar gain, summer shade, wind exposure, and orientation.\n");
        }
        return builder.toString().trim();
    }

    public static String buildOfflineAnswerPrompt(String question, List<SearchResult> contextResults, String sessionContext) {
        return buildOfflineAnswerPrompt(question, contextResults, sessionContext, question);
    }

    public static String buildOfflineAnswerPrompt(
        String question,
        List<SearchResult> contextResults,
        String sessionContext,
        String routingQuery
    ) {
        String effectiveRoutingQuery = safe(routingQuery).trim().isEmpty() ? question : routingQuery;
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(effectiveRoutingQuery);
        AnswerFormatSpec answerFormat = answerFormatFor(routeProfile);
        StringBuilder builder = new StringBuilder();
        builder.append("You are Senku, an offline field-guide assistant.\n");
        builder.append("Use only the retrieved notes below.\n");
        builder.append("If the notes clearly cover the question, answer directly from them.\n");
        builder.append("If support is partial, say what is uncertain instead of pretending.\n");
        builder.append("Prefer practical steps, concrete materials, and tradeoffs.\n");
        builder.append("When the notes describe a process, use short numbered steps with one step per line.\n");
        builder.append("Avoid ellipses and avoid copying markdown symbols into the answer.\n");
        builder.append("Keep the tone clear and useful on a phone screen.\n");
        builder.append("Cite guide IDs in square brackets like [GD-572].\n");
        builder.append("Return only these labels once: ").append(answerFormat.labelInstruction).append(".\n");
        builder.append(answerFormat.constraintLine).append("\n");
        builder.append("Do not repeat the prompt, do not include an extra Answer heading, and do not restate the notes verbatim.\n");
        if (!safe(sessionContext).trim().isEmpty()) {
            builder.append("Session context from earlier turns is available below.\n");
            builder.append("Use it only to resolve vague follow-up references like it, that, next, or what about.\n");
            builder.append("If session context conflicts with the retrieved notes, trust the retrieved notes.\n");
            builder.append("Answer the newest follow-up question directly instead of repeating the whole earlier plan.\n");
            builder.append("Only bring forward earlier steps when they are needed to make the follow-up answer clear.\n");
            builder.append("Keep the answer scoped to the same resource, system, or build step shown in the retrieved notes.\n");
            builder.append("Do not broaden a follow-up into generic advice for other materials or supplies unless the retrieved notes explicitly do that.\n");
            if (isContinuationFollowUp(question)) {
                builder.append("If the newest follow-up is asking what comes next, advance one stage past the latest answer instead of restarting from the beginning.\n");
                builder.append("Do not repeat the same stage unless the retrieved notes show a missing prerequisite.\n");
            }
            builder.append("Session context: ").append(sessionContext.trim()).append("\n");
        }
        for (String guidance : routeProfile.promptGuidanceLines()) {
            builder.append(guidance).append("\n");
        }
        if (routeProfile.isSeasonalHouseSiteSelectionPrompt(effectiveRoutingQuery)) {
            builder.append("For seasonal site-selection questions, answer the specific siting tradeoff first instead of falling back to generic drainage or foundation advice.\n");
            builder.append("If the retrieved notes support it, talk directly about winter solar gain, summer shade, wind exposure, and orientation.\n");
        }
        builder.append("Question: ");
        builder.append(question.trim());
        builder.append("\n\nRetrieved notes:\n");

        int preferredContextItems = Math.max(DEFAULT_CONTEXT_ITEMS, routeProfile.preferredContextItems());
        if (routeProfile.isSeasonalHouseSiteSelectionPrompt(effectiveRoutingQuery)) {
            preferredContextItems = Math.max(preferredContextItems, 4);
        }
        int capped = Math.min(preferredContextItems, contextResults.size());
        for (int index = 0; index < capped; index++) {
            SearchResult result = contextResults.get(index);
            builder.append("[").append(index + 1).append("] ");
            builder.append(sourceLabel(result));
            if (!safe(result.sectionHeading).isEmpty()) {
                builder.append(" / ").append(result.sectionHeading.trim());
            }
            builder.append("\n");
            String metadataLine = contextMetadataLine(result, index);
            if (!metadataLine.isEmpty()) {
                builder.append(metadataLine).append("\n");
            }
            builder.append("Note: ");
            builder.append(clip(normalizeExcerpt(preferredPromptExcerpt(result)), routeProfile.promptExcerptChars()));
            builder.append("\n");
        }

        if (capped == 0) {
            builder.append("No retrieved notes were available.\n");
        }

        builder.append("\nAnswer format:\n");
        for (String label : answerFormat.labels) {
            builder.append(label).append(":\n");
        }
        builder.append("\nAnswer:\n");
        return builder.toString();
    }

    public static String buildAnswerBody(String answer, List<SearchResult> contextResults, long elapsedMs) {
        StringBuilder builder = new StringBuilder();
        builder.append("Answer\n");
        builder.append(cleanAnswer(answer));
        builder.append("\n\nSources used");
        if (elapsedMs > 0) {
            builder.append(" (").append(formatDuration(elapsedMs)).append(")");
        }
        builder.append("\n");

        LinkedHashMap<String, SearchResult> uniqueSources = new LinkedHashMap<>();
        int capped = Math.min(DEFAULT_SOURCE_ITEMS, contextResults.size());
        for (int index = 0; index < capped; index++) {
            SearchResult result = contextResults.get(index);
            String key = buildSourceKey(result);
            if (!uniqueSources.containsKey(key)) {
                uniqueSources.put(key, result);
            }
        }

        for (Map.Entry<String, SearchResult> entry : uniqueSources.entrySet()) {
            SearchResult result = entry.getValue();
            builder.append("- ").append(sourceLabel(result));
            if (!safe(result.sectionHeading).isEmpty()) {
                builder.append(" :: ").append(result.sectionHeading.trim());
            }
            if (!safe(result.retrievalMode).isEmpty()) {
                builder.append(" (").append(result.retrievalMode).append(")");
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    public static String buildStreamingAnswerBody(String answer) {
        String sanitized = sanitizeAnswerText(answer);
        if (sanitized.isEmpty()) {
            return "Answer";
        }
        return ("Answer\n" + sanitized).trim();
    }

    public static String sanitizeAnswerText(String answer) {
        String cleaned = cleanAnswer(answer);
        if (isCorruptedAnswer(cleaned, answer)) {
            return "";
        }
        return cleaned;
    }

    public static boolean isLikelyCorruptedAnswer(String answer) {
        return isCorruptedAnswer(answer, answer);
    }

    public static String buildSourceFallbackSummary(String query, List<SearchResult> contextResults) {
        if (contextResults == null || contextResults.isEmpty()) {
            return "Generated text was unreliable. Review the sources below.";
        }

        LinkedHashMap<String, SearchResult> uniqueSources = new LinkedHashMap<>();
        int capped = Math.min(DEFAULT_SOURCE_ITEMS, contextResults.size());
        for (int index = 0; index < capped; index++) {
            SearchResult result = contextResults.get(index);
            String key = buildSourceKey(result);
            if (!uniqueSources.containsKey(key)) {
                uniqueSources.put(key, result);
            }
        }

        String topExcerpt = "";
        String secondExcerpt = "";
        String sourceCitations = "";
        int srcIdx = 0;
        for (SearchResult result : uniqueSources.values()) {
            String label = sourceLabel(result);
            String citation = safe(result.guideId).trim().isEmpty() ? label : "[" + result.guideId.trim() + "]";
            if (srcIdx == 0) {
                sourceCitations = citation;
                topExcerpt = compactExcerpt(preferredPromptExcerpt(result), 120);
            } else if (srcIdx == 1) {
                sourceCitations = sourceCitations + ", " + citation;
                secondExcerpt = compactExcerpt(preferredPromptExcerpt(result), 80);
            }
            srcIdx++;
        }

        StringBuilder builder = new StringBuilder();

        builder.append("Short answer: ");
        if (topExcerpt.isEmpty()) {
            builder.append("See source notes below.");
        } else {
            builder.append(topExcerpt);
        }
        if (!sourceCitations.isEmpty()) {
            builder.append(" (").append(sourceCitations).append(")");
        }
        builder.append("\n");

        builder.append("Steps:\n");
        if (!topExcerpt.isEmpty()) {
            builder.append("1. ").append(firstSentence(topExcerpt)).append("\n");
        }
        if (!secondExcerpt.isEmpty()) {
            builder.append("2. ").append(firstSentence(secondExcerpt)).append("\n");
        }
        if (topExcerpt.isEmpty() && secondExcerpt.isEmpty()) {
            builder.append("(no steps available)\n");
        }

        String safetyHint = extractSafetyHint(uniqueSources);
        builder.append("Limits or safety: ");
        if (safetyHint.isEmpty()) {
            builder.append("Follow local regulations and site-specific conditions.");
        } else {
            builder.append(safetyHint);
        }
        builder.append("\n");

        return builder.toString().trim();
    }

    private static String compactExcerpt(String text, int maxChars) {
        String normalized = normalizeExcerpt(text);
        if (normalized.isEmpty()) {
            return "";
        }
        return clip(normalized, maxChars);
    }

    private static String firstSentence(String text) {
        String trimmed = safe(text).trim();
        int dot = trimmed.indexOf('.');
        if (dot > 0 && dot < trimmed.length() - 1) {
            return trimmed.substring(0, dot + 1).trim();
        }
        return clip(trimmed, 80);
    }

    private static String extractSafetyHint(LinkedHashMap<String, SearchResult> sources) {
        String[] markers = {"risk", "hazard", "danger", "safety", "warning", "caution", "limit", "do not", "avoid", "never", "maximum", "minimum"};
        for (SearchResult result : sources.values()) {
            String excerpt = normalizeExcerpt(preferredPromptExcerpt(result)).toLowerCase(Locale.US);
            for (String marker : markers) {
                int idx = excerpt.indexOf(marker);
                if (idx >= 0) {
                    String surrounding = excerpt.substring(Math.max(0, idx - 20), Math.min(excerpt.length(), idx + 80));
                    String clean = normalizeExcerpt(surrounding).trim();
                    if (!clean.isEmpty()) {
                        return clip(clean, 120);
                    }
                }
            }
        }
        return "";
    }

    public static String formatDuration(long elapsedMs) {
        if (elapsedMs <= 0) {
            return "unknown time";
        }
        if (elapsedMs < 1000L) {
            return elapsedMs + " ms";
        }
        double seconds = elapsedMs / 1000.0;
        return String.format(Locale.US, "%.1f s", seconds);
    }

    private static String preferredPromptExcerpt(SearchResult result) {
        if (result.body != null && !result.body.trim().isEmpty()) {
            return result.body;
        }
        return result.snippet;
    }

    private static String buildSourceKey(SearchResult result) {
        String guideId = safe(result.guideId).replaceAll("\\s+", " ").trim().toLowerCase(Locale.US);
        String section = safe(result.sectionHeading).replaceAll("\\s+", " ").trim().toLowerCase(Locale.US);
        if (!guideId.isEmpty()) {
            return guideId + "::" + section;
        }
        return sourceLabel(result).replaceAll("\\s+", " ").trim().toLowerCase(Locale.US);
    }

    private static String clip(String text, int limit) {
        String safe = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        if (safe.length() <= limit) {
            return safe;
        }
        int hardLimit = Math.max(0, limit - 3);
        String prefix = safe.substring(0, hardLimit).trim();
        int sentenceBreak = lastSentenceBreak(prefix);
        if (sentenceBreak >= Math.max(48, hardLimit / 2)) {
            return prefix.substring(0, sentenceBreak).trim();
        }
        return prefix + "...";
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

    private static String sourceLabel(SearchResult result) {
        String guideId = safe(result.guideId).trim();
        String title = safe(result.title).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return "[" + guideId + "] " + title;
        }
        if (!guideId.isEmpty()) {
            return "[" + guideId + "]";
        }
        return title.isEmpty() ? "Unknown source" : title;
    }

    private static String contextMetadataLine(SearchResult result, int index) {
        StringBuilder builder = new StringBuilder("Metadata: ");
        builder.append(index == 0 ? "anchor note" : "support note");
        appendMetadataPart(builder, "category", normalizedMetadataValue(result.category));
        appendMetadataPart(builder, "role", normalizedMetadataValue(result.contentRole));
        appendMetadataPart(builder, "horizon", normalizedMetadataValue(result.timeHorizon));
        appendMetadataPart(builder, "structure", normalizedMetadataValue(result.structureType));
        appendMetadataPart(builder, "topics", normalizedTopicTags(result.topicTags));
        return builder.toString();
    }

    private static void appendMetadataPart(StringBuilder builder, String label, String value) {
        if (value.isEmpty()) {
            return;
        }
        builder.append(" | ").append(label).append("=").append(value);
    }

    private static String normalizedMetadataValue(String value) {
        return safe(value)
            .replace('_', ' ')
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String normalizedTopicTags(String topicTags) {
        String[] rawParts = safe(topicTags).split(",");
        StringBuilder builder = new StringBuilder();
        int added = 0;
        for (String rawPart : rawParts) {
            String normalized = normalizedMetadataValue(rawPart);
            if (normalized.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(normalized);
            added += 1;
            if (added >= 3) {
                break;
            }
        }
        return builder.toString();
    }

    private static int lastSentenceBreak(String text) {
        int best = -1;
        for (char marker : new char[]{'.', '!', '?', ';'}) {
            best = Math.max(best, text.lastIndexOf(marker));
        }
        return best >= 0 ? best + 1 : -1;
    }

    private static boolean isContinuationFollowUp(String question) {
        String lower = safe(question).trim().toLowerCase(Locale.US);
        return lower.contains("what next")
            || lower.contains("what now")
            || lower.contains("and then")
            || lower.contains("next step")
            || lower.contains("next steps");
    }

    private static String cleanAnswer(String answer) {
        String cleaned = safe(answer).trim();
        if (cleaned.isEmpty()) {
            return EMPTY_ANSWER_TEXT;
        }
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
        if (cleaned.length() <= 8 && !cleaned.contains(" ") && !cleaned.contains("[")) {
            return true;
        }
        if (isLikelyNonLatinNoise(cleaned)) {
            return true;
        }
        return false;
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

    private static AnswerFormatSpec answerFormatFor(QueryRouteProfile routeProfile) {
        if (routeProfile.prefersSummaryKeyPointsFormat()) {
            return new AnswerFormatSpec(
                new String[]{"Summary", "Key points", "Risks or limits"},
                "Summary, Key points, Risks or limits",
                "Keep Summary to 1-2 sentences, Key points to at most 4 short numbered lines, and Risks or limits to 1-2 sentences."
            );
        }
        return new AnswerFormatSpec(
            new String[]{"Short answer", "Steps", "Limits or safety"},
            "Short answer, Steps, Limits or safety",
            "Keep Short answer to one sentence, Steps to at most 4 numbered lines, and Limits or safety to 1-2 sentences."
        );
    }

    private static final class AnswerFormatSpec {
        final String[] labels;
        final String labelInstruction;
        final String constraintLine;

        AnswerFormatSpec(String[] labels, String labelInstruction, String constraintLine) {
            this.labels = labels;
            this.labelInstruction = labelInstruction;
            this.constraintLine = constraintLine;
        }
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

    public static boolean isLowCoverageAnswer(String answerText) {
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
            "notes do not address"
        };
        for (String signal : signals) {
            if (normalized.contains(signal)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

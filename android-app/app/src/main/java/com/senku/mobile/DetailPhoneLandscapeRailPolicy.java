package com.senku.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class DetailPhoneLandscapeRailPolicy {
    private static final Pattern GUIDE_SECTION_LABEL_PATTERN =
        Pattern.compile("^[\\-\\u2013\\u2014]+\\s*\\u00a7\\s*(\\d+)\\s*[\\u00b7:\\-]?\\s*(.*)$");

    private DetailPhoneLandscapeRailPolicy() {
    }

    static boolean shouldUseGuideSectionRail(boolean answerMode, boolean landscapePhone) {
        return !answerMode && landscapePhone;
    }

    static List<String> guideSectionRailLabels(SearchResult guide, boolean reviewDemoMode) {
        if (shouldUseGuideSectionRailDemoLabels(guide, reviewDemoMode)) {
            return guideSectionRailDemoLabels();
        }
        ArrayList<String> labels = new ArrayList<>();
        GuideBodySanitizer.ParsedGuideBody parsedBody =
            GuideBodySanitizer.parseGuideBodyForDisplay(safe(guide == null ? null : guide.body));
        for (GuideBodySanitizer.GuideBodyLine line : parsedBody.lines) {
            if (line.kind != GuideBodySanitizer.GuideBodyLine.Kind.SECTION) {
                continue;
            }
            String label = formatGuideSectionRailLabel(labels.size() + 1, line.text);
            if (!label.isEmpty() && !labels.contains(label)) {
                labels.add(label);
            }
            if (labels.size() >= 7) {
                break;
            }
        }
        if (labels.isEmpty()) {
            String fallback = firstNonEmpty(
                safe(guide == null ? null : guide.sectionHeading).trim(),
                safe(guide == null ? null : guide.title).trim(),
                safe(guide == null ? null : guide.guideId).trim(),
                "Guide overview"
            );
            labels.add(formatGuideSectionRailLabel(1, fallback));
        }
        return Collections.unmodifiableList(labels);
    }

    static int guideSectionRailCount(SearchResult guide, boolean reviewDemoMode) {
        if (shouldUseGuideSectionRailDemoLabels(guide, reviewDemoMode)) {
            return 17;
        }
        String sourceBody = safe(guide == null ? null : guide.body);
        int inferredCount = DetailGuidePresentationFormatter.inferGuideSectionCountForRail(
            guide,
            sourceBody,
            sourceBody
        );
        return Math.max(1, inferredCount);
    }

    static boolean shouldUseSourceRail(boolean answerMode, boolean landscapePhone) {
        return answerMode && landscapePhone;
    }

    static boolean shouldKeepThreadAtTop(boolean answerMode, int totalTurnCount, boolean landscapePhone) {
        return landscapePhone && answerMode && totalTurnCount > 1;
    }

    static boolean shouldPreserveThreadTopAfterComposerSetup(
        boolean answerMode,
        int totalTurnCount,
        boolean landscapePhone
    ) {
        return shouldKeepThreadAtTop(answerMode, totalTurnCount, landscapePhone);
    }

    static boolean shouldPreserveThreadTopAfterComposerFocus(
        boolean answerMode,
        int totalTurnCount,
        boolean landscapePhone
    ) {
        return shouldKeepThreadAtTop(answerMode, totalTurnCount, landscapePhone);
    }

    static long[] threadTopPreservationDelaysMs() {
        return new long[] {0L, 80L, 240L, 480L, 900L, 1400L};
    }

    static boolean shouldAutoOpenProvenanceForAnswerRail(
        boolean answerMode,
        int totalTurnCount,
        boolean landscapePhone
    ) {
        return !shouldKeepThreadAtTop(answerMode, totalTurnCount, landscapePhone);
    }

    static String buildSourceRailTitle(String baseTitle, int sourceCount) {
        String base = safe(baseTitle).trim();
        if (base.isEmpty() || "SOURCE GUIDES".equalsIgnoreCase(base)) {
            base = "SOURCES";
        } else {
            base = base.toUpperCase(Locale.US);
        }
        return base + " - " + Math.max(0, sourceCount);
    }

    static List<SearchResult> resolveVisibleSourceRailSourcesForState(
        boolean answerMode,
        boolean threadDetailRoute,
        boolean landscapePhone,
        List<SearchResult> currentSources,
        List<SessionMemory.TurnSnapshot> snapshots
    ) {
        List<SearchResult> safeSources = currentSources == null ? Collections.emptyList() : currentSources;
        if (!answerMode) {
            return new ArrayList<>(safeSources);
        }
        if (threadDetailRoute && landscapePhone) {
            return mergeThreadSourceRailSourcesForState(safeSources, snapshots);
        }
        return new ArrayList<>(safeSources);
    }

    static List<SearchResult> mergeThreadSourceRailSourcesForState(
        List<SearchResult> currentSources,
        List<SessionMemory.TurnSnapshot> snapshots
    ) {
        LinkedHashMap<String, SearchResult> deduped = new LinkedHashMap<>();
        if (snapshots != null) {
            for (SessionMemory.TurnSnapshot snapshot : snapshots) {
                addSourceRailCandidate(deduped, firstRealSource(snapshot == null ? null : snapshot.sourceResults));
            }
        }
        addSourceRailCandidate(deduped, firstRealSource(currentSources));
        return new ArrayList<>(deduped.values());
    }

    private static boolean shouldUseGuideSectionRailDemoLabels(SearchResult guide, boolean reviewDemoMode) {
        if (!reviewDemoMode) {
            return false;
        }
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        String title = safe(guide == null ? null : guide.title).trim().toLowerCase(Locale.US);
        return "GD-132".equalsIgnoreCase(guideId) || title.contains("foundry");
    }

    private static List<String> guideSectionRailDemoLabels() {
        return Arrays.asList(
            "\u00a71  Area readiness",
            "\u00a72  Required reading",
            "\u00a73  Hazard screen",
            "\u00a74  Material labeling",
            "\u00a75  No-go triggers",
            "\u00a76  Access control",
            "\u00a77  Owner handoff"
        );
    }

    private static String formatGuideSectionRailLabel(int ordinal, String rawLabel) {
        String cleaned = safe(rawLabel).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        java.util.regex.Matcher matcher = GUIDE_SECTION_LABEL_PATTERN.matcher(cleaned);
        if (matcher.matches()) {
            int sectionNumber = parsePositiveInt(matcher.group(1), ordinal);
            return "\u00a7" + sectionNumber + "  " + sentenceCaseRailLabel(matcher.group(2));
        }
        cleaned = cleaned
            .replaceFirst("^#{1,6}\\s+", "")
            .replaceFirst("(?i)^(?:Section\\s+|\\u00a7\\s*)\\d+\\s*[:\\-\\u00b7]?\\s*", "")
            .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        return "\u00a7" + Math.max(1, ordinal) + "  " + sentenceCaseRailLabel(cleaned);
    }

    private static void addSourceRailCandidate(
        LinkedHashMap<String, SearchResult> deduped,
        SearchResult source
    ) {
        if (deduped == null || source == null) {
            return;
        }
        String key = sourceRailDedupKey(source);
        if (!key.isEmpty() && !deduped.containsKey(key)) {
            deduped.put(key, source);
        }
    }

    private static String sourceRailDedupKey(SearchResult source) {
        if (source == null) {
            return "";
        }
        return (safe(source.guideId).trim() + "|"
            + safe(source.sectionHeading).trim() + "|"
            + safe(source.title).trim()).toLowerCase(Locale.US);
    }

    private static SearchResult firstRealSource(List<SearchResult> sources) {
        if (sources == null) {
            return null;
        }
        for (SearchResult source : sources) {
            if (source == null) {
                continue;
            }
            if (!safe(source.guideId).trim().isEmpty()
                || !safe(source.title).trim().isEmpty()
                || !safe(source.sectionHeading).trim().isEmpty()
                || !safe(source.body).trim().isEmpty()
                || !safe(source.snippet).trim().isEmpty()) {
                return source;
            }
        }
        return null;
    }

    private static String sentenceCaseRailLabel(String label) {
        String cleaned = safe(label).replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        if (cleaned.length() == 1) {
            return cleaned.toUpperCase(Locale.US);
        }
        return cleaned.substring(0, 1).toUpperCase(Locale.US)
            + cleaned.substring(1).toLowerCase(Locale.US);
    }

    private static int parsePositiveInt(String value, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(safe(value).trim()));
        } catch (NumberFormatException exc) {
            return Math.max(1, fallback);
        }
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String cleaned = safe(value).trim();
            if (!cleaned.isEmpty()) {
                return cleaned;
            }
        }
        return "";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

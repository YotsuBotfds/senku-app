package com.senku.mobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DetailEmergencyActionPresentationText {
    private static final String ACTIVE_WORK_ZONE = "from active work zone";
    private static final String THE_ACTIVE_WORK_ZONE = "from the active work zone";

    private DetailEmergencyActionPresentationText() {
    }

    static List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> extractEmergencyActionSpecs(
        String formattedAnswerText,
        DetailActionBlockPresentationFormatter.ActionBlockTextSanitizer sanitizer
    ) {
        ArrayList<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions = new ArrayList<>();
        for (String step : DetailEmergencyPresentationPolicy.extractImmediateActionLines(formattedAnswerText)) {
            String cleaned = sanitizeActionBlockText(step, sanitizer);
            if (cleaned.isEmpty()) {
                continue;
            }
            actions.add(splitEmergencyAction(cleaned));
        }
        return actions;
    }

    static String sanitizeActionBlockText(
        String text,
        DetailActionBlockPresentationFormatter.ActionBlockTextSanitizer sanitizer
    ) {
        String cleaned = safe(text).trim();
        if (sanitizer != null) {
            cleaned = safe(sanitizer.sanitize(cleaned)).trim();
        }
        cleaned = cleaned.replace(":::", "");
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        return DetailWarningCopySanitizer.sanitizeWarningResidualCopy(cleaned);
    }

    static int[] emergencyMinimumDistanceSpanRange(String text) {
        String value = safe(text);
        String lower = value.toLowerCase(Locale.US);
        SpanTarget target = emergencyDistanceSpanTarget(lower);
        int start = target.start;
        if (start < 0) {
            return new int[] {-1, -1};
        }
        int end = start + target.text.length();
        int activeWorkZoneEnd = lower.indexOf(ACTIVE_WORK_ZONE, start);
        if (activeWorkZoneEnd >= 0) {
            end = activeWorkZoneEnd + ACTIVE_WORK_ZONE.length();
        } else {
            activeWorkZoneEnd = lower.indexOf(THE_ACTIVE_WORK_ZONE, start);
            if (activeWorkZoneEnd >= 0) {
                end = activeWorkZoneEnd + THE_ACTIVE_WORK_ZONE.length();
            }
        }
        return new int[] {start, end};
    }

    static boolean shouldStyleEmergencyMinimumDistance(String text) {
        return emergencyDistanceSpanTarget(safe(text).toLowerCase(Locale.US)).start >= 0;
    }

    private static DetailActionBlockPresentationFormatter.EmergencyActionSpec splitEmergencyAction(String cleaned) {
        int splitIndex = firstSentenceBoundary(cleaned);
        if (splitIndex < 0) {
            return new DetailActionBlockPresentationFormatter.EmergencyActionSpec(
                normalizeEmergencyActionTitle(trimEmergencyActionTitle(cleaned)),
                ""
            );
        }
        String title = normalizeEmergencyActionTitle(trimEmergencyActionTitle(cleaned.substring(0, splitIndex)));
        String detail = normalizeEmergencyActionDetail(
            cleaned.substring(splitIndex).replaceFirst("^[.!?]+\\s*", "").trim()
        );
        if (title.isEmpty()) {
            return new DetailActionBlockPresentationFormatter.EmergencyActionSpec(cleaned, "");
        }
        return new DetailActionBlockPresentationFormatter.EmergencyActionSpec(title, detail);
    }

    private static String normalizeEmergencyActionTitle(String text) {
        String title = safe(text).trim();
        title = title.replace(
            "Clear the floor to a minimum 5 m radius",
            "Clear the floor to 5 m radius"
        );
        title = title.replace(
            "Clear the floor to a 5 m radius",
            "Clear the floor to 5 m radius"
        );
        title = title.replace(
            "Clear the floor to minimum 5 m radius",
            "Clear the floor to 5 m radius"
        );
        title = title.replace(
            "Clear the floor to 5 m radius",
            "Clear the floor to 5 m radius"
        );
        title = title.replace(
            "Move everyone to minimum 5 m from active work zone",
            "Move to minimum 5 m from active work zone"
        );
        return title;
    }

    private static String trimEmergencyActionTitle(String text) {
        return safe(text).trim().replaceFirst("[.!?]+$", "").trim();
    }

    private static String normalizeEmergencyActionDetail(String text) {
        String detail = safe(text).trim();
        detail = detail.replace(
            "Doors and roll-up openings must be unobstructed.",
            "Door and roll-up open and unobstructed."
        );
        detail = detail.replace(
            "GD-132 \u00a71 is current owner.",
            "GD-132 lists current owner."
        );
        return detail;
    }

    private static int firstSentenceBoundary(String text) {
        String value = safe(text).trim();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c == '.' || c == '!' || c == '?') && i + 1 < value.length()) {
                char next = value.charAt(i + 1);
                if (Character.isWhitespace(next)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static SpanTarget emergencyDistanceSpanTarget(String lower) {
        int start = lower.indexOf("minimum 5 m");
        if (start >= 0) {
            return new SpanTarget(start, "minimum 5 m");
        }
        start = lower.indexOf("minimum 5 meters");
        if (start >= 0) {
            return new SpanTarget(start, "minimum 5 meters");
        }
        start = lower.indexOf("minimum 5 metres");
        if (start >= 0) {
            return new SpanTarget(start, "minimum 5 metres");
        }
        start = lower.indexOf("minimum five meters");
        if (start >= 0) {
            return new SpanTarget(start, "minimum five meters");
        }
        start = lower.indexOf("minimum five metres");
        if (start >= 0) {
            return new SpanTarget(start, "minimum five metres");
        }
        return new SpanTarget(-1, "");
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    private static final class SpanTarget {
        final int start;
        final String text;

        SpanTarget(int start, String text) {
            this.start = start;
            this.text = text;
        }
    }
}

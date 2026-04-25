package com.senku.mobile;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DetailMetaPresentationFormatter {
    static final class State {
        final boolean answerMode;
        final boolean deterministicRoute;
        final boolean abstainRoute;
        final boolean lowCoverageRoute;
        final boolean pendingHostEnabled;
        final boolean currentAnswerUsesOnDeviceFallback;
        final boolean wide;
        final int currentSourcesCount;
        final int recentTurnsCount;
        final int currentPackVersion;
        final String currentSubtitle;
        final String evidenceStrengthLabel;
        final String currentPackGeneratedAt;
        final String currentPackHashShort;

        State(
            boolean answerMode,
            boolean deterministicRoute,
            boolean abstainRoute,
            boolean lowCoverageRoute,
            boolean pendingHostEnabled,
            boolean currentAnswerUsesOnDeviceFallback,
            boolean wide,
            int currentSourcesCount,
            int recentTurnsCount,
            int currentPackVersion,
            String currentSubtitle,
            String evidenceStrengthLabel,
            String currentPackGeneratedAt,
            String currentPackHashShort
        ) {
            this.answerMode = answerMode;
            this.deterministicRoute = deterministicRoute;
            this.abstainRoute = abstainRoute;
            this.lowCoverageRoute = lowCoverageRoute;
            this.pendingHostEnabled = pendingHostEnabled;
            this.currentAnswerUsesOnDeviceFallback = currentAnswerUsesOnDeviceFallback;
            this.wide = wide;
            this.currentSourcesCount = currentSourcesCount;
            this.recentTurnsCount = recentTurnsCount;
            this.currentPackVersion = currentPackVersion;
            this.currentSubtitle = safe(currentSubtitle);
            this.evidenceStrengthLabel = safe(evidenceStrengthLabel);
            this.currentPackGeneratedAt = safe(currentPackGeneratedAt);
            this.currentPackHashShort = safe(currentPackHashShort);
        }
    }

    private static final String SERIAL_META_SEPARATOR = " | ";

    private final Context context;

    DetailMetaPresentationFormatter(Context context) {
        this.context = context;
    }

    String buildCompactHeaderMeta(State state) {
        ArrayList<String> parts = new ArrayList<>();
        parts.add(context.getString(R.string.detail_loop4_meta_route_token, buildSerialRouteValue(state)));
        String backendValue = buildSerialBackendValue(state);
        if (!backendValue.isEmpty()) {
            parts.add(context.getString(R.string.detail_loop4_meta_backend_token, backendValue));
        }
        parts.add(context.getString(R.string.detail_loop4_meta_evidence_token, buildSerialEvidenceValue(state)));
        parts.add(context.getString(R.string.detail_loop4_meta_sources_token, state.currentSourcesCount));
        parts.add(context.getString(R.string.detail_loop4_meta_turns_token, Math.max(1, state.recentTurnsCount)));
        return buildHeaderMetaText(parts, state.wide, buildPackFreshnessMeta(state));
    }

    String buildGuideHeaderMeta(State state) {
        return buildPackFreshnessMeta(state);
    }

    String buildBackendMetaLabel(State state) {
        if (!state.answerMode || state.deterministicRoute || state.abstainRoute) {
            return "";
        }
        if (state.currentAnswerUsesOnDeviceFallback) {
            return context.getString(R.string.detail_backend_on_device_fallback);
        }
        if (state.pendingHostEnabled) {
            return context.getString(R.string.detail_backend_host);
        }
        String subtitle = safe(state.currentSubtitle).trim();
        if (subtitle.isEmpty()) {
            return "";
        }
        String lowerSubtitle = subtitle.toLowerCase(Locale.US);
        if (lowerSubtitle.startsWith("host answer |")) {
            return context.getString(R.string.detail_backend_host);
        }
        if (lowerSubtitle.startsWith("offline answer |")) {
            return context.getString(R.string.detail_backend_on_device);
        }
        if (lowerSubtitle.startsWith("low coverage |")) {
            return lowerSubtitle.contains(" @ ")
                ? context.getString(R.string.detail_backend_host)
                : context.getString(R.string.detail_backend_on_device);
        }
        return "";
    }

    String buildTrustRouteBackendSummary(State state, String routeLabel) {
        if (!state.answerMode) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        parts.add(safe(routeLabel));
        String backendLabel = buildBackendMetaLabel(state);
        if (!backendLabel.isEmpty()) {
            parts.add(backendLabel);
        }
        return TextUtils.join(SERIAL_META_SEPARATOR, parts);
    }

    String buildPackFreshnessMeta(State state) {
        String stamp = compactPackGeneratedAt(state.currentPackGeneratedAt, state.wide);
        String hash = safe(state.currentPackHashShort).trim();
        if (stamp.isEmpty() && hash.isEmpty() && state.currentPackVersion <= 0) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        if (!stamp.isEmpty()) {
            parts.add(context.getString(R.string.detail_loop4_meta_revision_token, stamp));
        } else {
            parts.add(context.getString(R.string.detail_revision_prefix));
        }
        if (state.currentPackVersion > 0) {
            parts.add(context.getString(R.string.detail_loop4_meta_pack_token, state.currentPackVersion));
        }
        if (!hash.isEmpty()) {
            parts.add(context.getString(R.string.detail_loop4_meta_hash_token, hash));
        }
        return joinSerialTokens(parts);
    }

    String buildSerialRouteValue(State state) {
        if (state.abstainRoute) {
            return "ABSTAIN";
        }
        if (state.lowCoverageRoute) {
            return context.getString(R.string.detail_loop4_route_serial_low_coverage);
        }
        if (state.deterministicRoute) {
            return context.getString(R.string.detail_loop4_route_serial_deterministic);
        }
        return context.getString(R.string.detail_loop4_route_serial_generated);
    }

    String buildSerialBackendValue(State state) {
        if (!state.answerMode || state.deterministicRoute || state.abstainRoute) {
            return "";
        }
        if (state.currentAnswerUsesOnDeviceFallback) {
            return context.getString(R.string.detail_loop4_backend_serial_fallback);
        }
        if (state.pendingHostEnabled) {
            return context.getString(R.string.detail_loop4_backend_serial_host);
        }
        String subtitle = safe(state.currentSubtitle).trim().toLowerCase(Locale.US);
        if (subtitle.startsWith("host answer |") || subtitle.contains(" @ ")) {
            return context.getString(R.string.detail_loop4_backend_serial_host);
        }
        return context.getString(R.string.detail_loop4_backend_serial_device);
    }

    String buildSerialEvidenceValue(State state) {
        if (state.currentSourcesCount <= 0) {
            return context.getString(R.string.detail_loop4_evidence_serial_none);
        }
        String label = safe(state.evidenceStrengthLabel);
        if (context.getString(R.string.detail_evidence_reviewed).equals(label)) {
            return context.getString(R.string.detail_loop4_evidence_serial_reviewed);
        }
        if (context.getString(R.string.detail_evidence_strong).equals(label)) {
            return context.getString(R.string.detail_loop4_evidence_serial_strong);
        }
        if (context.getString(R.string.detail_evidence_moderate).equals(label)) {
            return context.getString(R.string.detail_loop4_evidence_serial_moderate);
        }
        return context.getString(R.string.detail_loop4_evidence_serial_limited);
    }

    ArrayList<String> splitSerialTokens(String value) {
        ArrayList<String> tokens = new ArrayList<>();
        for (String token : safe(value).split("\\Q" + SERIAL_META_SEPARATOR + "\\E")) {
            String cleaned = safe(token).trim();
            if (!cleaned.isEmpty()) {
                tokens.add(cleaned);
            }
        }
        return tokens;
    }

    static String joinSerialTokens(List<String> tokens) {
        ArrayList<String> cleaned = new ArrayList<>();
        if (tokens != null) {
            for (String token : tokens) {
                String value = safe(token).trim();
                if (!value.isEmpty()) {
                    cleaned.add(value);
                }
            }
        }
        return TextUtils.join(SERIAL_META_SEPARATOR, cleaned);
    }

    private static String buildHeaderMetaText(List<String> primaryParts, boolean wide, String packFreshness) {
        ArrayList<String> firstLineParts = new ArrayList<>(primaryParts);
        if (packFreshness.isEmpty()) {
            return joinSerialTokens(firstLineParts);
        }
        if (wide) {
            firstLineParts.add(packFreshness);
            return joinSerialTokens(firstLineParts);
        }
        return joinSerialTokens(firstLineParts) + "\n" + packFreshness;
    }

    private static String compactPackGeneratedAt(String currentPackGeneratedAt, boolean wide) {
        String value = safe(currentPackGeneratedAt).trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.length() >= 16) {
            String date = value.substring(5, 10);
            String time = value.substring(11, 16);
            return wide ? date + " " + time : date;
        }
        return value;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

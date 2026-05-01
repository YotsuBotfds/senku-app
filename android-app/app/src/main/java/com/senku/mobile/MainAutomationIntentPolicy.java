package com.senku.mobile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

final class MainAutomationIntentPolicy {
    private static final char INVALID_INPUT_CHARACTER = '\uFFFD';

    enum Route {
        SEARCH,
        ASK
    }

    enum ApplyEffect {
        NONE,
        OPEN_EMPTY_ASK_LANE,
        SET_QUERY
    }

    enum AutomationEffect {
        NONE,
        OPEN_EMPTY_ASK_LANE,
        PREPARE_QUERY_WAITING_FOR_REPOSITORY,
        RUN_QUERY
    }

    static final class IntentState {
        final String decodedAutoQuery;
        final boolean autoAsk;
        final String decodedAutoFollowUpQuery;

        IntentState(String decodedAutoQuery, boolean autoAsk, String decodedAutoFollowUpQuery) {
            this.decodedAutoQuery = decodedAutoQuery;
            this.autoAsk = autoAsk;
            this.decodedAutoFollowUpQuery = decodedAutoFollowUpQuery;
        }

        static IntentState fromEncoded(
            String encodedAutoQuery,
            boolean autoAsk,
            String encodedAutoFollowUpQuery
        ) {
            return new IntentState(
                decodeQuery(encodedAutoQuery),
                autoAsk,
                decodeQuery(encodedAutoFollowUpQuery)
            );
        }
    }

    static final class ApplyDecision {
        final ApplyEffect effect;
        final String query;
        final Route route;

        ApplyDecision(ApplyEffect effect, String query, Route route) {
            this.effect = effect == null ? ApplyEffect.NONE : effect;
            this.query = query == null ? "" : query;
            this.route = route == null ? Route.SEARCH : route;
        }
    }

    static final class AutomationDecision {
        final AutomationEffect effect;
        final String query;
        final String followUpQuery;
        final Route route;
        final boolean markHandled;
        final boolean suppressSearchFocus;

        AutomationDecision(
            AutomationEffect effect,
            String query,
            String followUpQuery,
            Route route,
            boolean markHandled,
            boolean suppressSearchFocus
        ) {
            this.effect = effect == null ? AutomationEffect.NONE : effect;
            this.query = query == null ? "" : query;
            this.followUpQuery = followUpQuery;
            this.route = route == null ? Route.SEARCH : route;
            this.markHandled = markHandled;
            this.suppressSearchFocus = suppressSearchFocus;
        }
    }

    private MainAutomationIntentPolicy() {
    }

    static ApplyDecision resolveApply(IntentState intentState) {
        if (intentState == null) {
            return new ApplyDecision(ApplyEffect.NONE, "", Route.SEARCH);
        }
        if (shouldOpenEmptyAutoAskLane(intentState.decodedAutoQuery, intentState.autoAsk)) {
            return new ApplyDecision(ApplyEffect.OPEN_EMPTY_ASK_LANE, "", Route.ASK);
        }
        String trimmedQuery = trimmed(intentState.decodedAutoQuery);
        if (trimmedQuery.isEmpty()) {
            return new ApplyDecision(ApplyEffect.NONE, "", Route.SEARCH);
        }
        return new ApplyDecision(
            ApplyEffect.SET_QUERY,
            trimmedQuery,
            intentState.autoAsk ? Route.ASK : Route.SEARCH
        );
    }

    static AutomationDecision resolveAutomation(
        IntentState intentState,
        boolean autoIntentHandled,
        boolean repositoryReady
    ) {
        if (autoIntentHandled || intentState == null) {
            return new AutomationDecision(
                AutomationEffect.NONE,
                "",
                null,
                Route.SEARCH,
                false,
                false
            );
        }
        if (shouldOpenEmptyAutoAskLane(intentState.decodedAutoQuery, intentState.autoAsk)) {
            return new AutomationDecision(
                AutomationEffect.OPEN_EMPTY_ASK_LANE,
                "",
                null,
                Route.ASK,
                true,
                true
            );
        }
        if (intentState.decodedAutoQuery == null) {
            return new AutomationDecision(
                AutomationEffect.NONE,
                "",
                null,
                Route.SEARCH,
                false,
                false
            );
        }

        String trimmedQuery = trimmed(intentState.decodedAutoQuery);
        if (trimmedQuery.isEmpty()) {
            return new AutomationDecision(
                AutomationEffect.NONE,
                "",
                null,
                Route.SEARCH,
                false,
                false
            );
        }

        Route route = intentState.autoAsk ? Route.ASK : Route.SEARCH;
        if (!repositoryReady) {
            return new AutomationDecision(
                AutomationEffect.PREPARE_QUERY_WAITING_FOR_REPOSITORY,
                trimmedQuery,
                intentState.decodedAutoFollowUpQuery,
                route,
                false,
                false
            );
        }
        return new AutomationDecision(
            AutomationEffect.RUN_QUERY,
            trimmedQuery,
            intentState.decodedAutoFollowUpQuery,
            route,
            true,
            true
        );
    }

    static boolean hasAutoQuery(IntentState intentState) {
        return intentState != null && !trimmed(intentState.decodedAutoQuery).isEmpty();
    }

    static boolean shouldOpenEmptyAutoAskLane(String decodedAutoQuery, boolean autoAsk) {
        return autoAsk && trimmed(decodedAutoQuery).isEmpty();
    }

    static String decodeQuery(String query) {
        if (query == null) {
            return null;
        }
        StringBuilder decoded = new StringBuilder(query.length());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int index = 0; index < query.length(); index++) {
            char current = query.charAt(index);
            if (current != '%') {
                decoded.append(current);
                continue;
            }

            bytes.reset();
            boolean invalidEscape = false;
            while (index + 2 < query.length() && query.charAt(index) == '%') {
                int high = hexValue(query.charAt(index + 1));
                int low = hexValue(query.charAt(index + 2));
                if (high < 0 || low < 0) {
                    appendDecodedBytes(decoded, bytes);
                    decoded.append(INVALID_INPUT_CHARACTER);
                    invalidEscape = true;
                    index += 3;
                    break;
                }
                bytes.write((high << 4) + low);
                index += 3;
            }
            if (invalidEscape) {
                index--;
                continue;
            }
            if (index + 2 >= query.length() && index < query.length() && query.charAt(index) == '%') {
                appendDecodedBytes(decoded, bytes);
                decoded.append(INVALID_INPUT_CHARACTER);
                return decoded.toString();
            }
            if (bytes.size() > 0) {
                appendDecodedBytes(decoded, bytes);
                index--;
            } else {
                while (index < query.length() && query.charAt(index) != '%') {
                    decoded.append(query.charAt(index));
                    index++;
                }
                index--;
            }
        }
        return decoded.toString();
    }

    private static void appendDecodedBytes(StringBuilder decoded, ByteArrayOutputStream bytes) {
        if (bytes.size() > 0) {
            decoded.append(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            bytes.reset();
        }
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private static int hexValue(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'a' && value <= 'f') {
            return value - 'a' + 10;
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return -1;
    }
}

package com.senku.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

final class HostInferenceResponsePolicy {
    private static final String DEFAULT_BACKEND = "host";

    private HostInferenceResponsePolicy() {
    }

    static HostInferenceClient.Result parseResponseBody(String responseBody) throws Exception {
        JSONObject responseJson = new JSONObject(responseBody);
        JSONArray choices = responseJson.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new IllegalStateException("Host inference returned no choices");
        }

        JSONObject firstChoice = choices.optJSONObject(0);
        String finishReason = firstChoice == null ? "" : firstChoice.optString("finish_reason", "");
        if ("length".equalsIgnoreCase(finishReason.trim())) {
            throw new IllegalStateException("Host inference returned an incomplete answer");
        }
        JSONObject message = firstChoice == null ? null : firstChoice.optJSONObject("message");
        Object content = message == null ? null : message.opt("content");
        String flattened = flattenContent(content).trim();
        if (flattened.isEmpty()) {
            throw new IllegalStateException("Host inference returned an empty answer");
        }

        String backend = normalizeBackend(responseJson.optString("senku_backend", DEFAULT_BACKEND));
        double elapsedSeconds = normalizeElapsedSeconds(responseJson.optDouble("senku_elapsed_seconds", 0.0d));
        return new HostInferenceClient.Result(flattened, backend, elapsedSeconds);
    }

    static String flattenContent(Object content) {
        if (content instanceof String) {
            return (String) content;
        }
        if (content instanceof JSONArray) {
            return flattenContentArray((JSONArray) content);
        }
        return "";
    }

    private static String flattenContentArray(JSONArray parts) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < parts.length(); index++) {
            Object part = parts.opt(index);
            String text = flattenContentPart(part);
            if (!text.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(text);
            }
        }
        return builder.toString();
    }

    private static String flattenContentPart(Object part) {
        if (part instanceof JSONObject) {
            return ((JSONObject) part).optString("text", "");
        }
        if (part instanceof String) {
            return (String) part;
        }
        return "";
    }

    private static String normalizeBackend(String rawBackend) {
        String raw = rawBackend == null ? "" : rawBackend.trim();
        if (raw.isEmpty()) {
            return DEFAULT_BACKEND;
        }
        StringBuilder builder = new StringBuilder(raw.length());
        boolean previousWhitespace = false;
        for (int index = 0; index < raw.length(); index++) {
            char ch = raw.charAt(index);
            if (Character.isISOControl(ch) || Character.isWhitespace(ch)) {
                if (!previousWhitespace) {
                    builder.append(' ');
                    previousWhitespace = true;
                }
            } else {
                builder.append(ch);
                previousWhitespace = false;
            }
        }
        String normalized = builder.toString().trim();
        return normalized.isEmpty() ? DEFAULT_BACKEND : normalized;
    }

    private static double normalizeElapsedSeconds(double elapsedSeconds) {
        if (Double.isNaN(elapsedSeconds) || Double.isInfinite(elapsedSeconds) || elapsedSeconds < 0.0d) {
            return 0.0d;
        }
        return elapsedSeconds;
    }
}

package com.senku.mobile;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class HostInferenceClient {
    private static final String TAG = "SenkuMobile";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15 * 60 * 1000;

    private HostInferenceClient() {
    }

    public static Result generate(HostInferenceConfig.Settings settings, String systemPrompt, String prompt, Integer maxTokens) throws Exception {
        HostInferencePolicy.Decision policyDecision = HostInferencePolicy.evaluate(settings.baseUrl);
        if (!policyDecision.allowed) {
            throw new IllegalStateException(
                "Host inference endpoint rejected by policy (" + policyDecision.reason + "): " + settings.baseUrl
            );
        }

        URI uri = completionUri(settings);

        byte[] bodyBytes = requestPayload(settings, systemPrompt, prompt, maxTokens)
            .toString()
            .getBytes(StandardCharsets.UTF_8);
        Log.d(
            TAG,
            "host.request start url=" + uri +
                " model=" + settings.modelId +
                " systemChars=" + (systemPrompt == null ? 0 : systemPrompt.length()) +
                " promptChars=" + prompt.length() +
                " requestBytes=" + bodyBytes.length
        );

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Connection", "close");
        connection.setFixedLengthStreamingMode(bodyBytes.length);

        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bodyBytes);
                output.flush();
                Log.d(TAG, "host.request bodyWritten bytes=" + bodyBytes.length);
            }

            int statusCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, statusCode);
            Log.d(
                TAG,
                "host.response status=" + statusCode +
                    " bodyChars=" + responseBody.length()
            );

            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException(
                    "Host inference failed (" + statusCode + "): " + responseBody
                );
            }

            Result result = parseResponseBody(responseBody);
            Log.d(
                TAG,
                "host.response parsed chars=" + result.answer.length() +
                    " backend=" + result.backend +
                    " elapsedSeconds=" + result.elapsedSeconds
            );
            return result;
        } finally {
            connection.disconnect();
        }
    }

    static URI completionUri(HostInferenceConfig.Settings settings) {
        return HostInferenceRequestPolicy.completionUri(settings);
    }

    static JSONObject requestPayload(
        HostInferenceConfig.Settings settings,
        String systemPrompt,
        String prompt,
        Integer maxTokens
    ) throws Exception {
        return HostInferenceRequestPolicy.buildPayload(settings, systemPrompt, prompt, maxTokens);
    }

    static Result parseResponseBody(String responseBody) throws Exception {
        return HostInferenceResponsePolicy.parseResponseBody(responseBody);
    }

    static String flattenContent(Object content) {
        return HostInferenceResponsePolicy.flattenContent(content);
    }

    private static String readResponseBody(HttpURLConnection connection, int statusCode) throws Exception {
        InputStream stream = statusCode >= 200 && statusCode < 300
            ? connection.getInputStream()
            : connection.getErrorStream();
        return new String(readFullyBytes(stream), StandardCharsets.UTF_8);
    }

    private static byte[] readFullyBytes(InputStream stream) throws Exception {
        if (stream == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    public static final class Result {
        public final String answer;
        public final String backend;
        public final double elapsedSeconds;

        Result(String answer, String backend, double elapsedSeconds) {
            this.answer = answer == null ? "" : answer;
            this.backend = backend == null ? "host" : backend;
            this.elapsedSeconds = elapsedSeconds;
        }
    }
}

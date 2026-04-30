package com.senku.mobile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class HostInferenceClient {
    private static final String TAG = "SenkuMobile";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15 * 60 * 1000;
    private static final int DEFAULT_MAX_TOKENS = 2048;
    private static final double TEMPERATURE = 0.11;

    private HostInferenceClient() {
    }

    public static Result generate(HostInferenceConfig.Settings settings, String systemPrompt, String prompt, Integer maxTokens) throws Exception {
        HostInferencePolicy.Decision policyDecision = HostInferencePolicy.evaluate(settings.baseUrl);
        if (!policyDecision.allowed) {
            throw new IllegalStateException(
                "Host inference endpoint rejected by policy (" + policyDecision.reason + "): " + settings.baseUrl
            );
        }

        URI uri = URI.create(settings.baseUrl + "/chat/completions");
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme().trim().toLowerCase(Locale.US);
        if (!"http".equals(scheme)) {
            throw new IllegalStateException("Host inference currently supports only http endpoints: " + settings.baseUrl);
        }
        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalStateException("Host inference URL has no host: " + settings.baseUrl);
        }
        int port = uri.getPort() > 0 ? uri.getPort() : 80;
        String path = uri.getRawPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        JSONObject payload = new JSONObject();
        payload.put("model", settings.modelId);
        payload.put("temperature", TEMPERATURE);
        payload.put("stream", false);
        payload.put("max_tokens", maxTokens == null ? DEFAULT_MAX_TOKENS : maxTokens);

        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.put(systemMessage);
        }
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);
        payload.put("messages", messages);

        byte[] bodyBytes = payload.toString().getBytes(StandardCharsets.UTF_8);
        byte[] requestBytes = (
            "POST " + path + " HTTP/1.1\r\n" +
                "Host: " + host + ":" + port + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Accept: application/json\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        Log.d(
            TAG,
            "host.request start url=" + uri +
                " model=" + settings.modelId +
                " systemChars=" + (systemPrompt == null ? 0 : systemPrompt.length()) +
                " promptChars=" + prompt.length() +
                " requestBytes=" + bodyBytes.length
        );

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);

            try (OutputStream output = socket.getOutputStream();
                 InputStream input = socket.getInputStream()) {
                output.write(requestBytes);
                output.write(bodyBytes);
                output.flush();
                Log.d(TAG, "host.request bodyWritten bytes=" + bodyBytes.length);

                RawHttpResponse response = readResponse(input);
                Log.d(
                    TAG,
                    "host.response status=" + response.statusCode +
                        " bodyChars=" + response.body.length()
                );

                if (response.statusCode < 200 || response.statusCode >= 300) {
                    throw new IllegalStateException(
                        "Host inference failed (" + response.statusCode + "): " + response.body
                    );
                }

                Result result = parseResponseBody(response.body);
                Log.d(
                    TAG,
                    "host.response parsed chars=" + result.answer.length() +
                        " backend=" + result.backend +
                        " elapsedSeconds=" + result.elapsedSeconds
                );
                return result;
            }
        }
    }

    static Result parseResponseBody(String responseBody) throws Exception {
        JSONObject responseJson = new JSONObject(responseBody);
        JSONArray choices = responseJson.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new IllegalStateException("Host inference returned no choices");
        }
        JSONObject firstChoice = choices.optJSONObject(0);
        JSONObject message = firstChoice == null ? null : firstChoice.optJSONObject("message");
        Object content = message == null ? null : message.opt("content");
        String flattened = flattenContent(content).trim();
        if (flattened.isEmpty()) {
            throw new IllegalStateException("Host inference returned an empty answer");
        }
        String backend = responseJson.optString("senku_backend", "host").trim();
        if (backend.isEmpty()) {
            backend = "host";
        }
        double elapsedSeconds = responseJson.optDouble("senku_elapsed_seconds", 0.0d);
        return new Result(flattened, backend, elapsedSeconds);
    }

    static String flattenContent(Object content) {
        if (content instanceof String) {
            return (String) content;
        }
        if (content instanceof JSONArray) {
            StringBuilder builder = new StringBuilder();
            JSONArray parts = (JSONArray) content;
            for (int index = 0; index < parts.length(); index++) {
                Object part = parts.opt(index);
                if (part instanceof JSONObject) {
                    String text = ((JSONObject) part).optString("text", "");
                    if (!text.isEmpty()) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(text);
                    }
                } else if (part instanceof String) {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(part);
                }
            }
            return builder.toString();
        }
        return "";
    }

    private static RawHttpResponse readResponse(InputStream input) throws Exception {
        String statusLine = readAsciiLine(input);
        if (statusLine == null || statusLine.isEmpty()) {
            throw new java.io.EOFException("Host inference returned no status line");
        }
        String[] statusParts = statusLine.split(" ", 3);
        if (statusParts.length < 2) {
            throw new IllegalStateException("Malformed HTTP status line: " + statusLine);
        }
        int statusCode = Integer.parseInt(statusParts[1]);
        Map<String, String> headers = new LinkedHashMap<>();
        while (true) {
            String headerLine = readAsciiLine(input);
            if (headerLine == null || headerLine.isEmpty()) {
                break;
            }
            int colon = headerLine.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String name = headerLine.substring(0, colon).trim().toLowerCase(Locale.US);
            String value = headerLine.substring(colon + 1).trim();
            headers.put(name, value);
        }

        int contentLength = -1;
        String contentLengthValue = headers.get("content-length");
        if (contentLengthValue != null && !contentLengthValue.isEmpty()) {
            contentLength = Integer.parseInt(contentLengthValue);
        }

        byte[] bodyBytes = contentLength >= 0
            ? readBestEffortBytes(input, contentLength)
            : readFullyBytes(input);
        if (contentLength >= 0 && contentLength != bodyBytes.length) {
            Log.w(
                TAG,
                "host.response lengthMismatch header=" + contentLength + " actual=" + bodyBytes.length
            );
        }
        return new RawHttpResponse(statusCode, new String(bodyBytes, StandardCharsets.UTF_8));
    }

    private static String readAsciiLine(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int next;
        while ((next = input.read()) != -1) {
            if (next == '\n') {
                break;
            }
            if (next != '\r') {
                output.write(next);
            }
        }
        if (next == -1 && output.size() == 0) {
            return null;
        }
        return output.toString(StandardCharsets.UTF_8);
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

    private static byte[] readBestEffortBytes(InputStream stream, int expectedBytes) throws Exception {
        if (stream == null || expectedBytes <= 0) {
            return new byte[0];
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(expectedBytes);
        byte[] buffer = new byte[8192];
        int remaining = expectedBytes;
        while (remaining > 0) {
            int read = stream.read(buffer, 0, Math.min(buffer.length, remaining));
            if (read == -1) {
                break;
            }
            output.write(buffer, 0, read);
            remaining -= read;
        }
        return output.toByteArray();
    }

    private static final class RawHttpResponse {
        final int statusCode;
        final String body;

        RawHttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body == null ? "" : body;
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

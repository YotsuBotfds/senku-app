package com.senku.mobile;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class HostInferenceClientTest {
    @Test
    public void parseResponseBodyReturnsAnswerBackendAndElapsedSeconds() throws Exception {
        HostInferenceClient.Result result = HostInferenceClient.parseResponseBody(
            "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": \"Short answer: Four\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"senku_backend\": \"gpu\",\n" +
                "  \"senku_elapsed_seconds\": 3.344\n" +
                "}"
        );

        assertEquals("Short answer: Four", result.answer);
        assertEquals("gpu", result.backend);
        assertEquals(3.344d, result.elapsedSeconds, 0.0001d);
    }

    @Test
    public void parseResponseBodyDefaultsBackendWhenMissing() throws Exception {
        HostInferenceClient.Result result = HostInferenceClient.parseResponseBody(
            "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": [\n" +
                "          {\"text\": \"Line one\"},\n" +
                "          {\"text\": \"Line two\"}\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        );

        assertEquals("Line one\nLine two", result.answer);
        assertEquals("host", result.backend);
        assertEquals(0.0d, result.elapsedSeconds, 0.0001d);
    }

    @Test
    public void parseResponseBodyDefaultsBackendWhenBlank() throws Exception {
        HostInferenceClient.Result result = HostInferenceClient.parseResponseBody(
            "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": \"Ready\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"senku_backend\": \"   \"\n" +
                "}"
        );

        assertEquals("Ready", result.answer);
        assertEquals("host", result.backend);
        assertEquals(0.0d, result.elapsedSeconds, 0.0001d);
    }

    @Test
    public void parseResponseBodyFlattensMixedContentAndSkipsUnsupportedParts() throws Exception {
        HostInferenceClient.Result result = HostInferenceClient.parseResponseBody(
            "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": [\n" +
                "          {\"type\": \"text\", \"text\": \"Alpha\"},\n" +
                "          17,\n" +
                "          {\"type\": \"image_url\", \"image_url\": \"ignored\"},\n" +
                "          \"Beta\",\n" +
                "          {\"text\": \"Gamma\"}\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"senku_backend\": \"mixed\"\n" +
                "}"
        );

        assertEquals("Alpha\nBeta\nGamma", result.answer);
        assertEquals("mixed", result.backend);
    }

    @Test
    public void parseResponseBodyRejectsMalformedObjectContentAsEmptyAnswer() throws Exception {
        try {
            HostInferenceClient.parseResponseBody(
                "{\n" +
                    "  \"choices\": [\n" +
                    "    {\n" +
                    "      \"message\": {\n" +
                    "        \"content\": {\"unexpected\": \"shape\"}\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
            );
            fail("Expected malformed object content to be rejected");
        } catch (IllegalStateException exception) {
            assertEquals("Host inference returned an empty answer", exception.getMessage());
        }
    }

    @Test
    public void generateRejectsNonLocalCleartextBeforeConnecting() throws Exception {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "http://host.local:1235/v1",
            "gemma-4-e2b-it-litert"
        );

        try {
            HostInferenceClient.generate(settings, "", "ping", null);
            fail("Expected non-local cleartext host to be rejected");
        } catch (IllegalStateException exception) {
            assertTrue(exception.getMessage().contains("NON_LOCAL_CLEARTEXT_REJECTED"));
        }
    }

    @Test
    public void clientBuildsHttpsCompletionUriWhenPolicyAllowsHttps() {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "https://host.local/openai/v1",
            "gemma-4-e2b-it-litert"
        );

        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate(settings.baseUrl);

        assertTrue(decision.allowed);
        assertEquals(HostInferencePolicy.Reason.HTTPS_ALLOWED, decision.reason);
        assertEquals(
            "https://host.local/openai/v1/chat/completions",
            HostInferenceClient.completionUri(settings).toString()
        );
    }

    @Test
    public void requestPayloadUsesDefaultMaxTokensAndBuildsMessages() throws Exception {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "http://10.0.2.2:1235/v1",
            "gemma-4-e2b-it-litert"
        );

        JSONObject payload = HostInferenceClient.requestPayload(settings, "System note", "ping", null);
        JSONArray messages = payload.getJSONArray("messages");

        assertEquals("gemma-4-e2b-it-litert", payload.getString("model"));
        assertEquals(0.11d, payload.getDouble("temperature"), 0.0001d);
        assertEquals(false, payload.getBoolean("stream"));
        assertEquals(2048, payload.getInt("max_tokens"));
        assertEquals(2, messages.length());
        assertEquals("system", messages.getJSONObject(0).getString("role"));
        assertEquals("System note", messages.getJSONObject(0).getString("content"));
        assertEquals("user", messages.getJSONObject(1).getString("role"));
        assertEquals("ping", messages.getJSONObject(1).getString("content"));
    }

    @Test
    public void requestPayloadSkipsBlankSystemPromptAndUsesExplicitMaxTokens() throws Exception {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "http://10.0.2.2:1235/v1",
            "gemma-4-e2b-it-litert"
        );

        JSONObject payload = HostInferenceClient.requestPayload(settings, "  ", "ping", 64);
        JSONArray messages = payload.getJSONArray("messages");

        assertEquals(64, payload.getInt("max_tokens"));
        assertEquals(1, messages.length());
        assertEquals("user", messages.getJSONObject(0).getString("role"));
        assertEquals("ping", messages.getJSONObject(0).getString("content"));
    }
}

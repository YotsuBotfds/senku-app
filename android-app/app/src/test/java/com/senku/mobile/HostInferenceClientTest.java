package com.senku.mobile;

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
}

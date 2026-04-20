package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}

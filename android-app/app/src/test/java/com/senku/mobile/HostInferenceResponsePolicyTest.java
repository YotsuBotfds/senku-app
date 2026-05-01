package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.junit.Test;

public final class HostInferenceResponsePolicyTest {
    @Test
    public void flattenContentReturnsStringContentUnchanged() {
        assertEquals(
            "Short answer: Build the fire on mineral soil.",
            HostInferenceResponsePolicy.flattenContent("Short answer: Build the fire on mineral soil.")
        );
    }

    @Test
    public void flattenContentArrayJoinsStringAndTextPartsSkippingEmptyUnsupportedParts() throws Exception {
        JSONArray content = new JSONArray(
            "[\n" +
                "  {\"type\": \"text\", \"text\": \"Alpha\"},\n" +
                "  \"Beta\",\n" +
                "  {\"type\": \"image_url\", \"image_url\": \"ignored\"},\n" +
                "  17,\n" +
                "  {\"text\": \"Gamma\"},\n" +
                "  {\"text\": \"\"}\n" +
                "]"
        );

        assertEquals("Alpha\nBeta\nGamma", HostInferenceResponsePolicy.flattenContent(content));
    }

    @Test
    public void parseResponseBodyFlattensArrayContentAndTrimsAnswer() throws Exception {
        HostInferenceClient.Result result = HostInferenceResponsePolicy.parseResponseBody(
            "{\n" +
                "  \"choices\": [\n" +
                "    {\"message\": {\"content\": [\n" +
                "      {\"text\": \"  First line  \"},\n" +
                "      {\"text\": \"Second line\"}\n" +
                "    ]}}\n" +
                "  ],\n" +
                "  \"senku_backend\": \"  host-gpu  \",\n" +
                "  \"senku_elapsed_seconds\": 1.25\n" +
                "}"
        );

        assertEquals("First line  \nSecond line", result.answer);
        assertEquals("host-gpu", result.backend);
        assertEquals(1.25d, result.elapsedSeconds, 0.0001d);
    }

    @Test
    public void parseResponseBodyRejectsMissingChoices() throws Exception {
        assertIllegalStateMessage(
            "{}",
            "Host inference returned no choices"
        );
    }

    @Test
    public void parseResponseBodyRejectsEmptyChoices() throws Exception {
        assertIllegalStateMessage(
            "{\"choices\": []}",
            "Host inference returned no choices"
        );
    }

    @Test
    public void parseResponseBodyRejectsEmptyFlattenedContent() throws Exception {
        assertIllegalStateMessage(
            "{\n" +
                "  \"choices\": [\n" +
                "    {\"message\": {\"content\": [\n" +
                "      {\"type\": \"image_url\", \"image_url\": \"ignored\"},\n" +
                "      {\"text\": \"   \"}\n" +
                "    ]}}\n" +
                "  ]\n" +
                "}",
            "Host inference returned an empty answer"
        );
    }

    private static void assertIllegalStateMessage(String responseBody, String expectedMessage) throws Exception {
        try {
            HostInferenceResponsePolicy.parseResponseBody(responseBody);
            fail("Expected response body to be rejected");
        } catch (IllegalStateException exception) {
            assertEquals(expectedMessage, exception.getMessage());
        }
    }
}

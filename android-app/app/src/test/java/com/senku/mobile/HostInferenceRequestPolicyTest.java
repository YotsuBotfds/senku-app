package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class HostInferenceRequestPolicyTest {
    @Test
    public void completionUriAppendsChatCompletionsPath() {
        HostInferenceConfig.Settings settings =
            new HostInferenceConfig.Settings(true, "http://127.0.0.1:1235/v1", "test-model");

        assertEquals(
            "http://127.0.0.1:1235/v1/chat/completions",
            HostInferenceRequestPolicy.completionUri(settings).toString()
        );
    }

    @Test
    public void buildPayloadIncludesSystemAndUserMessagesWithCustomLimit() throws Exception {
        HostInferenceConfig.Settings settings =
            new HostInferenceConfig.Settings(true, "http://127.0.0.1:1235/v1", "test-model");

        JSONObject payload = HostInferenceRequestPolicy.buildPayload(
            settings,
            "System instructions",
            "User prompt",
            512
        );

        assertEquals("test-model", payload.getString("model"));
        assertEquals(false, payload.getBoolean("stream"));
        assertEquals(512, payload.getInt("max_tokens"));
        JSONArray messages = payload.getJSONArray("messages");
        assertEquals(2, messages.length());
        assertEquals("system", messages.getJSONObject(0).getString("role"));
        assertEquals("System instructions", messages.getJSONObject(0).getString("content"));
        assertEquals("user", messages.getJSONObject(1).getString("role"));
        assertEquals("User prompt", messages.getJSONObject(1).getString("content"));
    }

    @Test
    public void buildPayloadOmitsBlankSystemMessageAndUsesDefaultLimit() throws Exception {
        HostInferenceConfig.Settings settings =
            new HostInferenceConfig.Settings(true, "http://127.0.0.1:1235/v1", "test-model");

        JSONObject payload = HostInferenceRequestPolicy.buildPayload(settings, "  ", "User prompt", null);

        assertEquals(2048, payload.getInt("max_tokens"));
        assertFalse(payload.getBoolean("stream"));
        JSONArray messages = payload.getJSONArray("messages");
        assertEquals(1, messages.length());
        assertEquals("user", messages.getJSONObject(0).getString("role"));
        assertEquals("User prompt", messages.getJSONObject(0).getString("content"));
    }
}

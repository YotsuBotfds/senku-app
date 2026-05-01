package com.senku.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

final class HostInferenceRequestPolicy {
    private static final int DEFAULT_MAX_TOKENS = 2048;
    private static final double TEMPERATURE = 0.11;

    private HostInferenceRequestPolicy() {
    }

    static URI completionUri(HostInferenceConfig.Settings settings) {
        return URI.create(settings.baseUrl + "/chat/completions");
    }

    static JSONObject buildPayload(
        HostInferenceConfig.Settings settings,
        String systemPrompt,
        String prompt,
        Integer maxTokens
    ) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("model", settings.modelId);
        payload.put("temperature", TEMPERATURE);
        payload.put("stream", false);
        payload.put("max_tokens", maxTokens == null ? DEFAULT_MAX_TOKENS : maxTokens);
        payload.put("messages", buildMessages(systemPrompt, prompt));
        return payload;
    }

    private static JSONArray buildMessages(String systemPrompt, String prompt) throws Exception {
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
        return messages;
    }
}

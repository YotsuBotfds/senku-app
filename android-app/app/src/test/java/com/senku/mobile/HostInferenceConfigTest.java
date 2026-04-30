package com.senku.mobile;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class HostInferenceConfigTest {
    @Test
    public void normalizeBaseUrlAppendsV1AndTrimsTrailingSlash() {
        assertEquals(
            "http://10.0.2.2:1235/v1",
            HostInferenceConfig.normalizeBaseUrl(" http://10.0.2.2:1235/ ")
        );
    }

    @Test
    public void normalizeBaseUrlStripsChatCompletionsSuffix() {
        assertEquals(
            "http://10.0.2.2:1235/v1",
            HostInferenceConfig.normalizeBaseUrl("http://10.0.2.2:1235/v1/chat/completions")
        );
    }

    @Test
    public void normalizeBaseUrlKeepsPreV1PathPrefix() {
        assertEquals(
            "http://host.local:1235/openai/v1",
            HostInferenceConfig.normalizeBaseUrl("http://host.local:1235/openai/v1/chat/completions")
        );
    }

    @Test
    public void normalizeBaseUrlFallsBackForBlankValue() {
        assertEquals(
            "http://10.0.2.2:1235/v1",
            HostInferenceConfig.normalizeBaseUrl("   ")
        );
    }

    @Test
    public void normalizeBaseUrlAppendsV1ForHostOnlyValue() {
        assertEquals(
            "http://localhost:1235/v1",
            HostInferenceConfig.normalizeBaseUrl("localhost:1235")
        );
    }

    @Test
    public void normalizeModelFallsBackToLitertE2bDefault() {
        assertEquals("gemma-4-e2b-it-litert", HostInferenceConfig.normalizeModelId("   "));
    }

    @Test
    public void serverLabelUsesJvmSafeUriParsing() {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "http://10.0.2.2:1235/v1",
            "gemma-4-e2b-it-litert"
        );

        assertEquals("10.0.2.2:1235", settings.serverLabel());
    }

    @Test
    public void serverLabelFallsBackToRawBaseUrlWhenHostIsUnavailable() {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "not a uri/v1",
            "gemma-4-e2b-it-litert"
        );

        assertEquals("not a uri/v1", settings.serverLabel());
    }

    @Test
    public void flattenContentReturnsPlainStrings() {
        assertEquals("plain text", HostInferenceClient.flattenContent("plain text"));
    }

    @Test
    public void flattenContentJoinsTextPartsAndPlainStringParts() throws Exception {
        JSONArray content = new JSONArray()
            .put(new JSONObject().put("type", "text").put("text", "first"))
            .put("second")
            .put(new JSONObject().put("type", "image_url").put("image_url", "ignored"))
            .put(new JSONObject().put("text", "third"));

        assertEquals("first\nsecond\nthird", HostInferenceClient.flattenContent(content));
    }

    @Test
    public void flattenContentReturnsEmptyStringForUnsupportedContent() {
        assertEquals("", HostInferenceClient.flattenContent(null));
        assertEquals("", HostInferenceClient.flattenContent(new JSONObject()));
    }
}

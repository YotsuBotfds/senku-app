package com.senku.mobile;

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
    public void flattenContentReturnsPlainStrings() {
        assertEquals("plain text", HostInferenceClient.flattenContent("plain text"));
    }
}

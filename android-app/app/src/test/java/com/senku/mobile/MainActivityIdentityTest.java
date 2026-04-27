package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MainActivityIdentityTest {
    @Test
    public void identitySubtitleSurfacesHostModelTier() {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "http://10.0.2.2:1235/v1",
            "gemma-4-e2b-it-litert"
        );

        String subtitle = MainActivity.buildIdentitySubtitleWithRuntime(
            "271 guides | manual ed. 7",
            MainActivity.buildIdentityRuntimeLabel(settings, "No imported model selected")
        );

        assertEquals("271 guides | manual ed. 7 | E2B host", subtitle);
    }

    @Test
    public void identitySubtitleSurfacesOnDeviceModelTier() {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            false,
            "http://10.0.2.2:1235/v1",
            "gemma-4-e2b-it-litert"
        );

        String subtitle = MainActivity.buildIdentitySubtitleWithRuntime(
            "271 guides | manual ed. 7",
            MainActivity.buildIdentityRuntimeLabel(settings, "gemma-4-E4B-it.litertlm (3.9 GB)")
        );

        assertEquals("271 guides | manual ed. 7 | E4B on-device", subtitle);
    }

    @Test
    public void identitySubtitleOmitsMissingModelFallback() {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            false,
            "http://10.0.2.2:1235/v1",
            "gemma-4-e2b-it-litert"
        );

        String subtitle = MainActivity.buildIdentitySubtitleWithRuntime(
            "271 guides | manual ed. 7",
            MainActivity.buildIdentityRuntimeLabel(settings, "No imported model selected")
        );

        assertEquals("271 guides | manual ed. 7", subtitle);
    }
}

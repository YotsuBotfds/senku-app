package com.senku.mobile;

import java.util.Locale;

final class MainIdentityPresentationPolicy {
    private MainIdentityPresentationPolicy() {
    }

    static String buildSubtitleWithRuntime(String packSubtitle, String runtimeLabel) {
        String subtitle = safe(packSubtitle).trim();
        String runtime = safe(runtimeLabel).trim();
        if (runtime.isEmpty()) {
            return subtitle;
        }
        if (subtitle.isEmpty()) {
            return runtime;
        }
        return subtitle + " | " + runtime;
    }

    static String buildRuntimeLabel(HostInferenceConfig.Settings settings, String modelSummary) {
        if (settings != null && settings.enabled) {
            String modelTier = compactModelTier(settings.modelId);
            return modelTier.isEmpty() ? "Host model" : modelTier + " host";
        }
        String modelTier = compactModelTier(modelSummary);
        return modelTier.isEmpty() ? "" : modelTier + " on-device";
    }

    static String compactModelTier(String modelIdentity) {
        String normalized = safe(modelIdentity).trim().toLowerCase(Locale.US);
        if (normalized.isEmpty() || normalized.startsWith("no imported model")) {
            return "";
        }
        if (normalized.contains("e4b")) {
            return "E4B";
        }
        if (normalized.contains("e2b")) {
            return "E2B";
        }
        if (normalized.contains("gemma")) {
            return "Gemma";
        }
        if (normalized.contains("litert")) {
            return "LiteRT";
        }
        return "";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

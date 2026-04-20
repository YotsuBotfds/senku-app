package com.senku.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.net.URI;
import java.util.Locale;

public final class HostInferenceConfig {
    public static final String EXTRA_HOST_INFERENCE_ENABLED = "host_inference_enabled";
    public static final String EXTRA_HOST_INFERENCE_URL = "host_inference_url";
    public static final String EXTRA_HOST_INFERENCE_MODEL = "host_inference_model";

    private static final String PREFS_NAME = "senku_host_inference";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_MODEL_ID = "model_id";
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:1235/v1";
    private static final String DEFAULT_MODEL_ID = "gemma-4-e2b-it-litert";

    private HostInferenceConfig() {
    }

    public static Settings resolve(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new Settings(
            prefs.getBoolean(KEY_ENABLED, false),
            normalizeBaseUrl(prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL)),
            normalizeModelId(prefs.getString(KEY_MODEL_ID, DEFAULT_MODEL_ID))
        );
    }

    public static boolean applyIntentOverrides(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        boolean hasOverride = intent.hasExtra(EXTRA_HOST_INFERENCE_ENABLED)
            || intent.hasExtra(EXTRA_HOST_INFERENCE_URL)
            || intent.hasExtra(EXTRA_HOST_INFERENCE_MODEL);
        if (!hasOverride) {
            return false;
        }

        Settings current = resolve(context);
        boolean enabled = intent.hasExtra(EXTRA_HOST_INFERENCE_ENABLED)
            ? intent.getBooleanExtra(EXTRA_HOST_INFERENCE_ENABLED, current.enabled)
            : current.enabled;
        String baseUrl = intent.hasExtra(EXTRA_HOST_INFERENCE_URL)
            ? normalizeBaseUrl(intent.getStringExtra(EXTRA_HOST_INFERENCE_URL))
            : current.baseUrl;
        String modelId = intent.hasExtra(EXTRA_HOST_INFERENCE_MODEL)
            ? normalizeModelId(intent.getStringExtra(EXTRA_HOST_INFERENCE_MODEL))
            : current.modelId;
        save(context, enabled, baseUrl, modelId);
        return true;
    }

    public static void setEnabled(Context context, boolean enabled) {
        Settings current = resolve(context);
        save(context, enabled, current.baseUrl, current.modelId);
    }

    public static void save(Context context, boolean enabled, String baseUrl, String modelId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putString(KEY_BASE_URL, normalizeBaseUrl(baseUrl))
            .putString(KEY_MODEL_ID, normalizeModelId(modelId))
            .apply();
    }

    static String normalizeBaseUrl(String baseUrl) {
        String trimmed = safe(baseUrl).trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_BASE_URL;
        }
        String normalized = trimmed.replaceAll("/+$", "");
        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException exc) {
            uri = null;
        }
        String scheme = uri == null ? "" : safe(uri.getScheme());
        String authority = uri == null ? "" : safe(uri.getRawAuthority());
        if (scheme.isEmpty() || authority.isEmpty()) {
            int apiIndex = normalized.toLowerCase(Locale.US).indexOf("/v1");
            if (apiIndex >= 0) {
                normalized = normalized.substring(0, apiIndex);
            }
            return normalized + "/v1";
        }
        String path = safe(uri.getRawPath()).replaceAll("/+$", "");
        String lowerPath = path.toLowerCase(Locale.US);
        if (lowerPath.startsWith("/v1")) {
            path = "";
        } else {
            int apiIndex = lowerPath.indexOf("/v1");
            if (apiIndex >= 0) {
                path = path.substring(0, apiIndex).replaceAll("/+$", "");
            }
        }
        return scheme + "://" + authority + path + "/v1";
    }

    static String normalizeModelId(String modelId) {
        String trimmed = safe(modelId).trim();
        return trimmed.isEmpty() ? DEFAULT_MODEL_ID : trimmed;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    public static final class Settings {
        public final boolean enabled;
        public final String baseUrl;
        public final String modelId;

        Settings(boolean enabled, String baseUrl, String modelId) {
            this.enabled = enabled;
            this.baseUrl = baseUrl;
            this.modelId = modelId;
        }

        public String modeSummary() {
            return enabled ? "Host GPU" : "On-device LiteRT";
        }

        public String serverLabel() {
            URI uri;
            try {
                uri = URI.create(baseUrl);
            } catch (IllegalArgumentException exception) {
                uri = null;
            }
            String host = safe(uri == null ? null : uri.getHost());
            if (host.isEmpty()) {
                return baseUrl;
            }
            int port = uri == null ? -1 : uri.getPort();
            return port > 0 ? host + ":" + port : host;
        }
    }
}

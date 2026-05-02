package com.senku.mobile;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    public void defaultBaseUrlRemainsEmulatorFriendly() {
        assertEquals(
            "http://10.0.2.2:1235/v1",
            HostInferenceConfig.normalizeBaseUrl(null)
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
    public void applyIntentOverridesPersistsNormalizedSettingsForLaterResolve() {
        TestContext context = new TestContext();
        Intent intent = new TestIntent()
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true)
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, " localhost:1235/ ")
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, " local-model ");

        assertTrue(HostInferenceConfig.applyIntentOverridesForTest(context, intent, true));

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        assertTrue(settings.enabled);
        assertEquals("http://localhost:1235/v1", settings.baseUrl);
        assertEquals("local-model", settings.modelId);
    }

    @Test
    public void applyIntentOverridesRejectsNonLocalCleartextAndLeavesExistingSettings() {
        TestContext context = new TestContext();
        HostInferenceConfig.save(context, true, "http://localhost:1235/v1", "existing-model");
        Intent intent = new TestIntent()
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true)
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, "http://example.com:1235/v1")
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, "poisoned-model");

        assertFalse(HostInferenceConfig.applyIntentOverridesForTest(context, intent, true));

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        assertTrue(settings.enabled);
        assertEquals("http://localhost:1235/v1", settings.baseUrl);
        assertEquals("existing-model", settings.modelId);
    }

    @Test
    public void applyIntentOverridesRejectsPrivateLanCleartextAndLeavesExistingSettings() {
        TestContext context = new TestContext();
        HostInferenceConfig.save(context, true, "http://localhost:1235/v1", "existing-model");
        Intent intent = new TestIntent()
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true)
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, "http://192.168.1.50:1235/v1")
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, "lan-model");

        assertFalse(HostInferenceConfig.applyIntentOverridesForTest(context, intent, true));

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        assertTrue(settings.enabled);
        assertEquals("http://localhost:1235/v1", settings.baseUrl);
        assertEquals("existing-model", settings.modelId);
    }

    @Test
    public void applyIntentOverridesAllowsHttpsEndpointByPolicy() {
        TestContext context = new TestContext();
        Intent intent = new TestIntent()
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true)
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, "https://example.com/openai/v1/chat/completions")
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, "remote-model");

        assertTrue(HostInferenceConfig.applyIntentOverridesForTest(context, intent, true));

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        assertTrue(settings.enabled);
        assertEquals("https://example.com/openai/v1", settings.baseUrl);
        assertEquals("remote-model", settings.modelId);
    }

    @Test
    public void applyIntentOverridesRejectsUnauthorizedLauncherIntentAndLeavesExistingSettings() {
        TestContext context = new TestContext();
        HostInferenceConfig.save(context, true, "http://localhost:1235/v1", "existing-model");
        Intent intent = new TestIntent()
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true)
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, "https://example.com/openai/v1/chat/completions")
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, "remote-model");

        assertFalse(HostInferenceConfig.applyIntentOverrides(context, intent));

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        assertTrue(settings.enabled);
        assertEquals("http://localhost:1235/v1", settings.baseUrl);
        assertEquals("existing-model", settings.modelId);
    }

    @Test
    public void applyIntentOverridesAllowsAuthorizedDebugAutomationIntent() {
        TestContext context = new TestContext(true);
        Intent intent = new TestIntent()
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true)
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, "localhost:1235")
            .putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, "authorized-model");
        ReviewDemoPolicy.putProductReviewModeExtras(intent, true);

        assertTrue(HostInferenceConfig.applyIntentOverrides(context, intent));

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        assertTrue(settings.enabled);
        assertEquals("http://localhost:1235/v1", settings.baseUrl);
        assertEquals("authorized-model", settings.modelId);
    }

    @Test
    public void applyIntentOverridesIgnoresIntentsWithoutHostExtras() {
        TestContext context = new TestContext();
        HostInferenceConfig.save(context, true, "http://localhost:1235/v1", "existing-model");

        assertFalse(HostInferenceConfig.applyIntentOverrides(context, new TestIntent()));

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);
        assertTrue(settings.enabled);
        assertEquals("http://localhost:1235/v1", settings.baseUrl);
        assertEquals("existing-model", settings.modelId);
    }

    @Test
    public void directSavedRejectedEndpointStillFailsClientPolicyBeforeConnection() throws Exception {
        TestContext context = new TestContext();
        HostInferenceConfig.save(context, true, "http://example.com:1235/v1", "persisted-remote-model");

        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(context);

        assertTrue(settings.enabled);
        assertEquals("http://example.com:1235/v1", settings.baseUrl);
        try {
            HostInferenceClient.generate(settings, "", "ping", null);
            fail("Expected persisted non-local cleartext endpoint to be rejected by client policy");
        } catch (IllegalStateException exception) {
            assertTrue(exception.getMessage().contains("NON_LOCAL_CLEARTEXT_REJECTED"));
            assertFalse(exception.getMessage().contains("example.com"));
            assertFalse(exception.getMessage().contains("http://"));
        }
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
    public void serverLabelHidesRawBaseUrlWhenHostIsUnavailable() {
        HostInferenceConfig.Settings settings = new HostInferenceConfig.Settings(
            true,
            "not a uri/v1",
            "gemma-4-e2b-it-litert"
        );

        assertEquals("host runtime", settings.serverLabel());
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

    private static final class TestContext extends ContextWrapper {
        private final Map<String, SharedPreferences> prefs = new HashMap<>();
        private final ApplicationInfo applicationInfo = new ApplicationInfo();

        TestContext() {
            this(false);
        }

        TestContext(boolean debugBuild) {
            super(null);
            if (debugBuild) {
                applicationInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
            }
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return applicationInfo;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            SharedPreferences existing = prefs.get(name);
            if (existing != null) {
                return existing;
            }
            SharedPreferences created = new InMemorySharedPreferences();
            prefs.put(name, created);
            return created;
        }
    }

    private static final class TestIntent extends Intent {
        private final Map<String, Object> extras = new HashMap<>();

        @Override
        public Intent putExtra(String name, boolean value) {
            extras.put(name, value);
            return this;
        }

        @Override
        public Intent putExtra(String name, String value) {
            extras.put(name, value);
            return this;
        }

        @Override
        public boolean hasExtra(String name) {
            return extras.containsKey(name);
        }

        @Override
        public boolean getBooleanExtra(String name, boolean defaultValue) {
            Object value = extras.get(name);
            return value instanceof Boolean ? (Boolean) value : defaultValue;
        }

        @Override
        public String getStringExtra(String name) {
            Object value = extras.get(name);
            return value instanceof String ? (String) value : null;
        }
    }

    private static final class InMemorySharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Map<String, ?> getAll() {
            return new HashMap<>(values);
        }

        @Override
        public String getString(String key, String defValue) {
            Object value = values.get(key);
            return value instanceof String ? (String) value : defValue;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Object value = values.get(key);
            return value instanceof Set ? (Set<String>) value : defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            Object value = values.get(key);
            return value instanceof Integer ? (Integer) value : defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            Object value = values.get(key);
            return value instanceof Long ? (Long) value : defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            Object value = values.get(key);
            return value instanceof Float ? (Float) value : defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object value = values.get(key);
            return value instanceof Boolean ? (Boolean) value : defValue;
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public Editor edit() {
            return new InMemoryEditor();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        private final class InMemoryEditor implements Editor {
            private final Map<String, Object> pending = new HashMap<>();
            private final java.util.HashSet<String> removals = new java.util.HashSet<>();
            private boolean clear;

            @Override
            public Editor putString(String key, String value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> values) {
                pending.put(key, values);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                pending.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor remove(String key) {
                pending.remove(key);
                removals.add(key);
                return this;
            }

            @Override
            public Editor clear() {
                clear = true;
                pending.clear();
                removals.clear();
                return this;
            }

            @Override
            public boolean commit() {
                apply();
                return true;
            }

            @Override
            public void apply() {
                if (clear) {
                    values.clear();
                }
                for (String key : removals) {
                    values.remove(key);
                }
                values.putAll(pending);
            }
        }
    }
}

package com.senku.mobile;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class ModelFileStoreTest {
    private static final String PREFS_NAME = "senku_model_store";
    private static final String KEY_MODEL_NAME = "model_name";
    private static final String KEY_MODEL_PATH = "model_path";

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void getImportedModelFileReturnsPersistedModelWhenFileExists() throws Exception {
        TestContext context = new TestContext(temporaryFolder);
        File persisted = writeModel(new File(context.getFilesDir(), "chosen.task"), "chosen");
        prefs(context)
            .edit()
            .putString(KEY_MODEL_NAME, persisted.getName())
            .putString(KEY_MODEL_PATH, persisted.getAbsolutePath())
            .apply();

        assertEquals(persisted, ModelFileStore.getImportedModelFile(context));
    }

    @Test
    public void getImportedModelFileClearsStalePersistedPathBeforeFallback() throws Exception {
        TestContext context = new TestContext(temporaryFolder);
        File fallback = writeModel(
            new File(new File(context.getFilesDir(), "models"), "fallback.task"),
            "fallback"
        );
        prefs(context)
            .edit()
            .putString(KEY_MODEL_NAME, "missing.task")
            .putString(KEY_MODEL_PATH, new File(context.getFilesDir(), "missing.task").getAbsolutePath())
            .apply();

        assertEquals(fallback, ModelFileStore.getImportedModelFile(context));

        SharedPreferences prefs = prefs(context);
        assertFalse(prefs.contains(KEY_MODEL_NAME));
        assertFalse(prefs.contains(KEY_MODEL_PATH));
    }

    @Test
    public void getImportedModelFileClearsUnsupportedPersistedPathBeforeFallback() throws Exception {
        TestContext context = new TestContext(temporaryFolder);
        File unsupported = writeModel(new File(context.getFilesDir(), "notes.txt"), "not a model");
        File fallback = writeModel(
            new File(new File(context.getFilesDir(), "models"), "fallback.task"),
            "fallback"
        );
        prefs(context)
            .edit()
            .putString(KEY_MODEL_NAME, unsupported.getName())
            .putString(KEY_MODEL_PATH, unsupported.getAbsolutePath())
            .apply();

        assertEquals(fallback, ModelFileStore.getImportedModelFile(context));

        SharedPreferences prefs = prefs(context);
        assertFalse(prefs.contains(KEY_MODEL_NAME));
        assertFalse(prefs.contains(KEY_MODEL_PATH));
    }

    @Test
    public void getImportedModelFileClearsEmptyPersistedModelBeforeFallback() throws Exception {
        TestContext context = new TestContext(temporaryFolder);
        File emptyModel = writeModel(new File(context.getFilesDir(), "empty.task"), "");
        File fallback = writeModel(
            new File(new File(context.getFilesDir(), "models"), "fallback.task"),
            "fallback"
        );
        prefs(context)
            .edit()
            .putString(KEY_MODEL_NAME, emptyModel.getName())
            .putString(KEY_MODEL_PATH, emptyModel.getAbsolutePath())
            .apply();

        assertEquals(fallback, ModelFileStore.getImportedModelFile(context));

        SharedPreferences prefs = prefs(context);
        assertFalse(prefs.contains(KEY_MODEL_NAME));
        assertFalse(prefs.contains(KEY_MODEL_PATH));
    }

    @Test
    public void getImportedModelFileReturnsNullAndClearsStateWhenStalePathHasNoFallback() throws Exception {
        TestContext context = new TestContext(temporaryFolder);
        prefs(context)
            .edit()
            .putString(KEY_MODEL_NAME, "missing.task")
            .putString(KEY_MODEL_PATH, new File(context.getFilesDir(), "missing.task").getAbsolutePath())
            .apply();

        assertNull(ModelFileStore.getImportedModelFile(context));

        SharedPreferences prefs = prefs(context);
        assertFalse(prefs.contains(KEY_MODEL_NAME));
        assertFalse(prefs.contains(KEY_MODEL_PATH));
    }

    @Test
    public void getImportedModelFileReturnsNullAndClearsUnsupportedPersistedPathWhenNoFallback() throws Exception {
        TestContext context = new TestContext(temporaryFolder);
        File unsupported = writeModel(new File(context.getFilesDir(), "model.zip"), "not a model");
        prefs(context)
            .edit()
            .putString(KEY_MODEL_NAME, unsupported.getName())
            .putString(KEY_MODEL_PATH, unsupported.getAbsolutePath())
            .apply();

        assertNull(ModelFileStore.getImportedModelFile(context));

        SharedPreferences prefs = prefs(context);
        assertFalse(prefs.contains(KEY_MODEL_NAME));
        assertFalse(prefs.contains(KEY_MODEL_PATH));
    }

    @Test
    public void fallbackPrefersKnownInternalModelBeforeNewerGenericModel() throws Exception {
        TestContext context = new TestContext(temporaryFolder);
        File modelsDir = new File(context.getFilesDir(), "models");
        File newerGeneric = writeModel(new File(modelsDir, "newer.task"), "newer");
        File known = writeModel(new File(modelsDir, "gemma-4-E4B-it.task"), "known");
        newerGeneric.setLastModified(known.lastModified() + 10_000L);

        assertEquals(known, ModelFileStore.getImportedModelFile(context));
    }

    @Test
    public void policySanitizesFileNamesAndFallsBackWhenEmpty() {
        assertEquals("my_model__1_.task", ModelFileStorePolicy.sanitizeFileName("my model (1).task"));
        assertEquals("offline-model.litertlm", ModelFileStorePolicy.sanitizeFileName(""));
    }

    @Test
    public void policySanitizesNullAndWhitespaceNamesToDisplayFallback() {
        assertEquals("offline-model.litertlm", ModelFileStorePolicy.sanitizeFileName(null));
        assertEquals("offline-model.litertlm", ModelFileStorePolicy.sanitizeFileName(" \t\n "));
    }

    @Test
    public void policyTrimsDisplayNameBeforeSanitizing() {
        assertEquals("model.task", ModelFileStorePolicy.sanitizeFileName("  model.task  "));
    }

    @Test
    public void policySanitizesPathSeparatorsAndControlCharacters() {
        assertEquals(
            "dir_nested_model_.task",
            ModelFileStorePolicy.sanitizeFileName("dir\\nested/model\u0007.task")
        );
    }

    @Test
    public void policySanitizesUnicodeDisplayNamesToAsciiSafeFileNames() {
        assertEquals(
            "gemma-__-model.task",
            ModelFileStorePolicy.sanitizeFileName("gemma-\u6E2C\u8A66-model.task")
        );
    }

    @Test
    public void policyFindsNewestSupportedModelFile() throws Exception {
        File modelsDir = temporaryFolder.newFolder("candidate-models");
        File oldTask = writeModel(new File(modelsDir, "old.task"), "old");
        File ignored = writeModel(new File(modelsDir, "newer.txt"), "ignored");
        File newLiteRt = writeModel(new File(modelsDir, "new.litertlm"), "new");
        oldTask.setLastModified(1_000L);
        ignored.setLastModified(3_000L);
        newLiteRt.setLastModified(2_000L);

        assertEquals(newLiteRt, ModelFileStorePolicy.findNewestModelFile(modelsDir.listFiles()));
    }

    @Test
    public void policyAcceptsSupportedExtensionsCaseInsensitivelyAndRejectsTrailingExtensions() throws Exception {
        File modelsDir = temporaryFolder.newFolder("extension-model-checks");
        File uppercaseTask = writeModel(new File(modelsDir, "offline.TASK"), "model");
        File uppercaseLiteRt = writeModel(new File(modelsDir, "offline.LITERTLM"), "model");
        File trailingZip = writeModel(new File(modelsDir, "offline.task.zip"), "not-model");
        File trailingBackup = writeModel(new File(modelsDir, "offline.litertlm.backup"), "not-model");

        assertTrue(ModelFileStorePolicy.isSupportedModelFile(uppercaseTask));
        assertTrue(ModelFileStorePolicy.isSupportedModelFile(uppercaseLiteRt));
        assertFalse(ModelFileStorePolicy.isSupportedModelFile(trailingZip));
        assertFalse(ModelFileStorePolicy.isSupportedModelFile(trailingBackup));
        assertTrue(ModelFileStorePolicy.isSupportedModelFileName("offline.task"));
        assertFalse(ModelFileStorePolicy.isSupportedModelFileName("offline.task.zip"));
    }

    @Test
    public void policyRejectsUnsupportedFilesAndSupportedExtensionDirectories() throws Exception {
        File modelsDir = temporaryFolder.newFolder("supported-model-checks");
        File supported = writeModel(new File(modelsDir, "offline.litertlm"), "model");
        File unsupported = writeModel(new File(modelsDir, "offline.bin"), "not-model");
        File supportedLookingDirectory = new File(modelsDir, "directory.task");
        assertTrue(supportedLookingDirectory.mkdir());

        assertTrue(ModelFileStorePolicy.isSupportedModelFile(supported));
        assertFalse(ModelFileStorePolicy.isSupportedModelFile(unsupported));
        assertFalse(ModelFileStorePolicy.isSupportedModelFile(supportedLookingDirectory));
    }

    @Test
    public void policyRejectsEmptySupportedExtensionFiles() throws Exception {
        File modelsDir = temporaryFolder.newFolder("empty-model-checks");
        File emptyTask = writeModel(new File(modelsDir, "empty.task"), "");

        assertFalse(ModelFileStorePolicy.isSupportedModelFile(emptyTask));
        assertNull(ModelFileStorePolicy.findNewestModelFile(modelsDir.listFiles()));
    }

    private static SharedPreferences prefs(TestContext context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static File writeModel(File file, String contents) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create " + parent);
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(contents.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return file;
    }

    private static final class TestContext extends ContextWrapper {
        private final File filesDir;
        private final File externalFilesDir;
        private final Map<String, SharedPreferences> prefs = new HashMap<>();

        TestContext(TemporaryFolder temporaryFolder) throws IOException {
            super(null);
            filesDir = temporaryFolder.newFolder("files");
            externalFilesDir = temporaryFolder.newFolder("external");
        }

        @Override
        public File getFilesDir() {
            return filesDir;
        }

        @Override
        public File getExternalFilesDir(String type) {
            return externalFilesDir;
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
            private final Set<String> removals = new java.util.HashSet<>();
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

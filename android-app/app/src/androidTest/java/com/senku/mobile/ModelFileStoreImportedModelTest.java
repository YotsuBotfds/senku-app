package com.senku.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

@RunWith(AndroidJUnit4.class)
public final class ModelFileStoreImportedModelTest {
    private static final String PREFS_NAME = "senku_model_store";
    private static final String KEY_MODEL_NAME = "model_name";
    private static final String KEY_MODEL_PATH = "model_path";

    @Test
    public void importedLiteRtModelIsDiscoverableAndNonEmpty() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File modelFile = ModelFileStore.getImportedModelFile(context);
        assumeNotNull("no imported local LiteRT model installed", modelFile);

        String fileName = modelFile.getName().toLowerCase(Locale.US);
        assertTrue(
            "imported model must use .litertlm or .task extension: " + modelFile.getName(),
            fileName.endsWith(".litertlm") || fileName.endsWith(".task")
        );
        assertTrue("imported model must be non-empty: " + modelFile.getAbsolutePath(), modelFile.length() > 0L);
        captureHarnessArtifacts(context);
    }

    @Test
    public void importRejectsUnsupportedModelNameWithoutCopyingOrChangingPrefs() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String previousName = prefs.getString(KEY_MODEL_NAME, null);
        String previousPath = prefs.getString(KEY_MODEL_PATH, null);
        File target = new File(new File(context.getFilesDir(), "models"), "model.bin");
        if (target.exists()) {
            assertTrue("stale unsupported target should be removable", target.delete());
        }
        File source = writeFile(new File(context.getCacheDir(), "model.bin"), "not a model");

        try {
            ModelFileStore.importModel(context, Uri.fromFile(source));
            fail("Expected unsupported model extension to be rejected");
        } catch (IOException exc) {
            assertTrue(exc.getMessage().contains(".litertlm"));
            assertTrue(exc.getMessage().contains(".task"));
        }

        assertFalse("unsupported model should not be copied into the model directory", target.exists());
        assertEquals(previousName, prefs.getString(KEY_MODEL_NAME, null));
        assertEquals(previousPath, prefs.getString(KEY_MODEL_PATH, null));
    }

    private static File writeFile(File file, String contents) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            assertTrue("test parent directory should be created", parent.mkdirs());
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(contents.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return file;
    }

    private static void captureHarnessArtifacts(Context context) {
        File artifactDir = new File(context.getFilesDir(), "test-artifacts");
        if (!artifactDir.exists()) {
            assertTrue("test artifact directory should be created", artifactDir.mkdirs());
        }
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assertTrue(
            "local model readiness screenshot should be captured",
            device.takeScreenshot(new File(artifactDir, "local_model_readiness.png"))
        );
        try {
            device.dumpWindowHierarchy(new File(artifactDir, "local_model_readiness.xml"));
        } catch (Exception exc) {
            throw new AssertionError("local model readiness dump should be captured", exc);
        }
    }
}

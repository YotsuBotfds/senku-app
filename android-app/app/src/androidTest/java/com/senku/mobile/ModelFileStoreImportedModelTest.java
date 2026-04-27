package com.senku.mobile;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Locale;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

@RunWith(AndroidJUnit4.class)
public final class ModelFileStoreImportedModelTest {
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

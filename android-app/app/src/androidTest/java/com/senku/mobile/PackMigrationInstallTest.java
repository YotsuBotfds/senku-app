package com.senku.mobile;

import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.CURRENT_HEAD_ANSWER_CARD_COUNT;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.PACK_DIR;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public final class PackMigrationInstallTest {
    @Test
    public void cleanInstallHydratesBundledCurrentHeadPackIntoAppPrivateFiles() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File packRoot = new File(context.getFilesDir(), PACK_DIR);
        deleteRecursively(packRoot);

        PackInstaller.InstalledPack pack = installBundledCurrentHeadPack(context, "clean install hydration");

        assertEquals(packRoot.getAbsolutePath(), pack.rootDir.getAbsolutePath());
        assertEquals(CURRENT_HEAD_ANSWER_CARD_COUNT, pack.manifest.answerCardCount);
        assertTrue(pack.databaseFile.isFile());
        assertTrue(pack.vectorFile.isFile());
    }

    private static void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        assertTrue("failed to delete stale pack file before clean install: " + file.getAbsolutePath(), file.delete());
    }
}

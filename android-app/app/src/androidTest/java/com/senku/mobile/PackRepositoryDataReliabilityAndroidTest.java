package com.senku.mobile;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class PackRepositoryDataReliabilityAndroidTest {
    @Test
    public void corruptVectorCopyDisablesVectorStoreAndKeepsRainShelterSearchAvailable() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "corrupt vector boundary fallback"
        );
        File corruptVectorFile = File.createTempFile("corrupt-current-head-vector", ".vec", context.getCacheDir());
        copyFile(pack.vectorFile, corruptVectorFile);
        try (FileOutputStream output = new FileOutputStream(corruptVectorFile, false)) {
            output.write(new byte[]{1, 2, 3, 4});
        }

        try (PackRepository repository = new PackRepository(pack.databaseFile, corruptVectorFile)) {
            assertFalse("corrupt vector file should be disabled", repository.hasVectorStore());

            List<SearchResult> results = repository.search("rain shelter", 5);

            assertFalse("lexical or route fallback should return rain shelter results", results.isEmpty());
            assertTrue(
                "fallback results should include lexical or route retrieval modes: " + summarizeModes(results),
                containsLexicalOrRouteResult(results)
            );
        }
    }

    private static void copyFile(File source, File target) throws Exception {
        try (FileInputStream input = new FileInputStream(source);
             FileOutputStream output = new FileOutputStream(target, false)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
    }

    private static boolean containsLexicalOrRouteResult(List<SearchResult> results) {
        for (SearchResult result : results) {
            String mode = safe(result.retrievalMode).trim().toLowerCase(Locale.US);
            if ("lexical".equals(mode) || "route-focus".equals(mode) || "guide-focus".equals(mode)) {
                return true;
            }
        }
        return false;
    }

    private static String summarizeModes(List<SearchResult> results) {
        StringBuilder summary = new StringBuilder();
        for (SearchResult result : results) {
            if (summary.length() > 0) {
                summary.append(" | ");
            }
            summary.append(safe(result.guideId))
                .append("/")
                .append(safe(result.retrievalMode));
        }
        return summary.toString();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

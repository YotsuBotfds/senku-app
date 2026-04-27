package com.senku.mobile;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.CURRENT_HEAD_ANSWER_CARD_COUNT;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.MANIFEST_NAME;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.PACK_DIR;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.assumeCurrentHeadPack;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.databaseFile;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.manifestFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public final class PackMigrationInstallTest {
    private static final String ASSET_MANIFEST = PACK_DIR + "/" + MANIFEST_NAME;

    @Test
    public void normalInstallPreservesUsableCurrentHeadPackWhenAssetsAreOlder() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        PackManifest installedManifest = assumeCurrentHeadPack(
            manifestFile(context),
            databaseFile(context),
            "pack migration install preservation"
        );
        File installedVectorFile = vectorFileForManifest(context, installedManifest);
        assumeTrue(
            "installed mobile pack vector file is absent; push current-head pack before running migration install preservation",
            installedVectorFile.isFile()
        );
        assumeTrue(
            "installed current-head database size does not match its manifest; reinstall current-head pack before running migration install preservation",
            databaseFile(context).length() == installedManifest.sqliteBytes
        );
        assumeTrue(
            "installed current-head vector size does not match its manifest; reinstall current-head pack before running migration install preservation",
            installedVectorFile.length() == installedManifest.vectorBytes
        );

        PackManifest assetManifest = assumeAssetPackManifest(context);
        assumeTrue(
            "shipped asset pack is already current-head; migration install preservation requires an older asset pack",
            assetManifest.answerCardCount < CURRENT_HEAD_ANSWER_CARD_COUNT
        );

        PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(context, false);

        assertFalse("normal install must not downgrade to the older asset pack", pack.manifest.answerCardCount == assetManifest.answerCardCount);
        assertEquals(CURRENT_HEAD_ANSWER_CARD_COUNT, pack.manifest.answerCardCount);
        assertEquals(installedManifest.generatedAt, pack.manifest.generatedAt);
        assertEquals(databaseFile(context).getAbsolutePath(), pack.databaseFile.getAbsolutePath());
        assertEquals(installedVectorFile.getAbsolutePath(), pack.vectorFile.getAbsolutePath());
        assertTrue(pack.databaseFile.isFile());
        assertTrue(pack.vectorFile.isFile());
    }

    private static PackManifest assumeAssetPackManifest(Context context) throws Exception {
        PackManifest manifest = null;
        try {
            manifest = PackManifest.fromJson(readAssetText(context, ASSET_MANIFEST));
        } catch (IOException exc) {
            assumeTrue(
                "asset pack manifest is absent; include app assets before running migration install preservation: " + exc,
                false
            );
        }
        assumeTrue("asset pack manifest could not be read", manifest != null);
        return manifest;
    }

    private static String readAssetText(Context context, String assetPath) throws IOException {
        try (InputStream input = context.getAssets().open(assetPath)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static File vectorFileForManifest(Context context, PackManifest manifest) {
        String vectorName = "int8".equals(manifest.vectorDtype) ? "senku_vectors.i8" : "senku_vectors.f16";
        return new File(new File(context.getFilesDir(), PACK_DIR), vectorName);
    }
}

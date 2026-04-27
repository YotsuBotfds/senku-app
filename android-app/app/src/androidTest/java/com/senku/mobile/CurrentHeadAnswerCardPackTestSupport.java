package com.senku.mobile;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assume.assumeTrue;

final class CurrentHeadAnswerCardPackTestSupport {
    static final int CURRENT_HEAD_ANSWER_CARD_COUNT = 271;
    static final String PACK_DIR = "mobile_pack";
    static final String MANIFEST_NAME = "senku_manifest.json";
    static final String DATABASE_NAME = "senku_mobile.sqlite3";

    private CurrentHeadAnswerCardPackTestSupport() {
    }

    static File manifestFile(Context context) {
        return new File(packRoot(context), MANIFEST_NAME);
    }

    static File databaseFile(Context context) {
        return new File(packRoot(context), DATABASE_NAME);
    }

    static String readFileText(File file) throws Exception {
        try (FileInputStream input = new FileInputStream(file)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static PackManifest assumeCurrentHeadPack(
        File manifestFile,
        File databaseFile,
        String absentPackPurpose
    ) throws Exception {
        assumeTrue(
            "installed mobile pack manifest is absent; push current-head pack before running " + absentPackPurpose,
            manifestFile.isFile()
        );
        assumeTrue(
            "installed mobile pack database is absent; push current-head pack before running " + absentPackPurpose,
            databaseFile.isFile()
        );

        PackManifest manifest = PackManifest.fromJson(readFileText(manifestFile));
        assumeTrue(
            "installed mobile pack is not the current-head 271-answer-card pack; found manifest answer_cards="
                + manifest.answerCardCount
                + " generated_at="
                + manifest.generatedAt,
            manifest.answerCardCount == CURRENT_HEAD_ANSWER_CARD_COUNT
        );
        return manifest;
    }

    private static File packRoot(Context context) {
        return new File(context.getFilesDir(), PACK_DIR);
    }
}

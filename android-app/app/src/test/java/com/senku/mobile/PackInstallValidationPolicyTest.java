package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackInstallValidationPolicyTest {
    @Test
    public void forceOrMissingInstalledManifestInstallsFromAssets() throws Exception {
        PackManifest assetManifest = manifest(2, "2026-04-27T04:21:12Z", 271);

        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(true, assetManifest, assetManifest));
        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(false, assetManifest, null));
    }

    @Test
    public void sameOrNewerInstalledManifestIsPreserved() throws Exception {
        PackManifest assetManifest = manifest(2, "2026-04-27T04:21:12Z", 271);
        PackManifest sameInstalled = manifest(2, "2026-04-27T04:21:12Z", 271);
        PackManifest newerInstalled = manifest(2, "2026-04-28T04:21:12Z", 271);
        PackManifest richerInstalled = manifest(2, "2026-04-27T04:21:12Z", 300);

        assertFalse(PackInstallValidationPolicy.shouldInstallFromAssets(false, assetManifest, sameInstalled));
        assertFalse(PackInstallValidationPolicy.shouldInstallFromAssets(false, assetManifest, newerInstalled));
        assertFalse(PackInstallValidationPolicy.shouldInstallFromAssets(false, assetManifest, richerInstalled));
    }

    @Test
    public void newerBundledManifestInstallsFromAssets() throws Exception {
        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(3, "2026-04-27T04:21:12Z", 271),
            manifest(2, "2026-04-27T04:21:12Z", 271)
        ));
        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(2, "2026-04-28T04:21:12Z", 271),
            manifest(2, "2026-04-27T04:21:12Z", 271)
        ));
        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(2, "2026-04-27T04:21:12Z", 300),
            manifest(2, "2026-04-27T04:21:12Z", 271)
        ));
    }

    @Test
    public void packVersionWinsOverNewerDateOrHigherAnswerCardCount() throws Exception {
        assertFalse(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(1, "2026-05-01T04:21:12Z", 999),
            manifest(2, "2026-04-01T04:21:12Z", 271)
        ));
        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(3, "2026-04-01T04:21:12Z", 100),
            manifest(2, "2026-05-01T04:21:12Z", 999)
        ));
    }

    @Test
    public void sameVersionAndDatePreservesInstalledWhenBundledHasFewerAnswerCards() throws Exception {
        assertFalse(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(2, "2026-04-27T04:21:12Z", 200),
            manifest(2, "2026-04-27T04:21:12Z", 271)
        ));
    }

    @Test
    public void newerGeneratedAtInstalledManifestIsPreservedEvenWithLowerAnswerCardCount() throws Exception {
        assertFalse(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(2, "2026-04-27T04:21:12Z", 271),
            manifest(2, "2026-04-28T04:21:12Z", 200)
        ));
    }

    @Test
    public void unparsableGeneratedAtUsesStableStringOrderingBeforeAnswerCardCount() throws Exception {
        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(2, "generated-b", 100),
            manifest(2, "generated-a", 999)
        ));
        assertFalse(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(2, "generated-a", 999),
            manifest(2, "generated-b", 100)
        ));
        assertTrue(PackInstallValidationPolicy.shouldInstallFromAssets(
            false,
            manifest(2, "generated-a", 300),
            manifest(2, "generated-a", 271)
        ));
    }

    private static PackManifest manifest(int packVersion, String generatedAt, int answerCardCount) throws Exception {
        return PackManifest.fromJson(
            "{\n" +
                "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
                "  \"pack_version\": " + packVersion + ",\n" +
                "  \"generated_at\": \"" + generatedAt + "\",\n" +
                "  \"counts\": {\n" +
                "    \"guides\": 754,\n" +
                "    \"chunks\": 49841,\n" +
                "    \"deterministic_rules\": 9,\n" +
                "    \"guide_related_links\": 5750,\n" +
                "    \"answer_cards\": " + answerCardCount + "\n" +
                "  },\n" +
                "  \"embedding\": {\n" +
                "    \"model_id\": \"test-embedding\",\n" +
                "    \"dimension\": 768,\n" +
                "    \"vector_dtype\": \"float16\"\n" +
                "  },\n" +
                "  \"runtime_defaults\": {\"mobile_top_k\": 8},\n" +
                "  \"files\": {\n" +
                "    \"sqlite\": {\"bytes\": 12, \"sha256\": \"abc\"},\n" +
                "    \"vectors\": {\"bytes\": 34, \"sha256\": \"def\"}\n" +
                "  }\n" +
                "}"
        );
    }
}

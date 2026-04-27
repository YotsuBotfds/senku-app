package com.senku.mobile;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class PackInstallerTest {
    @Test
    public void sha256HexForTestMatchesKnownDigest() throws Exception {
        File tempFile = File.createTempFile("pack-installer", ".txt");
        try {
            byte[] content = "senku".getBytes(StandardCharsets.UTF_8);
            Files.write(tempFile.toPath(), content);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] expectedBytes = digest.digest(content);
            StringBuilder expected = new StringBuilder(expectedBytes.length * 2);
            for (byte b : expectedBytes) {
                expected.append(String.format("%02x", b));
            }

            assertEquals(expected.toString(), PackInstaller.sha256HexForTest(tempFile));
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void missingRequiredPackTablesAcceptsFts4AsLexicalFallback() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "guide_related", "lexical_chunks_fts4"))
        );
    }

    @Test
    public void missingRequiredPackTablesDoesNotRequireOptionalAnswerCardTables() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "guide_related", "lexical_chunks_fts"))
        );
    }

    @Test
    public void missingRequiredPackTablesIgnoresFutureOptionalTables() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(Set.of(
                "guides",
                "guide_related",
                "lexical_chunks_fts",
                "answer_cards",
                "answer_card_clauses",
                "answer_card_sources",
                "answer_card_tags",
                "retrieval_metadata_v2"
            ))
        );
    }

    @Test
    public void missingRequiredPackTablesRequiresAtLeastOneLexicalTable() {
        assertEquals(
            "lexical_chunks_fts or lexical_chunks_fts4",
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "guide_related"))
        );
    }

    @Test
    public void validateVectorInfoAcceptsMatchingFloat16ManifestAndHeader() throws Exception {
        PackInstaller.validateVectorInfoForTest(
            manifestWithVector("float16", 768),
            vectorInfo("SNKUVEC1", 1, 32, 49841, 768, 1, 0)
        );
    }

    @Test
    public void validateVectorInfoRejectsDimensionMismatch() throws Exception {
        expectVectorInfoRejected(
            manifestWithVector("float16", 768),
            vectorInfo("SNKUVEC1", 1, 32, 49841, 384, 1, 0),
            "dimension mismatch"
        );
    }

    @Test
    public void validateVectorInfoRejectsDtypeMismatch() throws Exception {
        expectVectorInfoRejected(
            manifestWithVector("float16", 768),
            vectorInfo("SNKUVEC1", 1, 32, 49841, 768, 2, 0),
            "dtype mismatch"
        );
    }

    @Test
    public void validateVectorInfoRejectsStaleVectorAfterManifestDtypeRefresh() throws Exception {
        expectVectorInfoRejected(
            manifestWithVector("int8", 768),
            vectorInfo("SNKUVEC1", 1, 32, 49841, 768, 1, 0),
            "dtype mismatch"
        );
    }

    @Test
    public void validateVectorInfoAcceptsMatchingInt8ManifestAndHeader() throws Exception {
        PackInstaller.validateVectorInfoForTest(
            manifestWithVector("int8", 768),
            vectorInfo("SNKUVEC1", 1, 32, 49841, 768, 2, 0)
        );
    }

    @Test
    public void validateVectorInfoRejectsUnsupportedHeaderBasics() throws Exception {
        PackManifest manifest = manifestWithVector("float16", 768);

        expectVectorInfoRejected(
            manifest,
            vectorInfo("BADVEC01", 1, 32, 49841, 768, 1, 0),
            "magic"
        );
        expectVectorInfoRejected(
            manifest,
            vectorInfo("SNKUVEC1", 2, 32, 49841, 768, 1, 0),
            "version"
        );
        expectVectorInfoRejected(
            manifest,
            vectorInfo("SNKUVEC1", 1, 64, 49841, 768, 1, 0),
            "header bytes"
        );
    }

    @Test
    public void shouldInstallFromAssetsPreservesUsableInstalledPackOnNormalInstall() throws Exception {
        InstalledPackFiles files = installedPackFiles(123, 456, 123, 456);

        assertEquals(
            false,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenForcedEvenIfInstalledPackIsUsable() throws Exception {
        InstalledPackFiles files = installedPackFiles(123, 456, 123, 456);

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                true,
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledFilesAreMissing() throws Exception {
        Path tempDir = Files.createTempDirectory("pack-installer");

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                tempDir.resolve("senku_manifest.json").toFile(),
                tempDir.resolve("senku_mobile.sqlite3").toFile(),
                tempDir.resolve("senku_vectors.f16").toFile()
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledSizesDoNotMatchManifest() throws Exception {
        InstalledPackFiles files = installedPackFiles(123, 456, 122, 456);

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledManifestIsMalformed() throws Exception {
        Path tempDir = Files.createTempDirectory("pack-installer");
        File manifestFile = tempDir.resolve("senku_manifest.json").toFile();
        File sqliteFile = tempDir.resolve("senku_mobile.sqlite3").toFile();
        File vectorFile = tempDir.resolve("senku_vectors.f16").toFile();
        Files.write(manifestFile.toPath(), "{ bad manifest".getBytes(StandardCharsets.UTF_8));
        Files.write(sqliteFile.toPath(), repeatedBytes(123));
        Files.write(vectorFile.toPath(), repeatedBytes(456));

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestFile,
                sqliteFile,
                vectorFile
            )
        );
    }

    private static PackInstaller.VectorInfo vectorInfo(
        String magic,
        int version,
        int headerBytes,
        int rowCount,
        int dimension,
        int dtypeCode,
        int flags
    ) {
        return new PackInstaller.VectorInfo(magic, version, headerBytes, rowCount, dimension, dtypeCode, flags);
    }

    private static PackManifest manifestWithVector(String vectorDtype, int dimension) throws Exception {
        return PackManifest.fromJson(
            "{\n" +
                "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
                "  \"pack_version\": 2,\n" +
                "  \"generated_at\": \"2026-04-12T19:23:50Z\",\n" +
                "  \"counts\": {\n" +
                "    \"guides\": 754,\n" +
                "    \"chunks\": 49841,\n" +
                "    \"deterministic_rules\": 9,\n" +
                "    \"guide_related_links\": 5750,\n" +
                "    \"answer_cards\": 271\n" +
                "  },\n" +
                "  \"embedding\": {\n" +
                "    \"model_id\": \"nomic-ai/text-embedding-nomic-embed-text-v1.5\",\n" +
                "    \"dimension\": " + dimension + ",\n" +
                "    \"vector_dtype\": \"" + vectorDtype + "\"\n" +
                "  },\n" +
                "  \"runtime_defaults\": {\n" +
                "    \"mobile_top_k\": 10\n" +
                "  },\n" +
                "  \"files\": {\n" +
                "    \"sqlite\": {\n" +
                "      \"bytes\": 123,\n" +
                "      \"sha256\": \"abc\"\n" +
                "    },\n" +
                "    \"vectors\": {\n" +
                "      \"bytes\": 456,\n" +
                "      \"sha256\": \"def\"\n" +
                "    }\n" +
                "  }\n" +
                "}"
        );
    }

    private static InstalledPackFiles installedPackFiles(
        long manifestSqliteBytes,
        long manifestVectorBytes,
        int actualSqliteBytes,
        int actualVectorBytes
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("pack-installer");
        File manifestFile = tempDir.resolve("senku_manifest.json").toFile();
        File sqliteFile = tempDir.resolve("senku_mobile.sqlite3").toFile();
        File vectorFile = tempDir.resolve("senku_vectors.f16").toFile();
        Files.write(
            manifestFile.toPath(),
            manifestJsonWithFileSizes(manifestSqliteBytes, manifestVectorBytes).getBytes(StandardCharsets.UTF_8)
        );
        Files.write(sqliteFile.toPath(), repeatedBytes(actualSqliteBytes));
        Files.write(vectorFile.toPath(), repeatedBytes(actualVectorBytes));
        return new InstalledPackFiles(manifestFile, sqliteFile, vectorFile);
    }

    private static String manifestJsonWithFileSizes(long sqliteBytes, long vectorBytes) {
        return "{\n" +
            "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
            "  \"pack_version\": 2,\n" +
            "  \"generated_at\": \"2026-04-12T19:23:50Z\",\n" +
            "  \"counts\": {\n" +
            "    \"guides\": 754,\n" +
            "    \"chunks\": 49841,\n" +
            "    \"deterministic_rules\": 9,\n" +
            "    \"guide_related_links\": 5750,\n" +
            "    \"answer_cards\": 271\n" +
            "  },\n" +
            "  \"embedding\": {\n" +
            "    \"model_id\": \"nomic-ai/text-embedding-nomic-embed-text-v1.5\",\n" +
            "    \"dimension\": 768,\n" +
            "    \"vector_dtype\": \"float16\"\n" +
            "  },\n" +
            "  \"runtime_defaults\": {\n" +
            "    \"mobile_top_k\": 10\n" +
            "  },\n" +
            "  \"files\": {\n" +
            "    \"sqlite\": {\n" +
            "      \"bytes\": " + sqliteBytes + ",\n" +
            "      \"sha256\": \"abc\"\n" +
            "    },\n" +
            "    \"vectors\": {\n" +
            "      \"bytes\": " + vectorBytes + ",\n" +
            "      \"sha256\": \"def\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
    }

    private static byte[] repeatedBytes(int length) {
        byte[] bytes = new byte[length];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) (index % 251);
        }
        return bytes;
    }

    private static void expectVectorInfoRejected(
        PackManifest manifest,
        PackInstaller.VectorInfo vectorInfo,
        String expectedMessage
    ) throws Exception {
        try {
            PackInstaller.validateVectorInfoForTest(manifest, vectorInfo);
            fail("Expected vector info rejection containing: " + expectedMessage);
        } catch (IOException exc) {
            if (!exc.getMessage().contains(expectedMessage)) {
                throw exc;
            }
        }
    }

    private static final class InstalledPackFiles {
        final File manifestFile;
        final File sqliteFile;
        final File vectorFile;

        InstalledPackFiles(File manifestFile, File sqliteFile, File vectorFile) {
            this.manifestFile = manifestFile;
            this.sqliteFile = sqliteFile;
            this.vectorFile = vectorFile;
        }
    }
}

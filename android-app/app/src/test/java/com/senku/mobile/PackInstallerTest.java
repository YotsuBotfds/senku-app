package com.senku.mobile;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
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
            PackInstaller.missingRequiredPackTablesForTest(
                Set.of("guides", "chunks", "guide_related", "lexical_chunks_fts4")
            )
        );
    }

    @Test
    public void missingRequiredPackTablesDoesNotRequireOptionalAnswerCardTables() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(
                Set.of("guides", "chunks", "guide_related", "lexical_chunks_fts")
            )
        );
    }

    @Test
    public void missingRequiredPackTablesIgnoresFutureOptionalTables() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackTablesForTest(Set.of(
                "guides",
                "chunks",
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
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "chunks", "guide_related"))
        );
    }

    @Test
    public void missingRequiredPackTablesRequiresChunksTable() {
        assertEquals(
            "chunks",
            PackInstaller.missingRequiredPackTablesForTest(Set.of("guides", "guide_related", "lexical_chunks_fts4"))
        );
    }

    @Test
    public void missingRequiredPackColumnsAcceptsCurrentRuntimeSchemaColumns() {
        assertEquals(
            "",
            PackInstaller.missingRequiredPackColumnsForTest(currentRuntimeSchemaColumns("lexical_chunks_fts4"))
        );
    }

    @Test
    public void missingRequiredPackColumnsIgnoresOptionalAnswerCardSchemas() {
        Map<String, Set<String>> columns = new java.util.HashMap<>(currentRuntimeSchemaColumns("lexical_chunks_fts4"));
        columns.put("answer_cards", Set.of("future_answer_card_column"));
        columns.put("answer_card_clauses", Set.of("future_clause_column"));
        columns.put("answer_card_sources", Set.of("future_source_column"));

        assertEquals(
            "",
            PackInstaller.missingRequiredPackColumnsForTest(columns)
        );
    }

    @Test
    public void missingRequiredPackColumnsRejectsSchemaThatWouldCrashRepositoryQueries() {
        String missingColumns = PackInstaller.missingRequiredPackColumnsForTest(Map.of(
            "guides",
            Set.of("guide_id", "title"),
            "chunks",
            Set.of("chunk_id", "guide_id", "document"),
            "guide_related",
            Set.of("guide_id"),
            "lexical_chunks_fts4",
            Set.of("chunk_id", "search_text")
        ));

        assertEquals(
            "guides(category, difficulty, description, body_markdown, content_role, time_horizon, structure_type, topic_tags), " +
                "chunks(vector_row_id, guide_title, section_heading, category, content_role, time_horizon, structure_type, topic_tags), " +
                "guide_related(related_guide_id), " +
                "lexical_chunks_fts4(guide_title, guide_id, section_heading, category, content_role, time_horizon, structure_type, topic_tags)",
            missingColumns
        );
    }

    @Test
    public void missingRequiredPackColumnsRejectsIncompleteFts5Schema() {
        String missingColumns = PackInstaller.missingRequiredPackColumnsForTest(Map.of(
            "guides",
            Set.of(
                "guide_id",
                "title",
                "category",
                "difficulty",
                "description",
                "body_markdown",
                "content_role",
                "time_horizon",
                "structure_type",
                "topic_tags"
            ),
            "chunks",
            Set.of(
                "chunk_id",
                "vector_row_id",
                "guide_title",
                "guide_id",
                "section_heading",
                "category",
                "document",
                "content_role",
                "time_horizon",
                "structure_type",
                "topic_tags"
            ),
            "guide_related",
            Set.of("guide_id", "related_guide_id"),
            "lexical_chunks_fts",
            Set.of("chunk_id", "search_text")
        ));

        assertEquals(
            "lexical_chunks_fts(guide_title, guide_id, section_heading, category, content_role, time_horizon, structure_type, topic_tags)",
            missingColumns
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
    public void validateVectorInfoRejectsRowCountMismatch() throws Exception {
        expectVectorInfoRejected(
            manifestWithVector("float16", 768),
            vectorInfo("SNKUVEC1", 1, 32, 49840, 768, 1, 0),
            "row count mismatch"
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
        InstalledPackFiles files = installedPackFiles("2026-04-27T04:21:12.533181+00:00", 271, 123, 456, 123, 456);

        assertEquals(
            false,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenForcedEvenIfInstalledPackIsUsable() throws Exception {
        PackManifest manifest = manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271);

        assertEquals(
            true,
            PackInstallValidationPolicy.shouldInstallFromAssets(
                true,
                manifest,
                manifest
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
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                tempDir.resolve("senku_manifest.json").toFile(),
                tempDir.resolve("senku_mobile.sqlite3").toFile(),
                tempDir.resolve("senku_vectors.f16").toFile()
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledSqliteFileIsMissing() throws Exception {
        InstalledPackFiles files = installedPackFiles("2026-04-27T04:21:12.533181+00:00", 271, 123, 456, 123, 456);
        Files.delete(files.sqliteFile.toPath());

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledVectorFileIsMissing() throws Exception {
        InstalledPackFiles files = installedPackFiles("2026-04-27T04:21:12.533181+00:00", 271, 123, 456, 123, 456);
        Files.delete(files.vectorFile.toPath());

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledSizesDoNotMatchManifest() throws Exception {
        InstalledPackFiles files = installedPackFiles("2026-04-27T04:21:12.533181+00:00", 271, 123, 456, 122, 456);

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledSqliteChecksumDoesNotMatchManifest() throws Exception {
        Path tempDir = Files.createTempDirectory("pack-installer");
        File manifestFile = tempDir.resolve("senku_manifest.json").toFile();
        File sqliteFile = tempDir.resolve("senku_mobile.sqlite3").toFile();
        File vectorFile = tempDir.resolve("senku_vectors.f16").toFile();
        byte[] expectedSqlite = repeatedBytes(123);
        byte[] corruptSqlite = repeatedBytes(123);
        corruptSqlite[17] = (byte) (corruptSqlite[17] + 1);
        byte[] vector = vectorBytes(456, 768, 1);
        Files.write(
            manifestFile.toPath(),
            manifestJson(
                "2026-04-27T04:21:12.533181+00:00",
                271,
                expectedSqlite.length,
                vector.length,
                sha256Hex(expectedSqlite),
                sha256Hex(vector)
            ).getBytes(StandardCharsets.UTF_8)
        );
        Files.write(sqliteFile.toPath(), corruptSqlite);
        Files.write(vectorFile.toPath(), vector);

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                manifestFile,
                sqliteFile,
                vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledVectorChecksumDoesNotMatchManifest() throws Exception {
        Path tempDir = Files.createTempDirectory("pack-installer");
        File manifestFile = tempDir.resolve("senku_manifest.json").toFile();
        File sqliteFile = tempDir.resolve("senku_mobile.sqlite3").toFile();
        File vectorFile = tempDir.resolve("senku_vectors.f16").toFile();
        byte[] sqlite = repeatedBytes(123);
        byte[] expectedVector = vectorBytes(456, 768, 1);
        byte[] corruptVector = vectorBytes(456, 768, 1);
        corruptVector[40] = (byte) (corruptVector[40] + 1);
        Files.write(
            manifestFile.toPath(),
            manifestJson(
                "2026-04-27T04:21:12.533181+00:00",
                271,
                sqlite.length,
                expectedVector.length,
                sha256Hex(sqlite),
                sha256Hex(expectedVector)
            ).getBytes(StandardCharsets.UTF_8)
        );
        Files.write(sqliteFile.toPath(), sqlite);
        Files.write(vectorFile.toPath(), corruptVector);

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                manifestFile,
                sqliteFile,
                vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesWhenInstalledVectorHeaderDoesNotMatchManifest() throws Exception {
        Path tempDir = Files.createTempDirectory("pack-installer");
        File manifestFile = tempDir.resolve("senku_manifest.json").toFile();
        File sqliteFile = tempDir.resolve("senku_mobile.sqlite3").toFile();
        File vectorFile = tempDir.resolve("senku_vectors.f16").toFile();
        byte[] sqlite = repeatedBytes(123);
        byte[] staleVector = repeatedBytes(456);
        Files.write(
            manifestFile.toPath(),
            manifestJson(
                "2026-04-27T04:21:12.533181+00:00",
                271,
                sqlite.length,
                staleVector.length,
                sha256Hex(sqlite),
                ""
            ).getBytes(StandardCharsets.UTF_8)
        );
        Files.write(sqliteFile.toPath(), sqlite);
        Files.write(vectorFile.toPath(), staleVector);

        assertEquals(
            true,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                manifestFile,
                sqliteFile,
                vectorFile
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
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                manifestFile,
                sqliteFile,
                vectorFile
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsRefreshesOlderInstalledPackOnNormalInstall() throws Exception {
        assertEquals(
            true,
            PackInstallValidationPolicy.shouldInstallFromAssets(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                manifestWithGeneratedAtAndAnswerCards("2026-04-12T19:23:50Z", 6)
            )
        );
    }

    @Test
    public void shouldInstallFromAssetsPreservesNewerInstalledPackOnNormalInstall() throws Exception {
        InstalledPackFiles files = installedPackFiles("2026-05-01T00:00:00Z", 300, 123, 456, 123, 456);

        assertEquals(
            false,
            PackInstaller.shouldInstallFromAssetsForTest(
                false,
                manifestWithGeneratedAtAndAnswerCards("2026-04-27T04:21:12.533181+00:00", 271),
                files.manifestFile,
                files.sqliteFile,
                files.vectorFile
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

    private static Map<String, Set<String>> currentRuntimeSchemaColumns(String ftsTable) {
        return Map.of(
            "guides",
            Set.of(
                "guide_id",
                "title",
                "category",
                "difficulty",
                "description",
                "body_markdown",
                "content_role",
                "time_horizon",
                "structure_type",
                "topic_tags"
            ),
            "chunks",
            Set.of(
                "chunk_id",
                "vector_row_id",
                "guide_title",
                "guide_id",
                "section_heading",
                "category",
                "document",
                "content_role",
                "time_horizon",
                "structure_type",
                "topic_tags"
            ),
            "guide_related",
            Set.of("guide_id", "related_guide_id"),
            ftsTable,
            Set.of(
                "chunk_id",
                "search_text",
                "guide_title",
                "guide_id",
                "section_heading",
                "category",
                "content_role",
                "time_horizon",
                "structure_type",
                "topic_tags"
            )
        );
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

    private static PackManifest manifestWithGeneratedAtAndAnswerCards(String generatedAt, int answerCardCount) throws Exception {
        return PackManifest.fromJson(manifestJson(generatedAt, answerCardCount, 123, 456));
    }

    private static InstalledPackFiles installedPackFiles(
        String generatedAt,
        int answerCardCount,
        long manifestSqliteBytes,
        long manifestVectorBytes,
        int actualSqliteBytes,
        int actualVectorBytes
    ) throws Exception {
        Path tempDir = Files.createTempDirectory("pack-installer");
        File manifestFile = tempDir.resolve("senku_manifest.json").toFile();
        File sqliteFile = tempDir.resolve("senku_mobile.sqlite3").toFile();
        File vectorFile = tempDir.resolve("senku_vectors.f16").toFile();
        byte[] sqlite = repeatedBytes(actualSqliteBytes);
        byte[] vector = vectorBytes(actualVectorBytes, 768, 1);
        Files.write(
            manifestFile.toPath(),
            manifestJson(
                generatedAt,
                answerCardCount,
                manifestSqliteBytes,
                manifestVectorBytes,
                sha256Hex(sqlite),
                sha256Hex(vector)
            ).getBytes(StandardCharsets.UTF_8)
        );
        Files.write(sqliteFile.toPath(), sqlite);
        Files.write(vectorFile.toPath(), vector);
        return new InstalledPackFiles(manifestFile, sqliteFile, vectorFile);
    }

    private static String manifestJson(String generatedAt, int answerCardCount, long sqliteBytes, long vectorBytes) {
        return manifestJson(generatedAt, answerCardCount, sqliteBytes, vectorBytes, "abc", "def");
    }

    private static String manifestJson(
        String generatedAt,
        int answerCardCount,
        long sqliteBytes,
        long vectorBytes,
        String sqliteSha256,
        String vectorSha256
    ) {
        return "{\n" +
            "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
            "  \"pack_version\": 2,\n" +
            "  \"generated_at\": \"" + generatedAt + "\",\n" +
            "  \"counts\": {\n" +
            "    \"guides\": 754,\n" +
            "    \"chunks\": 49841,\n" +
            "    \"deterministic_rules\": 9,\n" +
            "    \"guide_related_links\": 5750,\n" +
            "    \"answer_cards\": " + answerCardCount + "\n" +
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
            "      \"sha256\": \"" + sqliteSha256 + "\"\n" +
            "    },\n" +
            "    \"vectors\": {\n" +
            "      \"bytes\": " + vectorBytes + ",\n" +
            "      \"sha256\": \"" + vectorSha256 + "\"\n" +
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

    private static byte[] vectorBytes(int length, int dimension, int dtypeCode) {
        byte[] bytes = repeatedBytes(length);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            .put("SNKUVEC1".getBytes(StandardCharsets.US_ASCII))
            .putInt(1)
            .putInt(32)
            .putInt(49841)
            .putInt(dimension)
            .putInt(dtypeCode)
            .putInt(0);
        return bytes;
    }

    private static String sha256Hex(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] expectedBytes = digest.digest(content);
        StringBuilder expected = new StringBuilder(expectedBytes.length * 2);
        for (byte b : expectedBytes) {
            expected.append(String.format("%02x", b));
        }
        return expected.toString();
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

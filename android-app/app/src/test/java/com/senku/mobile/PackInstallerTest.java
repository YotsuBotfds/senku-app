package com.senku.mobile;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
}

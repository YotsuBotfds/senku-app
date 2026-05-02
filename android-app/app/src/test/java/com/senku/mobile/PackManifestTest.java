package com.senku.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class PackManifestTest {
    private static final Path MOBILE_PACK_DIR = Path.of("src", "main", "assets", "mobile_pack");
    private static final Path MANIFEST_PATH = MOBILE_PACK_DIR.resolve("senku_manifest.json");

    @Test
    public void fromJsonParsesRequiredFields() throws Exception {
        PackManifest manifest = PackManifest.fromJson(
            "{\n" +
                "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
                "  \"pack_version\": 2,\n" +
                "  \"generated_at\": \"2026-04-12T19:23:50Z\",\n" +
                "  \"counts\": {\n" +
                "    \"guides\": 692,\n" +
                "    \"chunks\": 31528,\n" +
                "    \"deterministic_rules\": 96,\n" +
                "    \"guide_related_links\": 4626,\n" +
                "    \"answer_cards\": 6\n" +
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

        assertEquals("senku-mobile-pack-v2", manifest.packFormat);
        assertEquals(96, manifest.deterministicRuleCount);
        assertEquals(6, manifest.answerCardCount);
        assertEquals("abc", manifest.sqliteSha256);
        assertEquals("def", manifest.vectorSha256);
    }

    @Test
    public void bundledManifestLocksCheckedInCurrentHeadInventory() throws Exception {
        String jsonText = readManifest();
        PackManifest manifest = PackManifest.fromJson(jsonText);
        JSONObject root = new JSONObject(jsonText);
        JSONObject files = root.getJSONObject("files");
        JSONObject sqlite = files.getJSONObject("sqlite");
        JSONObject vectors = files.getJSONObject("vectors");

        assertEquals("senku-mobile-pack-v2", manifest.packFormat);
        assertEquals(2, manifest.packVersion);
        assertEquals("2026-04-27T04:21:12.533181+00:00", manifest.generatedAt);
        assertEquals(754, manifest.guideCount);
        assertEquals(49841, manifest.chunkCount);
        assertEquals(9, manifest.deterministicRuleCount);
        assertEquals(5750, manifest.relatedLinkCount);
        assertEquals(271, manifest.answerCardCount);
        assertEquals("nomic-ai/text-embedding-nomic-embed-text-v1.5", manifest.embeddingModelId);
        assertEquals(768, manifest.embeddingDimension);
        assertEquals("float16", manifest.vectorDtype);
        assertEquals(10, manifest.mobileTopK);

        assertEquals("senku_mobile.sqlite3", sqlite.getString("path"));
        assertEquals(290738176L, manifest.sqliteBytes);
        assertEquals(manifest.sqliteBytes, sqlite.getLong("bytes"));
        assertEquals("bca1dc3d6de3e8ecd4d2ac585b97e4914974cb6d6889443a313646f295d686c5", manifest.sqliteSha256);
        assertEquals(manifest.sqliteSha256, sqlite.getString("sha256"));

        assertEquals("senku_vectors.f16", vectors.getString("path"));
        assertEquals(76555808L, manifest.vectorBytes);
        assertEquals(manifest.vectorBytes, vectors.getLong("bytes"));
        assertEquals("5c4decacbf506b31acf8ae1d2568771be24004c46c96944456c8d33b7948eeb1", manifest.vectorSha256);
        assertEquals(manifest.vectorSha256, vectors.getString("sha256"));
    }

    @Test
    public void fromJsonDefaultsMissingAnswerCardCountForOldManifests() throws Exception {
        PackManifest manifest = PackManifest.fromJson(
            "{\n" +
                "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
                "  \"pack_version\": 2,\n" +
                "  \"generated_at\": \"2026-04-12T19:23:50Z\",\n" +
                "  \"counts\": {\n" +
                "    \"guides\": 692,\n" +
                "    \"chunks\": 31528,\n" +
                "    \"deterministic_rules\": 96,\n" +
                "    \"guide_related_links\": 4626\n" +
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

        assertEquals(0, manifest.answerCardCount);
    }

    @Test
    public void fromJsonAcceptsSupportedInt8VectorDtype() throws Exception {
        JSONObject root = new JSONObject(minimalManifestJson());
        root.getJSONObject("embedding").put("vector_dtype", "int8");

        PackManifest manifest = PackManifest.fromJson(root.toString());

        assertEquals("int8", manifest.vectorDtype);
    }

    @Test
    public void fromJsonIgnoresFutureOptionalFields() throws Exception {
        PackManifest manifest = PackManifest.fromJson(
            "{\n" +
                "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
                "  \"pack_version\": 3,\n" +
                "  \"generated_at\": \"2026-04-12T19:23:50Z\",\n" +
                "  \"counts\": {\n" +
                "    \"guides\": 754,\n" +
                "    \"chunks\": 49841,\n" +
                "    \"deterministic_rules\": 9,\n" +
                "    \"guide_related_links\": 5750,\n" +
                "    \"answer_cards\": 271,\n" +
                "    \"retrieval_metadata_guides\": 237,\n" +
                "    \"answer_card_clauses\": 6945,\n" +
                "    \"answer_card_sources\": 311,\n" +
                "    \"future_optional_count\": 42\n" +
                "  },\n" +
                "  \"embedding\": {\n" +
                "    \"model_id\": \"nomic-ai/text-embedding-nomic-embed-text-v1.5\",\n" +
                "    \"dimension\": 768,\n" +
                "    \"vector_dtype\": \"float16\",\n" +
                "    \"future_embedding_field\": \"ignored\"\n" +
                "  },\n" +
                "  \"runtime_defaults\": {\n" +
                "    \"mobile_top_k\": 10,\n" +
                "    \"future_runtime_toggle\": true\n" +
                "  },\n" +
                "  \"files\": {\n" +
                "    \"sqlite\": {\n" +
                "      \"path\": \"senku_mobile.sqlite3\",\n" +
                "      \"bytes\": 123,\n" +
                "      \"sha256\": \"abc\",\n" +
                "      \"compression\": \"none\",\n" +
                "      \"logical_rows\": 49841,\n" +
                "      \"future_file_field\": \"ignored\"\n" +
                "    },\n" +
                "    \"vectors\": {\n" +
                "      \"path\": \"senku_vectors.f16\",\n" +
                "      \"bytes\": 456,\n" +
                "      \"sha256\": \"def\",\n" +
                "      \"compression\": \"none\",\n" +
                "      \"logical_rows\": 49841,\n" +
                "      \"future_file_field\": \"ignored\"\n" +
                "    },\n" +
                "    \"answer_card_metadata\": {\n" +
                "      \"bytes\": 789,\n" +
                "      \"sha256\": \"future\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"schema\": {\n" +
                "    \"answer_cards\": {\n" +
                "      \"columns\": [\"card_id\", \"guide_id\"],\n" +
                "      \"metadata_only\": false,\n" +
                "      \"review_contract_version\": 2\n" +
                "    }\n" +
                "  },\n" +
                "  \"future_top_level\": {\n" +
                "    \"ignored\": true\n" +
                "  }\n" +
                "}"
        );

        assertEquals(3, manifest.packVersion);
        assertEquals(271, manifest.answerCardCount);
        assertEquals("abc", manifest.sqliteSha256);
        assertEquals("def", manifest.vectorSha256);
    }

    @Test(expected = JSONException.class)
    public void fromJsonRejectsMissingRequiredFields() throws Exception {
        PackManifest.fromJson(
            "{\n" +
                "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
                "  \"pack_version\": 2,\n" +
                "  \"generated_at\": \"2026-04-12T19:23:50Z\",\n" +
                "  \"counts\": {\n" +
                "    \"guides\": 692,\n" +
                "    \"chunks\": 31528,\n" +
                "    \"guide_related_links\": 4626\n" +
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

    @Test
    public void fromJsonRejectsMissingRequiredCountsIndividually() throws Exception {
        assertMissingNestedFieldRejected("counts", "guides");
        assertMissingNestedFieldRejected("counts", "chunks");
        assertMissingNestedFieldRejected("counts", "deterministic_rules");
        assertMissingNestedFieldRejected("counts", "guide_related_links");
    }

    @Test
    public void fromJsonRejectsMissingEmbeddingRuntimeAndFileMetadata() throws Exception {
        assertMissingNestedFieldRejected("embedding", "model_id");
        assertMissingNestedFieldRejected("embedding", "dimension");
        assertMissingNestedFieldRejected("embedding", "vector_dtype");
        assertMissingNestedFieldRejected("runtime_defaults", "mobile_top_k");
        assertMissingNestedFieldRejected("files", "sqlite");
        assertMissingNestedFieldRejected("files", "vectors");
        assertMissingNestedFileFieldRejected("sqlite", "bytes");
        assertMissingNestedFileFieldRejected("sqlite", "sha256");
        assertMissingNestedFileFieldRejected("vectors", "bytes");
        assertMissingNestedFileFieldRejected("vectors", "sha256");
    }

    @Test
    public void fromJsonRejectsInvalidSemanticBounds() throws Exception {
        assertInvalidManifestRejected("pack_format", root -> root.put("pack_format", "senku-mobile-pack-v1"));
        assertInvalidManifestRejected("pack_version", root -> root.put("pack_version", 0));
        assertInvalidManifestRejected("generated_at", root -> root.put("generated_at", "  "));
        assertInvalidManifestRejected("counts.guides", root -> root.getJSONObject("counts").put("guides", 0));
        assertInvalidManifestRejected("counts.chunks", root -> root.getJSONObject("counts").put("chunks", -1));
        assertInvalidManifestRejected(
            "counts.deterministic_rules",
            root -> root.getJSONObject("counts").put("deterministic_rules", -1)
        );
        assertInvalidManifestRejected(
            "counts.guide_related_links",
            root -> root.getJSONObject("counts").put("guide_related_links", -1)
        );
        assertInvalidManifestRejected(
            "counts.answer_cards",
            root -> root.getJSONObject("counts").put("answer_cards", -1)
        );
        assertInvalidManifestRejected(
            "embedding.model_id",
            root -> root.getJSONObject("embedding").put("model_id", "")
        );
        assertInvalidManifestRejected(
            "embedding.dimension",
            root -> root.getJSONObject("embedding").put("dimension", 0)
        );
        assertInvalidManifestRejected(
            "embedding.vector_dtype",
            root -> root.getJSONObject("embedding").put("vector_dtype", "float32")
        );
        assertInvalidManifestRejected(
            "runtime_defaults.mobile_top_k",
            root -> root.getJSONObject("runtime_defaults").put("mobile_top_k", 0)
        );
        assertInvalidManifestRejected(
            "files.sqlite.bytes",
            root -> root.getJSONObject("files").getJSONObject("sqlite").put("bytes", 0)
        );
        assertInvalidManifestRejected(
            "files.sqlite.sha256",
            root -> root.getJSONObject("files").getJSONObject("sqlite").put("sha256", "")
        );
        assertInvalidManifestRejected(
            "files.vectors.bytes",
            root -> root.getJSONObject("files").getJSONObject("vectors").put("bytes", -1)
        );
        assertInvalidManifestRejected(
            "files.vectors.sha256",
            root -> root.getJSONObject("files").getJSONObject("vectors").put("sha256", "  ")
        );
    }

    private static String readManifest() throws IOException {
        return new String(Files.readAllBytes(MANIFEST_PATH), StandardCharsets.UTF_8);
    }

    private static void assertInvalidManifestRejected(String fieldName, ManifestMutation mutation) throws Exception {
        JSONObject root = new JSONObject(minimalManifestJson());
        mutation.apply(root);
        assertManifestRejected(root, fieldName);
    }

    private static void assertMissingNestedFieldRejected(String objectName, String fieldName) throws Exception {
        JSONObject root = new JSONObject(minimalManifestJson());
        root.getJSONObject(objectName).remove(fieldName);
        assertManifestRejected(root, objectName + "." + fieldName);
    }

    private static void assertMissingNestedFileFieldRejected(String fileName, String fieldName) throws Exception {
        JSONObject root = new JSONObject(minimalManifestJson());
        root.getJSONObject("files").getJSONObject(fileName).remove(fieldName);
        assertManifestRejected(root, "files." + fileName + "." + fieldName);
    }

    private static void assertManifestRejected(JSONObject root, String missingField) throws Exception {
        try {
            PackManifest.fromJson(root.toString());
            fail("Expected manifest parse failure for missing " + missingField);
        } catch (JSONException expected) {
            // Expected: missing required manifest fields make the pack unusable.
        }
    }

    private interface ManifestMutation {
        void apply(JSONObject root) throws Exception;
    }

    private static String minimalManifestJson() {
        return "{\n" +
            "  \"pack_format\": \"senku-mobile-pack-v2\",\n" +
            "  \"pack_version\": 2,\n" +
            "  \"generated_at\": \"2026-04-12T19:23:50Z\",\n" +
            "  \"counts\": {\n" +
            "    \"guides\": 692,\n" +
            "    \"chunks\": 31528,\n" +
            "    \"deterministic_rules\": 96,\n" +
            "    \"guide_related_links\": 4626\n" +
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
            "      \"bytes\": 123,\n" +
            "      \"sha256\": \"abc\"\n" +
            "    },\n" +
            "    \"vectors\": {\n" +
            "      \"bytes\": 456,\n" +
            "      \"sha256\": \"def\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
    }
}

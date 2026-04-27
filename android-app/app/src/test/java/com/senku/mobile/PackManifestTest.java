package com.senku.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

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
    public void bundledManifestLocksCheckedInPilotInventory() throws Exception {
        String jsonText = readManifest();
        PackManifest manifest = PackManifest.fromJson(jsonText);
        JSONObject root = new JSONObject(jsonText);
        JSONObject files = root.getJSONObject("files");
        JSONObject sqlite = files.getJSONObject("sqlite");
        JSONObject vectors = files.getJSONObject("vectors");

        assertEquals("senku-mobile-pack-v2", manifest.packFormat);
        assertEquals(2, manifest.packVersion);
        assertEquals("2026-04-25T00:08:46.459832+00:00", manifest.generatedAt);
        assertEquals(754, manifest.guideCount);
        assertEquals(49726, manifest.chunkCount);
        assertEquals(9, manifest.deterministicRuleCount);
        assertEquals(5743, manifest.relatedLinkCount);
        assertEquals(6, manifest.answerCardCount);
        assertEquals("nomic-ai/text-embedding-nomic-embed-text-v1.5", manifest.embeddingModelId);
        assertEquals(768, manifest.embeddingDimension);
        assertEquals("float16", manifest.vectorDtype);
        assertEquals(10, manifest.mobileTopK);

        assertEquals("senku_mobile.sqlite3", sqlite.getString("path"));
        assertEquals(286695424L, manifest.sqliteBytes);
        assertEquals(manifest.sqliteBytes, sqlite.getLong("bytes"));
        assertEquals("bf2d8e616c2855c63ab52d72c537290ca2e022b967e975368059cce1a3366540", manifest.sqliteSha256);
        assertEquals(manifest.sqliteSha256, sqlite.getString("sha256"));

        assertEquals("senku_vectors.f16", vectors.getString("path"));
        assertEquals(76379168L, manifest.vectorBytes);
        assertEquals(manifest.vectorBytes, vectors.getLong("bytes"));
        assertEquals("893a7d5704603dd2dfb9645dd90c845e9ede61f4d1f27b5e7fe86bee5df81802", manifest.vectorSha256);
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

    private static String readManifest() throws IOException {
        return new String(Files.readAllBytes(MANIFEST_PATH), StandardCharsets.UTF_8);
    }
}

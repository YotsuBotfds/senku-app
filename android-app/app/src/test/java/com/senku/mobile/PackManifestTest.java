package com.senku.mobile;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class PackManifestTest {
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
}

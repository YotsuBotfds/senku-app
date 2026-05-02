package com.senku.mobile;

import org.json.JSONException;
import org.json.JSONObject;

public final class PackManifest {
    private static final String SUPPORTED_PACK_FORMAT = "senku-mobile-pack-v2";
    private static final String SUPPORTED_VECTOR_DTYPE = "float16";

    public final String packFormat;
    public final int packVersion;
    public final String generatedAt;
    public final int guideCount;
    public final int chunkCount;
    public final int deterministicRuleCount;
    public final int relatedLinkCount;
    public final int answerCardCount;
    public final String embeddingModelId;
    public final int embeddingDimension;
    public final String vectorDtype;
    public final int mobileTopK;
    public final long sqliteBytes;
    public final String sqliteSha256;
    public final long vectorBytes;
    public final String vectorSha256;

    private PackManifest(
        String packFormat,
        int packVersion,
        String generatedAt,
        int guideCount,
        int chunkCount,
        int deterministicRuleCount,
        int relatedLinkCount,
        int answerCardCount,
        String embeddingModelId,
        int embeddingDimension,
        String vectorDtype,
        int mobileTopK,
        long sqliteBytes,
        String sqliteSha256,
        long vectorBytes,
        String vectorSha256
    ) {
        this.packFormat = packFormat;
        this.packVersion = packVersion;
        this.generatedAt = generatedAt;
        this.guideCount = guideCount;
        this.chunkCount = chunkCount;
        this.deterministicRuleCount = deterministicRuleCount;
        this.relatedLinkCount = relatedLinkCount;
        this.answerCardCount = answerCardCount;
        this.embeddingModelId = embeddingModelId;
        this.embeddingDimension = embeddingDimension;
        this.vectorDtype = vectorDtype;
        this.mobileTopK = mobileTopK;
        this.sqliteBytes = sqliteBytes;
        this.sqliteSha256 = sqliteSha256;
        this.vectorBytes = vectorBytes;
        this.vectorSha256 = vectorSha256;
    }

    public static PackManifest fromJson(String jsonText) throws JSONException {
        JSONObject root = new JSONObject(jsonText);
        JSONObject counts = root.getJSONObject("counts");
        JSONObject embedding = root.getJSONObject("embedding");
        JSONObject runtimeDefaults = root.getJSONObject("runtime_defaults");
        JSONObject files = root.getJSONObject("files");
        JSONObject sqlite = files.getJSONObject("sqlite");
        JSONObject vectors = files.getJSONObject("vectors");

        PackManifest manifest = new PackManifest(
            requireSupportedValue("pack_format", root.getString("pack_format"), SUPPORTED_PACK_FORMAT),
            requirePositiveInt("pack_version", root.getInt("pack_version")),
            requireNonBlank("generated_at", root.getString("generated_at")),
            requirePositiveInt("counts.guides", counts.getInt("guides")),
            requirePositiveInt("counts.chunks", counts.getInt("chunks")),
            requireNonNegativeInt("counts.deterministic_rules", counts.getInt("deterministic_rules")),
            requireNonNegativeInt("counts.guide_related_links", counts.getInt("guide_related_links")),
            requireNonNegativeInt("counts.answer_cards", counts.optInt("answer_cards", 0)),
            requireNonBlank("embedding.model_id", embedding.getString("model_id")),
            requirePositiveInt("embedding.dimension", embedding.getInt("dimension")),
            requireSupportedValue("embedding.vector_dtype", embedding.getString("vector_dtype"), SUPPORTED_VECTOR_DTYPE),
            requirePositiveInt("runtime_defaults.mobile_top_k", runtimeDefaults.getInt("mobile_top_k")),
            requirePositiveLong("files.sqlite.bytes", sqlite.getLong("bytes")),
            requireNonBlank("files.sqlite.sha256", sqlite.getString("sha256")),
            requirePositiveLong("files.vectors.bytes", vectors.getLong("bytes")),
            requireNonBlank("files.vectors.sha256", vectors.getString("sha256"))
        );
        return manifest;
    }

    private static String requireNonBlank(String fieldName, String value) throws JSONException {
        if (value == null || value.trim().isEmpty()) {
            throw new JSONException("Invalid manifest field " + fieldName);
        }
        return value;
    }

    private static String requireSupportedValue(String fieldName, String value, String supportedValue)
        throws JSONException {
        String checked = requireNonBlank(fieldName, value);
        if (!supportedValue.equals(checked)) {
            throw new JSONException("Unsupported manifest field " + fieldName + ": " + checked);
        }
        return checked;
    }

    private static int requirePositiveInt(String fieldName, int value) throws JSONException {
        if (value <= 0) {
            throw new JSONException("Invalid manifest field " + fieldName);
        }
        return value;
    }

    private static int requireNonNegativeInt(String fieldName, int value) throws JSONException {
        if (value < 0) {
            throw new JSONException("Invalid manifest field " + fieldName);
        }
        return value;
    }

    private static long requirePositiveLong(String fieldName, long value) throws JSONException {
        if (value <= 0L) {
            throw new JSONException("Invalid manifest field " + fieldName);
        }
        return value;
    }
}

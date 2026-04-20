package com.senku.mobile;

import org.json.JSONException;
import org.json.JSONObject;

public final class PackManifest {
    public final String packFormat;
    public final int packVersion;
    public final String generatedAt;
    public final int guideCount;
    public final int chunkCount;
    public final int deterministicRuleCount;
    public final int relatedLinkCount;
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

        return new PackManifest(
            root.getString("pack_format"),
            root.getInt("pack_version"),
            root.getString("generated_at"),
            counts.getInt("guides"),
            counts.getInt("chunks"),
            counts.getInt("deterministic_rules"),
            counts.getInt("guide_related_links"),
            embedding.getString("model_id"),
            embedding.getInt("dimension"),
            embedding.getString("vector_dtype"),
            runtimeDefaults.getInt("mobile_top_k"),
            sqlite.getLong("bytes"),
            sqlite.getString("sha256"),
            vectors.getLong("bytes"),
            vectors.getString("sha256")
        );
    }
}

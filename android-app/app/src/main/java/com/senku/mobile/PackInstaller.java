package com.senku.mobile;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import org.json.JSONException;

import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PackInstaller {
    private static final String ASSET_DIR = "mobile_pack";
    private static final String MANIFEST_NAME = "senku_manifest.json";
    private static final String SQLITE_NAME = "senku_mobile.sqlite3";
    private static final String VECTOR_F16_NAME = "senku_vectors.f16";
    private static final String VECTOR_I8_NAME = "senku_vectors.i8";
    private static final String GUIDES_TABLE = "guides";
    private static final String GUIDE_RELATED_TABLE = "guide_related";
    private static final String FTS5_TABLE = "lexical_chunks_fts";
    private static final String FTS4_TABLE = "lexical_chunks_fts4";
    private static final int VECTOR_HEADER_BYTES = 32;
    private static final int VECTOR_DTYPE_FLOAT16 = 1;
    private static final int VECTOR_DTYPE_INT8 = 2;
    private static final int ASSET_OPEN_ATTEMPTS = 5;
    private static final long ASSET_OPEN_RETRY_MS = 300L;

    private PackInstaller() {
    }

    public static InstalledPack ensureInstalled(Context context, boolean force) throws IOException, JSONException {
        AssetManager assets = context.getAssets();
        String assetManifestText = readAssetText(assets, assetPath(MANIFEST_NAME));
        PackManifest assetManifest = PackManifest.fromJson(assetManifestText);
        File rootDir = new File(context.getFilesDir(), "mobile_pack");
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new IOException("Could not create install directory: " + rootDir.getAbsolutePath());
        }

        File manifestFile = new File(rootDir, MANIFEST_NAME);
        String vectorName = "int8".equals(assetManifest.vectorDtype) ? VECTOR_I8_NAME : VECTOR_F16_NAME;
        File sqliteFile = new File(rootDir, SQLITE_NAME);
        File vectorFile = new File(rootDir, vectorName);

        boolean installedFromAssets = shouldInstallFromAssets(
            force,
            assetManifest,
            manifestFile,
            sqliteFile,
            vectorFile,
            PackInstaller::validateInstalledSqliteSchema
        );
        if (installedFromAssets) {
            copyAsset(assets, assetPath(MANIFEST_NAME), manifestFile);
            copyAsset(assets, assetPath(SQLITE_NAME), sqliteFile);
            copyAsset(assets, assetPath(vectorName), vectorFile);
            validateInstalledFiles(assetManifest, sqliteFile, vectorFile);
        }

        String installedManifestText = readFileText(manifestFile);
        PackManifest installedManifest = PackManifest.fromJson(installedManifestText);
        VectorInfo vectorInfo = readVectorInfo(vectorFile);
        validateVectorInfo(installedManifest, vectorInfo);
        return new InstalledPack(rootDir, manifestFile, sqliteFile, vectorFile, installedManifest, vectorInfo);
    }

    private static PackManifest readUsableInstalledPackManifest(
        File manifestFile,
        File sqliteFile,
        File vectorFile,
        SqliteSchemaValidator sqliteSchemaValidator
    ) throws IOException, JSONException {
        if (!manifestFile.isFile() || !sqliteFile.isFile() || !vectorFile.isFile()) {
            return null;
        }
        PackManifest installed;
        try {
            installed = PackManifest.fromJson(readFileText(manifestFile));
            validateInstalledFiles(installed, sqliteFile, vectorFile, sqliteSchemaValidator);
            validateVectorInfo(installed, readVectorInfo(vectorFile));
        } catch (IOException | JSONException exc) {
            return null;
        }
        return installed;
    }

    private static void copyAsset(AssetManager assets, String assetPath, File destination) throws IOException {
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create asset directory: " + parent.getAbsolutePath());
        }
        try (InputStream input = openAssetWithRetry(assets, assetPath); FileOutputStream output = new FileOutputStream(destination, false)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.getFD().sync();
        }
    }

    private static String readAssetText(AssetManager assets, String assetPath) throws IOException {
        try (InputStream input = openAssetWithRetry(assets, assetPath)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static InputStream openAssetWithRetry(AssetManager assets, String assetPath) throws IOException {
        IOException lastError = null;
        for (int attempt = 1; attempt <= ASSET_OPEN_ATTEMPTS; attempt++) {
            try {
                return assets.open(assetPath);
            } catch (IOException exc) {
                lastError = exc;
                if (attempt >= ASSET_OPEN_ATTEMPTS) {
                    break;
                }
                try {
                    Thread.sleep(ASSET_OPEN_RETRY_MS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while opening asset: " + assetPath, interrupted);
                }
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        throw new IOException("Could not open asset: " + assetPath);
    }

    private static String readFileText(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void validateInstalledFiles(PackManifest manifest, File sqliteFile, File vectorFile) throws IOException {
        validateInstalledFiles(manifest, sqliteFile, vectorFile, PackInstaller::validateInstalledSqliteSchema);
    }

    private static void validateInstalledFiles(
        PackManifest manifest,
        File sqliteFile,
        File vectorFile,
        SqliteSchemaValidator sqliteSchemaValidator
    ) throws IOException {
        validateInstalledFile(sqliteFile, manifest.sqliteBytes, manifest.sqliteSha256, SQLITE_NAME);
        validateInstalledFile(vectorFile, manifest.vectorBytes, manifest.vectorSha256, vectorFile.getName());
        sqliteSchemaValidator.validate(sqliteFile);
    }

    private static void validateInstalledFile(File file, long expectedBytes, String expectedSha256, String label) throws IOException {
        if (!file.isFile()) {
            throw new IOException("Installed " + label + " file is missing: " + file.getAbsolutePath());
        }
        if (file.length() != expectedBytes) {
            throw new IOException(
                "Installed " + label + " size mismatch. Expected " + expectedBytes + " bytes but found " + file.length()
            );
        }
        String expected = expectedSha256 == null ? "" : expectedSha256.trim().toLowerCase();
        if (!expected.isEmpty()) {
            String actual = sha256Hex(file);
            if (!expected.equals(actual)) {
                throw new IOException(
                    "Installed " + label + " checksum mismatch. Expected " + expected + " but found " + actual
                );
            }
        }
    }

    static String sha256HexForTest(File file) throws IOException {
        return sha256Hex(file);
    }

    static String missingRequiredPackTablesForTest(Set<String> tableNames) {
        return missingRequiredPackTables(tableNames);
    }

    static void validateVectorInfoForTest(PackManifest manifest, VectorInfo vectorInfo) throws IOException {
        validateVectorInfo(manifest, vectorInfo);
    }

    static boolean shouldInstallFromAssetsForTest(
        boolean force,
        PackManifest assetManifest,
        File manifestFile,
        File sqliteFile,
        File vectorFile
    ) throws IOException, JSONException {
        PackManifest installedManifest = readUsableInstalledPackManifest(manifestFile, sqliteFile, vectorFile, file -> {
        });
        return PackInstallValidationPolicy.shouldInstallFromAssets(force, assetManifest, installedManifest);
    }

    private static boolean shouldInstallFromAssets(
        boolean force,
        PackManifest assetManifest,
        File manifestFile,
        File sqliteFile,
        File vectorFile,
        SqliteSchemaValidator sqliteSchemaValidator
    ) throws IOException, JSONException {
        PackManifest installedManifest = readUsableInstalledPackManifest(
            manifestFile,
            sqliteFile,
            vectorFile,
            sqliteSchemaValidator
        );
        return PackInstallValidationPolicy.shouldInstallFromAssets(force, assetManifest, installedManifest);
    }

    private static void validateVectorInfo(PackManifest manifest, VectorInfo vectorInfo) throws IOException {
        if (!"SNKUVEC1".equals(vectorInfo.magic)) {
            throw new IOException("Unsupported vector file magic");
        }
        if (vectorInfo.version != 1) {
            throw new IOException("Unsupported vector file version: " + vectorInfo.version);
        }
        if (vectorInfo.headerBytes != VECTOR_HEADER_BYTES) {
            throw new IOException("Unsupported vector header bytes: " + vectorInfo.headerBytes);
        }
        if (vectorInfo.rowCount != manifest.chunkCount) {
            throw new IOException(
                "Vector row count mismatch. Manifest expected " + manifest.chunkCount
                    + " but header found " + vectorInfo.rowCount
            );
        }
        if (vectorInfo.dimension != manifest.embeddingDimension) {
            throw new IOException(
                "Vector dimension mismatch. Manifest expected " + manifest.embeddingDimension
                    + " but header found " + vectorInfo.dimension
            );
        }
        int expectedDtypeCode = expectedVectorDtypeCode(manifest.vectorDtype);
        if (vectorInfo.dtypeCode != expectedDtypeCode) {
            throw new IOException(
                "Vector dtype mismatch. Manifest expected " + manifest.vectorDtype
                    + " but header found code " + vectorInfo.dtypeCode
            );
        }
    }

    private static int expectedVectorDtypeCode(String vectorDtype) throws IOException {
        if ("float16".equals(vectorDtype)) {
            return VECTOR_DTYPE_FLOAT16;
        }
        if ("int8".equals(vectorDtype)) {
            return VECTOR_DTYPE_INT8;
        }
        throw new IOException("Unsupported manifest vector dtype: " + vectorDtype);
    }

    private static void validateInstalledSqliteSchema(File sqliteFile) throws IOException {
        Set<String> tableNames = readInstalledTableNames(sqliteFile);
        String missingTables = missingRequiredPackTables(tableNames);
        if (!missingTables.isEmpty()) {
            throw new IOException(
                "Installed SQLite schema is missing required tables: " + missingTables
            );
        }
    }

    private static Set<String> readInstalledTableNames(File sqliteFile) throws IOException {
        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openDatabase(
                sqliteFile.getAbsolutePath(),
                null,
                SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS
            );
            LinkedHashSet<String> tables = new LinkedHashSet<>();
            try (Cursor cursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null
            )) {
                while (cursor.moveToNext()) {
                    String tableName = cursor.getString(0);
                    if (tableName != null && !tableName.trim().isEmpty()) {
                        tables.add(tableName.trim());
                    }
                }
            }
            return tables;
        } catch (SQLiteException exc) {
            throw new IOException("Installed SQLite schema could not be opened for validation", exc);
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    private static String missingRequiredPackTables(Set<String> tableNames) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (tableNames != null) {
            for (String tableName : tableNames) {
                if (tableName != null && !tableName.trim().isEmpty()) {
                    normalized.add(tableName.trim());
                }
            }
        }

        ArrayList<String> missing = new ArrayList<>();
        if (!normalized.contains(GUIDES_TABLE)) {
            missing.add(GUIDES_TABLE);
        }
        if (!normalized.contains(GUIDE_RELATED_TABLE)) {
            missing.add(GUIDE_RELATED_TABLE);
        }
        if (!normalized.contains(FTS5_TABLE) && !normalized.contains(FTS4_TABLE)) {
            missing.add(FTS5_TABLE + " or " + FTS4_TABLE);
        }
        return String.join(", ", missing);
    }

    private static String sha256Hex(File file) throws IOException {
        MessageDigest digest = newSha256Digest();
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return toHex(digest.digest());
    }

    private static MessageDigest newSha256Digest() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exc) {
            throw new IOException("SHA-256 digest is unavailable", exc);
        }
    }

    private static String toHex(byte[] value) {
        StringBuilder builder = new StringBuilder(value.length * 2);
        for (byte b : value) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static VectorInfo readVectorInfo(File vectorFile) throws IOException {
        byte[] header = new byte[VECTOR_HEADER_BYTES];
        try (FileInputStream input = new FileInputStream(vectorFile)) {
            int read = input.read(header);
            if (read != VECTOR_HEADER_BYTES) {
                throw new IOException("Vector file header truncated: " + vectorFile.getAbsolutePath());
            }
        }
        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        byte[] magic = new byte[8];
        buffer.get(magic);
        int version = buffer.getInt();
        int headerBytes = buffer.getInt();
        int rowCount = buffer.getInt();
        int dimension = buffer.getInt();
        int dtypeCode = buffer.getInt();
        int flags = buffer.getInt();
        return new VectorInfo(
            new String(magic, StandardCharsets.US_ASCII),
            version,
            headerBytes,
            rowCount,
            dimension,
            dtypeCode,
            flags
        );
    }

    private static String assetPath(String fileName) {
        return ASSET_DIR + "/" + fileName;
    }

    private interface SqliteSchemaValidator {
        void validate(File sqliteFile) throws IOException;
    }

    public static final class InstalledPack {
        public final File rootDir;
        public final File manifestFile;
        public final File databaseFile;
        public final File vectorFile;
        public final PackManifest manifest;
        public final VectorInfo vectorInfo;

        public InstalledPack(
            File rootDir,
            File manifestFile,
            File databaseFile,
            File vectorFile,
            PackManifest manifest,
            VectorInfo vectorInfo
        ) {
            this.rootDir = rootDir;
            this.manifestFile = manifestFile;
            this.databaseFile = databaseFile;
            this.vectorFile = vectorFile;
            this.manifest = manifest;
            this.vectorInfo = vectorInfo;
        }
    }

    public static final class VectorInfo {
        public final String magic;
        public final int version;
        public final int headerBytes;
        public final int rowCount;
        public final int dimension;
        public final int dtypeCode;
        public final int flags;

        public VectorInfo(String magic, int version, int headerBytes, int rowCount, int dimension, int dtypeCode, int flags) {
            this.magic = magic;
            this.version = version;
            this.headerBytes = headerBytes;
            this.rowCount = rowCount;
            this.dimension = dimension;
            this.dtypeCode = dtypeCode;
            this.flags = flags;
        }
    }
}

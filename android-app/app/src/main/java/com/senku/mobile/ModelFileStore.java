package com.senku.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Locale;

public final class ModelFileStore {
    private static final String PREFS_NAME = "senku_model_store";
    private static final String KEY_MODEL_NAME = "model_name";
    private static final String KEY_MODEL_PATH = "model_path";
    private static final String[] KNOWN_MODEL_FILE_NAMES = {
        "gemma-4-E4B-it.litertlm",
        "gemma-4-E4B-it.task",
        "gemma-4-E2B-it.litertlm",
        "gemma-4-E2B-it.task"
    };

    private ModelFileStore() {
    }

    public static File importModel(Context context, Uri uri) throws IOException {
        String displayName = queryDisplayName(context, uri);
        if (displayName.isEmpty()) {
            displayName = "offline-model.litertlm";
        }
        String sanitizedName = sanitizeFileName(displayName);
        File modelsDir = getPreferredModelsDir(context);
        if (!modelsDir.exists() && !modelsDir.mkdirs()) {
            throw new IOException("Unable to create model directory");
        }

        File outputFile = new File(modelsDir, sanitizedName);
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(outputFile)) {
            if (input == null) {
                throw new IOException("Unable to open selected model file");
            }
            byte[] buffer = new byte[1024 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String previousPath = prefs.getString(KEY_MODEL_PATH, "");
        prefs.edit()
            .putString(KEY_MODEL_NAME, sanitizedName)
            .putString(KEY_MODEL_PATH, outputFile.getAbsolutePath())
            .apply();

        if (!previousPath.isEmpty() && !previousPath.equals(outputFile.getAbsolutePath())) {
            File previousFile = new File(previousPath);
            if (previousFile.isFile()) {
                previousFile.delete();
            }
        }
        return outputFile;
    }

    public static File getImportedModelFile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String path = prefs.getString(KEY_MODEL_PATH, "");
        if (path == null || path.isEmpty()) {
            return findFallbackModel(context);
        }
        File file = new File(path);
        return file.isFile() ? file : findFallbackModel(context);
    }

    public static String getModelSummary(Context context) {
        File file = getImportedModelFile(context);
        if (file == null) {
            return "No imported model selected";
        }
        return file.getName() + " (" + humanReadableSize(file.length()) + ")";
    }

    private static String queryDisplayName(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (android.database.Cursor cursor =
                     context.getContentResolver().query(
                         uri,
                         new String[]{OpenableColumns.DISPLAY_NAME},
                         null,
                         null,
                         null
                     )) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        String name = cursor.getString(index);
                        return name == null ? "" : name;
                    }
                }
            }
        }
        String lastSegment = uri.getLastPathSegment();
        return lastSegment == null ? "" : lastSegment;
    }

    private static String sanitizeFileName(String name) {
        String cleaned = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.isEmpty() ? "offline-model.litertlm" : cleaned;
    }

    private static String humanReadableSize(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        }
        double size = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = -1;
        while (size >= 1024.0 && unitIndex < units.length - 1) {
            size /= 1024.0;
            unitIndex += 1;
        }
        return new DecimalFormat("0.0").format(size) + " " + units[Math.max(0, unitIndex)];
    }

    private static File findFallbackModel(Context context) {
        File internalDir = getPreferredModelsDir(context);
        File internalKnown = findKnownModelFile(internalDir);
        if (internalKnown != null) {
            return internalKnown;
        }
        File internalBest = findNewestModelFile(internalDir);
        if (internalBest != null) {
            return internalBest;
        }

        File externalRoot = context.getExternalFilesDir(null);
        if (externalRoot == null) {
            return null;
        }
        File externalDir = new File(externalRoot, "models");
        File externalKnown = findKnownModelFile(externalDir);
        if (externalKnown != null) {
            return externalKnown;
        }
        return findNewestModelFile(externalDir);
    }

    private static File getPreferredModelsDir(Context context) {
        return new File(context.getFilesDir(), "models");
    }

    private static File findNewestModelFile(File modelsDir) {
        File[] candidates = modelsDir.listFiles();
        if (candidates == null || candidates.length == 0) {
            return null;
        }

        File best = null;
        for (File candidate : candidates) {
            if (!candidate.isFile()) {
                continue;
            }
            String name = candidate.getName().toLowerCase(Locale.US);
            if (!name.endsWith(".litertlm") && !name.endsWith(".task")) {
                continue;
            }
            if (best == null || candidate.lastModified() > best.lastModified()) {
                best = candidate;
            }
        }
        return best;
    }

    private static File findKnownModelFile(File modelsDir) {
        for (String fileName : KNOWN_MODEL_FILE_NAMES) {
            File candidate = new File(modelsDir, fileName);
            if (candidate.isFile()) {
                return candidate;
            }
        }
        return null;
    }
}

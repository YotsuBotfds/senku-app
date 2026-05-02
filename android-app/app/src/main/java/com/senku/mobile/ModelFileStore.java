package com.senku.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ModelFileStore {
    private static final String PREFS_NAME = "senku_model_store";
    private static final String KEY_MODEL_NAME = "model_name";
    private static final String KEY_MODEL_PATH = "model_path";

    private ModelFileStore() {
    }

    public static File importModel(Context context, Uri uri) throws IOException {
        String displayName = queryDisplayName(context, uri);
        if (displayName.isEmpty()) {
            displayName = "offline-model.litertlm";
        }
        String sanitizedName = ModelFileStorePolicy.sanitizeFileName(displayName);
        if (!ModelFileStorePolicy.isSupportedModelFileName(sanitizedName)) {
            throw new IOException("Selected model must be a .litertlm or .task file");
        }
        File modelsDir = getPreferredModelsDir(context);
        if (!modelsDir.exists() && !modelsDir.mkdirs()) {
            throw new IOException("Unable to create model directory");
        }

        File outputFile = new File(modelsDir, sanitizedName);
        InputStream openedInput = context.getContentResolver().openInputStream(uri);
        if (openedInput == null) {
            throw new IOException("Unable to open selected model file");
        }
        try (InputStream input = openedInput;
             FileOutputStream output = new FileOutputStream(outputFile)) {
            try {
                byte[] buffer = new byte[1024 * 1024];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            } catch (IOException exc) {
                outputFile.delete();
                throw exc;
            }
        }
        if (outputFile.length() <= 0L) {
            outputFile.delete();
            throw new IOException("Selected model file is empty");
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
        if (ModelFileStorePolicy.isSupportedModelFile(file)) {
            return file;
        }
        clearImportedModelPrefs(prefs);
        return findFallbackModel(context);
    }

    public static String getModelSummary(Context context) {
        File file = getImportedModelFile(context);
        if (file == null) {
            return "No imported model selected";
        }
        return file.getName() + " (" + ModelFileStorePolicy.humanReadableSize(file.length()) + ")";
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

    private static void clearImportedModelPrefs(SharedPreferences prefs) {
        prefs.edit()
            .remove(KEY_MODEL_NAME)
            .remove(KEY_MODEL_PATH)
            .apply();
    }

    private static File findNewestModelFile(File modelsDir) {
        return ModelFileStorePolicy.findNewestModelFile(modelsDir.listFiles());
    }

    private static File findKnownModelFile(File modelsDir) {
        return ModelFileStorePolicy.findKnownModelFile(modelsDir);
    }
}

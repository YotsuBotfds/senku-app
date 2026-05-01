package com.senku.mobile;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;

final class ModelFileStorePolicy {
    private static final String[] KNOWN_MODEL_FILE_NAMES = {
        "gemma-4-E4B-it.litertlm",
        "gemma-4-E4B-it.task",
        "gemma-4-E2B-it.litertlm",
        "gemma-4-E2B-it.task"
    };

    private ModelFileStorePolicy() {
    }

    static String sanitizeFileName(String name) {
        String cleaned = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.isEmpty() ? "offline-model.litertlm" : cleaned;
    }

    static String humanReadableSize(long bytes) {
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

    static boolean isSupportedModelFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.US);
        return name.endsWith(".litertlm") || name.endsWith(".task");
    }

    static File findNewestModelFile(File[] candidates) {
        if (candidates == null || candidates.length == 0) {
            return null;
        }

        File best = null;
        for (File candidate : candidates) {
            if (!isSupportedModelFile(candidate)) {
                continue;
            }
            if (best == null || candidate.lastModified() > best.lastModified()) {
                best = candidate;
            }
        }
        return best;
    }

    static File findKnownModelFile(File modelsDir) {
        for (String fileName : KNOWN_MODEL_FILE_NAMES) {
            File candidate = new File(modelsDir, fileName);
            if (candidate.isFile()) {
                return candidate;
            }
        }
        return null;
    }
}

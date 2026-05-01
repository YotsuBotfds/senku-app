package com.senku.mobile;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

final class PackInstallValidationPolicy {
    private PackInstallValidationPolicy() {
    }

    static boolean shouldInstallFromAssets(
        boolean force,
        PackManifest assetManifest,
        PackManifest installedManifest
    ) {
        if (force) {
            return true;
        }
        if (installedManifest == null) {
            return true;
        }
        // Preserve same-or-newer hot-swapped/downloaded packs, but let app upgrades promote
        // a newer bundled pack over an older private copy.
        return assetManifestIsNewer(assetManifest, installedManifest);
    }

    private static boolean assetManifestIsNewer(PackManifest assetManifest, PackManifest installedManifest) {
        if (assetManifest == null || installedManifest == null) {
            return false;
        }
        if (assetManifest.packVersion != installedManifest.packVersion) {
            return assetManifest.packVersion > installedManifest.packVersion;
        }
        int generatedAtCompare = compareGeneratedAt(assetManifest.generatedAt, installedManifest.generatedAt);
        if (generatedAtCompare != 0) {
            return generatedAtCompare > 0;
        }
        return assetManifest.answerCardCount > installedManifest.answerCardCount;
    }

    private static int compareGeneratedAt(String left, String right) {
        try {
            return OffsetDateTime.parse(left).toInstant().compareTo(OffsetDateTime.parse(right).toInstant());
        } catch (DateTimeParseException exc) {
            if (left == null && right == null) {
                return 0;
            }
            if (left == null) {
                return -1;
            }
            if (right == null) {
                return 1;
            }
            return left.compareTo(right);
        }
    }
}

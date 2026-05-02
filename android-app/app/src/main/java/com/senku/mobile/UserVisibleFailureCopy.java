package com.senku.mobile;

final class UserVisibleFailureCopy {
    private UserVisibleFailureCopy() {
    }

    static String manualInstallFailureDetails(Throwable ignored) {
        return "Manual install failed. Try reinstalling the offline pack.";
    }

    static String manualLoadFailureDetails(Throwable ignored) {
        return "Manual load failed. Try opening the manual again.";
    }

    static String searchFailureDetails(Throwable ignored) {
        return "Search failed. Try the query again or return to Library.";
    }

    static String lastErrorSummary(Throwable ignored) {
        return "Last error: details hidden to protect local paths. Try again.";
    }

    static String generationFailureStatus(Throwable ignored) {
        return "Offline answer failed";
    }

    static String generationFailureBody(Throwable ignored) {
        return "Could not finish generation. Review the source guides and try again.";
    }
}

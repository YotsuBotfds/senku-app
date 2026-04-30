package com.senku.mobile;

import android.content.Context;
import android.content.pm.ApplicationInfo;

final class DeveloperToolsPolicy {
    private DeveloperToolsPolicy() {
    }

    static boolean shouldShowDeveloperToolsPanel(
        boolean debugBuild,
        boolean productReviewMode,
        boolean browseMode,
        boolean hasResults
    ) {
        return debugBuild && !productReviewMode && browseMode;
    }

    static boolean isDebuggableBuild(Context context) {
        ApplicationInfo info = context == null ? null : context.getApplicationInfo();
        return info != null && (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}

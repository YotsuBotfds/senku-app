package com.senku.mobile;

public final class DetailActionBlockRenderPolicy {
    public enum Decision {
        NONE,
        EMERGENCY_PORTRAIT,
        HIGH_RISK
    }

    private DetailActionBlockRenderPolicy() {
    }

    public static Decision evaluate(
        boolean emergencyPortraitSurface,
        boolean answerMode,
        boolean highRiskRoute,
        boolean deterministicRoute,
        String body
    ) {
        if (emergencyPortraitSurface) {
            return Decision.EMERGENCY_PORTRAIT;
        }
        if (answerMode && highRiskRoute && deterministicRoute && !safe(body).trim().isEmpty()) {
            return Decision.HIGH_RISK;
        }
        return Decision.NONE;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

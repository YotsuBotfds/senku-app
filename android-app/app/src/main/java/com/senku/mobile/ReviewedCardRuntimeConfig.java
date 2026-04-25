package com.senku.mobile;

import android.content.Context;
import android.content.SharedPreferences;

public final class ReviewedCardRuntimeConfig {
    private static final String PREFS_NAME = "senku_answer_card_runtime";
    private static final String KEY_ENABLED = "reviewed_card_runtime_enabled";

    private ReviewedCardRuntimeConfig() {
    }

    public static boolean isEnabled(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
}

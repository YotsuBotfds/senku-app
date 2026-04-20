package com.senku.mobile;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class PinnedGuideStore {
    private static final String PREFS_NAME = "senku_pinned_guides";
    private static final String KEY_GUIDE_IDS = "guide_ids";
    private static final int MAX_PINS = 12;

    private PinnedGuideStore() {
    }

    public static List<String> listGuideIds(Context context) {
        SharedPreferences preferences = preferences(context);
        String stored = safe(preferences.getString(KEY_GUIDE_IDS, ""));
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String rawValue : stored.split(",")) {
            String guideId = normalizeGuideId(rawValue);
            if (!guideId.isEmpty()) {
                ordered.add(guideId);
            }
        }
        return new ArrayList<>(ordered);
    }

    public static boolean contains(Context context, String rawGuideId) {
        String guideId = normalizeGuideId(rawGuideId);
        if (guideId.isEmpty()) {
            return false;
        }
        for (String savedGuideId : listGuideIds(context)) {
            if (savedGuideId.equals(guideId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean add(Context context, String rawGuideId) {
        String guideId = normalizeGuideId(rawGuideId);
        if (guideId.isEmpty()) {
            return false;
        }
        ArrayList<String> guideIds = new ArrayList<>(listGuideIds(context));
        guideIds.remove(guideId);
        guideIds.add(0, guideId);
        while (guideIds.size() > MAX_PINS) {
            guideIds.remove(guideIds.size() - 1);
        }
        save(context, guideIds);
        return true;
    }

    public static boolean remove(Context context, String rawGuideId) {
        String guideId = normalizeGuideId(rawGuideId);
        if (guideId.isEmpty()) {
            return false;
        }
        ArrayList<String> guideIds = new ArrayList<>(listGuideIds(context));
        boolean removed = guideIds.remove(guideId);
        if (removed) {
            save(context, guideIds);
        }
        return removed;
    }

    private static void save(Context context, List<String> guideIds) {
        ArrayList<String> cleaned = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String rawGuideId : guideIds) {
            String guideId = normalizeGuideId(rawGuideId);
            if (!guideId.isEmpty() && seen.add(guideId)) {
                cleaned.add(guideId);
            }
            if (cleaned.size() >= MAX_PINS) {
                break;
            }
        }
        preferences(context).edit().putString(KEY_GUIDE_IDS, String.join(",", cleaned)).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String normalizeGuideId(String rawGuideId) {
        return safe(rawGuideId).trim().toUpperCase(Locale.US);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

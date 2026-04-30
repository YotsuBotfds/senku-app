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
        return guideIdsFromStoredValue(stored);
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
        save(context, guideIdsAfterAdd(listGuideIds(context), guideId));
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
        preferences(context).edit().putString(KEY_GUIDE_IDS, storedValueForGuideIds(guideIds)).apply();
    }

    static void clearForTest(Context context) {
        clear(preferences(context));
    }

    static List<String> guideIdsFromStoredValueForTest(String storedValue) {
        return guideIdsFromStoredValue(storedValue);
    }

    static List<String> guideIdsAfterAddForTest(List<String> currentGuideIds, String rawGuideId) {
        return guideIdsAfterAdd(currentGuideIds, rawGuideId);
    }

    static String storedValueForGuideIdsForTest(List<String> guideIds) {
        return storedValueForGuideIds(guideIds);
    }

    static void clearPreferencesForTest(SharedPreferences preferences) {
        clear(preferences);
    }

    private static void clear(SharedPreferences preferences) {
        preferences.edit().clear().commit();
    }

    private static List<String> guideIdsFromStoredValue(String storedValue) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String rawValue : safe(storedValue).split(",")) {
            String guideId = normalizeGuideId(rawValue);
            if (!guideId.isEmpty()) {
                ordered.add(guideId);
            }
        }
        return new ArrayList<>(ordered);
    }

    private static List<String> guideIdsAfterAdd(List<String> currentGuideIds, String rawGuideId) {
        String guideId = normalizeGuideId(rawGuideId);
        ArrayList<String> guideIds = new ArrayList<>(cleanGuideIds(currentGuideIds));
        if (guideId.isEmpty()) {
            return guideIds;
        }
        guideIds.remove(guideId);
        guideIds.add(0, guideId);
        while (guideIds.size() > MAX_PINS) {
            guideIds.remove(guideIds.size() - 1);
        }
        return guideIds;
    }

    private static String storedValueForGuideIds(List<String> guideIds) {
        return String.join(",", cleanGuideIds(guideIds));
    }

    private static ArrayList<String> cleanGuideIds(List<String> guideIds) {
        ArrayList<String> cleaned = new ArrayList<>();
        if (guideIds == null) {
            return cleaned;
        }
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
        return cleaned;
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

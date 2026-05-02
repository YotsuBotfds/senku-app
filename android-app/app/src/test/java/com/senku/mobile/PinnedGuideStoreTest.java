package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public final class PinnedGuideStoreTest {
    @Test
    public void storedGuideIdsNormalizeDedupeAndKeepOrder() {
        assertEquals(
            List.of("GD-220", "GD-132", "GD-345"),
            PinnedGuideStore.guideIdsFromStoredValueForTest(" gd-220,GD-132,,gd-220, gd-345 ")
        );
    }

    @Test
    public void addingGuideMovesItToFrontAndCapsSavedGuides() {
        ArrayList<String> current = new ArrayList<>();
        for (int index = 1; index <= 13; index++) {
            current.add(String.format("GD-%03d", index));
        }

        List<String> guideIds = PinnedGuideStore.guideIdsAfterAddForTest(current, " gd-005 ");

        assertEquals(12, guideIds.size());
        assertEquals("GD-005", guideIds.get(0));
        assertEquals("GD-001", guideIds.get(1));
        assertEquals("GD-012", guideIds.get(11));
    }

    @Test
    public void storedValueCleansInvalidDuplicateAndOverflowGuideIds() {
        ArrayList<String> guideIds = new ArrayList<>();
        guideIds.add(" ");
        guideIds.add("gd-220");
        guideIds.add("GD-132");
        guideIds.add("gd-220");
        for (int index = 1; index <= 12; index++) {
            guideIds.add(String.format("gd-%03d", index));
        }

        assertEquals(
            "GD-220,GD-132,GD-001,GD-002,GD-003,GD-004,GD-005,GD-006,GD-007,GD-008,GD-009,GD-010",
            PinnedGuideStore.storedValueForGuideIdsForTest(guideIds)
        );
    }

    @Test
    public void addCleansExistingOverflowAndKeepsNewestFirstUniquePins() {
        ArrayList<String> current = new ArrayList<>();
        current.add(" gd-003 ");
        current.add("GD-002");
        current.add("gd-003");
        current.add(" ");
        for (int index = 1; index <= 12; index++) {
            current.add(String.format("gd-%03d", index));
        }

        List<String> guideIds = PinnedGuideStore.guideIdsAfterAddForTest(current, " gd-013 ");

        assertEquals(
            List.of(
                "GD-013",
                "GD-003",
                "GD-002",
                "GD-001",
                "GD-004",
                "GD-005",
                "GD-006",
                "GD-007",
                "GD-008",
                "GD-009",
                "GD-010",
                "GD-011"
            ),
            guideIds
        );
    }

    @Test
    public void publicStoreApiNormalizesResavesAndRemovesPinnedGuides() {
        TestContext context = new TestContext();

        assertTrue(PinnedGuideStore.add(context, " gd-220 "));
        assertTrue(PinnedGuideStore.add(context, "gd-132"));
        assertTrue(PinnedGuideStore.add(context, "GD-220"));

        assertEquals(List.of("GD-220", "GD-132"), PinnedGuideStore.listGuideIds(context));
        assertTrue(PinnedGuideStore.contains(context, "gd-132"));
        assertTrue(PinnedGuideStore.remove(context, " gd-132 "));
        assertFalse(PinnedGuideStore.contains(context, "GD-132"));
        assertEquals(List.of("GD-220"), PinnedGuideStore.listGuideIds(context));
    }

    @Test
    public void publicStoreApiRecoversFromWrongTypedPersistedGuideList() {
        TestContext context = new TestContext();
        context.getSharedPreferences("senku_pinned_guides", Context.MODE_PRIVATE)
            .edit()
            .putInt("guide_ids", 7)
            .commit();

        assertTrue(PinnedGuideStore.listGuideIds(context).isEmpty());
        assertFalse(PinnedGuideStore.contains(context, "GD-220"));
        assertFalse(PinnedGuideStore.remove(context, "GD-220"));

        assertTrue(PinnedGuideStore.add(context, "gd-220"));
        assertEquals(List.of("GD-220"), PinnedGuideStore.listGuideIds(context));
    }

    @Test
    public void clearPreferencesForTestRemovesStoredGuideIds() {
        InMemorySharedPreferences preferences = new InMemorySharedPreferences();
        preferences.edit().putString(
            "ignored_test_key",
            PinnedGuideStore.storedValueForGuideIdsForTest(List.of("gd-220", "gd-132"))
        ).commit();

        assertEquals("GD-220,GD-132", preferences.getString("ignored_test_key", ""));

        PinnedGuideStore.clearPreferencesForTest(preferences);

        assertEquals("", preferences.getString("ignored_test_key", ""));
    }

    private static final class TestContext extends ContextWrapper {
        private final Map<String, SharedPreferences> prefs = new HashMap<>();

        TestContext() {
            super(null);
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            SharedPreferences existing = prefs.get(name);
            if (existing != null) {
                return existing;
            }
            SharedPreferences created = new InMemorySharedPreferences();
            prefs.put(name, created);
            return created;
        }
    }

    private static final class InMemorySharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Map<String, ?> getAll() {
            return new HashMap<>(values);
        }

        @Override
        public String getString(String key, String defValue) {
            Object value = values.get(key);
            return value instanceof String ? (String) value : defValue;
        }

        @Override
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Object value = values.get(key);
            return value instanceof Set ? (Set<String>) value : defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            Object value = values.get(key);
            return value instanceof Integer ? (Integer) value : defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            Object value = values.get(key);
            return value instanceof Long ? (Long) value : defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            Object value = values.get(key);
            return value instanceof Float ? (Float) value : defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object value = values.get(key);
            return value instanceof Boolean ? (Boolean) value : defValue;
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public Editor edit() {
            return new InMemoryEditor();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        private final class InMemoryEditor implements Editor {
            private final Map<String, Object> pendingValues = new HashMap<>();
            private final Set<String> pendingRemovals = new java.util.HashSet<>();
            private boolean clearRequested;

            @Override
            public Editor putString(String key, String value) {
                pendingValues.put(key, value);
                pendingRemovals.remove(key);
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> values) {
                pendingValues.put(key, values);
                pendingRemovals.remove(key);
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                pendingValues.put(key, value);
                pendingRemovals.remove(key);
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                pendingValues.put(key, value);
                pendingRemovals.remove(key);
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                pendingValues.put(key, value);
                pendingRemovals.remove(key);
                return this;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                pendingValues.put(key, value);
                pendingRemovals.remove(key);
                return this;
            }

            @Override
            public Editor remove(String key) {
                pendingRemovals.add(key);
                pendingValues.remove(key);
                return this;
            }

            @Override
            public Editor clear() {
                clearRequested = true;
                pendingValues.clear();
                pendingRemovals.clear();
                return this;
            }

            @Override
            public boolean commit() {
                if (clearRequested) {
                    values.clear();
                }
                for (String key : pendingRemovals) {
                    values.remove(key);
                }
                values.putAll(pendingValues);
                return true;
            }

            @Override
            public void apply() {
                commit();
            }
        }
    }
}

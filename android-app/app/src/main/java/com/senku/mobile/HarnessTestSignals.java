package com.senku.mobile;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class HarnessTestSignals {
    public interface Listener {
        void onBusyStateChanged(boolean idle, @NonNull String snapshot);
    }

    private static final AtomicInteger NEXT_TOKEN = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, String> ACTIVE = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private HarnessTestSignals() {
    }

    public static int begin(String label) {
        int token = NEXT_TOKEN.getAndIncrement();
        ACTIVE.put(token, safe(label));
        notifyListeners();
        return token;
    }

    public static void end(int token) {
        if (token <= 0) {
            return;
        }
        if (ACTIVE.remove(token) != null) {
            notifyListeners();
        }
    }

    public static boolean isIdle() {
        return ACTIVE.isEmpty();
    }

    public static int busyCount() {
        return ACTIVE.size();
    }

    @NonNull
    public static String snapshot() {
        if (ACTIVE.isEmpty()) {
            return "idle";
        }
        List<String> labels = new ArrayList<>(ACTIVE.values());
        Collections.sort(labels);
        return String.format(Locale.US, "busy[%d]: %s", labels.size(), String.join(", ", labels));
    }

    public static void addListener(Listener listener) {
        if (listener == null) {
            return;
        }
        LISTENERS.addIfAbsent(listener);
        listener.onBusyStateChanged(isIdle(), snapshot());
    }

    public static void removeListener(Listener listener) {
        if (listener == null) {
            return;
        }
        LISTENERS.remove(listener);
    }

    private static void notifyListeners() {
        boolean idle = isIdle();
        String snapshot = snapshot();
        for (Listener listener : LISTENERS) {
            listener.onBusyStateChanged(idle, snapshot);
        }
    }

    @NonNull
    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

package com.senku.mobile;

final class LatestJobGate {
    private final Object lock = new Object();
    private long currentJobToken;

    long nextJobToken() {
        synchronized (lock) {
            currentJobToken += 1;
            return currentJobToken;
        }
    }

    boolean isCurrentJob(long jobToken) {
        synchronized (lock) {
            return currentJobToken == jobToken;
        }
    }
}

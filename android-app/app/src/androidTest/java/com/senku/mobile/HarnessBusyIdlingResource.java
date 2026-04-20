package com.senku.mobile;

import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingResource;

public final class HarnessBusyIdlingResource implements IdlingResource, HarnessTestSignals.Listener {
    private volatile ResourceCallback callback;

    @NonNull
    @Override
    public String getName() {
        return "SenkuHarnessBusyIdlingResource";
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = HarnessTestSignals.isIdle();
        if (idle && callback != null) {
            callback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        callback = resourceCallback;
    }

    @Override
    public void onBusyStateChanged(boolean idle, @NonNull String snapshot) {
        if (idle && callback != null) {
            callback.onTransitionToIdle();
        }
    }

    public void startListening() {
        HarnessTestSignals.addListener(this);
    }

    public void stopListening() {
        HarnessTestSignals.removeListener(this);
    }
}

package com.senku.mobile;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public final class DeveloperPanelRuntimeToggleTest {
    private static final String REMOTE_ARTIFACT_DIR = "/sdcard/Download/senku-developer-panel";

    private Context context;
    private UiDevice device;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        executeShell("mkdir -p " + REMOTE_ARTIFACT_DIR);
        ReviewedCardRuntimeConfig.setEnabled(context, false);
    }

    @After
    public void tearDown() {
        ReviewedCardRuntimeConfig.setEnabled(context, false);
    }

    @Test
    public void developerPanelToggleControlsReviewedCardRuntimePreference() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitUntilEnabled(scenario, R.id.developer_toggle_button);

            scenario.onActivity(activity -> {
                View developerContent = activity.findViewById(R.id.developer_content);
                Button developerToggle = activity.findViewById(R.id.developer_toggle_button);
                Button runtimeToggle = activity.findViewById(R.id.reviewed_card_runtime_button);

                Assert.assertNotNull("developer content should exist", developerContent);
                Assert.assertNotNull("developer toggle should exist", developerToggle);
                Assert.assertNotNull("reviewed-card runtime toggle should exist", runtimeToggle);
                Assert.assertEquals(View.GONE, developerContent.getVisibility());
                Assert.assertFalse(ReviewedCardRuntimeConfig.isEnabled(activity));
                Assert.assertEquals(
                    activity.getString(R.string.reviewed_card_runtime_off_button),
                    runtimeToggle.getText().toString()
                );

            });

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            captureUiState("developer_panel_collapsed");

            scenario.onActivity(activity -> {
                View developerContent = activity.findViewById(R.id.developer_content);
                Button developerToggle = activity.findViewById(R.id.developer_toggle_button);
                developerToggle.performClick();
                Assert.assertEquals(View.VISIBLE, developerContent.getVisibility());
            });

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(150L);
            scenario.onActivity(activity -> scrollRuntimeToggleIntoView(
                activity,
                activity.findViewById(R.id.reviewed_card_runtime_button)
            ));
            captureUiState("developer_panel_expanded_runtime_off");

            scenario.onActivity(activity -> {
                Button runtimeToggle = activity.findViewById(R.id.reviewed_card_runtime_button);
                runtimeToggle.performClick();
                scrollRuntimeToggleIntoView(activity, runtimeToggle);
                Assert.assertTrue(ReviewedCardRuntimeConfig.isEnabled(activity));
                Assert.assertEquals(
                    activity.getString(R.string.reviewed_card_runtime_on_button),
                    runtimeToggle.getText().toString()
                );
            });

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            captureUiState("reviewed_card_runtime_on");

            scenario.onActivity(activity -> {
                Button runtimeToggle = activity.findViewById(R.id.reviewed_card_runtime_button);
                runtimeToggle.performClick();
                Assert.assertFalse(ReviewedCardRuntimeConfig.isEnabled(activity));
                Assert.assertEquals(
                    activity.getString(R.string.reviewed_card_runtime_off_button),
                    runtimeToggle.getText().toString()
                );
            });
        }
    }

    private void captureUiState(String label) {
        String safeLabel = label.replaceAll("[^a-zA-Z0-9._-]+", "_");
        String screenshotOutput = REMOTE_ARTIFACT_DIR + "/developer_panel_runtime_toggle__" + safeLabel + ".png";
        String dumpOutput = REMOTE_ARTIFACT_DIR + "/developer_panel_runtime_toggle__" + safeLabel + ".xml";
        device.waitForIdle();
        executeShell("screencap -p " + screenshotOutput);
        executeShell("uiautomator dump " + dumpOutput);
        Assert.assertTrue(
            "developer-panel screenshot should be captured",
            executeShell("ls -l " + screenshotOutput).contains("developer_panel_runtime_toggle")
        );
    }

    private String executeShell(String command) {
        try {
            return device.executeShellCommand(command);
        } catch (Exception exc) {
            throw new AssertionError("Shell command failed: " + command, exc);
        }
    }

    private static void scrollRuntimeToggleIntoView(MainActivity activity, View runtimeToggle) {
        View developerScroll = activity.findViewById(R.id.developer_scroll_view);
        if (developerScroll instanceof ScrollView && containsView(developerScroll, runtimeToggle)) {
            scrollChildNearMiddle((ScrollView) developerScroll, runtimeToggle);
        }
        View browseScroll = activity.findViewById(R.id.browse_scroll_view);
        if (browseScroll instanceof ScrollView && containsView(browseScroll, runtimeToggle)) {
            scrollChildNearMiddle((ScrollView) browseScroll, runtimeToggle);
        }
    }

    private static void scrollChildNearMiddle(ScrollView scrollView, View child) {
        int childTop = 0;
        View cursor = child;
        while (cursor != null && cursor != scrollView) {
            childTop += cursor.getTop();
            if (!(cursor.getParent() instanceof View)) {
                break;
            }
            cursor = (View) cursor.getParent();
        }
        int targetY = Math.max(0, childTop - (scrollView.getHeight() / 2));
        scrollView.scrollTo(0, targetY);
    }

    private static boolean containsView(View ancestor, View child) {
        View cursor = child;
        while (cursor != null) {
            if (cursor == ancestor) {
                return true;
            }
            if (!(cursor.getParent() instanceof View)) {
                return false;
            }
            cursor = (View) cursor.getParent();
        }
        return false;
    }

    private static void waitUntilEnabled(ActivityScenario<MainActivity> scenario, int viewId) {
        long deadline = SystemClock.uptimeMillis() + 15_000L;
        while (SystemClock.uptimeMillis() < deadline) {
            AtomicBoolean enabled = new AtomicBoolean(false);
            scenario.onActivity(activity -> {
                View view = activity.findViewById(viewId);
                enabled.set(view != null && view.isEnabled());
            });
            if (enabled.get()) {
                return;
            }
            SystemClock.sleep(100L);
        }
        Assert.fail("Timed out waiting for view to become enabled: " + viewId);
    }
}

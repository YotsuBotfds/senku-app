package com.senku.mobile;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

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

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public final class DeveloperPanelRuntimeToggleTest {
    private Context context;
    private UiDevice device;
    private File artifactDir;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        artifactDir = new File(context.getFilesDir(), "test-artifacts");
        if (!artifactDir.exists()) {
            artifactDir.mkdirs();
        }
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
                TextView supportText = activity.findViewById(R.id.reviewed_card_runtime_support_text);

                Assert.assertNotNull("developer content should exist", developerContent);
                Assert.assertNotNull("developer toggle should exist", developerToggle);
                Assert.assertNotNull("reviewed-card runtime toggle should exist", runtimeToggle);
                Assert.assertNotNull("reviewed-card support text should exist", supportText);
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
                assertReviewedRuntimeSupportText(activity);
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
                assertReviewedRuntimeSupportText(activity);
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
                assertReviewedRuntimeSupportText(activity);
                Assert.assertFalse(ReviewedCardRuntimeConfig.isEnabled(activity));
                Assert.assertEquals(
                    activity.getString(R.string.reviewed_card_runtime_off_button),
                    runtimeToggle.getText().toString()
                );
            });
        }
    }

    @Test
    public void developerPanelToggleButtonExpandsAndCollapsesRouteContent() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitUntilEnabled(scenario, R.id.developer_toggle_button);

            scenario.onActivity(activity -> {
                View developerPanel = activity.findViewById(R.id.developer_panel);
                View developerContent = activity.findViewById(R.id.developer_content);
                Button developerToggle = activity.findViewById(R.id.developer_toggle_button);

                Assert.assertNotNull("developer panel should exist", developerPanel);
                Assert.assertNotNull("developer content should exist", developerContent);
                Assert.assertNotNull("developer route toggle should exist", developerToggle);
                Assert.assertEquals(
                    "developer panel should be visible on the developer route surface",
                    View.VISIBLE,
                    developerPanel.getVisibility()
                );
                Assert.assertEquals(
                    "developer route content should start collapsed",
                    View.GONE,
                    developerContent.getVisibility()
                );
                Assert.assertEquals(
                    "developer route toggle should advertise the collapsed action",
                    activity.getString(R.string.developer_tools_show),
                    developerToggle.getText().toString()
                );

                developerToggle.performClick();
                Assert.assertEquals(
                    "developer route toggle should expand the route content",
                    View.VISIBLE,
                    developerContent.getVisibility()
                );
                Assert.assertEquals(
                    "developer route toggle should advertise the expanded action",
                    activity.getString(R.string.developer_tools_hide),
                    developerToggle.getText().toString()
                );

                developerToggle.performClick();
                Assert.assertEquals(
                    "developer route toggle should collapse the route content",
                    View.GONE,
                    developerContent.getVisibility()
                );
                Assert.assertEquals(
                    "developer route toggle should restore the collapsed action label",
                    activity.getString(R.string.developer_tools_show),
                    developerToggle.getText().toString()
                );
            });
        }
    }

    private void captureUiState(String label) {
        String safeLabel = label.replaceAll("[^a-zA-Z0-9._-]+", "_");
        File screenshotOutput = new File(
            artifactDir,
            "developer_panel_runtime_toggle__" + safeLabel + ".png"
        );
        File dumpOutput = new File(
            artifactDir,
            "developer_panel_runtime_toggle__" + safeLabel + ".xml"
        );
        device.waitForIdle();
        Assert.assertTrue(
            "developer-panel screenshot should be captured",
            device.takeScreenshot(screenshotOutput)
        );
        try {
            device.dumpWindowHierarchy(dumpOutput);
        } catch (Exception exc) {
            throw new AssertionError("developer-panel dump should be captured", exc);
        }
    }

    private static void assertReviewedRuntimeSupportText(MainActivity activity) {
        TextView supportText = activity.findViewById(R.id.reviewed_card_runtime_support_text);
        Assert.assertNotNull("reviewed-card support text should exist", supportText);
        Assert.assertEquals(View.VISIBLE, supportText.getVisibility());
        Assert.assertEquals(
            activity.getString(R.string.reviewed_card_runtime_support_text),
            supportText.getText().toString()
        );
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

package com.senku.mobile;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public final class ConfidenceTokenRenderTest {
    private static final String APP_PACKAGE = "com.senku.mobile";
    private static final long WAIT_MS = 5_000L;

    private UiDevice device;
    private File artifactDir;

    @Rule
    public final TestName testName = new TestName();

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();
        artifactDir = new File(context.getFilesDir(), "test-artifacts");
        if (!artifactDir.exists()) {
            artifactDir.mkdirs();
        }
    }

    @Test
    public void rendersMediumConfidenceMetaStripToken() throws Exception {
        launchAndCapture(
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            "Confidence token preview",
            "Related guidance may fit, but verify the shelter geometry before patching. " +
                "Inspect the tear, reinforce the ridge line, and patch both sides when material allows.",
            "confidence_token_medium"
        );
    }

    @Test
    public void rendersLowConfidenceMetaStripToken() throws Exception {
        launchAndCapture(
            OfflineAnswerEngine.ConfidenceLabel.LOW,
            "Confidence token preview",
            "This answer may not fit directly. Verify the context before you use any of these steps, " +
                "then inspect the material, reinforce the anchor points, and patch only if the surface is still sound.",
            "confidence_token_low"
        );
    }

    @Test
    public void scriptedPromptFlowCompletes() throws Exception {
        String scriptedQuery = decodeArgument("scriptedQuery");
        String captureLabel = decodeArgument("scriptedCaptureLabel");
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel =
            scriptedQuery.toLowerCase().contains("low")
                ? OfflineAnswerEngine.ConfidenceLabel.LOW
                : OfflineAnswerEngine.ConfidenceLabel.MEDIUM;
        String body = confidenceLabel == OfflineAnswerEngine.ConfidenceLabel.LOW
            ? "This answer may not fit directly. Verify the context before you use any of these steps, " +
                "then inspect the material, reinforce the anchor points, and patch only if the surface is still sound."
            : "Related guidance may fit, but verify the shelter geometry before patching. " +
                "Inspect the tear, reinforce the ridge line, and patch both sides when material allows.";
        launchAndCapture(
            confidenceLabel,
            "Confidence token preview",
            body,
            captureLabel.isEmpty() ? "confidence_token_scripted" : captureLabel
        );
    }

    private void launchAndCapture(
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        String title,
        String body,
        String captureLabel
    ) throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(
            DetailActivity.newAnswerIntent(
                context,
                title,
                "Host answer | gemma-4-e2b-it-litert @ 10.0.2.2 | host | 8.6S",
                body,
                Collections.emptyList(),
                null,
                "instrumented-confidence-token",
                null,
                confidenceLabel
            )
        )) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertTrue(
                "detail screen never reached the foreground",
                device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), WAIT_MS)
            );
            device.waitForIdle();
            captureUiState(captureLabel);
        }
    }

    private void captureUiState(String label) throws Exception {
        String safeLabel = safe(label).replaceAll("[^a-zA-Z0-9._-]+", "_");
        String safeTestName = safe(testName.getMethodName()).replaceAll("[^a-zA-Z0-9._-]+", "_");
        File screenshotOutput = new File(artifactDir, safeTestName + "__" + safeLabel + ".png");
        File dumpOutput = new File(artifactDir, safeTestName + "__" + safeLabel + ".xml");
        assertTrue("screenshot capture failed", device.takeScreenshot(screenshotOutput));
        device.dumpWindowHierarchy(dumpOutput);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String decodeArgument(String key) {
        return URLDecoder.decode(
            safe(InstrumentationRegistry.getArguments().getString(key)),
            StandardCharsets.UTF_8
        );
    }
}

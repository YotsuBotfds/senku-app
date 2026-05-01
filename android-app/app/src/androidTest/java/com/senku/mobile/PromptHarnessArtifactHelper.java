package com.senku.mobile;

import android.app.Activity;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

final class PromptHarnessArtifactHelper {
    private static final double SCREENSHOT_DIMENSION_COVERAGE_THRESHOLD = 0.6d;

    interface ResumedActivityProvider {
        Activity getResumedActivityOnMainThread();
    }

    private final UiDevice device;
    private final ResumedActivityProvider resumedActivityProvider;

    PromptHarnessArtifactHelper(UiDevice device, ResumedActivityProvider resumedActivityProvider) {
        this.device = device;
        this.resumedActivityProvider = resumedActivityProvider;
    }

    String currentFocusedWindowPackage() {
        String windowDump = readShellCommandOutput("dumpsys window");
        String packageName = parseFocusedPackage(windowDump, "mCurrentFocus", "mFocusedApp");
        if (!packageName.isEmpty()) {
            return packageName;
        }
        String activityDump = readShellCommandOutput("dumpsys activity top");
        return parseFocusedPackage(activityDump, "ACTIVITY");
    }

    boolean captureScreenshotWithFallback(File screenshotOutput) {
        if (captureShellScreenshot(screenshotOutput) && hasPlausibleDisplayCoverage(screenshotOutput)) {
            return true;
        }
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        if (automation != null) {
            Bitmap automationBitmap = automation.takeScreenshot();
            if (automationBitmap != null) {
                try {
                    if (writeBitmapToFile(automationBitmap, screenshotOutput)
                        && hasPlausibleDisplayCoverage(screenshotOutput)) {
                        return true;
                    }
                } finally {
                    automationBitmap.recycle();
                }
            }
        }
        for (int attempt = 0; attempt < 3; attempt++) {
            if (device.takeScreenshot(screenshotOutput) && hasPlausibleDisplayCoverage(screenshotOutput)) {
                return true;
            }
            SystemClock.sleep(150L);
            device.waitForIdle();
        }
        return captureResumedActivityBitmap(screenshotOutput) && hasPlausibleDisplayCoverage(screenshotOutput);
    }

    private String readShellCommandOutput(String command) {
        try {
            String direct = safe(device.executeShellCommand(command)).trim();
            if (!direct.isEmpty()) {
                return direct;
            }
        } catch (Exception ignored) {
        }
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        if (automation == null) {
            return "";
        }
        ParcelFileDescriptor descriptor = null;
        try {
            descriptor = automation.executeShellCommand(command);
            if (descriptor == null) {
                return "";
            }
            try (FileInputStream input = new FileInputStream(descriptor.getFileDescriptor())) {
                byte[] buffer = new byte[4096];
                StringBuilder output = new StringBuilder();
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read <= 0) {
                        continue;
                    }
                    output.append(new String(buffer, 0, read));
                }
                return output.toString();
            }
        } catch (Exception ignored) {
            return "";
        } finally {
            if (descriptor != null) {
                try {
                    descriptor.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String parseFocusedPackage(String dump, String... markers) {
        if (safe(dump).trim().isEmpty()) {
            return "";
        }
        String[] lines = dump.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = safe(rawLine).trim();
            boolean markerMatched = false;
            for (String marker : markers) {
                if (line.contains(marker)) {
                    markerMatched = true;
                    break;
                }
            }
            if (!markerMatched) {
                continue;
            }
            int slash = line.indexOf('/');
            if (slash <= 0) {
                continue;
            }
            int space = line.lastIndexOf(' ', slash);
            int brace = line.lastIndexOf('{', slash);
            int start = Math.max(space, brace) + 1;
            if (start <= 0 || start >= slash) {
                continue;
            }
            return safe(line.substring(start, slash)).trim();
        }
        return "";
    }

    private boolean hasValidScreenshotFile(File screenshotOutput) {
        return screenshotOutput.isFile() && screenshotOutput.length() > 0L;
    }

    private boolean hasPlausibleDisplayCoverage(File screenshotOutput) {
        if (!hasValidScreenshotFile(screenshotOutput)) {
            return false;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(screenshotOutput.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return false;
        }
        int displayWidth = Math.max(1, device.getDisplayWidth());
        int displayHeight = Math.max(1, device.getDisplayHeight());
        boolean sameOrientation = (bounds.outWidth >= bounds.outHeight) == (displayWidth >= displayHeight);
        double widthCoverage = (double) bounds.outWidth / (double) displayWidth;
        double heightCoverage = (double) bounds.outHeight / (double) displayHeight;
        return sameOrientation
            && widthCoverage >= SCREENSHOT_DIMENSION_COVERAGE_THRESHOLD
            && heightCoverage >= SCREENSHOT_DIMENSION_COVERAGE_THRESHOLD;
    }

    private boolean writeBitmapToFile(Bitmap bitmap, File screenshotOutput) {
        if (bitmap == null) {
            return false;
        }
        try (FileOutputStream stream = new FileOutputStream(screenshotOutput)) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                return false;
            }
            stream.flush();
            return hasValidScreenshotFile(screenshotOutput);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean captureResumedActivityBitmap(File screenshotOutput) {
        final Bitmap[] holder = {null};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = resumedActivityProvider.getResumedActivityOnMainThread();
            if (activity == null) {
                return;
            }
            View root = activity.findViewById(android.R.id.content);
            if (root == null) {
                root = activity.getWindow() == null ? null : activity.getWindow().getDecorView();
            }
            if (root == null) {
                return;
            }
            int width = root.getWidth();
            int height = root.getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            root.draw(canvas);
            holder[0] = bitmap;
        });
        Bitmap bitmap = holder[0];
        if (bitmap == null) {
            return false;
        }
        try {
            return writeBitmapToFile(bitmap, screenshotOutput);
        } finally {
            bitmap.recycle();
        }
    }

    private boolean captureShellScreenshot(File screenshotOutput) {
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        if (automation == null) {
            return false;
        }
        ParcelFileDescriptor descriptor = null;
        try {
            descriptor = automation.executeShellCommand("screencap -p");
            if (descriptor == null) {
                return false;
            }
            try (
                FileInputStream input = new FileInputStream(descriptor.getFileDescriptor());
                FileOutputStream output = new FileOutputStream(screenshotOutput)
            ) {
                byte[] buffer = new byte[16 * 1024];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read == 0) {
                        continue;
                    }
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
            return hasValidScreenshotFile(screenshotOutput);
        } catch (Exception ignored) {
            return false;
        } finally {
            if (descriptor != null) {
                try {
                    descriptor.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

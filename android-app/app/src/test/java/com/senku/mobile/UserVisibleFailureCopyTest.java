package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class UserVisibleFailureCopyTest {
    private static final RuntimeException LEAKY_FAILURE = new RuntimeException(
        "android.database.sqlite.SQLiteException near SELECT from /data/data/com.senku.mobile/files/mobile_pack/senku.db " +
            "content://provider/model.task http://127.0.0.1:1234/v1 C:\\Users\\tateb\\Downloads\\model.task " +
            "at com.senku.mobile.PackRepository.search(PackRepository.java:42)"
    );

    @Test
    public void mainFailureCopyHidesPathsUrisSqlAndExceptionClasses() {
        assertNoLeak(UserVisibleFailureCopy.manualInstallFailureDetails(LEAKY_FAILURE));
        assertNoLeak(UserVisibleFailureCopy.manualLoadFailureDetails(LEAKY_FAILURE));
        assertNoLeak(UserVisibleFailureCopy.searchFailureDetails(LEAKY_FAILURE));
        assertNoLeak(UserVisibleFailureCopy.lastErrorSummary(LEAKY_FAILURE));
    }

    @Test
    public void generationFailureCopyStaysGenericAndActionable() {
        String status = UserVisibleFailureCopy.generationFailureStatus(LEAKY_FAILURE);
        String body = UserVisibleFailureCopy.generationFailureBody(LEAKY_FAILURE);

        assertTrue(status.contains("Offline answer failed"));
        assertTrue(body.contains("Review the source guides"));
        assertNoLeak(status);
        assertNoLeak(body);
    }

    @Test
    public void nullFailuresStillReturnUsefulCopy() {
        assertTrue(UserVisibleFailureCopy.manualInstallFailureDetails(null).contains("Manual install failed"));
        assertTrue(UserVisibleFailureCopy.generationFailureBody(null).contains("Could not finish generation"));
    }

    private static void assertNoLeak(String copy) {
        String lower = copy.toLowerCase();
        assertFalse(copy, lower.contains("sqlite"));
        assertFalse(copy, lower.contains("select"));
        assertFalse(copy, lower.contains("content://"));
        assertFalse(copy, lower.contains("http://"));
        assertFalse(copy, lower.contains("/data/"));
        assertFalse(copy, lower.contains("c:\\"));
        assertFalse(copy, lower.contains("exception"));
        assertFalse(copy, lower.contains("packrepository.java"));
    }
}

package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class HostInferencePolicyTest {
    @Test
    public void localAndEmulatorCleartextHostsAreAllowed() {
        assertAllowed(
            HostInferencePolicy.evaluate("http://localhost:1235/v1"),
            HostInferencePolicy.Reason.LOCAL_CLEARTEXT_ALLOWED,
            "http",
            "localhost"
        );
        assertAllowed(
            HostInferencePolicy.evaluate("http://127.0.0.1:1235/v1"),
            HostInferencePolicy.Reason.LOCAL_CLEARTEXT_ALLOWED,
            "http",
            "127.0.0.1"
        );
        assertAllowed(
            HostInferencePolicy.evaluate("http://10.0.2.2:1235/v1"),
            HostInferencePolicy.Reason.LOCAL_CLEARTEXT_ALLOWED,
            "http",
            "10.0.2.2"
        );
        assertAllowed(
            HostInferencePolicy.evaluate("http://10.0.3.2:1235/v1"),
            HostInferencePolicy.Reason.LOCAL_CLEARTEXT_ALLOWED,
            "http",
            "10.0.3.2"
        );
    }

    @Test
    public void localCleartextHostMatchingIsTrimmedAndCaseInsensitive() {
        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate("http://LOCALHOST:1235/v1");

        assertAllowed(
            decision,
            HostInferencePolicy.Reason.LOCAL_CLEARTEXT_ALLOWED,
            "http",
            "LOCALHOST"
        );
    }

    @Test
    public void nonLocalCleartextIsRejectedByDefault() {
        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate("http://host.local:1235/v1");

        assertRejected(
            decision,
            HostInferencePolicy.Reason.NON_LOCAL_CLEARTEXT_REJECTED,
            "http",
            "host.local"
        );
    }

    @Test
    public void httpsRequiresConfiguration() {
        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate("https://host.local/v1");

        assertRejected(
            decision,
            HostInferencePolicy.Reason.HTTPS_REQUIRES_CONFIGURATION,
            "https",
            "host.local"
        );
    }

    @Test
    public void httpsIsAllowedWhenConfigured() {
        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate("https://host.local/v1", true);

        assertAllowed(
            decision,
            HostInferencePolicy.Reason.HTTPS_ALLOWED,
            "https",
            "host.local"
        );
    }

    @Test
    public void normalizedDefaultHostUrlIsAllowed() {
        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate(
            HostInferenceConfig.normalizeBaseUrl(" http://10.0.2.2:1235/ ")
        );

        assertAllowed(
            decision,
            HostInferencePolicy.Reason.LOCAL_CLEARTEXT_ALLOWED,
            "http",
            "10.0.2.2"
        );
    }

    @Test
    public void hostOnlyNormalizedUrlKeepsExistingUriLimitation() {
        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate(
            HostInferenceConfig.normalizeBaseUrl("localhost:1235")
        );

        assertRejected(
            decision,
            HostInferencePolicy.Reason.MISSING_HOST,
            "localhost",
            ""
        );
    }

    @Test
    public void unsupportedSchemesAreRejected() {
        HostInferencePolicy.Decision decision = HostInferencePolicy.evaluate("ftp://localhost:1235/v1");

        assertRejected(
            decision,
            HostInferencePolicy.Reason.UNSUPPORTED_SCHEME,
            "ftp",
            "localhost"
        );
    }

    private static void assertAllowed(
        HostInferencePolicy.Decision decision,
        HostInferencePolicy.Reason reason,
        String scheme,
        String host
    ) {
        assertTrue(decision.allowed);
        assertEquals(reason, decision.reason);
        assertEquals(scheme, decision.scheme);
        assertEquals(host, decision.host);
    }

    private static void assertRejected(
        HostInferencePolicy.Decision decision,
        HostInferencePolicy.Reason reason,
        String scheme,
        String host
    ) {
        assertFalse(decision.allowed);
        assertEquals(reason, decision.reason);
        assertEquals(scheme, decision.scheme);
        assertEquals(host, decision.host);
    }
}

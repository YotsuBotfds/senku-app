package com.senku.mobile;

import java.net.URI;
import java.util.Locale;

final class HostInferencePolicy {
    private HostInferencePolicy() {
    }

    static Decision evaluate(String baseUrl) {
        return evaluate(baseUrl, true);
    }

    static Decision evaluate(String baseUrl, boolean allowHttps) {
        URI uri;
        try {
            uri = URI.create(withDefaultHttpScheme(safe(baseUrl).trim()));
        } catch (IllegalArgumentException exception) {
            return Decision.rejected(Reason.INVALID_URL, "", "");
        }

        String scheme = safe(uri.getScheme()).trim().toLowerCase(Locale.US);
        if (scheme.isEmpty()) {
            scheme = "http";
        }

        String host = safe(uri.getHost()).trim();
        if (host.isEmpty()) {
            return Decision.rejected(Reason.MISSING_HOST, scheme, host);
        }

        if ("http".equals(scheme)) {
            if (isLocalCleartextHost(host)) {
                return Decision.allowed(Reason.LOCAL_CLEARTEXT_ALLOWED, scheme, host);
            }
            return Decision.rejected(Reason.NON_LOCAL_CLEARTEXT_REJECTED, scheme, host);
        }

        if ("https".equals(scheme)) {
            return allowHttps
                ? Decision.allowed(Reason.HTTPS_ALLOWED, scheme, host)
                : Decision.rejected(Reason.HTTPS_REQUIRES_CONFIGURATION, scheme, host);
        }

        return Decision.rejected(Reason.UNSUPPORTED_SCHEME, scheme, host);
    }

    static boolean isLocalCleartextHost(String host) {
        String normalized = safe(host).trim().toLowerCase(Locale.US);
        return "localhost".equals(normalized)
            || "127.0.0.1".equals(normalized)
            || "10.0.2.2".equals(normalized);
    }

    private static String withDefaultHttpScheme(String baseUrl) {
        if (baseUrl.isEmpty() || baseUrl.contains("://")) {
            return baseUrl;
        }
        return "http://" + baseUrl;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    enum Reason {
        LOCAL_CLEARTEXT_ALLOWED,
        NON_LOCAL_CLEARTEXT_REJECTED,
        HTTPS_ALLOWED,
        HTTPS_REQUIRES_CONFIGURATION,
        MISSING_HOST,
        UNSUPPORTED_SCHEME,
        INVALID_URL
    }

    static final class Decision {
        final boolean allowed;
        final Reason reason;
        final String scheme;
        final String host;

        private Decision(boolean allowed, Reason reason, String scheme, String host) {
            this.allowed = allowed;
            this.reason = reason;
            this.scheme = safe(scheme);
            this.host = safe(host);
        }

        private static Decision allowed(Reason reason, String scheme, String host) {
            return new Decision(true, reason, scheme, host);
        }

        private static Decision rejected(Reason reason, String scheme, String host) {
            return new Decision(false, reason, scheme, host);
        }
    }
}

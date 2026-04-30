package com.senku.mobile;

final class DetailAnswerGenerationBodyPolicy {
    static final class FinalBodyResolution {
        final String body;
        final boolean usedStreamingFallback;
        final int finalVisibleLength;
        final int streamVisibleLength;

        FinalBodyResolution(
            String body,
            boolean usedStreamingFallback,
            int finalVisibleLength,
            int streamVisibleLength
        ) {
            this.body = safe(body);
            this.usedStreamingFallback = usedStreamingFallback;
            this.finalVisibleLength = finalVisibleLength;
            this.streamVisibleLength = streamVisibleLength;
        }
    }

    private final DetailAnswerBodyFormatter bodyFormatter;

    DetailAnswerGenerationBodyPolicy(DetailAnswerBodyFormatter bodyFormatter) {
        this.bodyFormatter = bodyFormatter;
    }

    String normalizeStreamingBody(String partialBody) {
        String body = safe(partialBody);
        if (body.isEmpty()) {
            return "";
        }
        if (body.regionMatches(true, 0, "Answer\n", 0, "Answer\n".length())) {
            return body;
        }
        return PromptBuilder.buildStreamingAnswerBody(body);
    }

    FinalBodyResolution resolveFinalAnswerBody(
        String finalAnswerBody,
        int requestToken,
        int streamingAnswerToken,
        String bestStreamingAnswerBody
    ) {
        String finalBody = safe(finalAnswerBody);
        if (requestToken != streamingAnswerToken) {
            return new FinalBodyResolution(finalBody, false, 0, 0);
        }
        String streamBody = safe(bestStreamingAnswerBody);
        String finalVisible = formatAnswerBody(finalBody);
        String streamVisible = formatAnswerBody(streamBody);
        if (streamVisible.isEmpty()) {
            return new FinalBodyResolution(finalBody, false, finalVisible.length(), 0);
        }
        boolean finalLooksBroken = finalVisible.length() <= 12
            || (!finalVisible.contains(" ") && streamVisible.length() >= 24)
            || PromptBuilder.isLikelyCorruptedAnswer(finalVisible);
        boolean streamedIsSubstantiallyRicher = streamVisible.length() >= Math.max(40, finalVisible.length() + 24);
        if (finalLooksBroken && streamedIsSubstantiallyRicher) {
            return new FinalBodyResolution(streamBody, true, finalVisible.length(), streamVisible.length());
        }
        return new FinalBodyResolution(finalBody, false, finalVisible.length(), streamVisible.length());
    }

    private String formatAnswerBody(String body) {
        return bodyFormatter.formatAnswerBody(body);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

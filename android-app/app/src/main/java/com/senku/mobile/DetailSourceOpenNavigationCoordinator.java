package com.senku.mobile;

final class DetailSourceOpenNavigationCoordinator {
    private static final String SOURCE_GUIDE_HARNESS_LABEL = "detail.openSourceGuide";
    private static final String CROSS_REFERENCE_GUIDE_HARNESS_LABEL = "detail.openCrossReferenceGuide";
    private static final String SOURCE_GUIDE_LOAD_FAILURE_LABEL = "openSourceGuide.loadFailed";
    private static final String CROSS_REFERENCE_GUIDE_LOAD_FAILURE_LABEL = "openCrossReferenceGuide.loadFailed";

    private DetailSourceOpenNavigationCoordinator() {
    }

    static Decision decide(SearchResult source) {
        return decideSourceGuide(source);
    }

    static Decision decideSourceGuide(SearchResult source) {
        return decide(
            source,
            SOURCE_GUIDE_HARNESS_LABEL,
            SOURCE_GUIDE_LOAD_FAILURE_LABEL
        );
    }

    static Decision decideCrossReferenceGuide(SearchResult guide) {
        return decide(
            guide,
            CROSS_REFERENCE_GUIDE_HARNESS_LABEL,
            CROSS_REFERENCE_GUIDE_LOAD_FAILURE_LABEL
        );
    }

    static boolean shouldApplyResolvedOpen(
        boolean finishing,
        boolean destroyed,
        int requestToken,
        int activeToken
    ) {
        return !finishing && !destroyed && requestToken == activeToken;
    }

    private static Decision decide(
        SearchResult source,
        String harnessTaskLabel,
        String loadFailureLogLabel
    ) {
        SearchResult fallback = source == null ? new SearchResult("", "", "", "") : source;
        String guideId = safe(fallback.guideId).trim();
        return new Decision(
            fallback,
            guideId,
            !guideId.isEmpty(),
            harnessTaskLabel,
            loadFailureLogLabel
        );
    }

    static final class Decision {
        final SearchResult source;
        final String guideId;
        final boolean shouldLoadGuideBeforeOpen;
        final String harnessTaskLabel;
        final String loadFailureLogLabel;

        private Decision(
            SearchResult source,
            String guideId,
            boolean shouldLoadGuideBeforeOpen,
            String harnessTaskLabel,
            String loadFailureLogLabel
        ) {
            this.source = source;
            this.guideId = guideId;
            this.shouldLoadGuideBeforeOpen = shouldLoadGuideBeforeOpen;
            this.harnessTaskLabel = harnessTaskLabel;
            this.loadFailureLogLabel = loadFailureLogLabel;
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}

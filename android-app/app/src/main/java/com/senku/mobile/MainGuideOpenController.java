package com.senku.mobile;

import java.util.List;

final class MainGuideOpenController {
    interface GuideLoader {
        SearchResult loadGuideById(String guideId) throws Exception;
    }

    enum Action {
        IGNORE,
        OPEN_GUIDE,
        LOAD_FROM_REPOSITORY,
        PACK_UNAVAILABLE,
        REPOSITORY_LOAD_FAILED,
        GUIDE_UNAVAILABLE
    }

    static final class Request {
        final String guideId;
        final String unavailableLabel;
        final int unavailableMessageResId;

        Request(String guideId, String unavailableLabel, int unavailableMessageResId) {
            this.guideId = safe(guideId).trim();
            String cleanLabel = safe(unavailableLabel).trim();
            if (cleanLabel.isEmpty()) {
                cleanLabel = this.guideId;
            }
            if (cleanLabel.isEmpty()) {
                cleanLabel = "selected guide";
            }
            this.unavailableLabel = cleanLabel;
            this.unavailableMessageResId = unavailableMessageResId;
        }
    }

    static final class Decision {
        final Action action;
        final Request request;
        final SearchResult guide;
        final Exception repositoryLoadFailure;

        Decision(Action action, Request request, SearchResult guide) {
            this(action, request, guide, null);
        }

        Decision(Action action, Request request, SearchResult guide, Exception repositoryLoadFailure) {
            this.action = action == null ? Action.IGNORE : action;
            this.request = request;
            this.guide = guide;
            this.repositoryLoadFailure = repositoryLoadFailure;
        }
    }

    Decision resolveInitial(
        String guideId,
        String fallbackLabel,
        int unavailableMessageResId,
        List<SearchResult> loadedGuides,
        boolean repositoryAvailable
    ) {
        Request request = new Request(guideId, fallbackLabel, unavailableMessageResId);
        if (request.guideId.isEmpty()) {
            return new Decision(Action.IGNORE, request, null);
        }
        SearchResult loadedGuide = findGuideById(loadedGuides, request.guideId);
        if (loadedGuide != null) {
            return new Decision(Action.OPEN_GUIDE, request, loadedGuide);
        }
        if (!repositoryAvailable) {
            return new Decision(Action.PACK_UNAVAILABLE, request, null);
        }
        return new Decision(Action.LOAD_FROM_REPOSITORY, request, null);
    }

    Decision resolveRepositoryResult(Request request, SearchResult loadedGuide) {
        if (request == null || request.guideId.isEmpty()) {
            return new Decision(Action.IGNORE, request, null);
        }
        if (loadedGuide != null) {
            return new Decision(Action.OPEN_GUIDE, request, loadedGuide);
        }
        return new Decision(Action.GUIDE_UNAVAILABLE, request, null);
    }

    Decision resolveRepositoryLoad(Request request, GuideLoader guideLoader) {
        if (request == null || request.guideId.isEmpty()) {
            return new Decision(Action.IGNORE, request, null);
        }
        if (guideLoader == null) {
            return new Decision(Action.PACK_UNAVAILABLE, request, null);
        }
        try {
            return resolveRepositoryResult(request, guideLoader.loadGuideById(request.guideId));
        } catch (Exception exception) {
            return new Decision(Action.REPOSITORY_LOAD_FAILED, request, null, exception);
        }
    }

    private SearchResult findGuideById(List<SearchResult> guides, String guideId) {
        String normalizedGuideId = safe(guideId).trim();
        if (normalizedGuideId.isEmpty() || guides == null) {
            return null;
        }
        for (SearchResult guide : guides) {
            if (normalizedGuideId.equalsIgnoreCase(safe(guide == null ? null : guide.guideId).trim())) {
                return guide;
            }
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

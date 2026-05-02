package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class MainSavedGuidesController {
    private static final int MAX_SAVED_GUIDES = 12;

    private boolean pendingSectionFocus;
    private final LatestJobGate refreshGate = new LatestJobGate();

    RefreshPlan beginRefresh(boolean repositoryReady, List<String> savedGuideIds) {
        long refreshToken = refreshGate.nextJobToken();
        RefreshPlan refreshPlan = planRefresh(repositoryReady, savedGuideIds);
        return refreshPlan.withRefreshToken(refreshToken);
    }

    boolean isCurrentRefresh(long refreshToken) {
        return refreshToken > 0L && refreshGate.isCurrentJob(refreshToken);
    }

    RenderPlan planRender(RefreshPlan refreshPlan, boolean sameRepository, List<SearchResult> loadedGuides) {
        if (refreshPlan == null || !sameRepository || !isCurrentRefresh(refreshPlan.refreshToken)) {
            return RenderPlan.skip();
        }
        if (refreshPlan.renderEmpty) {
            return RenderPlan.render(Collections.emptyList());
        }
        Map<String, SearchResult> guidesById = new LinkedHashMap<>();
        if (loadedGuides != null) {
            for (SearchResult guide : loadedGuides) {
                if (guide != null) {
                    String normalizedGuideId = normalizeGuideId(guide.guideId);
                    if (!normalizedGuideId.isEmpty() && !guidesById.containsKey(normalizedGuideId)) {
                        guidesById.put(normalizedGuideId, guide);
                    }
                }
            }
        }
        ArrayList<SearchResult> renderGuides = new ArrayList<>();
        for (String guideId : refreshPlan.guideIdsToLoad) {
            SearchResult guide = guidesById.get(normalizeGuideId(guideId));
            if (guide != null) {
                renderGuides.add(guide);
            }
        }
        return RenderPlan.render(renderGuides);
    }

    RefreshPlan planRefresh(boolean repositoryReady, List<String> savedGuideIds) {
        if (!repositoryReady || savedGuideIds == null || savedGuideIds.isEmpty()) {
            return RefreshPlan.empty();
        }
        ArrayList<String> guideIds = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String guideId : savedGuideIds) {
            if (guideIds.size() >= MAX_SAVED_GUIDES) {
                break;
            }
            String normalizedGuideId = normalizeGuideId(guideId);
            if (!normalizedGuideId.isEmpty() && seen.add(normalizedGuideId)) {
                guideIds.add(normalizedGuideId);
            }
        }
        if (guideIds.isEmpty()) {
            return RefreshPlan.empty();
        }
        return new RefreshPlan(false, guideIds);
    }

    SectionState sectionState(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        int savedGuideCount
    ) {
        boolean hasSavedGuides = savedGuideCount > 0;
        boolean sectionVisible = SavedGuidesPolicy.shouldShowSection(
            browseMode,
            activePhoneTab,
            savedGuideCount
        );
        return new SectionState(
            sectionVisible,
            sectionVisible && !hasSavedGuides,
            sectionVisible && hasSavedGuides
        );
    }

    boolean shouldLoadBrowseGuidesForDestination(boolean repositoryReady, int loadedGuideCount) {
        return SavedGuidesPolicy.shouldLoadBrowseGuidesForDestination(repositoryReady, loadedGuideCount);
    }

    void requestSectionFocus() {
        pendingSectionFocus = true;
    }

    boolean shouldAttemptSectionFocus(boolean browseMode) {
        return pendingSectionFocus && browseMode;
    }

    boolean consumeSectionFocusIfReady(boolean browseMode, boolean sectionVisible) {
        if (!SavedGuidesPolicy.shouldFocusSection(pendingSectionFocus, browseMode, sectionVisible)) {
            return false;
        }
        pendingSectionFocus = false;
        return true;
    }

    boolean hasPendingSectionFocusForTest() {
        return pendingSectionFocus;
    }

    private static String normalizeGuideId(String guideId) {
        return guideId == null ? "" : guideId.trim().toUpperCase(Locale.US);
    }

    static final class RefreshPlan {
        final long refreshToken;
        final boolean renderEmpty;
        final List<String> guideIdsToLoad;

        private RefreshPlan(boolean renderEmpty, List<String> guideIdsToLoad) {
            this(0L, renderEmpty, guideIdsToLoad);
        }

        private RefreshPlan(long refreshToken, boolean renderEmpty, List<String> guideIdsToLoad) {
            this.refreshToken = refreshToken;
            this.renderEmpty = renderEmpty;
            this.guideIdsToLoad = Collections.unmodifiableList(new ArrayList<>(guideIdsToLoad));
        }

        static RefreshPlan empty() {
            return new RefreshPlan(true, Collections.emptyList());
        }

        private RefreshPlan withRefreshToken(long refreshToken) {
            return new RefreshPlan(refreshToken, renderEmpty, guideIdsToLoad);
        }
    }

    static final class RenderPlan {
        final boolean shouldRender;
        final List<SearchResult> guides;

        private RenderPlan(boolean shouldRender, List<SearchResult> guides) {
            this.shouldRender = shouldRender;
            this.guides = Collections.unmodifiableList(new ArrayList<>(guides));
        }

        static RenderPlan skip() {
            return new RenderPlan(false, Collections.emptyList());
        }

        static RenderPlan render(List<SearchResult> guides) {
            return new RenderPlan(true, guides == null ? Collections.emptyList() : guides);
        }
    }

    static final class SectionState {
        final boolean sectionVisible;
        final boolean emptyTextVisible;
        final boolean savedGuidesVisible;

        private SectionState(
            boolean sectionVisible,
            boolean emptyTextVisible,
            boolean savedGuidesVisible
        ) {
            this.sectionVisible = sectionVisible;
            this.emptyTextVisible = emptyTextVisible;
            this.savedGuidesVisible = savedGuidesVisible;
        }
    }
}

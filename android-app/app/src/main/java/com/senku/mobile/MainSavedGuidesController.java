package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MainSavedGuidesController {
    private static final int MAX_SAVED_GUIDES = 12;

    private boolean pendingSectionFocus;

    RefreshPlan planRefresh(boolean repositoryReady, List<String> savedGuideIds) {
        if (!repositoryReady || savedGuideIds == null || savedGuideIds.isEmpty()) {
            return RefreshPlan.empty();
        }
        ArrayList<String> guideIds = new ArrayList<>();
        for (String guideId : savedGuideIds) {
            if (guideIds.size() >= MAX_SAVED_GUIDES) {
                break;
            }
            guideIds.add(guideId);
        }
        return new RefreshPlan(false, guideIds);
    }

    SectionState sectionState(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        int savedGuideCount
    ) {
        boolean hasSavedGuides = savedGuideCount > 0;
        return new SectionState(
            SavedGuidesPolicy.shouldShowSection(browseMode, activePhoneTab, savedGuideCount),
            !hasSavedGuides,
            hasSavedGuides
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

    static final class RefreshPlan {
        final boolean renderEmpty;
        final List<String> guideIdsToLoad;

        private RefreshPlan(boolean renderEmpty, List<String> guideIdsToLoad) {
            this.renderEmpty = renderEmpty;
            this.guideIdsToLoad = Collections.unmodifiableList(new ArrayList<>(guideIdsToLoad));
        }

        static RefreshPlan empty() {
            return new RefreshPlan(true, Collections.emptyList());
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

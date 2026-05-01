package com.senku.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

final class MainResultPreviewBridgeController {
    private static final int SIGNAL_LIMIT = 1;
    private static final String HARNESS_LABEL = "main.refreshResultPreviewBridges";

    private int requestVersion;

    interface Host {
        Executor executor();

        int beginHarnessTask(String label);

        void runTrackedOnUiThread(int harnessToken, Runnable action);

        MainPresentationFormatter presentationFormatter();

        void onResultPreviewBridgeSignals(Map<String, SearchResultAdapter.LinkedGuidePreview> previewSignals);
    }

    interface RelatedGuideLoader {
        List<SearchResult> loadRelatedGuides(String guideId, int limit);
    }

    void refreshAsync(List<SearchResult> results, PackRepository repository, Host host) {
        refreshAsync(results, repository == null ? null : repository::loadRelatedGuides, host);
    }

    void refreshAsyncForTest(List<SearchResult> results, RelatedGuideLoader loader, Host host) {
        refreshAsync(results, loader, host);
    }

    private void refreshAsync(List<SearchResult> results, RelatedGuideLoader loader, Host host) {
        LinkedHashMap<String, String> previewGuideIds = ResultPreviewBridgePolicy.collectGuideIds(results);
        int activeRequestVersion = ++requestVersion;
        if (loader == null || previewGuideIds.isEmpty()) {
            return;
        }
        int harnessToken = host.beginHarnessTask(HARNESS_LABEL);
        host.executor().execute(() -> {
            try {
                Map<String, SearchResultAdapter.LinkedGuidePreview> previewSignals =
                    loadPreviewSignals(previewGuideIds, loader, host.presentationFormatter());
                publishIfCurrent(host, harnessToken, activeRequestVersion, previewSignals);
            } catch (Exception ignored) {
                publishIfCurrent(host, harnessToken, activeRequestVersion, Collections.emptyMap());
            }
        });
    }

    private LinkedHashMap<String, SearchResultAdapter.LinkedGuidePreview> loadPreviewSignals(
        LinkedHashMap<String, String> previewGuideIds,
        RelatedGuideLoader loader,
        MainPresentationFormatter formatter
    ) {
        LinkedHashMap<String, SearchResultAdapter.LinkedGuidePreview> previewSignals = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : previewGuideIds.entrySet()) {
            List<SearchResult> related = loader.loadRelatedGuides(entry.getValue(), SIGNAL_LIMIT);
            if (related == null || related.isEmpty()) {
                continue;
            }
            SearchResult preview = related.get(0);
            String previewGuideId = safe(preview == null ? null : preview.guideId).trim();
            if (previewGuideId.isEmpty() || previewGuideId.equalsIgnoreCase(entry.getValue())) {
                continue;
            }
            String previewLabel = formatter.clipLabel(
                formatter.buildGuideReference(preview, entry.getValue()),
                68
            );
            if (!previewLabel.isEmpty()) {
                previewSignals.put(
                    entry.getKey(),
                    new SearchResultAdapter.LinkedGuidePreview(
                        previewGuideId,
                        safe(preview == null ? null : preview.title).trim(),
                        previewLabel
                    )
                );
            }
        }
        return previewSignals;
    }

    private void publishIfCurrent(
        Host host,
        int harnessToken,
        int activeRequestVersion,
        Map<String, SearchResultAdapter.LinkedGuidePreview> previewSignals
    ) {
        host.runTrackedOnUiThread(harnessToken, () -> {
            if (activeRequestVersion != requestVersion) {
                return;
            }
            host.onResultPreviewBridgeSignals(previewSignals);
        });
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

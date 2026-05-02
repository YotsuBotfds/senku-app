package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.Test;

public final class MainResultPreviewBridgeControllerTest {
    @Test
    public void loadsFirstRelatedGuidePreviewForCollectedResultGuideIds() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();
        RecordingLoader loader = new RecordingLoader();
        loader.related.put("GD-101", guides(guide("GD-202", "Water Storage")));

        controller.refreshAsyncForTest(guides(guide("GD-101", "Fire Starting")), loader, host);
        host.flushUi();

        assertEquals(Arrays.asList("GD-101:1"), loader.calls);
        assertEquals(1, host.signals.size());
        SearchResultAdapter.LinkedGuidePreview preview = host.signals.get("gd-101");
        assertEquals("GD-202", preview.guideId);
        assertEquals("Water Storage", preview.title);
        assertEquals("GD-202 - Water Storage", preview.displayLabel);
    }

    @Test
    public void capsSignalsAtOneRelatedGuidePerAnchor() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();
        RecordingLoader loader = new RecordingLoader();
        loader.related.put("GD-101", guides(
            guide("GD-202", "Water Storage"),
            guide("GD-303", "Ignored")
        ));

        controller.refreshAsyncForTest(guides(guide("GD-101", "Fire Starting")), loader, host);
        host.flushUi();

        assertEquals(Arrays.asList("GD-101:1"), loader.calls);
        assertEquals("GD-202", host.signals.get("gd-101").guideId);
    }

    @Test
    public void skipsSelfLinksAndBlankRelatedGuideIds() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();
        RecordingLoader loader = new RecordingLoader();
        loader.related.put("GD-101", guides(guide("gd-101", "Same Guide")));
        loader.related.put("GD-202", guides(guide("   ", "Blank Guide")));
        loader.related.put("GD-303", guides(guide("GD-404", "Valid Guide")));

        controller.refreshAsyncForTest(
            guides(guide("GD-101", "One"), guide("GD-202", "Two"), guide("GD-303", "Three")),
            loader,
            host
        );
        host.flushUi();

        assertEquals(1, host.signals.size());
        assertEquals("GD-404", host.signals.get("gd-303").guideId);
    }

    @Test
    public void loaderExceptionPublishesEmptySignalsForCurrentRequest() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();

        controller.refreshAsyncForTest(guides(guide("GD-101", "Fire Starting")), (guideId, limit) -> {
            throw new IllegalStateException("load failed");
        }, host);
        host.flushUi();

        assertTrue(host.signals.isEmpty());
        assertEquals(1, host.publishCount);
    }

    @Test
    public void executeRejectionPublishesEmptySignalsAndSettlesHarnessToken() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();
        host.executor = command -> {
            throw new RejectedExecutionException("executor closed");
        };
        RecordingLoader loader = new RecordingLoader();
        loader.related.put("GD-101", guides(guide("GD-202", "Water Storage")));

        controller.refreshAsyncForTest(guides(guide("GD-101", "Fire Starting")), loader, host);
        host.flushUi();

        assertTrue(host.signals.isEmpty());
        assertEquals(1, host.publishCount);
        assertEquals(List.of(1), host.uiHarnessTokens);
        assertTrue(loader.calls.isEmpty());
    }

    @Test
    public void emptyResultsInvalidatePendingRequestWithoutPublishing() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();
        RecordingLoader loader = new RecordingLoader();
        loader.related.put("GD-101", guides(guide("GD-202", "Water Storage")));

        controller.refreshAsyncForTest(guides(guide("GD-101", "Fire Starting")), loader, host);
        controller.refreshAsyncForTest(Collections.emptyList(), loader, host);
        host.flushUi();

        assertEquals(0, host.publishCount);
    }

    @Test
    public void nullLoaderInvalidatesPendingRequestWithoutPublishing() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();
        RecordingLoader loader = new RecordingLoader();
        loader.related.put("GD-101", guides(guide("GD-202", "Water Storage")));

        controller.refreshAsyncForTest(guides(guide("GD-101", "Fire Starting")), loader, host);
        controller.refreshAsyncForTest(guides(guide("GD-303", "Shelter")), null, host);
        host.flushUi();

        assertEquals(0, host.publishCount);
    }

    @Test
    public void newerRequestWinsOverOlderQueuedUiPublication() {
        MainResultPreviewBridgeController controller = new MainResultPreviewBridgeController();
        RecordingHost host = new RecordingHost();
        RecordingLoader loader = new RecordingLoader();
        loader.related.put("GD-101", guides(guide("GD-202", "Old Preview")));
        loader.related.put("GD-303", guides(guide("GD-404", "New Preview")));

        controller.refreshAsyncForTest(guides(guide("GD-101", "Old")), loader, host);
        controller.refreshAsyncForTest(guides(guide("GD-303", "New")), loader, host);
        host.flushUi();

        assertEquals(1, host.publishCount);
        assertEquals("GD-404", host.signals.get("gd-303").guideId);
        assertTrue(host.signals.get("gd-101") == null);
    }

    private static List<SearchResult> guides(SearchResult... guides) {
        return Arrays.asList(guides);
    }

    private static SearchResult guide(String guideId, String title) {
        return new SearchResult(title, "", "", "", guideId, "", "", "");
    }

    private static final class RecordingLoader implements MainResultPreviewBridgeController.RelatedGuideLoader {
        final Map<String, List<SearchResult>> related = new LinkedHashMap<>();
        final List<String> calls = new ArrayList<>();

        @Override
        public List<SearchResult> loadRelatedGuides(String guideId, int limit) {
            calls.add(guideId + ":" + limit);
            return related.getOrDefault(guideId, Collections.emptyList());
        }
    }

    private static final class RecordingHost implements MainResultPreviewBridgeController.Host {
        final ArrayList<Runnable> uiActions = new ArrayList<>();
        final ArrayList<Integer> uiHarnessTokens = new ArrayList<>();
        Executor executor = Runnable::run;
        Map<String, SearchResultAdapter.LinkedGuidePreview> signals = Collections.emptyMap();
        int publishCount;

        @Override
        public Executor executor() {
            return executor;
        }

        @Override
        public int beginHarnessTask(String label) {
            return 1;
        }

        @Override
        public void runTrackedOnUiThread(int harnessToken, Runnable action) {
            uiHarnessTokens.add(harnessToken);
            uiActions.add(action);
        }

        @Override
        public MainPresentationFormatter presentationFormatter() {
            return new MainPresentationFormatter(null);
        }

        @Override
        public void onResultPreviewBridgeSignals(
            Map<String, SearchResultAdapter.LinkedGuidePreview> previewSignals
        ) {
            signals = new LinkedHashMap<>(previewSignals);
            publishCount++;
        }

        void flushUi() {
            ArrayList<Runnable> pending = new ArrayList<>(uiActions);
            uiActions.clear();
            for (Runnable action : pending) {
                action.run();
            }
        }
    }
}

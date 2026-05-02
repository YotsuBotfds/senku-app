package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class MainHomeRelatedGuideControllerTest {
    private final MainHomeRelatedGuideController controller = new MainHomeRelatedGuideController();
    private final MainPresentationFormatter formatter = new MainPresentationFormatter(null);

    @Test
    public void recentThreadAnchorWinsOverPinnedGuide() {
        MainActivity.HomeGuideAnchor anchor = controller.selectHomeGuideAnchor(
            guides(guide("GD-101", "Pinned Shelter")),
            previews(preview("GD-202", "Recent Water")),
            formatter
        );

        assertEquals("GD-202", anchor.guideId);
        assertEquals("GD-202 - Recent Water", anchor.label);
        assertTrue(anchor.fromRecentThread);
    }

    @Test
    public void fallsBackToFirstPinnedGuideWhenNoRecentGuideAnchorExists() {
        MainActivity.HomeGuideAnchor anchor = controller.selectHomeGuideAnchor(
            guides(guide(" GD-101 ", "Pinned Shelter"), guide("GD-202", "Second")),
            previews(preview("", "")),
            formatter
        );

        assertEquals("GD-101", anchor.guideId);
        assertEquals("GD-101 - Pinned Shelter", anchor.label);
        assertFalse(anchor.fromRecentThread);
    }

    @Test
    public void fallsBackToFirstValidPinnedGuideWhenEarlierSavedGuideIsInvalid() {
        MainActivity.HomeGuideAnchor anchor = controller.selectHomeGuideAnchor(
            guides(null, guide("   ", "No Id"), guide(" gd-202 ", "Fallback Water")),
            Collections.emptyList(),
            formatter
        );

        assertEquals("gd-202", anchor.guideId);
        assertEquals("gd-202 - Fallback Water", anchor.label);
        assertFalse(anchor.fromRecentThread);
    }

    @Test
    public void pinnedGuideWithoutGuideIdDoesNotProduceAnchor() {
        MainActivity.HomeGuideAnchor anchor = controller.selectHomeGuideAnchor(
            guides(guide("   ", "No Id")),
            Collections.emptyList(),
            formatter
        );

        assertNull(anchor);
    }

    @Test
    public void latestRecentThreadUsesFirstSourceWithGuideId() {
        ChatSessionStore.ConversationPreview preview = preview(
            source("", "Ignored Title"),
            source(" GD-303 ", "Recent Clay")
        );

        MainActivity.HomeGuideAnchor anchor =
            controller.selectLatestRecentThreadHomeGuideAnchor(previews(preview), formatter);

        assertEquals("GD-303", anchor.guideId);
        assertEquals("GD-303 - Recent Clay", anchor.label);
        assertTrue(anchor.fromRecentThread);
    }

    @Test
    public void latestRecentThreadWithoutSourcesDoesNotProduceAnchor() {
        assertNull(controller.selectLatestRecentThreadHomeGuideAnchor(
            previews(preview(source("", "No Id"))),
            formatter
        ));
        assertNull(controller.selectLatestRecentThreadHomeGuideAnchor(Collections.emptyList(), formatter));
    }

    @Test
    public void recentThreadAnchorSkipsNewestPreviewWithoutGuideSource() {
        MainActivity.HomeGuideAnchor anchor = controller.selectHomeGuideAnchor(
            guides(guide("GD-101", "Pinned Shelter")),
            previews(
                preview(source("", "No Source Recent")),
                preview(source("GD-404", "Older Sourced Recent"))
            ),
            formatter
        );

        assertEquals("GD-404", anchor.guideId);
        assertEquals("GD-404 - Older Sourced Recent", anchor.label);
        assertTrue(anchor.fromRecentThread);
    }

    private static List<SearchResult> guides(SearchResult... guides) {
        return Arrays.asList(guides);
    }

    private static List<ChatSessionStore.ConversationPreview> previews(
        ChatSessionStore.ConversationPreview... previews
    ) {
        return Arrays.asList(previews);
    }

    private static ChatSessionStore.ConversationPreview preview(String guideId, String title) {
        return preview(source(guideId, title));
    }

    private static ChatSessionStore.ConversationPreview preview(SearchResult... sources) {
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "question",
            "",
            "answer",
            Collections.emptyList(),
            Arrays.asList(sources),
            "",
            1L
        );
        return new ChatSessionStore.ConversationPreview("conversation", turn, 1, 1L);
    }

    private static SearchResult source(String guideId, String title) {
        return guide(guideId, title);
    }

    private static SearchResult guide(String guideId, String title) {
        return new SearchResult(title, "", "", "", guideId, "", "", "");
    }
}

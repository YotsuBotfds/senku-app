package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.senku.ui.answer.AnswerContent;
import com.senku.ui.answer.AnswerContentFactory;
import com.senku.ui.answer.Evidence;
import com.senku.ui.tablet.AnchorState;
import com.senku.ui.tablet.SourceState;
import com.senku.ui.tablet.Status;
import com.senku.ui.tablet.ThreadTurnState;
import com.senku.ui.tablet.XRefState;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class DetailTabletStateBuilderTest {
    @Test
    public void sourceStatesTrimLabelsAndMarkVisualOwner() {
        SearchResult abrasives = source(" GD-220 ", " Abrasives Manufacturing ", "Materials");
        SearchResult shelter = source("GD-345", "Rain Shelter", "Rigging");

        List<SourceState> states = DetailTabletStateBuilder.buildSourceStates(
            List.of(abrasives, shelter),
            source("gd-345", "Rain Shelter", "rigging"),
            source("GD-345", "Rain Shelter", "Rigging")
        );

        assertEquals(2, states.size());
        assertEquals("GD-220", states.get(0).getId());
        assertEquals("Abrasives Manufacturing", states.get(0).getTitle());
        assertFalse(states.get(0).isAnchor());
        assertFalse(states.get(0).isSelected());
        assertEquals("gd-345|rigging|rain shelter", states.get(1).getKey());
        assertTrue(states.get(1).isAnchor());
        assertTrue(states.get(1).isSelected());
    }

    @Test
    public void sourceStatesCanRepresentDifferentAnchorAndSelectedRows() {
        SearchResult anchor = source("GD-220", "Abrasives Manufacturing", "Materials");
        SearchResult selected = source("GD-132", "Foundry & Metal Casting", "Casting");

        List<SourceState> states = DetailTabletStateBuilder.buildSourceStates(
            List.of(anchor, selected),
            anchor,
            selected
        );

        assertTrue(states.get(0).isAnchor());
        assertFalse(states.get(0).isSelected());
        assertFalse(states.get(1).isAnchor());
        assertTrue(states.get(1).isSelected());
    }

    @Test
    public void blankSelectionKeysDoNotMarkBlankSourceRows() {
        List<SourceState> states = DetailTabletStateBuilder.buildSourceStates(
            List.of(new SearchResult(" ", "", "", "", "", " ", "", "")),
            null,
            null
        );

        assertEquals(1, states.size());
        assertEquals("||", states.get(0).getKey());
        assertFalse(states.get(0).isAnchor());
        assertFalse(states.get(0).isSelected());
    }

    @Test
    public void nullSourceRowsUseBlankLabelsAndUnselectedState() {
        List<SourceState> states = DetailTabletStateBuilder.buildSourceStates(
            Arrays.asList((SearchResult) null),
            null,
            null
        );

        assertEquals(1, states.size());
        assertEquals("", states.get(0).getKey());
        assertEquals("", states.get(0).getId());
        assertEquals("", states.get(0).getTitle());
        assertFalse(states.get(0).isAnchor());
        assertFalse(states.get(0).isSelected());
    }

    @Test
    public void xrefStatesTrimLabelsAndPreserveRelationBuckets() {
        List<XRefState> states = DetailTabletStateBuilder.buildXRefStates(List.of(
            new SearchResult(
                " Required prep ",
                "",
                "",
                "",
                " GD-499 ",
                "",
                "",
                "",
                "topic",
                "",
                "",
                ""
            ),
            new SearchResult(
                " Reviewed source ",
                "",
                "",
                "",
                " GD-222 ",
                "",
                "",
                "",
                "reviewed_source_anchor",
                "",
                "",
                ""
            ),
            source(" GD-333 ", " Related guide ", "")
        ));

        assertEquals(3, states.size());
        assertEquals("GD-499", states.get(0).getId());
        assertEquals("Required prep", states.get(0).getTitle());
        assertEquals("REQUIRED", states.get(0).getRelation());
        assertEquals("ANCHOR", states.get(1).getRelation());
        assertEquals("RELATED", states.get(2).getRelation());
    }

    @Test
    public void xrefStatesAllowNullAndEmptyInput() {
        List<XRefState> emptyStates = DetailTabletStateBuilder.buildXRefStates(null);
        List<XRefState> nullRowStates = DetailTabletStateBuilder.buildXRefStates(
            Arrays.asList((SearchResult) null)
        );

        assertTrue(emptyStates.isEmpty());
        assertEquals(1, nullRowStates.size());
        assertEquals("", nullRowStates.get(0).getId());
        assertEquals("", nullRowStates.get(0).getTitle());
        assertEquals("RELATED", nullRowStates.get(0).getRelation());
    }

    @Test
    public void turnStatesPreserveBindingFieldsAndMarkActiveById() {
        AnswerContent doneAnswer = answer("Earlier answer");
        AnswerContent activeAnswer = answer("Current answer");
        DetailActivity.TabletTurnBinding earlier = turn(
            "T1",
            "How do I prep?",
            doneAnswer,
            Status.Done,
            true
        );
        DetailActivity.TabletTurnBinding current = turn(
            "T2",
            "What next?",
            activeAnswer,
            Status.Pending,
            false
        );

        List<ThreadTurnState> states = DetailTabletStateBuilder.buildTurnStates(
            List.of(earlier, current),
            current
        );

        assertEquals(2, states.size());
        assertEquals("T1", states.get(0).getId());
        assertEquals("How do I prep?", states.get(0).getQuestion());
        assertSame(doneAnswer, states.get(0).getAnswer());
        assertEquals(Status.Done, states.get(0).getStatus());
        assertFalse(states.get(0).isActive());
        assertTrue(states.get(0).getShowQuestion());
        assertEquals("T2", states.get(1).getId());
        assertSame(activeAnswer, states.get(1).getAnswer());
        assertEquals(Status.Pending, states.get(1).getStatus());
        assertTrue(states.get(1).isActive());
        assertFalse(states.get(1).getShowQuestion());
    }

    @Test
    public void turnStatesUseActiveTurnIdRatherThanObjectIdentity() {
        DetailActivity.TabletTurnBinding current = turn(
            "T2",
            "What next?",
            answer("Current answer"),
            Status.Active,
            true
        );
        DetailActivity.TabletTurnBinding sameIdSelection = turn(
            "T2",
            "Different object",
            answer("Selection answer"),
            Status.Done,
            false
        );

        List<ThreadTurnState> states = DetailTabletStateBuilder.buildTurnStates(
            List.of(current),
            sameIdSelection
        );

        assertEquals(1, states.size());
        assertTrue(states.get(0).isActive());
    }

    @Test
    public void turnStatesAllowMissingInputAndNoActiveTurn() {
        List<ThreadTurnState> emptyStates = DetailTabletStateBuilder.buildTurnStates(null, null);
        List<ThreadTurnState> inactiveStates = DetailTabletStateBuilder.buildTurnStates(
            List.of(turn("T1", "Guide", answer("Guide body"), Status.Active, false)),
            null
        );

        assertTrue(emptyStates.isEmpty());
        assertEquals(1, inactiveStates.size());
        assertFalse(inactiveStates.get(0).isActive());
        assertFalse(inactiveStates.get(0).getShowQuestion());
    }

    @Test
    public void visualOwnerUsesGenericQuestionSourceOverlap() {
        SearchResult abrasives = topicSource(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );
        SearchResult kiln = topicSource(
            "GD-812",
            "Clay Kiln Firing",
            "clay kiln firing masonry"
        );

        SearchResult owner = DetailTabletStateBuilder.resolveVisualOwnerSource(
            true,
            false,
            false,
            List.of("How should I fire a clay kiln after the masonry dries?"),
            abrasives,
            List.of(abrasives, kiln)
        );

        assertEquals("GD-812", owner.guideId);
    }

    @Test
    public void visualOwnerKeepsExplicitSelection() {
        SearchResult abrasives = topicSource(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );
        SearchResult shelter = topicSource(
            "GD-345",
            "Rain shelter in wet weather",
            "field shelter rain tarp cord"
        );

        SearchResult owner = DetailTabletStateBuilder.resolveVisualOwnerSource(
            true,
            true,
            false,
            List.of("How do I build a simple rain shelter from tarp and cord?"),
            abrasives,
            List.of(abrasives, shelter)
        );

        assertEquals("GD-220", owner.guideId);
    }

    @Test
    public void visualOwnerReturnsNullWhenThreadHasNoSourceMatch() {
        SearchResult abrasives = topicSource(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );

        SearchResult owner = DetailTabletStateBuilder.resolveVisualOwnerSource(
            true,
            false,
            false,
            List.of("How do I plan the calendar?"),
            abrasives,
            List.of(abrasives)
        );

        assertNull(owner);
    }

    @Test
    public void anchorStateUsesEvidenceProjectionWhenSelectionMatches() {
        SearchResult activeSource = source(" GD-220 ", " Abrasives Manufacturing ", " Materials ");

        AnchorState state = DetailTabletStateBuilder.buildAnchorState(
            true,
            "",
            activeSource,
            "gd-220|materials|abrasives manufacturing",
            " GD-220A ",
            " Loaded abrasives guide ",
            " Loaded section ",
            " Use the finer grit last. "
        );

        assertEquals("gd-220|materials|abrasives manufacturing", state.getKey());
        assertEquals("GD-220A", state.getId());
        assertEquals("Loaded abrasives guide", state.getTitle());
        assertEquals("Loaded section", state.getSection());
        assertEquals("Use the finer grit last.", state.getSnippet());
        assertTrue(state.getHasSource());
    }

    @Test
    public void guideModeStateUsesAnswerSourceGuideId() {
        DetailTabletStateBuilder.GuideModeState state =
            DetailTabletStateBuilder.buildGuideModeState(
                true,
                false,
                "Guide",
                "Guide summary",
                "Guide anchor",
                source(" GD-345 ", " Rain Shelter ", " Rigging "),
                List.of(turn("T1", "How do I build a rain shelter?", answer("Use tarp."), Status.Active, true)),
                "Rain shelter"
            );

        assertEquals("ANSWER", state.label);
        assertEquals("GD-345", state.summary);
        assertEquals("Sources", state.anchorLabel);
    }

    @Test
    public void guideModeStateUsesThreadDistinctSourceCount() {
        DetailActivity.TabletTurnBinding first = turnWithSources(
            "T1",
            "How do I build a rain shelter?",
            List.of(
                source("GD-345", "Rain Shelter", "Rigging"),
                source("GD-345", "Rain Shelter", "Rigging")
            )
        );
        DetailActivity.TabletTurnBinding second = turnWithSources(
            "T2",
            "What cord should I use?",
            List.of(source("GD-220", "Abrasives Manufacturing", "Materials"))
        );

        DetailTabletStateBuilder.GuideModeState state =
            DetailTabletStateBuilder.buildGuideModeState(
                true,
                true,
                "",
                "",
                "",
                source("GD-345", "Rain Shelter", "Rigging"),
                List.of(first, second),
                "Rain shelter - 2 turns"
            );

        assertEquals("THREAD", state.label);
        assertEquals("Sources in thread - 2", state.summary);
        assertEquals("Thread sources", state.anchorLabel);
    }

    @Test
    public void guideModeStatePreservesGuideHandoffLabels() {
        DetailTabletStateBuilder.GuideModeState state =
            DetailTabletStateBuilder.buildGuideModeState(
                false,
                true,
                " Cross-reference ",
                " Opened from GD-220. ",
                " GD-220 \u00b7 Abrasives ",
                source("GD-345", "Rain Shelter", "Rigging"),
                List.of(turn("T1", "Guide", answer("Body"), Status.Active, true)),
                ""
            );

        assertEquals(" Cross-reference ", state.label);
        assertEquals(" Opened from GD-220. ", state.summary);
        assertEquals(" GD-220 \u00b7 Abrasives ", state.anchorLabel);
    }

    private static SearchResult source(String guideId, String title, String section) {
        return new SearchResult(title, "", "", "", guideId, section, "", "");
    }

    private static SearchResult topicSource(String guideId, String title, String topicTags) {
        return new SearchResult(title, "", "", "", guideId, "", "", "", "", "", "", topicTags);
    }

    private static DetailActivity.TabletTurnBinding turn(
        String id,
        String question,
        AnswerContent answer,
        Status status,
        boolean showQuestion
    ) {
        return new DetailActivity.TabletTurnBinding(
            id,
            question,
            answer,
            status,
            showQuestion,
            new ArrayList<>()
        );
    }

    private static DetailActivity.TabletTurnBinding turnWithSources(
        String id,
        String question,
        List<SearchResult> sources
    ) {
        return new DetailActivity.TabletTurnBinding(
            id,
            question,
            answer("Answer"),
            Status.Active,
            true,
            new ArrayList<>(sources)
        );
    }

    private static AnswerContent answer(String body) {
        return AnswerContentFactory.fromRenderedAnswer(
            body,
            1,
            "",
            0.0,
            Evidence.Moderate,
            false
        );
    }
}

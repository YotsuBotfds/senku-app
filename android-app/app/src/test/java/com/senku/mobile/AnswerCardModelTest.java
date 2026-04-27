package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class AnswerCardModelTest {
    @Test
    public void answerCardNormalizesNullStringsAndLists() {
        AnswerCard card = new AnswerCard(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertEquals("", card.cardId);
        assertEquals("", card.guideId);
        assertEquals("", card.slug);
        assertEquals("", card.title);
        assertEquals("", card.riskTier);
        assertEquals("", card.evidenceOwner);
        assertEquals("", card.reviewStatus);
        assertEquals("", card.runtimeCitationPolicy);
        assertEquals("", card.routineBoundary);
        assertEquals("", card.acceptableUncertainFit);
        assertEquals("", card.notes);
        assertTrue(card.clauses.isEmpty());
        assertTrue(card.sources.isEmpty());
    }

    @Test
    public void answerCardCopiesAndFreezesClauseAndSourceLists() {
        ArrayList<AnswerCardClause> clauses = new ArrayList<>();
        clauses.add(new AnswerCardClause("card", "step", 1, "Do this", List.of("do")));
        ArrayList<AnswerCardSource> sources = new ArrayList<>();
        sources.add(new AnswerCardSource("card", "GD-001", "", "", List.of("A"), true));

        AnswerCard card = new AnswerCard(
            "card",
            "GD-001",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            clauses,
            sources
        );
        clauses.clear();
        sources.clear();

        assertEquals(1, card.clauses.size());
        assertEquals(1, card.sources.size());
        assertThrows(UnsupportedOperationException.class, () ->
            card.clauses.add(new AnswerCardClause("", "", 0, "", List.of()))
        );
        assertThrows(UnsupportedOperationException.class, () ->
            card.sources.add(new AnswerCardSource("", "", "", "", List.of(), false))
        );
    }

    @Test
    public void clauseAndSourceNormalizeNullsAndFreezeNestedLists() {
        AnswerCardClause emptyClause = new AnswerCardClause(null, null, 2, null, null);
        AnswerCardSource emptySource = new AnswerCardSource(null, null, null, null, null, false);

        assertEquals("", emptyClause.cardId);
        assertEquals("", emptyClause.clauseKind);
        assertEquals("", emptyClause.text);
        assertTrue(emptyClause.triggerTerms.isEmpty());
        assertEquals("", emptySource.cardId);
        assertEquals("", emptySource.sourceGuideId);
        assertEquals("", emptySource.slug);
        assertEquals("", emptySource.title);
        assertTrue(emptySource.sections.isEmpty());

        ArrayList<String> triggerTerms = new ArrayList<>(List.of("bleeding"));
        ArrayList<String> sections = new ArrayList<>(List.of("Triage"));
        AnswerCardClause clause = new AnswerCardClause("card", "step", 1, "Do this", triggerTerms);
        AnswerCardSource source = new AnswerCardSource("card", "GD-001", "", "", sections, true);
        triggerTerms.clear();
        sections.clear();

        assertEquals(List.of("bleeding"), clause.triggerTerms);
        assertEquals(List.of("Triage"), source.sections);
        assertThrows(UnsupportedOperationException.class, () -> clause.triggerTerms.add("pain"));
        assertThrows(UnsupportedOperationException.class, () -> source.sections.add("Other"));
    }
}

package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class PromptBuilderTest {
    @Test
    public void followUpPromptIncludesDirectFollowUpGuidance() {
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            "what about sealing it",
            List.of(
                new SearchResult(
                    "Small Watercraft Construction",
                    "",
                    "Seal seams with pitch or resin after shaping the hull.",
                    "Seal seams with pitch or resin after shaping the hull.",
                    "GD-682",
                    "Sealing and Finishing",
                    "transportation",
                    "route-focus"
                )
            ),
            "recent questions: how do i build a canoe | recent guides: [GD-682] Small Watercraft Construction"
        );

        assertTrue(prompt.contains("Answer the newest follow-up question directly"));
        assertTrue(prompt.contains("Only bring forward earlier steps"));
        assertTrue(prompt.contains("Keep the answer scoped to the same resource"));
        assertTrue(prompt.contains("Metadata: anchor note | category=transportation"));
    }

    @Test
    public void standalonePromptOmitsFollowUpGuidance() {
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            "how do i build a canoe",
            List.of(),
            ""
        );

        assertFalse(prompt.contains("Answer the newest follow-up question directly"));
    }

    @Test
    public void continuationPromptIncludesAdvanceInstruction() {
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            "what next",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "After site selection, start laying out the foundation footprint.",
                    "After site selection, start laying out the foundation footprint.",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus"
                )
            ),
            "recent questions: how do i build a house | latest answer: Start with site assessment and drainage."
        );

        assertTrue(prompt.contains("advance one stage past the latest answer"));
        assertTrue(prompt.contains("Do not repeat the same stage"));
        assertTrue(prompt.contains("Do not broaden a follow-up into generic advice"));
    }

    @Test
    public void seasonalSiteFollowUpPromptUsesContextualRoutingAndKeepsFourthNote() {
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            "what about winter sun and summer shade?",
            List.of(
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "Use wind exposure and terrain to shape site layout.",
                    "Use wind exposure and terrain to shape site layout.",
                    "GD-446",
                    "Wind Exposure and Microclimate",
                    "survival",
                    "guide-focus"
                ),
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "Assess terrain and drainage before building.",
                    "Assess terrain and drainage before building.",
                    "GD-446",
                    "Terrain Analysis",
                    "survival",
                    "guide-focus"
                ),
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "Use a checklist to review access and hazards.",
                    "Use a checklist to review access and hazards.",
                    "GD-446",
                    "Site Assessment Checklist",
                    "survival",
                    "guide-focus"
                ),
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "Capture winter solar gain and summer shade when orienting the shelter.",
                    "Capture winter solar gain and summer shade when orienting the shelter.",
                    "GD-446",
                    "Seasonal Considerations",
                    "survival",
                    "guide-focus"
                )
            ),
            "recent questions: How do I choose a building site if drainage, wind, sun, and access all matter?",
            "winter sun summer shade site selection wind exposure microclimate cabin"
        );

        assertTrue(prompt.contains("answer the specific siting tradeoff first"));
        assertTrue(prompt.contains("winter solar gain, summer shade, wind exposure, and orientation"));
        assertTrue(prompt.contains("[4] [GD-446] Shelter Site Selection & Hazard Assessment / Seasonal Considerations"));
    }

    @Test
    public void systemPromptCarriesCoreOfflineInstructions() {
        String systemPrompt = PromptBuilder.buildOfflineAnswerSystemPrompt(
            "what's the safest way to store treated water long term"
        );

        assertTrue(systemPrompt.contains("You are Senku, an offline field-guide assistant."));
        assertTrue(systemPrompt.contains("Use only the retrieved notes below."));
        assertTrue(systemPrompt.contains("Cite guide IDs in square brackets"));
        assertTrue(systemPrompt.contains("matching structure/topic tags as stronger evidence"));
        assertTrue(systemPrompt.contains("For water storage prompts"));
    }

    @Test
    public void promptIncludesCompactContextMetadataHints() {
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            "how do i choose a safe site and foundation for a small cabin",
            List.of(
                new SearchResult(
                    "Shelter Site Selection & Hazard Assessment",
                    "",
                    "Assess drainage, slope, wind, and access before excavation.",
                    "Assess drainage, slope, wind, and access before excavation.",
                    "GD-446",
                    "Site Assessment Checklist",
                    "survival",
                    "guide-focus",
                    "starter_path",
                    "long_term",
                    "cabin_house",
                    "site_selection,drainage,foundation,wind_exposure"
                ),
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "Set out footings only after the site drains cleanly.",
                    "Set out footings only after the site drains cleanly.",
                    "GD-094",
                    "Foundations",
                    "building",
                    "guide-focus",
                    "subsystem",
                    "long_term",
                    "cabin_house",
                    "foundation,drainage,footings"
                )
            ),
            ""
        );

        assertTrue(prompt.contains("Metadata: anchor note | category=survival | role=starter path | horizon=long term | structure=cabin house | topics=site selection, drainage, foundation"));
        assertTrue(prompt.contains("Metadata: support note | category=building | role=subsystem | horizon=long term | structure=cabin house | topics=foundation, drainage, footings"));
    }

    @Test
    public void governancePromptUsesSummaryKeyPointsFormat() {
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            "how do we merge with another group if we don't trust each other yet?",
            List.of(),
            ""
        );

        assertTrue(prompt.contains("Return only these labels once: Summary, Key points, Risks or limits."));
        assertTrue(prompt.contains("Summary:\n"));
        assertTrue(prompt.contains("Key points:\n"));
        assertTrue(prompt.contains("Risks or limits:\n"));
        assertFalse(prompt.contains("Short answer:\n"));
    }

    @Test
    public void answerBodyNormalizesSummaryKeyPointsSections() {
        String body = PromptBuilder.buildAnswerBody(
            "Summary: Start with shared rules and reversible joint work.\n\n" +
                "Key points:\n1. Set limited shared tasks.\n2. Use transparent records.\n\n" +
                "Risks or limits: Avoid forced full merger before trust improves.",
            List.of(),
            0
        );

        assertTrue(body.contains("Summary: Start with shared rules and reversible joint work."));
        assertTrue(body.contains("Key points:\n1. Set limited shared tasks."));
        assertTrue(body.contains("Risks or limits:\nAvoid forced full merger before trust improves."));
    }

    @Test
    public void promptClipPrefersSentenceBoundaryWhenExcerptWouldCutMidSentence() {
        String prompt = PromptBuilder.buildOfflineAnswerPrompt(
            "how do i waterproof a roof with bark and clay",
            List.of(
                new SearchResult(
                    "Construction & Carpentry",
                    "",
                    "",
                    "Start with a steep roof pitch so water sheds quickly, overlap bark courses generously from the eaves upward, seal the ridge cap carefully, and keep the roof layout simple enough that runoff can leave the surface without pooling anywhere on the deck. "
                        + "This follow-on sentence is intentionally long and should not appear in partial form because the prompt excerpt should stop on the earlier sentence boundary instead of cutting this sentence halfway through for the model.",
                    "GD-094",
                    "Roof Waterproofing",
                    "building",
                    "guide-focus",
                    "starter_path",
                    "long_term",
                    "cabin_house",
                    "roofing,weatherproofing"
                )
            ),
            ""
        );

        assertTrue(prompt.contains("Metadata: anchor note | category=building | role=starter path | horizon=long term | structure=cabin house | topics=roofing, weatherproofing"));
        assertTrue(prompt.contains("Note: Start with a steep roof pitch so water sheds quickly, overlap bark courses generously from the eaves upward, seal the ridge cap carefully, and keep the roof layout simple enough that runoff can leave the surface without pooling anywhere on the deck."));
        assertFalse(prompt.contains("This follow-on sentence is intentionally long"));
    }
}

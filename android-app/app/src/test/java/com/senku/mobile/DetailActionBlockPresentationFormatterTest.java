package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailActionBlockPresentationFormatterTest {
    private final DetailCitationPresentationFormatter citationFormatter =
        new DetailCitationPresentationFormatter(null);

    @Test
    public void extractHighRiskActionBlockSpecsKeepsEmergencyLabelsDistinct() {
        List<DetailActionBlockPresentationFormatter.ActionBlockSpec> blocks = extract(
            "Short answer:\n" +
                "Treat this as urgent.\n\n" +
                "Steps:\n" +
                "1. Apply direct pressure with a clean cloth [GD-232].\n" +
                "2. Stabilize the area, but do not remove a deeply embedded object [GD-284].\n" +
                "3. Escalate if bleeding soaks through or red streaking appears [GD-298]."
        );

        assertEquals(3, blocks.size());
        assertBlock(
            blocks.get(0),
            DetailActionBlockPresentationFormatter.ActionBlockKind.DO_FIRST,
            "Do first",
            "Apply direct pressure with a clean cloth."
        );
        assertBlock(
            blocks.get(1),
            DetailActionBlockPresentationFormatter.ActionBlockKind.AVOID,
            "Avoid",
            "do not remove a deeply embedded object."
        );
        assertBlock(
            blocks.get(2),
            DetailActionBlockPresentationFormatter.ActionBlockKind.ESCALATE,
            "Escalate if",
            "Escalate if bleeding soaks through or red streaking appears."
        );
    }

    @Test
    public void extractHighRiskActionBlockSpecsSanitizesWarningResidualsAndGuideCitations() {
        List<DetailActionBlockPresentationFormatter.ActionBlockSpec> blocks = extract(
                "Steps:\n" +
                    "1. Keep the person still [System Instruction] [GD-232].\n" +
                    "2. Never give food or drink [Caution] [GD-284].\n" +
                    "3. Seek medical help if fever develops [GD-298]."
        );

        assertBlockBodyContains(blocks.get(0), "Keep", "person", "still");
        assertBlockBodyContains(blocks.get(1), "Never", "food", "drink");
        assertBlockBodyContains(blocks.get(2), "Seek", "medical", "fever");
        assertBlockBodyExcludes(blocks.get(0), "[System Instruction]", "[GD-232]");
        assertBlockBodyExcludes(blocks.get(1), "[Caution]", "[GD-284]");
        assertBlockBodyExcludes(blocks.get(2), "[GD-298]");
    }

    @Test
    public void extractHighRiskActionBlockSpecsReturnsEmptyWhenNoNumberedStepsExist() {
        assertTrue(extract("Short answer: Call local emergency help when available.").isEmpty());
        assertTrue(extract(null).isEmpty());
    }

    @Test
    public void extractEmergencyActionSpecsKeepsAllImmediateStepsWithDetailCopy() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Short answer:\n" +
                    "Stop work immediately.\n\n" +
                    "Steps:\n" +
                    "1. Stop all hot work. No new charges, no new pours. [GD-132]\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress.\n" +
                    "4. Notify the area owner. GD-132 lists current owner.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work", "No new charges");
        assertActionContains(actions.get(1), "Clear", "5 m radius", "upwind");
        assertActionContains(actions.get(2), "Confirm", "two paths", "egress");
        assertEquals("", actions.get(2).detail);
    }

    @Test
    public void extractEmergencyActionSpecsIgnoresNumberedSourceContext() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Short answer:\n" +
                    "Stop work immediately.\n\n" +
                    "Immediate actions:\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress. Door and roll-up open and unobstructed.\n" +
                    "4. Notify the area owner. GD-132 lists current owner.\n\n" +
                    "Sources:\n" +
                    "1. GD-132 - Foundry & Metal Casting: Section 1 Area readiness.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertEquals("Notify the area owner", actions.get(3).title);
        assertEquals("GD-132 lists current owner.", actions.get(3).detail);
    }

    @Test
    public void extractEmergencyActionSpecsReadsFieldStepsFixtureBeforeWatchCopy() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Stop work immediately. Move to minimum 5 m from the active work zone. Confirm two paths of egress.\n\n" +
                    "FIELD STEPS\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to a 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress. Doors and roll-up openings must be unobstructed.\n" +
                    "4. Notify the area owner. GD-132 \u00a71 is current owner.\n\n" +
                    "WATCH\n" +
                    "Treat water near molten metal as an extreme burn hazard.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work", "No new charges");
        assertActionContains(actions.get(1), "Clear", "5 m radius", "upwind");
        assertActionContains(actions.get(2), "Confirm", "two paths", "Door", "unobstructed");
        assertActionContains(actions.get(3), "Notify", "area owner", "GD-132", "current owner");
        assertNoActionContains(actions, "WATCH", "burn hazard");
    }

    @Test
    public void extractEmergencyActionSpecsReadsFormattedStepsDisplayLabel() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "ANSWER\n" +
                    "Stop work immediately. Move to minimum 5 m from active work zone.\n\n" +
                    "STEPS\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress. Door and roll-up open and unobstructed.\n" +
                    "4. Notify the area owner. GD-132 lists current owner.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertActionContains(actions.get(2), "Door", "roll-up", "unobstructed");
        assertActionContains(actions.get(3), "GD-132", "current owner");
    }

    @Test
    public void extractEmergencyActionSpecsReadsNoColonImmediateActionHeadings() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> immediateActions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Stop work immediately.\n\n" +
                    "IMMEDIATE ACTIONS\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to a 5 m radius. Move personnel upwind.",
                text -> citationFormatter.stripInlineCitationText(text)
            );
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> emergencyActions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Stop work immediately.\n\n" +
                    "EMERGENCY ACTIONS\n" +
                    "1. Confirm two paths of egress. Door and roll-up open and unobstructed.\n" +
                    "2. Notify the area owner. GD-132 lists current owner.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(2, immediateActions.size());
        assertEquals("Stop all hot work", immediateActions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", immediateActions.get(1).title);
        assertEquals(2, emergencyActions.size());
        assertEquals("Confirm two paths of egress", emergencyActions.get(0).title);
        assertEquals("Notify the area owner", emergencyActions.get(1).title);
    }

    @Test
    public void extractEmergencyActionSpecsKeepsUppercaseFieldStepsAfterAnswerFormatting() {
        DetailAnswerBodyFormatter bodyFormatter = new DetailAnswerBodyFormatter(null);

        String formatted = bodyFormatter.formatAnswerBody(
            "Stop work immediately. Move to minimum 5 m from active work zone.\n\n" +
                    "FIELD STEPS\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to a 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress. Door and roll-up open and unobstructed.\n" +
                    "4. Notify the area owner. GD-132 lists current owner."
        );
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                formatted,
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertActionContains(actions.get(2), "Door", "roll-up", "unobstructed");
        assertActionContains(actions.get(3), "GD-132", "current owner");
    }

    @Test
    public void extractEmergencyActionSpecsRequiresActionSectionBeforeNumberedRows() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Short answer:\n" +
                    "Stop work immediately.\n\n" +
                    "Why this answer:\n" +
                    "1. GD-132 - Foundry & Metal Casting.\n" +
                    "2. Backend route reviewed_card_runtime.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertTrue(actions.isEmpty());
    }

    @Test
    public void extractEmergencyActionSpecsStopsBeforeProofBackendChrome() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n\n" +
                    "Backend route reviewed_card_runtime\n" +
                    "1. Route: deterministic.\n" +
                    "2. Model: local.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(2, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertNoActionContains(actions, "Backend route", "Route: deterministic", "Model: local");
    }

    @Test
    public void extractEmergencyActionSpecsKeepsOrderedActionsAheadOfEvidenceAndProvenance() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1) Stop all hot work. No new charges, no new pours.\n" +
                    "2) Clear the floor to 5 m radius. Move personnel upwind.\n" +
                    "3) Confirm two paths of egress. Door and roll-up open and unobstructed.\n" +
                    "4) Notify the area owner. GD-132 lists current owner.\n\n" +
                    "Evidence:\n" +
                    "1) GD-132 - Foundry & Metal Casting.\n\n" +
                    "Provenance:\n" +
                    "1) Reviewed card runtime.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertActionContains(actions.get(3), "Notify", "area owner", "GD-132", "current owner");
        assertNoActionContains(actions, "Evidence", "Provenance", "Reviewed card runtime");
    }

    @Test
    public void emergencyPresentationPolicySeparatesImmediateActionsFromProofBoundaries() {
        assertTrue(DetailEmergencyPresentationPolicy.isImmediateActionHeading("Immediate actions"));
        assertTrue(DetailEmergencyPresentationPolicy.isImmediateActionHeading("FIELD STEPS:"));
        assertTrue(DetailEmergencyPresentationPolicy.isImmediateActionHeading("ANSWER GD-132 - Burn hazard response"));
        assertFalse(DetailEmergencyPresentationPolicy.isImmediateActionHeading("Why this answer"));

        assertTrue(DetailEmergencyPresentationPolicy.isProofBoundaryHeading("Why this answer"));
        assertTrue(DetailEmergencyPresentationPolicy.isProofBoundaryHeading("Route, backend & proof"));
        assertTrue(DetailEmergencyPresentationPolicy.isProofBoundaryHeading("Guide connection | Show"));
        assertFalse(DetailEmergencyPresentationPolicy.isProofBoundaryHeading("Stop all hot work"));
    }

    @Test
    public void emergencyPresentationPolicyReturnsOnlySemanticImmediateActionRows() {
        List<String> actions = DetailEmergencyPresentationPolicy.extractImmediateActionLines(
            "Short answer:\n" +
                "Stop work immediately.\n\n" +
                "Immediate actions:\n" +
                "1. Stop all hot work. No new charges, no new pours.\n" +
                "2) Clear the floor to 5 m radius. Move personnel upwind.\n\n" +
                "Why this answer\n" +
                "1. GD-132 - Foundry & Metal Casting.\n" +
                "2. Backend route reviewed_card_runtime."
        );

        assertEquals(List.of(
            "Stop all hot work. No new charges, no new pours.",
            "Clear the floor to 5 m radius. Move personnel upwind."
        ), actions);
    }

    @Test
    public void emergencyPresentationPolicyHighRiskFallbackStopsBeforeProofRows() {
        List<String> fallbackActions = DetailEmergencyPresentationPolicy.extractHighRiskActionCandidateLines(
            "1. Apply direct pressure.\n" +
                "2. Do not remove a deeply embedded object.\n\n" +
                "Sources:\n" +
                "1. GD-232 - First aid."
        );
        List<String> boundedActions = DetailEmergencyPresentationPolicy.extractHighRiskActionCandidateLines(
            "Steps:\n" +
                "1. Apply direct pressure.\n\n" +
                "Sources:\n" +
                "1. GD-232 - First aid."
        );

        assertEquals(List.of(
            "Apply direct pressure.",
            "Do not remove a deeply embedded object."
        ), fallbackActions);
        assertEquals(List.of("Apply direct pressure."), boundedActions);
    }

    @Test
    public void extractEmergencyActionSpecsStopsBeforeGuideConnectionChrome() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n\n" +
                    "Guide connection | Show\n" +
                    "1. Linked guide GD-132.\n\n" +
                    "Route, backend & proof\n" +
                    "1. Backend route reviewed_card_runtime.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(2, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertNoActionContains(actions, "Guide connection", "Route", "backend");
    }

    @Test
    public void extractEmergencyActionSpecsStopsBeforeStatusAndRouteMetaChrome() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Emergency actions:\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n\n" +
                    "Answer status\n" +
                    "1. answered (ok), danger (danger).\n\n" +
                    "Route metadata\n" +
                    "1. reviewed_card_runtime proof route.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(2, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertNoActionContains(actions, "Answer status", "Route metadata", "reviewed_card_runtime");
    }

    @Test
    public void extractEmergencyActionSpecsStopsBeforeSourceWhyProofChrome() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n\n" +
                    "Source / why proof\n" +
                    "1. GD-132 - Foundry & Metal Casting.\n" +
                    "2. Backend route reviewed_card_runtime.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(2, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertNoActionContains(actions, "Source / why proof", "GD-132 - Foundry", "reviewed_card_runtime");
    }

    @Test
    public void extractEmergencyActionSpecsReadsGd132AnswerHeadingAsActions() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "ANSWER GD-132 - Burn hazard response\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to a 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress. Doors and roll-up openings must be unobstructed.\n" +
                    "4. Notify the area owner. GD-132 \u00a71 is current owner.\n\n" +
                    "Limits & safety:\n" +
                    "Treat water near molten metal as an extreme burn hazard.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertActionContains(actions.get(0), "Stop", "hot work");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertActionContains(actions.get(2), "Door", "roll-up", "unobstructed");
        assertActionContains(actions.get(3), "GD-132", "current owner");
    }

    @Test
    public void extractEmergencyActionSpecsNormalizesFoundryMockDetailCopy() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                    "Immediate actions:\n" +
                    "1. Clear the floor to a minimum 5 m radius. Move personnel upwind.\n" +
                    "2. Confirm two paths of egress. Doors and roll-up openings must be unobstructed.\n" +
                    "3. Notify the area owner. GD-132 \u00a71 is current owner.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(3, actions.size());
        assertActionContains(actions.get(0), "Clear", "5 m radius");
        assertActionContains(actions.get(1), "Door", "roll-up", "unobstructed");
        assertActionContains(actions.get(2), "GD-132", "current owner");
    }

    @Test
    public void extractEmergencyActionSpecsNormalizesBurnHazardMockVariants() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1. Move everyone to minimum 5 m from active work zone. Confirm two paths of egress.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n" +
                    "3. Notify the area owner. The guide lists the current owner.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(3, actions.size());
        assertActionContains(actions.get(0), "Move", "minimum 5 m", "active work zone");
        assertActionContains(actions.get(1), "Clear", "5 m radius");
        assertActionContains(actions.get(2), "guide", "current owner");
    }

    @Test
    public void extractEmergencyActionSpecsPreservesGenericOwnerGuideWording() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1. Notify the area owner. The guide lists the current owner.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(1, actions.size());
        assertEquals("Notify the area owner", actions.get(0).title);
        assertEquals("The guide lists the current owner.", actions.get(0).detail);
    }

    @Test
    public void extractEmergencyActionSpecsKeepsOwnerActionAndCompactOwnerDetail() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress. Door and roll-up open and unobstructed.\n" +
                    "4. Notify the area owner. GD-132 lists current owner.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(4, actions.size());
        assertActionContains(actions.get(3), "Notify", "area owner", "GD-132", "current owner");
    }

    @Test
    public void emergencyActionHeadingMatchesMockCopy() {
        assertEquals(
            "IMMEDIATE ACTIONS \u00b7 4",
            DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_HEADING_PREFIX + "4"
        );
    }

    @Test
    public void emergencyActionRowsKeepFlatPortraitMetrics() {
        assertEquals(5, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_ROW_VERTICAL_PADDING_DP);
        assertEquals(12, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_CONTENT_START_PADDING_DP);
        assertEquals(3, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_DETAIL_TOP_PADDING_DP);
        assertEquals(8.5f, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_HEADING_TEXT_SIZE_SP, 0.0f);
        assertEquals(13.5f, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_TITLE_TEXT_SIZE_SP, 0.0f);
        assertEquals(12.5f, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_DETAIL_TEXT_SIZE_SP, 0.0f);
        assertEquals(22, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_BADGE_SIZE_DP);
        assertEquals(2, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_TITLE_MAX_LINES);
        assertEquals(2, DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_DETAIL_MAX_LINES);
    }

    @Test
    public void emergencyGd132AnchorSourceLabelMatchesTargetCardCopy() {
        DetailSourcePresentationFormatter.EvidenceCard card =
            new DetailSourcePresentationFormatter.EvidenceCard(
                "GD-132",
                "ANCHOR",
                "82%",
                "Foundry & Metal Casting",
                "Reviewed Answer-Card Boundary, Foundry Safety Quickstart, Emergency Procedures",
                "Reviewed Answer-Card Boundary",
                true
            );

        String label = DetailActivity.buildPhonePortraitSourceCardLabel(card, true);

        assertTrue(label.contains("GD-132 \u2022 ANCHOR    93%"));
        assertTrue(label.contains("Foundry & Metal Casting \u00b7 \u00a71 Area readiness"));
        assertTrue(label.contains("A single drop of water contacting molten metal causes a violent steam explosion"));
        assertEquals(
            "GD-132 \u2022 ANCHOR    93%",
            DetailActivity.buildEmergencyProofMetaLabel(card, "")
        );
    }

    @Test
    public void gd132PhonePortraitSourceLabelOnlyUsesEmergencyCopyInEmergencyContext() {
        DetailSourcePresentationFormatter.EvidenceCard card =
            new DetailSourcePresentationFormatter.EvidenceCard(
                "GD-132",
                "RELATED",
                "68%",
                "Foundry & Metal Casting",
                "Keep water, damp charge, and bystanders out of the pour zone.",
                "Reviewed Answer-Card Boundary",
                false
            );

        String label = DetailActivity.buildPhonePortraitSourceCardLabel(card);

        assertTrue(label.contains("GD-132 \u2022 RELATED \u2022 68%"));
        assertTrue(label.contains("Foundry & Metal Casting"));
        assertFalse(label.contains("violent steam explosion"));
    }

    @Test
    public void styleEmergencyMinimumDistanceUnderlinesEmergencyDistancePhrases() {
        assertTrue(DetailActionBlockPresentationFormatter.shouldStyleEmergencyMinimumDistance(
            "Move to minimum 5 m from the active work zone."
        ));
        assertTrue(DetailActionBlockPresentationFormatter.shouldStyleEmergencyMinimumDistance(
            "Move to minimum 5 meters from the active work zone."
        ));
        assertTrue(DetailActionBlockPresentationFormatter.shouldStyleEmergencyMinimumDistance(
            "Move to minimum five meters from the active work zone."
        ));
        assertTrue(DetailActionBlockPresentationFormatter.shouldStyleEmergencyMinimumDistance(
            "Move to minimum 5 metres from the active work zone."
        ));
    }

    @Test
    public void styleEmergencyMinimumDistanceTargetsMockPhraseAndColor() {
        String text = "Stop work immediately. Move to minimum 5 m from active work zone. Confirm two paths of egress.";
        int[] range = DetailActionBlockPresentationFormatter.emergencyMinimumDistanceSpanRangeForTest(text);

        assertEquals(DetailActionBlockPresentationFormatter.EMERGENCY_DISTANCE_HIGHLIGHT_COLOR, 0xFFC4704B);
        assertEquals(
            "minimum 5 m from active work zone",
            text.substring(range[0], range[1])
        );
    }

    @Test
    public void styleEmergencyMinimumDistanceLeavesTargetRadiusActionPlain() {
        String text = "Clear the floor to 5 m radius.";

        assertTrue(!DetailActionBlockPresentationFormatter.shouldStyleEmergencyMinimumDistance(
            text
        ));
        assertEquals(text, DetailActionBlockPresentationFormatter.styleEmergencyMinimumDistance(text).toString());
    }

    @Test
    public void styleEmergencyMinimumDistanceLeavesNegativeControlPlain() {
        CharSequence styled = DetailActionBlockPresentationFormatter.styleEmergencyMinimumDistance(
            "Confirm two paths of egress."
        );

        assertEquals("Confirm two paths of egress.", styled.toString());
    }

    @Test
    public void extractEmergencyActionSpecsCapsOnlyAtRenderContractButKeepsParsedActions() {
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions =
            DetailActionBlockPresentationFormatter.extractEmergencyActionSpecs(
                "Immediate actions:\n" +
                    "1. Stop all hot work. No new charges, no new pours.\n" +
                    "2. Clear the floor to 5 m radius. Move personnel upwind.\n" +
                    "3. Confirm two paths of egress. Door and roll-up open and unobstructed.\n" +
                    "4. Notify the area owner. GD-132 lists current owner.\n" +
                    "5. Document the pause for handoff.",
                text -> citationFormatter.stripInlineCitationText(text)
            );

        assertEquals(5, actions.size());
        assertActionContains(actions.get(4), "Document", "pause", "handoff");
    }

    private List<DetailActionBlockPresentationFormatter.ActionBlockSpec> extract(String formattedAnswerText) {
        return DetailActionBlockPresentationFormatter.extractHighRiskActionBlockSpecs(
            formattedAnswerText,
            text -> citationFormatter.stripInlineCitationText(text)
        );
    }

    private static void assertBlock(
        DetailActionBlockPresentationFormatter.ActionBlockSpec block,
        DetailActionBlockPresentationFormatter.ActionBlockKind kind,
        String label,
        String body
    ) {
        assertEquals(kind, block.kind);
        assertEquals(label, block.label);
        assertEquals(body, block.body);
    }

    private static void assertBlockBodyContains(
        DetailActionBlockPresentationFormatter.ActionBlockSpec block,
        String... tokens
    ) {
        assertTextContains(block.body, tokens);
    }

    private static void assertBlockBodyExcludes(
        DetailActionBlockPresentationFormatter.ActionBlockSpec block,
        String... tokens
    ) {
        assertTextExcludes(block.body, tokens);
    }

    private static void assertActionContains(
        DetailActionBlockPresentationFormatter.EmergencyActionSpec action,
        String... tokens
    ) {
        assertTextContains(action.title + " " + action.detail, tokens);
    }

    private static void assertNoActionContains(
        List<DetailActionBlockPresentationFormatter.EmergencyActionSpec> actions,
        String... tokens
    ) {
        StringBuilder combined = new StringBuilder();
        for (DetailActionBlockPresentationFormatter.EmergencyActionSpec action : actions) {
            combined.append(action.title).append(' ').append(action.detail).append('\n');
        }
        assertTextExcludes(combined.toString(), tokens);
    }

    private static void assertTextContains(String text, String... tokens) {
        for (String token : tokens) {
            assertTrue("Expected text to contain <" + token + "> but was <" + text + ">",
                text.contains(token));
        }
    }

    private static void assertTextExcludes(String text, String... tokens) {
        for (String token : tokens) {
            assertFalse("Expected text to exclude <" + token + "> but was <" + text + ">",
                text.contains(token));
        }
    }
}

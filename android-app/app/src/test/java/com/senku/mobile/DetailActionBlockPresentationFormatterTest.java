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

        assertEquals("Keep the person still.", blocks.get(0).body);
        assertEquals("Never give food or drink.", blocks.get(1).body);
        assertEquals("Seek medical help if fever develops.", blocks.get(2).body);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("No new charges, no new pours.", actions.get(0).detail);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
        assertEquals("Move personnel upwind.", actions.get(1).detail);
        assertEquals("Confirm two paths of egress", actions.get(2).title);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("No new charges, no new pours.", actions.get(0).detail);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
        assertEquals("Move personnel upwind.", actions.get(1).detail);
        assertEquals("Confirm two paths of egress", actions.get(2).title);
        assertEquals("Door and roll-up open and unobstructed.", actions.get(2).detail);
        assertEquals("Notify the area owner", actions.get(3).title);
        assertEquals("GD-132 lists current owner.", actions.get(3).detail);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
        assertEquals("Door and roll-up open and unobstructed.", actions.get(2).detail);
        assertEquals("GD-132 lists current owner.", actions.get(3).detail);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
        assertEquals("Door and roll-up open and unobstructed.", actions.get(2).detail);
        assertEquals("GD-132 lists current owner.", actions.get(3).detail);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
        assertEquals("Notify the area owner", actions.get(3).title);
        assertEquals("GD-132 lists current owner.", actions.get(3).detail);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
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
        assertEquals("Stop all hot work", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
        assertEquals("Door and roll-up open and unobstructed.", actions.get(2).detail);
        assertEquals("GD-132 lists current owner.", actions.get(3).detail);
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
        assertEquals("Clear the floor to 5 m radius", actions.get(0).title);
        assertEquals("Door and roll-up open and unobstructed.", actions.get(1).detail);
        assertEquals("GD-132 lists current owner.", actions.get(2).detail);
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
        assertEquals("Move to minimum 5 m from active work zone", actions.get(0).title);
        assertEquals("Clear the floor to 5 m radius", actions.get(1).title);
        assertEquals("GD-132 lists current owner.", actions.get(2).detail);
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
        assertEquals("Notify the area owner", actions.get(3).title);
        assertEquals("GD-132 lists current owner.", actions.get(3).detail);
    }

    @Test
    public void emergencyActionHeadingMatchesMockCopy() {
        assertEquals(
            "IMMEDIATE ACTIONS \u00b7 4",
            DetailActionBlockPresentationFormatter.EMERGENCY_ACTION_HEADING_PREFIX + "4"
        );
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

        assertTrue(label.contains("GD-132 \u2022 ANCHOR \u2022 93%"));
        assertTrue(label.contains("Foundry & Metal Casting \u00b7 \u00a71 Area readiness"));
        assertTrue(label.contains("A single drop of water contacting molten metal causes a violent steam explosion"));
    }

    @Test
    public void gd132PhonePortraitSourceLabelOnlyUsesEmergencyCopyInEmergencyContext() {
        DetailSourcePresentationFormatter.EvidenceCard card =
            new DetailSourcePresentationFormatter.EvidenceCard(
                "GD-132",
                "RELATED",
                "68%",
                "Foundry & Metal Casting",
                "Pitch the ridgeline along prevailing wind.",
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
        assertEquals("Document the pause for handoff", actions.get(4).title);
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
}

package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailGuidePresentationFormatterTest {
    @Test
    public void buildGuideBodyReturnsEmptyForNullResult() {
        assertEquals("", DetailGuidePresentationFormatter.buildGuideBody(null));
    }

    @Test
    public void buildGuideBodyUsesSanitizedBodyBeforeSnippetAndPrependsSection() {
        SearchResult result = new SearchResult(
            "Water",
            "",
            "Snippet should not win",
            "## Safe storage\nUse [clean water](manual://water) [System Instruction].",
            "GD-214",
            "Storage",
            "water",
            "guide-focus"
        );

        assertEquals(
            "\u00a7 1 \u00b7 Storage\n\n\u00a7 2 \u00b7 Safe storage\nUse clean water.",
            DetailGuidePresentationFormatter.buildGuideBody(result)
        );
    }

    @Test
    public void buildGuideBodyAvoidsDuplicatingExistingOpeningSection() {
        SearchResult result = new SearchResult(
            "Water",
            "",
            "",
            "## Storage\nUse covered jars.",
            "GD-214",
            "Storage",
            "water",
            "guide-focus"
        );

        assertEquals(
            "\u00a7 1 \u00b7 Storage\nUse covered jars.",
            DetailGuidePresentationFormatter.buildGuideBody(result)
        );
    }

    @Test
    public void buildGuideBodyFallsBackToSanitizedSnippetWhenBodyBlank() {
        SearchResult result = new SearchResult(
            "Water",
            "",
            "Use **covered jars**<br>Rotate monthly.",
            "   ",
            "GD-214",
            "",
            "water",
            "guide-focus"
        );

        assertEquals("Use covered jars\nRotate monthly.", DetailGuidePresentationFormatter.buildGuideBody(result));
    }

    @Test
    public void guideReaderSpanColorResourcesUsePaperSafeRev03Tokens() {
        assertEquals(R.color.senku_rev03_paper_ink, DetailGuidePresentationFormatter.guideBodyTextColorResForLegacy());
        assertEquals(R.color.senku_rev03_paper_ok, DetailGuidePresentationFormatter.guideAnchorLabelColorResForLegacy());
        assertEquals(
            R.color.senku_rev03_paper_ink_muted,
            DetailGuidePresentationFormatter.guideAnchorValueColorResForLegacy()
        );
        assertEquals(
            R.color.senku_rev03_paper,
            DetailGuidePresentationFormatter.guideAdmonitionBackgroundColorResForLegacy()
        );
    }

    @Test
    public void guideAdmonitionSpanColorResourcesUsePaperSeverityTokens() {
        assertEquals(
            R.color.senku_rev03_paper_danger,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("DANGER: severe bleeding")
        );
        assertEquals(
            R.color.senku_rev03_paper_warn,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("WARNING: use ventilation")
        );
        assertEquals(
            R.color.senku_rev03_paper_warn,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("CAUTION: watch for cracks")
        );
        assertEquals(
            R.color.senku_rev03_paper_ok,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("NOTE: store dry")
        );
    }

    @Test
    public void guideBodySanitizerFormatsInlineAdmonitionsAsManualLabels() {
        assertEquals(
            "DANGER \u00b7 Extreme burn hazard\nKeep every tool dry.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "DANGER: Extreme burn hazard\nKeep every tool dry."
            )
        );
    }

    @Test
    public void guideBodySanitizerRemovesRawSectionMarkersAndCountsSections() {
        assertEquals(
            "\u00a7 1 \u00b7 Area readiness\n\nKeep water away.\n\n\u00a7 2 \u00b7 Foundry Safety Quickstart\nCheck tools.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "[[SECTION]] Area readiness\n\nKeep water away.\n\nSource section: Foundry Safety Quickstart\nCheck tools."
            )
        );
    }

    @Test
    public void guideBodySanitizerRemovesRawHtmlLinksAndImageBang() {
        assertEquals(
            "For air supply, see Bellows & Forge Blower Construction.\nFoundry diagram 1",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "For air supply, see <a href=\"../bellows.html\">Bellows &amp; Forge Blower Construction</a>.\n"
                    + "!Foundry diagram 1"
            )
        );
    }

    @Test
    public void guideBodySanitizerPreservesRequiredReadingAsCompactGuideRow() {
        assertEquals(
            "Required reading \u00b7 Before attempting this guide, read the Chemical Safety Guide in full.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "::: warning\nRequired Reading: Before attempting this guide, read the Chemical Safety Guide in full.\n:::"
            )
        );
    }

    @Test
    public void guideBodySanitizerPromotesAdmonitionTitleOutOfBodyCopy() {
        assertEquals(
            "DANGER \u00b7 EXTREME BURN HAZARD\nA single drop of water contacting molten metal causes a violent steam explosion.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "::: danger\nEXTREME BURN HAZARD: A single drop of water contacting molten metal causes a violent steam explosion.\n:::"
            )
        );
    }

    @Test
    public void guideBodySanitizerRemovesDuplicateInlineAdmonitionSeparator() {
        assertEquals(
            "DANGER \u00b7 EXTREME BURN HAZARD\nKeep every tool dry.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "DANGER \u00b7 EXTREME BURN HAZARD\nKeep every tool dry."
            )
        );
    }

    @Test
    public void guideBodySanitizerRemovesRepeatedDangerLabelFromManualCallout() {
        assertEquals(
            "DANGER \u00b7 EXTREME BURN HAZARD",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "DANGER: DANGER - EXTREME BURN HAZARD"
            )
        );
    }

    @Test
    public void guideBodySanitizerRemovesRepeatedRequiredReadingLabel() {
        assertEquals(
            "Required reading \u00b7 Read GD-220 first.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "Required Reading: Required reading - Read GD-220 first."
            )
        );
    }

    @Test
    public void guideBodySanitizerSplitsVerboseSectionTitlesIntoManualLabelAndHeading() {
        assertEquals(
            "\u00a7 1 \u00b7 Area readiness\nReviewed Answer-Card Boundary\nUse this section only for screening.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "## Section 1 Reviewed Answer-Card Boundary: Area Readiness, Hazard Screen, and Handoffs\n"
                    + "Use this section only for screening."
            )
        );
    }

    @Test
    public void guideBodySanitizerSkipsFrontMatterRulesMetadataAndDuplicateSectionRows() {
        assertEquals(
            "\u00a7 1 \u00b7 Storage\nKeep jars covered.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "---\n"
                    + "title: Water Storage\n"
                    + "tags: water, storage\n"
                    + "---\n"
                    + "## Storage\n"
                    + "Source section: Storage\n"
                    + "---\n"
                    + "Keep jars covered."
            )
        );
    }

    @Test
    public void guideBodySanitizerNormalizesMojibakeAndRepeatedSeparators() {
        assertEquals(
            "DANGER \u00b7 EXTREME BURN HAZARD",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay("DANGER \u00c2\u00b7 \u00c2\u00b7 EXTREME BURN HAZARD")
        );
    }

    @Test
    public void guideBodySanitizerKeepsCompactSectionLabelsIdempotentAndCleansMojibake() {
        assertEquals(
            "\u00a7 1 \u00b7 Area readiness\nKeep owner's notes clean.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "\u00c2\u00a7 1 \u00c2\u00b7 Area readiness\nKeep owner\u00e2\u20ac\u2122s notes clean."
            )
        );
    }

    @Test
    public void guideBodyParserExposesSectionAndRequiredReadingKindsWithoutDisplayMarkers() {
        GuideBodySanitizer.ParsedGuideBody parsed = GuideBodySanitizer.parseGuideBodyForDisplay(
            "[[SECTION]] Area readiness\nRequired Reading: Read GD-220 first."
        );

        assertEquals("\u00a7 1 \u00b7 Area readiness\nRequired reading \u00b7 Read GD-220 first.", parsed.displayText);
        assertEquals(2, parsed.lines.length);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.SECTION, parsed.lines[0].kind);
        assertEquals("\u00a7 1 \u00b7", parsed.lines[0].label);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.REQUIRED_READING, parsed.lines[1].kind);
        assertEquals("Required reading", parsed.lines[1].label);
    }

    @Test
    public void guideBodyParserExposesSplitHeadingKind() {
        GuideBodySanitizer.ParsedGuideBody parsed = GuideBodySanitizer.parseGuideBodyForDisplay(
            "## Section 1 Reviewed Answer-Card Boundary: Area Readiness, Hazard Screen, and Handoffs"
        );

        assertEquals("\u00a7 1 \u00b7 Area readiness\nReviewed Answer-Card Boundary", parsed.displayText);
        assertEquals(2, parsed.lines.length);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.SECTION, parsed.lines[0].kind);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.HEADING, parsed.lines[1].kind);
    }
}

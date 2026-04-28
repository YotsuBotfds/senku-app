package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
            "FIELD MANUAL \u00b7 REV 04-27 \u00b7 PK 2\n"
                + "Water\n"
                + "GD-214 \u00b7 2 SECTIONS\n\n"
                + "\u2014 \u00a7 1 \u00b7 STORAGE\n\n"
                + "\u2014 \u00a7 2 \u00b7 SAFE STORAGE\n"
                + "Use clean water.",
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
            "FIELD MANUAL \u00b7 REV 04-27 \u00b7 PK 2\n"
                + "Water\n"
                + "GD-214 \u00b7 1 SECTION\n\n"
                + "\u2014 \u00a7 1 \u00b7 STORAGE\n"
                + "Use covered jars.",
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

        assertEquals(
            "FIELD MANUAL \u00b7 REV 04-27 \u00b7 PK 2\n"
                + "Water\n"
                + "GD-214 \u00b7 1 SECTION\n\n"
                + "Use covered jars\n"
                + "Rotate monthly.",
            DetailGuidePresentationFormatter.buildGuideBody(result)
        );
    }

    @Test
    public void buildGuideBodyInfersRicherSectionCountFromFullGuideMetadata() {
        SearchResult result = new SearchResult(
            "Foundry & Metal Casting",
            "",
            "",
            "---\n"
                + "id: GD-132\n"
                + "related:\n"
                + "  - abrasives-manufacturing\n"
                + "  - bearing-manufacturing\n"
                + "  - bellows-forge-blower-construction\n"
                + "---\n"
                + ":::danger\n"
                + "EXTREME BURN HAZARD: A single drop of water contacting molten metal causes a violent steam explosion. EVERY tool, mold, crucible, and surface that contacts molten metal must be completely dry. Never cast alone.\n"
                + ":::\n"
                + "## Reviewed Answer-Card Boundary: Area Readiness, Hazard Screen, and Handoffs\n"
                + "This is the reviewed answer-card surface for GD-132. Use it only for foundry-area readiness, visible hazard screening, material and source labeling, no-go triggers, access control, and expert or owner handoff. Start with the current activity status.\n"
                + "## Foundry Safety Quickstart\n"
                + "Check the area.",
            "GD-132",
            "",
            "metalworking",
            "guide-focus"
        );

        assertEquals(
            "FIELD MANUAL \u00b7 REV 04-27 \u00b7 PK 2\n"
                + "Foundry & Metal Casting\n"
                + "GD-132 \u00b7 3 SECTIONS\n\n"
                + "DANGER \u00b7 EXTREME BURN HAZARD\n"
                + "A single drop of water contacting molten metal causes a violent steam explosion. EVERY tool, mold, crucible, and surface that contacts molten metal must be completely dry.\n"
                + "REQUIRED READING \u00b7 GD-220 \u00b7 Abrasives Manufacturing\n"
                + "REQUIRED READING \u00b7 GD-499 \u00b7 Bellows & Forge Blower Construction\n"
                + "\u2014 \u00a7 1 \u00b7 AREA READINESS\n"
                + "Reviewed Answer-Card Boundary\n"
                + "Use this section only for foundry-area readiness, visible hazard screening, material and source labeling, no-go triggers, access control, and expert or owner handoff.",
            DetailGuidePresentationFormatter.buildGuideBody(result)
        );
    }

    @Test
    public void buildGuideBodyUsesFrontMatterRelatedCountForMockParityHeader() {
        StringBuilder body = new StringBuilder();
        body.append("---\n")
            .append("id: GD-132\n")
            .append("related:\n");
        for (int i = 1; i <= 17; i++) {
            body.append("  - guide-").append(i).append('\n');
        }
        body.append("---\n")
            .append(":::danger\n")
            .append("EXTREME BURN HAZARD: Keep tools dry.\n")
            .append(":::\n");
        for (int i = 1; i <= 26; i++) {
            body.append("## Section ").append(i).append(" Mock heading\n")
                .append("Body ").append(i).append(".\n");
        }

        SearchResult result = new SearchResult(
            "Foundry & Metal Casting",
            "",
            "",
            body.toString(),
            "GD-132",
            "",
            "metalworking",
            "guide-focus"
        );

        String displayBody = DetailGuidePresentationFormatter.buildGuideBody(result);

        assertTrue(displayBody.contains("GD-132 \u00b7 17 SECTIONS"));
        assertFalse(displayBody.contains("GD-132 \u00b7 26 SECTIONS"));
    }

    @Test
    public void buildGuideBodyDoesNotUseRelatedCountForUnrelatedGuideHeaders() {
        SearchResult result = new SearchResult(
            "Water Storage",
            "",
            "",
            "---\n"
                + "id: GD-214\n"
                + "related:\n"
                + "  - clay-vessels\n"
                + "  - charcoal-filtering\n"
                + "  - boil-water\n"
                + "---\n"
                + "## Safe storage\n"
                + "Use covered jars.\n"
                + "## Rotation\n"
                + "Rotate monthly.",
            "GD-214",
            "",
            "water",
            "guide-focus"
        );

        String displayBody = DetailGuidePresentationFormatter.buildGuideBody(result);

        assertTrue(displayBody.contains("GD-214 \u00b7 2 SECTIONS"));
        assertFalse(displayBody.contains("GD-214 \u00b7 3 SECTIONS"));
    }

    @Test
    public void buildGuideBodyCountsFrontMatterRelatedEntriesWithWindowsLineEndings() {
        StringBuilder body = new StringBuilder();
        body.append("---\r\n")
            .append("id: GD-132\r\n")
            .append("related:\r\n");
        for (int i = 1; i <= 17; i++) {
            body.append("  - guide-").append(i).append("\r\n");
        }
        body.append("---\r\n");
        for (int i = 1; i <= 26; i++) {
            body.append("## Section ").append(i).append(" Mock heading\r\n")
                .append("Body ").append(i).append(".\r\n");
        }

        SearchResult result = new SearchResult(
            "Foundry & Metal Casting",
            "",
            "",
            body.toString(),
            "GD-132",
            "",
            "metalworking",
            "guide-focus"
        );

        String displayBody = DetailGuidePresentationFormatter.buildGuideBody(result);

        assertTrue(displayBody.contains("GD-132 \u00b7 17 SECTIONS"));
        assertFalse(displayBody.contains("GD-132 \u00b7 26 SECTIONS"));
    }

    @Test
    public void buildGuideBodyUsesFoundryLiveDefaultsWhenPackBodyLacksFrontMatter() {
        StringBuilder body = new StringBuilder();
        body.append(":::danger\n")
            .append("**EXTREME BURN HAZARD:** Keep every tool dry. Never cast alone.\n")
            .append(":::\n\n")
            .append(":::warning\n")
            .append("**Required Reading:** Before attempting any procedures in this guide, read the [Chemical Safety Guide](chemical-safety.html) in full.\n")
            .append(":::\n\n");
        for (int i = 1; i <= 26; i++) {
            body.append("## Section ").append(i).append(" Mock heading\n")
                .append("Body ").append(i).append(".\n");
        }

        SearchResult result = new SearchResult(
            "Foundry & Metal Casting",
            "",
            "",
            body.toString(),
            "GD-132",
            "",
            "metalworking",
            "guide"
        );

        String displayBody = DetailGuidePresentationFormatter.buildGuideBody(result);

        assertTrue(displayBody.contains("GD-132 \u00b7 17 SECTIONS"));
        assertFalse(displayBody.contains("GD-132 \u00b7 26 SECTIONS"));
        assertTrue(displayBody.contains("REQUIRED READING \u00b7 GD-220 \u00b7 Abrasives Manufacturing"));
        assertTrue(displayBody.contains("REQUIRED READING \u00b7 GD-499 \u00b7 Bellows & Forge Blower Construction"));
        assertTrue(displayBody.contains("REQUIRED READING \u00b7 GD-225 \u00b7 Bloomery Furnace Construction"));
        assertFalse(displayBody.contains("REQUIRED READING \u00b7 Chemical Safety Guide"));
    }

    @Test
    public void buildGuideBodyPromotesFoundryRequiredReadingRowsFromRelatedMetadata() {
        SearchResult result = new SearchResult(
            "Foundry & Metal Casting",
            "",
            "",
            "---\n"
                + "id: GD-132\n"
                + "related:\n"
                + "  - abrasives-manufacturing\n"
                + "  - bearing-manufacturing\n"
                + "  - bellows-forge-blower-construction\n"
                + "  - bloomery-furnace\n"
                + "---\n"
                + ":::danger\n"
                + "EXTREME BURN HAZARD: Keep tools dry.\n"
                + ":::\n"
                + ":::warning\n"
                + "**Required Reading:** Before attempting any procedures in this guide, read the [Chemical Safety Guide](chemical-safety.html) in full.\n"
                + ":::\n"
                + "## Foundry Safety Quickstart\n"
                + "Check dry tools.",
            "GD-132",
            "",
            "metalworking",
            "guide-focus"
        );

        assertEquals(
            "FIELD MANUAL \u00b7 REV 04-27 \u00b7 PK 2\n"
                + "Foundry & Metal Casting\n"
                + "GD-132 \u00b7 4 SECTIONS\n\n"
                + "DANGER \u00b7 EXTREME BURN HAZARD\n"
                + "Keep tools dry.\n"
                + "REQUIRED READING \u00b7 GD-220 \u00b7 Abrasives Manufacturing\n"
                + "REQUIRED READING \u00b7 GD-499 \u00b7 Bellows & Forge Blower Construction\n"
                + "REQUIRED READING \u00b7 GD-225 \u00b7 Bloomery Furnace Construction\n"
                + "\u2014 \u00a7 1 \u00b7 FOUNDRY SAFETY QUICKSTART\n"
                + "Check dry tools.",
            DetailGuidePresentationFormatter.buildGuideBody(result)
        );
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
            "\u2014 \u00a7 1 \u00b7 AREA READINESS\n\nKeep water away.\n\n\u2014 \u00a7 2 \u00b7 FOUNDRY SAFETY QUICKSTART\nCheck tools.",
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
    public void guideBodySanitizerRemovesMarkdownImageSyntaxAndHtmlComments() {
        assertEquals(
            "For air supply, see Bellows & Forge Blower Construction.\nFoundry diagram 1",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "<!-- raw renderer marker -->\n"
                    + "For air supply, see [Bellows & Forge Blower Construction](manual://GD-499).\n"
                    + "![Foundry diagram 1](../images/foundry.png)"
            )
        );
    }

    @Test
    public void guideBodySanitizerPreservesRequiredReadingAsCompactGuideRow() {
        assertEquals(
            "REQUIRED READING \u00b7 Chemical Safety Guide",
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
    public void guideBodySanitizerTreatsDashedInfoBoxFencesAsNoteBlocks() {
        assertEquals(
            "NOTE \u00b7 Critical Insight\nAncient civilizations used natural stones.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                ":::info-box\nCritical Insight: Ancient civilizations used natural stones.\n:::"
            )
        );
    }

    @Test
    public void guideBodySanitizerPromotesShortStandaloneAdmonitionTitles() {
        assertEquals(
            "WARNING \u00b7 Crucible Safety\nNever use a cracked crucible.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "::: warning\nCrucible Safety\n\nNever use a cracked crucible.\n:::"
            )
        );
    }

    @Test
    public void guideBodySanitizerCompactsReviewedBoundaryOpeningForFirstViewport() {
        assertEquals(
            "DANGER \u00b7 EXTREME BURN HAZARD\n"
                + "A single drop of water contacting molten metal causes a violent steam explosion. EVERY tool, mold, crucible, and surface that contacts molten metal must be completely dry.\n"
                + "REQUIRED READING \u00b7 Chemical Safety Guide\n"
                + "\u2014 \u00a7 1 \u00b7 AREA READINESS\n"
                + "Reviewed Answer-Card Boundary\n"
                + "Use this section only for foundry-area readiness, visible hazard screening, material and source labeling, no-go triggers, access control, and expert or owner handoff.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "::: danger\n"
                    + "EXTREME BURN HAZARD: A single drop of water contacting molten metal causes a violent steam explosion. "
                    + "EVERY tool, mold, crucible, and surface that contacts molten metal must be completely dry. "
                    + "Inspect crucibles for cracks before every use. Never cast alone.\n"
                    + ":::\n"
                    + "Required Reading: Before attempting any procedures in this guide, read the Chemical Safety Guide in full.\n"
                    + "## Section 1 Reviewed Answer-Card Boundary: Area readiness, hazard screen, and handoffs\n"
                    + "This is the reviewed answer-card surface for GD-132. Use it only for foundry-area readiness, visible hazard screening, material and source labeling, no-go triggers, access control, and expert or owner handoff. Start with the current activity status.\n\n"
                    + "For routine boundary screening, record the work area and owner.\n"
                    + "## Section 2 Required Reading\n"
                    + "Read linked guides."
            )
        );
    }

    @Test
    public void guideBodySanitizerRemovesRawTableMarkupAndHtmlEntities() {
        assertEquals(
            "Material \u00b7 Best Use \u00b7 Sandstone \u00b7 Coarse grinding \u00b7 Owner's note",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "<table><tr><th>Material</th><th>Best Use</th></tr><tr><td>Sandstone</td><td>Coarse grinding</td></tr></table> "
                    + "Owner&apos;s note"
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
            "REQUIRED READING \u00b7 Read GD-220 first.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "Required Reading: Required reading - Read GD-220 first."
            )
        );
    }

    @Test
    public void guideBodySanitizerSplitsVerboseSectionTitlesIntoManualLabelAndHeading() {
        assertEquals(
            "\u2014 \u00a7 1 \u00b7 AREA READINESS\nReviewed Answer-Card Boundary\nUse this section only for screening.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "## Section 1 Reviewed Answer-Card Boundary: Area Readiness, Hazard Screen, and Handoffs\n"
                    + "Use this section only for screening."
            )
        );
    }

    @Test
    public void guideBodySanitizerSkipsFrontMatterRulesMetadataAndDuplicateSectionRows() {
        assertEquals(
            "\u2014 \u00a7 1 \u00b7 STORAGE\nKeep jars covered.",
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
    public void guideBodySanitizerNormalizesCommonDecodedMojibakeGlyphs() {
        assertEquals(
            "Owner's note - Pattern -> Unlimited molds \u00b7 dry tools",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "Ownerâ€™s note â€” Pattern â†’ Unlimited molds â€¢ dry tools"
            )
        );
    }

    @Test
    public void guideBodySanitizerDropsCommonMojibakeEmojiMarkers() {
        assertEquals(
            "\u2014 \u00a7 1 \u00b7 OVERVIEW\nUse dry tools.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "## \u00f0\u0178\u201d\u00a5 Overview\nUse dry tools."
            )
        );
    }

    @Test
    public void guideBodySanitizerKeepsCompactSectionLabelsIdempotentAndCleansMojibake() {
        assertEquals(
            "\u2014 \u00a7 1 \u00b7 AREA READINESS\nKeep owner's notes clean.",
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

        assertEquals("\u2014 \u00a7 1 \u00b7 AREA READINESS\nREQUIRED READING \u00b7 Read GD-220 first.", parsed.displayText);
        assertEquals(2, parsed.lines.length);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.SECTION, parsed.lines[0].kind);
        assertEquals("\u2014 \u00a7 1 \u00b7", parsed.lines[0].label);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.REQUIRED_READING, parsed.lines[1].kind);
        assertEquals("REQUIRED READING", parsed.lines[1].label);
    }

    @Test
    public void guideBodyParserExposesSplitHeadingKind() {
        GuideBodySanitizer.ParsedGuideBody parsed = GuideBodySanitizer.parseGuideBodyForDisplay(
            "## Section 1 Reviewed Answer-Card Boundary: Area Readiness, Hazard Screen, and Handoffs"
        );

        assertEquals("\u2014 \u00a7 1 \u00b7 AREA READINESS\nReviewed Answer-Card Boundary", parsed.displayText);
        assertEquals(2, parsed.lines.length);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.SECTION, parsed.lines[0].kind);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.HEADING, parsed.lines[1].kind);
    }
}

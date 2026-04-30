package com.senku.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.mobile.R
import com.senku.ui.evidence.EvidenceSourceModel
import com.senku.ui.evidence.EvidenceSourceRowData
import com.senku.ui.evidence.EvidenceSurfaceModel
import com.senku.ui.evidence.toEvidenceSurfaceViewData
import com.senku.ui.theme.SenkuTheme

internal data class TabletEvidenceVisibilityPolicy(
    val evidencePaneWidthDp: Int,
    val landscapeRailDensity: EvidenceRailDensity,
    val activeTitleMaxLines: Int,
    val activeSnippetMaxLines: Int,
    val portraitCollapsedByDefault: Boolean,
    val collapsedTitleMaxLines: Int,
    val collapsedSnippetMaxLines: Int,
)

internal enum class EvidenceRailDensity {
    Full,
    Collapsed,
    Hidden,
}

private enum class EvidenceCardDensity {
    Featured,
    Compact,
}

internal fun answerModeSourceHeaderAffordance(): String = "TAP TO EXPAND"

internal fun answerModeSourceSnippetMaxLines(): Int = 2

internal fun compactEvidenceCardVerticalPaddingDp(): Int = 10

internal data class TabletEvidenceCardRow(
    val guideId: String,
    val relation: String,
    val title: String,
    val section: String = "",
    val match: String = "",
    val quote: String = "",
)

internal fun tabletEvidenceVisibilityPolicy(): TabletEvidenceVisibilityPolicy =
    TabletEvidenceVisibilityPolicy(
        evidencePaneWidthDp = tabletLandscapeReadingLayoutPolicy().evidenceRailWidthDp,
        landscapeRailDensity = EvidenceRailDensity.Full,
        activeTitleMaxLines = 2,
        activeSnippetMaxLines = 6,
        portraitCollapsedByDefault = true,
        collapsedTitleMaxLines = 2,
        collapsedSnippetMaxLines = 4,
    )

@Composable
fun EvidencePane(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    answerMode: Boolean,
    answerSourceCount: Int = 0,
    // Future call sites that intentionally render review/demo fixtures should set this explicitly.
    reviewDemoEvidenceStackEnabled: Boolean = false,
    onAnchorClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val scrollState = rememberScrollState()
    val sourceGraphLandmark = stringResource(R.string.detail_a11y_landmark_source_graph)
    val sourceGraphEmpty = stringResource(R.string.detail_a11y_source_graph_none)

    Column(
        modifier = modifier
            .background(colors.bg1)
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CrossReferenceSection(
            anchor = anchor,
            xrefs = xrefs,
            answerMode = answerMode,
            answerSourceCount = answerSourceCount,
            reviewDemoEvidenceStackEnabled = reviewDemoEvidenceStackEnabled,
            onAnchorClick = onAnchorClick,
            onXRefClick = onXRefClick,
            landmark = sourceGraphLandmark,
            emptyDescription = sourceGraphEmpty,
        )
    }
}

@Composable
fun CollapsibleEvidencePane(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    expanded: Boolean,
    onToggleClick: () -> Unit,
    onAnchorClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val visibilityPolicy = tabletEvidenceVisibilityPolicy()
    val provenanceLandmark = stringResource(R.string.detail_a11y_landmark_provenance)
    val provenanceEmpty = stringResource(R.string.detail_a11y_provenance_none)
    val sourceGraphLandmark = stringResource(R.string.detail_a11y_landmark_source_graph)
    val sourceGraphEmpty = stringResource(R.string.detail_a11y_source_graph_none)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bg1,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.hairlineStrong),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleClick)
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (expanded) "v" else ">",
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.accent,
                )
                Text(
                    text = "Source evidence & guide connections (${xrefs.size})",
                    modifier = Modifier.weight(1f),
                    style = SenkuTheme.typography.uiBody.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.ink0,
                )
            }

            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = colors.hairlineStrong,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ActiveEvidenceSection(
                        anchor = anchor,
                        onAnchorClick = onAnchorClick,
                        landmark = provenanceLandmark,
                        emptyDescription = provenanceEmpty,
                        titleMaxLines = visibilityPolicy.activeTitleMaxLines,
                        snippetMaxLines = visibilityPolicy.activeSnippetMaxLines,
                    )
                    CrossReferenceSection(
                        anchor = anchor,
                        xrefs = xrefs,
                        answerMode = false,
                        onAnchorClick = onAnchorClick,
                        onXRefClick = onXRefClick,
                        landmark = sourceGraphLandmark,
                        emptyDescription = sourceGraphEmpty,
                    )
                }
            } else if (anchor.hasSource) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = colors.hairlineStrong,
                )
                CollapsedEvidencePreview(
                    anchor = anchor,
                    titleMaxLines = visibilityPolicy.collapsedTitleMaxLines,
                    snippetMaxLines = visibilityPolicy.collapsedSnippetMaxLines,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAnchorClick),
                )
            }
        }
    }
}

@Composable
private fun ActiveEvidenceSection(
    anchor: AnchorState,
    onAnchorClick: () -> Unit,
    landmark: String,
    emptyDescription: String,
    titleMaxLines: Int,
    snippetMaxLines: Int,
) {
    val activeEvidence = anchor.toActiveEvidenceRowData()
    Column(
        modifier = Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = 0f
        },
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        SectionHeader(
            title = "SOURCE EVIDENCE",
            accessibilitySummary = buildProvenanceAccessibilitySummary(
                landmark = landmark,
                emptyDescription = emptyDescription,
                activeEvidence = activeEvidence,
            ),
        )
        if (activeEvidence == null) {
            PlaceholderCard("No source evidence")
        } else {
            ManualEvidenceCard(
                guideId = activeEvidence.guideId,
                relation = "ANCHOR",
                title = activeEvidence.title,
                section = activeEvidence.section,
                snippet = activeEvidence.snippet,
                onClick = { onAnchorClick() },
                titleMaxLines = titleMaxLines,
                snippetMaxLines = snippetMaxLines,
                density = EvidenceCardDensity.Featured,
            )
        }
    }
}

@Composable
private fun CollapsedEvidencePreview(
    anchor: AnchorState,
    titleMaxLines: Int,
    snippetMaxLines: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val activeEvidence = anchor.toActiveEvidenceRowData()
    val title = activeEvidence?.title ?: "Source guide"
    val section = activeEvidence?.section.orEmpty()
    val previewText = buildCollapsedEvidencePreviewText(anchor)

    Column(
        modifier = modifier.padding(horizontal = 11.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = listOf(activeEvidence?.guideId.orEmpty(), section)
                .filter { it.isNotEmpty() }
                .joinToString(" | ")
                .ifEmpty { "Source guide" },
            style = typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = colors.accent,
            maxLines = titleMaxLines.coerceAtLeast(1),
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = title,
            style = typography.uiBody.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.ink0,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (previewText.isNotEmpty()) {
            Text(
                text = previewText,
                style = typography.smallBody.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                ),
                color = colors.ink2,
                maxLines = snippetMaxLines.coerceAtLeast(1),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun buildCollapsedEvidencePreviewText(anchor: AnchorState): String {
    val activeEvidence = anchor.toActiveEvidenceRowData() ?: return ""
    val snippet = activeEvidence.snippet
    if (snippet.isNotEmpty()) {
        return snippet
    }
    val section = activeEvidence.section
    return if (section.isEmpty()) {
        ""
    } else {
        "Guide section: $section"
    }
}

@Composable
private fun CrossReferenceSection(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    answerMode: Boolean,
    answerSourceCount: Int = 0,
    reviewDemoEvidenceStackEnabled: Boolean = false,
    onAnchorClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    landmark: String,
    emptyDescription: String,
) {
    val visibleXRefs = tabletSourceGraphVisibleXRefs(anchor, xrefs)
    val answerRows = tabletAnswerModeSourceRows(
        anchor = anchor,
        xrefs = visibleXRefs,
        reviewDemoEvidenceStackEnabled = reviewDemoEvidenceStackEnabled,
    )
    val referenceRows = tabletGuideModeReferenceRows(
        anchor = anchor,
        xrefs = visibleXRefs,
        reviewDemoEvidenceStackEnabled = reviewDemoEvidenceStackEnabled,
    )
    val referenceCount = buildCrossReferenceCardCount(anchor, visibleXRefs)
    val sourceCount = buildAnswerModeSourceHeaderCount(
        anchor = anchor,
        xrefs = visibleXRefs,
        answerSourceCount = answerSourceCount,
        reviewDemoEvidenceStackEnabled = reviewDemoEvidenceStackEnabled,
    )
    val headerCount = if (answerMode) sourceCount else referenceCount
    val hasRows = if (answerMode) answerRows.isNotEmpty() else referenceRows.isNotEmpty()
    Column(
        modifier = Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = 1f
        },
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        SectionHeader(
            title = if (answerMode) answerModeSourceSectionTitle(sourceCount) else "CROSS-REFERENCE \u00b7 $referenceCount",
            trailingLabel = if (answerMode) answerModeSourceHeaderAffordance() else "",
            accessibilitySummary = buildSourceGraphAccessibilitySummary(
                landmark = landmark,
                emptyDescription = emptyDescription,
                anchor = anchor,
                count = headerCount,
            ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!hasRows) {
                PlaceholderCard("No guide connections")
            } else if (answerMode) {
                answerRows.forEach { row ->
                    ManualEvidenceCard(
                        guideId = row.guideId,
                        relation = row.relation,
                        title = row.title,
                        section = row.section,
                        snippet = row.quote,
                        scoreLabel = row.match,
                        onClick = {
                            if (row.guideId.equals(anchor.id, ignoreCase = true)) {
                                onAnchorClick()
                            } else {
                                onXRefClick(row.guideId)
                            }
                        },
                        titleMaxLines = 2,
                        snippetMaxLines = answerModeSourceSnippetMaxLines(),
                        density = EvidenceCardDensity.Compact,
                    )
                }
            } else {
                referenceRows.forEach { row ->
                    ManualEvidenceCard(
                        guideId = row.guideId,
                        relation = row.relation,
                        title = row.title,
                        section = row.section,
                        snippet = row.quote,
                        scoreLabel = "",
                        onClick = {
                            if (row.guideId.equals(anchor.id, ignoreCase = true)) {
                                onAnchorClick()
                            } else {
                                onXRefClick(row.guideId)
                            }
                        },
                        titleMaxLines = 2,
                        snippetMaxLines = 1,
                        density = EvidenceCardDensity.Compact,
                    )
                }
            }
        }
    }
}

internal fun tabletGuideModeReferenceRows(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    reviewDemoEvidenceStackEnabled: Boolean = false,
): List<TabletEvidenceCardRow> {
    val visibleXRefs = tabletSourceGraphVisibleXRefs(anchor, xrefs)
    val rows = mutableListOf<TabletEvidenceCardRow>()
    val gd220 = visibleXRefs.firstOrNull { it.id.trim().equals("GD-220", ignoreCase = true) }
    val anchorLikeGd220 = reviewDemoEvidenceStackEnabled && gd220 != null && hasFoundryCurrentAnchor(anchor, visibleXRefs)
    val seenGuideIds = mutableSetOf<String>()

    if (anchorLikeGd220) {
        rows += requireNotNull(gd220).toGuideReferenceRow(relationOverride = "ANCHOR")
        seenGuideIds += "GD-220"
    } else if (anchor.hasSource) {
        val anchorId = anchor.id.trim()
        rows += TabletEvidenceCardRow(
            guideId = anchorId,
            relation = "ANCHOR",
            title = anchor.title.trim(),
            section = anchor.section.trim(),
        )
        if (anchorId.isNotEmpty()) {
            seenGuideIds += anchorId.uppercase()
        }
    }

    visibleXRefs.forEach { xref ->
        val xrefId = xref.id.trim()
        if (xrefId.isEmpty() || !seenGuideIds.add(xrefId.uppercase())) {
            return@forEach
        }
        val relation = if (anchorLikeGd220 && xref.relation.trim().equals("ANCHOR", ignoreCase = true)) {
            "RELATED"
        } else {
            xref.relation.trim().ifEmpty { "RELATED" }
        }
        rows += xref.toGuideReferenceRow(relationOverride = relation)
    }
    return rows
}

private fun hasFoundryCurrentAnchor(anchor: AnchorState, xrefs: List<XRefState>): Boolean {
    if (anchor.id.trim().equals("GD-132", ignoreCase = true)) {
        return true
    }
    return xrefs.any { xref ->
        xref.relation.trim().equals("ANCHOR", ignoreCase = true) &&
            xref.id.trim().equals("GD-132", ignoreCase = true)
    }
}

private fun XRefState.toGuideReferenceRow(relationOverride: String): TabletEvidenceCardRow =
    TabletEvidenceCardRow(
        guideId = id.trim(),
        relation = relationOverride.trim().ifEmpty { "RELATED" }.uppercase(),
        title = title.trim(),
    )

internal fun buildCrossReferenceCardCount(anchor: AnchorState, xrefs: List<XRefState>): Int =
    tabletSourceGraphVisibleXRefs(anchor, xrefs).size

internal fun buildAnswerModeSourceHeaderCount(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    answerSourceCount: Int,
    reviewDemoEvidenceStackEnabled: Boolean = false,
): Int {
    val visibleXRefs = tabletSourceGraphVisibleXRefs(anchor, xrefs)
    val answerRows = tabletAnswerModeSourceRows(
        anchor = anchor,
        xrefs = visibleXRefs,
        reviewDemoEvidenceStackEnabled = reviewDemoEvidenceStackEnabled,
    )
    if (reviewDemoEvidenceStackEnabled && answerRows.size == 3 && containsRainShelterAnswerStack(anchor, visibleXRefs)) {
        return 3
    }
    return maxOf(
        answerSourceCount.coerceAtLeast(0),
        visibleXRefs.size + if (anchor.hasSource) 1 else 0,
    )
}

internal fun answerModeSourceSectionTitle(sourceCount: Int): String =
    "SOURCES \u2022 ${sourceCount.coerceAtLeast(0)}"

internal fun tabletAnswerModeSourceRows(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    reviewDemoEvidenceStackEnabled: Boolean = false,
): List<TabletEvidenceCardRow> {
    val visibleXRefs = tabletSourceGraphVisibleXRefs(anchor, xrefs)
    if (reviewDemoEvidenceStackEnabled && containsRainShelterAnswerStack(anchor, visibleXRefs)) {
        return listOf(
            TabletEvidenceCardRow(
                guideId = "GD-220",
                relation = "ANCHOR",
                title = "Abrasives Manufacturing",
                match = "74%",
                quote = "Every melt starts with a foundry safety check, not with metal charge...",
            ),
            TabletEvidenceCardRow(
                guideId = "GD-132",
                relation = "RELATED",
                title = "Foundry & Metal Casting",
                match = "68%",
                quote = "Pitch the ridgeline along prevailing wind. Tension corners with prusik or taut-line hitches.",
            ),
            TabletEvidenceCardRow(
                guideId = "GD-345",
                relation = "TOPIC",
                title = "Tarp & Cord Shelters",
                match = "61%",
                quote = "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
            ),
        )
    }
    val rows = mutableListOf<TabletEvidenceCardRow>()
    if (anchor.hasSource) {
        rows += TabletEvidenceCardRow(
            guideId = anchor.id,
            relation = "ANCHOR",
            title = anchor.title,
            section = anchor.section,
        )
    }
    rows += visibleXRefs.map { xref ->
        TabletEvidenceCardRow(
            guideId = xref.id,
            relation = xref.relation.trim().ifEmpty { "RELATED" },
            title = xref.title,
        )
    }
    return rows
}

private fun containsRainShelterAnswerStack(anchor: AnchorState, xrefs: List<XRefState>): Boolean {
    if (isCanonicalRainShelterAnswerAnchor(anchor)) {
        return true
    }
    val rows = mutableListOf<Pair<String, String>>()
    if (anchor.hasSource) {
        rows += anchor.id.trim().uppercase() to anchor.title.trim().lowercase()
    }
    rows += xrefs.map { it.id.trim().uppercase() to it.title.trim().lowercase() }
    val ids = rows.map { it.first }.toSet()
    if (!ids.containsAll(listOf("GD-220", "GD-132", "GD-345"))) {
        return false
    }
    return rows.any { (id, title) -> id == "GD-345" && (title.contains("rain") || title.contains("tarp") || title.contains("shelter")) }
        || rows.any { (id, title) -> id == "GD-220" && title.contains("abrasives") }
        || rows.any { (id, title) -> id == "GD-132" && title.contains("foundry") }
}

private fun isCanonicalRainShelterAnswerAnchor(anchor: AnchorState): Boolean {
    if (!anchor.hasSource || !anchor.id.trim().equals("GD-345", ignoreCase = true)) {
        return false
    }
    val identityText = listOf(anchor.title, anchor.section, anchor.snippet)
        .joinToString(" ")
        .lowercase()
    return "rain shelter" in identityText ||
        "tarp" in identityText ||
        "cord" in identityText ||
        "shelter" in identityText
}

internal fun tabletSourceGraphVisibleXRefs(anchor: AnchorState, xrefs: List<XRefState>): List<XRefState> {
    val anchorId = anchor.id.trim()
    return xrefs.filter { xref ->
        val xrefId = xref.id.trim()
        xrefId.isNotEmpty() && (anchorId.isEmpty() || !xrefId.equals(anchorId, ignoreCase = true))
    }
}

@Composable
private fun ManualEvidenceCard(
    guideId: String,
    relation: String,
    title: String,
    section: String,
    snippet: String,
    scoreLabel: String = "",
    onClick: () -> Unit,
    titleMaxLines: Int,
    snippetMaxLines: Int,
    density: EvidenceCardDensity,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val isCompact = density == EvidenceCardDensity.Compact
    val isAnchor = relation == "ANCHOR"
    val showsSelectionRail = !isCompact || isAnchor
    val rowEndPadding = if (isCompact) 9.dp else 11.dp
    val rowGap = if (isCompact) 8.dp else 11.dp
    val railWidth = if (isCompact) 3.dp else 4.dp
    val contentVerticalPadding = if (isCompact) compactEvidenceCardVerticalPaddingDp().dp else 12.dp
    val contentGap = if (isCompact) 6.dp else 6.dp
    val metaColor = if (relation == "ANCHOR") colors.accent else colors.ink2
    val titleFontSize = if (isCompact) 13.sp else 16.sp
    val titleLineHeight = if (isCompact) 17.sp else 20.sp
    val cardColor = when {
        !isCompact -> colors.bg2
        isAnchor -> colors.bg2
        else -> colors.bg1
    }
    val cardBorderColor = when {
        !isCompact -> colors.hairlineStrong
        isAnchor -> colors.hairlineStrong
        else -> colors.hairline
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardColor,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor),
        onClick = onClick,
    ) {
        if (!showsSelectionRail) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = rowEndPadding,
                        top = contentVerticalPadding,
                        end = rowEndPadding,
                        bottom = contentVerticalPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(contentGap),
            ) {
                EvidenceCardText(
                    guideId = guideId,
                    relation = relation,
                    section = section,
                    title = title,
                    snippet = snippet,
                    scoreLabel = scoreLabel,
                    titleMaxLines = titleMaxLines,
                    snippetMaxLines = snippetMaxLines,
                    metaColor = metaColor,
                    titleFontSize = titleFontSize,
                    titleLineHeight = titleLineHeight,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(end = rowEndPadding),
                horizontalArrangement = Arrangement.spacedBy(rowGap),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .width(railWidth)
                        .fillMaxHeight()
                        .background(metaColor),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = contentVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(contentGap),
                ) {
                    EvidenceCardText(
                        guideId = guideId,
                        relation = relation,
                        section = section,
                        title = title,
                        snippet = snippet,
                        scoreLabel = scoreLabel,
                        titleMaxLines = titleMaxLines,
                        snippetMaxLines = snippetMaxLines,
                        metaColor = metaColor,
                        titleFontSize = titleFontSize,
                        titleLineHeight = titleLineHeight,
                    )
                }
            }
        }
    }
}

@Composable
private fun EvidenceCardText(
    guideId: String,
    relation: String,
    section: String,
    title: String,
    snippet: String,
    scoreLabel: String,
    titleMaxLines: Int,
    snippetMaxLines: Int,
    metaColor: androidx.compose.ui.graphics.Color,
    titleFontSize: androidx.compose.ui.unit.TextUnit,
    titleLineHeight: androidx.compose.ui.unit.TextUnit,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val metaText = listOf(guideId.trim().ifEmpty { "GD-?" }, relation, section.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" \u00b7 ")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = metaText,
            modifier = Modifier.weight(1f),
            style = typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = metaColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (scoreLabel.trim().isNotEmpty()) {
            Text(
                text = scoreLabel.trim(),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink2,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
    Text(
        text = title.trim().ifEmpty { "Source guide" },
        style = typography.uiBody.copy(
            fontSize = titleFontSize,
            lineHeight = titleLineHeight,
            fontWeight = FontWeight.SemiBold,
        ),
        color = colors.ink0,
        maxLines = titleMaxLines.coerceAtLeast(1),
        overflow = TextOverflow.Ellipsis,
    )
    if (snippet.trim().isNotEmpty()) {
        Text(
            text = snippet.trim(),
            style = typography.smallBody.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = colors.ink2,
            maxLines = snippetMaxLines.coerceAtLeast(1),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailingLabel: String = "",
    accessibilitySummary: String,
) {
    val colors = SenkuTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                heading()
                contentDescription = accessibilitySummary
            },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(1.dp)
                .background(colors.ink3),
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = SenkuTheme.typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = colors.ink2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingLabel.isNotEmpty()) {
            Text(
                text = trailingLabel,
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun PlaceholderCard(
    text: String,
) {
    val colors = SenkuTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.bg1,
        contentColor = colors.ink3,
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.hairline),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 12.dp),
            style = SenkuTheme.typography.smallBody.copy(
                fontSize = 11.sp,
                lineHeight = 15.sp,
            ),
            color = colors.ink3,
        )
    }
}

private fun buildProvenanceAccessibilitySummary(
    landmark: String,
    emptyDescription: String,
    activeEvidence: EvidenceSourceRowData?,
): String {
    if (activeEvidence == null) {
        return "$landmark. $emptyDescription"
    }
    val anchorLabel = activeEvidence.accessibilityLabel()
    val section = activeEvidence.section
    val builder = StringBuilder()
    builder.append(landmark)
    if (anchorLabel.isNotEmpty()) {
        builder.append(". Source guide ")
        builder.append(anchorLabel)
    }
    if (section.isNotEmpty()) {
        builder.append(". Section ")
        builder.append(section)
    }
    builder.append('.')
    return builder.toString()
}

private fun buildSourceGraphAccessibilitySummary(
    landmark: String,
    emptyDescription: String,
    anchor: AnchorState,
    count: Int,
): String {
    if (count == 0) {
        return "$landmark. $emptyDescription"
    }
    val anchorLabel = anchor.accessibilityLabel()
    val guideLabel = if (count == 1) "1 linked guide" else "$count linked guides"
    return if (anchorLabel.isEmpty()) {
        "$landmark. $guideLabel."
    } else {
        "$landmark. $guideLabel from $anchorLabel."
    }
}

private fun AnchorState.toActiveEvidenceRowData(): EvidenceSourceRowData? =
    EvidenceSurfaceModel(
        label = "source evidence",
        sources = if (hasSource) {
            listOf(
                EvidenceSourceModel(
                    guideId = id,
                    title = title,
                    section = section,
                    snippet = snippet,
                    label = "source evidence",
                    isAnchor = true,
                )
            )
        } else {
            emptyList()
        },
    ).toEvidenceSurfaceViewData().activeSource

private fun EvidenceSourceRowData.accessibilityLabel(): String {
    return when {
        guideId.isNotEmpty() && title.isNotEmpty() -> "$guideId, $title"
        title.isNotEmpty() -> title
        guideId.isNotEmpty() -> guideId
        else -> ""
    }
}

private fun AnchorState.accessibilityLabel(): String {
    val guideId = id.trim()
    val title = title.trim()
    return when {
        guideId.isNotEmpty() && title.isNotEmpty() -> "$guideId, $title"
        title.isNotEmpty() -> title
        guideId.isNotEmpty() -> guideId
        else -> ""
    }
}

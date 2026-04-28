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

internal fun tabletEvidenceVisibilityPolicy(): TabletEvidenceVisibilityPolicy =
    TabletEvidenceVisibilityPolicy(
        evidencePaneWidthDp = 360,
        landscapeRailDensity = EvidenceRailDensity.Full,
        activeTitleMaxLines = 2,
        activeSnippetMaxLines = 10,
        portraitCollapsedByDefault = true,
        collapsedTitleMaxLines = 2,
        collapsedSnippetMaxLines = 5,
    )

@Composable
fun EvidencePane(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    onAnchorClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val visibilityPolicy = tabletEvidenceVisibilityPolicy()
    val scrollState = rememberScrollState()
    val provenanceLandmark = stringResource(R.string.detail_a11y_landmark_provenance)
    val provenanceEmpty = stringResource(R.string.detail_a11y_provenance_none)
    val sourceGraphLandmark = stringResource(R.string.detail_a11y_landmark_source_graph)
    val sourceGraphEmpty = stringResource(R.string.detail_a11y_source_graph_none)

    Column(
        modifier = modifier
            .background(colors.bg1)
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                fontSize = 13.sp,
                lineHeight = 17.sp,
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
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
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
    onAnchorClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    landmark: String,
    emptyDescription: String,
) {
    val cardCount = buildCrossReferenceCardCount(anchor, xrefs)
    Column(
        modifier = Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = 1f
        },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader(
            title = "CROSS-REFERENCE - $cardCount",
            accessibilitySummary = buildSourceGraphAccessibilitySummary(
                landmark = landmark,
                emptyDescription = emptyDescription,
                anchor = anchor,
                count = xrefs.size,
            ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (cardCount == 0) {
                PlaceholderCard("No guide connections")
            } else {
                if (anchor.hasSource) {
                    ManualEvidenceCard(
                        guideId = anchor.id,
                        relation = "ANCHOR",
                        title = anchor.title,
                        section = anchor.section,
                        snippet = "",
                        onClick = onAnchorClick,
                        titleMaxLines = 2,
                        snippetMaxLines = 1,
                        density = EvidenceCardDensity.Compact,
                    )
                }
                xrefs.forEach { xref ->
                    ManualEvidenceCard(
                        guideId = xref.id,
                        relation = "RELATED",
                        title = xref.title,
                        section = "",
                        snippet = "",
                        onClick = { onXRefClick(xref.id) },
                        titleMaxLines = 2,
                        snippetMaxLines = 1,
                        density = EvidenceCardDensity.Compact,
                    )
                }
            }
        }
    }
}

internal fun buildCrossReferenceCardCount(anchor: AnchorState, xrefs: List<XRefState>): Int =
    xrefs.size + if (anchor.hasSource) 1 else 0

@Composable
private fun ManualEvidenceCard(
    guideId: String,
    relation: String,
    title: String,
    section: String,
    snippet: String,
    onClick: () -> Unit,
    titleMaxLines: Int,
    snippetMaxLines: Int,
    density: EvidenceCardDensity,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val isCompact = density == EvidenceCardDensity.Compact
    val railHeight = if (isCompact) 0.dp else 96.dp
    val rowEndPadding = if (isCompact) 10.dp else 12.dp
    val rowGap = if (isCompact) 12.dp else 14.dp
    val railWidth = if (isCompact) 2.dp else 3.dp
    val contentVerticalPadding = if (isCompact) 12.dp else 14.dp
    val contentGap = if (isCompact) 6.dp else 7.dp
    val metaColor = if (relation == "ANCHOR") colors.accent else colors.ink2
    val titleFontSize = if (isCompact) 14.sp else 15.sp
    val titleLineHeight = if (isCompact) 18.sp else 19.sp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.bg2,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.hairlineStrong),
        onClick = onClick,
    ) {
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
                    .then(if (isCompact) Modifier.fillMaxHeight() else Modifier.height(railHeight))
                    .background(metaColor),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = contentVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(contentGap),
            ) {
                Text(
                    text = listOf(guideId.trim().ifEmpty { "GD-?" }, relation, section.trim())
                        .filter { it.isNotEmpty() }
                        .joinToString(" - "),
                    style = typography.monoCaps.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = metaColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
                            lineHeight = 17.sp,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = colors.ink2,
                        maxLines = snippetMaxLines.coerceAtLeast(1),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    accessibilitySummary: String,
) {
    val colors = SenkuTheme.colors

    Row(
        modifier = Modifier.semantics {
            heading()
            contentDescription = accessibilitySummary
        },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(1.dp)
                .background(colors.ink3),
        )
        Text(
            text = title,
            style = SenkuTheme.typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = colors.ink2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            style = SenkuTheme.typography.smallBody.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
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

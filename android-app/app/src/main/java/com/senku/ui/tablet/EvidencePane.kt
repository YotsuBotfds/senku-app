package com.senku.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.mobile.R
import com.senku.ui.evidence.EvidenceSnippet
import com.senku.ui.evidence.EvidenceSnippetModel
import com.senku.ui.evidence.EvidenceSourceModel
import com.senku.ui.evidence.EvidenceSourceRowData
import com.senku.ui.evidence.EvidenceSurfaceModel
import com.senku.ui.evidence.XRefRow
import com.senku.ui.evidence.XRefRowModel
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
            .background(colors.bg0)
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .padding(14.dp),
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
        shape = RoundedCornerShape(12.dp),
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
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
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
            EvidenceSnippet(
                evidence = EvidenceSnippetModel(
                    guideId = activeEvidence.guideId,
                    title = activeEvidence.title,
                    section = activeEvidence.section,
                    snippet = activeEvidence.snippet,
                ),
                onClick = { onAnchorClick() },
                titleMaxLines = titleMaxLines,
                snippetMaxLines = snippetMaxLines,
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
    onXRefClick: (String) -> Unit,
    landmark: String,
    emptyDescription: String,
) {
    Column(
        modifier = Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = 1f
        },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader(
            title = "GUIDE CONNECTIONS · ${xrefs.size}",
            accessibilitySummary = buildSourceGraphAccessibilitySummary(
                landmark = landmark,
                emptyDescription = emptyDescription,
                anchor = anchor,
                count = xrefs.size,
            ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (xrefs.isEmpty()) {
                PlaceholderCard("No guide connections")
            } else {
                xrefs.forEach { xref ->
                    XRefRow(
                        xRef = XRefRowModel(
                            guideId = xref.id,
                            title = xref.title,
                        ),
                        onClick = { onXRefClick(xref.id) },
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

    Text(
        text = title,
        modifier = Modifier.semantics {
            heading()
            contentDescription = accessibilitySummary
        },
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

@Composable
private fun PlaceholderCard(
    text: String,
) {
    val colors = SenkuTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.bg1,
        contentColor = colors.ink3,
        shape = RoundedCornerShape(12.dp),
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

package com.senku.ui.sources

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senku.mobile.R
import com.senku.ui.evidence.EvidenceSourceModel
import com.senku.ui.evidence.normalizeEvidenceGuideId
import com.senku.ui.evidence.normalizeEvidenceLabel
import com.senku.ui.evidence.normalizeEvidenceSourceTitle
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme

private const val MetaSeparator = " \u00B7 "

@Immutable
data class SourceRowModel(
    val guideId: String,
    val title: String,
    val category: String = "",
    val isAnchor: Boolean = false,
)

/**
 * Compact Rev 03 source rail primitive. Mount sites own navigation and pass the row target
 * through [onClick] when they wire this into the legacy activity shells.
 */
@Composable
fun SourceRow(
    source: SourceRowModel,
    onClick: (SourceRowModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val rowCorner = dimensionResource(R.dimen.senku_rev03_corner_small)
    val rowMinHeight = dimensionResource(R.dimen.senku_rev03_identity_strip_height)
    val horizontalPadding = dimensionResource(R.dimen.senku_rev03_space_12)
    val verticalPadding = dimensionResource(R.dimen.senku_rev03_space_6)
    val contentGap = dimensionResource(R.dimen.senku_rev03_space_6)
    val chevronGap = dimensionResource(R.dimen.senku_rev03_space_6)
    val chevronSize = dimensionResource(R.dimen.senku_rev03_space_10)
    val metaLabel = buildSourceRowMeta(category = source.category, isAnchor = source.isAnchor)
    val containerColor = source.containerColor(colors)
    val idColor = if (source.isAnchor) colors.accent else colors.ink2
    val metaColor = if (source.isAnchor) colors.accent else colors.ink3
    val chevronColor = if (source.isAnchor) colors.accent else colors.ink2

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.hairline,
                shape = RoundedCornerShape(rowCorner),
            ),
        shape = RoundedCornerShape(rowCorner),
        color = containerColor,
        contentColor = colors.ink0,
        onClick = { onClick(source) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = rowMinHeight)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = source.displayGuideId(),
                style = typography.monoCaps,
                color = idColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(contentGap))
            Text(
                text = source.displayTitle(),
                modifier = Modifier.weight(1f),
                style = typography.uiBody,
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (metaLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.width(contentGap))
                Text(
                    text = metaLabel,
                    modifier = Modifier.widthIn(max = 96.dp),
                    style = typography.monoCaps,
                    color = metaColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(chevronGap))
            SourceChevron(
                color = chevronColor,
                modifier = Modifier.size(chevronSize),
            )
        }
    }
}

internal fun buildSourceRowMeta(category: String, isAnchor: Boolean): String {
    val parts = buildList {
        normalizeSourceCategory(category)
            .takeIf { it.isNotEmpty() }
            ?.let(::add)
        if (isAnchor) {
            add("anchor")
        }
    }
    return parts.joinToString(separator = MetaSeparator)
}

internal fun normalizeSourceCategory(category: String): String {
    val cleaned = normalizeEvidenceLabel(category)
    return when (cleaned) {
        "source anchor", "anchor", "anchor guide" -> ""
        "cross reference", "cross-reference", "crossref", "related guide" -> "related"
        "required reading", "prerequisite" -> "required"
        else -> cleaned
    }
}

internal fun EvidenceSourceModel.toSourceRowModel(): SourceRowModel =
    SourceRowModel(
        guideId = normalizeEvidenceGuideId(guideId),
        title = normalizeEvidenceSourceTitle(title),
        category = normalizeEvidenceLabel(label),
        isAnchor = isAnchor,
    )

internal fun SourceRowModel.displayGuideId(): String =
    normalizeEvidenceGuideId(guideId).ifBlank { "GD-?" }

internal fun SourceRowModel.displayTitle(): String = normalizeEvidenceSourceTitle(title)

private fun SourceRowModel.containerColor(colors: com.senku.ui.theme.SenkuColorTokens): Color =
    if (isAnchor) {
        colors.accent.copy(alpha = 0.10f).compositeOver(colors.bg1)
    } else {
        colors.bg1
    }

@Composable
private fun SourceChevron(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.75.dp.toPx()
        val startX = size.width * 0.32f
        val midX = size.width * 0.68f
        val topY = size.height * 0.22f
        val middleY = size.height * 0.50f
        val bottomY = size.height * 0.78f

        drawLine(
            color = color,
            start = Offset(startX, topY),
            end = Offset(midX, middleY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(startX, bottomY),
            end = Offset(midX, middleY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SourceRowPreview() {
    SenkuAppTheme {
        Surface(color = SenkuTheme.colors.bg0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.senku_rev03_space_16)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.senku_rev03_space_8)),
            ) {
                SourceRow(
                    source = SourceRowModel(
                        guideId = "GD-214",
                        title = "Water cache rotation",
                        category = "water",
                        isAnchor = true,
                    ),
                    onClick = {},
                )
                SourceRow(
                    source = SourceRowModel(
                        guideId = "GD-118",
                        title = "Emergency ash filtration",
                        category = "resource-management",
                    ),
                    onClick = {},
                )
            }
        }
    }
}

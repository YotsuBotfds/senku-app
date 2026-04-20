package com.senku.ui.primitives

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senku.mobile.R
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme

private const val ThreadFallbackQuestion = "Continue thread"
private const val ThreadFallbackGuideId = "GD-?"

@Immutable
data class ThreadRowModel(
    val question: String,
    val guideId: String,
    val kindLabel: String,
    val timeLabel: String,
)

@Composable
fun ThreadRow(
    thread: ThreadRowModel,
    onClick: (ThreadRowModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val corner = dimensionResource(R.dimen.senku_rev03_corner_small)
    val horizontalPadding = dimensionResource(R.dimen.senku_rev03_space_10)
    val verticalPadding = dimensionResource(R.dimen.senku_rev03_space_8)
    val contentGap = dimensionResource(R.dimen.senku_rev03_space_8)
    val iconSize = 28.dp

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bg1,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(corner),
        border = BorderStroke(1.dp, colors.hairline),
        onClick = { onClick(thread) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(contentGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(iconSize),
                color = colors.bg2,
                contentColor = colors.accent,
                shape = CircleShape,
            ) {
                ThreadGlyph()
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = thread.displayQuestion(),
                    style = SenkuTheme.typography.smallBody,
                    color = colors.ink0,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = thread.metaLine(),
                    style = SenkuTheme.typography.monoCaps,
                    color = colors.ink3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SourceChevron(
                color = colors.ink2,
                modifier = Modifier.size(dimensionResource(R.dimen.senku_rev03_space_10)),
            )
        }
    }
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

@Composable
private fun ThreadGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.padding(6.dp)) {
        val stroke = 1.7.dp.toPx()
        val leftX = size.width * 0.22f
        val midX = size.width * 0.46f
        val rightX = size.width * 0.72f
        val topY = size.height * 0.26f
        val midY = size.height * 0.50f
        val bottomY = size.height * 0.74f

        drawLine(
            color = colors.accent,
            start = Offset(leftX, topY),
            end = Offset(midX, midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = colors.accent,
            start = Offset(leftX, bottomY),
            end = Offset(midX, midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = colors.accent,
            start = Offset(midX, midY),
            end = Offset(rightX, midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private fun ThreadRowModel.displayQuestion(): String =
    question.trim().ifEmpty { ThreadFallbackQuestion }

private fun ThreadRowModel.metaLine(): String {
    val parts = buildList {
        add(guideId.trim().ifEmpty { ThreadFallbackGuideId })
        kindLabel.trim().takeIf { it.isNotEmpty() }?.let(::add)
        timeLabel.trim().takeIf { it.isNotEmpty() }?.let(::add)
    }
    return parts.joinToString(separator = " \u00B7 ")
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 360)
@Composable
private fun ThreadRowPreview() {
    SenkuAppTheme {
        Surface(color = SenkuTheme.colors.bg0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.senku_rev03_space_16)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.senku_rev03_space_8)),
            ) {
                ThreadRow(
                    thread = ThreadRowModel(
                        question = "Can I boil this water if it smells metallic?",
                        guideId = "GD-214",
                        kindLabel = "AI answer",
                        timeLabel = "12m",
                    ),
                    onClick = {},
                )
                ThreadRow(
                    thread = ThreadRowModel(
                        question = "How do I dry tinder after rain?",
                        guideId = "GD-087",
                        kindLabel = "instant",
                        timeLabel = "1h",
                    ),
                    onClick = {},
                )
            }
        }
    }
}

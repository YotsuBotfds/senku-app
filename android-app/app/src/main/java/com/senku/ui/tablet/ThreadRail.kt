package com.senku.ui.tablet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuTheme

@Composable
fun ThreadRail(
    turns: List<ThreadTurnState>,
    sources: List<SourceState>,
    guideMode: Boolean,
    pinVisible: Boolean,
    pinActive: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
    onTurnClick: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val scrollState = rememberScrollState()
    val guideLabels = tabletGuideNavigationLabels()

    Column(
        modifier = modifier
            .background(colors.bg1)
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Toolbar(
            pinVisible = pinVisible,
            pinActive = pinActive,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onPinClick = onPinClick,
        )

        RailSection(
            label = if (guideMode) guideLabels.sectionLabel else "THREAD",
            count = turns.size,
        ) {
            if (turns.isEmpty()) {
                PlaceholderText(if (guideMode) guideLabels.emptySectionLabel else "No turns yet.")
            } else {
                turns.forEachIndexed { index, turn ->
                    ThreadTurnRow(
                        turn = turn,
                        index = index + 1,
                        guideMode = guideMode,
                        onClick = { onTurnClick(turn.id) },
                    )
                }
            }
        }

        RailSection(
            label = if (guideMode) guideLabels.referenceLabel else "SOURCES",
            count = sources.size,
        ) {
            if (sources.isEmpty()) {
                PlaceholderText(if (guideMode) guideLabels.emptyReferenceLabel else "No sources yet.")
            } else {
                sources.forEach { source ->
                    SourcePill(
                        source = source,
                        guideMode = guideMode,
                        onClick = { onSourceClick(source.key) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Toolbar(
    pinVisible: Boolean,
    pinActive: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
) {
    val colors = SenkuTheme.colors

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RailActionButton(
                active = false,
                glyph = { BackGlyph() },
                onClick = onBackClick,
            )
            RailActionButton(
                active = false,
                glyph = { HomeGlyph() },
                onClick = onHomeClick,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (pinVisible) {
                RailActionButton(
                    active = pinActive,
                    glyph = { PinGlyph(active = pinActive) },
                    onClick = onPinClick,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = colors.hairlineStrong,
        )
    }
}

@Composable
private fun RailSection(
    label: String,
    count: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SenkuTheme.colors

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(18.dp),
                    thickness = 1.dp,
                    color = colors.hairlineStrong,
                )
                Text(
                    text = label + " - " + count,
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.ink2,
                    maxLines = 1,
                )
            }
            content()
        },
    )
}

@Composable
private fun ThreadTurnRow(
    turn: ThreadTurnState,
    index: Int,
    guideMode: Boolean,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val idColor = when (turn.status) {
        Status.Done -> colors.ok
        Status.Active -> colors.accent
        Status.Pending -> colors.ink3
    }
    val background = if (turn.isActive) {
        colors.accent.copy(alpha = 0.10f).compositeOver(colors.bg2)
    } else {
        colors.bg1
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(if (turn.isActive) colors.accent else colors.hairlineStrong),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (guideMode) "SEC $index" else turn.id,
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                    ),
                    color = idColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = turn.question.trim().ifEmpty { "No question recorded." },
                    style = SenkuTheme.typography.smallBody.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                    ),
                    color = colors.ink1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SourcePill(
    source: SourceState,
    guideMode: Boolean,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val background = when {
        source.isSelected -> colors.accent.copy(alpha = 0.12f).compositeOver(colors.bg1)
        source.isAnchor -> colors.ok.copy(alpha = 0.10f).compositeOver(colors.bg1)
        else -> colors.bg1
    }
    val idColor = when {
        source.isSelected -> colors.accent
        source.isAnchor -> colors.ok
        else -> colors.ink2
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(27.dp)
                    .background(if (source.isSelected || source.isAnchor) idColor else colors.hairlineStrong),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (guideMode) {
                        sourceGuideRole(source)
                    } else {
                        source.id.trim().ifEmpty { "GD-?" }
                    },
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = idColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = source.title.trim().ifEmpty { if (guideMode) "Related guide" else "Source guide" },
                    style = SenkuTheme.typography.smallBody.copy(
                        fontSize = 10.5.sp,
                        lineHeight = 12.sp,
                    ),
                    color = colors.ink1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun sourceGuideRole(source: SourceState): String =
    when {
        source.isAnchor -> "${source.id.trim().ifEmpty { "GD-?" }} - ANCHOR"
        source.isSelected -> "${source.id.trim().ifEmpty { "GD-?" }} - OPEN"
        else -> "${source.id.trim().ifEmpty { "GD-?" }} - RELATED"
    }

@Composable
private fun PlaceholderText(text: String) {
    Text(
        text = text,
        style = SenkuTheme.typography.smallBody.copy(
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
        ),
        color = SenkuTheme.colors.ink3,
    )
}

@Composable
private fun RailActionButton(
    active: Boolean,
    glyph: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors

    Surface(
        modifier = Modifier
            .size(30.dp)
            .clickable(onClick = onClick),
        color = if (active) colors.ok.copy(alpha = 0.18f) else colors.bg0,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) colors.ok.copy(alpha = 0.5f) else colors.hairlineStrong,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = glyph,
        )
    }
}

@Composable
private fun BackGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(14.dp)) {
        val stroke = 1.7.dp.toPx()
        val startX = size.width * 0.74f
        val midX = size.width * 0.30f
        val topY = size.height * 0.20f
        val middleY = size.height * 0.50f
        val bottomY = size.height * 0.80f
        drawLine(colors.ink0, Offset(startX, topY), Offset(midX, middleY), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(startX, bottomY), Offset(midX, middleY), stroke, StrokeCap.Round)
    }
}

@Composable
private fun HomeGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(15.dp)) {
        val stroke = 1.6.dp.toPx()
        val left = size.width * 0.20f
        val right = size.width * 0.80f
        val top = size.height * 0.28f
        val roof = size.height * 0.08f
        val bottom = size.height * 0.82f
        val center = size.width * 0.50f
        drawLine(colors.ink0, Offset(left, top), Offset(center, roof), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(center, roof), Offset(right, top), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(left + 1.dp.toPx(), top), Offset(left + 1.dp.toPx(), bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(right - 1.dp.toPx(), top), Offset(right - 1.dp.toPx(), bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(left + 1.dp.toPx(), bottom), Offset(right - 1.dp.toPx(), bottom), stroke, StrokeCap.Round)
    }
}

@Composable
private fun PinGlyph(
    active: Boolean,
) {
    val colors = SenkuTheme.colors
    val tint = if (active) colors.ok else colors.ink1
    Canvas(modifier = Modifier.size(15.dp)) {
        val stroke = 1.6.dp.toPx()
        val top = size.height * 0.20f
        val bottom = size.height * 0.84f
        val left = size.width * 0.34f
        val right = size.width * 0.66f
        val center = size.width * 0.50f
        drawLine(tint, Offset(left, top), Offset(right, top), stroke, StrokeCap.Round)
        drawLine(tint, Offset(center, top), Offset(center, size.height * 0.58f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(left, top), Offset(center, size.height * 0.42f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(right, top), Offset(center, size.height * 0.42f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(center, size.height * 0.58f), Offset(center - 2.dp.toPx(), bottom), stroke, StrokeCap.Round)
    }
}

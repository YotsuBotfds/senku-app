package com.senku.ui.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuColorTokens
import com.senku.ui.theme.SenkuTheme

/**
 * Non-interactive telemetry summary. Accessibility stays on the root region so future mount
 * sites can keep this as a single summary line instead of a focusable token list.
 */
@Composable
fun MetaStrip(
    items: List<MetaItem>,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
) {
    if (items.isEmpty()) {
        return
    }

    val summary = remember(items) { buildAccessibilitySummary(items) }
    val semanticsModifier = modifier.clearAndSetSemantics {
        contentDescription = summary
    }

    when (orientation) {
        Orientation.Horizontal -> HorizontalMetaStrip(
            items = items,
            modifier = semanticsModifier,
        )

        Orientation.Vertical -> Column(
            modifier = semanticsModifier,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            items.forEach { item ->
                MetaToken(item = item)
            }
        }
    }
}

@Composable
private fun HorizontalMetaStrip(
    items: List<MetaItem>,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    MetaSeparator()
                }
                MetaToken(item = item)
            }
        },
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val maxWidth = if (constraints.maxWidth == Constraints.Infinity) {
            Int.MAX_VALUE
        } else {
            constraints.maxWidth
        }

        val tokenPlaceables = ArrayList<Placeable>(items.size)
        val separatorPlaceables = ArrayList<Placeable?>(items.size)
        var measurableIndex = 0

        items.indices.forEach { itemIndex ->
            if (itemIndex == 0) {
                separatorPlaceables += null
            } else {
                separatorPlaceables += measurables[measurableIndex++].measure(childConstraints)
            }
            tokenPlaceables += measurables[measurableIndex++].measure(childConstraints)
        }

        val placements = ArrayList<TokenPlacement>(items.size)
        var x = 0
        var y = 0
        var lineHeight = 0
        var contentWidth = 0
        var hasItemOnLine = false

        items.indices.forEach { index ->
            val token = tokenPlaceables[index]
            val separator = separatorPlaceables[index]
            val separatorWidth = if (hasItemOnLine && separator != null) separator.width else 0
            val requiredWidth = separatorWidth + token.width
            val shouldWrap = hasItemOnLine &&
                maxWidth != Int.MAX_VALUE &&
                x + requiredWidth > maxWidth

            if (shouldWrap) {
                y += lineHeight
                x = 0
                lineHeight = 0
                hasItemOnLine = false
            }

            val placeSeparator = hasItemOnLine && separator != null
            val separatorX = if (placeSeparator) x else null
            if (placeSeparator) {
                x += separator.width
            }

            placements += TokenPlacement(
                token = token,
                tokenX = x,
                tokenY = y,
                separator = if (placeSeparator) separator else null,
                separatorX = separatorX,
                separatorY = if (placeSeparator) y else null,
            )

            x += token.width
            lineHeight = maxOf(
                lineHeight,
                token.height,
                separator?.height ?: 0,
            )
            contentWidth = maxOf(contentWidth, x)
            hasItemOnLine = true
        }

        val layoutWidth = contentWidth
            .coerceAtLeast(constraints.minWidth)
            .coerceAtMost(maxWidth.coerceAtLeast(constraints.minWidth))
        val measuredHeight = y + lineHeight
        val layoutHeight = measuredHeight
            .coerceAtLeast(constraints.minHeight)
            .coerceAtMost(
                if (constraints.maxHeight == Constraints.Infinity) {
                    measuredHeight
                } else {
                    constraints.maxHeight
                },
            )

        layout(layoutWidth, layoutHeight) {
            placements.forEach { placement ->
                placement.separator?.placeRelative(
                    x = placement.separatorX ?: 0,
                    y = placement.separatorY ?: 0,
                )
                placement.token.placeRelative(
                    x = placement.tokenX,
                    y = placement.tokenY,
                )
            }
        }
    }
}

@Composable
private fun MetaToken(
    item: MetaItem,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val toneColor = item.tone.color(colors)
    val textStyle = SenkuTheme.typography.monoCaps.withColor(toneColor)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.showDot) {
            Spacer(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(toneColor),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = item.label.uppercase(),
            style = textStyle,
        )
    }
}

@Composable
private fun MetaSeparator() {
    Text(
        text = "\u00B7",
        modifier = Modifier.padding(horizontal = 8.dp),
        style = SenkuTheme.typography.monoCaps.withColor(
            SenkuTheme.colors.ink3.copy(alpha = 0.6f),
        ),
    )
}

private fun buildAccessibilitySummary(items: List<MetaItem>): String {
    return items.joinToString(separator = ", ") { item ->
        when (item.tone) {
            Tone.Default -> item.label
            Tone.Warn -> "${item.label} (warn)"
            Tone.Ok -> "${item.label} (ok)"
            Tone.Danger -> "${item.label} (danger)"
            Tone.Accent -> "${item.label} (accent)"
        }
    }
}

private fun Tone.color(colors: SenkuColorTokens): Color {
    return when (this) {
        Tone.Default -> colors.ink2
        Tone.Warn -> colors.warn
        Tone.Ok -> colors.ok
        Tone.Danger -> colors.danger
        Tone.Accent -> colors.accent
    }
}

private fun TextStyle.withColor(color: Color): TextStyle = copy(color = color)

private data class TokenPlacement(
    val token: Placeable,
    val tokenX: Int,
    val tokenY: Int,
    val separator: Placeable?,
    val separatorX: Int?,
    val separatorY: Int?,
)

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 320)
@Composable
private fun MetaStripHorizontalPreview() {
    SenkuAppTheme {
        MetaStrip(
            items = listOf(
                MetaItem(label = "answered", tone = Tone.Ok, showDot = true),
                MetaItem(label = "host GPU", tone = Tone.Accent),
                MetaItem(label = "2 sources"),
                MetaItem(label = "1 turn"),
                MetaItem(label = "low coverage", tone = Tone.Warn),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 220)
@Composable
private fun MetaStripVerticalPreview() {
    SenkuAppTheme {
        MetaStrip(
            items = listOf(
                MetaItem(label = "answered", tone = Tone.Ok, showDot = true),
                MetaItem(label = "host GPU", tone = Tone.Accent),
                MetaItem(label = "2 sources"),
            ),
            orientation = Orientation.Vertical,
            modifier = Modifier.padding(16.dp),
        )
    }
}

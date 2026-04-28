package com.senku.ui.evidence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme

private const val FallbackTitle = "Anchor guide"
private const val AnchorLabel = "anchor"
private const val EmptySnippetLabel = "No snippet yet."
internal val EvidenceSnippetPaddingHorizontal = 10.dp
internal val EvidenceSnippetPaddingVertical = 9.dp
internal val EvidenceSnippetSectionSpacing = 6.dp
internal val EvidenceSnippetTitleSize = 12.sp
internal val EvidenceSnippetTitleLineHeight = 16.sp
internal val EvidenceSnippetSnippetSize = 11.sp
internal val EvidenceSnippetSnippetLineHeight = 16.sp

@Immutable
data class EvidenceSnippetModel(
    val guideId: String,
    val title: String,
    val section: String = "",
    val snippet: String = "",
)

@Composable
fun EvidenceSnippet(
    evidence: EvidenceSnippetModel,
    onClick: ((EvidenceSnippetModel) -> Unit)? = null,
    modifier: Modifier = Modifier,
    titleMaxLines: Int = 2,
    snippetMaxLines: Int = 5,
) {
    val colors = SenkuTheme.colors
    val shape = RoundedCornerShape(8.dp)
    val content: @Composable () -> Unit = {
        EvidenceSnippetContent(
            evidence = evidence,
            titleMaxLines = titleMaxLines,
            snippetMaxLines = snippetMaxLines,
        )
    }

    if (onClick == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = colors.bg1,
            contentColor = colors.ink0,
            shape = shape,
            border = BorderStroke(0.5.dp, colors.hairline),
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = colors.bg1,
            contentColor = colors.ink0,
            shape = shape,
            border = BorderStroke(0.5.dp, colors.hairline),
            onClick = { onClick(evidence) },
        ) {
            content()
        }
    }
}

@Composable
private fun EvidenceSnippetContent(
    evidence: EvidenceSnippetModel,
    modifier: Modifier = Modifier,
    titleMaxLines: Int = 2,
    snippetMaxLines: Int = 6,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val hasSnippet = evidence.displaySnippet().isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = EvidenceSnippetPaddingHorizontal,
                vertical = EvidenceSnippetPaddingVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(EvidenceSnippetSectionSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = evidence.anchorLine(),
                modifier = Modifier.weight(1f),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            LaunchGlyph(
                modifier = Modifier.size(11.dp),
            )
        }

        Text(
            text = evidence.displayTitle(),
            style = typography.uiBody.copy(
                fontSize = EvidenceSnippetTitleSize,
                lineHeight = EvidenceSnippetTitleLineHeight,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.ink0,
            maxLines = titleMaxLines.coerceAtLeast(1),
            overflow = TextOverflow.Ellipsis,
        )

        evidence.displaySection().takeIf { it.isNotEmpty() }?.let { section ->
            Text(
                text = section,
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                ),
                color = colors.ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.hairlineStrong)
        )

        Text(
            text = evidence.quotedSnippet(),
            style = typography.answerBody.copy(
                fontSize = EvidenceSnippetSnippetSize,
                lineHeight = EvidenceSnippetSnippetLineHeight,
                fontWeight = FontWeight.Normal,
            ),
            color = if (hasSnippet) colors.ink1 else colors.ink3,
            maxLines = snippetMaxLines.coerceAtLeast(1),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal fun EvidenceSnippetModel.anchorLine(): String =
    listOf(displayGuideId(), AnchorLabel)
        .filter { it.isNotEmpty() }
        .joinToString(" ")

internal fun EvidenceSnippetModel.displayGuideId(): String =
    guideId.trim()

internal fun EvidenceSnippetModel.displayTitle(): String =
    title.trim().ifEmpty { FallbackTitle }

internal fun EvidenceSnippetModel.displaySection(): String =
    section.trim()

internal fun EvidenceSnippetModel.displaySnippet(): String =
    snippet.trim()

internal fun EvidenceSnippetModel.quotedSnippet(): String {
    val value = displaySnippet()
    return if (value.isEmpty()) {
        EmptySnippetLabel
    } else {
        "\"" + value + "\""
    }
}

@Composable
private fun LaunchGlyph(
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    Canvas(modifier = modifier) {
        val stroke = 1.3.dp.toPx()
        val left = size.width * 0.18f
        val top = size.height * 0.22f
        val right = size.width * 0.82f
        val bottom = size.height * 0.86f
        val arrowStartX = size.width * 0.34f
        val arrowStartY = size.height * 0.68f
        val arrowEndX = size.width * 0.78f
        val arrowEndY = size.height * 0.24f

        drawLine(
            color = colors.accent,
            start = Offset(left, bottom),
            end = Offset(left, top),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = colors.accent,
            start = Offset(left, bottom),
            end = Offset(right, bottom),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = colors.accent,
            start = Offset(arrowStartX, arrowStartY),
            end = Offset(arrowEndX, arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = colors.accent,
            start = Offset(size.width * 0.58f, top),
            end = Offset(arrowEndX, arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = colors.accent,
            start = Offset(right, size.height * 0.44f),
            end = Offset(arrowEndX, arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 320)
@Composable
private fun EvidenceSnippetPreview() {
    SenkuAppTheme {
        Surface(
            color = SenkuTheme.colors.bg0,
            modifier = Modifier.padding(16.dp),
        ) {
            EvidenceSnippet(
                evidence = EvidenceSnippetModel(
                    guideId = "GD-214",
                    title = "Water purification and storage",
                    section = "treated water handling",
                    snippet = "Bring the water to a rolling boil, then let it cool covered before storing it in a clean container.",
                ),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 320)
@Composable
private fun EvidenceSnippetEmptyPreview() {
    SenkuAppTheme {
        Surface(
            color = SenkuTheme.colors.bg0,
            modifier = Modifier.padding(16.dp),
        ) {
            EvidenceSnippet(
                evidence = EvidenceSnippetModel(
                    guideId = "",
                    title = "",
                    section = "",
                    snippet = "",
                ),
            )
        }
    }
}

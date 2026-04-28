package com.senku.ui.evidence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme

private const val XRefFallbackTitle = "Guide connection"

@Immutable
data class XRefRowModel(
    val guideId: String,
    val title: String,
)

@Composable
fun XRefRow(
    xRef: XRefRowModel,
    onClick: (XRefRowModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bg1,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colors.hairline),
        onClick = { onClick(xRef) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 38.dp)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = xRef.displayGuideId(),
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                ),
                color = colors.ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = xRef.displayTitle(),
                modifier = Modifier.weight(1f),
                style = SenkuTheme.typography.uiBody.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                ),
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(6.dp))
            ChevronGlyph(
                modifier = Modifier.size(10.dp),
            )
        }
    }
}

private fun XRefRowModel.displayGuideId(): String =
    guideId.trim()

private fun XRefRowModel.displayTitle(): String =
    title.trim().ifEmpty { XRefFallbackTitle }

@Composable
private fun ChevronGlyph(
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val startX = size.width * 0.28f
        val midX = size.width * 0.68f
        val topY = size.height * 0.22f
        val middleY = size.height * 0.50f
        val bottomY = size.height * 0.78f

        drawLine(
            color = colors.ink2,
            start = Offset(startX, topY),
            end = Offset(midX, middleY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = colors.ink2,
            start = Offset(startX, bottomY),
            end = Offset(midX, middleY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 320)
@Composable
private fun XRefRowPreview() {
    SenkuAppTheme {
        Surface(
            color = SenkuTheme.colors.bg0,
            modifier = Modifier.padding(16.dp),
        ) {
            XRefRow(
                xRef = XRefRowModel(
                    guideId = "GD-118",
                    title = "Emergency ash filtration",
                ),
                onClick = {},
            )
        }
    }
}

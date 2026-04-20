package com.senku.ui.primitives

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senku.mobile.R
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuColorTokens
import com.senku.ui.theme.SenkuTheme

private const val PivotFallbackGuideId = "GD-?"
private const val PivotFallbackTitle = "Pivot guide"

@Immutable
data class PivotRowModel(
    val guideId: String,
    val title: String,
    val isPrimary: Boolean = false,
)

@Composable
fun PivotRow(
    pivot: PivotRowModel,
    onClick: (PivotRowModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val corner = dimensionResource(R.dimen.senku_rev03_corner_small)
    val horizontalPadding = dimensionResource(R.dimen.senku_rev03_space_10)
    val verticalPadding = dimensionResource(R.dimen.senku_rev03_space_8)
    val contentGap = dimensionResource(R.dimen.senku_rev03_space_8)
    val containerColor = pivot.containerColor(colors)
    val borderColor = if (pivot.isPrimary) {
        colors.accent.copy(alpha = 0.28f)
    } else {
        colors.hairlineStrong
    }
    val idColor = if (pivot.isPrimary) colors.accent else colors.ink2

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(corner),
        border = BorderStroke(1.dp, borderColor),
        onClick = { onClick(pivot) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = pivot.displayGuideId(),
                style = SenkuTheme.typography.monoCaps,
                color = idColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(contentGap))
            Text(
                text = pivot.displayTitle(),
                modifier = Modifier.weight(1f),
                style = SenkuTheme.typography.uiBody,
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun PivotRowModel.displayGuideId(): String = guideId.trim().ifEmpty { PivotFallbackGuideId }

private fun PivotRowModel.displayTitle(): String = title.trim().ifEmpty { PivotFallbackTitle }

private fun PivotRowModel.containerColor(colors: SenkuColorTokens): Color {
    return if (isPrimary) {
        colors.accent.copy(alpha = 0.10f).compositeOver(colors.bg1)
    } else {
        colors.bg1
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 360)
@Composable
private fun PivotRowPreview() {
    SenkuAppTheme {
        Surface(color = SenkuTheme.colors.bg0) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.senku_rev03_space_16)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.senku_rev03_space_8)),
            ) {
                PivotRow(
                    pivot = PivotRowModel(
                        guideId = "GD-214",
                        title = "Water cache rotation",
                        isPrimary = true,
                    ),
                    onClick = {},
                )
                PivotRow(
                    pivot = PivotRowModel(
                        guideId = "GD-118",
                        title = "Emergency ash filtration",
                    ),
                    onClick = {},
                )
            }
        }
    }
}

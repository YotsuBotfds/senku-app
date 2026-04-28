package com.senku.ui.home

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.dimensionResource
import com.senku.mobile.R
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.Locale

enum class IdentityStripLayoutMode {
    HORIZONTAL,
    VERTICAL,
}

enum class IdentityStripStatusTone {
    OK,
    ACCENT,
}

data class IdentityStripModel(
    val title: String,
    val subtitle: String,
    val statusLabel: String,
    val badgeLetter: String = "S",
    val statusTone: IdentityStripStatusTone = IdentityStripStatusTone.OK,
)

class IdentityStripHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    private var model: IdentityStripModel by mutableStateOf(
        IdentityStripModel(
            title = "Senku",
            subtitle = "PACK LOADING",
            statusLabel = "Loading",
            statusTone = IdentityStripStatusTone.ACCENT,
        ),
    )
    private var layoutMode: IdentityStripLayoutMode by mutableStateOf(IdentityStripLayoutMode.HORIZONTAL)

    fun updateModel(
        model: IdentityStripModel,
        layoutMode: IdentityStripLayoutMode,
    ) {
        this.model = model
        this.layoutMode = layoutMode
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            IdentityStrip(
                model = model,
                layoutMode = layoutMode,
            )
        }
    }
}

@Composable
fun IdentityStrip(
    model: IdentityStripModel,
    layoutMode: IdentityStripLayoutMode,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val contentGap = dimensionResource(R.dimen.senku_rev03_space_10)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bg1,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(dimensionResource(R.dimen.senku_rev03_corner_medium)),
        border = BorderStroke(1.dp, colors.hairlineStrong),
    ) {
        if (layoutMode == IdentityStripLayoutMode.VERTICAL) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.senku_rev03_space_12),
                        vertical = dimensionResource(R.dimen.senku_rev03_space_12),
                    ),
                verticalArrangement = Arrangement.spacedBy(contentGap),
            ) {
                HeaderRow(
                    model = model,
                    stackVertically = true,
                )
                StatusPill(model = model)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .padding(
                        horizontal = dimensionResource(R.dimen.senku_rev03_space_12),
                        vertical = dimensionResource(R.dimen.senku_rev03_space_10),
                    ),
                horizontalArrangement = Arrangement.spacedBy(contentGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderRow(
                    model = model,
                    modifier = Modifier.weight(1f),
                    stackVertically = false,
                )
                StatusPill(model = model)
            }
        }
    }
}

@Composable
private fun HeaderRow(
    model: IdentityStripModel,
    stackVertically: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val gap = dimensionResource(R.dimen.senku_rev03_space_10)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BadgeTile(letter = model.badgeLetter)
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = model.title,
                style = SenkuTheme.typography.uiBody.copy(
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = model.subtitle.uppercase(Locale.US),
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink2,
                maxLines = if (stackVertically) 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BadgeTile(letter: String) {
    val colors = SenkuTheme.colors
    val tileSize = dimensionResource(R.dimen.senku_rev03_identity_strip_height) + 2.dp
    Box(
        modifier = Modifier
            .size(tileSize)
            .background(colors.bg2, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.trim().ifEmpty { "S" },
            style = SenkuTheme.typography.uiBody.copy(
                fontSize = 17.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = colors.paperInk,
            maxLines = 1,
        )
    }
}

@Composable
private fun StatusPill(model: IdentityStripModel) {
    val colors = SenkuTheme.colors
    val background = when (model.statusTone) {
        IdentityStripStatusTone.OK -> colors.ok.copy(alpha = 0.18f)
        IdentityStripStatusTone.ACCENT -> colors.accent.copy(alpha = 0.16f)
    }
    val border = when (model.statusTone) {
        IdentityStripStatusTone.OK -> colors.ok.copy(alpha = 0.40f)
        IdentityStripStatusTone.ACCENT -> colors.accent.copy(alpha = 0.34f)
    }
    val textColor = when (model.statusTone) {
        IdentityStripStatusTone.OK -> colors.ok
        IdentityStripStatusTone.ACCENT -> colors.accent
    }

    Surface(
        color = background,
        contentColor = textColor,
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = model.statusLabel.uppercase(Locale.US),
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = textColor,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 380)
@Composable
private fun IdentityStripHorizontalPreview() {
    SenkuAppTheme {
        IdentityStrip(
            model = IdentityStripModel(
                title = "Senku",
                subtitle = "754 guides \u00b7 manual ed. 2",
                statusLabel = "Pack ready",
            ),
            layoutMode = IdentityStripLayoutMode.HORIZONTAL,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 320)
@Composable
private fun IdentityStripVerticalPreview() {
    SenkuAppTheme {
        IdentityStrip(
            model = IdentityStripModel(
                title = "Senku",
                subtitle = "754 guides \u00b7 manual ed. 2",
                statusLabel = "Ready",
            ),
            layoutMode = IdentityStripLayoutMode.VERTICAL,
            modifier = Modifier
                .width(220.dp)
                .padding(16.dp),
        )
    }
}

package com.senku.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

object SenkuTheme {
    val colors: SenkuColorTokens
        @Composable get() = LocalSenkuColors.current

    val typography: SenkuTypographyTokens
        @Composable get() = LocalSenkuTypography.current
}

@Composable
fun SenkuAppTheme(content: @Composable () -> Unit) {
    val colors = DefaultSenkuColors
    val typography = DefaultSenkuTypography
    val materialColors = darkColorScheme(
        primary = colors.accent,
        secondary = colors.olive40,
        tertiary = colors.ok,
        background = colors.bg0,
        surface = colors.bg1,
        surfaceVariant = colors.bg2,
        onPrimary = colors.paperInk,
        onSecondary = colors.ink0,
        onBackground = colors.ink0,
        onSurface = colors.ink0,
        outline = colors.hairlineStrong,
        error = colors.danger,
        onError = colors.paperInk,
    )
    val materialTypography = MaterialTheme.typography.copy(
        displayLarge = typography.canvasTitle,
        headlineSmall = typography.sectionTitle,
        bodyLarge = typography.answerBody,
        bodyMedium = typography.uiBody,
        bodySmall = typography.smallBody,
        labelLarge = typography.tag,
        labelSmall = typography.monoCaps,
    )

    CompositionLocalProvider(
        LocalSenkuColors provides colors,
        LocalSenkuTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = materialTypography,
            content = content,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16)
@Composable
private fun SenkuThemePreview() {
    SenkuAppTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SenkuTheme.colors.bg0,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Rev 03 Token Freeze",
                    style = SenkuTheme.typography.canvasTitle,
                    color = SenkuTheme.colors.ink0,
                )
                Text(
                    text = "Paper answer card typography is ready for the first Compose primitives.",
                    style = SenkuTheme.typography.answerBody,
                    color = SenkuTheme.colors.paper,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ToneDot(SenkuTheme.colors.ok)
                    Text(
                        text = "answered",
                        style = SenkuTheme.typography.monoCaps,
                        color = SenkuTheme.colors.ok,
                    )
                    Text(
                        text = "host gpu",
                        style = SenkuTheme.typography.monoCaps,
                        color = SenkuTheme.colors.accent,
                    )
                    Text(
                        text = "2 sources",
                        style = SenkuTheme.typography.monoCaps,
                        color = SenkuTheme.colors.ink2,
                    )
                }
                Surface(
                    color = SenkuTheme.colors.paper,
                    contentColor = SenkuTheme.colors.paperInk,
                    shape = RoundedCornerShape(14.dp),
                    tonalElevation = 1.dp,
                    shadowElevation = 6.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Senku answered",
                            style = SenkuTheme.typography.monoCaps,
                            color = SenkuTheme.colors.paperInk,
                        )
                        Text(
                            text = "Boil suspect water, let it cool covered, and keep raw collection tools separate from your clean container.",
                            style = SenkuTheme.typography.answerBody,
                            color = SenkuTheme.colors.paperInk,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "limits & safety",
                            style = SenkuTheme.typography.monoCaps,
                            color = SenkuTheme.colors.danger,
                        )
                        Text(
                            text = "If chemical contamination is possible, boiling alone is not enough.",
                            style = SenkuTheme.typography.smallBody,
                            color = SenkuTheme.colors.paperInk,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToneDot(color: Color) {
    Spacer(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color),
    )
}

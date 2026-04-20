package com.senku.ui.primitives

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme

enum class TopBarActionKind {
    Back,
    Home,
    Pin,
    Share,
}

@Immutable
data class TopBarActionSpec(
    val kind: TopBarActionKind,
    val contentDescription: String,
    val isVisible: Boolean = true,
    val isEnabled: Boolean = true,
    val isActive: Boolean = false,
) {
    companion object {
        fun back(
            contentDescription: String,
            isEnabled: Boolean = true,
        ) = TopBarActionSpec(
            kind = TopBarActionKind.Back,
            contentDescription = contentDescription,
            isEnabled = isEnabled,
        )

        fun home(
            contentDescription: String,
            isVisible: Boolean = true,
            isEnabled: Boolean = true,
        ) = TopBarActionSpec(
            kind = TopBarActionKind.Home,
            contentDescription = contentDescription,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )

        fun pin(
            contentDescription: String,
            isVisible: Boolean = true,
            isEnabled: Boolean = true,
            isActive: Boolean = false,
        ) = TopBarActionSpec(
            kind = TopBarActionKind.Pin,
            contentDescription = contentDescription,
            isVisible = isVisible,
            isEnabled = isEnabled,
            isActive = isActive,
        )

        fun share(
            contentDescription: String,
            isVisible: Boolean = true,
            isEnabled: Boolean = true,
        ) = TopBarActionSpec(
            kind = TopBarActionKind.Share,
            contentDescription = contentDescription,
            isVisible = isVisible,
            isEnabled = isEnabled,
        )
    }
}

@Composable
fun SenkuTopBar(
    title: String,
    subtitle: String? = null,
    actions: List<TopBarActionSpec>,
    onActionClick: (TopBarActionKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val visibleActions = remember(actions) {
        actions
            .filter { it.isVisible }
            .sortedBy { it.kind.ordinal }
            .distinctBy { it.kind }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (visibleActions.isNotEmpty()) {
            Surface(
                color = colors.bg2.copy(alpha = 0.96f),
                contentColor = colors.ink0,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, colors.hairlineStrong),
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    visibleActions.forEach { action ->
                        TopBarActionButton(
                            action = action,
                            onClick = { onActionClick(action.kind) },
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = typography.sectionTitle,
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = typography.monoCaps,
                    color = colors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(percent = 100))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.accent.copy(alpha = 0.92f),
                                colors.hairlineStrong,
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun TopBarActionButton(
    action: TopBarActionSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val shape = RoundedCornerShape(14.dp)
    val containerColor = when {
        action.isActive -> colors.ok.copy(alpha = 0.18f)
        action.isEnabled -> colors.bg1.copy(alpha = 0.78f)
        else -> colors.bg1.copy(alpha = 0.42f)
    }
    val borderColor = when {
        action.isActive -> colors.ok.copy(alpha = 0.62f)
        else -> colors.hairlineStrong
    }
    val iconTint = when {
        !action.isEnabled -> colors.ink3
        action.isActive -> colors.ok
        action.kind == TopBarActionKind.Share -> colors.accent
        action.kind == TopBarActionKind.Back -> colors.ink0
        else -> colors.ink1
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(containerColor)
            .clickable(
                enabled = action.isEnabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = action.contentDescription
                if (action.kind == TopBarActionKind.Pin) {
                    selected = action.isActive
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.matchParentSize(),
            color = Color.Transparent,
            shape = shape,
            border = BorderStroke(1.dp, borderColor),
        ) {}

        Icon(
            imageVector = action.kind.icon(),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
    }
}

private fun TopBarActionKind.icon(): ImageVector = when (this) {
    TopBarActionKind.Back -> SenkuTopBarIcons.Back
    TopBarActionKind.Home -> SenkuTopBarIcons.Home
    TopBarActionKind.Pin -> SenkuTopBarIcons.Pin
    TopBarActionKind.Share -> SenkuTopBarIcons.Share
}

private object SenkuTopBarIcons {
    val Back: ImageVector by lazy {
        ImageVector.Builder(
            name = "TopBarBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(20f, 11f)
                lineTo(8.83f, 11f)
                lineTo(13.71f, 6.12f)
                lineTo(12.29f, 4.71f)
                lineTo(5f, 12f)
                lineTo(12.29f, 19.29f)
                lineTo(13.71f, 17.88f)
                lineTo(8.83f, 13f)
                lineTo(20f, 13f)
                close()
            }
        }.build()
    }

    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "TopBarHome",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 4f)
                lineTo(4f, 10.5f)
                lineTo(5.4f, 11.9f)
                lineTo(6.5f, 11.01f)
                lineTo(6.5f, 19f)
                lineTo(10.25f, 19f)
                lineTo(10.25f, 14.5f)
                lineTo(13.75f, 14.5f)
                lineTo(13.75f, 19f)
                lineTo(17.5f, 19f)
                lineTo(17.5f, 11.01f)
                lineTo(18.6f, 11.9f)
                lineTo(20f, 10.5f)
                close()
            }
        }.build()
    }

    val Pin: ImageVector by lazy {
        ImageVector.Builder(
            name = "TopBarPin",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(9f, 5f)
                lineTo(15f, 5f)
                lineTo(17f, 9f)
                lineTo(13.25f, 11.5f)
                lineTo(13.25f, 14.25f)
                lineTo(16f, 17f)
                lineTo(14.75f, 18.25f)
                lineTo(12f, 15.5f)
                lineTo(9.5f, 21f)
                lineTo(8.4f, 20.5f)
                lineTo(10.4f, 14.6f)
                lineTo(7f, 11.2f)
                lineTo(9f, 9f)
                close()
            }
        }.build()
    }

    val Share: ImageVector by lazy {
        ImageVector.Builder(
            name = "TopBarShare",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(5f, 20f)
                lineTo(19f, 20f)
                lineTo(19f, 13.5f)
                lineTo(17f, 13.5f)
                lineTo(17f, 18f)
                lineTo(7f, 18f)
                lineTo(7f, 13.5f)
                lineTo(5f, 13.5f)
                close()
                moveTo(12f, 4f)
                lineTo(7f, 9f)
                lineTo(8.41f, 10.41f)
                lineTo(11f, 7.83f)
                lineTo(11f, 16f)
                lineTo(13f, 16f)
                lineTo(13f, 7.83f)
                lineTo(15.59f, 10.41f)
                lineTo(17f, 9f)
                close()
            }
        }.build()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 380)
@Composable
private fun SenkuTopBarPreview() {
    SenkuAppTheme {
        Surface(
            color = SenkuTheme.colors.bg0,
            contentColor = SenkuTheme.colors.ink0,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                SenkuTopBar(
                    title = "Copper sulfate troubleshooting",
                    subtitle = "GD-235 | HOST GPU",
                    actions = listOf(
                        TopBarActionSpec.back("Back to previous screen"),
                        TopBarActionSpec.home("Return to home"),
                        TopBarActionSpec.pin(
                            contentDescription = "Pin guide for quick access",
                            isVisible = true,
                            isActive = false,
                        ),
                        TopBarActionSpec.share(
                            contentDescription = "Share transcript",
                            isVisible = true,
                        ),
                    ),
                    onActionClick = {},
                )

                SenkuTopBar(
                    title = "Emergency shelter drainage",
                    subtitle = "GD-811 | SAVED",
                    actions = listOf(
                        TopBarActionSpec.back("Back to previous screen"),
                        TopBarActionSpec.home(
                            contentDescription = "Return to home",
                            isVisible = false,
                        ),
                        TopBarActionSpec.pin(
                            contentDescription = "Remove guide from quick access",
                            isVisible = true,
                            isActive = true,
                        ),
                        TopBarActionSpec.share(
                            contentDescription = "Share transcript",
                            isVisible = true,
                            isEnabled = false,
                        ),
                    ),
                    onActionClick = {},
                )
            }
        }
    }
}

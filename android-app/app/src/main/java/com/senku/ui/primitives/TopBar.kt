package com.senku.ui.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.sp
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
    dangerPillLabel: String? = null,
    actions: List<TopBarActionSpec>,
    onActionClick: (TopBarActionKind) -> Unit,
    modifier: Modifier = Modifier,
    titleMaxLines: Int = 1,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val actionLayout = remember(actions) {
        topBarActionLayout(actions)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actionLayout.leading.forEach { action ->
            TopBarActionButton(
                action = action,
                onClick = { onActionClick(action.kind) },
            )
        }
        if (actionLayout.leading.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(colors.hairlineStrong),
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = typography.uiBody.copy(fontSize = 14.sp, lineHeight = 17.sp),
                color = colors.ink0,
                maxLines = titleMaxLines.coerceAtLeast(1),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
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
                    .padding(top = 3.dp)
                    .height(1.dp)
                    .background(colors.hairlineStrong),
            )
        }

        if (!dangerPillLabel.isNullOrBlank() || actionLayout.trailing.isNotEmpty()) {
            Spacer(modifier = Modifier.width(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!dangerPillLabel.isNullOrBlank()) {
                    DangerPill(label = dangerPillLabel)
                }
                actionLayout.trailing.forEach { action ->
                    TopBarActionButton(
                        action = action,
                        onClick = { onActionClick(action.kind) },
                    )
                }
            }
        }
    }
}

internal data class TopBarActionLayout(
    val leading: List<TopBarActionSpec>,
    val trailing: List<TopBarActionSpec>,
)

internal fun topBarActionLayout(actions: List<TopBarActionSpec>): TopBarActionLayout {
    val visibleDistinct = actions
        .asSequence()
        .filter { it.isVisible }
        .distinctBy { it.kind }
        .toList()
    return TopBarActionLayout(
        leading = visibleDistinct.filter { it.kind == TopBarActionKind.Back },
        trailing = visibleDistinct
            .filter { it.kind != TopBarActionKind.Back }
            .sortedBy { it.kind.trailingOrder() },
    )
}

private fun TopBarActionKind.trailingOrder(): Int = when (this) {
    TopBarActionKind.Home -> 0
    TopBarActionKind.Share -> 1
    TopBarActionKind.Pin -> 2
    TopBarActionKind.Back -> 3
}

@Composable
private fun DangerPill(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.danger.copy(alpha = 0.14f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u2022 ${label.uppercase()}",
            style = SenkuTheme.typography.monoCaps.copy(fontSize = 10.sp, lineHeight = 12.sp),
            color = colors.danger,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun TopBarActionButton(
    action: TopBarActionSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val shape = RoundedCornerShape(6.dp)
    val containerColor = when {
        action.isActive -> colors.ok.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    val iconTint = when {
        !action.isEnabled -> colors.ink3
        action.isActive -> colors.ok
        action.kind == TopBarActionKind.Share -> colors.ink1
        action.kind == TopBarActionKind.Back -> colors.ink1
        else -> colors.ink1
    }

    Box(
        modifier = modifier
            .size(30.dp)
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
        Icon(
            imageVector = action.kind.icon(),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun TopBarActionKind.icon(): ImageVector = when (this) {
    TopBarActionKind.Back -> SenkuTopBarIcons.Back
    TopBarActionKind.Home -> SenkuTopBarIcons.Home
    TopBarActionKind.Pin -> SenkuTopBarIcons.More
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
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(15f, 6f)
                lineTo(9f, 12f)
                lineTo(15f, 18f)
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
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(4.5f, 11.2f)
                lineTo(12f, 5.2f)
                lineTo(19.5f, 11.2f)
                moveTo(6.8f, 10.4f)
                lineTo(6.8f, 19f)
                lineTo(10.2f, 19f)
                lineTo(10.2f, 14.2f)
                lineTo(13.8f, 14.2f)
                lineTo(13.8f, 19f)
                lineTo(17.2f, 19f)
                lineTo(17.2f, 10.4f)
            }
        }.build()
    }

    val More: ImageVector by lazy {
        ImageVector.Builder(
            name = "TopBarMore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(6f, 12f)
                lineTo(6.01f, 12f)
                moveTo(5f, 12f)
                arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
                arcToRelative(1f, 1f, 0f, true, false, -2f, 0f)
                moveTo(12f, 12f)
                lineTo(12.01f, 12f)
                moveTo(11f, 12f)
                arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
                arcToRelative(1f, 1f, 0f, true, false, -2f, 0f)
                moveTo(18f, 12f)
                lineTo(18.01f, 12f)
                moveTo(17f, 12f)
                arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
                arcToRelative(1f, 1f, 0f, true, false, -2f, 0f)
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
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(8.4f, 12.3f)
                lineTo(15.6f, 8.4f)
                moveTo(8.4f, 13.7f)
                lineTo(15.6f, 17.6f)
            }
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(6f, 15f)
                arcToRelative(3f, 3f, 0f, true, false, 0f, -6f)
                arcToRelative(3f, 3f, 0f, false, false, 0f, 6f)
                moveTo(18f, 10.5f)
                arcToRelative(3f, 3f, 0f, true, false, 0f, -6f)
                arcToRelative(3f, 3f, 0f, false, false, 0f, 6f)
                moveTo(18f, 19.5f)
                arcToRelative(3f, 3f, 0f, true, false, 0f, -6f)
                arcToRelative(3f, 3f, 0f, false, false, 0f, 6f)
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

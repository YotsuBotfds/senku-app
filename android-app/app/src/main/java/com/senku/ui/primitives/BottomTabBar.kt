package com.senku.ui.primitives

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme

enum class BottomTabDestination {
    HOME,
    SEARCH,
    ASK,
    THREADS,
    PINS,
}

enum class BottomTabBarLayoutMode {
    HORIZONTAL_BAR,
    VERTICAL_RAIL,
}

data class BottomTabModel(
    val destination: BottomTabDestination,
    val label: String,
    val contentDescription: String = label,
)

fun interface BottomTabSelectionHandler {
    fun onTabSelected(destination: BottomTabDestination)
}

class BottomTabBarHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    private var tabs: List<BottomTabModel> by mutableStateOf(defaultTabs())
    private var activeTab: BottomTabDestination by mutableStateOf(BottomTabDestination.HOME)
    private var layoutMode: BottomTabBarLayoutMode by mutableStateOf(BottomTabBarLayoutMode.HORIZONTAL_BAR)
    private var selectionHandler: BottomTabSelectionHandler? = null

    fun setTabs(
        value: List<BottomTabModel>,
        activeTab: BottomTabDestination,
        selectionHandler: BottomTabSelectionHandler?,
    ) {
        tabs = value.toList()
        this.activeTab = activeTab
        this.selectionHandler = selectionHandler
    }

    fun updateLayoutMode(value: BottomTabBarLayoutMode) {
        layoutMode = value
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            SenkuBottomTabBar(
                tabs = tabs,
                activeTab = activeTab,
                layoutMode = layoutMode,
                onTabSelected = { destination -> selectionHandler?.onTabSelected(destination) },
            )
        }
    }

    companion object {
        fun defaultTabs(): List<BottomTabModel> = listOf(
            BottomTabModel(BottomTabDestination.HOME, "Home"),
            BottomTabModel(BottomTabDestination.SEARCH, "Search"),
            BottomTabModel(BottomTabDestination.ASK, "Ask"),
            BottomTabModel(BottomTabDestination.THREADS, "Threads"),
            BottomTabModel(BottomTabDestination.PINS, "Pins"),
        )
    }
}

@Composable
fun SenkuBottomTabBar(
    tabs: List<BottomTabModel>,
    activeTab: BottomTabDestination,
    layoutMode: BottomTabBarLayoutMode,
    onTabSelected: (BottomTabDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    if (layoutMode == BottomTabBarLayoutMode.VERTICAL_RAIL) {
        Surface(
            modifier = modifier.fillMaxHeight(),
            color = colors.bg0,
            contentColor = colors.ink0,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(colors.bg0),
            ) {
                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .fillMaxHeight()
                        .padding(start = 4.dp, top = 7.dp, end = 4.dp, bottom = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    tabs.forEach { tab ->
                        val selected = tab.destination == activeTab
                        BottomTabItem(
                            tab = tab,
                            selected = selected,
                            layoutMode = layoutMode,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onTabSelected(tab.destination) },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.hairline),
                )
            }
        }
        return
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bg0,
        contentColor = colors.ink0,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bg0),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.hairline),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tabs.forEach { tab ->
                    val selected = tab.destination == activeTab
                    BottomTabItem(
                        tab = tab,
                        selected = selected,
                        layoutMode = layoutMode,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(tab.destination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    tab: BottomTabModel,
    selected: Boolean,
    layoutMode: BottomTabBarLayoutMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val tint = if (selected) colors.accent else colors.ink3
    val verticalRail = layoutMode == BottomTabBarLayoutMode.VERTICAL_RAIL
    val itemHeight = if (verticalRail) 46.dp else 40.dp

    Column(
        modifier = modifier
            .height(itemHeight)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(horizontal = if (verticalRail) 2.dp else 4.dp, vertical = if (verticalRail) 2.dp else 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        BottomTabIcon(
            destination = tab.destination,
            tint = tint,
            modifier = Modifier.size(if (verticalRail) 15.dp else 16.dp),
        )
        Text(
            text = tab.label,
            style = SenkuTheme.typography.tag.copy(
                    fontSize = if (verticalRail) 9.5.sp else 9.5.sp,
                    lineHeight = if (verticalRail) 10.5.sp else 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            ),
            color = tint,
            maxLines = if (verticalRail) 2 else 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .width(if (selected) 22.dp else 0.dp)
                .height(if (selected) 2.dp else 1.dp)
                .background(if (selected) colors.accent else Color.Transparent),
        )
    }
}

@Composable
private fun BottomTabIcon(
    destination: BottomTabDestination,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val stroke = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        when (destination) {
            BottomTabDestination.HOME -> {
                listOf(0.34f, 0.50f, 0.66f).forEach { y ->
                    drawLine(
                        color = tint,
                        start = Offset(size.width * 0.28f, size.height * y),
                        end = Offset(size.width * 0.72f, size.height * y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }

            BottomTabDestination.SEARCH -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * 0.44f, size.height * 0.42f),
                    style = stroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.58f, size.height * 0.56f),
                    end = Offset(size.width * 0.80f, size.height * 0.78f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }

            BottomTabDestination.ASK -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.20f),
                    size = Size(size.width * 0.68f, size.height * 0.46f),
                    cornerRadius = CornerRadius(size.width * 0.16f, size.width * 0.16f),
                    style = stroke,
                )
                val tail = Path().apply {
                    moveTo(size.width * 0.34f, size.height * 0.66f)
                    lineTo(size.width * 0.30f, size.height * 0.82f)
                    lineTo(size.width * 0.46f, size.height * 0.68f)
                }
                drawPath(
                    path = tail,
                    color = tint,
                    style = stroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.34f, size.height * 0.40f),
                    end = Offset(size.width * 0.66f, size.height * 0.40f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.34f, size.height * 0.52f),
                    end = Offset(size.width * 0.58f, size.height * 0.52f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }

            BottomTabDestination.THREADS -> {
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.20f, size.height * 0.30f),
                    end = Offset(size.width * 0.46f, size.height * 0.50f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.20f, size.height * 0.70f),
                    end = Offset(size.width * 0.46f, size.height * 0.50f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.46f, size.height * 0.50f),
                    end = Offset(size.width * 0.80f, size.height * 0.50f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }

            BottomTabDestination.PINS -> {
                val bookmark = Path().apply {
                    moveTo(size.width * 0.32f, size.height * 0.20f)
                    lineTo(size.width * 0.68f, size.height * 0.20f)
                    lineTo(size.width * 0.68f, size.height * 0.78f)
                    lineTo(size.width * 0.50f, size.height * 0.64f)
                    lineTo(size.width * 0.32f, size.height * 0.78f)
                    close()
                }
                drawPath(
                    path = bookmark,
                    color = tint,
                    style = stroke,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 390)
@Composable
private fun BottomTabBarPreview() {
    SenkuAppTheme {
        SenkuBottomTabBar(
            tabs = BottomTabBarHostView.defaultTabs(),
            activeTab = BottomTabDestination.SEARCH,
            layoutMode = BottomTabBarLayoutMode.HORIZONTAL_BAR,
            onTabSelected = {},
        )
    }
}
